angular.module('ModuloCustodia').controller('ConsultarCustodiaGeneralCtrl',
		function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServicioPlataforma)
{	
	
 
	$scope.filtros = {};
	$scope.data ={};

	var parametrosConsulta = {};
	var idVentana  = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	//Valores por defecto
	$scope.data.kngFechaConsulta 		=  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.FechaConsultaOpt 		= {};
	$scope.data.chkAgrupaPorBoveda 		= true;	
	$scope.data.chkAgrupaPorRelacion 	= true;	
	$scope.data.chkFechaAdquisicion 	= true;  //búsqueda por defecto por fecha de liquidación

	//-------------------------------------
	// CONFIGURACION DE COMBOBOX
	//-------------------------------------	
	$scope.data.oConfiguracionClasificacion={
			tipo 			: "combobox-clasificacion-tipo-instrumento",
			configuracion 	: {},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro 	: false
	};	


	$scope.data.oConfiguracionBoveda={
			tipo 			: "combobox-boveda",
			filtros 		: {
				IdEstado 			: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" }
			  },			
			configuracion 	: {},
			idActual 		: false,
			seleccionado 	: false,
			activado 		: true,
			idDefecto 		: '',
			idPostFiltro 	: false
	};	
	


	
	$scope.data.oConfiguracionTipoInstrumento={
			tipo 			: "combobox-tipo-instrumento",
			configuracion 	: {},
			filtros 		: {
								IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : parametros.IdClasificacionTipoInstrumento },
								IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
								IdTipoInstrumento				: { "field" : "IdTipoInstrumento",                       "operator": "neq", "value" : "FWD" },
								NoDerivados						: { "field" : "CodigoTipoInstrumento",              "operator": "neq", "value" : "DER" }
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro 	: false		 
	};
	
	
	$scope.data.oConfiguracionInstrumento={
			buscar          : "-1"    //para limpiar combo
	};	
	
	
	
	//-------------------------------------
	// CONFIGURACION DE GILLAS
	//-------------------------------------	
	var dsCustodiaGeneral = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
				pageSize		: 200,
				callTransaction : "1762",
				callResultSet   : "P_OUT_TBL",
				schema 			: {
					model : {
						fields : {                                                                                                                                                             
							IdClasificacionTipoInstrumento 		: { type: "string"},
							NombreClasificacionTipoInstrumento 	: { type: "string"},
							IdTipoInstrumento					: { type: "string"},
							NombreTipoInstrumento				: { type: "string"},
							IdInstrumento						: { type: "number"},
							Nemotecnico							: { type: "string"},
							ISIN								: { type: "string"},
							IdEmisor							: { type: "string"},
							IdBoveda							: { type: "string"},
							NumeroCuentaBoveda					: { type: "string"},
							Relacion							: { type: "string"},
							CantidadCustodia					: { type: "number"},
							CantidadSimulCom					: { type: "number"},
							CantidadSimulVen					: { type: "number"},
							CantidadPrestamo					: { type: "number"},
							CantidadEmpresti					: { type: "number"},
							CantidadGarantia					: { type: "number"},
							CantidadTransIng					: { type: "number"},
							CantidadTransEgr					: { type: "number"},
							TotalPosicion   					: { type: "number"},
							MontoValorizado   					: { type: "number"},
							CantidadPactoVenta   				: { type: "number"},
							CantidadPactoCompra   				: { type: "number"}
						}
					}
				},	                       
				aggregate	: [
					{ field: "TotalPosicion", aggregate: "sum" },
					{ field: "CantidadCustodia", aggregate: "sum" },
					{ field: "MontoValorizado", aggregate: "sum" }
				]	                               				
		}
	});
	
	
	$scope.columnasCustodiaGeneral = function() {
		return  [
	    		 {field: "IdClasificacionTipoInstrumento",	title: 'UI_MERCADO',				width: "60px",	groupable: true}
	    		,{field: "IdTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO',		width: "60px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "Nemotecnico",						title: 'UI_INSTRUMENTO',			width: "100px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "ISIN", 							title: 'UI_ISIN', 					width: "100px",	groupable: true,	attributes:{style:"white-space:nowrap;"},	hidden: true}
	    		,{field: "IdEmisor",						title: 'UI_EMISOR',					width: "80px",	groupable: true,	attributes:{style:"white-space:nowrap;"}}
	    		,{field: "IdBoveda",						title: 'UI_BOVEDA',					width: "60px",	groupable: true}	    		
	    		,{field: "NumeroCuentaBoveda", 				title: 'UI_NUMERO_CUENTA', 			width: "55px", 	groupable: true,	attributes:{style:"white-space:nowrap;"},	hidden: true}
	    		,{field: "Relacion",						title: 'UI_RELACION',				width: "60px",	groupable: true}	    		
	    		,{field: "TotalPosicion",					title: 'UI_TOTAL',			width: "100px", filterable:false, template : " <label> #: util.bcs_formato(TotalPosicion,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'').valor #</label>", groupable: false,aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#" , headerTemplate: '<span title="'+$translate.instant('UI_TOTAL_CUSTODIA')+'">'+$translate.instant('UI_TOTAL')+'</span>' }
	    		,{field: "CantidadCustodia",				title: 'UI_CUSTODIA',				width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadCustodia,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'').valor #</label>", groupable: false,aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#", headerTemplate: '<span title="'+$translate.instant('UI_CUSTODIA_DISPONIBLE')+'">'+$translate.instant('UI_CUSTODIA')+'</span>'}	    		
	    		,{field: "MontoValorizado",					title: 'UI_MONTO_VALORIZADO_ACTUAL',width: "100px", filterable:false, template : " <label> #: util.bcs_formato(MontoValorizado,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'').valor #</label>", groupable: false,aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','"+  $rootScope.SesionIdMonedaSistema +"','','')).valor#"}
	    		,{field: "CantidadGarantia",				title: 'UI_GARANTIAS',				width: "100px", filterable:false, template : " <label> #:  util.bcs_formato(CantidadGarantia,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadSimulCom",				title: 'UI_SIMULTANEA_COMPRAS',		width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadSimulCom,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadSimulVen",				title: 'UI_SIMULTANEA_VENTAS',		width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadSimulVen,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadPrestamo",				title: 'UI_PRESTAMOS',				width: "100px", filterable:false, template : " <label> #:  util.bcs_formato(CantidadPrestamo*-1,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadEmpresti",				title: 'UI_EMPRESTITO',				width: "100px", filterable:false, template : " <label> #:  util.bcs_formato(CantidadEmpresti,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadTransIng",				title: 'UI_INGRESO_TRANSITO',		width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadTransIng,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadTransEgr",				title: 'UI_EGRESOS_TRANSITO',		width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadTransEgr,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadPactoVenta",				title: 'UI_VENTAS_CON_PACTO',		width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadPactoVenta,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
	    		,{field: "CantidadPactoCompra",				title: 'UI_COMPRAS_CON_PACTO',		width: "100px", filterable:false, template : " <label> #: util.bcs_formato(CantidadPactoCompra,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','').valor #</label>", groupable: false}
		];
	};
	
	$scope.confGrillaCustodiaGeneral = {
		dataSource		: dsCustodiaGeneral,
		columns			: $scope.columnasCustodiaGeneral(),		
		sortable		: true,
		pageable		: true,
		filterable 		: true,
		selectable 		: "row",
		reorderable		: true,
		userSettings 	: true,	
		groupable		: true,
		height			: 514,
		allowCopy		: { delimeter: "\t" },
		resizable		: true,		
	    autoBind		: false,	    
	    scrollable		: true,
		excel : {
	        allPages: true,
	    },
	    gridCreated 	: function(grid) {
	    						grillaCustodiaGeneral = grid;
	    						if ($scope.SesionLocale === 'PE'){
									grid.showColumn("ISIN");
									grid.showColumn("NumeroCuentaBoveda");
								}
						    },
	};


 
	
	//-------------------------------------
	// FUNCIONES
	//-------------------------------------
	
	$scope.buscar = function ()
	{	
		setTimeout(function(){
			var parametrosConsulta = {};		
			parametrosConsulta.p_IdEmpresa 	= $scope.SesionIdEmpresa;
			parametrosConsulta.p_IdUsuario 	= $scope.SesionIdUsuario;
			parametrosConsulta.p_IdModulo  	= "CUS";
			parametrosConsulta.p_IdClasificacionTipoInstrumento = $scope.data.oConfiguracionClasificacion.seleccionado.IdClasificacionTipoInstrumento || '';
			parametrosConsulta.p_IdTipoInstrumento 				= $scope.data.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || '';						
			parametrosConsulta.p_IdInstrumento 					= ($scope.data.instrumentoDatos === undefined) ? 0 : ($scope.data.instrumentoDatos.ConteoNemotecnico === undefined || $scope.data.instrumentoDatos.ConteoNemotecnico === 1) ? $scope.data.instrumentoDatos.IdInstrumento : 0;
			parametrosConsulta.p_Nemotecnico 					= ($scope.data.instrumentoDatos === undefined) ? '' :  ($scope.data.instrumentoDatos.ConteoNemotecnico > 1) ? $scope.data.instrumentoDatos.Nemotecnico : '';			
			parametrosConsulta.p_IdBoveda 						= $scope.data.oConfiguracionBoveda.seleccionado.IdBoveda || '';			
			parametrosConsulta.p_FechaConsulta 					= util.dateSPdate($scope.data.kngFechaConsulta);
						
			if ($scope.data.chkAgrupaPorBoveda)
			{
				parametrosConsulta.p_AgrupaPorBoveda = "S";
				grillaCustodiaGeneral.showColumn("IdBoveda");  //muestra columna Boveda
				if ($scope.SesionLocale === 'PE')
					grillaCustodiaGeneral.showColumn("NumeroCuentaBoveda");
			} 
			else 
			{
				parametrosConsulta.p_AgrupaPorBoveda = "N";
				grillaCustodiaGeneral.hideColumn("IdBoveda");  //oculta columna Boveda
				if ($scope.SesionLocale === 'PE')
					grillaCustodiaGeneral.hideColumn("NumeroCuentaBoveda");
			}
			

			if ($scope.data.chkAgrupaPorRelacion)
			{
				parametrosConsulta.p_AgrupaPorRelacion = "S";
				grillaCustodiaGeneral.showColumn("Relacion");  //muestra columna Relacion
			} 
			else 
			{
				parametrosConsulta.p_AgrupaPorRelacion = "N";
				grillaCustodiaGeneral.hideColumn("Relacion");  //oculta columna Relacion
			}
			
			if ($scope.data.chkFechaLiquidacion)
			{
				parametrosConsulta.p_TipoFechaConsulta = "LIQ";   // Filtrar por Fecha Liquidación
			}
			else if ($scope.data.chkFechaAdquisicion)
			{
				parametrosConsulta.p_TipoFechaConsulta = "ADQ";   // Filtrar por Fecha Adquisición
			}
			
			 
			
			dsCustodiaGeneral.setCallParameters(parametrosConsulta);
			

			
		}, 400);  //se le da tiempo, para que combo instrumento alcance a actualizarse
		
		
		setTimeout(function(){
			// Se deja seleccionado el primer registro de la grilla para que al hacer doble clic tome el selecionado (quitar cuando la grilla nueva tenga esta propiedad)
			grillaCustodiaGeneral.select(grillaCustodiaGeneral.tbody.find(">tr"));
		}, 1200);
	};
	

	

	//Abre CustodiaPortafolio para listar todos los clientes que tienen custodia del instrumento seleccionado
	$scope.verCustodiaPortafolio = function(seleccionCustodiaGeneral)
	{	
		var objParametros = {};
		
		objParametros.IdClasificacionTipoInstrumento 	= seleccionCustodiaGeneral.IdClasificacionTipoInstrumento;		
		objParametros.IdTipoInstrumento 				= seleccionCustodiaGeneral.IdTipoInstrumento;
		objParametros.IdInstrumento 					= seleccionCustodiaGeneral.IdInstrumento;		
		objParametros.Nemotecnico 					    = seleccionCustodiaGeneral.Nemotecnico;
		objParametros.FechaConsulta                     = util.dateSPdate($scope.data.kngFechaConsulta);
		objParametros.IdCliente    						= 0; 
		objParametros.IdPortafolio 						= 0;
		objParametros.IdBoveda 							= seleccionCustodiaGeneral.IdBoveda;
		objParametros.agrupaPorBoveda 					= $scope.data.chkAgrupaPorBoveda ? "S" : "N";
		objParametros.TipoFechaConsulta					= $scope.data.chkFechaLiquidacion ? "LIQ" : "ADQ";
		objParametros.btnCustPort 					    = true;
		objParametros.esVentana 					    = true;
		 
		bcsComunes.ventanaAbrir("ConsultarCustodiaPortafolio", objParametros,false,false, $scope.idVentana);
		
		
		//Funcion en blotter padre(para traspasar datos entre controladores de tabs)
		//$scope.blotterPadre.data.buscarCustodiaPortafolio(objParametros);  //abre en tab "Custodia"
		//bcsComunes.ventanaAbrir("ConsultarCustodiaPortafolio", objParametros, false, false, idVentana); // Abre en ventana nueva
		
		//$scope.blotterPadre.data.cambiaTab("tabCustodiaPortafolio"); //activa tab de custodia por portafolio
		
		
	};

	

	
	
	$scope.limpiarFiltros = function() 
	{
		$scope.data.oConfiguracionClasificacion.idPostFiltro    = false;
		$scope.data.oConfiguracionClasificacion.idActual        = false;
		$scope.data.oConfiguracionClasificacion.seleccionado    = false;						
    	$scope.data.oConfiguracionTipoInstrumento.idPostFiltro  = false;
    	$scope.data.oConfiguracionTipoInstrumento.idActual      = false;
    	$scope.data.oConfiguracionTipoInstrumento.seleccionado  = false;
		$scope.data.oConfiguracionBoveda.idPostFiltro 			= false;
		$scope.data.oConfiguracionBoveda.idActual     			= false;
		$scope.data.oConfiguracionBoveda.seleccionado 			= false;
		$scope.data.oConfiguracionInstrumento.buscar			= "-1";
		
		$scope.data.kngFechaConsulta =  util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.FechaConsultaOpt = {max:util.dateInicioDia($scope.SesionFechaSistema)};		
		
		$scope.seleccionCustodiaGeneral = false
		dsCustodiaGeneral.removeAll();
	}
	
	
		
	$scope.$watch('data.chkAgrupaPorBoveda',function(newval,oldval){
		switch (newval){
			case true:
				$scope.data.oConfiguracionBoveda.activado = true;				
				break;
				
			case false: 
				$scope.data.oConfiguracionBoveda.idActual = 0;
				$scope.data.oConfiguracionBoveda.activado = false;
				break;  
		}
	});
	
	
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