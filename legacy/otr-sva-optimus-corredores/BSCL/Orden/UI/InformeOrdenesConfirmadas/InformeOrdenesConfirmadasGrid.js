angular.module('ModuloMesa').factory('bcsInformeOrdenesConfirmadasGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	
	modeloInformeOrdenesConfirmadas = function() {
		return {
			model: {
				fields : {
					Mercado 			    	: {type:"string"},
					DocumentoIdentidad 			: {type:"string"},
					Cliente 					: {type:"string"},
					IdOrden						: {type:"string"},
					Producto					: {type:"string"},
					FechaIngreso				: {type:"string"},
					FechaConfirmacionPlazo		: {type:"string"},
					FechaConfirmacion			: {type:"sysdatetime"},
					Operacion					: {type:"string"},
					Instrumento					: {type:"string"},
					Cantidad					: {type:"number"},
					Precio						: {type:"number"},
					EstadoOrden					: {type:"string"},
					EstadoConfirmacion			: {type:"string"},
					IdMonedaOrden				: {type:"string"},
					InstrumentoNegociacion		: {type:"string"},
					IdMonedaNegociacion			: {type:"string"},
					IdCondicionDeLiquidacion	: {type:"string"}
				}
			}
		};
	};
	
	
	
	obj.dataSourceInformeOrdenesConfirmadas = new bcs.DataSource({
		type 		: "call", //Los tipos posibles son: odata, call, array y object
		definition 	: {
						serverFiltering : false, // Filtrado de datos en el servidor.
						serverPaging 	: false, // Paginado de los datos en el servidor.
						pageSize		: 200,
						callTransaction	: "2901",
						callResultSet	: "P_OUT_TBL",
			            schema 			: modeloInformeOrdenesConfirmadas()
		              }  
	});
	
	
	
	obj.columnasInformeOrdenesConfirmadas = function() {
		return[
			{field : "Mercado", title:'UI_MERCADO', width : "200px", align: "left", sortable : false }
		   ,{field : "DocumentoIdentidad", title:'UI_DOCUMENTO_IDENTIDAD', width : "110px", align: "left", sortable : false }	
		   ,{field : "Cliente", title:'UI_NOMBRE_CLIENTE', width : "110px", align: "left", sortable : false }	
		   ,{field : "IdOrden", title:'UI_SECUENCIA_ORDEN', width : "110px", align: "left", sortable : false }
		   ,{field : "Producto", title:'UI_PRODUCTO', width : "100px", align: "right", sortable : false}
		   ,{field : "FechaIngreso", title:'UI_FECHA_INGRESO', width : "100px", align: "right", sortable : false}
		   ,{field : "FechaConfirmacionPlazo", title:'UI_CONFIRMACION_PLAZO', width : "100px", align: "right", sortable : false}
		   ,{field : "FechaConfirmacion", title:'UI_FECHA_CONFIRMACION', width : "100px", align: "right", sortable : false}
		   ,{field : "EstadoConfirmacion", title:'UI_ESTADO_CONFIRMACION', width : "110px", align: "left", sortable : false }	
		   ,{field : "Operacion", title:'UI_TIPO_OPERACION', width : "100px", align: "right", sortable : false}
		   ,{field : "Instrumento", title:'UI_NEMOTECNICO', width : "110px", align: "left", sortable : false }
		   ,{field : "Cantidad", title:'UI_CANTIDAD', width : "110px", align: "left", sortable : false, template : "<label> #: (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMonedaOrden,'','')).valor #</label>" }	
		   ,{field : "Precio", title:'UI_PRECIO', width : "110px", align: "left", sortable : false, template : "<label> #: (util.bcs_formato(Precio,'GEN','Precio',IdMonedaOrden,'','')).valor #</label>" }	
		   ,{field : "EstadoOrden", title:'UI_ESTADO_ORDEN', width : "110px", align: "left", sortable : false }	
		   ,{field : "InstrumentoNegociacion", title:'UI_INSTRUMENTO_NEGOCIACION', width : "110px", align: "left", sortable : false }
		   ,{field : "IdMonedaNegociacion", title:'UI_MONEDA_NEGOCIACION', width : "110px", align: "left", sortable : false }
		   ,{field : "IdCondicionDeLiquidacion", title:'UI_CONDICION_LIQUIDACION', width : "110px", align: "left", sortable : false }
		];
	};
		
	
		
	return obj;
});	


