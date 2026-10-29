angular.module('ModuloNormativa').controller('AgregarIndiceCtrl', 
		function($scope,bcsComunes,$translate,$resource, bcsServiciosNormativa){
	
	$scope.data = {}
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	$scope.Indice = $.extend({}, parametros.Indice);
	
	if (parametros.Indice) //modificacion
	{	
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_MODIFICAR_INDICE'));
		 
		$scope.data.nombreIndice        = parametros.Indice.Nombre;
		$scope.data.nombreCortoIndice   = parametros.Indice.NombreCorto;		
		$scope.data.tipoIndice 			= parametros.Indice.Tipo == "%" ? true : false;		
		
	}
	else //AGREGAR
	{	
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_AGREGAR_INDICE'));
	}
	
	// -----------------------------------------------
	// ----------------- Crear Indice ----------------
	// -----------------------------------------------
	var successCrearIndice = function (respuesta){
		//AGREGAR
		if($scope.Indice && respuesta.P_OUT_IDINDICE !== undefined && respuesta.P_OUT_IDINDICE.length > 0 && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_INDICE_INGRESAR_EXITO'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			
			bcsComunes.ventanaOverlay($scope.winID,false);
			$scope.limpiarFormulario();
			bcsComunes.ventanaCerrar($scope.winID);
		}
		//MODIFICAR
		else if ($scope.Indice && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaMsg({mensaje:$translate.instant('UI_INDICE_MODIFICADO_EXITO'),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			bcsComunes.ventanaCerrar($scope.winID);
		}
		//ERROR
		else {
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant('UI_INDICE_INGRESAR_ERROR'),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
			var i = 0;
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
            	bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
            }       
		}
	};
	var errorCrearIndice = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant('UI_INDICE_INGRESAR_ERROR'),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	$scope.AgregarIndice = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		if(parametros.Indice){
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'ILS',
					p_UsaReglasNegocio : 'S',
					p_TipoOperacion : 'M',
					p_tablaIndice :
						[{IdIndice: parametros.Indice.IdIndice, 
					      Nombre: $scope.data.nombreIndice,
					      NombreCorto: $scope.data.nombreCortoIndice,
					      Tipo: $scope.data.tipoIndice ? '%' : '',
						  Estado: 'VIG'
						}]
					
			};
			bcsComunes.log().info(JSON.stringify(parametrosSP));
			bcsServiciosNormativa.MantenedorIndice(parametrosSP, successCrearIndice, errorCrearIndice);
		}
		else {
			var parametrosSP = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'ILS',
					p_UsaReglasNegocio : 'S',
					p_TipoOperacion : 'I',
					p_tablaIndice :
						[{IdIndice: 1, 
						  Nombre: $scope.data.nombreIndice,
						  NombreCorto: $scope.data.nombreCortoIndice,
						  Tipo: $scope.data.tipoIndice ? '%' : '',
						  Estado: 'VIG'}]
				};
				bcsComunes.log().info(JSON.stringify(parametrosSP));
				bcsServiciosNormativa.MantenedorIndice(parametrosSP, successCrearIndice, errorCrearIndice);
		}
	};
	// -----------------------------------------------
	// --------------- Fin Crear Indice --------------
	// -----------------------------------------------
	
	$scope.habilitaGrabar = function(){
		if($scope.data.nombreIndice != "" && $scope.data.nombreCortoIndice != "" && 
		   $scope.data.nombreIndice != undefined && $scope.data.nombreCortoIndice != undefined)
			return true;
		else
			return false;
	};
	$scope.limpiarFormulario = function(){
		$scope.data.nombreIndice = '';
		$scope.data.nombreCortoIndice = '';
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
});