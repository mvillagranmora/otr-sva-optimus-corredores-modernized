angular.module('ModuloCustodia').controller('VencimientoSimultaneaCtrl',
				function($scope,$state,$translate,$resource,
						bcsComunes,bcsServicioPlataforma,bcsOperacionesComunes, bcsServiciosSimultanea)
{	
	$scope.data = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
		
	
	//SETEO VARIABLES
	//$scope.data.kngFechaSistema = mxment($scope.SesionFechaSistema).toDate();
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	//Codigo solo para pruebas de usuario
	if ($scope.SesionIdUsuario === 122 && $scope.Ambiente === 'PRO'){
		$scope.data.FechaMovimientoDisabled = false;
	} else {
		$scope.data.FechaMovimientoDisabled = true;
	};
	
	//COMPORTAMIENTO BOTONES
	var optionEnabled = function(){		
		$scope.data.procesarDisabled = true;
		$scope.data.cancelDisabled = true;
	}
	var optionDisabled = function(){
		$scope.data.procesarDisabled = false;
		$scope.data.cancelDisabled = false;
	}	
	
	//EJECUCION
	optionEnabled();

	$scope.data.cancelar = function()
	{
		bcsComunes.ventanaData($scope.idVentana,{resultado: true});
		bcsComunes.ventanaCerrar($scope.idVentana); 
	};

	//LLAMAR SERVICIO DE ANTICIPO SIMULTANEA
	$scope.data.procesar = function()
    {
		bcsComunes.ventanaOverlay(idVentana,true);
		
		//DATOS SERVICIO
		var datos = {};
		datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		datos.p_IdUsuario              	 	= $scope.SesionIdUsuario; 
		datos.p_IdModulo                	= 'CUS';
		datos.p_SimularOperacion        	= 'N';
		datos.p_IdTipoVencimiento        	= 'SIM';
	    datos.p_Fecha						= util.dateSPdate($scope.data.kngFechaSistema); 
		datos.p_IdPais						= $scope.SesionIdPaisSistema; 

		bcsComunes.log().info(JSON.stringify(datos)); 
       	bcsServiciosSimultanea.VencimientoSimultanea(
       		datos,
       		function(respuesta){				
       			if (typeof respuesta.P_OUT_MENSAJES !== 'undefined' && respuesta.P_OUT_MENSAJES.length !== 0){
       				bcsComunes.ventanaAbrir('VentanaResultado',
						{ 	usaNuevosMensajes : true ,
							titulo: $translate.instant('UI_VENCIMIENTO_SIMULTANEA_FINALIZADO'), 
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
					mensaje:$translate.instant("UI_ERROR_EJECUCION_VENCIMIENTO_SIMULTANEA"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana, false);
			}
       	);
       	
    };
});

