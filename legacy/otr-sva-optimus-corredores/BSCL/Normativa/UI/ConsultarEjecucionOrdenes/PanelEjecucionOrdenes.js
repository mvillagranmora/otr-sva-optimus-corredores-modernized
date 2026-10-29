(function() {
	angular.module('ModuloNormativa').controller('PanelEjecucionOrdenesCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA,$http){

		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pct 		= this;
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
		
		pct.PorFechaEjecucion= true;
		
		pct.TipoArchivo			= 'pdf';
	    pct.IdTipoDocumentoComprobante='UAFDOFCOMP';
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
		pct.dsDetalleDOF = function(){
			return new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering : true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1277",
						dataType: "json"
					}
				},
				schema: {
					model: { fields: {		 	
						IdDeclaracionOrigenFondos 		: { type: "number"},
						IdCliente						: { type: "number"},
					} }
				}
				}
			})
		};
		

				
		pct.grilla.EjecucionOrdenes = {
			dataSource: new bcs.DataSource({
				type: "odata",
				//type : "call",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sort 			: { field: "IdOrden", dir: "desc" },
					pageSize 		: 100,
					
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3235",
							dataType: "json"
						}
					},
					
					/*
					callParameters      : filters,
					callTransaction 	: "3076",
					callResultSet 		: "P_OUT_TABLADOF",
					*/
					schema: {
						model: {
							fields: {
									IdOrden							: { type: "number"},							
									Folio							: { type: "string"},							
									IdCliente						: { type: "number"},					
									IdPortafolio					: { type: "number"},	 				
									IdClasificacionTipoInstrumento	: { type: "string"},	 	
									IdTipoInstrumento				: { type: "string"},					
									IdTipoOperacion					: { type: "string"},						
									TipoOperacion					: { type: "string"},
									IdInstrumento					: { type: "number"}, 					
									Instrumento						: { type: "string"},
									Cantidad						: { type: "number"}, 						
									Monto							: { type: "number"}, 							
									MontoValorizado					: { type: "number"},					
									Precio							: { type: "number"}, 							
									IdMoneda						: { type: "string"}, 						
									IdCondicionDeLiquidacion		: { type: "string"}, 		
									FechaRecepcion					: { type: "date"}, 					
									FechaAceptacion					: { type: "date"}, 				
									TipoEjecucion					: { type: "string"},				
									FechaEjecucion					: { type: "date"}, 							
									IdBolsa							: { type: "string"},	 						
									EsOD							: { type: "string"},								
									EsODUI							: { type: "string"},
									PrecioEjecucion					: { type: "number"}, 						
									MontoTransado					: { type: "number"}, 						
									CantidadTransada				: { type: "number"}, 					
									ComisionTransada				: { type: "number"}, 					
									MejorPrecioOtraBolsa			: { type: "number"}, 				
									CantidadMejorPrecioOtraBolsa	: { type: "number"}, 	 	
									MejorPrecioMercado				: { type: "number"}, 					
									PrecioMinimoMercado				: { type: "number"}, 					
									HoraPrecioMinimoMercado			: { type: "string"},				
									IdBolsaPrecioMinimoMercado		: { type: "string"},	 		
									PrecioMaximoMercado				: { type: "number"}, 				
									HoraPrecioMaximoMercado			: { type: "string"},			
									IdBolsaPrecioMaximoMercado		: { type: "string"}, 		
									MayorPrecioOperacionCorredor	: { type: "number"}, 	
									HoraPrecioMayorCorredor			: { type: "string"},			
									MenorPrecioOperacionCorredor	: { type: "number"}, 	
									HoraPrecioMenorCorredor			: { type: "string"},			
									IdEmpleadoDigitador				: { type: "number"}, 				
									FolioComprobanteFacturacion		: { type: "number"},     
									FechaGeneracion					: { type: "date"},  				
									FolioExterno					: { type: "string"}, 					
									IdEstadoNotificacion			: { type: "string"},		 	
									IdEstadoCarta					: { type: "string"},		 			
									FechaIngreso					: { type: "date"},      				
									FechaCreacion					: { type: "date"},     				
									IdUsuarioCreador				: { type: "number"},  
									Relacion						: { type: "string"},
									DocumentoIdentidad				: { type: "string"},
									CodigoPortafolio				: { type: "string"},
									NombreCompleto					: { type: "string"},
									FechaIngreso					: { type: "date"},
							}
						}
					}
				}
			}),
			columns: [
			        {field: "IdOrden",						title: $translate.instant("UI_ORDEN"),					    width: "50px"	},
			        {field: "TipoOperacion",				title: $translate.instant("UI_TIPO_OPERACION"),				width: "180px"	},
			        {field: "Instrumento",					title: $translate.instant("UI_INSTRUMENTO"),				width: "80px"	},
			        {field: "Monto",						title: $translate.instant("UI_MONTO"),	template: "<label> #: (util.bcs_formato(Monto,'ORD','MONTO',IdMoneda,'','')).valor #</label>",						width: "80px"	},
			        {field: "Cantidad",						title: $translate.instant("UI_CANTIDAD"),	template: "<label> #: (util.bcs_formato(Cantidad,'ORD','CANTIDAD',IdMoneda,'','')).valor #</label>",					width: "80px"	},
			        {field: "Precio",						title: $translate.instant("UI_PRECIO"),	template: "<label> #: (util.bcs_formato(Precio,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",						width: "80px"	},
			        {field: "FechaIngreso",				    title: $translate.instant("UI_FECHA_INGRESO"),			    width: "70px"	},
				    {field: "FechaRecepcion",				title: $translate.instant("UI_RECEPCION"),				    width: "70px"	},
				    {field: "FechaAceptacion",				title: $translate.instant("UI_ACEPTACION"),				    width: "70px"	},
				    {field: "Folio",						title: $translate.instant("UI_FOLIO"),					    width: "50px"	},
				    {field: "FechaEjecucion",				title: $translate.instant("UI_EJECUCION"),				    width: "70px"	},
				    {field: "IdBolsa",						title: $translate.instant("UI_BOLSA"),					    width: "50px"	},
				    {field: "EsODUI",						title: $translate.instant("UI_OD"),					   		width: "50px"	},
				    {field: "Relacion",						title: $translate.instant("UI_RELACION"),					width: "150px"	},
				    {field: "PrecioEjecucion",				title: $translate.instant("UI_PRECIO_EJECUCION"),	template: "<label> #: (util.bcs_formato(PrecioEjecucion,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",			width: "80px"	},
				    {field: "MontoTransado",				title: $translate.instant("UI_MONTO_TRANSADO"),	template: "<label> #: (util.bcs_formato(MontoTransado,'ORD','MONTO',IdMoneda,'','')).valor #</label>",		width: "80px"	},
				    {field: "CantidadTransada",				title: $translate.instant("UI_CANTIDAD_TRANSADA"),	template: "<label> #: (util.bcs_formato(CantidadTransada,'ORD','CANTIDAD',IdMoneda,'','')).valor #</label>",			width: "80px"	},
				    {field: "ComisionTransada",				title: $translate.instant("UI_COMISION"),					width: "80px"	},
				    {field: "MejorPrecioMercado",			title: $translate.instant("UI_MEJOR_PRECIO_MERCADO"),	template: "<label> #: (util.bcs_formato(MejorPrecioMercado,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",		width: "80px"	},
				    {field: "PrecioMinimoMercado",			title: $translate.instant("UI_PRECIO_MINIMO_MERCADO"),	template: "<label> #: (util.bcs_formato(PrecioMinimoMercado,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",		width: "80px"	},
				    {field: "PrecioMaximoMercado",			title: $translate.instant("UI_PRECIO_MAXIMO_MERCADO"),	template: "<label> #: (util.bcs_formato(PrecioMaximoMercado,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",		width: "80px"	},
				    {field: "MayorPrecioOperacionCorredor",	title: $translate.instant("UI_MAYOR_PRECIO_OPERACION"),	template: "<label> #: (util.bcs_formato(MayorPrecioOperacionCorredor,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",		width: "80px"	},
				    {field: "MenorPrecioOperacionCorredor",	title: $translate.instant("UI_MENOR_PRECIO_OPERACION"),	template: "<label> #: (util.bcs_formato(MenorPrecioOperacionCorredor,'ORD','PRECIO',IdMoneda,'','')).valor #</label>",		width: "80px"	},
				    //{field: "TipoEjecucion",				title: $translate.instant("UI_TIPO_EJECUCION"),				width: "50px"	},
				    {field: "DocumentoIdentidad",			title: $translate.instant("UI_CLIENTE"),					width: "70px"	},
				    {field: "CodigoPortafolio",				title: $translate.instant("UI_PORTAFOLIO"),					width: "50px"	},
				    {field: "NombreCompleto",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
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
			excel : {
				allPages: true,
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

		/*********************************************
		 * FUNCIONES
		 *********************************************/
	
		
		
		pct.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			//objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			var auxDate1;
    		var auxDate2;

	    	if(pct.FecInicio){
	    			auxDate1 =  util.dateOdata(util.date(pct.FecInicio).hours(0).minutes(0).seconds(0).toDate());
	    			if (pct.PorFechaEjecucion===true){	    			
	    				niv1filters.push({ field: "FechaEjecucion", operator: "gte", value: auxDate1});
	    			}else{
	    				niv1filters.push({ field: "FechaIngreso", operator: "gte", value: auxDate1});
		    		};	    				
	   		
	    	};
	    	
	    	if(pct.FecTermino){
	    		auxDate2 = util.dateOdata(util.date(pct.FecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
	    		if (pct.PorFechaEjecucion===true){
	    			niv1filters.push({ field: "FechaEjecucion", operator: "lte", value: util.dateTerminoDia(auxDate2) });
	    		}else{
	    			niv1filters.push({ field: "FechaIngreso", operator: "lte", value: util.dateTerminoDia(auxDate2) });
	    		};
	    		
	    	};
	    	
	    	
	    	
	    	
			if(pct.SocioDeNegocioObj.IdCliente && pct.SocioDeNegocioObj.IdCliente > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pct.SocioDeNegocioObj.IdCliente });
			};
	    	
			if (pct.IdClasificacion !== undefined && pct.IdClasificacion != "")	{
        		niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "contains", value:pct.IdClasificacion });
			};
			
			if (pct.IdTipoInstrumento !== undefined && pct.IdTipoInstrumento != "")	{
        		niv1filters.push({ field: "IdTipoInstrumento", operator: "contains", value:pct.IdTipoInstrumento });
			};
			
			if (pct.instrumento	!== undefined){
	      		if (pct.instrumento.IdInstrumento > 0) {
        			niv1filters.push({ field: "IdInstrumento", operator: "eq", value: Number(pct.instrumento.IdInstrumento) });
       			};
			};
	    	if (niv1filters.length>0) pct.grilla.EjecucionOrdenes.dataSource.filter(objfilters);
	    	else pct.grilla.EjecucionOrdenes.dataSource.filter(new Array());
	    	
	    	//pct.grilla.EjecucionOrdenesSel = undefined;
			
			
				    	
		};
		
		
		
			
		pct.fn.limpiar = function(){
			
			
			pct.grilla.EjecucionOrdenes.dataSource.removeAll();
		};
		
		$scope.$watch('pct.PorFechaIngreso',function(newval,oldval){
			switch (newval){
				case true:  pct.PorFechaEjecucion = false; break;					
				case false: pct.PorFechaEjecucion = true;  break; 
			}
		
		});	
		$scope.$watch('pct.PorFechaEjecucion',function(newval,oldval){
			switch (newval){
				case true:  pct.PorFechaIngreso = false; break;					
				case false: pct.PorFechaIngreso = true;  break;  
			}
			
		});

	});
}());