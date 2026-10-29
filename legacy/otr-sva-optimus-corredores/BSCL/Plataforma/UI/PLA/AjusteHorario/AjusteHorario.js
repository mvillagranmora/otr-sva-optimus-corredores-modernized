angular.module('ModuloPlataforma').controller('AjusteHorarioCtrl',function($scope,bcsComunes,$translate,bcsAlerta,bcsServicioPlataforma){

console.log("AjusteHorarioCtrl");
console.log($scope.SesionFechaSistema);

	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.gtm = { zonaHoraria : -1};
	$scope.data = {};
	$scope.minimoOptions = {min:-20000,format:"n0",spinners:false};
	var dataSourceConfiguracion = new kendo.data.DataSource({
	type: "odata",
	serverFiltering:true,
	transport: {
		read: {
			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
			dataType: "json"
			  }		
			},
		filter: [{ field: "CodigoParametro", operator: "eq", value: 'PLA.SegundosZonaHoraria' },
		         { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }],
	});
	
	dataSourceConfiguracion.fetch(function(){
		
		$scope.$apply(function() {
			$scope.Configuracion = dataSourceConfiguracion.data()[0];
			$scope.gtm = { zonaHoraria : parseInt($scope.Configuracion.Valor,10)/3600}
			});
		
		console.log($scope.Configuracion);
		console.log($scope.gtm.zonaHoraria)
 	});
	
	$scope.CambiarZonaHoraria = function(){
		
        var datosAjusteHorario = {};
        	datosAjusteHorario.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
        	datosAjusteHorario.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
        	datosAjusteHorario.p_IdModulo = 'PLA'; //sacar de variables de sistema
	        datosAjusteHorario.p_CambioDeHoraSegundos = $scope.data.kngSegundos;
	        console.log($scope.data.kngSegundos);
	        console.log(datosAjusteHorario.p_CambioDeHoraSegundos);

	        bcsServicioPlataforma.AjustarZonaHoraria(datosAjusteHorario).then(function(result)
		    {
		        	 console.log(JSON.stringify(result));
		        	
		        	 
		        	 if (result.P_OUT_ERRORESCONTADOR == "0")
				        {
							console.log(result.SALIDA);
							dataSourceConfiguracion.fetch(function(){
								
								$scope.$apply(function() {
									$scope.Configuracion = dataSourceConfiguracion.data()[0];
									$scope.gtm = { zonaHoraria : parseInt($scope.Configuracion.Valor,10)/3600}
									});
								
								console.log($scope.Configuracion);
								console.log($scope.gtm.zonaHoraria)
						 	});
							
				        }
				        bcsComunes.ventanaMsg({
							mensaje: $translate.instant(result.SALIDA),
							conflictos:util.parseServerResponse(result)
						},idVentana);
		        	 
		        },
				function(result){
					datos.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
				});
		
		
	};
	
	
	 
	
});
