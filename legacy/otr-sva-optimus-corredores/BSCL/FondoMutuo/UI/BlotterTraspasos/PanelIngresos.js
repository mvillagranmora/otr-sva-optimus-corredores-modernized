(function() {
	angular.module('ModuloFFM').controller('PanelIngresosCtrl',
	function($rootScope,$scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServiciosCuentaInversion,
				 bcsAlerta, OPCIONES_BCSALERTA,$http,bcsBlotterComprobantesGrid){

		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pti 		= this;
		pti.idVentana	= bcsComunes.ventanaWinId($scope);
		pti.win		= {};
		pti.grilla		= {};
		pti.cmbbx		= {};
		pti.filtro		= {};
		pti.fn			= {};
		
		pti.filtro.IdClasificacionTipoInstrumentoDef	= 0;
			
		pti.salidaMensaje='';
		pti.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pti.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.dataTemplate	= {};
		var filters = {};
		var auxDate1 =util.dateOdata(util.date(pti.FecInicio).hours(0).minutes(0).seconds(0).toDate());
		var auxDate2 =util.dateOdata(util.date(pti.FecInicio).add(23,'hours').minutes(59).seconds(59).toDate());
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var defTraspasos = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 25,
	                sort			: {field: "IdTraspaso", dir: "desc" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300014",
							dataType: "json"
						}
					},
					filter: [
					    { field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa },
					    { field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAING' },
					    { field: "FechaIngreso ", operator: "gte", value: auxDate1 },
					    { field: "FechaIngreso ", operator: "lte", value: auxDate2 }				    
					],
					schema: {
						model: {
							fields: {
								IdTraspaso					: 	{ type: "number"},
								FolioTraspaso				: 	{ type: "number"},
								IdMotivoTraspaso         	:	{ type: "string"},
								IdTipoTraspaso				:	{ type: "string"},
								Glosa						:	{ type: "string"},
								FechaHoraSuscripcion		:	{ type: "string"},
								IdEstado					:	{ type: "string"},
								IntMontoTraspaso			: 	{ type: "number"},
								Monto						: 	{ type: "number"},
								FechaIngreso				:	{ type: "date"},
								CodigoPortafolio			:	{ type: "string"},
								RazonSocial					:	{ type: "string"},
		        				NombreCompleto				:	{ type: "string"},
		        				DocumentoIdentidad			:	{ type: "string"},
		        				IdPersona					: 	{ type: "number"},
		        				EstadoTraspaso				:	{ type: "string"},
		        				NombreCortoEmisor			:	{ type: "string"},
		        				DocumentoIdentidadEmisor	:	{ type: "string"},
		        				MotivoTraspaso				:	{ type: "string"},
		        				TipoMovimientoTraspaso		:	{ type: "string"},
		        				TipoTraspaso				:	{ type: "string"},
		        				CodigoUsuario				:	{ type: "string"},
		        				DiasTranscurridos			: 	{ type: "number"},
		        			    FolioMLT					: 	{ type: "number"},
		        				FolioCSC					: 	{ type: "number"},
		        				NombreCompletoEjecutivo		:	{ type: "string"},
		        				CodigoEjecutivo				:	{ type: "string"},
		        				IdSucursal					: 	{ type: "number"},
		        				NombreSucursal				:	{ type: "string"},
		        				CodigoSucursal				:	{ type: "string"},
		        				NombreEntidadRecaudadora	:	{ type: "string"},
		        				DocumentoIdentidadEntidadRecaudadora	:	{ type: "string"},
		        				IdMonedaFondo				:	{ type: "string"}
					
							}
						}
					}
				}	
			};
		
		var dsTraspasos = new bcs.DataSource(defTraspasos);
		pti.Traspasos = {
				dataSource: dsTraspasos,
				columns: [
						{field: "FolioTraspaso",			title: $translate.instant('UI_FOLIO_TRASPASO'),					width: "80px", align: "right", editable: false},
						{field: "FechaIngreso",				title: $translate.instant('UI_FECHA_INGRESO'),					width: "80px", align: "right", editable: false},
						
						{field: "DocumentoIdentidad",		title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),			width: "80px", align: "left"},
						{field: "NombreCompleto",			title: $translate.instant('UI_CLIENTE'),						width: "140px", align: "left"},
						{field: "CodigoPortafolio",			title: $translate.instant('UI_PORTAFOLIO'),						width: "100px", align: "left"},
						{field: "EstadoTraspaso",			title: $translate.instant('UI_ESTADO'),							width: "60px", align: "left"},
						{field: "IdTraspaso",				title: $translate.instant('UI_SECUENCIA'),						width: "60px", align: "right", editable: false},
						{field: "NombreEntidadRecaudadora",	title: $translate.instant('UI_ADMINISTRADORA_ORIGEN'),			width: "100px", align: "left"},
						{field: "DocumentoIdentidadEntidadRecaudadora",	title: $translate.instant('UI_DOCUMENTO_IDENTIDAD_EMISOR'),		width: "100px", align: "left"},
						{field: "TipoMovimientoTraspaso",	title: $translate.instant('UI_TIPO_MOVIMIENTO'),				width: "100px", align: "left"},
						{field: "TipoTraspaso",				title: $translate.instant('UI_TIPO_TRASPASO'),					width: "90px", align: "left"},
						{field: "MotivoTraspaso",			title: $translate.instant('UI_MOTIVO_TRASPASO'),				width: "100px", align: "left"},
						{field: "FolioMLT",					title: $translate.instant('UI_FOLIO_MLT'),						width: "60px", align: "right"},
						{field: "FolioCSC",					title: $translate.instant('UI_FOLIO_CSC'),						width: "60px", align: "right"},
						{field: "Monto",					title: $translate.instant('UI_MONTO'),							width: "60px", align: "right", template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMonedaFondo, '','')).valor #</label>"},
						
						{field: "NombreCompletoEjecutivo",	title: $translate.instant('UI_EJECUTIVO'),						width: "140px", align: "left"},
						{field: "CodigoEjecutivo",			title: $translate.instant('UI_CODIGO_EJECUTIVO'),				width: "60px", align: "left"},
						{field: "NombreSucursal",			title: $translate.instant('UI_SUCURSAL'),						width: "90px", align: "left"},
						
						{field: "CodigoUsuario",			title: $translate.instant('UI_USUARIO'),						width: "80px", align: "left"},
						{field: "IdMonedaFondo",			title: $translate.instant('UI_MONEDA'),							width: "60px", align: "left"},
					    ],	
				sortable		: true,
				filterable		: true,

				pageable: {
                    refresh: true,
                    //pageSizes: true,
                    buttonCount: 5
                },
                
				autoBind		: true,
				height			: 460,
				selectable		: "row",
			    userSettings	: true,
				navigatable		: true,
				gridCreated     : function(g){
					pti.ApiTraspasos = g;
				},
				rowColorBackground	: function(row) {		
					switch(row.get("IdEstado")) {
			             case 'ANU':
			                return "red";
			                break;
			              case 'VIG':
			                return "green";
			                break;
			            default:
			            	return "white";
			                break;     
					  }	
					},
				excel : {
					allPages: true,
				},
				allowCopy: true
		
		};
				
	
		pti.Origen = function(dataItem){
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300015",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdTraspaso"			, operator: "eq", value: (dataItem && dataItem.IdTraspaso) || -1  }
						   ,{ field: "IdEstado " 			, operator: "eq", value: 'ACT' }
						   ,{ field: "IdRegimenTributario"  , operator: "eq", value: '18Q'}
						],
						schema: {
							model: {
								fields: {
									
									IdDetalleTraspaso			: 	{ type: "number"},
									IdTraspaso					: 	{ type: "number"},
									IdSerieFondo				:	{ type: "string"},
									Cantidad					: 	{ type: "number"},
									IdEstado					:	{ type: "string"},
									FechaIngreso				:	{ type: "date"},
								    FechaModificacion			:	{ type: "date"},
								    FechaIngresoRegistro		:	{ type: "date"},
								    FechaModificacionRegistro	:	{ type: "date"},
									Estado						:	{ type: "string"},
									NombreSerieFondo			:	{ type: "string"},
									CodigoSerie					:	{ type: "string"},
									NombreCortoEmisor			:	{ type: "string"},
									NombreCompletoEmisor		:	{ type: "string"},
									DocumentoIdentidadEmisor	:	{ type: "string"},
									RegimenTributario			:	{ type: "string"}
									
								}
							}
						}
					}
				}),
				columns: [
				    {field: "IdSerieFondo",					title: $translate.instant("UI_INSTRUMENTO")								,width: "80px"},
				    {field: "RegimenTributario",			title: $translate.instant("UI_REGIMEN_TRIBUTARIO")						,width: "100px"},
				    {field: "NombreCompletoEmisor",			title: $translate.instant("UI_ADMINISTRADORA_ORIGEN")					,width: "100px"},
					{field: "DocumentoIdentidadEmisor",		title: $translate.instant("UI_DOCUMENTO_IDENTIDAD_EMISOR")				,width: "100px"}
					
				    
			    
				],
				sortable	: false,
				pageable	: false,
				autoBind	: true,
			    selectable 	: "row",
			    rowCellColor : function(row) {
			    	var column = new Array();
			    	if (row.get("EstadoSolicitud")=='CRT'){column.push({column: "NombreEstadoSolicitud", color: "green"});};
			    	if (row.get("IdEstadoCargoAbono")=='CRT'){column.push({column: "NombreEstadoCargoAbono", color: "green"});};
			    	if (row.get("EstadoSolicitud")=='ANU'){column.push({column: "NombreEstadoSolicitud", color: "red"});};
			    	if (row.get("IdEstadoCargoAbono")=='ANU'){column.push({column: "NombreEstadoCargoAbono", color: "red"});};
			    	return column;

			    }
			
			}
		};
		
		pti.Destino = function(dataItem){
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300016",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdTraspaso", 				operator: "eq", value: (dataItem && dataItem.IdTraspaso) || -1  }
						   ,{ field: "IdEstado " , 				operator: "eq", value:  'ACT' }
						],
						schema: {
							model: {
								fields: {
									
									IdDetalleTraspaso			: 	{ type: "number"},
									IdTraspaso					: 	{ type: "number"},
									IdSerieFondo				:	{ type: "string"},
									Porcentaje					: 	{ type: "number"},
									IdEstado					:	{ type: "string"},
									FechaIngreso				:	{ type: "date"},
								    FechaModificacion			:	{ type: "date"},
								    FechaIngresoRegistro		:	{ type: "date"},
								    FechaModificacionRegistro	:	{ type: "date"},
									Estado						:	{ type: "string"},
									NombreSerieFondo			:	{ type: "string"},
									CodigoSerie					:	{ type: "string"},
									NombreCortoEmisor			:	{ type: "string"},
									DocumentoIdentidadEmisor	:	{ type: "string"},
											
									IdSolicitudAporteRescate	: 	{ type: "number"},
									IdTipoMovimientoFondoMutuo	:	{ type: "string"},
									FechaCertificacion			:	{ type: "date"},
									FechaLiquidacion			:	{ type: "date"},
									IdMoneda					:	{ type: "string"},
									Monto						: 	{ type: "number"},
									NumeroCuotas				: 	{ type: "number"},
									ValorCuota					: 	{ type: "number"},
									DecimalesCuota				:   { type : "number"},
									DecimalesValorCuota   	  	:   { type : "number"},
																	
									MontoMonedaSistema			: 	{ type: "number"},
									EstadoSolicitud				:	{ type: "string"},
									EstadoLiquidacion			:	{ type: "string"},
									NombreEstadoSolicitud		:	{ type: "string"},
									NombreEstadoLiquidacion		:	{ type: "string"},
									DescripcionTipoMovimiento	:	{ type: "string"}
								}
							}
						}
					}
				}),
				columns: [
				    {field: "IdSerieFondo",		title: $translate.instant("UI_INSTRUMENTO")	,width: "100px"},
				    {field: "Porcentaje",			title: $translate.instant("UI_PORCENTAJE")	,width: "50px"	},
				    {field: "FechaIngreso",		title: $translate.instant('UI_FECHA_INGRESO'),	width: "80px", align: "right"},
				    
				    {field: "DescripcionTipoMovimiento",	title: $translate.instant('UI_OPERACION'),								width: "90px", align: "left"},
				    {field: "IdSolicitudAporteRescate",		title: $translate.instant('UI_SECUENCIA'),								width: "60px",	align: "left"},
				    {field: "NombreEstadoSolicitud",		title: $translate.instant('UI_ESTADO_SOLICITUD'),						width: "100px", align: "left"},
				    {field: "FechaCertificacion",	    	title: $translate.instant('UI_FECHA_CERTIFICACION'),					width: "80px", align: "left"},
				  
				    {field: "ValorCuota",					title: $translate.instant('UI_VALOR_CUOTA'),							width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(ValorCuota, DecimalesValorCuota) #</label>"},
				    {field: "NumeroCuotas",		    		title: $translate.instant('UI_CANTIDAD'),		    					width: "75px", 	align: "left" , template : " <label> #:   util.formateaNumero(NumeroCuotas, DecimalesCuota) #</label>"},
				    {field: "IdMoneda",				    	title: $translate.instant('UI_MONEDA'),			    					width: "60px",	align: "left"},
				    {field: "MontoMonedaSistema",			title: $translate.instant('UI_MONTO_MONEDA_SISTEMA'),					width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(MontoMonedaSistema,'FMM','Monto','"+ $rootScope.SesionIdMonedaSistema +"', '','')).valor #</label>"}
	
				    
				],
				sortable	: false,
				pageable	: false,
				autoBind	: true,
				dataBound: function () {},
			    selectable 	: "row",
				pageable: {
                    refresh: true,
                    buttonCount: 5
                },
			    refresh		: true,
			    rowCellColor : function(row) {
			    	var column = new Array();
			    	if (row.get("EstadoSolicitud")=='CRT'){column.push({column: "NombreEstadoSolicitud", color: "green"});};
			    	if (row.get("IdEstadoCargoAbono")=='CRT'){column.push({column: "NombreEstadoCargoAbono", color: "green"});};
			    	if (row.get("EstadoSolicitud")=='ANU'){column.push({column: "NombreEstadoSolicitud", color: "red"});};
			    	return column;
			    }
			}
		};


		pti.Recaudacion = function(dataItem){
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300022",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdTraspaso", operator: "eq", value: (dataItem && dataItem.IdTraspaso) || -1  }
						   ,{ field: "IdEstado " , operator: "eq", value:  'ACT' }
						   	],
						schema: {
							model: {
								fields: {
									IdRecaudacion 				: 	{ type: "number"},
									IdEmpresa					: 	{ type: "number"},
									IdTraspaso					: 	{ type: "number"},
									FechaDeposito				:	{ type: "date"},
									IdMedioDePago				:	{ type: "date"},
									IdBanco						: 	{ type: "number"},
									NumeroCuentaBancaria		: 	{ type: "number"}, 
									Sucursal					:	{ type: "string"}, 
									MontoDeposito				: 	{ type: "number"},  
									Observacion					:	{ type: "string"}, 
									IdEstado					:	{ type: "string"}, 
									FechaIngreso				:	{ type: "date"}, 
									FechaIngresoRegistro		:	{ type: "date"}, 
									MedioDePago					:	{ type: "string"},
									Banco						:	{ type: "string"} 
								
								}
							}
						}
					}
				}),
				columns: [
					{field: "FechaDeposito",	    	title: $translate.instant('UI_FECHA_DEPOSITO'),					width: "60px", align: "left"},
				    {field: "Banco",					title: $translate.instant("UI_BANCO"),							width: "150px"},
				    {field: "MedioDePago",				title: $translate.instant("UI_MEDIO_DE_PAGO"),					width: "100px"},
				    {field: "NumeroCuentaBancaria",		title: $translate.instant('UI_NUMERO_CUENTA'),					width: "90px", align: "left"},
				    {field: "MontoDeposito",			title: $translate.instant('UI_MONTO_DEPOSITO'),					width: "75px", align: "left"},
				    {field: "Observacion",				title: $translate.instant('UI_OBSERVACION'),					width: "200px", align: "left"}
				    
				],
				sortable	: false,
				pageable	: false,
				autoBind	: true,
				//dataBound: function () {},
	            dataBound: function(e) {
	        	   e.sender.expandRow( e.sender.tbody.find("tr.k-master-row").first());
	            },
				pageable: {
                    refresh: true
                    ///pageSizes: true,
                    //buttonCount: 5
                },
			    selectable 	: "row",
			    refresh		: true,
			}
		};
		
		pti.RecaudacionMovimiento = function(dataItem){
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=300023",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdRecaudacion", operator: "eq", value: (dataItem && dataItem.IdRecaudacion) || -1  }
						],
						schema: {
							model: {
								fields: {
									IdMovimientoRecaudacion 		: 	{ type: "number"},
									IdRecaudacion					: 	{ type: "number"},
									IdModulo						:	{ type: "string"},
									IdSecuenciaMovimiento			: 	{ type: "number"},  
									FechaEfectiva					:	{ type: "date"}, 
									IdTipoMovimiento				:	{ type: "string"}, 
									TipoMovimiento					:	{ type: "string"},
									IdEstado						:	{ type: "string"},
									Monto							: 	{ type: "number"},
									IdCliente						: 	{ type: "number"}, 
									IdPortafolio					: 	{ type: "number"},
									IdMoneda						:	{ type: "string"},
									//CodigoPortafolio				:	{ type: "string"},
									NombreCompleto					:	{ type: "string"},
									DocumentoIdentidad				:	{ type: "string"},
									EstadoMovimiento				:	{ type: "string"},
									Modulo							:	{ type: "string"}
								
								}
							}
						}
					}
				}),
				columns: [
					{field: "Modulo",					title: $translate.instant("UI_MODULO"),							width: "100px"},
					{field: "EstadoMovimiento",	    	title: $translate.instant('UI_ESTADO'),							width: "80px", align: "left"},
					{field: "FechaEfectiva",	    	title: $translate.instant('UI_FECHA_EFECTIVA'),					width: "75px", align: "left"},
				    {field: "IdSecuenciaMovimiento",	title: $translate.instant("UI_SECUENCIA"),						width: "60px"},
				    {field: "TipoMovimiento",			title: $translate.instant("UI_TIPO_MOVIMIENTO"),				width: "80px"},
				    {field: "IdMoneda",					title: $translate.instant('UI_MONEDA'),							width: "60px", align: "left"},
				    {field: "Monto",					title: $translate.instant('UI_MONTO'),							width: "60px", align: "left"},
				    {field: "NombreCompleto",			title: $translate.instant('UI_NOMBRE'),							width: "200px", align: "left"},
				    {field: "DocumentoIdentidad", menu: true,		title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),			width: "75px"}
				    
				],
				hideDetailButton: function(row) {
					var hide = true;
					if(row.IdModulo === 'TES') 
						hide = false;
					return hide;
				},
				sortable	: false,
				pageable	: false,
				autoBind	: true,
				dataBound: function () {},
			    selectable 	: "row",
			    refresh		: true,
			    rowCellColor : function(row) {
			    	var column = new Array();
			    	if (row.get("IdEstado")=='CRT'){column.push({column: "EstadoMovimiento", color: "green"});};
			    	if (row.get("IdEstado")=='EMI'){column.push({column: "EstadoMovimiento", color: "green"});};
			    	if (row.get("IdEstado")=='ANU'){column.push({column: "EstadoMovimiento", color: "red"});};
			    	return column;
			    }
			}
		};	

		$scope.confBasicaIngresoEgresoFormaPago = function(item) {
			var dsDef = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgresoFormaDePago;
			dsDef.definition.filter = 
			[
			    { field: "IdIngresoEgreso", operator: "eq",  value: item.IdSecuenciaMovimiento },
			    { field: "IdEstado",        operator: "neq", value: "MOD" }
			] 
			return {
				dataSource			: new bcs.DataSource(dsDef),
				columns				: bcsBlotterComprobantesGrid.columnasIngresoEgresoFormaDePago(),		
				sortable			: true,
				pageable			: true,
				autoBind			: true,
				height				: 150,
				selectable 			: "multiple row",
				userSettings		: true
			};
		};
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pti.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		
		pti.cmbbx.perfilProductos={
			filtros : {
				EsPerfilPersonalizado 		: { field: "EsPerfilPersonalizado",  operator: "eq", value: "N" },
				IdCategoriaInversionista 	: { field: "IdCategoriaInversionista", operator: "eq", value : "" }
			}
		};
		
		pti.cmbbx.TipoTraspaso={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
					},
			};
		pti.cmbbx.Motivo={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};
		pti.cmbbx.Emisor={
				filtros : {
					IdEstado 			: { field: "IdEstado", 				operator: "eq", value: 'ACT' },
					IndicadorAGF 		: { field: "IndicadorAGF",          operator: "eq", value: 'S' }
				},
			};	
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pti.fn.buscar = function(){
			//Datos grillas	
				
			var niv1filters = new Array();
	
	    	
			niv1filters.push({ field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa });
			niv1filters.push({ field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAING'});
			
			if (pti.FecInicio) {
				niv1filters.push({ field: "FechaIngreso", operator: "gte", value:util.dateInicioDia(pti.FecInicio) });
			};
			if (pti.FecTermino) {
				niv1filters.push({ field: "FechaIngreso", operator: "lte", value:util.dateTerminoDia(pti.FecTermino) });
			};
				
			if(pti.filtro.IdSocioDeNegocio && pti.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pti.SocioDeNegocioObj.IdCliente });
			};
			if(pti.cmbbx.IdTipoTraspaso ){
	    		niv1filters.push({ field: "IdTipoTraspaso", operator: "eq", value:pti.cmbbx.IdTipoTraspaso });
	    	};
			if(pti.cmbbx.IdEmisor){
	    		niv1filters.push({ field: "IdEmisor", operator: "eq", value:pti.cmbbx.IdEmisor });
	    	};
			if(pti.cmbbx.IdMotivo){
	    		niv1filters.push({ field: "IdMotivoTraspaso", operator: "eq", value:pti.cmbbx.IdMotivo });
	    	};	    	
	    	if (niv1filters.length>0) pti.Traspasos.dataSource.filter(niv1filters);
	    	else pti.Traspasos.dataSource.filter(new Array());
	    	
	    	pti.Traspasos.dataSource.read();
	    	pti.TraspasosSel=undefined;
			
		}
		pti.fn.limpiar = function(){
			pti.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pti.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pti.filtro.IdSocioDeNegocio=0;
			pti.cmbbx.IdTipoTraspaso=0;
			pti.cmbbx.IdEmisor=0;
			pti.cmbbx.IdMotivo=0;
		};
		
		pti.fn.ingresar= function(){
			bcsComunes.ventanaAbrir("IngresoTraspasosFFM", {IdTipoMovimiento: 'TRAING'} );
			
		};
		pti.fn.editar= function(){
			if (pti.TraspasosSel)
				{
					bcsComunes.ventanaAbrir("IngresoTraspasosFFM", {IdTipoMovimiento: 'TRAING',IdTraspaso: pti.TraspasosSel.IdTraspaso} );
				};
			
		};
		
		pti.fn.anular = function() {
			 var IdVentanamodal = bcsComunes.ventanaAbrir("AnularTraspasoFMM", {IdTipoMovimiento: 'TRAING',IdTraspaso: pti.TraspasosSel.IdTraspaso}, true, false, pti.idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					pti.fn.buscar();
				});
		};
		
		pti.fn.detalle= function(){
			if (pti.TraspasosSel)
				{
					bcsComunes.ventanaAbrir("IngresoTraspasosFFM", {IdTipoMovimiento: 'TRAING',IdTraspaso: pti.TraspasosSel.IdTraspaso, Modo:'DETALLE'} );
				};
			
		};
		
		pti.fn.recaudacion = function() {
			
					 
			 var IdVentanamodal = bcsComunes.ventanaAbrir("IngresoDepositoTraspasoFMM", {datosTraspaso: pti.TraspasosSel}, true, false, pti.idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					pti.fn.buscar();
				});
		};

		 pti.fn.certificarCargo = function(){		
	
				 var datos = {
						 p_IdEmpresa 		: $scope.SesionIdEmpresa, //saca de variables de entorno
						 p_IdUsuario 		: $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo			: 'FMM',
						 p_UsaReglasNegocio :  'S',
						 p_FechaMovimiento 	: $scope.SesionFechaSistema,
						 p_IdPais		 	: $scope.SesionIdPaisSistema,
						 p_CargosAbonosACertificar : [{
							 IdCargoAbonoMovimiento		      		:	pti.OrigenSel.IdCargoAbonoMovimiento
						 }]
						 
				 }
				
			
				bcsComunes.ventanaOverlay($scope.winID, true);
		       	bcsServiciosCuentaInversion.CertificarCargoAbono(
		       		datos,
		       		function(respuesta){
		       			bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						$scope.buscarMovimientos();
						
					},
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_CERTIFICAR_MOVIMIENTO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);				
					}
		       	);
		       	
			};
		
			/*********************************************
			 * Crystal Reports
			 ********************************************/		
			
			pti.fn.verComprobanteMLT = function() {
					
				if(pti.TraspasosSel==undefined)
				{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UN_TRASPASO"));
	    	 		return;
				};
				
				if(pti.TraspasosSel.IdEstado!='VIG')
				{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_ESTAR_VIGENTE"));
	    	 		return;
				};	

				
				
				
				
					var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'TrasMLT' },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
						if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
							
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_IdModulo"		: 'FMM',
												"p_IdUsuario"				: $scope.SesionIdUsuario,
												"p_IdTraspaso" :pti.TraspasosSel.IdTraspaso,
												
					                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
												  };
							
							$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
													   , "serviceData" :	parametros
													  };

							$scope.dataTemplate.parameters= {};
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							
							bcsConfiguracionFFM.getReport($scope.dataTemplate);
						}else{
							bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_FALTA_CONFIGURACION_DEL_REPORTE"));
			    	 		return;
						};
					});
					
					
					
			 };			
			
				pti.fn.verComprobanteCSC = function() {
					
					if(pti.TraspasosSel==undefined)
					{
		    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_TRASPASO"));
		    	 		return;
					};	
					
					
						var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'TrasCSC' },
						                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
						                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
						bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
							if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
								$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
								$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
								$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
								
								var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
													"p_IdModulo"		: 'FMM',
													"p_IdUsuario"				: $scope.SesionIdUsuario,
													"p_IdTraspaso" :pti.TraspasosSel.IdTraspaso,
													
						                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
													  };
								
								$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
														   , "serviceData" :	parametros
														  };

								$scope.dataTemplate.parameters= {};
								var subrep = new Array();
								$scope.dataTemplate.subReports=subrep;
								
								bcsConfiguracionFFM.getReport($scope.dataTemplate);
							}else{
								bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_FALTA_CONFIGURACION_DEL_REPORTE"));
				    	 		return;
							};
						});						
				 };			
					
			
			/*********************************************
			 * WATCH
			 ********************************************/
			$scope.$watch('pti.TraspasosSel',function(newval,oldval){
				pti.OrigenSel=undefined;
			});	
		
		

	});
}());