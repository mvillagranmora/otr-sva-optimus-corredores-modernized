angular.module('ModuloFacturacion').factory('bcsModificarMovimientoCobroGridModal',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	//MOVIMIENTOS COBROS
	obj.modeloMovimientoCobros = function() {
		return {
			model: {
				fields : {
					IdCobro                  : { type : "string"},
			        NombreCobro			     : { type : "string"},
			        DescripcionCobroClasificacion : { type : "string"},
			        NombreFormaCobro  	     : { type : "string"},			        
			        NombreMonedaOperacionCobro : { type : "string"},
					IdMonedaCobro            : { type : "string"},
					MontoCobro			     : { type : "number"},
					NombreMonedaSistemaCobro : { type : "string"},
					IdMonedaMovimientoComprobanteCobro : { type : "string"},
					MontoMovimientoComprobanteCobro : { type : "number"},
					NombreMonedaLiquidacionCobro : { type : "string"},					
					IdMonedaLiquidacionCobro : { type : "string"},
					MontoLiquidacionCobro    : { type : "number"},					
					Porcentaje		         : { type : "number"},
					NombreEstado	         : { type : "string"},
					EsRecalculable           : { type : "string"},
					IdMovimientoComprobante  : { type : "number"},
					IdMovimientoComprobanteCobro : { type : "number"},
					HayDatosModificados : { type : "string"},
				}
			}
		};
	};	
	
			 
	obj.columnasMovimientoCobros = function() {
		return  [
			 { field: "NombreCobro",      title: "UI_TIPO_COBRO",  width:"170px", align: "left",  sortable : true, filterable: false },
			 { field: "DescripcionCobroClasificacion", title: "UI_CLASIFICACION_COBRO",  width:"100px", align: "left",  sortable : false, filterable: false },
			 { field: "NombreFormaCobro", title: "UI_FORMA_COBRO", width:"80px", align: "left",  sortable : false, filterable: false },
		     { field: "NombreMonedaOperacionCobro", title: "UI_MONEDA_COBRO", width:"120px", align: "left",  sortable : false, filterable: false, hidden: true },
		     { field: "IdMonedaCobro", title: "UI_MONEDA_COBRO", width:"80px", align: "left",  sortable : false, filterable: false, hidden: false },
		     { field: "MontoCobro",  title: "UI_MONTO_COBRO", width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoCobro,'GEN','Monto',IdMonedaCobro,'','')).valor #</label>", editable: true},
		     { field: "NombreMonedaSistemaCobro", title: "UI_MONEDA_LOCAL",   width:"120px", align: "left",  sortable : false, filterable: false, hidden: true },
		     { field: "IdMonedaMovimientoComprobanteCobro", title: "UI_MONEDA_COBRO_SISTEMA", width:"80px", align: "left",  sortable : false, filterable: false, hidden: false },
		     { field: "MontoMovimientoComprobanteCobro", title: "UI_MONTO_LOCAL", width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoMovimientoComprobanteCobro,'GEN','Monto',IdMonedaMovimientoComprobanteCobro,'','')).valor #</label>"},
		     { field: "NombreMonedaLiquidacionCobro", title: "UI_MONEDA_LIQUIDACION",   width:"120px", align: "left",  sortable : false, filterable: false, hidden: true },
		     { field: "IdMonedaLiquidacionCobro", title: "UI_MONEDA_COBRO_LIQUIDACION", width:"80px", align: "left",  sortable : false, filterable: false, hidden: false },		     
		     { field: "MontoLiquidacionCobro", title: "UI_MONTO_LIQUIDACION", width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(MontoLiquidacionCobro,'GEN','Monto',IdMonedaLiquidacionCobro,'','')).valor #</label>"},
		     { field: "Porcentaje",       title: "UI_PORCENTAJE",  width:"100px",  align: "right", sortable : false, filterable: false, template : "<label> #: (util.bcs_formato(Porcentaje,'GEN','PARIDAD',IdMonedaCobro,'','')).valor #</label>", editable: true },	
		     { field: "NombreEstado",     title: "UI_ESTADO",   width:"80px", align: "left",  sortable : false, filterable: false },
		     { field: "EsRecalculable",   title: "UI_RECALCULABLE", width:"50px", align: "left",  sortable : false, filterable: false },
		     
		     { field: "IdCobro", title: "UI_COBRO",  width:"85px", hidden: true },		     
		     { field: "IdMovimientoComprobante", title: "UI_SECUENCIA_MOVIMIENTO", width:"85px", hidden: true},
		     { field: "IdMovimientoComprobanteCobro", title: "UI_SECUENCIA_MOVIMIENTO_COBRO", width:"85px", hidden: true},
		];		
	};
	
	
    return obj;
    
}]);	