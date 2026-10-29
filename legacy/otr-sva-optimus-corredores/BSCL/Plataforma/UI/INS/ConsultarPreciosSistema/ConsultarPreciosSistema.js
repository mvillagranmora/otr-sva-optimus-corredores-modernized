angular.module('ModuloPlataforma').controller('ConsultarPreciosSistemaCtrl',function($scope,bcsComunes){
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	
	$scope.fechaVector = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");

	$scope.buscar = function() {
		var filters = new Array();
		filters.push({ field: "IdEstado", operator: "eq", value: 'ACT' });
		if ($scope.instrumentoDatos !== undefined){    		
	    	if ($scope.instrumentoDatos.IdInstrumento != "" && $scope.instrumentoDatos.IdInstrumento !== undefined)	    		
	    		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.instrumentoDatos.IdInstrumento) });
    	}
		if ($scope.fechaVector) {
			   filters.push({ field: "FechaVector", operator: "gte", value: util.date($scope.fechaVector).hours(0).minutes(0).seconds(0).toDate()});
			   filters.push({ field: "FechaVector", operator: "lte", value: util.date($scope.fechaVector).hours(23).minutes(59).seconds(59).toDate()});
			}
			
		
		$scope.configuracion.dataSource.filter(filters);
	};
	$scope.ConfiguracionInstrumento={
			buscar          : "-1"    
	};

	
	$scope.limpiar = function (){
		$scope.ConfiguracionInstrumento.buscar = "-1"
		$scope.fechaVector = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
	};
	
		
	
	$scope.fecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	
	$scope.configuracion = {
			allowCopy: true,
			userSettings		: true,
			navigatable			: true,
			gridCreated : function(grid){
				$scope.apiGrid =  grid;
			},
	      	filterable: true,
	      	height:500,
	      	resizable: true,
			scrollable: true,
	      	sortable: true,
	      	pageable: true,
			
			columns : [
				{field: "IdInstrumento",				  title : "UI_INSTRUMENTO",			width: "100px"},
				{field: "Nemotecnico",					  title : "UI_NEMOTECNICO",			width: "75px"},
			    {field: "FechaVector",					  title : "UI_FECHA_VECTOR",		width: "100px"},
			    {field: "IdMoneda",						  title : "UI_MONEDA",				width: "100px"},//
			    //{field: "ValorPrecioBase",	  			  title : "UI_PRECIO_BASE",			width: "100px", template : " <label> #:  (util.bcs_formato(ValorPrecioBase,'GEN','Precio',IdMoneda || 'CLP','','')).valor #</label>"},
			    {field: "ValorPrecioAlgorithmics100",	  title : "UI_PRECIO_A100",			width: "100px", template : " <label> #:  (util.bcs_formato(ValorPrecioAlgorithmics100,'GEN','Precio',IdMoneda || 'CLP','','')).valor #</label>"},
			    {field: "ValorPrecioAlgorithmics98",	  title : "UI_PRECIO_A98",			width: "100px", template : " <label> #:  (util.bcs_formato(ValorPrecioAlgorithmics98,'GEN','Precio',IdMoneda || 'CLP','','')).valor #</label>"},
			    {field: "ValorPrecioAlgorithmics95",	  title : "UI_PRECIO_A95",			width: "100px", template : " <label> #:  (util.bcs_formato(ValorPrecioAlgorithmics95,'GEN','Precio',IdMoneda || 'CLP','','')).valor #</label>"},
			    {field: "ValorPrecioOperacionesPlazo01",  title : "UI_PRECIO_PLAZO",		width: "100px", template : " <label> #:  (util.bcs_formato(ValorPrecioOperacionesPlazo01,'GEN','Precio',IdMoneda || 'CLP','','')).valor #</label>"},
			    {field: "ValorPrecioVentaCorta02",		  title : "UI_PRECIO_VENTA_CORTA",	width: "100px", template : " <label> #:  (util.bcs_formato(ValorPrecioVentaCorta02,'GEN','Precio',IdMoneda || 'CLP','','')).valor #</label>"},
			    {field: "IdEstado",						  title : "UI_ESTADO",				width: "75px"},
			           
			           ],
			dataSource : new bcs.DataSource({
	      		type: "odata",
	      		definition : {
	      			serverFiltering: true,
					serverSorting: false,
					serverPagina: true,
					pageSize: 50,
					schema: {
						model : {
							fields : {
								IdInstrumento 					    : { type: "string"},
								Nemotecnico  		 				: { type: "string"},
								FechaVector 			 			: { type: "sysdate"},
								IdMoneda  							: { type: "string"},
								//ValorPrecioBase 					: { type: "number"},
								ValorPrecioAlgorithmics100 			: { type: "number"},
								ValorPrecioAlgorithmics98 			: { type: "number"},
								ValorPrecioAlgorithmics95 			: { type: "number"},
								ValorPrecioOperacionesPlazo01   	: { type: "number"},
								ValorPrecioVentaCorta02   			: { type: "number"},
								ValorOtrosPrecios03  				: { type: "number"},
								ValorOtrosPrecios04  				: { type: "number"},
								ValorOtrosPrecios05  				: { type: "number"},
								IdEstado 							: { type: "string"}
								
				           	}
						}
					},
					filter : [{ field: "IdEstado", operator: "eq", value: 'ACT' }],
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=20100",
							dataType: "json"
						}
					}
	      			
	      		}
	      	})

	};	
});
