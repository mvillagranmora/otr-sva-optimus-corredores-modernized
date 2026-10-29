angular.module('ModuloFFM').controller('ResumenCartaLiquidacionCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	var filtros = [];
	var dsCartaLiquidacion = bcsConfiguracionFFM.getDataSourceCartaLiquidacion();
	$scope.winID=bcsComunes.ventanaWinId($scope);

	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,12);
    }	
	
	filtros.push({ field: "FolioCartaLiquidacion", operator: "eq", value: 17} );
	
	dsCartaLiquidacion.definition.filter = filtros;
	
	$scope.defaultOptions = {
		      		
      		selectable : "row",
      		filterable: false,	
	  		autoBind:true,
			height:450,
	  		dataSource: dsCartaLiquidacion,
		
			columns: [
			           bcsConfiguracionFFM.getConfiguracionColumna("MontoAporte")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("MontoRescate")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("FolioCartaLiquidacion")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("IdSerieFondo")
			          ,bcsConfiguracionFFM.getConfiguracionColumna("Emisor")
			]
	};
	
	$scope.ajustaVentana();
});