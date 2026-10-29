angular.module('ModuloNormativa').controller('AgregarInformeCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosNormativa){
	
	$scope.data = {}
	
	$scope.winID = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	$scope.Informe = $.extend({}, parametros.Informe);
	
	if (parametros.Informe) //modificacion
	{	
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_MODIFICAR_INFORME'));
		 
		$scope.data.NombreInforme = parametros.Informe.Nombre;
		$scope.IdEmpresa = parametros.Informe.IdEmpresa;
	}
	else //AGREGAR
	{	
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_AGREGAR_INFORME'));
	}
	
	// -----------------------------------------------
	// ----------------- Mantiene Informe ------------------
	// -----------------------------------------------
	var successAgregaInforme = function (respuesta){
		//SUCCESS
		if(!parametros.Informe && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));

			bcsComunes.ventanaMsg({
				mensaje: $translate.instant('UI_INFORME_INGRESAR_EXITO'),
				conflictos: util.parseServerResponse(respuesta),
			},$scope.winID);
			
			bcsComunes.ventanaCerrar($scope.winID);
			
		}else if(parametros.Informe && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant('UI_INFORME_MODIFICADO_EXITO'),
				conflictos: util.parseServerResponse(respuesta),
			},$scope.winID);
			
			bcsComunes.ventanaCerrar($scope.winID);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant('UI_INFORME_INGRESAR_ERROR'),
				conflictos: util.parseServerResponse(respuesta),
			},$scope.winID);
			
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
		
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	var errorAgregaInforme = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		
		bcsComunes.ventanaMsg({
			mensaje: $translate.instant('UI_INFORME_INGRESAR_ERROR'),
			conflictos: util.parseServerResponse(respuesta),
		},$scope.winID);
		
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	$scope.AgregarInforme = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		if(parametros.Informe){
			var parametrosSP = {
					p_IdTipoInforme : parametros.Informe.IdTipoInforme,
					p_IdEmpresa 	: $scope.data.IdEmpresa.seleccionado.IdEmpresa,
					p_Nombre 		: $scope.data.NombreInforme,
					p_Estado		: 'VIG',
					p_TipoOperacion : 'M',
					p_IdUsuario 	: $scope.SesionIdUsuario					
			};
		}
		else {
			var parametrosSP = {
					p_IdTipoInforme : 0,
					p_IdEmpresa 	: $scope.data.IdEmpresa.seleccionado.IdEmpresa,
					p_Nombre 		: $scope.data.NombreInforme,
					p_Estado		: 'VIG',
					p_TipoOperacion : 'I',
					p_IdUsuario 	: $scope.SesionIdUsuario					
			};
		}	
		
		bcsComunes.log().info(JSON.stringify(parametrosSP));
		bcsServiciosNormativa.MantieneInforme(parametrosSP, successAgregaInforme, errorAgregaInforme);
	};
	// -----------------------------------------------
	// --------------- Fin Crear Informe ----------------
	// -----------------------------------------------
	
	//---------------------------------------------
    //------------------ EVENTOS ------------------
    //---------------------------------------------
	$scope.$watch('data.IdEmpresa.seleccionado',function(newval,oldval){		
		if($scope.data.IdEmpresa != undefined && $scope.data.NombreInforme != '' && $scope.data.NombreInforme != undefined){
			if($scope.data.IdEmpresa.seleccionado != false )
				$scope.data.Habilitado = true;
		}	
		else
			$scope.data.Habilitado = false;
	});
	
	$scope.$watch('data.NombreInforme',function(newval,oldval) {
		if($scope.data.IdEmpresa != undefined && $scope.data.NombreInforme != '' && $scope.data.NombreInforme != undefined){
			if($scope.data.IdEmpresa.seleccionado != false )
				$scope.data.Habilitado = true;
		}
		else
			$scope.data.Habilitado = false;
	});
	$scope.limpiarFormulario = function(){
		$scope.data.NombreInforme = '';
		$scope.data.IdEmpresa.seleccionado = 0;
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
});