angular.module('ModuloOrdenes').controller('VerReportantesCtrl',function($scope,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,$timeout){
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData = {};
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	
	if(!$scope.parametros.reportado.IdOrden){
		bcsComunes.ventanaCerrar($scope.winID);
		return false;
	}
	
	
	var configuracionColumnas = function() {
		return { 
			
			NombreCliente	  			:{field: "NombreCompletoReportante",	title: $translate.instant('UI_CLIENTE'),		width: "100px",	align: "left"},
			DescripcionPortafolio	  	:{field: "CodigoPortafolioReportante",	title: $translate.instant('UI_PORTAFOLIO'),		width: "100px",	align: "left"},
			Tasa	  					:{field: "TasaReportante",				title: $translate.instant('UI_TASA'),			width: "100px",	align: "left"},
			MontoContado	  			:{field: "MontoOrdenReportante",		title: $translate.instant('UI_MONTO_CONTADO'),	width: "100px",	align: "left", template : " <label> #:  (util.bcs_formato(MontoOrdenReportante,'ORD','Monto',IdMonedaOrdenReportante, '','')).valor #</label>"},
			IdMoneda	  				:{field: "IdMonedaOrdenReportante",		title: $translate.instant('UI_MONEDA'),			width: "100px",	align: "left"},
			
		}     
	};
	
	var getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	var filtroReportante = [{field:"IdOrdenReportado", operator: "eq", value: $scope.parametros.reportado.IdOrden}]
	
	var ds = bcsConfiguracionOrdenesV2.getDataSourceAsociaciones();
	ds.definition.filter = filtroReportante
	
	var conf = {
			options : {
				userSettings:true,
				sortable: true,
	      		reorderable: true,
	      		filterable: true,
				groupable: false,
				pageable: true,
				selectable: "multiple, row",
				reorderable: true,
				allowCopy: true,
				resizable: true,
	      		autoBind: true,
	      		scrollable: true,
	      		height:300,
		
				dataSource: ds,
			
				columns: [
				           getConfiguracionColumna("NombreCliente")
				          ,getConfiguracionColumna("DescripcionPortafolio")
				          ,getConfiguracionColumna("Tasa")
				          ,getConfiguracionColumna("MontoContado")
				          ,getConfiguracionColumna("IdMoneda")
				]
			}
	};
	
	$scope.formData.verConfGridReportes = conf.options;
	
	$scope.formData.verConfGridReportes.gridCreated = function(grid){
		bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
	}
	
	
});