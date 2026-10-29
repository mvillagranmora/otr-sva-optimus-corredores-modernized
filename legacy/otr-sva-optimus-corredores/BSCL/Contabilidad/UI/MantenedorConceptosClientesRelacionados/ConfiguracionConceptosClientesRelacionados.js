angular.module('ModuloContabilidad').controller('ConfiguracionConceptosClientesRelacionadosCtrl',
	function ($rootScope, $scope, $resource, $http, bcsComunes, $translate, bcsAlerta, $timeout, OPCIONES_BCSALERTA) {
		$scope.data = {}
		$scope.inputs = {};
		$scope.data.DatosConceptosNormativosSeleccionado = undefined;

		const filtersValoresFiltros = {}
		let arrCondiciones = [];
		let condicionesBases = [];
		const idVentana = bcsComunes.ventanaWinId($scope);
		const parametrosVentana = bcsComunes.ventanaParams(idVentana);
		const conceptoNormativo = parametrosVentana.ConceptoNormativoSeleccionado

		bcsComunes.ventanaTitulo(idVentana, $translate.instant("UI_CONFIGURACION_CONCEPTO_TITULO", { Concepto: conceptoNormativo.ConceptoNormativo }))

		const dsTipoDatoEventos = new bcs.DataSource({
			type: "array",
			definition: {
				schema: {
					model: {
						fields: {
							IdTipoDatoEvento: { type: "string" },
							TipoDato: { type: "string" },
							DatosPreDefinidos: { type: "string" }
						}
					}
				},
			}
		});

		$scope.data.ConfiguracionGrillaTipoDatosEventos = {
			dataSource: dsTipoDatoEventos,
			columns: [
				{ field: "IdTipoDatoEvento", title: 'UI_TIPO_DATO', hidden: true, width: "100px", headerAttributes: { style: "text-align: center;" } },
				{ field: "TipoDato", title: 'UI_DATO_CONFIGURAR', width: "100px", headerAttributes: { style: "text-align: center;" } },
			],
			filterable: false,
			autoBind: true,
			height: 250,
			selectable: "single, row",
			pageable: true
		};

		const dsCondicionesConceptoNormativo = new bcs.DataSource({
			type: "array",
			definition: {
				schema: {
					model: {
						fields: {
							IdCondicionConceptoNormativo: { type: "number" },
							ValorVarchar: { type: "string" },
							OperadorFuncion: { type: "string", editable: true },
							IdTipoDatoEvento: { type: "string" },
							Descripcion: { type: "string" },
						}
					}
				},
			}
		});

		$scope.data.condConceptoNormativo = {
			dataSource: dsCondicionesConceptoNormativo,
			columns: [
				{ field: "IdCondicionConceptoNormativo", hidden: true },
				{ field: "IdTipoDatoEvento", title: 'UI_TIPO_DATO', width: "95px" },
				{ field: "Descripcion", title: 'UI_DESCRIPCION' },
				{ field: "ValorVarchar", title: 'UI_VALOR', width: "40px", headerAttributes: { style: "text-align: center;" } },
				{ field: "OperadorFuncion", title: 'UI_OPERADOR', editable: true, width: "70px", headerAttributes: { style: "text-align: center;", } },
			],
			filterable: false,
			autoBind: true,
			height: 250,
			pageable: true,
			selectable: "single, row",
			notEditableColumns : function(fila)
			{
				if (fila.IdTipoDatoEvento !== "TipoDocumento")
				{
					fila.OperadorFuncion = '';
					return ['OperadorFuncion'];
				}
			}
		}

		$scope.data.condConceptoNormativo.save = function (e) {
			let a = Object.keys(e.values)[0];
			if (!(['+', '-', ''].includes(e.values[a]))) {
				bcsComunes.msg("error", "UI_ERROR", "UI_VALOR_NO_ESPERADO");
				e.preventDefault();
			}
		}

		function success_fnCombo (respuesta) {
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				$scope.inputs.comboboxEventos = Object
					.keys(respuesta.P_OUT_TABFILTROSVALOR)
					.map(key => respuesta.P_OUT_TABFILTROSVALOR[key]);

				let seleccionado = $scope.inputs.comboboxEventos[0];
				if ($scope.data.condicionSeleccionada) {
					seleccionado = $scope.inputs.comboboxEventos.find(v => v.Valor === $scope.data.condicionSeleccionada.ValorVarchar)
				}
				$scope.inputs.valorCombo = seleccionado;
				$scope.inputs.inputDisabled = true;
				$scope.inputs.comboDisabled = false;
				$scope.inputs.valorDato = "";
			}
			else
				bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
		};

		function success_fnConfigs (respuesta) {
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				arrCondiciones = respuesta.configuracionConcepto.map(function (c) {
					return {
						IdConceptoNormativo: c.IdConceptoNormativo,
						IdCondicionConceptoNormativo: c.IdCondicionConceptoNormativo,
						OperadorFuncion: c.OperadorFuncion,
						IdTipoDatoEvento: c.IdTipoDatoEvento,
						ValorVarchar: c.ValorVarchar,
						Descripcion: c.Descripcion
					}
				})
				$scope.data.condConceptoNormativo.dataSource.data(arrCondiciones);
				condicionesBases = JSON.parse(JSON.stringify(arrCondiciones));
			}
			else
				bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo));
		};

		function success_fnConfiguracion (respuesta) {
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
				bcsComunes.msg('info', $translate.instant("UI_INFORMACION"), $translate.instant("UI_CONCEPTO_CONFIGURADO_EXITO"));
			}
			else {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("UI_CONCEPTO_CONFIGURADO_ERROR"));
		
			}
		};

		function success_fnTipoDato ({ data }) {
			const final = data.d.results.map(dato => ({
				IdTipoDatoEvento: dato.IdTipoDatoEvento,
				TipoDato: dato.IdTipoDatoEvento.replace(/([a-z])([A-Z])/g, "$1 $2"),
				DatosPreDefinidos: dato.DatosPreDefinidos
			})).sort();
			$scope.data.ConfiguracionGrillaTipoDatosEventos.dataSource.data(final);
		};

		$scope.cargarFiltros = function () {
			$scope.data.condicionSeleccionada = null;
			if (!$scope.data.DatosConceptosNormativosSeleccionado) return;
			datosPredefinidos = $scope.data.DatosConceptosNormativosSeleccionado.DatosPreDefinidos;
			filtersValoresFiltros.p_IdTipoDatoEvento = $scope.data.DatosConceptosNormativosSeleccionado.IdTipoDatoEvento;
			if (datosPredefinidos === "N") return;
			$scope.call("12006", filtersValoresFiltros, success_fnCombo);
		}

		$scope.call = function (callTransaction, parameters, success) {
			const urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + callTransaction;
			const r = $resource("", {}, {
				'Filtros': { method: "POST", url: urlFiltros, async: false, }
			});
			const post = r.Filtros(parameters);
			post.$promise.then(success, function (result) {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.msg("error", "Error", $translate.instant('UI_ERROR'));
			});
		};

		$scope.callOData = function (callTransaction, success) {
			const urlFiltros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + callTransaction;

			const params = {
				$filter: "(EsConceptoNormativo eq 'S' and Estado eq 'V')",
				$format: "json",
				$inlinecount: "allpages"
			}

			$http({
				method: 'POST',
				url: urlFiltros,
				params: params,
				responseType: "application/json"
			}).then(success);
		};

		$scope.cancelar = function () {
			bcsComunes.ventanaCerrar(bcsComunes.ventanaWinId($scope));
		}

		$scope.guardarCambios = function () {
			if (!parametrosVentana && !parametrosVentana.ConceptoNormativoSeleccionado) {
				bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('CONCEPTO_NO_SELECCIONADO'))
				return
			}
			bcsComunes.ventanaOverlay(idVentana, true);
			const params = {};
			params.IdEmpresa = $scope.SesionIdEmpresa;
			params.condiciones = $scope.data.condConceptoNormativo.dataSource.data().map(function (c) {
				return {
					IdCondicion: c.IdCondicionConceptoNormativo,
					IdTipoDatoEvento: c.IdTipoDatoEvento,
					ValorVarchar: c.ValorVarchar,
					OperadorFuncion: c.OperadorFuncion
				};
			});
			params.IdConceptoNormativo = conceptoNormativo.IdConceptoNormativo;

			$scope.call("15486541", params, success_fnConfiguracion, )
		};

		$scope.agregarCondicion = function () {
			bcsComunes.ventanaOverlay(idVentana, true);
		
			const nuevoValor = $scope.inputs.valorCombo.Valor;
			const Descripcion = $scope.inputs.valorCombo.Descripcion;
			const nuevoOperador = $scope.inputs.OperadorFuncion;
		
			const index = arrCondiciones.findIndex(c => c.ValorVarchar === nuevoValor);
		
			if (index !== -1) {
				if (arrCondiciones[index].OperadorFuncion !== nuevoOperador) {
					arrCondiciones[index].OperadorFuncion = nuevoOperador;
					arrCondiciones[index].Descripcion = Descripcion;
					arrCondiciones[index].IdTipoDatoEvento = $scope.data.DatosConceptosNormativosSeleccionado.IdTipoDatoEvento;
				} else {
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('CONDICION_EXISTE'));
					return;
				}
			} else {
				arrCondiciones = [
					...$scope.data.condConceptoNormativo.dataSource.data(),
					{
						IdCondicionConceptoNormativo: null,
						ValorVarchar: nuevoValor,
						OperadorFuncion: nuevoOperador,
						Descripcion: Descripcion,
						IdTipoDatoEvento: $scope.data.DatosConceptosNormativosSeleccionado.IdTipoDatoEvento
					}
				];
			}
		
			$scope.data.condConceptoNormativo.dataSource.data(arrCondiciones);
			bcsComunes.ventanaOverlay(idVentana, false);
		};

		$scope.quitarCondicion = function () {
			bcsComunes.ventanaOverlay(idVentana, true);
			const valorAEliminar = $scope.data.condicionSeleccionada.ValorVarchar;
			const indice = arrCondiciones.findIndex(c => c.ValorVarchar === valorAEliminar);
			if (indice === -1) {
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.msg("warning", $translate.instant("UI_ADVERTENCIA"), $translate.instant('CONDICION_NO_EXISTE'));
				return;
			}
			arrCondiciones = [...$scope.data.condConceptoNormativo.dataSource.data()]
			arrCondiciones.splice(indice, 1);
			$scope.data.condConceptoNormativo.dataSource.data(arrCondiciones);
			bcsComunes.ventanaOverlay(idVentana, false);
		}

		$scope.btnGuardarActivo = function () {
			const mapCondiciones = data => data.map(c => ({
				IdCondicionConceptoNormativo: c.IdCondicionConceptoNormativo,
				IdTipoDatoEvento: c.IdTipoDatoEvento,
				ValorVarchar: c.ValorVarchar,
				OperadorFuncion: c.OperadorFuncion,
			}));
		
			const condicionesActuales = mapCondiciones($scope.data.condConceptoNormativo.dataSource.data());
			const condicionesIniciales = mapCondiciones(condicionesBases);
		
			return JSON.stringify(condicionesActuales) !== JSON.stringify(condicionesIniciales);
		};

		$scope.call("15486545", {
			p_IdConceptoNormativo: conceptoNormativo.IdConceptoNormativo,
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdEmpresa,
			p_IdModulo: "CON",
		}, success_fnConfigs)

		$scope.callOData("4231", success_fnTipoDato)
	});