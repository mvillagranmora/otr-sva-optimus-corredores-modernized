angular.module('ModuloCustodia').factory('bcsBlotterMovimientosEnTransitoGrid',['bcsComunes','$rootScope',
		function(bcsComunes,$rootScope,$translate,$timeout,$modal,$state,$scope)  
{
	var obj = {};	
	
	
	//MOVIMIENTO DE Carteras
	modeloMovimientos = function() {
		return {
    		model: {
    			fields: {
    				IndicadorIngEgr 				: { type: "string"},
					IdClasificacionTipoInstrumento 	: { type: "string"},			    	        		
					IdConcepto 						: { type: "string"},									
					IdComprobanteFactReferencia 	: { type: "number"},
					FolioExternoReferencia 			: { type: "String"},
					IdMovimientoCarteraEnTransito 	: { type: "number"},
					FechaOperacion 					: { type: "sysdate"},
					FechaIngresoRegistro 			: { type: "sysdate"},
					DocumentoIdentidad 				: { type: "string"},
					IdTipoOperacion 				: { type: "string"},									
					IdTipoProducto 					: { type: "string"},
					IdTipoInstrumento 				: { type: "string"},
					IdInstrumento					: { type: "number"},
					Nemotecnico 					: { type: "string"},
					Cantidad 						: { type: "number"},
					PrecioCompra					: { type: "number"},
					MontoCompra						: { type: "number"},									
					IdMonedaInstrumento 			: { type: "string"},
					IdMonedaSistema 				: { type: "string"},
					TasaCompra 						: { type: "number"},
					IdCondicionLiquidacion 			: { type: "string"},									
					FechaVencimientoInstrumento 	: { type: "sysdate"},
					FechaEmisionInstrumento 		: { type: "sysdate"},
					FechaVencimientoOperacion 		: { type: "sysdate"},
					CodigoBolsa 					: { type: "string"},
					IdCliente 						: { type: "number"},
					IdPortafolio 					: { type: "number"},
					DocumentoIdentidad 				: { type: "string"},
					IdBolsa 						: { type: "string"},
					FechaOperacion 					: { type: "sysdate"},
					FechaLiquidacion 				: { type: "sysdate"},
					EsNominal 						: { type: "string"},
					PermiteCertificacionPorUsuario	: { type: "string"},
					CodigoPortafolio 				: { type: "string"},
					IdBoveda 						: { type: "string"},
					CodigoCondicionLiquidacion 		: { type: "string"},
					FechaLiquidacion				: { type: "sysdate"},
					IdEmisor						: { type: "string"},
					IdTipoComprobante 				: { type: "string"},
					FolioComprobante				: { type: "number"},
					Relacion 						: { type: "string"},
					NombreCliente 					: { type: "string"},
					MonedaReajuste					: { type: "string"},
					ValorRescate					: { type: "number"},
					GeneraTraspaso 					: { type: "string"},
					EsTransferenciaPortafolio 		: { type: "string"},
					EsTransferenciaCliente 			: { type: "string"},
					NombreConcepto					: { type: "string"},
					NemoSVS 						: { type: "string"},
					DocumentoIdentidadDestino 		: { type: "string"},
					ClienteDestino 			        : { type: "string"},
					PortafolioDestino				: { type: "number"},
					Producto 						: { type: "string"},
					NombreCliente					: { type: "string"},
					NombreBoveda					: { type: "string"},
					NumeroCuentaBoveda				: { type: "string"},
					NombreEstado 					: { type: "string"},
					MontoCompraMonedaInstrumento	 : { type: "number"},
					ExcluyeMovimientoEnCartola		: { type: "string"}
        		} 
        	}
		};
	};

	modeloMovimientosAgrupados = function() {
		return {
    		model: {
    			fields: {
    				IdCliente 						: { type: "number"},
					DocumentoIdentidad 				: { type: "string"},			    	        		
					NombreCliente 					: { type: "string"},									
					IdTipoOperacion 				: { type: "string"},
					NombreTipoOperacion 			: { type: "string"},
					IdClasificacionTipoInstrumento 	: { type: "string"},
					EsNominal 						: { type: "string"},
					FechaOperacion 					: { type: "sysdate"},
					FechaLiquidacion 				: { type: "sysdate"},
					IdCondicionLiquidacion 			: { type: "string"},
					IdConcepto 						: { type: "string"},
					NombreConcepto					: { type: "string"},
					Nemotecnico 					: { type: "string"},
					IdMonedaInstrumento 			: { type: "string"},
					IdBoveda 						: { type: "string"},
					NombreBoveda					: { type: "string"},
					IdEstado						: { type: "string"},					
					PermiteCertificacionPorUsuario	: { type: "string"},
					CantidadAgrupada				: { type: "number"},
					MontoCompraAgrupado				: { type: "number"},
					MontoAgrupadoMonedaInstrumento	: { type: "number"}					
        		} 
        	}
		};
	};

	modeloTotalizador = function() {
		return {
    		model: {
    			fields: {
    				CantidadAgrupada 				: { type: "number"},
					MontoAgrupadoMonedaInstrumento 	: { type: "number"}								
        		} 
        	}
		};
	};
	

	obj.columnasMovimientos = function() {
		return  [
    		 {field: "IdMovimientoCarteraEnTransito", 	title: 'UI_MOVIMIENTO_ABR', 				width: "70px" }
    		,{field: "NombreEstado", 					title: 'UI_ESTADO', 						width: "70px", groupable: true,	hidden: true }
    		,{field: "IdTipoComprobante", 				title: 'UI_TIPO_COMPROBANTE', 				width: "50px", groupable: true }
    		,{field: "FolioComprobante",				title: 'UI_FOLIO_COMPROBANTE',				width: "80px", groupable: true }
    		,{field: "EsNominal", 						title: 'UI_NOMINAL', 						width: "50px", groupable: true }
    		,{field: "Relacion", 						title: 'UI_RELACION', 						width: "40px", groupable: true }
    		,{field: "DocumentoIdentidad",				title: 'UI_CLIENTE', 						width: "80px", groupable: true}
    		,{field: "NombreCliente",					title: 'UI_NOMBRE',							width: "120px", groupable: true	}
    		,{field: "CodigoPortafolio", 				title: 'UI_PORTAFOLIO_ABR',	 				width: "50px", groupable: true }
    		,{field: "IdClasificacionTipoInstrumento", 	title: 'UI_MERCADO', 						width: "50px", groupable: true }
    		,{field: "FechaOperacion", 					title: 'UI_FECHA_OPERACION_ABR', 			width: "80px", groupable: true }
    		,{field: "FechaLiquidacion", 				title: 'UI_FECHA_LIQUIDACION', 				width: "80px", groupable: true }
    		,{field: "IndicadorIngEgr", 				title: 'UI_OPERACION_ABR', 					width: "60px", groupable: true }
    		,{field: "IdConcepto", 						title: 'UI_CODIGO_CONCEPTO', 				width: "55px", groupable: true }
    		,{field: "NombreConcepto", 					title: 'UI_CONCEPTO_ABR', 					width: "80px", groupable: true }
    		,{field: "Nemotecnico", 					title: 'UI_NEMOTECNICO', 					width: "100px", groupable: true }
    		,{field: "IdEmisor", 						title: 'UI_EMISOR', 						width: "70px",  groupable: true }
    		,{field: "FechaVencimientoInstrumento", 	title: 'UI_FECHA_VENCIMIENTO',				width: "80px", groupable: true }
    		,{field: "FechaVencimientoOperacion", 		title: 'UI_FECHA_VENCIMIENTO_OPERACION', 	width: "80px", groupable: true }
    		,{field: "CodigoCondicionLiquidacion", 		title: 'UI_CONDICION_LIQUIDACION_ABR', 		width: "50px", groupable: true }
    		,{field: "NemoSVS", 						title: 'UI_NEMO_SVS', 						width: "80px", groupable: true}
    		,{field: "DocumentoIdentidadDestino",		title: 'UI_CLIENTE_RUT_DESTINO', 			width: "80px", groupable: true}
    		,{field: "ClienteDestino",					title: 'UI_NOMBRE_DESTINO',					width: "120px", groupable: true	}
    		,{field: "PortafolioDestino", 				title: 'UI_PORTAFOLIO_DESTINO',	 			width: "50px", groupable: true }
    		,{field: "Cantidad", 						title: 'UI_CANTIDAD', 						width: "90px",	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>" }
    		,{field: "MonedaReajuste", 					title: 'UI_MONEDA_TIPO_REAJUSTE', 			width: "55px", groupable: true  }
    		,{field: "ValorRescate", 					title: 'UI_VALOR_RESCATE', 					width: "90px", template : " <label> #:  (util.bcs_formato(ValorRescate,'GEN','Monto',MonedaReajuste,'','')).valor #</label>" }
    		,{field: "PrecioCompra", 					title: 'UI_PRECIO_MONEDA_INSTRUMENTO', 		width: "90px", template : " <label> #:  (util.bcs_formato(PrecioCompra,'GEN','Precio',MonedaReajuste,IdTipoInstrumento,'')).valor #</label>" }
    		,{field: "TasaCompra", 						title: 'UI_TASA', 							width: "70px", template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa',MonedaReajuste,IdTipoInstrumento,'')).valor #</label>" }
    		,{field: "MontoCompra", 					title: 'UI_MONTO_MONEDA_LOCAL', 			width: "90px", template : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>" }
    		,{field: "MontoCompraMonedaInstrumento",	title: 'UI_MONTO_MONEDA_ORIGEN',			width: "90px", template : " <label> #:  (util.bcs_formato(MontoCompraMonedaInstrumento,'GEN','Monto',MonedaReajuste,'','')).valor #</label>" }
    		//,{field: "IdBoveda", 						title: 'UI_BOVEDA_ABR', 					width: "75px", groupable: true }
    		,{field: "NombreBoveda", 					title: 'UI_BOVEDA', 						width: "75px", 	groupable: true }
	    	,{field: "NumeroCuentaBoveda", 				title: 'UI_NUMERO_CUENTA', 					width: "75px", 	groupable: true	}
    		,{field: "Producto", 						title: 'UI_PRODUCTO', 						width: "100px", groupable: true }
    		,{field: "ExcluyeMovimientoEnCartola", 		title: 'UI_EXCLUYE_MOVIMIENTO_EN_CARTOLA',	width: "175px", groupable: true }
    		
		];
	};


	obj.columnasMovimientosAgrupados = function() {
		return  [

    		{field: "DocumentoIdentidad",				title: 'UI_CLIENTE', 						width: "80px", groupable: true }
    		,{field: "NombreCliente",					title: 'UI_NOMBRE',							width: "120px", groupable: true	}
    		,{field: "IdClasificacionTipoInstrumento", 	title: 'UI_MERCADO', 						width: "50px", groupable: true }
    		,{field: "FechaOperacion", 					title: 'UI_FECHA_OPERACION_ABR', 			width: "110px", groupable: true }
    		,{field: "FechaLiquidacion", 				title: 'UI_FECHA_LIQUIDACION', 				width: "110px", groupable: true }
    		,{field: "IdConcepto", 						title: 'UI_CODIGO_CONCEPTO', 				width: "100px", groupable: true }
    		,{field: "NombreConcepto", 					title: 'UI_NOMBRE_CONCEPTO', 					width: "100px", groupable: true }
    		,{field: "Nemotecnico", 					title: 'UI_NEMOTECNICO', 					width: "100px", groupable: true }
			,{field: "NombreTipoOperacion", 			title: 'UI_OPERACION_ABR', 					width: "60px", groupable: true }
			//,{field: "CodigoCondicionLiquidacion", 		title: 'UI_CONDICION_LIQUIDACION_ABR', 		width: "50px", groupable: true }
			,{field: "IdCondicionLiquidacion", 			title: 'UI_CONDICION_LIQUIDACION_ABR', 		width: "100px", groupable: true }
			,{field: "NombreBoveda", 					title: 'UI_BOVEDA', 						width: "75px", 	groupable: true }
			,{field: "CantidadAgrupada", 				title: 'UI_CANTIDAD', 						width: "90px",	template : " <label> #:  (util.bcs_formato(CantidadAgrupada,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>" }
			,{field: "MontoAgrupadoMonedaInstrumento",	title: 'UI_MONTO_MONEDA_ORIGEN',			width: "110px", template : " <label> #:  (util.bcs_formato(MontoAgrupadoMonedaInstrumento,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>" }
    		,{field: "MontoCompraAgrupado", 			title: 'UI_MONTO_MONEDA_LOCAL', 			width: "110px", template : " <label> #:  (util.bcs_formato(MontoCompraAgrupado,'GEN','Monto','"+ $rootScope.SesionIdMonedaSistema +"','','')).valor #</label>" }		
    		,{field: "EsNominal", 						title: 'UI_NOMINAL', 						width: "50px", groupable: true }

    		
		];
	};
	return obj;
}]);	
