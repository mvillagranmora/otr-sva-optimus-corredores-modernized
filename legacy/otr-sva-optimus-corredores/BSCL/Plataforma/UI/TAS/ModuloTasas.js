(function() {
	angular.module('ModuloPlataforma').factory('bcsServiciosTasas',['$resource',function($resource) {
		
		var urlGenerarArchivoTasas	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=601";
		var urlSolicitudArchivoTasas= "/BSCL/Plataforma/GWJAVA/PLA/call?t=60068";
		var urlCrearTasa			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=607";
		var urlModificarTasa		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=608";
		var urlModificarEstadoTasas	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=609";
		var urlCargarArchivoTasas	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=60067";
		var urlConsultarPrecioTasa	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=631";
		
		var urlCrearCriterioSeleccionTasas		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=614";
		var urlModificarCriterioSeleccionTasas	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=615";
		
		var urlCrearMargenTasa		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=619";
		var urlModificarMargenTasa	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=620";
		var urlObtenerNemoSVS	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=600085";
		
		return $resource("", {},{
			'generarArchivoTasas'	: { method: "POST",	url: urlGenerarArchivoTasas},
			'solicitudArchivoTasas'	: { method: "POST",	url: urlSolicitudArchivoTasas},
			'crearTasa'				: { method: "POST",	url: urlCrearTasa},
			'modificarTasa'			: { method: "POST",	url: urlModificarTasa},
			'modificarEstadoTasas'	: { method: "POST",	url: urlModificarEstadoTasas},
			'cargarArchivoTasas'	: { method: "POST",	url: urlCargarArchivoTasas},
			'consultarPrecioTasa'	: { method: "POST",	url: urlConsultarPrecioTasa},
			
			'crearCriterioSeleccionTasas'		: { method: "POST",	url: urlCrearCriterioSeleccionTasas},
			'modificarCriterioSeleccionTasas'	: { method: "POST",	url: urlModificarCriterioSeleccionTasas},
			
			'crearMargenTasa'		: { method: "POST",	url: urlCrearMargenTasa},
			'modificarMargenTasa'	: { method: "POST",	url: urlModificarMargenTasa},
			'ObtenerNemoSVS'	: { method: "POST",	url: urlObtenerNemoSVS},
		});
	}]);
}());