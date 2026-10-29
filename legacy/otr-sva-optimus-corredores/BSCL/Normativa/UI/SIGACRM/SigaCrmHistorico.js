angular.module('ModuloCustodia').controller('SigaCrmHistoricoCtrl',

function SigaCrmHistorico ($scope, $resource, bcsComunes, bcsServiciosCustodia) {
//shechenleitner 10-12-2019 - Prueba de concepto consulta historica a SigaCRM
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.data = {};
	$scope.token = "";
	$scope.data.oClientePortafolioIdActual = 0;

	$scope.data.obtenerToken = function() {
      var params = {};
        params.p_idEmpresa         = $scope.SesionIdEmpresa;
        params.p_opcionMenu        = "showCertificadoTributario";
        params.p_canal            = "AGENTE";
        params.p_idPortafolio    = $scope.data.oClientePortafolio.idActual;
		
		
		bcsComunes.ventanaOverlay(idVentana,true);
		
		bcsServiciosCustodia.ConsultarSigaCrmHistorico(params,
			function(response){		
				//bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaOverlay(idVentana,false);
				if (response.P_OUT_ERRORESCONTADOR == 0){
					$scope.urlSigaCRM = response.P_OUT_SALIDA[0];
					console.log("URL: " + $scope.urlSigaCRM);	
					window.open($scope.urlSigaCRM, '_blank');
					bcsComunes.ventanaCerrar(idVentana);
				}
				else 
				{
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(response)
					},bcsComunes.ventanaWinId($scope));
					
				}
			},
			function(response){
				//bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(response)
				},bcsComunes.ventanaWinId($scope));
			}
		);
	};
	
	$scope.data.limpiar = function() {
	
		$scope.data.oClientePortafolio.seleccionado = false;
		$scope.data.oClientePortafolio.idActual = 0;
		$scope.data.oClientePortafolio.idDefecto = 0;
	}
	
	//$scope.dataVentana.obtenerToken();
}
);