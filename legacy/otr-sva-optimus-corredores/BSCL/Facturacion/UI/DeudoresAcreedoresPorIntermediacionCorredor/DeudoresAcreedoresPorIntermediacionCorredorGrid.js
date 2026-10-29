angular.module('ModuloFacturacion').factory('bcsDeudoresAcreedoresPorIntermediacionCorredorGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};
	
	
	//Formateo exportacion a excel
    var excelFormat = function(field, field2) {
    	return function (item) {
			return util.bcs_formato(item[field],'GEN','Monto',item[field2],'','').excelFormat;
		}; 
    }
                                      
                                      
	//TRANSACCIONES	
	modeloTransacciones = function() {
		return {
			model: {
				fields : {
					IdEmpresa                               : { type : "number" },
					IdTransaccion                           : { type : "number" },
					FolioTransaccion                        : { type : "number" },
					CodigoOperadorContraparte               : { type : "string" },
					CodigoOperadorCorredor                  : { type : "string" },
					IdCorredorContraparte                   : { type : "number" },
					NombreCorredorContraparte               : { type : "string" },
					EsOD                                    : { type : "string" },
					EsODUI                                  : { type : "string" },
					IdClasificacionTipoInstrumento          : { type : "string" },
//					CodigoClasificacionTipoInstrumento      : { type : "string" },
					NombreClasificacionTipoInstrumento      : { type : "string" },
					IdTipoInstrumento                       : { type : "string" },
					NombreTipoInstrumento                   : { type : "string" },
					IdEmisor                                : { type : "string" },
					IdInstrumentoUI                         : { type : "number" },
					InstrumentoUI                           : { type : "string" },
					FechaOperacion                          : { type : "sysdate"},
					FechaOperacionTransaccion               : { type : "sysdate"},
					FechaLiquidacion                        : { type : "sysdate"},
					IdCondicionDeLiquidacion                : { type : "string" },
					CodigoCondicionDeLiquidacion            : { type : "string" },
					NombreCondicionDeLiquidacion            : { type : "string" },
					IdMonedaTransaccion                     : { type : "string" },
					IdMonedaInstrumento                     : { type : "string" },
					CantidadTransaccion                     : { type : "number", excelFormat: excelFormat('CantidadTransaccion', 'IdMonedaTransaccion')},
					PrecioTransaccion                       : { type : "number", excelFormat: excelFormat('PrecioTransaccion', 'IdMonedaTransaccion') },
					Tasa                                    : { type : "number", excelFormat: excelFormat('Tasa', 'IdMonedaTransaccion') },
					PrecioTasa                              : { type : "number", excelFormat: excelFormat('PrecioTasa', 'IdMonedaTransaccion') },
					MontoTransaccion                        : { type : "number", excelFormat: excelFormat('MontoTransaccion', 'IdMonedaTransaccion') },
					ValorRescateTransaccion                 : { type : "number", excelFormat: excelFormat('ValorRescateTransaccion', 'IdMonedaTransaccion') },
					IdTipoOperacion                         : { type : "string" },
					AccionOrden                             : { type : "string" },
					IndicadorDCV                            : { type : "string" }
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
  	
	obj.dataSourceTransacciones = new bcs.DataSource({		
		    type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdTransaccion", dir: "desc" }, // Criterios de Ordenamiento.
							transport 			: {read : {
															url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=585",
															dataType : "json"
														  }
												  },
							schema 				: modeloTransacciones(),
							aggregate			: [ //Para resumen de totales de transaccion
													{ field: "MontoTransaccion", aggregate: "sum" },				
							                       ]
							}
	  });
	
	obj.columnasTransacciones = function() {
		return  [
			 {field: "FolioTransaccion", 								title: "UI_FOLIO",					width: "60px",  align: "right", sortable : false}
			,{field: "IdCorredorContraparte", 							title: "UI_CODIGO_CORREDOR", 		width: "60px",  align: "right", sortable : false}
			,{field: "NombreCorredorContraparte", 						title: "UI_CORREDOR_CONTRAPARTE",	width: "210px",  align: "left", sortable : false}
			,{field: "EsOD", 											title: "UI_ORDEN_DIRECTA",			width: "60px",  align: "left", sortable : false}
			,{field: "NombreClasificacionTipoInstrumento",				title: "UI_CLASIFICACION",			width: "180px",  align: "left", sortable : false}
			,{field: "NombreTipoInstrumento",							title: "UI_TIPO_INSTRUMENTO",		width: "170px",  align: "left", sortable : false}
			,{field: "IdEmisor",										title: "UI_EMISOR",					width: "70px",  align: "left", sortable : false}
			,{field: "InstrumentoUI",									title: "UI_INSTRUMENTO",			width: "70px",  align: "left", sortable : false}
    		,{field: "FechaOperacion", 									title: "UI_FECHA_OPERACION", 		width: "70px",  groupable: true }
    		,{field: "FechaLiquidacion", 								title: "UI_FECHA_LIQUIDACION", 		width: "70px",  groupable: true }
    		,{field: "CodigoCondicionDeLiquidacion",					title: "UI_CONDICION_LIQUIDACION",	width: "60px",  align: "left", sortable : false}
    		,{field: "CantidadTransaccion", 							title: "UI_CANTIDAD_RESCATE", 		width: "90px", align: "right", sortable : false, 
    		         format : function (value, item) {return util.bcs_formato(value,'GEN','Monto',item.IdMonedaTransaccion,'','');}}
    		,{field: "PrecioTasa", 										title: "UI_PRECIO_TASA", 				width: "100px", align: "right", sortable : false, 
    		         format : function (value, item) {return util.bcs_formato(value,'GEN','Precio',item.IdMonedaTransaccion,item.IdTipoInstrumento,'');}}
    		,{field: "MontoTransaccion", title: "UI_MONTO",width: "90px", align: "right",  sortable : false,
    				 template : " <label> #:  (util.bcs_formato(MontoTransaccion,'GEN','Monto','CLP','','')).valor #</label>", 
    				 footerTemplate:" #: (util.bcs_formato(sum,'GEN','Monto','CLP', '','')).valor #", 
    				 footerAttributes:{style:"text-align: right; padding-right:1px;"}}
			,{field: "AccionOrden",										title: "UI_ACCION",			width: "50px",  align: "left", sortable : false}
		
		];
	};
	
	return obj;
}]);	


