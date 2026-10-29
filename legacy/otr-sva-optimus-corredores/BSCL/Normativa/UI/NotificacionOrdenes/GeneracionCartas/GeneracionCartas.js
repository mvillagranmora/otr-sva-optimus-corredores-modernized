(function() {
	angular.module('ModuloNormativa').controller('GeneracionCartasCtrl',function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa, bcsServicioPlataforma){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+pc1.idVentana+"']");
		pc1.params = bcsComunes.ventanaParams(pc1.idVentana);
		
		if(pc1.params.PrevFecInicio !==undefined)
		{
			pc1.FecInicio 	 = pc1.params.PrevFecInicio;
		}else{
			pc1.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		};
			
		if(pc1.params.PrevFecTermino !==undefined)
		{
			pc1.FecTermino 	 = pc1.params.PrevFecTermino;
		}else{
			pc1.FecTermino 	 = util.dateInicioDia($scope.SesionFechaSistema);
		
		};
		if(pc1.params.PrevVentana !==undefined)
		{
			pc1.PrevVentana 	 = pc1.params.PrevVentana;
		};
		
		pc1.notificacion = {
			filtros: {
				IdTipoIndicador : { "field"   : "IdTipoIndicador","operator": "eq", "value"   : 74 },
				IdDetalleIndicador : { "field"   : "IdDetalleIndicador","operator": "neq", "value"   : 1 },
			}
		};
		
		var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
		var params = {
		p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
		{
			NombreParametro : 'idUsuario',
			Valor : $scope.SesionIdUsuario
		},{
			NombreParametro : 'schema',
			Valor :  Model.getSchema()
		}, {
			NombreParametro : 'grabar',
			Valor : 'false'
		}, {
			NombreParametro : 'reemplazar',
			Valor : 'false'
		},{
			NombreParametro : 'separadorListas',
			Valor : ';'
		},{
			NombreParametro : 'simboloDecimal',
			Valor : ','
		},{
			NombreParametro : 'simboloFecha',
			Valor :'dd/MM/yyyy'
		},{
			NombreParametro : 'aceptarFechasNull',
			Valor : 'true'
		},]};		
		
		pc1.modulo	='NOR';
		pc1.IdTipoDocumento = 'CIR1985MAS';
		pc1.salidaMensaje=''; 
		pc1.cmbbx		= {};
		var parametrosSP = {};
				
		pc1.MuestraMotivo=false;
		pc1.control	= {};
		

		/*********************************************
		 * DATA SOURCE
		 ********************************************/

		
		/*********************************************
		 * COMBOBOX
		 ********************************************/
	 
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		pc1.envioFecha = function(){
			var date = '';
			date= util.dateSPdate(pc1.Periodo);
			pc1.PeriodoEnv= date.substring(0,4)  + date.substring(5,7);
						
			var dateFin = new Date();
			dateFin 	 = pc1.Periodo;
			var ultimoDia = new Date(dateFin.getFullYear(), dateFin.getMonth() + 1, 0);
			pc1.PeriodoEnvFinal= util.dateInicioDia(ultimoDia);
			
		};
		
		var successCrearSolicitud = function(GeneracionCartas)
		{	
			//bcsComunes.ventanaOverlay(pc1.idVentana,true);
			if (GeneracionCartas.P_OUT_ERRORESCONTADOR == 0)
			{
								
				bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));
				pc1.salidaMensaje=$translate.instant("UI_" + pc1.salidaMensaje + "_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pc1.salidaMensaje);
				IdSolicitudCircular =GeneracionCartas.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				
				pc1.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
			/*	bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
										conflictos : util.parseServerResponse(GeneracionCartas)}, pc1.idVentana);
			*/
				bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));			
				bcsComunes.msg("error","error",pc1.salidaMensaje);
							
				var i = 0;
				for(i; i<GeneracionCartas.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + GeneracionCartas.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
			conflictos : util.parseServerResponse(GeneracionCartas)}, pc1.PrevVentana);
			
			bcsComunes.ventanaData(pc1.idVentana, GeneracionCartas);
			//bcsComunes.ventanaOverlay(pc1.idVentana,false);	
			//bcsComunes.ventanaCerrar(pc1.idVentana);
		};
		
		var errorCrearSolicitud = function(GeneracionCartas)
		{	

			pc1.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
								 conflictos : util.parseServerResponse(GeneracionCartas)}, pc1.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));			
			bcsComunes.msg("error","error",pc1.salidaMensaje);
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
		};
		
		
		
		$scope.pc1.generarCartas = function(){
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_GENERAR_CARTAS"),pc1.idVentana);// + ", " + $translate.instant("UI_PERIODO") + " : "  + pc1.PeriodoEnv);
			t .then(function(result)
			   // t.result.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
							parametrosSP= {}; 
							
					    	//Comunes
					    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
					    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
					    	parametrosSP.p_IdModulo 				=  pc1.modulo;
					    	
							parametrosSP.p_UsaReglasNegocio 		=  'N';
							
							parametrosSP.p_FechaInicio 				=  util.dateSPdate(pc1.FecInicio);
							parametrosSP.p_FechaTermino 		    =  util.dateSPdate(pc1.FecTermino);
							parametrosSP.p_Notificacion				= pc1.IdNotificacion;
							parametrosSP.p_TablaParametros 			= params.p_TablaParametrosaux;
							
							bcsComunes.msg('info', 'Generar', 'En proceso...');
							bcsComunes.ventanaOverlay(pc1.idVentana,true);
							pc1.salidaMensaje='GeneracionCartas';
							bcsServiciosNormativa.EjecucionOrdenesSolicitudCartas(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
							bcsComunes.ventanaCerrar(pc1.idVentana);
							//bcsComunes.ventanaOverlay(pc1.idVentana,true);
							//bcsComunes.ventanaOverlay(pc1.idVentana, false);
						 
				       };
				});			
			 
		};
		
	
		pc1.clear = function(){

			pc1.UIModo=pc1.params.PrevUIModo;	
		};
					

		pc1.cancelar = function() {
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};	
		
	
		pc1.clear();

	});
}());