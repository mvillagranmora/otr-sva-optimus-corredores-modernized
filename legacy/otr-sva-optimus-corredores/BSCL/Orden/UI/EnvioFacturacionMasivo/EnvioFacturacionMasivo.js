angular.module('ModuloOrdenes').controller('EnvioFacturacionMasivoCtrl',function ($scope,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes, bcsAlerta, OPCIONES_BCSALERTA) {
	let controllerName = 'EnvioFacturacionMasivoCtrl';

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoHeight($scope.idVentana);
	
	$scope.datos = {};
	$scope.filtros = {}
	$scope.inputsHabilitados = true;


	$scope.CanalCliente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "CLI" };
	$scope.CanalTrader	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "TRA" };
	$scope.CanalAgente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "AGE" };
	$scope.CanalOWP		= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "OWP" };
	
	$scope.filtros.filtrosCanalIngreso = {
		complejo : [$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente,$scope.CanalOWP],
		logic : "or",
		filters : [
			$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente, $scope.CanalOWP
		]
	};
	
	$scope.filtros.tipoInstrumento = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-tipo-instrumento');
	$scope.filtros.condicionDeLiquidacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-condicion-liquidacion');
	$scope.filtros.operacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-operacion');
	$scope.filtros.canalDistribucion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName,'combobox-tipo-canal-ingreso');

	let valoresIniciales = {
		fechaProceso: $scope.datos.fechaProceso,
		horaHasta: $scope.datos.horaHasta,
		IdClasificacionTipoInstrumento: $scope.datos.IdClasificacionTipoInstrumento,
		IdTipoInstrumento: $scope.datos.IdTipoInstrumento,
		AccionOperacion: $scope.datos.AccionOperacion,
		IdCondicionDeLiquidacion: $scope.datos.IdCondicionDeLiquidacion,
		IdLibroOrdenes: $scope.datos.IdLibroOrdenes,
		IdBolsa: $scope.datos.IdBolsa,
		IdOrden: $scope.datos.IdOrden,
		FolioTransaccion: $scope.datos.FolioTransaccion,
		IdTipoCanalIngreso: $scope.datos.IdTipoCanalIngreso,
		IdInstrumento: $scope.datos.IdInstrumento,
		MarcaAsingnacion: $scope.datos.MarcaAsingnacion
	};

	$scope.verElementos = function() {
		
		let filtros = getFiltros($scope.datos);
		
		let datos = {
				p_IdEmpresa 						: $scope.SesionIdEmpresa,
				p_IdUsuario          			 	: $scope.SesionIdUsuario,
				p_IdModulo           			 	: 'ORD',
				p_HoraHasta 						: util.formatoTime($scope.datos.horaHasta),
				p_IdClasificacionTipoInstrumento	: filtros.IdClasificacionTipoInstrumento,
				p_IdTipoInstrumento					: filtros.IdTipoInstrumento,
				p_IdLibroOrdenes					: filtros.IdLibroOrdenes,
				p_IdAccionOrden						: filtros.IdAccionOperacion,
				p_IdCondicionDeLiquidacion			: filtros.IdCondicionDeLiquidacion,		
				p_FolioTransaccion					: filtros.FolioTransaccion,		
				p_IdOrden							: filtros.IdOrden,	
				p_IdBolsa							: filtros.IdBolsa,
				p_IdAsignacionMasiva                : filtros.IdAsignacionMasiva,
				p_IdInstrumento						: filtros.IdInstrumento,
				p_IdTipoCanalIngreso			: filtros.IdTipoCanalIngreso,
				p_MarcaAsignacion					: filtros.MarcaAsignacion,
		};

		if (filtros.IdAsignacionMasiva == 0) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_ID_ASIGNACION_MASIVA_MAYOR_0");
    		return;
		}
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		let p = bcsServicioOrden.ConsultarInformacionEnvioFacturacionMasivo(datos).$promise;
		p.then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaAbrir('VentanaInformativa',{ titulo: $translate.instant('UI_INFORMACION_ENVIO_FACTURACION'),tipo  : 'INFORMACION_ENVIO_FACTURACION' , datos : result.P_OUT_ASIGNACIONES } ,false,false,$scope.idVentana);
		},function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
		});
	};
	
	$scope.aceptar = function() {
		
		let filtros = getFiltros($scope.datos);
		
		let datos = {
				p_IdEmpresa 						: $scope.SesionIdEmpresa,
				p_IdUsuario          			 	: $scope.SesionIdUsuario,
				p_IdModulo           			 	: 'ORD',
				p_HoraHasta 						: util.formatoTime($scope.datos.horaHasta),
				p_IdClasificacionTipoInstrumento	: filtros.IdClasificacionTipoInstrumento,
				p_IdTipoInstrumento					: filtros.IdTipoInstrumento,
				p_IdLibroOrdenes					: filtros.IdLibroOrdenes,
				p_IdAccionOrden						: filtros.IdAccionOperacion,
				p_IdCondicionDeLiquidacion			: filtros.IdCondicionDeLiquidacion,		
				p_FolioTransaccion					: filtros.FolioTransaccion,		
				p_IdOrden							: filtros.IdOrden,	
				p_IdBolsa							: filtros.IdBolsa,
				p_IdAsignacionMasiva                : filtros.IdAsignacionMasiva,
				p_IdInstrumento						: filtros.IdInstrumento,
				p_IdTipoCanalIngreso				: filtros.IdTipoCanalIngreso,
				p_MarcaAsignacion					: filtros.MarcaAsignacion,
		};

		if (filtros.IdAsignacionMasiva == 0) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_ID_ASIGNACION_MASIVA_MAYOR_0");
    		return;
		}
		
		let t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_ESTA_SEGURO_DE_FACTURAR_MASIVA"), OPCIONES_BCSALERTA.SI_NO);
		t.result.then(function (result) {
			if (result === OPCIONES_BCSALERTA.SI) {
				bcsComunes.ventanaOverlay($scope.idVentana, true);
				bcsServicioOrden.EnvioFacturacionMasivo(datos).$promise.then(function (result) {
					bcsComunes.ventanaOverlay($scope.idVentana,true);
					if (!result.P_OUT_MENSAJES || result.P_OUT_MENSAJES.length === 0  || result.P_OUT_MENSAJES[0].codigo == 'NO_SE_ENCONTRARON_ASIGNACIONES') {
						bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_NO_SE_ENCONTRARON_REGISTROS_A_FACTURAR");
						bcsComunes.ventanaOverlay($scope.idVentana, false);
					} else {
						bcsComunes.ventanaAbrir('VentanaResultado', { 
							titulo: $translate.instant('UI_RESULTADO_ENVIO_FACTURACION'), 
							campoSecuencia: "id", 
							nombreSecuencia: 'UI_SECUENCIA_ASIGNACION',
							datos: result.P_OUT_MENSAJES, 
							usaNuevosMensajes: true 
						}, false, false, $scope.idVentana);
					}
				}, function (result) { 
					bcsComunes.ventanaOverlay($scope.idVentana, false); 
					bcsComunes.ventanaMsg({ conflictos: util.parseServerResponse(result) }, $scope.idVentana);});
			}
		});
	};
	
	let getFiltros = function(filtrosIN) {
		return {
			IdLibroOrdenes: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.IdLibroOrdenes || undefined),
			IdBolsa: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.IdBolsa || undefined),
			IdClasificacionTipoInstrumento: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.IdClasificacionTipoInstrumento || undefined),
			IdTipoInstrumento: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.IdTipoInstrumento || undefined),
			IdAccionOperacion: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.AccionOperacion || undefined),
			IdCondicionDeLiquidacion: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.IdCondicionDeLiquidacion || undefined),
			FolioTransaccion: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.FolioTransaccion || undefined),
			IdOrden: filtrosIN.IdAsignacionMasiva ? undefined : (filtrosIN.IdOrden || undefined),
			FechaHoraHasta: util.date(util.dateSPdate(filtrosIN.fechaProceso) + ' ' + util.formatoTime(filtrosIN.horaHasta)).toDate(),
			IdAsignacionMasiva: filtrosIN.IdAsignacionMasiva ? Number(filtrosIN.IdAsignacionMasiva) : undefined,
			IdInstrumento: filtrosIN.Instrumento ? Number(filtrosIN.Instrumento.IdInstrumento) : undefined,
			IdTipoCanalIngreso: filtrosIN.IdTipoCanalIngreso ? filtrosIN.IdTipoCanalIngreso : undefined,
			MarcaAsignacion: filtrosIN.MarcaAsignacion ? filtrosIN.MarcaAsignacion : undefined,
		};
	};

	$scope.$watch('datos.IdAsignacionMasiva', function (newval, oldval) {
	$scope.datos.fechaProceso = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.datos.horaHasta = new Date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema);

		let limpio = (newval ?? '').toString().replace(/\D+/g, '');
		if (limpio !== newval) { 
			$scope.datos.IdAsignacionMasiva = limpio;
			return;
		}

		if (limpio.length > 0) {
			$scope.inputsHabilitados = false;
			angular.extend($scope.datos, {
				fechaProceso: $scope.datos.fechaProceso,
				horaHasta: $scope.datos.horaHasta,
				IdClasificacionTipoInstrumento: valoresIniciales.IdClasificacionTipoInstrumento,
				IdTipoInstrumento: valoresIniciales.IdTipoInstrumento,
				AccionOperacion: valoresIniciales.AccionOperacion,
				IdCondicionDeLiquidacion: valoresIniciales.IdCondicionDeLiquidacion,
				IdLibroOrdenes: valoresIniciales.IdLibroOrdenes,
				IdBolsa: valoresIniciales.IdBolsa,
				IdOrden: valoresIniciales.IdOrden,
				FolioTransaccion: valoresIniciales.FolioTransaccion
			});
		} else {
			$scope.inputsHabilitados = true;
		}
	});	

	$scope.$watch('datos.MarcaAsignacion', function (newVal) {
		if (newVal) {
			var limpio = newVal.replace(/[^a-zA-Z0-9_]/g, '');
			if (limpio !== newVal) $scope.datos.MarcaAsignacion = limpio;
		}
	});
});
