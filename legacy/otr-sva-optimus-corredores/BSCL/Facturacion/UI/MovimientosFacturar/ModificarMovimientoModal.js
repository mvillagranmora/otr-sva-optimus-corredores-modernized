angular.module('ModuloFacturacion').controller('ModificarMovimientoModalCtrl', function($scope, bcsComunes, bcsServiciosFacturacion,$translate,$timeout) 
{
    
    var bModifMvtoMod = this;
    
    //Recupera parametros desde la ventana recibida
    
    var idVentana = bcsComunes.ventanaWinId ($scope);    
    $scope.dato = bcsComunes.ventanaParams(idVentana);
    bcsComunes.ventanaAutoHeight(idVentana);
    bModifMvtoMod.oMovimientoComprobante = $scope.dato;

	if(bModifMvtoMod.oMovimientoComprobante.validadorLocal){
		bcsComunes.ventanaTitulo(idVentana,$translate.instant('UI_MODIFICAR_MONTO_LOCAL'));
	}

    $scope.montoInicial = bModifMvtoMod.oMovimientoComprobante.MontoMovimientoComprobante
    	 
	 
	 $scope.ajusteSencillo = function() { 
			 
			 var ajusteSencilloData = [];
			 ajusteSencilloData.push(
											{     
												IdMovimientoComprobante 		: bModifMvtoMod.oMovimientoComprobante.IdMovimientoComprobante,
												IdMonedaMovimientoComprobante	: bModifMvtoMod.oMovimientoComprobante.IdMonedaMovimientoComprobante,
												IdMoneda						: bModifMvtoMod.oMovimientoComprobante.IdMoneda,
												IdPortafolio            		: bModifMvtoMod.oMovimientoComprobante.IdPortafolio,
												IdAsignacion            		: bModifMvtoMod.oMovimientoComprobante.IdAsignacion,
												IdFormaCustodia         		: bModifMvtoMod.oMovimientoComprobante.IdFormaCustodia,
												Glosa	                		: bModifMvtoMod.oMovimientoComprobante.Glosa,
												MontoMovimientoComprobante 		: bModifMvtoMod.oMovimientoComprobante.MontoMovimientoComprobante,
												IdTransaccion					: bModifMvtoMod.oMovimientoComprobante.IdTransaccion,
												Cantidad						: bModifMvtoMod.oMovimientoComprobante.Cantidad,
												ValorRescate					: bModifMvtoMod.oMovimientoComprobante.ValorRescate

												
											}
										);
						
				//parametros de servicio
				var datos = {};		
				datos.p_IdEmpresa = $scope.SesionIdEmpresa;
				datos.p_IdUsuario = $scope.SesionIdUsuario;
				datos.p_IdModulo  ='FAC';
				datos.p_simularOperacion = 'N';
				datos.p_UsaReglasNegocio = 'S';
				datos.p_EsIngresoAutomatico = 'N';
				datos.P_ajusteSencillo = ajusteSencilloData;

				
				bcsServiciosFacturacion.AjusteSencilloComprobante(datos).$promise.then(function(respuesta){
					bcsComunes.ventanaOverlay(idVentana, false);
					  
					if (respuesta.P_OUT_ERRORESCONTADOR == 0){
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);

					} else{
						if (respuesta.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, idVentana);
							
						} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant(""),
								conflictos:util.parseServerResponse(respuesta)
							}, idVentana);
						}
					}
					bcsComunes.ventanaCerrar(idVentana);
				},function(respuesta){
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.msg("error","Error",'UI_ERROR_MODIFICACION');
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				});
		 
		 
	 };

    $scope.currentTab = null;
    $scope.tabClick = function (val){
        console.log(val)
        if(val !== $scope.currentTab){
            $scope.currentTab = val;
            if( val === 'gridModificarMovimientoComprobanteGeneral'){
                $('div#Win_ModificarMovimientoModal').css("height", "546px");
            }
            if(val === 'gridModificarMovimientoComprobanteCobro'){
                $('div#Win_ModificarMovimientoModal').css("height", "609px");
            }
            if(val === 'gridModificarMovimientoComprobanteFormaDePago'){
                $('div#Win_ModificarMovimientoModal').css("height", "527px");
            }
        }
    }

});
