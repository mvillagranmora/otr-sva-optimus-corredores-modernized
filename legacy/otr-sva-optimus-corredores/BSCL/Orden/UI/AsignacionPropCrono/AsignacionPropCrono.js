angular.module('ModuloOrdenes').controller('AsignacionPropCronoCtrl', function ($scope, bcsComunes, bcsServicioOrden, bcsConfiguracionOrdenes,$translate) {
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoHeight(idVentana);

	$scope.datos = {};
	$scope.filtros = {};
	
	let controllerName = 'AsignacionPropCronoCtrl';

	$scope.filtros.tipoAsignacion = {
		SoportaAsignacionAutomatica : {"field" : "SoportaAsignacionAutomatica", "operator" : "eq", "value" : 'S'}
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
	};

	$scope.verElementos = function() {
		bcsComunes.ventanaAbrir('InformacionAsignacionPropCrono',generarFiltros($scope.datos),false,false,idVentana);
	};
	
	$scope.datos.filtrosCondicionDeLiquidacion = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName, 'combobox-condicion-liquidacion');
	var filtrosBase = bcsConfiguracionOrdenes.UI.getFiltrosCombo(controllerName, 'combobox-tipo-instrumento');
	angular.extend($scope.filtros.tipoInstrumento, filtrosBase);

	$scope.datos.filtrosInstrumentos = {
		IdClasificacionTipoInstrumento: { field: "IdClasificacionTipoInstrumento", operator: "eq", value: "" },
		IdTipoInstrumento: { field: "IdTipoInstrumento", operator: "eq", value: "" }
	};

	$scope.datos.oConfiguracionMonedaNegociacion = { filtros: {} };
	$scope.datos.itemPostFiltroMonedaNegociacion = 1;

	$scope.CanalCliente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "CLI" };
	$scope.CanalTrader	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "TRA" };
	$scope.CanalAgente	= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "AGE" };
	$scope.CanalOWP		= { "field" : "IdTipoCanalIngreso","operator": "eq", "value"   : "OWP" };
	
	$scope.datos.filtrosCanalIngreso = {
		complejo : [$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente,$scope.CanalOWP],
		logic : "or",
		filters : [
			$scope.CanalCliente,$scope.CanalTrader,$scope.CanalAgente, $scope.CanalOWP
		]
	};
	
	const regexMarcaAsignacion = /^[a-zA-Z0-9_]+$/;
	
	var generarFiltros = function(filtros) {
		var obj = {};
		obj.IdLibroOrdenes = $scope.datos.IdLibroOrdenes > 0?$scope.datos.IdLibroOrdenes : undefined;
		obj.IdBolsa = $scope.datos.IdBolsa !== 0?$scope.datos.IdBolsa : undefined;
		obj.IdClasificacionTipoInstrumento = $scope.datos.IdClasificacionTipoInstrumento !==0?$scope.datos.IdClasificacionTipoInstrumento : undefined;
		obj.IdCondicionDeLiquidacion = $scope.datos.IdCondicionDeLiquidacion !== 0?$scope.datos.IdCondicionDeLiquidacion : undefined;
		obj.IdTipoInstrumento = $scope.datos.IdTipoInstrumento !== 0?$scope.datos.IdTipoInstrumento : undefined;
		obj.AccionOrden = $scope.datos.AccionOrden !== 0?$scope.datos.AccionOrden : undefined;
		obj.HoraHasta = util.formatoTime($scope.datos.horaHasta);
		obj.IdCanalIngreso = $scope.datos.IdTipoCanalIngreso !== 0?$scope.datos.IdTipoCanalIngreso : undefined;
		obj.IdInstrumento = ($scope.datos.IdInstrumento !== "-1" && $scope.datos.IdInstrumento) ? $scope.datos.IdInstrumento : undefined;
		obj.IdMonedaNegociacion = $scope.datos.IdMonedaNegociacion !== 0 && $scope.datos.IdMonedaNegociacion ? $scope.datos.IdMonedaNegociacion : undefined;
		obj.MarcaAsignacion = $scope.datos.MarcaAsignacion !== ''?$scope.datos.MarcaAsignacion : undefined;
		return obj;
	};

	$scope.aceptar = function () {
		var filtros = generarFiltros($scope.datos);
		var datos = {
				p_IdEmpresa 						: $scope.SesionIdEmpresa,
				p_IdUsuario 						: $scope.SesionIdUsuario,
				p_IdModulo 							: 'ORD',
				p_HoraHasta 						: filtros.HoraHasta,
				p_IdTipoAsignacion					: $scope.datos.tipoAsignacion,
				p_IdLibroOrdenes					: filtros.IdLibroOrdenes,
				p_IdClasificacionTipoInstrumento	: filtros.IdClasificacionTipoInstrumento,
				p_IdCondicionDeLiquidacion			: filtros.IdCondicionDeLiquidacion,
				p_IdTipoInstrumento					: filtros.IdTipoInstrumento,
				p_IdAccionOrden						: filtros.AccionOrden,
				p_IdBolsa							: filtros.IdBolsa,
				p_CanalIngreso						: filtros.IdCanalIngreso,
				p_MarcaAsignacion					: filtros.MarcaAsignacion,
				p_IdInstrumento						: filtros.IdInstrumento,
				p_IdMonedaNegociacion				: filtros.IdMonedaNegociacion
		};

		if(datos.p_MarcaAsignacion && datos.p_MarcaAsignacion.length > 25) {
			bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_MARCA_ASIGNACION_LONGITUD");
			return
		}

		if(datos.p_MarcaAsignacion && !regexMarcaAsignacion.test(datos.p_MarcaAsignacion)) {
			bcsComunes.msg("error", "UI_ERROR", "UI_ERROR_MARCA_ASIGNACION_FORMATO");
			return;
		}

		var p = bcsServicioOrden.EjecutarAsignacionPorHora(datos).$promise;
		bcsComunes.ventanaOverlay(idVentana,true);
			p.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_RESULTADO.length === 0) {
					bcsComunes.ventanaMsg({
						mensaje: '',
						conflictos: util.parseServerResponse(result)
						}, idVentana)

				}
				else {
					bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo:  $translate.instant('UI_RESULTADO_CREAR_ASIGNACIONES'), campoSecuencia : "id" ,
						nombreSecuencia : 'UI_SECUENCIA',datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
				}
			},
			function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
		});
	};

	$scope.datos.horaHasta = new Date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema);

	$scope.$watch('datos.IdInstrumento', function (nuevoId, viejoId) {
		if (nuevoId && nuevoId !== "-1" && nuevoId !== viejoId) {
			$scope.datos.itemPostFiltroMonedaNegociacion = 2;
			if ($scope.datos.instrumento && $scope.datos.instrumento.IdMoneda) {
				$scope.datos.IdMonedaNegociacion = $scope.datos.instrumento.IdMoneda;
				if (!$scope.datos.oConfiguracionMonedaNegociacion) {$scope.datos.oConfiguracionMonedaNegociacion = {};}
				$scope.datos.oConfiguracionMonedaNegociacion.filtros = {IdMoneda: {field: "IdMoneda", operator: "eq", value: $scope.datos.instrumento.IdMoneda}};
				$scope.datos.oConfiguracionMonedaNegociacion.seleccionado = {IdMoneda: $scope.datos.instrumento.IdMoneda};
			}
		} else {
			$scope.datos.IdMonedaNegociacion = null;
			$scope.datos.oConfiguracionMonedaNegociacion.filtros = {};
			$scope.datos.oConfiguracionMonedaNegociacion.seleccionado = null;
			$scope.datos.itemPostFiltroMonedaNegociacion = 1;
		}
	});

	$scope.$watch('datos.instrumento', function (nuevoInstrumento) {
		if (nuevoInstrumento && nuevoInstrumento.IdMoneda) {
			$scope.datos.itemPostFiltroMonedaNegociacion = 2;
			$scope.datos.IdMonedaNegociacion = nuevoInstrumento.IdMoneda;
			if (!$scope.datos.oConfiguracionMonedaNegociacion) {$scope.datos.oConfiguracionMonedaNegociacion = {};}
			$scope.datos.oConfiguracionMonedaNegociacion.filtros = {IdMoneda: {field: "IdMoneda", operator: "eq", value: nuevoInstrumento.IdMoneda}};
			$scope.datos.oConfiguracionMonedaNegociacion.seleccionado = {IdMoneda: nuevoInstrumento.IdMoneda};
		} else {
			$scope.datos.itemPostFiltroMonedaNegociacion = 1;
		}
	});

	$scope.$watch('datos.MarcaAsignacion', function (newVal) {
		if (newVal) {
			var limpio = newVal.replace(/[^a-zA-Z0-9_]/g, '');
			if (limpio !== newVal) $scope.datos.MarcaAsignacion = limpio;
		}
	});
});
