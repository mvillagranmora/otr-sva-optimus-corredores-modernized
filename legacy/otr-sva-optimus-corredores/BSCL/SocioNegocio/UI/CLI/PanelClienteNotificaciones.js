/*************************************************************
 * Purpose	: Notificaciones del Cliente
 * Author	: GAT-BLAC
 * Date		: 13/01/2017
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteNotificacionesCtrl',
	function($scope,$translate,$resource,$stateParams,bcsComunes,$uibModal){
		var pCliNot = this;
		pCliNot.datos = $scope.admSoc.dataSocioNegocio;

		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Notificaciones') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClienteNotificaciones' });
			} 
		});

		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Notificaciones') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Cliente", nombreTab: 'PanelClienteNotificaciones' });
			}
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------