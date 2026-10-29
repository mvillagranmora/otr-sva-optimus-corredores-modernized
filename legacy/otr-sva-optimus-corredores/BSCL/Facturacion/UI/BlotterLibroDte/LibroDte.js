angular.module('ModuloFacturacion').controller('LibroDteCtrl',
		function($scope,$translate,$modal,
				bcsComunes,bcsServiciosFacturacion, bcsServicioPlataforma, bcsLibroDteGrid)
{
	$scope.data	= {};

	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	$scope.data.oPeriodoContable = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	//Seteo de variables de sistema
	/*$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;	
	$scope.data.btnActivos  = true; */  //Activa botones
	$scope.data.monthSelectorOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
	
	//GRILLA
	$scope.confBasicaLibroDte = { 
		dataSource			: bcsLibroDteGrid.dataSourceLibroDte,
		columns				: bcsLibroDteGrid.columnasLibroDte(),
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		height				: 450,
		selectable 			: "multiple row"		
	};
	
	
	//BOTONES
	$scope.buscar = function(){
		var filters = new Array();

		if ($scope.data.oPeriodoContable)
			auxDatePerCont =  util.dateOdata(util.date($scope.data.oPeriodoContable).hours(0).minutes(0).seconds(0).toDate());
		    primerDia = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth(), 1);
		    ultimoDia = new Date(auxDatePerCont.getFullYear(), auxDatePerCont.getMonth() + 1, 0);

		    filters.push( { field: "PeriodoContable", operator: "gte", value : primerDia });
		    filters.push( { field: "PeriodoContable", operator: "lte", value : ultimoDia });
		
 	    bcsLibroDteGrid.dataSourceLibroDte.filters(filters);
	};
	
		
	$scope.limpiarFiltros = function()
	{	
		//$scope.data.portafolio = false;
		$scope.data.oPeriodoContable = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
		bcsLibroDteGrid.dataSourceLibroDte.removeAll();	
	};	
	
	$scope.data.seleccionarTodosLibros = function() {	
		$scope.gridApiLibroDte.select($scope.gridApiLibroDte.tbody.find(">tr"));		
	};
	
	
	$scope.data.deseleccionarTodosLibros = function() {
		$scope.gridApiLibroDte.clearSelection();
	};


});