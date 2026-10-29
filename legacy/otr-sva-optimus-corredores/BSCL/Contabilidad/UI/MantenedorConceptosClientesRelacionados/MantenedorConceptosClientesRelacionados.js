angular.module("ModuloContabilidad").controller("MantenedorConceptosClientesRelacionadosCtrl", function ($rootScope, $scope, $translate, bcsComunes, bcsConceptosNormativos) {
	$scope.data = {}
	const IdVentana = bcsComunes.ventanaWinId($scope);

	const parametros = {
		p_IdEmpresa: $scope.$root.SesionIdEmpresa,
		p_IdUsuario: $scope.$root.SesionIdUsuario,
		p_IdModulo: "COM"
	}
	
	const dsConceptosNormativos = new bcs.DataSource({
		type: "call",
		definition: {
			serverFiltering: false,
			serverPaging: false,
			serverSorting: true,
			pageSize: 200,
			callParameters	: parametros,
			callTransaction	: "15486546",
			callResultSet	: "conceptosNormativos",
			schema: {
				model: {
					fields: {
						IdConceptoNormativo: { type: "string" },
						ConceptoNormativo: { type: "string" },
						IdEstado: { type: "string" },
					}
				}
			}
		},
	});

	$scope.data.ConfiguracionGrillaConceptosNormativos = {
		dataSource: dsConceptosNormativos,
		columns: [
			{ field: "IdConceptoNormativo", hidden: true },
			{ field: "ConceptoNormativo", title: "UI_CONCEPTO", hidden: false }
		],
		filterable: false,
		autoBind: true,
		height: 400,
		selectable: "single, row",
		pageable: false
	};

	$scope.ModificarConceptoClienteRelacionado = function () {
		bcsComunes.ventanaAbrir("ConfiguracionConceptosClientesRelacionados", {
			ConceptoNormativoSeleccionado: $scope.data.ConceptoNormativoSeleccionado,
		}, false, false, IdVentana);
	};

});