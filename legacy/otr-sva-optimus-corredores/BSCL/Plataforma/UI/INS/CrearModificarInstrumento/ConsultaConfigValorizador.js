(function() {
	angular
			.module('ModuloPlataforma')
			.controller(
					'ConsultaConfigValorizadorCtrl',
					function($scope, $translate, bcsAlerta, bcsComunes,
							bcsServicioPlataforma) {

						$scope.data = {};

						var dataSourceConfig = bcsServicioPlataforma
								.DataSourceConfig();

						$scope.configuracionGrillaConfig = {
							dataSource : dataSourceConfig,
							columns : [ {
								field : 'Descripcion',
								title : 'Descripcion',
								width : '300px'
							}, {
								field : 'Tipo',
								title : 'Tipo',
								width : '50px'
							}, {
								field : 'Valor',
								title : 'Valor'
							} ],
							allowCopy : true,
							autoBind : true,
							userSettings : true,
							navigatable : true,
							excel : {
								allPages : true
							},
							filterable : true,
							height : 250,
							pageable : true,
							reorderable : true,
							resizable : true,
							scrollable : true,
							selectable : "row",
							sortable : true
						};

						$scope.buscarConfigs = function() {
							var filters = new Array();
							dataSourceConfig.filter(filters);
						}

						$scope.mostrarModificarConfig = function() {
							if ($scope.configSeleccionada === null
									|| typeof $scope.configSeleccionada === 'undefined') {

								showWarning(
										'Debe seleccionar una fila de la grilla',
										'Configuración de Valorizador',
										bcsComunes.ventanaWinId($scope));
								return;
							}

							bcsComunes
									.ventanaAbrir(
											'MantenedorConfigValorizador',
											{
												callBackFunction : $scope.buscarConfigs,
												configSeleccionada : $scope.configSeleccionada
											}, true, false, bcsComunes
													.ventanaWinId($scope));
						}

						function showWarning(msg, message, winid) {
							bcsComunes.ventanaMsg({
								mensaje : message,
								conflictos : {
									advertencias : [ {
										Codigo : 'COD',
										Descripcion : msg
									} ],
									autorizaciones : [],
									errores : [],
									erroresFatales : [],
									informaciones : []
								}
							}, winid);
						}

					});
}());
