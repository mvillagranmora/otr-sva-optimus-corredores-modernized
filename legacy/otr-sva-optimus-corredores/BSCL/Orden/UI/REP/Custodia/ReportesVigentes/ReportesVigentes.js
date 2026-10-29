angular.module('ModuloOrdenes').controller('ReportesVigentesCtrl',
		function($scope,$state,$translate,$resource,$timeout,bcsComunes,
				 bcsServicioPlataforma,bcsOperacionesComunes)
{	

	$scope.kngFecha 		 =  util.dateInicioDia($scope.SesionFechaSistema);
		
	$scope.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	$scope.oConfiguracionPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	$scope.oConfiguracionTipoOperacion={
		//tipo : "combobox-tipo-operacion",
		configuracion : {},
		filtros : {
			EstadoTipoOperacionTipoInstrumento : { "field"   : "EstadoTipoOperacionTipoInstrumento","operator": "eq", "value" : "ACT" },
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "SIM"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};	
	
	//GRILLA
	
	var dsGrillaReportesVig = new bcs.DataSource({
		sortable	: true,
		pageable	: true,
		filterable 	: true,
		excel : {
			allPages: true,
		},
		height		: 495,
		selectable: "multiple, row",
		type 		: "odata",
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						sort 				: { field: "FolioCartera", dir: "desc" },
						pageSize 			: 25,
						transport 			: {read : {
														url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1056",
														dataType : "json"
													  }
											  },
						schema 				: { model: {
													fields : {
														FolioCartera			: {type: "number"},
														AccionOperacion			: {type: "string"},
														AccionOperacionPlazo	: {type: "string"},
														FechaInversion			: {type: "sysdate"},
														FechaVencimientoOperacion: {type: "sysdate"},
														MontoCompra 			: { type: "number"},
														MontoAPlazo		 		: { type: "number"},
														EGRFechaOperacion		: {type: "sysdate"},
														INGCantidad				: {type: "number"},
														INGPrecioCompra			: {type: "number"},
														INGTasaCompra 			: {type: "number"},
														INGMontoATasaCompraActual		: {type: "number"},
														INGMontoAPlazo			: {type: "number"},
														INGFechaLiquidacion 	: {type: "sysdate"},
														EGRFechaLiquidacion 	: {type: "sysdate"},
														suby_Nemotecnico		: {type: "string"},
														FolioComprobante		: {type: "number"},
														suby_NombreTipoInstrumento	: {type: "string"},
														Cliente					: {type: "string"},
														Proposito				: {type: "string"},
														CodigoPortafolio 		: {type: "string"},
														FechaLiquidacion		: {type: "sysdate"},
														IdEstado				: {type: "string"},
														PaloNormal				: {type: "number"},
														PaloPlazo				: {type: "number"},
														NombreCorredora			: {type: "string"},
														IdCorredoraContraparte 	: {type: "number"},
														NombreEjecutivo 		: {type: "string"},
														FechaLiquidacionMovtoContado : {type: "sysdate"},
														PrecioInicial 			: {type: "number"},
														PrecioAPlazo			: {type: "number"},
														suby_INGPrecioMercadoActual 	: {type: "number"},
														PrecioActualCalculado 	: {type: "number"},
														IdClasificacionTipoInstrumentoOperacion 	: {type: "string"},
														EGRIdOperacion				: {type: "number"},
														}
													}
											   }
					}
	});
	
	
	$scope.confBasicaReportesVigentes = {
		dataSource 	:dsGrillaReportesVig,
		columns		: [  {field: "FolioCartera", title: "UI_FOLIO_CARTERA", width: "80px", align: "right", sortable : false, groupable: true }
						,{field: "PaloNormal", title: "UI_TRANSACCION", width: "80px", align: "right", sortable : false, groupable: true }
						,{field: "PaloPlazo", title: "UI_TRANSACCION_A_PLAZO", width: "80px", align: "right", sortable : false, groupable: true }
						,{field: "AccionOperacionPlazo", title: "UI_TIPO_OPERACION", width: "100px", align: "left", sortable : true, groupable: true } 
						,{field: "FechaInversion", title: "UI_FECHA_INVERSION", width: "80px", align: "center", sortable : true, groupable: true }
						,{field: "FechaLiquidacionMovtoContado", title: "UI_FECHA_LIQUIDACION", width: "80px", align: "center", sortable : true, groupable: true }
						,{field: "FechaVencimientoOperacion", title: "UI_FECHA_VENCIMIENTO", width: "80px", align: "center", sortable : true, groupable: true }
						,{field: "INGCantidad", title: "UI_CANTIDAD", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "PrecioInicial", title: "UI_PRECIO_INICIAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioInicial,'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "PrecioActualCalculado", title: "UI_PRECIO_ACTUAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioActualCalculado,'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "PrecioAPlazo", title: "UI_PRECIO_PLAZO", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioAPlazo,'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "INGTasaCompra", title: "UI_TASA", width: "60px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGTasaCompra,'GEN','Tasa',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "MontoCompra", title: "UI_MONTO_INICIAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "INGMontoATasaCompraActual", title: "UI_MONTO_ACTUAL", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "MontoAPlazo", title: "UI_MONTO_PLAZO", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoAPlazo,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
						,{field: "suby_Nemotecnico", title: "UI_NEMOTECNICO", width: "120px", align: "left", sortable : true, groupable: true }
						,{field: "FolioComprobante", title: "UI_FOLIO_COMPROBANTE", width: "80px", align: "right", sortable : false, groupable: true }
						,{field: "suby_NombreTipoInstrumento", title: "UI_TIPO_INSTRUMENTO_ABR", width: "100px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
						,{field: "Cliente", title: "UI_CLIENTE", width: "150px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
						,{field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "55px", align: "left", sortable : true, groupable: true }
						,{field: "NombreEjecutivo", title: "UI_AGENTE", width: "120px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"}  }
						,{field: "NombreCorredora", title: "UI_CORREDOR_CONTRAPARTE", width: "150px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
					  ],		
		sortable	: true,
		userSettings: true,
		allowCopy	: { delimeter: "\t" },
		pageable	: true,
		filterable	: true,
		height		: 465,
		selectable 	: "row"
	};
	
	//BOTONES
	$scope.buscar = function()
	{		
		var filters = new Array();
		
		if ($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona)
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente || 0)});		
		if ($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio)			
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0)});				
		if ( $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion && $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion != 0)
			filters.push( { "field": "IdTipoOperacion","operator": "eq", "value" :  $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion});		

		if ($scope.instrumento !== undefined)
			filters.push( { "field": "suby_IdInstrumento","operator": "eq", "value" : Number($scope.instrumento.IdInstrumento)});
		
		
		if ($scope.Transaccion !== undefined){
			if ($scope.Transaccion > 0){
				filters.push({
					logic: "or",
					filters : [
					       { field: "PaloNormal", operator: "eq", value: $scope.Transaccion },
				           { field: "PaloPlazo", operator: "eq", value: $scope.Transaccion  }]
				
				});
			}
		}
		
		
		filters.push( { "field": "FechaVencimientoOperacion","operator": "gte", "value" : util.dateInicioDia($scope.kngFecha)});
		
		filters.push({
			logic: "or",
			filters : [{
				logic: "and",
				filters : [{ field: "INGFechaLiquidacion", operator: "lte", value: $scope.kngFecha },
				           { field: "EGRCantidad", operator: "eq", value: 0 },
				           { field: "IdEstado", operator: "eq", value: 'VIG' }]
				
			},
			
			{
				logic: "and",
				filters : [{ field: "INGFechaLiquidacion", operator: "lte", value: $scope.kngFecha },
				           { field: "EGRCantidad", operator: "neq", value: 0 },
				           { field: "EGRFechaLiquidacion", operator: "gt", value: $scope.kngFecha }]
				
			}
			
			
			]
					
		});
		
						
		
		dsGrillaReportesVig.filter(filters);
	};
	$scope.anticipoReportes = function()
	{
		if ($scope.seleccionReportesVigentes != undefined) {
			var aux = $.extend(true, {}, $scope.seleccionReportesVigentes);
			bcsComunes.ventanaAbrir("AnticipoReportes", aux, true, false);
		}
		else {
			bcsComunes.msg("warning","info",$translate.instant('UI_SELECCIONAR_MOVIMIENTO'));
		}
	};
	
	$scope.limpiar = function()
	{					
		$scope.oConfiguracionTipoOperacion.idActual = 0;
		$scope.buscarinstrumento = "-1";
		$scope.oConfiguracionPortafolio.idActual = 0;
		$scope.oConfiguracionSocioNegocio.idActual = 0;
		$scope.TransaccionObj = "";
		$scope.Transaccion = undefined;
		
		dsGrillaReportesVig.removeAll();
	};
	
});

