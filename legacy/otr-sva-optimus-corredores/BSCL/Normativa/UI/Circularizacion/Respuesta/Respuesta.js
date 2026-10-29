(function() {
	angular.module('ModuloNormativa').controller('RespuestaCtrl',function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+pc1.idVentana+"']");
		pc1.params = bcsComunes.ventanaParams(pc1.idVentana);
		
		pc1.Seleccion = pc1.params.PrevSeleccion;
		pc1.Motivo=pc1.Seleccion[0].Motivo;
		pc1.modulo	='NOR';
		pc1.IdTipoDocumento = 'CIRCNomina';
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
		pc1.cmbbx.EstadoNormativa={
			filtros : {
				NombreEntidad : { field: "NombreEntidad", operator: "eq", value: 'CIRCULARIZACION-RESPUESTA' }
			}
		};	 
		/*********************************************
		 * VARIABLES PARAMETROS
		 ********************************************/
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		var successCrearSolicitud = function(respuesta)
		{	
			//bcsComunes.ventanaOverlay(pc1.idVentana,true);
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
								
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pc1.salidaMensaje=$translate.instant("UI_" + pc1.salidaMensaje + "_INGRESO_EXITO");
				
				bcsComunes.msg("info","info", pc1.salidaMensaje);
				IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
				//$scope.data.limpiarFormulario();
			}
			else
			{	
				
				pc1.salidaMensaje=$translate.instant("UI_ERROR_INGRESO");
			/*	bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
										conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);
			*/
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
				bcsComunes.msg("error","error",pc1.salidaMensaje);
							
				var i = 0;
				for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
				{
					bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
				}            
			}
			/*
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
				conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana),true,true;
			*/
			bcsComunes.ventanaData(pc1.idVentana, respuesta);
			bcsComunes.ventanaCerrar(pc1.idVentana);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	

			pc1.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
								 conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",pc1.salidaMensaje);
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
		};
		
		
		
		
		$scope.pc1.guardarCambios = function(){
			
			parametrosSP= {}; 
			
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
	    	parametrosSP.p_IdModulo 				=  pc1.modulo;
	    	
			parametrosSP.p_UsaReglasNegocio 		=  'N';
			
			parametrosSP.p_IdEstadoRespuesta 		=  pc1.cmbbx.IdEstadoNormativa;
			
			if (pc1.MuestraMotivo==true){
				parametrosSP.p_Motivo 		=  pc1.Motivo || '';	
			};
			
			/*
			if (pc1.control.notificarSI)
			{
				parametrosSP.p_Notificar					=	'SI';	
			}else{
				parametrosSP.p_Notificar					=	'NO';
			};
			*/
			
			//Especificos
			fnOntenerParametros();
					
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsComunes.ventanaOverlay(pc1.idVentana,true);
			pc1.salidaMensaje='RESPUESTA';
			bcsServiciosNormativa.CircularizacionModificacionNomina(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			bcsComunes.ventanaOverlay(pc1.idVentana,false);
	       
			 
		};
		
		function fnOntenerParametros() {
			 var registroCambio = [];
			 $.each(pc1.Seleccion,function(key,value)
      	                {       		     
      	    	           registroCambio.push(
	      		             {   
	      			           IdNomina : pc1.Seleccion[key].IdNomina,
	      			           
	      		             });
      	                });
      	    
			parametrosSP.p_TablaNomina = registroCambio;
			
		};	

	
		pc1.clear = function(){

			pc1.UIModo=pc1.params.PrevUIModo;	
		};
					

		pc1.cancelar = function() {
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};	
		
	
		$scope.$watch('pc1.cmbbx.IdEstadoNormativa',function(newval,oldval){
			if (pc1.cmbbx.IdEstadoNormativa!==0 && pc1.cmbbx.IdEstadoNormativa!=='RZD')
			{
				pc1.MuestraMotivo=false;
			}else{
				pc1.MuestraMotivo=true;
			};
				
		});
		
		
	
		pc1.clear();

	});
}());