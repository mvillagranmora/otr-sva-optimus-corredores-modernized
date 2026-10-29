angular.module('ModuloPlataforma').controller(
		'MantenedorInstrumentoValorizadorCtrl',
		[ '$http', '$resource', '$scope', '$translate', 'bcsComunes',
				'bcsServicioPlataforma', 'bcsAlerta', 'OPCIONES_BCSALERTA',
				MantenedorInstrumentoValorizadorCtrl ]);

function MantenedorInstrumentoValorizadorCtrl($http, $resource, $scope,
		$translate, bcsComunes, bcsServicioPlataforma, bcsAlerta,
		OPCIONES_BCSALERTA) {

	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.bono = parametros.bono;
	$scope.params = {}
	$scope.params.Nemo = parametros.instrumento.Nemotecnico;

	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.IdVentana);
	}

	$scope.valorizar = function() {
		var fechaValorizacion = $scope.bono.FechaValorizacion.getFullYear()
				+ "-" + ($scope.bono.FechaValorizacion.getMonth() + 1) + "-"
				+ $scope.bono.FechaValorizacion.getDate();
		var params = {
			nemo : $scope.params.Nemo,
			fechaValorizacion : fechaValorizacion,
			valorNominal : $scope.bono.ValorNominal,
			precio : $scope.bono.PrecioValorizacion
		}

		llamarValorizador(params, function(data) {
			if (data.codigoRetorno && data.codigoRetorno !== 0) {
				bcsComunes.ventanaOverlay($scope.IdVentana, false);
				bcsComunes.msg("error", "UI_ERROR", "Error al valorizar");
			} else {
				$scope.bono.Precio = data.precio;
				$scope.bono.Tasa = data.tasa;
				$scope.bono.ValorBolsa = data.montoTotal;
				$scope.bono.DurationBolsa = data.duration;
			}
		}, function(result) {
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
			bcsComunes.msg("error", "UI_ERROR",
					"UI_ERROR_AL_VALORIZAR_INSTRUMENTO");
		});
	}

	$scope.guardar = function() {
		var datos = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'PLA',
			p_IdInstrumento : $scope.bono.IdInstrumento,
			p_FechaDevengo : $scope.bono.FechaDevengo || null,
			p_AjusteFinDeMes : $scope.bono.AjusteFinDeMes ? "S" : "N",
			p_CallConfirmado : $scope.bono.CallConfirmado ? "S" : "N",
			p_ConvencionDias : $scope.bono.ConvencionDias,
			p_CuponeraVariable : $scope.bono.CuponeraVariable ? "S" : "N",
			p_TasaEmision : $scope.bono.TasaEmision,
			p_TenorInflacion : $scope.bono.TenorInflacion || null,
			p_TipoLibor : $scope.bono.TipoLibor || null,
			p_TipoCalculoInteresesCupon : $scope.bono.TipoCalculoInteresesCupon,
			p_TipoCalculoInteresesDevengados : $scope.bono.TipoCalculoInteresesDevengados,
			p_IdMonedaPago : $scope.bono.IdMonedaPago || null,
			p_FechaPrepago : $scope.bono.FechaPrepago || null,
			p_CUSIP : $scope.bono.CUSIP || null,
			p_COMMON : $scope.bono.COMMON || null,
			p_SEDOL : $scope.bono.SEDOL || null,
			p_TickerBloomberg : $scope.bono.TickerBloomberg || null,
			p_MercadoLocal : $scope.bono.MercadoLocal ? "S" : "N",
			p_MercadoExtrabursatil : $scope.bono.MercadoExtrabursatil ? "S"
					: "N",
			p_MercadoExtranjero : $scope.bono.MercadoExtranjero ? "S" : "N",
			p_PresenciaBursatil : $scope.bono.PresenciaBursatil ? "S" : "N",
			p_IdPaisTransaccion : $scope.bono.IdPaisTransaccion || null,
			p_ValorBolsa : $scope.bono.ValorBolsa,
			p_ValorBloomberg : $scope.bono.ValorBloomberg,
			p_DurationBolsa : $scope.bono.DurationBolsa,
			p_DurationBloomberg : $scope.bono.DurationBloomberg,
			p_FechaValorizacion : $scope.bono.FechaValorizacion || null,
			p_ValorNominal : $scope.bono.ValorNominal,
			p_PrecioValorizacion : $scope.bono.PrecioValorizacion
		};
		bcsComunes.ventanaOverlay($scope.IdVentana, true);
		bcsServicioPlataforma.Servicios().GuardarBono(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						var p = bcsComunes.ventanaMsg({
							mensaje : $translate.instant("UI_BONO_GUARDADO"),
							conflictos : util.parseServerResponse(result)
						}, $scope.IdVentana);
						bcsComunes.ventanaPromesa(p).then(function() {
							bcsComunes.ventanaData($scope.IdVentana, {
								guardar : true
							});
							bcsComunes.ventanaCerrar($scope.IdVentana);
						});
					} else {
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						bcsComunes.ventanaMsg({
							mensaje : "UI_ERROR_GUARDAR_BONO",
							conflictos : util.parseServerResponse(result)
						}, $scope.IdVentana);
					}
				}, function(result) {
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					bcsComunes
							.msg("error", "UI_ERROR", "UI_ERROR_GUARDAR_BONO");
				});
	}

	function llamarValorizador(params, fnSuccess, fnError) {
		var url = "/BSCL/Plataforma/GWJAVA/SERVLETS/valo2/valorizar";
		$http({
			method : 'GET',
			url : url,
			params : params
		}).success(fnSuccess).error(fnError);
	}

}
