angular.module('ModuloContabilidad').controller('InformeClientesRelacionadosCtrl',
	function ($scope, $timeout, $http, bcsComunes, $resource, $translate, bcsServicioContabilidad, bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma) {

		const winID = bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		$scope.winID = winID;
		bcsComunes.ventanaAutoOverlay(winID, false, 3000);

		$scope.data.HabilitaBotones = true;
		$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;

		$scope.filtros = {};
		$scope.filtros.kngFechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);

		$scope.data.generarInforme = function () {
			const validacion = validarRangoFechas($scope.filtros.kngFechaDesde, $scope.filtros.kngFechaHasta)
			if (!validacion.valido) {
				bcsComunes.msg("error", 'Error', $translate.instant(validacion.mensaje));
				return;
			}
			bcsComunes.ventanaOverlay(winID, true);

			const programa = 'INFORME_OPERACIONES_CLIENTES_REL';

			const datos = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'COR',
				p_TipoPrograma: [{
					IdInterno: 0,
					IdTipoPrograma: programa,
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
					Valor: 'CON'
				}, {
					IdInterno: 0,
					NombreParametro: 'FechaInicio',
					ValorBlob: util.dateSPdate($scope.filtros.kngFechaDesde)
				}, {
					IdInterno: 0,
					NombreParametro: 'FechaFin',
					ValorBlob: util.dateSPdate($scope.filtros.kngFechaHasta)
				}]
			}
			const r = $resource("", {}, {
				'AgregarPrograma': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043" }
			});

			r.AgregarPrograma(datos).$promise.then(function (result) {
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
				} else {
					const fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
						conflictos: util.parseServerResponse(result)
					}, winID);
				}
				bcsComunes.ventanaOverlay(winID, true);
			}, function (result) {
				alert(result);
				bcsComunes.ventanaOverlay(winID, true);
			});
		};

		$scope.data.downloadExcelReport = function (ruta, maximoIntentos = 10, intervaloMs = 4000) {
			bcsComunes.ventanaOverlay($scope.winID, true);
			return new Promise((resolve, reject) => {
				let intentosRealizados = 0;

				function newTry() {
					const url = "/BSCL/Plataforma/GWJAVA/PLA/download?";
					const data = { file: ruta };

					$http({
						method: 'POST',
						url: url,
						params: data,
						responseType: "arraybuffer"
					}).then(function (response) {
						const file = new Blob([response.data], { type: 'application/vnd.ms-excel' });
						saveAs(file, "InformeClientesRelacionados");
						bcsComunes.ventanaOverlay($scope.winID, false);
						resolve();
					}).catch(function (error) {
						intentosRealizados++;
						if (intentosRealizados >= maximoIntentos) {
							bcsComunes.msg("error", 'Error', $translate.instant('UI_ERROR_AL_GENERAR_REPORTE'));
							reject(error);
						} else {
							setTimeout(newTry, intervaloMs);
						}
					});
				}
				newTry();
			})
		}

		$scope.abrirMantenedorConceptos = function () {
			bcsComunes.ventanaAbrir("MantenedorConceptosClientesRelacionados", {}, false, false, winID);
		};

		function validarRangoFechas(fechaInicioStr, fechaFinStr) {
			if (!fechaInicioStr || !fechaFinStr) {
				return { valido: false, mensaje: "FECHA_RANGO_CONSULTA_INVALIDA"};
			}
			const fechaInicio = new Date(fechaInicioStr);
			const fechaFin = new Date(fechaFinStr);
		  
			if (fechaInicio > fechaFin) {
			  return { valido: false, mensaje: "UI_FECHA_FIN_MENOR"};
			}
		  
			const anios = fechaFin.getFullYear() - fechaInicio.getFullYear();
			const meses = fechaFin.getMonth() - fechaInicio.getMonth();
			const totalMeses = anios * 12 + meses;
		  
			if (
			  totalMeses > 3 ||
			  (totalMeses === 3 && fechaFin.getDate() > fechaInicio.getDate())
			) {
				return { valido: false, mensaje: "UI_RANGO_FECHA_TRIMESTRAL"};
			}
		  
			return { valido: true };
		  }
		  
	});