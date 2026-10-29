angular.module('ModuloSocioNegocio').
	controller('ModalEjecutivoCtrl',
			function($scope,$translate,$resource, bcsComunes){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	$scope.data.IdEmpleadoSupervisorDefecto = parametros.IdEmpleadoSupervisor;
	
	$scope.filtrosEjecutivo = {
			IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa}
	};
	
	$scope.filtrosSupervisor = {
			IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa}
	};
	
	var ingresarEjecutivoSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1471";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };

	$scope.ValidarIngreso = function ValidarIngreso(){
		let ejecutivoDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1470",
						dataType: "json"
					}
				},
				filter: { field: "IdEmpleadoEjecutivo", operator: "eq", value: $scope.data.IdEmpleadoEjecutivo},
				schema: {
					model: {
						fields : {
							IdEmpleadoSupervisor : { type: "number"},
						}
					}
				}
			}
		});
		ejecutivoDatasource.query().then(
			function(){
			let dataEjecutivo = $.extend({},ejecutivoDatasource.data()[0]);
			if(dataEjecutivo.IdEmpleadoSupervisor === undefined || dataEjecutivo.IdEmpleadoSupervisor === null){
				Ingresar();
			}else{
				bcsComunes.ventanaOverlay(ventanaId,false);
            	bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_EJECUTIVO_ASIGNADO'));
			}
		});
	}
    
    let Ingresar = function Ingresar(){
    	var parametrosSP = {
    			p_IdEmpresa			   : $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			   : $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			   : 'SOC',
    		   	p_UsaReglasNegocio	   : 'N',
    		   	p_IdEmpleadoSupervisor : $scope.data.IdEmpleadoSupervisor,
    		   	p_IdEmpleadoEjecutivo  : $scope.data.IdEmpleadoEjecutivo,
    		   	p_IdEstado			   : $scope.data.IdEstado,
    	};
    	
    	ingresarEjecutivoSP(parametrosSP).then(
				function(result) {
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Ejecutivo Ingresado",
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