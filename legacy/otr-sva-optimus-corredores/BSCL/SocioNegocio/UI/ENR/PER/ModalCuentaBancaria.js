/*************************************************************
 * Purpose	: Modal Cuenta Bancaria.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalCuentaBancariaEnrolCtrl', function($scope,
		$translate, servicioSociodeNegocio, bcsComunes, bcsServicioPlataforma, bcsAlerta,OPCIONES_BCSALERTA) {

		var mcb = this;

		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);

		var overlay = bcsComunes.overlay;
		mcb.IdEnrolamiento = parametros.IdEnrolamiento;
		mcb.CodigoTipoPersona = parametros.CodigoTipoPersona;
		mcb.CuentaBancaria = $.extend({}, parametros.CuentaBancaria);
		mcb.CuentaBancariaValida = false;
		mcb.CuentasBancarias = parametros.CuentasBancarias;
		mcb.camposRequeridos = {};
		mcb.CodigoFormulario = parametros.CodigoFormulario;
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mcb.CodigoFormulario, function(respuesta) {
			mcb.camposRequeridos = respuesta;
			mcb.cargarDatos();
		});
		
		mcb.cargarDatos = function(){
			if (parametros.CuentaBancaria) {
				mcb.CuentaBancariaOriginal = $.extend({}, parametros.CuentaBancaria);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_CUENTA_BANCARIA'));
				mcb.CuentaBancaria.EsPac = parametros.CuentaBancaria.EsPac == "S" ? true : false;
			} else {
				const valoresDefecto = mcb.asignarValoresPorDefecto()
				Object.assign(mcb.CuentaBancariaOriginal, valoresDefecto);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_CUENTA_BANCARIA'));
			}
		}
		
		mcb.cmbbxTipoBanco = {
			filtros: {
				IdEstadoTipoBanco: { "field": "IdEstadoTipoBanco", "operator": "eq", "value": 'ACT' }
			}
		};

		mcb.cmbbxCuentaBancaria = {
			filtros: {
				IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" },
				IdTipoBanco: { "field": "IdTipoBanco", "operator": "eq", "value": '' },
			}
		};

		if ($scope.SesionLocale == 'PE') {
			mcb.cmbbxCuentaBancariaFiltrarCuentas = {
				filtros: {
					IdTipoCuentaBancaria: { "field": "IdTipoCuentaBancaria", "operator": "neq", "value": 'CUVI' },
				}
			};
		}

		mcb.cmbbxPlaza = {
			filtros: {
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' },
				IdPais: { "field": "IdPais", "operator": "eq", "value": 0 },
			}
		};

		mcb.cmbbxCotitular = {
			filtros: {
				IdEmpresa: { "field": "IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa },
				IdCliente: { "field": "IdPersonaOrigen", "operator": "eq", "value": mcb.IdPersona },
			}
		};

		mcb.aceptar = function() {
			
			bcsComunes.ventanaOverlay(idVentana, true);
			if (mcb.CuentaBancaria.IdCuentaBancaria) {
				const datos = datosCuentaBancaria();
				servicioSociodeNegocio.modificarBorradorCuentaBancaria(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana, false);
						if (result.P_OUT_ERRORESCONTADOR > 0) {
							bcsComunes.msg("error", "error", "Error al modificar cuenta bancaria");
							return;
						}

						bcsComunes.msg("info", "info", "Cuenta bancaria modificada");

						var idMensaje = bcsComunes.ventanaMsg({
							mensaje: "Cuenta bancaria modificada",
							conflictos: util.parseServerResponse(result)
						}, idVentana);


						bcsComunes.ventanaPromesa(idMensaje).then(function() {
							bcsComunes.ventanaData(idVentana, true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(result) {
						bcsComunes.msg("error", "error", "Error al modificar cuenta bancaria");
						bcsComunes.ventanaData(idVentana, true);
						bcsComunes.ventanaOverlay(idVentana, false);
					}
				);
			} else {
				procesarCreacionCuentaBancaria();
			}
		};
		
		const procesarCreacionCuentaBancaria = function() {
			
			const cuentaBancaria = datosCuentaBancaria();
			const cuentasBancariasConMismaMoneda = filtrarCuentasBancariasConMismaMoneda();
			const ExisteCuentPredeterminada = ExisteCuentaBancariaPredeterminada();

			if (ExisteCuentPredeterminada.length ===0) {

				cuentaBancaria.p_CuentasBancarias[0].Predeterminado = 'S';
			}	
			if(cuentasBancariasConMismaMoneda.length === 0){
				crearCuentaBancariaConPreferenciaPago(cuentaBancaria);
			}else{
				crearCuentaBancaria(cuentaBancaria);
			}
		}
		
		const filtrarCuentasBancariasConMismaMoneda = function() {
		
			let cuentasBancarias = [];
			if(mcb.IdEnrolamiento){
				cuentasBancarias = mcb.CuentasBancarias.filter(function(item){
					return item.IdMoneda === mcb.CuentaBancaria.IdMoneda
				});
			}	
			return cuentasBancarias;
		}
		const ExisteCuentaBancariaPredeterminada = function() {

			let cuentaPredeterminada =[];
			for (let index = 0; index < mcb.CuentasBancarias.length; index++) {
				if (mcb.CuentasBancarias[index].Predeterminado=="UI_SI") {
					cuentaPredeterminada = mcb.CuentasBancarias[index];		
				}	
			}
			return cuentaPredeterminada;

		}
		
		const crearCuentaBancariaConPreferenciaPago = function(cuentaBancaria) {
			
			const creaPreferenciaAutomatica = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_CREAR_PREFERENCIA_AUTOMATICA'),idVentana);
			creaPreferenciaAutomatica.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
					cuentaBancaria.p_CuentasBancarias[0].CrearPreferenciaPago = 'S';
					crearCuentaBancaria(cuentaBancaria);
				}else{
					crearCuentaBancaria(cuentaBancaria);
				}
			});
		}
		
		const crearCuentaBancaria = function(cuentaBancaria) {
		
			bcsComunes.ventanaOverlay(idVentana,true);
			
			servicioSociodeNegocio.crearBorradorCuentaBancaria(cuentaBancaria).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					if (result.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.msg("error", "error", "Error al agregar cuenta bancaria");
						return;
					}
					const idMensaje = bcsComunes.ventanaMsg({
						mensaje: "Cuenta bancaria agregada",
						conflictos: util.parseServerResponse(result)
					}, idVentana);

					bcsComunes.ventanaPromesa(idMensaje).then(function() {
						bcsComunes.ventanaData(idVentana, true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);	
					});
				},
				function(result) {
					bcsComunes.msg("error", "error", "Error al agregar cuenta bancaria");
					bcsComunes.ventanaData(idVentana, false);
					bcsComunes.ventanaOverlay(idVentana, false);
				}
			);
		}
		
		const datosCuentaBancaria = function() {
		
			const EsPac = (mcb.CuentaBancaria.EsPac) ? 'S' : 'N';
			const codigoCuentaInterbancario = getCodigoCuentaInterbancario();
			const codigoAba = getCodigoAba();
			const otroCodigoBancoExtranjero = getOtroCodigoBancoExtranjero();
			const codigoIban = getCodigoIban();
			const otroCodigoCuentaExtranjero = getOtroCodigoCuentaExtranjero();

			const datos = {
				p_CuentasBancarias: [{
					IdInterno: 1,
					IdCuentaBancaria: mcb.CuentaBancaria.IdCuentaBancaria || 0,
					IdTipoBanco: mcb.cmbbxTipoBanco.idActual || '',
					IdBanco: mcb.CuentaBancaria.IdBanco || 0,
					IdTipoCuentaBancaria: mcb.CuentaBancaria.IdTipoCuentaBancaria || '',
					IdMoneda: mcb.CuentaBancaria.IdMoneda || '',
					NumeroCuentaBancaria: mcb.CuentaBancaria.NumeroCuentaBancaria || '',
					IdEnrolamiento: mcb.IdEnrolamiento,
					IdPlaza: mcb.CuentaBancaria.IdPlaza || 0,
					IdTipoIdentificadorCuenta: mcb.CuentaBancaria.IdTipoIdentificadorCuenta || '',
					IdentificadorCuenta: mcb.CuentaBancaria.IdentificadorCuenta || '',
					IdPersonaRelacionada: mcb.CuentaBancaria.IdPersonaRelacionada || 0,
					CodigoCuentaInterbancario: codigoCuentaInterbancario || '',
					CodigoAba: codigoAba || '',
					OtroCodigoBancoExtranjero: otroCodigoBancoExtranjero || '',
					CodigoIban: codigoIban || '',
					OtroCodigoCuentaExtranjero: otroCodigoCuentaExtranjero || '',
					CrearPreferenciaPago : 'N',
					EsPac: EsPac,
					Predeterminado:'N'
				}
				],
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'SOC',
				p_UsaReglasNegocio: 'S',
				p_CodigoFormulario: mcb.CodigoFormulario,
				p_IdTipoCanal: 'OPT'
			}
		
			return datos;
		}
		
		const getCodigoCuentaInterbancario = function() {
			if(esBancoNacional() && $scope.SesionLocale == 'PE'){
				return mcb.CuentaBancaria.CodigoCuentaInterbancario;
			} 
			return '';	
		}
		
		const getCodigoAba = function() {
			if(esBancoNacional()) return '';
			return mcb.CuentaBancaria.CodigoAba;
		}
		
		const getOtroCodigoBancoExtranjero = function() {
			if(esBancoNacional()) return '';
			return mcb.CuentaBancaria.OtroCodigoBancoExtranjero;
		}
		
		const getCodigoIban = function() {
			if(esBancoNacional()) return '';
			return mcb.CuentaBancaria.CodigoIban;
		}
		
		const getOtroCodigoCuentaExtranjero = function() {
			if(esBancoNacional()) return '';
			return mcb.CuentaBancaria.OtroCodigoCuentaExtranjero;
		}
		
		const esBancoNacional = function () {
			return mcb.cmbbxCuentaBancaria.seleccionado.IdTipoBanco == 'NAC';
		}
		
		mcb.asignarValoresPorDefecto = function (){
			const valoresPorDefecto = {};
			$.each(mcb.camposRequeridos ,function(key,value){

				const noEsRequerido = value.Requerido === 'N';
				const esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				const esCampoEnBlanco = !mcb.CuentaBancaria[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					valoresPorDefecto[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
				
			});
			return valoresPorDefecto
		}

		mcb.cancelar = function() {
			bcsComunes.ventanaData(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.$watch('mcb.CuentaBancaria.NumeroCuentaBancaria', function(nval, oval) {
			mcb.CuentaBancariaValida = region.ValidaFormatoCuentaBancaria(nval);
		});

	});
}());