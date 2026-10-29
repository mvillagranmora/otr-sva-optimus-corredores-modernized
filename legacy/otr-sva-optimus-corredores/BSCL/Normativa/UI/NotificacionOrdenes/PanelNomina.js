(function() {
	angular.module('ModuloNormativa').controller('PanelNominaCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosNormativa,
				 bcsAlerta, OPCIONES_BCSALERTA){

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
		
		
		pLCP.TipoArchivo			= 'pdf';
	    pLCP.IdVersionEsquema 	= 'V1';
		pLCP.IdTipoDocumento = 'EJECORD';
		pLCP.IdTipoDocumentoCarta='EJECORD';
		pLCP.IdVersionEsquemaCarta 	= 'V1';
		pLCP.modulo='NOR';
		pLCP.salidaMensaje='';
		$scope.dataTemplate	= {};
		pLCP.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		pLCP.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=601150",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								   IdEmpresa				: { type: "number"},
								   IdEjecucion				: { type: "number"},
								   //IdDocumento				: { type: "number"},
								   IdCliente				: { type: "number"},
								   IdPortafolio				: { type: "number"},
								   IdPersona				: { type: "number"},
								   CodigoPortafolio			: { type: "string"},
								   NombreCliente			: { type: "string"},
								   //Datos del Envio (Nomina)
								   DireccionElectronica		: { type: "string"},
								   DireccionFisica			: { type: "string"},
								   IdEstadoNotificacion	    : { type: "string"},
								   EstadoNotificacion	    : { type: "string"},
								   IdEstadoCarta			: { type: "string"},
								   EstadoCarta	   			: { type: "string"},
								   FechaIngreso				: { type: "date"},
								   sFechaIngreso				: { type: "date"},
								  // FechaCreacion			: { type: "date"},
								   FechaEjecucion			: { type: "date"},
								   sFechaEjecucion			: { type: "date"}
								   //IdUsuarioCreador			: { type: "number"},
								   //CodigoUsuarioCreador		: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			        {field: "FechaEjecucion",				title: $translate.instant("UI_FECHA_EJECUCION"),			width: "80px"	},
				    {field: "IdCliente",					title: $translate.instant("UI_CLIENTE"),					width: "60px"	},
					{field: "CodigoPortafolio",				title: $translate.instant("UI_PORTAFOLIO"),					width: "60px"	},
					{field: "NombreCliente",				title: $translate.instant("UI_NOMBRE"),						width: "180px"	},
					{field: "DireccionFisica",				title: $translate.instant("UI_ENVIO_CORRESPONDENCIA"),		width: "140px"	},
					{field: "DireccionElectronica",			title: $translate.instant("UI_DIRECCION_ELECTRONICA"),		width: "140px"	},
					{field: "EstadoNotificacion",			title: $translate.instant("UI_ESTADO_NOTIFICACION"),		width: "100px"	},
					{field: "EstadoCarta",					title: $translate.instant("UI_ESTADO_CARTA"),				width: "110px"	},
					{field: "FechaIngreso",					title: $translate.instant("UI_FECHA_INGRESO"),				width: "60px"	}
					//{field: "CodigoUsuarioCreador",			title: $translate.instant("UI_USUARIO"),					width: "100px"	}
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
		    filterable	: true
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
						paramServicio.p_TipoDocumento = pLCP.IdTipoDocumentoCarta;
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
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			

	    	if(pLCP.FecInicio){
	    		pLCP.fn.envioFecha();
	    		niv1filters.push({ field: "FechaEjecucion", operator: "gte", value: util.dateInicioDia(pLCP.FecInicio) });
	    		
	    	};
	    	
	    	if(pLCP.FecTermino){
	    		pLCP.fn.envioFecha();
	    		niv1filters.push({ field: "FechaEjecucion", operator: "lte", value: util.dateTerminoDia(pLCP.FecTermino) });
	    		
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
		

		
		pLCP.fn.GenerarCartas = function(seleccionado) {
		
						ventanaRespuesta = bcsComunes.ventanaAbrir("GeneracionCartasEjecucionOrdenes", {PrevFecInicio : $scope.pLCP.FecInicio,PrevFecTermino: $scope.pLCP.FecTermino,PrevVentana:pLCP.idVentana}, true, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaRespuesta);
						 promesa.then(function(data){
				         });
		
		};
		
		pLCP.fn.VerCarta = function(seleccionado) {
			pLCP.fnVersionEsquemaCarta();
			if(seleccionado!==undefined && seleccionado.length ==1)
			{
					var a= seleccionado;
					
								// Obtener  version del tipo documento
					
					var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: pLCP.IdTipoDocumentoCarta},
					                       { field: "TipoArchivo",  operator: "eq", value: pLCP.TipoArchivo },
					                       { field: "Version",  	operator: "eq", value: pLCP.IdVersionEsquemaCarta },
					                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
					                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
					$scope.cLim.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
						if($scope.cLim.ConsultaArchivoNormativa.view().length > 0) {
							$scope.dataTemplate.TipoReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].TipoArchivo;
							$scope.dataTemplate.IdReporte		= $scope.cLim.ConsultaArchivoNormativa.view()[0].IdArchivo;
							$scope.dataTemplate.NombreReporte	= $scope.cLim.ConsultaArchivoNormativa.view()[0].NombreArchivo;
							//PARAMETROS
							var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
												"p_IdCliente"				: seleccionado[0].IdCliente,
												"p_FechaDesde"				: util.dateSPdate($scope.pLCP.FecInicio),
												"p_FechaHasta"				: util.dateSPdate($scope.pLCP.FecTermino),	
											     "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
  											  };
							//'CIRRESUMEN.rpt'
							//DATA 
							$scope.dataTemplate.data= {  "serviceId"   :	$scope.cLim.ConsultaArchivoNormativa.view()[0].ServicioArchivo
  													   , "serviceData" :	parametros
													  };
							
							var subrep = new Array();
							$scope.dataTemplate.subReports=subrep;
							$scope.cLim.getReport($scope.dataTemplate);
							
										
						}
					});
					
				
			};
				
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

			if(seleccionado!==undefined && seleccionado.length ==1 )
			{
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_NOTIFICAR") + ", " + $translate.instant("UI_CLIENTE") + ":"  + seleccionado[0].NombreCliente,pLCP.idVentana);
				t .then(function(result)
			    //t.result.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						var a= seleccionado;
								
						pLCP.fn.envioFecha();
						var registro = [];		
						parametrosSP= {}; 
						//$scope.pc1.envioFecha();
				    	//Comunes
				    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
				    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
				    	parametrosSP.p_IdModulo 				=  pLCP.modulo;
				    	
						//parametrosSP.p_Periodo					=	pLCP.PeriodoEnv;
						
						
						//Especificos
						registro.push({IdEjecucion : seleccionado[0].IdEjecucion });
						parametrosSP.p_TablaNotificacion=registro;
								
						bcsComunes.msg('info', 'Carga', 'En proceso...');
						bcsComunes.ventanaOverlay(pLCP.idVentana,true);
						pLCP.salidaMensaje='NOTIFICACION';
						bcsServiciosNormativa.EjecucionOrdenesNotificarCliente(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
						bcsComunes.ventanaOverlay(pLCP.idVentana,false);
						pLCP.fn.buscar();
				       };
				});
			};
					
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
						bcsServiciosNormativa.CircularizacionNotificarNomina(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
						bcsComunes.ventanaOverlay(pLCP.idVentana,false);
						pLCP.fn.buscar();
				       };
				});
		};	
		
		pLCP.fn.ExampleCtrl= function() {
				var texto="";
				var i=1;
				while (i <= pLCP.grilla.Nomina.dataSource.total()){
					if (i==1)
					{
						texto= pLCP.grilla.Nomina.dataSource.ds._data[0].EstadoNotificacion;
					}
					else{
						texto= texto + "\n" + pLCP.grilla.Nomina.dataSource.ds._data[0].EstadoNotificacion;	
					};
					
					i=i+1;
				}
				
				var file = new File([texto], "Notificacion.txt", {type: "text/plain;charset=utf-8"});
					saveAs(file);
		
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
		
	
		$scope.$watch('pLCP.cmbbx.IdTipoDocumento',function(newval,oldval){
				if (!pLCP.cmbbx.IdTipoDocumento)
				{
					pLCP.cmbbx.IdTipoDocumento='CIR1985';
					
				};
		});
		pLCP.fn.limpiar = function(){
			
			//pLCP.filtro.IdClasificacionTipoInstrumento	= 0;
			pLCP.grilla.Nomina.dataSource.removeAll();
		};
		

	});
}());