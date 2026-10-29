angular.module('ModuloPlataforma').controller('MaestroParametroFinancieroCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabTiposCambioLoaded = false;
	var tabIndicesLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domTiposCambio =  '<div ng-include="\'../../Plataforma/UI/MER/MantenedorParametroFinanciero/TipoCambio.html\'" ></div>';
	var domIndices =  '<div ng-include="\'../../Plataforma/UI/MER/MantenedorParametroFinanciero/Indices.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabTiposCambioLoaded && grid === 'gridTiposCambio') {
			domVentana.find('#'+grid).append($compile(domTiposCambio)($scope));
			tabTiposCambioLoaded = true;
		}
		else if (!tabIndicesLoaded && grid === 'gridIndices') {
			domVentana.find('#'+grid).append($compile(domIndices)($scope));
			tabIndicesLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
		
});