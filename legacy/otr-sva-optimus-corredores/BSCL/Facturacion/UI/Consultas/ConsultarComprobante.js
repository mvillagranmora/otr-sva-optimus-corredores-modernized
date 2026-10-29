angular.module('ModuloFacturacion').controller('ConsultarComprobanteCtrl',
		function($scope,$translate,$modal,
				bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsConsultarComprobanteGrid)
{
	$scope.data	= {};
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivo  = true;   //Activa botones
	
	$scope.data.oFechaComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oFechaComprobanteHasta = new Date($scope.data.oFechaComprobanteDesde.getTime() + 30 * 24 * 60 * 60 * 1000);
	
	$scope.data.oFolioComprobanteDesde = 0;
	$scope.data.oFolioComprobanteHasta = 99999999999;
	
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
	
	$scope.data.oTipoComprobante={
		tipo : "combobox-tipo-comprobante",
		configuracion : {},
		filtros : {
			IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oMonedaLiquidacion={
		tipo : "combobox-moneda-tesoreria",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oEstadoComprobante={
		tipo : "combobox-estado-facturacion",
		configuracion : {},
		filtros : {
			        NombreEntidad : { "field": "NombreEntidad","operator": "eq", "value"   :  "FAC.Comprobante"}
		          },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oClasificacionTipoInstrumento={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : { 	
			IdEstado :	{field : "IdEstado", operator : "eq", value : "ACT"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : { 	
			          IdClasificacionTipoInstrumento : {
		                                                  "field"   : "IdClasificacionTipoInstrumento", "operator": "eq", "value"   : ""} 	
			                                           },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	
	$scope.data.oSucursal={
		tipo : "combobox-sucursal",
		configuracion : {},
		filtros : {
			EstadoSucursal : { field: "EstadoSucursal", operator: "eq", value: "VIG" },
			IdEmpresa :	{field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oEmpleado={
		tipo : "combobox-empleado",
		configuracion : {},
		filtros : {
			IdSucursal : { field: "IdSucursal", operator: "eq", value: "" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	
	//GRILLA PRINCIPAL
	$scope.confBasicaConsultarComprobantes = {
		dataSource			: bcsConsultarComprobanteGrid.dataSourceConsultarComprobantes,
		columns				: bcsConsultarComprobanteGrid.columnasConsultarComprobantes(),
		rowColorBackground	: bcsConsultarComprobanteGrid.fnColorBk,	
		sortable			: true,
		//pageable			: true,
		//autoBind			: true,
		filterable			: true,
		height				: 470,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true
			
	};
	
	//SUB-GRILLA DETALLE DE COMPROBANTE
	$scope.confBasicaConsultarComprobanteDetalle = function(dataItem)
	  {
		var filtrosComprobante = [{field: "IdComprobante", operator: "eq", value: Number(dataItem.IdComprobante)}];
		return {
		      dataSource		: bcsConsultarComprobanteGrid.dataSourceConsultarComprobanteDetalle(filtrosComprobante),
		      columns			: bcsConsultarComprobanteGrid.columnasConsultarComprobanteDetalle(),		
		      sortable			: true,
		      pageable			: true,		
		      height			: 200,
		      autoBind          : true,
			  selectable 		: "multiple row",
			  navigatable       : true,
			  allowCopy 		: true
				
		   };
	  };
	  
	//SUB-GRILLA MOVIMIENTO COMPROBANTE DETALLE	  
	$scope.confBasicaConsultarMovimientoComprobanteDetalle = function(dataItem)
	  {
		var filtrosComprobanteDetalle = [{field: "IdComprobanteDetalle", operator: "eq", value: Number(dataItem.IdComprobanteDetalle)}];
		return {
			dataSource			: bcsConsultarComprobanteGrid.dataSourceConsultarMovimientoComprobanteDetalle(filtrosComprobanteDetalle),
			columns				: bcsConsultarComprobanteGrid.columnasConsultarMovimientoComprobanteDetalle(),
			sortable			: true,
			pageable			: true,
			height				: 120,
			selectable 			: "multiple row",
			autoBind            : true,
			navigatable         : true,
			userSettings        : true,
			allowCopy 			: true
			
		   };
	};
	  
	  
	  
	  
	  //SUB-GRILLA DE COBROS
	$scope.confBasicaConsultarComprobanteCobro = function(dataItem)
	  {
		 var filtrosComprobanteCobros = [{field: "IdComprobante", operator: "eq", value: Number(dataItem.IdComprobante)}];
		 return {			  	 
		       dataSource		: bcsConsultarComprobanteGrid.dataSourceConsultarComprobanteCobro(filtrosComprobanteCobros),
		       columns			: bcsConsultarComprobanteGrid.columnasConsultarComprobanteCobro(),		
		       sortable			: true,
		       pageable			: true,
		       height			: 200,
		       autoBind         : true,
			   selectable 		: "multiple row",
			   navigatable      : true,
			   allowCopy 		: true
				
		 };
	  };
	
	//SUB-GRILLA FORMA DE PAGO
	$scope.confBasicaConsultarComprobanteFormaDePago = function(dataItem)
	  {
		 var filtrosComprobanteFormaDePago = [{field: "IdComprobante", operator: "eq", value: Number(dataItem.IdComprobante)}];
		 return {
			  dataSource		: bcsConsultarComprobanteGrid.dataSourceConsultarComprobanteFormaDePago(filtrosComprobanteFormaDePago),
		      columns			: bcsConsultarComprobanteGrid.columnasConsultarComprobanteFormaDePago(),
		      sortable			: true,
		      pageable			: true,
		      height			: 200,
		      autoBind          : true,
			  selectable 		: "multiple row",
			  navigatable       : true,
			  allowCopy 		: true
				
		 };
	};
	

    //OPCIONES	
	$scope.buscar = function(){			
		$scope.gridApiConsultarComprobantes.dataSource.filter(getFilters());		
	};
				
	function getFilters(){	
		var filters = new Array();
		
		if ($scope.data.oClientePortafolio.seleccionado.IdCliente)	
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente)});
		if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)});		
		if ($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante)
			filters.push( { "field": "IdTipoComprobante","operator": "eq", "value" : $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante});	    		
		if ($scope.data.oMonedaLiquidacion.seleccionado.IdMoneda)
			filters.push( { "field": "IdMoneda","operator": "eq", "value" : $scope.data.oMonedaLiquidacion.seleccionado.IdMoneda});	    		
		if ($scope.data.oEstadoComprobante.seleccionado.IdEstado)
			filters.push( { "field": "IdEstadoComprobante","operator": "eq", "value" : $scope.data.oEstadoComprobante.seleccionado.IdEstado});
		if ($scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
			filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});	    		
		if ($scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento});
		//if ($scope.data.oTipoProducto.seleccionado.IdTipoProducto && $scope.data.oTipoProducto.seleccionado.IdTipoProducto !== "0")
		//	filters.push( { "field": "IdTipoProducto","operator": "eq", "value" : $scope.data.oTipoProducto.seleccionado.IdTipoProducto});	    		
		if ($scope.data.oSucursal.seleccionado.IdSucursal)
			filters.push( { "field": "IdSucursalEmpleadoEjecutivoPortafolio","operator": "eq", "value" : $scope.data.oSucursal.seleccionado.IdSucursal});	    		
		if ($scope.data.oEmpleado.seleccionado.IdEmpleado)
			filters.push( { "field": "IdEmpleadoEjecutivoPortafolio","operator": "eq", "value" : $scope.data.oEmpleado.seleccionado.IdEmpleado});	    		
		if ($scope.data.oFechaComprobanteDesde)		    
		    filters.push( { "field": "FechaComprobante","operator": "gte", "value" : util.date($scope.data.oFechaComprobanteDesde).hours(0).minutes(0).seconds(0).toDate() });
		if ($scope.data.oFechaComprobanteHasta)		    
		    filters.push( { "field": "FechaComprobante","operator": "lte", "value" : util.date($scope.data.oFechaComprobanteHasta).hours(0).minutes(0).seconds(0).toDate() });
		if ($scope.data.oFolioComprobanteDesde)
			filters.push( { "field": "FolioComprobante","operator": "gte", "value" : Number($scope.data.oFolioComprobanteDesde) });				
		if ($scope.data.oFolioComprobanteHasta)
			filters.push( { "field": "FolioComprobante","operator": "lte", "value" : Number($scope.data.oFolioComprobanteHasta) });	
			
		return filters;		
	};
	
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdPortafolio = false;
		$scope.data.oTipoComprobante.idActual = 0;
		$scope.data.oMonedaLiquidacion.idActual = 0;
		$scope.data.oEstadoComprobante.idActual = 0;
		$scope.data.oClasificacionTipoInstrumento.idActual = 0;
		$scope.data.oTipoInstrumento.idActual = 0;
		//$scope.data.oTipoProducto.idActual = 0;
		$scope.data.oSucursal.idActual = 0;
		$scope.data.oEmpleado.idActual = 0;
		
		$scope.data.oFolioComprobanteDesde = 0;
		$scope.data.oFolioComprobanteHasta = 99999999999;

		$scope.data.oFechaComprobanteHasta = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oFechaComprobanteDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
		$scope.gridApiConsultarComprobantes.dataSource.removeAll();
	};	
});	