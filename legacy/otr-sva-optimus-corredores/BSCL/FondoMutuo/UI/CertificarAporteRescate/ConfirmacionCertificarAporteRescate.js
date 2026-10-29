angular.module('ModuloFFM').controller('ConfirmacionCertificarAporteRescateFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var filtrosFondos = [];
	var conf = bcsConfiguracionFFM.getConsultaConfirmacionCertificacionAporteRescate();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarAportesRescatesCertificados = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);
	var overlay = bcsComunes.overlay;
	$scope.habilitarBoton = false;
	$scope.limpiarFiltros = function()
	{
        $scope.formData.setFondo = "-1";
        $scope.filtros.IdEstado = undefined;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		bcsComunes.ventanaOverlay($scope.winID,true);
		$scope.defaultOptions.dataSource.data(parametros.respuesta.P_OUT_RESULTADO);
		if(parametros.respuesta.P_OUT_RESULTADO.length > 0){
			$scope.habilitarBoton = true;
			if(parametros.respuesta.P_OUT_RESULTADO.length > 0){
				for(var i = 0;i<parametros.respuesta.P_OUT_RESULTADO.length;i++){
					if((parametros.respuesta.P_OUT_RESULTADO[i].Estado === 'RZD')&&( parametros.respuesta.P_OUT_RESULTADO[i].IdSolicitudAporteRescateRelacionado > 0 )){
						let index0 = parametros.datosCertificarAporteRescate.p_Solicitudes.findIndex(function(x){return x.IdSolicitudAporteRescate == parametros.respuesta.P_OUT_RESULTADO[i].IdSolicitudAporteRescateRelacionado; });
						if ( index0 >=0){
							parametros.datosCertificarAporteRescate.p_Solicitudes.splice(index0,1);
						}
					  }
					if((parametros.respuesta.P_OUT_RESULTADO[i].Estado === 'RZD')&&( parametros.respuesta.P_OUT_RESULTADO[i].IdSolicitudAporteRescate > 0 )){
						let index1 = parametros.datosCertificarAporteRescate.p_Solicitudes.findIndex(function(x){return x.IdSolicitudAporteRescate == parametros.respuesta.P_OUT_RESULTADO[i].IdSolicitudAporteRescate; });
						if ( index1 >=0){
							parametros.datosCertificarAporteRescate.p_Solicitudes.splice(index1,1);
						}
					} 
				  } 
							   
				if(parametros.masivo){
					for(var i = 0;i<parametros.respuesta.P_OUT_RESULTADO.length;i++){
						if(parametros.respuesta.P_OUT_RESULTADO[i].Estado === 'RZD'){
							$scope.habilitarBoton = false;
							break;
						}			
					}				
				}else if (!parametros.masivo && parametros.datosCertificarAporteRescate.p_Solicitudes.length ===0 ){
					$scope.habilitarBoton = false;	
				}		
			} else{
				$scope.habilitarBoton = false;
			}	
		}	
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		$scope.ajustaVentana();
	}
	
	$scope.buscarCertificacion = function(){
		filtrosFondos = [];
		if($scope.filtros.fondoMutuo)
			filtrosFondos.push({ field: "IdSerieFondo", operator: "eq", value: $scope.filtros.fondoMutuo.IdSerieFondo } );
		if($scope.filtros.IdEstado)
			filtrosFondos.push({ field: "Estado", operator: "eq", value: $scope.filtros.IdEstado } );
		apiGrid.filter(filtrosFondos);
		$scope.apiGridSolicitud.refresh;
	};
	
	$scope.confirmarCertificacionFFM = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var datosCertificarAporteRescate = parametros.datosCertificarAporteRescate;
		
				datosCertificarAporteRescate.p_SimularOperacion = 'N';
			
		if(parametros.masivo){
			bcsServicioFondosMutuos.CertificarPorFiltrosSolicitudAporteRescate(datosCertificarAporteRescate).$promise.then(function(respuesta){
		     	 var id = undefined;
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		id = bcsComunes.ventanaMsg({mensaje:"UI_CERTIFICACION_OK",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }else{
		     		id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERTIFICACION_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
		     	if(id){
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
		     	}else{
		     		bcsComunes.ventanaOverlay($scope.winID,false);
		     	} 
		     	
	 		 },function(respuesta){
	 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERTIFICACION_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	 			bcsComunes.ventanaOverlay($scope.winID,false);
	 		 });
		}else{
			bcsServicioFondosMutuos.CertificarSolicitudAporteRescate(datosCertificarAporteRescate).$promise.then(function(respuesta){
		     	 var id = undefined;
		     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		id = bcsComunes.ventanaMsg({mensaje:"UI_CERTIFICACION_OK",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }else{
		     		id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERTIFICACION_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
		     	if(id){
		     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
		     	}else{
		     		bcsComunes.ventanaOverlay($scope.winID,false);
		     	}
	 		 },function(respuesta){
	 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERTIFICACION_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	 			bcsComunes.ventanaOverlay($scope.winID,false);
	 		 });
		}
	}
	
	$scope.confirmarCertificacionBatchFFM = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var datosCertificarAporteRescate = parametros.datosCertificarAporteRescate;
		datosCertificarAporteRescate.p_SimularOperacion = 'N';
		datosCertificarAporteRescate.p_Masivo = (parametros.masivo) ? 'S' : 'N';
		if (!datosCertificarAporteRescate.p_Solicitudes && !parametros.masivo)
			datosCertificarAporteRescate.p_Solicitudes = [];
	    bcsServicioFondosMutuos.CrearProgramaCertificacionBatch(datosCertificarAporteRescate).$promise.then(function(respuesta){
	     	 var id = undefined;
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		id = bcsComunes.ventanaMsg({mensaje:"UI_OK_AGENDA_PROGRAMA_CERTIFICACION_FFMM",conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }else{
	     		id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_AGENDA_PROGRAMA_CERTIFICACION_FFMM"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
	     	if(id){
	     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
	     	}else{
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	}
 		 },function(respuesta){
 			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_AGENDA_PROGRAMA_CERTIFICACION_FFMM"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
 			bcsComunes.ventanaOverlay($scope.winID,false);
 		 });
	}
	
	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate : items[0].IdSolicitudAporteRescate, Simular: 'S'} );
  	};
	
	$scope.limpiarFiltros();
	
});