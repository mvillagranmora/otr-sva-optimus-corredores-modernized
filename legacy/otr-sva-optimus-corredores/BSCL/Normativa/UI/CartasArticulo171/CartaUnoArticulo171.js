angular.module('ModuloNormativa').controller('ConsultarCartaUnoCtrl', 
		function($scope,$timeout,$modal,$translate,bcsComunes,bcsConsultarCartaUno)
{	
	
	//*** configuracion de objetos ***//
	
	$scope.filtro = {};
	$scope.data ={}; 
	var overlay = bcsComunes.overlay;
	var Accion = ''
		

   	//GRILLA PRINCIPAL 
	$scope.confBasicaCartaUno= {
		dataSource			: bcsConsultarCartaUno.dataSourceCartaUno(),
		columns				: bcsConsultarCartaUno.columnasCartaUno(),		
		sortable			: true,
		pageable			: true,
		height				: 580, 
		selectable 			: "row",
		filterable			: true	 
	};

	$scope.buscarCartaUno = function(){
		var filters = new Array();
		var auxDate1;
 
		
		//Filtro Fecha
		try{
			if ($scope.data.oCartaUno.fecha !== undefined && $scope.data.oCartaUno.fecha!== "" && $scope.data.oCartaUno.fecha !== null)
			{
			auxDate1 =  $scope.data.oCartaUno.fecha;
			//auxDate1 =   '01-01-2013'
			auxDate1 =  $scope.data.oCartaUno.fecha;
			filters.push({ field: "FechaVencimiento", operator: "gte", value: $scope.data.oCartaUno.fecha });
			
			
			//auxDate2 =  util.dateOdata(util.date($scope.data.oCircularConfiguracion.fechaHasta).hours(0).minutes(0).seconds(0).toDate());
    		//filters.push({ field: "FechaVencimiento", operator: "gte", value: auxDate2 });
			
			
			
			}	 
		}
		catch(e)
		{
		}
		
		
		$scope.gridApi.dataSource.filters(filters);		

	};
	 
	$scope.limpiarFiltros = function() {
		$scope.data.oCartaUno.fecha= "";
		$scope.gridApi.dataSource.removeAll();
		
	 
	
	}

	
	

});


