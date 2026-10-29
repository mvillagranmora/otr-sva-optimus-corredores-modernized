angular.module('ModuloFFM').controller('IngresarRescateReinversion1NFFMCtrl',function($rootScope,$scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsNormativaDOF,bcsServicioPlataforma,$timeout){
	var rescateReinversion1N = this; 
	$scope.rescateReinversion1N.formData = {};
	$scope.rescateReinversion1N.datosFondo = {};
	$scope.rescateReinversion1N.cmbbx = {};
	$scope.dataTemplate = {};
	$scope.rescateReinversion1N.PorcentajeTotalDestino=0;
	$scope.rescateReinversion1N.masInformacion = false;
	$scope.rescateReinversion1N.formData.permiteSwitchDestino = false;
	$scope.rescateReinversion1N.formData.modelSeleccionado = null;
	$scope.rescateReinversion1N.formData.setInstrumento = "-1";
	$scope.rescateReinversion1N.formData.IngresoConfirmacion = 'I';
	$scope.winID=bcsComunes.ventanaWinId($scope);
	rescateReinversion1N.domVentana = $("[ventanaid='"+$scope.winID+"']");
	$scope.rescateReinversion1N.formData.DatosAdicionales = "N";
	$scope.rescateReinversion1N.formData.disabledMlt = false;
	$scope.rescateReinversion1N.formData.igualRegimenDisabled = true;
	$scope.rescateReinversion1N.formData.idRegimeFondoMutuoActivado = false;
	rescateReinversion1N.confGlobal = $scope.aporteRescateMain.confGlobal;
	$scope.rescateReinversion1N.formData.idAutorizadoDarOrdenesDefecto=undefined;
	$scope.rescateReinversion1N.ajustaVentana = function()
    {
		 bcsComunes.ventanaCambiarTamano($scope.winID,1100,650);
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	var parametros = {};		
	parametros.p_IdEmpresa 	= $scope.SesionIdEmpresa;
	parametros.p_IdUsuario 	= $scope.SesionIdUsuario;
	parametros.p_IdModulo  	= "FMM";
	parametros.p_IdEstado  	= "ACT";
	parametros.p_IdMoneda  	= "-";
	parametros.p_IdFamiliaFondo = "-";
	parametros.p_FondoExtranjero = "-";
	parametros.p_IdEmisor = "-";
	parametros.p_IdRegimenFondoMutuo = "-";
	parametros.p_IdSerieFondoOrigen = "-";
	
	$scope.rescateReinversion1N.ajustaVentana();
	
	$scope.rescateReinversion1N.btnCerrar = function(refrescar) {
		bcsComunes.ventanaData($scope.winID,refrescar);
		bcsComunes.ventanaCerrar($scope.winID);
	};
	
	$scope.rescateReinversion1N.cmbbx.oClientePortafolio ={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.rescateReinversion1N.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.rescateReinversion1N.cmbbx.filtrosRegimen = {
			IdSerieFondo : { "field"   : "IdSerieFondo", "operator": "eq", "value"	 :	undefined},
			IdEstadoRegimenSerieDeFondo : {"field": "IdEstadoRegimenSerieDeFondo", "operator": "eq", "value":'ACT'},
			IdEstadoRegimenFondoMutuo : {"field": "IdEstadoRegimenFondoMutuo", "operator": "eq", "value":'ACT'},
			IdEmpresa : {"field" : "IdEmpresa", "operator":"eq", "value" : $scope.SesionIdEmpresa}
	}
	
	
	var dsDestinos = new bcs.DataSource({
		type: "call",
		definition : {
            pageSize        : 6,
            callTransaction : "600078",
            callResultSet   : "P_OUT_LISTA",
			schema: {
				model: {
					fields: {
				    	IdSerieFondo          		:	{ type: "string"},
				    	NombreSerieFondo			:	{ type: "string"},
				    	IdRegimenFondoMutuo			:	{ type: "string"},
				    	IdMoneda                    :	{ type: "string"}, 
				    	//PatrimonioMinimo            : 	{ type: "number"},
				    	MontoPatrimonioEmisor       : 	{ type: "number"},
				    	MontoMinimo                 : 	{ type: "number"},  
				    	ValorCuotaInstrumento		: 	{ type: "number"},
				    	IntMontoTraspaso			: 	{ type: "number"},//calculado
				    	NumeroCuotasInstrumento 	: 	{ type: "number"},//calculado
	        			IntPorcentajeTraspaso		: 	{ type: "number"},//calculado
					}
				}
			}
		}	
	});
	
	$scope.rescateReinversion1N.Solicitudes = {
			dataSource: dsDestinos,
			columns: [
				    {field: "IdSerieFondo",				title: $translate.instant('UI_INSTRUMENTO'),					width: "100px", align: "left"},
				    {field: "NombreSerieFondo",			title: $translate.instant('UI_NOMBRE'),							width: "100px", align: "left"},
				    {field: "IdRegimenFondoMutuo",		title: $translate.instant('UI_REGIMEN'),						width: "100px", align: "left"},
				    {field: "IdMoneda",		            title: $translate.instant('UI_MONEDA'),		  				    width: "60px", align: "left"},
				    //{field: "PatrimonioMinimo",	        title: $translate.instant('UI_MONTO_MINIMO_PATRIMONIO'),		width: "100px", align: "right", template : " <label> #: util.bcs_formato(PatrimonioMinimo,'FMM','Monto',IdMoneda,'','').valor #</label>", editable: false},
				    {field: "MontoPatrimonioEmisor",	title: $translate.instant('UI_MINIMO_PATRIMONIO_EMISOR'),		width: "100px", align: "right", template : " <label> #: util.bcs_formato(MontoPatrimonioEmisor,'FMM','Monto',IdMoneda,'','').valor #</label>", editable: false},
				    {field: "MontoMinimo",	            title: $translate.instant('UI_MONTO_MINIMO'),				    width: "100px", align: "right", template : " <label> #: util.bcs_formato(MontoMinimo,'FMM','Monto',IdMoneda,'','').valor #</label>", editable: false},
				    {field: "ValorCuotaInstrumento",	title: $translate.instant('UI_VALOR_CUOTA'),					width: "100px", align: "right", template : " <label> #: util.bcs_formato(ValorCuotaInstrumento,'FMM','Cantidad',IdMoneda,'','').valor #</label>", editable: false},
				    {field: "IntMontoTraspaso",			title: $translate.instant('UI_MONTO_TRASPASO'),					width: "100px", align: "right", template : " <label> #: util.bcs_formato(IntMontoTraspaso,'FMM','Cantidad',IdMoneda,'','').valor #</label>", editable: false},
				    {field: "NumeroCuotasInstrumento",	title: $translate.instant('UI_NUMERO_CUOTAS'),					width: "100px", align: "right", template : " <label> #: util.bcs_formato(NumeroCuotasInstrumento,'FMM','Precio',IdMoneda,'','').valor #</label>", editable: false},
					{field: "IntPorcentajeTraspaso",	title: $translate.instant('UI_PORCENTAJE_TRASPASO'),			width: "100px", align: "right", editable: true}
				    ],	
			sortable		: true,
			filterable		: true,
			pageable		: true,
			autoBind		: true,
			height			: 160,
			selectable		: "row",
		    userSettings	: false,
			navigatable		: true,
			save: function(e) 	{calculoMontoDestino(e.values, e.model);},
			gridCreated     : function(g){
				rescateReinversion1N.ApiDestinos = g;
				dsDestinos.setCallParameters(parametros);
				$scope.rescateReinversion1N.ajustaVentana();
			},
			notEditableColumns : function(fila)
			{
				if (fila.IdSerieFondo == '') 
				{
					fila.IntMontoTraspaso = 0;
					fila.IntPorcentajeTraspaso = 0;
					fila.ValorCuotaInstrumento = 0;
					fila.NumeroCuotasInstrumento = 0;
					return ['IntPorcentajeTraspaso'];
				}
			}
	};
	
	var defSaldoCustodia = {
		type : "call",
		definition : {
			serverFiltering : false, // Filtrado de datos en el servidor.
            serverPaging    : false, // Paginado de los datos en el servidor.
            serverSorting   : false, // Ordenamiento de los datos en el servidor.
            pageSize        : 6,
            sort 			: false,
            callTransaction : "700188",
            callResultSet   : "P_OUT_TBL",
            filters			: {field: "CantidadCustodia", operator: "gt", value: Number('0')},
			schema: {
				model: {
					fields: {
						IdInstrumento					   		: { type: "number"},
	        			IdCliente						   		: { type: "number"},
	        			IdPortafolio					   		: { type: "number"},
	        			CodigoPortafolio				   		: { type: "string"},
	        			IdMoneda						   		: { type: "string"},
	        			Nemotecnico						   		: { type: "string"},
	        			IdRegimenFondoMutuo						: { type: "string"},
	        			ValorCuota						   		: { type: "number"},
	        			CantidadCustodia						: { type: "number"},
	        			Monto									: { type: "number"},
	        			ValorCuota								: { type: "number"},
	        			PorcentajeRescate				   		: { type: "number"},//calculado 
	        			MontoRescate						   	: { type: "number"},//calculado
	        			NumeroCuotas							: { type: "number"},//calculado
	        			CantidadTransSolicitudesAporte			: { type: "number"},
	        			CantidadTransSolicitudesRescate			: { type: "number"},
	        			CantidadTransEgr						: { type: "number"},
	        			CantidadTransIng						: { type: "number"},
	        			IdDetalleTraspaso						: { type: "number"}       
					}
				}
			}
		}	
	};
	
	var dsSaldoCustodia = new bcs.DataSource(defSaldoCustodia);
	rescateReinversion1N.SaldoCustodia = {
			dataSource: dsSaldoCustodia,
			columns: [
						{field: "Nemotecnico",                   			title: $translate.instant('UI_INSTRUMENTO'),                   		width: "100px", align: "left"},
						{field: "IdRegimenFondoMutuo",                   	title: $translate.instant('UI_REGIMEN'),                   			width: "100px", align: "left"},
						{field: "CantidadCustodia",							title: $translate.instant('UI_CUSTODIA'),							width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadCustodia,'FMM','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "IdMoneda",							    	title: $translate.instant('UI_MONEDA'),			    				width: "75px",	align: "left"},
						{field: "ValorCuota",								title: $translate.instant('UI_PRECIO'),							    width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(ValorCuota,'FMM','Precio',IdMoneda, '','')).valor #</label>"},
						{field: "Monto",									title: $translate.instant('UI_MONTO'),								width: "100px", align: "left", template : " <label> #:  (util.bcs_formato(Monto,'FMM','Monto',IdMoneda, '','')).valor #</label>"},
						{field: "PorcentajeRescate",						title: $translate.instant('UI_PORCENTAJE_RESCATE'),					width: "100px", align: "right", editable: true},
						{field: "MontoRescate",								title: $translate.instant('UI_MONTO_RESCATE'),						width: "100px", align: "right", template : " <label> #: util.bcs_formato(MontoRescate,'FMM','Monto',IdMoneda,'','').valor #</label>", editable: false},
						{field: "NumeroCuotas",								title: $translate.instant('UI_MONTO_RESCATE_CUOTAS'),				width: "100px", align: "right", template : " <label> #: util.bcs_formato(NumeroCuotas,'FMM','Cantidad',IdMoneda,'','').valor #</label>", editable: false},
						{field: "CantidadTransSolicitudesAporte",			title: $translate.instant('UI_APORTES_TRANSITO'),					width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransSolicitudesAporte,'FMM','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "CantidadTransSolicitudesRescate",			title: $translate.instant('UI_RESCATES_TRANSITO'),					width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransSolicitudesRescate,'FMM','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "CantidadTransEgr",              			title: $translate.instant('UI_EGRESOS_TRANSITO'),           		width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransEgr,'FMM','Cantidad',IdMoneda,'','').valor #</label>"},
						{field: "CantidadTransIng",              			title: $translate.instant('UI_INGRESO_TRANSITO'),           		width: "100px", align: "left", template : " <label> #: util.bcs_formato(CantidadTransIng,'FMM','Cantidad',IdMoneda,'','').valor #</label>"}
				    ],	
			sortable		: false,
			filterable		: true,
			pageable		: true,
			autoBind		: true,
			height			: 160,
			selectable		: "row",
		    userSettings	: false,
			navigatable		: true,
			save: function(e) 	{calculoMontoATraspasar(e.values, e.model);},		
			gridCreated     : function(g){
				$scope.ApiMovimientos = g;
				$scope.rescateReinversion1N.ajustaVentana();
			},
			notEditableColumns : function(fila)
			{
				if (fila.CantidadCustodia == 0 || (rescateReinversion1N.NumeroCuotasReal > 0 && (fila.PorcentajeRescate ==null ||fila.PorcentajeRescate ==0)))
				{
					fila.PorcentajeRescate = 0;
					return ['PorcentajeRescate'];
				}
			}
	};	
	
	
	/*********************************************
	 * WATCH
	 ********************************************/
	
	$scope.$watch('rescateReinversion1N.formData.modelSeleccionado', function(newval, oldval){
		if(newval){
			$scope.rescateReinversion1N.cmbbx.filtrosRegimen.IdSerieFondo.value = rescateReinversion1N.formData.modelSeleccionado.IdSerieFondo;
			rescateReinversion1N.formData.igualRegimenDisabled = false;
			rescateReinversion1N.formData.idRegimeFondoMutuoActivado = true;			
			rescateReinversion1N.buscarDestinos();
		}
	});
	
	$scope.$watch('rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado',function(newval,oldval){
		if (newval){
			rescateReinversion1N.buscarCustodia();
			rescateReinversion1N.limpiar();
		};	
	});
	
	$scope.$watch('rescateReinversion1N.formData.MLT',function(newval,oldval){
		if (newval != oldval && rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado){
			rescateReinversion1N.buscarCustodia();
			rescateReinversion1N.limpiar();
		};	
	});
	
	$scope.$watch('rescateReinversion1N.formData.igualRegimen',function(newval,oldval){
		if (newval != oldval && $scope.rescateReinversion1N.formData.modelSeleccionado != null){
			$scope.rescateReinversion1N.cmbbx.filtrosRegimen.IdSerieFondo.value = rescateReinversion1N.formData.modelSeleccionado.IdSerieFondo;
			rescateReinversion1N.formData.idRegimeFondoMutuoActivado = true;
			rescateReinversion1N.buscarDestinos();
		};	
	});
	
	$scope.$watch('rescateReinversion1N.MontoTotalRescate',function(newval,oldval){
		calculoMontoDestinoGrilla();
	});
	
	$scope.$watchGroup(['rescateReinversion1N.formData.permiteSwitch', 'rescateReinversion1N.cmbbx.IdEmisor',
		'rescateReinversion1N.formData.igualMoneda', 'rescateReinversion1N.formData.igualFamilia'],function(newval,oldval){
		if (newval != oldval && $scope.rescateReinversion1N.formData.modelSeleccionado != null){
			rescateReinversion1N.buscarDestinos();
		};	
	});
	
	/*********************************************
	 * VARIABLES PARAMETROS
	 ********************************************/
	var apiGridCustodiaFondosMutuos = rescateReinversion1N.SaldoCustodia.dataSource;
	
	/*********************************************
	 * FUNCIONES
	 ********************************************/
	
	rescateReinversion1N.preparaParametros = function()
	{
		parametrosConsulta = {};
		parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
		parametrosConsulta.p_IdModulo = "FMM";
		parametrosConsulta.p_AgrupaPorBoveda = "S";
		parametrosConsulta.p_IdCliente = 0;
		parametrosConsulta.p_IdPortafolio = 0;
		parametrosConsulta.p_IdProductoAgrupador = null;
		parametrosConsulta.p_IdInstrumento = 0;
		parametrosConsulta.p_FechaConsulta = $scope.SesionFechaSistema;
		parametrosConsulta.p_BuscaMLT = $scope.rescateReinversion1N.formData.MLT ? 'S' : 'N';
	
		parametrosConsulta.p_IdCliente = (rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado) ? rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado.IdCliente : 0;
		parametrosConsulta.p_IdPortafolio = (rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado) ? rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado.IdPortafolio : 0;
		
		return parametrosConsulta;
	};
	
	rescateReinversion1N.buscarCustodia = function ()
	{
		apiGridCustodiaFondosMutuos.setCallParameters(rescateReinversion1N.preparaParametros());
		var filters = new Array();
		if(rescateReinversion1N.formData.MLT){
			filters.push({field : "MLT", operator : "eq", value: rescateReinversion1N.formData.MLT == true ? 'S' : 'N' });
		}
		filters.push({field : "CantidadCustodia", operator : "gt", value: Number(0)});
		apiGridCustodiaFondosMutuos.filter(filters)
		apiGridCustodiaFondosMutuos.read();
		
		//Se deja una fila oculta bloquedada por defecto
		rescateReinversion1N.SaldoCustodia.dataSource.add({Nemotecnico: "",
			CantidadCustodia 			:	Number('0'),
			IdMoneda 					:	"", 
			ValorCuota  	            :	Number('0'), 
			Monto                       :	Number('0'),
			PorcentajeRescate	 		:   Number('0'),
			MontoRescate	 			:	Number('0'),
			NumeroCuotas	 			:	Number('0'),
			CantidadTransSolicitudesAporte 			:	Number('0'),
			CantidadTransSolicitudesRescate 		:	Number('0'),
			CantidadTransEgr 			:	Number('0'),
			CantidadTransIng 			:	Number('0')
		});
	};
	
	rescateReinversion1N.buscarDestinos = function ()
	{
		rescateReinversion1N.Solicitudes.dataSource.removeAll();

		rescateReinversion1N.MontoTotalDestino=0;
		rescateReinversion1N.PorcentajeTotalDestino=0;
		rescateReinversion1N.Solicitudes.dataSource.add({IdSerieFondo: "",
			NombreSerieFondo 			:"",
			IdMoneda					:$rootScope.SesionIdMonedaSistema,
			IntMontoTraspaso 			:Number('0'),
			IntPorcentajeTraspaso 		:Number('0'),
			//PatrimonioMinimo            :Number('0'), 
			MontoPatrimonioEmisor       :Number('0'),
			MontoMinimo                 :Number('0'), 
			MontoMinimo                 :Number('0'), 
			ValorCuotaInstrumento       :Number('0'),
			NumeroCuotasInstrumento     :Number('0')
		});
		
		parametros.p_IdMoneda  	= rescateReinversion1N.formData.igualMoneda && rescateReinversion1N.formData.modelSeleccionado != null ? 
				rescateReinversion1N.formData.modelSeleccionado.IdMoneda : "-";
		parametros.p_IdFamiliaFondo = rescateReinversion1N.formData.igualFamilia && rescateReinversion1N.formData.modelSeleccionado != null ? rescateReinversion1N.formData.modelSeleccionado.IdFamiliaFondo : "-";
		
		if(rescateReinversion1N.formData.igualRegimen && rescateReinversion1N.formData.modelSeleccionado != null)
			parametros.p_IdRegimenFondoMutuo = rescateReinversion1N.formData.modelSeleccionado.IdRegimenFondoMutuo;
		else
			parametros.p_IdRegimenFondoMutuo = "-";
	
		if(rescateReinversion1N.formData.permiteSwitch && rescateReinversion1N.formData.modelSeleccionado != null){
			parametros.p_FondoExtranjero = "S";
			parametros.p_IdEmisor = rescateReinversion1N.formData.modelSeleccionado.IdEmisor;
			$scope.rescateReinversion1N.cmbbx.IdEmisor = rescateReinversion1N.formData.modelSeleccionado.IdEmisor;
		}
		else if(rescateReinversion1N.cmbbx.IdEmisor != 0 && rescateReinversion1N.formData.modelSeleccionado != null){
			parametros.p_FondoExtranjero = "-";
			parametros.p_IdEmisor = rescateReinversion1N.cmbbx.IdEmisor;
		}
		else{
			parametros.p_FondoExtranjero = "-";
			parametros.p_IdEmisor = "-";
		}
		
		if(rescateReinversion1N.formData.modelSeleccionado != null)
			parametros.p_IdSerieFondoOrigen = rescateReinversion1N.formData.modelSeleccionado.IdSerieFondo;
		else
			parametros.p_IdSerieFondoOrigen = "-";
		
		dsDestinos.setCallParameters(parametros);
	};
	 
	 var calculoMontoATraspasar = function(values, model){
		bcsComunes.ventanaOverlay($scope.winID,true); 
		if(model.FondoExtranjero == 'S')
			$scope.rescateReinversion1N.formData.permiteSwitchDestino = true;
		else
			$scope.rescateReinversion1N.formData.permiteSwitchDestino = false;
		 
		rescateReinversion1N.formData.modelSeleccionado = model;
		var Traspaso = rescateReinversion1N.SaldoCustodia.dataSource.data() || [];
		rescateReinversion1N.MontoTotalRescate=0;
		$scope.rescateReinversion1N.NumeroCuotasReal = (Number(values.PorcentajeRescate)/Number(100)) * Number(model.CantidadCustodia);
		
		var datosFondo = {};
		datosFondo.IdSerieFondo = model.IdSerieFondo;
		datosFondo.IdMoneda = model.IdMoneda;
		datosFondo.IdEmpresa = $scope.SesionIdEmpresa;

		$scope.rescateReinversion1N.fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
		
		bcsConfiguracionFFM.calcularMontoCuota(datosFondo,0,rescateReinversion1N.NumeroCuotasReal,'RES',model.IdMoneda,'C',$scope.SesionFechaSistema)
		.then(function(respuesta){
			 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
				 model.MontoRescate = respuesta.P_OUT_RESULTADO[0].Monto;
				 model.NumeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
				 $scope.rescateReinversion1N.MontoTotalRescateReal = respuesta.P_OUT_RESULTADO[0].Monto;
				 rescateReinversion1N.NumeroCuotas = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
				 rescateReinversion1N.decimalesMontoTotalRescate = util.bcs_formato(0,'FMM','Monto',datosFondo.IdMoneda,'','').cantidadDecimales;
				 rescateReinversion1N.IdMonedaMontoTotalRescate = model.IdMoneda;
				 rescateReinversion1N.MontoTotalRescate = respuesta.P_OUT_RESULTADO[0].Monto;
				 
				 rescateReinversion1N.decimalesMontoTotalRescateMonedaSistema = util.bcs_formato(0,'FMM','Monto', respuesta.P_OUT_RESULTADO[0].IdMonedaSistema,'','').cantidadDecimales;
				 rescateReinversion1N.IdMonedaMontoTotalRescateMonedaSistema = respuesta.P_OUT_RESULTADO[0].IdMonedaSistema;
				 rescateReinversion1N.MontoTotalRescateMonedaSistema = respuesta.P_OUT_RESULTADO[0].MontoMonedaSistema;
				 
				 rescateReinversion1N.decimalesCantidad = respuesta.P_OUT_RESULTADO[0].DecimalesNumeroCuotas;
			 }
			 else{
				 $scope.rescateReinversion1N.MontoTotalRescateReal = 0;
				 rescateReinversion1N.MontoTotalRescate = 0;
				 rescateReinversion1N.decimalesMontoTotalRescate = 0;
				 rescateReinversion1N.IdMonedaMontoTotalRescate = "";
				 rescateReinversion1N.decimalesMontoTotalRescateMonedaSistema = 0;
				 rescateReinversion1N.IdMonedaMontoTotalRescateMonedaSistema = "";
				 rescateReinversion1N.MontoTotalRescateMonedaSistema = 0;
			 }
			 var pas= rescateReinversion1N.SaldoCustodia.dataSource.page();
			 rescateReinversion1N.SaldoCustodia.dataSource.fetchPage(rescateReinversion1N.SaldoCustodia.dataSource.totalPages());
			 rescateReinversion1N.SaldoCustodia.dataSource.fetchPage(pas);
			 rescateReinversion1N.SaldoCustodia.notEditableColumns(model);
			 bcsComunes.ventanaOverlay($scope.winID,false);
		 },function(error){
			 bcsComunes.ventanaOverlay($scope.winID,false);
		 });
		 
	 };
	 
	var calculoMontoDestino = function(values, model){
		if(Number($scope.rescateReinversion1N.NumeroCuotasReal) > 0 && Number($scope.rescateReinversion1N.MontoTotalRescateReal) > 0){
			bcsComunes.ventanaOverlay($scope.winID,true);
			var datosFondo = {};
			datosFondo.IdSerieFondo = model.IdSerieFondo;
			datosFondo.IdMoneda = model.IdMoneda;
			datosFondo.IdEmpresa = $scope.SesionIdEmpresa;
			var monto = Number(rescateReinversion1N.MontoTotalRescate) * (Number(values.IntPorcentajeTraspaso)/Number(100));
			bcsConfiguracionFFM.calcularMontoCuota(datosFondo,monto,0,'APO', rescateReinversion1N.IdMonedaMontoTotalRescate,'M',$scope.SesionFechaSistema)
			.then(function(respuesta){
				 if (respuesta.P_OUT_RESULTADO[0] !== undefined){
					 model.IntMontoTraspaso = respuesta.P_OUT_RESULTADO[0].Monto;
					 model.NumeroCuotasInstrumento = respuesta.P_OUT_RESULTADO[0].NumeroCuotas;
				 }
				 else{
					 model.IntMontoTraspaso = 0;
					 model.NumeroCuotasInstrumento = 0;
				 }
				 var pas= rescateReinversion1N.Solicitudes.dataSource.page();
				 rescateReinversion1N.Solicitudes.dataSource.fetchPage(rescateReinversion1N.Solicitudes.dataSource.totalPages());
				 rescateReinversion1N.Solicitudes.dataSource.fetchPage(pas);
				 bcsComunes.ventanaOverlay($scope.winID,false);
			 },function(error){
				 bcsComunes.ventanaOverlay($scope.winID,false);
			 });
		}
	};
	
	
	var calculoMontoDestinoGrilla = function(values, model){
		if(Number($scope.rescateReinversion1N.NumeroCuotasReal) > 0 && Number($scope.rescateReinversion1N.MontoTotalRescateReal) > 0){
			bcsComunes.ventanaOverlay($scope.winID,true);
			var Destino = rescateReinversion1N.Solicitudes.dataSource.data() || [];
			var idInterno = 1;
			var seriesCalculo = [];
			$.each(Destino,function(key,value){
				if(value.IntPorcentajeTraspaso && value.IntPorcentajeTraspaso !== 0){
					value.IdInterno = idInterno;
					var monto = Number(rescateReinversion1N.MontoTotalRescate) * (Number(value.IntPorcentajeTraspaso)/Number(100));
					seriesCalculo.push({
						IdInterno:value.IdInterno,
						Fecha:$scope.SesionFechaSistema,
						IdSerieFondo:value.IdSerieFondo,
						CalculoPorMontoCuota:'M',
						Monto:monto,
						NumeroCuotas: 0,
						IdMonedaFondo:value.IdMoneda,
						IdMonedaInversion:rescateReinversion1N.IdMonedaMontoTotalRescate,
						TipoSolicitud:value.TipoSolicitud,
						TipoFactorAjuste:'UF',
						IdEmpresa:$rootScope.SesionIdEmpresa
					});
					idInterno++;
				}
			});
			
			if(seriesCalculo.length > 0){
				bcsConfiguracionFFM.calcularMontoCuotaMasivo(seriesCalculo)
				.then(function(respuesta){
					$.each(Destino,function(key,value){
						if(value.IntPorcentajeTraspaso && value.IntPorcentajeTraspaso !== 0){ 
							value.IntMontoTraspaso = 0;
							value.NumeroCuotasInstrumento = 0;
							$.each(respuesta.P_OUT_RESULTADO,function(id,resultado){
								if(resultado.IdInterno === value.IdInterno){
									value.IntMontoTraspaso = resultado.Monto;
									value.NumeroCuotasInstrumento = resultado.NumeroCuotas;
								}
							});
						}
					})
					var pas= rescateReinversion1N.Solicitudes.dataSource.page();
					rescateReinversion1N.Solicitudes.dataSource.fetchPage(rescateReinversion1N.Solicitudes.dataSource.totalPages());
					rescateReinversion1N.Solicitudes.dataSource.fetchPage(pas);
					bcsComunes.ventanaOverlay($scope.winID,false);
				 },function(error){
					bcsComunes.ventanaOverlay($scope.winID,false);
				 });
			}else{
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
			
		}
	};
	
	
	rescateReinversion1N.ingresar = function ()
	{
		bcsComunes.ventanaOverlay($scope.winID,true);
		
		var Origen 	= rescateReinversion1N.SaldoCustodia.dataSource.data() 	|| [];
		var Destino = rescateReinversion1N.Solicitudes.dataSource.data() 	|| [];
		
	 	if (rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado==undefined || rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado==0){
	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("SELECCIONAR_CLIENTE"));
	 		bcsComunes.ventanaOverlay($scope.winID,false);
	 		return;
	 	}
		
		//aportes
		var tablaDestino = [];
		var j = 1;
		$.each(Destino,function(key,value){
			if (Number(value.IntPorcentajeTraspaso)>0){
				tablaDestino.push({
					"IdInterno"		      	:	j,
				    "IdCliente" 	        :	$scope.rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado.IdCliente,
			    	"IdPortafolio"          :	$scope.rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado.IdPortafolio,
			    	"IdSerieFondo"          :	value.IdSerieFondo,
			    	"IdEmisor"              :   value.IdEmisor,
			    	"CalculoPorMontoCuota"  :	'M',
			    	"Monto" 				:   value.IntMontoTraspaso,
					"NumeroCuotas" 	  	  	:	value.NumeroCuotasInstrumento, //$scope.rescateReinversion1N.NumeroCuotasReal,
					"IndicadorMLT"          :   $scope.rescateReinversion1N.formData.MLT ? 'S' : 'N',
					"IdMoneda" 			  	:	value.IdMoneda,
					"IdMedioDePago"			:	'CIN',
					"IdRegimenTributario"	:	value.IdRegimenFondoMutuo,
			    	"Glosa"					: 	Number(value.IntPorcentajeTraspaso),
			    	"Porcentaje"			:	Number(value.IntPorcentajeTraspaso),
			    	"FolioContrato"			:	0,
			    	"IdAgrupador"			:	1
				});
				j++;
			};
		});
		
		//rescate
	 	var tablaOrigen	= [];
	 	for (var i = 1; i <= tablaDestino.length; i++) { 
	 		$.each(Origen,function(key,value){
				if (Number(value.MontoRescate)>0 || Number(value.PorcentajeRescate)>0){
					console.log(value);
					tablaOrigen.push({
						"IdInterno"		      	:	i,
					    "IdCliente" 	        :	$scope.rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado.IdCliente,
				    	"IdPortafolio"          :	$scope.rescateReinversion1N.cmbbx.oClientePortafolio.seleccionado.IdPortafolio,
				    	"IdSerieFondo"          :	value.Nemotecnico,
				    	"CalculoPorMontoCuota"  :	'C',
				    	"Monto" 				:   $scope.rescateReinversion1N.MontoTotalRescateReal,
						"NumeroCuotas" 	  	  	:	$scope.rescateReinversion1N.NumeroCuotasReal,
				    	"IdMoneda" 			  	:	value.IdMoneda,
				    	"IdRegimenTributario"	: 	value.IdRegimenFondoMutuo,
				    	"IdMedioDePago"			:	'CIN',
				    	"SolicitudBatch"		:	'N',
				    	"OrigenOperacion"		: 	'M', 
				    	"Glosa"					: 	'REINVERSION 1 A N',
				    	"IdAutorizadoOrigen"	:	0,
				    	"FolioContrato"			:	0,
				    	"IdAgrupador"			:	1,
				    	"Porcentaje"    		:   value.PorcentajeRescate
					});
				}
			});
	 	}
		
		if (tablaOrigen.length ==0){
	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("SELECCIONAR_ORIGEN"));
	 		bcsComunes.ventanaOverlay($scope.winID,false);
	 		return;
	 	}
		if (tablaDestino.length ==0){
	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("SELECCIONAR_DESTINO"));
	 		bcsComunes.ventanaOverlay($scope.winID,false);
	 		return;
	 	}
		
		var PorcentajeTotal=0;
		$.each(tablaDestino,function(key,value){
			PorcentajeTotal=Number(PorcentajeTotal)+ Number(value.Porcentaje);
		});
		
		if (Number(PorcentajeTotal) != 100){
			bcsComunes.ventanaMsg({mensaje:"TOTAL_PORCENTAJE_INCORRECTO"},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID,false);
	 		return;
	 	}
		
		var datosIngresoAporteRescate = {
			 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
			 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
			 p_IdModulo : 'FMM',
			 p_SimularOperacion : 'N',
			 p_UsaReglasNegocio : 'S',
			 p_SolicitudesRescate : tablaOrigen,
			 p_SolicitudesAporte : tablaDestino
		};
		
		bcsServicioFondosMutuos.CrearSolicitudRescateReinversion1N(datosIngresoAporteRescate).$promise.then(function(respuesta){
			if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
				var vMsg = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				bcsComunes.ventanaPromesa(vMsg).then(function() {$scope.rescateReinversion1N.btnCerrar(true);});
				bcsComunes.ventanaOverlay($scope.winID, false);
	     	}
	     	else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID, false);
	     	}
		},function(respuesta){
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
     		bcsComunes.ventanaOverlay($scope.winID, false);
		});
		
	};
	
	rescateReinversion1N.limpiar = function(){
		$scope.rescateReinversion1N.PorcentajeTotalDestino=0;
		$scope.rescateReinversion1N.formData.permiteSwitchDestino = false;
		$scope.rescateReinversion1N.formData.modelSeleccionado = null;
		$scope.rescateReinversion1N.formData.disabledMlt = false;
		$scope.rescateReinversion1N.formData.MLT = $scope.rescateReinversion1N.formData.MLT ? true : false;
		$scope.rescateReinversion1N.formData.igualRegimenDisabled = $scope.rescateReinversion1N.formData.MLT ? false : true;
		$scope.rescateReinversion1N.MontoTotalRescate = 0;
		$scope.rescateReinversion1N.MontoTotalRescateReal = 0;
		$scope.rescateReinversion1N.NumeroCuotas = 0;
		$scope.rescateReinversion1N.DolarObservadoSerieFondo = 0;		
		$scope.rescateReinversion1N.NumeroCuotasReal = 0;
		$scope.rescateReinversion1N.formData.permiteSwitch = false;
		$scope.rescateReinversion1N.formData.igualMoneda = false;
		$scope.rescateReinversion1N.formData.igualFamilia = false;
		$scope.rescateReinversion1N.formData.igualRegimen = $scope.rescateReinversion1N.formData.MLT ? true : false;
		$scope.rescateReinversion1N.cmbbx.IdEmisor = 0;
		$scope.rescateReinversion1N.cmbbx.IdRegimenFondoMutuoObj = 0;
		$scope.rescateReinversion1N.formData.idRegimeFondoMutuoActivado = false;
			
		parametros.p_IdEmpresa 	= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario 	= $scope.SesionIdUsuario;
		parametros.p_IdModulo  	= "FMM";
		parametros.p_IdEstado  	= "ACT";
		parametros.p_IdMoneda  	= "-";
		parametros.p_IdFamiliaFondo = "-";
		parametros.p_FondoExtranjero = "-";
		parametros.p_IdEmisor = "-";
		parametros.p_IdRegimenFondoMutuo = "-";
		parametros.p_IdSerieFondoOrigen = "-";
		
		dsDestinos.setCallParameters(parametros);
	};
	
	$scope.rescateReinversion1N.ajustaVentana();
});