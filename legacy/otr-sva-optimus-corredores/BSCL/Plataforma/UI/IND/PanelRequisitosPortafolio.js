(function() {
	angular.module('ModuloPlataforma').controller('PanelRequisitosPortafolioCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var IndPReqPor = this;
		IndPReqPor.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		IndPReqPor.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		IndPReqPor.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");
		
/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/		
	});
}());