angular.module('ModuloTesoreria').controller('ComplementarTransacionCtrl',
		function($scope,bcsComunes,$translate,bcsServiciosTesoreria, bcsAlerta, OPCIONES_BCSALERTA)
{

	$scope.winID  = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);
	
	
	if (parametros && parametros.fecha) {
		$scope.fechaTransaccion = parametros.fecha;
	}
	else
	{
		$scope.fechaTransaccion =  util.dateInicioDia($scope.SesionFechaSistema);
	}

	var success = function(respuesta)
	{	
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0)
		{	
				bcsComunes.ventanaMsg({									
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
		}
		else
		{	
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
		}
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
		bcsComunes.ventanaOverlay($scope.winID, false);		
	};
	
	
    //Procesar actualizacion de transacciones con datos del SCL(para Clearing y Liquidacion Bilateral)
	$scope.procesar = function()
    {	
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_COMPLEMENTAR_TRASACCIONES_DESDE_SCL') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)
			{	
				bcsComunes.ventanaOverlay($scope.winID, true);
				
				var parametros = {};
				parametros.p_IdEmpresa            = $scope.SesionIdEmpresa;
				parametros.p_FechaTransaccion     = util.dateSPdate($scope.fechaTransaccion);				           
				parametros.p_FolioTransaccion     = String($scope.folioTransaccion || "0");
				
				console.log("PARAMETROS:" + JSON.stringify(parametros));
				bcsServiciosTesoreria.ActualizarTransaccion(parametros, success, error);
			}
		});
		
		
    };
    
    
    //Cerrar ventana
	$scope.cerrar = function() {
		bcsComunes.ventanaCerrar($scope.winID);
	}


	var firstHeightCrear = $('#Win_ComplementarTransaccion');
        firstHeightCrear.css('height', '165px');
		
});


