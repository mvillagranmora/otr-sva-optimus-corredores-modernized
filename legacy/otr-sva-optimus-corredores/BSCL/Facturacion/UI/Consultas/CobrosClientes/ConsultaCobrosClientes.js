angular.module('ModuloFacturacion').controller('ConsultaCobrosClientesCtrl',function($scope,bcsComunes){
	
	
	$scope.grid = {};
	$scope.filtros = {};
	var filters = {};
	var idVentana = bcsComunes.ventanaWinId($scope);

	$scope.filtros.kngFechaConsultaDesde =  util.dateInicioDia($scope.SesionFechaSistema);
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
	
//	$scope.filtros.oConfiguracionSocioNegocioContrato={
//		tipo : "combobox-portafolio-contrato-cobro-periodico",
//		configuracion :{},
//		idActual : false,
//		seleccionado : false,
//		filtros: {
//			ValorContratoCobroPeriodico : {"field": "ValorContratoCobroPeriodico","operator": "neq", "value" : 1}
//		},
//	};	

	$scope.filtros.oConfiguracionTipoCLiente={		
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : { "field": "IdEstado","operator": "eq", "value"   :  'ACT' }
		},
	};	
	$scope.filtros.oConfiguracionEjecutivo={
		tipo : "combobox-ejecutivo",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : { "field": "IdEstado","operator": "eq", "value"   :  'ACT' }
		},
	};
	$scope.filtros.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		
	};
	

	$scope.grid.confComisionCliente = {
		dataSource  : new bcs.DataSource({ 
						type : "call",
						definition : {
							serverFiltering 	: false, // Filtrado de datos en el servidor.
							serverPaging 		: false, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.															
							callParameters      : filters,
							callTransaction 	: "1778",
							callResultSet 		: "P_OUT_COBROS",
							schema : {
								model : {
									fields : {
										IdCliente						: {type : "number"},
										NombreCliente					: {type : "string"},
										DocumentoIdentidad				: {type : "string"},
										IdTipoCliente					: {type : "string"},
										IdPortafolio					: {type : "number"},
										Portafolio						: {type : "string"},
										TipoContratoCobroPeriodico		: {type : "string"},
										FormatoContratoCobroPeriodico	: {type : "string"},
										NombreCompletoEjecutivo			: {type : "string"},
										NombreSucursal					: {type : "string"},
										IdClasificacionTipoInstrumento	: {type : "string"},
										IdTipoInstrumento				: {type : "string"},
										NombreTipoInstrumento			: {type : "string"},
										IdMonedaComprobante				: {type : "string"},
										MontoComprobante				: {type : "number"},
										MontoCobroComprobante			: {type : "number"},
										MontoFinal						: {type : "number"},
										TotalComision					: {type : "number"},
										TotalDerecho					: {type : "number"},
										TotalGasto						: {type : "number"},
										TotalImpuesto					: {type : "number"},
										TotalRetencion					: {type : "number"},
												
									}
								}
							}
						}
					  }),
		columns 	: [  {field : "NombreCliente", 					title:"UI_SOCIO_DE_NEGOCIO", 			width: "150px", groupable: true}
						,{field : "DocumentoIdentidad", 			title:"UI_DOCUMENTO_IDENTIDAD", 		width: "60px"}					
						,{field : "Portafolio", 					title:"UI_PORTAFOLIO", 					width: "100px", groupable: true}
						,{field : "IdTipoCliente", 					title:"UI_TIPO_CLIENTE", 				width: "100px", groupable: true}
						,{field : "NombreCompletoEjecutivo", 		title:"UI_EJECUTIVO", 					width: "150px", groupable: true}
						,{field : "NombreSucursal", 				title:"UI_SUCURSAL", 					width: "200px", groupable: true}
						,{field : "TipoContratoCobroPeriodico", 	title:"UI_TIPO_CONTRATO", 				width: "250px", groupable: true}
						,{field : "FormatoContratoCobroPeriodico",	title:"UI_FORMATO_CONTRATO", 			width: "80px",	groupable: true}
						,{field : "IdClasificacionTipoInstrumento", title:"UI_CLASIFICACION_INSTRUMENTO",	width: "50px",	groupable: true}
						,{field : "NombreTipoInstrumento", 			title:"UI_TIPO_INSTRUMENTO", 			width: "150px", groupable: true}
						,{field : "IdMonedaComprobante", 			title:"UI_MONEDA", 						width: "50px",	groupable: true}
						,{field : "MontoComprobante", 				title:"UI_MONTO_COMPROBANTE", 			width: "100px",	template : " <label> #:  (util.bcs_formato(MontoComprobante,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"} //$translate.instant("UI_TOTAL")+': '+
						,{field : "TotalComision", 					title:"UI_COMISIONES", 					width: "100px",	template : " <label> #:  (util.bcs_formato(TotalComision,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"}
						,{field : "TotalDerecho", 					title:"UI_DERECHOS", 					width: "100px",	template : " <label> #:  (util.bcs_formato(TotalDerecho,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",		aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"}
						,{field : "TotalGasto", 					title:"UI_GASTOS", 						width: "100px",	template : " <label> #:  (util.bcs_formato(TotalGasto,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",		aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"}
						,{field : "TotalImpuesto", 					title:"UI_IMPUESTOS", 					width: "100px",	template : " <label> #:  (util.bcs_formato(TotalImpuesto,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"}
						,{field : "TotalRetencion", 				title:"UI_RETENCIONES", 				width: "100px",	template : " <label> #:  (util.bcs_formato(TotalRetencion,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"}
						,{field : "MontoFinal", 					title:"UI_MONTO_FINAL", 				width: "100px",	template : " <label> #:  (util.bcs_formato(MontoFinal,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>",		aggregates: ["sum"], groupFooterTemplate:"<div style='float: right'>#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#</div>"}
					  ],
		gridCreated : function(g) { 
						$scope.grid.ApiComisionCliente = g;							
					  },
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		height 		: 390,
		selectable 	: "multiple row",
    	navigatable : true,
	    allowCopy 	: true,
		autoBind 	: false,
		userSettings: true,
		excel 		: { allPages: true },
		aggregate	: [
						{ field: "MontoComprobante", aggregate: "sum" },
						{ field: "TotalComision", aggregate: "sum" },
						{ field: "TotalDerecho", aggregate: "sum" },
						{ field: "TotalGasto", aggregate: "sum" },
						{ field: "TotalImpuesto", aggregate: "sum" },
						{ field: "TotalRetencion", aggregate: "sum" },
						{ field: "MontoFinal", aggregate: "sum" }
					  ]
	};
	

	
	$scope.limpiarFiltros = function()
	{			
		$scope.filtros.kngFechaConsultaDesde =  util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaConsultaHasta = util.dateInicioDia($scope.SesionFechaSistema);	
		$scope.showSociosDeNegocio	= true;
		$scope.showSociosDeNegocioConContrato = false;
		
		//$scope.filtros.oConfiguracionSocioNegocioContrato.idActual	= false;
		$scope.filtros.IdPortafolio					= false;			
		$scope.filtros.oConfiguracionEjecutivo.idActual 			= 0;
		$scope.filtros.oConfiguracionTipoCLiente.idActual			= 0;
		$scope.filtros.ngSucursal									= '';
		$scope.filtros.SocioConContratoCobro						= false;

		$scope.grid.ApiComisionCliente.clearSelection();
		$scope.grid.ApiComisionCliente.dataSource.removeAll();
	};

	$scope.buscar = function(){
		var filters = {};
		
		//Datos grillas	
		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;		
		filters.p_IdEjecutivo			= (typeof $scope.filtros.oConfiguracionEjecutivo.idActual !== 'undefined' && Number($scope.filtros.oConfiguracionEjecutivo.idActual) !==0) ? Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo) : null;
		filters.p_IdCliente				= (typeof $scope.filtros.oClientePortafolio !== 'undefined' ) ? Number($scope.filtros.oClientePortafolio.seleccionado.IdCliente) : null;
		filters.p_IdPortafolio			= (typeof $scope.filtros.oClientePortafolio !== 'undefined' ) ? Number($scope.filtros.oClientePortafolio.seleccionado.IdPortafolio) : null;
		filters.p_IdTipoCliente			= (typeof $scope.filtros.oConfiguracionTipoCLiente !== 'undefined' && Number($scope.filtros.oConfiguracionTipoCLiente.idActual) !==0) ? $scope.filtros.oConfiguracionTipoCLiente.seleccionado.IdTipoCliente : null;
		filters.p_FechaDesde			= util.dateSPdate($scope.filtros.kngFechaConsultaDesde) || '0001-01-01';
		filters.p_FechaHasta			= util.dateSPdate($scope.filtros.kngFechaConsultaHasta) || '0001-01-01';
		filters.p_IdTipoInstrumento		= (typeof $scope.filtros.oConfiguracionTipoInstrumento !== 'undefined' && Number($scope.filtros.oConfiguracionTipoInstrumento.idActual) !==0) ? $scope.filtros.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento : null;
		filters.p_SoloConContrato		= $scope.filtros.SoloConContratoCobro ? 'S' : 'N';
		$scope.grid.ApiComisionCliente.dataSource.setCallParameters(filters);
	};
});