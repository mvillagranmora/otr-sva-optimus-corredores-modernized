(function() {
	angular.module('ModuloNormativa').controller('PanelInformeDerechoCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pinformederecho = this;
		pinformederecho.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pinformederecho.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		//pc1.IdTipoCircular 	= 'ART171CartaUno';
		//pc1.IdTipoTarea		= 'EXPORTACIONCARTA1ART171';
		pinformederecho.TipoArchivo			= 'txt';
		pinformederecho.IdVersionEsquema 	= '';
	
		pinformederecho.IdTipoDocumento = 'INFDERECH';
		

		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ 
		{
			NombreParametro : 'idUsuario',
			Valor : $scope.SesionIdUsuario
		},
		{
			NombreParametro : 'schema',
			Valor :  Model.getSchema()
		}, 
		{
			NombreParametro : 'grabar',
			Valor : $scope.solCir.grabar
		},
		{
			NombreParametro : 'reemplazar',
			Valor : $scope.solCir.reemplazar
		},
		{
			NombreParametro : 'separadorListas',
			Valor : $scope.solCir.separadorListas
		},
		{
			NombreParametro : 'simboloDecimal',
			Valor : $scope.solCir.simboloDecimal
		},
		{
			NombreParametro : 'simboloFecha',
			Valor : $scope.solCir.simboloFecha
		},
		{
			NombreParametro : 'aceptarFechasNull',
			Valor : $scope.solCir.aceptarFechasNull
		},
		{
			NombreParametro : 'EsPauta',
			Valor : 'N'
		},
		]};
		
		var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pinformederecho.clear = function(){
			pinformederecho.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
						
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				
			}
			else
			{	
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada'));
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}		
			bcsComunes.ventanaOverlay(pinformederecho.idVentana, false);
			bcsComunes.ventanaData(pinformederecho.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pinformederecho.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pinformederecho.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pinformederecho.fnVersionEsquema = function() {
			if (pinformederecho.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pinformederecho.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pinformederecho.IdTipoDocumento;
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
								pinformederecho.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
	
		pinformederecho.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol

			
			//Especificos
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pinformederecho.FecInicio);

			//Tipo Listas 
			parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pinformederecho.IdTipoDocumento;
			parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			

			parametrosSP.p_CreaParam 				= 'S';
			
			parametrosSP.p_EsPauta					= 'N';
			
			var fd = util.dateSPdate(pinformederecho.FecInicio);
			params.p_TablaParametrosaux.push({NombreParametro : 'p_FecDesde',Valor : fd});

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CrearSolicitudCircular(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
			
		$scope.$watch('pinformederecho.FecInicio', function(newval,oldval){
			pinformederecho.fnVersionEsquema();
		},true);
		
		$scope.pinformederecho.clear();

	});
}());