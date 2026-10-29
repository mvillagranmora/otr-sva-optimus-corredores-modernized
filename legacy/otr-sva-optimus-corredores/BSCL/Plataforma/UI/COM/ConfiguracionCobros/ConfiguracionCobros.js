angular.module('ModuloOrdenes').controller('ConfiguracionCobrosCtrl',function($scope,bcsComunes,$translate) {

	$scope.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.pestanaTiposCobro = {};
	$scope.pestanaCobros = {};
	
	$scope.pestanaCobros.agregarCobro = function() {
		var p = bcsComunes.ventanaAbrir("IngresoCobro",{},false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(p).then(function(result){
			$scope.apiGrid.dataSource.read();	
		})
	}
	
	$scope.pestanaCobros.editarCobro = function(seleccionado) {
		var p = bcsComunes.ventanaAbrir("IngresoCobro",{ DefinicionCobro : seleccionado },false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(p).then(function(result){
			$scope.apiGrid.dataSource.read();	
		})
	}
	
	$scope.pestanaCobros.configuracionCobros = {
			autoBind: true,
      		allowCopy: true,
      		excel : {
				allPages: true,
			},
			height:400,
			resizable: true,
      		selectable: "row",
      		gridCreated : function(grid){
      			$scope.apiGrid = grid;
      		},
      		scrollable: true,
      		sortable: true,
      		navigatable: true,
      		filterable : true,
			columns : [
				{field: "CodigoDefinicionCobro", 		title: 'UI_CODIGO',		width: "200px"},
				{field: "Descripcion",		 			title: 'UI_NOMBRE',width: "150px"},
				{field: "NombreClaseCobro", 			title: 'UI_CLASE_COBRO',width: "150px"},
				{field: "NombreTipoCobro", 				title: 'UI_TIPO_COBRO',	width: "175px"},
				{field: "NombreTipoProducto",			title: 'UI_TIPO_PRODUCTO',	width: "250px"},
				{field: "ValorCobroCombinado",			width: "75px",title: $translate.instant("UI_VALOR"),template : " <label> #:  (util.bcs_formato(ValorCobroCombinado,'GEN',UsoMonedaFormato,IdMonedaCobroUI, '','')).valor #</label>"},
				{field: "ValorCobroMonedaPorcentaje",	width: "100px",title: $translate.instant("UI_MONEDA_PORCENTAJE")},

				
			],
			
			dataSource: new bcs.DataSource({
	  			type : "odata",
	  			definition : {
	  				serverFiltering : true,
	  				serverSorting : true,
	  				sort : { field : "CodigoDefinicionCobro",dir : "asc" },
	  				filter : [{ field : "IdEmpresa", operator: "eq", value : $scope.SesionIdEmpresa}],
					transport: {
						read: {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=3530",
							dataType: "json"
						},
					},
					schema : {
						model : {
							fields : {
								IdEmpresa					: {type : "number"},
								CodigoDefinicionCobro		: {type : "string"},
								Descripcion	 				: {type : "string"},
								IdClaseCobro	 			: {type : "string"},
								NombreClaseCobro	 		: {type : "string"},
								NombreTipoCobro	 			: {type : "string"},
								IdTipoCobro 				: {type : "string"},
								NombreTipoProducto			: {type : "string"},
								MontoFijo_MontoCobro		: {type : "number"},
								PorcentualFijo_ValorCobro	: {type : "number"},
								ValorCobroCombinado 		: {type : "number"},
								IdMonedaCobroUI		 		: {type : "string"},
								ValorCobroMonedaPorcentaje 	: {type : "string"},
								UsoMonedaFormato 			: {type : "string"}
							}
						} 
							
					}
				},
	  		}),
	};

});


