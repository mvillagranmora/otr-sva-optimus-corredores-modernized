angular.module('ModuloFFM').controller('ResumenCargaOperacionFFMCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var conf = bcsConfiguracionFFM.getConsultaResumenCargaOperacionesFondosMutuos();
	var apiGrid = conf.options.dataSource;
	$scope.buscarResumen = conf.buscar;
	$scope.defaultOptions = conf.options;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.resumenCargaOperacionesFFM = function (items) {
		bcsComunes.ventanaAbrir("ResumenCargaOperacionesFFM", {} );
  	};
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.oConfiguracionTipoOperacion = {
		filtros : {
			TieneCarga : { "field": "TieneCarga","operator": "eq", "value"   :  'S' }
		}
	}
	
	$scope.anularCargaFFMM = function(cargas){
		if(!cargas || cargas.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ANULACION_CARGA_APORTE_RESCATE'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'FMM',
		  			p_IdCargas 	  : [],
	    		};
		    	$.each(cargas,function(key,carga){
		  			datos.p_IdCargas.push({IdCarga : carga.IdCarga});
		  		});
		    	overlay.encendido = true;
		    	bcsServicioFondosMutuos.AnulacionCargaSolicitudAporteRescate(datos).$promise.then(function(result){
		    		if(result.P_OUT_ERRORESCONTADOR === 0){
		    			var operaciones = [];
						$.each(result.P_OUT_RESULTADO,function(key,value) {
							operaciones.push({id: value.IdCarga , resultado: value.Codigo, mensaje: ''});
						});

			     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
							tituloVentana : $translate.instant("UI_INFORMACION"), 
							nombreEntidad : $translate.instant("UI_SECUENCIA"), 
							operaciones : operaciones   
						});
		    		}else{
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ANULAR_CARGA_APORTE_RESCATE"),conflictos:util.parseServerResponse(result)},$scope.winID);
		    		}
		    		$scope.buscarResumen($scope.apiGrid,{});
		    		overlay.encendido = false;
		    	},function(result){
		    		console.log(result);
		    		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_ANULAR_CARGA_APORTE_RESCATE"),conflictos:util.parseServerResponse(result)},$scope.winID);
		    		$scope.buscarResumen($scope.apiGrid,{});
		    		overlay.encendido = false;
		    	});
			}
		});
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.IdTipoMovimientoCarga = 0;
		$scope.filtros.IdEmisor = 0;
	};
	
	$scope.agendarCargaFFMM = function(cargasTmp)
    {	
		 if(!cargasTmp || cargasTmp.length == 0) return;
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosCargaMasiva = {
				 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				 p_IdModulo : 'FMM',
				 p_IdCargas : []
		 }
		 $.each(cargasTmp,function(key,sol){
			 datosCargaMasiva.p_IdCargas.push({IdCarga : sol.IdCarga});
	  		});
	     bcsServicioFondosMutuos.CrearProgramaCargaBatch(datosCargaMasiva).$promise.then(function(respuesta){
	     	 
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		bcsComunes.ventanaMsg({mensaje:"UI_OK_AGENDA_PROGRAMA_CARGA_MASIVA: " + respuesta.P_OUT_RESULTADO[0].IdPrograma,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_AGENDA_PROGRAMA_CARGA_MASIVA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_AGENDA_PROGRAMA_CARGA_MASIVA"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	     overlay.encendido = false;
    }
	
	$scope.limpiarFiltros();
});