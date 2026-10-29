angular.module('ModuloNormativa').controller('GenerarInformeEstadisticasCtrl',function($scope,$translate,bcsComunes,bcsConsultasOrdenes,bcsServicioOrden,$timeout){
	
	var controllerName = 'GenerarInformeEstadisticasCtrl';
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.datos = {};
	$scope.filtros = {};
	$scope.defecto = {};
	
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	
	
	$scope.fecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	
	$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	
	
	$scope.generarInformeEstadistica = function(){
		
		let fechaDesde = util.dateSPdate($scope.filtros.fechaDesde);
		let fechaHasta = util.dateSPdate($scope.filtros.fechaHasta);
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'INFORME_ESTADISTICA', 
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'ORD'
				},{
					IdInterno : 0,
					NombreParametro : 'FechaOperacionOrdenI',
					Valor : fechaDesde
				},{
					IdInterno : 0,
					NombreParametro : 'FechaOperacionOrdenF',
					Valor : fechaHasta
				}]
		}
		
		bcsServicioOrden.InscribirProcesoBatch(datos).$promise.then(
		function(result) {
			if(result.P_OUT_ERRORESCONTADOR>0){
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}else{
				bcsComunes.ventanaOverlay($scope.winID,false);
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}
		},
		function(result){
			
		});
	};
	
	
})