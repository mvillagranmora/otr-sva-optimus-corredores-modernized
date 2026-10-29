angular.module('ModuloFFM').controller('IngresarRescateTransferenciaFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateTransferencia = this;             
	$scope.rescateTransferencia.formData = {};
	$scope.rescateTransferencia.masInformacion = false;
	$scope.rescateTransferencia.formData.setInstrumento = "-1";
	$scope.rescateTransferencia.datosFondo = {};
	$scope.rescateTransferencia.formData.IngresoConfirmacion = 'I';
	$scope.rescateTransferencia.formData.AporteRescate = 'A';
	$scope.rescateTransferencia.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateTransferencia.formData.DatosAdicionales = "N";
	$scope.rescateTransferencia.formData.visibleTruco = true;
	rescateTransferencia.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.rescateTransferencia.formData.idAutorizadoDarOrdenesDefecto = undefined;
	$scope.rescateTransferencia.formData.idAutorizadoDarOrdenesDestinoDefecto = undefined;
	$scope.rescateTransferencia.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateTransferencia.ajustaVentana();
	
	$scope.rescateTransferencia.formData.oConfiguracionTipoRescate={
			tipo : "combobox-tipo-rescate-ffmm",
			configuracion : {},
			filtros : {
				Estado : { "field"   : "Estado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.rescateTransferencia.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
	
	$scope.rescateTransferencia.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   : 0  }			
		};
	$scope.rescateTransferencia.formData.FiltrosAutorizarOrdenDestino = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0 },
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
	
	$scope.rescateTransferencia.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0 },
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
	
	
		
	$scope.rescateTransferencia.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			
	};
	
	$scope.rescateTransferencia.formData.FiltrosBusquedaFondoDestino = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			
	};
	
	$scope.rescateTransferencia.formData.FiltrosComboFondo = {
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' }
	};
	
	$scope.rescateTransferencia.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : 0  },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.rescateTransferencia.formData.filtrosFormaPagoDestino = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	$scope.rescateTransferencia.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
	$scope.rescateTransferencia.oSocioNegocioDestino={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
	$scope.rescateTransferencia.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
	
	$scope.rescateTransferencia.oPortafolioDestino={
			 tipo : "combobox-portafolio",
			 configuracion :{},
			 idActual : false,
			 seleccionado : false,
			 filtros: {
			 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			 },
		 };
	
	$scope.rescateTransferencia.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
		
	 $scope.rescateTransferencia.formData.ingresarAporteRescate = function()
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
						IdCliente 	          		:	$scope.rescateTransferencia.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateTransferencia.oPortafolio.seleccionado.IdPortafolio,
				    	IdSerieFondo          		:	$scope.rescateTransferencia.datosFondo.IdSerieFondo,
				    	IdClienteDestino            :   $scope.rescateTransferencia.oPortafolioDestino.seleccionado.IdCliente,
				    	IdPortafolioDestino         :   $scope.rescateTransferencia.oPortafolioDestino.seleccionado.IdPortafolio,
				    	CalculoPorMontoCuota  		:	rescateTransferencia.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'RES' ? $scope.rescateTransferencia.formData.TipoRescate : 'M',
				    	Monto 				  		:   $scope.rescateTransferencia.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateTransferencia.formData.numeroCuotas,
						

						// Ahora en los rescates totales tambien se especifica regimen tributario

						IdRegimenTributario			:	$scope.rescateTransferencia.formData.IdRegimenFondoMutuo || '',
						
						IdMoneda 			  		:	rescateTransferencia.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.rescateTransferencia.formData.IdMonedaAporte : $scope.rescateTransferencia.datosFondo.IdMoneda,
				    	IdMedioDePago				:	$scope.rescateTransferencia.formData.IdMedioDePago ? $scope.rescateTransferencia.formData.IdMedioDePago : 'CIN',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.rescateTransferencia.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.rescateTransferencia.formData.idAutorizadoDarOrdenes || 0,
				    	IdAutorizadoDestino			:	$scope.rescateTransferencia.formData.idAutorizadoDarOrdenesDestino || 0,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudRescateTransferencia(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateTransferencia.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoRescate	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateTransferencia.formData.decimalesCantidad),
						TipoRescate                     : $scope.rescateTransferencia.formData.TipoRescateSeleccionado.NombreConcepto,
	     		};
	     		$scope.rescateTransferencia.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateTransferencia.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		if(id){
	     			var promesa=bcsComunes.ventanaPromesa(id); 
		     		$.when(promesa).then(function(data){  
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
	 
	 $scope.$watch('rescateTransferencia.oPortafolio.idActual',function(newval,oldval){
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
					 $scope.rescateTransferencia.oPortafolio.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateTransferencia.oPortafolioDestino.idActual',function(newval,oldval){
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
					 $scope.rescateTransferencia.oPortafolioDestino.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateTransferencia.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.rescateTransferencia.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.rescateTransferencia.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateTransferencia.formData.montoInicial,$scope.rescateTransferencia.formData.numeroCuotas,'RES',$scope.rescateTransferencia.datosFondo.IdMoneda,$scope.rescateTransferencia.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTransferencia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTransferencia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTransferencia.formData.numeroCuotas = 0;
						 $scope.rescateTransferencia.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTransferencia.formData.numeroCuotas = 0;
					 $scope.rescateTransferencia.formData.montoInicial = 0;
		  		 });
				$scope.rescateTransferencia.formData.IdMonedaAporte = newval.IdMoneda;
		    }
			else {
				$scope.rescateTransferencia.formData.decimalesCantidad = 0;
				$scope.rescateTransferencia.formData.numeroCuotas = 0;
				$scope.rescateTransferencia.formData.montoInicial = 0;
				$scope.rescateTransferencia.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.rescateTransferencia.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTransferencia.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.rescateTransferencia.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.rescateTransferencia.formData.decimalesMonto = 0;
		 }
		 $scope.rescateTransferencia.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTransferencia.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.rescateTransferencia.datosFondo,newval,$scope.rescateTransferencia.formData.numeroCuotas,'RES',$scope.rescateTransferencia.datosFondo.IdMoneda,$scope.rescateTransferencia.formData.TipoRescate,$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.rescateTransferencia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.rescateTransferencia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.rescateTransferencia.formData.numeroCuotas = 0;
					 $scope.rescateTransferencia.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.rescateTransferencia.formData.numeroCuotas = 0;
				 $scope.rescateTransferencia.formData.montoInicial = 0;
	  		 });
		}
	 });
	 
	 $scope.$watch('rescateTransferencia.masInformacion',function(newval,oldval){
		 $scope.rescateTransferencia.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTransferencia.formData.TipoRescate',function(newval,oldval){
		 if(newval === 'T'){
			 $scope.rescateTransferencia.formData.numeroCuotas = $scope.rescateTransferencia.formData.SaldoCuotas;
			 $scope.rescateTransferencia.formData.montoInicial = $scope.rescateTransferencia.formData.SaldoMonto;
		 }else{
			 $scope.rescateTransferencia.formData.numeroCuotas = 0;
			 $scope.rescateTransferencia.formData.montoInicial = 0;
		 }
		 
	 });
	 

	 $scope.$watch('rescateTransferencia.formData.SaldoCuotas', function(newval,oldval){	 	
		 $scope.rescateTransferencia.formData.numeroCuotas = $scope.rescateTransferencia.formData.SaldoCuotas;
		 $scope.rescateTransferencia.formData.montoInicial = $scope.rescateTransferencia.formData.SaldoMonto;		 
	 });
	 
	 $scope.$watch('rescateTransferencia.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateTransferencia.datosFondo,$scope.rescateTransferencia.formData.montoInicial,newval,'RES',$scope.rescateTransferencia.datosFondo.IdMoneda,$scope.rescateTransferencia.formData.TipoRescate,$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.rescateTransferencia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.rescateTransferencia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.rescateTransferencia.formData.numeroCuotas = 0;
					 $scope.rescateTransferencia.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.rescateTransferencia.formData.numeroCuotas = 0;
				 $scope.rescateTransferencia.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('rescateTransferencia.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateTransferencia.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateTransferencia.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateTransferencia.formData.FiltrosBusquedaFondoDestino.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateTransferencia.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateTransferencia.formData.montoInicial = undefined;
				 $scope.rescateTransferencia.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateTransferencia.formData.AporteRescate = 'A';
				$scope.rescateTransferencia.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateTransferencia.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateTransferencia.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.rescateTransferencia.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateTransferencia.formData.tipoOperacion = 0;
			 $scope.rescateTransferencia.formData.AporteRescate = 'A';
		 }
		 $scope.rescateTransferencia.formData.IngresoConfirmacion = 'I';
		 $scope.rescateTransferencia.formData.TipoRescate = 'M';
		 $scope.rescateTransferencia.oSocioNegocio.idActual = 0;
		 $scope.rescateTransferencia.oSocioNegocioDestino.idActual = 0;
		 $scope.rescateTransferencia.oPortafolio.idActual = 0;
		 $scope.rescateTransferencia.oPortafolioDestino.idActual = 0;
		 $scope.rescateTransferencia.formData.setFondo = '-1';
		 $scope.rescateTransferencia.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateTransferencia.formData.montoInicial = undefined;
		 $scope.rescateTransferencia.formData.numeroCuotas = undefined;
		 $scope.rescateTransferencia.formData.montoComisiones = undefined;
		 $scope.rescateTransferencia.formData.IdFormaPago = '0';
		 $scope.rescateTransferencia.formData.comentario = '';
		 $scope.rescateTransferencia.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.rescateTransferencia.formData.idAutorizadoDarOrdenesDestino=undefined;
		 $scope.rescateTransferencia.formData.FiltrosBusquedaFondo = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateTransferencia.formData.FiltrosBusquedaFondoDestino = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateTransferencia.ajustaVentana();
	 }
	 	 
	 $scope.rescateTransferencia.formData.resetFoco =  function() {
	 	$scope.rescateTransferencia.formData.focoVolver = false;
		if($scope.rescateTransferencia.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateTransferencia.formData.focoOperacion = true;
			$scope.rescateTransferencia.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateTransferencia.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
		 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
		 						,IdUsuario			: $scope.SesionIdUsuario
		 						,IdModulo			: "FMM"
		 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
		 						,IdCliente			: $scope.rescateTransferencia.oPortafolio.seleccionado.IdCliente
		 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
								,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
								,IdMedioDePago		: $scope.rescateTransferencia.formData.IdMedioDePago ? $scope.rescateTransferencia.formData.IdMedioDePago : 'CIN'
								,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
							 };
			 
			 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
		 };
});