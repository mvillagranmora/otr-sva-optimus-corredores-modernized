angular.module('ModuloOrdenes').controller('SelectorTransaccionesCtrl',function($scope,bcsServicioOrdenV2,bcsComunes,bcsConfiguracionOrdenesV2){
		
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	var conf = {};
	
	$scope.configuracionInterfaz = {
			filtrosTipoInstrumento : {
				IdClasificacionTipoInstrumento : { field   : "IdClasificacionTipoInstrumento",operator: "eq",value   : "" }
			},
			filtrosOperacion : {
				IdTipoInstrumento : { field   : "IdTipoInstrumento", operator: "eq", value   : "" },
				EsTransaccion : { field   : "EsTransaccion", operator: "eq",value   : "S" }
			}
	};
	
	conf = bcsConfiguracionOrdenesV2.getConfiguracionSelectorTransacciones();
	
	$scope.configuracion = conf.options;
	
	$scope.defecto = {
			AccionOrden : parametros.AccionOrden,
			IdClasificacionTipoInstrumento : parametros.IdClasificacionTipoInstrumento,
			IdLibroOrdenes : parametros.IdLibroOrdenes,
			IdTipoInstrumento : parametros.IdTipoInstrumento,
			IdBolsa : parametros.IdBolsa,
			instrumento: parametros.instrumento,
			IdCondicionDeLiquidacion : parametros.IdCondicionDeLiquidacion,
			EsFungible : parametros.EsFungible,
			EsVentaCorta : parametros.EsVentaCorta,
			FechaOperacion : parametros.FechaOperacion?util.dateInicioDia(parametros.FechaOperacion):undefined,
	};
	
	$scope.buscar = function() {
		$scope.filtros.fechaDesde = undefined;
		$scope.filtros.fechaHasta = undefined;
		if ($scope.filtros.FechaOperacion) {
			$scope.filtros.fechaDesde = util.dateInicioDia($scope.filtros.FechaOperacion);
			$scope.filtros.fechaHasta = util.dateTerminoDia($scope.filtros.FechaOperacion);
		}
		
		conf.buscar($scope.apiGrid,$scope.filtros);
	};
	
	$scope.aceptar = function() {
		
		if (parametros.permiteCombinar === false) {
			if (bcsServicioOrdenV2.transaccionesEstanCombinadas($scope.seleccionado)) {
				return;
			}
		}
		
		bcsComunes.ventanaData(idVentana, $scope.seleccionado);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData(idVentana, undefined);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.instrumento = false;
		$scope.filtros.AccionOrden = 0;
		$scope.filtros.IdTipoInstrumento = 0;
		$scope.filtros.IdClasificacionTipoInstrumento = 0;
		$scope.filtros.EsFungible = $scope.defecto.EsFungible !== undefined? $scope.defecto.EsFungible : false;
		$scope.filtros.esFungibleDeshabilitado = $scope.defecto.EsFungible !== undefined? true : false;
		$scope.filtros.EsVentaCorta = $scope.defecto.EsVentaCorta !== undefined? $scope.defecto.EsVentaCorta : false;
		$scope.filtros.esVentaCortaDeshabilitado = $scope.defecto.EsVentaCorta !== undefined? true : false;;
		$scope.filtros.FechaOperacion = $scope.defecto.FechaOperacion?$scope.defecto.FechaOperacion:util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.marcaNegociacion = undefined;
	};

	//GO
	$scope.filtros = {};    	
  	$scope.limpiarFiltros();
  	

});
