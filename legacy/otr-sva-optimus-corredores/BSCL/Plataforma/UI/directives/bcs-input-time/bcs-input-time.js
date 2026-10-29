angular.module('ModuloPlataforma').directive('bcsInputTime', function() {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			kOptions: '=?',
			ngModel : '=?',
			kNgModel : '=?',
			kendoWidget : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?'
		},
		templateUrl: 'directives/bcs-input-time/bcs-input-time.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return $scope.kNgModel;
			}
		},
		link: function(scope, element, attrs, fn) {
			if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
			if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;						
			scope.kOptions = scope.kOptions || {};			
			scope.$on("kendoWidgetCreated", function (event, widget) {
				if (widget.$angular_scope === scope) {
					scope.kendoWidget = widget;
					scope.kendoWidget.element.on('keypress', function(e) {
						if (e.keyCode === 13) {
          					e.preventDefault();
        				}							
					});					
				}
			});
			
		}
	};
});
