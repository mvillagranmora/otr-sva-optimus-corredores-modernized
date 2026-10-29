angular.module('ModuloFFM').controller('ConfiguracionHorarioCierreCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaHorarioCierreFondosMutuos();
	var ds = conf.options.dataSource;
	var dsAporte = angular.copy(bcsConfiguracionFFM.getDataSourceHorarioFondo());
	var dsRescate = angular.copy(bcsConfiguracionFFM.getDataSourceHorarioFondo());
	var dsReinversion = angular.copy(bcsConfiguracionFFM.getDataSourceHorarioFondo());
	var dsLiquidez = angular.copy(bcsConfiguracionFFM.getDataSourceHorarioFondo());
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.ingresarHorarioCierre = function(params)
	{
		var id = bcsComunes.ventanaAbrir("IngresarHorarioCierreFFM", params, false, false, $scope.winID);
		var promesa=bcsComunes.ventanaPromesa(id);                               
	    $.when(promesa).then(function(data){
	    	$scope.completarTablas();
	    });
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
    }
	$scope.ajustaVentana();
	
	var obtenerHorariosCierre = conf.options.dataSource;
	
	obtenerHorariosCierre.query().then(function(){
		if (obtenerHorariosCierre.data().length!=1)
		{
			console.log("Solicitud de aporte rescate no existe!");
			return false;
		}
		else
		{
			$scope.$apply(function() {$scope.formData.Horarios = obtenerHorariosCierre.data()[0];});
			return true;
		}
	});
	
	$scope.buscarInstrumentoAportes = function(){
		$scope.formData.fondosAporte = undefined;
		var filter = {field: "UsaHorarioAporteDefault", operator: "eq", value: 'N'};
		dsAporte.query({filter:filter}).then(function(){
			if (dsAporte.data().length<1)
			{
				console.log("No existe Instrumentos con horario aporte");
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
				return false;
			}else
			{
				$scope.formData.fondosAporte = dsAporte.data();
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
			}
		})
	}
	
	$scope.buscarInstrumentoRescates = function(){
		$scope.formData.fondosRescate = undefined;
		var filter = {field: "UsaHorarioRescateDefault", operator: "eq", value: 'N'};
		dsRescate.query({filter:filter}).then(function(){
			if (dsRescate.data().length<1)
			{
				console.log("No existe Instrumentos con horario rescate");
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
				return false;
			}else
			{
				$scope.formData.fondosRescate = dsRescate.data();
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
			}
		})
	}
	
	$scope.buscarInstrumentoReinversion = function(){
		$scope.formData.fondosReinversion = undefined;
		var filter = {field: "UsaHorarioReinversionDefault", operator: "eq", value: 'N'};
		dsReinversion.query({filter:filter}).then(function(){
			if (dsReinversion.data().length<1)
			{
				console.log("No existe Instrumentos con horario reinversion");
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
				return false;
			}else
			{
				$scope.formData.fondosReinversion = dsReinversion.data();
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
			}
		})
	}
	
	$scope.buscarInstrumentoLiquidez = function(){
		$scope.formData.fondosLiquidez = undefined;
		var filter = {field: "UsaHorarioLiquidezDefault", operator: "eq", value: 'N'};
		bcsComunes.ventanaOverlay($scope.winID,true);
		dsLiquidez.query({filter:filter}).then(function(){
			if (dsLiquidez.data().length<1)
			{
				console.log("No existe Instrumentos con horario liquidez");
				bcsComunes.ventanaOverlay($scope.winID,false);
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
				return false;
			}else
			{
				$scope.formData.fondosLiquidez = dsLiquidez.data();
				bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
		})
	}
	
	$scope.formData.modificarHorarioCierre = function()
    {	 
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosModificarHorarioCierre = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_UsaReglasNegocio : 'S',
				 
				 p_Horario : [{
					    IdInterno		      		        :	0,
					    HoraCierreAporteComercial 	     :	$scope.formData.Horarios.HoraCierreAporteComercial,
					    HoraCierreAporteBackOffice       :	$scope.formData.Horarios.HoraCierreAporteBackOffice,
					    HoraCierreRescateComercial       :	$scope.formData.Horarios.HoraCierreRescateComercial,
					    HoraCierreRescateBackOffice      :	$scope.formData.Horarios.HoraCierreRescateBackOffice,
					    HoraCierreReinversionComercial   :  $scope.formData.Horarios.HoraCierreReinversionComercial,
					    HoraCierreReinversionBackOffice  :	$scope.formData.Horarios.HoraCierreReinversionBackOffice
				 }]
		 }
	     bcsServicioFondosMutuos.ModificarHorarioCierreDefault(datosModificarHorarioCierre).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.formData.Horarios = {
	     				HoraCierreAporteComercial       : respuesta.P_OUT_RESULTADO[0].HoraCierreAporteComercial,
	     				HoraCierreAporteBackOffice		: respuesta.P_OUT_RESULTADO[0].HoraCierreAporteBackOffice,
	     				HoraCierreRescateComercial      : respuesta.P_OUT_RESULTADO[0].HoraCierreRescateComercial,
	     				HoraCierreRescateBackOffice 	: respuesta.P_OUT_RESULTADO[0].HoraCierreRescateBackOffice,
	     				HoraCierreReinversionComercial  : respuesta.P_OUT_RESULTADO[0].HoraCierreReinversionComercial,
	     				HoraCierreReinversionBackOffice	: respuesta.P_OUT_RESULTADO[0].HoraCierreReinversionBackOffice
				   };
	     		$scope.ajustaVentana();
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_MODIFICAR_HORARIO_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_HORARIO_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_HORARIO_CIERRE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  			console.log('me cai jeje ' + respuesta);
  		 });
    }
	
	$scope.completarTablas = function(){
		$scope.buscarInstrumentoAportes();
		$scope.buscarInstrumentoRescates();
		$scope.buscarInstrumentoReinversion();
		$scope.buscarInstrumentoLiquidez();
	}
	
	$scope.completarTablas();
	
});