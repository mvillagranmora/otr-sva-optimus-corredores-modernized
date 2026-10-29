angular.module('ModuloOrdenes').controller('BuscarReportantesCtrl',function($scope,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,$timeout){
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	if(!$scope.parametros.reportado.IdTipoOperacion){
		bcsComunes.ventanaCerrar($scope.winID);
		return false;
	}
	
	var operacion = $scope.parametros.reportado.IdTipoOperacion === 'VENRRF' ? 'COMRRF' : 'COMRRV';
	
	console.log("Reportado:"+JSON.stringify($scope.parametros.reportado));
	
	var filtrosReportantes = [{field:"IdTipoOperacion", operator: "eq", value: operacion},
	                          {field:"IdEstadoOrden", operator: "eq", value: "ING"},
	                          {field:"OrdenVigente", operator: "eq", value:"S"},
	                          {field:"IdMonedaOrden", operator:"eq", value:$scope.parametros.reportado.IdMonedaOrden},
	                          {field:"MontoDisponibleAsociar",operator:"gt",value:0},
	                          {field:"IdCliente",operator:"neq",value:$scope.parametros.reportado.IdCliente},
	                          {field:"Plazo", operator:"gte", value:$scope.parametros.reportado.Plazo}
	                          //{field:"Tasa",operator:"lte",value:$scope.parametros.reportado.TasaReportanteReferencia}
	                         ];
	
	/*if($scope.parametros.reportado.TasaReportante >= 0){
		filtrosReportantes.push({field:"Tasa",operator:"eq",value:$scope.parametros.reportado.TasaReportante})
	}else{
		filtrosReportantes.push({field:"Tasa",operator:"gte",value:$scope.parametros.reportado.Tasa})
	}
	*/
	
	var configuracionColumnas = function() {
		return { 
			
			NombreCliente	  			:{field: "NombreCompleto",				title: $translate.instant('UI_CLIENTE'),		width: "100px",	align: "left"},
			DescripcionPortafolio	  	:{field: "CodigoPortafolio",			title: $translate.instant('UI_PORTAFOLIO'),		width: "100px",	align: "left"},
			MontoOrden					:{field: "MontoOrden",					title: $translate.instant('UI_MONTO'),			width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(MontoOrden,'ORD','Monto',IdMonedaOrden, IdTipoInstrumento,'')).valor #</label>"},
			MontoDisponibleAsociar	  	:{field: "MontoDisponibleAsociar",		title: $translate.instant('UI_SALDO'),			width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(MontoDisponibleAsociar,'ORD','Monto',IdMonedaOrden, IdTipoInstrumento,'')).valor #</label>"},
			Moneda	  					:{field: "IdMonedaOrden",				title: $translate.instant('UI_MONEDA'),			width: "100px",	align: "left"},
			Tasa	  					:{field: "Tasa",						title: $translate.instant('UI_TASA'),			width: "100px",	align: "left"},
		}     
	};
	
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	var ds = bcsConfiguracionOrdenesV2.getDataSourceOrdenesReporte();
	
	ds.definition.filter = filtrosReportantes;
	
	var conf = {
			options : {
				userSettings:true,
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: false,
				pageable: true,
				selectable: "row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:550,
		
				dataSource: ds,
			
				columns: [
				           getConfiguracionColumna("NombreCliente")
				          ,getConfiguracionColumna("DescripcionPortafolio")
				          ,getConfiguracionColumna("MontoOrden")
				          ,getConfiguracionColumna("MontoDisponibleAsociar")
				          ,getConfiguracionColumna("Moneda")
				          ,getConfiguracionColumna("Tasa")
				]
			}
	};
	
	$scope.formData.confGridReportes = conf.options;
	
	$scope.formData.confGridReportes.gridCreated = function(grid){
		bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
	}
	
	$scope.asociarReportante = function(reportante){
		if(!reportante) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var id = bcsComunes.ventanaAbrir("AsociarReportantes",{reportado: $scope.parametros.reportado, reportante: reportante},false);
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	//bcsComunes.ventanaCerrar($scope.winID);
        	
        	$scope.formData.confGridReportes.dataSource.read();
        	bcsComunes.ventanaOverlay($scope.winID,false);
        });
	}
		
});