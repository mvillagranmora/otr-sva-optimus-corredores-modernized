angular.module('ModuloTesoreria').controller('ConsultarConfiguracionChequerasCtrl',
		function($scope,bcsComunes,$translate,bcsServiciosTesoreria,bcsBlotterCuentaBancariaGrid, $resource){
	
	$scope.btnActivo =  true;
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	
	//GRILLA
	$scope.confBasicaConfiguracionChequeras = {
		dataSource			: bcsBlotterCuentaBancariaGrid.dsConfiguracionChequeras,
		columns				: bcsBlotterCuentaBancariaGrid.columnasconfiguracionChequeras(),		
		sortable			: true,
		pageable			: true,
		autoBind			: true,
		filterable			: true,
		height				: 360,
		selectable 			: "row" // "multiple row"
	};
	
	$scope.abrirModal = function(oConfiguracionChequera)
	{	
		var idModal = bcsComunes.ventanaAbrir("editarConfiguracionChequeras",{ ConfiguracionChequera: oConfiguracionChequera},false,false);
		
        var promesa = bcsComunes.ventanaPromesa(idModal);
        
        promesa.then(function(data){ 
        	if (data === true)
			{
        		//Actualiza datasource de la grilla
        		bcsBlotterCuentaBancariaGrid.dsConfiguracionChequeras.read();
			}
    	}); 
	};
	
	$scope.crearConfiguracionChequera = function(){		
		$scope.abrirModal(undefined);
	};
	
	$scope.modificarConfiguracionChequera = function(oConfiguracionChequera){		
		$scope.abrirModal(oConfiguracionChequera);
	};

	var EliminarConfiguracionChequera = function(v_Dato){
    	var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=192";
    	var r = $resource ("", {}, {
    		'urlConsulta': {method: "POST", url: urlConsulta}
    	});
    	var post = r.urlConsulta(v_Dato);
    	return post.$promise;
    };
    
	$scope.eliminarConfiguracionChequera = function (oConfiguracionChequera){
		bcsComunes.ventanaOverlay($scope.winID, true);
		var parametrosSP = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'TES',
				p_UsaReglasNegocio : 'S',
				p_IdConfiguracionChequera: oConfiguracionChequera.IdConfiguracionChequera,
				p_FechaModificacion : util.dateSPdate(new Date())
		};
		bcsComunes.log().info(JSON.stringify(parametrosSP));
		EliminarConfiguracionChequera(parametrosSP).then(
				function(result){ 
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						bcsComunes.msg("warning", "Advertencia", "La Operacion tiene advertencias");
					}else if (result.P_OUT_ERRORESCONTADOR){
						bcsComunes.msg("error", "Error", "La operacion tiene errores, " + result.P_OUT_ERRORESDETALLE[0].Descripcion);
						var i = 0;
			            for(i; i<result.P_OUT_ERRORESCONTADOR; i++)
			            {
			            	bcsComunes.log().info("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);       
			            }    
					}else {
						bcsComunes.msg("info", "info", "Operacion realizada con exito");						
						bcsBlotterCuentaBancariaGrid.dsConfiguracionChequeras.read();
					}
					bcsComunes.ventanaOverlay($scope.winID, false);
				},
				function(result){
					bcsComunes.msg("error", "Error", "La operacion tiene errores");
					bcsComunes.ventanaOverlay($scope.winID, false);
				});
	};
	
});