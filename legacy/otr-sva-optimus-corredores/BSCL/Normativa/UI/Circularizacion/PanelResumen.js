(function() {
	angular.module('ModuloNormativa').controller('PanelResumenCtrl',
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
		pLCP.PeriodoOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pLCP.grilla.Nomina = {
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
								IdLimite			: { type: "number"},
								NombreTipoLimite	: { type: "string"},
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
								IdEmisor			: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "NombreTipoLimite",		title: $translate.instant("UI_TIPO_LIMITE"),			width: "200px"	},
				{field: "TipoInstrumento",		title: $translate.instant("UI_TIPO_INSTRUMENTO"),		width: "200px"	},
				{field: "Nemotecnico",			title: $translate.instant("UI_INSTRUMENTO"),			width: "100px"	},
				{field: "TipoOperacion",		title: $translate.instant("UI_TIPO_OPERACION"),			width: "180px"	},
				{field: "CanalIngreso",			title: $translate.instant("UI_CANAL"),					width: "60px"	},
				{field: "Plazo",				title: $translate.instant("UI_PLAZO_MAXIMO_DIAS"),		width: "100px"	},
				{field: "CantidadEmisores",		title: $translate.instant("UI_CANTIDAD_EMISORES"),		width: "100px"	},
				{field: "ClasificacionRiesgo",	title: $translate.instant("UI_CLASIFICACION_RIESGO"),	width: "120px"	},
				{field: "CodigoOperador",		title: $translate.instant("UI_OPERADOR"),				width: "100px"	},
				{field: "CodigoYNombreEmpleado",title: $translate.instant("UI_AGENTE"),					width: "100px"	},
				{field: "Moneda",				title: $translate.instant("UI_MONEDA_OPERACION"),		width: "100px"	},
				{field: "MontoIntraday",		title: $translate.instant("UI_MONTO_INTRADAY"),			width: "90px",	template: "<label> #: (util.bcs_formato(MontoIntraday,'ORD','MONTO',IdMoneda,'','')).valor #</label>"},
				{field: "MontoOvernight",		title: $translate.instant("UI_MONTO_OVERNIGHT"),		width: "90px",	template: "<label> #: (util.bcs_formato(MontoOvernight,'ORD','MONTO',IdMoneda,'','')).valor #</label>"},
				{field: "MontoTotal",			title: $translate.instant("UI_MONTO_TOTAL"),			width: "90px",	template: "<label> #: (util.bcs_formato(MontoTotal,'ORD','MONTO','"+ $scope.$root.SesionIdMonedaSistema +"','','')).valor #</label>"},
				{field: "MonedaCalculo",		title: $translate.instant("UI_MONEDA_LIMITE"),			width: "100px"	},
				{field: "IdGrupoEmisor",		title: $translate.instant("UI_GRUPO_EMISORES"),			width: "100px"	},
				{field: "IdEmisor",				title: $translate.instant("UI_EMISOR"),					width: "80px"	}
			],
			hideDetailButton: function(row) {
				var hide = true;
				if(row.IdTipoLimite === 'LIMCTPEXCLUIREMISOR' || row.IdTipoLimite === 'LIMCTPMARGENTASAS') 
					hide = false;
				return hide;
			},
			pageable	: true,
			pageSize	: 50,
			autoBind	: false,
			height		: 540,
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

	    	if(pLCP.filtro.IdClasificacionTipoInstrumento){
	    		niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: pLCP.filtro.IdClasificacionTipoInstrumento });
	    	}

	    	
	    	if (niv1filters.length>0) pLCP.grilla.Nomina.dataSource.filters(objfilters);
	    	else pLCP.grilla.Nomina.dataSource.filters(new Array());
	    	
	    	pLCP.grilla.limiteSel = undefined;
		};
		
		pLCP.fn.SolicitarCircularizacion = function() {
		
						ventanaSolicitarCircularizacion = bcsComunes.ventanaAbrir("SolicitarCircularizacion", {PrevPeriodo :pLCP.Periodo}, false, false, pLCP.idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaSolicitarCircularizacion);
				       // promesa.then(function(data){
				        	//$scope.buscarCircular();
						//});
		};
		
		pLCP.fn.limpiar = function(){
			
			pLCP.filtro.IdClasificacionTipoInstrumento	= 0;
			pLCP.grilla.Nomina.dataSource.removeAll();
		};
		

	});
}());