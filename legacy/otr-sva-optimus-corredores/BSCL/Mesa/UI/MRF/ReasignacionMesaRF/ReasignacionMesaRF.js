angular.module('ModuloMesa').controller('BlotterReasignacionMesaRFCtrl',
	function ($scope, $translate, bcsComunes, $resource, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosMesa) {

		$scope.data = {};

		$scope.IdVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.IdVentana, false, 3000);
		var parametros = bcsComunes.ventanaParams($scope.IdVentana);
		$scope.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);

		var gridReasignacionOP;
		var gridReasignacionCartera;
		var gridReasignados;
		var esFungible = true;

		// INICIALIZACION BOTONES:

		$scope.btn = {};
		$scope.infoOperacion = {};
		$scope.infoOperacion.totalReasignado = '';

		var totalReasignado = 0;
		var diferencia = 0;
		var cantidad = 0;
		var parcial = false;
		var carteras = [];
		var comprobar = true;
		var enTransito;

		// MESAS:
		var cMesa = this;
		cMesa.mesas = [];
		cMesa.ds = {};

		$scope.btn.Detalle = false;
		$scope.btn.BuscarOp = false;
		$scope.btn.QuitarOp = false;
		$scope.btn.Reasig = false;
		$scope.btn.QuitarReasig = false;
		$scope.btn.AceptarReasig = false;

		//------------------------------------------------
		// CONFIGURACION COMBOBOX
		//------------------------------------------------

		// CONFIGURACION DE COMBOBOX TIPO DE OPERACION
		$scope.IdTipoProducto = { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' };
		$scope.data.EsAnticipo = { "field": "EsAnticipo", "operator": "eq", "value": 'N' };
		$scope.data.EsAnulacion = { "field": "EsAnulacion", "operator": "eq", "value": 'N' };
		$scope.data.IdTipoOperacion = [
			{ "field": "IdTipoOperacion", "operator": "eq", "value": 'VENTER' },
			{ "field": "IdTipoOperacion", "operator": "eq", "value": 'COMTER' }
		];

		$scope.data.oConfiguracionTipoOperacion = {
			tipo: "combobox-tipo-operacion",
			configuracion: {},
			idActual: true,
			seleccionado: false,
			filtros: {
				complejo: [$scope.data.IdTipoOperacion],
				logic: "and",
				filters: [{
					logic: "or",
					filters: $scope.data.IdTipoOperacion
				},
				$scope.IdTipoProducto,
				$scope.data.EsAnticipo,
				$scope.data.EsAnulacion
				]
			},
			idDefecto: ''
		};

		//------------------------------------------------
		// CONFIGURACION DE GRILLA
		//------------------------------------------------

		// GRILLA OPERACIONES
		$scope.confBasicaReasignacionOp = {
			dataSource: new bcs.DataSource({
				type: "odata",
				pageSize: 1,
				definition: {
					serverFiltering: true, // Filtrado de datos en el servidor.
					serverPaging: true, // Paginado de los datos en el servidor.
					serverSorting: true, // Ordenamiento de los datos en el servidor.
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1450",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								NombreOperacion: { type: "string" },
								FolioOperacion: { type: "number" },
								Secuencia: { type: "number" },
								IdOperacion: { type: "number" },
								FechaOperacion: { type: "sysdate" },
								DocumentoIdentidad: { type: "string" },
								CodigoPortafolio: { type: "string" },
								MontoOperacion: { type: "number" },
								Tasa: { type: "number" },
								IdTipoReajuste: { type: "string" },
								MontoOperacionInicial: { type: "number" },
								IdMoneda: { type: "string" },
								MontoOperacionFinal: { type: "number" },
								FechaVencimiento: { type: "sysdate" },
								Glosa: { type: "string" }
							}
						}
					}
				},
			}),

			columns: [
				{ field: "NombreOperacion", title: "UI_TIPO_OPERACION", width: "100px", groupable: true }
				, { field: "FolioOperacion", title: "UI_FOLIO_OPERACION", width: "60px" }
				, { field: "IdOperacion", title: "UI_SECUENCIA_OPERACION", width: "70px" }
				// , { field: "IdOperacion", title: "UI_SECUENCIA_OPERACION", width: "70px", template: kendo.template("#if(NombreOperacion!=undefined){# #: FolioOperacion # #}else{# #: IdOperacion # #}#") }
				, { field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "70px" }
				, { field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "90px" }
				, { field: "CodigoPortafolio", title: "UI_CODIGO_PORTAFOLIO", width: "90px", }
				, { field: "MontoOperacion", title: "UI_MONTO_OPERACION", width: "90px", template: "<label> #: (util.bcs_formato(MontoOperacion, 'GEN','MONTO','CLP','','')).valor #</label>" }
				, { field: "Tasa", title: "UI_TASA_OPERACION", width: "40px" }
				, { field: "IdTipoReajuste", title: "UI_TIPO_REAJUSTE", width: "50px" }
				, { field: "MontoOperacionInicial", title: "UI_MONTO_EQUIVALENTE", width: "90px", template: "<label> #: (util.bcs_formato(MontoOperacionInicial, 'GEN','MONTO','CLP','','')).valor #</label>" }
				, { field: "IdMoneda", title: "UI_MONEDA", width: "70px" }
				, { field: "MontoOperacionFinal", title: "UI_VALOR_FINAL", width: "100px", width: "90px", template: "<label> #: (util.bcs_formato(MontoOperacionFinal, 'GEN','MONTO','CLP','','')).valor #</label>" }
				, { field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "70px" }
				,{field: "Glosa", 		title: "UI_COMENTARIO", 	width: "80px",	groupable: true}
			],
			reorderable: true,
			sortable: true,
			pageable: {
				numeric: true
			},
			page: 1,
			refresh: false,
			height: 200,
			selectable: "row",
			userSettings: true,
			gridCreated: function (g) {
				gridReasignacionOP = g;
				if (parametros.operacion !== undefined) {
					let operacion = [{
						"NombreOperacion": parametros.operacion.NombreOperacion
						, "FolioOperacion": parametros.operacion.FolioOperacion
						, "IdOperacion": parametros.operacion.IdOperacion
						, "FechaOperacion": util.dateSPdate(parametros.operacion.FechaOperacion || '0001-01-01')
						, "DocumentoIdentidad": parametros.operacion.DocumentoIdentidad
						, "CodigoPortafolio": parametros.operacion.IdPortafolio
						, "MontoOperacion": parametros.operacion.MontoOperacion
						, "Tasa": parametros.operacion.Tasa
						, "IdTipoReajuste": parametros.operacion.IdTipoReajuste
						, "MontoOperacionInicial": parametros.operacion.MontoOperacionInicial
						, "IdMoneda": parametros.operacion.IdMoneda
						, "MontoOperacionFinal": parametros.operacion.MontoOperacionFinal
						, "FechaVencimiento": util.dateSPdate(parametros.operacion.FechaOperacion || '0001-01-01')
					}];

					$scope.confBasicaReasignacionOp.dataSource.add(parametros.operacion);
					gridReasignacionOP.refresh();
				}
			}
		};

		// GRILLA CARTERAS
		$scope.confBasicaReasignacionCarteras = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true, // Filtrado de datos en el servidor.
					serverPaging: true, // Paginado de los datos en el servidor.
					serverSorting: true, // Ordenamiento de los datos en el servidor.
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1477",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								FolioCarteraCustodiaDetalle: { type: "number" },
								CarteraNominal: { type: "string" },
								NemotecnicoDetalle: { type: "string" },
								IdClasificacionTipoInstrumentoDetalle: { type: "string" },
								IdEmisorDetalle: { type: "string" },
								TasaCompraDetalle: { type: "number" },
								MontoCompraDetalle: { type: "number" },
								NominalAsignar: { type: "number" },
								CantidadReservadaDetalle: { type: "number" },
								ValorRescateDetalle: { type: "number" },
								CorteMinimoDetalle: { type: "number" },
								FolioOperacion: { type: "number" },
								NombreTipoOperacion: { type: "string" },
								FechaOperacion: { type: "sysdate" },
								NominalesAsignados: { type: "number" },
								CortesPorAsignar: { type: "number" },
								Glosa: { type: "string" }
							}
						}
					}
				},
			}),
			dataBound: function (e) {
				const carteras = $scope.confBasicaReasignacionCarteras.dataSource.view();
				if (carteras.length > 0 && comprobar) {
					comprobarMovimientosEnTransito(carteras)
					comprobar = false;
				}
			},
			columns: [
				{ field: "FolioCarteraCustodiaDetalle", title: "UI_FOLIO_CARTERA", width: "70px", groupable: true }
				, { field: "NemotecnicoDetalle", title: "UI_NEMOTECNICO", width: "70px", groupable: true }
				, { field: "IdClasificacionTipoInstrumentoDetalle", title: "UI_MERCADO", width: "50px" }
				, { field: "IdEmisorDetalle", title: "UI_EMISOR", width: "60px", }
				, { field: "TasaCompraDetalle", title: "UI_TASA", width: "40px", groupable: true }
				, { field: "MontoCompraDetalle", title: "UI_MONTO_LIQUIDACION", width: "70px", groupable: true, template: "<label> #: (util.bcs_formato(MontoCompraDetalle, 'GEN','MONTO','CLP','','')).valor #</label>" }
				, { field: "NominalAsignar", title: "UI_NOMINALES_ASIGNAR", width: "70px", template: kendo.template("#if(IdClasificacionTipoInstrumentoDetalle == 'RF'){# #: (util.bcs_formato(CantidadReservadaDetalle, 'GEN','MONTO','CLP','','')).valor # #}else{# #: (util.bcs_formato(ValorRescateDetalle, 'GEN','MONTO','CLP','','')).valor # #}#") }
				, { field: "CorteMinimoDetalle", title: "UI_CORTE_MINIMO", width: "70px", template: "<label> #: (util.bcs_formato(CorteMinimoDetalle, 'GEN','MONTO','CLP','','')).valor #</label>" }
				, { field: "FolioOperacion", title: "UI_FOLIO_OPERACION", width: "80px" }
				, { field: "NombreTipoOperacion", title: "UI_TIPO_OPERACION", width: "100px" }
				, { field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "70px" }
				,{field: "Glosa", 		title: "UI_COMENTARIO", 	width: "80px",	groupable: true}

			],
			sortable: true,
			pageable: {
				numeric: true,
				pageSize: 10
			},
			refresh: false,
			height: 200,
			selectable: "row",
			userSettings: true,
			gridCreated: function (g) {
				gridReasignacionCartera = g;
			}
		};

		// REASIGNACIONES
		$scope.confReasignados = {
			dataSource: new bcs.DataSource({
				type: "array",
				definition: {
					data: [],
					pageSize: 10,
					schema: {
						model: {
							fields: {
								NombreOperacion: { type: "string" },
								Nemotecnico: { type: "String" },
								Emisor: { type: "String" },
								Secuencia: { type: "string" },
								DocumentoIdentidad: { type: "string" },
								NombreCompleto: { type: "string" },
								IdPortafolioCliente: { type: "number" },
								FechaLiquidacion: { type: "Sysdate" },
								TasaOperacion: { type: "number" },
								MontoAsignado: { type: "number" },
								CantidadAsignada: { type: "number" },
								Glosa: { type: "string" }
							}
						}
					}
				},
			}),
			columns: [
				{ field: "NombreOperacion", title: "UI_TIPO_OPERACION", width: "95px" }
				, { field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "75px" }
				, { field: "Emisor", title: "UI_EMISOR", width: "70px" }
				, { field: "Secuencia", title: "UI_SECUENCIA", width: "60px" }
				, { field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "110px", groupable: true }
				, { field: "NombreCompleto", title: "UI_NOMBRE", width: "250px", groupable: true }
				, { field: "IdPortafolioCliente", title: "UI_PORTAFOLIO", width: "60px", groupable: true }
				, { field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "95px" }
				, { field: "TasaOperacion", title: "UI_TASA_OPERACION", width: "40px" }
				, { field: "MontoAsignado", title: "UI_MONTO_REASIGNADO", width: "95px", template: "<label> #: (util.bcs_formato(MontoAsignado, 'GEN','MONTO','CLP','','')).valor #</label>" }
				, { field: "CantidadAsignada", title: "UI_CANTIDAD_REASIGNADA", width: "105px", template: "<label> #: (util.bcs_formato(CantidadAsignada, 'GEN','MONTO','CLP','','')).valor #</label>" }
				,{field: "Glosa", 		title: "UI_COMENTARIO", 	width: "80px",	groupable: true}
			],
			sortable: true,
			pageable: {
				numeric: true,
				pageSize: 10
			},
			refresh: false,
			height: 200,
			selectable: "row",
			userSettings: true,
			gridCreated: function (g) {
				gridReasignados = g;
			}
		};


		cMesa.ds.Mesas = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=452",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdMesa: { type: "number" },
							Codigo: { type: "string" },
							Nombre: { type: "string" },
							IdTipoProducto: { type: "string" },
							IdEstadoMesa: { type: "string" },
							IdEstadoRegistro: { type: "string" },
							FechaModificacionFrmt: { type: "string" }
						}
					}
				}
			}
		});

		// --------------------------------------------------
		// FUNCIONES 
		// --------------------------------------------------

		$scope.limpiarFiltros = function () {
			$scope.frmMesa_TipoOperacion = 0;
			$scope.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.confBasicaReasignacionOp.dataSource.removeAll();
			$scope.confBasicaReasignacionCarteras.dataSource.removeAll();
			$scope.confReasignados.dataSource.removeAll();
			$scope.btn.Detalle = false;
			$scope.btn.QuitarOp = false;
			$scope.btn.Reasig = false;
			$scope.btn.QuitarReasig = false;
			$scope.infoOperacion.tipoOperacion = '';
			$scope.infoOperacion.secuenciaOp = '';
			$scope.infoOperacion.totalNominal = '';
			$scope.infoOperacion.totalReasignado = '';
			$scope.infoOperacion.diferencia = '';
			totalReasignado = 0;
			diferencia = 0;
			parcial = false;
			carteras = [];
		};

		// BUSCAR OPERACIONES:
		$scope.$watch("data.oConfiguracionTipoOperacion.seleccionado", function () {
			var nval = $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion;
			if (nval === 'COMTER' || nval === 'VENTER' || nval === 0) {
				$scope.btn.BuscarOp = true;
			} else {
				$scope.btn.BuscarOp = false;
			}
		})

		$scope.BuscarOperaciones = function () {
			var objfilters = {};
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
			var tipoOperacion = $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion || 0;
			if ($scope.kngFecha !== null) {
				niv1filters.push({ field: "FechaOperacion", operator: "eq", value: util.dateSPdate($scope.kngFecha) });
				niv1filters.push({ field: "IdEstado", operator: "eq", value: "VIG" });
			}
			if (tipoOperacion !== null) {
				if (tipoOperacion !== 0) {
					niv1filters.push({ field: "IdTipoOperacion", operator: "eq", value: tipoOperacion });
				} else {
					niv1filters.push({ field: "IdTipoOperacion", operator: "neq", value: "COMPAC" });
					niv1filters.push({ field: "IdTipoOperacion", operator: "neq", value: "VENPAC" });
				}

			}
			if (niv1filters.length > 0) {
				$scope.confBasicaReasignacionOp.dataSource.query({ filter: objfilters })
				$scope.confBasicaReasignacionOp.dataSource.pageSize(10);
				// Limpiar grillas 2 y 3:
				$scope.confBasicaReasignacionCarteras.dataSource.removeAll();
				$scope.confReasignados.dataSource.removeAll();
				$scope.btn.Reasig = false;
				$scope.btn.QuitarReasig = false;
				$scope.infoOperacion.tipoOperacion = '';
				$scope.infoOperacion.secuenciaOp = '';
				$scope.infoOperacion.totalNominal = '';
				$scope.infoOperacion.totalReasignado = '';
				$scope.infoOperacion.diferencia = '';
				totalReasignado = 0;
				diferencia = 0;
				parcial = false;
				carteras = [];
			}
			else $scope.confBasicaReasignacionOp.dataSource.filters(new Array());
		};

		$scope.btnQuitarOperaciones = function () {
			$scope.confBasicaReasignacionOp.dataSource.remove($scope.data.seleccionOperaciones);
		}

		// DETALLE OPERACION:
		$scope.$watch("data.seleccionOperaciones", function () {
			if ($scope.data.seleccionOperaciones) {
				$scope.btn.Detalle = true;
				$scope.btn.QuitarOp = true;
			} else {
				$scope.btn.Detalle = false;
				$scope.btn.QuitarOp = false;
			}
		})

		async function comprobarCierreMesas() {
			var mesas = [];
			const IdTipoProductoMesaModulo = 'MERF';
			var filterMesas = [
				{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
				{ field: "IdTipoProductoMesaModulo", operator: "eq", value: IdTipoProductoMesaModulo }
			];

			await cMesa.ds.Mesas.query({ filter: filterMesas }).then(function () {
				if (cMesa.ds.Mesas.view().length > 0) {
					$.each(cMesa.ds.Mesas.data(), function (key, value) {
						var aux = {};
						aux.IdMesa = value.IdMesa;
						aux.NombreMesa = value.NombreMesa;
						aux.IdEstadoMesa = value.IdEstadoMesa;
						aux.MesaCerrada = value.IdEstadoMesa === 'C' ? true : false;
						aux.MsjEstadoMesa = value.IdEstadoMesa === 'C' ? $translate.instant("UI_MESA_CERRADA") : $translate.instant("UI_MESA_ABIERTA");

						mesas.push(aux);
					});
					cMesa.mesas = mesas;
				}
			})
		}

		$scope.btnDetalleOperaciones = async function () {

			// COMPROBAR CIERRE DE MESA:
			await comprobarCierreMesas();

			parametros.opeDetalle = $scope.data.seleccionOperaciones;
			if (parametros.opeDetalle === undefined) {
				bcsComunes.msg("warning", $translate.instant(""), 'Debe seleccionar una operacion');
			} else {

				var mesaOperacion = parametros.opeDetalle.IdMesaOrigen;
				mesaOperacion = cMesa.mesas.filter(mesa => mesa.IdMesa == mesaOperacion);

				mesaOperacion = mesaOperacion[0];

				if (mesaOperacion.MesaCerrada) {
					bcsComunes.ventanaMsgErr(`"${mesaOperacion.NombreMesa}" ` + $translate.instant("UI_MESA_CERRADA"), $scope.ventanaId)
					$scope.confBasicaReasignacionCarteras.dataSource.removeAll();
					$scope.confReasignados.dataSource.removeAll();
				} else {
					var objfiltersDetalle = {};
					var niv1filtersDetalle = new Array();
					objfiltersDetalle.logic = 'and';
					objfiltersDetalle.filters = niv1filtersDetalle;
					var idEmpresa = parametros.opeDetalle.IdEmpresa;
					var idTipoProducto = parametros.opeDetalle.IdTipoProducto;
					var idOperacion = parametros.opeDetalle.IdOperacion;
					if (idEmpresa !== null && idTipoProducto !== null && idOperacion !== null) {
						niv1filtersDetalle.push({ field: "IdEmpresa", operator: "eq", value: idEmpresa });
						niv1filtersDetalle.push({ field: "IdTipoProducto", operator: "eq", value: idTipoProducto });
						niv1filtersDetalle.push({ field: "IdOperacion", operator: "eq", value: idOperacion });
					}
					if (niv1filtersDetalle.length > 0) {
						$scope.confBasicaReasignacionCarteras.dataSource.query({ filter: objfiltersDetalle })
						$scope.confBasicaReasignacionCarteras.dataSource.pageSize(10);
					}
				}
			}
		}

		function comprobarMovimientosEnTransito(carteras) {

			const ope = $scope.data.seleccionOperaciones;

			if (ope.IdTipoOperacion == 'COMTER' || ope.IdTipoOperacion == 'VENTER') {
				let aux = carteras.filter(x => x.FolioCarteraCustodiaDetalle == 0 && x.CarteraNominal == "S");
				if (aux.length > 0) { // SI LA CARTERA ESTA EN TRANSITO:
					enTransito = true;
					bcsComunes.msg("error", $translate.instant(""), 'No es posible continuar con la reasignacion, la operacion se encuentra con movimientos en transito.');
				} else { enTransito = false; }
			} else {
				enTransito = false;
			}
		}

		function formatearFecha(fecha) {
			var fechaFormat = fecha.split("(");
			fechaFormat = fechaFormat[1].split(")");
			fechaFormat = parseInt(fechaFormat[0]);
			fechaFormat = new Date(fechaFormat);
			return fechaFormat;
		}

		// LIMPIA GRILLAS 2 Y 3 AL SELECCIONAR OTRA OPERACION:
		$scope.$watch("data.seleccionOperaciones", function () {
			$scope.confBasicaReasignacionCarteras.dataSource.removeAll();
			$scope.confReasignados.dataSource.removeAll();
			$scope.btn.Reasig = false;
			$scope.btn.QuitarReasig = false;
			$scope.infoOperacion.tipoOperacion = '';
			$scope.infoOperacion.secuenciaOp = '';
			$scope.infoOperacion.totalNominal = '';
			$scope.infoOperacion.totalReasignado = '';
			$scope.infoOperacion.diferencia = '';
			totalReasignado = 0;
			diferencia = 0;
			parcial = false;
		})

		// BOTON REASIGNAR: // Aqui tambien se carga la data en INFO OPERACION
		// Definimos data para info operacion:
		$scope.$watch("data.seleccionCartera", function () {

			// Para crear un objeto con la info de la operacion:
			function Cartera(folioCartera, totalNominal, totalReasignado, diferencia, parcial, folioTemporal) {
				this.folioCartera = folioCartera;
				this.totalNominal = totalNominal;
				this.totalReasignado = totalReasignado;
				this.diferencia = diferencia;
				this.parcial = parcial;
				this.folioTemporal = folioTemporal
			}

			parametros.ope = $scope.data.seleccionOperaciones;
			parametros.cartera = $scope.data.seleccionCartera;


			if ($scope.data.seleccionOperaciones) {

				if (parametros.cartera.FolioCarteraCustodiaDetalle != 0) { // FLUJO NORMAL

					cartera = carteras.find(e => e.folioCartera == parametros.cartera.FolioCarteraCustodiaDetalle);

					if (!cartera) {

						if (parametros.cartera.IdClasificacionTipoInstrumentoDetalle == 'RF') {
							datosCartera = new Cartera(parametros.cartera.FolioCarteraCustodiaDetalle, parametros.cartera.CantidadReservadaDetalle, totalReasignado, diferencia, parcial);
							carteras.push(datosCartera);
							cartera = carteras.find(e => e.folioCartera == parametros.cartera.FolioCarteraCustodiaDetalle)
						} else {
							datosCartera = new Cartera(parametros.cartera.FolioCarteraCustodiaDetalle, parametros.cartera.ValorRescateDetalle, totalReasignado, diferencia, parcial);
							carteras.push(datosCartera);
							cartera = carteras.find(e => e.folioCartera == parametros.cartera.FolioCarteraCustodiaDetalle)
						}

					}
				} else { // CUANDO SE UTILIZA FOLIO TEMPORAL PARA LA INFO DE LA CARTERA.

					cartera = carteras.find(e => e.folioTemporal == parametros.cartera.folioTemporal);

					if (!cartera) {

						if (parametros.cartera.IdClasificacionTipoInstrumentoDetalle == 'RF') {
							let folioTemporal = Math.round(Math.random() * 1000);
							parametros.cartera.folioTemporal = folioTemporal;
							datosCartera = new Cartera(parametros.cartera.FolioCarteraCustodiaDetalle, parametros.cartera.CantidadReservadaDetalle, totalReasignado, diferencia, parcial, parametros.cartera.folioTemporal);
							carteras.push(datosCartera);
							cartera = carteras.find(e => e.folioTemporal == parametros.cartera.folioTemporal)
						} else {
							let folioTemporal = Math.round(Math.random() * 1000);
							parametros.cartera.folioTemporal = folioTemporal;
							datosCartera = new Cartera(parametros.cartera.FolioCarteraCustodiaDetalle, parametros.cartera.ValorRescateDetalle, totalReasignado, diferencia, parcial, parametros.cartera.folioTemporal);
							carteras.push(datosCartera);
							cartera = carteras.find(e => e.folioTemporal == parametros.cartera.folioTemporal)
						}
					}
				}

				cargarInfoOperacion(parametros, cartera);
				if (!enTransito) {
					$scope.btn.Reasig = true;
				}
			} else {
				$scope.btn.Reasig = false;
			}
		})

		function cargarInfoOperacion(parametros, cartera) {
			$scope.infoOperacion.tipoOperacion = `${parametros.ope.NombreOperacion} (${parametros.ope.IdOperacion})`;
			$scope.infoOperacion.totalNominal = cartera.totalNominal;
			$scope.infoOperacion.totalReasignado = cartera.totalReasignado;
			$scope.infoOperacion.diferencia = cartera.diferencia;

			// SE FORMATEAN LOS VALORES Y SE MUESTRAN EN LA INFORMACION DE LA OPERACION.
			$scope.infoOperacion.totalNominalEnPantalla = util.bcs_formato($scope.infoOperacion.totalNominal, 'GEN', 'MONTO', 'CLP', '', '').valor;
			$scope.infoOperacion.totalReasignadoEnPantalla = util.bcs_formato($scope.infoOperacion.totalReasignado, 'GEN', 'MONTO', 'CLP', '', '').valor;
			$scope.infoOperacion.diferenciaEnPantalla = util.bcs_formato($scope.infoOperacion.diferencia, 'GEN', 'MONTO', 'CLP', '', '').valor;

		}

		$scope.btnReasignarClientes = function () {
			parametros.cartera = $scope.data.seleccionCartera;
			parametros.opeClientes = $scope.data.seleccionOperaciones;
			var fechaLiquidacion = parametros.cartera.FechaLiquidacion;
			fechaLiquidacion = formatearFecha(fechaLiquidacion);

			if (cartera.parcial == true) {
				cantidad = cartera.diferencia;
			} else {
				cantidad = cartera.totalNominal;
			}

			if (cartera.totalReasignado >= $scope.infoOperacion.totalNominal) {
				bcsComunes.msg("error", $translate.instant(""), 'Ya se reasignaron todos los nominales');
			} else {
				if (parametros.cartera === undefined || parametros.opeClientes === undefined) {
					bcsComunes.msg("warning", $translate.instant(""), 'Debe seleccionar una operacion y una cartera');
				} else {
					var id1 = bcsComunes.ventanaAbrir("SelectorClientesReasignacionRF", { cantidad: cantidad, corteMinimo: parametros.cartera.CorteMinimoDetalle }, false, false);
					bcsComunes.ventanaPromesa(id1).then(function (data) {
						if (!data || !data[0]) return;

						var porcentajeAsignado = (data[1] * 100) / cartera.totalNominal;
						var montoAsignado = (parametros.cartera.MontoCompraDetalle / 100) * porcentajeAsignado;

						let reasignacion = [{
							"NombreOperacion": parametros.cartera.NombreTipoOperacion,
							"Nemotecnico": parametros.cartera.NemotecnicoDetalle,
							"Emisor": parametros.cartera.IdEmisorDetalle,
							"Secuencia": parametros.opeClientes.IdOperacion,
							"DocumentoIdentidad": data[0].DocumentoIdentidad,
							"NombreCompleto": data[0].NombreCompleto,
							"IdPortafolioCliente": data[0].IdPortafolio,
							"IdCliente": data[0].IdCliente,
							"FechaLiquidacion": util.dateSPdate(fechaLiquidacion || '0001-01-01'),
							"TasaOperacion": parametros.cartera.TasaCompraDetalle,
							"MontoAsignado": montoAsignado,
							"CantidadAsignada": data[1],
							"FolioCartera": parametros.cartera.FolioCarteraCustodiaDetalle,
							"FolioOperacion": parametros.cartera.FolioOperacion,
							"AfectaCustodia": parametros.opeClientes.AfectaCustodia === 'S' ? true : false,
							"IdInstrumento": parametros.cartera.IdInstrumentoDetalle,
							"TipoOperacion": parametros.opeClientes.IdTipoOperacion,
							"Glosa": parametros.opeClientes.Glosa
						}];


						var parametrosServicio = {};
						parametrosServicio.p_IdEmpresa = $scope.SesionIdEmpresa;
						parametrosServicio.p_IdUsuario = $scope.SesionIdUsuario;
						parametrosServicio.p_IdModulo = 'MRF';
						parametrosServicio.p_FolioOperacion = parametros.opeClientes.FolioOperacion;
						parametrosServicio.p_FolioCartera = parametros.cartera.FolioCarteraCustodiaDetalle;
						parametrosServicio.p_CantidadAsignada = data[1];
						parametrosServicio.p_AfectaCustodia = parametros.opeClientes.AfectaCustodia === 'S' ? true : false,
							parametrosServicio.p_IdInstrumento = parametros.cartera.IdInstrumentoDetalle;
						parametrosServicio.p_Procesar = false;
						parametrosServicio.p_IdCliente = reasignacion[0].IdCliente;
						parametrosServicio.p_IdPortafolio = reasignacion[0].IdPortafolioCliente;
						parametrosServicio.p_TipoOperacion = parametros.opeClientes.IdTipoOperacion;


						bcsComunes.log().info(JSON.stringify(parametrosServicio));

						bcsServiciosMesa.ValidarReasignacionMesaRF(parametrosServicio, function (respuesta) {
							bcsComunes.ventanaOverlay($scope.IdVentana, false);

							if (respuesta.P_OUT_ERRORESCONTADOR === 0) {

								// bcsComunes.msg("success", $translate.instant(""), respuesta.P_OUT_MENSAJES[0].descripcion); // MUESTRA EL MENSAJE SUCCESS DE LA VALIDACION.
								$scope.btn.AceptarReasig = true;

								if (data[2] == true) {
									cartera.parcial = true;
									cartera.totalReasignado = cartera.totalReasignado + data[1];
									cartera.diferencia = cantidad - data[1];

									if (cartera.diferencia == 0) {
										parcial = false;
									}

									cargarInfoOperacion(parametros, cartera);
									$scope.confReasignados.dataSource.add(reasignacion[0]);
								} else {
									cartera.totalReasignado = cartera.totalReasignado + data[1];
									cartera.diferencia = cantidad - data[1];
									cargarInfoOperacion(parametros, cartera);
									$scope.confReasignados.dataSource.add(reasignacion[0]);
								}

							} else {
								bcsComunes.msg("error", $translate.instant(""), respuesta.P_OUT_MENSAJES[0].descripcion);
							}
						},
							function (respuesta) {
								bcsComunes.msg("error", $translate.instant(""), respuesta.P_OUT_MENSAJES[0].descripcion);
							}
						);
					});
				}
			}
		}

		// BOTON QUITAR REASIGNACIONES:
		$scope.$watch("data.seleccionReasignados", function () {
			if ($scope.data.seleccionReasignados) {
				$scope.btn.QuitarReasig = true;
			} else {
				$scope.btn.QuitarReasig = false;
			}
		})

		$scope.btnQuitarReasignacion = function () {
			carteraReasignada = carteras.find(e => e.folioCartera == $scope.data.seleccionReasignados.FolioCartera);
			carteraReasignada.totalReasignado = carteraReasignada.totalReasignado - $scope.data.seleccionReasignados.CantidadAsignada;
			carteraReasignada.diferencia = carteraReasignada.diferencia + $scope.data.seleccionReasignados.CantidadAsignada;
			cargarInfoOperacion(parametros, cartera);
			$scope.confReasignados.dataSource.remove($scope.data.seleccionReasignados);
		}

		$scope.cargargrilla = function () {
			if (parametros.operacion !== undefined) {
				let operacion = [{
					"NombreOperacion": parametros.operacion.NombreOperacion
					, "FolioOperacion": parametros.operacion.FolioOperacion
					, "IdOperacion": parametros.operacion.IdOperacion
					, "FechaOperacion": util.dateSPdate(parametros.operacion.FechaOperacion || '0001-01-01')
					, "DocumentoIdentidad": parametros.operacion.DocumentoIdentidad
					, "CodigoPortafolio": parametros.operacion.IdPortafolio
					, "MontoOperacion": parametros.operacion.MontoOperacion
					, "Tasa": parametros.operacion.Tasa
					, "IdTipoReajuste": parametros.operacion.IdTipoReajuste
					, "MontoOperacionInicial": parametros.operacion.MontoOperacionInicial
					, "IdMoneda": parametros.operacion.IdMoneda
					, "MontoOperacionFinal": parametros.operacion.MontoOperacionFinal
					, "FechaVencimiento": util.dateSPdate(parametros.operacion.FechaOperacion || '0001-01-01')
				}];

				$scope.confBasicaReasignacionOp.dataSource.add(parametros.operacion);
				gridReasignacionOP.refresh();
			}
		}

		// BOTON ACEPTAR REASIGNACIONES:
		$scope.btnAceptarReasig = function () {
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
			let datosC = $scope.confBasicaReasignacionCarteras.dataSource.view();
			let datosR = $scope.confReasignados.dataSource.view();

			let calculoNominales = [];
			let sumaTotal = 0;
			var checkReasignadas = 0;

			function CalculoNominales(folioCartera, totalNominal, sumaTotal) {
				this.folioCartera = folioCartera;
				this.totalNominal = totalNominal;
				this.sumaTotal = sumaTotal;
			}

			for (let i = 0; i < datosC.length; i++) {
				papelCartera = calculoNominales.find(e => e.folioCartera == datosC[i].FolioCarteraCustodiaDetalle);
				if (!papelCartera) {
					if (datosC[i].IdClasificacionTipoInstrumentoDetalle == 'RF') {
						let datosPapel = new CalculoNominales(datosC[i].FolioCarteraCustodiaDetalle, datosC[i].CantidadReservadaDetalle, sumaTotal);
						calculoNominales.push(datosPapel);
					} else {
						let datosPapel = new CalculoNominales(datosC[i].FolioCarteraCustodiaDetalle, datosC[i].ValorRescateDetalle, sumaTotal);
						calculoNominales.push(datosPapel);
					}
				}
			}

			for (let i = 0; i < datosR.length; i++) {
				papelCartera = calculoNominales.find(e => e.folioCartera == datosR[i].FolioCartera);
				papelCartera.sumaTotal = papelCartera.sumaTotal + datosR[i].CantidadAsignada;
			}

			calculoNominales.forEach(papelC => {
				if (papelC.sumaTotal == papelC.totalNominal) {
					checkReasignadas = checkReasignadas + 1;
					if (checkReasignadas == datosC.length) {

						var parametrosServicioProcesar = {};
						parametrosServicioProcesar.p_IdEmpresa = $scope.SesionIdEmpresa;
						parametrosServicioProcesar.p_IdUsuario = $scope.SesionIdUsuario;
						parametrosServicioProcesar.p_IdModulo = 'MRF';
						parametrosServicioProcesar.p_IdOperacion = datosC[0].IdOperacion;
						parametrosServicioProcesar.p_Carteras = datosR;
						parametrosServicioProcesar.p_CantidadAProcesar = datosR.length;

						bcsComunes.log().info(JSON.stringify(parametrosServicioProcesar));

						bcsServiciosMesa.ProcesarReasignacionMesaRF(parametrosServicioProcesar, function (respuesta) {
							bcsComunes.ventanaOverlay($scope.IdVentana, false);

							if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
								bcsComunes.msg("success", $translate.instant(""), respuesta.P_OUT_MENSAJES[0].descripcion);
								$scope.limpiarFiltros();
								bcsComunes.ventanaCerrar($scope.IdVentana);
							} else {
								bcsComunes.msg("error", $translate.instant(""), respuesta.P_OUT_MENSAJES[0].descripcion);
							}
						},
							function (respuesta) {
								bcsComunes.msg("error", $translate.instant(""), respuesta.P_OUT_MENSAJES[0].descripcion);
							}
						);
					}
				} else {
					bcsComunes.msg("error", $translate.instant(""), 'Quedan nominales o pesos por asignar en la cartera con folio: ' + papelC.folioCartera);
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				}
			});

		}

	});

