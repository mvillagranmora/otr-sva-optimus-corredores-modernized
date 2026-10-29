angular.module('ModuloFFM').directive('bcsComboboxBuscadorFondos', function(bcsComunes,$resource,$rootScope,bcsConfiguracionFFM,$timeout, bcsServicioFondosMutuos) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			muestraValorCuota : '=?',
			muestraDetalle : '=?',
			masInformacion : '=?',
			esDetalle : '=?',
			datosFondo : '=?',
			buscarFondo : '=?',
			buscarIdFondo : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			idVentana : '=?',
			nemotecnico: '=?',
			idDefecto: '=?', 
			filtros : '=?',
			filtrosCombobox : '=?',
			bcsRequerido: '=?'
		},
		templateUrl: '../../FondoMutuo/UI/bcs-combobox-buscador-fondos/bcs-combobox-buscador-fondos.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return ($scope.datosFondo && $scope.datosFondo.IdFondo);
			}
		},		
		link: function(scope, element, attrs, fn) {
			scope.pensando = false;
			scope.$watch('datosFondo',function(newval,oldval){
				if(newval && newval.IdSerieFondo){
					scope.nemotecnico = scope.datosFondo.IdSerieFondo;
					
					var entrada = {
							p_IdEmpresa : $rootScope.SesionIdEmpresa, 
							p_IdUsuario : $rootScope.SesionIdUsuario, 
							p_IdModulo : 'FMM',
							p_IdSerieFondo : scope.datosFondo.IdSerieFondo
					}
					
					bcsServicioFondosMutuos.ConsultarFondo(entrada).$promise.then(function(respuesta){
						scope.datosFondo.ValorCuota = respuesta.P_OUT_RESULTADO[0].ValorCuota;
						newval.ValorFormato = util.truncarNumero(scope.datosFondo.ValorCuota, newval.DecimalesValorCuota);
					},function(respuesta){
	
					});
				}
			})
			
			scope.$watch('buscarFondo',function(newval,oldval) {
				if (newval === undefined || newval === "") return;
				if (newval === "-1") {
					scope.datosFondo = undefined;
					scope.buscarFondo = "";
					scope.nemotecnico = "";
					return;
				}
				scope.buscarFondo = "";
			})
			
			scope.fnBusquedaFondo = function() {
				if (scope.kNgDisabled || scope.kNgReadonly) return;
				
				var entrada = {
						p_IdEmpresa : $rootScope.SesionIdEmpresa, 
						p_IdUsuario : $rootScope.SesionIdUsuario, 
						p_IdModulo : 'FMM',
						p_IdSerieFondo : scope.datosFondo.IdSerieFondo
				}
				
				var ValorCuota = 0;
				
				bcsServicioFondosMutuos.ConsultarFondo(entrada).$promise.then(function(respuesta){
					scope.datosFondo.ValorCuota = respuesta.P_OUT_RESULTADO[0].ValorCuota;
					ValorCuota = respuesta.P_OUT_RESULTADO[0].ValorCuota;
				},function(respuesta){
					
				});
				
				var id1 = bcsComunes.ventanaAbrir('busquedaFondos',{
					filtros     			: scope.filtros
				},false,false,scope.idVentana);
				bcsComunes.ventanaPromesa(id1).then(function(data){
					if (data && data.IdFondo) {
						scope.datosFondo = $.extend({},data);
						scope.nemotecnico = undefined;
					
						scope.datosFondo.ValorFormato = util.truncarNumero(ValorCuota, scope.datosFondo.DecimalesValorCuota);
					}
				});
				
			};
		}
	};
});
