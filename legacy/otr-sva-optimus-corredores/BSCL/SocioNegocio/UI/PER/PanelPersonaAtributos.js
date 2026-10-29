/*************************************************************
 * Purpose	: Panel Persona Atributos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaAtributosCtrl',
	function($scope,$translate,servicioSociodeNegocio,$stateParams,bcsComunes){
	  var me = this;
		me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;
		var idVentana = bcsComunes.ventanaWinId($scope);
		$scope.admSoc.socioNegocioPromise && $scope.admSoc.socioNegocioPromise.then(function () {
			me.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['INDICADORES_PERSONA'];
		});

		$scope.$on('CambiosOk', function (evt, data) {
			if (data.source === 'Indicadores') {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaAtributos' });
			}
		});

		$scope.$on('CambiosPendientes', function (evt, data) {
			if (data.source === 'Indicadores') {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaAtributos' });
			}
		});
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------