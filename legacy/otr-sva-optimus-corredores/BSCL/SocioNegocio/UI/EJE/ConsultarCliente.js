angular.module('ModuloSocioNegocio').
	controller('ConsultarClienteCtrl',
			function($scope,$translate, bcsComunes){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	if(parametros)
	{
		$scope.data.IdEmpleadoSupervisorDefecto = parametros.IdEmpleadoSupervisor; 
		$scope.data.IdEmpleadoEjecutivoDefecto = parametros.IdEmpleadoEjecutivo;
	}
	
	$scope.data.Ejecutivo = {
			filtros: {
				IdEmpresa 				: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
				IdEmpleadoSupervisor 	: {"field"   : "IdEmpleadoSupervisor","operator": "eq", "value"   :  0 }
			},
		};
	
	$scope.data.Supervisor = {
			filtros: {
				IdEmpresa 				: {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
			},
		}
	
	$scope.gridCliente = {
			columns :
			[
			 	{field: "NombreCompletoCliente",title: $translate.instant('UI_NOMBRE_CLIENTE'),align: "left",width: "150px"}
				,{field: "CodigoPortafolio",title: $translate.instant('UI_CODIGO_PORTAFOLIO'),align: "left",width: "80px"}
				,{field: "NombreCompletoEjecutivo",title: $translate.instant('UI_NOMBRE_EJECUTIVO'),align: "left"}	
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
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1480",
	                		dataType: "json"
	                	}
	            	},
	            	schema: {
		        		model: {
	    	        		fields: {
	    	        			IdEmpleadoSupervisor : { type: "number"}
	    	        			,IdEmpleadoEjecutivoPortafolio : { type: "number"}
	    	        			,IdCliente : { type: "number"}
	    	        			,NombreCompletoCliente : { type: "string"}
	    	        			,CodigoPortafolio : { type: "string"}
	    	        			,NombreCompletoEjecutivo : { type: "string"}
	    	        			,IdPortafolio : {type:"number"}
	    	        			
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
	
	$scope.buscar = function(){
		var filters = new Array();
		
		if($scope.data.IdEmpleadoSupervisor){
			filters.push({ field: "IdEmpleadoSupervisor", operator: "eq", value: $scope.data.IdEmpleadoSupervisor });
		}
				
		if ($scope.data.IdEmpleadoEjecutivo){
    		filters.push({ field: "IdEmpleadoEjecutivoPortafolio", operator: "eq", value: $scope.data.IdEmpleadoEjecutivo });
    	}
    			
		$scope.gridCliente.dataSource.filter(filters);
	};
	
	$scope.limpiarFiltros = function(){
		$scope.data.IdEmpleadoSupervisor  = undefined;
		$scope.data.IdEmpleadoEjecutivo = undefined;
	};
	
	$scope.CambiarEjecutivo = function() {
		if($scope.seleccionGrilla && 
		 (($scope.seleccionGrilla.EsEjecutivoExclusivo ==="S" &&  $scope.seleccionGrilla.IdEmpleadoEjecutivoPortafolio === BCS_SESION.IdEmpleado) || $scope.seleccionGrilla.EsEjecutivoExclusivo==="N")
		){
			var id1 = bcsComunes.ventanaAbrir("modalCambiarEjecutivo",
					{IdEmpleadoEjecutivo: $scope.data.IdEmpleadoEjecutivo,
					IdSocioDeNegocioCliente: Number($scope.seleccionGrilla.IdSocioDeNegocioCliente),
					IdPortafolio: Number($scope.seleccionGrilla.IdPortafolio),
					EsEjecutivoExclusivo: $scope.seleccionGrilla.EsEjecutivoExclusivo,
					EsEjecutivoPrincipal: $scope.seleccionGrilla.EsEjecutivoPrincipal,
					}, false, false, ventanaId); 
		    var promesa=bcsComunes.ventanaPromesa(id1);                               
		    $.when(promesa).then(function(respuesta){ 
		    	$scope.buscar();
		    	$scope.seleccionGrilla=undefined;
		    });
		}else{
			bcsComunes.msg("error","Error","El Cliente es Privado.");
		}
	};
	
	
	
	
	
	


	
});