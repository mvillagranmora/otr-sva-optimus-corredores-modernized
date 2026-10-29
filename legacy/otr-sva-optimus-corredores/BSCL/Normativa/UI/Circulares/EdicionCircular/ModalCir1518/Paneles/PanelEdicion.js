(function() {
	angular.module('ModuloNormativa').controller('PanelEdicionCtrl',function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc1 = this;
		pc1.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc1.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
	    pc1.TipoArchivo			= 'pdf';
	    pc1.IdVersionEsquema 	= $scope.solCir.IdVersionEsquema;
		
		pc1.IdTipoDocumento = 'CIRCNomina';
		pc1.salidaMensaje=''; 
		var parametrosSP = {};
		$scope.dataTemplate	= {};
		
		//pc1.dataParametro	= {};
		pc1.control	= $scope.solCir.Seleccion[0] ;//{};
		pc1.control.TodosCli=true;
		
		$scope.pc1.envioFecha = function(){
			var date = '';
			date= util.dateSPdate($scope.solCir.Periodo);
			pc1.Periodo= date.substring(0,4)  + date.substring(5,7);
		};
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
		
		var successCrearSolicitud = function(respuesta)
		{	
			//bcsComunes.ventanaOverlay(pc1.idVentana,true);
			if (respuesta.P_OUT_ERRORESCONTADOR == 0)
			{
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pc1.salidaMensaje=$translate.instant("UI_MODIFICACION_INGRESO_EXITO");
				
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
			//bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
			//	conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana),true,true;
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
				conflictos : util.parseServerResponse(respuesta)}, $scope.solCir.PrevVentana);
			
			
			
			//bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
			//conflictos : util.parseServerResponse(GeneracionCartas)},$scope.solCir.idVentana);
			
			
			bcsComunes.ventanaData(pc1.idVentana, respuesta);
			//bcsComunes.ventanaCerrar(pc1.idVentana);
			
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
			
			//Especificos
			
	    	parametrosSP.p_IdCuadratura		= pc1.control.IdCuadratura;
	 		parametrosSP.p_CantidadCustodia	= pc1.control.CantidadCustodia;
			parametrosSP.p_CantidadGarantia	= pc1.control.CantidadGarantia;
			parametrosSP.p_CantidadPlazoVenta	=pc1.control.CantidadPlazoVenta;
			parametrosSP.p_CantidadPlazoCompra=pc1.control.CantidadPlazoCompra;
			parametrosSP.p_CantidadTotal=pc1.control.CantidadTotal;
			parametrosSP.p_CantidadDCVRelacionado=pc1.control.CantidadDCVRelacionado;
			parametrosSP.p_CantidadDCVTerceros=pc1.control.CantidadDCVTerceros;
			parametrosSP.p_CantidadDCVCuentaPropia=pc1.control.CantidadDCVCuentaPropia;
			parametrosSP.p_CantidadBCS=pc1.control.CantidadBCS;
			parametrosSP.p_CantidadBEC=pc1.control.CantidadBEC;
			parametrosSP.p_CantidadBOVALPO=pc1.control.CantidadBOVALPO;
			parametrosSP.p_CantidadEmisores=pc1.control.CantidadEmisores;
			parametrosSP.p_CantidadTransitoIngreso=pc1.control.CantidadTransitoIngreso;
			parametrosSP.p_CantidadTransitoEgreso=pc1.control.CantidadTransitoEgreso;
			parametrosSP.p_Diferencia=pc1.control.Diferencia;
			parametrosSP.p_ExplicacionDescuadratura=pc1.control.ExplicacionDescuadratura;
					
			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsComunes.ventanaOverlay(pc1.idVentana,true);
		
			bcsServiciosNormativa.Cir1518EditarDatos(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
			bcsComunes.ventanaCerrar(pc1.idVentana);
	       
			 
		};
		
	
	
		$scope.pc1.clear = function(){

			pc1.UIModo=$scope.solCir.params.PrevUIModo;	
		};
					

		pc1.cancelar = function() {
			bcsComunes.ventanaCerrar(pc1.idVentana);
		};	
		
	
		$scope.$watch('pc1.control.notificarNO',pc1.fnnotificar);
		
		
		$scope.$watch('pc1.control.notificarSI',function(newval,oldval){
		if (pc1.control.notificarSI)
		{
			pc1.control.notificarNO=false;
		}else{
			pc1.control.notificarNO=true;
			};

		});
		
		$scope.$watch('pc1.control.notificarNO',function(newval,oldval){
			if (pc1.control.notificarNO)
			{
				pc1.control.notificarSI=false;
			}else{
				pc1.control.notificarSI=true;
			};
				
		});
		
		$scope.pc1.clear();


		$scope.$watch('pc1.control.CantidadTransitoIngreso',function(){
			pc1.control.Diferencia = pc1.control.CantidadTransitoIngreso - pc1.control.CantidadTransitoEgreso;
			pc1.control.Diferencia = util.bcs_formato(pc1.control.Diferencia,'GEN','Monto','CLP','','INDICE').valor;
		});

		$scope.$watch('pc1.control.CantidadTransitoEgreso',function(){
            pc1.control.Diferencia = pc1.control.CantidadTransitoIngreso - pc1.control.CantidadTransitoEgreso;
            pc1.control.Diferencia = util.bcs_formato(pc1.control.Diferencia,'GEN','Monto','CLP','','INDICE').valor;
   		});
	});
}());