angular.module('ModuloCustodia').controller('ConsultarPatrimonioCajaCtrl',
function($rootScope, $scope,$translate,bcsComunes)
{		
	$scope.filtros = {};
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.chkFechaAdquisicion = true;  //búsqueda por defecto por fecha de liquidación
	$scope.data.chkVigente = true;
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oConfiguracionClientePortafolio = {	
		idActual : false,
		seleccionado : -1,
		idDefecto : -1
	};
	
	var dsPatrimonio = new bcs.DataSource({
		type : "odata",
		definition	: {
			serverFiltering : true, 
			serverPaging : true, 
			serverSorting : true, 
			pageSize : 200,
			transport: {
				read: {
					url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=601219",
					dataType: "json"
				}
			},
			schema : {
				model : {
					fields : {
						IdCliente 							: { type: "number"},
						IdPortafolio 						: { type: "number"},
						FechaPatrimonio						: { type: "sysdate"},
						TipoFechaConsulta					: { type: "string"},
						DocumentoIdentidad					: { type: "string"},
						NombreCompleto						: { type: "string"},
						TipoAhorro							: { type: "string"},
						IdMatrizAgrupadorAPV				: { type: "number"},
						CodigoPortafolio					: { type: "string"},
						Proposito							: { type: "string"},
						PlanAPV								: { type: "string"},
						CategoriaAPV         				: { type: "string"},
						IdRegimenTributario					: { type: "string"},
						Fondo								: { type: "string"},
						Serie								: { type: "string"},
						IdInstrumento						: { type: "number"},
						Nemotecnico							: { type: "string"},
						IdClasificacionTipoInstrumento		: { type: "string"},
						CantidadPatrimonio					: { type: "number"},
						PrecioMercadoCustodia				: { type: "number"},
						ValorCuota							: { type: "number"},
						MontoPatrimonio						: { type: "number"},
						IdMoneda							: { type: "string"},
						IdMonedaSistema						: { type: "string"},
						Vigencia							: { type: "string"},
						MontoPatrimonioAfectoComision		: { type: "number"},
						PorcentajeCobroComision				: { type: "number"},
						MontoComision						: { type: "number"},
						IdEstadoComision					: { type: "string"},
						NombreEstadoComision				: { type: "string"}
					}
				}
			}
		}
	});
	
	
	$scope.columnas = function() {
		return  [
	         {field: "FechaPatrimonio",					title: 'UI_FECHA_PATRIMONIO',						width: "100px", groupable: true	}
	        ,{field: "DocumentoIdentidad",				title: 'UI_DOCUMENTO_IDENTIDAD',			        width: "150px", groupable: true	}
	        ,{field: "NombreCompleto",					title: 'UI_NOMBRE',									width: "100px", groupable: true }
	        ,{field: "TipoAhorro",						title: 'UI_TIPO_AHORRO', 							width: "100px", groupable: true }
	        ,{field: "IdMatrizAgrupadorAPV",			title: 'UI_MATRIZ_AGRUPADOR',						width: "150px", groupable: true	}
	        ,{field: "CodigoPortafolio",				title: 'UI_CODIGO_PORTAFOLIO',						width: "100px", groupable: true	}
			,{field: "Proposito",						title: 'UI_PROPOSITO',								width: "100px",	groupable: true	}
			,{field: "PlanAPV",							title: 'UI_PLAN_APV',								width: "100px", groupable: true	}
			,{field: "CategoriaAPV",					title: 'UI_CATEGORIA_APV',							width: "100px", groupable: true	}
			,{field: "IdClasificacionTipoInstrumento",	title: 'UI_CLASIFICACION',							width: "60px", 	groupable: true }
			,{field: "MontoPatrimonio", 				title: 'UI_MONTO_PATRIMONIO', 						width: "150px", template : " <label> #: util.bcs_formato(MontoPatrimonio,'GEN','Monto',IdMonedaSistema,'','').valor #</label>"}
			,{field: "IdMoneda", 						title: 'UI_MONEDA', 								width: "60px",	groupable: true	}
			,{field: "IdMonedaSistema",					title: 'UI_MONEDA_SISTEMA',							width: "150px", groupable: true	}
			,{field: "Vigencia",						title: 'UI_VIGENCIA',								width: "150px", groupable: true	}
			,{field: "MontoPatrimonioAfectoComision", 	title: 'UI_MONTO_PATRIMONIO_AFECTO_COMISION', 		width: "150px", template : " <label> #: util.bcs_formato(MontoPatrimonioAfectoComision,'GEN','Monto',IdMonedaSistema,'','').valor #</label>"}
			,{field: "PorcentajeCobroComision", 		title: 'UI_PORCENTAJE_COBRO', 						width: "150px", template : " <label> #: util.bcs_formato(PorcentajeCobroComision,'GEN','Tasa',IdMonedaSistema,'','').valor #</label>"}
			,{field: "MontoComision", 					title: 'UI_MONTO_COMISION', 						width: "150px", template : " <label> #: util.bcs_formato(MontoComision,'GEN','Monto',IdMonedaSistema,'','').valor #</label>"}
			,{field: "NombreEstadoComision",			title: 'UI_ESTADO_COBRO_COMISION',					width: "150px", groupable: true	}
		];
	};
	
   	//GRILLA PRINCIPAL
	var grillaPatrimonio;
	
	$scope.confGrillaPatrimonioCaja = {
		dataSource	: dsPatrimonio,
		columns		: $scope.columnas(),
		sortable	: true,
		pageable	: true,   //pagina localmente
		filterable 	: true,
		selectable 	: "none",
		reorderable	: false,
		userSettings : true,
		groupable	: true,
		height		: 562,
		allowCopy	: { delimeter: "\t" },
		resizable	: true,
	    autoBind	: false,
	    hideGroupingHeader: true,
	    excel : {
	        allPages: true,
	      },
	    gridCreated : function(grid) {
	    	grillaPatrimonio = grid;
	    },
	    scrollable	: true
	};
   		
	$scope.buscar = function (){
		var filtro = [];
		if($scope.data.oConfiguracionClientePortafolio.idActual){
			filtro.push({ field: "IdPortafolio", operator: "eq", value: $scope.data.oConfiguracionClientePortafolio.idActual});
		}
		if($scope.data.chkFechaLiquidacion){
			filtro.push({ field: "TipoFechaConsulta", operator: "eq", value: 'LIQ'});
		}
		if($scope.data.chkFechaAdquisicion){
			filtro.push({ field: "TipoFechaConsulta", operator: "eq", value: 'ADQ'});
		}
		if($scope.data.chkVigente){
			filtro.push({ field: "Vigencia", operator: "eq", value: 'V'});
		}
		if($scope.data.kngFecha){
			filtro.push({ field: "FechaPatrimonio", operator: "gte", value: util.dateOdata(util.dateInicioDia($scope.data.kngFecha))});
			filtro.push({ field: "FechaPatrimonio", operator: "lte", value: util.dateOdata(util.dateTerminoDia($scope.data.kngFecha))});
		}
		$scope.confGrillaPatrimonioCaja.dataSource.filter(filtro);
	};
	
	$scope.limpiarFiltros = function() {
		$scope.data.oConfiguracionClientePortafolio.idActual = false;
		$scope.data.oConfiguracionClientePortafolio.seleccionado = false;
						
		$scope.data.kngFecha =  util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.chkFechaAdquisicion = true;

	}
	
	$scope.$watch('data.chkFechaLiquidacion',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.data.chkFechaAdquisicion = false;  //desclikea check fecha adquisicion
				break;
			case false: 
				$scope.data.chkFechaAdquisicion = true;				
				break;
		}				
	});
	
	$scope.$watch('data.chkFechaAdquisicion',function(newval,oldval){
		switch (newval){
			case true:  
				$scope.data.chkFechaLiquidacion = false; //desclikea check fecha liquidacion				
				break;
				
			case false: 
				$scope.data.chkFechaLiquidacion = true;
				break;  
		}
	});
});