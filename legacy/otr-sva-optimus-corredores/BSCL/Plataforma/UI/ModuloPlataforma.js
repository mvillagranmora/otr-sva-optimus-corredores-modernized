angular.module('ModuloPlataforma', []);
angular.module('ModuloCorredor', []);

angular.module('ModuloPlataforma').factory('bcsServicioPlataforma',['$resource', '$q', function($resource, $q) {
	var obj = {};
	
	var urlBase = "/BSCL/Plataforma/GWJAVA/PLA/call?t=";
	//limpia cache en ambientes no productivos
	if (BCS_AMBIENTE != 'PRO')
		urlBase = "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t="; 
	
	obj.Servicios = function() {
		return $resource("",{},{
			'CalcularExigibilidad'							: { method: "POST", url: urlBase + 30},
			'CalcularCobertura'								: { method: "POST", url: urlBase + 31},	
			'CrearInstrumento'								: { method: "POST", url: urlBase + 1509},
			'ModificarInstrumento'							: { method: "POST", url: urlBase + 1510},
			'CrearParidadYTipoCambio'						: { method: "POST", url: urlBase + 1514},
			'ModificarParidadYTipoCambio'					: { method: "POST", url: urlBase + 1515},
			'ConsultaFlujoCaja'								: { method: "POST", url: urlBase + 1673},
			'CrearConceptoManual'							: { method: "POST", url: urlBase + 1787},
			'EditarConceptoManual'							: { method: "POST", url: urlBase + 1791},
			'GuardarBono'		         					: { method: "POST", url: urlBase + 4147},
			'GuardarCupones'		        				: { method: "POST", url: urlBase + 4148},
			'ModificarTasaGenerica' 						: { method: "POST", url: urlBase + 3588},
			'CrearTasaGenerica' 							: { method: "POST", url: urlBase + 3590},
			'AbrirModulo' 									: { method: "POST", url: urlBase + 4180},
			'CerrarModulo' 									: { method: "POST", url: urlBase + 4181},
			'EntregarSistema' 								: { method: "POST", url: urlBase + 20048},
			'InsertarProcesoBatch' 							: { method: "POST", url: urlBase + 20043},
			'BarraProgresoServicio' 						: { method: "POST", url: urlBase + 702167},
			'ConsultarDiaSiguiente'							: { method: "POST", url: urlBase + 23},		//DEPRECATED
			'ConsultarDiaSiguienteMasivo'					: { method: "POST", url: urlBase + 13053}, 	//DEPRECATED
			'MonedasPorProductoFeriadoMonPrinc'             : { method: "POST", url: urlBase + 398}, 
			'ObtenerDiaSiguienteMultiplesPaises'			: { method: "POST", url: urlBase + 44290894},
			'ObtenerDiaSiguiente'							: { method: "POST", url: urlBase + 21030},
			'MantenedorParametrosEditar'					: { method: "POST", url: urlBase + 20058},
			'CrearModificarEmisor'							: { method: "POST", url: urlBase + 100013},
			'ConsultarMapaRelaciones'						: { method: "POST", url: urlBase + 20070},
			'InscribirProcesoBatch'         				: { method: "POST", url: urlBase + 20043},
			'CargaEntidadVectorPrecio'      				: { method: "POST", url: urlBase + 100037},
			'CargaEntidadVectorPrecio_MER'  				: { method: "POST", url: urlBase + 601001},
			'CargaImagenesFirmas' 			 				: { method: "POST", url: urlBase + 700177},
			'ImprimirContratos'								: { method: "POST", url: urlBase + 70130},
			'ModificaParametroFinanciero'   				: { method: "POST", url: urlBase + 4258},
			'CrearParametroFinanciero'   					: { method: "POST", url: urlBase + 4259},
			'CrearEventoAutorizacion'   					: { method: "POST", url: urlBase + 130},
			'ConsultarParametroFinanciero'					: { method: "POST", url: urlBase + 600066},
			'AdministrarParametroFinanciero'				: { method: "POST", url: urlBase + 600067},
			'AgregarUsoParidadParametroFinanciero'  		: { method: "POST", url: urlBase + 600071},
			'QuitarUsoParidadParametroFinanciero'			: { method: "POST", url: urlBase + 600072},
			'CambiarPrioridadUsoParidadParametroFinanciero' : { method: "POST", url: urlBase + 600073},
			'ConsultaIndicesCorredora'      				: { method: "POST", url: urlBase + 700005},
			'AdministrarIndices'							: { method: "POST", url: urlBase + 700007},
			'AdministraConfiguracionPatrimonioCustodia'		: { method: "POST", url: urlBase + 601214},
			'AdministraConfiguracionPatrimonioCaja' 		: { method: "POST", url: urlBase + 601215},
			'Conciliacion' 					                : { method: "POST", url: urlBase + 700164}, 
			'CambiarEstadoNotificacion'		                : { method: "POST", url: urlBase + 4290624}, 
			'CrearCampoFormulario'		                	: { method: "POST", url: urlBase + 700201},
			'EditarCampoFormulario'		                	: { method: "POST", url: urlBase + 700202},
			'EliminarCampoFormulario'		                : { method: "POST", url: urlBase + 700203},
			'FirmarDocumento'                               : { method: "POST", url: urlBase + 700317},
			'GestionarFirmasDocumento'						: { method: "POST", url: urlBase + 700327},
			'ModificarFirmaDocumento'						: { method: "POST", url: urlBase + 19486544},
			'ConsultarConceptosSinValidacionFlujo'      	: { method: "POST", url: urlBase + 700632},
			'ConsultarConceptoAbonoClpContrapartida'		: { method: "POST", url: urlBase + 21045},
			'ConsultarConfiguracionUsuario'					: { method: "POST", url: urlBase + 438 },
			'RegistrarUsoVentanaOptimus'					: { method: "POST", url: urlBase + 700729},
			'ReaperturarTraspaso'							: { method: "POST", url: urlBase + 6200 },
			'ConsultarTipoProgramaEnProceso'				: { method: "POST", url: urlBase + 700428 },
			'ConsultarInformeLimitesCorredora'				: { method: "POST", url: urlBase + 60069 },
			'CrearDeterioroEmisor'							: { method: "POST", url: urlBase + 55669901 },
			'ModificarDeterioroEmisor'						: { method: "POST", url: urlBase + 55669902 },
			'CargarArchivoDeterioroEmisor'					: { method: "POST", url: urlBase + 55669903 },
			'ConsultarLogValorizadorPauta' 					: { method: "POST", url: urlBase + 101010390},
			'AgregarUsoGlosaTIpoInstrumento'  				: { method: "POST", url: urlBase + 101010270},
			'QuitarUsoGlosaTIpoInstrumento'					: { method: "POST", url: urlBase + 101010271},
			'CambiarPrioridadUsoGlosaTIpoInstrumento'		: { method: "POST", url: urlBase + 101010272},
		});
	}
	
	angular.module('ModuloPlataforma').factory('bcsServiciosCargaMasivaPlataforma',['$resource',function($resource) {
		UrlCrearCargaMasivaPlataforma				=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=8014";
		UrlProcesarCargaMasivaPlataforma			=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=8016";
		UrlEliminarCargaMasivaPlataforma			=	"/BSCL/Plataforma/GWJAVA/PLA/call?t=8015";
		UrlProcesarCargaMasivaPlataformaReporte		= 	"/BSCL/Plataforma/GWJAVA/PLA/call?t=3267";
		// llamadas para generar cartola copiadas desde modulo normativa
		urlGenerarCartola 	                        =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=21021";
	    urlDataCartola		                        =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=601174";
	    urlInformacionCliente	                    =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=601199";
		urlBarraProgresoServicio	            =   "/BSCL/Plataforma/GWJAVA/PLA/call?t=702167";
		
		// ______________________________________________________________
	return $resource("", {},
	  {
		// llamadas para generar cartola copiadas desde modulo normativa
        'GenerarCartola'					    :   { method: "POST", url: urlGenerarCartola},
        'ObtieneDataCartola'					:   { method: "POST", url: urlDataCartola},
        'InformacionCliente'					:   { method: "POST", url: urlInformacionCliente},
		// ______________________________________________________________
		'CrearCargaMasivaPlataforma'			:	{ method: "POST", url: UrlCrearCargaMasivaPlataforma},
		'ProcesarCargaMasivaPlataforma'			:	{ method: "POST", url: UrlProcesarCargaMasivaPlataforma},
		'EliminarCargaMasivaPlataforma'			:	{ method: "POST", url: UrlEliminarCargaMasivaPlataforma},
		'ProcesarCargaMasivaPlataformaReporte'	:	{ method: "POST", url: UrlProcesarCargaMasivaPlataformaReporte},
		'BarraProgresoServicio'					:	{ method: "POST", url: urlBarraProgresoServicio},
	   });
	}]);
	
	obj.DataSourceConfig = function() {
		return new bcs.DataSource(
			{
				type : 'odata',
				definition : {
					transport : {
						read : {
							url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=4141',
							dataType : 'json'
						}
					},
					serverFiltering : true,
					serverPaging : true,
					serverSorting : true,
					sort : {
						field : "Descripcion",
						dir : "asc"
					},
					pageSize : 1000,
					schema : {
						model : {
							fields : {
								IdConfig : {
									type : 'number'
								},
								Descripcion : {
									type : 'string'
								},
								Tipo : {
									type : 'string'
								},
								Nombre : {
									type : 'string'
								},
								Valor : {
									type : 'string'
								}
							}
						}
					}
				}
			});
	}

	obj.TiposDeBonos = function(success_fn) {
		var ds = obj.DataSourceConfig();
		var filter = [ { field: "Nombre", operator: "eq", value: 'tiposBono'}];
		ds.query({ filter: filter }).then(function(e) {
			var view = ds.view();
			var ret = null;
			if (view.length > 0) {
				var val = view[0].Valor;
				if (val !== null && val.length > 0) {
					ret = val.split(',');
				}
			}
			 
			success_fn(ret);
		});
	}

	obj.InterfazLiquidezIntradia = function(datos,success_fn,error_fn){
		var urlInterfazLiquidezIntradia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=37463443"; 
		var r = $resource("", {},{
			'InterfazLiquidezIntradia': { method: "POST", url: urlInterfazLiquidezIntradia}
		});
		var post = r.InterfazLiquidezIntradia(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	obj.AgregarPrograma = function(datos,success_fn,error_fn)
	{

		var urlAgregarPrograma = "/BSCL/Plataforma/GWJAVA/PLA/call?t=183";
		
		var r = $resource("", {},
	    {
			'AgregarPrograma' : { method: "POST", url: urlAgregarPrograma}																
		});
		var post = r.AgregarPrograma(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	
	obj.CalcularValorizacion = function(datos,success_fn,error_fn)
	{
		// var urlValorizador =
		// "/BSCL/Plataforma/XSREST/Valorizador.xsjs?op="+operacion;
		var urlValorizador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=266";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlValorizador}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	obj.Valorizar = function(datos,success_fn,error_fn)
	{
		// var urlValorizador =
		// "/BSCL/Plataforma/XSREST/Valorizador.xsjs?op="+operacion;
		var urlValorizador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=279";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlValorizador}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	obj.ValorizarNuevo = function(datos,success_fn,error_fn)
	{
		var urlValorizador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1684";
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlValorizador}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	// VALORIZAR JAVA (incluye Perú)
	obj.ValorizarJava = function(datos,success_fn,error_fn)
	{
		var urlValorizador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20033";
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlValorizador}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	// VALORIZAR JAVA (incluye Perú)
	obj.ValorizarInstrumento = function(datos,success_fn,error_fn)
	{
		var urlValorizador = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20200";
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlValorizador}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	// registro log plataforma
	
	obj.RegistrarLog = function(datos,success_fn,error_fn)
	{
		var urlRegistrarLog = "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290893";
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlRegistrarLog}// ,
														
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	 
	obj.ObtenerPrecio = function(datos,success_fn,error_fn)
	{
		var urlPrecio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=268";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlPrecio}// , isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.ObtenerPrecioNew = function(datos,success_fn,error_fn)
	{
		var urlPrecioNew = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3252";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlPrecioNew}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.EntregarPreciosTasas = function(datos,success_fn,error_fn)
	{
		var urlPrecioNew = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100042";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlPrecioNew}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.AgregarControlProceso = function(datos,success_fn,error_fn)
	{
		var urlPrecioNew = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600007";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlPrecioNew}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.Fechas = function(datos,success_fn,error_fn)
	{
		var urlConsultarFechas = "/BSCL/Plataforma/GWJAVA/PLA/call?t=14";
		var r = $resource("", {},
	    {
			'ConsultarFechas': { method: "POST", url: urlConsultarFechas},
		});
		var post = r.ConsultarFechas(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	/* Servicios de EVT */
	obj.AgregarPlantilla = function(datos,success_fn,error_fn)
	{
		var urlAgregarPlantilla = "/BSCL/Plataforma/GWJAVA/PLA/call?t=203";
		var r = $resource("", {},
	    {
			'AgregarPlantilla': { method: "POST", url: urlAgregarPlantilla},
		});
		var post = r.AgregarPlantilla(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.EnviarNotificaciones = function(datos,success_fn,error_fn)
	{
		var urlEnviarNotificaciones = "/BSCL/Plataforma/GWJAVA/PLA/call?t=312";
 		var r = $resource("", {},
	    {
			'EnviarNotificaciones': { method: "POST", url: urlEnviarNotificaciones},
		});
		var post = r.EnviarNotificaciones(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.QuitarEvento = function(datos,success_fn,error_fn)
	{
		var urlQuitarEvento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1501";
 		var r = $resource("", {},
	    {
			'QuitarEvento': { method: "POST", url: urlQuitarEvento},
		});
		var post = r.QuitarEvento(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ConfirmarNotificacion = function(datos,success_fn,error_fn)
	{
		var urlConfirmarNotificacion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1423";
 		var r = $resource("", {},
	    {
			'ConfirmarNotificacion': { method: "POST", url: urlConfirmarNotificacion},
		});
		var post = r.ConfirmarNotificacion(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ReEnviarNotificaciones = function(datos,success_fn,error_fn)
	{
		var urlReEnviarNotificaciones = "/BSCL/Plataforma/GWJAVA/PLA/call?t=409";
 		var r = $resource("", {},
	    {
			'ReEnviarNotificaciones': { method: "POST", url: urlReEnviarNotificaciones},
		});
		var post = r.ReEnviarNotificaciones(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.AgregarTipoEvento = function(datos,success_fn,error_fn)
	{
		var urlAgregarTipoEvento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=207";
 		var r = $resource("", {},
	    {
			'AgregarTipoEvento': { method: "POST", url: urlAgregarTipoEvento},
		});
		var post = r.AgregarTipoEvento(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.AgregarEventoConfiguracion = function(datos,success_fn,error_fn)
	{
		var urlAgregarEventoConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=205";
 		var r = $resource("", {},
	    {
			'AgregarEventoConfiguracion': { method: "POST", url: urlAgregarEventoConfiguracion},
		});
		var post = r.AgregarEventoConfiguracion(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	obj.AsocicarTipoEventoConConfiguracion = function(datos,success_fn,error_fn)
	{
		var urlAsocicarTipoEventoConConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=209";
 		var r = $resource("", {},
	    {
			'AsocicarTipoEventoConConfiguracion': { method: "POST", url: urlAsocicarTipoEventoConConfiguracion},
		});
		var post = r.AsocicarTipoEventoConConfiguracion(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	obj.ActualizarEventoConfiguracion = function(datos,success_fn,error_fn)
	{
		var urlActualizarEventoConfiguracion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=339";
 		var r = $resource("", {},
	    {
			'ActualizarEventoConfiguracion': { method: "POST", url: urlActualizarEventoConfiguracion},
		});
		var post = r.ActualizarEventoConfiguracion(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	obj.ActivarConfiguracionEvento = function(datos,success_fn,error_fn)
	{
		var urlActivarConfiguracionEvento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=340";
 		var r = $resource("", {},
	    {
			'ActivarConfiguracionEvento': { method: "POST", url: urlActivarConfiguracionEvento},
		});
		var post = r.ActivarConfiguracionEvento(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	obj.ActivarConfiguracionEventoDetalle = function(datos,success_fn,error_fn)
	{
		var urlActivarConfiguracionEventoDetalle = "/BSCL/Plataforma/GWJAVA/PLA/call?t=341";
 		var r = $resource("", {},
	    {
			'ActivarConfiguracionEventoDetalle': { method: "POST", url: urlActivarConfiguracionEventoDetalle},
		});
		var post = r.ActivarConfiguracionEventoDetalle(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ActualizarPlantilla = function(datos,success_fn,error_fn)
	{
		var urlActualizarPlantilla = "/BSCL/Plataforma/GWJAVA/PLA/call?t=353";
 		var r = $resource("", {},
	    {
			'ActualizarPlantilla': { method: "POST", url: urlActualizarPlantilla},
		});
		var post = r.ActualizarPlantilla(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	obj.EnviarComentenos = function(datos,success_fn,error_fn)
	{
		var urlEnviarComentenos = "/BSCL/Plataforma/GWJAVA/PLA/call?t=413";
 		var r = $resource("", {},
	    {
			'EnviarComentenos': { method: "POST", url: urlEnviarComentenos},
		});
		var post = r.EnviarComentenos(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ModificarPreferenciasNotificacion = function(datos,success_fn,error_fn)
	{
		var urlModificarAtributo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1468";
		var r = $resource("", {},
	    {
			'Modificar': { method: "POST", url: urlModificarAtributo}
		});
		var post = r.Modificar(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	//Servicios de Mensajes
	obj.AgregarConfiguracionMensaje = function(datos,success_fn,error_fn)
	{
		var urlAgregarConfiguracionMensaje = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700311";
 		var r = $resource("", {},
	    {
			'AgregarConfiguracionMensaje': { method: "POST", url: urlAgregarConfiguracionMensaje},
		});
		var post = r.AgregarConfiguracionMensaje(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ModificarConfiguracionMensaje = function(datos,success_fn,error_fn)
	{
		var urlModificarConfiguracionMensaje = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700312";
 		var r = $resource("", {},
	    {
			'ModificarConfiguracionMensaje': { method: "POST", url: urlModificarConfiguracionMensaje},
		});
		var post = r.ModificarConfiguracionMensaje(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	/* FIN de Servicios EVT */

	// Servicios Diccionario
	obj.AgregarDiccionario = function(datos,success_fn,error_fn)
	{
		var urlAgregarDiccionario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20034";
		var r = $resource("", {},
	    {
			'AgregarDiccionario': { method: "POST", url: urlAgregarDiccionario}
		});
		var post = r.AgregarDiccionario(datos);
		return post.$promise;
	};
	
	obj.ModificarDiccionario = function(datos,success_fn,error_fn)
	{
		var urlModificarDiccionario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20035";
		var r = $resource("", {},
	    {
			'ModificarDiccionario': { method: "POST", url: urlModificarDiccionario}
		});
		var post = r.ModificarDiccionario(datos);
		return post.$promise;
	};
	
	// Servicio para ajustar fecha del sistema, eliminar cache y reiniciar
	// sesion
	obj.AjustarFechaSistema = function(datos,success_fn,error_fn)
	{
		var urlAjustarFechaSistema = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20039&c=all";
		var r = $resource("", {},
	    {
			'AjustarFechaSistema': { method: "POST", url: urlAjustarFechaSistema},
		});
		var post = r.AjustarFechaSistema(datos);
		return post.$promise;
	};

	obj.FechaSistema = function(datos,success_fn,error_fn)
	{
		var urlConsultarFechaHoraSistema = "/BSCL/Plataforma/GWJAVA/PLA/call?t=2";
		var r = $resource("", {},
	    {
			'ConsultarFechaHoraSistema': { method: "POST", url: urlConsultarFechaHoraSistema},
		});
		var post = r.ConsultarFechaHoraSistema(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.FechaLiquidacion = function(datos,success_fn,error_fn)
	{
		var urlConsultarFechaLiquidacion = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1";
		var r = $resource("", {},
	    {
			'ConsultarFechaLiquidacion': { method: "POST", url: urlConsultarFechaLiquidacion},
		});
		var post = r.ConsultarFechaLiquidacion(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.FechaSiguiente = function(datos,success_fn,error_fn)
	{
		console.error("NO USAR ESTA FUNCION, USAR ObtenerDiaSiguiente 21030")
		var urlConsultarFechaSiguiente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=23";
		var r = $resource("", {},
	    {
			'ConsultarFechaSiguiente': { method: "POST", url: urlConsultarFechaSiguiente},
		});
		var post = r.ConsultarFechaSiguiente(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.FechaAnterior = function(datos,success_fn,error_fn)
	{
		var urlConsultarFechaAnterior = "/BSCL/Plataforma/GWJAVA/PLA/call?t=550";
		var r = $resource("", {},
	    {
			'ConsultarFechaAnterior': { method: "POST", url: urlConsultarFechaAnterior},
		});
		var post = r.ConsultarFechaAnterior(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.Plazo = function(datos,success_fn,error_fn)
	{
		var urlObtenerPlazo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=60";
		var r = $resource("", {},
	    {
			'ObtenerPlazo': { method: "POST", url: urlObtenerPlazo},
		});
		var post = r.ObtenerPlazo(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};


	obj.ConsultarFechaVencimiento = function(datos,success_fn,error_fn)
	{
		var r = $resource("", {},
	    {
			'ConsultarFechaVencimiento': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=47" },
		});
		var post = r.ConsultarFechaVencimiento(datos);
		return post.$promise;
	};


	/*
	 * obj.CalcularExigibilidad = function(datos) { var r = $resource("", {}, {
	 * 'CalcularExigibilidad': { method: "POST", url:
	 * "/BSCL/Plataforma/GWJAVA/PLA/call?t=30" }, }); var post =
	 * r.CalcularExigibilidad(datos); return post.$promise; };
	 * 
	 * obj.CalcularCobertura = function(datos) { var r = $resource("", {}, {
	 * 'CalcularCobertura': { method: "POST", url:
	 * "/BSCL/Plataforma/GWJAVA/PLA/call?t=31" }, }); return
	 * r.CalcularCobertura(datos).$promise; };
	 */

	obj.CalcularLinea = function(datos)
	{
		var r = $resource("", {},
	    {
			'CalcularLinea': { method: "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=33" },
		});
		return r.CalcularLinea(datos).$promise;
	};

	obj.CrearLimite = function(datos,success_fn,error_fn)
	{
		var urlCrearLimite = "/BSCL/Plataforma/GWJAVA/PLA/call?t=39";
		var r = $resource("", {},
	    {
			'Limite': { method: "POST", url: urlCrearLimite}
		});
		var post = r.Limite(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.ConsultarAtributos = function(datos,success_fn,error_fn)
	{
		var urlConsultarAtributos = "/BSCL/Plataforma/GWJAVA/PLA/call?t=63";
		var r = $resource("", {},
	    {
			'Atributos': { method: "POST", url: urlConsultarAtributos}
		});
		var post = r.Atributos(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.BolsaInstrumentoPrecioActual = function(datos,success_fn,error_fn)
	{
		var urlObtenerPrecio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=17";
		var r = $resource("", {},
	    {
			'Precio': { method: "POST", url: urlObtenerPrecio}
		});
		var post = r.Precio(datos);
		return post.$promise;
	};

	obj.ConsultarFlujoCustodia = function(datos,success_fn,error_fn)
	{
		// var urlConsultarFlujoCustodia =
		// "/BSCL/Plataforma/GWJAVA/PLA/call?t=19";
		var urlConsultarFlujoCustodia = "/BSCL/Plataforma/GWJAVA/PLA/call?t=14000";
		var r = $resource("", {},
	    {
			'ConsultarFlujoCustodia': { method: "POST", url: urlConsultarFlujoCustodia}
		});
		var post = r.ConsultarFlujoCustodia(datos);
		return post.$promise;
	};
	
	obj.ModificarAtributo = function(datos,success_fn,error_fn)
	{
		var urlModificarAtributo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=65";
		var r = $resource("", {},
	    {
			'Modificar': { method: "POST", url: urlModificarAtributo}
		});
		var post = r.Modificar(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ModificarAtributoEnrolamiento = function(datos,success_fn,error_fn)
	{
		var urlModificarAtributo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700256";
		var r = $resource("", {},
	    {
			'Modificar': { method: "POST", url: urlModificarAtributo}
		});
		var post = r.Modificar(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};
	
	obj.ModificarRequisito = function(datos,success_fn,error_fn)
	{
		var urlModificarRequisito = "/BSCL/Plataforma/GWJAVA/PLA/call?t=278";
		var r = $resource("", {},
	    {
			'Modificar': { method: "POST", url: urlModificarRequisito}
		});
		var post = r.Modificar(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.FirmarAnexo = function(datos,success_fn,error_fn)
	{
		var urlFirmarAnexo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=73";
		var r = $resource("", {},
	    {
			'Firmar': { method: "POST", url: urlFirmarAnexo}
		});
		var post = r.Firmar(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.FirmarDocumento = function(datos,success_fn,error_fn)
	{
		let urlFirmarDocumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700317";
		let r = $resource("", {},
	    {
			'FirmarDocumento': { method: "POST", url: urlFirmarDocumento }
		});
		let post = r.FirmarDocumento(datos);
		post.$promise.then(success_fn).catch(error_fn)
	};

	obj.GestionarFirmasDocumento = function(datos,success_fn,error_fn)
	{
		let urlGestionarFirmasDocumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700327";
		let r = $resource("", {}, { GestionarFirmasDocumento: { method: "POST", url: urlGestionarFirmasDocumento } });
		let post = r.GestionarFirmasDocumento(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	obj.ReaperturarTraspaso = function(datos,success_fn,error_fn)
	{
		let urlReaperturarTraspaso = "/BSCL/Plataforma/GWJAVA/PLA/call?t=6200";
		let r = $resource("", {}, { GestionarFirmasDocumento: { method: "POST", url: urlReaperturarTraspaso } });
		let post = r.ReaperturarTraspaso(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.ModificarFirmaDocumento = function(datos,success_fn,error_fn)
	{
		let urlModificarFirmaDocumento = "/BSCL/Plataforma/GWJAVA/PLA/call?t=19486544";
		let r = $resource("", {}, { ModificarFirmaDocumento: { method: "POST", url: urlModificarFirmaDocumento } });
		let post = r.ModificarFirmaDocumento(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};

	obj.AgregarTipoAtributo = function(datos,success_fn,error_fn)
	{
		var urlAgregarTipoAtributo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=122";
		var r = $resource("", {},
	    {
			'Agregar': { method: "POST", url: urlAgregarTipoAtributo}
		});
		var post = r.Agregar(datos);
		return post.$promise;
	};
	obj.ModificarTipoAtributo = function(datos,success_fn,error_fn)
	{
		var urlModificarTipoAtributo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=123";
		var r = $resource("", {},
	    {
			'Modificar': { method: "POST", url: urlModificarTipoAtributo}
		});
		var post = r.Modificar(datos);
		return post.$promise;
	};
	
	obj.AjustarZonaHoraria = function(datos,success_fn,error_fn)
	{
		var urlAjustarZonaHoraria = "/BSCL/Plataforma/GWJAVA/PLA/call?t=333";
		var r = $resource("", {},
	    {
			'AjustarZonaHoraria': { method: "POST", url: urlAjustarZonaHoraria}
		});
		var post = r.AjustarZonaHoraria(datos);
		return post.$promise;
	};
	
	obj.ConsultarEstadoModulo = function(datos,success_fn,error_fn)
	{
		var urlConsultarEstadoModulo = "/BSCL/Plataforma/GWJAVA/PLA/call?t=600017";
		var r = $resource("", {},
	    {
			'ConsultarEstadoModulo': { method: "POST", url: urlConsultarEstadoModulo}
		});
		var post = r.ConsultarEstadoModulo(datos);
		return post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.ConceptosCheckboxSinValidacionFlujoCaja = function(success_fn) {
		var dataSourceConceptos = new bcs.DataSource({
			type : 'odata',
			definition : {
				transport : {
					read : {
						url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=700362',
						dataType : 'json'
					}
				},
				serverFiltering : true,
				serverPaging : true,
				serverSorting : true,
				pageSize : 100,
				schema : {
					model : {
						fields : {
							Conceptos : {
								type : 'string'
							}
						}
					}
				}
			}
		});
		
		dataSourceConceptos.fetch().then(function(e) {
			var conceptos = dataSourceConceptos.data();
			var respuesta = [];
			respuesta = conceptos[0].Conceptos.split(';');
			
			success_fn(respuesta);
		});
		
	};
	
	obj.CamposRequeridosFormulario = function(codigoFormulario, success_fn) {
		
		var dataSourceCamposFormulario = new bcs.DataSource(
		{
			type : 'odata',
			definition : {
				transport : {
					read : {
						url : '/BSCL/Plataforma/GWJAVA/PLA/call?t=700198',
						dataType : 'json'
					}
				},
				serverFiltering : true,
				serverPaging : true,
				serverSorting : true,
				pageSize : 10000,
				schema : {
					model : {
						fields : {
							CodigoFormulario : {
								type : 'string'
							},
							NombreFormulario : {
								type : 'string'
							},
							CodigoCampo : {
								type : 'string'
							},
							CodigoCampoFrontend : {
								type : 'string'
							},
							CodigoCampoBackend : {
								type : 'string'
							},
							NombreCampo : {
								type : 'string'
							},
							TipoDato : {
								type : 'string'
							},
							ValorDefecto : {
								type : 'string'
							},
							CantidadMinima : {
								type : 'number'
							},
							Requerido : {
								type : 'string'
							},
							IdTipoCanal : {
								type : 'string'
							},
							NombreCanal : {
								type : 'string'
							}
						}
					}
				}
			}
		});
		
		var filtrosCanal = { field: "IdTipoCanal", operator: "eq", value: 'OPT'};
		var filtroFormulario = { field: "CodigoFormulario", operator: "eq", value: codigoFormulario};
		var filtros = [];
		filtros.push(filtroFormulario);
		filtros.push(filtrosCanal);
		dataSourceCamposFormulario.query({ filter: filtros }).then(function(e) {
			var datos = dataSourceCamposFormulario.data();
			var respuesta = {};
			respuesta = datos
			.reduce(function(acum, item){
				acum[item.CodigoCampoFrontend] = item;
				return acum;
			}, {});
			
			success_fn(respuesta);
		});
	}
	
	obj.ObtenerParametroConfiguracionSistemaPromise = function ObtenerParametroConfiguracionSistemaPromise(codigoParametro, idEmpresa, moduloConfiguracion) {
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
					model: {
						Modulo: { type: 'string' },
						IdEmpresa: { type: 'number' },
						CodigoParametro: { type: 'string' },
						Valor: { type: 'string' },
						ValorDefecto: { type: 'string' },
					}
				},
				filter: [
					{ field: "CodigoParametro", operator: "eq", value: codigoParametro },
					{ field: "IdEmpresa", operator: "eq", value: idEmpresa },
					{ field: "Modulo", operator: "eq", value: moduloConfiguracion },
				],
			},
		});
		
		const deferred = $q.defer();
		try {
			dsParametrosSistema.fetch(function () {
				const empty = { Modulo: moduloConfiguracion, IdEmpresa: idEmpresa, CodigoParametro: codigoParametro, Valor: '', ValorDefecto: '', }
				deferred.resolve(dsParametrosSistema.data()[0] || empty);
			});
		} catch (e) {
			console.error(codigoParametro, e);
			deferred.reject(e);
		}
		
		return deferred.promise;
	} 

	return obj;



}]);

/* Configuracion de grillas para modulo */

angular.module('ModuloPlataforma').factory('bcsConfiguracionMatrizPerfilComercial',function($translate) {

	var obj = {};
	
	obj.getConfiguracionGridMatrizOperacional = function() {
		return {
			dataSource		: new bcs.DataSource({
				type 		: "odata", // Los tipos posibles son: odata, call,
										// array y object
				definition 	: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el
											// servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el
											// servidor.
					sortable 		: true,
					pageSize 		: 50,
					schema 			: {
						model	: {
		                    fields	: {
	                       		Bolsa			: { type: "string",editable: false 	},
		                        NombreProducto	: { type: "string",editable: false	},
		                        Operacion		: { type: "string" ,editable: false },
		                        Canal			: { type: "string",editable: false 	},
		                        Cobros			: { type: "string", editable: false },
		                        // Limites : { type: "string",editable: false }
	                        }
                   		}
					}
				}
			}),
			columns			: [
			 	{field: "Bolsa",width: "50px",title: $translate.instant("UI_BOLSA"),attributes : {style : "text-align:left;"}},
			 	{field: "NombreProducto",width: "150px",title: $translate.instant("UI_PRODUCTO"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
			 	{field: "Operacion",width: "100px",title: $translate.instant("UI_TIPO_OPERACION"),format:"{0:c}",attributes : {style : "text-align:left;"}},
			 	{field: "Canal",width: "50px",title: $translate.instant("UI_CANAL"),format:"{0:n0}", attributes: { style : "text-align:left;" }},
			 	{field: "Cobros",width: "400px",title: $translate.instant("UI_COBRO"),format:"{0:c}",attributes : {style : "text-align:left;"}},
			 	// {field: "Limites",width: "10%",title:
				// $translate.instant("UI_LIMITE"),format:"{0:c}",attributes :
				// {style : "text-align:left;"}}
			],		
			sortable			: false,
			pageable			: false,
			autoBind			: true,  // comentar
			height				: 490,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row"
		 };
	};
		
	// MATRIZ COBROS
	obj.getEsquemaGridMatrizCobros = function(){
		return {		 	
		 	IdInternal 						: { type: "number"},
			Prioridad 						: { type: "number"},
			IdPerfilCobros 					: { type: "string"},
			DescripcionPerfilCobros 		: { type: "string"},
			IdEmpresa 						: { type: "number"},
			EstadoPerfilCobros 				: { type: "string"},
			NombreEstadoPerfilCobros 		: { type: "string"},
			// IdGrupoCobrosNormal : { type: "string"},
			IdDetallePerfilCobros 			: { type: "number"},
			IdBolsa 						: { type: "string"},
			Bolsa 							: { type: "string"},
			IdTipoProducto 					: { type: "string"},
			TipoProducto 					: { type: "string"},
			IdTipoOperacion 				: { type: "string"},
			TipoOperacion 					: { type: "string"},
			IdCanalIngreso 					: { type: "string"},
			CanalIngreso 					: { type: "string"},
			DiasPlazo 						: { type: "number"},
			EstadoDetalle 					: { type: "string"},
			NombreEstadoDetalle 			: { type: "string"},
			IdCobro 						: { type: "string"},
			ValorCobro 						: { type: "string"},
			ValorTopeMinimoOrden 			: { type: "number"},
			IdMoneda 						: { type: "string"},
			EstadoDetalleGrupoCobroNormal 	: { type: "string"},
			NombreCobro 					: { type: "string"},
			IdCobroClasificacion 			: { type: "number"},
			FormaCobro 						: { type: "string"},
			PermiteCambiarValor 			: { type: "string"},
			CodigoCobroClasificacion 		: { type: "string"},
			CobroCalculado 					: { type: "number"},
			EsPersonalizado					: { type: "string"},
			// auxiliares.
			EsLineaNueva 					: { type: "string"}, // s/n
			ValorTopeMinimoOrdenTexto 		: { type: "string"},
			ValorCobroTexto 				: { type: "string"} 
		}
	};

	
	// MATRIZ PRODUCTOS
	obj.getEsquemaGridMatrizProductos = function(){
		return {		 	
			IdDetallePerfilProductos	: { type: "number"},
			IdTipoProducto 				: { type: "string"},
			TipoProducto 				: { type: "string"},
			IdTipoOperacion 			: { type: "string"},
			TipoOperacion 				: { type: "string"},
			IdCanalIngreso 				: { type: "string"},
			CanalIngreso 				: { type: "string"},
			PermiteRuteo 				: { type: "string"},
			IdMoneda 					: { type: "string"},
			Moneda 						: { type: "string"},
			IdGrupoActivos				: { type: "string"},
			EstadoProducto 				: { type: "string"},
			NombreEstadoProducto 		: { type: "string"},
		}
	};
	
	obj.getConfiguracionGridMatrizProductos = function() {
		return {
			dataSource		: new bcs.DataSource({
				type 		: "odata", // Los tipos posibles son: odata, call,
										// array y object
				definition 	: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el
											// servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el
											// servidor.
					sortable 		: true,
					pageSize 		: 50,
					schema 			: {
						model	: { fields	: obj.getEsquemaGridMatrizProductos() }
					}
				}
			}),
			columns			: [
				{field: "TipoProducto",width: "20%",title: $translate.instant("UI_PRODUCTO"),attributes : {style : "text-align:left;"}},
				{field: "TipoOperacion",width: "20%",title: $translate.instant("UI_TIPO_OPERACION"),attributes : {style : "text-align:left;"}},
				{field: "CanalIngreso",width: "20%",title: $translate.instant("UI_CANAL"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
				{field: "IdMoneda",width: "20%",title: $translate.instant("UI_MONEDA"),format:"{0:c}",attributes : {style : "text-align:left;"}},
				{field: "NombreEstadoProducto",width: "20%",title: $translate.instant("UI_HABILITADO"),format:"{0:n0}", attributes: { style : "text-align:left;" }},
				{field: "IdGrupoActivos",width: "20%",title: $translate.instant("UI_GRUPO_ACTIVOS"), attributes: { style : "text-align:left;" }},
			],		
			sortable			: false,
			pageable			: true,
			autoBind			: true,  // comentar
			height				: 280,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row"
		 };
	};
	
	obj.getConfiguracionGridMatrizGrupoActivos = function() {
		return {
			dataSource		: new bcs.DataSource({
				type 		: "odata", // Los tipos posibles son: odata, call,
										// array y object
				definition 	: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el
											// servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el
											// servidor.
					sortable 		: true,
					pageSize 		: 50,
					schema 			: {
						model	: {
		                    fields	: {
		                    	IdDetalleGrupoActivos	: {type: "int"		},
		                    	IdGrupoActivos 			: { type: "string"	},
		                    	IdInstrumento 			: { type: "string"	},
		                    	Nemotecnico				: { type: "string"	},
		                    	IdTipoInstrumento		: { type: "string"	},
		                    	CodigoTipoInstrumento	: { type: "string"	},
		                    	NombreTipoInstrumento	: { type: "string"	},
		                    	IdEstado	 			: { type: "string"	},
		                    	EstadoDetalle 			: { type: "string"	},
		                    	// NombreEstado
								// IdEmpresa : { type: "string" }
							}
                   		}
					}
				}
			}),
			columns			: [
				{field: "Nemotecnico",width: "30%",title: $translate.instant("UI_NEMOTECNICO"),attributes : {style : "text-align:left;"}},
				{field: "NombreTipoInstrumento",width: "60%",title: $translate.instant("UI_TIPO_INSTRUMENTO"),attributes : {style : "text-align:left;"}},
				{field: "IdEstado",width: "30%",title: $translate.instant("UI_HABILITADO"),format:"{0:n0}", attributes: { style : "text-align:left;" }, hidden:true},	
				{field: "EstadoDetalle",width: "30%",title: $translate.instant("UI_ESTADO"),format:"{0:n0}", attributes: { style : "text-align:left;" }}	
			],		
			sortable			: false,
			pageable			: true,
			autoBind			: true,  // comentar
			height				: 280,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row"
		 };
	};
	
	return obj;
});



angular.module('ModuloPlataforma').factory('bcsConfiguracionPlataforma',function($translate,$rootScope) {
	var obj = {};
	var minAutocomplete = 1;// empezar a buscar resultados al ingresar un
							// caracter
	
	obj.configuracionCombos = {};
	
	obj.getConfigGridDemo = function()
	{
		var translate_array = ['UI_TODOS'];
		var configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
                dataSource: {
                    type: "odata",
                    transport: {
                        read: "//demos.telerik.com/kendo-ui/service/Northwind.svc/Employees"
                    },
                    pageSize: 5,
                    serverPaging: true,
                    serverSorting: true
                },
                sortable: true,
                pageable: true,
                columns: [{
                    field: "FirstName",
                    title: "First Name",
                    width: "120px"
                    },{
                    field: "LastName",
                    title: "Last Name",
                    width: "120px"
                    },{
                    field: "Country",
                    width: "120px"
                    },{
                    field: "City",
                    width: "120px"
                    },{
                    field: "Title"
                }]
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFiltroEstadosConfirmacionFWRF = function()
	{
		var translate_array = [];
		
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
						{ Codigo: "CNF", Nombre: $translate.instant('UI_CONFIRMADO_MYUS') },
						{ Codigo: "NCF", Nombre: $translate.instant('UI_NO_CONFIRMADO') },
						{ Codigo: "RZD", Nombre: $translate.instant('UI_RECHAZADO_MYUS') }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxFiltroTipoCarteraMesa = function()
	{
		var translate_array = [];

		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
						{ Codigo: '', Nombre: 'TODOS' },
						{ Codigo: 'N', Nombre: 'CARTERA PROPIA' },
						{ Codigo: 'S', Nombre: 'RETRO COMPRA' }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxFiltroTipoSaldo = function()
	{
		var translate_array = [];
		
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
						{ Codigo: "POS", Nombre: $translate.instant('UI_SALDOS_POSITIVOS') },
						{ Codigo: "NEG", Nombre: $translate.instant('UI_SALDOS_NEGATIVOS') }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFiltroTipoOperacionForward = function()
	{
		var translate_array = [];
		
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
						{ Codigo: "COMFWD", Nombre: $translate.instant('UI_COMPRAS_FORWARD') },
						{ Codigo: "VENFWD", Nombre: $translate.instant('UI_VENTAS_FORWARD') }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	var getConfigComboboxTipoCliente= function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"), 
            dataTextField: 'Nombre',
            dataValueField: 'IdTipoCliente',
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	sort: { field: "Nombre", dir: "asc" },
            	serverPaging: false,
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1664",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-tipo-cliente'] = getConfigComboboxTipoCliente();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxClaseCliente= function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: 'Nombre',
            dataValueField: 'IdClaseCliente',
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	sort: { field: "Nombre", dir: "asc" },
            	serverPaging: false,
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3562",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-clase-cliente'] = getConfigComboboxClaseCliente();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxTipoRangoCobro = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreTipoRangoCobro",
            dataValueField: "IdTipoRangoCobro",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "IdTipoRangoCobro", dir: "desc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3199",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-tipo-rango-cobro'] = getConfigComboboxTipoRangoCobro();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxBaseTasaCobro = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreBaseTasaCobro",
            dataValueField: "IdBaseTasaCobro",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "IdBaseTasaCobro", dir: "desc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3198",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-base-tasa'] = getConfigComboboxBaseTasaCobro();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxCobroPeru = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'CodigoDefinicionCobro',
	            dataValueField: 'CodigoDefinicionCobro',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoDefinicionCobro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3530",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-cobro-peru'] = getConfigComboboxCobroPeru();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxEstadoCobro = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreEstado",
            dataValueField: "IdEstado",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "IdEstado", dir: "desc" }, // campo y criterio
															// de ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3194",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-estado-cobro'] = getConfigComboboxEstadoCobro();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxTipoCobro = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreTipoCobro",
            dataValueField: "IdTipoCobro",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "IdTipoCobro", dir: "desc" }, // campo y
																// criterio de
																// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3193",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-tipo-cobro'] = getConfigComboboxTipoCobro();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxClaseCobro = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreClaseCobro",
            dataValueField: "IdClaseCobro",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "IdClaseCobro", dir: "desc" }, // campo y
																// criterio de
																// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3192",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-clase-cobro'] = getConfigComboboxClaseCobro();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxCompraVenta = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreAccionOrden",
            dataValueField: "AccionOrden",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "AccionOrden", dir: "asc" }, // campo y criterio
															// de ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3593",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-compra-venta'] = getConfigComboboxCompraVenta();
	// -----------------------------------------------------------------------------------------
	obj.getConfigComboboxBolsaInstrumento = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_BOLSA"),
            dataTextField: "Nombre",
            dataValueField: "IdBolsa",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
														// de ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21010",
						dataType:"json"
            		}
            	},
            	schema : {
            		model : {
                		fields : {
                			IdPais 					: {type : "number"},
                			IdInstrumento			: {type : "number"},
                			Nombre 					: {type : "string"},
                			EsExtranjera			: {type : "string"},
                			TieneFormaNegociacion	: {type : "string"}
                		}
                	}	
            	}
            	
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-bolsa-instrumento'] = obj.getConfigComboboxBolsaInstrumento();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxEntidadClasificadoraRiesgo = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "IdEntidadClasificadoraRiesgo",
            dataValueField: "IdEntidadClasificadoraRiesgo",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "IdEntidadClasificadoraRiesgo", dir: "desc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3231",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-entidad-clasificadora-riesgo'] = getConfigComboboxEntidadClasificadoraRiesgo();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxUsoFlujoCaja = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "UsoFlujoCaja",
            dataValueField: "UsoFlujoCaja",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "UsoFlujoCaja", dir: "desc" }, // campo y
																// criterio de
																// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3149",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-uso-flujo-caja'] = getConfigComboboxUsoFlujoCaja();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxUsoFlujoCustodia = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreCompleto",
            dataValueField: "UsoTipoOperacion",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "UsoTipoOperacion", dir: "desc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3266",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-uso-flujo-custodia'] = getConfigComboboxUsoFlujoCustodia();
	// -----------------------------------------------------------------------------------------
	obj.getConfigComboboxNemoNegociacion = function()
	{
		var translate_array = ['UI_NEMO_NEGOCIACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONDICION_LIQUIDACION,
	            dataTextField: "P_InstrumentoSalida",
	            dataValueField: "P_InstrumentoSalida",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=70000000",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxCondicionLiquidacionNegociacion = function()
	{
		var translate_array = ['UI_CONDICION_LIQUIDACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONDICION_LIQUIDACION,
	            dataTextField: "IdCondicionDeLiquidacion",
	            dataValueField: "IdCondicionDeLiquidacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=70000005",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigTipoInstrumentoMonedaOrden = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "IdMoneda",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=70000004", //
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxMonedaNegociacion = function()
    {
        var translate_array = ['UI_MONEDA','UI_TODOS'];
        configPromise = $translate(translate_array).then(function (translations) {
            var config = {};
            // configuracion del combo
            config = {
                placeholder: translations.UI_MONEDA,
                dataTextField: "IdMoneda",
                dataValueField: "IdMoneda",
                filter: "contains",
                autoBind: false,
                minLength: minAutocomplete,
                dataSource: new kendo.data.DataSource({
                    type: "odata",
                    serverFiltering: false,
                    serverSorting: false,  // ordena al lado del servidor
                    transport: {
                        read: {
                            url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=70000010",
                            dataType:"json"
                        }
                    }
                })
            };
            return config;
        });
        return configPromise;
    };

	obj.getConfigComboboxOpcionesCobroDisponible = function()
    {
        var translate_array = ['UI_OPCIONES_COBRO','UI_TODOS'];
        configPromise = $translate(translate_array).then(function (translations) {
            var config = {};
            // configuracion del combo
            config = {
                placeholder: translations.UI_OPCIONES_COBRO,
                dataTextField: "DetalleIndicador",
                dataValueField: "IdDetalleIndicador",
                filter: "startswith",
                autoBind: false,
                minLength: minAutocomplete,
                dataSource: new kendo.data.DataSource({
                    type: "odata",
                    serverFiltering: false,
                    serverSorting: false,  // ordena al lado del servidor
                    sort: { field: "IdDetalleIndicador", dir: "asc" }, // campo y
                                                                    // criterio
                                                                    // de
                                                                    // ordenamiento
                    transport: {
                        read: {
                            url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702166",
                            dataType:"json"
                        }
                    }, 
                    schema: { 
                        model: { 
                            fields : {
                                IdDetalleIndicador               : { type:"number" },
                                DetalleIndicador             : { type:"string" }                          
                            }   
                        }  
                    },
                })
            };
            return config;
        });
        return configPromise;
    };
	obj.getConfigComboboxCondicionLiquidacion = function()
	{
		var translate_array = ['UI_CONDICION_LIQUIDACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONDICION_LIQUIDACION,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdCondicionDeLiquidacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Orden", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1568",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCondicionLiquidacionIntegracion = function()
	{
		var translate_array = ['UI_CONDICION_LIQUIDACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONDICION_LIQUIDACION,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdCondicionDeLiquidacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Orden", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=70000005",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxInstrumento = function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "and",
				      filters: [
				         {"field": "toupper(NemotecnicoUI)" ,"operator":"contains", "value": text.toUpperCase() },
						 {"field": "IdInstrumentoRaiz" ,"operator":"eq", "value": 0 },
				      ]
			    	};
					filter.push(nfilters);
				},
				minLength: minAutocomplete,
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "NemotecnicoUI",
	            dataValueField: "IdInstrumento",
	            autoBind: false,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NemotecnicoUI", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1540",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				 IdInstrumento : { type:"number"  } ,
	            				 Nemotecnico : { type:"string"  } ,
	            				 IdTipoInstrumento : { type:"string"  }

	            			}
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInstrumentoSimultanea = function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: false,
				minLength: minAutocomplete,
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1540",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				 IdInstrumento : { type:"number"  } ,
	            				 Nemotecnico : { type:"string"  } ,
	            				 IdTipoInstrumento : { type:"string"  }

	            			}
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInstrumentoReporte = function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: false,
				minLength: minAutocomplete,
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4162",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				 IdInstrumento : { type:"number"  } ,
	            				 Nemotecnico : { type:"string"  } ,
	            				 IdTipoInstrumento : { type:"string"}

	            			}
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};
	
	    	
	obj.getConfigComboboxInstrumentoBolsa = function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1569",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				 IdInstrumento : { type:"number"  } ,
	            				 Nemotecnico : { type:"string"  } ,
	            				 IdTipoInstrumento : { type:"string"  }

	            			}
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};	

	
	
	obj.getConfigComboboxTipoInstrumento = function()
	{
		var translate_array = ['UI_TIPO_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: "NombreLargoTipoInstrumento",
	            dataValueField: "IdTipoInstrumento",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoInstrumento", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1570",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: { fields : {IdContraparteGarantia : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxClasificacionTipoInstrumento = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION,
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionTipoInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1571",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxNemo4Porciento = function()
	{
		var translate_array = ['UI_NEMO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo nemo
			config = {
				placeholder: translations.UI_NEMO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "Nemotecnico",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3618",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxClientes4porciento = function()
	{
		var translate_array = ['UI_CLIENTE','UI_TODOS'];
		console.log("llama a getConfigComboboxClientes4porciento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo clientes
			config = {
				placeholder: translations.UI_CLIENTE,
	            dataTextField: "RutCliente",
	            dataValueField: "IdCliente",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "RutCliente", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3617",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

    obj.getConfigComboboxTipoInstrumentoSubyacente = function()
	{
		var translate_array = ['UI_TIPO_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo nemo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: "NombreLargoTipoInstrumento",
	            dataValueField: "IdTipoInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLargoTipoInstrumento", dir: "asc" }, // campo
																					// y
																					// criterio
																					// de
																					// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1572",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoInstrumentoSubyacenteInstrucciones = function()
	{
		var translate_array = ['UI_TIPO_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: "NombreTipoInstrumentoSubyacente",
	            dataValueField: "IdTipoInstrumentoSubyacente",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLargoTipoInstrumento", dir: "asc" }, // campo
																					// y
																					// criterio
																					// de
																					// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1572",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
    obj.getConfigComboboxTipoNormativa = function()
	{
		var translate_array = ['UI_TIPO_NORMATIVA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_NORMATIVA,
	            dataTextField: "NombreNormativa",
	            dataValueField: "IdNormativa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdNormativa", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1573",
							dataType:"json"
	            		}
	            	},
	            	// schema: { model: { fields : {IdTipoCircular : {
					// type:"number" }} } }
	            })
			};
			return config;
		});
		return configPromise;
	};
	
    obj.getConfigComboboxTipoNormativaUI = function()
	{
		var translate_array = ['UI_TIPO_NORMATIVA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_NORMATIVA,
	            dataTextField: "NombreNormativa",
	            dataValueField: "IdNormativa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdNormativa", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3578",
							dataType:"json"
	            		}
	            	},
	            	// schema: { model: { fields : {IdTipoCircular : {
					// type:"number" }} } }
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	 obj.getConfigComboboxTipoDocumentoNormativa = function()
	{
		var translate_array = ['UI_TIPO_DOCUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_DOCUMENTO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoDocumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoDocumento", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1574",
							dataType:"json"
	            		}
	            	},
	            	// schema: { model: { fields : {IdTipoCircular : {
					// type:"number" }} } }
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	 obj.getConfigComboboxSicam2p1Moneda = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "Descripcion",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Orden", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4400058",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};
	 obj.getConfigComboboxTipoDocumentoNormativaUI = function()
	{
		var translate_array = ['UI_TIPO_DOCUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_DOCUMENTO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoDocumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoDocumento", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3234",
							dataType:"json"
	            		}
	            	},
	            	// schema: { model: { fields : {IdTipoCircular : {
					// type:"number" }} } }
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	
	obj.getConfigComboboxClasificacionFatca = function()
	{
		var translate_array = ['UI_CLASIFICACION_FATCA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionFatca",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdClasificacionFatca", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1575",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxClasificacionEmpresaFatca = function()
	{
		var translate_array = ['UI_CLASIFICACION_EMPRESA_FATCA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionEmpresaFatca",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdClasificacionEmpresaFatca", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1576",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoOperacionOrigenFondos = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoOperacionOrigenFondos",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoOperacionOrigenFondos", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1577",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxBolsa = function()
	{
		var translate_array = ['UI_BOLSA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_BOLSA,
	            dataTextField: "Nombre",
	            dataValueField: "IdBolsa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1578",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCanalDistribucion = function()
	{
		var translate_array = ['UI_CANAL_DISTRIBUCION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CANAL_DISTRIBUCION,
	            dataTextField: "NombreTipoCanalDistribucion",
	            dataValueField: "IdTipoCanalDistribucion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoCanalDistribucion", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3045",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	// agrupación de cámaras del CCLV
	obj.getConfigComboboxGrupoCamara = function() 
	{
		var translate_array = ['UI_CAMARA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};	
			config = {
				placeholder: translations.UI_CAMARA,
	            dataTextField: "NombreGrupoCamara",
	            dataValueField: "CodigoGrupo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreGrupoCamara", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3121",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	
	obj.getConfigComboboxMoneda = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Orden", dir: "asc" },
	            	// sort: { field: "NombreLargo", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1013",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	 

	obj.getConfigComboboxTipoMoneda = function()
	{
		var translate_array = ['UI_TIPO_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_TIPO_MONEDA,
				dataTextField: "NombreTipoMoneda",
	            dataValueField: "IdTipoMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoMoneda", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1803",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	
	obj.getConfigComboboxMonedaPuntas = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Orden", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3598",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxMonedaTesoreria = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLargo", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1038",
							dataType:"json"
	            		} 
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigTipoInstrumentoMonedaLiquidacion = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "NombreMonedaLiquidacion",
	            dataValueField: "IdMonedaLiquidacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdMonedaLiquidacion", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1580",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	
	

		
	obj.getConfigComboboxEstadoLiquidacionSpot = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3600",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoCajaSpot = function()
	{
		var translate_array = [];

		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
						{ Codigo: "CNF", Nombre: $translate.instant('UI_CONFIRMADO_MYUS') },
						{ Codigo: "NRE", Nombre: $translate.instant('UI_NO_REQUERIDO_MYUS') },
						{ Codigo: "PEN", Nombre: $translate.instant('UI_PENDIENTE_MYUS') }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	}
	

	obj.getConfigComboboxEstadoMoneda = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",	            
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1804",
							dataType:"json"
	            		} 
	            	}
	            	
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoCorredorCorresponsal = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3014",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCorredorCorresponsal = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'CodigoCorredor',
	            dataValueField: 'IdCorredorCorresponsal',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,
	            	sort: { field: "DefaultPaisUI", dir: "desc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3009",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCorredora = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
				KdataTextTemplate:  function(row) {
					return row.CodigoCorredora + " - " + row.NombreCorto;
				},
	            dataTextField: 'KdataTextTemplate',
	            dataValueField: 'IdCorredora',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,
	            	sort: { field: "CodigoCorredora", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3130",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCorredoraInstruccion = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
				KdataTextTemplate:  function(row) {
					return row.CodigoCorredora + " - " + row.NombreCorto;
				},
	            dataTextField: 'KdataTextTemplate',
	            dataValueField: 'IdCorredora',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,
	            	sort: { field: "CodigoCorredora", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700356",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoPrograma = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1719",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoNormativa = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3068",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxOperacion = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "AccionOperacionText",
            dataValueField: "AccionOperacion",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "AccionOperacion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1582",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-operacion'] = getConfigComboboxOperacion();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxEstadoBancoCustodio = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreEstado",
            dataValueField: "IdEstado",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "IdEstado", dir: "asc" }, // campo y criterio
															// de ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3517",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-estado-banco-custodio'] = getConfigComboboxEstadoBancoCustodio();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxModeloCustodia = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreModeloCustodia",
            dataValueField: "IdModeloCustodia",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "IdModeloCustodia", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3519",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-modelo-custodia'] = getConfigComboboxModeloCustodia();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxTipoCustodiaPortafolio = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreTipoCustodia",
            dataValueField: "IdTipoCustodia",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "IdTipoCustodia", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3521",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-tipo-custodia-portafolio'] = getConfigComboboxTipoCustodiaPortafolio();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxTipoCuentaCustodia = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreTipoCuentaCustodia",
            dataValueField: "IdTipoCuentaCustodia",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "IdTipoCuentaCustodia", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3520",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-tipo-cuenta-custodia'] = getConfigComboboxTipoCuentaCustodia();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxEstadoBrokerExtranjero = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "NombreEstado",
            dataValueField: "IdEstado",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "IdEstado", dir: "asc" }, // campo y criterio
															// de ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3516",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-estado-broker-extranjero'] = getConfigComboboxEstadoBrokerExtranjero();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxBrokerExtranjero = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "CodigoNombreCompleto",
            dataValueField: "IdPersona",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "Codigo", dir: "asc" }, // campo y criterio de
														// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3511",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-broker-extranjero'] = getConfigComboboxBrokerExtranjero();
	
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxEstadoConceptos = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "Nombre",
            dataValueField: "IdEstado",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio de
														// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1790",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-estado-conceptos'] = getConfigComboboxEstadoConceptos();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxBancoCustodio = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "CodigoNombreCompleto",
            dataValueField: "IdPersona",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "Codigo", dir: "asc" }, // campo y criterio de
														// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3510",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-banco-custodio'] = getConfigComboboxBancoCustodio();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxBancoLiquidador = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "CodigoNombreCompleto",
            dataValueField: "IdPersona",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor**
            	sort: { field: "Codigo", dir: "asc" }, // campo y criterio de
														// ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3522",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-banco-liquidador'] = getConfigComboboxBancoLiquidador();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxPerfilCobrosPeru = function()
	{
		return {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField:  'IdPerfilCobros',
            dataValueField: 'IdPerfilCobros',
            filter: "contains",
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,
            	sort: { field: "IdPerfilCobros", dir: "asc" },
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3544",
						dataType:"json"
            		}
            	}
            })
		}
	};
	obj.configuracionCombos['combobox-perfil-cobros-peru'] = getConfigComboboxPerfilCobrosPeru();
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxTipoProductoCobroPeru = function()
	{
		return {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField:  'NombreTipoProducto',
            dataValueField: 'IdTipoProducto',
            filter: "contains",
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,
            	sort: { field: "IdTipoProducto", dir: "asc" },
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3551",
						dataType:"json"
            		}
            	}
            })
		}
	};
	obj.configuracionCombos['combobox-tipo-producto-cobro'] = getConfigComboboxTipoProductoCobroPeru();
	// -----------------------------------------------------------------------------------------
	obj.getConfigComboboxContraparte = function()
	{
		var translate_array = ['UI_CONTRAPARTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONTRAPARTE,
	            dataTextField: "NombreCompleto",
	            dataValueField: "IdContraparte",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "RazonSocial", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1583",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: { fields : {IdContraparte : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxGradoVinculacionParental = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
            dataTextField: "Nombre",
            dataValueField: "IdGradoVinculacionParental",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,
            	sort: { field: "Nombre", dir: "asc" }, 
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3529",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-grado-vinculacion-parental'] = getConfigComboboxGradoVinculacionParental();
	// -----------------------------------------------------------------------------------------
	


	obj.getConfigComboboxOperador = function()
	{
		var translate_array = ['UI_OPERADOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_OPERADOR,
	            dataTextField: "CodigoOperador",
	            dataValueField: "CodigoOperador",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CodigoOperador", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1351",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
									CodigoOperador: { type: "string"}
								}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	obj.getConfigComboboxOperadorLibro = function()
	{
		var translate_array = ['UI_OPERADOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_OPERADOR,
	            dataTextField: "CodigoOperador",
	            dataValueField: "CodigoOperador",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CodigoOperador", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1351",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
								CodigoOperador: { type: "string"},
								IdLibroOrdenes: { type: 'number'}
							}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoPrecio = function()
	{
		var translate_array = ['UI_TIPO_PRECIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_PRECIO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoPrecioOrden",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1218",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoReajuste = function()
	{
		var translate_array = ['UI_TIPO_REAJUSTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_REAJUSTE,
	            dataTextField: "Codigo",
	            dataValueField: "IdTipoReajuste",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoReajuste", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1507",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoInflacion = function()
	{
		var translate_array = ['UI_TIPO_INFLACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INFLACION,
	            dataTextField: "Codigo",
	            dataValueField: "IdTipoInflacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoInflacion", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4190",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInstrumentoTipoReajuste = function()
	{
		var translate_array = ['UI_TIPO_REAJUSTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_REAJUSTE,
	            dataTextField: "IdTipoReajuste",
	            dataValueField: "IdTipoReajuste",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoReajuste", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1585",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxFormaCustodiaOrden = function()
	{
		var translate_array = ['UI_FORMA_CUSTODIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMA_CUSTODIA,
	            dataTextField: "Nombre",
	            dataValueField: "IdFormaCustodiaOrden",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1219",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	
	obj.getConfigComboboxFormaCustodiaMesaRF = function()
	{
		var translate_array = ['UI_FORMA_CUSTODIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMA_CUSTODIA,
	            dataTextField: "Nombre",
	            dataValueField: "IdFormaCustodiaOrden",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1219",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	obj.getConfigComboboxTipoCustodiaOrden = function()
	{
		var translate_array = ['UI_TIPO_CUSTODIA_ORDEN','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CUSTODIA_ORDEN,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCustodiaOrden",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1220",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPorcentajeVisibilidadOrden = function()
	{
		var translate_array = ['UI_PORCENTAJE_VISIBILIDAD_ORDEN','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PORCENTAJE_VISIBILIDAD_ORDEN,
	            dataTextField: "Nombre",
	            dataValueField: "IdPorcentajeVisibilidad",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdPorcentajeVisibilidad ", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1221",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	// solo hoy, semanal, permanente, a fecha
	obj.getConfigComboboxTipoFechaVigenciaOrden = function()
	{
		var translate_array = ['UI_FECHA_VIGENCIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FECHA_VIGENCIA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoVigenciaOrden",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1222",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadosVigenciaOrden = function()
	{
		var translate_array = ['UI_VIGENCIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FECHA_VIGENCIA,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "desc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1223",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEmisor = function()
	{
		var translate_array = ['UI_EMISOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_EMISOR,
	            dataTextField: "NombreCorto",
	            dataValueField: "IdEmisor",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEmisor", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3161",
							dataType:"json",
							data:{
								$select: "\"IdEmisor\",\"EmiteIRF\",\"EmiteIIF\",\"EmiteFungible\",\"DocumentoIdentidad\",\"NombreCorto\""
							}
	            		}
		            	
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	/*
	 * obj.getConfigComboboxTipoMovimientoCtaInv = function() { var
	 * translate_array = ['UI_CONCEPTO','UI_TODOS']; configPromise =
	 * $translate(translate_array).then(function (translations) { var config =
	 * {}; //configuracion del combo config = { placeholder:
	 * translations.UI_CONCEPTO, dataTextField: "NombreConcepto",
	 * dataValueField: "IdConcepto", filter: "contains", autoBind: false,
	 * minLength: minAutocomplete, dataSource: new kendo.data.DataSource({ type:
	 * "odata", serverFiltering: false, serverSorting: false, //ordena al lado
	 * del servidor sort: { field: "NombreConcepto", dir: "asc" }, //campo y
	 * criterio de ordenamiento transport: { read: { url:
	 * "/BSCL/Plataforma/GWJAVA/PLA/call?t=1362", dataType:"json" } } }) };
	 * return config; }); return configPromise; };
	 */
	
	obj.getConfigComboboxTipoMovimiento = function()
	{
		var translate_array = ['UI_TIPO_MOVIMIENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_MOVIMIENTO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoMovimiento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1424",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxConceptoTesoreria = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "NombreConcepto",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1035",	            					
							dataType:"json"
	            		}
	            	},
	            	schema: { 
	            		model: { 
	            			fields : {
			            		IdConcepto				: { type:"string" },
			            		TipoConcepto			: { type:"string" },
			            		IdModulo				: { type:"string" },
			            		NombreConcepto			: { type:"string" },
			            		IdEstado				: { type:"string" },
			            		NombreEntidad			: { type:"string" },
			            		TipoConceptoTesoreria	: { type:"string" }
	            			}   
	            		}  
	            	},
	                serverSorting: true,  // ordena al lado del servidor
            	    sort: { field: "NombreConcepto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxConceptoCtaInv = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "NombreConcepto",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreConcepto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1362",	
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxCuentaInversion = function()
	{
		var translate_array = ['UI_CUENTA_INVERSION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CUENTA_INVERSION,
	            dataTextField: "Moneda",
	            dataValueField: "IdCuentaInversion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CuentaInversionPrincipal", dir: "desc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {	            			
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1365",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoOperacion = function()
	{
		var translate_array = ['UI_TIPO_OPERACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_OPERACION,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoOperacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1542",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxDemoRegion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "RegionDescription",
	            dataValueField: "RegionID",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "RegionDescription", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "http://demos.telerik.com/kendo-ui/service/Northwind.svc/Regions"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxDemoTerritori= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "TerritoryDescription",
	            dataValueField: "TerritoryID",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "TerritoryDescription", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "http://demos.telerik.com/kendo-ui/service/Northwind.svc/Territories"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};



	obj.getConfigComboboxConceptosGarantia = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "Nombre",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1433",
							dataType:"json"
	            		} 
	            	},
	            	schema: { model: { fields : {IdContraparte : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEspecieEnGarantia = function()
	{
		var translate_array = ['UI_ESPECIE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESPECIE,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEspecieGarantia",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1587",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxTipoGenero= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdTipoGenero',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1588",
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoPersona= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdTipoPersona',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1589",
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxClaseDocumento= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdClase',
	            filter: "contains", 
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601128",
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoTelefono= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoTelefono',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1590",
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	
	obj.getConfigComboboxPropositoMedioContacto= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdPropositoMedioContacto',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1591",
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCodigoEmpleado = function()
	{
		const translate_array = ['UI_TIPO_OPERACION','UI_TODOS'];
		return $translate(translate_array).then(function (translations) {
			return {
				placeholder: "...",
				dataTextField: 'CodigoEmpleado',
				dataValueField: 'CodigoEmpleado',
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1592",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdSucursal: { type: "number" }
							}
						}
					}
				}),
			};
		});
	};
	
	obj.getConfigComboboxEmpleado = function()
	{
		var translate_array = ['UI_TIPO_OPERACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'CodigoYNombreEmpleado',
	            dataValueField: 'IdEmpleado',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1592",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: { fields : {IdSucursal : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxTipoCargoEmpleado= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoCargoEmpleado',
	            dataValueField: 'CodigoTipoCargoEmpleado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoTipoCargoEmpleado", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1593",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxTipoCargoPersona= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoCargoPersona',
	            dataValueField: 'CodigoTipoCargoPersona',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoTipoCargoPersona", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3253",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	


	obj.getConfigComboboxEstadoTipoCliente = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700167",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxEstadoCliente = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1474",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoUsuario= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1530",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoRol= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1531",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxEstadoPerfil= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1532",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
		obj.getConfigComboboxEstadoEmpleado= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1594",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
		obj.getConfigComboboxEstadoProveedor= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1595",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAreaProveedor= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreArea',
	            dataValueField: 'IdAreaProveedor',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
						model: {
							fields: {
									IdPais :  { type: "number"}
									,NombrePais : { type: "string"}
									,CodigoPais : { type: "string"}
								}
						}
					},
	            	
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreArea", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1596",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	

	obj.getConfigComboboxTipoBloqueoSocioNegocio = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoBloqueo',
	            dataValueField: 'CodigoTipoBloqueo',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1597",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxTipoOperacionCustodia = function()
	{
		var translate_array = ['UI_TIPO_OPERACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_OPERACION,  // "Tipo
																// Operac.",
	            dataTextField: "Accion",
	            dataValueField: "IdTipoOperacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1598",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxInstrumentosCustodia = function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_INSTRUMENTO,  // "Tipo Operac.",
	            dataTextField: "Nemo",
	            dataValueField: "IdInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdInstrumento", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1599",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxBoveda = function()
	{
		var translate_array = ['UI_BOVEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_BOVEDA,
	            dataTextField: "Nombre",
	            dataValueField: "IdBoveda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1476",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxConcepto = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "Nombre",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1600",
							dataType:"json"
	            		}
	            	},
	                serverSorting: true,  // ordena al lado del servidor
            	    sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxConceptoCustodia = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "NombreConceptoFull", // Full: IdConcepto +
														// Nombre
	            dataValueField: "IdConceptoCustodia",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete, 
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1432",
							dataType:"json"
	            		}
	            	},
	                serverSorting: true,  // ordena al lado del servidor
            	    sort: { field: "NombreConcepto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoEmisor = function()
	{
		var translate_array = ['UI_EMISOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_EMISOR,
	            dataTextField: "IdEmisor",
	            dataValueField: "IdEmisor",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1503",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoCustodia = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Descripcion", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1601",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxEstadoCargaMasiva = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1881",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoPla = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Descripcion", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1581",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxTipoComprobante = function()
	{
		var translate_array = ['UI_TIPO_COMPROBANTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_COMPROBANTE,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoComprobante",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1245",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoReferencia = function()
	{
		var translate_array = ['UI_TIPO_REFERENCIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_REFERENCIA,
	            dataTextField: "Descripcion",
	            dataValueField: "TipoReferencia",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=848",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPortafolioContratoCobroPeriodico = function()
	{
		var translate_array = ['UI_DIGITAR_SOCIO_NEGOCIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_DIGITAR_SOCIO_NEGOCIO,
	            dataTextField: "Nombre",
	            dataValueField: "IdCliente",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3040",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoContratoCobroPeriodico = function()
	{
		var translate_array = ['UI_TIPO_CONTRATO_COBRO_PERIODICO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CONTRATO_COBRO_PERIODICO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoContratoCobroPeriodico",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3027",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFormatoContratoCobroPeriodico = function()
	{
		var translate_array = ['UI_FORMATO_CONTRATO_COBRO_PERIODICO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMATO_CONTRATO_COBRO_PERIODICO,
	            dataTextField: "Nombre",
	            dataValueField: "IdFormatoContratoCobroPeriodico",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3057",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
		

	obj.getConfigComboboxEstadoComprobante = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Estado",
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1246",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxSucursal = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: 'Sucursal',
	            dataTextField: "NombreSucursal",
	            dataValueField: "IdSucursal",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1602",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoTesoreria = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1036",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxMedioPago = function()
	{
		var translate_array = ['UI_MEDIOS_DE_PAGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MEDIOS_DE_PAGO,
	            dataTextField: "Nombre",
	            dataValueField: "IdMedioDePago",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1603",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxMedioPagoTesoreria = function()
	{
		var translate_array = ['UI_MEDIOS_DE_PAGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MEDIOS_DE_PAGO,
	            dataTextField: "Nombre",
	            dataValueField: "IdMedioDePago",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1040",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxMedioPagoCuentaInversion = function()
	{
		var translate_array = ['UI_MEDIOS_DE_PAGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MEDIOS_DE_PAGO,
	            dataTextField: "Nombre",
	            dataValueField: "IdMedioDePago",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1366",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFormaDePagoMesa = function()
	{
		var translate_array = ['UI_FORMAS_DE_PAGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMAS_DE_PAGO,
	            dataTextField: "NombreFormaDePago",
	            dataValueField: "IdFormaDePago",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: [{ field: "PorDefecto", dir: "desc" },
	            		   // { field: "Dias", dir: "asc" },
	            		   // { field: "RecibePagaPortafolioDesc", dir: "asc"
							// },
	            		   { field: "NombreFormaDePago", dir: "asc" },
	            		   { field: "Dias", dir: "asc" },],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1769",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxFormaDePagoPreferenciaPortafolio = function()
	{
		var translate_array = ['UI_FORMAS_DE_PAGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMAS_DE_PAGO,
	            dataTextField: "NombreFormaDePago",
	            dataValueField: "IdFormaDePago",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: [{ field: "PorDefecto", dir: "desc" },
	            		   { field: "NombreFormaDePago", dir: "asc" },
	            		   { field: "Dias", dir: "asc" },],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=80005",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getFormaDePagoCorredora = function()
	{
		var translate_array = ['UI_FORMAS_DE_PAGO_CORREDORA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMAS_DE_PAGO,
	            dataTextField: "Descripcion",
	            dataValueField: "Descripcion",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: [{ field: "PorDefecto", dir: "desc" }
	            		],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=900204",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getFormaDePagoCliente = function()
	{
		var translate_array = ['UI_FORMAS_DE_PAGO_CLIENTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMAS_DE_PAGO,
	            dataTextField: "Descripcion",
	            dataValueField: "Descripcion",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: [{ field: "PorDefecto", dir: "desc" }// ,
	            		   // { field: "Dias", dir: "asc" },
	            		   // { field: "RecibePagaPortafolioDesc", dir: "asc"
							// },
	            		   // { field: "NombreFormaDePago", dir: "asc" }
	            		],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1266",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getFormaDePagoCorredoraSpot = function()
	{
		var translate_array = ['UI_FORMAS_DE_PAGO_CORREDORA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMAS_DE_PAGO,
	            dataTextField: "Descripcion",
	            dataValueField: "Descripcion",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: [{ field: "Dias", dir: "asc" }
	            		],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290683",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getFormaDePagoClienteSpot = function()
	{
		var translate_array = ['UI_FORMAS_DE_PAGO_CLIENTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMAS_DE_PAGO,
	            dataTextField: "Descripcion",
	            dataValueField: "Descripcion",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: [{ field: "Dias", dir: "asc" }
	            		],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290682",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPersonaCuentasBancarias = function()
	{
		var translate_array = ['UI_CUENTAS_BANCARIAS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CUENTAS_BANCARIAS,				
	            dataTextField: "NombreCuentaBancariaFull",
	            dataValueField: "IdCuentaBancaria",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreCuentaBancariaFull", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1310",
							dataType:"json"
	            		}
	            	},
					schema: {
						model: {
		                    fields: {
	                       		// IdPortafolio: { type: "number"},
	                       		// IdCliente: { type: "number"},
	                       		IdPersona: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxCaja = function()
	{
		var translate_array = ['UI_CAJA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CAJA,
	            dataTextField: "NombreLargoCaja",
	            dataValueField: "IdCaja",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLargoCaja", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1028",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: { fields : {IdCaja : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCajaMoneda = function()
	{
		var translate_array = ['UI_CAJA_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CAJA_MONEDA,
	            dataTextField: "NombreLargoCajaMoneda",
	            dataValueField: "IdMoneda",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLargoCajaMoneda", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1028",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: { fields : {IdCaja : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};


	
	obj.getConfigComboboxCajaMonedaUsuario = function()
	{	
		var translate_array = ['UI_CAJA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CAJA_MONEDA,
	            dataTextField: "NombreLargoCaja",
	            dataValueField: "IdCaja",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLargoCaja", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1046",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: 
	            				{ fields : 
	            					{
	            						IdCaja    : { type:"number" },
	            						IdEmpresa : { type:"number" },
	            						IdMoneda  : { type:"string" },
	            						IdUsuario : { type:"number" }
	            					}   			
	            				}  			
	            			 }
	            })
			};
			return config;
		});
		return configPromise;
	};	
	

	obj.getConfigComboboxEstadoCuentaInversion = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1367",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoMovimientoEnTransito = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Custodia/XdSODATA/EstadosMovimientoEnTransito.xdsodata/EstadosMovTransito",// TODO
																													// eliminar
																													// configuracion
																													// ODATA
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxTipoTransaccion = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: 'Tipo Transacción', // falta diccionario
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoTransaccion',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1238",
	            			dataType:"json"
	            		}
	            	}
	            })
			
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxConceptoFacturacion = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "NombreConcepto",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1693",	            					
							dataType:"json"
	            		}
	            	},
	            	schema: { 
	            		model: { 
	            			fields : {
			            		IdConcepto				: { type:"string" },
			            		NombreConcepto			: { type:"string" },
			            		IdTipoOperacion         : { type:"string" },
			            		Origen                  : { type:"string" }, 
			            		IdEstado				: { type:"string" }			            		
	            			}   
	            		}  
	            	},
	                serverSorting: true,  // ordena al lado del servidor
            	    sort: { field: "NombreConcepto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoFacturacion = function()
	{
		var translate_array = ['UI_ESTADO_FACTURACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1239",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	var getConfigComboboxEstadoSocioNegocio = function()
	{
		var translate_array = ['UI_ESTADO_SOCIO_NEGOCIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
				dataTextField: 'NombreEstado',
				dataValueField: 'IdEstado',
				filter: "startswith",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,
					sort: { field: "NombreEstado", dir: "asc" },
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700420",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-estado-socio-negocio'] = getConfigComboboxEstadoSocioNegocio();
	
	obj.getConfigComboboxLibroOrdenes = function()
	{
		var translate_array = ['UI_LIBRO_ORDENES','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder : $translate.instant('UI_SELECCIONAR'),
	            dataTextField: "NombreLibroOrdenes",
	            dataValueField: "IdLibroOrdenes",      
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreLibroOrdenes", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1224",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
									IdSegmentoCliente :  { type: "number"},
									CodigoOperador: { type: "string"},
									IdLibroOrdenes: { type: "number"}
								}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoMovimientoFactura = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO, // falta diccionario
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1239",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEntidadClasificadoraRiesgo = function getConfigComboboxEntidadClasificadoraRiesgo() {
		return $translate([ 'UI_TODOS', 'UI_ENTIDAD_CLASIFICADORA_RIESGO' ]).then(function (translations) {
			return {
				placeholder: translations.UI_ENTIDAD_CLASIFICADORA_RIESGO,
				dataTextField: 'NombreEntidadClasificadoraRiesgo',
				dataValueField: 'IdEntidadClasificadoraRiesgo',
				filter: 'startswith',
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: 'odata',
					serverFiltering: true,
					serverSorting: true,
					sort: { field: 'NombreEntidadClasificadoraRiesgo', dir: 'asc' },
					transport: {
						read: {
							url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=3231',
							dataType: 'json',
						},
					},
				}),
			};
		});
	};

	obj.getConfigComboboxClasificacionRiesgo= function()
	{
		var translate_array = ['UI_CLASIFICACION_RIESGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION_RIESGO,
	            dataTextField: 'ClasificacionRiesgo',
	            dataValueField: 'IdClasificacionRiesgo',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting : true,
	            	sort: { field: "ClasificacionRiesgo", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1604",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};



	obj.getConfigComboboxClasificacionRiesgoTipoInstrumento= function()
	{
		var translate_array = ['UI_CLASIFICACION_RIESGO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION_RIESGO,
	            dataTextField: 'ClasificacionRiesgo',
	            dataValueField: 'IdClasificacionRiesgo',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting : true,
	            	sort: { field: "ClasificacionRiesgo", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3335",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTipoSocioNegocio= function()
	{
		var translate_array = ['UI_TIPO_SOCIO_NEGOCIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_SOCIO_NEGOCIO,
	            dataTextField: 'NombreTipoSocioDeNegocio',
	            dataValueField: 'CodigoTipoSocioDeNegocio',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1605",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoMensaje= function()
	{
		var translate_array = ['UI_TIPO_MENSAJE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_MENSAJE,
	            dataTextField: 'NombreTipoMensaje',
	            dataValueField: 'IdTipoMensaje',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700309",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoSegmento= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'DescripcionSegmento',
	            dataValueField: 'IdSegmentoCliente',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreSegmentoCliente", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1606",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxTipoDocumentoIdentidad= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoDocumentoIdentidad',
	            dataValueField: 'IdTipoDocumentoIdentidad',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1607",// cambiar
																			// a
																			// url
																			// definitiva
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoRelacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoRelacion',
	            dataValueField: 'IdTipoRelacion',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoRelacion", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1608",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEntidadDestinoRelacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'IdTipoEntidad',
	            dataValueField: 'IdTipoEntidad',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1609",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPersonaTipoRelacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
					placeholder: "...",
					dataTextField: 'IdPersona',
					dataValueField: 'IdPersona',
					filter: "contains",
					autoBind: false,
					minLength: minAutocomplete,
					dataSource: new kendo.data.DataSource({
						type: "odata",
						serverFiltering: false,
						sort: { field: "IdPersona", dir: "asc" },
		            	serverPaging: false,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1610",
								dataType:"json"
							}
						}
					})
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxTipoBanco= function()
	{
		var translate_array = ['UI_TIPO_BANCO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_BANCO,
				
	            dataTextField: 'NombreTipoBanco',
	            dataValueField: 'IdTipoBanco',
	            
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreTipoBanco", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1756",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};	

	
	obj.getConfigComboboxBanco= function()
	{
		var translate_array = ['UI_BANCO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_BANCO,
	            dataTextField: 'NombreBancoPais',
	            dataValueField: 'IdBanco',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreBanco", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1611",
							dataType:"json"
	            		}
	            	},
	            	schema 			: {
						model	: {
		                    fields	: {
		                    	IdBanco			: { type: "number",editable: false	},
		                    	NombreBancoPais	: { type: "string",editable: false 	},
	                        }
                   		}
					},
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxPlaza= function()
	{
		var translate_array = ['UI_PLAZA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PLAZA,
	            dataTextField: 'NombrePlaza',
	            dataValueField: 'IdPlaza',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombrePlaza", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1757",
							dataType:"json"
	            		}
	            	},
	            	schema 			: {
						model	: {
		                    fields	: {
		                    	PlazoRetencionMismoBanco	: { type: "number"},
		                    	PlazoRetencionOtroBanco		: { type: "number"},
		                    	PlazoMaximoCobroMismoBanco	: { type: "number"},
		                    	PlazoMaximoCobroOtroBanco	: { type: "number"},
		                    	IdPais		                : { type: "number"},
		                    	IdCiudad					: { type: "number"}
		                    	
	                        }
                   		}
					},
	            })
			};
			return config;
		});
		return configPromise;
	};	

	
	obj.getConfigComboboxTarjetaBancaria= function()
	{
		var translate_array = ['UI_TARJETA_BANCARIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TARJETA_BANCARIA,

	            dataTextField: 'NumeroTarjetaBancaria',
	            dataValueField: 'IdTarjetaBancaria',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            template: kendo.template("#= '['+NombreTipoTarjetaBancaria+'] '+NumeroTarjetaBancaria #"),
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreTipoTarjetaBancaria", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1612",
							dataType:"json"
	            		}
	            	},
	            	schema: { model: { fields : {IdPersona : { type:"number"  }}   }  }
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoTarjetaBanco= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoTarjetaBancaria',
	            dataValueField: 'IdTipoTarjetaBancaria',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreTipoTarjetaBancaria", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1612",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoCuentaBanco= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoCuentaBancaria',
	            dataValueField: 'IdTipoCuentaBancaria',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreTipoCuentaBancaria", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1613",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoCuenta= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoCuenta',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3579",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	obj.getConfigComboboxCuentasBancariasCorredora= function()
	{
		var translate_array = ['UI_CUENTAS_BANCARIAS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CUENTAS_BANCARIAS,
	            dataTextField: 'NombreCuentaBancaria',
	            dataValueField: 'IdCuentaBanco',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreCuentaBancaria", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1017",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
									IdBanco :        { type: "number"},
	            					IdCuentaBanco :  { type: "number"}
								}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxNumeroCuentasBancariasCorredora= function()
	{
		var translate_array = ['UI_NUMERO_CUENTAS_BANCARIAS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_NUMERO_CUENTAS_BANCARIAS,
	            dataTextField: 'NumeroCuentaBancaria',
	            dataValueField: 'IdCuentaBanco',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreCuentaBancaria", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1017",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
									IdBanco 		:	{ type: "number"},
	            					IdCuentaBanco 	:	{ type: "number"}
								}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxBancoCuentasBancariasCorredora= function()
	{
		var translate_array = ['UI_BANCO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_BANCO,
	            dataTextField: 'NombreBanco',
	            dataValueField: 'IdBanco',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreBanco", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {	            			
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1808",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
									IdBanco :  { type: "number"}
								}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxBancoCuentasBancariasCorredoraCuentas= function()
	{
		var translate_array = ['UI_BANCO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_BANCO,
	            dataTextField: 'BancoCuentaBancaria',
	            dataValueField: 'IdCuentaBanco',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreBanco", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {	            			
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4237",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
							fields: {
									IdBanco :  { type: "number"}
								}
						}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMonedaCuentaBanco= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreTipoMonedaCuenta',
	            dataValueField: 'IdTipoMonedaCuenta',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreTipoMonedaCuenta", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			// TODO mo existe tipo
							// Banco.xdsodata/TipoMonedaCuenta
	            			url: "/BSCL/Plataforma/XdSODATA/Banco.xdsodata/TipoMonedaCuenta",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};



	obj.getConfigComboboxTipoInstrumentoComoGarantia= function()
	{
		var translate_array = ['UI_TODOS','UI_TIPO_INSTRUMENTO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: 'NombreTipoInstrumentoAval',
	            dataValueField: 'IdTipoInstrumentoAval',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreTipoInstrumentoAval", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1614",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};




	obj.getConfigComboboxPais= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombrePais',
	            dataValueField: 'IdPais',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
						model: {
							fields: {
									IdPais :  { type: "number"}
									,NombrePais : { type: "string"}
									,CodigoPais : { type: "string"}
									,EsOcde : { type: "string"}
									,EsPaisUs : { type: "string"}
								}
						}
					},
	            	
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombrePais", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1615",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCalendario= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombrePais',
	            dataValueField: 'CodigoPais',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
						model: {
							fields: {
									IdPais :  { type: "number"}
									,NombrePais : { type: "string"}
									,CodigoPais : { type: "string"}
								}
						}
					},
	            	
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombrePais", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1615",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxGiro= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdGiro',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
						model: {
							fields: {
									Nombre :  { type: "string"},
									IdPais :  { type: "number"},
								}
						}
					},
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1616",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxEstadoCivil= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoEstadoCivil',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1617",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxRegimenConyugal= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoRegimenConyugal',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1618",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxProfesion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdProfesion',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1619",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

		obj.getConfigComboboxEstadoDireccionFisica= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1620",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxEstadoDireccionElectronica= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1621",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxEstadoCuentaBancaria= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1622",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	

	obj.getConfigComboboxEstadoTarjetaBancaria= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1623",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxEstadoNombreFantasia= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1624",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
			
	
	
	obj.getConfigComboboxEstadoTelefono= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1625",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxEstadoDocumentoIdentidad= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1626",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCiudad= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreCiudadPais',
	            dataValueField: 'IdCiudad',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
						model: {
							fields: {
									IdPais :  { type: "number"},
									IdCiudad: { type: "number"}
								}
						}
					},
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1627",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigDivisionPais= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreDivisionPaisClasificacion1',// 'NombreDivisionPais',
	            dataValueField: 'IdDivisionPais',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
						model: {
							fields: {
									IdPais 		  :	{ type: "number"},
									IdCiudad	  : { type: "number"},
									IdDivisionPais: { type: "number"}
								}
						}
					},
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1628",
							dataType:"json",
							data:{
								$select: "\"IdDivisionPais\", " +
										"\"NombreDivisionPais\", " +
										"\"NombreDivisionPaisClasificacion1\", " +
										"\"IdPais\", " +
										"\"IdCiudad\"," +
										"\"CodigoDivisionPais\"," +
										"\"IdClasificacionDivisionPais1\"," +
										"\"IdClasificacionDivisionPais2\""
							}
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxOrigenGarantia= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TODOS,
	            dataTextField: 'NombreOrigenGarantia',
	            dataValueField: 'IdOrigenGarantia',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreOrigenGarantia", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1629",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPerfilComercial= function()
	{
		var translate_array = ['UI_MODELO_COMERCIAL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MODELO_COMERCIAL,
	            dataTextField:  'IdPerfilComercialBase',
	            dataValueField: 'IdPerfilComercialBase',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPerfilComercialBase", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1630",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxApoderadoCliente= function()
	{
		var translate_array = ['UI_APODERADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_APODERADO,
	            dataTextField:  'NombreCompletoDestino',
	            dataValueField: 'DocumentoIdentidadDestino',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "FechaInicioRelacion", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1293",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxApoderadoCorredora= function()
	{
		var translate_array = ['UI_APODERADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_APODERADO,
	            dataTextField:  'NombreCompleto',
	            dataValueField: 'DocumentoIdentidad',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "NombreCompleto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700711",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAreaCliente = function()
	{
		var translate_array = ['UI_AREA_CLIENTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_AREA_CLIENTE,
	            dataTextField:  'Descripcion',
	            dataValueField: 'Codigo',
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "Codigo", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700262",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPerfilPlanComercial= function()
	{
		var translate_array = ['UI_MODELO_COMERCIAL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MODELO_COMERCIAL,
	            dataTextField:  'IdPerfilComercialBase',
	            dataValueField: 'IdPerfilComercialBase',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPerfilComercialBase", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601157",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPerfilProductos= function()
	{
		var translate_array = ['UI_PERFIL_PRODUCTOS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PERFIL_PRODUCTOS,
	            dataTextField:  'IdPerfilProductos',
	            dataValueField: 'IdPerfilProductos',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPerfilProductos", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1631",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPerfilCobros= function()
	{
		var translate_array = ['UI_PERFIL_COBROS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PERFIL_COBROS,
	            dataTextField:  'PerfilCobros',
	            dataValueField: 'IdPerfilCobros',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPerfilCobros", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1632",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxGrupoActivos= function()
	{
		var translate_array = ['UI_GRUPO_ACTIVOS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_GRUPO_ACTIVOS,
	            dataTextField:  'IdGrupoActivos',
	            dataValueField: 'IdGrupoActivos',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdGrupoActivos", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1539",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoGrupoActivos= function()
	{
		var translate_array = ['UI_TIPO_GRUPO_ACTIVOS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_GRUPO_ACTIVOS,
	            dataTextField:  'Nombre',
	            dataValueField: 'IdTipoGrupoActivos',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1633",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPlantillaCondicionesOperativas = function()
	{
		var translate_array = ['UI_PLANTILLA_CONDICIONES_OPERATIVAS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PLANTILLA_CONDICIONES_OPERATIVAS,
	            dataTextField:  'IdPlantillaCondicionesOperativas',
	            dataValueField: 'IdPlantillaCondicionesOperativas',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPlantillaCondicionesOperativas", dir: "asc" },
	            	transport: {
	            		read: {
	            			// TODO eliminar configuracion ODATA
	            			url: "/BSCL/Plataforma/XdSODATA/PerfilComercial.xdsodata/PlantillaCondicionesOperativas",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoComportamientoCondicion = function()
	{
		var translate_array = ['UI_TIPO_COMPORTAMIENTO_CONDICION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_COMPORTAMIENTO_CONDICION,
	            dataTextField:  'Nombre',
	            dataValueField: 'IdTipoComportamientoCondicion',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdTipoComportamientoCondicion", dir: "asc" },
	            	transport: {
	            		read: {
	            			// TODO eliminar configuracion ODATA
	            			url: "/BSCL/Plataforma/XdSODATA/PerfilComercial.xdsodata/TipoComportamientoCondicion",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	obj.getConfigComboboxModeloFacturacion= function()
	{
		var translate_array = ['UI_MODELO_FACTURACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MODELO_FACTURACION,
	            dataTextField:  'IdModeloFacturacion',
	            dataValueField: 'IdModeloFacturacion',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdModeloFacturacion", dir: "asc" },
	            	transport: {
	            		read: {
	            			// TODO eliminar configuracion ODATA
	            			url: "/BSCL/Plataforma/XdSODATA/PerfilComercial.xdsodata/ModeloFacturacion",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPerfilCondicionesOperativas= function()
	{
		var translate_array = ['UI_PERFIL_CONDICIONES_OPERATIVAS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PERFIL_CONDICIONES_OPERATIVAS,
	            dataTextField:  'IdPerfilCondicionesOperativas',
	            dataValueField: 'IdPerfilCondicionesOperativas',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPerfilCondicionesOperativas", dir: "asc" },
	            	transport: {
	            		read: {
	            			// TODO eliminar configuracion ODATA
	            			url: "/BSCL/Plataforma/XdSODATA/PerfilComercial.xdsodata/PerfilCondicionesOperativas",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPlantillaEmpleado= function()
	{
		var translate_array = ['UI_PLANTILLA_EMPLEADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField:  'IdPlantillaEmpleado',
	            dataValueField: 'IdPlantillaEmpleado',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPlantillaEmpleado", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1634",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPlantillaLimites= function()
	{
		var translate_array = ['UI_PLANTILLA_LIMITES','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PLANTILLA_LIMITES,
	            dataTextField:  'IdPlantillaLimite',
	            dataValueField: 'IdPlantillaLimite',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPlantillaLimite", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=362",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxEstadoPerfilComercial = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField:  'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdEstado", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1635",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEstadoGrupoActivos = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField:  'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdEstado", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13050",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getConfigEntidadDestinoGarantia = function()
	{
		var translate_array = ['UI_ENTIDAD_DESTINO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ENTIDAD_DESTINO,
	            dataTextField: "NombreEntidad",
	            dataValueField: "IdEntidadDestinoGarantia",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEntidad", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1768",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	obj.getConfigEntidadGarantia = function()
	{
		var translate_array = ['UI_ENTIDAD_DESTINO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ENTIDAD_DESTINO,
	            dataTextField: "NombreEntidad",
	            dataValueField: "IdContraparte",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEntidad", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1768",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxCategoriaInversionista = function()
	{
		var translate_array = ['UI_CATEGORIA_INVERSIONISTA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CATEGORIA_INVERSIONISTA,
	            dataTextField: "NombreCategoriaInversionista",
	            dataValueField: "IdCategoriaInversionista",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreCategoriaInversionista", dir: "asc" }, // campo
																					// y
																					// criterio
																					// de
																					// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1636",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxPlanCategoria = function()
	{
		var translate_array = ['UI_CATEGORIA_INVERSIONISTA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CATEGORIA_INVERSIONISTA,
	            dataTextField: "NombreCategoriaInversionista",
	            dataValueField: "IdCategoriaInversionista",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreCategoriaInversionista", dir: "asc" }, // campo
																					// y
																					// criterio
																					// de
																					// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601151",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxTipoPerfilFondoMutuo = function()
	{
		var translate_array = ['UI_CATEGORIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CATEGORIA_INVERSIONISTA,
	            dataTextField: "NombreCategoriaPerfilFondoMutuo",
	            dataValueField: "IdCategoriaPerfilFondoMutuo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Jerarquia", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3026",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCanalIngreso = function()
	{
		var translate_array = ['UI_CANAL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CANAL,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCanalIngreso",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoCanalIngreso", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1637",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	// -----------------------------------------------------------------------------------------
	var getConfigComboboxTipoProducto = function()
	{
		var config = {};
			// configuracion del combo
			config = {
				placeholder: $translate.instant("UI_SELECCIONE"),
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoProducto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1541",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				IdTipoProducto		 	: { type:"string"  },
	            			}
	            		}
	            	}
	            })
			};
			return config;
	};
	obj.configuracionCombos['combobox-tipo-producto'] = getConfigComboboxTipoProducto();
	// -----------------------------------------------------------------------------------------
	obj.getConfigComboboxEstadoLimites = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=361",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxGrupoEmisores = function()
	{
		var translate_array = ['UI_GRUPO_EMISORES','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_GRUPO_EMISORES,
	            dataTextField: "IdGrupoEmisor",
	            dataValueField: "IdGrupoEmisor",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdGrupoEmisor", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=412",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoLimite = function()
	{
		var translate_array = ['UI_TIPO_LIMITE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_LIMITE,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoLimite",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=423",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoLimiteCliente = function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_TIPO_LIMITE"),
            dataTextField: "Nombre",
            dataValueField: "IdTipoLimite",
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	type: "odata",
            	serverFiltering: false,
            	serverSorting: false,  // ordena al lado del servidor
            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
														// de ordenamiento
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700175",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};

	obj.getConfigComboboxEstadoEvento = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1402",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
obj.getConfigComboboxTipoEventoCapital = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_EVENTO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoEventoCapital",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1638",
							dataType:"json"																
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	
	//GCBSXNEC-1 jmedina_ext
	obj.getConfigComboboxSecuenciaRelacionada = function()
	{
		var translate_array = ['UI_SECUENCIA_RELACIONADA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SECUENCIA_RELACIONADA,
				dataTextField: "Descripcion",
				dataValueField: "Secuencia",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,
					sort: { field: "FechaIngreso", dir: "desc" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010109",
							dataType: "json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxTipoCartera = function()
	{
		var translate_array = ['UI_TIPO_CARTERA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARTERA,
	            dataTextField: "TipoCartera",
	            dataValueField: "IdTipoCartera",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "TipoCartera", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1639",
							dataType:"json"																
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxTipoRelacionInstrumento = function()
	{
		var translate_array = ['UI_TIPO_RELACION_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_RELACION_INSTRUMENTO,
	            dataTextField: "TipoRelacionInstrumento",
	            dataValueField: "IdTipoRelacionInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  									// ordena
																			// al
																			// lado
																			// del
																			// servidor
	            	sort: { field: "TipoRelacionInstrumento", dir: "asc" },	// campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1043",
							dataType:"json"																
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxTipoEvento = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoEvento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1399",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};



	obj.getConfigComboboxUsuario = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Usuario",
	            dataTextField: "CodigoUsuario",
	            dataValueField: "IdUsuario",
	            filter: "contains",
	            autoBind: false,
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "or",
				      filters: [
				         {"field": "toupper(CodigoUsuario)" ,"operator":"contains", "value": text.toUpperCase() },
				      ]
			    	};
					filter.push(nfilters);
				},	           	            
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "CodigoUsuario", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=384",
							dataType:"json"
	            		}
	            	}
	            })
			}; 
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxRol = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Rol",
	            dataTextField: "Nombre",
	            dataValueField: "IdRol",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1527",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigComboboxTipoPrestamo = function()
	{
		var translate_array = ['UI_TIPO_PRESTAMO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_PRESTAMO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdTipoPrestamo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1640",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoSocioDeNegocioCustodia = function()
	{
		var translate_array = ['UI_TIPO_CUSTODIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CUSTODIA,
	            dataTextField: "TipoSocioNegocio",
	            dataValueField: "IdTipoSocioDeNegocioCustodia",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "TipoSocioNegocio", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1641",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxSocioDeNegocio = function()
	{
		var translate_array = ['UI_DIGITAR_SOCIO_NEGOCIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
					var opcionesDeBusqueda = text.split(' ');	
					for (var i = 0; i < opcionesDeBusqueda.length; i++) {
						var nfilters = {
								logic: "or",
							    filters: [
							    	{"field": "toupper(NombreCompleto)" ,"operator":"contains", "value": opcionesDeBusqueda[i].toUpperCase() },
									{"field": "toupper(DocumentoIdentidadDefecto)" ,"operator":"startswith", "value": opcionesDeBusqueda[i].toUpperCase().replace(/\./g, "") }
							    ]
						};
						filter.push(nfilters);
					}
				},
				compareFn: function(x,y,row) {		
					x=x.toUpperCase().trim().replace(/\./g, "");
					y=y.toUpperCase().trim().replace(/\./g, "");
					if (y.indexOf(" [")>0) y=y.substring(1, y.indexOf(" [") - 1);
					if (x.indexOf(y)>=0 || y.indexOf(row.DocumentoIdentidadDefecto)===0) return true;
					return false;
				},
				changeFn: function(selected){
					if(sesion.getPreferencias("recordarClienteAutomatico")==true){
						sesion.setPreferencias("portafolioAutomatico", false);
						sesion.setPreferencias("SocioNegocioAutomatico", selected.IdSocioDeNegocio);
					}
				},
				permiteRepetidos : true,
				loadFn : function (scope) {
					// Pasamos el socio encadenado,
					var obj={};
					if (scope.cargaClienteAutomatico && sesion.getPreferencias("recordarCliente") && 
							sesion.getPreferencias("SocioNegocioRecordado") != 0 &&
							scope.idActual===false && 
							(scope.idDefecto===false ||scope.idDefecto===undefined)   ) { 
						obj.idDefecto = sesion.getPreferencias("SocioNegocioRecordado");  
						obj.autoCargar=true;
					}else 
						if(scope.cargaClienteAutomatico && sesion.getPreferencias("recordarClienteAutomatico") == true && 
								sesion.getPreferencias("SocioNegocioAutomatico")!=undefined &&
								sesion.getPreferencias("SocioNegocioAutomatico")!=0 && 
								scope.idActual===false &&
								(scope.idDefecto===false ||scope.idDefecto===undefined)){ 
							obj.idDefecto = sesion.getPreferencias("SocioNegocioAutomatico");
							obj.autoCargar=true;
					}
					return obj;
				},				
				placeholder: translations.UI_DIGITAR_SOCIO_NEGOCIO,
				KdataTextTemplate:  function(row) {
					var tipos="";
					if (row.EsCliente !='UI_SI'      && row.EsEmpleado !='UI_SI'  && 
						    row.EsOperador !='UI_SI' && row.EsProveedor!='UI_SI'  && 
						    row.EsContraparte!='UI_SI' ) {
						tipos="[" + $translate.instant('UI_PERSONA') + (row.EstadoPersona=='ACT' ? "]" : ",X]");						
					}else {
						tipos="[";
						if (row.EsCliente == 'UI_SI' )     tipos+=(row.EstadoCliente =='ACT'     ?  "C |" : "CX|");						
						else tipos+='--|';
						if (row.EsEmpleado == 'UI_SI' )    tipos+=(row.EstadoEmpleado =='ACT'    ?  "E |" : "EX|");						
						else tipos+='--|';
						if (row.EsOperador == 'UI_SI' )    tipos+=(row.EstadoOperador =='ACT'    ?  "O |" : "OX|");						
						else tipos+='--|';
						if (row.EsProveedor == 'UI_SI' )   tipos+=(row.EstadoProveedor =='ACT'   ?  "P |" : "PX|");						
						else tipos+='--|';			
						if (row.EsContraparte == 'UI_SI' ) tipos+=(row.EstadoContraparte =='ACT' ?  "CP " : "CPX");						
						else tipos+='--';						
						tipos+="]";							
					}	
					tipos=tipos.indexOf("X")>-1?" [X]":"";
					if (row.IdTipoDocumentoIdentidadDefecto == 'R') {
						return row.DocumentoIdentidadDefecto + ' '+row.NombreCompleto+tipos;
					}else {
						return row.DocumentoIdentidadDefecto + ' '+row.NombreCompleto+tipos;
					}
				},
	            dataTextField: "KdataTextTemplate",
	            dataValueField: "IdSocioDeNegocio",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "NombreCompleto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3541",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    }	 	            		
	            	},
					schema: {
						model: {
		                    fields: {
	                       		IdSocioDeNegocio: { type: "number"},
	                       		IdEmpresa: { type: "number"},
	                       		IdPersona: { type: "number"},
	                       		IdCliente: { type: "number"},
	                       		IdProveedor: { type: "number"},
	                       		IdOperador: { type: "number"},
	                       		IdContraparte: { type: "number"},
	                       		FechaIngreso: { type: "date"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getConfigComboboxEmpleador = function()
	{
		var translate_array = ['UI_DIGITAR_EMPLEADOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "or",
				      filters: [
				         {"field": "toupper(NombreCompleto)" ,"operator":"contains", "value": text.toUpperCase() },
				         {"field": "toupper(DocumentoIdentidad)" ,"operator":"startswith", "value": text.toUpperCase().replace(/\./g, "") },
				      ]
			    	};
					filter.push(nfilters);
				},
				permiteRepetidos : true,			
				placeholder: translations.UI_DIGITAR_PERSONA,
				KdataTextTemplate:  function(row) {
					var tipos="";
					if (row.EsCliente !='UI_SI'      && row.EsEmpleado !='UI_SI'  && 
						    row.EsOperador !='UI_SI' && row.EsProveedor!='UI_SI'  && 
						    row.EsContraparte!='UI_SI' ) {
						tipos="[" + $translate.instant('UI_PERSONA') + (row.EstadoPersona=='ACT' ? "]" : ",X]");						
					}else {
						tipos="[";
						if (row.EsCliente == 'UI_SI' )     tipos+=(row.EstadoCliente =='ACT'     ?  "C |" : "CX|");						
						else tipos+='--|';
						if (row.EsEmpleado == 'UI_SI' )    tipos+=(row.EstadoEmpleado =='ACT'    ?  "E |" : "EX|");						
						else tipos+='--|';
						if (row.EsOperador == 'UI_SI' )    tipos+=(row.EstadoOperador =='ACT'    ?  "O |" : "OX|");						
						else tipos+='--|';
						if (row.EsProveedor == 'UI_SI' )   tipos+=(row.EstadoProveedor =='ACT'   ?  "P |" : "PX|");						
						else tipos+='--|';			
						if (row.EsContraparte == 'UI_SI' ) tipos+=(row.EstadoContraparte =='ACT' ?  "CP " : "CPX");						
						else tipos+='--';						
						tipos+="]";							
					}	
					tipos=tipos.indexOf("X")>-1?" [X]":"";
					if (row.IdTipoDocumentoIdentidad == 'R') {
						return row.DocumentoIdentidad + ' '+row.NombreCompleto+tipos;
					}else {
						return row.DocumentoIdentidad + ' '+row.NombreCompleto+tipos;
					}
				},
				dataTextField: "KdataTextTemplate",
	            dataValueField: "IdPersona",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "NombreCompleto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6027",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    }	 	            		
	            	},
					schema: {
						model: {
		                    fields: {
	                       		IdPersona			: { type: "number"},
	                       		NombreCompleto		: { type: "string"},
	                       		DocumentoIdentidad	: { type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxClientes = function()
	{
		var translate_array = ['UI_DIGITAR_CLIENTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "or",
				      filters: [
				         {"field": "toupper(NombreCompleto)" ,"operator":"contains", "value": text.toUpperCase() },
				         {"field": "toupper(DocumentoIdentidad)" ,"operator":"startswith", "value": text.toUpperCase().replace(/\./g, "") },
				      ]
			    	};
					filter.push(nfilters);
				},
				permiteRepetidos : true,			
				placeholder: translations.UI_DIGITAR_PERSONA,
				KdataTextTemplate:  function(row) {
					var tipos="";
					if (row.EsCliente !='UI_SI'      && row.EsEmpleado !='UI_SI'  && 
						    row.EsOperador !='UI_SI' && row.EsProveedor!='UI_SI'  && 
						    row.EsContraparte!='UI_SI' ) {
						tipos="[" + $translate.instant('UI_PERSONA') + (row.EstadoPersona=='ACT' ? "]" : ",X]");						
					}else {
						tipos="[";
						if (row.EsCliente == 'UI_SI' )     tipos+=(row.EstadoCliente =='ACT'     ?  "C |" : "CX|");						
						else tipos+='--|';
						if (row.EsEmpleado == 'UI_SI' )    tipos+=(row.EstadoEmpleado =='ACT'    ?  "E |" : "EX|");						
						else tipos+='--|';
						if (row.EsOperador == 'UI_SI' )    tipos+=(row.EstadoOperador =='ACT'    ?  "O |" : "OX|");						
						else tipos+='--|';
						if (row.EsProveedor == 'UI_SI' )   tipos+=(row.EstadoProveedor =='ACT'   ?  "P |" : "PX|");						
						else tipos+='--|';			
						if (row.EsContraparte == 'UI_SI' ) tipos+=(row.EstadoContraparte =='ACT' ?  "CP " : "CPX");						
						else tipos+='--';						
						tipos+="]";							
					}	
					tipos=tipos.indexOf("X")>-1?" [X]":"";
					if (row.IdTipoDocumentoIdentidad == 'R') {
						return row.DocumentoIdentidad + ' '+row.NombreCompleto+tipos;
					}else {
						return row.DocumentoIdentidad + ' '+row.NombreCompleto+tipos;
					}
				},
				dataTextField: "KdataTextTemplate",
	            dataValueField: "IdPersona",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "NombreCompleto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1275",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    }	 	            		
	            	},
					schema: {
						model: {
		                    fields: {
	                       		IdPersona			: { type: "number"},
	                       		NombreCompleto		: { type: "string"},
	                       		DocumentoIdentidad	: { type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPropositoPortafolio = function()
	{
		var translate_array = ['UI_PROPOSITO_PORTAFOLIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PROPOSITO_PORTAFOLIO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdPropositoPortafolio",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdPropositoPortafolio", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1642",
							dataType:"json"
	            		}
	            	},
					schema: {
						model: {
		                    fields: {
	                       		IdPortafolio: { type: "number"},		                    	
	                       		IdCliente: { type: "number"},
	                        }
                   		}
					}		            	
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPortafolio= function()
	{
		var translate_array = ['UI_PORTAFOLIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				enlace : {
					ventana: 'administrarPortafolio',
					parametros : [ 'IdCliente', 'IdPortafolio' ]
				},
				placeholder: translations.UI_PORTAFOLIO,
				KdataTextTemplate:  function(row) {
					if (row.KdataTextTemplate && row.KdataTextTemplate.trim() == $translate.instant('UI_TODOS') ) return $translate.instant('UI_TODOS')
					return '(' + row.CodigoPortafolio + ') - '+row.Proposito;
				},
				changeFn : function(selected){
					if(sesion.getPreferencias("recordarClienteAutomatico")==true){
						sesion.setPreferencias("portafolioAutomatico", selected.IdPortafolio);	
					}else{
						sesion.setPreferencias("portafolioAutomatico", false);	
					}
				},
				loadFn : function (scope) {
					// Pasamos el portafolio encadenado
					var obj={};
					if (scope.cargaClienteAutomatico && sesion.getPreferencias("recordarCliente") && sesion.getPreferencias("portafolioRecordado") != 0) { 
						obj.idDefecto = sesion.getPreferencias("portafolioRecordado");  
					}else if(sesion.getPreferencias("recordarClienteAutomatico")==true && sesion.getPreferencias("portafolioAutomatico")!=false){
						obj.idDefecto = sesion.getPreferencias("portafolioAutomatico");  
					}
					return obj;
				},						
	            dataTextField: "KdataTextTemplate",
	            dataValueField: "IdPortafolio",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "CodigoPortafolio", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1643",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    },	 		            		
		        		data : {
		        			$top : 500,
		        		} 
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdPortafolio: { type: "number"},		                    	
	                       		IdCliente: { type: "number"},
	                       		IdEmpresaCuentaPropia: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxPortafolioTraspaso= function()
	{
		var translate_array = ['UI_PORTAFOLIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				enlace : {
					ventana: 'administrarPortafolio',
					parametros : [ 'IdCliente', 'IdPortafolio' ]
				},
				placeholder: translations.UI_PORTAFOLIO,
				KdataTextTemplate:  function(row) {
					if (row.KdataTextTemplate && row.KdataTextTemplate.trim() == $translate.instant('UI_TODOS') ) return $translate.instant('UI_TODOS')
					return '(' + row.CodigoPortafolio + ') - '+row.Proposito+ ' '+row.NombreAgrupador;
				},
				changeFn : function(selected){
					if(sesion.getPreferencias("recordarClienteAutomatico")==true){
						sesion.setPreferencias("portafolioAutomatico", selected.IdPortafolio);	
					}else{
						sesion.setPreferencias("portafolioAutomatico", false);	
					}
				},
				loadFn : function (scope) {
					// Pasamos el portafolio encadenado
					var obj={};
					if (scope.cargaClienteAutomatico && sesion.getPreferencias("recordarCliente") && sesion.getPreferencias("portafolioRecordado") != 0) { 
						obj.idDefecto = sesion.getPreferencias("portafolioRecordado");  
					}else if(sesion.getPreferencias("recordarClienteAutomatico")==true && sesion.getPreferencias("portafolioAutomatico")!=false){
						obj.idDefecto = sesion.getPreferencias("portafolioAutomatico");  
					}
					return obj;
				},						
	            dataTextField: "KdataTextTemplate",
	            dataValueField: "IdPortafolio",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "CodigoPortafolio", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1643",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    },	 		            		
		        		data : {
		        			$top : 500,
		        		} 
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdPortafolio: { type: "number"},		                    	
	                       		IdCliente: { type: "number"},
	                       		IdEmpresaCuentaPropia: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	var getConfigComboboxCliente = function()
	{
			var config = {};
			config = {
				autocomplete: true,
				defLupa :  { 
						ventana : 'AgenteConsultaAvanzadaClientes',
						parametros : { modo :"SELECCION",tipo:"CLIENTE" }
				},
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "or",
				      filters: [
				         {"field": "toupper(NombreCompleto)" ,"operator":"contains", "value": text.toUpperCase() },
				      ]
			    	};
					filter.push(nfilters);
				},
				/*
				 * enlace : { ventana: 'administrarPortafolio', parametros : [
				 * 'IdCliente', 'IdPortafolio' ] },
				 */
				placeholder: $translate.instant("UI_CLIENTE"),
				dataTextField: "NombreCompleto",
	            dataValueField: "IdCliente",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "NombreCompleto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3250",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";
	                    	obj.$top = 100;
	                    	obj.$handler = util.parseHandler({p_IdEmpresa : $rootScope.SesionIdEmpresa, p_IdUsuario : $rootScope.SesionIdUsuario, p_IdModulo : 'PLA', p_IdEmpleado : $rootScope.SesionIdEmpleado, });
	                        return  obj;
	                    } 
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       			                    	
								   IdCliente: { type: "number"},
								   IdPortafolio: {type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
	};
	obj.configuracionCombos['combobox-cliente'] = getConfigComboboxCliente();
	
	obj.getConfigComboboxClientePortafolioSinFiltro = function() {
		return obj.getConfigComboboxClientePortafolio('S');
	}
	
	obj.getConfigComboboxClientePortafolioSpot = function(ignorafiltros)
	{
		var translate_array = ['UI_PORTAFOLIO','UI_CLIENTE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
					var opcionesDeBusqueda = text.split(' ');	
					for (var i = 0; i < opcionesDeBusqueda.length; i++) {
						var nfilters = {
								logic: "or",
							    filters: [
							    	{"field": "toupper(NombreCompletoPortafolio)" ,"operator":"contains", "value": ignorafiltros == 'S' ? '' : opcionesDeBusqueda[i].toUpperCase() } 
							    ]
						};
						filter.push(nfilters);
					}
				},
				defLupa :  { 
					ventana : 'AgenteConsultaAvanzadaClientes',
					parametros : { modo :"SELECCION",tipo:"PORTAFOLIO" }
				},
				enlace : {
					ventana: 'administrarPortafolio',
					parametros : [ 'IdCliente', 'IdPortafolio' ]
				},
				permiteRepetidos : true,
				template : '<span ng-class="{\'red-font\': (dataItem.IdEstado !== \'ACT\' || dataItem.IdEstadoCliente !== \'ACT\')}">#:data.NombreCompletoPortafolio#</span>',
				placeholder: translations.UI_CLIENTE + " - " + translations.UI_PORTAFOLIO,
				changeFn : function(selected){
					if(sesion.getPreferencias("recordarClienteAutomatico")==true){
						sesion.setPreferencias("portafolioAutomatico", selected.IdPortafolio);	
					}else{
						sesion.setPreferencias("portafolioAutomatico", false);	
					}
				},
				loadFn : function (scope) {
					// Pasamos el portafolio encadenado
					var obj={};
					if (sesion.getPreferencias("recordarCliente") && sesion.getPreferencias("portafolioRecordado") != 0) { 
						obj.idDefecto = sesion.getPreferencias("portafolioRecordado");  
					}else if(sesion.getPreferencias("recordarClienteAutomatico")==true && sesion.getPreferencias("portafolioAutomatico")!=false){
						obj.idDefecto = sesion.getPreferencias("portafolioAutomatico");  
					}
					return obj;
				},			
	            dataTextField: "NombreCompletoPortafolio",
	            dataValueField: "IdPortafolio",
	            fnIconoAlerta : function() {
	            	return this.seleccionado && (this.seleccionado.IdEstado !== 'ACT' || this.seleccionado.IdEstadoCliente !== 'ACT');
	            },
	            tooltipTextAlerta : function() {
				    if (this.seleccionado) {
				        if (this.seleccionado.IdEstadoCliente && this.seleccionado.IdEstadoCliente !== 'ACT') {
				            return "Cliente bloqueado: "+this.seleccionado.NombreTipoBloqueoCliente;
				        } else if (this.seleccionado.IdEstado && this.seleccionado.IdEstado !== 'ACT') {
				            return "Portafolio bloqueado: "+this.seleccionado.NombreTipoBloqueo;
				        }
				    }
				    return "";
				},
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{field: "NombreCompleto", dir: "asc" },{field: "CodigoPortafolioPad", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12102",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";
	                    	obj.$top = 100;
	                    	obj.$handler = util.parseHandler({
	                    		p_IdEmpresa : $rootScope.SesionIdEmpresa, 
	                    		p_IdUsuario : $rootScope.SesionIdUsuario, 
	                    		p_IdModulo : 'PLA', 
	                    		p_IdEmpleado : $rootScope.SesionIdEmpleado, 
	                    		p_IgnoraFiltro : ignorafiltros?'S':'N', 
	                    	});
	                    	
	                        return  obj;
	                    } 
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdPortafolio: { type: "number"},		                    	
	                       		IdCliente: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxClientePortafolioContrato = function(ignorafiltros)
	{
		var translate_array = ['UI_PORTAFOLIO','UI_CLIENTE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
					var opcionesDeBusqueda = text.split(' ');	
					for (var i = 0; i < opcionesDeBusqueda.length; i++) {
						var nfilters = {
								logic: "or",
							    filters: [
							    	{"field": "toupper(NombreCompletoPortafolio)" ,"operator":"contains", "value": ignorafiltros == 'S' ? '' : opcionesDeBusqueda[i].toUpperCase() } 
							    ]
						};
						filter.push(nfilters);
					}
				},
				defLupa :  { 
					ventana : 'AgenteConsultaAvanzadaClientes',
					parametros : { modo :"SELECCION",tipo:"PORTAFOLIO" }
				},
				enlace : {
					ventana: 'administrarPortafolio',
					parametros : [ 'IdCliente', 'IdPortafolio', 'TipoEnrolamiento', 'TipoPersona','CodigoPortafolio' ]
				},
				permiteRepetidos : true,
				template : '<span ng-class="{\'red-font\': (dataItem.IdEstado !== \'ACT\' || dataItem.IdEstadoCliente !== \'ACT\')}">#:data.NombreCompletoPortafolio#</span>',
				placeholder: translations.UI_CLIENTE + " - " + translations.UI_PORTAFOLIO,
				changeFn : function(selected){
					if(sesion.getPreferencias("recordarClienteAutomatico")==true){
						sesion.setPreferencias("portafolioAutomatico", selected.IdPortafolio);	
					}else{
						sesion.setPreferencias("portafolioAutomatico", false);	
					}
				},
				loadFn : function (scope) {
					// Pasamos el portafolio encadenado
					var obj={};
					if (sesion.getPreferencias("recordarCliente") && sesion.getPreferencias("portafolioRecordado") != 0) { 
						obj.idDefecto = sesion.getPreferencias("portafolioRecordado");  
					}else if(sesion.getPreferencias("recordarClienteAutomatico")==true && sesion.getPreferencias("portafolioAutomatico")!=false){
						obj.idDefecto = sesion.getPreferencias("portafolioAutomatico");  
					}
					return obj;
				},			
	            dataTextField: "NombreCompletoPortafolio",
	            dataValueField: "IdPortafolio",
	            fnIconoAlerta : function() {
	            	return this.seleccionado && (this.seleccionado.IdEstado !== 'ACT' || this.seleccionado.IdEstadoCliente !== 'ACT');
	            },
	            tooltipTextAlerta : function() {
					            if (this.seleccionado) {
					                if (this.seleccionado.IdEstadoCliente && this.seleccionado.IdEstadoCliente !== 'ACT') {
					                    return "Cliente bloqueado: "+this.seleccionado.NombreTipoBloqueoCliente;
					                } else if (this.seleccionado.IdEstado && this.seleccionado.IdEstado !== 'ACT') {
					                    return "Portafolio bloqueado: "+this.seleccionado.NombreTipoBloqueo;
					                }
					            }
					            return "";
					        },
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{field: "NombreCompleto", dir: "asc" },{field: "CodigoPortafolioPad", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010266",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";
	                    	obj.$top = 100;
	                    	obj.$handler = util.parseHandler({
	                    		p_IdEmpresa : $rootScope.SesionIdEmpresa, 
	                    		p_IdUsuario : $rootScope.SesionIdUsuario, 
	                    		p_IdModulo : 'PLA', 
	                    		p_IdEmpleado : $rootScope.SesionIdEmpleado, 
	                    		p_IgnoraFiltro : ignorafiltros?'S':'N', 
	                    	});
	                    	
	                        return  obj;
	                    } 
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdPortafolio: { type: "number"},		                    	
	                       		IdCliente: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxClientePortafolio = function(ignorafiltros)
	{
		var translate_array = ['UI_PORTAFOLIO','UI_CLIENTE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
					var opcionesDeBusqueda = text.split(' ');	
					for (var i = 0; i < opcionesDeBusqueda.length; i++) {
						var nfilters = {
								logic: "or",
							    filters: [
							    	{"field": "toupper(NombreCompletoPortafolio)" ,"operator":"contains", "value": ignorafiltros == 'S' ? '' : opcionesDeBusqueda[i].toUpperCase() } 
							    ]
						};
						filter.push(nfilters);
					}
				},
				defLupa :  { 
					ventana : 'AgenteConsultaAvanzadaClientes',
					parametros : { modo :"SELECCION",tipo:"PORTAFOLIO" }
				},
				enlace : {
					ventana: 'administrarPortafolio',
					parametros : [ 'IdCliente', 'IdPortafolio', 'TipoEnrolamiento', 'TipoPersona','CodigoPortafolio' ]
				},
				permiteRepetidos : true,
				template : '<span ng-class="{\'red-font\': (dataItem.IdEstado !== \'ACT\' || dataItem.IdEstadoCliente !== \'ACT\')}">#:data.NombreCompletoPortafolio#</span>',
				placeholder: translations.UI_CLIENTE + " - " + translations.UI_PORTAFOLIO,
				changeFn : function(selected){
					if(sesion.getPreferencias("recordarClienteAutomatico")==true){
						sesion.setPreferencias("portafolioAutomatico", selected.IdPortafolio);	
					}else{
						sesion.setPreferencias("portafolioAutomatico", false);	
					}
				},
				loadFn : function (scope) {
					// Pasamos el portafolio encadenado
					var obj={};
					if (sesion.getPreferencias("recordarCliente") && sesion.getPreferencias("portafolioRecordado") != 0) { 
						obj.idDefecto = sesion.getPreferencias("portafolioRecordado");  
					}else if(sesion.getPreferencias("recordarClienteAutomatico")==true && sesion.getPreferencias("portafolioAutomatico")!=false){
						obj.idDefecto = sesion.getPreferencias("portafolioAutomatico");  
					}
					return obj;
				},			
	            dataTextField: "NombreCompletoPortafolio",
	            dataValueField: "IdPortafolio",
	            fnIconoAlerta : function() {
	            	return this.seleccionado && (this.seleccionado.IdEstado !== 'ACT' || this.seleccionado.IdEstadoCliente !== 'ACT');
	            },      
	            tooltipTextAlerta : function() {
				    if (this.seleccionado) {
				        if (this.seleccionado.IdEstadoCliente && this.seleccionado.IdEstadoCliente !== 'ACT') {
				            return "Cliente bloqueado: "+this.seleccionado.NombreTipoBloqueoCliente;
				        } else if (this.seleccionado.IdEstado && this.seleccionado.IdEstado !== 'ACT') {
				            return "Portafolio bloqueado: "+this.seleccionado.NombreTipoBloqueo;
				        }
				    }
				    return "";
				},
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{field: "NombreCompleto", dir: "asc" },{field: "CodigoPortafolioPad", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12101",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";
	                    	obj.$top = 100;
	                    	obj.$handler = util.parseHandler({
	                    		p_IdEmpresa : $rootScope.SesionIdEmpresa, 
	                    		p_IdUsuario : $rootScope.SesionIdUsuario, 
	                    		p_IdModulo : 'PLA', 
	                    		p_IdEmpleado : $rootScope.SesionIdEmpleado, 
	                    		p_IgnoraFiltro : ignorafiltros?'S':'N', 
	                    	});
	                    	
	                        return  obj;
	                    } 
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdPortafolio: { type: "number"},		                    	
	                       		IdCliente: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoProcesos= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdTipoPrograma',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Descripcion", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1644",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxModalidadDePago= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TODOS,
	            dataTextField: 'Nombre',
	            dataValueField: 'IdModalidadDePago',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdModalidadDePago", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1645",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCargaMasiva = function()
	{
		var translate_array = ['UI_TIPO_CARGA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TODOS,
	            dataTextField: 'Codigo',
	            dataValueField: 'IdTipoCarga',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoCarga", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1646",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxModoCargaMasiva = function()
	{
		var translate_array = ['UI_MODO_CARGA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TODOS,
	            dataTextField: 'Codigo',
	            dataValueField: 'IdModoCarga',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdModoCarga", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1647",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxTipoLog = function (){
		var translate_array = ['UI_TIPO_LOG'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_LOG,
	            dataTextField: "DescripcionTipoLog",
	            dataValueField: "IdTipoLog",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoLog", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1648",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoRegistro = function (){
		var translate_array = ['UI_TIPO_REGISTRO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_REGISTRO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoRegistro",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoRegistro", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1649",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPersonaDireccionFisica = function (){
		var translate_array = ['UI_DIRECCION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_DIRECCION,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdDireccionFisica",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort:[ { field: "Predeterminado", dir: "desc" }]	,// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1650",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxaDireccionContrato = function (){
		var translate_array = ['UI_DIRECCION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_DIRECCION,
	            dataTextField: "DireccionCompleta",
	            dataValueField: "IdRelacion",
	            filter:"contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdRelacion", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290614",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxPersonaDireccionElectronica = function (){
		var translate_array = ['UI_EMAIL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_EMAIL,
	            dataTextField: "Direccion",
	            dataValueField: "IdDireccionElectronica",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Direccion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1651",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPersonaGrupoYDireccionElectronica = function (){
		var translate_array = ['UI_EMAIL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_EMAIL,
	            dataTextField: "Nombre",
	            dataValueField: "IdDireccion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3132",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxEnrolamientoDireccionElectronica = function () {
		return $translate([ 'UI_EMAIL', 'UI_TODOS' ])
			.then((translations) => {
				return {
					placeholder   : translations.UI_EMAIL,
					dataTextField : 'Direccion',
					dataValueField: 'IdDireccionElectronica',
					filter        : 'contains',
					autoBind      : false,
					minLength     : minAutocomplete,
					dataSource    : new kendo.data.DataSource({
						type           : 'odata',
						serverFiltering: false,
						serverSorting  : false,
						sort           : {
							field: 'Direccion',
							dir  : 'asc',
						},
						transport      : {
							read: {
								url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=700205'
							}
						}
					})
				};
			});
	}

	obj.getConfigComboboxPersonaTelefono = function (){
		var translate_array = ['UI_TELEFONO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TELEFONO,
	            dataTextField: "Numero",
	            dataValueField: "IdTelefono",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Numero", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1652",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPersonaTarjetaBancaria = function (){
		var translate_array = ['UI_TARJETA_BANCARIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TARJETA_BANCARIA,
	            dataTextField: "NombreTarjetaBancariaFull",
	            dataValueField: "IdTarjetaBancaria",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTarjetaBancariaFull", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1653",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		// IdPortafolio: { type: "number"},
	                       		// IdCliente: { type: "number"},
	                       		IdPersona: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEnrolamientoTarjetaBancaria = function (){
		var translate_array = ['UI_TARJETA_BANCARIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TARJETA_BANCARIA,
	            dataTextField: "NombreTarjetaBancariaFull",
	            dataValueField: "IdTarjetaBancaria",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "NombreTarjetaBancariaFull", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700210",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdEnrolamiento: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPersonaCuentaBancaria = function (){
		var translate_array = ['UI_CUENTA_BANCARIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				// placeholder: translations.UI_CUENTA_BANCARIA,
				placeholder: "...",	
	            dataTextField: "NombreCuentaBancariaFull",
	            dataValueField: "IdCuentaBancaria",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreCuentaBancariaFull", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1310",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		// IdPortafolio: { type: "number"},
	                       		// IdCliente: { type: "number"},
	                       		IdPersona: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEnrolamientoCuentaBancaria = function (){
		var translate_array = ['UI_CUENTA_BANCARIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",	
	            dataTextField: "NombreCuentaBancariaFull",
	            dataValueField: "IdCuentaBancaria",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "NombreCuentaBancariaFull", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700212",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdEnrolamiento: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxDetalleTipoAtributo = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: "DetalleIndicador",
	            dataValueField: "IdDetalleIndicador",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdDetalleIndicador", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1549",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.configuracionCombos['combobox-tipo-cliente-cmf'] = obj.getConfigComboboxDetalleTipoAtributo();

	obj.getConfigComboboxTipoNotificacionCarta = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: "DetalleIndicador",
	            dataValueField: "IdDetalleIndicador",
	            filter: { field : "IdTipoIndicador", operator : "eq", value : 74},
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	filter: [{ field : 'IdTipoIndicador', operator : 'eq', value : 74 }],
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "DetalleIndicador", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1549",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxDetalleTipoAtributoEntidad = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: "ValorIndicador",
	            dataValueField: "IdEntidad",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "ValorIndicador", dir: "asc" }, // campo
																		// y.
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1814",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		
		return configPromise;
	};

	obj.getConfigComboboxDetalleTipoAtributoEntidadJson = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: "CodigoDetalleIndicador",
	            dataValueField: "ValorIndicador",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CodigoDetalleIndicador", dir: "asc" }, // campo
																		// y.
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=100063",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	
	obj.getConfigComboboxProductoEmpresa = function()
	{
		var translate_array = ['UI_TIPO_PRODUCTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_PRODUCTO,
	            dataTextField: "NombreTipoProducto",
	            dataValueField: "IdProductoEmpresa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdProductoEmpresa", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1654",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoDatoAtributo = function()
	{
		var translate_array = ['UI_TIPO_DATO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_DATO,
	            dataTextField: "NombreTipoDato",
	            dataValueField: "TipoDato",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoDato", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1655",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoIndicador = function()
	{
		var translate_array = ['UI_TIPO_INDICADOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_DATO,
	            dataTextField: "NombreTipoIndicador",
	            dataValueField: "IdTipoIndicador",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoIndicador", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1344",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxGrupoIndicador = function()
	{
		var translate_array = ['UI_GRUPO_INDICADOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_DATO,
	            dataTextField: "GrupoIndicador",
	            dataValueField: "GrupoIndicador",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "GrupoIndicador", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1344",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoTipoAtributo = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1656",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxNombreFantasia = function()
	{
		var translate_array = ['UI_NOMBRE_FANTASIA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_NOMBRE_FANTASIA,
	            dataTextField: "DescripcionNombreFantasia",
	            dataValueField: "IdNombreFantasia",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	schema: {
	            		model: {
	            			fields : {
	            				 IdPersona : { type:"number" } ,
	            				 IdNombreFantasia : { type:"number" } ,
	            			}
	            		}
	            	},
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1657",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxConfiguracionChequeras = function()
	{
		var translate_array = ['UI_NOMBRE_CONFIGURACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_NOMBRE_CONFIGURACION,
	            dataTextField: "NombreConfChequera",
	            dataValueField: "IdConfiguracionChequera",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1019",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxChequerasSeries = function()
	{
		var translate_array = ['UI_SERIES','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SERIES,
	            dataTextField: "Serie",
	            dataValueField: "IdChequera",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1018",
							dataType:"json"
	            		}
	            	},
	            	schema: {
						model: {
		                    fields: {
	                       		IdChequera: { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoGrupo= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1212",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	// CELZORRO-464 - Grupos de Clientes (Cartola)
	obj.getConfigComboboxEstadoGrupoClientesActivos= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: $translate.instant("UI_GRUPOS_DE_CLIENTES"),
	            dataTextField: 'NombreGrupoPortafolio',
	            dataValueField: 'IdGrupo',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700014",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	// CELZORRO-461 - Grupos de Clientes
	obj.getConfigComboboxEstadoGrupoClientes= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700009",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	var getConfigComboboxGrupoClientes= function()
	{
		var config = {};
		// configuracion del combo
		config = {
			placeholder: $translate.instant("UI_SELECCIONE_GRUPO"),
            dataTextField: 'Nombre',
            dataValueField: 'IdGrupo',
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            dataSource: new kendo.data.DataSource({
            	schema: {
            		model: {
            			fields : {
            				 IdGrupo : { type:"number"} ,
            				 Nombre:	{type:"string"},
            				 IdEstado:	{type:"string"}
            			}
            		}
            	},
            	type: "odata",
            	transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1210",
						dataType:"json"
            		}
            	}
            })
		};
		return config;
	};
	obj.configuracionCombos['combobox-grupos-clientes'] = getConfigComboboxGrupoClientes();
	// -----------------------------------------------------------------------------------------
	
	obj.getConfigComboboxFormaDespacho= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Nombre',
	            dataValueField: 'CodigoFormaDespacho',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1497",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTiempoDespacho= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Nombre',
	            dataValueField: 'CodigoTiempoDespacho',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "desc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1460",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTiempoDespachoPrincipal= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Nombre',
	            dataValueField: 'CodigoTiempoDespacho',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1463",
							dataType:"json"
	            		}
	            	}
 	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxEnviado= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'EnviadoUI',
	            dataValueField: 'Enviado',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "EnviadoUI", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1495",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTipoEventoNotificacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Descripcion',
	            dataValueField: 'CodigoTipoEvento',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoTipoEvento", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1496",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPlantillaNotificacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdPlantilla',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdPlantilla", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1498",
							dataType:"json"
	            		}
	            	},
 	            })
			};
			return config;
		});
		return configPromise;
	};
	
 
 
	obj.getConfigComboboxEstadosLibroOrdenes = function()
	{
		var translate_array = ['UI_ESTADO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1225",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadosAsignacion = function()
	{
		var translate_array = ['UI_ESTADO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1226",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
		
	obj.getConfigComboboxEstadoTransaccionBolsa = function()
	{
		var translate_array = ['UI_ESTADO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700020",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxAnoProcesoCartolas = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "2022", Nombre: "2022" },
						{ Codigo: "2023", Nombre: "2023" },
						{ Codigo: "2024", Nombre: "2024" },
						{ Codigo: "2025", Nombre: "2025" }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTrimestresFFMM = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "1", Nombre: "Primer Trimestre" },
					    { Codigo: "2", Nombre: "Segundo Trimestre" },
					    { Codigo: "3", Nombre: "Tercer Trimestre" },
					    { Codigo: "4", Nombre: "Cuarto Trimestre" }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	obj.getConfigComboboxMesProcesoCartolas = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "01", Nombre: "ENERO" },
					    { Codigo: "02", Nombre: "FEBRERO" },
					    { Codigo: "03", Nombre: "MARZO" },
					    { Codigo: "04", Nombre: "ABRIL" },
					    { Codigo: "05", Nombre: "MAYO" },
					    { Codigo: "06", Nombre: "JUNIO" },
					    { Codigo: "07", Nombre: "JULIO" },
					    { Codigo: "08", Nombre: "AGOSTO" },
					    { Codigo: "09", Nombre: "SEPTIEMBRE" },
					    { Codigo: "10", Nombre: "OCTUBRE" },
					    { Codigo: "11", Nombre: "NOVIEMBRE" },
					    { Codigo: "12", Nombre: "DICIEMBRE" }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxOpcionProcesoCartolas = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
						{ Codigo: "TODOS", Nombre: $translate.instant('UI_TODOS') },
						{ Codigo: "NORMATIVO", Nombre: $translate.instant('UI_NORMATIVO') },
						{ Codigo: "PERSONALIZADO", Nombre: $translate.instant('UI_PERSONALIZADO') }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAniosComisionesFFMM = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "2022", Nombre: "2022" },
					    { Codigo: "2021", Nombre: "2021" },
					    { Codigo: "2020", Nombre: "2020" },
					    { Codigo: "2019", Nombre: "2019" },
					    { Codigo: "2018", Nombre: "2018" }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoProcesoCartolas = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "", Nombre: $translate.instant('UI_TODOS') },
					    { Codigo: "NORMAL", Nombre: "NORMAL" },
					    { Codigo: "REPROCESO", Nombre: "REPROCESO" }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoProcesoCartolas = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "", Nombre: $translate.instant('UI_TODOS') },
					    { Codigo: "CAN", Nombre: $translate.instant('CANCELADO') },
					    { Codigo: "EPR", Nombre: $translate.instant('EN PROCESO') },
					    { Codigo: "ERR", Nombre: $translate.instant('ERROR') },
					    { Codigo: "ING", Nombre: $translate.instant('INGRESADO') },
					    { Codigo: "TER", Nombre: $translate.instant('TERMINADO') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCalculoIntereses = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: 0, Nombre: $translate.instant('SIMPLE') },
					    { Codigo: 1, Nombre: $translate.instant('COMPUESTO') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCentroCosto = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "INGRESO", Nombre: $translate.instant('INGRESO') },
					    { Codigo: "COSTO", Nombre: $translate.instant('COSTO') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	
		obj.getConfigComboboxTipoBonoIRF = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "SOB", Nombre: $translate.instant('Bonos Soberanos') },
					    { Codigo: "COR", Nombre: $translate.instant('Corporativos') },
					    { Codigo: "ALL", Nombre: $translate.instant('All') },
					    { Codigo: "X", 	 Nombre: $translate.instant('No Aplica') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTipoCentro = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "Producto", Nombre: $translate.instant('Producto') },
					    { Codigo: "Centro",   Nombre: $translate.instant('Centro') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoIfrs = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "D", Nombre: $translate.instant('DISPONIBLE A LA VENTA') },
					    { Codigo: "T", Nombre: $translate.instant('TRADING') },
					    { Codigo: "V", Nombre: $translate.instant('AL VENCIMIENTO') },
					    { Codigo: "X", Nombre: $translate.instant('No Aplica') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};		
	
	obj.getConfigComboboxCentroCostoRelacionado = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "CL", Nombre: $translate.instant('CLIENTE') },
					    { Codigo: "CP", Nombre: $translate.instant('CUENTA PROPIA') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	

	
	
	obj.getConfigComboboxTipoPlaza = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "C", Nombre: $translate.instant('CAVALI') },
					    { Codigo: "E", Nombre: $translate.instant('OTRO') },
					    { Codigo: "X", Nombre: $translate.instant('NO APLICA') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxPresenciaBursatil = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "S", Nombre: $translate.instant('SI') },
					    { Codigo: "N", Nombre: $translate.instant('NO') },
					    { Codigo: "X",  Nombre: $translate.instant('NO APLICA') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoEnrolamiento = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "FULL", Nombre: $translate.instant('UI_TIPO_ENROLAMIENTO_FULL')},
					    { Codigo: "EXPRESS", Nombre: $translate.instant('UI_TIPO_ENROLAMIENTO_EXPRESS')}
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxOrigenInstrumento = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "P", Nombre: $translate.instant('Peruano') },
					    { Codigo: "E", Nombre: $translate.instant('Extranjero') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoRenta = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "A", Nombre: $translate.instant('Afectos') },
					    { Codigo: "E", Nombre: $translate.instant('Exonerados') },
					    { Codigo: "I", Nombre: $translate.instant('Inafectos') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxRelacionadoGrupo = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "S", Nombre: $translate.instant('SI') },
					    { Codigo: "N", Nombre: $translate.instant('NO') },
					    { Codigo: "X",  Nombre: $translate.instant('NO APLICA') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxFormadorMercado = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "S", Nombre: $translate.instant('SI') },
					    { Codigo: "N", Nombre: $translate.instant('NO') },
					    { Codigo: "X",  Nombre: $translate.instant('NO APLICA') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	
	
	obj.getConfigComboboxPactoAndino = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "S", Nombre: $translate.instant('SI') },
					    { Codigo: "N", Nombre: $translate.instant('NO') },
					    { Codigo: "X",  Nombre: $translate.instant('NO APLICA') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};	
	obj.getConfigComboboxTenorInflacion = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "", Nombre: $translate.instant('VIGENTE') },
					    { Codigo: "-1M", Nombre: $translate.instant('1M ANTERIOR') },
					    { Codigo: "-3M", Nombre: $translate.instant('3M ANTERIOR') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoLibor = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "LIBOR1M", Nombre: $translate.instant('LIBOR1M') },
					    { Codigo: "LIBOR3M", Nombre: $translate.instant('LIBOR3M') },
					    { Codigo: "LIBOR6M", Nombre: $translate.instant('LIBOR6M') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoPlantillas = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
	            dataTextField: "Nombre",
	            dataValueField: "Nombre",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3102",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxConvencionDias = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
	            dataTextField: "Nombre",
	            dataValueField: "IdConvencion",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4143",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoDocumentos = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				// placeholder: translations.UI_ESTADO,
	            dataTextField: "NombreDocumento",
	            dataValueField: "IdTipoDocumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreDocumento", dir: "desc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1544",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxDireccionEvento = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				// placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdDireccionEvento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "desc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			// TODO eliminar configuracion ODATA
	            			url: "/BSCL/Plataforma/XdSODATA/Plataforma.xdsodata/DireccionEvento",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	obj.getConfigComboboxSiNo = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "S", Nombre: $translate.instant('UI_SI') },
					    { Codigo: "N", Nombre: $translate.instant('UI_NO') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxSiNoNoAplica = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "S", Nombre: $translate.instant('UI_SI') },
					    { Codigo: "N", Nombre: $translate.instant('UI_NO') },
					    { Codigo: "", Nombre: $translate.instant('UI_NO_APLICA') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAbiertoCerrado = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "A", Nombre: $translate.instant('UI_ABIERTO') },
					    { Codigo: "C", Nombre: $translate.instant('UI_CERRADO') },
					    { Codigo: "", Nombre: $translate.instant('UI_NO_APLICA') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxBloqueadoDesbloqueado = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "B", Nombre: $translate.instant('UI_BLOQUEADO') },
					    { Codigo: "D", Nombre: $translate.instant('UI_DESBLOQUEADO') },
					    { Codigo: "", Nombre: $translate.instant('UI_NO_APLICA') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoAsignacion = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "KdataTextTemplate",
				dataValueField: "IdTipoAsignacion",
				filter: "contains",
				KdataTextTemplate:  function(row) {
					return $translate.instant(row.Nombre);
				},
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					sort: { field: "IdTipoAsignacion", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1227",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoRegistroParidad = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_LIBRO,
				dataTextField: "NombreTipoRegistro",
				dataValueField: "IdTipoRegistro",
				filter: "contains",
				autoBind: false,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: true,
					serverSorting: true,  // ordena al lado del servidor
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1516",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoLibroOrden = function()
	{
		var translate_array = ['UI_TIPO_LIBRO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_LIBRO,
				dataTextField: "NombreTipoLibroOrden",
				dataValueField: "IdTipoLibroOrden",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "IdTipoLibroOrden", dir: "asc" }, //campo
					// y criterio de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1228",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxConfiguracionEvento = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_LIBRO,
				dataTextField: "Descripcion",
				dataValueField: "IdConfiguracionEvento",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "IdTipoLibroOrden", dir: "asc" }, //campo
					// y criterio de ordenamiento
					transport: {
						read: {
							// TODO eliminar configuracion ODATA
							url: "/BSCL/Plataforma/XdSODATA/Plataforma.xdsodata/ConfiguracionEvento",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxModulosCierre = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MODULOS,
				dataTextField: "Nombre",
				dataValueField: "IdCREModuloEmpresa",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "IdTipoLibroOrden", dir: "asc" }, //campo
					// y criterio de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1558",
							dataType:"json"
						}
					},
					schema: {
						model: {
							fields: {
								IdCREModuloEmpresa :  { type: "number"}
								}
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPerfil = function()
	{
		var translate_array = ['UI_PERFIL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PERFIL,
				dataTextField: "Nombre",
				dataValueField: "IdPerfil",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "IdTipoLibroOrden", dir: "asc" }, //campo
					// y criterio de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1525",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoDestinatario = function()
	{
		var translate_array = ['UI_TIPO_DESTINATARIO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PERFIL,
				dataTextField: "Nombre",
				dataValueField: "IdTipoDestinatario",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "IdTipoLibroOrden", dir: "asc" }, //campo
					// y criterio de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1659",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPreferenciaPagoTipoOperacion = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreTipoOperacion",
	            dataValueField: "IdTipoOperacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreTipoOperacion", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1660",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPreferenciaPagoMedioPago = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreMedioDePago",
	            dataValueField: "IdMedioDePago",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreMedioDePago", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1660",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPreferenciaPagoMoneda = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreMoneda",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreMoneda", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1660",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoPortafolio = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Nombre",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1661",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRelacionado = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreCompleto",
	            dataValueField: "IdPersonaDestino",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreCompleto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1753",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
		
		/*
		 * var translate_array = ['UI_TODOS']; configPromise =
		 * $translate(translate_array).then(function (translations) { var config =
		 * {}; config = { placeholder: '...', dataTextField: "NombreCompleto",
		 * dataValueField: "IdPersona", filter: "contains", autoBind: false,
		 * minLength: minAutocomplete, dataSource: new kendo.data.DataSource({
		 * type: "odata", serverFiltering: false, serverSorting: false, //ordena
		 * al lado del servidor sort: { field: "NombreCompleto", dir: "asc" },
		 * //campo y criterio de ordenamiento transport : { read:
		 * function(options) { $.ajax({ url:
		 * "/BSCL/Plataforma/GWJAVA/PLA/call?t=176&r=P_OUT_PERSONASRELACIONADAS",
		 * dataType: "json", type: "POST", contentType:"application/json;
		 * charset=utf-8", data: JSON.stringify({ p_IdEmpresa :
		 * $rootScope.SesionIdEmpresa, }), success: function(result) {
		 * options.success(result.P_OUT_PERSONASRELACIONADAS); }, error:
		 * function(result) { options.error(result); }}); } }, requestStart:
		 * function(e) { console.log("bcs-combobox-relacionados event ::
		 * requestStart",e); }, requestEnd: function(e) {
		 * console.log("bcs-combobox-relacionados event :: requestEnd",e); var
		 * result = []; $.each(e.response,function(key,value){
		 * result.push(value); }); e.response.d = {results:result, __count :
		 * result.length}; } }) }; return config; }); return configPromise;
		 */
	};
	
	obj.getConfigComboboxModuloFuncionProceso = function()
	{
		var translate_array = ['UI_PROCESO_FUNCION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PROCESO_FUNCION,
				dataTextField: "NombreFuncionProceso",
				dataValueField: "IdModuloFuncionProceso",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "IdTipoLibroOrden", dir: "asc" }, //campo
					// y criterio de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1560",
							dataType:"json"
						}
					},
					schema: {
						model: {
							fields: {
								IdCREModuloEmpresa :  { type: "number"}
								}
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxModoFacturacion = function()
	{
		var translate_array = ['UI_MODO_FACTURACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PROCESO_FUNCION,
				dataTextField: "Nombre",
				dataValueField: "IdModoFacturacion",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "Nombre", dir: "asc" }, //campo y criterio
					// de ordenamiento
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1247",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAtributoAnotacion = function()
	{
		var translate_array = ['UI_TIPO_ANOTACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				// placeholder: translations.UI_TIPO_ANOTACION,
				dataTextField: "NombreAtributoAnotacion",
				dataValueField: "IdAtributoAnotacion",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "Nombre", dir: "asc" }, //campo y criterio
					// de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1662",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getConfigComboboxAtributoAnotacionCliente = function()
	{
		var translate_array = ['UI_TIPO_ANOTACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				// placeholder: translations.UI_TIPO_ANOTACION,
				dataTextField: "NombreAtributoAnotacion",
				dataValueField: "IdAtributoAnotacion",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "Nombre", dir: "asc" }, //campo y criterio
					// de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12093",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoAutorizadoPortafolio = function()
	{
		var translate_array = ['UI_TIPO_AUTORIZACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_AUTORIZACION,
				dataTextField: "NombreTipoAutorizado",
				dataValueField: "IdTipoAutorizado",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "Nombre", dir: "asc" }, //campo y criterio
					// de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1663",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoPerfilPortafolio = function()
	{
		var translate_array = ['UI_TIPO_AUTORIZACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_AUTORIZACION,
				dataTextField: "NombreTipoAutorizado",
				dataValueField: "IdTipoAutorizado",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: true,
					serverSorting: false,  // ordena al lado del servidor
					// sort: { field: "Nombre", dir: "asc" }, //campo y criterio
					// de ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1663",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAutorizadoDarOrdenes = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '',
				dataTextField: "NombreCompleto",
				dataValueField: "IdAutorizado",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: true,
					filter : { field : "IdTipoAutorizado", operator : "eq", value : "ORDEN-CREAR" },
					serverSorting: true,  // ordena al lado del servidor
					sort: { field: "NombreCompleto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1262",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCondicionOperativa= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdCondicionOperativa',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1665",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoCondicionOperativa= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1666",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRequisitoOperativo= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdRequisitoOperativo',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1564",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoComportamiento= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoComportamiento',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1667",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoEtapa= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoEtapa',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1668",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoOperacionUsuario= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoOperacionUsuario',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1669",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxValorCobro= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'DescripcionValorCobro',
	            dataValueField: 'IdValorCobro',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "DescripcionValorCobro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1670",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				IdCobroClasificacion 	: { type:"number"  },
	            				IdTipoProducto		 	: { type:"string"  },
	            				// ValorCobro : { type:"number" },
	            				ValorTopeMinimoOrden 	: { type:"number"  },
	            				IdValorCobro 			: { type:"string"  }
	            			}
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}; 
	
	obj.getConfigComboboxCobro= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreCobro',
	            dataValueField: 'IdCobro',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreCobro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1671",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCobroClasificacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'CodigoCobroClasificacion',
	            dataValueField: 'IdCobroClasificacion',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoCobroClasificacion", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3225",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				IdCobroClasificacion : { type:"number"  }
	            			}
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCarteraClasificacionIfrs= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
					placeholder: translations.UI_CATEGORIA_IFRS,
					dataTextField: "CarteraIFRS",
					dataValueField: "CodigoCarteraIFRS",
					filter: "contains",
					autoBind: false,
					minLength: minAutocomplete,
					dataSource: new kendo.data.DataSource({
						type: "odata",
						serverSorting: false,  // ordena al lado del servidor
						sort: { field: "CarteraIFRS", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
						transport: {
							read: {
								// TODO eliminar configuracion ODATA
								// url:
								// "/BSCL/MesaRF/XdSODATA/MesaRentaFija.xdsodata/ClasificacionCartera",
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20018",
								dataType:"json"
							}
						},
						schema: {
							model: {
								fields: {
									 IdClasificacionCartera 	:  { type: "number"}
									,IdClasificacionCartera 	:  { type: "string"}
									,CodigoCarteraIFRS			:  { type: "string"}
									,CarteraIFRS 				:  { type: "string"}
									,CodigoCarteraCorredor	 	:  { type: "string"}
									,CarteraCorredor			:  { type: "string"}
									,CodigoCarteraClasificacion	:  { type: "string"}
									,CarteraClasificacion		:  { type: "string"}
									,IdEstado	 				:  { type: "string"}
									}
							}
						}
					})
				};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCarteraCorredor= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
					placeholder: "...",
					dataTextField: "CarteraCorredor",
					dataValueField: "CodigoCarteraCorredor",
					filter: "contains",
					autoBind: false,
					minLength: minAutocomplete,
					dataSource: new kendo.data.DataSource({
						type: "odata",
						serverFiltering: false,
						serverSorting: false,  // ordena al lado del servidor
						// sort: { field: "IdTipoLibroOrden", dir: "asc" },
						// //campo y criterio de ordenamiento
						transport: {
							read: {
								// TODO eliminar configuracion ODATA
								url: "/BSCL/MesaRF/XdSODATA/MesaRentaFija.xdsodata/ClasificacionCartera",
								dataType:"json"
							}
						},
						schema: {
							model: {
								fields: {
									IdClasificacionCartera :  { type: "number"}
									}
							}
						}
					})
				};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPreferenciaPagoPortafolio= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
					placeholder: "...",
					dataTextField: "TextoPreferenciaPago",
					dataValueField: "IdPreferenciaPagoPortafolio",
					filter: "contains",
					autoBind: false,
					minLength: minAutocomplete,
					dataSource: new kendo.data.DataSource({
						type: "odata",
						serverFiltering: true,
						//serverSorting: false,  // ordena al lado del servidor
						sort: { field: "PorDefecto", dir: "desc" },
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1672",
								dataType:"json"
							}
						},
						schema: {
							model: {
								fields: {
									IdPreferenciaPagoPortafolio :  { type: "number"},
									IdPortafolio 				:  { type: "number"},
								}
							}
						}
					})
				};
			return config;
		});
		return configPromise;
	};

	// MSH, Junio 2016
	obj.getConfigComboboxMesas = function()
	{
		var translate_array = ['UI_MESA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MESA,
				KdataTextTemplate:  function(row) {
					return row.IdMesa !== 0?(row.DocumentoIdentidad + " / " + row.Nombre +" ("+row.CodigoPortafolio+")") : (row.KdataTextTemplate).trim();
				},
				dataTextField: 'KdataTextTemplate',
	            dataValueField: 'IdMesa',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            template: '<span >#: KdataTextTemplate #</span>',
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	// sort: { field: "Nombre", dir: "asc" },
	            	sort: [ { field: "PorDefecto", dir: "desc" }
	            			,{ field: "IdTipoProducto", dir: "asc" }
	            			,{ field: "Nombre", dir: "asc" } ],
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1232",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	// NSM, Julio 2021
	obj.getConfigComboboxClienteMesas = function()
	{
		var translate_array = ['UI_MESA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MESA,
				KdataTextTemplate:  function(row) {
					return row.IdMesa !== 0?(row.NombreCompletoPortafolio) : (row.KdataTextTemplate).trim();
				},
				dataTextField: 'KdataTextTemplate',
	            dataValueField: 'IdMesa',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            template: '<span >#: KdataTextTemplate #</span>',
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: [  { field: "DocumentoIdentidad", dir: "asc" }
	            			,{ field: "CodigoPortafolio", dir: "asc" } ],
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1255",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	// MSH, Junio 2016
	obj.getConfigComboboxTipoCarteraConsolidada = function()
	{
		var translate_array = ['UI_TIPO_CARTERA','UI_TODAS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MESA,
	            dataTextField: 'Nombre',
	            dataValueField: 'IdMesa',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	// sort: { field: "Nombre", dir: "asc" },
	            	sort: [ { field: "IdTipoProducto", dir: "asc" },{ field: "Nombre", dir: "asc" } ],
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1232",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	// MSH, Noviembre 2016
	obj.getConfigComboboxMetodoPosicionMRF = function()
	{
		var translate_array = ['UI_TIPO_POSICION_MRF'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MESA,
	            dataTextField: 'NombreMetodo',
	            dataValueField: 'IdMetodo',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: [ { field: "NombreMetodo", dir: "asc" } ],
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1048",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	
	// Fondos Mutuos, Junio 2016
	obj.getConfigComboboxTipoFondo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreCorto",
	            dataValueField: "IdTipoFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdTipoFondo", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=368",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPropositoFFM = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreConcepto",
	            dataValueField: "CodigoConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdConcepto", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=370",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoRescate = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreConcepto",
	            dataValueField: "CodigoConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdConcepto", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1233",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMonedaCuentaInversionFFMM = function()
	{
		var translate_array = ['UI_CUENTA_INVERSION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "Moneda",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CuentaInversionPrincipal", dir: "desc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1365",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxOperacionFondoMutuo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreConcepto",
	            dataValueField: "CodigoConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdConcepto", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1690",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInstrumentoFondoMutuo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			var ds = new bcs.DataSource({
            	type: "odata",
	            definition: {	
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdSerieFondo", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=356",
							dataType:"json"
	            		}
	            	},schema : {
	    				model: {
	    					fields : {
	    						FechaFondo: {type: "sysdate"}
	    					}
	    				}
	    			}
	            }
            });
			config = {
				placeholder: '...',
	            dataTextField: "IdSerieFondoNombre",
	            dataValueField: "IdSerieFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: ds.ds
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxSerieFondoMutuo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			var ds = new bcs.DataSource({
            	type: "odata",
	            definition: {	
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdSerieFondo", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=356",
							dataType:"json"
	            		}
	            	},schema : {
	    				model: {
	    					fields : {
	    						FechaFondo: {type: "sysdate"}
	    					}
	    				}
	    			}
	            }
            });
			config = {
				placeholder: '...',
	            dataTextField: "NombreSerieFondo",
	            dataValueField: "IdSerieFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: ds.ds
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInstrumentoCustodiaFondoMutuo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "IdSerieFondoNombre",
	            dataValueField: "IdSerieFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdSerieFondo", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3023",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoRegistroCargaFondoMutuo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreConcepto",
	            dataValueField: "CodigoConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdConcepto", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3128",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	// MESAS
	obj.getConfigComboboxCanastaForward = function()
	{
		var translate_array = ['UI_CANASTA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONDICION_LIQUIDACION,
	            dataTextField: "Nombre",
	            dataValueField: "IdCanasta",
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdCanasta", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=502",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxParametroMesa= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PARAMETRO,
	            dataTextField: 'NombreParametro',
	            dataValueField: 'ValorParametro',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdParametro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=393",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxModalidadLiquidacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
					placeholder: translations.UI_MODALIDAD_LIQUIDACION,
					dataTextField: 'NombreParametro',
					dataValueField: 'ValorParametro',
					filter: "contains",
					autoBind: false,
					minLength: minAutocomplete,
					dataSource: new kendo.data.DataSource({
						type: "odata",
						serverFiltering: false,
						serverPaging: false,
						sort: { field: "IdParametro", dir: "asc" },
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=486",
								dataType:"json"
							}
						}
					})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxOperacionMesa= function()
	{
		var translate_array = ['UI_OPERACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_OPERACION,
	            dataTextField: "Accion",
	            dataValueField: "IdTipoOperacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Accion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1582",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getConfigComboboxTipoInstrumentoMesa= function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				minLength: minAutocomplete,
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "Instrumento",
	            dataValueField: "Instrumento",
	            // filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Instrumento", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=398",
							dataType:"json"
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxInstrumentoMesaUI= function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				minLength: minAutocomplete,
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "RazonSocial",
	            dataValueField: "Nemotecnico",
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3326",
							dataType:"json"
	            		}
	            	}
	        	}) 

			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxInstrumentoMesa= function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				minLength: minAutocomplete,
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "RazonSocial",
	            dataValueField: "Nemotecnico",
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1540",
							dataType:"json"
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxInstrumentoIF= function()
	{
		var translate_array = ['UI_NEMOTECNICO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				minLength: minAutocomplete,
				placeholder: translations.UI_NEMOTECNICO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "Nemotecnico",
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1540",
							dataType:"json"
	            		}
	            	}
	        	})

			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMonedaPrincipal= function()
	{
		var translate_array = ['UI_MONEDA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: 'IdMonedaPrincipal',
	            dataValueField: 'IdMonedaPrincipal',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Orden", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=398",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoMesa= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_OPERACION,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=501",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoOperacionMesa = function ()
	{
		var translate_array = ['UI_ESTADO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601101",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
		
	};
	
	obj.getConfigComboboxEstadoDatatec = function ()
	{
		var translate_array = ['UI_ESTADO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601106",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
		
	};
	
	
	obj.getConfigComboboxMonedaSecundaria= function()
	{
		var translate_array = ['UI_MONEDA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: 'IdMonedaSecundaria',
	            dataValueField: 'IdMonedaSecundaria',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdMonedaSecundaria", dir: "desc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=398",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigConsultaParMonedasBase= function()
	{
		var translate_array = ['UI_MONEDA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: 'NombreLargo',
	            dataValueField: 'IdMoneda',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdTipoInstrumento", dir: "desc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3325",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
		
	obj.getConfigComboboxPlanillaBancoCentral= function()
	{
		var translate_array = ['UI_CODIGO_PLANILLA_BANCO_CENTRAL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: 'IdMasDescripcion',
	            dataValueField: 'IdPlanillaBancoCentral',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdPlanillaBancoCentral", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=399",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMedioDePagoMesa= function()
	{
		var translate_array = ['UI_MONEDA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_INSTRUMENTO,
	            dataTextField: 'NombreMedioDePago',
	            dataValueField: 'IdMedioDePago',
	            filter: "startswith",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdMedioDePago", dir: "desc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=394",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoComprobantePosicionMRF= function()
	{
		var translate_array = ['UI_TIPO_COMPROBANTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_COMPROBANTE,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoComprobante",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=469",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoEstadoPosicionMRF= function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_ESTADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=470",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoOperacionPosicion= function()
	{
		var translate_array = ['UI_TIPO_OPERACION_POSICION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_OPERACION_POSICION,
	            dataTextField: "NombreEvento",
	            dataValueField: "IdOperacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "NombreEvento", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=471",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEventoPosicion= function()
	{
		var translate_array = ['UI_EVENTO_POSICION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_EVENTO_POSICION,
	            dataTextField: "NombreOperacion",
	            dataValueField: "IdEvento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "NombreOperacion", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=472",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRegimenFFM = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Descripcion",
	            dataValueField: "IdRegimenFondoMutuo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdRegimenFondoMutuo", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=371",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoOperacionFFMM = function()
	{
		var translate_array = ['UI_TIPO_OPERACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_OPERACION,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoMovimientoFondoMutuo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1041",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMotivoRetiro = function()
	{
		
		var translate_array = ['UI_MOTIVO_RETIRO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_OPERACION,
	            dataTextField: "Descripcion",
	            dataValueField: "IdMotivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=700022", // cambiar
																				// servicio
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoMovimientoTraspasoFFMM = function()
	{
		var translate_array = ['UI_TIPO_MOVIMIENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_MOVIMIENTO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoMovimientoTraspaso",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300008",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMotivoTraspasoFFMM = function()
	{
		var translate_array = ['UI_MOTIVO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MOTIVO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdMotivoTraspaso",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300009",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMotivoAnulacionTraspasoFFMM = function()
	{
		var translate_array = ['UI_MOTIVO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MOTIVO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdMotivoAnulacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300018",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoTraspasoFFMM = function()
	{
		var translate_array = ['UI_TIPO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MOTIVO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoTraspaso",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=300010",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
		
	
	obj.getConfigComboboxEstadoFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=392",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoOmnibusDetalle = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600015",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoSolicitudAgfFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3123",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoLiquidacionFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1685",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoInstrumentoFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1249",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoInstrumento = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1508",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTipoEntidad = function()
	{
		var translate_array = ['UI_TIPO_ENTIDAD_EMISOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "name",
	            dataValueField: "id",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "name", dir: "desc" },
					data: [
						{ id: 1, name: 'B'},
						{ id: 2, name: 'F' },
						{ id: 3, name: 'S' },
						{ id: 4, name: 'O' },
						{ id: 5, name: 'T' },
					]
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoComisionFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1429",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoCertificacionFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_ESTADO,
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1676",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFamiliaFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "NombreFamilia",
	            dataValueField: "IdFamiliaFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreFamilia", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1359",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoPerfilFFMM = function()
	{
		var translate_array = ['UI_TIPO_PERFIL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "Key",
	            dataValueField: "IdKeyPerfil",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdKeyPerfil", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1715",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoOperacionCargaFFMM = function()
	{
		var translate_array = ['UI_TIPO_OPERACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_OPERACION,
	            dataTextField: "Descripcion",
	            dataValueField: "TipoMovimiento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3103",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoAutorizacion = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: $translate.instant("UI_TIPO_AUTORIZACION"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "EVENTO", Nombre: $translate.instant('UI_EVENTO') },
					    { Codigo: "ETAPA", Nombre: $translate.instant('UI_ETAPA') },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPerfilFondoMutuo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Nombre",
	            dataValueField: "NombrePerfilConfiguracion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombrePerfilConfiguracion", dir: "desc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1395",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxEstadoDeposito= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=437",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoArchivoContrato= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1457",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxEstadoConfirmacionNotificacion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Todos",
	            dataTextField: 'EstadoConfirmacion',
	            dataValueField: 'CodigoConfirmacion',
	            filter: "startswith",

	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoConfirmacion", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1458",			
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxSupervisor= function()
	{
		var translate_array = ['UI_SUPERVISOR','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SUPERVISOR,
	            dataTextField: "NombreCompleto",
	            dataValueField: "IdEmpleadoSupervisor",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "NombreCompleto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1466",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEjecutivo= function()
	{
		var translate_array = ['UI_EJECUTIVO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_EJECUTIVO,
	            dataTextField: "NombreCompleto",
	            dataValueField: "IdEmpleadoEjecutivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "NombreCompleto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1472",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAgente= function()
	{
		var translate_array = ['UI_AGENTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_AGENTE,
	            dataTextField: "NombreCompleto",
	            dataValueField: "IdEmpleadoEjecutivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "NombreCompleto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1472",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAsuntosPendientes= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1484",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAsuntos = function()
	{
		var translate_array = ['UI_ASUNTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ASUNTO,
	            dataTextField: "Asunto",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Asunto", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=600008",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxRelacionNormativa= function()
	{
		var translate_array = ['UI_RELACION_NORMATIVA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_RELACION_NORMATIVA,
	            dataTextField: "DescripcionRelacionNorma",
	            dataValueField: "IdTipoRelacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "DescripcionRelacionNorma", dir: "asc" }, // campo
																				// y
																				// criterio
																				// de
																				// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1511",
							dataType:"json"
	            		}
	            	},
	            
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAccesos = function()
	{
		var translate_array = ['UI_ACCESO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ACCESO,
	            dataTextField: "NombreAcceso",
	            dataValueField: "IdAcceso",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdAcceso", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1696",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoRegistrosSistema = function()
	{
		var translate_array = ['UI_TIPO_REGISTRO_SISTEMA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_REGISTRO_SISTEMA,
	            dataTextField: "NombreRegistro",
	            dataValueField: "CodigoRegistro",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "array",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CodigoRegistro", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	data : [
		        		{"NombreRegistro":"USUARIOS INTERNET CLIENTE", "CodigoRegistro":"USUARIOS_CLIENTE"}, 
		        		{"NombreRegistro":"PERFILES DE ACCESO POR USUARIO", "CodigoRegistro":"ACCESOS_USUARIOS_CLIENTE"},
		        		{"NombreRegistro":"ROLES Y PERFILES POR USUARIO", "CodigoRegistro":"PERFILES_USUARIOS_CLIENTE"},
		        		{"NombreRegistro":"ESTADO DE PORTAFOLIOS DE CLIENTES", "CodigoRegistro":"ESTADO_PORTAFOLIO_CLIENTE"},
		        		{"NombreRegistro":"PARAMETROS DEL SISTEMA", "CodigoRegistro":"PARAMETROS"}
		            ]
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoConfirmacion= function()
	{
		var translate_array = ['UI_ESTADO_CONFIRMACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO_CONFIRMACION,
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstadoOrden',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21044",			
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoInstruccion= function()
	{
		var translate_array = ['UI_ESTADO_INSTRUCCION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdEstadoOrden", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21044",			
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoProductoMesaModulo= function()
	{
		var translate_array = ['UI_TIPO_PRODUCTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_PRODUCTO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoProductoMesaModulo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=451",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoItem = function()
	{
		var translate_array = ['UI_TIPO_ITEM','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_PRODUCTO,
	            dataTextField: "NombreItemTipo",
	            dataValueField: "IdTipoItem",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "NombreItemTipo", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12085",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxTipoAutorizacionEtapa = function()
	{
		var translate_array = ['UI_TIPO_AUTORIZACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_AUTORIZACION,
	            dataTextField: "NombreTipoAutorizacion",
	            dataValueField: "IdTipoAutorizacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1698",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigCertificadosCliente = function()
	{
		var translate_array = ['UI_PERIODO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PERIODO,
	            dataTextField: "Periodo",
	            dataValueField: "IdCertificadoCliente",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdCertificadoCliente", dir: "asc" }, // campo
																			// y
																			// criterio
																			// de
																			// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3003",
							dataType:"json",
							data:{
								$select: "\"IdCertificadoCliente\", \"Periodo\", \"IdArchivo\", \"NombreArchivo\", \"TipoArchivo\", \"ServicioArchivo\""
							}
	            		}
	            	},schema 				: {
	    				model: {
	    					fields : {
	    						IdCertificadoCliente: {type: "number"},
	    						Periodo: {type: "string"}
	    					}
	    				}
	    			}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
 
	obj.getConfigComboboxTipoCargaMasivaTesoreria= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1737",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	
	obj.getConfigComboboxTipoCargaMasivaOrdenes= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "Nombre", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=2002",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	// CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva
	// Abonos y Cargos
	obj.getConfigComboboxTipoCargaMasivaCtaInversion = function()	
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3337",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	//CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y Cargos
	
	
	obj.getConfigComboboxTipoCargaMasivaCustodia= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdTipoCargaMasiva", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3201",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoCargaMasivaSpot= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdTipoCargaMasiva", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1851",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};			
	
	
	
	obj.getConfigComboboxTipoCargaMasivaFacturacion= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1797",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};		
	
	obj.getConfigComboboxTipoCargaMasivaPlataforma= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdTipoCargaMasiva", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8011",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxEstadoMesas= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=455",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.BarraProgresoServicio = function(datos,success_fn,error_fn)
	{
		var urlPrecioNew = "/BSCL/Plataforma/GWJAVA/PLA/call?t=702167";
		
		var r = $resource("", {},
	    {
			'Operacion' : { method: "POST", url: urlPrecioNew}// ,
																// isArray:true}
		});
		var post = r.Operacion(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	
	obj.getConfigComboboxEstadoFamiliaFFMM = function()
	{
		var translate_array = ['UI_TIPO_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1712",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCargaMasivaSocioNegocio= function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
				dataTextField: "Nombre",
				dataValueField: "IdTipoCargaMasiva",
				filter: "contains",
				autoBind: false,
				minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,  // ordena al lado del servidor**
					sort: { field: "Nombre", dir: "asc" }, 
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700419",
							dataType:"json"
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFuente = function()
	{
		var translate_array = ['UI_FUENTE','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FUENTE,
	            dataTextField: "CodigoDescripcion",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdFuente", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=600",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoTasa = function()
	{
		var translate_array = ['UI_TIPO_TASA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_TASA,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoTasa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=603",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxEstadoTasa= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=605",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxInstrumentoCustodiaTasa= function()
	{
		var translate_array = ['UI_INSTRUMENTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_INSTRUMENTO,
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=606",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxConfiguracionTasas= function()
	{
		var translate_array = ['UI_CONFIGURACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONFIGURACION,
	            dataTextField: "Descripcion",
	            dataValueField: "Valor",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=611",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTipoCriterioTasa= function()
	{
		var translate_array = ['UI_CRITERIO_TASA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CRITERIO_TASA,
	            dataTextField: "Descripcion",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=612",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxUsoCriterio= function()
	{
		var translate_array = ['UI_USO_CRITERIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_USO_CRITERIO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdUso",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=621",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoPrecioModuloTasas = function()
	{
		var translate_array = ['UI_TIPO_PRECIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_PRECIO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdTipoPrecio",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio de ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=632",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigCuentasContables = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Cuentas",
	            dataTextField: 'NumeroCuentaNombre',
	            dataValueField: 'Cuenta',
	            filter: "contains",
	            autoBind: false,	           
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,	            	
	            	serverPaging: false,
	            	sort: { field: "Cuenta", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12087",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigCuentasContablesNombre = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Nombre Cuentas",
	            dataTextField: 'NombreCuenta',
	            dataValueField: 'Cuenta',
	            filter: "contains",
	            autoBind: false,	           
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,	            	
	            	serverPaging: false,
	            	sort: { field: "Cuenta", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12087",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigTipoCentralizacion = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Centralizacion",
	            dataTextField: 'NombreTipoCentralizacion',
	            dataValueField: 'IdTipoCentralizacion',
	            filter: "contains",
	            autoBind: false,	           
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,	            	
	            	serverPaging: false,
	            	sort: { field: "NombreTipoCentralizacion", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12064",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	    
	obj.getConfigTipoJuntaAccionistas= function()
	{
		var translate_array = ['UI_TIPO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO,
	            dataTextField: "Descripcion",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3182",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigEstadoJuntaAccionistas= function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: "Descripcion",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3169",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigTipoInstruccionJuntaAccionista= function()
	{
		var translate_array = ['...','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "TipoInstruccion",
	            dataValueField: "IdTipoInstruccion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdTipoInstruccion", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3171",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigTipoDiaFeriado= function()
	{
		var translate_array = ['UI_TIPO_DIA_FERIADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_DIA_FERIADO,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoDiaFeriado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=626",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigRangoPatrimonio= function()
	{
		var translate_array = ['UI_RANGO_PATRIMONIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_RANGO_PATRIMONIO,
	            dataTextField: "DescripcionRango",
	            dataValueField: "IdRangoPatrimonio",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: [{ field: "Minimo", dir: "asc" },{ field: "Maximo", dir: "asc" }], // campo
																								// y
																								// criterio
																								// de
																								// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3177",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigOperacionesPortafolioCliente= function()
	{
		var translate_array = ['UI_OPERACIONES','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_OPERACIONES,
	            dataTextField: "NombreTipoOperacion",
	            dataValueField: "IdPreferenciaPago",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: [{ field : "IdTipoOperacion", 	dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1250",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCargaTranOrd= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CAGA,
	            dataTextField: "Descripcion",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: [{ field : "Descripcion", 	dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4199",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoModeloCliente= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CAGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: [{ field : "IdEstado", 	dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=100015",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	
	/* INICIO - ILS */
	obj.getConfigConsultaTipoOperacionCtb = function()
	{
		var translate_array = ['UI_OPERACION_CTB','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Operación",
	            dataTextField: "CodigoOperacionCtb",
	            dataValueField: "IdOperacionCtb",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdOperacionCtb", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13000",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigConsultaCondicionCtb = function()
	{
		var translate_array = ['UI_CONDICION_CTB','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Condición",
	            dataTextField: "CodigoCondicionCtb",
	            dataValueField: "IdCondicionCtb",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdCondicionCtb", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13001",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigConsultaTipoSaldoDH = function()
	{
		var translate_array = ['UI_DEBE_HABER','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Tipo Saldo",
	            dataTextField: "CodigoTipoSaldoDH",
	            dataValueField: "IdTipoSaldoDH",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdTipoSaldoDH", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13002",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigConsultaItem = function()
	{
		var translate_array = ['UI_ITEM'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Item",
	            dataTextField: "IdItemNombre",
	            dataValueField: "IdItem",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdItem", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12081",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigConsultaInformeTipo = function()
	{
		var translate_array = ['UI_INFORME'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Informe",
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoInforme",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdTipoInforme", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13023",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigConsultaConcepto = function()
	{
		var translate_array = ['UI_CONCEPTO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Concepto",
				KdataTextTemplate:  function(row) {
					return row.IdConcepto + " - " + row.NombreConcepto;
				},
	            dataTextField: 'KdataTextTemplate',
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdConcepto", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13009",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxModulosPauta = function()
	{
		var translate_array = ['UI_MODULOS','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			//configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION,
	            dataTextField: "Nombre",
	            dataValueField: "CodigoModulo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4176",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigConsultaPosicionFraccion = function()
	{
		var translate_array = ['UI_POSICION_FRACCION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "PosicionFraccion",
	            dataTextField: "NombrePosicionFraccion",
	            dataValueField: "IdPosicionFraccion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdPosicionFraccion", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13011",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};


	obj.getConfigConsultaEmpresas = function()
	{
		var translate_array = ['UI_EMPRESA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Empresa",
	            dataTextField: "Nombre",
	            dataValueField: "IdEmpresa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdEmpresa", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigConsultaIndices = function()
	{
		var translate_array = ['UI_INDICE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "Indice",
				KdataTextTemplate:  function(row) {
					return row.IdIndice + " - " + row.Nombre;
				},
	            dataTextField: 'KdataTextTemplate',
	            dataValueField: "IdIndice",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "IdIndice", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13008",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	/* FIN - ILS */
	
	obj.getConfigFondoMutuo = function()
	{
		var translate_array = ['UI_FONDO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3195",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigRegimenSerieFondo = function()
	{
		var translate_array = ['UI_REGIMEN','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "Descripcion",
	            dataValueField: "IdRegimenFondoMutuo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdRegimenFondoMutuo", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3203",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigSerieFondoRegimen = function()
	{
		var translate_array = ['UI_REGIMEN','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "IdSerieFondo",
	            dataValueField: "IdSerieFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdSerieFondo", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3203",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	
	// Cobertura
	obj.getValorCoberturaPatrimonial = function()
	{
		var translate_array = ['UI_VALOR_CP'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "ValorCobertura",
	            dataValueField: "IdValorCobertura",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdValorCobertura", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=572",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	// Cobertura
	obj.getValorDiasCobertura = function()
	{
		var translate_array = ['UI_VALOR_DIAS_COBERTURA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "ValorDiasCobertura",
	            dataValueField: "IdDiasCobertura",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdDiasCobertura", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=575",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	// Cobertura
	obj.getValorOpcionCobertura = function()
	{
		var translate_array = ['UI_VALOR_OPCION_COBERTURA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "ValorOpcionCobertura",
	            dataValueField: "IdOpcionCobertura",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdOpcionCobertura", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=576",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	// ESQUEMA DECLARACION
	obj.getConfigEsquemaDeclaracion = function()
	{
		var translate_array = ['...','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "Esquema",
	            dataValueField: "Esquema",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Esquema", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1774",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigVersionEsquemaDeclaracion = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			var ds = new bcs.DataSource({
            	type: "odata",
	            definition: {	
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Esquema", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1774",
							dataType:"json"
	            		}
	            	},schema : {
	    				model: {
	    					fields : {
	    						FechaPresentacionLimite: {type: "date"}
	    					}
	    				}
	    			}
	            }
            });
			config = {
				placeholder: '...',
	            dataTextField: "Version",
	            dataValueField: "Version",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: ds.ds
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigEstadoRegimenFFMM = function()
	{
		var translate_array = ['...','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "NombreEstado",
	            dataValueField: "IdEstado",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdEstado", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3227",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	
	
	/*
	 * INICIO - OPCIONES
	 * ----------------------------------------------------------------------------------------------
	 */
	obj.getConfigComboboxFamiliaOpciones = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoInstrumento',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Descripcion", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8001",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoOpciones = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreParametro',
	            dataValueField: 'ValorParametro',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreParametro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8005",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	obj.getConfigComboboxSubyacenteOpciones = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nemo',
	            dataValueField: 'Nemo',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nemo", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8002",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxEstiloOpciones = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreParametro',
	            dataValueField: 'ValorParametro',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreParametro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8004",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxModalidadLiquidacionOpciones = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreParametro',
	            dataValueField: 'ValorParametro',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreParametro", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8003",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxMercadoOpciones= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'GlosaCodigoClasificacion',
	            dataValueField: 'CodigoClasificacion',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "GlosaCodigoClasificacion", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8000",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	/*
	 * FIN - OPCIONES
	 * ----------------------------------------------------------------------------------------------
	 */
	
	obj.getConfigComboboxTipoProductoPlantillaProducto= function()
	{
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdTipoProducto',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdTipoProducto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=8007",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxGradoInstruccion= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Nombre',
	            dataValueField: 'IdGradoInstruccion',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Codigo", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3501",
							dataType:"json"
	            		}
	            	},
	            	schema: {
	            		model: {
	            			fields : {
	            				IdGradoInstruccion : { type:"number"  },
	            				IdPais : { type:"number"  }
	            			}
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxComplementos= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'CodigoComplemento',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "CodigoComplemento", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3523",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCertificadosDJs= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'NombreCertificado',
	            dataValueField: 'CodigoCertificado',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreCertificado", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3573",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	// -----------------------------------------------------------------------------------------
	/*
	 * var getConfigComboboxNumeroCuentaCustodio = function() { var config = {};
	 * //configuracion del combo config = { placeholder:
	 * $translate.instant("UI_CODIGO"), dataTextField: "Codigo", dataValueField:
	 * "Codigo", filter: "contains", autoBind: false, minLength:
	 * minAutocomplete, dataSource: new kendo.data.DataSource({ type: "odata",
	 * serverFiltering: false, serverSorting: false, //ordena al lado del
	 * servidor** sort: [{ field: "IdBoveda", dir: "asc" }, { field: "Codigo",
	 * dir: "asc" }], //campo y criterio de ordenamiento transport: { read: {
	 * url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1263", dataType:"json" } } }) };
	 * return config; };
	 */
	
	obj.getConfigComboboxTipoUtilidadesCalificacionTributaria= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'DescripcionCombobox',
	            dataValueField: 'IdTipoUtilidadesCalificacionTributaria',
	            filter: "startswith",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "IdTipoUtilidadesCalificacionTributaria", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4157",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxOrigenCalificacionTributaria= function()
	{
		
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'Descripcion',
	            dataValueField: 'IdOrigen',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Orden", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=55669998",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPeriodoComercialCalificacionesTributarias = function()
	{
		
		var translate_array = [''];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "...",
	            dataTextField: 'PeriodoComercial',
	            dataValueField: 'PeriodoComercial',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "PeriodoComercial", dir: "desc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=55670000",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

    obj.getConfigComboboxMercadoCalificacionesTributarias = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];

		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};

			config = {
				placeholder: translations.UI_CLASIFICACION,
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionTipoInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "Nombre", dir: "asc" },

	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=11998233",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	var getConfigComboboxEmisores = function()
	{
		var dataSource =  new kendo.data.DataSource({
        	type: "odata",
        	sort: [{ field: "IdEmisor", dir: "asc" }],
        	transport: {
        		read: {
        			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3161",
					dataType:"json"
        		}, 
        	},
        	schema: {
				model: {
                    fields: {
							IdEmisor					: { type: "string"},
  							NombreCorto					: { type: "string"},
  							CodigoEmisor				: { type: "string"},	  							
  							CodigoRegulador				: { type: "string"},	  							
  							Regulador					: { type: "string"},	  							
  							EmiteIIF					: { type: "string"},	  							
  							EmiteIRF					: { type: "string"},	  							
  							EmiteFungible				: { type: "string"},	  							
  							ValorClasificacionRiesgoIIF	: { type: "string"},	  							
  							IdEntidadClasificadoraRiesgo: { type: "string"},	  							
  							IndicadorAGF				: { type: "string"},	  							
  							IdPersona					: { type: "number"},								
  							IdEstado					: { type: "string"},	  							
  							FechaActualizacion			: { type: "string"},
  							DocumentoIdentidad			: { type: "string"},
  							TipoDocumentoIdentidad		: { type: "string"},
  							NombreCompleto				: { type: "string"},
  							TipoEntidad	  				: { type: "string"}
                    }
           		}
			}
        });
		
		var config = {};
		config = {
			permiteRepetidos : true,
			placeholder: "",
            dataTextField: "KdataTextTemplate",
            dataValueField: "IdEmisor",
            KdataTextTemplate:  function(row) {
				return  row.IdEmisor;
			},
			dataSource : dataSource,
			defLupa :  { 
				ventana : 'SelectorEmisores',
				parametros : function() {
					return { filtros : dataSource }
				}
			},
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            
		};
		return config;

	};	
	obj.configuracionCombos['combobox-emisores'] = getConfigComboboxEmisores();		
//	
	
	var getConfigComboboxEmisoresLite = function()
	{
		var dataSource =  new kendo.data.DataSource({
        	type: "odata",
        	sort: [{ field: "IdEmisor", dir: "asc" }],
        	transport: {
        		read: {
        			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3161",
					dataType:"json",
					data: {
						$select: function () {
							return '"IdEmisor", "IdEstado", "DocumentoIdentidad", "NombreCompleto"'
						}
					}
        		}, 
        	},
        	schema: {
				model: {
                    fields: {
							IdEmisor					: { type: "string"},								
  							IdEstado					: { type: "string"},	  							
  							DocumentoIdentidad			: { type: "string"},
  							NombreCompleto				: { type: "string"},
                    }
           		}
			}
        });
		
		var config = {};
		config = {
			permiteRepetidos : true,
			placeholder: "",
            dataTextField: "IdEmisor",
            dataValueField: "IdEmisor",
			dataSource : dataSource,
			defLupa :  { 
				ventana : 'SelectorEmisores',
				parametros : function() {
					return { filtros : dataSource }
				}
			},
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            
		};
		return config;

	};
	obj.configuracionCombos['combobox-emisores-lite'] = getConfigComboboxEmisoresLite();
	
	var getConfigComboboxCuentaCustodioPortafolio = function()
	{
		var dataSource =  new kendo.data.DataSource({
        	type: "odata",
        	sort: [{ field: "IdBoveda", dir: "asc" },
        	       { field: "Codigo", dir: "asc" }],
        	transport: {
        		read: {
        			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1263",
					dataType:"json"
        		}, 
        	},
        	schema: {
				model: {
                    fields: {
                    	IdCuentaCustodioPortafolio	: { type: "number"},
						IdPortafolio				: { type: "number"},
						IdBoveda					: { type: "string"},
						NombreBoveda				: { type: "string"},
						Codigo						: { type: "string"},
						IdEstado					: { type: "string"},
						NombreEstado				: { type: "string"},
						NombreModeloCustodia			: { type: "string"},
						NombreTipoCustodia				: { type: "string"},
						NombreTipoCuentaCustodia		: { type: "string"},
						CodigoBancoCustodio				: { type: "string"},
						NombreCompletoBancoCustodio		: { type: "string"},
						
						CodigoBancoBancoLiquidador		: { type: "string"},
						NombreCompletoBancoLiquidador	: { type: "string"}
                    }
           		}
			}
        });
		
		var config = {};
		config = {
			permiteRepetidos : true,
			placeholder: "",
            dataTextField: "KdataTextTemplate",
            dataValueField: "IdCuentaCustodioPortafolio",
            KdataTextTemplate:  function(row) {
				return row.NombreTipoCustodia + " - " + row.NombreBoveda + " - " + row.Codigo;
			},
			dataSource : dataSource,
			defLupa :  { 
				ventana : 'SelectorCuentaCustodio',
				parametros : function() {
					return { filtros : dataSource }
				}
			},
            filter: "contains",
            autoBind: false,
            minLength: minAutocomplete,
            
		};
		return config;

	};
	obj.configuracionCombos['combobox-cuenta-custodio-portafolio'] = getConfigComboboxCuentaCustodioPortafolio();
	
	obj.getConfigComboboxReportantesAsociados = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "IdOrdenAsociadaNombreReportanteUI",
	            dataValueField: "IdOrdenAsociada",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdOrdenAsociada", dir: "desc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4164",
							dataType:"json"
	            		}
	            	},
	            	schema:{
	            		model: {
	        				fields : {
	        					IdOrdenAsociada					: { type : "number"},
	        					IdOrdenReportado 	  		 	: { type : "number"},
	        					IdOrdenReportante 	  	  		: { type : "number"},
	        					MontoOrdenReportado 	  	  	: { type : "number"},
	        					MontoOrdenReportante 	  	  	: { type : "number"},
	        					NombreCompletoReportado			: { type : "string"},
	        					CodigoPortafolioReportado		: { type : "string"},
	        					IdMonedaOrdenReportado			: { type : "string"},
	        					NombreCompletoReportante		: { type : "string"},
	        					CodigoPortafolioReportante		: { type : "string"},
	        					IdMonedaOrdenReportante			: { type : "string"},
	        					TasaReportado					: { type : "number"},
	        					TasaReportante					: { type : "number"},
	        					FechaRegistro					: { type : "sysdate"},
	        					IdEstadoOrdenReportado			: { type : "string"},
	        					IdEstadoOrdenReportante			: { type : "string"},
	        					MontoAsociado					: { type : "number"},
	        					CantidadAsociada				: { type : "number"},
	        					IdTipoInstrumento				: { type : "string"},
	        					IdInstrumentoReportante			: { type : "number"},
	        					PlazoReportante					: { type : "number"},
	        					PrecioOrden						: { type : "number"},
	        					TasaPromedioReporte				: { type : "number"},
	        					IdMonedaLiquidacionOrdenReportante : { type : "string"},
	        					IdTipoOperacionReportante		: { type : "string"},
	        					IdTipoCanalIngresoReportante	: { type : "string"},
	        					IdBolsa							: { type : "string"},
	        					FechaOperacionReportante		: { type : "date"},
	        					IdTipoProducto					: { type : "string"},
	        					IdGrupoCobroReportante			: { type : "number"},
	        					IdGrupoCobroReportado			: { type : "number"},
	        					BaseReporteReportado			: { type : "number"},
	        					BaseReporteReportante			: { type : "number"}
	        				}
	        			}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	//------------------------------------------------------------------------------------------
	var getConfigComboboxTipoCriterioNeteo = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreTipoCriterioNeteo",
	            dataValueField: "IdTipoCriterioNeteo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreTipoCriterioNeteo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=100031",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	IdTipoCriterioNeteo : { type: "number"},
		                    	NombreTipoCriterioNeteo : { type: "string"},
		                    	SecuenciaNeteo : { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-tipo-criterio-neteo'] = getConfigComboboxTipoCriterioNeteo();
	
	//------------------------------------------------------------------------------------------
	var getConfigComboboxSistemaBursatil = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreSistemaBursatil",
	            dataValueField: "IdSistemaBursatil",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdSistemaBursatil", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3330",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	IdSistemaBursatil : { type: "string"},	
		                		NombreSistemaBursatil : { type: "string"},	
		                		IdClasificacionTipoInstrumento : { type: "string"},	
		                		IdEmpresa : { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-sistema-bursatil'] = getConfigComboboxSistemaBursatil();
	
	var getConfigComboboxCarteraEjecutivo = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreCarteraEjecutivo",
	            dataValueField: "NombreCarteraEjecutivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreCarteraEjecutivo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21034",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	NombreCarteraEjecutivo : { type: "string"},	
		                    	Descripcion : { type: "string"},	
		                    	IdEstado : { type: "string"},	
		                		IdEmpresa : { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-cartera-ejecutivo'] = getConfigComboboxCarteraEjecutivo();
	//------------------------------------------------------------------------------------------
	var getConfigComboboxCarteraInversion = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreCarteraInversion",
	            dataValueField: "NombreCarteraInversion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreCarteraInversion", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21000",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	NombreCarteraInversion : { type: "string"},	
		                    	Descripcion : { type: "string"},	
		                    	IdEstado : { type: "string"},	
		                		IdEmpresa : { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-cartera-inversion'] = getConfigComboboxCarteraInversion();
	
	//------------------------------------------------------------------------------------------
	var getConfigComboboxOperadorCarteraInversion = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreCarteraInversion",
	            dataValueField: "NombreCarteraInversion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreCarteraInversion", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21002",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	NombreCarteraInversion : { type: "string"},	
		                    	CodigoOperador : { type: "string"},	
		                    	IdEstado : { type: "string"},	
		                		IdEmpresa : { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-operador-cartera-inversion'] = getConfigComboboxOperadorCarteraInversion();
	
	var getConfigComboboxOperadorCarteraEjecutivo = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "NombreCarteraEjecutivo",
	            dataValueField: "NombreCarteraEjecutivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreCarteraEjecutivo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21040",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	NombreCarteraEjecutivo : { type: "string"},	
		                    	CodigoOperador : { type: "string"},	
		                    	IdEstado : { type: "string"},	
		                		IdEmpresa : { type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-operador-cartera-ejecutivo'] = getConfigComboboxOperadorCarteraEjecutivo();
	
	
	var getConfigComboboxMonedaEmision = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_MONEDA_EMISION,
	            dataTextField: "IdMonedaEmision",
	            dataValueField: "IdMonedaEmision",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdMonedaEmision", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21011",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	IdInstrumento 	: { type: "number"},
		                    	IdMonedaEmision : { type: "string"},
		                    	IdEstado 		: { type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-moneda-emision'] = getConfigComboboxMonedaEmision();
	
	
	var getConfigComboboxMonedaPagoInstrumento = function()
	{
		var translate_array = ['...'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_MONEDA_EMISION,
	            dataTextField: "IdMoneda",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdMoneda", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21013",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	IdInstrumento 	: { type: "number"},
		                    	IdMoneda 		: { type: "string"},
		                    	IdEstado 		: { type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.configuracionCombos['combobox-moneda-pago-instrumento'] = getConfigComboboxMonedaPagoInstrumento();
	
	
	// -----------------------------------------------------------------------------------------
	obj.getConfigComboboxTipoSalida = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "P", Nombre: $translate.instant('PROCESO').toUpperCase() },
					    { Codigo: "R", Nombre: $translate.instant('REPORTE').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoProcesoAperturaCierreModulo = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "APERTURA", Nombre: $translate.instant('APERTURA').toUpperCase() },
					    { Codigo: "CIERRE", Nombre: $translate.instant('CIERRE').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInterfaces = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "IdInterfaz",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdInterfaz", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4191",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	IdInterfaz 		: { type: "number"},	
		                    	Nombre 			: { type: "string"},	
		                    	Descripcion 	: { type: "string"},	
		                    	JsonData 		: { type: "string"},
		                    	IdTipoPrograma 	: { type: "string"},
		                    	TipoInterfaz 	: { type: "string"},
		                    	IdEstado		: { type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxCotitulares = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreCompleto",
				dataValueField: "IdPersona",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreCompleto", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3555",
							dataType:"json"
	            		}, 
	            	},
	            	schema: {
						model: {
		                    fields: {
		                    	IdPersonaOrigen : {type: "number"},
		                    	IdClienteOrigen : {type: "number"},
		                    	IdPersona : {type: "number"},
		                    	IdCliente : {type: "number"},
		                    	NombreCompleto : {type: "string"},
		                    	DocumentoIdentidad : {type: "string"},
		                    	IdPortafolio : {type: "number"},
		                    	CodigoPortafolio : {type: "string"},
		                    	Proposito : {type: "string"},
		                    	Estado : {type: "string"},
		                    	FechaIngreso : {type: "date"},
		                    	DocumentoIdentidadNombreCliente : {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	/**************************************/
	/*** INICIO APV						***/
	/**************************************/
	
	obj.getConfigComboboxFolioContratosSAAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "FolioContratoSAAPV",
				dataValueField: "FolioContratoSAAPV",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "FolioContratoSAAPV", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6002",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdContratoSAAPV 	: {type	: "number"},
		                    	FolioContratoSAAPV 	: {type	: "string"},	
		                    	IdCliente 			: {type : "number"},
		                    	IdPersona 			: {type : "number"},
		                    	NombrePlan			: {type	: "string"},
		                    	Producto			: {type	: "string"},
		                    	IdMedioRecaudacionFormaPago			: {type	: "number"},
		                    	IdPortafolio		: {type	: "number"}
		                    	
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxFolioRecaudacion = function()
	{
		var translate_array = ['UI_FOLIO_RECAUDACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
					var opcionesDeBusqueda = text.split(' ');	
					for (var i = 0; i < opcionesDeBusqueda.length; i++) {
						var nfilters = {
								logic: "or",
							    filters: [
									{"field": "IdRecaudacion" ,"operator":"startswith", "value": opcionesDeBusqueda[i].replace(/\./g, "") }
							    ]
						};
						filter.push(nfilters);
					}
				},
				filter: "contains",
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "IdRecaudacion",
				dataValueField: "IdRecaudacion",
				autoBind: false,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdRecaudacion", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6016",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdRecaudacion : {type: "number"},
		                    	MontoDeposito : {type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxTipoTrabajador= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "TipoTrabajador",
				dataValueField: "IdTipoTrabajador",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdTipoTrabajador", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6000",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdContratoSAAPV : {type: "number"},
		                    	FolioContratoSAAPV : {type: "string"}		      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	

	obj.getConfigComboboxBancoPorPais= function()
	{
		var translate_array = ['UI_BANCO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_BANCO,
	            dataTextField: 'NombreBanco',
	            dataValueField: 'IdBanco',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreBanco", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1611",
							dataType:"json"
	            		}
	            	},
	            	schema 			: {
						model	: {
		                    fields	: {
		                    	IdBanco			: { type: "number",editable: false	},
		                    	NombreBanco		: { type: "string",editable: false 	},
		                    	IdPaisOperacion	: { type: "string",editable: false 	},
	                        }
                   		}
					},
	            })
			};
			return config;
		});
		return configPromise;
	};


	
	obj.getConfigComboboxEstadoContratoAPV= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Nombre", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6003",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Nombre : {type: "string"},
		                    	Codigo : {type: "string"}		      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}

	obj.getConfigComboboxEstadoAPV= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Nombre", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6026",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Nombre : {type: "string"},
		                    	Codigo : {type: "string"},
		                    	NombreEntidad : {type: "string"}		      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxEntidadRecaudadora= function()
	 {
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "DocumentoIdentidadNombre",
				dataValueField: "Codigo",				
				filter: "contains",
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	sort: [{ field: "IdTipoEntidadRecaudadora", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6005",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdEntidadRecaudadora	: {type: "number"},
		                    	IdPersona				: {type: "number"},
		                    	Codigo 					: {type: "string"},
	            				Nombre 					: {type: "string"},
		                    	Estado					: {type: "string"},
		                    	IdTipoEntidadRecaudadora: {type: "number"},
		                    	DocumentoIdentidad		: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	//configuracion combo entidad destino 
	
	obj.getConfigComboboxEntidadRecaudadoraDestino= function()
	 {
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Codigo",
				dataValueField: "RutSociedadAdmFFMM",				
				filter: "contains",
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	sort: [{ field: "SociedadAdmFFMM", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=900203",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	RutSociedadAdmFFMM	: {type: "string"},
		                    	SociedadAdmFFMM		: {type: "string"},
		                    	Codigo 				: {type: "string"},
								IdEntidadRecaudadora	: {type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxNombrePlanAhorro = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombrePlanTipoInversion",
				dataValueField: "IdPlanTipoInversion",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6041",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdPlanTipoInversion		: {type: "number"},
		                    	NombrePlanTipoInversion : {type: "string"},
	            				IdEstado				: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoPagoDetalleEmpleadorAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "NORMAL", Nombre: $translate.instant('NORMAL').toUpperCase() },
					    { Codigo: "ATRASADO", Nombre: $translate.instant('ATRASADO').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPensionado = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "SI", Nombre: $translate.instant('SI').toUpperCase() },
					    { Codigo: "NO", Nombre: $translate.instant('NO').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxTrabajadorSAAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "DEP", Nombre: $translate.instant('DEPENDIENTE').toUpperCase(), 	CodigoFormaPago: 0},
					    { Codigo: "IND", Nombre: $translate.instant('INDEPENDIENTE').toUpperCase(),	CodigoFormaPago: 2 },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRazonRetiro = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Nombre", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6067",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Codigo	: {type: "string"},
	            				Nombre	: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxOrigen = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "CV", Nombre: $translate.instant('CV').toUpperCase() },
					    { Codigo: "DC", Nombre: $translate.instant('DC').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxFondoOrigen = function(){
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "A", Nombre: $translate.instant('A').toUpperCase() },
					    { Codigo: "B", Nombre: $translate.instant('B').toUpperCase() },
					    { Codigo: "C", Nombre: $translate.instant('C').toUpperCase() },
					    { Codigo: "D", Nombre: $translate.instant('D').toUpperCase() },
					    { Codigo: "E", Nombre: $translate.instant('E').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoPagoSAAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "IND", Nombre: $translate.instant('INDEFINIDO').toUpperCase() },
					    { Codigo: "TEM", Nombre: $translate.instant('TEMPORAL').toUpperCase() },
					    { Codigo: "UNI", Nombre: $translate.instant('UNICO').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxOrigenTraspaso = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "ING", Nombre: $translate.instant('INGRESO').toUpperCase() },
					    { Codigo: "EGR", Nombre: $translate.instant('EGRESO').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoSolicitud = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
					placeholder: $translate.instant("UI_SELECCIONAR"),
					dataTextField: "Nombre",
					dataValueField: "Codigo",
					autoBind: true,
					dataSource: new kendo.data.DataSource({
		            	type: "odata",
		            	sort: [{ field: "Codigo", dir: "asc" }],
		            	transport: {
		            		read: {
		            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6075",
								dataType:"json"
		            		}, 
		            	},
		            	schema: { 
							model: {
			                    fields: {
			                    	Nombre	: {type: "string"},
			                    	Codigo 	: {type: "string"}
		                        }
	                   		}
						}
		            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRegimenTributario = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Codigo",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "CodigoAgrupador", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6020",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	CodigoAgrupador			: {type: "string"},
		                    	Codigo 					: {type: "string"},
		                    	IdMatrizAgrupadorAPV	: {type: "number"},
		                    	IdProductoAgrupador		: {type: "number"},
		                    	ProductoAgrupador		: {type: "number"},
		                    	IdTipoPlanInversion		: {type: "string"},
	            				IdEstado 				: {type: "string"},
	            				CodigoRegimenTipoAhorro : {type: "string"},
	            				EstadoRecaudacion 		: {type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxProductoAgrupador = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreProductoAgrupador",
				dataValueField: "IdProductoAgrupador",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreProductoAgrupador", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6040",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdProductoAgrupador		: {type: "number"},
		                    	NombreProductoAgrupador : {type: "string"},
		                    	IdPlanTipoInversion		: {type: "number"},
	            				IdEstado 				: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRegimenAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Codigo",
				dataValueField: "IdMatrizAgrupadorAPV",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "CodigoAgrupador", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6031",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdMatrizAgrupadorAPV	: {type: "number"},
		                    	NombreAgrupador			: {type: "string"},
		                    	CodigoAgrupador			: {type: "string"},
		                    	NombrePlan				: {type: "string"},
		                    	Codigo					: {type: "string"},
		                    	IdPropositoPortafolio 	: {type: "string"},
		                    	Producto				: {type: "string"},
	            				IdEstado 				: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getConfigComboboxOrigenAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Nombre", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6035",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdOrigen	: {type: "number"},
		                    	Codigo 		: {type: "string"},
		                    	Nombre 		: {type: "string"},
		                    	Nombre 		: {type: "string"},
		                    	Tipo 		: {type: "string"},
	            			IdEstado 	: {type: "string"},
		                    	DestinoCtaBco 	: {type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigFormaPagoDirecta = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: 1, Nombre: $translate.instant('DIRECTAMENTE AL TRABAJADOR').toUpperCase() },
					    { Codigo: 2, Nombre: $translate.instant('A TRAVES DEL EMPLEADOR').toUpperCase() },
					    { Codigo: 3, Nombre: $translate.instant('PAC').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigFormaPagoIndirecta = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: 1, Nombre: $translate.instant('IPS-EX INP').toUpperCase() },
					    { Codigo: 2, Nombre: $translate.instant('AFP').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigTipoAhorro = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: 'DC', Nombre: $translate.instant('DC').toUpperCase() },
					    { Codigo: 'APV', Nombre: $translate.instant('APV').toUpperCase() },

					  ],
				})
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigTipoCasilla = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "TipoCasilla",
				dataValueField: "TipoCasilla",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { TipoCasilla: 'ELECTRONICA'},
						{ TipoCasilla: 'FISICA'}
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigTipoTraspaso = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: 'PAR', Nombre: $translate.instant('PARCIAL').toUpperCase() },
					    { Codigo: 'TOT', Nombre: $translate.instant('TOTAL').toUpperCase() },
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigSucursalBancaria = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreSucursal",
				dataValueField: "IdSucursal",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdSucursal", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6017",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdSucursal		: {type: "number"},
		                    	NombreSucursal	: {type: "string"},
		                    	IdBanco 		: {type: "number"},
		                    	IdEstado 		: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCargaArchivo = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreTipoCarga",
				dataValueField: "IdTipoCarga",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdTipoCarga", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6012",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdEmpresa		: {type: "number"},
		                    	IdTipoCarga		: {type: "number"},
		                    	NombreTipoCarga : {type: "string"},
		                    	CodigoTipoCarga : {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	//
	obj.getConfigComboboxDiaDescuento = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "ValorDia",
				dataValueField: "ValorDia",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "ValorDia", dir: "desc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6063",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdParametro	: {type: "number"},
		                    	ValorDia	: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAnoAhorroBonificacionAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "AnoAhorro",
				dataValueField: "AnoAhorro",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "AnoAhorro", dir: "desc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6083",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	AnoAhorro 	: {type	: "number"},	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxAnoProcesoBonificacionAPV = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "AnoProceso",
				dataValueField: "AnoProceso",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "AnoProceso", dir: "desc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6084",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	AnoProceso 	: {type	: "number"},	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxProcesoOrigenDestino= function()
	{
		var translate_array = ['UI_PROCESO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PROCESO,
	            dataTextField: 'Operacion',
	            dataValueField: 'IdOrigenDestinoTransicion',
	            filter: "contains",
	            autoBind: true,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	sort: { field: "Operacion", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6087",
							dataType:"json"
	            		}
	            	},
	            	schema 			: {
						model	: {
		                    fields	: {
		                    	IdEmpresa					: { type: "number",editable: false	},
		                    	IdOrigenDestinoTransicion	: { type: "number",editable: false 	},
		                    	Operacion					: { type: "string",editable: false 	},
		                    	MueveCuentaCorriente		: { type: "string",editable: false 	},
		                    	MueveCuentaInversion		: { type: "string",editable: false 	},
		                    	OrigenCtaBco				: { type: "number",editable: false 	},
		                    	SocioNegocioCtaBco			: { type: "number",editable: false 	},
		                    	DestinoCtaBco				: { type: "number",editable: false 	},
		                    	IdEstado					: { type: "string",editable: false 	},
		                    	HabilitaCuentaOrigen		: { type: "string",editable: false	},
	                        }
                   		}
					},
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCuentasBancariasSocioNegocioCorredora= function()
	{
		var translate_array = ['UI_CUENTA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CUENTA,
	            dataTextField: 'NombreCuentaBancariaFull',
	            dataValueField: 'IdCuentaBancaria',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "NombreCuentaBancariaFull", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6088",
							dataType:"json"
	            		}
	            	},
	            	schema 			: {
						model	: {
		                    fields	: {
		                    	IdCuentaBancaria			: { type: "number",editable: false	},
		                    	NombreCuentaBancariaFull	: { type: "string",editable: false 	},
		                    	NombreBanco					: { type: "string",editable: false 	},
		                    	NumeroCuentaBancaria		: { type: "string",editable: false 	},
		                    	IdEmpresa					: { type: "number",editable: false 	},
	                        }
                   		}
					},
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPerfilComercialAPV = function()
	{
		var translate_array = ['UI_MODELO_COMERCIAL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MODELO_COMERCIAL,
	            dataTextField:  'IdPerfilComercialBase',
	            dataValueField: 'IdPerfilComercialBase',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdPerfilComercialBase", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6110",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
		
	obj.getConfigComboboxDocumentoIdentidadNomina = function()
	{
		var translate_array = ['UI_RUT','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "or",
				      filters: [
				         {"field": "toupper(NombreCompleto)" ,"operator":"contains", "value": text.toUpperCase() },
				         {"field": "toupper(DocumentoIdentidadDefecto)" ,"operator":"startswith", "value": text.toUpperCase().replace(/\./g, "") },
				      ]
			    	};
					filter.push(nfilters);
				},
				permiteRepetidos : true,			
				placeholder: translations.UI_RUT,
				KdataTextTemplate:  function(row) {
					var tipos="";
					if (row.EsCliente !='UI_SI'      && row.EsEmpleado !='UI_SI'  && 
						    row.EsOperador !='UI_SI' && row.EsProveedor!='UI_SI'  && 
						    row.EsContraparte!='UI_SI' ) {
						tipos="[" + $translate.instant('UI_PERSONA') + (row.EstadoPersona=='ACT' ? "]" : ",X]");						
					}else {
						tipos="[";
						if (row.EsCliente == 'UI_SI' )     tipos+=(row.EstadoCliente =='ACT'     ?  "C |" : "CX|");						
						else tipos+='--|';
						if (row.EsEmpleado == 'UI_SI' )    tipos+=(row.EstadoEmpleado =='ACT'    ?  "E |" : "EX|");						
						else tipos+='--|';
						if (row.EsOperador == 'UI_SI' )    tipos+=(row.EstadoOperador =='ACT'    ?  "O |" : "OX|");						
						else tipos+='--|';
						if (row.EsProveedor == 'UI_SI' )   tipos+=(row.EstadoProveedor =='ACT'   ?  "P |" : "PX|");						
						else tipos+='--|';			
						if (row.EsContraparte == 'UI_SI' ) tipos+=(row.EstadoContraparte =='ACT' ?  "CP " : "CPX");						
						else tipos+='--';						
						tipos+="]";							
					}	
					tipos=tipos.indexOf("X")>-1?" [X]":"";
					if (row.IdTipoDocumentoIdentidad == 'R') {
						return row.DocumentoIdentidadDefecto + ' '+row.NombreCompleto+tipos;
					}else {
						return row.DocumentoIdentidadDefecto + ' '+row.NombreCompleto+tipos;
					}
				},
				dataTextField: "KdataTextTemplate",
	            dataValueField: "DocumentoIdentidadDefecto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "NombreCompleto", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6113",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    }	 	            		
	            	},
					schema: {
						model: {
		                    fields: {
	                       		NombreCompleto				: { type: "string"},
	                       		DocumentoIdentidadDefecto	: { type: "string"},
	                       		DocumentoEntidad			: { type: "string"},
	                       		NombreEntidad				: { type: "string"},
	                       		IdEmpresa					: { type: "number"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxPeriodosComisiones = function()
    {
        var translate_array = ['UI_PERIODO','UI_TODOS'];
        configPromise = $translate(translate_array).then(function (translations) {
            var config = {};
            // configuracion del combo
            config = {
                placeholder: translations.UI_PERIODO,
                dataTextField: "Descripcion",
                dataValueField: "IdPeriodo",
                filter: "startswith",
                autoBind: false,
                minLength: minAutocomplete,
                dataSource: new kendo.data.DataSource({
                    type: "odata",
                    serverFiltering: false,
                    serverSorting: false,  // ordena al lado del servidor
                    sort: { field: "IdPeriodo", dir: "asc" }, // campo y
                                                                    // criterio
                                                                    // de
                                                                    // ordenamiento
                    transport: {
                        read: {
                            url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702165",
                            dataType:"json"
                        }
                    }, 
                    schema: { 
                        model: { 
                            fields : {
                                IdPeriodo               : { type:"number" },
                                Descripcion             : { type:"string" },
                                FechaDesde              : { type:"date" },
                                FechaHasta              : { type:"date" }                           
                            }   
                        }  
                    },
                })
            };
            return config;
        });
        return configPromise;
    };
    
    obj.getConfigComboboxOpcionesCobroDisponible = function()
    {
        var translate_array = ['UI_OPCIONES_COBRO','UI_TODOS'];
        configPromise = $translate(translate_array).then(function (translations) {
            var config = {};
            // configuracion del combo
            config = {
                placeholder: translations.UI_OPCIONES_COBRO,
                dataTextField: "DetalleIndicador",
                dataValueField: "IdDetalleIndicador",
                filter: "startswith",
                autoBind: false,
                minLength: minAutocomplete,
                dataSource: new kendo.data.DataSource({
                    type: "odata",
                    serverFiltering: false,
                    serverSorting: false,  // ordena al lado del servidor
                    sort: { field: "IdDetalleIndicador", dir: "asc" }, // campo y
                                                                    // criterio
                                                                    // de
                                                                    // ordenamiento
                    transport: {
                        read: {
                            url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702166",
                            dataType:"json"
                        }
                    }, 
                    schema: { 
                        model: { 
                            fields : {
                                IdDetalleIndicador               : { type:"number" },
                                DetalleIndicador             : { type:"string" }                          
                            }   
                        }  
                    },
                })
            };
            return config;
        });
        return configPromise;
    };
    
    obj.getConfigComboboxCobroComisionMercados = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION,
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionTipoInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702157",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxDocumentoEntidadNomina = function()
	{
		var translate_array = ['UI_DOCUMENTO_ENTIDAD','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				autocomplete: true,
				autocompleteFn : function(filter,text,row) {
			    	var nfilters = {
				      logic: "or",
				      filters: [
				         {"field": "toupper(NombreEntidad)" ,"operator":"contains", "value": text.toUpperCase() },
				         {"field": "toupper(DocumentoEntidad)" ,"operator":"startswith", "value": text.toUpperCase().replace(/\./g, "") },
				      ]
			    	};
					filter.push(nfilters);
				},
				permiteRepetidos : true,			
				placeholder: translations.UI_DOCUMENTO_ENTIDAD,
				KdataTextTemplate:  function(row) {
					var tipos="";
					if (row.EsCliente !='UI_SI'      && row.EsEmpleado !='UI_SI'  && 
						    row.EsOperador !='UI_SI' && row.EsProveedor!='UI_SI'  && 
						    row.EsContraparte!='UI_SI' ) {
						tipos="[" + $translate.instant('UI_PERSONA') + (row.EstadoPersona=='ACT' ? "]" : ",X]");						
					}else {
						tipos="[";
						if (row.EsCliente == 'UI_SI' )     tipos+=(row.EstadoCliente =='ACT'     ?  "C |" : "CX|");						
						else tipos+='--|';
						if (row.EsEmpleado == 'UI_SI' )    tipos+=(row.EstadoEmpleado =='ACT'    ?  "E |" : "EX|");						
						else tipos+='--|';
						if (row.EsOperador == 'UI_SI' )    tipos+=(row.EstadoOperador =='ACT'    ?  "O |" : "OX|");						
						else tipos+='--|';
						if (row.EsProveedor == 'UI_SI' )   tipos+=(row.EstadoProveedor =='ACT'   ?  "P |" : "PX|");						
						else tipos+='--|';			
						if (row.EsContraparte == 'UI_SI' ) tipos+=(row.EstadoContraparte =='ACT' ?  "CP " : "CPX");						
						else tipos+='--';						
						tipos+="]";							
					}	
					tipos=tipos.indexOf("X")>-1?" [X]":"";
					if (row.IdTipoDocumentoIdentidad == 'R') {
						return row.DocumentoEntidad + ' '+row.NombreEntidad+tipos;
					}else {
						return row.DocumentoEntidad + ' '+row.NombreEntidad+tipos;
					}
				},
				dataTextField: "KdataTextTemplate",
	            dataValueField: "DocumentoEntidad",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: { field: "NombreEntidad", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6112",
							dataType:"json"
	            		},
	                    parameterMap: function(data) {
	                    	var obj=kendo.data.transports.odata.parameterMap(data);
	                    	if (obj.$inlinecount !== undefined) obj.$inlinecount="none";	                    	
	                        return  obj;
	                    }	 	            		
	            	},
					schema: {
						model: {
		                    fields: {
		                    	NombreEntidad		: { type: "string"},
	                       		DocumentoEntidad	: { type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	/* ----------- COMBOBOX CONTRATO PORTAFOLIO      -------------*/
	
	obj.getConfigComboboxContratoPortafolio = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField:  "IdPortafolio",
				dataValueField: "IdPortafolio",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdPortafolio", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6023",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdPortafolio	: {type: "number"},
		                    	IdCliente		: {type: "number"},
		                    	IdAgrupador		: {type: "number"},
		                    	IdRelacion		: {type: "number"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxIndicadoresArchivoAGF = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreTipoIndicador",
				dataValueField: "IdTipoIndicador",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreTipoIndicador", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4216",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	NombreTipoIndicador		: {type: "string"},
		                    	IdTipoIndicador			: {type: "number"},
		                    	CodigoTipoIndicador		: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCodigoIFRS = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Descripcion",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Codigo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4218",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Codigo		: {type: "string"},
		                    	Descripcion			: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCertificadosPrestamos = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreTipoCertificadoPrestamo",
				dataValueField: "IdTipoCertificadoPrestamo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreTipoCertificadoPrestamo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4228",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdTipoCertificadoPrestamo		: {type: "string"},
		                    	NombreTipoCertificadoPrestamo	: {type: "string"},
		                    	IdArchivoCustodia				: {type: "string"},
		                    	IdReporte						: {type: "string"},
		                    	NombreArchivo					: {type: "string"},
		                    	ServicioArchivo					: {type: "number"},
		                    	RutaServicio					: {type: "string"},
		                    	Descripcion						: {type: "string"},
		                    	IdEmpresa						: {type: "number"},
		                    	IdEstado						: {type: "string"},
		                    	ReportName						: {type: "string"},
		                    	ReportVersion					: {type: "number"},
		                    	TipoArchivo						: {type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxContratoSAAPV= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "FolioContratoSAAPV",
				dataValueField: "IdContratoSAAPV",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "RutCliente", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6049",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Nombres			: {type: "string"},
		                    	RutCliente		: {type: "string"},
		                    	IdContratoSAAPV	: {type: "number"},
		                    	Portafolio		: {type: "number"},
		                    	Estado			: {type: "string"},
		                    	TipoContrato	: {type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}

	
	obj.getConfigComboboxEventoTipoDato = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreTipoDatoEvento",
				dataValueField: "IdTipoDatoEvento",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdTipoDatoEvento", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4231",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdTipoDatoEvento : {type: "string"},
		                    	NombreTipoDatoEvento : {type: "string"},
		                    	TipoDato		: {type: "string"},
		                    	ValorLibre		: {type: "string"},
		                    	LargoVarchar	: {type: "number"},
		                    	Enteros			: {type: "number"},
		                    	Decimales		: {type: "number"},
		                    	FormatoEdicion	: {type: "string"},
		                    	DatosPreDefinicion : {type: "string"},
		                    	Estado : {type: "string"},
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxEventoValorTipoDato = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Descripcion",
				dataValueField: "Valor",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdTipoDatoEvento", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4232",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdTipoDatoEvento : {type: "string"},
		                    	Valor		: {type: "string"},
		                    	Descripcion		: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxInstrumentoFondoMutuoCustodia = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			var ds = new bcs.DataSource({
            	type: "odata",
	            definition: {
	            	sort: { field: "IdSerieFondo", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3023",
							dataType:"json"
	            		}
	            	},schema : {
	    				model: {
	    					fields : {
	    						FechaFondo: {type: "sysdate"}
	    					}
	    				}
	    			}
	            }
            });
			config = {
				placeholder: '...',
	            dataTextField: "IdSerieFondoNombre",
	            dataValueField: "IdSerieFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: ds.ds
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxSerieFondoMutuoCustodia = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			var ds = new bcs.DataSource({
            	type: "odata",
	            definition: {	
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdSerieFondo", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3023",
							dataType:"json"
	            		}
	            	},schema : {
	    				model: {
	    					fields : {
	    						FechaFondo: {type: "sysdate"}
	    					}
	    				}
	    			}
	            }
            });
			config = {
				placeholder: '...',
	            dataTextField: "NombreSerieFondo",
	            dataValueField: "IdSerieFondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: ds.ds
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxConfiguracionMetadata = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "CodigoMetadata",
				dataValueField: "IdMetadataCuentaContable",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "IdMetadataCuentaContable", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4230",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdMetadataCuentaContable : {type: "number"},
		                    	CodigoMetadata		: {type: "string"},
		                    	DescripcionMetadata		: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}

	obj.getConfigComboboxRangoDiasTraspaso = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Codigo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6062",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdMetadataCuentaContable : {type: "number"},
		                    	CodigoMetadata		: {type: "string"},
		                    	DescripcionMetadata		: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxGlosaAPV= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Descripcion",
				dataValueField: "IdGlosa",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					type: "odata",
	            	sort: [{ field: "IdGlosa", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6068",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdGlosa		: {type: "number"},
								Descripcion : {type: "string"},
								Codigo		: {type: "string"},
		                    	Nombre		: {type: "string"},
								EstadoGlosa : {type: "string"}	      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigTipoCargaConfiguracionCon= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Descripcion",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					type: "odata",
	            	sort: [{ field: "Codigo", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4248",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Codigo		: {type: "string"},
		                    	Descripcion : {type: "string"},
		                    	Formato		: {type: "string"},
		                    	EjemploLinea: {type: "string"}	      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxOtroProductoAgrupador = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreProductoAgrupador",
				dataValueField: "IdProductoAgrupador",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Orden", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6076",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Orden					: {type: "number"},
		                    	IdProductoAgrupador		: {type: "number"},
		                    	NombreProductoAgrupador : {type: "string"},
	            				IdEstado 				: {type: "string"}
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	/**************************************/
	/*** FIN APV						***/
	/**************************************/
	
	obj.getConfigParametroFinanciero = function(){
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "IdParametroFinanciero",
				dataValueField: "NombreParametroFinanciero",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					type: "odata",
	            	sort: [{ field: "IdParametroFinanciero", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4257",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Fecha  							: { type: "sysdate"},
								IdParametroFinanciero  			: { type: "string"},
								AgnoMes  						: { type: "string"},
								Agno  							: { type: "string"},
								Valor							: { type: "number"},
								FechaActualizacion  			: { type: "sysdatetime"}	      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxTipoConcepto = function()
	{
		var translate_array = ['UI_TIPO_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_TIPO_CONCEPTO,
	            dataTextField: "TipoConcepto",
	            dataValueField: "TipoConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "array",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "CodigoRegistro", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	data : [
		        		{"TipoConcepto":"MANUAL"}, 
		        		{"TipoConcepto":"SISTEMA"}
		            ]
	            })
			};
			return config;	
		});
		return configPromise;
	};
	
	obj.getConfigComboboxNegociacion = function()
	{
		var translate_array = ['UI_NEGOCIACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_NEGOCIACION,
	            dataTextField: "Negociacion",
	            dataValueField: "CodigoNegociacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "array",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Negociacion", dir: "asc" }, // campo y
																// criterio
																// de
																// ordenamiento
	            	data : [
		        		{"Negociacion" : $translate.instant('UI_BURSATIL'), "CodigoNegociacion" : 'N'}, 
		        		{"Negociacion" : $translate.instant('UI_FUERA_DE_RUEDA'), "CodigoNegociacion" : 'S'}
		            ]
	            })
			};
			return config;	
		});
		return configPromise;
	};	

	obj.getConfigComboboxGenerarInterfaz = function()
	{
		var translate_array = ['UI_INTERFAZ','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_INTERFAZ,
	            dataTextField: "Nombre",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700036",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxGenerarTipoInterfaz = function()
	{
		var translate_array = ['UI_INTERFAZ','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_INTERFAZ,
	            dataTextField: "Nombre",
	            dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,                  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700037",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxMercadosCreados = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION,
	            dataTextField: "Descripcion",
	            dataValueField: "IdArbolClasificacionActivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601163",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMercadosTodosCreados = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CLASIFICACION,
	            dataTextField: "Descripcion",
	            dataValueField: "IdArbolClasificacionActivo2",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601171",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	//RAM
	obj.getConfigComboboxSubMercadosCreados = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		//console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "SUBMERCADO",
	            dataTextField: "Descripcion",
	            dataValueField: "IdArbolClasificacionActivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601165",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	//INSTRUMENTO
	obj.getConfigComboboxClasificacionInstrumento = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		//console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: $translate.instant("UI_CLASIFICACION_INSTRUMENTO"),
	            dataTextField: "Descripcion",
	            dataValueField: "IdArbolClasificacionActivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601165",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxInstrumentosClasificados = function()
	{
		var translate_array = ['UI_CLASIFICACION','UI_TODOS'];
		//console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "INSTRUMENTO",
	            dataTextField: "Nemotecnico",
	            dataValueField: "IdRelacionClasificacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nemotecnico", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601166",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	obj.getConfigComboboxNuevoMercado = function()
	{
		var translate_array = ['UI_MOVER_A_MERCADO','UI_TODOS'];
		//console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MOVER_A_MERCADO,
	            dataTextField: "Descripcion",
	            dataValueField: "IdArbolClasificacionActivo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Descripcion", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601163",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	

	obj.getConfigComboboxTipoClasInstrumentoAsignar = function()
	{
		var translate_array = ['UI_TIPOCLASIFICACIONINSTRUMENTO','UI_TODOS'];
		console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "TIPO CLASIFICACION INSTRUMENTO",
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionTipoInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601168",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxClasInstrumentoAsignar = function()
	{
		var translate_array = ['UI_OCLASIFICACIONINSTRUMENTO','UI_TODOS'];
		console.log("llamada a clasificaciontipoinstrumento");
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "CLASIFICACION INSTRUMENTO",
	            dataTextField: "Nombre",
	            dataValueField: "IdClasificacionTipoInstrumento",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Nombre", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601170",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	// RAM
	obj.getConfigTipoIndice = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "DI", Nombre: $translate.instant('DIARIO') },
					    { Codigo: "ME", Nombre: $translate.instant('MENSUAL') },
					    { Codigo: "AN", Nombre: $translate.instant('ANUAL') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigFrecuenciaTipoCambio = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "DI", Nombre: $translate.instant('DIARIO') },
					    { Codigo: "ME", Nombre: $translate.instant('MENSUAL') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigUsoParidadYTipoCambio = function(){
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreUso",
				dataValueField: "IdUso",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					type: "odata",
	            	sort: [{ field: "IdUso", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=600068",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdUso  							: { type: "string"},
		                    	IdEmpresa  						: { type: "number"},
		                    	TipoBusqueda  					: { type: "string"},
		                    	NombreUso  						: { type: "string"},
		                    	DescripcionUso					: { type: "number"}	      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigTipoIndiceAnexo7 = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "ME", Nombre: $translate.instant('UI_MES') },
					    { Codigo: "AN", Nombre: $translate.instant('UI_ANO') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
    
    	obj.getConfigTipoDocumentoVector = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "BLM", Nombre: $translate.instant('BLOOMBERG') },
					    { Codigo: "NAV", Nombre: $translate.instant('NAV') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigTipoDocumentoVector = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "BLM", Nombre: $translate.instant('BLOOMBERG') },
					    { Codigo: "NAV", Nombre: $translate.instant('NAV') }
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getFolioContratoCSC= function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "FolioCSC",
				dataValueField: "FolioCSC",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					type: "odata",
	            	sort: [{ field: "FolioCSC", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=600079",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	FolioCSC	: {type: "string"},
		                    	TipoPago  : {type: "string"}     
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	//CELZORRO-272
	obj.getConfigComboboxIndice = function()
	{
		var translate_array = ['UI_INDICE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_INDICE,
				dataTextField: "IdIndiceFinanciero", //El que muestra
	            dataValueField: "IdIndiceFinanciero", //El que captura
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdIndiceFinanciero", dir: "asc" }, //el campo que ordena
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601210",
							dataType:"json"
	            		},
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Nombre	     : {type: "string"},
		                    	Descripcion  : {type: "string"}     
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	//CELZORRO-272
	
////////////////INDICE CLASIFICACION DE ACTIVOS - CELZORRO-273
    obj.getConfigComboboxIndices_Todos = function()
    {
          var translate_array = ['UI_INDICES','UI_TODOS'];
          console.log("indices busca");
          configPromise = $translate(translate_array).then(function (translations) {
                 var config = {};
                 // configuracion del combo
                 config = {
                        placeholder: $translate.instant("UI_INDICE"),
                dataTextField: "Indice",
                dataValueField: "Indice",
                filter: "contains",
                autoBind: false,
                minLength: minAutocomplete,
                dataSource: new kendo.data.DataSource({
                type: "odata",
                 serverFiltering: false,
                serverSorting: false,  // ordena al lado del servidor
                sort: { field: "Indice", dir: "asc" }, // campo y criterio
                                                                                                  // de ordenamiento
                transport: {
                        read: {
                               url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601211",
                                            dataType:"json"
                        }
                }
                })
                 };
                 return config;
          });
          return configPromise;
    };
    
    obj.getConfigComboboxModalidadEjercicio = function()
	{
		var translate_array = ['UI_MODALIDAD_EJERCICIO_OPCIONES','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreParametro',
	            dataValueField: 'ValorParametro',
	            filter: "startswith",	            
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "NombreParametro", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700144",
							dataType:"json"
	            		} 
	            	}
	            	
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxParametroEjercicio = function()
	{
		var translate_array = ['UI_PARAMETRO_EJERCICIO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreParametro',
	            dataValueField: 'ValorParametro',
	            filter: "startswith",	            
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "NombreParametro", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700145",
							dataType:"json"
	            		} 
	            	}
	            	
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	
	
	obj.getConfigComboboxContactos = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "",
				dataTextField: 'Nombre',
	            dataValueField: 'IdPersona', // devuelve Id de persona del contacto 
	            autoBind: false,
				minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
					serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=499",
							dataType:"json"
						}
						
					},
					schema: { 
						model: {
		                    fields: {
		                    	IdCliente	     			: {type: "number"},
		                    	Nombre 		 				: {type: "string"},
		                    	IdTipoRelacion 		 		: {type: "string"},
		                    	IdPersona					: {type: "number"}
	                        }
                   		}
					}
	            
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	
	obj.getConfigComboboxDocumentosMesa = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "",
				dataTextField: 'Nombre',
	            dataValueField: 'Codigo',
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=498",
							dataType:"json"
	            		}
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Codigo	     				: {type: "string"},
		                    	Nombre 		 				: {type: "string"},
		                    	IdTipoOperacion 		 	: {type: "string"},
		                    	TipoOperacionMesa			: {type: "string"}
	                        }
                   		}
					}
	            
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxAFPs = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "AFP",
	            dataTextField: "NombreAFP",
	            dataValueField: "IdAFP",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreAFP", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290600",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMultifondo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "MULTIFONDO",
	            dataTextField: "NombreMultifondo",
	            dataValueField: "IdMultifondo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "NombreMultifondo", dir: "asc" }, // campo
																		// y
																	// criterio
																	// de
																	// ordenamiento
	            	transport: {
	            		read: {
	            			url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290601",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMercadoFFMM = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: '...',
	            dataTextField: "Nombre",
	            dataValueField: "IdMercadoFondoMutuo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdMercadoFondoMutuo", dir: "asc" }, // campo y
																// criterio de
																// ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700169",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxCodigoTipoRelacion = function()
	{
		var translate_array = ['UI_TIPO_RELACION'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_RELACION,
	            dataTextField: "DescripcionTipoRelacion",
	            dataValueField: "CodigoTipoRelacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "DescripcionTipoRelacion", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3609",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxRepresentanteLegal = function()
	{
		var translate_array = ['UI_REPRESENTANTE_LEGAL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_REPRESENTANTE_LEGAL,
	            dataTextField: "NombreCompletoRepresentante",
	            dataValueField: "IdPersonaRepresentante",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "NombreCompletoRepresentante", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4214",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRepresentanteLegal = function()
	{
		var translate_array = ['UI_REPRESENTANTE_LEGAL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_REPRESENTANTE_LEGAL,
	            dataTextField: "NombreCompletoRepresentante",
	            dataValueField: "IdPersonaRepresentante",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "NombreCompletoRepresentante", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4214",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	

	obj.getConfigComboboxRepresentanteLegalCorredor = function()
	{
		var translate_array = ['UI_REPRESENTANTE_LEGAL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_REPRESENTANTE_LEGAL,
	            dataTextField: "NombreCompletoRepresentante",
	            dataValueField: "IdPersonaRepresentante",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "NombreCompletoRepresentante", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4300005",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxRepresentanteLegalEnrolar = function()
	{
		var translate_array = ['UI_REPRESENTANTE_LEGAL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_REPRESENTANTE_LEGAL,
	            dataTextField: "NombreCompletoRepresentante",
	            dataValueField: "IdPersonaRepresentante",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "NombreCompletoRepresentante", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4300006",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};		
	
	obj.getConfigComboboxRifiDifi = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
					data: [
						{ Codigo: "ALL", Nombre: $translate.instant('UI_TODOS') },
						{ Codigo: "RIFI", Nombre: $translate.instant('UI_RIFI') },
						{ Codigo: "DIFI", Nombre: $translate.instant('UI_DIFI') }
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxInformeCobroComisionesMensuales = function()
	{
		var translate_array = ['UI_TIPO_INFORME','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_TIPO_INFORME,
				dataTextField: "Nombre",
				dataValueField: "Codigo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
					data: [
						{ Codigo: "CCDET", Nombre: $translate.instant('UI_CC_DETALLADO') },
						{ Codigo: "CCRES", Nombre: $translate.instant('UI_CC_RESUMIDO') },
						{ Codigo: "MZCOM", Nombre: $translate.instant('UI_MZ_COMISIONES') },
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	
// //////////////INDICE CLASIFICACION DE ACTIVOS - CELZORRO-273

	obj.getConfigComboboxFormulario = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMULARIO,
	            dataTextField: "NombreFormulario",
	            dataValueField: "CodigoFormulario",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "NombreFormulario", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700199",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCampo = function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CAMPO,
	            dataTextField: "NombreCampo",
	            dataValueField: "CodigoCampo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  				   // ordena al lado
															// del servidor
	            	sort: { field: "NombreCampo", dir: "asc" }, // campo y criterio
														   // de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700200",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxNormativaHomologacion = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "",
				dataTextField: 'Descripcion',
	            dataValueField: 'ValorDestino',
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290640",
							dataType:"json"
	            		}
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Tipo	     			: {type: "string"},
		                    	Descripcion 		 	: {type: "string"},
		                    	ValorOrigen 		 	: {type: "string"},
		                    	ValorDestino			: {type: "string"},
								Esquema	     			: {type: "string"},
		                    	Version 		 		: {type: "string"},
		                    	FechaDatosDesde 		: {type: "date"},
		                    	FechaDatosHasta			: {type: "date"},
	                        }
                   		}
					}
	            
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCausaNoTin = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: "",
				dataTextField: 'Descripcion',
	            dataValueField: 'IdCausaNoTin',
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700227",
							dataType:"json"
	            		}
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	IdCausaNoTin	: {type: "string"},
		                    	Descripcion 	: {type: "string"},
	                        }
                   		}
					}
	            
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTransparenciaFiscal = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				/*placeholder: $translate.instant("UI_TRANSPARENCIA_FISCAL_CONNACIONAL"),*/
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "CONNACIONAL", Nombre: $translate.instant('UI_TRANSPARENCIA_FISCAL_CONNACIONAL')},
					    { Codigo: "US-PERSON-FACTA", Nombre: $translate.instant('UI_TRANSPARENCIA_FISCAL_US_PERSON')},
					    { Codigo: "OCDE-PERSON-CRS", Nombre: $translate.instant('UI_TRANSPARENCIA_FISCAL_OCDE_PERSON')},
					    { Codigo: "OTRO", Nombre: $translate.instant('UI_TRANSPARENCIA_FISCAL_OTRO')}
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoLiquidacionComisionFactura = function()
	{
		var translate_array = ['UI_LIQUIDACION_COMISION_FACTURA'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_LIQUIDACION_COMISION_FACTURA,
				dataTextField: "TipoLiquidacion",
	            dataValueField: "IdTipoLiquidacionComisionFactura",
	            filter: "contains",
	            autoBind: false,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	sort: { field: "IdTipoLiquidacionComisionFactura ", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700274",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxArchivosContratos = function () {
		return $translate(['UI_PLANTILLA_CONTRATO']).then(function (translations) {
			return {
				placeholder: translations.UI_PLANTILLA_CONTRATO,
				dataTextField: 'CodigoArchivo',
				dataValueField: 'CodigoArchivo',
				filter: 'contains',
				autoBind: false,
				dataSource: new kendo.data.DataSource({
					type: 'odata',
					filter : { field : "IdEstado", operator : "eq", value : "ACT" },
					serverFiltering: true,
					serverPaging: false,
					sort: { field: 'Descripcion', dir: 'asc' },
					transport: {
						read: {
							url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=1421',
							dataType: 'json',
							data: {
								$select: function () {
									return '"IdEmpresa", "CodigoArchivo", "Descripcion", "IdEstado", "IdTipoPersona", "TipoCliente", "IdTipoClienteCMF"'
								}
							}
						}
					},
					schema: {
						model: {
							fields: {
								IdEmpresa: { type: 'number' },
								CodigoArchivo: { type: 'string' },
								Descripcion: { type: 'string' },
								IdEstado: { type: 'string' },
								IdTipoPersona: { type: 'string' },
								TipoCliente: { type: 'string' },
								IdTipoClienteCMF: { type: 'string' },
							}
						}
					}
				})
			};
		});
	};
	obj.getConfigComboboxFirmantesCliente = function () {
		return $translate(['UI_PERSONA_FIRMANTE']).then(function (translations) {
			return {
				placeholder: translations.UI_FIRMANTES_CLIENTE,
				dataTextField: 'NombreCompleto',
				dataValueField: 'IdPersona',
				filter: 'contains',
				autoBind: false,
				dataSource: new kendo.data.DataSource({
					type: 'odata',
					serverFiltering: true,
					serverPaging: false,
					sort: { field: 'NombreCompleto', dir: 'asc' },
					transport: {
						read: {
							url: '/BSCL/Plataforma/GWJAVA/PLA/call?t=700320',
							dataType: 'json',
						}
					},
					schema: {
						model: {
							fields: {
								IdPersona: { type: 'number' },
								NombreCompleto: { type: 'string' },
								IdPersonaOrigen: { type: 'string' }
							}
						}
					}
				})
			};
		});
	};
	
	obj.getConfigComboboxComercialTipoContrato = function()
	{
		var translate_array = ['UI_TIPO_CONTRATO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_TIPO_CONTRATO,
				dataTextField: "Nombre",
	            dataValueField: "IdTipoContrato",
	            filter: "contains",
	            autoBind: false,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	sort: { field: "IdTipoContrato", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700277",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxComercialFormatoCobro = function()
	{
		var translate_array = ['UI_FORMATO_CONTRATO_COBRO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: translations.UI_FORMATO_CONTRATO_COBRO,
				dataTextField: "Nombre",
	            dataValueField: "IdFormatoCobro",
	            filter: "contains",
	            autoBind: false,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	sort: { field: "IdFormatoCobro", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700278",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};	
	
	obj.getConfigComboboxFormatoDeArchivo = function () {

		var translate_array = ['UI_FORMATO_ARCHIVO', 'UI_TODOS'];
		
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {}
			config = {
				placeholder: translations.UI_FORMATO_ARCHIVO,
				dataTextField: "FormatoArchivo",
				dataValueField: "FormatoArchivo",
				filter: "contains",
				autoBind: true,
				minLength: 0,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					serverFiltering: false,
					serverSorting: false,
					sort: { field: "FormatoArchivo", dir: "asc" },
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010284",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								FormatoArchivo: { type: "string" }
							}
						}
					}
				})
			};
			return config;
		});
		return configPromise;
	};	

	obj.getConfigComboboxConceptoOrden = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "NombreConcepto",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700303",	            					
							dataType:"json"
	            		}
	            	},
	            	schema: { 
	            		model: { 
	            			fields : {
			            		IdConcepto				: { type:"string" },
			            		NombreConcepto			: { type:"string" },
			            		IdTipoOperacion         : { type:"string" },
			            		Uso                  : { type:"string" }, 
			            		IdBolsa				: { type:"string" }			            		
	            			}   
	            		}  
	            	},
	                serverSorting: true,  // ordena al lado del servidor
            	    sort: { field: "NombreConcepto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxConceptoContratoComision = function()
	{
		var translate_array = ['UI_CONCEPTO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_CONCEPTO,
	            dataTextField: "NombreConcepto",
	            dataValueField: "IdConcepto",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010264",	            					
							dataType:"json"
	            		}
	            	},
	            	schema: { 
	            		model: { 
	            			fields : {
			            		IdConcepto				: { type:"string" },
			            		NombreConcepto			: { type:"string" },
			            		IdTipoOperacion         : { type:"string" },
			            		Uso                  : { type:"string" }, 
			            		IdBolsa				: { type:"string" },
								IdClasificacionTipoInstrumento : { type:"string" }	            		
	            			}   
	            		}  
	            	},
	                serverSorting: true,  // ordena al lado del servidor
            	    sort: { field: "NombreConcepto", dir: "asc" }, // campo y
																	// criterio
																	// de
																	// ordenamiento
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMonedaTasa = function()
	{
		var translate_array = ['UI_MONEDA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_MONEDA,
	            dataTextField: "NombreLargo",
	            dataValueField: "IdMoneda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "Orden", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700361",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxSituacionLaboral= function()
	{
		var translate_array = ['UI_SITUACION_LABORAL','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SITUACION_LABORAL,
	            dataTextField: 'SituacionLaboral',
	            dataValueField: 'CodigoSituacionLaboral',
				filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
					type: "odata",
	            	serverFiltering: false,
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700369",
							dataType:"json"
	            		}
	            	}
	            	
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoCSC = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreEstado",
				dataValueField: "IdEstado",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreEstado", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700367",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	NombreEstado : {type: "string"},
		                    	IdEstado : {type: "string"}		      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}

	obj.getConfigComboboxDetalleTipoIndicadorFormaCobro = function()
	{
		var translate_array = ['UI_FORMA_COBRO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_FORMA_COBRO,
	            dataTextField:  'DetalleIndicador',
	            dataValueField: 'DetalleIndicador',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,
	            	sort: { field: "DetalleIndicador", dir: "asc" },
					transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1338",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxPortafoliosPorDefecto = function()
	{
		var translate_array = ['UI_PORTAFOLIO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PORTAFOLIO,
	            dataTextField:  'CodigoPortafolio',
	            dataValueField: 'CodigoPortafolio',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,
	            	sort: { field: "CodigoPortafolio", dir: "asc" },
					transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290678",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxProductoBasilea = function(){
		var translate_array = ['UI_PRODUCTO'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_PORTAFOLIO,
	            dataTextField:  'Producto',
	            dataValueField: 'Producto',
	            filter: "contains",
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,
	            	sort: { field: "Producto", dir: "asc" },
					transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=701155",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoMandatoPAC = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "NombreEstado",
				dataValueField: "IdEstado",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "NombreEstado", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700368",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	NombreEstado : {type: "string"},
		                    	IdEstado : {type: "string"}		      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigTipoMotivo = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				dataTextField: "Nombre",
				dataValueField: "Tipo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Tipo: "A", Nombre: $translate.instant('UI_TIPO_MOTIVO_A')},
					    { Tipo: "M", Nombre: $translate.instant('UI_TIPO_MOTIVO_M')}
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigEstadoMotivo= function()
	{
		var translate_array = ['UI_ESTADO_CONFIRMACION','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_ESTADO,
	            dataTextField: 'Nombre',
	            dataValueField: 'IdEstado',
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Nombre", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21044",			
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigMotivo= function()
	{
		var translate_array = ['UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
	            dataTextField: 'Motivo',
	            dataValueField: 'IdMotivo',
	            autoBind: true,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	sort: { field: "Motivo", dir: "asc" },
	            	serverPaging: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700382",			
	            			dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxGananciaCapital = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_GANANCIA_CAP"),
				dataTextField: "Nombre",
				dataValueField: "IdTipoRetencion",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	sort: [{ field: "Nombre", dir: "asc" }],
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306001",
							dataType:"json"
	            		}, 
	            	},
	            	schema: { 
						model: {
		                    fields: {
		                    	Nombre : {type: "string"},
		                    	IdTipoRetencion : {type: "string"}		      
	                        }
                   		}
					}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTipoPegado = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				dataTextField: "Nombre",
				dataValueField: "Tipo",
				filter: "contains",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Tipo: "CANTIDAD", Nombre: $translate.instant('UI_CANTIDAD')},
					    { Tipo: "PORCENTAJE", Nombre: $translate.instant('UI_PORCENTAJE')}
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxTipoPegadoMonto = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				dataTextField: "Nombre",
				dataValueField: "Tipo",
				filter: "contains",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					type: "odata",
					data: [
					    { Tipo: "CANTIDAD", Nombre: $translate.instant('UI_CANTIDAD')},
					    { Tipo: "PORCENTAJE", Nombre: $translate.instant('UI_PORCENTAJE')},
					    { Tipo: "MONTO", Nombre: $translate.instant('UI_MONTO')}
					  ]
				})
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxFFMMDolar = function()
	{
		var translate_array = [];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			config = {
				placeholder: $translate.instant("UI_SELECCIONAR"),
				dataTextField: "Nombre",
				dataValueField: "Codigo",
				autoBind: true,
				dataSource: new kendo.data.DataSource({
					data: [
					    { Codigo: "GENERAL", Nombre: $translate.instant('UI_GENERAL')},
					    { Codigo: "NACIONALES_VALUTA_CERO", Nombre: $translate.instant('UI_NACIONALES_VALUTA_CERO')}
					  ],
				})
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoCargaMasivaMesaRF = function()
	{
		var translate_array = ['UI_TIPO_CARGA','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_TIPO_CARGA,
	            dataTextField: "Nombre",
	            dataValueField: "IdTipoCargaMasiva",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor**
	            	sort: { field: "Nombre", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=900206",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxEstadoSolicitudesPuntas = function()
	{
		var translate_array = ['UI_ESTADO','UI_TODOS'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: 'NombreEstado',
	            dataValueField: 'IdEstado',
	            filter: "startswith",	            
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "NombreEstado", dir: "asc" }, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290887",
							dataType:"json"
	            		} 
	            	}
	            	
	            })
			};
			return config;
		});
		return configPromise;
	};

	var getConfigComboboxEstadoCont = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
			dataTextField: "NombreEstado",
			dataValueField: "IdEstadoContabilidad",
			filter: "contains",
			autoBind: false,
			minLength: minAutocomplete,
			dataSource: new kendo.data.DataSource({
				type: "odata",
				serverFiltering: false,
				serverSorting: false, 
				sort: { field: "NombreEstado", dir: "asc" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12066",
						dataType:"json"
					}
				}
			})
		};
		return config;
	};
	obj.configuracionCombos['combobox-estado-contabilidad'] = getConfigComboboxEstadoCont();
	
	var getConfigComboboxTipoCentralizacion = function()
	{
		var config = {};
		config = {
			placeholder: $translate.instant("UI_SELECCIONE"),
			dataTextField: "Nombre",
			dataValueField: "IdTipoCentralizacion",
			filter: "contains",
			autoBind: false,
			minLength: minAutocomplete,
			dataSource: new kendo.data.DataSource({
				type: "odata",
				serverFiltering: false,
				serverSorting: false, 
				sort: { field: "Nombre", dir: "asc" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=12067",
						dataType:"json"
					}
				}
			})
		};
		return config;
	};
	obj.configuracionCombos['combobox-tipo-centralizacion'] = getConfigComboboxTipoCentralizacion();
	
	obj.ConsultarInformeLimitesCorredora = function(datos,success_fn,error_fn)
	{
		var urlConsultarInformeLimitesCorredora = "/BSCL/Plataforma/GWJAVA/PLA/call?t=60069";
		
		var r = $resource("", {},
	    {
			'ConsultarInformeLimitesCorredora' : { method: "POST", url: urlConsultarInformeLimitesCorredora}																
		});
		var post = r.ConsultarInformeLimitesCorredora(datos);
		post.$promise.then(success_fn).catch(error_fn);
	};
	obj.getConfigComboboxRangoRenta = function()
	{
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "Descripcion",
	            dataValueField: "CodigoRangoSeleccion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=701171",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxRangoNivelVenta = function()
	{
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "Descripcion",
	            dataValueField: "CodigoRangoSeleccion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=701172",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	obj.getConfigComboboxPropositoTransaccion = function()
	{
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "Descripcion",
	            dataValueField: "CodigoRangoSeleccion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false, 
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=701170",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	
	// Tipo Control Informe Control Facturacion
	obj.getValorTipoControl = function()
	{
		var translate_array = ['UI_VALOR_TIPO_CONTROL'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: '...',
	            dataTextField: "ValorTipoControl",
	            dataValueField: "IdValorTipoControl",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,  // ordena al lado del servidor
	            	sort: { field: "IdValorTipoControl", dir: "asc" }, // campo
																		// y
																		// criterio
																		// de
																		// ordenamiento
	            	transport: {
	            		read: {
	            			url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=101010160",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfigComboboxInstrumentoAsignacionMasiva = function (){
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "InstrumentoUI",
	            dataValueField: "IdInstrumentoUI",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "InstrumentoUI", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702176",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxCondicionDeLiquidacionAsignacionMasiva = function (){
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "NombreCondicionDeLiquidacion",
	            dataValueField: "IdCondicionDeLiquidacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdCondicionDeLiquidacion", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702177",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxBolsaAsignacionMasiva = function (){
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "NombreBolsa",
	            dataValueField: "IdBolsa",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "IdBolsa", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702178",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxTipoOperacionAsignacionMasiva = function (){
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "AccionTipoOperacion",
	            dataValueField: "AccionTipoOperacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	sort: { field: "AccionTipoOperacion", dir: "asc" },
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702179",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxMarcaNegociacionAsignacionMasiva = function (){
		var translate_array = ['UI_SELECCIONE'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONE,
	            dataTextField: "NombreMarcaNegociacion",
	            dataValueField: "MarcaNegociacion",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: false,
	            	serverSorting: false,
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702181",
							dataType:"json"
	            		}
	            	},
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxLibroOrdenesAsignacionMasiva = function() {
		var translate_array = ['UI_SELECCIONAR'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONAR,
	            dataTextField: "NombreLibroOrdenes",
	            dataValueField: "IdLibroOrdenes",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "NombreLibroOrdenes", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702182",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};
	
	obj.getConfigComboboxBovedaAsignacionMasiva = function()
	{
		var translate_array = ['UI_SELECCIONAR'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONAR,
	            dataTextField: "NombreBoveda",
	            dataValueField: "IdBoveda",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "NombreBoveda", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=702183",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	};

	obj.getConfigComboboxTipoArchivoFFMM = function ()
	{
		var translate_array = ['UI_SELECCIONAR'];
		configPromise = $translate(translate_array).then(function (translations) {
			var config = {};
			// configuracion del combo
			config = {
				placeholder: translations.UI_SELECCIONAR,
	            dataTextField: "Descripcion",
	            dataValueField: "IdArchivoFondoMutuo",
	            filter: "contains",
	            autoBind: false,
	            minLength: minAutocomplete,
	            dataSource: new kendo.data.DataSource({
	            	type: "odata",
	            	serverFiltering: true,
	            	serverSorting: true,  // ordena al lado del servidor
	            	sort: { field: "IdArchivoFondoMutuo", dir: "asc" }, // campo y criterio
															// de ordenamiento
	            	transport: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1694",
							dataType:"json"
	            		}
	            	}
	            })
			};
			return config;
		});
		return configPromise;
	}
	
	obj.getConfiguracion = function(elem) {
		var configPromise;
		var configuraciones = {
				"grid-demo" 									: obj.getConfigGridDemo,
				"combobox-tipo-control"                         : obj.getValorTipoControl,
				"combobox-generarInterfaz"						: obj.getConfigComboboxGenerarInterfaz,
				"combobox-generarTipoInterfaz"					: obj.getConfigComboboxGenerarTipoInterfaz,
				"combobox-estadoConfirmacion"					: obj.getConfigComboboxFiltroEstadosConfirmacionFWRF,
				"combobox-TipoOperacionForward"					: obj.getConfigComboboxFiltroTipoOperacionForward,
				"combobox-tipo-concepto"						: obj.getConfigComboboxTipoConcepto,
				"combobox-demo-territori" 						: obj.getConfigComboboxDemoTerritori,
				"combobox-demo-region"							: obj.getConfigComboboxDemoRegion,
				"combobox-condicion-liquidacion"				: obj.getConfigComboboxCondicionLiquidacion,
				"combobox-condicion-liquidacion-integracion"	: obj.getConfigComboboxCondicionLiquidacionIntegracion,				
				"combobox-instrumento"							: obj.getConfigComboboxInstrumento,
				"combobox-instrumento-simultanea"				: obj.getConfigComboboxInstrumentoSimultanea,
				"combobox-instrumento-bolsa"					: obj.getConfigComboboxInstrumentoBolsa,
				"combobox-tipo-instrumento"						: obj.getConfigComboboxTipoInstrumento,
				"combobox-tipo-instrumento-como-garantia"		: obj.getConfigComboboxTipoInstrumentoComoGarantia,
				"combobox-clasificacion-tipo-instrumento"		: obj.getConfigComboboxClasificacionTipoInstrumento,
				"combobox-clientes-complemento4porciento"		: obj.getConfigComboboxClientes4porciento,
				"combobox-nemo-complemento4porciento"			: obj.getConfigComboboxNemo4Porciento,
				"combobox-mercados-creados"                     : obj.getConfigComboboxMercadosCreados,
				"combobox-mercados-todoscreados"                : obj.getConfigComboboxMercadosTodosCreados,
				"combobox-submercados-creados"                  : obj.getConfigComboboxSubMercadosCreados,
				"combobox-clasificacion-instrumento"	   	    : obj.getConfigComboboxClasificacionInstrumento,
				"combobox-instrumentos-clasificados"            : obj.getConfigComboboxInstrumentosClasificados,
				"combobox-Nuevo-Mercado"                        : obj.getConfigComboboxNuevoMercado,
				"combobox-Tipo-Clas-Instrumento-Asignar"		: obj.getConfigComboboxTipoClasInstrumentoAsignar,
				"combobox-Clas-Instrumento-Asignar"				: obj.getConfigComboboxClasInstrumentoAsignar,
				"combobox-bolsa"								: obj.getConfigComboboxBolsa,
				"combobox-tipo-moneda"							: obj.getConfigComboboxTipoMoneda,
				"combobox-moneda-parametro"                     : obj.getConfigComboboxMoneda, //CELZORRO-272	
				"combobox-moneda-valor"                         : obj.getConfigComboboxMoneda, //CELZORRO-272 
				"combobox-indice-valor"                         : obj.getConfigComboboxIndice, //CELZORRO-272
				"combobox-indices"                              : obj.getConfigComboboxIndices_Todos,    //CELZORRO-273
				"combobox-moneda"								: obj.getConfigComboboxMoneda,				
				"combobox-moneda-puntas"						: obj.getConfigComboboxMonedaPuntas,				
				"combobox-moneda-tesoreria"						: obj.getConfigComboboxMonedaTesoreria,
				"combobox-tipo-instrumento-moneda-liquidacion"	: obj.getConfigTipoInstrumentoMonedaLiquidacion,
				"combobox-estado-moneda"						: obj.getConfigComboboxEstadoMoneda,
				"combobox-estado-liquidacion-spot"				: obj.getConfigComboboxEstadoLiquidacionSpot,
				"combobox-estado-caja-spot"						: obj.getConfigComboboxEstadoCajaSpot,
				"combobox-estado-solicitudes-puntas"			: obj.getConfigComboboxEstadoSolicitudesPuntas,
				"combobox-estado-programa"						: obj.getConfigComboboxEstadoPrograma,
				"combobox-estado-normativa"						: obj.getConfigComboboxEstadoNormativa,
				"combobox-estado-corredor-corresponsal"			: obj.getConfigComboboxEstadoCorredorCorresponsal,
				"combobox-corredor-corresponsal"				: obj.getConfigComboboxCorredorCorresponsal,
				"combobox-corredora"							: obj.getConfigComboboxCorredora,
				"combobox-corredora-instruccion"				: obj.getConfigComboboxCorredoraInstruccion,
				"combobox-persona-cuentas-bancarias"			: obj.getConfigComboboxPersonaCuentasBancarias,
				"combobox-contraparte"							: obj.getConfigComboboxContraparte,
				"combobox-operador"								: obj.getConfigComboboxOperador,
				"combobox-operador-libro"						: obj.getConfigComboboxOperadorLibro,
				"combobox-ano-proceso-cartolas"					: obj.getConfigComboboxAnoProcesoCartolas,
				"combobox-mes-proceso-cartolas"					: obj.getConfigComboboxMesProcesoCartolas,
				"combobox-opcion-proceso-cartolas"				: obj.getConfigComboboxOpcionProcesoCartolas,
				"combobox-tipo-proceso-cartolas"				: obj.getConfigComboboxTipoProcesoCartolas,
				"combobox-estado-proceso-cartolas"				: obj.getConfigComboboxEstadoProcesoCartolas,
				"combobox-tipo-movimiento"						: obj.getConfigComboboxTipoMovimiento,
				"combobox-concepto-cta-inv"						: obj.getConfigComboboxConceptoCtaInv,
				"combobox-cuenta-inversion"						: obj.getConfigComboboxCuentaInversion,
				"combobox-tipo-operacion"						: obj.getConfigComboboxTipoOperacion,
				"combobox-tipo-precio"							: obj.getConfigComboboxTipoPrecio,
				"combobox-instrumento-tipo-reajuste"			: obj.getConfigComboboxInstrumentoTipoReajuste,
				"combobox-tipo-inflacion"						: obj.getConfigComboboxTipoInflacion,
				"combobox-tipo-reajuste"						: obj.getConfigComboboxTipoReajuste,
				"combobox-tipo-libor"       				    : obj.getConfigComboboxTipoLibor,
				"combobox-tenor-inflacion"       				: obj.getConfigComboboxTenorInflacion,
				"combobox-tipo-calculo-intereses"				: obj.getConfigComboboxTipoCalculoIntereses,
				"combobox-tipo-bono-irf"						: obj.getConfigComboboxTipoBonoIRF,
				"combobox-tipo-centro-costo-relacionado"		: obj.getConfigComboboxCentroCostoRelacionado,
				"combobox-tipo-centro-costo"					: obj.getConfigComboboxCentroCosto,								
				"combobox-tipo-plaza"							: obj.getConfigComboboxTipoPlaza,
				"combobox-tipo-ifrs"							: obj.getConfigComboboxTipoIfrs,
				"combobox-tipo-centro"							: obj.getConfigComboboxTipoCentro,
				"combobox-presencia-bursatil"					: obj.getConfigComboboxPresenciaBursatil,
				"combobox-tipo-enrolamiento"					: obj.getConfigComboboxTipoEnrolamiento,
				"combobox-tipo-origen-instrumento"				: obj.getConfigComboboxOrigenInstrumento,
				"combobox-tipo-renta"							: obj.getConfigComboboxTipoRenta,
				"combobox-relacion-grupo"						: obj.getConfigComboboxRelacionadoGrupo,
				"combobox-formador-mercado"						: obj.getConfigComboboxFormadorMercado,
				"combobox-pacto-andino"							: obj.getConfigComboboxPactoAndino,
				
				"combobox-forma-custodia-orden"					: obj.getConfigComboboxFormaCustodiaOrden,
				
				"combobox-forma-custodia-mesaRF"				: obj.getConfigComboboxFormaCustodiaMesaRF,
				
				"combobox-tipo-cartera-mesa"                    : obj.getConfigComboboxFiltroTipoCarteraMesa,			
				
				"combobox-tipo-custodia-orden"					: obj.getConfigComboboxTipoCustodiaOrden,
				"combobox-porcentaje-visibilidad-orden"			: obj.getConfigComboboxPorcentajeVisibilidadOrden,
				"combobox-tipo-fecha-vigencia-orden"			: obj.getConfigComboboxTipoFechaVigenciaOrden,
				"combobox-emisor"								: obj.getConfigComboboxEmisor,

				"combobox-boveda"								: obj.getConfigComboboxBoveda,
				"combobox-vector-carga"                         : obj.getConfigTipoDocumentoVector, 
				"combobox-vector-carga"                         : obj.getConfigTipoDocumentoVector, 
				
				"combobox-conceptos-garantia"					: obj.getConfigComboboxConceptosGarantia,
				"combobox-especie-en-garantia"					: obj.getConfigComboboxEspecieEnGarantia,
				"combobox-estados-vigencia-orden"				: obj.getConfigComboboxEstadosVigenciaOrden,
				"combobox-tipo-instrumento-subyacente"			: obj.getConfigComboboxTipoInstrumentoSubyacente,
				"combobox-tipo-instrumento-subyacente-instrucciones"	: obj.getConfigComboboxTipoInstrumentoSubyacenteInstrucciones,
				"combobox-periodos-comisiones"                  : obj.getConfigComboboxPeriodosComisiones,
				"combobox-opciones-cobro-disponibles"           : obj.getConfigComboboxOpcionesCobroDisponible,
				"combobox-cobro-comision-mercados"				: obj.getConfigComboboxCobroComisionMercados,
				"combobox-tipo-genero"							: obj.getConfigComboboxTipoGenero,
				"combobox-tipo-persona"							: obj.getConfigComboboxTipoPersona,
				"combobox-clase-documento"					    : obj.getConfigComboboxClaseDocumento,
				"combobox-tipo-telefono"						: obj.getConfigComboboxTipoTelefono,
				"combobox-proposito-contacto"					: obj.getConfigComboboxPropositoMedioContacto,
				"combobox-codigo-empleado"						: obj.getConfigComboboxCodigoEmpleado,
				"combobox-empleado"								: obj.getConfigComboboxEmpleado,
				"combobox-tipo-cargo-empleado"					: obj.getConfigComboboxTipoCargoEmpleado,
				"combobox-tipo-cargo-persona"					: obj.getConfigComboboxTipoCargoPersona,
				"combobox-estado-cliente"						: obj.getConfigComboboxEstadoCliente,
				"combobox-estado-tipo-cliente"					: obj.getConfigComboboxEstadoTipoCliente,				
				"combobox-estado-usuario"						: obj.getConfigComboboxEstadoUsuario,
				"combobox-estado-rol"							: obj.getConfigComboboxEstadoRol,
				"combobox-estado-perfil"						: obj.getConfigComboboxEstadoPerfil,
				"combobox-estado-empleado"						: obj.getConfigComboboxEstadoEmpleado,
				"combobox-estado-proveedor"						: obj.getConfigComboboxEstadoProveedor,
				"combobox-area-proveedor"						: obj.getConfigComboboxAreaProveedor,
				"combobox-tipo-bloqueo-socio-negocio"			: obj.getConfigComboboxTipoBloqueoSocioNegocio,
				"combobox-tipo-operacion-custodia"				: obj.getConfigComboboxTipoOperacionCustodia,
				"combobox-instrumentos-custodia"				: obj.getConfigComboboxInstrumentosCustodia,
				
				"combobox-concepto"								: obj.getConfigComboboxConcepto,
				"combobox-concepto-custodia"					: obj.getConfigComboboxConceptoCustodia,
				"combobox-tipo-emisor"							: obj.getConfigComboboxTipoEmisor,
				"combobox-estado-custodia"						: obj.getConfigComboboxEstadoCustodia,
				"combobox-tipo-comprobante"						: obj.getConfigComboboxTipoComprobante,
				"combobox-tipo-referencia"						: obj.getConfigComboboxTipoReferencia,
				"combobox-tipo-contrato-cobro-periodico"		: obj.getConfigComboboxTipoContratoCobroPeriodico,
				"combobox-formato-contrato-cobro-periodico"		: obj.getConfigComboboxFormatoContratoCobroPeriodico,
				"combobox-comercial-tipo-contrato"				: obj.getConfigComboboxComercialTipoContrato,
				"combobox-comercial-formato-cobro"				: obj.getConfigComboboxComercialFormatoCobro,
				"combobox-formato-archivo"                      : obj.getConfigComboboxFormatoDeArchivo,
				
				"combobox-portafolio-contrato-cobro-periodico"	: obj.getConfigComboboxPortafolioContratoCobroPeriodico,
				"combobox-estado-comprobante"					: obj.getConfigComboboxEstadoComprobante,
				"combobox-sucursal"								: obj.getConfigComboboxSucursal,
				"combobox-estado-cuenta-inversion"				: obj.getConfigComboboxEstadoCuentaInversion,
				"combobox-estado-movimiento-en-transito"		: obj.getConfigComboboxEstadoMovimientoEnTransito,
				"combobox-tipo-transaccion"						: obj.getConfigComboboxTipoTransaccion,
				"combobox-concepto-facturacion"					: obj.getConfigComboboxConceptoFacturacion,
				"combobox-estado-facturacion"					: obj.getConfigComboboxEstadoFacturacion,
				"combobox-tipo-socio-negocio"					: obj.getConfigComboboxTipoSocioNegocio,
				"combobox-tipo-segmento"						: obj.getConfigComboboxTipoSegmento,
				"combobox-tipo-relacion"						: obj.getConfigComboboxTipoRelacion,
				"combobox-entidad-destino-relacion"				: obj.getConfigComboboxEntidadDestinoRelacion,
				"combobox-persona-tipo-relacion"				: obj.getConfigComboboxPersonaTipoRelacion,
				"combobox-pais"									: obj.getConfigComboboxPais,
				"combobox-calendario"							: obj.getConfigComboboxCalendario,
				"combobox-giro"									: obj.getConfigComboboxGiro,
				"combobox-estado-civil"							: obj.getConfigComboboxEstadoCivil,
				"combobox-regimen-conyugal"						: obj.getConfigComboboxRegimenConyugal,
				"combobox-profesion"							: obj.getConfigComboboxProfesion,
				"combobox-estado-cuenta-bancaria"				: obj.getConfigComboboxEstadoCuentaBancaria,
				"combobox-estado-tarjeta-bancaria"				: obj.getConfigComboboxEstadoTarjetaBancaria,
				"combobox-estado-nombre-fantasia"				: obj.getConfigComboboxEstadoNombreFantasia,
				"combobox-estado-documento-identidad"			: obj.getConfigComboboxEstadoDocumentoIdentidad,
				"combobox-estado-direccion-fisica"				: obj.getConfigComboboxEstadoDireccionFisica,
				"combobox-estado-direccion-electronica"			: obj.getConfigComboboxEstadoDireccionElectronica,
				"combobox-estado-telefono"						: obj.getConfigComboboxEstadoTelefono,
				"combobox-tipo-documento-identidad"				: obj.getConfigComboboxTipoDocumentoIdentidad,
				"combobox-ciudad"								: obj.getConfigComboboxCiudad,
				"combobox-division-pais"						: obj.getConfigDivisionPais,
				"combobox-origen-garantia"						: obj.getConfigComboboxOrigenGarantia,
				"combobox-entidad-clasificadora-riesgo"			: obj.getConfigComboboxEntidadClasificadoraRiesgo,
				"combobox-clasificacion-riesgo"					: obj.getConfigComboboxClasificacionRiesgo,
				"combobox-clasificacion-riesgo-tipo-instrumento": obj.getConfigComboboxClasificacionRiesgoTipoInstrumento,
				"combobox-estado-movimiento-factura"			: obj.getConfigComboboxEstadoMovimientoFactura,
				"combobox-perfil-comercial"						: obj.getConfigComboboxPerfilComercial,
				"combobox-apoderado-cliente"                    : obj.getConfigComboboxApoderadoCliente,
				"combobox-apoderado-corredora"                  : obj.getConfigComboboxApoderadoCorredora,
				"combobox-area-cliente"							: obj.getConfigComboboxAreaCliente,
				"combobox-perfil-plan-comercial"				: obj.getConfigComboboxPerfilPlanComercial,
				"combobox-perfil-productos"						: obj.getConfigComboboxPerfilProductos,
				"combobox-perfil-cobros"						: obj.getConfigComboboxPerfilCobros,
				"combobox-modelo-facturacion"					: obj.getConfigComboboxModeloFacturacion,
				"combobox-perfil-condiciones-operativas"		: obj.getConfigComboboxPerfilCondicionesOperativas,
				"combobox-plantilla-empleado"					: obj.getConfigComboboxPlantillaEmpleado,
				"combobox-plantilla-limites"					: obj.getConfigComboboxPlantillaLimites,
				"combobox-estado-perfil-comercial"				: obj.getConfigComboboxEstadoPerfilComercial,
				"combobox-grupo-activos"						: obj.getConfigComboboxGrupoActivos,
				"combobox-grupos-clientes"						: obj.getConfigComboboxGrupoClientes,
				"combobox-tipo-grupo-activos"					: obj.getConfigComboboxTipoGrupoActivos,
				"combobox-estado-grupo-activos"					: obj.getConfigComboboxEstadoGrupoActivos,
				"combobox-plantilla-condicion-operativa"		: obj.getConfigComboboxPlantillaCondicionesOperativas,
				"combobox-tipo-comportamiento-condicion"		: obj.getConfigComboboxTipoComportamientoCondicion,
				"combobox-categoria-inversionista"				: obj.getConfigComboboxCategoriaInversionista,
				"combobox-plan-categoria"						: obj.getConfigComboboxPlanCategoria,
				"combobox-categoria-perfil-ffmm"				: obj.getConfigComboboxTipoPerfilFondoMutuo,
				"combobox-tipo-canal-ingreso"					: obj.getConfigComboboxTipoCanalIngreso,
				"combobox-estado-limites"						: obj.getConfigComboboxEstadoLimites,
				"combobox-grupo-emisores"						: obj.getConfigComboboxGrupoEmisores,
				"combobox-tipo-limite"							: obj.getConfigComboboxTipoLimite,
				"combobox-tipo-limite-cliente"					: obj.getConfigComboboxTipoLimiteCliente,
				"combobox-estado-evento"						: obj.getConfigComboboxEstadoEvento,
				"combobox-tipo-evento"							: obj.getConfigComboboxTipoEvento,
				"combobox-tipo-evento-capital"					: obj.getConfigComboboxTipoEventoCapital,
				//GCBSXNEC-1 jmedina_ext
        "combobox-secuencia-relacionada"				: obj.getConfigComboboxSecuenciaRelacionada, //GCBSXNEC-1 jmedina_ext
				"combobox-tipo-cartera"							: obj.getConfigComboboxTipoCartera,
				"combobox-tipo-relacion-instrumento"			: obj.getConfigComboboxTipoRelacionInstrumento,
				"combobox-concepto-tesoreria"					: obj.getConfigComboboxConceptoTesoreria,
				"combobox-estado-tesoreria"						: obj.getConfigComboboxEstadoTesoreria,
				"combobox-caja"									: obj.getConfigComboboxCaja,
				"combobox-caja-moneda"							: obj.getConfigComboboxCajaMoneda,
				"combobox-caja-moneda-usuario"					: obj.getConfigComboboxCajaMonedaUsuario,
				"combobox-medio-pago"							: obj.getConfigComboboxMedioPago,
				"combobox-medio-pago-tesoreria"					: obj.getConfigComboboxMedioPagoTesoreria,
				"combobox-medio-pago-cuenta-inversion"			: obj.getConfigComboboxMedioPagoCuentaInversion,
				"combobox-forma-pago-mesa"						: obj.getConfigComboboxFormaDePagoMesa,
				"combobox-forma-pago-pref-portafolio"			: obj.getConfigComboboxFormaDePagoPreferenciaPortafolio,
				"combobox-forma-pago-mesaportafolio"			: obj.getConfigComboboxFormaDePagoMesaPortafolio,
				"combobox-forma-pago-cliente"					: obj.getFormaDePagoCliente,
				"combobox-forma-pago-corredora"					: obj.getFormaDePagoCorredora,
				"combobox-forma-pago-cliente-spot"				: obj.getFormaDePagoClienteSpot,
				"combobox-forma-pago-corredora-spot"			: obj.getFormaDePagoCorredoraSpot,

				"combobox-tipo-banco"							: obj.getConfigComboboxTipoBanco,				
				"combobox-estado-socio-negocio"					: obj.getConfigComboboxEstadoSocionegocio,
				"combobox-banco"								: obj.getConfigComboboxBanco,
				"combobox-plaza"								: obj.getConfigComboboxPlaza,
				
				"combobox-tipo-cuenta-banco"					: obj.getConfigComboboxTipoCuentaBanco,
				"combobox-tipo-cuenta"							: obj.getConfigComboboxTipoCuenta,
				"combobox-tipo-tarjeta-banco"					: obj.getConfigComboboxTipoTarjetaBanco,
				"combobox-moneda-cuenta-banco"					: obj.getConfigComboboxMonedaCuentaBanco,
				"combobox-cuentas-bancarias-corredora"			: obj.getConfigComboboxCuentasBancariasCorredora,
				"combobox-numero-cuentas-bancarias-corredora"	: obj.getConfigComboboxNumeroCuentasBancariasCorredora,
				"combobox-banco-cuentas-bancarias-corredora"	: obj.getConfigComboboxBancoCuentasBancariasCorredora,
				"combobox-banco-cuentas-bancarias-corredora-cuentas"	: obj.getConfigComboboxBancoCuentasBancariasCorredoraCuentas,
				"combobox-tarjeta-bancaria"						: obj.getConfigComboboxTarjetaBancaria,
				"combobox-libro-orden"							: obj.getConfigComboboxLibroOrdenes,
				"combobox-usuario"								: obj.getConfigComboboxUsuario,
				"combobox-rol"									: obj.getConfigComboboxRol,
				"combobox-tipo-prestamo"						: obj.getConfigComboboxTipoPrestamo,
				"combobox-tipo-socio-negocio-custodia"			: obj.getConfigComboboxTipoSocioDeNegocioCustodia,
				"combobox-socio-negocio"						: obj.getConfigComboboxSocioDeNegocio,
				"combobox-cliente-portafolio"					: obj.getConfigComboboxClientePortafolio,
				"combobox-cliente-portafolio-sf"				: obj.getConfigComboboxClientePortafolioSinFiltro,
				"combobox-cliente-portafolio-spot"				: obj.getConfigComboboxClientePortafolioSpot,
				"combobox-cliente-portafolio-contrato"			: obj.getConfigComboboxClientePortafolioContrato,
				"combobox-portafolio"							: obj.getConfigComboboxPortafolio,
				"combobox-portafolio-traspaso"					: obj.getConfigComboboxPortafolioTraspaso,
				"combobox-proposito-portafolio"					: obj.getConfigComboboxPropositoPortafolio,
				"combobox-tipo-procesos"						: obj.getConfigComboboxTipoProcesos,
				"combobox-modalidad-pago"						: obj.getConfigComboboxModalidadDePago,
				"combobox-tipo-log"								: obj.getConfigComboboxTipoLog,
				"combobox-tipo-registro"						: obj.getConfigComboboxTipoRegistro,
				"combobox-persona-direccion-fisica"				: obj.getConfigComboboxPersonaDireccionFisica,
				"combobox-direccion-contrato"					: obj.getConfigComboboxaDireccionContrato,
				"combobox-persona-direccion-electronica"		: obj.getConfigComboboxPersonaDireccionElectronica,
				"combobox-persona-grupo-y-direccion"			: obj.getConfigComboboxPersonaGrupoYDireccionElectronica,
				"combobox-enrolamiento-direccion-electronica"   : obj.getConfigComboboxEnrolamientoDireccionElectronica,
				"combobox-persona-telefono"						: obj.getConfigComboboxPersonaTelefono,
				"combobox-persona-tarjeta-bancaria"				: obj.getConfigComboboxPersonaTarjetaBancaria,
				"combobox-persona-cuenta-bancaria"				: obj.getConfigComboboxPersonaCuentaBancaria,
				"combobox-enrolamiento-tarjeta-bancaria"		: obj.getConfigComboboxEnrolamientoTarjetaBancaria,
				"combobox-enrolamiento-cuenta-bancaria"			: obj.getConfigComboboxEnrolamientoCuentaBancaria,
				"combobox-detalle-tipo-atributo"				: obj.getConfigComboboxDetalleTipoAtributo,
				"combobox-detalle-tipo-atributo-Entidad"		: obj.getConfigComboboxDetalleTipoAtributoEntidad,
				"combobox-detalle-tipo-atributo-entidad-json"	: obj.getConfigComboboxDetalleTipoAtributoEntidadJson,
				"combobox-producto-empresa"						: obj.getConfigComboboxProductoEmpresa,
				"combobox-tipo-carga-masiva"					: obj.getConfigComboboxTipoCargaMasiva,
				"combobox-modo-carga-masiva"					: obj.getConfigComboboxModoCargaMasiva,
				"combobox-tipo-dato-atributo"					: obj.getConfigComboboxTipoDatoAtributo,
				"combobox-tipo-indicador"						: obj.getConfigComboboxTipoIndicador,
				"combobox-grupo-indicador"						: obj.getConfigComboboxGrupoIndicador,
				"combobox-estado-tipo-atributo"					: obj.getConfigComboboxEstadoTipoAtributo,
				"combobox-nombre-fantasia"						: obj.getConfigComboboxNombreFantasia,
				"combobox-configuracion-chequeras"				: obj.getConfigComboboxConfiguracionChequeras,
				"combobox-chequeras-series"						: obj.getConfigComboboxChequerasSeries,
				"combobox-estado-grupo-portafolio"				: obj.getConfigComboboxEstadoGrupo,
				"combobox-estado-grupo-clientes-activos"		: obj.getConfigComboboxEstadoGrupoClientesActivos, 	// CELZORRO-464
				"combobox-estado-grupo-cliente"					: obj.getConfigComboboxEstadoGrupoClientes,			// CELZORRO-461
				"combobox-forma-despacho"						: obj.getConfigComboboxFormaDespacho,
				"combobox-tiempo-despacho" 						: obj.getConfigComboboxTiempoDespacho,
				"combobox-notificacion-enviada"					: obj.getConfigComboboxEnviado,
				"combobox-tipo-evento-notificacion"				: obj.getConfigComboboxTipoEventoNotificacion,
				"combobox-tipo-plantilla-notificacion"			: obj.getConfigComboboxPlantillaNotificacion,
				"combobox-estados-libro-ordenes"				: obj.getConfigComboboxEstadosLibroOrdenes,
				"combobox-estados-asignacion"					: obj.getConfigComboboxEstadosAsignacion,
				"combobox-estado-transaccionbolsa"				: obj.getConfigComboboxEstadoTransaccionBolsa,
				"combobox-tipo-plantillas"						: obj.getConfigComboboxTipoPlantillas,
				"combobox-convencion-dias"						: obj.getConfigComboboxConvencionDias,
				"combobox-tipo-documentos"						: obj.getConfigComboboxTipoDocumentos,
				"combobox-direccion-evento"						: obj.getConfigComboboxDireccionEvento,
				"combobox-tipo-asignacion"						: obj.getConfigComboboxTipoAsignacion,
				"combobox-tipo-libro-orden"						: obj.getConfigComboboxTipoLibroOrden,
				"combobox-configuracion-evento"					: obj.getConfigComboboxConfiguracionEvento,
				"combobox-modulos-cierre"						: obj.getConfigComboboxModulosCierre,
				"combobox-perfil"								: obj.getConfigComboboxPerfil,
				"combobox-funcion-proceso"						: obj.getConfigComboboxModuloFuncionProceso,
				"combobox-modo-facturacion"						: obj.getConfigComboboxModoFacturacion,
				"combobox-tipo-destinatario"					: obj.getConfigComboboxTipoDestinatario,
				"combobox-preferencia-pago-tipo-operacion"		: obj.getConfigComboboxPreferenciaPagoTipoOperacion,
				"combobox-preferencia-pago-medio-pago"			: obj.getConfigComboboxPreferenciaPagoMedioPago,
				"combobox-preferencia-pago-moneda"				: obj.getConfigComboboxPreferenciaPagoMoneda,
				"combobox-estado-portafolio"					: obj.getConfigComboboxEstadoPortafolio,
				"combobox-relacionado"							: obj.getConfigComboboxRelacionado,
				"combobox-atributo-anotacion"					: obj.getConfigComboboxAtributoAnotacion,
				"combobox-atributo-anotacion-cliente"			: obj.getConfigComboboxAtributoAnotacionCliente,
				"combobox-tipo-autorizado-portafolio"			: obj.getConfigComboboxTipoAutorizadoPortafolio,
				"combobox-tipo-perfil-portafolio"				: obj.getConfigComboboxTipoPerfilPortafolio,
				"combobox-autorizado-dar-ordenes"				: obj.getConfigComboboxAutorizadoDarOrdenes,
				"combobox-condicion-operativa"					: obj.getConfigComboboxCondicionOperativa,
				"combobox-estado-condicion-operativa"			: obj.getConfigComboboxEstadoCondicionOperativa,
				"combobox-requisito-operativo"					: obj.getConfigComboboxRequisitoOperativo,
				"combobox-tipo-comportamiento"					: obj.getConfigComboboxTipoComportamiento,
				"combobox-tipo-etapa"							: obj.getConfigComboboxTipoEtapa,
				"combobox-tipo-operacion-usuario"				: obj.getConfigComboboxTipoOperacionUsuario,
				"combobox-valor-cobro"							: obj.getConfigComboboxValorCobro,
				"combobox-cobro"								: obj.getConfigComboboxCobro,
				"combobox-cobro-clasificacion"					: obj.getConfigComboboxCobroClasificacion,
				"combobox-tipo-normativa"						: obj.getConfigComboboxTipoNormativa,
				"combobox-tipo-normativa-UI"					: obj.getConfigComboboxTipoNormativaUI,
				"combobox-tipo-documento-normativa"				: obj.getConfigComboboxTipoDocumentoNormativa,
				"combobox-tipo-documento-normativa-UI"			: obj.getConfigComboboxTipoDocumentoNormativaUI,
				"combobox-clasificacion-fatca"					: obj.getConfigComboboxClasificacionFatca,
				"combobox-clasificacion-empresa-fatca"			: obj.getConfigComboboxClasificacionEmpresaFatca,
				"combobox-tipo-operacion-origen-fondos"			: obj.getConfigComboboxTipoOperacionOrigenFondos,
				"combobox-cartera-clasificacion-ifrs"			: obj.getConfigComboboxCarteraClasificacionIfrs,
				"combobox-cartera-corredor"						: obj.getConfigComboboxCarteraCorredor,
				"combobox-preferencia-pago-portafolio"			: obj.getConfigComboboxPreferenciaPagoPortafolio,
				"combobox-mesas"								: obj.getConfigComboboxMesas,
				"combobox-cliente-mesas"						: obj.getConfigComboboxClienteMesas,
				"combobox-tipo-cartera-consolidada"				: obj.getConfigComboboxTipoCarteraConsolidada,
				"combobox-metodo-posicion-mrf"					: obj.getConfigComboboxMetodoPosicionMRF,
				"combobox-tipo-fondo"							: obj.getConfigComboboxTipoFondo,
				"combobox-proposito-ffm"						: obj.getConfigComboboxPropositoFFM,
				"combobox-regimen-ffm"							: obj.getConfigComboboxRegimenFFM,
				"combobox-canasta-mesa"							: obj.getConfigComboboxCanastaForward,
				"combobox-parametro-mesa"						: obj.getConfigComboboxParametroMesa,
				"combobox-modalidad-liquidacion"				: obj.getConfigComboboxModalidadLiquidacion,
				"combobox-operacion-mesa"						: obj.getConfigComboboxOperacionMesa,
				"combobox-estado-mesa"							: obj.getConfigComboboxEstadoMesa,
				"combobox-estado-operacion-mesa"				: obj.getConfigComboboxEstadoOperacionMesa,
				"combobox-estado-datatec"						: obj.getConfigComboboxEstadoDatatec,
				"combobox-tipoinstrumento-mesa"					: obj.getConfigComboboxTipoInstrumentoMesa,
				"combobox-instrumento-mesa"						: obj.getConfigComboboxInstrumentoMesa,
				"combobox-instrumento-IF"						: obj.getConfigComboboxInstrumentoIF,
				"combobox-instrumento-mesa-ui"					: obj.getConfigComboboxInstrumentoMesaUI,				
				"combobox-moneda-principal"						: obj.getConfigComboboxMonedaPrincipal,
				"combobox-moneda-secundaria"					: obj.getConfigComboboxMonedaSecundaria,
				"combobox-planilla-banco-central"				: obj.getConfigComboboxPlanillaBancoCentral,
				"combobox-mediodepago-mesa"						: obj.getConfigComboboxMedioDePagoMesa,
				"combobox-tipo-comprobante-posicion-mrf"		: obj.getConfigComboboxTipoComprobantePosicionMRF,
				"combobox-tipo-estado-posicion-mrf"				: obj.getConfigComboboxTipoEstadoPosicionMRF,
				"combobox-tipo-operacion-posicion"				: obj.getConfigComboboxTipoOperacionPosicion,
				"combobox-evento-posicion"						: obj.getConfigComboboxEventoPosicion,
				"combobox-si-no"								: obj.getConfigComboboxSiNo,
				"combobox-tipo-operacion-ffmm"					: obj.getConfigComboboxTipoOperacionFFMM,
				"combobox-tipo-motivo-retiro"					: obj.getConfigComboboxMotivoRetiro,				
				"combobox-tipo-movimiento-traspaso-ffmm"		: obj.getConfigComboboxTipoMovimientoTraspasoFFMM,
				"combobox-motivo-traspaso-ffmm"					: obj.getConfigComboboxMotivoTraspasoFFMM,
				"combobox-motivo-anulacion_traspaso-ffmm"		: obj.getConfigComboboxMotivoAnulacionTraspasoFFMM,
				"combobox-tipo-traspaso-ffmm"					: obj.getConfigComboboxTipoTraspasoFFMM,
				"combobox-estado-ffmm"							: obj.getConfigComboboxEstadoFFMM,
				"combobox-estado-omnibus-detalle"				: obj.getConfigComboboxEstadoOmnibusDetalle,
				"combobox-estado-liquidacion-ffmm"				: obj.getConfigComboboxEstadoLiquidacionFFMM,
				"combobox-tipo-rescate-ffmm"					: obj.getConfigComboboxTipoRescate,
				"combobox-estado-instrumento-ffmm"				: obj.getConfigComboboxEstadoInstrumentoFFMM,
				"combobox-operacion-ffmm"						: obj.getConfigComboboxOperacionFondoMutuo,
				"combobox-estado-instrumento"					: obj.getConfigComboboxEstadoInstrumento,
				"combobox-tipo-entidad"							: obj.getConfigComboboxTipoEntidad,
				"combobox-estado-comision-ffmm"					: obj.getConfigComboboxEstadoComisionFFMM,
				"combobox-estado-certificacion-ffmm" 			: obj.getConfigComboboxEstadoCertificacionFFMM,
				"combobox-familia-ffmm"							: obj.getConfigComboboxFamiliaFFMM,
				"combobox-tipo-perfil-ffmm"						: obj.getConfigComboboxTipoPerfilFFMM,
				"combobox-estado-interfaz-agf-ffmm"				: obj.getConfigComboboxEstadoSolicitudAgfFFMM,
				"combobox-tipo-autorizacion"					: obj.getConfigComboboxTipoAutorizacion,
				"combobox-perfil-fondo-mutuo"					: obj.getConfigComboboxPerfilFondoMutuo,
				"combobox-instrumento-ffmm"						: obj.getConfigComboboxInstrumentoFondoMutuo,
				"combobox-serie-ffmm"							: obj.getConfigComboboxSerieFondoMutuo,
				"combobox-serie-custodia-ffmm"					: obj.getConfigComboboxSerieFondoMutuoCustodia,
				"combobox-instrumento-custodia-ffmm"			: obj.getConfigComboboxInstrumentoFondoMutuoCustodia,
				"combobox-moneda-cuenta-inversion-ffmm"			: obj.getConfigComboboxMonedaCuentaInversionFFMM,
				"combobox-tipo-operacion-carga-ffmm"			: obj.getConfigComboboxTipoOperacionCargaFFMM,
				"combobox-tipo-registro-carga-ffmm"				: obj.getConfigComboboxTipoRegistroCargaFondoMutuo,
				"combobox-si-no-noaplica"						: obj.getConfigComboboxSiNoNoAplica,
				"combobox-bloqueado-desbloqueado"				: obj.getConfigComboboxBloqueadoDesbloqueado,
				"combobox-abierto-cerrado"						: obj.getConfigComboboxAbiertoCerrado,
				"combobox-estado-deposito"						: obj.getConfigComboboxEstadoDeposito,
				"combobox-estado-archivo-contrato"				: obj.getConfigComboboxEstadoArchivoContrato,
				"combobox-estado-confirmacion"					: obj.getConfigComboboxEstadoConfirmacionNotificacion,
				"combobox-tiempo-despacho-principal"			: obj.getConfigComboboxTiempoDespachoPrincipal,
				"combobox-ejecutivo"							: obj.getConfigComboboxEjecutivo,
				"combobox-agente"					     		: obj.getConfigComboboxAgente,
				"combobox-supervisor"							: obj.getConfigComboboxSupervisor,
				"combobox-asuntos"								: obj.getConfigComboboxAsuntos,
				"combobox-asuntos-pendientes"					: obj.getConfigComboboxAsuntosPendientes,
				"combobox-tipo-registro-paridad"				: obj.getConfigComboboxTipoRegistroParidad,
				"combobox-relacion-normativa"					: obj.getConfigComboboxRelacionNormativa,
				"combobox-accesos"								: obj.getConfigComboboxAccesos,
				"combobox-tipo-registro-sistema"				: obj.getConfigComboboxTipoRegistrosSistema,
				"combobox-estado-confirmacion-orden"			: obj.getConfigComboboxEstadoConfirmacion,
				"combobox-estado-instruccion"					: obj.getConfigComboboxEstadoInstruccion,
				"combobox-tipo-producto-mesa-modulo"			: obj.getConfigComboboxTipoProductoMesaModulo,
				"combobox-tipo-item"							: obj.getConfigComboboxTipoItem,
				"combobox-tipo-autorizacion-etapa"				: obj.getConfigComboboxTipoAutorizacionEtapa,
				"combobox-certificados-cliente"					: obj.getConfigCertificadosCliente,
				"combobox-estado-mesas"							: obj.getConfigComboboxEstadoMesas,
				"combobox-estado-familia-ffmm"					: obj.getConfigComboboxEstadoFamiliaFFMM,
				"combobox-fuente"								: obj.getConfigComboboxFuente,
				"combobox-tipo-carga-masiva-tesoreria"			: obj.getConfigComboboxTipoCargaMasivaTesoreria,
				"combobox-tipo-carga-masiva-custodia"			: obj.getConfigComboboxTipoCargaMasivaCustodia,
				"combobox-tipo-carga-operacion-spot"			: obj.getConfigComboboxTipoCargaMasivaSpot,
				"combobox-tipo-carga-masiva-facturacion"		: obj.getConfigComboboxTipoCargaMasivaFacturacion,
				"combobox-tipo-carga-masiva-plataforma"			: obj.getConfigComboboxTipoCargaMasivaPlataforma,
				"combobox-transparencia-fiscal"					: obj.getConfigComboboxTransparenciaFiscal,
				"combobox-tipo-liquidacion-comision-factura"	: obj.getConfigComboboxTipoLiquidacionComisionFactura,
				"combobox-archivos-contratos"                   : obj.getConfigComboboxArchivosContratos,
				"combobox-firmantes-cliente"                    : obj.getConfigComboboxFirmantesCliente,
				"combobox-tipo-carga-masiva-ordenes"			: obj.getConfigComboboxTipoCargaMasivaOrdenes,
				"combobox-tipo-carga-masiva-socio-negocio"		: obj.getConfigComboboxTipoCargaMasivaSocioNegocio,
				"combobox-moneda-tasa"                     		: obj.getConfigComboboxMonedaTasa,
				"combobox-Estado-Carga-Masiva"					: obj.getConfigComboboxEstadoCargaMasiva,
				"combobox-rango-renta"                          : obj.getConfigComboboxRangoRenta,
				"combobox-rango-nivel-venta"                    : obj.getConfigComboboxRangoNivelVenta,
				"combobox-proposito-transaccion"                : obj.getConfigComboboxPropositoTransaccion,
				
// CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y
// Cargos
				"combobox-tipo-carga-masiva-cuentainversion"	: obj.getConfigComboboxTipoCargaMasivaCtaInversion,
// CELZORRO-264 - MBI Manejo de Activos Internacionales - Carga masiva Abonos y
// Cargos
				"combobox-tipo-tasa"							: obj.getConfigComboboxTipoTasa,
				"combobox-estado-tasa"							: obj.getConfigComboboxEstadoTasa,
				"combobox-instrumento-custodia-tasa"			: obj.getConfigComboboxInstrumentoCustodiaTasa,
				"combobox-configuracion-tasas"					: obj.getConfigComboboxConfiguracionTasas,
				"combobox-tipo-criterio-tasa"					: obj.getConfigComboboxTipoCriterioTasa,
				"combobox-uso-criterio"							: obj.getConfigComboboxUsoCriterio,
				"combobox-tipo-precio-modulo-tasas"				: obj.getConfigComboboxTipoPrecioModuloTasas,
				"combobox-canal-distribucion"					: obj.getConfigComboboxCanalDistribucion,
				"combobox-grupo-camara" 						: obj.getConfigComboboxGrupoCamara,
				"combobox-cuentas-contables"					: obj.getConfigCuentasContables,				
				"combobox-entidad-destino-garantia"				: obj.getConfigEntidadDestinoGarantia,
				"combobox-entidad-garantia"						: obj.getConfigEntidadGarantia,
				"combobox-cuentas-contables-nombre"				: obj.getConfigCuentasContablesNombre,
				"combobox-tipo-centralizaciones"				: obj.getConfigTipoCentralizacion,
				"combobox-tipo-junta-accionista"				: obj.getConfigTipoJuntaAccionistas,
				"combobox-estado-junta-accionista"				: obj.getConfigEstadoJuntaAccionistas,
				"combobox-tipo-instruccion-junta-accionista"	: obj.getConfigTipoInstruccionJuntaAccionista,
				"combobox-tipo-dia-feriado"						: obj.getConfigTipoDiaFeriado,
				"combobox-rango-patrimonio"						: obj.getConfigRangoPatrimonio,
				"combobox-operaciones-portafolio-cliente"		: obj.getConfigOperacionesPortafolioCliente,
				"combobox-tipo-carga-conf-con"					: obj.getConfigTipoCargaConfiguracionCon,
				"combobox-parametro-financiero"					: obj.getConfigParametroFinanciero,
				"combobox-tipo-indice"							: obj.getConfigTipoIndice,
				"combobox-frecuencia-tipo-cambio"				: obj.getConfigFrecuenciaTipoCambio,
				"combobox-uso-parametro-financiero"				: obj.getConfigUsoParidadYTipoCambio,
				"combobox-estado-pla"							: obj.getConfigComboboxEstadoPla,
				
				/* INICIO - ILS */
				"combobox-tipo-operacion-ctb"					: obj.getConfigConsultaTipoOperacionCtb,
				"combobox-condicion-ctb"						: obj.getConfigConsultaCondicionCtb,
				"combobox-tipo-saldo-dh"						: obj.getConfigConsultaTipoSaldoDH,
				"combobox-item-ils"								: obj.getConfigConsultaItem,
				"combobox-concepto-ils"  						: obj.getConfigConsultaConcepto,
				"combobox-posicion-fraccion-ils"  			    : obj.getConfigConsultaPosicionFraccion,
				"combobox-informe-tipo-ils"  			        : obj.getConfigConsultaInformeTipo,
				"combobox-empresas-ils"  			            : obj.getConfigConsultaEmpresas,
				"combobox-indice-ils"  			            	: obj.getConfigConsultaIndices,
                                                                				
				"combobox-parmonedas-base"  			         : obj.getConfigConsultaParMonedasBase,
				
				/* FIN - ILS */
				"combobox-fondo-ffmm"							: obj.getConfigFondoMutuo,
				"combobox-regimen-serie-fondo"					: obj.getConfigRegimenSerieFondo,
				"combobox-serie-fondo-regimen"					: obj.getConfigSerieFondoRegimen,
				
				
				/* MERF - COBERTURA PATRIMONIAL */
				"combobox-cobertura"							:obj.getValorCoberturaPatrimonial,
				"combobox-dias-cobertura"						:obj.getValorDiasCobertura ,
				"combobox-opcion-cobertura"						:obj.getValorOpcionCobertura,
				/* TRI - ESQUEMA */
				"combobox-esquema-declaracion"					:obj.getConfigEsquemaDeclaracion,
				"combobox-version-declaracion"					:obj.getConfigVersionEsquemaDeclaracion,
				/* FFMM Regimen */
				"combobox-estado-regimenes-ffmm"				:obj.getConfigEstadoRegimenFFMM,
				
				/* APV */
				"combobox-folio-contratos-saapv"				:obj.getConfigComboboxFolioContratosSAAPV,
				"combobox-folio-recaudacion"					:obj.getConfigComboboxFolioRecaudacion,
				"combobox-tipo-trabajador"						:obj.getConfigComboboxTipoTrabajador,
				"combobox-nombre-plan-ahorro"					:obj.getConfigComboboxNombrePlanAhorro,
				"combobox-tipo-pago-empleador"					:obj.getConfigComboboxTipoPagoDetalleEmpleadorAPV,
				"combobox-tipo-solicitud"						:obj.getConfigComboboxTipoSolicitud,
				"combobox-estado-contrato-apv"					:obj.getConfigComboboxEstadoContratoAPV,
				"combobox-estado-apv"							:obj.getConfigComboboxEstadoAPV,
				"combobox-pensionado"							:obj.getConfigComboboxPensionado,
				"combobox-trabajador-saapv"						:obj.getConfigComboboxTrabajadorSAAPV,
				"combobox-origen"								:obj.getConfigComboboxOrigen,
				"combobox-fondo-origen"							:obj.getConfigComboboxFondoOrigen,
				"combobox-tipo-pago-saapv"						:obj.getConfigComboboxTipoPagoSAAPV,
				"combobox-regimen-tributario"					:obj.getConfigComboboxRegimenTributario,
				"combobox-forma-pago-directa"					:obj.getConfigFormaPagoDirecta,
				"combobox-forma-pago-indirecta"					:obj.getConfigFormaPagoIndirecta,
				"combobox-tipo-ahorro"							:obj.getConfigTipoAhorro,
				"combobox-tipo-traspaso"						:obj.getConfigTipoTraspaso,
				"combobox-sucursal-bancaria"					:obj.getConfigSucursalBancaria,
				"combobox-fondos-industria"						:obj.getConfigComboboxEntidadRecaudadoraDestino,
				"combobox-entidad-recaudadora"					:obj.getConfigComboboxEntidadRecaudadora,
				"combobox-tipocarga-apv"						:obj.getConfigComboboxTipoCargaArchivo,
				"combobox-empleador"							:obj.getConfigComboboxEmpleador,
				"combobox-clientes"								:obj.getConfigComboboxClientes,
				"combobox-contrato-persona"						:obj.getConfigComboboxContratoPortafolio,
				"combobox-producto-agrupador"					:obj.getConfigComboboxProductoAgrupador,
				"combobox-regimen-apv"							:obj.getConfigComboboxRegimenAPV,
				"combobox-banco-por-pais"						:obj.getConfigComboboxBancoPorPais,
				"combobox-origen-apv"							:obj.getConfigComboboxOrigenAPV,
				"combobox-indicador-archivo-agf"				:obj.getConfigComboboxIndicadoresArchivoAGF,
				"combobox-codigo-ifrs"							:obj.getConfigComboboxCodigoIFRS,
				"combobox-certificados-prestamos"				:obj.getConfigComboboxCertificadosPrestamos,
				"combobox-contrato-saapv"						:obj.getConfigComboboxContratoSAAPV,
				"combobox-evento-tipo-dato"						:obj.getConfigComboboxEventoTipoDato,
				"combobox-valor-evento-tipo-dato"				:obj.getConfigComboboxEventoValorTipoDato,
				"combobox-configuracion-metadata-centralizacion":obj.getConfigComboboxConfiguracionMetadata,
				"combobox-rango-dias-traspaso"					:obj.getConfigComboboxRangoDiasTraspaso,
				"combobox-dias-descuento"						:obj.getConfigComboboxDiaDescuento,
				"combobox-origen-traspaso"						:obj.getConfigComboboxOrigenTraspaso,
				"combobox-razon-retiro"							:obj.getConfigComboboxRazonRetiro,
				"combobox-glosa-apv"							:obj.getConfigComboboxGlosaAPV,
				"combobox-otro-producto-agrupador"				:obj.getConfigComboboxOtroProductoAgrupador,
				"combobox-ano-ahorro-bonificacion"				:obj.getConfigComboboxAnoAhorroBonificacionAPV,
				"combobox-ano-proceso-bonificacion"				:obj.getConfigComboboxAnoProcesoBonificacionAPV,
				"combobox-proceso-origen-destino" 				:obj.getConfigComboboxProcesoOrigenDestino,
				"combobox-cuentas-bancarias-socio-negocio-corredora" :obj.getConfigComboboxCuentasBancariasSocioNegocioCorredora,
				"combobox-perfil-comercial-apv"					: obj.getConfigComboboxPerfilComercialAPV,
				"combobox-documento-entidad-nomina"				: obj.getConfigComboboxDocumentoEntidadNomina,
				"combobox-documento-identidad-nomina"			: obj.getConfigComboboxDocumentoIdentidadNomina,

				/* FIN APV */
				
				/* OPCIONES */				
				"combobox-mercado-opciones"						:obj.getConfigComboboxMercadoOpciones,
				"combobox-familia-opciones"						:obj.getConfigComboboxFamiliaOpciones,
				"combobox-tipo-opciones"						:obj.getConfigComboboxTipoOpciones,
				"combobox-tipo-subyacente-opciones"				:obj.getConfigComboboxSubyacenteOpciones,
				"combobox-estilo-liquidacion-opciones"			:obj.getConfigComboboxEstiloOpciones,
				"combobox-modalidad-liquidacion-opciones"		:obj.getConfigComboboxModalidadLiquidacionOpciones,
				
				"combobox-tipo-producto-plantilla-producto"		:obj.getConfigComboboxTipoProductoPlantillaProducto,
				"combobox-tipo-notificacion-carta"				:obj.getConfigComboboxTipoNotificacionCarta,
				
				"combobox-contrato-folio-csc"					:obj.getFolioContratoCSC,
				
				
				/* Peru */
				"combobox-grado-instruccion"					:obj.getConfigComboboxGradoInstruccion,
				"combobox-complementos"					        :obj.getConfigComboboxComplementos,
				"combobox-certificados-djs"					    :obj.getConfigComboboxCertificadosDJs,
				"combobox-tipo-utilidad-calificacion-tributaria":obj.getConfigComboboxTipoUtilidadesCalificacionTributaria,
				"combobox-instrumento-reporte"					:obj.getConfigComboboxInstrumentoReporte,
				"combobox-reportantes-asociados"				:obj.getConfigComboboxReportantesAsociados,
				"combobox-modulos-pauta"					    :obj.getConfigComboboxModulosPauta,
				"combobox-tipo-salida"							:obj.getConfigComboboxTipoSalida,
				"combobox-tipo-proceso-apertura-cierre-modulo"	:obj.getConfigComboboxTipoProcesoAperturaCierreModulo,
				"combobox-interfaces"							:obj.getConfigComboboxInterfaces,
				
				"combobox-cotitulares"							:obj.getConfigComboboxCotitulares,
				"combobox-tipo-carga-tran-ord"					:obj.getConfigComboboxTipoCargaTranOrd,
				"combobox-tipo-indice-anexo7"					:obj.getConfigTipoIndiceAnexo7,
				
				"combobox-modalidad-ejercicio"					:obj.getConfigComboboxModalidadEjercicio,
				"combobox-parametros-ejercicio"					:obj.getConfigComboboxParametroEjercicio,
				"combobox-documentos-mesa"						:obj.getConfigComboboxDocumentosMesa,
				"combobox-contactos"							:obj.getConfigComboboxContactos,

				/* AFPs */
				"combobox-afp"									:obj.getConfigComboboxAFPs,
				"combobox-multifondo"							:obj.getConfigComboboxMultifondo,
				
				"combobox-mercado-ffmm"							:obj.getConfigComboboxMercadoFFMM,
				"combobox-negociacion"							: obj.getConfigComboboxNegociacion,
				"combobox-codigo-tipo-relacion"					: obj.getConfigComboboxCodigoTipoRelacion,
				"combobox-representante-legal"					: obj.getConfigComboboxRepresentanteLegal,

				"combobox-representante-legal-corredor"			: obj.getConfigComboboxRepresentanteLegalCorredor,
				"combobox-representante-legal-enrolar"			: obj.getConfigComboboxRepresentanteLegalEnrolar,

				
				/* RIF-DIFI*/
				"combobox-rifi-difi"							:obj.getConfigComboboxRifiDifi,
				
				"combobox-informe-comisiones-mensuales"			:obj.getConfigComboboxInformeCobroComisionesMensuales,
				
				"combobox-formulario"							:obj.getConfigComboboxFormulario,
				"combobox-campo"								:obj.getConfigComboboxCampo,
				/*NORMATIVA DJ1*/
				"combobox-normativa-homologacion"				:obj.getConfigComboboxNormativaHomologacion,
				
				"combobox-causa-no-tin"							:obj.getConfigComboboxCausaNoTin,
				"combobox-concepto-orden"					    : obj.getConfigComboboxConceptoOrden,
				"combobox-concepto-contrato"					: obj.getConfigComboboxConceptoContratoComision,
				"combobox-tipo-mensaje"						    : obj.getConfigComboboxTipoMensaje,
				"combobox-estado-csc"                           : obj.getConfigComboboxEstadoCSC,
				"combobox-estado-mandato-pac"                   : obj.getConfigComboboxEstadoMandatoPAC,
				"combobox-situacion-laboral"					:obj.getConfigComboboxSituacionLaboral,
				"combobox-tipo-saldo"							:obj.getConfigComboboxFiltroTipoSaldo,
				"combobox-tipo-motivo"							:obj.getConfigTipoMotivo,
				"combobox-estado-motivo"						:obj.getConfigEstadoMotivo,
				"combobox-motivo"						        :obj.getConfigMotivo,
				"combobox-trimestres"                           :obj.getConfigComboboxTrimestresFFMM,
				"combobox-anios-comisiones-ffmm"                :obj.getConfigComboboxAniosComisionesFFMM,
				"combobox-ganancia-capital"                		:obj.getConfigComboboxGananciaCapital,
				"combobox-ffmm-dolar"                			:obj.getConfigComboboxFFMMDolar,
				"combobox-tipo-carga-masiva-mesa-rf"			:obj.getConfigComboboxTipoCargaMasivaMesaRF,
				"combobox-tipo-casilla"							:obj.getConfigTipoCasilla,
				"combobox-sicam2p1-moneda"						:obj.getConfigComboboxSicam2p1Moneda,
				/*NORMATIVO CALIFICACIONES*/
				"combobox-origen-calificacion-tributaria"		:obj.getConfigComboboxOrigenCalificacionTributaria,
				"combobox-periodo-comercial-calificaciones-tributarias"		:obj.getConfigComboboxPeriodoComercialCalificacionesTributarias,
                "combobox-mercado-calificaciones-tributarias"		:obj.getConfigComboboxMercadoCalificacionesTributarias,
                
				"combobox-forma-cobro"							:obj.getConfigComboboxDetalleTipoIndicadorFormaCobro,
				"combobox-portafolio-defecto"					:obj.getConfigComboboxPortafoliosPorDefecto,
				"combobox-tipo-Pegado"							:obj.getConfigComboboxTipoPegado,
				"combobox-tipo-Pegado-Monto"					:obj.getConfigComboboxTipoPegadoMonto,
				"combobox-producto-basilea"						: obj.getConfigComboboxProductoBasilea,
				"combobox-condicion-liquidacion-negociacion"	: obj.getConfigComboboxCondicionLiquidacionNegociacion,
				"combobox-tipo-instrumento-moneda-orden"		: obj.getConfigTipoInstrumentoMonedaOrden,
				"combobox-moneda-negociacion"                   : obj.getConfigComboboxMonedaNegociacion,
				"combobox-opciones-cobro-disponibles"           : obj.getConfigComboboxOpcionesCobroDisponible,
				"combobox-instrumento-asignacion-masiva"		: obj.getConfigComboboxInstrumentoAsignacionMasiva,
				"combobox-condicion-liquidacion-asignacion-masiva"		: obj.getConfigComboboxCondicionDeLiquidacionAsignacionMasiva,
				"combobox-bolsa-asignacion-masiva"		: obj.getConfigComboboxBolsaAsignacionMasiva,
				"combobox-tipo-operacion-asignacion-masiva"		: obj.getConfigComboboxTipoOperacionAsignacionMasiva,
				"combobox-marca-negociacion-asignacion-masiva"	: obj.getConfigComboboxMarcaNegociacionAsignacionMasiva,
				"combobox-libro-orden-asignacion-masiva"		: obj.getConfigComboboxLibroOrdenesAsignacionMasiva,
				"combobox-boveda-asignacion-masiva"				: obj.getConfigComboboxBovedaAsignacionMasiva,
				"combobox-tipo-archivo-ffmm"					: obj.getConfigComboboxTipoArchivoFFMM
		}
		
		var em = configuraciones[elem];
		var em2 = obj.configuracionCombos[elem];
		
		if (em)
			return em();
		if (em2)
			return em2;
		throw("TIPO NO EXISTE IMPLEMENTADO:" +elem);
		
	}
	
	
	return obj;
});

angular.module('ModuloPlataforma').constant("OPCIONES_BCSALERTA", {
        "SI": 1,
        "NO": 2,
        "ACEPTAR": 3,
        "CANCELAR" :4  
    });
