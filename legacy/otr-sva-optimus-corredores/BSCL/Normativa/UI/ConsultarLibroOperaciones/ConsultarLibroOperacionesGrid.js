angular.module('ModuloNormativa').factory('bcsBlotterLibroOperaciones', ['bcsComunes',
function(bcsComunes)
{
	var obj = {};
	
	obj.dataSourceLibroOperaciones = new bcs.DataSource({
			type : "call",
			definition : {
				callTransaction : "8798790",
				callParameters  : {},
				serverFiltering		: false,
				serverSorting		: false,
				serverPaging		: false,
				pageSize			: 200,
				callResultSet       : "P_OUT_LISTAOPERACIONES",
				
				schema : {
					data: function(response) {
						const items = response || [];
						items.forEach(item => {
							item.IdComprobanteSecuencia = item.IdComprobante || "";
						});
						return items;
					},
					model	: {
						fields : {
							FechaMovimiento				: { type: "sysdatetime"},
							FolioTransaccion			: { type: "string"},
							IdOrden						: { type: "string"},
							IdComprobante				: { type: "string"},
							IdComprobanteSecuencia		: { type: "string"},
							IdOperacion					: { type: "number"},
							IdTipoOperacion				: { type: "string"},
							IdCliente					: { type: "number"},
							RutCliente					: { type: "string"},
							NombreCliente				: { type: "string"},
							IdPortafolio				: { type: "number"},
							CodigoPortafolio			: { type: "string"},
							Relacion					: { type: "string"},
							CodigoTipoRelacion			: { type: "string"},
							TipoRelacion				: { type: "string"},
							IdClasificacionTipoInstrumento	: { type: "string"},
							IdTipoInstrumento			: { type: "string"},
							IdTipoOperacion				: { type: "string"},
							ClasificacionTipoInstrumento: { type: "string"},
							TipoInstrumento				: { type: "string"},
							TipoOperacion				: { type: "string"},
							AccionOperacion				: { type: "string"},
							IdEmisor					: { type: "string"},
							Nemotecnico					: { type: "string"},
							Cantidad					: { type: "number"},
							Precio						: { type: "number"},
							Tasa						: { type: "number"},
							IdMoneda					: { type: "string"},
							IdMonedaSecundaria			: { type: "string"},
							TasaCompra					: { type: "number"},
							RelacionInstrumento			: { type: "string"},
							IdOperadorContraparte		: { type: "number"},
							IdOperadorCorredor			: { type: "number"},
							IdTipoComprobante			: { type: "string"},
							NombreTipoComprobante		: { type: "string"},
							FolioComprobante			: { type: "number"},
							FolioDTE					: { type: "string"},
							ValorPresenteCompraInst		: { type: "number"},
							FechaVencimiento			: { type: "sysdatetime"},
							MontoCompra					: { type: "number"},
							MontoVenta					: { type: "number"},
							Comision					: { type: "number"},
							Derecho						: { type: "number"},
							CargoFijo					: { type: "number"},
							IVA							: { type: "number"},
							TotalCompra					: { type: "number"},
							TotalVenta					: { type: "number"},
							IdCondicionLiquidacion		: { type: "string"},
							IdDocumentoRelacion			: { type: "number"},
							IdOperacionRelacionada		: { type: "string"},
							FechaVencimientoPacto		: { type: "sysdatetime"},
							PrecioPlazo					: { type: "number"},
							TasaPlazo					: { type: "number"},
							IdMonedaPlazo				: { type: "string"},
							TotalCompraVencimiento		: { type: "number"},
							TotalVentaVencimiento		: { type: "number"},
							Custodia					: { type: "string"},
							IdBoveda					: { type: "string"},
							CantidadGarantia			: { type: "number"},
							NemoSVS						: { type: "string"},
							TasaVenta					: { type: "number"},
							ValorPresente				: { type: "number"},
							UtilidadPerdida				: { type: "number"},
							FechaOrigenPacto			: { type: "sysdatetime"},
							IdEstadoFactura				: { type: "string"},
							Canal						: { type: "string"},
							ValorIVA					: { type: "number"},
							IdComprobanteReferencia   	: { type: "number"},
							FolioComprobanteAsociado	: { type: "number"},
							NombreComprador				: { type: "string"},
							NombreVendedor				: { type: "string"},
							RutComprador				: { type: "string"},
							RutVendedor					: { type: "string"},
							IdBolsa						: { type: "string"},
							Proceso						: { type: "number"},
							EsVentaCortaUI				: { type: "string"},
							IdModulo					: { type: "string"},
							ParidadCierre				: { type: "number"},
							PlazoVencimiento			: { type: "number"},
							IdOperacionOrigenReasignacion:{ type: "number"},
							InstrumentoNegociacion		: { type: "string"},
							
							}
						}
					}
				
			}
		});
	
	obj.columnasLibroOperaciones = function () {
		return [   
		        
	        {field:	"FechaMovimiento", 				title: "UI_FECHA_MOVIMIENTO", 			width: "100px"},
	        {field:	"FolioTransaccion",				title: "UI_FOLIO_TRANSACCION", 			width: "80px"},
	        {field:	"IdComprobante", 				title: "UI_FOLIO_COMPROBANTE", 			width: "100px"},
	        {field:	"FolioDTE", 					title: "UI_FOLIO_DTE", 					width: "100px"},
	        {field:	"IdComprobanteSecuencia",		title: "UI_SECUENCIA",					width: "80px", hidden: true},
	        {field:	"IdOperacion", 					title: "UI_OPERACION", 					width: "80px", hidden: true},
	        {field:	"AccionOperacion", 				title: "UI_ACCION", 					width: "80px", groupable: true},
	        {field:	"TipoOperacion", 				title: "UI_TIPO_OPERACION", 			width: "160px", groupable: true},
	        
	        {field:	"RutCliente", 					title: "UI_RUT_SOCIO", 				    width: "80px"},
	        {field:	"NombreCliente", 				title: "UI_SOCIO_NEGOCIO_2",				width: "160px"},
	        {field:	"CodigoPortafolio", 			title: "Portafolio Socio", 				width: "60px"},
	        
	        {field:	"RutComprador", 				title: "UI_RUT_CONTRAPARTE", 			width: "80px", groupable: true},
	        {field:	"NombreComprador", 				title: "UI_NOMBRE_CONTRAPARTE",			width: "160px", groupable: true},
	        

	        
	        {field:	"Relacion", 					title: "UI_RELACION",	 				width: "80px"},
	        {field:	"TipoRelacion", 				title: "UI_TIPO_RELACION",	 			width: "180px", groupable: true},
	        
			{field:	"ClasificacionTipoInstrumento", title: "UI_CLASIFICACION", 				width: "160px"},
			{field:	"TipoInstrumento", 				title: "UI_TIPO_INSTRUMENTO", 			width: "160px", groupable: true},
			
			
			{field:	"IdEmisor", 					title: "UI_EMISOR", 					width: "100px", groupable: true},
			{field:	"Nemotecnico", 					title: "UI_NEMOTECNICO", 				width: "100px", groupable: true},
			{field:	"Cantidad",						title: "UI_CANTIDAD", 					width: "100px", format:"{0:n4}"},//, template : " <label> #:  (util.bcs_formato(Precio,'CUS','Cantidad',IdMoneda,'','')).valor #</label>"},
			{field:	"Precio", 						title: "UI_PRECIO", 					width: "100px", template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,((IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC')?'PAC':IdTipoInstrumento),'')).valor #</label>"},
			{field:	"Tasa", 						title: "UI_TASA", 						width: "100px", template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMoneda,((IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC')?'PAC':IdTipoInstrumento),'')).valor #</label>"},
			{field:	"IdMoneda", 					title: "UI_MONEDA", 					width: "60px", groupable: true},
			{field:	"IdMonedaSecundaria", 			title: "UI_MONEDA_SECUNDARIA",			width: "60px"},
			{field:	"TasaCompra", 					title: "UI_TASA_COMPRA", 				width: "100px", template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa',IdMoneda,((IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC')?'PAC':IdTipoInstrumento),'')).valor #</label>"},
			{field:	"RelacionInstrumento", 			title: "UI_RELACION_INSTRUMENTO", 		width: "100px"},
			{field:	"IdTipoComprobante", 			title: "UI_TIPO_COMPROBANTE", 			width: "100px"},
			
			{field:	"ValorPresenteCompraInst", 		title: "UI_VALOR_PRESENTE", 			width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},			
			{field:	"FechaVencimiento", 			title: "UI_FECHA_VENCIMIENTO", 			width: "100px"},
			{field:	"MontoCompra", 					title: "UI_MONTO_COMPRA", 				width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"MontoVenta", 					title: "UI_MONTO_VENTA", 				width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"Comision", 					title: "UI_COMISION", 					width: "100px", format:"{0:n4}"},
			{field:	"Derecho", 						title: "UI_DERECHOS", 					width: "100px", format:"{0:n4}"},
			{field:	"CargoFijo", 					title: "UI_GASTOS", 				width: "100px", format:"{0:n4}"},
			{field:	"IVA", 							title: "UI_IVA_2", 						width: "100px", format:"{0:n4}"},
			{field:	"TotalCompra", 					title: "Total Compra", 				width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"TotalVenta", 					title: "Total Venta", 				width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"IdCondicionLiquidacion", 		title: "Id Condicion Liquidacion",	width: "100px"},
			{field:	"IdDocumentoRelacion", 			title: "Id Documento Relacion", 		width: "100px", template: kendo.template("#if(IdDocumentoRelacion != 0){# #: IdDocumentoRelacion # #}else{# #: IdOperacionOrigenReasignacion # #}#") },
			{field:	"FechaVencimientoPacto", 		title: "Fecha Vencimiento Pacto", 	width: "100px"},
			{field:	"PrecioPlazo", 					title: "UI_PRECIO_PLAZO", 				width: "100px", template : " <label> #:  (util.bcs_formato(PrecioPlazo,'GEN','Precio',IdMoneda,((IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC')?'PAC':IdTipoInstrumento),'')).valor #</label>"},
			{field:	"TasaPlazo", 					title: "UI_TASA_PLAZO", 				width: "100px", template : " <label> #:  (util.bcs_formato(TasaPlazo,'GEN','Tasa',IdMoneda,((IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC')?'PAC':IdTipoInstrumento),'')).valor #</label>"},
			{field:	"IdMonedaPlazo", 				title: "Id Moneda Plazo", 			width: "100px"},
			{field:	"TotalCompraVencimiento", 		title: "Total Compra Vencimiento", 	width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"TotalVentaVencimiento", 		title: "Total Venta Vencimiento", 	width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"Custodia", 					title: "UI_CUSTODIA", 					width: "100px"},
			{field:	"IdBoveda", 					title: "UI_ID_BOVEDA", 					width: "100px"},
			{field:	"CantidadGarantia", 			title: "UI_CANTIDAD_GARANTIA", 			width: "100px", format:"{0:n4}"},
			{field:	"NemoSVS", 						title: "UI_NEMO_SVS", 					width: "100px", groupable: true},
			{field:	"TasaVenta", 					title: "Tasa Venta", 				width: "100px", template : " <label> #:  (util.bcs_formato(TasaVenta,'GEN','Tasa',IdMoneda,((IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC')?'PAC':IdTipoInstrumento),'')).valor #</label>"},
			{field:	"ValorPresente", 				title: "UI_VALOR_PRESENTE", 			width: "100px", format:"{0:n4}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"UtilidadPerdida", 				title: "Utilidad Perdida", 			width: "100px", format:"{0:n0}" , aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}},
			{field:	"FechaOrigenPacto", 			title: "Fecha Origen Pacto", 		width: "100px"},
			{field:	"IdEstadoFactura", 				title: "Estado Facturacion", 			width: "100px", groupable: true},
			{field:	"Canal", 						title: "UI_CANAL", 						width: "100px", groupable: true},
			{field:	"ValorIVA", 					title: "Valor IVA",					width: "100px"},
	
			{field:	"IdBolsa", 						title: "UI_BOLSA", 						width: "60px"},
			{field:	"Proceso", 						title: "UI_PROCESO", 					width: "50px"},
			{field:	"IdTipoOperacion", 				title: "UI_COD_MOVTO",		 			width: "60px"},
			{field:	"EsVentaCortaUI", 				title: "UI_VENTA_CORTA",		 		width: "80px"},
			{field:	"ParidadCierre", 				title: "UI_PARIDAD_CIERRE",		 		width: "80px"},
			{field:	"PlazoVencimiento", 			title: "UI_PLAZO_VENCIMIENTO",		 	width: "100px"},
			{field: "InstrumentoNegociacion",		title: "UI_INSTRUMENTO_NEGOCIACION",	width: "100px"},
			{field:	"IdComprobanteReferencia", 		title: "UI_COMPROBANTE_REFERENCIA", 	width: "100px"},
	        {field:	"FolioComprobanteAsociado", 	title: "UI_REFERENCIA_ANULACION_DTE", 	width: "100px"},
		     ];
	}
	
	return obj;
	
	
}])