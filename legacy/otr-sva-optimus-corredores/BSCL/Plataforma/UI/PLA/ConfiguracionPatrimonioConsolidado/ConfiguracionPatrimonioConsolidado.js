angular.module('ModuloPlataforma').controller('ConfiguracionPatrimonioConsolidadoCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabCustodiaLoaded = false;
	var tabCajaLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domCustodia =  '<div ng-include="\'../../Plataforma/UI/PLA/ConfiguracionPatrimonioConsolidado/ConfiguracionPatrimonioCustodia.html\'" ></div>';
	var domCaja =  '<div ng-include="\'../../Plataforma/UI/PLA/ConfiguracionPatrimonioConsolidado/ConfiguracionPatrimonioCaja.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabCustodiaLoaded && grid === 'gridCustodia') {
			domVentana.find('#'+grid).append($compile(domCustodia)($scope));
			tabCustodiaLoaded = true;
		}
		else if (!tabCajaLoaded && grid === 'gridCaja') {
			domVentana.find('#'+grid).append($compile(domCaja)($scope));
			tabCajaLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
		
});