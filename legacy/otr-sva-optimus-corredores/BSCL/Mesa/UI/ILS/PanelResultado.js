(function() {
	angular.module('ModuloMesa').controller('PanelResultadoCtrl',
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
		var pResultado = this;
		pResultado.idVentana= bcsComunes.ventanaWinId($scope);
		pResultado.win		= {};
		pResultado.grilla	= {};
		pResultado.cmbbx	= {};
		pResultado.filtro	= {};
		pResultado.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.data.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
		
		/*********************************************
		 * CONFIGURACION DE GRILLAS
		 *********************************************/
		var dsDefILSResultado = 
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
													IdOperacion							: {type: "number"},
													NombreCliente						: {type: "string"},
													PlazoResidual						: {type: "number"},
													FechaOperacion						: {type: "sysdate"},
													Agente								: {type: "number"},
													FechaLiquidacion					: {type: "sysdate"},
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaResultados = new bcs.DataSource(dsDefILSResultado);
		$scope.confResultados = 
		{
    				dataSource		: dsGrillaResultados,
					columns		: [  {field: "IdOperacion", title: "UI_ID", width: "80px", sortable : true }
									,{field: "NombreCliente", title: "UI_NOMBRE", width: "150px", sortable : false }
				   				    ,{field: "PlazoResidual", title: "UI_INDICE", width: "100px",  sortable : false }										
				   				    ,{field: "FechaOperacion", title: "UI_FECHA_CONTABLE", width: "90px", sortable : false}
				   				    ,{field: "Agente", title: "UI_INDICE_ANTERIOR", width: "100px",  sortable : false}				   				    
				   				    ,{field: "FechaLiquidacion", title: "UI_FECHA_CONTABLE", width: "90px", sortable : false }
				   			    	
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
//		pResultado.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pResultado.fn.limpiar = function(){
			pResultado.filtro.IdEmpresa= 0;
			pResultado.filtro.IdReporte = 0;
			$scope.data.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaResultados.removeAll();
		}
	});
}());