angular.module('ModuloContabilidad', []);
 
angular.module('ModuloContabilidad').factory('bcsServicioContabilidad',['$resource',function($resource) {
	var generarResource = function() {
		var r = {};
		$.each(resources,function(key,value) {
			r[value.nombre] = { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+value.id  };
			
		});
		return r;
	};		                 
	
	var resources = [
        { nombre : 'LeerData', id : "1405" },
        { nombre : 'AgregarMetadata', id: '4233'},
        { nombre : 'EliminarMetadata', id: '4234'},
        { nombre : 'FiltrarCuentas', id:'4235'},
        { nombre : 'AgregarCuentasPorMetadata', id:'4236'},
        { nombre : 'EditarMetadata', id: '4245'},
        { nombre : 'CargarCabeceraComprobante', id: '4249'},
        { nombre : 'CargarDeterminanteComprobante', id: '4250'},
        { nombre : 'CargarAsientoComprobante', id: '4256'},
		{ nombre : 'EditarCuentaBasilea', id: '701156'}
	];
				
	return $resource("", {},generarResource());			
}]);

angular.module('ModuloContabilidad').factory('bcsConceptosNormativos', function () {
	var consultas = {
		ConfiguracionConceptosNormativos: 15486541,
		ConsultarConceptosNormativos: 15486540,
	}
	return function (nombreConsulta) {
		return "/BSCL/Plataforma/GWJAVA/PLA/call?t=" + consultas[nombreConsulta];
	};
});
