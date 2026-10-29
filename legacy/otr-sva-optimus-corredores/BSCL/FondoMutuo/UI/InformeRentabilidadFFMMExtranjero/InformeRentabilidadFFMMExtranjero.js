angular.module('ModuloFFM').controller('InformeRentabilidadFFMMExtranjeroCtrl',
	function ($scope, bcsComunes, $translate, $resource, $http, bcsServiciosNormativa, bcsServicioFondosMutuos, bcsServicioInterfaces) {

		$scope.data = {};
		$scope.config = {};

		$scope.config.agnoComercial = {
			max: new Date(),
			start: "decade",
			depth: "decade",
			format: "yyyy"
		};

		$scope.data.oSocioNegocio = {
			tipo: "combobox-socio-negocio",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdEmpresa: { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
				EstadoCliente: { field: "EstadoCliente", operator: "eq", value: 'ACT' }
			},
		};

		$scope.data.oPortafolio = {
			tipo: "combobox-portafolio",
			configuracion: {},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdCliente: { "field": "IdCliente", "operator": "eq", "value": 0 }
			},
		};

		$scope.generarInformeRentabilidadFFMMExtranjeroMasivo = function generarInformeRentabilidadFFMMExtranjeroMasivo() {
			bcsComunes.ventanaOverlay($scope.winID, true);
			if($scope.data.agnoComercial && $scope.data.agnoComercial.getFullYear().toString() !=''){
				const datos = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdModulo: 'FFM',
					p_TipoPrograma: [{
						IdInterno: 0,
						IdTipoPrograma: "GenerarInformeRentabilidadFFMMExtranjero",
						SegundosDesfase: 0
					}],
					p_Parametro: [{
						IdInterno: 0,
						NombreParametro: 'IdEmpresa',
						Valor: $scope.SesionIdEmpresa
					}, {
						IdInterno: 0,
						NombreParametro: 'IdUsuario',
						Valor: $scope.SesionIdUsuario
					}, {
						IdInterno: 0,
						NombreParametro: 'IdModulo',
						Valor: 'FMM'
					}, {
						IdInterno: 0,
						NombreParametro: 'AgnoComercial',
						Valor: $scope.data.agnoComercial.getFullYear().toString()
					}, {
						IdInterno: 0,
						NombreParametro: 'EsMasivo',
						Valor: "S"
					}, {
						IdInterno: 0,
						NombreParametro: 'IdCliente',
						Valor: 0
	
					}]
				}
				bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
					function (result) {
						bcsComunes.ventanaOverlay($scope.winID, false);
						var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
							conflictos: util.parseServerResponse(result)
						}, $scope.winID);
	
					},
					function (result) {
						bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				});
			}else {
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.msg("error", "Error", $translate.instant('UI_GENERAR_REPORTE_MASIVO_ERROR'));
			}
			
		};

		$scope.generarInformeRentabilidadFFMMExtranjeroUnitario = function generarInformeRentabilidadFFMMExtranjeroUnitario() {
			bcsComunes.ventanaOverlay($scope.winID, true);
			
			if($scope.data.agnoComercial && $scope.data.agnoComercial.getFullYear().toString() !='' && $scope.data.oSocioNegocio.seleccionado.IdCliente && $scope.data.oSocioNegocio.seleccionado.IdCliente > 0){
				const datos = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdModulo: 'FFM',
					p_TipoPrograma: [{
						IdInterno: 0,
						IdTipoPrograma: "GenerarInformeRentabilidadFFMMExtranjero",
						SegundosDesfase: 0
					}],
					p_Parametro: [{
						IdInterno: 0,
						NombreParametro: 'IdEmpresa',
						Valor: $scope.SesionIdEmpresa
					}, {
						IdInterno: 0,
						NombreParametro: 'IdUsuario',
						Valor: $scope.SesionIdUsuario
					}, {
						IdInterno: 0,
						NombreParametro: 'IdModulo',
						Valor: 'FMM'
					}, {
						IdInterno: 0,
						NombreParametro: 'AgnoComercial',
						Valor: $scope.data.agnoComercial.getFullYear().toString()
					}, {
						IdInterno: 0,
						NombreParametro: 'EsMasivo',
						Valor: "N"
					}, {
						IdInterno: 0,
						NombreParametro: 'IdCliente',
						Valor: $scope.data.oSocioNegocio.seleccionado.IdCliente
	
					}]
				}
				bcsServicioInterfaces.InscribirProcesoBatch(datos).$promise.then(
					function (result) {
						bcsComunes.ventanaOverlay($scope.winID, false);
						var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
						bcsComunes.ventanaMsg({
							mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
							conflictos: util.parseServerResponse(result)
						}, $scope.winID);
	
					},
					function (result) {
						bcsComunes.ventanaOverlay($scope.winID, false);
						bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				});
			}else{
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.msg("error", "Error", $translate.instant('UI_GENERAR_REPORTE_UNITARIO_ERROR'));
			}
		};
	});
