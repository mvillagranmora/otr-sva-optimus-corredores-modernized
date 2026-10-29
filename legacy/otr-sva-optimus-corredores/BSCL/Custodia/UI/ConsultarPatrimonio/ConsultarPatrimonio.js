angular.module('ModuloCustodia').controller('ConsultarPatrimonioCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabConsolidadoLoaded = false;
	var tabCustodiaLoaded = false;
	var tabCajaLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domConsolidado = '<div ng-include="\'../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonioConsolidado.html\'" ></div>';
	var domCustodia =  '<div ng-include="\'../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonioCustodia.html\'" ></div>';
	var domCaja =  '<div ng-include="\'../../Custodia/UI/ConsultarPatrimonio/ConsultarPatrimonioCaja.html\'" ></div>';
	
	$scope.select = function(grid)
	{

		if (!tabConsolidadoLoaded && grid === 'gridConsolidado') {
			domVentana.find('#'+grid).append($compile(domConsolidado)($scope));
			tabConsolidadoLoaded = true;
		}
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