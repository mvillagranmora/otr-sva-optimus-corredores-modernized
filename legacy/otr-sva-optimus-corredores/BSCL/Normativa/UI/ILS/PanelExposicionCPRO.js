(function() {
	angular.module('ModuloNormativa').controller('PanelExposicionCPROCtrl',
	function (
			  bcsAlerta,
			  bcsComunes,
			  bcsConsultasMesa,
			  bcsServiciosNormativa,
			  $http,
			  $modal,
			  $scope,
			  $state,
			  $timeout, 
			  $translate
			  ) {

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		
		//Obtiene datos desde la ventana principal
		var pExposicionCPRO = this;
		pExposicionCPRO.idVentana= bcsComunes.ventanaWinId($scope);
		pExposicionCPRO.win		= {};
		pExposicionCPRO.grilla	= {};
		pExposicionCPRO.cmbbx	= {};
		pExposicionCPRO.filtro	= {};
		pExposicionCPRO.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.pExposicionCPRO.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
	
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaExposicionCPRO.removeAll();
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pExposicionCPRO.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pExposicionCPRO.kngFechaContable);
	    	bcsServiciosNormativa.ExposicionCPROLlenaGrilla(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADOExposicionCPRO,function(key,value)
	    		{
	    			dsGrillaExposicionCPRO.data().push(value);
	    		});
	    		$scope.data.texto = false;
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}

	    };
	    var error = function(respuesta)
	    {
			bcsComunes.ventanaMsg({
				//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			overlay.encendido = false;
			bcsComunes.ventanaOverlay($scope.winID, false);
	    	
	    };

		
		/*********************************************
		 * CONFIGURACION DE GRILLAS
		 *********************************************/
		var dsDefILSExposicionCPRO = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "Nemotecnico", dir: "DESC" },
                    pageSize        : 15,
//                    transport		: {
//	            		read:  
//	            		{
//	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
//							dataType:"json"
//	            		}
//	            	},
                    schema 			: { 
                    				   model: 
                    						{
												fields :  
												{
													Nemotecnico					: {type: "string"},
													DelDia						: {type: "string"},
													EnGarantia					: {type: "string"},
													Contable					: {type: "string"},
													PorCompromisos				: {type: "string"},
													TotalTenencia				: {type: "string"},
													DeudasValores				: {type: "string"},
													Precio						: {type: "string"},
													FactorAseguramiento			: {type: "string"},
													ContableValorizada			: {type: "string"},
													PorCompromisosValorizada	: {type: "string"},
													DeudasValoresValorizada		: {type: "string"},
													TotalValorizado				: {type: "string"},
													ValoresEnCirculacion		: {type: "string"},
													PorcentajeTenencia			: {type: "string"},
													FactorPonderacion			: {type: "string"},
													TenenciaPonderada			: {type: "string"},
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaExposicionCPRO = new bcs.DataSource(dsDefILSExposicionCPRO);
		$scope.confExposicionCPRO = 
		{
    				dataSource		: dsGrillaExposicionCPRO,
					columns		: [  {field: "Nemotecnico", title: "UI_NEMOTECNICO", width: "130px", sortable : true }
									,{field: "DelDia", title: "UI_DEL_DIA", width: "130px", sortable : true }
									,{field: "EnGarantia", title: "UI_EN_GARANTIA", width: "130px", sortable : true }
									,{field: "Contable", title: "UI_CONTABLE", width: "130px", sortable : true }
									,{field: "PorCompromisos", title: "UI_POR_COMPROMISOS", width: "130px", sortable : true }
									,{field: "TotalTenencia", title: "UI_TOTAL_TENENCIA", width: "130px", sortable : true }
									,{field: "DeudasValores", title: "UI_DEUDAS_VALORES", width: "130px", sortable : true }
									,{field: "Precio", title: "UI_PRECIO", width: "130px", sortable : true }
									,{field: "FactorAseguramiento", title: "UI_FACTOR_ASEGURAMIENTO", width: "130px", sortable : true }
									,{field: "ContableValorizada", title: "UI_CONTABLE_VALORIZADA", width: "130px", sortable : true }
									,{field: "PorCompromisosValorizada", title: "UI_POR_COMPRAS_VALORIZADA", width: "130px", sortable : true }
									,{field: "DeudasValoresValorizada", title: "UI_DEUDAS_VALORES_VALORIZADA", width: "130px", sortable : true }
									,{field: "TotalValorizado", title: "UI_TOTAL_VALORIZADO", width: "130px", sortable : true }
									,{field: "ValoresEnCirculacion", title: "UI_VALORES_EN_CIRCULACION", width: "130px", sortable : true }
									,{field: "PorcentajeTenencia", title: "UI_PORCENTAJE_TENENCIA", width: "130px", sortable : true }
									,{field: "FactorPonderacion", title: "UI_FACTOR_PONDERACION", width: "130px", sortable : true }
									,{field: "TenenciaPonderada", title: "UI_TENENCIA_PONDERADA", width: "130px", sortable : true }
								  ],		
    					sortable		: true,
    					pageable		: true,
    					filterable		: true,
    					navigatable 	: true,
//    					userSettings 	: true,
    					height			: 500,
    					excel 			: { allPages: true },
    					scrollable		: true,
    					selectable 		: "row",
    				    autoBind 		: true		
       	};		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
//		// Función para buscar el ExposicionCPRO 
//		pExposicionCPRO.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pExposicionCPRO.fn.limpiar = function(){
			$scope.pExposicionCPRO.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaExposicionCPRO.removeAll();
		}
	});
}());