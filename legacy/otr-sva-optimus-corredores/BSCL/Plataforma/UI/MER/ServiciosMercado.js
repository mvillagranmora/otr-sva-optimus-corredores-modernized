angular.module('ModuloPlataforma').factory('bcsServiciosPlataformaMercado',['$resource',function($resource) {
		
	var obj = {};
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t="+value.id};
			
		});
		return r;
	};
	
	var resources = [
        { nombre : 'CalcularParidadYTipoCambio', id : "3155" },
        
	];
	resources = $resource("", {},generarResource());
	
	
	//funciones
	obj.CalcularParidadYTipoCambio = function(entrada) {
		
		return resources.CalcularParidadYTipoCambio(entrada).$promise;
	}
	
	return obj;
}]);
