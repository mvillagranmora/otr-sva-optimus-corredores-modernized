/*************************************************************
 * Purpose	: Panel Portafolio Indicadores
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioNotificacionesCtrl', function($scope,$translate,$stateParams){
	    var pPortNot = this;
	    pPortNot.datos = $scope.admPort.datos;
		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Notificaciones') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Portafolio", nombreTab: 'PanelPortafolioNotificaciones' });
			} 
		});
		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Notificaciones') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Portafolio", nombreTab: 'PanelPortafolioNotificaciones' });
			}
		});
	  });
}());
//------------------------- FIN CONTROLLER --------------------------