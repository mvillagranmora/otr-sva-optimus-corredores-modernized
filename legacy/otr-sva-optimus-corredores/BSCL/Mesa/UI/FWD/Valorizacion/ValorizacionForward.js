(function() {
	angular.module('ModuloMesa').controller('ValorizacionForward',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.data.LimiteFechaProceso = {};
		$scope.data.LimiteFechaProceso.min = new Date(util.dateInicioDia($scope.SesionFechaSistema));
		$scope.data.kngFechaProceso = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		
		$scope.ValorizacionForward = function(){
			var datosValorizacion = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdPais			: $scope.SesionIdPaisSistema,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdModulo			: 'MFW',
				p_UsaReglasNegocio 	: 'N',
				p_FechaProceso 		: util.dateSPdate($scope.data.kngFechaProceso),
				p_IdTipoCanalIngreso: 'OPT'
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.ValorizacionForward(datosValorizacion).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.msg("info","Informacion","Proceso de Valorización de Forward realizado correctamente.");
				}
				else{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_PROCESO_VALORIZACION_FORWARD"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
			    }
				bcsComunes.ventanaOverlay(idVentana,false);   			
	   		},function(respuesta){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error","Error","Existen problemas al realizar el proceso de Valorización de Forward.");
	   		});
		};
		
	});
}());
