angular.module('ModuloCustodia').controller('VencimientoPactosCtrl',
				function($scope,$state,$translate,$resource,
						bcsComunes,bcsServicioPlataforma,bcsOperacionesComunes, bcsServiciosMesa)
{	
	$scope.data = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
		
	
	//SETEO VARIABLES
	//$scope.data.kngFechaSistema = mxment($scope.SesionFechaSistema).toDate();
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
	
	//EJECUCION
	optionEnabled();

	
	//LLAMAR SERVICIO DE ANTICIPO SIMULTANEA
	var success = function(respuesta)
	{
		if (respuesta.P_OUT_ERRORESCONTADOR == 0){	
			//Se debe mostrar el listado
			bcsComunes.msg("info","info", $translate.instant('UI_OPERACION_INGRESO_EXITO'));
			
			bcsComunes.ventanaData(idVentana,'false');
			//bcsComunes.ventanaCerrar(idVentana);
		}
		else
		{
			bcsComunes.ventanaMsg({	conflictos:util.parseServerResponse(respuesta) },$scope.winID);
			
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
			bcsComunes.msg("error","Error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
		}
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	var error = function(respuesta)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg("error","Error Interno",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
						
		bcsComunes.ventanaOverlay(idVentana, false);
	};
	$scope.data.procesar = function()
    {
		bcsComunes.ventanaOverlay(idVentana, true);
		
		//DATOS SERVICIO
		var datos = {};
		datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		datos.p_IdUsuario              	 	= $scope.SesionIdUsuario; 
		datos.p_IdModulo                	= 'MRF';
		datos.p_UsaReglasNegocio        	= 'S';
		datos.p_IdTipoVencimiento			= 'PAC';
	    datos.p_SimularOperacion			= 'N';
	    datos.p_Fecha						= util.dateSPdate($scope.data.kngFechaSistema); 
		datos.p_IdPais						= $scope.SesionIdPaisSistema; 

		bcsComunes.log().info(JSON.stringify(datos));
       	bcsServiciosMesa.VencimientoPactos(datos,success,error); 
    };
	
    
    var parseServerResponse = function(response)
	{
		var result = { errores :[],advertencias:[]};
		$.each(response.P_OUT_ADVERTENCIASDETALLE,function(index,value){;
  			result.adv.push(value);
		});
		$.each(response.P_OUT_ERRORESDETALLE,function(index,value){;
  			result.errores.push(value);
		});
		return result;
	}
	
});

