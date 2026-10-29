angular.module('ModuloNormativa').controller(
		'GridController',
		[ '$scope', '$element','$http', '$translate', '$timeout', '$interval', 'bcsComunes', 'Model', 'ModelFactory', 'ModelService',
				'Utils', 'bcsAlerta', 'OPCIONES_BCSALERTA', 'bcsServiciosNormativa', GridController ]);

function GridController($scope, $element, $http, $translate, $timeout, $interval, bcsComunes, Model,ModelFactory, ModelService,
		Utils, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosNormativa) {
	
	var admDJ = this;
	admDJ.params = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	var $contenedor = $($element);
	//ABBI
	var idVentana = bcsComunes.ventanaWinId($scope);
	var idEntidadReguladora = admDJ.params.IdEntidadReguladora;
	$scope.cmbbx		= {};
	$scope.cmbbx.IdMmonedaPrimariaObj= null;
	$scope.cmbbx.dMmonedaPrimaria=null;
	Model = ModelFactory();
	ModelService.setModel(Model);
	/*
	if (admDJ.params.TipoUI =='DOCUMENTOS')
	{$scope.TITLE = $translate.instant('UI_MANTENEDOR_DE_DOCUMENTOS');}else{$scope.TITLE = $translate.instant('UI_VISOR');};
	*/
	//INICIO - ABBI
	$scope.TITLE = $translate.instant('UI_NOMBRE_DOCUMENTO');
	$scope.textoBotonCuadrar = 'UI_CUADRAR';
	$scope.CodigoValidacion = null;
	$scope.esquemaHabilitado = true;
	$scope.modoEdicion = false;
	$scope.filtroFechasOculto = false;
	$scope.filtroClienteOculto = false;
	$scope.fechaDesde = null;
	$scope.fechaHasta = null;
	$scope.documento = null;
	//$scope.modulo = null;
	$scope.socioNegocioSeleccionado = null;
	$scope.idSocioNegocioActual = false;
	$scope.marcados = [];
	$scope.todos = [];
	$scope.chkEscogerDiaOculto = idEntidadReguladora === 'BCC'? false : true;
	$scope.EsDJ1 = idEntidadReguladora === 'BCC'? true : false;
	$scope.seteaFiltroGrilla = true;
	//Datos solo para SICAM
	$scope.esRepo = false;
	$scope.dataSicam = [];
	$scope.primerSeteo = false;
	$scope.tieneEnvio = false;
	$scope.tieneTotales = true;
	$scope.tieneMonedaPrimaria = false;
	
	//manejar fecha hasta
	$scope.fechaHastaDeshabilitado = false;
	$scope.chkEscogerDia = false;
	
	// Show Tabs y Botones.
	$scope.ShowTabGeneral = true;
	$scope.ShowTabDatosDeclaracion = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowTabDatosDeclarante = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowTabAuditoriaCarga = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowTabAuditoriaCuadratura = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowTabAuditoriaGeneracion = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowTabAuditoriaValidacion = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnModuloAplicar = true;
	$scope.ShowBtnExportarExcel = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnSelTodo = idEntidadReguladora === 'BCC'? true : false;
	
	$scope.ShowBtnCrear = idEntidadReguladora === 'BCC'? false : true;
	$scope.ShowBtnEliminar = idEntidadReguladora === 'BCC'? false : true;
	$scope.ShowBtnCargar = idEntidadReguladora === 'BCC'? false : true;
	$scope.ShowBtnCuadrar = idEntidadReguladora === 'BCC'? false : true;
	$scope.ShowBtnGenerarCertificado = idEntidadReguladora === 'BCC'? false : true;
	$scope.ShowBtnGenerarDeclaracion = idEntidadReguladora === 'BCC'? false : true;
	
	$scope.ShowBtnCrearBC = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnEliminarBC = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnCargarBC = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnCuadrarBC = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnRectificar = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowBtnGenerarDeclaracionBC = idEntidadReguladora === 'BCC'? true : false;
	$scope.ShowGridSumsEstandar= true;
	$scope.ShowGridSumsSicam= false;
	
	$scope.controlProceso = {};
	//FIN - ABBI
	$scope.data = {}
	
	$scope.IntervaloCargado = null;

	$scope.habilitarEdicion = function() {
		$scope.modoEdicion = true;
		
		if($scope.documento !== null){
			$scope.data.switchDefine = $scope.documento.DefineFechaCertificado == "S" ? true : false;
		}
	};
	$scope.cmbbx.MmonedaPrimaria={
				filtros : {
					Activo  : { field: "Codigo", operator: "neq", value: ' ' }
				},
	};

	$scope.deshabilitarEdicion = function() {
		$scope.modoEdicion = false;
	};

	$scope.guardarDocumento = function() {
		guardarDocumento();
	}

	$scope.configSocioNegocio = {
		filtros : {
			IdEmpresa : {
				'field' : 'IdEmpresa',
				'operator' : 'eq',
				'value' : $scope.SesionIdEmpresa
			},
			EstadoCliente : {
				'field' : 'EstadoCliente',
				'operator' : 'eq',
				'value' : 'ACT'
			},
			EsCliente : {
				'field' : 'EsCliente',
				'operator' : 'eq',
				'value' : 'UI_SI'
			}
		},
	};

	var gridCarga = null;
	var gridCuadratura = null;
	var gridGeneracion = null;
	var gridValidacion = null;
	var gridData = null;
	var gridSums = null;
	var listModulos = null;
	var listEsquemas = null;
	var listVersiones = null;
	var campoFecha = null;
	var campoRutCliente = null;
	var configuracionMantenedor = null;
		
	function abrirMantenedor(item, masivo) {
		Utils.openWindow('MantenedorDeclaraciones', {
			item : item,
			idDocumento : $scope.documento.IdDocumento,
			gridScope : $scope,
			masivo : masivo,
			TienePropositoAPV : $scope.TienePropositoAPV,
			Model: Model
		});
	}

	function filtrarInvalidos(item) {
		$scope.establecerEsquema(item.CodigoValidacion);
	}
	
	$scope.buscarDatos =  function(){
		buscarDetalles();
	}
	
	function buscarDetalles() {
		var filters = [ {
			field : 'IdDocumento',
			operator : 'eq',
			value : $scope.documento.IdDocumento
		} ];
		if ($scope.CodigoValidacion === null) {
			if ($scope.idSocioNegocioActual
					&& $scope.socioNegocioSeleccionado != null
					&& typeof $scope.socioNegocioSeleccionado.IdSocioDeNegocio !== 'undefined') {
				filters
						.push({
							field : campoRutCliente.Nombre,
							operator : 'startswith',
							value : $scope.socioNegocioSeleccionado.DocumentoIdentidadDefecto
						});
			}
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
			if (Model.getEsquema()=='REPO2P1'){
				if ($scope.cmbbx.IdMmonedaPrimariaObj != null && $scope.cmbbx.IdMmonedaPrimaria != 0) {
					filters.push({
						field : 'MonedaPrimaria',
						operator : 'eq',
						value : $scope.cmbbx.IdMmonedaPrimaria
					});
				}
			}
		}
		$scope.marcados = [];
		refreshSums = true;
		if ($scope.seteaFiltroGrilla)
		{
			gridData.dataSource.filter(filters);
		}else{
			gridData.dataSource.read();
			gridData.refresh();
		};
		$scope.seteaFiltroGrilla=true;
	}

	function limpiarVista() {
		$scope.controlProceso = {};
		$scope.esquemaHabilitado = false;
		Utils.destroyGrid(gridCarga);
		Utils.destroyGrid(gridCuadratura);
		Utils.destroyGrid(gridGeneracion);
		Utils.destroyGrid(gridValidacion);
		Utils.destroyGrid(gridData);
		Utils.destroyGrid(gridSums);
	}

	// Manejo de los links de download de archivos.
	$(document).on(
			'click',
			'a.fileDownload',
			function(e) {
				e.stopImmediatePropagation();
				var tempHref = $(this).prop('href');
				var x = tempHref.lastIndexOf('?');
				var search = tempHref.substring(x + 1);
				var obj = JSON.parse('{"'
						+ decodeURI(search).replace(/"/g, '\\"').replace(/&/g,
								'","').replace(/=/g, '":"') + '"}');

				var filename = 'resultado.txt';
				if (obj.hasOwnProperty('nombreArchivo')) {
					filename = obj.nombreArchivo;
				} else if (obj.hasOwnProperty('tipo')) {
					filename = obj.tipo;
					if (obj.hasOwnProperty('idProceso')) {
						filename += '-' + obj.idProceso + '.txt';
					}
				}
				var zipped = 'N';
				var ruta = 'nor/'+filename;
				if (obj.hasOwnProperty('zipped') && obj.zipped) {
					zipped = 'S';
				}
				var data = zipped == 'S' ? {file: ruta, zipped: zipped} : {file: ruta}

				var servicioDescarga = '/BSCL/Plataforma/GWJAVA/PLA/download?'
				var href = $(this).prop('href').split('/BSCL')[0]+servicioDescarga;

				var filenameOutput = zipped == 'S' ? filename.split('.')[0]+'.zip' : filename;

				$http({
					method: 'POST',
					url: href,
					responseType: 'blob',
					params: data
				}).success(function(data){
					var file = new File([data], filenameOutput);
					saveAs(file);
				}).error(function(err){
					bcsComunes.msg('error', 'Ocurrió un problema al descargar el archivo');
				});
				// this is critical to stop the click event which will trigger
				// a normal file download!
				return false;
			});

	var refreshSums = false;

	function refreshGrid(grid) {
		$timeout(function() {
			if (grid)
				grid.refresh();
		}, 10);
	}

	$scope.refreshGridCarga = function() {
		refreshGrid(gridCarga);
	}

	$scope.refreshGridCuadratura = function() {
		refreshGrid(gridCuadratura);
	}

	$scope.refreshGridData = function() {
		refreshGrid(gridData);
	}

	$scope.refreshGridValidacion = function() {
		refreshGrid(gridValidacion);
	}

	$scope.refreshGridGeneracion = function() {
		refreshGrid(gridGeneracion);
	}

	$scope.botonLimpiarHabilitado = true;
	$scope.limpiar = function() {
		$scope.botonLimpiarHabilitado = false;
		$scope.idSocioNegocioActual = false;
		//INICIO - ABBI
		if(idEntidadReguladora === 'BCC' && listVersiones !== null){
			var year = listVersiones.value().substring(0,4);
			var month = listVersiones.value().substring(5,7).replace(/^0+/,"") - 1;
			var startDate = new Date(year, month, 1);
			var endDate = new Date(year, month + 1, 0);
			$scope.fechaHastaDeshabilitado = false;
			$scope.fechaDesde = util.dateSPdate(startDate);
			$scope.fechaHasta = util.dateSPdate(endDate);
			
			$scope.chkEscogerDia = false;		
		}
			if (Model.getEsquema()=='REPO2P1'){
				if ($scope.cmbbx.IdMmonedaPrimariaObj != null && $scope.cmbbx.IdMmonedaPrimaria != 0) {
					$scope.cmbbx.IdMmonedaPrimaria = 0;
				}
		    }
		//FIN - ABBI
		try {
			buscarDetalles();
		} catch (err) {
			bcsComunes.msg('error', 'Limpiar', err);
		}
		$scope.botonLimpiarHabilitado = true;
	}
	$scope.moduloAplicar = function() {
		$scope.ShowTabGeneral=true;
		$scope.ShowBtnModuloAplicar = false;
		$scope.establecerEsquema(null);
	}
	
	$scope.cargarData = function() {
		Utils.openWindow('CargadorDatos', {Model: Model});
	}

	$scope.edicionMasiva = function() {
		abrirMantenedor(null, true);
	}

	$scope.cuadrarData = function() {
		Utils.openWindow('CuadraturaDatos', {Model: Model});
	}

	$scope.generarCertificado = function() {
		Utils.openWindow('TributariosCertificados', { esquema : listEsquemas.value(), version : listVersiones.value() });
	}

	$scope.complementos = function() {
		Utils.openWindow('TributariosComplementos', { esquema : listEsquemas.value(), version : listVersiones.value() });
	}
	
	$scope.generarDeclaracion = function() {
		var rectifica = 'N';
		if (idEntidadReguladora === 'BCC'){
			var currentDataSource = Model.getDataSourceDetalles(null);
			currentDataSource.pageSize(0);
			
			var filters = configurarFiltro();
			currentDataSource.filter(filters);
			
			currentDataSource.query().then(function(){
				var rectificados = currentDataSource.view().filter((e) => (e.Rectifica!=null && e.Rectifica=='S'));
				if (rectificados.length>0 && !$scope.chkEscogerDia)
					rectifica = 'S';
				var periodicidad = $scope.chkEscogerDia ? 'D' : 'M';
				var fechaDesde = $scope.fechaDesde !== null ? $scope.fechaDesde : "";
				var fechaHasta = $scope.fechaDesde !== null ? $scope.fechaHasta : "";
				Utils.openWindow('GenerarDeclaracion', { Periodicidad : periodicidad, FechaDesde: fechaDesde, FechaHasta: fechaHasta, Rectifica: rectifica, Model: Model});			
			});
		} else{
			var periodicidad = $scope.chkEscogerDia ? 'D' : 'M';
			var fechaDesde = $scope.fechaDesde !== null ? $scope.fechaDesde : "";
			var fechaHasta = $scope.fechaDesde !== null ? $scope.fechaHasta : "";
			Utils.openWindow('GenerarDeclaracion', { Periodicidad : periodicidad, FechaDesde: fechaDesde, FechaHasta: fechaHasta, Rectifica: rectifica, Model: Model});			
		}
	}

	$scope.botonExportarHabilitado = true;
	$scope.exportarExcel = function() {
		Utils.openWindow('ExportadorDatos', {Model: Model});
	}

	$scope.agregarDetalle = function() {
		abrirMantenedor(null, false);
	}
	
	// Funcion que revisa si dataSource aun esta procesando solicitudes
	function cargaTerminada() {
		console.log("el otro");
		console.log(gridData.dataSource._requestInProgress);
		if (!gridData.dataSource._requestInProgress){
			console.log("listo");
			$scope.checkMarcados();
			$interval.cancel($scope.IntervaloCargado);
		}
	};
	
	// Marca los checkbox correspondientes al cambiar de pagina
	$scope.$watch(function($scope) {
		if(gridData!=null && gridData.dataSource!=null)
			return gridData.dataSource.page();
		else return 0;
		},function(nuevo) {
			if(nuevo>0 && gridData.dataSource.view().length>0){
				if (gridData.dataSource._requestInProgress){
					if ($scope.IntervaloCargado!=null)
						$interval.cancel($scope.IntervaloCargado);
					$scope.IntervaloCargado = $interval(cargaTerminada,50);
				} else {
					if ($scope.IntervaloCargado!=null)
						$interval.cancel($scope.IntervaloCargado);
					$scope.checkMarcados();
				}
			}
		}
	);
	
	//INICIO - ABBI
	
	$scope.$watch('fechaDesde', function(newval,oldval) {
		if(idEntidadReguladora === 'BCC'){
			if ($scope.chkEscogerDia) {
				$scope.fechaHasta = $scope.fechaDesde;
			}
		}
	});
	//FIN - ABBI
	
	$scope.rectificarMarcados = function() {
		Utils.openWindow('RectificarMarcados', {marcados: $scope.marcados, gridData: gridData, buscarDatos: $scope.buscarDatos});
	}
	
	$scope.seleccionarTodo = function() {
		var inter = $scope.todos.filter(e => $scope.marcados.includes(e.ID));
		if (inter.length!=$scope.todos.length){
			$scope.todos.forEach(function(e) {
				$scope.marcados.push(e.ID);
			});
		} else $scope.marcados = [];
		$scope.checkMarcados();
	}


	
	$scope.checkMarcados = function() {
		var checkboxes = $contenedor.find('#gridData :checkbox');
		for (var i=0;i<checkboxes.length;i++){
			var row =  $contenedor.find(checkboxes[i]).closest('tr');
			var id = gridData.dataItem(row).ID;
			if ($scope.marcados.includes(id))
				checkboxes[i].checked = true;
			else checkboxes[i].checked = false;
		}
	}
	
	$scope.botonEliminarHabilitado = true;
	$scope.eliminarDetallesMarcados = function() {
		$scope.botonEliminarHabilitado = false;
		var t = bcsAlerta.crear('PREGUNTA_SI_NO',
				'Confirme eliminacion de registros');
		t.result.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				var p = gridData.dataSource.page();

				var params = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : Model.getModulo(),
					p_List : []
				};

				for (var i = 0; i < $scope.marcados.length; i++) {
					params.p_List.push({
						ID : $scope.marcados[i]
					});
				}
				

				Model.callService('EliminarDetalles', params, function(reply) {
					if(reply.P_OUT_ADVERTENCIASCONTADOR > 0){
						var msj = reply.P_OUT_ADVERTENCIASDETALLE[0].Descripcion;
						bcsComunes.msg('warning','Eliminar Detalle',msj);
					}else{
						bcsComunes.msg('info', 'Eliminar Detalle', 'OK');
					}
					buscarDetalles();
					gridData.dataSource.page(p);
					$scope.botonEliminarHabilitado = true;
				}, function(error) {
					bcsComunes.msg('error', 'Eliminar Detalle', error.message);
					$scope.botonEliminarHabilitado = true;
				});
			}
		});
	}

	//TO DO Ajara > Metodo para enviar los registros marcados a API SICAM a travez del servicio java.
	//@params, se incluye una lista de ids para los registros seleccionados.
	//@output, dependiendo del code, se despliega el modal informativo (verificar si esto será así).

	$scope.enviarDetallesMarcados = function() {
		$scope.botonEnviarHabilitado = false;
		let mensaje = `Desea enviar ${$scope.marcados.length} registros a SICAM`;
		var t = bcsAlerta.crear('PREGUNTA_SI_NO', mensaje);
		t.result.then(function (result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				bcsComunes.msg('warning', 'Aviso Sistema', 'Reporte en espera de respuesta por parte de SICAM');
				var p = gridData.dataSource.page();
				var params = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : Model.getModulo(),
					p_IdEsquema: listEsquemas.value(),
					p_Version: listVersiones.value(),
					p_Reporte : $scope.marcados.map(id => ({ ID: id }))
				};
				bcsServiciosNormativa.EnviarSicam(params).$promise.then(function (respuesta) {
					let msj = "";
					if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.msg('error', 'Respuesta SICAM', respuesta.P_OUT_MESSAJE);
						buscarDetalles();
						return;
					}
					if (respuesta.P_OUT_ERRORESCONTADOR === 0 &&
						(respuesta.P_OUT_WARNINGCONTADOR > 0 || respuesta.P_OUT_NOENVIADOSCONTADOR > 0)) {
						
						if (respuesta.P_OUT_NOENVIADOSCONTADOR > 0) {
							msj += `${respuesta.P_OUT_NOENVIADOSCONTADOR} se enviaron anteriormente. `;
						}
						if (respuesta.P_OUT_WARNINGCONTADOR > 0) {
							msj += `${respuesta.P_OUT_WARNINGCONTADOR} registros presentan error`;
						}

						bcsComunes.msg('warning', 'Respuesta SICAM', msj.trim());
						buscarDetalles();
						return;
					}
					msj = `${$scope.marcados.length} registros ingresados correctamente a SICAM`;
					bcsComunes.msg('info', 'Respuesta SICAM', msj);
					buscarDetalles();
				});
			}
		});
	};


	
	//TO DO Ajara -> Funcion para obtener fecha reporte SICAM y dejar como fecha desde y hasta en mantenedor
	//Si el esquema seleccionado es REPOxxx, se deja como fecha desde, la fecha reporte?fecha sistema lo mismo para fecha hasta
	//Si el esquema seleccionado es REPO3P1, se marca el chek escoger día
	$scope.fnFechaReporte = function (version) {
		var esquema = listEsquemas.value();
		var key = 'REPO3P1';
		var year = version.substring(0, 4);
		var month = version.substring(5, 7);
		var fechaDesde = util.dateSPdate(new Date(parseInt(year, 10), parseInt(month, 10) - 1, 1));
		var fechaHasta = util.dateSPdate(new Date(parseInt(year, 10), parseInt(month, 10), 0));
		var checkDia = false;
		if (esquema && key === esquema) {
			bcsServiciosNormativa.ObtenerFechaREPO3P1().$promise.then(function(respuesta) {
				var fechaREPO = (respuesta && respuesta.d && respuesta.d.results && respuesta.d.results.length > 0)
					? respuesta.d.results[0].FechaReporte
					: $scope.SesionFechaSistema;
				var refYear = fechaREPO.substring(0, 4);
				var refMonth = fechaREPO.substring(5, 7);
				if (year === refYear && month === refMonth) {
					$scope.fechaHasta = fechaREPO;
					$scope.fechaDesde = fechaREPO;
					checkDia = true;
				}else{
					$scope.fechaDesde = fechaDesde;
					$scope.fechaHasta = fechaHasta;
				}
				$scope.chkEscogerDia = checkDia;
			});
		} else {
			$scope.fechaDesde = fechaDesde;
			$scope.fechaHasta = fechaHasta;
			$scope.chkEscogerDia = checkDia;
		}
	}



	//TO DO Ajara -> Funcion para mostrar los botones Generar Repo y Enviar a Sicam, si y solo si la entidad es BCC, se selecionó un esquema
	//Si el equema seleccionado es REPOxxx, se obtienen las primeras 4 letras "REPO" y se compara con la key REPO. Si esto ocurre se muestra el botón
	$scope.mostrarBtnSicam = function(){
		if(idEntidadReguladora === 'BCC' && listEsquemas != null){
			var esquema = listEsquemas.value().match(/^[A-Za-z]+/);
			var key = 'REPO';
			if(esquema && key === esquema[0]){
				$scope.esRepo = true;
				$scope.ShowBtnGenerarDeclaracionBC = false;
				$scope.ShowBtnGenerarDeclaracion = false;
				$scope.ShowBtnGenerarCertificado = false;
				$scope.ShowBtnRectificar = false;
				return $scope.esRepo;
			}
		}
		return false;
	}

	//TO DO Ajara -> Funcion para habilitar el Boton Enviar a Sicam. en base a 4 parametros.
	//Esquema haiblitado = true
	//marcados.length > 0, existen elementos seleccionados en la grilla
	//marcados.some < date, verifica si algun elemento en la lista de seleccionados es de fecha operación menor a la fecha del sistema
	$scope.habilitarBtnEnviar = function() {
		const esquema = listEsquemas?.value()?.match(/^[A-Za-z]+/);
		if (idEntidadReguladora === 'BCC' && listEsquemas != null &&
			esquema?.[0] === 'REPO' && $scope.esquemaHabilitado &&
			$scope.marcados.length > 0) {
				const habilitar = $scope.marcados.every(marcado => {
					const registro = $scope.dataSicam.find(d => d.ID === marcado);
					if (!registro || !registro.Estado || $scope.tieneEnvio == false) return false; 
					return registro.Estado == 'ACT';
				});
				return habilitar;					
		}
		return false;
	};

	//TO DO Ajara -> Funcion para habilitar el Boton Enviar a Sicam. en base a 4 parametros.
	//Esquema haiblitado = true
	//marcados.length > 0, existen elementos seleccionados en la grilla
	//marcados.some < date, verifica si algun elemento en la lista de seleccionados tiene EstadoSICAM
	$scope.habilitarBtnEiminar = function() {
		const esquema = listEsquemas?.value()?.match(/^[A-Za-z]+/);
		if (idEntidadReguladora === 'BCC' &&
			esquema?.[0] === 'REPO' &&
			$scope.esquemaHabilitado &&
			$scope.marcados.length > 0) {
			return !$scope.marcados.some(marcado => {
				const registro = $scope.dataSicam.find(d => d.ID === marcado);
				return registro && registro.EstadoSICAM !== '' && registro.EstadoSICAM != null;
			});
		}
		return $scope.esquemaHabilitado && $scope.marcados.length > 0;
	};

	//TO DO Ajara -> Funcion para ejecutar la ventana Generar REPO SICAM
	$scope.generarREPOSicam = function() {
		Utils.openWindow('GenerarRepoSicam', { esquema : listEsquemas.value(), version : listVersiones.value(), fechaDesde : $scope.fechaDesde, fechaHasta : $scope.fechaHasta, Model: Model });
	}


	$scope.eliminarTodos = function() {
		$scope.botonEliminarHabilitado = false;
		var t = bcsAlerta.crear('PREGUNTA_SI_NO',
				'Confirme eliminacion de todos los registros');
		t.result.then(function(result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				var p = gridData.dataSource.page();

				var params = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : Model.getModulo(),
					p_IdDocumento : $scope.documento.IdDocumento,
					p_List : []
				};

				Model.callService('EliminarDetalles', params, function(reply) {
					bcsComunes.msg('info', 'Eliminar Detalle', 'OK');
					buscarDetalles();
					gridData.dataSource.page(p);
				}, function(error) {
					bcsComunes.msg('error', 'Eliminar Detalle', error.message);
				});
			}
		});
	}

	$scope.getPeriodo = function(fecha) {
		if (typeof fecha === 'string') {
			const [año, mes] = fecha.split('-');
			return `${año}/${mes}`;
		}
		const año = fecha.getFullYear();
		const mes = (fecha.getMonth() + 1).toString().padStart(2, '0');
		return `${año}/${mes}`;
	}
	
	/* Toma el codigo de validacion de error seleccionado */
	var esq = null;
	$scope.establecerEsquema = function(codigoValidacion) {
		$scope.CodigoValidacion = codigoValidacion;
		limpiarVista();
		//var modulo =  listModulos.value();
		/* Toma la DJ y el año de la DJ */
		var esquema = listEsquemas.value();
		var version = listVersiones.value();

		if (
  ($scope.fechaDesde && $scope.fechaDesde !== '') &&
  ($scope.fechaHasta && $scope.fechaHasta !== '')
) {
    var validarFechaDesde = $scope.getPeriodo($scope.fechaDesde);
    var validarFechaHasta = $scope.getPeriodo($scope.fechaHasta);

    if (validarFechaDesde !== version || validarFechaHasta !== version) {
        const campos = [
            validarFechaDesde !== version ? 'Fecha Desde' : null,
            validarFechaHasta !== version ? 'Fecha Hasta' : null
        ].filter(Boolean).join(' y ');

        bcsComunes.msg(
            'warning',
            campos,
            'Debe estar contenida dentro de la versión seleccionada'
        );
    }
}

		
		esq = esquema;
		
		Model
		.parametros(
				esquema,
				version,
				$scope.SesionIdEmpresa,
				function() {
					$scope.TienePropositoAPV = '';
					if (Model.getValorParametro('ExcluyePortafoliosAPV')==='S')
						$scope.TienePropositoAPV ='N';
				}, function(error) {
					/*bcsComunes
							.msg('error', $scope.TITLE, error.message);*/
					$scope.TienePropositoAPV ='';
					console.log('error: ' + error.message);
				});
		
		Model
				.establecer(
						esquema,
						version,
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
												if ($scope.ShowGridSumsEstandar)
												{ 
												Model
														.getDataSourceTotales(
																function(
																		dataSource) {
																	gridSums = $contenedor.find('#gridSums')
																			.kendoGrid(
																					Model
																							.getGridSumsOptions(dataSource))
																			.data(
																					'kendoGrid');
																},
																function(error) {
																	bcsComunes
																			.msg(
																					'error',
																					$scope.TITLE,
																					error.message);
																});
												};					
												if ($scope.ShowGridSumsSicam)
												{ 
													Model
															.getDataSourceTotalesSICAM(
																	function(
																			dataSource) {
																		gridSums = $contenedor.find('#gridSums')
																				.kendoGrid(
																						Model
																								.getGridSumsOptionsSICAM(dataSource))
																				.data(
																						'kendoGrid');
																	},
																	function(error) {
																		bcsComunes
																				.msg(
																						'error',
																						$scope.TITLE,
																						error.message);
																	});
												};	

												if ($scope.documento == null)
													return;

												gridCarga = $contenedor.find('#gridCarga')
														.kendoGrid(
																Model
																		.getGridCargaOptions())
														.data('kendoGrid');

												gridCuadratura = $contenedor.find('#gridCuadratura')
														.kendoGrid(
																Model
																		.getGridCuadraturaOptions())
														.data('kendoGrid');

												gridGeneracion = $contenedor.find('#gridGeneracion')
														.kendoGrid(
																Model
																		.getGridGeneracionOptions())
														.data('kendoGrid');

												gridValidacion = $contenedor.find('#gridValidacion')
														.kendoGrid(
																Model
																		.getGridValidacionOptions())
														.data('kendoGrid');
												/* Obtiene todos los campos de la DJ seleccionada */
												gridData = $contenedor.find('#gridData')
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

												var tooltip = gridData.thead
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
									
												$contenedor.find('#gridData')
														.on(
																'click',
																'.checkbox',
																function() {
																	var checked = this.checked;
																	var row = $(
																			this)
																			.closest(
																					'tr');
																	var id = gridData
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

												$contenedor.find('#gridData')
														.on(
																'dblclick',
																'tr.k-state-selected',
																function() {
																	if(idEntidadReguladora === 'BCC' && listEsquemas != null){
																		var esquema = listEsquemas.value().match(/^[A-Za-z]+/);
																		var registro = gridData.dataItem(gridData.select());
																		if (esquema && esquema[0] === 'REPO') {
																			if (registro.EstadoSICAM && (registro.EstadoSICAM.trim() == 'REC' || registro.EstadoSICAM.trim() == 'ENV')) {
																				bcsComunes.msg('warning', 'Acción no permitida', 'Este registro ya fue enviado a SICAM y no puede ser editado.');
																				return;
																			}
																		}
																	}										
																	abrirMantenedor(
																			gridData
																					.dataItem(gridData
																							.select()),
																			false);
																});

												$contenedor.find('#gridValidacion')
														.on(
																'dblclick',
																'tr.k-state-selected',
																function() {
																	/* Contiene los detalles de la validacion, codigo, descripcion , cant de casos, fecha */
																	filtrarInvalidos(gridValidacion
																			.dataItem(gridValidacion
																					.select()));
																});
												if(idEntidadReguladora === 'BCC' && Model.getEsquema()=='DJ1'){
													$contenedor.find('#context-menu').kendoContextMenu({
          												target: "#gridData",
														dataSource:[{ text: "Ver Detalles", attr: {id: 4}}],                    
                         								filter: "tr[role='row']",
           		 										select: function(e) {
                    											var grid = $contenedor.find('#gridData').data("kendoGrid");
																var dataItem = grid.dataItem($(e.target));
                    											var tab = e.item.id;
																var keyCabecera = { 
																		KRutCtraparte1 : dataItem.KRutCtraparte1,
																		KIdContrato : dataItem.KIdContrato,
																		KFechaContrato :dataItem.KFechaContrato,
																		KIdOpeEstruc :dataItem.KIdOpeEstruc,
																		kProducto :dataItem.kProducto,
																		kPeriodoComercial : dataItem.kPeriodoComercial
																}
																
																abrirModal(tab,keyCabecera);  
          												}
        											});
												}
												//if ($scope.ShowTabGeneral === true){
												Utils
														.setWindowTitle(
																$scope,
																$scope.TITLE
																		+ ' '
																		+ Model
																				.getEsquema()
																		+ '-'
																		+ Model
																				.getVersion());
												//};

												campoFecha = Model
														.getCampoFecha();
												campoRutCliente = Model
														.getCampoRutCliente();

												$scope.filtroFechasOculto = campoFecha == null;
												$scope.filtroClienteOculto = campoRutCliente == null;
												$scope.esquemaHabilitado = true;
												consultaControlProceso();
												buscarDetalles();
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
		refreshSums = true;
		refrescarTotales();
		obtenerTodos();
	}
	
	$scope.getFiltros = function() {
		return Model.getFiltrosDeTotales(gridData.dataSource.filter());
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
	
	function abrirModal(tab,keyCabecera){
		if(idEntidadReguladora === 'BCC' && Model.getEsquema()=='DJ1'){
			if(listVersiones !== null){
				Utils.openWindow('ModalDetallesDj', {  tab : tab, keyCabecera : keyCabecera, version: listVersiones.value() });
			}
		}
	}
	
	function refrescarTotales() {
		if (!refreshSums)
			return;

		if (!gridSums || !gridSums.dataSource) {
			refreshSums = false;
			return;
		}
		
		var filtros = $scope.getFiltros();
		filtros.x_CodigoValidacion = $scope.CodigoValidacion;
		filtros.p_IdEmpresa = $scope.SesionIdEmpresa;
		filtros.p_IdUsuario = $scope.SesionIdUsuario;
		filtros.p_IdModulo = Model.getModulo();
		filtros.p_IdDocumento = Model.getDocumento().IdDocumento;

		Model.setFiltrosDeTotales(filtros);
		gridSums.dataSource.read();
		refreshSums = false;
	}
	
	function configurarFiltro() {
		var filters = [ {
			field : 'IdDocumento',
			operator : 'eq',
			value : $scope.documento.IdDocumento
		} ];
		if ($scope.CodigoValidacion === null) {
			if ($scope.idSocioNegocioActual
					&& $scope.socioNegocioSeleccionado != null
					&& typeof $scope.socioNegocioSeleccionado.IdSocioDeNegocio !== 'undefined') {
				filters
						.push({
							field : campoRutCliente.Nombre,
							operator : 'startswith',
							value : $scope.socioNegocioSeleccionado.DocumentoIdentidadDefecto
						});
			}
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
		}
		return filters;
	}
	
	function obtenerTodos() {
		$scope.todos = [];
		var currentDataSource = Model.getDataSourceDetalles(null);
		currentDataSource.pageSize(0);
		
		var filters = configurarFiltro();
		currentDataSource.filter(filters);
		
		currentDataSource.query().then(function(){
			var todos = currentDataSource.view().map(function(e) {
				return {ID: e.ID};
			});
			const esquema = listEsquemas?.value()?.match(/^[A-Za-z]+/);
			if(esquema){
				let sicamData = [];
				if(esquema?.[0] === 'REPO'){
					sicamData = currentDataSource.view().map(function(e) {
						return {ID: e.ID, Estado: e.Estado, EstadoSICAM: e.EstadoSICAM};
					});
				}
				$scope.dataSicam.push(...sicamData);
			}
			$scope.todos.push(...todos);
		});
	}
	
	function fitrarVersion(versiones, esquema , listVersiones){
		var versionesFiltradas = versiones.filter(function(v){
					return v.Esquema === esquema;
					});
		listVersiones.setDataSource(versionesFiltradas);
		listVersiones.value(versionesFiltradas.length > 0 ? versionesFiltradas[0].Version : '');
	};

	var datosEsquemas = null;
	$scope.data.switchDefine = false;
	$scope.tieneCertificadoEsquema = 'N';
	$scope.data.tieneCertificado = false;

	// DataSource de los esquemas especificados.
	// Con esto se llenan las dos primeras listbox de la
	// ventana y, luego se fija el primer esquema visible.
	
	//INICIO - ABBI
	//Logica para agregar nuevo filtro para banco central
	//defino el filtro
	//Obtengo el datasource de los esquemas
	var dataSourceEsquema = Model.getDataSourceEsquema();
	//obtengo los filtros actuales para no perderlos
	var current_filters = dataSourceEsquema.filter().filters;
	//creo la variable para el nuevo filtro
	var new_filter = {};
	//reviso si la ventana es la del banco central
	if(idEntidadReguladora === "BCC"){
		new_filter = { field: "IdEntidadReguladora", operator: "eq", value: "BCC" };
		current_filters.push(new_filter);
	}else{
		new_filter = { field: "IdEntidadReguladora", operator: "neq", value: "BCC" };
		current_filters.push(new_filter);
	}
	
	new_filter = { field: "IdEntidadReguladora", operator: "neq", value: "RMO" };
	current_filters.push(new_filter);
	//seteo los filtros de la grilla incluyendo el nuevo
	dataSourceEsquema.filter(current_filters);
	//FIN - ABBI
	
	dataSourceEsquema.fetch().then(function() {
		var versiones = dataSourceEsquema.data();

		if (versiones.length < 1) {
			bcsComunes.msg('error', $scope.TITLE, 'No hay esquemas definidos');
			return;
		}
		//datosModulos = Utils.getUniqueByKey(versiones, 'Modulo');
		datosEsquemas = Utils.getUniqueByKey(versiones, 'Esquema');
		Model.setDatosEsquemas(datosEsquemas);
/*
		$('#modulo').kendoDropDownList({
			dataTextField : 'NombreModulo',
			dataValueField : 'Modulo',
			dataSource : datosModulos,
			autobind : true,
			change: onChangeModulo,
			index : 0
		});
		
		listModulos = $('#modulo').data('kendoDropDownList');
	*/
		$contenedor.find('#esquema').kendoDropDownList({
			dataTextField : 'Esquema',
			dataValueField : 'Esquema',
			dataSource : datosEsquemas,
			autobind : true,
			//cascadeFrom : 'modulo',
			index : 0,
			change: function(e) {
				var esquemaSeleccionado = this.value();
				fitrarVersion(versiones, esquemaSeleccionado, listVersiones);
				var dataItem = this.dataItem(); 
				if(idEntidadReguladora === 'BCC'){
					$scope.chkEscogerDia = false;
					$scope.tieneEnvio = (dataItem.TieneEnvio === 'S');
					var esquema = this.value()?.match(/^[A-Za-z]+/);
					var fechaSistema = $scope.SesionFechaSistema;
					var partes = fechaSistema.split('-');
					var versionSistema = partes[0] + '/' + partes[1];					
					listVersiones.value(versionSistema);   
					$scope.tieneTotales = true;
					$scope.tieneMonedaPrimaria = false
					if (esquema && esquema[0] === 'REPO') {	
						if (this.value()==='REPO2P2'){$scope.tieneTotales = false;};
						if (this.value()==='REPO2P1'){$scope.tieneMonedaPrimaria = true;};
						$scope.textoBotonCuadrar = 'UI_VALIDAR';
						setTimeout(function() {                             
							listVersiones.trigger("change");
							$scope.primerSeteo = false;
						}, 100);				
					} else {
						$scope.textoBotonCuadrar = 'UI_CUADRAR';
						$scope.primerSeteo = false;
					}
				}				
			}
		});

		listEsquemas = $contenedor.find('#esquema').data("kendoDropDownList");

		$contenedor.find('#version').kendoDropDownList({
			dataTextField : 'Version',
			dataValueField : 'Version',
			dataSource : versiones,
			autobind : true,
			//index : 0,
			change: function(e) {
    				var value = this.value();
    				if(idEntidadReguladora === 'BCC'){
						$scope.fnFechaReporte(value);
					}
  				}
		});
		listVersiones = $contenedor.find('#version').data('kendoDropDownList');
		
		var esquemaInicial= listEsquemas.value();
			
		fitrarVersion(versiones, esquemaInicial, listVersiones);
		
		$scope.initFechas();
		$scope.establecerEsquema(null);
		
		$scope.$watch('chkEscogerDia', function(newval,oldval) {
			if(idEntidadReguladora === 'BCC'){
				if (newval !== undefined && listVersiones !== null) {
					var year = listVersiones.value().substring(0,4);
					var month = listVersiones.value().substring(5,7).replace(/^0+/,"") - 1;
					var startDate = new Date(year, month, 1);
					var endDate = new Date(year, month + 1, 0);
					$scope.fechaHastaDeshabilitado = newval;
						if(listEsquemas && listEsquemas.value() === 'REPO3P1'){
	            		return; 
	        		}
					$scope.fechaDesde = newval ? util.dateSPdate(startDate) : util.dateSPdate(startDate);
					$scope.fechaHasta = newval ? util.dateSPdate(startDate) : util.dateSPdate(endDate);
				}
			}
		});
	});

	$scope.initFechas = function() {
		var fechaSistema = $scope.SesionFechaSistema;
		var partes = fechaSistema.split('-');
		var versionSistema = partes[0] + '/' + partes[1];			
		listVersiones.value(versionSistema);
		if(idEntidadReguladora=== 'BCC'){		
			$scope.fnFechaReporte(versionSistema);		
			$scope.primerSeteo = true;
		}else{
			listVersiones.value(versionSistema);
		}     
	};
	
	function onChangeModulo() {
		$scope.ShowTabGeneral=false;
		$scope.ShowBtnModuloAplicar = true;
	};
 

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
		//INICIO - ABBI
		$scope.ShowTabDatosDeclaracion = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowTabDatosDeclarante = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowTabAuditoriaCarga = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowTabAuditoriaCuadratura = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowTabAuditoriaGeneracion = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowTabAuditoriaValidacion = idEntidadReguladora === 'BCC'? true : false;
		
		$scope.ShowBtnCrear = idEntidadReguladora === 'BCC'? false : true;
		$scope.ShowBtnEliminar = idEntidadReguladora === 'BCC'? false : true;
		$scope.ShowBtnCargar = idEntidadReguladora === 'BCC'? false : true;
		$scope.ShowBtnCuadrar = idEntidadReguladora === 'BCC'? false : true;
		$scope.ShowBtnGenerarCertificado = idEntidadReguladora === 'BCC'? false : true;
		$scope.ShowBtnGenerarDeclaracion = idEntidadReguladora === 'BCC'? false : true;
	
		$scope.ShowBtnCrearBC = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowBtnEliminarBC = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowBtnCargarBC = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowBtnCuadrarBC = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowBtnRectificar = idEntidadReguladora === 'BCC'? true : false;
		$scope.ShowBtnGenerarDeclaracionBC = idEntidadReguladora === 'BCC'? true : false;
		
		$scope.ShowBtnExportarExcel = idEntidadReguladora === 'BCC'? true : false;
		
		//FIN - ABBI
		$scope.ShowGridSumsEstandar= true;
		$scope.ShowGridSumsSicam= false;
		
		EsquemaTieneCertificado(esq);
		
		for (i = 0; i < configuracionMantenedor.length; i++) {
			switch (configuracionMantenedor[i].IdObjeto) {
			case 'FormDetailView_TabDatosDeclaracion':
				$scope.ShowTabDatosDeclaracion = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_TabDatosDeclarante':
				$scope.ShowTabDatosDeclarante = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_TabAuditoriaCarga':
				$scope.ShowTabAuditoriaCarga = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_TabAuditoriaCuadratura':
				$scope.ShowTabAuditoriaCuadratura = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_TabAuditoriaGeneracion':
				$scope.ShowTabAuditoriaGeneracion = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_TabAuditoriaValidacion':
				$scope.ShowTabAuditoriaValidacion = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_BtnCrear':
				if(idEntidadReguladora === 'BCC'){
					$scope.ShowBtnCrear = false;
					$scope.ShowBtnCrearBC = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}else{
					$scope.ShowBtnCrearBC = false;
					$scope.ShowBtnCrear = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}
				
				break;
				
			case 'FormDetailView_BtnCargar':
				if(idEntidadReguladora === 'BCC'){
					$scope.ShowBtnCargar = false;
					$scope.ShowBtnCargarBC = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}else{
					$scope.ShowBtnCargarBC = false;
					$scope.ShowBtnCargar = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}
				break;
				
			case 'FormDetailView_BtnCuadrar':
				if(idEntidadReguladora === 'BCC'){
					$scope.ShowBtnCuadrar = false;
					$scope.ShowBtnCuadrarBC = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}else{
					$scope.ShowBtnCuadrarBC = false;
					$scope.ShowBtnCuadrar = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}
				break;
				
			case 'FormDetailView_BtnGenerarCertificado':
				$scope.ShowBtnGenerarCertificado = (configuracionMantenedor[i].ValorActivo == 'S') && !$scope.EsDJ1 ? true: false;
				break;
				
			case 'FormDetailView_BtnGenerarDeclaracion':
				if(idEntidadReguladora === 'BCC'){
					$scope.ShowBtnGenerarDeclaracion = false;
					$scope.ShowBtnGenerarDeclaracionBC = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}else{
					$scope.ShowBtnGenerarDeclaracionBC = false;
					$scope.ShowBtnGenerarDeclaracion = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				}
				break;
				
			case 'FormDetailView_BtnExportarExcel':
				$scope.ShowBtnExportarExcel = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;
				
			case 'FormDetailView_gridSumsEstandar':
				$scope.ShowGridSumsEstandar = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;	
			case 'FormDetailView_gridSumsSicam':
				$scope.ShowGridSumsSicam = (configuracionMantenedor[i].ValorActivo == 'S') ? true: false;
				break;	
			default:
				break;
			}
		}
		return false;
	}
	
}
