angular.module('ModuloNormativa').controller('ModalCirNCG499Ctrl', function ($scope, $translate, bcsComunes, bcsServiciosNormativa) {

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.carteraNegativa = false;
	$scope.IdEmisor = parametros.IdEmisor;
	$scope.AccionBoton = parametros.AccionBoton;
	$scope.data = {};
	$scope.data = parametros;
	$scope.accionBoton = ($scope.data.AccionBoton === 'MODIFICAR');
	$scope.ListaBaseTasas = [];
	$scope.ListaTipoInstrumentos = [];
	$scope.ListaCodigosValorizacion = [];
    $scope.ListaClasificacionRiesgo = [];

	$scope.dataTemplate = {};

	$scope.data.BaseTasa = parseInt($scope.data.BaseTasa);
	$scope.data.CodigoDef = $scope.data.FuentePrecios || 0;
    $scope.data.noInforma = $scope.data.IdEstado === 'DES' ? true : false;
    $scope.data.Duration = $scope.data.Duration || 0;
	console.log($scope.data.IdEstado);

	if ($scope.data.IdEstado === 'NEG') {
		carteraNegativa = true;
		console.log(carteraNegativa);
		console.log($scope.data.IdEstado);
	}

	ModeloHomologacion = function () {
		return {
			model: {
				fields: {
					Esquema: {
						type: "string"
					},
					Tipo: {
						type: "string"
					},
					Descripcion: {
						type: "string"
					},
					ValorOrigen: {
						type: "string"
					},
					ValorDestino: {
						type: "string"
					}
				}
			}
		};
	};

	dataSourceHomologacion = function (TipoHomologa) {
		return new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				serverPaging: true,
				serverSorting: true,
				pageSize: 200,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010106",
						dataType: "json"
					}
				},
				filter: [
					{field: "Tipo",operator: "eq",	value: TipoHomologa	}
				],
				schema: ModeloHomologacion()
			}
		});
	};

	$scope.ListaTipoInstrumentosDS = dataSourceHomologacion("INSTRUMENTOCMF");
	$scope.ListaBaseTasasDS = dataSourceHomologacion("BASETASA");
	$scope.ListaCodigosValorizacionDS = dataSourceHomologacion("CODVALORIZ");
	$scope.ListaClasificacionRiesgoDS = dataSourceHomologacion("CLASIFICACIONRIESGO");


	$scope.cargaTiposInstrumento = function(){
		$scope.ListaTipoInstrumentosDS.fetch(function () {
			var data = this.data();
			$scope.$apply(function () {
				$scope.ListaTipoInstrumentos = data;
			});
		});
	};

	$scope.cargaBaseTasas = function(){
		$scope.ListaBaseTasasDS.fetch(function () {
			var data = this.data();
			$scope.$apply(function () {
				$scope.ListaBaseTasas = data;
			});
		});
	};

	$scope.cargaCodigosValorizacion = function(){
		$scope.ListaCodigosValorizacionDS.fetch(function () {
			var data = this.data();
			$scope.$apply(function () {
				$scope.ListaCodigosValorizacion = data;
			});
		});
	};

    $scope.cargaClasificacionRiesgo = function(){
		$scope.ListaClasificacionRiesgoDS.fetch(function () {
			var data = this.data();
			$scope.$apply(function () {
				$scope.ListaClasificacionRiesgo = data;
                var descripcionesVistas = {};
                $scope.ListaClasificacionRiesgo = $scope.ListaClasificacionRiesgo.filter(function(item) { // Implementar funcion para quitar duplicados
                    if (!descripcionesVistas[item.Descripcion]) {
                        descripcionesVistas[item.Descripcion] = true;
                        return true;
                    }
                    return false;
                });
			});
		});
	};

	$scope.homologaciones = [];	
	$scope.homologacionesDataSource = dataSourceHomologacion("PAISCONTRACMF");

	$scope.cargarHomologaciones = function () {
		$scope.homologacionesDataSource.fetch(function () {
			var data = this.data();
			$scope.$apply(function () {
				$scope.homologaciones = data;
				$scope.data.DescripcionPaisSeleccionado = $scope.getCountryDescription($scope.data.CodigoPais);
				$scope.data.DescripcionPaisEmisionSeleccionado = $scope.getCountryDescription($scope.data.CodigoPaisEmisionInstrumento);
			});
		});
	};

	$scope.getCountryDescription = function (countryCode) {
		var country = $scope.homologaciones.find(function (item) {
			return item.ValorOrigen === countryCode;
		});
		return country ? country.Descripcion : '';
	};

	$scope.seleccionarPais = function (descripcionSeleccionada) {
		var pais = $scope.homologaciones.find(function (item) {
			return item.Descripcion === descripcionSeleccionada;
		});

		if (pais) {
			$scope.data.CodigoPais = pais.ValorOrigen;
		}
	};

	$scope.seleccionarPaisEmision = function (descripcionSeleccionada) {
		var pais = $scope.homologaciones.find(function (item) {
			return item.Descripcion === descripcionSeleccionada;
		});

		if (pais) {
			$scope.data.CodigoPaisEmisionInstrumento = pais.ValorOrigen;
		}
	};

	$scope.cargarHomologaciones();
	$scope.cargaTiposInstrumento();
	$scope.cargaBaseTasas();
	$scope.cargaCodigosValorizacion();
    $scope.cargaClasificacionRiesgo();

	$scope.formatearTir = function () {
		if ($scope.data.Tir !== undefined && $scope.data.Tir !== null) {
			var valorFormateado = $scope.data.Tir.toString().replace(/,/g, '');
			$scope.data.Tir = parseFloat(valorFormateado);
		}
	};


	$scope.formatearTir();

	$scope.formatearCantidadUnidades = function () {
		if ($scope.data.CantidadUnidades !== undefined && $scope.data.CantidadUnidades !== null) {
			var valorFormateado = $scope.data.CantidadUnidades.toString().replace(/,/g, '');
			$scope.data.CantidadUnidades = parseFloat(valorFormateado);
		}
	};

	$scope.formatearValorizacionCierre = function () {
		if ($scope.data.ValorizacionCierre !== undefined && $scope.data.ValorizacionCierre !== null) {
			var valorFormateado = $scope.data.ValorizacionCierre.toString().replace(/,/g, '');
			$scope.data.ValorizacionCierre = parseFloat(valorFormateado);
		}
	};
	$scope.formatearDuration = function () {
		if ($scope.data.Duration !== undefined && $scope.data.Duration !== null) {
			var valorFormateado = $scope.data.Duration.toString().replace(/,/g, '');
			$scope.data.Duration = parseFloat(valorFormateado);
		}
	};

	$scope.formatearDuration();
	$scope.formatearCantidadUnidades();
	$scope.formatearValorizacionCierre();


	function buscarExisteTipoInstrumento(dato) {
		var ret = $scope.ListaTipoInstrumentos.find(function (item) {
			return item.ValorDestino === dato;
		});
		return ret ? true : false;
	}

	function existeCodigoValorizacion(dato) {
		var ret = $scope.ListaCodigosValorizacion.find(function (item) {
			return item.ValorDestino === dato;
		});
		return ret ? true : false;
	}

	function esNumeroValido(numero) {
		const numeroConPunto = numero.replace(',', '.');
		const formatoValido = /^-?\d*\.?\d*$/.test(numeroConPunto);
		if (!formatoValido) return false;
		const parsedNumber = parseFloat(numeroConPunto);
		return !isNaN(parsedNumber) && isFinite(parsedNumber);
	}

	$scope.guardarCambios = function () {

		if ($scope.data.DescripcionPaisSeleccionado && $scope.data.DescripcionPaisSeleccionado.trim() !== '') {
			$scope.data.CodigoPaisNuevo = $scope.data.DescripcionPaisSeleccionado;
		} else {
			$scope.data.CodigoPaisNuevo = $scope.data.CodigoPais;
		}

		if ($scope.data.DescripcionPaisEmisionSeleccionado && $scope.data.DescripcionPaisEmisionSeleccionado.trim() !== '') {
			$scope.data.CodigoPaisEmisionInstrumentoNuevo = $scope.data.DescripcionPaisEmisionSeleccionado;
		} else {
			// Aquí, siempre asignaremos un valor predeterminado si CodigoPaisEmisionInstrumento es null o vacío
			$scope.data.CodigoPaisEmisionInstrumentoNuevo = $scope.data.CodigoPaisEmisionInstrumento || "CL";
		}
		var baseTasaPermitidos = [0, 30, 360, 365];
		if (baseTasaPermitidos.indexOf(Number($scope.data.BaseTasa)) === -1) {
			bcsComunes.msg("error", "error", 'Base Tasa debe ser 0, 30, 360 o 365. no puede esta vacio.');
			return;
		}

		if (($scope.data.ISIN || $scope.data.ISIN.trim() !== '') && $scope.data.ISIN.length > 12) {
			bcsComunes.msg("error", "error", 'ISIN no puede superar los 12 caracteres.');
			return;
		}

		if (!$scope.data.CodigoPais || $scope.data.CodigoPais.trim() === '') {
			bcsComunes.msg("error", "error", 'Código de País no puede estar vacío.');
			return;
		}

		if ($scope.data.FuentePrecios.length > 100) {
			bcsComunes.msg("error", "error", 'FuentePrecios el limite de largo permitido (100 caracteres).');
			return;
		}

		if (isNaN($scope.data.Duration) || $scope.data.Duration === null || $scope.data.Duration === '') {
			bcsComunes.msg("error", "error", 'Duración debe ser un número válido.');
			return;
		}		

		if (!$scope.data.IdTipoInstrumento || $scope.data.IdTipoInstrumento.trim() === '') {
			bcsComunes.msg("error", "error", 'Tipo Instrumento no puede estar vacía.');
			return;
		}
		const ExisteTipoInstrumento = buscarExisteTipoInstrumento($scope.data.IdTipoInstrumento);

		if (!ExisteTipoInstrumento) {
			bcsComunes.msg("error", "error", 'El Tipo de Instrumento debe ser una opcion valida de la lista y no debe esta vacio.');
			return;
		}

		if ($scope.data.IdTipoInstrumento.length > 10) {
			bcsComunes.msg("error", "error", 'Tipo Instrumento sobrepasa el limite de largo permitido (10 caracteres).');
			return;
		}
	
		if (!existeCodigoValorizacion($scope.data.CodigoValorizacion)) {
			bcsComunes.msg("error", "error", 'El Codigo de Valorizacion debe ser una opcion valida de la lista y no debe esta vacio.');
			return;
		}

		if (!$scope.data.CodigoValorizacion || $scope.data.CodigoValorizacion.trim() === '') {
			bcsComunes.msg("error", "error", 'El codigo de valorizacion no puede estar vacío.');
			return;
		}

		var regex = /^-?\d{1,15}(\.\d{1,4})?$/;

		if (!regex.test($scope.data.ValorizacionCierre)) {
			bcsComunes.msg("error", "error", 'Valorizacion sobrepasa el limite de largo permitido (15 Enteros y 4 decimales, positivo o negativo).');
			return;
		}

		if (!regex.test($scope.data.CantidadUnidades)) {
			bcsComunes.msg("error", "error", 'Cantidad Unidades sobrepasa el limite de largo permitido (15 Enteros y 4 decimales).');
			return;
		}

		if (!regex.test($scope.data.Tir.replace(",","."))) {
			bcsComunes.msg("error", "error", 'Tir sobrepasa el limite de largo permitido (15 Enteros y 4 decimales).');
			return;
		}

		var NCG499;
		$scope.AccionBoton = 'MODIFICAR';
		NCG499 = [{			
			Fecha: $scope.data.Fecha,
			IdClasificacionTipoInstrumento: $scope.data.IdClasificacionTipoInstrumentoModificado,
			Nemotecnico: $scope.data.Nemotecnico,			
			ISIN: $scope.data.ISIN,
			SituacionInstrumento: $scope.data.SituacionInstrumento,
			IdEmisor: $scope.data.IdEmisor,
			CodigoPais: $scope.data.CodigoPaisNuevo,			
			IdTipoInstrumentoCMF: $scope.data.IdTipoInstrumento,
			FechaVencimiento: $scope.data.FechaVencimiento,
			ClasificacionRiesgoInstrumento: $scope.data.ClasificacionRiesgoInstrumento || '',
			TendenciaDeRiesgo: $scope.data.TendenciaClasificacionriesgo,
			CantidadUnidades: $scope.data.CantidadUnidades.replace(",","."),
			Tir: $scope.data.Tir.replace(",","."),
			CodigoValorizacion : $scope.data.CodigoValorizacion,
			BaseTasa: $scope.data.BaseTasa,
			MontoMercadoCustodia :$scope.data.ValorizacionCierre, 
			CodigoPaisEmisionInstrumento: $scope.data.CodigoPaisEmisionInstrumentoNuevo,
			FuentePrecios: $scope.data.FuentePrecios || '',
			Duration: $scope.data.Duration,
			IdOrigen: $scope.data.IdOrigen,
			IdEstado: $scope.data.noInforma ? 'DES' : 'ACT',
			SituacionInstrumentoAux: $scope.data.SituacionInstrumentoAux,
			IdInstrumento: $scope.data.IdInstrumento

		}];

		var datos = {
			NCG499_in: NCG499,
			Accion: $scope.AccionBoton,
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario
		};

		bcsComunes.ventanaOverlay(idVentana, true);
		bcsServiciosNormativa.ModificarNCG499(datos).$promise.then(function(result) {

			if (result.P_OUT_ERRORESCONTADOR == "0" && (result.P_OUT_ADVERTENCIASCONTADOR == "0" || result.P_OUT_ADVERTENCIASCONTADOR === undefined)) {
				
				var mensajeTotal = new Array();
				$.each(result.P_OUT_MENSAJES,function(key,value){
					 var objMensaje = {
							Id : value.id,
							tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
							codigo : value.codigo,
							descripcion : $translate.instant(value.descripcion)
					 };
					 mensajeTotal.push(objMensaje);
				});
				result.P_OUT_MENSAJES = mensajeTotal;			
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_EXITOSO"),conflictos:util.parseServerResponse(result)},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			}
			else if (result.P_OUT_ERRORESCONTADOR > 0) {

				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					conflictos : util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
			else if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_PROCESO_ERROR"),
					conflictos : util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
			
		},
		function(result) {
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaMsg({
				mensaje : $translate.instant("UI_ERROR_MODIFICACION"),
				conflictos : util.parseServerResponse(result)
			},idVentana);
		});
	};

	$scope.preventNegativeInput = function(event) {
		if (event.key === '-' || event.keyCode === 189) {
			event.preventDefault();  // Evita que se escriba el signo "-"
		}
	};

	$scope.cancelar = function () {
		bcsComunes.ventanaCerrar(idVentana);
	};



});