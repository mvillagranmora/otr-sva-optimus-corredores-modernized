angular.module('ModuloNormativa').controller(
		'GridControllerRMO', function GridControllerRMO($scope, $http, $translate, $timeout, bcsComunes, Model, ModelFactory, ModelService,
		Utils, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosNormativa) {
	
	Model = ModelFactory();
	ModelService.setModel(Model);
	
	const admDJ = this;
	const idVentana = bcsComunes.ventanaWinId($scope);
	admDJ.params = bcsComunes.ventanaParams(idVentana);
	const idEntidadReguladora = admDJ.params.IdEntidadReguladora;
	
	$scope.CodigoValidacion = null;
	$scope.esquemaHabilitado = false;
	$scope.modoEdicion = false;
	$scope.fechaDesde = null;
	$scope.fechaHasta = null;
	$scope.documento = null;
	$scope.marcados = [];
	$scope.chkEscogerDiaOculto = false;
	
	//manejar fecha hasta
	$scope.fechaHastaDeshabilitado = false;
	$scope.chkEscogerDia = false;
	
	// Show Tabs y Botones.
	$scope.ShowTabGeneral = true;
	$scope.ShowTabDatosDeclaracion = false;
	$scope.ShowTabDatosDeclarante = false;
	$scope.ShowTabAuditoriaCarga = false;
	$scope.ShowTabAuditoriaCuadratura = false;
	$scope.ShowTabAuditoriaGeneracion = false;
	$scope.ShowTabAuditoriaValidacion = false;
	$scope.ShowBtnCrear = false;
	$scope.ShowBtnCargar = false;
	$scope.ShowBtnCuadrar = false;
	$scope.ShowBtnGenerarCertificado = false;
	$scope.ShowBtnGenerarDeclaracion = false;
	$scope.ShowBtnExportarExcel = false;
	$scope.controlProceso = {};
	$scope.data = {};

	$scope.habilitarEdicion = function() {
		$scope.modoEdicion = true;
	};

	$scope.deshabilitarEdicion = function() {
		$scope.modoEdicion = false;
	};

	$scope.guardarDocumento = function() {
		guardarDocumento();
	}

	var gridCargaRMO = null;
	var gridCuadraturaRMO = null;
	var gridGeneracionRMO = null;
	var gridValidacionRMO = null;
	var gridDataRMO = null;
	var gridSumsRMO = null;
	var listEsquemas = null;
	var listVersiones = null;
	var campoFecha = null;
	var campoRutCliente = null;
	var configuracionMantenedor = null;

	
	function abrirMantenedorRMO(item, masivo) {
		Utils.openWindow('MantenedorOperacionesRMO', {
			item : item,
			idDocumento : $scope.documento.IdDocumento,
			gridScope : $scope,
			masivo : masivo,
			TienePropositoAPV : $scope.TienePropositoAPV,
			Model: Model
		});
	}

	function filtrarInvalidos(item) {
		$scope.establecerEsquemaRMO(item.CodigoValidacion);
	}
	
	$scope.buscarDatos =  function(){
		buscarDetalles();
	}
	
	function buscarDetalles() {
		if ($scope.fechaHasta < $scope.fechaDesde){
	    	 bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 return;
	    }
	    
		var filters = [ {
			field : 'IdDocumento',
			operator : 'eq',
			value : $scope.documento.IdDocumento
		} ];
		if ($scope.CodigoValidacion === null) {
			if (campoFecha != null) {
				if ($scope.fechaDesde != null) {
					filters.push({
						field : campoFecha.Nombre,
						operator : 'gte',
						value : $scope.fechaDesde
					});
				}
				if ($scope.fechaHasta != null) {
					filters.push({
						field : campoFecha.Nombre,
						operator : 'lte',
						value : $scope.fechaHasta
					});
				}
			}
		}else{
			filters.push({
						field : 'CodigoValidacion',
						operator : 'eq',
						value : $scope.CodigoValidacion
					});
		}
		
		//refreshSums = true;
		gridDataRMO.dataSource.filter(filters);
	}

	function limpiarVista() {
		$scope.controlProceso = {};
		$scope.esquemaHabilitado = false;
		Utils.destroyGrid(gridCargaRMO);
		Utils.destroyGrid(gridCuadraturaRMO);
		Utils.destroyGrid(gridGeneracionRMO);
		Utils.destroyGrid(gridValidacionRMO);
		Utils.destroyGrid(gridDataRMO);
		Utils.destroyGrid(gridSumsRMO);
	}

	// Manejo de los links de download de archivos.
	$(document).on(
			'click',
			'a.fileDownload',
			function(e) {
                e.stopImmediatePropagation();
                
				let href = $(this).prop('href');
				let x = href.lastIndexOf('?');
				let search = href.substring(x + 1);
				let obj = JSON.parse('{"'
						+ decodeURI(search).replace(/"/g, '\\"').replace(/&/g,
								'","').replace(/=/g, '":"') + '"}');

				let filename = 'resultado.txt';
				if (obj.hasOwnProperty('nombreArchivo')) {
					filename = obj.nombreArchivo;
				} else if (obj.hasOwnProperty('tipo')) {
					filename = obj.tipo;
					if (obj.hasOwnProperty('idProceso')) {
						filename += '-' + obj.idProceso + '.txt';
					}
				}
				if (obj.hasOwnProperty('zipped') && obj.zipped) {
					filename += '.zip';
				}

				$http({
					method : 'GET',
					responseType : 'blob',
					url : href
				}).then(
						function(response) {
							var file = new File([ response.data ], filename);
							saveAs(file);
							bcsComunes.msg('info', 'Bajar Archivo', 'OK');
						},
						function(response) {
							bcsComunes.msg('error', 'Bajar Archivo',
									response.statusText);
						});

				// this is critical to stop the click event which will trigger
				// a normal file download!
				return false;
			});


	function refreshGrid(grid) {
		$timeout(function() {
			if (grid) {
				grid.refresh();
				if ($scope.documento.IdDocumento != null) {
					$scope.limpiar();
				}
			}
		}, 10);
	}

	$scope.refreshGridCarga = function() {
		refreshGrid(gridCargaRMO);
	}

	$scope.refreshGridCuadratura = function() {
		refreshGrid(gridCuadraturaRMO);
	}

	$scope.refreshGridData = function() {
		refreshGrid(gridDataRMO);
		
	}

	$scope.refreshGridValidacion = function() {
		refreshGrid(gridValidacionRMO);
	}

	$scope.refreshGridGeneracion = function() {
		refreshGrid(gridGeneracionRMO);
	}
	
	$scope.botonLimpiarHabilitado = true;
	
	$scope.limpiar = function() {
		$scope.botonLimpiarHabilitado = false;
		if(idEntidadReguladora === 'RMO'){
			var year = versionRMO.value.substring(0,4);
			var month = versionRMO.value.substring(5,7).replace(/^0+/,"") - 1;
			var startDate = new Date(year, month, 1);
			var endDate = new Date(year, month + 1, 0);
			$scope.fechaHastaDeshabilitado = false;
			$scope.fechaDesde = startDate;
			$scope.fechaHasta = endDate;
					
			$scope.chkEscogerDia = false;	
			$scope.dateOptions = { min: startDate, max: endDate};	
		}
		try {
			buscarDetalles();
		} catch (err) {
			bcsComunes.msg('error', 'Limpiar', err);
		}
		$scope.botonLimpiarHabilitado = true;
	}
	
	$scope.cargarData = function() {
		if ($scope.fechaHasta < $scope.fechaDesde){
	    	 bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 return;
	    }
	    
		Utils.openWindow('CargadorDatosRMO', {fechaDesde: $scope.fechaDesde, fechaHasta: $scope.fechaHasta, datos: admDJ});
	}

	$scope.validarData = function() {
		Utils.openWindow('CuadraturaDatosRMO', {fechaDesde: $scope.fechaDesde, fechaHasta: $scope.fechaHasta, datos: admDJ});
	}

	$scope.complementos = function() {
		Utils.openWindow('ReporteMensualOperacionesComplementos', {datos: admDJ});
	}
	
	$scope.generarDeclaracionRMO = function() {
		var periodicidad = $scope.chkEscogerDia ? 'D' : 'M';
		Utils.openWindow('GenerarDeclaracionRMO', { periodicidad : periodicidad, fechaDesde: $scope.fechaDesde, fechaHasta: $scope.fechaHasta, datos: admDJ});
	}

	$scope.botonExportarHabilitado = true;
	$scope.exportarExcel = function() {
		Utils.openWindow('ExportadorDatosRMO', {datos: admDJ});
	}

	$scope.agregarDetalle = function() {
		abrirMantenedorRMO(null, false);
	}
	
	$scope.$watch('chkEscogerDia', function(newval,oldval) {
		if(idEntidadReguladora === 'RMO'){
			if (newval !== undefined) {	
				var year = versionRMO.value.substring(0,4);
				var month = versionRMO.value.substring(5,7).replace(/^0+/,"") - 1;
				var startDate = new Date(year, month, 1);
				var endDate = new Date(year, month + 1, 0);
				$scope.fechaHastaDeshabilitado = newval;
				$scope.fechaDesde = newval ? startDate : startDate;
				$scope.fechaHasta = newval ? startDate : endDate;
			}
		}
	});
	
	$scope.$watch('fechaDesde', function(newval,oldval) {
		if ($scope.chkEscogerDia) {
			$scope.fechaHasta = $scope.fechaDesde;
		}
	});
	

	$scope.eliminarDetallesMarcados = function() {
		var t = bcsAlerta.crear('PREGUNTA_SI_NO', 'Confirme eliminacion de registros');
		t.result.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				var p = gridDataRMO.dataSource.page();

				var params = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : admDJ.modulo,
					p_Esquema : esquemaRMO.value,
					p_Version : versionRMO.value,
					p_List : []
				};

				for (var i = 0; i < $scope.marcados.length; i++) {
					params.p_List.push({
						ID : $scope.marcados[i]
					});
				}
				
				bcsServiciosNormativa.EliminarDetallesRMO(params).$promise.then(function(respuesta) {
					if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.msg('info', 'Eliminar Detalle', 'OK');
						$scope.marcados = [];
						gridDataRMO.dataSource.page(p);
					} else {
						bcsComunes.msg('error', 'Eliminar Detalle', respuesta.P_OUT_ERRORESDETALLE[0].Codigo);
					}
	
				}, function(error) {
					var msg = "Error";
					if (typeof error.message !== 'undefined') {
						msg = error.message;
					} else if (typeof error.statusText !== 'undefined') {
						msg = error.statusText;
					}
					bcsComunes.msg('error', 'Eliminar Detalle', msg);
				});
			}
		});
	}

	$scope.eliminarTodos = function() {
		var t = bcsAlerta.crear('PREGUNTA_SI_NO','Confirme eliminacion de todos los registros');
		t.result.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				var p = gridDataRMO.dataSource.page();
				
				var params = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : Model.getModulo(),
					p_IdDocumento : $scope.documento.IdDocumento,
					p_Esquema : esquemaRMO.value,
					p_Version : versionRMO.value,
					p_List : []
				};

				bcsServiciosNormativa.EliminarDetallesRMO(params).$promise.then(function(respuesta) {
					if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.msg('info', 'Eliminar Detalle', 'OK');
						$scope.marcados = [];
						gridDataRMO.dataSource.page(p);
					} else {
						bcsComunes.msg('error', 'Eliminar Detalle', respuesta.P_OUT_ERRORESDETALLE[0].Codigo);
					}
				}, function(error) {
					var msg = "Error";
					if (typeof error.message !== 'undefined') {
						msg = error.message;
					} else if (typeof error.statusText !== 'undefined') {
						msg = error.statusText;
					}
					bcsComunes.msg('error', 'Eliminar Detalle', msg);
				});
			}
		});
	}

	var esq = null;
	$scope.establecerEsquemaRMO = function(codigoValidacion) {
		$scope.CodigoValidacion = codigoValidacion;
		limpiarVista();
		
		var esquemaRMO = listEsquemas.value();
		var versionRMO = listVersiones.value();
		
		esq = esquemaRMO;
		
		Model
		.parametros(
				esquemaRMO,
				versionRMO,
				$scope.SesionIdEmpresa,
				function() {
					$scope.TienePropositoAPV = '';
					if (Model.getValorParametro('ExcluyePortafoliosAPV')==='S')
						$scope.TienePropositoAPV ='N';
				}, function(error) {
					$scope.TienePropositoAPV ='';
					console.log('error: ' + error.message);
				});
		
		Model
				.establecer(
						esquemaRMO,
						versionRMO,
						$scope.SesionIdEmpresa,
						function() {
							$scope.documento = Model.getDocumento();
							if ($scope.documento == null)
								return;
							Model
									.establecerConfiguracionMantenedor(
											'FormDetailView',
											$scope.documento.IdTipoDocumento,
											$scope.SesionIdEmpresa,
											function() {
												configuracionMantenedor = Model
														.getConfiguracionMantenedor();
												TabBtnActivo();

												if ($scope.documento == null)
													return;

												gridCargaRMO = $('#gridCargaRMO')
														.kendoGrid(
																Model
																		.getGridCargaOptions())
														.data('kendoGrid');

												gridCuadraturaRMO = $(
														'#gridCuadraturaRMO')
														.kendoGrid(
																Model
																		.getGridCuadraturaOptions())
														.data('kendoGrid');

												gridGeneracionRMO = $(
														'#gridGeneracionRMO')
														.kendoGrid(
																Model
																		.getGridGeneracionOptions())
														.data('kendoGrid');

												gridValidacionRMO = $(
														'#gridValidacionRMO')
														.kendoGrid(
																Model
																		.getGridValidacionOptions())
														.data('kendoGrid');

												gridDataRMO = $('#gridDataRMO')
														.kendoGrid(
																Model
																		.getGridDataOptions(
																				onGridDataBound,
																				codigoValidacion))
														.data('kendoGrid');

												// customize the _show method to
												// call options.beforeShow
												// to allow preventing the
												// tooltip
												// from being shown.
												kendo.ui.Tooltip.fn._show = function(
														show) {
													return function(target) {
														var e = {
															sender : this,
															target : target,
															preventDefault : function() {
																this.isDefaultPrevented = true;
															}
														};

														if (typeof this.options.beforeShow === "function") {
															this.options.beforeShow
																	.call(this,
																			e);
														}
														if (!e.isDefaultPrevented) {
															show.call(this,
																	target);
														}
													};
												}(kendo.ui.Tooltip.fn._show);

												var tooltip = gridDataRMO.thead
														.kendoTooltip({
															filter : 'th',
															beforeShow : function(
																	e) {
																var target = e.target;
																var text = $(
																		target)
																		.text();
																var help = Model
																		.getAyudaCampo(text);
																if (help.length === 0) {
																	e
																			.preventDefault();
																}
															},
															content : function(
																	e) {
																var target = e.target;
																var text = $(
																		target)
																		.text();
																return Model
																		.getAyudaCampo(text);
															}
														});

												$('#gridDataRMO')
														.on(
																'click',
																'.checkbox',
																function() {
																	var checked = this.checked;
																	var row = $(
																			this)
																			.closest(
																					'tr');
																	var id = gridDataRMO
																			.dataItem(row).ID;
																	var x = $scope.marcados
																			.indexOf(id);
																	if (x >= 0) {
																		$scope.marcados
																				.splice(
																						x,
																						1);
																	}
																	if (checked)
																		$scope.marcados
																				.push(id);
																});

												$('#gridDataRMO')
														.on(
																'dblclick',
																'tr.k-state-selected',
																function() {
																	abrirMantenedorRMO(
																			gridDataRMO
																					.dataItem(gridDataRMO
																							.select()),
																			false);
																});

												$('#gridValidacionRMO')
														.on(
																'dblclick',
																'tr.k-state-selected',
																function() {
																	filtrarInvalidos(gridValidacionRMO
																			.dataItem(gridValidacionRMO
																					.select()));
																});
												
												campoFecha = Model
														.getCampoFecha();
												campoRutCliente = Model
														.getCampoRutCliente();

												$scope.esquemaHabilitado = true;
												consultaControlProceso();
												buscarDetalles();
												admDJ.idDocumento = Model.getDocumento().IdDocumento;
												admDJ.esquemaDatos = Model.getSchema();
												admDJ.version = Model.getVersion();
												admDJ.modulo = Model.getModulo();
												admDJ.esquema = Model.getEsquema();
											}, function(error) {
												bcsComunes.msg('error',
														$scope.TITLE,
														error.message);
											});
						}, function(error) {
							bcsComunes
									.msg('error', $scope.TITLE, error.message);
						});
	}

	function onGridDataBound(e) {
	}
	

	function consultaControlProceso() {
		
		var filterTemplate = [ { field: "Esquema", operator:"eq", value: listEsquemas.value() },
		                       { field: "Version",	operator: "eq", value: listVersiones.value() }];
		Model.ConsultaControlProceso.query({ filter: filterTemplate }).then(function(){
			if(Model.ConsultaControlProceso.view().length > 0) {
				$scope.controlProceso.Carga	= Model.ConsultaControlProceso.view()[0].Carga;
				$scope.controlProceso.EstadoCertificado	= Model.ConsultaControlProceso.view()[0].EstadoCertificado;
				$scope.controlProceso.Validacion	= Model.ConsultaControlProceso.view()[0].Validacion;
				$scope.controlProceso.PreDeclaracion	= Model.ConsultaControlProceso.view()[0].PreDeclaracion;
				$scope.controlProceso.Certificacion	= Model.ConsultaControlProceso.view()[0].Certificacion;
			}
		});
    };
	

	var datosEsquemas = null;
	$scope.data.switchDefine = false;
	$scope.tieneCertificadoEsquema = 'N';
	$scope.data.tieneCertificado = false;

	// DataSource de los esquemas especificados.
	// Con esto se llenan las dos primeras listbox de la
	// ventana y, luego se fija el primer esquema visible.
	var dataSourceEsquema = Model.getDataSourceEsquema();
	
	//obtengo los filtros actuales para no perderlos
	var current_filters = dataSourceEsquema.filter().filters;
	//creo la variable para el nuevo filtro
	var new_filter = {};

	new_filter = { field: "IdEntidadReguladora", operator: "eq", value: "RMO" };
	current_filters.push(new_filter);

	//seteo los filtros de la grilla incluyendo el nuevo
	dataSourceEsquema.filter(current_filters);
	
	dataSourceEsquema.fetch().then(function() {
		var versiones = dataSourceEsquema.data();
		
		if (versiones.length < 1) {
			bcsComunes.msg('error', $scope.TITLE, 'No hay esquemas definidos');
			return;
		}

		datosEsquemas = Utils.getUniqueByKey(versiones, 'Esquema');
		Model.setDatosEsquemas(datosEsquemas);
		
		$('#esquemaRMO').kendoDropDownList({
			dataTextField : 'IdEntidadReguladora',
			dataValueField : 'Esquema',
			dataSource : datosEsquemas,
			autobind : true,
			index : 0
		});
		
		listEsquemas = $('#esquemaRMO').data('kendoDropDownList');

		$('#versionRMO').kendoDropDownList({
			dataTextField : 'Version',
			dataValueField : 'Version',
			dataSource : versiones,
			autobind : false,
			cascadeFrom : 'esquemaRMO',
			index : 0,
			change: function(e) {
    				var value = this.value();
    				if(idEntidadReguladora === 'RMO'){
						var year = value.substring(0,4);
						var month = value.substring(5,7).replace(/^0+/,"") - 1;
						var startDate = new Date(year, month, 1);
						var endDate = new Date(year, month + 1, 0);
						$scope.fechaDesde = startDate;
						$scope.fechaHasta = endDate;
						$scope.dateOptions = { min: startDate, max: endDate};
					}
  				}
		});
		listVersiones = $('#versionRMO').data('kendoDropDownList');

		$scope.establecerEsquemaRMO(null);
	});

	function guardarDocumento() {
		var params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : Model.getModulo(),

			p_IdDocumento : $scope.documento.IdDocumento,
			p_RutDeclarante : $scope.documento.DocumentoIdentidadDeclarante,
			p_RutRepresentante : $scope.documento.DocumentoIdentidadRepresentante,
			p_Direccion : $scope.documento.DireccionFisicaDeclaracion,
			p_Comuna : $scope.documento.ComunaDeclaracion,
			p_Email : $scope.documento.DireccionElectronicaDeclaracion,
			p_Telefono : $scope.documento.TelefonoDeclaracion,
			p_FechaCertificado: $scope.documento.FechaCertificado,
			p_DefineFechaCertificado: $scope.data.switchDefine ? 'S' : 'N'
		};

		bcsComunes.msg('info', 'Guardar', 'En proceso...');
		Model.guardarDocumento(params, function(reply) {
			bcsComunes.msg('info', 'Guardar Documento', 'OK');
		}, function(error) {
			bcsComunes.msg('error', 'Guardar Documento', error.message);
		});
	}
	
	function EsquemaTieneCertificado(esq) {
		for (i = 0; i < datosEsquemas.length; i++) {
			if (datosEsquemas[i].Esquema == esq) {
				$scope.tieneCertificadoEsquema = datosEsquemas[i].TieneCertificado;
				$scope.data.switchDefine = $scope.documento.DefineFechaCertificado == "S" ? true : false;
				if($scope.tieneCertificadoEsquema === 'S'){
					$scope.data.tieneCertificado = false;
				}else{
					$scope.data.tieneCertificado = true;
				}
				return;
			}
		}
	}

	function TabBtnActivo() {
		$scope.ShowTabDatosDeclaracion = false;
		$scope.ShowTabDatosDeclarante = false;
		$scope.ShowTabAuditoriaCarga = false;
		$scope.ShowTabAuditoriaCuadratura = false;
		$scope.ShowTabAuditoriaGeneracion = false;
		$scope.ShowTabAuditoriaValidacion = false;
		$scope.ShowBtnCrear = false;
		$scope.ShowBtnCargar = false;
		$scope.ShowBtnCuadrar = false;
		$scope.ShowBtnGenerarCertificado = false;
		$scope.ShowBtnGenerarDeclaracion = false;
		$scope.ShowBtnExportarExcel = false;
		
		EsquemaTieneCertificado(esq);
		
		for (i = 0; i < configuracionMantenedor.length; i++) {
			switch (configuracionMantenedor[i].IdObjeto) {
			case 'FormDetailView_TabDatosDeclaracion':
				$scope.ShowTabDatosDeclaracion = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_TabDatosDeclarante':
				$scope.ShowTabDatosDeclarante = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_TabAuditoriaCarga':
				$scope.ShowTabAuditoriaCarga = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_TabAuditoriaCuadratura':
				$scope.ShowTabAuditoriaCuadratura = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_TabAuditoriaGeneracion':
				$scope.ShowTabAuditoriaGeneracion = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_TabAuditoriaValidacion':
				$scope.ShowTabAuditoriaValidacion = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_BtnCrear':
				$scope.ShowBtnCrear = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_BtnCargar':
				$scope.ShowBtnCargar = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_BtnCuadrar':
				$scope.ShowBtnCuadrar = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_BtnGenerarCertificado':
				$scope.ShowBtnGenerarCertificado = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_BtnGenerarDeclaracion':
				$scope.ShowBtnGenerarDeclaracion = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			case 'FormDetailView_BtnExportarExcel':
				$scope.ShowBtnExportarExcel = (configuracionMantenedor[i].ValorActivo == 'S') ? true
						: false;
				break;
			default:
				break;
			}
		}
		return false;
	}
	
	
});
