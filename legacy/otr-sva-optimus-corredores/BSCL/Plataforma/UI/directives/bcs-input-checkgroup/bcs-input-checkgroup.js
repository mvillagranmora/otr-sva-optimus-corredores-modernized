angular.module('ModuloPlataforma').directive('bcsInputCheckgroup', function() {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			activado 		: '=?',
			etiquetas 		: '=', //tiene etiqueta y codigo 
			valores			: '=', //tiene valor y codigo
			modo 			: '@', //M,U
			orientacion		: '@'

		},
		templateUrl: 'directives/bcs-input-checkgroup/bcs-input-checkgroup.html',
		controller : function($scope){
			this.getSeleccionado = function(){
				return _.countBy($scope.valores, function(obj) {
					  return obj === true? true: false;
				})[true];
			}
		},
		link: function(scope, element, attrs, fn) {
			if (!scope.modo) scope.modo = "U";
			if (!scope.orientacion) scope.orientacion = 'H';
			if (scope.activado === undefined) scope.activado = true;
			if (scope.modo === "U") {
				scope.$watch('valores',function(newval,oldval){
					//buscamos cual cambio
					var keyChanged = undefined;
					$.each(newval,function(key,val){
						if (newval[key] === true && oldval[key] === false) { 
							keyChanged = key;
						}
					})
					if (keyChanged) {
						$.each(scope.valores,function(key,val){
							if (key !== keyChanged) {
								scope.valores[key] = false;
							}
						})
					}
					/*if (newval === false && oldval === true) {
						var found = false;
						$.each(scope.datos,function(key2,val2){
							if (val2[scope.valor] === true) {
								found = true;
								return false;
							}
						});
						if (!found)
							newval[scope.valor] = true;
					}*/
				},true)
			}
		}
  };
});
