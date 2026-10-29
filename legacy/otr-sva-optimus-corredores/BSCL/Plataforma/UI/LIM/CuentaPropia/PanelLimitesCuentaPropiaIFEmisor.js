(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaIFEmisorCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPIFE = this;
		plCPIFE.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPIFE.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPIFE.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());