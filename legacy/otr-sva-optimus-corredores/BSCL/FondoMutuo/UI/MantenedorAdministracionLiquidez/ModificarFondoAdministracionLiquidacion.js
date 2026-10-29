angular.module('ModuloFFM').controller('ModificarFondoAdministracionLiquidacionCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	var conf = bcsConfiguracionFFM.getFondosPortafolioAdministracionLiquidez();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarConfiguracionPortafoliosAdmLiquidez = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	var parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.datosAntes = parametros.ConfiguracionAdministracionLiquidez;
	$scope.datosDespues = angular.copy($scope.datosAntes);
	$scope.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',$scope.datosDespues.IdMoneda,'','').cantidadDecimales;
	
	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  0 },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente    : { "field": "IdCliente","operator": "eq", "value" : 0 },
		 	 IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value" : 0 }
		 },
	 };
	
	$scope.oFondos={
			filtros:{
				MoneyMarket : {"field": "MoneyMarket","operator":"eq","value":'S'},
				IdMoneda : {"field": "IdMoneda","operator":"eq","value":$scope.datosDespues.IdMoneda}
			}
	}
	
	$scope.guardar = function(){
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosConfigurarFondoAdministracionLiquidez = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_FondoAdministracionLiquidez : [{
					 	IdMoneda		     :	$scope.datosDespues.IdMoneda,
					    IdSerieFondo 	     :	$scope.formData.FondoMutuo.IdSerieFondo || '',
					    MontoMinimo 	     :	$scope.datosDespues.MontoMinimo || 0,
					    Hora				 : 	$scope.datosDespues.HoraCierreLiquidezComercial
				 }]
		 }
	     bcsServicioFondosMutuos.ConfigurarFondoAdministracionLiquidez(datosConfigurarFondoAdministracionLiquidez).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ajustaVentana();
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CONFIGURAR_FONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_FONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_FONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			console.log('me cai jeje ' + respuesta);
  		 });
	}
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
});