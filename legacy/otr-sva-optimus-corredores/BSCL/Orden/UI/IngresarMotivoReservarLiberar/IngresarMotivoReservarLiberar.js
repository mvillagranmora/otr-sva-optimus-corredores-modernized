angular.module('ModuloOrdenes').controller('IngresarMotivoReservarLiberarCtrl',function($scope,bcsComunes)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.aceptar = function() {
		bcsComunes.ventanaData(idVentana,$scope.motivo);
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}

});


