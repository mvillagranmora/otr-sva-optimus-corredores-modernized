angular.module('ModuloOrdenes').controller('EnvioFacturarModalCtrl',
		function ($scope,bcsServicioOrden,bcsComunes,$translate) {

		$scope.idVentana = bcsComunes.ventanaWinId($scope);	
		var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		var parametros = bcsComunes.ventanaParams(idVentana);
		var algoFacturado = false;
		$scope.cerrarHabilitado = true;
			
		var limpiarAsignaciones = function(asignaciones)
		{
			$.each(asignaciones,function(key,value){
				value.resultado = 'UI_NO_ENVIADO';
				value.IdMovimientoGenerado = undefined;
				value.Mensajes = undefined;
			});
		};
		
		var processResult = function(data) {
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			$scope.cerrarHabilitado = true;
			if (data.status === 503)
			{
				//mostrar popup
				return;
			}
			
			$.each($scope.asignaciones,function(asigKey,asignacion){
				$.each(data.P_OUT_MENSAJES,function(key,value){
					if (value.id === asignacion.IdAsignacion) {
						if (value.tipo === 'ERROR' || value.tipo === 'ADVERTENCIAS'){
							asignacion.resultado = 'UI_ERROR';
							asignacion.IdMovimientoGenerado = undefined;
							asignacion.Mensajes = value.Descripcion?value.Descripcion:value.descripcion;

							errores = true;
						}else {
							asignacion.Resultado = value.Descripcion?value.Descripcion:value.descripcion;
							algoFacturado = true;
						}
					}
				});
														
			});
				
			$.each($scope.asignaciones,function(asigKey,asignacion){	
				$.each(data.P_OUT_ERRORESDETALLE,function(dataKey,dataValue){
					
					if (dataValue.data && dataValue.data.PILA !== undefined)
					{	
						asignacion.resultado = 'UI_ERROR';
						asignacion.IdMovimientoGenerado = undefined;
						asignacion.Mensajes = util.parseServerResponse(dataValue);
						asignacion.Mensajes =asignacion.Mensajes.errores[dataKey].Descripcion;
					}
					else if (dataValue.IdInterno === asignacion.IdAsignacion){
						asignacion.resultado = 'UI_ERROR';
						asignacion.IdMovimientoGenerado = undefined;
						asignacion.Mensajes = dataValue.Descripcion;
					}
					errores = true;
				});
				
				$.each(data.P_OUT_ADVERTENCIASDETALLE,function(dataKey,dataValue){
					
					if (dataValue.data && dataValue.data.PILA !== undefined)
					{	
						asignacion.resultado = 'UI_ERROR';
						asignacion.IdMovimientoGenerado = undefined;
						asignacion.Mensajes = util.parseServerResponse(dataValue);
						asignacion.Mensajes =asignacion.Mensajes.errores[dataKey].Descripcion;
					}
					else if (dataValue.IdInterno === asignacion.IdAsignacion){
						asignacion.resultado = 'UI_ERROR';
						asignacion.IdMovimientoGenerado = undefined;
						asignacion.Mensajes = dataValue.Descripcion;
					}
					errores = true;
				});
				
			});
				
	  	};

		$scope.cerrar = function () {
			bcsComunes.ventanaData(idVentana,algoFacturado);
			bcsComunes.ventanaCerrar(idVentana);
		};

		$scope.confirmarEnvioFacturar = function()
	  	{
			var datos = {};
			var asig = [];
			bcsComunes.ventanaOverlay($scope.idVentana,true);
	  		$.each($scope.asignaciones,function(key,asignacion){
	  			asig.push({IdAsignacion : asignacion.IdAsignacion,GeneraComprobante : 'N'});
	  			
	  		});
	  		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
	  		datos.p_IdUsuario = $scope.SesionIdUsuario;
	  		datos.p_IdModulo = 'ORD';
	  		datos.p_Asignaciones = asig;
	  		
	  		$scope.cerrarHabilitado = false;
	  		bcsServicioOrden.EnviarAsignacionesFacturar(datos,function(result){
	  			
	  			processResult(result);
	  		},function(result) {
	  			bcsComunes.ventanaOverlay($scope.idVentana,false);
	  			$scope.cerrarHabilitado = true;
				bcsComunes.msg("error","Error Interno",result.data.CODIGO);
	  		});
	  	}
	  	
	  	$scope.asignaciones = parametros.asignaciones;
		$scope.confirmarHabilitado = true;
		limpiarAsignaciones($scope.asignaciones);
});
