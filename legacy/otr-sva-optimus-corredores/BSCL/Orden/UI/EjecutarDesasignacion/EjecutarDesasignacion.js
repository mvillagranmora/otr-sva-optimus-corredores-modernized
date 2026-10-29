angular.module('ModuloOrdenes').controller('EjecutarDesasignacionCtrl', function ($scope, $translate, bcsComunes, bcsServicioOrden, bcsAlerta, OPCIONES_BCSALERTA) {
	$scope.winID = bcsComunes.ventanaWinId($scope);
	let idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoHeight($scope.winID);

	$scope.datos = {};
	$scope.filtros = {}
	$scope.inputsHabilitados = true;
	$scope.data	= {};

	let valoresIniciales = {
		IdTipoCanalIngreso: "AGE",
		IdLibroOrdenes: $scope.datos.IdLibroOrdenes,
		IdClasificacionTipoInstrumento: "RV",
		IdTipoInstrumento: $scope.datos.IdTipoInstrumento,
		instrumento: undefined,
		AccionOperacion: $scope.datos.AccionOperacion,
		IdBolsa: $scope.datos.IdBolsa,
		IdPortafolio: $scope.datos.IdPortafolio,
		IdMonedaOperacion: $scope.datos.IdMonedaOperacion,
		IdCondicionDeLiquidacion: $scope.datos.IdCondicionDeLiquidacion,
		MarcaAsignacion: $scope.datos.MarcaAsignacion
	};

	$scope.filtros.clasificacionTipoInstrumento = {
			logic : "or",
			RV : { field : "IdClasificacionTipoInstrumento" , operator : "eq" , value : "RV"},
			DV : { field : "IdClasificacionTipoInstrumento" , operator : "eq" , value : "DV"},
	};
	$scope.filtros.tipoInstrumento = {
			IdClasificacionTipoInstrumento : { field : "IdClasificacionTipoInstrumento " , operator : "eq" , value : ""},
			EsOrden : { field : "EsOrden" , operator : "eq" , value : "S"},
	}
	$scope.filtros.operacion = {
			logic : "or",
			C : { field : "AccionOrden" , operator : "eq" , value : "COMPRA"},
			V : { field : "AccionOrden" , operator : "eq" , value : "VENTA"},
	}
	$scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};

	$scope.inputsDeshabilitados = function () {
    	return Number($scope.datos.IdAsignacionMasiva) >= 0 && $scope.datos.IdAsignacionMasiva !== "";
	};

	$scope.verElementos = function () {
		if ($scope.datos.IdAsignacionMasiva && $scope.datos.IdAsignacionMasiva == 0) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_ID_ASIGNACION_MASIVA_MAYOR_0");
    		return;
		}
		bcsComunes.ventanaAbrir("PrevisualizarDesasignacion", $scope.datos);
	}
	
	$scope.aceptar = function() {
		let soloIdAsignacionMasiva = $scope.inputsDeshabilitados() === true;
		let idasignacionmasiva = soloIdAsignacionMasiva ? (Number($scope.datos.IdAsignacionMasiva) >= 0 ? Number($scope.datos.IdAsignacionMasiva) : undefined) : undefined;		
		let idinstrumento = !soloIdAsignacionMasiva ? ($scope.datos.instrumento && $scope.datos.instrumento.IdInstrumento > 0 ? $scope.datos.instrumento.IdInstrumento : undefined) : undefined;
		let idlibro = !soloIdAsignacionMasiva ? ($scope.datos.IdLibroOrdenes > 0 ? $scope.datos.IdLibroOrdenes : undefined) : undefined;
		let idbolsa = !soloIdAsignacionMasiva ? ($scope.datos.IdBolsa ? $scope.datos.IdBolsa : undefined) : undefined;
		let idPortafolio = !soloIdAsignacionMasiva ? ($scope.datos.IdPortafolio ? $scope.datos.IdPortafolio : undefined) : undefined;
		let idclasificacion = !soloIdAsignacionMasiva ? ($scope.datos.IdClasificacionTipoInstrumento ? $scope.datos.IdClasificacionTipoInstrumento : undefined) : undefined;
		let idtipoinstrumento = !soloIdAsignacionMasiva ? ($scope.datos.IdTipoInstrumento ? $scope.datos.IdTipoInstrumento : undefined) : undefined;
		let idaccion = !soloIdAsignacionMasiva ? ($scope.datos.AccionOperacion && $scope.datos.AccionOperacion.trim() !== "TODOS" ? $scope.datos.AccionOperacion : undefined) : undefined;
		let idmoneda = !soloIdAsignacionMasiva ? ($scope.datos.IdMonedaOperacion ? $scope.datos.IdMonedaOperacion : undefined) : undefined;
		let idcondicionliquidacion = !soloIdAsignacionMasiva ? ($scope.datos.IdCondicionDeLiquidacion ? $scope.datos.IdCondicionDeLiquidacion : undefined) : undefined;
		let marcaasignacion = !soloIdAsignacionMasiva ? ($scope.datos.MarcaAsignacion && $scope.datos.MarcaAsignacion.trim() !== "" ? $scope.datos.MarcaAsignacion : undefined) : undefined;

		let datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: "ORD",
				p_FechaHasta 			: util.dateSPseconddate($scope.datos.fechaHasta),
				p_FechaDesde 			: util.dateSPseconddate($scope.datos.fechaDesde),
				p_IdLibroOrdenes		: idlibro,
				p_IdClasificacionTipoInstrumento : idclasificacion,
				p_IdTipoInstrumento		: idtipoinstrumento,
				p_Accion                : idaccion,
				p_IdBolsa				: idbolsa,		
				p_IdInstrumento			: idinstrumento,
				p_IdTipoCanalIngreso	: $scope.datos.IdTipoCanalIngreso,
				p_IdPortafolio			: idPortafolio,
				p_IdAsignacionMasiva	: idasignacionmasiva,
				p_IdMonedaOperacion		: idmoneda,
				p_IdCondicionDeLiquidacion : idcondicionliquidacion,
				p_MarcaAsignacion		: marcaasignacion
		};

		if (soloIdAsignacionMasiva && $scope.datos.IdAsignacionMasiva == 0) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_ID_ASIGNACION_MASIVA_MAYOR_0");
    			return;
		}

		let t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_ESTA_SEGURO_DE_EJECUTAR_DESASIGNACION"), OPCIONES_BCSALERTA.SI_NO);
		t.result.then(function (result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				let p = bcsServicioOrden.EjecutarDesasignacion(datos).$promise;
				bcsComunes.ventanaOverlay(idVentana, true);
				p.then(function (result) {
					bcsComunes.ventanaOverlay(idVentana, false);
					if (result.p_out_Mensajes.length === 0) {
						bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_NO_SE_ENCONTRARON_ASIGNACIONES");
					} else {
						bcsComunes.ventanaAbrir(
							"VentanaResultado", {
							titulo: $translate.instant("UI_RESULTADO_DESASIGNACION"),
							campoSecuencia: "id",
							nombreSecuencia: "UI_SECUENCIA_ASIGNACION",
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
	}

	$scope.datos.fechaDesde = util.date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema).toDate()
	$scope.datos.fechaHasta = util.dateTerminoDia(util.date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema).toDate())

	$scope.$watch("datos.IdAsignacionMasiva", function (newVal, oldVal) {
		if (newVal === oldVal) return;
		let limpio = (newVal || "").toString().replace(/\D+/g, "");
		if (limpio !== newVal) {
			$scope.datos.IdAsignacionMasiva = limpio;
			return;
		}
		
		if (limpio.length > 0) {
			$scope.inputsHabilitados = false;

			angular.extend($scope.datos, {
				IdTipoCanalIngreso: valoresIniciales.IdTipoCanalIngreso,
				IdLibroOrdenes: valoresIniciales.IdLibroOrdenes,
				IdClasificacionTipoInstrumento: valoresIniciales.IdClasificacionTipoInstrumento,
				IdTipoInstrumento: valoresIniciales.IdTipoInstrumento,
				instrumento: undefined,
				AccionOperacion: valoresIniciales.AccionOperacion,
				IdBolsa: valoresIniciales.IdBolsa,
				IdPortafolio: valoresIniciales.IdPortafolio,
				IdMonedaOperacion: valoresIniciales.IdMonedaOperacion,
				IdCondicionDeLiquidacion: valoresIniciales.IdCondicionDeLiquidacion,
				MarcaAsignacion: valoresIniciales.MarcaAsignacion
			});
		} else { $scope.inputsHabilitados = true;}
	});
});
