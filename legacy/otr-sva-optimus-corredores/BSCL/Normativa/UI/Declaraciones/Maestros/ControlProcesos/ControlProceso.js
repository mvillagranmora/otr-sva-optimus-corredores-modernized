angular.module('ModuloNormativa').controller('BlotterControlProcesosCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,$timeout){
	
	$scope.filtros = {};
	$scope.datos = {};
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaControlProceso();
	
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarControlProceso = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	$scope.datos.oConfiguracionEsquema = {
		filtros:{
			Modulo : { "field"   : "Modulo","operator": "eq", "value"   :  "TRI"}
		}	
	}
	
	$scope.datos.oConfiguracionVersion = {
		filtros:{
			Esquema : {"field" : "Esquema","operator":"eq","value" : undefined}
		}
	}
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.esquema = undefined;
		$scope.filtros.version = undefined;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.limpiarFiltros();
	
	
	
})