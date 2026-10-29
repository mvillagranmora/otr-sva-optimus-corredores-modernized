(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaSpotMesaDistrCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPSMD = this;
		plCPSMD.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPSMD.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPSMD.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());