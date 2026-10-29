angular.module('ModuloPlataforma').controller('VentanaMensajeResultadoCtrl',function($scope,$translate,bcsComunes){
	
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.mensaje = $translate.instant(parametros.mensaje);
	$scope.conflictos = parametros.conflictos;
	$scope.iconomensaje = true;

	
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

	

	if($scope.mensaje)
	{
		if(parametros.conflictos){
			if(parametros.conflictos.errores.length || parametros.conflictos.erroresFatales.length || parametros.conflictos.autorizaciones.length || parametros.conflictos.advertencias.length || parametros.conflictos.informaciones.length){
				$scope.iconomensaje = false;
			}
		}
	}else{
		if(parametros.conflictos){
			if(parametros.conflictos.errores.length || parametros.conflictos.erroresFatales.length || parametros.conflictos.autorizaciones.length || parametros.conflictos.advertencias.length || parametros.conflictos.informaciones.length){
				$scope.iconomensaje = true;
			}
		}
	}
	

	/* if(parametros.conflictos){
		if(parametros.conflictos.errores.length || parametros.conflictos.erroresFatales.length || parametros.conflictos.autorizaciones.length || parametros.conflictos.advertencias.length || parametros.conflictos.informaciones.length){
			$scope.iconomensaje = true;
		}else{
			$scope.iconomensaje = false;
		}
	} */

	
	  
	
	
	bcsComunes.ventanaTitulo(idVentana, tituloVentana);

	$scope.salir = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	bcsComunes.ventanaTamanoAutomatico(idVentana,5);

	var autoHeight = document.querySelector("div#FORM_VentanaMensajeResultado[ventanaid='"+idVentana+"']");
		autoHeight.parentNode.style.height = "auto";

	$scope.toggleDiv = function() {
		var labelDesc = document.querySelector("div#FORM_VentanaMensajeResultado[ventanaid='"+idVentana+"'] >  div.ng-scope > div.ng-scope > div.bcs-modal > div.row.bcs-modal-info > .hidden-description")
		var autoHeight = document.querySelector("div#FORM_VentanaMensajeResultado[ventanaid='"+idVentana+"']");
		autoHeight.parentNode.style.height = "auto";
		if (!labelDesc.style.display) {
			labelDesc.style.display = 'block';
		}
	
		labelDesc.style.display = (labelDesc.style.display === 'block') ? 'none' : 'block';
	};


	
		
});
