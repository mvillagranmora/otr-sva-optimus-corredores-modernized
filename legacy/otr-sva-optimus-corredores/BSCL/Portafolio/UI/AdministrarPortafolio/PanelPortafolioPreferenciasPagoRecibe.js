/*********************************************************
 * Purpose	: Controlador para administrar Preferencias 
 *            de Pago "RECIBE" para un Portafolio
 * Author	: JGarces - BCS
 * Date		: 21/03/2016
 ********************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPreferenciasPagoRecibeCtrl',
	function($state,$scope,$translate,bcsComunes,bcsAlerta,$compile){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pPortPPR = this; 
		
		/*********************************************
		 * HEREDAR DATOS CONTROLADOR PADRE
		 ********************************************/
		$scope.admPort.portafolioPromise.then(function(){
			if($scope.admPort && $scope.admPort.datos) {
				pPortPPR.IdPortafolio = $scope.admPort.datos.IdPortafolio;
				pPortPPR.IdPersona = $scope.admPort.datos.IdPersona;
			}
			pPortPPR.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PREFERENCIAS_PAGO'];
		});
	});
}());