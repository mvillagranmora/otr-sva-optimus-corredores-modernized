angular.module('ModuloCustodia').controller('IngresarJuntaAccionistasCtrl',
		function($scope, $timeout, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, $translate, bcsBlotterConsultaJunta, bcsServiciosJuntaAccionista){
	$scope.filtros = {};
	$scope.data = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.filtros.FecInicio 	 = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.FecTermino	 = util.dateInicioDia($scope.SesionFechaSistema);
    var evento = $scope.parametros.evento;
	$scope.datosAntes = {};
	$scope.datosDespues = {};
	console.log(evento);
	$scope.EsModificacion = 'N';
	$scope.fechaCierreEditable = 'S';
	if (evento)
	{
		$scope.EsModificacion = 'S';
		$scope.EstadoValido = $scope.datosAntes.IdEstadoJunta = 'VIG' ? true : false; 
		$scope.datosAntes.IdEmisor = evento.IdEmisor;
		$scope.datosAntes.tipoJuntaAccionista = evento.IdTipoJunta;
		$scope.datosAntes.FecRealizacionUI = util.formatoDate(evento.FechaRealizacion);
		$scope.datosAntes.FecRealizacion = evento.FechaRealizacion;
		$scope.datosAntes.HoraRealizacion = evento.HoraRealizacion;
		$scope.datosAntes.FecCierreUI = util.formatoDate(evento.FechaCierre);
		$scope.datosAntes.FecCierre = evento.FechaCierre;
		$scope.datosAntes.nombreEmisor = evento.NombreEmisor;
		$scope.datosAntes.lugar = evento.Lugar;
		$scope.datosAntes.instruccionMasiva = evento.Instruccion;
		$scope.datosAntes.cuadraturaDCV = evento.CuadraturaDCV;
		$scope.datosAntes.materias = evento.Materias;
		$scope.datosAntes.TipoJunta = evento.TipoJunta;
		$scope.fechaCierreEditable = evento.FechaCierreEditable;
		$scope.datosDespues = angular.copy($scope.datosAntes);
	}
	
	
	$scope.data.filtrosEmisor = {
			IdEmisor : { "field"   : "IdEmisor","operator": "eq", "value"   :  $scope.datosAntes.IdEmisor}
	};

	$scope.data.oConfiguracionTipoJuntaAccionista={
			tipo : "combobox-tipo-junta-accionista",
			configuracion : {},
			filtros : { 	
				NombreEntidad : {"field" : "Entidad", "operator" : "eq", "value" : "TIPOJUNTA"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};	
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.$watch('emisor.seleccionado',function(newval,oldval){
		 if (newval && newval.NombreCompleto) {
				$scope.datosDespues.nombreEmisor = newval.NombreCompleto;
		    }
	 });
	
	$scope.ingresarJuntaAccionista = function() {
		bcsComunes.ventanaOverlay($scope.winID,true);
		if($scope.datosDespues.FecRealizacion > $scope.datosDespues.FecCierre) {
			var datosIngresoEventoJunta = {
					 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
					 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
					 p_IdModulo : 'CUS'
			}
			if($scope.EsModificacion === 'N'){
				datosIngresoEventoJunta.p_JuntaAccionista = [{
				    IdInterno		      		:	0,
				    IdTipoJuntaAccionista 	    :	$scope.datosDespues.tipoJuntaAccionista,
				    FechaRealizacion          	:	util.dateSPdate($scope.datosDespues.FecRealizacion),
				    HoraRealizacion         	:	$scope.datosDespues.HoraRealizacion,
				    IdEmisor          			:	$scope.datosDespues.IdEmisor,
				    FechaCierre   				:   util.dateSPdate($scope.datosDespues.FecCierre),
				    Lugar 	  					:	$scope.datosDespues.lugar,
				    Instruccion 	  			:	$scope.datosDespues.instruccionMasiva || '',
				    CuadraturaDCV   			:	$scope.datosDespues.cuadraturaDCV || 0,
				    Materias 		  			:   $scope.datosDespues.materias || ''
				}]
				bcsServiciosJuntaAccionista.CrearEventoJuntaAccionista(datosIngresoEventoJunta).$promise.then(function(respuesta){
			     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     		bcsBlotterConsultaJunta.ingresarCerrarVentana(id,$scope.winID);
			     	 }else{
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     	 }
		  		 },function(respuesta){
		  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CREAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		  		 });
			}else{
				datosIngresoEventoJunta.p_JuntaAccionista = [{
				    IdInterno		      		:	0,
				    IdEventoJuntaAccionista 	:	evento.IdEventoJuntaAccionista,
				    FechaCierre   				:   util.dateSPdate($scope.datosDespues.FecCierre),
				    Lugar 	  					:	$scope.datosDespues.lugar,
				    Instruccion 	  			:	$scope.datosDespues.instruccionMasiva || '',
				    CuadraturaDCV   			:	$scope.datosDespues.cuadraturaDCV || 0,
				    Materias 		  			:   $scope.datosDespues.materias || ''
				}]
				bcsServiciosJuntaAccionista.ModificarEventoJuntaAccionista(datosIngresoEventoJunta).$promise.then(function(respuesta){
			     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     		bcsBlotterConsultaJunta.ingresarCerrarVentana(id,$scope.winID);
			     	 }else{
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     	 }
		  		 },function(respuesta){
		  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_JUNTA_ACCIONISTA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		  		 });
			}
		} else {
			$scope.datosDespues.FecRealizacion	= undefined;
			$scope.datosDespues.FecCierre		= undefined;
			
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_FECHA_REALIZACION_DEBE_SER_MAYOR_FECHA_CIERRE"));
			bcsComunes.ventanaOverlay($scope.winID,false);
			return;
		}
		
	}
});