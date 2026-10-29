angular.module('ModuloCustodia').controller('ConsultarEventoCapitalClienteCtrl',
	function($scope,$timeout,$modal,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsBlotterEventoCapitalGrid,bcsServicioPlataforma,$resource,
			bcsServiciosEventoCapital,bcsAlerta,bcsServiciosCustodia,$rootScope)
{	
	
	//*** configuracion de objetos ***//
	
	$scope.filtro = {};
	$scope.data ={};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	if ( $scope.SesionLocale === 'PE' ){
		$scope.MuestraBotonCargarBeneficios		= true;
	}else{
		$scope.MuestraBotonCargarBeneficios		= false;
	};

	//Fechas filtro
//	$scope.filtro.fecInicioCL 		 =  $scope.SesionFechaSistema;
//	$scope.filtro.fecTerminoCL		 =  $scope.SesionFechaSistema;
	
	//var cEEC = this;
//	cEEC.DatosEventoCapital.FechaPago = util.datefix(util.date(bEC.idActualFechaPago).toDate());
	
	//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles
	$scope.oClientePortafolio ={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : 0,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};	
	//Fin GCMBIPAPAS-1	
	
	$scope.data.oConfiguracionTipoEvento={
			tipo : "combobox-tipo-evento-capital",
			configuracion : {},
			filtros : { 	
				IdEstado 	: { "field" : "IdEstado","operator": "eq", "value" : 'ACT'}				
				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
    	
	$scope.data.oConfiguracionEstadoMovimiento={
		tipo : "combobox-estado-custodia",
		configuracion : {},
		filtros : {
			NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : 'CUSTODIA.EVENTO' }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
 	

   	//GRILLA PRINCIPAL
	$scope.confBasicaEventoCapitalCliente = {
		dataSource			: bcsBlotterEventoCapitalGrid.dataSourceEventoCliente(),
		columns				: bcsBlotterEventoCapitalGrid.columnasEventoCliente(),
		rowColorBackground 	: bcsBlotterEventoCapitalGrid.fnColorBk,
		sortable			: true,
		userSettings		: true,
		pageable			: true,
		excel 				: { allPages : true },
		height				: 470,		
		selectable 			: "multiple row",
		filterable			: true,
		navigatable         : true,

		rowColorBackground  :
			function(row) {
				if (row.OperacionesVigentes === 'S' || row.CantidadDerecho < 0) 
					return "red";
				else return "";
			}
	};
	

	//GRILLA FORMAS DE PAGO
	$scope.confEventoCapitalClienteFormaDePago = function(item) {
//		console.log("Id Evento de Capital Cliente : " + Number(row.IdEventoCapitalCliente));
		var dsDef = {
	               	type : "odata",
	               	definition : {
			  			serverFiltering : true, // Filtrado de datos en el servidor.
			  			serverPaging 	: true, // Paginado de los datos en el servidor.
			  			pageSize 		: 25, // Número de registros por página.
			  			filterable 		: true,
			            transport: {
									read: {
											url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=367",
											dataType: "json"
										}
						},
			  			schema : bcsBlotterEventoCapitalGrid.modeloEventoCapitalClienteFormaDePago(),
	               }
		};
			
		dsDef.definition.filter = [
		                           { field: "IdEventoCapitalCliente", operator: "eq", value: Number(item.IdEventoCapitalCliente)}
		                          ]
		                           
		return {
			dataSource			: new bcs.DataSource(dsDef),
			columns				: bcsBlotterEventoCapitalGrid.columnasEventoClienteFormaDePago(),
			sortable			: true,
			pageable			: true,
			autoBind			: true,
			height				: 130,
			selectable 			: "single row"
		}
	};
	
	$scope.HabilitarBotones = function (vars){

		if (vars[0].IdTipoEventoCapital == 'SU' || vars[0].IdTipoEventoCapital == 'PB') //SU: Suscripción OSA    PB: Prepago Bono
		{
			// EN CASO DE TRATARSE DE UNA SUSCRIPCIÓN SE DEBEN ACTIVAR CIERTOS BOTONES.
			
			$scope.ActivaBotonMovimientoFacturar	= true;
			$scope.ActivaBotonCertificar			= true;
			$scope.ActivaBotonNotificar				= true;
			$scope.ActivaBotonAnular				= true;			
		}
		else
		{
			// EN CASO DE TRATARSE DE UNA SUSCRIPCIÓN SE DEBEN DESACTIVAR CIERTOS BOTONES.
			$scope.ActivaBotonMovimientoFacturar	= false;
			$scope.ActivaBotonCertificar			= false;
			$scope.ActivaBotonNotificar				= false;
			$scope.ActivaBotonContraparte 			= false;
			$scope.ActivaBotonFormaDePago			= true; 
			$scope.ActivaBotonActualizarEv			= false;
			$scope.ActivaBotonAnular				= false;	
			// por el momento lo tengo aca, lo debo quitar cuando resuelva el problema de habilitar botón desde grilla secundaria.

			if (vars[0].IdEstado == 'PEN' ){
				$scope.ActivaBotonContraparte = true;
			};

		};		
	};
	
	$scope.HabilitarCertificar = function (vars) {
		if (!vars || vars.length === 0) return false;
	
		return vars.every(function(item) {
			return item.IdTipoEventoCapital === 'SU' || item.IdTipoEventoCapital === 'PB';
		});
	};

	
	$scope.HabilitarBotonesFormaPago = function (vars){
		$scope.ActivaBotonFormaDePago			= true;
	};	
   	//********* fin configuracion de objetos *********//
	
	
	// CREAR NUEVO EVENTO CAPITAL
	$scope.btnIngresar = function(oEventoCapital){	
		Accion = 'Crear' 
		$scope.abrirModal(oEventoCapital);		
	};	
	
	// MODIFICAR EVENTO CAPITAL
	$scope.btnModificar = function(oEventoCapital){
		if ($scope.seleccionEventoCapitalCliente === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
		{
			Accion = 'Modificar' 
			$scope.abrirModal(oEventoCapital);
		}
	};
	
	
	// MODAL DE EVENTO CAPITAL CLIENTE
	$scope.abrirModal = function(oEventoCapital)
	{
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if ($scope.filtro.TipoEvento.IdTipoEventoCapital === 'PB') 
		{	// Para PB(Prepago Bono) usa un formulario especial
			var datos = {};
			datos.accion = Accion;
			datos.oEventoCapital = oEventoCapital;
			
			var id1 = bcsComunes.ventanaAbrir("InscribirClientePrepagoBono",datos,false,false,$scope.idVentana);
		}else{
			var auxDate3;
			auxDate3 =  util.dateOdata(util.date($scope.bEC.idActualFechaPago).hours(0).minutes(0).seconds(0).toDate());
			var id1 = bcsComunes.ventanaAbrir("CrearEventoCapitalClienteModal",{Accion: Accion,EventoCapitalCliente:oEventoCapital, IdEvento : $scope.filtro.TipoEvento, IdInstrumento: $scope.bEC.IdInstrumento, IdFechaPago: auxDate3},false,false,$scope.idVentana);		

		}	
		
		var promesa=bcsComunes.ventanaPromesa(id1); 
        promesa.then(function(data){ 
        	if (data === true){
        		$scope.gridApi.dataSource.read();  //Refrescar grilla para visualizar cambios.
			}
    	}); 
	};
	
	// MODIFICAR FORMA DE PAGO
	$scope.btnFormaDePago = function(oEventoCapitalFormaDePago){
		if ($scope.seleccionEventoCapitalCliente === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}else{
			if ($scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital == 'DV' || $scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital == 'CC' || $scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital == 'RC'){
				Accion = 'ModificarFormaDePago' 
				$scope.abrirModalFormaPago(oEventoCapitalFormaDePago);				
			}else{
				bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('UI_MEDIOS_DE_PAGO_SOLO_APLICAN_PARA_DIV_DINERO_CORTE_CUPON_Y_REP_CAPITAL'));
				return;
			}
		}
	};	
	
	// MODAL DE EVENTO CAPITAL CLIENTE FORMA DE PAGO
	$scope.abrirModalFormaPago = function(oEventoCapitalFormaDePago){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		var auxDate3;
//		auxDate3 =  util.dateOdata(util.date($scope.bEC.idActualFechaPago).hours(0).minutes(0).seconds(0).toDate());
		var id1 = bcsComunes.ventanaAbrir("ModificaFormaPagoEventoCapitalClienteModal",{IdEventoCliente:$scope.seleccionEventoCapitalCliente["0"].IdEventoCapitalCliente, IdPortafolio: $scope.seleccionEventoCapitalCliente["0"].IdPortafolio ,IdMoneda: $scope.seleccionEventoCapitalCliente["0"].IdMoneda, EventoCapitalCliente:oEventoCapitalFormaDePago},false,false,$scope.idVentana);

		var promesa=bcsComunes.ventanaPromesa(id1); 
        promesa.then(function(data){ 
        	if (data === true)
			{
				//Refrescar grilla para visualizar cambios.
        		
        		$scope.gridApi.dataSource.read();
        		//buscarEventoCapitalCliente();
			}
    	}); 
	};
		
	// SUSCRIBIR EVENTO CAPITAL

	$scope.data.btnSuscribirModificar = function(){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}

		if ($scope.seleccionEventoCapitalCliente && $scope.seleccionEventoCapitalCliente.IdCliente !== null){
			if($scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital == 'EP'){
				var oEventoCapital = $scope.seleccionEventoCapitalCliente;
				Accion = 'Modificar'
				oEventoCapital.seleccionEventoCapital = $scope.bEC.seleccionEventoCapital
				$scope.abrirModalSucripcion(oEventoCapital[0]);
				return;
			}
			bcsComunes.msg('error',$translate.instant("UI_ERROR"),$translate.instant('UI_VALIDA_SUSCRIPCION'));
		} else {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"),$translate.instant('UI_VALIDA_SUSCRIPCION_SELECCION'));
		}
	}

	$scope.data.btnSuscribirCrear = function(){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}

		// Suscripción OSAS sin custodia inicial
		const tiposSuscripcionOSAManual = ['EP','SU'];
		const eventoCapitalSeleccionado = $scope.bEC?.seleccionEventoCapital;

		if(!eventoCapitalSeleccionado){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"),$translate.instant('UI_VALIDA_SUSCRIPCION_SELECCION'));
			return;
		}

		const idTipoEventoSeleccionado = eventoCapitalSeleccionado[0]?.IdTipoEventoCapital;
		const idTipoEventoCapitalFiltro = $scope.filtro?.TipoEvento?.Codigo;
		const nemoEventoSeleccionado = eventoCapitalSeleccionado[0]?.Nemo;
		const nemoEventoCapitalFiltro = $scope.filtro?.instrumentoObjeto?.Nemotecnico;

		if (!tiposSuscripcionOSAManual.includes(idTipoEventoSeleccionado)) {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"),$translate.instant('UI_VALIDA_SUSCRIPCION_TIPO_EVENTO'));
			return;
		}
		
		if(!tiposSuscripcionOSAManual.includes(idTipoEventoCapitalFiltro) || nemoEventoSeleccionado != nemoEventoCapitalFiltro){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"),$translate.instant('UI_VALIDA_SUSCRIPCION_FILTROS_EVENTO'));
			return;
		}

		Accion = 'Crear'
		$scope.data.oEventoCapital={};
		$scope.data.oEventoCapital.seleccionEventoCapital = eventoCapitalSeleccionado;
		$scope.data.TipoEventoTXT		= eventoCapitalSeleccionado[0].TipoEvento;
		$scope.data.idInstrumentoTXT	= nemoEventoSeleccionado;	
		$scope.data.kngSecuenciaTXT		= eventoCapitalSeleccionado[0].Secuencia;
		$scope.data.DescripcionTXT 		= eventoCapitalSeleccionado[0].Descripcion;		
		$scope.abrirModalSucripcion($scope.data);
		return;
	}

	$scope.data.opcionesSuscribir = [
		{
			titulo : $translate.instant("UI_SUSCRIBIR"),
			funcion : $scope.data.btnSuscribirModificar
		},
		{
			titulo : $translate.instant("UI_SUSCRIBIR_CREAR"),
			funcion : $scope.data.btnSuscribirCrear
		} ];

	
	
	
	// MODAL SUSCRIPCION
	$scope.abrirModalSucripcion = function(oEventoCapital){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		var auxDate3;
		auxDate3 =  util.dateOdata(util.date($scope.bEC.idActualFechaPago).hours(0).minutes(0).seconds(0).toDate());
		var id1 = bcsComunes.ventanaAbrir("CrearSuscripcionClienteModal",{Accion: Accion,EventoCapitalCliente:oEventoCapital, IdEvento : $scope.filtro.TipoEvento, IdInstrumento:$scope.filtro.instrumentoObjeto, IdFechaPago: auxDate3},true,true);

		var promesa=bcsComunes.ventanaPromesa(id1); 
        promesa.then(function(data){ 
        	if (data === true)
			{
				//Refrescar grilla para visualizar cambios.
        		
        		$scope.gridApi.dataSource.read();
        		//buscarEventoCapitalCliente();
			}
        	$scope.seleccionEventoCapitalCliente = undefined;// QUITAR
    	}); 
	}

	
/******************************************
* BUSCAR
*******************************************/
/*
	$scope.buscarEventoCapitalCliente = function(){
		var filters = new Array();
		
		try{
        	if ($scope.filtro.TipoEvento.IdTipoEventoCapital !== undefined && $scope.filtro.TipoEvento.IdTipoEventoCapital != "")
        		filters.push({ field: "IdTipoEventoCapital", operator: "contains", value: $scope.filtro.TipoEvento.IdTipoEventoCapital });
		}catch(e){
		}
		
		try {
        	if ($scope.filtro.instrumentoObjeto !== undefined){
        		if ($scope.filtro.instrumentoObjeto.IdInstrumento > 0) {
        			filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.filtro.instrumentoObjeto.IdInstrumento) });
        		}
        	}
		}catch(e){
		}	
		
		try{
        	if ($scope.filtro.estadoObj.IdEstado !== undefined && $scope.filtro.estadoObj.IdEstado != "")
        		filters.push({ field: "IdEstado", operator: "contains", value: $scope.filtro.estadoObj.IdEstado });
		}catch(e){
		}
		
		try{
        	if ($scope.filtro.TipoEntidad.IdTipoSocioDeNegocio !== undefined && $scope.filtro.TipoEntidad.IdTipoSocioDeNegocio != "")
        		filters.push({ field: "IdTipoSocioDeNegocio", operator: "contains", value: $scope.filtro.TipoEntidad.IdTipoSocioDeNegocio });
		}catch(e){
		}
		
		try{
        	if ($scope.filtro.TipoEntidad.IdTipoSocioDeNegocio !== undefined && $scope.filtro.TipoEntidad.IdTipoSocioDeNegocio != "")
        		filters.push({ field: "IdTipoSocioDeNegocio", operator: "contains", value: $scope.filtro.TipoEntidad.IdTipoSocioDeNegocio });
		}catch(e){
		}
		
		try{
			if ($scope.oConfiguracionSocioNegocio.seleccionado.IdCliente > 0 ){
				filters.push({ field: "IdCliente", operator: "eq", value: $scope.oConfiguracionSocioNegocio.seleccionado.IdCliente});
//	    		seleccOk = true;
			}
		}catch(e){
		}
		
		try { // se mete dentro de un try ya q cuando no se ingresa un valor al objeto, este no se lee
        	if ($scope.bEC.idActualNumeroDividendo !== undefined && $scope.bEC.idActualNumeroDividendo !=='' && $scope.bEC.idActualNumeroDividendo !==0){
	    		try{
		    		if (Number($scope.bEC.idActualNumeroDividendo)){
			    		var auxNum = Number($scope.bEC.idActualNumeroDividendo);
			    		seleccOk = true;
		        		filters.push({ field: "NumeroDividendo", operator: "eq", value: auxNum });
		    		}
	    		}catch (e){
	    		}
        	}
    	} catch(e) {
    	}
    	
    	
    	var auxDate1;
    	var auxDate2;
    	if ($scope.bEC.idActualFechaPago !== undefined && $scope.bEC.idActualFechaPago !== "" && $scope.bEC.idActualFechaPago !== null){
			//var auxDate1 =kendo.parseDate(moment($scope.filtro.fecInicio).format('YYYY-MM-DD') + "T00:00:00" )
    		auxDate1 =  util.dateOdata(util.date($scope.bEC.idActualFechaPago).hours(0).minutes(0).seconds(0).toDate());
			filters.push({ field: "FechaPago", operator: "gte", value: auxDate1 });
    	}
    	
    	if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null){
    	 	//var auxDate2 = kendo.parseDate(moment($scope.filtro.fecTermino).format('YYYY-MM-DD') + "T23:59:59" )
    		auxDate2 = util.dateOdata(util.date($scope.bEC.idActualFechaPagoHasta).add(23,'hours').minutes(59).seconds(59).toDate());
    	 	if (auxDate2 < auxDate1){
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    	 		return;
    	 	}
    		filters.push({ field: "FechaPago", operator: "lte", value: auxDate2 });
    	}
		$scope.gridApi.dataSource.filter(filters);
	};
*/	
	
	//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles		
	//$scope.buscar = function(filtros)
	$scope.buscarEventoCapitalCliente = function()
	{
    	var auxFecDesde;
    	var auxFecHasta;
		var antiguoVal = $scope.valoresAnteriores;

    	if ($scope.bEC.idActualFechaPago !== undefined && $scope.bEC.idActualFechaPago !== "" && $scope.bEC.idActualFechaPago !== null){			
    		auxFecDesde =  util.dateOdata(util.date($scope.bEC.idActualFechaPago).hours(0).minutes(0).seconds(0).toDate());			
    	}
    	
    	if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null){    	 	
    		auxFecHasta = util.dateOdata(util.date($scope.bEC.idActualFechaPagoHasta).add(23,'hours').minutes(59).seconds(59).toDate());
    	 	if (auxFecHasta < auxFecDesde){
    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
    	 		return;
    	 	}    		
    	}
    	
		var datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario	 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'CUS',
				p_IdEmpleadoConsulta	: $scope.SesionIdEmpleado,
															
				//Parametros de ventana
				p_IdCliente			: Number($scope.oClientePortafolio.seleccionado.IdCliente || 0),
				p_TipoEvento		: $scope.filtro.TipoEvento.IdTipoEventoCapital,
				p_Instrumento		: Number($scope.filtro.instrumentoObjeto == undefined ? 0 : ($scope.filtro.instrumentoObjeto.ConteoNemotecnico === undefined || $scope.filtro.instrumentoObjeto.ConteoNemotecnico === 1) ? $scope.filtro.instrumentoObjeto.IdInstrumento : 0),
				p_Nemo				: $scope.filtro.instrumentoObjeto == undefined ? "" : ($scope.filtro.instrumentoObjeto.ConteoNemotecnico > 1) ? $scope.filtro.instrumentoObjeto.Nemotecnico : "",	
				p_Estado			: $scope.filtro.estadoObj.IdEstado || 0,
				p_TipoSocioNegocio	: $scope.filtro.TipoEntidad.IdTipoSocioDeNegocio == undefined ? 0 : $scope.filtro.TipoEntidad.IdTipoSocioDeNegocio,
				p_NumeroDividendo	: Number($scope.bEC.idActualNumeroDividendo == undefined ? 0 : $scope.bEC.idActualNumeroDividendo),
				p_FechaPagoDesde	: auxFecDesde,
				p_FechaPagoHasta	: auxFecHasta,
				p_Secuencia			: Number( $scope.bEC.Secuencia == undefined ? 0 :$scope.bEC.Secuencia)
					
																			
		}
		var tipo = $scope.filtro.TipoEvento.IdTipoEventoCapital;
		$rootScope.$broadcast('evento:tipoSeleccionado', tipo);

		if ($rootScope.PrimerValNull === false && $rootScope.ItemSeleccionado === true) {
			if (datos.p_NumeroDividendo !== antiguoVal) {
				$rootScope.limpiarSeleccionado();
				$rootScope.ItemSeleccionado = false
			}
		}

		if ($rootScope.ItemSeleccionado === false) {
			datos.p_Secuencia = 0
		}

		if ($rootScope.PrimerValNull === true) {
			$rootScope.PrimerValNull = false
		}

		$scope.valoresAnteriores = datos.p_NumeroDividendo;

		$scope.gridApi.dataSource.removeAll();
		$scope.gridApi.dataSource.setCallParameters(datos)
	};
//Fin GCMBIPAPAS-1
	
	
	$scope.limpiarFiltros = function() {
		$scope.bEC.idActualEvento.TipoEvento			= 0;
		$scope.instrumento 								= "";
		$scope.filtro.estado 							= 0;
		$scope.filtro.instrumentoObjeto 				= undefined;
		$scope.bEC.NemoActual			 				= "-1";
		$scope.formData.tipoSocioNegocio 				= 0;
		//Inicio GCMBIPAPAS-1 Filtro de clientes por Perfiles	
		//$scope.oConfiguracionSocioNegocio.seleccionado 	= false;	
		//$scope.oConfiguracionSocioNegocio.idActual 		= false;
		$scope.oClientePortafolio.seleccionado 			= false;		
		$scope.oClientePortafolio.idActual 				= false;
		//Fin GCMBIPAPAS-1
		$scope.bEC.idActualNumeroDividendo				= "";
		
		$scope.filtro.fecInicio 		 				=  $scope.SesionFechaSistema;
		$scope.filtro.fecTermino 		 				=  $scope.SesionFechaSistema;
		//$scope.filtro.fecTermino		 				=  util.date($scope.filtro.fecTermino).add(5,'days').toDate();

		$scope.bEC.idActualFechaPago 					= $scope.filtro.fecInicio;
		$scope.bEC.idActualFechaPagoHasta 				= $scope.filtro.fecTermino;	
		$scope.bEC.Secuencia                            = 0;
		$scope.bEC.idActualNumeroDividendo              = 0;
	
		
		$scope.gridApi.dataSource.removeAll();
		$scope.seleccionEventoCapitalCliente = false;
	};
   	
	
/*************************************************************************
* ELIMINAR 
**************************************************************************/	

  	$scope.btnEliminar = function(){
  		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
	if ($scope.seleccionEventoCapitalCliente != undefined){	
		if (($scope.seleccionEventoCapitalCliente[0].IdEstado == 'VIG') || ($scope.seleccionEventoCapitalCliente[0].IdEstado == 'PRO')){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_EVENTO_CAPITAL") + " Ha seleccionado " + $scope.seleccionEventoCapitalCliente.length + " registro(s) seleccionados");		
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};	
					bcsComunes.ventanaOverlay($scope.idVentana, true); 
					
					/*datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';
					datos.p_UsaReglasNegocio = 'S';
	
					datos.p_IdEventoCapitalCliente				= $scope.seleccionEventoCapitalCliente[0].IdEventoCapitalCliente || '';     
					*/
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';
					datos.p_UsaReglasNegocio = 'S';
					
					var eventosCapitalCliente = [];
					for(var i = 0; i<$scope.seleccionEventoCapitalCliente.length; i++){
						var item = $scope.seleccionEventoCapitalCliente[i];
						eventosCapitalCliente.push({
							"p_IdEventoCapital" : Number(item.IdEventoCapital) || 0,
							"p_IdEventoCapitalCliente" : Number(item.IdEventoCapitalCliente) || 0
						});
					}
					datos.p_EventoCapital = eventosCapitalCliente;
					bcsServiciosEventoCapital.EliminarEventoCapitalCliente(datos,successEliminarEventoCapitalCliente,errorEliminarEventoCapitalCliente);
				
				}
				else 
				{		
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				}
			});
			return;   
   		}
		else
		{	
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_VIGENTE"));
		}
   
	}
	else
	{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
	}
  return;
   };
		

/*************************************************************************
* ELIMINAR EXITOSO
**************************************************************************/
	var successEliminarEventoCapitalCliente = function(respuesta)
	{
		bcsComunes.ventanaOverlay($scope.idVentana, false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if ( respuesta.P_OUT_ERRORESCONTADOR!== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_INGRESO_EXITO"));
			$scope.gridApi.dataSource.read();		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULACION_EVENTO"));
	    }
	};
	

/*************************************************************************
* ELIMINAR CON ERRORES
**************************************************************************/	
	var errorEliminarEventoCapitalCliente = function(respuesta)
	{
		bcsComunes.ventanaOverlay($scope.idVentana, false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULACION_EVENTO"));
		$scope.data.accionDisabled = true;
	};
   

/*************************************************************************
* ANULAR SUSCRIPCION OSA  
**************************************************************************/	
  $scope.btnAnula = function(){
  	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
	if ($scope.bEC.seleccionEventoCapital != undefined){
		if ((($scope.seleccionEventoCapitalCliente[0].IdEstado == 'CRT') ||
		($scope.seleccionEventoCapitalCliente[0].IdEstado == 'NOT') || 
		($scope.seleccionEventoCapitalCliente[0].IdEstado == 'VIG')) && 
		($scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital == 'SU')){
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_NOTIFICAR_ANULA_OSA"));
			t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) {
				var datos = {};	
				var datosPrecio = {};
				bcsComunes.ventanaOverlay($scope.idVentana, true); 
				
				datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
				datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
				datos.p_IdModulo = 'CUS';					

				datos.p_IdEventoCapitalCliente	= Number($scope.seleccionEventoCapitalCliente[0].IdEventoCapitalCliente) || 0;			                   		
		   
				bcsServiciosEventoCapital.AnulaSuscripcionOSA(datos,successAnulaSuscripcionOSA,errorAnulaSuscripcionOSA);				
			}
			else
			{
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			}
			});
			return;
		}
		else
		{
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("UI_SOLO_ANULA_SUSCRIPCION_ESTADO_CER_NOT"));
		}
	}
	else
	{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
	}
  return;
   };
   

/*************************************************************************
* ANULAR SUSCRIPCION OSA  EXITOSO
**************************************************************************/
	var successAnulaSuscripcionOSA = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		bcsComunes.ventanaOverlay($scope.idVentana, false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_ANULA_SUSCRIPCION_EXITO"));
			//bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.gridApi.dataSource.read();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULA_SUSCRIPCIÓN_ERROR"));
	    }
	};


/*************************************************************************
* ANULAR SUSCRIPCION OSA  CON ERRORES
**************************************************************************/	
	var errorAnulaSuscripcionOSA = function(respuesta)
	{
		bcsComunes.ventanaOverlay($scope.idVentana, false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULA_SUSCRIPCIÓN_ERROR"));
		$scope.data.accionDisabled = true;
	};		


/*************************************************************************
* CERTIFICAR OSA  Y PREPAGO BONO(PB)
**************************************************************************/	
  $scope.btnCertificar = function()
  {
	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
	if ($scope.seleccionEventoCapitalCliente != undefined)
	{
		
		if ($scope.filtro.TipoEvento.IdTipoEventoCapital == 'SU' || $scope.filtro.TipoEvento.IdTipoEventoCapital == 'PB')   
		{
			var datos = {};	
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERTIFICAR_EVENTO_CAPITAL"));
			t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) 
			{
				
				//CERTIFICA SUSCRIPCION OSA
				if ($scope.filtro.TipoEvento.IdTipoEventoCapital == 'SU')
				{
					var eventosVigentes = true;
					$.each($scope.seleccionEventoCapitalCliente, function(key, value) {
						if (value.IdEstado !== 'VIG') 
						{
							eventosVigentes = false;
							return false;
						}
					});
					if (eventosVigentes)
					{	
						var datosPrecio = {};
						bcsComunes.ventanaOverlay($scope.idVentana, true); 
						
						datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
						datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
						datos.p_IdModulo = 'CUS';	
						datos.p_IdEventoCapital	= [];
						
						for (i = 0; i < $scope.seleccionEventoCapitalCliente.length; i++) 
						{
							var datosEventoCapitalCliente = {};
							datosEventoCapitalCliente.IdEventoCapitalCliente = $scope.seleccionEventoCapitalCliente[i].IdEventoCapitalCliente;
							datos.p_IdEventoCapital.push(datosEventoCapitalCliente);
						};                  		
				   
						bcsServiciosEventoCapital.CertificaSuscripcionOSA(datos, successCertificarEventoCapital, errorCertificarEventoCapital);	
					}					
					else
					{
						bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("UI_SOLO_CERTIFICA_SUSCRIPCION_ESTADO_VIG"));					
					}
				} 
				
				
				//CERTIFICA PREPAGO BONO
				if ($scope.filtro.TipoEvento.IdTipoEventoCapital == 'PB')
				{				
					bcsComunes.ventanaOverlay($scope.idVentana, true);
					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa;
					datos.p_IdUsuario = $scope.SesionIdUsuario;
					datos.p_IdModulo  = 'CUS';
					datos.p_IdEventoCapital = Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;
					datos.p_TipoEventoCapital = 'PB';
					datos.p_EventoCapitalCliente = [];
					
					for (i = 0; i < $scope.seleccionEventoCapitalCliente.length; i++) 
					{
						var datosEventoCapitalCliente = {};
						datosEventoCapitalCliente.p_IdEventoCapitalCliente = $scope.seleccionEventoCapitalCliente[i].IdEventoCapitalCliente;
						datos.p_EventoCapitalCliente.push(datosEventoCapitalCliente);
					};
					
					bcsServiciosEventoCapital.CertificarEventoCapital(datos, successCertificarEventoCapital, errorCertificarEventoCapital);
					
				}
				
			}
			else
			{
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			}
			});
			return;
		}

	}
	else
	{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
	}
  return;
};
   

/*************************************************************************
* CERTIFICAR EXITOSO
**************************************************************************/
   
	var successCertificarEventoCapital = function(respuesta)
	{
		$scope.data.accionDisabled = true;
		
    	if (respuesta.P_OUT_ERRORESCONTADOR == 0)
    	{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_CERTIFICAR_EXITO"), conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana, false);
			$scope.gridApi.dataSource.read();
		}
		else
		{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_CERTIFICAR_ERROR"), conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana, false);
		}
    	
	};


/*************************************************************************
* CERTIFICAR CON ERRORES
**************************************************************************/	
	var errorCertificarEventoCapital = function(respuesta)
	{	
		$scope.data.accionDisabled = true;		
		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_CERTIFICAR_ERROR"),conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
		bcsComunes.ventanaOverlay($scope.idVentana, false);
	};
	
/*************************************************************************
* NOTIFICACION
**************************************************************************/
	
/*************************************************************************
* NOTIFICACION OSA  
**************************************************************************/
		   
  $scope.btnNotificar = function(){
  	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
	if ($scope.seleccionEventoCapitalCliente != undefined){
		if (($scope.seleccionEventoCapitalCliente[0].EstadoNotificacion == 'N') && ($scope.seleccionEventoCapitalCliente[0].IdPortafolio > 0) && ($scope.seleccionEventoCapitalCliente[0].CantidadDerecho > 0)){
			if($scope.seleccionEventoCapitalCliente[0].IdEstado == 'PRO' || $scope.seleccionEventoCapitalCliente[0].IdEstado == 'CER'){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_NOTIFICAR_SUSCRIPCION"));
				t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};	
					var datosPrecio = {};
					bcsComunes.ventanaOverlay($scope.idVentana,true);
					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';					

					datos.p_IdsEventoCapital					= [{"p_IdEventoCapital" : Number($scope.seleccionEventoCapitalCliente[0].IdEventoCapital) || 0}];
			   		//datos.p_IdTipoEventoCapital				= $scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital;			                   		
			   		datos.p_IdsEventoCapitalCliente			= [{"p_IdEventoCapitalCliente" : Number($scope.seleccionEventoCapitalCliente[0].IdEventoCapitalCliente) || 0}];
			   		
			   		//datos.p_IdInstrumentoFiltro		= $scope.filtro.instrumentoObjeto == undefined ? 0 : $scope.filtro.instrumentoObjeto;
			   		//datos.p_IdEstadoFiltro			= $scope.filtro.estadoObj.IdEstado == 0 ? '' : $scope.filtro.estadoObj.IdEstado;
			   		//datos.p_FechaPagoDesdeFiltro	= util.dateSPdate($scope.bEC.idActualFechaPago);
			   		//datos.p_FechaPagoHastaFiltro	= util.dateSPdate($scope.bEC.idActualFechaPagoHasta);
			   		
//					bcsServiciosEventoCapital.NotificaSuscripcionOSA(datos,successNotificaSuscripcionOSA,errorNotificaSuscripcionOSA);
					bcsServiciosEventoCapital.NotificarEventoCapital(datos,
								function(respuesta){	  		
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
									$scope.gridApi.dataSource.read();
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
								});				
				}
				else
				{
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				}
				});
				return;
			}
			else{	
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_NO_VALIDA"));
			}
		}
		else
		{
			var msje = $scope.seleccionEventoCapitalCliente.EstadoNotificacion == 'S' ? "UI_CLIENTE_YA_NOTIFICADO" : "UI_SOLO_NOTIFICA_CLIENTES";
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant(msje));
		}
	}
	else
	{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
	}
  return;
   };
	
   

/*************************************************************************
* NOTIFICACION EXITOSO OSA
**************************************************************************/
/*	var successNotificaSuscripcionOSA = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_NOTIFICAR_SUSCRIPCION_EXITO"));
			//bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.gridApi.dataSource.read();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERTIFICAR_SUSCRIPCIÓN_ERROR"));
	    }
	};
	*/
/*************************************************************************
* NOTIFICACION CON ERRORES OSA
**************************************************************************/	
/*	var errorNotificaSuscripcionOSA = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERTIFICAR_SUSCRIPCIÓN_ERROR"));
		$scope.data.accionDisabled = true;
	};	
*/

	
/*************************************************************************
* FACTURAR 
**************************************************************************/	

  	$scope.btnMovimientoFacturar = function(){
  		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
	if ($scope.seleccionEventoCapitalCliente != undefined)
	{		
		var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_FACTURAR_EVENTO_CAPITAL"));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) 
			{
				var datos = {};
				//FACTURAR SUSCRIPCION OSA
				if ($scope.filtro.TipoEvento.IdTipoEventoCapital == 'SU')
				{
					datos.p_IdEventoCapitalCliente = [];
					
					for(i = 0; i < $scope.seleccionEventoCapitalCliente.length; i++){
						//if ($scope.seleccionEventoCapitalCliente[i].IdEstado != 'CRT'){
						//	bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_ERROR_MOVIMIENTO_DISTINTO_CER"));
						//}else{
							bcsComunes.ventanaOverlay($scope.idVentana, true);
							datos.p_IdEventoCapitalCliente.push($scope.seleccionEventoCapitalCliente[i].IdEventoCapitalCliente || '');					
						//}	
					}
					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';					
					bcsServiciosEventoCapital.FacturarEventoCapitalClienteMasivo(datos,successFacturarEventoCapitalCliente,errorFacturarEventoCapitalCliente);
				}
				 	
				//FACTURAR PREPAGO BONO
				if ($scope.filtro.TipoEvento.IdTipoEventoCapital == 'PB')
				{	
					bcsComunes.ventanaOverlay($scope.idVentana, true);
					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa;
					datos.p_IdUsuario = $scope.SesionIdUsuario;
					datos.p_IdModulo  = 'CUS';
					datos.p_IdEventoCapital = Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;					
					datos.p_EventoCapitalCliente = [];
					
					for (i = 0; i < $scope.seleccionEventoCapitalCliente.length; i++) 
					{
						var datosEventoCapitalCliente = {};
						datosEventoCapitalCliente.p_IdEventoCapitalCliente = $scope.seleccionEventoCapitalCliente[i].IdEventoCapitalCliente;
						datos.p_EventoCapitalCliente.push(datosEventoCapitalCliente);
					};
					
					bcsServiciosEventoCapital.EnviarFacturarPrepagoBono(datos, successFacturarEventoCapitalCliente, errorFacturarEventoCapitalCliente);
				}
				
				
			}
			else 
			{		
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			}
		});
		
		
  
	}
	else
	{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
	}
	
	return;
  
   };
		

/*************************************************************************
* FACTURAR EXITOSO
**************************************************************************/
	var successFacturarEventoCapitalCliente = function(respuesta)
	{	
		$scope.data.accionDisabled = true;
		
    	if (respuesta.P_OUT_ERRORESCONTADOR == 0)
    	{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_FACTURAR_EXITO"), conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana, false);
			$scope.gridApi.dataSource.read();
		}
		else
		{	
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_FACTURAR_ERROR"), conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
			bcsComunes.ventanaOverlay($scope.idVentana, false);
		}
	};
	

/*************************************************************************
* FACTURAR CON ERRORES
**************************************************************************/	
	var errorFacturarEventoCapitalCliente = function(respuesta)
	{	
		$scope.data.accionDisabled = true;		
		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_CERTIFICAR_ERROR"),conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
		bcsComunes.ventanaOverlay($scope.idVentana, false);
	};
   
	
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
    
	//Fechas filtro
	$scope.filtro.fecInicio 		 =  $scope.SesionFechaSistema;
	$scope.filtro.fecTermino		 =  $scope.SesionFechaSistema;
	
/*************************************************************************
* CONTRAPARTE
**************************************************************************/	
  $scope.btnContraparte = function(){
  	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
	if ($scope.bEC.seleccionEventoCapital != undefined){
		if ($scope.seleccionEventoCapitalCliente[0].IdEstado == 'PEN'){
					
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ACTUALIZAR_CONTRAPARTE"));
			t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) {
				var datos = {};	
				var datosPrecio = {};
				bcsComunes.ventanaOverlay($scope.idVentana,true);
				
				datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
				datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
				datos.p_IdModulo = 'CUS';					

				datos.p_IdInstrumento					= Number($scope.seleccionEventoCapitalCliente[0].IdInstrumento) || 0;			                   		
				datos.p_IdEventoCapital					= Number($scope.seleccionEventoCapitalCliente[0].IdEventoCapital) || 0;			                   		
				datos.p_IdTipoEventoCapital				= $scope.seleccionEventoCapitalCliente[0].IdTipoEventoCapital;
				datos.p_IdEventoCapitalCliente			= Number($scope.seleccionEventoCapitalCliente[0].IdEventoCapitalCliente) || 0;	
		   
				bcsServiciosEventoCapital.ActualizaContraparte				
							(datos,
							function(respuesta){	  		
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
								$scope.gridApi.dataSource.read();
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana,false);
							});
								
			}
			else
			{
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			}
			});
			return;
		}
		else
		{
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("UI_SOLO_ACTUALIZA_CONTRAPARTE_ESTADO_PEN"));
		}
	}
	else
	{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
	
	}
  return;
   };

    //**************************************
	//************ CARGA MASIVA ************
	//**************************************
	$scope.CargaMasiva = function()
	{
		bcsComunes.ventanaAbrir("CargaMasivaCustodia", {} );
	};
	
	$scope.cargarArchivoISFUT = function(){
		var datosArchivo = {codigoArchivo: 'CARGA_CLIENTES_ISFUT' || ''};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,$scope.idVentana);				
		 var promesa = bcsComunes.ventanaPromesa(id1);
		 
		 promesa.then(function(data){
			 //El objeto data, contiene la informacion del archivo en formato JSON.
			 
			 if(!data || !data.fileData)
			 {
				 return;
			 }
			 var cargaMasiva = [];
			 var cargaMasivaDetalle = [];			 
			 var idRegistro = 1;
			 
			 
			 cargaMasiva.push({
					// IdCargaMasiva         : 1,
					IdEmpresa	: $scope.SesionIdEmpresa,
					IdUsuario	: $scope.SesionIdUsuario,
					// CantidadRegistros	: idRegistro,
			    	NombreArchivo	      : data.fileName,
					FechaCarga	: util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)),
					CodigoTipoCargaMasiva : 'CARGA_CLIENTES_ISFUT' || ''
			 });
			 $.each(data.fileData, function(key, value){
				 if(value.IGKIdEventoCapitalCliente != "" && !isNaN(value.IGKIdEventoCapitalCliente)){
					 cargaMasivaDetalle.push({IdEventoCapitalCliente: value.IGKIdEventoCapitalCliente.toString()
											,IdTipoEventoCapital: value.IGKIdTipoEventoCapital.toString()
											,NumeroDividendo: value.IGKNumeroDividendo.toString()
											,Secuencia: value.IGKSecuencia.toString()
											,TipoSocioNegocio: value.IGKTipoSocioNegocio.toString()
											,CodigoPortafolio: value.IGKCodigoPortafolio.toString()
											,IdClasificacionTipoInstrumento: value.IGKIdClasificacionTipoInstrumento.toString()
											,Nemo: value.IGKNemo.toString()
											,ISIN: value.IGKISIN.toString()
											,NemoNuevo: value.IGKNemoNuevo.toString()
											,IdConceptoGarantia: value.IGKIdConceptoGarantia.toString()
											,TipoCartera: value.IGKTipoCartera.toString()
											,DocumentoIdentidad: value.IGKDocumentoIdentidad.toString()
											,NombreCliente: value.IGKNombreCliente.toString()
											,ConfiguracionNotificacion: value.IGKConfiguracionNotificacion.toString()
											,TienePreferenciaDePago: value.IGKTienePreferenciaDePago.toString()
											,MontoCapital: value.IGKMontoCapital.toString()
											,Precio: value.IGKPrecio.toString()
											,Factor: value.IGKFactor.toString()
											,FactorNuevo: value.IGKFactorNuevo.toString()
											,PrecioNuevo: value.IGKPrecioNuevo.toString()
											,CantidadNueva: value.IGKCantidadNueva.toString()
											,MontoInteres: value.IGKMontoInteres.toString()
											,MontoReajuste: value.IGKMontoReajuste.toString()
											,CantidadDerecho: value.IGKCantidadDerecho.toString()
											,Total: value.IGKTotal.toString()
											,MontoRetencion: value.IGKMontoRetencion.toString()
											,CantidadDerechoArchivo: value.IGKCantidadDerechoArchivo.toString()
											,AccionesDisponiblesArchivo: value.IGKAccionesDisponiblesArchivo.toString()
											,DiferenciaCantidadDerecho: value.IGKDiferenciaCantidadDerecho.toString()
											,DiferenciaAccionesCantidadNueva: value.IGKDiferenciaAccionesCantidadNueva.toString()
											,MontoBeneficioArchivo: value.IGKMontoBeneficioArchivo.toString()
											,DiferenciaMontoBeneficio: value.IGKDiferenciaMontoBeneficio.toString()
											,MontoInteresArchivo: value.IGKMontoInteresArchivo.toString()
											,MontoCapitalArchivo: value.IGKMontoCapitalArchivo.toString()
											,DiferenciaMontoInteres: value.IGKDiferenciaMontoInteres.toString()
											,DiferenciaMontoCapital: value.IGKDiferenciaMontoCapital.toString()
											,MontoRetencionArchivo: value.IGKMontoRetencionArchivo.toString()
											,DiferenciaMontoRetencion: value.IGKDiferenciaMontoRetencion.toString()
											,IdMoneda: value.IGKIdMoneda.toString()
											,FechaPago: value.IGKFechaPago.toString()
											,CodigoUsuarioAgente: value.IGKCodigoUsuarioAgente.toString()
											,NombreAgente: value.IGKNombreAgente.toString()
											,IdBoveda: value.IGKIdBoveda.toString()
											,IdEstado: value.IGKIdEstado.toString()
											,Origen: value.IGKOrigen.toString()
											,EstadoNotificacion: value.IGKEstadoNotificacion.toString()
											,NombreMedioDePago: value.IGKNombreMedioDePago.toString()
											,TipoCuentaBoveda: value.IGKTipoCuentaBoveda.toString()
											,NumeroCuentaBoveda: value.IGKNumeroCuentaBoveda.toString()
											,PagaClienteCorredor: value.IGKPagaClienteCorredor.toString()
											,IdTipoReajuste: value.IGKIdTipoReajuste.toString()
											,INGMontoInteresCorteCupon: value.IGKINGMontoInteresCorteCupon.toString()
											,Amortizacion: value.IGKAmortizacion.toString()
											,AfectoaClearing: value.IGKAfectoaClearing.toString()
											,ProcesadoClearing: value.IGKProcesadoClearing.toString()
											,IdRegimenFondoMutuo: value.IGKIdRegimenFondoMutuo.toString()
											,ISFUT: value.IGKISFUT.toString()
											,RetencionISFUT: value.IGKRetencionISFUT.toString()});	
						idRegistro++;
					}
				});	
			
		
			 var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );		
			 pregunta.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR){

						bcsComunes.ventanaOverlay($scope.idVentana, true);

						var parametros = {};
						parametros.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
						parametros.p_IdUsuario               	= $scope.SesionIdUsuario; 
						parametros.p_IdModulo                	= 'CUS';
						parametros.p_UsaReglasNegocio			= 'S';
						parametros.p_FechaCarga					= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
						parametros.p_CargaMasiva				= cargaMasiva;
						parametros.p_CargaMasivaDetalle 		= cargaMasivaDetalle;
						
						bcsComunes.log().info(JSON.stringify(parametros));
						bcsServiciosCustodia.CrearCargaMasivaISFUT(parametros,
							function(respuesta){
								bcsComunes.ventanaOverlay($scope.idVentana, false);
								 		
								if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
								} else {
									bcsComunes.ventanaOverlay($scope.idVentana, false);
									bcsComunes.ventanaMsg({
										mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
										conflictos:util.parseServerResponse(respuesta)
									},$scope.idVentana);
								}
							},
							function(respuesta){
								bcsComunes.ventanaOverlay($scope.idVentana, false);
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.idVentana);
								bcsComunes.ventanaOverlay($scope.idVentana, false);
							}
						);
					}
			 });
						 

 		}); 
	};
   


//**************************************
//***** Consulta Fecha de Sistema *****
//**************************************	
	var strFechasistema;
	var successFecha = function(result)
	{	   		
		if (result.P_OUT_ERRORESCONTADOR == "0")
		{	
			strFechasistema =  mxment.utc(result.P_OUT_FECHAHORASISTEMA).local().toDate();
			//strFechasistema =  mxment(result.P_OUT_FECHAHORASISTEMA).utc().format("DD-MM-YYYY");
			
			$scope.filtro.fecInicio 		 =  strFechasistema;
			$scope.filtro.fecTermino		 =  strFechasistema;
			$scope.filtro.fecTermino		 =  util.date($scope.filtro.fecTermino).add(5,'days').toDate();							
			
		}
		else
		{
			strFechasistema =   mxment.utc('1900-01-01.00:00:00').local().toDate();
			$scope.filtro.fecInicio 		 =  strFechasistema;
			$scope.filtro.fecTermino		 =  strFechasistema;
			$scope.filtro.fecTermino		 =  util.date($scope.filtro.fecTermino).add(5,'days').toDate();
		};
	};
	
	var errorFecha = function(result)
	{
		//Falta Revisar como y donde mostrar este error.
		//console.log(JSON.stringify(result));   		
	};
	

	/*************************************************************************
	* CARGA ARCHIVO DE BENEFICIOS DE EVENTOS DE CAPITAL - PARA PERÚ
	**************************************************************************/
	$scope.cargarArchivoDividendos = function(){
	  	var codArch = 'EVENTO_CAPITAL_BENEFICIOS_DIVIDENDOS_ENCABEZADO';
		var titulo  = 'Evento de Capital Clientes';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: 'Carga de '+ titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		
		var EventoCapital=bcsComunes.ventanaPromesa(id1);
		EventoCapital.then(function(data){
						
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				return;
			}

			var parametrosSP = {};
			parametrosSP.p_IdEmpresa					= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 					= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo						= 'CUS';
	        parametrosSP.p_DataArchivoEncabezado		= data.fileData;
	        parametrosSP.p_DataArchivoDetalle			= data.detalle.fileData;
	        parametrosSP.p_DataArchivoPie				= data.pie.fileData;
	        
	        if(data.fileData.length){
	        	obtenerDatosDesdeArchivoEventoCapitalClienteSP(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE[0].Codigo);

	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.msg("info","INFO","Archivo Cargado Exitosamente!!!");
	        		}
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.msg("error","Error","La operacion tiene errores");
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay($scope.idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoEventoCapitalClienteSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=102&c=1";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
    
	/*************************************************************************
	* CARGA ARCHIVO LIBERADAS DE EVENTOS DE CAPITAL - PARA PERÚ
	**************************************************************************/
	$scope.cargarArchivoArchivoLiberadas = function(){
	  	var codArch = 'EVENTO_CAPITAL_BENEFICIOS_LIBERADAS_ENCABEZADO';
		var titulo  = 'Evento de Capital Clientes';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: 'Carga de '+ titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		
		var EventoCapital=bcsComunes.ventanaPromesa(id1);
		EventoCapital.then(function(data){
						
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				return;
			}

			var parametrosSP = {};
			parametrosSP.p_IdEmpresa					= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 					= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo						= 'CUS';
	        parametrosSP.p_DataArchivoEncabezado		= data.fileData;
	        parametrosSP.p_DataArchivoDetalle			= data.detalle.fileData;
	        parametrosSP.p_DataArchivoPie				= data.pie.fileData;
	        
	        if(data.fileData.length){
	        	obtenerDatosDesdeArchivoLiberadasSP(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE[0].Codigo);

	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.msg("info","INFO","Archivo Cargado Exitosamente!!!");
	        		}
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.msg("error","Error","La operacion tiene errores");
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay($scope.idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoLiberadasSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=106";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
	
	/*************************************************************************
	* CARGA ARCHIVO DE PAGO DE CUPONES Y AMORTIZACIONES - PARA PERÚ
	**************************************************************************/
	$scope.cargarArchivoCupones = function(){
	  	var codArch = 'EVENTO_CAPITAL_PAGO_CUPONES_Y_AMORTIZACIONES';
		var titulo  = 'Evento de Capital Clientes';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: 'Carga de '+ titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false);
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		
		var EventoCapital=bcsComunes.ventanaPromesa(id1);
		EventoCapital.then(function(data){
						
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				return;
			}

			var parametrosSP = {};
			parametrosSP.p_IdEmpresa					= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 					= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo						= 'CUS';
	        parametrosSP.p_DataArchivoDetalle			= data.fileData;
	        
	        if(data.fileData.length){
	        	obtenerDatosDesdeArchivoCuponesSP(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error","Error",result.P_OUT_ERRORESDETALLE[0].Codigo);

	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.msg("info","INFO","Archivo Cargado Exitosamente!!!");
	        		}
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.msg("error","Error","La operacion tiene errores");
	        		bcsComunes.ventanaOverlay($scope.idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay($scope.idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoCuponesSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=124";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
	
	/*************************************************************************
	* CARGA ARCHIVO DE RETENCIONES - PARA PERÚ
	**************************************************************************/
    $scope.btnRetenciones = function(id) {
    	// validar que solo se pueda cargar para dividendos en dinero y cupones
    	if ($scope.bEC.idActualEvento.TipoEvento=== 'DV' || $scope.bEC.idActualEvento.TipoEvento=== 'CC'){
    		var datosArchivo = {codigoArchivo: id, titulo: id, eventoCapital: $scope.bEC.IdEventoCapital};
			bcsComunes.ventanaAbrir("CargaArchivoRetenciones",datosArchivo,false,false,$scope.idVentana);
    	}else{
    		bcsComunes.msg("warning","Advertencia","Las retenciones solo se cargan para dividendos en dinero y cortes de cupón");
    	}
		
	}
    
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	
	//fechas filtro
	$scope.filtro.fecInicio 	=  $scope.SesionFechaSistema;
	$scope.filtro.fecTermino    =  $scope.SesionFechaSistema;
	$scope.bEC.idActualFechaPago = $scope.SesionFechaSistema;
	$scope.bEC.idActualFechaPagoHasta = $scope.SesionFechaSistema;

	//bcsServicioPlataforma.FechaSistema(datos,successFecha,errorFecha);	
	//****************************************
	//*** Fin Consulta Fecha de Sistema *****
	//****************************************
	
   	$scope.$watch('bEC.NemoActual', function(newVal, oldVal){
            if(newVal != oldVal){
                if($scope.gridApi.dataSource){
                    $scope.gridApi.dataSource.removeAll();
                }
              }
      })

	
	
});