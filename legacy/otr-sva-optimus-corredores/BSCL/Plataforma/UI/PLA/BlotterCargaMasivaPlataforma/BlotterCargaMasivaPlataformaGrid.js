angular.module('ModuloPlataforma').factory('bcsBlotterCargaMasivaPlataformaGrid',['bcsComunes',
		function(bcsComunes)
{
	
	var obj = {};	
	
	//-----------------------------
	// CARGA MASIVA
	//-----------------------------
	
	obj.dsCargaMasiva = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdCargaMasiva", dir: "desc" },
			filter 				: { field: "IdEstadoCargaMasiva", operator: "eq", value: 'ANU' },
			pageSize 			: 45,
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=8012",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdTipoCargaMasiva			: { type: "number"},
						CodigoTipoCargaMasiva		: { type: "string"},
						IdCargaMasiva				: { type: "number"},
						FechaCarga					: { type: "sysdate"},
						FechaProceso				: { type: "sysdate"},
						CodigoUsuario				: { type: "string"},
						CantidadRegistros			: { type: "number"},
						NombreArchivo				: { type: "string"},
						IdEstadoCargaMasiva			: { type: "string"},
						NombreEstadoCargaMasiva		: { type: "string"},
					}
				}
			}
		}
	});
	
	
	obj.columnasCargaMasiva = function() {
		return  [
		     {field: "IdCargaMasiva", 				title: "UI_FOLIO_CARGA",        width: "70px"}
		    ,{field: "FechaCarga",				    title: "UI_FECHA_CARGA", 		width: "80px",	 groupable: true } 
			,{field: "FechaProceso", 				title: "UI_FECHA_PROCESO", 		width: "90px",	 groupable: true }			
			,{field: "CantidadRegistros", 			title: "UI_CANTIDAD_REGISTROS", width: "90px"}
			,{field: "CodigoUsuario", 				title: "UI_USUARIO", 			width: "80px",  groupable: true }
			,{field: "NombreEstadoCargaMasiva", 	title: "UI_ESTADO", 			width: "80px",  groupable: true }			
			,{field: "NombreArchivo", 	            title: "UI_NOMBRE_ARCHIVO", 		           }
		];
	};	
	
	
	
	//-----------------------------
	// CARGA MASIVA DETALLE
	//-----------------------------	
	
	obj.dsDefCargaMasivaDetallePlataforma = {
			type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },				
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=8013",dataType : "json"}},
				schema : {
					model : {
						fields : {
							IdCargaMasivaDetalle			: { type: "number"},
							Nemotecnico						: { type: "string"},
							ObservacionValidacion 	        : { type: "string"},
							RF_PorcentajeDescuento			: { type: "string"},
							RV_IdEntidadClasificadoraRiesgo	: { type: "string"},
							RV_Valor 	        			: { type: "string"},
						}
					}
				}
			}
		};
	
	
	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
		     {field: "IdCargaMasivaDetalle", 			title: "UI_FOLIO", 		        	width: "70px"}
			,{field: "Nemotecnico",						title: "UI_NEMOTECNICO",			width: "110px",	groupable: true }
			,{field: "RF_PorcentajeDescuento",			title: "UI_PORCENTAJE_DESCUENTO",	width: "70px",	groupable: true }
			,{field: "RV_IdEntidadClasificadoraRiesgo",	title: "UI_ENTIDAD",				width: "110px",	groupable: true }
			,{field: "RV_Valor",						title: "UI_VALOR",					width: "60px",	groupable: true }	
			,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 			width: "200px", groupable: true }
		];
	}; 
	

	return obj;
}]);	

