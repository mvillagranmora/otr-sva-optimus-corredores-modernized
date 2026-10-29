(function() {
	angular.module('ModuloContabilidad').controller('ProcesarFechaCtrl',
	function($scope,$translate,$resource,$timeout,bcsConsultasMesa,bcsServiciosMesa,bcsComunes,bcsServicioPlataforma)
	{		
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);

		$scope.data = {};
		
		$scope.data.LimiteFechaProceso = {};
		$scope.data.LimiteFechaProceso.max = new Date(util.dateInicioDia(parametros.p_FechaContable));
		$scope.data.kngFechaProceso = util.dateDS(parametros.p_FechaContable,"FechaSistema","CALL");
		
		$scope.procesarCentralizacionFecha = function(){
			p_fechaConsulta = util.dateSPdate($scope.data.kngFechaProceso);
			bcsComunes.ventanaData(idVentana,p_fechaConsulta);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
	});
}());
