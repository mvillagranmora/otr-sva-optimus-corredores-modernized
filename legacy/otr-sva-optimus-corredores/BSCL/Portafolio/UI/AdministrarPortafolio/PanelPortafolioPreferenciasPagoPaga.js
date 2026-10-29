/*********************************************************
 * Purpose	: Controlador para administrar Preferencias 
 *            de Pago "PAGA" para un Portafolio
 * Author	: JGarces - BCS
 * Date		: 21/03/2016
 ********************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPreferenciasPagoPagaCtrl',
	function($state,$scope,$translate,bcsComunes,bcsAlerta,$compile){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pPortPPP = this; 

		/*********************************************
		 * HEREDAR DATOS CONTROLADOR PADRE
		 ********************************************/
		$scope.admPort.portafolioPromise.then(function(){
			if($scope.admPort && $scope.admPort.datos) {
				pPortPPP.IdPortafolio = $scope.admPort.datos.IdPortafolio;	
				pPortPPP.IdPersona = $scope.admPort.datos.IdPersona;
			}
			pPortPPP.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PREFERENCIAS_PAGO'];
		});
	});
}());
