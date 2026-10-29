angular.module('ModuloFacturacion').factory('bcsConfiguracionAFPGrid',['bcsComunes',
		function(bcsComunes)  
{
	var obj = {};	
	
	//CONFIGURACION AFP 
	transporteConfiguracionAFP = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290603",
				dataType : "json"
			}
		};
	};	
	
	modeloConfiguracionAFP = function() {
		return {
			model: {
				fields : {		
					IdConfiguracionAFP	: {type: "number"},			
					NombreAFP			: {type: "string"},
					NombreMultifondo	: {type: "string"},
					DocumentoCliente	: {type: "string"},
					NombreCliente		: {type: "string"},
					CodigoPortafolio	: {type: "string"}
					
				}
			}
		};
	};
	
	obj.dataSourceConfiguracionAFP = new bcs.DataSource({
		type 		: "odata", //Los tipos posibles son: odata, call, array y object
		definition 	: {
			            serverFiltering 	: true, // Filtrado de datos en el servidor.
			            serverPaging 		: true, // Paginado de los datos en el servidor.
			            serverSorting 		: true, // Ordenamiento de los datos en el servidor.
//			            sort 				: { field: "NombreAFP", dir: "asc" }, // Criterios de Ordenamiento.
			            pageSize 			: 30, // Número de registros por página.
			            transport 			: transporteConfiguracionAFP(),
			            schema 				: modeloConfiguracionAFP()
		              }
	});
	
	obj.ColumnasConfiguracionAFP = function() {
		return [
			{field: "IdConfiguracionAFP",	title: "UI_ID", 					width: "20px", sortable : false, groupable: false, 	hidden: true },
			{field: "NombreAFP",			title: "UI_AFP", 					width: "60px", sortable : false, groupable: false },
			{field: "NombreMultifondo",		title: "UI_MULTIFONDO", 			width: "20px", sortable : false, groupable: false },
			{field: "DocumentoCliente",		title: "UI_DOCUMENTO_IDENTIDAD",	width: "30px", sortable : false, groupable: false },
			{field: "NombreCliente", 		title: "UI_NOMBRE_CLIENTE",			width: "70px", sortable : false },
			{field: "CodigoPortafolio", 	title: "UI_CODIGOPORTAFOLIO", 		width: "40px", sortable : false }
		];
	};
	
	return obj;
}]);	


