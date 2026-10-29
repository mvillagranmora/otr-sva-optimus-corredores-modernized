angular.module('ModuloCustodia').controller('ConsultaMovimientosGarantiasCtrl', 
		function($scope,$timeout,$modal,$state,bcsComunes, $rootScope, bcsServicioPlataforma, $translate)
{	
	
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	
	$scope.data = {};
	$scope.filtros = {};
	
	$scope.data.instrumentoBusqueda={IdInstrumento: 0};	
	$scope.data.kngFecha 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.FechaConsultaOpt = {/*max:util.dateInicioDia($scope.SesionFechaSistema)*/};
	var filters = new Array();
	
	//COMBOBOX
	$scope.data.oSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa 	: {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			IdCliente 	: { "field": "IdCliente","operator": "eq", "value"   :  0 },
			EsCliente	: {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	$scope.data.oPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente 	: { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value"   :  0 }
		},
	};
	$scope.data.oConfiguracionTipoInstrumento={
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
				ExigeGarantia : { "field"   : "ExigeGarantia","operator": "eq", "value"   : "S" }
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
												INGMontoATasaCompraActual				: {type: "number"},
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
												INGPrecioGarantia 						: {type: "string"},
												FuenteTasaPrecioMercado 				: {type: "string"},
												FechaValorizacionGarantia 				: {type: "sysdate"},
												ISIN									: {type: "string"},
												MontoReajusteCorrido					: {type: "number"},
												MontoInteresCorrido						: {type: "number"},
												DescripcionEspecie 						: {type: "string"}
											}
										}
									},

	   }
	});
	 
	//GRILLA
	$scope.confBasicaGarantias = {
		dataSource			: dsGrillaGarantias,
		columns				: [ 
		       				   	 {field: "FechaLiquidacion",						title: "UI_FECHA_INGRESO", 					width: "80px",	groupable: true}
		       				   	,{field: "NombreOrigenGarantia", 					title: "UI_ORIGEN", 						width: "55px",	groupable:true, 	attributes:{style:"white-space:nowrap;"} }
		       				   	,{field: "NombreCliente", 							title: "UI_CLIENTE", 						width: "150px",	groupable: true, 	attributes:{style:"white-space:nowrap;"} }
		       				   	,{field: "DocumentoIdentidad", 						title: "UI_DOCUMENTO_IDENTIDAD", 			width: "80px",	groupable: true }
		       				   	,{field: "CodigoPortafolio", 						title: "UI_PORTAFOLIO", 					width: "50px",	groupable: true }
		       				   	,{field: "NombreConcepto", 							title: "UI_CONCEPTO", 						width: "150px",	groupable: true, 	attributes:{style:"white-space:nowrap;"} }
								,{field: "RazonSocialContraparte", 					title: "UI_ENTIDAD_DESTINO", 				width: "150px",	groupable: true, 	attributes:{style:"white-space:nowrap;"} }
								,{field: "IdTipoInstrumento", 						title: "UI_TIPO_INSTRUMENTO", 				width: "80px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
								,{field: "Nemotecnico", 							title: "UI_INSTRUMENTO", 					width: "90px",	groupable: true }
								,{field: "ISIN", 									title: 'UI_ISIN', 							width: "80px", 	groupable: true,	hidden: true,	attributes:{style:"white-space:nowrap;"}}	    		
								,{field: "CantidadGarantia", 						title: "UI_CANTIDAD_VALOR_RESCATE", 		width: "80px",	template : " <label> #:  (util.bcs_formato(CantidadGarantia,'GEN','Cantidad','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>" }
								,{field: "IdMonedaInstrumento", 					title: "UI_MONEDA", 						width: "55px",	groupable:true }
								,{field: "MontoValorizadoMonedaInstrumento", 		title: "UI_VALOR_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizadoMonedaInstrumento,'GEN','Monto',IdMonedaInstrumento,'','')).valor #</label>"}
								,{field: "MontoValorizado", 						title: "UI_MONTO_VALORIZADO", 				width: "95px",	template : " <label> #:  (util.bcs_formato(MontoValorizado,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>"}																
								,{field: "MontoInteresCorrido",						title: 'UI_INTERESES_CORRIDOS', 			width: "105px",	hidden: true,		template : " <label> #:  (util.bcs_formato(MontoInteresCorrido,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}
								,{field: "MontoReajusteCorrido", 					title: 'UI_REAJUSTES_VAC_CORRIDO', 			width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(MontoReajusteCorrido,'GEN','Monto','" + $scope.SesionIdMonedaSistema + "','','')).valor #</label>"}								
								,{field: "NemoGarantizado", 						title: "UI_OPERACION", 						width: "150px",	groupable: true}
								,{field: "FechaValorizacionGarantia", 				title: "UI_FECHA_VALORIZACION",				width: "80px",	groupable: true}
								,{field: "FuenteTasaPrecioMercado", 				title: "UI_FUENTE_PRECIO", 					width: "80px",	groupable: true}
								,{field: "FolioCarteraGarantizada", 				title: "UI_FOLIO_OPERACION_RELACIONADA",	width: "80px",	groupable: true,	format: "{0:n0}"}
								,{field: "FolioGarantia", 							title: "UI_SECUENCIA_GARANTIA",				width: "70px",	format: "{0:n0}"}
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
									grid.showColumn("INGMontoInteresCorridoCompraActual");
									grid.showColumn("INGMontoReajusteCorridoCompraActual");
								}
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
		$scope.data.oSocioNegocio.idActual = false;
		$scope.data.oPortafolio.idActual = 0;
		$scope.data.oConfiguracionInstrumento.buscar			= "-1";			
		$scope.seleccionGarantias = false;
		 
		dsGrillaGarantias.removeAll();
	};
	
 
	$scope.data.buscar = function()
	{	
		setTimeout(function(){			
			var filters = new Array();
 
			$scope.seleccionGarantias = false; //Limpia seleccion actual ya que se hará otra búsqueda
			
			if ($scope.data.oSocioNegocio.seleccionado.IdPersona)
				filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oSocioNegocio.seleccionado.IdCliente || 0)});
			if ($scope.data.oPortafolio.seleccionado.IdPortafolio)			
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oPortafolio.seleccionado.IdPortafolio || 0)});
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
				filters.push( { "field": "IdMonedaSistema","operator": "eq", "value" : $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda});										
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
	};
	
	$scope.limpiarFiltros = function() {
		$scope.filtro.Portafolio = "";
		$scope.filtro.cliente = undefined;
		$scope.filtro.PortafolioObjeto = undefined;
		$scope.filtro.clasificacionTipoInstrumento = "";
		$scope.filtro.tipoInstrumento = "";				
		$scope.filtro.accion = undefined;
		$scope.filtro.concepto = undefined;
		$scope.filtro.buscarinstrumento = "-1";
		$scope.data.oConfiguracionCliente.idActual = false;
		$scope.data.oConfiguracionPortafolio.filtros = { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
		$scope.data.oConfiguracionPortafolio.idActual = false;
		$scope.data.oConfiguracionPortafolio.seleccionado = false;
		//$scope.filtro.fecInicio 		 =  util.dateInicioDia($scope.SesionFechaSistema);
		//$scope.filtro.fecTermino		 =  util.dateInicioDia($scope.SesionFechaSistema);
	
	}
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
    
	//Fechas filtro
	//$scope.filtro.fecInicio 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	//$scope.filtro.fecTermino		 =  util.dateInicioDia($scope.SesionFechaSistema);
	
});