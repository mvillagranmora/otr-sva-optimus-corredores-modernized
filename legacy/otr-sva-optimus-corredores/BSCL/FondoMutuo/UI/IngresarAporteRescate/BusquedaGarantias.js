angular.module('ModuloFFM').controller('ConsultarGarantiasFFMMCtrl',
		function ($rootScope,$scope,$translate,bcsComunes) {

	$scope.data = {};
	$scope.garantias = {};

	var filters = [];
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.oDatosCartera = parametros;
	$scope.data.kngFecha 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	//filtros iniciales
	filters.push({field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'FM'});
	filters.push({field: "IdTipoInstrumento", operator: "eq", value: 'FFM'});
	filters.push({field: "IdPortafolio", operator: "eq", value: Number($scope.data.oDatosCartera.IdPortafolio)});
	filters.push({field: "IdInstrumento", operator: "eq", value: Number($scope.data.oDatosCartera.IdInstrumento)});
	filters.push({field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa)});
	filters.push({
		logic: "or",
		filters : [
		    {				
		    	logic: "and",
				filters : [{ field: "INGFechaLiquidacion", operator: "lte", value: $scope.data.kngFecha },
				           { field: "EGRCantidad", operator: "eq", value: 0 },
				           { field: "IdEstado", operator: "eq", value: 'VIG' }]
			},
		
			{
				logic: "and",
				filters : [{ field: "INGFechaLiquidacion", operator: "lte", value: $scope.data.kngFecha },
				           { field: "EGRCantidad", operator: "neq", value: 0 },
				           { field: "EGRFechaLiquidacion", operator: "gt", value: $scope.data.kngFecha }]
			}
		]
	});

	
	var dsGrillaGarantias = new bcs.DataSource({
	   type : "odata",
	   definition : {
		   serverFiltering 	: true, // Filtrado de datos en el servidor.
		   serverPaging     : true, // Paginado de los datos en el servidor.
		   serverSorting    : true, // Ordenamiento de los datos en el servidor.
		   allowCopy		: { delimeter: "\t" },
		   userSettings 	: true,
		   sort 			: { field: "IdGarantia", dir: "desc" },
		   pageSize         : 25,
		   filter			: filters,
		   transport 		: {read : {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1012",
								dataType : "json"
								}
			},
		   schema 			: { model: {
											fields : {
												IdGarantia								: {type: "number"},
												FechaIngresoRegistro					: {type: "sysdate"},
												FechaLiquidacion						: {type: "sysdate"},
												NombreInstrumento						: {type: "string"},
												Nemotecnico								: {type: "string"},
												INGCantidad								: {type: "number"},
												INGValorRescate							: {type: "number"},
												CantidadGarantia						: {type: "number"},
												INGMontoATasaCompraActual				: {type: "number"},
												INGMontoCompraActualMonedaInstrumento	: {type: "number"},
												MontoValorizado							: {type: "number"},	
												MontoValorizadoMonedaInstrumento		: {type: "number"},
												NombreMonedaInstrumento					: {type: "string"},
												IdOrigenGarantia						: {type: "string"},
												FolioGarantia							: {type: "number"},
												FolioCarteraGarantizada					: {type: "number"},
												IdTipoInstrumento						: {type: "string"},
												NombreTipoInstrumento					: {type: "string"},
												NemoGarantizado         				: {type: "string"},
												RazonSocialContraparte					: {type: "string"},
												NombreConcepto							: {type: "string"},
												INGGlosa								: {type: "string"},
												NombreEstado							: {type: "string"},
												NombreCliente							: {type: "string"},
												DocumentoIdentidad						: {type: "string"},
												IdPortafolio							: {type: "number"},
												CodigoPortafolio 						: {type: "string"},
												IdMonedaInstrumento						: {type: "string"},
												IdTipoOperacion							: {type: "string"},
												NombreOrigenGarantia 					: {type: "string"},
												IdOrigenGarantia 						: {type: "string"},
												INGPrecioGarantia 						: {type: "number"},
												INGTasaGarantia							: {type: "number"},
												FuenteTasaPrecioMercado 				: {type: "string"},
												FechaValorizacionGarantia 				: {type: "sysdate"},												
												ISIN									: {type: "string"},
												MontoInteresCorrido						: {type: "number"},	
												MontoReajusteCorrido					: {type: "number"},
												MontoInteresCorridoMercado				: {type: "number"},
												MontoReajusteCorridoMercado				: {type: "number"},
												DescripcionEspecie 						: {type: "string"},
												IdClasificacionTipoInstrumentoAval 		: {type: "string"},
												EsFungible 								: {type: "string"},
												IdRegimenFondoMutuo						: {type: "string"}
											}
										}
									},

	   }
	});
	
	//GRILLA
	$scope.garantias.confBasicaGarantias = {
		dataSource			: dsGrillaGarantias,
		columns				: [ 
		       				   	 
		       				   	{field: "NombreOrigenGarantia", 					title: "UI_ORIGEN", 						width: "55px",	groupable:true, 	attributes:{style:"white-space:nowrap;"} }
		       				   	,{field: "NombreConcepto", 							title: "UI_CONCEPTO", 						width: "150px",	groupable: true, 	attributes:{style:"white-space:nowrap;"} }
								,{field: "RazonSocialContraparte", 					title: "UI_ENTIDAD_DESTINO", 				width: "150px",	groupable: true, 	attributes:{style:"white-space:nowrap;"} }
								,{field: "IdClasificacionTipoInstrumentoAval", 		title: "UI_MERCADO", 						width: "60px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
								,{field: "IdTipoInstrumento", 						title: "UI_TIPO_INSTRUMENTO", 				width: "80px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
								,{field: "Nemotecnico", 							title: "UI_INSTRUMENTO", 					width: "90px",	groupable: true }
								,{field: "EsFungible", 								title: "UI_FUNGIBLE", 						width: "50px",	groupable: true }
								,{field: "ISIN", 									title: 'UI_ISIN', 							width: "80px", 	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}	    		
								,{field: "CantidadGarantia", 						title: "UI_CANTIDAD_VALOR_RESCATE", 		width: "80px",	template : " <label> #:  (util.bcs_formato(CantidadGarantia,'GEN','Cantidad','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
								,{field: "IdMonedaInstrumento", 					title: "UI_MONEDA", 						width: "55px",	groupable:true }
								,{field: "MontoValorizadoMonedaInstrumento", 		title: "UI_VALOR_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizadoMonedaInstrumento,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
								,{field: "MontoValorizado", 						title: "UI_MONTO_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizado,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>"}								
								
								,{field: "MontoInteresCorrido",						title: 'UI_INTERESES_CORRIDOS', 			width: "105px",	hidden: true,		template : " <label> #:  (util.bcs_formato(MontoInteresCorrido,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoReajusteCorrido", 					title: 'UI_REAJUSTES_VAC_CORRIDO', 			width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(MontoReajusteCorrido,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}								
								,{field: "NemoGarantizado", 						title: "UI_OPERACION", 						width: "150px",	groupable: true}
								,{field: "FechaValorizacionGarantia", 				title: "UI_FECHA_VALORIZACION",				width: "80px",	groupable: true}
								,{field: "FuenteTasaPrecioMercado", 				title: "UI_FUENTE_PRECIO", 					width: "80px",	groupable: true}
								,{field: "FolioCarteraGarantizada", 				title: "UI_FOLIO_OPERACION_RELACIONADA",	width: "80px",	groupable: true,	format: "{0:n0}"}
								,{field: "FolioGarantia", 							title: "UI_SECUENCIA_GARANTIA",				width: "70px",	format: "{0:n0}"}
								,{field: "DescripcionEspecie", 						title: "UI_ESPECIE",						width: "70px",	groupable: true}
								,{field: "IdRegimenFondoMutuo", 					title: "UI_REGIMEN",						width: "70px",	groupable: true}
							  ],		
		sortable			: true,
		pageable			: true,
		autoBind			: true,		
		filterable			: true,
		userSettings 		: true,
		height				: 520,
		selectable 			: "row",
		gridCreated 		: function(grid) {
			$scope.grillaAll = grid;
	    }
	};
		
	$scope.data.fnDobleClick = function(datos) {
		bcsComunes.ventanaData(idVentana,datos);
		bcsComunes.ventanaCerrar(idVentana);
	};


});