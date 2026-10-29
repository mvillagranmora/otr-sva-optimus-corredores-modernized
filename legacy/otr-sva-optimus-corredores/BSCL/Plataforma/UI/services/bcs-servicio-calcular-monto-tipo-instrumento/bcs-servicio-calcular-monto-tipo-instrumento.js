angular.module('ModuloPlataforma').factory('bcsServicioCalcularMontoTipoInstrumento',['$resource',function($resource) {
	var obj = {};
	
	obj.llamar = function(datos,success_fn,error_fn)
	{
		var urlCalcularMontoPorTipoInstrumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1729";
		var r = $resource("", {},
	    {
			'CalcularMontoPorTipoInstrumento': { method: "POST", url: urlCalcularMontoPorTipoInstrumento},
		});
		var post = r.CalcularMontoPorTipoInstrumento(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	/*
	obj.llamar2 = function(datos)
	{
		var urlCalcularMontoPorTipoInstrumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=BSCL.Plataforma.BD.Servicios%3A%3ACalcularMontoPorTipoInstrumento";
		var r =  $resource("", {},
	    {
			'CalcularMontoPorTipoInstrumento': { method: "POST", url: urlCalcularMontoPorTipoInstrumento},
		});
		obj = r.CalcularMontoPorTipoInstrumento(datos);
		

	};
	*/
	return obj;

}]);
