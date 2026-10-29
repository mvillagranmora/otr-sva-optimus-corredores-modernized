angular.module('ModuloSocioNegocio').factory('bcsExclusionLimpiezaLineaOperacionalGrid',['bcsComunes',
		function(bcsComunes)
{
	
	var obj = {};	
	
	//-----------------------------
	// CARGA MASIVA
	//-----------------------------
	obj.dsExclusionLimpiezaLineaOperacional = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true,
			serverPaging 		: true,
			serverSorting 		: false,
			sort 				: { field: "IdPortafolio", dir: "desc" },
			transport 			: { read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=702173",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdPortafolio				: { type: "number"},
						CodigoPortafolio			: { type: "string"},
						IdCliente					: { type: "number"},						
						NombreCompleto				: { type: "string"},
						DocumentoIdentidad			: { type: "string"}
					}
				}
			}
		}
	});
	
	obj.columnasExclusionLimpiezaLineaOperacional = function() {
		return  [
		     {field: "IdPortafolio", 				title: "UI_ID_PORTAFOLIO_ABR",		width: "50px"}
		    ,{field: "CodigoPortafolio",			title: "UI_CODIGO_PORTAFOLIO", 		width: "80px"}
			,{field: "NombreCompleto", 		title: "UI_CLIENTE", 				width: "100px"}
			,{field: "DocumentoIdentidad", 			title: "UI_DOCUMENTO_IDENTIDAD", 	width: "80px"}
		];
	};
	
	return obj;
}]);	

