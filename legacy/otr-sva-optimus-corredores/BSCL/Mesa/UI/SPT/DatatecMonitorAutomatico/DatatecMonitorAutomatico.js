// Datatec Monitor Automatico: lee periodicamente el archivo local que el operador conecta
// (donde la aplicacion Datatec deja las operaciones) y las ingresa como operaciones spot,
// sin intervencion del operador salvo la conexion inicial.
//
// La carga manual de archivo sigue viviendo en la ventana Carga Online Datatec
// (Mesa/UI/SPT/Datatec/), que no se modifica. Ambas ventanas toman el mismo bloqueo
// (BloquearDATATEC), por lo que no pueden operar en simultaneo sobre el mismo archivo.
angular.module('ModuloMesa').controller('DatatecMonitorAutomaticoCtl',
function($scope,$translate,$q,$timeout,bcsComunes,bcsServiciosMesa,bcsDatatecFs,bcsDatatecProceso){

	var idVentana = bcsComunes.ventanaWinId($scope);

	var nombreCorredora = '';

	//Monitor de archivo local
	var INTERVALO_MINIMO  = 5000;
	var INTERVALO_DEFECTO = 5000;
	var monitorActivo = false;
	var intervaloMonitor = INTERVALO_DEFECTO;
	var timerDatatec  = null;
	var handleArchivo = null;
	var ultimaVista   = { candidata: null, procesado: null };

	$scope.data = {};
	$scope.config = {};
	$scope.config.overlay = bcsComunes.overlay;
	$scope.configColumnas = [];

	$scope.data.codigoArchivo = 'CARGA_ARCHIVO_DATATEC';
	$scope.data.conHeader = false;
	$scope.data.texto = false;
	$scope.data.enProceso = false;
	$scope.data.fsSoportado = bcsDatatecFs.soportado();
	$scope.data.fsEstado = 'DESCONECTADO';
	$scope.data.fsArchivo = '';
	$scope.data.fsUltimaLectura = '';

	//Estado real del bloqueo compartido (parametro de mesa USUARIOBLOQUEODATATEC), consultado
	//al abrir la ventana via BloquearDATATEC en modo 'C' (solo lectura, no muta nada).
	//BLOQUEADO = esta sesion lo tiene tomado; BLOQUEADO_OTRO = lo tiene otro usuario.
	$scope.data.bloqueoEstado = 'DESCONOCIDO';   // DESCONOCIDO | LIBRE | BLOQUEADO | BLOQUEADO_OTRO
	$scope.data.bloqueoUsuario = '';             // usuario real que tiene el bloqueo, para mostrarlo


	//DATASOURCES
	var dsDefDatatec = {
		type : "array",
		definition: {
			sort : { field: "NumeroRegistro", dir: "asc" },
			schema: {
				model: {
					fields: {
						NumeroRegistro  : {type: "number"},
						IdEstadoRegistro: {type: "string"},
						GlosaProceso    : {type: "string"}
					}
				}
			}
		}
	};

	var archivosFormatoDatasource = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1358",
				dataType: "json"
			}
		},
		schema: {
			model: {
				fields: {
					IdArchivo: {type:"number"},
					IdEmpresa: {type:"number"},
					CodigoArchivo: {type:"string"},
					TieneSeparador: {type:"string"},
					SeparadorColumnas: {type:"string"},
					TieneCabecera: {type:"string"},
					IdFormato: {type:"number"},
					CodigoCampo: {type:"string"},
					TipoDato: {type:"string"},
					Posicion: {type:"string"},
					LargoCampo: {type:"string"},
					EsObligatorio: {type:"string"},
					Validacion: {type:"string"},
					MensajeValidacion: {type:"string"},
					IdArchivoFormato: {type:"number"}
				}
			}
		},
		sort : [{ field: "Posicion", dir: "ASC"}],
	});

	// Razon social de la corredora en Datatec. El filtro IdEmpresa = 1 esta fijo, igual que
	// en la ventana Carga Online Datatec: se preserva para que ambas ventanas resuelvan la
	// misma corredora y calculen el mismo IDOPERACION sobre el mismo archivo.
	var consultarEmpresaaDatasource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
					dataType: "json"
				}
			},
			filter: [{ field: "IdTipoProducto", operator: "eq", value: "MEME" },
					{ field: "CodigoParametro", operator: "eq", value: "RAZONSOCIALDATATEC" },
					{ field: "IdEmpresa", operator: "eq", value: 1 }],
			schema: {
				model: {
					fields: {
						IdTipoProducto: {type:"string"},
						CodigoParametro: {type:"string"},
						Valor: {type:"string"},
						Descripcion: {type:"string"},
						IdEmpresa: {type:"number"}
					}
				}
			},
		}
	});


	//GRILLA DE LOG
	var dsLog = new bcs.DataSource(dsDefDatatec);
	$scope.confGrillaLog = {
		dataSource: dsLog,
		columns : [
			{field: "NumeroRegistro", title: "UI_NRO_REGISTRO", width: "15px",  sortable : true},
			{field: "GlosaProceso",   title: "UI_LOG",          width: "110px", sortable : true}
		],
		sortable    : true,
		pageable    : true,
		filterable  : false,
		navigatable : true,
		height      : 420,
		selectable  : "row",
		autoBind    : false,
		gridCreated : function(grid){ $scope.cargarConfiguracionArchivo(); },
		rowColorBackground :
			function(row) {
				if(row.IdEstadoRegistro === 'ERR') return "red";
				if(row.IdEstadoRegistro === 'PEN') return "yellow";
				return "";
			}
	};


	// ----------------------------------------------------------------------------------
	// CONFIGURACION
	// ----------------------------------------------------------------------------------

	//Formato del archivo y razon social de la corredora
	$scope.cargarConfiguracionArchivo = function(){
		var filterFile = [];
		filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
		filterFile.push({ field: "CodigoArchivo", operator: "eq", value: $scope.data.codigoArchivo });

		archivosFormatoDatasource.query({filter:filterFile}).then(function() {
			$scope.configColumnas = archivosFormatoDatasource.view();

			if($scope.configColumnas.length > 0){
				if($scope.configColumnas[0].TieneCabecera === "S"){
					$scope.data.conHeader = true;
				}
			}
			else{
				bcsComunes.msg("error",'Error',$translate.instant("ARCHIVO_NO_CONFIGURADO"));
			}
		});

		consultarEmpresaaDatasource.query().then(function(){
			if(consultarEmpresaaDatasource.data().length === 1){
				nombreCorredora = consultarEmpresaaDatasource.data()[0].Valor;
			}
			else{
				bcsComunes.msg("error","Error", $translate.instant("NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA"));
			}
		},function(){
			bcsComunes.msg("error","Error", $translate.instant("NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA"));
		});
	};

	//Parametros de mesa: ruta, nombre de archivo, horario e intervalo
	var parametrosSP = {};
	parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
	parametrosSP.p_IdTipoProducto = 'MEME';
	parametrosSP.p_CodigoParametro = '';

	bcsServiciosMesa.ConsultarConfiguracionMesa(parametrosSP, function(respuesta){
		if (respuesta.P_OUT_DATOSMESA.length === 0){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_OBTENER_PARAMETROS_MESA'));
			bcsComunes.ventanaCerrar(idVentana);
			return;
		}

		$scope.data.RutaDatatecBase = "";
		$scope.data.NombreArchivo = "";
		angular.forEach(respuesta.P_OUT_DATOSMESA, function(value) {
			if (value.CodigoParametro === "CODRUTADATATEC")
				$scope.data.RutaDatatecBase = value.Valor || "";
			else if (value.CodigoParametro === "NOMBREARCHIVODATATEC")
				$scope.data.NombreArchivo = value.Valor || "";
			else if (value.CodigoParametro === "HORAFINPROCESO")
				$scope.data.HoraFinProceso = value.Valor;
			else if (value.CodigoParametro === "INTENTOSDEREPROCESO")
				$scope.data.Intentos = value.Valor;
			else if (value.CodigoParametro === "TIEMPOESPERAREPROCESO")
				$scope.data.Intervalo = value.Valor * 1000;// En segundos
		});

		//Ruta y nombre completos son solo referencia de lo configurado, aunque vengan
		//vacios: el archivo real lo elige el operador en el picker, sin validarse contra
		//este nombre ni exigir que estos parametros de mesa esten configurados. La ventana
		//siempre abre desconectada: aunque el permiso sobre un archivo ya conectado en una
		//sesion anterior siga vigente, el operador debe presionar "Conectar archivo" para
		//arrancar el monitor.
		$scope.data.RutaDatatec = $scope.data.RutaDatatecBase + $scope.data.NombreArchivo;

	}, function(){
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_OBTENER_PARAMETROS_MESA'));
		bcsComunes.ventanaCerrar(idVentana);
	});


	// ----------------------------------------------------------------------------------
	// BLOQUEO DE LA INTERFAZ DATATEC
	// ----------------------------------------------------------------------------------
	// Es el mismo bloqueo que toma la ventana Carga Online Datatec (parametro de mesa
	// USUARIOBLOQUEODATATEC). Tomarlo es automatico (boton Bloquear o "Conectar archivo",
	// ver mas abajo); liberarlo siempre es manual (boton Desbloquear) — ni desconectar el
	// archivo ni salir de la ventana lo liberan solos.

	// Intenta tomar el bloqueo (p_BloqueoDesbloqueo:'B'). Devuelve una promesa que resuelve
	// true si quedo tomado por esta sesion, false si lo tiene otro usuario (la alerta con el
	// nombre de ese usuario ya se muestra aqui mismo). La usan tanto el boton "Bloquear" como
	// "Conectar archivo", que la invoca automaticamente si esta sesion aun no lo tiene.
	var tomarBloqueo = function() {
		var deferred = $q.defer();
		var parametros = {};
		parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametros.p_CodigoUsuario = $scope.SesionUsuario;
		parametros.p_BloqueoDesbloqueo = 'B';

		bcsServiciosMesa.BloquearDATATEC(parametros, function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR > 0 ) {
				//El servicio informa en el detalle que usuario tiene tomado el bloqueo
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_DATATEC_BLOQUEADA"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				deferred.resolve(false);
				return;
			}
			$scope.data.bloqueoEstado = 'BLOQUEADO';
			$scope.data.bloqueoUsuario = $scope.SesionUsuario;
			bcsComunes.msg("info",$translate.instant("UI_DATATEC_MONITOR_AUTOMATICO"),$translate.instant("UI_DATATEC_BLOQUEO_OK"));
			deferred.resolve(true);

		}, function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_BLOQUEAR_INTERFAZ_DATATEC'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			deferred.resolve(false);
		});

		return deferred.promise;
	};

	$scope.data.bloquearDatatec = function() {
		tomarBloqueo();
	};

	$scope.data.desbloquearDatatec = function() {
		var parametros = {};
		parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametros.p_CodigoUsuario = $scope.SesionUsuario;
		parametros.p_BloqueoDesbloqueo = 'D';

		bcsServiciosMesa.BloquearDATATEC(parametros, function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR > 0 ) {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_DESBLOQUEAR_INTERFAZ_DATATEC"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				return;
			}
			$scope.data.bloqueoEstado = 'LIBRE';
			$scope.data.bloqueoUsuario = '';
			bcsComunes.msg("info",$translate.instant("UI_DATATEC_MONITOR_AUTOMATICO"),$translate.instant("UI_DATATEC_DESBLOQUEO_OK"));

		}, function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_DESBLOQUEAR_INTERFAZ_DATATEC'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		});
	};

	// Consulta de solo lectura (p_BloqueoDesbloqueo:'C'): nunca falla ni muta el parametro,
	// solo informa si esta bloqueado y por quien. Se llama una vez al abrir la ventana para
	// avisar de inmediato si otro usuario ya la tiene tomada, sin esperar a que el operador
	// intente Bloquear o Conectar archivo.
	var consultarEstadoBloqueo = function() {
		var parametros = {};
		parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametros.p_CodigoUsuario = $scope.SesionUsuario;
		parametros.p_BloqueoDesbloqueo = 'C';

		bcsServiciosMesa.BloquearDATATEC(parametros, function(respuesta){
			if (respuesta.P_OUT_VALOR === 'S') {
				$scope.data.bloqueoUsuario = respuesta.P_OUT_MODIFICADORPARAMETRO || '';
				if (respuesta.P_OUT_MODIFICADORPARAMETRO === $scope.SesionUsuario) {
					//Ya lo tengo tomado desde otra pestana/sesion propia
					$scope.data.bloqueoEstado = 'BLOQUEADO';
				} else {
					$scope.data.bloqueoEstado = 'BLOQUEADO_OTRO';
					bcsComunes.msg("warning", $translate.instant("UI_DATATEC_MONITOR_AUTOMATICO"),
						$translate.instant("UI_DATATEC_YA_BLOQUEADA_POR").replace("XXX", respuesta.P_OUT_MODIFICADORPARAMETRO));
				}
			} else {
				$scope.data.bloqueoEstado = 'LIBRE';
				$scope.data.bloqueoUsuario = '';
			}
		}, function(){
			//Falla de comunicacion: no se puede afirmar nada, queda en DESCONOCIDO. No cierra
			//la ventana (es solo informativa) ni impide seguir usandola.
		});
	};

	consultarEstadoBloqueo();


	// ----------------------------------------------------------------------------------
	// MONITOR DE ARCHIVO LOCAL
	// ----------------------------------------------------------------------------------
	// El operador elige el archivo directamente (showOpenFilePicker), no una carpeta.
	// NOMBREARCHIVODATATEC (mostrado arriba como referencia) no se valida contra el
	// archivo elegido: el operador puede conectar cualquier archivo, sea cual sea su
	// nombre real en disco.

	//Boton "Conectar archivo": aporta el gesto de usuario que exige la API. Antes de abrir el
	//picker, exige el bloqueo — si otro usuario lo tiene, se aborta la conexion con la misma
	//alerta que muestra el boton Bloquear, sin llegar a abrir el archivo ni arrancar el monitor.
	//Si esta sesion ya lo tiene tomado (bloqueoEstado='BLOQUEADO'), no se vuelve a llamar al
	//servicio: 'B' es un toggle y llamarlo de nuevo lo liberaria por error.
	$scope.data.conectarArchivo = function() {
		if (!$scope.data.fsSoportado) {
			bcsComunes.msg("error", "Error", $translate.instant("UI_DATATEC_FS_NO_SOPORTADO"));
			return;
		}

		var aseguraBloqueo = ($scope.data.bloqueoEstado === 'BLOQUEADO') ? $q.when(true) : tomarBloqueo();

		aseguraBloqueo.then(function(bloqueoOk) {
			if (!bloqueoOk) return; //La alerta con el usuario que lo tiene ya se mostro

			bcsDatatecFs.reconectar(true).then(function(handle) {
				return handle || bcsDatatecFs.conectar();
			}).then(function(handle) {
				if (!handle) return;

				handleArchivo = handle;
				$scope.data.fsArchivo = handle.name;
				ultimaVista = { candidata: null, procesado: null };
				iniciarMonitor();
			}).catch(function(e) {
				//AbortError = el operador cerro el picker sin elegir archivo
				if (e && e.name === 'AbortError') return;
				bcsComunes.msg("error", "Error", $translate.instant("UI_DATATEC_ARCHIVO_NO_AUTORIZADO"));
			});
		});
	};

	//Olvida el archivo conectado: la siguiente vez que se presione "Conectar archivo" se
	//abre el picker de nuevo (sin esto, reconectar() encontraria el handle guardado y
	//nunca dejaria elegir un archivo distinto al ya conectado)
	$scope.data.desconectarArchivo = function() {
		detenerMonitor();
		handleArchivo = null;
		$scope.data.fsArchivo = '';
		$scope.data.fsEstado = 'DESCONECTADO';
		bcsDatatecFs.olvidar();
	};

	// Timer auto-reprogramable: cada ciclo agenda el siguiente recien cuando termina (en el
	// finally de revisarArchivo), en vez de un $interval de periodo fijo. Con un $interval,
	// si IngresoDatatec tarda mas que el intervalo (ej. un archivo con miles de filas), los
	// ticks que caen mientras la llamada anterior sigue en vuelo se descartan enteros y se
	// pierden ciclos de revision sin dejar rastro. Asi, el intervalo se cuenta siempre desde
	// que el ciclo anterior libero el semaforo, nunca se descarta un ciclo silenciosamente.
	var iniciarMonitor = function() {
		if (monitorActivo) return;
		monitorActivo = true;

		intervaloMonitor = Number($scope.data.Intervalo) || INTERVALO_DEFECTO;
		if (intervaloMonitor < INTERVALO_MINIMO) intervaloMonitor = INTERVALO_MINIMO;

		$scope.data.fsEstado = 'CONECTADO';
		revisarArchivo();
	};

	var detenerMonitor = function() {
		monitorActivo = false;
		if (timerDatatec) {
			$timeout.cancel(timerDatatec);
			timerDatatec = null;
		}
	};

	var agendarProximaRevision = function() {
		if (!monitorActivo) return;
		timerDatatec = $timeout(revisarArchivo, intervaloMonitor);
	};

	//El formato del archivo y la razon social se cargan en cadenas asincronas propias
	//(gridCreated), independientes de la que arranca el monitor: sin esta guarda un tick
	//temprano leeria el archivo sin configuracion y lo descartaria.
	var configuracionLista = function() {
		return $scope.configColumnas.length > 0 && nombreCorredora !== '';
	};

	var revisarArchivo = function() {
		if (!monitorActivo) return;

		if (!handleArchivo) {
			agendarProximaRevision();
			return;
		}

		if (!configuracionLista()) {
			$scope.data.fsEstado = 'ESPERANDO_CONFIG';
			agendarProximaRevision();
			return;
		}

		$scope.data.enProceso = true;

		bcsDatatecFs.leerSiCambio(handleArchivo, ultimaVista)
			.then(function(res) {
				if (res.firma) ultimaVista.candidata = res.firma;

				if (res.estado === 'SIN_PERMISO') {
					//El permiso se revoco o expiro: se exige reconexion manual
					detenerMonitor();
					handleArchivo = null;
					$scope.data.fsEstado = 'DESCONECTADO';
					bcsComunes.msg("error", "Error", $translate.instant("UI_DATATEC_PERMISO_REVOCADO"));
					return null;
				}

				if (res.estado !== 'LEIDO') {
					$scope.data.fsEstado = res.estado;
					return null;
				}

				$scope.data.fsEstado = 'CONECTADO';
				$scope.data.fsUltimaLectura = kendo.toString(new Date(), "dd-MM-yyyy HH:mm:ss");

				return procesarContenido(res.texto).then(function(salida) {
					//El archivo se marca como procesado solo si no queda nada por reintentar:
					//ante una falla de comunicacion se vuelve a intentar en el proximo ciclo,
					//en lugar de perder el archivo por haberlo dado por consumido.
					if (!salida || !salida.reintentar) {
						ultimaVista.procesado = res.firma;
					}
				});
			})
			.catch(function() {
				$scope.data.fsEstado = 'ERROR';
				bcsComunes.msg("error", "Error", $translate.instant("UI_DATATEC_ERROR_LECTURA"));
			})
			.finally(function() {
				$scope.data.enProceso = false;
				agendarProximaRevision();
			});
	};

	$scope.$on('$destroy', detenerMonitor);


	// ----------------------------------------------------------------------------------
	// PROCESO
	// ----------------------------------------------------------------------------------

	// Parsea el contenido leido y lo ingresa como operaciones spot.
	//
	// Devuelve una promesa que se resuelve en TODOS los caminos, incluidos los de error: el
	// monitor la usa para liberar el semaforo, y un camino sin cerrar lo dejaria tomado.
	// Resuelve con {reintentar:true} solo cuando la causa es transitoria (falla de
	// comunicacion con un servicio); en ese caso el monitor vuelve a leer el mismo archivo.
	var procesarContenido = function(contenido) {
		var deferred = $q.defer();

		if (!contenido) {
			bcsComunes.msg("error",'Error',$translate.instant("DEBE_SELECCIONAR_ARCHIVO_CON_REGISTROS"));
			deferred.resolve({reintentar:false});
			return deferred.promise;
		}

		dsLog.removeAll();
		$scope.data.texto = true;

		var config = bcsDatatecProceso.construirConfiguracion(
			$scope.configColumnas, $scope.data.codigoArchivo, $scope.data.conHeader);

		var parseo = bcsDatatecProceso.parsearArchivo(contenido, config);

		if (parseo.errorCabecera) {
			bcsComunes.msg("error",'Error', parseo.errorCabecera.mensaje);
			$scope.data.texto = false;
			deferred.resolve({reintentar:false});
			return deferred.promise;
		}

		var parametrosPuntas = {};
		parametrosPuntas.p_IdEmpresa     = $scope.SesionIdEmpresa;
		parametrosPuntas.p_IdMoneda      = 'USD';
		parametrosPuntas.p_MontoPorTramo = 0;

		bcsServiciosMesa.ConsultarPuntasPorTramo(parametrosPuntas, function(respuesta){
			if(respuesta.P_OUT_ADVERTENCIASDETALLE.length != 0){
				if(respuesta.P_OUT_ADVERTENCIASDETALLE[0].Codigo === 'PUNTA'){
					bcsComunes.msg("error","error",$translate.instant('UI_FALTA_CONF_PUNTA'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				}
				if(respuesta.P_OUT_ADVERTENCIASDETALLE[0].Codigo === 'TRAMO'){
					bcsComunes.msg("error","error",$translate.instant('UI_FALTA_CONF_TRAMO'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				}
			}

			if(respuesta.P_OUT_PUNTAS.length === 0){
				//Sin puntas configuradas no hay nada que enviar. No se reintenta: requiere
				//que alguien complete la configuracion de puntas o tramos de la mesa.
				$scope.data.texto = false;
				deferred.resolve({reintentar:false});
				return;
			}

			bcsDatatecProceso.aplicarPuntas(parseo.filas, respuesta.P_OUT_PUNTAS, nombreCorredora);

			var p_IngresoDatatec = {};
			p_IngresoDatatec.p_IdEmpresa        = $scope.SesionIdEmpresa;
			p_IngresoDatatec.p_IdUsuario        = $scope.SesionIdUsuario;
			p_IngresoDatatec.p_IdModulo         = 'MSP';
			p_IngresoDatatec.p_UsaReglasNegocio = 'S';
			p_IngresoDatatec.p_DatosDatatec     = parseo.validas;

			bcsServiciosMesa.IngresoDatatec(p_IngresoDatatec, function(respuesta){
				if(respuesta.P_OUT_ERRORESCONTADOR === 0){
					var datosLog = respuesta.P_OUT_DATOSDATATEC;
					if(parseo.errores.length != 0){
						datosLog = datosLog.concat(parseo.errores);
					}

					$scope.confGrillaLog.dataSource.data(datosLog);

					if(respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR > 0){
						angular.forEach(respuesta.P_OUT_ERRORCONFIGURACIONDETALLE, function(value){
							bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.codigo));
						});
					}
				}
				else{
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_PROCESAR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
				}
				$scope.data.texto = false;
				deferred.resolve({reintentar:false});

			}, function(respuesta){
				//Falla de comunicacion con IngresoDatatec: se reintenta el mismo archivo.
				//El servicio es idempotente por IdDatatec + ClaseMensaje, asi que un
				//reintento no puede duplicar operaciones ya ingresadas.
				$scope.data.texto = false;
				bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				deferred.resolve({reintentar:true});
			});

		}, function(respuesta){
			//Falla de comunicacion con ConsultarPuntasPorTramo: nada se envio todavia
			$scope.data.texto = false;
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			deferred.resolve({reintentar:true});
		});

		return deferred.promise;
	};


	// ----------------------------------------------------------------------------------
	// ACCIONES
	// ----------------------------------------------------------------------------------

	//Limpia el log y permite volver a procesar el archivo actual sin esperar que cambie
	$scope.data.limpiarDatos = function(){
		dsLog.removeAll();
		ultimaVista = { candidata: null, procesado: null };
	};

	$scope.data.salir = function(){
		detenerMonitor();
		bcsComunes.ventanaCerrar(idVentana);
	};
});
