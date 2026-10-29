angular.module('ModuloCustodia').controller('ConsultarMovimientosEnTransitoHistoricoCtrl', 
function($scope,$timeout,$modal,$http,$translate,bcsComunes,bcsBlotterMovimientosEnTransitoGrid,bcsServicioPlataforma,bcsServiciosMovTransito,bcsServiciosCustodia,
			bcsServicioReportes,bcsAlerta,OPCIONES_BCSALERTA)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.filtro = {};
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	
	$scope.filtro.fecInicio = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTermino = util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.filtro.fecInicioLiq	=  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTerminoLiq	=  util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.gridMovimientosHistoricos;
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oClientePortafolio = {
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			cargaClienteAutomatico :'S',
			filtros: {
				IdEmpresa 	: {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				IdCliente 	: { "field": "IdCliente","operator": "eq", "value"   :  0 },
				EsCliente	: {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			}
	};
	
	$scope.data.oConfiguracionEstadoMovimiento = {
		tipo : "combobox-estado-custodia",
		configuracion : {},
		filtros : {
			NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : 'Cartera.MovimientoEnTransito' }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	
 	$scope.data.oConfiguracionConceptoCustodia = {
		tipo : "combobox-concepto-custodia",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	
 	$scope.data.oConfiguracionTipoOperacionCustodia = {
		tipo : "combobox-tipo-operacion-custodia",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	
 	$scope.data.oConfiguracionMoneda = {
		tipo : "combobox-moneda",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	
 	$scope.data.oConfiguracionClasificacionTipoInstrumento = {
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	
 	$scope.data.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : { 	
			IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento", "operator": "eq", "value"   : "" },
			NoDerivados					   : { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "DER" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
   	
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	/*
	var dataSourceConfiguracion = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'APV.Habilitado' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceConfiguracion.fetch(function(){
		if(dataSourceConfiguracion.data().length > 0){
			if(dataSourceConfiguracion.data()[0].Valor === 'S'){
				$scope.MostrarProducto = true;
			}
		}
 	});
 	*/
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
 	
	var dsGrillaHistoricos = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, // Filtrado de datos en el servidor.
			serverPaging	: true, // Paginado de los datos en el servidor.
			serverSorting	: true, // Ordenamiento de los datos en el servidor.
			sort			: { field: "IdMovimientoCarteraEnTransito", dir: "desc" },
			pageSize		: 100,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1006",
					dataType : "json"
				}
			},
			schema			: modeloMovimientos()
		}
	});
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.confBasicaMovimientosEnTransitoHistorico = {
		dataSource			: dsGrillaHistoricos,
		columns				: bcsBlotterMovimientosEnTransitoGrid.columnasMovimientos(),		
		sortable			: true,
		pageable			: true,
		filterable 			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		height				: 467,
		selectable 			: "multiple row",
		gridCreated 		: function(grid) {
			$scope.gridMovimientosHistoricos = grid;
	    	grid.showColumn("NombreEstado");
	    }
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.buscarHistorico = function(){
		var filters = new Array();
		//filters.push({ field: "PermiteCertificacionPorUsuario", operator: "eq", value:"S" });
		filters.push({ field: "IdEstado", operator: "neq", value: "PEN"});
		
		if($scope.MostrarProducto)
		{
			if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
				filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.oSocioNegocio.seleccionado.IdCliente)});
			
			if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oPortafolio.seleccionado.IdPortafolio)});
			}
			else if($scope.oProducto.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				filters.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oPortafolio.seleccionado.IdPortafolio)});
			}
			else if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolio.seleccionado.IdPortafolio === undefined)
			{
				if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) !== 0)
				{
					if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
					{
						filters.push({ field: "IdProductoAgrupador", operator: "eq", value: 0});
					}
					else
					{
						filters.push({ field: "IdProductoAgrupador", operator: "eq", value: Number($scope.oProducto.seleccionado.IdProductoAgrupador)});
					}
				}
			}
		}
		else
		{
        	if ($scope.data.oClientePortafolio.seleccionado.IdCliente > 0 && $scope.data.oClientePortafolio.seleccionado.IdPortafolio > 0)
        	{
        		filters.push({ field: "IdCliente", operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdCliente});
        		filters.push({ field: "IdPortafolio", operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdPortafolio});
        	}
		}

		if($scope.filtro.generaTraspaso == true)
		{
			if ( $scope.filtro.accion && $scope.filtro.accion.idActual !== undefined && $scope.filtro.accion.idActual !== 0)
				if ($scope.filtro.accion.seleccionado !== undefined && $scope.filtro.accion.seleccionado.Accion !== "")
	        		filters.push({ field: "IndicadorIngEgr", operator: "contains", value: $scope.filtro.accion.seleccionado.Accion });
			
			filters.push({ field: "GeneraTraspaso", operator: "eq", value: "S"});
		}
		else
		{
			if ( $scope.filtro.accion && $scope.filtro.accion.idActual !== undefined && $scope.filtro.accion.idActual !== 0)
				if ($scope.filtro.accion.seleccionado !== undefined && $scope.filtro.accion.seleccionado.Accion !== "")
	        		filters.push({ field: "IndicadorIngEgr", operator: "contains", value: $scope.filtro.accion.seleccionado.Accion });
			
	        if ($scope.filtro.concepto && $scope.filtro.concepto.idActual !== undefined && $scope.filtro.concepto.idActual !== 0)
				if ($scope.filtro.concepto.seleccionado !== undefined && $scope.filtro.concepto.seleccionado.IdConcepto !== "")
					filters.push({ field: "IdConcepto", operator: "contains", value: $scope.filtro.concepto.seleccionado.IdConcepto });
			
	    	if ($scope.filtro.clasificacionTipoInstrumento)
	        	if ($scope.filtro.clasificacionTipoInstrumento !== undefined )
	        		if ($scope.filtro.clasificacionTipoInstrumento !== "")
	        			filters.push({ field: "IdClasificacionTipoInstrumento", operator: "contains", value: $scope.filtro.clasificacionTipoInstrumento });
	    	
	    	if ($scope.filtro.tipoInstrumento)
	        	if ($scope.filtro.tipoInstrumento !== undefined)
	        		if ($scope.filtro.tipoInstrumento !== "")
	        			filters.push({ field: "IdTipoInstrumento", operator: "contains", value: $scope.filtro.tipoInstrumento });
	    	
	    	if ($scope.filtro.instrumento !== undefined) {
	    		if ($scope.filtro.instrumento.IdInstrumento > 0 && ($scope.filtro.instrumento.ConteoNemotecnico === undefined || $scope.filtro.instrumento.ConteoNemotecnico === 1) )
					filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.filtro.instrumento.IdInstrumento) });
					
				if ($scope.filtro.instrumento.Nemotecnico != "" && $scope.filtro.instrumento.ConteoNemotecnico > 1)
					filters.push({ field: "Nemotecnico", operator: "eq", value: $scope.filtro.instrumento.Nemotecnico });
	    	}
	    	if ($scope.filtro.estado)
	        	if ($scope.filtro.estado !== undefined)
					if ($scope.filtro.estado !== "")
	        			filters.push({ field: "IdEstado", operator: "contains", value: $scope.filtro.estado });
	    	
			if ($scope.filtro.moneda)
	        	if ($scope.filtro.moneda !== undefined)
	        		if ($scope.filtro.moneda !== "")
	        			filters.push({ field: "IdMonedaInstrumento", operator: "contains", value: $scope.filtro.moneda });
		}
		
		
    	var auxDate1;
    	var auxDate2;
    	
    	if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null){
    		try{
	    		//if (!kendo.parseDate($scope.filtro.fecInicio,"dd-MM-yyyy")){  
	    		if ($scope.filtro.fecInicio  !== undefined){
	    			//auxDate1 = kendo.parseDate(moment($scope.filtro.fecInicio).format('YYYY-MM-DD') + "T00:00:00" );
	    			//auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
	    			auxDate1 = util.dateInicioDia($scope.filtro.fecInicio);
	    			filters.push({ field: "FechaOperacion", operator: "gte", value: auxDate1 });
	    		}
    		}catch (e){
    			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_INICIO_RANGO_CONSULTA_INVALIDA"));
    			return;
    		}
    	}
    	
    	if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null){
    		try{
	    		if ($scope.filtro.fecTermino !== undefined){ 
    			//if (!kendo.parseDate($scope.filtro.fecInicio,"dd-MM-yyyy")){  
	    			//auxDate2 =kendo.parseDate(moment($scope.filtro.fecTermino).format('YYYY-MM-DD') + "T23:59:59" )
    		 		//auxDate2 = util.dateOdata(util.date($scope.filtro.fecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
	    			auxDate2 = util.dateTerminoDia($scope.filtro.fecTermino);
	    		}	
    		 		if (auxDate2 < auxDate1) {
		    			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
		    			return;
    		 		}
    		 		
    				filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia(auxDate2) });
	    		
    		}catch (e){
    			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    			return;
    		}
    	}
    	
    	if ($scope.filtro.fecInicioLiq !== undefined && $scope.filtro.fecInicioLiq !== "" && $scope.filtro.fecInicioLiq !== null){
			filters.push({ field: "FechaLiquidacion", operator: "gte", value: $scope.filtro.fecInicioLiq});
	    }
	    	
	    if ($scope.filtro.fecTerminoLiq !== undefined && $scope.filtro.fecTerminoLiq !== "" && $scope.filtro.fecTerminoLiq !== null){
	    	 if ($scope.filtro.fecTerminoLiq < $scope.filtro.fecInicioLiq){
	    	 	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 	return;
	    	 }
	    	filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.filtro.fecTerminoLiq)});
	    }
    	
		dsGrillaHistoricos.filter(filters);			
	};
	
	$scope.limpiarFiltros = function(){
		$scope.clienteng									= "";
		$scope.filtro.cliente								= undefined;
		$scope.filtro.clienteObj							= undefined;
		$scope.filtro.clasificacionTipoInstrumento			= "";
		$scope.filtro.tipoInstrumento						= "";				
		$scope.filtro.accion								= undefined;
		$scope.filtro.concepto								= undefined;
		$scope.filtro.buscarinstrumento						= "-1";
		$scope.filtro.estado								= "";
		$scope.filtro.moneda								= "";
		if(!$scope.MostrarProducto)
			$scope.data.filtros.IdPortafolio					= false;
		$scope.filtro.fecInicio								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTermino							= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecInicioLiq							= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTerminoLiq							= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.generaTraspaso						= false;
		
		$scope.oSocioNegocio.idActual						= false;
		$scope.oProducto.idActual							= 0;
		$scope.oPortafolio.idActual							= false;
	}
	
	
	var ConfiguracionTemplate = new bcs.DataSource({
		type: "odata",
		definition:{
			serverFiltering:true,
			sort: { field: "ReportVersion", dir: "desc" },
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=3015&c=1",
					dataType: "json",
					data:{
						$select: "\"CompanyId\",\"Id\",\"ReportName\",\"ReportVersion\""
					}
				}
			},
			schema: {
				model: {
					fields : {
						Id: {type: "string"},
						ReportName: {type: "string"},
						ReportVersion: {type: "string"}
					}
				}
			}
		}
	});
	
	
	//************************************************
	// Descarga carta de traspaso
	//************************************************
	$scope.descargarCartaTraspaso= function(){
		var tipoCarta="TRASPASO_ACCIONES_CIA";
		var movimientos = $scope.seleccionMovimientosEnTransitoHistorico;
		var cadenaMovimientos= "";
		
		if(movimientos.length >0){
			for(var i= 0; i<movimientos.length; i++){
				if(movimientos[0].IdTipoOperacion!==movimientos[i].IdTipoOperacion){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("REGISTROS_CON_DIFERENTES_TIPOS_DE_OPERACIONES"));
					return;
				}else if (movimientos[0].IdCliente!==movimientos[i].IdCliente){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("REGISTROS_CON_DIFERENTES_CLIENTES"));
					return;
				}
				else if (movimientos[0].GeneraTraspaso !== 'S'){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_TIPO_MOVIMIENTO_INVALIDO"));
					return;
				
				}else{
					if(i!==movimientos.length-1){
						cadenaMovimientos = cadenaMovimientos+String(movimientos[i].IdMovimientoCarteraEnTransito)+",";	
					}else{
						cadenaMovimientos = cadenaMovimientos+String(movimientos[i].IdMovimientoCarteraEnTransito);	
					}				
				}
			}
			
			var filtro = new Array();
			filtro.push({ field: "CompanyId", operator: "eq", value: $scope.SesionIdEmpresa });
			filtro.push({ field: "ReportName", operator: "eq", value: tipoCarta });    
			
			ConfiguracionTemplate.query({filter:filtro}).then(function() {
				if(ConfiguracionTemplate.data().length > 0){						
						var pdf417 =movimientos[0].IdCliente+" "+movimientos[0].DocumentoIdentidad+" "+util.dateSPdate(movimientos[0].FechaOperacion);
						var data = {
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_Movimientos :cadenaMovimientos,								
								$schema : [{ "field" : "LogoCorredora", "type" : "blob" }]
						};	
						bcsServicioReportes.descargarReportePdf417(ConfiguracionTemplate.data()[0].Id, 4226, "TRASPASO_ACCIONES_"+movimientos[0].IdCliente+$scope.SesionFechaSistema.replace(/-/g,''), pdf417, "PDF", data, idVentana, false);
					
				}else{
					bcsComunes.msg("error","Error", $translate.instant("UI_ERROR_OBTENER_REPORTE"));
				}
			});
			
		}else{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	};
	
	
	//************************************************
	// Descarga Comprobante 
	//************************************************
	$scope.obtenerComprobante = function(){
		var filtro = new Array();
		filtro.push({ field: "CompanyId", operator: "eq", value: $scope.SesionIdEmpresa });
		filtro.push({ field: "ReportName", operator: "eq", value: "COMPROBANTEINGRESORETIROCUSTODIA" });
		
		ConfiguracionTemplate.query({filter:filtro}).then(function() {
			if(ConfiguracionTemplate.data().length > 0){
				for(var i=0; i < $scope.seleccionMovimientosEnTransitoHistorico.length; i++){
					var movimiento = $scope.seleccionMovimientosEnTransitoHistorico[i];
					var pdf417 = movimiento.IdMovimientoCarteraEnTransito+" "+movimiento.IdCliente+" "+movimiento.DocumentoIdentidad+" "+util.dateSPdate(movimiento.FechaOperacion);
					var data = {
						"P_IDEMPRESA" : $scope.SesionIdEmpresa,
						"P_IDMODULO" : "CUS",
						"P_IDCLIENTE" : movimiento.IdCliente,
						"P_IDPORTAFOLIO" : movimiento.IdPortafolio,
						"P_IDMOVIMIENTO" : movimiento.IdMovimientoCarteraEnTransito,
						"$schema":[{"field":"LogoCorredora", "type":"blob"}]
					};	
					bcsServicioReportes.descargarReportePdf417(ConfiguracionTemplate.data()[0].Id, 13018, "COMPROBANTEINGRESORETIROCUSTODIA", pdf417, "PDF", data, idVentana, false);
				}
			}else{
				bcsComunes.msg("error","Error", $translate.instant("UI_ERROR_OBTENER_REPORTE"));
			}
		});
	};
	
	//************************************************
	// Anular Comprobante 
	//************************************************
	$scope.anularComprobante = function(){
		
		if ($scope.seleccionMovimientosEnTransitoHistorico.length == 1){

			var movimiento = [];
			
			movimiento.push(
			{
				IdMovimientoCarteraTransito: $scope.seleccionMovimientosEnTransitoHistorico[0].IdMovimientoCarteraEnTransito,
				IdTipoOperacion: $scope.seleccionMovimientosEnTransitoHistorico[0].IdTipoOperacion,
				IdEstado: $scope.seleccionMovimientosEnTransitoHistorico[0].IdEstado
			
			});
			
			if($scope.validarMovimientoAnular($scope.seleccionMovimientosEnTransitoHistorico[0])){
				
				var mensajeConfirmacion = "UI_SE_ANULARA_LOS_REGISTROS_SELECCIONADOS";
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant(mensajeConfirmacion));


				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){
						bcsComunes.ventanaOverlay(idVentana,true);	
					   //Envio de valores al servicio
						var datosAnular = {};

						datosAnular.p_IdEmpresa				=	$scope.SesionIdEmpresa || 0	;
						datosAnular.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
						datosAnular.p_IdModulo				=	"CUS"	;
						datosAnular.p_simularOperacion		=	"N";
						datosAnular.p_UsaReglasNegocio		=	"N";
						datosAnular.P_MOVIMIENTOSANULAR		=	movimiento;
						datosAnular.p_GlosaAnulacion		=	"";
						datosAnular.p_FechaProceso 			= 	util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						datosAnular.p_IdSucursal 			= 	0;

						bcsServiciosMovTransito.AnularMovimientoCertificado(datosAnular).$promise.then(
							function(result) {
								let mensajeId = bcsComunes.ventanaMsg({ conflictos: util.parseServerResponse(result) }, idVentana);

								bcsComunes.ventanaPromesa(mensajeId).then(function() {
									bcsComunes.ventanaOverlay(idVentana, false);
									if (result.P_OUT_ERRORESCONTADOR === 0) {
										$scope.gridMovimientosHistoricos.dataSource.read();
									}
								});
							},
							function() {
								bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
								bcsComunes.ventanaOverlay(idVentana, false);
						});	       
					}
				});
			}			
		}else{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Solo se puede anular un Movimiento a la vez."/*$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO")*/);
			//Deshabilitar boton
		}
				
	};
	//************************************************
	// Descarga Traspaso Historico Custodia Emisor
	//************************************************
	$scope.descargarTraspasoHistoricoCustodiaEmisor = function(){
		var tipoCarta="TRASPASO_CUSTODIA_EMISOR";
		var movimientos = $scope.seleccionMovimientosEnTransitoHistorico;
		var cadenaMovimientos= "";
		
		if(movimientos.length >0){
			for(var i= 0; i<movimientos.length; i++){
				if(movimientos[0].IdTipoOperacion!==movimientos[i].IdTipoOperacion){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("REGISTROS_CON_DIFERENTES_TIPOS_DE_OPERACIONES"));
					return;
				}else if (movimientos[0].IdCliente!==movimientos[i].IdCliente){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("REGISTROS_CON_DIFERENTES_CLIENTES"));
					return;
				}else{
					if(i!==movimientos.length-1){
						cadenaMovimientos = cadenaMovimientos+String(movimientos[i].IdMovimientoCarteraEnTransito)+",";	
					}else{
						cadenaMovimientos = cadenaMovimientos+String(movimientos[i].IdMovimientoCarteraEnTransito);	
					}				
				}
			}
			if (movimientos[0].GeneraTraspaso === 'S'){
				var filtro = new Array();
				filtro.push({ field: "CompanyId", operator: "eq", value: $scope.SesionIdEmpresa });
				filtro.push({ field: "ReportName", operator: "eq", value: tipoCarta });    

				ConfiguracionTemplate.query({filter:filtro}).then(function() {
					if(ConfiguracionTemplate.data().length > 0){
						var data = {
									p_IdEmpresa : $scope.SesionIdEmpresa,
									p_Movimientos :cadenaMovimientos,								
									$schema : [{ "field" : "LogoCorredora", "type" : "blob" }]
							};	

							var parameters = {
								"reportId": ConfiguracionTemplate.data()[0].Id,
								"parameters": [],
								"exportFormat": "PDF",
								//"debug" : false,
								"configuration": {
									"mainReport": {
										"reportName": "TRASPASO_CUSTODIA_EMISOR",
										"parameters": [],
										"data": {
											"serviceId" : 4270,
											"serviceData" : data,
											"serviceResponse":"resultSet"
										},	
										"parameters": {},					
									},
								"subReports": []
								}
							};	
							$scope.getReport(parameters);
					}else{
						bcsComunes.msg("error","Error", $translate.instant("UI_ERROR_OBTENER_REPORTE"));
					}
				});
		    }else{
		    	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("REGISTROS_NO_SON_TRANSFERENCIA"));
				return;
		    }
		}else{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	};
	
	$scope.getReport = function(dataTemplate){
	
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', dataTemplate, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				var nombreArchivo = "TRASPASO_CUSTODIA_EMISOR";
				saveAs(file, nombreArchivo+".pdf");
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
		}

	$scope.validarMovimientoAnular = function(movimiento){
		
		var resultado 		= true;
		var fechaActual 	= util.dateInicioDia($scope.SesionFechaSistema);
		var fechaOperacion 	= util.dateInicioDia(movimiento.FechaOperacion);
		
		if (movimiento.IdEstado != "CRT"){
			
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Estado invalido. Debe estar Certificado."/*$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO")*/);
			resultado = false;
			
		}else if(["RV", "RF", "IF"].indexOf(movimiento.IdClasificacionTipoInstrumento) == -1 && !((["INGGTA", "RETGTA"].includes(movimiento.IdTipoOperacion) && movimiento.IdOrigenGarantia == 'CTA') || (["INGGTA", "RETGTA", "-"].includes(movimiento.IdTipoOperacion) && movimiento.IdOrigenGarantia == 'ESP'))){
			
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Mercado invalido."/*$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO")*/);
			resultado = false;
			
		}else if (movimiento.IdTipoComprobante != "CUS") {
			
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Tipo Comprobante invalido."/*$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO")*/);
			resultado = false;
			
		}else if (fechaActual > fechaOperacion) {
			
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Fecha Operacion invalida, debe ser del dia."/*$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO")*/);
			resultado = false;
		}else if (["INGTIT", "RETTIT","INGGTA","RETGTA"].indexOf(movimiento.IdTipoOperacion) == -1 ) {
			
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Solo se pueden anular ingresos y retiros de titulos/garantias.");
			resultado = false;
		}
		
		return resultado;
	}
   	
	
	//************************************************
	// Llamada a servicio para Anular Movimientos
	//************************************************
	$scope.enviarAnular = function() {
		if ($scope.seleccionMovimientosEnTransitoHistorico != undefined){
			var MovimientosAnular = [];
      	     
      	    //Recorremos las filas seleccionadas
		 	var v_iter ;
		 	 v_iter=1;
      	    $.each($scope.seleccionMovimientosEnTransitoHistorico,function(key,value){
      	    	MovimientosAnular.push(
      	    	{
      	    		IdMovimientoCarteraTransito: $scope.seleccionMovimientosEnTransitoHistorico[key].IdMovimientoCarteraEnTransito
	      				 		      				 
	      		 });
      	    	 v_iter=v_iter+1;
      	    });
      	    
      	    var mensajeConfirmacion = "UI_SE_ANULARA_LOS_REGISTROS_SELECCIONADOS";
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant(mensajeConfirmacion));
			
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
		    	   //Envio de valores al servicio
		    	    var datosAnular = {};
		    	   
		    	    datosAnular.p_IdEmpresa				=	$scope.SesionIdEmpresa || 0	;
		    	    datosAnular.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
		    	    datosAnular.p_IdModulo				=	"CUS"	;
		    	    datosAnular.p_simularOperacion		=	"N";
		    	    datosAnular.p_UsaReglasNegocio		=	"N";
		    	    datosAnular.P_MOVIMIENTOSANULAR		=	MovimientosAnular;
		    	    datosAnular.p_GlosaAnulacion		=	"";
		    	    datosAnular.p_FechaProceso 			= 	util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		    	    datosAnular.p_IdSucursal 			= 	0;
		    	    
		    	    bcsServiciosMovTransito.AnularMovimiento(datosAnular,
						function(respuesta){	  		
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
							$scope.gridMovimientosHistoricos.dataSource.read();
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);
						}
		    	   	)
				}
			});
		}else{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	};
	
	$scope.generarContrapartidaComprobante = function (){
		
		if ($scope.seleccionMovimientosEnTransitoHistorico.length == 1){
			bcsComunes.ventanaOverlay(idVentana,true);	
			let movimiento = [];
			if($scope.validarMovimientoGenerarContrapartida($scope.seleccionMovimientosEnTransitoHistorico[0])){
				
				movimiento.push(
				{
					IdMovimientoCarteraEnTransito: $scope.seleccionMovimientosEnTransitoHistorico[0].IdMovimientoCarteraEnTransito,
					IdTipoOperacion: $scope.seleccionMovimientosEnTransitoHistorico[0].IdTipoOperacion,
					IdEstado: $scope.seleccionMovimientosEnTransitoHistorico[0].IdEstado,
					IdClasificacionTipoInstrumento: $scope.seleccionMovimientosEnTransitoHistorico[0].IdClasificacionTipoInstrumento,
					CorteMinimo: $scope.seleccionMovimientosEnTransitoHistorico[0].CorteMinimo,
					Cantidad : $scope.seleccionMovimientosEnTransitoHistorico[0].Cantidad,
					IdCliente: $scope.seleccionMovimientosEnTransitoHistorico[0].IdCliente,
					IdConcepto: $scope.seleccionMovimientosEnTransitoHistorico[0].IdConcepto,
					IdInstrumento: $scope.seleccionMovimientosEnTransitoHistorico[0].IdInstrumento,
					IdTipoInstrumento: $scope.seleccionMovimientosEnTransitoHistorico[0].IdTipoInstrumento,
					IdCondicionLiquidacion: $scope.seleccionMovimientosEnTransitoHistorico[0].IdCondicionLiquidacion,
					IdPortafolio: $scope.seleccionMovimientosEnTransitoHistorico[0].IdPortafolio,
					PrecioCompra: $scope.seleccionMovimientosEnTransitoHistorico[0].PrecioCompra,
					TasaCompra: $scope.seleccionMovimientosEnTransitoHistorico[0].TasaCompra,
					MontoCompra: $scope.seleccionMovimientosEnTransitoHistorico[0].MontoCompra,
					MontoCompraMonedaInstrumento: $scope.seleccionMovimientosEnTransitoHistorico[0].MontoCompraMonedaInstrumento,
					ValorRescate: $scope.seleccionMovimientosEnTransitoHistorico[0].ValorRescate,
					IdUsuario: $scope.seleccionMovimientosEnTransitoHistorico[0].IdUsuario,
					IdBolsa: $scope.seleccionMovimientosEnTransitoHistorico[0].IdBolsa,
					IdMonedaInstrumento: $scope.seleccionMovimientosEnTransitoHistorico[0].IdMonedaInstrumento,
					IdOrigenGarantia: $scope.seleccionMovimientosEnTransitoHistorico[0].IdOrigenGarantia,
					IdTipoComprobante: $scope.seleccionMovimientosEnTransitoHistorico[0].IdTipoComprobante,
					IdEstado: $scope.seleccionMovimientosEnTransitoHistorico[0].IdEstado,
					IdCondicionLiquidacion: $scope.seleccionMovimientosEnTransitoHistorico[0].IdCondicionLiquidacion,
					EsGarantia: $scope.seleccionMovimientosEnTransitoHistorico[0].EsGarantia,
					IdAgente: $scope.seleccionMovimientosEnTransitoHistorico[0].IdAgente,
					FechaOperacion : util.dateSPdate($scope.seleccionMovimientosEnTransitoHistorico[0].FechaOperacion),
					IdTipoReajuste: $scope.seleccionMovimientosEnTransitoHistorico[0].IdTipoReajuste,
					NemoSVS: $scope.seleccionMovimientosEnTransitoHistorico[0].NemoSVS || '',
					FechaEmisionInstrumento: util.dateSPdate($scope.seleccionMovimientosEnTransitoHistorico[0].FechaEmisionInstrumento),
					FechaVencimientoInstrumento: util.dateSPdate($scope.seleccionMovimientosEnTransitoHistorico[0].FechaVencimientoInstrumento),
					IdEmisor: $scope.seleccionMovimientosEnTransitoHistorico[0].IdEmisor,
					PlazoEnDias: $scope.seleccionMovimientosEnTransitoHistorico[0].PlazoEnDias,
					EsFungible: $scope.seleccionMovimientosEnTransitoHistorico[0].EsFungible,
					IdEspecieGarantia : $scope.seleccionMovimientosEnTransitoHistorico[0].IdEspecieGarantia,
					IdContraparteGarantia: $scope.seleccionMovimientosEnTransitoHistorico[0].IdContraparteGarantia || 0,
					ExcluyeMovimientoEnCartola: $scope.seleccionMovimientosEnTransitoHistorico[0].ExcluyeMovimientoEnCartola
				});
				
				let parametros = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario	: $scope.SesionIdUsuario,
					p_IdEmpleado : $scope.SesionIdEmpleado,
					p_IdModulo 	: "CUS",
					p_UsaReglasNegocio	: "N",
					p_SimularOperacion	: "N",
					p_IndicadorIngEgr: $scope.seleccionMovimientosEnTransitoHistorico[0].IndicadorIngEgr,
					p_MovimientosIngreso: $scope.seleccionMovimientosEnTransitoHistorico[0].IndicadorIngEgr =="INGRESO" ? movimiento :[],
					p_MovimientosRetiro: $scope.seleccionMovimientosEnTransitoHistorico[0].IndicadorIngEgr =="RETIRO" ? movimiento :[],
					
				};
				
				bcsServiciosMovTransito.GenerarMovimientoContraparteGarantia(parametros).$promise.then(
					function(result) {
						let mensajeId = bcsComunes.ventanaMsg({ conflictos: util.parseServerResponse(result) }, idVentana);

						bcsComunes.ventanaPromesa(mensajeId).then(function() {
							bcsComunes.ventanaOverlay(idVentana, false);
							if (result.P_OUT_ERRORESCONTADOR === 0) {
								$scope.gridMovimientosHistoricos.dataSource.read();
							}
						});
					},
					function() {
						bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
						bcsComunes.ventanaOverlay(idVentana, false);
				});	
			}
				
			
		}else{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_CANTIDAD_MOVIMIENTOS_GENERAR_CONTRAPARTE"));
			//Deshabilitar boton
		}
	};
	
	$scope.validarMovimientoGenerarContrapartida = function (movimiento) {
		let resultado 		= true;
		let fechaActual 	= util.dateInicioDia($scope.SesionFechaSistema);
		let fechaOperacion 	= util.dateInicioDia(movimiento.FechaOperacion);
		
		if (movimiento.IdEstado != "CRT"){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_ESTADO_INVALIDO"));
			resultado = false;
		}else if(["RV", "RF", "IF"].indexOf(movimiento.IdClasificacionTipoInstrumento) == -1 && !((["INGGTA", "RETGTA"].includes(movimiento.IdTipoOperacion) && movimiento.IdOrigenGarantia == 'CTA') || (["INGGTA", "RETGTA", "-"].includes(movimiento.IdTipoOperacion) && movimiento.IdOrigenGarantia == 'ESP'))){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_MERCADO_INVALIDO"));
			resultado = false;
		}else if (movimiento.IdTipoComprobante != "CUS") {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_TIPO_COMPROBANTE_INVALIDO"));
			resultado = false;
		}else if (["INGGTA","RETGTA"].indexOf(movimiento.IdTipoOperacion) == -1 ) {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_TIPO_OPERACION_GENERAR_CONTRAPARTE"));
			resultado = false;
		}else if (movimiento.ExcluyeMovimientoEnCartola == "SI"){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_EXCLUYE_MOVIMIENTO_GENERAR_CONTRAPARTE"));
			resultado = false;
		}
		
		return resultado;
	};
/******************************************************/
/*** FINAL											***/
/******************************************************/
});
