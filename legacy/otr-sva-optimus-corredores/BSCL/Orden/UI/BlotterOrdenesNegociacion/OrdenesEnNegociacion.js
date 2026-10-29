angular.module('ModuloOrdenes').controller('OrdenesEnNegociacionCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionOrdenesV2,bcsServicioOrden){

	var conf = bcsConfiguracionOrdenesV2.getConfiguracionConsultarOrdenesEnNegociacion();
	
	$scope.defaultOptionsNew = conf.options;
	$scope.filtros = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);

	$scope.buscarOrdenes = function(apiGrid,filtros){
		var filtros = bcsConfiguracionOrdenesV2.validarFiltrosV2($scope.filtros,'ORDEN');
		if ($scope.filtros.Operador && $scope.filtros.Operador.trim() != 'TODOS'){
			bcsConfiguracionOrdenesV2.generarFiltroOperadorCarteraInstrumento($scope.filtros,function(filtroInstrumentos){
				filtros.push(filtroInstrumentos);
				$scope.apiGridOrdenes.dataSource.filter(filtros);
				$scope.ordenesSeleccionadas = undefined;
			});
		}else{
			$scope.apiGridOrdenes.dataSource.filter(filtros);
			$scope.ordenesSeleccionadas = undefined;
		}
	};
	
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.buscarInstrumento = "-1";
		$scope.filtros.IdTipoOperacion = 0;
		$scope.filtros.IdTipoInstrumento = 0;		
		$scope.filtros.IdClasificacionTipoInstrumento = 'RV';
		$scope.filtros.fechaOperacionVigencia = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.IdCorredorCorresponsal = 0;
		
		$scope.filtros.filtroBolsa.IdInstrumento.value = 0;
		$scope.filtros.filtroBolsa.read = true;
		$scope.filtros.IdBolsa = 0; 
		$scope.filtros.IdMoneda	= 0;
		$scope.filtros.IdCondicionDeLiquidacion = 0;
	};

	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleOrden", {IdOrden : items[0].IdOrden} );
	};
  
	$scope.retirarOfertas = function(ordenesSeleccionadas) {
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);	
		
		var datos = {};
		var ordenes = [];
  		$.each(ordenesSeleccionadas,function(key,orden){
  			ordenes.push({IdOrden : orden.IdOrden});
  			
  		});
  		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
  		datos.p_IdUsuario = $scope.SesionIdUsuario;
  		datos.p_IdModulo = 'ORD';
  		datos.p_Ordenes = ordenes;
  		
  		bcsServicioOrden.RetirarOfertasOrdenes(datos).$promise.then(function(result){
  			bcsComunes.ventanaOverlay($scope.idVentana,false);	
  			
  			$scope.ordenesSeleccionadas = undefined;
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  			
  			 bcsComunes.ventanaMsg({  
	        	conflictos : util.parseServerResponse(result)}, $scope.idVentana);
  			
  		},function(result){
  			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({  
	        	conflictos : util.parseServerResponse(result)}, $scope.idVentana);	
  			$scope.buscarOrdenes($scope.apiGridOrdenes,$scope.filtros);
  		});
		

	};
  

	$scope.oConfiguracionTipoInstrumento={
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : ""
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.oConfiguracionTipoOperacion={
			filtros : { 	EsOrden :  {"field"   : "EsOrden","operator": "eq","value"   : "S"},
							IdTipoInstrumento :	{"field"   : "IdTipoInstrumento","operator": "eq","value"   : ""}	},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.filtrosClasificacionTipoInstrumento = {
			logic : "or",
			RV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RV"},
			DV : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "DV"},
			RF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
			IF : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
	};
	
	$scope.filtros.filtroBolsa = {
		IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : 0 }
	};
	
	$scope.filtrosOperadorCarteraInversion = {
		IdEmpresa : {"field": "IdEmpresa","operator": "eq","value": $scope.SesionIdEmpresa} ,
		CodigoOperador : {"field": "CodigoOperador","operator": "eq","value": "false"} 
	};

	$scope.filtrosMonedaNegociacion = {
		IdMoneda : {"field": "IdMoneda","operator": "eq","value": ""}    
	};
	
	$scope.limpiarFiltros();
  
});
