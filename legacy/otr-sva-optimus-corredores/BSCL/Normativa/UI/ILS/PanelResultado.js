(function() {
	angular.module('ModuloNormativa').controller('PanelResultadoCtrl',
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
		$scope.pResultado.kngFechaContable = util.dateInicioDia($scope.SesionFechaSistema);
		
		$scope.data.habilitaDig = false; 
		
	    $scope.data.BuscarResultado = function() {
	    	sptArray=[];
	    	dsGrillaResultados.removeAll();
	    	//REPROCESO 
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'ILS';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_IdTipoInforme		= $scope.pResultado.filtro.IdReporte;
			parametrosReproceso.p_FechaContable		= util.dateSPdate($scope.pResultado.kngFechaContable);
			parametrosReproceso.p_FechaEmision		= $scope.SesionFechaSistema;
	    	bcsServiciosNormativa.InformeResultadoBuscar(parametrosReproceso,successReproceso,error);
	    };


	    var successReproceso = function(respuesta)
	    {
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		$.each(respuesta.P_TABLARESULTADOINFORME,function(key,value)
	    		{
	    			dsGrillaResultados.data().push(value);
	    		});
	    		$scope.data.habilitaDig = false;
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
		var dsDefILSResultado = 
		{
				type : "array",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdResultadoIndice", dir: "ASC" },
                    pageSize        : 15,
                    //Prueba
                    transport		: {
	            		read: {
	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=13041",
							dataType:"json"
	            		}
	            	},
                    //
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													IdResultadoIndice	     			: {type: "number"},
													NombreIndice 						: {type: "string"},
													IdIndice    						: {type: "number"},
													FechaContable						: {type: "sysdate"},
													IdIndiceAnterior					: {type: "number"},
													FechaContableAnterior				: {type: "sysdate"},
												}
                    						}
		        	            	}

    			 }
		};	
		var dsGrillaResultados = new bcs.newDataSource(dsDefILSResultado);
		$scope.confResultados = 
		{
    				dataSource		: dsGrillaResultados,
					columns		: [  {field: "IdResultadoIndice", title: "UI_ID_INDICE", width: "80px", sortable : true }
									,{field: "NombreIndice", title: "UI_NOMBRE_INDICE", width: "200px", sortable : false }
				   				    ,{field: "IdIndice", title: "UI_INDICE", width: "60px", template : "#: (util.bcs_formato(FactorIndice,'ILS','Tasa','USD','','INDICE')).valor #"}										
				   				    ,{field: "FechaContable", title: "UI_FECHA_CONTABLE", width: "90px", sortable : false}
				   				    ,{field: "IdIndiceAnterior", title: "UI_INDICE_ANTERIOR", width: "60px", template : "#: (util.bcs_formato(FactorIndice,'ILS','Tasa','USD','','INDICE')).valor #"}			   				    
				   				    ,{field: "FechaContableAnterior", title: "UI_FECHA_CONTABLE_ANTERIOR", width: "100px", sortable : false }
				   			    	
								  ],		
    					sortable		: true,
    					pageable		: true,
    					filterable		: true,
    					navigatable 	: true,
    					userSettings 	: true,
    					height			: 500,
    					excel 			: { allPages: true },
    					scrollable		: true,
    					selectable 		: "row",
    				    autoBind 		: false		

       	};		

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		$scope.data.DigitarResultadoUI = function()
	    {
		
				var items = {Reporte: $scope.pResultado.reporte.seleccionado.IdTipoInforme, FechaContable: $scope.pResultado.kngFechaContable, IdEmpresa: $scope.pResultado.empresa.seleccionado.IdEmpresa};
				var IdVentanamodal = bcsComunes.ventanaAbrir("ILSMantenedorDigitado", {items : items} , true,true);
		        var promesa=bcsComunes.ventanaPromesa(IdVentanamodal);
		        promesa.then(function(data){
		        	if(typeof data !== 'undefined')
		        		if(data.Resultado === 'OK')
		        		{
		        			bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_ESTADO_ACTUALIZADO"),
								conflictos:util.parseServerResponse(response)
							},idVentana);
		        			 
		        		}
		        		else if (data.Resultado === 'ERROR')
		        		{
		    				bcsComunes.ventanaMsg({
		  				      mensaje:$translate.instant("UI_ERROR_ACTUALIZAR"),
		  				      conflictos:{
		  				       errores : [ {
		  				           Codigo : 'COD',
		  				           Descripcion : ''
		  				          } ],
		  				      }
		    				},idVentana);
		        		}	
		        	$scope.data.habilitaDig = true;
		        	//$scope.data.filtrarMonedaPorProducto();
		        }); 
					
		};
		
		$scope.$watch('data.seleccionGrilla',function(newval,oldval){
			if($scope.data.seleccionGrilla === undefined) return;
			
			$scope.data.habilitaDig = true; 
		});
		
//		// Función para buscar el resultado 
//		pResultado.fn.buscar = function(){
//
//		};
		
		// Función para limpiar los campos de la búsqueda 

	});
}());