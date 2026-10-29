angular.module('ModuloFFM').directive('bcsInputFondos', function(bcsComunes,$resource,$rootScope,bcsConfiguracionFFM) {
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
			filtros : '=?',
		},
		templateUrl: '../../FondoMutuo/UI/bcs-input-fondos/bcs-input-fondos.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return ($scope.datosFondo && $scope.datosFondo.IdFondo);
			}
		},		
		link: function(scope, element, attrs, fn) {
			scope.pensando = false;
			
			scope.blur = function(event) {
				buscarFondoPorNemo();
            };
			element.bind('keypress', function (event) {
                    if(event.keyCode == 13 && !event.shiftKey){
                        event.preventDefault();
                        buscarFondoPorNemo();
                    }
                });
			scope.$watch('buscarFondo',function(newval,oldval) {
				if (newval === undefined || newval === "") return;
				if (newval === "-1") {
					scope.datosFondo = undefined;
					scope.buscarFondo = "";
					scope.nemotecnico = "";
					return;
				}
				scope.nemotecnico = newval;
				scope.buscarFondo = "";
				buscarFondos(newval);
			});
			scope.$watch('buscarIdFondo',function(newval,oldval) {
				if (!newval) return;
				scope.buscarIdFondo = "";
				buscarFondoPorId(newval);
			});
			scope.fnBusquedaFondo = function() {
				if (scope.kNgDisabled || scope.kNgReadonly) return;
				var id1 = bcsComunes.ventanaAbrir('busquedaFondos',{
					filtros     			: scope.filtros,
				},false,false,scope.idVentana);
				bcsComunes.ventanaPromesa(id1).then(function(data){
					if (data && data.IdFondo) {
						scope.datosFondo = $.extend({},data);
						scope.nemotecnico = scope.datosFondo.IdSerieFondo;
						scope.datosFondo.ValorFormato = util.truncarNumero(scope.datosFondo.ValorCuota, scope.datosFondo.DecimalesValorCuota);
						console.log(data);
					}
				});
				
			};
			
			var buscarFondoPorId = function(idFondo) {
				buscarFondo(undefined,idFondo);
			};
			var buscarFondoPorNemo = function() {
				if (!scope.nemotecnico || scope.nemotecnico.length < 2) {
					scope.datosFondo = undefined;
					return;
				}
				scope.nemotecnico = (scope.nemotecnico || '').toUpperCase();
				buscarFondos(scope.nemotecnico);
			};
			var buscarFondos = function(nemo,idFondo) {
				
				var ds = bcsConfiguracionFFM.getDataSourceFondo();
				var filters = [  ];
				scope.pensando = true;

				ds.query({ filter: filters	}).then(function(){
					scope.pensando = false;
					var data = ds.data();
					var found = false;
					$.each(data,function(key,value) {
						if (value.IdSerieFondo == nemo || value.IdFondo == idFondo) {
							scope.datosFondo = value;
							scope.datosFondo.ValorFormato = util.formateaNumero(scope.datosFondo.ValorCuota, scope.datosFondo.DecimalesValorCuota);
							scope.nemotecnico = value.IdSerieFondo;
							found = true;
						}
					});
					if (!found) scope.datosFondo = undefined;
				},function(result){
					
				});
			}
		}
	};
});
