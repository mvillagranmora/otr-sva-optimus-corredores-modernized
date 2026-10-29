(function() {
	angular.module('ModuloPlataforma').controller('PanelInformacionClienteCtrl',function($scope,bcsComunes){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		const IndPInfCli = this;
		IndPInfCli.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		IndPInfCli.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		IndPInfCli.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");

/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/		
	});
}());