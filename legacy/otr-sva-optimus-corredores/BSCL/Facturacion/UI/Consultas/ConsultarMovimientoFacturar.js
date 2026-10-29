angular.module('ModuloFacturacion').controller('ConsultarMovimientoFacturarCtrl',
		function($scope,$translate,$modal,
				bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsAlerta, OPCIONES_BCSALERTA,
				bcsConsultarMovimientoFacturarGrid)
{
	$scope.data	= {};
	
	var idVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'FAC';
	$scope.data.btnActivos  = true;   //Activa botones
	
	$scope.data.oFechaMovimientoDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	$scope.data.oFechaMovimientoHasta = new Date($scope.data.oFechaMovimientoDesde.getTime() + 30 * 24 * 60 * 60 * 1000);
	
	//COMBOBOX
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
			IdClasificacionTipoInstrumento : { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value"   : ""},
			NoDerivados					   : { "field" : "IdTipoInstrumento", "operator": "neq", "value" : "DER" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oOperacion={
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
			
	$scope.data.oEstadoMovimientoFactura={
		tipo : "combobox-estado-facturacion",
		configuracion : {},
		filtros : {
			        NombreEntidad : { "field": "NombreEntidad","operator": "eq", "value"   :  "FAC.MovimientoComprobante"}
		          },
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
		
	$scope.data.oCondicionLiquidacion={
		tipo : "combobox-condicion-liquidacion",
		configuracion : {}, 
		filtros : { 	
		Estado : {field : "EstadoTipoOperacionYCondicionDeLiquidacion", operator : "eq", value : "ACT"},
		IdTipoOperacion : {field : "IdTipoOperacion", operator : "eq", value : ""}},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
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
  	
	
	$scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
		  		   IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				   EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}},
	};	
	
	//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
	$scope.confBasicaConsultarMovimientoFacturar = { 
		dataSource			: bcsConsultarMovimientoFacturarGrid.dataSourceConsultarMovimientoFacturar,
		columns				: bcsConsultarMovimientoFacturarGrid.columnasConsultarMovimientoFacturar(),
		sortable			: true,
		pageable			: true,
		filterable			: true,
		userSettings        : true,
		////autoBind			: true,
		height				: 470,
	    selectable 			: "multiple row",
	    navigatable         : true,
		allowCopy 			: true,		
		excel               : {
                                allPages: true,
                              }	
		
	};
	
	//SUB-GRILLA DE COBROS 
	$scope.confBasicaConsultaMovimientoCobros = function(dataItem)
	  {
		
		var filtrosMovimientoCobros = [{field: "IdMovimientoComprobante", operator: "eq", value: Number(dataItem.IdMovimientoComprobante)}];
		return {
			  dataSource		: bcsConsultarMovimientoFacturarGrid.dataSourceConsultarMovimientoCobros(filtrosMovimientoCobros),
			  columns			: bcsConsultarMovimientoFacturarGrid.columnasConsultarMovimientoCobros(),		
			  sortable			: true,
			  pageable			: true,		
			  height			: 120,
			  width				: 300,
			  autoBind          : true,
			  selectable 		: "multiple row",
			  navigatable       : true,
			  allowCopy 		: true
	       };
	  }; 
	
	//SUB-GRILLA FORMAS DE PAGO
	$scope.confBasicaConsultaMovimientoFormasDePago = function(dataItem)
	  {
		var filtrosMovimientoForDePago = [{field: "IdMovimientoComprobante", operator: "eq", value: Number(dataItem.IdMovimientoComprobante)}];
		return {
			 dataSource			: bcsConsultarMovimientoFacturarGrid.dataSourceConsultarMovimientoFormasDePago(filtrosMovimientoForDePago),
		     columns			: bcsConsultarMovimientoFacturarGrid.columnasConsultarMovimientoFormasDePago(),		
		     sortable			: true,
		     pageable			: true,
		     height				: 120,
		     width				: 300,
		     autoBind           : true,
			 selectable 		: "multiple row",
			 navigatable        : true,
			 allowCopy 			: true
		  }
	  };
		
		
	
	//OPCIONES
	$scope.buscar = function(){			
			$scope.gridApiConsultarMovimientoFacturar.dataSource.filter(getFilters());		
		};
	
	function getFilters(){	
		
		var filters = new Array();
	
		if ($scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
	        filters.push( { "field": "IdClasificacionTipoInstrumento","operator": "eq", "value" : $scope.data.oClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento});			
		
		if ($scope.data.oOperacion.seleccionado.AccionOperacion)
			filters.push( { "field": "AccionTipoOperacion","operator": "eq", "value" : $scope.data.oOperacion.seleccionado.AccionOperacion});
						
		if ($scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento)
			filters.push( { "field": "IdTipoInstrumento","operator": "eq", "value" : $scope.data.oTipoInstrumento.seleccionado.IdTipoInstrumento});
		
		if ($scope.instrumentoDatos !== undefined){    		
	    	if ($scope.instrumentoDatos.IdInstrumento != "" && $scope.instrumentoDatos.IdInstrumento !== undefined)	    		
	    		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.instrumentoDatos.IdInstrumento) });
    	}
				
		if ($scope.data.oEstadoMovimientoFactura.seleccionado.IdEstado)
			filters.push( { "field": "IdEstadoFacturacion","operator": "eq", "value" : $scope.data.oEstadoMovimientoFactura.seleccionado.IdEstado});
		
		if ($scope.data.oCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion)
			filters.push( { "field": "IdCondicionLiquidacion","operator": "eq", "value" : $scope.data.oCondicionLiquidacion.seleccionado.IdCondicionDeLiquidacion});
		
		if ($scope.data.oFechaMovimientoDesde)
			filters.push( { "field": "FechaMovimientoComprobante","operator": "gte", "value" : util.date($scope.data.oFechaMovimientoDesde).hours(0).minutes(0).seconds(0).toDate() });
		
		if ($scope.data.oFechaMovimientoHasta)
			filters.push( { "field": "FechaMovimientoComprobante","operator": "lte", "value" : util.date($scope.data.oFechaMovimientoHasta).hours(0).minutes(0).seconds(0).toDate() });
		
		if ($scope.data.oTipoComprobante.seleccionado.IdTipoComprobante)
			filters.push( { "field": "IdTipoComprobante","operator": "eq", "value" : $scope.data.oTipoComprobante.seleccionado.IdTipoComprobante});
		
		if (($scope.data.oClientePortafolio.seleccionado.IdCliente))	
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente)});			
		
		if (($scope.data.oClientePortafolio.seleccionado.IdPortafolio))	
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)});			
		
		return filters;     
	};
			
		
	$scope.limpiarFiltros = function()
	{	
		$scope.data.buscarinstrumento = "-1";		
		$scope.filtros.IdPortafolio = false;
		$scope.data.oClasificacionTipoInstrumento.idActual = 0;
		$scope.data.oOperacion.idActual = 0;
		$scope.data.oTipoInstrumento.idActual = 0;
		$scope.data.oEstadoMovimientoFactura.idActual = 0;
		$scope.data.oCondicionLiquidacion.idActual = 0;
		$scope.data.oTipoComprobante.idActual = 0;
		$scope.data.oFechaMovimientoDesde = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.data.oFechaMovimientoHasta = new Date($scope.data.oFechaMovimientoDesde.getTime() + 30 * 24 * 60 * 60 * 1000);

		$scope.gridApiConsultarMovimientoFacturar.dataSource.removeAll();	
	};
	
	
});