(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRFMargenTasasCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRFMT = this;
		plCPRFMT.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRFMT.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRFMT.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());