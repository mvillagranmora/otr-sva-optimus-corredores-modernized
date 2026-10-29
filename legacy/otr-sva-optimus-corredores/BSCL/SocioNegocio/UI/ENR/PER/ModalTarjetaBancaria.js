(function() {
	angular.module('ModuloSocioNegocio').controller('ModalTarjetaBancariaEnrolCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
		var mtb = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mtb.IdEnrolamiento = parametros.IdEnrolamiento;
		mtb.CodigoFormulario = parametros.CodigoFormulario;
		mtb.TarjetaBancaria = $.extend({},parametros.TarjetaBancaria);
		mtb.kOptFechaVencimiento = {}; 
		mtb.camposRequeridos = {};
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mtb.CodigoFormulario, function(respuesta) {
			mtb.camposRequeridos = respuesta;
			mtb.asignarValoresPorDefecto();
			mtb.cargarDatos();
		});
		
		mtb.cargarDatos = function(){
			if (parametros.TarjetaBancaria){
				//modificacion
				mtb.TarjetaBancariaOriginal = $.extend({},parametros.TarjetaBancaria);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_TARJETA_BANCARIA'));
			}else{
				mtb.TarjetaBancariaOriginal = $.extend({},mtb.TarjetaBancaria);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_TARJETA_BANCARIA'));
				mtb.TarjetaBancariaOriginal.IdTipoTarjetaBancaria = 'CUCO';
				mtb.kOptFechaVencimiento.min = $scope.$root.SesionFechaSistema;
				mtb.TarjetaBancaria.FechaVencimiento = util.date($scope.$root.SesionFechaSistema).toDate();
				mtb.TarjetaBancariaOriginal.IdEstado = 'ACT';
			}
		}
		
		mtb.aceptar = function() {
			if(mtb.TarjetaBancaria.IdTipoTarjetaBancaria === 'TDEB')
			{
				mtb.TarjetaBancaria.CodigoVerificacion = 0;
				mtb.TarjetaBancaria.FechaVencimiento = '';
				if(mtb.TarjetaBancaria.NumeroTarjetaBancaria == undefined)
					mtb.TarjetaBancaria.NumeroTarjetaBancaria = '';
				else
					if(!mtb.validaFormatoNroTarjeta()) return;
			}
			else{
				if(!mtb.validaFormatoNroTarjeta()) return;
				if(!mtb.validaFormatoCodVerificacion()) return;
			}

			var datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoFormulario		: mtb.CodigoFormulario,
				p_IdTipoCanal			: 'OPT',
				p_TarjetasBancarias		: [{
					IdInterno : 1,
					IdTarjetaBancaria : mtb.TarjetaBancaria.IdTarjetaBancaria || 0,
					IdEnrolamiento : mtb.IdEnrolamiento,
					IdBanco : mtb.TarjetaBancaria.IdBanco || 0,
					IdTipoTarjetaBancaria : mtb.TarjetaBancaria.IdTipoTarjetaBancaria || '',
					NumeroTarjetaBancaria : mtb.TarjetaBancaria.NumeroTarjetaBancaria || '',
					FechaVencimiento : mtb.TarjetaBancaria.FechaVencimiento? util.dateSPdate(mtb.TarjetaBancaria.FechaVencimiento): '0001-01-01',
					CodigoVerificacion : mtb.TarjetaBancaria.CodigoVerificacion || 0
				}]	
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			//modificacion
			if (mtb.TarjetaBancaria.IdTarjetaBancaria){
				servicioSociodeNegocio.modificarBorradorTarjetaBancaria(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.msg("error","error","Error al modificar tarjeta bancaria");
							return;
						}
						
						bcsComunes.msg("info","info","Tarjeta bancaria modificada");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Tarjeta bancaria modificada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
	
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(){

						bcsComunes.msg("error","error","Error al modificar tarjeta bancaria");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
			}
			else {
				servicioSociodeNegocio.crearBorradorTarjetaBancaria(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.msg("error","error","Error al agregar tarjeta bancaria");
							return;
						}
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Tarjeta bancaria agregada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(){

						bcsComunes.msg("error","error","Error al agregar tarjeta bancaria");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
			}
		};
		
		mtb.asignarValoresPorDefecto = function (){
			if(parametros.TarjetaBancaria) return;
			$.each(mtb.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mtb.TarjetaBancaria[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mtb.TarjetaBancaria[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
		
		mtb.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		// Se obtienen placeholder
		mtb.placeholder = {};
		mtb.placeholder.numeroTarjeta		= '1111222233334444';
		mtb.placeholder.codigoVerificacion	= '111';
		
		// Valida formato numero tarjeta
		mtb.validaFormatoNroTarjeta = function(){
			if(mtb.TipoTarjetaBancoObj !== undefined && mtb.TipoTarjetaBancoObj && mtb.TarjetaBancaria.NumeroTarjetaBancaria !== undefined){
				if(region.ValidaFormatoNumeroTarjetaCredito('',16,mtb.TarjetaBancaria.NumeroTarjetaBancaria) == false){  // TODO revisar mejora para traer el IdTipoTarjetaBancaria y el LargoTarjeta 
					bcsComunes.msg("warning","Advertencia",$translate.instant("UI_FORMATO_NUMERO_TARJETA_INCORRECTO"));
					$scope.formTarjetaBancaria.$valid = undefined;
					return false;
				}
			}
	    	return true;
	    }
		
		// Valida formato codigo verificacion
		mtb.validaFormatoCodVerificacion = function(){
			if(mtb.TipoTarjetaBancoObj !== undefined && mtb.TipoTarjetaBancoObj && mtb.TarjetaBancaria.CodigoVerificacion !== undefined){
				if(mtb.TipoTarjetaBancoObj.IdTipoTarjetaBancaria === 'TDEB')  return true;
				if(region.ValidaFormatoCodigoVerificacion('',4,mtb.TarjetaBancaria.CodigoVerificacion) == false){  // TODO revisar mejora para traer el IdTipoTarjetaBancaria y el LargoCodigoVerificacion
					bcsComunes.msg("warning","Advertencia",$translate.instant("UI_FORMATO_CODIGO_VERIFICACION_INCORRECTO"));
					$scope.formTarjetaBancaria.$valid = undefined;
					return;
				}
			}
	    	return true;
	    }
	});
}());
//------------------------- FIN CONTROLLER --------------------------