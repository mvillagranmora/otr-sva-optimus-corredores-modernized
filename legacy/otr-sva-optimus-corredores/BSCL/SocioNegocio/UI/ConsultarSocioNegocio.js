/*******************************************************************************
 * Purpose : Blotter de Consulta de Socio de Negocio. Author : TEAM SOCIO
 * NEGOCIO - BCS Date : 30/06/2016 [Norm.]
 ******************************************************************************/
(function() {
	angular
			.module('ModuloSocioNegocio')
			.controller(
					'ConsultarSocioNegocioCtrl',
					function($scope, bcsComunes, servicioSociodeNegocio,
							$translate) {

						var conSocNeg = this;
						$scope.formData = {};
						conSocNeg.win = {};
						conSocNeg.ventanaId = bcsComunes.ventanaWinId($scope);
						conSocNeg.win.defSinValor = '-';
						var filtersRel = {
							p_IdEmpresa : $scope.$root.SesionIdEmpresa,
							p_IdSocioDeNegocio : 0
						}; // default
						var PestanaPortafolioAPVHabilitada = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? 'S'
								: 'N';
					    
						$scope.formData.oConfiguracionTipoDocumentoIdentidad={filtros : {}};						
						$scope.formData.oConfiguracionTipoDocumentoIdentidad.filtros = {
							PorDefecto : {"field": "IdEstado","operator": "eq","value": "ACT"}							
						};
						
						
						$scope.initDetalle = function(dataItem) {
							var filterCli = [];
							var filterEmp = [];
							var filterPro = [];
							var filterCnt = [];
							dataItem.datosCli = {};
							dataItem.datosEmp = {};
							dataItem.datosPro = {};
							dataItem.datosCnt = {};

							// DATOS TAB CLIENTE
							if (dataItem.IdCliente > 0) {
								filterCli.push({
									field : "IdCliente",
									operator : "eq",
									value : Number(dataItem.IdCliente)
								});
								ClienteDataSource
										.query({
											filter : filterCli
										})
										.then(
												function() {
													if (ClienteDataSource
															.view().length > 0) {
														var datos = ClienteDataSource
																.data();
														if (datos[0]) {
															dataItem.datosCli = datos[0];
															PortafolioClienteDataSource
																	.query(
																			{
																				filter : filterCli
																			})
																	.then(
																			function() { // obtener
																							// cantidad
																							// de
																							// portafolios.
																				var cantPort = PortafolioClienteDataSource
																						.view().length;
																				dataItem.datosCli.cantidadPortafolios = cantPort;
																			});
														}
													}
												});
							}

							// DATOS TAB EMPLEADO
							if (dataItem.IdEmpleado > 0) {
								filterEmp.push({
									field : "IdEmpleado",
									operator : "eq",
									value : Number(dataItem.IdEmpleado)
								});
								EmpleadoDataSource.query({
									filter : filterEmp
								}).then(function() {
									if (EmpleadoDataSource.view().length > 0) {
										var datos = EmpleadoDataSource.data();
										if (datos[0])
											dataItem.datosEmp = datos[0];
									}
								});
							}

							// DATOS TAB PROVEEDOR
							if (dataItem.IdProveedor > 0) {
								filterPro.push({
									field : "IdProveedor",
									operator : "eq",
									value : Number(dataItem.IdProveedor)
								});
								ProveedorDataSource.query({
									filter : filterPro
								}).then(function() {
									if (ProveedorDataSource.view().length > 0) {
										var datos = ProveedorDataSource.data();
										if (datos[0])
											dataItem.datosPro = datos[0];
									}
								});
							}

							// DATOS TAB CONTRAPARTE
							if (dataItem.IdContraparte > 0) {
								filterCnt.push({
									field : "IdContraparte",
									operator : "eq",
									value : Number(dataItem.IdContraparte)
								});
								ContraparteDataSource
										.query({
											filter : filterCnt
										})
										.then(
												function() {
													if (ContraparteDataSource
															.view().length > 0) {
														var datos = ContraparteDataSource
																.data();
														if (datos[0]) {
															dataItem.datosCnt = datos[0];
															dataItem.datosCnt.Corredora = (dataItem.datosCnt.Corredora === 'S') ? true
																	: false;
															dataItem.datosCnt.Emisor = (dataItem.datosCnt.Emisor === 'S') ? true
																	: false;
															dataItem.datosCnt.CamaraDeCompensacion = (dataItem.datosCnt.CamaraDeCompensacion === 'S') ? true
																	: false;
															dataItem.datosCnt.Depositos = (dataItem.datosCnt.Depositos === 'S') ? true
																	: false;
															dataItem.datosCnt.Banco = (dataItem.datosCnt.Banco === 'S') ? true
																	: false;
															dataItem.datosCnt.Bolsa = (dataItem.datosCnt.Bolsa === 'S') ? true
																	: false;
														}
													}
												});
							}

						};

						$scope.abrirDetalleSoc = function(items) {
							bcsComunes
									.ventanaAbrir(
											"administrarSocioNegocio",
											{
												IdSocioNegocio : items.IdSocioDeNegocio,
												Entidad : "SOC",
												IdPersona : items.IdPersona,
												PestanaPortafolioAPVHabilitada : PestanaPortafolioAPVHabilitada
											}, false);
						};

						var PortafolioClienteDataSource = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1265",
												dataType : "json"
											}
										},

										schema : {
											model : {
												fields : {
													IdPortafolio : {
														type : "string"
													},
													CodigoPortafolio : {
														type : "string"
													},
													Proposito : {
														type : "string"
													},
													EstadoPortafolio : {
														type : "string"
													},
													FechaCreacion : {
														type : "date"
													}
												}
											}
										}
									}
								});

						$scope.PortafolioClienteOptions = {
							sortable : false,
							reorderable : false,
							filterable : false,
							groupable : false,
							pageable : false,
							selectable : "row",
							columnMenu : false,

							resizable : false,
							autoBind : true,
							scrollable : true,
							height : 100,

							columns : [
									{
										field : "IdPortafolio",
										title : $translate
												.instant('UI_ID_PORTAFOLIO_ABR'),
										template : "<span><a ui-sref='administrarPortafolio({IdPortafolio: #= IdPortafolio #, IdCliente: #= IdCliente #})'>#= IdPortafolio #</a></span>",
										format : "{0:n5}",
										width : "150px"
									},
									{
										field : "CodigoPortafolio",
										title : $translate
												.instant('UI_CODIGO_PORTAFOLIO_ABR'),
										format : "{0:n5}",
										width : "150px"
									},
									{
										field : "Proposito",
										title : $translate
												.instant('UI_PROPOSITO_PORTAFOLIO_ABR'),
										format : "{0:n5}",
										width : "150px"
									},
									{
										field : "EstadoPortafolio",
										title : $translate
												.instant('UI_ESTADO_PORTAFOLIO_ABR'),
										format : "{0:n5}",
										width : "150px"
									},
									{
										field : "FechaCreacion",
										title : $translate
												.instant('UI_FECHA_INGRESO_ABR'),
										format : "{0:G}",
										width : "150px"
									} ],
						};

						var ClienteDataSource = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1275",
												dataType : "json"
											}
										},
										schema : {
											model : {
												fields : {
													IdCliente : {
														type : "number"
													},
													ComentarioBloqueo : {
														type : "string"
													},
													EstaHabilitado : {
														type : "string"
													},
													FechaBloqueo : {
														type : "date"
													},
													FechaCreacion : {
														type : "date"
													},
													FechaIngreso : {
														type : "date"
													},
													FechaModificacion : {
														type : "date"
													},
													IdPortafolioClientePorDefecto : {
														type : "number"
													},
													IdSegmentoCliente : {
														type : "number"
													},
													NombreSegmentoCliente : {
														type : "string"
													},

												}
											}
										}
									}
								});

						var EmpleadoDataSource = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700140", //aca
												dataType : "json"
											}
										},
										schema : {
											model : {
												fields : {
													IdEmpleado : {
														type : "number"
													},
													CodigoEmpleado : {
														type : "string"
													},
													CodigoTipoCargoEmpleado : {
														type : "string"
													},
													NombreTipoCargoEmpleado : {
														type : "string"
													},
													CodigoSucursal : {
														type : "string"
													},
													NombreSucursal : {
														type : "string"
													},
													CodigoEmpleadoSupervisor : {
														type : "string"
													},
													IdEstado : {
														type : "string"
													},
													NombreEstado : {
														type : "string"
													},
													FechaInicioCargo : {
														type : "date"
													},
													FechaTerminoCargo : {
														type : "date"
													},
												}
											}
										}
									}
								});

						var ProveedorDataSource = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1290",
												dataType : "json"
											}
										},
										schema : {
											model : {
												fields : {
													IdProveedor : {
														type : "number"
													},
													CodigoEmpleado : {
														type : "string"
													},
													IdAreaProveedor : {
														type : "number"
													},
													NombreAreaProveedor : {
														type : "string"
													},
													IdEstado : {
														type : "string"
													},
													NombreEstado : {
														type : "string"
													},
												}
											}
										}
									}
								});

						var ContraparteDataSource = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1284",
												dataType : "json"
											}
										},
										schema : {
											model : {
												fields : {
													IdContraparte : {
														type : "number"
													},
													Corredora : {
														type : "string"
													},
													Codigo : {
														type : "string"
													},
													Emisor : {
														type : "string"
													},
													CamaraDeCompensacion : {
														type : "string"
													},
													Depositos : {
														type : "string"
													},
													Banco : {
														type : "string"
													},
													Bolsa : {
														type : "string"
													},
												}
											}
										}
									}
								});

						$scope.dskOptions = new bcs.DataSource(
								{
									type : "odata",
									definition : {
										serverFiltering : true,
										serverPaging : true,
										serverSorting : true,
										pageSize : 100,
										sort : [
												{
													field : "NombreTipoDocumentoIdentidadDefecto",
													dir : "asc"
												},
												{
													field : "DocumentoIdentidadDefecto",
													dir : "asc"
												}, {
													field : "NombreCompleto",
													dir : "asc"
												} ],
										transport : {
											read : {
												url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1355",
												dataType : "json"
											}
										},
										schema : {
											model : {
												fields : {
													IdSocioDeNegocio : {
														type : "number"
													},
													FechaIngreso : {
														type : "datetime"
													},
													FechaCreacion : {
														type : "date"
													},
													FechaModificacion : {
														type : "date"
													},
													IdTipoDocumentoIdentidadDefecto : {
														type : "string"
													},
													NombreTipoDocumentoIdentidadDefecto : {
														type : "string"
													},
													NombreCompleto : {
														type : "string"
													},
													DocumentoIdentidadDefecto : {
														type : "string"
													},
													TipoPersona : {
														type : "string"
													},
													RazonSocial : {
														type : "string"
													},
													NombreCompleto : {
														type : "string"
													},
													Nombres : {
														type : "string"
													},
													ApellidoPaterno : {
														type : "string"
													},
													ApellidoMaterno : {
														type : "string"
													},
													Alias : {
														type : "string"
													},
													FechaNacimiento : {
														type : "date"
													},
													IdPaisResidencia : {
														type : "string"
													},
													CodigoPaisResidencia : {
														type : "string"
													},
													NombrePaisResidencia : {
														type : "string"
													},
													FechaBloqueo : {
														type : "date"
													},
													EsCliente : {
														type : "code"
													},
													EsEmpleado : {
														type : "code"
													},
													EsProveedor : {
														type : "code"
													},
													EsContraparte : {
														type : "code"
													},
													EsClienteFinal : {
														type : "code"
													},
													DescripcionSegmento : {
														type : "string"
													},
													NombreTipoBloqueo : {
														type : "string"
													},
													TipoEnrolamiento : {
														type : "string"
													}
												}
											}
										}
									}
								});
						
						$scope.kOptions = {
							columns : [
									{
										field : "FechaIngreso",
										title : $translate
												.instant('UI_FECHA_INGRESO_ABR'),
										template : " <label> #:  moment(FechaIngreso).local().format('DD-MM-YYYY') #</label>",
										format : "{0:d}",
										align : "left",
										width : "105px"
									},
									{
										field : "NombreTipoDocumentoIdentidadDefecto",
										title : $translate
												.instant('UI_TIPO_DOCUMENTO_IDENTIDAD_ABR'),
										align : "left",
										width : "80px"
									},
									{
										field : "DocumentoIdentidadDefecto",
										title : $translate
												.instant('UI_DOCUMENTO_IDENTIDAD_ABR'),
										align : "left",
										width : "100px"
									},
									{
										field : "TipoPersona",
										title : $translate
												.instant('UI_TIPO_PERSONA_ABR'),
										align : "left",
										width : "100px"
									},
									{
										field : "NombreCompleto",
										title : $translate.instant('UI_NOMBRE'),
										align : "left",
										width : "300px"
									},
									{
										field : "NombrePaisResidencia",
										title : $translate
												.instant('UI_PAIS_RESIDENCIA_ABR'),
										align : "left",
										width : "100px"
									},
									{
										field : "EsCliente",
										title : $translate
												.instant('UI_CLIENTE'),
										align : "left",
										width : "70px"
									},
									{
										field : "EsEmpleado",
										title : $translate
												.instant('UI_EMPLEADO_ABR'),
										align : "left",
										width : "70px"
									},
									{
										field : "EsProveedor",
										title : $translate
												.instant('UI_PROVEEDOR_ABR'),
										align : "left",
										width : "70px"
									},
									{
										field : "EsContraparte",
										title : $translate
												.instant('UI_CONTRAPARTE_ABR'),
										align : "left",
										width : "70px"
									},
									{
										field : "EsClienteFinal",
										title : $translate
												.instant('UI_CLIENTE_FINAL'),
										align : "left",
										width : "70px"
									},
									{
										field : "DescripcionSegmento",
										title : $translate
												.instant('UI_SEGMENTO_CLIENTE'),
										align : "left",
										width : "100px"
									},
									{
										field : "NombreTipoBloqueo",
										title : $translate
												.instant('UI_TIPO_BLOQUEO'),
										align : "left",
										width : "100px"
									},
									{
										field : "TipoEnrolamiento",
										title : $translate
												.instant('UI_TIPO_ENROLAMIENTO'),
										align : "left",
										width : "120px"
									} ],
							selectable : "row",
							dataSource : $scope.dskOptions,
							pageable : true,
							sortable : true,
							filterable : true,
							pageSize : 50,
							autoBind : false,
							userSettings : true,
							navigatable : true,
							excel : {
								allPages : false,
							},
							height : 520,
							detailInit : function(e) {
								$scope.initDetalle(e.data);
							}
						};
						
						$scope.gridRelacionados = function(dataItem) {
							var filtersRel = {
								p_IdEmpresa : $scope.$root.SesionIdEmpresa,
								p_IdSocioDeNegocio : Number(dataItem.IdSocioDeNegocio)
							};
							var dsDef = new bcs.DataSource(
									{
										type : "call",
										definition : {
											serverFiltering : true,
											serverPaging : false,
											serverSorting : true,
											pageSize : 6,
											callParameters : filtersRel,
											callTransaction : "176",
											callResultSet : "P_OUT_PERSONASRELACIONADAS",
											schema : {
												model : {
													fields : {
														IdRelacionPersona : {
															type : "number"
														},
														IdPersonaOrigen : {
															type : "number"
														},
														IdPersona : {
															type : "number"
														},
														TipoPersona : {
															type : "string"
														},
														NombreTipoDocumentoIdentidadDefecto : {
															type : "string"
														},
														DocumentoIdentidadDefecto : {
															type : "string"
														},
														NombreCompleto : {
															type : "string"
														},
														NombreTipoRelacion : {
															type : "string"
														},
														IdEstado : {
															type : "string"
														},
														NombreEstado : {
															type : "string"
														},
														FechaInicioRelacion : {
															type : "date"
														},
														FechaFinRelacion : {
															type : "date"
														}
													}
												}
											}
										}
									});

							return {
								dataSource : dsDef,
								columns : [
										{
											field : "TipoPersona",
											title : $translate
													.instant('UI_TIPO_PERSONA'),
											attributes : {
												style : "text-align:left;"
											},
											width : "100px"
										},
										{
											field : "NombreTipoDocumentoIdentidadDefecto",
											title : $translate
													.instant('UI_TIPO_DOCUMENTO'),
											attributes : {
												style : "text-align:left;"
											},
											width : "80px"
										},
										{
											field : "DocumentoIdentidadDefecto",
											template : "<label> #:  IdTipoDocumentoIdentidadDefecto === 'R' ? util.formatRut(DocumentoIdentidadDefecto) : DocumentoIdentidadDefecto #</label>",
											title : $translate
													.instant('UI_NUMERO_DOCUMENTO'),
											attributes : {
												style : "text-align:left;"
											},
											width : "100px"
										},
										{
											field : "NombreCompleto",
											title : $translate
													.instant('UI_RAZON_SOCIAL_O_NOMBRE'),
											attributes : {
												style : "text-align:left;"
											},
											width : "300px"
										},
										{
											field : "NombreTipoRelacion",
											title : $translate
													.instant('UI_TIPO_RELACION'),
											attributes : {
												style : "text-align:left;"
											},
											width : "170px"
										},
										{
											field : "NombreEstado",
											title : $translate
													.instant('UI_ESTADO'),
											attributes : {
												style : "text-align:left;"
											},
											width : "50px"
										},
										{
											field : "FechaInicioRelacion",
											title : $translate
													.instant('UI_FECHA_INICIO'),
											attributes : {
												style : "text-align:left;"
											},
											format : "{0:d}",
											width : "80px"
										},
										{
											field : "FechaFinRelacion",
											title : $translate
													.instant('UI_FECHA_TERMINO'),
											attributes : {
												style : "text-align:left;"
											},
											format : "{0:d}",
											width : "80px",
											template : kendo
													.template("#if (kendo.toString(FechaFinRelacion, 'dd-MM-yyyy')=='31-12-0001') {##} else {##=kendo.toString(FechaFinRelacion, 'dd-MM-yyyy')##}#")
										}, ],
								sortable : false,
								pageable : true,
								autoBind : true,
								height : 150,
								selectable : "row"
							};
						};

						$scope.crearPersona = function() {
							bcsComunes.ventanaAbrir("CrearPersona", {});
						};

						$scope.crearCliente = function() {
							bcsComunes.ventanaAbrir("CrearCliente", {});
						};

						$scope.crearEmpleado = function() {
							bcsComunes.ventanaAbrir("CrearEmpleado", {});
						};

						$scope.crearContraparte = function() {
							bcsComunes.ventanaAbrir("CrearContraparte", {});
						};

						$scope.crearProveedor = function() {
							bcsComunes.ventanaAbrir("CrearProveedor", {});
						};

						$scope.crearClienteNomina = function() {
							bcsComunes.ventanaAbrir("CrearClienteNomina", {});
						};

						$scope.clear = function() {
							$scope.formData = {};
							$scope.formData.tipoSocioNegocio = 0;
							$scope.formData.tipoDocumentoIdentidad = 0;
							
							$scope.kwidget.dataSource.removeAll();
							//$scope.initDetalle(null);
							//$scope.read();
							//console.log();
						}

						var keyEnter = false;
						$scope.fnKeyEnter = function() {
							if (keyEnter == true
									&& $scope.dskOptions.data().length == 1) {
								$scope
										.abrirDetalleSoc($scope.dskOptions
												.data()[0]);
								keyEnter = false;
							} else {
								keyEnter = true;
								$scope.read();
							}
						};

						$scope.getFilters = function() {
							var objfilters = {};
							var niv1filters = new Array();
							objfilters.logic = 'and';
							objfilters.filters = niv1filters;
							niv1filters.push({
								field : "IdEmpresa",
								operator : "eq",
								value : $scope.$root.SesionIdEmpresa
							});
							if ($scope.formData.tipoSocioNegocio) {
								switch ($scope.formData.tipoSocioNegocio) {
								case 'CLI':
									niv1filters.push({
										field : "EsCliente",
										operator : "eq",
										value : 'UI_SI',
										text: 'SI'
									});
									break;
								case 'EMP':
									niv1filters.push({
										field : "EsEmpleado",
										operator : "eq",
										value : 'UI_SI',
										text: 'SI'
									});
									break;
								case 'PRO':
									niv1filters.push({
										field : "EsProveedor",
										operator : "eq",
										value : 'UI_SI',
										text: 'SI'
									});
									break;
								case 'CNT':
									niv1filters.push({
										field : "EsContraparte",
										operator : "eq",
										value : 'UI_SI',
										text: 'SI'
									});
									break;
								case 'CLF':
									niv1filters.push({
										field : "EsClienteFinal",
										operator : "eq",
										value : 'UI_SI',
										text: 'SI'
									});
									break;
								}
							}
							if ($scope.formData.tipoDocumentoIdentidad) {
								niv1filters
										.push({
											field : "IdTipoDocumentoIdentidadDefecto",
											operator : "eq",
											value : $scope.formData.tipoDocumentoIdentidad
										});
							}
							if ($scope.formData.tipoEnrolamiento) {
								niv1filters
										.push({
											field : "TipoEnrolamiento",
											operator : "eq",
											value : $scope.formData.tipoEnrolamiento
										});
							}
							if ($scope.formData.NumeroDocumentoIdentidad) {
								// niv1filters.push({ field:
								// "tolower(DocumentoIdentidadDefecto)",
								// operator: "contains", value:
								// $scope.formData.NumeroDocumentoIdentidad.toLowerCase()
								// });
								niv1filters
										.push({
											field : "tolower(ListaDocumentosIdentidad)",
											operator : "contains",
											value : $scope.formData.NumeroDocumentoIdentidad
													.toLowerCase()
										});
							}

							if ($scope.formData.fechaIngresoDesde != undefined) {
								if (!!$scope.formData.fechaIngresoHasta) {
									var resp = util.validarRangoFecha(
											$scope.formData.fechaIngresoDesde,
											$scope.formData.fechaIngresoHasta);
									if (resp.estado) {
										bcsComunes.msg("error", "Error",
												$translate
														.instant(resp.mensaje));
										return;
									}
								}
								niv1filters
										.push({
											field : "FechaIngreso",
											operator : "ge",
											value : util
													.dateSPseconddate($scope.formData.fechaIngresoDesde)
										});

							}
							if ($scope.formData.fechaIngresoHasta != undefined) {
								niv1filters
										.push({
											field : "FechaIngreso",
											operator : "le",
											value : util
													.dateSPseconddate(util
															.dateTerminoDia($scope.formData.fechaIngresoHasta))
										});
							}

							if ($scope.formData.NombreCompleto) {
								var obj2filters = {};
								var niv2filters = new Array();
								obj2filters.logic = 'or';
								obj2filters.filters = niv2filters;
								// Busqueda Alias
								niv2filters.push({
									field : "tolower(Alias)",
									operator : "contains",
									value : $scope.formData.NombreCompleto
											.toLowerCase()
								});
								// Busqueda DocumentoIdentidadDefecto
								niv1filters
										.push({
											field : "tolower(ListaDocumentosIdentidad)",
											operator : "contains",
											value : ($scope.formData.NumeroDocumentoIdentidad || '')
													.toLowerCase()
										});

								// Buscar socio de negocio con nombre ingresado
								// en cualquier orden (siempre y cuando contenga
								// mas de dos palabras)
								var nombreSplit = $scope.formData.NombreCompleto
										.toLowerCase().split(" ");
								if (nombreSplit.length >= 1) {
									var obj3filters = {};
									var niv3filters = new Array();
									obj3filters.logic = 'and';
									obj3filters.filters = niv3filters;
									for (var i = 0; i < nombreSplit.length; i++) {
										niv3filters.push({
											field : "tolower(NombreCompleto)",
											operator : "contains",
											value : nombreSplit[i]
										});
										if ((i + 1) == nombreSplit.length) {
											niv2filters.push(obj3filters);
										}
									}
								}
								// Adjunta Filtros a listas
								niv1filters.push(obj2filters);

							}
							return objfilters;
						}

						$scope.read = function() {
							var objfilters = $scope.getFilters();

							if ($scope.kwidget && !!objfilters) {
								if (objfilters.filters.length > 0)
									$scope.kwidget.dataSource
											.filter(objfilters);
								else 
									$scope.kwidget.dataSource
											.filter(new Array());									
							}
						};

						// Se obtienen placeholder
						$scope.placeholder = {};
						$scope
								.$watch(
										'formData.tipoDocumentoIdentidad',
										function(newVal) {
											if (newVal !== undefined) {
												if (newVal === 'R')
													$scope.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;
												else if (newVal === 'P')
													$scope.placeholder.documentoIdentidad = '';
												else
													$scope.placeholder.documentoIdentidad = '';
											}
										});

						$scope.exportarClientesFallecidos = function() {
							var datos = {
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_IdUsuario : $scope.SesionIdUsuario,
								p_IdModulo : 'SOC',
								p_TipoPrograma : [ {
									IdInterno : 0,
									IdTipoPrograma : 'GenerarReporteClienteFallecido',
									SegundosDesfase : 0
								} ],
								p_Parametro : [ {
									IdInterno : 0,
									NombreParametro : 'IdEmpresa',
									Valor : $scope.SesionIdEmpresa
								}, {
									IdInterno : 0,
									NombreParametro : 'IdUsuario',
									Valor : $scope.SesionIdUsuario
								}, {
									IdInterno : 0,
									NombreParametro : 'IdModulo',
									Valor : 'SOC'
								} ]
							}
							servicioSociodeNegocio
									.generarReporteClienteFallecidos(datos).$promise
									.then(
											function(result) {
												bcsComunes.ventanaOverlay(
														conSocNeg.ventanaId,
														false);
												var fechaLocal = util
														.dateDS(
																result.P_OUT_PROGRAMA[0].FechaEjecucion,
																"FechaHora",
																"CALL");
												bcsComunes
														.ventanaMsg(
																{
																	mensaje : $translate
																			.instant(
																					'UI_GENERACION_INFORME_BATCH_EXITOSO')
																			.replace(
																					"::IdPrograma",
																					result.P_OUT_PROGRAMA[0].IdPrograma)
																			.replace(
																					"::FechaEjecucion",
																					util
																							.formatoSecondate(fechaLocal)),
																	conflictos : util
																			.parseServerResponse(result)
																},
																conSocNeg.ventanaId);

											},
											function(result) {
												bcsComunes.ventanaOverlay(
														conSocNeg.ventanaId,
														false);
												bcsComunes
														.msg(
																"error",
																"Error",
																$translate
																		.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
											});
						}

						// Menu Cliente Final
						$scope.crearClienteFinal = function() {
							var parametros = {
								clienteFinal : true
							};
							var id1 = bcsComunes.ventanaAbrir(
									"CrearClienteFinal", parametros, false,
									false, conSocNeg.ventanaId);
							var promesa = bcsComunes.ventanaPromesa(id1);

							promesa.then(function(data) {
								// if (data.creado === true){
								// $scope.recordData.IdSocNegPersonaConyuge =
								// data.IdSocioNegocio;
								// }
							});
						};
						//

						$scope.bloqueoDesbloqueoMasivo = function() {

						};

					});
}());
// ------------------------- FIN CONTROLLER --------------------------
