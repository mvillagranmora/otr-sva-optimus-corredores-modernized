(function() {
	angular.module('ModuloPlataforma').factory('servicioLimites', ['$resource', function($resource) {
		
		const urlCrearLimiteCtaPropia		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=316";
		const urlModificarLimiteCtaPropia	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=318";
		const urlCrearLimiteEmisor		    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=319";
		const urlModificarLimiteEmisor	    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=320";
		const urlCrearLimiteRangos		    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=335";
		const urlModificarLimiteRangos	    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=336";
		const urlCopiarLimiteCtaPropia	    = "/BSCL/Plataforma/GWJAVA/PLA/call?t=351";
		
		const urlCrearGrupoEmisor			 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=416";
		const urlModificarGrupoEmisor		 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=417";
		const urlCrearGrupoEmisorDetalle	 = "/BSCL/Plataforma/GWJAVA/PLA/call?t=418";
		const urlModificarGrupoEmisorDetalle = "/BSCL/Plataforma/GWJAVA/PLA/call?t=419";
		
		return $resource("", {}, {
			'crearLimiteCtaPropia'		: { method: "POST",	url: urlCrearLimiteCtaPropia},
			'modificarLimiteCtaPropia'	: { method: "POST",	url: urlModificarLimiteCtaPropia},
			'crearLimiteEmisor'			: { method: "POST",	url: urlCrearLimiteEmisor},
			'modificarLimiteEmisor'		: { method: "POST",	url: urlModificarLimiteEmisor},
			'crearLimiteRangos'			: { method: "POST",	url: urlCrearLimiteRangos},
			'modificarLimiteRangos'		: { method: "POST",	url: urlModificarLimiteRangos},
			'copiarLimiteCtaPropia'		: { method: "POST",	url: urlCopiarLimiteCtaPropia},
			'crearGrupoEmisor'			: { method: "POST",	url: urlCrearGrupoEmisor},
			'modificarGrupoEmisor'		: { method: "POST",	url: urlModificarGrupoEmisor},
			'crearGrupoEmisorDetalle'	: { method: "POST",	url: urlCrearGrupoEmisorDetalle},
			'modificarGrupoEmisorDetalle':{ method: "POST",	url: urlModificarGrupoEmisorDetalle}
		});
	}]);
}());


angular.module('ModuloPlataforma').factory('bcsConfiguracionLimite',function($q) {
	const obj = {};
	
	obj.getSchemaSolicitudDeCambio = function() {
		return {
			model: {
				fields : {
					IdSolicitudCambio		: { type: "number"},
					IdSecuenciaCambio		: { type: "number"},
					IdTipoEntidad			: { type: "string"},
					IdEntidadOrigen			: { type: "string"},
					CampoModificado			: { type: "string"},
					TipoDato				: { type: "string"},
					ValorAntiguo			: { type: "string"},
					ValorNuevo				: { type: "string"},
					FechaSolicitud			: { type: "string"},
					FechaRevision			: { type: "string"},
					IdEstadoAutorizacion	: { type: "string"}
				}
			}
		};
	 };
	 
	obj.getSchemaLimite = function(){
		return {
			schema: {
				model: {
					fields: {
						IdPlantillaLimite	: { type: "string"},
						IdLimite			: { type: "number"},
						IdTipoInstrumento	: { type: "string"},
						IdTipoOperacion   	: { type: "string"},
						IdCanalIngreso     	: { type: "string"},
						CantidadEmisores	: { type: "number"},
						IdClasificacionRiesgo:{ type: "number"},
						IdEstado			: { type: "string"},
						IdEmpresa			: { type: "number"},
						IdUsuarioCreador	: { type: "number"}
					}
				}
			}
		};
	};

	obj.obtenerTipoValidacionPorDefectoPromise = function (idEmpresa) {
		const dsParametrosSistema = new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering: true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
						dataType: "json"
					}
				},
				schema: {
					model: {}
				},
				filter: [
					{ field: "CodigoParametro", operator: "eq", value: 'LIM.TipoValidacionPorDefecto' },
					{ field: "IdEmpresa", operator: "eq", value: idEmpresa },
					{ field: "Modulo", operator: "eq", value: "LIMITE" },
				],
			},
		});

		const deferred = $q.defer();
		try {
			dsParametrosSistema.fetch(function () {
				deferred.resolve(dsParametrosSistema.data()[0].Valor || '');
			});
		} catch (e) {
			console.error('tipoValidacionPorDefectoPromise', e);
			deferred.reject(e);
		}
		
		return deferred.promise;
	};
	
	obj.mapIf = function mapIf(current, unwated, replacement) {
		return current === unwated || !current ? replacement : current;
	}
	
	obj.CodigoNoAplicaString = '_NA';
	obj.CodigoNoAplicaNumber = -1;
	
	return obj;
});