angular.module('ModuloOrdenes').factory('bcsBlotterCargaMasivaGrid',['bcsComunes',
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
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=2003",dataType : "json"}},
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
						CodigoTipoCargaMasiva		: { type: "string"}
					}
				}
			}
		}
	});

	
	obj.columnasCargaMasiva = function() {
		return  [
		     {field: "IdCargaMasiva", 				title: "UI_FOLIO_CARGA",        width: "80px"}
		    ,{field: "FechaCarga",				    title: "UI_FECHA_CARGA", 		width: "90px",	groupable: true }
			,{field: "CantidadRegistros", 			title: "UI_CANTIDAD_REGISTROS", width: "80px"}			
			,{field: "CodigoUsuario", 				title: "UI_USUARIO", 			width: "90px",  groupable: true }			
			//Datos que se obtiene después de procesar la CM	
			,{field: "FechaProceso", 				title: "UI_FECHA_PROCESO", 		width: "90px",	groupable: true }			
			,{field: "NombreEstadoCargaMasiva", 	title: "UI_ESTADO", 			width: "90px",  groupable: true }
			,{field: "NombreArchivo", 	            title: "UI_NOMBRE_ARCHIVO",  	groupable: true,   attributes:{style:"white-space:nowrap"} }
			];
	};	
	
	
	
	//-----------------------------
	// CARGA MASIVA DETALLE
	//-----------------------------	

	obj.dsDefCargaMasivaDetalle = {
			type : "odata",
			definition : {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				
				pageSize 			: 200,
				sort 				: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },				
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=2006",dataType : "json"}},
				schema : {
					model : {
						fields : {							
							 	IdCargaMasiva					: { type: "number"},
								IdTipoCargaMasiva				: { type: "number"},
								CodigoTipoCargaMasiva			: { type: "string"},
								IdEmpresa						: { type: "number"},
								IdUsuario						: { type: "number"},
							  	FechaCarga						: { type: "sysdate"},
							 	FechaProceso					: { type: "sysdate"},
							 	FechaIngresoRegistro			: { type: "sysdate"},
							 	NombreArchivo					: { type: "string"},
								IdEstadoCargaMasiva				: { type: "string"},
								IdCargaMasivaDetalle			: { type: "number"},
								ObservacionValidacion			: { type: "string"},
								IdEstadoCargaMasivaDetalle		: { type: "string"},
								NombreEstadoCargaMasivaDetalle	: { type: "string"},
								
								DocumentoIdentidad				: { type: "string"},
								CodigoPortafolio				: { type: "string"},
								TipoOperacion					: { type: "string"},
								IdMecanismoCaptacion			: { type: "string"},
								Cantidad						: { type: "number"},
								Nemotecnico						: { type: "string"},
								Precio							: { type: "number"},
								IdCondicionDeLiquidacion		: { type: "string"},
								IdTipoVigenciaOrden				: { type: "string"},
								FechaVigencia					: { type: "string"},
								IdFormaCustodia					: { type: "string"},
								IdBolsa							: { type: "string"},
								IdBoveda						: { type: "string"},								
								IdTipoPrecioOrden				: { type: "string"},
								IdOperacionGenerada				: { type: "number"},

								TipoInstruccion					: { type: "string"},
								Emisor							: { type: "string"},
								FechaVencimiento				: { type: "string"},
								ValorRescate					: { type: "number"},
								FechaTrade						: { type: "string"},
								Intermediario					: { type: "string"},
								FolioFactura					: { type: "string"},
								MontoLiquidacion				: { type: "number"},
								
								Tasa							: { type: "number"},
								IdTipoReajuste					: { type: "string"}, 
								TotalCobros						: { type: "number"} 							
						}
					}
				}
			}
		};

	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
			 {field: "DocumentoIdentidad", 				title: "UI_DOCUMENTO_IDENTIDAD", 	width: "85px",	groupable: true }
			,{field: "CodigoPortafolio", 				title: "UI_CODIGO_PORTAFOLIO", 		width: "80px",	groupable: true }
			,{field: "TipoOperacion", 					title: "UI_TIPO_OPERACION", 		width: "80px",	groupable: true }
			,{field: "IdMecanismoCaptacion", 			title: "UI_MECANISMO_CAPTACION", 	width: "85px",	groupable: true }			
			,{field: "Nemotecnico", 					title: "UI_NEMOTECNICO", 			width: "80px",	groupable: true }
			,{field: "Cantidad", 						title: "UI_CANTIDAD", 				width: "80px",	groupable: true }
			,{field: "Precio", 							title: "UI_PRECIO", 				width: "80px",	groupable: true }			
			,{field: "IdCondicionDeLiquidacion", 		title: "UI_CONDICION_LIQUIDACION", 	width: "85px",	groupable: true }
			,{field: "IdTipoVigenciaOrden", 			title: "UI_TIPO_VIGENCIA", 			width: "80px",	groupable: true }
			,{field: "FechaVigencia", 					title: "UI_FECHA_VIGENCIA", 		width: "90px",	groupable: true }
			,{field: "IdFormaCustodia", 				title: "UI_FORMA_CUSTODIA", 		width: "85px",	groupable: true }
			,{field: "IdBolsa", 						title: "UI_BOLSA", 					width: "80px",	groupable: true }
			,{field: "IdBoveda", 						title: "UI_BOVEDA", 				width: "80px",	groupable: true }			
			,{field: "NombreEstadoCargaMasivaDetalle", 	title: "UI_ESTADO", 				width: "85px",  groupable: true }
			,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 			width: "300px",groupable: true, attributes:{style:"white-space:nowrap"} }
			,{field: "IdOperacionGenerada", 			title: "UI_ORDEN", 					width: "80px",	groupable: true }
			
		];
	}; 
	
	obj.columnasCargaMasivaInstruccionDetalle = function() {
		return  [
			 {field: "DocumentoIdentidad", 				title: "UI_DOCUMENTO_IDENTIDAD", 		width: "85px",	groupable: true }
			,{field: "CodigoPortafolio", 				title: "UI_PORTAFOLIO", 				width: "80px",	groupable: true }
			,{field: "TipoInstruccion", 				title: "UI_TIPO_INSTRUCCION", 			width: "80px",	groupable: true }			
			,{field: "TipoOperacion", 					title: "UI_OPERACION", 					width: "85px",	groupable: true }
			,{field: "FechaTrade", 						title: "UI_FECHA_TRADE", 				width: "90px",	groupable: true }
			,{field: "FolioFactura", 					title: "UI_FOLIO_FACTURA", 				width: "80px",	groupable: true }
			,{field: "Intermediario", 					title: "UI_INTERMEDIARIO", 				width: "80px",	groupable: true }
			,{field: "IdCondicionDeLiquidacion", 		title: "UI_CONDICION_LIQUIDACION", 	    width: "85px",	groupable: true }			
			,{field: "Nemotecnico", 					title: "UI_NEMOTECNICO", 				width: "80px",	groupable: true }
			,{field: "Emisor", 							title: "UI_EMISOR", 					width: "80px",	groupable: true }
			,{field: "IdTipoReajuste", 					title: "UI_TIPO_REAJUSTE", 				width: "80px",	groupable: true }
			,{field: "FechaVencimiento", 				title: "UI_FECHA_VENCIMIENTO", 			width: "80px",	groupable: true }			
			,{field: "Cantidad", 						title: "UI_CANTIDAD", 					width: "80px",	groupable: true }			
			,{field: "ValorRescate", 					title: "UI_VALOR_RESCATE", 				width: "80px",	groupable: true }
			,{field: "Precio", 							title: "UI_PRECIO", 					width: "80px",	groupable: true }
			,{field: "Tasa", 							title: "UI_TASA", 						width: "80px",	groupable: true }			
			,{field: "TotalCobros", 					title: "UI_TOTAL_COBROS", 				width: "80px",	groupable: true }
			,{field: "MontoLiquidacion", 				title: "UI_TOTAL_MONTO", 				width: "80px",	groupable: true }						
			,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 				width: "300px", groupable: true, attributes:{style:"white-space:nowrap"} }		
		];
	}; 

	return obj;
}]);	

