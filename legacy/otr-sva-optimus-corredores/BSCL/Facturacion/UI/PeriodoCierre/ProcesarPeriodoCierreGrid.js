angular.module('ModuloFacturacion').factory('bcsPeriodoCierreGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	obj.fnColorBk = function(row) {
        var color = "";
		
		if (row.IdEstadoPeriodo === 'ABT' )
		   color = "green";
	    else
	       color = "red";

        return color;
  	};
	

	transportePeriodoCierre = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4219",
				dataType : "json"
			}
		};
	};
	
	
	modeloPeriodoCierre = function() {
		return {
			model: {
				fields : {
					IdPeriodoCierre			: { type: "number"},
					Periodo 				: { type: "string"},
					IdEstadoPeriodo			: { type: "string"},
					NombreEstadoPeriodoCierre : { type: "string"},
					IdEstado				: { type: "string"},					
					IdUsuario 				: { type: "number"}
				}
			}
		};
	};
	
	
	
	obj.dataSourcePeriodoCierre = new bcs.DataSource({
		type 		: "odata", 
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "Periodo", dir: "asc" }, // Criterios de Ordenamiento.
			            pageSize 		    : 12,
			            transport 			: transportePeriodoCierre(),
			            schema 				: modeloPeriodoCierre(),			
		              }
	});
	
	
	obj.columnasPeriodoCierre = function() {
		return  [
			{field : "Periodo", title:'UI_PERIODO', width : "80px" , align: "center", sortable : false }
		   ,{field : "NombreEstadoPeriodoCierre", title:'UI_ESTADO', width : "90px" , align: "center", sortable : false }	
		   ,{field : "IdUsuario", title:'UI_USUARIO', width : "90px" , align: "center", sortable : false }		   
		];
	};
	
	return obj;

});