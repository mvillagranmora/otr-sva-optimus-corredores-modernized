angular.module('ModuloTesoreria').controller('CuadraturaComprobantesCuentaInversionCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,bcsServiciosTesoreria)
{	
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	
	
	var translate_array = ['UI_CLIENTE'];		
	$translate(translate_array).then(function(TRADUCCION)
	{		
 		$scope.ngTipoSocioNegocio	=  	TRADUCCION.UI_CLIENTE;
	});
	
	$scope.oConfiguracionTipoMovimiento={
		tipo : "combobox-tipo-movimiento",
		configuracion : {},
		filtros : {
			IdModulo      : { "field"   : "IdModulo","operator": "eq", "value"   : "TES" },				
			IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa }
		},
	};
	$scope.oConfiguracionPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};	
	$scope.oConfiguracionCajaMoneda={
		tipo : "combobox-caja-moneda",
		configuracion : {},
		filtros : {
			IdCaja : { "field"   : "IdCaja","operator": "eq", "value"   : "" },
			IdMoneda : { "field"   : "IdMoneda","operator": "neq", "value"   : "" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};
	$scope.oConfiguracionConcepto={
		tipo : "combobox-concepto-tesoreria",
		configuracion : {},
		filtros : {
			NombreEntidad : { "field": "NombreEntidad", "operator": "eq", "value": "TES.IngresoEgreso" },
			IdEstado :      { "field": "IdEstado", "operator": "eq", "value": "VIG" },				
			TipoConceptoTesoreria :  { "field": "TipoConceptoTesoreria", "operator": "eq", "value": "" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};
	$scope.oConfiguracionEstadoTesoreria={
		tipo 	: "combobox-estado-tesoreria",
		filtros : {
			  complejo : [{"field": "NombreEntidad","operator": "eq", "value"   : 'TES.Comprobante'},
			              [{"field": "IdEstado","operator": "eq", "value"   : 'LIQ' },
						   {"field": "IdEstado","operator": "eq", "value"   : 'LIP' }]
			             ],
			  logic: "and",
			  filters: [
				{
				  logic: "or",
				  filters: [{"field": "IdEstado","operator": "eq", "value"   : 'LIQ' },
						   {"field": "IdEstado","operator": "eq", "value"   : 'LIP' }]
				},
				{"field": "NombreEntidad","operator": "eq", "value"   : 'TES.Comprobante'}
			  ]
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};
	
	$scope.confCuadratura = { 
		dataSource			: new bcs.DataSource({	
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.
									sort 			: { field: "IdComprobanteFormaDePago", dir: "desc" },
									transport 		: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1755",dataType : "json"}},
									schema : {
										model : {
											fields : {
												//Tesoreria
												IdComprobante 					: { type: "number"},
												NombreTipoMovimiento 			: { type: "string"},
												NombreConcepto					: { type: "string"},
												IdMoneda 						: { type: "string"},
												Monto 							: { type: "number"},
												FechaLiquidacion 				: { type: "sysdate"},
												NombrePersona 					: { type: "string"},
												NombrePersonaTitularPago		: { type: "string"},
												NombrePortafolioTitularPago		: { type: "string"},
												NombreCaja 						: { type: "string"},
												NombreEstado					: { type: "string"},
												
												//CuentaInversion
												IdCargoAbonoMovimiento			: { type: "number"},
												IdEstadoCTA						: { type: "string"},
												NombreEstadoCTA					: { type: "string"},
												NombreTipoMovimientoCTA			: { type: "string"},
												Cargo 							: { type: "number"},
												Abono 							: { type: "number"},
												ExisteDiferenciaEnMonto			: { type: "string"},
											}
										}
									},
								}
							  }),
		columns				: [
								 {field: "IdComprobante", 				title: "UI_SECUENCIA_COMPROBANTE", 		width: "70px",  template: '<label> #:  util.formateaNumero(IdComprobante,0) #</label>' }
					    		,{field: "NombreTipoMovimiento", 		title: "UI_TIPO_COMPROBANTE", 			width: "100px",	groupable: true }
					    		,{field: "NombreEstado", 				title: "UI_ESTADO",						width: "100px"}
					    		
					    		,{field: "IdCargoAbonoMovimiento", 		title: "UI_SECUENCIA_CTA",				width: "80px",  template: '<label> #:  util.formateaNumero(IdCargoAbonoMovimiento,0) #</label>'}
					    		,{field: "NombreTipoMovimientoCTA", 	title: "UI_TIPO_MOVIMIENTO",			width: "100px"}								
					    		,{field: "NombreEstadoCTA", 			title: "UI_ESTADO_CTA",					width: "100px"}

					    		
					    		,{field: "NombreConcepto", 				title: "UI_CONCEPTO", 					width: "250px",	groupable: true, sortable : false }					    		
								,{field: "FechaLiquidacion", 			title: "UI_FECHA_LIQUIDACION", 			width: "70px",	sortable : false}					    
					    		,{field: "IdMoneda", 					title: "UI_MONEDA", 					width: "80px",	groupable: true }
					    		,{field: "Monto", 						title: "UI_MONTO", 						width: "80px",	groupable: true, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
					    		,{field: "Cargo", 						title: "UI_CARGO", 						width: "80px",	groupable: true, template : " <label> #:  (util.bcs_formato(Cargo,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
					    		,{field: "Abono", 						title: "UI_ABONO", 						width: "80px",	groupable: true, template : " <label> #:  (util.bcs_formato(Abono,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
								,{field: "NombrePersona", 				title: "UI_SOCIO_NEGOCIO", 				width: "200px",	groupable: true }			
								,{field: "NombrePersonaTitularPago", 	title: "UI_PERSONA_PAGO_COBRO", 		width: "200px",	groupable: true }			
								,{field: "NombrePortafolioTitularPago", title: "UI_PORTAFOLIO", 				width: "140px",	groupable: true }
								,{field: "NombreCaja", 					title: "UI_CAJA", 						width: "80px",	groupable: true }
							  ],		
		sortable			: true,
		pageable			: false,
		filterable			: true,
		autobind			: true,
		height				: 470,
		selectable 			: "multiple row",
		gridCreated 		: function(g) { $scope.ApiCuadratura = g; },		
		rowColorBackground	: function(row) { 
				var color = "";
				 
				if (row.IdCargoAbonoMovimiento === 0 || row.IdEstadoCTA === 'ANU')    
					color = "red";
				if (row.ExisteDiferenciaEnMonto === 'NOK')
					color = "yellow";
				 
				return color;
		},
		userSettings		: true
	};
	
	$scope.detalleComprobante = function(datos){
		bcsComunes.ventanaAbrir("DetalleComprobanteTesoreria", {IdComprobante: datos[0].IdComprobante}, false, false );
	};
	
	
	$scope.buscarComprobantes = function(){
		var filters = new Array();
		
		if ($scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento && $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== 0)
			filters.push({ field: "IdTipoMovimiento", operator: "eq", value: $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento });		
		if ($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona)
			filters.push({ field: "IdPersona", operator: "eq", value: Number($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona || 0) });
		if ($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio)			
			filters.push({ field: "IdPortafolioTitularPago", operator: "eq", value: Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0)});		
		if ($scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: $scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado });
		if ($scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda)
			filters.push({ field: "IdMoneda", operator: "eq", value: $scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda });
		if ($scope.oConfiguracionConcepto.seleccionado.IdConcepto && $scope.oConfiguracionConcepto.seleccionado.IdConcepto != 0)
			filters.push({ field: "IdConcepto", operator: "eq", value: $scope.oConfiguracionConcepto.seleccionado.IdConcepto });
		if ($scope.kngFechaLiquidacionDesde)
			filters.push({ field: "FechaLiquidacion", operator: "gte", value: util.dateOdata($scope.kngFechaLiquidacionDesde) });
		if ($scope.kngFechaLiquidacionHasta)
			filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.kngFechaLiquidacionHasta) });
		if (typeof $scope.kngSecuenciaComprobante !== 'undefined')
			filters.push({ field: "IdComprobante", operator: "eq", value: Number($scope.kngSecuenciaComprobante || 0) });		

		$scope.ApiCuadratura.dataSource.filters(filters);
	};
	
	$scope.limpiarFiltros = function() {
		$scope.oConfiguracionTipoMovimiento.idActual 	= 0;
		$scope.oConfiguracionCajaMoneda.idActual 		= 0;
		$scope.oConfiguracionEstadoTesoreria.idActual 	= 0;
		$scope.oConfiguracionSocioNegocio.idActual 		= false;
		$scope.oConfiguracionConcepto.idActual 			= 0;
		$scope.kngFechaLiquidacionDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaLiquidacionHasta 	= util.dateTerminoDia($scope.SesionFechaSistema);

		$scope.kngSecuenciaComprobante = undefined;
		$scope.ApiCuadratura.dataSource.removeAll();
		$scope.seleccionCuadratura = [];
	};

	var firstHeight = $('#Win_CuadraturaComprobantesCuentaInversion');
	firstHeight.css("height", "675px");
	var firstWidth = $('#Win_CuadraturaComprobantesCuentaInversion')
	firstWidth.css("width", "979px");
});