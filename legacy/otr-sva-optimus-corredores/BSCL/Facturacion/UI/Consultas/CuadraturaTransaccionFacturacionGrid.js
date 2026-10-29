angular.module('ModuloFacturacion').factory('bcsCuadraturaTransaccionGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};
	
	//Grilla Transaccion
	transporteCuadraturaTransaccion = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1723",
				dataType : "json"
			}
		};
	};
	
	modeloCuadraturaTransaccion = function() {
		return {
			model: {
				fields : {					
					IdTransaccion  	           : { type: "number"},
					TienePendientes			   : { type: "string"},
	        		FolioTransaccion  	       : { type: "string"},
	        		Cantidad                   : { type: "number"},
	        		Precio                     : { type: "number"},
	        		Monto                      : { type: "number"},
	        		CantidadAsignada           : { type: "number"},
	        		CantidadPorAsignar         : { type: "number"},
	        		CantidadEnviada            : { type: "number"},
	        		CantidadPorEnviar          : { type: "number"},
	        		CantidadFacturada          : { type: "number"},
	        		CantidadPorFacturar        : { type: "number"},
	        		MontoFacturado             : { type: "number"},
	        		MontoPorFacturar           : { type: "number"},
	        		NombreBolsa                : { type: "string"},
	        		TipoOperacion              : { type: "string"},
	        		FechaTransaccion           : { type: "sysdate"},	        		
	        		IdCondicionLiquidacion     : { type: "string"},
	        		IdMoneda                   : { type: "string"},
	        		NombreTipoInstrumento      : { type: "string"},
	        		NemotecnicoInstrumento     : { type: "string"},
	        		NombreCliente              : { type: "string"},
	        		DocumentoIdentidadCliente  : { type: "string"},
	        		IdPortafolioCliente        : { type: "number"},
	        		CodigoPortafolioCliente    : { type: "string"},
	        		TipoCliente                : { type: "string"},
	        		NombrePortafolio           : { type: "string"},
				}
			}
		};
	};	
	
	
	obj.dataSourceCuadraturaTransaccion = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            //groupable 			: true, 
			            sort 				: { field: "FolioTransaccion", dir: "asc" }, // Criterios de Ordenamiento.
			            pageSize 			: 25, // Número de registros por página.
			            transport 			: transporteCuadraturaTransaccion(),
			            schema 				: modeloCuadraturaTransaccion(),			
	                    aggregate			: [ //Para resumen de totales de transaccion
	                 	  		                { field: "Monto", aggregate: "sum" },
						                        { field: "CantidadAsignada", aggregate: "sum" },
						                        { field: "CantidadPorAsignar", aggregate: "sum" },
						                        { field: "CantidadEnviada", aggregate: "sum" },
						                        { field: "CantidadPorEnviar", aggregate: "sum" },
						                        { field: "CantidadFacturada", aggregate: "sum" },
						                        { field: "CantidadPorFacturar", aggregate: "sum" },
						                        { field: "MontoFacturado", aggregate: "sum" },
						                        { field: "MontoPorFacturar", aggregate: "sum" },				
                                              ]
		              }
	});	
	
	obj.columnasCuadraturaTransaccion = function() { 
		return  [
		     {field: "IdTransaccion", 	    	title: "UI_SECUENCIA_TRANSACCION", 	width: "75px",  align: "right", hidden : true},
	   	     {field: "FolioTransaccion", 		title: "UI_FOLIO_TRANSACCION", 		width: "75px",  align: "left"},
	   	     {field: "TienePendientes", 		title: "UI_MODIFICACION_ANULACION", width: "120px", align: "center"},
	   	     {field: "Cantidad",    	    	title: "UI_CANTIDAD",          		width: "60px",  align: "right", template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad','CLP','','')).valor #</label>"},   	     
	   	     {field: "Precio",    	        	title: "UI_PRECIO",            		width: "60px",  align: "right", template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio','CLP',IdTipoInstrumento,'')).valor #</label>"},
	   	     {field: "Monto",        	    	title: "UI_MONTO",             		width: "70px",  align: "right", template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>",                     footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},	   	     
	   	     {field: "CantidadAsignada",    	title: "UI_CANTIDAD_ASIGNADA",     	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadAsignada,'GEN','Cantidad','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "CantidadPorAsignar",  	title: "UI_CANTIDAD_POR_ASIGNAR",  	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadPorAsignar,'GEN','Cantidad','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "CantidadEnviada",     	title: "UI_CANTIDAD_ENVIADA",      	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadEnviada,'GEN','Cantidad','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "CantidadPorEnviar",  		title: "UI_CANTIDAD_POR_ENVIAR",   	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadPorEnviar,'GEN','Cantidad','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "CantidadFacturada",   	title: "UI_CANTIDAD_FACTURADA",    	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadFacturada,'GEN','Cantidad','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "CantidadPorFacturar", 	title: "UI_CANTIDAD_POR_FACTURAR", 	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadPorFacturar,'GEN','Cantidad','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Cantidad','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "MontoFacturado",    		title: "UI_MONTO_FACTURADO",       	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(MontoFacturado,'GEN','Monto','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "MontoPorFacturar",    	title: "UI_MONTO_POR_FACTURAR",    	width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(MontoPorFacturar,'GEN','Monto','CLP','','')).valor #</label>", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:1px;"}},
	   	     {field: "NombreBolsa", 			title: "UI_BOLSA",             		width: "180px",  align: "left"},
	   	     {field: "FechaTransaccion", 		title: "UI_FECHA_TRANSACCION", 		width: "75px"},	   	     
	   	     {field: "NombreCliente",  	        title: "UI_NOMBRE_CLIENTE",  		width: "200px",  align: "left"},
	   	     {field: "DocumentoIdentidadCliente", title: "UI_DOCUMENTO_IDENTIDAD",  width: "100px",  align: "left"},
	   	     {field: "CodigoPortafolioCliente", title: "UI_PORTAFOLIO",             width: "80px",  align: "left"},
	   	     {field: "TipoCliente",             title: "UI_TIPO_CLIENTE",           width: "100px",  align: "left"},
	   	     {field: "IdCondicionLiquidacion", 	title: "UI_CONDICION_LIQUIDACION",  width: "75px",  align: "left"},
	   	     {field: "IdMoneda",    	       	title: "UI_MONEDA",           		width: "60px",  align: "left"},
	   	     {field: "TipoOperacion", 			title: "UI_TIPO_OPERACION",			width: "200px",  align: "left", groupable: true},
	   	     {field: "NombreTipoInstrumento",  	title: "UI_TIPO_INSTRUMENTO",  		width: "120px",  align: "left"},	   	     
	   	     {field: "NemotecnicoInstrumento", 	title: "UI_INSTRUMENTO",       		width: "100px",  align: "left", groupable: true},
	   	     {field: "NombrePortafolio",  		title: "UI_NOMBRE_PORTAFOLIO",      width: "140px",  align: "left", groupable: true},
		];
	};
	
	
	//Grilla Transaccion Detalle
	transporteCuadraturaTransaccionDetalle = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1725",				
				dataType : "json"
			}
		};
	};
	
	modeloCuadraturaTransaccionDetalle = function() {
		return {
			model: {
				fields : {
	        		CantidadAsignada           : { type: "number"},
	        		CantidadEnviada            : { type: "number"},
	        		CantidadFacturada          : { type: "number"},
	        		MontoFacturado             : { type: "number"},
	        		IdComprobante              : { type: "number"},
	        		IdComprobanteReferencia    : { type: "number"},
	        		TipoComprobante            : { type: "string"},	        		
	        		IdPortafolio               : { type: "number"},
	        		DocumentoIdentidad         : { type: "string"},
	        		NombreCliente              : { type: "string"},
	        		IdEstadoFacturacion        : { type: "string"},
	        		NombreEstadoFacturacion    : { type: "string"},
				}
			}
		};
	};
	
	definicionCuadraturaTransaccionDetalle = function(inFiltros) 
	   {
		  return { 
		      serverFiltering : true,
		      serverSorting   : true, 
		      sort 			  : { field: "IdComprobante", dir: "asc" }, // Criterios de Ordenamiento.
			  pageSize 		  : 10, // Número de registros por página.
			  transport 	  : transporteCuadraturaTransaccionDetalle(),
			  schema 		  : modeloCuadraturaTransaccionDetalle(),              
			  filter		  : inFiltros
		  };
	   };
	   
	obj.dataSourceCuadraturaTransaccionDetalle = function(filtrosCuadraturaTransaccionDetalle) 
	   {
		  return new bcs.DataSource(
		    {
			   type 		: "odata",
			   definition 	: definicionCuadraturaTransaccionDetalle(filtrosCuadraturaTransaccionDetalle)
		    });
	   };
	   
	obj.columnasCuadraturaTransaccionDetalle = function() {
		return  [	   	     
	   	     {field: "CantidadAsignada",    title: "UI_CANTIDAD_ASIGNADA",    width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadAsignada,'GEN','Cantidad','CLP','','')).valor #</label>" },
	   	     {field: "CantidadEnviada",     title: "UI_CANTIDAD_ENVIADA",     width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadEnviada,'GEN','Cantidad','CLP','','')).valor #</label>" },
	   	     {field: "CantidadFacturada",   title: "UI_CANTIDAD_FACTURADA",   width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(CantidadFacturada,'GEN','Cantidad','CLP','','')).valor #</label>" },
	   	     {field: "MontoFacturado",    	title: "UI_MONTO_FACTURADO",       width: "100px",  align: "right", template : " <label> #:  (util.bcs_formato(MontoFacturado,'GEN','Monto','CLP','','')).valor #</label>"},
	   	     {field: "IdComprobante", 	    title: "UI_SECUENCIA_COMPROBANTE", width: "75px",  align: "right"},
	   	     {field: "IdComprobanteReferencia", title: "UI_REFERENCIA_COMPROBANTE", width: "75px",  align: "right"},
	   	     {field: "TipoComprobante",    	title: "UI_TIPO_COMPROBANTE",     width: "120px",  align: "left"},	   	     
	   	     {field: "IdPortafolio",    	title: "UI_PORTAFOLIO",        width: "65px",  align: "right"},
	   	     {field: "DocumentoIdentidad", 	title: "UI_RUT_CLIENTE",       width: "75px",  align: "left"},	   	     	   	     
	   	     {field: "NombreCliente",    	title: "UI_NOMBRE",            width: "150px",  align: "left"},
	   	     {field: "NombreEstadoFacturacion", title: "UI_ESTADO_MOVIMIENTO", width: "150px",  align: "left"},
		];
	};
	
	
	
	
	
	
    
	return obj;
    
}]);