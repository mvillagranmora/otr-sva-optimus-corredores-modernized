(function() {
	angular.module('ModuloPlataforma').controller('PanelMargenesTasasClienteCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pMTC = this;
		pMTC.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pMTC.parametros	= bcsComunes.ventanaParams(pMTC.idVentana);
		pMTC.domVentana = $("[ventanaid='"+pMTC.idVentana+"']");
		

/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/
		
	});
}());