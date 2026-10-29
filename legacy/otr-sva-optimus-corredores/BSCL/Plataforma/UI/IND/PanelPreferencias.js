(function() {
	angular.module('ModuloPlataforma').controller('PanelPreferenciasCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var IndPPref = this;
		IndPPref.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		IndPPref.parametros	= bcsComunes.ventanaParams($scope.idVentana);
		IndPPref.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		

/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/
		
	});
}());