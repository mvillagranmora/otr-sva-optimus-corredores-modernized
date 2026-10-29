angular.module('ModuloFFM').controller('ModificarFondoAdministracionLiquidacionPortafolioCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData.datosFondoDefecto = {};
	var overlay = bcsComunes.overlay;
	var parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',parametros.ConfiguracionAdministracionLiquidez.IdMoneda,'','').cantidadDecimales;
	$scope.formData.IdMonedaFondo = parametros.ConfiguracionAdministracionLiquidez.IdMoneda;
	$scope.formData.MontoMinimo = parametros.ConfiguracionAdministracionLiquidez.MontoMinimo;
		
	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  $scope.formData.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			}
		};

	$scope.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
			 IdCliente    : { "field": "IdCliente","operator": "eq", "value" : 0 },
		 	 IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value" : 0 }
		 }
	 };
	
	$scope.oFondos={
			filtros:{
				MoneyMarket : {"field": "MoneyMarket","operator":"eq","value":'S'},
				IdMoneda : {"field": "IdMoneda","operator":"eq","value":parametros.ConfiguracionAdministracionLiquidez.IdMoneda}
			}
	}
	
	
	$scope.guardarPortafolio = function(){
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosConfigurarPortafolioFondoAdministracionLiquidez = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_PortafolioFondoAdministracionLiquidez : [{
					 	IdMoneda		     :	parametros.ConfiguracionAdministracionLiquidez.IdMoneda,
					    IdSerieFondo 	     :	$scope.formData.datosFondoPortafolio.IdSerieFondo || '',
					    MontoMinimo 	     :	$scope.formData.MontoMinimo || 0,
					    IdCliente			 :  $scope.oSocioNegocio.seleccionado.IdCliente,
					    IdPortafolio		 :	$scope.oPortafolio.seleccionado.IdPortafolio,
					    IdEstado			 :	'ACT'
				 }]
		 }
	     bcsServicioFondosMutuos.ConfigurarPortafolioAdministracionLiquidez(datosConfigurarPortafolioFondoAdministracionLiquidez).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CONFIGURAR_PORTAFOLIOFONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsConfiguracionFFM.ingresarCerrarVentana(id, $scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_PORTAFOLIOFONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_PORTAFOLIOFONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	}
	
	
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.ajustaVentana();
	
	$scope.oSocioNegocio.idDefecto = parametros.ConfiguracionAdministracionLiquidez.IdSocioDeNegocio;
    $scope.oSocioNegocio.filtros.IdCliente.value = parametros.ConfiguracionAdministracionLiquidez.IdCliente;
	$scope.oSocioNegocio.filtros.read = true;
	 
	$scope.oPortafolio.idDefecto	= parametros.ConfiguracionAdministracionLiquidez.IdPortafolio;
	$scope.oPortafolio.filtros.IdPortafolio.value = parametros.ConfiguracionAdministracionLiquidez.IdPortafolio;
	$scope.oPortafolio.filtros.read = true;
	 
	$scope.formData.datosFondoDefecto.IdDefecto = parametros.ConfiguracionAdministracionLiquidez.IdSerieFondo;

});