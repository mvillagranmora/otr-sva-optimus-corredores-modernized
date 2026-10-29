angular.module('ModuloCustodia').factory('bcsBlotterCargaMasivaCustodiaGrid',['bcsComunes',
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
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3200",dataType : "json"}},
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
	
	obj.dsDefCargaMasivaDetalleCustodia = {
			type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },				
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3205",dataType : "json"}},
				schema : {
					model : {
						fields : {
							IdCargaMasivaDetalle			: { type: "number"},
							DocumentoIdentidad				: { type: "string"},
							CodigoPortafolio				: { type: "string"},
							Codigo							: { type: "string"},
							IdBoveda						: { type: "string"},
							IdConcepto						: { type: "string"},
							Nemotecnico						: { type: "string"},
							Emisor							: { type: "string"},
							IdTIpoReajuste					: { type: "string"},
							FechaVencimiento				: { type: "sysdate"},
							Cantidad						: { type: "number"},
							Precio							: { type: "number"},
							Tasa							: { type: "number"},
							Monto							: { type: "number"},
							Glosa							: { type: "string"},
							FechaIngresoRegistro			: { type: "string"},
							NombreEstadoCargaMasivaDetalle  : { type: "string"},
							ObservacionValidacion 	        : { type: "string"}
						}
					}
				}
			}
		};
	
	
	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
		     {field: "IdCargaMasivaDetalle", 			title: "UI_FOLIO", 		        	width: "70px"}
			,{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",	width: "100px",	groupable: true }
			,{field: "CodigoPortafolio",				title: "UI_PORTAFOLIO",				width: "70px",	groupable: true }
			,{field: "Codigo",							title: "UI_CONDICION_LIQUIDACION",	width: "100px",	groupable: true }
			,{field: "IdBoveda",						title: "UI_BOVEDA",					width: "60px",	groupable: true }
			,{field: "IdConcepto",						title: "UI_CONCEPTO",				width: "70px",	groupable: true }
			,{field: "Nemotecnico",						title: "UI_NEMOTECNICO",			width: "100px",	groupable: true }
			,{field: "Emisor",							title: "UI_EMISOR",					width: "80px",	groupable: true }
			,{field: "IdTipoReajuste",					title: "UI_TIPO_REAJUSTE",			width: "70px",	groupable: true }
			,{field: "FechaVencimiento",				title: "UI_FECHA_VENCIMIENTO",		width: "100px",	groupable: true }
			,{field: "Cantidad", 			       		title: "UI_CANTIDAD",        		width: "90px",  template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad','CLP','','')).valor #</label>"}			
			,{field: "Precio", 			   	    		title: "UI_PRECIO",	        		width: "90px",  template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio','CLP','','')).valor #</label>"}
			,{field: "Tasa",	 			       		title: "UI_TASA", 		       		width: "90px",  template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa','CLP','','')).valor #</label>"}
			,{field: "Monto", 			        		title: "UI_MONTO",        			width: "90px",  template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>"}
			,{field: "Glosa",							title: "UI_GLOSA",					width: "200px",	groupable: true }
			,{field: "NombreEstadoCargaMasivaDetalle", 	title: "UI_ESTADO", 				width: "90px", groupable: true }			
			,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 			width: "200px", groupable: true }
		];
	}; 
	

	return obj;
}]);	

