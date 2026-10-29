angular.module('ModuloOrdenes').controller('LiberacionMasivaCtrl', function ($scope, $translate, bcsComunes, bcsServicioOrden, bcsAlerta, OPCIONES_BCSALERTA) {
	$scope.winID = bcsComunes.ventanaWinId($scope);
	let idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoHeight($scope.winID);

	$scope.datos = {};
	$scope.filtros = {};

	$scope.filtros.clasificacionTipoInstrumento = {
		logic : "or",
		RV : { field : "IdClasificacionTipoInstrumento", operator : "eq", value : "RV"},
		DV : { field : "IdClasificacionTipoInstrumento", operator : "eq", value : "DV"},
	};
	$scope.filtros.tipoInstrumento = {
		IdClasificacionTipoInstrumento : { field : "IdClasificacionTipoInstrumento ", operator : "eq", value : ""},
		EsOrden : { field : "EsOrden", operator : "eq", value : "S"},
	};
	$scope.filtros.operacion = {
		logic : "or",
		C : { field : "AccionOrden", operator : "eq", value : "COMPRA"},
		V : { field : "AccionOrden", operator : "eq", value : "VENTA"},
	};

	$scope.datos.fechaDesde = util.date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema).toDate();
	$scope.datos.fechaHasta = util.dateTerminoDia(util.date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema).toDate());

	let armarDatos = function () {
		return {
			p_IdEmpresa						: $scope.SesionIdEmpresa,
			p_IdUsuario						: $scope.SesionIdUsuario,
			p_IdModulo						: "ORD",
			p_FechaDesde					: util.dateSPseconddate($scope.datos.fechaDesde),
			p_FechaHasta					: util.dateSPseconddate($scope.datos.fechaHasta),
			p_IdLibroOrdenes				: $scope.datos.IdLibroOrdenes > 0 ? $scope.datos.IdLibroOrdenes : undefined,
			p_IdClasificacionTipoInstrumento: $scope.datos.IdClasificacionTipoInstrumento ? $scope.datos.IdClasificacionTipoInstrumento : undefined,
			p_IdTipoInstrumento				: $scope.datos.IdTipoInstrumento ? $scope.datos.IdTipoInstrumento : undefined,
			p_IdInstrumento					: ($scope.datos.instrumento && $scope.datos.instrumento.IdInstrumento > 0) ? $scope.datos.instrumento.IdInstrumento : undefined,
			p_AccionOrden					: ($scope.datos.AccionOperacion && $scope.datos.AccionOperacion.trim() !== "TODOS") ? $scope.datos.AccionOperacion : undefined,
			p_IdBolsa						: $scope.datos.IdBolsa ? $scope.datos.IdBolsa : undefined,
			p_IdMoneda						: $scope.datos.IdMonedaOperacion ? $scope.datos.IdMonedaOperacion : undefined,
			p_IdCondicionDeLiquidacion		: $scope.datos.IdCondicionDeLiquidacion ? $scope.datos.IdCondicionDeLiquidacion : undefined
		};
	};

	$scope.verElementos = function () {
		bcsComunes.ventanaAbrir("PrevisualizarLiberacionMasiva", $scope.datos);
	};

	$scope.aceptar = function () {
		let datos = armarDatos();
		let t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_ESTA_SEGURO_DE_EJECUTAR_LIBERACION"), OPCIONES_BCSALERTA.SI_NO);
		t.result.then(function (result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				bcsComunes.ventanaOverlay(idVentana, true);
				let p = bcsServicioOrden.EjecutarLiberacionMasiva(datos).$promise;
				p.then(function (result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					if (result.p_out_Mensajes.length === 0) {
						bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_NO_SE_ENCONTRARON_TRANSACCIONES");
					} else {
						bcsComunes.ventanaAbrir(
							"VentanaResultado", {
							titulo: $translate.instant("UI_RESULTADO_LIBERACION"),
							campoSecuencia: "id",
							nombreSecuencia: "UI_SECUENCIA_TRANSACCION",
							datos: result.p_out_Mensajes,
							usaNuevosMensajes: true
						},
							false,
							false,
							$scope.idVentana
						);
					}
				}, function (result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaMsg({ conflictos: util.parseServerResponse(result.p_out_Mensajes) }, idVentana);
				});
			}
		});
	};
});
