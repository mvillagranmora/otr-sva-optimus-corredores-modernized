(function() {
	angular.module('ModuloNormativa').controller('PanelItemsCtrl',
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
		var pItems = this;
		pItems.idVentana= bcsComunes.ventanaWinId($scope);
		pItems.win		= {};
		pItems.grilla	= {};
		pItems.cmbbx	= {};
		pItems.filtro	= {};
		pItems.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.pItems.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
	
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaItems.removeAll();
	    	//REPROCESO 
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pItems.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pItems.kngFechaContable);
	    	bcsServiciosNormativa.ItemResultadoBuscar(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADOITEM,function(key,value)
	    		{
	    			dsGrillaItems.data().push(value);
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
		var dsDefILSItems = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdOperacion", dir: "DESC" },
                    pageSize        : 15,
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{ 
													NombreIndice       			: {type: "string"},
													NombreConcepto				: {type: "string"},
													Posicion					: {type: "string"},
													NombreItem					: {type: "string"},
													Monto						: {type: "number"},
													Operacion					: {type: "string"},	
													Factor						: {type: "string"},													
													MontoCalculado				: {type: "number"},
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaItems = new bcs.DataSource(dsDefILSItems);
		$scope.confItems = 
		{
    				dataSource		: dsGrillaItems,
					columns		: [  {  field: "NombreIndice", title: "UI_NOMBRE_INDICE", width: "150px", sortable : false}
										,{field: "NombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "150px", sortable : false }
										,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }
										,{field: "NombreItem", title: "UI_NOMBRE", width: "100px", sortable : false }
					   				    ,{field: "Monto", title: "UI_MONTO", width: "90px",  sortable : false }
					   				    ,{field: "Operacion", title: "UI_OPER", width: "50px", sortable : false}
					   				    ,{field: "Factor", title: "UI_FACT", width: "60px", sortable : false }				   				    
					   				    ,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px",  sortable : false}
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
    				    autoBind 		: false		
       	};		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
//		// Función para buscar el resultado 
//		pItems.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pItems.fn.limpiar = function(){
			$scope.pItems.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaItems.removeAll();
		}
	});
}());