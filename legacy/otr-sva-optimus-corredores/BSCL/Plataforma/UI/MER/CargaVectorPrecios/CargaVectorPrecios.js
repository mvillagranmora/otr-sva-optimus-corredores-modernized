angular.module('ModuloPlataforma').controller('CargaVectorPreciosCtrl', function ($scope, $translate, $resource, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma, $timeout) {
	var formData = this;

	//var conf = bcsConfiguracionFFM.getDataSourceValorCuotaArray([]); 
	//$scope.defaultOptions = bcsConfiguracionFFM.getConsultaValorCuota([]);

	var ventanaId = bcsComunes.ventanaWinId($scope);

	$scope.habilitaBotonProcesar = false;
	formData.HabilitoCarga = false;
	formData.datosCarga = [];
	formData.Codigo = false;
	$scope.configData = {};
	$scope.filtros = {};
	$scope.recordData = {};
	$scope.ultimaCarga = "";
	formData.seleccionar = function (filtros) {
		if (filtros == "BLM") { formData.cargarArchivo_bloomberg(); }
		else if (filtros == "NAV") { formData.cargarArchivo_nav(); }
	};

	function existeFecha(fecha) {
		var fechaf = fecha.split("-");
		var day = fechaf[0];
		var month = fechaf[1];
		var year = fechaf[2];
		var date = new Date(year, month, '0');
		if ((day - 0) > (date.getDate() - 0)) {
			return false;
		}
		return true;
	}

	$scope.configResultados = {
		dataSource: new bcs.DataSource({
			type: "array", //Los tipos posibles son: odata, call, array y object
			definition: {
				serverFiltering: false, // Filtrado de datos en el servidor.
				serverPaging: false, // Paginado de los datos en el servidor.
				serverSorting: false, // Ordenamiento de los datos en el servidor.
				pageSize: 200, // Número de registros por página.
				schema: {
					model: {
						fields: {
							FECHA: { type: "string" },
							INSTRUMENTO: { type: "string" },
							ENTIDAD: { type: "string" },
							VALOR: { type: "number" },
							TIPOREGISTRO: { type: "string" },
							ESTADOVALIDACION: { type: "string" },
							MENSAJEVALIDACION: { type: "string" },
						}
					}
				},
			}
		}),
		columns: [
			{ field: "FECHA", title: "UI_FECHA_INGRESO", format: "{0:n5}", width: "150px" },
			{ field: "INSTRUMENTO", title: "UI_NEMOTECNICO", format: "{0:n5}", width: "150px" },
			{ field: "ENTIDAD", title: "UI_ENTIDAD", format: "{0:n5}", width: "150px" },
			{ field: "VALOR", title: "UI_VALOR", format: "{0:n5}", width: "150px" },
			{ field: "TIPOREGISTRO", title: "UI_TIPO_REGISTRO", format: "{0:n5}", width: "150px" },
			{ field: "ESTADOVALIDACION", title: "UI_ESTADO_VALIDACION", format: "{0:n5}", width: "150px" },
			{ field: "MENSAJEVALIDACION", title: "UI_MENSAJE_VALIDACION", format: "{0:n5}", width: "150px" },
		],
		gridCreated: function (grid) {
			$scope.mainGrid = grid;
		},
		pageable: true,
		filterable: true,
		groupable: true,
		autoBind: true,
		height: 300,
		userSettings: true,
		navigatable: true,
		excel: {
			allPages: true,
		},
		selectable: "row"
	};
	$scope.buscar = function () {
		var objfilters = {};
		var niv1filters = new Array();
		objfilters.logic = 'and';
		objfilters.filters = niv1filters;

		if ($scope.filtros.fechaDesde != undefined) {
			if (!!$scope.filtros.fechaHasta) {
				var resp = util.validarRangoFecha($scope.filtros.fechaDesde, $scope.filtros.fechaHasta, 366);
				if (resp.estado) {
					bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
					return;
				}
			}

			niv1filters.push({ field: "FECHA", operator: "gte", value: util.dateSPdate($scope.filtros.fechaDesde) });


		}
		if ($scope.filtros.fechaHasta != undefined) {
			niv1filters.push({ field: "FECHA", operator: "lte", value: util.dateSPdate(util.dateTerminoDia($scope.filtros.fechaHasta)) });
		}

		if (!!$scope.filtros.instrumento) {
			niv1filters.push({ field: "INSTRUMENTO", operator: "eq", value: $scope.filtros.instrumento.Nemotecnico });
		}

		$scope.mainGrid.dataSource.filter(objfilters);
	}

	formData.cargarArchivo_bloomberg = function (P_Simular = 'S') {
		var data = {};
		if (P_Simular === 'N') {

			data.fileData = $scope.configResultados.dataSource.data();
			formData.RequestConfig(data, "BLOOMBERG", P_Simular);
		}
		else {

			bcsComunes.ventanaOverlay($scope.winID, true);
			var datosArchivo = { codigoArchivo: 'CARGA_VECTOR_PRECIOS_PLA', formatoArchivoDefecto: '.txt' };
			var ventanaIdCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true, true);
			var promesa = bcsComunes.ventanaPromesa(ventanaIdCargaArchivo);
			promesa.then(function (respuesta) {
				formData.RequestConfig(respuesta, "BLOOMBERG", P_Simular);
			});

		}
	}

	formData.cargarArchivo_nav = function (P_Simular = 'S') {
		var data = {};
		if (P_Simular === 'N') {

			data.fileData = $scope.configResultados.dataSource.data();
			formData.RequestConfig(data, "NAV", P_Simular);
		}
		else {

			bcsComunes.ventanaOverlay($scope.winID, true);
			var datosArchivo = { codigoArchivo: 'CARGA_VECTOR_PRECIOS_PLA', formatoArchivoDefecto: '.txt' };
			var ventanaIdCargaArchivo = bcsComunes.ventanaAbrir("CargaArchivosGeneral", datosArchivo, true, true);
			var promesa = bcsComunes.ventanaPromesa(ventanaIdCargaArchivo);
			promesa.then(function (respuesta) {
				
				formData.RequestConfig(respuesta, "NAV", P_Simular);
			});

		}


	}

	formData.RequestConfig = function (data, cargaActual, P_Simular) {
		if (!data || !data.fileData) {
			bcsComunes.ventanaOverlay($scope.winID, false);
			return false;
		} else {
			bcsComunes.ventanaOverlay($scope.ventanaId, true);

			for (var i = 0; i < data.fileData.length; i++) {
				data.fileData[i].ENTIDAD = cargaActual;
				data.fileData[i].TIPOREGISTRO = cargaActual + ' PRECIO';
			}

			var ERRORES = 0;
			var BUENOS = 0;
			var INS = "";
			var FECH = "";
			var VAL = "";
			var SACOCOMA = "";
			for (var i = 0; i < data.fileData.length; i++) {

				INS = data.fileData[i].INSTRUMENTO;
				data.fileData[i].FECHA = P_Simular == 'S' ? data.fileData[i].FECHA : data.fileData[i].FECHA.split('-').reverse().join('-');// Al volver la data desde Java, formatea la fecha de otra forma, con esto se invierte para que quede igual como se fue la primera vez
				FECH = data.fileData[i].FECHA;
				VAL = data.fileData[i].VALOR;
				MMS = data.fileData[i].MENSAJEVALIDACION;
				ENT = data.fileData[i].ENTIDAD;
				TREG = data.fileData[i].TIPOREGISTRO;
				EV = data.fileData[i].ESTADOVALIDACION;

				if (MMS == "OK" || (P_Simular == 'N' && (EV == 'OK' || EV == 'ADV'))) {
					SACOCOMA = VAL.replace(",", "")

					VAL = SACOCOMA.trim();

					data.fileData[i].VALOR = VAL;

					var guiones = [];

					for (var j = 0; j < FECH.length; j++) {
						if (FECH[j] === "-") guiones.push(j);
					}


					if (INS.length == 0 ||
						INS == "" ||
						FECH.length == 0 ||
						VAL.length == 0 ||
						MMS == "Falta un campo en la fila" ||
						FECH.indexOf("-") < 0 ||
						guiones.length < 2 || guiones.length > 3 ||
						ENT != cargaActual ||
						TREG != cargaActual + " PRECIO") {
						ERRORES++;
					}
					else { BUENOS++; }
				}//fin if
				else { ERRORES = 2; }
			}

			var parametrosSP = {};
			parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario = $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdModulo = 'PLA';
			parametrosSP.p_CodigoArchivo = 'CARGA_VECTOR_PRECIOS_PLA';
			parametrosSP.p_Archivo = JSON.stringify(data.fileData);
			parametrosSP.P_Simular = P_Simular;

			if (ERRORES >= 1) {
				var p = bcsComunes.ventanaMsgErr($translate.instant('El Archivo posee un formato no valido. La carga de datos no será realizada'), ventanaId);
				bcsComunes.ventanaPromesa(p).then(function () {
					bcsComunes.ventanaOverlay(ventanaId, false);
				});

			}
			else {


				//var sp = bcsServicioPlataforma.Servicios().CargaEntidadVectorPrecio_MER(parametrosSP);

				bcsComunes.ventanaOverlay(ventanaId, true);
				bcsServicioPlataforma.Servicios().CargaEntidadVectorPrecio_MER(parametrosSP).$promise.then(function (respuesta) {
					console.log(respuesta);
					if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
						//var id = bcsComunes.ventanaMsg({mensaje:"Operacion realizada"},ventanaId);

						bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_OPERACION_EXITOSA"), conflictos: util.parseServerResponse(respuesta) }, ventanaId);
						bcsComunes.ventanaOverlay(ventanaId, false);
						$scope.ultimaCarga = cargaActual;

						$scope.habilitaBotonProcesar = P_Simular === 'S' ? true : false;
					} else {
						bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_ERROR_PROCESAR"), conflictos: util.parseServerResponse(respuesta) }, ventanaId);
						bcsComunes.ventanaOverlay(ventanaId, false);
					}

					$scope.configResultados.dataSource.data(respuesta.p_Resultado);

				}, function (respuesta) {
					bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_ERROR_PROCESAR"), conflictos: util.parseServerResponse(respuesta) }, ventanaId);
					bcsComunes.ventanaOverlay(ventanaId, false);
				});


			}//fin else validacion	
			$scope.buscar();
		}//FIN ELSE
	}

	formData.procesarVectores = function () {
		if ($scope.ultimaCarga === "NAV") {
			formData.cargarArchivo_nav('N');
		}
		if ($scope.ultimaCarga === "BLOOMBERG") {
			formData.cargarArchivo_bloomberg('N');
		}
		$scope.habilitaBotonProcesar = false;

	}
	$scope.$watch('formData.Codigo', function () {
		if (formData.Codigo !== undefined && formData.Codigo !== false) {
			formData.HabilitoCarga = true;
		} else {
			formData.HabilitoCarga = false;
		}
	});
});