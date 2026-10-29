angular
		.module('ModuloNormativa')
		.controller(
				'ConsultarEmisionCircularCtrl',
				function($scope, $http, $timeout, $modal, $translate,
						bcsComunes, bcsBlotterEmisionCircular) {
					$(document).on('click', 'a.hola', function() {
						//alert('Hola');
						// this is critical to stop the click event which will
						// trigger
						// a normal file download!
						return false;
					});


					var leFunction = function(url,nombreArchivo,rutaArchivo) {

																		
							 $http.post(
						  	        url+"file="+rutaArchivo + nombreArchivo,
						  	        {},
						  	        {"responseType": "arraybuffer"}
						  	    ).success(function (data) {  
							  	  	var file = new File([ data ], nombreArchivo);
									saveAs(file);
						  	    	bcsComunes.log().info($translate.instant('UI_ARCHIVO_DESCARGA_EXITO'));
						  	    	
						  	    }).error(function (err){
						  	    	bcsComunes.msg("error","error",$translate.instant('UI_ARCHIVO_DESCARGA_ERROR'));
						  	    });

						// this is critical to stop the click
						// event which will trigger
						// a normal file download!
						return false;

					};
					
					// *** configuracion de objetos ***//
					
					var idVentana = bcsComunes.ventanaWinId($scope);
					bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
					var emiCon 		= this;
					emiCon.filtro = {};
					emiCon.data = {};
					emiCon.data.cmbbx		= {};
					var overlay = bcsComunes.overlay;
					var Accion = '';
					

					emiCon.data.kngFechaDesde = util
							.dateInicioDia($scope.SesionFechaSistema);
					emiCon.data.kngFechaHasta = util
							.dateTerminoDia($scope.SesionFechaSistema);

					emiCon.data.oCircularConfiguracion = {
						tipo : "combobox-tipo-normativa",
						configuracion : {},
						idActual : false,
						seleccionado : false,
						idDefecto : '',
						filtros : {
										IdNormativa : { field: "IdEstado", operator: "eq", value: 'ACT' },
										UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'S' }
									}
					};

					// GRILLA PRINCIPAL
					emiCon.confBasicaEmisionCircular = {
						dataSource : bcsBlotterEmisionCircular
								.dataSourceSolicitudCircular(),
						columns : bcsBlotterEmisionCircular.columnasCircular(),
						pageable : true,
						height : 500,
						selectable : "multiple row",
						filterable : false,
						autoBind	: false,
						groupable			: false,
						userSettings		: true,
						navigatable			: true,
						//allowCopy			: true,
						allowCopy			: { delimeter: "\t" },
						rowColorBackground	: function(row) {		
							switch(row.get("IdEstado")) {
					            case 'ERR':
					                return "red";
					                break;
					            case 'TER':
					            	if (row.NumeroRegistrosLog > 0)
										return "yellow";
					            	else
					                return "green";
					                break;
					             case 'PEN':
					                return "blue";
					                break;    
					            default:
					            	return "white";
					                break;     
							  }	
		  				}
					  
					};
					/*
					emiCon.confBasicaParametros = {
						dataSource : bcsBlotterEmisionCircular
								.dataSourceParametrosSolicitudCircular(),
						columns : bcsBlotterEmisionCircular.columnasDetalleSolicitudCircular(),
						sortable : true,
						pageable : true,
						height : 500,
						selectable : "row",
						filterable : true,
						autoBind	: true
					};
					*/
				emiCon.Parametros = function(dataItem){
					return{
						dataSource: new bcs.DataSource({
							type: "odata",
							definition: {
								serverFiltering : true, // Filtrado de datos en el servidor.
								serverPaging 	: true, // Paginado de los datos en el servidor.
								serverSorting 	: true, // Ordenamiento de los datos en el servidor.
								sortable 		: true,
								pageSize 		: 50,
								transport: {
									read: {
										url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3079",
										dataType: "json"
									}
								},
								filter: [
								    { field: "IdSolicitudCircular", operator: "eq", value: (dataItem && dataItem.IdSolicitudCircular) || -1  }
								    ,{ field: "EsInterno"		  , operator: "eq", value:  'N' }
								],
								schema: {
									model: {
										fields: {
											 IdSolicitudParametros		: { type: "number"},
											 IdSolicitudCircular		: { type: "number"},
											 TipoParametro				: { type: "string"},
											 Valor						: { type: "string"},
											 Nombre						: { type: "string"},
											 EsMasivo					: { type: "string"}
										}
									}
								}
							}
						}),
						columns: [
						    {field: "Nombre",		title: $translate.instant("UI_PARAMETROS"),width: "100px"},
							{field: "Valor",	title: $translate.instant("UI_VALOR"),	width: "200px"}
						],
						hideDetailButton: function(row) {
							var hide = true;
							if(row.TipoParametro === 'RUT_MAS' || row.TipoParametro === 'RUTAUT_MAS' || row.TipoParametro === 'RUTEMPL_MAS' || row.TipoParametro === 'NEMO_MAS' || row.TipoParametro === 'CLAINS_MAS') 
								hide = false;
							return hide;
						},
						sortable	: false,
						pageable	: false,
						autoBind	: true,
						//height		: 500,
						dataBound: function () {},
					    selectable 	: "row",
					    refresh		: true
					}
				};
				emiCon.SubParametros = function(dataItem){
					return{
						dataSource: new bcs.DataSource({
							type: "odata",
							definition: {
								serverFiltering : true, // Filtrado de datos en el servidor.
								serverPaging 	: true, // Paginado de los datos en el servidor.
								serverSorting 	: true, // Ordenamiento de los datos en el servidor.
								sortable 		: true,
								pageSize 		: 50,
								transport: {
									read: {
										url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3080",
										dataType: "json"
									}
								},
								filter: [
								    { field: "IdSolicitudCircular", operator: "eq", value: (dataItem && dataItem.IdSolicitudCircular) || -1  }
								    ,{ field: "TipoParametro"	  , operator: "eq", value: (dataItem && dataItem.TipoParametro) || -1  }
								    
								],
								schema: {
									model: {
										fields: {
											 IdSolicitudParametros		: { type: "number"},
											 IdSolicitudCircular		: { type: "number"},
											 TipoParametro				: { type: "string"},
											 Valor						: { type: "string"},
											 EsInterno					: { type: "string"}
										}
									}
								}
							}
						}),
						columns: [
						    //{field: "Nombre",		title: $translate.instant("UI_PARAMETROS"),width: "100px"},
							{field: "Valor",	title: $translate.instant("UI_VALOR"),	width: "600px"}
						],
						sortable	: false,
						pageable	: false,
						autoBind	: true,
						//height		: 100,
						dataBound: function () {},
					    selectable 	: "row"
					}
				};				
				
				
		
/*
					emiCon.fnTipoDocProp = function() {
						if(emiCon.data.cmbbx.__defineGetter__.length < 1)
						{
							return "S";
						}else{
							return "N";
						};
						
					};
*/
					emiCon.buscarCircular = function() {
						emiCon.EmisionCircularSeleccionado = false;
						var filters = [];

						// Filtro Normativa

						if (emiCon.data.oCircularConfiguracion.seleccionado
								&& emiCon.data.oCircularConfiguracion.seleccionado.IdNormativa !== 0
								&& emiCon.data.oCircularConfiguracion.seleccionado !== undefined
								&& emiCon.data.oCircularConfiguracion.seleccionado.IdNormativa !== ''
								&& emiCon.data.oCircularConfiguracion.seleccionado !== null)
							filters
									.push({
										field : "IdTipoCircular",
										operator : "eq",
										value : emiCon.data.oCircularConfiguracion.seleccionado.IdNormativa
									});

						if (emiCon.data.cmbbx.IdTipoDocumentoObj
								&& emiCon.data.cmbbx.IdTipoDocumentoObj.IdTipoDocumento !== 0
								&& emiCon.data.cmbbx.IdTipoDocumentoObj !== undefined
								&& emiCon.data.cmbbx.IdTipoDocumentoObj.IdNormativa !== ''
								&& emiCon.data.cmbbx.IdTipoDocumentoObj !== null)
							filters
									.push({
										field : "IdTipoDocumento",
										operator : "eq",
										value : emiCon.data.cmbbx.IdTipoDocumentoObj.IdTipoDocumento
									});						
						
						// Filtro Fechas
						if (emiCon.data.kngFechaDesde !== undefined
								&& emiCon.data.kngFechaDesde !== ""
								&& emiCon.data.kngFechaDesde !== null) {

							 var auxDate1 =util.dateOdata(util.date(emiCon.data.kngFechaDesde).hours(0).minutes(0).seconds(0).toDate());
							 var auxDate2 =util.dateOdata(util.date(emiCon.data.kngFechaHasta).add(23,'hours').minutes(59).seconds(59).toDate());
							filters
									.push({
										field : "FechaIngresoSolicitud",
										operator : "gte",
										//value : util.dateInicioDia(emiCon.data.kngFechaDesde)
										value : auxDate1
									});	
						}
						if (emiCon.data.kngFechaHasta) {
							filters
									.push({
										field : "FechaIngresoSolicitud",
										operator : "lte",
										//value : util.dateTerminoDia(emiCon.data.kngFechaHasta)
										value : auxDate2
									});
						}

						//var p = emiCon.gridApi.dataSource.query({ filter: filters});
						var p = emiCon.confBasicaEmisionCircular.dataSource.query({ filter: filters});
						
						p.then(function() {
							$("a.fileDownload").click(function() {
								var data = $("#gridEmisionCircular").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent())
								
								var nombreArchivo =  data.NombreArchivo==null? '' : data.NombreArchivo;
								
								var url="/BSCL/Plataforma/GWJAVA/PLA/download?";
								
								var rutaArchivo = "nor/";
								
								/*var url="/BSCL/Plataforma/GWJAVA/NOR/files/download?schema=BSCL&tipo="+nombreArchivo+"&idDocumento=" + data.IdDocumento + 
								"&idProceso=" + data.IdProceso + "&nombreArchivo="+nombreArchivo;*/
								
								leFunction(url,nombreArchivo,rutaArchivo);

								});
							$("a.fileDownload2").click(function() {
								var data = $("#gridEmisionCircular").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent())
								
								var nombreArchivo =  data.RutaLog==null? '' : data.RutaLog;
								
								var url="/BSCL/Plataforma/GWJAVA/PLA/download?";
								
								var rutaArchivo = "nor/";
								
								/*var url="/BSCL/Plataforma/GWJAVA/NOR/files/download?schema=BSCL&tipo="+nombreArchivo+"&idDocumento=" + data.IdDocumento + 
								"&idProceso=" + data.IdProceso + "&nombreArchivo="+nombreArchivo;*/
								
								leFunction(url,nombreArchivo,rutaArchivo);

								});
						});
						emiCon.EmisionCircularSeleccionado = undefined;
					};
	
				/*********************************************
				 * COMBOBOX
				 *********************************************/
				     
					emiCon.data.cmbbx.TipoDocumento={
						filtros : {
							IdNormativa : { field: "IdNormativa", operator: "eq", value: '-1' },
										UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'S' }
						}
					};		
		
					
					
					emiCon.data.limpiarFiltros = function() {
						emiCon.filtro.IdNormativa = "";
						emiCon.data.oCircularConfiguracion.fechaHasta = "";
						emiCon.data.oCircularConfiguracion.fechaDesde = "";
						emiCon.data.oCircularConfiguracion.idactual = "";
						emiCon.gridApi.dataSource.removeAll();
						emiCon.data.oCircularConfiguracion.seleccionado = false;

					}

					emiCon.crearSolicitud = function() {
						ventanaSolicitudCircular = bcsComunes.ventanaAbrir("IngresarSolicitudCircular", {}, false, false, idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaSolicitudCircular);
				        promesa.then(function(data){
				        	emiCon.buscarCircular();
						});
					};

					emiCon.detalleSolicitud = function(seleccionado) {
						bcsComunes.ventanaAbrir("IngresarSolicitudCircular", {
							SolicitudCircular : seleccionado
						});
					};
		
					emiCon.solicitarCircular = function() {
						var norma=norma=emiCon.data.oCircularConfiguracion;
						if (norma.idactual==0){	norma.idactual='NCG380';};
						
						ventanaSolicitarCircular = bcsComunes.ventanaAbrir("SolicitarCircular", {PrevNormativa :norma,PrevDocumento :emiCon.data.cmbbx}, false, false, idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaSolicitarCircular);
				        promesa.then(function(data){
				        	emiCon.buscarCircular();
						});
						//bcsComunes.ventanaAbrir("SolicitarCircular", {});
						
					};
					
					$scope.$watch('emiCon.data.oCircularConfiguracion.idactual',function(newval,oldval){
					if (!emiCon.data.oCircularConfiguracion)
						{	
						if (!emiCon.data.oCircularConfiguracion.idactual)
						{
							emiCon.data.oCircularConfiguracion.idactual=0;//Setear ID y cascada
							emiCon.data.cmbbx.IdTipoDocumento=0;
							//Setear desde los filtros
							/*
							emiCon.data.cmbbx.TipoDocumento={
										filtros : {
													IdNormativa : { field: "IdNormativa", operator: "eq", value: 0},
													UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'S' }
											}
							}		
									emiCon.data.cmbbx.IdTipoDocumento=-1;
							}else{
								
									emiCon.data.cmbbx.TipoDocumento={
									filtros : {
													IdNormativa : { field: "IdNormativa", operator: "eq", value: emiCon.data.oCircularConfiguracion.idactual },
													UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'S' }
									}
							}
							
							*/
							
						};
					};	
					});
					
				});
