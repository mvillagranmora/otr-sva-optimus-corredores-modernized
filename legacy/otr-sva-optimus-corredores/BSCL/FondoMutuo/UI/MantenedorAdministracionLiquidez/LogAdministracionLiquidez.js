angular.module('ModuloFFM').controller('LogAdministracionLiquidezFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaLogAdministracionLiquidez();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaIngreso = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oSocioNegocio.idActual = false;
		$scope.filtros.IdPortafolio = 0;
		$scope.filtros.IdMoneda = false;
        $scope.formData.setFondo = "-1";
	};
	
	$scope.oFondos={
			filtros:{
				MoneyMarket : {"field": "MoneyMarket","operator":"eq","value":'S'}
			}
	}
	
	
	$scope.buscarLogAdministracionLiquidez = function(){
		var filtrosLog = [];

		if($scope.filtros.fechaIngreso)
			filtrosLog.push({ field: "FechaIngreso", operator: "eq", value: $scope.filtros.fechaIngreso } );
		if($scope.filtros.fondoMutuo)
			filtrosLog.push({ field: "IdSerieFondo", operator: "eq", value: $scope.filtros.fondoMutuo.IdSerieFondo } );
		if($scope.oSocioNegocio.seleccionado)
			filtrosLog.push({ field: "IdCliente", operator: "eq", value: $scope.oSocioNegocio.seleccionado.IdCliente } );
		if($scope.oPortafolio.seleccionado)
			filtrosLog.push({ field: "IdPortafolio", operator: "eq", value: $scope.oPortafolio.seleccionado.IdPortafolio } );
		if($scope.filtros.IdMoneda){
			filtrosLog.push({ field: "IdMoneda", operator: "eq", value: $scope.filtros.IdMoneda } );
		}
		apiGrid.filter(filtrosLog);
		$scope.apiGridLogAdmLiquidez.refresh;
	};
	
	$scope.abrirDetalle = function (items) {
		if (items[0].IdSolicitudAporteRescate == 0) return;
		bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate : items[0].IdSolicitudAporteRescate} );
  	};
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
  	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.limpiarFiltros();
	
});