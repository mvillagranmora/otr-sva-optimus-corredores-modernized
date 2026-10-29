angular.module('ModuloFFM').controller('IngresarAporteReinversionFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var aporteReinversion = this;             
	$scope.aporteReinversion.formData = {};
	$scope.aporteReinversion.masInformacion = false;
	$scope.aporteReinversion.formData.setInstrumento = "-1";
	$scope.aporteReinversion.datosFondo = {};
	$scope.aporteReinversion.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.aporteReinversion.formData.DatosAdicionales = "N";
	$scope.aporteReinversion.formData.visibleTruco = true;
	aporteReinversion.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.aporteReinversion.formData.idAutorizadoDarOrdenesDefecto=undefined;
	$scope.dataTemplate = {};
	$scope.aporteReinversion.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.aporteReinversion.ajustaVentana();
	
	$scope.aporteReinversion.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
		
	$scope.aporteReinversion.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.aporteReinversion.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.aporteReinversion.formData.FiltrosBusquedaFondoDestino = {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' }
	};
	
	$scope.aporteReinversion.oConfiguracionMedioDePago={
			tipo : "combobox-medio-pago",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.aporteReinversion.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 },
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};

//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	/*	
	$scope.aporteReinversion.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.aporteReinversion.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
*/

	$scope.aporteReinversion.oClientePortafolio ={
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
	
	
	$scope.aporteReinversion.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}			
	};
	
	$scope.aporteReinversion.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'}
	};
		
	$scope.aporteReinversion.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}	
	}
	
	 $scope.aporteReinversion.formData.masDatos = function()
	 {
		bcsComunes.ventanaAutoHeight($scope.winID);
		 if ($scope.aporteReinversion.formData.DatosAdicionales === 'S'){
			 $scope.aporteReinversion.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.aporteReinversion.formData.DatosAdicionales = 'S';
	     }
	 }
	
	
	 $scope.aporteReinversion.formData.ingresarAporteRescate = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 p_IdTipoCanalIngreso : 'OPT',
				 p_SolicitudesAporte : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
						//IdCliente 	          		:	$scope.aporteReinversion.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.aporteReinversion.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.aporteReinversion.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.aporteReinversion.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1	
				    	IdSerieFondoOrigen     		:	$scope.aporteReinversion.IdSerieFondoOrigen || '',
				    	IdEmisorOrigen              :   $scope.aporteReinversion.IdEmisorOrigen || '',
				    	CostoUF                     :   $scope.aporteReinversion.formData.costoUF || 0,
				    	FechaOrigenInversion        :   $scope.aporteReinversion.formData.fechaOrigenInversion ? util.dateSPdate($scope.aporteReinversion.formData.fechaOrigenInversion) : '0001-01-01',
				    	MontoOrigen                 :   $scope.aporteReinversion.formData.montoInicial,
				    	IndicadorMLT                :   $scope.aporteReinversion.formData.esMLT ? 'S' : 'N',
				    	IdSerieFondo          		:	$scope.aporteReinversion.datosFondo.IdSerieFondo,
				    	Monto 				  		:   $scope.aporteReinversion.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.aporteReinversion.formData.numeroCuotas,
				    	IdMoneda 			  		:	aporteReinversion.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.aporteReinversion.formData.IdMonedaAporte : $scope.aporteReinversion.datosFondo.IdMoneda,
				    	IdMedioDePago				:	$scope.aporteReinversion.formData.IdMedioDePago ? $scope.aporteReinversion.formData.IdMedioDePago : 'CIN',
				    	Visado						:	'N',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	IdRegimenTributario			:	$scope.aporteReinversion.formData.IdRegimenFondoMutuo || '',
				    	Glosa						: 	$scope.aporteReinversion.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.aporteReinversion.formData.idAutorizadoDarOrdenes || 0,
				    	FolioContrato				:	0
					   
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudAporteReinversion(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.aporteReinversion.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondoOrigen,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.aporteReinversion.formData.decimalesCantidad),
						CostoUF                         : $scope.aporteReinversion.formData.costoUF,
						IdEmisor                        : respuesta.P_OUT_RESULTADO[0].IdEmisor,
						MontoAporteOrigen               : respuesta.P_OUT_RESULTADO[0].Monto,
						esMLT                           : respuesta.P_OUT_RESULTADO[0].IndicadorMLT === 'S' ? 'SI' : 'NO',
						FechaOrigenInversion            : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaOriginalInversion),
						DocumentoIdentidad              : respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_RESULTADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_RESULTADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_RESULTADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotaTransferencia, $scope.formData.decimalesCantidad),
						IdEmisorOrigen                  : respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						IdEmisorDestino                 : respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_RESULTADO[0].IndicadorMLT,
						CodigoSerieReinversion			: "",
						IdSerieFondoReinversion			: respuesta.P_OUT_RESULTADO[0].IdSerieFondoOrigen,
						IdSerieFondoReinversion			: respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoOrigen				: respuesta.P_OUT_RESULTADO[0].IdSerieFondoOrigen
	     			};
	     		$scope.aporteReinversion.formData.IngresoConfirmacion = 'C';
	     		$scope.aporteReinversion.ajustaVentana();
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
	 
	 //$scope.$watch('aporteReinversion.oPortafolio.idActual',function(newval,oldval){			//GCMBIPAPAS-1
	 $scope.$watch('aporteReinversion.oClientePortafolio.idActual',function(newval,oldval){		//GCMBIPAPAS-1
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
					 //$scope.aporteReinversion.oPortafolio.idActual = 0;		//GCMBIPAPAS-1
					 $scope.aporteReinversion.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('aporteReinversion.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.aporteReinversion.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.aporteReinversion.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.aporteReinversion.formData.montoAporteOrigen,$scope.aporteReinversion.formData.numeroCuotas,'APO',$scope.aporteReinversion.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.aporteReinversion.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.aporteReinversion.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].Monto;
					 }else{
						 $scope.aporteReinversion.formData.numeroCuotas = 0;
						 $scope.aporteReinversion.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.aporteReinversion.formData.numeroCuotas = 0;
					 $scope.aporteReinversion.formData.montoInicial = 0;
		  		 });
				$scope.aporteReinversion.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.aporteReinversion.formData.decimalesCantidad = 0;
				$scope.aporteReinversion.formData.numeroCuotas = 0;
				$scope.aporteReinversion.formData.montoInicial = 0;
				$scope.aporteReinversion.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.aporteReinversion.ajustaVentana();
	 });
	 	 
	 $scope.$watch('aporteReinversion.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.aporteReinversion.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.aporteReinversion.formData.decimalesMonto = 0;
		 }
		 $scope.aporteReinversion.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteReinversion.formData.montoAporteOrigen',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.aporteReinversion.datosFondo,newval,$scope.aporteReinversion.formData.numeroCuotas,'APO',$scope.aporteReinversion.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteReinversion.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteReinversion.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].Monto;
				 }else{
					 $scope.aporteReinversion.formData.numeroCuotas = 0;
					 $scope.aporteReinversion.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.aporteReinversion.formData.numeroCuotas = 0;
				 $scope.aporteReinversion.formData.montoInicial = 0;
	  		 });
		}
	 });
	 
	 $scope.$watch('aporteReinversion.masInformacion',function(newval,oldval){
		 $scope.aporteReinversion.ajustaVentana();
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.aporteReinversion.formData.montoInicial = undefined;
				 $scope.aporteReinversion.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.aporteReinversion.ajustaVentana();
		 }
		 
	 });
	 

	 //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	 $scope.$watch('aporteReinversion.oClientePortafolio.seleccionado', function(newval,oldval){
		 if (newval && newval.IdCliente>0){
			 $scope.aporteReinversion.formData.filtrosFormaPago.IdPortafolio = newval.IdPortafolio			 
			 $scope.aporteReinversion.formData.FiltrosBusquedaFondo.IdPortafolio = newval.IdPortafolio                	 
		 }
		 
	 });	
	 //Fin GCMBIPAPAS-1
	 
	 $scope.$watch('aporteReinversion.formData.montoAporteOrigen',function(newval,oldval){
		 if(newval && newval != oldval){
			 $scope.aporteReinversion.formData.montoInicial = newval;
		 }
	 });
	 
	 $scope.$watch('aporteReinversion.formData.IdMonedaAporte',function(newval,oldval){
		if(newval && newval != oldval){
			if(newval && newval != oldval){
				bcsConfiguracionFFM.calcularMontoCuota($scope.aporteReinversion.datosFondo,$scope.aporteReinversion.formData.montoAporteOrigen,$scope.aporteReinversion.formData.numeroCuotas,'APO',newval,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.aporteReinversion.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.aporteReinversion.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].Monto;
					 }else{
						 $scope.aporteReinversion.formData.numeroCuotas = 0;
						 $scope.aporteReinversion.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.aporteReinversion.formData.numeroCuotas = 0;
					 $scope.aporteReinversion.formData.montoInicial = 0;
		  		 });
			}
		} 
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('aporteReinversion.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = aporteReinversion.confGlobal.JerarquiaPerfil[newval];
			 $scope.aporteReinversion.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.aporteReinversion.formData.FiltrosBusquedaFondoDestino.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.aporteReinversion.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });*/
	 
	 $scope.aporteReinversion.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.aporteReinversion.formData.tipoOperacion = 0;
		 }
		 $scope.aporteReinversion.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.aporteReinversion.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles		 
		 //$scope.aporteReinversion.oSocioNegocio.idActual = 0;
		 //$scope.aporteReinversion.oPortafolio.idActual = 0;
		 $scope.aporteReinversion.oClientePortafolio.idActual = 0;
		//Fin GCMBIPAPAS-1			 
		 $scope.aporteReinversion.formData.setFondo = '-1';
		 $scope.aporteReinversion.formData.setFondoOrigen = '-1';
		 $scope.aporteReinversion.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.aporteReinversion.formData.fechaOrigenInversion = undefined;
		 $scope.aporteReinversion.formData.esMLT = true;
		 $scope.aporteReinversion.formData.costoUF = undefined;
		 $scope.aporteReinversion.formData.montoInicial = undefined;
		 $scope.aporteReinversion.formData.numeroCuotas = undefined;
		 $scope.aporteReinversion.formData.montoComisiones = undefined;
		 $scope.aporteReinversion.formData.IdFormaPago = '0';
		 $scope.aporteReinversion.formData.comentario = '';
		 $scope.aporteReinversion.formData.FiltrosBusquedaFondo = {
				 IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
				IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
				BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
				BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.aporteReinversion.formData.FiltrosBusquedaFondoDestino = {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
				IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
				BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
				BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		};
		 $scope.aporteReinversion.ajustaVentana();
	 }
	 
	 $scope.aporteReinversion.formData.resetFoco =  function() {
	 	$scope.aporteReinversion.formData.focoVolver = false;
		if($scope.aporteReinversion.formData.IngresoConfirmacion === 'I')
		{
			$scope.aporteReinversion.formData.focoOperacion = true;
			$scope.aporteReinversion.formData.focoVolver = false;
		}
	 };
	 
	 $scope.aporteReinversion.formData.verComprobante = function() {
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'ComApRei' },
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
										"p_IdSolicitudAporteRescate" : $scope.aporteReinversion.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
			
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [{
														"type" : "pdf417",
														"name" : "pdf417",
														"data" :  bcsConfiguracionFFM.generarPdf417($scope.aporteReinversion.formData.ResultadoSolicitudAporteRescate)
													}];
					
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 };
	 
	 $scope.aporteReinversion.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.aporteReinversion.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
	 						,IdCliente			: $scope.aporteReinversion.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: $scope.aporteReinversion.formData.IdMedioDePago ? $scope.aporteReinversion.formData.IdMedioDePago : 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };

	 $scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.aporteReinversion.oClientePortafolio.seleccionado,
			$scope.aporteReinversion.datosFondo,
			$scope.aporteReinversion.formData
		);
	 };
});