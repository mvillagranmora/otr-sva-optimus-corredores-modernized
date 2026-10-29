angular.module('ModuloOrdenes').controller('VentanaInformativaCtrl',function ($scope,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenesV2) {

	$scope.apiGrid;	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana); //tipo, datos
	bcsComunes.ventanaTitulo(idVentana,parametros.titulo)
	
	if (parametros.tipo === 'INFORMACION_ENVIO_FACTURACION') {
		$scope.confGrid = bcsConfiguracionOrdenesV2.getConfiguracionInformacionEnvioFacturacionMasivo($scope.apiGrid,parametros.datos);
	}
	
	$scope.aceptar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};		
});