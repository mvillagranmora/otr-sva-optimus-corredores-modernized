angular.module('ModuloSocioNegocio').
	controller('ModalReemplazoCtrl',
			function($scope,$translate,$resource, bcsComunes){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	bcsComunes.ventanaTitulo(ventanaId,$translate.instant('UI_REEMPLAZO_EJECUTIVO')+ ' ' + parametros.NombreEjecutivo); 
	
	$scope.data.IdEmpleadoEjecutivo = parametros.IdEmpleadoEjecutivoAnterior;
	
	$scope.data.filtroEjecutivo = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
			IdEstado : {field: "IdEstado", operator: "eq", value: 'ACT'}
		};
	
	
	var ingresarEjecutivoSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1478";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };
    
    $scope.Ingresar = function() {
    	var parametrosSP = {
    			p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			   	: 'SOC',
    		   	p_UsaReglasNegocio	   	: 'N',
    		   	p_IdEmpleadoEjecutivo	: $scope.data.IdEmpleadoEjecutivo,
    		   	p_IdEmpleadoReemplazo 	: $scope.data.IdEmpleadoReemplazo,
    		   	p_FechaInicioReemplazo  : util.dateInicioDia($scope.data.FechaInicio),
    		   	p_FechaFinReemplazo		: util.dateTerminoDia($scope.data.FechaFin)
    	};
    	
    	ingresarEjecutivoSP(parametrosSP).then(
				function(result) {
				
					var idModalMensaje = bcsComunes.ventanaMsg({ mensaje : "Reemplazo Ingresado",
						conflictos : util.parseServerResponse(result)}, ventanaId);
						
						bcsComunes.ventanaPromesa(idModalMensaje).then(function(){
							bcsComunes.ventanaCerrar(ventanaId);							
						});
					
				},
				function(result){
					
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
   	    
   	  
	};
	
	
	
	


	
});