/*************************************************************
 * Purpose	: Panel Empleado Datos Generales
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelEmpleadoDatosGeneralesCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			$resource,
			bcsConfiguracionSociodeNegocio,bcsServicioPlataforma){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/	
		var pedg = this;
		pedg.idVentana = bcsComunes.ventanaWinId($scope);
		pedg.dataEmpleado = {};
		pedg.oConfiguracionDireccionElectronica = {};
		pedg.dataSolicitudCambio= {};
		pedg.dataSolicitudCambio.CambiosPendientes=false;	
		pedg.dataSolicitudCambio.CambioEstadoPendiente=false;
		pedg.maximoCodigoEmpleado = 0;

		bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('EMP.MaximoCodigoEmpleado', $scope.SesionIdEmpresa, 'SOCIONEGOCIO')
		.then(function (parametroMaximoCodigoEmpleado) {
			pedg.maximoCodigoEmpleado = parametroMaximoCodigoEmpleado.Valor ? Number(parametroMaximoCodigoEmpleado.Valor) : 40;
		});
		
		var idVentana = bcsComunes.ventanaWinId($scope)
		var cargarDatosMaestropedg = function() {
			bcsComunes.ventanaOverlay(pedg.idVentana,true);
			$scope.admSoc.socioNegocioPromise.then(function(){
				pedg.IdEmpleado = ($scope.admSoc.dataSocioNegocio.IdEmpleado?$scope.admSoc.dataSocioNegocio.IdEmpleado:0);
				
				var EmpleadoDatosGeneralesDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=13017",
								dataType: "json"
						    }
						},
						filter: { field: "IdEmpleado", operator: "eq", value: Number(pedg.IdEmpleado) },
						schema: bcsConfiguracionSociodeNegocio.getSchemaEmpleado()
					}
				});
				
				EmpleadoDatosGeneralesDatasource.query().then(function(result){
					console.log(EmpleadoDatosGeneralesDatasource.data());
					if (EmpleadoDatosGeneralesDatasource.data().length != 1)
					{
						bcsComunes.ventanaOverlay(pedg.idVentana,false);
					}
					else
					{
						pedg.dataEmpleado = $.extend({},EmpleadoDatosGeneralesDatasource.data()[0]);						
						pedg.dataEmpleado.UtilizaPlantillaEmpleado = (pedg.dataEmpleado.UtilizaPlantillaEmpleado=='S') ? true : false;
						pedg.dataEmpleado.RestringeCarteraCliente = (pedg.dataEmpleado.RestringeCarteraCliente=='S' && (pedg.dataEmpleado.EsEjecutivo==='S' || pedg.dataEmpleado.EsSupervisor==='S')) ? true : false;
						pedg.dataEmpleado.RestringeOrdenCliente = (pedg.dataEmpleado.RestringeOrdenCliente=='S' && (pedg.dataEmpleado.EsEjecutivo==='S' || pedg.dataEmpleado.EsSupervisor==='S')) ? true : false;
						pedg.dataEmpleado.FechaInicioCargo = util.date(pedg.dataEmpleado.FechaInicioCargo).toDate();
						pedg.dataEmpleado.FechaTerminoCargo = util.date(pedg.dataEmpleado.FechaTerminoCargo).toDate();
						pedg.dataEmpleado.CargoIndefinido = (pedg.dataEmpleado.CargoIndefinido=='S') ? true : false;
						pedg.dataEmpleadoOriginal = $.extend({},pedg.dataEmpleado);
						
						bcsComunes.ventanaOverlay(pedg.idVentana,false);
						
						// Se consultan solicitudes de cambios Pendientes
							var filterSolDatos = new Array();
							filterSolDatos.push({ field: "IdTipoEntidad", operator: "eq", value: 'EMPLEADO' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: pedg.IdEmpleado},
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
									filter: filterSolDatos,
									schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
								}
							});							
							
							//Se consulta si el evento está Ingresado
							var filterEvtDatos = new Array();
							filterEvtDatos.push(
							    { field: "IdTipoEvento", operator: "eq", value: 'MODEMP' },
							    { field: "IdCliente", operator: "eq", value: pedg.IdEmpleado },
							    { field: "IdEstado", operator: "eq", value: 'VIG' }
							);
							
							var EventoDataSource = new bcs.DataSource({
							    type: "odata",
							    definition: {
							        serverFiltering: true,
							        transport: {
							            read: {
							                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1396", //AUT.ConsultarEvento
							                dataType: "json"
							            }
							        },
							        filter: filterEvtDatos,
							        schema: bcsConfiguracionSociodeNegocio.getSchemaAutorizacionEvento()
							    }
							});														
								
							// Se consulta cambio de estado Pendiente
							var filterSolEstado = new Array();
							filterSolEstado.push({ field: "IdTipoEntidad", operator: "eq", value: 'EMPLEADO-ESTADO' },
									  	   { field: "IdEntidadOrigen", operator: "eq", value: pedg.IdEmpleado},
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
									filter: filterSolEstado,
									schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
								}
							});
							
							//Se consulta si el evento está Ingresado
							var filterEvtCambioEstado = new Array();
							filterEvtCambioEstado.push(
							    { field: "IdTipoEvento", operator: "eq", value: 'CAEEMP' },
							    { field: "IdCliente", operator: "eq", value: pedg.IdEmpleado },
							    { field: "IdEstado", operator: "eq", value: 'VIG' }
							);
							
							var EventoCambioEstadoDataSource = new bcs.DataSource({
							    type: "odata",
							    definition: {
							        serverFiltering: true,
							        transport: {
							            read: {
							                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1396", //AUT.ConsultarEvento
							                dataType: "json"
							            }
							        },
							        filter: filterEvtCambioEstado,
							        schema: bcsConfiguracionSociodeNegocio.getSchemaAutorizacionEvento()
							    }
							});
							
							pedg.dataSolicitudCambio.CambiosPendientes = false;
							pedg.dataSolicitudCambio.CambioEstadoPendiente = false;		
							
							$.when(
							  SolicitudDeCambioDatasource.query(),
							  EventoDataSource.query(),
							  SolicitudDeCambioEstadoDatasource.query(),
							  EventoCambioEstadoDataSource.query()
							).then(function () {
							  var solDatos  = SolicitudDeCambioDatasource.data() || [];
							  var evtDatos  = EventoDataSource.data() || [];
							  var solEstado = SolicitudDeCambioEstadoDatasource.data() || [];
							  var evtEstado = EventoCambioEstadoDataSource.data() || [];
							
							  var tienePendienteDatos  = (solDatos.length  > 0 && evtDatos.length  > 0);
							  var tienePendienteEstado = (solEstado.length > 0 && evtEstado.length > 0);
							  
							  if (tienePendienteDatos) {
							    $.extend(pedg.dataSolicitudCambio, solDatos);
							    pedg.dataSolicitudCambio.CambiosPendientes = true;
							  }
							  if (tienePendienteEstado) {
							    pedg.dataSolicitudCambio.CambioEstadoPendiente = true;
							  }
						  });																														
					}
				});
			});
		};
		
		pedg.pantalla = {};
		pedg.pantalla.modoEdicion = false;
		pedg.pantalla.deshabilitado = false;
		
		/******************************************************
		 * CONFIGURACIONES
		 * ***************************************************/	
		pedg.oConfiguracionDireccionElectronica = { filtros : {
			IdPersona : {field : "IdPersona", operator : "eq", value : Number(($scope.admSoc.dataSocioNegocio.IdPersona?$scope.admSoc.dataSocioNegocio.IdPersona:0))},
			IdEstado : {field : "IdEstado", operator : "eq", value : 'ACT'},
			}
		};
		
		pedg.oConfiguracionPlantillaEmpleado = { 
			filtros : {
				IdEmpresa : {field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa}
			}
		};

		pedg.confBasicaSucursalAtencion = {
			dataSource			: new bcs.DataSource({
									type 		: "odata", //Los tipos posibles son: odata, call, array y object
									definition 	: {
										serverFiltering 	: true, // Filtrado de datos en el servidor.
										serverPaging 		: true, // Paginado de los datos en el servidor.
										serverSorting 		: true, // Ordenamiento de los datos en el servidor.
										groupable 			: true, 
										sort 				: { field: "IdSucursal", dir: "asc" }, // Criterios de Ordenamiento.
										pageSize 			: 25, // Número de registros por página.
										transport 			: {
																read : {
																	url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=13051",
																	dataType : "json"
																}
															},
										filter: [
											{ field: "IdEmpleado", operator: "eq", value: Number($scope.admSoc.dataSocioNegocio.IdEmpleado) },
											{ field: "Borrado", operator: "eq", value: "N" }
										],
										schema 				: {
											model: {
												fields : {
													IdSucursalAtencion: {type: "number"},
													IdSucursal: {type: "number"},
													NombreSucursal: {type: "string"},
													EstadoSucursal: {type: "string"},
													IdEmpresa: {type: "number"},
													IdEmpleado: {type: "number"},
													Borrado: {type: "string"},
												}
											}
										},			
									}
								}),
			columns				: [
								{field: "IdSucursalAtencion",title: "UI_SUCURSAL",hidden:true},
								{field: "NombreSucursal",title: "UI_SUCURSAL"}
							],
			gridCreated: function(grid) {
				pedg.gridSucursalAtencion = grid;
			},
			sortable			: true,
			pageable			: false,
			filterable			: false,
			groupable           : true,
			autoBind			: true,
			height				: 120,
			userSettings		: false,
			navigatable			: true,
			selectable 			: "row"
		};

		
		pedg.oConfiguracionJefeDirecto = {filtros:{}};

		pedg.oConfiguracionSucursal = {filtros : {IdPais: {"field":"IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa}}};
		
		/******************************************************
		 * Funciones
		 * ***************************************************/		
		
		pedg.editarpedg = function() {
			pedg.pantalla.modoEdicion = true;
			
		};
		
		pedg.guardarpedg = function() {
			var campos = [];
			if(pedg.dataEmpleado.UtilizaPlantillaEmpleado && !pedg.dataEmpleado.IdPlantillaEmpleado) {
				bcsComunes.msg("warning","guardar","Se ha marcado utilizar plantilla sin seleccionar una.");
			}
			else {
				campos = [{ nombre: "CodigoTipoCargoEmpleado" ,tipo : "texto"},
			              { nombre: "IdEmpleadoSupervisor",tipo : "combo"},
			              { nombre: "IdEstado",tipo : "combo"},
			              { nombre: "IdSucursal",tipo : "combo"},
			              { nombre: "UtilizaPlantillaEmpleado",tipo : "bool"},
			              { nombre: "RestringeCarteraCliente",tipo : "bool"},
			              { nombre: "RestringeOrdenCliente",tipo : "bool"},
			              { nombre: "IdPlantillaEmpleado",tipo : "combo"},
			              { nombre: "FechaInicioCargo",tipo : "fecha"},
			              { nombre: "FechaTerminoCargo",tipo : "fecha"},
			              { nombre: "CodigoEmpleado",tipo : "texto"},
			              { nombre: "DetalleCargo",tipo : "texto"},
			              { nombre: "IdDireccionElectronica",tipo : "combo"},
			              { nombre: "CargoIndefinido",tipo : "combo"}];
			}

			if (pedg.dataEmpleado.CodigoEmpleado.length > pedg.maximoCodigoEmpleado) {
				bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant("UI_INGRESE_CODIGO_EMPLEADO_MAXIMO_PERMITIDO").replace("MaximoCodigoEmpleado", pedg.maximoCodigoEmpleado));
				return;
			}
			
			var registroCambio = [];
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (pedg.dataEmpleado[value.nombre] !== pedg.dataEmpleadoOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pedg.dataEmpleado[value.nombre]?pedg.dataEmpleado[value.nombre]:"")});
				}
				if (value.tipo === 'combo') {
					if(value.nombre==="IdPlantillaEmpleado" && !pedg.dataEmpleado.UtilizaPlantillaEmpleado)
						pedg.dataEmpleado[value.nombre] = ""; //considera dependencia caso uso plantilla.
					if(value.nombre==="CargoIndefinido" && pedg.dataEmpleado[value.nombre] !== pedg.dataEmpleadoOriginal[value.nombre]){
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pedg.dataEmpleado[value.nombre]?"S":"N")});		
						if(pedg.dataEmpleado[value.nombre]){
							registroCambio.push({CampoModificado : "FechaTerminoCargo", ValorNuevo : ''});		
						}
					}else{
						if (!((pedg.dataEmpleado[value.nombre] === false || pedg.dataEmpleado[value.nombre] === undefined) && (pedg.dataEmpleadoOriginal[value.nombre] === null || pedg.dataEmpleadoOriginal[value.nombre] == 0)) && (pedg.dataEmpleado[value.nombre] != pedg.dataEmpleadoOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pedg.dataEmpleado[value.nombre]?pedg.dataEmpleado[value.nombre]:"")});
					}		
				}
				if (value.tipo === 'fecha') {
					if (pedg.dataEmpleado[value.nombre] !== pedg.dataEmpleadoOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pedg.dataEmpleado[value.nombre]?pedg.dataEmpleado[value.nombre]:"")});
				}
				if (value.tipo === 'bool') {
					if (pedg.dataEmpleado[value.nombre] !== pedg.dataEmpleadoOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (pedg.dataEmpleado[value.nombre]?"S":"N")});
				}			
			});
			
			
			if (registroCambio.length === 0)
			{
				pedg.pantalla.modoEdicion = false;
				bcsComunes.msg("warning","info","No hay modificaciones para guardar.");
				pedg.cancelarpedg();
				return;
			}
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_IdEmpleado : pedg.IdEmpleado,
				p_RegistroCambio : registroCambio,
			};
			
			bcsComunes.ventanaOverlay(pedg.idVentana,true);
			servicioSociodeNegocio.modificarEmpleadoDatosGenerales(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaMsg({ mensaje : "Los datos fueron modificados",
							conflictos : util.parseServerResponse(result)}, pedg.idVentana);
						
						cargarDatosMaestropedg();
						pedg.pantalla.modoEdicion = false;
					},
					function(result){
						cargarDatosMaestropedg();
						pedg.pantalla.modoEdicion = false;
						bcsComunes.msg("error","Error","La operacion tiene errores");
					});
			
		};
		
		pedg.cancelarpedg = function() {
			pedg.pantalla.modoEdicion = false;
			pedg.dataEmpleado = $.extend({},pedg.dataEmpleadoOriginal);
		};
		
		pedg.mostrarCambios = function() {
			var ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : pedg.dataSolicitudCambio},false,true);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
		};
		
		
		var cambioEstadoEmpleadoSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=103";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
	    
	    pedg.bloquearEmpleado = function() {
			bcsComunes.ventanaOverlay(pedg.idVentana,true);
	    	var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pedg.IdEmpleado, Entidad: 'EMPLEADO',
				IdEstado:(pedg.dataEmpleado.IdEstado==='ACT'?'BLO':'ACT')},false,false);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			$.when(promesa).then(function(){
				bcsComunes.ventanaOverlay(pedg.idVentana,false);
				cargarDatosMaestropedg();
	         }); 
		};
		
		pedg.desbloquearEmpleado = function() {
			bcsComunes.ventanaOverlay(pedg.idVentana,true);
			var ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : pedg.IdEmpleado, Entidad: 'EMPLEADO',
				IdEstado:(pedg.dataEmpleado.IdEstado==='ACT'?'BLO':'ACT')},false,false);
			var promesa = bcsComunes.ventanaPromesa(ventanaId);   
			
			$.when(promesa).then(function(){
				bcsComunes.ventanaOverlay(pedg.idVentana,false);
				cargarDatosMaestropedg();
	         }); 	
		};

		pedg.agregarSucursalAtencion = function (){
			var id1 = bcsComunes.ventanaAbrir("ModalSucursalAtencion",{IdEmpleado : pedg.IdEmpleado},false,false);

			var promesa=bcsComunes.ventanaPromesa(id1);                               
			promesa.then(function(data){ 
				//Refrescar grilla para visualizar cambios.
				pedg.gridSucursalAtencion.dataSource.read();
			}); 
		};
		pedg.eliminarSucursalAtencion = function(){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
			parametrosSP.p_IdUsuario =  $scope.SesionIdUsuario;
			parametrosSP.p_IdModulo = 'SOC';        

			parametrosSP.p_IdSucursalAtencion = pedg.sucursalAtencion.IdSucursalAtencion;
			parametrosSP.p_IdEmpleado = pedg.sucursalAtencion.IdEmpleado;
			var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20037";
			var r = $resource("", {}, {'url': { method: "POST", url: url}});
			var post = r.url(parametrosSP);
			bcsComunes.ventanaOverlay(pedg.idVentana,true);	
			post.$promise.then(function(result){
				var titulo = "UI_PUNTO_ATENCION_ELIMINADO";
				if (result.P_OUT_ERRORESCONTADOR.length>0){
					titulo = "La operacion tiene errores";
				}
				var idModal = bcsComunes.ventanaMsg({ mensaje : $translate.instant(titulo),
								conflictos : util.parseServerResponse(result)}, pedg.idVentana);

				bcsComunes.ventanaPromesa(idModal).then(function(){
					bcsComunes.ventanaOverlay(pedg.idVentana,false);	
					pedg.gridSucursalAtencion.dataSource.read();
				});
			}, function(){
				bcsComunes.msg("error","Error","La operacion tiene errores");
				bcsComunes.ventanaOverlay(ventanaId,false);	
			});

		};
		
		
		var crearEventoAutorizacionSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=130";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
		
		 pedg.solicitarEventoAutorizacion = function() {
			
			var parametrosSP = {};
			var eventoFila = {};
			var evento='ENROLE';
			var modulo='SOC';
			
	        parametrosSP.P_IDEMPRESA 				= $scope.$root.SesionIdEmpresa;
	        parametrosSP.P_IDUSUARIO 				= $scope.$root.SesionIdUsuario;
	        parametrosSP.P_IDMODULO 				= modulo;
	        parametrosSP.P_SIMULAROPERACION		= "N";
	        parametrosSP.P_USAREGLASNEGOCIO 		= "N";
	        parametrosSP.P_TABLAEVENTOS 			= [];
	        parametrosSP.P_DETALLE 				= [];

	        eventoFila.IdTipoEvento 				= evento;
	        eventoFila.IdModuloSolicita 			= modulo;
	        eventoFila.IdUsuarioSolicita 		= $scope.$root.SesionIdUsuario;
	        eventoFila.Fecha 					= pedg.kngFechaSistema;
	        eventoFila.IdConcepto 				= evento;
	        eventoFila.NumeroOperacionConcepto	= String(pedg.IdEmpleado);
	        eventoFila.FechaOperacionConcepto	= pedg.kngFechaSistema;
	        eventoFila.IdFuncionario 			= pedg.IdEmpleado;
	        eventoFila.IdCliente 				= pedg.IdEmpleado;
	        eventoFila.Glosa 					= pedg.dataEmpleado.DocumentoIdentidad;

	        parametrosSP.P_TABLAEVENTOS.push(eventoFila);
					      
	        crearEventoAutorizacionSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Solicitud de autorizacion enviada",
						conflictos : util.parseServerResponse(result)}, pedg.idVentana);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
			
		};
		
		//FECHA SISTEMA
		var successFecha = function(result)
		{
			if (result.P_OUT_ERRORESCONTADOR == "0"){
				pedg.kngFechaSistema = util.dateSPdate(util.date(result.P_OUT_FECHAHORASISTEMA).toDate());
			}
			else{
				pedg.kngFechaSistema = '0001-01-01';
	
				console.log("Error." + result.P_OUT_ERRORESCONTADOR);
				var i = 0;
	
				for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
				{
					console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
					console.log("Error2.");
				}
			}
		};
		var errorFecha = function(result)
		{
			console.log(JSON.stringify(result));
			console.log("revisar");
		};
		var seteoFechaSistema = function(){
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'SOC';
			datos.p_UsaReglasNegocio = 'N';		
			bcsServicioPlataforma.FechaSistema(datos,successFecha,errorFecha);
		};	
		
		seteoFechaSistema();
		/******************************************************
		 * GO
		 * ***************************************************/
		cargarDatosMaestropedg();
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------