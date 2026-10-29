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
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				pc1.salidaMensaje=$translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Codigo);
				bcsComunes.msg("error","error",pc1.salidaMensaje);
			}		
			
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje,
				conflictos : util.parseServerResponse(respuesta)}, $scope.solCir.PrevVentana);
			
			
			bcsComunes.ventanaData(pc1.idVentana, respuesta);
			
		};
		
		var errorCrearSolicitud = function(respuesta)
		{	

			pc1.salidaMensaje=$translate.instant("UI_ERROR");
			bcsComunes.ventanaMsg({ mensaje : pc1.salidaMensaje, 
								 conflictos : util.parseServerResponse(respuesta)}, pc1.idVentana);

			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","error",respuesta.P_OUT_ERRORESDETALLE[i].Codigo);
			bcsComunes.ventanaOverlay(pc1.idVentana, false);
		};
		
		
		
		
		$scope.pc1.guardarCambios = function(){
			
			parametrosSP= {}; 
			
	    	//Comunes
	    	parametrosSP.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
	    	parametrosSP.p_IdUsuario              	=  $scope.SesionIdUsuario;
			
			//Especificos
			
	    	parametrosSP.p_IdCuadratura		= pc1.control.IdCuadratura;
	    	
			parametrosSP.p_CantidadUnidadesNominal		= pc1.control.CantidadUnidadesNominal;
			parametrosSP.p_TipoInstrumento				= pc1.control.TipoInstrumento;
			//parametrosSP.p_TipoUnidades					= pc1.control.TipoUnidades;
			parametrosSP.p_Valorizacion					= pc1.control.Valorizacion;
			parametrosSP.p_CarteraPropia				= pc1.control.CarteraPropia;
			parametrosSP.p_CustodiaTerceros				= pc1.control.CustodiaTerceros;
			parametrosSP.p_CustodiaPropiaDCV			= pc1.control.CustodiaPropiaDCV;
			parametrosSP.p_CustodiaTercerosDCV			= pc1.control.CustodiaTercerosDCV;
			parametrosSP.p_CustodiaSociedadEmisora		= pc1.control.CustodiaSociedadEmisora;
			parametrosSP.p_CustodiaIntermediario		= pc1.control.CustodiaIntermediario;
			parametrosSP.p_CustodiaBolsas				= pc1.control.CustodiaBolsas;
			parametrosSP.p_CustodiaOtros				= pc1.control.CustodiaOtros;
			parametrosSP.p_Habilitado					= pc1.control.Habilitado === true ? 'S':'N';

			bcsComunes.msg('info', 'Carga', 'En proceso...');
			bcsComunes.ventanaOverlay(pc1.idVentana,true);
		
			bcsServiciosNormativa.Cir1720EditarDatos(parametrosSP,successCrearSolicitud,errorCrearSolicitud);
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

	});
}());