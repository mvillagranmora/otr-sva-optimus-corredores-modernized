angular.module('ModuloFacturacion').factory('bcsConsultarMovimientoFacturarGrid',
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
	transporteConsultarMovimientoFacturar = function() {
		return {
			read : {
				     url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1235",
				     dataType : "json"
			       }
		       };
	};
	
	modeloConsultarMovimientoFacturar = function() {
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
					TasaAPlazo                      : { type: "number"},
					CodigoIFRS						: { type: "string"},
					NemoSVS							: { type: "string"}
				}
			}
		};
	};	
 

	obj.dataSourceConsultarMovimientoFacturar = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdMovimientoComprobante", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 25, // Número de registros por página.
			            transport 			: transporteConsultarMovimientoFacturar(),
			            schema 				: modeloConsultarMovimientoFacturar(),			
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
	
	
	
	obj.columnasConsultarMovimientoFacturar = function() {
		var functionFormato = function(uso,moneda) {
			return function (value, item) {
				return util.bcs_formato(value,'GEN',uso,item[moneda],'','');
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
			    		 return util.bcs_formato(value,'GEN','Precio',item.IdMonedaMovimientoComprobante,'','');
			    	 }
			    	 return util.bcs_formato(value,'GEN','Cantidad',item.IdMonedaMovimientoComprobante,'','');
			     }
		};
		
		var Precio = {field: "Precio",                          title: "UI_PRECIO",                  width: "80px", sortable : false, 
			format : function (value, item) {
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
		var MontoLiquidacion 				= {field: "MontoLiquidacion",				title: "UI_MONTO_LIQUIDACION",       width: "100px",format : functionFormato('Monto','IdMonedaLiquidacion'),sortable : false, footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IdMonedaLiquidacion			 	= {field: "IdMonedaLiquidacion", 			title: "UI_MONEDA_LIQUIDACION",      width: "80px",	groupable: true,  template: "<span class='flags' title='#=NombreMoneda#'>#=IdMonedaLiquidacion#</span>" }
		var Tasa 							= {field: "Tasa", 							title: "UI_TASA", 				     width: "80px",
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}
		}
		var NombreCondicionDeLiquidacion 	= {field: "NombreCondicionDeLiquidacion", 	title: "UI_CONDICION_LIQUIDACION",   width: "100px",	groupable: true }			
		var FechaLiquidacion 				= {field: "FechaLiquidacion",               title: "UI_FECHA_LIQUIDACION",       width :"80px",		groupable: true }
		var FechaVencimiento 				= {field: "FechaVencimiento",               title: "UI_FECHA_VENCIMIENTO",       width :"80px",		groupable: true }
		var CodigoBolsa 					= {field: "CodigoBolsa", 					title: "UI_BOLSA", 				     width: "70px",		groupable: true  }
		var IdCorredoraContraparteString 	= {field: "IdCorredoraContraparteString", 	title: "UI_CORREDOR_CONTRAPARTE",    width: "80px",		groupable: true  }
		var EsVentaCorta 					= {field: "EsVentaCorta", 					title: "UI_VENTA_CORTA", 			 width: "75px",		groupable: true }
		var EsComprobIndividual 			= {field: "EsComprobIndividual", 			title: "UI_COMPROBANTE_INDIVIDUAL",  width: "75px",		groupable: true }			
		var EsAPlazo 						= {field: "EsAPlazo",             			title: "UI_A_PLAZO",    			 width: "50px",		groupable: true }
		var Glosa 							= {field: "Glosa",             			    title: "UI_GLOSA",       			 width: "250px"}
		var ResumenMedioPago 				= {field: "ResumenMedioPago", 	            title: "UI_MEDIO_PAGO",              width: "200px"}
		var PrecioAPlazo 					= {field: "PrecioAPlazo",  					title: "UI_PRECIO_PLAZO", 			 width: "80px",
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}	
		};
		
		
		
		var COMMVTOCMBTE = {field: "COMMVTOCMBTE",	title: "UI_COMISION",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};	
		var DERMVTOCMBTE = {field: "DERMVTOCMBTE",	title: "UI_DERECHOS", 	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var GASMVTOCMBTE = {field: "GASMVTOCMBTE",	title: "UI_GASTOS",		width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var IMPMVTOCMBTE = {field: "IMPMVTOCMBTE",	title: "UI_IMPUESTO",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		var RETMVTOCMBTE = {field: "RETMVTOCMBTE",	title: "UI_RETENCION",	width: "90px",format : functionFormato('Monto','IdMonedaMovimientoComprobante'),footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}};
		
		var TasaAPlazo 							= {field: "TasaAPlazo", 						title: "UI_TASA_PLAZO", 			 width: "80px",
			format : function (value, item) {
				return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');
			}
		}
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
				EsVentaCorta,
				EsComprobIndividual,
				EsAPlazo,
				Glosa,
				ResumenMedioPago,
				PrecioAPlazo,
				TasaAPlazo,
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
				MontoMovimientoComprobanteFinal
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
				EsComprobIndividual,
				EsAPlazo,
				Glosa,
				ResumenMedioPago,
				PrecioAPlazo,
				TasaAPlazo,
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
				NemoSVS
			];
			
				
			
		}
		
		return base;
	};
	
	
	
	
	
	
	//MOVIMIENTOS COBROS
	var transporteConsultarMovimientoCobros = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1236",				
				dataType : "json"
			}
		};
	};
	
	//MOVIMIENTOS COBROS PERU
	var transporteConsultarMovimientoCobrosPeru = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=20062",				
				dataType : "json"
			}
		};
	};
	
		
	var modeloConsultarMovimientoCobros = function() {
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
	
	
	var definicionoConsultarMovimientoCobros = function(inFiltros) 
	   {
			if ($rootScope.SesionLocale === 'CL') {
				return { 
				      serverFiltering : true,
				      serverSorting   : true, 
				      sort 			  : { field: "IdMovimientoComprobanteCobro", dir: "asc" }, // Criterios de Ordenamiento.
					  pageSize 		  : 10, // Número de registros por página.
					  transport 	  : transporteConsultarMovimientoCobros(),
					  schema 		  : modeloConsultarMovimientoCobros(),              
					  filter		  : inFiltros
				  };	
			}
			if ($rootScope.SesionLocale === 'PE') {
				return { 
				      serverFiltering : true,
				      serverSorting   : true, 
				      sort 			  : { field: "IdMovimientoComprobanteCobro", dir: "asc" }, // Criterios de Ordenamiento.
					  pageSize 		  : 10, // Número de registros por página.
					  transport 	  : transporteConsultarMovimientoCobrosPeru(),
					  schema 		  : modeloConsultarMovimientoCobros(),              
					  filter		  : inFiltros
				  };
			}
			else {
				
			}
		
		  
	};
	   	      
	   
	obj.dataSourceConsultarMovimientoCobros = function(filtrosMovimientoCobros) 
	   {
		  return new bcs.DataSource(
		    {
			   type 		: "odata",
			   definition 	: definicionoConsultarMovimientoCobros(filtrosMovimientoCobros)
		    });
	   };
	   
	 obj.columnasConsultarMovimientoCobros = function() {
		 var NombreCobro 						= { field: "NombreCobro",      						title: "UI_TIPO_COBRO",  				width:"100px",	sortable : false, filterable: false };
		 var IdMonedaMovimientoComprobanteCobro = { field: "IdMonedaMovimientoComprobanteCobro", 	title: "UI_MONEDA_COBRO_LOCAL", 		width:"50px",	sortable : false, filterable: false };
		 var IdMonedaLiquidacionCobro 			= { field: "IdMonedaLiquidacionCobro", 				title: "UI_MONEDA_LIQUIDACION", 		width:"50px",	sortable : false, filterable: false };
		 var Porcentaje 						= { field: "Porcentaje",       						title: "UI_PORCENTAJE",  				width:"50px", 	sortable : false, filterable: false };
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
	
	
	//FORMA DE PAGO
	transporteConsultarMovimientoFormasDePago = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1237",
				dataType : "json"
			}
		};
	};
	
	modeloConsultarMovimientoFormasDePago = function() {
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
	
	definicionoConsultarMovimientoFormasDePago = function(filtrosMovimientoForDePago)
	  {
		 return {
			  serverFiltering 	: true, // Filtrado de datos en el servidor.
			  serverPaging 		: true, // Paginado de los datos en el servidor.
			  serverSorting 	: true, // Ordenamiento de los datos en el servidor.
			  sort 				: { field: "IdMovimientoComprobanteFormaDePago", dir: "asc" }, // Criterios de Ordenamiento.
			  pageSize 			: 10, // Número de registros por página.
			  transport 		: transporteConsultarMovimientoFormasDePago(),
			  schema 			: modeloConsultarMovimientoFormasDePago(),
			  filter			: filtrosMovimientoForDePago,
		   };
	  }
	
	obj.dataSourceConsultarMovimientoFormasDePago = function(filtrosMovimientoForDePago) 
	  {
		return new bcs.DataSource(
		  {
			type 		: "odata",
			definition 	: definicionoConsultarMovimientoFormasDePago(filtrosMovimientoForDePago)
		  });
	  };
	
	obj.columnasConsultarMovimientoFormasDePago = function() {
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
	
    return obj;
});