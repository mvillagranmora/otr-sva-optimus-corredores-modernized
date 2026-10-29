angular.module('ModuloFFM').controller('MantenedorFamiliaCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	var conf = bcsConfiguracionFFM.getConsultaFamiliasFondosMutuos();
	$scope.defaultOptions = conf.options;
	$scope.buscarFamiliasFMM = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.ingresarFamiliaFMM = function()
	{
		$scope.formData.abrirVentanaFFMM({});
	};
	
	$scope.formData.abrirVentanaFFMM = function(params){
		var id = bcsComunes.ventanaAbrir("IngresarFamiliaFFM",params,false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	conf.options.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        }); 
	}
	
	$scope.modificarFamiliaFMM = function (items) {
		if(items && items.length > 0){
			$scope.formData.abrirVentanaFFMM({IdFamiliaFondo : items[0].IdFamiliaFondo});
		}
  	};
	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
});