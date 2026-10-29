(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaForwardCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPF = this;
		plCPF.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPF.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPF.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());