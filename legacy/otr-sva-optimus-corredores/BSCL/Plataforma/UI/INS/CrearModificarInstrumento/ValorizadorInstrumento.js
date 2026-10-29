angular.module('ModuloPlataforma').controller(
		'ValorizadorInstrumentoCtrl',
		[ '$http', '$resource', '$scope', '$translate', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'bcsServicioPlataforma',
				ValorizadorInstrumentoCtrl ]);

function ValorizadorInstrumentoCtrl($http, $resource, $scope, $translate,
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
		$scope.params.FechaValorizacion = new Date();
		$scope.params.ValorNominal = 1000000;
		$scope.params.Precio = 100;
	}

	$scope.limpiar();

	$scope.valorizar = function() {
		$scope.params.Tasa360 = null;
		$scope.params.MontoCapital = null;
		$scope.params.MontoIntereses = null;
		$scope.params.MontoReajuste = null;
		$scope.params.MontoTotal = null;
		$scope.params.Duration = null;
		$scope.params.Convexidad = null;

		var fechaValorizacion = $scope.params.FechaValorizacion.getFullYear()
				+ "-" + ($scope.params.FechaValorizacion.getMonth() + 1) + "-"
				+ $scope.params.FechaValorizacion.getDate();
		var params = {
			nemo : $scope.params.instrumento.Nemotecnico,
			fechaValorizacion : fechaValorizacion,
			valorNominal : $scope.params.ValorNominal,
			precio : $scope.params.Precio,
			tasa : $scope.params.Precio === null ? $scope.params.Tasa : null
		}

		llamarValorizador(params, function(data) {
			if (data.codigoRetorno && data.codigoRetorno !== 0) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				var mensaje = data.mensaje ? data.mensaje : "Error al valorizar";
				bcsComunes.msg("error", "UI_ERROR", mensaje);
			} else {
				$scope.params.Precio = data.precio;
				$scope.params.Tasa = data.tasa;
				$scope.params.Tasa360 = data.tasa360 ? data.tasa360 : null;
				$scope.params.Moneda = data.moneda;
				$scope.params.MonedaPago = data.monedaPago ? data.monedaPago : null;
				$scope.params.MontoCapital = data.montoCapital;
				$scope.params.MontoIntereses = data.montoIntereses;
				$scope.params.MontoReajuste = data.montoReajuste;
				$scope.params.MontoTotal = data.montoTotal;
				$scope.params.MontoTotalPago = data.montoTotalPago ? data.montoTotalPago : null;
				$scope.params.Duration = data.duration;
				$scope.params.Convexidad = data.convexidad;
			}
		}, function(result) {
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			bcsComunes.msg("error", "UI_ERROR",
					"UI_ERROR_AL_VALORIZAR_INSTRUMENTO");
		});
	}

	function llamarValorizador(params, fnSuccess, fnError) {
		var url = "/BSCL/Plataforma/GWSERVICES/valo2/valorizar";
		$http({
			method : 'GET',
			url : url,
			params : params
		}).success(fnSuccess).error(fnError);
	}

}
