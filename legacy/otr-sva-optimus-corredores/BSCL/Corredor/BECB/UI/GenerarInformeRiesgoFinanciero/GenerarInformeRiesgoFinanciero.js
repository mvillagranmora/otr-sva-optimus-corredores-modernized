angular.module('ModuloCorredor').controller('GenerarInformeRiesgoFinancieroCtrl',
		function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$timeout,$translate,$q, $rootScope,$resource){
	
	$scope.data = {};									// YYYY-mm-dd								// YYYY-mm-dd
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana, false, 3000);
	
	$scope.data.oComboboxRifiDifi = {
			tipo : "combobox-rifi-difi",
			configuracion : {},
			filtros : {
				read : false
			},
			idActual : false,
			seleccionado : false,
			idDefecto : 'ALL'
		};
	
	/******************************************************/
	/*** BOTON GENERAR									***/
	/******************************************************/
				
	$scope.data.generar = function() {
		if ($scope.data.oComboboxRifiDifi.seleccionado.Codigo === undefined){
			$scope.data.btnGenerar = true; 
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_INGRESAR_CAMPOS_OBLIGATORIOS'));
			return;
		}else{
			var programa;
			if($scope.data.oComboboxRifiDifi.seleccionado.Codigo === "RIFI"){
				programa = 'GENERAR_INFORME_RIESGO_FINANCIERO';
			}else if ($scope.data.oComboboxRifiDifi.seleccionado.Codigo === "DIFI"){
				programa = 'GENERAR_INFORME_DETALLE_RIESGO';
			}else{
				programa = 'GENERAR_INFORME_RIESGO_COMPLETO';
			}
			
			console.log($scope.data.oComboboxRifiDifi.seleccionado.Codigo);
			console.log($scope.data.oComboboxRifiDifi.seleccionado.Nombre);
    		var datos = {
    				p_IdEmpresa : $scope.SesionIdEmpresa,
    				p_IdUsuario : $scope.SesionIdUsuario,
    				p_IdModulo  : 'FAC',
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
    					Valor : 'FAC'
    				},{
    					IdInterno : 0,
    					NombreParametro : 'Archivo',
    					ValorBlob : 'PruebaGenerarInformeRiesgo'
    				},{
    					IdInterno : 0,
    					NombreParametro : 'Fecha_Informe',
    					ValorBlob : $scope.data.oFechaRifiDifi
    				},{
    					IdInterno : 0,
    					NombreParametro : 'Tipo_Informe',
    					ValorBlob : $scope.data.oComboboxRifiDifi.seleccionado.Codigo
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
			bcsComunes.msg("info", "info", 'Archivo generado correctamente');
			$scope.filtros.IdAFP = false;
    	
		}
	};
	
	/******************************************************/
	/*** FECHA											***/
	/******************************************************/
	
	$scope.data.oFechaRifiDifi = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
});