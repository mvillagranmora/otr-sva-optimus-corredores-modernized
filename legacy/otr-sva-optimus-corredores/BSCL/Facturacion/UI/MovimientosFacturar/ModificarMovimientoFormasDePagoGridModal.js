angular.module('ModuloFacturacion').factory('bcsModificarMovimientoFormasDePagoModalGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	
	
	//MOVIMIENTOS FORMA DE PAGO
	obj.modeloMovimientoFormasDePago = function() {
		return {
			model: {
				fields : {
					NombreMedioDePago           : { type: "string"},
					NombreModalidadDePago       : { type: "string"},
					NombreCuentaBancaria        : { type: "string"}, 
					NombreTipoCuentaBancaria	: { type: "string"},					
					NumeroCuentaBancaria		: { type: "string"},					
					NombreBanco					: { type: "string"},					
					NombreCuentaBancaria		: { type: "string"},					
					NombreTipoTarjetaBancaria	: { type: "string"},
					NumeroTarjetaBancaria		: { type: "string"},
					FechaVctoTarjetaBancaria    : { type: "date"},
					NumeroSerie                 : { type: "string"},
					NumeroDocumento				: { type: "number"},
					FechaDocumentoPago			: { type: "date"},
					IdMoneda					: { type: "string"},
					MontoLiquidacionFormaDePago : { type: "number"}, 	
					IdEstado                    : { type: "string"},
					NombreEstado                : { type: "string"},
					IdMovimientoComprobanteFormaDePago : { type: "number"},   //columna oculta
					IdMedioDePago               : { type: "string"},
					IdMovimientoComprobante     : { type: "number"},   //columna oculta
					IdModalidadDePago             : { type: "string"},   //columna oculta					
					IdBanco		                : { type: "number"},   //columna oculta
					IdTipoCuentaBancaria		: { type: "string"},   //columna oculta
					IdCuentaBancaria		    : { type: "number"},   //columna oculta
					IdTarjetaBancaria		    : { type: "number"},   //columna oculta
					IdTipoMovimiento 			: { type: "string"},
					IdTipoMovimientoSpot		: { type: "string"}
				}
			}
		};
	};
	
		
	obj.columnasMovimientoFormasDePago = function() {
		return  [
			 {field : "NombreMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "110px", align: "left", sortable : false }
			,{field : "NombreModalidadDePago", title:'UI_MODALIDAD', width : "60px", align: "left", sortable : false}									
			,{field : "IdMoneda", title: "UI_MONEDA", width: "60px", align: "left", sortable : false}
			,{field : "MontoLiquidacionFormaDePago", title:'UI_MONTO', width : "100px", align: "right", sortable : false, template : "<label> #: (util.bcs_formato(MontoLiquidacionFormaDePago,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "NombreCuentaBancaria", title:'UI_NOMBRE_CUENTA_BANCARIA', width : "150px", align: "left", sortable : false }
			,{field : "NombreTipoCuentaBancaria", title:'UI_TIPO_CUENTA_BANCARIA', width : "120px", align: "left", sortable : false }
			,{field : "NombreBanco", title:'UI_BANCO', width : "200px", align: "left", sortable : false }
			,{field : "NumeroCuentaBancaria", title:'UI_NUMERO_CUENTA', width : "100px", align: "left", sortable : false }
			,{field : "NombreTipoTarjetaBancaria", title:'UI_TIPO_TARJETA_BANCARIA', width : "100px", align: "left", sortable : false }
			,{field : "NumeroTarjetaBancaria", title:'UI_NUMERO_TARJETA', width : "100px", align: "left", sortable : false }
			,{field : "FechaVctoTarjetaBancaria", title:'UI_FECHA_VENCIMIENTO_TARJETA', width : "100px" , align: "center", sortable : false, template: kendo.template("#if (util.dateisdefault(FechaVctoTarjetaBancaria))  {##} else {##=util.formatoDate(FechaVctoTarjetaBancaria)##}#")}
			,{field : "NumeroSerie", title:'UI_NUMERO_SERIE', width : "100px", align: "left", sortable : false }
			,{field : "NumeroDocumento", title:'UI_NUMERO_DOCUMENTO', width : "100px", align: "right", sortable : false }
			,{field : "FechaDocumentoPago", title:'UI_FECHA_DOCUMENTO', width : "100px" , align: "center", sortable : false, template: kendo.template("#if (util.dateisdefault(FechaDocumentoPago))  {##} else {##=util.formatoDate(FechaDocumentoPago)##}#")}
			,{field : "IdEstado", title:'UI_ESTADO', width : "70px", align: "left", sortable : false, hidden:true }
			,{field : "NombreEstado", title:'UI_ESTADO', width : "70px", align: "left", sortable : false }
			 	
			//Columnas Ocultas  
			,{field : "IdMovimientoComprobanteFormaDePago", title:'UI_MVTO_FORMA_DE_PAGO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdMedioDePago", title:'UI_MEDIO_DE_PAGO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdModalidadDePago", title:'UI_MODALIDAD', width : "80px", align: "right", sortable : false, hidden: true}			
			,{field : "IdBanco", title:'UI_BANCO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdTipoCuentaBancaria", title:'UI_TIPO', width : "80px", align: "right", sortable : false, hidden: true}
			,{field : "IdCuentaBancaria", title:'UI_CUENTAS_BANCARIAS', width : "80px", align: "right", sortable : false, hidden: true}			
			,{field : "IdTarjetaBancaria", title:'UI_TARJETA_BANCARIA', width : "80px", align: "right", sortable : false, hidden: true}			
		];
	};
	
	
	
    return obj;
    
}]);