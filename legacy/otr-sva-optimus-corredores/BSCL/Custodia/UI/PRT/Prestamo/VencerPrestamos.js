angular.module('ModuloCustodia').controller('VencerPrestamosCtrl',
				function($scope,$state,$translate,$resource,
						bcsComunes,bcsServicioPlataforma,bcsOperacionesComunes,bcsServiciosPrestamo)
{	
	$scope.data = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
		
	
	//SETEO VARIABLES
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	//COMPORTAMIENTO BOTONES
	var optionEnabled = function(){		
		$scope.data.procesarDisabled = true;
		$scope.data.cancelDisabled = true;
	}
	var optionDisabled = function(){
		$scope.data.procesarDisabled = false;
		$scope.data.cancelDisabled = false;
	}
	
	$scope.data.FechaMovimientoDisabled = true;
	
	//EJECUCION
	optionEnabled();

	$scope.data.cancelar = function()
	{
		bcsComunes.ventanaData($scope.idVentana,{resultado: true});
		bcsComunes.ventanaCerrar($scope.idVentana); 
	};
	
	//LLAMAR SERVICIO DE VENCIMIENTO DE PRESTAMOS
	$scope.data.procesar = function()
    {
		bcsComunes.ventanaOverlay(idVentana,true);
		
		//DATOS SERVICIO
		var datos = {};
		datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		datos.p_IdUsuario              	 	= $scope.SesionIdUsuario; 
		datos.p_IdModulo                	= 'CUS';
		datos.p_SimularOperacion        	= 'N';
		datos.p_IdTipoVencimiento        	= 'PRT';
	    datos.p_Fecha						= util.dateSPdate($scope.data.kngFechaSistema); 
		datos.p_IdPais						= $scope.SesionIdPaisSistema; 

		bcsComunes.log().info(JSON.stringify(datos)); 
		bcsServiciosPrestamo.VencerPrestamo(
       		datos,
       		function(respuesta){				
       			if (typeof respuesta.P_OUT_MENSAJES !== 'undefined' && respuesta.P_OUT_MENSAJES.length !== 0){
       				bcsComunes.ventanaAbrir('VentanaResultado',
						{ 	usaNuevosMensajes : true ,
							titulo: $translate.instant('UI_VENCIMIENTO_PRESTAMO_FINALIZADO'), 
							campoSecuencia : "id" ,
							nombreSecuencia : $translate.instant('UI_SECUENCIA'),
							datos : respuesta.P_OUT_MENSAJES
						} ,false,false,idVentana);
       			} else if (typeof respuesta.P_OUT_DESCRIPCION !== 'undefined') { 
       				bcsComunes.msg("error","error",respuesta.P_OUT_DESCRIPCION);
       			};
				bcsComunes.ventanaOverlay(idVentana, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_EJECUCION_VENCIMIENTO_PRESTAMO"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
			}
       	);
       	
    };
});
