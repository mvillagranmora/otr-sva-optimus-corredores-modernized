angular.module('ModuloFacturacion').factory('bcsIngresarMovimientoFacturarCobroGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
		
	obj.columnasMovimientoCobro = function() {
		return  [
			 { field: "NombreCobro",      title: "UI_TIPO_COBRO",  width:"170px", align: "left",  sortable : false, filterable: false },
			 { field: "DescripcionCobroClasificacion", title: "UI_CLASIFICACION_COBRO",  width:"100px", align: "left",  sortable : false, filterable: false },
			 { field: "NombreFormaCobro", title: "UI_FORMA_COBRO", width:"80px", align: "left",  sortable : false, filterable: false },
		     { field: "NombreMoneda", title: "UI_MONEDA_COBRO", width:"120px", align: "left",  sortable : false, filterable: false },
		     { field: "Monto",            title: "UI_MONTO_COBRO", width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>"},
		     { field: "MontoMovimientoComprobanteCobro", title: "UI_MONTO_LOCAL", width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoMovimientoComprobanteCobro,'GEN','Monto','CLP','','')).valor #</label>"},
		     { field: "NombreMonedaLiquidacion", title: "UI_MONEDA_LIQUIDACION",   width:"120px", align: "left",  sortable : false, filterable: false },
		     { field: "MontoLiquidacion", title: "UI_MONTO_LIQUIDACION", width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoLiquidacion,'GEN','Monto','CLP','','')).valor #</label>"},
		     { field: "Porcentaje",       title: "UI_PORCENTAJE",  width:"100px",  align: "right", sortable : false, filterable: false },	
		     { field: "EsRecalculable",   title: "UI_RECALCULABLE", width:"50px", align: "left",  sortable : false, filterable: false },		     
		];
	};
	
	obj.modeloMovimientoCobros = function() {
		return {
			model: {
				fields : {
			        NombreCobro			: { type : "string"},
			        DescripcionCobroClasificacion : { type : "string"},
			        NombreFormaCobro	: { type : "string"},			        
					NombreMoneda		: { type : "string"},
					Monto				: { type : "number"},
					MontoMovimientoComprobanteCobro : { type : "number"},
					NombreMonedaLiquidacion : { type : "string"},
					MontoLiquidacion    : { type : "number"},					
					Porcentaje		    : { type : "string"},
					EsRecalculable      : { type : "string"},
				}
			}
		};
	};
	
	
    return obj;
    
}]);

