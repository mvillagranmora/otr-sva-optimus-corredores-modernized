(function() {
	angular.module('ModuloMesa').controller('ModalEditarCodigoComercio',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
				
		var movSpot = this;
		
		movSpot.datosAntes = {};
		movSpot.datosDespues = {};
		movSpot.datosAntes = angular.copy(movSpot.datosDespues);
		
		//Configurar al principio
		
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		movSpot.data = {};
		
		movSpot.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		
		$scope.data.editarCodigoComercio = function() {
			
				var parametros = {};
				parametros.p_IdEmpresa           = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario           = $scope.SesionIdUsuario;
				parametros.p_IdModulo		 	 = 'MSP';
				parametros.p_UsaReglasNegocio	 = 'S';			
				var tablaDatosOperacion 		= [];	

				$.each(movSpot.datosEntrada.Movimientos,function(key,value){	
					var datos = {
							NumeroOperacion : value.IdOperacion, 
							CodigoComercio : Number($scope.data.CodigoComercioIdActual)
					};
					tablaDatosOperacion.push(datos);
				});
				parametros.p_DatosOperacion	 = tablaDatosOperacion;			
				
				bcsServiciosMesa.ModificarCodigoComercio(parametros).$promise.then(successEditarEstado).catch(error);

		};
		
		var error = function(respuesta)
		{
			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID, false);
		};
		
		var successEditarEstado = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
			{		
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			else
			{		
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
				});
			}
			overlay.encendido = false;
			bcsComunes.ventanaOverlay($scope.winID, false);	
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		});
}());