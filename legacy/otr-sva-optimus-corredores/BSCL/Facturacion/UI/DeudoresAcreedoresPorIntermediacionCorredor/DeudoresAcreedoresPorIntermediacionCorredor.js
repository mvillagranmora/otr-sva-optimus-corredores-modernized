angular.module('ModuloFacturacion').controller('DeudoresAcreedoresPorIntermediacionCorredorCtrl',
		function($scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosFacturacion,
				 bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma, bcsDeudoresAcreedoresPorIntermediacionCorredorGrid,bcsOperacionesComunes)
{
	$scope.data = {};
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';	
	
	$scope.data.oFechaLiquidacionTransaccion = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
	//COMBOBOX

	$scope.data.oConfiguracionOperacion={
			tipo : "combobox-operacion",
			configuracion : {},
			filtros : { 	
				IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"},
				Comprobante : {"field"   : "Comprobante","operator": "eq","value"   : "S"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};

	$scope.data.oPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
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
	$scope.confBasicaDeudoresAcreedoresPorIntermediacionCorredor = {
		dataSource			: bcsDeudoresAcreedoresPorIntermediacionCorredorGrid.dataSourceTransacciones,
		columns				: bcsDeudoresAcreedoresPorIntermediacionCorredorGrid.columnasTransacciones(),
		rowColorBackground	: bcsDeudoresAcreedoresPorIntermediacionCorredorGrid.fnColorBk,	
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
		$scope.gridApiDeudoresAcreedoresCorredor.dataSource.filter(getFilters());		
	};

	function getFilters(){
		var filters = new Array();

		if ($scope.data.oFechaLiquidacionTransaccion)		    
		    filters.push( { "field": "FechaLiquidacion","operator": "eq", "value" : util.date($scope.data.oFechaLiquidacionTransaccion).hours(0).minutes(0).seconds(0).toDate() });
		
		if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
			filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});
        
		if ($scope.data.oConfiguracionOperacion.seleccionado.AccionOperacion)
			filters.push( { "field": "AccionOrden","operator": "eq", "value" : $scope.data.oConfiguracionOperacion.seleccionado.AccionOperacion});		    

		return filters;
	}
	
	
	$scope.limpiarFiltros = function()
	{
		$scope.data.oSocioNegocio.idActual = false;
		$scope.data.oConfiguracionTipoComprobante.idActual = 0;
		$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionOperacion.idActual = 0;
		$scope.data.oFechaLiquidacionComprobanteHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oFechaLiquidacionComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.gridApiDeudoresAcreedoresCorredor.dataSource.removeAll();
	};
	
	//***********************************************
	// Seleccionar todos los Movimientos 
	//***********************************************
	$scope.data.seleccionarTodosMovimientos = function() {	
		$scope.gridApiDeudoresAcreedoresCorredor.select($scope.gridApiDeudoresAcreedoresCorredor.tbody.find(">tr"));		
	};
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		$scope.gridApiDeudoresAcreedoresCorredor.clearSelection();
	};
	
	//Primer evento
	$timeout(function(){
		$scope.data.oConfiguracionTipoInstrumento.filtros.read = true;
		//$scope.data.oConfiguracionEmpleado.filtros.read = true;
		
		//$scope.limpiarFiltros();
	},1000);
});