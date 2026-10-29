angular.module('ModuloFFM').controller('CertificarAporteRescateDiferidosCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {'EsDiferido':'S'};
	var conf = bcsConfiguracionFFM.getConsultaSolicitudAporteRescateDiferidos();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarSolicitudes = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.formData.filtrosFondosMutuos = {
			IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value"   :  "INFM"}
	};
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
		
	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate : items[0].IdSolicitudAporteRescate} );
  	};
	
  	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.IdTipoMovimientoFondoMutuo = 0;
		$scope.filtros.IdEstado = 0;
		$scope.filtros.IdEmisor = 0;
		$scope.formData.setFondo = "-1";
	};
	
	$scope.limpiarFiltros();
});