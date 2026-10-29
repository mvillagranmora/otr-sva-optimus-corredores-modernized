angular.module('ModuloFFM').controller('IngresarAporteOvernightFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,$timeout){
	var aporteOvernight = this;
	var valoresMontoCuota = {};
	$scope.aporteOvernight.formData = {};
	$scope.aporteOvernight.masInformacion = false;
	$scope.aporteOvernight.formData.setInstrumento = "-1";
	$scope.aporteOvernight.datosFondo = {};
	$scope.aporteOvernight.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.aporteOvernight.formData.DatosAdicionales = "N";
	$scope.aporteOvernight.formData.visibleTruco = true;
	$scope.aporteOvernight.oFormaPago = {};
	aporteOvernight.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.dataTemplate = {};
	$scope.aporteOvernight.formData.idAutorizadoDarOrdenesDefecto=undefined;
	$scope.aporteOvernight.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
		
	$scope.aporteOvernight.ajustaVentana();
	
	$scope.aporteOvernight.formData.IdCategoriaInversionista = {
			"field"   : "IdCategoriaInversionista","operator": "eq", "value"   :  undefined	
	};
		
	$scope.aporteOvernight.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' }
	};
	
	$scope.aporteOvernight.formData.FiltrosComboFondo = {
		IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
		BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
	};
	
	$scope.aporteOvernight.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};
	
	$scope.aporteOvernight.oConfiguracionMedioDePago={
			tipo : "combobox-medio-pago",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.aporteOvernight.formData.filtrosFormaPago = {
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  0},
			IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   :  "-1"}
	};

//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
/*	$scope.aporteOvernight.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

	$scope.aporteOvernight.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		 },
	 };
*/
	$scope.aporteOvernight.oClientePortafolio ={
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
	
	$scope.aporteOvernight.formData.filtrosCuentaInversion = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0}			
	};
	
	$scope.aporteOvernight.formData.FiltrosAutorizarOrden = {
			IdPortafolio : { "field"   : "IdPortafolio", "operator": "eq", "value"   :  0},
			IdTipoAutorizado : {"field" : "IdTipoAutorizado", "operator":"eq", "value" : 'ORDEN-CREAR'}
	};
	
	$scope.aporteOvernight.formData.filtrosRegimen = {
		IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
		IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
		IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
		IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}	
	}
	
	 $scope.aporteOvernight.formData.masDatos = function()
	 {
		bcsComunes.ventanaAutoHeight($scope.winID);
		 if ($scope.aporteOvernight.formData.DatosAdicionales === 'S'){
			 $scope.aporteOvernight.formData.DatosAdicionales = 'N';
	     }
		 else{
			 $scope.aporteOvernight.formData.DatosAdicionales = 'S';
	     }
	 }

	
	 $scope.aporteOvernight.formData.ingresarAporteRescate = function()
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
					    //IdCliente 	          		:	$scope.aporteOvernight.oPortafolio.seleccionado.IdCliente,
				    	//IdPortafolio          		:	$scope.aporteOvernight.oPortafolio.seleccionado.IdPortafolio,
					    IdCliente 	          		:	$scope.aporteOvernight.oClientePortafolio.seleccionado.IdCliente,
				    	IdPortafolio          		:	$scope.aporteOvernight.oClientePortafolio.seleccionado.IdPortafolio,
 			    	  //Fin GCMBIPAPAS-1	
				    	IdSerieFondo          		:	$scope.aporteOvernight.datosFondo.IdSerieFondo,
				    	Monto 				  		:   $scope.aporteOvernight.formData.montoInicial,
						NumeroCuotas 	  	  		:	$scope.aporteOvernight.formData.numeroCuotas,
				    	IdMoneda 			  		:	aporteOvernight.confGlobal.oOperacion.seleccionado.IndicadorAporteRescate === 'APO' ? $scope.aporteOvernight.formData.IdMonedaAporte : $scope.aporteOvernight.datosFondo.IdMoneda,
				    	IdMedioDePago				:	$scope.aporteOvernight.formData.IdMedioDePago ? $scope.aporteOvernight.formData.IdMedioDePago : 'CIN',
				    	IdMedioDePagoRescate		:	$scope.aporteOvernight.formData.IdMedioDePagoRescate ? $scope.aporteOvernight.formData.IdMedioDePagoRescate : 'CIN',
				    	IdPreferenciaPagoPortafolioRescate : $scope.aporteOvernight.oFormaPago.seleccionado ? $scope.aporteOvernight.oFormaPago.seleccionado.IdPreferenciaPagoPortafolio : 0,
				    	Visado						:	'N',
				    	SolicitudBatch				:	'N',
				    	OrigenOperacion				: 	'M',
				    	IdRegimenTributario			:	$scope.aporteOvernight.formData.IdRegimenFondoMutuo || '',
				    	AdministracionLiquidez		:	'N',
				    	Glosa						: 	$scope.aporteOvernight.formData.comentario,
				    	IdAutorizadoOrigen			:	$scope.aporteOvernight.formData.idAutorizadoDarOrdenes || 0,
				    	FolioContrato				:	0
				 }]
		 }
	     bcsServicioFondosMutuos.CrearSolicitudAporteOvernight(datosIngresoAporteRescate).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.aporteOvernight.formData.ResultadoSolicitudAporteRescate = {
	    			    SecuenciaOperacion              : respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,
						IdOperacion						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FondoMutuo            			: respuesta.P_OUT_RESULTADO[0].IdSerieFondo,
						TipoFondo 						: respuesta.P_OUT_RESULTADO[0].IdTipoMovimientoFondoMutuo,
						FechaAporte  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacion),
						FechaRescate  			 		: util.formatoDate(respuesta.P_OUT_RESULTADO[0].FechaAceptacionRescate),
						MontoInicial	 				: respuesta.P_OUT_RESULTADO[0].CostoMonedaLocal,
						NumeroCuotas			 		: util.truncarNumero(respuesta.P_OUT_RESULTADO[0].NumeroCuotas, $scope.aporteOvernight.formData.decimalesCantidad),
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
	     		$scope.aporteOvernight.formData.IngresoConfirmacion = 'C';
	     		$scope.aporteOvernight.ajustaVentana();
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
	 
	 //$scope.$watch('aporteOvernight.oPortafolio.idActual',function(newval,oldval){		//GCMBIPAPAS-1
     $scope.$watch('aporteOvernight.oClientePortafolio.idActual',function(newval,oldval){	//GCMBIPAPAS-1
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
					 //$scope.aporteOvernight.oPortafolio.idActual = 0;			//GCMBIPAPAS-1
					 $scope.aporteOvernight.oClientePortafolio.idActual = 0;	//GCMBIPAPAS-1
					 bcsComunes.ventanaMsg({conflictos : util.parseServerResponse(respuesta)},$scope.winID);
				}
			 })
		 }
	 })
	 
	 $scope.$watch('aporteOvernight.datosFondo',function(newval,oldval){
		 if (newval && newval.IdFondo) {
				$scope.aporteOvernight.formData.decimalesCantidad = newval.DecimalesCuota;
				$scope.aporteOvernight.formData.filtrosRegimen.IdSerieFondo.value = newval.IdSerieFondo;
				bcsConfiguracionFFM.calcularMontoCuota(newval,$scope.aporteOvernight.formData.montoInicial,$scope.aporteOvernight.formData.numeroCuotas,'APO',$scope.aporteOvernight.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
				.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.aporteOvernight.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.aporteOvernight.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.aporteOvernight.formData.numeroCuotas = 0;
						 $scope.aporteOvernight.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.aporteOvernight.formData.numeroCuotas = 0;
					 $scope.aporteOvernight.formData.montoInicial = 0;
		  		 });
				$scope.aporteOvernight.formData.IdMonedaAporte = newval.IdMoneda;
				$scope.asignarRegimenPorDefecto(); 
		    }
			else {
				$scope.aporteOvernight.formData.decimalesCantidad = 0;
				$scope.aporteOvernight.formData.numeroCuotas = 0;
				$scope.aporteOvernight.formData.montoInicial = 0;
				$scope.aporteOvernight.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
			}
		 $scope.aporteOvernight.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteOvernight.formData.IdMonedaAporte', function(newval,oldaval){
		 if(newval){
			 $scope.aporteOvernight.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',newval,'','').cantidadDecimales;
		 }else{
			 $scope.aporteOvernight.formData.decimalesMonto = 0;
		 }
		 $scope.aporteOvernight.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteOvernight.formData.montoInicial',function(newval,oldval){
		if(newval && newval != oldval){
			bcsConfiguracionFFM.calcularMontoCuota($scope.aporteOvernight.datosFondo,newval,$scope.aporteOvernight.formData.numeroCuotas,'APO',$scope.aporteOvernight.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
					 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
						 $scope.aporteOvernight.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
						 $scope.aporteOvernight.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
					 }else{
						 $scope.aporteOvernight.formData.numeroCuotas = 0;
						 $scope.aporteOvernight.formData.montoInicial = 0;
					 }
				 },function(respuesta){
					 $scope.aporteOvernight.formData.numeroCuotas = 0;
					 $scope.aporteOvernight.formData.montoInicial = 0;
		  		 });
		}
	 });
	 
	 $scope.$watch('aporteOvernight.masInformacion',function(newval,oldval){
		 $scope.aporteOvernight.ajustaVentana();
	 });
	 
	 $scope.$watch('aporteOvernight.formData.numeroCuotas',function(newval,oldval){
		 if(newval && newval != oldval){
			 bcsConfiguracionFFM.calcularMontoCuota($scope.aporteOvernight.datosFondo,$scope.aporteOvernight.formData.montoInicial,newval,'APO',$scope.aporteOvernight.formData.IdMonedaAporte,'M',$scope.SesionFechaSistema)
			 .then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 $scope.aporteOvernight.formData.numeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
					 $scope.aporteOvernight.formData.montoInicial = respuesta.P_OUT_RESULTADO[0].MontoDestino;
				 }else{
					 $scope.aporteOvernight.formData.numeroCuotas = 0;
					 $scope.aporteOvernight.formData.montoInicial = 0;
				 }
			 },function(respuesta){
				 $scope.aporteOvernight.formData.numeroCuotas = 0;
				 $scope.aporteOvernight.formData.montoInicial = 0;
	  		 });
		 }
	 });
	 
	 $scope.$watch('oOperacion.seleccionado', function(newval,oldval){
		 if (newval && newval.IdTipoMovimientoFondoMutuo){
			 if(newval.IndicadorAporteRescate != oldval.IndicadorAporteRescate){
				 $scope.aporteOvernight.formData.montoInicial = undefined;
				 $scope.aporteOvernight.formData.numeroCuotas = undefined;
			 }
			 
			 $scope.aporteOvernight.ajustaVentana();
		 }
		 
	 });
	 /* se quitar por IMPSURAOPT-267
	 $scope.$watch('aporteOvernight.formData.IdCategoriaInversionista.value', function(newval,oldval){
		 if(newval){
			 var jerarquia = aporteOvernight.confGlobal.JerarquiaPerfil[newval];
			 $scope.aporteOvernight.formData.FiltrosBusquedaFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
			 $scope.aporteOvernight.formData.FiltrosComboFondo.Jerarquia = { "field"   : "Jerarquia","operator": "gte", "value"   :  jerarquia};
		 }
	 });
	 */
	 $scope.aporteOvernight.limpiarFMM = function(volver){
		 if(!volver){
			 $scope.aporteOvernight.formData.tipoOperacion = 0;
		 }
		 $scope.aporteOvernight.formData.idAutorizadoDarOrdenes=undefined;
		 $scope.aporteOvernight.formData.IngresoConfirmacion = 'I';
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
		 //$scope.aporteOvernight.oSocioNegocio.idActual = 0;		 
		 //$scope.aporteOvernight.oPortafolio.idActual = 0;
		 $scope.aporteOvernight.oClientePortafolio.idActual = 0;
		//Fin GCMBIPAPAS-1			 
		 $scope.aporteOvernight.formData.setFondo = '-1';
		 $scope.aporteOvernight.formData.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		 $scope.aporteOvernight.formData.montoInicial = undefined;
		 $scope.aporteOvernight.formData.numeroCuotas = undefined;
		 $scope.aporteOvernight.formData.montoComisiones = undefined;
		 $scope.aporteOvernight.formData.IdFormaPago = '0';
		 $scope.aporteOvernight.formData.comentario = '';
		 $scope.aporteOvernight.formData.FiltrosBusquedaFondo = {
			IdCategoriaPerfilFondoMutuo : { "field"   : "IdCategoriaPerfilFondoMutuo","operator": "eq", "value"   :  undefined},
		    IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  undefined},
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  undefined},
			IdEstado : { field: "IdEstado", operator: "eq", value:'ACT' },
			BloqueoAporte : { field: "BloqueoAporte", operator: "eq", value:'N' },
			BloqueoRescate : { field: "BloqueoRescate", operator: "eq", value:'N' }
		 };
		 $scope.aporteOvernight.ajustaVentana();
	 }
	 
	 $scope.aporteOvernight.formData.resetFoco =  function() {
	 	$scope.aporteOvernight.formData.focoVolver = false;
		if($scope.aporteOvernight.formData.IngresoConfirmacion === 'I')
		{
			$scope.aporteOvernight.formData.focoOperacion = true;
			$scope.aporteOvernight.formData.focoVolver = false;
		}
	 };
	 
	 
	 $scope.aporteOvernight.formData.verComprobante = function() {
			
			var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: 'CompOver' },
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
										"p_IdSolicitudAporteRescate" : $scope.aporteOvernight.formData.ResultadoSolicitudAporteRescate.SecuenciaOperacion,
										
			                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
										  };
			
					$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
					$scope.dataTemplate.parameters= [{
						"type" : "pdf417",
						"name" : "pdf417",
						"data" :  bcsConfiguracionFFM.generarPdf417($scope.aporteOvernight.formData.ResultadoSolicitudAporteRescate)
					}];
					
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;
					
					bcsConfiguracionFFM.getReport($scope.dataTemplate);
				}
			});
	 };
	 
	 $scope.aporteOvernight.limpiarFMM();

	 $scope.solicitarDeclaracion= function(respuesta) {
			
		 	var parametrosDOF = {IdEmpresa			: $scope.$root.SesionIdEmpresa
		 						,IdUsuario			: $scope.SesionIdUsuario
		 						,IdModulo			: "FMM"
		 						,IdTipoOperacion	: $scope.oOperacion.seleccionado.IdTipoOperacion
		 						//,IdCliente			: $scope.aporteOvernight.oPortafolio.seleccionado.IdCliente		//GCMBIPAPAS-1
		 						,IdCliente			: $scope.aporteOvernight.oClientePortafolio.seleccionado.IdCliente	//GCMBIPAPAS-1
		 						,MontoOperacion 	: respuesta.P_OUT_RESULTADO[0].Monto
								,IdMonedaOperacion	: respuesta.P_OUT_RESULTADO[0].IdMoneda
								,IdMedioDePago		: $scope.aporteOvernight.formData.IdMedioDePago ? $scope.aporteOvernight.formData.IdMedioDePago : 'CIN'
								,FechaOperacion		: respuesta.P_OUT_RESULTADO[0].FechaLiquidacion
							 };
			 
			 bcsNormativaDOF.evaluarOperacion($scope.winID,parametrosDOF);									
		 };

	$scope.asignarRegimenPorDefecto = function () {
		bcsConfiguracionFFM.asignarRegimenPorDefecto(
			$scope.aporteOvernight.oClientePortafolio.seleccionado,
			$scope.aporteOvernight.datosFondo,
			$scope.aporteOvernight.formData
		);
	};
});