(function() { 
	angular.module('ModuloOrdenes').controller('ConsultaAnticiposReportesCtrl',
	function($rootScope,$scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma)
	{	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var ConAntReportes = this; //controller as object
		ConAntReportes.kngFechaInicio 	= util.dateInicioDia($scope.SesionFechaSistema);
		ConAntReportes.fechaInicioOpt 	= {max:util.dateInicioDia($scope.SesionFechaSistema)};
		ConAntReportes.kngFechaTermino 	= util.dateInicioDia($scope.SesionFechaSistema);;
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		ConAntReportes.oConfiguracionSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
		ConAntReportes.oConfiguracionPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};
		
		ConAntReportes.oConfiguracionTipoOperacion={
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
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsGrillaReporteAntic = new bcs.DataSource({
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
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1057",
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
							suby_IdInstrumento			: {type: "number"},
							suby_Nemotecnico			: {type: "string"},
							FolioComprobante			: {type: "number"},
							suby_NombreTipoInstrumento	: {type: "string"},
							Cliente						: { type: "string"},
							Proposito					: { type: "string"},
							
							EGRCantidad					: {type: "number"}, // CANTIDAD ANTICIPADA
							EGRMonto					: {type: "number"}, // MONTO ANTICIPADO
							EGRMontoEnMonedaInstrumento	: {type: "number"}, // MONTO EN MONEDA INSTRUMENTO
							EGRMontoAPlazo				: {type: "number"}, // MONTO A PLAZO
							EGRPrecioAPlazo				: {type: "number"}, // EGRESO A PLAZO
							}
						}
					},
			}
		});
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		ConAntReportes.confBasicaAnticipos = {
			dataSource 	: dsGrillaReporteAntic,
				columns	: [  {field: "FolioCartera", 				title: "UI_FOLIO_CARTERA", 				width: "90px",	align: "right",	sortable : false, groupable: true }
							,{field: "EGRFechaOperacion", 			title: "UI_FECHA_ANTICIPO",				width: "125px", align: "center",sortable : false, groupable: true }
							,{field: "suby_Nemotecnico", 			title: "UI_NEMOTECNICO", 				width: "120px", align: "left", 	sortable : false, groupable: true } //rev
							,{field: "INGCantidad", 				title: "UI_CANTIDAD_ORIGINAL",			width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,					'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "EGRCantidad", 				title: "UI_CANTIDAD_ANTICIPADA",		width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRCantidad,					'GEN','Cantidad',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "EGRMonto", 					title: "UI_MONTO_ANTICIPADO",	 		width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(EGRMonto,						'GEN','Monto',IdMonedaSistema,IdTipoInstrumentoOperacion,IdTipoOperacion)).valor #</label>" }
							,{field: "Cliente", 					title: "UI_CLIENTE", 					width: "300px", align: "left", 	sortable : false, groupable: true }
							,{field: "Proposito", 					title: "UI_PORTAFOLIO",					width: "150px", align: "left", 	sortable : false, groupable: true }
						],		
			sortable	: true,
			pageable	: true,
			userSettings: true,
			allowCopy	: { delimeter: "\t" },
			filterable	: true,
			height		: 500,
			selectable 	: "multiple row"
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		ConAntReportes.buscar = function()
		{
			var filters = new Array();
			
			if (ConAntReportes.oConfiguracionSocioNegocio.seleccionado.IdCliente)
				filters.push({ field: "IdCliente", operator: "eq", value: Number(ConAntReportes.oConfiguracionSocioNegocio.seleccionado.IdCliente || 0)});
			if (ConAntReportes.oConfiguracionPortafolio.seleccionado.IdPortafolio)			
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number(ConAntReportes.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0)});
			if ( ConAntReportes.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion)
				filters.push( { "field": "IdTipoOperacion","operator": "eq", "value" :  ConAntReportes.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion});
			if (ConAntReportes.instrumento && ConAntReportes.instrumento.IdInstrumento)
				filters.push( { "field": "IdInstrumento_suby","operator": "eq", "value" : ConAntReportes.instrumento.IdInstrumento});
			if (ConAntReportes.kngFechaInicio)
				filters.push( { "field": "EGRFechaOperacion","operator": "gte", "value" : util.dateOdata(ConAntReportes.kngFechaInicio)});
			if (ConAntReportes.kngFechaTermino)
				filters.push( { "field": "EGRFechaOperacion","operator": "lte", "value" : util.dateOdata(util.dateTerminoDia(ConAntReportes.kngFechaTermino))});
			dsGrillaReporteAntic.filter(filters);	
		};	
		
		ConAntReportes.limpiar = function()
		{									
			ConAntReportes.oConfiguracionPortafolio.idActual = 0;
			ConAntReportes.oConfiguracionSocioNegocio.idActual = 0;
			ConAntReportes.oConfiguracionTipoOperacion.idActual = 0;
			ConAntReportes.buscarinstrumento = "-1";
			ConAntReportes.kngFechaInicio = util.dateInicioDia($scope.SesionFechaSistema);
			ConAntReportes.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaReporteAntic.removeAll();
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------