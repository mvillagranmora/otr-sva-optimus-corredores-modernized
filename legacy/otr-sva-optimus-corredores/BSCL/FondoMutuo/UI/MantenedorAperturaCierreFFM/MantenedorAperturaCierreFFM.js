angular.module('ModuloFFM').controller('MantenedorAperturaCierreCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.formData.EsNacional = false;
	var conf = bcsConfiguracionFFM.getConsultaAperturaCierreFondosMutuos();
	var ds = conf.options.dataSource;
	$scope.buscarInstrumentosHoraCierre = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var filtrosHorario = [];
	filtrosHorario.push({ "field" : "Perfil","operator": "eq", "value":"PERFIL_COMERCIAL"});
	$scope.formData.filtrosEmisor = {
			IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	ds.definition.filter = filtrosHorario;
	$scope.defaultOptions = {
			userSettings:true,
			sortable: true,
      		reorderable: true,
      		filterable: true,
			groupable: true,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: true,
      		scrollable: true,
      		height:450,
      		columns: conf.options.columns,
			dataSource:ds
	}
	
	$scope.configurarHorariosFMM = function()
	{
		bcsComunes.ventanaAbrir("MantenedorHorarioCierreFFM", {} );
	};
	
	$scope.configurarAperturaCierreBloqueoFMM = function(){
		var parametro = ($scope.datos && $scope.datos.kNgModel) ? $scope.datos.kNgModel : null
		var id = bcsComunes.ventanaAbrir("MantenedorAperturaCierreBloqueoFFM", parametro);
		var promesa=bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function(data){
			conf.options.dataSource.read();			
			$scope.apiGridInstrumentos.clearSelection();
	    });
	};
	
	$scope.reapertura = function(series){
		if(!series || series.length == 0) return;
		var serie = series[0];
		var id = bcsComunes.ventanaAbrir("MantenedorReaperturaFondos", {IdFondo : serie.IdFondo, FechaFondo: serie.FechaFondo} );
		var promesa=bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function(data){
			conf.options.dataSource.read();
			$scope.datos.kNgModel = undefined;
	    });
	}
	
	$scope.abrirCerrarFondos = function(series,accion){
		bcsComunes.ventanaOverlay($scope.winID,true);
		if(!series || series.length == 0){
			bcsComunes.ventanaOverlay($scope.winID,false);
			return;
		};
		var mensaje = accion === 'ABRIR' ? 'UI_ADVERTENCIA_APERTURA_FONDO' : 'UI_ADVERTENCIA_CIERRE_FONDO';
		var estado = accion === 'ABRIR' ? 'A' : 'C';
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant(mensaje));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'FMM',
		  			p_UsaReglasNegocio : 'N',
		  			p_Fondo : [],
	    		};
		    	$.each(series,function(key,serie){
		  			datos.p_Fondo.push({IdFondo : serie.IdFondo, EstadoAporte: estado, EstadoRescate: estado, EstadoReinversion: estado, EstadoLiquidez: estado});
		  		});
		    	bcsServicioFondosMutuos.ModificarAperturaCierreInstrumento(datos).$promise.then(function(result){
		    		var operaciones = [];
					$.each(result.P_OUT_RESULTADO,function(key,value) {
						operaciones.push({id: value.IdFondo , resultado: $translate.instant(value.Codigo), mensaje: ''});
					});
		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					});
					
		    		$scope.buscarInstrumentosHoraCierre($scope.apiGridInstrumentos,$scope.filtros);
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    	},function(result){
		    		console.log(result);
		    		$scope.buscarInstrumentosHoraCierre($scope.apiGridInstrumentos,$scope.filtros);
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    	});
			}
			else{
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
		});
	}
	
	$scope.abrirAdmPerfilFondoMutuo = function(){
		bcsComunes.ventanaAbrir("MantenedorPerfilesFondoMutuo", {} );
	}
	
	$scope.limpiarFiltros = function(){
		$scope.filtros.IdEmisor = false;
		$scope.filtros.IdMoneda = false;
		$scope.filtros.IdPais = false;
		$scope.filtros.PerfilFondoMutuo = 'PERFIL_COMERCIAL';
		$scope.formData.setFondo = "-1";
		$scope.filtros.EsNacional = false;
	}
	
	$scope.aperturaAutomaticaFondoMutuo = function(){
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datosServicio = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario
		};
		
		bcsServicioFondosMutuos.AperturaAutomaticoFondos(datosServicio).$promise.then(function(respuesta){
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSerieFondo != '' && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OK_APERTURA_AUTOMATICA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		conf.options.dataSource.read();
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_APERTURA_AUTOMATICA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		bcsComunes.ventanaOverlay($scope.winID,false);
	     	 }
  		 },function(respuesta){
  		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_APERTURA_AUTOMATICA_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 bcsComunes.ventanaOverlay($scope.winID,false);
  		 });
	}
});