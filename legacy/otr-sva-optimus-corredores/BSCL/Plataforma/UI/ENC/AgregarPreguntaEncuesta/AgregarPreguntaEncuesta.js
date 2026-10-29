angular.module('ModuloPlataforma').controller('AgregarPreguntaEncuestaCtrl', function($scope,OPCIONES_BCSALERTA,$translate,bcsComunes,bcsServicioPlataforma,bcsAlerta){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);;
	
	
	$scope.form = {};
	
	var params = bcsComunes.ventanaParams($scope.idVentana);
	if (params.data) {
		$scope.form.pregunta = params.data.TextoPregunta;
	}
	
	$scope.form.aceptar =  function() {
		bcsComunes.ventanaData($scope.idVentana,{TextoPregunta : $scope.form.pregunta});
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
	
	$scope.form.cancelar = function() {
		bcsComunes.ventanaData($scope.idVentana,undefined);
		bcsComunes.ventanaCerrar($scope.idVentana);
	}
		
});