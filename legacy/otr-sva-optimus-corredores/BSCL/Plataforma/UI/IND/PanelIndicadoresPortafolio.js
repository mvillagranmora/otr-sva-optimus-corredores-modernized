(function() {
	angular.module('ModuloPlataforma').controller('PanelIndicadoresPortafolioCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var IndPIndPor = this;
		IndPIndPor.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		IndPIndPor.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		IndPIndPor.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");	

/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/		
	});
}());