angular.module('ModuloNormativa').factory('bcsConfigGridModalDetalles',['bcsComunes',function(bcsComunes)  {
	var obj = {};	

	transporteCalendarioPagos = function() {
		url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290636";
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};
	
	transportePagosPeriodo = function() {
		url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290637";
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};
	
	transporteGarantias = function() {
		url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290638";
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};
	
	transporteTipoActivoGarantia = function() {
		url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290639";
		return {
			read : {
				url : url,
				dataType : "json"
			}
		};
	};

	//DATASOURCE CALENDARIO PAGOS
	modeloCalendarioPagos = function() {
		return {
    		model: {
    			fields: {	
					ID					: {type:"number"},				
					IdDocumento			: {type:"number"},
					VboUsuario			: {type:"string"},
					Observaciones		: {type:"string"},
					FuenteDatos			: {type:"string"},
					KRutCtraparte1		: {type:"string"},
					KIdContrato			: {type:"string"},
					KFechaContrato 		: {type:"date"},
					KIdOpeEstruc		: {type:"string"},
					KS4_XR_NroFlu		: {type:"number"},
					S4_XR_TipoFlujo		: {type:"string"},
					S4_XR_FacTasa		: {type:"number"},
					S4_XR_SpreadTasa	: {type:"number"},
					S4_XR_TasaFija		: {type:"number"},
					S4_XR_FecFixTasa	: {type:"date"},
					S4_XR_FecIni		: {type:"date"},
					S4_XR_FecTer		: {type:"date"},
					S4_XR_MontoNoc		: {type:"number"},
					S4_XR_MontoInt		: {type:"number"},
					S4_X_MonMonto		: {type:"string"},
					UsrUltMod			: {type:"string"},
					FecUltMod			: {type:"date"},
					Estado				: {type:"string"},
					kProducto			: {type:"string"},
					kPeriodoComercial 	: {type:"date"}
    			} 
        	}
		};
	};

	obj.dataSourceCalendarioPagos = function(keyCabecera) {
		return new bcs.DataSource( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.							
							pageSize 			: 25,
							transport 			: transporteCalendarioPagos(),
							filter				: [ { field: "KRutCtraparte1", operator: "eq", value: keyCabecera.KRutCtraparte1},
													{ field: "KIdContrato", operator: "eq", value: keyCabecera.KIdContrato},
													{ field: "KFechaContrato", operator: "eq", value: keyCabecera.KFechaContrato},
													{ field: "KIdOpeEstruc", operator: "eq", value: keyCabecera.KIdOpeEstruc},
													{ field: "kProducto", operator: "eq", value: keyCabecera.kProducto},
													{ field: "kPeriodoComercial", operator: "eq", value: keyCabecera.kPeriodoComercial}
												  ],
							schema 				: modeloCalendarioPagos() 
						  }
		});
		
	}; 

	obj.columnasCalendarioPagos = function() {
		return  [
			 {field: "IdDocumento", 		title: 'Id Documento', 			width: "80px" }
			,{field: "VboUsuario",			title: 'VboUsuario',			width: "80px" }
			,{field: "Observaciones",		title: 'Observaciones',			width: "80px" }
			,{field: "FuenteDatos", 		title: 'Fuente Datos', 			width: "80px" }
			,{field: "KRutCtraparte1", 		title: 'Rut Contraparte', 		width: "100px"}
			,{field: "KIdContrato", 		title: 'Id Contrato', 			width: "70px" }
			,{field: "KFechaContrato", 		title: 'Fecha Contrato', 		width: "80px" }
			,{field: "KIdOpeEstruc", 		title: 'Id Operacion', 			width: "70px" }
			,{field: "KS4_XR_NroFlu",   	title: 'Número de Flujo', 		width: "90px" }
			,{field: "S4_XR_TipoFlujo", 	title: 'Tipo de Flujo', 		width: "80px" }
			,{field: "S4_XR_FacTasa", 		title: 'Factor Tasa', 			width: "80px" }
			,{field: "S4_XR_SpreadTasa",	title: 'Spread Tasa', 			width: "80px" }
			,{field: "S4_XR_TasaFija",  	title: 'Tasa Int. Fija',		width: "80px" }
			,{field: "S4_XR_FecFixTasa",	title: 'Fecha Fixing Tasa', 	width: "100px"}
			,{field: "S4_XR_FecIni",    	title: 'Fecha Inicio', 			width: "100px"}
			,{field: "S4_XR_FecTer",    	title: 'Fecha Término', 		width: "100px"}
			,{field: "S4_XR_MontoNoc",  	title: 'Monto Nocional', 		width: "100px"}
			,{field: "S4_XR_MontoInt",  	title: 'Monto Intercambio', 	width: "100px"}
			,{field: "S4_X_MonMonto",   	title: 'Moneda Monto', 			width: "100px"}
			,{field: "UsrUltMod",       	title: 'Usuario Modifica', 		width: "100px"}
			,{field: "FecUltMod",       	title: 'Fecha Modifica', 		width: "100px"} 
			,{field: "Estado",          	title: 'Estado', 				width: "50px" }
			,{field: "kProducto",       	title: 'Producto', 				width: "80px" }
			,{field: "kPeriodoComercial",	title: 'Periodo Comercial', 	width: "100px"}
		];
	};  
	
	//DATASOURCE PAGOS PERIODO
		modeloPagosPeriodo = function() {
		return {
    		model: {
    			fields: {					
    				ID 					: {type:"number"},
					IdDocumento 		: {type:"number"},
					VboUsuario 			: {type:"string"},
					Observaciones 		: {type:"string"},
					FuenteDatos 		: {type:"string"},
					KRutCtraparte1 		: {type:"string"},
					KIdContrato 		: {type:"string"},
					KFechaContrato 		: {type:"date"},
					KIdOpeEstruc 		: {type:"string"},
					KS5_CorrPago 		: {type:"string"},
					KS5_IdPago 			: {type:"string"},
					S5_FecPago 			: {type:"date"},
					S5_MontoPago 		: {type:"number"},
					S5_TipoPago 		: {type:"string"},
					S5_CodMoneda 		: {type:"string"},
					UsrUltMod 			: {type:"string"},
					FecUltMod 			: {type:"date"},
					Estado	 			: {type:"string"},
					kProducto	 		: {type:"string"},
					kPeriodoComercial   : {type:"date"}
    			} 
        	}
		};
	};

	obj.dataSourcePagosPeriodo = function(keyCabecera) {
		return new bcs.DataSource( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.							
							pageSize 			: 25,
							transport 			: transportePagosPeriodo(),
							filter				: [ { field: "KRutCtraparte1", operator: "eq", value: keyCabecera.KRutCtraparte1},
													{ field: "KIdContrato", operator: "eq", value: keyCabecera.KIdContrato},
													{ field: "KFechaContrato", operator: "eq", value: keyCabecera.KFechaContrato},
													{ field: "KIdOpeEstruc", operator: "eq", value: keyCabecera.KIdOpeEstruc},
													{ field: "kProducto", operator: "eq", value: keyCabecera.kProducto},
													{ field: "kPeriodoComercial", operator: "eq", value: keyCabecera.kPeriodoComercial}
												  ],
							schema 				: modeloPagosPeriodo() 
						  }
		});
		
	}; 

	obj.columnasPagosPeriodo = function() {
		return  [
			 {field: "IdDocumento", 		title: 'Id Documento', 			width: "80px" }
			,{field: "VboUsuario",			title: 'VboUsuario',			width: "80px" }
			,{field: "Observaciones",		title: 'Observaciones',			width: "80px" }
			,{field: "FuenteDatos", 		title: 'Fuente Datos', 			width: "80px" }
			,{field: "KRutCtraparte1", 		title: 'Rut Contraparte', 		width: "100px"}
			,{field: "KIdContrato", 		title: 'Id Contrato', 			width: "70px" }
			,{field: "KFechaContrato", 		title: 'Fecha Contrato', 		width: "80px" }
			,{field: "KIdOpeEstruc", 		title: 'Id Operacion', 			width: "70px" }
			,{field: "KS5_CorrPago",    	title: 'Correlativo Pago',		width: "100px"}
			,{field: "KS5_IdPago", 			title: 'Id Pago', 				width: "70px" }
			,{field: "S5_FecPago", 			title: 'Fecha Pago', 			width: "80px" }
			,{field: "S5_MontoPago", 		title: 'Monto Pago', 			width: "80px", template: " <label> #:  (util.bcs_formato(S5_MontoPago,'GEN','Monto', S5_CodMoneda === null ? 'CLP' : S5_CodMoneda,'','')).valor #</label>" }
			,{field: "S5_TipoPago",     	title: 'Tipo Pago', 			width: "70px" }
			,{field: "S5_CodMoneda",     	title: 'Moneda Pago',			width: "70px" }
			,{field: "UsrUltMod",       	title: 'Usuario Modifica', 		width: "100px"}
			,{field: "FecUltMod",       	title: 'Fecha Modifica', 		width: "100px"}
			,{field: "Estado",          	title: 'Estado', 				width: "50px" }
			,{field: "kProducto",       	title: 'Producto', 				width: "80px" }
			,{field: "kPeriodoComercial",	title: 'Periodo Comercial', 	width: "100px"}
		];
	};  
	

	//DATASOURCE GARANTIAS
		modeloGarantias = function() {
		return {
    		model: {
    			fields: {					
    				ID 					: {type:"number"},
					IdDocumento 		: {type:"number"},
					VboUsuario 			: {type:"string"},
					Observaciones 		: {type:"string"},
					FuenteDatos 		: {type:"string"},
					KRutCtraparte1 		: {type:"string"},
					KIdContrato 		: {type:"string"},
					KFechaContrato 		: {type:"date"},
					KIdOpeEstruc 		: {type:"string"},
					S6_TipoGar 			: {type:"string"},
					S6_MonVal 			: {type:"string"},
					KS6_IdGar 			: {type:"string"},
					S6_MontoUmb 		: {type:"number"},
					S6_ValGtia 			: {type:"number"},
					UsrUltMod 			: {type:"string"},
					FecUltMod 			: {type:"date"},
					Estado 				: {type:"string"},
					kProducto 			: {type:"string"},
					kPeriodoComercial 	: {type:"date"}
    			} 
        	}
		};
	};

	obj.dataSourceGarantias = function(keyCabecera) {
		return new bcs.DataSource( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.							
							pageSize 			: 25,
							transport 			: transporteGarantias(),
							filter				: [ { field: "KRutCtraparte1", operator: "eq", value: keyCabecera.KRutCtraparte1},
													{ field: "KIdContrato", operator: "eq", value: keyCabecera.KIdContrato},
													{ field: "KFechaContrato", operator: "eq", value: keyCabecera.KFechaContrato},
													{ field: "KIdOpeEstruc", operator: "eq", value: keyCabecera.KIdOpeEstruc},
													{ field: "kProducto", operator: "eq", value: keyCabecera.kProducto},
													{ field: "kPeriodoComercial", operator: "eq", value: keyCabecera.kPeriodoComercial}
												  ],
							schema 				: modeloGarantias() 
						  }
		});
		
	}; 

	obj.columnasGarantias = function() {
		return  [
			 {field: "IdDocumento", 		title: 'Id Documento', 			width: "80px"  }
			,{field: "VboUsuario",			title: 'VboUsuario',			width: "80px"  }
			,{field: "Observaciones",		title: 'Observaciones',			width: "80px"  }
			,{field: "FuenteDatos", 		title: 'Fuente Datos', 			width: "80px"  }
			,{field: "KRutCtraparte1", 		title: 'Rut Contraparte', 		width: "100px" }
			,{field: "KIdContrato", 		title: 'Id Contrato', 			width: "70px"  }
			,{field: "KFechaContrato", 		title: 'Fecha Contrato', 		width: "80px"  }
			,{field: "KIdOpeEstruc", 		title: 'Id Operacion', 			width: "70px"  }
			,{field: "S6_TipoGar",      	title: 'Tipo Garantia', 		width: "80px"  }
			,{field: "S6_MonVal", 			title: 'Moneda Valorización', 	width: "120px" }
			,{field: "KS6_IdGar", 			title: 'Id Garantía', 			width: "70px"  }
			,{field: "S6_MontoUmb", 		title: 'Monto Umbral', 			width: "80px"  }
			,{field: "S6_ValGtia",      	title: 'Valor Garantía', 		width: "80px"  }
			,{field: "UsrUltMod",       	title: 'Usuario Modifica', 		width: "100px" }
			,{field: "FecUltMod",       	title: 'Fecha Modifica', 		width: "100px" }
			,{field: "Estado",          	title: 'Estado', 				width: "50px"  }
			,{field: "kProducto",       	title: 'Producto', 				width: "80px"  }
			,{field: "kPeriodoComercial",	title: 'Periodo Comercial', 	width: "100px" }
		];
	}; 
	
	//DATASOURCE TIPO ACTIVO GARANTIAS
		modeloTipoActivoGarantias = function() {
		return {
    		model: {
    			fields: {	
    				ID 					: {type:"number"},
					IdDocumento 		: {type:"number"},
					VboUsuario 			: {type:"string"},
					Observaciones 		: {type:"string"},
					FuenteDatos 		: {type:"string"},
					KRutCtraparte1 		: {type:"string"},
					KIdContrato 		: {type:"string"},
					KFechaContrato 		: {type:"date"},
					KIdOpeEstruc 		: {type:"string"},
					S7_TipoGar			: {type:"string"}, 
					KS7_IdGar			: {type:"string"},
					S7_TipoActGar		: {type:"string"},
					S7_PorcGar			: {type:"number"},
					UsrUltMod 			: {type:"string"},
					FecUltMod 			: {type:"date"},
					Estado 				: {type:"string"},
					kProducto 			: {type:"string"},
					kPeriodoComercial 	: {type:"date"}
    			} 
        	}
		};
	};

	obj.dataSourceTipoActivoGarantias = function(keyCabecera) {
		return new bcs.DataSource( {
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.							
							pageSize 			: 25,
							transport 			: transporteTipoActivoGarantia(),
							filter				: [ { field: "KRutCtraparte1", operator: "eq", value: keyCabecera.KRutCtraparte1},
													{ field: "KIdContrato", operator: "eq", value: keyCabecera.KIdContrato},
													{ field: "KFechaContrato", operator: "eq", value: keyCabecera.KFechaContrato},
													{ field: "KIdOpeEstruc", operator: "eq", value: keyCabecera.KIdOpeEstruc},
													{ field: "kProducto", operator: "eq", value: keyCabecera.kProducto},
													{ field: "kPeriodoComercial", operator: "eq", value: keyCabecera.kPeriodoComercial}
												  ],
							schema 				: modeloTipoActivoGarantias() 
						  }
		});
		
	}; 

	obj.columnasTipoActivoGarantias = function() {
		return  [
			 {field: "IdDocumento", 		title: 'Id Documento', 			width: "80px"  }
			,{field: "VboUsuario",			title: 'VboUsuario',			width: "80px"  }
			,{field: "Observaciones",		title: 'Observaciones',			width: "80px"  }
			,{field: "FuenteDatos", 		title: 'Fuente Datos', 			width: "80px"  }
			,{field: "KRutCtraparte1", 		title: 'Rut Contraparte', 		width: "100px" }
			,{field: "KIdContrato", 		title: 'Id Contrato', 			width: "70px"  }
			,{field: "KFechaContrato", 		title: 'Fecha Contrato', 		width: "80px"  }
			,{field: "KIdOpeEstruc", 		title: 'Id Operacion', 			width: "70px"  }
			,{field: "S7_TipoGar", 			title: 'Tipo Garantía', 		width: "80px"  }
			,{field: "KS7_IdGar",			title: 'Id Garantía', 			width: "80px"  }
			,{field: "S7_TipoActGar",		title: 'Tipo Activos Garantía',	width: "120px" }
			,{field: "S7_PorcGar",			title: 'Porcentaje Tipo Act.', 	width: "120px" }
			,{field: "UsrUltMod",       	title: 'Usuario Modifica', 		width: "100px" }
			,{field: "FecUltMod",       	title: 'Fecha Modifica', 		width: "100px" }
			,{field: "Estado",          	title: 'Estado', 				width: "50px"  }
			,{field: "kProducto",       	title: 'Producto', 				width: "80px"  }
			,{field: "kPeriodoComercial",	title: 'Periodo Comercial', 	width: "100px" }
		];
	};   

	return obj;
	
}]);	

