angular.module('ModuloSocioNegocio').factory('bcsCargaMasivaGrid',['bcsComunes',
		function(bcsComunes)
{
	
	var obj = {};	
	
	//-----------------------------
	// CARGA MASIVA
	//-----------------------------
	obj.dsCargaMasiva = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true,
			serverPaging 		: true,
			serverSorting 		: false,
			sort 				: { field: "IdCargaMasiva", dir: "desc" },
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700421",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdCargaMasiva				: { type: "number"},
						FechaCarga					: { type: "sysdate"},
						FechaProceso				: { type: "sysdate"},
						CodigoUsuario				: { type: "string"},
						CantidadRegistros			: { type: "number"},						
						NombreArchivo				: { type: "string"},
						NombreEstadoCargaMasiva		: { type: "string"},						
					}
				}
			}
		}
	});
	
	
	
	obj.columnasCargaMasiva = function() {
		return  [
		     {field: "IdCargaMasiva", 				title: "UI_FOLIO_CARGA",        width: "50px"}
		    ,{field: "FechaCarga",				    title: "UI_FECHA_CARGA", 		width: "80px",	groupable: true }
			,{field: "CantidadRegistros", 			title: "UI_CANTIDAD_REGISTROS", width: "80px"}
			,{field: "CodigoUsuario", 				title: "UI_USUARIO", 			width: "80px",  groupable: true }
			,{field: "FechaProceso", 				title: "UI_FECHA_PROCESO", 		width: "80px",	groupable: true }
			,{field: "NombreEstadoCargaMasiva", 	title: "UI_ESTADO", 			width: "80px",  groupable: true }
			,{field: "NombreArchivo", 	            title: "UI_NOMBRE_ARCHIVO",  groupable: true,   attributes:{style:"white-space:nowrap"} }
		];
	};	
	
	
	
	//-----------------------------
	// CARGA MASIVA DETALLE
	//-----------------------------
	obj.dsCargaMasivaDetalle = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true,
			serverPaging 		: true,
			serverSorting 		: false,
			sort 				: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700424",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdCargaMasiva					: {type: "number"},
						IdCargaMasivaDetalle			: {type: "number"},						
						Entidad							: {type: "string"},
						DocumentoIdentidad				: {type: "string"},
						NombreSocioDeNegocio			: {type: "string"},	
						IdPortafolio					: {type: "number"},
						CodigoPortafolio				: {type: "string"},
						CodigoIndicador					: {type: "string"},
						ValorIndicador					: {type: "string"},
						ObservacionValidacion			: {type: "string"},
						IdEstadoCargaMasivaDetalle		: {type: "string"},
						NombreEstadoCargaMasivaDetalle	: {type: "string"},					
					}
				}
			}
		}
	});
	
	
	obj.dsDefCargaMasivaDetalle = {
		type : "odata",
		definition : {
			serverFiltering 	: true,
			serverPaging 		: true,
			serverSorting 		: false,
			sort 				: { field: "IdCargaMasivaDetalle", dir: "asc" },				
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700424",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdCargaMasiva					: {type: "number"},
						IdCargaMasivaDetalle			: {type: "number"},
						Entidad							: {type: "string"},							
						DocumentoIdentidad				: {type: "string"},
						NombreSocioDeNegocio			: {type: "string"},
						IdPortafolio					: {type: "number"},
						CodigoPortafolio				: {type: "string"},
						CodigoIndicador					: {type: "string"},
						ValorIndicador					: {type: "string"},
						ObservacionValidacion			: {type: "string"},
						IdEstadoCargaMasivaDetalle		: {type: "string"},
						NombreEstadoCargaMasivaDetalle	: {type: "string"},						
					}
				}
			}
		}
	};
	 
	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
					{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",	 	width: "90px",	groupable: true }
					,{field: "NombreSocioDeNegocio",			title: "UI_SOCIO_DE_NEGOCIO",		 	width: "170px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "Entidad", 						title: "UI_ENTIDAD",	 			 	width: "80px",	groupable: true }
					,{field: "IdPortafolio", 					title: "UI_PORTAFOLIO", 			 	width: "65px",	groupable: true }
					,{field: "CodigoPortafolio", 				title: "UI_CODIGO_PORTAFOLIO", 		 	width: "80px",	groupable: true }
					,{field: "CodigoIndicador", 				title: "UI_CODIGO_INDICADOR", 		 	width: "80px",	groupable: true }
					,{field: "ValorIndicador", 					title: "UI_VALOR_INDICADOR", 		 	width: "80px",	groupable: true }
					,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 			 	width: "170px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "NombreEstadoCargaMasivaDetalle", 	title: "UI_ESTADO", 				 	width: "80px",  groupable: true }
		];
	};
	
	
	return obj;
}]);	

