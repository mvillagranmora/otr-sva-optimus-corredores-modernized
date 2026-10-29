(function() {
	angular.module('ModuloSocioNegocio').controller('ModalPortafolioCuentaCustodioEnrolCtrl',
		function ($scope,bcsComunes,servicioSociodeNegocio,bcsServicioPlataforma) {
			const modPortCcust 			= this;
			modPortCcust.idVentana 		= bcsComunes.ventanaWinId($scope);
			modPortCcust.parametros 	= bcsComunes.ventanaParams(modPortCcust.idVentana);
			modPortCcust.datosAntes		= {};
			modPortCcust.datosDespues	= {};
			modPortCcust.cmbbx			= {};
			modPortCcust.respuesta 		= {};
			modPortCcust.modoEditar 	= false;
			modPortCcust.camposRequeridos = {};
		
			modPortCcust.cargaDatosPantalla = function cargaDatosPantalla(){
                if ($scope.SesionLocale !== 'PE') {
						modPortCcust.datosAntes.LiquidaDividendos = false;
						modPortCcust.datosAntes.LiquidaCupones  = false;
						modPortCcust.datosAntes.LiquidaVencimientos  = false;
						modPortCcust.datosAntes.LiquidaPrepagos  = false;
					}
				modPortCcust.datosDespues	= _.clone(modPortCcust.datosAntes);
			};		
	
			modPortCcust.cancelar = function cancelar() {
				modPortCcust.respuesta.aceptar = false;
				bcsComunes.ventanaData(modPortCcust.idVentana, modPortCcust.respuesta);
				bcsComunes.ventanaCerrar(modPortCcust.idVentana);
			};
			
			bcsServicioPlataforma.CamposRequeridosFormulario(modPortCcust.parametros.CodigoFormulario, function(respuesta) {
                modPortCcust.camposRequeridos = respuesta;
            });
			
			modPortCcust.preparaDatosGuardar = function(){
				var datosGuardar = {
					p_IdEmpresa : $scope.$root.SesionIdEmpresa, 
					p_IdUsuario : $scope.$root.SesionIdUsuario,
					p_CodigoFormulario : modPortCcust.parametros.CodigoFormulario,
                    p_IdTipoCanal : 'OPT',
					p_IdModulo : "PLA",
					p_UsaReglasNegocio : "S",
					p_CuentasCustodioPortafolio : [{
						IdEnrolamiento : modPortCcust.parametros.IdEnrolamiento,
	                	IdBoveda : modPortCcust.datosDespues.IdBoveda || '',
	                	Codigo : modPortCcust.datosDespues.Codigo || '',
						IdTipoCuentaCustodia: modPortCcust.datosDespues.IdTipoCuentaCustodia,
						LiquidaDividendos :modPortCcust.datosDespues.LiquidaDividendos ? 'S' : 'N',
						LiquidaCupones : modPortCcust.datosDespues.LiquidaCupones ? 'S' : 'N',
						LiquidaVencimientos : modPortCcust.datosDespues.LiquidaVencimientos ? 'S' : 'N',
						LiquidaPrepagos : modPortCcust.datosDespues.LiquidaPrepagos ? 'S' : 'N'
					}]
				};
				
                if ($scope.SesionLocale === 'PE') {
                    datosGuardar.p_CuentasCustodioPortafolio[0].IdTipoCuentaCustodia = modPortCcust.datosDespues.IdTipoCuentaCustodia || '';
                    datosGuardar.p_CuentasCustodioPortafolio[0].IdTipoCustodia = modPortCcust.datosDespues.IdTipoCustodia || '';
                    datosGuardar.p_CuentasCustodioPortafolio[0].IdModeloCustodia = modPortCcust.datosDespues.IdModeloCustodia || '';
                    datosGuardar.p_CuentasCustodioPortafolio[0].IdPersonaBancoCustodio = modPortCcust.datosDespues.IdPersonaBancoCustodio ? modPortCcust.datosDespues.IdPersonaBancoCustodio : 0;
                    datosGuardar.p_CuentasCustodioPortafolio[0].IdPersonaBancoLiquidador = modPortCcust.datosDespues.IdPersonaBancoLiquidador ? modPortCcust.datosDespues.IdPersonaBancoLiquidador : 0;						
				}
				
				return datosGuardar;
			};
			
			modPortCcust.aceptar = function aceptar() {
				var datosGuardar = modPortCcust.preparaDatosGuardar();

                console.log(datosGuardar);
				bcsComunes.ventanaOverlay(modPortCcust.idVentana,true);
                servicioSociodeNegocio.crearBorradorCuentaCustodioPortafolio(datosGuardar,function (result){
                    bcsComunes.ventanaOverlay(modPortCcust.idVentana,false);
                    modPortCcust.resuelveResp(result);
					}, function(resError){
						bcsComunes.ventanaOverlay(modPortCcust.idVentana,false);
						modPortCcust.resuelveErrorResp(resError);
					});
			};
			
			modPortCcust.resuelveResp = function resuelveResp(result) {
				var msj = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
					msj = "Cuenta Custodio: La operación tiene advertencias";
				}
				else if (result.P_OUT_ERRORESCONTADOR > 0){
					msj = "Cuenta Custodio: La operación tiene errores";
				}
				else {
					if (result.P_OUT_REFID && result.P_OUT_REFID > 0){
						msj = "Cuenta Custodio: Guardar OK"; 
					}
				}
				var prom = bcsComunes.ventanaMsg({
					mensaje: msj,
					conflictos:util.parseServerResponse(result)
				},modPortCcust.idVentana);
				
				bcsComunes.ventanaPromesa(prom).then(function(){
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.ventanaData(modPortCcust.idVentana, modPortCcust.respuesta);
						bcsComunes.ventanaCerrar(modPortCcust.idVentana);
					}
				})
			}
			
			modPortCcust.resuelveErrorResp = function (resError) {
				bcsComunes.ventanaMsg({
					mensaje: "Cuenta Custodio: Error",
					conflictos:util.parseServerResponse(resError)
				},modPortCcust.idVentana);
			}
			
			/*********************************************
			 * WATCH
			 *********************************************/
			$scope.$watch('modPortCcust.datosDespues.IdTipoCustodia',function(n,o) {
				if(n===o)return;
				if(n){
					var idbc = modPortCcust.tipoCustodiaSel.IdPersonaBancoCustodioDefault;
					var idbl = modPortCcust.tipoCustodiaSel.IdPersonaBancoLiquidadorDefault;
					modPortCcust.datosDespues.IdPersonaBancoCustodio=(idbc>0)?idbc:undefined;
					modPortCcust.datosDespues.IdPersonaBancoLiquidador=(idbl>0)?idbl:undefined;
					modPortCcust.activaBancoCustodio=(idbc>0)?false:true;
					modPortCcust.activaBancoLiquidador=(idbl>0)?false:true;
				}else{
					modPortCcust.datosDespues.IdPersonaBancoCustodio=undefined;
					modPortCcust.datosDespues.IdPersonaBancoLiquidador=undefined;
					modPortCcust.activaBancoCustodio=true;
					modPortCcust.activaBancoLiquidador=true;
				}
			});

			modPortCcust.cargaDatosPantalla();
			
	});
}());