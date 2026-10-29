(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaRVMargenTasasCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPRVMT = this;
		plCPRVMT.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPRVMT.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPRVMT.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());