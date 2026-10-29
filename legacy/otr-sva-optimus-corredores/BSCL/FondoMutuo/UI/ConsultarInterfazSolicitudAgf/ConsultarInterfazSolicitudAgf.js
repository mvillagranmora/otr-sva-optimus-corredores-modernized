angular.module('ModuloFFM').controller('ConsultaInterfazSolicitudAgfCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaInterfazSolicitudAgf();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarSolicitudes = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.reprocesar = function(solicitudesTemp){
		if(!solicitudesTemp || solicitudesTemp.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_REPROCESAR_ERROR_SOLICITUD_AGF'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
	    			p_SolicitudesAGF : [],
	    		};
		    	$.each(solicitudesTemp,function(key,sol){
		  			datos.p_SolicitudesAGF.push({IdInterfazAgf : sol.IdInterfazAgf, IdEstado: 'PEN', Observacion: ''});
		  		});
		    	overlay.encendido = true;
		    	bcsServicioFondosMutuos.CambiarEstadoSolicitudAgf(datos).$promise.then(function(result){
		    		var operaciones = [];
					$.each(result.P_OUT_RESULTADO,function(key,value) {
						operaciones.push({id: value.IdInterfazAgf , resultado: $translate.instant(value.Codigo), mensaje: ''});
					});

		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					});
					
		    		$scope.buscarSolicitudes($scope.apiGridSolicitud,$scope.filtros);
		    		overlay.encendido = false;
		    	},function(result){
		    		console.log(result);
		    		$scope.buscarSolicitudes($scope.apiGridSolicitud,$scope.filtros);
		    		overlay.encendido = false;
		    	});
			}
		});
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaIngresoDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaIngresoHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.IdEstado = 0;
		$scope.formData.setFondo = "-1";
		$scope.filtros.TipoRegistro = undefined;
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.limpiarFiltros();
});