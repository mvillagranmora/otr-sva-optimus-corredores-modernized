angular.module('ModuloPlataforma').controller('MantenedorCamposFormularioObligatoriosCtrl',function ($scope,bcsComunes,$translate,bcsServicioPlataforma) {
	var mantenedorCamposFormularioObligatorios = this;
	var idVentana=bcsComunes.ventanaWinId($scope);
	
	mantenedorCamposFormularioObligatorios.formData = {};
	mantenedorCamposFormularioObligatorios.filtros = {};
	mantenedorCamposFormularioObligatorios.datos = {};
	
	mantenedorCamposFormularioObligatorios.formData.filtrosCanal = {
    	PermiteEnrolamiento :  { field: "PermiteEnrolamiento", operator: "eq", value: 'S'}
	}

	
	mantenedorCamposFormularioObligatorios.configuracionGrilla = {
			dataSource: new bcs.DataSource({
				type		: "call",
				definition 	: {
					pageSize 		: 200,
					callTransaction	: "700413",
					callResultSet	: "P_OUT_FORMULARIO",					
					sort: { field: "CodigoFormulario", dir: "desc" },
					schema: {
						model: {
							fields: {
								CodigoFormulario			: { type: "string"},
								NombreFormulario			: { type: "string"},
								CodigoCampo					: { type: "string"},
								CodigoCampoFrontend			: { type: "string"},
								CodigoCampoBackend    		: { type: "string"},
								NombreCampo					: { type: "string"},
								TipoDato					: { type: "string"},
								ValorDefecto				: { type: "string"},
								CantidadMinima				: { type: "number"},
								Requerido					: { type: "string"},
								RegeneraContratos			: { type: "string"},
								IdTipoCanal					: { type: "string"},
								NombreCanal					: { type: "string"},
								ValorDefectoCampoFormulario	: { type: "string"},
								NombreValorDefectoCampoFormulario: { type: "string"},
								AplicaRequerido: { type: "string"}
								
							}
						}
					}
				}
			}),
			columns: [
			    {field: "NombreFormulario"		,title: $translate.instant('UI_FORMULARIO'),  width: "150px", align: "left" , sortable : true, editable: false },
			    {field: "NombreCampo"			,title: $translate.instant('UI_CAMPO'), width: "150px", align: "left" , sortable : true, editable: false },
				{field: "NombreCanal"			,title: $translate.instant('UI_CANAL'), width: "100px", align: "left" , sortable : true, editable: false },
			    {field: "CantidadMinima"		,title: $translate.instant('UI_CANTIDAD_MINIMA'), width: "75px", align: "left" , sortable : true, editable: false },
			    {field: "Requerido"				,title: $translate.instant('UI_REQUERIDO'), width: "50px", align: "left" , sortable : true, editable: false },
  				{field: "RegeneraContratos"		,title: $translate.instant('UI_REGENERA_CONTRATOS'), width: "50px", align: "left" , sortable : true, editable: false },
			    {field: "NombreValorDefectoCampoFormulario"			,title: $translate.instant('UI_VALOR_DEFECTO'), width: "150px", align: "left" , sortable : true, editable: false }
			],
			gridCreated : function(grid){
				mantenedorCamposFormularioObligatorios.ajustaVentana();
			},
			sortable			: true,
			pageable			: true,
			autoBind			: true,
	    	filterable			: true,
			height				: 550,
		    selectable 			: "row",
		    userSettings		: true,
			navigatable			: true
	};

  	mantenedorCamposFormularioObligatorios.editar = function (dato) {
		if(!dato) return;
		var id = bcsComunes.ventanaAbrir("EditarCampoFormularioObligatorio",dato,false); 
        var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	mantenedorCamposFormularioObligatorios.configuracionGrilla.dataSource.read();
        	mantenedorCamposFormularioObligatorios.datos.kNgModel = undefined;
        }); 
  	};

	mantenedorCamposFormularioObligatorios.ajustaVentana = function() {
		 bcsComunes.ventanaAutoHeight(idVentana);
    }

	mantenedorCamposFormularioObligatorios.buscar = function() {
		
		var parametrosConsulta = {};
		
		parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo = "PLA";		
		parametrosConsulta.p_CodigoFormulario = mantenedorCamposFormularioObligatorios.filtros.CodigoFormulario;
		parametrosConsulta.p_CodigoCampo =mantenedorCamposFormularioObligatorios.filtros.CodigoCampo
		parametrosConsulta.p_IdTipoCanal =mantenedorCamposFormularioObligatorios.filtros.IdTipoCanal
		mantenedorCamposFormularioObligatorios.configuracionGrilla.dataSource.setCallParameters(parametrosConsulta);
	};
	
	mantenedorCamposFormularioObligatorios.limpiarFiltros = function() {
  		mantenedorCamposFormularioObligatorios.filtros.CodigoFormulario = undefined;
		mantenedorCamposFormularioObligatorios.filtros.CodigoCampo = undefined;
		mantenedorCamposFormularioObligatorios.filtros.IdTipoCanal = undefined;
	};
		
	mantenedorCamposFormularioObligatorios.limpiarFiltros();
});