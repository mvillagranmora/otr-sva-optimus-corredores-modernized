angular.module('ModuloFacturacion').controller('GenerarArchivoAFPCtrl',
		function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$timeout,$translate,$q, $rootScope,$resource){
	
	$scope.data = {};									// YYYY-mm-dd								// YYYY-mm-dd
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana, false, 3000);
	
	$scope.data.oComboboxAFP = {
			tipo : "combobox-afp",
			configuracion : {},
			filtros : {
				read : false
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	/******************************************************/
	/*** BOTON GENERAR									***/
	/******************************************************/
				
	$scope.data.generar = function() {
		var validacionParametro = "N";
		console.log('Parametro JPM: ' + validacionParametro);
		var parametro = BCS_CONFIGURACION.FACTURACION["Desabilitar_OD_AFP"] === 'S' ? true : false;
		if(parametro == true){
			validacionParametro = "S";
		}

		console.log('Parametro JPM: ' + validacionParametro);
		if ($scope.data.oComboboxAFP.seleccionado.IdAFP === undefined){
			$scope.data.btnGenerar = true; 
			bcsComunes.msg("warning", "warning", $translate.instant('UI_DEBE_INGRESAR_CAMPOS_OBLIGATORIOS'));
			return;
		}else{
			console.log($scope.data.oComboboxAFP.seleccionado.IdAFP);
			console.log($scope.data.oComboboxAFP.seleccionado.NombreAFP);
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", 
					$translate.instant("UI_CONTINUAR"), idVentana);
			t.then(function(result) {
				if (result === OPCIONES_BCSALERTA.SI) {
					// Logica para Quitar Registro
					$scope.data.btnGenerar = false;
					bcsComunes.ventanaOverlay(idVentana, true);
					var envio = {};
			    	envio.IdAFP = $scope.data.oComboboxAFP.seleccionado.IdAFP; 
			        var validar = $resource("",{} , {
						'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290610"}
					});
			        validar.Consultar(envio).$promise.then(function(result){						
				    	if(result.P_OUT_ERRORESCONTADOR === 0){
				    		var datos = {
				    				p_IdEmpresa : $scope.SesionIdEmpresa,
				    				p_IdUsuario : $scope.SesionIdUsuario,
				    				p_IdModulo  : 'FAC',
				    				p_TipoPrograma : [{
				    					IdInterno : 0,
				    					IdTipoPrograma : 'GENERAR_ARCHIVO_AFP',
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
				    					ValorBlob : 'PruebaGenerarAFP'
				    				},{
				    					IdInterno : 0,
				    					NombreParametro : 'ID_AFP',
				    					ValorBlob : $scope.data.oComboboxAFP.seleccionado.IdAFP
				    				},{
				    					IdInterno : 0,
				    					NombreParametro : 'Fecha_AFP',
				    					ValorBlob : $scope.data.oFechaMovimientoComprobante
				    				},{
				    					IdInterno : 0,
				    					NombreParametro : 'NOMBRE_AFP',
				    					ValorBlob : $scope.data.oComboboxAFP.seleccionado.NombreAFP
				    				},{
				    					IdInterno : 0,
				    					NombreParametro : 'PARAMETRO_JPM',
				    					ValorBlob : validacionParametro
				    				}]
				    		}
				    		console.log(JSON.stringify(datos));
				    		 var r = $resource("",{} , {
				    				'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=20043"}
				    			});
				    			
				    		r.Consultar(datos).$promise.then(
				    		function(result) {
				    			if(result.P_OUT_ERRORESCONTADOR>0){
				    				bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_PROCESAR'));
				    			}else{
				    				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				    				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_DIRIGIRSE_CONSULTAR_PROGRAMAS').replace("XXX", result.P_OUT_PROGRAMA[0].IdPrograma),
				    					conflictos : util.parseServerResponse(result)}, $scope.winID);
				    			}
				    		},
				    		function(result){
				    			alert(result);
				    		});
							bcsComunes.msg("info", "info", 'Archivo generado correctamente');
							$scope.filtros.IdAFP = false;
				    	}else{
							bcsComunes.msg("warning", "warning", result.P_OUT_MENSAJES[0].descripcion);
							$scope.filtros.IdAFP = false;
				    	}
						
					},function(result){
						bcsComunes.msg("warning", "warning", 'Error de conexion');
						$scope.filtros.IdAFP = false;
					});
					$scope.data.btnGenerar = true; 
					bcsComunes.ventanaOverlay(idVentana, false);
				} else {
					bcsComunes.msg("info", "info", $translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + 
							$translate.instant("UI_CANCELAR"));
				}
			});
		
		}
	};
	
	/******************************************************/
	/*** FECHA											***/
	/******************************************************/
	
	$scope.data.oFechaMovimientoComprobante = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
	
});