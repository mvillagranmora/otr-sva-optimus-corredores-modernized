(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaSpotPosInterbanCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPSPI = this;
		plCPSPI.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPSPI.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPSPI.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());