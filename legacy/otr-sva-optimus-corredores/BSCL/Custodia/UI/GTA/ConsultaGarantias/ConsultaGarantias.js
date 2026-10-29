angular.module('ModuloCustodia').controller('ConsultaGarantiasCtrl',
				function($scope,$state,$translate,$resource,$timeout,bcsComunes,
						bcsServiciosGarantia,bcsServicioPlataforma,bcsOperacionesComunes,bcsServicioReportes)
{	
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	
	$scope.data = {};
	$scope.filtros = {};
	$scope.data.chkAgruparGarantias = false;
	$scope.data.instrumentoBusqueda={IdInstrumento: 0};	
	$scope.data.kngFecha 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.FechaConsultaOpt = {/*max:util.dateInicioDia($scope.SesionFechaSistema)*/};
	var filters = new Array();

	var obj = {};
	
	//COMBOBOX
	$scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		cargaClienteAutomatico :'S',
		filtros: {
			IdEmpresa 	: {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			IdCliente 	: { "field": "IdCliente","operator": "eq", "value"   :  0 },
			EsCliente	: {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};

	$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado 				: { "field"   : "IdEstado",					"operator": "eq", "value"   : "VIG" },
				ExigeGarantia 			: { "field"   : "ExigeGarantia",			"operator": "eq", "value"   : "S" },
				IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },
				IdTipoInstrumento: 		  { "field"   : "IdTipoInstrumento",		"operator": "eq", "value"   : "" },	
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};	
	$scope.data.oConfiguracionOperacion={
			tipo : "combobox-instrumento",
			configuracion : {},
			filtros : {
				IdTipoInstrumento : { "field" : "IdTipoInstrumento",  "operator": "eq",  "value" : "" },
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionEntidadDestinoGarantia={
			tipo : "combobox-entidad-destino-garantia",
			configuracion : {},
			filtros : {
				IdEstadoEntidadDestinoGarantia : { "field"   : "IdEstadoEntidadDestinoGarantia","operator": "eq", "value"   : "ACT" },
				IdContraparte     : { "field"   : "IdContraparte","operator": "eq", "value"   : 0 }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};		
	
	$scope.data.oConfiguracionEstadoGarantia={
		tipo : "combobox-estado-custodia",
		configuracion : {},
		filtros : {
			NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : 'GARANTIA' },
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : '' },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.data.oConfiguracionMoneda={
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : {
			IdMoneda : { "field"   : "IdMoneda","operator": "eq", "value"   : '' }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.data.oConfiguracionOrigenGarantia={
			tipo : "combobox-origen-garantia",
			configuracion : {},
			filtros : {
				IdOrigenGarantia : { "field"   : "IdOrigenGarantia","operator": "eq", "value"   : '' }				
				//read : false
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	$scope.data.oConfiguracionInstrumento={
			buscar          : "-1"    //para limpiar combo
	};		

	
	var dsGrillaGarantias = new bcs.DataSource({
	   type : "odata",
	   definition : {
		   serverFiltering 	: true, // Filtrado de datos en el servidor.
		   serverPaging     : true, // Paginado de los datos en el servidor.
		   serverSorting    : true, // Ordenamiento de los datos en el servidor.
		   allowCopy		: { delimeter: "\t" },
		   userSettings 	: true,
		   sort 			: { field: "IdGarantia", dir: "desc" },
		   pageSize         : 25,
		   filter			: filters,
		   transport 		: {read : {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1012",
								dataType : "json"
								}
			},
		   schema 			: { model: {
											fields : {
												IdGarantia								: {type: "number"},
												FechaIngresoRegistro					: {type: "sysdate"},
												FechaLiquidacion						: {type: "sysdate"},
												NombreInstrumento						: {type: "string"},
												Nemotecnico								: {type: "string"},
												INGCantidad								: {type: "number"},
												INGValorRescate							: {type: "number"},
												CantidadGarantia						: {type: "number"},
												INGMontoATasaCompraActual 				: {type: "number"},
												INGMontoATasaGarantiaActual				: {type: "number"},
												INGMontoCompraActualMonedaInstrumento	: {type: "number"},
												MontoValorizado							: {type: "number"},	
												MontoValorizadoMonedaInstrumento		: {type: "number"},
												NombreMonedaInstrumento					: {type: "string"},
												IdOrigenGarantia						: {type: "string"},
												FolioGarantia							: {type: "number"},
												FolioCarteraGarantizada					: {type: "number"},
												IdTipoInstrumento						: {type: "string"},
												NombreTipoInstrumento					: {type: "string"},
												NemoGarantizado         				: {type: "string"},
												RazonSocialContraparte					: {type: "string"},
												NombreConcepto							: {type: "string"},
												INGGlosa								: {type: "string"},
												NombreEstado							: {type: "string"},
												NombreCliente							: {type: "string"},
												DocumentoIdentidad						: {type: "string"},
												IdPortafolio							: {type: "number"},
												CodigoPortafolio 						: {type: "string"},
												IdMonedaInstrumento						: {type: "string"},
												IdTipoOperacion							: {type: "string"},
												NombreOrigenGarantia 					: {type: "string"},
												IdOrigenGarantia 						: {type: "string"},
												INGPrecioGarantia 						: {type: "number"},
												INGTasaGarantia							: {type: "number"},
												FuenteTasaPrecioMercado 				: {type: "string"},
												FechaValorizacionGarantia 				: {type: "sysdate"},												
												ISIN									: {type: "string"},
												MontoInteresCorrido						: {type: "number"},	
												MontoReajusteCorrido					: {type: "number"},
												MontoInteresCorridoMercado				: {type: "number"},
												MontoReajusteCorridoMercado				: {type: "number"},
												IdOperacionOrigenGarantia				: {type: "number"},
												DescripcionEspecie 						: {type: "string"},
												IdClasificacionTipoInstrumentoAval 		: {type: "string"},
												EsFungible 								: {type: "string"},
												ValorClasificacionRiesgo 				: {type: "string"},
												INGMontoMercadoActualMonedaInstrumento 	: {type: "number"},
												INGMontoATasaMercadoActual 				: {type: "number"},
												ConCertificadoAutomatico 				: {type: "string"}
											}
										}
									},

	   }
	});
	 
	//GRILLA
	$scope.confBasicaGarantias = {
		dataSource			: dsGrillaGarantias,
		columns				: [ 
							{field: "FolioGarantia", 							title: "UI_SECUENCIA_GARANTIA",				width: "70px",}
		       				   	,{field: "FechaLiquidacion",						title: "UI_FECHA_INGRESO", 					width: "80px",	groupable: true}
		       				   	,{field: "NombreOrigenGarantia", 					title: "UI_ORIGEN", 						width: "55px",	groupable:true}
		       				   	,{field: "NombreCliente", 							title: "UI_CLIENTE", 						width: "150px",	groupable: true}
		       				   	,{field: "DocumentoIdentidad", 						title: "UI_DOCUMENTO_IDENTIDAD", 			width: "80px",	groupable: true}
		       				   	,{field: "CodigoPortafolio", 						title: "UI_PORTAFOLIO", 					width: "50px",	groupable: true}
		       				   	,{field: "NombreConcepto", 							title: "UI_CONCEPTO", 						width: "150px",	groupable: true}
							,{field: "RazonSocialContraparte", 					title: "UI_ENTIDAD_DESTINO", 				width: "150px",	groupable: true}
							,{field: "IdClasificacionTipoInstrumentoAval", 		title: "UI_MERCADO", 						width: "60px",	groupable: true}
							,{field: "IdTipoInstrumento", 						title: "UI_TIPO_INSTRUMENTO", 				width: "80px",	groupable: true}
							,{field: "Nemotecnico", 							title: "UI_INSTRUMENTO", 					width: "90px",	groupable: true}
							,{field: "ValorClasificacionRiesgo", 				title: "UI_CLASIFICACION_RIESGO", 			width: "50px",	groupable: true}
							,{field: "EsFungible", 								title: "UI_FUNGIBLE", 						width: "50px",	groupable: true}
							,{field: "ISIN", 									title: 'UI_ISIN', 							width: "80px", 	groupable: true,	hidden: true}	    		
							,{field: "CantidadGarantia", 						title: "UI_CANTIDAD_VALOR_RESCATE", 		width: "80px",	template : " <label> #:  (util.bcs_formato(CantidadGarantia,'GEN','Cantidad','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
							,{field: "IdMonedaInstrumento", 					title: "UI_MONEDA", 						width: "55px",	groupable:true }
							,{field: "MontoValorizadoMonedaInstrumento", 		title: "UI_VALOR_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizadoMonedaInstrumento,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
							,{field: "MontoValorizado", 						title: "UI_MONTO_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizado,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>"}								
							
							,{field: "MontoInteresCorrido",						title: 'UI_INTERESES_CORRIDOS', 			width: "105px",	hidden: true,		template : " <label> #:  (util.bcs_formato(MontoInteresCorrido,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}
							,{field: "MontoReajusteCorrido", 					title: 'UI_REAJUSTES_VAC_CORRIDO', 			width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(MontoReajusteCorrido,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}								
							,{field: "NemoGarantizado", 						title: "UI_OPERACION", 						width: "150px",	groupable: true}
							,{field: "FechaValorizacionGarantia", 				title: "UI_FECHA_VALORIZACION",				width: "80px",	groupable: true}
							,{field: "FuenteTasaPrecioMercado", 				title: "UI_FUENTE_PRECIO", 					width: "80px",	groupable: true}
							,{field: "IdOperacionOrigenGarantia", 				title: "UI_FOLIO_OPERACION_GARANTIZADA",	width: "80px",	groupable: true}
							,{field: "DescripcionEspecie", 						title: "UI_ESPECIE",						width: "70px",	groupable: true}
						  ],		
		sortable			: true,
		pageable			: true,
		autoBind			: false,		
		filterable			: true,
		userSettings 		: true,
		height				: 520,
		selectable 			: "multiple row",
		gridCreated 		: function(grid) {
								$scope.grillaAll = grid;
								if ($scope.SesionLocale === 'PE'){
									grid.showColumn("ISIN");
									grid.showColumn("MontoInteresCorrido");
									grid.showColumn("MontoReajusteCorrido");
								}
	    }
	};

	// GRILLA GARANTIAS MASIVAS

	obj.getConfiguracionColumna = function(campo,extras) {
		if (!configuracionColumnas()[campo]) console.log("FALTA COLUMNA " + campo);
		return _.extend( {}, configuracionColumnas()[campo], extras);
	};
	
	obj.getSchemaConsultarGarantiasAgrupadas = function() {
		return {
			model: {
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
					INGCantidadAgrupada : {type: "number"},
					MontoAgrupado: {type: "number"},
					MontoValorizadoMonedaInstrumentoAGR : { type : "number"},
					IdOrigenGarantia : {type : "string"}

				 }
			}
		};
	};
	
	var dsGrillaGarantiasAgrupadas = new bcs.DataSource({ 
		type : "call",
		definition : {
			callTransaction	: "47524",
			serverFiltering : false, 
			serverPaging : false, 
			serverSorting : false,
			callParameters  : {},
			allowCopy		: { delimeter: "\t" },
			callResultSet   : "P_OUT_TABLA",
			sort: {},
			pageSize : 200,
			schema : obj.getSchemaConsultarGarantiasAgrupadas(),
			aggregate: []
		},			
	});
	

	$scope.confBasicaGarantiasAgrupadas = {
		dataSource			: dsGrillaGarantiasAgrupadas,
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
							,{field: "INGCantidadAgrupada", 					title: "UI_CANTIDAD", 						width: "80px",	template : " <label> #:  (util.bcs_formato(INGCantidadAgrupada,'GEN','Cantidad','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }	    		
							,{field: "CantidadAgrupada", 						title: "UI_CANTIDAD_VALOR_RESCATE", 		width: "80px",	template : " <label> #:  (util.bcs_formato(CantidadAgrupada,'GEN','Cantidad','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
							,{field: "IdMonedaInstrumento", 					title: "UI_MONEDA", 						width: "55px",	groupable:true }
							,{field: "MontoValorizadoMonedaInstrumentoAGR", 	title: "UI_MONTO_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizadoMonedaInstrumentoAGR,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}								
							,{field: "MontoAgrupado", 							title: "UI_VALOR_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoAgrupado,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
							
						  ],		
		sortable			: true,
		pageable			: true,
		autoBind			: false,		
		filterable			: true,
		userSettings 		: true,
		height				: 520,
		selectable 			: "multiple row",
		gridCreated 		: function(grid) {
								$scope.grillaAgrupada = grid;
								
	    }
	};

	//BOTONES LIMPIAR Y BUSCAR
	$scope.data.limpiar = function()
	{	
		$scope.data.oConfiguracionTipoInstrumento.idActual = 0;
		$scope.data.oConfiguracionOperacion.idActual = 0;		
		$scope.data.oConfiguracionEntidadDestinoGarantia.idActual = 0;		
		$scope.data.oConfiguracionOrigenGarantia.idActual = 0;
		$scope.data.oConfiguracionEstadoGarantia.idActual = 0;
		$scope.filtros.IdPortafolio = false;
		$scope.data.oConfiguracionInstrumento.buscar			= "-1";			
		$scope.seleccionGarantias = false;
		 
		dsGrillaGarantias.removeAll();
		dsGrillaGarantiasAgrupadas.removeAll();
	};
	
 
	$scope.data.buscar = function()
	{	
		if($scope.data.chkAgruparGarantias != true){
		setTimeout(function(){			
			var filters = new Array();
 
			$scope.seleccionGarantias = false; //Limpia seleccion actual ya que se hará otra búsqueda
			
			if ($scope.data.oClientePortafolio.seleccionado.IdPersona)
				filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdCliente || 0)});
			if ($scope.data.oClientePortafolio.seleccionado.IdPortafolio)			
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio || 0)});
			if ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento)
				filters.push( { "field": "IdTipoInstrumentoGarantizado","operator": "eq", "value" : $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento});
			if ($scope.data.oConfiguracionOperacion.seleccionado.IdInstrumento)
				filters.push( { "field": "IdInstrumentoCarteraGarantizada","operator": "eq", "value" : $scope.data.oConfiguracionOperacion.seleccionado.IdInstrumento});
			if ($scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte)
				filters.push( { "field": "IdContraparteGarantia","operator": "eq", "value" : $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte});
			if ($scope.data.oConfiguracionOrigenGarantia.seleccionado.IdOrigenGarantia)
				filters.push( { "field": "IdOrigenGarantia","operator": "eq", "value" : $scope.data.oConfiguracionOrigenGarantia.seleccionado.IdOrigenGarantia});
			if ($scope.data.oConfiguracionEstadoGarantia.seleccionado.IdEstado)
				filters.push( { "field": "IdEstado","operator": "eq", "value" : $scope.data.oConfiguracionEstadoGarantia.seleccionado.IdEstado});
			if ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda)
				filters.push( { "field": "IdMonedaInstrumento","operator": "eq", "value" : $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda});										
			if (typeof $scope.data.instrumentoBusqueda !== 'undefined') {
				if ($scope.data.instrumentoBusqueda.IdInstrumento > 0){ 
					filters.push( { "field": "IdInstrumento","operator": "eq", "value" : $scope.data.instrumentoBusqueda.IdInstrumento});	
				}
			}
			if (Number($scope.data.ngFolioOperacionGarantizada) > 0)
				filters.push({ field: "FolioCarteraGarantizada", operator: "eq", value: Number($scope.data.ngFolioOperacionGarantizada || 0)});
			
			
			filters.push({
				logic: "or",
				filters : [
				    {				
				    	logic: "and",
						filters : [{ field: "INGFechaLiquidacion", operator: "lte", value: $scope.data.kngFecha },
						           { field: "EGRCantidad", operator: "eq", value: 0 },
						           { field: "IdEstado", operator: "eq", value: 'VIG' }]
					},
				
					{
						logic: "and",
						filters : [{ field: "INGFechaLiquidacion", operator: "lte", value: $scope.data.kngFecha },
						           { field: "EGRCantidad", operator: "neq", value: 0 },
						           { field: "EGRFechaLiquidacion", operator: "gt", value: $scope.data.kngFecha }]
					}
				]
			});

			dsGrillaGarantias.filter(filters);
		}, 1000);  //se le da tiempo, para que combo instrumento alcance a actualizarse
	}
	else{
		setTimeout(function(){	
		var datos = {};	
			
		bcsComunes.ventanaOverlay($scope.winID, true);
		datos.p_IdEmpresa 						= $scope.SesionIdEmpresa;
		datos.p_IdUsuario 						= $scope.SesionIdUsuario;
		datos.p_IdModulo  						='CUS';
		datos.p_UsaReglasNegocio 				= 'N';
		datos.p_Cliente 	 	 				= ($scope.data.oClientePortafolio.seleccionado.IdCliente) ? $scope.data.oClientePortafolio.seleccionado.IdCliente : null;
		datos.p_Portafolio 		 				= ($scope.data.oClientePortafolio.seleccionado.IdPortafolio) ? $scope.data.oClientePortafolio.seleccionado.IdPortafolio : null;
		datos.p_IdClasificacionIntrumento		= ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento) ? $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento : null;
		datos.p_IdContraparteGarantia 		 	= ($scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte) ? $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte : null;	
		datos.p_IdEstado         				= 'VIG';
		datos.p_IdMonedaInstrumento 		 	= ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda) ? $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda : null;
		datos.p_IdInstrumento	 				= ($scope.data.instrumentoBusqueda) ? $scope.data.instrumentoBusqueda.IdInstrumento : null;
		datos.p_IdOrigenGarantia				= ($scope.data.oConfiguracionOrigenGarantia.seleccionado.IdOrigenGarantia) ? $scope.data.oConfiguracionOrigenGarantia.seleccionado.IdOrigenGarantia : null;
		datos.p_FechaDeLiquidacion				= util.dateSPdate($scope.data.kngFecha)
		dsGrillaGarantiasAgrupadas.setCallParameters(datos);

    	bcsComunes.ventanaOverlay($scope.winID, false);
			
		}, 1000); 
	}
	};
 
	//OPCIONES
	$scope.data.ingresoGarantia = function()
	{
		var clienteSeleccionado = $scope.data.oClientePortafolio.seleccionado
		bcsComunes.ventanaAbrir("IngresarGarantia",clienteSeleccionado,false,false,$scope.winID);
	};

	$scope.data.retiroGarantiaAgrupada = function(seleccionGarantiasAgrupada){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}

		var objGarantiasAgrupadas = {};

		objGarantiasAgrupadas.Registros = [];

		for (i=0; i< seleccionGarantiasAgrupada.length; i++){
			objGarantiasAgrupadas.Registros.push({

				"IdCliente": seleccionGarantiasAgrupada[i].IdCliente,
				"IdPortafolio": seleccionGarantiasAgrupada[i].IdPortafolio,
				"IdConcepto": seleccionGarantiasAgrupada[i].IdConcepto,
				"IdInstrumento": seleccionGarantiasAgrupada[i].IdInstrumento,
				"IdContraparteGarantia": seleccionGarantiasAgrupada[i].IdContraparteGarantia,
				"NombreConcepto": seleccionGarantiasAgrupada[i].NombreConcepto,
				"NombreCliente": seleccionGarantiasAgrupada[i].NombreCliente,
				"IdMonedaInstrumento": seleccionGarantiasAgrupada[i].IdMonedaInstrumento,
				"DocumentoIdentidad": seleccionGarantiasAgrupada[i].DocumentoIdentidad,
				"CodigoPortafolio": seleccionGarantiasAgrupada[i].CodigoPortafolio,
				"IdClasificacionTipoInstrumento": seleccionGarantiasAgrupada[i].IdClasificacionTipoInstrumento,
				"Nemotecnico": seleccionGarantiasAgrupada[i].Nemotecnico,
				"IdTipoInstrumento": seleccionGarantiasAgrupada[i].IdTipoInstrumento,
				"NombreTipoInstrumento": seleccionGarantiasAgrupada[i].NombreTipoInstrumento,
				"NombreOrigenGarantia": seleccionGarantiasAgrupada[i].NombreOrigenGarantia,
				"RazonSocialContraparte": seleccionGarantiasAgrupada[i].RazonSocialContraparte,
				"CantidadAgrupada": seleccionGarantiasAgrupada[i].CantidadAgrupada,
				"MontoAgrupado": seleccionGarantiasAgrupada[i].MontoAgrupado,
				"INGCantidadAgrupada" : seleccionGarantiasAgrupada[i].INGCantidadAgrupada,
				"MontoValorizadoMonedaInstrumentoAGR" : seleccionGarantiasAgrupada[i].MontoValorizadoMonedaInstrumentoAGR,
				"IdOrigenGarantia" : seleccionGarantiasAgrupada[i].IdOrigenGarantia
				
			});
		}

		objGarantiasAgrupadas.FechaConsulta = util.dateSPdate($scope.data.kngFecha);

		var retiro = bcsComunes.ventanaAbrir("RetirarGarantiaAgrupadas", objGarantiasAgrupadas, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(retiro);
		promesa.then(function(data){
			if (typeof data !== 'undefined'){
				dsGrillaGarantias.read();
				dsGrillaGarantiasAgrupadas.read();
			}
		}); 

	}
	
	$scope.data.retiroGarantia = function(seleccionGarantias)
	{
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		var objGarantias = [];
		for (i=0; i< seleccionGarantias.length; i++){
			objGarantias.push({
				"IdGarantia"							: seleccionGarantias[i].IdGarantia,
				"IdOrigenGarantia"						: seleccionGarantias[i].IdOrigenGarantia,
				"FolioGarantia" 						: seleccionGarantias[i].FolioGarantia,
				"NombreInstrumento"						: seleccionGarantias[i].NombreInstrumento,
				"Nemotecnico"							: seleccionGarantias[i].Nemotecnico,
				"CantidadARetirar"						: seleccionGarantias[i].CantidadARetirar || 0,				
				"MontoARetirar" 						: seleccionGarantias[i].MontoARetirar || 0,
				"MontoARetirarMonSist" 					: seleccionGarantias[i].MontoARetirarMonSist || 0,
				"INGCantidad"							: seleccionGarantias[i].INGCantidad,
				"INGValorRescate"						: seleccionGarantias[i].INGValorRescate,
								
				"MontoGarantia"							: seleccionGarantias[i].MontoGarantia,
				"MontoGarantiaMonedaInstrumento"		: seleccionGarantias[i].MontoGarantiaMonedaInstrumento,

				"NombreMoneda"							: seleccionGarantias[i].NombreMoneda,
				"FechaIngresoRegistro"					: seleccionGarantias[i].FechaIngresoRegistro,
				"NombreCliente" 						: seleccionGarantias[i].NombreCliente,
				"DocumentoIdentidad" 					: seleccionGarantias[i].DocumentoIdentidad,
				"CodigoPortafolio" 						: seleccionGarantias[i].CodigoPortafolio,
				"IdMonedaInstrumento"     				: seleccionGarantias[i].IdMonedaInstrumento,
				"INGPrecioGarantia"						: seleccionGarantias[i].INGPrecioGarantia,
				"NombreConcepto"             			: seleccionGarantias[i].NombreConcepto,
				"RazonSocialContraparte"      			: seleccionGarantias[i].RazonSocialContraparte,
				"ISIN"									: seleccionGarantias[i].ISIN,
				"EsFungible"							: seleccionGarantias[i].EsFungible,
				"MontoInteresCorrido"					: seleccionGarantias[i].MontoInteresCorrido,
				"MontoReajusteCorrido"					: seleccionGarantias[i].MontoReajusteCorrido,
				"FolioCarteraGarantizada"				: seleccionGarantias[i].FolioCarteraGarantizada,
				"CorteMinimo"							: seleccionGarantias[i].CorteMinimo,
				"ValorNominal"							: seleccionGarantias[i].ValorNominal,
				"IdTipoInstrumento"						: seleccionGarantias[i].IdTipoInstrumento,
				"IdClasificacionTipoInstrumento"		: seleccionGarantias[i].IdClasificacionTipoInstrumento,
				"Cantidad"								: seleccionGarantias[i].Cantidad,
				"MontoValorizado"						: seleccionGarantias[i].MontoValorizado,  //para cta y especies tiene el monto real
				"MontoValorizadoInstrumento"			: seleccionGarantias[i].MontoValorizadoMonedaInstrumento,  //para cta y especies tiene el monto real
				"DescripcionEspecie"					: seleccionGarantias[i].DescripcionEspecie,
				"IdTipoReajuste"						: seleccionGarantias[i].IdTipoReajuste,
				"PrecioRetiro"							: seleccionGarantias[i].INGPrecioMercadoActual,
				"TasaRetiro"							: seleccionGarantias[i].INGTasaMercadoActual,
				"IdEntidadDestinoGarantiaEmpresa"		: seleccionGarantias[i].IdEntidadDestinoGarantiaEmpresa,
				"IdConcepto"							: seleccionGarantias[i].IdConcepto,
				"IdContraparteGarantia" 				: seleccionGarantias[i].IdContraparteGarantia,
				"ConCertificadoAutomatico" 				: seleccionGarantias[i].ConCertificadoAutomatico,
				"ValorClasificacionRiesgo" 				: seleccionGarantias[i].ValorClasificacionRiesgo
			});
		}
		
			
		var retiro = bcsComunes.ventanaAbrir("RetirarGarantia", objGarantias, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(retiro);
		promesa.then(function(data){
			if (typeof data !== 'undefined'){
				dsGrillaGarantias.read();
				dsGrillaGarantiasAgrupadas.read();
			}
		});

		var aux = $.extend(true, {}, $scope.seleccionGarantias);
		bcsOperacionesComunes.AlmacenarRegistrosGrilla(aux);
		bcsComunes.ventanaAbrir("RetirarGarantia", {} );
	};
	
	$scope.data.certificarGarantia = function()
	{	
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_PENDIENTE_DE_DESARROLLO")
		},$scope.winID);
	};

	
	
	//Primer evento - filtro inicial
	$timeout(function(){
		$scope.data.oConfiguracionTipoInstrumento.filtros.read = true;
		$scope.data.oConfiguracionOrigenGarantia.filtros.read = true;
	},100);
	
	$scope.obtenerComprobante = function(){		
		var ConfiguracionTemplate = new bcs.DataSource({
			type: "odata",
			definition:{
				serverFiltering:true,
				sort: { field: "ReportVersion", dir: "desc" },
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015&c=1",
						dataType: "json",
						data:{
							$select: "\"CompanyId\",\"Id\",\"ReportName\",\"ReportVersion\""
						}
					}
				},
				schema: {
					model:
					{
						fields : {
							Id: {type: "string"},
							ReportName: {type: "string"},
							ReportVersion: {type: "string"}
						}
					}
				}
			}
		});

		var filtro = new Array();
		filtro.push({ field: "CompanyId", operator: "eq", value: $scope.SesionIdEmpresa });
		filtro.push({ field: "ReportName", operator: "eq", value: "COMPROBANTEINGRESORETIROGARANTIA" });
		
		ConfiguracionTemplate.query({filter:filtro}).then(function() {
			if(ConfiguracionTemplate.data().length > 0){
				for(var i=0; i < $scope.seleccionGarantias.length; i++){
					var movimiento = $scope.seleccionGarantias[i];
					var pdf417 = movimiento.FolioGarantia+""+movimiento.IdCliente+""+movimiento.DocumentoIdentidad+""+movimiento.FechaOperacion;
					var data = {
						"P_IDEMPRESA" : $scope.SesionIdEmpresa,
						"P_IDMODULO" : "CUS",
						"P_IDCLIENTE" : movimiento.IdCliente,
						"P_IDPORTAFOLIO" : movimiento.IdPortafolio,
						"P_FOLIOGARANTIA" : movimiento.FolioGarantia,
						"$schema":[{"field":"LogoCorredora", "type":"blob"}]
					};	
					bcsServicioReportes.descargarReportePdf417(ConfiguracionTemplate.data()[0].Id, 13019, "COMPROBANTEINGRESORETIROGARANTIA", pdf417, "PDF", data, $scope.winID, false);
				}
			}else{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_OBTENER_REPORTE")
				},$scope.winID);
				
			}
		});
		
	};
	
	
	//Llamado desde otra ventana(Ej.: Cuenta Inversion Cliente)
	if ($scope.parametros.IdSocioDeNegocio !== undefined){
		$scope.data.oClientePortafolio.idDefecto	= $scope.parametros.IdSocioDeNegocio;
		$scope.data.oClientePortafolio.filtros.IdCliente.value = $scope.parametros.IdCliente;
		$scope.data.oClientePortafolio.filtros.read = true;

		$scope.data.oPortafolio.idDefecto	= $scope.parametros.IdPortafolio;
		$scope.data.oPortafolio.filtros.IdPortafolio.value = $scope.parametros.IdPortafolio;
		$scope.data.oPortafolio.filtros.read = true;

		$scope.data.oConfiguracionMoneda.idDefecto	= $scope.parametros.IdMoneda;
		$scope.data.oConfiguracionMoneda.filtros.IdMoneda.value = $scope.parametros.IdMoneda;

		$scope.data.oConfiguracionOrigenGarantia.idDefecto	= $scope.parametros.IdOrigenGarantia;
		$scope.data.oConfiguracionOrigenGarantia.filtros.IdOrigenGarantia.value = $scope.parametros.IdOrigenGarantia;

		$scope.data.oConfiguracionEstadoGarantia.idDefecto	= "VIG";
		$scope.data.oConfiguracionEstadoGarantia.filtros.IdEstado.value = "VIG";
 
		$scope.data.oClientePortafolio.activado = false;
		$scope.data.oPortafolio.activado = false;
		$scope.data.oConfiguracionOrigenGarantia.activado = false;
		$scope.data.oConfiguracionMoneda.activado = $scope.parametros.ActivarCombo;
		
		$scope.data.buscar(); //busca de acuerdo a los criterios enviados
	};	
	
	//OPTIMUSPE-1187
	$scope.generarArchivoGarantia = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'CUS',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'GENERAR_ARCHIVO_GARANTIA',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdCliente',
					Valor : ($scope.data.oClientePortafolio.seleccionado.IdPersona) ? Number($scope.data.oClientePortafolio.seleccionado.IdCliente) : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdPortafolio',
					Valor : ($scope.data.oClientePortafolio.seleccionado.IdPortafolio) ? Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio) : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdTipoInstrumentoGarantizado',
					Valor : ($scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento) ? $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdInstrumentoCarteraGarantizada',
					Valor : ($scope.data.oConfiguracionOperacion.seleccionado.IdInstrumento) ? $scope.data.oConfiguracionOperacion.seleccionado.IdInstrumento : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdContraparteGarantia',
					Valor : ($scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte) ? $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdContraparte : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdOrigenGarantia',
					Valor : ($scope.data.oConfiguracionOrigenGarantia.seleccionado.IdOrigenGarantia) ? $scope.data.oConfiguracionOrigenGarantia.seleccionado.IdOrigenGarantia : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdEstado',
					Valor : ($scope.data.oConfiguracionEstadoGarantia.seleccionado.IdEstado) ? $scope.data.oConfiguracionEstadoGarantia.seleccionado.IdEstado : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdMonedaInstrumento',
					Valor : ($scope.data.oConfiguracionMoneda.seleccionado.IdMoneda) ? $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda : null
				},{
					IdInterno : 0,
					NombreParametro : 'IdInstrumento',
					Valor : (typeof $scope.data.instrumentoBusqueda !== 'undefined') ? $scope.data.instrumentoBusqueda.IdInstrumento : null
				},{
					IdInterno : 0,
					NombreParametro : 'FolioCarteraGarantizada',
					Valor : (Number($scope.data.ngFolioOperacionGarantizada) > 0) ? Number($scope.data.ngFolioOperacionGarantizada) : null
				},{
					IdInterno : 0,
					NombreParametro : 'FechaLiquidacion',
					Valor : util.dateSPdate($scope.data.kngFecha) || null
				}
				]
		}
		
		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then( function(result) {
			bcsComunes.ventanaOverlay($scope.winID,false);
			if(result.P_OUT_ERRORESCONTADOR === 0){
				var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
				bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
					conflictos : util.parseServerResponse(result)}, $scope.winID);
			}else{
				bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
			}
		},
		function(result){
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});

	}

	
	
	$scope.$watch('data.chkAgruparGarantias',function(newval,oldval){

		$scope.data.buscar();
		var firstWidth = $('#grid-garantias-masiva .k-grid-content');
        firstWidth.css('height', '422px');
		if(newval = true){

			if(!$scope.grillaAgrupada){
				var firstWidth = $('#grid-garantias-masiva .k-grid-content');
        		firstWidth.css('height', '422px')
			} ;
		}			
	});
	
	$scope.exportarExcelGTA = function(){
		if ($scope.data.chkAgruparGarantias == true){
			$scope.tab.exportarExcelAgrupado();
		}
		else{
			$scope.tab.exportarExcelVig();
		}
		
	};

	
	
});

