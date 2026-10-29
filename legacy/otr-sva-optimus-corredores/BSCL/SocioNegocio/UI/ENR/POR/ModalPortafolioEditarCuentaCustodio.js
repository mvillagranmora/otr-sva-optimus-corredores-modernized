(function() {
	angular.module('ModuloSocioNegocio').controller('ModalPortafolioCuentaCustodioEnrolCtrl',
		function ($scope,bcsComunes,servicioSociodeNegocio,bcsServicioPlataforma) {
			const modPortCcust 			= this;
			modPortCcust.idVentana 		= bcsComunes.ventanaWinId($scope);
			modPortCcust.parametros 	= bcsComunes.ventanaParams(modPortCcust.idVentana);
			modPortCcust.datosAntes		= {};
			modPortCcust.datosDespues	= {};
			modPortCcust.respuesta 		= {};
			modPortCcust.camposRequeridos = {};
		
			modPortCcust.cargaDatosPantalla = function(){
				if(modPortCcust.parametros && modPortCcust.parametros.CuentaCustodioObj){
					modPortCcust.datosAntes				= _.clone(modPortCcust.parametros.CuentaCustodioObj);
					modPortCcust.datosAntes.IdEstado 	= (modPortCcust.parametros.CuentaCustodioObj.IdEstado==='ACT')?true:false;
					
					if ($scope.SesionLocale !== 'PE') {
						modPortCcust.datosAntes.LiquidaDividendos = (modPortCcust.parametros.CuentaCustodioObj.LiquidaDividendos==='S')?true:false;
						modPortCcust.datosAntes.LiquidaCupones = (modPortCcust.parametros.CuentaCustodioObj.LiquidaCupones==='S')?true:false;
						modPortCcust.datosAntes.LiquidaVencimientos = (modPortCcust.parametros.CuentaCustodioObj.LiquidaVencimientos==='S')?true:false;
						modPortCcust.datosAntes.LiquidaPrepagos = (modPortCcust.parametros.CuentaCustodioObj.LiquidaPrepagos==='S')?true:false;
					}
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
						IdCuentaCustodioPortafolio : modPortCcust.datosDespues.IdCuentaCustodioPortafolio,
	                	IdBoveda : modPortCcust.datosDespues.IdBoveda || '',
	                	Codigo : modPortCcust.datosDespues.Codigo || '',
						IdTipoCuentaCustodia: modPortCcust.datosDespues.IdTipoCuentaCustodia || '',
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
				
				modPortCcust.respuesta.aceptar = true;
				var datosGuardar = modPortCcust.preparaDatosGuardar();
				if(datosGuardar === undefined){
					bcsComunes.msg("warning","Cuenta Custodio",'No hay datos para modificar');
					modPortCcust.cancelar();
					return;
				} 
				bcsComunes.ventanaOverlay(modPortCcust.idVentana,true);
				servicioSociodeNegocio.modificarBorradorCuentaCustodioPortafolio(datosGuardar,function (result){
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
			
			modPortCcust.resuelveErrorResp = function resuelveErrorResp(resError) {
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