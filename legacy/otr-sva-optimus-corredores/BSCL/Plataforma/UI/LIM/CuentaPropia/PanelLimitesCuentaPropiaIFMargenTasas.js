(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaIFMargenTasasCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPIFMT = this;
		plCPIFMT.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPIFMT.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPIFMT.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());