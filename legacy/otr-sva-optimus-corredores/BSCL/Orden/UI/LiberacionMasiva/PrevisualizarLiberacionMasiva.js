angular.module('ModuloOrdenes').controller('PrevisualizarLiberacionMasivaCtrl', function ($scope, $translate, bcsComunes, bcsServicioOrden) {

	let idVentana = bcsComunes.ventanaWinId($scope);

	$scope.aceptar = function () {
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.confTransacciones = {
		gridCreated : function (e) {
			$scope.apiTransacciones = e;
		},
		autoBind: true,
		height : 300,
		pageable: true,
		reorderable: true,
		resizable: true,
		selectable : "multiple, row",
		excel: {
			allPages: true
		},
		columns : [
			{ field: "FolioTransaccion",			title: $translate.instant('UI_FOLIO_TRANSACCION') },
			{ field: "Nemotecnico",					title: $translate.instant('UI_INSTRUMENTO') },
			{ field: "IdBolsa",						title: $translate.instant('UI_BOLSA') },
			{ field: "IdClasificacionTipoInstrumento", title: $translate.instant('UI_CLASIFICACION') },
			{ field: "IdTipoInstrumento",			title: $translate.instant('UI_TIPO_INSTRUMENTO') },
			{ field: "AccionOrden",					title: $translate.instant('UI_OPERACION') },
			{ field: "IdMoneda",					title: $translate.instant('UI_MONEDA') },
			{ field: "NombreCondicionDeLiquidacion",title: $translate.instant('UI_CONDICION_LIQUIDACION') },
			{ field: "NombreLibroOrdenes",			title: $translate.instant('UI_LIBRO_ORDENES') },
			{ field: "FechaSistemaCreacion",		title: $translate.instant('UI_FECHA') },
			{ field: "HoraSistemaCreacion",			title: $translate.instant('UI_HORA') }
		],
		dataSource : new bcs.DataSource({
			type : "array",
			definition : {
				data : [],
				schema : {
					model : {
						fields : {
							IdTransaccion					: { type: "number" },
							FolioTransaccion				: { type: "string" },
							Nemotecnico						: { type: "string" },
							IdBolsa							: { type: "string" },
							IdClasificacionTipoInstrumento	: { type: "string" },
							IdTipoInstrumento				: { type: "string" },
							AccionOrden						: { type: "string" },
							IdMoneda						: { type: "string" },
							NombreCondicionDeLiquidacion	: { type: "string" },
							NombreLibroOrdenes				: { type: "string" },
							FechaSistemaCreacion			: { type: "string" },
							HoraSistemaCreacion				: { type: "string" }
						}
					}
				}
			}
		})
	};

	let filtros = $.extend({}, bcsComunes.ventanaParams(idVentana));

	let datos = {
		p_IdEmpresa						: $scope.SesionIdEmpresa,
		p_IdUsuario						: $scope.SesionIdUsuario,
		p_IdModulo						: "ORD",
		p_FechaDesde					: util.dateSPseconddate(filtros.fechaDesde),
		p_FechaHasta					: util.dateSPseconddate(filtros.fechaHasta),
		p_IdLibroOrdenes				: filtros.IdLibroOrdenes > 0 ? filtros.IdLibroOrdenes : undefined,
		p_IdClasificacionTipoInstrumento: filtros.IdClasificacionTipoInstrumento ? filtros.IdClasificacionTipoInstrumento : undefined,
		p_IdTipoInstrumento				: filtros.IdTipoInstrumento ? filtros.IdTipoInstrumento : undefined,
		p_IdInstrumento					: (filtros.instrumento && filtros.instrumento.IdInstrumento > 0) ? filtros.instrumento.IdInstrumento : undefined,
		p_AccionOrden					: (filtros.AccionOperacion && filtros.AccionOperacion.trim() !== "TODOS") ? filtros.AccionOperacion : undefined,
		p_IdBolsa						: filtros.IdBolsa ? filtros.IdBolsa : undefined,
		p_IdMoneda						: filtros.IdMonedaOperacion ? filtros.IdMonedaOperacion : undefined,
		p_IdCondicionDeLiquidacion		: filtros.IdCondicionDeLiquidacion ? filtros.IdCondicionDeLiquidacion : undefined
	};

	bcsComunes.ventanaOverlay(idVentana, true);
	let p = bcsServicioOrden.PrevisualizarLiberacionMasiva(datos).$promise;
	p.then(function (result) {
		bcsComunes.ventanaOverlay(idVentana, false);
		if (result.P_OUT_ERRORESCONTADOR > 0) {
			bcsComunes.ventanaMsg({
				conflictos : util.parseServerResponse(result.p_out_Mensajes)
			}, idVentana);
		}
		else {
			if (result.p_out_Transacciones.length === 0) {
				bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_NO_SE_ENCONTRARON_TRANSACCIONES");
			}
			$scope.apiTransacciones.dataSource.data(result.p_out_Transacciones);
		}
	}, function (result) {
		bcsComunes.ventanaOverlay(idVentana, false);
		bcsComunes.ventanaMsg({
			conflictos : util.parseServerResponse(result.p_out_Mensajes)
		}, idVentana);
	});
});
