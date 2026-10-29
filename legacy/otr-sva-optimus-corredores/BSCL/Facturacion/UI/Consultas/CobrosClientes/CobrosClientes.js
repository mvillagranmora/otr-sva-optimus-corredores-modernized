
angular.module('ModuloFacturacion').controller('CobrosClientesCtrl',function($scope,bcsComunes,$timeout,$compile){

	var tabCobrosClientes = false;
	var tabDiferenciaCobros = false;

	
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	                                                    
	var domCobrosClientes   =  '<div ng-include="\'../../Facturacion/UI/Consultas/CobrosClientes/ConsultaCobrosClientes.html\'" ></div>';	
	var domDiferenciaCobros =  '<div ng-include="\'../../Facturacion/UI/Consultas/CobrosClientes/ConsultaDiferenciaCobros.html\'" ></div>';
	
	
	
	$scope.select = function(grid)
	{
		if (!tabCobrosClientes && grid === 'gridCobrosClientes') {
			domVentana.find('#'+grid).append($compile(domCobrosClientes)($scope));
			tabCobrosClientes = true;
		}
		else if (!tabDiferenciaCobros && grid === 'gridDiferenciaCobros') {
			domVentana.find('#'+grid).append($compile(domDiferenciaCobros)($scope));
			tabDiferenciaCobros  = true;
		}
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};

});

