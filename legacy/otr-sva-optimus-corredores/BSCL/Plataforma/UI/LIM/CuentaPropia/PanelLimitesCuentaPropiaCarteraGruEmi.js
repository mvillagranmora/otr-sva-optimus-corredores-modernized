(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaCarteraGruEmiCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var plCPCGE = this;
		plCPCGE.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		plCPCGE.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		plCPCGE.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());