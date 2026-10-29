angular.module('ModuloOrdenes').controller('BlotterCustodiaOperacionesReportesCtrl',function($scope,bcsComunes,$timeout,$compile){
	$scope.configTab = {};
	var tabReportesHistoricosLoaded = false;
	var tabAnticiposLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domReportesHistoricos =  '<div ng-include="\'../../Orden/UI/REP/Custodia/ReportesHistoricos/ReportesHistoricos.html\'" ></div>';
	var domAnticipos =  '<div ng-include="\'../../Orden/UI/REP/Custodia/ConsultaAnticiposReportes/ConsultaAnticiposReportes.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabReportesHistoricosLoaded && grid === 'gridReportesHistoricos') {
			domVentana.find('#'+grid).append($compile(domReportesHistoricos)($scope));
			tabReportesHistoricosLoaded = true;
		}
		else if (!tabAnticiposLoaded && grid === 'gridAnticipos') {
			domVentana.find('#'+grid).append($compile(domAnticipos)($scope));
			tabAnticiposLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	$scope.configTab.consultarOrdenes = true;
});

