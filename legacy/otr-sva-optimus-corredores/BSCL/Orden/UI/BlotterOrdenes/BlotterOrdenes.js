angular.module('ModuloOrdenes').controller('BlotterOrdenesCtrl',function($scope,bcsComunes,$timeout,$compile){
	
	var tabOrdenesLoaded = false;
	var tabAsignacionesLoaded = false;
	var tabTransaccionesLoaded = false;
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domOrdenes =  '<div ng-include="\'../../Orden/UI/ConsultarOrdenes/ConsultarOrdenes.html\'" ></div>';
	var domAsignaciones =  '<div ng-include="\'../../Orden/UI/ConsultarAsignaciones/ConsultarAsignaciones.html\'" ></div>';
	var domTransacciones =  '<div ng-include="\'../../Orden/UI/ConsultarTransacciones/ConsultarTransacciones.html\'" ></div>';
	
	$scope.select = function(grid)
	{
		if (!tabOrdenesLoaded && grid === 'gridOrdenes') {
			domVentana.find('#'+grid).append($compile(domOrdenes)($scope));
			tabOrdenesLoaded = true;
		}
		else if (!tabAsignacionesLoaded && grid === 'gridAsignaciones') {
			domVentana.find('#'+grid).append($compile(domAsignaciones)($scope));
			tabAsignacionesLoaded  = true;
		}
		else if (!tabTransaccionesLoaded && grid === 'gridTransacciones') {
			domVentana.find('#'+grid).append($compile(domTransacciones)($scope));
			tabTransaccionesLoaded  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	/*if ($scope.SesionLocale === 'CL'){
		domVentana.find('#gridTransacciones').append($compile(domTransacciones)($scope));
		tabTransaccionesLoaded  = true;
	}
	//PE
	else {
		domVentana.find('#gridOrdenes').append($compile(domOrdenes)($scope));
		tabOrdenesLoaded  = true;
	}*/
	
	let firstHeight = $('#Win_BlotterOrdenes');
		firstHeight.css('height', 'auto');

});