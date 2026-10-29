(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesCuentaPropiaCtrl',
	function($scope,$translate,bcsComunes){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLCP 		= this;
		pLCP.idVentana	= bcsComunes.ventanaWinId($scope);
		pLCP.win		= {};
		pLCP.grilla		= {};
		pLCP.cmbbx		= {};
		pLCP.filtro		= {};
		pLCP.fn			= {};
		
		pLCP.filtro.IdClasificacionTipoInstrumentoDef	= 0;
		pLCP.filtro.IdTipoInstrumentoDef				= 0;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLCP.grilla.LimitesCuentaPropia = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=421",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdLimite: { type: "number"},
								NombreTipoLimite: { type: 'string' },
								TipoInstrumento: { type: 'string' },
								TipoOperacion: { type: 'string' },
								CanalIngreso: { type: 'string' },
								CodigoOperador: { type: 'string' },
								UICodigoOperador: { type: 'string' },
								Moneda: { type: 'string' },
								MontoIntraday: { type: 'number' },
								MontoOvernight: { type: 'number' },
								MontoTotal: { type: 'number' },
								UsaPlazo: { type: 'string' },
								UnidadPlazo: { type: 'string' },
								Plazo: { type: 'number' },
								IdEmisor: { type: 'string' },
								Porcentaje: { type: 'number' },
								IdEntidadClasificadoraRiesgo: { type: 'string' },
								ClasificacionRiesgo: { type: 'string' },
								MargenInferior: { type: 'number' },
								MargenSuperior: { type: 'number' },
								TipoComportamiento: { type: 'string' },
								IdEstadoConfigurable: { type: 'string' },
								IdEstadoAutorizacion: { type: 'string' },
							}
						}
					}
				}
			}),
			columns: [
				{ field: "NombreTipoLimite", title: $translate.instant("UI_TIPO_LIMITE"), width: "200px" },
				{ field: "TipoInstrumento", title: $translate.instant("UI_TIPO_INSTRUMENTO"), width: "200px", template: "<label translate> #: TipoInstrumento #</label>" },
				{ field: "TipoOperacion", title: $translate.instant("UI_TIPO_OPERACION"), width: "180px", template: "<label translate> #: TipoOperacion #</label>"  },
				{ field: "CanalIngreso", title: $translate.instant("UI_CANAL"), width: "60px", template: "<label translate> #: CanalIngreso #</label>" },
					
				{ field: 'UICodigoOperador', title: $translate.instant('UI_OPERADOR'), width: '100px', template: "<label translate> #: UICodigoOperador #</label>"  },
				{ field: "Moneda", title: $translate.instant("UI_MONEDA"), width: "100px", template: "<label translate> #: Moneda #</label>" },
				{ field: 'MontoIntraday', title: $translate.instant('UI_MONTO_INTRADAY'), width: '90px', template: "<label> #: (util.bcs_formato(MontoIntraday, 'ORD', 'MONTO', IdMoneda, '', '')).valor #</label>" },
				{ field: 'MontoOvernight', title: $translate.instant('UI_MONTO_OVERNIGHT'), width: '90px', template: "<label> #: (util.bcs_formato(MontoOvernight, 'ORD', 'MONTO', IdMoneda, '', '')).valor #</label>" },
				{ field: 'MontoTotal', title: $translate.instant('UI_MONTO'), width: '90px', template: "<label> #: (util.bcs_formato(MontoTotal, 'ORD', 'MONTO', IdMoneda, '', '')).valor #</label>" },
				{ field: 'UsaPlazo', title: $translate.instant('UI_USA_PLAZO'), width: '80px', template: "<label translate> #: UsaPlazo #</label>"  },
				{ field: 'UnidadPlazo', title: $translate.instant('UI_UNIDAD_PLAZO'), width: '80px', template: "<label translate> #: UnidadPlazo #</label>"  },
				{ field: 'Plazo', title: $translate.instant('UI_LIMCTPPLAZO'), width: '80px' },
				{ field: 'IdEmisor', title: $translate.instant('UI_EMISOR'), width: '100px', template: "<label translate> #: IdEmisor #</label>"  },
				{ field: 'Porcentaje', title: $translate.instant('UI_PORCENTAJE_CUSTODIA'), width: '110px' },
				{ field: 'IdEntidadClasificadoraRiesgo', title: $translate.instant('UI_ENTIDAD_CLASIFICADORA_RIESGO'), width: '140px', template: "<label translate> #: IdEntidadClasificadoraRiesgo #</label>"  },
				{ field: 'ClasificacionRiesgo', title: $translate.instant('UI_CLASIFICACION_RIESGO'), width: '120px', template: "<label translate> #: ClasificacionRiesgo #</label>"  },
				{ field: 'MargenInferior', title: $translate.instant('UI_MARGEN_TASA_INFERIOR'), width: '110px' },
				{ field: 'MargenSuperior', title: $translate.instant('UI_MARGEN_TASA_SUPERIOR'), width: '110px' },
					
				{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '110px' },
				{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '110px' },
				{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '110px' },
			],
			pageable	: true,
			pageSize	: 50,
			autoBind	: false,
			height		: 580,
		    selectable 	: "row",
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pLCP.cmbbx.TipoLimite={
			filtros : {
				IdEstado		: {field: "IdEstado",		operator: "eq" , value: "ACT" },
				EsCuentaPropia	: {field: "EsCuentaPropia",	operator: "eq" , value: "S" }
			}
		};
		
		pLCP.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				IdEstado : {field: "IdEstado",	operator: "eq" , value: "ACT" }
			}
		};
		
		pLCP.cmbbx.TipoInstrumento={
			filtros : {
				IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
				IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: '-1' }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pLCP.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(pLCP.filtro.IdTipoLimite){
				niv1filters.push({ field: "IdTipoLimite", operator: "eq", value: pLCP.filtro.IdTipoLimite });
	    	}
	    	if(pLCP.filtro.IdClasificacionTipoInstrumento){
	    		niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: pLCP.filtro.IdClasificacionTipoInstrumento });
	    	}
	    	if(pLCP.filtro.IdTipoInstrumento){
	    		niv1filters.push({ field: "IdTipoInstrumento", operator: "eq", value: pLCP.filtro.IdTipoInstrumento });
	    	}
	    	
	    	if (niv1filters.length>0) pLCP.grilla.LimitesCuentaPropia.dataSource.filters(objfilters);
	    	else pLCP.grilla.LimitesCuentaPropia.dataSource.filters(new Array());
	    	
	    	pLCP.grilla.limiteSel = undefined;
		};
		
		pLCP.fn.limpiar = function(){
			pLCP.filtro.IdTipoLimite					= 0;
			pLCP.filtro.IdClasificacionTipoInstrumento	= 0;
			pLCP.filtro.IdTipoInstrumento				= 0;
			pLCP.grilla.LimitesCuentaPropia.dataSource.removeAll();
		}
		
	});
}());