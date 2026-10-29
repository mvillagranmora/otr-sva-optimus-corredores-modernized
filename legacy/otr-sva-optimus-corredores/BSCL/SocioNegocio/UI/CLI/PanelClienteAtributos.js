/*************************************************************
 * Purpose	: Panel Cliente Atributos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteAtributosCtrl',
	function($scope,$translate,servicioSociodeNegocio,$stateParams,bcsComunes ){
		var me = this;
		me.test = 'PanelClienteAtributosCtrlCtrl';
		me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;
		var idVentana = bcsComunes.ventanaWinId($scope);
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
		bcsComunes.ventanaAutoHeight(idVentana);	
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
