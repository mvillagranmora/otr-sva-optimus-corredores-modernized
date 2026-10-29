angular.module('ModuloFFM').controller('IngresarRescateTraspasoPortafolioFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateTraspasoPortafolio = this;             
	$scope.rescateTraspasoPortafolio.formData = {};
	$scope.rescateTraspasoPortafolio.masInformacion = false;
	$scope.rescateTraspasoPortafolio.formData.setInstrumento = "-1";
	$scope.rescateTraspasoPortafolio.datosFondo = {};
	$scope.rescateTraspasoPortafolio.formData.IngresoConfirmacion = 'I';
	$scope.rescateTraspasoPortafolio.formData.AporteRescate = 'A';
	$scope.rescateTraspasoPortafolio.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateTraspasoPortafolio.formData.DatosAdicionales = "N";
	$scope.rescateTraspasoPortafolio.formData.visibleTruco = true;
	$scope.rescateTraspasoPortafolio.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.rescateTraspasoPortafolio.formData.idAutorizadoDarOrdenesDefecto = undefined;
	$scope.rescateTraspasoPortafolio.formData.idAutorizadoDarOrdenesDestinoDefecto = undefined;
	$scope.rescateTraspasoPortafolio.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateTraspasoPortafolio.ajustaVentana();
	
	$scope.rescateTraspasoPortafolio.oMotivoRetiro = {};
	$scope.oConfiguracionMotivoRetiro = {
		filtros: {	
			IdEstado: { field: 'IdEstado', operator: "eq", value:'ACT' },
			Visible: { field: 'Visible', operator: "eq", value:'S' },
			IdTipoMovimientoFondoMutuo: {field:'IdTipoMovimientoFondoMutuo', operator: 'eq', value: $scope.formData.tipoOperacion} 
		}
	};

	$scope.rescateTraspasoPortafolio.formData.oConfiguracionTipoRescate={
			tipo : "combobox-tipo-rescate-ffmm",
			configuracion : {},
			filtros : {
				Estado : { "field"   : "Estado","operator": "eq", "value"   : "ACT" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.rescateTraspasoPortafolio.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
		
	$scope.rescateTraspasoPortafolio.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.rescateTraspasoPortafolio.formData.FiltrosBusquedaFondoDestino = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.rescateTraspasoPortafolio.formData.FiltrosComboFondo = {
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' }
	};
	
	$scope.rescateTraspasoPortafolio.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}			
		};
	
	$scope.rescateTraspasoPortafolio.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
	
	$scope.rescateTraspasoPortafolio.formData.FiltrosAutorizarOrdenDestino = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
	
	$scope.rescateTraspasoPortafolio.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.rescateTraspasoPortafolio.formData.filtrosFormaPagoDestino = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.rescateTraspasoPortafolio.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.rescateTraspasoPortafolio.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
	
	$scope.rescateTraspasoPortafolio.oPortafolioDestino={
			 tipo : "combobox-portafolio",
			 configuracion :{},
			 idActual : false,
			 seleccionado : false,
			 filtros: {
			 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			 },
		 };
	
	$scope.rescateTraspasoPortafolio.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
		
	 $scope.rescateTraspasoPortafolio.formData.ingresarAporteRescate = function()
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
						IdCliente 	          		:	$scope.rescateTraspasoPortafolio.oPortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateTraspasoPortafolio.oPortafolio.seleccionado.IdPortafolio,
				    	IdSerieFondo          		:	$scope.rescateTraspasoPortafolio.datosFondo.IdSerieFondo,
				    	IdPortafolioDestino         :   $scope.rescateTraspasoPortafolio.oPortafolioDestino.seleccionado.IdPortafolio,
				    	CalculoPorMontoCuota  		:	$scope.rescateTraspasoPortafolio.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'RES' ? $scope.rescateTraspasoPortafolio.formData.TipoRescate : 'M',
				    	Monto 				  		:   $scope.rescateTraspasoPortafolio.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateTraspasoPortafolio.formData.numeroCuotas,
						
					
					
						// Ahora en los rescates totales tambien se especifica regimen tributario
						IdRegimenTributario			:	$scope.rescateTraspasoPortafolio.formData.IdRegimenFondoMutuo || '',
								
						
						IdMoneda 			  		:	rescateTraspasoPortafolio.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.rescateTraspasoPortafolio.formData.IdMonedaAporte : $scope.rescateTraspasoPortafolio.datosFondo.IdMoneda,
				    	IdMedioDePago				:	$scope.rescateTraspasoPortafolio.formData.IdMedioDePago ? $scope.rescateTraspasoPortafolio.formData.IdMedioDePago : 'CIN',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.rescateTraspasoPortafolio.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.rescateTraspasoPortafolio.formData.idAutorizadoDarOrdenes || 0,
				    	IdAutorizadoDestino			:	$scope.rescateTraspasoPortafolio.formData.idAutorizadoDarOrdenesDestino || 0,
						FolioContrato				:	0,
						IdMotivoRetiro				:   $scope.rescateTraspasoPortafolio.oMotivoRetiro.Seleccionado.IdMotivo
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudRescateTraspasoPortafolio(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateTraspasoPortafolio.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoRescate	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateTraspasoPortafolio.formData.decimalesCantidad),
						TipoRescate                     : $scope.rescateTraspasoPortafolio.formData.TipoRescateSeleccionado.NombreConcepto,
	     		};
	     		$scope.rescateTraspasoPortafolio.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateTraspasoPortafolio.ajustaVentana();
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
	 
	 $scope.$watch('rescateTraspasoPortafolio.oPortafolio.idActual',function(newval,oldval){
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
					 $scope.rescateTraspasoPortafolio.oPortafolio.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateTraspasoPortafolio.oPortafolioDestino.idActual',function(newval,oldval){
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
					 $scope.rescateTraspasoPortafolio.oPortafolioDestino.idActual = 0;
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('rescateTraspasoPortafolio.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.rescateTraspasoPortafolio.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.rescateTraspasoPortafolio.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateTraspasoPortafolio.formData.montoInicial,$scope.rescateTraspasoPortafolio.formData.numeroCuotas,'RES',$scope.rescateTraspasoPortafolio.datosFondo.IdMoneda,$scope.rescateTraspasoPortafolio.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTraspasoPortafolio.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
						 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
					 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
		  		 });
				$scope.rescateTraspasoPortafolio.formData.IdMonedaAporte = newval.IdMoneda;
		    }
			else {
				$scope.rescateTraspasoPortafolio.formData.decimalesCantidad = 0;
				$scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
				$scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
				$scope.rescateTraspasoPortafolio.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.rescateTraspasoPortafolio.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTraspasoPortafolio.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.rescateTraspasoPortafolio.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.rescateTraspasoPortafolio.formData.decimalesMonto = 0;
		 }
		 $scope.rescateTraspasoPortafolio.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTraspasoPortafolio.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.rescateTraspasoPortafolio.datosFondo,newval,$scope.rescateTraspasoPortafolio.formData.numeroCuotas,'RES',$scope.rescateTraspasoPortafolio.datosFondo.IdMoneda,$scope.rescateTraspasoPortafolio.formData.TipoRescate,$scope.SesionFechaSistema)
			.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTraspasoPortafolio.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
						 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
					 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
		  		 });
		}
	 });
	 
	 $scope.$watch('rescateTraspasoPortafolio.masInformacion',function(newval,oldval){
		 $scope.rescateTraspasoPortafolio.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateTraspasoPortafolio.formData.TipoRescate',function(newval,oldval){
		 if(newval === 'T'){
			 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = $scope.rescateTraspasoPortafolio.formData.SaldoCuotas;
			 $scope.rescateTraspasoPortafolio.formData.montoInicial = $scope.rescateTraspasoPortafolio.formData.SaldoMonto;
		 }else{
			 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
			 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
		 }
		 
	 });
	 
	 
	 $scope.$watch('rescateTraspasoPortafolio.formData.SaldoCuotas', function(newval,oldval){	 	
		 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = $scope.rescateTraspasoPortafolio.formData.SaldoCuotas;
		 $scope.rescateTraspasoPortafolio.formData.montoInicial = $scope.rescateTraspasoPortafolio.formData.SaldoMonto;			 
	 });
	 
	 
	 $scope.$watch('rescateTraspasoPortafolio.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateTraspasoPortafolio.datosFondo,$scope.rescateTraspasoPortafolio.formData.montoInicial,newval,'RES',$scope.rescateTraspasoPortafolio.datosFondo.IdMoneda,$scope.rescateTraspasoPortafolio.formData.TipoRescate,$scope.SesionFechaSistema)
			 .then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateTraspasoPortafolio.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
						 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = 0;
					 $scope.rescateTraspasoPortafolio.formData.montoInicial = 0;
		  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateTraspasoPortafolio.formData.montoInicial = undefined;
				 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateTraspasoPortafolio.formData.AporteRescate = 'A';
				$scope.rescateTraspasoPortafolio.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateTraspasoPortafolio.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateTraspasoPortafolio.ajustaVentana();
		 }
		 
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('rescateTraspasoPortafolio.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateTraspasoPortafolio.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateTraspasoPortafolio.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateTraspasoPortafolio.formData.FiltrosBusquedaFondoDestino.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateTraspasoPortafolio.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.rescateTraspasoPortafolio.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateTraspasoPortafolio.formData.tipoOperacion = 0;
			 $scope.rescateTraspasoPortafolio.formData.AporteRescate = 'A';
		 }
		 $scope.rescateTraspasoPortafolio.formData.idAutorizadoDarOrdenes = undefined;
		 $scope.rescateTraspasoPortafolio.formData.idAutorizadoDarOrdenesDestino = undefined;
		 $scope.rescateTraspasoPortafolio.formData.IngresoConfirmacion = 'I';
		 $scope.rescateTraspasoPortafolio.formData.TipoRescate = 'M';
		 $scope.rescateTraspasoPortafolio.oSocioNegocio.idActual = 0;
		 $scope.rescateTraspasoPortafolio.oPortafolio.idActual = 0;
		 $scope.rescateTraspasoPortafolio.oPortafolioDestino.idActual = 0;
		 $scope.rescateTraspasoPortafolio.formData.setFondo = '-1';
		 $scope.rescateTraspasoPortafolio.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateTraspasoPortafolio.formData.montoInicial = undefined;
		 $scope.rescateTraspasoPortafolio.formData.numeroCuotas = undefined;
		 $scope.rescateTraspasoPortafolio.formData.montoComisiones = undefined;
		 $scope.rescateTraspasoPortafolio.formData.IdFormaPago = '0';
		 $scope.rescateTraspasoPortafolio.formData.comentario = '';
		 $scope.rescateTraspasoPortafolio.formData.FiltrosBusquedaFondoDestino = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.rescateTraspasoPortafolio.formData.FiltrosBusquedaFondo = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		$scope.rescateTraspasoPortafolio.oMotivoRetiro = {};
		$scope.rescateTraspasoPortafolio.ajustaVentana();
	 }
	 
	 
	 $scope.rescateTraspasoPortafolio.formData.resetFoco =  function() {
	 	$scope.rescateTraspasoPortafolio.formData.focoVolver = false;
		if($scope.rescateTraspasoPortafolio.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateTraspasoPortafolio.formData.focoOperacion = true;
			$scope.rescateTraspasoPortafolio.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateTraspasoPortafolio.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
		 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
		 						,IdUsuario			: $scope.SesionIdUsuario
		 						,IdModulo			: "FMM"
		 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
		 						,IdCliente			: $scope.rescateTraspasoPortafolio.oPortafolio.seleccionado.IdCliente
		 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
								,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
								,IdMedioDePago		: $scope.rescateTraspasoPortafolio.formData.IdMedioDePago ? $scope.rescateTraspasoPortafolio.formData.IdMedioDePago : 'CIN'
								,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
							 };
			 
			 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
		 };

});