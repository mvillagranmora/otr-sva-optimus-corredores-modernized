angular.module('ModuloTesoreria').controller('EditarComprobanteFormaDePagoCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,
					bcsServiciosTesoreria,bcsBlotterComprobantesGrid)
{
	$scope.gridApi = {};
	$scope.data = {};
	var IdComprobanteFormaDePago = 0;           //Id temporal(solo para edición)
	var IdComprobanteFormaDePagoEditado = 0;  	//Id temporal(solo para edición)
	
	//Obtiene parametros traspasados desde la ventana de Ingresos/Egresos
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);

	$scope.objFormaDePago = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.data.showDatosOperacion 	= true;
	$scope.data.showDatosSocio 		= true;
	$scope.data.showBtnAgregar 		= true;
	$scope.data.showBtnEditar 		= false;	
	$scope.data.showBtnCancelar		= false;
	$scope.data.showBtnGrabar		= false;

	//Calcula Saldo por asignar.
	function ObtenerSaldoPorAsignar()
	{
		var saldoTotal = 0;
		if (typeof ds !== 'undefined'){
			//saldoTotal = $scope.objFormaDePago[0].Monto - ds.aggregates().Monto.sum;

			var arrDataFormaPago = [];
			arrDataFormaPago = ds.dataColumns(['Monto']);
			
			for (i=0;i<ds.data().length;i++){
				saldoTotal = saldoTotal + arrDataFormaPago[i].Monto;
			}
					
			$scope.data.saldoPorAsignar = $scope.objFormaDePago[0].Monto - saldoTotal;
		}
	};

	//------------------------------------------------
	// CONFIGURACION GRILLA FORMA DE PAGO
	//------------------------------------------------	
		
	//Redefine Data Source para filtro dinámico.
	var change = function(e){
		var dsLength = ds.data.length;
		ObtenerSaldoPorAsignar();

		switch (e.action){
			case "remove":
			case "add":
				$scope.data.showBtnGrabar = true;
				break;
			default:
				$scope.data.showBtnGrabar = false;
				break;
		}
	};
	var filters = [{ field: "IdComprobante", operator: "eq", value: $scope.objFormaDePago[0].IdComprobante },
				   { field: "IdEstado", operator: "neq", value: 'ANU' }];
	var ds = bcsBlotterComprobantesGrid.getDsComprobanteFormaDePago(change);

	//$scope.gridApi = {};
	$scope.confBasicaComprobanteFormaDePago = {
		dataSource			: ds,
		columns				: bcsBlotterComprobantesGrid.columnasComprobanteFormaDePago(),		
		sortable			: true,
		pageable			: false,		
		height				: 160,
		selectable 			: "row",
		userSettings		: true,
		userSettings		: true,
		dataBound			: function(e) {	ObtenerSaldoPorAsignar(); },
		gridCreated 		: function(g) { ds.filters(filters); $scope.gridApi = g; }
	};

	
	
	
	// --------------------------------------------------
	// FUNCIONES 
	// --------------------------------------------------

	//Cierra la ventana dado el ID
	$scope.data.cancelar = function()
	{
		bcsComunes.ventanaData($scope.IdVentana);
		bcsComunes.ventanaCerrar($scope.IdVentana);  
	};

	//Quita una forma de pago de la grilla(solo visualmente) 
	$scope.eliminarFormaPago = function(){	         
		if (typeof $scope.seleccionComprobanteFormaDePago === 'undefined'){	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_FORMA_DE_PAGO'));		
		}
		else {				
			ds.remove($scope.seleccionComprobanteFormaDePago);

			if ($scope.seleccionComprobanteFormaDePago.IdTipoMovimiento === 'MOVEGR' && 
				$scope.seleccionComprobanteFormaDePago.IdMedioDePago === 'CHE'){

				var formaDePago      = ds.data();
				for (i= 0 ; i < formaDePago.length ; i++){
					console.log(formaDePago[i].NumeroDocumentoCorredora);

					if (formaDePago[i].NumeroSerieCorredora === $scope.seleccionComprobanteFormaDePago.NumeroSerieCorredora &&
						formaDePago[i].NumeroDocumentoCorredora > $scope.seleccionComprobanteFormaDePago.NumeroDocumentoCorredora){
						formaDePago[i].NumeroDocumentoCorredora = formaDePago[i].NumeroDocumentoCorredora - 1;
						
						
						$scope.data.deleteFormaDePago = {IdCuentaBancariaCorredora 	: formaDePago[i].IdCuentaBancariaCorredora,
														 NumeroSerieCorredora 		: formaDePago[i].NumeroSerieCorredora,
														 NumeroDocumentoCorredora 	: formaDePago[i].NumeroDocumentoCorredora};

					}
				}
				if (formaDePago.length === 0){
					$scope.data.deleteFormaDePago = {IdCuentaBancariaCorredora 	: $scope.seleccionComprobanteFormaDePago.IdCuentaBancariaCorredora,
													 NumeroSerieCorredora 		: $scope.seleccionComprobanteFormaDePago.NumeroSerieCorredora,
													 NumeroDocumentoCorredora 	: $scope.seleccionComprobanteFormaDePago.NumeroDocumentoCorredora -1
													 };
				}
				
			}			
			$scope.gridApi.refresh();
			$scope.seleccionComprobanteFormaDePago = undefined;
			ObtenerSaldoPorAsignar();
		}
	}

	//Agrega una nueva forma de pago(un nuevo row a la grilla) (solo visualmente)
	$scope.agregarFormaPago = function()
	{
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPago(); // recupera la configuración desde la directiva
		
		if (typeof objFormaPago !== 'undefined'){	
			IdComprobanteFormaDePago  				= IdComprobanteFormaDePago  + 1;  //Id temporal(sólo para edición)
			objFormaPago.IdComprobanteFormaDePago 	= IdComprobanteFormaDePago; //Id temporal(para edición)

			ds.add(objFormaPago);
			ObtenerSaldoPorAsignar();
		}
	}

	$scope.EditarFormaDePago = function(datos) {		
		IdComprobanteFormaDePagoEditado	= datos.IdComprobanteFormaDePago;  //Id(temporal para edición) de la forma de pago editada.
		$scope.data.showBtnAgregar 	 	= false;
		$scope.data.showBtnEditar 	 	= true;
		$scope.data.showBtnCancelar	 	= true;
					
		$scope.data.formaDePagoAEditar	= datos; //Datos a enviar a directiva
	};

	$scope.cargarFormaDePagoEditada = function() {
		var objFormaPago = {};
		objFormaPago = $scope.data.configuracionFormaPago(); // recupera la configuración desde la directiva

		if (typeof objFormaPago !== 'undefined'){
			var formaDePago      = ds.data();
			for (i= 0 ; i < formaDePago.length ; i++){
				if (Number(IdComprobanteFormaDePagoEditado) === Number(formaDePago[i].IdComprobanteFormaDePago) ){
					formaDePago[i].IdComprobanteFormaDePago				= objFormaPago.IdComprobanteFormaDePago;
					formaDePago[i].IdCaja								= objFormaPago.IdCaja;
					formaDePago[i].CodigoCaja							= objFormaPago.CodigoCaja;
					formaDePago[i].NombreCaja							= objFormaPago.NombreCaja;


					formaDePago[i].IdMedioDePago						= objFormaPago.IdMedioDePago;
					formaDePago[i].NombreMedioDePago					= objFormaPago.NombreMedioDePago;
					formaDePago[i].TipoMedioDePago  					= objFormaPago.TipoMedioDePago;
					formaDePago[i].IdModalidadDePago					= objFormaPago.IdModalidadDePago;
					formaDePago[i].NombreModalidadDePago				= objFormaPago.NombreModalidadDePago;
					formaDePago[i].IdMoneda								= objFormaPago.IdMoneda;
					formaDePago[i].Monto								= objFormaPago.Monto;
					formaDePago[i].IdComprobante						= objFormaPago.IdComprobante;
					formaDePago[i].IdTipoMovimiento						= objFormaPago.IdTipoMovimiento;
					formaDePago[i].IdComprobantePartida              	= objFormaPago.IdComprobantePartida;
					formaDePago[i].IdComprobanteFormaDePago				= objFormaPago.IdComprobanteFormaDePago;

					//CORREDORA
					formaDePago[i].IdCuentaBancariaCorredora 			= objFormaPago.IdCuentaBancariaCorredora;
					formaDePago[i].IdTipoCuentaBancariaCorredora 		= objFormaPago.IdTipoCuentaBancariaCorredora;
					formaDePago[i].NombreTipoCuentaBancariaCorredora	= objFormaPago.NombreTipoCuentaBancariaCorredora;
					formaDePago[i].NombreBancoCorredora					= objFormaPago.NombreBancoCorredora;
					formaDePago[i].NombreCuentaBancariaCorredora		= objFormaPago.NombreCuentaBancariaCorredora;
					formaDePago[i].NumeroCuentaBancariaCorredora		= objFormaPago.NumeroCuentaBancariaCorredora;
					formaDePago[i].IdChequera 			            	= objFormaPago.IdChequera;
					formaDePago[i].NumeroDocumentoCorredora				= objFormaPago.NumeroDocumentoCorredora;
					formaDePago[i].NumeroSerieCorredora					= objFormaPago.NumeroSerieCorredora;
					formaDePago[i].IdTarjetaBancariaCorredora 			= objFormaPago.IdTarjetaBancariaCorredora;
					formaDePago[i].FechaDocumentoPagoCorredora 			= objFormaPago.FechaDocumentoPagoCorredora;

					//SOCIO NEGOCIO
					formaDePago[i].NumeroCuentaBancariaSocio			= objFormaPago.NumeroCuentaBancariaSocio;
					formaDePago[i].IdCuentaBancariaSocio				= objFormaPago.IdCuentaBancariaSocio;
					formaDePago[i].IdBancoSocio			    			= objFormaPago.IdBancoSocio;
					formaDePago[i].NombreBancoSocio 					= objFormaPago.NombreBancoSocio;
					formaDePago[i].FechaDocumentoPagoSocio 				= objFormaPago.FechaDocumentoPagoSocio;
					formaDePago[i].NumeroDocumentoSocio 				= objFormaPago.NumeroDocumentoSocio;
					formaDePago[i].NumeroSerieSocio 					= objFormaPago.NumeroSerieSocio;
					formaDePago[i].IdCuentaBancariaSocio 				= objFormaPago.IdCuentaBancariaSocio;
					formaDePago[i].IdTipoCuentaBancariaSocio 			= objFormaPago.IdTipoCuentaBancariaSocio;
					formaDePago[i].NombreTipoCuentaBancariaSocio 		= objFormaPago.NombreTipoCuentaBancariaSocio;
					formaDePago[i].NombreCuentaBancariaSocio			= objFormaPago.NombreCuentaBancariaSocio;
					formaDePago[i].IdTarjetaBancariaSocio 		    	= objFormaPago.IdTarjetaBancariaSocio;
					formaDePago[i].IdTipoTarjetaBancariaSocio 			= objFormaPago.IdTipoTarjetaBancariaSocio;
					formaDePago[i].NombreTipoTarjetaBancariaSocio 		= objFormaPago.NombreTipoTarjetaBancariaSocio;
					formaDePago[i].NumeroTarjetaBancariaSocio 			= objFormaPago.NumeroTarjetaBancariaSocio;
				}
			};
		};

		//refresca la grilla con los datos editados		
		$scope.gridApi.refresh();
		ObtenerSaldoPorAsignar();
		$scope.data.showBtnGrabar = true;
		$scope.data.showBtnAgregar 	= true;
		$scope.data.showBtnEditar  	= false;
		$scope.data.showBtnCancelar = false;
	};

	$scope.cancelarEdicionFormaDePago = function(){
		$scope.data.showBtnAgregar 			= true;
		$scope.data.showBtnEditar 			= false;
		$scope.data.showBtnCancelar			= false;
		$scope.data.limpiarFormaPago();		//esta en la directiva
	};

	// -----------------------------------------------------
	// SERVICIO CREAR FORMA DE PAGO COMPROBANTE   
	// -----------------------------------------------------
	var successGestionFormaDePago = function(respuesta)
	{
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == "0"){
			if (respuesta.P_OUT_ADVERTENCIASCONTADOR !== undefined && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0"){
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_EDITAR_FORMA_PAGO_EXITO"),
					conflictos : util.parseServerResponse(respuesta)
					},$scope.IdVentana);
			}					
			else{
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
					conflictos : util.parseServerResponse(respuesta)
					},$scope.IdVentana);
			}				
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		}
		else {
			bcsComunes.ventanaMsg({
				mensaje : $translate.instant("UI_EDITAR_FORMA_PAGO_ERROR"),
				conflictos : util.parseServerResponse(respuesta)
				},$scope.IdVentana);
			
		}
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		$scope.data.cancelar();
	};
	var errorGestionFormaDePago = function(respuesta)
	{
		bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_EDITAR_FORMA_PAGO_ERROR"),
					conflictos : util.parseServerResponse(respuesta)
					},$scope.IdVentana);
		bcsComunes.ventanaOverlay($scope.IdVentana, false);
		
	};


	$scope.data.crearComprobanteFormaDePago = function()
	{
		//Rescata valores ingresados en formulario
		var parametros = {};
		bcsComunes.ventanaOverlay($scope.IdVentana, true);


		//Obtiene forma de pago desde la grilla
		var arrDataFormaPago = new Array();			
		arrDataFormaPago = ds.dataColumns(['IdComprobante','IdComprobanteFormaDePago','IdTipoMovimiento','IdMedioDePago','IdModalidadDePago',
										   'IdMoneda','Monto','IdCuentaBancariaCorredora','IdTarjetaBancariaCorredora','IdChequera',
										   'NumeroDocumentoCorredora','FechaDocumentoPagoCorredora','IdCuentaBancariaSocio','IdBancoSocio',
										   'NombreCuentaBancariaSocio','NumeroCuentaBancariaSocio','IdTipoCuentaBancariaSocio',
										   'NombreTipoCuentaBancariaSocio','IdTarjetaBancariaSocio','IdTipoTarjetaBancariaSocio',
										   'NombreTipoTarjetaBancariaSocio','NumeroTarjetaBancariaSocio','NumeroSerieSocio','NumeroDocumentoSocio',
										   'FechaDocumentoPagoSocio','CertificadoAutomatico']);

		//Esto se debe borrar cuando se solucione el problema de formato de la grilla
		if (arrDataFormaPago !== undefined)
		{
			$.each(arrDataFormaPago, function(key,value){
				arrDataFormaPago[key].CertificadoAutomatico = $scope.data.CertificadoAutomatico ? 'S':'N';			 
			});
		}

		parametros.p_IdEmpresa              = $scope.SesionIdEmpresa;
		parametros.p_IdUsuario              = $scope.SesionIdUsuario;
		parametros.p_IdModulo               = 'TES';
		parametros.p_simularOperacion       = 'N';
		parametros.p_UsaReglasNegocio       = 'S';	
		parametros.p_FechaMovimiento        = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		parametros.p_TipoEjecucion			= 'ING';
		parametros.p_FormasDePago 			= arrDataFormaPago || [];						 


		bcsServiciosTesoreria.GestionFormaDePago(parametros,successGestionFormaDePago,errorGestionFormaDePago);
	};
});
