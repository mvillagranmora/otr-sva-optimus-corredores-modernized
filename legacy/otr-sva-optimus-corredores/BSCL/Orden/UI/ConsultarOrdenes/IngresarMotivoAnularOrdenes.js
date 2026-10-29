angular.module('ModuloOrdenes').controller('IngresarMotivoAnularOrdenesCtrl',function($scope,bcsComunes)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.motivo = "";
	$scope.aceptar = function() {

		bcsComunes.ventanaData(idVentana,$scope.motivo);
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	$scope.cancelar = function() {
		bcsComunes.ventanaData(idVentana,undefined);
		bcsComunes.ventanaCerrar(idVentana);
	}

});


