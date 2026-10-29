(function() {
	angular.module('ModuloNormativa').controller('PanelDetalleSaldoCuentaContableCtrl',
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
		var pDetalleSaldoCuentaContable = this;
		pDetalleSaldoCuentaContable.idVentana= bcsComunes.ventanaWinId($scope);
		pDetalleSaldoCuentaContable.win		= {};
		pDetalleSaldoCuentaContable.grilla	= {};
		pDetalleSaldoCuentaContable.cmbbx	= {};
		pDetalleSaldoCuentaContable.filtro	= {};
		pDetalleSaldoCuentaContable.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.pDetalleSaldoCuentaContable.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
	
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaDetalleSaldoCuentaContable.removeAll();
	    	//REPROCESO 
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pDetalleSaldoCuentaContable.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pDetalleSaldoCuentaContable.kngFechaContable);
	    	bcsServiciosNormativa.DetalleSaldoCuentaContableLlenaGrilla(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADODETALLECOBERTURA,function(key,value)
	    		{
	    			dsGrillaDetalleSaldoCuentaContable.data().push(value);
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
		var dsDefILSDetalleSaldoCuentaContable = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdOperacion", dir: "DESC" },
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
														Titulo					: {type: "string"},
														Categoria				: {type: "string"},
														MontoGarantia    		: {type: "string"},
														MontoBancosyEmpresas	: {type: "number"},
														FactorA					: {type: "number"},
														FactorB 			    : {type: "number"},
														MontoCobertura    		: {type: "number"},
														MontoEntidadFinanciera	: {type: "number"},
														Fecha					: {type: "string"},
														IdEmpresa				: {type: "number"},
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaDetalleSaldoCuentaContable = new bcs.DataSource(dsDefILSDetalleSaldoCuentaContable);
		$scope.confDetalleSaldoCuentaContable = 
		{
    				dataSource		: dsGrillaDetalleSaldoCuentaContable,
					columns		: [  {field: "Titulo", title: "UI_CARTERA_OBLIGACIONES", width: "130px", sortable : true }
									,{field: "Categoria", title: "UI_GLOSA", width: "130px", sortable : false }
				   				    ,{field: "MontoGarantia", title: "UI_MONTO_GARANTIA", width: "90px",  sortable : false }										
				   				    ,{field: "MontoBancosyEmpresas", title: "UI_BANCOS_Y_EMPRESAS", width: "100px", sortable : false}
				   				    ,{field: "FactorA", title: "UI_FACTOR_A", width: "90px",  sortable : false}				   				    
				   				    ,{field: "FactorB", title: "UI_FACTOR_B", width: "90px", sortable : false }
				   				    ,{field: "MontoCobertura", title: "UI_COBERTURA", width: "90px", sortable : false }				   				    
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
		
//		// Función para buscar el DetalleSaldoCuentaContable 
//		pDetalleSaldoCuentaContable.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pDetalleSaldoCuentaContable.fn.limpiar = function(){
			$scope.pDetalleSaldoCuentaContable.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaDetalleSaldoCuentaContable.removeAll();
		}
	});
}());