(function() {
	angular.module('ModuloPlataforma').controller('PanelLimitesClienteCtrl',
	function($rootScope,$scope,$translate,bcsComunes){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pLC 		= this;
		pLC.idVentana	= bcsComunes.ventanaWinId($scope);
		pLC.win			= {};
		pLC.grilla		= {};
		pLC.cmbbx		= {};
		pLC.filtro		= {};
		pLC.fn			= {};
		
		pLC.filtro.IdClasificacionTipoInstrumentoDef= 0;
		pLC.filtro.IdTipoInstrumentoDef				= 0;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLC.grilla.LimitesCliente = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=422",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								DocumentoIdentidad  : { type: "string"},
								NombreCompleto		: { type: "string"},
								IdPlantillaLimite	: { type: "string"},
								IdLimite			: { type: "number"},
								NombreTipoLimite	: { type: "string"},
								Nombre				: { type: "string"},
								CodigoPortafolio	: { type: "string"},
								NombreClasificacionTipoInstrumento: { type: "string"},
								TipoInstrumento		: { type: "string"},
								TipoOperacion		: { type: "string"},
								CanalIngreso		: { type: "string"},
								Moneda				: { type: "string"},
								Porcentaje			: { type: "number"},
								MontoTotal			: { type: "number"},
							}
						}
					}
				}
			}),
			columns: [
				{field: "DocumentoIdentidad",	title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),width: "100px", groupable: true },
				{field: "NombreCompleto",		title: $translate.instant("UI_NOMBRE"),				width: "150px", groupable: true },
				{field: "CodigoPortafolio",		title: $translate.instant("UI_CODIGO_PORTAFOLIO"),	width: "150px", groupable: true },
				{field: "NombreTipoLimite",		title: $translate.instant("UI_TIPO_LIMITE"),		width: "150px", groupable: true },
			    {field: "Nombre",				title: $translate.instant("UI_NOMBRE_LIMITE"),		width: "150px", groupable: true },			    
				{field: "NombreClasificacionTipoInstrumento",title: $translate.instant("UI_CLASIFICACION"),width: "160px", groupable: true },
			    {field: "TipoInstrumento",		title: $translate.instant("UI_TIPO_INSTRUMENTO"),	width: "200px", groupable: true },		
				{field: "TipoOperacion",		title: $translate.instant("UI_TIPO_OPERACION"),		width: "200px", groupable: true },
				{field: "CanalIngreso",			title: $translate.instant("UI_CANAL"),				width: "70px",  groupable: true },
				{field: "Moneda",				title: $translate.instant("UI_MONEDA"),				width: "100px", groupable: true },
				{field: "Porcentaje",			title: $translate.instant("UI_PORCENTAJE"),			width: "50",    template: "<label> #: (util.bcs_formato(Porcentaje,'ORD','TASA', IdMoneda,'','')).valor #</label>"},
				{field: "MontoTotal",			title: $translate.instant("UI_LINEA_ASIGNADA"),		width: "100px",	template: "<label> #: (util.bcs_formato(MontoTotal,'ORD','MONTO',IdMoneda,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"}
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
		
		pLC.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				IdEstado : {field: "IdEstado",	operator: "eq", value: "ACT" }
			}
		};
		
		pLC.cmbbx.TipoInstrumento={
			filtros : {
				IdEstado: {field: "IdEstado",	operator: "eq", value: "VIG" },
				IdBolsa	: {field: "IdBolsa",	operator: "eq", value: $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: '-1' }
			}
		};
		
		
		pLC.cmbbx.ClientePortafolio ={
				tipo : "combobox-cliente-portafolio",
				configuracion :{},
				idActual : 0,
				seleccionado : false,
				filtros: {
					IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
					EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
				}
		};	
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pLC.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			if(pLC.filtro.IdPortafolio && pLC.filtro.IdPortafolio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pLC.dataModal.ClientePortafolioObj.IdCliente });
	    		niv1filters.push({ field: "IdPortafolio", operator: "eq", value: pLC.dataModal.ClientePortafolioObj.IdPortafolio });
			}
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(pLC.filtro.IdClasificacionTipoInstrumento){
		    	niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: pLC.filtro.IdClasificacionTipoInstrumento });
		    }
		    if(pLC.filtro.IdTipoInstrumento){
		    	niv1filters.push({ field: "IdTipoInstrumento", operator: "eq", value: pLC.filtro.IdTipoInstrumento });
		    }
		    	
		    if (niv1filters.length>0) pLC.grilla.LimitesCliente.dataSource.filters(objfilters);
		    	else pLC.grilla.LimitesCliente.dataSource.filters(new Array());
		    	
		    	pLC.grilla.limiteSel = undefined;

		};
		
		pLC.fn.limpiar = function(){
			pLC.filtro.IdClasificacionTipoInstrumento	= 0;
			pLC.filtro.IdTipoInstrumento				= 0;
			pLC.filtro.IdSocioDeNegocio					= 0;
			pLC.grilla.LimitesCliente.dataSource.removeAll();
			pLC.filtro.IdPortafolio 					= false;
		}
		
	});
}());