angular.module('ModuloFFM').controller('IngresarRescateTraspasoFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateTraspaso = this;             
	$scope.rescateTraspaso.formData = {};
	$scope.rescateTraspaso.masInformacion = false;
	$scope.rescateTraspaso.formData.setInstrumento = "-1";
	$scope.rescateTraspaso.datosFondo = {};
	$scope.rescateTraspaso.formData.IngresoConfirmacion = 'I';
	$scope.rescateTraspaso.formData.AporteRescate = 'A';
	$scope.rescateTraspaso.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateTraspaso.formData.DatosAdicionales = "N";
	$scope.rescateTraspaso.formData.visibleTruco = true;
	rescateTraspaso.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.rescateTraspaso.formData.idAutorizadoDarOrdenesDefecto = undefined;
	$scope.rescateTraspaso.formData.idAutorizadoDarOrdenesDestinoDefecto = undefined;
	$scope.rescateTraspaso.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateTraspaso.ajustaVentana();
	
	$scope.rescateTraspaso.formData.oConfiguracionTipoRescate={
			tipo : "combobox-tipo-rescate-ffmm",
			configuracion : {},
			filtros : {
				Estado : { "field"   : "Estado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.rescateTraspaso.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
		
	$scope.rescateTraspaso.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.rescateTraspaso.formData.FiltrosBusquedaFondoDestino = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.rescateTraspaso.formData.FiltrosComboFondo = {
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' }
	};
	
	$scope.rescateTraspaso.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}			
		};
	
	$scope.rescateTraspaso.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
	
	$scope.rescateTraspaso.formData.FiltrosAutorizarOrdenDestino = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
	
	$scope.rescateTraspaso.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.rescateTraspaso.formData.filtrosFormaPagoDestino = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.rescateTraspaso.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.rescateTraspaso.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
	
	$scope.rescateTraspaso.oPortafolioDestino={
			 tipo : "combobox-portafolio",
			 configuracion :{},
			 idActual : false,
			 seleccionado : false,
			 filtros: {
			 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			 },
		 };
	
	$scope.rescateTraspaso.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
		
	 $scope.rescateTraspaso.formData.ingresarAporteRescate = function()
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
						IdCliente 	          		:	$scope.rescateTraspaso.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateTraspaso.oPortafolio.seleccionado.IdPortafolio,
				    	IdSerieFondo          		:	$scope.rescateTraspaso.datosFondo.IdSerieFondo,
				    	IdPortafolioDestino         :   $scope.rescateTraspaso.oPortafolioDestino.seleccionado.IdPortafolio,
				    	CalculoPorMontoCuota  		:	rescateTraspaso.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'RES' ? $scope.rescateTraspaso.formData.TipoRescate : 'M',
				    	Monto 				  		:   $scope.rescateTraspaso.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateTraspaso.formData.numeroCuotas,
						IdRegimenTributario			:	$scope.rescateTraspaso.formData.TipoRescate !== 'T' ? $scope.rescateTraspaso.formData.IdRegimenFondoMutuo : '',
						IdMoneda 			  		:	rescateTraspaso.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.rescateTraspaso.formData.IdMonedaAporte : $scope.rescateTraspaso.datosFondo.IdMoneda,
				    	IdMedioDePago				:	$scope.rescateTraspaso.formData.IdMedioDePago ? $scope.rescateTraspaso.formData.IdMedioDePago : 'CIN',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.rescateTraspaso.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.rescateTraspaso.formData.idAutorizadoDarOrdenes || 0,
				    	IdAutorizadoDestino			:	$scope.rescateTraspaso.formData.idAutorizadoDarOrdenesDestino || 0,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudRescateTraspaso(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateTraspaso.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoRescate	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateTraspaso.formData.decimalesCantidad),
						TipoRescate                     : $scope.rescateTraspaso.formData.TipoRescateSeleccionado.NombreConcepto,
	     		};
	     		$scope.rescateTraspaso.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateTraspaso.ajustaVentana();
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
	 
	 $scope.$watch('rescateTraspaso.oPortafolio.idActual',function(newval,oldval){
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
					 $scope.rescateTraspaso.oPortafolio.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateTraspaso.oPortafolioDestino.idActual',function(newval,oldval){
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
					 $scope.rescateTraspaso.oPortafolioDestino.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateTraspaso.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.rescateTraspaso.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.rescateTraspaso.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateTraspaso.formData.montoInicial,$scope.rescateTraspaso.formData.numeroCuotas,'RES',$scope.rescateTraspaso.datosFondo.IdMoneda,$scope.rescateTraspaso.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTraspaso.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTraspaso.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTraspaso.formData.numeroCuotas = 0;
						 $scope.rescateTraspaso.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTraspaso.formData.numeroCuotas = 0;
					 $scope.rescateTraspaso.formData.montoInicial = 0;
		  		 });
				$scope.rescateTraspaso.formData.IdMonedaAporte = newval.IdMoneda;
		    }
			else {
				$scope.rescateTraspaso.formData.decimalesCantidad = 0;
				$scope.rescateTraspaso.formData.numeroCuotas = 0;
				$scope.rescateTraspaso.formData.montoInicial = 0;
				$scope.rescateTraspaso.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.rescateTraspaso.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTraspaso.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.rescateTraspaso.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.rescateTraspaso.formData.decimalesMonto = 0;
		 }
		 $scope.rescateTraspaso.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTraspaso.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.rescateTraspaso.datosFondo,newval,$scope.rescateTraspaso.formData.numeroCuotas,'RES',$scope.rescateTraspaso.datosFondo.IdMoneda,$scope.rescateTraspaso.formData.TipoRescate,$scope.SesionFechaSistema)
			.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTraspaso.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTraspaso.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTraspaso.formData.numeroCuotas = 0;
						 $scope.rescateTraspaso.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTraspaso.formData.numeroCuotas = 0;
					 $scope.rescateTraspaso.formData.montoInicial = 0;
		  		 });
		}
	 });
	 
	 $scope.$watch('rescateTraspaso.masInformacion',function(newval,oldval){
		 $scope.rescateTraspaso.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTraspaso.formData.TipoRescate',function(newval,oldval){
		 if(newval === 'T'){
			 $scope.rescateTraspaso.formData.numeroCuotas = $scope.rescateTraspaso.formData.SaldoCuotas;
			 $scope.rescateTraspaso.formData.montoInicial = $scope.rescateTraspaso.formData.SaldoMonto;
		 }else{
			 $scope.rescateTraspaso.formData.numeroCuotas = 0;
			 $scope.rescateTraspaso.formData.montoInicial = 0;
		 }
		 
	 });
	 
	 $scope.$watch('rescateTraspaso.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateTraspaso.datosFondo,$scope.rescateTraspaso.formData.montoInicial,newval,'RES',$scope.rescateTraspaso.datosFondo.IdMoneda,$scope.rescateTraspaso.formData.TipoRescate,$scope.SesionFechaSistema)
			 .then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTraspaso.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTraspaso.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTraspaso.formData.numeroCuotas = 0;
						 $scope.rescateTraspaso.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTraspaso.formData.numeroCuotas = 0;
					 $scope.rescateTraspaso.formData.montoInicial = 0;
		  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateTraspaso.formData.montoInicial = undefined;
				 $scope.rescateTraspaso.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateTraspaso.formData.AporteRescate = 'A';
				$scope.rescateTraspaso.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateTraspaso.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateTraspaso.ajustaVentana();
		 }
		 
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('rescateTraspaso.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateTraspaso.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateTraspaso.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateTraspaso.formData.FiltrosBusquedaFondoDestino.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateTraspaso.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.rescateTraspaso.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateTraspaso.formData.tipoOperacion = 0;
			 $scope.rescateTraspaso.formData.AporteRescate = 'A';
		 }
		 $scope.rescateTraspaso.formData.idAutorizadoDarOrdenes = undefined;
		 $scope.rescateTraspaso.formData.idAutorizadoDarOrdenesDestino = undefined;
		 $scope.rescateTraspaso.formData.IngresoConfirmacion = 'I';
		 $scope.rescateTraspaso.formData.TipoRescate = 'M';
		 $scope.rescateTraspaso.oSocioNegocio.idActual = 0;
		 $scope.rescateTraspaso.oPortafolio.idActual = 0;
		 $scope.rescateTraspaso.oPortafolioDestino.idActual = 0;
		 $scope.rescateTraspaso.formData.setFondo = '-1';
		 $scope.rescateTraspaso.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateTraspaso.formData.montoInicial = undefined;
		 $scope.rescateTraspaso.formData.numeroCuotas = undefined;
		 $scope.rescateTraspaso.formData.montoComisiones = undefined;
		 $scope.rescateTraspaso.formData.IdFormaPago = '0';
		 $scope.rescateTraspaso.formData.comentario = '';
		 $scope.rescateTraspaso.formData.FiltrosBusquedaFondoDestino = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateTraspaso.formData.FiltrosBusquedaFondo = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateTraspaso.ajustaVentana();
	 }
	 
	 
	 $scope.rescateTraspaso.formData.resetFoco =  function() {
	 	$scope.rescateTraspaso.formData.focoVolver = false;
		if($scope.rescateTraspaso.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateTraspaso.formData.focoOperacion = true;
			$scope.rescateTraspaso.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateTraspaso.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
		 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
		 						,IdUsuario			: $scope.SesionIdUsuario
		 						,IdModulo			: "FMM"
		 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
		 						,IdCliente			: $scope.rescateTraspaso.oPortafolio.seleccionado.IdCliente
		 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
								,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
								,IdMedioDePago		: $scope.rescateTraspaso.formData.IdMedioDePago ? $scope.rescateTraspaso.formData.IdMedioDePago : 'CIN'
								,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
							 };
			 
			 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
		 };

});