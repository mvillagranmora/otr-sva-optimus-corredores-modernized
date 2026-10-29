angular.module('ModuloPlataforma').controller('FormularioIngresoCtrl',
	function($scope,$translate,$timeout,bcsComunes,bcsServicioPlataforma){

	//Configuracion de Ventana Inicial
	bcsComunes.configurarVentana(900,780,true,true,'UI_TRANSACCION_MANUAL');
	
	$scope.data = {};
	
	
});


