angular.module('ModuloMesa').controller('gConsultarCarteraMesaRFCtrl',function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsServiciosGrillasMesaRF,$timeout,$http, $element){
	var idVentana = bcsComunes.ventanaWinId($scope);
	var confGridCartera = bcsServiciosGrillasMesaRF.getConfiguracionCarteraMesa();
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	$scope.data = {};
	$scope.data.filtros = {};
	
	console.log(parametrosVentana);
	$scope.data.oConfiguracionSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				EsNombreFantasia: { field: "EsNombreFantasia",	operator: "eq", value : 'N' },
			},
			idPostFiltro:false,
			idDefecto:false,
	};
	$scope.data.oConfiguracionPortafolio={
			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
	};
	
	$scope.data.oConfiguracionMesaActiva={
		tipo : "combobox-mesas",
		configuracion : {},
		filtros : { 
				 IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" }
				,IdTipoProducto : {"field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF"}	
				},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		autoCargar: true 
	};
	
	reset = function (){
		$scope.data.kngFecha = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		$scope.data.SesionIdEmpresa = $scope.SesionIdEmpresa;
		$scope.data.oConfiguracionMesaActiva.idActual = false;
		
		//$scope.data.oConfiguracionSocioNegocio.idPostFiltro = $scope.SesionCorredoraIdSocioDeNegocio;
		$scope.data.oConfiguracionSocioNegocio.idDefecto = $scope.SesionCorredoraIdSocioDeNegocio;
		//$scope.data.oConfiguracionSocioNegocio.idActual = $scope.SesionCorredoraIdSocioDeNegocio;
		
		//$scope.data.oConfiguracionPortafolio.idActual = false;
		
		confGridCartera.options.dataSource.removeAll();
		
	};
	$scope.data.limpiarFiltros = function (){
		reset();
	};
	
	$scope.data.confBasicaCartera = confGridCartera.options;
	$scope.data.buscarCarteraMesa = confGridCartera.buscar;
	
	$scope.data.confBasicaCarteraReservada = function (item){
		item.kngFecha = $scope.data.kngFecha;
		item.SesionIdEmpresa = $scope.data.SesionIdEmpresa;
		item.CantidadReservada = true;
		
		return bcsServiciosGrillasMesaRF.getConfiguracionCarteraReservadaMesa(item).options;
	}
	
	$scope.data.abrirDepositosYAutodesembolsos = function(){
		var prom = bcsComunes.ventanaAbrir("ConsultaDepositosyAutodesembolsos", { data: $scope.data } ,false,false,$scope.winID);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data) {
				
			}
		});
	};
	
	
	
	reset();
});