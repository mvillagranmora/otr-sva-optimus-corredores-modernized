angular.module('ModuloFacturacion').factory('bcsCobroPeriodicoDetalleGridModal',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	//COBRO DETALLE
	obj.columnasCobroPeriodicoDet = function() {
		return  [	
    	   	 {field: "MontoDesde", title: "UI_MONTO_DESDE", width: "80px", align: "right"}
    	   	,{field: "MontoHasta", title: "UI_MONTO_HASTA", width: "80px", align: "right"}
			,{field: "MontoCobroPeriodico", title: "UI_MONTO_COBRO", width: "80px", align: "right"}
			,{field: "PorcentajeCobroPeriodico", title: "UI_PORCENTAJE_COBRO", width: "80px", align: "right"}
		];
	};	
	
	
	obj.modeloCobroPeriodicoDet = function() {
		return {
			model: {
				fields : {	
	        		       MontoDesde            : { type: "number"},
	        		       MontoHasta            : { type: "number"},
					       MontoCobroPeriodico   : { type: "number"},
					       PorcentajeCobroPeriodico : { type: "number"}
				         }
			}
		};
	};
	
	return obj;
}]);