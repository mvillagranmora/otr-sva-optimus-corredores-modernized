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
		pinformederecho.envioBursatil=false;

		$scope.dataTemplate	= {};
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		var p_TablaParametrosAux; //parametros generales que puede ver el jobmanager
		var params = {
				p_TablaParametrosAux : [
				    {	Tipo			: 'BATCH',
				    	NombreParametro : 'idUsuario',
				    	Valor : $scope.SesionIdUsuario
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'schema',
				    	Valor : Model.getSchema()
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'grabar',
				    	Valor : $scope.solCir.grabar
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'reemplazar',
				    	Valor : $scope.solCir.reemplazar
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'separadorListas',
				    	Valor : ''
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'simboloDecimal',
				    	Valor : $scope.solCir.simboloDecimal
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'simboloFecha',
				    	Valor : $scope.solCir.simboloFecha
				    }, {Tipo			: 'BATCH',
				    	NombreParametro : 'aceptarFechasNull',
				    	Valor : $scope.solCir.aceptarFechasNull
				    },]};
		
		
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

					
			//Creacion Documento
			parametrosSP.p_IdTipoDocumento 			= pinformederecho.IdTipoDocumento;
			//parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

			//Tarea Programada			

			parametrosSP.p_CreaParam 				= 'S';
			
			parametrosSP.p_EsPauta					= 'N'
				
				
				var fd = util.dateSPdate(pinformederecho.FecInicio);
			params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'fechaDesde',Valor : fd});
			
			var fh = util.dateSPdate(pinformederecho.FecInicio);
			params.p_TablaParametrosAux.push({Tipo: 'BATCH',NombreParametro : 'fechaHasta',Valor : fh});
			
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosAux;
			
			if (fh < fd){
	 			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	 			return;
	 		};
			
	 		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'NOMCOLSAL' , Valor : 'N' });
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : fd });
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FTER' , Valor : fh });			
				
			if(	pinformederecho.envioBursatil)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'ENVIO_TANDEM' , Valor : 'S' });
			}else{	
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'ENVIO_TANDEM' , Valor : 'N' });
				
			};

			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
			
			
						
			
			
			
			
			
			
			
	      
		};
			
		$scope.$watch('pinformederecho.FecInicio', function(newval,oldval){
			pinformederecho.fnVersionEsquema();
		},true);
		
		$scope.pinformederecho.clear();

	});
}());