angular.module('ModuloNormativa').controller('BlotterEsquemaDeclaracionTributarioCtrl',function($scope,$translate,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionMaestroDeclaraciones,bcsServicioMaestroDeclaraciones,$timeout){
	
	$scope.filtros = {};
	$scope.filtros.modulo = 'TRI';
	$scope.datos = {};
	var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaUltimaVersionEsquema();
	
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscarEsquemas = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	$scope.datos.oConfiguracionEsquema = {
		filtros:{
			Modulo : { "field"   : "Modulo","operator": "eq", "value"   :  "TRI"}
		}	
	}
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.esquema = '0';
	};
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	
	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
	
	$scope.nuevoEsquema = function(datos){
		bcsComunes.ventanaAbrir("AgregarEsquemaDeclaraciones", {} );
	}
	
	$scope.abrirPeriodo = function(){
		bcsComunes.ventanaAbrir("AbrirPeriodoDeclaraciones", {} );
	}
	
	$scope.activarEsquema = function(datos){
		if(!datos || datos.lenght == 0) return;
		bcsComunes.ventanaAbrir("ActivarEsquemaDeclaraciones", {esquema : datos[0].Esquema} );
	}
	
	$scope.bloquearDesbloquearEsquema = function(esquemas){
		if(!esquemas || esquemas.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_BLOQUEO_DESBLOQUEO_ESQUEMA'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var datos = {};
				datos.p_esquemas = [];
		    	$.each(esquemas,function(key,es){
		    		datos.p_esquemas.push({Esquema : es.Esquema});
		  		});
		    	overlay.encendido = true;
		    	var operaciones = [];
		    	$.each(datos.p_esquemas,function(key,value) {
					operaciones.push({id: value.Esquema , resultado: $translate.instant('UI_EXITO'), mensaje: ''});
				});

	     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
					tituloVentana : $translate.instant("UI_INFORMACION"), 
					nombreEntidad : $translate.instant("UI_ESQUEMA"), 
					operaciones : operaciones   
				});
	     		overlay.encendido = false;
	    		$scope.datos.kNgModel = undefined;
			}
			
		});
	}
	
	$scope.limpiarFiltros();
	
	
	
})