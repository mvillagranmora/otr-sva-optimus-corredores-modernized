/*************************************************************
 * Purpose	: Panel Persona Atributos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaAtributosEnrolCtrl',
	function($scope){
	  var me = this;
		me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;

		$scope.admSoc.socioNegocioPromise && $scope.admSoc.socioNegocioPromise.then(function () {
			me.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['INDICADORES_PERSONA'];
		});

		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'IndicadoresPersona') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaAtributos' });
			} 
		});

		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'IndicadoresPersona') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaAtributos' });
			}
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------