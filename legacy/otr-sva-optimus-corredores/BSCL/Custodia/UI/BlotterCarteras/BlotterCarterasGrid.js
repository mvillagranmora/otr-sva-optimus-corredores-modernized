angular.module('ModuloCustodia').factory('bcsBlotterCarterasGrid',['bcsComunes','$rootScope',
		function(bcsComunes,$rootScope,$translate,$timeout,$modal,$state,$scope)  
{
	var obj = {};	

	//MOVIMIENTO DE Carteras
	modeloCarteras = function() {
		return {
    		model: {
    			fields: {
        			IdCartera: 					{ type: "number"},
        			FolioCartera: 				{ type: "number"},
        			FechaLiquidacion: 			{ type: "sysdate"},
        			FechaOperacion:				{ type: "sysdate"},
        			INGFechaOperacion:			{ type: "sysdate"},
        			NombreEstado: 				{ type: "string"},
        			DocumentoIdentidad: 		{ type: "string"},
        			IdCliente: 					{ type: "number"},
        			CodigoPortafolio: 			{ type: "string"},
        			Nemotecnico:				{ type: "string"},
        			FechaVencimientoInstrumento:{ type: "sysdate"},
        			IdBoveda: 					{ type: "string"},
        			Cantidad: 					{ type: "number"},
        			INGCantidad: 				{ type: "number"},
        			IdMonedaInstrumento:		{ type: "string"},
        			IdMonedaSistema: 			{ type: "string"},
        			PrecioCompra:				{ type: "number"},
        			MontoCompra: 				{ type: "number"},
        			INGMontoATasaCompraActual:  { type: "number"},
        			TipoInstrumento: 			{ type: "string"},
        			EsFungible: 				{ type: "string"},
        			TasaCompra: 				{ type: "number"},
        			INGTasaMercadoActual: 		{ type: "number"},
        			NemoSVS: 					{ type: "string"},
        			IdTipoComprobante:			{ type: "string"},
        			FolioComprobante:			{ type: "number"},
        			FechaComprobante:			{ type: "sysdate"},
        			INGPrecioPromedioCompra: 	{type: "number"},
        			INGMontoPrecioPromedioCompra: { type: "number"},
        			INGPrecioMercadoActual:		{ type: "number"},
        			INGMontoATasaMercadoActual: { type: "number"},
        			INGFechaCorteCupon:			{ type: "sysdate"},
        			INGMontoCorteCupon: 		{ type: "number"},
        			INGMontoCapitalCorteCupon:	{ type: "number"},
        			INGMontoInteresCorteCupon:	{ type: "number"},
        			RentabilidadReal:			{ type: "number"},
        			CostoUF:					{ type: "number"},
        			DiferenciaInversion: 		{ type: "number"},
        			EsPagaderoEn: 				{ type: "string"},
        			IdMonedaReajuste: 			{ type: "string"},
        			
        			IdClasificacionTipoInstrumento: { type: "string"},
        			IdEmisor: { type: "string"},
        			INGPrecioCompra: { type: "number"},
        			INGFuenteTasaPrecioMercadoActual: { type: "string"},
        			IdConcepto: { type: "string"},
        			NombreConcepto: { type: "string"},
        			CodigoIFRS: { type: "string"}
        		} 
        	}
		};
	};
	
	modeloCarterasInstrumento = function() {
		return {
    		model: {
    			fields: {
        			IdCartera: 					{ type: "number"},
        			FolioCartera: 				{ type: "number"},
        			FechaLiquidacion: 			{ type: "sysdate"},
        			FechaOperacion:				{ type: "sysdate"},
        			INGFechaOperacion:			{ type: "sysdate"},
        			NombreEstado: 				{ type: "string"},
        			DocumentoIdentidad: 		{ type: "string"},
        			IdCliente: 					{ type: "number"},
        			CodigoPortafolio: 			{ type: "string"},
        			Nemotecnico:				{ type: "string"},
        			FechaVencimientoInstrumento:{ type: "sysdate"},
        			IdBoveda: 					{ type: "string"},
        			Cantidad: 					{ type: "number"},
        			INGCantidad: 				{ type: "number"},
        			IdMonedaInstrumento:		{ type: "string"},
        			IdMonedaSistema: 			{ type: "string"},
        			PrecioCompra:				{ type: "number"},
        			MontoCompra: 				{ type: "number"},
        			INGMontoATasaCompraActual:  { type: "number"},
        			TipoInstrumento: 			{ type: "string"},
        			EsFungible: 				{ type: "string"},
        			TasaCompra: 				{ type: "number"},
        			INGTasaMercadoActual: 		{ type: "number"},
        			RazonSocial: 				{ type: "string"},
        			NemoSVS: 					{ type: "string"},
        			IdTipoComprobante:			{ type: "string"},
        			FolioComprobante:			{ type: "number"},
        			FechaComprobante:			{ type: "sysdate"},
        			INGPrecioPromedioCompra: 	{type: "number"},
        			INGMontoPrecioPromedioCompra: { type: "number"},
        			INGPrecioMercadoActual:		{ type: "number"},
        			INGMontoATasaMercadoActual: { type: "number"},
        			INGFechaCorteCupon:			{ type: "sysdate"},
        			INGMontoCorteCupon: 		{ type: "number"},
        			INGMontoCapitalCorteCupon:	{ type: "number"},
        			INGMontoInteresCorteCupon:	{ type: "number"},
        			DiferenciaInversion: 		{ type: "number"},
        			EsPagaderoEn: 				{ type: "string"},
        			IdMonedaReajuste: 			{ type: "string"},
        		} 
        	}
		};
	};

	
	obj.columnasCarteras = function() {
		return  [
	    		 {field: "FolioCartera", title: 'UI_FOLIO_CARTERA_ABR', width: "70px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FechaOperacion", title: 'UI_FECHA_INGRESO_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
		    	,{field: "FechaLiquidacion", title: 'UI_FECHA_LIQUIDACION_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "DocumentoIdentidad", title: 'UI_CLIENTE', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "85px", template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>", groupable: true   } 
	    		,{field: "CodigoPortafolio", title: 'UI_PORTAFOLIO_ABR', attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, width: "50px", groupable: true }
	    		,{field: "IdClasificacionTipoInstrumento", title: 'UI_MERCADO', width: "50px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template: "<span class='flags' title='#=ClasificacionTipoInstrumento#'>#=IdClasificacionTipoInstrumento#</span>", groupable: true }
	    		,{field: "TipoInstrumento", title: 'UI_TIPO_INSTRUMENTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "Nemotecnico", title: 'UI_INSTRUMENTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdEmisor", title: 'UI_EMISOR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true  }
	    		,{field: "FechaVencimientoInstrumento", title: 'UI_VENCIMIENTO', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "IdBoveda", title: 'UI_BOVEDA_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "INGCantidad", title: 'UI_CANTIDAD', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "80px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" , template : " <label> #:  (util.bcs_formato(INGCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>"}
	    		
	    		,{field: "IdMonedaReajuste", title: 'UI_MONEDA_TIPO_REAJUSTE', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "INGPrecioCompra", title: 'UI_PRECIO_COMPRA_ABR', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioCompra,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>"}
				,{field: "TasaCompra", title: 'UI_TASA_COMPRA', width: "60px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(TasaCompra,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
				,{field: "INGMontoATasaCompraActual", title: 'UI_MONTO_COMPRA', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "INGPrecioMercadoActual", title: 'UI_PRECIO_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", title: 'UI_TASA_MERCADO', width: "60px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoATasaMercadoActual", title: 'UI_MONTO_MERCADO', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoATasaMercadoActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGFuenteTasaPrecioMercadoActual", title: 'UI_FUENTE_MERCADO', width: "150px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, grouupable: true  }
	    		
	    		,{field: "INGPrecioPromedioCompra", title: 'UI_PRECIO_PROMEDIO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioPromedioCompra,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoPrecioPromedioCompra", title: 'UI_MONTO_PROMEDIO', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoPrecioPromedioCompra,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "DiferenciaInversion", title: 'UI_DIFERENCIA_MERCADO', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(DiferenciaInversion,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "EsFungible", title: 'UI_FUNGIBLE', width: "50px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "IdConcepto", title: 'UI_CONCEPTO_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>", groupable: true  }
	    		,{field: "NombreConcepto", title: 'UI_CONCEPTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true  }
	    		,{field: "CodigoIFRS", title: 'UI_CATEGORIA_IFRS_ABR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "NemoSVS", title: 'UI_NEMO_SVS', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, grouupable: true  }
	    		
	    		,{field: "IdTipoComprobante", title: 'UI_TIPO_COMPROBANTE', width: "60px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FolioComprobante", title: 'UI_FOLIO_COMPROBANTE', width: "50px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, format: "{0:n0}" }
	    		,{field: "FechaComprobante", title: 'UI_FECHA_COMPROBANTE', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		
	    		,{field: "INGFechaCorteCupon", title: 'UI_FECHA_CORTE_CUPON', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "INGMontoCorteCupon", title: 'UI_MONTO_CORTE_CUPON', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoCorteCupon,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoCapitalCorteCupon", title: 'UI_CAPITAL_CORTE_CUPON', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoCapitalCorteCupon,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGMontoInteresCorteCupon", title: 'UI_INTERES_CORTE_CUPON', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoInteresCorteCupon,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "RentabilidadReal", title: 'UI_RENTABILIDAD_REAL', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(RentabilidadReal,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "CostoUF", title: 'UI_COSTO_UF', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(CostoUF,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
		];
	}; 
	
	
	obj.columnasCarterasInstrumento = function() {
		return  [
	    		 {field: "FolioCartera", title: 'UI_FOLIO_CARTERA_ABR', width: "70px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FechaOperacion", title: 'UI_FECHA_INGRESO_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
		    	,{field: "FechaLiquidacion", title: 'UI_FECHA_LIQUIDACION_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true  }
		    	,{field: "FechaVencimientoInstrumento", title: 'UI_VENCIMIENTO', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "DocumentoIdentidad", title: 'UI_CLIENTE', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "85px", template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>", groupable: true   } 
	    		,{field: "CodigoPortafolio", title: 'UI_PORTAFOLIO_ABR', attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, width: "50px", groupable: true }
	    		,{field: "IdBoveda", title: 'UI_BOVEDA_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
//	    		,{field: "INGCantidad", title: 'UI_CANTIDAD', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "80px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#", template : " <label> #: util.bcs_formato(INGCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'').valor #</label>"}
	    		,{field: "INGCantidad", title: 'UI_CANTIDAD', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "80px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" , template : " <label> #:  (util.bcs_formato(INGCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>"}
	    		,{field: "Nemotecnico", title: 'UI_INSTRUMENTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		
	    		,{field: "IdMonedaReajuste", title: 'UI_MONEDA_TIPO_REAJUSTE', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
//	    		,{field: "INGPrecioCompra", title: 'UI_PRECIO_COMPRA_ABR', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioCompra,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>" }
	    		,{field: "INGPrecioCompra", title: 'UI_PRECIO_COMPRA_ABR', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioCompra,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>"}
	    		
				,{field: "TasaCompra", title: 'UI_TASA_COMPRA', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(TasaCompra,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
				,{field: "INGMontoATasaCompraActual", title: 'UI_MONTO_COMPRA', width: "95px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "INGPrecioMercadoActual", title: 'UI_PRECIO_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", title: 'UI_TASA_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoATasaMercadoActual", title: 'UI_MONTO_MERCADO', width: "95px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGMontoATasaMercadoActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
				,{field: "DiferenciaInversion", title: 'UI_DIFERENCIA', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(DiferenciaInversion,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		
	    		,{field: "INGPrecioPromedioCompra", title: 'UI_PRECIO_PROMEDIO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioPromedioCompra,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoPrecioPromedioCompra", title: 'UI_MONTO_PROMEDIO', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoPrecioPromedioCompra,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }

	    		,{field: "RazonSocial", title: 'UI_NOMBRE_CLIENTE', attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", groupable: true   }
	    		,{field: "EsFungible", title: 'UI_FUNGIBLE', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "IdConcepto", title: 'UI_CONCEPTO_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>" }
	    		,{field: "NombreConcepto", title: 'UI_CONCEPTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "CodigoIFRS", title: 'UI_CATEGORIA_IFRS_ABR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		
	    		,{field: "IdTipoComprobante", title: 'UI_TIPO_COMPROBANTE', width: "60px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FolioComprobante", title: 'UI_FOLIO_COMPROBANTE', width: "50px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, format: "{0:n0}" }
	    		,{field: "FechaComprobante", title: 'UI_FECHA_COMPROBANTE', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		
	    		,{field: "INGFechaCorteCupon", title: 'UI_FECHA_CORTE_CUPON', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "INGMontoCorteCupon", title: 'UI_MONTO_CORTE_CUPON', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoCorteCupon,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoCapitalCorteCupon", title: 'UI_CAPITAL_CORTE_CUPON', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoCapitalCorteCupon,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "INGMontoInteresCorteCupon", title: 'UI_INTERES_CORTE_CUPON', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoInteresCorteCupon,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		
		];
	}; 
	
	return obj;
}]);	


