(function() {
	angular.module('ModuloNormativa').controller('PanelConceptosCtrl',
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
		var pConceptos = this;
		pConceptos.idVentana= bcsComunes.ventanaWinId($scope);
		pConceptos.win		= {};
		pConceptos.grilla	= {};
		pConceptos.cmbbx	= {};
		pConceptos.filtro	= {};
		pConceptos.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.pConceptos.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
	
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaConceptos.removeAll();
	    	//REPROCESO 
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pConceptos.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pConceptos.kngFechaContable);
	    	bcsServiciosNormativa.ConceptoResultadoBuscar(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADOCONCEPTO,function(key,value)
	    		{
	    			dsGrillaConceptos.data().push(value);
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
		var dsDefILSConceptos = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdIndice", dir: "DESC" },
                    pageSize        : 15,
                    transport		: {
		        	            		read: 
		        	            		{
		        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13028",
		        							dataType:"json"
		        	            		}
		        	            	},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													IdIndice   	            : {type: "number"},
													NombreIndice			: {type: "string"},
													IdIndiceNombre			: {type: "string"},
													IdResultadoIndice   	: {type: "number"},
													IdConcepto   	        : {type: "number"},
													NombreConcepto			: {type: "string"},
													IdConceptoNombre		: {type: "string"},
													IdResultadoConcepto		: {type: "number"},
													Monto   	            : {type: "number"},
													Posicion			    : {type: "string"},													
													Operador				: {type: "string"},													
													Factor					: {type: "string"},													
													MontoCalculado			: {type: "number"},
													Estado					: {type: "string"},													

												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaConceptos = new bcs.newDataSource(dsDefILSConceptos);
		$scope.confConceptos = 
		{
    				dataSource		: dsGrillaConceptos,
					columns		: [  
									 {field: "IdIndiceNombre", title: "UI_NOMBRE_INDICE", width: "150px", sortable : false }
									,{field: "IdConceptoNombre", title: "UI_NOMBRE_CONCEPTO", width: "450px", sortable : false }
									,{field: "Posicion", title: "UI_POSICION", width: "80px", sortable : false }									
									,{field: "NombreIndice", title: "UI_NOMBRE", width: "150px", sortable : false }
									,{field: "Monto", title: "UI_MONTO", width: "150px", sortable : false}
				   				    ,{field: "Operador", title: "UI_OPER", width: "50px", sortable : false}
				   				    ,{field: "Factor", title: "UI_FACT", width: "50px", sortable : false }				   				    
				   				    ,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "100px",  sortable : false}				   				    
				   			    	
								  ],		
    					sortable: true,
                                    pageable: true,
                                    filterable: true,
                                    navigatable: true,
                                    userSettings: true,
                                    height: 500,
                                    excel: { allPages: true },
                                    scrollable: true,
                                    selectable: 'row',
                                    autoBind: true
       	};		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
//		// Función para buscar el resultado 
//		pConceptos.fn.buscar = function(){
//
//		};
        $scope.$watch('data.seleccionGrilla',function(newval,oldval){
			if($scope.data.seleccionGrilla === undefined) return;
			$scope.data.habilitaDig = true;
		});

		
		// Función para limpiar los campos de la búsqueda 
		pConceptos.fn.limpiar = function(){
			$scope.pConceptos.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaConceptos.removeAll();			
		}


	});
}());