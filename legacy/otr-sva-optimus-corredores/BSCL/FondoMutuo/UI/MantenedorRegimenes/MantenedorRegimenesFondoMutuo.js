angular.module('ModuloFFM').controller('MantenedorRegimenesFFMMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.datos = {};
	var conf = bcsConfiguracionFFM.getConsultaRegimen();
	$scope.defaultOptions = conf.options;
	$scope.buscarRegimenesFM = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.confPropositoPortafolio = function(dataItem)
	  {
		 var filtrosConfiguracionPropositoPortafolio = [{field: "IdRegimenFondoMutuo", operator: "eq", value: dataItem.IdRegimenFondoMutuo}];
		 return {
			 dataSource		: bcsConfiguracionFFM.getDataSourceConfiguracionPropositoPortafolio(filtrosConfiguracionPropositoPortafolio),
			  columns: [
				  bcsConfiguracionFFM.getConfiguracionColumna("PropositoDestino")
		      ],
		      sortable			: true,
		      pageable			: true,
		      height			: 200,
		      userSettings      : true,
		      selectable 		: "multiple row",
		      autoBind          : true,
			  navigatable         : true,
			  allowCopy 			: true
		 };
	  };
	  
	  
	$scope.confRegimenDestino = function(dataItem)
	  {
    	  var filtrosConfiguracionRegimenFondoMutuo = [{field: "IdRegimenFondoMutuoOrigen", operator: "eq", value: dataItem.IdRegimenFondoMutuo}];
			 return {
				 dataSource		: bcsConfiguracionFFM.getDataSourceConfiguracionRegimenFondoMutuo(filtrosConfiguracionRegimenFondoMutuo),
				  columns: [
					  bcsConfiguracionFFM.getConfiguracionColumna("RegimenFondoMutuoDestino")
			      ],
			      sortable			: true,
			      pageable			: true,
			      height			: 200,
			      userSettings      : true,
			      selectable 		: "multiple row",
			      autoBind          : true,
				  navigatable       : true,
				  allowCopy 		: true
			 };
		};	
	$scope.modificarRegimenesFM = function (datos) {
		if(!datos || datos.length == 0) return;
		var id = bcsComunes.ventanaAbrir("ModificarRegimenFondoMutuo",datos[0],false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	conf.options.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        }); 
  	};
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdEstado = 0;
	};
		
	$scope.limpiarFiltros();
});