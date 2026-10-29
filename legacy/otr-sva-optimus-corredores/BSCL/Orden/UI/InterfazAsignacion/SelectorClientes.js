angular.module('ModuloOrdenes').controller('SelectorClientesCtrl',
		function($scope,bcsServicioOrden,bcsComunes,bcsConfiguracionOrdenesV2,$timeout,$translate){
	
	$scope.aceptar = function() {
		bcsComunes.ventanaData(idVentana, $scope.seleccionado);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.IdLibroOrdenes = parametros.IdLibroOrdenes;
	//cargar filtros	
	$scope.configuracion = bcsConfiguracionOrdenesV2.getConfiguracionSelectorClientes();
	
	$scope.filtrosGrupoCliente = {
			IdEstado : {"field" : "IdEstado", "operator" : "eq", "value" : 'ACT'}
	}
	
	$scope.editarGrupo = function() {
		if (!$scope.grupoSeleccionado)
			return;
		var id1 = bcsComunes.ventanaAbrir('ModificarGrupoPortafolio',{ 
			IdGrupo: $scope.grupoSeleccionado.IdGrupo, 
			Nombre: $scope.grupoSeleccionado.Nombre, 
			Codigo: $scope.grupoSeleccionado.Codigo, 
			Estado : $scope.grupoSeleccionado.IdEstado  
		},false,false,idVentana);
		bcsComunes.ventanaPromesa(id1).then(function(respuesta) {});
	};
	
	$scope.buscarClientes = function() {
		
		var filters = [];
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
		
		if ($scope.IdLibroOrdenes)
			filters.push({ field: "IdLibroOrdenes", operator: "eq", value: $scope.IdLibroOrdenes });
		if ($scope.tipoDocumentoIdentidad){
    		filters.push({ field: "TipoDocumentoIdentidad", operator: "eq", value: $scope.tipoDocumentoIdentidad });
    	}
		if ($scope.IdGrupo){
    		filters.push({ field: "IdGrupo", operator: "eq", value: $scope.IdGrupo });
    	}
		if ($scope.numeroDocumentoIdentidad){
    		filters.push({ field: "tolower(DocumentoIdentidad)", operator: "contains", value: $scope.numeroDocumentoIdentidad.toLowerCase() });
    	}
		if ($scope.nombreCompleto){
			filters.push({ field: "tolower(NombreCompleto)", operator: "contains", value: $scope.nombreCompleto.toLowerCase() });
    	}
		if ($scope.recordData.BrokerObj.IdPersona){
			filters.push({ field: "IdPersonaBrokerExtranjero", operator: "eq", value: $scope.recordData.BrokerObj.IdPersona });	
		}

		$scope.apiGrid.dataSource.filter(filters);
		
	};

});
