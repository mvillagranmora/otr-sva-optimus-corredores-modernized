angular.module('ModuloPlataforma').controller('AutorizacionesSolicitadasCtrl',function($scope,$translate, bcsComunes, bcsServiciosAUT, bcsConfiguracionVistaAutorizaciones){
	
	$scope.data = {};
	$scope.filtros = {};
	$scope.filtros.estado = "";
	$scope.kOptions = {};
	$scope.gridApiSol = {};
	$scope.seleccionEvento = {};
	const idVentana = bcsComunes.ventanaWinId($scope); 
	$scope.usuarioSolicitante = $scope.$root.SesionIdUsuario;
	
	$scope.data.oConfiguracionEstadoEvento={
		tipo : "combobox-estado-evento",
		configuracion : {},
		filtros : {
			NombreEntidad: { field: "NombreEntidad", operator: "eq", value: 'AUT.EVENTO' }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoEvento={
		tipo : "combobox-tipo-evento",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.Autorizador = function(dataItem) {
		return {
			dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceAutorizador(dataItem),
			columns				: bcsConfiguracionVistaAutorizaciones.columnasAutorizador(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
			height				: 100
		};
	};
	
	$scope.EventoSol = {
		dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceEventoSolicitada($scope.$root.SesionIdUsuario),
		columns				: bcsConfiguracionVistaAutorizaciones.columnasEvento(),		
		sortable			: true,
		pageable			: true,
		autoBind			: false,  //comentar
		height				: 450,
		selectable 			: "row",
		filterable			: true,
		groupable			: true,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true
		}
	};
		
	$scope.Etapa = function(dataItem) {
		return {
			dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceEtapa(dataItem),
			columns				: bcsConfiguracionVistaAutorizaciones.columnasEtapa(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
		    selectable 			: "row"
		};
	};

	$scope.EventoSol.gridCreated = function(grid){// para evitar que la tabla se renderice vacia sin altura
		$("#grid-eventoSol .k-grid-content.k-auto-scrollable").css("height", "352px");	
	}
	
	$scope.filtros.fecInicio  = util.date($scope.SesionFechaSistema).hour(0).minute(0).second(0).toDate();
	$scope.filtros.fecTermino = util.date($scope.SesionFechaSistema).add(23,'hours').minute(59).second(59).toDate();

	$scope.limpiar = function(){
		$scope.filtros.fecIniciong 		= "";
		$scope.filtros.fecTerminong 	= "";
		$scope.filtros.idEstado	   		= 0;
		$scope.filtros.idEvento			= 0;
		$scope.filtros.fecInicio  = util.date($scope.SesionFechaSistema).hour(0).minute(0).second(0).toDate();
		$scope.filtros.fecTermino = util.date($scope.SesionFechaSistema).add(23,'hours').minute(59).second(59).toDate();
	};
	
	let filtroInicial = function(){
		$scope.EventoSol.dataSource.filter([{ field: "IdUsuario", operator: "eq", value: $scope.$root.SesionIdUsuario}]);
	};
	
	//Filtros para el grid
	$scope.Buscar = function(){
		let filters = new Array();
			
		if ($scope.$root.SesionIdEmpresa !== undefined  && $scope.$root.SesionIdEmpresa != "")
			filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		if ($scope.usuarioSolicitante !== undefined && $scope.usuarioSolicitante != "")
			filters.push({ field: "IdUsuario", operator: "eq", value: $scope.usuarioSolicitante});
		
		if ($scope.filtros.estadoObj.IdEstado !== undefined  && $scope.filtros.estadoObj.IdEstado != "0")
			filters.push({ field: "IdEstado", operator: "eq", value: $scope.filtros.estadoObj.IdEstado });
		
		if ($scope.filtros.tipoEventoObj.IdTipoEvento !== undefined  && $scope.filtros.tipoEventoObj.IdTipoEvento != "0")
			filters.push({ field: "IdTipoEvento", operator: "eq", value: $scope.filtros.tipoEventoObj.IdTipoEvento });
		
		if($scope.filtros.fecInicio !== null){			
			let resp = util.validarRangoFecha($scope.filtros.fecInicio, $scope.filtros.fecTermino);
			if(resp.estado){
				bcsComunes.msg("error", "Error", resp.mensaje);
				return;
			}
			
			if(util.date(util.dateOdata($scope.filtros.fecInicio)).add(1,'month').toDate() < util.dateOdata($scope.filtros.fecTermino)){
				bcsComunes.msg("error", "Error", $translate.instant('FECHA_RANGO_CONSULTA_INVALIDA'));
				return;
			}
			
    		filters.push({ field: "Fecha", operator: "gte", value: util.dateOdata($scope.filtros.fecInicio)   });
    	}
		
		if ($scope.filtros.fecTermino !== null){	
			filters.push({ field: "Fecha", operator: "lte", value: util.dateOdata($scope.filtros.fecTermino) });
    	}

		$scope.gridApiSol.clearSelection();
		$scope.EventoSol.dataSource.filter(filters);
	};
	
	
	//*******************************************
	// Llamada a servicio para modificar estado
	//*******************************************
	
	$scope.modificarEstado = function(p_estado) {
		bcsComunes.ventanaOverlay(idVentana,true);
		
		if ($scope.seleccionEvento !== undefined){
			bcsComunes.ventanaOverlay(idVentana,true);
			if ($scope.seleccionEvento !== undefined){
		   	    let datosModificar = {
					p_IdEmpresa			: $scope.$root.SesionIdEmpresa, 
			   	    p_IdUsuario			: $scope.$root.SesionIdUsuario,
			   	    p_IdModulo			: 'AUT',//$scope.seleccionEvento.IdModulo,
			   	    p_simularOperacion	: 'N',
			   	    p_UsaReglasNegocio	: 'N',
		   	    	p_Etapas			: [{IdEvento: 0, IdEtapa: 0}],
		   	    	p_Eventos 			: [],
		   	    	p_EventoEtapa		: 'EVENTO',   		
		   	    	p_IdEstado			: p_estado,
		   	    	p_Observaciones		: ' '
				};
		   	    
	   	    	datosModificar.p_Eventos.push({IdEvento:$scope.seleccionEvento.IdEvento});	
	    	
				bcsServiciosAUT.ModificarEstadoEventoAutorizacion(datosModificar).$promise.then(
					function(result) {
						let mensajeId = bcsComunes.ventanaMsg({ conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(mensajeId).then(function(){
							$scope.seleccionEvento.IdEstado = undefined;
							bcsComunes.ventanaOverlay(idVentana,false);
							if (result.P_OUT_ERRORESCONTADOR === 0){
								$scope.EventoSol.dataSource.read();
							}else{
								$scope.data.btnActivo = true;
							}
						});
					},
					function(result){   		   		
		        		bcsComunes.msg("error","Error Interno", JSON.stringify(result));
						$scope.data.btnActivo   = true;   
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		}
	};
});