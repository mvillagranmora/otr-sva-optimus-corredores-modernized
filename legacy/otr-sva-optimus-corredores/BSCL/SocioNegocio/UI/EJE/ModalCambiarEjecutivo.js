angular.module('ModuloSocioNegocio').
	controller('ModalCambiarEjecutivoCtrl',
			function($scope,$translate,$resource, bcsComunes){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	if(parametros){	
		$scope.data.IdClienteDefecto = parametros.IdSocioDeNegocioCliente;
		$scope.data.IdPortafolioDefecto = parametros.IdPortafolio;
		$scope.data.IdEmpleadoEjecutivoDefecto = parametros.IdEmpleadoEjecutivo;
		$scope.data.EjecutivoPrincipal = parametros.EsEjecutivoPrincipal==="S" ? true : false;
		$scope.data.ClientePrivado = parametros.EsEjecutivoExclusivo==="S" ? true : false;
	}
	
	$scope.data.configuracionSocioNegocio = {
		filtros: {
			IdEmpresa 		: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
			//EstadoCliente 	: { "field"   : "EstadoCliente","operator": "eq", "value"   :  'ACT' },
			EsCliente 		: {"field"   : "EsCliente","operator": "eq", "value"   :  'UI_SI' }
		},
	}
	
	$scope.data.configuracionPortafolio = {
			filtros: {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
			},
		}
	
	var ModificarEjecutivoSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1488";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };
    
    $scope.Modificar = function() {
    	console.log($scope.data.clienteSeleccionado);
    	var parametrosSP = {
    			p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			   	: 'SOC',
    		   	p_UsaReglasNegocio	   	: 'N',
    		   	p_IdCliente				: $scope.data.portafolioSeleccionado.IdCliente,
    		   	p_IdPortafolio			: $scope.data.portafolioSeleccionado.IdPortafolio,
    		   	p_IdEmpleadoEjecutivo	: $scope.data.IdEmpleadoEjecutivo,
    		   	p_EsPrincipal			: $scope.data.EjecutivoPrincipal ? 'S':'N',
    		   	p_ClientePrivado		: $scope.data.ClientePrivado ? 'S' : 'N'
    	};
    	
    	ModificarEjecutivoSP(parametrosSP).then(
				function(result) {
					
					var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : "Cliente Ejecutivo Modificado.",
						conflictos : util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaPromesa(idMensajeModal).then(function(){
						bcsComunes.ventanaCerrar(ventanaId);	
					});
				},
				function(result){
					
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
   	    
   	  
	};
	
	
	
	


	
});