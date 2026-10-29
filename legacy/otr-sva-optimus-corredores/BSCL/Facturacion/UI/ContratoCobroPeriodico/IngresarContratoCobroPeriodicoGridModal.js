angular.module('ModuloFacturacion').factory('bcsIngContCobPeriodicoGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	//TIPO INSTRUMENTO	
	obj.modeloIngContCobTipIns = function() {
		return {
			model: {
				fields : {					
	        		       IdClasificacionTipoInstrumento 	: { type: "string"},
	        		       IdTipoInstrumento                : { type: "string"},
					       Nombre  			                : { type: "string"},					       
				         }
			}
		};
	};	
	
	obj.columnasIngContCobTipIns = function() {
		return  [	   	    
    	   	 {field: "IdClasificacionTipoInstrumento", title: "UI_CLASIFICACION", width: "62px", align: "left"}
    	   	,{field: "IdTipoInstrumento", title: "UI_TIPO_INSTRUMENTO", width: "62px", align: "left"}
			,{field: "Nombre", title: "UI_NOMBRE_TIPO_INSTRUMENTO", width: "120px", align: "left"}
		];
	};
	
	
	//TIPO INSTRUMENTO SELECCIONADO
	obj.columnasIngContCobTipInsSel = function() {
		return  [
			      {field: "IdClasificacionTipoInstrumento", title: "UI_CLASIFICACION", width: "62px", align: "left", hidden:true}
    	   	     ,{field: "IdTipoInstrumento", title: "UI_TIPO_INSTRUMENTO", width: "62px", align: "left", hidden:true}
			     ,{field: "NombreTipoInstrumento", title: "UI_NOMBRE_TIPO_INSTRUMENTO", width: "120px", align: "left"}
			     ,{field: "IdConceptoCobroPeriodico", title: "UI_CONCEPTO", width: "120px", align: "left", hidden:true}
			     ,{field: "NombreConceptoCobroPeriodico", title: "UI_CONCEPTO", width: "120px", align: "left"}
		        ];
	};
	
	
	obj.modeloIngContCobTipInsSel = function() {
		return {
			model: {
				fields : {	
					       IdClasificacionTipoInstrumento 	: { type: "string"},
					       IdTipoInstrumento                : { type: "string"},
					       NombreTipoInstrumento  			: { type: "string"},
					       IdConceptoCobroPeriodico         : { type: "string"},
					       NombreConceptoCobroPeriodico     : { type: "string"},
				         }
			}
		};
	};	
		
		
	
	//COBRO	
	obj.columnasCobroPeriodico = function() {
		return  [	   	    
		     {field: "IdCobro", title: "UI_COBRO", width: "150px", align: "left", hidden:true}    
    	   	,{field: "NombreCobro", title: "UI_COBRO", width: "150px", align: "left"}
    	   	,{field: "IdTipoCobroPeriodico", title: "UI_TIPO_COBRO", width: "80px", align: "left", hidden:true}
    	   	,{field: "TipoCobroPeriodico", title: "UI_TIPO_COBRO", width: "50px", align: "left"}
			,{field: "MontoMinimoCobrar", title: "UI_MONTO_MINIMO", width: "80px", align: "right"}
			,{field: "MontoMaximoCobrar", title: "UI_MONTO_MAXIMO", width: "80px", align: "right"}
			,{field: "PorRango", title: "UI_ES_RANGO", width: "50px", align: "left"}
		];
	};	
	
	
	obj.modeloCobroPeriodico = function() {
		return {
			model: {
				fields : {	
					       IdCobro                  : { type: "string"},
	        		       NombreCobro 	            : { type: "string"},
	        		       IdTipoCobroPeriodico     : { type: "string"},
	        		       TipoCobroPeriodico       : { type: "string"},
					       MontoMinimoCobrar        : { type: "number"},
					       MontoMaximoCobrar        : { type: "number"},
					       PorRango                 : { type: "string"}
				         }
			}
		};
	};
	
	//COBRO DETALLE
	obj.columnasCobroPeriodicoDet = function() {
		return  [	
		     {field: "IdCobro", title: "UI_COBRO", width: "150px", align: "left", hidden:true}      
    	   	,{field: "NombreCobro", title: "UI_COBRO", width: "150px", align: "left"}
    	   	,{field: "MontoDesde", title: "UI_MONTO_DESDE", width: "80px", align: "right", editable: true}
    	   	,{field: "MontoHasta", title: "UI_MONTO_HASTA", width: "80px", align: "right", editable: true}
			,{field: "MontoCobroPeriodico", title: "UI_MONTO_COBRO", width: "80px", align: "right", editable: true}
			,{field: "PorcentajeCobroPeriodico", title: "UI_PORCENTAJE_COBRO", width: "80px", align: "right", editable: true}
		];
	};	
	
	
	obj.modeloCobroPeriodicoDet = function() {
		return {
			model: {
				fields : {	
					       IdCobro                  : { type: "string"},
	        		       NombreCobro 	            : { type: "string"},
	        		       MontoDesde               : { type: "number"},
	        		       MontoHasta               : { type: "number"},
					       MontoCobroPeriodico      : { type: "number"},
					       PorcentajeCobroPeriodico : { type: "number"}
				         }
			}
		};
	};
	
		
	
    return obj;
    
}]);	