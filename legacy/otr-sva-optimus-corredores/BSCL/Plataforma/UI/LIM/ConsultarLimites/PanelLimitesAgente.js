(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesAgenteCtrl',
	function($rootScope,$scope,$translate,bcsComunes){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLAG 		= this;
		pLAG.idVentana	= bcsComunes.ventanaWinId($scope);
		pLAG.win			= {};
		pLAG.grilla		= {};
		pLAG.cmbbx		= {};
		pLAG.filtro		= {};
		pLAG.fn			= {};
		
		pLAG.filtro.IdClasificacionTipoInstrumentoDef= 0;
		pLAG.filtro.IdTipoInstrumentoDef			 = 0;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLAG.grilla.LimitesAgente = {
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
								IdPlantillaLimite	: { type: "string"},
								IdAgente			: { type: "number"},
								IdLimite			: { type: "number"},
								TipoInstrumento		: { type: "string"},
								Nemotecnico			: { type: "string"},
								TipoOperacion   	: { type: "string"},
								CanalIngreso     	: { type: "string"},
								Plazo				: { type: "number"},
								CantidadEmisores	: { type: "number"},
								ClasificacionRiesgo	: { type: "string"},
								CodigoOperador		: { type: "string"},
								CodigoYNombreEmpleado:{ type: "string"},
								Moneda				: { type: "string"},
								MontoIntraday		: { type: "number"},
								MontoOvernight		: { type: "number"},
								MontoTotal			: { type: "number"},
								MonedaCalculo		: { type: "string"},
								IdGrupoEmisor		: { type: "string"},
								IdEmisor			: { type: "string"},
								NombreEstadoLimite	: { type: "string"},
								TipoComportamiento  : { type: "string"},
								IdClasificacionTipoInstrumento : { type: "string"},
								DocumentoIdentidad  : { type: "string"},
								NombreCompleto      : { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				{field: "DocumentoIdentidad",	title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"), width: "100px", groupable: true },		
				{field: "NombreCompleto",		title: $translate.instant("UI_NOMBRE"),	width: "100px", groupable: true },
				{field: "CodigoYNombreEmpleado",title: $translate.instant("UI_AGENTE"), width: "80px",  groupable: true },
				{field: "TipoInstrumento",		title: $translate.instant("UI_TIPO_INSTRUMENTO"), width: "150px", groupable: true },				
				{field: "IdClasificacionTipoInstrumento", title: $translate.instant("UI_MERCADO"), width: "130px", groupable: true },
				{field: "TipoOperacion",		title: $translate.instant("UI_TIPO_OPERACION"), width: "130px", groupable: true, hidden: pLAG.filtro.IdClasificacionTipoInstrumento === 'SP'},
				{field: "CanalIngreso",			title: $translate.instant("UI_CANAL"), width: "50px", groupable: true},				
				{field: "Moneda",				title: $translate.instant("UI_MONEDA_OPERACION"), width: "80px", groupable: true},
				{field: "MontoTotal",			title: $translate.instant("UI_LIMITE_TOTAL"), width: "60px", template: "<label> #: (util.bcs_formato(MontoTotal,'ORD','MONTO','"+ $rootScope.SesionIdMonedaSistema +"','','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"},
				{field: "MonedaCalculo",		title: $translate.instant("UI_MONEDA_LIMITE"), width: "80px", groupable: true},
				{field: "NombreEstadoLimite",	title: $translate.instant("UI_ESTADO"), width: "40px", groupable: true},
				{field: "TipoComportamiento",	title: $translate.instant("UI_TIPO_VALIDACION"), width: "80px", groupable: true}
			],
			pageable	: true,
			pageSize	: 50,
			autoBind	: false,
			height		: 580,
		    selectable 	: "row",
		    groupable	: true,
		    userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    filterable 	: true
		};
		
			
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pLAG.cmbbx.TipoInstrumento={
			filtros : {
				IdEstado: {field: "IdEstado",	operator: "eq", value: "VIG" },
				IdBolsa	: {field: "IdBolsa",	operator: "eq", value: $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'RV' }
			}
		};
		
		pLAG.cmbbx.Agente={
			configuracion:{
				placeholder: $translate.instant('UI_AGENTE'),
			},
			filtros : {
				CodigoTipoCargoEmpleado : { field: "CodigoTipoCargoEmpleado", operator: "eq", value: "AGE" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pLAG.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(pLAG.filtro.IdEmpleado && pLAG.filtro.IdEmpleado > 0){
	    		niv1filters.push({ field: "IdAgente", operator: "eq", value: pLAG.dataModal.AgenteObj.IdEmpleado });
			}
			
			if(pLAG.filtro.IdTipoInstrumento){
		    	niv1filters.push({ field: "IdTipoInstrumento", operator: "eq", value: pLAG.filtro.IdTipoInstrumento });
		    }
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		    niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'RV' });
		    niv1filters.push({ field: "IdTipoLimite", operator: "eq", value: 'LIMCTPAGENTE' });
		    
		    if (niv1filters.length>0) pLAG.grilla.LimitesAgente.dataSource.filters(objfilters);
		    else pLAG.grilla.LimitesAgente.dataSource.filters(new Array());
		    	
		    pLAG.grilla.limiteSel = undefined;
//	    	}
//			else{
//				bcsComunes.msg("error",$translate.instant("UI_CONSULTAR_LIMITES"),$translate.instant("UI_INGRESE_FILTRO_AGENTE"));
//			}
		};
		
		pLAG.fn.limpiar = function(){
			pLAG.filtro.IdTipoInstrumento				= 0;
			pLAG.filtro.IdEmpleado						= 0;
			pLAG.grilla.LimitesAgente.dataSource.removeAll();
		}
		
	});
}());