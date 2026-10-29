angular
		.module('ModuloFacturacion')
		.controller(
				'ComprobanteFacturacionCtrl',
				function($scope, $state, $translate, $resource, $timeout,
						bcsComunes, bcsServiciosFacturacion, bcsAlerta,
						OPCIONES_BCSALERTA, bcsServicioPlataforma,
						bcsComprobanteFacturacionGrid, bcsOperacionesComunes) {
					$scope.data = {};
					// var overlay = bcsComunes.overlay;

					var idVentana = bcsComunes.ventanaWinId($scope);
					bcsComunes
							.ventanaAutoOverlay($scope.IdVentana, false, 3000);

					// Seteo de variables de sistema
					
					$scope.data.IdEmpresa = $scope.SesionIdEmpresa;
					$scope.data.IdUsuario = $scope.SesionIdUsuario;
					$scope.data.IdModulo = 'FAC';

					$scope.data.oFechaComprobanteDesde = util.dateDS(
							$scope.SesionFechaSistema, "FechaSistema", "CALL");
					$scope.data.oFechaComprobanteHasta = util.dateDS(
							$scope.SesionFechaSistema, "FechaSistema", "CALL");
					$scope.data.oFolioComprobanteDesde = 0;
					$scope.data.oFolioComprobanteHasta = 99999999999;
					$scope.data.btnMantenedorIgkActivo = false;
					$scope.sumaCantidad = 0;
					$scope.sumaMonto = 0;

					// COMBOBOX
					$scope.data.oClientePortafolio = {
						tipo : "combobox-cliente-portafolio",
						configuracion : {},
						idActual : false,
						seleccionado : false,
						filtros : {
							IdEstado : {
								"field" : "IdEmpresa",
								"operator" : "eq",
								"value" : $scope.SesionIdEmpresa
							},
							EsCliente : {
								"field" : "EsCliente",
								"operator" : "eq",
								"value" : 'UI_SI'
							}
						},
					};

					$scope.data.oConfiguracionTipoComprobante = {
						tipo : "combobox-tipo-comprobante",
						configuracion : {},
						filtros : {
							IdEstadoFacturacion : {
								field : "IdEstadoFacturacion",
								operator : "eq",
								value : "ACT"
							}
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					$scope.data.oConfiguracionMoneda = {
						tipo : "combobox-moneda",
						configuracion : {},
						filtros : {
							IdEstado : {
								field : "IdEstado",
								operator : "eq",
								value : "ACT"
							}
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					$scope.data.oConfiguracionEstadoComprobante = {
						tipo : "combobox-estado-comprobante",
						configuracion : {},
						filtros : {
							read : false
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					$scope.data.oConfiguracionClasificacionTipoInstrumento = {
						tipo : "combobox-clasificacion-tipo-instrumento",
						configuracion : {},
						filtros : { 	
							IdEstado 		:	{field : "IdEstado", operator : "eq", value : "ACT"},
							NoDerivados	    :   { "field" : "IdTipoInstrumento","operator": "neq", "value" : "DER" }
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					$scope.data.oConfiguracionTipoInstrumento = {
						tipo : "combobox-tipo-instrumento",
						configuracion : {},
						filtros : {
							IdClasificacionTipoInstrumento : {
								"field" : "IdClasificacionTipoInstrumento",
								"operator" : "eq",
								"value" : ""
							}
						},
						// filtros : { IdTipoInstrumento : {"field" :
						// "IdTipoInstrumento","operator": "eq","value" : ""} },
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};

					$scope.data.oConfiguracionSucursal = {
						tipo : "combobox-sucursal",
						configuracion : {},
						filtros : {
							EstadoSucursal : {
								field : "EstadoSucursal",
								operator : "eq",
								value : "VIG"
							},
							IdEmpresa : {
								field : "IdEmpresa",
								operator : "eq",
								value : $scope.SesionIdEmpresa
							}
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					$scope.data.oConfiguracionEmpleado = {
						tipo : "combobox-empleado",
						configuracion : {},
						filtros : {
							// Tipo : { field: "CodigoTipoCargoEmpleado",
							// operator: "eq", value: "AGE" }, //REVISAR
							IdSucursal : {
								field : "IdSucursal",
								operator : "eq",
								value : ""
							}
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
					};
					
					$scope.data.oConfiguracionTipoLiquidacionFactura={
							tipo : "combobox-tipo-liquidacion-comision-factura",
							configuracion : {}, 
							filtros : { 	
								Estado : {field : "IdEstado", operator : "eq", value : "ACT"}
							},
							idActual : false,
							seleccionado : false,
							idDefecto : ''
					};
					$scope.data.oConfiguracionGananciaCapital={
						tipo : "combobox-ganancia-capital",
						configuracion : {}, 
						filtros : { 	
							IdEstado : {field : "IdEstado", operator : "eq", value : "ACT"}
						},
						idActual : false,
						seleccionado : false,
						idDefecto : ''
				};
					// GRILLA PRINCIPAL
					$scope.confBasicaComprobantesFacturacion = {
						dataSource : bcsComprobanteFacturacionGrid.dataSourceComprobantes,
						columns : bcsComprobanteFacturacionGrid
								.columnasComprobantes(),
						rowColorBackground : bcsComprobanteFacturacionGrid.fnColorBk,
						sortable : true,
						pageable : true,
						// autoBind : true,
						filterable : true,
						excel : {
							allPages : true
						},
						height : 450,
						selectable : "multiple row",
						navigatable : true,
						userSettings : true,
						allowCopy : true
					};

					// SUB-GRILLA DETALLE DE COMPROBANTE
					$scope.confBasicaComprobanteDetalle = function(dataItem) {
						var filtrosComprobante = [ {
							field : "IdComprobante",
							operator : "eq",
							value : Number(dataItem.IdComprobante)
						} ];
						return {
							dataSource : bcsComprobanteFacturacionGrid
									.dataSourceComprobanteDetalle(filtrosComprobante),
							columns : bcsComprobanteFacturacionGrid
									.columnasComprobanteDetalle(),
							sortable : true,
							pageable : true,
							height : 200,
							selectable : "multiple row",
							autoBind : true,
							navigatable : true,
							userSettings : true,
							allowCopy : true
						};
					};

					$scope.confBasicaMovimientoComprobanteDetalle = function(
							dataItem) {
						var filtrosComprobanteDetalle = [ {
							field : "IdComprobanteDetalle",
							operator : "eq",
							value : Number(dataItem.IdComprobanteDetalle)
						} ];
						return {
							dataSource : bcsComprobanteFacturacionGrid
									.dataSourceMovimientoComprobanteDetalle(filtrosComprobanteDetalle),
							columns : bcsComprobanteFacturacionGrid
									.columnasMovimientoComprobanteDetalle(),
							sortable : true,
							pageable : true,
							height : 120,
							selectable : "multiple row",
							autoBind : true,
							navigatable : true,
							userSettings : true,
							allowCopy : true

						};
					};

					// SUB-GRILLA DE COBROS
					$scope.confBasicaComprobanteCobro = function(dataItem) {
						var filtrosComprobanteCobros = [ {
							field : "IdComprobante",
							operator : "eq",
							value : Number(dataItem.IdComprobante)
						}, {
							field : "IdEstado",
							operator : "eq",
							value : 'VIG'
						} ];
						return {
							dataSource : bcsComprobanteFacturacionGrid
									.dataSourceComprobanteCobro(filtrosComprobanteCobros),
							columns : bcsComprobanteFacturacionGrid
									.columnasComprobanteCobro(),
							sortable : true,
							pageable : true,
							userSettings : true,
							height : 200,
							selectable : "multiple row",
							autoBind : true,
							navigatable : true,
							allowCopy : true
						};
					};

					// SUB-GRILLA FORMA DE PAGO
					$scope.confBasicaComprobanteFormaDePago = function(dataItem) {
						var filtrosComprobanteFormaDePago = [ {
							field : "IdComprobante",
							operator : "eq",
							value : Number(dataItem.IdComprobante)
						} ];
						return {
							dataSource : bcsComprobanteFacturacionGrid
									.dataSourceComprobanteFormaDePago(filtrosComprobanteFormaDePago),
							columns : bcsComprobanteFacturacionGrid
									.columnasComprobanteFormaDePago(),
							sortable : true,
							pageable : true,
							height : 200,
							userSettings : true,
							selectable : "multiple row",
							autoBind : true,
							navigatable : true,
							allowCopy : true
						};
					};

					$scope.buscar = function() {
						$scope.gridApiComprobantes.dataSource
								.filter(getFilters());
								$scope.seleccionComprobante = undefined;
						$scope.sumaCantidad = 0;
						$scope.sumaMonto = 0
					};

					function getFilters() {
						var filters = new Array();

						if ($scope.data.oClientePortafolio.seleccionado.IdCliente)
							filters
									.push({
										field : "IdCliente",
										operator : "eq",
										value : Number($scope.data.oClientePortafolio.seleccionado.IdCliente)
									});
						if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
							filters
									.push({
										field : "IdPortafolio",
										operator : "eq",
										value : Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
									});
						if ($scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante)
							filters
									.push({
										"field" : "IdTipoComprobante",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante
									});
						if ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda)
							filters
									.push({
										"field" : "IdMoneda",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda
									});
						if ($scope.data.oConfiguracionEstadoComprobante.seleccionado.IdEstado)
							filters
									.push({
										"field" : "IdEstadoComprobante",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionEstadoComprobante.seleccionado.IdEstado
									});
						if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
							filters
									.push({
										"field" : "IdClasificacionTipoInstrumento",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento
									});
						if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
							filters
									.push({
										"field" : "IdTipoInstrumento",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento
									});
						// if
						// ($scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProducto
						// &&
						// $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProducto
						// !== "0")
						// filters.push( { "field": "IdTipoProducto","operator":
						// "eq", "value" :
						// $scope.data.oConfiguracionTipoProducto.seleccionado.IdTipoProducto});
						if ($scope.data.oConfiguracionSucursal.seleccionado.IdSucursal)
							filters
									.push({
										"field" : "IdSucursalEmpleadoEjecutivoPortafolio",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionSucursal.seleccionado.IdSucursal
									});
						if ($scope.data.oConfiguracionEmpleado.seleccionado.IdEmpleado)
							filters
									.push({
										"field" : "IdEmpleadoEjecutivoPortafolio",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionEmpleado.seleccionado.IdEmpleado
									});
						if ($scope.data.oFechaComprobanteDesde)
							filters.push({
								"field" : "FechaComprobante",
								"operator" : "gte",
								"value" : util.date(
										$scope.data.oFechaComprobanteDesde)
										.hours(0).minutes(0).seconds(0)
										.toDate()
							});
						if ($scope.data.oFechaComprobanteHasta)
							filters.push({
								"field" : "FechaComprobante",
								"operator" : "lte",
								"value" : util.date(
										$scope.data.oFechaComprobanteHasta)
										.hours(0).minutes(0).seconds(0)
										.toDate()
							});
						if ($scope.data.oFolioComprobanteDesde)
							filters
									.push({
										"field" : "FolioComprobante",
										"operator" : "gte",
										"value" : Number($scope.data.oFolioComprobanteDesde)
									});
						if ($scope.data.oFolioComprobanteHasta)
							filters
									.push({
										"field" : "FolioComprobante",
										"operator" : "lte",
										"value" : Number($scope.data.oFolioComprobanteHasta)
									});

						if ($scope.data.oConfiguracionTipoLiquidacionFactura.seleccionado.IdTipoLiquidacionComisionFactura)
							filters.push({ 
										"field": "IdTipoLiquidacionComisionFactura",
										"operator": "eq", 
										"value" : $scope.data.oConfiguracionTipoLiquidacionFactura.seleccionado.IdTipoLiquidacionComisionFactura
									});
						if ($scope.data.oConfiguracionGananciaCapital.seleccionado.IdTipoRetencion != "0")
							filters
									.push({
										"field" : "IdTipoRetencion",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionGananciaCapital.seleccionado.IdTipoRetencion
									});
						
						

						return filters;
					}

					$scope.limpiarFiltros = function() {
						$scope.filtros.IdPortafolio = false;
						$scope.data.oConfiguracionTipoComprobante.idActual = 0;
						$scope.data.oConfiguracionMoneda.idActual = 0;
						$scope.data.oConfiguracionEstadoComprobante.idActual = 0;
						$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = 0;
						$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
						// $scope.data.oConfiguracionTipoProducto.idActual = 0;
						$scope.data.oConfiguracionSucursal.idActual = 0;
						$scope.data.oConfiguracionEmpleado.idActual = 0;

						$scope.data.oFolioComprobanteDesde = 0;
						$scope.data.oFolioComprobanteHasta = 99999999999;

						$scope.data.oFechaComprobanteHasta = util.dateDS(
								$scope.SesionFechaSistema, "FechaSistema",
								"CALL");
						$scope.data.oFechaComprobanteDesde = util.dateDS(
								$scope.SesionFechaSistema, "FechaSistema",
								"CALL");

						$scope.gridApiComprobantes.dataSource.removeAll();
						$scope.data.oConfiguracionGananciaCapital.idActual = 0;
						$scope.sumaCantidad = 0;
						$scope.sumaMonto = 0;
					};

					// ***********************************************
					// Seleccionar todos los Movimientos
					// ***********************************************
					$scope.data.seleccionarTodosMovimientos = function() {
						$scope.gridApiComprobantes
								.select($scope.gridApiComprobantes.tbody
										.find(">tr"));
					};

					$scope.data.deseleccionarTodosMovimientos = function() {
						$scope.gridApiComprobantes.clearSelection();
						$scope.seleccionComprobante = [];
					};

					// *********************************************************************************
					// Solicita generación archivo "Anexo 19 - Libro Ventas"
					// (Inscripción en JobManager)
					// *********************************************************************************
					$scope.data.generarArchivoLibroVentas = function() {

						if ($scope.gridApiComprobantes.dataSource.ds._total < 1) {
							bcsComunes
									.ventanaMsg(
											{
												mensaje : $translate
														.instant("UI_DEBE_FILTRAR_DATOS_PARA_GENERAR_ARCHIVO")
											}, idVentana);
							return;
						}

						bcsComunes.ventanaOverlay(idVentana, true);

						// Parámetros de entrada al job
						var dataCols = [];

						// Parametros fijos
						dataCols.push({
							NombreParametro : 'IdEmpresa',
							Valor : $scope.SesionIdEmpresa
						});
						dataCols.push({
							NombreParametro : 'IdUsuario',
							Valor : $scope.SesionIdUsuario
						});
						dataCols.push({
							NombreParametro : 'CodigoFormatoArchivo',
							Valor : 'ANEXO19_LIBRO_VENTAS_ELECTRONICO'
						});

						// Los parametros de entrada al SP son los mismos que
						// los filtrso de búsqueda
						if ($scope.data.oClientePortafolio.seleccionado.IdCliente)
							dataCols
									.push({
										NombreParametro : 'IdCliente',
										Valor : String($scope.data.oClientePortafolio.seleccionado.IdCliente)
									});
						if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
							dataCols
									.push({
										NombreParametro : 'IdPortafolio',
										Valor : String($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
									});
						if ($scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante)
							dataCols
									.push({
										NombreParametro : 'IdTipoComprobante',
										Valor : $scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante
									});
						if ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda)
							dataCols
									.push({
										NombreParametro : 'IdMoneda',
										Valor : $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda
									});
						if ($scope.data.oConfiguracionEstadoComprobante.seleccionado.IdEstado)
							dataCols
									.push({
										NombreParametro : 'IdEstadoComprobante',
										Valor : $scope.data.oConfiguracionEstadoComprobante.seleccionado.IdEstado
									});
						if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
							dataCols
									.push({
										NombreParametro : 'IdClasificacionTipoInstrumento',
										Valor : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento
									});
						if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
							dataCols
									.push({
										NombreParametro : 'IdTipoInstrumento',
										Valor : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento
									});
						if ($scope.data.oConfiguracionSucursal.seleccionado.IdSucursal)
							dataCols
									.push({
										NombreParametro : 'IdSucursalEmpleadoEjecutivoPortafolio',
										Valor : $scope.data.oConfiguracionSucursal.seleccionado.IdSucursal
									});
						if ($scope.data.oConfiguracionEmpleado.seleccionado.IdEmpleado)
							dataCols
									.push({
										NombreParametro : 'IdEmpleadoEjecutivoPortafolio',
										Valor : $scope.data.oConfiguracionEmpleado.seleccionado.IdEmpleado
									});
						if ($scope.data.oFechaComprobanteDesde)
							dataCols
									.push({
										NombreParametro : 'FechaComprobanteDesde',
										Valor : util
												.dateSPdate($scope.data.oFechaComprobanteDesde)
									});
						if ($scope.data.oFechaComprobanteHasta)
							dataCols
									.push({
										NombreParametro : 'FechaComprobanteHasta',
										Valor : util
												.dateSPdate($scope.data.oFechaComprobanteHasta)
									});
						if ($scope.data.oFolioComprobanteDesde)
							dataCols
									.push({
										NombreParametro : 'FolioComprobanteDesde',
										Valor : String($scope.data.oFolioComprobanteDesde)
									});
						if ($scope.data.oFolioComprobanteHasta)
							dataCols
									.push({
										NombreParametro : 'FolioComprobanteHasta',
										Valor : String($scope.data.oFolioComprobanteHasta)
									});

						if (dataCols.length > 0) {
							var datos = {};
							datos.p_IdEmpresa = $scope.SesionIdEmpresa;
							datos.p_IdUsuario = $scope.SesionIdUsuario;
							datos.p_IdModulo = 'FAC';
							datos.p_UsaReglasNegocio = 'S';
							datos.p_IdTipoPrograma = 'ANEXO19_LIBRO_VENTAS_ELECTRONICO';
							datos.p_ParametrosPrograma = dataCols;
							bcsComunes.log().info(JSON.stringify(datos));

							bcsServiciosFacturacion
									.SolicitudGenerarArchivo(datos).$promise
									.then(
											function(result) {
												bcsComunes.ventanaOverlay(
														idVentana, false);
												bcsComunes
														.ventanaMsg(
																{
																	conflictos : util
																			.parseServerResponse(result)
																}, idVentana);

											},
											function(result) {
												bcsComunes.ventanaOverlay(
														idVentana, false);
												bcsComunes
														.msg(
																"error",
																"Error",
																$translate
																		.instant('UI_SOLICITUD_GENERACION_ARCHIVO_ERROR'));
											});
						}

					};

					// ***********************************************
					// Anular Comprobantes
					// ***********************************************
					$scope.data.anularComprobante = function() {
						// Valida que se hayan seleccionado movimientos
						if ($scope.seleccionComprobante === undefined) {
							$scope.data.btnActivo = true; // Activa botones
							bcsComunes.msg("warning", "warning", $translate
									.instant('UI_SELECCIONAR_COMPROBANTE'));
							return;
						} else {

							// Mensaje de confirmación
							var t = bcsAlerta
									.crear(
											"PREGUNTA_SI_NO",
											$translate
													.instant("UI_MENSAJE_ANULAR_COMPROBANTE_FACTURACION"),
											idVentana);
							t
									.then(function(result) {
										if (result === OPCIONES_BCSALERTA.SI) {
											var ComprobanteAAnular = [];
											$scope.data.btnActivo = false; // bloquea
																			// botones
											bcsComunes.ventanaOverlay(
													idVentana, true);

											// Recopila Id de movimientos
											// seleccionados
											$
													.each(
															$scope.seleccionComprobante,
															function(key, value) {
																ComprobanteAAnular
																		.push({
																			IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																			IdPortafolio : $scope.seleccionComprobante[key].IdPortafolio,
																			IdCliente : $scope.seleccionComprobante[key].IdCliente,
																			IdTipoComprobante : $scope.seleccionComprobante[key].IdTipoComprobante,
																		});
															});

											// Parámetros de entrada al servicio
											// de anulacion.
											var datosAAnular = {};

											datosAAnular.p_IdEmpresa = $scope.data.IdEmpresa;
											datosAAnular.p_IdUsuario = $scope.data.IdUsuario;
											datosAAnular.p_IdModulo = $scope.data.IdModulo;
											datosAAnular.p_simularOperacion = 'N';
											datosAAnular.p_UsaReglasNegocio = 'S';
											datosAAnular.p_EsIngresoAutomatico = 'N';
											datosAAnular.p_EjecutaAutorizacion = 'N';
											datosAAnular.p_OmiteRegistroSinAutorizar = 'S';
											datosAAnular.p_Glosa = 'Anulado por Sistema';
											datosAAnular.P_COMPROBANTEAANULAR = ComprobanteAAnular;

											// Resultado del servicio
											var successAAnular = function(
													respuesta) {
												bcsComunes.ventanaOverlay(
														idVentana, false);
												// if
												// (respuesta.P_OUT_ERRORESCONTADOR
												// == "0" &&
												// respuesta.P_OUT_ADVERTENCIASCONTADOR
												// == "0") // Para servicio de
												// anulacion en SQL (149)
												if (respuesta.P_OUT_ERRORESCONTADOR == "0") // Para
																							// servicio
																							// de
																							// anulacion
																							// en
																							// JAVA
																							// (149)
												{
													bcsComunes
															.ventanaMsg(
																	{
																		conflictos : util
																				.parseServerResponse(respuesta)
																	},
																	idVentana);

													$scope.gridApiComprobantes.dataSource
															.read();
												} else {
													if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
														if (respuesta.P_OUT_ADVERTENCIASCONTADOR > "0"
																&& respuesta.P_OUT_ERRORESCONTADOR > 0) {
															if (respuesta.P_OUT_ADVERTENCIASDETALLE[0].RequiereAutorizacion == 'RAUT') { // llamamos
																																			// al
																																			// procedimiento
																																			// para
																																			// crear
																																			// una
																																			// autorización
																																			// de
																																			// Anulación
																																			// de
																																			// Facturación.

																// Recopila Id
																// de
																// movimientos
																// seleccionados
																bcsComunes
																		.ventanaOverlay(
																				idVentana,
																				true);
																var ComprobanteAutorizarAAnular = [];
																$
																		.each(
																				$scope.seleccionComprobante,
																				function(
																						key,
																						value) {
																					ComprobanteAutorizarAAnular
																							.push({
																								IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																								IdPortafolio : $scope.seleccionComprobante[key].IdPortafolio,
																								IdCliente : $scope.seleccionComprobante[key].IdCliente,
																								IdTipoComprobante : $scope.seleccionComprobante[key].IdTipoComprobante,
																							});
																				});

																// Parámetros de
																// entrada al
																// servicio de
																// anulacion.
																var datosAutorizacionAAnular = {};

																datosAutorizacionAAnular.p_IdEmpresa = $scope.data.IdEmpresa;
																datosAutorizacionAAnular.p_IdUsuario = $scope.data.IdUsuario;
																datosAutorizacionAAnular.p_IdModulo = $scope.data.IdModulo;
																datosAutorizacionAAnular.p_simularOperacion = 'N';
																datosAutorizacionAAnular.p_UsaReglasNegocio = 'S';
																datosAutorizacionAAnular.P_COMPROBANTEAAUTORIZAR = ComprobanteAutorizarAAnular;

																// Llamada a
																// servicio para
																// anular
																// comprobantes
																bcsComunes
																		.ventanaOverlay(
																				idVentana,
																				false);
																bcsServiciosFacturacion
																		.CrearAutorizacionAnularComprobante(
																				datosAutorizacionAAnular,
																				function(
																						respuesta) {
																					bcsComunes
																							.ventanaMsg(
																									{
																										conflictos : util
																												.parseServerResponse(respuesta)
																									},
																									idVentana);
																				},
																				function(
																						respuesta) {
																					bcsComunes
																							.ventanaMsg(
																									{
																										conflictos : util
																												.parseServerResponse(respuesta)
																									},
																									idVentana);
																				});
															}
														} else {
															bcsComunes
																	.ventanaMsg(
																			{
																				mensaje : $translate
																						.instant(""),
																				conflictos : util
																						.parseServerResponse(respuesta)
																			},
																			idVentana);
														}
													} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
														bcsComunes
																.ventanaMsg(
																		{
																			mensaje : $translate
																					.instant(""),
																			conflictos : util
																					.parseServerResponse(respuesta)
																		},
																		idVentana);

													}
												}

												$scope.data.btnActivo = true; // habilita
																				// botones
												$scope.gridApiComprobantes.dataSource
														.read();
											};

											var errorAAnular = function(
													respuesta) {
												bcsComunes.ventanaOverlay(
														idVentana, false);
												bcsComunes
														.setConflictos(util
																.parseServerResponse(respuesta));
												bcsComunes
														.msg(
																"error",
																"error",
																$translate
																		.instant('UI_ERROR_INGRESO'));
												$scope.data.btnActivo = true; // habilita
																				// botones
											};

											// Llamada a servicio para anular
											// comprobantes
											bcsServiciosFacturacion
													.AnularComprobante(
															datosAAnular,
															successAAnular,
															errorAAnular);
										} else {
											bcsComunes
													.msg(
															"info",
															"info",
															$translate
																	.instant("UI_MENSAJE_CANCELAR")
																	+ ' : '
																	+ $translate
																			.instant("UI_CANCELAR"));
										}
									});
						}
					};
					//***********************************************
					// Nota de Credito Comprobantes automatica y manual
					//***********************************************
					$scope.data.notaCreditoAutomatica = function() 
					  {
						if ($scope.seleccionComprobante === undefined)
						  {
							 $scope.data.btnActivo   = true;     //Activa botones
							 bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_COMPROBANTE'));
							 return;
						  };
						
						
						for(var i = 0; i < $scope.seleccionComprobante.length; i++) {
							if($scope.seleccionComprobante[i].IdEstadoComprobante !== 'ENV') {
								bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_COMPROBANTE_DEBE_ESTAR_ENVIADO"));
								return;
							}
						};
						
						
						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_GENERAR_COMPROBANTE_NOTA_DE_CREDITO"), idVentana);
						t.then(function(result){
						  if (result===OPCIONES_BCSALERTA.SI)
						   {	 
					
					         bcsComunes.ventanaOverlay(idVentana, true);
								     
							 var ComprobanteANC = [];
							 $scope.data.btnActivo  = false;   //bloquea botones			     
								
					 		 //Recopila Id de movimientos seleccionados
					    	 $.each($scope.seleccionComprobante,function(key,value){      		     
					    	    ComprobanteANC.push({   	      			   
						      		                    IdPortafolio : $scope.seleccionComprobante[key].IdPortafolio,
						      			                IdCliente    : $scope.seleccionComprobante[key].IdCliente,
						      			                IdComprobante: $scope.seleccionComprobante[key].IdComprobante,
						      			                IdTipoComprobante : $scope.seleccionComprobante[key].IdTipoComprobante,
						      		                });      	    	 
					    	                 });
					               
					            //Parámetros de entrada al servicio de anulacion.
						   	    var datosANC = {};
						   	    
						      	datosANC.p_IdEmpresa	= $scope.data.IdEmpresa; 
						      	datosANC.p_IdUsuario	= $scope.data.IdUsuario;
						      	datosANC.p_IdModulo		= $scope.data.IdModulo;
						      	datosANC.p_simularOperacion = 'N';
						      	datosANC.p_UsaReglasNegocio = 'S';
						      	datosANC.p_EsIngresoAutomatico = 'N';
						      	datosANC.P_COMPROBANTE = ComprobanteANC;
						      	datosANC.p_CrearComprobanteAutomatico = 'S';
					
					            //Llamada a servicio para anular comprobantes
						      	bcsServiciosFacturacion.CrearComprobanteNotaCredito(datosANC,
						      	       function(respuesta){
							      	         bcsComunes.ventanaOverlay(idVentana, false);
							      	         if (respuesta.P_OUT_ERRORESCONTADOR === 0 && (respuesta.P_OUT_ADVERTENCIASCONTADOR ? respuesta.P_OUT_ADVERTENCIASCONTADOR :0) === 0)
							    	            {	
							    	               //Se actualiza los datos de la grilla
							      	        	   bcsComunes.ventanaMsg({
							    			    	                        conflictos:util.parseServerResponse(respuesta)},idVentana);
				                                                         }
						    	        
						    			           bcsComprobanteFacturacionGrid.dataSourceComprobantes.read();	    			
										           if (respuesta.P_OUT_ERRORESCONTADOR > 0){
										        	   respuestaSoloErrores = respuesta;
										        	   respuestaSoloErroresFiltrados = respuesta.P_OUT_MENSAJES.filter(mensaje=>mensaje.tipo === 'ERROR');
										        	   respuestaSoloErrores.P_OUT_MENSAJES = respuestaSoloErroresFiltrados; 
						    				            bcsComunes.ventanaMsg({
						    					                                  
						    				            						  mensaje:$translate.instant(""),
						    					                                  conflictos:util.parseServerResponse(respuestaSoloErrores)}, idVentana);
						    				              

							                                  }else{
						    			                	          if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
						    			          	                      bcsComunes.ventanaMsg({
						    			          	                	 		  	      		    mensaje:$translate.instant(""),
						    					                                                    conflictos:util.parseServerResponse(respuesta)}, idVentana); 
						    			                                }
						    			                	       }
							    		           
							      	               $scope.data.btnActivo  = true;   //habilita botones
							    		           $scope.gridApiComprobantes.dataSource.read();
							      		         },
						      		   
							      	                         function(respuesta) 
							      		                       {
							      	                              bcsComunes.setConflictos(util.parseServerResponse(respuesta));
										                          bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
										
										                          $scope.data.btnActivo  = true;   //habilita botones
										                          bcsComunes.ventanaOverlay(idVentana, false);
							      		                       });
				            }
						});				
							      
					  };
					  
					  
					
					$scope.data.notaCreditoManual = function(){
						//Valida que se hayan seleccionado movimientos
						if ($scope.seleccionComprobante === undefined)
						  {	
							 $scope.data.btnActivos   = true;     //Activa botones
							 bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_COMPROBANTE'));			
							 return;
						  }else if ($scope.seleccionComprobante.length > 1)
							      {
							         $scope.data.btnActivos   = true;     //Activa botones
								     bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_UN_COMPROBANTE'));			
								     return;
							      }else {
							    	        bcsComunes.ventanaOverlay(idVentana, true);  	  
							            };

						var datos = {};
						
						datos.accion = 'NCM';
						datos.registro = $scope.seleccionComprobante[0]; 
						
						if(datos.registro.IdEstadoComprobante !== "ENV"){
							$scope.data.btnActivos   = true; 
						    bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_COMPROBANTE_SIN_ID_DIST_ENV"));
						    bcsComunes.ventanaOverlay(idVentana, false);
						    return;
						}
						
						if(!datos.registro.IdTipoComprobante.startsWith("F")){
							$scope.data.btnActivos   = true; 
						    bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPCION_VALIDA_SOLO_PARA_FACTURA"));
						    bcsComunes.ventanaOverlay(idVentana, false);
						    return;
						}		
						
						if(datos.registro.EsOtrosConceptos === 'S'){
							datos.registro.TotalComisLiq = datos.registro.Monto;
						}

						if(datos.registro.TotalComisLiq <= 0){
							$scope.data.btnActivos   = true; 
						    bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_MONTO_COMISION_CERO"));
						    bcsComunes.ventanaOverlay(idVentana, false);
						    return;
						}
						
						
						var idModal = bcsComunes.ventanaAbrir("CrearOtrosConceptosModal", datos, false,false,idVentana);
						var promesa = bcsComunes.ventanaPromesa(idModal);
						
						promesa.then(function(data)
						  {
							$scope.gridApiComprobantes.dataSource.read();
							bcsComunes.ventanaOverlay(idVentana, false);
							$scope.seleccionComprobante = undefined;
						  });
				    };
					//NOTA DE DEBITO
					$scope.data.notaDebitoManual = function() {
						if($scope.seleccionComprobante == null){
								bcsComunes.msg('error', $translate.instant("UI_ERROR"),$translate.instant("UI_DEBE_SELECCIONAR_UN_MOVIMIENTO"));
									}else{
										if(($scope.seleccionComprobante[0].CodigoTipoComprobante == 'NCE') && ($scope.seleccionComprobante[0].IdEstadoSII == 'ACP')	&& ($scope.seleccionComprobante[0].EsVentaCorta != 'S')	&& ($scope.seleccionComprobante[0].IdTipoOperacion != 'VENSIM' )){
								var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_PREGUNTA_NOTA_DEBITO'));
								t.result.then(function(result) {
									if (result === OPCIONES_BCSALERTA.ACEPTAR) {
										var ComprobanteANC = [];
										$.each($scope.seleccionComprobante, function(key, value) {
													ComprobanteANC.push({
																IdPortafolio : $scope.seleccionComprobante[key].IdPortafolio,
																IdCliente : $scope.seleccionComprobante[key].IdCliente,
																IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																IdTipoComprobante : $scope.seleccionComprobante[key].IdTipoComprobante,
															});
												});

										var datosANC = {};
										datosANC.p_IdEmpresa 					= $scope.data.IdEmpresa;
										datosANC.p_IdUsuario 					= $scope.data.IdUsuario;
										datosANC.p_IdModulo 					= $scope.data.IdModulo;
										datosANC.p_simularOperacion 			= 'N';
										datosANC.p_UsaReglasNegocio 			= 'S';
										datosANC.p_EsIngresoAutomatico 			= 'N';
										datosANC.P_COMPROBANTE 					= ComprobanteANC ;
										datosANC.p_CrearComprobanteAutomatico 	= 'N';

										bcsServiciosFacturacion.CrearComprobanteNotaDebito(datosANC, function(respuesta) {bcsComunes.ventanaOverlay(idVentana,false);
													if (respuesta.P_OUT_ERRORESCONTADOR === 0 && (respuesta.P_OUT_ADVERTENCIASCONTADOR ? respuesta.P_OUT_ADVERTENCIASCONTADOR: 0) === 0) {
														bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},idVentana);
													}
													bcsComprobanteFacturacionGrid.dataSourceComprobantes.read();
													if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
														bcsComunes.ventanaMsg({
																			mensaje : $translate.instant(""),
																			conflictos : util.parseServerResponse(respuesta)
																		},idVentana);
													} else {
														if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
															bcsComunes.ventanaMsg({
																				mensaje : $translate.instant(""),
																				conflictos : util.parseServerResponse(respuesta)
																			},idVentana);
														}
													}
													$scope.gridApiComprobantes.dataSource.read();
										},
										function(respuesta) {bcsComunes.setConflictos(util.parseServerResponse(respuesta));
													bcsComunes.msg(
																	"error",
																	"error",
																	$translate.instant('UI_ERROR_INGRESO'));

													$scope.data.btnActivo = true; 
													bcsComunes.ventanaOverlay(idVentana,false);
										});
									}
								});
							}else{
								if($scope.seleccionComprobante.length == 0)
								{
									bcsComunes.msg('error',	$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UN_MOVIMIENTO"));
								}											
								else if($scope.seleccionComprobante[0].IdEstadoSII == 'PEN')
								{
									bcsComunes.msg('error',	$translate.instant("UI_ERROR"),	$translate.instant("UI_EL_ESTADO_SII_ACEPTADO"));
								}
								else if($scope.seleccionComprobante[0].EsVentaCorta == 'S')
								{
							    	bcsComunes.msg('error',	$translate.instant("UI_ERROR"), $translate.instant("UI_ES_VENTA_CORTA"));
							    } 
								else if($scope.seleccionComprobante[0].IdTipoOperacion == 'VENSIM')
								{						
									bcsComunes.msg('error',	$translate.instant("UI_ERROR"),	$translate.instant("UI_ES_VENTA_SIMULTANEA"));		
								}
								else if($scope.seleccionComprobante[0].IdEstadoSII == 'ACP')
								{
									bcsComunes.msg('error',	$translate.instant("UI_ERROR"),	$translate.instant("UI_EL_ESTADO_SII_ACEPTADO"));
								}
								else if($scope.seleccionComprobante[0].CodigoTipoComprobante !== 'NCE') 
								{			
								    bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_SELECCIONAR_UNA_NOTA_DE_CREDITO"));
								}						
							}
						}		
					};
					// FIN NOTA DE DEBITO
					//************************************************
				
				
				//***********************************************
				// Enviar a DTE 
				//***********************************************	

					$scope.data.enviarDTE = function() {
						// Valida que se hayan seleccionado movimientos
						var mensaje = 'UI_MENSAJE_ENVIAR_COMPROBANTE';
						if (!$scope.seleccionComprobante || $scope.seleccionComprobante.length > 1)
						{
							mensaje = 'UI_MENSAJE_ENVIAR_COMPROBANTES_MASIVOS'; // Selecciono mas de uno
						}
						if (!$scope.seleccionComprobante || $scope.seleccionComprobante.length == 0) {
							mensaje = 'UI_MENSAJE_ENVIAR_COMPROBANTES_MASIVOS'; // Si No selecciono ninguno, abajo los marca todos como seleccionados
								$scope.gridApiComprobantes
										.select($scope.gridApiComprobantes.tbody
												.find(">tr"));
						}
							// Mensaje de confirmación
							var t = bcsAlerta
									.crear(
											"PREGUNTA_SI_NO",
											$translate
													.instant(mensaje),
											idVentana);
							t
									.then(function(result) {
										if (result === OPCIONES_BCSALERTA.SI) {
											var ComprobanteAEnviar = [];
											bcsComunes.ventanaOverlay(
													idVentana, true);
											$scope.data.btnActivos = false; // bloquea
																			// botones

											// Recopila Id de movimientos
											// seleccionados
											$
													.each(
															$scope.seleccionComprobante,
															function(key, value) {
																ComprobanteAEnviar
																		.push({
																			IdPortafolio : $scope.seleccionComprobante[key].IdPortafolio,
																			IdCliente : $scope.seleccionComprobante[key].IdCliente,
																			IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																			IdTipoComprobante : $scope.seleccionComprobante[key].IdTipoComprobante,
																		});
															});

											// Parámetros de entrada al servicio
											// de anulacion.
											var datosAEnviar = {};

											datosAEnviar.p_IdEmpresa = $scope.data.IdEmpresa;
											datosAEnviar.p_IdUsuario = $scope.data.IdUsuario;
											datosAEnviar.p_IdModulo = $scope.data.IdModulo;
											datosAEnviar.p_simularOperacion = 'N';
											datosAEnviar.p_UsaReglasNegocio = 'S';
											datosAEnviar.p_EsIngresoAutomatico = 'N';
											datosAEnviar.p_ComprobanteAEnviar = ComprobanteAEnviar;

											// Llamada a servicio para
											// DesBloquear comprobantes
											bcsServiciosFacturacion
													.EnviarComprobante(
															datosAEnviar,
															function(respuesta) {
																if (respuesta.P_OUT_ERRORESCONTADOR == "0"
																		&& respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") {
																	// Se
																	// actualiza
																	// los datos
																	// de la
																	// grilla
																	bcsComunes
																			.ventanaMsg(
																					{
																						conflictos : util
																								.parseServerResponse(respuesta)
																					},
																					idVentana);

																	$scope.data.btnActivos = true; // habilita
																									// botones

																	$scope.gridApiComprobantes.dataSource
																			.fetch();
																	bcsComunes
																			.ventanaOverlay(
																					idVentana,
																					false);
																} else {
																	bcsComunes
																			.ventanaMsg(
																					{
																						mensaje : $translate
																								.instant(""),
																						conflictos : util
																								.parseServerResponse(respuesta)
																					},
																					idVentana);
																	bcsComunes
																			.ventanaOverlay(
																					idVentana,
																					false);
																}

															},
															function(resError) {
																bcsComunes
																		.msg(
																				"error",
																				"error",
																				$translate
																						.instant('UI_ERROR_INGRESO'));
																$scope.data.btnActivos = true; // habilita
																								// botones
																bcsComunes
																		.ventanaOverlay(
																				idVentana,
																				false);
															});
										} else {
											bcsComunes
													.msg(
															"info",
															"info",
															$translate
																	.instant("UI_MENSAJE_CANCELAR")
																	+ ' : '
																	+ $translate
																			.instant("UI_CANCELAR"));
										}
									});
					};

					// *******************************************************
					// Consultar DTE y actualizar estado sii de comprobante
					// *******************************************************							        
					
			        function getFiltersConsDTE() {
			        	
			        	var filters = new Array();
			        				        	
			        	if ($scope.data.oClientePortafolio.seleccionado.IdCliente)
							filters
									.push({
										     field : "IdCliente",
										     operator : "eq",
										     value : Number($scope.data.oClientePortafolio.seleccionado.IdCliente)
									      });
			        	if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
							filters
									.push({
										field : "IdPortafolio",
										operator : "eq",
										value : Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio)
									});
						if ($scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante)
							filters
									.push({
										"field" : "IdTipoComprobante",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionTipoComprobante.seleccionado.IdTipoComprobante
									});
						if ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda)
							filters
									.push({
										"field" : "IdMoneda",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda
									});
						if ($scope.data.oConfiguracionEstadoComprobante.seleccionado.IdEstado)
							filters
									.push({
										"field" : "IdEstadoComprobante",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionEstadoComprobante.seleccionado.IdEstado
									});
						if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento)
							filters
									.push({
										"field" : "IdClasificacionTipoInstrumento",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento
									});
						if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
							filters
									.push({
										"field" : "IdTipoInstrumento",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento
									});						
						if ($scope.data.oConfiguracionSucursal.seleccionado.IdSucursal)
							filters
									.push({
										"field" : "IdSucursalEmpleadoEjecutivoPortafolio",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionSucursal.seleccionado.IdSucursal
									});
						if ($scope.data.oConfiguracionEmpleado.seleccionado.IdEmpleado)
							filters
									.push({
										"field" : "IdEmpleadoEjecutivoPortafolio",
										"operator" : "eq",
										"value" : $scope.data.oConfiguracionEmpleado.seleccionado.IdEmpleado
									});
						if ($scope.data.oFechaComprobanteDesde)
							filters.push({
								"field" : "FechaComprobante",
								"operator" : "gte",
								"value" : util.date(
										$scope.data.oFechaComprobanteDesde)
										.hours(0).minutes(0).seconds(0)
										.toDate()
							});
						if ($scope.data.oFechaComprobanteHasta)
							filters.push({
								"field" : "FechaComprobante",
								"operator" : "lte",
								"value" : util.date(
										$scope.data.oFechaComprobanteHasta)
										.hours(0).minutes(0).seconds(0)
										.toDate()
							});
						if ($scope.data.oFolioComprobanteDesde)
							filters
									.push({
										"field" : "FolioComprobante",
										"operator" : "gte",
										"value" : Number($scope.data.oFolioComprobanteDesde)
									});
						if ($scope.data.oFolioComprobanteHasta)
							filters
									.push({
										"field" : "FolioComprobante",
										"operator" : "lte",
										"value" : Number($scope.data.oFolioComprobanteHasta)
									});			
									        	
			        	//Estado DTE para consultar pendientes						
						filters.push({
							           logic: "or",
							           filters: [{
							        	            filters: [
							        	            	       { field: "IdEstadoDTE", operator: "eq", value: "ENV"}
							        	            	     ]
							                     },
							           {
							              logic: "and",
							              filters: [
			                    	                  { field: "IdEstadoDTE", operator: "eq", value: "REC"},
			                    	                  { field: "IdEstadoSII", operator: "eq", value: "PEN" }
			                                       ]
			                           }
			                        ]
			            });
						
																					
			            return filters;			        	
			        }	      
			        
			       
			        // *******************************************************
				    // Consultar DTE y actualizar estado sii de comprobante
				    // *******************************************************
					$scope.data.actualizarEstadoDTE = function() {
						// Valida que se hayan seleccionado movimientos
						if ($scope.seleccionComprobante === undefined) {
							$scope.data.btnActivos = true; // Activa botones
							bcsComunes.msg("warning", "warning", $translate
									.instant('UI_SELECCIONAR_COMPROBANTE'));
							return;
						} else {
							// Mensaje de confirmación
							var t = bcsAlerta
									.crear(
											"PREGUNTA_SI_NO",
											$translate
													.instant("UI_MENSAJE_CONSULTAR_COMPROBANTE"),
											idVentana);
							t
									.then(function(result) {
										if (result === OPCIONES_BCSALERTA.SI) {
											var ComprobanteAAct = [];
											$scope.data.btnActivos = false; // bloquea
																			// botones
											bcsComunes.ventanaOverlay(
													idVentana, true);

											// Recopila Id de movimientos
											// seleccionados
											$
													.each(
															$scope.seleccionComprobante,
															function(key, value) {
																ComprobanteAAct
																		.push({
																			IdPortafolio : $scope.seleccionComprobante[key].IdPortafolio,
																			IdCliente : $scope.seleccionComprobante[key].IdCliente,
																			IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																			IdTipoComprobante : $scope.seleccionComprobante[key].IdTipoComprobante,
																		});
															});

											// Parámetros de entrada al servicio
											// de anulacion.
											var datosAConsultar = {};

											datosAConsultar.p_IdEmpresa = $scope.data.IdEmpresa;
											datosAConsultar.p_IdUsuario = $scope.data.IdUsuario;
											datosAConsultar.p_IdModulo = $scope.data.IdModulo;
											datosAConsultar.p_simularOperacion = 'N';
											datosAConsultar.p_UsaReglasNegocio = 'S';
											datosAConsultar.p_EsIngresoAutomatico = 'N';
											datosAConsultar.p_ComprobanteAAct = ComprobanteAAct;

											// Resultado del servicio
											var successAConsultar = function(
													respuesta) {
												if (respuesta.P_OUT_ERRORESCONTADOR == "0"
														&& respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") {
													// Se actualiza los datos de
													// la grilla
													var n = 0;
													var interval = setInterval(
															function() {
																bcsComunes
																		.ventanaMsg(
																				{
																					conflictos : util
																							.parseServerResponse(respuesta)
																				},
																				idVentana);

																$scope.data.btnActivos = true; // habilita
																								// botones
																if (n === 1) {
																	clearInterval(interval);
																	bcsComunes
																			.ventanaOverlay(
																					idVentana,
																					false);
																	bcsComunes
																			.ventanaMsg(
																					{
																						mensaje : $translate
																						        .instant(""),
																						conflictos : $translate
																								.instant("Error en el DTE. Contacte al administrador.")
																					},
																					idVentana);
																	return;
																}
																n++;

																$scope.gridApiComprobantes.dataSource
																		.fetch(function() {
																			var data = this
																					.data();
																			var countOK = 0;
																			var resultado = respuesta.P_OUT_RESULTADOPROCESO;
																			for (var i = 0; i < data.length; i++) {
																				for (var j = 0; j < resultado.length; j++) {
																					if (data[i].IdComprobante === resultado[j].Id
																							&& data[i].IdEstadoComprobante === "ENV") {
																						countOK++;
																					}
																				}
																			}
																			if (countOK === resultado.length) {
																				clearInterval(interval);
																				bcsComunes
																						.ventanaOverlay(
																								idVentana,
																								false);
																			}
																		});

															}, 4000);
												} else {
													if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
														bcsComunes
																.ventanaMsg(
																		{
																			mensaje : $translate
																					.instant(""),
																			conflictos : util
																					.parseServerResponse(respuesta)
																		},
																		idVentana);
													} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {
														bcsComunes
																.ventanaMsg(
																		{
																			mensaje : $translate
																					.instant(""),
																			conflictos : util
																					.parseServerResponse(respuesta)
																		},
																		idVentana);
													}
												}

											};

											var errorAConsultar = function(
													respuesta) {
												bcsComunes
														.msg(
																"error",
																"error",
																$translate
																		.instant('UI_ERROR_INGRESO'));
												$scope.data.btnActivos = true; // habilita
																				// botones
												bcsComunes.ventanaOverlay(
														idVentana, false);
											};

											// Llamada a servicio para
											// DesBloquear comprobantes
											bcsServiciosFacturacion
													.ConsultarEstadoDteComprobante(
															datosAConsultar,
															successAConsultar,
															errorAConsultar);
										} else
											bcsComunes
													.msg(
															"info",
															"info",
															$translate
																	.instant("UI_MENSAJE_CANCELAR")
																	+ ' : '
																	+ $translate
																			.instant("UI_CANCELAR"));
									});
						}
					};
					
					$scope.data.actualizarEstadoDTE_Masivo = function() { 
						
						if ($scope.gridApiComprobantes.dataSource.ds._data.length === 0) {
							$scope.data.btnActivos = true; // Activa botones
							bcsComunes.msg("warning", "warning", $translate
									.instant('UI_NO_EXISTEN_COMPROBANTE'));
							return;
						}else{
						
					  // Mensaje de confirmación
					  var t = bcsAlerta
						  .crear(
									"PREGUNTA_SI_NO",
									$translate
											 .instant("UI_MENSAJE_CONSULTAR_COMPROBANTE"),
									idVentana);
							t
							 .then(function(result) {
									if (result === OPCIONES_BCSALERTA.SI) {
										var ComprobanteAAct = [];
										var datosAConsultar = {};		
										bcsComunes.ventanaOverlay(idVentana, true);										
					                    $scope.gridApiComprobantes.dataSource.filter(getFiltersConsDTE());
					                        
										$scope.gridApiComprobantes.dataSource
											  .fetch(function() {
											                       var data = $scope.gridApiComprobantes.dataSource.data();
											                        	
																   $.each(data,function(key,value) {				 
																		  ComprobanteAAct
																						.push({
																								IdPortafolio : value.IdPortafolio,
																								IdCliente : value.IdCliente,
																								IdComprobante : value.IdComprobante,
																								IdTipoComprobante : value.IdTipoComprobante,
																							  });
																								
																   });																		

													               // Parámetros de entrada al servicio
																   var datosAConsultar = {};

																   datosAConsultar.p_IdEmpresa = $scope.data.IdEmpresa;
																   datosAConsultar.p_IdUsuario = $scope.data.IdUsuario;
																   datosAConsultar.p_IdModulo = $scope.data.IdModulo;
																   datosAConsultar.p_simularOperacion = 'N';
																   datosAConsultar.p_UsaReglasNegocio = 'S';
																   datosAConsultar.p_EsIngresoAutomatico = 'N';
																   datosAConsultar.p_ComprobanteAAct = ComprobanteAAct;

																   // Resultado del servicio
																   var successAConsultar = function(
																		respuesta) {
																	                  if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") {
																	                	// Se actualiza los datos de
																							// la grilla
																							var n = 0;
																							var interval = setInterval(
																									function() {
																										bcsComunes
																												.ventanaMsg(
																														{
																															conflictos : util
																																	.parseServerResponse(respuesta)
																														},
																														idVentana);

																										$scope.data.btnActivos = true; // habilita
																																		// botones
																										if (n === 5) {
																											clearInterval(interval);
																											bcsComunes
																													.ventanaOverlay(
																															idVentana,
																															false);
																											bcsComunes
																													.ventanaMsg(
																															{
																																mensaje : $translate
																																          .instant(""),
																																conflictos : $translate
																																		.instant("Error en el DTE. Contacte al administrador.")
																															},
																															idVentana);
																											return;
																										}
																										n++;

																										$scope.gridApiComprobantes.dataSource
																												.fetch(function() {
																													var data = this
																															.data();
																													var countOK = 0;
																													var resultado = respuesta.P_OUT_RESULTADOPROCESO;
																													for (var i = 0; i < data.length; i++) {
																														for (var j = 0; j < resultado.length; j++) {
																															if (data[i].IdComprobante === resultado[j].Id
																																	&& data[i].IdEstadoComprobante === "ENV") {
																																countOK++;
																															}
																														}
																													}
																													if (countOK === resultado.length) {
																														clearInterval(interval);
																														bcsComunes
																																.ventanaOverlay(
																																		idVentana,
																																		false);
																													}
																												});

																									}, 4000);
																							bcsComunes.ventanaOverlay(idVentana, false);
																	} else {
																		if (respuesta.P_OUT_ERRORESCONTADOR > 0) {														
																			bcsComunes.ventanaMsg(
																					{
																						mensaje : $translate
																								.instant(""),
																						conflictos : util
																								.parseServerResponse(respuesta)
																					},
																					idVentana);
																		} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0) {														
																			bcsComunes.ventanaMsg(
																					{
																						mensaje : $translate
																								.instant(""),
																						conflictos : util
																								.parseServerResponse(respuesta)
																					},
																					idVentana);				
																		}
																	}
																	
																	$scope.gridApiComprobantes.dataSource.filter(getFilters());
																	bcsComunes.ventanaOverlay(idVentana, false);
																};

																var errorAConsultar = function(
																		respuesta) {
																	bcsComunes
																			.msg(
																					"error",
																					"error",
																					$translate
																							.instant('UI_ERROR_INGRESO'));
																	$scope.data.btnActivos = true; // habilita
																									// botones
																	bcsComunes.ventanaOverlay(
																			idVentana, false);
																};

																// Llamada a servicio
																bcsServiciosFacturacion.ConsultarEstadoDteComprobante(datosAConsultar, successAConsultar, errorAConsultar);	

										
					                     });
										} else 
											bcsComunes
													.msg(
															"info",
															"info",
															$translate
																	.instant("UI_MENSAJE_CANCELAR")
																	+ ' : '
																	+ $translate
																			.instant("UI_CANCELAR"));
									});	
						}	
					};
					
					

					// *******************************************************
					// Informes DTE
					// *******************************************************
					$scope.data.informesDte = function(comprobantes) {

						// Valida que se hayan seleccionado movimientos
						if ($scope.seleccionComprobante === undefined)// ||
																		// $scope.seleccionComprobante.length
																		// > 1)
						{
							$scope.data.btnActivos = true; // Activa botones
							bcsComunes.msg("warning", "warning", $translate
									.instant('UI_SELECCIONAR_UN_COMPROBANTE'));
							return;
						}

						var comprobantesErr = [];
						var comprobantesCor = [];
						$
								.each(
										$scope.seleccionComprobante,
										function(key, value) {
											if ($scope.seleccionComprobante[key].IdEstadoComprobante !== 'ENV') {
												$scope.data.btnActivos = true; // Activa
																				// botones
												// bcsComunes.msg("warning","warning",$translate.instant('UI_COMPROBANTE_DIST_ENV'));
												comprobantesErr
														.push({
															IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
															IdRespuestaDTE : 0,
															Mensaje : 'Comprobante con estado distinto de enviado',
														});
											} else {
												if ($scope.seleccionComprobante[key].RespuestaXmlDTE === '') {
													$scope.data.btnActivos = true; // Activa
																					// botones
													// bcsComunes.msg("warning","warning",$translate.instant('UI_COMPROBANTE_SIN_URL_XML'));
													comprobantesErr
															.push({
																IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																Mensaje : 'Comprobante DTE no generado correctamente',
																IdRespuestaDTE : 0
															});
												} else {
													comprobantesCor
															.push({
																IdComprobante : $scope.seleccionComprobante[key].IdComprobante,
																Mensaje : 'Comprobante DTE consultado correctamente',
																IdRespuestaDTE : $scope.seleccionComprobante[key].IdRespuestaDTE
															});
												}

											}

										});

						// Acumula comprobantes correctos y con error
						var cmbteDte = [];
						cmbteDte[0] = comprobantesErr;
						cmbteDte[1] = comprobantesCor;

						var idModal = bcsComunes.ventanaAbrir(
								"ComprobanteInformesDTEModal", cmbteDte, false,
								false, idVentana);
						var promesa = bcsComunes.ventanaPromesa(idModal);

						promesa.then(function(data) {
							$scope.gridApiComprobantes.dataSource.read();
							bcsComunes.ventanaOverlay(idVentana, false);
						});
					};

					// *******************************************************
					// Refacturacion
					// *******************************************************


					$scope.data.refacturarComprobante = function(comprobantes) 
					{
						var datos = [];
						
						//Si hay comprobantes seleccionados, se envian a interfaz de refacturacion
						//Si NO hay comprobantes seleccionados, se debe abrir la UI de interfaz de refacturacion limpia
						if ($scope.seleccionComprobante !== undefined)
						{
							if ($scope.seleccionComprobante[0] && $scope.seleccionComprobante[0].IdTipoInstrumento === 'SPT') 
							{
								bcsComunes.msg("error",	"error", $translate.instant('UI_NO_REFACTURAR_COMPROBANTE_SPOT'));
								return;
							}							

							for (var i = 0; i < comprobantes.length; i++) {
								if ($scope.seleccionComprobante[i].IdTipoComprobante === 'NCE') {
									$scope.data.btnActivo = true; // Activa botones
									bcsComunes.msg("error",	"error", $translate.instant('UI_REFACTURACION_NOTA_DE_CREDITO'));
									return;
								}

								datos.push({"IdMovimiento" : comprobantes[i].IdComprobante});
							};
						}
						
						bcsComunes.ventanaAbrir("InterfazRefacturacion", {
							FormaAsignacion : 'REFACTURACION',
							Origen : 'COMP',
							movimientos : datos
						}, false, false, idVentana);

					}




					
					$scope.data.opcionNoHabilitada = function() {
						bcsComunes.msg("warning", "warning",
								"Está opción no se encuentra habilitada.");
					}

					$scope.exportarDetalleExcel = function() {
						// getFilters()
						// Formateo exportacion a excel
						var excelFormat = function(field, field2) {
							return function(item) {
								return util.bcs_formato(item[field], 'GEN',
										'Monto', item[field2], '', '').excelFormat;
							};
						}

						var comprobanteDetalleDatasource = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=20056&c=1",
												dataType : "json"
											}
										},
										schema : {
											model : {
												fields : {
													NombreEstado : {
														field : "NombreEstado",
														type : "string"
													},
													NombreEstadoDTE : {
														field : "NombreEstadoDTE",
														type : "string"
													},
													NombreEstadoSII : {
														field : "NombreEstadoSII",
														type : "string"
													},
													IdComprobante : {
														field : "IdComprobante",
														type : "number"
													},
													FolioComprobante : {
														field : "FolioComprobante",
														type : "number"
													},
													IdComprobanteReferencia : {
														field : "IdComprobanteReferencia",
														type : "number"
													},
													IdTareaDTE : {
														field : "IdTareaDTE",
														type : "number"
													},
													CodigoTipoComprobante : {
														field : "CodigoTipoComprobante",
														type : "string"
													},
													DocumentoIdentidad : {
														field : "DocumentoIdentidad",
														type : "string"
													},
													NombreLargoCliente : {
														field : "NombreLargoCliente",
														type : "string"
													},
													CodigoPortafolio : {
														field : "CodigoPortafolio",
														type : "string"
													},
													NombreEmpleadoEjecutivoPortafolio : {
														field : "NombreEmpleadoEjecutivoPortafolio",
														type : "string"
													},
													NombreSucursalEmpleadoEjecutivoPortafolio : {
														field : "NombreSucursalEmpleadoEjecutivoPortafolio",
														type : "string"
													},
													FechaComprobante : {
														field : "FechaComprobante",
														type : "sysdate"
													},
													FechaLiquidacion : {
														field : "FechaLiquidacion",
														type : "sysdate"
													},
													IdMoneda : {
														field : "IdMoneda",
														type : "string"
													},
													Monto : {
														field : "Monto",
														type : "number"
													},
													MontoCobro : {
														field : "MontoCobro",
														type : "number"
													},
													MontoFinal : {
														field : "MontoFinal",
														type : "number"
													},
													MontoPagado : {
														field : "MontoPagado",
														type : "number"
													},
													NombreTipoProducto : {
														field : "NombreTipoProducto",
														type : "string"
													},
													NombreTipoOperacion : {
														field : "NombreTipoOperacion",
														type : "string"
													},
													EsVentaCorta : {
														field : "EsVentaCorta",
														type : "string"
													},
													TotalComis : {
														field : "TotalComis",
														type : "number"
													},
													TotalDere : {
														field : "TotalDere",
														type : "number"
													},
													TotalGasto : {
														field : "TotalGasto",
														type : "number"
													},
													TotalImpto : {
														field : "TotalImpto",
														type : "number"
													},
													TotalRetencion : {
														field : "TotalRetencion",
														type : "number"
													},
													IdCondicionLiquidacion : {
														field : "IdCondicionLiquidacion",
														type : "string"
													},
													MayorMenorValor : {
														field : "MayorMenorValor",
														type : "number"
													},
													MontoRetencionGananciaCap : {
														field : "MontoRetencionGananciaCap",
														type : "number"
													},
													// detalle
													NroLinea : {
														type : "number"
													},
													NemotecnicoInstrumento : {
														type : "string"
													},
													Cantidad : {
														type : "number"
													},
													Precio : {
														type : "string"
													},
													MontoDetalle : {
														type : "string"
													},
													NombreTipoInstrumento : {
														type : "string"
													},
													IdTipoProducto : {
														type : "string"
													},
													IdTipoOperacion : {
														type : "string"
													},
													IdAgente : {
														type : "number"
													},
													IdComprobanteDetalle : {
														type : "number"
													},
													MayorMenorValor : {
														field : "MayorMenorValor",
														type : "number"
													},
													MontoRetencionGananciaCap : {
														field : "MontoRetencionGananciaCap",
														type : "number"
													}
												}
											}
										}
									}
								});

						bcsComunes.ventanaOverlay(idVentana, true);
						comprobanteDetalleDatasource
								.query({
									filter : getFilters()
								})
								.then(
										function(res) {
											var data = [];
											var campos = [
													{
														field : "NombreEstado",
														title : "UI_ESTADO",
														width : "70px",
														align : "left",
														sortable : false
													},
													{
														field : "NombreEstadoDTE",
														title : "UI_ESTADO_DTE",
														width : "70px",
														align : "left",
														sortable : false
													},
													{
														field : "NombreEstadoSII",
														title : "UI_ESTADO_SII",
														width : "70px",
														align : "left",
														sortable : false
													},
													{
														field : "IdComprobante",
														title : "UI_SECUENCIA_COMPROBANTE_ABR",
														width : "60px",
														align : "right",
														sortable : false
													},
													{
														field : "FolioComprobante",
														title : "UI_FOLIO",
														width : "60px",
														align : "left",
														sortable : false
													},
													{
														field : "IdComprobanteReferencia",
														title : "UI_REFERENCIA_COMPROBANTE",
														width : "60px",
														align : "left",
														sortable : false
													},
													{
														field : "IdTareaDTE",
														title : "UI_TAREA_DTE",
														width : "60px",
														align : "left",
														sortable : false,
														hidden : true
													},
													{
														field : "CodigoTipoComprobante",
														title : "UI_TIPO",
														width : "50px",
														align : "left",
														sortable : false,
														template : '<span class="flags" title="#=NombreTipoComprobante#">#=CodigoTipoComprobante#</span>'
													},
													{
														field : "DocumentoIdentidad",
														title : "UI_DOCUMENTO_IDENTIDAD_ABR",
														width : "70px",
														align : "left",
														sortable : false,
														groupable : true,
														template : '<span class="flags" title="#=NombreLargoCliente#">#=DocumentoIdentidad#</span>'
													},
													{
														field : "NombreLargoCliente",
														title : "UI_CLIENTE",
														width : "200px",
														align : "left",
														sortable : false
													},
													{
														field : "CodigoPortafolio",
														title : "UI_PORTAFOLIO_ABR",
														width : "50px",
														align : "left",
														sortable : false,
														groupable : true
													},
													{
														field : "NombreEmpleadoEjecutivoPortafolio",
														title : "UI_EJECUTIVO",
														width : "200px",
														align : "left",
														sortable : false
													},
													{
														field : "NombreSucursalEmpleadoEjecutivoPortafolio",
														title : "UI_SUCURSAL",
														width : "120px",
														align : "left",
														sortable : false
													},
													{
														field : "FechaComprobante",
														title : "UI_FECHA_COMPROBANTE",
														width : "70px",
														groupable : true
													},
													{
														field : "FechaLiquidacion",
														title : "UI_FECHA_LIQUIDACION",
														width : "70px",
														groupable : true
													},
													{
														field : "IdMoneda",
														title : "UI_MONEDA",
														width : "60px",
														align : "left",
														sortable : false,
														template : '<span class="flags" title="#=NombreMoneda#">#=IdMoneda#</span>'
													},
													{
														field : "Monto",
														title : "UI_MONTO",
														width : "100px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "MontoCobro",
														title : "UI_MONTO_COBRO",
														width : "100px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(MontoCobro,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "MontoFinal",
														title : "UI_MONTO_FINAL",
														width : "100px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(MontoFinal,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>",
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "MontoPagado",
														title : "UI_MONTO_PAGADO",
														width : "100px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(MontoPagado,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "NombreTipoProducto",
														title : "UI_TIPO_PRODUCTO",
														width : "200px",
														align : "left",
														sortable : false
													},
													{
														field : "NombreTipoOperacion",
														title : "UI_TIPO_OPERACION",
														width : "180px",
														align : "left",
														sortable : false
													},
													{
														field : "EsVentaCorta",
														title : "UI_VENTA_CORTA",
														width : "80px",
														align : "left",
														sortable : false
													},
													{
														field : "TotalComis",
														title : "UI_COMISION",
														width : "60px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(TotalComis,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "TotalDere",
														title : "UI_DERECHOS",
														width : "60px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(TotalDere,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "TotalGasto",
														title : "UI_GASTOS",
														width : "60px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(TotalGasto,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "TotalImpto",
														title : "UI_IMPUESTO",
														width : "60px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(TotalImpto,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "TotalRetencion",
														title : "UI_RETENCION",
														width : "60px",
														align : "right",
														sortable : false,
														// template : " <label>
														// #:
														// (util.bcs_formato(TotalRetencion,'GEN','Monto',IdMoneda,'','')).valor
														// #</label>" }
														format : function(
																value, item) {
															return util
																	.bcs_formato(
																			value,
																			'GEN',
																			'Monto',
																			item.IdMoneda,
																			'',
																			'');
														}
													},
													{
														field : "IdCondicionLiquidacion",
														title : "UI_CONDICION_LIQUIDACION",
														width : "200px",
														align : "left",
														sortable : false
													},
													{
														field : "MayorMenorValor",
														title : "UI_MAYOR_MENOR_VALOR",
														width : "120px",
														align : "left",
														sortable : false
													},
													{
														field : "MontoRetencionGananciaCap",
														title : "UI_MONTO_RETENCION_GANANCIA_CAP",
														width : "120px",
														align : "left",
														sortable : false
													}
													/** *********DETALLE********************* */
													,
													{
														field : "NroLinea",
														title : "UI_NUMERO_LINEA_ABR",
														width : "30px",
														align : "right",
														sortable : false
													}
													// ,{ field:
													// "FechaLiquidacion",
													// title:
													// "UI_FECHA_LIQUIDACION_ABR",
													// width: "125px", align:
													// "center", sortable :
													// false, template :
													// '<label> #:
													// util.formatoDate(FechaLiquidacion)
													// #</label>' }
													,
													{
														field : "NemotecnicoInstrumento",
														title : "UI_INSTRUMENTO",
														width : "150px",
														align : "left",
														sortable : false
													},
													{
														field : "Cantidad",
														title : "UI_CANTIDAD",
														width : "70px",
														align : "right",
														sortable : false,
														template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"
													},
													{
														field : "Precio",
														title : "UI_PRECIO",
														width : "70px",
														align : "right",
														sortable : false,
														template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,'','')).valor #</label>"
													},
													{
														field : "MontoDetalle",
														title : "UI_MONTO_DETALLE",
														width : "70px",
														align : "right",
														sortable : false,
														template : " <label> #:  (util.bcs_formato(MontoDetalle,'GEN','Monto',IdMoneda,'','')).valor #</label>"
													},
													{
														field : "NombreTipoInstrumento",
														title : "UI_TIPO_INSTRUMENTO_ABR",
														width : "150px",
														align : "left",
														sortable : false
													},
													{
														field : "IdTipoProducto",
														title : "UI_TIPO_PRODUCTO_ABR",
														width : "65px",
														align : "left",
														sortable : false,
														template : '<span class="flags" title="#=NombreTipoProducto#">#=IdTipoProducto#</span>'
													},
													{
														field : "IdTipoOperacion",
														title : "UI_TIPO_OPERACION_ABR",
														width : "65px",
														align : "left",
														sortable : false,
														template : '<span class="flags" title="#=NombreTipoOperacion#">#=IdTipoOperacion#</span>'
													}, {
														field : "IdBolsa",
														title : "UI_BOLSA_ABR",
														align : "left",
														sortable : false
													},
													{
														field : "MayorMenorValor",
														title : "UI_MAYOR_MENOR_VALOR",
														width : "120px",
														align : "left",
														sortable : false
													},
													{
														field : "MontoRetencionGananciaCap",
														title : "UI_MONTO_RETENCION_GANANCIA_CAP",
														width : "120px",
														align : "left",
														sortable : false
													}

											];

											data = _
													.map(
															campos,
															function(element,
																	index) {
																return {
																	field : element.field,
																	title : $translate
																			.instant(element.title)
																};
															});

											comprobanteDetalleDatasource
													.exportToExcel(
															data,
															"Gestion_Facturacion",
															"Gestion_Facturacion");
											bcsComunes.ventanaOverlay(
													idVentana, false);
										});
					};

					$scope.data.factura = function(comprobantes) {
						var datos = [];
						if (!comprobantes) {
							bcsComunes
									.msg(
											'error',
											$translate.instant("UI_ERROR"),
											$translate
													.instant("UI_DEBE_SELECCIONAR_UNICO_REGISTRO"));
							return;
						}

						if (comprobantes.length > 1) {
							bcsComunes
									.msg(
											'error',
											$translate.instant("UI_ERROR"),
											$translate
													.instant("UI_DEBE_SELECCIONAR_UNICO_REGISTRO"));
							return;
						}

						if (!comprobantes[0].IdTipoComprobante.startsWith("F")
								&& !comprobantes[0].IdTipoComprobante
										.startsWith("N")
								&& $scope.SesionLocale === 'PE') {
							bcsComunes
									.msg(
											'error',
											$translate.instant("UI_ERROR"),
											$translate
													.instant("UI_OPCION_VALIDA_SOLO_PARA_FACTURAS_Y_NC"));
							return;
						}

						if ((comprobantes[0].IdTipoComprobante !== ("FEE") && comprobantes[0].IdTipoComprobante !== ("FOE"))
								&& !(comprobantes[0].IdClasificacionTipoInstrumento === null || comprobantes[0].IdClasificacionTipoInstrumento === '')) {
							bcsComunes
									.msg(
											'error',
											$translate.instant("UI_ERROR"),
											$translate
													.instant("UI_OPCION_VALIDA_SOLO_PARA_FACTURA_EXENTA"));
							return;
						}

						if (comprobantes[0].IdEstadoComprobante !== 'ANU' && comprobantes[0].IdComprobanteReferencia == 0){
							bcsComunes
									.msg(
											'error',
											$translate.instant("UI_ERROR"),
											$translate
													.instant("UI_COMPROBANTE_DEBE_ESTAR_ANULADO_NC"));
							return;
						}	
						
						if(comprobantes[0].IdTipoComprobanteReferencia.startsWith("F") && comprobantes[0].IdComprobanteReferencia > 0){
							bcsComunes
							.msg(
									'error',
									$translate.instant("UI_ERROR"),
									$translate
											.instant("UI_COMPROBANTE_ASOCIADO_FACTURA_PREVIA"));
  					        return;							
						}

						datos.IdComprobante = comprobantes[0].IdComprobante;
						datos.IdTipoComprobante = comprobantes[0].IdTipoComprobante;
						datos.NombreTipoComprobante = comprobantes[0].NombreTipoComprobante;
						datos.FechaSistema = $scope.SesionFechaSistema;

						var id1 = bcsComunes.ventanaAbrir(
								"CrearMovimientosDesdeOtroComprobante", datos,
								false, false, idVentana);
						var promesa = bcsComunes.ventanaPromesa(id1);
						promesa.then(function(data) {
							if (data === true) {
								buscar();
								$scope.prestamos.selPrestamos = undefined;
							}
						});
					}

					// Primer evento
					$timeout(
							function() {
								$scope.data.oConfiguracionTipoInstrumento.filtros.read = true;
								$scope.data.oConfiguracionEmpleado.filtros.read = true;

								// $scope.limpiarFiltros();
							}, 1000);

					$scope.detalleComprobanteFacturacion = function(datos) {
						bcsComunes
								.ventanaAbrir(
										"DetalleComprobanteFacturacion",
										{
											IdComprobante : datos[0].IdComprobante,
											IdTipoComprobante : datos[0].IdTipoComprobante
										}, false, false, idVentana);

					};

					$scope.data.detalleMantenedorIgk = function(datos) {
						if (!datos || !datos[0])
							return;
						const cargoGananciaCap = datos[0].IdTipoRetencion;
						if(cargoGananciaCap =="C" || cargoGananciaCap == "R"){
							bcsComunes
							.ventanaAbrir(
									"DetalleMantenedorIGK",
									{
										IdComprobante : datos[0].IdComprobante,
										IdTipoComprobante : datos[0].IdTipoComprobante
									}, false, false, idVentana);
						}else{
							mostrarErrorSeleccion("UI_NO_AFECTA_IGK");
						}			

					};

					$scope.data.verMapaRelaciones = function(datos) {
						if (!datos || !datos[0])
							return;
						bcsComunes.ventanaAbrir("MapaRelaciones", {
							TipoEntidad : "CMBTEFACTURACION",
							IdEntidad : datos[0].IdComprobante
						}, false, false);
					};

					$scope.data.opcionesNotaDeCredito = [
							{
								titulo : $translate
										.instant("UI_NOTA_CREDITO_AUTOMATICA_ABR"),
								funcion : $scope.data.notaCreditoAutomatica
							},
							{
								titulo : $translate
										.instant("UI_NOTA_CREDITO_MANUAL_ABR"),
								funcion : $scope.data.notaCreditoManual
							} ];

					$scope.data.duplicarFactura = function(datos) {
						if(datos && datos.length > 0){
							bcsComunes
									.ventanaAbrir(
											"DetalleComprobanteFacturacion",
											{
												IdComprobante : datos[0].IdComprobante,
												IdTipoComprobante : datos[0].IdTipoComprobante,
												EsDuplicarFactura: true
											}, false, false, idVentana);
							
						}else{
							mostrarErrorSeleccion("UI_SELECCIONAR_COMPROBANTE");
						}
					};
					
					let mostrarErrorSeleccion = function(mensaje){
						let mensajesError = [];
							
						let P_OUT_MENSAJES =  {
							descripcion: $translate.instant(mensaje),
							tipo: "ERROR"
						};			
						mensajesError.push(P_OUT_MENSAJES);
						
						let detalleError = {
							"P_OUT_MENSAJES": mensajesError, 
							"P_OUT_ERRORESCONTADOR": 1
						};
						
						bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_OPERACION_CON_ERRORES"),
								conflictos: util.parseServerResponse(detalleError)
						},$scope.winID);
					}		

				var array = [
						{ Estado: 'Compra', Color: 'green' },
					    { Estado: 'Venta', Color: 'red' }
					];
					
					bcsComunes.tootipGlosa('toolComprobantesFacturacion', array)
					
				$scope.FiltrosBotones = function() {
					
				}

					$scope.$watch('seleccionComprobante', function (newval, oldval) {
						if (!newval) return;
						if (newval[0] === undefined) {
							return;
						}
						const contrapartida = $scope.seleccionComprobante[0].Contrapartida;
						const idComprobanteReferencia = $scope.seleccionComprobante[0].IdComprobanteReferencia;

						$.each($scope.gridApiComprobantes.dataSource.data(), function (keyTran, valueTran) {
							row = $scope.gridApiComprobantes.tbody.find("tr:eq(" + keyTran + ")");
							data = $scope.gridApiComprobantes.dataItem(row);

							if (data != null) {
								if ("S" === contrapartida && data.Contrapartida === contrapartida && data.IdComprobante === idComprobanteReferencia) {
									$scope.gridApiComprobantes.select($scope.gridApiComprobantes.tbody.find("tr")[keyTran]);
									if ($scope.seleccionComprobante.findIndex(comp => comp.IdComprobante === data.IdComprobante) === -1) {
										$scope.seleccionComprobante.push(data);
									}
								}
							}
						});
					}, true);
					let firstHeight = $('#Win_BlotterFacturacion');
                    firstHeight.css('width', '1070px')
					$scope.$watch('seleccionComprobante',function(newval,oldval) {
						if (!newval) return;
						$scope.sumaCantidad = 0;
						$scope.sumaMonto = 0;
						var moneda = "USD";
						$.each(newval,function(key,value){
							if(isNaN(Number(value.CantidadTotal)) || value.CantidadTotal === ""){
								bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_CANTIDAD");
							}else{
								$scope.sumaCantidad = $scope.sumaCantidad + value.CantidadTotal;
							}
							if(isNaN(Number(value.Monto)) || value.Monto === ""){
								bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_MONTO");
							}else{
								$scope.sumaMonto = $scope.sumaMonto + value.Monto;
							}
						})
						if($scope.sumaCantidad != 0){
							$scope.sumaCantidad = util.bcs_formato($scope.sumaCantidad,'GEN','CANTIDAD',moneda,'','').valor;
						}
						if($scope.sumaMonto != 0 && util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor != 'Error'){
							$scope.sumaMonto = util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor;
						}else{
							$scope.sumaMonto = 0;
						}
					});
				});