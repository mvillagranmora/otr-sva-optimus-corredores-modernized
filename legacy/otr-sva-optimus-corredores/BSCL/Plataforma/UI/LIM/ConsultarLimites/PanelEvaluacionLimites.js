(function() {
	angular.module('ModuloPlataforma').controller('PanelEvaluacionLimitesCtrl',
	function($rootScope,$scope,$translate,bcsComunes){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var pEL 		= this;
		pEL.idVentana	= bcsComunes.ventanaWinId($scope);
		pEL.win		= {};
		pEL.grilla		= {};
		pEL.cmbbx		= {};
		pEL.filtro		= {};
		pEL.fn			= {};
		
		pEL.filtro.IdClasificacionTipoInstrumentoDef= 0;
		pEL.filtro.IdTipoInstrumentoDef				= 0;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		pEL.grilla.EvaluacionLimites = {
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
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=433",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdEvaluacionLimite	: { type: "number"},
								IdSecuenciaEvaluacion:{ type: "number"},
								IdLimite			: { type: "number"},
								TipoLimite			: { type: "string"},
								ClasificacionTipoInstrumento: { type: "string"},
								TipoInstrumento		: { type: "string"},
								TipoOperacion   	: { type: "string"},
								CanalIngreso     	: { type: "string"},
								IdOperacion			: { type: "number"},
								MontoUtilizado		: { type: "number"},
								Moneda				: { type: "string"},
								MontoLimite			: { type: "number"},
								MontoCalculado		: { type: "number"},
								IdMonedaCalculo		: { type: "string"},
								MonedaCalculo		: { type: "string"},
								MontoExcedido		: { type: "number"},
								MontoRemanente		: { type: "number"},
								//MontoExceso			: { type: "number"},
								EstadoEvaluacion	: { type: "string"},
								EstadoAutorizacion	: { type: "string"},
								ConceptoExceso		: { type: "string"},
								IdTipoEvento		: { type: "string"},
								Prioridad			: { type: "number"},
								ConceptoEvaluacion	: { type: "string"},
								IdTipoEntidad		: { type: "string"},
								DocumentoIdentidad	: { type: "string"},
								NombreCliente		: { type: "string"},
								CodigoPortafolio	: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				{field: "DocumentoIdentidad",		title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),	width: "120px", groupable: true, template: "<label> #: TipoDocumentoIdentidad === 'R' ? util.formatRut(DocumentoIdentidad) : DocumentoIdentidad #</label>" },
				{field: "NombreCliente",			title: $translate.instant("UI_NOMBRE_CLIENTE"),			width: "190px", groupable: true },
				{field: "CodigoPortafolio",			title: $translate.instant("UI_CODIGO_PORTAFOLIO"),		width: "100px", groupable: true	},
			    //{field: "IdEvaluacionLimite",		title: $translate.instant("UI_EVALUACION_LIMITE"),		width: "100px"	},
			    {field: "IdSecuenciaEvaluacion",	title: $translate.instant("UI_SECUENCIA_EVALUACION"),	width: "110px", groupable: true	},
			    {field: "IdLimite",					title: $translate.instant("UI_LIMITE"),					width: "70px",	groupable: true },
			    {field: "TipoLimite",				title: $translate.instant("UI_TIPO_LIMITE"),			width: "140px", groupable: true },
			    {field: "ClasificacionTipoInstrumento",	title: $translate.instant("UI_CLASIFICACION"),		width: "120px",	groupable: true },
			    {field: "TipoInstrumento",			title: $translate.instant("UI_TIPO_INSTRUMENTO"),		width: "140px",	groupable: true },
				{field: "TipoOperacion",			title: $translate.instant("UI_TIPO_OPERACION"),			width: "140px",	groupable: true },
				{field: "CanalIngreso",				title: $translate.instant("UI_CANAL"),					width: "60px",  groupable: true	},
				{field: "IdOperacion",				title: $translate.instant("UI_OPERACION"),				width: "60px",  groupable: true },
				{field: "MontoUtilizado",			title: $translate.instant("UI_MONTO_UTILIZADO"),		width: "100px",	template: "<label> #: IdMoneda!=='' ? (util.bcs_formato(MontoUtilizado,'ORD','MONTO',IdMoneda,'','')).valor : MontoUtilizado #</label>", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"},
				{field: "Moneda",					title: $translate.instant("UI_MONEDA_OPERACION"),		width: "110px",	groupable: true },
				{field: "MontoLimite",				title: $translate.instant("UI_MONTO_LIMITE"),			width: "100px",	template: "<label> #: IdMonedaCalculo !== '' ? (util.bcs_formato(MontoLimite,'ORD','MONTO',IdMonedaCalculo,'','')).valor : MontoLimite #</label>", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"},
				{field: "MontoCalculado",			title: $translate.instant("UI_MONTO_CALCULADO"),		width: "100px",	template: "<label> #: IdMonedaCalculo !== '' ? (util.bcs_formato(MontoCalculado,'ORD','MONTO',IdMonedaCalculo,'','')).valor : MontoCalculado #</label>", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"},
				{field: "MonedaCalculo",			title: $translate.instant("UI_MONEDA_CALCULO"),			width: "110px", groupable: true },
				{field: "MontoExcedido",			title: $translate.instant("UI_MONTO_EXCEDIDO"),			width: "100px",	template: "<label> #: IdMonedaCalculo !== '' ? (util.bcs_formato(MontoExcedido,'ORD','MONTO',IdMonedaCalculo,'','')).valor : MontoExcedido #</label>", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"},
				{field: "MontoRemanente",			title: $translate.instant("UI_MONTO_REMANENTE"),		width: "100px",	template: "<label> #: IdMonedaCalculo !== '' ? (util.bcs_formato(MontoRemanente,'ORD','MONTO',IdMonedaCalculo,'','')).valor : MontoRemanente #</label>", aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"},
				{field: "EstadoEvaluacion",			title: $translate.instant("UI_ESTADO_EVALUACION"),		width: "100px", groupable: true	},
				{field: "EstadoAutorizacion",		title: $translate.instant("UI_ESTADO_AUTORIZACION"),	width: "100px", groupable: true	},
				//{field: "ConceptoExceso",			title: $translate.instant("UI_CONCEPTO_EXCESO"),		width: "90px"	},
				//{field: "IdTipoEvento",				title: $translate.instant("UI_ID_TIPO_EVENTO"),			width: "80px"	},
				{field: "Prioridad",				title: $translate.instant("UI_PRIORIDAD"),				width: "70px",  groupable: true },
				{field: "ConceptoEvaluacion",		title: $translate.instant("UI_CONCEPTO_EVALUACION"),	width: "150px", groupable: true },
				{field: "IdTipoEntidad",			title: $translate.instant("UI_ID_TIPO_ENTIDAD"),		width: "85px",  groupable: true }
				
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
		    filterable	: true
		};
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		pEL.cmbbx.ClientePortafolio ={
				tipo : "combobox-cliente-portafolio",
				configuracion :{},
				idActual : 0,
				seleccionado : false,
				filtros: {
					IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
					EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			   }
		};	
		
		pEL.cmbbx.TipoLimite={
			filtros : {
				IdEstado : {field: "IdEstado",	operator: "eq" , value: "ACT" }
			}
		};
		
		pEL.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				IdEstado : {field: "IdEstado",	operator: "eq" , value: "ACT" }
			}
		};
		
		pEL.cmbbx.TipoInstrumento={
			filtros : {
				IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
				IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: '-1' }
			}
		};
		
		pEL.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "EVALUACION" }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		pEL.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
			if(pEL.filtro.IdPortafolio && pEL.filtro.IdPortafolio > 0){
	    		niv1filters.push({ field: "IdCliente", operator: "eq", value: pEL.dataModal.ClientePortafolioObj.IdCliente });
	    		niv1filters.push({ field: "IdPortafolio", operator: "eq", value: pEL.dataModal.ClientePortafolioObj.IdPortafolio });
	    	}
			if(pEL.filtro.IdTipoLimite){
				niv1filters.push({ field: "IdTipoLimite", operator: "eq", value: pEL.filtro.IdTipoLimite });
	    	}
	    	if(pEL.filtro.IdClasificacionTipoInstrumento){
	    		niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: pEL.filtro.IdClasificacionTipoInstrumento });
	    	}
	    	if(pEL.filtro.IdTipoInstrumento){
	    		niv1filters.push({ field: "IdTipoInstrumento", operator: "eq", value: pEL.filtro.IdTipoInstrumento });
	    	}
	    	if(pEL.filtro.IdEstado){
	    		niv1filters.push({ field: "IdEstado", operator: "eq", value: pEL.filtro.IdEstado });
	    	}
	    	
	    	if (niv1filters.length>0) pEL.grilla.EvaluacionLimites.dataSource.filters(objfilters);
	    	else pEL.grilla.EvaluacionLimites.dataSource.filters(new Array());
	    	
	    	pEL.grilla.limiteSel = undefined;
		};
		
		pEL.fn.limpiar = function(){
			pEL.filtro.IdSocioDeNegocio					= 0;
			pEL.filtro.IdTipoLimite						= 0;
			pEL.filtro.IdClasificacionTipoInstrumento	= 0;
			pEL.filtro.IdTipoInstrumento				= 0;
			pEL.filtro.IdEstado							= 0;
			pEL.grilla.EvaluacionLimites.dataSource.removeAll();
			pEL.filtro.IdPortafolio 					= false;
		}
		
	});
}());