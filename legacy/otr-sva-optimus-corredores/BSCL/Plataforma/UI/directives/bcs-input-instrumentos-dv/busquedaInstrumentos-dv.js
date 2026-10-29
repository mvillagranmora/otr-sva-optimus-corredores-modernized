angular.module('ModuloPlataforma').controller('BusquedaInstrumentosCtrl',
		function($scope,$resource,$rootScope,bcsComunes,$translate)
{

	$scope.data	= {};	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	var autoBindVal = true;
	$scope.advFlujo = false;
	if (parametros.dataSourceFlujoCustodiaOrden) {
		autoBindVal = false;
		$scope.advFlujo = true;
	}
	
	$scope.gridConfigBusquedaInstrumentos = {
			columns :
			[
			 	{field: "Nemotecnico",		title: 'UI_NEMOTECNICO',		width: "90px",	filterable: { cell: { operator: "contains"  } }},
			 	/*OPCIONES*/			 	
			 	{field: "TipoOpcion",		title: 'UI_TIPO_OPCION',	width: "60px", hidden: (parametros.filtroGrupo === 'FU')?true:false},			 	
			 	{field: "IdModoOpcion",		title: 'UI_MODO_OPCION',	width: "100px", hidden:true},
			 	{field: "ModoOpcion",		title: 'UI_MODO_OPCION',	width: "100px", hidden: (parametros.filtroGrupo === 'FU')?true:false},
			 	{field: "IdEstiloOpcion",	title: 'UI_ESTILO_OPCION',	width: "100px", hidden:true},			 	
			 	{field: "EstiloOpcion",	title: 'UI_ESTILO_OPCION',	width: "100px", hidden: (parametros.filtroGrupo === 'FU')?true:false},			 	
			 	{field: "IdTipoOpcion",		title: 'UI_TIPO_OPCION',	width: "100px", hidden:true},			 	
			 	{field: "IdModalidadEjercicio",		title: 'UI_MODALIDAD_EJERCICIO_OPCION',	width: "50px", hidden:true},
			 	{field: "ModalidadEjercicio",		title: 'UI_MODALIDAD_EJERCICIO_OPCION',	width: "90px" , hidden: (parametros.filtroGrupo === 'FU')?true:false},			 	
			 	{field: "FechaVencimiento",		title: 'UI_FECHA_VENCIMIENTO',	width: "100px" },
			 	//
			 	{field: "IdEmisor",			title: 'UI_EMISOR',		width: "100px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "RazonSocial",		title: 'UI_RAZON_SOCIAL',		width: "200px", filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="PE")?true:false)},
			 	{field: "ISIN",				title: 'UI_ISIN',	hidden:true,			width: "150px",	filterable: { cell: { operator: "contains"  } }},
			 	{field: "FechaVencimiento",	title: 'UI_FECHA_VENCIMIENTO',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "CorteMinimo",		title: 'UI_CORTE_MINIMO',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "IdTipoReajuste",	title: 'UI_TIPO_REAJUSTE',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "Tasa",				title: 'UI_TASA_EMISION',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "FrecuenciaCupon",title: 'UI_PERIODO_PAGO_CUPONES',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "IdMoneda",			title: 'UI_MONEDA', hidden: (($scope.SesionLocale==="PE")?true:false)},
			 	/*CUSTODIA*/
			 	{field: "Cantidad",			title: 'UI_CANTIDAD_DISPONIBLE', hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "Monto",			title: 'UI_VALOR_INVERSION', hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "Precio",			title: 'UI_PRECIO_INVERSION', hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			],
	  		selectable : "row",
	  		filterable : {
	  			mode : "row"
	  		},
	  		pageable : true,
	  		gridCreated : function(grid) {
	  			if (autoBindVal === false) {
	  				bcsComunes.ventanaOverlay(idVentana,true);
	  				parametros.dataSourceFlujoCustodiaOrden.fetch().then(function(){
	  					bcsComunes.ventanaOverlay(idVentana,false);
						var data = parametros.dataSourceFlujoCustodiaOrden.data();
						var filtros = grid.dataSource.filter();
						var filtrosInstrumento = {};
						filtrosInstrumento.logic = "or";
						filtrosInstrumento.filters = [];
						$.each(data,function(key,value){
							filtrosInstrumento.filters.push({field: "IdInstrumento", operator: "eq", value: value.IdInstrumento});
						})
						filtros.filters.push(filtrosInstrumento);
						grid.dataSource.filter(filtros);
						console.log("go");
	  				},function(){
	  					bcsComunes.ventanaOverlay(idVentana,false);
	  				})
	  			}
	  			
	  		},
	  		dataSource: parametros.dataSourceInstrumentos,
			autoBind:autoBindVal,
			height:250,
		};

		

	$scope.seleccionarInstrumento = function(){
		bcsComunes.ventanaData(idVentana,$scope.seleccionado);
		bcsComunes.ventanaCerrar(idVentana);
	}	
	
});	