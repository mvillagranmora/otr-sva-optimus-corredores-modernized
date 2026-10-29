angular.module('ModuloFFM').controller('ConfiguracionAperturaCierreBloqueoCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.formData.mostrarPlazos = false;
	$scope.formData.mostrarTipoOperacion = false;
	var conf = bcsConfiguracionFFM.getConsultaAperturaCierreFondosMutuos();
	var ds = conf.options.dataSource;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.formData.filtrosTipoOperacion = {
			IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value"   :  "INFM"}
	};
	
	$scope.defaultOptions = {
			columns: conf.options.columns,
			selectable : "row",
	  		filterable : {
	  			mode : "row"
	  		},	
	  		autoBind:true,
			height:450,
			dataSource:ds
	}
	
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.IdFondoSeleccionado = ($scope.parametros && $scope.parametros[0]) ? $scope.parametros[0].IdFondo : '0'
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }

	$scope.formData.modificarAperturaCierreBloqueo = function()
    {	 
		 bcsComunes.ventanaOverlay($scope.winID,true);

		 let bloqueoAporteVal   = '';
         let bloqueoRescateVal  = '';

		 const esTodos = !$scope.formData.tipoOperacion || 
                $scope.formData.tipoOperacion === '0' || 
                $scope.formData.tipoOperacion === 'TODOS';

		 if ($scope.formData.BloqueadoDesbloqueado === 'B') {
			// Bloqueo
			if (esTodos || $scope.formData.tipoOperacion === 'APORFM') {
				bloqueoAporteVal = 'S';
			}
			if (esTodos || $scope.formData.tipoOperacion === 'RESCFM') {
				bloqueoRescateVal = 'S';
			}
		} else if ($scope.formData.BloqueadoDesbloqueado === 'D') {
			// Desbloqueo
			if (esTodos || $scope.formData.tipoOperacion === 'APORFM') {
				bloqueoAporteVal = 'N';
			}
			if (esTodos || $scope.formData.tipoOperacion === 'RESCFM') {
				bloqueoRescateVal = 'N';
			}
		} else {
			
			bloqueoAporteVal  = '';
			bloqueoRescateVal = '';
		}

		 let datosModificarBloqueo = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_UsaReglasNegocio : 'N',
				 
				 p_Fondo : [{
					    IdInterno		  :	0,					    
					    IdFondo      	  : $scope.formData.IdFondo ? $scope.formData.IdFondo : '',
						IdEmisor          : $scope.formData.IdEmisor === 0 || !$scope.formData.IdEmisor ? '' : $scope.formData.IdEmisor,						
						BloqueoAporte     : bloqueoAporteVal,
        				BloqueoRescate    : bloqueoRescateVal,						
						BloqueoIndefinido : $scope.formData.BloqueoIndefinido ? $scope.formData.BloqueoIndefinido : '',
						FechaBloqueoDesde : $scope.formData.mostrarPlazos ? util.dateSPdate($scope.formData.fechaInicioBloqueo) : '',
						FechaBloqueoHasta : $scope.formData.mostrarPlazos ? util.dateSPdate($scope.formData.fechaTerminoBloqueo) : '',
					    
				 }]
		 }
	     bcsServicioFondosMutuos.ModificarBloqueoInstrumento(datosModificarBloqueo).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ajustaVentana();
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_APERTURA_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
				bcsComunes.ventanaCerrar($scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_APERTURA_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_APERTURA_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
    }
	
	$scope.ajustaVentana();
	
	$scope.$watch('formData.BloqueoIndefinido',function(newval,oldval) {
		if (newval === 'N'){
			$scope.formData.mostrarPlazos = true;
			$scope.ajustaVentana();
		}
		else{
			$scope.formData.mostrarPlazos = false;
			$scope.ajustaVentana();
		}
	});
	
	$scope.$watch('formData.BloqueadoDesbloqueado',function(newval,oldval) {
		if (newval === 'B') {			
			$scope.formData.mostrarOpcionesBloqueo = true;
			$scope.formData.mostrarBloqueoIndefinido = true;
		} else if (newval === 'D') {
			// Si está DESBLOQUEADO, muestro opciones de bloqueo pero NO Bloqueo Indefinido
			$scope.formData.mostrarOpcionesBloqueo = true;
			$scope.formData.mostrarBloqueoIndefinido = false;
		} else {
			// Si es 'NA' u otra opción, oculto todo el bloque
			$scope.formData.mostrarOpcionesBloqueo = false;
			$scope.formData.mostrarBloqueoIndefinido = false;
		}
		$scope.ajustaVentana();
	});
});