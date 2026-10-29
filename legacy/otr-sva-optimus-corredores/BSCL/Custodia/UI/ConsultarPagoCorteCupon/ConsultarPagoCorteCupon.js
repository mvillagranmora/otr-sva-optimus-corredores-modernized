angular.module('ModuloCustodia').controller('ConsultarPagoCorteCuponCtrl', 
		function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosCustodia,OPCIONES_BCSALERTA,bcsAlerta)				
{	
	
	//*** configuracion de objetos ***//	
	$scope.filtros = {};
	$scope.filtro = {};
	$scope.data ={};	
		
	var parametrosConsulta = {};	
	var conParametros = false;
		
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	
	$scope.oClientePortafolio={
	tipo : "combobox-cliente-portafolio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
 var dsPagoCorteCupon = new bcs.DataSource({
	               type : "odata",
	               definition : {
	            	   				serverFiltering 	: true, // Filtrado de datos en el servidor.
	                               	serverPaging     : true, // Paginado de los datos en el servidor.
	                               	serverSorting    : true, // Ordenamiento de los datos en el servidor.
	                               	sort 			: { field: "FolioCartera", dir: "desc" },
	                               	pageSize         : 100,
	                               	transport 		: {
	                            	   					read : {
															url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=673",
															dataType : "json"
														}
	                               					  },	                               
	                               	schema : {
	                                               model : {
	                                                              fields : {                                                                                                                                                             
																			FolioCartera					: { type: "number"},
																			FechaOperacion                  : { type: "date"},
																			FechaVencimientoOperacion       : { type: "date"},
																			IdPortafolio                    : { type: "number"},
																			DocumentoIdentidad              : { type: "string"},
																			RazonSocial                     : { type: "string"},
																			IdClasificacionTipoInstrumento  : { type: "string"},
																			IdTipoInstrumento               : { type: "string"},
																			RazonSocialInstrumento          : { type: "string"},
																			Nemotecnico                     : { type: "string"},
																			IdEmisor                        : { type: "string"},
																			MontoCompra                     : { type: "number"},
																			INGFechaPagoCorteCupon          : { type: "date"},
																			INGFechaCorteCupon              : { type: "date"},
																			INGNumeroCupon                  : { type: "number"},
																			INGIdMonedaPagoCupon            : { type: "string"},
																			INGMontoCorteCupon              : { type: "number"},
																			MontoAmortizacion               : { type: "number"},
																			MontoIntereses             		: { type: "number"},
																			IdTipoReajuste                  : { type: "string"},
																			IdBoveda                        : { type: "string"},
																			IdTipoOperacion                 : { type: "string"},
																			IdCondicionLiquidacion          : { type: "string"},
																			FechaLiquidacion                : { type: "date"},
																			IdMonedaSistema                 : { type: "string"},
																			IdEstado	                    : { type: "string"},
																			
	                                                              }
	                                               }
	                               }
	               }
	});
 
 	$scope.columnasPagoCorteCupon = function() {
		return  [
			 {field: "FolioCartera"						, title: 'UI_FOLIO_CARTERA', 							format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "INGFechaPagoCorteCupon"           , title: 'UI_FECHA_PAGO_CUPON', 						width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }//template : " <label> #:  util.formatoDate(INGFechaPagoCorteCupon) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "INGFechaCorteCupon"               , title: 'UI_FECHA_CORTE_CUPON', 						width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }//template : " <label> #:  util.formatoDate(INGFechaCorteCupon) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "FechaOperacion"                   , title: 'UI_FECHA_OPERACION',							width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }//template : " <label> #:  util.formatoDate(FechaOperacion) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "FechaVencimientoOperacion"        , title: 'UI_FECHA_VENCIMIENTO', 						width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }//template : " <label> #:  util.formatoDate(FechaVencimientoOperacion) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "IdPortafolio"                     , title: 'UI_PORTAFOLIO', 								width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "DocumentoIdentidad"               , title: 'UI_CLIENTE', 									width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "RazonSocial"                      , title: 'UI_NOMBRE_CLIENTE', 							width: "230px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdClasificacionTipoInstrumento"   , title: 'UI_CODIGO_CLASIFICACION_TIPO_INSTRUMENTO', 	width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoInstrumento"                , title: 'UI_TIPO_INSTRUMENTO', 					width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "Nemotecnico"                      , title: 'UI_NEMO_ABR', 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdEmisor"                         , title: 'UI_EMISOR',	 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
			,{field: "MontoCompra"                      , title: 'UI_MONTO_COMPRA', 							format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "INGNumeroCupon"                   , title: 'UI_NUMERO_CORTE_CUPON', 						format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "INGIdMonedaPagoCupon"             , title: 'UI_MONEDA_CORTE_CUPON',						width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "INGMontoCorteCupon"               , title: 'UI_MONTO_CORTE_CUPON',						format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "MontoAmortizacion"               	, title: 'UI_MONTO_AMORTIZACION',						format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "MontoIntereses"               	, title: 'UI_MONTO_INTERESES',						format: "{0:n0}",		width: "100px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoReajuste"                   , title: 'UI_TIPO_REAJUSTE',							width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdBoveda"                         , title: 'UI_BOVEDA', 									width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdTipoOperacion"                  , title: 'UI_TIPO_OPERACION', 							width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdCondicionLiquidacion"           , title: 'UI_CONDICION_LIQUIDACION_ABR',				width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "FechaLiquidacion"                 , title: 'UI_FECHA_LIQUIDACION', 						width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }//template : " <label> #:  util.formatoDate(FechaLiquidacion) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }
			,{field: "IdMonedaSistema"                  , title: 'UI_MONEDA', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			,{field: "IdEstado"                         , title: 'UI_ESTADO', 									width: "50px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
			
		];
	};
	
	
	$scope.confGrillaPagoCorteCupon = {
		dataSource	: dsPagoCorteCupon,
		columns		: $scope.columnasPagoCorteCupon(),		
		sortable	: true,
		pageable	: { pageSize : 25 },   //pagina localmente
		filterable 	: true,
		selectable 	: "row",
		reorderable	: false,
		userSettings : true,
		groupable	: true,
		height		: 526,
		allowCopy	: true,
		resizable	: true,
	    autoBind	: false,
	    gridCreated : function(grid) {
	    	grillaPagoCorteCupon = grid;
	    },
	    scrollable	: true
	};
	
   	//********* fin configuracion de objetos *********//
	
/*************************************************************************
* BUSCAR
**************************************************************************/


	$scope.buscarPagoCorteCupon = function(){
		var filters = new Array();		
/*
		if ($scope.oClientePortafolio.seleccionado.IdCliente > 0){
	    	filters.push({ field: "IdCliente", operator: "eq", value: $scope.oClientePortafolio.seleccionado.IdCliente});
	    };
	    */
		if ($scope.oClientePortafolio.seleccionado.IdPortafolio > 0){
			filters.push({ field: "IdPortafolio", operator: "eq", value: $scope.oClientePortafolio.seleccionado.IdPortafolio});
		};

		var auxDate1;
    	var auxDate2;
    	if ($scope.filtros.fecInicio !== undefined && $scope.filtros.fecInicio !== "" && $scope.filtros.fecInicio !== null){
    		auxDate1 =  util.dateOdata(util.date($scope.filtros.fecInicio).hours(0).minutes(0).seconds(0).toDate());
			filters.push({ field: "INGFechaPagoCorteCupon", operator: "gte", value: auxDate1 });
    	}
    	
    	if ($scope.filtros.fecTermino !== undefined && $scope.filtros.fecTermino !== "" && $scope.filtros.fecTermino !== null){
    		auxDate2 = util.dateOdata(util.date($scope.filtros.fecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
    	 	if (auxDate2 < auxDate1){
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    	 		return;
    	 	}
    	 	
    		filters.push({ field: "INGFechaPagoCorteCupon", operator: "lte", value: auxDate2 });
    	}

    	if (!!$scope.filtro.instrumentoObjeto){
        		if ($scope.filtro.instrumentoObjeto.IdInstrumento > 0) {
        			filters.push({ field: "Nemotecnico", operator: "eq", value: $scope.filtro.instrumentoObjeto.Nemotecnico });
        		}
        }
	    
	    dsPagoCorteCupon.filter(filters);
	};

	

/*************************************************************************
* LIMPIAR
***************************************************************************/

	$scope.limpiarFiltros = function() {
		
		$scope.instrumento 								= "";
		$scope.filtro.instrumentoObjeto 				= undefined;
		$scope.filtro.buscaroInstrumento 				= "-1";
		$scope.filtros.IdPortafolio 					= false;
		$scope.filtros.fecInicio		 = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fecTermino 		 = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fecTermino		 = util.date($scope.filtros.fecTermino).add(5,'days').toDate();

	};
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
    
	$scope.filtros.fecInicio		 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.fecTermino 		 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.fecTermino		 = util.date($scope.filtros.fecTermino).add(5,'days').toDate();
	
		

});


