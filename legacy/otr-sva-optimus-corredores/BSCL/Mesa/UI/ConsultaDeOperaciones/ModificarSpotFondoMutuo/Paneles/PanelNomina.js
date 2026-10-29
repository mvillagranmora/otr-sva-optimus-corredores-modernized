(function() {
	angular.module('ModuloMesa').controller('PanelNominaCtrl',function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		pc1.control={};
		pc1.FecInicio=$scope.solCir.Prevfecha;
		var parametrosSP = {};
		pc1.modulo='SPT';
		pc1.TipoOperacion =$scope.solCir.params.TipoOperacion.idActual;
		pc1.FondoMutuoDolar = $scope.solCir.params.FondoMutuoDolar;

		/*********************************************
		 * FUNCIONES
		 ********************************************/
		pc1.cancelar = function() {
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};	
		
		pc1.guardarCambios = function(){
			
					
			if (pc1.kngTipoCambioCierre==undefined || pc1.kngTipoCambioCosto==undefined)
			{
				bcsComunes.msg('info', 'info', 'Debe ingresar T/C Cierre y T/C Costo');
				return;
			};
			
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_GRABAR_CAMBIOS" ) + " ?",pc1.idVentana);
			t .then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
							parametrosSP= {}; 
							
					    	//Comunes
					    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
					    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
					    	parametrosSP.p_IdModulo 				=  pc1.modulo;
							parametrosSP.p_UsaReglasNegocio 		=  'S';
							parametrosSP.p_FechaOperacion			=  util.dateSPdate(pc1.FecInicio);
							parametrosSP.p_TipoCambioCierre			=  pc1.kngTipoCambioCierre;
							

							parametrosSP.p_TipoCambioCosto			=  pc1.kngTipoCambioCosto;
							
							
							parametrosSP.p_TipoOperacion			=  pc1.TipoOperacion;
							parametrosSP.p_FondoMutuoDolar			=  pc1.FondoMutuoDolar;
							bcsComunes.msg('info', 'Grabar', 'En proceso...');
							bcsComunes.ventanaOverlay(pc1.idVentana,true);
							pc1.salidaMensaje='CAMBIO';
							bcsServiciosMesa.ModificarSpotFondoMutuo(parametrosSP,successCrearSolicitud,errorCrearSolicitud);

						 
				       };
				});			
			 
		};
		
		var successCrearSolicitud = function(GeneracionCartas)
		{	
			if (GeneracionCartas.P_OUT_ERRORESCONTADOR == 0)
			{
								
				bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));
				pc1.salidaMensaje=$translate.instant("UI_" + pc1.salidaMensaje + "_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pc1.salidaMensaje);
				
			}
			else
			{	
				
				pc1.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
				bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));			
				bcsComunes.msg("error","error",pc1.salidaMensaje);
							
				var i = 0;
				for(i; i<GeneracionCartas.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + GeneracionCartas.P_OUT_MENSAJES[i].Descripcion);       
				}            
			}
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
			conflictos : util.parseServerResponse(GeneracionCartas)}, pc1.PrevVentana);
			bcsComunes.ventanaData(pc1.idVentana, GeneracionCartas);
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};
		
		var errorCrearSolicitud = function(GeneracionCartas)
		{	

			pc1.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
								 conflictos : util.parseServerResponse(GeneracionCartas)}, pc1.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));			
			bcsComunes.msg("error","error",pc1.salidaMensaje);
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};
		
		

	});
}());