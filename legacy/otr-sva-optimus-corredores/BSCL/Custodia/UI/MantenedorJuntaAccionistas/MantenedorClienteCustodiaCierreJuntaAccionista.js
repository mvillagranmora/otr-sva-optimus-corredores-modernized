angular.module('ModuloCustodia').controller('MantenedorClienteCustodiaCierreJuntaAccionistaCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsBlotterConsultaJunta, bcsServiciosJuntaAccionista){
	
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.datosAntes = {};
	$scope.datosDespues = {};
	$scope.evento = $scope.parametros.evento;
	

	$scope.filtros.IdEmisor = $scope.evento.IdEmisor;
	
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
	if($scope.parametros.esEdicion){
		$scope.datosAntes = $scope.parametros.clienteCustodia;
		$scope.datosDespues = angular.copy($scope.datosAntes);
		$scope.oSocioNegocio.filtros.IdSocioDeNegocio = { "field": "IdSocioDeNegocio","operator": "eq", "value"   : $scope.datosDespues.IdSocioDeNegocio }
	}
	
	$scope.guardarClienteCustodia = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosIngresoClienteCustodia = {
			p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
			p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
			p_IdModulo : 'CUS',
			p_ClienteDerecho : []
		}
		if($scope.parametros.esEdicion){
			datosIngresoClienteCustodia.p_ClienteDerecho = [{
				IdInterno : 0,
				IdClienteConDerechoJuntaAccionista : $scope.datosDespues.IdClienteConDerechoJuntaAccionista,
				IdEventoJuntaAccionista : $scope.datosDespues.IdEventoJuntaAccionista,
				IdInstrumento : $scope.datosDespues.IdInstrumento,
				CantidadDerecho : $scope.datosDespues.CantidadDerecho,
				Instruccion : $scope.datosDespues.Instruccion
			}]
			bcsServiciosJuntaAccionista.ModificarClienteConDerechoJuntaAccionista(datosIngresoClienteCustodia).$promise.then(function(respuesta){
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_CLIENTE_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
					bcsBlotterConsultaJunta.ingresarCerrarVentana(id,$scope.winID);
		     	 }else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_CLIENTE_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	  		 },function(respuesta){
	  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_CLIENTE_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	  		 });
		}else{
			datosIngresoClienteCustodia.p_ClienteDerecho = [{
				IdInterno : 0,
				IdEventoJuntaAccionista : $scope.evento.IdEventoJuntaAccionista,
				IdCliente : $scope.oSocioNegocio.seleccionado.IdCliente,
				IdInstrumento : $scope.instrumento.IdInstrumento || 0,
				CantidadDerecho : $scope.datosDespues.CantidadDerecho,
				Instruccion : $scope.datosDespues.Instruccion || ''
			}]
			bcsServiciosJuntaAccionista.CrearClienteConDerechoJuntaAccionista(datosIngresoClienteCustodia).$promise.then(function(respuesta){
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
		     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREAR_CLIENTE_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
					bcsBlotterConsultaJunta.ingresarCerrarVentana(id,$scope.winID);
		     	 }else{
		     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_CLIENTE_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		     	 }
	  		 },function(respuesta){
	  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_CLIENTE_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	  		 });
		}
	}
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,30);
    }
	
	$scope.ajustaVentana();
	
});