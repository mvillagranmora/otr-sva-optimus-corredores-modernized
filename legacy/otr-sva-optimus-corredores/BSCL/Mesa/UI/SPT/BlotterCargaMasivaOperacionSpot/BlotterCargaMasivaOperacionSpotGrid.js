angular.module('ModuloMesa').factory('bcsBlotterCargaMasivaOperacionSpotGrid', ['bcsComunes',
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
				transport: { read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1852", dataType: "json" } },
				schema: {
					model: {
						fields: {
							IdTipoCargaMasiva: { type: "number" },
							CodigoTipoCargaMasiva: { type: "string" },
							NombreTipoCargaMasiva: { type: "string" },
							IdEstadoTipoCargaMasiva: { type: "string" },
							IdCargaMasiva: { type: "number" },
							FechaCarga: { type: "sysdate" },
							FechaProceso: { type: "sysdate" },
							CantidadRegistros: { type: "number" },
							IdEmpresa: { type: "string" },
							IdUsuario: { type: "string" },
							IdEstadoCargaMasiva: { type: "string" },
							NombreArchivo: { type: "string" },
							CodigoUsuario: { type: "string" },
							EstadoCargaMasiva: { type: "string" },
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
				, { field: "CodigoUsuario", title: "UI_USUARIO", width: "80px", groupable: true }
				, { field: "EstadoCargaMasiva", title: "UI_ESTADO", width: "80px", groupable: true }
				, { field: "NombreArchivo", title: "UI_NOMBRE_ARCHIVO", }
			];
		};



		//-----------------------------
		// CARGA MASIVA DETALLE
		//-----------------------------	

		obj.dsDefCargaMasivaDetalleOperacionSpot = {
			type: "odata",
			definition: {
				serverFiltering: true, // Filtrado de datos en el servidor.
				serverPaging: true, // Paginado de los datos en el servidor.
				serverSorting: false, // Ordenamiento de los datos en el servidor.
				sort: { field: "IdEstadoCargaMasivaDetalle", dir: "asc" },
				transport: { read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1853", dataType: "json" } },
				schema: {
					model: {
						fields: {
							IdCargaMasiva: { type: "number" },
							IdCargaMasivaDetalle: { type: "number" },
							DocumentoIdentidad: { type: "string" },
							CodigoPortafolio: { type: "string" },
							ProductoAgrupador: { type: "string" },
							TipoOperacion: { type: "string" },
							FechaOperacion: { type: "sysdate" },
							HoraOperacion: { type: "string" },
							CanalIngreso: { type: "string" },
							MontoMonedaPrincipal: { type: "number" },
							TipoCambioCierre: { type: "number" },
							TipoCambioCosto: { type: "number" },
							PuntaCompra: { type: "number" },
							PuntaVenta: { type: "number" },
							FormaPagoEntregamos: { type: "string" },
							FormaPagoRecibimos: { type: "string" },
							Observacion: { type: "string" },
							AfectoADerivado: { type: "string" },
							DolarObservado: { type: "string" },
							CodigoComercio: { type: "number" },
							DetalleValidacion: { type: "string" },
							CodigoTipoCargaMasiva: { type: "string" },
							TipoComprobanteFacturacion: { type: "string" },
						}
					}
				}
			}
		};


		obj.columnasCargaMasivaDetalle = function () {
			return [
				  { field: "IdCargaMasiva", title: "UI_FOLIO_CARGA", width: "70px" }
				, { field: "IdCargaMasivaDetalle", title: "UI_FOLIO", width: "70px" }
				, { field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "70px" }
				, { field: "CodigoPortafolio", title: "UI_PORTAFOLIO", width: "70px" }
				, { field: "ProductoAgrupador", title: "UI_PRODUCTO_AGRUPADOR", width: "70px" }
				, { field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "70px" }
				, { field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "70px" }
				, { field: "HoraOperacion", title: "UI_HORA_OPERACION", width: "70px" }
				, { field: "CanalIngreso", title: "UI_TIPO_CANAL_INGRESO", width: "70px" }
				, { field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_PRINCIPAL", width: "70px" }
				, { field: "TipoCambioCierre", title: "UI_TIPO_CAMBIO_CIERRE", width: "70px" }
				, { field: "TipoCambioCosto", title: "UI_TIPO_CAMBIO_COSTO", width: "70px" }
				, { field: "PuntaCompra", title: "UI_PUNTA_COMPRA", width: "70px" }
				, { field: "PuntaVenta", title: "UI_PUNTA_VENTA", width: "70px" }
				, { field: "FormaPagoEntregamos", title: "UI_FORMA_PAGO_ENTREGAMOS", width: "70px" }
				, { field: "FormaPagoRecibimos", title: "UI_FORMA_PAGO_RECIBIMOS", width: "70px" }
				, { field: "Observacion", title: "UI_OBSERVACION", width: "70px" }
				, { field: "AfectoADerivado", title: "UI_AFECTO_A_DERIVADO", width: "70px" }
				, { field: "DolarObservado", title: "UI_DOLAR_OBSERVADO", width: "70px" }
				, { field: "CodigoComercio", title: "UI_CODIGO_COMERCIO", width: "70px" }
				, { field: "DetalleValidacion", title: "UI_DETALLE", width: "70px" }
				, { field: "CodigoTipoCargaMasiva", title: "UI_TIPO_CARGA_MASIVA", width: "70px" }
				, { field: "TipoComprobanteFacturacion", title: "UI_TIPO_COMPROBANTE_FACTURACION", width: "70px" }
			];
		};	


		return obj;
	}]);

