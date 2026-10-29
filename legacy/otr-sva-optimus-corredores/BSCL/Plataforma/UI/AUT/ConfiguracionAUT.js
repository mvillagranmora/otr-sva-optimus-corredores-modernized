angular.module('ModuloPlataforma').factory('bcsServiciosAUT',function($resource) {
	
	const urlModificarEstadoEventoAutorizacion	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=41";  
	const urlCrearEtapaAutorizacion 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=49";
	const urlCrearAutorizadorEtapa 				= "/BSCL/Plataforma/GWJAVA/PLA/call?t=50";	  
	const urlModificarEtapa						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1704";
	const urlModificarAutorizadorEtapa 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=1705";
	const urlCambiarEstadoEventoMantenedor 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290888";
	const urlVencerEventoAutorizacion			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290891";


	return $resource("", {},
	{
		'ModificarEstadoEventoAutorizacion' 	: { method: "POST", url: urlModificarEstadoEventoAutorizacion},
		'CrearEtapaAutorizacion'				: { method: "POST", url: urlCrearEtapaAutorizacion},
		'CrearAutorizadorEtapa'					: { method: "POST", url: urlCrearAutorizadorEtapa},
		'ModificarEtapa'						: { method: "POST", url: urlModificarEtapa},
		'ModificarAutorizadorEtapa'				: { method: "POST", url: urlModificarAutorizadorEtapa},
		'CambiarEstadoEventoMantenedor'			: { method: "POST", url: urlCambiarEstadoEventoMantenedor},
		'VencerEventoAutorizacion'				: { method: "POST", url: urlVencerEventoAutorizacion}
	});
});