angular.module('ModuloPlataforma').controller(
		'ValorizadorPagareCtrl',
		[ '$http', '$resource', '$scope', '$translate', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'bcsServicioPlataforma',
				ValorizadorPagareCtrl ]);

function ValorizadorPagareCtrl($http, $resource, $scope, $translate,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, bcsServicioPlataforma) {

	$scope.IdVentana = bcsComunes.ventanaWinId($scope);

	$scope.tiposDeBonos = null;
	bcsServicioPlataforma.TiposDeBonos(function(arr) {
		$scope.tiposDeBonos = arr;
	});

	$scope.config = {
		escalaConvexidad : 2,
		escalaDuration : 2,
		escalaIntereses : 2,
		escalaMontos : 2,
		escalaPrecios : 4,
		escalaTasas : 6
	};
	var dataSourceConfig = bcsServicioPlataforma.DataSourceConfig();
	dataSourceConfig.query().then(function(e) {
		var view = dataSourceConfig.view();
		for (var i = 0; i < view.length; i++) {
			if (view[i].Tipo === 'NUMERO') {
				$scope.config[view[i].Nombre] = 1 * view[i].Valor;
			}
		}
	});

	$scope.limpiar = function() {
		$scope.params = {};
		$scope.params.TipoInteres = 0;
		$scope.params.DiasAno = 360;
	}

	$scope.limpiar();

	$scope.valorizar = function() {
		if ($scope.params.FechaVencimiento == null
				&& $scope.params.DiasVencimiento == null) {
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			bcsComunes.msg("error", "UI_ERROR",
					"Debe ingresar fecha de vencimiento o dias al vencimiento");
			return;
		}

		if ($scope.params.Tasa != null) {
			if ($scope.params.ValorCaptacion == null
					&& $scope.params.ValorRescate == null) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				bcsComunes.msg("error", "UI_ERROR",
						"Debe ingresar valor captacion o valor rescate");
				return;
			}
		} else if ($scope.params.ValorCaptacion != null) {
			if ($scope.params.Tasa == null
					&& $scope.params.ValorRescate == null) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				bcsComunes.msg("error", "UI_ERROR",
						"Debe ingresar tasa o valor rescate");
				return;
			}
		} else if ($scope.params.ValorRescate != null) {
			if ($scope.params.ValorCaptacion == null
					&& $scope.params.Tasa == null) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				bcsComunes.msg("error", "UI_ERROR",
						"Debe ingresar valor captacion o tasa");
				return;
			}
		} else {
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			bcsComunes
					.msg("error", "UI_ERROR",
							"Debe ingresar 2 valores de entre: valor captacion, valor rescate o tasa");
			return;
		}

		var fechaCaptacion = $scope.params.FechaCaptacion.getFullYear() + "-"
				+ ($scope.params.FechaCaptacion.getMonth() + 1) + "-"
				+ $scope.params.FechaCaptacion.getDate();
		var fechaVencimiento = null;
		if ($scope.params.FechaVencimiento != null) {
			fechaVencimiento = util.dateSPdate($scope.params.FechaVencimiento);
		}

		var params = {
			nemo : $scope.params.instrumento.Nemotecnico,
			fechaCaptacion : fechaCaptacion,
			fechaVencimiento : fechaVencimiento,
			valorCaptacion : $scope.params.ValorCaptacion,
			valorRescate : $scope.params.ValorRescate,
			diasAno : $scope.params.DiasAno,
			diasVencimiento : $scope.params.DiasVencimiento,
			tasa : $scope.params.Tasa,
			tipoInteres : $scope.params.TipoInteres,
			moneda : $scope.params.instrumento.IdTipoReajuste,
			monedaPago : $scope.params.instrumento.IdMoneda
		}

		llamarValorizador(
				params,
				function(data) {
					if (data.codigoRetorno && data.codigoRetorno !== 0) {
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						var mensaje = data.mensaje ? data.mensaje
								: "Error al valorizar";
						bcsComunes.msg("error", "UI_ERROR", mensaje);
					} else {
						$scope.params.DiasVencimiento = data.diasVencimiento;
						$scope.params.FechaVencimiento = util.dateDS(data.fechaVencimiento,'Fecha','CALL');
						$scope.params.Tasa = data.tasa;
						$scope.params.ValorCaptacion = data.valorCaptacion;
						$scope.params.ValorRescate = data.valorRescate;
						$scope.params.ValorCaptacionPago = data.valorCaptacionPago ? data.valorCaptacionPago
								: null;
						$scope.params.ValorRescatePago = data.valorRescatePago ? data.valorRescatePago
								: null;
					}
				}, function(result) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					bcsComunes.msg("error", "UI_ERROR",
							"UI_ERROR_AL_VALORIZAR_INSTRUMENTO");
				});
	}

	function llamarValorizador(params, fnSuccess, fnError) {
		var url = "/BSCL/Plataforma/GWJAVA/SERVLETS/valo2/valorizarPagare";
		$http({
			method : 'GET',
			url : url,
			params : params
		}).success(fnSuccess).error(fnError);
	}

}
