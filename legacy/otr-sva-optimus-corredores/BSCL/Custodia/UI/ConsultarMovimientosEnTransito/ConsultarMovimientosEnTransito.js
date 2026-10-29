angular.module('ModuloCustodia').controller('ConsultarMovimientosEnTransitoCtrl', 
function($scope,$timeout,$modal,$state,bcsComunes, bcsBlotterMovimientosEnTransitoGrid, bcsServicioPlataforma,
		bcsServiciosMovTransito,bcsServicioReportes,$translate, bcsAlerta, OPCIONES_BCSALERTA,$http)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.filtro = {};
	$scope.data ={};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
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
	$scope.sumaCantidad = 0;
	$scope.sumaMonto = 0;
	$scope.MonedaReajusteTotalizador = $scope.SesionIdMonedaSistema
	
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
			sort			: { field: "IdMovimientoCarteraEnTransito", dir: "desc" },
			//filter			: { field: "IdEstado", operator: "contains", value: ['PEN','PDA'] },
			pageSize		: 100,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1006",
					dataType : "json",
					data: function() {	
						var data = dsGrillaMovimientos.filter();
						try {
							$scope.ActualizarTotalizador(data.filters);
							
						} catch (error) {
							$scope.sumaCantidad = 0;
							$scope.sumaMonto = 0;
						}
								
					  }
				}
			},
			schema			: modeloMovimientos()
		},

	});
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.data.confBasicaMovimientosEnTransito = {
		dataSource			: dsGrillaMovimientos,
		columns				: bcsBlotterMovimientosEnTransitoGrid.columnasMovimientos(),		
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
	    	
	    	grillaMovimientos = grid;

	    	if($scope.MostrarProducto === true){
		    	grid.showColumn("NombreProductoAgrupador");
	    		$scope.buscarMovimientos(true);
	    	}
	    }
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.buscarMovimientos = function(sinFiltrosGrilla){
		
		var filters = new Array();
		
		filters.push({ field: "PermiteCertificacionPorUsuario", operator: "eq", value:"S" });
//		filters.push({ field: "IdEstado", operator: "gte", value: "PDA"});
//		filters.push({ field: "IdEstado", operator: "lte", value: "PEN" });
//		filters.push({ field: "IdEstado", operator: "eq", value: "MAU" });
//		filters.push({ field: "IdEstado", operator: "lte", value: "MAU" });
		
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
		
		if(!sinFiltrosGrilla){

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
			if($scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento !== undefined && $scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento != 0)
				filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.filtro.clasificacionTipoInstrumentoObj.IdClasificacionTipoInstrumento });
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
				if($scope.data.oClientePortafolio.seleccionado.IdCliente > 0)
					filters.push({ field: "IdCliente", operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdCliente});
				
				if($scope.data.oClientePortafolio.seleccionado.IdPortafolio > 0)
					filters.push({ field: "IdPortafolio", operator: "eq", value: $scope.data.oClientePortafolio.seleccionado.IdPortafolio});
			}
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
	    	 	//var auxDate2 = kendo.parseDate(moment($scope.filtro.fecTermino).format('YYYY-MM-DD') + "T23:59:59" )
	    		auxDate2 = util.dateOdata(util.date($scope.filtro.fecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
	    	 	if (auxDate2 < auxDate1)
	    	 	{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 		return;
	    	 	}
	    		filters.push({ field: "FechaOperacion", operator: "lte", value: util.dateTerminoDia(auxDate2) });
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
		}
			
		dsGrillaMovimientos.filter(filters);
	};
	
	
	$scope.limpiarFiltros = function() {
		$scope.filtro.Portafolio								= "";
		$scope.filtro.cliente									= undefined;
		$scope.filtro.PortafolioObjeto							= undefined;
		$scope.filtro.clasificacionTipoInstrumento				= "";
		$scope.filtro.tipoInstrumento							= "";
		$scope.filtro.accion									= undefined;
		$scope.filtro.concepto									= undefined;
		$scope.filtro.buscarinstrumento							= "-1";
		if(!$scope.MostrarProducto)
			$scope.data.filtros.IdPortafolio					= false;
		$scope.filtro.fecInicio									= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTermino								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecInicioLiq								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTerminoLiq								= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.oSocioNegocio.idActual							= false;
		$scope.oProducto.idActual								= 0;
		$scope.oPortafolio.idActual								= false;
		$scope.sumaCantidad 									= 0;
		$scope.sumaMonto 										= 0;
		grillaMovimientos.dataSource.removeAll();
	};

	
	//************************************************
	// Llamada a servicio para Certificar Movimientos
	//************************************************
	$scope.enviarCertificar = function(param) {
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (!!$scope.data.seleccionMovimientosEnTransito){

			var MovimientosCertificar = [];

			//Recorremos las filas seleccionadas
			var v_iter ;
			 v_iter=1;
			$.each($scope.data.seleccionMovimientosEnTransito,function(key,value){

				MovimientosCertificar.push(
				 {
					  IdMovimientoCarteraEnTransito: $scope.data.seleccionMovimientosEnTransito[key].IdMovimientoCarteraEnTransito,
					  IdTipoOperacion: $scope.data.seleccionMovimientosEnTransito[key].IdTipoOperacion,
					  MovimientoGarantia :$scope.validarMovimientoGarantia($scope.data.seleccionMovimientosEnTransito[key])

				 });
				 v_iter=v_iter+1;
			});

		   //Envio de valores al servicio
			var datosCertificar = {};

			datosCertificar.p_IdEmpresa				=	$scope.SesionIdEmpresa || 0	;
			datosCertificar.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
			datosCertificar.p_IdModulo				=	'CUS';
			datosCertificar.P_MOVIMIENTOSCERTIFICAR	=	MovimientosCertificar;
			datosCertificar.p_FechaOperacion 		= 	util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			bcsComunes.ventanaOverlay(idVentana,true);

			bcsServiciosMovTransito.CertificarMovimiento
				(datosCertificar,
				function(respuesta){	  		
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
					grillaMovimientos.dataSource.read();
					grillaMovimientosAgrupados.dataSource.read();
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
	
	$scope.validarMovimientoGarantia = function(  movSeleccionado) {
		 let esGarantia = 'N';
		 
		 if('VCDTIT'===movSeleccionado.IdConcepto && 'RETTIT'===movSeleccionado.IdTipoOperacion &&  'IF'===movSeleccionado.IdClasificacionTipoInstrumento && 'VENCIMIENTO GARANTIA'===movSeleccionado.Glosa){
			esGarantia='S';
		}		
		
		return esGarantia;
	}
	
	
	//***********************************************
	// Seleccionar todos los Movimientos 
	//***********************************************
	$scope.data.seleccionarTodosMovimientos = function() {	
		grillaMovimientos.select(grillaMovimientos.tbody.find(">tr"));		
	};
	
	
	$scope.data.deseleccionarTodosMovimientos = function() {
		grillaMovimientos.clearSelection();
	};
	
	
	//************************************************
	// Llamada a servicio para Anular Movimientos
	//************************************************
	$scope.enviarAnular = function(param) {
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (!!$scope.data.seleccionMovimientosEnTransito){

			var MovimientosAnular = [];

			//Recorremos las filas seleccionadas
			var v_iter ;
			 v_iter=1;
			$.each($scope.data.seleccionMovimientosEnTransito,function(key,value){

				MovimientosAnular.push(
				 {
					  IdMovimientoCarteraTransito: $scope.data.seleccionMovimientosEnTransito[key].IdMovimientoCarteraEnTransito,
					  IdTipoOperacion: $scope.data.seleccionMovimientosEnTransito[key].IdTipoOperacion,
					  IdEstado: $scope.data.seleccionMovimientosEnTransito[key].IdEstado

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
								grillaMovimientos.dataSource.read();
								grillaMovimientosAgrupados.dataSource.read();
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
					);

				}else{ 
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				}
			});
		}else{
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	};	
	
	
	/*
	var Anularsuccess = function(result){
		if (result.P_OUT_CONTADORANULADOS !== 0){
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), "Se anularon: " + result.P_OUT_CONTADORANULADOS  + " de " + result.P_OUT_CONTADORENVIADOS + " movimientos enviados.");
			$scope.buscarMovimientos();
		}else{
			console.log("Error." + result.P_OUT_ERRORESCONTADOR);
			
            var i = 0;
            var errordetalle ="";
           
            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
            }
            
            bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Operación no realizada. Detalle: " + errordetalle);
	    }
	};
	*/
	
	
	//************************************************
	// Llamada a servicio para Anular Movimientos
	//************************************************
	$scope.generarTraspaso = function() {
	   //Envio de valores al servicio
	    var datosTraspaso = {};
	   
	    datosTraspaso.p_IdEmpresa				=	$scope.SesionIdEmpresa || 0	;
	    datosTraspaso.p_IdUsuario				=	$scope.SesionIdUsuario || 0	;
	    datosTraspaso.p_IdModulo				=	'CUS'	;
	    datosTraspaso.p_Fecha		 			= 	util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
	    
	    var seleccionGeneraTraspaso = [];
	    var array = $scope.data.seleccionMovimientosEnTransito;
	    for(var i = 0; i < array.length; i++) {
	    	if(array[i].GeneraTraspaso === 'S') {
	    		seleccionGeneraTraspaso.push(array[i]);
	    	}
	    }

	    bcsServiciosMovTransito.GeneracionTraspasos(datosTraspaso,Traspasosuccess,error);
	};

	$scope.habilitaBotonGeneraTraspaso = function(array) {
		if (!array) return false;
		for(var i = 0; i < array.length; i++) {
			if(array[i].GeneraTraspaso === 'S') return true;
		}
		return false;
	}
	
   	var Traspasosuccess = function(result){
		if (result.p_out_IngresosRealizados !== 0){
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), "Se generaron: " + result.p_out_IngresosRealizados  +  " movimientos en tránsito.");
			$scope.buscarMovimientos();
			
		}else{
			
			console.log("Error." + result.P_OUT_ERRORESCONTADOR);
			
            var i = 0;
            var errordetalle ="";
           
            for(i; i<result.P_OUT_ERRORESCONTADOR; i++){
                console.log("Error: " + result.P_OUT_ERRORESDETALLE[i].Descripcion);
                errordetalle = errordetalle  + "("+ (i + 1) +") " + result.P_OUT_ERRORESDETALLE[i].Descripcion;
            }
            
            bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Operación no realizada. Detalle: " + errordetalle);
	    }
	};
	
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
	
	$scope.descargarTraspasoCustodiaEmisor = function(){
		var tipoCarta="TRASPASO_CUSTODIA_EMISOR";
		var movimientos = $scope.data.seleccionMovimientosEnTransito;
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
	
	//***********************************************
	// Habilitar boton editar precio Moneda
	//***********************************************
	
	$scope.editarPrecioMoneda = function(){
	
		let movimientos = $scope.data.seleccionMovimientosEnTransito;

		if(movimientos[0].EsNominal != 'S')
			return 	bcsComunes.msg("error",'Error',$translate.instant("MOVIMIENTO SELECCIONADO NO ES NOMINAL"));

		if(movimientos[0].IdClasificacionTipoInstrumento != 'RV')
			return 	bcsComunes.msg("error",'Error',$translate.instant("MERCADO DEBE CORRESPONDER A RV"));

		if(movimientos[0].IdTipoComprobante != 'FAE')
			return 	bcsComunes.msg("error",'Error',$translate.instant("TIPO DE COMPROBANTE DEBE CORRESPONDER A  FAE "));

		abrirModal(movimientos[0]);
	}
	
	
	abrirModal = function(movimientos){
			var id1 = bcsComunes.ventanaAbrir("ActualizarPrecioMonedaInstrumento",{Movimientos: movimientos },false,false,idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);     
	        promesa.then(function(respuesta){
	        console.log(respuesta);
	        if(respuesta)
	  			 $scope.buscarMovimientos(true);
	        }) ;                             
		};
	
	//***********************************************
	// Boton descargar comprobante traspaso entre corredores
	//***********************************************
	
	$scope.descargarTraspasoEntreCorredores = function(){
        var movimientos = $scope.data.seleccionMovimientosEnTransito;
        var cadenaMovimientos = "";
    
        if(movimientos.length > 0){
            for(var i = 0; i < movimientos.length; i++){
                if(movimientos[i].IdConcepto !== 'TRACOR') {
                    bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("MOVIMIENTO_NO_CORRESPONDE_A_TRASPASO_ENTRE_CORREDORES"));
                    return;
                }
                if(i !== movimientos.length - 1){
                    cadenaMovimientos = cadenaMovimientos + String(movimientos[i].IdMovimientoCarteraEnTransito) + ",";
                } else {
                    cadenaMovimientos = cadenaMovimientos + String(movimientos[i].IdMovimientoCarteraEnTransito);
                }	
            }
    
            var tipoReporte = "TRASPASO_ENTRE_CORREDORES";
    
            var filtro = new Array();
            filtro.push({ field: "CompanyId", operator: "eq", value: $scope.SesionIdEmpresa });
            filtro.push({ field: "ReportName", operator: "eq", value: tipoReporte });    
    
            ConfiguracionTemplate.query({filter:filtro}).then(function() {
                if(ConfiguracionTemplate.data().length > 0){
                    var data = {
                        p_IdEmpresa : $scope.SesionIdEmpresa,
                        p_Movimientos : cadenaMovimientos,
                        $schema : [{ "field" : "LogoCorredora", "type" : "blob" }]
                    };	
    
                    var parameters = {
                        "reportId": ConfiguracionTemplate.data()[0].Id,
                        "parameters": [],
                        "exportFormat": "PDF",
                        "configuration": {
                            "mainReport": {
                                "reportName": tipoReporte,
                                "parameters": [],
                                "data": {
                                    "serviceId" : 55669990,
                                    "serviceData" : data,
                                    "serviceResponse":"resultSet"
                                },
                                "parameters": {},					
                            },
                            "subReports": []
                        }
                    };	
                    $scope.getReport(parameters);
                } else {
                    bcsComunes.msg("error","Error", $translate.instant("UI_ERROR_OBTENER_REPORTE"));
                }
            });
        } else {
            bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
        }
    };

	//************************************************
	// Descarga carta de traspaso
	//************************************************
	$scope.descargarCartaTraspaso= function(){
		var tipoCarta="TRASPASO_ACCIONES_CIA";
		var movimientos = $scope.data.seleccionMovimientosEnTransito;
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

	/* TOTALIZADOR */

	$scope.$watch('data.seleccionMovimientosEnTransito',function(newval,oldval) {
		if (!newval) return;
		$scope.sumaCantidad = 0;
		$scope.sumaMonto = 0;
		var moneda = $scope.SesionIdMonedaSistema;

		var reajusteSeleccion = newval.find(function(item) {
			return item.MonedaReajuste && item.MonedaReajuste != $scope.SesionIdMonedaSistema;
		});
		
		moneda = (reajusteSeleccion) ? reajusteSeleccion.MonedaReajuste : $scope.SesionIdMonedaSistema;
		$.each(newval,function(key,value){
			if(isNaN(Number(value.Cantidad)) || value.Cantidad === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_CANTIDAD");
			}else{
				$scope.sumaCantidad = $scope.sumaCantidad + value.Cantidad;	
			}
			if(isNaN(Number(value.MontoCompraMonedaInstrumento)) || value.MontoCompraMonedaInstrumento === ""){
				bcsComunes.msg("warning", "UI_ADVERTENCIAS", "UI_NO_POSIBLE_SUMAR_MONTO");
			}else{
				$scope.sumaMonto = $scope.sumaMonto + value.MontoCompraMonedaInstrumento;
			}
		})
		if($scope.sumaCantidad != 0){
			$scope.sumaCantidad = util.bcs_formato($scope.sumaCantidad,'GEN','CANTIDAD',moneda,'','').valor;
		}
		if($scope.sumaMonto != 0 && util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor != 'Error'){
			$scope.sumaMonto = util.bcs_formato($scope.sumaMonto,'GEN','Monto',moneda,'','').valor;
		}else{
			$scope.sumaMonto = 0;
		}
	});

	$scope.ActualizarTotalizador = function(filters){
		const operadores = {
			'eq': '=',
			'neq': '!=',
			'lt': '<',
			'lte': '<=',
			'gt': '>',
			'gte': '>=',
			'contains' : 'lk',
			'doesnotcontain': 'nlk',
			'startswith' : 'wt',
			'endswith' : 'wt',
			'isnull'	: 'inl',
			'isnotnull' : 'innl'
		};

		const tipos = modeloMovimientos().model.fields;
		
		data = {};
		data.p_IdEmpresa				=	$scope.SesionIdEmpresa;
		data.p_IdUsuario				=	$scope.SesionIdUsuario || 1;
		data.p_IdModulo					=	'CUS';
		data.Filtros = filters
		.filter(f => f.field && f.operator && (f.value || f.operator === "isnull" || f.operator === "isnotnull"))
		.map(f => ({
			Campo: f.field,
			Operador: operadores[f.operator] || f.operator,
			Valor: (tipos[f.field]?.type != "sysdate") ? f.value.toString(): (f.value) ? (util.dateSPdate(f.value)).toString() : "",
			Tipo : (tipos[f.field]?.type != "sysdate") ? (tipos[f.field]?.type) : "string"
		})); 

		const filtroMoneda = data.Filtros.find(f => f.Campo === 'MonedaReajuste');
		$scope.MonedaReajusteTotalizador = (filtroMoneda) ? filtroMoneda.Valor : $scope.SesionIdMonedaSistema;

 		bcsServiciosMovTransito.CertificarMovimientoTotalizador
		(data,
		function(respuesta){	


			if (respuesta.P_OUT_TABLA[0]){

				$scope.sumaCantidad = util.bcs_formato(respuesta.P_OUT_TABLA[0].CantidadAgrupada,'GEN','CANTIDAD',$scope.SesionIdMonedaSistema,'','').valor;
				$scope.sumaMonto = util.bcs_formato(respuesta.P_OUT_TABLA[0].MontoAgrupadoMonedaInstrumento,'GEN','Monto',$scope.MonedaReajusteTotalizador,'','').valor;
			}

			else {
				$scope.sumaCantidad = util.bcs_formato($scope.sumaCantidad,'GEN','CANTIDAD',$scope.SesionIdMonedaSistema,'','').valor;
				$scope.sumaMonto = util.bcs_formato($scope.sumaMonto,'GEN','Monto',$scope.MonedaReajusteTotalizador,'','').valor;
			}
		},);  



	}
/******************************************************/
/*** FINAL											***/
/******************************************************/
});