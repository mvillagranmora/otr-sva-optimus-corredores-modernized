angular.module('ModuloPlataforma').controller('SelectorCuentaCustodioCtrl',function($scope,$translate,bcsComunes){

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	var filtrosGrid = [];
	for (var prop in parametros.filtros) {
		if (parametros.filtros.hasOwnProperty(prop)) {
			filtrosGrid.push(parametros.filtros[prop]);
		}
		
	}
	
	$scope.dobleClick = function(seleccionado) {
		bcsComunes.ventanaData($scope.idVentana,$scope.seleccionado.IdCuentaCustodioPortafolio);
		bcsComunes.ventanaCerrar($scope.idVentana);
	}

	$scope.configuracion = {
			allowCopy: false,
			autoBind: false,
			filterable: false,
			groupable: false,
			height:200,
			pageable: false,
			reorderable: false,
			resizable: false,
			scrollable: true,
			selectable: "row",
			sortable: true,
			userSettings:false,
			navigatable: false,
			gridCreated : function(grid) {
				$scope.gridApi = grid;
				grid.dataSource.filter(filtrosGrid);
			},
			dataSource: new bcs.DataSource({
	  			type : "odata",
	  			definition : {
	  				serverFiltering: true,
	  				transport : {
						read : {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1263",
							dataType : "json"
						}
					},
	  				schema : {
	  					model : {
	  						fields :{
								IdCuentaCustodioPortafolio	: { type: "number"},
								IdPortafolio				: { type: "number"},
								IdBoveda					: { type: "string"},
								NombreBoveda				: { type: "string"},
								Codigo						: { type: "string"},
								IdEstado					: { type: "string"},
								NombreEstado				: { type: "string"},
								NombreModeloCustodia			: { type: "string"},
								NombreTipoCustodia				: { type: "string"},
								NombreTipoCuentaCustodia		: { type: "string"},
								CodigoBancoCustodio				: { type: "string"},
								NombreCompletoBancoCustodio		: { type: "string"},
								
								CodigoBancoBancoLiquidador		: { type: "string"},
								NombreCompletoBancoLiquidador	: { type: "string"}
							}
	  					}
	  				}
				},
	  		}),
  			columns: [
  				{field: "NombreModeloCustodia",				title: "UI_MODELO",	width: "50px"},
  				{field: "NombreTipoCustodia",				title: "UI_TIPO",	width: "100px"},
  				{field: "NombreBoveda",						title: "UI_BOVEDA",	width: "125px"},
  				{field: "Codigo",							title: "UI_CUENTA",	width: "50px"},
  				{field: "NombreCompletoBancoCustodio",		title: "UI_BANCO_CUSTODIO",	width: "125px"},
  				{field: "NombreCompletoBancoLiquidador",	title: "UI_BANCO_LIQUIDADOR",	width: "125px"},
  			]
	};
		
});