angular.module('ModuloSocioNegocio').controller('ModalSucursalAtencionCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
		
	$scope.Cerrar = function () {
		bcsComunes.ventanaCerrar(ventanaId); 
	};

	var agregarDocumentoSP = function(v_Dato)
    {
	       var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=20036";
	       var r = $resource
         ("", {}, {
	                 'url': { method: "POST", url: url}
                  }
         );
	       var post = r.url(v_Dato);
	       return post.$promise;
    };

    $scope.oConfiguracionSucursal = {
    	filtros : {IdPais: {"field":"IdEmpresa", "operator": "eq", "value": $scope.SesionIdEmpresa}}
    };
	
	$scope.agregar = function () {
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'SOC';        

        parametrosSP.p_IdSucursal = $scope.data.IdSucursalActual;
        parametrosSP.p_IdEmpleado = parametros.IdEmpleado;
    	bcsComunes.ventanaOverlay(ventanaId,true);	
        agregarDocumentoSP(parametrosSP).then(function(result) {
			var titulo = "UI_PUNTO_ATENCION_INGRESADO";
			if (result.P_OUT_ERRORESCONTADOR>0){
				titulo = result.P_OUT_DESCRIPCION || "La operacion tiene errores";
			}
        	var idModal = bcsComunes.ventanaMsg({ mensaje : $translate.instant(titulo),
							conflictos : util.parseServerResponse(result)}, ventanaId);

			bcsComunes.ventanaPromesa(idModal).then(function(){
				bcsComunes.ventanaOverlay(ventanaId,false);	
				bcsComunes.ventanaCerrar(ventanaId);				
			});
		},function(result){
			bcsComunes.msg("error","Error","La operacion tiene errores");
			bcsComunes.ventanaOverlay(ventanaId,false);	
		});
	};
	
});