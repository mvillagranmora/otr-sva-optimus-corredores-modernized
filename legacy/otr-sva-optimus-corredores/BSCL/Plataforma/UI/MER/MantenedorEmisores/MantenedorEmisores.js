angular.module('ModuloPlataforma').controller('MantenedorEmisoresCtrl',function($scope,bcsComunes){
	$scope.filtros = {};
	$scope.formData = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);

	$scope.formData.buscar = function(filtros) {
		var filters = new Array();
		if (filtros.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: filtros.IdEstado });
		$scope.formData.grilla.dataSource.filter(filters);
	};
	
	$scope.formData.ingresar = function() {
		var prom = bcsComunes.ventanaAbrir("CrearModificarEmisor", { } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
	};
	
	$scope.formData.modificar = function(iii) {
		if (!iii) return;
		var prom = bcsComunes.ventanaAbrir("CrearModificarEmisor", { emisor : iii[0] } ,false,false,$scope.idVentana);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}

		});
	};
	
	
	$scope.formData.grilla = {
			allowCopy: true,
			autoBind: false,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	height:500,
	      	pageable : true,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,
			
			columns : [
			           {field: "IdEmisor",						title : "UI_EMISOR",				width: "75px"},
			           {field: "NombreCompleto",				title : "UI_NOMBRE",				width: "150px", attributes:{style:"white-space:nowrap;"}},
			           {field: "IdEstado",						title : "UI_ESTADO",				width: "65px",groupable: true},
			           {field: "CodigoEmisor",					title : "UI_CODIGO",				width: "100px"},
			           {field: "CodigoRegulador",				title : "UI_CODIGO_REGULADOR",		width: "100px"},
			           {field: "Regulador",						title : "UI_REGULADOR",				width: "100px"},
			           {field: "EmiteIRF",						title : "UI_EMITE_IRF",				width: "75px",groupable: true},
			           {field: "EmiteIIF",						title : "UI_EMITE_IIF",				width: "75px",groupable: true},
			           {field: "EmiteFungible",					title : "UI_EMITE_FUNGIBLES",		width: "75px",groupable: true},
			           {field: "IndicadorAGF",					title : "UI_ES_AGF",				width: "75px",groupable: true},
			           {field: "IdEntidadClasificadoraRiesgo",	title : "UI_CLASIFICADOR_RIESGO",	width: "75px",groupable: true},
			           {field: "ValorClasificacionRiesgoIIF",	title : "UI_VALOR_RIESGO",			width: "75px",groupable: true}
			          ],
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: { field: "IdEmisor", dir: "asc" },
					pageSize:250,
					schema: {
						model : {
							fields : {
								"IdEmisor" 						: { type: "string"},
								"NombreCorto" 					: { type: "string"},
								"CodigoEmisor" 					: { type: "string"},
								"CodigoRegulador" 				: { type: "string"},
								"Regulador" 					: { type: "string"},
								"EmiteIIF" 						: { type: "string"},
								"EmiteIRF" 						: { type: "string"},
								"EmiteFungible" 				: { type: "string"},
								"ValorClasificacionRiesgoIIF" 	: { type: "string"},
								"IdEntidadClasificadoraRiesgo" 	: { type: "string"},
								"IndicadorAGF" 					: { type: "string"},
								"IdPersona" 					: { type: "number"},
								"IdEstado" 						: { type: "string"},
								"FechaActualizacion" 			: { type: "sysdate"},
								"DocumentoIdentidad" 			: { type: "string"},
								"TipoDocumentoIdentidad" 		: { type: "string"},
								"NombreCompleto" 				: { type: "string"},
								"TipoEntidad" 					: { type: "string"},
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=3161",
							dataType: "json"
						}
					}
				}
	      	}),

	};
	
});
