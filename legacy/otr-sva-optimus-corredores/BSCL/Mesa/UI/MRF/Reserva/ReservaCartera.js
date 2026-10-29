angular.module('ModuloMesa').controller('ReservaCarteraCtrl',
		function($scope
				,$state
				,$timeout
				,$modal
				,$translate
				,bcsComunes 
				,bcsServiciosMesa
				,bcsAlerta 
				) {


	$scope.data = {};
	$scope.grid = {};
	$scope.filtros = {};
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	$scope.objCartera = bcsComunes.ventanaParams($scope.IdVentana);
	
	// Variables para la ventana
	$scope.v_Concepto = $scope.objCartera["Tipo"];
	$scope.Registro = $scope.objCartera["Registro"];
	$scope.data.frmNemo = $scope.Registro[0].Nemotecnico;
	$scope.data.frmFolioCartera = $scope.Registro[0].FolioCartera;
	//$scope.data.frmIdReservaCus = $scope.Registro[0].IdReservaCus;
	$scope.data.frmEmisor = $scope.Registro[0].IdEmisor;
	$scope.data.frmFechaVencimiento = $scope.Registro[0].FechaVencimientoInstrumento;
	$scope.data.frmMercado = $scope.Registro[0].IdClasificacionTipoInstrumento;
	$scope.data.frmTipoReajuste = $scope.Registro[0].IdTipoReajuste;
	$scope.data.frmCantidadTotal = $scope.Registro[0].Cantidad;
	$scope.data.frmCantidadTotalReservada = $scope.Registro[0].CantidadReservadaCus;
	//$scope.data.frmCantidadReservada = $scope.Registro[0].CantidadReservada;
	$scope.data.frmCorteMinimo = $scope.Registro[0].CorteMinimo;
	
	
	// Configuracion de textos
	if ( $scope.v_Concepto == 'RES' )	{
		$scope.data.frmBoton = 'UI_RESERVAR'
		$scope.data.frmCantidadSP = 'UI_CANTIDAD_A_RESERVAR'
		$scope.data.frmComentario = 'UI_PARA_RESERVAR'			
		$scope.data.frmCantidadTotalLibre = $scope.Registro[0].Cantidad;// - $scope.Registro[0].CantidadReservadaMes;
	} else {
		$scope.data.frmBoton = 'UI_LIBERAR'
		$scope.data.frmCantidadSP = 'UI_CANTIDAD_A_LIBERAR'
		$scope.data.frmComentario = 'UI_PARA_LIBERAR'			
		$scope.data.frmCantidadTotalLibre = $scope.Registro[0].CantidadReservadaCus;
	}
	
	var filtersCartera = {};

	
	// PROCESOS COMUNES 
	var success = function(respuesta)
	{
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
		{		
			var id = bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),//+": " +$translate.instant("RESERVA")+" " + respuesta.p_out_IdReserva[0].IdReserva,
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);

			// Cierra ventana
			bcsComunes.ventanaPromesa(id).then(function(result){
						bcsComunes.ventanaCerrar($scope.IdVentana);
					});
		}
		else
		{	
			bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);			
		}
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	var error = function(respuesta)
	{		
		bcsComunes.ventanaMsg({
						//mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
		bcsComunes.ventanaOverlay($scope.winID, false);
	};
	
	

	//Cierra la ventana dado el ID
	$scope.Cancelar = function()
	{
		var data = {boton:'CANCELAR', data: {}};
		bcsComunes.ventanaData($scope.IdVentana,data);
		bcsComunes.ventanaCerrar($scope.IdVentana);
	};
	
	
	//Cierra la ventana dado el ID
	$scope.Reservar = function()
	{
		
        // Parametros del servicio
		var param = {};
		var v_IdConcepto = '';
		var v_FechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		

		// Validaciones
		if ( $scope.data.frmCantidadTotalLibre < $scope.data.frmCantidadReserva ) {
			if ( $scope.v_Concepto == 'RES' )	{
				bcsComunes.msg("warning","info",$translate.instant('UI_CANTIDAD_SUPERIOR_AL_DISPONIBLE'));
			} else {
				bcsComunes.msg("warning","info",$translate.instant('UI_CANTIDAD_SUPERIOR_A_LA_RESERVA'));
			}
		} else {
		
			if ( $scope.v_Concepto == 'RES' )	{
				$scope.v_IdConcepto	= 'INGRES';
			}	else	{
				$scope.v_IdConcepto	= 'RETRES';  
			}
			
			
			// Obligatorios
			param.p_IdEmpresa           = $scope.SesionIdEmpresa;
			param.p_IdUsuario           = $scope.SesionIdUsuario;
			param.p_IdModulo            = 'CUS';
			param.p_UsaReglasNegocio    = 'N';
			param.p_simularOperacion    = 'N';
			//param.p_FechaMovimiento     = $scope.v_FechaSistema;
			param.p_IdPais				= $scope.SesionIdPaisSistema;
	
			// Reserva
			param.p_FechaOperacion		= $scope.SesionFechaSistema;
			param.p_TblCarteraReserva   =  [{
											"IdOperacion" : 0	// Para la reserva es cero
											,"IdRegistro" : 1
											,"IdReserva" : Number($scope.objCartera.Registro[0].IdReserva) || 0
											,"IdConcepto" : $scope.objCartera.Registro[0].IdConcepto || $scope.v_IdConcepto
											,"FolioOperacion" : $scope.objCartera.Registro[0].FolioOperacion || 0
											,"FolioCartera"	: $scope.objCartera.Registro[0].FolioCartera || 0
											,"FolioCarteraCustodia" : $scope.objCartera.Registro[0].FolioCarteraCustodia || 0
											,"IdTipoProducto" : $scope.objCartera.Registro[0].IdTipoProducto || 'CUST'
											,"Cantidad" : $scope.objCartera.Registro[0].IdClasificacionTipoInstrumento === 'RF' ? Number($scope.data.frmCantidadReserva) : 0
											,"Tasa" : $scope.objCartera.Registro[0].Tasa || 0
											,"TasaMercado" : $scope.objCartera.Registro[0].TasaMercado || 0
											,"Precio" : $scope.objCartera.Registro[0].Precio || 0
											,"PrecioMercado" : $scope.objCartera.Registro[0].PrecioMercado || 0
											,"ValorRescate" : $scope.objCartera.Registro[0].IdClasificacionTipoInstrumento === 'IF' ? Number($scope.data.frmCantidadReserva) : 0
											,"MontoValorizado" : $scope.objCartera.Registro[0].MontoValorizado || 0
											,"IdClasificacionTipoInstrumento" : $scope.objCartera.Registro[0].IdClasificacionTipoInstrumento || ''
											,"IdTipoInstrumento" : $scope.objCartera.Registro[0].IdTipoInstrumento || ''
											,"IdInstrumento" : $scope.objCartera.Registro[0].IdInstrumento || 0
											,"IdTipoReajuste" : $scope.objCartera.Registro[0].IdTipoReajuste || ''
											,"IdEmisor" : $scope.objCartera.Registro[0].IdEmisor || ''
											,"CorteMinimo" : Number($scope.data.frmCorteMinimo) || 0
											,"FechaVencimiento" : util.dateSPdate($scope.objCartera.Registro[0].FechaVencimientoInstrumento) || ''
											,"EsFungible": $scope.objCartera.Registro[0].EsFungible || ''
											,"Glosa" : $scope.objCartera.Registro[0].Glosa || ''
											,"IdBolsa" : $scope.objCartera.Registro[0].IdBolsa || ''
											,"IdBoveda" : $scope.objCartera.Registro[0].IdBoveda || ''	
											,"CodigoIFRS" : $scope.objCartera.Registro[0].CodigoIFRS || ''
			}];
			
											
	       	bcsServiciosMesa.IngresarReservaCartera(param,success,error);
	       	console.log(JSON.stringify(param));
		}
	};

});


