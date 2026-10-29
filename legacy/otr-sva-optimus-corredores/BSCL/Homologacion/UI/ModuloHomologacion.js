angular.module('ModuloHomologacion', ['ui.bootstrap','ui.utils','ui.router','ngAnimate']);

angular.module('ModuloHomologacion').factory('bcsServiciosHomologacion',['$resource',function($resource) {	
	var obj = {};
	obj.IngresarTitulo = function(datos,success_fn,error_fn){
		var urlEjecutarCargaMasiva= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3";
		var r = $resource("", {},{
			'EjecutarCargaMasiva': { method: "POST", url: urlEjecutarCargaMasiva}
		});
		var post = r.IngresarTitulo(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	return obj;
}]);


