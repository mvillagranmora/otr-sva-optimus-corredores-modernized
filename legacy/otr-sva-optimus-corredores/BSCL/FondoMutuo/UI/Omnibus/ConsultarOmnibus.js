angular.module('ModuloFFM').controller('ConsultarOmnibusCtrl',
function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,$timeout)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.formData = {};
	var conf = bcsConfiguracionFFM.getConsultaOmnibusDetalle();
	var apiGrid = conf.options.dataSource;
	$scope.defaultOptions = conf.options;
	$scope.buscar = conf.buscar;
	idVentana=bcsComunes.ventanaWinId($scope);
	$scope.filtros = {};
	
	$scope.MostrarProducto = false;
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
		
	$scope.formData.filtrosEmisor = {
		IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
				
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
	$scope.ajustaVentana = function()
	{
		bcsComunes.ventanaAutoHeight(idVentana);
	}
	
	$scope.defaultOptions.gridCreated = function(grid)
	{
		$scope.ajustaVentana();
	}
	
	$scope.autorizarOmnibus = function(omnibus){
		if(!omnibus || omnibus.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_AUTORIZACION_OMNIBUS'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'FMM',
		  			p_IdEstado	  : 'AUT',
		  			p_Omnibus : [],
	    		};
		    	$.each(omnibus,function(key,sol){
		  			datos.p_Omnibus.push(sol.IdOmnibusDetalle);
		  		});
		    	bcsComunes.ventanaOverlay($scope.winID,true);
		    	bcsServicioFondosMutuos.AutorizarOmnibus(datos).$promise.then(function(result){
		    		if(result.P_OUT_ERRORESCONTADOR === 0){
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_AUTORIZACION_OK"),conflictos:util.parseServerResponse(result)},$scope.winID);
		    		}else{
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_AUTORIZACION_ERROR"),conflictos:util.parseServerResponse(result)},$scope.winID);
		    		}
					
		     		$scope.buscar($scope.apiGridSolicitud,$scope.filtros);
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    		$scope.datos.kNgModel = undefined;
		    	},function(result){
		    		console.log(result);
		    		$scope.buscar($scope.apiGridSolicitud,$scope.filtros);
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    		$scope.datos.kNgModel = undefined;
		    	});
			}
			
		});
	};
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaOmnibus = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.IdEstado = 0;
		$scope.filtros.IdEmisor = 0;
		$scope.formData.setFondo = "-1";
	};
		
	$scope.limpiarFiltros();
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});