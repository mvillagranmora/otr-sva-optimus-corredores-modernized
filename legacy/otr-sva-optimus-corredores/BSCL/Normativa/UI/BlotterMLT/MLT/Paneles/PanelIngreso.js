(function() {
	angular.module('ModuloNormativa').controller('PanelIngresoCtrl',function($rootScope,$scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa
			,bcsAlerta, OPCIONES_BCSALERTA,$http){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var pc2 = this;
		pc2.idVentana 	= bcsComunes.ventanaWinId($scope); // ID del Formulario.
		pc2.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		pc2.cmbbx		= {};
		pc2.datasource  = {};
		pc2.apiGridDestinoFondosMutuos= {};
		pc2.CantidadTotalTraspaso=0;
		pc2.MontoTotalTraspaso=0;
		
		pc2.IdMonedaSistema=$rootScope.SesionIdMonedaSistema;
		
		pc2.MontoTotalDestino=0;
		pc2.PorcentajeTotalDestino=0;
		pc2.formData= {};
		pc2.modoPantalla="";
		pc2.PorcentajeMoneyMarket=100;
		pc2.ConfiguracionFM = {};
		/*
		pc2.ConsultarTraspaso 					= bcsConfiguracionFFM.getDataSourceConsultarTraspaso();
		pc2.ConsultarTraspasoDetalleOrigen 		= bcsConfiguracionFFM.getDataSourceConsultarTraspasoDetalleOrigen();
		pc2.ConsultarTraspasoDetalleDestino		= bcsConfiguracionFFM.getDataSourceConsultarTraspasoDetalleDestino();
		*/
		pc2.formData = {};
	    	    
	    if ($scope.solTra.params.Regimen){pc2.Modo=$scope.solTra.params.Regimen;};
	 

	    pc2.ingresar = function(){
			bcsComunes.ventanaOverlay(pc2.idVentana,true);
		
			
			var parametrosSP = {};
	        parametrosSP.p_IdSolicitudAporteRescate = $scope.solTra.params.IdSolicitudAporteRescate;
	        parametrosSP.p_FolioMandato = pc2.FolioMandato;
	        parametrosSP.p_valor = pc2.valor;
	        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	        
	        
	    	var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_CONFIRMA" ) + " "  + $translate.instant("UI_INGRESO" ) +" ?",pc2.idVentana);
			t.then(function(result)
			      {		
					 if (result===OPCIONES_BCSALERTA.SI)
				       {	
		
							    	bcsServiciosNormativa.IngresarMLT(parametrosSP).$promise.then(function(respuesta){
							   	     	if (respuesta.P_OUT_ERRORESCONTADOR === 0){
								     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_INGRESO_REALIZADO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
							   	     	}else{
								     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_DE_INGRESO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
								     	 }
							   	     	bcsComunes.ventanaOverlay(pc2.idVentana,false);
								    	bcsComunes.ventanaCerrar(pc2.idVentana);
								    },function(respuesta){
									  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_DE_INGRESO"),conflictos:util.parseServerResponse(respuesta)},pc2.idVentana);
									  bcsComunes.ventanaOverlay(pc2.idVentana,false);
								    });
						
						
				       };
				});	
	        
		}
	  	
		
	
	
		

	});
}());