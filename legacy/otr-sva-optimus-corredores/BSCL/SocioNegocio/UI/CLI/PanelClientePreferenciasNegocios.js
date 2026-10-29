/*************************************************************
 * Purpose	: Panel Cliente Preferencias Negocios.
 * Author	: TEAM CELULA ZORRO - BCS
 * Date		: 29/05/2019 [Norm.]
 * 
 * Purpose	: Registrar preferencia de negocios respecto del ID de Portafolio que se asignara a prestamos provenientes de Block Chain 
 * Author	: Juan Leiva
 * Date		: 29/05/2019
 *************************************************************/
(function() { angular.module('ModuloSocioNegocio').controller('PanelClientePreferenciasNegociosCtrl',
		function($scope,$translate,servicioSociodeNegocio,$stateParams){
			var me = this;
			//me.test = 'PanelClientePreferenciasNegociosCtrl';  
			//me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;
			me.IdCliente = $scope.admSoc.dataSocioNegocio.IdCliente;
			me.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;

			$scope.admSoc.socioNegocioPromise.then(function () {
				me.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['PREFERENCIAS_NEGOCIOS_CLIENTE'];
			});
			
			$scope.$on('CambiosOk', function (evt, data) {
				if (data.source === 'Preferencias') {
					$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClientePreferenciasNegocios' });
				} 
			});

			$scope.$on('CambiosPendientes', function (evt, data) {
				if (data.source === 'Preferencias') {
					$scope.$emit('HayDatosSinGuardar', { ambito: "Cliente", nombreTab: 'PanelClientePreferenciasNegocios' });
				}
			});
		});
 }());
			//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
