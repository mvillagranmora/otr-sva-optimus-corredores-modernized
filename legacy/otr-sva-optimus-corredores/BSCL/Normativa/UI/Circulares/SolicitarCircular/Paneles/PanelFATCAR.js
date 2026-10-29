(function() {
	angular.module('ModuloNormativa').controller('PanelFATCARCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pfatcar = this;
		pfatcar.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pfatcar.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		pfatcar.TipoArchivo			= 'pdf';
		pfatcar.IdVersionEsquema 	= '';
	
		pfatcar.IdTipoDocumento = 'FATCAR';
		
		$scope.filtros = {};
		$scope.dataTemplate	= {};
		
		$scope.filtros.FecEjec   = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.filtros.FecEnrol  = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.filtros.FecDesde  = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.filtros.FecHasta  = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		$scope.filtros.FecEjec = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");//util.dateInicioDia($scope.SesionFechaSistema);
		$scope.activadoCli =true;
		
		var p_TablaParametrosaux ;
		var params = {
		p_TablaParametrosaux : [ 
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
		$scope.pfatcar.clear = function(){
			pfatcar.FecInicio 	 =util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
						
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
			bcsComunes.ventanaOverlay(pfatcar.idVentana, false);
			bcsComunes.ventanaData(pfatcar.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pfatcar.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pfatcar.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pfatcar.fnVersionEsquema = function() {
			if (pfatcar.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pfatcar.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pfatcar.IdTipoDocumento;
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
								pfatcar.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		
			
		pfatcar.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol;
			parametrosSP.p_CreaParam 				= 'S';
			parametrosSP.p_IdTipoDocumento 			=  'FATCAR';  
			
			var fd = $scope.SesionFechaSistema || '';

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
						
			//Especificos  
			if($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : $scope.oConfiguracionSocioNegocio.seleccionado.IdCliente });

			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : 0 });
			}
			
			if($scope.filtros.MontoMin != undefined && $scope.filtros.MontoMin !='')
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'MMIN' , Valor : $scope.filtros.MontoMin || 0 });
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'MMIN' , Valor : 0 });
			}
		
		
			if($scope.filtros.MontoMax != undefined && $scope.filtros.MontoMax !='')
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'MMAX' , Valor :$scope.filtros.MontoMax || 0});
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'MMAX' , Valor : 0 });
			}
		
			
			if($scope.filtros.FecEjec != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEJEC' , Valor : util.dateSPdate($scope.filtros.FecEjec) });
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEJEC' , Valor : '0001-01-01' });
			}
			
			if($scope.filtros.FecEnrol != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FENR' , Valor : util.dateSPdate($scope.filtros.FecEnrol) });
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FENR' , Valor : '0001-01-01' });
			}
			
			if($scope.ClasificacionFatca.seleccionado.IdClasificacionFatca != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CFAT' , Valor : $scope.ClasificacionFatca.seleccionado.IdClasificacionFatca });
			}
			
			if($scope.filtros.indicadorFatca)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IFAT' , Valor :'S' });
			}else{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IFAT' , Valor :'N' });
			}
			
			if($scope.TipoPersona.seleccionado.IdTipoPersona != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TP' , Valor :$scope.TipoPersona.seleccionado.IdTipoPersona });
			}
			
			if($scope.filtros.registroIndicios)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RIND' , Valor : 'S' });
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RIND' , Valor : 'N' });
			}
				
			parametrosSP.p_TablaParametros.push({Tipo: 'EsPauta',NombreParametro :'RIND' , Valor : 'N' });

			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
		};
	});
}());