angular.module('ModuloFFM').controller('IngresarTituloFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var ingresoTitulo = this;             
	var valoresMontoCuota = {};
	$scope.ingresoTitulo.formData = {};
	$scope.ingresoTitulo.masInformacion = false;
	$scope.ingresoTitulo.formData.setInstrumento = "-1";
	$scope.ingresoTitulo.datosFondo = {};
	$scope.ingresoTitulo.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.ingresoTitulo.formData.visibleTruco = true;
	ingresoTitulo.confGlobal = $scope.aporteRescateMain.confGlobal;
	//ingresoTitulo.oSocioNegocio = {};		//GCMBIPAPAS-1
	ingresoTitulo.oClientePortafolio = {};		//GCMBIPAPAS-1
	$scope.dataTemplate = {};
	ingresoTitulo.datosFondoDefecto = {};
	$scope.ingresoTitulo.autoCargarClientePortafolio = false;
	$scope.ingresoTitulo.formData.idAutorizadoDarOrdenesDefecto = undefined;
	
	$scope.ingresoTitulo.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,15);
    }
		
	$scope.ingresoTitulo.ajustaVentana();
	
	$scope.ingresoTitulo.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
		
	$scope.ingresoTitulo.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.ingresoTitulo.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
/*		
	$scope.ingresoTitulo.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  ingresoTitulo.oSocioNegocio.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.ingresoTitulo.oPortafolio={
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

	$scope.ingresoTitulo.oClientePortafolio ={
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
	
	
	$scope.ingresoTitulo.formData.FiltrosAutorizarOrden = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1},
		IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.ingresoTitulo.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
	
	 $scope.ingresoTitulo.formData.ingresarTitulo = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoTitulo = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesTitulo : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
					    //IdCliente 	          		:	$scope.ingresoTitulo.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.ingresoTitulo.oPortafolio.seleccionado.IdPortafolio,
				    	IdCliente 	          		:	$scope.ingresoTitulo.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.ingresoTitulo.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1
				    	IdTipoMovimientoFondoMutuo	:	'INGTIT',
				    	IdSerieFondo				:	$scope.ingresoTitulo.datosFondo.IdSerieFondo,
				    	NumeroCuotas				:	$scope.ingresoTitulo.formData.numeroCuotas,
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	IdRegimenTributario			:	$scope.ingresoTitulo.formData.IdRegimenFondoMutuo,
				    	Glosa						: 	$scope.ingresoTitulo.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.ingresoTitulo.formData.idAutorizadoDarOrdenes || 0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearIngresoRetiroTituloFondoMutuo(datosIngresoTitulo).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ingresoTitulo.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.ingresoTitulo.formData.decimalesCantidad),
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
	     		$scope.ingresoTitulo.formData.IngresoConfirmacion = 'C';
	     		$scope.ingresoTitulo.ajustaVentana();
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
	 
	 //$scope.$watch('ingresoTitulo.oPortafolio.idActual',function(newval,oldval){		
     $scope.$watch('ingresoTitulo.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.ingresoTitulo.oPortafolio.idActual = 0;		//GCMBIPAPAS-1
					 $scope.ingresoTitulo.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('ingresoTitulo.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.ingresoTitulo.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.ingresoTitulo.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				bcsConfiguracionFFM.calcularMontoCuota(newval,0,$scope.ingresoTitulo.formData.numeroCuotas,'APO',$scope.ingresoTitulo.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.ingresoTitulo.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.ingresoTitulo.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.ingresoTitulo.formData.numeroCuotas = 0;
						 $scope.ingresoTitulo.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.ingresoTitulo.formData.numeroCuotas = 0;
					 $scope.ingresoTitulo.formData.montoInicial = 0;
		  		 });
				$scope.ingresoTitulo.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.ingresoTitulo.formData.decimalesCantidad = 0;
				$scope.ingresoTitulo.formData.numeroCuotas = 0;
				$scope.ingresoTitulo.formData.montoInicial = 0;
				$scope.ingresoTitulo.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.ingresoTitulo.ajustaVentana();
	 });
	 
	 $scope.$watch('ingresoTitulo.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.ingresoTitulo.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.ingresoTitulo.formData.decimalesMonto = 0;
		 }
		 $scope.ingresoTitulo.ajustaVentana();
	 });
	 
	 $scope.$watch('ingresoTitulo.masInformacion',function(newval,oldval){
		 $scope.ingresoTitulo.ajustaVentana();
	 });
	 
	 $scope.$watch('ingresoTitulo.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.ingresoTitulo.datosFondo,0,newval,'APO',$scope.ingresoTitulo.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.ingresoTitulo.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.ingresoTitulo.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.ingresoTitulo.formData.numeroCuotas = 0;
					 $scope.ingresoTitulo.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.ingresoTitulo.formData.numeroCuotas = 0;
				 $scope.ingresoTitulo.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.ingresoTitulo.formData.montoInicial = undefined;
				 $scope.ingresoTitulo.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.ingresoTitulo.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.ingresoTitulo.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.ingresoTitulo.formData.tipoOperacion = 0;
		 }
		 $scope.ingresoTitulo.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.ingresoTitulo.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles		 
		 //$scope.ingresoTitulo.oSocioNegocio.idActual = ingresoTitulo.confGlobal;
		 //$scope.ingresoTitulo.oPortafolio.idActual = 0;
		 $scope.ingresoTitulo.oClientePortafolio.idActual = 0;
		//GCMBIPAPAS-1		 
		 $scope.ingresoTitulo.formData.setFondo = '-1';
		 $scope.ingresoTitulo.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.ingresoTitulo.formData.numeroCuotas = undefined;
		 $scope.ingresoTitulo.formData.montoComisiones = undefined;
		 $scope.ingresoTitulo.formData.comentario = '';
		 $scope.ingresoTitulo.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.ingresoTitulo.ajustaVentana();
	 }
	 
	 $scope.ingresoTitulo.formData.resetFoco =  function() {
	 	$scope.ingresoTitulo.formData.focoVolver = false;
		if($scope.ingresoTitulo.formData.IngresoConfirmacion === 'I')
		{
			$scope.ingresoTitulo.formData.focoOperacion = true;
			$scope.ingresoTitulo.formData.focoVolver = false;
		}
	 };
	 
	 $scope.ingresoTitulo.limpiarFMM();
	 
	 if (ingresoTitulo.confGlobal.IdCliente){
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
		 /*
		 $scope.ingresoTitulo.oSocioNegocio.idDefecto = ingresoTitulo.confGlobal.IdSocioDeNegocio;
	     $scope.ingresoTitulo.oSocioNegocio.filtros.IdCliente.value = ingresoTitulo.confGlobal.IdCliente;
		 $scope.ingresoTitulo.oSocioNegocio.filtros.read = true;
		 
		 $scope.ingresoTitulo.oPortafolio.idDefecto	= ingresoTitulo.confGlobal.IdPortafolio;
		 $scope.ingresoTitulo.oPortafolio.filtros.IdPortafolio.value = ingresoTitulo.confGlobal.IdPortafolio;
		 $scope.ingresoTitulo.oPortafolio.filtros.read = true;
		 */
		 $scope.ingresoTitulo.oClientePortafolio.idDefecto = ingresoTitulo.confGlobal.IdSocioDeNegocio;
	     $scope.ingresoTitulo.oClientePortafolio.filtros.IdCliente.value = ingresoTitulo.confGlobal.IdCliente;
		 $scope.ingresoTitulo.oClientePortafolio.filtros.read = true;
		 
		 //$scope.ingresoTitulo.oClientePortafolio.idDefecto	= ingresoTitulo.confGlobal.IdPortafolio;
		 $scope.ingresoTitulo.oClientePortafolio.filtros.IdPortafolio.value = ingresoTitulo.confGlobal.IdPortafolio;
		 //$scope.ingresoTitulo.oClientePortafolio.filtros.read = true;
		 		 
		 //Fin GCMBIPAPAS-1
		 
		 ingresoTitulo.datosFondoDefecto.IdDefecto = ingresoTitulo.confGlobal.IdSerieFondo;
		 $scope.ingresoTitulo.formData.FiltrosCliente = {
		    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  ingresoTitulo.confGlobal.IdCliente},	
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  ingresoTitulo.confGlobal.IdPortafolio}	
		 }
	 }

	//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
	 /*	
	 $scope.$watch('ingresoTitulo.oPortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.ingresoTitulo.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.ingresoTitulo.oSocioNegocio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.ingresoTitulo.oPortafolio.idActual}	
				 }
			}
		 });
	*/
	 $scope.$watch('ingresoTitulo.oClientePortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.ingresoTitulo.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.ingresoTitulo.oClientePortafolio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.ingresoTitulo.oClientePortafolio.idActual}	
				 }
			}
			
			
			if ($scope.ingresoTitulo.oClientePortafolio.seleccionado.BloqueadoPorCumplimiento === 'S')
			{
				bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_CLIENTE_BLOQUEADO_POR_CUMPLIMIENTO")},$scope.winID);	
			}
			
		 });	 
	 //Fin GCMBIPAPAS-1	
	 /* se quitar por IMPSURAOPT-267
	$scope.$watch('ingresoTitulo.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = ingresoTitulo.confGlobal.JerarquiaPerfil[newval];
			 $scope.ingresoTitulo.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.ingresoTitulo.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 })*/
	 
	$scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.ingresoTitulo.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
	 						,IdCliente			: $scope.ingresoTitulo.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };	
	 
	 $scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.ingresoTitulo.oClientePortafolio.seleccionado,
			$scope.ingresoTitulo.datosFondo,
			$scope.ingresoTitulo.formData
		);
	 };
});