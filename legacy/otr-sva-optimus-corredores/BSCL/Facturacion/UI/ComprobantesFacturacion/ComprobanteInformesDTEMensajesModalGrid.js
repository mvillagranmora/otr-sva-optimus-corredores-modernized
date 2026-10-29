angular.module('ModuloFacturacion').factory('bcsComprobantesDteMensajesGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	//Grilla Mensajes
	obj.columnasCmbteDteMens = function() {
		return  [	
			{field: "IdComprobante", title: "UI_SECUENCIA_COMPROBANTE", width: "40px", align: "right", sortable : false},
			{field: "Mensaje", title: "UI_MENSAJE", width: "180px", align: "right"}
		];
	};	
	
	
	obj.modeloCmbteDteMens = function() {
		return {
			model: {
				fields : {	
					       IdComprobante    : { type: "number"},
					       Mensaje          : { type: "string"}
				         }
			}
		};
	};	
	
	
	return obj;
}]);	


