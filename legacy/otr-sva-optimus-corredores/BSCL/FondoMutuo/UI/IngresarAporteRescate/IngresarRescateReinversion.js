angular.module('ModuloFFM').controller('IngresarRescateReinversionFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateReinversion = this;             
	$scope.rescateReinversion.formData = {};
	$scope.rescateReinversion.masInformacion = false;
	$scope.rescateReinversion.formData.setInstrumento = "-1";
	$scope.rescateReinversion.datosFondo = {};
	$scope.rescateReinversion.formData.btnSimulacion=false;
	$scope.rescateReinversion.formData.IngresoConfirmacion = 'I';
	$scope.rescateReinversion.formData.AporteRescate = 'A';
	$scope.rescateReinversion.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateReinversion.formData.DatosAdicionales = "N";
	$scope.rescateReinversion.formData.visibleTruco = true;
	$scope.rescateReinversion.formData.disabledMlt = false;
	rescateReinversion.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.dataTemplate = {};
	$scope.rescateReinversion.formData.idAutorizadoDarOrdenesDefecto=undefined;
	$scope.rescateReinversion.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateReinversion.ajustaVentana();
	$scope.rescateReinversion.formData.oConfiguracionTipoRescate={
			tipo : "combobox-tipo-rescate-ffmm",
			configuracion : {},
			filtros : {
				Estado : { "field"   : "Estado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.rescateReinversion.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
		
	$scope.rescateReinversion.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
	};
	
	$scope.rescateReinversion.formData.FiltrosBusquedaAporteFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdMoneda : {"field" : "IdMoneda", "operator": "eq", "value" : $scope.rescateReinversion.datosFondo.IdMoneda},
			IdFamiliaFondo : {"field" : "IdFamiliaFondo", "operator": "eq", "value": undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.rescateReinversion.formData.FiltrosComboFondo = {
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }	
	};
	
	$scope.rescateReinversion.formData.FiltrosComboFondoAporte = {
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoRescate : { field: "BloqueoAporte", operator: "eq", value:'N' }	
	};
	
	
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.rescateReinversion.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}
	};
	
	$scope.rescateReinversion.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.rescateReinversion.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
	/*			
	$scope.rescateReinversion.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.rescateReinversion.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };

	*/

	$scope.rescateReinversion.oClientePortafolio ={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};	
//Fin GCMBIPAPAS-1		
	
	
	$scope.rescateReinversion.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	};
	
	$scope.rescateReinversion.formData.filtrosRegimenRescate = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	};
	 
	 $scope.rescateReinversion.formData.ingresarAporteRescate = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesRescate : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
					    //IdCliente 	          		:	$scope.rescateReinversion.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.rescateReinversion.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.rescateReinversion.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateReinversion.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1	
				    	IdSerieFondo          		:	$scope.rescateReinversion.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	$scope.rescateReinversion.formData.TipoRescate,
				    	
				    
				    	// Ahora en los rescates totales tambien se especifica regimen tributario
				    	IdRegimenTributario			:	$scope.rescateReinversion.formData.IdRegimenFondoMutuoRescate || '',
				    	
				    	
				    	Monto 				  		:   $scope.rescateReinversion.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateReinversion.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.rescateReinversion.formData.IdMonedaRescate,
				    	IdMedioDePago				:	'CIN',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.rescateReinversion.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.rescateReinversion.formData.idAutorizadoDarOrdenes || 0,
				    	FolioContrato				:	0
				 }],
				 
				 p_SolicitudesAporte : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
					    //IdCliente 	          		:	$scope.rescateReinversion.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.rescateReinversion.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.rescateReinversion.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateReinversion.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1
				    	IdSerieFondo          		:	$scope.rescateReinversion.datosFondoDestino.IdSerieFondo,
				    	IdEmisor                    :   $scope.rescateReinversion.filtros.IdEmisor,
				    	CalculoPorMontoCuota  		:	$scope.rescateReinversion.formData.TipoRescate,
				    	Monto 				  		:   $scope.rescateReinversion.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateReinversion.formData.numeroCuotasAporte,
						IndicadorMLT                :   $scope.rescateReinversion.formData.esMLT ? 'S' : 'N',
						IdMoneda 			  		:	$scope.rescateReinversion.formData.IdMonedaRescate,
						IdMedioDePago				:	'CIN',
						IdRegimenTributario			:	$scope.rescateReinversion.formData.IdRegimenFondoMutuo || '',
				    	Glosa						: 	$scope.rescateReinversion.formData.comentario,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudRescateReinversion(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateReinversion.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaRescate 			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoRescate	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateReinversion.formData.decimalesCantidad),
						SaldoCuotas                     : '',
						SaldoMonto                      : '',
						TipoRescate                     : $scope.rescateReinversion.formData.TipoRescateSeleccionado.NombreConcepto,
						MontoComisiones                 : respuesta.P_OUT_RESULTADO[0].MontoComisionLocal,
						MontoAporteDestino              : respuesta.P_OUT_RESULTADO[0].MontoAporte,
						IdEmisor                        : respuesta.P_OUT_RESULTADO[0].IdEmisorAporte,
						NumeroCuotasDestino             : respuesta.P_OUT_RESULTADO[0].NumeroCuotasAporte,
						esMLT                           : respuesta.P_OUT_RESULTADO[0].IndicadorMLT === 'S' ? 'Si' : 'No',
						DocumentoIdentidad              : respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_RESULTADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_RESULTADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_RESULTADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotaTransferencia, $scope.formData.decimalesCantidad),
						MontoOrigen						: respuesta.P_OUT_RESULTADO[0].Monto,
						IdSerieFondoReinversion			: respuesta.P_OUT_RESULTADO[0].IdSerieFondoDestino,
						IdEmisorReinversion				: respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IdEmisorOrigen                  : respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_RESULTADO[0].IdSerieFondoDestino,
						IdEmisorDestino                 : respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_RESULTADO[0].IndicadorMLT,
	     			    
	     			};
	     		$scope.rescateReinversion.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateReinversion.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		if(id){
	     			var promesa=bcsComunes.ventanaPromesa(id); 
		     		$.when(promesa).then(function(data){  
		     			bcsComunes.ventanaOverlay($scope.winID,false);
		     			$scope.solicitarDeclaracion(respuesta);
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
	 
	 $scope.rescateReinversion.formData.simularAporteRescate = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, 
				 p_IdUsuario : $scope.SesionIdUsuario, 
				 p_IdModulo : 'FMM',
				 p_UsaReglasNegocio : 'S',
				 p_SimularCustodia 	: 'S',
				 
				 
				 p_SolicitudesAporteRescate : [{
					    IdInterno		      		:	0,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
					    //IdCliente 	          		:	$scope.rescateReinversion.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.rescateReinversion.oPortafolio.seleccionado.IdPortafolio,
				    	IdCliente 	          		:	$scope.rescateReinversion.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateReinversion.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1 
				    	TipoSolicitud         		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate,
				    	IdSerieFondo          		:	$scope.rescateReinversion.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	$scope.rescateReinversion.formData.TipoRescate,
				    	EsPorMontoLiquido			:	'N',
				    	Monto 				  		:   $scope.rescateReinversion.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateReinversion.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.rescateReinversion.formData.IdMonedaRescate,
				    	IdMedioDePago				:	'CIN',
				    	IdPreferenciaPagoPortafolio :   0,
				    	Visado						:	'N',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	FechaOperacion				:	util.dateSPdate($scope.rescateReinversion.datosFondo.FechaFondo),
				    	IdRegimenTributario			:	'',
				    	Glosa						: 	$scope.rescateReinversion.formData.comentario,
				    	IndicadorMLT				:	$scope.rescateReinversion.formData.esMLT ? 'S' : 'N',
				    	IdSerieFondoDestino			:	$scope.rescateReinversion.formData.IdSerieFondoDestino || '',
				    	IdEmisorDestino				:	$scope.rescateReinversion.formData.IdEmisorDestino || '',
				    	IdAutorizadoOrigen			:	$scope.rescateReinversion.formData.idAutorizadoDarOrdenes || 0,
				    	Porcentaje					:	0,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.SimularCertificarAporteRescate(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0] !== undefined  && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateReinversion.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdTipoMovimientoFondoMutuo,
						FechaEmision  			 		: util.formatoDate(respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].FechaAceptacion),
						EsPorMontoLiquido        	 	: (respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].EsPorMontoLiquido ==='S')? true : false,
						Monto        	 				: respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].NumeroCuotas, $scope.rescateReinversion.formData.decimalesCantidad),
						TipoRescate                     : $scope.rescateReinversion.formData.TipoRescateSeleccionado.NombreConcepto,
						MontoComisiones                 : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].MontoComisionLocal,
						SaldoCuotas                     : '',
						SaldoMonto                      : '',
						DocumentoIdentidad              : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].NumeroCuotas, $scope.formData.decimalesCantidad),
						IdEmisorOrigen                  : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdSerieFondoDestino,
						IdEmisorDestino                 : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].IndicadorMLT,
						MontoLiquido                    : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].MontoLiquidoLocal,
						Porcentaje                      : respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].Porcentaje,
						Spread                      	: respuesta.P_OUT_SOLICITUDAPORTERESCATECERTIFICADO[0].Spread
	     		};
	     		$scope.rescateReinversion.formData.utilidadPerdida=$scope.rescateReinversion.formData.ResultadoSolicitudAporteRescate.Spread;
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_SIMULACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
	     	 
   		 },function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_SIMULACION"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
   			bcsComunes.ventanaOverlay($scope.winID,false);
   		 });
     }
	 
	 //$scope.$watch('rescateReinversion.oPortafolio.idActual',function(newval,oldval){			//GCMBIPAPAS-1
	 $scope.$watch('rescateReinversion.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.rescateReinversion.oPortafolio.idActual = 0;	//GCMBIPAPAS-1
					 $scope.rescateReinversion.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })

	 //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	 $scope.$watch('rescateReinversion.oClientePortafolio.seleccionado', function(newval,oldval){
		 if (newval && newval.IdCliente>0){
			 $scope.rescateReinversion.formData.IdCategoriaInversionista = newval.IdCategoriaInversionista			 
			 $scope.rescateReinversion.formData.FiltrosComboFondo.IdPortafolio.value = newval.IdPortafolio
			 $scope.rescateReinversion.formData.filtrosFormaPago.IdPortafolio.value = newval.IdPortafolio   
		 }
		 
	 });	
	 //Fin GCMBIPAPAS-1	 
	 
	 $scope.$watch('rescateReinversion.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.rescateReinversion.formData.filtrosRegimenRescate.IdSerieFondo.value = newval.IdSerieFondo;
			 	$scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';
				$scope.rescateReinversion.formData.decimalesCantidad = newval.DecimalesCuota;
				if($scope.rescateReinversion.datosFondoDestino.GrupoRegimen != newval.GrupoRegimen || newval.RescateConMandato == 'N'){
					$scope.rescateReinversion.formData.disabledMlt = true;
					$scope.rescateReinversion.formData.esMLT = false;
				}else{
					$scope.rescateReinversion.formData.disabledMlt = false;
				}
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateReinversion.formData.montoInicial,$scope.rescateReinversion.formData.numeroCuotas,'RES',newval.IdMoneda,$scope.rescateReinversion.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateReinversion.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateReinversion.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateReinversion.formData.numeroCuotas = 0;
						 $scope.rescateReinversion.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateReinversion.formData.numeroCuotas = 0;
					 $scope.rescateReinversion.formData.montoInicial = 0;
		  		 });
				$scope.rescateReinversion.formData.IdMonedaRescate = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
				bcsConfiguracionFFM.calcularMontoCuota($scope.rescateReinversion.datosFondoDestino,$scope.rescateReinversion.formData.montoInicial,$scope.rescateReinversion.formData.numeroCuotasAporte,'RES',$scope.rescateReinversion.formData.IdMonedaRescate,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateReinversion.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 }else{
						 $scope.rescateReinversion.formData.numeroCuotasAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateReinversion.formData.numeroCuotasAporte = 0;
		  		 });
				$scope.rescateReinversion.formData.FiltrosBusquedaAporteFondo.IdMoneda.value = newval.IdMoneda;
				$scope.rescateReinversion.formData.FiltrosBusquedaAporteFondo.IdFamiliaFondo.value = newval.IdFamiliaFondo;
		    }
			else {
				$scope.rescateReinversion.formData.decimalesCantidad = 0;
				$scope.rescateReinversion.formData.numeroCuotas = 0;
				$scope.rescateReinversion.formData.montoInicial = 0;
				$scope.rescateReinversion.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.rescateReinversion.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateReinversion.datosFondoDestino',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.rescateReinversion.formData.decimalesCantidadAporte = newval.DecimalesCuota;
				$scope.rescateReinversion.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				if(newval.GrupoRegimen != $scope.rescateReinversion.datosFondo.GrupoRegimen || $scope.rescateReinversion.datosFondo.RescateConMandato == 'N'){
					$scope.rescateReinversion.formData.disabledMlt = true;
					$scope.rescateReinversion.formData.esMLT = false;
				}else{
					$scope.rescateReinversion.formData.disabledMlt = false;
				}
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateReinversion.formData.montoInicial,$scope.rescateReinversion.formData.numeroCuotasAporte,'RES',$scope.rescateReinversion.formData.IdMonedaRescate,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateReinversion.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 }else{
						 $scope.rescateReinversion.formData.numeroCuotasAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateReinversion.formData.numeroCuotasAporte = 0;
		  		 });
				$scope.rescateReinversion.filtros.IdEmisor = newval.IdEmisor;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.rescateReinversion.formData.decimalesCantidadAporte = 0;
				$scope.rescateReinversion.formData.numeroCuotasApote = 0;
				$scope.rescateReinversion.formData.montoAporte = 0;
			}
		 $scope.rescateReinversion.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateReinversion.formData.IdMonedaRescate', function(newval,oldaval){
		 $scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';
		 if(newval){
			 $scope.rescateReinversion.formData.decimalesMontoDestino = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
			 $scope.rescateReinversion.formData.FiltrosBusquedaAporteFondo.IdMoneda.value = newval;
		 }else{
			 $scope.rescateReinversion.formData.decimalesMontoDestino = 0;
		 }
		 $scope.rescateReinversion.ajustaVentana();
	 });
	 
	 
	
	 $scope.$watch('rescateReinversion.formData.montoInicial',function(newval,oldval){
		 if (newval==null || newval==0){$scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';};
		
			if(newval && newval != oldval){
				 $scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';
				bcsConfiguracionFFM.calcularMontoCuota($scope.rescateReinversion.datosFondo,newval,$scope.rescateReinversion.formData.numeroCuotas,'RES',$scope.rescateReinversion.formData.IdMonedaRescate,$scope.rescateReinversion.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateReinversion.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateReinversion.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 $scope.rescateReinversion.formData.btnSimulacion=true;
					 }else{
						 $scope.rescateReinversion.formData.numeroCuotas = 0;
						 $scope.rescateReinversion.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateReinversion.formData.numeroCuotas = 0;
					 $scope.rescateReinversion.formData.montoInicial = 0;
		  		 });
				bcsConfiguracionFFM.calcularMontoCuota($scope.rescateReinversion.datosFondoDestino,newval,$scope.rescateReinversion.formData.numeroCuotasAporte,'RES',$scope.rescateReinversion.formData.IdMonedaRescate,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateReinversion.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 }else{
						 $scope.rescateReinversion.formData.numeroCuotasAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateReinversion.formData.numeroCuotasAporte = 0;
		  		 });
			}
	 });
	 
	 $scope.$watch('rescateReinversion.masInformacion',function(newval,oldval){
		 $scope.rescateReinversion.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateReinversion.formData.TipoRescate',function(newval,oldval){
		 $scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';
		 if(newval === 'T'){
			 $scope.rescateReinversion.formData.btnSimulacion=true;
			 $scope.rescateReinversion.formData.numeroCuotas = $scope.rescateReinversion.formData.SaldoCuotas;
			 $scope.rescateReinversion.formData.montoInicial = $scope.rescateReinversion.formData.SaldoMonto;
		 }else{
			 $scope.rescateReinversion.formData.numeroCuotas = 0;
			 $scope.rescateReinversion.formData.montoInicial = 0;
		 }
		 $scope.rescateReinversion.ajustaVentana();
		 
	 });
	 

	 $scope.$watch('rescateReinversion.formData.SaldoCuotas', function(newval,oldval){	 	
		 $scope.rescateReinversion.formData.numeroCuotas = $scope.rescateReinversion.formData.SaldoCuotas;
		 $scope.rescateReinversion.formData.montoInicial = $scope.rescateReinversion.formData.SaldoMonto;			 
	 });
	 
	 
	 $scope.$watch('rescateReinversion.formData.numeroCuotas',function(newval,oldval){
		 if (newval==null || newval==0){$scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';};
		 
		 if(newval && newval != oldval){
			 $scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';
			 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateReinversion.datosFondo,$scope.rescateReinversion.formData.montoInicial,newval,'RES',$scope.rescateReinversion.formData.IdMonedaRescate,$scope.rescateReinversion.formData.TipoRescate,$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.rescateReinversion.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.rescateReinversion.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 $scope.rescateReinversion.formData.btnSimulacion=true;
				 }else{
					 $scope.rescateReinversion.formData.numeroCuotas = 0;
					 $scope.rescateReinversion.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.rescateReinversion.formData.numeroCuotas = 0;
				 $scope.rescateReinversion.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('rescateReinversion.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateReinversion.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateReinversion.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateReinversion.formData.FiltrosBusquedaAporteFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateReinversion.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 $scope.rescateReinversion.formData.btnSimulacion=false;$scope.rescateReinversion.formData.utilidadPerdida='';
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateReinversion.formData.montoInicial = undefined;
				 $scope.rescateReinversion.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateReinversion.formData.AporteRescate = 'A';
				$scope.rescateReinversion.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateReinversion.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateReinversion.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.rescateReinversion.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateReinversion.formData.tipoOperacion = 0;
			 $scope.rescateReinversion.formData.AporteRescate = 'A';
		 }
		 $scope.rescateReinversion.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.rescateReinversion.formData.IngresoConfirmacion = 'I';
		 $scope.rescateReinversion.formData.TipoRescate = 'M';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
		 //$scope.rescateReinversion.oSocioNegocio.idActual = 0;
		 //$scope.rescateReinversion.oPortafolio.idActual = 0;
		 $scope.rescateReinversion.oClientePortafolio.idActual = 0;
		//Fin GCMBIPAPAS-1			 
		 $scope.rescateReinversion.formData.setFondo = '-1';
		 $scope.rescateReinversion.formData.setFondoDestino = '-1';
		 $scope.rescateReinversion.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateReinversion.formData.montoInicial = undefined;
		 $scope.rescateReinversion.formData.montoAporte = undefined;
		 $scope.rescateReinversion.formData.numeroCuotas = undefined;
		 $scope.rescateReinversion.formData.numeroCuotasAporte = undefined;
		 $scope.rescateReinversion.formData.esMLT = true;
		 $scope.rescateReinversion.formData.montoComisiones = undefined;
		 $scope.rescateReinversion.formData.IdFormaPago = '0';
		 $scope.rescateReinversion.formData.comentario = '';
		 $scope.rescateReinversion.formData.FiltrosBusquedaFondo = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateReinversion.formData.FiltrosBusquedaAporteFondo = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdMoneda : {"field" : "IdMoneda", "operator": "eq", "value" : undefined},
			IdFamiliaFondo : {"field" : "IdFamiliaFondo", "operator": "eq", "value": undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateReinversion.ajustaVentana();
	 }
	 
	 $scope.rescateReinversion.formData.resetFoco =  function() {
	 	$scope.rescateReinversion.formData.focoVolver = false;
		if($scope.rescateReinversion.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateReinversion.formData.focoOperacion = true;
			$scope.rescateReinversion.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateReinversion.formData.verComprobante = function() {
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'ComReRei' },
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
										"p_IdSolicitudAporteRescate" : $scope.rescateReinversion.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
			
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [{
														"type" : "pdf417",
														"name" : "pdf417",
														"data" :  bcsConfiguracionFFM.generarPdf417($scope.rescateReinversion.formData.ResultadoSolicitudAporteRescate)
													}];
													
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 };
	 
	 $scope.rescateReinversion.formData.verMandato = function(){
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
										"p_IdSolicitudAporteRescate" : $scope.rescateReinversion.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
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
	 
	 $scope.rescateReinversion.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.rescateReinversion.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
	 						,IdCliente			: $scope.rescateReinversion.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: $scope.rescateReinversion.formData.IdMedioDePago ? $scope.rescateReinversion.formData.IdMedioDePago : 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };

	 $scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.rescateReinversion.oClientePortafolio.seleccionado,
			$scope.rescateReinversion.datosFondo,
			$scope.rescateReinversion.formData
		);
	};
});