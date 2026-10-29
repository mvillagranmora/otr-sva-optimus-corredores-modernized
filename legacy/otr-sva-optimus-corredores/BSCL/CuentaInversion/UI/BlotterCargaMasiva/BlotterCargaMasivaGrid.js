angular.module('ModuloCuentaInversion').factory('bcsBlotterCargaMasivaGrid',['bcsComunes',
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
			//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3338",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						IdCargaMasiva				: { type: "number"},
						FechaCarga					: { type: "sysdate"},
						FechaProceso				: { type: "sysdate"},
						CodigoUsuario				: { type: "string"},
						CantidadRegistros			: { type: "number"},
						MontoTotal					: { type: "number"},
						NombreArchivo				: { type: "string"},
						NombreEstadoCargaMasiva		: { type: "string"},
						IdMoneda					: { type: "string"}, 
						IdConcepto					: { type: "string"},
						NombreConcepto				: { type: "string"},
						CodigoTipoCargaMasiva		: { type: "string"}
					}
				}
			}
		}
	});

	
	obj.columnasCargaMasiva = function() {
		return  [
		     {field: "IdCargaMasiva", 				title: "UI_FOLIO_CARGA",        width: "40px"}
		    ,{field: "FechaCarga",				    title: "UI_FECHA_CARGA", 		width: "65px",	groupable: true }
			,{field: "CantidadRegistros", 			title: "UI_CANTIDAD_REGISTROS", width: "80px"}
			,{field: "MontoTotal", 			        title: "UI_MONTO_TOTAL",        width: "80px",  template: kendo.template("#if (IdMoneda !== '' && IdMoneda !== 'MULTIPLES') {##=(util.bcs_formato(MontoTotal,'GEN','Monto',IdMoneda,'','')).valor##} else {##=util.formateaNumero(MontoTotal,0)##}#")}
			,{field: "CodigoUsuario", 				title: "UI_USUARIO", 			width: "75px",  groupable: true }
			
			//Datos que se obtiene después de procesar la CM	
			,{field: "FechaProceso", 				title: "UI_FECHA_PROCESO", 		width: "65px",	groupable: true }
			,{field: "IdMoneda", 					title: "UI_MONEDA", 			width: "60px",  groupable: true}
			,{field: "NombreConcepto", 				title: "UI_CONCEPTO", 			width: "120px", groupable: true, attributes:{style:"white-space:nowrap"}}
			,{field: "NombreEstadoCargaMasiva", 	title: "UI_ESTADO", 			width: "70px",  groupable: true }
			,{field: "NombreArchivo", 	            title: "UI_NOMBRE_ARCHIVO",  groupable: true,   attributes:{style:"white-space:nowrap"} }
			
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
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3341",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdCargaMasivaDetalle				: { type: "number"},
					    IdTipoMovimiento					: { type: "string"},
						DocumentoIdentidad					: { type: "string"},						
						IdPortafolio						: { type: "number"},												
						Monto								: { type: "number"},						
						NombreEstadoCargaMasivaDetalle 		: { type: "string"},						
						ObservacionValidacion 	        	: { type: "string"},						
						IdMoneda					 		: { type: "string"},
						CodigoTipoCargaMasiva				: { type: "string"},
						
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
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3341",dataType : "json"}},
				schema : {
					model : {
						fields : {
							IdCargaMasivaDetalle				: { type: "number"},
						    IdTipoMovimiento					: { type: "string"},
						    NombreSocioDeNegocio				: { type: "string"},
							DocumentoIdentidad					: { type: "string"},						
							IdPortafolio						: { type: "number"},												
							Monto								: { type: "number"},						
							NombreEstadoCargaMasivaDetalle 		: { type: "string"},						
							ObservacionValidacion 	        	: { type: "string"},
							IdConcepto					 		: { type: "string"},
							IdMoneda					 		: { type: "string"},
							CodigoTipoCargaMasiva				: { type: "string"},					
						}
					}
				}
			}
		};

	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
			 {field: "IdTipoMovimiento", 				title: "UI_TIPO_MOVIMIENTO", 		width: "85px",	groupable: true }
			,{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",	width: "90px",	groupable: true }
			,{field: "NombreSocioDeNegocio",			title: "UI_NOMBRE_SOCIO",	        width: "90px",	groupable: true }
			,{field: "IdConcepto",				        title: "UI_CONCEPTO",           	width: "90px",	groupable: true }
			,{field: "Monto", 			        		title: "UI_MONTO",        			width: "85px",  template: kendo.template("#if (IdMoneda !== '') {##=(util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor##} else {##=util.formateaNumero(Monto,0)##}#")}			
			,{field: "IdMoneda", 		 				title: "UI_MONEDA", 				width: "85px"}
		    ,{field: "NombreEstadoCargaMasivaDetalle", 	title: "UI_ESTADO", 				width: "85px",  groupable: true }
		];
	}; 

	return obj;
}]);	

