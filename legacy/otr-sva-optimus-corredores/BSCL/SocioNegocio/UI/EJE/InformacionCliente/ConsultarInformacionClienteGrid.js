		
angular.module('ModuloMesa').factory('bcsConsultarInformacionClienteGrid',
		function(bcsComunes, $rootScope)  
{
	
	var obj = {};
	
	
	
	transporteInformacionCliente = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=528",
				dataType : "json"
			}
		};
	};	
	
	
	modeloInformacionCliente = function() {
		return {
			model: {
				fields : {
					DocumentoIdentidadCliente : { type: "string"},
					NombreCliente 			: { type: "string"},
					NombreTipoPersona 		: { type: "string"},
					NombreEjecutivo         : { type: "string"},
					DocumentoIdentidadRepLegal : { type: "string"},
					NombreRepLegal          : { type: "string"},
					Cargo 					: { type: "string"},
					FechaNacimiento 		: { type: "sysdate"},
					DireccionElectronica 	: { type: "string"},
					Pesos					: { type: "number"}
				}
			}
		};
	};
	
	
	
	obj.dataSourceInformacionCliente = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			            sort 				: { field: "NombreCliente", dir: "desc" }, // Criterios de Ordenamiento.
			            pageSize 			: 50, // Número de registros por página.
			            transport 			: transporteInformacionCliente(),
			            schema 				: modeloInformacionCliente()
		              }  
	});
	
	
	
	obj.columnasInformacionCliente = function() {
		return[
			{field : "DocumentoIdentidadCliente", title:'UI_RUT_CLIENTE', width : "80px", sortable : false }
		   ,{field : "NombreCliente", title:'UI_NOMBRE_CLIENTE', width : "200px", sortable : false }	
		   ,{field : "NombreTipoPersona", title:'UI_TIPO_PERSONA', width : "110px", sortable : false }
		   ,{field : "NombreEjecutivo", title:'UI_AGENTE', width : "200px", sortable : false }
		   ,{field : "DocumentoIdentidadRepLegal", title:'UI_RUT_REPRESENTANTE_LEGAL', width : "80px", sortable : false}
		   ,{field : "NombreRepLegal", title:'UI_NOMBRE_REPRESENTANTE_LEGAL', width : "200px", sortable : false}
		   ,{field : "Cargo", title:'UI_CARGO', width : "200px", sortable : false}
		   ,{field : "FechaNacimiento", title:'UI_FECHA_NACIMIENTO', width : "80px", sortable : false }
		   ,{field : "DireccionElectronica", title:'UI_EMAIL', width : "200px", sortable : false }
		   ,{field : "Pesos", title:'UI_PESOS', width : "100px", sortable : false }
		];
	};
		
	
		
	return obj;
});	


