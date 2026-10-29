angular.module('ModuloSocioNegocio').
	controller('ModalTransferirClientesCtrl',
			function($scope,$translate,$resource, bcsComunes){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	$scope.filtroEjecutivo = {};
	$scope.data.IdEmpleadoEjecutivoAnteriorDefecto = parametros.IdEmpleadoEjecutivoAnterior;
	
	
	var TransferirClientesSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1494";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };
    
    $scope.Transferir = function() {
    	var parametrosSP = {
    			p_IdEmpresa			   			: $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			   			: $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			   			: 'SOC',
    		   	p_UsaReglasNegocio	   			: 'N',
    		   	p_IdEmpleadoEjecutivoAntiguo	: $scope.data.IdEmpleadoEjecutivoAnterior,
    		   	p_IdEmpleadoEjecutivoNuevo		: $scope.data.IdEmpleadoEjecutivoNuevo,
    		   
    	};
    	
    	TransferirClientesSP(parametrosSP).then(
				function(result) {
					var titulo = "";
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						titulo = "La operacion tiene advertencias";
					}
					if (result.P_OUT_ERRORESCONTADOR){
						titulo = "La operacion tiene errores";
					}
					else
					{
						titulo = "Clientes Transferidos";
					}

					var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
						conflictos : util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
						bcsComunes.ventanaCerrar(ventanaId);
					});
				},
				function(result){
					
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
   	    
   	  
	};
	
	$scope.filtroEjecutivo.ejecutivo = { 
			filtros : {  
				IdEmpresa 	: {field: "IdEmpresa", 	operator: "eq", value: ($scope.$root == null? $scope.SesionIdEmpresa: $scope.$root.SesionIdEmpresa) },
				IdEstado 	: {field: "IdEstado", 	operator: "eq", value: 'ACT' } 
			} 
		};
	
	
	


	
});