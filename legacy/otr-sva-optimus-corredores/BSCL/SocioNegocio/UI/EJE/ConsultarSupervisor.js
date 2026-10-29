angular.module('ModuloSocioNegocio').
	controller('ConsultarSupervisorCtrl',
			function($scope,$translate, bcsComunes, $resource){
	var ventanaId = bcsComunes.ventanaWinId($scope);
	$scope.data = {};
	
	
	$scope.gridSupervisor = {
			columns :
			[
			 	{field: "NombreCompleto",title: $translate.instant('UI_NOMBRE_SUPERVISOR'),align: "left",width: "200px"}
				,{field: "CodigoEmpleado",title: $translate.instant('UI_CODIGO_EMPLEADO'),align: "left",width: "80px"}
				,{field: "NombreEstado",title: $translate.instant('UI_ESTADO'),align: "left",width: "100px"}	
			],
	  		selectable : "row",
	  		dataSource : new bcs.DataSource({
	  			type: 'odata',
	  			definition : {
	  				serverFiltering: true,
	            	serverPaging: true,
	            	serverSorting: true,
	           	 	pageSize:20,
	        		sort: [//{ field: "NombreTipoDocumentoIdentidadDefecto", dir: "asc" },
	              	  	//{ field: "DocumentoIdentidadDefecto", dir: "asc" },
	                   	//{ field: "NombreCompleto", dir: "asc" }
	                ],
	            	transport: {
	                	read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1466",
	                		dataType: "json"
	                	}
	            	},
	            	schema: {
		        		model: {
	    	        		fields: {
	    	        			IdEmpresa : { type: "number"}
	    	        			,IdEmpleadoSupervisor : { type: "number"}
	    	        			,NombreCompleto : { type: "string"}
	    	        			,CodigoEmpleado : { type: "string"}
	    	        			,NombreEstado : { type: "string"}
		        			}
		        		}
		        	}
	  			},
	  		}),
			pageable: true,
			pageSize: 20,
			autoBind:false,
			height:300,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			}
		};
	
	var modificarEstadoSupervisorSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1677";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };
	
	$scope.buscar = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa});
		
		if ($scope.data.NombreEmpleadoFiltro){
    		filters.push({ field: "NombreCompleto", operator: "contains", value: $scope.data.NombreEmpleadoFiltro });
    	}
    	if ($scope.data.NombreUsuarioFiltro){
    		filters.push({ field: "CodigoEmpleado", operator: "contains", value: $scope.data.NombreUsuarioFiltro });
    	}
    			
		$scope.gridSupervisor.dataSource.filter(filters);
	};
	
	$scope.limpiarFiltros = function(){
		$scope.data.NombreEmpleadoFiltro = "";
		$scope.data.NombreUsuarioFiltro = "";
	};
	
	$scope.Ingresar = function() {
		var id1 = bcsComunes.ventanaAbrir("modalSupervisor",{},false,false, ventanaId); 
	    var promesa=bcsComunes.ventanaPromesa(id1);                               
	    $.when(promesa).then(function(respuesta){ 
	    	$scope.buscar();
	    });
	};
	
	$scope.Bloquear = function() {
		bcsComunes.ventanaOverlay(ventanaId,true);
		var parametrosSP = {
    			p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			   	: 'SOC',
    		   	p_UsaReglasNegocio	   	: 'N',
    		   	p_IdEmpleadoSupervisor	: $scope.seleccionGrilla.IdEmpleadoSupervisor,
    		   	p_IdEstado				: 'BLO',
    	};
    	
		modificarEstadoSupervisorSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaOverlay(ventanaId,false);
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Supervisor Bloqueado",
						conflictos : util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						$scope.buscar();	
					});
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ventanaId,false);
				});
	};
	
	$scope.Desbloquear = function() {
		bcsComunes.ventanaOverlay(ventanaId,true);
		var parametrosSP = {
    			p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
    		   	p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
    		   	p_IdModulo			   	: 'SOC',
    		   	p_UsaReglasNegocio	   	: 'N',
    		   	p_IdEmpleadoSupervisor	: $scope.seleccionGrilla.IdEmpleadoSupervisor,
    		   	p_IdEstado				: 'ACT',
    	};
    	
		modificarEstadoSupervisorSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaOverlay(ventanaId,false);
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Supervisor Desbloqueado",
						conflictos : util.parseServerResponse(result)}, ventanaId);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						$scope.buscar();	
					});
				},
				function(result){
					bcsComunes.ventanaOverlay(ventanaId,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	};
	
	$scope.Ejecutivos = function() {
		
		if($scope.seleccionGrilla.IdEmpleadoSupervisor){
			var id1 = bcsComunes.
				ventanaAbrir("ConsultarEjecutivo",
						{IdEmpleadoSupervisor: $scope.seleccionGrilla.IdEmpleadoSupervisor},false,false,ventanaId); 
		    var promesa=bcsComunes.ventanaPromesa(id1);                               
		}
	};
	
	


	
});