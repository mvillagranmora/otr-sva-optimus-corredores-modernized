angular.module('ModuloPlataforma').controller('AgregarRespuestaEncuestaCtrl', function($scope,OPCIONES_BCSALERTA,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	
	
	$scope.form = {};
	var params = bcsComunes.ventanaParams($scope.idVentana);
	if (params.data) {
		$scope.form.respuesta = params.data.TextoRespuesta;
		$scope.form.puntaje = params.data.Puntaje;
	}
	
	
	$scope.form.aceptar =  function() {
		bcsComunes.ventanaData($scope.idVentana,{TextoRespuesta : $scope.form.respuesta, Puntaje : $scope.form.puntaje});
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.form.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
});