angular.module('ModuloPlataforma').controller('VistaAutorizacionesCtrl',function($scope,$translate, bcsComunes, bcsServiciosAUT, bcsConfiguracionVistaAutorizaciones){

	$scope.data = {};
	$scope.filtros = {}
	$scope.filtros.estado = "";
	$scope.kOptions = {};
	$scope.IdUsuario = $scope.$root.SesionIdUsuario;
	$scope.seleccionEvento = {};
	$scope.gridApiEvento;
	
	var idVentana = bcsComunes.ventanaWinId($scope); 
	
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
			filtros : {
			
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
	$scope.Evento =  {
		dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceEvento(),
		columns				: bcsConfiguracionVistaAutorizaciones.columnasEvento(),	
		userSettings		: true,	
		sortable			: true,
		pageable			: true,
		autoBind			: false,  //comentar
		height				: 450,
	    selectable 			: "multiple row",
	    filterable			: true,
	    userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
	    //groupable			: true,
	    detailExpandold: function(e) {
	        console.log(e);
	        e.sender.select(e.masterRow);
	    }
	};
	
	$scope.Etapa = function(dataItem) {
		return {
			dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceEtapa(dataItem),
			columns				: bcsConfiguracionVistaAutorizaciones.columnasEtapa(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
		    selectable 			: "row",
		    detailExpandold: function(e) {
		        console.log(e);
		        e.sender.select(e.masterRow);
		    }
		};
	};
	
	$scope.Autorizador = function(dataItem) {
		return {
			dataSource			: bcsConfiguracionVistaAutorizaciones.dataSourceAutorizador(dataItem),
			columns				: bcsConfiguracionVistaAutorizaciones.columnasAutorizador(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
			height				: 183
		};
	};	
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'AUT';
	datos.p_UsaReglasNegocio = 'S';

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
		$scope.filtros.idEstado			= 'VIG';
		$scope.filtros.idEvento			= 0;
		$scope.filtros.fecInicio  = util.dateInicioDia($scope.SesionFechaSistema);  //rev fecha
		$scope.filtros.fecTermino = util.dateTerminoDia($scope.SesionFechaSistema); //rev fecha	
	};
	
	//Filtros para el grid
	$scope.Buscar = function(){
		var filters = new Array();
			
		if ($scope.$root.SesionIdEmpresa !== undefined  && $scope.$root.SesionIdEmpresa != "")
			filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		if ($scope.usuarioSolicitante !== undefined && $scope.usuarioSolicitante != "")
			filters.push({ field: "IdUsuario", operator: "eq", value: $scope.usuarioSolicitante});
		
		if ($scope.filtros.estadoObj.IdEstado !== undefined  && $scope.filtros.estadoObj.IdEstado != "0")
			filters.push({ field: "IdEstado", operator: "eq", value: $scope.filtros.estadoObj.IdEstado });
		
		if ($scope.filtros.tipoEventoObj.IdTipoEvento !== undefined  && $scope.filtros.tipoEventoObj.IdTipoEvento != "0")
			filters.push({ field: "IdTipoEvento", operator: "eq", value: $scope.filtros.tipoEventoObj.IdTipoEvento });
		
		if($scope.filtros.fecInicio !== null)
    	{	
			var resp = util.validarRangoFecha($scope.filtros.fecInicio, $scope.filtros.fecTermino);
			if(resp.estado){
				bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
				return;
			}
			if(util.date(util.dateOdata($scope.filtros.fecInicio)).add(1,'month').toDate() < util.dateOdata($scope.filtros.fecTermino)){
				bcsComunes.msg("error", "Error", $translate.instant('FECHA_RANGO_CONSULTA_INVALIDA'));
				return;
			}
			
    		filters.push({ field: "HoraRegistro", operator: "gte", value: util.dateSPseconddate($scope.filtros.fecInicio)   });
    	}
		if($scope.filtros.fecTermino !== null)
    	{	
			console.log(util.dateSPseconddate($scope.filtros.fecTermino));
    		filters.push({ field: "HoraRegistro", operator: "lte", value: util.dateSPseconddate($scope.filtros.fecTermino) });
    	}

		$scope.gridApiEvento.clearSelection();
		$scope.Evento.dataSource.filter(filters);
	};
	
	
	
	
	
	
	//*******************************************
	// Llamada a servicio para modificar estado
	//*******************************************
	$scope.solicitarRechazo = function() {

		var ventanaAnualar = bcsComunes.ventanaAbrir("ComentarioRechazaAutorizacion", {}, false, false,idVentana);
		var promesa=bcsComunes.ventanaPromesa(ventanaAnualar);
        promesa.then(function(data){
			if(data.Accion==='RZD'){
				$scope.modificarEstado(data.Accion, data.ComentarioRechazo);
			}
		});
	}

	$scope.modificarEstado = function(p_estado, p_Comentario ) {

		bcsComunes.ventanaOverlay(idVentana,true);
		if ($scope.seleccionEvento !== undefined){
	
	   	    let datosModificar = {
		 		p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
				p_IdUsuario			: $scope.$root.SesionIdUsuario,
				p_IdModulo			: 'AUT',
				p_simularOperacion	: 'N',
				p_UsaReglasNegocio	: 'N',
				p_Comentario		: '',
				p_Eventos 			: [{IdEvento: 0}],
				p_Etapas			: [],
				p_EventoEtapa		: 'ETAPA', 		
	   	    	p_IdEstado			: p_estado,
	   	    	p_Observaciones		: ' '
			};
			
			if(p_estado === 'RZD'){
				datosModificar.p_Comentario = p_Comentario;
			}
	   	   
   	    	angular.forEach($scope.seleccionEvento, function(value, key) {
	   	    	datosModificar.p_Etapas.push({IdEvento: value.IdEvento, IdEtapa: value.IdEtapaEvento});
			});
	    	
	    	bcsServiciosAUT.ModificarEstadoEventoAutorizacion(datosModificar).$promise.then(
				function(result) {
					let mensajeId = bcsComunes.ventanaMsg({ conflictos : util.parseServerResponse(result)}, idVentana);
					
					bcsComunes.ventanaPromesa(mensajeId).then(function(){
						$scope.seleccionEvento.IdEstado = undefined;
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR === 0){
							$scope.Evento.dataSource.read();
							$scope.gridApiEvento.clearSelection();
						}else{
							$scope.data.btnActivo = true;
						}
					});
				},
				function(){
					bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
					bcsComunes.ventanaOverlay(idVentana,false);
					$scope.data.btnActivo   = true;     //Activa botones
			});	       
		}		  	
	};	
	
	$scope.abrirMantenedorAutorizaciones = function(){
		var id1 = bcsComunes.ventanaAbrir("MantenedorAutorizaciones",{},false,false,idVentana); 
        var promesa=bcsComunes.ventanaPromesa(id1);                               
        $.when(promesa).then(function(){ 
			$scope.Buscar();
        });
	};
	
	$scope.DetalleAutorizacion = function (datos) {
		if(datos[0].IdTipoEvento=='INGORD' || datos[0].IdTipoEvento=='SOLPRO'){
			bcsComunes.ventanaAbrir("VDetalleAutorizacion", {IdEvento: datos[0].IdEvento}, false, false, idVentana );
		}
	};
});
