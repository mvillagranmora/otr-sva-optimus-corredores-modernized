angular.module('ModuloMesa').controller('SelectorClientesCtrl',
	function ($scope, $translate, bcsComunes, $resource, bcsServiciosMesa, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosGrillasMesaRF, $timeout, $translate) {

		$scope.IdVentana = bcsComunes.ventanaWinId($scope);
		var cantidadOp = bcsComunes.ventanaParams($scope.IdVentana);
		var gridClientes;
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
		$scope.data.inputCantidad = true;

		//------------------------------------------------
		// CONFIGURACION DE GRILLA
		//------------------------------------------------

		// GRILLA DE CLIENTES:
		$scope.confBasicaClientesMesaRF = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering: true, // Filtrado de datos en el servidor.
					serverPaging: true, // Paginado de los datos en el servidor.
					serverSorting: true, // Ordenamiento de los datos en el servidor.
					pageSize : 200,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1205",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								NombreCompleto: { type: "string" },
								DocumentoIdentidad: { type: "string" },
								IdPortafolio: { type: "number" },
								Proposito: { type: "string"}
							}
						}
					}
				},
			}),
			columns: [
				{ field: "NombreCompleto", title: "UI_NOMBRE", width: "25%" }
				, { field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", width: "25%" }
				, { field: "IdPortafolio", title: "UI_PORTAFOLIO", width: "25%" }
				, {field: "Proposito",title: $translate.instant('UI_PROPOSITO'), width: "25%", align: "left"},

			],
			sortable: true,
			pageable: {
				numeric: true,
				pageSize: 10,
				refresh: true
			},
			height: 263,
			selectable: "row",
			userSettings: true,
			gridCreated: function (g) {
				gridClientes = g;
				gridClientes.refresh();
			}
		};


		// --------------------------------------------------
		// FUNCIONES 
		// --------------------------------------------------


		$scope.aceptar = function () {

			var seleccionado = $scope.seleccionado;
			var cantidadTotal = parseInt(cantidadOp.cantidad);
			var corteMinimo = parseInt(cantidadOp.corteMinimo);
			var cantidad = $scope.cantidadNominales;
			cantidad = parseInt(cantidad);

			if(corteMinimo <= 0) {
				corteMinimo = 1;
			}

			if ($scope.data.opcionesFiltro.opParcial == true) {

				if (cantidad == 0 || isNaN(cantidad)) {
					bcsComunes.msg("error", $translate.instant(""), 'Debe ingresar un valor numerico mayor a 0');
				} else if (cantidad > cantidadTotal) {
					bcsComunes.msg("error", $translate.instant(""), 'La cantidad ingresada es mayor a la cantidad total');
				} else if (cantidad < corteMinimo) {
					bcsComunes.msg("error", $translate.instant(""), 'La cantidad ingresada es menor que el corte minimo permitido por el intrumento. El corte minimo es de: ' + (util.bcs_formato(corteMinimo, 'GEN','MONTO','CLP','','')).valor );
				} else if (cantidad % corteMinimo !== 0) {
					bcsComunes.msg("error", $translate.instant(""), 'La cantidad ingresada no es multiplo del corte minimo permitido por el intrumento. El corte minimo es de: ' + (util.bcs_formato(corteMinimo, 'GEN','MONTO','CLP','','')).valor );
				} else if (cantidad <= cantidadTotal) {

					if (cantidad == cantidadTotal) {
						bcsComunes.msg("warning", $translate.instant(""), 'La cantidad ingresada es igual a la cantidad total');
					}

					if (seleccionado == undefined) {
						bcsComunes.msg("error", $translate.instant(""), 'Debe seleccionar un cliente');
					} else {
						var parcial = true;
						var datos = [seleccionado, cantidad, parcial]
						bcsComunes.ventanaData(idVentana, datos);
						bcsComunes.ventanaCerrar(idVentana);
					}
				}

			} else {

				if (seleccionado == undefined) {
					bcsComunes.msg("error", $translate.instant(""), 'Debe seleccionar un cliente');
				} else {
					var datos = [seleccionado, cantidadTotal]
					bcsComunes.ventanaData(idVentana, datos);
					bcsComunes.ventanaCerrar(idVentana);
				}
			}
		};


		$scope.cancelar = function () {
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.data.opcionesFiltro = { opTotal: true, opParcial: false };
		$scope.$watch("data.opcionesFiltro.opTotal", function (nval, oval) {
			if (nval == true) {
				$scope.data.opcionesFiltro.opParcial = false;
				$scope.data.inputCantidad = true;
			} else if (nval == false) {
				$scope.data.opcionesFiltro.opParcial = true;
				$scope.data.inputCantidad = false;
			}
		});
		$scope.$watch("data.opcionesFiltro.opParcial", function (nval, oval) {
			if (nval == true) {
				$scope.data.opcionesFiltro.opTotal = false;
				$scope.data.inputCantidad = false;
			} else if (nval == false) {
				$scope.data.opcionesFiltro.opTotal = true;
				$scope.data.inputCantidad = true;
			}
		});

		$scope.filtrosGrupoCliente = {
			IdEstado: { "field": "IdEstado", "operator": "eq", "value": 'ACT' }
		}

		$scope.buscarClientesMesaRF = function () {

			var filters = [];
			filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });

			if ($scope.numeroDocumentoIdentidad) {
				filters.push({ field: "tolower(DocumentoIdentidad)", operator: "contains", value: $scope.numeroDocumentoIdentidad.toLowerCase() });
			}
			if ($scope.nombreCompleto) {
				filters.push({ field: "tolower(NombreCompleto)", operator: "contains", value: $scope.nombreCompleto.toLowerCase() });
			}

			$scope.confBasicaClientesMesaRF.dataSource.filter(filters);
		};

	});
