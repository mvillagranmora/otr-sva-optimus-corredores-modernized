angular.module('ModuloFFM').controller('HistorialCambiosModificarFechasCTRL',function($scope,bcsComunes,bcsConfiguracionFFM){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	
	var conf = bcsConfiguracionFFM.getHistorialCambiosModificarFechas();
	$scope.defaultOptions = conf.options;
	$scope.defaultOptions.dataSource.filter({ });
	$scope.apiGridHistorialCambiosModificarFechas = conf.buscar;	
	$scope.winID=bcsComunes.ventanaWinId($scope);

	
	
});