/*************************************************************
 * Purpose	: Consulta de Anticipos de Simultánea.
 * Author	: XX - BCS
 * Date		: 05/07/2016
 *************************************************************/
(function() { // IIFE
	angular.module('ModuloCustodia').controller('ConsultaAnticiposCtrl',
	function($rootScope,$scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma)
	{	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var ConAntSIM = this; //controller as object
		ConAntSIM.kngFechaInicio 	= util.dateInicioDia($scope.SesionFechaSistema);
		ConAntSIM.fechaInicioOpt 	= {max:util.dateInicioDia($scope.SesionFechaSistema)};
		ConAntSIM.kngFechaTermino 	= util.dateInicioDia($scope.SesionFechaSistema);;
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		ConAntSIM.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
	
		ConAntSIM.oConfiguracionTipoOperacion={
			tipo : "combobox-tipo-operacion",
			configuracion : {},
			filtros : {
				EstadoTipoOperacionTipoInstrumento : { "field"   : "EstadoTipoOperacionTipoInstrumento","operator": "eq", "value" : "ACT" },
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "SIM"},
				cpa 			: { "field":"IdTipoOperacion", "operator":"neq", "value":"COMSIM" },
				vta 			: { "field":"IdTipoOperacion", "operator":"neq", "value":"VENSIM" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsGrillaSimulAntic = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering : true,
				serverPaging 	: true,
				userSettings 	: true,
				allowCopy		: { delimeter: "\t" },
				serverSorting 	: true,
				sort 			: { field: "FolioCartera", dir: "desc" },
				pageSize 		: 50,
				transport 		: {
					read : {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1375",
							dataType : "json" 
					}
				},
				schema : { 
					model: {
						fields : {	
							FolioCartera				: {type: "number"}, // CARTERA ANTICIPADA
							AccionOperacion				: {type: "string"},
							FechaInversion				: {type: "sysdate"},
							FechaVencimientoOperacion	: {type: "sysdate"},
							EGRFechaOperacion			: {type: "sysdate"}, //FECHA DEL ANTICIPO
							INGCantidad					: {type: "number"},
//							INGPrecioCompraActual		: {type: "number"},
//							INGTasaCompraActual			: {type: "number"},
//							INGMontoATasaCompraActual	: {type: "number"},
//							INGMontoAPlazo				: {type: "number"},
							suby_IdInstrumento			: {type: "number"},
							suby_Nemotecnico			: {type: "string"},
							FolioComprobante			: {type: "number"},
							suby_NombreTipoInstrumento	: {type: "string"},
							Cliente						: { type: "string"},
							CodigoPortafolio					: { type: "string"},
							
							EGRCantidad					: {type: "number"}, // CANTIDAD ANTICIPADA
							EGRMonto					: {type: "number"}, // MONTO ANTICIPADO
							EGRMontoEnMonedaInstrumento	: {type: "number"}, // MONTO EN MONEDA INSTRUMENTO
							EGRMontoAPlazo				: {type: "number"}, // MONTO A PLAZO
							EGRPrecioAPlazo				: {type: "number"}, // EGRESO A PLAZO
							DocumentoIdentidadDefecto	: {type: "string"},
							NombreLargoCliente			: {type: "string"}
							}
						}
					},
/*				filters: [
				    { field: "IdEstado", operator: "eq", value: 'ANT' }
				],
*/
			}
		});
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		ConAntSIM.confBasicaAnticipos = {
			dataSource 	: dsGrillaSimulAntic,
				columns	: [  {field: "FolioCartera", 				title: "UI_FOLIO_CARTERA", 				width: "90px",	align: "right",	sortable : false, groupable: true }
							,{field: "EGRFechaOperacion", 			title: "UI_FECHA_ANTICIPO",				width: "125px", align: "center",sortable : false, groupable: true }
							,{field: "suby_Nemotecnico", 			title: "UI_NEMOTECNICO", 				width: "120px", align: "left", 	sortable : false, groupable: true } //rev
							,{field: "INGCantidad", 				title: "UI_CANTIDAD_ORIGINAL",			width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,					'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "EGRCantidad", 				title: "UI_CANTIDAD_ANTICIPADA",		width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRCantidad,					'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "EGRMontoARecibirAnticipo", 	title: "UI_MONTO_ANTICIPADO",	 		width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRMontoARecibirAnticipo,		'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							//,{field: "EGRMontoEnMonedaInstrumento",	title: "UI_MONTO_MONEDA_INSTRUMENTO",	width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRMontoEnMonedaInstrumento,	'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
//							,{field: "EGRMontoAPlazo", 				title: "UI_CANTIDAD",	 				width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRMontoAPlazo,				'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
//							,{field: "EGRPrecioAPlazo", 			title: "UI_PRECIO_A_PLAZO",	 			width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRPrecioAPlazo,				'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "Cliente", 					title: "UI_CLIENTE", 					width: "300px", align: "left", 	sortable : false, groupable: true }
							,{field: "CodigoPortafolio", 					title: "UI_PORTAFOLIO",					width: "150px", align: "left", 	sortable : false, groupable: true }
							,{field: "DocumentoIdentidadDefecto",	title: "UI_DOCUMENTO_IDENTIDAD", 		width: "80px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
							,{field: "NombreLargoCliente", 			title: "UI_NOMBRE_CLIENTE", 			width: "180px", align: "left", sortable : true, groupable: true, attributes: {style:"text-align:left; white-space:nowrap"} }
							
							/*,{field: "AccionOperacion", 			title: "UI_TIPO_OPERACION", 		width: "120px",	align: "left",	sortable : false } 
							,{field: "FechaInversion", 				title: "UI_FECHA_INVERSION", 		width: "125px",	align: "center",sortable : false}
							,{field: "FechaVencimientoOperacion", 	title: "UI_FECHA_VENCIMIENTO", 		width: "125px", align: "center",sortable : false }
							,{field: "EGRFechaOperacion", 			title: "UI_FECHA_TERMINO", 			width: "125px", align: "center",sortable : false }
							,{field: "INGCantidad", 				title: "UI_CANTIDAD",	 			width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "INGPrecioCompraActual", 		title: "UI_PRECIO", 				width: "100px", align: "right",	sortable : false, template : " <label> #:  (util.bcs_formato(INGPrecioCompraActual,'GEN','Precio',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "INGTasaCompraActual", 		title: "UI_TASA", 					width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGTasaCompraActual,'GEN','Tasa',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "INGMontoATasaCompraActual", 	title: "UI_MONTO", 					width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "INGMontoAPlazo", 				title: "UI_MONTO_PLAZO", 			width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGMontoAPlazo,'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "suby_Nemotecnico", 			title: "UI_NEMOTECNICO", 			width: "120px", align: "left", 	sortable : false }
							,{field: "FolioComprobante", 			title: "UI_FOLIO_COMPROBANTE", 		width: "90px",	align: "right", sortable : false }
							,{field: "suby_NombreTipoInstrumento", 	title: "UI_TIPO_INSTRUMENTO_ABR", 	width: "150px", align: "left", 	sortable : false }*/
							
						],		
			sortable	: true,
			pageable	: true,
			userSettings: true,
			allowCopy	: { delimeter: "\t" },
			filterable	: true,
			height		: 495,
			selectable 	: "multiple row"
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		ConAntSIM.buscar = function()
		{
			var filters = new Array();
			//filters.push({field: "IdEstado", operator: "eq", value: 'ANT' });
			if (ConAntSIM.oClientePortafolio.seleccionado.IdCliente)
				filters.push({ field: "IdCliente", operator: "eq", value: Number(ConAntSIM.oClientePortafolio.seleccionado.IdCliente || 0)});
			if (ConAntSIM.oClientePortafolio.seleccionado.IdPortafolio)			
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number(ConAntSIM.oClientePortafolio.seleccionado.IdPortafolio || 0)});
			if ( ConAntSIM.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion)
				filters.push( { "field": "EGRIdTipoOperacion","operator": "eq", "value" :  ConAntSIM.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion});
			if (ConAntSIM.instrumento) {
				if (ConAntSIM.instrumento.IdInstrumento)
					filters.push( { "field": "suby_IdInstrumento","operator": "eq", "value" : ConAntSIM.instrumento.IdInstrumento});				
			}

			if (ConAntSIM.kngFechaInicio)
				filters.push( { "field": "EGRFechaOperacion","operator": "gte", "value" : util.dateSPdate(ConAntSIM.kngFechaInicio)});
			if (ConAntSIM.kngFechaTermino)
				filters.push( { "field": "EGRFechaOperacion","operator": "lte", "value" : util.dateSPdate(ConAntSIM.kngFechaTermino)});
			dsGrillaSimulAntic.filter(filters);	
		};	
		
		ConAntSIM.limpiar = function()
		{									
			$scope.filtros.IdPortafolio = false;
			ConAntSIM.oConfiguracionTipoOperacion.idActual = 0;
			ConAntSIM.buscarinstrumento = "-1";
			ConAntSIM.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
			ConAntSIM.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaSimulAntic.removeAll();
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------