(function() {
	angular.module('ModuloPlataforma').controller('PanelRequisitosClienteCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var IndPReqCli = this;
		IndPReqCli.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		IndPReqCli.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		IndPReqCli.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");
		
/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/
	});
}());