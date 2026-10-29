angular.module('ModuloPlataforma').controller('AbrirModuloMesaCtrl',function($scope,$translate,bcsComunes,bcsAlerta,bcsConfiguracionPauta,bcsServicioPlataforma,OPCIONES_BCSALERTA,$timeout){
	
	$scope.datosModulo = {};
	$scope.datosProceso = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.datosProceso.FechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	$scope.abrirModulo = function() {	
		var datos = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
			    p_IdUsuario 		: $scope.SesionIdUsuario,
			    p_IdModulo       	: 'PLA',
				p_MesaAbierta		: 'S',
				p_Modulo : [ {
					CodigoModulo 			: parametros.CodigoModulo,
					Estado					: 'ABT',
					CodigoUsuario			: $scope.$root.SesionUsuario,
				}]
		};
		bcsComunes.ventanaOverlay($scope.winID,true);
		bcsServicioPlataforma.Servicios().AbrirModulo(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			if (result.P_OUT_ERRORESCONTADOR === 0) {
				var p = bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_REAPERTURA_MODULO_OK").replace("::NombreModulo",result.P_OUT_RESULTADO[0].NombreModulo),
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(p);   
				$.when(promesa).then(function(data){
					bcsComunes.ventanaCerrar($scope.winID);
		        }); 
			} else {
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaMsg({
						mensaje:"UI_ERROR_REAPERTURA_MODULO",
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
			}
		},function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_REAPERTURA_MODULO");
		});
	}
	

});