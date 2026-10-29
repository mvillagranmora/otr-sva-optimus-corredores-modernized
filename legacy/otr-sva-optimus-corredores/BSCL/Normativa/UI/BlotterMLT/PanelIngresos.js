(function() {
	angular.module('ModuloNormativa').controller('PanelIngresosCtrl',
	function($rootScope,$scope,$translate,bcsComunes,bcsServicioPlataforma,
				 bcsAlerta, OPCIONES_BCSALERTA,$http){

		
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
		pti.cmbbx.Regimen='18Q';
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var defMLT = {
				type: "odata",
				definition : {
					serverFiltering : true, // Filtrado de datos en el servidor.
	                serverPaging    : true, // Paginado de los datos en el servidor.
	                serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                pageSize        : 25,
	                sort			: {field: "IdSolicitudAporteRescate", dir: "desc" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290687",
							dataType: "json"
						}
					},
					filter: [
					    { field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa },
					   // { field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAING' },
					    { field: "FechaValorCuota ", operator: "gte", value: auxDate1 },
					    { field: "FechaValorCuota ", operator: "lte", value: auxDate2 }				    
					],
					schema: {
						model: {
							fields: {
		        				IdSolicitudAporteRescate	: 	{ type: "number"},
		        				FolioContrato				: 	{ type: "number"},
		        				FolioMandato				: 	{ type: "number"},
		        				RentabilidadReal			: 	{ type: "number"},
		        				CostoUF						: 	{ type: "number"},
		        				MontoMonedaSistema			: 	{ type: "number"},
		        				ValorCuota 					: 	{ type: "number"},
		        				NumeroCuotas				: 	{ type: "number"}, 
		        				FechaCertificacion			:	{ type: "date"},
		        				FechaValorCuota				:	{ type: "date"},
		        				RazonSocial					:	{ type: "string"},
		        				NombreCompleto				:	{ type: "string"},
		        				DocumentoIdentidad			:	{ type: "string"},
		        				CodigoPortafolio			:	{ type: "string"},
		        				DescripcionPortafolio 		:	{ type: "string"},
		        				PropositoPortafolio 		:	{ type: "string"},
		        				IndicadorMLT				:	{ type: "string"},
		        				EsIndicadorMLTUI			:	{ type: "string"},
		        				IdSerieFondoOrigen			:	{ type: "string"},
		        				TipoSolicitud				:	{ type: "string"},
		        				DescripcionTipoSolicitud	:	{ type: "string"}
							}
						}
					}
				}	
			};
		
		var dsMLT = new bcs.DataSource(defMLT);
		pti.MLT = {
				dataSource: dsMLT,
				columns: [
						{field: "IdSolicitudAporteRescate",		title: $translate.instant('UI_SECUENCIA'),						width: "80px", align: "right", editable: false},
						{field: "FolioContrato",				title: $translate.instant('UI_FOLIO_CONTRATO'),					width: "80px", align: "right", editable: false},
						{field: "FolioMandato",					title: $translate.instant('UI_FOLIO_MANDATO'),					width: "80px", align: "right", editable: false},
						{field: "RentabilidadReal",				title: $translate.instant('UI_RENTABILIDAD_REAL'),				width: "80px", align: "right", editable: false,	hidden: true},
						{field: "CostoUF",						title: $translate.instant('UI_COSTO_UF'),						width: "80px", align: "right", editable: false,	hidden: true},
						{field: "MontoMonedaSistema",			title: $translate.instant('UI_MONTO_MONEDA_SISTEMA'),			width: "100px",align: "right",  template : " <label> #:  (util.bcs_formato(MontoMonedaSistema,'FMM','Monto','"+ $rootScope.SesionIdMonedaSistema +"', '','')).valor #</label>"},
						{field: "ValorCuota",					title: $translate.instant('UI_VALOR_CUOTA'),					width: "75px", 	align: "right"},
						{field: "NumeroCuotas",		    		title: $translate.instant('UI_CANTIDAD'),		 				width: "75px", align: "right"},
						{field: "FechaCertificacion",	    	title: $translate.instant('UI_FECHA_CERTIFICACION'),			width: "125px", align: "right"},
						{field: "FechaValorCuota",				title: $translate.instant('UI_FECHA_VALOR_CUOTA'),				width: "100px", align: "right"},
						{field: "NombreCompleto",				title: $translate.instant('UI_NOMBRE'),							width: "100px", align: "left"},
						{field: "DocumentoIdentidad",			title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'),			width: "80px", align: "left"},
						{field: "CodigoPortafolio",				title: $translate.instant('UI_CODIGO_PORTAFOLIO'),				width: "100px", align: "left"},
						{field: "DescripcionPortafolio",		title: $translate.instant('UI_PORTAFOLIO'),						width: "100px", align: "left"},
						{field: "PropositoPortafolio", 			title: $translate.instant('UI_PROPOSITO'),						width: "100px",	align: "left"},
						{field: "IndicadorMLT", 				title: $translate.instant('UI_MLT'),							width: "100px",	align: "left"},
						///{field: "IdSerieFondoOrigen",			title: $translate.instant('UI_INSTRUMENTO_ORIGEN'),				width: "100px", align: "left"},
						{field: "DescripcionTipoSolicitud",		title: $translate.instant('UI_TIPO_SOLICITUD'),					width: "100px", align: "left"}
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
					if (pti.cmbbx.Regimen==='18Q'){
						g.showColumn("CostoUF");
						
					}else{
						g.showColumn("RentabilidadReal");
					}
					
					
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
									IdCliente						: 	{ type: "number"},
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
				    {field: "TipoSolicitud",			title: $translate.instant("UI_TIPO_SOLICITUD"),				width: "80px"},
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
			if (pti.cmbbx.Regimen==='18Q')
				{
				pti.ApiTraspasos.showColumn("CostoUF");
				pti.ApiTraspasos.hideColumn("RentabilidadReal");
				}else{
					pti.ApiTraspasos.showColumn("RentabilidadReal");
					pti.ApiTraspasos.hideColumn("CostoUF");	
				}
				
	    	
			niv1filters.push({ field: "IdEmpresa ", operator: "eq", value: $scope.SesionIdEmpresa });
			//niv1filters.push({ field: "IdTipoMovimientoTraspaso ", operator: "eq", value: 'TRAING'});
			
			if (pti.FecInicio) {
				niv1filters.push({ field: "FechaValorCuota", operator: "gte", value:util.dateInicioDia(pti.FecInicio) });
			};
			if (pti.FecTermino) {
				niv1filters.push({ field: "FechaValorCuota", operator: "lte", value:util.dateTerminoDia(pti.FecTermino) });
			};
				
			if(pti.filtro.IdSocioDeNegocio && pti.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pti.SocioDeNegocioObj.IdCliente });
			};
			
			if(pti.cmbbx.Regimen ){
	    		niv1filters.push({ field: "Regimen", operator: "eq", value:pti.cmbbx.Regimen });
	    	};
	    	
			
			if(pti.Secuencia){
	    		niv1filters.push({ field: "IdSolicitudAporteRescate", operator: "eq", value:pti.Secuencia });
	    	};
	    	if(pti.FolioContrato){
	    		niv1filters.push({ field: "FolioContrato", operator: "eq", value:pti.FolioContrato });
	    	};
	    	/*
	    	if(pti.cmbbx.IdMotivo){
	    		niv1filters.push({ field: "IdMotivoTraspaso", operator: "eq", value:pti.cmbbx.IdMotivo });
	    	};
	    	*/	    	
	    	if (niv1filters.length>0) pti.MLT.dataSource.filter(niv1filters);
	    	else pti.MLT.dataSource.filter(new Array());
	    	
	    	pti.MLT.dataSource.read();
	    	pti.MLTSel=undefined;
			
		}
		pti.fn.limpiar = function(){
			pti.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pti.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pti.filtro.IdSocioDeNegocio=0;
			pti.Secuencia='';
			pti.FolioContrato='';
			pti.cmbbx.Regimen='18Q';
		};
		

		pti.fn.editar= function(){
			if (pti.MLTSel)
				{
					bcsComunes.ventanaAbrir("IngresoIngresoMLT", {Regimen: pti.MLTSel.Regimen,IdSolicitudAporteRescate: pti.MLTSel.IdSolicitudAporteRescate,selec:pti.MLTSel} );
				};
			
		};
		
		pti.fn.procesarCostos = function() {
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_PROCESAR_COSTOS" ) +" ?",pti.idVentana);
			t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						 
							var entradaPrograma 		= {};					
							entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
							entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
							entradaPrograma.p_IdModulo    		= 'NOR';
							entradaPrograma.p_IdAgenda    		= 0;    
							entradaPrograma.p_IdTipoTarea   	= 'ACTUALIZA_COSTO_TRIBUTARIO';
							entradaPrograma.p_Segundos      	= 0;			
							entradaPrograma.p_CreaParam     	= 'S';
						    

							entradaPrograma.p_TablaParametros =
							[
							   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
							   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario}
							   //{NombreParametro	: 'IdModulo', 	  		Valor: 'FMM'},	
							   //{NombreParametro	: 'FechaDesde', 	  	Valor: util.dateSPdate($scope.filtros.fechaDesde) || '0001-01-01'},
							   //{NombreParametro	: 'FechaHasta', 	  	Valor: util.dateSPdate($scope.filtros.fechaHasta) || '0001-01-01'},  
							
							];

							

							console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
										
							bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
								function(respuesta){
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
										Programa = respuesta.P_OUT_IDPROGRAMA;					
										if (Programa.length > 0)
										    idPrograma = Programa[0].IdPrograma;								
												
										bcsComunes.ventanaMsg({
											mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea su ejecucion en Consultar Programas"							
										},$scope.winID);						
									}
									else
									{						
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("ERROR")							
										},$scope.winID);
									}
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.winID);					
								}
							);		

						
						 
				       };
				});	
	        
	  	
			
		};
		
				
		pti.fn.recaudacion = function() {
			
					 
			 var IdVentanamodal = bcsComunes.ventanaAbrir("IngresoDepositoTraspasoFMM", {datosTraspaso: pti.MLTSel}, true, false, pti.idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
					pti.fn.buscar();
				});
		};

		pti.fn.cargaArchivoMLT = function() {
			bcsComunes.ventanaAbrir("CargaArchivoMLT",{}); 
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
			 * WATCH
			 ********************************************/
			$scope.$watch('pti.MLTSel',function(newval,oldval){
				pti.OrigenSel=undefined;
			});	
		
		

	});
}());