angular.module('ModuloSocioNegocio').
	controller('ModalSupervisorCtrl',
			function($scope,$translate,$resource, bcsComunes){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	
	var ingresarSupervisorSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1467";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };
    
    $scope.Ingresar = function() {
    	var parametrosSP = {
    			p_IdEmpresa			: $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			: $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			: 'SOC',
    		   	p_UsaReglasNegocio	: 'N',
    		   	p_IdEmpleadoSupervisor : $scope.data.IdEmpleadoSupervisor,
    		   	p_IdEstado			   : $scope.data.IdEstado,
    	};
    	
    	ingresarSupervisorSP(parametrosSP).then(
				function(result) {
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Supervisor Ingresado",
						conflictos : util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaCerrar(ventanaId);
					});
				},
				function(result){
					
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
   	    
   	  
	};
	
	
	
	


	
});