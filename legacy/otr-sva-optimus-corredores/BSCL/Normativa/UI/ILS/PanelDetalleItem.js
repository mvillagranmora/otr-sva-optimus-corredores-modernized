(function() {
	angular.module('ModuloNormativa').controller('PanelDetalleItemCtrl',
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
		var pDetalleItem = this;
		pDetalleItem.idVentana= bcsComunes.ventanaWinId($scope);
		pDetalleItem.win		= {};
		pDetalleItem.grilla	= {};
		pDetalleItem.cmbbx	= {};
		pDetalleItem.filtro	= {};
		pDetalleItem.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.pDetalleItem.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
	
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaDetalleItem.removeAll();
	    	//REPROCESO 
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pDetalleItem.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pDetalleItem.kngFechaContable);
	    	bcsServiciosNormativa.ItemDetalleResultadoBuscar(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADOITEMDETALLE,function(key,value)
	    		{
	    			dsGrillaDetalleItem.data().push(value);
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
		var dsDefILSDetalleItem = 
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
                    						   NombreConcepto       	: {type: "string"},
                    						   Posicion					: {type: "string"},
                    						   NombreItem				: {type: "string"},
                    						   Monto					: {type: "number"},
                    						   CuentaContable			: {type: "number"},
                    						   SaldoDebeHaber			: {type: "number"},
                    						   Condicion				: {type: "string"},
                    						   MontoCondicion			: {type: "string"},
                    						   Operacion				: {type: "string"},
                    						   Factor					: {type: "string"},
                    						   MontoCalculado			: {type: "string"},
											}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaDetalleItem = new bcs.DataSource(dsDefILSDetalleItem);
		$scope.confDetalleItem = 
		{
    				dataSource		: dsGrillaDetalleItem,
					columns		: [  
   				    {field: "NombreConcepto", title: "UI_NOMBRE_CONCEPTO", width: "130px", sortable : false}
   				    ,{field: "Posicion", title: "UI_POSICION", width: "90px", sortable : false }
   				    ,{field: "NombreItem", title: "UI_NOMBRE_ITEM", width: "120px", sortable : false }
   				    ,{field: "Monto", title: "UI_MONTO", width: "90px", sortable : false }				       				    
   				    ,{field: "CuentaContable", title: "UI_CUENTA_CORRIENTE", width: "90px", sortable : false }				       				    
   				    ,{field: "SaldoDebeHaber", title: "UI_D_H", width: "50px",  sortable : false }	
   				    ,{field: "Condicion", title: "UI_CON", width: "50px",  sortable : false }				       				    
   				    ,{field: "MontoCondicion", title: "UI_MONTO_CONDICION", width: "80px",  sortable : false }				       				    
   				    ,{field: "Operacion", title: "UI_OPE", width: "50px",  sortable : false }				       				    
   				    ,{field: "Factor", title: "UI_FACTOR", width: "60px", sortable : false }				       				    
   			    	,{field: "MontoCalculado", title: "UI_MONTO_CALCULADO", width: "90px",  sortable : false }
				   			    	
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
		
//		// Función para buscar el DetalleItem 
//		pDetalleItem.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pDetalleItem.fn.limpiar = function(){
			$scope.pDetalleItem.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaDetalleItem.removeAll();
		}
	});
}());