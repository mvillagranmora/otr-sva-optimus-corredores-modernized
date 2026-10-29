angular.module('ModuloNormativa').controller('AgregarConceptoCtrl',
		function($scope,bcsComunes,$translate,$resource,bcsServiciosNormativa){
	
	$scope.data = {}
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var tipoOperacion = parametros.tipoOperacion;
	
	if(tipoOperacion === 'M'){
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_MODIFICAR_CONCEPTO'));
		$scope.idConcepto = parametros.IdConcepto; 
		$scope.nombreConcepto = parametros.Nombre;
		$scope.nombreCortoConcepto = parametros.NombreCortoConcepto;
	}else{
		bcsComunes.ventanaTitulo($scope.winID, $translate.instant('UI_ILS_AGREGAR_CONCEPTO'));
	}
	
	$scope.MantenedorConcepto = function() {
		var tablaConcepto	= [];
		if(tipoOperacion === 'I'){
			if($scope.nombreConcepto === undefined || $scope.nombreConcepto === '')
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Nombre.");
				return;
			}
			if($scope.nombreCortoConcepto === undefined || $scope.nombreCortoConcepto === '')
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Nombre Corto.");
				return;
			}
			tablaConcepto.push({
				IdConcepto 	: 0 
			  , Nombre    	: $scope.nombreConcepto
			  , NombreCorto	: $scope.nombreCortoConcepto
			});
		}else{
			if($scope.idConcepto === undefined)
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Concepto.");
				return;
			}
			if($scope.nombreConcepto === undefined || $scope.nombreConcepto === '')
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Nombre.");
				return;
			}
			if($scope.nombreCortoConcepto === undefined || $scope.nombreCortoConcepto === '')
			{
				bcsComunes.msg("warning", "Error", "Debe seleccionar un Nombre Corto.");
				return;
			}
			tablaConcepto.push({
				IdConcepto 	: $scope.idConcepto 
			  , Nombre    	: $scope.nombreConcepto
			  , NombreCorto	: $scope.nombreCortoConcepto
			});
		}

		var dataParametros = {
			p_IdEmpresa 					: $scope.SesionIdEmpresa,
			p_IdUsuario 					: $scope.SesionIdUsuario,
			p_IdModulo  					: 'NOR',
			p_UsaReglasNegocio 				: 'N',
			p_TipoOperacion					: tipoOperacion,
			p_tablaConcepto					: tablaConcepto
		};
		
		var mensajeOk = '';
   		var mensajeErr = '';
   		if(tipoOperacion === 'I'){
   			mensajeOk = 'UI_CONCEPTO_INGRESAR_EXITO';
	   		mensajeErr = 'UI_CONCEPTO_INGRESAR_ERROR';	
   		}else if(tipoOperacion === 'M'){
   			mensajeOk = 'UI_CONCEPTO_MODIFICADO_EXITO';
	   		mensajeErr = 'UI_CONCEPTO_MODIFICADO_ERROR';
   		}else{
   			mensajeOk = 'UI_CONCEPTO_ELIMINAR_EXITO';
	   		mensajeErr = 'UI_CONCEPTO_ELIMINAR_ERROR';
   		}
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		bcsServiciosNormativa.MantenedorConcepto(dataParametros).$promise.then(function(respuesta){
   			
	   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	   		
			//se valido la orden
	   		bcsComunes.ventanaOverlay($scope.winID,false);
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
			{				
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant(mensajeOk),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaCerrar($scope.winID);
			}
			else
		    {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.ventanaMsg({
					mensaje: "Error",
					conflictos: util.parseServerResponse(respuesta),
					errores : [ {
				                 Codigo : 'COD',
				                 Descripcion : $translate.instant(mensajeErr)
				                } 
					          ]
				},$scope.winID);
				bcsComunes.ventanaCerrar($scope.winID);
		    }
			bcsComunes.ventanaOverlay($scope.winID,false);
   		},function(respuesta){
   			//ERROR AL CREAR LA ORDEN
   			var mensajeErr = '';

			bcsComunes.ventanaMsg({
				mensaje:$translate.instant(mensajeErr),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaCerrar($scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
   		});	
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
});