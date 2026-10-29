angular.module('ModuloTesoreria').factory('bcsBlotterCargaMasivaGrid',['bcsComunes',
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
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1738",dataType : "json"}},
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
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1739",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdCargaMasivaDetalle				: { type: "number"},
					    IdTipoMovimiento					: { type: "string"},
						DocumentoIdentidad					: { type: "string"},						
						IdPortafolio						: { type: "number"},
						CodigoPortafolio					: { type: "string"},
						PropositoPortafolio					: { type: "string"},
						Monto								: { type: "number"},						
						NombreEstadoCargaMasivaDetalle 		: { type: "string"},						
						ObservacionValidacion 	        	: { type: "string"},						
						NombreSocioDeNegocio    			: { type: "string"},
						IdMovimientoComprobante				: { type: "number"},
						IdMoneda					 		: { type: "string"},
						CodigoTipoCargaMasiva				: { type: "string"},
						
						//Datos de CONCILIACION_RECAUDACION_BCP
						BCP_CON_CodigoCuenta				: { type: "number"},
						BCP_CON_CodigoMoneda				: { type: "number"},
						BCP_CON_NumeroCuenta				: { type: "number"},
						BCP_CON_IdentificacionDepositante	: { type: "string"},											
						BCP_CON_FechaTransaccion			: { type: "date"},											
						BCP_CON_NumeroOperacion				: { type: "number"},											
						BCP_CON_ReferenciaOperacion			: { type: "string"},											
						BCP_CON_IndicadorExtorno			: { type: "string"},											
						BCP_CON_NumeroOperacionBD			: { type: "number"},
						
						//DATOS DE ORIGEN ENTIDAD EXTERNA
						Ori_TipoCuenta						: { type: "string"},
						Ori_NumeroCuenta					: { type: "string"},
						Ori_Moneda							: { type: "string"},
						Ori_TipoDocumentoIdentidad			: { type: "string"},
						Ori_DocumentoIdentidad				: { type: "string"},
						
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
				transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1739",dataType : "json"}},
				schema : {
					model : {
						fields : {
							IdCargaMasivaDetalle				: { type: "number"},
						    IdTipoMovimiento					: { type: "string"},
							DocumentoIdentidad					: { type: "string"},
							IdPortafolio						: { type: "number"},
							CodigoPortafolio					: { type: "string"},
							PropositoPortafolio					: { type: "string"},
							Monto								: { type: "number"},
							NombreEstadoCargaMasivaDetalle  	: { type: "string"},
							ObservacionValidacion 	        	: { type: "string"},
							NombreSocioDeNegocio    			: { type: "string"},
							Glosa    							: { type: "string"},
							IdMovimientoComprobante				: { type: "number"},
							IdMoneda					 		: { type: "string"},
							
							//Datos de CONCILIACION_RECAUDACION_BCP
							BCP_CON_CodigoCuenta				: { type: "number"},
							BCP_CON_CodigoMoneda				: { type: "number"},
							BCP_CON_NumeroCuenta				: { type: "number"},
							BCP_CON_IdentificacionDepositante	: { type: "string"},											
							BCP_CON_FechaTransaccion			: { type: "date"},											
							BCP_CON_NumeroOperacion				: { type: "number"},											
							BCP_CON_ReferenciaOperacion			: { type: "string"},											
							BCP_CON_IndicadorExtorno			: { type: "string"},											
							BCP_CON_NumeroOperacionBD			: { type: "number"},
							
							//DATOS DE ORIGEN ENTIDAD EXTERNA
							Ori_TipoCuenta						: { type: "string"},
							Ori_NumeroCuenta					: { type: "string"},
							Ori_Moneda							: { type: "string"},
							Ori_TipoDocumentoIdentidad			: { type: "string"},
							Ori_DocumentoIdentidad				: { type: "string"},
							
						}
					}
				}
			}
		};

	
	obj.columnasCargaMasivaDetalle = function() {
		return  [
			 {field: "IdTipoMovimiento", 				title: "UI_TIPO_MOVIMIENTO", 		width: "85px",	groupable: true }
			,{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",	width: "90px",	groupable: true }
			,{field: "NombreSocioDeNegocio",			title: "UI_SOCIO_DE_NEGOCIO",		width: "170px",	groupable: true, attributes:{style:"white-space:nowrap"} }			
			,{field: "Monto", 			        		title: "UI_MONTO",        			width: "85px",  template: kendo.template("#if (IdMoneda !== '') {##=(util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor##} else {##=util.formateaNumero(Monto,0)##}#")}			
			,{field: "IdMoneda", 		 				title: "UI_MONEDA", 				width: "85px"}
			,{field: "CodigoPortafolio", 				title: "UI_PORTAFOLIO", 			width: "70px",	groupable: true }			
			,{field: "PropositoPortafolio", 			title: "UI_PROPOSITO_PORTAFOLIO", 	width: "100px",	groupable: true, attributes:{style:"white-space:nowrap"} }
			,{field: "Glosa", 							title: "UI_GLOSA", 					width: "100px", groupable: true, attributes:{style:"white-space:nowrap"} }
			
		    ,{title: "UI_DATOS_ARCHIVO_ORIGEN", align: "center", columns : [
						 // DATOS DE ORIGEN DE ENTIDAD EXTERNA (se ocultan y se activan segun tipo de carga)
						 {field: "Ori_TipoCuenta", 		 			 title: "UI_TIPO_CUENTA", 				width: "85px",hidden: true,	groupable: true}
						,{field: "Ori_NumeroCuenta", 		 		 title: "UI_NUMERO_CUENTA", 			width: "85px",hidden: true}
						,{field: "Ori_Moneda", 		 				 title: "UI_MONEDA", 					width: "85px",hidden: true,	groupable: true}
						,{field: "Ori_TipoDocumentoIdentidad", 		 title: "UI_TIPO_DOCUMENTO_IDENTIDAD", 	width: "95px",hidden: true,	groupable: true}
						,{field: "Ori_DocumentoIdentidad", 		 	 title: "UI_DOCUMENTO_IDENTIDAD", 		width: "95px",hidden: true}
						
						//Datos de CONCILIACION_RECAUDACION_BCP(se ocultan y se activan segun tipo de carga)
						,{field: "BCP_CON_CodigoCuenta", 			 title: "UI_CODIGO_CUENTA", 			 width: "80px",hidden: true,	groupable: true}
						,{field: "BCP_CON_CodigoMoneda", 			 title: "UI_CODIGO_MONEDA", 			 width: "80px",hidden: true,	groupable: true}
						,{field: "BCP_CON_NumeroCuenta", 			 title: "UI_NUMERO_CUENTA", 			 width: "80px",hidden: true,	groupable: true}
						,{field: "BCP_CON_IdentificacionDepositante",title: "UI_IDENTIFICACION_DEPOSITANTE", width: "80px",hidden: true,	groupable: true}											
						,{field: "BCP_CON_FechaTransaccion", 		 title: "UI_FECHA_TRANSACCION", 		 width: "80px",hidden: true,}											
						,{field: "BCP_CON_NumeroOperacion", 		 title: "UI_NUMERO_OPERACION", 			 width: "80px",hidden: true,}											
						,{field: "BCP_CON_ReferenciaOperacion", 	 title: "UI_REFERENCIA_OPERACION", 		 width: "80px",hidden: true,}											
						,{field: "BCP_CON_IndicadorExtorno", 		 title: "UI_INDICADOR_EXTORNO", 		 width: "80px",hidden: true,	groupable: true}	
						,{field: "BCP_CON_NumeroOperacionBD", 		 title: "UI_NUMERO_OPERACION_BD", 		 width: "80px",hidden: true,}
			]}
			    
			
		    
			,{field: "IdMovimientoComprobante",			title: "UI_SECUENCIA_MOVIMIENTO",		width: "80px",	groupable: true }
			,{field: "NombreEstadoCargaMasivaDetalle", 	title: "UI_ESTADO", 					width: "85px",  groupable: true }
			,{field: "ObservacionValidacion", 	        title: "UI_VALIDACION", 				width: "200px",groupable: true, attributes:{style:"white-space:nowrap"} }
			
		];
	}; 

	return obj;
}]);	

