/*************************************************************
 * Purpose	: Controlador crear/editar tipo criterio neteo
 * Author	: JGarces - BCS
 * Date		: 06/03/2018
 *************************************************************/

(function() {
	angular.module('ModuloPlataforma').controller('modalTipoCriterioNeteoCtrl',
		function($scope,$resource,$translate,bcsComunes,$timeout,bcsAlerta){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var modTCN = this;
		modTCN.datosAntes 	= {};
		modTCN.datosDespues= {};
		modTCN.win			= {}; 
		modTCN.winId 		= bcsComunes.ventanaWinId($scope);
		modTCN.param 		= bcsComunes.ventanaParams(modTCN.winId);

		modTCN.datosAntes = $.extend({},modTCN.param.dato);
		modTCN.datosDespues = $.extend({},modTCN.param.dato);
		
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		
		modTCN.win.guardar = function() {
			var r = $resource("", {}, {
				'crearTipoCriterio': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=100032"},
				'modificarTipoCriterio': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=100033"}
			});
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'PLA',
				
				p_NombreTipoCriterioNeteo : modTCN.datosDespues.NombreTipoCriterioNeteo,
				p_SecuenciaNeteo : modTCN.datosDespues.SecuenciaNeteo
			};
			if(modTCN.param.dato){ //editar
				datos.p_IdTipoCriterioNeteo=modTCN.datosDespues.IdTipoCriterioNeteo;
	
				bcsComunes.ventanaOverlay(modTCN.winId,true);
				r.modificarTipoCriterio(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(modTCN.winId,false);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_DATOS_GUARDADOS'),
							conflictos : util.parseServerResponse(result)}, modTCN.winId);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){	
							bcsComunes.ventanaCerrar(modTCN.winId);
						});
					},
					function(result){
						bcsComunes.ventanaOverlay(modTCN.winId,false);
						
						var p=bcsComunes.ventanaMsgErr($translate.instant('UI_OPERACION_TIENE_ERRORES'),$scope.ventanaId);
						bcsComunes.ventanaPromesa(p).then(function(){
							bcsComunes.ventanaCerrar(modTCN.winId);
						});
					}
				);	
			}else{ //crear
				bcsComunes.ventanaOverlay(modTCN.winId,true);
				r.crearTipoCriterio(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(modTCN.winId,false);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_DATOS_GUARDADOS'),
							conflictos : util.parseServerResponse(result)}, modTCN.winId);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){	
							bcsComunes.ventanaCerrar(modTCN.winId);
						});
					},
					function(result){
						bcsComunes.ventanaOverlay(modTCN.winId,false);
						
						var p=bcsComunes.ventanaMsgErr($translate.instant('UI_OPERACION_TIENE_ERRORES'),$scope.ventanaId);
						bcsComunes.ventanaPromesa(p).then(function(){
							bcsComunes.ventanaCerrar(modTCN.winId);
						});
					}
				);
			}
			/*if(modTCN.datosAntes)
			bcsComunes.ventanaOverlay(modTCN.winId,true);
			r.crearOCI(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(modTCN.winId,false);
				if (result.P_OUT_ERRORESCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_ERROR'),
						conflictos:util.parseServerResponse(result)},modTCN.winId);
				}else{
					bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OK'),
						conflictos:util.parseServerResponse(result)},modTCN.winId);
					modTCN.tablaCarteraInversion.dataSource.read();
					modTCN.inputCarteraInversion=undefined;
				}
			},function(error){
				bcsComunes.ventanaOverlay(modTCN.winId,false);
				bcsComunes.ventanaMsgErr($translate.instant('UI_ERROR_OPERACION'),modTCN.winId);
			});*/

		};
		
		modTCN.win.cancelar = function() {
			bcsComunes.ventanaCerrar(modTCN.winId);
		};
		
		
/*********************************************
 * DATASOURCES
 *********************************************/
		
/*********************************************
 * COMBOBOX
 *********************************************/
	
/*********************************************
 * GRILLAS
 *********************************************/
		
/*********************************************
 * FUNCIONES
 *********************************************/	

/*********************************************
 * FUNCIONES DE POPUP
 *********************************************/	
		
		
/*********************************************
 * FUNCIONES WATCH 
 *********************************************/	

/*********************************************
 * GOOOOO
 *********************************************/	
	
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------


