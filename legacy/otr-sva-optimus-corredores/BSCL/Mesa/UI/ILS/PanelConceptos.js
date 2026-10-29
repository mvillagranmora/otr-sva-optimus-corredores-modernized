(function() {
	angular.module('ModuloMesa').controller('PanelConceptosCtrl',
	function (
			  bcsAlerta,
			  bcsComunes,
			  bcsConsultasMesa,
			  bcsServiciosMesa,
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
		$scope.data.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
		
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
                    sort 			: { field: "IdOperacion", dir: "DESC" },
                    pageSize        : 15,
                    transport		: {
		        	            		read: 
		        	            		{
		        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=513",
		        							dataType:"json"
		        	            		}
		        	            	},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													NombreInstrumento       			: {type: "string"},
													NombreCliente						: {type: "string"},
													FechaOperacionOPE					: {type: "systime"},												
													PlazoResidual						: {type: "number"},
													IdMonedaSecundaria					: {type: "string"},	
													Plazo								: {type: "number"},													
													Agente								: {type: "number"},
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaConceptos = new bcs.DataSource(dsDefILSConceptos);
		$scope.confConceptos = 
		{
    				dataSource		: dsGrillaConceptos,
					columns		: [  {field: "NombreInstrumento", title: "UI_ID_INDICE", width: "150px", sortable : false}
									,{field: "NombreCliente", title: "UI_NOMBRE", width: "150px", sortable : false }
									,{field: "FechaOperacionOPE", title: "UI_POSICION", width: "80px", sortable : false }									
				   				    ,{field: "PlazoResidual", title: "UI_CONCEPTO", width: "90px",  sortable : false }
				   				    ,{field: "IdMonedaSecundaria", title: "UI_OPER", width: "50px", sortable : false}
				   				    ,{field: "Plazo", title: "UI_FACT", width: "50px", sortable : false }				   				    
				   				    ,{field: "Agente", title: "UI_MONTO_CALCULADO", width: "100px",  sortable : false}				   				    
				   			    	
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
//		pConceptos.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pConceptos.fn.limpiar = function(){
			pConceptos.filtro.IdEmpresa= 0;
			pConceptos.filtro.IdReporte = 0;
			$scope.data.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaConceptos.removeAll();			
		}
	});
}());