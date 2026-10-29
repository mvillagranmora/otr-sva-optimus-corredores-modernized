angular.module('ModuloFFM').controller('CuadraturaPagoDividendosAGFCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var conf = bcsConfiguracionFFM.getEventoCapitalFondoMutuo();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	var confDetail = bcsConfiguracionFFM.getDetalleEventoCapitalFondoMutuo();
	$scope.defaultDetailsOptions = confDetail.options;

	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.formData.setFondo = "-1";
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.defaultDetailsOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.buscar = function(apiGridCuadratura){
		var filtros = {
			IdTipoEventoCapital : 'DF',
			IdEstado : 'CER'
		}
		if($scope.filtros.FondoMutuo){
			filtros.IdInstrumento = $scope.filtros.FondoMutuo.IdInstrumento;
		}
		if($scope.filtros.fecha){
			filtros.FechaPagoDesde = util.dateInicioDia($scope.filtros.fecha);
			filtros.FechaPagoHasta = util.dateTerminoDia($scope.filtros.fecha)
		}
		conf.buscar(apiGridCuadratura,filtros)
	}
	
	$scope.ConsultaEventoCapital = function(apiGridCuadraturaDetalle,seleccionEventoCapital){
		var filtros = {
			IdDividendo : seleccionEventoCapital.IdEventoCapital
		}
		confDetail.buscar(apiGridCuadraturaDetalle,filtros)
	}
	
	$scope.limpiarFiltros();
});