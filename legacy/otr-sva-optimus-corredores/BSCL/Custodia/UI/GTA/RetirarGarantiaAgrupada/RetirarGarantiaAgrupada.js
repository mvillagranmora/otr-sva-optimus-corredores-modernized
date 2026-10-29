angular.module('ModuloCustodia').controller('RetirarGarantiaAgrupadaCtrl',
		function($scope,$translate,$timeout,$resource,bcsComunes,bcsServiciosGarantia,
				 bcsServicioPlataforma,bcsOperacionesComunes)
{	
	$scope.data = {};
	$scope.data.SimboloMonedaLocal = $scope.SesionIdMonedaSistema;
	$scope.data.DecimalesMonedaLocal = (util.bcs_formato(0,'GEN','Monto',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales;
	$scope.data.kngTotalMontoARetirar = 0;
	$scope.btnActivo = true;

	$scope.ventanaId = bcsComunes.ventanaWinId($scope); // bcsComunes.ventanaOverlay($scope.ventanaId,true);
    var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var objGTA = bcsComunes.ventanaParams(idVentana);

	var parametros = objGTA.Registros


	//SETEO INICIAL
	$scope.data.FechaConsulta = objGTA.FechaConsulta
	
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
			switch (parametros[i].NombreOrigenGarantia)
			{
				case "CUSTODIA":
					dataCustodia.push(parametros[i]);
					break;
				case "CUENTA INVERSION":
					dataCtaInv.push(parametros[i]);
					break;
			};

		}
		$scope.data.kngTotalMontoGarantizado = TotalMontoGarantizado;
	};
	RecuperarData();

	


	//CALCULO DE MONTO A RETIRAR	
	var TotalMontoARetirar = 0;

	var calculoMontoARetirar2 	= function(grid, values, model){
		var dataEntrada = {};

		model.CantidadARetirar 		= values.CantidadARetirar;
		MontoAGarantizarOriginal 	= Number(model.MontoARetirarMonSist) || 0;
		TotalARetirarOriginal 		= $scope.data.kngTotalMontoGarantizado || 0;
		
		TotalMontoARetirar = TotalMontoARetirar - MontoAGarantizarOriginal;

		dataEntrada.p_IdEmpresa =  $scope.SesionIdEmpresa;
		dataEntrada.p_IdUsuario = $scope.SesionIdUsuario; 
		dataEntrada.p_IdTrabajo =  1367;
		dataEntrada.p_IdModulo = "CUS";
		dataEntrada.p_UsaReglasNegocio = "N";
		dataEntrada.p_EsSimulacion = "S"
		dataEntrada.p_IdEmpleado = $scope.SesionIdEmpleado
		dataEntrada.p_Garantias = [];


		var garantia = {}

		switch (grid) {
			case "CUS" :
					garantia.p_Cliente = model.IdCliente;
					garantia.p_Portafolio = model.IdPortafolio;
					garantia.p_FechaDeLiquidacion = objGTA.FechaConsulta ;
					garantia.p_IdClasificacionIntrumento = model.IdClasificacionTipoInstrumento;
					garantia.p_IdContraparteGarantia = model.IdContraparteGarantia ;
					garantia.p_IdMonedaInstrumento = model.IdMonedaInstrumento;
					garantia.p_IdConcepto = model.IdConcepto;
					garantia.p_IdInstrumento = model.IdInstrumento;
					garantia.p_IdOrigenGarantia = model.IdOrigenGarantia;
					garantia.p_Nemotecnico = model.Nemotecnico;
					garantia.CantidadAgrupada = (model.IdClasificacionTipoInstrumento == "IF") ? model.CantidadAgrupada : model.INGCantidadAgrupada;
					garantia.CantidadARetirar = (values.CantidadARetirar) ? values.CantidadARetirar : 0;
					garantia.MontoARetirar = (values.MontoARetirarMonSist) ? values.MontoARetirarMonSist : 0;

					dataEntrada.p_Garantias.push(garantia)
					break;

			case "CTA": 
					garantia.p_Cliente = model.IdCliente;
					garantia.p_Portafolio = model.IdPortafolio;
					garantia.p_FechaDeLiquidacion = objGTA.FechaConsulta;
					garantia.p_IdClasificacionIntrumento = model.IdClasificacionTipoInstrumento;
					garantia.p_IdContraparteGarantia = model.IdContraparteGarantia ;
					garantia.p_IdMonedaInstrumento = model.IdMonedaInstrumento;
					garantia.p_IdConcepto = model.IdConcepto;
					garantia.p_IdInstrumento = model.IdInstrumento;
					garantia.p_IdOrigenGarantia = model.IdOrigenGarantia;
					garantia.CantidadAgrupada = model.INGCantidadAgrupada;
					garantia.CantidadARetirar = values.CantidadARetirar;
					garantia.MontoARetirar = (values.MontoARetirar) ? values.MontoARetirar : 0;
					dataEntrada.p_Garantias.push(garantia)

					break;


		}


		bcsServiciosGarantia.RetiroGarantiasAgrupadas(dataEntrada, function(value) {

			if(value && value.P_OUT_TABLA.length > 0){

			
				model.CantidadARetirar = value.P_OUT_TABLA[0].CantidadARetirar
				model.MontoARetirar = value.P_OUT_TABLA[0].MontoARetirar
				model.MontoARetirarMonSist = value.P_OUT_TABLA[0].MontoCalculado
				TotalMontoARetirar = TotalMontoARetirar + Number(value.P_OUT_TABLA[0].MontoCalculado);
			}

			else {

				if(value.P_OUT_ERRORESCONTADOR > 0 ){
					bcsComunes.msg("error", $translate.instant("UI_ERROR"), $translate.instant(value.P_OUT_MENSAJES[0].codigo))

				}

				model.CantidadARetirar = 0;
				model.MontoARetirar = 0;
				model.MontoARetirarMonSist = 0;


			}

			grillaCustodia.refresh();
			grillaCtaInv.refresh();

			$scope.data.kngTotalMontoARetirar = TotalMontoARetirar;
		
		}); 

		
	}
	
	
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
						IdCliente: { type: "number" },
												IdPortafolio: { type: "number" },
												IdConcepto: { type: "string" },
												IdInstrumento: { type: "number" },
												IdContraparteGarantia: { type: "number" },
												NombreConcepto: { type: "string" },
												NombreCliente: { type: "string" },
												IdMonedaInstrumento: { type: "string" },
												DocumentoIdentidad: { type: "string" },
												CodigoPortafolio: { type: "string" },
												IdClasificacionTipoInstrumento: { type: "string" },
												Nemotecnico: { type: "string" },
												IdTipoInstrumento: { type: "string" },
												NombreTipoInstrumento: { type: "string" },
												NombreOrigenGarantia: { type: "string" },
												RazonSocialContraparte: { type: "string" },
												CantidadAgrupada: {type: "number"},
												MontoAgrupado: {type: "number"},
												CantidadARetirar : {type: "number"},
												MontoARetirarMonSist : {type: "number"},
												MontoARetirar : {type: "number"}
					}
				}
			}
		}
	});
	$scope.data.confBasicaCustodiaRetirar = {
		dataSource			: dsGrillaGtaCustodia,
		columns				:  [ 
								{field: "NombreOrigenGarantia", 					title: "UI_ORIGEN", 						width: "100px",	groupable:true}
								,{field: "NombreCliente", 							title: "UI_CLIENTE", 						width: "150px",	groupable: true}
								,{field: "DocumentoIdentidad", 						title: "UI_DOCUMENTO_IDENTIDAD", 			width: "80px",	groupable: true}
								,{field: "CodigoPortafolio", 						title: "UI_PORTAFOLIO", 					width: "50px",	groupable: true}
								,{field: "NombreConcepto", 							title: "UI_CONCEPTO", 						width: "150px",	groupable: true}
								,{field: "RazonSocialContraparte", 					title: "UI_ENTIDAD_DESTINO", 				width: "150px",	groupable: true}
								,{field: "IdClasificacionTipoInstrumento", 			title: "UI_MERCADO", 						width: "60px",	groupable: true}
								,{field: "IdTipoInstrumento", 						title: "UI_TIPO_INSTRUMENTO", 				width: "80px",	groupable: true}
								,{field: "Nemotecnico", 							title: "UI_INSTRUMENTO", 					width: "90px",	groupable: true}	    		
								,{field: "CantidadAgrupada", 						title: "UI_CANTIDAD", 						width: "80px",	template : " <label> #:  (util.bcs_formato(CantidadAgrupada,'GEN','Cantidad','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
								,{field: "IdMonedaInstrumento", 					title: "UI_MONEDA", 						width: "55px",	groupable:true }
								,{field: "CantidadARetirar", 					title: "UI_CANTIDAD_A_RETIRAR", 			width: "100px",	spinners:false, editable: true,	template : " <label> #:  (util.bcs_formato(CantidadARetirar||0,'GEN','Cantidad',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}
								,{field: "MontoARetirarMonSist", 				title: "UI_MONTO_A_RETIRAR_MONEDA_SISTEMA",	width: "100px",	spinners:false, editable: true, template : " <label> #:  (util.bcs_formato(MontoARetirarMonSist||0,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoARetirar", 						title: "UI_VALOR_A_RETIRAR", 				width: "100px",	spinners:false,	template : " <label> #:  (util.bcs_formato(MontoARetirar||0,'GEN','Monto',(IdMonedaInstrumento!=''?IdMonedaInstrumento:(IdTipoReajuste!=''?IdTipoReajuste:IdMonedaSistema)),'','')).valor #</label>"}								
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
		save				: function(e) 	{ calculoMontoARetirar2('CUS',e.values, e.model); },
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
						IdCliente: { type: "number" },
						IdPortafolio: { type: "number" },
						IdConcepto: { type: "string" },
						IdInstrumento: { type: "number" },
						IdContraparteGarantia: { type: "number" },
						NombreConcepto: { type: "string" },
						NombreCliente: { type: "string" },
						IdMonedaInstrumento: { type: "string" },
						DocumentoIdentidad: { type: "string" },
						CodigoPortafolio: { type: "string" },
						IdClasificacionTipoInstrumento: { type: "string" },
						Nemotecnico: { type: "string" },
						IdTipoInstrumento: { type: "string" },
						NombreTipoInstrumento: { type: "string" },
						NombreOrigenGarantia: { type: "string" },
						RazonSocialContraparte: { type: "string" },
						CantidadAgrupada: {type: "number"},
						INGCantidadAgrupada : {type : "number"},
						MontoAgrupado: {type: "number"},
						MontoARetirarMonSist : {type: "number"},
						MontoARetirar : {type: "number"}
					}
				}
			}
		}
	});
	$scope.data.confBasicaCuentaInversionRetirar = {
		dataSource			: dsGrillaCtaInv,
		columns				: [  
								{field: "NombreOrigenGarantia", 					title: "UI_ORIGEN", 						width: "100px",	groupable:true}
								,{field: "NombreCliente", 							title: "UI_CLIENTE", 						width: "150px",	groupable: true}
								,{field: "DocumentoIdentidad", 						title: "UI_DOCUMENTO_IDENTIDAD", 			width: "80px",	groupable: true}
								,{field: "CodigoPortafolio", 						title: "UI_PORTAFOLIO", 					width: "50px",	groupable: true}
								,{field: "NombreConcepto", 							title: "UI_CONCEPTO", 						width: "150px",	groupable: true}
								,{field: "RazonSocialContraparte", 					title: "UI_ENTIDAD_DESTINO", 				width: "150px",	groupable: true}
								,{field: "IdClasificacionTipoInstrumento", 			title: "UI_MERCADO", 						width: "60px",	groupable: true}
								,{field: "IdTipoInstrumento", 						title: "UI_TIPO_INSTRUMENTO", 				width: "80px",	groupable: true}
								,{field: "Nemotecnico", 							title: "UI_INSTRUMENTO", 					width: "90px",	groupable: true}	    		
								,{field: "INGCantidadAgrupada", 					title: "UI_VALOR",							width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(INGCantidadAgrupada,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
								,{field: "IdMonedaInstrumento", 					title: "UI_MONEDA", 						width: "55px",	groupable:true }
								,{field: "MontoAgrupado", 							title: "UI_VALOR_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoAgrupado,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
								,{field: "MontoARetirar", 							title: "UI_VALOR_A_RETIRAR", 				width: "100px", align: "right", sortable : false, editable: true, template : " <label> #:  (util.bcs_formato(MontoARetirar,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>" }
								,{field: "MontoARetirarMonSist",					title: "UI_MONTO_A_RETIRAR", 				width: "100px", align: "right", sortable : false, template : " <label> #:  (util.bcs_formato(MontoARetirarMonSist,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
								
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
		save				: function(e) 	 { calculoMontoARetirar2('CTA',e.values, e.model);},
		gridCreated 		: function(grid) { grillaCtaInv = grid; },
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
	

	$scope.data.retirarGarantiaAgrupada = function(param)
    {
		bcsComunes.ventanaOverlay($scope.ventanaId,true);//overlay.encendido = true;

		var dataRetiro = {};

		dataRetiro.p_IdEmpresa =  $scope.SesionIdEmpresa;
		dataRetiro.p_IdUsuario = $scope.SesionIdUsuario; 
		dataRetiro.p_IdTrabajo =  1367;
		dataRetiro.p_IdModulo = "CUS";
		dataRetiro.p_UsaReglasNegocio = "N";
		dataRetiro.p_EsSimulacion = "N"
		dataRetiro.p_IdEmpleado = $scope.SesionIdEmpleado
		dataRetiro.p_Garantias = [];
		$scope.btnActivo = false;	


		var RegistrosCus = dsGrillaGtaCustodia.dataColumns(["IdCliente","IdPortafolio","IdClasificacionTipoInstrumento","IdContraparteGarantia","IdMonedaInstrumento","IdConcepto","INGCantidadAgrupada","CantidadAgrupada","Nemotecnico","IdInstrumento","IdOrigenGarantia","CantidadARetirar","MontoARetirarMonSist"],dsGrillaGtaCustodia.itemsEdited()) || [];
		$.each(RegistrosCus,function(key,value){		
			if ((value.MontoARetirar > 0 || value.CantidadARetirar > 0)){

				garantiaRetiro = {};
				garantiaRetiro.p_Cliente = value.IdCliente;
				garantiaRetiro.p_Portafolio = value.IdPortafolio;
				garantiaRetiro.p_FechaDeLiquidacion = objGTA.FechaConsulta;
				garantiaRetiro.p_IdClasificacionIntrumento = value.IdClasificacionTipoInstrumento;
				garantiaRetiro.p_IdContraparteGarantia = value.IdContraparteGarantia ;
				garantiaRetiro.p_IdMonedaInstrumento = value.IdMonedaInstrumento;
				garantiaRetiro.p_IdConcepto = value.IdConcepto;
				garantiaRetiro.p_IdInstrumento = value.IdInstrumento;
				garantiaRetiro.p_IdOrigenGarantia = value.IdOrigenGarantia;
				garantiaRetiro.p_Nemotecnico = value.Nemotecnico;
				garantiaRetiro.CantidadAgrupada = (value.IdClasificacionTipoInstrumento == "IF") ? value.CantidadAgrupada : value.INGCantidadAgrupada;
				garantiaRetiro.CantidadARetirar = (value.CantidadARetirar) ? value.CantidadARetirar : 0;
				garantiaRetiro.MontoARetirar = (value.MontoARetirarMonSist) ? value.MontoARetirarMonSist : 0;

				dataRetiro.p_Garantias.push(garantiaRetiro)
				
			}
		});

		
		var RegistrosCta = dsGrillaCtaInv.dataColumns(["IdCliente","IdPortafolio","IdClasificacionTipoInstrumento","IdContraparteGarantia","IdMonedaInstrumento","IdConcepto", "IdInstrumento", "IdOrigenGarantia", "INGCantidadAgrupada", "CantidadARetirar", "MontoARetirar"],dsGrillaCtaInv.itemsEdited()) || [];		
		$.each(RegistrosCta,function(key,value){
			if (value.MontoARetirar > 0){
				
				if (value.MontoARetirar <= value.INGCantidadAgrupada){

					garantiaRetiro = {}
					garantiaRetiro.p_Cliente = value.IdCliente;
					garantiaRetiro.p_Portafolio = value.IdPortafolio;
					garantiaRetiro.p_FechaDeLiquidacion = objGTA.FechaConsulta;
					garantiaRetiro.p_IdClasificacionIntrumento = value.IdClasificacionTipoInstrumento;
					garantiaRetiro.p_IdContraparteGarantia = value.IdContraparteGarantia ;
					garantiaRetiro.p_IdMonedaInstrumento = value.IdMonedaInstrumento;
					garantiaRetiro.p_IdConcepto = value.IdConcepto;
					garantiaRetiro.p_IdInstrumento = value.IdInstrumento;
					garantiaRetiro.p_IdOrigenGarantia = value.IdOrigenGarantia;
					garantiaRetiro.CantidadAgrupada = value.INGCantidadAgrupada;
					garantiaRetiro.CantidadARetirar = value.CantidadARetirar;
					garantiaRetiro.MontoARetirar = (value.MontoARetirar) ? value.MontoARetirar : 0;

					dataRetiro.p_Garantias.push(garantiaRetiro)

									
				} else {
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_ERROR_EXCEDE_MONTO"));
					return;
				}
				

			}
		});
		
		
		if (dataRetiro.p_Garantias.length > 0) {

	      	bcsServiciosGarantia.RetiroGarantiasAgrupadas(
				dataRetiro,
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

