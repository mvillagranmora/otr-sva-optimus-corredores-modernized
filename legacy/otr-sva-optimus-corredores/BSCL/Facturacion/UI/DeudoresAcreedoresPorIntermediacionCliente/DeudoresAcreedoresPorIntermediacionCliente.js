angular.module('ModuloFacturacion').controller('DeudoresAcreedoresPorIntermediacionClienteCtrl',
		function($scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosFacturacion,
				 bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma, bcsDeudoresAcreedoresPorIntermediacionClienteGrid,bcsOperacionesComunes)
{
	$scope.data = {};
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';	
	
	$scope.data.oFechaLiquidacionComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oFechaLiquidacionComprobanteHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	//COMBOBOX
	$scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};

	$scope.data.oConfiguracionTipoComprobante={
		tipo : "combobox-tipo-comprobante",
		configuracion : {},
		filtros : {
			IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.data.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento : 
			   {
	             "field"   : "IdClasificacionTipoInstrumento",
	             "operator": "eq",
	             "value"   : ""
			   } 	
		},
		//filtros : { IdTipoInstrumento : {"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""}	},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	//GRILLA PRINCIPAL
	$scope.confBasicaDeudoresAcreedoresPorIntermediacionCliente = {
		dataSource			: bcsDeudoresAcreedoresPorIntermediacionClienteGrid.dataSourceComprobantes,
		columns				: bcsDeudoresAcreedoresPorIntermediacionClienteGrid.columnasComprobantes(),
		rowColorBackground	: bcsDeudoresAcreedoresPorIntermediacionClienteGrid.fnColorBk,	
		sortable			: true,
		//pageable			: true,
		//autoBind			: true,
		filterable			: true,
		excel 				: { allPages : true },
		height				: 450,
		selectable 			: "multiple row",
		navigatable         : true,
		allowCopy 			: true
	};
	

	
	
	$scope.buscar = function(){			
		$scope.gridApiDeudoresAcreedores.dataSource.filter(getFilters());		
	};

	function getFilters(){
		var filters = new Array();

		filters.push (	{"field": "IdTipoComprobante","operator": "neq","value": "FOE"},
						{"field": "IdTipoComprobante","operator": "neq","value": "FOX"},
						{"field": "IdTipoComprobante","operator": "neq","value": "NEO"},
						{"field": "IdTipoComprobante","operator": "neq","value": "FEE"},
						{"field": "IdTipoComprobante","operator": "neq","value": "FOC"});
		
		if ($scope.data.oClientePortafolio.seleccionado.IdCliente)	
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente)});
		if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)});		
		if ($scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante)
			filters.push( { "field": "IdTipoComprobante","operator": "eq", "value" : $scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante});	    		
		if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
			filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});
		if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento});
		if ($scope.data.oFechaLiquidacionComprobanteDesde)		    
		    filters.push( { "field": "FechaLiquidacion","operator": "gte", "value" : util.date($scope.data.oFechaLiquidacionComprobanteDesde).hours(0).minutes(0).seconds(0).toDate() });
		if ($scope.data.oFechaLiquidacionComprobanteHasta)		    
		    filters.push( { "field": "FechaLiquidacion","operator": "lte", "value" : util.date($scope.data.oFechaLiquidacionComprobanteHasta).hours(0).minutes(0).seconds(0).toDate() });

		return filters;
	}
	
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdPortafolio = false;
		$scope.data.oConfiguracionTipoComprobante.idActual = 0;
		$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.data.oFechaLiquidacionComprobanteHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oFechaLiquidacionComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.gridApiDeudoresAcreedores.dataSource.removeAll();
	};
	
	//***********************************************
	// Seleccionar todos los Movimientos 
	//***********************************************
	$scope.data.seleccionarTodosMovimientos = function() {	
		$scope.gridApiDeudoresAcreedores.select($scope.gridApiDeudoresAcreedores.tbody.find(">tr"));		
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		$scope.gridApiDeudoresAcreedores.clearSelection();
	};
	
	//Primer evento
	$timeout(function(){
		$scope.data.oConfiguracionTipoInstrumento.filtros.read = true;
		//$scope.data.oConfiguracionEmpleado.filtros.read = true;
		
		//$scope.limpiarFiltros();
	},1000);
});