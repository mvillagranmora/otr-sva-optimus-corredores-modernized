angular.module('ModuloCustodia').controller('SimultaneasHistoricasCtrl',
		function($rootScope,$scope,$state,$translate,$resource,$timeout,bcsComunes,
				 bcsServicioPlataforma)
{	
	//SETEO
	$scope.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.fechaInicioOpt = {max:util.dateInicioDia($scope.SesionFechaSistema)};
	$scope.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);;
		

	$scope.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
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
	var dsGrillaSimulHis = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
						serverFiltering 	: true, // Filtrado de datos en el servidor.
						serverPaging 		: true, // Paginado de los datos en el servidor.
						serverSorting 		: true, // Ordenamiento de los datos en el servidor.
						userSettings 		: true,
						sort 				: { field: "FolioCartera", dir: "desc" },
						pageSize 			: 25,
						transport 			: {read : {
														url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1008",
														dataType : "json"
													  }
											  },
						schema 				: { model: {
													fields : {
														FolioCartera			: {type: "number"},
														IdEstado 				: {type: "string"},
														NombreEstado 			: {type: "string"},
														AccionOperacion			: {type: "string"},
														FechaInversion			: {type: "sysdate"},
														FechaVencimientoOperacion		: {type: "sysdate"},
														EGRFechaOperacion				: {type: "sysdate"},
														EGRMonto					    : {type: "number"},
														INGCantidad				: {type: "number"},
														INGPrecioCompraActual				: {type: "number"},
														INGTasaCompraActual			: {type: "number"},
														INGMontoATasaCompraActual				: {type: "number"},
														MontoAPlazo				: {type: "number"},
														INGMontoAPlazo			: {type: "number"},
														suby_Nemotecnico				: {type: "string"},
														FolioComprobante		: {type: "number"},
														suby_NombreTipoInstrumento	: {type: "string"},
														Cliente			: { type: "string"},
														Proposito				: { type: "string"},
														Portafolio: {type:"string"},
													    suby_IdInstrumento : {type: "number"},
														INGMontoInteresCompraAnterior 						: {type: "number"},
														INGMontoInteresCompraAnteriorMonedaInstrumento 		: {type: "number"},
														INGMontoInteresCompraAnteriorMonedaAdicional 		: {type: "number"},
														INGMontoInteresCorridoCompraActual 					: {type: "number"},
														INGMontoInteresCorridoCompraActualMonedaInstrumento : {type: "number"},
														INGMontoInteresCorridoCompraActualMonedaAdicional 	: {type: "number"},
														INGMontoInteresCompraProximo 						: {type: "number"},
														INGMontoInteresCompraProximoMonedaInstrumento 		: {type: "number"},
														INGMontoInteresCompraProximoMonedaAdicional 		: {type: "number"},
														INGMontoInteresMercadoAnterior 						: {type: "number"},
														INGMontoInteresMercadoAnteriorMonedaInstrumento 	: {type: "number"},
														INGMontoInteresMercadoAnteriorMonedaAdicional 		: {type: "number"},
														INGMontoInteresCorridoMercadoActual 				: {type: "number"},
														INGMontoInteresCorridoMercadoActualMonedaInstrumento : {type: "number"},
														INGMontoInteresCorridoMercadoActualMonedaAdicional 	: {type: "number"},
														INGMontoInteresMercadoProximo 						: {type: "number"},
														INGMontoInteresMercadoProximoMonedaInstrumento 		: {type: "number"},
														INGMontoInteresMercadoProximoMonedaAdicional 		: {type: "number"},
														MontoLiquidacion									: {type: "number"},
														Intereses											: {type: "number"},
														DocumentoIdentidad									: {type: "string"},
														NombreLargoCliente									: {type: "string"}
														
														}
													}
												}
					}
		
	});
	 
	$scope.confBasicaSimultaneasHistoricas = {
		dataSource : dsGrillaSimulHis,
		columns				: [  {field: "FolioCartera", title: "UI_FOLIO_CARTERA", width: "90px", align: "right", sortable : false, groupable: true }
								,{field: "NombreEstado", title: "UI_ESTADO", width: "60px", align: "left", sortable : true, groupable: true}
								,{field: "AccionOperacion", title: "UI_TIPO_OPERACION", width: "120px", align: "left", sortable : false, groupable: true } 
								,{field: "FechaInversion", title: "UI_FECHA_INVERSION", width: "125px", align: "center", sortable : false, groupable: true}
								,{field: "FechaVencimientoOperacion", title: "UI_FECHA_VENCIMIENTO", width: "125px", align: "center", sortable : false, groupable: true }
								,{field: "EGRFechaOperacion", title: "UI_FECHA_TERMINO", width: "125px", align: "center", sortable : false }
								,{field: "INGCantidad", title: "UI_CANTIDAD", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "INGPrecioCompraActual", title: "UI_PRECIO", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGPrecioCompraActual,'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "INGTasaCompraActual", title: "UI_TASA", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGTasaCompraActual,'GEN','Tasa',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "MontoLiquidacion", title: "UI_MONTO_LIQUIDACION", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoLiquidacion,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								
								
								//El MontoAPlazo se obtuvo de la factura a plazo y es correcto, NO tomar el INGMontoAPlazo
								,{field: "MontoAPlazo", title: "UI_MONTO_PLAZO", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoAPlazo,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								    
								
								
								,{field: "Intereses", title: "UI_INTERESES", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(Intereses,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
								,{field: "suby_Nemotecnico", title: "UI_NEMOTECNICO", width: "120px", align: "left", sortable : false, groupable: true }
								,{field: "FolioComprobante", title: "UI_FOLIO_COMPROBANTE", width: "90px", align: "right", sortable : false, groupable: true }
								,{field: "suby_NombreTipoInstrumento", title: "UI_TIPO_INSTRUMENTO_ABR", width: "150px", align: "left", sortable : false, groupable: true }
								,{field: "Cliente", title: "UI_CLIENTE", width: "150px", align: "left", sortable : false, groupable: true }
								,{field: "Portafolio", title: "UI_PORTAFOLIO", width: "150px", align: "left", sortable : false, groupable: true }
								,{field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "80px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
								,{field: "NombreLargoCliente", title: "UI_NOMBRE_CLIENTE", width: "180px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
							  ],		
		sortable			: true,
		userSettings		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: true,
		filterable			: true,
		excel 				: {allPages: true},
		height				: 495,
		selectable 			: "multiple row"
	};
	
	//OPCIONES
	$scope.buscar = function()
	{
		var filters = new Array();

		if ($scope.oClientePortafolio.seleccionado.IdCliente)
			filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.oClientePortafolio.seleccionado.IdCliente || 0)});
		if ($scope.oClientePortafolio.seleccionado.IdPortafolio)			
			filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oClientePortafolio.seleccionado.IdPortafolio || 0)});
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

		dsGrillaSimulHis.filter(filters);	
		
	};	
	$scope.limpiar = function()
	{									
		$scope.filtros.IdPortafolio = false;
		$scope.oConfiguracionTipoOperacion.idActual = 0;
		$scope.buscarinstrumento = "-1";
		$scope.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oConfiguracionEstado.idActual = 0;
		
		dsGrillaSimulHis.removeAll();
	};
	
	//TIMEOUT
	/*$scope.select = function(gridArray)
	{
		$.each(gridArray,function(key,value){
			$timeout(function(){
				$("#" + value).find("[kendo-grid]").data("kendo-grid").refresh();}
			,100);
		});
	};*/
});

