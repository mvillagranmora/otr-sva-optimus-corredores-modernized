(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaCarteraEmisorCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPCE = this;
		plCPCE.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPCE.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPCE.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());