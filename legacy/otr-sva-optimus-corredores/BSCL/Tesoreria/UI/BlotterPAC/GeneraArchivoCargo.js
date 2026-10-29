angular.module('ModuloTesoreria').controller('GeneraArchivoCargoCtrl',function($scope,$translate,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria,bcsComunes,$timeout){
	
	/*********************************************** INFORMACION ****************************************************************
	* La funcion de este js es:																									*
	* 1.- Generar un archivo, en el cual se reuniran los mandatos que posean las caracteristicas ingresadas (Dias Descuento),   *
	* 	  el cual es enviado al banco. Es decir, si se ingresa Dias Descuento 5, el archivo contendra los IdentServicio de      *
	* 	  los mandatos que posean este valor. En caso de seleccionar TODOS, este archivo incluir� todos mandatos ingresados     *
	*     ya sean con Dia Descuento 5 o 25																						*
	* 																															*
	* 2.- Dependiendo del Dia Descuento Ingresado, se activaran los mandatos que cumplan con este valor. Es decir,				*
	* 	  si se ingresa Dias Descuento 25, todos los mandatos que posean este valor quedaran con estadoCargo ACTIVO (ACT),		*
	*     desactivando los que no lo sean.																						*
	*     																														*
	* Los mandatos con los que se trabaja son los que previamente fueron activados con el archivo Universo.						*						
	*****************************************************************************************************************************/
	
	/*********** PARAMETROS DE INICIO *************/
	$scope.winID 							= bcsComunes.ventanaWinId($scope);
	$scope.parametros 						= bcsComunes.ventanaParams($scope.winID);
	$scope.formData 						= {};
	$scope.oConfiguracionDiaDescuento 		= {};
	$scope.oConfiguracionDiaDescuento.conf 	= {"min" : 1, "max" : 31};
	$scope.formData.Todos 					= false;
	$scope.formData.habilitarGenerar 		= false;
	$scope.formData.FechaCargo				= util.dateInicioDia($scope.SesionFechaSistema);
	/**********************************************/
	
	/*************** WATCH's *********************/
	$scope.$watch('formData.Todos',function(newval,oldval){
					
		if (newval) {
			
			$scope.formData.habilitarDiasDescuento 	= true;
			$scope.formData.habilitarGenerar 		= true;
			$scope.formData.DiaDescuento 			= '';
			
		}else{
			$scope.formData.habilitarGenerar 		= false;
			$scope.formData.habilitarDiasDescuento 	= false;
	
		}
	});
	
	$scope.$watch('formData.DiaDescuento',function(newval,oldval){
		var todos = $scope.formData.Todos;
		
		if (newval != undefined && !todos) {
			
			var diasPermitidos 	= new Array('5','25');
			var valido 			= diasPermitidos.find(element => element == newval) != undefined ? true : false;
			
			if (!valido) {
				$scope.formData.DiaDescuento 		= '';
				$scope.formData.habilitarGenerar 	= false;
	
			}else{
				$scope.formData.habilitarGenerar 	= true;
			}
		}
	});
	
	$scope.$watch('formData.FechaCargo',function(newval,oldval){
		if (newval != undefined && newval != oldval){
			$scope.formData.FechaCargo = newval;
		}
	});
	/*********************************************/
	
	/*************** FUNCIONES ********************/

	$scope.ajustaVentana = function()
	{
		bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
	}
	
	$scope.generar = function(filtros) { 

			var dataCols = [];	
			
		
			dataCols.push(
					{
						IdNominaTransferencia : 1,
						IdBanco: 1,
						IdMoneda: 'CLP',
						IdMedioDePago: 'TRA',
						IdTipoTarea: 'GENERAR_ARCHIVO_CARGOS',
						IdAgenda: 0,
						Segundos: 1,
						DiaDescuento : ($scope.formData.Todos) ? 0 : $scope.formData.DiaDescuento,
						FechaCargo : util.dateSPdate($scope.formData.FechaCargo)
					});
			 
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_SOLICITARA_GENERACION_ARCHIVO_PAGO') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;

					//DATOS SERVICIO
					var datos = {};
					datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
					datos.p_IdUsuario               = $scope.SesionIdUsuario; 
					datos.p_IdModulo                = 'TES';
					datos.p_UsaReglasNegocio        = 'S';
					datos.p_ArchivosAGenerar	    = dataCols;

					bcsComunes.log().info(JSON.stringify(datos));
					
					/************************** GENERAR ARCHIVO **************************/
					bcsServiciosTesoreria.SolicitudGenerarArchivoPac(datos).$promise.then(
					
							function(result) {
								bcsComunes.ventanaOverlay($scope.winID,false);
								$scope.btnActivo = true;
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(result)
								},$scope.winID);
								
							},
							function(result){
								bcsComunes.ventanaOverlay($scope.winID,false);
								$scope.btnActivo = true;								
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_SOLICITUD_GENERACION_ARCHIVO_ERROR"),
									conflictos:util.parseServerResponse(result)
								},$scope.winID);
							});
					}
			});
		return;
	};
	/*********************************************/

	
	$scope.ajustaVentana();

	var firstHeightCrear = $('#Win_GeneraArchivoCargoPAC');
	firstHeightCrear.css('height', '160px');
});