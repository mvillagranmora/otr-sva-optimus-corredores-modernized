(function() {
	angular.module('ModuloMesa').controller('InformeOperacionesDiariasPactosCtrl',
	function($scope,$modal,$translate,bcsComunes,$timeout,$http,bcsServiciosMesa,bcsAlerta){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var iODP 		= this;
		iODP.idVentana	= bcsComunes.ventanaWinId($scope);
		$scope.data = {};
		iODP.options 	= {};
		iODP.win		= {};
		iODP.grilla		= {};
		iODP.cmbbx		= {};
		iODP.filtro		= {};
		iODP.fn			= {};
		iODP.filtro.Fecha = util.dateInicioDia($scope.SesionFechaSistema);
		iODP.filtro.FechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		iODP.filtro.ValorParametroDef = 'CLIENTE';
		$scope.data.opFechaEstado = true;
		iODP.pactoConfirmado = false;
		var fechaMinima = util.dateInicioDia($scope.SesionFechaSistema);
		iODP.dataTemplate	= {};
		iODP.parametros		= {};
		iODP.options.oConfiguracionFechaDesde = {
				max: fechaMinima
				//min: iODP.filtro.Fecha
		};
		iODP.options.oConfiguracionFechaHasta = {
				
				min: iODP.filtro.Fecha,
				max: fechaMinima//fechaSistema
				
		};
		iODP.contadorGrilla = 0;
		iODP.FiltrosFechas = {
			FechaOpDesde : null,
			FechaOpHasta : null,
			FechaVeDesde : null,
			FechaVeHasta : null,
			FechaAnDesde : null,
			FechaAnHasta : null
		};
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsOperaciones = {
			type: "call",
			definition: {
				//serverFiltering : true, // Filtrado de datos en el servidor.
                //serverPaging    : true, // Paginado de los datos en el servidor.
                //serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 100,
                callTransaction	: "4290692",
				callResultSet	: "P_OUT_OPERACIONES",
				schema: {
					model: {
						fields: {
							IdOperacion		: { type: "number"},
							NombreOperacion	: { type: "string"},
							FolioOperacion	: { type: "number"},
							Mesa			: { type: "string"},
							DocumentoIdentidad:{type: "string"},
							RazonSocial		: { type: "string"},
							CodigoPortafolio: { type: "string"},
							Tasa			: { type: "number"},
							CatIFRS			: { type: "string"},
							IdTipoReajuste	: { type: "string"},
							FechaVencimiento: { type: "string"},
							Agente			: { type: "string"},
							Sucursal		: { type: "string"},
							MontoCompromiso	: { type: "number"},
							MontoOperacion	: { type: "number"},
							MontoCompraProximoCartera	: { type: "number"},
							FechaOperacion	: { type: "string"},
							ValorCompromiso	: { type: "number"},
							ValorOperacion  : { type: "number"},
							ValorCompraProximoCartera   : { type: "number"},
							IdEstado		: { type: "string"},
							FechaAnticipo   : { type: "string"},
							Liquidado		: { type: "string"},
							DateVencimiento : {type: "string"},
							DateLiquidacion : {type: "string"},
							ValorCompraCartera	: { type: "number"},
							ValorInteresCartera	: { type: "number"},
							ValorReajusteCartera: { type: "number"},
							ValorCompraProximoCartera: { type: "number"},
							PlazoEnDias:{type: "number"},
							RelacionCorredora:{type:"string"},
							ConfirmaAdemdum:{type:"string"},
							FechaRespaldo:{type:"string"},
						}
					}
				},
				aggregate: [
		            { field: "MontoCompromiso", aggregate: "sum"},
		            { field: "MontoOperacion", aggregate: "sum"},
		            { field: "MontoCompraProximoCartera", aggregate: "sum"},
		            { field: "ValorCompromiso", aggregate: "sum"},
		            { field: "ValorOperacion", aggregate: "sum"},
		            { field: "ValorCompraProximoCartera", aggregate: "sum"},
		            { field: "ValorInteresCartera", aggregate: "sum"},
		            { field: "ValorReajusteCartera", aggregate: "sum"}
		        ]					
			}
		};		
		
		
		var columnasOperaciones = function() {			
			
			return  [		
				{field: "NombreOperacion",	title: $translate.instant('UI_TIPO_COMPROBANTE'),	width: "100px", groupable: true },
			    {field: "FolioOperacion",	title: $translate.instant('UI_FOLIO_OPERACION'),	width: "80px", groupable: true },
			    {field: "Mesa",				title: $translate.instant('UI_MESA'),				width: "150px", groupable: true },	
			    {field: "IdEstado",        	title: $translate.instant('UI_ESTADO'), 			width: "100px", groupable: true },
			    {field: "FechaOperacion",	title: $translate.instant('UI_FECHA_OPERACION'),	width: "80px", groupable: true },
			    {field: "DocumentoIdentidad",title:$translate.instant('UI_RUT_CLIENTE'),		width: "80px", groupable: true },
			    {field: "RazonSocial",		title: $translate.instant('UI_NOMBRE_CLIENTE'),		width: "120px", groupable: true },
			    {field: "CodigoPortafolio",	title: $translate.instant('UI_CODIGO_PORTAFOLIO'),	width: "90px", groupable: true },
			    {field: "Tasa",				title: $translate.instant('UI_TASA'),				width: "60px", groupable: true, template: "<label> #: (util.bcs_formato(Tasa,'GEN','TASA',IdTipoReajuste,(IdTipoOperacion == 'VENPAC' || IdTipoOperacion == 'COMPAC' ? 'PAC' : ''),'')).valor #</label>"},
			    {field: "IdTipoReajuste",	title: $translate.instant('UI_TIPO_REAJUSTE'),		width: "80px", groupable: true },
			    {field: "FechaVencimiento",	title: $translate.instant('UI_FECHA_VENCIMIENTO'),	width: "80px", groupable: true },
			    {field: "Agente",			title: $translate.instant('UI_NOMBRE_AGENTE'),		width: "170px", groupable: true },
			    {field: "Sucursal",			title: $translate.instant('UI_SUCURSAL'),			width: "120px", groupable: true },
			    {field: "MontoCompromiso",	title: $translate.instant('UI_MONTO_COMPROMISO'),	width: "90px", template: "<label> #: (util.bcs_formato(MontoCompromiso,'GEN','MONTO','CLP','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},	
			    {field: "MontoOperacion",	title: $translate.instant("UI_MONTO_OPERACION"),	width: "90px", template: "<label> #: (util.bcs_formato(MontoOperacion,'GEN','MONTO','CLP','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "MontoCompraProximoCartera", title: $translate.instant('UI_MONTO_PROXIMO'),	width: "90px", template: "<label> #: (util.bcs_formato(MontoCompraProximoCartera,'GEN','MONTO','CLP','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "ValorCompromiso",	title: $translate.instant('UI_VALOR_COMPROMISO'),	width: "90px", template: "<label> #: (util.bcs_formato(ValorCompromiso,'GEN','MONTO',IdTipoReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "ValorOperacion",	title: $translate.instant('UI_VALOR_OPERACION'),	width: "90px", template: "<label> #: (util.bcs_formato(ValorOperacion,'GEN','MONTO',IdTipoReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "ValorCompraProximoCartera", title: $translate.instant('UI_VALOR_PROXIMO'),	width: "90px", template: "<label> #: (util.bcs_formato(ValorCompraProximoCartera,'GEN','MONTO',IdTipoReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "ValorInteresCartera",	title: $translate.instant('UI_INTERES'),		width: "90px", template: "<label> #: (util.bcs_formato(ValorInteresCartera,'GEN','MONTO',IdTipoReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "ValorReajusteCartera",	title: $translate.instant('UI_REAJUSTE'),		width: "90px", template: "<label> #: (util.bcs_formato(ValorReajusteCartera,'GEN','MONTO',IdTipoReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate:" #=(util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerTemplate:" #: (util.bcs_formato(sum,'GEN','MONTO','CLP', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}},
			    {field: "PlazoEnDias",			title: $translate.instant('UI_PLAZO'),			width: "100px", groupable: true },
			    {field: "RelacionCorredora",	title: $translate.instant('UI_RELACION_CORREDORA'),	width: "100px", groupable: true },
			    {field: "ConfirmaAdemdum",	title: $translate.instant('UI_CONFIRMA_PACTO'),	width: "100px", groupable: true }
			];
		};
		
		iODP.dsOperacionesDetalle = function(){
			return{
				type: "call",
				definition: {			
					pageSize: 10,
					callParameters      : '',
					callTransaction	: "4290692",
					callResultSet	: "P_OUT_DETALLEOPERACIONES",
					
					schema: {
						model: {
							fields: {
								Nemotecnico				: { type: "string"},
								IdEmisor				: { type: "string"},
								IdTipoReajuste			: { type: "string"},
								Mercado 				: { type: "string"},
								Tasa 					: { type: "number"},
								IndicadorCobertura 		: { type: "string"},
								FechaVencimientoInstrumentoCadena : { type: "string"},
								Cantidad				:{type: "number"},
								MontoCompromisoDetalle 	: {type:"number"},
								MontoSubyacente 		: {type:"number"},
								FolioCartera			: {type: "number"},
								FlagDeterioro			: {type: "string"},
								PorcentajeDeterioro 	: {type: "number"},
								Deterioro				: {type: "number"},
								Fase					: {type: "number"},
								IdMonedaInstrumento		: { type: "string"},
							}
						}
					}
				}
			}
		};
		
		iODP.columnasOperacionesDetalle = function() {
			return [
				{field: "FolioCartera",			title: $translate.instant('UI_FOLIO_CARTERA'),	width: "40px", groupable: true },
				{field: "Nemotecnico",			title: $translate.instant('UI_NEMO'),	width: "50px"	},
				{field: "IdEmisor",				title: $translate.instant('UI_EMISOR'),	width: "40px"	},
				{field: "IdTipoReajuste",		title: $translate.instant('UI_TIPO_REAJUSTE'),				width: "30px"	},	
				{field: "Mercado",				title: $translate.instant('UI_MERCADO'),	width: "30px"	},
				{field: "IndicadorCobertura",	title: $translate.instant('UI_INDICADOR_COBERTURA'),	width: "30px"	},
				{field: "Tasa",					title: $translate.instant('UI_TASA_CARTERA_PACTO'),	width: "30px"	},
				{field: "FechaVencimientoInstrumentoCadena", title: $translate.instant('UI_FECHA_VENCIMIENTO'),	width: "40px"	},
				{field: "Cantidad",				title: $translate.instant('UI_CANTIDAD'),	width: "50px",	template: "<label> #: (util.bcs_formato(Cantidad,'GEN','CANTIDAD',IdTipoReajuste,'','')).valor #</label>"	},
				{field: "MontoCompromisoDetalle", title: $translate.instant('UI_MONTO_FINAL'),	width: "50px",	template: "<label> #: (util.bcs_formato(MontoCompromisoDetalle,'GEN','Monto',IdTipoReajuste==='UF'?'CLP':IdTipoReajuste,'','')).valor #</label>"	},
				{field: "MontoSubyacente",		title: $translate.instant('UI_MONTO_INICIAL'),	width: "50px",	template: "<label> #: (util.bcs_formato(MontoSubyacente,'GEN','Monto',IdTipoReajuste==='UF'?'CLP':IdTipoReajuste,'','')).valor #</label>"	},
				{field: "FlagDeterioro", 					title: 'UI_FLAG_DETERIORO', 				width: "100px", groupable: true, attributes:{style:"white-space:nowrap;"}},
				{field: "PorcentajeDeterioro", 			title: 'UI_PORCENTAJE_DETERIORO', 			width: "100px", groupable: true, attributes:{style:"white-space:nowrap;"}},
				{field: "Deterioro", 						title: 'UI_DETERIORO', 						width: "100px", template : " <label> #:  (util.bcs_formato(Deterioro,'GEN', 'Monto', IdMonedaInstrumento,'','')).valor #</label>" },
				{field: "Fase", 							title: 'UI_FASE',	 						width: "100px", groupable: true, attributes:{style:"white-space:nowrap;"}}
			];
		};
		
		var ConsultaTemplate = new bcs.DataSource({
			type 		: "odata",
			definition 	: {
				serverFiltering	: true,
				transport		: {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=450",
						dataType : "json",
					}
				},
				schema : {
					model : {
						fields : {
							IdTemplateMesa	: {type: "string"},
							IdReporte		: {type: "string"},
							NombreReporte	: {type: "string"},
							TipoReporte		: {type: "string"},
							ServicioReporte	: {type: "string"},
							IdEmpresa		: {type: "number"},
							IdEstado		: {type: "string"}
						}
					}
				}
			},
		});
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		/*iODP.cmbbx.Corredora={
			filtros : { // Si es combobox 'combobox-corredora' solo usar filtro IdEstado
				Corredora	: { field: "Corredora",	operator: "eq", value: "S" },
				IdEmpresa	: { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstado",	operator: "eq", value: "ACT" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_CORREDORA'),
			},
		};*/
		
		iODP.cmbbx.Mesa={
			filtros : {
				IdEstado		: { "field": "IdEstadoRegistro","operator": "eq", "value" : "ACT" },
			    IdTipoProducto	: { "field": "IdTipoProducto",	"operator": "eq", "value" : "MERF" }
			}
		};
		
		iODP.cmbbx.OrdenarOperaciones={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "ORDENAROPERACIONES" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_ORDENAR_POR'),
			},
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		var dsGrillaOperaciones = new bcs.DataSource(dsOperaciones);
		iODP.grilla.OperacionesDiariasPactos = {
			dataSource: dsGrillaOperaciones,
			columns   : columnasOperaciones(),			
			sortable		: true,
			pageable		: true,
			filterable		: true,
			userSettings 	: true,
			navigatable 	: true,
			excel 			: { allPages: true },
			height			: 480,			
			selectable 		: "multiple row",
			navigatable     : true,
			allowCopy 		: true
		};
		
		
		iODP.grilla.OperacionesDiariasPactosDetalle = function(dataItem) {

			var dsDef = iODP.dsOperacionesDetalle();

			dsDef.definition.callParameters = iODP.fn.preparaParametros(dataItem.FolioOperacion);

			return {
				dataSource: new bcs.DataSource(dsDef),
				columns   : iODP.columnasOperacionesDetalle(),			
				sortable		: true,
				pageable		: true,
				filterable		: true,
				navigatable 	: true,
				userSettings 	: true,
				height			: 200,
				width			: 80,
				excel 			: { allPages: true },
				scrollable		: true,
				selectable 		: "row",
				autoBind 		: true
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		iODP.fn.buscar = function(){
			
		iODP.FiltrosFechas = {
			FechaOpDesde : null,
			FechaOpHasta : null,
			FechaVeDesde : null,
			FechaVeHasta : null,
			FechaAnDesde : null,
			FechaAnHasta : null
		};
			var niv1filters = new Array();
			if(!iODP.filtro.HistoricoOperaciones && (!iODP.filtro.Fecha || iODP.filtro.Fecha === '' || !iODP.filtro.FechaHasta || iODP.filtro.FechaHasta === '')){
				bcsComunes.msg("error",$translate.instant("UI_INFORME_OPERACIONES_DIARIAS_PACTOS"),'Debe ingresar valor en filtros de Fecha.');
				return;
			}
			
			niv1filters.push({ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa });
			
            if($scope.data.carterasVigentes){
				
				//if(iODP.filtro.IdEstado){
					niv1filters.push({ field: "IdEstado", operator: "eq", value: 'VIG' });
					//niv1filters.push({ field: "FechaOperacion", operator: "lte", value: '$scope.$root.SesionFechaSistema' });
					iODP.FiltrosFechas.FechaOpHasta = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
				//}
				
				if(iODP.filtro.IdMesa){				
					niv1filters.push({ field: "IdMesaOrigen", operator: "eq", value: iODP.filtro.IdMesa });
					niv1filters.push({ field: "IdTipoProducto", operator: "eq", value: iODP.dataFiltros.MesaObj.IdTipoProducto });
				}
			}
            
            if($scope.data.opDia){
				if(iODP.filtro.Fecha){
					iODP.FiltrosFechas.FechaOpDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
				}
				if(iODP.filtro.FechaHasta){
					iODP.FiltrosFechas.FechaOpHasta = iODP.filtro.FechaHasta;
				}
				if(iODP.filtro.IdMesa){				
					niv1filters.push({ field: "IdMesaOrigen", operator: "eq", value: iODP.filtro.IdMesa });
					niv1filters.push({ field: "IdTipoProducto", operator: "eq", value: iODP.dataFiltros.MesaObj.IdTipoProducto });
				}
			}

			if($scope.data.opFechaEstado){
				if(iODP.filtro.IdMesa){				
					niv1filters.push({ field: "IdMesaOrigen", operator: "eq", value: iODP.filtro.IdMesa });
					niv1filters.push({ field: "IdTipoProducto", operator: "eq", value: iODP.dataFiltros.MesaObj.IdTipoProducto });
				}

				if(iODP.filtro.IdEstado == 0) {
					iODP.FiltrosFechas.FechaOpDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
					iODP.FiltrosFechas.FechaOpHasta = iODP.filtro.FechaHasta;
				}

				if(iODP.filtro.IdEstado){
					if(iODP.filtro.IdEstado==='ANU'){

						if(iODP.filtro.Fecha){
							iODP.FiltrosFechas.FechaOpDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
						}

						if(iODP.filtro.FechaHasta){
							iODP.FiltrosFechas.FechaOpHasta = iODP.filtro.FechaHasta;
						}
						niv1filters.push({ field: "IdEstado", operator: "eq", value: 'ANU' });
					}
					if(iODP.filtro.IdEstado==='VIG'){	
						iODP.FiltrosFechas.FechaOpDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
						iODP.FiltrosFechas.FechaOpHasta = iODP.filtro.FechaHasta;				
						niv1filters.push({ field: "IdEstado", operator: "eq", value: 'VIG'});
					}

					if(iODP.filtro.IdEstado==='LIQ'){
						if(iODP.filtro.Fecha){
							iODP.FiltrosFechas.FechaAnDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
							iODP.FiltrosFechas.FechaVeDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
						}
						if(iODP.filtro.FechaHasta){
							iODP.FiltrosFechas.FechaAnHasta = iODP.filtro.FechaHasta;
							iODP.FiltrosFechas.FechaVeHasta = iODP.filtro.FechaHasta ;
						}
					
						niv1filters.push({ field: "Liquidado", operator: "eq", value: 'S' });
						niv1filters.push({ field: "IdEstado", operator: "eq", value: 'LIQ' });
						
					}

					if(iODP.filtro.IdEstado==='ANT'){
						iODP.FiltrosFechas.FechaAnDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
						iODP.FiltrosFechas.FechaAnHasta = iODP.filtro.FechaHasta;
						niv1filters.push({ field: "Liquidado", operator: "eq", value: 'N' });
						niv1filters.push({ field: "IdEstado", operator: "eq", value: 'ANT' });
							
					}

					if(iODP.filtro.IdEstado==='EMI'){
						iODP.FiltrosFechas.FechaOpDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
						iODP.FiltrosFechas.FechaOpHasta = iODP.filtro.FechaHasta;
						niv1filters.push({ field: "Liquidado", operator: "eq", value: 'N' });
						niv1filters.push({ field: "IdEstado", operator: "eq", value: 'EMI' });
							
					}

					if(iODP.filtro.IdEstado==='RGN'){
						iODP.FiltrosFechas.FechaOpDesde = moment(moment(iODP.filtro.Fecha).format('YYYY-MM-DD')).toDate();
						iODP.FiltrosFechas.FechaOpHasta = iODP.filtro.FechaHasta;
						niv1filters.push({ field: "Liquidado", operator: "eq", value: 'N' });
						niv1filters.push({ field: "IdEstado", operator: "eq", value: 'RGN' });
							
					}
				}
			}
			
			if(iODP.pactoConfirmado)
				niv1filters.push({ field: "ConfirmaAdemdum",	operator: "eq", value: 'Si' });
			
			dsGrillaOperaciones.setCallParameters(iODP.fn.preparaParametros());
			iODP.grilla.OperacionesDiariasPactos.dataSource.filter(niv1filters);

			iODP.grilla.OperacionesDiariasPactos.dataSource.fetch(function(){
				iODP.contadorGrilla =iODP.grilla.OperacionesDiariasPactos.dataSource.total();
				if(iODP.contadorGrilla > 0  ){
					var campoOrden = iODP.filtro.ValorParametro === 'AGENTE' ? "Agente" : "RazonSocial";
					iODP.grilla.OperacionesDiariasPactos.dataSource.sort({ field: campoOrden, dir: "asc" });

					iODP.parametros.Fecha			= util.dateSPdate(iODP.filtro.Fecha);
					iODP.parametros.FechaHasta		= util.dateSPdate(iODP.filtro.FechaHasta);
					iODP.parametros.IdMesa			= iODP.filtro.IdMesa;
					iODP.parametros.IdTipoProducto	= !iODP.dataFiltros.MesaObj.IdTipoProducto ? '' : iODP.dataFiltros.MesaObj.IdTipoProducto;
					iODP.parametros.OrdenarPor		= iODP.filtro.ValorParametro;
					iODP.win.existenOperaciones 	= true;
					if( (iODP.filtro.IdEstado ==='VIG' || iODP.filtro.IdEstado == 0) && $scope.data.opFechaEstado){
						iODP.win.existenOperacionesVig = true;
					}else{
						iODP.win.existenOperacionesVig=false;
					}
				}else{
					iODP.parametros.Fecha			= undefined;
				    iODP.parametros.FechaHasta		= undefined;
				    iODP.parametros.IdMesa			= undefined;
				    iODP.parametros.IdTipoProducto	= undefined;
				    iODP.parametros.OrdenarPor		= undefined;
					iODP.win.existenOperaciones 	= false;
					iODP.win.existenOperacionesVig 	= false;
					iODP.parametros.FolioOperacion  = undefined;
				}
			});
			iODP.contadorGrilla= 0;
			
		};
		
		
		iODP.fn.limpiar = function(){
			iODP.filtro.Fecha			= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			iODP.filtro.FechaHasta 		= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			//iODP.filtro.IdContraparte	= 0;
			iODP.filtro.IdMesa			= 0;
			iODP.filtro.IdEstado		= 0;
			iODP.filtro.ValorParametro	= 'CLIENTE';
			iODP.filtro.HistoricoOperaciones = false;
			iODP.grilla.OperacionesDiariasPactos.dataSource.removeAll();
			iODP.win.existenOperaciones = false;
			iODP.win.existenOperacionesVig = false;

			iODP.grilla.operacionesDiariasPactosSel = undefined;
		};
		
		iODP.fn.preparaParametros = function(FolioOperacion){
			parametrosConsulta = {};

			parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa;
			parametrosConsulta.p_IdUsuario =$scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo = "CUS";
			parametrosConsulta.p_IdPais = null;
			parametrosConsulta.p_FechaOpDesde = iODP.FiltrosFechas.FechaOpDesde;
			parametrosConsulta.p_FechaOpHasta = iODP.FiltrosFechas.FechaOpHasta;
			parametrosConsulta.p_FechaAnDesde = util.dateInicioDia 	(iODP.FiltrosFechas.FechaAnDesde);
			parametrosConsulta.p_FechaAnHasta = util.dateTerminoDiaLocal (iODP.FiltrosFechas.FechaAnHasta);
			parametrosConsulta.p_FechaVeDesde = util.dateInicioDia 	(iODP.FiltrosFechas.FechaVeDesde);
			parametrosConsulta.p_FechaVeHasta = util.dateTerminoDiaLocal (iODP.FiltrosFechas.FechaVeHasta);
			parametrosConsulta.p_FolioOperacion = FolioOperacion;
			
			return parametrosConsulta;
		};

		
		iODP.fn.generarInforme = function() {
			var subReports = [];
			var template = {};
			iODP.dataTemplate.NombreReporte = 'OPERACIONESDIARIASPACTOS';
			var fechaInicialTotal = "0001-01-01";

			if($scope.data.carterasVigentes){
				iODP.parametros.Fecha = fechaInicialTotal;
				iODP.filtro.IdEstado = "VIG";
			}

			if(!$scope.data.carterasVigentes && moment(iODP.filtro.FechaHasta).format('YYYY-MM-DD') == moment(iODP.filtro.Fecha).format('YYYY-MM-DD')){
				iODP.parametros.Fecha = moment(iODP.filtro.FechaHasta).format('YYYY-MM-DD');
				//iODP.filtro.IdEstado = "VIG";
			}

			var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: iODP.dataTemplate.NombreReporte },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			
			var parametros = { "p_IdEmpresa"		: $scope.$root.SesionIdEmpresa,
					   "p_IdUsuario"		: $scope.$root.SesionIdUsuario,
					   "p_IdModulo"			: 'MRF',
					   "p_FechaDesde"		: iODP.parametros.Fecha,
					   "p_FechaHasta"		: iODP.parametros.FechaHasta,
					   //"p_Corredora"		: iODP.parametros.Corredora,
					   "p_IdMesa"			: iODP.parametros.IdMesa,
					   "p_IdTipoProducto"	: iODP.parametros.IdTipoProducto,
					   "p_OrdenarPor"		: iODP.parametros.OrdenarPor,
					   "p_IdEstado"			: iODP.filtro.IdEstado,
					   "$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" }]};
			var parametrosSub = { 
					   "p_IdEmpresa"		: $scope.$root.SesionIdEmpresa,
					   "p_IdUsuario"		: $scope.$root.SesionIdUsuario,
					   "p_IdModulo"			: 'MRF',
					   "p_FechaDesde"		: iODP.parametros.Fecha,
					   "p_FechaHasta"		: iODP.parametros.FechaHasta
					   };
			bcsComunes.ventanaOverlay($scope.IdVentana,true);
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				var data = ConsultaTemplate.data();
				for(i=0;i<data.length;i++) {
					var dataTem = data[i];
					if (data[i].IdTemplateMesa === 'OPERACIONESDIARIASPACTOS'){
						template = {
								TipoReporte		: data[i].TipoReporte//'pdf' 
								,IdReporte		: data[i].IdReporte
								,NombreReporte	: $translate.instant(data[i].NombreReporte)
								,data			: { "serviceId" 	: data[i].ServicioReporte,
													"serviceData" 	:parametros
													}
													,idReporte		: 'CAR_PRO'
											};
					}else{
						//
						  subReports.push({ "reportName" 	: data[i].IdTemplateMesaSubreporte//'InformeCarteraPropiaGarantiaMesaRF_Detalle'
								              		,"parameters" 	: {} 
								              		,"data" 		: {
													                  "serviceId" : data[i].ServicioReporte,//'1759',
													                  "serviceData" :parametrosSub
													              }
											});
						
					}
				}				
				template.subReports = subReports;
				console.log(template);
				iODP.fn.getReport(template);
			});
			//bcsComunes.ventanaOverlay($scope.IdVentana,false);
		};
		
		iODP.fn.generarCertificado = function() {		
			
			var template = {};
			iODP.dataTemplate.NombreReporte = 'CERTIFICADOOPERACIONESDIARIASPACTOS';
			var filterTemplate = [ { field: "NombreReporte",operator:"eq", value: iODP.dataTemplate.NombreReporte },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
			
			var parametros = { 
						"p_IdEmpresa"		: $scope.$root.SesionIdEmpresa,
					   	"p_IdUsuario"		: $scope.$root.SesionIdUsuario,
					   	"p_IdModulo"		: 'MRF',
					   	"p_FechaDesde"		: iODP.parametros.Fecha,
					   	"p_FechaHasta"		: iODP.parametros.FechaHasta,
					   	"p_IdMesa"			: iODP.parametros.IdMesa,
					   	"p_IdTipoProducto"	: iODP.parametros.IdTipoProducto,
					   	"p_OrdenarPor"		: iODP.parametros.OrdenarPor,
						"$schema"			: [{ "field" : "LogoCorredora", "type" : "blob" },
												{"field" : "FirmaSolicitante", "type": "blob"}]};

			let IdEstado;								
			if (iODP.filtro.IdEstado == 0) IdEstado = ['VIG','ANT','LIQ'];
			else IdEstado = ['VIG'];
			IdEstado = IdEstado.map(( item => {

				let aux = { IdEstado : item }
				return aux;

			}));
			parametros.p_IdEstado = IdEstado;

			if ( iODP.grilla.operacionesDiariasPactosSel != undefined ) {

				let IdClientes = [...new Set(iODP.grilla.operacionesDiariasPactosSel.map( obj => obj.IdCliente ))];
				IdClientes = IdClientes.map(( item => {

					let aux = { IdCliente : item }
					return aux;

				}));
				parametros.p_IdCliente = IdClientes;
				
			}
			
			bcsComunes.ventanaOverlay($scope.IdVentana,true);
			ConsultaTemplate.query({ filter: filterTemplate }).then(function(){
				var data = ConsultaTemplate.data();
				for(i=0;i<data.length;i++) {
					var dataTem = data[i];
					if (data[i].IdTemplateMesa === 'CERTIFICADOOPERACIONESDIARIASPACTOS'){
						template = {
								TipoReporte		: data[i].TipoReporte//'pdf' 
								,IdReporte		: data[i].IdReporte
								,NombreReporte	: $translate.instant(data[i].NombreReporte)
								,data			: { "serviceId" 	: data[i].ServicioReporte,
													"serviceData" 	:parametros
													}
											};
					}
				}				
				console.log(template);
				iODP.fn.getReportCertificado(template);
				return;
			});
			bcsComunes.ventanaOverlay($scope.IdVentana,false);
		}


		iODP.fn.getReportCertificado = function(dataTemplate){
			var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')+"."+dataTemplate.TipoReporte;
			var parameters = {
				"reportId"		: dataTemplate.IdReporte,
				"parameters": [],
				"exportFormat"	: dataTemplate.TipoReporte,
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.NombreReporte,
						"parameters": [],
						"data"		: dataTemplate.data,
						"parameters": {},
					},
				"subReports": []
				},
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					saveAs(file, nombreArchivo);
					bcsComunes.ventanaOverlay($scope.winID,false);
				}).error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_INFORME"));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
			return;
		};

		
		iODP.fn.getReport = function(dataTemplate){
			var parameters = {
				"exportFormat"	: dataTemplate.TipoReporte,
				"reportId"		: dataTemplate.IdReporte,
				"parameters"	: {},
				"configuration"	: {
					"mainReport": {
						"reportName": dataTemplate.NombreReporte,
						"data"		: dataTemplate.data,
						"parameters": {}
					},
					"subReports": dataTemplate.subReports || []
				}
			};
			
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
				.success(function (response) {
					var file = new Blob([response], {type: 'application/pdf'});
					saveAs(file, dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'')+"."+dataTemplate.TipoReporte);
					bcsComunes.ventanaOverlay($scope.winID,false);
				})
				.error(function (response) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_INFORME"));
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
		};
	
	/***********************************
	 *			WATCH                 
	 ***********************************/
		//WATCH

		$scope.$watch('data.carterasVigentes',function(newval,oldval) {
					
			if(newval===true){
				if($scope.data.opFechaEstado===true || $scope.data.opDia===true){
					$scope.data.opFechaEstado=false;
					$scope.data.opDia = false;
					iODP.fn.limpiar();
				}
						
			}
		});

		$scope.$watch('data.opDia',function(newval,oldval) {
			
			if(newval===true){
				if($scope.data.opFechaEstado===true || $scope.data.carterasVigentes===true){
					$scope.data.carterasVigentes=false;
					$scope.data.opFechaEstado = false;
					//iODP.grilla.OperacionesDiariasPactos.showcolumn("IdEstado");
					iODP.fn.limpiar();
				}
					
			}
		});
		$scope.$watch('data.opDia',function(newval,oldval) {
			
			if(newval===false){
				if($scope.data.carterasVigentes===false && $scope.data.opFechaEstado===false){
					$scope.data.carterasVigentes=false;
					$scope.data.opFechaEstado = true;
					iODP.fn.limpiar();
				}
				
			}
		});
		$scope.$watch('data.opFechaEstado',function(newval,oldval) {
			
			if(newval===true){
				if($scope.data.opDia===true || $scope.data.carterasVigentes===true){
					$scope.data.carterasVigentes=false;
					$scope.data.opDia = false;
					iODP.fn.limpiar();
				}
				if(iODP.contadorGrilla > 0  ){
					iODP.fn.limpiar();
				}	
			}
		});
		$scope.$watch('data.opFechaEstado',function(newval,oldval) {
			
			if(newval===false){
				if($scope.data.carterasVigentes===false && $scope.data.opDia===false){
					$scope.data.carterasVigentes=true;
					$scope.data.opDia = false;
					iODP.fn.limpiar();
				}
				iODP.win.existenOperacionesVig = false;
			}
		});

		$scope.$watch('iODP.filtro.Fecha',function(newval,oldval){
			
			iODP.options.oConfiguracionFechaHasta = {
					//max: iODP.filtro.FechaHasta
					min: iODP.filtro.Fecha,
					max: fechaMinima//fechaSistema
					
			};
			if(iODP.filtro.Fecha > iODP.filtro.FechaHasta){
				//iODP.filtro.Fecha = iODP.filtro.FechaHasta
				iODP.filtro.FechaHasta = iODP.filtro.Fecha
			}
		});	
	}) 
}());