angular.module('ModuloFacturacion').factory('bcsConsultarComprobanteGrid',
		function(bcsComunes, $rootScope)  
{
	
	 var obj = {};
	 
	//Formateo exportacion a excel
	var excelFormat = function(field, field2) {
	   return function (item) {
			return util.bcs_formato(item[field],'GEN','Monto',item[field2],'','').excelFormat;
		}; 
	 }
	 
	 //COMPROBANTE
	modeloConsultarComprobantes = function() {
		return {
					model: {
						fields : {
							
							CantidadTotal                   : { type: "number"},
			        		CodigoPortafolio 				: { type: "string"},
							CodigoTipoComprobante 			: { type: "string"},
							CodigoUsuario                   : { type: "string"},
							CodigoBoveda                    : { type : "string"},
							CodigoBancoCustodio             : { type : "string"},
			        		CodigoBancoLiquidador           : { type : "string"},
			        		CodigoBroker                    : { type : "string"},
			        		
			        		DiasPlazo                       : { type: "number"},
			        		DocumentoIdentidad 				: { type: "string"},
							DocumentoIdentidadCAVALI		: { type: "string"},
							
							EsVentaCorta					: { type : "string"},
							EsAPlazo                        : { type : "string"},
			        		EsClienteFinal                  : { type : "string"},
			        		
			        		FechaComprobante     			: { type: "sysdate"},
							FechaLiquidacion 				: { type: "sysdate"},
							FolioComprobante 				: { type: "number"},
							FechaVencimiento 				: { type: "sysdate"},
			        		
							IdEmpresa 						: { type: "number"},
							IdComprobante 					: { type: "number"},
							IdComprobanteReferencia         : { type: "number"},
							IdTareaDTE                      : { type: "number"},
							IdMoneda 						: { type: "string"},
							IdMonedaComprobante				: { type: "string"},
							IdMonedaLiquidacion				: { type: "string"},
							IdTipoOperacion 				: { type : "string"},
							IdCondicionLiquidacion 			: { type : "string"},
							IdBolsa 						: { type: "string"},
							IdComprobanteRef 				: { type : "number"},					
				        	IdTipoComprobante 				: { type: "string"},
				        	EsExento 						: { type: "string"},
				        	IdCliente 						: { type : "number"},
				        	IdPortafolio 					: { type : "number"},
				        	IdEmpleadoEjecutivoPortafolio 	: { type : "number"},
				        	IdTipoComprobanteRef 			: { type : "string"},
				        	Intereses                       : { type: "number"},
				        	LiquidaBancoCustodio            : { type :"string"},
				        	IdEstadoComprobante 			: { type: "string"},
			        		
							Monto 							: { type: "number", excelFormat: excelFormat('Monto', 'IdMoneda')},
							MontoComprobante				: { type: "number", excelFormat: excelFormat('MontoComprobante', 'IdMonedaComprobante')},
							MontoLiquidacion				: { type: "number", excelFormat: excelFormat('MontoLiquidacion', 'IdMonedaLiquidacion')},
							MontoCobro                      : { type: "number", excelFormat: excelFormat('MontoCobro', 'IdMoneda')},
							MontoComprobanteCobro           : { type: "number", excelFormat: excelFormat('MontoComprobanteCobro', 'IdMonedaComprobante')},
							MontoLiquidacionCobro           : { type: "number", excelFormat: excelFormat('MontoLiquidacionCobro', 'IdMonedaLiquidacion')},
			        		MontoFinal 						: { type: "number", excelFormat: excelFormat('MontoFinal', 'IdMoneda')},
			        		MontoComprobanteFinal			: { type: "number", excelFormat: excelFormat('MontoFinal', 'IdMonedaComprobante')},
			        		MontoLiquidacionFinal			: { type: "number", excelFormat: excelFormat('MontoFinal', 'IdMonedaLiquidacion')},
			        		MontoPagado 					: { type: "number", excelFormat: excelFormat('MontoPagado', 'IdMoneda')},
			        		ModalidadSMV                    : { type : "string"},
			        		MercadoSMV                      : { type : "string"},
			        		
			        		NemotecnicoInstrumento 			: { type : "string"},
							NombreBoveda				 	: { type: "string"},
							NombreCompletoBancoCustodio 	: { type: "string"},					
							NombreCompletoBancoLiquidador 	: { type: "string"},
							NombreConcepto                  : { type: "string"},
							NombreEstado 					: { type: "string"},
							NombreEstadoDTE					: { type: "string"},
							NombreEstadoSII					: { type: "string"},
							NombreEmpleadoEjecutivoPortafolio			: { type : "string"},
							NombreFormaCustodia				: { type: "string"},
							NombreLargoCliente 				: { type: "string"},
							NombreMoneda 					: { type: "string"},
			        		NombreSucursalEmpleadoEjecutivoPortafolio	: { type : "string"},
							NombreTipoProducto 				: { type : "string"},
							NombreTipoOperacion 			: { type : "string"},
			        		NombreTipoComprobante 			: { type: "string"},
			        		NombreTipoCliente               : { type : "string"},
			        		
			        		Precio                          : { type : "number"},
			        		
			        		ReajustesVAC                    : { type: "number"},
			        		
			        		Tasa                            : { type : "number"},
			        		TipoCustodia					: { type : "string"},
			        		TotalComis 						: { type : "number", excelFormat: excelFormat('TotalComis', 'IdMoneda')},
			        		TotalDere 						: { type : "number", excelFormat: excelFormat('TotalDere', 'IdMoneda')},
			        		TotalGasto 						: { type : "number", excelFormat: excelFormat('TotalGasto', 'IdMoneda')},
			        		TotalImpto 						: { type : "number", excelFormat: excelFormat('TotalImpto', 'IdMoneda')},
			        		TotalRetencion 					: { type : "number", excelFormat: excelFormat('TotalRetencion', 'IdMoneda')},	
			        		
			        		ValorTipoCambio                 : { type : "number"},
			        		
			        		ComisionBVL           			: { type: "number", excelFormat: excelFormat('ComisionBVL', 'IdMonedaLiquidacion')},
			        		ComisionSMV           			: { type: "number", excelFormat: excelFormat('ComisionSMV', 'IdMonedaLiquidacion')},
			        		ComisionSAB           			: { type: "number", excelFormat: excelFormat('ComisionSAB', 'IdMonedaLiquidacion')},
			        		ComisionCAVALI           		: { type: "number", excelFormat: excelFormat('ComisionCAVALI', 'IdMonedaLiquidacion')},
			        		FondoGarantia           		: { type: "number", excelFormat: excelFormat('FondoGarantia', 'IdMonedaLiquidacion')},
			        		FondoLiquidacion           		: { type: "number", excelFormat: excelFormat('FondoLiquidacion', 'IdMonedaLiquidacion')},
			        		ImpuestoIGV           			: { type: "number", excelFormat: excelFormat('ImpuestoIGV', 'IdMonedaLiquidacion')},
			        		OtrasComisiones           		: { type: "number", excelFormat: excelFormat('OtrasComisiones', 'IdMonedaLiquidacion')}
						}
					}
				};
	};
	
	obj.fnColorBk = function(row) {
        var color = "";
		
		if ( Number(row.MontoFinal) < 0 )
		   color = "green";
	    else
	       color = "red";

        return color;
  	};
  	
	
  	obj.dataSourceConsultarComprobantes = new bcs.DataSource({		
		    type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
							//pageSize 			: 25, // Número de registros por página.
							transport 			: {read : {
															url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1254",
															dataType : "json"
														  }
												  },
							schema 				: modeloConsultarComprobantes()
						  }              
	  });
  	
	
	obj.columnasConsultarComprobantes = function() {
		
		var NombreEstado 								= {field: "NombreEstado",title: "UI_ESTADO", width: "70px", sortable : false, groupable: true };
		var NombreEstadoDTE 							= {field: "NombreEstadoDTE",title: "UI_ESTADO_DTE", width: "70px", sortable : false, groupable: true };
		var NombreEsNombreEstadoSIItado 				= {field: "NombreEstadoSII",title: "UI_ESTADO_SII", width: "70px", sortable : false, groupable: true };
		var IdComprobante 								= {field: "IdComprobante", title: "UI_SECUENCIA_COMPROBANTE", width: "60px", sortable : false};
		var FolioComprobante 							= {field: "FolioComprobante", title: "UI_FOLIO", width: "60px", sortable : false };
		var IdComprobanteReferencia 					= {field: "IdComprobanteReferencia", title: "UI_REFERENCIA_COMPROBANTE", width: "60px", sortable : false };
		var IdTareaDTE 									= {field: "IdTareaDTE", title: "UI_TAREA_DTE", width: "60px", sortable : false, hidden: true };
		var CodigoTipoComprobante 						= {field: "CodigoTipoComprobante", title: "UI_TIPO", width: "50px", sortable : false, template: '<span class="flags" title="#=NombreTipoComprobante#">#=CodigoTipoComprobante#</span>', groupable: true };
		var DocumentoIdentidad 							= {field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD_ABR", width: "70px", sortable : false, template: '<span class="flags" title="#=NombreLargoCliente#">#=DocumentoIdentidad#</span>', groupable: true };
		var DocumentoIdentidadCAVALI					= {field: "DocumentoIdentidadCAVALI", title: "UI_DOCUMENTO_IDENTIDAD_CAVALI", width: "70px" };
		var NombreLargoCliente 							= {field: "NombreLargoCliente", title: "UI_CLIENTE", width: "200px", sortable : false, groupable: true };
		var CodigoPortafolio 							= {field: "CodigoPortafolio", title: "UI_PORTAFOLIO_ABR", width: "50px", sortable : false, groupable: true };
		var NombreEmpleadoEjecutivoPortafolio 			= {field: "NombreEmpleadoEjecutivoPortafolio", title: "UI_EJECUTIVO", width: "200px", sortable : false, groupable: true };
		var NombreSucursalEmpleadoEjecutivoPortafolio 	= {field: "NombreSucursalEmpleadoEjecutivoPortafolio", title: "UI_SUCURSAL", width: "120px", sortable : false };
		var FechaComprobante 							= {field: "FechaComprobante", title: "UI_FECHA_COMPROBANTE", width: "70px", groupable: true };
		var FechaLiquidacion 							= {field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "70px", groupable: true };
 		var NemotecnicoInstrumento                      = {field: "NemotecnicoInstrumento",title: "UI_NEMOTECNICO", width: "200px", sortable : false };
 		var Precio                                      = {field: "Precio", title: "UI_PRECIO", width: "100px", sortable : false,format : function (value, item) {return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,'','');}}; 		
 		var ValorTipoCambio                             = {field: "ValorTipoCambio", title: "UI_TIPO_CAMBIO", width: "100px", sortable : false,format : function (value, item) {return util.bcs_formato(value,'GEN','TipoCambio',item.IdMoneda,'','');}};
		var CodigoBoveda                                = {field: "CodigoBoveda", title: "UI_CUENTA_BOVEDA", width: "80px" };
		var NombreBoveda                                = {field: "NombreBoveda", title: "UI_BOVEDA", width: "80px" };
		var NombreFormaCustodia                         = {field: "NombreFormaCustodia", title: "UI_FORMA_CUSTODIA", width: "80px" };
		var Tasa 							            = {field: "Tasa", title: "UI_TASA", width: "80px"};
		var FechaVencimiento 				            = {field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width :"80px", groupable: true };
		var DiasPlazo 			         				= {field: "DiasPlazo", title: "UI_DIAS_PLAZO", width: "80px"}
		var CodigoUsuario                    			= {field: "CodigoUsuario", title: "UI_USUARIO", width: "60px" }
		var CantidadTotal 	                     		= {field: "CantidadTotal", title: "UI_CANTIDAD", width: "60px" }
		var ModalidadSMV                                = {field: "ModalidadSMV", title: "UI_MODALIDAD_SMV", width: "60px" }
		var MercadoSMV                                  = {field: "MercadoSMV", title: "UI_MERCADO_SMV", width: "60px" }
		var Intereses                                   = {field: "Intereses", title: "UI_INTERESES", width: "60px" }
		var ReajustesVAC                                = {field: "ReajustesVAC", title: "UI_REAJUSTES_VAC", width: "60px" }
		
		var CodigoBroker                                = {field: "CodigoBroker", title: "UI_BROKER", width: "60px" }
		var NombreTipoCliente                           = {field: "NombreTipoCliente", title: "UI_TIPO_CLIENTE", width: "100px" }
		var EsClienteFinal                              = {field: "EsClienteFinal", title: "UI_CLIENTE_FINAL", width: "60px" }
		var LiquidaBancoCustodio                        = {field: "LiquidaBancoCustodio", title: "UI_LIQUIDA_BANCO_CUSTODIO", width: "60px" }
		var CodigoBancoCustodio                         = {field: "CodigoBancoCustodio", title: "UI_CODIGO_BANCO_CUSTODIO", width: "60px" }
		var NombreCompletoBancoCustodio                 = {field: "NombreCompletoBancoCustodio", title: "UI_NOMBRE_BANCO_CUSTODIO", width: "125px" }
		var CodigoBancoLiquidador                       = {field: "CodigoBancoLiquidador", title: "UI_CODIGO_BANCO_LIQUIDADOR", width: "60px" }
		var NombreCompletoBancoLiquidador               = {field: "NombreCompletoBancoLiquidador", title: "UI_NOMBRE_BANCO_LIQUIDADOR", width: "125px" }
		
		
		//En moneda operacion
		var IdMoneda 				= {field: "IdMoneda", title: "UI_MONEDA", width: "80px", sortable : false, template: '<span class="flags" title="#=NombreMoneda#">#=IdMoneda#</span>', groupable: true }		
		var Monto 					= {field: "Monto", title: "UI_MONTO", width: "100px", sortable : false,format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}};
		var MontoCobro 				= {field: "MontoCobro", title: "UI_MONTO_COBRO", width: "100px", sortable : false,format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}		};
		var MontoFinal 				= {field: "MontoFinal", title: "UI_MONTO_FINAL", width: "100px", sortable : false,format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}		};
		
		//En moneda local
		var IdMonedaComprobante 	= {field: "IdMonedaComprobante", title: "UI_MONEDA_LOCAL", width: "80px", sortable : false, template: '<span class="flags" title="#=NombreMonedaComprobante#">#=IdMonedaComprobante#</span>' };    		
		var MontoComprobante 		= {field: "MontoComprobante", title: "UI_MONTO_LOCAL", width: "100px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaComprobante,'',''); }		};
		var MontoComprobanteCobro 	= {field: "MontoComprobanteCobro", title: "UI_MONTO_COBRO_LOCAL", width: "100px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaComprobante,'',''); }};
		var MontoComprobanteFinal 	= {field: "MontoComprobanteFinal", title: "UI_MONTO_FINAL_LOCAL", width: "100px", sortable : false,format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaComprobante,'',''); }};
		
		//En moneda liquidacion
		var IdMonedaLiquidacion 	= {field: "IdMonedaLiquidacion", title: "UI_MONEDA_LIQUIDACION", width: "80px", sortable : false, template: '<span class="flags" title="#=NombreMonedaLiquidacion#">#=IdMonedaLiquidacion#</span>' };
		var MontoLiquidacion 		= {field: "MontoLiquidacion", title: "UI_MONTO_LIQUIDACION", width: "100px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }		};
		var MontoLiquidacionCobro 	= {field: "MontoLiquidacionCobro", title: "UI_MONTO_COBRO_LIQUIDACION", width: "100px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }	};
		var MontoLiquidacionFinal 	= {field: "MontoLiquidacionFinal", title: "UI_MONTO_FINAL_LIQUIDACION", width: "100px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }	};
		var MontoPagado 			= {field: "MontoPagado", title: "UI_MONTO_PAGADO", width: "100px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'',''); }	};
		var NombreTipoProducto 		= {field: "NombreTipoProducto", title: "UI_TIPO_PRODUCTO", width: "200px", sortable : false, groupable: true };
		var NombreTipoOperacion 	= {field: "NombreTipoOperacion", title: "UI_TIPO_OPERACION", width: "200px", sortable : false, groupable: true };			
		var EsVentaCorta 			= {field: "EsVentaCorta", title: "UI_VENTA_CORTA", width: "80px", sortable : false };		
		var EsAPlazo    			= {field: "EsAPlazo", title: "UI_A_PLAZO", width: "80px", sortable : false };
		var TotalComis 				= {field: "TotalComis", title: "UI_COMISION", width: "60px", sortable : false, format : function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}};
		var TotalDere 				= {field: "TotalDere", title: "UI_DERECHOS", width: "60px", sortable : false, format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}};
		var TotalGasto 				= {field: "TotalGasto", title: "UI_GASTOS", width: "60px", sortable : false, format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}};
		var TotalImpto 				= {field: "TotalImpto", title: "UI_IMPUESTO", width: "60px", sortable : false, format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}};
		var TotalRetencion 			= {field: "TotalRetencion", title: "UI_RETENCION", width: "60px", sortable : false, format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');}};
		var IdCondicionLiquidacion 	= {field: "IdCondicionLiquidacion",  title: "UI_CONDICION_LIQUIDACION", width: "60px" };
		
		//Detalle Comisiones
		var ComisionBVL 			= {field: "ComisionBVL", title: "UI_COMISION_BVL", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var ComisionSMV 			= {field: "ComisionSMV", title: "UI_COMISION_SMV", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var ComisionSAB 			= {field: "ComisionSAB", title: "UI_COMISION_SAB", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var ComisionCAVALI			= {field: "ComisionCAVALI", title: "UI_COMISION_CAVALI", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var FondoGarantia			= {field: "FondoGarantia", title: "UI_FONDO_GARANTIA", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var FondoLiquidacion		= {field: "FondoLiquidacion", title: "UI_FONDO_LIQUIDACION", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var ImpuestoIGV				= {field: "ImpuestoIGV", title: "UI_IMPUESTO_IGV", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};
		var OtrasComisiones			= {field: "OtrasComisiones", title: "UI_OTRAS_COMISIONES", width: "80px", sortable: false, format: function (value, item) { return util.bcs_formato(value,'GEN','Monto',item.IdMonedaLiquidacion,'',''); }};

		if ($rootScope.SesionLocale === 'PE') {
			return [
				NombreEstado,NombreEstadoDTE,NombreEsNombreEstadoSIItado,IdComprobante,FolioComprobante,IdComprobanteReferencia,
				IdTareaDTE,CodigoTipoComprobante,DocumentoIdentidad,DocumentoIdentidadCAVALI,NombreLargoCliente,CodigoPortafolio,NombreEmpleadoEjecutivoPortafolio ,
				NombreSucursalEmpleadoEjecutivoPortafolio,FechaComprobante,FechaLiquidacion,
				IdMoneda,CodigoUsuario,CantidadTotal,IdMonedaComprobante,MontoComprobante 
				,MontoComprobanteFinal,IdMonedaLiquidacion 
				,MontoLiquidacion,MontoLiquidacionCobro,MontoLiquidacionFinal 
				,MontoPagado,NombreTipoProducto,NombreTipoOperacion 
				,EsVentaCorta,EsAPlazo,NemotecnicoInstrumento, Precio, ValorTipoCambio, Tasa, FechaVencimiento, DiasPlazo
				,ModalidadSMV,MercadoSMV,Intereses,ReajustesVAC,CodigoBroker,NombreTipoCliente,EsClienteFinal,NombreBoveda,CodigoBoveda,
				LiquidaBancoCustodio,NombreFormaCustodia,CodigoBancoCustodio,NombreCompletoBancoCustodio,CodigoBancoLiquidador,NombreCompletoBancoLiquidador
				,ComisionBVL,ComisionSMV,ComisionSAB,ComisionCAVALI,FondoGarantia,FondoLiquidacion,ImpuestoIGV,OtrasComisiones
			];
		}
		else {
			return [
				NombreEstado,NombreEstadoDTE,NombreEsNombreEstadoSIItado,IdComprobante,FolioComprobante,IdComprobanteReferencia,
				IdTareaDTE,CodigoTipoComprobante,DocumentoIdentidad,NombreLargoCliente,CodigoPortafolio,NombreEmpleadoEjecutivoPortafolio ,
				NombreSucursalEmpleadoEjecutivoPortafolio,FechaComprobante,FechaLiquidacion,
				IdMoneda,CodigoUsuario,Monto,MontoCobro,MontoFinal,IdMonedaComprobante,MontoComprobante 
				,MontoComprobanteCobro,MontoComprobanteFinal,IdMonedaLiquidacion 
				,MontoLiquidacion,MontoLiquidacionCobro,MontoLiquidacionFinal 
				,MontoPagado,NombreTipoProducto,NombreTipoOperacion 
				,EsVentaCorta,TotalComis 
				,TotalDere,TotalGasto 
				,TotalImpto,TotalRetencion,IdCondicionLiquidacion

			];
		}
	};
	
	
	//COMPROBANTE DETALLE
	obj.modeloConsultarComprobanteDetalle = function() {
		return {
			model: {
				fields : {
					NroLinea         		: { type : "number"},
					FechaLiquidacion     	: { type : "date"},
	        		NemotecnicoInstrumento  : { type : "string"},
	        		IndicadorOrdenDirecta   : { type : "string"}, 
	        		Cantidad         		: { type : "number"},
	        		Precio         			: { type : "number"},
	        		Tasa                    : { type : "number"}, 
	        		Monto         			: { type : "number"},
	        		MontoComprobanteDetalle : { type : "number"},
	        		MontoLiquidacion        : { type : "number"},
	        		NombreTipoInstrumento	: { type : "string"},
	        		IdTipoProducto   		: { type : "string"},
	        		IdTipoOperacion  		: { type : "string"},	        		
	        		IdBolsa          		: { type : "string"},
	        		IdAgente         		: { type : "number"},
	        		IdComprobanteDetalle 	: { type : "number"},
	        		IdMoneda                : { type : "string"},
	        		IdMonedaComprobante     : { type : "string"},
	        		IdMonedaLiquidacion     : { type : "string"},
				}
			}
		};
	};
	
	
	obj.dataSourceConsultarComprobanteDetalle = function(filtrosComprobante)
	  {
		 return new bcs.DataSource(
		   {
			  type 		: "odata",
			  definition 	: {
								serverFiltering 	: true, // Filtrado de datos en el servidor.
								serverPaging 		: true, // Paginado de los datos en el servidor.
								serverSorting 		: true, // Ordenamiento de los datos en el servidor.
								sort 				: [{ field: "NroLinea", dir: "asc" },
								     				   { field: "IdComprobanteDetalle", dir: "asc" }],// Criterios de Ordenamiento.
								                      
								pageSize 			: 20, // Número de registros por página.
								transport 			: {read : {
																url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1241",
																dataType : "json"
															  }
													  },
								schema 				: obj.modeloConsultarComprobanteDetalle(),								
								filter				: filtrosComprobante, 
						      }
		});
	};
	
	obj.columnasConsultarComprobanteDetalle = function() {
		return  [
			 { field: "NroLinea", title: "UI_NUMERO_LINEA_ABR", width: "30px", sortable : false }		
			,{ field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION_ABR", width: "125px", template: kendo.template("#if (util.dateisdefault(FechaLiquidacion))  {##} else {##=util.formatoDate(FechaLiquidacion)##}#")}
			,{ field: "NemotecnicoInstrumento", title: "UI_INSTRUMENTO", width: "150px", sortable : false }
			,{ field: "IndicadorOrdenDirecta", title: "UI_INDICADOR_ORDEN_DIRECTA", width: "80px", sortable : false }			
			,{ field: "Cantidad", title: "UI_CANTIDAD", width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>" }			
			,{ field: "Precio", title: "UI_PRECIO", width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>" }
			,{ field: "Tasa", title: "UI_TASA", width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>" }
			,{ field: "IdMoneda", title: "UI_MONEDA", width: "80px", sortable : false }
			,{ field: "Monto", title: "UI_MONTO", width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{ field: "IdMonedaComprobante", title: "UI_MONEDA_LOCAL", width: "80px", sortable : false }
			,{ field: "MontoComprobanteDetalle", title: "UI_MONTO_LOCAL", width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>" }
			,{ field: "IdMonedaLiquidacion", title: "UI_MONEDA_LIQUIDACION", width: "80px", sortable : false }
			,{ field: "MontoLiquidacion", title: "UI_MONTO_LIQUIDACION", width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMonedaLiquidacion,'','')).valor #</label>" }
			,{ field: "NombreTipoInstrumento", title: "UI_TIPO_INSTRUMENTO_ABR", width: "150px", sortable : false }
			,{ field: "IdTipoProducto", title: "UI_TIPO_PRODUCTO_ABR", width: "65px", sortable : false, template: '<span class="flags" title="#=NombreTipoProducto#">#=IdTipoProducto#</span>' }
			,{ field: "IdTipoOperacion", title: "UI_TIPO_OPERACION_ABR", width: "65px", sortable : false, template: '<span class="flags" title="#=NombreTipoOperacion#">#=IdTipoOperacion#</span>' }
			,{ field: "IdBolsa", title: "UI_BOLSA_ABR", sortable : false }
		];
	};
	
	
	//MovimientoComprobanteDetalle
	obj.modeloConsultarMovimientoComprobanteDetalle = function() 
	  {
		return {
			model: {
				fields : 
				  {					
	        		 IdMovimientoComprobanteDetalle : { type : "number"},
	        		 IdConceptoMovimiento 			: { type : "string"},
	        		 NemotecnicoInstrumento 		: { type : "string"},
	        		 NemotecnicoInstrumentoUnificado : { type : "string"},
	        		 IndicadorOrdenDirecta          : { type : "string"},
	        		 Cantidad 						: { type : "number"},
	        		 Precio 						: { type : "number"},
	        		 Tasa   						: { type : "number"},
	        		 Monto                          : { type : "number"},  
	        		 MontoMovimientoComprobanteDetalle : { type : "number"},
	        		 MontoLiquidacion               : { type : "number"},
	        		 IdTransaccion 					: { type : "number"},
	        		 IdComprobanteDetalle 			: { type : "number"},
	        		 IdMovimientoComprobante 		: { type : "number"},	        		
	        		 IdInstrumento 					: { type : "number"},
	        		 PrecioPromedio 				: { type : "string"},
	        		 AfectoComision                 : { type : "string"},
	        		 AfectoDerecho                  : { type : "string"},
	        		 AfectoGasto                    : { type : "string"},
	        		 AfectoIva                      : { type : "string"},
	        		 IdMoneda                       : { type : "string"},
	        		 IdMonedaMovimientoComprobanteDetalle : { type : "string"},
	        		 IdMonedaLiquidacion            : { type : "string"}, 
	        		 
				  }
			   }
		    };
	  };
	  
	obj.dataSourceConsultarMovimientoComprobanteDetalle = function(filtrosComprobanteDetalle) 
	  {
		return new bcs.DataSource(
		  {
			type 		: "odata",
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdMovimientoComprobanteDetalle", dir: "asc" }, // Criterios de Ordenamiento.
							pageSize 			: 20, // Número de registros por página.
							transport 			: {read : {
															url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1242",
															dataType : "json"
														  }
												  },
							schema 				: obj.modeloConsultarMovimientoComprobanteDetalle(),
							//filtersFn 			: function (row) {
							//							return { field: "IdComprobanteDetalle", operator: "eq", value: row.IdComprobanteDetalle };
							//					  }
							filter				: filtrosComprobanteDetalle,
						}
		});
	};
	
	obj.columnasConsultarMovimientoComprobanteDetalle = function() {
		return  [
			 { field: "IdMovimientoComprobante", title: "UI_MOVIMIENTO", width: "70px", sortable : false, template : '<label> #:  util.formateaNumero(IdMovimientoComprobante,0) #</label>' }		                    
			,{ field: "IdConceptoMovimiento", title: "UI_CONCEPTO", width: "70px", sortable : false }
			,{ field: "NemotecnicoInstrumentoUnificado", title: "UI_NEMO" , width: "100px", sortable : false }
			,{ field: "IndicadorOrdenDirecta", title: "UI_INDICADOR_ORDEN_DIRECTA", width: "80px", sortable : false }
			,{ field: "Cantidad", title: "UI_CANTIDAD" , width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>" }
			,{ field: "Precio", title: "UI_PRECIO" , width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>" }
			,{ field: "Tasa", title: "UI_TASA" , width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>" }
			,{ field: "IdMoneda", title: "UI_MONEDA", width: "80px", sortable : false }
			,{ field: "Monto", title: "UI_MONTO" , width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{ field: "IdMonedaMovimientoComprobanteDetalle", title: "UI_MONEDA_LOCAL", width: "80px", sortable : false }
			,{ field: "MontoMovimientoComprobanteDetalle", title: "UI_MONTO_LOCAL" , width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(MontoMovimientoComprobanteDetalle,'GEN','Monto',IdMonedaMovimientoComprobanteDetalle,'','')).valor #</label>" }
			,{ field: "IdMonedaLiquidacion", title: "UI_MONEDA_LIQUIDACION", width: "80px", sortable : false }
			,{ field: "MontoLiquidacion", title: "UI_MONTO_LIQUIDACION" , width: "70px", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMonedaLiquidacion,'','')).valor #</label>" }
			,{ field: "IdTransaccion", title: "UI_TRANSACCION", width: "70px"}		
			,{ field: "AfectoComision", title: "UI_AFECTO_COMISION", width: "70px"} 
			,{ field: "AfectoDerecho", title: "UI_AFECTO_DERECHO", width: "70px"}
			,{ field: "AfectoGasto", title: "UI_AFECTO_GASTO", width: "70px"}
			,{ field: "AfectoIva", title: "UI_AFECTO_IVA", width: "70px"}
		];
	};
	
	
	//COMPROBANTE COBROS	
	obj.modeloConsultarComprobanteCobro = function() {
		return {
			model: {
				fields : {					
	        		NombreCobro			: { type : "string"},
	        		FormaCobro          : { type : "string"},
	        		NombreFormaCobro          : { type : "string"},
	        		Porcentaje 			: { type : "string"},
	        		NombreMonedaLocalCobro  : { type : "string"},	        		
	        		MontoLocalCobro		: { type : "string"},
	        		NombreMonedaLiquidacionCobro : { type : "string"},
	        		IdMonedaLiquidacionCobro : { type : "string"},
	        		MontoLiquidacionCobro : { type : "string"},
	        		IdCobro 			: { type : "string"}, 
	        		IdMonedaCobro		: { type : "string"}, 
	        		CodigoMoneda		: { type : "string"} 
				}
			}
		};
	};
	
	
	obj.dataSourceConsultarComprobanteCobro = function(filtrosComprobanteCobros) 
	  {
		if ($rootScope.SesionLocale === 'PE') {
			return new bcs.DataSource(
					   {
						  type 		    : "odata",
						  definition 	: {
											serverFiltering 	: true, // Filtrado de datos en el servidor.
											serverPaging 		: true, // Paginado de los datos en el servidor.
											serverSorting 		: true, // Ordenamiento de los datos en el servidor.
											sort 				: { field: "IdCobro", dir: "desc" }, // Criterios de Ordenamiento.
											pageSize 			: 20, // Número de registros por página.
											transport 			: {read : {
																			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=20071",
																			dataType : "json"
																		  }
																  },
											schema 				: obj.modeloConsultarComprobanteCobro(),
											//filtersFn 			: function (row) {
											//							return { field: "IdComprobante", operator: "eq", value: row.IdComprobante };
											//					  }
											filter              : filtrosComprobanteCobros
									  }
					});
		}
		else {
			return new bcs.DataSource(
					   {
						  type 		    : "odata",
						  definition 	: {
											serverFiltering 	: true, // Filtrado de datos en el servidor.
											serverPaging 		: true, // Paginado de los datos en el servidor.
											serverSorting 		: true, // Ordenamiento de los datos en el servidor.
											sort 				: { field: "IdCobro", dir: "desc" }, // Criterios de Ordenamiento.
											pageSize 			: 20, // Número de registros por página.
											transport 			: {read : {
																			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1243",
																			dataType : "json"
																		  }
																  },
											schema 				: obj.modeloConsultarComprobanteCobro(),
											//filtersFn 			: function (row) {
											//							return { field: "IdComprobante", operator: "eq", value: row.IdComprobante };
											//					  }
											filter              : filtrosComprobanteCobros
									  }
					});
		}
	};
	
	obj.columnasConsultarComprobanteCobro = function() {
		var NombreCobro 					= { field: "NombreCobro", title: "UI_TIPO_COBRO", width: "25%", sortable : false };
		var FormaCobro 						= { field: "FormaCobro", title: "UI_FORMA_COBRO", width: "10%", sortable : false };
		var NombreFormaCobro 				= { field: "NombreFormaCobro", title: "UI_FORMA_COBRO", width: "10%", sortable : false };
		var Porcentaje 						= { field: "Porcentaje", title: "UI_PORCENTAJE", width: "10%", sortable : false, template : " <label> #:  (util.bcs_formato(Porcentaje,'GEN','Tasa',IdMonedaCobro,'','')).valor #</label>" };
		var NombreMonedaLocalCobro 			= { field: "NombreMonedaLocalCobro", title: "UI_MONEDA_LOCAL_COBRO", width: "15%", sortable : false };			
		var MontoLocalCobro 				= { field: "MontoLocalCobro", title: "UI_MONTO_LOCAL_COBRO", width: "20%", sortable : false, template : " <label> #:  (util.bcs_formato(MontoLocalCobro,'GEN','Monto',IdMonedaLocalCobro,'','')).valor #</label>" };
		var NombreMonedaLiquidacionCobro 	= { field: "NombreMonedaLiquidacionCobro", title: "UI_MONEDA_LIQUIDACION_COBRO", width: "15%", sortable : false };			
		var MontoLiquidacionCobro 			= { field: "MontoLiquidacionCobro", title: "UI_MONTO_LIQUIDACION_COBRO", width: "20%", sortable : false, template : " <label> #:  (util.bcs_formato(MontoLiquidacionCobro,'GEN','Monto',IdMonedaLiquidacionCobro,'','')).valor #</label>" };
		var IdMonedaLiquidacionCobro 		= { field: "IdMonedaLiquidacionCobro", title: "UI_MONEDA_LIQUIDACION", width: "20%", sortable : false };
		
		
		
		if ($rootScope.SesionLocale === 'PE') {
			return  [
				NombreCobro,NombreFormaCobro,Porcentaje,IdMonedaLiquidacionCobro,MontoLiquidacionCobro
			];
		}
		else {
			return  [
				NombreCobro,FormaCobro,Porcentaje,NombreMonedaLocalCobro,MontoLocalCobro,NombreMonedaLiquidacionCobro,MontoLiquidacionCobro
			];
		}		
	};
	

	//COMPROBANTE FORMA DE PAGO	
	obj.modeloConsultarComprobanteFormaDePago = function() {
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
					FechaVctoTarjetaBancaria    : { type: "date"},
					NumeroSerie  				: { type: "string"},
					NumeroDocumento				: { type: "number"},
					FechaDocumentoPago			: { type: "date"},
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
	
	
	obj.dataSourceConsultarComprobanteFormaDePago = function(filtrosComprobanteFormaDePago) 
	  {
		return new bcs.DataSource(
		  {		
			 type 		: "odata",
			 definition 	: {
								serverFiltering 	: true, // Filtrado de datos en el servidor.
								serverPaging 		: true, // Paginado de los datos en el servidor.
								serverSorting 		: true, // Ordenamiento de los datos en el servidor.
								sort 				: { field: "IdComprobanteFormaDePago", dir: "desc" }, // Criterios de Ordenamiento.
								pageSize 			: 20, // Número de registros por página.
								transport 			: {read : {
									                            url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1244",
																dataType : "json"
															  }
													  },
								schema 				: obj.modeloConsultarComprobanteFormaDePago(),								
								filter              : filtrosComprobanteFormaDePago,
						  }
		});
	};
	
	obj.columnasConsultarComprobanteFormaDePago = function() {
		return  [
			 {field : "NombreMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "80px", sortable : false }
			,{field : "NombreModalidadDePago", title:'UI_MODALIDAD', width : "60px", sortable : false}									
			,{field : "IdMoneda", title: "UI_MONEDA", width: "60px", sortable : false}
			,{field : "MontoLiquidacionFormaDePago", title:'UI_MONTO_LIQUIDACION', width : "100px", sortable : false, template : '<label> #: util.formateaNumero(MontoLiquidacionFormaDePago,2) #</label>'} 			
			,{field : "NombreTipoCuentaBancaria", title:'UI_TIPO_CUENTA_BANCO', width : "100px", sortable : false }
			,{field : "NombreBanco", title:'UI_BANCO', width : "200px", sortable : false }
			,{field : "NumeroCuentaBancaria", title:'UI_NUMERO_CUENTA', width : "100px", sortable : false }
			,{field : "NombreTipoTarjetaBancaria", title:'UI_TARJETA_BANCARIA', width : "100px", sortable : false }
			,{field : "NumeroTarjetaBancaria", title:'UI_NUMERO_TARJETA', width : "100px", sortable : false }
			,{field : "FechaVctoTarjetaBancaria", title:'UI_FECHA_VENCIMIENTO_TARJETA', width : "100px" , sortable : false, template: kendo.template("#if (util.dateisdefault(FechaVctoTarjetaBancaria))  {##} else {##=util.formatoDate(FechaVctoTarjetaBancaria)##}#")}			
			,{field : "NumeroSerie", title:'UI_NUMERO_SERIE', width : "100px", sortable : false }
			,{field : "NumeroDocumento", title:'UI_NUMERO_DOCUMENTO', width : "80px", sortable : false, hidden: true}
			,{field : "FechaDocumentoPago", title:'UI_FECHA_DOCUMENTO_PAGO', width : "100px" , sortable : false, template: kendo.template("#if (util.dateisdefault(FechaDocumentoPago))  {##} else {##=util.formatoDate(FechaDocumentoPago)##}#")}
			 	
			//Columnas Ocultas  			
			,{field : "IdMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "80px", sortable : false, hidden: true}
			,{field : "IdModalidadPago", title:'UI_MODALIDAD', width : "80px", sortable : false, hidden: true}			
			,{field : "IdBanco", title:'UI_BANCO', width : "80px", sortable : false, hidden: true}
			,{field : "IdTipoCuentaBancaria", title:'UI_TIPO', width : "80px", sortable : false, hidden: true}
			,{field : "IdCuentaBancaria", title:'UI_CUENTAS_BANCARIAS', width : "80px", sortable : false, hidden: true}			
			,{field : "IdTarjetaBancaria", title:'UI_TARJETA_BANCARIA', width : "80px", sortable : false, hidden: true}			
		];
	};
	 
	 return obj;
});