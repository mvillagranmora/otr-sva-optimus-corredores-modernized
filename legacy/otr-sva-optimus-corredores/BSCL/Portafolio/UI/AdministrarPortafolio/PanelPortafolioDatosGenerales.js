/********************************************************************
 * Purpose	: Controlador para administrar Portafolios de un cliente
 * Author	: JGarces - BCS
 * Date		: 25/11/2015
 *******************************************************************/

(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioDatosGeneralesCtrl',
	function($scope, $translate, servicioPortafolio, servicioSociodeNegocio,bcsComunes, bcsConfiguracionPortafolio, $resource, bcsServicioPlataforma){

		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pPortDG = this; 
		pPortDG.datosAntes = {};
		pPortDG.datosDespues = {};
		pPortDG.guardarHabilitado = true;
		pPortDG.cmbbx = {};
		pPortDG.fn = {};
		pPortDG.win = {};				
		pPortDG.win.modoLectura = true;
		pPortDG.dataSolicitudCambio= {};
		pPortDG.dataSolicitudCambio.CambiosPendientes=false;
		pPortDG.dataSolicitudCambio.CambioEstadoPendiente=false;
		pPortDG.idVentana = bcsComunes.ventanaWinId($scope);
		pPortDG.stringVacio = '';
		var parametrosVentana = bcsComunes.ventanaParams(pPortDG.idVentana);
		pPortDG.TipoPortafolio =	parametrosVentana.TipoPortafolio;
		pPortDG.kOptFechaUltimaCartola = {};
		bcsComunes.ventanaAutoOverlay(pPortDG.idVentana,'telefonoContactoPortafolio input',3000);
		pPortDG.camposRequeridos = {};
		$scope.admPort.dataSocioNegocio = {};
		var requestBody = {};
		if (parametrosVentana.dataSocioNegocio === undefined){
			 requestBody = {
						p_TipoEnrolamiento: parametrosVentana.TipoEnrolamiento || '',
						p_TipoPersona: parametrosVentana.TipoPersona,
					};
			
		}else{
			 requestBody = {
				p_TipoEnrolamiento: parametrosVentana.dataSocioNegocio.TipoEnrolamiento || '',
				p_TipoPersona: parametrosVentana.dataSocioNegocio.CodigoTipoPersona,
			};
		}
		servicioSociodeNegocio.consultarFormularioSocioNegocio(requestBody).$promise.then(function(result){
			//cuando llamen desde socio de negocio no va a entrar a este IF, solo cuando se llame con la flecha del combobox cliente portafolio
			if (result.p_FormulariosSocioNegocio && Array.isArray(result.p_FormulariosSocioNegocio)) {
				$scope.admPort.dataSocioNegocio.Matriz = result.p_FormulariosSocioNegocio.reduce(function (acum, current) {
					acum[current.Formulario] = current.CodigoFormulario;

					return acum;
				}, {});
			}

			
		}).then(function(result){
			pPortDG.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['DATOS_GENERALES_PORTAFOLIO_CLIENTE'];
	
			bcsServicioPlataforma.CamposRequeridosFormulario(pPortDG.CodigoFormulario, function(respuesta) {
				pPortDG.camposRequeridos = respuesta;
			pPortDG.fn.asignarValoresPorDefecto();
			pPortDG.fn.cargarFormulario();
			});
			
			$scope.admPort.pPortDG = pPortDG;
		})
		
		
		
/*********************************************
 * MODALIDADES DE PANTALLA
 ********************************************/
		pPortDG.win.ModoEdicion = function(){
			pPortDG.win.modoLectura = false;
		};

		pPortDG.win.ModoLectura = function(){
			pPortDG.win.modoLectura = true;
		};

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
		pPortDG.win.editar = function(){
			pPortDG.win.ModoEdicion();
		};

		pPortDG.win.guardar = function(){
			if ( pPortDG.datosDespues.CodigoPortafolio.length > 0 ) {
				bcsComunes.ventanaOverlay(pPortDG.idVentana,true);
				var dataPortafolio = pPortDG.fn.preparaDatosGuardar();
				servicioPortafolio.updatePortafolio(dataPortafolio, function (result) {
					var titulo = "";
					if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0){
						titulo = "Portafolio modificado";
						pPortDG.win.ModoLectura();
						pPortDG.datosAntes = _.clone(pPortDG.datosDespues); //homologar datos antes/despues.
						pPortDG.emitirDatosGuardados();
					}
					else {
						if (result.P_OUT_ADVERTENCIASCONTADOR){
								titulo = "La operacion tiene advertencias";
								pPortDG.guardarHabilitado = false;
								pPortDG.datosDespues = _.clone(pPortDG.datosAntes); 
								pPortDG.win.modoLectura = true;
								
						}else{
								titulo = "La operacion tiene errores";
						}
					}
					pPortDG.LogCambioEjecutivoOptions.dataSource.filter([{ field: "IdPortafolio", operator: "eq", value: Number($scope.admPort.datos.IdPortafolio)}]);
					bcsComunes.ventanaMsg({
						mensaje: titulo,
						conflictos:util.parseServerResponse(result)
					},pPortDG.idVentana);
					bcsComunes.ventanaOverlay(pPortDG.idVentana,false);
				},
				function(error){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					console.log(error);
					bcsComunes.ventanaOverlay(pPortDG.idVentana,false);
				});
			}
			else {
				bcsComunes.msg("error","MODIFICAR PORTAFOLIO",'Codigo vacío');
			}
		};

		pPortDG.win.cancelar = function(){
			pPortDG.datosDespues = _.clone(pPortDG.datosAntes);
			pPortDG.win.ModoLectura();
			pPortDG.emitirDatosGuardados();
		};
		
		pPortDG.mostrarCambios = function() {
			var ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : pPortDG.dataSolicitudCambio},false,true);
			bcsComunes.ventanaPromesa(ventanaId);   
		};
		
		var crearEventoAutorizacionSP = function(v_Dato){
			var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=130";
			var r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
			var post = r.urlConsulta(v_Dato);
			return post.$promise;
		};
		
		pPortDG.win.solicitarEventoAutorizacion = function() {
			bcsComunes.ventanaOverlay(pPortDG.idVentana,true);
			var parametrosSP = {};
			var eventoFila = {};
			var evento='NUEPOR';
			var modulo='SOC';
	        var sesionIdEmpresa = ($scope.$root && $scope.$root.SesionIdEmpresa) || $scope.SesionIdEmpresa || (typeof BCS_SESION !== 'undefined' ? BCS_SESION.IdEmpresa : null);
	        var sesionIdUsuario = ($scope.$root && $scope.$root.SesionIdUsuario) || $scope.SesionIdUsuario || (typeof BCS_SESION !== 'undefined' ? BCS_SESION.IdUsuario : null);
	        var sesionFechaSistemaRaw = ($scope.$root && $scope.$root.SesionFechaSistema) || $scope.SesionFechaSistema || (typeof BCS_SESION !== 'undefined' ? BCS_SESION.FechaSistema : null);
	        var sesionFechaSistema = (sesionFechaSistemaRaw && sesionFechaSistemaRaw.length >= 10) ? sesionFechaSistemaRaw.substring(0,10) : sesionFechaSistemaRaw;

	        parametrosSP.P_IDEMPRESA 				= Number(sesionIdEmpresa);
	        parametrosSP.P_IDUSUARIO 				= Number(sesionIdUsuario);
	        parametrosSP.P_IDMODULO 				= modulo;
	        parametrosSP.P_SIMULAROPERACION			= "N";
	        parametrosSP.P_USAREGLASNEGOCIO 			= "N";
	        parametrosSP.P_TABLAEVENTOS 			= [];
	        parametrosSP.P_DETALLE 				= [];

	        eventoFila.IdTipoEvento 				= evento;
	        eventoFila.IdModuloSolicita 			= modulo;
	        eventoFila.IdUsuarioSolicita 			= sesionIdUsuario;
	        eventoFila.Fecha 					= sesionFechaSistema;
	        eventoFila.IdConcepto 				= evento;
			eventoFila.NumeroOperacionConcepto	= String(pPortDG.datosAntes.IdPortafolio);
			eventoFila.FechaOperacionConcepto	= sesionFechaSistema;
			eventoFila.IdCliente				= pPortDG.datosAntes.IdCliente;
			eventoFila.Glosa					= pPortDG.datosDespues.DocumentoIdentidad + ' / ' + pPortDG.datosDespues.NombreCompleto + ' / CodPort: ' + pPortDG.datosDespues.CodigoPortafolio;

	        parametrosSP.P_TABLAEVENTOS.push(eventoFila);

	        crearEventoAutorizacionSP(parametrosSP).then(
				function(result) {
					if (result.P_OUT_ERRORESCONTADOR && Number(result.P_OUT_ERRORESCONTADOR) > 0){
						bcsComunes.ventanaMsg({ mensaje: "La operacion tiene errores",
							conflictos: util.parseServerResponse(result)}, pPortDG.idVentana);
					}else if (result.P_OUT_ADVERTENCIASCONTADOR && Number(result.P_OUT_ADVERTENCIASCONTADOR) > 0){
						bcsComunes.ventanaMsg({ mensaje: "La operacion tiene advertencias",
							conflictos: util.parseServerResponse(result)}, pPortDG.idVentana);
					}else{
						bcsComunes.msg("info","info","Solicitud de autorizacion enviada");
						//cargarDatosMaestroPPDG();
					}
					bcsComunes.ventanaOverlay(pPortDG.idVentana,false);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(pPortDG.idVentana,false);
				}
			);
		};

/*********************************************
 * DATASOURCES
 *********************************************/
	
		var obtieneEstadoSolicitudesCambio = function(idPortafolio){
			// Se consultan solicitudes de cambios Pendientes		
			var filterSol = new Array();
			filterSol.push( { field: "IdTipoEntidad", operator: "eq", value: 'PORTAFOLIO' },
							{ field: "IdEntidadOrigen", operator: "eq", value: String(idPortafolio)},
					  	  	{ field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
			var SolicitudDeCambioDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					serverSorting:true,
					transport: {
					read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
							dataType: "json"
						  }
						},
					filter: filterSol,
					schema: bcsConfiguracionPortafolio.getSchemaSolicitudDeCambio()
				}
			});	
				
			SolicitudDeCambioDatasource.read().then(function(){
				if(SolicitudDeCambioDatasource.data().length > 0){
						pPortDG.dataSolicitudCambio = $.extend({},SolicitudDeCambioDatasource.data());
						pPortDG.dataSolicitudCambio.CambiosPendientes=true;
				}
			});	

			// Se consulta cambio de estado Pendiente		
			var filterSolEstado = new Array();
			filterSolEstado.push( { field: "IdTipoEntidad", operator: "eq", value: 'PORTAFOLIO-ESTADO' },
								  { field: "IdEntidadOrigen", operator: "eq", value: String(idPortafolio)},
					  	  		  { field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN'});
			var SolicitudDeCambioEstadoDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					serverSorting:true,
					transport: {
					read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
							dataType: "json"
					  	}
					},
					filter: filterSolEstado,
					schema: bcsConfiguracionPortafolio.getSchemaSolicitudDeCambio()
				}
			});	
			
			SolicitudDeCambioEstadoDatasource.read().then(function(){
				if(SolicitudDeCambioEstadoDatasource.data().length > 0){
					pPortDG.dataSolicitudCambio.CambioEstadoPendiente=true;
				}
			});	
		};
		
/*********************************************
 * COMBOBOX
 *********************************************/
		pPortDG.cmbbx.oConfiguracionPropositoPortafolio = {
			configuracion : {},
			filtros : {},
			seleccionado : false,
			idActual : false,
			idDefecto : ''
		};
		
		pPortDG.cmbbx.direccionEnvioCorrespondencia = { 
			filtros : {  
				IdPersona 	: {field: "IdPersona", 	operator: "eq", value: -1 },
				IdEstado 	: {field: "IdEstado", 	operator: "eq", value: 'ACT' } 
			} 
		};
		
		pPortDG.cmbbx.telefonoContacto = { 
			filtros : {  
				IdPersona 	: {field: "IdPersona", operator: "eq",  value: -1 },
				IdEstado 	: {field: "IdEstado", 	operator: "eq", value: 'ACT' } 
			} 
		};
		
		pPortDG.cmbbx.ejecutivo = { 
			filtros : {  
				IdEmpresa 	: {field: "IdEmpresa", 	operator: "eq", value: ($scope.$root == null? $scope.SesionIdEmpresa: $scope.$root.SesionIdEmpresa) }
			} 
		};
		
		pPortDG.cmbbx.tipoBloqueo = { 
				filtros : {  
					IdEmpresa 	: {field: "IdEmpresa", 	operator: "eq", value: ($scope.$root == null? $scope.SesionIdEmpresa: $scope.$root.SesionIdEmpresa)  },
					IdPersona 	: {field: "IdPersona", operator: "eq", value: -1 },
					IdEstado 	: {field: "IdEstado", 	operator: "eq", value: 'ACT' } 
				} 
			};
		
		pPortDG.cmbbx.nombreFantasia = { 
			filtros : {  
				IdPersona : {field: "IdPersona", 	operator: "eq", value: Number(parametrosVentana.dataSocioNegocio.IdPersona) },
				IdEstado:{field:"IdEstado",operator:"eq",value:'ACT'}
			} 
		};
		
		var filtrosProposito={IdEstado:{field:"IdEstado",operator:"eq",value:'ACT'}};	
		if($scope.SesionLocale=='PE')
			filtrosProposito.IdPropositoPortafolio={field:"IdPropositoPortafolio",operator:"eq",value:'PROPGENERALES'};
		
		pPortDG.cmbbx.proposito = { 
			filtros:filtrosProposito
		};
		
		//CONTRATOS APV
		//PRONET-IT 
		//12-02-2018
		pPortDG.FolioContratoFiltro = {
				filtros: {
					IdCliente  				: {field: "IdCliente"			 ,operator: "eq", value  : $scope.admPort.params.IdCliente },
					IdPropositoPortafolio 	: {field: "IdPropositoPortafolio",operator: "eq", value  : 'PROPOSITOAPV' },
					IdEstado				: {field: "IdEstado"			 ,operator: "eq", value  : 'VIG' },
					Producto				: {field: "Producto"			 ,operator: "eq", value  : $scope.admPort.params.Producto },
				},
		}
		
		
/*********************************************
 * GRILLAS
 *********************************************/
		pPortDG.LogCambioEjecutivoOptions = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true,
					serverSorting 	: true,
					pageSize 		: 50,
					transport: {
						read		: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3565",
										dataType: "json"
									  }
						},						
					sort 			: { field: "FechaRegistro", dir: "desc" },
					schema 			: {
						model	: { fields	: { 
							IdPortafolio 			: { type: "number"},
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
			height				: 170,
		    selectable 			: "row",
		    gridCreated: function(grid) {
				pPortDG.LogCambioEjecutivoOptions.dataSource.filter([{ field: "IdPortafolio", operator: "eq", value: Number(parametrosVentana.IdPortafolio)}]);
			},
		 };
/*********************************************
 * FUNCIONES
 *********************************************/
		pPortDG.fn.asignarValoresPorDefecto = function (){
			$.each(pPortDG.camposRequeridos,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !$scope.admPort.datos[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					$scope.admPort.datos[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
		
		pPortDG.fn.preparaDatosGuardar = function(){
			var dataPortafolio = {};
			dataPortafolio.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa;
			dataPortafolio.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
			dataPortafolio.p_IdModulo 			= 'SOC';
			dataPortafolio.p_UsaReglasNegocio 	= 'N',
			dataPortafolio.p_IdPortafolio 		= pPortDG.datosDespues.IdPortafolio,
			dataPortafolio.p_RegistroCambio 	=
			[
				//{CampoModificado:'CodigoPortafolio'				,ValorNuevo: pPortDG.datosDespues.CodigoPortafolio || ''},
				{CampoModificado:'Alias'							,ValorNuevo: pPortDG.datosDespues.Alias || ''},
			    {CampoModificado:'Proposito'						,ValorNuevo: pPortDG.datosDespues.Proposito || '0'},
			    {CampoModificado:'DescripcionPortafolio'			,ValorNuevo: pPortDG.datosDespues.DescripcionPortafolio || ''},
			    {CampoModificado:'IdDireccionEnvioCorrespondencia'	,ValorNuevo: pPortDG.datosDespues.IdDireccionEnvioCorrespondencia || '0'},
			    {CampoModificado:'IdTelefonoContacto'				,ValorNuevo: pPortDG.datosDespues.IdTelefonoContacto || '0'},
			    {CampoModificado:'IdEmpleadoEjecutivo'				,ValorNuevo: pPortDG.datosDespues.IdEmpleadoEjecutivo || '0'},
			    {CampoModificado:'IdEmpleadoEjecutivoComercial'		,ValorNuevo: pPortDG.datosDespues.IdEmpleadoEjecutivoComercial || '0'},
			    {CampoModificado:'NumeroFicha'						,ValorNuevo: pPortDG.datosDespues.NumeroFicha || '0'},
			    {CampoModificado:'IdNombreFantasia'					,ValorNuevo: pPortDG.datosDespues.IdNombreFantasia || '0'},
				{CampoModificado:'AreaCliente'						,ValorNuevo: pPortDG.datosDespues.IdAreaCliente || ''},
			];

			if ($scope.SesionLocale === 'PE') {
				dataPortafolio.p_RegistroCambio.push({CampoModificado:'IdPersonaBrokerExtranjero'		,ValorNuevo: pPortDG.datosDespues.IdPersonaBrokerExtranjero || 0});
			}
			
			return dataPortafolio;
		};
			
		pPortDG.win.visualizarMatrizOperacional = function() {
			if(pPortDG.PerfilComercialObj.IdPerfilComercialBase !== undefined) {
				pPortDG.abrirPopUpMatrizOperacional(pPortDG.PerfilComercialObj);
			}
		};

		pPortDG.formatoEstadoAutorizacion = function(){
			pPortDG.ColorAutorizacion = (pPortDG.EstadoAutorizacion.toLowerCase() === 'pen' ||  pPortDG.EstadoAutorizacion.toLowerCase() === 'pendiente') ? 'red' : 'blue';
		};
		
		pPortDG.PortafolioAPVDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=6019",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPortafolio 	: { type: "number"},
							IdCliente		: { type: "number"},
							IdPersona		: { type: "number"},
							IdPortafolioAPV : { type: "number"},
							NombreAgrupador : { type: "string"}
						}
					}
				}
			}
		});
		
/*********************************************
 * HEREDAR DATOS CONTROLADOR PADRE
 ********************************************/
		pPortDG.fn.cargarFormulario = function(){
		$scope.admPort.portafolioPromise.then(function(){
			if ($scope.admPort && $scope.admPort.datos) { 
				pPortDG.datosAntes = _.clone($scope.admPort.datos);
				pPortDG.datosDespues = _.clone($scope.admPort.datos);
				pPortDG.guardarHabilitado = (pPortDG.datosDespues.IdEstadoAutorizacion==='PEN')?false:true;
				pPortDG.datosDespues.PropositoDef = $scope.admPort.datos.Proposito;
				$scope.pPortDG.cmbbx.direccionEnvioCorrespondencia.filtros.IdPersona.value = $scope.admPort.datos.IdPersona;
				$scope.pPortDG.cmbbx.telefonoContacto.filtros.IdPersona.value = $scope.admPort.datos.IdPersona;
				$scope.pPortDG.cmbbx.nombreFantasia.filtros.IdPersona.value = $scope.admPort.datos.IdPersona;
				$scope.pPortDG.cmbbx.tipoBloqueo.filtros.IdPersona.value = $scope.admPort.datos.IdPersona;
				obtieneEstadoSolicitudesCambio($scope.admPort.datos.IdPortafolio);
				
				if(BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] == 'S'){
					if(pPortDG.datosAntes.APV !== 'APV'){
						pPortDG.cmbbx.FiltroIdPropositoPortafolio = {
							filtros: {
								IdPropositoPortafolio	: { "field" : "IdPropositoPortafolio" , "operator": "doesnotcontain" , "value" : "APV"}
							}
						}	
					}else{
						pPortDG.cmbbx.FiltroIdPropositoPortafolio = {
							filtros:{
								IdPropositoPortafolio	: { "field" : "IdPropositoPortafolio" , "operator": "contains" , "value" : "APV"}
							}
						}
						var filtroConsultaPortafolio = [ { field: "IdPortafolio", operator: "eq", value: Number(pPortDG.datosAntes.IdPortafolio) } ]
						pPortDG.PortafolioAPVDataSource.query({ filter: filtroConsultaPortafolio}).then(function(){
							//COMENTADO 15-02
							if(pPortDG.PortafolioAPVDataSource.data().length > 0){
								//pPortDG.datosDespues = pPortDG.PortafolioAPVDataSource.data()[0];
								pPortDG.datosDespues.CodigoPortafolio = pPortDG.PortafolioAPVDataSource.data()[0].NombreAgrupador;
							}
						});
					}
				}
			}
		});
		}

		pPortDG.obtenerDatosComparables = function (data) {
			return {
				Alias: data.Alias || '',
				Proposito: data.Proposito || '0',
				DescripcionPortafolio: data.DescripcionPortafolio || '',
				IdDireccionEnvioCorrespondencia: data.IdDireccionEnvioCorrespondencia || '0',
				IdTelefonoContacto: data.IdTelefonoContacto || '0',
				IdEmpleadoEjecutivo: data.IdEmpleadoEjecutivo || '0',
				IdEmpleadoEjecutivoComercial: data.IdEmpleadoEjecutivoComercial || '0',
				NumeroFicha: data.NumeroFicha || '0',
				IdNombreFantasia: data.IdNombreFantasia || '0',
				AreaCliente: data.IdAreaCliente || '',
				IdAreaCliente: data.IdAreaCliente || '',
			};
		}

		pPortDG.emitirDatosGuardados = function () {
			$scope.$emit('DatosCambiadosGuardados', { ambito: "Portafolio", nombreTab: 'PanelPortafolioDatosGenerales' });
		}

		pPortDG.avisarCambioPanel = function () {
			const dataOriginal = pPortDG.obtenerDatosComparables(pPortDG.datosAntes);
			const data = pPortDG.obtenerDatosComparables(pPortDG.datosDespues);
			const sonIguales = _.isEqual(data, dataOriginal);
			
			if (sonIguales) {
				pPortDG.emitirDatosGuardados();
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Portafolio", nombreTab: 'PanelPortafolioDatosGenerales' });
			}
		};

		$scope.$watchCollection('pPortDG.datosDespues', function () {
			let modoEdicion= !pPortDG.win.modoLectura
			if (modoEdicion) {
				pPortDG.avisarCambioPanel();
			}	
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------