/*************************************************************
 * Purpose	: Panel Cliente Atributos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteAtributosEnrolCtrl',
	function($scope){
		const me = this;
		me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;

		$scope.admSoc.socioNegocioPromise && $scope.admSoc.socioNegocioPromise.then(function () {
			me.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['INDICADORES_CLIENTE'];
		});
		
		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Indicadores') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClienteAtributos' });
			} 
		});

		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Indicadores') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Cliente", nombreTab: 'PanelClienteAtributos' });
			}
		});
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
