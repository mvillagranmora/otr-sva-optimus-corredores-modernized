angular.module('ModuloFacturacion').controller('ModificarPortafolioMovimientoModalCtrl', function($scope, $translate, $modal, bcsComunes,bcsServiciosFacturacion, bcsServicioPlataforma, $timeout) 
{
    
    var bModifMvtoMod = this;
    
    //Recupera parametros desde la ventana recibida
    
    var idVentana = bcsComunes.ventanaWinId ($scope);    
    $scope.dato = bcsComunes.ventanaParams(idVentana);
    
    bModifMvtoMod.oMovimientoComprobante = $scope.dato;
    bModifMvtoMod.infoPrincipalCliente = bModifMvtoMod.oMovimientoComprobante[0];
    $scope.data	= {}; 

   	
    $scope.data.oPortafolio={
 	   tipo : "combobox-portafolio",
 	   configuracion :{},
 	   idActual : false,
 	   seleccionado : false,
 	   filtros: {
 		  	      IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  bModifMvtoMod.infoPrincipalCliente.IdCliente},
 	   			  IdEstado  : { "field": "IdEstado","operator": "eq", "value"   :  "ACT"}
 			    },
    };	
    

    //-----------------------------------------------------
    //Cierra modal
    //-----------------------------------------------------	 
    $scope.data.cancelar = function() 
      {
 		bcsComunes.ventanaCerrar(idVentana);
 	 };
 	 
 	//-----------------------------------------------------
	//Actualiza Portafolio Movimientos 
	//-----------------------------------------------------
	$scope.data.modificarMovimientoComprobanteGral = function()   
	{	       
		$scope.MvtoCbmte = $.extend({},$scope.bModifMvtoMod.oMovimientoComprobante);
				
		bcsComunes.setConflictos([]);
				
		//Acumula tabla de entrada de movimiento comprobante - general
		var MovimientoFacturarGral = [];

		$.each($scope.MvtoCbmte,function(key,value){
			MovimientoFacturarGral.push(
	                     {
		                    IdMovimientoComprobante :$scope.MvtoCbmte[key].IdMovimientoComprobante
		                 }
			);	
		});
		//parametros de servicio
		var datos = {};		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdUsuario = $scope.SesionIdUsuario;
		datos.p_IdModulo  ='FAC';
		datos.p_simularOperacion = 'N';
		datos.p_UsaReglasNegocio = 'S';
		datos.p_EsIngresoAutomatico = 'N';
		datos.p_IdPortafolioAConfigurar = $scope.data.oPortafolio.seleccionado.IdPortafolio;
		datos.P_MOVIMIENTOFACTURAR = MovimientoFacturarGral;
		
		bcsComunes.ventanaOverlay(idVentana, true);
		
		bcsServiciosFacturacion.ModificarMovimientoComprobanteMasivo(datos).$promise.then(function(respuesta)
		{
			bcsComunes.ventanaOverlay(idVentana, false);

			if (respuesta.P_OUT_ERRORESCONTADOR == "0") 
			{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_OPERACION_REALIZADA_EXITO"),
					conflictos:false
				},idVentana);
				bcsComunes.ventanaCerrar(idVentana);
			}
			else 
			{
				if (respuesta.P_OUT_ERRORESCONTADOR > 0)  
				{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_MODIFICACION"),
						conflictos:util.parseServerResponse(respuesta)
					}, idVentana);
				}
				bcsComunes.ventanaCerrar(idVentana);
			}

		},function(respuesta)
	    {
			bcsComunes.ventanaOverlay(idVentana, false);
		  
			bcsComunes.msg("error","Error",'UI_ERROR_MODIFICACION');
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaCerrar(idVentana);
	    });
	 };
});
