(function() {
	angular.module('ModuloPlataforma').controller('PanelMargenesTasasCustodiaCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pMTCu = this;
		pMTCu.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pMTCu.parametros= bcsComunes.ventanaParams(pMTCu.idVentana);
		pMTCu.domVentana= $("[ventanaid='"+pMTCu.idVentana+"']");
		

/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/
		
	});
}());