angular.module('ModuloFFM').controller('RetirarGarantiaFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var retiroGarantia = this;             
	var valoresMontoCuota = {};
	$scope.retiroGarantia.formData = {};
	$scope.retiroGarantia.masInformacion = false;
	$scope.retiroGarantia.formData.setInstrumento = "-1";
	$scope.retiroGarantia.datosFondo = {};
	$scope.retiroGarantia.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.retiroGarantia.formData.visibleTruco = true;
	retiroGarantia.confGlobal = $scope.aporteRescateMain.confGlobal;
	//retiroGarantia.oSocioNegocio = {};	//GCMBIPAPAS-1
	retiroGarantia.oClientePortafolio = {};		//GCMBIPAPAS-1
	$scope.dataTemplate = {};
	retiroGarantia.datosFondoDefecto = {};
	$scope.retiroGarantia.autoCargarClientePortafolio = false;
	$scope.retiroGarantia.formData.idAutorizadoDarOrdenesDefecto = undefined;
	
	$scope.retiroGarantia.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,15);
    }
		
	$scope.retiroGarantia.ajustaVentana();
	
	$scope.retiroGarantia.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
		
	$scope.retiroGarantia.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.retiroGarantia.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};

//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
	/*				
	$scope.retiroGarantia.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  retiroGarantia.oSocioNegocio.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.retiroGarantia.oPortafolio={
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
	
	$scope.retiroGarantia.oClientePortafolio ={
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
	
	 $scope.retiroGarantia.oConceptoGarantia={
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
	
	$scope.retiroGarantia.formData.FiltrosAutorizarOrden = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1},
		IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	
	 $scope.retiroGarantia.formData.retirarGarantia = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosRetiroGarantias = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesGarantias : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles					    						
					    //IdCliente 	          		:	$scope.retiroGarantia.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.retiroGarantia.oPortafolio.seleccionado.IdPortafolio,
				    	IdCliente 	          		:	$scope.retiroGarantia.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.retiroGarantia.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1	
				    	IdTipoMovimientoFondoMutuo	:	'RETGTA',
				    	IdSerieFondo				:	$scope.retiroGarantia.datosFondo.IdSerieFondo,
				    	NumeroCuotas				:	$scope.retiroGarantia.formData.numeroCuotas,
				    	IdContraparteGarantia		: 	0,
				    	IdInstrumentoGarantizado	:	0,
				    	IdRegimenTributario			:	$scope.retiroGarantia.formData.idRegimenTributario || '',
				    	IdGarantia					:	$scope.retiroGarantia.formData.idGarantia || 0,
				    	IdConcepto					:	$scope.retiroGarantia.oConceptoGarantia.seleccionado.IdConcepto,
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.retiroGarantia.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.retiroGarantia.formData.idAutorizadoDarOrdenes || 0,
				    	IdRegimenTributario			:	$scope.retiroGarantia.formData.IdRegimenFondoMutuo
				 }]
		 }
	     bcsServicioFondosMutuos.CrearIngresoRetiroGarantiasFondoMutuo(datosRetiroGarantias).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.retiroGarantia.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.retiroGarantia.formData.decimalesCantidad),
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
	     		$scope.retiroGarantia.formData.IngresoConfirmacion = 'C';
	     		$scope.retiroGarantia.ajustaVentana();
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
	 
	 //$scope.$watch('retiroGarantia.oPortafolio.idActual',function(newval,oldval){			//GCMBIPAPAS-1
	 $scope.$watch('retiroGarantia.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.retiroGarantia.oPortafolio.idActual = 0;	//GCMBIPAPAS-1
					 $scope.retiroGarantia.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('retiroGarantia.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.retiroGarantia.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,0,$scope.retiroGarantia.formData.numeroCuotas,'APO',$scope.retiroGarantia.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.retiroGarantia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.retiroGarantia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.retiroGarantia.formData.numeroCuotas = 0;
						 $scope.retiroGarantia.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.retiroGarantia.formData.numeroCuotas = 0;
					 $scope.retiroGarantia.formData.montoInicial = 0;
		  		 });
				$scope.retiroGarantia.formData.IdMonedaAporte = newval.IdMoneda;
		    }
			else {
				$scope.retiroGarantia.formData.decimalesCantidad = 0;
				$scope.retiroGarantia.formData.numeroCuotas = 0;
				$scope.retiroGarantia.formData.montoInicial = 0;
				$scope.retiroGarantia.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.retiroGarantia.ajustaVentana();
	 });
	 
	 $scope.$watch('retiroGarantia.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.retiroGarantia.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.retiroGarantia.formData.decimalesMonto = 0;
		 }
		 $scope.retiroGarantia.ajustaVentana();
	 });
	 
	 $scope.$watch('retiroGarantia.masInformacion',function(newval,oldval){
		 $scope.retiroGarantia.ajustaVentana();
	 });
	 
	 $scope.$watch('retiroGarantia.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.retiroGarantia.datosFondo,0,newval,'APO',$scope.retiroGarantia.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.retiroGarantia.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.retiroGarantia.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.retiroGarantia.formData.numeroCuotas = 0;
					 $scope.retiroGarantia.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.retiroGarantia.formData.numeroCuotas = 0;
				 $scope.retiroGarantia.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.retiroGarantia.formData.montoInicial = undefined;
				 $scope.retiroGarantia.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.retiroGarantia.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.retiroGarantia.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.retiroGarantia.formData.tipoOperacion = 0;
		 }
		 $scope.retiroGarantia.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.retiroGarantia.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
		 //$scope.retiroGarantia.oSocioNegocio.idActual = retiroGarantia.confGlobal;
		 //$scope.retiroGarantia.oPortafolio.idActual = 0;		 
		 $scope.retiroGarantia.oClientePortafolio.idActual = 0;
		//Fin GCMBIPAPAS-1	
		 $scope.retiroGarantia.formData.setFondo = '-1';
		 $scope.retiroGarantia.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.retiroGarantia.formData.numeroCuotas = undefined;
		 $scope.retiroGarantia.formData.montoComisiones = undefined;
		 $scope.retiroGarantia.formData.comentario = '';
		 $scope.retiroGarantia.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.retiroGarantia.oConceptoGarantia.idActual = 0;
		 $scope.retiroGarantia.ajustaVentana();
	 }
	 
	 $scope.retiroGarantia.formData.resetFoco =  function() {
	 	$scope.retiroGarantia.formData.focoVolver = false;
		if($scope.retiroGarantia.formData.IngresoConfirmacion === 'I')
		{
			$scope.retiroGarantia.formData.focoOperacion = true;
			$scope.retiroGarantia.formData.focoVolver = false;
		}
	 };
	 
	 $scope.retiroGarantia.limpiarFMM();
	 
	 if (retiroGarantia.confGlobal.IdCliente){
	//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	/*
		 $scope.retiroGarantia.oSocioNegocio.idDefecto = retiroGarantia.confGlobal.IdSocioDeNegocio;
	     $scope.retiroGarantia.oSocioNegocio.filtros.IdCliente.value = retiroGarantia.confGlobal.IdCliente;
		 $scope.retiroGarantia.oSocioNegocio.filtros.read = true;
		 
		 $scope.retiroGarantia.oPortafolio.idDefecto	= retiroGarantia.confGlobal.IdPortafolio;
		 $scope.retiroGarantia.oPortafolio.filtros.IdPortafolio.value = retiroGarantia.confGlobal.IdPortafolio;
		 $scope.retiroGarantia.oPortafolio.filtros.read = true;
	*/
		 $scope.retiroGarantia.oClientePortafolio.idDefecto = retiroGarantia.confGlobal.IdSocioDeNegocio;
	     $scope.retiroGarantia.oClientePortafolio.filtros.IdCliente.value = retiroGarantia.confGlobal.IdCliente;
		 $scope.retiroGarantia.oClientePortafolio.filtros.read = true;
		 
		 //$scope.retiroGarantia.oClientePortafolio.idDefecto	= retiroGarantia.confGlobal.IdPortafolio;
		 $scope.retiroGarantia.oClientePortafolio.filtros.IdPortafolio.value = retiroGarantia.confGlobal.IdPortafolio;
		 //$scope.retiroGarantia.oClientePortafolio.filtros.read = true;
	//Fin GCMBIPAPAS-1			 
		 retiroGarantia.datosFondoDefecto.IdDefecto = retiroGarantia.confGlobal.IdSerieFondo;
		 $scope.retiroGarantia.formData.FiltrosCliente = {
		    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  retiroGarantia.confGlobal.IdCliente},	
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  retiroGarantia.confGlobal.IdPortafolio}	
		 }
	 }

//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
 /*				 
	 $scope.$watch('retiroGarantia.oPortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.retiroGarantia.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.retiroGarantia.oSocioNegocio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.retiroGarantia.oPortafolio.idActual}	
				 }
			}
		 });
*/
	 $scope.$watch('retiroGarantia.oClientePortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.retiroGarantia.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.retiroGarantia.oClientePortafolio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.retiroGarantia.oClientePortafolio.idActual}	
				 }
			}
		 });
//Fin GCMBIPAPAS-1			 
	 /* se quitar por IMPSURAOPT-267
	$scope.$watch('retiroGarantia.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = retiroGarantia.confGlobal.JerarquiaPerfil[newval];
			 $scope.retiroGarantia.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.retiroGarantia.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 })*/
	 
	$scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.retiroGarantia.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
	 						,IdCliente			: $scope.retiroGarantia.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };
	 
	 $scope.retiroGarantia.buscarGarantia = function() {
		 var oDatosCartera = {
				 //"IdPortafolio" : $scope.retiroGarantia.oPortafolio.idActual,			//GCMBIPAPAS-1
				 "IdPortafolio" : $scope.retiroGarantia.oClientePortafolio.seleccionado.IdPortafolio,	//GCMBIPAPAS-1
				 "IdInstrumento" : $scope.retiroGarantia.datosFondo.IdInstrumento
		 }
		 var id1 = bcsComunes.ventanaAbrir("ConsultarGarantiaFFMM",oDatosCartera,false, false, $scope.winID); 			
	     var promesa=bcsComunes.ventanaPromesa(id1);
	     promesa.then(function(data){
	        	if (data && data.IdInstrumento){
	        		$scope.retiroGarantia.formData.folioGarantia = data.FolioGarantia;
					$scope.retiroGarantia.formData.idGarantia = data.IdGarantia;
	        		$scope.retiroGarantia.formData.saldoGarantia = data.CantidadGarantia;
	        		$scope.retiroGarantia.formData.IdRegimenFondoMutuo = data.IdRegimenFondoMutuo;
	        	}
	     
	     });
	 };
	 
});