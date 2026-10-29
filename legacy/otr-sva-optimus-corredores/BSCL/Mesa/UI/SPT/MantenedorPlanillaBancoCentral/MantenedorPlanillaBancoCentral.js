(function() {
	angular.module('ModuloMesa').controller('MantenedorPorProducto',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
		
		//var ingSpot = this;
		
		$scope.data.datosAntes = {};
		$scope.data.datosDespues = {};
		$scope.data.datosAntes = angular.copy($scope.data.datosDespues);
		$scope.data.MovimientosSeleccionados = {};
		$scope.data.HabilitoBotonesMovimiento = false;

		//Configurar al principio
		//moment js

		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.SesionFechaSistema =$scope.SesionFechaSistema //util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.data.modificar = function(){
			$scope.data.abrirModal($scope.data.MovimientosSeleccionados);
		};

		$scope.$watch('data.MovimientosSeleccionados',function(newval,oldval){
			if($scope.data.MovimientosSeleccionados !== undefined
				&& $scope.data.MovimientosSeleccionados.length > 0 && $scope.SesionFechaSistema === util.dateSPdate($scope.data.MovimientosSeleccionados[0].FechaOperacion) ){
				$scope.data.HabilitoBotonesMovimiento = true;
			}else{
				$scope.data.HabilitoBotonesMovimiento = false;
			}
		});
		
		$scope.data.abrirModal = function(MovimientosSeleccionados){
			var id1 = bcsComunes.ventanaAbrir("ModalEditarCodigoComercio",{ Movimientos: MovimientosSeleccionados,},false,true);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		$scope.data.MovimientosSeleccionados = undefined;
	        		$scope.data.buscarMovimientos();
				}
	    	});
		};
		
		//---------------------------------------------
		/*------------------*/
		var error = function(respuesta)
		{
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID, false);
		};	
		
		//---------------------------------------------
		var defDataSource = function(){
			var dsDefConsultaMovimientosSPT = 
			{
					type : "odata",
					definition : 
					{
	                    serverFiltering : true, // Filtrado de datos en el servidor.
	                    serverPaging    : true, // Paginado de los datos en el servidor.
	                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
	                    sort 			: { field: "IdOperacion", dir: "DESC" },
	                    pageSize        : 20,
	                    transport		: {
			        	            		read: {
			        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=439",
			        							dataType:"json"
			        	            		}
			        	            	},
	                    schema 			: { 
	                    				   model: 
	                    						{
													fields : 
													{
														IdOperacion							: {type: "number"},
														FechaOperacion						: {type: "sysdate"},
														TipoOperacion						: {type: "string"},
														IdTipoCanalIngreso					: {type: "string"},
														IdTipoInstrumento					: {type: "string"},
														RutCliente							: {type: "string"},
														NombreCliente						: {type: "string"},
														CodigoPropositoPortafolio			: {type: "string"},
														CodigoComercio						: {type: "string"},
														CodigoYNombrePlanillaBancoCentral	: {type: "string"}
													}
												}
										  }

	    			 }
			};
			return dsDefConsultaMovimientosSPT;
		};
		       	
		//---------------------------------------------
		//GRILLA PUNTAS
 
		var dsMovimientosSPT = new bcs.DataSource(defDataSource());
		$scope.confMovimientoSPT = 
		{
    				dataSource		: dsMovimientosSPT,
    					columns		: [  {field: "IdOperacion", title: "UI_FOLIO_OPERACION", width: "20px", align: "left", sortable : false }
				       				    ,{field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "20px", align: "left", sortable : false }
				       				    ,{field: "IdTipoCanalIngreso", title: "UI_CANAL", width: "20px", align: "left", sortable : false }
				       				    ,{field: "IdTipoInstrumento", title: "UI_TIPO_INSTRUMENTO", width: "20px", align: "left", sortable : false }
				       				    ,{field: "RutCliente", title: "UI_RUT_CLIENTE", width: "20px", align: "left", sortable : false }
				       				    ,{field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "70px", align: "left", sortable : false }
				       				    ,{field: "CodigoPropositoPortafolio", title: "UI_PORTAFOLIO_CLIENTE", width: "70px", align: "left", sortable : false }
				       				    ,{field: "CodigoYNombrePlanillaBancoCentral", title: "UI_CODIGO_BANCO_CENTRAL", width: "100px", align: "left", sortable : false }
									  ],		
    					sortable	: true,
    					pageable	: true,
    					filterable	: true,
    					navigatable: true,
    					height		: 400,
    					//selectable 	: "row",
    					userSettings 	: true,
    					excel 			: { allPages: true },
    				    autoBind 	 : false,
    				    selectable 	: "multiple row",
    				    gridCreated: function(grid){
    				    	var filtroMovimientoSPT = new Array();
    			    		filtroMovimientoSPT.push({ field: "FechaOperacion", operator: "gte", value: util.dateInicioDia($scope.data.kngFecha) });
    			    		filtroMovimientoSPT.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia($scope.data.kngFecha) });
    			    		filtroMovimientoSPT.push({ field: "Estado", operator: "eq", value: 'VIG'});
    			    		dsMovimientosSPT.filters(filtroMovimientoSPT);
    			            $scope.confMovimientoSPT.dataSource = dsMovimientosSPT;
    				    }
       	};
		

		//--- FUNCION BUSCAR -----------------------
		$scope.data.buscarMovimientos = function()
		{
			//-- Filtros
			var filtroMovimientoSPT = new Array();

				//Parametro de filtro
    		filtroMovimientoSPT.push({ field: "FechaOperacion", operator: "gte", value: util.dateInicioDia($scope.data.kngFecha) });
    		filtroMovimientoSPT.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia($scope.data.kngFecha) });
    		filtroMovimientoSPT.push({ field: "Estado", operator: "eq", value: 'VIG'});
				
				//bcsServiciosMesa.ConsultaMovimientosVencidosFDW(parametros, successVencimiento,error);
			dsMovimientosSPT.filters(filtroMovimientoSPT);
			$scope.confMovimientoSPT.dataSource = dsMovimientosSPT;
			$scope.data.MovimientosSeleccionados = undefined;
		}
	});
}());