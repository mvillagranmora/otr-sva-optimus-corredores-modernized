(function() {
	angular.module('ModuloNormativa').controller('PanelNominaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA, bcsConfiguracionNormativa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLCP 		= this;
		pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
		pLCP.win		= {};
		pLCP.grilla		= {};
		pLCP.cmbbx		= {};
		pLCP.filtro		= {};
		pLCP.fn			= {};
		
		pLCP.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		pLCP.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		pLCP.Periodo 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pLCP.configuracionVerCarta = true;
		pLCP.configVerCarta = {P: false, UP: false, CP: false};
		
		pLCP.TipoArchivo			= 'pdf';
	    pLCP.IdVersionEsquema 	= '';
		pLCP.IdTipoDocumento = 'CIRCNomina';
		pLCP.IdTipoDocumentoCarta='CIRCCarta';
		pLCP.IdTipoDocumentoCartaConsolidada='CIRCCartaC';
		pLCP.IdVersionEsquemaCarta 	= '';
		pLCP.modulo='NOR';
		pLCP.salidaMensaje='';
		$scope.dataTemplate	= {};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		bcsConfiguracionNormativa.CONFIGURACION.promise.then(function(result){
			if (bcsConfiguracionNormativa.CONFIGURACION['ConfiguracionCircularizacionCustodia'] !== undefined){
				pLCP.ConfiguracionCircularizacionCustodia = bcsConfiguracionNormativa.CONFIGURACION['ConfiguracionCircularizacionCustodia'];
				pLCP.configVerCarta[pLCP.ConfiguracionCircularizacionCustodia] = true;
			}
			else
				pLCP.ConfiguracionCircularizacionCustodia = 'P';
			
			if (bcsConfiguracionNormativa.CONFIGURACION['NotificarNominaCircAgrupadoPorCliente'] === 'S')
				pLCP.notificarNominaCircPorCliente = 'S';
			else
				pLCP.notificarNominaCircPorCliente = 'N';
		});
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLCP.grilla.Nomina = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3025",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								   IdNomina					: { type: "number"},
								   IdDocumento				: { type: "number"},
								   IdCliente				: { type: "number"},
								   IdPortafolio				: { type: "number"},
								   CodigoPortafolio			: { type: "string"},
								   NombreCliente			: { type: "string"},
								   DocumentoIdentidad		: { type: "string"},
								   //Datos del Envio (Sistema)
								   //IdDireccionElectronica	: { type: "number"},
								   //IdTelefonoContacto		: { type: "number"},
								   //IdDireccionFisica		: { type: "number"},
								   //Datos del Envio (Nomina)
								   FormaEnvio				: { type: "string"},
								   DireccionElectronica		: { type: "string"},
								   DireccionFisica			: { type: "string"},
								   Notificar			    : { type: "string"},
								   IdEstadoNotificacion	    : { type: "string"},
								   EstadoNotificacion	    : { type: "string"},
								   IdEstadoRespuesta		: { type: "string"},
								   EstadoRespuesta	   		: { type: "string"},
								   Motivo			   		: { type: "string"},
								   FechaIngreso				: { type: "date"},
								   FechaCreacion			: { type: "date"},
								   FechaModificacion		: { type: "date"},
								   IdUsuarioCreador			: { type: "number"},
								   CodigoUsuarioCreador		: { type: "string"},
								   Periodo					: { type: "string"},
								   PDFMasivo				: { type: "string"},
							}
						}
					}
				}
			}),
			columns: [
				    //{field: "IdCliente",					title: $translate.instant("UI_CLIENTE"),					width: "60px"	},
				    {field: "DocumentoIdentidad",			title: $translate.instant("UI_RUT_CLIENTE"),				width: "80px"	},
					{field: "CodigoPortafolio",				title: $translate.instant("UI_PORTAFOLIO"),					width: "60px"	},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
					{field: "DireccionFisica",				title: $translate.instant("UI_ENVIO_CORRESPONDENCIA"),		width: "140px"	},
					{field: "DireccionElectronica",			title: $translate.instant("UI_DIRECCION_ELECTRONICA"),		width: "140px"	},
					{field: "FormaEnvio",					title: $translate.instant("UI_ENVIO"),						width: "100px"	},
					{field: "Notificar",					title: $translate.instant("UI_NOTIFICAR"),					width: "60px"	},
					{field: "EstadoNotificacion",			title: $translate.instant("UI_ESTADO_NOTIFICACION"),		width: "100px"	},
					{field: "EstadoRespuesta",				title: $translate.instant("UI_ESTADO_RESPUESTA"),			width: "110px"	},
					{field: "Motivo",						title: $translate.instant("UI_MOTIVO"),						width: "110px"	},
					{field: "FechaIngreso",					title: $translate.instant("UI_FECHA_INGRESO"),				width: "60px"	},
					{field: "CodigoUsuarioCreador",			title: $translate.instant("UI_USUARIO"),					width: "100px"	},
					{field: "PDFMasivo",					title: $translate.instant("UI_PDF_MASIVO"),					width: "85px"	}
			],
			hideDetailButton: function(row) {
				var hide = true;
				//if(row.IdTipoLimite === 'LIMCTPEXCLUIREMISOR' || row.IdTipoLimite === 'LIMCTPMARGENTASAS') 
				//	hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
			autoBind	: false,
			height		: 540,//580,
		    //selectable 	: "row",
		    selectable 			: "multiple row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable	: true,
			rowCellColor : function(row) {
				switch(row.get("PDFMasivo")) {
					case 'SI':
							return {column: "PDFMasivo", color: "green"};
		                break;
				
				    default:
		            	return {column: "PDFMasivo", color: "white"};
		                break;  	
				}
			},
			rowColorBackground	: function(row) {		
				switch(row.get("PDFMasivo")) {
		             case 'SI':
		                return "white";
		                break;    
		            default:
		            	return "blue";
		                break;     
				  }	
				}
		    
		    
		    
		    
		};
		
	
		

		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		pLCP.cmbbx.SocioDeNegocio={
			filtros: {
				IdEmpresa : {field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : {field: "EsCliente",operator: "eq", value: 'UI_SI'}
			}
		};
		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		pLCP.fn.envioFecha = function(){
			var date = '';
			date= util.dateSPdate(pLCP.Periodo);
			pLCP.PeriodoEnv= date.substring(0,4)  + date.substring(5,7);
						
			var dateFin = new Date();
			dateFin 	 = pLCP.Periodo;
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pLCP.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		
				//Funcion Obtener version del Esquema
		pLCP.fnVersionEsquema = function() {
			if (pLCP.Periodo !=undefined)
				{
						var fd = util.dateSPdate(pLCP.Periodo);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pLCP.IdTipoDocumento;
						if (fd !== null || fd !== undefined) {
							paramServicio.p_Fecha = fd;
						}
						else 
						{
							paramServicio.p_Fecha = $scope.SesionFechaSistema;
						}
						
						bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
							if (respuesta.P_OUT_VERSIONESQUEMA)
							{
								pLCP.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
							}
							else
							{	
								bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
							}		
							
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});	
				};	
		};	
		
		pLCP.fnVersionEsquemaCarta = function() {
			if (pLCP.Periodo !=undefined)
				{
						var fd = util.dateSPdate(pLCP.Periodo);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pLCP.ConfiguracionCircularizacionCustodia != 'CP' ? pLCP.IdTipoDocumentoCarta : pLCP.IdTipoDocumentoCartaConsolidada;
						if (fd !== null || fd !== undefined) {
							paramServicio.p_Fecha = fd;
						}
						else 
						{
							paramServicio.p_Fecha = $scope.SesionFechaSistema;
						}
						
						bcsServiciosNormativa.ObtenerVersionEsquema(paramServicio, function(respuesta) {
							if (respuesta.P_OUT_VERSIONESQUEMA)
							{
								pLCP.IdVersionEsquemaCarta=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
							}
							else
							{	
								bcsComunes.msg('error', $scope.TITLE, 'UI_ERROR_VERSIONESQUEMA_NO_DEFINIDO');
							}		
							
						}, function(error){
							bcsComunes.msg('error', $scope.TITLE, error.message);
						});	
				};	
		};	
		
		pLCP.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			//objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			//niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });

	    	if(pLCP.Periodo){
	    		pLCP.fn.envioFecha();
	    		niv1filters.push({ field: "Periodo", operator: "eq", value:pLCP.PeriodoEnv });
	    	};
			if(pLCP.filtro.IdSocioDeNegocio && pLCP.filtro.IdSocioDeNegocio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pLCP.SocioDeNegocioObj.IdCliente });
			};
	    	
	    	if (niv1filters.length>0) pLCP.grilla.Nomina.dataSource.filter(objfilters);
	    	else pLCP.grilla.Nomina.dataSource.filter(new Array());
	    	
	    	pLCP.grilla.limiteSel = undefined;
		};
		
		pLCP.fn.SolicitarCircularizacion = function() {
		
						ventanaSolicitarCircularizacion = bcsComunes.ventanaAbrir("SolicitarCircularizacion", {PrevPeriodo :pLCP.Periodo,PrevUIModo :'CIRCULARIZACION',PrevIdVersionEsquema :pLCP.IdVersionEsquema}, false, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaSolicitarCircularizacion);
				        promesa.then(function(data){
				        	pLCP.fn.buscar();
						});
		};
		pLCP.fn.ModificarParametros = function() {
						ventanaSolicitarCircularizacion = bcsComunes.ventanaAbrir("SolicitarCircularizacion", {PrevPeriodo :pLCP.Periodo,PrevUIModo :'PARAMETRO',PrevIdVersionEsquema :pLCP.IdVersionEsquema}, true, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaSolicitarCircularizacion);
						 promesa.then(function(data){
							//bcsComunes.ventanaCerrar(ventanaSolicitarCircularizacion.idVentana);
				        	
						 });
		};
		pLCP.fn.EdicionMultiple = function(seleccionado) {
						ventanaEdicionMultiple = bcsComunes.ventanaAbrir("EdicionMultipleCircularizacion", {PrevSeleccion :seleccionado}, true, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaEdicionMultiple);
						 promesa.then(function(data){
				        	pLCP.fn.buscar();
						 });
		};
		
		pLCP.fn.Respuesta = function(seleccionado) {
			if(seleccionado!==undefined && seleccionado.length ==1)
			{
						ventanaRespuesta = bcsComunes.ventanaAbrir("RespuestaCircularizacion", {PrevSeleccion :seleccionado}, true, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaRespuesta);
						 promesa.then(function(data){
				        	pLCP.fn.buscar();
						 });
			};
		};
		
		pLCP.fn.GenerarCartas = function(seleccionado) {
		
						ventanaRespuesta = bcsComunes.ventanaAbrir("GeneracionCartasCircularizacion", {PrevPeriodo :pLCP.Periodo}, true, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaRespuesta);
						 promesa.then(function(data){
				         });
		
		};
		
		pLCP.fn.VerCarta = function(seleccionado) {
			$("body").append("<div id='loading-overlay' class='overlay'><div class='k-loading-image'></div></div>");
			pLCP.fnVersionEsquemaCarta();
			if(seleccionado!==undefined && seleccionado.length ==1)
			{
					var a= seleccionado;
					
								// Obtener  version del tipo documento
					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.ConfiguracionCircularizacionCustodia == 'CP' ? pLCP.IdTipoDocumentoCartaConsolidada : pLCP.IdTipoDocumentoCarta},
					                       { field: "TipoArchivo",  operator: "eq", value: pLCP.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pLCP.IdVersionEsquemaCarta },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.cLim.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.cLim.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.cLim.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							$scope.dataTemplate.CodigoPortafolio   = seleccionado[0].CodigoPortafolio;
							$scope.dataTemplate.DocumentoIdentidad = seleccionado[0].DocumentoIdentidad;
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_IdCliente"				: seleccionado[0].IdCliente,
												"p_IdPortafolio"			: seleccionado[0].IdPortafolio,
												"p_FechaConsulta"			: util.dateSPdate(pLCP.PeriodoEnvFinal),
												"p_FormaSalida"				: "CARTA",
												"p_UnificarPortafolios"		: pLCP.ConfiguracionCircularizacionCustodia === 'UP' ?'S':'N',
												"p_IdUsuario"				: $scope.SesionIdUsuario,
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }, { "field" : "FirmaCorredora", "type" : "blob" }]
  											  };
							//'CIRRESUMEN.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							$scope.dataTemplate.parameters= [{ "type"   :	"pdf417"
													 		 , "name"   :	"pdf417"
													 		 , "data"   :	seleccionado[0].NombreCliente.normalize("NFD").replace(/[^a-zA-Z0-9-\s]/g, '') 
															}];
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							$scope.cLim.getReport($scope.dataTemplate);
							
										
						}
					});	
				$("body").find(".overlay").remove();
			}
			else{
				$("body").find(".overlay").remove();
				bcsComunes.ventanaOverlay(pLCP.idVentana,true);
				bcsComunes.msg('error', 'Error', 'Debes seleccionar una fila de la tabla.');
				bcsComunes.ventanaOverlay(pLCP.idVentana,false);
			}
				
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			//bcsComunes.ventanaOverlay(pc1.idVentana,true);
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pLCP.salidaMensaje=$translate.instant("UI_" + pLCP.salidaMensaje + "_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pLCP.salidaMensaje);
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				
				pLCP.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
			/*	bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
										conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);
			*/
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",pLCP.salidaMensaje);
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			bcsComunes.ventanaMsg({ mensaje : pLCP.salidaMensaje,
				conflictos : util.parseServerResponse(respuesta)}, pLCP.idVentana),true,true;
			
			bcsComunes.ventanaData(pLCP.idVentana, respuesta);
			//bcsComunes.ventanaCerrar(pLCP.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	

			pLCP.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pLCP.salidaMensaje, 
								 conflictos : util.parseServerResponse(respuesta)}, pLCP.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",pLCP.salidaMensaje);
			bcsComunes.ventanaOverlay(pLCP.idVentana, false);
		};		
		
		
		pLCP.fn.Notificar= function(seleccionado) {
			if ((seleccionado && seleccionado.length <= 0) || !seleccionado) {
				bcsComunes.msg('error', 'Carga', 'Debes seleccionar al menos un registro');
				return;
			}
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("ENVIAR_NOTIFICACIONES_CLIENTE"), pLCP.idVentana);
			t .then(function(result) {		
				if (result===OPCIONES_BCSALERTA.SI) {	
					pLCP.fn.envioFecha();
					var registro = [];		
					parametrosSP= {}; 
					//$scope.pc1.envioFecha();
					//Comunes
					parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
					parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
					parametrosSP.p_IdModulo 				=  pLCP.modulo;
					parametrosSP.p_Periodo					=	pLCP.PeriodoEnv;
					parametrosSP.p_IdSolicitudCircularizacion = 0;
					parametrosSP.p_DataSource = null;

					//Especificos
					registro.push(...seleccionado.map(({ IdNomina }) => IdNomina));
					parametrosSP.p_IdNominas=registro;
							
					bcsComunes.msg('info', 'Carga', 'En proceso...');
					bcsComunes.ventanaOverlay(pLCP.idVentana,true);
					pLCP.salidaMensaje='NOTIFICACION';
					bcsServiciosNormativa.CircularizacionNotificarCliente(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
					bcsComunes.ventanaOverlay(pLCP.idVentana,false);
					pLCP.fn.buscar();
				};
			});
					
		};
		
		pLCP.fn.NotificarNomina= function() {

		
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_NOTIFICAR_NOMINA_CLIENTES_TODOS" +  " ?" ),pLCP.idVentana);
			    //t.result.then(function(result)
				t .then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
					 {
						pLCP.fn.envioFecha();
						var registro = [];		
						parametrosSP= {}; 
						//$scope.pc1.envioFecha();
				    	//Comunes
				    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
				    	parametrosSP.p_IdModulo 				=  pLCP.modulo;
						parametrosSP.p_Periodo					=	pLCP.PeriodoEnv;
						bcsComunes.msg('info', 'Carga', 'En proceso...');
						bcsComunes.ventanaOverlay(pLCP.idVentana,true);
						pLCP.salidaMensaje='NOTIFICACION';
						if(pLCP.notificarNominaCircPorCliente == 'S')
							bcsServiciosNormativa.CircularizacionNotificarNominaPorCliente(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
						else
							bcsServiciosNormativa.CircularizacionNotificarNomina(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
						bcsComunes.ventanaOverlay(pLCP.idVentana,false);
						pLCP.fn.buscar();
				       };
				});
		};	
		
		
		 pLCP.fn.ActualizarCliente= function(seleccionado) {
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_ACTUALIZACION_CLIENTES_SELECCIONADOS" +  " ?" ),pLCP.idVentana);
			   // t.result.then(function(result)
			t .then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
					
								
						pLCP.fn.envioFecha();
						var registro = [];		
						parametrosSP= {}; 
						//$scope.pc1.envioFecha();
				    	//Comunes
				    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
				    	parametrosSP.p_IdModulo 				=  pLCP.modulo;
				    	parametrosSP.p_UsaReglasNegocio			= 'N';
						 var registroCambio = [];
						 $.each(seleccionado,function(key,value)
			      	                {       		     
			      	    	           registroCambio.push(
				      		             {   
				      			           IdNomina : seleccionado[key].IdNomina,
				      			           
				      		             });
			      	                });
			      	    
						parametrosSP.p_TablaNomina = registroCambio;
						
						
						bcsComunes.msg('info', 'Carga', 'En proceso...');
						bcsComunes.ventanaOverlay(pLCP.idVentana,true);
						pLCP.salidaMensaje='ACTUALIZACION';
						bcsServiciosNormativa.CircularizacionActualizarDatos(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
						bcsComunes.ventanaOverlay(pLCP.idVentana,false);
						pLCP.fn.buscar();
				       };
				});
				
		};
		
		pLCP.fn.ImprimirResumen = function() {

			// Obtener  version del tipo documento
					pLCP.fn.envioFecha();
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.IdTipoDocumento },
					                       { field: "TipoArchivo",  operator: "eq", value: pLCP.TipoArchivo },
					                       { field: "Version",  	  operator: "eq", value: pLCP.IdVersionEsquema },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.cLim.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.cLim.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.cLim.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_Periodo"					: pLCP.PeriodoEnv,
												"p_Seccion"					: "AUD",
												
					                           "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'CIRRESUMEN.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							
							//SUB-REPORTE :RESCLIENTES
							var parametros1 =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												 "p_Periodo"					: pLCP.PeriodoEnv,
												 "p_seccion"					: "CLI",
					                             "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							var parametros2 =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												 "p_Periodo"					: pLCP.PeriodoEnv,									
												 "p_seccion"					: "COM",
					                             "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							var parametros3 =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												 "p_Periodo"					: pLCP.PeriodoEnv,									
												 "p_seccion"					: "INS",
					                             "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							var parametros4 =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												 "p_Periodo"					: pLCP.PeriodoEnv,									
												 "p_seccion"					: "CTP",
					                             "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };	

							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							subrep.push({"reportName" : "RESCLIENTES",
												                  "data" : {
													                  		"serviceId" : $scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo,
													                  		"serviceData" :parametros1 
													                       }
												            });					     
							subrep.push({"reportName" : "RESCOMPANIA",
												                  "data" : {
													                  		"serviceId" : $scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo,
													                  		"serviceData" :parametros2 
													                       }
												            });	
							subrep.push({"reportName" : "RESINSTRUMENTO",
												                  "data" : {
													                  		"serviceId" : $scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo,
													                  		"serviceData" :parametros3 
													                       }
												            });	
							subrep.push({"reportName" : "RESCTAPROPIA",
												                  "data" : {
													                  		"serviceId" : $scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo,
													                  		"serviceData" :parametros4 
													                       }
												            });								
							//$scope.dataTemplate.dataSource	= $scope.dataTemplate.ServicioReporte+"(p_IdEmpresa =>"+$scope.$root.SesionIdEmpresa+",p_FechaOperacionDesde =>'" + util.dateSPdate(pLCP.FecInicio) + "',p_FechaOperacionHasta =>'" +  util.dateSPdate(pLCP.FecTermino) + "',p_out_TablaCarta1 => ?)";
							$scope.dataTemplate.parameters= {};
							$scope.cLim.getReport($scope.dataTemplate);
							
										
						}
					});
				
			};
			
		$scope.$watch('pLCP.Periodo', function(newval,oldval){
			pLCP.fnVersionEsquema();
			pLCP.fnVersionEsquemaCarta();
		},true);
				
		pLCP.fn.limpiar = function(){
			
			pLCP.filtro.IdClasificacionTipoInstrumento	= 0;
			pLCP.grilla.Nomina.dataSource.removeAll();
		};
		

	});
}());