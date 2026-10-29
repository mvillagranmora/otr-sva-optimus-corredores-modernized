angular.module('ModuloFFM').controller('IngresarGarantiaFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var ingresoGarantia = this;             
	var valoresMontoCuota = {};
	$scope.ingresoGarantia.formData = {};
	$scope.ingresoGarantia.masInformacion = false;
	$scope.ingresoGarantia.formData.setInstrumento = "-1";
	$scope.ingresoGarantia.datosFondo = {};
	$scope.ingresoGarantia.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.ingresoGarantia.formData.visibleTruco = true;
	ingresoGarantia.confGlobal = $scope.aporteRescateMain.confGlobal;
	//ingresoGarantia.oSocioNegocio = {};		//GCMBIPAPAS-1
	ingresoGarantia.oClientePortafolio = {};	//GCMBIPAPAS-1
	$scope.dataTemplate = {};
	ingresoGarantia.datosFondoDefecto = {};
	$scope.ingresoGarantia.autoCargarClientePortafolio = false;
	$scope.ingresoGarantia.formData.idAutorizadoDarOrdenesDefecto = undefined;
	
	$scope.ingresoGarantia.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,15);
    }
		
	$scope.ingresoGarantia.ajustaVentana();
	
	$scope.ingresoGarantia.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
		
	$scope.ingresoGarantia.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.ingresoGarantia.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.ingresoGarantia.filtrosGarantia = {
			IdEstadoEntidadDestinoGarantia 	: { "field"   : "IdEstadoEntidadDestinoGarantia", "operator": "eq", "value"   : "ACT" },
			IdEntidadDestinoGarantia 	    : { "field"   : "IdEntidadDestinoGarantia",       "operator": "eq", "value"   : 0 }
	}
	
//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
/*	
	$scope.ingresoGarantia.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  ingresoGarantia.oSocioNegocio.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.ingresoGarantia.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente    : { "field": "IdCliente","operator": "eq", "value" : 0 },
		 	 IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value" : 0 }
		 },
	 };
	*/

	$scope.ingresoGarantia.oClientePortafolio ={
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
	
	
	$scope.ingresoGarantia.oConceptoGarantia={
		tipo : "combobox-conceptos-garantia",
		configuracion : {},
		filtros : {
		    IdEstado : { "field"  : "IdEstado","operator": "eq", "value"   : "VIG" },
		    ConfiguradoEnModulo	: { "field"  : "ConfiguradoEnModulo","operator": "eq", "value"   : "S" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.ingresoGarantia.formData.FiltrosAutorizarOrden = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1},
		IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.ingresoGarantia.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}

	
	 $scope.ingresoGarantia.formData.ingresarGarantia = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoGarantias = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesGarantias : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
					    //IdCliente 	          		:	$scope.ingresoGarantia.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.ingresoGarantia.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.ingresoGarantia.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.ingresoGarantia.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1
				    	IdTipoMovimientoFondoMutuo	:	'INGGTA',
				    	IdSerieFondo				:	$scope.ingresoGarantia.datosFondo.IdSerieFondo,
				    	IdRegimenTributario			:	$scope.ingresoGarantia.formData.IdRegimenFondoMutuo,
				    	NumeroCuotas				:	$scope.ingresoGarantia.formData.numeroCuotas,
				    	IdContraparteGarantia		: 	ingresoGarantia.seleccionadoGarantia.IdContraparte || 0,
				    	IdInstrumentoGarantizado	:	0,
				    	FolioCarteraGarantizada		:	0,
				    	IdConcepto					:	$scope.ingresoGarantia.oConceptoGarantia.seleccionado.IdConcepto,
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.ingresoGarantia.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.ingresoGarantia.formData.idAutorizadoDarOrdenes || 0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearIngresoRetiroGarantiasFondoMutuo(datosIngresoGarantias).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ingresoGarantia.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.ingresoGarantia.formData.decimalesCantidad),
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
	     		$scope.ingresoGarantia.formData.IngresoConfirmacion = 'C';
	     		$scope.ingresoGarantia.ajustaVentana();
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
	 
	 //$scope.$watch('ingresoGarantia.oPortafolio.idActual',function(newval,oldval){		//GCMBIPAPAS-1
	 $scope.$watch('ingresoGarantia.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.ingresoGarantia.oPortafolio.idActual = 0;			//GCMBIPAPAS-1
					 $scope.ingresoGarantia.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('ingresoGarantia.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.ingresoGarantia.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.ingresoGarantia.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,0,$scope.ingresoGarantia.formData.numeroCuotas,'APO',$scope.ingresoGarantia.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.ingresoGarantia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.ingresoGarantia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.ingresoGarantia.formData.numeroCuotas = 0;
						 $scope.ingresoGarantia.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.ingresoGarantia.formData.numeroCuotas = 0;
					 $scope.ingresoGarantia.formData.montoInicial = 0;
		  		 });
				$scope.ingresoGarantia.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.ingresoGarantia.formData.decimalesCantidad = 0;
				$scope.ingresoGarantia.formData.numeroCuotas = 0;
				$scope.ingresoGarantia.formData.montoInicial = 0;
				$scope.ingresoGarantia.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.ingresoGarantia.ajustaVentana();
	 });
	 
	 $scope.$watch('ingresoGarantia.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.ingresoGarantia.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.ingresoGarantia.formData.decimalesMonto = 0;
		 }
		 $scope.ingresoGarantia.ajustaVentana();
	 });
	 
	 $scope.$watch('ingresoGarantia.masInformacion',function(newval,oldval){
		 $scope.ingresoGarantia.ajustaVentana();
	 });
	 
	 $scope.$watch('ingresoGarantia.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.ingresoGarantia.datosFondo,0,newval,'APO',$scope.ingresoGarantia.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.ingresoGarantia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.ingresoGarantia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.ingresoGarantia.formData.numeroCuotas = 0;
					 $scope.ingresoGarantia.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.ingresoGarantia.formData.numeroCuotas = 0;
				 $scope.ingresoGarantia.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.ingresoGarantia.formData.montoInicial = undefined;
				 $scope.ingresoGarantia.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.ingresoGarantia.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.ingresoGarantia.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.ingresoGarantia.formData.tipoOperacion = 0;
		 }
		 $scope.ingresoGarantia.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.ingresoGarantia.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles		 
		 //$scope.ingresoGarantia.oSocioNegocio.idActual = ingresoGarantia.confGlobal;
		 //$scope.ingresoGarantia.oPortafolio.idActual = 0;
		 $scope.ingresoGarantia.oClientePortafolio.idActual = 0
		//Fin GCMBIPAPAS-1		 
		 $scope.ingresoGarantia.formData.setFondo = '-1';
		 $scope.ingresoGarantia.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.ingresoGarantia.formData.numeroCuotas = undefined;
		 $scope.ingresoGarantia.formData.montoComisiones = undefined;
		 $scope.ingresoGarantia.formData.comentario = '';
		 $scope.ingresoGarantia.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.ingresoGarantia.oConceptoGarantia.idActual = 0;
		 $scope.ingresoGarantia.ajustaVentana();
	 }
	 
	 $scope.ingresoGarantia.formData.resetFoco =  function() {
	 	$scope.ingresoGarantia.formData.focoVolver = false;
		if($scope.ingresoGarantia.formData.IngresoConfirmacion === 'I')
		{
			$scope.ingresoGarantia.formData.focoOperacion = true;
			$scope.ingresoGarantia.formData.focoVolver = false;
		}
	 };
	 
	 $scope.ingresoGarantia.limpiarFMM();
	 
	 if (ingresoGarantia.confGlobal.IdCliente){
		 //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
		 /*		 
		 $scope.ingresoGarantia.oSocioNegocio.idDefecto = ingresoGarantia.confGlobal.IdSocioDeNegocio;
	     $scope.ingresoGarantia.oSocioNegocio.filtros.IdCliente.value = ingresoGarantia.confGlobal.IdCliente;
		 $scope.ingresoGarantia.oSocioNegocio.filtros.read = true;
		 
		 $scope.ingresoGarantia.oPortafolio.idDefecto	= ingresoGarantia.confGlobal.IdPortafolio;
		 $scope.ingresoGarantia.oPortafolio.filtros.IdPortafolio.value = ingresoGarantia.confGlobal.IdPortafolio;
		 $scope.ingresoGarantia.oPortafolio.filtros.read = true;
		 */
		 $scope.ingresoGarantia.oClientePortafolio.idDefecto = ingresoGarantia.confGlobal.IdSocioDeNegocio;
	     $scope.ingresoGarantia.oClientePortafolio.filtros.IdCliente.value = ingresoGarantia.confGlobal.IdCliente;
		 $scope.ingresoGarantia.oClientePortafolio.filtros.read = true;
		 
		 //$scope.ingresoGarantia.oClientePortafolio.idDefecto	= ingresoGarantia.confGlobal.IdPortafolio;
		 $scope.ingresoGarantia.oClientePortafolio.filtros.IdPortafolio.value = ingresoGarantia.confGlobal.IdPortafolio;
		 //$scope.ingresoGarantia.oClientePortafolio.filtros.read = true;
		//Fin GCMBIPAPAS-1
		 
		 ingresoGarantia.datosFondoDefecto.IdDefecto = ingresoGarantia.confGlobal.IdSerieFondo;
		 $scope.ingresoGarantia.formData.FiltrosCliente = {
		    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  ingresoGarantia.confGlobal.IdCliente},	
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  ingresoGarantia.confGlobal.IdPortafolio}	
		 }
	 }

	 //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
	 /*
	 $scope.$watch('ingresoGarantia.oPortafolio.seleccionado',function(newval,oldval){		  
			if(newval && newval != oldval){
				$scope.ingresoGarantia.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.ingresoGarantia.oSocioNegocio.seleccionado.IdCliente},		
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.ingresoGarantia.oPortafolio.idActual}					
				 }
			}
		 });
	*/
	 $scope.$watch('ingresoGarantia.oClientePortafolio.seleccionado',function(newval,oldval){		 
			if(newval && newval != oldval){
				$scope.ingresoGarantia.formData.FiltrosCliente = {		
					IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.ingresoGarantia.oClientePortafolio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.ingresoGarantia.oClientePortafolio.idActual}					
				 }
			}
		 });
	 
	//Fin GCMBIPAPAS-1
	 /* se quitar por IMPSURAOPT-267
	$scope.$watch('ingresoGarantia.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = ingresoGarantia.confGlobal.JerarquiaPerfil[newval];
			 $scope.ingresoGarantia.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.ingresoGarantia.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 })*/
	 
	$scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.ingresoGarantia.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
	 						,IdCliente			: $scope.ingresoGarantia.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };
	 
	$scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.ingresoGarantia.oClientePortafolio.seleccionado,
			$scope.ingresoGarantia.datosFondo,
			$scope.ingresoGarantia.formData
		);
	};
});