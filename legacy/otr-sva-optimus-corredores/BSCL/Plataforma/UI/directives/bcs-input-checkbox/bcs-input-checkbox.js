angular.module('ModuloPlataforma').directive('bcsInputCheckbox', function($timeout) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			ngModel : '=?',
			ngDisabled : '=?'
		},
		templateUrl: 'directives/bcs-input-checkbox/bcs-input-checkbox.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.ngModel;
			}
		},
		compile: function(tElem, tAttrs) {
            var expresionExterna = tAttrs.ngShow || tElem.attr('ng-show');
            tElem.removeAttr('ng-show');

            return function link(scope, elem, attrs) {
                scope.tieneAcceso = true;
                scope.expresionExternaVisible = true;

                scope.mostrar = function() {
                    return scope.tieneAcceso && scope.expresionExternaVisible;
                };

                if (expresionExterna) {
                    scope.$parent.$watch(expresionExterna, function(nuevo, anterior) {
                        scope.expresionExternaVisible = !!nuevo;
                    });
                }

                $timeout(function() {
                    var id = elem.attr('id');
                    if (id) {
                        scope.tieneAcceso = !!sesion.getAccess(id);
                    }
                });
            };
        }
  };
});
