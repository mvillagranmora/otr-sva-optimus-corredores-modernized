 angular.module('ModuloContabilidad').factory('bcsMantenedorEventosGrid',function($translate) {

	var obj = {};
	var filters = {};
	//['bcsComunes',function(bcsComunes)
	//paso 1    

	ModeloConsultarNotificaciones= function() {
		return {
			model: {
				fields : {

						 IdEmpresa						: {type: "number"}
						,IdNotificacion 				: {type: "number"}
						,IdConfiguracionEvento			: {type: "number"}
						,FechaCreacion 					: {type: "sysdate"}
						,FormaDespacho					: {type: "code"}
						,TiempoDespacho					: {type: "code"}
						,FechaDespachoOrginal			: {type: "sysdatetime"}
						,FechaDespachoRecordatorio		: {type: "sysdatetime"}
						,Enviado						: {type: "code"}
						,FechaEnviado					: {type: "sysdatetime"}
						,IdUsuarioEnviado				: {type: "number"}
						,ReEnviar						: {type: "code"}
						,ReEnviado						: {type: "code"}
						,FechaReEnviado					: {type: "sysdatetime"}
						,IdUsuarioReEnviado				: {type: "number"}
						,ConfirmacionDespacho			: {type: "code"}
						,TokenConfirmacion				: {type: "string"}
						,Confirmado						: {type: "code"}
						,FechaConfirmado				: {type: "sysdate"}
						,AccionConfirmado				: {type: "code"}
						,IdUsuarioConfimado				: {type: "number"}
						,FechaVencimiento				: {type: "sysdate"}
						,FechaRecordatorio				: {type: "sysdate"}
						,Observacion					: {type: "code"}
						,Descartado						: {type: "code"}
						,IdUsuarioDescarte				: {type: "number"}
						,FechaDescarte					: {type: "sysdatetime"}
						,Enviar							: {type: "code"}
						,DireccionEvento				: {type: "string"}
						,Leida							: {type: "string"}
						,IdUsuario						: {type: "number"}
				}
			}
		};
	};
	

	
	
	
	ModeloConsultarNotificacionesDetalle= function() {
		return {
			model: {
				fields : {

 						 IdNotificacion 				: {type: "number"}
						,IdNotificacionDetalle			: {type: "number"}
						,IdEvento 						: {type: "number"}
						,Despachado						: {type: "code"}
						,IdEstado						: {type: "code"}
						,CodigoTipoEvento				: {type: "string"}
						,Descripcion					: {type: "string"}
				}
			}
		};
	}; 
	
	ModeloTipoEventos= function() {
		return {
			model: {
				fields : {

						 CoditoTipoEvento					: {type: "string"}
						,DireccionEvento 					: {type: "string"}
						,Descripcion						: {type: "string"}
						 
					
				}
			}
		};
	}; 
	
	ModeloConfiguracionEvento= function() {
		return {
			model: {
				fields : {					
					IdConfiguracionEvento 		: {type: "number"}
			 		,Descripcion				: {type: "string"}
					,IdUsuario					: {type: "number"} 
					,FechaCreacion				: {type: "sysdate"}
					,IdEmpresa					: {type: "number"} 
					,IdEstado					: {type: "string"}
					,IdEstadoUI					: {type: "string"}
					,NombreFormaDespachoUI		: {type: "string"}
					,NombreTiempoDespachoUI		: {type: "string"}
					,PermitePersonalizar		: {type: "string"} 
					,FormaDespacho				: {type: "string"} 
					,TiempoDespacho				: {type: "string"} 
					,ConfirmacionDespacho		: {type: "string"} 
					,ConfirmacionDespachoUI		: {type: "string"} 
					,FechaVencimiento			: {type: "sysdate"} 
					,FechaRecordatorio			: {type: "sysdate"} 
					,IdPlantilla				: {type: "number"} 
					,IdPlantillaRecordatorio	: {type: "number"}
					,CodigoTipoEvento			: {type: "string"}
					
				}
			} 
		};
	};
	
	obj.ModeloConsultarCentralizaciones= function() {
		return {
			model: {
				fields : {
		
					 NombreTipoContralizacion	: {type: "string"}
					,FechaProceso				: {type: "sysdate"}
					,HoraProceso				: {type: "systime"}
					,FechaContable				: {type: "sysdate"}
					,CodigoUsuario				: {type: "string"}
					,Estado						: {type: "string"}
					,NombreEstado				: {type: "string"}
					,NemoEmpresa				: {type: "string"} 
					,Glosa						: {type: "string"} 
					,IdTipoCentralizacion		: {type: "number"} 
					,IdCentralizacion			: {type: "number"}
					
				}
			} 
		};
	};
	
	obj.ModeloConsultarCentralizacionesTipo= function() {
		return {
			model: {
				fields : {
		
					 NombreTipo					: {type: "string"}
					,NemoTipo					: {type: "string"}
					,NombreEmpresa				: {type: "string"}
					,NemoEmpresa				: {type: "string"}
					,Estado						: {type: "string"}
					,NombreEstado 				: {type: "string"}
					,IdCentralizacionTipo		: {type: "number"} 
					,Check						: {type: "number"}	
					,RequiereFechaConsulta      : {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarMovimientosCentralizacion= function() { 
		return {
			model: {
				fields : {
		
					 IdMovimiento			: {type: "number"}
					,IdTipoMovimiento		: {type: "string"}
					,IdEmpresa				: {type: "number"}
					,Numero					: {type: "number"}
					,Linea					: {type: "number"}
					,FechaMov				: {type: "sysdate"} 
					,DocumentoIdentidad		: {type: "string"} 
					,Cuenta					: {type: "number"}
					,CodigoPortafolio       : {type: "string"}
					,Ejercicio				: {type: "number"} 
					,Periodo				: {type: "string"}
					,Estado					: {type: "string"}
					,NombreEstado			: {type: "string"} 
					,Nombre					: {type: "string"} 
					,FechaContable			: {type: "sysdate"}
					,IdMoneda				: {type: "string"}
					,Monto					: {type: "number",  excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,CompraVenta			: {type: "string"}
					,Concepto				: {type: "string"}
					,CondicionLiquidacion	: {type: "string"}
					,MonedaLiquidacion		: {type: "string"}
					,Operacion				: {type: "string"}
					,Producto				: {type: "string"}
					,Relacion				: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarMovimientosDatos= function() {
		return {
			model: {
				fields : {
					
					 IdMovimiento			: {type: "number"}
					,IdTipoDatoEvento		: {type: "string"}
					,ValorVarChar			: {type: "string"}
					,ValorDecimal			: {type: "number"}
					,Estado					: {type: "string"}
					,NombreEstado			: {type: "string"}
					,Descripcion			: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarMovimientosFiltros= function() {
		return {
			model: {
				fields : {
					
					IdTipoDatoEvento		: {type: "string"}
					,Valor					: {type: "string"}
					,TipoDato				: {type: "string"}
					,DatosPreDefinidos		: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarTablaMovimientosFiltros= function() {
		return {
			model: {
				fields : {
					
					IdTipoDatoEvento		: {type: "string"}
					,TipoDato				: {type: "string"}
					,ValorVarchar			: {type: "string"}
					,ValorDecimal			: {type: "decimal"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarEventos= function() {
		return {
			model: {
				fields : {
					
					 IdMovimiento						: {type: "number"}
					,IdEvento							: {type: "number"}
					,NombreEvento						: {type: "string"}
					,IdMoneda							: {type: "string"} 									
					,Monto								: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,CodigoMoneda						: {type: "string"}
					,FechaProceso						: {type: "sysdate"}
					,HoraProceso						: {type: "systime"}
					,EstadoEvento						: {type: "string"}
					,NombreEstadoEvento					: {type: "string"}
					,Contabilizado						: {type: "string"}
					,NumeroMovimiento					: {type: "number"}
					,FechaMovimiento					: {type: "sysdate"}
					,RutMovimiento						: {type: "string"}
					,CodigoUsuario						: {type: "string"}
					,IdPreComprobante					: {type: "decimal"}
					,IdPreComprobanteAsiento			: {type: "decimal"}
					,IdTipoEvento						: {type: "number"}
					,IdTipoMovimiento					: {type: "string"}
					,IdTipoDatoEvento					: {type: "string"}
					,NombreMovimiento					: {type: "string"}
					,CuentaMovimiento					: {type: "decimal"}
					,EstadoMovimiento					: {type: "string"}
					,Operador							: {type: "string"}
					,EstadoDetallePreComprobante		: {type: "string"}
					,EstadoPreComprobante				: {type: "string"}
					,NombreCentralizacion				: {type: "string"}
					,MontoDebe							: {type: "number"}
					,MontoHaber							: {type: "number"}
					,Linea								: {type: "number"}
					,IdCuentaContable					: {type: "string"}
					,NombreCuenta						: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarComprobantesFecha= function() {
		return {
			model: {
				fields : {
					
					IdPreComprobante					: {type: "number"}
					,NombreEmpresa						: {type: "string"}
					,Ejercicio							: {type: "number"}
					,FechaContable						: {type: "sysdate"}
					,FechaProceso						: {type: "sysdate"}
					,HoraProceso						: {type: "systime"}
					,FolioContabilidad					: {type: "number"}
					,CantidadAsientos					: {type: "number"}
					,Estado								: {type: "string"}
					,NombreEstado						: {type: "string"}
					,Glosa								: {type: "string"}
					,IdReversa							: {type: "number"}
					,Tipo								: {type: "string"}
					,CodigoUsuario						: {type: "string"}
					,IdTipoContabilidad					: {type: "number"}
					,NombreTipoContabilidad				: {type: "string"}
					,IdEmpresa							: {type: "number"}
//					,Debe								: {type: "number" , excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP','','').excelFormat;}}
//					,Haber								: {type: "number" , excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP','','').excelFormat;}}
					,Debe								: {type: "number" , excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,Haber								: {type: "number" , excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,IdPrograma							: {type: "number"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarListaComprobantes = function() {
		return {
			model: {
				fields : {
					
					Check								: {type: "number"}
					,IdTipoContabilidad					: {type: "number"}
					,Nombre								: {type: "string"}
					,Nemo								: {type: "string"}
					,Tipo								: {type: "string"}
					,Reversa							: {type: "string"}
					,Agrupa								: {type: "string"}
					,Condicionado						: {type: "string"}
					,TipoGeneracion						: {type: "string"}
					,Formato							: {type: "string"}
					,Estado								: {type: "string"}
					,NombreEstado						: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarAsientos = function() {
		return {
			model: {
				fields : {
					
					Glosa								: {type: "string"}
					,IdCuentaContable					: {type: "string"}
					,NombreCuenta						: {type: "string"}
					,Ejercicio							: {type: "number"}
					,FechaContable						: {type: "sysdate"}
//					,MontoDebe							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP','','').excelFormat;}}
//					,MontoHaber							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP','','').excelFormat;}}
					,MontoDebe							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,MontoHaber							: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,Estado								: {type: "string"}
					,NombreEstado						: {type: "string"}
					,IdPreComprobanteAsiento			: {type: "number"}
					,IdPreComprobante					: {type: "number"}
					,IdEmpresa							: {type: "number"}
					,Linea								: {type: "number"}
					,IdTipoContabilidad					: {type: "number"}
					,IdDefinicionAsiento				: {type: "number"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarEventosAsientos = function() {
		return {
			model: {
				fields : {
					
					IdPreComprobanteAsiento				: {type: "number"}
					,IdEvento							: {type: "number"}
					,IdTipoEvento						: {type: "string"}
					,Nombre								: {type: "string"}
					,Operador							: {type: "string"}
					,Monto								: {type: "number"}
					,IdMovimiento						: {type: "number"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarGlosa= function() {
		return {
			model: {
				fields : {					
					 GlosaCampo							: {type: "string"}
					,NombreCampo						: {type: "string"}
					,Orden								: {type: "number"}
					,Origen								: {type: "string"}
					,IdDefinicionAsiento				: {type: "number"}
					,IdTipoContabilidad					: {type: "number"}
					,TipoDato							: {type: "string"}
				}
			} 
		};
	};
	

	
	obj.ModeloConsultarEventosAsiento = function() {
		return {
			model: {
				fields : {
					
					IdTipoEvento						: {type: "string"}
					,IdDefinicionAsiento				: {type: "number"}
					,Operador							: {type: "string"}
					,Estado								: {type: "string"}
					,Nombre								: {type: "string"}
				}
			} 
		};
	};
	
	
	obj.ModeloConsultarConfiguracionComprobante = function() {
		return {
			model: {
				fields : {
					
					IdCondicion							: {type: "number"}
					,IdDefinicionAsiento				: {type: "number"}
					,IdTipoContabilidad					: {type: "number"}
					,ConectorTextual					: {type: "string"}
					,ParaOrdenar						: {type: "string"}
					,Conector							: {type: "string"}
					,IdTipoDatoEvento					: {type: "string"}
					,Condicion							: {type: "string"}
					,ValorVarchar						: {type: "string"}
					,ValorDecimal						: {type: "number"}
					,TipoCondicion						: {type: "string"}
					,Estado								: {type: "string"}
					,NombreEstado						: {type: "string"}
					,Descripcion						: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarDetallesAsientos = function() {
		return {
			model: {
				fields : {
					
					IdPreComprobanteAsiento		: {type: "number"}
					,IdPreComprobante			: {type: "number"}
					,IdEmpresa					: {type: "number"}
					,Ejercicio					: {type: "number"}
					,FechaContable				: {type: "sysdate"}
					,IdCuentaContable			: {type: "string"}
					,NombreCuenta				: {type: "string"}
					,Linea						: {type: "number"}
					,Glosa						: {type: "string"}
					,Operador					: {type: "string"}
//					,MontoDebe					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP','','').excelFormat;}}
//					,MontoHaber					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto','CLP','','').excelFormat;}}
					,MontoDebe					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,MontoHaber					: {type: "number", excelFormat:function (item){return util.bcs_formato(0,'GEN','Monto',item.IdMoneda,'','').excelFormat;}}
					,IdTipoEvento				: {type: "number"}
					,Evento						: {type: "string"}
					,Estado						: {type: "string"}
					,IdMovimiento				: {type: "number"}
					,IdTipoMovimiento			: {type: "string"}
					,Numero						: {type: "number"}
					,DocumentoIdentidad			: {type: "string"}
					,Cuenta						: {type: "number"}
					,CodigoPortafolio			: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarAsientosComprobantes = function() {
		return {
			model: {
				fields : {
					
					IdDefinicionAsiento		: {type: "number"}
					,IdTipoContabilidad		: {type: "number"}
					,IdCuentaContable		: {type: "string"}
					,NombreCuenta			: {type: "string"}
					,Ejercicio				: {type: "number"}
					,Orden					: {type: "number"}
					,DebeHaber				: {type: "string"}
					,TipoEvento				: {type: "string"}
					,Condicionado			: {type: "string"}
					,OmiteCondicionCmbte	: {type: "string"}
					,Estado					: {type: "string"}
					,Formato				: {type: "string"}
					,NombreEstado			: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarMovimientosFiltrados = function() {
		return {
			model: {
				fields : {
					
					TipoMovimiento			: {type: "number"}
					,Numero					: {type: "number"}
					,FechaCont				: {type: "sysdate"}
					,FechaMov				: {type: "sysdate"}
					,Rut					: {type: "string"}
					,Cuenta					: {type: "number"}
					,Ejercicio				: {type: "string"}
					,Periodo				: {type: "string"}
					,Estado					: {type: "string"}
					,Nombre					: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarCentralizacionesRequeridas = function() {
		return {
			model: {
				fields : {
					
					IdTipoCentralizacion		: {type: "number"}
					,NombreTipoCentralizacion	: {type: "string"}
					,IdTipoContabilidad			: {type: "string"}
					,Estado						: {type: "string"}
				}
			} 
		};
	};
	
	obj.getSchemaConfiguracionMetadata = function () {
		return {
			model: {
				fields : {
					
					IdMetadataCuentaContable	: {type: "number"}
					,CodigoMetadata				: {type: "string"}
					,DescripcionMetadata		: {type: "string"}
				}
			} 
		};
	}
	
	obj.ModeloPreCuentasFiltros = function () {
		return {
			model: {
				fields : {
					
					IdFiltro	: {type: "number"}
					,CuentaOptimus : {type: "string"}
					,IdTipoDatoEvento : {type: "string"}
					,Condicion : {type: "string"}
					,Valor : {type: "string"}
				}
			} 
		};
	}
	
	obj.ModeloMetadataFiltros = function () {
		return {
			model: {
				fields : {
					
					IdMetadataFiltros	: {type: "number"}
					,IdMetadataCuentaContable : {type: "number"}
					,CuentaOptimus : {type: "string"}
					,IdTipoDatoEvento : {type: "string"}
					,Condicion : {type: "string"}
					,Valor : {type: "string"}
				}
			} 
		};
	}
	
	obj.ModeloValorFiltros = function() {
		return {
			model: {
				fields : {
					
					Valor		: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarEventosDisponibles = function() {
		return {
			model: {
				fields : {
                    Nombre			: {type: "string"}
					,IdTipoEvento	: {type: "string"}
				}
			} 
		};
	};
	
	obj.ModeloConsultarCuentasMetadata = function(){
		return {
			model: {
				fields : {
					IdMetadataCuentaContable : {type: "number"}
					,CuentaOptimus		: {type: "string"}
					,CuentaContabilidad	: {type: "string"}
					,NombreCuenta		: {type: "string"}
					,DetalleCuenta		: {type: "string"}
				}
			} 
		};
	}
	
	obj.datasourceConsultarNotificacionesRecibidas = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdNotificacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('NotificacionRecibidas'),
							schema 				: ModeloConsultarNotificaciones(),
							filter : {
										logic: "or",
										filters : [
									           {field : "DireccionEvento",operator: "eq", value : 'EMPEMP'},  
									           {field : "DireccionEvento",operator: "eq", value : 'SYSEMP'},
									           {field : "DireccionEvento",operator: "eq", value : 'CLIEMP'},
									          ]
									 }
							 
							
				}
		};
	};
	
	obj.datasourceConsultarNotificacionesEnviadas = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdNotificacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('NotificacionEnviadas'),
							schema 				: ModeloConsultarNotificaciones()
							
							 
							
						}
		};
	};
	
 
 
	//paso 3 
	obj.dataSourceEvento = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "CodigoTipoEvento", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('TipoEvento'),
							schema 				: ModeloTipoEventos(),
							/*filtersFn 			: function (row) {
												  var filterDetalle = new Array();
												  filterDetalle.push({ field: "IdConfiguracionEvento", operator: "eq", value: row.IdConfiguracionEvento });
												  return filterDetalle;
												}*/
							
						  }
		};
	};
	// resumen de log de tareas  
	obj.dataSourceConfiguracionEvento = function(){
		return {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "FechaCreacion", dir: "desc" },
							pageSize 			: 50,
							transport 			: transporte('ConfiguracionEventos'),
							schema 				: ModeloConfiguracionEvento()
							
						}
		};
	};
	
	
	obj.getDataSourceConfiguracionMetadata = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "CodigoMetadata", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4230",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConfiguracionMetadata(),
			},
			
		});
	}
	
	obj.getDataSourceMetadataFiltros = function(){
		return new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "IdMetadataFiltros", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4244",
						dataType: "json"
					}
				},
				schema : obj.getSchemaConfiguracionMetadata(),
			},
			
		});
	}
	// paso 4 las columnas 
	//detalle operaciones 
	obj.columnasEvento = function() {  
		return  [
			{ field: "DescripcionTipoEvento"	,title: 'UI_NOMBRE_EVENTO'		, width: "321px", headerAttributes:{style:"text-align: center;"} },
			{ field: "DireccionEvento"			,title: 'UI_DIRECCION_EVENTO'	, width: "321px", headerAttributes:{style:"text-align: center;"} },
			{ field: "IdEstado"			   		,title: 'UI_ESTADO'				, headerAttributes:{style:"text-align: center;"} },
		];
	};
	obj.columnasEventoAsociados = function() {  
		return  [
			{ field: "Descripcion"		,title: 'UI_NOMBRE_EVENTO'		, width: "264px", headerAttributes:{style:"text-align: center;"} },
			{ field: "DireccionEvento"	,title: 'UI_DIRECCION_EVENTO'	, headerAttributes:{style:"text-align: center;"} },  	            
		];
	};
	obj.columnasEventoDetalle = function() {  
		return  [
			{ field: "Llave"	,title: 'UI_LLAVE', width: "50px", headerAttributes:{style:"text-align: center;"} },   	                	
		];
	};      
	
	obj.columnasConfiguracionEvento = function() {
		return  [
			{ field: "Descripcion"					,title: 'UI_NOMBRE_NOTIFICACION'	, width: "117px", headerAttributes:{style:"text-align: center;"}},
			{ field: "PermitePersonalizar"			,title: 'UI_PERSONALIZAR'			, width: "117px", headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreFormaDespachoUI"		,title: 'UI_FORMA_DESPACHO'			, width: "117px", headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreTiempoDespachoUI"		,title: 'UI_TIEMPO_DESPACHO'		, width: "117px", headerAttributes:{style:"text-align: center;"} },
			{ field: "ConfirmacionDespachoUI"		,title: 'UI_CONFIRMACION_DESPACHO'	, width: "117px", headerAttributes:{style:"text-align: center;"} },
			{ field: "FechaVencimiento"				,title: 'UI_FECHA_VENCIMIENTO'		, width: "117px", headerAttributes:{style:"text-align: center;"} },
			{ field: "FechaRecordatorio"			,title: 'UI_FECHA_RECORDATORIO'		, width: "117px", headerAttributes:{style:"text-align: center;"} },
			{ field: "IdEstadoUI"					,title: 'UI_ESTADO'			        , headerAttributes:{style:"text-align: center;"} },       
		];
	};
	
	obj.columnasConsultarNotificaciones = function() {
		return  [
		   { field: "DescripcionConfiguracion"		,title: 'UI_NOTIFICACION'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FormaDespacho"					,title: 'UI_FORMA_DESPACHO'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "TiempoDespacho"				,title: 'UI_TIEMPO_DESPACHO'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaDespachoOrginal"			,title: 'UI_FECHA_DESPACHO_ORIGINAL'		, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaDespachoRecordatorio"		,title: 'UI_FECHA_DESPACHO_RECORDATORIO'	, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Enviar"						,title: 'UI_ENVIAR'							, width: "100px", headerAttributes:{style:"text-align: center;"} },                  
		   { field: "Enviado"						,title: 'UI_ENVIADO'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaEnviado"					,title: 'UI_FECHA_ENVIADO'					, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioEnviado"				,title: 'UI_USUARIO_ENVIADO'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "ReEnviar"						,title: 'UI_REENVIAR'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "ReEnviado"						,title: 'UI_REENVIADO'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaReEnviado"				,title: 'UI_FECHA_REENVIADO'				, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioReEnviado"				,title: 'UI_USUARIO_REENVIADO'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "ConfirmacionDespacho"			,title: 'UI_CONFIRMACION_DESPACHO'			, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "TokenConfirmacion"				,title: 'UI_TOKEN_CONFIRMACION'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Confirmado"					,title: 'UI_CONFIRMACION'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaConfirmado"				,title: 'UI_FECHA_CONFIRMADO'				, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "AccionConfirmada"				,title: 'UI_ACCION_CONFIRMADA'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioConfirma"				,title: 'UI_USUARIO_CONFIRMA'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaVencimiento"				,title: 'UI_FECHA_VENCIMIENTO'				, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaRecordatorio"				,title: 'UI_FECHA_RECORDATORIO'				, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Observacion"					,title: 'UI_OBSERVACION'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Descartado"					,title: 'UI_DESCARTADO'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioDescartado"				,title: 'UI_USUARIO_DESCARTE'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaDescarte"					,title: 'UI_FECHA_DESCARTE'					, headerAttributes:{style:"text-align: center;"}},
	            
		];
	};
	
	obj.columnasConsultarNotificacionesRecibidas = function() {
		return  [     
		   { field: "DescripcionConfiguracion"		,title: 'UI_NOTIFICACION'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FormaDespacho"					,title: 'UI_FORMA_DESPACHO'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "TiempoDespacho"				,title: 'UI_TIEMPO_DESPACHO'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaDespachoOrginal"			,title: 'UI_FECHA_DESPACHO_ORIGINAL'		, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaDespachoRecordatorio"		,title: 'UI_FECHA_DESPACHO_RECORDATORIO'	, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Enviar"						,title: 'UI_ENVIAR'							, width: "100px", headerAttributes:{style:"text-align: center;"} },                  
		   { field: "Enviado"						,title: 'UI_ENVIADO'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaEnviado"					,title: 'UI_FECHA_ENVIADO'					, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioEnviado"				,title: 'UI_USUARIO_ENVIADO'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "ReEnviar"						,title: 'UI_REENVIAR'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "ReEnviado"						,title: 'UI_REENVIADO'						, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaReEnviado"				,title: 'UI_FECHA_REENVIADO'				, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioReEnviado"				,title: 'UI_USUARIO_REENVIADO'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "ConfirmacionDespacho"			,title: 'UI_CONFIRMACION_DESPACHO'			, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "TokenConfirmacion"				,title: 'UI_TOKEN_CONFIRMACION'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Confirmado"					,title: 'UI_CONFIRMACION'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "FechaConfirmado"				,title: 'UI_FECHA_CONFIRMADO'				, width: "145px", headerAttributes:{style:"text-align: center;"} },
		   { field: "AccionConfirmada"				,title: 'UI_ACCION_CONFIRMADA'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "UsuarioConfirma"				,title: 'UI_USUARIO_CONFIRMA'				, width: "100px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Observacion"					,title: 'UI_OBSERVACION'					, width: "100px", headerAttributes:{style:"text-align: center;"} },
		];
	};
	
	obj.columnasConsultarNotificacionesDetalle = function() {
		return  [            
		   { field: "IdNotificacion"				,title: 'UI_NOTIFICACION'	, width: "307px", headerAttributes:{style:"text-align: center;"}},
		   { field: "Descripcion"					,title: 'UI_EVENTO'			, width: "307px", headerAttributes:{style:"text-align: center;"} },
		   { field: "Despachado"					,title: 'UI_DESPACHADO'		, headerAttributes:{style:"text-align: center;"} },
		];
	};
		
	obj.columnasTiposComprobantes = function() {
		return  [    	                   
			{ field: "Nombre"					,title: 'UI_NOMBRE'			, width: "60px",  headerAttributes:{style:"text-align: center;"}},
			{ field: "Estado"					,title: 'UI_ESTADO'			, width: "100px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Tipo"						,title: 'UI_TIPO'			, width: "100px",  headerAttributes:{style:"text-align: center;"} },
		]
	};
	
	obj.columnasCentralizacionesRequeridas = function() {
		return  [    
			{ field: "NombreTipoCentralizacion"	,title: 'UI_NOMBRE'			, width: "120px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Estado"					,title: 'UI_ESTADO'			, width: "60px",   headerAttributes:{style:"text-align: center;"} },  
		]
	};
	
	
	obj.columnasEventosDisponibles = function() {
		return  [    		         	 
			{ field: "Nombre"				,title: 'UI_TIPO_EVENTO' 		, width: "120px",  headerAttributes:{style:"text-align: center;"}},	                     
		]
	};
	
	obj.columnasMovimientosFiltrados = function() {
		return  [    
	                   
	                    { field: ""			,title: 'Tipo Movimiento'			, width: "60px",  headerAttributes:{style:"text-align: center;"}},
	                    { field: ""			,title: 'UI_NUMERO'					, width: "60px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'Fecha Cont'				, width: "60px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'Fecha Mov'					, width: "60px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'Rut'						, width: "60px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'Cuenta'					, width: "40px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'Ejercicio'					, width: "40px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'Periodo'					, width: "40px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'UI_ESTADO'					, width: "40px",  headerAttributes:{style:"text-align: center;"} },
	                    { field: ""			,title: 'UI_NOMBRE'					, width: "100px",  headerAttributes:{style:"text-align: center;"} },	
		]
	};
	
	obj.columnasGlosa = function(){
		return [						 
			{ field: "NombreCampo"				,title: 'UI_DATO'	, width: "120px",  headerAttributes:{style:"text-align: center;"}},
						 
		]		
	};
		
	obj.columnasGlosaAsociadas = function(){
		return [						 
			{ field: "NombreCampo"				,title: 'UI_DATO'	, width: "120px", headerAttributes:{style:"text-align: center;"}},
			{ field: "Orden"					,title: 'UI_ORDEN'	, width: "60px",  headerAttributes:{style:"text-align: center;"} },
		]		
	};
	
	
/////////////////////////////////////////////////////CONFIGURACION COMPROBANTES (INICIO)
	
	//--------------------------- TAB TIPOS DE COMPROBANTES
	obj.columnasAsientosComprobantes = function() {
		return  [    
			{ field: "IdCuentaContable"			,title: 'UI_CUENTA_CONTABLE'	, width: "70px", headerAttributes:{style:"text-align: center;"} },	                    
			{ field: "NombreCuenta"				,title: 'UI_NOMBRE_CUENTA'		, width: "190px", headerAttributes:{style:"text-align: center;"} },
			{ field: "Orden"					,title: 'UI_ORDEN'				, width: "30px", headerAttributes:{style:"text-align: center;"} },
			{ field: "DebeHaber"				,title: 'UI_DEBE_HABER'			, width: "30px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Ejercicio"				,title: 'UI_EJERCICIO'			, width: "50px", headerAttributes:{style:"text-align: center;"} },
		]
	};
	
	//--------------------------- TAB CONFIGURAR ASIENTOS
	obj.columnasDatosAsientos = function() {
		return  [    
			{ field: "IdTipoDatoEvento"		,title: 'UI_TIPO_DATO'			, width: "100px",  headerAttributes:{style:"text-align: center;"}},
		];
	};

	obj.columnasAsientosConf = function() {
		return  [    
			{ field: "IdDefinicionAsiento"		,title: 'UI_ID'					, width: "60px",  headerAttributes:{style:"text-align: center;"}},
			{ field: "IdCuentaContable"			,title: 'UI_CUENTA_CONTABLE'	, width: "90px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreCuenta"				,title: 'UI_NOMBRE_CUENTA'		, width: "200px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Ejercicio"				,title: 'UI_EJERCICIO'			, width: "60px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Orden"					,title: 'UI_ORDEN'				, width: "40px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "DebeHaber"				,title: 'UI_DEBE_HABER'			, width: "40px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "TipoEvento"				,title: 'UI_TIPO_EVENTO'		, width: "40px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Condicionado"				,title: 'UI_CONDICIONADO'		, width: "40px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstado"				,title: 'UI_ESTADO'				, width: "40px",  headerAttributes:{style:"text-align: center;"} },
		]
	};
/////////////////////////////////////////////////////CONFIGURACION COMPROBANTES (FIN)
	

/////////////////////////////////////////////////////Gestion Interfaz Contable (INICIO)
 
 	//--------------------------- TAB CENTRALIZACIONES
	obj.columnasCentralizacionesProcesadas = function() {
		return  [	                   
			{ field: "NombreTipoContralizacion"	,title: 'UI_CENTRALIZACION'			, width: "250px", headerAttributes:{style:"text-align: center;"}},
			//{ field: "FechaContable"			,title: 'UI_FECHA_CONTABLE'			, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "FechaProceso"				,title: 'UI_FECHA_PROCESO'			, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "HoraProceso"				,title: 'UI_HORA_PROCESO'			, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstado"				,title: 'UI_ESTADO'					, width: "80px",  headerAttributes:{style:"text-align: center;"} }
	   ];
	};

	obj.columnasListaCentralizaciones = function() {
		return  [  
			{ field: "NombreTipo"				,title: 'UI_CENTRALIZACION'			, width: "200px", headerAttributes:{style:"text-align: center;"}},
			{ field: "NombreEstado"				,title: 'UI_ESTADO'					, width: "80px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NemoTipo"					,title: 'UI_NEMO'					, width: "100px", headerAttributes:{style:"text-align: center;"} },          
		];
	};
	//--------------------------- TAB MOVIMIENTOS
	obj.columnasMovimientosCentralizacion = function() {
		return  [
			{ field: "IdTipoMovimiento"			,title: 'UI_TIPO_MOVIMIENTO'		, width: "60px",  headerAttributes:{style:"text-align: center;"}},
			{ field: "Numero"					,title: 'UI_FOLIO'					, width: "70px",  headerAttributes:{style:"text-align: center;"},	template: kendo.template("#if ( Numero == 0  || Numero == null)  {##} else {##=Numero##}#")},
			{ field: "Monto"					,title: 'UI_MONTO'					, width: "80px",  headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"},
			{ field: "FechaContable"			,title: 'UI_FECHA_CONTABLE'			, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "FechaMov"					,title: 'UI_FECHA_MOVIMIENTO'		, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "DocumentoIdentidad"		,title: 'UI_DOCUMENTO_IDENTIDAD'	, width: "80px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "CodigoPortafolio"			,title: 'UI_PORTAFOLIO'				, width: "80px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstado"				,title: 'UI_ESTADO'					, width: "40px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Ejercicio"				,title: 'UI_EJERCICIO'				, width: "60px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Nombre"					,title: 'UI_NOMBRE'					, width: "230px", headerAttributes:{style:"text-align: center;"} },
			{ field: "Periodo"					,title: 'UI_PERIODO'				, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "IdMovimiento"				,title: 'UI_SECUENCIA_MOVIMIENTO'	, width: "40px",  headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "CompraVenta"				,title: 'UI_COMPRA_VENTA'			, width: "90px",  headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "Concepto"					,title: 'UI_CONCEPTO'				, width: "70px",  headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "CondicionLiquidacion"		,title: 'UI_CONDICION_LIQUIDACION'	, width: "120px", headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "MonedaLiquidacion"		,title: 'UI_MONEDA_LIQUIDACION'		, width: "100px", headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "Operacion"				,title: 'UI_OPERACION'				, width: "70px",  headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "Producto"					,title: 'UI_PRODUCTO'				, width: "70px",  headerAttributes:{style:"text-align: center;"},	hidden: true},
			{ field: "Relacion"					,title: 'UI_RELACION'				, width: "70px",  headerAttributes:{style:"text-align: center;"},	hidden: true}
		];
		
	};
	
	obj.columnasMovimientosFiltros = function() {
		return  [    
			{ field: "IdTipoDatoEvento"			,title: 'UI_TIPO_DATO'				, width: "100px", headerAttributes:{style:"text-align: center;"}},
			{ field: "Valor"					,title: 'UI_VALOR'					, width: "80px",  headerAttributes:{style:"text-align: center;", editable: true} },
		];
	};
	
	obj.columnasMovimientosDatos = function() {
		return  [    	                   
			{ field: "IdTipoDatoEvento"			,title: 'UI_TIPO_DATO'				, width: "100px", headerAttributes:{style:"text-align: center;"}},
			{ field: "Descripcion"				,title: 'UI_VALOR'					, width: "80px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstado"					,title: 'UI_ESTADO'					, width: "80px",  headerAttributes:{style:"text-align: center;"} },
		];
	};	
	//--------------------------- TAB EVENTOS
	obj.columnasEventos = function() {
		return  [	                   
			{ field: "NumeroMovimiento"			,title: 'UI_FOLIO'				, width: "40px",  headerAttributes:{style:"text-align: center;"}},
			{ field: "NombreEvento"				,title: 'UI_NOMBRE_EVENTO'			, width: "200px", headerAttributes:{style:"text-align: center;"} },
			{ field: "Monto"					,title: 'UI_MONTO'					, width: "80px",  headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "CodigoMoneda"				,title: 'UI_MONEDA'					, width: "45px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstadoEvento"		,title: 'UI_ESTADO'					, width: "35px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "FechaProceso"				,title: 'UI_FECHA_PROCESO'			, width: "75px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "HoraProceso"				,title: 'UI_HORA_PROCESO'			, width: "75px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "FechaMovimiento"			,title: 'UI_FECHA_MOVIMIENTO'		, width: "75px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "IdMovimiento"				,title: 'UI_SECUENCIA_MOVIMIENTO'	, width: "40px",  headerAttributes:{style:"text-align: center;"}, hidden: true}
		];       
	};
	//--------------------------- TAB  PRE COMPROBANTES
	obj.columnasComprobantesFecha = function() {
		return  [    	                   
			{ field: "NombreTipoContabilidad"	,title: 'UI_COMPROBANTE'			, width: "250px",  headerAttributes:{style:"text-align: center;"}},
			{ field: "Debe"						,title: 'UI_DEBE'					, width: "90px",   headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Debe,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "Haber"					,title: 'UI_HABER'					, width: "90px",   headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(Haber,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "FechaContable"			,title: 'UI_FECHA_CONTABLE'			, width: "70px",   headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstado"				,title: 'UI_ESTADO'					, width: "40px",   headerAttributes:{style:"text-align: center;"}, template: kendo.template("#if (Estado === 'E') {#<a class='verTareaProgramada'><span>#=NombreEstado#</span></a> #} else{##=NombreEstado##}#")}
		];       
	};
	
	obj.columnasListaComprobantes = function() {
		return  [    
			{ field: "Nombre"					,title: 'UI_COMPROBANTE'			, width: "240px",  headerAttributes:{style:"text-align: center;"}},	                    
			{ field: "NombreEstado"				,title: 'UI_ESTADO'					, width: "50px",  headerAttributes:{style:"text-align: center;"} },
			//{ field: "Condicionado"				,title: 'UI_CONDICIONADO'			, width: "50px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Reversa"					,title: 'UI_REVERSA'				, width: "50px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Agrupa"					,title: 'UI_AGRUPA'					, width: "50px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "TipoGeneracion"			,title: 'UI_TIPO_GENERACION'		, width: "50px",  headerAttributes:{style:"text-align: center;"} },	                    
		]; 
	};
	//--------------------------- TAB ASIENTOS
	obj.columnasAsientos = function() {
		return  [    
			{ field: "Linea"					,title: 'UI_LINEA'				, width: "40px"},
			{ field: "IdCuentaContable"			,title: 'UI_CUENTA_CONTABLE'	, width: "100px"},
			{ field: "NombreCuenta"				,title: 'UI_NOMBRE_CUENTA'		, width: "200px"},
			{ field: "MontoDebe"				,title: 'UI_DEBE'				, width: "80px",  template : " <label> #:  (util.bcs_formato(MontoDebe,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "MontoHaber"				,title: 'UI_HABER'				, width: "80px",  template : " <label> #:  (util.bcs_formato(MontoHaber,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "Glosa"					,title: 'UI_GLOSA'				, width: "250px"},
			{ field: "NombreEstado"				,title: 'UI_ESTADO'				, width: "35px"}
		]; 
	};
	
	obj.columnasEventosAsientos = function() {
		return  [    
	       	{ field: "Nombre"					,title: 'UI_NOMBRE'		, width: "240px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Operador"					,title: 'UI_OPERADOR'	, width: "60px",  headerAttributes:{style:"text-align: center;"} },	                   
		];
	};
	obj.columnasConfiguracionComprobante = function() {
		return  [    	    
			{ field: "IdTipoDatoEvento"			,title: 'UI_DATO_EVENTO'			, width: "230px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "Condicion"				,title: 'UI_CONDICION'				, width: "60px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "ValorVarchar"				,title: 'UI_VALOR_CHAR'				, width: "60px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "ValorDecimal"				,title: 'UI_VALOR_DECIMAL'			, width: "70px",  headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreEstado"					,title: 'UI_ESTADO'					, width: "40px",  headerAttributes:{style:"text-align: center;"} },
		]; 
	};
	//--------------------------- TAB DETALLE ASIENTOS
	obj.columnasDetallesAsientos = function() {
		return  [    
	                   
			{ field: "Linea"					,title: 'UI_LINEA'					, width: "60px",  headerAttributes:{style:"text-align: center;"}},
			{ field: "IdCuentaContable"			,title: 'UI_CUENTA_CONTABLE'		, width: "100px",	headerAttributes:{style:"text-align: center;"} },
			{ field: "MontoDebe"				,title: 'UI_DEBE'					, width: "100px",	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoDebe,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "MontoHaber"				,title: 'UI_HABER'					, width: "100px",	headerAttributes:{style:"text-align: center;"},	template : " <label> #:  (util.bcs_formato(MontoHaber,'GEN','Monto',IdMoneda,'','')).valor #</label>" },
			{ field: "Operador"					,title: 'UI_OPERADOR'				, width: "50px",	headerAttributes:{style:"text-align: center;"} },
			{ field: "Evento"					,title: 'UI_EVENTO'					, width: "70px",	headerAttributes:{style:"text-align: center;"} },
			{ field: "IdTipoMovimiento"			,title: 'UI_TIPO_MOVIMIENTO'		, width: "70px", 	headerAttributes:{style:"text-align: center;"} },
			{ field: "Numero"					,title: 'UI_FOLIO'					, width: "70px", 	headerAttributes:{style:"text-align: center;"} },
			{ field: "Glosa"					,title: 'UI_GLOSA'					, width: "250px",	headerAttributes:{style:"text-align: center;"} },
			{ field: "DocumentoIdentidad"		,title: 'UI_DOCUMENTO_IDENTIDAD'	, width: "70px", 	headerAttributes:{style:"text-align: center;"} },
			{ field: "CodigoPortafolio"			,title: 'UI_PORTAFOLIO'				, width: "70px", 	headerAttributes:{style:"text-align: center;"} },     
		]
	};
	
/////////////////////////////////////////////////////Gestion Interfaz Contable (FIN)
	
	obj.columnasCuentasMetadata = function() {
		return  [    
			{ field: "CuentaOptimus"			,title: 'UI_NUMERO_CUENTA'		, width: "100px", 	headerAttributes:{style:"text-align: center;"} },           
			{ field: "CuentaContabilidad"		,title: 'UI_CUENTA_CONTABLE'	, width: "100px", 	headerAttributes:{style:"text-align: center;"} },
			{ field: "NombreCuenta"				,title: 'UI_NOMBRE_CUENTA'		, width: "200px", 	headerAttributes:{style:"text-align: center;"} }
		]
	};
	
	obj.columnasPreCuentasFiltros = function(){
		return  [    
			{ field: "CuentaOptimus"			,title: 'UI_NUMERO_CUENTA'		, width: "100px", 	headerAttributes:{style:"text-align: center;"} },           
			{ field: "IdTipoDatoEvento"			,title: 'UI_TIPO_DATO'			, width: "100px", 	headerAttributes:{style:"text-align: center;"} },
			{ field: "Condicion"				,title: 'UI_CONDICION'			, width: "200px", 	headerAttributes:{style:"text-align: center;"} },
			{ field: "Valor"					,title: 'UI_VALOR'				, width: "80px",  	headerAttributes:{style:"text-align: center;"} },
		]
	}


	return obj; 
});
