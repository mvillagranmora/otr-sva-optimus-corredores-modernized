(function() {
	angular.module('ModuloMesa').controller('PanelDetalleCoberturaCtrl',
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
		var pDetalleCobertura = this;
		pDetalleCobertura.idVentana= bcsComunes.ventanaWinId($scope);
		pDetalleCobertura.win		= {};
		pDetalleCobertura.grilla	= {};
		pDetalleCobertura.cmbbx	= {};
		pDetalleCobertura.filtro	= {};
		pDetalleCobertura.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.data.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
		
		/*********************************************
		 * CONFIGURACION DE GRILLAS
		 *********************************************/
		var dsDefILSDetalleCobertura = 
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
													fields : 
													{
														FechaOperacionOPE					: {type: "systime"},
														NombreInstrumento       			: {type: "string"},
														MontoMonedaPrincipal				: {type: "number"},
														MontoMonedaSecundaria				: {type: "number"},
														ResultadoMonedaLocalPuntas 			: {type: "number"},
														ResultadoMonedaLocalBidAsk 			: {type: "number"},
														ResultadoMonedaLocalDO 				: {type: "number"},
													}
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaDetalleCobertura = new bcs.DataSource(dsDefILSDetalleCobertura);
		$scope.confDetalleCobertura = 
		{
    				dataSource		: dsGrillaDetalleCobertura,
					columns		: [  {field: "FechaOperacionOPE", title: "UI_CARTERA_OBLIGACIONES", width: "130px", sortable : true }
									,{field: "NombreInstrumento", title: "UI_GLOSA", width: "130px", sortable : false }
				   				    ,{field: "MontoMonedaPrincipal", title: "UI_MONTO_GARANTIA", width: "90px",  sortable : false }										
				   				    ,{field: "MontoMonedaSecundaria", title: "UI_BANCOS_Y_EMPRESAS", width: "100px", sortable : false}
				   				    ,{field: "ResultadoMonedaLocalPuntas", title: "UI_FACTOR_A", width: "90px",  sortable : false}				   				    
				   				    ,{field: "ResultadoMonedaLocalDO", title: "UI_FACTOR_B", width: "90px", sortable : false }
				   				    ,{field: "ResultadoMonedaLocalDO", title: "UI_COBERTURA", width: "90px", sortable : false }				   				    
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
		
//		// Función para buscar el detalleCobertura 
//		pDetalleCobertura.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pDetalleCobertura.fn.limpiar = function(){
			pDetalleCobertura.filtro.IdEmpresa= 0;
			pDetalleCobertura.filtro.IdReporte = 0;
			$scope.data.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaDetalleCobertura.removeAll();
		}
	});
}());