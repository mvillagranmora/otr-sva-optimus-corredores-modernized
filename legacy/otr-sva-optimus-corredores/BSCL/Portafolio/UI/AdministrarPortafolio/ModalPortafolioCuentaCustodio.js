/*************************************************************
 * Purpose	: Modal Cuentas Custodio
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('ModalPortafolioCuentaCustodioCtrl',
		function ($scope,bcsComunes,servicioPortafolio,bcsServicioPlataforma) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var modPortCcust 			= this;
			modPortCcust.idVentana 		= bcsComunes.ventanaWinId($scope);
			modPortCcust.parametros 	= bcsComunes.ventanaParams(modPortCcust.idVentana);
			modPortCcust.datosAntes		= {};
			modPortCcust.datosDespues	= {};
			modPortCcust.cmbbx			= {};
			modPortCcust.respuesta 		= {};
			modPortCcust.filtros		= {};
			modPortCcust.modoEditar 	= false;
			
			modPortCcust.camposRequeridos = {};
			modPortCcust.CodigoFormulario = modPortCcust.parametros.CodigoFormulario;
			
			bcsServicioPlataforma.CamposRequeridosFormulario(modPortCcust.CodigoFormulario, function(respuesta){
				modPortCcust.camposRequeridos = respuesta;
			});
		
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			modPortCcust.cargaDatosPantalla = function(){
				//EDITAR
				if(modPortCcust.parametros && modPortCcust.parametros.CuentaCustodioObj){
					modPortCcust.modoEditar 			= true;
					modPortCcust.datosAntes				= _.clone(modPortCcust.parametros.CuentaCustodioObj);
					modPortCcust.datosAntes.IdEstado 	= (modPortCcust.parametros.CuentaCustodioObj.IdEstado==='ACT')?true:false;
					
					if ($scope.SesionLocale !== 'PE') {
						modPortCcust.datosAntes.LiquidaDividendos = (modPortCcust.parametros.CuentaCustodioObj.LiquidaDividendos==='S')?true:false;
						modPortCcust.datosAntes.LiquidaCupones = (modPortCcust.parametros.CuentaCustodioObj.LiquidaCupones==='S')?true:false;
						modPortCcust.datosAntes.LiquidaVencimientos = (modPortCcust.parametros.CuentaCustodioObj.LiquidaVencimientos==='S')?true:false;
						modPortCcust.datosAntes.LiquidaPrepagos = (modPortCcust.parametros.CuentaCustodioObj.LiquidaPrepagos==='S')?true:false;
					}
				}
				//CREAR
				else {
					modPortCcust.datosAntes.IdEstado = true;
					
					if ($scope.SesionLocale !== 'PE') {
					
						modPortCcust.datosAntes.LiquidaDividendos = false;
						modPortCcust.datosAntes.LiquidaCupones  = false;
						modPortCcust.datosAntes.LiquidaVencimientos  = false;
						modPortCcust.datosAntes.LiquidaPrepagos  = false;
					}
				}
				modPortCcust.datosDespues	= _.clone(modPortCcust.datosAntes);
			};		
	
			modPortCcust.cancelar = function () {
				modPortCcust.respuesta.aceptar = false;
				bcsComunes.ventanaData(modPortCcust.idVentana, modPortCcust.respuesta);
				bcsComunes.ventanaCerrar(modPortCcust.idVentana);
			};
			
			modPortCcust.preparaDatosGuardar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 		= "PLA"; 
				datosGuardar.p_UsaReglasNegocio = "S";
				if(modPortCcust.modoEditar){
					datosGuardar.p_IdCuentaCustodioPortafolio = modPortCcust.datosDespues.IdCuentaCustodioPortafolio;
					datosGuardar.p_RegistroCambio = [];
					if(modPortCcust.datosDespues.IdBoveda !== modPortCcust.datosAntes.IdBoveda)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdBoveda',ValorNuevo:modPortCcust.datosDespues.IdBoveda})
					if(modPortCcust.datosDespues.Codigo !== modPortCcust.datosAntes.Codigo)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'Codigo',ValorNuevo:modPortCcust.datosDespues.Codigo})
					if(modPortCcust.datosDespues.IdTipoCuentaCustodia !== modPortCcust.datosAntes.IdTipoCuentaCustodia)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoCuentaCustodia',ValorNuevo:modPortCcust.datosDespues.IdTipoCuentaCustodia})
					if(modPortCcust.datosDespues.IdEstado !== modPortCcust.datosAntes.IdEstado)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:(modPortCcust.datosDespues.IdEstado)?'ACT':'INA'})
					
					if ($scope.SesionLocale === 'PE') {
						if(modPortCcust.datosDespues.IdTipoCuentaCustodia !== modPortCcust.datosAntes.IdTipoCuentaCustodia)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoCuentaCustodia',ValorNuevo:modPortCcust.datosDespues.IdTipoCuentaCustodia})
						if(modPortCcust.datosDespues.IdTipoCustodia !== modPortCcust.datosAntes.IdTipoCustodia)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoCustodia',ValorNuevo:modPortCcust.datosDespues.IdTipoCustodia})
						if(modPortCcust.datosDespues.IdModeloCustodia !== modPortCcust.datosAntes.IdModeloCustodia)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdModeloCustodia',ValorNuevo:modPortCcust.datosDespues.IdModeloCustodia})
						if(modPortCcust.datosDespues.IdPersonaBancoCustodio !== modPortCcust.datosAntes.IdPersonaBancoCustodio)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdPersonaBancoCustodio',ValorNuevo:modPortCcust.datosDespues.IdPersonaBancoCustodio?modPortCcust.datosDespues.IdPersonaBancoCustodio:0})
						if(modPortCcust.datosDespues.IdPersonaBancoLiquidador !== modPortCcust.datosAntes.IdPersonaBancoLiquidador)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdPersonaBancoLiquidador',ValorNuevo:modPortCcust.datosDespues.IdPersonaBancoLiquidador?modPortCcust.datosDespues.IdPersonaBancoLiquidador:0})
					}
					if(modPortCcust.datosDespues.LiquidaDividendos !== modPortCcust.datosAntes.LiquidaDividendos)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'LiquidaDividendos',ValorNuevo:(modPortCcust.datosDespues.LiquidaDividendos)?'S':'N'})
					if(modPortCcust.datosDespues.LiquidaCupones !== modPortCcust.datosAntes.LiquidaCupones)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'LiquidaCupones',ValorNuevo:(modPortCcust.datosDespues.LiquidaCupones)?'S':'N'})
					if(modPortCcust.datosDespues.LiquidaVencimientos !== modPortCcust.datosAntes.LiquidaVencimientos)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'LiquidaVencimientos',ValorNuevo:(modPortCcust.datosDespues.LiquidaVencimientos)?'S':'N'})
					if(modPortCcust.datosDespues.LiquidaPrepagos !== modPortCcust.datosAntes.LiquidaPrepagos)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'LiquidaPrepagos',ValorNuevo:(modPortCcust.datosDespues.LiquidaPrepagos)?'S':'N'})
						
					if(datosGuardar.p_RegistroCambio.length===0) datosGuardar=undefined;
					

				}
				else {
				var cuentaCustodio = {
					IdInterno: 1,
					IdPortafolio: modPortCcust.parametros.IdPortafolio,
					IdBoveda: modPortCcust.datosDespues.IdBoveda || '',
					Codigo: modPortCcust.datosDespues.Codigo || '',
					IdEstado: (modPortCcust.datosDespues.IdEstado) ? 'ACT' : 'INA',
					IdTipoCuentaCustodia: modPortCcust.datosDespues.IdTipoCuentaCustodia || '',
					IdTipoCustodia: modPortCcust.datosDespues.IdTipoCustodia || '',
					IdModeloCustodia: modPortCcust.datosDespues.IdModeloCustodia || '',
					IdPersonaBancoCustodio: modPortCcust.datosDespues.IdPersonaBancoCustodio ? modPortCcust.datosDespues.IdPersonaBancoCustodio : 0,
					IdPersonaBancoLiquidador: modPortCcust.datosDespues.IdPersonaBancoLiquidador ? modPortCcust.datosDespues.IdPersonaBancoLiquidador : 0,
					LiquidaDividendos: modPortCcust.datosDespues.LiquidaDividendos ? 'S' : 'N',
					LiquidaCupones: modPortCcust.datosDespues.LiquidaCupones ? 'S' : 'N',
					LiquidaVencimientos: modPortCcust.datosDespues.LiquidaVencimientos ? 'S' : 'N',
					LiquidaPrepagos: modPortCcust.datosDespues.LiquidaPrepagos ? 'S' : 'N'
				};

				datosGuardar.p_CuentasCustodio = [cuentaCustodio];
				}
				return datosGuardar;
			};
			
			modPortCcust.aceptar = function () {
				
				modPortCcust.respuesta.aceptar = true;
				var datosGuardar = modPortCcust.preparaDatosGuardar();
				if(datosGuardar === undefined){
					bcsComunes.msg("warning","Cuenta Custodio",'No hay datos para modificar');
					modPortCcust.cancelar();
					return;
				} 
				bcsComunes.ventanaOverlay(modPortCcust.idVentana,true);
				if(modPortCcust.modoEditar) { //EDITAR
					servicioPortafolio.updateCuentaCustodio(datosGuardar,function (result){
						bcsComunes.ventanaOverlay(modPortCcust.idVentana,false);
						modPortCcust.resuelveResp(result);
					}, function(resError){
						bcsComunes.ventanaOverlay(modPortCcust.idVentana,false);
						modPortCcust.resuelveErrorResp(resError);
					});
				}
				else { // GUARDAR
					servicioPortafolio.saveCuentaCustodio(datosGuardar,function (result){
						bcsComunes.ventanaOverlay(modPortCcust.idVentana,false);
						modPortCcust.resuelveResp(result);
					}, function(resError){
						bcsComunes.ventanaOverlay(modPortCcust.idVentana,false);
						modPortCcust.resuelveErrorResp(resError);
					});
				}
			};
			
			modPortCcust.resuelveResp = function (result) {
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
			 * COMBOBOX
			 *********************************************/
			modPortCcust.cmbbx.boveda={
				filtros : {
					IdEstado : {field : "IdEstado", operator : "eq", value : 'VIG'}
				}
			};
			 
			modPortCcust.filtros.bancoCustodio = {
				IdEstado:{field:"IdEstado",operator:"eq",value:'ACT'}
			};
			
			modPortCcust.filtros.bancoLiquidador = {
				IdEstado:{field:"IdEstado",operator:"eq",value:'ACT'}
			};
			
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

			/*********************************************
			 * GO!!!
			 *********************************************/
			modPortCcust.cargaDatosPantalla();
			
	});
}());