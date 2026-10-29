angular.module('ModuloTesoreria').controller('ModalChequerasCtrl',function($scope,$translate,
		bcsServiciosTesoreria,bcsComunes) {
	
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametrosVentana =  bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);

	$scope.FechaRetiro 		  = util.dateInicioDia($scope.SesionFechaSistema);
	
	//CONFIGURACION COMBOBOX	
	$scope.oConfiguracionBanco={
			tipo          : "combobox-banco-cuentas-bancarias-corredora",
			configuracion : {},
			filtros       : {
				IdEstado    : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" }
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',		
			idPostFiltro  :	false
	};
	
	$scope.oConfiguracionCuentaBancaria={
			tipo          : "combobox-cuentas-bancarias-corredora",
			configuracion : {},
			filtros       : {
				IdBanco : {"field" : "IdBanco", "operator" : "eq", "value" : "" }
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',		
			idPostFiltro  :	false
	};
	
	$scope.oConfiguracionConfigChequera={
			tipo          : "combobox-configuracion-chequeras",
			configuracion : {},
			filtros       : {
				//NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "TES.CuentaBancaria" },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',
			idPostFiltro  :	false
	};
	
	$scope.oConfiguracionEstado={
			tipo          : "combobox-estado-tesoreria",
			configuracion : {},
			filtros       : {
				NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "TES.CuentaBancaria" },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',
			idPostFiltro  :	false
	};
	
	if (parametrosVentana.Chequera){
		
		var objChequera = $.extend({}, parametrosVentana.Chequera);
		
		$scope.oConfiguracionBanco.idDefecto			= objChequera.IdBanco;
		$scope.oConfiguracionBanco.idActual				= objChequera.IdBanco;
		$scope.oConfiguracionCuentaBancaria.idDefecto 	= objChequera.IdCuentaBanco;
		$scope.oConfiguracionCuentaBancaria.idActual 	= objChequera.IdCuentaBanco;
		$scope.FechaRetiro 								= util.dateInicioDia(objChequera.FechaRetiro);
		$scope.Serie 									= objChequera.Serie;
		$scope.CantidadCheques 							= objChequera.CantidadCheques;
		$scope.Remanente	 							= objChequera.Remanente;
		$scope.NroChequeDesde 							= objChequera.NumeroChequeDesde;
		$scope.NroChequeHasta 							= objChequera.NumeroChequeHasta;
		$scope.NroChequeActual 							= objChequera.NumeroChequeActual;
		$scope.oConfiguracionEstado.idDefecto 			= objChequera.IdEstado;
		$scope.oConfiguracionEstado.idActual 			= objChequera.IdEstado;
		$scope.oConfiguracionConfigChequera.idDefecto	= objChequera.IdConfiguracionChequera;
		$scope.oConfiguracionConfigChequera.idActual 	= objChequera.IdConfiguracionChequera;
		
	}
	
	var parametrosModificados = function(){
		var parametros = new Array();
		
		if ($scope.oConfiguracionCuentaBancaria.idActual != objChequera.IdCuentaBanco)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Id Cuenta Bancaria', ValorAntiguo: objChequera.IdCuentaBanco, 
																							ValorNuevo: $scope.oConfiguracionCuentaBancaria.idActual});
		if (util.formatoDate($scope.FechaRetiro) != util.formatoDate(util.dateInicioDia(objChequera.FechaRetiro)))
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Fecha Retiro', ValorAntiguo: util.dateInicioDia(objChequera.FechaRetiro), 
																						ValorNuevo: $scope.FechaRetiro});
		if ($scope.Serie !== objChequera.Serie)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Serie', ValorAntiguo: objChequera.Serie, 
																				ValorNuevo: $scope.Serie});
		if ($scope.CantidadCheques !== objChequera.CantidadCheques)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Cantidad Cheques', ValorAntiguo: objChequera.CantidadCheques, 
																							ValorNuevo: $scope.CantidadCheques});
		if ($scope.NroChequeDesde !== objChequera.NumeroChequeDesde)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Numero Cheque Desde', ValorAntiguo: objChequera.NumeroChequeDesde, 
																								ValorNuevo: $scope.NroChequeDesde});
		if ($scope.NroChequeHasta != objChequera.NumeroChequeHasta)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Numero Cheque Hasta', ValorAntiguo: objChequera.NumeroChequeHasta, 
																								ValorNuevo: $scope.NroChequeHasta});
		if ($scope.oConfiguracionEstado.idActual !== objChequera.IdEstado)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'Estado', ValorAntiguo: objChequera.IdEstado, 
																				ValorNuevo: $scope.oConfiguracionEstado.idActual});
		if ($scope.oConfiguracionConfigChequera.idActual !== objChequera.IdConfiguracionChequera)
			parametros.push({SubRegistro : 'Chequera', CampoRegistro: 'IdConfiguracionChequera', 	ValorAntiguo: objChequera.IdConfiguracionChequera, 
																									ValorNuevo: $scope.oConfiguracionConfigChequera.idActual || ''});
		console.log("PARAMETROS MODIFICADOS: " +  JSON.stringify(parametros));
		return parametros
	}
	
	//LLAMADA A SERVICIO
	var successGrabarChequera = function(respuesta){
		if (respuesta.P_OUT_ERRORESCONTADOR != undefined && respuesta.P_OUT_ERRORESCONTADOR == 0){
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			
			if (parametrosVentana.Chequera)
			{ // Modificar
				bcsComunes.msg("info","Informacion",$translate.instant('DATOS_MODIFICACION_EXITO'));
			}
			else
			{ // Crear
				bcsComunes.msg("info","Informacion",$translate.instant('DATOS_INGRESO_EXITO'));	
			}
			
			bcsComunes.ventanaOverlay(idVentana, false);
			$scope.cancelar(true);   //Cerrar ventana modal
		}
		else {
					
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("error","Error",$translate.instant('UI_PROCESO_REALIZADO_CON_ERROR'));
			
			//console.log("Error:" + JSON.stringify(respuesta.P_OUT_ERRORESDETALLE));			
            var i = 0;           
            for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
            {
                console.log("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);                
            }
            bcsComunes.ventanaOverlay(idVentana, false);	
		}
	};
	
	var errorGrabarChequera = function(respuesta)
	{	
		
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));		
		bcsComunes.msg("error","Error",$translate.instant('UI_PROCESO_REALIZADO_CON_ERROR'));
		bcsComunes.ventanaOverlay(idVentana, false);	
	};
	
	$scope.grabarChequera = function (){
		
		bcsComunes.ventanaOverlay(idVentana, false);	
		
		var parametrosSP = {};
				
		parametrosSP.p_IdEmpresa				= $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario				= $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo					= 'TES';
		parametrosSP.p_simularOperacion			= 'N';
		parametrosSP.p_UsaReglasNegocio 		= 'S';
		parametrosSP.p_IdCuentaBanco 			= $scope.oConfiguracionCuentaBancaria.seleccionado.IdCuentaBanco || 0;
		parametrosSP.p_FechaRetiro				= util.dateSPdate($scope.FechaRetiro);
		parametrosSP.p_Serie					= $scope.Serie || '';
		parametrosSP.p_CantidadCheques			= $scope.CantidadCheques || 0;
		parametrosSP.p_NumeroChequeDesde 		= $scope.NroChequeDesde || 0;
		parametrosSP.p_NumeroChequeHasta		= $scope.NroChequeHasta || 0;
		parametrosSP.p_IdEstado					= $scope.oConfiguracionEstado.seleccionado.IdEstado || '';
		parametrosSP.p_IdConfiguracionChequera	= $scope.oConfiguracionConfigChequera.seleccionado.IdConfiguracionChequera;
		
		console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
		
		if (parametrosVentana.Chequera){
			//	Modificar
			parametrosSP.p_IdChequera		= parametrosVentana.Chequera.IdChequera;
			parametrosSP.p_Dato 			= parametrosModificados();
			parametrosSP.p_FechaModificacion =  util.dateSPdate(new Date())
			
			console.log("PARAMETROS: " +  JSON.stringify(parametrosSP));
			bcsServiciosTesoreria.ModificarChequera(parametrosSP, successGrabarChequera, errorGrabarChequera);
		}
		else {
			// Crear
			parametrosSP.p_Remanente			= $scope.Remanente;
			parametrosSP.p_NumeroChequeActual 	= $scope.NroChequeActual;
			bcsServiciosTesoreria.CrearChequera(parametrosSP, successGrabarChequera, errorGrabarChequera);
		}
	};
	
	$scope.cancelar = function(respuesta) {		
		bcsComunes.ventanaData(idVentana,respuesta);  //Asocia data a la ventana, la cual será enviada a la respuesta de la promesa		
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.$watch('NroChequeDesde', function(newValue, oldValue){
		if (newValue === oldValue) return;
		if (newValue !== false && newValue !== 0 && newValue !== null) {
			$scope.NroChequeHasta = (Number($scope.NroChequeDesde) + Number($scope.CantidadCheques) - 1);
			if(!parametrosVentana.Chequera){
				$scope.NroChequeActual = $scope.NroChequeDesde;
			}
		}
		else $scope.NroChequeHasta = '';
	});

	
	$scope.AnularCheque = function() {

		//Abre Modal de edición
		var info = {};
		info.IdChequera = parametrosVentana.Chequera.IdChequera;
		info.IdSerieActual = parametrosVentana.Chequera.NumeroChequeActual;
		info.p_IdEmpresa = $scope.SesionIdEmpresa;
		info.p_IdUsuario = $scope.SesionIdUsuario;
		info.p_IdModulo = 'TES';
		info.p_simularOperacion	= 'N';
		info.p_UsaReglasNegocio	= 'S';
		
		var ventanaAnualar = bcsComunes.ventanaAbrir("AnularCheque", info, false, false,idVentana);
		var promesa=bcsComunes.ventanaPromesa(ventanaAnualar);

        promesa.then(function(data){
    		// Actualizar ultimo cheque disponible
    		//
    		if (data.Accion === 'ANULAR') {
    			$scope.NroChequeActual = data.NuevaSerie;	
    			$scope.Remanente = data.NuevoDisponible;

    		}
		});

	};	
	
	
	$scope.$watch('CantidadCheques', function(newValue, oldValue){
		if (newValue === oldValue) return;
		if (newValue !== false && newValue !== 0 && newValue !== null) {
			if(!parametrosVentana.Chequera){
				$scope.Remanente = $scope.CantidadCheques;
			}
		}
	});

	var firstHeight = $('#Win_editarChequeras');
        firstHeight.css('height', '335px');
	
});