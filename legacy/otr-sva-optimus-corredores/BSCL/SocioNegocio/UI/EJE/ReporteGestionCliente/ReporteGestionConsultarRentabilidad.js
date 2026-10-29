(function() {
    angular.module('ModuloSocioNegocio').controller('ReporteRentabilidadController', 
    		function($scope, $translate, $resource, $timeout, $http, bcsConsultasMesa, 
    				bcsServiciosMesa, bcsComunes, bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA, bcsConsultarReporteRentabilidadGrid) {
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.seleccionReporteRentabilidad = {};
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		
		//COMBOBOX		
		$scope.data.oConfiguracionAgente={
				tipo : "combobox-ejecutivo",
				configuracion : {},
				filtros : { 	
					IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};		
					
		
		$scope.confBasicaReporteRentabilidad = { 
				dataSource			: bcsConsultarReporteRentabilidadGrid.dataSourceReporteRentabilidad,
				columns				: bcsConsultarReporteRentabilidadGrid.columnasReporteRentabilidad(),
				sortable			: true,
				pageable			: true,
				filterable			: true,
				userSettings        : true,
				excel 				: { allPages : true },
				height				: 450,
				selectable 			: "multiple row",
				navigatable         : true,
				allowCopy 			: true
		};

		
		
		$scope.buscar = function(){
			var filters = new Array();
			
			if (($scope.data.oConfiguracionAgente.seleccionado.IdEmpleadoEjecutivo))	
				filters.push({ field: "IdAgente", operator: "eq", value: Number($scope.data.oConfiguracionAgente.seleccionado.IdEmpleadoEjecutivo)});	
			
			
			if ($scope.data.kngFechaOperacion)
				 filters.push( { "field": "FechaVencimiento","operator": "eq", "value" : util.date($scope.data.kngFechaOperacion).hours(0).minutes(0).seconds(0).toDate() });
			
			$scope.gridApiReporteRentabilidad.dataSource.filter(filters);
		};		
		
		
		$scope.limpiarFiltros = function(){
			$scope.data.oConfiguracionAgente.idActual = -1;
			$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.seleccionReporteRentabilidad = {};
			
			$scope.gridApiReporteRentabilidad.dataSource.removeAll();
		};
	});
}());
