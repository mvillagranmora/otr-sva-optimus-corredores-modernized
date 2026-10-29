angular.module('ModuloFFM').controller('ConfirmacionComisionesNNCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	
	var conf = bcsConfiguracionFFM.getConfirmacionComisionesNN();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);
	var overlay = bcsComunes.overlay;
	
	
	$scope.limpiarFiltros = function()
	{
        $scope.formData.setFondo = "-1";
        $scope.filtros.IdEstado = undefined;
	};
	
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		bcsComunes.ventanaOverlay($scope.winID,true);
		$scope.defaultOptions.dataSource.data(parametros.respuesta.P_OUT_RESULTADO);
		bcsComunes.ventanaOverlay($scope.winID,false);
		$scope.ajustaVentana();
	}
	
		
});