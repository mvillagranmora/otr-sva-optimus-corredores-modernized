angular.module('ModuloPlataforma').controller('BusquedaInstrumentosCtrl',
		function($scope,$resource,$rootScope,bcsComunes,$translate)
{

	$scope.data	= {};	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	var autoBindVal = true;
	$scope.grid = undefined;
	$scope.dataFlujo = undefined;
	$scope.advFlujo = false;
	if (parametros.dataSourceFlujoCustodiaOrden) {
		autoBindVal = false;
		$scope.advFlujo = true;
	}
	
	$scope.gridConfigBusquedaInstrumentos = {
			columns :
			[
			 	{field: "Nemotecnico",		title: 'UI_NEMOTECNICO',					width: "100px",	filterable: { cell: { operator: "contains"  } }},
			 	{field: "IdEmisor",			title: 'UI_EMISOR',							width: "100px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "RazonSocial",		title: 'UI_RAZON_SOCIAL',					width: "200px", filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="PE")?true:false)},
			 	{field: "ISIN",				title: 'UI_ISIN',							width: "150px",	filterable: { cell: { operator: "contains"  } }},
			 	
			 	{field: "PrecioMercado",	title: 'UI_PRECIO_MERCADO',					width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "SaldoT0",			title: 'UI_SALDO_T0',			 			width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "SaldoT1",			title: 'UI_SALDO_T1',			 			width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "SaldoT2",			title: 'UI_SALDO_T2',			 			width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "SaldoT3",			title: 'UI_SALDO_T3',			 			width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "SaldoT4",			title: 'UI_SALDO_T4',			 			width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	{field: "SaldoT5",			title: 'UI_SALDO_T5',			 			width: "100px",hidden: ((autoBindVal || $scope.SesionLocale==="CL")?true:false)},
			 	
			 	{field: "FechaVencimiento",	title: 'UI_FECHA_VENCIMIENTO',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "CorteMinimo",		title: 'UI_CORTE_MINIMO',					width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "IdTipoReajuste",	title: 'UI_TIPO_REAJUSTE',					width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "TasaEmision",		title: 'UI_TASA_EMISION',					width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "CallConfirmado",	title: 'UI_CALL_CONFIRMADO',				width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "ValorClasificacionRiesgo",	title: 'UI_CLASIFICACION_RIESGO',	width: "50px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "FrecuenciaCupon",	title: 'UI_PERIODO_PAGO_CUPONES',			width: "150px",	filterable: { cell: { operator: "contains"  } }, hidden: (($scope.SesionLocale==="CL")?true:false)},
			 	{field: "IdMoneda",			title: 'UI_MONEDA', 						width: "100px",hidden: (($scope.SesionLocale==="PE")?true:false)},	 	
			],
	  		selectable : "row",
	  		filterable : {
	  			mode : "row"
	  		},
	  		dataBound : function(e) {
	  			console.log('asdf');
	  			if (!$scope.grid) return;
	  			//console.log($scope.grid.dataSource.data());
	  			//console.log($scope.dataFlujo);


				$.each($scope.grid.dataSource.data(),function(key,valueInstrumento){
					$.each($scope.dataFlujo,function(key2,valueFlujo){

						if (valueInstrumento.IdInstrumento === valueFlujo.IdInstrumento) {
							valueInstrumento['PrecioMercado'] = (util.bcs_formato(valueFlujo.PrecioMercado,'GEN','Precio',valueFlujo.IdMonedaInstrumento,'','')).valor;
							valueInstrumento['SaldoT'+valueFlujo.Dias] = (util.bcs_formato(valueFlujo.Cantidad,'GEN','Monto',valueFlujo.IdMonedaInstrumento,'','')).valor;
						}
					});
				});


	  		},
	  		pageable : true,
	  		gridCreated : function(grid) {
	  			if (autoBindVal === false) {
	  				bcsComunes.ventanaOverlay(idVentana,true);
	  				parametros.dataSourceFlujoCustodiaOrden.fetch().then(function(){
	  					bcsComunes.ventanaOverlay(idVentana,false);
						var data = parametros.dataSourceFlujoCustodiaOrden.data();
						$scope.dataFlujo = parametros.dataSourceFlujoCustodiaOrden.data();
						var filtros = grid.dataSource.filter();
						var filtrosInstrumento = {};
						$scope.grid = grid;
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