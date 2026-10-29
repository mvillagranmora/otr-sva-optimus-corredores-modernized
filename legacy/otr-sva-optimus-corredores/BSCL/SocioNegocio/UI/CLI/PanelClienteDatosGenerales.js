/*************************************************************
 * Purpose	: Panel Cliente Datos Generales.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 * 
 * Purpose	: Cambios asociados Optimus Perú
 * Author	: JGarces
 * Date		: 14/03/2017
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClienteDatosGeneralesCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			$timeout,
			$resource,
			bcsConfiguracionSociodeNegocio,
			bcsServicioPlataforma)
	{
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/	
		const pcdg = this;
		pcdg.ventanaId = bcsComunes.ventanaWinId($scope);
		pcdg.dataCliente = {};
		pcdg.dataClienteOriginal = {};
		pcdg.dataSolicitudCambio= {};
		pcdg.dataAutorizacionEvento = {};
		pcdg.dataAutorizacionEvento.EventosVencidos = false;
		pcdg.dataSolicitudCambio.CambiosPendientes = false;
		pcdg.dataSolicitudCambio.CambioEstadoPendiente = false;
		pcdg.camposRequeridos = {};
		var idVentana = bcsComunes.ventanaWinId($scope)
		bcsComunes.ventanaAutoOverlay(pcdg.ventanaId,'id_tipoCliente input',5000);

		const asignarValoresPorDefecto = function asignarValoresPorDefecto() {
			$.each(pcdg.camposRequeridos, function (key, value) {
				const noEsRequerido = value.Requerido === 'N';
				const esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				const tipoCampo = value.TipoCampoFrontend;
				const esCampoEnBlanco = tipoCampo === 'bcs-input-texto' ? !pcdg.dataCliente[value.CodigoValorFrontend] : !pcdg.dataClienteOriginal[value.CodigoValorFrontend]

				if (noEsRequerido && esValorDefectoValido && esCampoEnBlanco) {
					let typedValue = value.ValorDefectoCampoFormulario;

					if (value.TipoDato === 'NUMBER') typedValue = parseInt(typedValue, 10);
					if (tipoCampo === 'bcs-input-texto') {
						pcdg.dataCliente[value.CodigoValorFrontend] = typedValue;
					} else {
						pcdg.dataClienteOriginal[value.CodigoValorFrontend] = typedValue;
					}
				}
			});
		};

		var cargarDatosMaestropcdg = function() {
			$scope.admSoc.socioNegocioPromise.then(function(){
				pcdg.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['DATOS_GENERALES_CLIENTE'];

				bcsServicioPlataforma.CamposRequeridosFormulario(pcdg.CodigoFormulario, function(respuesta) {
					pcdg.camposRequeridos = respuesta;
				});

				pcdg.IdCliente = ($scope.admSoc.dataSocioNegocio.IdCliente?$scope.admSoc.dataSocioNegocio.IdCliente:0);
				
				
				var ClienteDatosGeneralesDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700353",
								dataType: "json"
						    }
						},
						filter: { field: "IdCliente", operator: "eq", value: Number(pcdg.IdCliente) },
						schema: bcsConfiguracionSociodeNegocio.getSchemaCliente()
					}
				});
				
				
			
				ClienteDatosGeneralesDatasource.query().then(function(){
					if (ClienteDatosGeneralesDatasource.data().length != 1){
						bcsComunes.ventanaOverlay(pcdg.ventanaId,false);
					}
					else{
						pcdg.dataCliente = $.extend({},ClienteDatosGeneralesDatasource.data()[0]);
						pcdg.dataCliente.FechaBloqueo = pcdg.dataCliente.FechaBloqueo;
						pcdg.IdPersonaEjecutivo = pcdg.dataCliente.IdPersonaEjecutivo;
						pcdg.LogCambioEjecutivoOptions.dataSource.filter([{ field: "IdCliente", operator: "eq", value: Number(pcdg.IdCliente)}]);
						pcdg.dataClienteOriginal = $.extend({},pcdg.dataCliente);

						// Se consulta el Socio de Negocio del Ejecutivo
						var socioDeNegocioEjecutivoDatasource = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering:true,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1050",
										dataType: "json"
								    }
								},
								filter: { field: "IdPersona", operator: "eq", value: Number(pcdg.IdPersonaEjecutivo) },
								schema: bcsConfiguracionSociodeNegocio.getSchemaSocioDeNegocioEjecutivo()
							}
						});
						socioDeNegocioEjecutivoDatasource.query().then(function(result){
							if(socioDeNegocioEjecutivoDatasource.data().length > 0){
								pcdg.dataSocioDeNegocio = $.extend({},socioDeNegocioEjecutivoDatasource.data()[0]);							
								pcdg.IdSocNegPersonaEjecutivo = pcdg.dataSocioDeNegocio.IdSocioDeNegocio;
								pcdg.dataCliente.IdPersonaEjecutivo = pcdg.dataSocioDeNegocio.IdPersona;
								pcdg.dataClienteOriginal.IdPersonaEjecutivo = pcdg.dataSocioDeNegocio.IdPersona;
							}
							
							//Se consultan autorizaciones vencidas
							var filterSol = new Array();
							filterSol.push({ field: "IdEstado", operator: "eq", value: 'VCD' },
											{ field: "IdCliente", operator: "eq", value: pcdg.IdCliente});
							
							var AutorizacionEventoDatasource = new bcs.DataSource({
								type: "odata",
								definition : {
									serverFiltering: true,
									transport: {
										read: {
											url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1396",
											dataType: "json"
										}
									},
									filter: filterSol,
									schema: bcsConfiguracionSociodeNegocio.getSchemaAutorizacionEvento()
								}
							});
							AutorizacionEventoDatasource.query().then(function(result){
								if(AutorizacionEventoDatasource.data().length > 0){
									pcdg.dataAutorizacionEvento = $.extend({},AutorizacionEventoDatasource.data());
									pcdg.dataAutorizacionEvento.EventosVencidos = true;
								}
							});
						
							// Se consultan solicitudes de cambios Pendientes
							var filterSol = new Array();
							filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'CLIENTE' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: pcdg.IdCliente},
										   { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
							var SolicitudDeCambioDatasource = new bcs.DataSource({
								type: "odata",
								definition : {
									serverFiltering:true,
									transport: {
										read: {
											url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
											dataType: "json"
									    }
									},
									filter: filterSol,
									schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
								}
							});
							SolicitudDeCambioDatasource.query().then(function(result){
								if(SolicitudDeCambioDatasource.data().length > 0){
									pcdg.dataSolicitudCambio = $.extend({},SolicitudDeCambioDatasource.data());
									pcdg.dataSolicitudCambio.CambiosPendientes=true;
								}
								// Se consulta cambios de estado Pendientes
								var filterSol = new Array();
								filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'CLIENTE-ESTADO' },
										  	   { field: "IdEntidadOrigen", operator: "eq", value: pcdg.IdCliente},
											   { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
								var SolicitudDeCambioEstadoDatasource = new bcs.DataSource({
									type: "odata",
									definition : {
										serverFiltering:true,
										transport: {
											read: {
												url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
												dataType: "json"
										    }
										},
										filter: filterSol,
										schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
									}
								});
								SolicitudDeCambioEstadoDatasource.query().then(function(result){
									if(SolicitudDeCambioEstadoDatasource.data().length > 0){
										pcdg.dataSolicitudCambio.CambioEstadoPendiente=true;
									}
								});
							});
						});
						pcdg.dataCliente.IdTipoCliente = pcdg.dataClienteOriginal.IdTipoCliente;
						
						if (pcdg.dataClienteOriginal.EsClienteFinal === 'S')
							v_EsClienteFinal = true;
						else
							v_EsClienteFinal = false;
						
						pcdg.dataCliente.EsClienteFinal = v_EsClienteFinal;
						
						asignarValoresPorDefecto();
						//bcsComunes.ventanaOverlay(pcdg.ventanaId,false);
					}
				});
			});
		};
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		 pcdg.LogCambioEjecutivoOptions = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3566",
										dataType: "json"
									  }
						},						
					sort 			: { field: "FechaRegistro", dir: "desc" },
					schema 			: {
						model	: { fields	: { 
							IdCliente 				: { type: "number"},
							FechaRegistro			: { type: "datetime"},
							CodigoUsuario			: { type: "string"},
							ValorAntiguo 			: { type: "string"},
							ValorNuevo   			: { type: "string"},
							NombreCompletoAntiguo	: { type: "string"},
							CodigoEmpleadoAntiguo 	: { type: "string"},
							NombreCompletoNuevo   	: { type: "string"},
							CodigoEmpleadoNuevo   	: { type: "string"},
							}
						}
					},
				}
			}),
			columns	: [{
					field: "CodigoUsuario",
					title: $translate.instant('UI_USUARIO'),
					attributes: {style:"text-align:left;"},
					width: "60px"
				},{
					field: "CodigoEmpleadoAntiguo",
					title: $translate.instant('UI_VALOR_ANTIGUO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "CodigoEmpleadoNuevo",
					title: $translate.instant('UI_VALOR_NUEVO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "FechaRegistro",
					title: $translate.instant('UI_FECHA_REGISTRO'),
					attributes: {style:"text-align:left;"},
					format: "{0:d}",
					width: "100px"
				}],		
			sortable			: false,
			groupable			: false,
			pageable			: false,
			autoBind			: false,  //comentar
			height				: 140,
		    selectable 			: "row"
		 };
		
		pcdg.pantalla = {};
		pcdg.pantalla.modoEdicion = false;
		pcdg.pantalla.deshabilitado = false;

		pcdg.oConfiguracionEjecutivo={
			filtros : {
				IdEmpresa		: {field : "IdEmpresa",		operator : "eq", value : $scope.$root.SesionIdEmpresa},
				IdEstado 		: {field : "IdEstado", operator : "eq", value : "ACT"}
			}
		};

		pcdg.oConfiguracionSucursalPreferencia={
			filtros : {
				IdEmpresa	: {field : "IdEmpresa", operator : "eq", value : $scope.$root.SesionIdEmpresa}
			}
		};

		pcdg.oConfiguracionTipoCliente = {
			filtros : {
				IdEmpresa	: {field : "IdEmpresa", operator : "eq", value : $scope.$root.SesionIdEmpresa},
				IdEstado : {field : "IdEstado", operator : "eq", value : "ACT"}
			}
		};
		
		/******************************************************
		 * Funciones
		 * ***************************************************/		
		var evaluaBroker = function(){
			if(!($scope.SesionLocale == 'PE' && pcdg.dataCliente.Tipo === 'J' 
				&& pcdg.dataCliente.IdTipoCliente === 'INSTITUCIONAL')){
				pcdg.dataCliente.IdPersonaBrokerExtranjero = null;
			}
		}
		
		pcdg.editarpcdg = function() {
			pcdg.pantalla.modoEdicion = true;
		};
		
		pcdg.guardarpcdg = function() {
			evaluaBroker();
			//caso ejecutivo, se está guardando idPersonaEjecutivo y combo usa idEmpleadoEjecutivo
			if(pcdg.EjecutivoObj&&pcdg.EjecutivoObj.IdPersonaEjecutivo) {
				pcdg.dataCliente.IdPersonaEjecutivo = pcdg.EjecutivoObj.IdPersonaEjecutivo;
			}
			else if (pcdg.EjecutivoDefectoObj && pcdg.EjecutivoDefectoObj.IdPersonaEjecutivo) {
				pcdg.dataCliente.IdPersonaEjecutivo = pcdg.EjecutivoDefectoObj.IdPersonaEjecutivo;
			}

			var campos = [{ nombre: "IdTipoCliente",tipo : "combo"},
			              { nombre: "IdCategoriaInversionista",tipo : "combo"},
			              { nombre: "IdSegmentoCliente" ,tipo : "combo", valorBlanco: null},
			              { nombre: "IdPersonaEjecutivo",tipo : "combo", valorBlanco: null},
			              { nombre: "CodigoEjecutivoExterno" ,tipo : "texto"},
			              { nombre: "IdTipoBloqueo",tipo : "combo"},
			              { nombre: "FechaBloqueo",tipo : "fecha"},
			              { nombre: "ComentarioBloqueo",tipo : "texto"},
			              { nombre: "IdSucursalPreferencia",tipo : "combo", valorBlanco: null},
			              { nombre: "IdIntermediario",tipo : "combo"},
			              { nombre: "CuentaPershing",tipo : "texto"},
						  { nombre: "TipoEnrolamiento",tipo: "combo"}];
			
			if($scope.SesionLocale == 'PE'){
				campos.push({ nombre: "CodigoAdicionalHolding",tipo : "texto"});
				campos.push({ nombre: "IdClaseCliente",tipo : "combo"});
				campos.push({ nombre: "EsClienteFinal",tipo : "bool"});
				if(pcdg.dataCliente.Tipo === 'J'&& pcdg.dataCliente.IdTipoCliente === 'INSTITUCIONAL'){
					campos.push({ nombre: "IdPersonaBrokerExtranjero",tipo : "combo"});
				}
			}

			const obtenerValorCampo = function (campo) {
				switch (campo.tipo) {
					case 'texto': {
						return pcdg.dataCliente[campo.nombre] || (campo.hasOwnProperty('valorBlanco') ? campo.valorBlanco : '');
					}
					case 'combo': {
						return pcdg.dataCliente[campo.nombre] || (campo.hasOwnProperty('valorBlanco') ? campo.valorBlanco : '');
					}
					case 'fecha': {
						return pcdg.dataCliente[campo.nombre] || '0001-01-01';
					}
					case 'bool': {
						return pcdg.dataCliente[campo.nombre] ? 'S' : 'N';
					}
					default: return '';
				}
			};

			var registroCambio = [];
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (pcdg.dataCliente[value.nombre] !== pcdg.dataClienteOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}
				if (value.tipo === 'combo') {
					if (!((pcdg.dataCliente[value.nombre] === false || pcdg.dataCliente[value.nombre] === undefined) && (pcdg.dataClienteOriginal[value.nombre] === null || pcdg.dataClienteOriginal[value.nombre] == 0)) && (pcdg.dataCliente[value.nombre] != pcdg.dataClienteOriginal[value.nombre]))
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}
				if (value.tipo === 'fecha') {
					if (pcdg.dataCliente[value.nombre] !== pcdg.dataClienteOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}
				if (value.tipo === 'bool') {
					if (pcdg.dataCliente[value.nombre] !== pcdg.dataClienteOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}			
			});
			
			if (registroCambio.length === 0){
				pcdg.pantalla.modoEdicion = false;
				bcsComunes.msg("warning","info",$translate.instant('UI_SIN_MODIFICACIONES_POR_GUARDAR'));
				return;
			}
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_IdCliente : pcdg.dataCliente.IdCliente,
				p_IdCanal: 'OPT',
				p_DesdeEnrolamiento : false,
				p_RegistroCambio : registroCambio,
			};
			
			bcsComunes.ventanaOverlay(pcdg.ventanaId,true);
			servicioSociodeNegocio.modificarClienteDatosGenerales(datos).$promise.then(
				function(result) {
					bcsComunes.ventanaOverlay(pcdg.ventanaId,false);
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_DATOS_MODIFICADOS'),
						conflictos : util.parseServerResponse(result)}, pcdg.ventanaId);

					if (result.P_OUT_REQUISITOS_ENR_FULL.length > 0) {
						bcsComunes.ventanaAbrir('ventanaGrillaRequisitosEnrolamientoFull', { validaciones: result.P_OUT_REQUISITOS_ENR_FULL });
					}
					
					cargarDatosMaestropcdg();
					pcdg.pantalla.modoEdicion = false;
					$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClienteDatosGenerales' });

				},
				function(result){
					bcsComunes.ventanaOverlay(pcdg.ventanaId,false);
					cargarDatosMaestropcdg();
					pcdg.pantalla.modoEdicion = false;
					bcsComunes.msg("error","Error",$translate.instant('UI_OPERACION_TIENE_ERRORES'));
				});
		};
		
		pcdg.cancelarpcdg = function() {
			pcdg.IdSocNegPersonaEjecutivo = false;
			$timeout(function() {
				pcdg.pantalla.modoEdicion = false;
				pcdg.dataCliente = $.extend({},pcdg.dataClienteOriginal);
				pcdg.IdSocNegPersonaEjecutivo = Number(pcdg.dataClienteOriginal.IdPersonaEjecutivo);
			});
		};

		pcdg.mostrarCambios = function() {
			var ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : pcdg.dataSolicitudCambio},false,false,pcdg.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
		};
		
		pcdg.bloquearCliente = function() {
			var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pcdg.IdCliente, Entidad: 'CLIENTE',
				IdEstado:(pcdg.dataCliente.IdEstado==='ACT'?'BLO':'ACT')},false,false,pcdg.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			promesa.then(function(data){
				cargarDatosMaestropcdg();				
			}); 
			
		};
		
		pcdg.desbloquearCliente = function() {
			var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pcdg.IdCliente, Entidad: 'CLIENTE',
				IdEstado:(pcdg.dataCliente.IdEstado==='ACT'?'BLO':'ACT')},false,false,pcdg.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			promesa.then(function(data){
				cargarDatosMaestropcdg();				
			}); 
		};
			
		var crearEventoAutorizacionSP = function(v_Dato){
			var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=130";
			var r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
			var post = r.urlConsulta(v_Dato);
			return post.$promise;
	    };
	    
	    pcdg.solicitarEventoAutorizacion = function() {
	    	var parametrosSP = {};
	    	var eventoFila = {};
			var evento='ENROLC';
			var modulo='SOC';
			
	        parametrosSP.P_IDEMPRESA 				= $scope.$root.SesionIdEmpresa;
	        parametrosSP.P_IDUSUARIO 				= $scope.$root.SesionIdUsuario;
	        parametrosSP.P_IDMODULO 				= modulo;
	        parametrosSP.P_SIMULAROPERACION		= "N";
	        parametrosSP.P_USAREGLASNEGOCIO 		= "N";
	        parametrosSP.P_TABLAEVENTOS 			= [];
	        parametrosSP.P_DETALLE 				= [];

	        eventoFila.IdTipoEvento 				= 'ENROL';
	        eventoFila.IdModuloSolicita 			= modulo;
	        eventoFila.IdUsuarioSolicita 		= $scope.$root.SesionIdUsuario;
	        eventoFila.Fecha 					= pcdg.kngFechaSistema;
	        eventoFila.IdConcepto 				= evento;
	        eventoFila.NumeroOperacionConcepto	= String(pcdg.IdCliente);
	        eventoFila.FechaOperacionConcepto	= pcdg.kngFechaSistema;
	        eventoFila.IdCliente 				= pcdg.IdCliente;
	        eventoFila.Glosa 					= pcdg.dataCliente.DocumentoIdentidad;

	        parametrosSP.P_TABLAEVENTOS.push(eventoFila);
			
	        crearEventoAutorizacionSP(parametrosSP).then(
	    		function(result) {
					if (result.P_OUT_ERRORESCONTADOR && Number(result.P_OUT_ERRORESCONTADOR) > 0){
						bcsComunes.ventanaMsg({ mensaje : "La operacion tiene errores",
							conflictos : util.parseServerResponse(result)}, pcdg.ventanaId);
					}else if (result.P_OUT_ADVERTENCIASCONTADOR && Number(result.P_OUT_ADVERTENCIASCONTADOR) > 0){
						bcsComunes.ventanaMsg({ mensaje : "La operacion tiene advertencias",
							conflictos : util.parseServerResponse(result)}, pcdg.ventanaId);
					}else{
						bcsComunes.msg("info","info","Solicitud de autorizacion enviada");
						cargarDatosMaestropcdg();
					}
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
		};
		
		var successFecha = function(result){
			if (result.P_OUT_ERRORESCONTADOR == "0"){
				pcdg.kngFechaSistema = util.dateSPdate(util.date(result.P_OUT_FECHAHORASISTEMA).toDate());
			}else{
				pcdg.kngFechaSistema = '0001-01-01';
			}
		};
		var errorFecha = function (result) { };
		
		var seteoFechaSistema = function(){
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'SOC';
			datos.p_UsaReglasNegocio = 'S';		
			bcsServicioPlataforma.FechaSistema(datos,successFecha,errorFecha);
		};	
		
		seteoFechaSistema();	
		
		/******************************************************
		 * WATCHERS
		 * ***************************************************/
		
		/******************************************************
		 * GO
		 * ***************************************************/
		
		cargarDatosMaestropcdg();
		
		pcdg.obtenerDatosComparables = function (data) {
			return {
				TipoEnrolamiento: data.TipoEnrolamiento || '',
				IdTipoCliente: data.IdTipoCliente || '',
				IdCategoriaInversionista: data.IdCategoriaInversionista || '',
				//IdSegmentoCliente: data.IdSegmentoCliente || 0,
				IdClaseCliente: data.IdClaseCliente || '',
				IdPersonaEjecutivo: data.IdPersonaEjecutivo || 0,
				CodigoEjecutivoExterno: data.CodigoEjecutivoExterno || '',
				IdPersonaBrokerExtranjero: data.IdPersonaBrokerExtranjero || 0,
				IdSucursalPreferencia: data.IdSucursalPreferencia || 0,
				CodigoAdicionalHolding: data.CodigoAdicionalHolding || '',
				IdTipoBloqueo: data.IdTipoBloqueo || '',
				FechaBloqueo: data.FechaBloqueo || '0001-01-01 00:00:00',
				EsClienteFinal: (data.EsClienteFinal && data.EsClienteFinal==='S')?'S':'N',
				ComentarioBloqueo: data.ComentarioBloqueo || '',
			};
		};

		pcdg.avisarCambiosOk = function() {
			$scope.$emit('DatosCambiadosGuardados', { ambito: "Cliente", nombreTab: 'PanelClienteDatosGenerales' });	
		}
		pcdg.avisarCambioPanel = function () {
			const dataOriginal = pcdg.obtenerDatosComparables(pcdg.dataClienteOriginal);
			const data = pcdg.obtenerDatosComparables(pcdg.dataCliente);
			const sonIguales = _.isEqual(data, dataOriginal);

			if (sonIguales) {
				pcdg.avisarCambiosOk();
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Cliente", nombreTab: 'PanelClienteDatosGenerales' });
			}
		};

		$scope.$watchCollection('pcdg.dataCliente', function () {
			if (pcdg.pantalla.modoEdicion) {
				pcdg.avisarCambioPanel();
			}
			bcsComunes.ventanaAutoHeight(idVentana);
		});

	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
