angular.module('ModuloPlataforma').controller('CrearPerfilCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.recordData = {};
	$scope.configData.bloqueoBtnCrearPerfil = false;
	$scope.configData.overlay = bcsComunes.overlay;
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
	};
	
	$scope.limpiarPantalla = function() {
		document.formCrearPerfil.reset();
		$scope.recordData = {};
		
	};
	
	$scope.crearPerfil= function(){
		if($scope.configData.bloqueoBtnCrearPerfil ==false){
			
			$scope.configData.bloqueoBtnCrearPerfil=true;
			var urlCrearPerfil = "/BSCL/Plataforma/GWJAVA/PLA/call?t=138";
			var r = $resource("", {},
		    {
				'CrearPerfil': { method: "POST", url: urlCrearPerfil}
			});
			
			var datosCreacion= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'PLA',
				p_Nombre: $scope.recordData.nombrePerfil,
				p_IdEstado:$scope.recordData.estado
			};
			
			var post = r.CrearPerfil(datosCreacion);
			bcsComunes.ventanaOverlay($scope.ventanaId, true);

			post.$promise.then(function(result){
                bcsComunes.ventanaOverlay($scope.ventanaId, false);
				if(result.P_OUT_NEW_PERFIL_ID!=null && result.P_OUT_NEW_PERFIL_ID>0){
					
					var promesa = bcsComunes.ventanaMsg({ 
					    mensaje: $translate.instant("UI_PERFIL_CREADO_OK"),
						conflictos:util.parseServerResponse(result)
					},$scope.ventanaId);
                    
                    bcsComunes.ventanaPromesa(promesa).then(function(){
                        bcsComunes.ventanaCerrar($scope.ventanaId);	
                    });

				}else{
					for(var i in result.P_OUT_ERRORESDETALLE){
						bcsComunes.msg("error",'Error',$translate.instant(result.P_OUT_ERRORESDETALLE[i].Codigo));	
					}
				}
				$scope.configData.bloqueoBtnCrearPerfil = false;

			});
		}
	};
	
});
