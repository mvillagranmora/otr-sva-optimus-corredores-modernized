angular.module('ModuloFFM').controller('MantenedorAdministracionLiquidezCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	var conf = bcsConfiguracionFFM.getFondosAdministracionLiquidez();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
				
	$scope.modificarInstrumentoAdministracionLiquidezGenerico = function(datos)
	{
		if(datos && datos.length > 0){
			var id = bcsComunes.ventanaAbrir("ModificarConfiguracionAdministracionLiquidezGenericoFondoMutuo", {ConfiguracionAdministracionLiquidez: datos[0]} );
			var promesa=bcsComunes.ventanaPromesa(id);                               
	        $.when(promesa).then(function(data){  
	        	conf.options.dataSource.read();
	        	$scope.datos.kNgModel = undefined;
	        });
		}
	};
		
	$scope.verLogAdministracionLiquidez = function () {
		bcsComunes.ventanaAbrir("LogAdministracionLiquidacionFondoMutuo", {} );
  	};
  	
	$scope.consultaAdministracionLiquidezGenerico = function(datos)
	{
		if(datos && datos.length > 0){
			var id = bcsComunes.ventanaAbrir("ConsultaAdministracionLiquidez", {ConfiguracionAdministracionLiquidez: datos[0]} );
			var promesa=bcsComunes.ventanaPromesa(id);                               
	        $.when(promesa).then(function(data){  
	        	conf.options.dataSource.read();
	        	$scope.datos.kNgModel = undefined;
	        }); 
		}
		
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
});