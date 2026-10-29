/*************************************************************
 * Purpose	: Panel Portafolio Indicadores
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	  angular.module('ModuloPortafolio').controller('PanelPortafolioIndicadoresCtrl',
	  function($scope,$translate,$stateParams){
	    var pPortInd = this;
	    pPortInd.datos = $scope.admPort.datos;
	    pPortInd.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_INDICADORES'];
		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Indicadores') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Portafolio", nombreTab: 'PanelPortafolioIndicadores' });
			} 
		});
		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Indicadores') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Portafolio", nombreTab: 'PanelPortafolioIndicadores' });
			}
		});
	  });
}());
//------------------------- FIN CONTROLLER --------------------------