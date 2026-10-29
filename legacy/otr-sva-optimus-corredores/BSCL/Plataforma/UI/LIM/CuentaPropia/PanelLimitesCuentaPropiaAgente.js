(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaAgenteCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPA = this;
		plCPA.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPA.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPA.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());