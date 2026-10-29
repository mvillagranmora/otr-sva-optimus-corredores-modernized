(function() {
	angular.module('ModuloNormativa').controller('PanelPosicionesDescubiertasCtrl',
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
		var pPosicionesDescubiertas = this;
		pPosicionesDescubiertas.idVentana= bcsComunes.ventanaWinId($scope);
		pPosicionesDescubiertas.win		= {};
		pPosicionesDescubiertas.grilla	= {};
		pPosicionesDescubiertas.cmbbx	= {};
		pPosicionesDescubiertas.filtro	= {};
		pPosicionesDescubiertas.fn		= {};
		
		// Obtiene la variable scope
		$scope.data = {};
		$scope.winID = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
		
		// Obtiene fecha de inicio del sistema 
		$scope.pPosicionesDescubiertas.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
	
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaPosicionesDescubiertas.removeAll();
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pPosicionesDescubiertas.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pPosicionesDescubiertas.kngFechaContable);
	    	bcsServiciosNormativa.PosicionesDescubiertasLlenaGrilla(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADOPosicionesDescubiertas,function(key,value)
	    		{
	    			dsGrillaPosicionesDescubiertas.data().push(value);
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
		var dsDefILSPosicionesDescubiertas = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "Rut", dir: "ASC" },
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
													NombreDelComitente	: {type: "string"},
													Rut					: {type: "string"},
													FechaLiquidacion	: {type: "string"},
													FechaOperacion		: {type: "string"},
													NumeroPoliza		: {type: "string"},
													Mercado				: {type: "string"},
													Operacion			: {type: "string"},
													Cobertura			: {type: "string"},
													Moneda				: {type: "string"},
													GarantiaDinero		: {type: "string"},
													GarantiaValores		: {type: "string"},
													TotalGarantias		: {type: "string"},
													Descubierta			: {type: "string"},
													Incomplimientos		: {type: "string"},
													Total				: {type: "string"}
												}
                    						}
		        	            	}

    			 }
		};	
				
		var dsGrillaPosicionesDescubiertas = new bcs.DataSource(dsDefILSPosicionesDescubiertas);
		$scope.confPosicionesDescubiertas = 
		{
    				dataSource		: dsGrillaPosicionesDescubiertas,
					columns		: [  {field: "NombreDelComitente", title: "UI_NOMBRE", width: "130px", sortable : true }
									,{field: "Rut", title: "UI_RUT", width: "130px", sortable : true }
									,{field: "FechaLiquidacion", title: "UI_FECHA_LIQUIDACION", width: "130px", sortable : true }
									,{field: "FechaOperacion", title: "UI_FECHA_OPERACION", width: "130px", sortable : true }
									,{field: "NumeroPoliza", title: "UI_NUMERO_POLIZA", width: "130px", sortable : true }
									,{field: "Mercado", title: "UI_MERCADO", width: "130px", sortable : true }
									,{field: "Operacion", title: "UI_OPERACION", width: "130px", sortable : true }
									,{field: "Cobertura", title: "UI_COBERTURA", width: "130px", sortable : true }
									,{field: "Moneda", title: "UI_MONEDA", width: "130px", sortable : true }
									,{field: "GarantiaDinero", title: "UI_GARANTIA_EN_DINERO", width: "130px", sortable : true }
									,{field: "GarantiaValores", title: "UI_GARANTIA_EN_VALORES", width: "130px", sortable : true }
									,{field: "TotalGarantias", title: "UI_TOTAL_GARANTIAS", width: "130px", sortable : true }
									,{field: "Descubierta", title: "UI_DESCUBIERTA", width: "130px", sortable : true }
									,{field: "Incomplimientos", title: "UI_INCOMPLIMIENTOS", width: "130px", sortable : true }
									,{field: "Total", title: "UI_TOTAL", width: "130px", sortable : true }
									
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
		
//		// Función para buscar el PosicionesDescubiertas 
//		pPosicionesDescubiertas.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 
		pPosicionesDescubiertas.fn.limpiar = function(){
			$scope.pPosicionesDescubiertas.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
			dsGrillaPosicionesDescubiertas.removeAll();
		}
	});
}());