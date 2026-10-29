		
angular.module('ModuloFacturacion').factory('bcsMovimientoFacturarGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	//Formateo exportacion a excel
    var excelFormat = function(field, field2) {
    	return function (item) {
			return util.bcs_formato(item[field],'GEN','Monto',item[field2],'','').excelFormat;
		}; 
    }
    
	
	//MOVIMIENTOS A FACTURAR 
	transporteMovimientoFacturar = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1235",
				dataType : "json"
			}
		};
	};	
	
	
	modeloMovimientoFacturar = function() {
		return {
			model: {
				fields : {
					AccionTipoOperacion 			: { type: "string"},
					DocumentoIdentidad 				: { type: "string"},
					Cantidad 						: { type: "number", excelFormat: excelFormat('Cantidad', 'IdMonedaMovimientoComprobante')},
					CodigoUsuarioBloqueo            : { type: "string"},
					CodigoPortafolio 				: { type: "string"},
					CodigoBolsa 					: { type: "string"},
					CodigoBoveda 					: { type: "string"},
					CodigoBroker 					: { type: "string"},
					CodigoBancoCustodio				: { type: "string"},
					CodigoBancoLiquidador			: { type: "string"},
					CodigoCorredorCorresponsal		: { type: "string"},
					ComisionBVLMonedaMovimiento		: { type: "number"},
					ComisionCavaliMonedaMovimiento	: { type: "number"},
					ComisionSMVMonedaMovimiento		: { type: "number"},
					ComisionSABMonedaMovimiento		: { type: "number"},
					COMMVTOCMBTE					: { type: "number", excelFormat: excelFormat('COMMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					DERMVTOCMBTE					: { type: "number", excelFormat: excelFormat('DERMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					DiasPlazo                       : { type: "number"},			  
					EsAPlazo                        : { type: "string"},
					EsClienteFinal					: { type: "string"},
					EsConvenioDerechBCS				: { type: "string"},
					EsConvenioDerechBEC				: { type: "string"},					
					EsVentaCorta 					: { type: "string"},
					EsComprobIndividual             : { type: "string"},
					EsAPlazo                        : { type: "string"}, 
					FolioTransaccion 				: { type: "number"},
					FechaTransaccion 				: { type: "sysdate"},
					FechaLiquidacion                : { type: "sysdate"},
					FechaVencimiento 				: { type: "sysdate"},
					FechaMovimientoComprobante		: { type: "sysdate"},
					FondoLiquidacionMonedaMovimiento: { type: "number"},
					FondoGarantiaMonedaMovimiento	: { type: "number"},
					GASMVTOCMBTE					: { type: "number", excelFormat: excelFormat('GASMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					Glosa                           : { type: "string"},
					IdClasificacionTipoInstrumento 	: { type: "string"},
					IdCliente 						: { type: "number"},
					IdComprobante                   : { type: "number"},
	        		IdComprobanteReferencia         : { type: "number"},
					IdConceptoMovimiento 			: { type: "string"},
					IdCondicionLiquidacion 			: { type: "string"},
					IdCorredoraContraparteString 	: { type: "number"},
					IdEmisor						: { type: "string"},
					IdEstadoFacturacion 			: { type: "string"},					
					IdOrdenString 					: { type: "number"},
					IdMoneda 						: { type: "string"},
					IdMonedaMovimientoComprobante   : { type: "string"},
					IdMonedaLiquidacion             : { type: "string"},					
					IdMovimientoComprobante         : { type: "number"},
					IdPortafolio 					: { type: "number"},
					IdTipoComprobante 				: { type: "string"},
					IdTipoCliente	 				: { type: "string"},
					IdTipoReajuste					: { type: "string"},
					ISIN							: { type: "string"},
					IndicadorOrdenDirecta           : { type: "string"},
					IdMonedaMovimientoComprobanteCobro : { type: "string"},
					IdFormaCustodia	                : { type: "string"},
					NombreFormaCustodia             : { type: "string"},
					IdMonedaCobro 					: { type: "string"},
					Intereses						: { type: "number"},
					ImpuestoIGVMonedaMovimiento		: { type: "number"},
					IMPMVTOCMBTE					: { type: "number", excelFormat: excelFormat('IMPMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					LiquidaBancoCustodio			: { type: "string"},
					MercadoSMV						: { type: "string"},
					ModalidadSMV					: { type: "string"},
					Monto 							: { type: "number", excelFormat: excelFormat('Monto', 'IdMoneda')},									
					MontoMovimientoComprobante      : { type: "number", excelFormat: excelFormat('MontoMovimientoComprobante', 'IdMonedaMovimientoComprobante')},									
					MontoLiquidacion                : { type: "number", excelFormat: excelFormat('MontoLiquidacion', 'IdMonedaLiquidacion')},									
					MontoMovimientoComprobanteCobro : { type: "number", excelFormat: excelFormat('MontoMovimientoComprobanteCobro', 'IdMonedaMovimientoComprobanteCobro')},
					MontoMovimientoComprobanteFinal : { type: "number", excelFormat: excelFormat('MontoMovimientoComprobanteFinal', 'IdMonedaMovimientoComprobante')},	
					NombreEstadoFacturacion 		: { type: "string"},
					NombreEstadoCavali				: { type: "string"},
					NombreLargoCliente 				: { type: "string"},
					NombreTipoOperacion             : { type: "string"},
					NombreTipoCliente				: { type: "string"},
					NombreTipoProducto 				: { type: "string"},
					NombreTipoInstrumento			: { type: "string"},
					NemotecnicoInstrumentoUnificado	: { type: "string"},
					NombreConceptoMovimiento 		: { type: "string"},
					NombreCondicionDeLiquidacion 	: { type: "string"},
					OtrasComisionesMonedaMovimiento	: { type: "number"},
					
					Precio 							: { type: "number", excelFormat: excelFormat('Precio', 'IdMoneda')},
					PrecioAPlazo                    : { type: "number", excelFormat: excelFormat('PrecioAPlazo', 'IdMoneda')},
					RazonSocialEmisor               : { type: "string"},
					ResumenMedioPago                : { type: "string"},
					ReajustesVAC					: { type: "number"},
					RETMVTOCMBTE					: { type: "number", excelFormat: excelFormat('RETMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					ValorRescate					: { type: "number"},
					ValorTipoCambio					: { type: "number", excelFormat: excelFormat('Monto', 'IdMoneda')},
					Tasa 							: { type: "number"},
					TasaOperacion					: { type: "number"},   // Para Venta corta					
					CodigoIFRS						: { type: "string"},
					NemoSVS							: { type: "string"},
					IdTipoCanalIngreso				: { type: "string"}, // canal de Origen MGH 
					TipoLiquidacion 				: { type: "string"},
					ModPrefFactDescripcion			: { type: "string"},
					FolioSpot						: { type: "number"},
					EstadoSpot						: { type: "string"},
					SecuenciaComprobanteSpot		: { type: "number"},
					CodigoOperador					: { type: "string"},
					IdAsignacionMasiva				: { type: "number"},
					TipoAsignacion					: { type: "string"},
					MarcaAsignacion 				: { type: "string"},
				}
			}
		};
	};
	
		
	
	definicionODataMovimientoFacturar = function() {
		return {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdMovimientoComprobante", dir: "desc" }, // Criterios de Ordenamiento.
			pageSize 			: 25, // Número de registros por página.
			transport 			: transporteMovimientoFacturar(),
			schema 				: modeloMovimientoFacturar(),			
	        aggregate			: [ //Para resumen de totales de cobro
	                 			      { field: "Monto", aggregate: "sum" },
						              { field: "COMMVTOCMBTE", aggregate: "sum" },			    	        
						              { field: "DERMVTOCMBTE", aggregate: "sum" },
						              { field: "GASMVTOCMBTE", aggregate: "sum" },
						              { field: "IMPMVTOCMBTE", aggregate: "sum" },
						              { field: "RETMVTOCMBTE", aggregate: "sum" },				
           ]
		};
	}
	
	obj.dataSourceMovimientoFacturar = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdMovimientoComprobante", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 50, // Número de registros por página.
			            transport 			: transporteMovimientoFacturar(),
			            schema 				: modeloMovimientoFacturar(),			
	                    aggregate			: [ //Para resumen de totales de cobro
	                 	  		                { field: "Monto", aggregate: "sum" },
	                 	  		                { field: "MontoMovimientoComprobante", aggregate: "sum" },
	                 	  		                { field: "MontoLiquidacion", aggregate: "sum" },
	                 	  		                { field: "MontoMovimientoComprobanteFinal", aggregate: "sum" },	                 	  		                
						                        { field: "COMMVTOCMBTE", aggregate: "sum" },			    	        
						                        { field: "DERMVTOCMBTE", aggregate: "sum" },
						                        { field: "GASMVTOCMBTE", aggregate: "sum" },
						                        { field: "IMPMVTOCMBTE", aggregate: "sum" },
						                        { field: "RETMVTOCMBTE", aggregate: "sum" },				
                                              ]
		              }
	});
	
	
	obj.columnasMovimientoFacturar = function() {
		var functionFormato = function(uso,moneda) {
			return function (value, item) {
				return util.bcs_formato(value,item.CodigoTipoProducto==='MEME'?'SPT':'GEN',uso,item[moneda],'','');
    	    }
		}
		
		var IdMovimientoComprobante 		= {field: "IdMovimientoComprobante", 		title: "UI_SECUENCIA_MOVIMIENTO",    width: "75px"};
		var IdComprobante 					= {field: "IdComprobante",          		title: "UI_SECUENCIA_COMPROBANTE",   width: "75px"};
		var IdComprobanteReferencia 		= {field: "IdComprobanteReferencia",  	    title: "UI_REFERENCIA_COMPROBANTE",  width: "75px"};
		var IdClasificacionTipoInstrumento	= {field: "IdClasificacionTipoInstrumento", title: "UI_CLASIFICACION", 		     width: "62px", groupable: true };
		var IdTipoComprobante 				= {field: "IdTipoComprobante", 				title: "UI_TIPO_COMPROBANTE", 		 width: "55px", groupable: true , template: "<span class='flags' title='#=NombreTipoComprobante #'>#=IdTipoComprobante#</span>" };
		var NombreConceptoMovimiento		= {field: "NombreConceptoMovimiento", 		title: "UI_CONCEPTO", 	             width: "180px", groupable: true , template: "<span class='flags' title='#=NombreConceptoMovimiento #'>#=NombreConceptoMovimiento#</span>", attributes:{style:"white-space:nowrap;"} };
		var NombreEstadoFacturacion 		= {field: "NombreEstadoFacturacion", 		title: "UI_ESTADO", 				 width: "70px", groupable: true };
		var CodigoUsuarioBloqueo 			= {field: "CodigoUsuarioBloqueo",    		title: "UI_USUARIO_BLOQUEO",		 width: "70px", attributes:{style:"white-space:nowrap;"} };
		var IdFormaCustodia 				= {field: "IdFormaCustodia",    			title: "UI_FORMA_CUSTODIA",			 width: "80px", hidden: true};
		var NombreFormaCustodia 			= {field: "NombreFormaCustodia",  			title: "UI_FORMA_CUSTODIA",			 width: "80px" };
		var IndicadorOrdenDirecta 			= {field: "IndicadorOrdenDirecta",          title: "UI_INDICADOR_ORDEN_DIRECTA", width: "80px",		sortable : true };
		var CodigoBolsa                     = {field: "CodigoBolsa",       		        title: "UI_BOLSA",      		     width: "60px" };
		var CodigoBoveda                    = {field: "CodigoBoveda",       	        title: "UI_BOVEDA",      		     width: "60px" };
		var FolioTransaccion 				= {field: "FolioTransaccion", 				title: "UI_TRANSACCION", 			 width: "80px"};
		var FechaTransaccion 				= {field: "FechaTransaccion", 				title: "UI_FECHA_TRANSACCION",	     width: "80px",		groupable: true };
		var IdOrdenString 					= {field: "IdOrdenString", 					title: "UI_ORDEN", 				     width: "65px"};
		var NombreLargoCliente 				= {field: "NombreLargoCliente", 			title: "UI_CLIENTE", 				 width: "200px", 	groupable: true };
		var CodigoPortafolio 				= {field: "CodigoPortafolio", 				title: "UI_PORTAFOLIO", 			 width: "70px", 	groupable: true };
		var DocumentoIdentidad 				= {field: "DocumentoIdentidad", 			title: "UI_DOCUMENTO_IDENTIDAD",     width: "80px", 	groupable: true };
		var EsConvenioDerechBCS 			= {field: "EsConvenioDerechBCS",			title: "UI_CONVENIO_DERECHO_BCS",	width: "75px", 	groupable: true };
		var EsConvenioDerechBEC 			= {field: "EsConvenioDerechBEC",			title: "UI_CONVENIO_DERECHO_BEC",	width: "75px", 	groupable: true };			
		var CodigoOperador					= {field: "CodigoOperador",					title: "UI_OPERADOR",	 			width: "75px", 	    groupable: true };
		var MarcaAsignacion 				= {field: "MarcaAsignacion",				title: "UI_MARCA_ASIGNACION",	 			width: "75px", 	    groupable: true };
		
		var COMMVTOCMBTE = {field: "COMMVTOCMBTE",	title: "UI_COMISION",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
    	    }
		};	
		var DERMVTOCMBTE = {field: "DERMVTOCMBTE",	title: "UI_DERECHOS", 	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},  
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
    	    }
		};
		var GASMVTOCMBTE = {field: "GASMVTOCMBTE",	title: "UI_GASTOS",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},  
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		};
		var IMPMVTOCMBTE = {field: "IMPMVTOCMBTE",	title: "UI_IMPUESTO",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		};
		var RETMVTOCMBTE = {field: "RETMVTOCMBTE",	title: "UI_RETENCION",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}, 
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		};
		
		
		var AccionTipoOperacion 			= {field: "AccionTipoOperacion", 			title: "UI_OPERACION", 				 width: "80px", 	groupable: true };
		var NombreTipoOperacion 			= {field: "NombreTipoOperacion",             title: "UI_TIPO_OPERACION", 		 width: "200px",	groupable: true };
	   	var FechaMovimientoComprobante 		= {field: "FechaMovimientoComprobante",		title: "UI_FECHA_MOVIMIENTO", 		 width: "80px",  groupable: true };
		var NombreTipoProducto 				= {field: "NombreTipoProducto", 			title: "UI_TIPO_PRODUCTO", 		     width: "200px",  groupable: true };
		var NombreTipoInstrumento 			= {field: "NombreTipoInstrumento", 			title: "UI_TIPO_INSTRUMENTO",	 	 width: "120px", groupable: true };
		var NemotecnicoInstrumentoUnificado = {field: "NemotecnicoInstrumentoUnificado", title: "UI_NEMOTECNICO", 			 width: "120px", groupable: true };
		var RazonSocialEmisor 				= {field: "RazonSocialEmisor"              , title: "UI_EMISOR",     			 width: "120px", groupable: true };
		var Cantidad 						= {field: "Cantidad",                        title: "UI_CANTIDAD",                width: "80px",  sortable : true,
			format : function (value, item) {
				if (item.CodigoTipoProducto === 'MEME'){
					return util.bcs_formato(value,'SPT','Monto',item.IdMonedaSpot,'','');
				}
				return util.bcs_formato(value,'GEN','Cantidad',item.IdMonedaMovimientoComprobante,'','');
		     }
		};
		var Precio = {field: "Precio",	title: "UI_PRECIO",	width: "80px", sortable : true, 
			format : function (value, item) {
				if (item.CodigoTipoProducto === 'MEME'){
					return util.bcs_formato(value,'SPT','Precio',item.IdMoneda,'','');
				}
			
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}
		}
		var ValorRescate = {field: "ValorRescate",                    title: "UI_VALOR_RESCATE",           width: "80px",sortable : true, 
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',(item.IdTipoReajuste!== '' ? item.IdTipoReajuste : item.IdMonedaLiquidacion),'','');
			}
		}
		var ValorTipoCambio 	= {field: "ValorTipoCambio",    title: "UI_TIPO_CAMBIO",    width: "100px",format : functionFormato('Monto','IdMoneda'),sortable : true};
		var Monto 				= {field: "Monto",				title: "UI_MONTO",			width: "100px",format : functionFormato('Monto','IdMoneda'),sortable : true, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IdMoneda 			= {field: "IdMoneda", 			title: "UI_MONEDA", 		width: "80px", groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMoneda#</span>" }			
		var MontoMovimientoComprobante = {field: "MontoMovimientoComprobante",      title: "UI_MONTO_LOCAL",             width: "100px",sortable : true, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}, 
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		}
		var IdMonedaMovimientoComprobante = {field: "IdMonedaMovimientoComprobante", 	title: "UI_MONEDA_LOCAL",            width: "80px", groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaMovimientoComprobante#</span>" }
		var MontoLiquidacion 				= {field: "MontoLiquidacion",				title: "UI_MONTO_LIQUIDACION",       width: "100px", format : functionFormato('Monto','IdMonedaLiquidacion'),sortable : true, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IdMonedaLiquidacion			 	= {field: "IdMonedaLiquidacion", 			title: "UI_MONEDA_LIQUIDACION",      width: "80px",	groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaLiquidacion#</span>" }
		var Tasa 							= {field: "Tasa", 							title: "UI_TASA", 				     width: "80px",
			format : function (value, item) {
				return util.bcs_formato(item.Tasa, item.CodigoTipoProducto==='MEME'?'SPT':'GEN', 'Tasa', item.IdMonedaLiquidacion,item.IdTipoInstrumento,'');
		     }
		}
		var TasaVentaCorta 					= {field: "TasaOperacion", 				title: "UI_TASA_VENTA_CORTA", 			width: "80px", 
			format: function (value, item) {
				return util.bcs_formato(item.TasaOperacion, item.CodigoTipoProducto === 'MEME' ? 'SPT' : 'GEN', 'Tasa', item.IdMonedaLiquidacion,'','');
			}
		}
		
		var NombreCondicionDeLiquidacion 	= {field: "NombreCondicionDeLiquidacion", 	title: "UI_CONDICION_LIQUIDACION",   width: "100px", groupable: true }			
		var FechaLiquidacion 				= {field: "FechaLiquidacion",               title: "UI_FECHA_LIQUIDACION",       width :"80px",	groupable: true }
		var FechaVencimiento 				= {field: "FechaVencimiento",               title: "UI_FECHA_VENCIMIENTO",       width :"80px",	groupable: true }
		var CodigoBolsa 					= {field: "CodigoBolsa", 					title: "UI_BOLSA", 				     width: "70px",	groupable: true }
		var IdCorredoraContraparteString 	= {field: "IdCorredoraContraparteString", 	title: "UI_CORREDOR_CONTRAPARTE",    width: "80px",	groupable: true }
		var EsVentaCorta 					= {field: "EsVentaCorta", 					title: "UI_VENTA_CORTA", 			 width: "75px",	groupable: true }
		var EsComprobIndividual 			= {field: "EsComprobIndividual", 			title: "UI_COMPROBANTE_INDIVIDUAL",  width: "75px",	groupable: true }			
		var EsAPlazo 						= {field: "EsAPlazo",             			title: "UI_A_PLAZO",    			 width: "50px",	groupable: true }
		var Glosa 							= {field: "Glosa",             			    title: "UI_GLOSA",       			 width: "250px" }
		var ResumenMedioPago 				= {field: "ResumenMedioPago", 	            title: "UI_MEDIO_PAGO",              width: "200px" }
		var PrecioAPlazo 					= {field: "PrecioAPlazo",  					title: "UI_PRECIO_PLAZO", 			 width: "80px",
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}
		};
		var CodigoCorredorCorresponsal		= {field: "CodigoCorredorCorresponsal",		title: "UI_CORREDOR_CORRESPONSAL",	 width: "80px"}
		
		
		var COMMVTOCMBTE = {field: "COMMVTOCMBTE",	title: "UI_COMISION",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};	
		var DERMVTOCMBTE = {field: "DERMVTOCMBTE",	title: "UI_DERECHOS", 	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var GASMVTOCMBTE = {field: "GASMVTOCMBTE",	title: "UI_GASTOS",		width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IMPMVTOCMBTE = {field: "IMPMVTOCMBTE",	title: "UI_IMPUESTO",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var RETMVTOCMBTE = {field: "RETMVTOCMBTE",	title: "UI_RETENCION",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		
		var DiasPlazo 							= {field: "DiasPlazo",  					    title: "UI_DIAS_PLAZO", 			 width: "80px"}			
		var MontoMovimientoComprobanteCobro		= {field: "MontoMovimientoComprobanteCobro", title: "UI_MONTO_TOTAL_LOCAL_COBRO", width: "100px", sortable : true,format : functionFormato('Monto','IdMonedaMovimientoComprobante')};	
		var IdMonedaMovimientoComprobanteCobro	= {field: "IdMonedaMovimientoComprobanteCobro",	title: "UI_MONEDA_COBRO_LOCAL",      width: "80px", groupable: true, template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaMovimientoComprobanteCobro#</span>" }
		var MontoMovimientoComprobanteFinal		= {field: "MontoMovimientoComprobanteFinal", 	title: "UI_MONTO_TOTAL_LOCAL",       width: "100px", sortable : true,format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};	
		
		var NombreTipoCliente 		= {field: "NombreTipoCliente",		title: "UI_TIPO_CLIENTE", width: "150px"};
		var EsClienteFinal 			= {field: "EsClienteFinal",			title: "UI_CLIENTE_FINAL", width: "80px"};
		var CodigoBroker 			= {field: "CodigoBroker",			title: "UI_BROKER", width: "80px"};
		var LiquidaBancoCustodio 	= {field: "LiquidaBancoCustodio",	title: "UI_LIQUIDA_BANCO_CUSTODIO", width: "80px"};
		var CodigoBancoCustodio 	= {field: "CodigoBancoCustodio",	title: "UI_BANCO_CUSTODIO", width: "80px"};
		var CodigoBancoLiquidador 	= {field: "CodigoBancoLiquidador",	title: "UI_BANCO_LIQUIDADOR", width: "80px"};
		var ISIN 					= {field: "ISIN",					title: "UI_ISIN", width: "80px"};
		var MercadoSMV 				= {field: "MercadoSMV",				title: "UI_MERCADO_SMV", width: "80px"};
		var ModalidadSMV			= {field: "ModalidadSMV",			title: "UI_MODALIDAD_SMV", width: "80px"};
		var NombreEstadoCavali 		= {field: "NombreEstadoCavali",		title: "UI_ESTADO_CAVALI", width: "80px"};
		var ReajustesVAC 			= {field: "ReajustesVAC",			title: "UI_REAJUSTES_VAC", width: "80px",format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var Intereses 				= {field: "Intereses",				title: "UI_INTERESES", width: "80px",format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		
		var ComisionBVLMonedaMovimiento 			= {field: "ComisionBVLMonedaMovimiento",			title: "UI_COMISION_BVL", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ComisionCavaliMonedaMovimiento 			= {field: "ComisionCavaliMonedaMovimiento",			title: "UI_COMISION_CAVALI", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ComisionSMVMonedaMovimiento 			= {field: "ComisionSMVMonedaMovimiento",			title: "UI_COMISION_SMV", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ComisionSABMonedaMovimiento 			= {field: "ComisionSABMonedaMovimiento",			title: "UI_COMISION_SAB", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var FondoLiquidacionMonedaMovimiento 		= {field: "FondoLiquidacionMonedaMovimiento",		title: "UI_FONDO_LIQUIDACION", width: "80px",	format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var FondoGarantiaMonedaMovimiento 			= {field: "FondoGarantiaMonedaMovimiento",			title: "UI_FONDO_GARANTIA", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var OtrasComisionesMonedaMovimiento 		= {field: "OtrasComisionesMonedaMovimiento",		title: "UI_OTRAS_COMISIONES", width: "80px",	format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ImpuestoIGVMonedaMovimiento				= {field: "ImpuestoIGVMonedaMovimiento",			title: "UI_IMPUESTO_IGV", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var CodigoIFRS								= {field: "CodigoIFRS",			title: "UI_CODIGO_IFRS", width: "80px"};
		var TipoReajuste							= {field: "IdTipoReajuste",		title: "UI_TIPO_REAJUSTE", width: "80px"};
		var NemoSVS									= {field: "NemoSVS",			title: "UI_NEMO_SVS",  width: "80px"};		
		var IdTipoCanalIngreso 	                    = {field: "IdTipoCanalIngreso", 	title: "UI_TIPO_CANAL_INGRESO",   width: "100px", groupable: true}; // Canal Origen MGH
		var TipoLiquidacion 	    = {field: "TipoLiquidacion", 	title: "UI_TIPO_LIQUIDACION_COMISION_FACTURA",   width: "90px", groupable: true};
		var ModPrefFactDescripcion 	                    	= {field: "ModPrefFactDescripcion", 	title: "UI_MODO_FACTURACION",   width: "110px", groupable: true};
		
		var FolioSpot 		= {field: "FolioSpot", 		title: "UI_FOLIO_SPOT",    width: "75px"};		
		var EstadoSpot		= {field: "EstadoSpot",		title: "UI_ESTADO_SPOT",  width: "80px"};
		var SecuenciaComprobanteSpot 		= {field: "SecuenciaComprobanteSpot", 		title: "UI_SECUENCIA_COMPROBANTE_SPOT",    width: "75px"};
		
		var IdAsignacionMasiva =  {field: "IdAsignacionMasiva",  title: "UI_ID_ASIGNACION_MASIVA", 	width: "75px",	hidden: true};		
		var TipoAsignacion 	   =  {field: "TipoAsignacion", 	 title: "UI_TIPO_ASIGNACION",		width: "150px",  hidden: true};
		
		if ($rootScope.SesionLocale === 'PE') {
			
			var base = [
				IdMovimientoComprobante,
				IdComprobante,
				IdComprobanteReferencia,
				IdClasificacionTipoInstrumento,
				IdTipoComprobante,
				NombreConceptoMovimiento,
				NombreEstadoFacturacion,
				CodigoUsuarioBloqueo,
				NombreFormaCustodia,				
				IndicadorOrdenDirecta,
				CodigoBolsa,
				CodigoBoveda,
				FolioTransaccion,
				FechaTransaccion,
				IdOrdenString,
				NombreLargoCliente,
				CodigoPortafolio,
				DocumentoIdentidad,
				AccionTipoOperacion,
				NombreTipoOperacion,
				FechaMovimientoComprobante,
				NombreTipoProducto,
				NombreTipoInstrumento,
				NemotecnicoInstrumentoUnificado,
				RazonSocialEmisor,
				Cantidad,
				Precio,
				ValorTipoCambio,
				Monto,
				IdMoneda,
				MontoMovimientoComprobante,
				IdMonedaMovimientoComprobante,
				MontoLiquidacion,
				IdMonedaLiquidacion,
				Tasa,
				NombreCondicionDeLiquidacion,
				FechaLiquidacion,
				FechaVencimiento,
				CodigoBolsa,
				IdCorredoraContraparteString,
				CodigoCorredorCorresponsal,
				EsVentaCorta,
				TasaVentaCorta,
				EsComprobIndividual,
				EsAPlazo,
				Glosa,
				ResumenMedioPago,
				PrecioAPlazo,
				DiasPlazo,
					NombreTipoCliente,
					EsClienteFinal,
					CodigoBroker,
					MercadoSMV,
					ModalidadSMV,
					LiquidaBancoCustodio,
					CodigoBancoCustodio,
					CodigoBancoLiquidador,
					ISIN,
					NombreEstadoCavali,
					ReajustesVAC,
					Intereses,
					ComisionBVLMonedaMovimiento,
					ComisionCavaliMonedaMovimiento,
					ComisionSMVMonedaMovimiento,
					ComisionSABMonedaMovimiento,
					FondoLiquidacionMonedaMovimiento,
					FondoGarantiaMonedaMovimiento,
					OtrasComisionesMonedaMovimiento,
					ImpuestoIGVMonedaMovimiento,
				MontoMovimientoComprobanteCobro,
				IdMonedaMovimientoComprobanteCobro,
				MontoMovimientoComprobanteFinal,
				IdTipoCanalIngreso, // Canal Origen MGH
				CodigoOperador,
				MarcaAsignacion
			];
			
		}

		if ($rootScope.SesionLocale === 'CL') {
						
			
			var base = [
				IdMovimientoComprobante,
				IdComprobante,
				IdComprobanteReferencia,
				IdClasificacionTipoInstrumento,
				IdTipoComprobante,
				NombreConceptoMovimiento,
				NombreEstadoFacturacion,
				CodigoUsuarioBloqueo,
				NombreFormaCustodia,
				IndicadorOrdenDirecta,
				FolioTransaccion,
				FechaTransaccion,
				IdOrdenString,
				NombreLargoCliente,
				CodigoPortafolio,
				DocumentoIdentidad,
				MarcaAsignacion,
				AccionTipoOperacion,
				NombreTipoOperacion,
				FechaMovimientoComprobante,
				NombreTipoProducto,
				NombreTipoInstrumento,
				NemotecnicoInstrumentoUnificado,
				RazonSocialEmisor,
				Cantidad,
				EsConvenioDerechBCS,
				EsConvenioDerechBEC,
				Precio,
				ValorRescate,
				ValorTipoCambio,
				Monto,
				TipoReajuste,
				IdMoneda,
				MontoMovimientoComprobante,
				IdMonedaMovimientoComprobante,
				MontoLiquidacion,
				IdMonedaLiquidacion,
				Tasa,
				NombreCondicionDeLiquidacion,
				FechaLiquidacion,
				FechaVencimiento,
				CodigoBolsa,
				IdCorredoraContraparteString,
				EsVentaCorta,
				TasaVentaCorta,
				EsComprobIndividual,
				EsAPlazo,
				Glosa,
				ResumenMedioPago,
				PrecioAPlazo,
				DiasPlazo,
				COMMVTOCMBTE,
				DERMVTOCMBTE,
				GASMVTOCMBTE,
				IMPMVTOCMBTE,
				RETMVTOCMBTE,
				MontoMovimientoComprobanteCobro,
				IdMonedaMovimientoComprobanteCobro,
				MontoMovimientoComprobanteFinal,
				CodigoIFRS,
				NemoSVS,
				IdTipoCanalIngreso, // Canal Origen MGH
				TipoLiquidacion,
				ModPrefFactDescripcion,
				FolioSpot,
				EstadoSpot,
				SecuenciaComprobanteSpot,
				CodigoOperador,
				IdAsignacionMasiva,
				TipoAsignacion
			];
			
				
			
		}
		
		return base;
		
	};

	obj.columnasModificarMovimiento = function() {
		var functionFormato = function(uso,moneda) {
			return function (value, item) {
				return util.bcs_formato(value,item.CodigoTipoProducto==='MEME'?'SPT':'GEN',uso,item[moneda],'','');
    	    }
		}
		
		var IdCobro	 						= {field: "IdCobro", 						title: "UI_COBRO",    width: "75px", hidden: true};
		var IdMovimientoComprobante 		= {field: "IdMovimientoComprobante", 		title: "UI_SECUENCIA_MOVIMIENTO",    width: "75px"};
		var IdComprobante 					= {field: "IdComprobante",          		title: "UI_SECUENCIA_COMPROBANTE",   width: "75px"};
		var IdComprobanteReferencia 		= {field: "IdComprobanteReferencia",  	    title: "UI_REFERENCIA_COMPROBANTE",  width: "75px"};
		var IdClasificacionTipoInstrumento	= {field: "IdClasificacionTipoInstrumento", title: "UI_CLASIFICACION", 		     width: "62px", groupable: true };
		var IdTipoComprobante 				= {field: "IdTipoComprobante", 				title: "UI_TIPO_COMPROBANTE", 		 width: "55px", groupable: true , template: "<span class='flags' title='#=NombreTipoComprobante #'>#=IdTipoComprobante#</span>" };
		var NombreConceptoMovimiento		= {field: "NombreConceptoMovimiento", 		title: "UI_CONCEPTO", 	             width: "180px", groupable: true , template: "<span class='flags' title='#=NombreConceptoMovimiento #'>#=NombreConceptoMovimiento#</span>", attributes:{style:"white-space:nowrap;"} };
		var NombreEstadoFacturacion 		= {field: "NombreEstadoFacturacion", 		title: "UI_ESTADO", 				 width: "70px", groupable: true };
		var CodigoUsuarioBloqueo 			= {field: "CodigoUsuarioBloqueo",    		title: "UI_USUARIO_BLOQUEO",		 width: "70px", attributes:{style:"white-space:nowrap;"} };
		var IdFormaCustodia 				= {field: "IdFormaCustodia",    			title: "UI_FORMA_CUSTODIA",			 width: "80px", hidden: true};
		var NombreFormaCustodia 			= {field: "NombreFormaCustodia",  			title: "UI_FORMA_CUSTODIA",			 width: "80px" };
		var IndicadorOrdenDirecta 			= {field: "IndicadorOrdenDirecta",          title: "UI_INDICADOR_ORDEN_DIRECTA", width: "80px",		sortable : false };
		var CodigoBolsa                     = {field: "CodigoBolsa",       		        title: "UI_BOLSA",      		     width: "60px" };
		var CodigoBoveda                    = {field: "CodigoBoveda",       	        title: "UI_BOVEDA",      		     width: "60px" };
		var FolioTransaccion 				= {field: "FolioTransaccion", 				title: "UI_TRANSACCION", 			 width: "80px"};
		var FechaTransaccion 				= {field: "FechaTransaccion", 				title: "UI_FECHA_TRANSACCION",	     width: "80px",		groupable: true };
		var IdOrdenString 					= {field: "IdOrdenString", 					title: "UI_ORDEN", 				     width: "65px"};
		var NombreLargoCliente 				= {field: "NombreLargoCliente", 			title: "UI_CLIENTE", 				 width: "200px", 	groupable: true };
		var CodigoPortafolio 				= {field: "CodigoPortafolio", 				title: "UI_PORTAFOLIO", 			 width: "70px", 	groupable: true };
		var DocumentoIdentidad 				= {field: "DocumentoIdentidad", 			title: "UI_DOCUMENTO_IDENTIDAD",     width: "80px", 	groupable: true };
		var EsConvenioDerechBCS 			= {field: "EsConvenioDerechBCS",			title: "UI_CONVENIO_DERECHO_BCS",	width: "75px", 	groupable: true };
		var EsConvenioDerechBEC 			= {field: "EsConvenioDerechBEC",			title: "UI_CONVENIO_DERECHO_BEC",	width: "75px", 	groupable: true };			
		
		
		var COMMVTOCMBTE = {field: "COMMVTOCMBTE",	title: "UI_COMISION",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
    	    }
		};	
		var DERMVTOCMBTE = {field: "DERMVTOCMBTE",	title: "UI_DERECHOS", 	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},  
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
    	    }
		};
		var GASMVTOCMBTE = {field: "GASMVTOCMBTE",	title: "UI_GASTOS",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},  
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		};
		var IMPMVTOCMBTE = {field: "IMPMVTOCMBTE",	title: "UI_IMPUESTO",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"},
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		};
		var RETMVTOCMBTE = {field: "RETMVTOCMBTE",	title: "UI_RETENCION",	width: "90px", footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}, 
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		};
		
		
		var AccionTipoOperacion 			= {field: "AccionTipoOperacion", 			title: "UI_OPERACION", 				 width: "80px", 	groupable: true };
		var NombreTipoOperacion 			= {field: "NombreTipoOperacion",             title: "UI_TIPO_OPERACION", 		 width: "200px",	groupable: true };
	   	var FechaMovimientoComprobante 		= {field: "FechaMovimientoComprobante",		title: "UI_FECHA_MOVIMIENTO", 		 width: "80px",  groupable: true };
		var NombreTipoProducto 				= {field: "NombreTipoProducto", 			title: "UI_TIPO_PRODUCTO", 		     width: "200px",  groupable: true };
		var NombreTipoInstrumento 			= {field: "NombreTipoInstrumento", 			title: "UI_TIPO_INSTRUMENTO",	 	 width: "120px", groupable: true };
		var NemotecnicoInstrumentoUnificado = {field: "NemotecnicoInstrumentoUnificado", title: "UI_NEMOTECNICO", 			 width: "120px", groupable: true };
		var RazonSocialEmisor 				= {field: "RazonSocialEmisor"              , title: "UI_EMISOR",     			 width: "120px", groupable: true };
		var Cantidad 						= {field: "Cantidad",                        title: "UI_CANTIDAD",                width: "80px",  sortable : false,
			format : function (value, item) {
				if (item.CodigoTipoProducto === 'MEME'){
					return util.bcs_formato(value,'SPT','Monto',item.IdMonedaSpot,'','');
				}
				return util.bcs_formato(value,'GEN','Cantidad',item.IdMonedaMovimientoComprobante,'','');
		     }
		};
		var Precio = {field: "Precio",	title: "UI_PRECIO",	width: "80px", sortable : false, 
			format : function (value, item) {
				if (item.CodigoTipoProducto === 'MEME'){
					return util.bcs_formato(value,'SPT','Precio',item.IdMoneda,'','');
				}
			
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}
		}
		var ValorRescate = {field: "ValorRescate",                    title: "UI_VALOR_RESCATE",           width: "80px",sortable : false, 
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',(item.IdTipoReajuste!== '' ? item.IdTipoReajuste : item.IdMonedaLiquidacion),'','');
			}
		}
		var ValorTipoCambio 	= {field: "ValorTipoCambio",    title: "UI_TIPO_CAMBIO",    width: "100px",format : functionFormato('Monto','IdMoneda'),sortable : false};
		var Monto 				= {field: "Monto",				title: "UI_MONTO",			width: "100px",format : functionFormato('Monto','IdMoneda'),sortable : false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IdMoneda 			= {field: "IdMoneda", 			title: "UI_MONEDA", 		width: "80px", groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMoneda#</span>" }			
		var MontoMovimientoComprobante = {field: "MontoMovimientoComprobante",      title: "UI_MONTO_LOCAL",             width: "100px",sortable : false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}, 
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Monto',item.IdMonedaMovimientoComprobante,'','');
			}
		}
		var IdMonedaMovimientoComprobante = {field: "IdMonedaMovimientoComprobante", 	title: "UI_MONEDA_LOCAL",            width: "80px", groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaMovimientoComprobante#</span>" }
		var MontoLiquidacion 				= {field: "MontoLiquidacion",				title: "UI_MONTO_LIQUIDACION",       width: "100px", format : functionFormato('Monto','IdMonedaLiquidacion'),sortable : false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IdMonedaLiquidacion			 	= {field: "IdMonedaLiquidacion", 			title: "UI_MONEDA_LIQUIDACION",      width: "80px",	groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaLiquidacion#</span>" }
		var Tasa 							= {field: "Tasa", 							title: "UI_TASA", 				     width: "80px", template: kendo.template("#if (EsVentaCorta == 'S')  {##=TasaOperacion##} else {##=Tasa##}#"),
			format : function (value, item) {
				if (item.EsVentaCorta === 'S')
					return util.bcs_formato(item.TasaOperacion, item.CodigoTipoProducto==='MEME'?'SPT':'GEN', 'Tasa', item.IdMonedaLiquidacion,'','');
				else
					return util.bcs_formato(item.Tasa, item.CodigoTipoProducto==='MEME'?'SPT':'GEN', 'Tasa', item.IdMonedaLiquidacion,item.IdTipoInstrumento,'');
		     }
		}
		
		
		var NombreCondicionDeLiquidacion 	= {field: "NombreCondicionDeLiquidacion", 	title: "UI_CONDICION_LIQUIDACION",   width: "100px", groupable: true }			
		var FechaLiquidacion 				= {field: "FechaLiquidacion",               title: "UI_FECHA_LIQUIDACION",       width :"80px",	groupable: true }
		var FechaVencimiento 				= {field: "FechaVencimiento",               title: "UI_FECHA_VENCIMIENTO",       width :"80px",	groupable: true }
		var CodigoBolsa 					= {field: "CodigoBolsa", 					title: "UI_BOLSA", 				     width: "70px",	groupable: true }
		var IdCorredoraContraparteString 	= {field: "IdCorredoraContraparteString", 	title: "UI_CORREDOR_CONTRAPARTE",    width: "80px",	groupable: true }
		var EsVentaCorta 					= {field: "EsVentaCorta", 					title: "UI_VENTA_CORTA", 			 width: "75px",	groupable: true }
		var EsComprobIndividual 			= {field: "EsComprobIndividual", 			title: "UI_COMPROBANTE_INDIVIDUAL",  width: "75px",	groupable: true }			
		var EsAPlazo 						= {field: "EsAPlazo",             			title: "UI_A_PLAZO",    			 width: "50px",	groupable: true }
		var Glosa 							= {field: "Glosa",             			    title: "UI_GLOSA",       			 width: "250px" }
		var ResumenMedioPago 				= {field: "ResumenMedioPago", 	            title: "UI_MEDIO_PAGO",              width: "200px" }
		var PrecioAPlazo 					= {field: "PrecioAPlazo",  					title: "UI_PRECIO_PLAZO", 			 width: "80px",
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}
		};
		var CodigoCorredorCorresponsal		= {field: "CodigoCorredorCorresponsal",		title: "UI_CORREDOR_CORRESPONSAL",	 width: "80px"}
		
		
		var COMMVTOCMBTE = {field: "COMMVTOCMBTE",	title: "UI_COMISION",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};	
		var DERMVTOCMBTE = {field: "DERMVTOCMBTE",	title: "UI_DERECHOS", 	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var GASMVTOCMBTE = {field: "GASMVTOCMBTE",	title: "UI_GASTOS",		width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IMPMVTOCMBTE = {field: "IMPMVTOCMBTE",	title: "UI_IMPUESTO",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var RETMVTOCMBTE = {field: "RETMVTOCMBTE",	title: "UI_RETENCION",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		
		var DiasPlazo 							= {field: "DiasPlazo",  					    title: "UI_DIAS_PLAZO", 			 width: "80px"}			
		var MontoMovimientoComprobanteCobro		= {field: "MontoMovimientoComprobanteCobro", title: "UI_MONTO_TOTAL_LOCAL_COBRO", width: "100px", sortable : false,format : functionFormato('Monto','IdMonedaMovimientoComprobante')};	
		var IdMonedaMovimientoComprobanteCobro	= {field: "IdMonedaMovimientoComprobanteCobro",	title: "UI_MONEDA_COBRO_LOCAL",      width: "80px", groupable: true, template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaMovimientoComprobanteCobro#</span>" }
		var MontoMovimientoComprobanteFinal		= {field: "MontoMovimientoComprobanteFinal", 	title: "UI_MONTO_TOTAL_LOCAL",       width: "100px", sortable : false,format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};	
		
		var NombreTipoCliente 		= {field: "NombreTipoCliente",		title: "UI_TIPO_CLIENTE", width: "150px"};
		var EsClienteFinal 			= {field: "EsClienteFinal",			title: "UI_CLIENTE_FINAL", width: "80px"};
		var CodigoBroker 			= {field: "CodigoBroker",			title: "UI_BROKER", width: "80px"};
		var LiquidaBancoCustodio 	= {field: "LiquidaBancoCustodio",	title: "UI_LIQUIDA_BANCO_CUSTODIO", width: "80px"};
		var CodigoBancoCustodio 	= {field: "CodigoBancoCustodio",	title: "UI_BANCO_CUSTODIO", width: "80px"};
		var CodigoBancoLiquidador 	= {field: "CodigoBancoLiquidador",	title: "UI_BANCO_LIQUIDADOR", width: "80px"};
		var ISIN 					= {field: "ISIN",					title: "UI_ISIN", width: "80px"};
		var MercadoSMV 				= {field: "MercadoSMV",				title: "UI_MERCADO_SMV", width: "80px"};
		var ModalidadSMV			= {field: "ModalidadSMV",			title: "UI_MODALIDAD_SMV", width: "80px"};
		var NombreEstadoCavali 		= {field: "NombreEstadoCavali",		title: "UI_ESTADO_CAVALI", width: "80px"};
		var ReajustesVAC 			= {field: "ReajustesVAC",			title: "UI_REAJUSTES_VAC", width: "80px",format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var Intereses 				= {field: "Intereses",				title: "UI_INTERESES", width: "80px",format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		
		var ComisionBVLMonedaMovimiento 			= {field: "ComisionBVLMonedaMovimiento",			title: "UI_COMISION_BVL", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ComisionCavaliMonedaMovimiento 			= {field: "ComisionCavaliMonedaMovimiento",			title: "UI_COMISION_CAVALI", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ComisionSMVMonedaMovimiento 			= {field: "ComisionSMVMonedaMovimiento",			title: "UI_COMISION_SMV", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ComisionSABMonedaMovimiento 			= {field: "ComisionSABMonedaMovimiento",			title: "UI_COMISION_SAB", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var FondoLiquidacionMonedaMovimiento 		= {field: "FondoLiquidacionMonedaMovimiento",		title: "UI_FONDO_LIQUIDACION", width: "80px",	format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var FondoGarantiaMonedaMovimiento 			= {field: "FondoGarantiaMonedaMovimiento",			title: "UI_FONDO_GARANTIA", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var OtrasComisionesMonedaMovimiento 		= {field: "OtrasComisionesMonedaMovimiento",		title: "UI_OTRAS_COMISIONES", width: "80px",	format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var ImpuestoIGVMonedaMovimiento				= {field: "ImpuestoIGVMonedaMovimiento",			title: "UI_IMPUESTO_IGV", width: "80px",		format : functionFormato('Monto','IdMonedaMovimientoComprobante')};
		var CodigoIFRS								= {field: "CodigoIFRS",			title: "UI_CODIGO_IFRS", width: "80px"};
		var TipoReajuste							= {field: "IdTipoReajuste",		title: "UI_TIPO_REAJUSTE", width: "80px"};
		var NemoSVS									= {field: "NemoSVS",			title: "UI_NEMO_SVS",  width: "80px"};		
		var IdTipoCanalIngreso 	                    = {field: "IdTipoCanalIngreso", 	title: "UI_TIPO_CANAL_INGRESO",   width: "100px", groupable: true}; // Canal Origen MGH
		var TipoLiquidacion 	    = {field: "TipoLiquidacion", 	title: "UI_TIPO_LIQUIDACION_COMISION_FACTURA",   width: "90px", groupable: true};
		var ModPrefFactDescripcion 	                    	= {field: "ModPrefFactDescripcion", 	title: "UI_MODO_FACTURACION",   width: "110px", groupable: true};
		
		if ($rootScope.SesionLocale === 'CL') {
						
			var base = [
				IdCobro,
				IdMovimientoComprobante,
				NombreLargoCliente,
				DocumentoIdentidad,
				NemotecnicoInstrumentoUnificado,
				CodigoPortafolio,
				COMMVTOCMBTE,
				Monto,
				IdComprobante,
				IdComprobanteReferencia,
				IdClasificacionTipoInstrumento,
				AccionTipoOperacion,
				NombreTipoOperacion,
				IdTipoComprobante,
				NombreConceptoMovimiento,
				NombreEstadoFacturacion,
				CodigoUsuarioBloqueo,
				NombreFormaCustodia,
				IndicadorOrdenDirecta,
				FolioTransaccion,
				FechaTransaccion,
				IdOrdenString,
				FechaMovimientoComprobante,
				NombreTipoProducto,
				NombreTipoInstrumento,
				RazonSocialEmisor,
				Cantidad,
				EsConvenioDerechBCS,
				EsConvenioDerechBEC,
				Precio,
				ValorRescate,
				ValorTipoCambio,
				TipoReajuste,
				IdMoneda,
				MontoMovimientoComprobante,
				IdMonedaMovimientoComprobante,
				MontoLiquidacion,
				IdMonedaLiquidacion,
				Tasa,
				NombreCondicionDeLiquidacion,
				FechaLiquidacion,
				FechaVencimiento,
				CodigoBolsa,
				IdCorredoraContraparteString,
				EsVentaCorta,
				EsComprobIndividual,
				EsAPlazo,
				Glosa,
				ResumenMedioPago,
				PrecioAPlazo,
				DiasPlazo,
				DERMVTOCMBTE,
				GASMVTOCMBTE,
				IMPMVTOCMBTE,
				RETMVTOCMBTE,
				MontoMovimientoComprobanteCobro,
				IdMonedaMovimientoComprobanteCobro,
				MontoMovimientoComprobanteFinal,
				CodigoIFRS,
				NemoSVS,
				IdTipoCanalIngreso, // Canal Origen MGH
				TipoLiquidacion,
				ModPrefFactDescripcion
			];
			
				
			
		}
		
		return base;
		
	};
	
	//MOVIMIENTOS COBROS
	var transporteMovimientoCobros = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",				
				dataType : "json"
			}
		};
	};
	
	//MOVIMIENTOS COBROS PERU
	var transporteMovimientoCobrosPeru = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=20062",				
				dataType : "json"
			}
		};
	};
	
	
	var modeloMovimientoCobros = function() {
		return {
			model: {
				fields : {
			        NombreCobro							: { type : "string"},
			        IdMonedaMovimientoComprobanteCobro 	: { type : "string"},
			        Porcentaje		    				: { type : "number"},
			        MontoMovimientoComprobanteCobro		: { type : "number"},
			        IdMonedaLiquidacionCobro			: { type : "string"},
			        MontoLiquidacionCobro				: { type : "number"},
			        NombreFormaCobro					: { type : "string"}
				}
			}
		};
	};
	
	
	var definicionOMovimientoCobros = function(inFiltros) 
	   {
			if ($rootScope.SesionLocale === 'CL') {
				return { 
				      serverFiltering : true,
				      serverSorting   : true, 
				      sort 			  : { field: "IdMovimientoComprobanteCobro", dir: "asc" }, // Criterios de Ordenamiento.
					  pageSize 		  : 10, // Número de registros por página.
					  transport 	  : transporteMovimientoCobros(),
					  schema 		  : modeloMovimientoCobros(),              
					  filter		  : inFiltros
				  };	
			}
			if ($rootScope.SesionLocale === 'PE') {
				return { 
				      serverFiltering : true,
				      serverSorting   : true, 
				      sort 			  : { field: "IdMovimientoComprobanteCobro", dir: "asc" }, // Criterios de Ordenamiento.
					  pageSize 		  : 10, // Número de registros por página.
					  transport 	  : transporteMovimientoCobrosPeru(),
					  schema 		  : modeloMovimientoCobros(),              
					  filter		  : inFiltros
				  };
			}
			else {
				
			}
		
		  
	   };
	
	
	obj.dataSourceMovimientoCobros = function(filtrosMovimientoCobros) 
	   {
		  return new bcs.DataSource(
		    {
			   type 		: "odata",
			   definition 	: definicionOMovimientoCobros(filtrosMovimientoCobros)
		    });
	   };
	
	 
	obj.columnasMovimientoCobros = function() {
		
		 var NombreCobro 						= { field: "NombreCobro",      						title: "UI_TIPO_COBRO",  				width:"100px",	sortable : false, filterable: false };
		 var IdMonedaMovimientoComprobanteCobro = { field: "IdMonedaMovimientoComprobanteCobro", 	title: "UI_MONEDA_COBRO_LOCAL", 		width:"50px",	sortable : false, filterable: false };
		 var IdMonedaLiquidacionCobro 			= { field: "IdMonedaLiquidacionCobro", 				title: "UI_MONEDA_LIQUIDACION", 		width:"50px",	sortable : false, filterable: false };
		 var Porcentaje 						= { field: "Porcentaje",       						title: "UI_PORCENTAJE",  				width:"50px", 	sortable : false, filterable: false, template : "<label>  #: (util.bcs_formato(Porcentaje,'GEN','Tasa',IdMonedaLiquidacionCobro,'','')).valor #</label>" };
		 var MontoMovimientoComprobanteCobro 	= { field: "MontoMovimientoComprobanteCobro", 		title: "UI_MONTO_COBRO_LOCAL",  		width:"80px", 	sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoMovimientoComprobanteCobro,'GEN','Monto',IdMonedaMovimientoComprobanteCobro,'','')).valor #</label>" };
		 var MontoLiquidacionCobro 				= { field: "MontoLiquidacionCobro", 				title: "UI_MONTO_LIQUIDACION", 			width:"80px", 	sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoLiquidacionCobro,'GEN','Monto',IdMonedaLiquidacionCobro,'','')).valor #</label>" };      
		 var NombreFormaCobro 					= { field: "NombreFormaCobro", 						title: "UI_FORMA_COBRO", 				width:"50px", 	sortable : false, filterable: false };
	
		
		if ($rootScope.SesionLocale === 'PE') {
			return  [
				NombreCobro,IdMonedaLiquidacionCobro,Porcentaje,MontoLiquidacionCobro,NombreFormaCobro
			];
		}
		else {
			return  [
				NombreCobro,IdMonedaMovimientoComprobanteCobro,Porcentaje,MontoMovimientoComprobanteCobro,NombreFormaCobro
			];
		}
		
		
	};
	
	
	
	//MOVIMIENTOS FORMA DE PAGO
	transporteMovimientoFormasDePago = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1237",
				dataType : "json"
			}
		};
	};
	
	modeloMovimientoFormasDePago = function() {
		return {
			model: {
				fields : {
					NombreMedioDePago           : { type: "string"},
					NombreModalidadDePago       : { type: "string"},
					NombreTipoCuentaBancaria	: { type: "string"},
					NombreBanco					: { type: "string"},					
					NombreCuentaBancaria		: { type: "string"},
					NumeroCuentaBancaria		: { type: "string"},
					NombreTipoTarjetaBancaria	: { type: "string"},
					NumeroTarjetaBancaria		: { type: "string"},
					FechaVctoTarjetaBancaria    : { type: "sysdate"},
					NumeroDocumento				: { type: "number"},
					FechaDocumentoPago			: { type: "sysdate"},
					IdMoneda					: { type: "string"},
					MontoLiquidacionFormaDePago : { type: "number"}, 						
					IdMedioDePago               : { type: "string"},   //columna oculta
					IdModalidadPago             : { type: "string"},   //columna oculta					
					IdBanco		                : { type: "number"},   //columna oculta
					IdTipoCuentaBancaria		: { type: "number"},   //columna oculta
					IdCuentaBancaria		    : { type: "number"},   //columna oculta
					IdTarjetaBancaria		    : { type: "number"},   //columna oculta
				}
			}
		};
	};
	
	definicionOMovimientoFormasDePago = function(filtrosMovimientoForDePago)
	  {
		 return {
			  serverFiltering 	: true, // Filtrado de datos en el servidor.
			  serverPaging 		: true, // Paginado de los datos en el servidor.
			  serverSorting 	: true, // Ordenamiento de los datos en el servidor.
			  sort 				: { field: "IdMovimientoComprobanteFormaDePago", dir: "asc" }, // Criterios de Ordenamiento.
			  pageSize 			: 10, // Número de registros por página.
			  transport 		: transporteMovimientoFormasDePago(),
			  schema 			: modeloMovimientoFormasDePago(),			  
			  filter			: filtrosMovimientoForDePago,
		   };
	  }
	
	obj.dataSourceMovimientoFormasDePago = function(filtrosMovimientoForDePago) 
	  {
		return new bcs.DataSource(
		  {
			type 		: "odata",
			definition 	: definicionOMovimientoFormasDePago(filtrosMovimientoForDePago)
		  });
	  };
	  
	  
	
	obj.columnasMovimientoFormasDePago = function() {
		return  [
			 {field : "NombreMedioDePago", 				title:'UI_MEDIO_DE_PAGO', width : "120px", align: "right", sortable : false }
			,{field : "NombreModalidadDePago", 			title:'UI_MODALIDAD', width : "60px", sortable : false}									
			,{field : "IdMoneda", 						title: "UI_MONEDA", width: "60px", align: "left", sortable : false}
			,{field : "MontoLiquidacionFormaDePago",	title:'UI_MONTO_LIQUIDACION', width : "100px", align: "right", sortable : false, 
															format : function (value, item) { 
																							if (item.IdMoneda === 'CLP'){
																								return util.bcs_formato(value,'GEN','Cantidad',item.IdMoneda,'','');
																							}
																								return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,'','');
																						 	}
			 }			
			,{field : "NombreTipoCuentaBancaria", 		title:'UI_TIPO_CUENTA_BANCO', width : "100px", align: "right", sortable : false }
			,{field : "NombreBanco", 					title:'UI_BANCO', width : "200px", align: "right", sortable : false }
			,{field : "NumeroCuentaBancaria", 			title:'UI_NUMERO_CUENTA', width : "100px", align: "left", sortable : false }
			,{field : "NombreTipoTarjetaBancaria",		title:'UI_TARJETA_BANCARIA', width : "100px", align: "right", sortable : false }	
			,{field : "FechaVctoTarjetaBancaria",		title:'UI_FECHA_VENCIMIENTO_TARJETA', width : "100px" }
			,{field : "NumeroTarjetaBancaria",			title:'UI_NUMERO_TARJETA', width : "100px", align: "left", sortable : false }		
			,{field : "NumeroDocumento", 				title:'UI_NUMERO_CHEQUE', width : "100px", align: "left", sortable : false }
			,{field : "FechaDocumentoPago", 			title:'UI_FECHA_VENCIMIENTO_DOCUMENTO', width : "100px" }			
			 	
			//Columnas Ocultas  			
			,{field : "IdMedioDePago", 					title:'UI_MEDIO_DE_PAGO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdModalidadPago", 				title:'UI_MODALIDAD', width : "80px", align: "right", sortable : false, hidden: true}			
			,{field : "IdBanco", 						title:'UI_BANCO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdTipoCuentaBancaria", 			title:'UI_TIPO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdCuentaBancaria", 				title:'UI_CUENTAS_BANCARIAS', width : "80px", align: "right", sortable : false, hidden: true}			
			,{field : "IdTarjetaBancaria", 				title:'UI_TARJETA_BANCARIA', width : "80px", align: "right", sortable : false, hidden: true}
			
		];
	};
	
	schemaGrillaMovimientosFacturarComisionVariableYFija = function() {
		return {
			model: {
				fields : {
					AccionTipoOperacion 			: { type: "string"},
					DocumentoIdentidad 				: { type: "string"},
					Cantidad 						: { type: "number", excelFormat: excelFormat('Cantidad', 'IdMonedaMovimientoComprobante')},
					CodigoUsuarioBloqueo            : { type: "string"},
					CodigoPortafolio 				: { type: "string"},
					CodigoBolsa 					: { type: "string"},
					CodigoBoveda 					: { type: "string"},
					CodigoBroker 					: { type: "string"},
					CodigoBancoCustodio				: { type: "string"},
					CodigoBancoLiquidador			: { type: "string"},
					CodigoCorredorCorresponsal		: { type: "string"},
					ComisionBVLMonedaMovimiento		: { type: "number"},
					ComisionCavaliMonedaMovimiento	: { type: "number"},
					ComisionSMVMonedaMovimiento		: { type: "number"},
					ComisionSABMonedaMovimiento		: { type: "number"},
					COMMVTOCMBTE					: { type: "number", excelFormat: excelFormat('COMMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					DERMVTOCMBTE					: { type: "number", excelFormat: excelFormat('DERMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					DiasPlazo                       : { type: "number"},			  
					EsAPlazo                        : { type: "string"},
					EsClienteFinal					: { type: "string"},
					EsConvenioDerechBCS				: { type: "string"},
					EsConvenioDerechBEC				: { type: "string"},					
					EsVentaCorta 					: { type: "string"},
					EsComprobIndividual             : { type: "string"},
					EsAPlazo                        : { type: "string"}, 
					FolioTransaccion 				: { type: "number"},
					FechaTransaccion 				: { type: "sysdate"},
					FechaLiquidacion                : { type: "sysdate"},
					FechaVencimiento 				: { type: "sysdate"},
					FechaMovimientoComprobante		: { type: "sysdate"},
					FondoLiquidacionMonedaMovimiento: { type: "number"},
					FondoGarantiaMonedaMovimiento	: { type: "number"},
					GASMVTOCMBTE					: { type: "number", excelFormat: excelFormat('GASMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					Glosa                           : { type: "string"},
					IdClasificacionTipoInstrumento 	: { type: "string"},
					IdCliente 						: { type: "number"},
					IdCobro						 	: { type: "string"},
					IdComprobante                   : { type: "number"},
	        		IdComprobanteReferencia         : { type: "number"},
					IdConceptoMovimiento 			: { type: "string"},
					IdCondicionLiquidacion 			: { type: "string"},
					IdCorredoraContraparteString 	: { type: "number"},
					IdEmisor						: { type: "string"},
					IdEstadoFacturacion 			: { type: "string"},					
					IdOrdenString 					: { type: "number"},
					IdMoneda 						: { type: "string"},
					IdMonedaMovimientoComprobante   : { type: "string"},
					IdMonedaLiquidacion             : { type: "string"},					
					IdMovimientoComprobante         : { type: "number"},
					IdPortafolio 					: { type: "number"},
					IdTipoComprobante 				: { type: "string"},
					IdTipoCliente	 				: { type: "string"},
					IdTipoReajuste					: { type: "string"},
					ISIN							: { type: "string"},
					IndicadorOrdenDirecta           : { type: "string"},
					IdMonedaMovimientoComprobanteCobro : { type: "string"},
					IdFormaCustodia	                : { type: "string"},
					NombreFormaCustodia             : { type: "string"},
					IdMonedaCobro 					: { type: "string"},
					Intereses						: { type: "number"},
					ImpuestoIGVMonedaMovimiento		: { type: "number"},
					IMPMVTOCMBTE					: { type: "number", excelFormat: excelFormat('IMPMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					LiquidaBancoCustodio			: { type: "string"},
					MercadoSMV						: { type: "string"},
					ModalidadSMV					: { type: "string"},
					Monto 							: { type: "number", excelFormat: excelFormat('Monto', 'IdMoneda')},									
					MontoMovimientoComprobante      : { type: "number", excelFormat: excelFormat('MontoMovimientoComprobante', 'IdMonedaMovimientoComprobante')},									
					MontoLiquidacion                : { type: "number", excelFormat: excelFormat('MontoLiquidacion', 'IdMonedaLiquidacion')},									
					MontoMovimientoComprobanteCobro : { type: "number", excelFormat: excelFormat('MontoMovimientoComprobanteCobro', 'IdMonedaMovimientoComprobanteCobro')},
					MontoMovimientoComprobanteFinal : { type: "number", excelFormat: excelFormat('MontoMovimientoComprobanteFinal', 'IdMonedaMovimientoComprobante')},	
					NombreEstadoFacturacion 		: { type: "string"},
					NombreEstadoCavali				: { type: "string"},
					NombreLargoCliente 				: { type: "string"},
					NombreTipoOperacion             : { type: "string"},
					NombreTipoCliente				: { type: "string"},
					NombreTipoProducto 				: { type: "string"},
					NombreTipoInstrumento			: { type: "string"},
					NemotecnicoInstrumentoUnificado	: { type: "string"},
					NombreConceptoMovimiento 		: { type: "string"},
					NombreCondicionDeLiquidacion 	: { type: "string"},
					OtrasComisionesMonedaMovimiento	: { type: "number"},
					
					Precio 							: { type: "number", excelFormat: excelFormat('Precio', 'IdMoneda')},
					PrecioAPlazo                    : { type: "number", excelFormat: excelFormat('PrecioAPlazo', 'IdMoneda')},
					RazonSocialEmisor               : { type: "string"},
					ResumenMedioPago                : { type: "string"},
					ReajustesVAC					: { type: "number"},
					RETMVTOCMBTE					: { type: "number", excelFormat: excelFormat('RETMVTOCMBTE', 'IdMonedaMovimientoComprobante')},
					ValorRescate					: { type: "number"},
					ValorTipoCambio					: { type: "number", excelFormat: excelFormat('Monto', 'IdMoneda')},
					Tasa 							: { type: "number"},
					TasaOperacion					: { type: "number"},   // Para Venta corta					
					CodigoIFRS						: { type: "string"},
					NemoSVS							: { type: "string"},
					IdTipoCanalIngreso				: { type: "string"}, // canal de Origen MGH 
					TipoLiquidacion 				: { type: "string"},
					ModPrefFactDescripcion			: { type: "string"}
				
				}
			}
		};
	};
	
	//MOVIMIENTOS A FACTURAR 
	transporteMovimientosFacturarComisionVariableYFija = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700388",
				dataType : "json"
			}
		};
	};
	
	obj.dataSourceMovimientosFacturarComisionVariableYFija = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdMovimientoComprobante", dir: "desc" }, // Criterios de Ordenamiento.
						pageSize 			: 200, // Número de registros por página.
			            transport 			: transporteMovimientosFacturarComisionVariableYFija(),
			            schema 				: schemaGrillaMovimientosFacturarComisionVariableYFija(),
						aggregate			: [ //Para resumen de totales de cobro
	                 	  		                { field: "Monto", aggregate: "sum" },
	                 	  		                { field: "MontoMovimientoComprobante", aggregate: "sum" },
	                 	  		                { field: "MontoLiquidacion", aggregate: "sum" },
	                 	  		                { field: "MontoMovimientoComprobanteFinal", aggregate: "sum" },	                 	  		                
						                        { field: "COMMVTOCMBTE", aggregate: "sum" },			    	        
						                        { field: "DERMVTOCMBTE", aggregate: "sum" },
						                        { field: "GASMVTOCMBTE", aggregate: "sum" },
						                        { field: "IMPMVTOCMBTE", aggregate: "sum" },
						                        { field: "RETMVTOCMBTE", aggregate: "sum" },				
                                              ]
		              }
	});
	
	schemaGrillaCobrosModificarMovimientos = function() {
		return {
			model: {
				fields : {
					IdTipoCobro					: { type: "string"},
					NombreTipoCobro				: { type: "string"},
					IdClasificacionCobro		: { type: "string"},
					NombreClasificacionCobro	: { type: "string"},
					IdFormaCobro				: { type: "string"},
					NombreFormaCobro			: { type: "string"},
					IdMonedaCobro				: { type: "string"},
					MontoCobro					: { type: "number"},
					MontoCobroOriginal			: { type: "number"},
					IdMonedaCobroSistema		: { type: "string"},
					MontoLocal					: { type: "number"},
					IdMonedaCobroLiquidacion	: { type: "string"},
					MontoLiquidacion			: { type: "number"},
					Porcentaje					: { type: "number"},
					Estado						: { type: "string"},
					Recalculable				: { type: "string"},
					MontoOperacion				: { type: "number"}
				
				}
			}
		};
	};
	
	obj.dataSourceGrillaCobrosMovimiento = new bcs.DataSource({
		type 		: "array", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            pageSize 			: 50, // Número de registros por página.
			            schema 				: schemaGrillaCobrosModificarMovimientos()
		              }
	});
	
	obj.columnasGrillaCobrosVariableMovimiento = function() {
		
		return[
			 {field : "IdTipoCobro", 				title:'UI_TIPO_COBRO', width : "120px", align: "right", hidden: true }
			,{field : "NombreTipoCobro", 			title:'UI_TIPO_COBRO', width : "80px", align: "right" }
			,{field : "NombreClasificacionCobro", 	title:'UI_CLASIFICACION_COBRO', width : "100px", align: "right", sortable : true }
			,{field : "NombreFormaCobro", 			title:'UI_FORMA_COBRO', width : "70px", align: "right", sortable : true }
			,{field : "IdMonedaCobro", 				title:'UI_MONEDA_COBRO', width : "70px", align: "right", sortable : true }
			,{field : "MontoCobro", 				title:'UI_MONTO_COBRO', editable: true, width : "70px", align: "right", 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobro,'','');
				}
			}
			,{field : "IdMonedaCobroSistema", 		title:'UI_MONEDA_COBRO_SISTEMA', width : "70px", align: "right", sortable : true }
			,{field : "MontoLocal", 				title:'UI_MONTO_LOCAL', width : "90px", align: "right", sortable : true , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobroSistema,'','');
				}
			}
			,{field : "IdMonedaCobroLiquidacion", 	title:'UI_MONEDA_COBRO_LIQUIDACION', width : "70px", align: "right", sortable : true }
			,{field : "MontoLiquidacion", 			title:'UI_MONTO_LIQUIDACION', width : "90px", align: "right", sortable : true , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobroLiquidacion,'','');
				}
			}
			,{field : "Porcentaje", 				title:'UI_PORCENTAJE', editable: true, width : "70px", align: "right" , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Tasa',item.IdMonedaCobro,'','');
				}}
			,{field : "Estado", 					title:'UI_ESTADO', width : "70px", align: "right", sortable : true }
			,{field : "Recalculable", 				title:'UI_RECALCULABLE', width : "70px", align: "right", sortable : true }
			,{field : "MontoOperacion", 			title:'UI_MONTO_OPERACION', width : "90px", align: "right", sortable : true , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobroSistema,'','');
				}
			}
			
		];
	};
	
	obj.columnasGrillaCobrosFijoMovimiento = function() {
		
		return[
			 {field : "IdTipoCobro", 				title:'UI_TIPO_COBRO', width : "120px", align: "right", hidden: true }
			,{field : "NombreTipoCobro", 			title:'UI_TIPO_COBRO', width : "80px", align: "right" }
			,{field : "NombreClasificacionCobro", 	title:'UI_CLASIFICACION_COBRO', width : "100px", align: "right", sortable : true }
			,{field : "NombreFormaCobro", 			title:'UI_FORMA_COBRO', width : "70px", align: "right", sortable : true }
			,{field : "IdMonedaCobro", 				title:'UI_MONEDA_COBRO', width : "70px", align: "right", sortable : true }
			,{field : "MontoCobro", 				title:'UI_MONTO_COBRO', editable: true, width : "70px", align: "right",
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobro,'','');
				}
			}
			,{field : "IdMonedaCobroSistema", 		title:'UI_MONEDA_COBRO_SISTEMA', width : "70px", align: "right", sortable : true }
			,{field : "MontoLocal", 				title:'UI_MONTO_LOCAL', width : "90px", align: "right", sortable : true , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobroSistema,'','');
				}
			}
			,{field : "IdMonedaCobroLiquidacion", 	title:'UI_MONEDA_COBRO_LIQUIDACION', width : "70px", align: "right", sortable : true }
			,{field : "MontoLiquidacion", 			title:'UI_MONTO_LIQUIDACION', width : "90px", align: "right", sortable : true , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobroLiquidacion,'','');
				}
			}
			,{field : "Estado", 					title:'UI_ESTADO', width : "70px", align: "right", sortable : true }
			,{field : "Recalculable", 				title:'UI_RECALCULABLE', width : "70px", align: "right", sortable : true }
			,{field : "MontoOperacion", 			title:'UI_MONTO_OPERACION', width : "90px", align: "right", sortable : true , 
				format : function (value, item) {
					return util.bcs_formato(value,'GEN','Monto',item.IdMonedaCobroSistema,'','');
				}
			}
			
		];
	};
		
	return obj;
});	


