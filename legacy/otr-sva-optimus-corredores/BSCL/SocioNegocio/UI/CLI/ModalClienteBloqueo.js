angular.module('ModuloSocioNegocio').controller('ModalClienteBloqueoCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.dataCliente = {};
	
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	
	$scope.cerrar = function () {
		bcsComunes.ventanaCerrar(ventanaId); 
	};
	
	var bloqueoClienteSP = function(v_Dato)
    {
	       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=96";
	       var r = $resource
         ("", {}, {
	                 'urlConsulta': { method: "POST", url: urlConsulta}
                  }
         );
	       var post = r.urlConsulta(v_Dato);
	       return post.$promise;
    };
	
    
		

    $scope.bloqueoCliente = function () {
    	
    	var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'CLI';
        parametrosSP.p_UsaReglasNegocio = "N";
        parametrosSP.p_IdCliente = parametros.IdCliente;
        parametrosSP.p_IdEstado = 'INA';
        parametrosSP.p_IdTipoBloqueo = $scope.dataCliente.IdTipoBloqueo;
        parametrosSP.p_FechaBloqueo = $scope.dataCliente.FechaBloqueo;
        parametrosSP.p_ComentarioBloqueo = $scope.dataCliente.ComentarioBloqueo;
    	    	
		bloqueoClienteSP(parametrosSP).then(
				function(result) {

					bcsComunes.ventanaMsg({ mensaje : "Cliente Bloqueado",
						conflictos : util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaCerrar(ventanaId);
				},
				function(result){
					
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
        
    };
    
    
	
	
	
	
	
});