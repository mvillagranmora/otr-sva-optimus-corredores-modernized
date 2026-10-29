angular.module('ModuloFFM').controller('IngresarAporteNormalFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,bcsServicioPlataforma,bcsServiciosGrillasMesaRF,$timeout){
	var aporteNormal = this;             
		
	$scope.data = {};
	$scope.data.objFecha= {};
	$scope.data.objCaja = {};
	$scope.data.MonedaPrincipalIdActual = 'USD';
	$scope.data.MonedaSecundariaIdActual = 'CLP';
	$scope.data.IdTipoOperacionRelacionada='COMSPT';
	$scope.data.IdTipoOperacion='VENSPT';
	$scope.data.IdTipoProducto="MEME";
	
	$scope.data.DeshabilitaFormaPago = false;
	$scope.ConfiguracionFM = {};
	$scope.ConsultarParametroFondoMutuo = bcsConfiguracionFFM.getDataSourceConsultarParametroFondoMutuo();
		
	$scope.aporteNormal.formData = {};
	$scope.aporteNormal.masInformacion = false;
	$scope.aporteNormal.formData.setInstrumento = "-1";
	$scope.aporteNormal.datosFondo = {};
	$scope.aporteNormal.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.aporteNormal.formData.DatosAdicionales = "N";
	$scope.aporteNormal.formData.visibleTruco = true;
	aporteNormal.confGlobal = $scope.aporteRescateMain.confGlobal;
	aporteNormal.oSocioNegocio = {};
	$scope.dataTemplate = {};
	
	/*********nueva forma de pago **************/
	$scope.data.IdMonedaMontoDisplay = $scope.SesionIdMonedaSistema;
	$scope.data.formaDePagoEntregamos = {};
	$scope.data.formaDePagoRecibimos = {};
	$scope.data.formaDePagoEntregamosTotal = 0;
	$scope.data.formaDePagoRecibimosTotal = 0;

	$scope.aporteNormal.formData.formaPagoEntregamosCargada = false;
	$scope.aporteNormal.formData.formaPagoRecibimosCargada = false;
	$scope.aporteNormal.formData.botonHabilitado = false;

	function actualizarBoton() {
		$scope.aporteNormal.formData.botonHabilitado =
		$scope.aporteNormal.formData.formaPagoEntregamosCargada &&
		$scope.aporteNormal.formData.formaPagoRecibimosCargada &&
		$scope.FormIngresoFFM &&
		$scope.FormIngresoFFM.$valid;
	}

	$scope.$watch('FormIngresoFFM.$valid', function(newVal, oldVal) {
    	actualizarBoton();
	});
	
	$scope.$watchCollection('data.formaDePagoEntregamos', function(newVal) {
    if (newVal && newVal !== undefined && Object.keys(newVal).length > 0) {
        $scope.aporteNormal.formData.formaPagoEntregamosCargada = true;
		actualizarBoton()
    }
	});

	$scope.$watchCollection('data.formaDePagoRecibimos', function(newVal) {
    if (newVal && newVal !== undefined && Object.keys(newVal).length > 0) {
        $scope.aporteNormal.formData.formaPagoRecibimosCargada = true;
		actualizarBoton()
    }
	});

	var dataSourcePreferenciaPortafolio = bcsServiciosGrillasMesaRF.getDataSourcePreferenciaPortafolio();
	
	aporteNormal.datosFondoDefecto = {};
	$scope.aporteNormal.autoCargarClientePortafolio = false;
	$scope.aporteNormal.formData.idAutorizadoDarOrdenesDefecto = undefined;
	$scope.aporteNormal.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,15);
    }
		
	$scope.aporteNormal.ajustaVentana();
	
	//Formas de Pagos (Entregamos/Recibimos)
	$scope.data.oConfiguracionFormaPago={
		filtros : {
			NombreTipoParametro : { "field"   : "NombreTipoParametro","operator": "eq", "value" : "FormaPago" }
		}		
	};
	
	$scope.aporteNormal.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
		
	$scope.aporteNormal.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.aporteNormal.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	
	$scope.aporteNormal.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
		
	$scope.aporteNormal.oConfiguracionMedioDePago={
			tipo : "combobox-medio-pago",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.aporteNormal.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  aporteNormal.oSocioNegocio.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};
	
	$scope.aporteNormal.oConfiguracionMonedaPago={
			filtros : {
			//	IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1}
				//MonUSD 		  : { "field"   : "IdMoneda","operator": "eq", "value" : "USD" },
				//MonCLP		  : { "field"   : "IdMoneda","operator": "eq", "value" : "CLP" },
			}
		};
	$scope.aporteNormal.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente    : { "field": "IdCliente","operator": "eq", "value" : 0 },
		 	 IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value" : 0 }
		 },
	 };
	
	$scope.aporteNormal.formData.filtrosCuentaInversion = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1}
	};
	
	
	$scope.filIdPortafolio	= { "field" : "IdPortafolio","operator": "eq", "value"   :-1 };
	$scope.filMonUSD	= { "field"   : "IdMoneda","operator": "eq", "value" : "USD"  };
	$scope.filMonCLP	= { "field"   : "IdMoneda","operator": "eq", "value" : "CLP"};
	
	$scope.aporteNormal.formData.filtroMonedaPago = {
			complejo : [$scope.filMonCLP],
			logic : "or",
			filters : [
				$scope.filMonUSD,$scope.filMonCLP
			]
	};

	
	$scope.aporteNormal.formData.FiltrosAutorizarOrden = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
		IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.aporteNormal.formData.filtrosRegimen = {
			IdSerieFondo : {"field":"IdSerieFondo", "operator": "eq", "value":undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
	
	 $scope.aporteNormal.formData.masDatos = function()
	 {
		bcsComunes.ventanaAutoHeight($scope.winID);
		 if ($scope.aporteNormal.formData.DatosAdicionales === 'S'){
			 $scope.aporteNormal.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.aporteNormal.formData.DatosAdicionales = 'S';
	     }
	 }
	
	 $scope.aporteNormal.configuracionFMM = function(){
		 var filters= [ { field: "NombreTipoParametro",operator: "eq", value: 'APORTE_GENERA_COMPRA_SPOT_DOLAR'},
			 					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			 					{ field: "IdEstado",		operator: "eq", value: 'VIG' } ];
		  
		 	$scope.ConsultarParametroFondoMutuo.query({ filter: filters }).then(function(){
		 		if($scope.ConsultarParametroFondoMutuo.ds._data.length > 0) {
		 			$scope.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR= $scope.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro;
		 		}else{$scope.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR='N';};
		 	});
	 };
	 
	 $scope.aporteNormal.formData.ingresarAporteRescate = function()
     {	

		 var IdMonedaPago='';
		 if ($scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO ==='S'){IdMonedaPago=$scope.aporteNormal.formData.MonedaPagoIdActual? $scope.aporteNormal.formData.MonedaPagoIdActual : ''};
		 
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesAporteRescate : [{
					    IdInterno		      		:	1,
						IdCliente 	          		:	$scope.aporteNormal.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.aporteNormal.oPortafolio.seleccionado.IdPortafolio,
				    	TipoSolicitud         		:	$scope.oOperacion.seleccionado.IndicadorAporteRescate,
				    	IdSerieFondo          		:	$scope.aporteNormal.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	'M',
				    	EsPorMontoLiquido			:	'N',
				    	Monto 				  		:   $scope.aporteNormal.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.aporteNormal.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.aporteNormal.formData.IdMonedaAporte,
				    	IdMedioDePago				:	$scope.aporteNormal.formData.IdMedioDePago ? $scope.aporteNormal.formData.IdMedioDePago : 'CIN',
				    	IdPreferenciaPagoPortafolio :   0,
				    	Visado						:	'N',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	FechaOperacion				:	'0001-01-01',
				    	IdRegimenTributario			:	$scope.aporteNormal.formData.IdRegimenFondoMutuo || '',
				    	Glosa						: 	$scope.aporteNormal.formData.comentario,
				    	IndicadorMLT				:	'N',
				    	IdSerieFondoDestino			:	'',
				    	IdEmisorDestino				:	'',
				    	IdAutorizadoOrigen			:	$scope.aporteNormal.formData.idAutorizadoDarOrdenes || 0,
				    	Porcentaje					:	0,
				    	FolioContrato				:	0,
				    	IdMonedaPago				:	IdMonedaPago,
						IngresoDesde				:	"CORREDORES"
				 }]
		 }
		 //obj Pago SPOT
		 var objFormaPagoSPOT	= $scope.obtenerObjPagoSpot();
		 datosIngresoAporteRescate.p_FormaPagoSpot =objFormaPagoSPOT;
			 
		 
		 if ($scope.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR=="S" && $scope.aporteNormal.datosFondo.IdMoneda=="USD" && $scope.aporteNormal.formData.IdMonedaAporte=="USD" && $scope.aporteNormal.formData.OperacionesSpot==true){			 
			 var msjErrorFormaPago = '';
				var entraError = false;
				
		 			if($scope.data.formaDePagoEntregamosTotal < ($scope.aporteNormal.formData.montoInicial||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_INCOMPLETO'; entraError = true;}
		 			if($scope.data.formaDePagoEntregamosTotal > ($scope.aporteNormal.formData.montoInicial||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_ENTREGAMOS_EXCEDE'; entraError = true;}
		 			if($scope.data.formaDePagoRecibimosTotal < ($scope.aporteNormal.formData.MontoMonedaSistema||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_INCOMPLETO'; entraError = true;}
		 			if($scope.data.formaDePagoRecibimosTotal > ($scope.aporteNormal.formData.MontoMonedaSistema||0)){ msjErrorFormaPago = 'FORMA_DE_PAGO_RECIBIMOS_EXCEDE'; entraError = true;}
		 			
		 			if(entraError){
		 				bcsComunes.ventanaOverlay($scope.winID,false);
		 				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(msjErrorFormaPago));
		 				return;
		 			}
		 };
		 
	     bcsServicioFondosMutuos.CrearSolicitudAporteRescateNormal(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		
	     		 $scope.aporteNormal.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.aporteNormal.formData.decimalesCantidad),
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
	     			};
	     		$scope.aporteNormal.formData.IngresoConfirmacion = 'C';
	     		$scope.aporteNormal.ajustaVentana();
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
	 
	 $scope.$watch('aporteNormal.oPortafolio.idActual',function(newval,oldval){
		 if(newval && newval > 0){
			 var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'FMM',
					p_IdEmpleado		: $scope.SesionIdEmpleado,
					p_IdTipoProducto	: 'INFM',
					p_IdTipoOperacion	: 'APORFM',
					P_IdPortafolio		: newval
			 }
			 
			 bcsServicioFondosMutuos.ConsultarRequisitosBasicosCrearSolicitudAporteRescate(datos).$promise.then(function(respuesta){
				 if (respuesta.P_OUT_ERRORESCONTADOR > 0) {
					 $scope.aporteNormal.oPortafolio.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
			  //Se obtienen fechas del movimiento y obtiene el flujo de caja desde el watch
			 if ($scope.aporteNormal.datosFondo.IdFondo && $scope.data.objFecha)
			 {
				 $scope.data.objFecha = $scope.data.FechasSolicitud();
				 $scope.flujoCaja('MOVIMIENTO');
				 $scope.flujoCaja('');
				 $scope.aporteNormal.formData.setFondo = '-1';
			 };
			 
			 //Forma de pago CTA
			 if ($scope.aporteNormal.oPortafolio.idActual)
   			  {
 				  var filter = [
 								  { "field"   : "IdPortafolio","operator": "eq", "value"   :  $scope.aporteNormal.oPortafolio.idActual}
 				               ];

				  dataSourcePreferenciaPortafolio.query({filter:filter}).then(function(){
						if (dataSourcePreferenciaPortafolio.data().length > 0){
							var datos = dataSourcePreferenciaPortafolio.data();							
							$.each(datos,function(key,value){
									                           if(value.OperaCuentaInversion === 'S'){
									                            	  $scope.operaCuentaInversionCliente = value.OperaCuentaInversion;
										                              $scope.aporteNormal.formData.IdMedioDePago = 'CIN';
										                              $scope.aporteNormal.formData.IdMedioDePagoPostFiltro = 'CIN';
										                              $scope.aporteNormal.oConfiguracionMedioDePago.filtros.IdMedioDePago = { "field"   : "IdMedioDePago","operator": "eq", "value"   :  undefined};
									                           }else{
 									                            	     $scope.operaCuentaInversionCliente = value.OperaCuentaInversion;
										                                 $scope.aporteNormal.formData.IdMedioDePago = 'TRA';  
										                                 $scope.aporteNormal.formData.IdMedioDePagoPostFiltro = 'TRA';
										                                 $scope.aporteNormal.oConfiguracionMedioDePago.filtros.IdMedioDePago = { "field"   : "IdMedioDePago","operator": "ne", "value"   :  'CIN'};
										                                 
									                                }
									                        });								
						}
 			      })
		      }
		 }	 
	 })
	 
	 $scope.$watch('aporteNormal.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.aporteNormal.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.aporteNormal.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.aporteNormal.formData.montoInicial,$scope.aporteNormal.formData.numeroCuotas,'APO',newval.IdMoneda,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.aporteNormal.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.aporteNormal.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 $scope.aporteNormal.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
						 $scope.aporteNormal.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;
						 
					 }else{
						 $scope.aporteNormal.formData.numeroCuotas = 0;
						 $scope.aporteNormal.formData.montoInicial = 0;
						 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
						 $scope.aporteNormal.formData.IdMonedaSistema = 0;
					 }
					
				 },function(respuesta){
					 $scope.aporteNormal.formData.numeroCuotas = 0;
					 $scope.aporteNormal.formData.montoInicial = 0;
					 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
					 $scope.aporteNormal.formData.IdMonedaSistema = 0;
					 
		  		 });
				 $scope.aporteNormal.formData.IdMonedaAporte = newval.IdMoneda;
			 		
				 //Se obtienen fechas del movimiento y obtiene el flujo de caja desde el wztch
				 $scope.data.objFecha = $scope.data.FechasSolicitud();
				 
				 $scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.aporteNormal.formData.decimalesCantidad = 0;
				$scope.aporteNormal.formData.numeroCuotas = 0;
				$scope.aporteNormal.formData.montoInicial = 0;
				$scope.aporteNormal.formData.MontoMonedaSistema = 0;
				$scope.aporteNormal.formData.IdMonedaSistema = 0;
				$scope.aporteNormal.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}

		 	
			
			
		 $scope.aporteNormal.ajustaVentana();
	 });
	 
	 
	 $scope.flujoCaja = function(tipo)
     {	
		 var moneda='';
		 
		 if (tipo=="MOVIMIENTO"){
			 moneda=$scope.aporteNormal.formData.IdMonedaAporte;
		 }else{
			 moneda='CLP';
		 };
	    
	     
		if ($scope.data.objFecha.fechaAceptacionRes){
			 var fecha ='';
		     if ((($scope.data.objFecha.fechaAceptacionRes).toString()).length >23){
		     	fecha= util.dateSPdate($scope.data.objFecha.fechaAceptacionRes);
		     }else if ((($scope.data.objFecha.fechaAceptacionRes).toString()).length =23){
			   fecha=$scope.data.objFecha.fechaAceptacionRes.substring(0, 10);  
		     }else{
		     	fecha=$scope.data.objFecha.fechaAceptacionRes;
		     };

			
		 	var datos = {
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_UsaReglasNegocio : 'N',
					p_Uso : 'INGRESO_APORTE_FMM',
					p_IdModulo: 'FMM',
				
					p_PortafolioAConsultar : [{
						"Identificador" : 0,
						"IdPortafolio" 	: $scope.aporteNormal.oPortafolio.seleccionado.IdPortafolio,
						"IdMoneda"		: moneda
					}],
	
					p_FechaDesde :  fecha,
					p_FechaHasta :  fecha
				};
					
		 	bcsServicioPlataforma.Servicios().ConsultaFlujoCaja(datos).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 
	     	 
	     	if (respuesta.P_OUT_FLUJOCAJA["0"] && respuesta.P_OUT_FLUJOCAJA["0"].IdPortafolio){
	     		$.each(respuesta.P_OUT_FLUJOCAJA,function(key,value) {
					if (value.IdMoneda === moneda)
						if (tipo=="MOVIMIENTO"){
						$scope.data.objCaja.saldoCuentaInversion=value.Saldo;
						}else{
							 $scope.data.objCaja.saldoCuentaInversionPesos=value.Saldo;
						 };
					    
					   
				});

	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_OBTENER_SALDO_CAJA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
   		 },function(respuesta){
   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_OBTENER_SALDO_CAJA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
   			bcsComunes.ventanaOverlay($scope.winID,false);
   		 });
		 	
		};	
     };
     
	 $scope.$watch('data.objFecha', function(newval,oldaval){
		 if(newval){
			 $scope.flujoCaja('MOVIMIENTO');
			 $scope.flujoCaja('');
		 };
		 
		
	 });
	 
	 $scope.validaCondicionAporteUSD = function(eve){
		 // ** APORTES USD (diferidos) ** // 
		 // - CASO 3 : Si el aporte es USD  y tiene Saldo en USD, se hace un flujo normal, (sin SPOT)  
		 if ($scope.data.objCaja.saldoCuentaInversion>0){
			 $scope.ConfiguracionFM.APORTE_DATOS_TESORERIA="N";
			 $scope.ConfiguracionFM.APORTE_DATOS_SPOT="N";
			 $scope.aporteNormal.formData.OperacionesSpot=false;
		 }else{
			if ($scope.data.objCaja.saldoCuentaInversionPesos <=0){
		 		$scope.ConfiguracionFM.APORTE_DATOS_TESORERIA="S";
		 	};
		 	
		 	$scope.ConfiguracionFM.APORTE_DATOS_SPOT="S";
		 	if (eve == 0 ){
		 		$scope.aporteNormal.formData.montoInicial = 0;
		 	};
		 };
     };
	 
    $scope.obtenerObjPagoSpot = function()
		{
    	 
    		var tablaDatosFormaPago	= [];
    		
    		var i=0;
			$.each($scope.data.formaDePagoEntregamos,function(key,value) {
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'P' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
										 "IdInternoPagoOperacion" : ++i
										,"IdComprobantePagoOperacion" : idComprobantePagoOperacion
										,"IdTipoMovimiento"          :   $scope.data.IdTipoOperacion==='COMSPT'?'MOVING':'MOVEGR'
										,"IdFormaDePago":key
										,"IdOperacion":1
										,"RecibePagaPortafolio":'P'
										,"IdMoneda":$scope.data.IdTipoOperacion==='COMSPT'?$scope.data.MonedaSecundariaIdActual:$scope.data.MonedaPrincipalIdActual
										,"IdMonedaLiquidacion":$scope.data.IdTipoOperacion==='COMSPT'?$scope.data.MonedaSecundariaIdActual:$scope.data.MonedaPrincipalIdActual
										,"MontoFormaDePago":value
										,"AfectarTesoreria" :key.includes("_CIN_")?'N':'S'});
				}
			});
			$.each($scope.data.formaDePagoRecibimos,function(key,value) {
				if (value > 0){
					var idComprobantePagoOperacion = 0;
					$.each($scope.data.formasDePago,function(keyF,valueF) {
						if (valueF.RecibePagaPortafolio === 'R' && valueF.IdFormaDePago === key)
							idComprobantePagoOperacion = valueF.IdComprobantePagoOperacion
					});
					tablaDatosFormaPago.push({
									 "IdInternoPagoOperacion" : ++i
									,"IdComprobantePagoOperacion" : idComprobantePagoOperacion
									,"IdTipoMovimiento"          : $scope.data.IdTipoOperacion==='COMSPT'?'MOVEGR':'MOVING'
									,"IdFormaDePago":key
									,"IdOperacion":1
									,"RecibePagaPortafolio":'R'
									,"IdMoneda":$scope.data.IdTipoOperacion==='COMSPT'?$scope.data.MonedaPrincipalIdActual:$scope.data.MonedaSecundariaIdActual
									,"IdMonedaLiquidacion":$scope.data.IdTipoOperacion==='COMSPT'?$scope.data.MonedaPrincipalIdActual:$scope.data.MonedaSecundariaIdActual
									,"MontoFormaDePago":value
									,"AfectarTesoreria" :key.includes("_CIN_")?'N':'S'});
				}
			});
    		
    	 
    	 
    		return tablaDatosFormaPago;
			
		};
		
      
 
	 $scope.$watch('data.objCaja.saldoCuentaInversionPesos', function(newval,oldaval){
		 if(newval!=undefined){
			 $scope.validaCondicionAporteUSD(0);
		 };
		 
		
	 });
	 
	 $scope.$watch('aporteNormal.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.aporteNormal.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.aporteNormal.formData.decimalesMonto = 0;
		 }
		 $scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO="N";
		
		 if ($scope.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR=="S" && $scope.aporteNormal.datosFondo.IdMoneda=="USD" && $scope.aporteNormal.formData.IdMonedaAporte=="USD"){
		    $scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO="S";
		    $scope.validaCondicionAporteUSD(0);
		 }else{
			 $scope.ConfiguracionFM.APORTE_DATOS_TESORERIA="N";
			 $scope.ConfiguracionFM.APORTE_DATOS_SPOT="N";
			 $scope.aporteNormal.formData.OperacionesSpot=false;
 		 };
		 $scope.aporteNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteNormal.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.aporteNormal.datosFondo,newval,$scope.aporteNormal.formData.numeroCuotas,'APO',$scope.aporteNormal.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteNormal.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteNormal.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 $scope.aporteNormal.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					 $scope.aporteNormal.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;					 
				 }else{
					 $scope.aporteNormal.formData.numeroCuotas = 0;
					 $scope.aporteNormal.formData.montoInicial = 0;
					 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
					 $scope.aporteNormal.formData.IdMonedaSistema = 0;
					 
				 }
			 },function(respuesta){
				 $scope.aporteNormal.formData.numeroCuotas = 0;
				 $scope.aporteNormal.formData.montoInicial = 0;
				 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
				 $scope.aporteNormal.formData.IdMonedaSistema = 0;
	  		 });
			
			
			
		}
	 });
	 
	 $scope.$watch('aporteNormal.formData.MontoMonedaSistema',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.aporteNormal.datosFondo,newval,$scope.aporteNormal.formData.numeroCuotas,'APO',$scope.aporteNormal.formData.IdMonedaSistema,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteNormal.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteNormal.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].Monto;
					 $scope.aporteNormal.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					 $scope.aporteNormal.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;					 
				 }else{
					 $scope.aporteNormal.formData.numeroCuotas = 0;
					 $scope.aporteNormal.formData.montoInicial = 0;
					 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
					 $scope.aporteNormal.formData.IdMonedaSistema = 0;
					 
				 }
			 },function(respuesta){
				 $scope.aporteNormal.formData.numeroCuotas = 0;
				 $scope.aporteNormal.formData.montoInicial = 0;
				 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
				 $scope.aporteNormal.formData.IdMonedaSistema = 0;
	  		 });
		}
	});
	 
	 $scope.$watch('aporteNormal.masInformacion',function(newval,oldval){
		 $scope.aporteNormal.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteNormal.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.aporteNormal.datosFondo,$scope.aporteNormal.formData.montoInicial,newval,'APO',$scope.aporteNormal.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteNormal.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteNormal.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 $scope.aporteNormal.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					 $scope.aporteNormal.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;
				 }else{
					 $scope.aporteNormal.formData.numeroCuotas = 0;
					 $scope.aporteNormal.formData.montoInicial = 0;
					 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
					 $scope.aporteNormal.formData.IdMonedaSistema = 0;
				 }
			 },function(respuesta){
				 $scope.aporteNormal.formData.numeroCuotas = 0;
				 $scope.aporteNormal.formData.montoInicial = 0;
				 $scope.aporteNormal.formData.MontoMonedaSistema = 0;
				 $scope.aporteNormal.formData.IdMonedaSistema = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.aporteNormal.formData.montoInicial = undefined;
				 $scope.aporteNormal.formData.numeroCuotas = undefined;
				 $scope.aporteNormal.formData.MontoMonedaSistema =  undefined;
				 $scope.aporteNormal.formData.IdMonedaSistema =  undefined;
			 }
			 
			 $scope.aporteNormal.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.$watch('aporteNormal.formData.MonedaPagoIdActual', function(newval,oldval){
		 if (newval && newval != oldval && $scope.ConfiguracionFM.MOSTRAR_MONEDA_PAGO=="S"){
			 if(newval == 'USD'){
				 $scope.validaCondicionAporteUSD(0);
				 if($scope.aporteNormal.datosFondo.IdMoneda == 'CLP'){
					 $scope.aporteNormal.formData.OperacionesSpot=true;
				 }
			 }else{
				 $scope.ConfiguracionFM.APORTE_DATOS_TESORERIA="N";
				 //$scope.ConfiguracionFM.APORTE_DATOS_SPOT="N";
				 if($scope.aporteNormal.datosFondo.IdMoneda == 'USD'){
					 $scope.aporteNormal.formData.OperacionesSpot=true;
				 } else{
					 $scope.aporteNormal.formData.OperacionesSpot=false;
				 }
			 };	
		 }	 
	 });
	 
	 $scope.$watch('ConfiguracionFM.MOSTRAR_MONEDA_PAGO', function(newval,oldval){
		 if (newval !='S' && newval != oldval){
			 $scope.ConfiguracionFM.APORTE_DATOS_TESORERIA="N";
			 $scope.ConfiguracionFM.APORTE_DATOS_SPOT="N";
		 }
		 
	 });
	 
	 $scope.aporteNormal.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.aporteNormal.formData.tipoOperacion = 0;
		 }
		 $scope.aporteNormal.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.aporteNormal.formData.IngresoConfirmacion = 'I';
		 $scope.aporteNormal.oSocioNegocio.idActual = aporteNormal.confGlobal;
		 $scope.aporteNormal.oPortafolio.idActual = 0;
		 $scope.aporteNormal.formData.setFondo = '-1';
		 $scope.aporteNormal.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.aporteNormal.formData.montoInicial = undefined;
		 $scope.aporteNormal.formData.numeroCuotas = undefined;
		 $scope.aporteNormal.formData.montoComisiones = undefined;
		 $scope.aporteNormal.formData.IdFormaPago = '0';
		 $scope.aporteNormal.formData.comentario = '';
		 $scope.aporteNormal.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.aporteNormal.ajustaVentana();
		 $scope.aporteNormal.formData.formaPagoEntregamosCargada = false;
		 $scope.aporteNormal.formData.formaPagoRecibimosCargada = false;
		 $scope.aporteNormal.formData.botonHabilitado = false;
	 }
	 
	 $scope.aporteNormal.formData.resetFoco =  function() {
	 	$scope.aporteNormal.formData.focoVolver = false;
		if($scope.aporteNormal.formData.IngresoConfirmacion === 'I')
		{
			$scope.aporteNormal.formData.focoOperacion = true;
			$scope.aporteNormal.formData.focoVolver = false;
		}
	 };
	 
	 $scope.aporteNormal.formData.verComprobante = function() {
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'CompApor' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
				if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
					
					if(typeof $scope.aporteNormal.formData.ResultadoSolicitudAporteRescate.NumeroCuotasTransferencia == 'undefined')
						$scope.aporteNormal.formData.ResultadoSolicitudAporteRescate.NumeroCuotasTransferencia = 0;
					if(typeof $scope.aporteNormal.formData.ResultadoSolicitudAporteRescate.MontoTransferencia == 'undefined')
						$scope.aporteNormal.formData.ResultadoSolicitudAporteRescate.MontoTransferencia = 0;
					
					var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
										"p_IdModulo"		: 'FMM',
										"p_IdUsuario"				: $scope.SesionIdUsuario,
										"p_IdSolicitudAporteRescate" : $scope.aporteNormal.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
					
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [{
					    								"type" : "pdf417",
					    								"name" : "pdf417",
					    								"data" :  bcsConfiguracionFFM.generarPdf417($scope.aporteNormal.formData.ResultadoSolicitudAporteRescate)
					    							}];
					
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 };
	 
	 $scope.aporteNormal.limpiarFMM();
	 $scope.aporteNormal.configuracionFMM();
	 
	 if (aporteNormal.confGlobal.IdCliente){
		 $scope.aporteNormal.oSocioNegocio.idDefecto = aporteNormal.confGlobal.IdSocioDeNegocio;
	     $scope.aporteNormal.oSocioNegocio.filtros.IdCliente.value = aporteNormal.confGlobal.IdCliente;
		 $scope.aporteNormal.oSocioNegocio.filtros.read = true;
		 
		 $scope.aporteNormal.oPortafolio.idDefecto	= aporteNormal.confGlobal.IdPortafolio;
		 $scope.aporteNormal.oPortafolio.filtros.IdPortafolio.value = aporteNormal.confGlobal.IdPortafolio;
		 $scope.aporteNormal.oPortafolio.filtros.read = true;
		 
		 aporteNormal.datosFondoDefecto.IdDefecto = aporteNormal.confGlobal.IdSerieFondo;
		 $scope.aporteNormal.formData.FiltrosCliente = {
		    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  aporteNormal.confGlobal.IdCliente},	
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  aporteNormal.confGlobal.IdPortafolio}	
		 }
	 }
	 
	 $scope.$watch('aporteNormal.oPortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.aporteNormal.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.aporteNormal.oSocioNegocio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.aporteNormal.oPortafolio.idActual}	
				 }
			}
		 });
	 
	 /* se quitar por IMPSURAOPT-267
	$scope.$watch('aporteNormal.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = aporteNormal.confGlobal.JerarquiaPerfil[newval];
			 $scope.aporteNormal.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.aporteNormal.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 })*/
	$scope.$watch('data.ValoresSaldos', function(newval,oldval){
		 if(newval){
		 }
	 })
	 
	 

	$scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						,IdCliente			: $scope.aporteNormal.oPortafolio.seleccionado.IdCliente
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: $scope.aporteNormal.formData.IdMedioDePago ? $scope.aporteNormal.formData.IdMedioDePago : 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };	
	 
	$scope.asignarRegimenPorDefecto = function () {
		if($scope.aporteNormal.oPortafolio.seleccionado && $scope.aporteNormal.oPortafolio.seleccionado.CodigoPortafolio.length > 0
			&& $scope.aporteNormal.datosFondo.IdSerieFondo && $scope.aporteNormal.datosFondo.IdSerieFondo.length > 0 ){
				$scope.aporteNormal.formData.IdRegimenFondoMutuo = $scope.aporteNormal.oPortafolio.seleccionado.CodigoPortafolio;
				$scope.aporteNormal.formData.RegimenDefecto = $scope.aporteNormal.formData.IdRegimenFondoMutuo;
		}
	};
	
});