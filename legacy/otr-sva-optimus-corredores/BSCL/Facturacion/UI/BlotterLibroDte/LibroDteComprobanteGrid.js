		
angular.module('ModuloFacturacion').factory('bcsLibroDteComprobanteGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};	
	
 
	transporteLibroDteComprobante = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3215",
				dataType : "json"
			}
		};
	};	
	
	modeloLibroDteComprobante = function() {
		return {
			model: {
				fields : {
					IdEmpresa                       : { type: "number"},
					IdLibro                         : { type: "number"},
					IdComprobante                   : { type: "number"},
					IdPortafolio 					: { type: "number"},
					IdCliente 						: { type: "number"},
					NombreLargoCliente 				: { type: "string"},
					FechaComprobante                : { type: "sysdate"},
					IdTipoComprobante 				: { type: "string"},
					IdMoneda                        : { type: "string"}, 
					Monto                           : { type: "number"},
					IdMonedaComprobante             : { type: "string"},					
					MontoComprobante                : { type: "number"},
					IdMonedaLiquidacion             : { type: "string"},
					MontoLiquidacion                : { type: "number"},					
				}
			}
		};
	};
	
		
	
	obj.dataSourceLibroDteComprobante = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 25, // Número de registros por página.
			            transport 			: transporteLibroDteComprobante(),
			            schema 				: modeloLibroDteComprobante(),
		              }
	});
	
	
	obj.columnasLibroDteComprobante = function() {
		return  [
		     {field: "IdEmpresa",             		    title: "UI_EMPRESA",                 width: "50px",  align: "right" }    
	   	    ,{field: "IdLibro",             		    title: "UI_LIBRO",                   width: "50px",  align: "right" }
		    ,{field: "IdComprobante",          		    title: "UI_SECUENCIA_COMPROBANTE",   width: "75px",  align: "right" } 
	   	    ,{field: "IdPortafolio", 					title: "UI_PORTAFOLIO",				 width: "80px",  hidden: true   }
			,{field: "IdCliente",    					title: "UI_CLIENTE",    			 width: "80px",  hidden: true   }
			,{field: "NombreLargoCliente", 				title: "UI_CLIENTE", 				 width: "150px", align: "left", groupable: true }
			,{field: "FechaComprobante", 				title: "UI_FECHA_COMPROBANTE",	     width: "80px",  groupable: true }
			,{field: "IdTipoComprobante", 				title: "UI_TIPO_COMPROBANTE", 		 width: "55px",  align: "left", groupable: true , template: "<span class='flags' title='#=NombreTipoComprobante #'>#=IdTipoComprobante#</span>" }
			,{field: "IdMoneda",                        title: "UI_MONEDA",                  width: "75px",  align: "left"  }
			,{field: "Monto",                           title: "UI_MONTO",                   width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" }
			,{field: "IdMonedaComprobante",             title: "UI_MONEDA_COMPROBANTE",      width: "75px",  align: "left"  }
			,{field: "MontoComprobante",                title: "UI_MONTO_COMPROBANTE",       width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoComprobante,'GEN','Monto',IdMonedaComprobante,'','')).valor #</label>" }
			,{field: "IdMonedaLiquidacion",             title: "UI_MONEDA_LIQUIDACION",      width: "75px",  align: "left"  }
			,{field: "MontoLiquidacion",                title: "UI_MONTO_LIQUIDACION",       width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoLiquidacion,'GEN','Monto',IdMonedaLiquidacion,'','')).valor #</label>" }
		
		];
	};

	
		
	return obj;
}]);	


