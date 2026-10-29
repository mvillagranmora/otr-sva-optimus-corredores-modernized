angular.module('ModuloPlataforma').directive('bcsInputPassword', function() {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			ngModel : '=?',
			ngDisabled : '=?kNgDisabled',
			atributos: '@?'
				
		},
		templateUrl: 'directives/bcs-input-password/bcs-input-password.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.ngModel;
			}
		},
		link: function(scope, element, attrs, fn) {
				if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
				if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;
		}
	};
});
