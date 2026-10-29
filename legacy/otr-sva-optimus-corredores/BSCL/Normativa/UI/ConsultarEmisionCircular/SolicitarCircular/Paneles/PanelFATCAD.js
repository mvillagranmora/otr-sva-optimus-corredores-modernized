(function() {
	angular.module('ModuloNormativa').controller('PanelFATCADCtrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pfatcad = this;
		pfatcad.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pfatcad.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		

		pfatcad.TipoArchivo			= 'pdf';
		pfatcad.IdVersionEsquema 	= '';
	
		pfatcad.IdTipoDocumento = 'FATCAD';
		
		$scope.filtros = {};
		$scope.dataTemplate	= {};
		
		$scope.filtros.FecEjec   = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.filtros.FecEnrol  = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		$scope.filtros.FecDesde  = "";
		$scope.filtros.FecHasta  = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		/*********************************************
		 * COMBOBOX
		 ********************************************/
		
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		$scope.activadoCli =true;
		
	/*	$scope.$watch('TodosCli',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.activadoCli = false;  
					break;					
				case false: 
					$scope.activadoCli = true;
					break; 
			}			
		});
		*/
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
		
	/*	var ListadoRuts = [];
		var ListadoRutAuts = [];
		var ListadoNemos = [];
		var ListadoRutEmpl = [];
	*/	
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		$scope.pfatcad.clear = function(){
			pfatcad.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
						
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
			bcsComunes.ventanaOverlay(pfatcad.idVentana, false);
			bcsComunes.ventanaData(pfatcad.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pfatcad.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
			bcsComunes.ventanaOverlay(pfatcad.idVentana, false);
		};
		//Funcion Obtener version del Esquema
		pfatcad.fnVersionEsquema = function() {
			if (pfatcad.FecInicio !=undefined)
				{
						var fd = util.dateSPdate(pfatcad.FecInicio);
						var paramServicio = {};
						paramServicio.p_TipoDocumento = pfatcad.IdTipoDocumento;
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
								pfatcad.IdVersionEsquema=respuesta.P_OUT_VERSIONESQUEMA[0].P_OUT_VERSION;
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
		
		
			
		pfatcad.solicitar = function() {
	    	var parametrosSP = {};
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  $scope.solCir.modulo;
			parametrosSP.p_FechaIngresoSolicitud 	=  $scope.solCir.fechaInicioSol;
			parametrosSP.p_CreaParam 				= 'S';
			parametrosSP.p_IdTipoDocumento 			=  'FATCAD';  


			
			var fd = $scope.SesionFechaSistema || '';
			//params.p_TablaParametrosaux.push({NombreParametro : 'fechaDesde',Valor : fd});
			

			parametrosSP.p_TablaParametros = params.p_TablaParametrosaux;
			
			
			//Especificos  

			//parametrosSP.p_IdCliente                = $scope.oConfiguracionSocioNegocio.seleccionado.IdCliente;
			
			
			if($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente != undefined)
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : $scope.oConfiguracionSocioNegocio.seleccionado.IdCliente });

			}
			else
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IDCLI' , Valor : 0 });

			}
			
			if($scope.SesionFechaSistema != undefined)
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FINI' , Valor : util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)) });

			}

		//	parametrosSP.p_FechaInicio 				= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		
		
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

			//parametrosSP.p_FecEjec					=  util.dateSPdate($scope.filtros.FecEjec) || '';
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEJEC' , Valor : '0001-01-01' });

			//	parametrosSP.p_FecEjec				=  '0001-01-01';

			}
			
			if($scope.filtros.FecEnrol != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FENR' , Valor : util.dateSPdate($scope.filtros.FecEnrol) });

				//parametrosSP.p_FecEnrol					=  util.dateSPdate($scope.filtros.FecEnrol)|| '';
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FENR' , Valor : '0001-01-01' });
				//parametrosSP.p_FecEnrol				=  '0001-01-01';

			}
			
			if($scope.filtros.FecDesde != undefined && $scope.filtros.FecDesde != "")
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEDES' , Valor : util.dateSPdate($scope.filtros.FecDesde) });

			//	parametrosSP.p_FecDesde				=  util.dateSPdate($scope.filtros.FecDesde)|| '0001-01-01';
			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEDES' , Valor : '0001-01-01' });

				//parametrosSP.p_FecDesde				=  '0001-01-01';

			} 
			 
			if($scope.filtros.FecHasta != undefined)
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEHAS' , Valor : util.dateSPdate($scope.filtros.FecHasta) });
				//parametrosSP.p_FecHasta				=  util.dateSPdate($scope.filtros.FecHasta)|| '9999-01-01';
			}
			else
			{		
        		parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'FEHAS' , Valor : '9999-01-01' });

				//parametrosSP.p_FecHasta				=  '9999-01-01';

			}
			
			//parametrosSP.p_UsoFecha					=  $scope.filtros.UsoFecha || false;
			
			//parametrosSP.p_UsoFechaInd				=  $scope.filtros.UsoFechaInd || false;
			
			
			if($scope.ClasificacionFatca.seleccionado.IdClasificacionFatca != undefined)
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'CFAT' , Valor : $scope.ClasificacionFatca.seleccionado.IdClasificacionFatca });

			//parametrosSP.p_ClasificacionFatca 		=  $scope.ClasificacionFatca.seleccionado.IdClasificacionFatca;

			}
			else
			{
				
				
			}
			
			if($scope.IndicadorFatca.seleccionado.IdClasificacionFatca != undefined)
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'IFAT' , Valor :$scope.IndicadorFatca.seleccionado.IdClasificacionFatca });

				
			//parametrosSP.p_IndicadorFatca 		=  $scope.IndicadorFatca.seleccionado.IdClasificacionFatca;

			}
			else
			{
				
				
			}
			
			if($scope.TipoPersona.seleccionado.IdTipoPersona != undefined)
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'TP' , Valor :$scope.TipoPersona.seleccionado.IdTipoPersona });

				
			//parametrosSP.p_TipoPersona 		=  $scope.TipoPersona.seleccionado.IdTipoPersona;

			}
			else
			{
				
				
			}
			
			if($scope.filtros.registroIndicios != undefined)
			{
			parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RIND' , Valor : true });

				
			//parametrosSP.p_registroIndicios 		=  $scope.TipoPersona.seleccionado.IdTipoPersona;

			}
			else
			{
				parametrosSP.p_TablaParametros.push({Tipo: 'SOLICITUD',NombreParametro :'RIND' , Valor : false });
				
				
			}
			
			

			//Tipo Listas 
		/*	parametrosSP.p_ListaRut 				= ListadoRuts 		|| []; 
			parametrosSP.p_ListaRutAut			 	= ListadoRutAuts 	|| [];
			parametrosSP.p_ListaNemo 				= ListadoNemos 		|| [];
			parametrosSP.p_ListaRutEmpl 			= ListadoRutEmpl 	|| [];
			*/
			//Creacion Documento
			//parametrosSP.p_IdTipoDocumento 			= pfatcad.IdTipoDocumento;
			//parametrosSP.p_IdEstadoDocumento 		= $scope.solCir.IdEstadoDocumento;

				
			parametrosSP.p_TablaParametros.push({Tipo: 'EsPauta',NombreParametro :'RIND' , Valor : 'N' });



			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsServiciosNormativa.CircularCrearSolicitud(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			
	      
		};
	 

	});
	
	
	

	
	
	
}());