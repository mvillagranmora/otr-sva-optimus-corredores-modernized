angular.module('ModuloCustodia').controller('BlotterCarterasCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	$scope.bl = {};
	$scope.bl.tabCarterasLoaded = false;
	$scope.bl.tabCarterasInstrumentoLoaded = false;
	$scope.bl.tabCartolaMovimientoslLoaded = false;
	$scope.bl.tabCustodiaVencimientosLoaded = false;
	$scope.bl.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.bl.domVentana = $("[ventanaid='"+$scope.bl.idVentana+"']");
	
	$scope.bl.domCarteras =  '<div ng-include="\'../../Custodia/UI/ConsultarCarteras/ConsultarCarteras.html\'"></div>';
	$scope.bl.domCarterasInstrumento =  '<div ng-include="\'../../Custodia/UI/ConsultarCarterasPorInstrumento/ConsultarCarterasPorInstrumento.html\'"></div>';
	$scope.bl.domCartolaMovimientos =  '<div ng-include="\'../../Custodia/UI/ConsultarCartolaMovimientos/ConsultarCartolaMovimientos.html\'"></div>';
	$scope.bl.domCustodiaVencimientos =  '<div ng-include="\'../../Custodia/UI/ConsultarCustodiaVencimientos/ConsultarCustodiaVencimientos.html\'"></div>';
	
	$scope.select = function(grid)
	{
		
		if (!$scope.bl.tabCarterasLoaded && grid === 'gridCarteras') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCarteras)($scope));
			$scope.bl.tabCarterasLoaded = true;
		}
		else if (!$scope.bl.tabCarterasInstrumentoLoaded && grid === 'gridCarterasInstrumento') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCarterasInstrumento)($scope));
			$scope.bl.tabCarterasInstrumentoLoaded  = true;
		}
		else if (!$scope.bl.tabCartolaMovimientoslLoaded && grid === 'gridCartolaMovimientos') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCartolaMovimientos)($scope));
			$scope.bl.tabCartolaMovimientoslLoaded  = true;
		}
		else if (!$scope.bl.tabCustodiaVencimientosLoaded && grid === 'gridCustodiaVencimientos') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCustodiaVencimientos)($scope));
			$scope.bl.tabCustodiaVencimientosLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	$scope.socioGral = {
		idActual: -1,
		idDefecto: -1
	};
	
	
});