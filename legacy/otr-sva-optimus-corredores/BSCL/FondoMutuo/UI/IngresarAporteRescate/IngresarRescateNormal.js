angular.module('ModuloFFM').controller('IngresarRescateNormalFFMCtrl',function($rootScope,$scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateNormal = this;   
	$scope.data = {};
	$scope.data.objFecha= {};
	$scope.data.objCaja = {};
	
	$scope.rescateNormal.formData = {};
	$scope.rescateNormal.masInformacion = false;
	$scope.rescateNormal.formData.setInstrumento = "-1";
	$scope.rescateNormal.datosFondo = {};
	$scope.rescateNormal.formData.btnSimulacion=false;
	$scope.rescateNormal.formData.IngresoConfirmacion = 'I';
	$scope.rescateNormal.formData.AporteRescate = 'A';
	$scope.rescateNormal.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateNormal.formData.DatosAdicionales = "N";
	$scope.rescateNormal.formData.visibleTruco = true;
	$scope.rescateNormal.formData.disabledMlt = true;
	rescateNormal.confGlobal = $scope.aporteRescateMain.confGlobal;
	rescateNormal.oSocioNegocio = {};
	$scope.dataTemplate = {};
	rescateNormal.datosFondoDefecto = {};
	$scope.rescateNormal.oFormaPago = {};
	$scope.rescateNormal.formData.idAutorizadoDarOrdenesDefecto = undefined;
	$scope.rescateNormal.formData.porcentajeOptions = {min:1,max:100};
	$scope.rescateNormal.formData.IdMonedaSistema=$rootScope.SesionIdMonedaSistema;
	

	$scope.ConfiguracionFM = {};
	$scope.ConsultarParametroFondoMutuo = bcsConfiguracionFFM.getDataSourceConsultarParametroFondoMutuo();
	
	$scope.rescateNormal.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateNormal.ajustaVentana();
	
	$scope.rescateNormal.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
	
	
	
	$scope.rescateNormal.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
	};
	
	$scope.rescateNormal.formData.FiltrosComboFondo = {
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }	
	};
	
	$scope.rescateNormal.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}
			
	};
	
	$scope.rescateNormal.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.rescateNormal.formData.FiltrosCliente = undefined;
	
	$scope.rescateNormal.formData.filtrosFormaPago = {
			IdPortafolio : { "field" : "IdPortafolio","operator": "eq", "value"   :  -1},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
			
	$scope.rescateNormal.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  rescateNormal.oSocioNegocio.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.rescateNormal.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente    : { "field": "IdCliente","operator": "eq", "value" : 0 },
		 	 IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value" : 0 }
		 },
	 };
	
	
	$scope.filIdPortafolio	= { "field" : "IdPortafolio","operator": "eq", "value"   : 0 };
	$scope.filMonUSD	= { "field"   : "IdMoneda","operator": "eq", "value" : "USD"  };
	$scope.filMonCLP	= { "field"   : "IdMoneda","operator": "eq", "value" : "CLP"};	
	
	$scope.rescateNormal.formData.filtroMonedaPago = {
			logic : "or",
			IdMoneda: $scope.filMonUSD,
			IdMoneda1: $scope.filMonCLP
	};
	
	$scope.rescateNormal.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}

	
	 $scope.rescateNormal.formData.masDatos = function()
	 {
		 bcsComunes.ventanaAutoHeight($scope.winID);
		 if ($scope.rescateNormal.formData.DatosAdicionales === 'S'){
			 $scope.rescateNormal.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.rescateNormal.formData.DatosAdicionales = 'S';
	     }
	 }
	 
	 $scope.rescateNormal.configuracionFMM = function(){
		 var filters= [ { field: "NombreTipoParametro",operator: "eq", value: 'RESCATE_GENERA_VENTA_SPOT_DOLAR'},
			 					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			 					{ field: "IdEstado",		operator: "eq", value: 'VIG' } ];
		  
		 	$scope.ConsultarParametroFondoMutuo.query({ filter: filters }).then(function(){
		 		if($scope.ConsultarParametroFondoMutuo.ds._data.length > 0) {
		 			$scope.ConfiguracionFM.RESCATE_GENERA_VENTA_SPOT_DOLAR= $scope.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro;
		 		}else{$scope.ConfiguracionFM.RESCATE_GENERA_VENTA_SPOT_DOLAR='N';};
		 	});
	 };
	
	 $scope.rescateNormal.formData.ingresarAporteRescate = function()
     {	
		 var IdMonedaPago='';
		 if ($scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO ==='S'){IdMonedaPago=$scope.rescateNormal.formData.MonedaPagoIdActual? $scope.rescateNormal.formData.MonedaPagoIdActual : ''};
		 
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesAporteRescate : [{
					    IdInterno		      		:	1,
						IdCliente 	          		:	$scope.rescateNormal.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateNormal.oPortafolio.seleccionado.IdPortafolio,
				    	TipoSolicitud         		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate,
				    	IdSerieFondo          		:	$scope.rescateNormal.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	$scope.rescateNormal.formData.TipoRescate,
				    	EsPorMontoLiquido			:	'N',
				    	Monto 				  		:   $scope.rescateNormal.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateNormal.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.rescateNormal.formData.IdMonedaRescate,
				    	IdMedioDePago				:	$scope.rescateNormal.formData.IdMedioDePago ? $scope.rescateNormal.formData.IdMedioDePago : 'CIN',
				    	IdPreferenciaPagoPortafolio :   $scope.rescateNormal.oFormaPago.seleccionado ? $scope.rescateNormal.oFormaPago.seleccionado.IdPreferenciaPagoPortafolio : 0,
				    	Visado						:	'N',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	//FechaOperacion				:	util.dateSPdate($scope.rescateNormal.formData.fechaRescate),
				    	FechaOperacion				:	util.dateSPdate($scope.rescateNormal.formData.fechaRescate? $scope.rescateNormal.formData.fechaRescate:$scope.rescateNormal.datosFondo.FechaFondo),
				    	
				    	// Ahora en los rescates totales tambien se especifica regimen tributario
				    	IdRegimenTributario			:	$scope.rescateNormal.formData.IdRegimenFondoMutuo || '',	
				    			
				    	Glosa						: 	$scope.rescateNormal.formData.comentario,
				    	IndicadorMLT				:	$scope.rescateNormal.formData.esMLT ? 'S' : 'N',
				    	IdSerieFondoDestino			:	$scope.rescateNormal.formData.IdSerieFondoDestino || '',
				    	IdEmisorDestino				:	$scope.rescateNormal.formData.IdEmisorDestino || '',
				    	IdAutorizadoOrigen			:	$scope.rescateNormal.formData.idAutorizadoDarOrdenes || 0,
				    	Porcentaje					:	$scope.rescateNormal.formData.porcentaje || 0,
				    	FolioContrato				:	0,
				    	IdMonedaPago				:	IdMonedaPago
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudAporteRescateNormal(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateNormal.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaEmision  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						EsPorMontoLiquido        	 	: (respuesta.P_OUT_RESULTADO[0].EsPorMontoLiquido ==='S')? true : false,
						Monto        	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateNormal.formData.decimalesCantidad),
						TipoRescate                     : $scope.rescateNormal.formData.TipoRescateSeleccionado.NombreConcepto,
						MontoComisiones                 : respuesta.P_OUT_RESULTADO[0].ComisionLocal,
						SaldoCuotas                     : '',
						SaldoMonto                      : '',
						DocumentoIdentidad              : respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_RESULTADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_RESULTADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_RESULTADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotaTransferencia, $scope.formData.decimalesCantidad),
						IdEmisorOrigen                  : respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_RESULTADO[0].IdSerieFondoDestino,
						IdEmisorDestino                 : respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_RESULTADO[0].IndicadorMLT,
						MontoLiquido                    : respuesta.P_OUT_RESULTADO[0].MontoLiquidoLocal,
						Porcentaje                      : respuesta.P_OUT_RESULTADO[0].Porcentaje
	     		};
	     		$scope.rescateNormal.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateNormal.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		if(id){
	     			var promesa=bcsComunes.ventanaPromesa(id); 
		     		$.when(promesa).then(function(data){  
		     			$scope.solicitarDeclaracion(respuesta);
		     			bcsComunes.ventanaOverlay($scope.winID,false);
		            });
	     		}else{
	     			bcsComunes.ventanaOverlay($scope.winID,false);
	     		}
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
   		 },function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
   			bcsComunes.ventanaOverlay($scope.winID,false);
   		 });
     }
	 
$scope.rescateNormal.formData.simularAporteRescate = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var IdMonedaPago='';
		 if ($scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO ==='S'){IdMonedaPago=$scope.rescateNormal.formData.MonedaPagoIdActual? $scope.rescateNormal.formData.MonedaPagoIdActual : ''};
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, 
				 p_IdUsuario : $scope.SesionIdUsuario, 
				 p_IdModulo : 'FMM',
				 p_UsaReglasNegocio : 'S',
				 p_SimularOperacion	: 'S',
				 
				 
				 p_SolicitudesAporteRescate : [{
					    IdInterno		      		:	1,
						IdCliente 	          		:	$scope.rescateNormal.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateNormal.oPortafolio.seleccionado.IdPortafolio,
				    	TipoSolicitud         		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate,
				    	IdSerieFondo          		:	$scope.rescateNormal.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	$scope.rescateNormal.formData.TipoRescate,
				    	EsPorMontoLiquido			:	'N',
				    	Monto 				  		:   $scope.rescateNormal.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateNormal.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.rescateNormal.formData.IdMonedaRescate,
				    	IdMedioDePago				:	$scope.rescateNormal.formData.IdMedioDePago ? $scope.rescateNormal.formData.IdMedioDePago : 'CIN',
				    	IdPreferenciaPagoPortafolio :   $scope.rescateNormal.oFormaPago.seleccionado ? $scope.rescateNormal.oFormaPago.seleccionado.IdPreferenciaPagoPortafolio : 0,
				    	Visado						:	'N',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	FechaOperacion				:	util.dateSPdate($scope.rescateNormal.formData.fechaRescate? $scope.rescateNormal.formData.fechaRescate:$scope.rescateNormal.datosFondo.FechaFondo),
				    	
				    	// Ahora en los rescates totales tambien se especifica regimen tributario
				    	IdRegimenTributario			:	$scope.rescateNormal.formData.IdRegimenFondoMutuo || '',	
				    			
				    	Glosa						: 	$scope.rescateNormal.formData.comentario,
				    	IndicadorMLT				:	$scope.rescateNormal.formData.esMLT ? 'S' : 'N',
				    	IdSerieFondoDestino			:	$scope.rescateNormal.formData.IdSerieFondoDestino || '',
				    	IdEmisorDestino				:	$scope.rescateNormal.formData.IdEmisorDestino || '',
				    	IdAutorizadoOrigen			:	$scope.rescateNormal.formData.idAutorizadoDarOrdenes || 0,
				    	Porcentaje					:	$scope.rescateNormal.formData.porcentaje || 0,
				    	FolioContrato				:	0,
				    	IdMonedaPago				:	IdMonedaPago
				 }]
		 }
		 
	
		 
		 
	     bcsServicioFondosMutuos.SimulaCalculoSpreadRescate(datosIngresoAporteRescate).$promise.then(function(respuesta){
	    	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined ){
	     		$scope.rescateNormal.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaEmision  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						EsPorMontoLiquido        	 	: (respuesta.P_OUT_RESULTADO[0].EsPorMontoLiquido ==='S')? true : false,
						Monto        	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateNormal.formData.decimalesCantidad),
						TipoRescate                     : $scope.rescateNormal.formData.TipoRescateSeleccionado.NombreConcepto,
						MontoComisiones                 : respuesta.P_OUT_RESULTADO[0].ComisionLocal,
						SaldoCuotas                     : '',
						SaldoMonto                      : '',
						DocumentoIdentidad              : respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_RESULTADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_RESULTADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_RESULTADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.formData.decimalesCantidad),
						IdEmisorOrigen                  : respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_RESULTADO[0].IdSerieFondoDestino,
						IdEmisorDestino                 : respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_RESULTADO[0].IndicadorMLT,
						MontoLiquido                    : respuesta.P_OUT_RESULTADO[0].MontoLiquidoLocal,
						Porcentaje                      : respuesta.P_OUT_RESULTADO[0].Porcentaje,
						Spread                      	: respuesta.P_OUT_RESULTADO[0].Spread
	     		};
	     		$scope.rescateNormal.formData.utilidadPerdida=$scope.rescateNormal.formData.ResultadoSolicitudAporteRescate.Spread;
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     		
	     	 }else if(respuesta.P_OUT_MENSAJES[0] !== undefined){
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_SIMULACION"),conflictos:util.parseServerResponse(respuesta.P_OUT_MENSAJES)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
	     	 else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_SIMULACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
	     	 
   		 },function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_SIMULACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
   			bcsComunes.ventanaOverlay($scope.winID,false);
   		 });
		 	 
		
}
	 
	 $scope.$watch('rescateNormal.oPortafolio.idActual',function(newval,oldval){
		 if(newval && newval > 0){
			 var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'FMM',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoProducto	: 'INFM',
					p_IdTipoOperacion	: 'RESCFM',
					P_IdPortafolio		: newval
			 }
			 
			 bcsServicioFondosMutuos.ConsultarRequisitosBasicosCrearSolicitudAporteRescate(datos).$promise.then(function(respuesta){
				 if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					 $scope.rescateNormal.oPortafolio.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateNormal.datosFondo',function(newval,oldval){
		 if ( newval && newval.IdSerieFondo !=oldval.IdSerieFondo && newval.IdFondo) {
			 	$scope.rescateNormal.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
			 	$scope.rescateNormal.formData.btnSimulacion=false;$scope.rescateNormal.formData.utilidadPerdida='';
				$scope.rescateNormal.formData.decimalesCantidad = newval.DecimalesCuota;
				if(newval.RescateConMandato == 'N'){
					$scope.rescateNormal.formData.disabledMlt = true;
					$scope.rescateNormal.formData.esMLT = false;
				}else{
					$scope.rescateNormal.formData.disabledMlt = false;
				}
				 $scope.rescateNormal.formData.btnSimulacion=false;$scope.rescateNormal.formData.utilidadPerdida='';

				$scope.rescateNormal.formData.IdMonedaRescate = newval.IdMoneda;
				
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.rescateNormal.formData.numeroCuotas = 0;
				$scope.rescateNormal.formData.montoInicial = 0;
				$scope.rescateNormal.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.rescateNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateNormal.formData.esMLT',function(newval,oldval){
		 $scope.rescateNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateNormal.formData.IdMonedaRescate', function(newval,oldaval){
		 $scope.rescateNormal.formData.btnSimulacion=false;$scope.rescateNormal.formData.utilidadPerdida='';
		 if(newval){
			 $scope.rescateNormal.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.rescateNormal.formData.decimalesMonto = 0;
		 }
		 
		 
		 
			
		 if ($scope.ConfiguracionFM.RESCATE_GENERA_VENTA_SPOT_DOLAR=="S" && $scope.rescateNormal.datosFondo.IdMoneda=="USD" && $scope.rescateNormal.formData.IdMonedaRescate=="USD")
		 {
		    $scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO="S";
		   
		 }else{
		    	
			 $scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO="N";
		    	
		    
 		 };
		 
		 
		 
		 
		 $scope.rescateNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateNormal.formData.montoInicial',function(newval,oldval){
		 if (newval==null || newval==0){
			 $scope.rescateNormal.formData.btnSimulacion=false;
			 $scope.rescateNormal.formData.utilidadPerdida='';
		 }
		
		if(newval && newval != oldval){
				
			if(($scope.rescateNormal.formData.TipoRescate === 'M')){
				$scope.rescateNormal.formData.btnSimulacion=false;
				$scope.rescateNormal.formData.utilidadPerdida='';	
				bcsConfiguracionFFM.calcularMontoCuota($scope.rescateNormal.datosFondo,newval,$scope.rescateNormal.formData.numeroCuotas,'RES',$scope.rescateNormal.formData.IdMonedaRescate,$scope.rescateNormal.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateNormal.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateNormal.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 $scope.rescateNormal.formData.btnSimulacion=true;
					 }else{
						 $scope.rescateNormal.formData.numeroCuotas = 0;
						 $scope.rescateNormal.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateNormal.formData.numeroCuotas = 0;
					 $scope.rescateNormal.formData.montoInicial = 0;
		  		 });
			}
		}
	 });
	 
	 $scope.$watch('rescateNormal.masInformacion',function(newval,oldval){
		 $scope.rescateNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateNormal.formData.TipoRescate',function(newval,oldval){
		 $scope.rescateNormal.formData.btnSimulacion=false;
		 $scope.rescateNormal.formData.utilidadPerdida='';
		 if(newval === 'T'){
			 $scope.rescateNormal.formData.btnSimulacion=true;


			 $scope.rescateNormal.formData.numeroCuotas = $scope.rescateNormal.formData.SaldoCuotas;
			 $scope.rescateNormal.formData.montoInicial = $scope.rescateNormal.formData.SaldoMonto;
			 	
			 
		 }else{
			 $scope.rescateNormal.formData.todosRegimen = 'N';
			 $scope.rescateNormal.formData.numeroCuotas = 0;
			 $scope.rescateNormal.formData.montoInicial = 0;
		 }
		 
		 $scope.rescateNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateNormal.formData.numeroCuotas',function(newval,oldval){
		 if (newval==null || newval==0){
			 $scope.rescateNormal.formData.btnSimulacion=false;
			 $scope.rescateNormal.formData.utilidadPerdida='';
		 };
		 
		 if(newval && newval != oldval){
			 	 
			 if($scope.rescateNormal.formData.TipoRescate === 'C' || $scope.rescateNormal.formData.TipoRescate === 'P' || $scope.rescateNormal.formData.TipoRescate === 'T'){
				 $scope.rescateNormal.formData.btnSimulacion=false;
				 $scope.rescateNormal.formData.utilidadPerdida='';	 
				 //bcsConfiguracionFFM.calcularMontoCuota($scope.rescateNormal.datosFondo,$scope.rescateNormal.formData.montoInicial,newval,'RES',$scope.rescateNormal.formData.IdMonedaRescate,$scope.rescateNormal.formData.TipoRescate,$scope.SesionFechaSistema)
				 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateNormal.datosFondo,$scope.rescateNormal.formData.montoInicial,newval,'RES',$scope.rescateNormal.formData.IdMonedaRescate,'C',$scope.SesionFechaSistema)
				 .then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateNormal.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateNormal.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 $scope.rescateNormal.formData.btnSimulacion=true;
					 }else{
						 $scope.rescateNormal.formData.numeroCuotas = 0;
						 $scope.rescateNormal.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateNormal.formData.numeroCuotas = 0;
					 $scope.rescateNormal.formData.montoInicial = 0;
		  		 });
			 }
		 }
	 });
	 
	 $scope.$watch('rescateNormal.formData.porcentaje',function(newval,oldval){
		 $scope.rescateNormal.formData.btnSimulacion=false;$scope.rescateNormal.formData.utilidadPerdida='';
		 if(newval && newval != oldval){
			 
			 $scope.rescateNormal.formData.numeroCuotas = ($scope.rescateNormal.formData.SaldoCuotas * newval) / 100;
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 $scope.rescateNormal.formData.btnSimulacion=false;$scope.rescateNormal.formData.utilidadPerdida='';
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateNormal.formData.montoInicial = undefined;
				 $scope.rescateNormal.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateNormal.formData.AporteRescate = 'A';
				$scope.rescateNormal.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateNormal.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateNormal.ajustaVentana();
		 }
		 
	 });

	 $scope.$watch('rescateNormal.formData.SaldoCuotas', function(newval,oldval){
	 	$scope.rescateNormal.formData.numeroCuotas = $scope.rescateNormal.formData.SaldoCuotas;
	 	$scope.rescateNormal.formData.montoInicial = $scope.rescateNormal.formData.SaldoMonto;
	 });	 


	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('rescateNormal.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateNormal.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateNormal.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateNormal.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.rescateNormal.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateNormal.formData.tipoOperacion = 0;
			 $scope.rescateNormal.formData.AporteRescate = 'A';
		 }
		 $scope.rescateNormal.formData.idAutorizadoDarOrdenes = undefined;
		 $scope.rescateNormal.formData.IngresoConfirmacion = 'I';
		 $scope.rescateNormal.formData.TipoRescate = 'M';
		 $scope.rescateNormal.oSocioNegocio.idActual = 0;
		 $scope.rescateNormal.oPortafolio.idActual = 0;
		 $scope.rescateNormal.formData.setFondo = '-1';
		 $scope.rescateNormal.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateNormal.formData.montoInicial = undefined;
		 $scope.rescateNormal.formData.numeroCuotas = undefined;
		 $scope.rescateNormal.formData.montoComisiones = undefined;
		 $scope.rescateNormal.formData.porcentaje = undefined;
		 $scope.rescateNormal.formData.IdFormaPago = '0';
		 $scope.rescateNormal.formData.comentario = '';
		 $scope.rescateNormal.formData.FiltrosBusquedaFondo = { 
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
			}
		 $scope.rescateNormal.ajustaVentana();
	 }
	 
	 
	 $scope.rescateNormal.formData.resetFoco =  function() {
	 	$scope.rescateNormal.formData.focoVolver = false;
		if($scope.rescateNormal.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateNormal.formData.focoOperacion = true;
			$scope.rescateNormal.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateNormal.formData.verComprobante = function() {
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'CompResc' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
				if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;

					var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
										"p_IdUsuario"				: $scope.SesionIdUsuario,
										"p_IdModulo"		: 'FMM',
										"p_IdSolicitudAporteRescate" : $scope.rescateNormal.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
		
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [{
														"type" : "pdf417",
														"name" : "pdf417",
														"data" :  bcsConfiguracionFFM.generarPdf417($scope.rescateNormal.formData.ResultadoSolicitudAporteRescate)
													}];
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 };
	 
	 $scope.rescateNormal.formData.verMandato = function(){
		 var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'CompMLT' },
             					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
             					{ field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		 bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
				if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;

					var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
										"p_IdUsuario"				: $scope.SesionIdUsuario,
										"p_IdModulo"		: 'FMM',
										"p_IdSolicitudAporteRescate" : $scope.rescateNormal.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
		
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [];
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 }
	 
	 $scope.rescateNormal.limpiarFMM();
	 $scope.rescateNormal.configuracionFMM();
	 
	 if (rescateNormal.confGlobal.IdCliente){
		 $scope.rescateNormal.oSocioNegocio.idDefecto = rescateNormal.confGlobal.IdSocioDeNegocio;
	     $scope.rescateNormal.oSocioNegocio.filtros.IdCliente.value = rescateNormal.confGlobal.IdCliente;
		 $scope.rescateNormal.oSocioNegocio.filtros.read = true;
		 
		 $scope.rescateNormal.oPortafolio.idDefecto	= rescateNormal.confGlobal.IdPortafolio;
		 $scope.rescateNormal.oPortafolio.filtros.IdPortafolio.value = rescateNormal.confGlobal.IdPortafolio;
		 $scope.rescateNormal.oPortafolio.filtros.read = true;
		 
		 rescateNormal.datosFondoDefecto.IdDefecto = rescateNormal.confGlobal.IdSerieFondo;
		 $scope.rescateNormal.formData.fechaRescate = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateNormal.formData.FiltrosCliente = {
		    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  rescateNormal.confGlobal.IdCliente},	
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  rescateNormal.confGlobal.IdPortafolio}	
		 }
	 }
	 
	 $scope.$watch('rescateNormal.oPortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.rescateNormal.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.rescateNormal.oSocioNegocio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.rescateNormal.oPortafolio.idActual}	
				 }
			}
		 });
	
	$scope.asignarRegimenPorDefecto = function () {
		if($scope.rescateNormal.oPortafolio.seleccionado && $scope.rescateNormal.oPortafolio.seleccionado.CodigoPortafolio.length > 0
			&& $scope.rescateNormal.datosFondo.IdSerieFondo && $scope.rescateNormal.datosFondo.IdSerieFondo.length > 0 ){
				$scope.rescateNormal.formData.IdRegimenFondoMutuo = $scope.rescateNormal.oPortafolio.seleccionado.CodigoPortafolio;
				$scope.rescateNormal.formData.RegimenDefecto = $scope.rescateNormal.formData.IdRegimenFondoMutuo;
		}
	};
	
	 $scope.solicitarDeclaracion= function(respuesta) {
			
		 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
		 						,IdUsuario			: $scope.SesionIdUsuario
		 						,IdModulo			: "FMM"
		 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
		 						,IdCliente			: $scope.rescateNormal.oPortafolio.seleccionado.IdCliente
		 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
								,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
								,IdMedioDePago		: $scope.rescateNormal.formData.IdMedioDePago ? $scope.rescateNormal.formData.IdMedioDePago : 'CIN'
								,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
							 };
			 
			 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
		 };
});