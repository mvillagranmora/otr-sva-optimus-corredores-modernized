angular.module('ModuloFFM').directive('bcsLinkMetodo', function(bcsComunes) {
	return {
		restrict: 'E',
		scope: {
			bcsClic  : '&?',
		},
		template : '<i class="fa fa-arrow-right" aria-hidden="true" ng-click="btnClic()"></i>',
        link: function(scope, element, attrs, ctrls) {
        	if (scope.bcsClic === undefined) {
				console.warn("funcion bcs-clic no definida");
			}
			scope.btnClic = function (e) {
				if(sesion.getAccess($(element).attr("id"))){
					if (scope.bcsClic) scope.bcsClic();
				}else{
					bcsComunes.msg("error",'Error',$translate.instant("UI_NO_TIENE_ACCESO"));
				}
			};
        }
    }
});