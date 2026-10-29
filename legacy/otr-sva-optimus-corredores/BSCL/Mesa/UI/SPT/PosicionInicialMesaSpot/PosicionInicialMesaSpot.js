+(function() {
	angular.module('ModuloMesa').controller('PosicionInicialMesaSptCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		
		var contadorHabilita = 0;
		var varIdPosicionInicial;
		
		$scope.data = {};
		$scope.data.oConfiguracionPosicionInicial =  {min: -999999999999999}
		$scope.data.oConfiguracionMoneda = {};
		$scope.data.oConfiguracionMoneda.idDefecto = 'USD';
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.bloqueoGrabar = false;
		
		
		//Funciones
		var successGrabarPuntas = function(respuesta){
			//si vuelve ref id, se creo la asignacion
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){		
				bcsComunes.msg("info","info",$translate.instant('UI_OPERACION_INGRESO_EXITO'));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			else{			
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_OPERACION_INGRESO_ERROR"));
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			}
			overlay.encendido = false;
			$scope.data.bloqueoGrabar = false;
			bcsComunes.ventanaOverlay(idVentana, false);	
		};
		
		
		var error = function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay(idVentana, false);
		};
		
		
		//GUARDAR POSICION INICIAL
		$scope.data.grabarDatos = function(){
			var tablaDatosPosicion = [];		
			var ObjPosicion = {
			     "IdPosicionInicial": varIdPosicionInicial || 0
			    ,"Fecha"			: util.dateSPdate($scope.data.kngFecha)
			    ,"IdMoneda"			: $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda
				,"TipoCambioInicial": $scope.data.kngTipoCambioInicial
				,"CierreIngresado"	: $scope.data.kngCierreIngresado
				,"PosicionInicial"	: $scope.data.kngPosicionInicial
				,"TipoInstrumento"	: 'SPT'
				,"Estado"			: 'ACT'
				,"ResultadoPosicionInicial" : ($scope.data.kngTipoCambioInicial - $scope.data.kngCierreIngresado) * $scope.data.kngPosicionInicial
			};
			tablaDatosPosicion.push(ObjPosicion);
	
			var parametros = {};
			parametros.p_IdEmpresa      = $scope.SesionIdEmpresa;
			parametros.p_IdUsuario		= $scope.SesionIdUsuario;
			parametros.p_IdMoneda		= $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
			parametros.p_Fecha			= util.dateSPdate($scope.data.kngFecha);
			parametros.p_DatosPosicion  = tablaDatosPosicion;
			
	       	bcsServiciosMesa.CrearPosicionInicial(parametros,successGrabarPuntas,error);
		};
		
		
		//WATCH
		$scope.$watch('data.oConfiguracionMoneda.seleccionado',function(newval,oldval){
			if(newval === undefined || newval === oldval || newval === false) return;
			if(!$scope.data.oConfiguracionMoneda || !$scope.data.oConfiguracionMoneda.idActual || $scope.data.oConfiguracionMoneda.idActual === '') return;
			
			//Valores para el SP Consultar Puntas
			var datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdMoneda = $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda;
			datos.p_Fecha = util.dateSPdate($scope.data.kngFecha);
			
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosMesa.ConsultarPosicionInicial(datos).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		
		   		if (respuesta.P_OUT_POSICION.length === 0){
					$scope.data.kngTipoCambioInicial = 0;
					$scope.data.kngCierreIngresado = 0;
					$scope.data.kngPosicionInicial = 0;
				}
				else{
	            	varIdPosicionInicial			= respuesta.P_OUT_POSICION[0].IdPosicionInicial;
					$scope.data.kngTipoCambioInicial= respuesta.P_OUT_POSICION[0].TipoCambioInicial;
					$scope.data.kngCierreIngresado	= respuesta.P_OUT_POSICION[0].CierreIngresado;
					$scope.data.kngPosicionInicial	= respuesta.P_OUT_POSICION[0].PosicionInicial;
	            }
				bcsComunes.ventanaOverlay(idVentana,false);
	   			
	   		},function(respuesta){
	   			bcsComunes.msg("error","error","Existen problemas al consultar la Posición Inicial");
				bcsComunes.ventanaOverlay(idVentana,false);
	   		});
		});
		
		
		$scope.$watch('data.kngTipoCambioInicial',function(newval,oldval){  
			contadorHabilita++;
			if ((oldval === undefined) || (newval === oldval)){
				$scope.data.bloqueoGrabar = false;
				return
			}
			if ((newval !== oldval) && (oldval != undefined) && (contadorHabilita > 3)){
				$scope.data.bloqueoGrabar = true;
			}
		});
		
		
		$scope.$watch('data.kngCierreIngresado',function(newval,oldval){  
			contadorHabilita++;
			if ((oldval === undefined) || (newval === oldval)){
				$scope.data.bloqueoGrabar = false;
				return
			}
			if ((newval !== oldval) && (oldval != undefined) && (contadorHabilita > 3)){
				$scope.data.bloqueoGrabar = true;
			}		
		});
		
		
		$scope.$watch('data.kngPosicionInicial',function(newval,oldval){  
			contadorHabilita++;
			if ((oldval === undefined) || (newval === oldval)){
				$scope.data.bloqueoGrabar = false;
				return
			}
			if ((newval !== oldval) && (oldval !== undefined) && (contadorHabilita > 3)){
				$scope.data.bloqueoGrabar = true;
			}
		});
		
		
		$scope.$watch('data.oConfiguracionMoneda.seleccionado',function(newval,oldval){  
			if ((oldval === undefined) || (newval !== oldval)){
				contadorHabilita = 0; 
			}
		});
		
	});
}());