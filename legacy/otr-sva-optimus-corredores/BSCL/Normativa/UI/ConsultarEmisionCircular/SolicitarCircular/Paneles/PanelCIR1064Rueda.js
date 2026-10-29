(function() {
	angular.module('ModuloNormativa').controller('PanelCIR1064RuedaCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1064R = this;
		pc1064R.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1064R.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pc1064R.TipoArchivo			= 'pdf';
		pc1064R.IdVersionEsquema 	= '';
	
		pc1064R.IdTipoDocumento = 'CIR1064R';
		

		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
		                  		// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	  		{	Tipo			: 'BATCH',
	  			NombreParametro : 'idUsuario',
	  			Valor : $scope.SesionIdUsuario
	  		},{ Tipo			: 'BATCH',
	  			NombreParametro : 'schema',
	  			Valor :  Model.getSchema()
	  		}, {Tipo			: 'BATCH',
	  			NombreParametro : 'grabar',
	  			Valor : $scope.solCir.grabar
	  		}, {Tipo			: 'BATCH',
	  			NombreParametro : 'reemplazar',
	  			Valor : $scope.solCir.reemplazar
	  		},{ Tipo			: 'BATCH',
	  			NombreParametro : 'separadorListas',
	  			Valor : $scope.solCir.separadorListas
	  		},{ Tipo			: 'BATCH',
	  			NombreParametro : 'simboloDecimal',
	  			Valor : $scope.solCir.simboloDecimal
	  		},{	Tipo			: 'BATCH',
	  			NombreParametro : 'simboloFecha',
	  			Valor : $scope.solCir.simboloFecha
	  		},{	Tipo			: 'BATCH',
	  			NombreParametro : 'aceptarFechasNull',
	  			Valor : $scope.solCir.aceptarFechasNull
	 	},]};
		

		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pc1064R.clear = function(){
			pc1064R.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
						
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
			bcsComunes.ventanaOverlay(pc1064R.idVentana, false);
			bcsComunes.ventanaData(pc1064R.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pc1064R.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pc1064R.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pc1064R.fnVersionEsquema = function() {
			if (pc1064R.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pc1064R.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pc1064R.IdTipoDocumento;
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
								pc1064R.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		
			
		pc1064R.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol

			
			//Especificos

			//parametrosSP.p_FechaInicio 				=  util.dateSPdate(pc1064R.FecInicio);

			
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pc1064R.IdTipoDocumento;
			//parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			

			parametrosSP.p_CreaParam 				= 'S';
			
			parametrosSP.p_EsPauta					= 'N'
			
		//	var fd = util.dateSPdate(pc1064R.FecInicio);
		//	params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : util.dateSPdate(pc1064R.FecInicio) });

			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
		

		$scope.$watch('pc1064R.FecInicio', function(newval,oldval){
			pc1064R.fnVersionEsquema();
		},true);
		
		$scope.pc1064R.clear();

	});
}());