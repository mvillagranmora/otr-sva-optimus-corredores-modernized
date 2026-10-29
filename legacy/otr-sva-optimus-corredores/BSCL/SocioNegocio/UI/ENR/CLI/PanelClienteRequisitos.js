/*************************************************************
 * Purpose	: Panel Cliente Requisitos, utiliza directiva
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	(function() {
	  angular.module('ModuloSocioNegocio').controller('PanelClienteRequisitosEnrolCtrl',
	  function($scope){
	    var me = this;
	    me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;

		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Requisitos') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClienteRequisitos' });
			} 
		});

		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Requisitos') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Cliente", nombreTab: 'PanelClienteRequisitos' });
			}
		});
	  });
	}());
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------