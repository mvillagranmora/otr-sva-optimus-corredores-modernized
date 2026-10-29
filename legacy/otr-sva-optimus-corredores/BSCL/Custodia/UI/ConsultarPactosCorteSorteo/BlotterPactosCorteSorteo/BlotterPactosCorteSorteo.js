angular.module('ModuloCustodia').controller('BlotterPactosCorteSorteoCtrl',
				function($scope,$state,$translate,$resource,$timeout,bcsComunes,
						bcsServicioPlataforma,bcsOperacionesComunes)
{	
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	
	$scope.data = {};
	$scope.filtro = {};
	
	$scope.filtro.fecInicio 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTermino 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	
	var filters = new Array();
	
	var dsGrillaPactosCorteSorteo = new bcs.DataSource({
	   type : "odata",
	   definition : {
		   serverFiltering 	: true, // Filtrado de datos en el servidor.
		   serverPaging     : true, // Paginado de los datos en el servidor.
		   serverSorting    : true, // Ordenamiento de los datos en el servidor.
		   sort 			: { field: "FolioCartera", dir: "desc" },
		   pageSize         : 25,
		   filter			: filters,
		   transport 		: {read : {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=622",
								dataType : "json"
								}
			},
		   schema 			: { model: {
											fields : {
												FolioCartera					: {type: "number"},
												Nemo							: {type: "string"},
												FechaOperacion					: {type: "sysdate"},
												IdClasificacionTipoInstrumento	: {type: "string"},
												IdTipoInstrumento				: {type: "string"},
												Nemotecnico						: {type: "string"},
												IdEmisor						: {type: "string"},
												TipoEventoCapital				: {type: "string"},
												Secuencia						: {type: "number"},
												IdPortafolio					: {type: "number"},											
												IdSocioNegocio					: {type: "number"},
												DocumentoIdentidad				: {type: "string"},
												NombreCliente					: {type: "string"},
												FechaPago						: {type: "sysdate"}
											}
										}
									},

	   }
	});
	 
	//GRILLA
	$scope.confBasicaPactosCorteSorteo = {
		dataSource			: dsGrillaPactosCorteSorteo,
		columns				: [ 
		       				   
								 {field: "FolioCartera", 					title: "UI_FOLIO", width: "70px",   }
	   						 	,{field: "Nemo", 							title: 'UI_TIPO'	, 				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
							   	,{field: "FechaOperacion", 					title: "UI_FECHA_OPERACION"			, width: "80px", align: "center", sortable : false, groupable: true}
								,{field: "IdClasificacionTipoInstrumento", 	title: 'UI_ID_CLASIFICACION_TIPO_INSTRUMENTO', 			width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
								,{field: "IdTipoInstrumento", 				title: 'UI_TIPO_INSTRUMENTO', 							width: "40px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
								,{field: "Nemotecnico", 					title: 'UI_NEMO_ABR', 				width: "80px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
								,{field: "IdEmisor", 						title: 'UI_EMISOR',	 				width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} 	, groupable: true}
								,{field: "TipoEventoCapital", 				title: 'UI_TIPO_EVENTO', 								width: "90px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
								,{field: "Secuencia", 						title: 'UI_SECUENCIA', 				width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
								,{field: "IdSocioNegocio", 					title: 'UI_SOCIO_NEGOCIO', 			width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
								,{field: "IdPortafolio", 					title: 'UI_PORTAFOLIO', 			width: "60px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
								,{field: "DocumentoIdentidad", 				title: 'UI_CLIENTE', 				width: "70px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
								,{field: "NombreCliente", 					title: 'UI_NOMBRE_CLIENTE', 		width: "230px", attributes:{style:"text-align:left;"}, headerAttributes:{style:"text-align: left;"} , groupable: true}
								,{field: "FechaPago", 						title: 'UI_FECHA_PAGO', 			template : " <label> #:  util.formatoDate(FechaPago) #</label>" ,			 	width: "80px", attributes:{style:"text-align:center; white-space:nowrap;"}, headerAttributes:{style:"text-align: left;"}  }

								
							  		 		
							  ],		
		sortable			: true,
		pageable			: true,
		autoBind			: $scope.parametros.IdSocioDeNegocio !== undefined ? true : false,
		filterable			: true,
		userSettings 		: true,
		height				: 520,
		selectable 			: "multiple row"
	};

	//BOTONES LIMPIAR Y BUSCAR
	$scope.limpiarFiltros = function()
	{							
		//seteoFechaSistema();
		$scope.filtro.buscarinstrumento = "-1";
		$scope.oConfiguracionSocioNegocio.idActual = false;
		dsGrillaPactosCorteSorteo.removeAll();
	};
 
	$scope.data.buscar = function()
	{
		var filters = new Array();
		
			//Fechas filtro

		
		
		if ($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente > 0 ){
				filters.push({ field: "IdCliente", operator: "eq", value: $scope.oConfiguracionSocioNegocio.seleccionado.IdCliente});
		};
			
		if ($scope.filtro.instrumentoObjeto !== undefined){
        	if ($scope.filtro.instrumentoObjeto.IdInstrumento > 0) {
        		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.filtro.instrumentoObjeto.IdInstrumento) });
        	}
        };			

        var auxDate1;
    	var auxDate2;

    	if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null){
    		auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
			filters.push({ field: "INGFechaOperacion", operator: "gte", value: auxDate1 });
    	}
    	if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null){
    		auxDate2 = util.dateOdata(util.date($scope.filtro.fecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
    	 	if (auxDate2 < auxDate1){
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    	 		return;
    	 	}
    		filters.push({ field: "INGFechaOperacion", operator: "lte", value: auxDate2 });
    	}
        		
		dsGrillaPactosCorteSorteo.filter(filters);	
		$scope.data.btnActivo  = true;
	};
	  

	//Primer evento - filtro inicial
	$timeout(function(){
		$scope.oConfiguracionSocioNegocio.filtros.read = true;
	},100);
	
});

