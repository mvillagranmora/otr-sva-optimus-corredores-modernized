angular.module('ModuloPlataforma').controller('RequisitosProcesoFuncionesCtrl',
		function($scope,bcsComunes,$translate,bcsCierreModulosGrid, $resource){
	
	var overlay = bcsComunes.overlay;
	var rpfn = this;
	
	//GRILLA MODULOS
	rpfn.confBasicaProcesoFunciones = {
		dataSource			: bcsCierreModulosGrid.dataSourceProcesosFunciones(),
		columns				: bcsCierreModulosGrid.columnasProcesosFunciones(),
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 350,
		selectable			: "row"
	};
	
	rpfn.buscar = function(fechaConsulta){
		var filters = new Array();
		if (fechaConsulta !== null)
			filters.push({ field: "FechaRetiro", operator: "eq", value: fechaConsulta});
		
		console.log("FILTROS: " + JSON.stringify(filters));
		//$scope.grillas.gridLogProcesos.dataSource.filters(filters);
		//$scope.grillas.gridModulos.dataSource.filters(filters);
		
	};
	
	$scope.$watch('rpfn.seleccionProcesoFunciones', function(newValue, oldValue){
		if (angular.equals(newValue, oldValue)) return;
		if (newValue !== false) {
			rpfn.nombre = rpfn.seleccionProcesoFunciones.NombreFuncionProceso;
			
			rpfn.abierto = (rpfn.seleccionProcesoFunciones.PermiteEjecucion === 'A' || rpfn.seleccionProcesoFunciones.PermiteEjecucion == 'T')?true:false;
			rpfn.cerrado = (rpfn.seleccionProcesoFunciones.PermiteEjecucion === 'C' || rpfn.seleccionProcesoFunciones.PermiteEjecucion == 'T')?true:false;
		}
	});
	
	$scope.$watch('rcrr.ProcesosBuscar', function(newValue, oldValue){
		if (angular.equals(newValue, oldValue)) return;
		if (newValue !== false) {
			var filters = new Array();
			if($scope.rcrr.oConfiguracionModulos.idActual !== null && $scope.rcrr.oConfiguracionModulos.idActual !== false){
				filters.push({field: "IdCREModuloEmpresa", operator: "eq", value: $scope.rcrr.oConfiguracionModulos.idActual});
				console.log("FILTROS: " +JSON.stringify(filters));
				rpfn.gridApiProcesoFunciones.dataSource.filters(filters);
			}
			$scope.rcrr.ProcesosBuscar = false;
		}
	});
		
	
});