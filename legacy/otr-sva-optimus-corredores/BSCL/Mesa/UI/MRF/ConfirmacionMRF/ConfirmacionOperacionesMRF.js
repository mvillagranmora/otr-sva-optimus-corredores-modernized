(function () {
	angular.module('ModuloMesa').controller('ConfirmacionMRFCtrl',
		function ($scope,
			$translate,
			$resource,
			$timeout,
			bcsServiciosGrillasMesaRF,
			bcsComunes,
			bcsAlerta, OPCIONES_BCSALERTA,
			bcsServiciosMesa,
			bcsConsultasMesa) {
			//Obtiene datos desde la ventana principal
			var idVentana = bcsComunes.ventanaWinId($scope);
			var overlay = bcsComunes.overlay;
			var parametrosVentana = bcsComunes.ventanaParams(idVentana);
			var grillaSpot = undefined;
			var OperacionesOk = false;

			$scope.data = {};
			$scope.data.seleccionMovimiento = false;

			//Configurar al principio
			$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
			$scope.HabilitoBotones = true;

			var dataSourceConsultarEstadoMesa = bcsServiciosGrillasMesaRF.getDataSourceConsultarEstadoMesa();

			//Combobox Tipo Instrumento
			$scope.data.oConfiguracionTipoInstrumento = {
				filtros: {
					IdTipoInstrumento: { "field": "IdTipoInstrumento", "operator": "eq", "value": "SPT" }
				}
			};

			$scope.data.oSocioNegocio = {
				filtros: {
					IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }
				},
			};


			$scope.data.oConfiguracionTipoReajusteOperacion = {
				tipo: "combobox-tipo-reajuste",
				configuracion: {
					template: '<span >#: IdTipoReajuste # - #: Nombre #</span>'
				},
				idActual: false,
				seleccionado: false,
				filtros: {
					IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" }//,

				},
				idDefecto: ''
			};


			$scope.data.oConfiguracionMonedaSecundaria = {
				filtros: {
					IdTipoInstrumento: { "field": "IdTipoInstrumento", "operator": "eq", "value": "" },
					Nemotecnico: { "field": "Nemotecnico", "operator": "eq", "value": '' },
					OrdenEnPar: { "field": "OrdenEnPar", "operator": "eq", "value": 2 }
				}
			};

			$scope.data.oConfiguracionTipoOperacion = {
				tipo: "combobox-tipo-operacion",
				configuracion: {},
				idActual: false,
				seleccionado: false,
				filtros: {
					IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": 'MERF' },
					EsAnticipo: { "field": "EsAnticipo", "operator": "eq", "value": 'N' },
					EsAnulacion: { "field": "EsAnulacion", "operator": "eq", "value": 'N' },
					Accion: { "field": "Accion", "operator": "eq", "value": '' }
				},
				idDefecto: ''
			};


			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////// SELECCION DE UN REGISTRO ////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			$scope.$watchCollection('data.seleccionMovimiento', function (newval, oldval) {
				if (newval === undefined) return;
				if (newval === false) return;

				if (newval[0].Estado === 'PEN' && newval[0].EstadoConfirmacion === 'NCF') {
					$scope.HabilitoBotones = true;
				} else {
					$scope.HabilitoBotones = false;
				}
			});

			var Transaccion_OK = function (respuesta) {
				if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
					id = bcsComunes.ventanaMsg({ conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
				}
				else {
					id = bcsComunes.ventanaMsg({ conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
				}
				bcsComunes.ventanaPromesa(id).then(function (result) {
					bcsComunes.ventanaOverlay($scope.winID, false);
					// Vuelve a cargar datos en la grilla...
					$scope.data.BuscarOperaciones();
				});
			};


			var Transaccion_Err = function (respuesta) {
				bcsComunes.ventanaMsg({
					conflictos: util.parseServerResponse(respuesta)
				}, $scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
				$scope.data.BuscarOperaciones();
			};

			var verificarBloqueoOperacion = function (idOperacion, callback) {
				var dsVerificarBloqueo = new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering: true,
						transport: {
							read: {
								url: bcsConsultasMesa('ConsultarOperacionesMesaRF'),
								dataType: "json"
							}
						},
						schema: {
							model: {
								fields: {
									IdOperacion: { type: "number" },
									FolioOperacion: { type: "number" },
									BloqueoPorEdicion: { type: "string" },
									CodigoUsuarioBloqueoEdicion: { type: "string" }
								}
							}
						}
					}
				});
				dsVerificarBloqueo.query({ filter: { field: "IdOperacion", operator: "eq", value: idOperacion } }).then(function () {
					var data = dsVerificarBloqueo.data();
					callback(data.length > 0 ? data[0] : null);
				});
			};

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////// CONFIRMAR OPERACIONES ///////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
			$scope.data.ConfirmarOperacion = function () {
				var ope = {};
				if ($scope.data.seleccionMovimiento === undefined) {
					bcsComunes.msg("warning", "info", $translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
				}
				else {
					var t = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_CONFIRMAR_OPERACION_RF'));
					t.result.then(function (result) {
						if (result === OPCIONES_BCSALERTA.ACEPTAR) {
							$.each($scope.data.seleccionMovimiento, function (key, value) {
								ope.IdOperacion = value.IdOperacion;
								ope.IdCliente = value.IdCliente;
							});
							verificarBloqueoOperacion(ope.IdOperacion, function (operacionBloqueada) {
								if (operacionBloqueada && operacionBloqueada.BloqueoPorEdicion === 'S') {
									bcsComunes.ventanaMsg({
										conflictos: util.parseServerResponse({P_OUT_ERRORESDETALLE: [{
											Codigo: 'UI_OPERACION_BLOQUEADA_POR_USUARIO',
											Descripcion: $translate.instant('UI_OPERACION_BLOQUEADA_POR_USUARIO')
												.replace("::IdOperacion::", operacionBloqueada.IdOperacion)
												.replace("::CodigoUsuarioBloqueoEdicion::", operacionBloqueada.CodigoUsuarioBloqueoEdicion)
										}]})
									}, $scope.winID);
									return;
								}
								bcsComunes.ventanaOverlay($scope.winID, true);
								// Registros seleccionados
								var parametros = {};
								parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
								parametros.p_IdUsuario = $scope.SesionIdUsuario;
								parametros.p_IdModulo = 'MRF';
								parametros.p_IdOperacion = ope.IdOperacion;
								parametros.p_IdCliente = ope.IdCliente;
								parametros.p_IdMesa = 'MERF';
								bcsComunes.log().info(JSON.stringify(parametros));
								bcsServiciosMesa.ConfirmarOperacionesFWRF(parametros, Transaccion_OK, Transaccion_Err);
							});
						}
					});
					return;
				}
			};

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////// ANULAR OPERACIONES //////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			$scope.data.RechazarOperacion = function () {
				var datos = {};
				if ($scope.data.seleccionMovimiento === undefined) {
					bcsComunes.msg("warning", "info", $translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
				}
				else {
					if ($scope.data.seleccionMovimiento.length > 1) {
						bcsComunes.msg("warning", "info", $translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
					}
					else {
						var t = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_SE_RECHAZARA_LA_OPERACION'));
						t.result.then(function (result) {
							bcsComunes.ventanaOverlay($scope.winID, true);
							if (result === OPCIONES_BCSALERTA.ACEPTAR) {
								verificarBloqueoOperacion($scope.data.seleccionMovimiento[0].IdOperacion, function (operacionBloqueada) {
									if (operacionBloqueada && operacionBloqueada.BloqueoPorEdicion === 'S') {
										bcsComunes.ventanaOverlay($scope.winID, false);
										bcsComunes.ventanaMsg({
											conflictos: util.parseServerResponse({P_OUT_ERRORESDETALLE: [{
												Codigo: 'UI_OPERACION_BLOQUEADA_POR_USUARIO',
												Descripcion: $translate.instant('UI_OPERACION_BLOQUEADA_POR_USUARIO')
													.replace("::IdOperacion::", operacionBloqueada.IdOperacion)
													.replace("::CodigoUsuarioBloqueoEdicion::", operacionBloqueada.CodigoUsuarioBloqueoEdicion)
											}]})
										}, $scope.winID);
										return;
									}
									datos.p_IdEmpresa = $scope.SesionIdEmpresa;
									datos.p_IdUsuario = $scope.SesionIdUsuario;
									datos.p_IdModulo = 'MRF';
									datos.p_UsaReglasNegocio = 'S';
									datos.p_Operaciones = [];
									$.each($scope.data.seleccionMovimiento, function (key, value) {
										var ope = {};
										ope.IdOperacion = value.IdOperacion;
										ope.IdEstadoSiguiente = 'ANU';
										ope.EstadoConfirmacion = 'RZD';
										datos.p_Operaciones.push(ope);
									});

									bcsServiciosMesa.AnularOperacionesMesaRF(datos, success, error);
									$scope.data.BuscarOperaciones
								});
							}
							else {
								bcsComunes.ventanaOverlay($scope.winID, false);
								$scope.data.BuscarOperaciones
							}
						});
						return;
					}
				}
			};
			var success = function (respuesta) {
				var operaciones = [];
				var id;
				//si vuelve ref id, se creo la asignacion
				if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
					$.each(respuesta.P_OUT_OPERACIONES, function (key, value) {
						operaciones.push({ id: value.IdOperacion, resultado: $translate.instant('UI_EXITO'), mensaje: $translate.instant('UI_OPERACION_ANULADA_EXITO') });
					});

					id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", {
						tituloVentana: $translate.instant("UI_MESA_RENTA_FIJA"),
						nombreEntidad: $translate.instant("UI_SECUENCIA"),
						operaciones: operaciones
					}, false, false, $scope.winID);

				}
				else {
					id = bcsComunes.ventanaMsg({
						conflictos: util.parseServerResponse(respuesta)
					}, $scope.winID);
				}
				bcsComunes.ventanaPromesa(id).then(function (result) {
					bcsComunes.ventanaOverlay($scope.winID, false);
					$scope.data.BuscarOperaciones();
				});


			};
			var error = function (respuesta) {
				var id = bcsComunes.ventanaMsg({
					conflictos: util.parseServerResponse(respuesta)
				}, $scope.winID);
				bcsComunes.ventanaPromesa(id).then(function (result) {
					bcsComunes.ventanaOverlay($scope.winID, false);
				});

			};
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////// BUSCAR OPERACIONES //////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			$scope.data.BuscarOperaciones = function () {
				var Parametros = {};
				Parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
				Parametros.p_IdUsuario = $scope.SesionIdUsuario;
				Parametros.p_IdModulo = "MES";
				Parametros.p_IdTipoProducto = "MERF";
				Parametros.p_IdTipoOperacion = $scope.data.oConfiguracionTipoOperacion.idActual || 0;
				Parametros.p_IdMonedaPrincipal = $scope.data.oConfiguracionTipoReajusteOperacion.idActual || "0";
				Parametros.p_MonedaSecundaria = "0";
				Parametros.p_EstadoConfirmacion = $scope.data.oConfiguracionEstado.idActual;
				Parametros.p_IdCliente = $scope.data.oSocioNegocio.seleccionado.IdCliente || 0;
				Parametros.p_Fecha = util.date($scope.data.kngFechaOperacion).format("YYYY-MM-DD");

				bcsComunes.ventanaOverlay(idVentana, true);
				bcsServiciosMesa.ConsultarOperacionesFWRF(Parametros).$promise.then(function (Resp) {
					var OperacionSpot = [];
					for (var pos in Resp.P_OUT_OPERACIONES) {
						var Campos_Merf = {};
						Campos_Merf.FolioOperacion = Resp.P_OUT_OPERACIONES[pos].FolioOperacion;
						Campos_Merf.FechaOperacion = Resp.P_OUT_OPERACIONES[pos].FechaOperacion;
						Campos_Merf.FechaLiquidacion = Resp.P_OUT_OPERACIONES[pos].FechaLiquidacion;
						Campos_Merf.FechaEntregamos = Resp.P_OUT_OPERACIONES[pos].FechaEntregamos;
						Campos_Merf.FechaRecibimos = Resp.P_OUT_OPERACIONES[pos].FechaRecibimos;
						Campos_Merf.IdCliente = Resp.P_OUT_OPERACIONES[pos].IdCliente;
						Campos_Merf.IdClienteCorredor = Resp.P_OUT_OPERACIONES[pos].IdClienteCorredor;
						Campos_Merf.IdInstrumento = Resp.P_OUT_OPERACIONES[pos].IdInstrumento;
						Campos_Merf.NombreInstrumento = Resp.P_OUT_OPERACIONES[pos].NombreInstrumento;
						Campos_Merf.Nemotecnico = Resp.P_OUT_OPERACIONES[pos].Nemotecnico;
						Campos_Merf.IdTipoOperacion = Resp.P_OUT_OPERACIONES[pos].IdTipoOperacion;
						Campos_Merf.TipoOperacion = Resp.P_OUT_OPERACIONES[pos].TipoOperacion;
						Campos_Merf.RutCliente = Resp.P_OUT_OPERACIONES[pos].RutCliente;
						Campos_Merf.NombreCliente = Resp.P_OUT_OPERACIONES[pos].NombreCliente;
						Campos_Merf.CodigoPropositoPortafolio = Resp.P_OUT_OPERACIONES[pos].Portafolio;
						Campos_Merf.MonedaPrincipal = Resp.P_OUT_OPERACIONES[pos].MonedaPrincipal;
						Campos_Merf.MonedaSecundaria = Resp.P_OUT_OPERACIONES[pos].MonedaSecundaria;
						Campos_Merf.MontoMonedaPrincipal = Resp.P_OUT_OPERACIONES[pos].MontoMonedaPrincipal;
						Campos_Merf.MontoMonedaSecundaria = Resp.P_OUT_OPERACIONES[pos].MontoMonedaSecundaria;
						Campos_Merf.TipoCambioCierre = Resp.P_OUT_OPERACIONES[pos].TipoCambioCierre;
						Campos_Merf.TipoCambioCosto = Resp.P_OUT_OPERACIONES[pos].TipoCambioCosto;
						Campos_Merf.ParidadCierre = Resp.P_OUT_OPERACIONES[pos].ParidadCierre;
						Campos_Merf.ParidadCosto = Resp.P_OUT_OPERACIONES[pos].ParidadCosto;
						Campos_Merf.MontoEquivalente = Resp.P_OUT_OPERACIONES[pos].MontoEquivalente;
						Campos_Merf.CodigoUsuario = Resp.P_OUT_OPERACIONES[pos].CodigoUsuario;
						Campos_Merf.Estado = Resp.P_OUT_OPERACIONES[pos].EstadoOperacion;
						Campos_Merf.IdMesa = Resp.P_OUT_OPERACIONES[pos].IdMesa;
						Campos_Merf.Mesa = Resp.P_OUT_OPERACIONES[pos].Mesa;
						Campos_Merf.Producto = Resp.P_OUT_OPERACIONES[pos].Producto;
						Campos_Merf.IdUsuario = Resp.P_OUT_OPERACIONES[pos].IdUsuario;
						Campos_Merf.IdPortafolio = Resp.P_OUT_OPERACIONES[pos].Portafolio;
						Campos_Merf.IdPortafolioCliente = Resp.P_OUT_OPERACIONES[pos].IdPortafolioCliente;
						Campos_Merf.IdPortafolioCorredor = Resp.P_OUT_OPERACIONES[pos].IdPortafolioCorredor;
						Campos_Merf.IdTipoInstrumento = Resp.P_OUT_OPERACIONES[pos].IdTipoInstrumento;
						Campos_Merf.EstadoConfirmacion = Resp.P_OUT_OPERACIONES[pos].EstadoConfirmacion;
						Campos_Merf.IdTipoCanalIngreso = Resp.P_OUT_OPERACIONES[pos].IdTipoCanalIngreso;
						Campos_Merf.IdMovimientoComprobante = Resp.P_OUT_OPERACIONES[pos].IdMovimientoComprobante;
						Campos_Merf.IdOperacion = Resp.P_OUT_OPERACIONES[pos].IdOperacion;
						Campos_Merf.IdTipoReajuste = Resp.P_OUT_OPERACIONES[pos].IdTipoReajuste;
						Campos_Merf.MontoOperacion = Resp.P_OUT_OPERACIONES[pos].MontoOperacion;
						Campos_Merf.Tasa = Resp.P_OUT_OPERACIONES[pos].Tasa;
						Campos_Merf.PlazoEnDias = Resp.P_OUT_OPERACIONES[pos].PlazoEnDias;
						Campos_Merf.FechaVencimiento = Resp.P_OUT_OPERACIONES[pos].FechaVencimiento;
						Campos_Merf.MontoOperacionMoneda = Resp.P_OUT_OPERACIONES[pos].MontoOperacionMoneda;

						OperacionSpot.push(Campos_Merf);
					}

					$scope.confGrillaSpot.dataSource.data(OperacionSpot);
					bcsComunes.ventanaOverlay(idVentana, false);
				}, function (Resp) {
					bcsComunes.msg("error", "error", $translate.instant('UI_ERROR_GENERACION'));
					bcsComunes.ventanaOverlay(idVentana, false);
				});
			};

			/*********************************************
			 * DATASOURCES
			 ********************************************/
			var dsDataMerf = new bcs.DataSource({
				type: "array",
				definition: {
					pageSize: 200,
					data: [],
					schema: {
						model: {
							fields: {
								FolioOperacion: { type: "number" },
								FechaOperacion: { type: "sysdate" },
								FechaLiquidacion: { type: "sysdate" },
								FechaEntregamos: { type: "sysdate" },
								FechaRecibimos: { type: "sysdate" },
								IdInstrumento: { type: "number" },
								IdTipoInstrumento: { type: "string" },
								NombreInstrumento: { type: "string" },
								Nemotecnico: { type: "string" },
								IdTipoOperacion: { type: "string" },
								TipoOperacion: { type: "string" },
								IdCliente: { type: "number" },
								RutCliente: { type: "string" },
								NombreCliente: { type: "string" },
								CodigoPropositoPortafolio: { type: "string" },
								IdPortafolio: { type: "string" },
								MonedaPrincipal: { type: "string" },
								MonedaSecundaria: { type: "string" },
								MontoMonedaPrincipal: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaPrincipal, '', '').excelFormat; } },
								MontoMonedaSecundaria: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaSecundaria, '', '').excelFormat; } },
								TipoCambioCierre: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaPrincipal, '', '').excelFormat; } },
								TipoCambioCosto: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaPrincipal, '', '').excelFormat; } },
								ParidadCierre: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaPrincipal, '', '').excelFormat; } },
								ParidadCosto: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaPrincipal, '', '').excelFormat; } },
								MontoEquivalente: { type: "number", excelFormat: function (item) { return util.bcs_formato(0, item.IdTipoInstrumento, 'Monto', item.MonedaSecundaria, '', '').excelFormat; } },
								CodigoUsuario: { type: "string" },
								Estado: { type: "string" },
								EstadoConfirmacion: { type: "string" },
								Mesa: { type: "string" },
								Producto: { type: "string" },
								IdTipoCanalIngreso: { type: "string" },
								IdMovimientoComprobante: { type: "number" },
								IdOperacion: { type: "number" },
								IdTipoReajuste: { type: "string" },
								MontoOperacion: { type: "number" },
								Tasa: { type: "number" },
								PlazoEnDias: { type: "number" },
								FechaVencimiento: { type: "sysdate" },
								MontoOperacionMoneda: { type: "number" },
								IdPortafolioCorredor: { type: "number" },
								IdPortafolioCliente: { type: "number" },
								IdClienteCorredor: { type: "number" },
								IdMesa: { type: "number" },
							}
						}
					}
				}
			});


			$scope.columnasMerf = function () {
				return [
					{ field: "FolioOperacion", title: "UI_FOLIO_OPERACION", width: "80px", sortable: true }
					, { field: "IdOperacion", title: "UI_SECUENCIA", width: "40px", sortable: false, groupable: true }
					, { field: "EstadoConfirmacion", title: "UI_ESTADO_CONFIRMACION", width: "40px", sortable: false, groupable: true }
					, { field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "90px", sortable: true, groupable: true }
					, { field: "NombreInstrumento", title: "UI_NOMBRE_INSTRUMENTO", width: "130px", sortable: true, groupable: true }
					, { field: "RutCliente", title: "UI_RUT_CLIENTE", width: "90px", sortable: false, groupable: true }
					, { field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "150px", sortable: false, groupable: true }
					, { field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO", width: "120px", sortable: false }
					, { field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "80px", sortable: false, hidden: true, groupable: true }
					, {
						field: "MontoOperacionMoneda", title: "UI_MONTO_OPERACION", width: "100px", sortable: true, template_: " <label> #:  (util.bcs_formato(MontoOperacionMoneda,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>",
						format: function (value, dataItem) {
							return util.bcs_formato(value, 'GEN', 'Monto', dataItem.IdTipoReajuste, '', '');
						}
					}
					, { field: "IdTipoReajuste", title: "UI_TIPO_REAJUSTE", width: "90px", sortable: false, groupable: true }
					, {
						field: "MontoOperacion", title: "UI_MONTO_EQUIVALENTE", width: "100px", sortable: true, template_: " <label> #:  (util.bcs_formato(MontoOperacion,'GEN','Monto',MonedaPrincipal,'','')).valor #</label>",
						format: function (value, dataItem) {
							return util.bcs_formato(value, 'GEN', 'Monto', dataItem.MonedaPrincipal, '', '');
						}
					}
					, { field: "MonedaPrincipal", title: "UI_MONEDA", width: "90px", sortable: false, groupable: true }
					, {
						field: "Tasa", title: "UI_TASA", width: "60px", sortable: true, format: function (value, dataItem) {
							return util.bcs_formato(value, 'GEN', 'Tasa', dataItem.IdTipoReajuste, dataItem.IdTipoInstrumento, '');
						}
					}
					, { field: "PlazoEnDias", title: "UI_PLAZO", width: "60px", sortable: true }
					, { field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "90px", sortable: true }
					, { field: "FechaVencimiento", title: "UI_FECHA_VENCIMIENTO", width: "90px", sortable: true }
					, { field: "Estado", title: "UI_ESTADO_OPERACION", width: "40px", sortable: false, groupable: true }
				]
			};


			$scope.confGrillaSpot = {
				dataSource: dsDataMerf,
				columns: $scope.columnasMerf(),
				sortable: true,
				pageable: true, //{ pageSize : 25 },   //pagina localmente
				excel: { allPages: true },
				filterable: true,
				selectable: "multiple, row",
				reorderable: false,
				userSettings: true,
				groupable: true,
				height: 400,
				navigatable: true,
				allowCopy: true,
				resizable: true,
				autoBind: true,
				scrollable: true,
				gridCreated: function (grid) {
					grillaSpot = grid;
				},
			};

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////// SUB-GRILLA FORMAS DE PAGO ///////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			var dsFormaDePago = function (dataItem) {
				var filtrosMovimientoForDePago = [
					{ field: "IdOperacion", operator: "eq", value: Number(dataItem.IdOperacion) }
					//{field: "IdTipoProducto", operator: "eq", value: 'MEME'}
				];
				return new bcs.DataSource({
					type: "odata",
					definition: {
						filter: filtrosMovimientoForDePago,
						serverFiltering: true,
						serverPaging: true,
						serverSorting: true,
						pageSize: 10,
						transport: { read: { url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3567", dataType: "json" } },
						schema: {
							model: {
								fields: {
									RecibePagaDesc: { type: "string" },
									RecibePagaPortafolioDesc: { type: "string" },
									NombreFormaDePago: { type: "string" },
									IdMonedaComprobantePago: { type: "string" },
									Monto: { type: "number" },
									FechaLiquidacionComprobantePagoOperacion: { type: "date" },
									IdMedioDePago: { type: "string" },   //columna oculta
									IdModalidadPago: { type: "string" },   //columna oculta
								}
							}
						}
					}
				});
			};


			$scope.columnasFormasDePago = function () {
				return [
					{ field: "RecibePagaDesc", title: 'UI_TIPO_MOVIMIENTO', width: "40px", align: "left", sortable: false }
					, { field: "NombreFormaDePago", title: 'UI_FORMA_DE_PAGO', width: "80px", align: "left", sortable: false }
					, { field: "IdMonedaComprobantePago", title: "UI_MONEDA", width: "30px", align: "left", sortable: false }
					, { field: "Monto", title: 'UI_MONTO_LIQUIDACION', width: "40px", align: "right", sortable: false, template: "<label> #: (util.bcs_formato(Monto,'GEN','Monto',IdMonedaComprobantePago,'','')).valor #</label>" }
					, { field: "FechaLiquidacionComprobantePagoOperacion", title: 'UI_FECHA_LIQUIDACION', width: "40px", align: "right" }

					//Columnas Ocultas  			
					, { field: "IdMedioDePago", title: 'UI_MEDIO_DE_PAGO', width: "80px", align: "right", sortable: false, hidden: true }
					, { field: "IdModalidadPago", title: 'UI_MODALIDAD', width: "80px", align: "right", sortable: false, hidden: true }
				];
			};

			//SUB-GRILLA FORMAS DE PAGO
			$scope.confGrillaFormasDePago = function (DataItem) {
				return {
					dataSource: dsFormaDePago(DataItem),
					columns: $scope.columnasFormasDePago(),
					sortable: true,
					pageable: true,
					height: 120,
					width: 200,
					selectable: "multiple row",
					autoBind: true,
					navigatable: true,
					allowCopy: true
				}
			};


			//////////////MODIFICAR MEDIOS DE PAGO/////////////////////
			$scope.validarOperacionSecundaria = function (principal) {
				if (principal == "COMTER") {
					return "VENTER";
				} else if (principal == "VENTER") {
					return "COMTER";
				} else if (principal == "COMPAC") {
					return "VENPAC";
				} else if (principal == "VENPAC") {
					return "COMPAC";
				} else {
					return "";
				}
			}

			$scope.modificarMediosDePago = function (accion) {
				if (accion.length > 0) {

					let operacion = accion[0];
					var ventana = "IngresoOperacionMesaRF";

					dataSourceConsultarEstadoMesa.query({ filter: { field: "IdMesa", operator: "eq", value: Number(operacion.IdMesa) } }).then(function () {
						var data = dataSourceConsultarEstadoMesa.data();
						if (data.length > 0) {
							if (data[0].TipoEstadoModulo === 'C') {
								bcsComunes.msg("warning", "info", $translate.instant('UI_MODULO_CERRADO'));
								return;
							}
							if (data[0].IdEstadoMesa === 'C') {
								bcsComunes.msg("warning", "info", $translate.instant('MESA_RF_CERRADA'));
								return;
							}
							if (data[0].FechaActualSistema !== $scope.SesionFechaSistema) {
								//bcsComunes.msg("warning","info",$translate.instant('FECHA_PROCESO_INVALIDA'));
								bcsComunes.ventanaMsg({
									conflictos: util.parseServerResponse({ P_OUT_ERRORESDETALLE: [{ Codigo: 'FECHA_PROCESO_INVALIDA', Descripcion: $translate.instant('FECHA_PROCESO_INVALIDA') }] })
								}, $scope.winID);
								return;
							}
							$scope.data.HabilitaBotones = false;

							var datos = {
								"p_IdEmpresa": $scope.SesionIdEmpresa
								, "p_IdUsuario": $scope.SesionIdUsuario
								, "p_IdModulo": 'MRF'
								, "p_Operaciones": [{
									"Accion": 'B',
									"IdOperacion": accion[0].IdOperacion,
								}]

							};
							bcsServiciosMesa.MarcarOperacion(datos, function (respuesta) {
								console.log("RESPUESTA", respuesta);
								if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
									//bcsComunes.msg("warning","info",$translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
									bcsComunes.ventanaMsg({
										conflictos: util.parseServerResponse(respuesta)
									}, $scope.winID);
									$scope.data.HabilitaBotones = true;
									return;
								}
								else {
									var id1 = bcsComunes.ventanaAbrir(ventana,
										{
											IdMesa: operacion.IdMesa,
											IdClienteCorredor: operacion.IdClienteCorredor,
											IdPortafolioCorredor: operacion.IdPortafolioCorredor,
											//FechaOperacion: fechaSist,
											IdOperacion: operacion.IdOperacion,
											CodigoIFRS: operacion.CodigoIFRS,
											Accion: "MODIFICAR",
											ModificacionFormasDePago: true
										}, false, false);//,$scope.winID);
									var promesa = bcsComunes.ventanaPromesa(id1);
									$.when(promesa).then(function (data) {
										$scope.data.HabilitaBotones = true;
										var datosLiberar = {
											"p_IdEmpresa": $scope.SesionIdEmpresa,
											"p_IdUsuario": $scope.SesionIdUsuario,
											"p_IdModulo": 'MRF',
											"p_Operaciones": [{
												"Accion": 'L',
												"IdOperacion": operacion.IdOperacion
											}]
										};
										bcsServiciosMesa.MarcarOperacion(datosLiberar, function (respuesta) {
											if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
												bcsComunes.ventanaMsg({
													conflictos: util.parseServerResponse(respuesta)
												}, $scope.winID);
											}
											$scope.data.BuscarOperaciones();
										}, function (respuesta) {
											bcsComunes.ventanaMsg({
												conflictos: util.parseServerResponse(respuesta)
											}, $scope.winID);
											$scope.data.BuscarOperaciones();
										});
									});
								}

							}, function (respuesta) {
								//bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_BLOQUEADA_ERROR'));
								//bcsComunes.setConflictos(util.parseServerResponse(respuesta));
								bcsComunes.ventanaMsg({
									conflictos: util.parseServerResponse(respuesta)
								}, $scope.winID);
								$scope.data.HabilitaBotones = true;
								bcsComunes.ventanaOverlay($scope.winID, false);
							});
						}

					});
				};


			}

		});
}());