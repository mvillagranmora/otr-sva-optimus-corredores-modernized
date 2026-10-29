angular.module('ModuloPlataforma').controller('SelectorEmisoresCtrl',function($scope,$translate,bcsComunes){

	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	var parametros = bcsComunes.ventanaParams($scope.idVentana);
	var filtrosGrid = [];
	for (var prop in parametros.filtros) {
		if (parametros.filtros.hasOwnProperty(prop)) {
			filtrosGrid.push(parametros.filtros[prop]);
		}
		
	}
	
	$scope.dobleClick = function(seleccionado) {
		bcsComunes.ventanaData($scope.idVentana,$scope.seleccionado.IdEmisor);
		bcsComunes.ventanaCerrar($scope.idVentana);
	}

	$scope.configuracion = {
			allowCopy: false,
			autoBind: false,
			filterable: true,
			groupable: false,
			height:200,
			pageable: true,
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
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3161",
							dataType : "json"
						}
					},
	  				schema : {
	  					model : {
	  						fields :{
	  							IdEmisor					: { type: "string"},
	  							NombreCorto					: { type: "string"},
	  							CodigoEmisor				: { type: "string"},	  							
	  							CodigoRegulador				: { type: "string"},	  							
	  							Regulador					: { type: "string"},	  							
	  							EmiteIIF					: { type: "string"},	  							
	  							EmiteIRF					: { type: "string"},	  							
	  							EmiteFungible				: { type: "string"},	  							
	  							ValorClasificacionRiesgoIIF	: { type: "string"},	  							
	  							IdEntidadClasificadoraRiesgo: { type: "string"},	  							
	  							IndicadorAGF				: { type: "string"},	  							
	  							IdPersona					: { type: "number"},								
	  							IdEstado					: { type: "string"},	  							
	  							FechaActualizacion			: { type: "string"},
	  							DocumentoIdentidad			: { type: "string"},
	  							TipoDocumentoIdentidad		: { type: "string"},
	  							NombreCompleto				: { type: "string"},
	  							TipoEntidad	  				: { type: "string"}
							}
	  					}
	  				}
				},
	  		}),
  			columns: [
  				{field: "IdEmisor",							title: "UI_EMISOR",	width: "50px"},
  				{field: "NombreCompleto",					title: "UI_NOMBRE",	width: "100px"},
  				{field: "DocumentoIdentidad",				title: "UI_DOCUMENTO_IDENTIDAD",	width: "100px"},
  				{field: "IdEstado",							title: "UI_ESTADO",	width: "50px"},
  			]
	};
		
});