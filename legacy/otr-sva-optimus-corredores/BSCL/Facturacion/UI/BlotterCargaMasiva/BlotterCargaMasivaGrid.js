angular.module('ModuloFacturacion').factory('bcsBlotterCargaMasivaGrid',['bcsComunes',
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
			//pageSize 			: 25,
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1798",dataType : "json"}},
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
			//se obtiene después de procesar la CM	
			,{field: "FechaProceso", 				title: "UI_FECHA_PROCESO", 		width: "80px",	groupable: true }			
			,{field: "NombreArchivo", 	            title: "UI_NOMBRE_ARCHIVO",  groupable: true,   attributes:{style:"white-space:nowrap"} }
			,{field: "NombreEstadoCargaMasiva", 	title: "UI_ESTADO", 			width: "80px",  groupable: true }			
		];
	};	
	
	
	
	//-----------------------------
	// CARGA MASIVA DETALLE
	//-----------------------------
	obj.dsCargaMasivaDetalle = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },
			//pageSize 			: 25,
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1800",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdCargaMasiva					: {type: "number"},
						IdCargaMasivaDetalle			: {type: "number"},						
						IdConcepto						: {type: "string"},
						NombreConcepto					: {type: "string"},
						IdTipoOperacion					: {type: "string"},
						NombreTipoOperacion				: {type: "string"},
						DocumentoIdentidad				: {type: "string"},
						NombreSocioDeNegocio			: {type: "string"},	
						IdPortafolio					: {type: "number"},
						CodigoPortafolio				: {type: "string"},
						PropositoPortafolio				: {type: "string"},	
						IdTipoComprobante				: {type: "string"},
						NombreTipoComprobante			: {type: "string"},
						IdCondicionLiquidacion			: {type: "string"},
						CodigoCondicionDeLiquidacion	: {type: "string"},
						IdMoneda						: {type: "string"},
						Monto							: {type: "number"},
						Glosa							: {type: "string"},
						Referencia						: {type: "string"},
						TipoReferencia					: {type: "string"},
						IdMedioDePago					: {type: "string"},
						NombreMedioDePago				: {type: "string"},
						IdModalidadDePago				: {type: "string"},
						NombreModalidadDePago			: {type: "string"},
						ObservacionValidacion			: {type: "string"},
						IdEstadoCargaMasivaDetalle		: {type: "string"},
						NombreEstadoCargaMasivaDetalle	: {type: "string"},
						IdMovimientoComprobante         : {type: "number"},						
					}
				}
			}
		}
	});
	

	obj.dsDefCargaMasivaDetalle = {
			type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: false, // Ordenamiento de los datos en el servidor.
				sort 				: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },				
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1800",dataType : "json"}},
				schema : {
					model : {
						fields : {
							IdCargaMasiva					: {type: "number"},
							IdCargaMasivaDetalle			: {type: "number"},
							IdConcepto						: {type: "string"},
							NombreConcepto					: {type: "string"},
							IdTipoOperacion					: {type: "string"},
							NombreTipoOperacion				: {type: "string"},							
							DocumentoIdentidad				: {type: "string"},
							NombreSocioDeNegocio			: {type: "string"},
							IdPortafolio					: {type: "number"},
							CodigoPortafolio				: {type: "string"},
							PropositoPortafolio				: {type: "string"},
							IdMatrizAgrupador               : {type: "number"},
							IdTipoComprobante				: {type: "string"},
							NombreTipoComprobante			: {type: "string"},
							IdCondicionLiquidacion			: {type: "string"},
							CodigoCondicionDeLiquidacion	: {type: "string"},
							IdMoneda						: {type: "string"},
							Monto							: {type: "number"},							
							Glosa							: {type: "string"},
							Referencia						: {type: "string"},
							TipoReferencia					: {type: "string"},
							IdMedioDePago					: {type: "string"},
							NombreMedioDePago				: {type: "string"},
							IdModalidadDePago				: {type: "string"},
							NombreModalidadDePago			: {type: "string"},							
							ObservacionValidacion			: {type: "string"},
							IdEstadoCargaMasivaDetalle		: {type: "string"},
							NombreEstadoCargaMasivaDetalle	: {type: "string"},
							IdMovimientoComprobante         : {type: "number"},							
						}
					}
				}
			}
		};
	 
	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
					 {field: "NombreTipoOperacion", 			title: "UI_TIPO_OPERACION", 		 	width: "100px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "NombreConcepto", 					title: "UI_CONCEPTO", 				 	width: "110px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",	 	width: "90px",	groupable: true }
					,{field: "NombreSocioDeNegocio",			title: "UI_SOCIO_DE_NEGOCIO",		 	width: "170px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "IdPortafolio", 					title: "UI_PORTAFOLIO", 			 	width: "65px",	groupable: true }
					,{field: "CodigoPortafolio", 				title: "UI_CODIGO_PORTAFOLIO", 		 	width: "80px",	groupable: true }					
					,{field: "PropositoPortafolio", 			title: "UI_PROPOSITO_PORTAFOLIO", 	 	width: "100px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "IdMatrizAgrupador", 				title: "UI_MATRIZ_AGRUPADOR", 			width: "65px",	groupable: true }
					,{field: "NombreTipoComprobante",      		title: "UI_TIPO_COMPROBANTE", 		 	width: "130px",	groupable: true, attributes:{style:"white-space:nowrap"} }
					,{field: "CodigoCondicionDeLiquidacion",    title: "UI_CONDICION_LIQUIDACION", 	 	width: "85px",	groupable: true }					
					,{field: "IdMoneda",        				title: "UI_MONEDA", 				 	width: "70px",	groupable: true }
					,{field: "Monto", 			        		title: "UI_MONTO",        			 	width: "80px",  template : " <label> #: util.formateaNumero(Monto,0) #</label>"}
					,{field: "Glosa", 							title: "UI_GLOSA", 					 	width: "130px", groupable: true, attributes:{style:"white-space:nowrap"} }				
					,{field: "Referencia", 						title: "UI_REFERENCIA_DOCUMENTO", 	 	width: "100px", groupable: true, attributes:{style:"white-space:nowrap"} }				
					,{field: "TipoReferencia", 					title: "UI_TIPO_REFERENCIA", 		 	width: "80px", groupable: true, attributes:{style:"white-space:nowrap"} }				
					,{field: "NombreMedioDePago",        		title: "UI_MEDIO_DE_PAGO", 			 	width: "85px",	groupable: true }
					,{field: "NombreModalidadDePago",        	title: "UI_MODALIDAD_PAGO", 		 	width: "85px",	groupable: true }
					,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 			 	width: "170px",	groupable: true, attributes:{style:"white-space:nowrap"} }										
					,{field: "NombreEstadoCargaMasivaDetalle", 	title: "UI_ESTADO", 				 	width: "80px",  groupable: true }
					,{field: "IdMovimientoComprobante", 	    title: "UI_SECUENCIA_MOVIMIENTO", 	 	width: "85px",  groupable: true }					
		];
	}; 
	
	
	return obj;
}]);	

