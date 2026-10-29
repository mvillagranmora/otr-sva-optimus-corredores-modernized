angular.module('ModuloPlataforma').factory('bcsServiciosPlataformaComercialPeru',function($resource) {
	var urlCrearPerfilCobros 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3545";
	var urlModificarPerfilCobros 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=3546";
	var urlCrearDetallePerfilCobros 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20012";
	var urlEliminarDetallePerfilCobros 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20013";
	var urlEditarDefinicionCobro 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=20072";
	var urlCopiarPerfilCobros			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600074";
	
	return $resource("", {},
	{
		'crearPerfilCobros'					: { method: "POST",	url: urlCrearPerfilCobros},
		'modificarPerfilCobros'				: { method: "POST",	url: urlModificarPerfilCobros},
		'crearDetallePerfilCobros'			: { method: "POST",	url: urlCrearDetallePerfilCobros},
		'eliminarDetallePerfilCobros'		: { method: "POST",	url: urlEliminarDetallePerfilCobros},
		'editarDefinicionCobros'			: { method: "POST",	url: urlEditarDefinicionCobro},
		'copiarPerfilCobros'				: { method: "POST", url: urlCopiarPerfilCobros},
  	 });
});

angular.module('ModuloPlataforma').factory('servicioPerfilComercial',function($resource) {
	var urlSavePerfilComercial = "/BSCL/Plataforma/GWJAVA/PLA/call?t=46";
	var urlUpdatePerfilComercial = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=253";
	var urlGetMatrizOperacional = "/BSCL/Plataforma/GWJAVA/PLA/call?t=305";
	var urlQuitarDetallePerfilCobros = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20050";
	
	return $resource("", {},
	{
	  'savePerfilComercial'			:{method: "POST",url: urlSavePerfilComercial},
	  'updatePerfilComercial'		:{method: "POST",url: urlUpdatePerfilComercial},
	  'quitarDetallePerfilCobros'	:{method: "POST",url: urlQuitarDetallePerfilCobros},
	  'getMatrizOperacional'		:{method: "POST",url: urlGetMatrizOperacional}
	});

});

angular.module('ModuloPlataforma').factory('servicioContratoComision',function($resource) {
	const urlCrearContratoComision 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700279";
	const urlModificarContratoComision 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700281";
	const urlCrearDetalleContratoComision 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700282";
	const urlModificarDetalleContratoComision 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700287";
	const urlEliminarDetalleContratoComision 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010265";
	
	return $resource("", {},
	{
	  'crearContratoComision'				:{method: "POST", url: urlCrearContratoComision},
	  'modificarContratoComision'			:{method: "POST", url: urlModificarContratoComision},
	  'crearDetalleContratoComision' 		:{method: "POST", url: urlCrearDetalleContratoComision},
	  'modificarDetalleContratoComision' 	:{method: "POST", url: urlModificarDetalleContratoComision},
	  'eliminarDetalleContratoComision'		:{method: "POST", url: urlEliminarDetalleContratoComision},
	});

});