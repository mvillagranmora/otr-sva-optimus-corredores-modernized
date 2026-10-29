angular.module('ModuloNormativa').controller(
		'ReprocesarCartolasController',
		[ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils',
				'bcsCartolas', '$timeout', ReprocesarCartolasController ]);

function ReprocesarCartolasController($scope, $translate, bcsComunes, Model,
		Utils, bcsCartolas, $timeout) {

	var start = new Date();
	var windowParams = Utils.getWindowParams($scope);
	Utils.setWindowTitle($scope,  $translate.instant(windowParams.Title));
	$scope.Nomina = angular.copy(windowParams.NominaSeleccionada);
	$scope.Nomina.TipoProceso = windowParams.TipoProceso;
	$scope.Nomina.EstadoProceso = 'ING';
	$scope.Nomina.FechaProgramadaProceso = start;
	$scope.Nomina.IdEmpresa = $scope.SesionIdEmpresa;
	$scope.Nomina.IdUsuario = $scope.SesionIdUsuario;
	var idVentana = bcsComunes.ventanaWinId($scope);

	$scope.programar = function() {
		bcsComunes.ventanaOverlay(idVentana,true);
		var errors = [];

		if ($scope.Nomina.FechaProgramadaProceso === null
				|| typeof $scope.Nomina.FechaProgramadaProceso === 'undefined') {
			errors.push('Debe seleccionar fecha/hora programada de proceso');
		} else {
			if ($scope.Nomina.FechaProgramadaProceso.getTime() < start
					.getTime()) {
				errors.push('Fecha/hora de proceso debe ser futura');
			}
		}

		if (errors.length > 0) {
			Utils.showErrors(errors, windowParams.Title, bcsComunes
					.ventanaWinId($scope));
			return;
		}
		
		$scope.Nomina.AccionNomina	= "REPROCESAR";

		bcsCartolas.gestionarNomina($scope.Nomina, 
			function(reply) {
				var id = bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(reply)
					},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				if(id){
					var promesa=bcsComunes.ventanaPromesa(id);                               
					$.when(promesa).then(function(data){
				    	 $timeout(function() {
				    		 bcsComunes.ventanaOverlay(idVentana,false);
				    		 bcsComunes.ventanaCerrar(idVentana);
				    	 },20);
				    });
				}else{
					bcsComunes.ventanaOverlay(idVentana,false);
		    		bcsComunes.ventanaCerrar(idVentana);
				}
			}, function(error) {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_REPROCESAR_NOMINA"),
					conflictos:util.parseServerResponse(error)
				},$scope.winID);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
		);
	}
}
