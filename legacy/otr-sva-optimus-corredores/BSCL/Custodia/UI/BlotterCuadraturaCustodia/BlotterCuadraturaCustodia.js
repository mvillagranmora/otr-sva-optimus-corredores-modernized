
angular.module('ModuloCustodia').controller('BlotterCuadraturaCustodiaCtrl',function($scope,bcsComunes,$timeout,$modal,$translate){
//	var bEC = this;
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},10);
	};
	
//	bEC.cargaFiltroCliente = function (vars){
////		console.log(bEC.seleccionEventoCapital);
////		console.log(vars);
////		console.log("Id Evento de Capital : " + vars.IdTipoEventoCapital);
////		console.log("Id Instrumento : " + vars.IdInstrumento);
////		console.log("Numero de dividendo : " + vars.NumeroDividendo);
////		console.log("Fecha Pago :" + util.datefix(vars.FechaPago));
////		console.log(vars.IdEstado);
//		
//		
//		if (vars.IdTipoEventoCapital == 'SU'){
//			// EN CASO DE TRATARSE DE UNA SUSCRIPCIÓN SE DEBEN ACTIVAR CIERTOS BOTONES.
//			bEC.ActivaBotonCertificar			= true;
//			//bEC.ActivaBotonMovimientoFacturar	= true;
//		}
//		else
//		{
//			// EN CASO DE TRATARSE DE UNA SUSCRIPCIÓN SE DEBEN DESACTIVAR CIERTOS BOTONES.
//			bEC.ActivaBotonCertificar 			= false;
//			//bEC.ActivaBotonMovimientoFacturar	= false;
//		};
//		bEC.idActualEvento.TipoEvento = vars.IdTipoEventoCapital; 
////		bEC.idActualEstado.IdEstado = vars.IdEstado;
//		bEC.idActualInst.IdInstrumento = vars.Nemo;
//		bEC.idActualNumeroDividendo = vars.NumeroDividendo;
//		bEC.idActualNumeroSecuencia = vars.Secuencia;
//		bEC.idActualFechaPago = vars.FechaPago;
//		bEC.idActualFechaPagoHasta = vars.FechaPago;
		
		//bEC.idActualFechaPagoNg = util.datefix(vars.FechaPago);
//		bEC.idActualFechaPago = util.datefix(util.date(vars.FechaPago).toDate())		
		
//	};
});