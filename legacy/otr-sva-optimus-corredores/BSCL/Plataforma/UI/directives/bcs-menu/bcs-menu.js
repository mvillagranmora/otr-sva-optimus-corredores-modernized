angular.module('ModuloPlataforma').directive('bcsMenu', function() {
	return {
		restrict: 'E',
		transclude: true,
		replace: false,
		scope: {
			bcsTexto      : '=?',				 /*Texto a mostrar en el boton */
			bcsHabilitado : '=?', 
			bcsTipo		  :	'=?'			 /* activa/desactiva  el boton  */
		},
		template: '<div><bcs-button id=""  bcs-tipo="bcsTipo"  bcs-auto-desactivar="false" bcs-texto="bcsTexto" bcs-habilitado="bcsHabilitado"   ></bcs-button>'+
		 		  '<ul kendo-context-menu="menu"  target="target" show-on="\'click\'" align-to-anchor="\'true\'" ng-transclude></ul></div>',
		link: function(scope, elem, attrs,controller,transcludeFn) {
			var vid=new Date().getTime();
			elem.first().attr('id',vid);
			scope.target="#"+vid;
			if (typeof scope.bcsHabilitado === 'undefined') scope.bcsHabilitado=false;
			/*scope.btnClic = function (e) {
				if (scope.bcsAutoDesactivar) scope.bcsHabilitado=false;
				console.log('evento',e);
				scope.bcsClic(e);
			};*/
		}
  };
});
