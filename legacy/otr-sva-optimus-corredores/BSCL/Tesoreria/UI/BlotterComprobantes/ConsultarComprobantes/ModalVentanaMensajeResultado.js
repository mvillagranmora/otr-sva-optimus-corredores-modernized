angular.module('ModuloTesoreria').controller('ModalVentanaMensajeResultadoCtrl',function($scope,$translate,bcsComunes,OPCIONES_BCSALERTA){
	
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.mensaje = $translate.instant(parametros.mensaje);
	$scope.conflictos = parametros.conflictos;
	
	var tituloVentana = $translate.instant("UI_INFORMACION");
	$scope.icono = 'INFORMACION';
	
	if(parametros.conflictos){
		if(parametros.conflictos.errores.length||parametros.conflictos.erroresContador>0){
			tituloVentana = $translate.instant("UI_ERRORES");
			$scope.icono = 'ERROR';
		} else if(parametros.conflictos.erroresFatales.length){
			tituloVentana = $translate.instant("UI_ERRORES");
			$scope.icono = 'ERROR';
		} else if(parametros.conflictos.autorizaciones.length){
			tituloVentana = $translate.instant("UI_POR_AUTORIZAR");
			$scope.icono = 'AUTORIZACION';
		} else if(parametros.conflictos.advertencias.length){
			tituloVentana = $translate.instant("UI_ADVERTENCIA");
			$scope.icono = 'ADVERTENCIA';
		} else if (parametros.conflictos.informaciones.length) {
			tituloVentana = $translate.instant("UI_INFORMACION");
			$scope.icono = 'INFORMACION';
		}
	}
	
	if (!$scope.mensaje) {
		if (parametros.conflictos.erroresFatalesContador > 0) {
			$scope.mensaje = $translate.instant("UI_OPERACION_CON_ERRORES");
		}
		else if (parametros.conflictos.erroresContador > 0) {
			$scope.mensaje = $translate.instant("UI_OPERACION_CON_ERRORES");
		}
		else if (parametros.conflictos.erroresContador === 0 
				&& parametros.conflictos.autorizacionesContador === 0 
				&& parametros.conflictos.advertenciasContador === 0 
				&& parametros.conflictos.informacionesContador === 0) {
			$scope.mensaje = $translate.instant("UI_OPERACION_EJECUTADA");
		}
	}
	
	bcsComunes.ventanaTitulo(idVentana, tituloVentana);

	
	$scope.confirmar = function()
	{			
		
		bcsComunes.ventanaData(idVentana,OPCIONES_BCSALERTA.ACEPTAR);
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.cancelar = function()
	{					
	
		bcsComunes.ventanaData(idVentana,OPCIONES_BCSALERTA.CANCELAR);
		bcsComunes.ventanaCerrar(idVentana);
	};
	

});