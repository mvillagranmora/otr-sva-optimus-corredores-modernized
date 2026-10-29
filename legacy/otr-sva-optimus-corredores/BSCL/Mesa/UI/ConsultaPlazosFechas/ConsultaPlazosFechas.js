(function() {
	angular.module('ModuloMesa').controller('ConsultaPlazosFechasCtrl',
	function($scope,$translate,bcsComunes,$http,bcsServiciosMesa){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var cPLFE 		= this;
		cPLFE.idVentana	= bcsComunes.ventanaWinId($scope);
		cPLFE.win		= {};
		cPLFE.grilla	= {};
		cPLFE.fn		= {};
		cPLFE.filtro	= {};
		cPLFE.cmbbx		= {};
		
		cPLFE.win.DecimalUF = (util.bcs_formato(0,'GEN','MONTO','UF','','')).cantidadDecimales;
		cPLFE.win.DecimalUSD = (util.bcs_formato(0,'GEN','MONTO','USD','','')).cantidadDecimales;
		cPLFE.win.DecimalIVP = (util.bcs_formato(0,'GEN','MONTO','IVP','','')).cantidadDecimales;
		cPLFE.filtro.ValorParametroDef = '60';
		
		cPLFE.datosIndicadores = {};
		cPLFE.datosIndicadores.ValorUF	= 0;
		cPLFE.datosIndicadores.ValorIVP	= 0;
		cPLFE.datosIndicadores.ValorUSD	= 0;
		
		// Parametros entrada del servicio ConsultarPlazosFechas
		var filtroPlazosFechas = {};
		filtroPlazosFechas.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
		filtroPlazosFechas.p_IdUsuario	= $scope.$root.SesionIdUsuario;
		filtroPlazosFechas.p_IdPais		= $scope.$root.SesionIdPaisSistema;
		filtroPlazosFechas.p_CantDias	= Number(cPLFE.filtro.ValorParametro);
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		cPLFE.cmbbx.CantidadDias={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "CANTIDADDIASPLAZO" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_CANTIDAD_DIAS'),
			},
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		cPLFE.grilla.PlazosFechas = {
			dataSource: new bcs.DataSource({
				type: "call",
				definition: {
					serverFiltering : false, // Filtrado de datos en el servidor.
					serverPaging 	: false, // Paginado de los datos en el servidor.
					serverSorting 	: false, // Ordenamiento de los datos en el servidor.
					pageSize 		: 370,
					callParameters	: filtroPlazosFechas,
					callTransaction	: "464",
					callResultSet	: "P_OUT_PLAZOSFECHAS",
					schema: {
						model: {
							fields: {
								Plazo		: { type: "number"},
								Fecha		: { type: "date"},
								DiaSemana	: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "Plazo",	title: $translate.instant('UI_PLAZO_DIAS'),	width: "100px"	},
			    {field: "Fecha",	title: $translate.instant('UI_FECHA'),		width: "100px"	},
			    {field: "DiaSemana",title: $translate.instant('UI_DIA_SEMANA'),	width: "100px"	}
			],
			sortable	: false,
			pageable	: true,
			autoBind	: false,
			height		: 470,
		    selectable 	: "row",
		    excel : {
				allPages: true,
			},
		    filterable	: true
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		var fuente = 'BCS';
		var fechaIndicador = $scope.$root.SesionFechaSistema;
		var p_Indicadores = [{ "IdParametroFinanciero": 'UF', "FechaConsulta": fechaIndicador},
		                     { "IdParametroFinanciero": 'IVP',"FechaConsulta": fechaIndicador},
		                     { "IdParametroFinanciero": 'DO', "FechaConsulta": fechaIndicador}];
		var filtrosValorIndicadores = {};
		filtrosValorIndicadores.p_ParametrosConsulta = p_Indicadores;
		filtrosValorIndicadores.p_out_ParametrosResultado =  {};
		filtrosValorIndicadores.p_out_ErroresDetalle =  {};
		
		bcsServiciosMesa.ObtenerValorIndicador(filtrosValorIndicadores,function (result){
			$.each(result.P_OUT_PARAMETROSRESULTADO,function(key,value){
				if(value.IdParametroFinanciero === 'UF')	cPLFE.datosIndicadores.ValorUF	= value.Valor || 0;
				if(value.IdParametroFinanciero === 'IVP')	cPLFE.datosIndicadores.ValorIVP	= value.Valor || 0;
				if(value.IdParametroFinanciero === 'DO')	cPLFE.datosIndicadores.ValorUSD	= value.Valor || 0;
			});
		}, function(resError){
			bcsComunes.msg("error","UI_CONSULTA_PLAZOS_FECHAS",$translate.instant("UI_REVISE_LOG"));
		});
		
		
		cPLFE.fn.cargar = function(){
			filtroPlazosFechas.p_IdEmpresa	= $scope.$root.SesionIdEmpresa;
			filtroPlazosFechas.p_IdUsuario	= $scope.$root.SesionIdUsuario;
			filtroPlazosFechas.p_IdPais		= $scope.$root.SesionIdPaisSistema;
			filtroPlazosFechas.p_CantDias	= Number(cPLFE.filtro.ValorParametro);
			
			cPLFE.grilla.PlazosFechas.dataSource.setCallParameters(filtroPlazosFechas);
			
		};
		
	});
}());