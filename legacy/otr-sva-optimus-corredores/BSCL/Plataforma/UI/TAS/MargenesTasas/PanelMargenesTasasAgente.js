(function() {
	angular.module('ModuloPlataforma').controller('PanelMargenesTasasAgenteCtrl',function($scope,bcsComunes,$compile){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pMTA = this;
		pMTA.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pMTA.parametros	= bcsComunes.ventanaParams(pMTA.idVentana);
		pMTA.domVentana = $("[ventanaid='"+pMTA.idVentana+"']");
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
				
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
	});
}());