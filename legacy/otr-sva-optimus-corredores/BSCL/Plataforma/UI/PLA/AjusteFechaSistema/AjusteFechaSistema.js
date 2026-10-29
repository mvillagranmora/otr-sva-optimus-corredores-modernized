angular.module('ModuloPlataforma').controller('AjusteFechaSistemaCtrl',
		function($scope,bcsComunes,$resource,$translate,bcsServicioPlataforma,bcsAlerta,OPCIONES_BCSALERTA){
	//AJUSTE FECHA DEL SISTEMA
	//shechenleitner 25-05-2017
	$scope.data = {};
	$scope.data.ignorarPrimeraLinea = true;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.data.kngFechaActual = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaNueva = util.dateInicioDia($scope.SesionFechaSistema);

	$scope.cambiarFechaSistema = function(datos){		
		//Bloquea ventana en caso de servicio lento o 2 clics seguidos
		//bcsComunes.ventanaOverlay($scope.idVentana,true);
		
		//NO PUEDEN IR CAMPOS NULOS
		if($scope.data.kngFechaNueva == null) $scope.data.kngFechaNueva = '';

		if ($scope.data.kngFechaNueva.toString() == '' || $scope.data.kngFechaNueva.toString() == $scope.data.kngFechaActual.toString()) {
			bcsComunes.msg("warning","warning",$translate.instant('UI_INGRESAR_FECHA_VALIDA'));
			return;
		}
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_AJUSTARA_FECHA_SISTEMA') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.winID, true);

				//GENERACION DE DATOS PARA ENVIO AL SERVICIO
				var datosFechaSistema = {};
				
				datosFechaSistema.p_IdEmpresa 			= $scope.SesionIdEmpresa;
				datosFechaSistema.p_IdUsuario 			= $scope.SesionIdUsuario;
				datosFechaSistema.p_IdModulo 			= 'PLA';
				datosFechaSistema.p_SimularOperacion 	= 'N';
				
				var dataRows = [];
				dataRows.push({
					FechaSistema	: util.dateSPdate($scope.data.kngFechaNueva), 
					IdEmpresa		: $scope.SesionIdEmpresa
				});	
				datosFechaSistema.p_FechaSistema = dataRows;
				
				console.log("Ajuste Fecha Sistema");
				console.log(JSON.stringify(datosFechaSistema));
				
				bcsServicioPlataforma.AjustarFechaSistema(
					datosFechaSistema,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
							$scope.buscarComprobantes();
						} else {	
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						bcsComunes.ventanaOverlay($scope.winID, false);
					}
				);
			}
		});
		return;
	};	
});