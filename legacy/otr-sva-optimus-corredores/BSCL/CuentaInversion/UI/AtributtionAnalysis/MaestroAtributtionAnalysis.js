angular.module('ModuloCuentaInversion').controller('MaestroAtributtionAnalisysCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabMantenedorLoaded = false;
	var tabIndicesLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domIndices    =  '<div ng-include="\'../../CuentaInversion/UI/AtributtionAnalysis/Indices.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabIndicesLoaded && grid === 'gridIndices') {
			domVentana.find('#'+grid).append($compile(domIndices)($scope));
			tabIndicesLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
		
});