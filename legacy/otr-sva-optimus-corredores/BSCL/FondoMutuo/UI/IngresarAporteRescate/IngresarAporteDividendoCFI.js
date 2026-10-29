angular.module('ModuloFFM').controller('IngresarAporteDividendoCFICtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServicioPlataforma){
	$scope.ingresoAporteDividendoCFI.ConfiguracionFM = {};
	$scope.ingresoAporteDividendoCFI.ConsultarParametroFondoMutuo = bcsConfiguracionFFM.getDataSourceConsultarParametroFondoMutuo();
	
	$scope.ingresoAporteDividendoCFI.formData = {};
	$scope.ingresoAporteDividendoCFI.filtros = {};
	$scope.ingresoAporteDividendoCFI.masInformacion = false;
	$scope.ingresoAporteDividendoCFI.formData.setInstrumento = "-1";
	$scope.ingresoAporteDividendoCFI.datosFondo = {};
	$scope.ingresoAporteDividendoCFI.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.ingresoAporteDividendoCFI.formData.DatosAdicionales = "N";
	$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	
	
	$scope.ingresoAporteDividendoCFI.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,15);
    }

	$scope.ingresoAporteDividendoCFI.oClientePortafolio ={
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.ingresoAporteDividendoCFI.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
	
	$scope.ingresoAporteDividendoCFI.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
	
	$scope.ingresoAporteDividendoCFI.formData.FiltrosBusquedaFondo = {
		IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
		IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.ingresoAporteDividendoCFI.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.ingresoAporteDividendoCFI.formData.filtrosRegimen = {
		IdSerieFondo : {"field":"IdSerieFondo", "operator": "eq", "value":undefined},
		IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
		IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
		IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
	
	$scope.ingresoAporteDividendoCFI.formData.masDatos = function(){
		 if ($scope.ingresoAporteDividendoCFI.formData.DatosAdicionales === 'S'){
			 $scope.ingresoAporteDividendoCFI.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.ingresoAporteDividendoCFI.formData.DatosAdicionales = 'S';
	     }
	}
	
	$scope.ingresoAporteDividendoCFI.configuracionFMM = function(){
		 var filters= [ { field: "NombreTipoParametro",operator: "eq", value: 'APORTE_GENERA_COMPRA_SPOT_DOLAR'},
	 					{ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
	 					{ field: "IdEstado",		operator: "eq", value: 'VIG' } ];
		  
	 	$scope.ingresoAporteDividendoCFI.ConsultarParametroFondoMutuo.query({ filter: filters }).then(function(){
	 		if($scope.ingresoAporteDividendoCFI.ConsultarParametroFondoMutuo.ds._data.length > 0) {
	 			$scope.ingresoAporteDividendoCFI.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR = $scope.ingresoAporteDividendoCFI.ConsultarParametroFondoMutuo.ds._data[0].ValorParametro;
	 		}else{
				$scope.ingresoAporteDividendoCFI.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR ='N';
			};
	 	});
	};
	
	$scope.$watch('ingresoAporteDividendoCFI.oPortafolio.idActual',function(newval){
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
					$scope.ingresoAporteDividendoCFI.filtros.IdPortafolio = 0;
					bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			})
		}	 
	});
	
	$scope.$watch('ingresoAporteDividendoCFI.datosFondo',function(newval){
		 if (newval && newval.IdFondo) {
			$scope.ingresoAporteDividendoCFI.formData.decimalesCantidad = newval.DecimalesCuota;
			$scope.ingresoAporteDividendoCFI.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
			$scope.ingresoAporteDividendoCFI.ConfiguracionFM.MOSTRAR_MONEDA_PAGO = 'N';
			if(newval.IdMoneda !== $scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema && $scope.ingresoAporteDividendoCFI.ConfiguracionFM.APORTE_GENERA_COMPRA_SPOT_DOLAR === 'S'){
				$scope.ingresoAporteDividendoCFI.ConfiguracionFM.MOSTRAR_MONEDA_PAGO = 'S';
			}
			bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.ingresoAporteDividendoCFI.formData.montoInicial,$scope.ingresoAporteDividendoCFI.formData.numeroCuotas,'APO',newval.IdMoneda,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.ingresoAporteDividendoCFI.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.ingresoAporteDividendoCFI.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 $scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					 $scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;
					 
				 }else{
					 $scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
					 $scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
					 $scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
					 $scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
				 }
				
			},function(){
				 $scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
				 $scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
				 $scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
				 $scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	  		});
			$scope.ingresoAporteDividendoCFI.formData.IdMonedaAporte = newval.IdMoneda;
			$scope.asignarRegimenPorDefecto();
		}
		else {
			$scope.ingresoAporteDividendoCFI.formData.decimalesCantidad = 0;
			$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
			$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
			$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
			$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
			$scope.ingresoAporteDividendoCFI.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		}
		 $scope.ingresoAporteDividendoCFI.ajustaVentana();
	});
	
	$scope.$watch('ingresoAporteDividendoCFI.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.ingresoAporteDividendoCFI.datosFondo,newval,$scope.ingresoAporteDividendoCFI.formData.numeroCuotas,'APO',$scope.ingresoAporteDividendoCFI.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					$scope.ingresoAporteDividendoCFI.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;					 
				}else{
					$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
					$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
					$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
					$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema; 
				}
			},function(){
				$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
				$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
				$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
				$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	  		});
		}
	});
	
	$scope.$watch('ingresoAporteDividendoCFI.formData.MontoMonedaSistema',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.ingresoAporteDividendoCFI.datosFondo,newval,$scope.ingresoAporteDividendoCFI.formData.numeroCuotas,'APO',$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					$scope.ingresoAporteDividendoCFI.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].Monto;
					$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;					 
				}else{
					$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
					$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
					$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
					$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
				}
			 },function(){
				$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
				$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
				$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
				$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	  		});
		}
	});
	
	$scope.$watch('ingresoAporteDividendoCFI.masInformacion',function(newval,oldval){
		$scope.ingresoAporteDividendoCFI.ajustaVentana();
	});
	
	$scope.$watch('ingresoAporteDividendoCFI.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.ingresoAporteDividendoCFI.datosFondo,$scope.ingresoAporteDividendoCFI.formData.montoInicial,newval,'APO',$scope.ingresoAporteDividendoCFI.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					$scope.ingresoAporteDividendoCFI.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
					$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;
				}else{
					$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
					$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
					$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
					$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
				}
			},function(){
				$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = 0;
				$scope.ingresoAporteDividendoCFI.formData.montoInicial = 0;
				$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema = 0;
				$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	  		});
		}
	});
	
	$scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		if (newval && newval.IdTipoMovimientoFondoMutuo){
			if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				$scope.ingresoAporteDividendoCFI.formData.montoInicial = undefined;
				$scope.ingresoAporteDividendoCFI.formData.numeroCuotas = undefined;
				$scope.ingresoAporteDividendoCFI.formData.MontoMonedaSistema =  undefined;
				$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
			}
			$scope.ingresoAporteDividendoCFI.ajustaVentana();
		}
	});
	
	$scope.ingresoAporteDividendoCFI.formData.resetFoco =  function() {
	 	$scope.ingresoAporteDividendoCFI.formData.focoVolver = false;
		if($scope.ingresoAporteDividendoCFI.formData.IngresoConfirmacion === 'I'){
			$scope.ingresoAporteDividendoCFI.formData.focoOperacion = true;
			$scope.ingresoAporteDividendoCFI.formData.focoVolver = false;
		}
	};
	
	$scope.ingresoAporteDividendoCFI.formData.ingresarAporte = function(){	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporte = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesAporte : [{
					    IdInterno		      		:	1,
						IdCliente 	          		:	$scope.ingresoAporteDividendoCFI.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.ingresoAporteDividendoCFI.oClientePortafolio.seleccionado.IdPortafolio,
				    	IdSerieFondo          		:	$scope.ingresoAporteDividendoCFI.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	'M',
				    	Monto 				  		:   $scope.ingresoAporteDividendoCFI.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.ingresoAporteDividendoCFI.formData.numeroCuotas,
				    	IdMoneda 			  		:	$scope.ingresoAporteDividendoCFI.formData.IdMonedaAporte,
				    	IdRegimenTributario			:	$scope.ingresoAporteDividendoCFI.formData.IdRegimenFondoMutuo || '',
				    	Glosa						: 	$scope.ingresoAporteDividendoCFI.formData.comentario
				 }]
		 }
		 
	     bcsServicioFondosMutuos.CrearSolicitudAportePorDividendosCFI(datosIngresoAporte).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		
	     		 $scope.ingresoAporteDividendoCFI.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.ingresoAporteDividendoCFI.formData.decimalesCantidad),
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
	     		$scope.ingresoAporteDividendoCFI.formData.IngresoConfirmacion = 'C';
	     		$scope.ingresoAporteDividendoCFI.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		if(id){
	     			var promesa=bcsComunes.ventanaPromesa(id); 
		     		$.when(promesa).then(function(){
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
	
	$scope.ingresoAporteDividendoCFI.limpiarFMM = function(){
		 $scope.ingresoAporteDividendoCFI.formData.IngresoConfirmacion = 'I';
		 $scope.ingresoAporteDividendoCFI.filtros.IdPortafolio = 0;
		 $scope.ingresoAporteDividendoCFI.formData.setFondo = '-1';
		 $scope.ingresoAporteDividendoCFI.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.ingresoAporteDividendoCFI.formData.montoInicial = undefined;
		 $scope.ingresoAporteDividendoCFI.formData.numeroCuotas = undefined;
		 $scope.ingresoAporteDividendoCFI.formData.comentario = '';
		$scope.ingresoAporteDividendoCFI.formData.IdMonedaSistema = $scope.SesionIdMonedaSistema;
		 $scope.ingresoAporteDividendoCFI.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.ingresoAporteDividendoCFI.ajustaVentana();
	}
	
	$scope.ingresoAporteDividendoCFI.limpiarFMM();
	$scope.ingresoAporteDividendoCFI.configuracionFMM();

	$scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.ingresoAporteDividendoCFI.oClientePortafolio.seleccionado,
			$scope.ingresoAporteDividendoCFI.datosFondo,
			$scope.ingresoAporteDividendoCFI.formData
		);
	};
	
});