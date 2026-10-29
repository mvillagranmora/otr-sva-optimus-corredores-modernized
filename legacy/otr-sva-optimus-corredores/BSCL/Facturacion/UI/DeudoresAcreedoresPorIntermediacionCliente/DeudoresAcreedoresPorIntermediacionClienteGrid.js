angular.module('ModuloFacturacion').factory('bcsDeudoresAcreedoresPorIntermediacionClienteGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	
	//Formateo exportacion a excel
    var excelFormat = function(field, field2) {
    	return function (item) {
			return util.bcs_formato(item[field],'GEN','Monto',item[field2],'','').excelFormat;
		}; 
    }
                                      
                                      
	//COMPROBANTES	
	modeloComprobantes = function() {
		return {
			model: {
				fields : {
					IdComprobante                               : { type : "number"},
					IdTipoComprobante                       : { type : "string"},
					NombreTipoComprobante                       : { type : "string"},
					FolioComprobante                            : { type : "number"},
					FechaLiquidacionString                      : { type : "string"},
					FechaLiquidacion                            : { type : "sysdate"},
					FechaEmisionString                          : { type : "sysdate"},
					FechaEmision                                : { type : "sysdate"},
					FechaComprobante                            : { type : "sysdate"},
					IdCliente                                   : { type : "number"},
					DocumentoIdentidad                          : { type : "string"},
					IdPortafolio                                : { type : "number"},
					CodigoPortafolio                            : { type : "string"},
					Proposito                                   : { type : "string"},
					NombreLargoCliente                          : { type : "string"},
					IdEmpleadoEjecutivoPortafolio               : { type : "number"},
					NombreEmpleadoEjecutivoPortafolio           : { type : "string"},
					IdSucursalEmpleadoEjecutivoPortafolio       : { type : "number"},
					NombreSucursalEmpleadoEjecutivoPortafolio   : { type : "string"},
					IdMoneda                                    : { type : "string"},
					CodigoCondicionLiquidacion					: { type : "string"},
					SaldoAFavor                                 : { type : "number", excelFormat: excelFormat('SaldoAFavor', 'IdMoneda')},
					SaldoEnContra                               : { type : "number", excelFormat: excelFormat('SaldoEnContra', 'IdMoneda')},
					TotalGastoCompra                            : { type : "number", excelFormat: excelFormat('TotalGastoCompra', 'IdMoneda')},
					TotalGastoVenta                             : { type : "number", excelFormat: excelFormat('TotalGastoVenta', 'IdMoneda')},
					IdTipoOperacion                             : { type : "string"},
					AccionTipoOperacion                         : { type : "string"},
					CodigoTipoOperacion                         : { type : "string"},
					NombreTipoOperacion                         : { type : "string"},
					IdTipoProducto                              : { type : "string"},
					CodigoTipoProducto                          : { type : "string"},
					NombreTipoProducto                          : { type : "string"},
				}
			}
		};
	};
	obj.fnColorBk = function(row) {
        var color = "";
		
		if ( Number(row.MontoFinal) < 0 )
		   color = "green";
	    else
	       color = "red";

        return color;
  	};
  	
	obj.dataSourceComprobantes = new bcs.DataSource({		
		    type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
							transport 			: {read : {
															url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=584",
															dataType : "json"
														  }
												  },
							schema 				: modeloComprobantes(),
						}
	  });
	
	obj.columnasComprobantes = function() {
		return  [
			 {field: "IdComprobante", 									title: "UI_SECUENCIA_COMPROBANTE", 		width: "60px",  align: "right", sortable : false}
    		,{field: "FolioComprobante", 								title: "UI_FOLIO", 						width: "60px",  align: "left", sortable : false }
    		,{field: "FechaComprobante", 								title: "UI_FECHA_COMPROBANTE", 			width: "70px",  groupable: true }
    		,{field: "FechaLiquidacion", 								title: "UI_FECHA_LIQUIDACION", 			width: "70px",  groupable: true }
    		,{field: "IdTipoComprobante", 								title: "UI_TIPO", 						width: "50px",  align: "left", sortable : false, groupable: true, template: '<span class="flags" title="#=NombreTipoComprobante#">#=IdTipoComprobante#</span>' }
    		,{field: "DocumentoIdentidad", 								title: "UI_DOCUMENTO_IDENTIDAD_ABR",	width: "70px",  align: "left", sortable : false, groupable: true, template: '<span class="flags" title="#=NombreLargoCliente#">#=DocumentoIdentidad#</span>' }
    		,{field: "NombreLargoCliente", 								title: "UI_CLIENTE", 					width: "200px", align: "left", sortable : false }
    		,{field: "CodigoPortafolio", 								title: "UI_PORTAFOLIO_ABR", 			width: "50px",  align: "left", sortable : false, groupable: true }
    		,{field: "Proposito", 										title: "UI_PROPOSITO", 					width: "150px", align: "left", sortable : false }
    		,{field: "NombreEmpleadoEjecutivoPortafolio", 				title: "UI_EJECUTIVO", 					width: "200px", align: "left", sortable : false, groupable: true }
    		,{field: "NombreSucursalEmpleadoEjecutivoPortafolio",		title: "UI_SUCURSAL", 					width: "120px", align: "left", sortable : false, groupable: true }
    		,{field: "IdMoneda", 										title: "UI_MONEDA", 					width: "80px",  align: "left", sortable : false}
    		,{field: "CodigoCondicionLiquidacion", 						title: "UI_CONDICION_LIQUIDACION", 		width: "80px",  align: "left", sortable : false}
			//template: '<span class="flags" title="#=NombreMoneda#">#=IdMoneda#</span>' }    		
    		,{field: "SaldoAFavor", 									title: "UI_SALDO_A_FAVOR", width: "100px", align: "right", sortable : false, 
    		 //template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
    		         format : function (value, item) {
    		  	                                        return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');
    		                                         }
    		}
    		,{field: "SaldoEnContra", title: "UI_SALDO_EN_CONTRA", width: "100px", align: "right", sortable : false,
    		 //template : " <label> #:  (util.bcs_formato(MontoCobro,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
    			     format : function (value, item) {
    		  	                                        return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');
    		                                         }
    		}
    		,{field: "TotalGastoCompra", title: "UI_TOTAL_GASTO_COMPRA", width: "100px", align: "right", sortable : false, 
    		 //template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
    		         format : function (value, item) {
    		  	                                        return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');
    		                                         }
    		}
    		,{field: "TotalGastoVenta", title: "UI_TOTAL_GASTO_VENTA", width: "100px", align: "right", sortable : false, 
    		 //template : " <label> #:  (util.bcs_formato(MontoCobro,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
    			     format : function (value, item) {
    		  	                                        return util.bcs_formato(value,'GEN','Monto',item.IdMoneda,'','');
    		                                         }
    		}
    		,{field: "AccionTipoOperacion", title: "UI_OPERACION", width: "90px", align: "left", sortable : false }
    		,{field: "NombreTipoOperacion", title: "UI_TIPO_OPERACION", width: "180px", align: "left", sortable : false }
    		,{field: "NombreTipoProducto", title: "UI_TIPO_PRODUCTO", width: "180px", align: "left", sortable : false }
		];
	};
	
	return obj;
}]);	

