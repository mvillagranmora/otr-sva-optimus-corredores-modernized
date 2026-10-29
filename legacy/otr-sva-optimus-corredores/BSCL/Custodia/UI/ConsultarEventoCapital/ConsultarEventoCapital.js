angular.module('ModuloCustodia').controller('ConsultarEventoCapitalCtrl', 
		function($scope,
				$timeout,
				$modal,
				$translate,
				bcsComunes, 
				bcsAlerta, 
				OPCIONES_BCSALERTA, 
				bcsBlotterEventoCapitalGrid,  
				bcsAlerta,
				bcsServiciosEventoCapital,
				bcsServicioPlataforma,
				$resource,
				$rootScope)
{	
	
	//*** configuracion de objetos ***//
	
	$scope.filtro = {};
	$scope.data ={};
	var overlay = bcsComunes.overlay;
	var Accion = ''
		
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.idVentana = idVentana;
	$scope.procesandoFiltro;
	$scope.entraPorBoton = false;
	// BOTONES
	$scope.ActivaBotonModificarEv		= false;
	$scope.ActivaBotonEliminarEv		= false;
	$scope.ActivaBotonProcesarEv		= false;
	$scope.ActivaBotonNotificarEv		= false;
	$scope.ActivaBotonReprocesarEv		= false;
	$scope.ActivaBotonIndicesEv			= false;
	$scope.ActivaBotonCerrarEv			= false;
	$scope.ActivaBotonActualizarEv		= false;
	$scope.ActivaBotonSorteoLetrasEv	= true;
	$scope.ActivaBotonRenovarEv			= false;
	$scope.ActivaBotonCorteCuponEv		= false;
	
	if ( $scope.SesionLocale === 'PE' ){
		$scope.MuestraBotonCargarEC		= true;
		$scope.MuestraBotonReplicarEC	= true;
	}else{
		$scope.MuestraBotonCargarEC		= false;
		$scope.MuestraBotonReplicarEC	= false;
	}
			
	
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
			NombreEntidad 	: { "field"   : "NombreEntidad","operator": "eq", "value"   : 'CUSTODIA.EVENTO'}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
   	//GRILLA PRINCIPAL
   	var dsEventoCapital = bcsBlotterEventoCapitalGrid.dataSourceEvento();
	$scope.confBasicaEventoCapital = {
		dataSource			: dsEventoCapital,
		columns				: bcsBlotterEventoCapitalGrid.columnasEvento(),	
		rowColorBackground 	: bcsBlotterEventoCapitalGrid.fnColorBk,
		sortable			: true,
		pageable			: true,
		excel 				: { allPages : true },
		userSettings		: true,
		height				: 470,
		selectable 			: "multiple, row",
		filterable			: true
			
	};
	

	
   	//********* fin configuracion de objetos *********//

/*************************************************************************
* BUSCAR
**************************************************************************/	

	$scope.buscarEventoCapital = function(_entraPorBoton){
		var filters = [];
		$scope.entraPorBoton = _entraPorBoton;
		
	if(!$scope.procesandoFiltro){
		var filtro = !$scope.filtro ? undefined : bcs.clone($scope.filtro);
		var filtros = !$scope.filtros ? undefined : bcs.clone($scope.filtros);
		
		try{
        	if (!!filtro.TipoEvento.IdTipoEventoCapital &&
        	 filtro.TipoEvento.IdTipoEventoCapital !== "")
        		filters.push({ field: "IdTipoEventoCapital", operator: "contains", value: filtro.TipoEvento.IdTipoEventoCapital });
				
				var tipo = filtro.TipoEvento.IdTipoEventoCapital;
				$rootScope.$broadcast('evento:tipoSeleccionado', tipo);
			
		}catch(e){
		}
	
		try {
			
        	if (!!filtro.instrumentoObjeto){
    			if (filtro.instrumentoObjeto.IdInstrumento > 0 && (filtro.instrumentoObjeto.ConteoNemotecnico === undefined || filtro.instrumentoObjeto.ConteoNemotecnico === 1) )
					filters.push({ field: "IdInstrumento", operator: "eq", value: Number(filtro.instrumentoObjeto.IdInstrumento) });
					
				if (filtro.instrumentoObjeto.Nemotecnico != "" && filtro.instrumentoObjeto.ConteoNemotecnico > 1)
					filters.push({ field: "Nemo", operator: "eq", value: filtro.instrumentoObjeto.Nemotecnico });
        	}
		}catch(e){
		}		
	
		try{
        	if (!!filtro.estadoObj.IdEstado && filtro.estadoObj.IdEstado !== "")
        		filters.push({ field: "IdEstado", operator: "contains", value: filtro.estadoObj.IdEstado });
		}catch(e){
		}
		

		try { // se mete dentro de un try ya q cuando no se ingresa un valor al objeto, este no se lee
    		
        	if (filtros.secuencia !== undefined && filtros.secuencia !=='' && filtros.secuencia !==0){

	    		try{
		    		if (Number(filtros.secuencia)){
			    		var auxNum = Number(filtros.secuencia);
			    		seleccOk = true;
		        		filters.push({ field: "Secuencia", operator: "eq", value: auxNum });
		    		}
	    		}catch (e){
	    		}

        	}
    	} catch(e) {
    	}

		var auxDate1;
    	var auxDate2;

		let filtrosFechaPago = [];
		let filtrosFechaLimite = [];
		
		if (filtro.fecInicio !== undefined && filtro.fecInicio !== "" && filtro.fecInicio != null) {
		    auxDate1 = filtro.fecInicio;
		    filtrosFechaPago.push({ field: "FechaPago", operator: "gte", value: auxDate1 });
		}
		
		if (filtro.fecTermino !== undefined && filtro.fecTermino !== "" && filtro.fecTermino != null) {
		    auxDate2 = filtro.fecTermino;
		    if (auxDate2 < auxDate1) {
		        bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
		        return;
		    }
		    filtrosFechaPago.push({ field: "FechaPago", operator: "lte", value: auxDate2 });
		}
		
		if (filtro.fecInicioLim !== undefined && filtro.fecInicioLim !== "" && filtro.fecInicioLim != null) {
		    auxDate1 = filtro.fecInicioLim;
		    filtrosFechaLimite.push({ field: "FechaLimite", operator: "gte", value: auxDate1 });
		}
		
		if (filtro.fecTerminoLim !== undefined && filtro.fecTerminoLim !== "" && filtro.fecTerminoLim != null) {
		    auxDate2 = filtro.fecTerminoLim;
		    if (auxDate2 < auxDate1) {
		        bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
		        return;
		    }
		    filtrosFechaLimite.push({ field: "FechaLimite", operator: "lte", value: auxDate2 });
		}
		
		// Combinar los filtros con OR
		if (filtrosFechaPago.length > 0 || filtrosFechaLimite.length > 0) {
		    filters.push({
		        logic: "or",
		        filters: []
		            .concat(filtrosFechaPago.length > 0 ? [{ logic: "and", filters: filtrosFechaPago }] : [])
		            .concat(filtrosFechaLimite.length > 0 ? [{ logic: "and", filters: filtrosFechaLimite }] : [])
		    });
		}


    	
    	$scope.bEC.idActualEvento.TipoEvento	= undefined; 
    	$scope.bEC.idActualNumeroDividendo 		= undefined;
    	$scope.bEC.idActualFechaPago 			= undefined;
    	$scope.bEC.idActualFechaPagoHasta 		= undefined;
    	$scope.bEC.NemoActual	 				= undefined;
    	$scope.bEC.IdEventoCapital 				= undefined;
    	$scope.bEC.ActivaBotonEliminarEv		= true;
    	$scope.bEC.ActivarBotones		 		= false;
    	$scope.bEC.ActivarBotonIngresarEvc		= false;
		
		$scope.gridApi.dataSource.filter(filters);
		$scope.entraPorBoton=false;
	}
		//dsEventoCapital.filter(filters);
	};

	$rootScope.limpiarSeleccionado = function() {
		$scope.gridApi.clearSelection();
	}

	$scope.limpiarFiltros = function() {
		$scope.filtro.oConfiguracionTipoEvento.Actual		= "";
		$scope.filtro.oConfiguracionEstadoMovimiento.estado	= "";
		$scope.filtro.instrumentoObjeto 					= undefined;
		$scope.filtro.buscaroInstrumento 					= "-1";
		if ($scope.filtros !== undefined){
			$scope.filtros.secuenciaObj = '';
		};
		$scope.filtro.fecInicio 		 =  util.dateSubMonths(util.date($scope.SesionFechaSistema),1);
		$scope.filtro.fecInicioLim 		 =  util.dateSubMonths(util.date($scope.SesionFechaSistema),1);
		$scope.filtro.fecTermino		 =  util.dateAddMonths(util.date($scope.SesionFechaSistema),1);
		$scope.filtro.fecTerminoLim		 =  util.dateAddMonths(util.date($scope.SesionFechaSistema),1);

		$scope.gridApi.dataSource.removeAll();
		$scope.seleccionEventoCapital = false;
	};
	

	
	

	/*************************************************************************
	* CARGAR EVENTO CAPITAL DESDE BURSATIL 
	**************************************************************************/	
	$scope.cargarEventoDesdeBursatil = function(oEventoCapital)
	{
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		
		var id1 = bcsComunes.ventanaAbrir("CargarEventoCapitalModal",false,false,false,$scope.idVentana);
		
	};

	
	
/*************************************************************************
* INGRESAR 
**************************************************************************/	
	
	$scope.btnIngresar = function(){	
		Accion = 'Crear' 
		if ($scope.filtro.TipoEvento.IdTipoEventoCapital != 'SL'){
			$scope.abrirModal(undefined);
		}
		else
		{
		$scope.abrirModalSorteoLetras(undefined);
		}
	};	

/*************************************************************************
* MODIFICAR
**************************************************************************/	

	$scope.btnModificar = function(oEventoCapital){
		if($scope.bEC.seleccionEventoCapital.length != 1)
			return;
		else{
			$scope.bEC.seleccionEventoCapital = null;
			$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
		}
		if ($scope.bEC.seleccionEventoCapital === undefined){
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant('DEBE_SELECCIONAR_UN_REGISTRO'));
			return;
		}
		else
			if (($scope.bEC.seleccionEventoCapital.IdEstado != 'VIG') && ($scope.bEC.seleccionEventoCapital.IdEstado != 'ACT') && ($scope.bEC.seleccionEventoCapital.IdEstado != 'MOD') && ($scope.bEC.seleccionEventoCapital.IdEstado != 'PRO') && ($scope.bEC.seleccionEventoCapital.IdEstado != 'NOT')){
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_VIGENTE_MODIFICAR"));
				return;
			}
			else
			{
				Accion = 'Modificar' 
					$scope.abrirModal(oEventoCapital);
			}
	};	
	
	
/*************************************************************************
* ABRIR MODAL
**************************************************************************/	
	
	$scope.abrirModal = function(oEventoCapital)
	{
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		var id1 = bcsComunes.ventanaAbrir("CrearEventoCapitalModal",{Accion: Accion, EventoCapital : oEventoCapital,IdEvento : $scope.filtro.TipoEvento},false,false,$scope.idVentana);
        var promesa=bcsComunes.ventanaPromesa(id1);
        
        promesa.then(function(data){ 
        	if (data === true){	
        		$scope.gridApi.dataSource.read();	
			}
        	else{
				$scope.bEC.seleccionEventoCapital = null;
        		$scope.bEC.seleccionEventoCapital = oEventoCapital;
			}
    	});  
        
	};
	
/*************************************************************************
* ABRIR MODAL CORTE CUPON
**************************************************************************/	
		
		$scope.abrirModalCorteCupon = function(oEventoCapital)
		{
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			var id1 = bcsComunes.ventanaAbrir("ProcesarCorteCuponModal",{Accion: Accion, EventoCapital : oEventoCapital,IdEvento : $scope.filtro.TipoEvento},false,false,$scope.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        
	        promesa.then(function(data){ 
	        	if (data === true)
				{	
	        		$scope.gridApi.dataSource.read();	
				}
	    	});  
	        
		};	


/*************************************************************************
* ABRIR MODAL SORTEO LETRAS
**************************************************************************/	
	
	$scope.abrirModalSorteoLetras = function(oEventoCapital)
	{
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		var id1 = bcsComunes.ventanaAbrir("CrearSorteoLetrasModal",{Accion: Accion, EventoCapital : oEventoCapital},true,true);
        var promesa=bcsComunes.ventanaPromesa(id1);
        
        promesa.then(function(data){ 
        	if (data === true)
			{	
        		$scope.gridApi.dataSource.read();	
			}
    	});  
        
	};
/*************************************************************************
* PROCESO 
**************************************************************************/	

	$scope.btnProcesar = function(oEventoCapital){		
		
		if ($scope.bEC.seleccionEventoCapital != undefined){
			var eventoCapitalaProcesar = [];
			var fechaLimite = [];
			var procesarMensaje = '';
			$scope.bEC.seleccionEventoCapital.forEach(function(eventoCapital, index) {				
				if ( $scope.SesionLocale === 'PE' ){
					var auxDateLimite;
					auxDateLimite = util.fechaHoraSistema(eventoCapital.FechaLimite,"YYYY-MM-DD");
					
					if(BCS_ESTADO_MODULO['CUS'] === "ABT"){
						if(auxDateLimite >= $scope.SesionFechaSistema){
							bcsComunes.msg("warning","warning", $translate.instant("FECHA_LIMITE_IGUAL_O_MAYOR_A_LA_DE_SISTEMA"));
							return;
						}	
					}else{
						if(auxDateLimite > $scope.SesionFechaSistema){
							bcsComunes.msg("warning","warning", $translate.instant("FECHA_LIMITE_IGUAL_O_MAYOR_A_LA_DE_SISTEMA"));
							return;
						}
					}
				}else{
					if (BCS_ESTADO_MODULO['CUS'] === "CER") {
						bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
						return;
					}
				}
				
				if ((eventoCapital.Indice) !== (eventoCapital.IndiceRue)){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_INDICE_ERROR"));
					return;
				};
				
				if (eventoCapital.IdTipoEventoCapital == 'SU'){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_SUSCIPCIONES_NO_SE_PROCESAN"));
					return;			
				};
				
				if (eventoCapital.IdTipoEventoCapital === 'CC'){
					bcsComunes.msg("warning","warning", "Utilizar el boton Corte Cupon.");
					return;
				}				
					
				if (eventoCapital.IdEstado == 'VIG'){
						eventoCapitalaProcesar.push(Number(eventoCapital.IdEventoCapital) || 0);
						fechaLimite.push(util.dateSPdate(eventoCapital.FechaLimite));

				}else{	
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_VIGENTE_PROCESAR"));
				}				
			
			});			
			if(oEventoCapital.length > 1){
				procesarMensaje = " Tipo Evento Capital: " +  oEventoCapital[0].TipoEvento + " Cantidad a procesar: " + oEventoCapital.length;
			}
			else{
				procesarMensaje = " Tipo Evento Capital: " +  oEventoCapital[0].TipoEvento + " Nemo: " + oEventoCapital[0].Nemo + " Secuencia: " + oEventoCapital[0].Secuencia;
			}
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_REPROCESAR_EVENTO ") + procesarMensaje);		
			t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.SI) {
				var datos = {};	
				bcsComunes.ventanaOverlay($scope.idVentana,true);
				datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
				datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
				datos.p_IdModulo = 'CUS';
				datos.p_Origen = 'UI';
				datos.p_UsaReglasNegocio = 'S';
				datos.p_IdEventoCapital = eventoCapitalaProcesar;
				datos.p_FechaLimite = fechaLimite;
				datos.p_IdTipoEventoCapital = [];
				bcsServiciosEventoCapital.ProcesarEventoCapital(datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.idVentana,false);
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
						$scope.gridApi.dataSource.read();
					},
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.idVentana,false);
						bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},$scope.idVentana);
					});
			}
			else 
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			});
			return;   
				
			
		}else{
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
	return;
	};

	$scope.btnReprocesar = function(oEventoCapital){
		if($scope.bEC.seleccionEventoCapital.length != 1)
			return;
		else{
			$scope.bEC.seleccionEventoCapital = null;
			$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
		}
		if($scope.bEC.seleccionEventoCapital.FechaPago < util.date($scope.SesionFechaSistema))
		{
			bcsComunes.ventanaMsgErr($translate.instant("UI_FECHA_PAGO_MENOR_SISTEMA"));
			return;
    }
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if ($scope.bEC.seleccionEventoCapital != undefined){
			if (($scope.bEC.seleccionEventoCapital.IdEstado == 'MOD') || ($scope.bEC.seleccionEventoCapital.IdEstado == 'MAN') || ($scope.bEC.seleccionEventoCapital.IdEstado == 'PRO')){
				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_REPROCESAR_EVENTO ") + " Tipo Evento Capital: " +  $scope.bEC.seleccionEventoCapital.TipoEvento + " Nemo: " + $scope.bEC.seleccionEventoCapital.Nemo + " Secuencia: " + $scope.bEC.seleccionEventoCapital.Secuencia);		
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.SI) {
						var datos = {};	
						bcsComunes.ventanaOverlay($scope.idVentana,true);
		
						datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
						datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
						datos.p_IdModulo = 'CUS';
						datos.p_Origen = 'UI';
						datos.p_UsaReglasNegocio = 'S'
						datos.p_IdEventoCapital					= [Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0];			                   		
						//datos.p_IdTipoEventoCapital				= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital 	|| '';     
		//				datos.p_Factor							= Number($scope.bEC.seleccionEventoCapital.Factor) 					|| 0;                          		
						//if ((datos.p_IdTipoEventoCapital == 'DA') || (datos.p_IdTipoEventoCapital == 'DV') || (datos.p_IdTipoEventoCapital == 'CA') || (datos.p_IdTipoEventoCapital == 'DS')){
							bcsServiciosEventoCapital.ReprocesarEventoCapital
								(datos,
								function(respuesta){	  		
									bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
									$scope.gridApi.dataSource.read();
								},
								function(respuesta){
									bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.idVentana);
									bcsComunes.ventanaOverlay($scope.idVentana,false);
								});
						//}
					}
					else 
						bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   
			}else{	
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_MODIFICA_PROCESAR"));
			}
		}else{
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
		return;
	};
		
/*************************************************************************
* ACTUALIZA INDICES
*************************************************************************/

$scope.btnIndices = function(oEventoCapital){
	if($scope.bEC.seleccionEventoCapital.length != 1)
		return;
	else{
		$scope.bEC.seleccionEventoCapital = null;
		$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
	}
	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
 if ($scope.bEC.seleccionEventoCapital != undefined){
	 if ($scope.bEC.seleccionEventoCapital.EsInscrito == 'S'){
		if (($scope.bEC.seleccionEventoCapital.IdEstado == 'VIG') || (($scope.bEC.seleccionEventoCapital.IdEstado == 'ACT'))|| (($scope.bEC.seleccionEventoCapital.IdEstado == 'PRO'))){	  
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_INDICE_EVENTO")+ " Tipo Evento Capital: " +  $scope.bEC.seleccionEventoCapital.TipoEvento + " Nemo: " + $scope.bEC.seleccionEventoCapital.Nemo + " Secuencia: " + $scope.bEC.seleccionEventoCapital.Secuencia)
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};
					bcsComunes.ventanaOverlay($scope.idVentana,true);

					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';

					datos.p_IdEventoCapital					= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;			                   		
					datos.p_IdTipoEventoCapital				= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital || '';
					datos.p_Secuencia						= $scope.bEC.seleccionEventoCapital.Secuencia || 0;
					bcsServiciosEventoCapital.ActualizaIndice(datos,
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
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   
		}else{	
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_INDICE_TIPO_EVENTO"));
		}
		  }else{
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("UI_INDICE_INSCRITO"));
  }

  }else{
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
  }
return;
};	


/*************************************************************************
* NOTIFICACION
**************************************************************************/
$scope.btnNotificar = function(oEventoCapital){
	if($scope.bEC.seleccionEventoCapital.length != 1)
		return;
	else{
		$scope.bEC.seleccionEventoCapital = null;
		$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
	}
	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
  if ($scope.bEC.seleccionEventoCapital != undefined){
		if ($scope.bEC.seleccionEventoCapital.IdEstado != 'ANU'){	  
			var t = bcsAlerta.crear("PREGUNTA_SI_NO", util.translate($translate,"UI_MENSAJE_NOTIFICAR_EVENTO", {TipoEvento: $scope.bEC.seleccionEventoCapital.TipoEvento , Nemo:  $scope.bEC.seleccionEventoCapital.Nemo, Secuencia: $scope.bEC.seleccionEventoCapital.Secuencia}))
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};
					bcsComunes.ventanaOverlay($scope.idVentana,true);

					
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';

					datos.p_IdsEventoCapital					= [{"p_IdEventoCapital" : Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0}];			                   		
					//datos.p_IdTipoEventoCapital				= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital || '';
					datos.p_IdsEventoCapitalCliente			= [];
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
					bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   
		}else{	
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_NO_VALIDA"));
		}
  }else{
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
  }
return;
};	


	
/*************************************************************************
* CERRAR 
**************************************************************************/
  $scope.btnCerrar = function(oEventoCapital){


	if ($scope.bEC.seleccionEventoCapital != undefined){
  		
		if($scope.bEC.seleccionEventoCapital.length>1){

			$scope.CerrarProcesosEventos(undefined);
		}else{

			$scope.bEC.seleccionEventoCapital.forEach(function(eventoCapital, index) {
				
				if ((eventoCapital.Indice) !== (eventoCapital.IndiceRue)){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_INDICE_ERROR"));
				  return;
				   };
							
				if ((eventoCapital.IdEstado == 'MAN') || (eventoCapital.IdEstado == 'MOD')){
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_INDICE_REPROCESAR"));
				  return;
				   };
						
				if ((eventoCapital.IdTipoEventoCapital !== 'SU' && eventoCapital.IdEstado == 'NOT')||
				   ((eventoCapital.IdTipoEventoCapital == 'SU' || eventoCapital.IdTipoEventoCapital == 'CC') && eventoCapital.IdEstado == 'VIG'))
				{	
					if ($scope.SesionFechaSistema !== util.dateSPdate(eventoCapital.FechaPago)){
						if ( eventoCapital.IdEstado == 'NOT' ) {
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_NOTIFICADO_FECHA_DISTINTA ") + " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);
						} else {
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_FECHA_DISTINTA ") + " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);
						}
			
						t.result.then(function(result){
							if (result===OPCIONES_BCSALERTA.SI) {
								$scope.CerrarProcesosEventos(undefined);
							}else{
								bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
							}
						})
		
					} else{
						if ( eventoCapital.IdEstado == 'NOT' ) {
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_NOTIFICADO")+ " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);
						} else {
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL")+ " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);
						}
						t.result.then(function(result){
							if (result===OPCIONES_BCSALERTA.SI) {
								$scope.CerrarProcesosEventos(undefined);
							}else{
								bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
							}
						})
					}
					return;
				}else{
					if ($scope.SesionFechaSistema !== util.dateSPdate(eventoCapital.FechaPago)){
						if (eventoCapital.IdEstado == 'PEN'){
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_SIN_NOTIFICAR_Y_FECHA_DISTINTA_Y_PENDIENTE")+ " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);
						}else{
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_SIN_NOTIFICAR_Y_FECHA_DISTINTA")+ " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);	
						}
						
						t.result.then(function(result){
							if (result===OPCIONES_BCSALERTA.SI) {
								$scope.CerrarProcesosEventos(undefined);
							}else{
								bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
							}
						})
						
					}else{
						if (eventoCapital.IdEstado == 'PEN'){
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_SIN_NOTIFICAR_Y_PENDIENTE")	+ " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);
							}else{
							var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_EVENTO_CAPITAL_SIN_NOTIFICAR")+ " Tipo Evento Capital: " +  eventoCapital.TipoEvento + " Nemo: " + eventoCapital.Nemo + " Secuencia: " + eventoCapital.Secuencia);	
						}
						t.result.then(function(result){
							if (result===OPCIONES_BCSALERTA.SI) {
								$scope.CerrarProcesosEventos(undefined);
							}else{
								bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
							}
						})
					}
				}

				
			  });
		}

	} else {	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
	}
  	return;
   };	
   
   
   $scope.$watch('procesandoFiltro',function(newval,oldval){
		if(!newval && oldval && $scope.entraPorBoton){
			$scope.buscarEventoCapital(false);
		}
	
   });	
   
   
   
	$scope.CerrarProcesosEventos = function(eventos){

		var eventosparaCerrar = [];
		$scope.bEC.seleccionEventoCapital.forEach(function(eventoCapital, index) {
			eventosparaCerrar.push(eventoCapital.IdEventoCapital);
		});
		
		var datos = {};
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo  = 'CUS';
		datos.p_UsaReglasNegocio = 'S';
		datos.p_IdEventoCapital	= eventosparaCerrar;		                   		
					
		bcsServiciosEventoCapital.CerrarEventoCapitalMasivo
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
	};
	
	
/*************************************************************************
* CERRAR EXITOSO DA
**************************************************************************/
	var successCerrarEventoCapitalDA = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_INGRESOSREALIZADOS !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_CERRAR_EXITO"));
			$scope.gridApi.dataSource.read();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant(respuesta.P_OUT_ERRORESDETALLE[0].Descripcion));
	    }
	};
	
/*************************************************************************
* CERRAR CON ERRORES DA
**************************************************************************/	
	var errorCerrarEventoCapitalDA = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
		$scope.data.accionDisabled = true;
	};

/*************************************************************************
* CERRAR EXITOSO CORTE CUPON
**************************************************************************/
	var successCerrarEventoCapitalCorteCupon = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (	(respuesta.P_OUT_ADVERTENCIASCONTADOR !== undefined && respuesta.P_OUT_ADVERTENCIASCONTADOR === 0) && 
				(respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0)  
			) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_CERRAR_EXITO"));
			bcsComunes.ventanaCerrar(admEveCap.winId);
		}
		else
	    {
			if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR > 0)
			{
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
			}
			else
			{
				bcsComunes.msg('warning',$translate.instant("UI_ADVERTENCIA"), $translate.instant("UI_OPERACION_CIERRE_EVENTO_CAPITAL_WARNING"));
			}

	    }
	};
	
/*************************************************************************
* CERRAR CON ERRORES  CORTE CUPON
**************************************************************************/	
	var errorCerrarEventoCapitalCorteCupon = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
		$scope.data.accionDisabled = true;
	};	


/*************************************************************************
* CERRAR EXITOSO CANJE
**************************************************************************/

	var successCerrarEventoCapitalCanje = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
				if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
				
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_CERRAR_EXITO"));
			bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.gridApi.dataSource.read();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
	    }
	};

/*************************************************************************
* CERRAR CON ERRORES  CANJE
**************************************************************************/	
	
	var errorCerrarEventoCapitalCanje = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
		$scope.data.accionDisabled = true;
	};

/*************************************************************************
* CERRAR EXITOSO DS
**************************************************************************/

	var successCerrarEventoCapitalDivisionSociedad = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
				if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
				
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_CERRAR_EXITO"));
			bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.gridApi.dataSource.read();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
	    }
	};

/*************************************************************************
* CERRAR CON ERRORES  DS
**************************************************************************/	
	
	var errorCerrarEventoCapitalDivisionSociedad = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
		$scope.data.accionDisabled = true;
	};		
/*************************************************************************
* RETIRAR OSA EXITOSO 
**************************************************************************/
	var successRetirarCustodiaOSA = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
//   		overlay.encendido = false;
   		bcsComunes.ventanaOverlay($scope.idVentana,false);
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_ERRORESCONTADOR === 0) 
				
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_CERRAR_EXITO"));
//			bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.gridApi.dataSource.read();

		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
	    }
	};
	
/*************************************************************************
* * RETIRAR OSA CON ERRORES
**************************************************************************/	
	var errorRetirarCustodiaOSA = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_CERRAR_ERROR"));
		$scope.data.accionDisabled = true;
	};
	
/*************************************************************************
* ACTUALIZAR OSA  
**************************************************************************/	
  $scope.btnActualizar = function(oEventoCapital){
	if($scope.bEC.seleccionEventoCapital.length != 1)
		return;
	else{
		$scope.bEC.seleccionEventoCapital = null;
		$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
	}
	  
  	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
	if ($scope.bEC.seleccionEventoCapital != undefined){
		if ($scope.bEC.seleccionEventoCapital.IdEstado == 'NOT' || $scope.bEC.seleccionEventoCapital.IdEstado == 'PRO'){
			if ($scope.bEC.seleccionEventoCapital.IdTipoEventoCapital == 'EP'){
				if (($scope.bEC.seleccionEventoCapital.IdEstado == 'NOT') || ($scope.bEC.seleccionEventoCapital.EstadoNotificacion=='S')){
					var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ACTUALIZAR_OSA"));
					t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.SI) {
						var datos = {};	
						var datosPrecio = {};
						bcsComunes.ventanaOverlay($scope.idVentana,true);
						//overlay.encendido = true; 
						
						datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
						datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
						datos.p_IdModulo = 'CUS';					
						datos.p_IdPais							= $scope.SesionIdPaisSistema;
						
						datos.p_IdEventoCapital					= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;			                   		
						//datos.p_IdTipoEventoCapital				= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital || '';     
						bcsServiciosEventoCapital.ActualizarCustodiaOSA
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
					if ($scope.bEC.seleccionEventoCapital.IdEstado == 'PRO') {
						var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ACTUALIZAR_OSA_NO_NOTIFICADA"));
						t.result.then(function(result){
						if (result===OPCIONES_BCSALERTA.SI) {
							var datos = {};	
							var datosPrecio = {};
							//overlay.encendido = true;
							bcsComunes.ventanaOverlay($scope.idVentana,true);
			
							datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
							datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
							datos.p_IdModulo = 'CUS';					
			
							datos.p_IdEventoCapital					= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;			                   		
							//datos.p_IdTipoEventoCapital				= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital || '';     
							
							bcsServiciosEventoCapital.ActualizarCustodiaOSA
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
						else{
							bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
						}
						});
					}
					else
					{
						bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("UI_SOLO_ACTUALIZA_EMISION_PAGADA_ESTADO_PRO_Y_NOT"));
					}
				}
			}
			else{
				bcsComunes.msg("warning",$translate.instant("UI_SOLO_EMISION_PAGADA_SE_PUEDE_ACTUALIZAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			}
		}else{
			bcsComunes.msg("warning",$translate.instant("UI_SOLO_ACTUALIZA_ESTADO_PRO_Y_NOT") + ' : ' + $translate.instant("UI_CANCELAR"));
		}
	}else{	
		bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
	}
  return;
   };
   
/*************************************************************************
* ELIMINAR 
**************************************************************************/	

  $scope.btnEliminar = function(){
  	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
  if ($scope.bEC.seleccionEventoCapital != undefined){
	  var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_EVENTO") + " Tipo Evento Capital: " +  $scope.bEC.seleccionEventoCapital[0].TipoEvento + " Nemo: " + $scope.bEC.seleccionEventoCapital[0].Nemo + " Secuencia: " + $scope.bEC.seleccionEventoCapital[0].Secuencia,$scope.idVentana);
//			t.result.then(function(result){
	  t.then(function(result){
		if (result===OPCIONES_BCSALERTA.SI) {
			var datos = {};	
			//overlay.encendido = true; 
			
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
			datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
			datos.p_IdModulo = 'CUS';
			datos.p_UsaReglasNegocio = 'S';
			
			var eventosCapital	= [];
			for(var i = 0; i<$scope.bEC.seleccionEventoCapital.length; i++){
				var item = $scope.bEC.seleccionEventoCapital[i];
				if(item.IdEstado == 'VIG' || item.IdTipoEventoCapital == 'CA'){
					eventosCapital.push({
						"p_IdEventoCapital" : Number(item.IdEventoCapital) || 0,
						"p_IdTipoEventoCapital" : item.IdTipoEventoCapital || '',
						"p_IdEmpresa" : item.IdEmpresa,
						"p_FechaHoraUltimaActualizacion" : item.FechaHoraUltimaActualizacion,
						"p_IdEstado" : item.IdEstado
					});
				}
				else{
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_VIGENTE_ELIMINAR"));
					return;
				}
			}
			datos.p_EventoCapital = eventosCapital;
			
			bcsServiciosEventoCapital.EliminarEventoCapital(datos,
				function(respuesta){	  		
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_EXITO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.idVentana);
					bcsComunes.ventanaOverlay($scope.idVentana,false);
					$scope.gridApi.dataSource.read();
				},
				function(respuesta){
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_INGRESO_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.idVentana);
					bcsComunes.ventanaOverlay($scope.idVentana,false);
				});
		}
		else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
	});
	return;
   
   }else{
	    bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		}
  return;
   };
		

/*************************************************************************
* ELIMINAR EXITOSO
**************************************************************************/
	var successEliminarEventoCapital = function(respuesta)
	{
   		overlay.encendido = false;
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
	var errorEliminarEventoCapital = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULACION_EVENTO"));
		$scope.data.accionDisabled = true;
	};

	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; //se obtiene de variables de sistema
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; //se obtiene de variables de sistema
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
    
	$scope.filtro.fecInicio 		 =  util.dateSubMonths(util.date($scope.SesionFechaSistema),1);
	$scope.filtro.fecInicioLim 		 =  util.dateSubMonths(util.date($scope.SesionFechaSistema),1);
	$scope.filtro.fecTermino 		 =  util.dateAddMonths(util.date($scope.SesionFechaSistema),1);
	$scope.filtro.fecTerminoLim		 =  util.dateAddMonths(util.date($scope.SesionFechaSistema),1);


/*************************************************************************
* RENOVACION
**************************************************************************/
  $scope.btnRenovar = function(oEventoCapital){
	if($scope.bEC.seleccionEventoCapital.length != 1)
		return;
	else{
		$scope.bEC.seleccionEventoCapital = null;
		$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
	}
  	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
    if ($scope.bEC.seleccionEventoCapital != undefined){
	if ($scope.bEC.seleccionEventoCapital.IdTipoEventoCapital == 'EP'){		
		if ($scope.bEC.seleccionEventoCapital.IdEstado == 'CER'){		
			var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_RENOVAR_EVENTO"));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.SI) {
					var datos = {};	
					overlay.encendido = true; 				
					datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
					datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
					datos.p_IdModulo = 'CUS';
					datos.p_UsaReglasNegocio = 'S';

					datos.p_IdEventoCapital					= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;	                   		
    
					bcsServiciosEventoCapital.RenovarEventoCapitalEmisionPago(datos,successRenovarEventoCapitalEmisionPago,errorRenovarEventoCapitalEmisionPago);
					
				}
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
				});
				return;   
		}else{	
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_NOTIFICACION_TIPO_EVENTO"));
			}
			
			
	}else{	
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_NOTIFICACION_TIPO_EVENTO"));
			}
			
	}else{
			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("DEBE_SELECCIONAR_UN_REGISTRO"));
		
			}
	return;
	};	

/*************************************************************************
* PROCESO EXITOSO RENOVACION
**************************************************************************/
	var successRenovarEventoCapitalEmisionPago = function(respuesta)
	{
		//console.log(JSON.stringify(respuesta));
   		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		$scope.data.accionDisabled = true;
			
		if (respuesta.P_OUT_SECUENCIA !== undefined && respuesta.P_OUT_SECUENCIA  > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
		{
			bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_PROCESAR_EXITO"));
//			bcsComunes.ventanaCerrar(admEveCap.winId);
			$scope.gridApi.dataSource.read();
		}
		else
	    {
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_PROCESAR_ERROR"));
	    }
	};
	

/*************************************************************************
* PROCESO CON ERRORES RENOVACION
**************************************************************************/	
	var errorRenovarEventoCapitalEmisionPago = function(respuesta)
	{
		overlay.encendido = false;
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_PROCESAR_ERROR"));
		$scope.data.accionDisabled = true;
	};
	

	
/*************************************************************************
* CORTE CUPON
**************************************************************************/
  $scope.btnCorteCupon = function(){
//		if ($scope.bEC.seleccionEventoCapital.IdTipoEventoCapital == 'CC') {
//			if ($scope.bEC.seleccionEventoCapital.IdEstado == 'VIG'){
				Accion = 'Cerrar'; 
				$scope.abrirModalCorteCupon(undefined);
//				$scope.gridApi.dataSource.read();
//			}
//			else
//			{
//				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_VIGENTE_MODIFICAR"));
//				return;
//			}
  		return;
//   		};		  
  };
  /*var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CORTE_CUPON"));
	  t.result.then(function(result){
		if (result===OPCIONES_BCSALERTA.SI) {
			var datos = {};	
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
			datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
			datos.p_IdModulo = 'CUS';
			bcsServiciosEventoCapital.ProcesarEventoCapitalCorteCupon			
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
		else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		});
		return;   

	};	*/
  

		
/*************************************************************************
* CARGA ARCHIVO DE SORTEO DE LETRAS
**************************************************************************/
  $scope.cargarArchivo = function(){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
	  	var codArch = 'ARCHIVO_SORTEO_LETRAS';
		var titulo  = 'Sorteo de Letras';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: 'Asignación '+titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false);
		bcsComunes.ventanaOverlay(idVentana,true);
		
		var SorteoLetras=bcsComunes.ventanaPromesa(id1);
		SorteoLetras.then(function(data){
			
			
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}

			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo			= 'CUS';
	        parametrosSP.p_DataArchivo		= data.fileData;
	        
	        if(data.fileData.length){
	        	obtenerDatosDesdeArchivoSorteoLetrasSP(parametrosSP).then(function(result) {
	        		bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(result)
							},$scope.idVentana);
	        		/*if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error","Error","La operacion tiene errores");
	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.msg("info","INFO","Archivo Cargado Exitosamente!!!");
	        		}*/
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.msg("error","Error","La operacion tiene errores");
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoSorteoLetrasSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=199";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };
		
	
/*************************************************************************
* PRORRATEA ARCHIVO DE SORTEO DE LETRAS
**************************************************************************/
 $scope.ProrratearArchivo = function(oEventoCapital){
	if($scope.bEC.seleccionEventoCapital.length != 1)
		return;
	else{
		$scope.bEC.seleccionEventoCapital = null;
		$scope.bEC.seleccionEventoCapital = oEventoCapital[0]; 
	}
 	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
 	if($scope.bEC.seleccionEventoCapital){
	  var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_PRORRATEA_SORTEO_LETRAS"));
	  t.result.then(function(result){
		if (result===OPCIONES_BCSALERTA.SI) {
			var datos = {};	
			//overlay.encendido = true;
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			
			datos.p_IdEmpresa 						= $scope.SesionIdEmpresa; //saca de variables de entorno
			datos.p_IdUsuario 						= $scope.SesionIdUsuario; //saca de variables de entorno
			datos.p_IdModulo 						= 'CUS';
			
			datos.p_IdEventoCapital					= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;
			datos.p_IdTipoEventoCapital				= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital;
			datos.p_Secuencia						= $scope.bEC.seleccionEventoCapital.Secuencia;
			datos.p_IdClasificacionTipoInstrumento	= $scope.bEC.seleccionEventoCapital.IdClasificacionTipoInstrumento;
			datos.p_IdTipoInstrumento				= $scope.bEC.seleccionEventoCapital.IdTipoInstrumento;
			datos.p_IdInstrumento					= $scope.bEC.seleccionEventoCapital.IdInstrumento;
			datos.p_Nemo							= $scope.bEC.seleccionEventoCapital.Nemo;
			datos.p_FechaPago						= util.dateSPdate($scope.bEC.seleccionEventoCapital.FechaPago)||'1900-01-01';
			datos.p_IdEstado						= 'PRO'
			
// ME FALTA REGISTRAR EL SERVICIO Y PROBARLO, CON ESO DEJARÍAMOS LISTO EL PRORRATEO DEL SORTEO DE LETRAS.
				
//			bcsServiciosEventoCapital.ProrrateoSorteoLetras(datos,successProrrateoSorteoLetras,errorProrrateoSorteoLetras);
			bcsServiciosEventoCapital.ProrrateoSorteoLetras				
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
			bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		});
		return;   
  }
  

};	

	
/*************************************************************************
* CIERRE DE SORTEO DE LETRAS
**************************************************************************/
 $scope.CerrarSorteoLetras = function(oEventoCapital){
	if($scope.bEC.seleccionEventoCapital.length != 1)
		return;
	else
	{
		$scope.bEC.seleccionEventoCapital = null;
		$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
	}
 	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
		bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
		return;
	}
  var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_CERRAR_SORTEO_LETRAS"));
	  t.result.then(function(result){
		if (result===OPCIONES_BCSALERTA.SI) {
			var datos = {};	
			//overlay.encendido = true;
			bcsComunes.ventanaOverlay($scope.idVentana,true);
			
			
			datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; //saca de variables de entorno
			datos.p_IdUsuario 				= $scope.SesionIdUsuario; //saca de variables de entorno
			datos.p_IdModulo 				= 'CUS';					
			datos.p_IdEventoCapital			= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;
			datos.p_IdTipoEventoCapital		= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital || '';     
			datos.p_Precio					= Number($scope.data.kngPrecio) || 0;
			
			bcsServiciosEventoCapital.CerrarEventoCapitalSorteoLetras
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
			bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
		});
		return;   

	};	

	
	/*************************************************************************
	* CARGA ARCHIVO DE EVENTOS DE CAPITAL - PARA PERÚ
	**************************************************************************/
	$scope.btnCargarEC = function(){
		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
	  	var codArch = 'ARCHIVO_EVENTO_CAPITAL';
		var titulo  = 'Evento de Capital';
		
		var datosArchivo = {codigoArchivo: codArch, titulo: 'Carga de '+ titulo};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false);
		bcsComunes.ventanaOverlay(idVentana,true);

		var EventoCapital=bcsComunes.ventanaPromesa(id1);
		EventoCapital.then(function(data){
			
			
			//el objeto data, contiene la informacion del archivo en formato JSON.
			console.log(data);
			if(!data || !data.fileData){
				bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}

			var parametrosSP = {};
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.SesionIdUsuario; //saca de variables de entorno
	        parametrosSP.p_IdModulo			= 'CUS';
	        parametrosSP.p_DataArchivo		= data.fileData;
	        
	        if(data.fileData.length){
	        	obtenerDatosDesdeArchivoEventoCapitalSP(parametrosSP).then(function(result) {
	        		if (result.P_OUT_ADVERTENCIASCONTADOR){
	        			bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
	        		}
	        		else if (result.P_OUT_ERRORESCONTADOR){
	        			bcsComunes.msg("error","Error","La operacion tiene errores");
	        		}
	        		else{
	        			// se limpian las grillas
	        			bcsComunes.msg("info","INFO","Archivo Cargado Exitosamente!!!");
	        		}
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	},
	        	function(result){
	        		bcsComunes.msg("error","Error","La operacion tiene errores");
	        		bcsComunes.ventanaOverlay(idVentana,false);
	        	});
	        }
	        else bcsComunes.ventanaOverlay(idVentana,false); 
		}); 
	return;
	};
	
	
	var obtenerDatosDesdeArchivoEventoCapitalSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=99";
		var r			= $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
		var post		= r.urlConsulta(p_datos);
		
		return post.$promise;
    };

	
    /*************************************************************************
    * REPLICAR
    **************************************************************************/
    $scope.btnReplicarEC = function(oEventoCapital){
    	if($scope.bEC.seleccionEventoCapital.length != 1)
			return;
		else
		{
			$scope.bEC.seleccionEventoCapital = null;
			$scope.bEC.seleccionEventoCapital = oEventoCapital[0];
		}
    	if (BCS_ESTADO_MODULO['CUS'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
        if ($scope.bEC.seleccionEventoCapital != undefined){
    		if ($scope.bEC.seleccionEventoCapital.IdTipoEventoCapital == 'DA' || $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital == 'DV' || $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital == 'CC'){
    			if ($scope.bEC.seleccionEventoCapital.IdEstado == 'PRO'){
    				var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_REPLICAR_MONTOS_DE_CARGA_EN_EVENTOS_CLIENTES"));
    				t.result.then(function(result){
    		    		if (result===OPCIONES_BCSALERTA.SI) {
    		    			var datos = {};	
    		    			bcsComunes.ventanaOverlay($scope.idVentana,true);
    		    			
    		    			datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; //saca de variables de entorno
    		    			datos.p_IdUsuario 				= $scope.SesionIdUsuario; //saca de variables de entorno
    		    			datos.p_IdModulo 				= 'CUS';					
    		    			datos.p_IdEventoCapital			= Number($scope.bEC.seleccionEventoCapital.IdEventoCapital) || 0;
    		    			datos.p_IdTipoEventoCapital		= $scope.bEC.seleccionEventoCapital.IdTipoEventoCapital || '';     
    		    			
    		    			bcsServiciosEventoCapital.ReplicarEventoCapital
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
    		    		}else{ 
    		    			bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
    		    		}
    		    	return});
    			}else{	
    				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_EVENTO_DEBE_ESTAR_EN_ESTADO_PROCESADO"));
    			}
    		}else{	
    				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_TIPO_EVENTO_NO_VALIDO_PARA_REPLICAR"));
    		}
    	}else{
    			bcsComunes.msg("warning",$translate.instant("UI_ADVERTENCIA"),$translate.instant("UI_DEBE_SELECCIONAR_UN_REGISTRO"));
    	}
    	return;
    	}; 
    	
    	$scope.btnCargaArchivo = function(){
    		if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
        	var datosArchivo = {codigoArchivo: 'DIVIDENDO_EN_DINERO_FFMM', titulo: 'Carga Dividendos en Dinero FFMM'};
        	var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
        	bcsComunes.ventanaPromesa(id1).then(function(result){
			if(!result || !result.fileData){
				 bcsComunes.ventanaOverlay(idVentana,false);
				 return;
			}
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'CUS'
			}
			datos.p_Dividendos = result.fileData;
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosEventoCapital.CargaMasivaDividendosEnDineroFFMM(datos, function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR < 1){
					bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
				} else{
					if (respuesta.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
					} else if (respuesta.P_OUT_ADVERTENCIASCONTADOR > 0){
						bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)}, $scope.idVentana);
					}
				}	 
			});
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			console.log(result);
		})
    	};
});