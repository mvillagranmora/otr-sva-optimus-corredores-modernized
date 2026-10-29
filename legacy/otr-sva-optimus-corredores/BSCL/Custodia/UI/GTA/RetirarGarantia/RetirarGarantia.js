angular.module('ModuloCustodia').controller('RetirarGarantiaCtrl',
		function($scope,$translate,$timeout,$resource,bcsComunes,bcsServiciosGarantia,
				 bcsServicioPlataforma,bcsOperacionesComunes)
{	
	$scope.data = {};
	$scope.data.SimboloMonedaLocal = $scope.SesionIdMonedaSistema;
	$scope.data.DecimalesMonedaLocal = (util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
	$scope.data.kngTotalMontoARetirar = 0;
	var tablaCorteMinimoInvalido = [];
	var corteMinimoValido = true;
	$scope.btnActivo = true;

	$scope.ventanaId = bcsComunes.ventanaWinId($scope); // bcsComunes.ventanaOverlay($scope.ventanaId,true);
    var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	//SETEO INICIAL
	$scope.data.kngFechaSistema = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.data.oConfiguracionConceptoGarantia={
		tipo : "combobox-conceptos-garantia",
		configuracion : {},
		filtros : {
				IdEstado			: { "field"  : "IdEstado","operator": "eq", "value"   : "VIG" },
				ConfiguradoEnModulo	: { "field"  : "ConfiguradoEnModulo","operator": "eq", "value"   : "S" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
		

	$scope.data.oConfiguracionEntidadDestinoGarantia={
			tipo : "combobox-entidad-destino-garantia",
			configuracion : {},
			filtros : {
				IdEstadoEntidadDestinoGarantia : { "field"   : "IdEstadoEntidadDestinoGarantia","operator": "eq", "value"   : "ACT" }				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	
	
	//RECUPERAR DATA
	var TotalMontoGarantizado = 0;
	var dataCustodia	= [];
	var dataCtaInv		= [];
	var dataEspecie		= [];

	RecuperarData = function(){	
		for (i=0;i<Object.keys(parametros).length;i++){
			if(i==0) { 
				$scope.data.oConfiguracionConceptoGarantia.idDefecto = parametros[i].IdConcepto
				$scope.data.oConfiguracionConceptoGarantia.idActual = parametros[i].IdConcepto
				
				if (parametros[i].IdEntidadDestinoGarantiaEmpresa === 0){
					$scope.data.oConfiguracionEntidadDestinoGarantia.idDefecto = undefined
					$scope.data.oConfiguracionEntidadDestinoGarantia.idActual = undefined
				} else {
					$scope.data.oConfiguracionEntidadDestinoGarantia.idDefecto = parametros[i].IdEntidadDestinoGarantiaEmpresa
					$scope.data.oConfiguracionEntidadDestinoGarantia.idActual = parametros[i].IdEntidadDestinoGarantiaEmpresa	
				}
			}
			switch (parametros[i].IdOrigenGarantia)
			{
				case "CUS":
					dataCustodia.push(parametros[i]);
					break;
				case "CTA":
					dataCtaInv.push(parametros[i]);
					break;
				case "ESP":
					dataEspecie.push(parametros[i]);
					TotalMontoGarantizado 	 	= (TotalMontoGarantizado || 0) + Number(parametros[i].INGMonto);
					break;
			};

		}
		$scope.data.kngTotalMontoGarantizado = TotalMontoGarantizado;
	};
	RecuperarData();

	
	//LLAMADA A SERVICIOS SP
//	ObtenerParidadTipoCambio = function(param){
//		var urlObtenerParidadTipoCambio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=201";
//		var r = $resource("", {},{'ObtenerParidadTipoCambio': { method: "POST", url: urlObtenerParidadTipoCambio}});
//		var post = r.ObtenerParidadTipoCambio(param);
//		return post.$promise;
//	};

	//CALCULO DE MONTO A RETIRAR	
	var TotalMontoARetirar = 0;
	var calculoMontoARetirar 	= function(grid, values, model){		
		var MontoARetirar      		= 0;
		var MontoARetirarMonSist	= 0;
		var IdMonedaOrigen      	= '';
		var data = Object();

		model.CantidadARetirar 		= values.CantidadARetirar;
		MontoAGarantizarOriginal 	= Number(model.MontoARetirarMonSist) || 0;
		TotalARetirarOriginal 		= $scope.data.kngTotalMontoGarantizado || 0;
		//TotalMontoARetirar = $scope.data.kngTotalMontoGarantizado || 0;
		TotalMontoARetirar = TotalMontoARetirar - MontoAGarantizarOriginal;

		switch (grid) 
		{
			case "CUS":
				IdMonedaOrigen        = (model.IdClasificacionTipoInstrumento === 'RF' || model.IdClasificacionTipoInstrumento === 'IF') ? model.IdTipoReajuste: model.IdMonedaInstrumento;
				if (values.CantidadARetirar < 1)
				{	
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CANTIDAD_RETIRAR_INVALIDA"));
					model.CantidadARetirar = 0;
					return;
				}
				if (values.MontoARetirarMonSist < 1)
				{	
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CANTIDAD_RETIRAR_INVALIDO"));
					model.CantidadARetirar = 0;					
					return;
				}				
				if ((values.CantidadARetirar > model.INGCantidad && model.IdClasificacionTipoInstrumento !== 'IF') || (values.CantidadARetirar > model.INGValorRescate && model.IdClasificacionTipoInstrumento === 'IF'))
				{	
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_CANTIDAD"));
					values.CantidadARetirar = 0;
					model.CantidadARetirar = 0;
					return;
				}
				if (values.MontoARetirarMonSist > model.MontoGarantia)
				{	
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_MONTO"));
					values.MontoARetirarMonSist = 0;
					model.CantidadARetirar = 0;
					return;
				}
				if (values.MontoARetirarMonSist){
					var cant = new Big((Number(values.MontoARetirarMonSist) * Number(model.INGCantidad)) / Number(model.MontoGarantia));
					model.CantidadARetirar = Math.trunc(Number(cant.toString()));
					values.CantidadARetirar = model.CantidadARetirar;
				}

				var disp = model.INGCantidad;

				var existeCorteMinimoInvalido = false;
				$.each(tablaCorteMinimoInvalido,function(key,value){			
					if(model.IdGarantia == value.IdGarantia){
						existeCorteMinimoInvalido = true;
						return false;
					}
					else{
						existeCorteMinimoInvalido = false;
					}
				});
				$scope.data.validaCorteMinimo(model,values,existeCorteMinimoInvalido);

				if (model.IdClasificacionTipoInstrumento === "IF"){
					disp = model.INGValorRescate;
				}
									

				if (model.MontoGarantiaMonedaInstrumento > 0)
				{
					MontoARetirar = values.CantidadARetirar?((Number(model.MontoGarantiaMonedaInstrumento) / disp) * values.CantidadARetirar):values.MontoARetirarMonSist;
				} 
				else 
				{
					MontoARetirar = values.CantidadARetirar?(Number(model.PrecioRetiro) * values.CantidadARetirar):values.MontoARetirarMonSistr;
				}


				model.MontoARetirarMonSist = MontoARetirar;


				data.p_IdEmpresa =  $scope.SesionIdEmpresa;
				data.p_IdTrabajo =  1367;
				data.p_CalculosEntrada = [{
					"IdTrabajo"			: data.p_IdTrabajo
					,"IdInterno"		: 1
					,"Fecha"			: util.dateSPdate($scope.data.kngFechaSistema)
					,"IdMonedaOrigen" 	: model.IdMonedaInstrumento
					,"IdMonedaDestino" 	: $scope.SesionIdMonedaSistema
					,"ValorCambioLocalModificado" : 0
					,"MontoOrigen"		: MontoARetirar || 0
					,"IdUso"			: 'CUS'
					//,"TipoCambio" 		: 0
					//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
					//,"OrigenTipoCambio" : 'OBSERVADO'
				}];
				bcsServiciosGarantia.ObtenerParidadTipoCambio(data,function(value){
					if (value.p_out_CalculosSalida.length > 0){

						var montoLoc = value.p_out_CalculosSalida[0].MontoLocal;
						var montoDes = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
						var montoOri = value.p_out_CalculosSalida[0].MontoOrigen;

						//if (Number(montoOri) === Number(montoLoc)){
						if (value.p_out_CalculosSalida[0].IdMonedaOrigen === value.p_out_CalculosSalida[0].IdMonedaDestino){
							//model.MontoAGarantizar = montoLoc;
							MontoARetirar = montoLoc;
						}
						else {					
							//model.MontoAGarantizar = montoDes;
							MontoARetirar = montoDes;
						}

						if (model.IdClasificacionTipoInstrumento == 'IF') 
						{
							model.MontoARetirarMonSist = (values.CantidadARetirar * model.MontoGarantia)/model.INGValorRescate;
						}
						else
						{
							model.MontoARetirarMonSist = MontoARetirar;
						}
						//model.MontoARetirar = MontoARetirar;
						model.MontoARetirar = montoOri;
						grillaCustodia.refresh();
						TotalMontoARetirar = TotalMontoARetirar + Number(MontoARetirar);

					}
					$scope.data.kngTotalMontoARetirar = TotalMontoARetirar;
				});



				break;

			case "CTA":
			
				if (values.MontoARetirar > 0){
					if (values.MontoARetirar <= model.INGCantidad){
						data.p_IdEmpresa =  $scope.SesionIdEmpresa;
						data.p_IdTrabajo =  1366;
						data.p_CalculosEntrada = [{
							"IdTrabajo"			: data.p_IdTrabajo
							,"IdInterno"		: 1
							,"Fecha"			: $scope.SesionFechaSistema  //util.dateSPdate($scope.SesionFechaSistema)
							,"IdMonedaOrigen" 	: model.IdMonedaInstrumento
							,"IdMonedaDestino" 	: $scope.SesionIdMonedaSistema
							,"ValorCambioLocalModificado" : 0
							,"MontoOrigen"		: values.MontoARetirar || 0
							,"IdUso"			: 'CUS'
							//,"TipoCambio" 		: 0
							//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
							//,"OrigenTipoCambio" : 'OBSERVADO'
						}];
						bcsServiciosGarantia.ObtenerParidadTipoCambio(data,function(value){
							if (value.p_out_CalculosSalida.length > 0){
								
								var montoLoc = value.p_out_CalculosSalida[0].MontoLocal;
								var montoDes = value.p_out_CalculosSalida[0].MontoDestino <= 0? value.p_out_CalculosSalida[0].MontoOrigen:value.p_out_CalculosSalida[0].MontoDestino;
								var montoOri = value.p_out_CalculosSalida[0].MontoOrigen;
								
								//if (Number(montoOri) === Number(montoLoc)){
								if (value.p_out_CalculosSalida[0].IdMonedaOrigen === value.p_out_CalculosSalida[0].IdMonedaDestino){
									//model.MontoAGarantizar = montoLoc;
									model.MontoARetirarMonSist = Number(montoLoc);
								}
								else {					
									//model.MontoAGarantizar = montoDes;
									model.MontoARetirarMonSist = Number(montoDes);
								}
								bcsComunes.ventanaOverlay($scope.ventanaId,true);
								TotalMontoARetirar = TotalMontoARetirar + Number(model.MontoARetirarMonSist);			
								$scope.data.kngTotalMontoARetirar = TotalMontoARetirar;
								bcsComunes.ventanaOverlay($scope.ventanaId,false);
								grillaCtaInv.refresh();	
							}
						});
					} else  {
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_MONTO"));
						values.MontoARetirar = 0;
						values.MontoARetirarMonSist = 0;
						grillaCtaInv.refresh();
						$scope.data.kngTotalMontoGarantizado = TotalARetirarOriginal || 0;
						return;
						
					} //
	
				}
			
			break;
			case "ESP":{
				//Para especies (ESP) se retira el total del monto
				TotalMontoARetirar = TotalMontoARetirar + Number(model.MontoValorizado);
				$scope.data.kngTotalMontoGarantizado = TotalMontoARetirar;
				break;
			}	
		}

		grillaCustodia.refresh();
		grillaCtaInv.refresh();
		grillaEspecie.refresh();

		//$scope.data.kngTotalMontoGarantizado = TotalMontoARetirar;
		return true;
	};
	
	
	//GRILLAS
	var grillaCustodia = null;
	var dsGrillaGtaCustodia = new bcs.DataSource({
		type : "array",
		definition : {
			data : dataCustodia,
			serverSorting 		: true,
			schema : {
				model : {
					fields : {
						IdGarantia								: 	{type: "number"},
						FolioGarantia 							:	{type: "number"},
						NombreInstrumento						:	{type: "string"},
						Nemotecnico								:	{type: "string"},
						CantidadARetirar						:	{type: "number"},
						ValorRescatedARetirar					:	{type: "number"},
						MontoARetirar 							: 	{type: "number"},
						MontoARetirarIF							: 	{type: "number"},
						MontoARetirarMonSist 					: 	{type: "number"},							
						INGCantidad								:	{type: "number"},
						INGValorRescate							:	{type: "number"},
						MontoGarantia							:	{type: "number"},
						MontoGarantiaMonedaInstrumento			:	{type: "number"},
						NombreMoneda							:	{type: "string"},
						FechaIngresoRegistro					: 	{type: "sysdate"},
						NombreCliente 							:	{type: "string"},
						DocumentoIdentidad 						:	{type: "string"},
						CodigoPortafolio 						:	{type: "string"},
						IdMonedaInstrumento     				:   {type: "string"},
						INGPrecioGarantia						: 	{type: "number"},						
						NombreConcepto             				:   {type: "string"},
						ISIN									: 	{type: "string"},
						MontoInteresCorrido						: 	{type: "number"},
						MontoReajusteCorrido					: 	{type: "number"},
						FolioCarteraGarantizada					:	{type: "number"},
						CorteMinimo								:	{type: "number"},
						ValorNominal							:	{type: "number"},
						IdTipoInstrumento						: 	{type: "string"},
						IdMonedaInstrumento						: 	{type: "string"},
						IdTipoReajuste 							:   {type: "string"},
						IdClasificacionTipoInstrumento	 		: 	{type: "string"},
						EsFungible 								: 	{type: "string"},
						PrecioRetiro							:	{type: "number"},
						TasaRetiro								:	{type: "number"},
						ValorClasificacionRiesgo 				:	{type: "string"},
						RazonSocialContraparte  				:	{type: "string"},
						IdContraparteGarantia 					: 	{type: "number"},
						IdConcepto 								: 	{type: "string"},
						ConCertificadoAutomatico 				: 	{type: "string"}
					}
				}
			}
		}
	});
	$scope.data.confBasicaCustodiaRetirar = {
		dataSource			: dsGrillaGtaCustodia,
		columns				:  [ {field: "FechaIngresoRegistro", 				title: "UI_FECHA_MOVIMIENTO",  				width: "80px"}
								,{field: "RazonSocialContraparte", 				title: "UI_ENTIDAD_DESTINO", 				width: "150px",	groupable: true}
		       				   	,{field: "NombreConcepto", 						title: "UI_CONCEPTO", 						width: "110px", groupable: true,	attributes:{style:"white-space:nowrap;"}}
		       				    ,{field: "IdClasificacionTipoInstrumento", 		title: "UI_MERCADO", 						width: "60px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
								,{field: "Nemotecnico", 						title: "UI_NEMO", 							width: "80px", 	groupable: true,	attributes:{style:"white-space:nowrap;"}}
								,{field: "ValorClasificacionRiesgo", 			title: "UI_CLASIFICACION_RIESGO", 			width: "50px",	groupable: true}
								,{field: "ISIN", 								title: 'UI_ISIN', 							width: "70px", 	groupable: true,	hidden: true,		attributes:{style:"white-space:nowrap;"}}
								,{field: "EsFungible", 							title: "UI_FUNGIBLE", 						width: "50px"}																
								,{field: "CorteMinimo", 						title: "UI_CORTE_MINIMO", 					width: "65px",	template : " <label> #:  (util.bcs_formato(CorteMinimo||0,'GEN','Cantidad',(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaInstrumento),'','')).valor #</label>"}
								,{field: "INGCantidad", 						title: "UI_CANTIDAD", 						width: "90px",	template : " <label> #:  (util.bcs_formato(INGCantidad||0,'GEN','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}
								,{field: "INGValorRescate", 					title: "UI_VALOR_RESCATE", 					width: "90px",	template : " <label> #:  (util.bcs_formato(INGValorRescate||0,'GEN','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}

								,{field: "PrecioRetiro", 						title: "UI_PRECIO_MERCADO", 				width: "100px", template : " <label> #:  (util.bcs_formato(PrecioRetiro,'GEN','Precio',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								,{field: "TasaRetiro", 							title: "UI_TASA_MERCADO", 					width: "100px", template : " <label> #:  (util.bcs_formato(TasaRetiro,'GEN','Tasa',(IdMonedaInstrumento!==''?IdMonedaInstrumento:IdTipoReajuste),IdTipoInstrumento,'')).valor #</label>"}
								
								//,{field: "ValorNominal", 						title: "UI_VALOR_NOMINAL", 					width: "90px",	hidden: true,		template : " <label> #:  (util.bcs_formato(ValorNominal||0,'GEN','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}								

								,{field: "IdMonedaInstrumento", 				title: "UI_MONEDA", 						width: "55px"}
								,{field: "MontoGarantia", 						title: "UI_MONTO", 							width: "90px",	template : " <label> #:  (util.bcs_formato(MontoGarantia,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>",

									/*format : function(value, dataItem) {
											return util.bcs_formato(value,'GEN','Monto',$scope.SesionIdMonedaSistema,dataItem.IdTipoInstrumento,'');
										}*/}								
								,{field: "MontoGarantiaMonedaInstrumento", 		title: "UI_VALOR", 							width: "90px",	template : " <label> #:  (util.bcs_formato(MontoGarantiaMonedaInstrumento,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
								
								
								,{field: "CantidadARetirar", 					title: "UI_CANTIDAD_A_RETIRAR", 			width: "100px",	spinners:false, editable: true,	template : " <label> #:  (util.bcs_formato(CantidadARetirar||0,'GEN','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}
								,{field: "MontoARetirarMonSist", 				title: "UI_MONTO_A_RETIRAR_MONEDA_SISTEMA",	width: "100px",	spinners:false, editable: true, template : " <label> #:  (util.bcs_formato(MontoARetirarMonSist||0,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}
								
								,{field: "MontoARetirar", 						title: "UI_VALOR_A_RETIRAR", 				width: "100px",	spinners:false,	template : " <label> #:  (util.bcs_formato(MontoARetirar||0,'GEN','Monto',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}

								,{field: "MontoInteresCorrido",					title: 'UI_INTERESES_CORRIDOS', 			width: "105px",	hidden: true,	template : " <label> #:  (util.bcs_formato(MontoInteresCorrido||0,'GEN','Monto',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}
								,{field: "MontoReajusteCorrido", 				title: 'UI_REAJUSTES_VAC_CORRIDO', 			width: "105px",	hidden: true,	template : " <label> #:  (util.bcs_formato(MontoReajusteCorrido||0,'GEN','Monto',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}

								,{field: "NombreCliente", 						title: "UI_CLIENTE", 						width: "100px",	attributes:{style:"white-space:nowrap;"}}
								,{field: "DocumentoIdentidad", 					title: "UI_DOCUMENTO_IDENTIDAD", 			width: "90px",	attributes:{style:"white-space:nowrap;"}}
								,{field: "CodigoPortafolio", 					title: "UI_PORTAFOLIO", 					width: "50px",	attributes:{style:"white-space:nowrap;"}}
								,{field: "RazonSocialContraparte", 				title: "UI_ENTIDAD_DESTINO", 				width: "100px",	attributes:{style:"white-space:nowrap;"}}
		       				   	,{field: "FolioCarteraGarantizada", 			title: "UI_FOLIO_OPERACION_RELACIONADA",	width: "80px",	format: "{0:n0}"}
								,{field: "FolioGarantia", 						title: "UI_SECUENCIA_GARANTIA",				width: "70px",	format: "{0:n0}"}
								
								,{field: "ValorRescatedARetirar", 				title: "UI_VALOR_RESCATE",					width: "70px",	hidden: true}
							   ],
		sortable			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: false,
		autoBind			: true,
		filterable			: true,
		height				: 480, 
		selectable 			: "row",
		navigatable			: true,
		save				: function(e) 	{ calculoMontoARetirar('CUS',e.values, e.model); },
		gridCreated 		: function(grid) { 
									grillaCustodia = grid;
									if ( $scope.SesionLocale === 'PE' ){
										grid.showColumn("ISIN");
									}
									grillaCustodia.refresh();
							  },		
		notEditableColumns : function(fila) {
								if ( fila.IdClasificacionTipoInstrumento !=='RV' ) {
									return ['MontoARetirarMonSist'];
								}
	                          }	
	};

	
	
	var grillaCtaInv  = null;
	var dsGrillaCtaInv = new bcs.DataSource({
		type : "array",
		definition : {
			data : dataCtaInv,
			schema : {
				model : {
					fields : {
						IdGarantia				: {type: "number"},
						FolioGarantia 			: {type: "number"},						
						IdMonedaInstrumento		: {type: "string"},
						IdConcepto 				: {type: "string"},
						MontoARetirar 			: {type: "number"},
						MontoARetirarMonSist 	: {type: "number"},
						FechaMovimiento			: {type: "sysdate"},
						NombreCliente 			: {type: "string"},
						DocumentoIdentidad 		: {type: "string"},
						CodigoPortafolio 		: {type: "string"},
						MontoValorizado	 		: {type: "number"},    //Para cta. tiene el monto de la garantía
						IdMonedaInstrumento		: {type: "string"},
						RazonSocialContraparte  : {type: "string"},
						NombreConcepto 			: {type: "string"},
						IdContraparteGarantia 	: {type: "number"},
						ConCertificadoAutomatico: {type: "string"},
						INGCantidad 			: {type: "number"}
					}
				}
			}
		}
	});
	$scope.data.confBasicaCuentaInversionRetirar = {
		dataSource			: dsGrillaCtaInv,
		columns				: [  {field: "IdGarantia", 				title: "UI_GARANTIA_ABR", 					width: "70px", align: "right", sortable : false }
								,{field: "RazonSocialContraparte", 	title: "UI_ENTIDAD_DESTINO", 				width: "150px", groupable: true	}
								,{field: "NombreConcepto", 			title: "UI_CONCEPTO", 						width: "110px", attributes:{style:"white-space:nowrap;"}, groupable: true}
								,{field: "NombreCliente", 			title: "UI_CLIENTE", 						width: "180px", align: "left",  sortable : true, attributes:{style:"white-space:nowrap;"}, groupable: true }
								,{field: "CodigoPortafolio", 		title: "UI_PORTAFOLIO", 					width: "50px", 	align: "right", sortable : true, groupable: true }
								,{field: "DocumentoIdentidad", 		title: "UI_DOCUMENTO_IDENTIDAD", 			width: "90px",  align: "right", sortable : true, groupable: true }
								,{field: "IdMonedaInstrumento", 	title: "UI_MONEDA", 						width: "60px",  align: "left", sortable : false, groupable: true}
								,{field: "INGCantidad", 			title: "UI_VALOR",							width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
								,{field: "MontoValorizado", 		title: "UI_MONTO_REFERENCIAL",				width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoValorizado,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoARetirar", 			title: "UI_VALOR_A_RETIRAR", 				width: "100px", align: "right", sortable : false, editable: true, template : " <label> #:  (util.bcs_formato(MontoARetirar,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>" }
								,{field: "MontoARetirarMonSist",	title: "UI_MONTO_A_RETIRAR", 				width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoARetirarMonSist,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
								,{field: "FechaMovimiento", 		title: "UI_FECHA_INGRESO", 					width: "90px" , align: "center", sortable : false, groupable: true }
							  ],
		sortable			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: false,
		autoBind			: true,
		filterable			: true,
		height				: 480,
		selectable 			: "row",
		editable			: true,
		save				: function(e) 	 { calculoMontoARetirar('CTA',e.values, e.model);},
		gridCreated 		: function(grid) { grillaCtaInv = grid; },
	};
	
	
	var grillaEspecie = null;
	var dsGrillaEspecie = new bcs.DataSource({
		type : "array",
		definition : {
			data : dataEspecie,
			schema : {
				model : {
					fields : {
						IdGarantia				: { type: "number"},
						FolioGarantia 			: { type: "number"},
						RazonSocialContraparte 	: { type: "string"},
						NombreConcepto 		 	: { type: "string"},
						DescripcionEspecie		: { type: "string"},
						INGCantidad 			: { type: "number"},
						MontoValorizado			: { type: "number"},  //para especies tiene el monto de la garantía
						FechaMovimiento			: { type: "sysdate"},
						Glosa					: { type: "string"},
						NombreCliente 			: { type: "string"},
						DocumentoIdentidad 		: { type: "string"},
						CodigoPortafolio 		: { type: "string"},
						IdMonedaInstrumento 	: { type: "string"},
						RazonSocialContraparte  : { type: "string"},
						NombreConcepto 			: { type: "string"},
						IdContraparteGarantia 	: { type: "number"},
						IdConcepto 			 	: { type: "string"},
						ConCertificadoAutomatico: { type: "string"}
					}
				}
			}
		}
	});
	
	//Para especies se retira la totalidad del monto 
	$scope.data.confBasicaEspecieRetirar = {
		dataSource			: dsGrillaEspecie,
		columns				: [  {field: "IdGarantia", 			title: "UI_GARANTIA_ABR", width: "70px", align: "right", sortable : false }
								,{field: "RazonSocialContraparte", 	title: "UI_ENTIDAD_DESTINO", width: "150px"	}
								,{field: "NombreConcepto", 			title: "UI_CONCEPTO", width: "110px", attributes:{style:"white-space:nowrap;"}}
								,{field: "DocumentoIdentidad", 	title: "UI_DOCUMENTO_IDENTIDAD", width: "100px", align: "right", sortable : true }
								,{field: "CodigoPortafolio", 	title: "UI_PORTAFOLIO", width: "50px", align: "right", sortable : true }
								,{field: "NombreCliente", 		title: "UI_CLIENTE", width: "150px", align: "left", sortable : true , attributes:{style:"white-space:nowrap;"}}
								,{field: "DescripcionEspecie", 	title: "UI_ESPECIE", width: "150px", align: "left", sortable : false , attributes:{style:"white-space:nowrap;"}}
								,{field: "INGCantidad", 		title: "UI_CANTIDAD", hidden:true, width: "60px", align: "left", sortable : false }
								,{field: "IdMonedaInstrumento", title: "UI_MONEDA", width: "50px", align: "center", sortable : true }																
								,{field: "MontoValorizado", 	title: "UI_MONTO_GARANTIA", width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoValorizado,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>" }
								,{field: "FechaMovimiento", 	title: "UI_FECHA_INGRESO", width: "85px", align: "center", sortable : false }
								,{field: "Glosa", 				title: "UI_DESCRIPCION", align: "left", sortable : false }
							  ],
		sortable			: true,
		pageable			: false,
		autoBind			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		filterable			: true,
		editable			: true,
		height				: 480,
		selectable 			: "row",
		save				: function(e) 	 { calculoMontoARetirar('ESP',e.values, e.model);},
		gridCreated 		: function(grid) { grillaEspecie = grid; },
	};

	//BOTONES 
	$scope.data.cancelar = function()
	{					
		bcsComunes.ventanaData(idVentana,{resultado: true});
		bcsComunes.ventanaCerrar(idVentana); 
	};
	
	
	//HABILITAR RETIRO	
	$scope.$watchGroup(['data.oConfiguracionEntidadDestinoGarantia.idActual','data.oConfiguracionConceptoGarantia.idActual'],function(newval,oldval){
		if ($scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte != undefined &&
			$scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto != undefined){
			
			$scope.data.accionDisabled = true;
		} else {
			$scope.data.accionDisabled = false;
		}	
	});
	
	
	//LLAMAR SERVICIO DE RETIRO GARANTIA
	var success = function(respuesta)
	{
		//si vuelve ref id, se creo la asignacion
		if (respuesta.P_OUT_ERRORESCONTADOR == 0) 
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.ventanaId);
			
			$scope.data.cancelar();
		}
		else
		{
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.ventanaId);
		}	
		bcsComunes.ventanaOverlay($scope.ventanaId,false);//overlay.encendido = false;
		$scope.btnActivo = true;	
	};
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.ventanaId);		
					
		bcsComunes.ventanaOverlay($scope.ventanaId,false);//overlay.encendido = false;
		$scope.btnActivo = true;
	};
	
	$scope.data.validaCorteMinimo = function(model,values,existe){
		
		if ( model.IdClasificacionTipoInstrumento === "IF" || model.IdClasificacionTipoInstrumento === "RF")	{
			var cantidad = new Big(values.CantidadARetirar);
			var corte = new Big(model.CorteMinimo);
			var nominal = cantidad.mod(corte);
			var rescate = new Big(values.CantidadARetirar);
			nominal = rescate.mod(corte);
			var obj = {"IdGarantia" : model.IdGarantia}
			if (nominal > 0){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("CORTE_MINIMO_INVALIDO"));
				values.MontoARetirarMonSist = 0;
				if(!existe){
					tablaCorteMinimoInvalido.push(obj);
				}
			}
			else{
				if(existe){
					tablaCorteMinimoInvalido = tablaCorteMinimoInvalido.filter(function (item) {
						return item.IdGarantia !== model.IdGarantia;
					});
				}
			}
		}	
	}
	$scope.data.obtieneCorteMinimoInvalido = function(idGarantia){
		if (tablaCorteMinimoInvalido.length > 0) {
			$.each(tablaCorteMinimoInvalido,function(key,value){			
				if(idGarantia == value.IdGarantia){
					corteMinimoValido = false;
					return false;
				}
				else{
					corteMinimoValido = true;
				}
			});
		}
		else{
			corteMinimoValido = true;
		}
	}

	$scope.recalculaCantidad = function(cantidadARetirar,corteMinimo){
		return Number(cantidadARetirar) / Number(corteMinimo);
	}
	$scope.validaInstrumentoFungible = function(esFungible,idClasificacionTipoInstrumento){
		if('N' === esFungible && "IF" === idClasificacionTipoInstrumento){
			return false;
		}
		else{
			return true;
		}
	}

	$scope.data.retirarGarantia = function(param)
    {
		bcsComunes.ventanaOverlay($scope.ventanaId,true);//overlay.encendido = true;
		$scope.btnActivo = false;	
		var tablaRetiroGarantia = [];

		var RegistrosCus = dsGrillaGtaCustodia.dataColumns(["IdGarantia","CantidadARetirar","ValorRescatedARetirar","MontoARetirar","PrecioRetiro","TasaRetiro","IdConcepto","ConCertificadoAutomatico","EsFungible","CorteMinimo","IdClasificacionTipoInstrumento"],dsGrillaGtaCustodia.itemsEdited()) || [];
		$.each(RegistrosCus,function(key,value){
			$scope.data.obtieneCorteMinimoInvalido(value.IdGarantia);
			var esFungible = $scope.validaInstrumentoFungible(value.EsFungible,value.IdClasificacionTipoInstrumento);
			
			if ((value.MontoARetirar > 0 || value.CantidadARetirar > 0) && corteMinimoValido){
				if(!esFungible){
					var cantidadSegunCorteMinimo = $scope.recalculaCantidad(value.CantidadARetirar,value.CorteMinimo);
				}
				
				var Obj = {
					"IdGarantia"				: Number(value.IdGarantia) || 0,
					"Cantidad"					: esFungible ? (Number(value.CantidadARetirar) || 0) : cantidadSegunCorteMinimo,
					"ValorRescate"				: Number(value.CantidadARetirar) || 0,
					"MontoMonedaInstrumento"	: Number(value.MontoARetirar) || 0,
					"PrecioRetiro"				: Number(value.PrecioRetiro) || 0,
					"TasaRetiro"				: Number(value.TasaRetiro) || 0,
					"IdConcepto" 				: value.IdConcepto || '',
					"ConCertificadoAutomatico"	: value.ConCertificadoAutomatico || '',
					"IdClasificacionTipoInstrumento" : value.IdClasificacionTipoInstrumento
				};
				tablaRetiroGarantia.push(Obj);
			}
		});
		
		
		var RegistrosCta = dsGrillaCtaInv.dataColumns(["IdGarantia","MontoARetirar","MontoARetirarMonSist","INGCantidad","IdConcepto","ConCertificadoAutomatico"],dsGrillaCtaInv.itemsEdited()) || [];		
		$.each(RegistrosCta,function(key,value){
			if (value.MontoARetirar > 0){
				
				if (value.MontoARetirar <= value.INGCantidad){
					var Obj = {
							"IdGarantia"				: Number(value.IdGarantia) || 0,
							"Cantidad"					: Number(value.MontoARetirar) || 0,
							"MontoMonedaInstrumento"	: Number(value.MontoARetirar) || 0,
							"MontoMonedaSistema"		: Number(value.MontoARetirarMonSist) || 0,
							"IdConcepto" 				: value.IdConcepto || '',
							"ConCertificadoAutomatico"	: value.ConCertificadoAutomatico || ''
						};
						tablaRetiroGarantia.push(Obj);					
				} else {
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_MONTO"));
					return;
				}
				

			}
		});
		
		
		//Para especies se retira la totalidad(no existe retiro parcial)
		$.each(dsGrillaEspecie.data(),function(key,value){			
			var Obj = {
				"IdGarantia"				: Number(value.IdGarantia) || 0,
				"Cantidad"					: 1,   //Para especies se retira el monto total
				"MontoMonedaSistema"		: Number(value.MontoGarantia) || 0,
				"IdConcepto" 				: value.IdConcepto || '',
				"ConCertificadoAutomatico"	: value.ConCertificadoAutomatico || ''
			};
			tablaRetiroGarantia.push(Obj);
		});

		
		
		if (tablaRetiroGarantia.length > 0) {
			var datos = {};
			datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               = $scope.SesionIdUsuario; 
			datos.p_IdModulo                = 'CUS';
			datos.p_IdEmpleado 				= $scope.SesionIdEmpleado;
			datos.p_UsaReglasNegocio		= 'S';
			datos.p_SimulaOperacion			= 'N';
			datos.p_FechaMovimiento			= util.dateSPdate($scope.data.kngFechaSistema); 
			datos.p_IdTipoOperacion			= 'RETGTA';
			datos.p_tablaRetiro				= tablaRetiroGarantia;

	       					
	       	bcsServiciosGarantia.RetirarGarantia(
	       		datos,
	       		function(respuesta){	  		
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);

					if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
						$scope.data.cancelar();
					}
					
					bcsComunes.ventanaOverlay($scope.ventanaId, false);
					$scope.btnActivo = true;
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_RETIRO_GARANTIA"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
					bcsComunes.ventanaOverlay($scope.ventanaId, false);
					$scope.btnActivo = true;
				}
	       	); 

		} 
		else 
		{
			bcsComunes.ventanaOverlay($scope.ventanaId,false);
			$scope.btnActivo = true;
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_NO_HAY_DATOS_PARA_GUARDAR")
			},idVentana);
		}


    };

    
    
});

