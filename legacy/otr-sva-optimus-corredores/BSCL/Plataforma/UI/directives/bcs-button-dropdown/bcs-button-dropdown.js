angular.module('ModuloPlataforma').directive('bcsButtonDropdown', function(bcsComunes,$translate) {
	return {
		restrict: 'E',
		transclude: true,
		replace: false,
		scope: {
			bcsHabilitado : '=?',
			opciones		: '=?',
			bcsTipo			: '=?'
		},
		templateUrl: 'directives/bcs-button-dropdown/bcs-button-dropdown.html',
		link: function(scope, elem, attrs,controller,transcludeFn) {
			scope.textoPrincipal = scope.opciones[0].titulo;
			scope.funcionPrincipal = scope.opciones[0].funcion;
			elem.find("ul").click(function(e){
				scope.textoPrincipal = angular.element(e.target).scope().opcion.titulo;
				scope.funcionPrincipal =  angular.element(e.target).scope().opcion.funcion;
			})
			
		}
  };
});
