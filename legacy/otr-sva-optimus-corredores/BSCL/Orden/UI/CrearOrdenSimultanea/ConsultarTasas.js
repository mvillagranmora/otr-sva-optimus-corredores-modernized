angular.module('ModuloOrdenes').controller('ConsultarTasasCtrl',function($scope,bcsServicioLimites,bcsComunes)
{
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.cerrar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.tipo = parametros.Tasas.tipo;
	//descomentar cuanto esten las tasas de mercado
	//if(tipo === "CORREDORA")
//	{
		
		var filtros = {};
		filtros.IdTipoLimite = 'LIMRVCPMARGEN';//'LIMRVCPMARGEN';
		filtros.IdTipoOperacion = parametros.Tasas.IdTipoOperacion;//'VENSIM','COMSIM'
		filtros.IdTipoInstrumento = '';//'SIM'
		
		
		
		bcsServicioLimites.ObtenerLimitesRangosMontoPlazo(filtros).then(function(result)
		{
			$scope.montos = result.columnas;//columnas;
			$scope.plazos = result.data;
			
			$scope.$apply();			
		});
	//}
	//if(tipo ==="MERCADO")
	//{
	//}
	
	//$scope.montos = columnas;//[10,15,25,100,500,1000];
	/*$scope.plazos = [
		 {
			 plazo : 7 , tasas : [0.05,0.04,0.06,0.02,0.02,0.02]
		 },
		 {
			 plazo : 30 , tasas : [0.05,0.04,0.06,0.02,0.02,0.02]
		 },
		 {
			 plazo : 60 , tasas : [0.05,0.04,0.06,0.02,0.02,0.02]
		 },
		 {
			 plazo : 90 , tasas : [0.05,0.04,0.06,0.02,0.02,0.02]
		 },
		 {
			 plazo : 365 , tasas : [0.05,0.04,0.06,0.02,0.02,0.02]
		 }
     ];*/
		
});