angular.module('ModuloFacturacion').factory('bcsMovimientoFacturarCostosModalGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
		
	obj.modeloMovimientoFacturarCostosModal = function() {
		return {
			model: {
				fields : {
					NombreCobro                     : { type: "string"},
					MontoMovimientoComprobanteCobro : { type: "number"},
					MontoAActualizar   			    : { type: "number"},
					MontoAActualizarVis			    : { type: "number"},
					PorcentajeAActualizar           : { type: "number"},
					PorcentajeAActualizarVis        : { type: "number"},
				}
			}
		};
	};
		
	
	obj.columnasMovimientoFacturarCostosModal = function() {
		return  [
	   	     {field: "NombreCobro",              title: "UI_COBRO",            width: "150px" }
	   	    ,{field: "MontoMovimientoComprobanteCobro",  title: "UI_MONTO_ACTUAL", width: "75px", template : " <label> #:  (util.bcs_formato(MontoMovimientoComprobanteCobro,'GEN','Monto',IdMonedaCobro,'','')).valor #</label>"}	   	    			
			,{field: "MontoAActualizar",         title: "UI_MONTO",            width: "100px" }
			,{field: "MontoAActualizarVis",      title: "UI_MONTO",            width: "100px", editable: true, hidden: true }			
			,{field: "PorcentajeAActualizar",    title: "UI_PORCENTAJE",       width: "100px" }
			,{field: "PorcentajeAActualizarVis", title: "UI_PORCENTAJE",       width: "100px", editable: true, hidden: true }
		];
	};
	
    return obj;
    
}]);	