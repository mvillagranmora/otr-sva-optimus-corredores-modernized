angular.module('ModuloCustodia').factory('bcsCoberturaGarantia',function($translate) {
 
	var obj = {};
	
	//-----------------------------------
	// COBERTURA GARANTIA RESUMEN
	//-----------------------------------
									  		
	obj.dsCoberturaGarantiaResumen = new bcs.DataSource({
	               type : "call",
	               definition : {
           	   			   pageSize			: 100,
                           callTransaction 	: "1763",
                           callResultSet    : "P_OUT_GARANTIASRESUMEN",                           
                           schema : {
                                    model : {
                                              fields : {
													IdCliente    					: { type: "number"},
													IdPortafolio					: { type: "number"},
													CodigoPortafolio             	: { type: "string"},
													PropositoPortafolio             : { type: "string"},
													NombreCliente       			: { type: "string"},
													DocumentoIdentidad  			: { type: "string"},
													IdTipoInstrumentoGarantizado    : { type: "string"},
													NombreTipoInstrumentoGarantizado: { type: "string"},
				                                    IdInstrumentoCarteraGarantizada : { type: "number"},
				             						NemoGarantizado          		: { type: "string"},
				             						IdContraparteGarantia           : { type: "number"},
													RazonSocialContraparte   		: { type: "string"},
													EsEspecifica             		: { type: "string"},
													IdOperacionOrigenGarantia		: { type: "number"},
													IdMonedaLiquidacionOperacion	: {type:  "string"},
													MontoCoberturaEntidad			: { type: "number"},
													MontoCoberturaCorredora			: { type: "number"},													
													MontoExigibilidadEntidad		: { type: "number"},													
													MontoExigibilidadCorredora		: { type: "number"},													
													EstadoCoberturaEntidad         	: { type: "string"},
													EstadoCoberturaCorredora		: { type: "string"},
													IdConcepto						: { type: "string"},
													FolioCarteraGarantizada			: { type: "number"},
													PorcentajeCoberturaEntidad		: { type: "number"},
													PorcentajeCoberturaCorredora	: { type: "number"},
													PorcentajeExigibilidadEntidad	: { type: "number"},
													PorcentajeExigibilidadCorredora	: { type: "number"}
,                                                    }
                                            }
	                               }
	               }
	});

	
	obj.columnasCoberturaGarantiaResumen = function() {
		return  [
			{ field:  "NombreCliente"				,title: 'UI_CLIENTE'					,width: "100px", attributes:{style:"white-space:nowrap;"}},
         	{ field:  "DocumentoIdentidad"			,title: 'UI_DOCUMENTO_IDENTIDAD'		,width: "63px"},	
         	{ field:  "CodigoPortafolio"			,title: 'UI_PORTAFOLIO'					,width: "70px"},
         	{ field:  "IdTipoInstrumentoGarantizado",title: 'UI_ID_TIPO_INSTRUMENTO'		,width: "53px"},
         	{ field:  "NombreTipoInstrumentoGarantizado",title: 'UI_NOMBRE_TIPO_INSTRUMENTO',width: "53px"},
         	{ field:  "EstadoCoberturaEntidad"		,title: 'UI_EST_COBERTURA_ENT' 			,width: "65px"},
         	{ field:  "EstadoCoberturaCorredora"	,title: 'UI_EST_COBERTURA_CORR'			,width: "65px"},
         	{ field:  "EsEspecifica"				,title: 'UI_ESPECIFICA'		        	,width: "50px"},		         	
         	{ field:  "IdOperacionOrigenGarantia"	,title: 'UI_NUMERO_OPERACION'       	,width: "75px"},
         	{ field:  "IdMonedaLiquidacionOperacion",title: 'UI_MONEDA_LIQUIDACION'			,width: "53px"},
         	{ field:  "RazonSocialContraparte"		,title: 'UI_ENTIDAD_DESTINO'			,width: "80px", attributes:{style:"white-space:nowrap;"}},
         	{ field:  "MontoCoberturaEntidad"		,title: 'UI_MONTO_COBERTURA_ENT'	    ,width: "70px", template : " <label> #:  (util.bcs_formato(MontoCoberturaEntidad,'CUS','Monto','CLP','','')).valor #</label>"}, //FALTA.. moneda debe ser dinamico
         	{ field:  "PorcentajeCoberturaEntidad"  ,title: 'UI_PORCENTAJE_COBERTURA_ENT'	,width: "70px"},
         	{ field:  "MontoCoberturaCorredora"		,title: 'UI_MONTO_COBERTURA_CORR'	    ,width: "70px", template : " <label> #:  (util.bcs_formato(MontoCoberturaCorredora,'CUS','Monto','CLP','','')).valor #</label>"}, //FALTA.. moneda debe ser dinamico	              
         	{ field:  "PorcentajeCoberturaCorredora"  ,title: 'UI_PORCENTAJE_COBERTURA_CORR',width: "70px"},
         	{ field:  "MontoExigibilidadEntidad"	,title: 'UI_EXIGIBILIDAD_ENTIDAD'		,width: "70px", template : " <label> #:  (util.bcs_formato(MontoExigibilidadEntidad,'CUS','Monto','CLP','','')).valor #</label>"}, //FALTA.. moneda debe ser dinamico
         	{ field:  "PorcentajeExigibilidadEntidad"  ,title: 'UI_PORCENTAJE_EXIGIBILIDAD_ENT'	,width: "70px"},
         	{ field:  "MontoExigibilidadCorredora"	,title: 'UI_EXIGIBILIDAD_CORREDORA'		,width: "70px", template : " <label> #:  (util.bcs_formato(MontoExigibilidadCorredora,'CUS','Monto','CLP','','')).valor #</label>"}, //FALTA.. moneda debe ser dinamico
         	{ field:  "PorcentajeExigibilidadCorredora"  ,title: 'UI_PORCENTAJE_EXIGIBILIDAD_CORR'	,width: "70px"}
        ];
	};
	 
	
	//----------------------------------------
	// COBERTURA GARANTIA DETALLE
	//----------------------------------------	
	obj.dsCoberturaGarantiaDetalle = function(){
		return {
			type : "call",
	        definition : {
	        	pageSize : 5,
	            callParameters : '',
	        	callTransaction : "4290620",
	            callResultSet : "P_OUT_GARANTIASDETALLE",                           
	            schema : {
	                 model : {
	                	 fields : {
	                		FechaLiquidacion			: {type: "sysdate"},
	                		Nemotecnico					: {type: "string"},
	                		FolioGarantia				: {type: "number"},
	                		NombreTipoInstrumento		: {type: "string"},
	                		NemoGarantizado         	: {type: "string"},
	                		NombreConcepto				: {type: "string"},													
	                		IdMonedaInstrumento			: {type: "string"},													
                		 	NombreOrigenGarantia 		: {type: "string"},
	                		EsEspecifica				: {type: "string"},
                		 	IdBolsaOperacionOrigen		: {type: "string"},													
                		 	MontoCoberturaEntidad		: {type: "number"},
                		 	PorcentajeCoberturaEntidad	: {type: "number"},
                		 	MontoCoberturaCorredora		: {type: "number"},													
                		 	PorcentajeCoberturaCorredora: {type: "number"},
                		 	CantidadGarantia			: {type: "number"},
                		 	MontoValorizado				: {type: "number"},
	                		IdOperacionOrigenGarantia	: {type: "number"},
                		 	PrecioGarantia				: {type: "number"},
	                		PrecioMercado				: {type: "number"},
	                   	}
	                 }
	            }
	        }
		};
	};
        
	obj.columnasCoberturaGarantiaDetalle = function() {
		return  [	
		   	 {field: "FechaLiquidacion", 		  	title: "UI_FECHA_INGRESO", width: "80px", align: "center", sortable : false}
		   	,{field: "NombreConcepto", 			  	title: "UI_CONCEPTO", width: "150px", align: "left", sortable : false, attributes:{style:"white-space:nowrap;"} }
		   	,{field: "NombreOrigenGarantia",	 	title: "UI_ORIGEN", width: "55px", align: "left", sortable : false, attributes:{style:"white-space:nowrap;"} }
		   	,{field: "Nemotecnico", 			  	title: "UI_INSTRUMENTO", width: "90px", align: "left", sortable : false  }
			,{field: "CantidadGarantia", 		  	title: "UI_CANTIDAD", width: "80px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(CantidadGarantia,'CUS','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:IdTipoReajuste),'','')).valor #</label>" }
			,{field: "PrecioGarantia", 		  	  	title: "UI_PRECIO_MERCADO", width: "95px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioGarantia,'GEN','Precio','CLP',IdTipoInstrumento,'')).valor #</label>"}
			,{field: "PrecioMercado", 		  	  	title: "UI_PRECIO_VAR", width: "95px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(PrecioMercado,'GEN','Precio','CLP',IdTipoInstrumento,'')).valor #</label>"}
			,{field: "MontoValorizado", 		  	title: "UI_MONTO_VALORIZADO", width: "95px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoValorizado,'CUS','Monto','CLP','','')).valor #</label>"}
			,{field: "IdMonedaInstrumento", 	  	title: "UI_MONEDA", width: "55px", align: "left", sortable : false,  template: kendo.template("#if ( IdClasificacionTipoInstrumento == 'RF' || IdClasificacionTipoInstrumento == 'IF') {##=IdTipoReajuste##} else {##=IdMonedaInstrumento##}#")}
			,{field: "NemoGarantizado", 		  	title: "UI_OPERACION", width: "150px", align: "left"}
			,{field: "FolioGarantia", 			  	title: "UI_FOLIO", width: "70px",   }
			,{field: "NombreTipoInstrumento", 	  	title: "UI_TIPO_INSTRUMENTO", width: "150px", align: "left", sortable : false, attributes:{style:"white-space:nowrap;"}}
			,{field: "EsEspecifica", 		  	  	title: "UI_ESPECIFICA",  width: "70px",   }
			,{field: "IdBolsaOperacionOrigen", 	  	title: "UI_ENTIDAD", width: "70px",   }
			,{field: "MontoCoberturaEntidad", 	  	title: "UI_MONTO_COBERTURA_ENT",  width: "120px", sortable : false, template : " <label> #:  (util.bcs_formato(MontoCoberturaEntidad,'CUS','Monto','CLP','','')).valor #</label>"}
			,{field: "PorcentajeCoberturaEntidad",	title: "UI_PORCENTAJE_COBERTURA_ENT",  width: "120px"}
			,{field: "MontoCoberturaCorredora",  	title: "UI_MONTO_COBERTURA_CORR", width: "120px", sortable : false, template : " <label> #:  (util.bcs_formato(MontoCoberturaCorredora,'CUS','Monto','CLP','','')).valor #</label>"}
			,{field: "PorcentajeCoberturaCorredora",title: "UI_PORCENTAJE_COBERTURA_CORR", width: "120px"}
			,{field: "IdOperacionOrigenGarantia", 	title: "UI_NUMERO_OPERACION", width: "80px"}			
        ];
	};	

	 
	//----------------------------------------
	// COBERTURA GARANTIA OPERACIONES
	//----------------------------------------
	
	obj.dsCoberturaGarantiaOperaciones = new bcs.DataSource({
		   type : "odata",
		   definition : {
			   serverFiltering 	: true, // Filtrado de datos en el servidor.
			   serverPaging     : true, // Paginado de los datos en el servidor.
			   serverSorting    : true, // Ordenamiento de los datos en el servidor.
			   pageSize         : 5,
			   filter			: [],			   
			   transport 		: {read : {
									url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1765",
									dataType : "json"
									}
				},
			   schema 			: { model: {
												fields : {
												     IdTipoInstrumentoGarantizado   		: { type : "string"}		// SIM 											 
													,IdInstrumentoGarantizado       		: { type : "number"}        // 2249 (VENSIM)
													,NombreTipoOperacion            		: { type : "string"}        // VENTA SIMULTANEA													 
													,FolioCartera 							: { type : "number"}
													,Nemotecnico 							: { type : "string"}
													,Cantidad 								: { type : "number"}
													,TasaCompra								: { type : "number"}
													,PrecioCompra							: { type : "number"}
													,MontoCompra							: { type : "number"}
													,FechaVencimiento 						: { type : "sysdate"}
													,IdMonedaInstrumento					: { type : "string"}
													,IdTipoComprobanteOperacionRelacionada	: { type : "string"}
													,FolioComprobanteOperacionRelacionada	: { type : "string"}													
													,IdCliente								: { type : "number"}													
													,IdPortafolio 							: { type : "number"}
													,EsEspecifica 							: { type : "string"}
													,IdBolsa 								: { type : "string"}
													,IdContraparteGarantia					: { type : "number"}
													,ExigibilidadEntidad					: { type : "number"}
													,ExigibilidadEntidadPorcentaje			: { type : "number"}
									            	,ExigibilidadCorredora					: { type : "number"}
									            	,ExigibilidadCorredoraPorcentaje		: { type : "number"}
									            	,IdOperacionOrigenGarantia		        : { type : "number"}  //Numero de orden que originó la garantía
													,Plazo									: { type : "number"}
													,TasaTir								: { type : "number"}
												}
											}
										},
		   }
		});

	obj.columnasCoberturaGarantiaOperaciones = function() {
		return  [
				 { field: "IdOperacionOrigenGarantia"   	,title: "UI_NUMERO_OPERACION"   	, width: "80px"},			
		         { field: "IdTipoInstrumentoGarantizado"	,title: 'UI_TIPO_INSTRUMENTO'		, width: "50px" },	         
		         { field: "NombreTipoOperacion"				,title: 'UI_OPERACION'		    	, width: "110px", attributes:{style:"text-align:center;white-space:nowrap;"} },
                 { field: "Nemotecnico"						,title: 'UI_NEMO'					, width: "75px", attributes:{style:"text-align:center;white-space:nowrap;"} },
                 { field: "Cantidad"						,title: 'UI_CANTIDAD'				, width: "60px", template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaInstrumento,'','')).valor #</label>"},
                 { field: "PrecioCompra"					,title: 'UI_PRECIO_COMPRA'			, width: "65px", template : " <label> #:  (util.bcs_formato(PrecioCompra,'GEN','Precio',IdMonedaInstrumento,IdTipoInstrumentoGarantizado,'')).valor #</label>"},
                 { field: "MontoCompra"						,title: 'UI_MONTO_COMPRA'			, width: "80px", template : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"},
                 { field: "TasaCompra"						,title: 'UI_TASA_COMPRA'			, width: "62px", template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa',IdMonedaInstrumento,IdTipoInstrumentoGarantizado,'')).valor #</label>"},
                 { field: "FechaVencimiento"				,title: 'UI_FECHA_VENCIMIENTO'		, width: "80px"},
                 { field: "FolioCartera"					,title: 'UI_FOLIO_CARTERA'			, width: "65px"},
            	 { field: "IdTipoComprobanteOperacionRelacionada"	,title: 'UI_TIPO_COMPROBANTE'		, width: "70px"},
            	 { field: "FolioComprobanteOperacionRelacionada"	,title: 'UI_FOLIO_COMPROBANTE'		, width: "70px"},            	
            	 { field: "IdMonedaInstrumento"				,title: 'UI_MONEDA'					, width: "60px"},            	 
                 { field: "IdBolsa"							,title: 'UI_ENTIDAD'				, width: "44px", attributes:{style:"text-align:center;"}, headerAttributes:{style:"text-align: center;"} },
                 //{ field: "EsEspecifica"					,title: 'UI_ESPECIFICA'         	, width: "53px", attributes:{style:"text-align:center;white-space:nowrap;"}, headerAttributes:{style:"text-align: center;"}},
                 { field: "ExigibilidadEntidad"				,title: 'UI_EXIGIBILIDAD_ENTIDAD'	, width: "120px", template : " <label> #:  (util.bcs_formato(ExigibilidadEntidad,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"},
                 { field: "ExigibilidadEntidadPorcentaje"	,title: 'UI_PORC_EXIG_ENT'			, width: "120px"},
                 { field: "ExigibilidadCorredora"			,title: 'UI_EXIGIBILIDAD_CORREDORA'	, width: "120px", template : " <label> #:  (util.bcs_formato(ExigibilidadCorredora,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"},
                 { field: "ExigibilidadCorredoraPorcentaje"	,title: 'UI_PORC_EXIG_CORR'			, width: "120px"},
				 { field: "Plazo"							,title: 'UI_PLAZO'					, width: "80px"},
				 { field: "TasaTir"							,title: 'UI_TIR'					, width: "80px"},
	            ];  
	};
	
	return obj;
});
