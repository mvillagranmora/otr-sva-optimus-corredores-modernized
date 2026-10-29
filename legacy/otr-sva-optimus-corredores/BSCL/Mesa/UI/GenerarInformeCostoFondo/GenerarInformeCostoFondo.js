angular.module('ModuloNormativa').controller('GenerarInformeCostoFondoCtrl',function($scope,$translate,bcsComunes,bcsServiciosMesa,$timeout){
	
	var controllerName = 'GenerarInformeCostoFondoCtrl';
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.datos = {};
	$scope.filtros = {};
	$scope.defecto = {};	
	
		
	$scope.fecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	
	$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	
	
	$scope.generarInformeCostoFondo = function(){
		
		let fechaDesde = moment($scope.filtros.fechaDesde).format().split('T')[0] + ' 00:00:00';
		let fechaHasta = moment($scope.filtros.fechaHasta).format().split('T')[0] + ' 23:59:59';
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'MESA',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'INFORME_COSTO_FONDO', 
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
					Valor : 'MESA'
				},{
					IdInterno : 0,
					NombreParametro : 'FechaDesde',
					Valor : fechaDesde
				},{
					IdInterno : 0,
					NombreParametro : 'FechaHasta',
					Valor : fechaHasta
				}]
		}
		
		bcsServiciosMesa.GenerarProcesoBatch(datos).$promise.then(
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