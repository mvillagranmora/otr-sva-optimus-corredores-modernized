(function() {
	angular.module('ModuloNormativa').controller('PanelCIR20Anexo1Ctrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc2 = this;
		pc2.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		
		pc2.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		
		pc2.TipoArchivo			= 'xml';
	    pc2.IdVersionEsquema 	= '';
		pc2.IdTipoDocumento = 'CIR20AN1';
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
		},{ Tipo			: 'BATCH',
			NombreParametro : 'simboloFecha',
			Valor : $scope.solCir.simboloFecha
		},{ Tipo			: 'BATCH',
			NombreParametro : 'aceptarFechasNull',
			Valor : $scope.solCir.aceptarFechasNull
		},]};
		
		pc2.FecInicioOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pc2.clear = function(){
			pc2.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
			pc2.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		};
		
		$scope.pc2.envioFecha = function(){
			var date = '';
			var dateFin = new Date();
			dateFin 	 = pc2.FecInicio;
			date= util.dateSPdate(pc2.FecInicio);
			pc2.FecInicio= util.dateInicioDia(date.substring(0,8) + '01');
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pc2.FecTermino= util.dateInicioDia(ultimoDia);
		};
		
		var successCrearSolicitud = function(respuesta)
		{	
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("info","info",'Solicitud creada con éxito');
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
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
			bcsComunes.ventanaOverlay(pc2.idVentana, false);
			bcsComunes.ventanaData(pc2.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pc2.idVentana);
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pc2.idVentana, false);
		};
		
		//Funcion Obtener version del Esquema
		pc2.fnVersionEsquema = function() {
			if (pc2.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pc2.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pc2.IdTipoDocumento;
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
								pc2.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		 pc2.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    				
			//Especificos
	    	parametrosSP.p_IdTipoDocumento 			= pc2.IdTipoDocumento;
			$scope.pc2.envioFecha();
			parametrosSP.p_FechaInicio 				=  util.dateSPdate(pc2.FecInicio);
			parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pc2.FecTermino);
			
			
			
			params.p_TablaParametrosaux.push({Tipo: 'BATCH',NombreParametro : 'IdEmpresa',Valor : $scope.SesionIdEmpresa});
			
			var fs = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			params.p_TablaParametrosaux.push({Tipo: 'BATCH',NombreParametro : 'FechaSistema',Valor : fs});
			
			var fd = util.dateSPdate(pc2.FecInicio);
			params.p_TablaParametrosaux.push({Tipo: 'BATCH',NombreParametro : 'FechaInicio',Valor : fd});
			
			var fh = util.dateSPdate(pc2.FecTermino);
			params.p_TablaParametrosaux.push({Tipo: 'BATCH',NombreParametro : 'FechaFin',Valor : fh});
			
			params.p_TablaParametrosaux.push({Tipo: 'SOLICITUD',NombreParametro : 'FEDES',Valor : fd});
			params.p_TablaParametrosaux.push({Tipo: 'SOLICITUD',NombreParametro : 'FEHAS',Valor : fh});
			
			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
		};
		
		
		$scope.$watch('pc2.FecInicio', function(newval,oldval){
			pc2.fnVersionEsquema();
		},true);
			
		
		$scope.pc2.clear();
		
	});
}());