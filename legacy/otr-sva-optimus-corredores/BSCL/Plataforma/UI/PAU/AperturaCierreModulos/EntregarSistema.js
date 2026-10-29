angular.module('ModuloPlataforma').controller('EntregarSistemaCtrl',function($scope,$translate,bcsComunes,bcsAlerta,bcsConfiguracionPauta,bcsServicioPlataforma,OPCIONES_BCSALERTA,$timeout){
	
	$scope.datos = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	
	$scope.datos.FechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		
	$scope.entregarSistema = function() {	
		var datos = {
				p_IdEmpresa 		  : $scope.SesionIdEmpresa,
			    p_IdUsuario 		  : $scope.SesionIdUsuario,
			    p_IdModulo       	  : 'PLA',
			    p_FechaSistemaDestino : util.dateSPdate($scope.datos.FechaSistema),
			    p_CodigoUsuario       : $scope.$root.SesionUsuario
		};
		bcsComunes.ventanaOverlay($scope.winID,true);
		bcsServicioPlataforma.Servicios().EntregarSistema(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			if (result.P_OUT_ERRORESCONTADOR === 0) {
				var p = bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_SISTEMA_SE_ENTREGARA_EN_BREVE"),
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(p);   
				$.when(promesa).then(function(data){
					bcsComunes.ventanaCerrar($scope.winID);
		        }); 
			} else {
				bcsComunes.ventanaOverlay($scope.winID,false);
				var p = bcsComunes.ventanaMsg({
						mensaje:result.P_OUT_MENSAJE,
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(p);   
				$.when(promesa).then(function(data){
					bcsComunes.ventanaCerrar($scope.winID);
		        }); 
			}
		},function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			var p = bcsComunes.msg("error","UI_ERROR","UI_ERROR_AL_ENTREGAR_SISTEMA");
			var promesa=bcsComunes.ventanaPromesa(p);   
			$.when(promesa).then(function(data){
				bcsComunes.ventanaCerrar($scope.winID);
	        }); 
		});
	}
	
	$scope.obtenerFechaSiguienteHabil = function() {	
		var datos = {
				p_fechas : [
					{ fechaInicial :util.dateSPdate($scope.datos.FechaSistema), numeroDias : 1 }
				],
				p_diaHabil : 'S',
				p_soloCaeDiaHabil : 'S',
				p_idPais : $scope.SesionIdPaisSistema
		};
		bcsComunes.ventanaOverlay($scope.winID,true);
		bcsServicioPlataforma.Servicios().ObtenerDiaSiguiente(datos).$promise.then(function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			if (result && result.P_OUT_ERRORESCONTADOR === 0 && result.p_out_fechas && result.p_out_fechas[0]) {
				$scope.datos.FechaSistema = util.dateInicioDia(result.p_out_fechas[0].fechaCalculada);
			} else {
				bcsComunes.ventanaOverlay($scope.winID,false);
				var p = bcsComunes.ventanaMsg({
						mensaje:result.P_OUT_MENSAJE,
						conflictos:util.parseServerResponse(result)
					},$scope.winID);
				var promesa=bcsComunes.ventanaPromesa(p);   
				$.when(promesa).then(function(data){
					bcsComunes.ventanaCerrar($scope.winID);
		        }); 
			}
		},function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			var p = bcsComunes.msg("error","UI_ERROR","UI_ERROR_AL_OBTENER_FECHA_SIGUIENTE_HABIL");
			var promesa=bcsComunes.ventanaPromesa(p);   
			$.when(promesa).then(function(data){
				bcsComunes.ventanaCerrar($scope.winID);
	        }); 
		});
	}
	
	$scope.obtenerFechaSiguienteHabil();
	
});