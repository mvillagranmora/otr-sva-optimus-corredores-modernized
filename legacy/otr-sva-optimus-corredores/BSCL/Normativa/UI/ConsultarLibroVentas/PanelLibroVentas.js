(function() {
	angular.module('ModuloNormativa').controller('PanelLibroVentasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA,$http){

		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pct 		= this;
		pct.idMonedaSistema = $scope.SesionIdMonedaSistema;
		pct.idVentana	= bcsComunes.ventanaWinId($scope);
		pct.win		= {};
		pct.grilla		= {};
		pct.cmbbx		= {};
		pct.filtro		= {};
		pct.fn			= {};
		
		pct.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		pct.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		pct.Periodo 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		pct.cmbbx.Operacion						= {};
		pct.cmbbx.Operacion.filtros				= {};
		
		pct.cmbbx.TipoInstrumento				= {};
		pct.cmbbx.TipoInstrumento.filtros		= {};
		
		pct.cmbbx.Clasificacion					= {};
		pct.cmbbx.Clasificacion.filtros			= {};
		
		pct.cmbbx.TipoComprobante					= {};
		pct.cmbbx.TipoComprobante.filtros			= {};
		
		pct.FecInicioOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		
		pct.TipoArchivo			= 'pdf';
	   // pct.IdTipoDocumentoComprobante='UAFDOFCOMP';
		pct.IdVersionEsquemaComprobante 	= '';
		pct.modulo='NOR';
		pct.salidaMensaje='';
		pct.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pct.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.dataTemplate	= {};
		var filters = {};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
					
		pct.grilla.LibroVentas = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3334",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								
								FechaMovimiento		: { type: "date"}, 		
								FolioTransaccion	: { type: "string"},
								FolioComprobante	: { type: "number"},
								SecuenciaOrden		: { type: "string"},
								IdOperacion			: { type: "number"},	
								IdCliente			: { type: "number"},	
								RutCliente			: { type: "string"},
								NombreCliente		: { type: "string"},
								IdPortafolio		: { type: "number"},	
								CodigoPortafolio	: { type: "string"},
								IdClasificacionTipoInstrumento: { type: "string"},
								IdTipoInstrumento	: { type: "string"},
								IdTipoOperacion		: { type: "string"},
								ClasificacionTipoInstrumento: { type: "string"},
								TipoInstrumento		: { type: "string"},
								TipoOperacion		: { type: "string"},
								AccionOperacion		: { type: "string"},
								ClaseOperacion		: { type: "string"},
								IdTipoProducto		: { type: "string"},
								IdEmisor			: { type: "string"},
								Nemotecnico			: { type: "string"},
								Cantidad			: { type: "number"}, 		
								Precio				: { type: "number"}, 		
								Tasa				: { type: "number"}, 		
								IdMoneda			: { type: "string"},
								IdTipoComprobante	: { type: "string"},
								NombreTipoComprobante: { type: "string"}, 		
								MontoOperado		: { type: "number"}, 		
								Comision			: { type: "number"}, 		
								Derecho				: { type: "number"}, 		
								CargoFijo			: { type: "number"}, 		
								IVA					: { type: "number"}, 		
								MontoAfecto			: { type: "number"}, 		
								MontoExento			: { type: "number"}, 		
								MontoTotal			: { type: "number"}
							
							}
						}
					},
					aggregate: [
			            { field: "Derecho", aggregate: "sum" },
			            { field: "CargoFijo", aggregate: "sum"},
			            { field: "Comision", aggregate: "sum"},
			            { field: "MontoTotal", aggregate: "sum"},
			            { field: "MontoExento", aggregate: "sum"},
			            { field: "MontoOperado", aggregate: "sum"},
			            { field: "MontoAfecto", aggregate: "sum"}
			        ]
				}
			}),
			columns: [
				
			    	{field: "FechaMovimiento",				title: $translate.instant("UI_FECHA_INGRESO"),			width: "70px"	},
			        {field: "FolioComprobante",				title: $translate.instant("UI_FOLIO_COMPROBANTE"),		width: "50px"	},
			        {field: "IdTipoComprobante",			title: $translate.instant("UI_TIPO_COMPROBANTE"),		width: "30px"	},
			        {field: "RutCliente",					title: $translate.instant("UI_RUT_CLIENTE"),			width: "70px"	},
			        {field: "CodigoPortafolio",				title: $translate.instant("UI_PORTAFOLIO"),				width: "50px"	},
			        {field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),					width: "180px"	},
			        {field: "IdTipoProducto",				title: $translate.instant("UI_PRODUCTO"),				width: "30px"	},
			        {field: "ClaseOperacion",				title: $translate.instant("UI_OPERACION"),				width: "20px"	},
			        {field: "MontoAfecto",					title: $translate.instant("UI_MONTO_AFECTO"),			template: "<label> #: (util.bcs_formato(MontoAfecto,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}	},
			        {field: "MontoOperado",					title: $translate.instant("UI_MONTO_OPERADO"),			template: "<label> #: (util.bcs_formato(MontoOperado,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}	},
			        {field: "MontoExento",					title: $translate.instant("UI_MONTO_EXENTO"),			template: "<label> #: (util.bcs_formato(MontoExento,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}		},
			        {field: "IVA",							title: $translate.instant("UI_IMPUESTO"),				template: "<label> #: (util.bcs_formato(IVA,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px"},
			        {field: "MontoTotal",					title: $translate.instant("UI_MONTO_TOTAL"),			template: "<label> #: (util.bcs_formato(MontoTotal,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}		},
			        {field: "Derecho",				    	title: $translate.instant("UI_DERECHOS"),		    	template: "<label> #: (util.bcs_formato(Derecho,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}		},
			        {field: "CargoFijo",					title: $translate.instant("UI_GASTOS"),		        	template: "<label> #: (util.bcs_formato(CargoFijo,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}		},
			        {field: "Comision",						title: $translate.instant("UI_COMISIONES"),			    template: "<label> #: (util.bcs_formato(Comision,'ORD','MONTO','"+pct.idMonedaSistema+"','','')).valor #</label>",width: "80px", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'ORD','Monto','"+pct.idMonedaSistema+"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}		},
			        
			],
			hideDetailButton: function(row) {
				var hide = false;
				//if(row.IdTipoLimite === 'LIMCTPEXCLUIREMISOR' || row.IdTipoLimite === 'LIMCTPMARGENTASAS') 
				//	hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
			//autoBind	: true,
			autoBind	: false,
			height		: 540,//580,
		    //selectable 	: "row",
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true,
			excel 				: { allPages : true },
			rowColorBackground	: function(row) {		
				switch(row.get("AccionOperacion")) {
		             case 'VENTA':
		                return "red";
		                break;
		             case 'RESCATE':
		                return "red";
		                break;		            
		             case 'COMPRA':
		                return "green";
		                break;
		             case 'APORTE':
			            return "green";
			            break;     
		            default:
		            	return "white";
		                break;     
				  }	
				},
		    filterable	: true
		};
		
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pct.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		
		pct.cmbbx.Clasificacion={
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		pct.cmbbx.TipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				//IdBolsa : { "field"   : "IdBolsa","operator": "eq", "value"   : $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		pct.cmbbx.TipoComprobante={
				tipo : "combobox-tipo-comprobante",
				configuracion : {},
				filtros : {
					IdEstadoFacturacion :	{field : "IdEstadoFacturacion", operator : "eq", value : "ACT"}
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
			};

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pct.envioFecha = function(){
			var date = '';
			var dateFin = new Date();
			dateFin 	 = pct.FecInicio;
			date= util.dateSPdate(pct.FecInicio);
			pct.FecInicio= util.dateInicioDia(date.substring(0,8) + '01');
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pct.FecTermino= util.dateInicioDia(ultimoDia);
 
			
		};
		
		
		pct.fn.buscar = function(){
			
			pct.envioFecha();
	    	
	    	if (pct.FecInicio == undefined || pct.FecInicio == undefined){return;};
			
			var handler = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_FechaDesde : util.dateSPdate(pct.FecInicio),
				p_FechaHasta : util.dateSPdate(pct.FecTermino),
				
				p_IdBolsa : null,
				p_IdCliente : (pct.SocioDeNegocioObj.IdCliente && pct.SocioDeNegocioObj.IdCliente > 0) ? pct.SocioDeNegocioObj.IdCliente : null,
				p_IdClasificacion : (pct.IdClasificacion !== undefined && pct.IdClasificacion !== 0) ? pct.IdClasificacion : null,
				p_IdTipoInstrumento : (pct.IdTipoInstrumento !== undefined && pct.IdTipoInstrumento !== 0) ? pct.IdTipoInstrumento : null,
				p_TipoComprobante : (pct.IdTipoComprobante !== undefined && pct.IdTipoComprobante !== 0) ? pct.IdTipoComprobante : null
				
			}
			

			pct.grilla.LibroVentas.dataSource.setHandler(handler);
			pct.grilla.LibroVentas.dataSource.read();
	    	
			
				    	
		};
		
		
		
			
		pct.fn.limpiar = function(){
			
			
			pct.grilla.LibroVentas.dataSource.removeAll();
		};
		

	});
}());