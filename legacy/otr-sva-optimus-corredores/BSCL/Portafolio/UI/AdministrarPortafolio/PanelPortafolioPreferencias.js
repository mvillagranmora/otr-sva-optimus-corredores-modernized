/*************************************************************
 * Purpose	: Panel Portafolio Preferencias
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPreferenciasCtrl',
	function($scope,$translate,servicioSociodeNegocio,$stateParams){
		var pPorPref = this;
		pPorPref.datos = $scope.admPort.datos;
		pPorPref.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PREFERENCIA_NEGOCIO'];
		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Preferencias') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Portafolio", nombreTab: 'PanelPortafolioPreferenciasDeNegocio' });
			} 
		});
		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Preferencias') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Portafolio", nombreTab: 'PanelPortafolioPreferenciasDeNegocio' });
			}
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------