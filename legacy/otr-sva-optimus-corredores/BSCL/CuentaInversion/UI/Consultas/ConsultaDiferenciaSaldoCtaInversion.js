(function() {
	angular.module('ModuloPlataforma').controller('ConsultaDiferenciaSaldoCtaInversionCtrl',
	function($scope,$translate,bcsComunes,bcsServicioPlataforma,$http,$timeout){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		$scope.dataTemplate= {};
		
		var idVentana = bcsComunes.ventanaWinId ($scope);
		$scope.data.btnGenerar = true;
		$scope.data.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");		
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/	
		// --------------------------------------------------------------------------------
		//Genera en forma Batch un archivo csv con las diferencias saldo cuenta inversion
		// --------------------------------------------------------------------------------		
		$scope.data.generarArchivo = function(){		
			
			var entradaPrograma 		= {};					
			entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
			entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
			entradaPrograma.p_IdModulo    		= 'CTA';
			entradaPrograma.p_IdAgenda    		= 0;    
			entradaPrograma.p_IdTipoTarea   	= 'VALIDA_SALDO_CUENTA_INVERSION';
			entradaPrograma.p_Segundos      	= 0;			
			entradaPrograma.p_CreaParam     	= 'S';
			
			entradaPrograma.p_TablaParametros =
				[
				   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
				   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
				   {NombreParametro	: 'IdModulo', 	  		Valor: 'CTA'},	
				   {NombreParametro	: 'FechaDesde', 	  	Valor: util.dateSPdate($scope.data.Fecha)},
				   {NombreParametro	: 'FechaHasta', 	  	Valor: '0001-01-01'}				   
				];
			
			console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
			bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
					function(respuesta){
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
							Programa = respuesta.P_OUT_IDPROGRAMA;					
							if (Programa.length > 0)
							    idPrograma = Programa[0].IdPrograma;								
									
							bcsComunes.ventanaMsg({
								mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"							
							},$scope.winID);						
						}
						else
						{						
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
							},$scope.winID);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);					
					}
				);
	    }
	});
	var firstHeightCrear = $('#Win_DiferenciaSaldoCuentaInversion');
        firstHeightCrear.css('width', '335px');
}());