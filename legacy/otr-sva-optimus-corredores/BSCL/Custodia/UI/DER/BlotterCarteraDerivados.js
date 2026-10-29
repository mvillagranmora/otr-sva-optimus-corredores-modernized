angular.module('ModuloCustodia').controller('BlotterCustodiaDerivadosCtrl',function($scope,bcsComunes,$timeout,$compile, bcsServiciosCustodia){
	
	var tabCarteraOpcionesLoaded = false;
	var tabCarteraFuturosLoaded = false;
	var tabEjercicioOpcionesLaoded = false;
	var tabContratosOpcionesLaoded = false;
	var tabResultadoDiarioFuturosLaoded = false;
	var tabValorizacionFuturosLaoded = false;
	
	var idVentana = bcsComunes.ventanaWinId($scope);;
	var domVentana = $("[ventanaid='"+idVentana+"']");
	
	var domCarteraOpciones =  '<div ng-include="\'../../Custodia/UI/DER/CarteraOpciones.html\'" ></div>';
	var domCarteraFuturos =  '<div ng-include="\'../../Custodia/UI/DER/CarteraFuturos.html\'" ></div>';	
	var domEjercicioOpciones =  '<div ng-include="\'../../Custodia/UI/DER/EjercicioOpciones.html\'" ></div>';	
	var domContratosOpciones =  '<div ng-include="\'../../Custodia/UI/DER/ContratosOpciones.html\'" ></div>';
	var domResultadoDiarioFuturos =  '<div ng-include="\'../../Custodia/UI/DER/ResultadoDiarioFuturos.html\'" ></div>';
	var domValorizacionFuturos = '<div ng-include="\'../../Custodia/UI/DER/ValorizacionFuturos.html\'" ></div>';	

	$scope.select = function(grid)
	{
		if (!tabCarteraOpcionesLoaded && grid === 'gridCarteraOpciones') {
			domVentana.find('#'+grid).append($compile(domCarteraOpciones)($scope));
			tabCarteraOpcionesLoaded = true;
		}
				
		else if (!tabCarteraFuturosLoaded && grid === 'gridCarteraFuturos') {
			domVentana.find('#'+grid).append($compile(domCarteraFuturos)($scope));
			tabCarteraFuturosLoaded  = true;
		}	
			
		else if (!tabEjercicioOpcionesLaoded && grid === 'gridEjercicioOpciones') {
			domVentana.find('#'+grid).append($compile(domEjercicioOpciones)($scope));
			tabEjercicioOpcionesLaoded  = true;
		}
		else if (!tabResultadoDiarioFuturosLaoded && grid === 'gridResultadoDiarioFuturos') {
			domVentana.find('#'+grid).append($compile(domResultadoDiarioFuturos)($scope));
			tabResultadoDiarioFuturosLaoded  = true;
		}
		else if (!tabContratosOpcionesLaoded && grid === 'gridContratosOpciones') {
			domVentana.find('#'+grid).append($compile(domContratosOpciones)($scope));
			tabContratosOpcionesLaoded  = true;
		}	
		else if (!tabValorizacionFuturosLaoded && grid === 'gridValorizacionFuturos') {
			domVentana.find('#'+grid).append($compile(domValorizacionFuturos)($scope));
			tabValorizacionFuturosLaoded  = true;
		}	
		
		
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	
	
	$scope.actualizarCartera = function(){
		parametrosSP = {};
		
		parametrosSP.p_IdEmpresa = 1; // falta sacar de variables de sistema
		parametrosSP.p_IdUsuario = 21;
		parametrosSP.p_IdModulo = 'CUS';
		parametrosSP.p_tipoFecha = 'ADQ';
		parametrosSP.p_FechaConsulta = $scope.SesionFechaSistema;
		
		parametrosSP.p_IdClasificacionTipoInstrumento = "";
		parametrosSP.p_IdTipoInstrumento = "";
		parametrosSP.p_IdInstrumento = 0;
		parametrosSP.p_FolioCartera = 0;
		parametrosSP.p_IdMoneda = "";
		parametrosSP.p_IdEstado = "";
		
		parametrosSP.p_IdProductoAgrupador = 0;
		parametrosSP.p_IdCliente = 1423323;
		parametrosSP.p_IdPortafolio = $scope.IdPortafolioVentana;
		parametrosSP.p_TipoDerivado = 'OP';	
		bcsServiciosCustodia.ActualizarCarteraDV(parametrosSP, success, error);
		//post.$promise.then({}).catch({});
	}

	var error = function(respuesta)	{
		console.log(respuesta);
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_ERROR_GENERAR"),
			conflictos:util.parseServerResponse(respuesta)
		},$scope.winID);
		bcsComunes.ventanaOverlay($scope.winID, false);
		//$scope.data.habilitaDig = true;
	};

	var success = function(respuesta){

		console.log(respuesta);
	    	if(respuesta.P_OUT_ERRORESCONTADOR === 0){
	    		
	    		//mantenedorILS.Generar(respuesta.P_OUT_SALDOSCUENTASCONTABLES);
	    	}
	    	if(respuesta.P_OUT_ERRORESCONTADOR != 0)
	    	{
	    		$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value)
	    		{
	    			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
	    		});
	    	}
			//$scope.data.habilitaDig = true;
	    };
	
});