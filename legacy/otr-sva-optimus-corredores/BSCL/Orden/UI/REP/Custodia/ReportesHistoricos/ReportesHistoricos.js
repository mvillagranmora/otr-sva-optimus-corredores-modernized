angular.module('ModuloOrdenes').controller('ReportesHistoricasCtrl',
		function($rootScope,$scope,$state,$translate,$resource,$timeout,bcsComunes,
				 bcsServicioPlataforma)
{	
	//SETEO
	$scope.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.fechaInicioOpt = {max:util.dateInicioDia($scope.SesionFechaSistema)};
	$scope.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);;
		

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
		tipo : "combobox-tipo-operacion",
		configuracion : {},
		filtros : {
			EstadoTipoOperacionTipoInstrumento : { "field"   : "EstadoTipoOperacionTipoInstrumento","operator": "eq", "value" : "ACT" },
			IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "SIM"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.oConfiguracionEstado={
		tipo : "combobox-estado-custodia",
		configuracion : {},
		filtros : {
			NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : 'BlotterSIM.Historica' }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	//GRILLA
	var dsGrillaReportesHis = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						userSettings 		: true,
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
														IdEstado 				: {type: "string"},
														NombreEstado 			: {type: "string"},
														AccionOperacion			: {type: "string"},
														AccionOperacionPlazo			: {type: "string"},
														FechaInversion			: {type: "sysdate"},
														FechaVencimientoOperacion		: {type: "sysdate"},
														EGRFechaOperacion				: {type: "sysdate"},
														INGCantidad				: {type: "number"},
														INGPrecioCompraActual				: {type: "number"},
														INGTasaCompraActual			: {type: "number"},
														INGMontoATasaCompraActual				: {type: "number"},
														INGMontoAPlazo			: {type: "number"},
														suby_Nemotecnico				: {type: "string"},
														FolioComprobante		: {type: "number"},
														suby_NombreTipoInstrumento	: {type: "string"},
														Cliente			: { type: "string"},
														Proposito				: { type: "string"},
													    suby_IdInstrumento : {type: "number"}
														}
													}
												}
					}
		
	});
	 
	$scope.confBasicaReportesHistoricas = {
		dataSource : dsGrillaReportesHis,
		columns				: [  {field: "FolioCartera", title: "UI_FOLIO_CARTERA", width: "90px", align: "right", sortable : false, groupable: true }
								,{field: "NombreEstado", title: "UI_ESTADO", width: "60px", align: "left", sortable : true, groupable: true}
								,{field: "AccionOperacionPlazo", title: "UI_TIPO_OPERACION", width: "120px", align: "left", sortable : false, groupable: true } 
								,{field: "FechaInversion", title: "UI_FECHA_INVERSION", width: "125px", align: "center", sortable : false, groupable: true}
								,{field: "FechaVencimientoOperacion", title: "UI_FECHA_VENCIMIENTO", width: "125px", align: "center", sortable : false, groupable: true }
								,{field: "EGRFechaOperacion", title: "UI_FECHA_TERMINO", width: "125px", align: "center", sortable : false }
								,{field: "INGCantidad", title: "UI_CANTIDAD", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "INGPrecioCompraActual", title: "UI_PRECIO", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGPrecioCompraActual,'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "INGTasaCompraActual", title: "UI_TASA", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGTasaCompraActual,'GEN','Tasa',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "INGMontoATasaCompraActual", title: "UI_MONTO", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "INGMontoAPlazo", title: "UI_MONTO_PLAZO", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoAPlazo,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "suby_Nemotecnico", title: "UI_NEMOTECNICO", width: "120px", align: "left", sortable : false, groupable: true }
								,{field: "FolioComprobante", title: "UI_FOLIO_COMPROBANTE", width: "90px", align: "right", sortable : false, groupable: true }
								,{field: "suby_NombreTipoInstrumento", title: "UI_TIPO_INSTRUMENTO_ABR", width: "150px", align: "left", sortable : false, groupable: true }
								,{field: "Cliente", title: "UI_CLIENTE", width: "150px", align: "left", sortable : false, groupable: true }
								,{field: "Proposito", title: "UI_PORTAFOLIO", width: "150px", align: "left", sortable : false, groupable: true }
							  ],		
		sortable			: true,
		userSettings		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: true,
		filterable			: true,
		height				: 500,
		selectable 			: "multiple row"
	};
	
	//OPCIONES
	$scope.buscar = function()
	{
		var filters = new Array();

		if ($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente)
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente || 0)});
		if ($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio)			
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0)});
		if ( $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion)
			filters.push( { "field": "IdTipoOperacion","operator": "eq", "value" :  $scope.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion});
		//if ($scope.instrumento.IdInstrumento)
		if ($scope.instrumento)
			filters.push( { "field": "suby_IdInstrumento","operator": "eq", "value" : $scope.instrumento.IdInstrumento});
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push( { "field": "IdEstado","operator": "eq", "value" : $scope.oConfiguracionEstado.seleccionado.IdEstado});					
		if ($scope.kngFechaInicio)
			//filters.push( { "field": "EGRFechaOperacion","operator": "gte", "value" : kendo.parseDate(mxment($scope.kngFechaInicio).format('YYYY-MM-DD'))});
			filters.push( { "field": "EGRFechaOperacion","operator": "gte", "value" : util.dateOdata($scope.kngFechaInicio)});
		if ($scope.kngFechaTermino)
			//filters.push( { "field": "EGRFechaOperacion","operator": "lte", "value" : kendo.parseDate(mxment($scope.kngFechaTermino).format('YYYY-MM-DD'))});
			filters.push( { "field": "EGRFechaOperacion","operator": "lte", "value" : util.dateOdata(util.dateTerminoDia($scope.kngFechaTermino))});

		dsGrillaReportesHis.filter(filters);	
		
	};	
	$scope.limpiar = function()
	{									
		$scope.oConfiguracionPortafolio.idActual = 0;
		$scope.oConfiguracionSocioNegocio.idActual = 0;
		
		$scope.oConfiguracionTipoOperacion.idActual = 0;
		$scope.buscarinstrumento = "-1";
		$scope.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oConfiguracionEstado.idActual = 0;
		
		dsGrillaReportesHis.removeAll();
	};
	
});

