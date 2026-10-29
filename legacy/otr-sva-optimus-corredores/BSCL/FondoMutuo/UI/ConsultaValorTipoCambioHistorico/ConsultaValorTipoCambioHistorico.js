angular.module('ModuloFFM').controller('ConsultaValorTipoCambioHistoricoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaValoresTipoCambioHistorico();
	$scope.defaultOptions = conf.options;
	$scope.buscarValoresTipoCambio = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaCargaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaCargaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.IdEmisor = '0';
		$scope.formData.setFondo = "-1";
	};
	
	$scope.limpiarFiltros();
})