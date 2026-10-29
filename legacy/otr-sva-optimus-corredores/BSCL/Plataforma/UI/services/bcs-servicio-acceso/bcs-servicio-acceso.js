angular.module('ModuloPlataforma').factory('bcsServicioAcceso',['$resource',function($resource) {
	
	var obj = new Object();
	obj.getAccesos = function(perfil, formulario) {
		var resource = $resource('/BSCL/Plataforma/XSREST/Acceso.xsjs?perfil='+ perfil +'&formulario=' + formulario).get();
		return resource.$promise;
	  };

	  return obj;

}]);
