angular.module('ModuloPlataforma').controller('ConsultaConciliacionCtrl',
		function($scope,bcsComunes,$resource,$translate,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA){

	$scope.data = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	
	$scope.ejecutarConciliacion = function(){		
		bcsComunes.ventanaOverlay($scope.winID, true);

		var datosConciliacion = {};

		datosConciliacion.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		datosConciliacion.p_IdUsuario 			= $scope.SesionIdUsuario;
		datosConciliacion.p_IdModulo 			= 'PLA';

		bcsServicioPlataforma.Servicios().Conciliacion(datosConciliacion, function(res){
			    bcsComunes.ventanaOverlay($scope.winID, false);
				console.log(res);
				if (res.P_OUT_ERRORESCONTADOR === 0){
					let fecha = $scope.$root.SesionFechaSistema.replaceAll("-","");
					let hora = $scope.$root.SesionHoraSistema.replaceAll(":","");
					let nombreArchivo = fecha+hora+"_conciliacion.txt";
					
                    var file = new File([res.P_OUT_SALIDA], nombreArchivo);
				    saveAs(file);
				    bcsComunes.msg("info",$translate.instant("UI_DESCARGAR"),$translate.instant("UI_ARCHIVO_DESCARGA_EXITO"));
				}else{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
						conflictos:util.parseServerResponse(res)
					},$scope.winID);
				}
			},
			function(res){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					conflictos:util.parseServerResponse(res)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
		return;
	};	
});