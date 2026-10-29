angular.module('ModuloPlataforma').controller('AlertasCtrl',function($scope,OPCIONES_BCSALERTA,bcsComunes) {

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	
	$scope.mensaje = $scope.parametros.mensaje;
	$scope.tipo = $scope.parametros.tipo;

	$scope.aceptar = function(){
		bcsComunes.ventanaData($scope.idVentana,OPCIONES_BCSALERTA.ACEPTAR);
		bcsComunes.ventanaCerrar($scope.idVentana);
    	//$modalInstance.close(OPCIONES_BCSALERTA.ACEPTAR);
    };
    
    $scope.cancelar = function(){
    	bcsComunes.ventanaData($scope.idVentana,OPCIONES_BCSALERTA.CANCELAR);
		bcsComunes.ventanaCerrar($scope.idVentana);
    	//$modalInstance.close(OPCIONES_BCSALERTA.CANCELAR);
    };
    
    $scope.si = function(){
    	bcsComunes.ventanaData($scope.idVentana, OPCIONES_BCSALERTA.SI);
		bcsComunes.ventanaCerrar($scope.idVentana);
    	//$modalInstance.close(OPCIONES_BCSALERTA.SI);
    };
    
    $scope.no = function(){
    	bcsComunes.ventanaData($scope.idVentana,OPCIONES_BCSALERTA.NO);
		bcsComunes.ventanaCerrar($scope.idVentana);
    	//$modalInstance.close(OPCIONES_BCSALERTA.NO);
    };
	var firstHeightCrear = $('#Win_VentanaAlerta');
	firstHeightCrear.css('height','auto');
});