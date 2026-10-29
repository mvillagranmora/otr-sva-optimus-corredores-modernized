angular.module('ModuloFFM').controller('ConsultaValorCuotaVigenteCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaValoresCuotaVigente();
	$scope.defaultOptions = conf.options;
	$scope.buscarValoresCuota = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaVigenciaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaVigenciaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.IdEmisor = '0';
		$scope.formData.setFondo = "-1";
	};
	
	$scope.limpiarFiltros();
})