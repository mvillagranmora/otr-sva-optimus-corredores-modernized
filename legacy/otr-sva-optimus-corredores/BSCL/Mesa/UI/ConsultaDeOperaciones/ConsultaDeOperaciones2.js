angular.module('ModuloMesa').controller('ConsultaOperacionesCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabMovimientosLoaded = false;
	var tabCarteraLoaded = false;
	var tabVencimientoLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
    var params = bcsComunes.ventanaParams(idVentana);
    var tipoFlujo = params.tipoFlujo;
    var sufijo = (tipoFlujo === 'Spot') ? 'Spot' : '';
    var pathMovimientos = '../../Mesa/UI/ConsultaDeOperaciones/ConsultarMovimientos/ConsultarMovimientos' + sufijo + '.html';
    var pathCartera = '../../Mesa/UI/ConsultaDeOperaciones/ConsultarCartera/ConsultarCartera' + sufijo + '.html';
    
    var domMovimientos = '<div ng-include="\' ' + pathMovimientos + ' \'"></div>';
	var domCartera =  '<div ng-include="\' ' + pathCartera + ' \'"></div>';
	var domVencimiento =  '<div ng-include="\'../../Mesa/UI/ConsultaDeOperaciones/ConsultarVencimiento/ConsultarVencimiento.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabMovimientosLoaded && grid === 'gridMovimientos') {
			domVentana.find('#'+grid).append($compile(domMovimientos)($scope));
			tabMovimientosLoaded = true;
		}
		else if (!tabCarteraLoaded && grid === 'gridCartera') {
			domVentana.find('#'+grid).append($compile(domCartera)($scope));
			tabCarteraLoaded  = true;
		}
		else if (!tabVencimientoLoaded && grid === 'gridVencimiento') {
			domVentana.find('#'+grid).append($compile(domVencimiento)($scope));
			tabVencimientoLoaded  = true;
		}

		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
});