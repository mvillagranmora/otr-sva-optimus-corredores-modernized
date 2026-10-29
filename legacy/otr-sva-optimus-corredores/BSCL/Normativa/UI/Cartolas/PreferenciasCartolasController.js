angular.module('ModuloNormativa').controller(
		'PreferenciasCartolasController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils', 'bcsCartolas',
				PreferenciasCartolasController ]);

function PreferenciasCartolasController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, bcsCartolas) {

	$scope.data = {}
	$scope.VigenteAl = util.dateInicioDia($scope.SesionFechaSistema);

	var dataSourcePlantillas = bcsCartolas
			.getDataSourcePlantillas($scope.SesionIdEmpresa);

	var dataSourceRecursos = bcsCartolas.getDataSourceRecursos();
	$scope.configuracionGrillaRecursos = bcsCartolas
			.getConfiguracionGrillaRecursos(dataSourceRecursos);

	var dataSourceConfig = bcsCartolas
			.getDataSourceConfig($scope.SesionIdEmpresa);
	$scope.configuracionGrillaConfig = bcsCartolas
			.getConfiguracionGrillaConfig(dataSourceConfig);

	$scope.buscarConfigs = function() {
		var filters = new Array();

		filters.push({
			field : 'IdEmpresa',
			operator : 'eq',
			value : $scope.SesionIdEmpresa
		});

		dataSourceConfig.filter(filters);
	}

	$scope.mostrarModificarConfig = function() {
		if ($scope.data.ConfigSeleccionada === null
				|| typeof $scope.data.ConfigSeleccionada === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Configuración de Cartolas', bcsComunes
							.ventanaWinId($scope));
			return;
		}

		bcsComunes.ventanaAbrir('ModificarConfig', {
			CallBackFunction : $scope.buscarConfigs,
			ConfigSeleccionada : $scope.data.ConfigSeleccionada
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.mostrarModificarRecurso = function() {
		if ($scope.data.RecursoSeleccionado === null
				|| typeof $scope.data.RecursoSeleccionado === 'undefined') {
			Utils.showWarning('Debe seleccionar una fila de la grilla',
					'Modificación de Recurso', bcsComunes.ventanaWinId($scope));
			return;
		}

		bcsComunes.ventanaAbrir('ModificarRecurso', {
			CallBackFunction : $scope.buscarRecursos,
			RecursoSeleccionado : $scope.data.RecursoSeleccionado
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.mostrarModificarVigencia = function() {
		if ($scope.data.PlantillaSeleccionada === null
				|| typeof $scope.data.PlantillaSeleccionada === 'undefined') {
			Utils
					.showError('Debe buscar plantilla',
							'Modificación de Vigencia', bcsComunes
									.ventanaWinId($scope));
			return;
		}

		bcsComunes.ventanaAbrir('ModificarVigencia', {
			PlantillaSeleccionada : $scope.data.PlantillaSeleccionada
		}, true, false, bcsComunes.ventanaWinId($scope));
	}

	$scope.buscarPlantilla = function() {
		var errors = [];

		if (typeof $scope.data.Plantilla.Nombre !== 'string') {
			errors.push('Debe seleccionar tipo plantilla');
		}

		if (typeof $scope.VigenteAl !== 'object') {
			errors.push('Debe seleccionar la fecha de vigencia');
		}

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Preferencias de Cartolas', bcsComunes
					.ventanaWinId($scope));
			return;
		}

		var filters = new Array();

		filters.push({
			field : 'IdEmpresa',
			operator : 'eq',
			value : $scope.SesionIdEmpresa
		});

		filters.push({
			field : 'Nombre',
			operator : 'eq',
			value : $scope.data.Plantilla.Nombre
		});

		filters.push({
			field : 'VigenteDesde',
			operator : 'lte',
			value : $scope.VigenteAl
		});

		dataSourcePlantillas.query({
			sort : {
				field : "VigenteDesde",
				dir : "desc"
			},
			filter : filters
		}).then(
				function(e) {
					var data = dataSourcePlantillas.data();
					if (data.length < 1) {
						Utils.showError('No hay plantillas configuradas',
								'Consulta de Plantillas', bcsComunes
										.ventanaWinId($scope));
						return;
					}

					$scope.data.PlantillaSeleccionada = data[0];
					$scope.buscarRecursos();
				});
	}

	$scope.buscarRecursos = function() {
		var filters = new Array();

		filters.push({
			field : 'IdPlantilla',
			operator : 'eq',
			value : $scope.data.PlantillaSeleccionada.IdPlantilla
		});

		dataSourceRecursos.filter(filters);
	}
	
	$scope.nuevaPlantilla = function() {
		var plantilla = {};
		plantilla.IdEmpresa = $scope.SesionIdEmpresa;
		plantilla.IdUsuario = $scope.SesionIdUsuario;
		plantilla.IdPlantilla = $scope.data.PlantillaSeleccionada.IdPlantilla;
		plantilla.Nombre = $scope.data.PlantillaSeleccionada.Nombre;
		plantilla.Vigente = 'NO';
		plantilla.VigenteDesde = util.dateInicioDia($scope.SesionFechaSistema);

		bcsCartolas.copiarPlantilla(plantilla, function(reply) {
			var id = reply.P_OUT_ID;
		}, function(error) {
			Utils.showError(error, 'Creación de Plantilla', bcsComunes
					.ventanaWinId($scope));
		});
	}
}