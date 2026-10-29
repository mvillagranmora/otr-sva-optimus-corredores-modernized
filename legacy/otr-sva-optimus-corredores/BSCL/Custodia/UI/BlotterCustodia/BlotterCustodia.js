angular.module('ModuloCustodia').controller('BlotterCustodiaCtrl',function($scope,bcsComunes,$timeout,$compile){
	$scope.bl = {};
	$scope.bl.tabCustodiaPortafolioLoaded = false;
	$scope.bl.tabMovimientosLoaded = false;
	$scope.bl.tabCustodiaGeneralLoaded = false;
	$scope.bl.tabCustodiaPendientesLoaded = false;
	$scope.bl.idVentana = bcsComunes.ventanaWinId($scope);
	$scope.bl.domVentana = $("[ventanaid='"+$scope.bl.idVentana+"']");
	
	$scope.bl.domCustodiaPortafolio =  '<div ng-include="\'../../Custodia/UI/ConsultarCustodiaPortafolio/ConsultarCustodiaPortafolio.html\'"></div>';
	$scope.bl.domMovimientos =  '<div ng-include="\'../../Custodia/UI/ConsultarCustodiaMovimientos/ConsultarCustodiaMovimientos.html\'"></div>';
	$scope.bl.domCustodiaGeneral =  '<div ng-include="\'../../Custodia/UI/ConsultarCustodiaGeneral/ConsultarCustodiaGeneral.html\'"></div>';
	$scope.bl.domCustodiaPendientes =  '<div ng-include="\'../../Custodia/UI/ConsultarCustodiaInstrPendientes/ConsultarCustodiaInstrPendientes.html\'"></div>';
	
	$scope.select = function(grid)
	{
		
		if (!$scope.bl.tabCustodiaPortafolioLoaded && grid === 'gridCustodiaPortafolio') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCustodiaPortafolio)($scope));
			$scope.bl.tabCustodiaPortafolioLoaded = true;
		}
		else if (!$scope.bl.tabMovimientosLoaded && grid === 'gridMovimientos') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domMovimientos)($scope));
			$scope.bl.tabMovimientosLoaded  = true;
		}
		else if (!$scope.bl.tabCustodiaGeneralLoaded && grid === 'gridCustodiaGeneral') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCustodiaGeneral)($scope));
			$scope.bl.tabCustodiaGeneralLoaded  = true;
		}
		else if (!$scope.bl.tabCustodiaPendientesLoaded && grid === 'gridCustodiaPendientes') {
			$scope.bl.domVentana.find('#'+grid).append($compile($scope.bl.domCustodiaPendientes)($scope));
			$scope.bl.tabCustodiaPendientesLoaded  = true;
		}
		
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	
	$scope.socioGral = {
			idActual : -1,
			idDefecto : -1
	};
	
	//Permite traspasar datos entre los controladores hijos(tabs)
	
	var blotterPadre = this;
	blotterPadre.data = {};
	
	
});