		
angular.module('ModuloFacturacion').factory('bcsInterfazAfpGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	//OPERADOR AFP 
	transporteInterfazAfp = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=5001",
				dataType : "json"
			}
		};
	};	
	
	modeloInterfazAfp = function() {
		return {
			model: {
				fields : {					
					FechaTransaccion                          : {type: "sysdate"},
					FolioTransaccion                          : {type: "number"},
					Corredora                                 : {type: "number"},
					ValorIndicador                            : {type: "number"},
					AccionTipoOperacion                       : {type: "string"},
					Cantidad                                  : {type: "number"},
					Precio                                    : {type: "number"},
					HoraTransaccion                           : {type: "string"},
					IdMoneda								  : {type: "string"},
					IdClasificacionTipoInstrumento            : {type: "string"},
					NombreClasificacionTipoInstrumento        : {type: "string"},
					IdTipoInstrumento                         : {type: "string"},
					NombreTipoInstrumento                     : {type: "string"},
					NemotecnicoInstrumento                    : {type: "string"},
					Total                                     : {type: "number"},
					Boveda                                    : {type: "number"},
					OD                                        : {type: "string"},
					Bolsa                                     : {type: "string"},
					CondicionDeLiquidacion                    : {type: "string"},
					TipoOperador                              : {type: "string"},
					IdCliente								  : {type: "number"},
					DocumentoIdentidad                        : {type: "string"},
					IdPortafolio							  : {type: "number"},
					Portafolio                                : {type: "string"},
					NombreCliente                             : {type: "string"},
					IdTipoComprobante                         : {type: "string"},
					NombreTipoComprobante                     : {type: "string"},
					IdCondicionLiquidacion                    : {type: "string"},
					CodigoCondicionLiquidacion                : {type: "string"},
					NombreCondicionLiquidacion                : {type: "string"},
					FechaLiquidacion                          : {type: "sysdate"}
				}
			}
		};
	};

	obj.dataSourceInterfazAfp = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdCliente", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 30, // Número de registros por página.
			            transport 			: transporteInterfazAfp(),
			            schema 				: modeloInterfazAfp()
		              }
	});
	
	obj.columnasInterfazAfp = function() {
		return  [
			  {field: "FechaTransaccion",   					title: "UI_FECHA_TRANSACCION", 				width: "90px" }
			 ,{field: "FolioTransaccion",   					title: "UI_FOLIO_TRANSACCION", 				width: "90px" }
			 ,{field: "Corredora", 								title: "UI_CORREDORA", 						width: "50px" }
			 ,{field: "ValorIndicador", 						title: "UI_CODIGO", 						width: "50px" }
			 ,{field: "AccionTipoOperacion", 					title: "UI_ACCION_OPERACION", 				width: "50px" }
			 ,{field: "Cantidad", 								title: "UI_CANTIDAD", 						width: "90px", 
			 	format : function (value, item) {return util.bcs_formato(value,'GEN','Cantidad',item.IdMoneda,'','');    		                                    }
			 }
			 ,{field: "Precio", 								title: "UI_PRECIO", 						width: "80px" , 
			 	format : function (value, item) {return util.bcs_formato(value,'GEN','Precio',item.IdMoneda,item.IdTipoInstrumento,'');    		                                    }
			 }
			 ,{field: "HoraTransaccion", 						title: "UI_HORA", 							width: "80px" }
			 ,{field: "IdClasificacionTipoInstrumento", 		title: "UI_CLASIFICACION_INSTRUMENTO", 		width: "50px" }
			 ,{field: "NombreClasificacionTipoInstrumento", 	title: "UI_CLASIFICACION_INSTRUMENTO", 		width: "150px"}
			 ,{field: "IdTipoInstrumento", 						title: "UI_TIPO_INSTRUMENTO",				width: "80px" }
			 ,{field: "NombreTipoInstrumento", 					title: "UI_TIPO_INSTRUMENTO", 				width: "150px"}
			 ,{field: "NemotecnicoInstrumento", 				title: "UI_NEMOTECNICO", 					width: "140px"}
			 ,{field: "Total", 									title: "UI_TOTAL", 							width: "90px" , 
			 	format : function (value, item) {return util.bcs_formato(value,'GEN','Cantidad',item.IdMoneda,'','');    		                                    }
			 }
			 ,{field: "Boveda", 								title: "UI_BOVEDA", 						width: "50px" }
			 ,{field: "OD", 									title: "UI_ORDEN_DIRECTA", 					width: "50px" }
			 ,{field: "Bolsa", 									title: "UI_BOLSA", 							width: "50px" }
			 ,{field: "CondicionDeLiquidacion", 				title: "UI_CONDICION_LIQUIDACION", 			width: "50px" }
			 ,{field: "TipoOperador", 							title: "UI_TIPO_OPERADOR", 					width: "50px" }
			 ,{field: "IdCliente", 								title: "UI_ID_CLIENTE", 					width: "50px" }
			 ,{field: "DocumentoIdentidad", 					title: "UI_DOCUMENTO_IDENTIDAD", 			width: "80px" }
			 ,{field: "Portafolio", 							title: "UI_PORTAFOLIO", 					width: "50px" }
			 ,{field: "NombreCliente", 							title: "UI_NOMBRE", 						width: "200px"}
			 ,{field: "IdCondicionLiquidacion", 				title: "UI_CONDICION_LIQUIDACION", 			width: "50px" }
			 ,{field: "CodigoCondicionLiquidacion", 			title: "UI_CONDICION_LIQUIDACION", 			width: "50px" }
			 ,{field: "NombreCondicionLiquidacion", 			title: "UI_CONDICION_LIQUIDACION", 			width: "150px"}
			 ,{field: "FechaLiquidacion", 						title: "UI_CODIGO_OPERADOR_AFP", 			width: "90px" }
		];
	};
	
	return obj;
}]);	


