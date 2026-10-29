angular.module('ModuloCuentaInversion').factory('bcsCuentaInversionGrid',
		function(bcsComunes,$translate)  	
{
	var obj = {};
	var filtersSaldos = {};	
	
	//SALDO CUENTAS
	obj.dsDefSaldosCuentas = {
		type : "array",
		definition : {
			data		: [],
			pageSize 	: 20,
			serverSorting	: false, // Ordenamiento de los datos en el servidor.
			sort 		: [ { field: "NombreEjecutivo", dir: "asc" },
							{ field: "NombreSucursal", dir: "asc" },
							{ field: "NombreCliente", dir: "asc" },
							{ field: "NombrePortafolio", dir: "asc" }
						  ],
			schema  	: {
				model : {
					fields : {
						NombreEjecutivo			: {type : "string"},
						NombreCliente			: {type : "string"},
						TipoCliente				: {type : "string"},
						DocumentoIdentidad		: {type : "string"},
						CodigoPortafolio 		: {type : "string"},
						NombreMoneda  			: {type : "string"},
						NombreSucursal  		: {type : "string"},
						CodigoMoneda			: {type : "string"},
						SaldoDisponible  		: {type : "number"},
						SaldoEnGarantia  		: {type : "number"},
						SaldoAPlazoSimultanea	: {type : "number"},
                      	SaldoAPlazoVentaCorta	: {type : "number"},
                      	SaldoAPlazoCompromiso	: {type : "number"},
						SaldoRetiro				: {type : "number"},
						IdEjecutivo				: {type : "number"},
						IdCliente				: {type : "number"},
						IdPortafolio			: {type : "number"},
						IdMoneda				: {type : "string"},
						FechaSaldo				: {type : "sysdate"},						
						IdCuentaInversion      	: {type : "number"},
						MontoMoneyMarket		: {type : "number"},
						TotalLiquidez			: {type : "number"},
						Producto				: {type : "string"},
						SaldoRetenido			: {type : "string"},
						IdProductoAgrupador     : {type : "number"},
						Relacion				: {type : "string"},
						CodigoEjecutivo			: {type : "string"},
						FechaUltimoMovimiento	: {type : "sysdate"},
						SaldoPendienteLibDAP    : {type : "number"}
						
					}
				}
			}
		}
	};

	obj.columnasSaldosCuentasCliente = function() {
		return  [
    		 {field : "NombreMoneda", 		title:"UI_CUENTA_INVERSION", 	width: "100px"}
			,{field : "SaldoDisponible", 	title:"UI_DISPONIBLE", 			width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoEnGarantia", 	title:"UI_GARANTIA",			width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoEnGarantia,'GEN','Monto',IdMoneda,'','')).valor #</label>"}			
			,{field : "SaldoRetiro", 		title:"UI_INGRESOS_EGRESOS",	width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoRetiro,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoAPlazoSimultanea", 	title:"UI_A_PLAZO_SIMULTANEA",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoAPlazoSimultanea,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoAPlazoVentaCorta", 	title:"UI_A_PLAZO_PRESTAMOS",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoAPlazoVentaCorta,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoAPlazoCompromiso", 	title:"UI_A_PLAZO_COMPROMISO",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoAPlazoCompromiso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoRetenido", 		title:"UI_SALDO_RETENIDO",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoRetenido,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "Producto", 			title:"UI_PRODUCTO",			width: "100px" }
			,{field : "CodigoPortafolio", 	title:"UI_CODIGO_PORTAFOLIO", 			width: "90px" }
			,{field : "SaldoPendienteLibDAP",	title:"UI_SALDO_PEND_LIB_DAP",	width: "120px",	template : " <label> #:  (util.bcs_formato(SaldoPendienteLibDAP,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
		];
	};

	obj.columnasSaldosCuentasEjecutivo = function() {
		return  [
			 {field : "FechaSaldo", 			title:"UI_FECHA_EFECTIVA", 		width: "70px",	groupable: true}
			,{field : "NombreEjecutivo", 		title:"UI_EJECUTIVO", 			width: "200px",	groupable: true}
			,{field : "NombreSucursal", 		title:"UI_SUCURSAL", 			width: "150px",	groupable: true}
			,{field : "NombreCliente", 			title:"UI_CLIENTE", 			width: "200px",	groupable: true}
			,{field : "TipoCliente", 			title:"UI_TIPO_CLIENTE", 		width: "150px",	groupable: true}
			,{field : "DocumentoIdentidad", 	title:"UI_DOCUMENTO_IDENTIDAD", width: "60px",	groupable: true}
			,{field : "CodigoPortafolio", 		title:"UI_CODIGO_PORTAFOLIO", 	width: "90px",	groupable: true}
    		,{field : "CodigoMoneda", 			title:"UI_CUENTA_INVERSION", 	width: "60px",	groupable: true}
			,{field : "SaldoDisponible", 		title:"UI_DISPONIBLE", 			width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoDisponible,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "MontoMoneyMarket", 		title:"UI_MONEY_MARKET",	width: "100px",	template : " <label> #:  (util.bcs_formato(MontoMoneyMarket,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "TotalLiquidez", 			title:"UI_TOTAL_LIQUIDEZ",		width: "100px",	template : " <label> #:  (util.bcs_formato(TotalLiquidez,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoEnGarantia", 		title:"UI_GARANTIA",			width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoEnGarantia,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoRetiro", 			title:"UI_INGRESOS_EGRESOS",	width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoRetiro,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoAPlazoSimultanea", 	title:"UI_A_PLAZO_SIMULTANEA",	width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoAPlazoSimultanea,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoAPlazoVentaCorta", 	title:"UI_A_PLAZO_PRESTAMOS",	width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoAPlazoVentaCorta,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "SaldoAPlazoCompromiso", 	title:"UI_A_PLAZO_COMPROMISO",	width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoAPlazoCompromiso,'GEN','Monto',IdMoneda,'','')).valor #</label>"}

					
			,{field : "SaldoRetenido", 		    title:"UI_SALDO_RETENIDO",	    width: "100px",	template : " <label> #:  (util.bcs_formato(SaldoRetenido,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			
			,{field : "Producto",				title:"UI_PRODUCTO",			width: "100px"}
			,{field : "Relacion", 				title:"UI_RELACION", 			width: "60px",	groupable: true}
			,{field : "CodigoEjecutivo",		title:"UI_CODIGO_EJECUTIVO",	width: "100px",	groupable: true}
			,{field : "FechaUltimoMovimiento",	title:"UI_FECHA_ULTIMO_MOVIMIENTO",	width: "150px", groupable: true}
		];
	};


	//CERTIFICACION MOVIMIENTOS
	obj.dsDefMovimientosCuentaInversion = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			pageSize 			: 17,
			sort 				: [ { field: "FechaSistema", dir: "asc" },
			     				    
			     				    { field: "IdCargoAbonoMovimiento", dir: "asc" },
			     				   
									{ field: "IdCliente", dir: "asc" },
									{ field: "IdPortafolio", dir: "asc" },
									{ field: "IdMoneda", dir: "asc" }
								  ],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1363",dataType : "json"}},
			schema : {
				model : {
					fields : {										
						FechaSistema 					: {type : "sysdate"},
						FechaEfectivaMovimiento 		: {type : "sysdate"},
						FechaCertificacion				: {type : "sysdatetime"},
						NombreCliente					: {type : "string"},
						DocumentoIdentidad				: {type : "string"},
						CodigoPortafolio				: {type : "string"},
						Proposito						: {type : "string"},
						NombreConcepto					: {type : "string"},
						CodigoMoneda					: {type : "string"},
						CodigoNombreMoneda				: {type : "string"},
						Monto							: {type : "number"},
						Cargo							: {type : "number"},
						Abono							: {type : "number"},
						NombreTipoComprobante			: {type : "string"},
						FechaComprobante 				: {type : "sysdate"},
						IdComprobante 					: {type : "number"},
						FolioComprobante				: {type : "string"},
						Descripcion						: {type : "string"},
						IdCargoAbonoMovimiento			: {type : "number"},
						GlosaAnulacion					: {type : "string"},
						NombreEstado					: {type : "string"},
						AutorizacionEstado				: {type : "string"},
						NombreTipoProducto				: {type : "string"},
						NombreTipoOperacion				: {type : "string"},		
						FechaOperacion					: {type : "sysdate"},
						FechaVencimientoOperacion		: {type : "sysdate"},
						IdOperacion						: {type : "number"},
						MontoOperacion					: {type : "number"},
						CodigoUsuarioCertificador		: {type : "string"},
						CodigoUsuarioCreador			: {type : "string"},						
						EsCargoAbonoPorTraspaso			: {type : "string"},
						EsCargoAbonoPorTraspasoTraducido: {type : "string"},
						IdCargoAbonoRelacionadoTraspaso : {type : "number"},
						NombreClienteRelacionado		: {type : "string"},
						CodigoPortafolioRelacionado		: {type : "string"},
						NombreEstadoEvento				: {type : "string"},
						Producto						: {type : "string"},
						NombrePortafolio				: {type : "string"},
						Cantidad						: {type : "number"},
						Relacion						: {type : "string"},
						NemotecnicoOriginal             : {type : "string"},
						IdClasificacionTipoInstrumento  : {type : "string"}
					}
				}
			}
		}
	};

	obj.columnasMovimientosCuentaInversionCliente = function() {
		return  [
    		 {field : "FechaSistema", 						title:"UI_FECHA_MOVIMIENTO", 			width: "80px"}
    		,{field : "NombreEstado", 						title:"UI_ESTADO", 						width: "80px"}
			,{field : "NombreConcepto", 					title:"UI_CONCEPTO", 					width: "200px"}
			,{field : "CodigoMoneda", 						title:"UI_MONEDA", 						width: "50px"}
			,{field : "EsCargoAbonoPorTraspasoTraducido", 	title:"UI_ES_TRANSFERENCIA", 			width: "70px"}
			,{field : "Cargo",	 							title:"UI_CARGO", 						width: "90px",	template : " <label> #:  (util.bcs_formato(Cargo,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "Abono",	 							title:"UI_ABONO", 						width: "90px",	template : " <label> #:  (util.bcs_formato(Abono,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "NombreTipoComprobante",				title:"UI_TIPO_COMPROBANTE", 			width: "250px"}
			,{field : "FechaComprobante", 					title:"UI_FECHA_COMPROBANTE", 			width: "80px"}
			,{field : "IdComprobante", 						title:"UI_FOLIO_COMPROBANTE", 			width: "85px",	template: kendo.template("#if ( IdComprobante == 0  || IdComprobante == null)  {##} else {##=IdComprobante##}#")}
			,{field : "GlosaAnulacion", 					title:"UI_MOTIVO_ANULACION", 			width: "200px",	editable: true }
			,{field : "AutorizacionEstado", 				title:"UI_AUTORIZACION", 				width: "300px"}
			,{field : "NombreEstadoEvento", 				title:"UI_ESTADO_AUTORIZACION", 		width: "80px"}
			,{field : "IdCargoAbonoMovimiento", 			title:"UI_SECUENCIA_MOVIMIENTO", 		width: "80px"}
			,{field : "IdCargoAbonoRelacionadoTraspaso", 	title:"UI_SECUENCIA_TRANSFERENCIA", 	width: "80px",	template: kendo.template("#if (( IdCargoAbonoRelacionadoTraspaso == 0  || (IdCargoAbonoRelacionadoTraspaso == null)))  {##} else {##=IdCargoAbonoRelacionadoTraspaso##}#")}
			,{field : "NombreClienteRelacionado", 			title:"UI_NOMBRE_CLIENTE_RELACIONADO", 	width: "150px",	groupable: true}
			,{field : "CodigoPortafolioRelacionado", 		title:"UI_PORTAFOLIO_RELACIONADO", 		width: "70px", 	groupable: true}		
			,{field : "Descripcion", 						title:"UI_DESCRIPCION", 				width: "250px"}
			,{field : "CodigoUsuarioCreador", 				title:"UI_EMPLEADO_CREADOR", 			width: "80px"}
			,{field : "CodigoUsuarioCertificador", 			title:"UI_EMPLEADO_CERTIFICADOR", 		width: "80px"}
			,{field : "Producto",				 			title:"UI_PRODUCTO", 					width: "80px"}
			,{field : "NombrePortafolio",			 		title:"UI_NOMBRE_PORTAFOLIO", 			width: "80px"}
			,{field	: "Cantidad",							title:"UI_CANTIDAD", 					width: "85px", 	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}

		];
	};

	obj.columnasMovimientosCuentaInversionEjecutivo = function() {
		return  [		
    		 {field : "NombreCliente", 						title:"UI_NOMBRE_CLIENTE", 				width: "150px",	groupable: true}
			,{field : "DocumentoIdentidad", 				title:"UI_DOCUMENTO_IDENTIDAD", 		width: "70px", 	groupable: true}
			,{field : "CodigoPortafolio", 					title:"UI_PORTAFOLIO", 					width: "70px", 	groupable: true}
			,{field : "FechaSistema", 						title:"UI_FECHA_MOVIMIENTO", 			width: "70px"}
			,{field : "FechaEfectivaMovimiento",			title:"UI_FECHA_EFECTIVA", 				width: "70px"}			
			,{field : "NombreConcepto", 					title:"UI_CONCEPTO", 					width: "200px",	groupable: true}
			,{field : "EsCargoAbonoPorTraspasoTraducido", 	title:"UI_ES_TRANSFERENCIA", 			width: "70px",	groupable: true}
			,{field : "CodigoMoneda", 						title:"UI_MONEDA", 						width: "50px",	groupable: true}
			,{field : "Cargo",	 							title:"UI_CARGO", 						width: "90px",	template : " <label> #:  (util.bcs_formato(Cargo,'GEN','Monto',IdMoneda,'','')).valor #</label>", aggregates: ["sum"] ,groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#"}
			,{field : "Abono",	 							title:"UI_ABONO", 						width: "90px",	template : " <label> #:  (util.bcs_formato(Abono,'GEN','Monto',IdMoneda,'','')).valor #</label>", aggregates: ["sum"] ,groupFooterTemplate:$translate.instant("UI_TOTAL")+': '+"#=(util.bcs_formato(sum,'GEN','Monto','USD','','')).valor#"}
			,{field : "NombreTipoComprobante",				title:"UI_TIPO_COMPROBANTE", 			width: "250px"}			
			,{field : "IdComprobante", 						title:"UI_SECUENCIA_COMPROBANTE", 		width: "85px", 	template: kendo.template("#if (( IdComprobante == 0  || (IdComprobante == null)))  {##} else {##=IdComprobante##}#")}						
			,{field : "FolioComprobante", 					title:"UI_FOLIO_COMPROBANTE", 			width: "85px",	template: kendo.template("#if ( FolioComprobante == '0'  || FolioComprobante == null)  {##} else {##=FolioComprobante##}#")}
			,{field : "FechaComprobante", 					title:"UI_FECHA_COMPROBANTE", 			width: "70px"}
			//Datos de la Operación asociada
			,{field : "NombreTipoProducto", 				title:"UI_TIPO_PRODUCTO", 				width: "150px"}
			,{field : "NombreTipoOperacion", 				title:"UI_TIPO_OPERACION", 				width: "200px"}
			,{field : "FechaOperacion", 					title:"UI_FECHA_OPERACION", 			width: "70px"}
			,{field : "FechaVencimientoOperacion", 			title:"UI_FECHA_VENCIMIENTO", 			width: "70px"}
			,{field : "IdOperacion", 						title:"UI_SECUENCIA_OPERACION", 		width: "80px", template: kendo.template("#if (( IdOperacion == 0  || (IdOperacion == null)))  {##} else {##=IdOperacion##}#")}
			,{field : "MontoOperacion", 					title:"UI_MONTO_OPERACION", 			width: "80px", template: " <label> #:  (util.bcs_formato(MontoOperacion,'GEN','Monto',IdMoneda,'','')).valor #</label>" }			
			//Datos Movimiento Cuenta Inversion
			,{field : "IdCargoAbonoMovimiento", 			title:"UI_SECUENCIA_MOVIMIENTO", 		width: "80px"}						
			,{field : "IdCargoAbonoRelacionadoTraspaso",  	title:"UI_SECUENCIA_TRANSFERENCIA",		width: "80px",	template: kendo.template("#if (( IdCargoAbonoRelacionadoTraspaso == 0  || (IdCargoAbonoRelacionadoTraspaso == null)))  {##} else {##=IdCargoAbonoRelacionadoTraspaso##}#")}
			,{field : "NombreClienteRelacionado", 			title:"UI_NOMBRE_CLIENTE_RELACIONADO", 	width: "150px",	groupable: true}
			,{field : "CodigoPortafolioRelacionado", 		title:"UI_PORTAFOLIO_RELACIONADO",		width: "70px", 	groupable: true}
			,{field : "CodigoUsuarioCreador", 				title:"UI_EMPLEADO_CREADOR", 			width: "80px"}
			,{field : "FechaCertificacion", 				title:"UI_FECHA_CERTIFICACION", 		width: "100px"}
			,{field : "CodigoUsuarioCertificador", 			title:"UI_EMPLEADO_CERTIFICADOR", 		width: "80px"}
			,{field : "Descripcion", 						title:"UI_DESCRIPCION", 				width: "250px"}
			,{field : "NombreEstado", 						title:"UI_ESTADO", 						width: "80px"}
			//Datos Anulacion
			,{field : "GlosaAnulacion", 					title:"UI_MOTIVO_ANULACION", 			width: "200px", editable: true }
			,{field : "AutorizacionEstado", 				title:"UI_AUTORIZACION", 				width: "300px"}
			,{field : "NombreEstadoEvento", 				title:"UI_ESTADO_AUTORIZACION", 		width: "80px"}
			,{field : "Producto",							title:"UI_PRODUCTO",					width: "100px"}
			,{field	: "Cantidad",							title:"UI_CANTIDAD", 					width: "85px", 	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
			,{field : "Relacion", 							title:"UI_RELACION", 					width: "60px",	groupable: true}
			
		];
	};


	//LIBRO DE INVENTARIO
	obj.dsDefLibroInventarioSaldos = {
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: true, // Ordenamiento de los datos en el servidor.
			sort 				: [{ field: "IdCuentaInversion", dir: "asc" },								   
								   { field: "FechaEfectivaMovimiento", dir: "asc" },
								   { field: "FechaCalce", dir: "asc" }],
			//pageSize 			: 25,
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1415",dataType : "json"}},
			schema : {
				model : {
					fields : {
						IdCuentaInversion			: {type : "number"},
						CodigoNombreMoneda 			: {type : "string"},																		
						IdCargoAbonoMovimiento		: {type : "number"},
						NombreConcepto				: {type : "string"},
						FechaEfectivaMovimiento		: {type : "sysdate"},

						IdCargoAbonoMovimientoCalce	: {type : "number"},
						NombreConceptoCalce			: {type : "string"},


						AntiguedadDeuda 			: {type : "number"},
						Monto 						: {type : "number"},
						MontoCalce					: {type : "number"},
						Saldo						: {type : "number"},
						FechaCalce					: {type : "sysdatetime"},
						Producto					: {type : "string"},
						NombrePortafolio			: {type : "string"},
					}
				}
			}
		}
	};

	obj.columnasLibroInventarioSaldosCliente = function() {
		return  [
    		 {field : "CodigoNombreMoneda", 			title:"UI_CUENTA_INVERSION", 			width: "150px"}						
						,{field : "IdCargoAbonoMovimiento",			title:"UI_SECUENCIA_MOVIMIENTO",		width: "80px"}
						,{field : "NombreConcepto", 				title:"UI_CONCEPTO", 					width: "200px"}
						,{field : "FechaEfectivaMovimiento",		title:"UI_FECHA_EFECTIVA",				width: "85px"}
						,{field : "IdCargoAbonoMovimientoCalce",	title:"UI_SECUENCIA_MOVIMIENTO_CALCE",	width: "80px"}
						,{field : "NombreConceptoCalce", 			title:"UI_CONCEPTO_CALCE", 				width: "200px"}
						,{field : "Monto", 							title:"UI_MONTO_DEUDA", 				width: "85px",		template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "FechaCalce",						title:"UI_FECHA_CALCE",					width: "100px"}					
						,{field : "MontoCalce", 					title:"UI_MONTO_CALCE", 				width: "85px",		template : " <label> #:  (util.bcs_formato(MontoCalce,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "AntiguedadDeuda",				title:"UI_ANTIGUEDAD_DEUDA",			width: "85px"}
						,{field : "Saldo", 							title:"UI_SALDO", 						width: "85px",		template : " <label> #:  (util.bcs_formato(Saldo,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "Producto", 						title:"UI_PRODUCTO", 					width: "85px"}
						,{field : "NombrePortafolio", 				title:"UI_PORTAFOLIO", 					width: "85px"}
						
		];
	};

	return obj;
});	

