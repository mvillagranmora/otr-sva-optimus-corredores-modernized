(function() {
	angular.module('ModuloMesa').controller('InformeMovimientosDiariosCtrl',
	function($scope,$translate,bcsComunes,$http){
        /*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var iMD 		= this;
		iMD.idVentana	= bcsComunes.ventanaWinId($scope);
		iMD.win			= {};
		iMD.grilla		= {};
		iMD.cmbbx		= {};
		iMD.filtro		= {};
		iMD.fn			= {};

		iMD.filtro.Fecha = util.dateInicioDia($scope.SesionFechaSistema);
		iMD.dataTemplate	= {};
		iMD.parametros		= {};
		iMD.activarTipoCompromiso = true;

		/*********************************************
		 * Filtros
		 *********************************************/
		iMD.configuracionMercado = {
			filtros : {
				logic : "or",
					Intermediacion : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
					RentaFija : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
			}
		};		

		iMD.configuracionTipoCompromiso = {
			filtros: {
				IdTipoProducto : {"field": "IdTipoProducto","operator": "eq", "value"   :  'MERF' },
				EsAnticipo : {"field": "EsAnticipo","operator": "eq", "value"   :  'N' },
				EsAnulacion : {"field": "EsAnulacion","operator": "eq", "value"   :  'N' },
				Accion : {"field": "Accion","operator": "eq", "value"   :  '' }
			}
		};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsOperaciones = {
			type: "odata",
			definition: {
				serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize: 100,
                transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=525",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							Folio: {type:"number"},
							Rut : { type: "string"},
							Nombre : { type: "string"},
							Cuenta : { type: "string"},
							Nemotecnico : { type: "string"},
							Emisor : { type: "string"},
							Negociacion : { type: "string"},
							TipoCompromiso : { type: "string"},
							CodigoMovimiento : { type: "string"},
							Cantidad : { type: "number"},
							MO : { type: "string"},
							Tasa : { type: "number"},
							TAgente : { type: "number"},
							FechaVencimientoInstrumento : { type: "sysdate"},
							FechaVencimientoCompromiso : { type: "sysdate"},
							MontoOperacion : { type: "number"},
							Spread : { type: "number"},
							FolioComprobanteAdjunto : { type: "number"},
							FechaConsulta : { type: "sysdate"},
						}
					}
				}
			}
		};

		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
						dataType : "json",
					}
				},
				schema : {
					model : {
						fields : {
							IdTemplateMesa	: {type: "string"},
							IdReporte		: {type: "string"},
							NombreReporte	: {type: "string"},
							TipoReporte		: {type: "string"},
							ServicioReporte	: {type: "string"},
							IdEmpresa		: {type: "number"},
							IdEstado		: {type: "string"}
						}
					}
				}
			},
		});

		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		var dsGrillaOperaciones = new bcs.DataSource(dsOperaciones);
		iMD.grilla.MovimientosDiarios = {
			dataSource: dsGrillaOperaciones,
			columns: [
				{field: "Folio", title: $translate.instant('UI_FOLIO'), width: "80px"},
				{field: "Rut", title: $translate.instant('UI_RUT_CLIENTE'), width: "80px"},
				{field: "Nombre", title: $translate.instant('UI_NOMBRE_CLIENTE'), width: "160px"},
				{field: "Cuenta", title: $translate.instant('UI_CUENTA'), width: "100px"},
				{field: "Nemotecnico", title: $translate.instant('UI_NEMOTECNICO'), width: "100px"},
				{field: "Emisor", title: $translate.instant('UI_EMISOR'), width: "100px"},
				{field: "Negociacion", title: $translate.instant('UI_NEGOCIACION'), width: "80px"},
				{field: "TipoCompromiso", title: $translate.instant('UI_TIPO_COMPROMISO'), width: "100px"},				
				{field: "CodigoMovimiento", title: $translate.instant('UI_CODIGO_MOVIMIENTO'), width: "100px"},
				{field: "Cantidad", title: $translate.instant('UI_CANTIDAD'), width: "100px", 	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',MO,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" },	
				{field: "MO", title: $translate.instant('UI_MONEDA'), width: "60px"},
				{field: "Tasa", title: $translate.instant('UI_TASA'), width: "100px", template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',MO,IdTipoInstrumento,'')).valor #</label>"},
				{field: "TAgente", title: $translate.instant('UI_TASA_AGENTE'), width: "100px"},
				{field: "FechaVencimientoInstrumento", title: $translate.instant('UI_FECHA_VENCIMIENTO_INSTRUMENTO'), width: "100px"},
				{field: "FechaVencimientoCompromiso", title: $translate.instant('UI_FECHA_VENCIMIENTO_COMPROMISO'), width: "100px"},
				{field: "MontoOperacion", title: $translate.instant('UI_MONTO_OPERACION'), width: "100px", template : " <label> #:  (util.bcs_formato(MontoOperacion,'GEN','Monto',MO,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
				{field: "Spread", title: $translate.instant('UI_SPREAD'), width: "100px",template : " <label> #:  (util.bcs_formato(Spread,'GEN','Monto',MO,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" },
				{field: "FolioComprobanteAdjunto", title: $translate.instant('UI_FOLIO_COMPROBANTE_ADJUNTO'), width: "100px"},
			],
			sortable		: true,
			pageable		: true,
			filterable		: true,
			navigatable 	: true,
			userSettings 	: true,
			height			: 500,
			excel 			: { allPages: true },
			scrollable		: true,
			selectable 		: "row",
		    autoBind 		: false
		  
		};

		iMD.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;

			//niv1filters.push({ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(iMD.filtro.Fecha){
				niv1filters.push({
		              logic: "or",
		              filters: [
		                { field: "FechaConsulta", operator: "eq", value: util.dateInicioDia(iMD.filtro.Fecha) },
		                { field: "FechaOperacion", operator: "eq", value: util.dateInicioDia(iMD.filtro.Fecha) }
		              ]
		            });
			}
			if(iMD.configuracionNegociacion.idActual != 0) {
				niv1filters.push({ field: "EsFueraRueda", operator: "eq", value: iMD.configuracionNegociacion.idActual});
			}			
			if(iMD.configuracionTipoCompromiso.idActual != 0) {
				niv1filters.push({ field: "TipoCompromiso", operator: "eq", value: iMD.configuracionTipoCompromiso.idActual});
			}
			if(iMD.configuracionMercado.idActual != 0) {
				niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: iMD.configuracionMercado.idActual});
			}
			if(niv1filters.length>0) {
				dsGrillaOperaciones.query({filter: objfilters}).then(function(){
					if(dsGrillaOperaciones.view().length > 0) {
						iMD.win.existenOperaciones 	= true;
					} else {
						iMD.win.existenOperaciones = false;
					}
				});

				//iMD.grilla.MovimientosDiarios.dataSource.filters(objfilters);

				
				
			} else {
				iMD.grilla.MovimientosDiarios.dataSource.filters(new Array());
			}
			
			
			iMD.grilla.MovimientosDiariosSel= undefined;
			
			
		};

		iMD.fn.limpiar = function(){
			iMD.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			iMD.configuracionMercado.idActual = 0;
			iMD.configuracionTipoCompromiso.idActual = 0;
			iMD.grilla.MovimientosDiarios.dataSource.removeAll();
			iMD.win.existenOperaciones = false;
		};

		iMD.fn.generarInforme = function() {
			console.log("Generar Informe");
			/*
			iODP.dataTemplate.IdTemplateMesa = 'MOVIMIENTOSDIARIOS';
			var filterTemplate = [ { field: "IdTemplateMesa",operator:"eq", value: iODP.dataTemplate.IdTemplateMesa },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				if(ConsultaTemplate.view().length > 0) {
					iMD.dataTemplate.TipoReporte	= ConsultaTemplate.view()[0].TipoReporte;
					iMD.dataTemplate.IdReporte		= ConsultaTemplate.view()[0].IdReporte;
					iMD.dataTemplate.NombreReporte	= ConsultaTemplate.view()[0].NombreReporte;
					iMD.dataTemplate.Descripcion	= ConsultaTemplate.view()[0].Descripcion;
					
					var parametros = { "p_IdEmpresa"		: $scope.$root.SesionIdEmpresa,
									   "p_IdUsuario"		: $scope.$root.SesionIdUsuario,
									   //"p_IdModulo"			: 'MRF',
									   //"p_FechaDesde"		: iODP.parametros.Fecha,
									   //"p_FechaHasta"		: iODP.parametros.FechaHasta,
									   //"p_IdMesa"			: iODP.parametros.IdMesa,
									   //"p_IdTipoProducto"	: iODP.parametros.IdTipoProducto,
									   //"p_OrdenarPor"		: iODP.parametros.OrdenarPor,
									   "$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]};
					iMD.dataTemplate.data = { "serviceId" : ConsultaTemplate.view()[0].ServicioReporte, "serviceData" : parametros };
					
					iMD.fn.getReport(iMD.dataTemplate);
				}
			});
			*/
		};

		iMD.fn.getReport = function(dataTemplate) {
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.IdReporte,
				"parameters"	: {},
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.NombreReporte,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": []
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					saveAs(file, dataTemplate.Descripcion+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_INFORME"));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
		};

	});
}());
