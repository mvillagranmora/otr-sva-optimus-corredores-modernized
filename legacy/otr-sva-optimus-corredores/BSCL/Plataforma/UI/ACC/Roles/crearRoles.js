angular.module('ModuloPlataforma').controller('CrearRolCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.recordData = {};
	$scope.configData.bloqueoCrearRol = false;
	$scope.configData.overlay = bcsComunes.overlay;
	
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaTitulo($scope.ventanaId, bcsComunes.ventanaGetTitulo($scope.ventanaId)+ " - "+ $scope.SesionEmpresa );
	
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
	};
	
	$scope.limpiarPantalla = function() {
		document.formCrearRol.reset();
		$scope.recordData = {};
		
	};
	
	$scope.crearRol= function(){
		if($scope.configData.bloqueoCrearRol ==false){
			
			$scope.configData.bloqueoCrearRol=true;
			var urlCrearRol = "/BSCL/Plataforma/GWJAVA/PLA/call?t=126";
			var r = $resource("", {},
		    {
				'CrearRol': { method: "POST", url: urlCrearRol}
			});
			
			var datosCreacion= {
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdModulo: 'PLA',
				p_Nombre: $scope.recordData.nombreRol,
				p_IdEmpresaRol : $scope.SesionIdEmpresa,
				p_IdEstado:$scope.recordData.estado
			};
			
			var post = r.CrearRol(datosCreacion);
			
			bcsComunes.ventanaOverlay($scope.ventanaId, true);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay($scope.ventanaId, false);
				
				if(result.P_OUT_NEW_ROL_ID!=null && result.P_OUT_NEW_ROL_ID>0){
					var promesa = bcsComunes.ventanaMsg({ 
					    mensaje: $translate.instant("UI_ROL_CREADO_OK"),
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
				$scope.configData.bloqueoCrearRol = false;
			});
		}
	};
	
});
