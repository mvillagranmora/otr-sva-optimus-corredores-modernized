angular.module('ModuloFFM').controller('IngresarRescateCambioSerieFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var rescateCambioSerie = this;             
	$scope.rescateCambioSerie.formData = {};
	$scope.rescateCambioSerie.masInformacion = false;
	$scope.rescateCambioSerie.formData.setInstrumento = "-1";
	$scope.rescateCambioSerie.datosFondo = {};
	$scope.rescateCambioSerie.formData.IngresoConfirmacion = 'I';
	$scope.rescateCambioSerie.formData.AporteRescate = 'A';
	$scope.rescateCambioSerie.formData.TipoRescate = '';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.rescateCambioSerie.formData.DatosAdicionales = "N";
	$scope.rescateCambioSerie.formData.visibleTruco = true;
	$scope.rescateCambioSerie.formData.idAutorizadoDarOrdenesDefecto = undefined;
	rescateCambioSerie.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.EsFondoNacional='S';
	$scope.EsSwitch='N';
	
	$scope.rescateCambioSerie.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.rescateCambioSerie.ajustaVentana();
	
	$scope.rescateCambioSerie.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
	
	$scope.rescateCambioSerie.formData.filtrosCuentaInversion = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}		
	};
	$scope.rescateCambioSerie.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
		};
		
	$scope.rescateCambioSerie.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	};
	
	$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdFondo : {"field" : "IdFondo", "operator": "eq", "value" : $scope.rescateCambioSerie.datosFondo.IdFondo},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	};
	
	$scope.rescateCambioSerie.formData.FiltrosComboFondo = {
		IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
	    CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
	    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	};
	
	$scope.rescateCambioSerie.formData.FiltrosComboFondoDestino = {
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		    CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    IdFondo : {"field" : "IdFondo", "operator": "eq", "value" : $scope.rescateCambioSerie.datosFondo.IdFondo},
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
	};
		
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.rescateCambioSerie.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.rescateCambioSerie.formData.filtrosRegimenAporte = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	};
	
	$scope.rescateCambioSerie.formData.filtrosRegimenRescate = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	};

//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
/*	
	$scope.rescateCambioSerie.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.rescateCambioSerie.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
*/

	$scope.rescateCambioSerie.oClientePortafolio ={
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
	
	
	$scope.rescateCambioSerie.ajustafiltros = function()
    {
		$scope.rescateCambioSerie.formData.FiltrosComboFondo.EsFondoNacional.value= $scope.EsFondoNacional;
		$scope.rescateCambioSerie.formData.FiltrosComboFondoDestino.EsFondoNacional.value= $scope.EsFondoNacional;
		$scope.rescateCambioSerie.formData.FiltrosBusquedaFondo.EsFondoNacional.value = $scope.EsFondoNacional;
		$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.EsFondoNacional.value= $scope.EsFondoNacional;
		
		
    }

	
	 $scope.rescateCambioSerie.formData.masDatos = function()
	 {
		 if ($scope.rescateCambioSerie.formData.DatosAdicionales === 'S'){
			 $scope.rescateCambioSerie.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.rescateCambioSerie.formData.DatosAdicionales = 'S';
	     }
	 }
	
	 $scope.rescateCambioSerie.formData.ingresarAporteRescate = function()
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
					    //IdCliente 	          		:	$scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.rescateCambioSerie.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.rescateCambioSerie.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateCambioSerie.oClientePortafolio.seleccionado.IdPortafolio,
				       //Fin GCMBIPAPAS-1
				    	IdSerieFondo          		:	$scope.rescateCambioSerie.datosFondo.IdSerieFondo,
				    	CalculoPorMontoCuota  		:	$scope.rescateCambioSerie.formData.TipoRescate,
				    	Monto 				  		:   $scope.rescateCambioSerie.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.rescateCambioSerie.formData.numeroCuotas,
						IdRegimenTributario			:	$scope.rescateCambioSerie.formData.TipoRescate !== 'T' ? $scope.rescateCambioSerie.formData.IdRegimenFondoMutuoRescate : '',
				    	IdMoneda 			  		:	$scope.rescateCambioSerie.datosFondo.IdMoneda,
				    	IdMedioDePago				:	'CIN',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	Glosa						: 	$scope.rescateCambioSerie.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.rescateCambioSerie.formData.idAutorizadoDarOrdenes || 0,
				    	Switch                      :   $scope.EsSwitch,
				    	FolioContrato				:	0
				 }],
				 
				 p_SolicitudesAporte : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles 
						//IdCliente 	          		:	$scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.rescateCambioSerie.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.rescateCambioSerie.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.rescateCambioSerie.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1
				    	IdSerieFondo          		:	$scope.rescateCambioSerie.datosFondoDestino.IdSerieFondo,
				    	IdEmisor                    :   '',
				    	Monto 				  		:   $scope.rescateCambioSerie.formData.montoAporte,
						NumeroCuotas 	  	  		:	$scope.rescateCambioSerie.formData.numeroCuotasAporte,
						IdRegimenTributario			:	$scope.rescateCambioSerie.formData.IdRegimenFondoMutuoAporte,
						IdMoneda 			  		:	$scope.rescateCambioSerie.datosFondoDestino.IdMoneda,
				    	IdMedioDePago				:	'CIN',
				    	Glosa						: 	$scope.rescateCambioSerie.formData.comentario,
				    	Switch                      :   $scope.EsSwitch,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudCambioSerie(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.rescateCambioSerie.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion          	: respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoRescate	 				: respuesta.P_OUT_RESULTADO[0].Monto,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.rescateCambioSerie.formData.decimalesCantidad),
						SaldoCuotas                 	: '',
				    	SaldoMonto                		: '',
				    	TipoRescate                     : $scope.rescateCambioSerie.formData.TipoRescateSeleccionado.NombreConcepto,
				    	MontoAporte  	 				: respuesta.P_OUT_RESULTADO[0].MontoAporte,
				    	NumeroCuotasDestino  	 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotasAporte, $scope.rescateCambioSerie.formData.decimalesCantidadAporte),
				    	EmisorDestino                   : respuesta.P_OUT_RESULTADO[0].IdEmisorAporte
	     		};
	     		$scope.rescateCambioSerie.formData.IngresoConfirmacion = 'C';
	     		$scope.rescateCambioSerie.ajustaVentana();
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
	 
	 //$scope.$watch('rescateCambioSerie.oPortafolio.idActual',function(newval,oldval){			//GCMBIPAPAS-1
	 $scope.$watch('rescateCambioSerie.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.rescateCambioSerie.oPortafolio.idActual = 0;		//GCMBIPAPAS-1
					 $scope.rescateCambioSerie.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 

	 //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	 $scope.$watch('rescateCambioSerie.oClientePortafolio.seleccionado', function(newval,oldval){
		 if (newval && newval.IdCliente>0){
			 $scope.rescateCambioSerie.formData.filtrosCuentaInversion.IdPortafolio.value = newval.IdPortafolio			 
			 $scope.rescateCambioSerie.formData.FiltrosComboFondo.IdPortafolio.value = newval.IdPortafolio
			 $scope.rescateCambioSerie.formData.filtrosFormaPago.IdPortafolio.value = newval.IdPortafolio   
		 }
		 
	 });	
	 //Fin GCMBIPAPAS-1	 
	 
	 $scope.$watch('rescateCambioSerie.formData.Switch', function(newval,oldaval){
		 if($scope.rescateCambioSerie.formData.Switch==true)
			 {
			 	$scope.EsFondoNacional='N';
			 	$scope.EsSwitch='S';
			 }else{
				 $scope.EsFondoNacional='S'; 
				 $scope.EsSwitch='N';
			 };
			 $scope.rescateCambioSerie.ajustaVentana();
			 $scope.rescateCambioSerie.ajustafiltros();
	 });
	 
	 	
	 
	 
	 $scope.$watch('rescateCambioSerie.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.rescateCambioSerie.formData.filtrosRegimenRescate.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.rescateCambioSerie.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.rescateCambioSerie.formData.FiltrosComboFondoDestino.IdFondo.value = newval.IdFondo;
				$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.IdFondo.value = newval.IdFondo;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateCambioSerie.formData.montoInicial,$scope.rescateCambioSerie.formData.numeroCuotas,'RES',$scope.rescateCambioSerie.datosFondo.IdMoneda,$scope.rescateCambioSerie.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateCambioSerie.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
						 $scope.rescateCambioSerie.formData.montoInicial = 0;
						 $scope.rescateCambioSerie.formData.montoAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
					 $scope.rescateCambioSerie.formData.montoInicial = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
		  		 });
				$scope.rescateCambioSerie.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.IdFondo.value = newval.IdFondo;
				$scope.asignarRegimenPorDefecto(); 
		    }
			else {
				$scope.rescateCambioSerie.formData.decimalesCantidad = 0;
				$scope.rescateCambioSerie.formData.numeroCuotas = 0;
				$scope.rescateCambioSerie.formData.montoInicial = 0;
				$scope.rescateCambioSerie.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateCambioSerie.datosFondoDestino',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.rescateCambioSerie.formData.filtrosRegimenAporte.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.rescateCambioSerie.formData.decimalesCantidadAporte = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.rescateCambioSerie.formData.montoInicial,$scope.rescateCambioSerie.formData.numeroCuotasAporte,'RES',newval.IdMoneda,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
						 $scope.rescateCambioSerie.formData.montoAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
		  		 });
				$scope.rescateCambioSerie.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.rescateCambioSerie.formData.decimalesCantidadAporte = 0;
				$scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
				$scope.rescateCambioSerie.formData.montoAporte = 0;
			}
		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.rescateCambioSerie.formData.decimalesMontoDestino = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.rescateCambioSerie.formData.decimalesMontoDestino = 0;
		 }
		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.montoAporte',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.rescateCambioSerie.datosFondoDestino,newval,$scope.rescateCambioSerie.formData.numeroCuotas,'RES',$scope.rescateCambioSerie.datosFondoDestino.IdMoneda,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.rescateCambioSerie.formData.numeroCuotasAporte = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
				 }
			 },function(respuesta){
				 $scope.rescateCambioSerie.formData.numeroCuotasAporte = 0;
				 $scope.rescateCambioSerie.formData.montoAporte = 0;
	  		 });
		}
	 });
	
	 $scope.$watch('rescateCambioSerie.formData.montoInicial',function(newval,oldval){
			if(newval && newval != oldval){
				bcsConfiguracionFFM.calcularMontoCuota($scope.rescateCambioSerie.datosFondo,newval,$scope.rescateCambioSerie.formData.numeroCuotas,'RES',$scope.rescateCambioSerie.datosFondo.IdMoneda,$scope.rescateCambioSerie.formData.TipoRescate,$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateCambioSerie.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
						 $scope.rescateCambioSerie.formData.montoAporte = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
						 $scope.rescateCambioSerie.formData.montoInicial = 0;
						 $scope.rescateCambioSerie.formData.montoAporte = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
					 $scope.rescateCambioSerie.formData.montoInicial = 0;
					 $scope.rescateCambioSerie.formData.montoAporte = 0;
		  		 });
			}
		 });
	 
	 $scope.$watch('rescateCambioSerie.masInformacion',function(newval,oldval){
		 $scope.rescateCambioSerie.ajustaVentana();
	 });
	 
	 $scope.$watch('rescateCambioSerie.formData.TipoRescate',function(newval,oldval){
		 if(newval === 'T'){
			 $scope.rescateCambioSerie.formData.numeroCuotas = $scope.rescateCambioSerie.formData.SaldoCuotas;
			 $scope.rescateCambioSerie.formData.montoInicial = $scope.rescateCambioSerie.formData.SaldoMonto;
		 }else{
			 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
			 $scope.rescateCambioSerie.formData.montoInicial = 0;
		 }
		 
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('rescateCambioSerie.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = rescateCambioSerie.confGlobal.JerarquiaPerfil[newval];
			 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.rescateCambioSerie.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.$watch('rescateCambioSerie.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.rescateCambioSerie.datosFondo,$scope.rescateCambioSerie.formData.montoInicial,newval,'RES',$scope.rescateCambioSerie.datosFondo.IdMoneda,$scope.rescateCambioSerie.formData.TipoRescate,$scope.SesionFechaSistema)
			 .then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.rescateCambioSerie.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.rescateCambioSerie.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
						 $scope.rescateCambioSerie.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.rescateCambioSerie.formData.numeroCuotas = 0;
					 $scope.rescateCambioSerie.formData.montoInicial = 0;
		  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.rescateCambioSerie.formData.montoInicial = undefined;
				 $scope.rescateCambioSerie.formData.numeroCuotas = undefined;
			 }
			 if(newval.IndicadorAporteRescate === 'APO'){
				$scope.rescateCambioSerie.formData.AporteRescate = 'A';
				$scope.rescateCambioSerie.formData.TipoRescate = 'M';
			 }
			 else{
				$scope.rescateCambioSerie.formData.AporteRescate = 'R';
			 }
			 
			 $scope.rescateCambioSerie.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.rescateCambioSerie.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.rescateCambioSerie.formData.tipoOperacion = 0;
			 $scope.rescateCambioSerie.formData.AporteRescate = 'A';
		 }
		 $scope.rescateCambioSerie.formData.idAutorizadoDarOrdenes = undefined;
		 $scope.rescateCambioSerie.formData.IngresoConfirmacion = 'I';
		 $scope.rescateCambioSerie.formData.TipoRescate = 'M';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles		 
		 //$scope.rescateCambioSerie.oSocioNegocio.idActual = 0;
		 //$scope.rescateCambioSerie.oPortafolio.idActual = 0;
		 $scope.rescateCambioSerie.oClientePortafolio.idActual = 0;		 
		//Fin GCMBIPAPAS-1		 
		 $scope.rescateCambioSerie.formData.setFondo = '-1';
		 $scope.rescateCambioSerie.formData.setFondoDestino = '-1';
		 $scope.rescateCambioSerie.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.rescateCambioSerie.formData.montoInicial = undefined;
		 $scope.rescateCambioSerie.formData.montoAporte = undefined;
		 $scope.rescateCambioSerie.formData.numeroCuotas = undefined;
		 $scope.rescateCambioSerie.formData.numeroCuotasAporte = undefined;
		 $scope.rescateCambioSerie.formData.montoComisiones = undefined;
		 $scope.rescateCambioSerie.formData.IdFormaPago = '0';
		 $scope.rescateCambioSerie.formData.comentario = '';
		 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' },
			CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
		    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
		 };
		 $scope.rescateCambioSerie.formData.FiltrosBusquedaFondoAporteDestino = {
				IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
				IdFondo : {"field" : "IdFondo", "operator": "eq", "value" : $scope.rescateCambioSerie.datosFondo.IdFondo},
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
				IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
				BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
				BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' },
				CambioFondo : { field: "CambioFondo", operator: "eq", value:'S' },
			    EsFondoNacional : {"field" : "EsFondoNacional", "operator": "eq", "value" : $scope.EsFondoNacional}
				
		};
		
		 $scope.rescateCambioSerie.ajustaVentana();
	 }
	 	 
	 $scope.rescateCambioSerie.formData.resetFoco =  function() {
	 	$scope.rescateCambioSerie.formData.focoVolver = false;
		if($scope.rescateCambioSerie.formData.IngresoConfirmacion === 'I')
		{
			$scope.rescateCambioSerie.formData.focoOperacion = true;
			$scope.rescateCambioSerie.formData.focoVolver = false;
		}
	 };
	 
	 $scope.rescateCambioSerie.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.rescateCambioSerie.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
	 						,IdCliente			: $scope.rescateCambioSerie.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: $scope.rescateCambioSerie.formData.IdMedioDePago ? $scope.rescateCambioSerie.formData.IdMedioDePago : 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };

	 $scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.rescateCambioSerie.oClientePortafolio.seleccionado,
			$scope.rescateCambioSerie.datosFondo,
			$scope.rescateCambioSerie.formData
		);
	};
});