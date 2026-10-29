angular.module('ModuloPlataforma').controller('AutorizacionesMasivasCtrl',function($scope,$translate, bcsComunes, bcsServiciosAUT, bcsConfiguracionVistaAutorizaciones){


	$scope.data = {};
	$scope.filtros = {}
	$scope.filtros.estado = "";
	$scope.kOptions = {};
	$scope.IdUsuario = $scope.$root.SesionIdUsuario;
	$scope.seleccionEvento = {};
	$scope.botonesHabilitados = true;
	
	const idVentana = bcsComunes.ventanaWinId($scope); 
	
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
	
	$scope.data.oConfiguracionTipoAutorizacion={
		tipo : "combobox-tipo-autorizacion",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : 'EVENTO'
	};
	
	$scope.EventoMasivo =  {
		dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceEvento(),
		columns				: bcsConfiguracionVistaAutorizaciones.columnasEvento(),		
		sortable			: true,
		pageable			: true,
		autoBind			: false, 
		height				: 450,
	    selectable 			: "multiple, row",
	    filterable			: true,
	    userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
	    detailExpand: function(e) {
	        console.log(e);
	        e.sender.select(e.masterRow);
	    },
		hideDetailButton : 	function(row) {
			let hide = false
			if($scope.data.oConfiguracionTipoAutorizacion.idActual === 'EVENTO')
				hide = true;
			return hide;
			},
	};
	
	$scope.EtapaMasivo = function(dataItem) {
		return {
			dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceEtapa(dataItem),
			columns				: bcsConfiguracionVistaAutorizaciones.columnasEtapa(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
		    selectable 			: "row",
		    detailExpand: function(e) {
		        console.log(e);
		        e.sender.select(e.masterRow);
		    }
		};
	};
		
	var datos = {
		p_IdEmpresa : $scope.$root.SesionIdEmpresa,
		p_IdUsuario : $scope.$root.SesionIdUsuario,
		p_IdModulo : 'AUT',
		p_UsaReglasNegocio : 'S'
	};
	
	bcsServiciosAUT.VencerEventoAutorizacion(datos).$promise.then(
		function(result) {
			//$scope.EventoMasivo.dataSource.read();
		},
		function(result){   		
			bcsComunes.msg("error","Error Interno", JSON.stringify(result));
			bcsComunes.ventanaOverlay(idVentana,false);
	});	
		
	//Filtros de busqueda
	$scope.filtros.fecInicio  = util.dateInicioDia($scope.SesionFechaSistema); //rev fecha
	$scope.filtros.fecTermino = util.dateTerminoDia($scope.SesionFechaSistema); //rev fecha	
	$scope.filtros.idDefecto  = 'VIG';
	
	
	//****************************************
	// Fin Consulta Fecha de Sistema
	//****************************************
	
	$scope.limpiar = function(){
		$scope.filtros.fecIniciong 		= "";
		$scope.filtros.fecTerminong 	= "";
		$scope.filtros.idEstado	   		= 0;
		$scope.filtros.idDefecto		= 'VIG';
		$scope.filtros.idEvento			= 0;
		$scope.filtros.fecInicio  		= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fecTermino 		= util.dateTerminoDia($scope.SesionFechaSistema);
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
				bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
				return;
			}
    		filters.push({ field: "Fecha", operator: "gte", value: util.dateOdata($scope.filtros.fecInicio)   });
    	}
		
		if($scope.filtros.fecTermino !== null){	
    		filters.push({ field: "Fecha", operator: "lte", value: util.dateOdata($scope.filtros.fecTermino) });
    	}
		
		$scope.EventoMasivo.dataSource.filter(filters);
	};
	
	//*******************************************
	// Llamada a servicio para modificar estado
	//*******************************************
	$scope.modificarEstado = function(p_estado) {
		bcsComunes.ventanaOverlay(idVentana,true);
		if ($scope.seleccionEvento !== undefined){
	   	    let datosModificar = {
		   	    p_IdEmpresa			: $scope.$root.SesionIdEmpresa, 
		   	   	p_IdUsuario			: $scope.$root.SesionIdUsuario,
		   	    p_IdModulo			: 'AUT',
		   	    p_simularOperacion	: 'N',
		   	   	p_UsaReglasNegocio	: 'N',
		   	 	p_Eventos			: [], 
		   	 	p_Etapas			: [],
		   	 	p_IdEstado			: p_estado,
	   	    	p_Observaciones		: ' '
	   	    };
	   	    
	   	 	if($scope.data.oConfiguracionTipoAutorizacion.idActual === 'EVENTO'){
	   	    	angular.forEach($scope.seleccionEvento, function(value, key) {
		   	    	datosModificar.p_Eventos.push({ IdEvento: value.IdEvento });	
				});
	   	    	datosModificar.p_EventoEtapa = 'EVENTO';
	    	}
	    	else //ETAPA
   	    	{
				let etapaSeleccionada = false;

			    angular.forEach($scope.seleccionEvento, function(value, key) {
			        if (value.seleccionEtapa && value.seleccionEtapa.IdEtapaEvento) {
			            datosModificar.p_Etapas.push({ IdEvento: value.IdEvento, IdEtapa: value.seleccionEtapa.IdEtapaEvento });
			            etapaSeleccionada = true; 
			        }
			    });
			
			    if (!etapaSeleccionada) {
			        bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("Debe seleccionar al menos una Etapa."));  
					bcsComunes.ventanaOverlay(idVentana,false);
			        return;
			    } else {
			        datosModificar.p_EventoEtapa = 'ETAPA';
			    }
   	    	}

			bcsServiciosAUT.ModificarEstadoEventoAutorizacion(datosModificar).$promise.then(
				function(result) {
					var mensajeId = bcsComunes.ventanaMsg({ mensaje : "Los datos fueron modificados",
						conflictos : util.parseServerResponse(result)}, idVentana);
					
					bcsComunes.ventanaPromesa(mensajeId).then(function(){
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR === 0){
							$scope.EventoMasivo.dataSource.read();
						}
					});
				},
				function(result){   		
					bcsComunes.msg("error","Error Interno", JSON.stringify(result));
					bcsComunes.ventanaOverlay(idVentana,false);
			});	
		}
	};

	
	$scope.abrirMantenedorAutorizaciones = function(){
		let mantenedorAutorizaciones = bcsComunes.ventanaAbrir("MantenedorAutorizaciones",{},false,true); 
        let promesa=bcsComunes.ventanaPromesa(mantenedorAutorizaciones);                               
        $.when(promesa).then(function(respuesta){ 
        	//algo
        });
	};

	util.refreshGridSize('grid-evento-masivo');
	
});