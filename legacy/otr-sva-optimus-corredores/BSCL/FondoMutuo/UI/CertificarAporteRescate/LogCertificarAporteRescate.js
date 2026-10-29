angular.module('ModuloFFM').controller('LogCertificarAporteRescateFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var filtrosFondos = [];
	var conf = bcsConfiguracionFFM.getConsultaLogCertificacionAporteRescate();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarAportesRescatesCertificados = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaCertificacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.Simulacion = undefined;
        $scope.formData.setFondo = "-1";
	};
	
	
	$scope.buscarLogCertificacion = function(){
		filtrosFondos = [];

		if($scope.filtros.fechaCertificacion)
			filtrosFondos.push({ field: "FechaSistema", operator: "eq", value: $scope.filtros.fechaCertificacion } );
		if($scope.filtros.fondoMutuo)
			filtrosFondos.push({ field: "IdSerieFondo", operator: "eq", value: $scope.filtros.fondoMutuo.IdSerieFondo } );
		if($scope.filtros.Simulacion)
			filtrosFondos.push({ field: "Simulacion", operator: "eq", value: $scope.filtros.Simulacion } );
		apiGrid.filter(filtrosFondos);
		$scope.apiGridSolicitud.refresh;
	};
	
	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate : items[0].IdSolicitudAporteRescate} );
  	};
	
	$scope.limpiarFiltros();
	//$scope.ajustaVentana();
});