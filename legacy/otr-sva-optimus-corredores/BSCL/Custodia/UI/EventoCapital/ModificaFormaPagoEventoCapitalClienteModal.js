angular.module('ModuloCustodia').controller('ModificaFormaPagoEventoCapitalClienteModalCtrl',
	function($scope,
			$timeout,
			$modal,
			$translate,
			bcsComunes,
			bcsAlerta, 
			OPCIONES_BCSALERTA,
			bcsBlotterEventoCapitalGrid,
			bcsServicioPlataforma,
			bcsServiciosCustodia,
			bcsServiciosEventoCapital,
//			bcsConfiguracionFFM,
			bcsAlerta)
{	
	
	//*** configuracion de objetos ***//
	
	var ModificaFormaDePago = this;
	$scope.data = {};
	$scope.filtro = {};
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var overlay = bcsComunes.overlay;
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.oDatosPagoEvento = parametros


	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	
	$scope.configuracionPreferenciaPagoPortafolio = {
		filtros : {
			IdPortafolio 	: { "field"   : "IdPortafolio"		,"operator": "eq", "value"   :  "" },
			IdTipoOperacion : { "field"   : "IdTipoOperacion"	,"operator": "eq", "value"   :  "DIVDIN" },
			IdMedioDePago 	: { "field"   : "IdMedioDePago"		,"operator": "neq", "value"   :  "CIN" },
			IdMoneda 		: { "field"   : "IdMoneda"			,"operator": "eq", "value"   :  "" }
		}
	};
	
	/*************************************************************************
	* CARGA INICIAL MODIFICACIÓN
	**************************************************************************/		
	if ($scope.data.oDatosPagoEvento.EventoCapitalCliente){

		$scope.configuracionPreferenciaPagoPortafolio.filtros.IdPortafolio.value	= $scope.data.oDatosPagoEvento.EventoCapitalCliente.IdPortafolio
		$scope.configuracionPreferenciaPagoPortafolio.filtros.IdMoneda.value		= $scope.data.oDatosPagoEvento.EventoCapitalCliente.IdMoneda
		$scope.data.DocumentoIdentidadTXT 											= $scope.data.oDatosPagoEvento.EventoCapitalCliente.DocumentoIdentidad
		$scope.data.PortafolioTXT													= $scope.data.oDatosPagoEvento.EventoCapitalCliente.CodigoPortafolio
		$scope.data.IdPortafolioTXT													= $scope.data.oDatosPagoEvento.EventoCapitalCliente.IdPortafolio
		$scope.data.NombreTXT 														= $scope.data.oDatosPagoEvento.EventoCapitalCliente.NombreCliente
		$scope.configuracionPreferenciaPagoPortafolio.idPostFiltro	 				= $scope.data.oDatosPagoEvento.EventoCapitalCliente.IdPreferenciaPagoPortafolio
		$scope.data.IdTipoOperacionTXT												= 'DIVDIN'
		$scope.data.IdSeleccionadoTXT												= 'CIN'
	}else{
		$scope.configuracionPreferenciaPagoPortafolio.filtros.IdPortafolio.value	= $scope.data.oDatosPagoEvento.IdPortafolio;
		$scope.configuracionPreferenciaPagoPortafolio.filtros.IdMoneda.value		= $scope.data.oDatosPagoEvento.IdMoneda;
		$scope.data.DocumentoIdentidadTXT = $scope.data.oDatosPagoEvento.DocumentoIdentidad
	}
	
	/*************************************************************************
	* CARGA INICIAL MODIFICACIÓN
	**************************************************************************/		
	$scope.data.ModificaFormaDePago = function()
	{
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		datos.p_IdEmpresa 					= $scope.SesionIdEmpresa;
		datos.p_IdUsuario 					= $scope.SesionIdUsuario; 
		datos.p_IdModulo 					= 'CUS';
		datos.p_UsaReglasNegocio 			= 'S';

		//FORMA DE PAGO
		datos.p_IdEventoCapitalCliente		= $scope.data.oDatosPagoEvento.IdEventoCliente;
		datos.p_IdPreferenciaPagoPortafolio	= $scope.data.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio;
		//FIN FORMA DE PAGO
		
		bcsServiciosEventoCapital.ModificarFormaDePagoEventoCapitalCliente
							(datos,
							function(respuesta){	  		
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								$scope.data.btnCerrar(true)
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								$scope.data.btnCerrar(true)
							});
	};
	
	$scope.data.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData(idVentana,refrescar);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	
});