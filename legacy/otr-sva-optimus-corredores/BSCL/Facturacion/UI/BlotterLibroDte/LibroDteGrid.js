		
angular.module('ModuloFacturacion').factory('bcsLibroDteGrid',['bcsComunes',
		function(bcsComunes)  
{
	
	var obj = {};	
	
 
	transporteLibroDte = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4217",
				dataType : "json"
			}
		};
	};	
	
	modeloLibroDte = function() {
		return {
			model: {
				fields : {
					IdLibro                      : { type: "number"},
			        Tipo                         : { type: "string"},
			        DocumentoIdentidadEmpresa    : { type: "string"}, 
			        IdEstado                     : { type: "string"}, 
			        Estado                       : { type: "string"},
				}
			}
		};
	};
	
		
	
	obj.dataSourceLibroDte = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "IdLibro", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 25, // Número de registros por página.
			            transport 			: transporteLibroDte(),
			            schema 				: modeloLibroDte(),
		              }
	});
	
	
	obj.columnasLibroDte = function() {
		return  [   
	   	     {field: "IdLibro", title: "UI_LIBRO", width: "30px",  align: "right" }
			,{field: "Tipo", 	title: "UI_TIPO", width: "50px", align: "left", groupable: true }
			,{field: "DocumentoIdentidadEmpresa", title: "UI_DOCUMENTO_IDENTIDAD", width: "80px",  groupable: true }
			,{field: "Estado", title: "UI_ESTADO", width: "50px",  align: "left" }		
		];
	};

	
		
	return obj;
}]);	


