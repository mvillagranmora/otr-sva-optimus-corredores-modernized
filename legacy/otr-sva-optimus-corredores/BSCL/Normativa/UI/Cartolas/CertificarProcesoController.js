angular.module('ModuloNormativa').controller(
		'CertificarProcesoController',
		[ '$scope', '$translate', 'bcsComunes', 'Model', 'Utils',
				'bcsCartolas', '$timeout', CertificarProcesoController ]);

function CertificarProcesoController($scope, $translate, bcsComunes, Model,
		Utils, bcsCartolas, $timeout) {

	var windowParams = Utils.getWindowParams($scope);
	$scope.Nomina = angular.copy(windowParams.NominaSeleccionada);
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.certificar = function() {
		bcsComunes.ventanaOverlay(idVentana,true);
		$scope.Nomina.IdUsuario = $scope.SesionIdUsuario;
		$scope.Nomina.NotaCertificacion	= $scope.Nomina.NotaCertificacion;
		$scope.Nomina.AccionNomina	= "CERTIFICAR";
		
		bcsCartolas.gestionarNomina($scope.Nomina, function(reply) {
			var id = bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(reply)
				},idVentana);
			
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
				mensaje:$translate.instant("UI_ERROR_CERTIFICACION_NOMINA"),
				conflictos:util.parseServerResponse(error)
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		}
		);
	}

}
