angular.module('ModuloMesa').factory('bcsReporteGestionClienteGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	
	modeloReporteGestionCliente = function() {
		return {
			model: {
				fields : {
					NombreCliente 			    : { type: "string"},
					Nemotecnico 				: { type: "string"},
					Pesos		 				: { type: "number"},
					Nominales		            : { type: "number"},
					Porcentaje			        : { type: "number"},
					TotalPesos		 			: { type: "number"},
					TotalNominales	 			: { type: "number"},
					TotalPorcentaje 		    : { type: "number"}
				}
			}
		};
	};
	
	
	
	obj.dataSourceReporteGestionCliente = new bcs.newDataSource({
		type 		: "call", //Los tipos posibles son: odata, call, array y object
		definition 	: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						pageSize		: 200,
						callTransaction	: "2900",
						callResultSet	: "P_OUT_TBL",
			            schema 			: modeloReporteGestionCliente()
		              }  
	});
	
	
	
	obj.columnasReporteGestionCliente = function() {
		return[
			{field : "NombreCliente", title:'UI_NOMBRE_CLIENTE', width : "200px", align: "left", sortable : false }
		   ,{field : "Nemotecnico", title:'UI_NEMO', width : "110px", align: "left", sortable : false }	
		   ,{field : "Pesos", title:'UI_PESOS', width : "110px", align: "left", sortable : false }
		   ,{field : "Nominales", title:'UI_CANTIDAD_NOMINALES', width : "100px", align: "right", sortable : false, template : "<label> #: (util.bcs_formato(Nominales,'GEN','Cantidad','CLP','','')).valor #</label>"}
		   ,{field : "Porcentaje", title:'UI_PORCENTAJE', width : "100px", align: "right", sortable : false}
		   ,{field : "TotalPesos", title:'UI_TOTAL_PESOS', width : "100px", align: "right", sortable : false}
		   ,{field : "TotalNominales", title:'UI_TOTAL_NOMINALES', width : "100px", align: "right", sortable : false}
		   ,{field : "TotalPorcentaje", title:'UI_PORCENTAJE_TOTAL', width : "110px", align: "left", sortable : false }			
		];
	};
		
	
		
	return obj;
});	


