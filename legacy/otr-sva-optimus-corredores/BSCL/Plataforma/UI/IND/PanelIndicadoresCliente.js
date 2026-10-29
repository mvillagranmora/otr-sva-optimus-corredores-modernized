(function() {
	angular.module('ModuloPlataforma').controller('PanelIndicadoresClienteCtrl',function($scope,bcsComunes,$compile){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var IndPIndCli = this;
		IndPIndCli.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		IndPIndCli.parametros 	= bcsComunes.ventanaParams($scope.idVentana);
		IndPIndCli.domVentana 	= $("[ventanaid='"+$scope.idVentana+"']");	

/*********************************************
 * COMBOBOX
 ********************************************/
		
/*********************************************
 * FUNCIONES
 ********************************************/
		
	});
}());