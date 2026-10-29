angular.module('ModuloCustodia').controller('ConsultarMovimientosEnTransitoAgrupadosCtrl', 
function($scope,$timeout,$modal,$state,bcsComunes, bcsBlotterMovimientosEnTransitoGrid, bcsServicioPlataforma,
		bcsServiciosMovTransito,bcsServicioReportes,$translate, bcsAlerta, OPCIONES_BCSALERTA,$http)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/

	$scope.filtro = {};
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);

	bcsComunes.ventanaAutoHeight(idVentana);
	
	//Fechas filtro
	$scope.filtro.fecInicio 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTermino		 =  util.dateInicioDia($scope.SesionFechaSistema);

	$scope.filtro.fecInicioLiq 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTerminoLiq		 =  util.dateInicioDia($scope.SesionFechaSistema);
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	
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
	
	$scope.data.oConfiguracionConceptoCustodia = {
		tipo : "combobox-concepto-custodia",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionBoveda = {
		tipo : "combobox-boveda",
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
			IdClasificacionTipoInstrumento : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq","value"   : ""},
			NoDerivados					   : { "field"   : "IdTipoInstrumento","operator": "neq", "value" : "DER" }
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
	
	
	
	var dsGrillaMovimientos = new bcs.DataSource({
		type		: "odata",
		definition	: {
			serverFiltering	: true, // Filtrado de datos en el servidor.
			serverPaging	: true, // Paginado de los datos en el servidor.
			serverSorting	: true, // Ordenamiento de los datos en el servidor.
			//sort			: { field: "IdMovimientoCarteraEnTransito", dir: "desc" },
			//filter			: { field: "IdEstado", operator: "contains", value: ['PEN','PDA'] },
			pageSize		: 100,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=63021",
					dataType : "json"
				}
			},
			schema			: modeloMovimientosAgrupados()
		}
	});
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.data.confBasicaMovimientosEnTransitoAgrupados = {
		dataSource			: dsGrillaMovimientos,
		columns				: bcsBlotterMovimientosEnTransitoGrid.columnasMovimientosAgrupados(),		
		sortable			: true,
		filterable 			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: true,
		height				: 470,
		selectable 			: "multiple row",
	    excel : {
	        allPages: true,
	      },
	    gridCreated : function(grid) {
	    	
	    	grillaMovimientosAgrupados = grid;

	    }
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.buscarMovimientos = function(sinFiltrosGrilla){
		
		var filters = new Array();
		
		filters.push({ field: "PermiteCertificacionPorUsuario", operator: "eq", value:"S" });
		
		filters.push( {
			logic: "or",
		    filters: [
		      {	logic: "and",
			    filters: [
			      { field: "IdEstado", operator: "eq", value: "PDA" }
			    ]
			  }, 
		      {	logic: "and",
			    filters: [
			      { field: "IdEstado", operator: "eq", value: "PEN" }
			    ]
		      },
		      {	logic: "and",
				    filters: [
				      { field: "IdEstado", operator: "eq", value: "MAU" }
				    ]
				  }
		    ]
		  }
		);

			try {
			if ($scope.filtro.concepto && $scope.filtro.concepto.idActual !== undefined && $scope.filtro.concepto.idActual !== 0)
				if ($scope.filtro.concepto.seleccionado !== undefined && $scope.filtro.concepto.seleccionado.IdConcepto !== "")
					filters.push({ field: "IdConcepto", operator: "contains", value: $scope.filtro.concepto.seleccionado.IdConcepto });

			} catch(e) {}
			try {
			if ($scope.filtro.accion && $scope.filtro.accion.idActual !== undefined && $scope.filtro.accion.idActual !== 0)
				if ($scope.filtro.accion.seleccionado !== undefined && $scope.filtro.accion.seleccionado.Accion !== "")
					filters.push({ field: "IndicadorIngEgr", operator: "contains", value: $scope.filtro.accion.seleccionado.Accion });
			
			} catch(e) {}
			try {
			if($scope.filtro.clasificacionTipoInstrumentoObj.seleccionado.IdClasificacionTipoInstrumento !== undefined && $scope.filtro.clasificacionTipoInstrumentoObj.seleccionado.IdClasificacionTipoInstrumento != 0)
				filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.filtro.clasificacionTipoInstrumentoObj.seleccionado.IdClasificacionTipoInstrumento });
			}catch(e){}
		
			try{
			if($scope.filtro.tipoInstrumento.IdTipoInstrumento !== undefined && $scope.filtro.tipoInstrumento.IdTipoInstrumento != "" && $scope.filtro.tipoInstrumento.IdTipoInstrumento != "0")
				filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.filtro.tipoInstrumento.IdTipoInstrumento });
			}catch(e){}
				
			try {
			if($scope.filtro.instrumentoObjeto !== undefined){
				if ($scope.filtro.instrumentoObjeto.IdInstrumento > 0 && ($scope.filtro.instrumentoObjeto.ConteoNemotecnico === undefined || $scope.filtro.instrumentoObjeto.ConteoNemotecnico === 1) )
					filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.filtro.instrumentoObjeto.IdInstrumento) });
					
				if ($scope.filtro.instrumentoObjeto.Nemotecnico != "" && $scope.filtro.instrumentoObjeto.ConteoNemotecnico > 1)
					filters.push({ field: "Nemotecnico", operator: "eq", value: $scope.filtro.instrumentoObjeto.Nemotecnico });
			}
			}catch(e){}
			
	    	try{
				if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
				filters.push({ field: "IdCliente", operator: "eq", value: Number($scope.oSocioNegocio.seleccionado.IdCliente)});
			
	    	}catch (e){}
	    	
	    	try{
			if($scope.filtro.boveda !== undefined && $scope.filtro.boveda !== null && $scope.filtro.boveda.IdBoveda.length > 0)
				filters.push({ field: "IdBoveda", operator: "eq", value: $scope.filtro.boveda.IdBoveda });	        		
	    	}catch(e){}
	    	
	    	var auxDate1;
	    	var auxDate2;    	
	
	    	if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null)
	    	{
	    		auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
				filters.push({ field: "FechaOperacion", operator: "gte", value: auxDate1 });
	    	}
	    	
	    	if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null)
	    	{
	    		auxDate2 = util.dateOdata(util.date($scope.filtro.fecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
	    	 	if (auxDate2 < auxDate1)
	    	 	{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 		return;
	    	 	}
	    		filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia(auxDate2) });
	    	}

			if ($scope.filtro.fecInicioLiq !== undefined && $scope.filtro.fecInicioLiq !== "" && $scope.filtro.fecInicioLiq !== null)
	    	{
				filters.push({ field: "FechaLiquidacion", operator: "gte", value: $scope.filtro.fecInicioLiq  });
	    	}
	    	
	    	if ($scope.filtro.fecTerminoLiq !== undefined && $scope.filtro.fecTerminoLiq !== "" && $scope.filtro.fecTerminoLiq !== null)
	    	{
	    	 	if ($scope.filtro.fecTerminoLiq < $scope.filtro.fecInicioLiq)
	    	 	{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 		return;
	    	 	}
	    		filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.filtro.fecTerminoLiq) });
	    	}
		
			
		dsGrillaMovimientos.filter(filters);
	};
	
	
	$scope.limpiarFiltros = function() {
		$scope.filtro.Portafolio								= "";
		$scope.filtro.cliente									= undefined;
		$scope.filtro.PortafolioObjeto							= undefined;
		$scope.filtro.clasificacionTipoInstrumentoObj.idActual	= 0;
		$scope.filtro.tipoInstrumento							= 0;
		$scope.filtro.accion									= undefined;
		$scope.filtro.concepto									= undefined;
		$scope.filtro.boveda									= undefined;
		$scope.filtro.buscarinstrumento							= "-1";
		$scope.filtro.fecInicio									= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTermino								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecInicioLiq								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTerminoLiq								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oSocioNegocio.idActual							= false;
		$scope.oProducto.idActual								= 0;
		$scope.oPortafolio.idActual								= false;
		grillaMovimientosAgrupados.dataSource.removeAll();
	};
	
	//************************************************
	// Llamada a servicio para Certificar Movimientos
	//************************************************
	$scope.enviarCertificarAgrupado = function(param) {
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (!!$scope.data.seleccionMovimientosEnTransitoAgrupados){

			var MovimientosCertificar = [];

			//Recorremos las filas seleccionadas

			$.each($scope.data.seleccionMovimientosEnTransitoAgrupados,function(key,value){

				MovimientosCertificar.push(
				 {
						IdCliente : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdCliente,
						DocumentoIdentidad : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].DocumentoIdentidad,
						IdTipoOperacion : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdTipoOperacion,
						IdClasificacionTipoInstrumento : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdClasificacionTipoInstrumento,
						FechaOperacion : util.dateSPdate($scope.data.seleccionMovimientosEnTransitoAgrupados[key].FechaOperacion),
						FechaLiquidacion : util.dateSPdate($scope.data.seleccionMovimientosEnTransitoAgrupados[key].FechaLiquidacion),
						IdCondicionLiquidacion : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdCondicionLiquidacion,
						IdConcepto : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdConcepto,
						IdInstrumento : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdInstrumento,
						Nemotecnico : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].Nemotecnico,
						IdMonedaInstrumento : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdMonedaInstrumento,
						IdBoveda : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].IdBoveda,
						EsNominal : $scope.data.seleccionMovimientosEnTransitoAgrupados[key].EsNominal

				 });
				
			});


		   //Envio de valores al servicio
			var datosCertificar = {};

			datosCertificar.p_IdEmpresa				=	$scope.SesionIdEmpresa || 0	;
			datosCertificar.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
			datosCertificar.p_IdModulo				=	'CUS';
			datosCertificar.p_MovEnTransitoAG	=	MovimientosCertificar;
			datosCertificar.p_FechaProc 		= 	util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			bcsComunes.ventanaOverlay(idVentana,true);


 			bcsServiciosMovTransito.CertificarMovimientoAgrupado
				(datosCertificar,
				function(respuesta){	  		
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					grillaMovimientosAgrupados.dataSource.read();
					grillaMovimientos.dataSource.read();
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				});        
		}else{
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	};
	
	

	var firstWidth = $('#grid-movimientos-en-transito-agrupado .k-grid-content');
        firstWidth.css('height', '372px');
/******************************************************/
/*** FINAL											***/
/******************************************************/
});