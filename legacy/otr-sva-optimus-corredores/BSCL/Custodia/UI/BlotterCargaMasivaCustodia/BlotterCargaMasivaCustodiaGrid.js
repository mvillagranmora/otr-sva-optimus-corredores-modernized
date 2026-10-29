angular.module('ModuloCustodia').factory('bcsBlotterCargaMasivaCustodiaGrid', ['bcsComunes',
	function (bcsComunes) {

		var obj = {};

		//-----------------------------
		// CARGA MASIVA
		//-----------------------------

		obj.dsCargaMasiva = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true, // Filtrado de datos en el servidor.
				serverPaging: true, // Paginado de los datos en el servidor.
				serverSorting: false, // Ordenamiento de los datos en el servidor.
				sort: { field: "IdCargaMasiva", dir: "desc" },
				filter: { field: "IdEstadoCargaMasiva", operator: "eq", value: 'ANU' },
				pageSize: 45,
				transport: { read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3200", dataType: "json" } },
				schema: {
					model: {
						fields: {
							IdTipoCargaMasiva: { type: "number" },
							CodigoTipoCargaMasiva: { type: "string" },
							IdCargaMasiva: { type: "number" },
							FechaCarga: { type: "sysdate" },
							FechaProceso: { type: "sysdate" },
							CodigoUsuario: { type: "string" },
							CantidadRegistros: { type: "number" },
							RegistroConErrores:{type:"string"},
							NombreArchivo: { type: "string" },
							IdEstadoCargaMasiva: { type: "string" },
							NombreEstadoCargaMasiva: { type: "string" },
						}
					}
				}
			}
		});


		obj.columnasCargaMasiva = function () {
			return [
				{ field: "IdCargaMasiva", title: "UI_FOLIO_CARGA", width: "70px" }
				, { field: "FechaCarga", title: "UI_FECHA_CARGA", width: "80px", groupable: true }
				, { field: "FechaProceso", title: "UI_FECHA_PROCESO", width: "90px", groupable: true }
				, { field: "CantidadRegistros", title: "UI_CANTIDAD_REGISTROS", width: "90px" }
				, { field: "RegistroConErrores", title: "REGISTRO_CON_ERRORES", width: "90px" }
				, { field: "CodigoUsuario", title: "UI_USUARIO", width: "80px", groupable: true }
				, { field: "NombreEstadoCargaMasiva", title: "UI_ESTADO", width: "80px", groupable: true }
				, { field: "NombreArchivo", title: "UI_NOMBRE_ARCHIVO", }
			];
		};



		//-----------------------------
		// CARGA MASIVA DETALLE
		//-----------------------------	

		obj.dsDefCargaMasivaDetalleCustodia = {
			type: "odata",
			definition: {
				serverFiltering: true, // Filtrado de datos en el servidor.
				serverPaging: true, // Paginado de los datos en el servidor.
				serverSorting: false, // Ordenamiento de los datos en el servidor.
				sort: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },
				transport: { read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3205", dataType: "json" } },
				schema: {
					model: {
						fields: {
							IdCargaMasivaDetalle: { type: "number" },
							DocumentoIdentidad: { type: "string" },
							CodigoPortafolio: { type: "string" },
							Codigo: { type: "string" },
							IdBoveda: { type: "string" },
							CuentaCustodio: { type: "string" },
							IdConcepto: { type: "string" },
							Nemotecnico: { type: "string" },
							Emisor: { type: "string" },
							IdTipoReajuste: { type: "string" },
							FechaVencimiento: { type: "sysdate" },
							Cantidad: { type: "number" },
							Precio: { type: "number" },
							Tasa: { type: "number" },
							Monto: { type: "number" },
							Glosa: { type: "string" },
							FechaIngresoRegistro: { type: "string" },
							NombreEstadoCargaMasivaDetalle: { type: "string" },
							ObservacionValidacion: { type: "string" },
							CorrelativoReporte: { type: "number" },
							TipoOperacion: { type: "string" },
							ClasificacionInstrumento: { type: "string" },
							Plazo: { type: "number" },
							FechaLiquidacion: { type: "sysdate" },
							FechaOperacion: { type: "sysdate" },
							TipoCambio: { type: "number" },
							MontoContado: { type: "number" },
							MontoPlazo: { type: "number" },
							EsNominal: { type: "string" },
							ReferenciaReportado: { type: "string" },
							Bolsa: { type: "string" },
							TipoEvento: { type: "string" },
							NumeroDividendo: { type: "number" },
							FechaLimite: { type: "sysdate" },
							FechaPago: { type: "sysdate" },
							Descripcion: { type: "string" },
							Factor: { type: "number" },
							Cuotas: { type: "number" }
						}
					}
				}
			}
		};



		obj.columnasCargaMasivaDetalle = function (TipoCarga) {
			if (TipoCarga == 'INGRESO_DE_TITULOS_INTERNACIONALES' || TipoCarga == 'ENGRESO_DE_TITULOS_INTERNACIONALES') {
				return [
					{ field: "IdCargaMasivaDetalle", title: "UI_FOLIO", width: "70px" }
					, { field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "110px", groupable: true }
					, { field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "70px", groupable: true }
					, { field: "Codigo", title: "UI_CONDICION_LIQUIDACION", width: "110px", groupable: true }
					, { field: "IdBoveda", title: "UI_BOVEDA", width: "60px", groupable: true }
					, { field: "Bolsa", title: "UI_BOLSA", width: "60px", groupable: true }
					, { field: "CuentaCustodio", title: "UI_CUENTA_CUSTODIA", width: "90px", groupable: true }
					, { field: "IdConcepto", title: "UI_CONCEPTO", width: "70px", groupable: true }
					, { field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "100px", groupable: true }
					, { field: "Emisor", title: "UI_EMISOR", width: "80px", groupable: true }
					, { field: "IdTipoReajuste", title: "UI_TIPO_REAJUSTE", width: "70px", groupable: true }
					, { field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px", groupable: true }
					, { field: "Cantidad", title: "UI_CANTIDAD", width: "90px", template: " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad','CLP','','')).valor #</label>" }
					, { field: "Precio", title: "UI_PRECIO", width: "90px", template: " <label> #:  (util.bcs_formato(Precio,'GEN','Precio','CLP',IdTipoInstrumento,'')).valor #</label>" }
					, { field: "Tasa", title: "UI_TASA", width: "90px", template: " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa','CLP',IdTipoInstrumento,'')).valor #</label>" }
					, { field: "Monto", title: "UI_MONTO", width: "90px", template: " <label> #:  (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "CorrelativoReporte", title: "UI_CORRELATIVO", width: "100px", template: " <label> #:  (util.bcs_formato(CorrelativoReporte,'GEN','Cantidad','CLP','','')).valor #</label>" }
					, { field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "70px", groupable: true }
					, { field: "ClasificacionInstrumento", title: "UI_CLASIFICACION_INSTRUMENTO", width: "70px", groupable: true }
					, { field: "Plazo", title: "UI_PLAZO", width: "90px", template: " <label> #:  (util.bcs_formato(Plazo,'GEN','Cantidad','CLP','','')).valor #</label>" }
					, { field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "100px", groupable: true }
					, { field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "100px", groupable: true }
					, { field: "TipoCambio", title: "UI_TIPO_CAMBIO", width: "90px", template: " <label> #:  (util.bcs_formato(TipoCambio,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "MontoContado", title: "UI_MONTO_CONTADO", width: "90px", template: " <label> #:  (util.bcs_formato(MontoContado,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "MontoPlazo", title: "UI_MONTO_PLAZO", width: "90px", template: " <label> #:  (util.bcs_formato(MontoPlazo,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "EsNominal", title: "UI_ES_NOMINAL", width: "70px", groupable: true }
					, { field: "ReferenciaReportado", title: "UI_REFERENCIA_REPORTADO", width: "110px", groupable: true }
					, { field: "Glosa", title: "UI_GLOSA", width: "200px", groupable: true }
					, { field: "NombreEstadoCargaMasivaDetalle", title: "UI_ESTADO", width: "90px", groupable: true }
					, { field: "ObservacionValidacion", title: "UI_VALIDACION", width: "200px", groupable: true }
				];
			} else if (TipoCarga == 'DIVIDENDO_EN_CUOTAS_FFMM') {
				return [
					{ field: "TipoEvento", title: "UI_TIPO_EVENTO", width: "70px", groupable: true }
					, { field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "100px", groupable: true }
					, { field: "NumeroDividendo", title: "UI_NUMERO_DIVIDENDO", width: "100px", groupable: true }
					, { field: "FechaLimite", title: "UI_FECHA_LIMITE", width: "100px", groupable: true }
					, { field: "FechaPago", title: "UI_FECHA_PAGO", width: "100px", groupable: true }
					, { field: "Descripcion", title: "UI_DESCRIPCION", width: "100px", groupable: true }
					, { field: "Monto", title: "UI_MONTO", width: "90px", template: " <label> #:  (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "Factor", title: "UI_FACTOR", width: "100px", groupable: true }
					, { field: "Cuotas", title: "UI_CUOTAS", width: "100px", groupable: true }
					, { field: "NombreEstadoCargaMasivaDetalle", title: "UI_ESTADO", width: "90px", groupable: true }
					, { field: "ObservacionValidacion", title: "UI_VALIDACION", width: "200px", groupable: true }
				];
			} else {
				return [
					{ field: "IdCargaMasivaDetalle", title: "UI_FOLIO", width: "70px" }
					, { field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "110px", groupable: true }
					, { field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "70px", groupable: true }
					, { field: "Codigo", title: "UI_CONDICION_LIQUIDACION", width: "110px", groupable: true }
					, { field: "IdBoveda", title: "UI_BOVEDA", width: "60px", groupable: true }
					, { field: "Bolsa", title: "UI_BOLSA", width: "60px", groupable: true }
					, { field: "CuentaCustodio", title: "UI_CUENTA_CUSTODIA", width: "90px", groupable: true }
					, { field: "IdConcepto", title: "UI_CONCEPTO", width: "70px", groupable: true }
					, { field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "100px", groupable: true }
					, { field: "Emisor", title: "UI_EMISOR", width: "80px", groupable: true }
					, { field: "IdTipoReajuste", title: "UI_TIPO_REAJUSTE", width: "70px", groupable: true }
					, { field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "100px", groupable: true }
					, { field: "Cantidad", title: "UI_CANTIDAD", width: "90px", template: " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad','CLP','','')).valor #</label>" }
					, { field: "Precio", title: "UI_PRECIO", width: "90px", template: " <label> #:  (util.bcs_formato(Precio,'GEN','Precio','CLP',IdTipoInstrumento,'')).valor #</label>" }
					, { field: "Tasa", title: "UI_TASA", width: "90px", template: " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa','CLP',IdTipoInstrumento,'')).valor #</label>" }
					, { field: "Monto", title: "UI_MONTO", width: "90px", template: " <label> #:  (util.bcs_formato(Monto,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "CorrelativoReporte", title: "UI_CORRELATIVO", width: "100px", template: " <label> #:  (util.bcs_formato(CorrelativoReporte,'GEN','Cantidad','CLP','','')).valor #</label>" }
					, { field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "70px", groupable: true }
					, { field: "ClasificacionInstrumento", title: "UI_CLASIFICACION_INSTRUMENTO", width: "70px", groupable: true }
					, { field: "Plazo", title: "UI_PLAZO", width: "90px", template: " <label> #:  (util.bcs_formato(Plazo,'GEN','Cantidad','CLP','','')).valor #</label>" }
					, { field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "100px", groupable: true }
					, { field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "100px", groupable: true }
					, { field: "TipoCambio", title: "UI_TIPO_CAMBIO", width: "90px", template: " <label> #:  (util.bcs_formato(TipoCambio,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "MontoContado", title: "UI_MONTO_CONTADO", width: "90px", template: " <label> #:  (util.bcs_formato(MontoContado,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "MontoPlazo", title: "UI_MONTO_PLAZO", width: "90px", template: " <label> #:  (util.bcs_formato(MontoPlazo,'GEN','Monto','CLP','','')).valor #</label>" }
					, { field: "EsNominal", title: "UI_ES_NOMINAL", width: "70px", groupable: true }
					, { field: "ReferenciaReportado", title: "UI_REFERENCIA_REPORTADO", width: "110px", groupable: true }
					, { field: "Glosa", title: "UI_GLOSA", width: "200px", groupable: true }
					, { field: "NombreEstadoCargaMasivaDetalle", title: "UI_ESTADO", width: "90px", groupable: true }
					, { field: "ObservacionValidacion", title: "UI_VALIDACION", width: "200px", groupable: true }
				];
			}
		};


		return obj;
	}]);

