(function() {
	angular.module('ModuloFFM').controller('PanelPrincipalCtrl',function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioFondosMutuos){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.params = bcsComunes.ventanaParams(pc1.idVentana);
		
		pc1.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		pc1.control={};
		pc1.FecInicio=$scope.solCir.Prevfecha;
		var parametrosSP = {};
		pc1.modulo='FFM';
		pc1.cmbbx		= {};
		
		pc1.fecha = util.dateInicioDia($scope.SesionFechaSistema);
		/*********************************************
		 * COMBOBOX
		 *********************************************/

		pc1.cmbbx.Motivo={
				filtros : {
					IdEstado : { field: "IdEstado", operator: "eq", value: 'ACT' }
				},
			};		
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		pc1.cancelar = function() {
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};	
		
		pc1.anular = function(){
			if (pc1.fecha==undefined){return;};
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA_EL_PROCESO_DE" ) + " "  + $translate.instant("UI_ANULACION" ) +" ?",pc1.idVentana);
			t .then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
						 
						 
						 var datos = {
								 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
								 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
								 p_IdModulo : 'FMM',
									 
								 p_Traspaso : [{
						    		 	IdTraspaso		      		:	pc1.params.IdTraspaso,
						    		 	IdMotivoAnulacion     		:	(pc1.cmbbx.IdMotivo) ? pc1.cmbbx.IdMotivo : '',
						    		 	FechaAnulacion          	:	util.dateSPdate(pc1.fecha)
								 }]
								 
						 }
						 

						bcsComunes.msg('info', 'Anular', 'En proceso...');
						bcsComunes.ventanaOverlay(pc1.idVentana,true);
						pc1.salidaMensaje='ANULAR';
						bcsServicioFondosMutuos.AnularTraspaso(datos,successCrearSolicitud,errorCrearSolicitud);
						bcsComunes.ventanaCerrar(pc1.idVentana);

						 
				       };
				});			
			 
		};
		
		var successCrearSolicitud = function(GeneracionCartas)
		{	
			if (GeneracionCartas.P_OUT_ERRORESCONTADOR == 0)
			{
								
				bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));
				pc1.salidaMensaje=$translate.instant("UI_ANULACION_REALIZADA");
				
				bcsComunes.msg("info","info", pc1.salidaMensaje);
				
			}
			else
			{	
				
				//pc1.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
				//bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));			
				//bcsComunes.msg("error","error",pc1.salidaMensaje);
							
				var i = 0;
				for(i; i<GeneracionCartas.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + GeneracionCartas.P_OUT_MENSAJES[i].Descripcion);       
				}            
			}
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,conflictos : util.parseServerResponse(GeneracionCartas)}, pc1.PrevVentana);
			
			//bcsComunes.ventanaData(pc1.idVentana, GeneracionCartas);
		};
		
		var errorCrearSolicitud = function(GeneracionCartas)
		{	

		
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ANULACION_TRASPASO"),conflictos:util.parseServerResponse(GeneracionCartas)},pc1.idVentana);
			//bcsComunes.setConflictos(util.parseServerResponse(GeneracionCartas));			
			//bcsComunes.msg("error","error",pc1.salidaMensaje);
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
		};
		
		

	});
}());