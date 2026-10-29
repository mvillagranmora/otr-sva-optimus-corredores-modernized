angular.module('ModuloCorredor').controller('GenerarInformeVencimientosGeneralesCtrl',
		function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$timeout,$translate,$q, $rootScope,$resource){
	
	$scope.data = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana, false, 3000);
	
	/******************************************************/
	/*** FECHA											***/
	/******************************************************/
	
	$scope.data.oFechaVencimiento = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
	/******************************************************/
	/*** BOTON GENERAR									***/
	/******************************************************/
				
	$scope.data.generar = function() {
		
		if ($scope.data.oFechaVencimiento === null || $scope.data.oFechaVencimiento === undefined){
			$scope.data.btnGenerar = true; 
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_INGRESAR_CAMPOS_OBLIGATORIOS'));
			return;
		}else{
			var programa = 'GENERAR_INFORME_VENCIMIENTOS_GENERALES';

    		var datos = {
    				p_IdEmpresa : $scope.SesionIdEmpresa,
    				p_IdUsuario : $scope.SesionIdUsuario,
    				p_IdModulo  : 'COR',
    				p_TipoPrograma : [{
    					IdInterno : 0,
    					IdTipoPrograma : programa,
    					SegundosDesfase : 0
    				}],
    				p_Parametro : [{
    					IdInterno : 0,
    					NombreParametro : 'IdEmpresa',
    					Valor : $scope.SesionIdEmpresa
    				},{
    					IdInterno : 0,
    					NombreParametro : 'IdUsuario',
    					Valor : $scope.SesionIdUsuario
    				},{
    					IdInterno : 0,
    					NombreParametro : 'IdModulo',
    					Valor : 'COR'
    				},{
    					IdInterno : 0,
    					NombreParametro : 'Fecha_Informe',
    					ValorBlob : $scope.data.oFechaVencimiento
    				}]
    		}
    		console.log(JSON.stringify(datos));
    		 var r = $resource("",{} , {
    				'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
    			});
    			
    		r.Consultar(datos).$promise.then(
    		function(result) {
    			if(result.P_OUT_ERRORESCONTADOR>0){
    				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
    			}else{
    				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
    				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
    					conflictos : util.parseServerResponse(result)}, $scope.winID);    				
    			}
    		},
    		function(result){
    			alert(result);
    		});
		}
	};
});