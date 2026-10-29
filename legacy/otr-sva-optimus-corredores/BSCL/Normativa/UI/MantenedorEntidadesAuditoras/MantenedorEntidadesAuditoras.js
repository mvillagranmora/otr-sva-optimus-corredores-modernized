angular.module('ModuloNormativa').controller('MantenedorEntidadesAuditorasCtrl',function($scope,bcsComunes,$resource,$translate){
	
	$scope.data = {};
	$scope.showDetalle = false;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	
	
	ModeloEntidadesAuditoras = function() {
		return {
			model: {
    	        	fields:{
    	        		IdEntidadAuditora		: { type : "number"},
						EmpresaAuditora			: { type : "string"},
						AuditorEncargado			: { type : "string"},
    	        		TipoCasilla				: { type : "string"},
    	        		Estado					: { type : "string"},
						Casilla					: { type : "string"}
    	        		
    	        	} 
			}
		};
	};
	
	dataSourceEntidadesAuditoras = function() {
	return new bcs.DataSource({
			type : "odata", 
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				pageSize : 50,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306008", //asignar ODATA
						dataType : "json"
					}
				},
				schema : ModeloEntidadesAuditoras(),
				
			}
		});
	};
	
	columnasEntidadesAuditoras = function() {
		return [ 
    	   	    {field: "EmpresaAuditora", title: $translate.instant('UI_EMPRESA_AUDITORA'), width: "150px", attributes:{style:"text-align:center;"} }
				,{field: "AuditorEncargado", title: $translate.instant('UI_AUDITOR_ENCARGADO'), width: "150px", attributes:{style:"text-align:center;"} }
				,{field: "TipoCasilla", title: $translate.instant('UI_TIPO_CASILLA'), width: "150px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "Casilla", title: $translate.instant('UI_CASILLA'), width: "75px", attributes:{style:"text-align:center;"}  }
    	   	    ,{field: "Estado", title: $translate.instant('UI_ESTADO'), width: "100px", attributes:{style:"text-align:center;"} }
    	   	    ];
	};
	
	 
	
	
	$scope.EntidadesAuditoras = {
			dataSource			: dataSourceEntidadesAuditoras(),
			columns				: columnasEntidadesAuditoras(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
			height				: 280,
		    selectable 			: "row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable			: true,
		};
	

        $scope.CrearEntidadAuditora = function() {
            bcsComunes.ventanaOverlay(idVentana,true);
            var ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarEntidadesAuditoras",{ AccionBoton : 'CREAR' },true,false);
            var promesa = bcsComunes.ventanaPromesa(ventanaModalId);   
            
            $.when(promesa).then(function(){
                bcsComunes.ventanaOverlay(idVentana,false);
                $scope.gridApiAtributos.dataSource.read();
             }); 
            
        };

		$scope.ModificarEntidadAuditora = function() {
            bcsComunes.ventanaOverlay(idVentana,true);
            var ventanaModalId = bcsComunes.ventanaAbrir("AgregarModificarEntidadesAuditoras",{ IdEntidadAuditora : $scope.data.seleccionEntidadesAuditoras.IdEntidadAuditora,
																								EmpresaAuditora : $scope.data.seleccionEntidadesAuditoras.EmpresaAuditora,
																								AuditorEncargado : $scope.data.seleccionEntidadesAuditoras.AuditorEncargado,
																								TipoCasilla : $scope.data.seleccionEntidadesAuditoras.TipoCasilla,
																								Casilla : $scope.data.seleccionEntidadesAuditoras.Casilla,
																								Estado : $scope.data.seleccionEntidadesAuditoras.Estado,
																								AccionBoton : 'MODIFICAR' },true,false);
            var promesa = bcsComunes.ventanaPromesa(ventanaModalId);   
            
            $.when(promesa).then(function(){
                bcsComunes.ventanaOverlay(idVentana,false);
                $scope.gridApiAtributos.dataSource.read();
             }); 
            
        };
	
});