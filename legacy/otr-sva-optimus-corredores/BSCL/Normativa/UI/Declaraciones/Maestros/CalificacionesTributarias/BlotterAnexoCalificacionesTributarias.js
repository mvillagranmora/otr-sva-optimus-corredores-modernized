angular.module('ModuloNormativa').controller('BlotterAnexoCalificacionesTributariasCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServiciosNormativa,$timeout){
	$scope.configuraciones = {};
	$scope.filtros = {};
	$scope.datos = {};
	$scope.configuraciones.date = {
			start: "decade",                          
            depth: "decade",                           
            format: "yyyy"	
	}
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaAnexoCalificacionTributaria();
	$scope.defaultOptions = conf.options;
	$scope.buscarAnexo = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
	$scope.ingresarAnexo = function()
	{
		var id = bcsComunes.ventanaAbrir("IngresarAnexoCalificacionesTributarias", {} );
		var promesa = bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	conf.options.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        });
	};
	
	$scope.modificarAnexo = function(datos)
	{
		console.log(datos);
		if(!datos || datos.length == 0) return;
		var id = bcsComunes.ventanaAbrir("ModificarAnexoCalificacionesTributarias", {IdCalificacionTributaria: datos[0].IdCalificacionTributaria, Edicion: true});
		var promesa = bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	conf.options.dataSource.read();
        	$scope.datos.kNgModel = undefined;
        }); 
	};
	
	
	$scope.eliminarAnexo = function(anexos){
		if(!anexos || anexos.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ELIMINAR_ANEXO'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'NOR',
		  			p_SimularOperacion : 'N',
		  			p_UsaReglasNegocio : 'S',
		  			p_Anexo : [],
	    		};
		    	$.each(anexos,function(key,anexo){
		  			datos.p_Anexo.push({IdInterno : anexo.IdCalificacionTributaria, IdCalificacionTributaria: anexo.IdCalificacionTributaria, ValorHistorico: anexo.ValorHistorico, 
		  								IdEventoCapital: anexo.IdEventoCapital, Descripcion: anexo.Descripcion, SecuenciaEvento: anexo.SecuenciaEvento, TasaCredito: anexo.TasaCredito,
		  								PorcentajeDividendo: anexo.PorcentajeDividendo, IdTipoUtilidades: anexo.IdTipoUtilidades, IdEstado: 'INA'});
		  			
		  		});
		    	bcsComunes.ventanaOverlay($scope.winID,true);
		    	bcsServiciosNormativa.ModificarAnexoCalificacionTributaria(datos).$promise.then(function(result){
		    		var operaciones = [];
					$.each(result.P_OUT_RESULTADO,function(key,value) {
						operaciones.push({id: value.IdCalificacionTributaria , resultado: $translate.instant(value.Resultado), mensaje: ''});
					});

		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : operaciones   
					});
					
		    		$scope.buscarAnexo($scope.apiGridAnexo,$scope.filtros);
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    		$scope.datos.kNgModel = undefined;
		    	},function(result){
		    		console.log(result);
		    		$scope.buscarAnexo($scope.apiGridAnexo,$scope.filtros);
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    		$scope.datos.kNgModel = undefined;
		    	});
			}
			
		});
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.Anio = undefined;
		$scope.filtros.IdClasificacionTipoInstrumento = false;
		$scope.filtros.buscarInstrumento = "-1";
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.limpiarFiltros();
});