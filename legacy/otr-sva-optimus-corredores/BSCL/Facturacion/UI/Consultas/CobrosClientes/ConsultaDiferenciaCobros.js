angular.module('ModuloFacturacion').controller('ConsultaDiferenciaCobrosCtrl',function($scope,bcsComunes){
	
	$scope.grid = {};
	$scope.filtros = {};
	var filters = {};
	var idVentana = bcsComunes.ventanaWinId($scope);

	
	$scope.filtros.kngFechaConsultaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaConsultaHasta = util.dateInicioDia($scope.SesionFechaSistema);	

	$scope.filtros.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};


	// CONFIGURACION GRILLA PRINCIPAL (Comprobantes con cobros con diferencias)
	$scope.grid.confComprobanteDiferenciaCobros = {
		dataSource  : new bcs.DataSource({ 
						type : "call",
						definition : {
							serverFiltering 	: false, // Filtrado de datos en el servidor.
							serverPaging 		: false, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.															
							callParameters      : filters,
							pageSize 			: 100,
							callTransaction 	: "3608",   // PLUGIN_FAC.jar@cl.bcs.facturacion.ConsultarCobrosClientesConDiferencia
							callResultSet 		: "P_OUT_COMPROBANTES_DIFERENCIAS",
							schema : {
								model : {
									fields : {
										IdComprobante					: {type : "number"}, 
										IdTipoComprobante				: {type : "string"},	
										IdConcepto						: {type : "string"}, 
										NombreConcepto					: {type : "string"},	
										IdPortafolio					: {type : "number"}, 
										NombrePortafolio				: {type : "string"},	
										IdCliente						: {type : "number"},
										NombreLargoCliente				: {type : "string"},
										DocumentoIdentidad				: {type : "string"},
										IdTipoOperacion					: {type : "string"},
										NombreTipoOperacion				: {type : "string"},
										IdTipoProducto					: {type : "string"}, 
										NombreTipoProducto				: {type : "string"}, 
										IdMoneda						: {type : "string"},   
										IdMonedaComprobante				: {type : "string"},
										FechaComprobante				: {type : "date"},
										FechaLiquidacion				: {type : "date"},
										Monto							: {type : "number"},
										MontoCobro						: {type : "number"}
									}
								}
							}
						}
					  }),
		columns 	: [
						 {field : "IdComprobante",				title:"UI_SECUENCIA_COMPROBANTE", 		width: "70px"}
						,{field : "IdTipoComprobante",			title:"UI_TIPO", 						width: "50px",  groupable: true}
						,{field : "NombreLargoCliente",			title:"UI_SOCIO_DE_NEGOCIO", 			width: "150px", groupable: true}
						,{field : "DocumentoIdentidad", 		title:"UI_DOCUMENTO_IDENTIDAD", 		width: "60px"}
						,{field : "NombrePortafolio", 			title:"UI_PORTAFOLIO", 					width: "100px", groupable: true}
						,{field : "NombreConcepto", 			title:"UI_CONCEPTO", 					width: "180px", groupable: true}
						,{field : "IdMonedaComprobante",		title:"UI_MONEDA", 						width: "80px",  groupable: true}
						,{field : "NombreTipoProducto",			title:"UI_TIPO_PRODUCTO", 				width: "120px",  groupable: true}
						,{field : "NombreTipoOperacion",		title:"UI_TIPO_OPERACION", 				width: "180px",  groupable: true}						
						,{field : "FechaComprobante",			title:"UI_FECHA_COMPROBANTE", 			width: "80px",  groupable: true}
						,{field : "FechaLiquidacion",			title:"UI_FECHA_LIQUIDACION", 			width: "80px",  groupable: true}
						,{field : "Monto", 						title:"UI_MONTO", 						width: "70px",	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>" }
						,{field : "MontoCobro", 				title:"UI_MONTO_COBRO", 				width: "70px",	template : " <label> #:  (util.bcs_formato(MontoCobro,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>" }
					],
		gridCreated : function(g) { 
						$scope.grid.ApiComprobanteDiferenciaCobros = g;							
					  },
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 408,
		selectable 	: "multiple row",
    	navigatable : true,
	    allowCopy 	: true,
		autoBind 	: false,
		userSettings: true,
		excel 		: { allPages: true },
	};

	
	
	
	
	
	// CONFIGURACION DE SUBGRILLA (Cobros con diferencias)
	
	var obj = {};
	
	obj.dsCobroDiferenciaConf = function(){
			return {
				type : "call",
				definition : {
					sort 				: { field: "IdBolsa", dir: "asc" },
					callParameters      : '',
					callTransaction 	: 3608,  // PLUGIN_FAC.jar@cl.bcs.facturacion.ConsultarCobrosClientesConDiferencia
					callResultSet 		: "P_OUT_COBROS_DIFERENCIAS",
					schema : {
						model : {
							fields : {	
								IdComprobante					: {type : "number"},
					 			NombreCobro						: {type : "string"},
								FormaCobro						: {type : "string"},
								IdMonedaCobro					: {type : "string"},
								NombreFormaCobro				: {type : "string"},
								ValorCobroFacturado				: {type : "number"},
								MontoCobroFacturado				: {type : "number"},
								ValorCobroPlantilla				: {type : "number"}
							}
						}
					}
					
					
										
				}
			};
	};


	$scope.confBasicaCobroDiferencia = function(item) 
	{
		
		var parametros = {};
		parametros = $scope.filtrosBusqueda();

		var dsDetalleConf = obj.dsCobroDiferenciaConf();
		
		dsDetalleConf.definition.callParameters = parametros;

		dsDetalleConf.definition.filter = [{ field: "IdComprobante",                   operator: "eq", value: Number(item.IdComprobante)}];

		return {
			dataSource			: new bcs.DataSource(dsDetalleConf),
			columns				: [
				{field : "NombreCobro",				title:"UI_TIPO_COBRO", 				width: "120px"},
				{field : "NombreFormaCobro",		title:"UI_FORMA_COBRO", 			width: "55px"},
				{field : "IdMonedaCobro",		    title:"UI_MONEDA_COBRO", 			width: "55px"},
				{field : "MontoCobroFacturado",		title:"UI_MONTO_COBRO_FACTURADO", 	width: "80px",	template : " <label> #:  (util.bcs_formato(MontoCobroFacturado,'GEN','Monto',IdMonedaCobro,'','')).valor #</label>"},
				{field : "ValorCobroFacturado",		title:"UI_VALOR_COBRO_FACTURADO", 	width: "80px",	template: kendo.template("#if ( FormaCobro == 'V')  {##= ValorCobroFacturado + ' %' ##} else {##= (util.bcs_formato(ValorCobroFacturado,'GEN','Monto',IdMonedaCobro,'','')).valor ##}#")},
				{field : "ValorCobroPlantilla",		title:"UI_VALOR_COBRO_PLANTILLA", 	width: "80px",	template: kendo.template("#if ( FormaCobro == 'V')  {##= ValorCobroPlantilla + ' %' ##} else {##= (util.bcs_formato(ValorCobroPlantilla,'GEN','Monto',IdMonedaCobro,'','')).valor ##}#")}
				],
			sortable			: false,
			pageable			: false,
			filterable			: false,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row"
		}
		
		
	}
	
	
	
	$scope.detalleComprobanteFacturacion = function(datos)
	{
		bcsComunes.ventanaAbrir("DetalleComprobanteFacturacion", {IdComprobante: datos[0].IdComprobante, IdTipoComprobante: datos[0].IdTipoComprobante},  false, false, idVentana );
		
	};	
	
	
	$scope.limpiarFiltros = function()
	{	
		$scope.filtros.kngFechaConsultaDesde =  util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaConsultaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.IdPortafolio			 = false;
		$scope.grid.ApiComprobanteDiferenciaCobros.clearSelection();
		$scope.grid.ApiComprobanteDiferenciaCobros.dataSource.removeAll();
	};


	
	$scope.filtrosBusqueda = function()
	{
		var filters = {};

		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		filters.p_IdUsuario 			= $scope.SesionIdUsuario; 
		filters.p_IdModulo 				= "FAC";
		filters.p_IdCliente				= (typeof $scope.filtros.oClientePortafolio !== 'undefined' ) ? Number($scope.filtros.oClientePortafolio.seleccionado.IdCliente) : null;
		filters.p_IdPortafolio			= (typeof $scope.filtros.oClientePortafolio !== 'undefined' ) ? Number($scope.filtros.oClientePortafolio.seleccionado.IdPortafolio) : null;
		filters.p_FechaDesde			= util.dateSPdate($scope.filtros.kngFechaConsultaDesde) || '0001-01-01';
		filters.p_FechaHasta			= util.dateSPdate($scope.filtros.kngFechaConsultaHasta) || '0001-01-01';
		
		return filters;
	}

	
	$scope.buscar = function()
	{	
		//Valida que no exceda los 30 días de consulta
		var resp = util.validarRangoFecha($scope.filtros.kngFechaConsultaDesde, $scope.filtros.kngFechaConsultaHasta);
		if(resp.estado === 'NOK')
		{	
			bcsComunes.msg("warning", "INFO", resp.mensaje);
			return false;
		}
		
		var parametros = {};
		parametros = $scope.filtrosBusqueda();
		console.log("PARAMETROS:" + JSON.stringify(parametros));
		$scope.grid.ApiComprobanteDiferenciaCobros.dataSource.setCallParameters(parametros);		
	};
});