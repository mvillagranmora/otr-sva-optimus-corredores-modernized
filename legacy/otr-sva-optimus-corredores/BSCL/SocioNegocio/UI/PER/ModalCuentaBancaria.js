/*************************************************************
 * Purpose	: Modal Cuenta Bancaria.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalCuentaBancariaCtrl',function($scope,
			$translate,$timeout,servicioSociodeNegocio,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioPlataforma) {
		
		var mcb = this;
		
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		
		var overlay = bcsComunes.overlay;
		mcb.IdPersona = parametros.IdPersona;
		mcb.CodigoTipoPersona = parametros.CodigoTipoPersona;
		mcb.CuentaBancaria = $.extend({},parametros.CuentaBancaria);
		mcb.CuentaBancariaValida = false;
		mcb.IdCliente = parametros.IdCliente;
		mcb.CuentasBancarias = parametros.CuentasBancarias;
		mcb.CodigoFormulario = parametros.CodigoFormulario;
		mcb.camposRequeridos = {};

		bcsServicioPlataforma.CamposRequeridosFormulario(mcb.CodigoFormulario, function(respuesta) {
			mcb.camposRequeridos = respuesta;

			$timeout(function() {	
				mcb.cargarDatos();
			},1000);

			
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
				filtros : {
					IdEstadoTipoBanco    : { "field"   : "IdEstadoTipoBanco","operator": "eq", "value"   : 'ACT' },
				}
			};
		
		mcb.cmbbxCuentaBancaria = {
			filtros : {
				IdEmpresa   : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
                IdEstado    : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
                IdTipoBanco : { "field"   : "IdTipoBanco","operator": "eq", "value"   : '' }, 
			}
		};
		
		if($scope.SesionLocale=='PE') {
			mcb.cmbbxCuentaBancariaFiltrarCuentas = {
					filtros : {
						IdTipoCuentaBancaria : { "field"   : "IdTipoCuentaBancaria","operator": "neq", "value"   : 'CUVI' },
					}
			};
		}

		mcb.cmbbxPlaza = {
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' },                           
                    IdPais   : { "field"   : "IdPais",  "operator": "eq", "value"   : 0 },
				}
			};

		mcb.cmbbxCotitular = {
			filtros : {
				IdEmpresa   : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
                IdCliente    : { "field"   : "IdPersonaOrigen", "operator": "eq", "value"   : mcb.IdPersona },                          
			}
		};
		
		mcb.aceptar = function() {
			//modificacion
			if (mcb.CuentaBancaria.IdCuentaBancaria){
				var registroCambio = [];
				var EsPac = '';
				var campos = [
							{ nombre: "IdBanco" ,tipo : "combo"},
							{ nombre: "IdTipoCuentaBancaria" ,tipo : "combo"},
							{ nombre: "IdMoneda" ,tipo : "combo"},
							{ nombre: "NumeroCuentaBancaria" ,tipo : "texto"},
							{ nombre: "IdEstado" ,tipo : "combo"},
							{ nombre: "IdPlaza" ,tipo : "combo"},
							{ nombre: "CodigoAba" ,tipo : "texto"},
							{ nombre: "OtroCodigoBancoExtranjero" ,tipo : "texto"},
							{ nombre: "CodigoIban" ,tipo : "texto"},
							{ nombre: "OtroCodigoCuentaExtranjero" ,tipo : "texto"},
							{ nombre: "EsPac",tipo : "bool"}
							];
				
				 if($scope.SesionLocale=='PE'){
					if (mcb.CuentaBancaria["IdTipoCuentaBancariaX"] !== mcb.CuentaBancariaOriginal["IdTipoIdentificadorCuenta"])
						registroCambio.push({CampoModificado : "IdTipoIdentificadorCuenta", ValorNuevo : mcb.CuentaBancaria["IdTipoCuentaBancariaX"] || '' });
					if (mcb.CuentaBancaria["IdCuentaBancariaX"] !== mcb.CuentaBancariaOriginal["IdentificadorCuenta"])
						registroCambio.push({CampoModificado : "IdentificadorCuenta", ValorNuevo : mcb.CuentaBancaria["IdCuentaBancariaX"] || '' });
					if (mcb.CuentaBancaria["IdPersonaRelacionada"] !== mcb.CuentaBancariaOriginal["IdPersonaRelacionada"])
						registroCambio.push({CampoModificado : "IdPersonaRelacionada", ValorNuevo : mcb.CuentaBancaria["IdPersonaRelacionada"] || '' });
					if (mcb.CuentaBancaria["CodigoCuentaInterbancario"] !== mcb.CuentaBancariaOriginal["CodigoCuentaInterbancario"])
						registroCambio.push({CampoModificado : "CodigoCuentaInterbancario", ValorNuevo : mcb.CuentaBancaria["CodigoCuentaInterbancario"] || '' });
				}
				
				mcb.CuentaBancaria.CodigoCuentaInterbancario = getCodigoCuentaInterbancario() || '';
				mcb.CuentaBancaria.CodigoAba = getCodigoAba() || '';
				mcb.CuentaBancaria.OtroCodigoBancoExtranjero = getOtroCodigoBancoExtranjero() || '';
				mcb.CuentaBancaria.CodigoIban = getCodigoIban() || '';
				mcb.CuentaBancaria.OtroCodigoCuentaExtranjero = getOtroCodigoCuentaExtranjero() || '';
			
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mcb.CuentaBancaria[value.nombre] !== mcb.CuentaBancariaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mcb.CuentaBancaria[value.nombre]?mcb.CuentaBancaria[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mcb.CuentaBancaria[value.nombre] === false && mcb.CuentaBancariaOriginal[value.nombre] === null) && (mcb.CuentaBancaria[value.nombre] != mcb.CuentaBancariaOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mcb.CuentaBancaria[value.nombre]?mcb.CuentaBancaria[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mcb.CuentaBancaria[value.nombre] !== mcb.CuentaBancariaOriginal[value.nombre])
							//registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mcb.CuentaBancaria[value.nombre]?mxment(mcb.CuentaBancaria[value.nombre]).utc().format('YYYY-MM-DD') :"")});
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mcb.CuentaBancaria[value.nombre]?util.dateSPdate(mcb.CuentaBancaria[value.nombre]):"")});
					}
					if (value.tipo === 'bool') {
						if (mcb.CuentaBancaria[value.nombre] !== mcb.CuentaBancariaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mcb.CuentaBancaria[value.nombre]?"S":"N")});
					}			
				});

				if (registroCambio.length === 0)
				{
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				if(mcb.CuentaBancaria.EsPac==true){
					EsPac = 'S';
				}else{
					EsPac = 'N';
				}
				
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdPersona :mcb.IdPersona,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdCuentaBancaria: mcb.CuentaBancaria.IdCuentaBancaria,
					p_RegistroCambio: registroCambio,
					p_EsPac :EsPac
				};

				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaCuentaBancaria(datos).$promise.then(
					function(result) {

						var idMensaje = bcsComunes.ventanaMsg({ mensaje : result.P_OUT_ERRORESCONTADOR === 1 ? 'Cuenta bancaria no modificada' : "Cuenta bancaria modificada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
						
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al modificar cuenta bancaria");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
			}
			//creacion
			else {
				var bancos = undefined
				
				if(mcb.IdCliente && mcb.IdCliente > 0){
					bancos = mcb.CuentasBancarias.filter(function(item){
						return item.IdMoneda === mcb.CuentaBancaria.IdMoneda
					}).length
				}
				
					var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_CREAR_PREFERENCIA_AUTOMATICA'),idVentana);
					t.then(function(result){
						if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
							agregarCuenta('S');
						}else{
							agregarCuenta('N');
						}
					})
			}

		};
		
		var agregarCuenta = function(creaPreferencia){
			
			const EsPac = (mcb.CuentaBancaria.EsPac == true) ? 'S' : 'N';
			const codigoCuentaInterbancario = getCodigoCuentaInterbancario();
			const codigoAba = getCodigoAba();
			const otroCodigoBancoExtranjero = getOtroCodigoBancoExtranjero();
			const codigoIban = getCodigoIban();
			const otroCodigoCuentaExtranjero = getOtroCodigoCuentaExtranjero();
			
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_InCuentaBancaria: [{
						IdInterno: 1,
						IdBanco: mcb.CuentaBancaria.IdBanco || 0,
						IdTipoCuentaBancaria: mcb.CuentaBancaria.IdTipoCuentaBancaria || '',
						IdMoneda: mcb.CuentaBancaria.IdMoneda || '',
						NumeroCuentaBancaria: mcb.CuentaBancaria.NumeroCuentaBancaria || '',
						IdPersona: mcb.IdPersona,
						IdEstado: mcb.CuentaBancaria.IdEstado || 'ACT',
						IdPlaza: mcb.CuentaBancaria.IdPlaza || 0,
						IdTipoIdentificadorCuenta: mcb.CuentaBancaria.IdTipoCuentaBancariaX || '',
						IdentificadorCuenta: mcb.CuentaBancaria.IdCuentaBancariaX || 0,
						IdPersonaRelacionada: mcb.CuentaBancaria.IdPersonaRelacionada || 0,
						CodigoCuentaInterbancario: codigoCuentaInterbancario || '',
						CodigoAba: codigoAba || '',
						OtroCodigoBancoExtranjero: otroCodigoBancoExtranjero || '',
						CodigoIban: codigoIban || '',
						OtroCodigoCuentaExtranjero: otroCodigoCuentaExtranjero || '',
						CrearPreferenciaPago: creaPreferencia || 'N',
						IdCliente: mcb.IdCliente || 0,
						EsPac: EsPac
					}]
			};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaCuentaBancaria(datos).$promise.then(
					function(result) {			
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : result.P_OUT_ERRORESCONTADOR === 1 ? 'Cuenta bancaria no agregada' : "Cuenta bancaria agregada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
						
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al agregar cuenta bancaria");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
		}
		
		
		const getCodigoCuentaInterbancario = function() {
			if(esBancoNacional() && $scope.SesionLocale == 'PE'){
				return mcb.CuentaBancaria.CodigoCuentaInterbancario;
			} 
			return '';	
		}
		
		const getCodigoAba = function() {
			return mcb.CuentaBancaria.CodigoAba;
		}
		
		const getOtroCodigoBancoExtranjero = function() {
			return mcb.CuentaBancaria.OtroCodigoBancoExtranjero;
		}
		
		const getCodigoIban = function() {
			return mcb.CuentaBancaria.CodigoIban;
		}
		
		const getOtroCodigoCuentaExtranjero = function() {
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
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.$watchGroup(['mcb.CuentaBancaria.NumeroCuentaBancaria', 'mcb.camposRequeridos'],function(nval,oval){
			const numeroCuentaBancariaConfig = mcb.camposRequeridos['formCuentaBancaria_NumeroCuentaBancaria'];
			if (typeof mcb.cmbbxCuentaBancaria != "undefined"){
				if (typeof mcb.cmbbxCuentaBancaria.seleccionado != "undefined"){
					if (typeof mcb.cmbbxCuentaBancaria.seleccionado.IdTipoBanco != "undefined"){
					    if(mcb.cmbbxCuentaBancaria.seleccionado.IdTipoBanco.toUpperCase() === 'EXT'){
							mcb.CuentaBancariaValida = (numeroCuentaBancariaConfig && numeroCuentaBancariaConfig.Requerido !== 'S')|| region.ValidaFormatoCuentaBancariaEXT(nval[0]);
					    }else{
							mcb.CuentaBancariaValida = (numeroCuentaBancariaConfig && numeroCuentaBancariaConfig.Requerido !== 'S') || region.ValidaFormatoCuentaBancaria(nval[0]);
						}
				}
				}
			}
			
			
		});
		
	$scope.$watchGroup(['mcb.CuentaBancaria.OtroCodigoCuentaExtranjero','mcb.CuentaBancaria.NumeroCuentaBancaria',
		'mcb.CuentaBancaria.CodigoIban',
		'mcb.CuentaBancaria.OtroCodigoBancoExtranjero',
		 'mcb.CuentaBancaria.CodigoAba'],function(nval,oval){
			const numeroCuentaBancariaConfig = mcb.camposRequeridos['formCuentaBancaria_NumeroCuentaBancaria'];
			let validadornval = typeof nval != "undefined" ? true : false;
			
			if (mcb.CuentaBancaria.IdCuentaBancaria){
				if (typeof mcb.cmbbxCuentaBancaria != "undefined"){
					if (typeof mcb.cmbbxCuentaBancaria.seleccionado != "undefined"){
						if (typeof mcb.cmbbxCuentaBancaria.seleccionado.IdTipoBanco != "undefined"){
				  			if(mcb.cmbbxCuentaBancaria.seleccionado.IdTipoBanco.toUpperCase() === 'EXT'){
								mcb.CuentaBancariaValida = (numeroCuentaBancariaConfig && numeroCuentaBancariaConfig.Requerido !== 'S')|| region.ValidaFormatoCuentaBancariaEXT(mcb.CuentaBancaria.NumeroCuentaBancaria);
				    		}else{
								mcb.CuentaBancariaValida = (numeroCuentaBancariaConfig && numeroCuentaBancariaConfig.Requerido !== 'S') || region.ValidaFormatoCuentaBancaria(mcb.CuentaBancaria.NumeroCuentaBancaria);
							}
							
							if(mcb.CuentaBancaria.NumeroCuentaBancaria.trim() == ''){
								mcb.CuentaBancariaValida = false;
							}else{
								mcb.CuentaBancariaValida = (
									mcb.CuentaBancariaValida == true
									&& validadornval == true
								)? true :false;
							}			
							
							
						}
					}
				}  
			}
			
			
		});
		
		var firstHeightCrear = $('#Win_agregarModificarCuentaBancaria');
        firstHeightCrear.css('height', '300px');
	});
}());
//------------------------- FIN CONTROLLER --------------------------	