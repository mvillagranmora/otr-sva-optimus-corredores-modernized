angular.module('ModuloFFM').controller('IngresarAporteRescateProgramadoFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var aporteRescateProgramado = this;
	var valoresMontoCuota = {};
	$scope.aporteRescateProgramado.formData = {};
	$scope.dataTemplate = {};
	$scope.aporteRescateProgramado.masInformacion = false;
	$scope.aporteRescateProgramado.formData.setInstrumento = "-1";
	$scope.aporteRescateProgramado.datosFondo = {};
	$scope.aporteRescateProgramado.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.aporteRescateProgramado.formData.DatosAdicionales = "N";
	$scope.aporteRescateProgramado.formData.visibleTruco = true;
	$scope.aporteRescateProgramado.oFormaPago = {};
	$scope.aporteRescateProgramado.formData.idAutorizadoDarOrdenesDefecto = undefined;
	aporteRescateProgramado.confGlobal = $scope.aporteRescateMain.confGlobal;
	console.log(aporteRescateProgramado.confGlobal.JerarquiaPerfil)
	$scope.aporteRescateProgramado.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
			
	$scope.aporteRescateProgramado.ajustaVentana();
	
	
	$scope.aporteRescateProgramado.formData.IdCategoriaInversionista = {
		"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	}
		
	$scope.aporteRescateProgramado.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.aporteRescateProgramado.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.aporteRescateProgramado.oConfiguracionMedioDePago={
			tipo : "combobox-medio-pago",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.aporteRescateProgramado.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  -1},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
/*	
	$scope.aporteRescateProgramado.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.aporteRescateProgramado.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
*/

	$scope.aporteRescateProgramado.oClientePortafolio ={
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
	
	$scope.aporteRescateProgramado.formData.filtrosCuentaInversion = {
		IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1}
	};
	
	$scope.aporteRescateProgramado.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  -1},	
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator": "eq", "value": 'ORDEN-CREAR'}
	};
	
	
	$scope.aporteRescateProgramado.formData.filtrosRegimen = {
		IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
		IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
		IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
		IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
	
	 $scope.aporteRescateProgramado.formData.masDatos = function()
	 {
		 bcsComunes.ventanaAutoHeight($scope.winID);
		 if ($scope.aporteRescateProgramado.formData.DatosAdicionales === 'S'){
			 $scope.aporteRescateProgramado.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.aporteRescateProgramado.formData.DatosAdicionales = 'S';
	     }
	 }
	
	
	 $scope.aporteRescateProgramado.formData.ingresarAporteRescate = function()
     {	
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosIngresoAporteRescate = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_simularOperacion : 'N',
				 p_UsaReglasNegocio : 'S',
				 
				 p_SolicitudesAporte : [{
					    IdInterno		      		:	1,
					  //Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
					    //IdCliente 	          		:	$scope.aporteRescateProgramado.oPortafolio.seleccionado.IdCliente,		
				    	//IdPortafolio          		:	$scope.aporteRescateProgramado.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.aporteRescateProgramado.oClientePortafolio.seleccionado.IdCliente,	
  					    IdPortafolio          		:	$scope.aporteRescateProgramado.oClientePortafolio.seleccionado.IdPortafolio,
  					  //Fin GCMBIPAPAS-1
				    	IdSerieFondo          		:	$scope.aporteRescateProgramado.datosFondo.IdSerieFondo,
				    	IdSerieFondoDestino   		:   '',
				    	IdEmisorDestino 	  		:	'',
				    	IdClienteDestino 	  		:	0,
				    	IdPortafolioDestino   		:	0,
				    	IdTipoCartera 		  		:   '',
				    	FechaRescate                :   util.dateSPdate($scope.aporteRescateProgramado.formData.fechaRescate),
				    	Monto 				  		:   $scope.aporteRescateProgramado.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.aporteRescateProgramado.formData.numeroCuotas,
				    	IdMoneda 			  		:	aporteRescateProgramado.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.aporteRescateProgramado.formData.IdMonedaAporte : $scope.aporteRescateProgramado.datosFondo.IdMoneda,
				    	IdMedioDePago				:	$scope.aporteRescateProgramado.formData.IdMedioDePago ? $scope.aporteRescateProgramado.formData.IdMedioDePago : 'CIN',
				    	Visado						:	'N',
				    	IdMedioDePagoRescate		:	$scope.aporteRescateProgramado.formData.IdMedioDePagoRescate ? $scope.aporteRescateProgramado.formData.IdMedioDePagoRescate : 'CIN',
				    	IdPreferenciaPagoPortafolioRescate : $scope.aporteRescateProgramado.oFormaPago.seleccionado ? $scope.aporteRescateProgramado.oFormaPago.seleccionado.IdPreferenciaPagoPortafolio : 0,
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	IdRegimenTributario			:	$scope.aporteRescateProgramado.formData.IdRegimenFondoMutuo || '',
				    	Glosa						: 	$scope.aporteRescateProgramado.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.aporteRescateProgramado.formData.idAutorizadoDarOrdenes || 0,
				    	FolioContrato				:	0					   
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudAporteRescateProgramado(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.aporteRescateProgramado.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacionRescate),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.aporteRescateProgramado.formData.decimalesCantidad),
						DocumentoIdentidad              : respuesta.P_OUT_RESULTADO[0].DocumentoIdentidad,
						CodigoPortafolio                : respuesta.P_OUT_RESULTADO[0].CodigoPortafolio,
						NombreCliente                   : respuesta.P_OUT_RESULTADO[0].NombreCliente,
						FechaLiquidacion                : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaLiquidacion),
						FechaCertificacion              : util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaCertificacion),
						MontoTransferencia              : respuesta.P_OUT_RESULTADO[0].MontoTransferencia,
						NumeroCuotaTransferencia        : util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotaTransferencia, $scope.aporteRescateProgramado.formData.decimalesCantidad),
						IdEmisorOrigen                  : respuesta.P_OUT_RESULTADO[0].IdEmisorOrigen,
						IdSerieFondoDestino             : respuesta.P_OUT_RESULTADO[0].IdSerieFondoDestino,
						IdEmisorDestino                 : respuesta.P_OUT_RESULTADO[0].IdEmisorDestino,
						IndicadorMLT                    : respuesta.P_OUT_RESULTADO[0].IndicadorMLT,
	     		};
	     		$scope.aporteRescateProgramado.formData.IngresoConfirmacion = 'C';
	     		$scope.aporteRescateProgramado.ajustaVentana();
	     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		if(id){
	     			var promesa=bcsComunes.ventanaPromesa(id); 
		     		$.when(promesa).then(function(data){  
		     			$scope.solicitarDeclaracion(respuesta);
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
	 
	 //$scope.$watch('aporteRescateProgramado.oPortafolio.idActual',function(newval,oldval){		//GCMBIPAPAS-1
     $scope.$watch('aporteRescateProgramado.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.aporteRescateProgramado.oPortafolio.idActual = 0;			//GCMBIPAPAS-1
					 $scope.aporteRescateProgramado.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('aporteRescateProgramado.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.aporteRescateProgramado.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.aporteRescateProgramado.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.aporteRescateProgramado.formData.montoInicial,$scope.aporteRescateProgramado.formData.numeroCuotas,'APO',$scope.aporteRescateProgramado.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.aporteRescateProgramado.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.aporteRescateProgramado.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.aporteRescateProgramado.formData.numeroCuotas = 0;
						 $scope.aporteRescateProgramado.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.aporteRescateProgramado.formData.numeroCuotas = 0;
					 $scope.aporteRescateProgramado.formData.montoInicial = 0;
		  		 });
				$scope.aporteRescateProgramado.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto();
		    }
			else {
				$scope.aporteRescateProgramado.formData.decimalesCantidad = 0;
				$scope.aporteRescateProgramado.formData.numeroCuotas = 0;
				$scope.aporteRescateProgramado.formData.montoInicial = 0;
				$scope.aporteRescateProgramado.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
				$scope.aporteRescateProgramado.formData.fechaRescate = undefined;
			}
		 $scope.aporteRescateProgramado.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteRescateProgramado.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.aporteRescateProgramado.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.aporteRescateProgramado.formData.decimalesMonto = 0;
		 }
		 $scope.aporteRescateProgramado.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteRescateProgramado.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.aporteRescateProgramado.datosFondo,newval,$scope.aporteRescateProgramado.formData.numeroCuotas,'APO',$scope.aporteRescateProgramado.formData.IdMonedaAporte, 'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteRescateProgramado.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteRescateProgramado.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.aporteRescateProgramado.formData.numeroCuotas = 0;
					 $scope.aporteRescateProgramado.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.aporteRescateProgramado.formData.numeroCuotas = 0;
				 $scope.aporteRescateProgramado.formData.montoInicial = 0;
	  		 });
		}
	 });
	 
	 $scope.$watch('aporteRescateProgramado.masInformacion',function(newval,oldval){
		 $scope.aporteRescateProgramado.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteRescateProgramado.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.aporteRescateProgramado.datosFondo,$scope.aporteRescateProgramado.formData.montoInicial,newval,'APO',$scope.aporteRescateProgramado.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteRescateProgramado.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteRescateProgramado.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.aporteRescateProgramado.formData.numeroCuotas = 0;
					 $scope.aporteRescateProgramado.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.aporteRescateProgramado.formData.numeroCuotas = 0;
				 $scope.aporteRescateProgramado.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.aporteRescateProgramado.formData.montoInicial = undefined;
				 $scope.aporteRescateProgramado.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.aporteRescateProgramado.ajustaVentana();
		 }
		 
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('aporteRescateProgramado.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = aporteRescateProgramado.confGlobal.JerarquiaPerfil[newval];
			 $scope.aporteRescateProgramado.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.aporteRescateProgramado.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 })
	 */
	 
	 
	 $scope.aporteRescateProgramado.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.aporteRescateProgramado.formData.tipoOperacion = 0;
		 }
		 $scope.aporteRescateProgramado.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.aporteRescateProgramado.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
		 //$scope.aporteRescateProgramado.oSocioNegocio.idActual = 0;
		 //$scope.aporteRescateProgramado.oPortafolio.idActual = 0;
		 $scope.aporteRescateProgramado.oClientePortafolio.idActual = 0;
		//Fin GCMBIPAPAS-1
		 $scope.aporteRescateProgramado.formData.setFondo = '-1';
		 $scope.aporteRescateProgramado.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.aporteRescateProgramado.formData.fechaRescate = undefined;
		 $scope.aporteRescateProgramado.formData.montoInicial = undefined;
		 $scope.aporteRescateProgramado.formData.numeroCuotas = undefined;
		 $scope.aporteRescateProgramado.formData.montoComisiones = undefined;
		 $scope.aporteRescateProgramado.formData.IdFormaPago = '0';
		 $scope.aporteRescateProgramado.formData.comentario = '';
		 $scope.aporteRescateProgramado.formData.FiltrosBusquedaFondo = {
		    IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.aporteRescateProgramado.ajustaVentana();
	 }
	 
	 $scope.aporteRescateProgramado.formData.resetFoco =  function() {
	 	$scope.aporteRescateProgramado.formData.focoVolver = false;
		if($scope.aporteRescateProgramado.formData.IngresoConfirmacion === 'I')
		{
			$scope.aporteRescateProgramado.formData.focoOperacion = true;
			$scope.aporteRescateProgramado.formData.focoVolver = false;
		}
	 };
	 
	 $scope.aporteRescateProgramado.verComprobante = function() {
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'CompApor' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
				if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;
					
					var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
										"p_IdModulo"		: 'FMM',
										"p_IdUsuario"				: $scope.SesionIdUsuario,
										"p_IdSolicitudAporteRescate" : $scope.aporteRescateProgramado.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
					
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [{
					    								"type" : "pdf417",
					    								"name" : "pdf417",
					    								"data" :  bcsConfiguracionFFM.generarPdf417($scope.aporteRescateProgramado.formData.ResultadoSolicitudAporteRescate)
					    							}];
					
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 };
	 
	 $scope.aporteRescateProgramado.limpiarFMM();
	 
	 $scope.solicitarDeclaracion= function(respuesta) {
			
		 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
		 						,IdUsuario			: $scope.SesionIdUsuario
		 						,IdModulo			: "FMM"
		 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
		 						//,IdCliente			: $scope.aporteRescateProgramado.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
		 						,IdCliente			: $scope.aporteRescateProgramado.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
		 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
								,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
								,IdMedioDePago		: $scope.aporteRescateProgramado.formData.IdMedioDePago ? $scope.aporteRescateProgramado.formData.IdMedioDePago : 'CIN'
								,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
							 };
			 
			 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
		 };

	$scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.aporteRescateProgramado.oClientePortafolio.seleccionado,
			$scope.aporteRescateProgramado.datosFondo,
			$scope.aporteRescateProgramado.formData
		);
	};

});