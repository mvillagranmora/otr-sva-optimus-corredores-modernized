angular.module('ModuloFacturacion').factory('bcsFacturacionCobroMensualGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	//Vista previa Facturacion de Cobros Mensual	
	obj.modeloFacturacionCobroMensual = function() {
		return {
			model: {
				fields : {
					IdCliente                		: { type: "number"},
					NombreCliente            		: { type: "string"},
					DocumentoIdentidad       		: { type: "string"},
					IdPortafolio             		: { type: "number"},
					CodigoPortafolio         		: { type: "string"},
					NombreConceptoFacturacion 		: { type: "string"},
					IdMoneda       	 		 		: { type: "string"},
					ModPrefFact       	 			: { type: "string"},
					ModoFacturacion       	 		: { type: "string"},
					IdClasificacionTipoInstrumento	: { type: "string"},
					IdContratoComision              : { type: "number"},
					NombreCliente            		: { type: "string"},					
					NombreConvenioContrato			: { type: "string"},
					TotalMontoLiquidacion			: { type: "number"},
					Monto      		 				: { type: "number"},
					MontoDerecho      		 		: { type: "number"},
					MontoComision	         		: { type: "number"},
					IdInstrumento 					: { type: "number"},
				}
			}
		};
	};

	obj.columnasFacturacionCobroMensual = function() {	
		return  [
			 {field: "NombreCliente", 					title: "UI_CLIENTE",width: "150px", align: "left", groupable: true },
			 {field: "DocumentoIdentidad", 				title: "UI_DOCUMENTO_IDENTIDAD",width: "60px",  align: "left", groupable: true },			 
			 {field: "CodigoPortafolio", 				title: "UI_PORTAFOLIO",width: "30px", align: "right"},    
			 {field: "IdMoneda", 						title: "UI_MONEDA",width: "30px", align: "right"},
			 {field: "IdClasificacionTipoInstrumento", 	title: "UI_MERCADO",width: "30px", align: "right"},
	   	     {field: "ModoFacturacion", 				title: "UI_MODO_FACTURACION",width: "70px", align: "left", groupable: true },
	   	     {field: "NombreConvenioContrato", 			title: "UI_NOMBRE_CONVENIO_CONTRATO",width: "90px", align: "left", groupable: true },
	   	     {field: "TotalMontoLiquidacion",			title: "UI_TOTAL_MONTO_LIQUIDACION",format: "{0:n0}",width: "60px",  align: "left", groupable: true },
	   	     {field: "MontoDerecho",					title: "UI_DERECHO",format: "{0:n0}",width: "60px",  align: "left", groupable: true },	   	     
	   	     {field: "MontoComision",					title: "UI_COMISION",format: "{0:n0}",width: "60px",  align: "left", groupable: true },	   	     
	   	     {field: "Monto",							title: "UI_MONTO_POR_FACTURAR",format: "{0:n0}",width: "60px",  align: "left", groupable: true },	   	     
	   	     {field: "NombreConceptoFacturacion", 		title: "UI_CONCEPTO",width: "120px", align: "left", groupable: true },
		];  
	};
	
    return obj;
    
}]);	