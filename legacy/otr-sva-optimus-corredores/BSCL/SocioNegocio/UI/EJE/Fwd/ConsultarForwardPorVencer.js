(function() {
	angular.module('ModuloMesa').controller('ConsultarForwardPorVencer',
	function($scope,$translate,$resource,$timeout,$http,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA, bcsConsultarForwardPorVencerGrid)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		
		$scope.seleccionFwdPorVencer = {};
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
					
		
		$scope.confBasicaFwdPorVencer = { 
				dataSource			: bcsConsultarForwardPorVencerGrid.dataSourceForwardPorVencer,
				columns				: bcsConsultarForwardPorVencerGrid.columnasForwardPorVencer(),
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
			
			if (($scope.data.oConfiguracionAgente.seleccionado.IdPersonaEjecutivo))	
				filters.push({ field: "IdAgente", operator: "eq", value: Number($scope.data.oConfiguracionAgente.seleccionado.IdPersonaEjecutivo)});	
			
			
			if ($scope.data.kngFechaOperacion)
				filters.push( { "field": "FechaVencimientoAnticipo","operator": "eq", "value" : util.date($scope.data.kngFechaOperacion).hours(0).minutes(0).seconds(0).toDate() });
			
			filters.push({"field": "Estado","operator": "neq", "value" : "ANU"});
			filters.push({"field": "Estado","operator": "neq", "value" : "MOD"});
			$scope.gridApiFwdPorVencer.dataSource.filter(filters);
		};		
		
		
		$scope.limpiarFiltros = function(){
			$scope.data.oConfiguracionAgente.idActual = -1;
			$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.seleccionFwdPorVencer = {};
			
			$scope.gridApiFwdPorVencer.dataSource.removeAll();
		};
	});
}());
