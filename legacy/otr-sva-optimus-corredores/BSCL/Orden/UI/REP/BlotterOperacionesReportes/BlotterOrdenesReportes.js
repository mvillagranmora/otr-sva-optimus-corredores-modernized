angular.module('ModuloOrdenes').controller('BlotterOrdenesReporteCtrl',function($scope,bcsComunes,$timeout,$compile){
	$scope.configTab = {};
	var tabTransaccionesLoaded = false;
	var tabAsignacionesLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domTransacciones =  '<div ng-include="\'../../Orden/UI/REP/ConsultarTransaccionesReporte/ConsultarTransaccionesReporte.html\'" ></div>';
	var domAsignaciones =  '<div ng-include="\'../../Orden/UI/REP/ConsultarAsignacionesReporte/ConsultarAsignacionesReporte.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabTransaccionesLoaded && grid === 'gridTransacciones') {
			domVentana.find('#'+grid).append($compile(domTransacciones)($scope));
			tabTransaccionesLoaded = true;
		}
		else if (!tabAsignacionesLoaded && grid === 'gridAsignaciones') {
			domVentana.find('#'+grid).append($compile(domAsignaciones)($scope));
			tabAsignacionesLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	$scope.configTab.consultarOrdenes = true;
	
});