angular.module('ModuloCustodia').factory('bcsBlotterCustodiaGrid',['bcsComunes','$rootScope',
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
        			FechaLiquidacionStr: 		{ type: "string"},
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
        			NemoSVS: 					{ type: "string"}
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
        			FechaLiquidacionStr: 		{ type: "string"},
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
        			NemoSVS: 					{ type: "string"}
        		} 
        	}
		};
	};
	

	
	
	
	modeloCarteraMovimientos = function() {
		return {
    		model: {
    			fields: {
					IdCartera 			: { type: "number"},
        			FolioCartera 	 	: { type: "number"},
        			FechaLiquidacion 	: { type: "sysdate"},
        			INGFechaOperacion 	: { type: "sysdate"},
        			FechaOperacion 		: { type: "sysdate"},
        			NombreEstado 		: { type: "string"},
        			DocumentoIdentidad 	: { type: "string"},
        			IdCliente 			: { type: "number"},
        			CodigoPortafolio 	: { type: "string"},
        			Nemotecnico 		: { type: "string"},
        			FechaVencimientoInstrumento:{ type: "sysdate"},
        			IdBoveda 			: { type: "string"},
        			Cantidad 			: { type: "number"},
        			INGCantidad 		: { type: "number"},
        			EGRCantidad 		: { type: "number"},
        			Saldo 				: { type: "number"},
        			IdMonedaInstrumento : { type: "string"},
        			IdMonedaSistema 	: { type: "string"},
        			PrecioCompra		: { type: "number"},
        			MontoCompra 		: { type: "number"},
        			EsFungible			: { type: "string"},
        			TasaCompra			: { type: "number"},
        			INGTasaMercadoActual: { type: "number"},
        			EGRIdConcepto		: { type: "string"},
        			EGRNombreConcepto	: { type: "string"}
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
	    		,{field: "INGCantidad", title: 'UI_CANTIDAD', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "80px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,0)#" , template : " <label> #:  (util.bcs_formato(INGCantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
	    		
	    		,{field: "INGPrecioCompraActual", title: 'UI_PRECIO_COMPRA_ABR', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioCompraActual,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
				,{field: "TasaCompra", title: 'UI_TASA_COMPRA', width: "60px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(TasaCompra,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
				,{field: "INGMontoATasaCompraActual", title: 'UI_MONTO_COMPRA', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "INGPrecioMercadoActual", title: 'UI_PRECIO_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", title: 'UI_TASA_MERCADO', width: "60px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoATasaMercadoActual", title: 'UI_MONTO_MERCADO', width: "105px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoATasaMercadoActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "IdBoveda", title: 'UI_BOVEDA_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdMonedaInstrumento", title: 'UI_MONEDA_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "EsFungible", title: 'UI_FUNGIBLE', width: "50px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "IdConcepto", title: 'UI_CONCEPTO_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>", groupable: true  }
	    		,{field: "NombreConcepto", title: 'UI_CONCEPTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true  }
	    		,{field: "CodigoIFRS", title: 'UI_CATEGORIA_IFRS_ABR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "NemoSVS", title: 'UI_NEMO_SVS', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true  }
	    		
		];
	}; 
	
	
	obj.columnasCarterasInstrumento = function() {
		return  [
	    		 {field: "FolioCartera", title: 'UI_FOLIO_CARTERA_ABR', width: "70px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FechaOperacion", title: 'UI_FECHA_INGRESO_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
		    	,{field: "FechaLiquidacion", title: 'UI_FECHA_LIQUIDACION_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true  }
		    	,{field: "FechaVencimientoInstrumento", title: 'UI_VENCIMIENTO', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "IdBoveda", title: 'UI_BOVEDA_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "DocumentoIdentidad", title: 'UI_CLIENTE', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "85px", template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>", groupable: true   } 
	    		,{field: "CodigoPortafolio", title: 'UI_PORTAFOLIO_ABR', attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, width: "50px", groupable: true }
	    		,{field: "INGCantidad", title: 'UI_CANTIDAD', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "80px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,0)#"}
	    		
	    		,{field: "INGPrecioCompraActual", title: 'UI_PRECIO_COMPRA_ABR', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioCompraActual,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
				,{field: "TasaCompra", title: 'UI_TASA_COMPRA', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(TasaCompra,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
				,{field: "INGMontoATasaCompraActual", title: 'UI_MONTO_COMPRA', width: "95px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		
	    		,{field: "INGPrecioMercadoActual", title: 'UI_PRECIO_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", title: 'UI_TASA_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGMontoATasaMercadoActual", title: 'UI_MONTO_MERCADO', width: "95px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGMontoATasaMercadoActual,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		

	    		,{field: "RazonSocial", title: 'UI_NOMBRE_CLIENTE', attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", groupable: true   }
	    		,{field: "EsFungible", title: 'UI_FUNGIBLE', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "IdConcepto", title: 'UI_CONCEPTO_ABR', width: "55px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>" }
	    		,{field: "NombreConcepto", title: 'UI_CONCEPTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "CodigoIFRS", title: 'UI_CATEGORIA_IFRS_ABR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		
		];
	}; 
	
	obj.columnasCarteraMovimientos = function() {
		return  [
	    		 {field: "FolioCartera", title: 'UI_FOLIO_CARTERA_ABR', width: "85px", headerAttributes:{style:"text-align: left;"}, attributes:{style:"text-align:right; white-space:nowrap;"}, groupable: true }
    		 	,{field: "INGFechaOperacion", title: 'UI_FECHA_INGRESO_ABR',  width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "IdConceptoGrilla", title: 'UI_CONCEPTO_ABR', width: "65px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "NombreConceptoGrilla", title: 'UI_CONCEPTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdClasificacionTipoInstrumento", title: 'UI_MERCADO', width: "85px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template: "<span class='flags' title='#=ClasificacionTipoInstrumento#'>#=IdClasificacionTipoInstrumento#</span>", groupable: true }
	    		,{field: "TipoInstrumento", title: 'UI_TIPO_INSTRUMENTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "Nemotecnico", title: 'UI_NEMO_ABR', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdEmisor", title: 'UI_EMISOR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FechaVencimientoInstrumento", title: 'UI_VENCIMIENTO', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "INGCantidad", title: 'UI_CANTIDAD_INICIAL_ABR', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "90px" }
	    		,{field: "EGRCantidad", title: 'UI_CANTIDAD_EGRESO_ABR', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "90px" }
	    		,{field: "Saldo", title: 'UI_SALDO_ABR', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "75px" }
	    		,{field: "EGRIdConcepto", title: 'UI_CONCEPTO_ABR', width: "65px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
				,{field: "IdMonedaInstrumento", title: 'UI_MONEDA_ABR', width: "63px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "PrecioCompra", title: 'UI_PRECIO', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", template : " <label> #:  (util.bcs_formato(PrecioCompra,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "MontoCompra", title: 'UI_MONTO', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", template : " <label> #:  (util.bcs_formato(MontoCompra,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "TasaCompra", title: 'UI_TASA_COMPRA', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(TasaCompra,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "INGTasaMercadoActual", title: 'UI_TASA_MERCADO', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "EsFungible", title: 'UI_FUNGIBLE', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
				,{field: "IdCartera", title: 'UI_SECUENCIA_ABR', width: "85px", headerAttributes:{style:"text-align: left;"}, attributes:{style:"text-align:right; white-space:nowrap;"} }
	    		,{field: "FechaLiquidacion", title: 'UI_FECHA_LIQUIDACION_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "IdEstado", title: 'UI_ESTADO_ABR', width: "50px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "DocumentoIdentidad", title: 'UI_CLIENTE', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "85px", template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>", groupable: true   }
	    		,{field: "CodigoPortafolio", title: 'UI_PORTAFOLIO_ABR', attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"}, width: "58px", groupable: true }
	    		,{field: "IdBoveda", title: 'UI_BOVEDA_ABR', width: "67px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "CodigoIFRS", title: 'UI_CATEGORIA_IFRS_ABR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		
		];
	};
	
	
	obj.columnasCartolaMovimientos = function() {
		return  [
	    		 {field: "FolioCartera", title: 'UI_FOLIO_CARTERA_ABR', width: "85px", headerAttributes:{style:"text-align: left;"}, attributes:{style:"text-align:right; white-space:nowrap;"}, groupable: true }
    		 	,{field: "FechaOperacion", title: 'UI_FECHA_OPERACION',  width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "TipoOperacion", title: 'UI_OPERACION', width: "65px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "NombreConcepto", title: 'UI_CONCEPTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdClasificacionTipoInstrumento", title: 'UI_MERCADO', width: "50px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdTipoInstrumento", title: 'UI_TIPO_INSTRUMENTO', width: "50px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "Nemotecnico", title: 'UI_INSTRUMENTO', width: "100px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "IdEmisor", title: 'UI_EMISOR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "FechaVencimientoInstr", title: 'UI_FECHA_VENCIMIENTO_ABR', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		//,{field: "FechaVencimientoInstrumento", title: 'UI_VENCIMIENTO', width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
	    		,{field: "Cantidad", title: 'UI_CANTIDAD', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "90px", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "Saldo", title: 'UI_SALDO', format: "{0:n0}", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "90px" }
	    		,{field: "IdEmisor", title: 'UI_EMISOR', width: "80px", attributes:{style:"text-align:left; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "IdMonedaInstr", title: 'UI_MONEDA_ABR', width: "63px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"} }
	    		,{field: "IdBoveda", title: 'UI_BOVEDA_ABR', width: "67px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, groupable: true }
	    		,{field: "Precio", title: 'UI_PRECIO', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", template : " <label> #: (util.bcs_formato(Precio,'CUS','Precio','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "Tasa", title: 'UI_TASA', width: "90px", attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, template : " <label> #:  (util.bcs_formato(Tasa,'CUS','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "Monto", title: 'UI_MONTO', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", template : " <label> #:  (util.bcs_formato(Monto,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
	    		,{field: "ValorRescate", title: 'UI_VALOR_RESCATE', attributes:{style:"text-align:right; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}, width: "100px", template : " <label> #: (util.bcs_formato(ValorRescate,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }
	    		,{field: "FolioComprobante", title: 'UI_FOLIO_COMPROBANTE', width: "85px", headerAttributes:{style:"text-align: left;"}, attributes:{style:"text-align:right; white-space:nowrap;"}, groupable: true }
				,{field: "IdTipoComprobante", title: 'UI_TIPO_COMPROBANTE', width: "85px", headerAttributes:{style:"text-align: left;"}, attributes:{style:"text-align:right; white-space:nowrap;"}, groupable: true }
		];
	};
	
	return obj;
}]);	


