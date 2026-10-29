angular.module('ModuloMesa').controller('ParidadesYTipoCambioCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabIngresoValoresLoaded = false;
	var tabValoresPorDefectoLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domIngresoValores =  '<div ng-include="\'../../Mesa/UI/SPT/ParidadesYTipoCambio/IngresoValores/IngresoValores.html\'" ></div>';
	var domValoresPorDefecto =  '<div ng-include="\'../../Mesa/UI/SPT/ParidadesYTipoCambio/ValoresPorDefecto/ValoresPorDefecto.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabIngresoValoresLoaded && grid === 'gridIngresoValores') {
			domVentana.find('#'+grid).append($compile(domIngresoValores)($scope));
			tabIngresoValoresLoaded = true;
		}
		else if (!tabValoresPorDefectoLoaded && grid === 'gridValoresPorDefecto') {
			domVentana.find('#'+grid).append($compile(domValoresPorDefecto)($scope));
			tabValoresPorDefectoLoaded  = true;
		}		

		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
});

