angular.module('ModuloFFM').controller('RetirarTituloFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var retiroTitulo = this;             
	var valoresMontoCuota = {};
	$scope.retiroTitulo.formData = {};
	$scope.retiroTitulo.masInformacion = false;
	$scope.retiroTitulo.formData.setInstrumento = "-1";
	$scope.retiroTitulo.datosFondo = {};
	$scope.retiroTitulo.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.retiroTitulo.formData.visibleTruco = true;
	retiroTitulo.confGlobal = $scope.aporteRescateMain.confGlobal;
	//retiroTitulo.oSocioNegocio = {};	//GCMBIPAPAS-1
	$scope.dataTemplate = {};
	retiroTitulo.datosFondoDefecto = {};
	$scope.retiroTitulo.autoCargarClientePortafolio = false;
	$scope.retiroTitulo.formData.idAutorizadoDarOrdenesDefecto = undefined;
	
	$scope.retiroTitulo.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,15);
    }
		
	$scope.retiroTitulo.ajustaVentana();
	
	$scope.retiroTitulo.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
		
	$scope.retiroTitulo.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.retiroTitulo.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
/*				
	$scope.retiroTitulo.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  retiroTitulo.oSocioNegocio.IdSocioDeNegocio },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.retiroTitulo.oPortafolio={
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
	$scope.retiroTitulo.oClientePortafolio ={
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
	
	$scope.retiroTitulo.formData.FiltrosAutorizarOrden = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1},
		IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'} 
	};
	
	$scope.retiroTitulo.formData.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
		
	 $scope.retiroTitulo.formData.retirarTitulo = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosRetiroTitulo = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesTitulo : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
						//IdCliente 	          		:	$scope.retiroTitulo.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.retiroTitulo.oPortafolio.seleccionado.IdPortafolio,

						IdCliente 	          		:	$scope.retiroTitulo.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.retiroTitulo.oClientePortafolio.seleccionado.IdPortafolio,
				      //Fin GCMBIPAPAS-1			
				    	IdTipoMovimientoFondoMutuo	:	'RETTIT',
				    	IdSerieFondo				:	$scope.retiroTitulo.datosFondo.IdSerieFondo,
				    	NumeroCuotas				:	$scope.retiroTitulo.formData.numeroCuotas,
				    	IdRegimenFondoMutuo			:	$scope.retiroTitulo.formData.IdRegimenFondoMutuo,
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	IdRegimenTributario			:	$scope.retiroTitulo.formData.IdRegimenFondoMutuo,
				    	Glosa						: 	$scope.retiroTitulo.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.retiroTitulo.formData.idAutorizadoDarOrdenes || 0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearIngresoRetiroTituloFondoMutuo(datosRetiroTitulo).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.retiroTitulo.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.retiroTitulo.formData.decimalesCantidad),
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
	     		$scope.retiroTitulo.formData.IngresoConfirmacion = 'C';
	     		$scope.retiroTitulo.ajustaVentana();
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
	 
	 //$scope.$watch('retiroTitulo.oPortafolio.idActual',function(newval,oldval){		//GCMBIPAPAS-1
	 $scope.$watch('retiroTitulo.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.retiroTitulo.oPortafolio.idActual = 0;		//GCMBIPAPAS-1
					 $scope.retiroTitulo.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('retiroTitulo.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
			 	$scope.retiroTitulo.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				$scope.retiroTitulo.formData.decimalesCantidad = newval.DecimalesCuota;
				bcsConfiguracionFFM.calcularMontoCuota(newval,0,$scope.retiroTitulo.formData.numeroCuotas,'RES',$scope.retiroTitulo.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.retiroTitulo.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.retiroTitulo.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.retiroTitulo.formData.numeroCuotas = 0;
						 $scope.retiroTitulo.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.retiroTitulo.formData.numeroCuotas = 0;
					 $scope.retiroTitulo.formData.montoInicial = 0;
		  		 });
				$scope.retiroTitulo.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.retiroTitulo.formData.decimalesCantidad = 0;
				$scope.retiroTitulo.formData.numeroCuotas = 0;
				$scope.retiroTitulo.formData.montoInicial = 0;
				$scope.retiroTitulo.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.retiroTitulo.ajustaVentana();
	 });
	 
	 $scope.$watch('retiroTitulo.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.retiroTitulo.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.retiroTitulo.formData.decimalesMonto = 0;
		 }
		 $scope.retiroTitulo.ajustaVentana();
	 });
	 
	 $scope.$watch('retiroTitulo.masInformacion',function(newval,oldval){
		 $scope.retiroTitulo.ajustaVentana();
	 });
	 
	 $scope.$watch('retiroTitulo.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.retiroTitulo.datosFondo,0,newval,'RES',$scope.retiroTitulo.formData.IdMonedaAporte,'C',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.retiroTitulo.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.retiroTitulo.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.retiroTitulo.formData.numeroCuotas = 0;
					 $scope.retiroTitulo.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.retiroTitulo.formData.numeroCuotas = 0;
				 $scope.retiroTitulo.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.retiroTitulo.formData.montoInicial = undefined;
				 $scope.retiroTitulo.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.retiroTitulo.ajustaVentana();
		 }
		 
	 });
	 
	 $scope.retiroTitulo.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.retiroTitulo.formData.tipoOperacion = 0;
		 }
		 $scope.retiroTitulo.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.retiroTitulo.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
		 //$scope.retiroTitulo.oSocioNegocio.idActual = retiroTitulo.confGlobal;
		 //$scope.retiroTitulo.oPortafolio.idActual = 0;
		 $scope.retiroTitulo.oClientePortafolio.idActual = 0;
		//Fin GCMBIPAPAS-1
		 $scope.retiroTitulo.formData.setFondo = '-1';
		 $scope.retiroTitulo.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.retiroTitulo.formData.numeroCuotas = undefined;
		 $scope.retiroTitulo.formData.montoComisiones = undefined;
		 $scope.retiroTitulo.formData.comentario = '';
		 $scope.retiroTitulo.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.retiroTitulo.ajustaVentana();
	 }
	 
	 $scope.retiroTitulo.formData.resetFoco =  function() {
	 	$scope.retiroTitulo.formData.focoVolver = false;
		if($scope.retiroTitulo.formData.IngresoConfirmacion === 'I')
		{
			$scope.retiroTitulo.formData.focoOperacion = true;
			$scope.retiroTitulo.formData.focoVolver = false;
		}
	 };
	 
	 $scope.retiroTitulo.limpiarFMM();
	 
	 if (retiroTitulo.confGlobal.IdCliente){
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
		 /*
		 $scope.retiroTitulo.oSocioNegocio.idDefecto = retiroTitulo.confGlobal.IdSocioDeNegocio;
	     $scope.retiroTitulo.oSocioNegocio.filtros.IdCliente.value = retiroTitulo.confGlobal.IdCliente;
		 $scope.retiroTitulo.oSocioNegocio.filtros.read = true;
		 
		 $scope.retiroTitulo.oPortafolio.idDefecto	= retiroTitulo.confGlobal.IdPortafolio;
		 $scope.retiroTitulo.oPortafolio.filtros.IdPortafolio.value = retiroTitulo.confGlobal.IdPortafolio;
		 $scope.retiroTitulo.oPortafolio.filtros.read = true;
		*/
		 //$scope.retiroTitulo.oClientePortafolio.idDefecto = retiroTitulo.confGlobal.IdSocioDeNegocio;
	     $scope.retiroTitulo.oClientePortafolio.filtros.IdCliente.value = retiroTitulo.confGlobal.IdCliente;
		 //$scope.retiroTitulo.oClientePortafolio.filtros.read = true;
		 
		 $scope.retiroTitulo.oClientePortafolio.idDefecto	= retiroTitulo.confGlobal.IdPortafolio;
		 $scope.retiroTitulo.oClientePortafolio.filtros.IdPortafolio.value = retiroTitulo.confGlobal.IdPortafolio;
		 $scope.retiroTitulo.oClientePortafolio.filtros.read = true;
		//Fin GCMBIPAPAS-1
		 
		 retiroTitulo.datosFondoDefecto.IdDefecto = retiroTitulo.confGlobal.IdSerieFondo;
		 $scope.retiroTitulo.formData.FiltrosCliente = {
		    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  retiroTitulo.confGlobal.IdCliente},	
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  retiroTitulo.confGlobal.IdPortafolio}	
		 }
	 }

	//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	 /*
	 $scope.$watch('retiroTitulo.oPortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.retiroTitulo.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.retiroTitulo.oSocioNegocio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.retiroTitulo.oPortafolio.idActual}	
				 }
			}
		 });
	*/

	 $scope.$watch('retiroTitulo.oClientePortafolio.seleccionado',function(newval,oldval){
			if(newval && newval != oldval){
				$scope.retiroTitulo.formData.FiltrosCliente = {
				    IdCliente : { "field"   : "IdCliente", "operator": "eq", "value"   :  $scope.retiroTitulo.oClientePortafolio.seleccionado.IdCliente},	
					IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  $scope.retiroTitulo.oClientePortafolio.idActual}	
				 }
			}
		 });
	//Fin GCMBIPAPAS-1	 
	 /* se quitar por IMPSURAOPT-267
	$scope.$watch('retiroTitulo.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = retiroTitulo.confGlobal.JerarquiaPerfil[newval];
			 $scope.retiroTitulo.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.retiroTitulo.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 })*/
	 
	$scope.solicitarDeclaracion= function(respuesta) {
			
	 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
	 						,IdUsuario			: $scope.SesionIdUsuario
	 						,IdModulo			: "FMM"
	 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
	 						//,IdCliente			: $scope.retiroTitulo.oPortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,IdCliente			: $scope.retiroTitulo.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
	 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
							,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
							,IdMedioDePago		: 'CIN'
							,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
						 };
		 
		 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
	 };
	 
	$scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.retiroTitulo.oClientePortafolio.seleccionado,
			$scope.retiroTitulo.datosFondo,
			$scope.retiroTitulo.formData
		);
	 };
	 
});