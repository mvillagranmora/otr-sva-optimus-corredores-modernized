/*************************************************************
 * Purpose	: Plantilla Cobros. Utiliza directiva.
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('PlantillaCobrosCtrl',
	function($scope,$translate,bcsComunes,$state,$filter,$resource,$uibModal){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pCob = this;
		pCob.idVentana = bcsComunes.ventanaWinId($scope);
		pCob.cmbbx 	= {};
		pCob.fn		= {}; 
		pCob.datos	= {};
		pCob.datos.modoLectura = true;

/*********************************************
 * COMBOBOX
 *********************************************/
		pCob.cmbbx.plantillaCobros = {
			configuracion 	: {},
			filtros 		: {
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			}
		};
			
/*********************************************
 * FUNCIONES
 *********************************************/	
		pCob.fn.buscar = function(id){
			pCob.datos.IdPlantillaDirectiva = id;
		};
		
		$scope.$watch('pCob.datos.modoLectura',function(newVal){
			if(!newVal) { pCob.datos.IdPerfilCobros = '';
				//pCob.datos.IdPlantillaDirectiva = '0';
			}
		});
		
		$scope.$watch('pCob.datos.guardarOk',function(newVal){
			if(newVal) pCob.cmbbx.plantillaCobros.filtros.read = true;
		});

		
	});
}());
//------------------------- FIN CONTROLLER --------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioPerfilCobros',['$resource',function($resource) {
		var urlSavePerfilCobros 					= "/BSCL/Plataforma/GWJAVA/PLA/call?t=44";
		var urlUpdateCabeceraPerfilCobros 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=87";
		var urlUpdateModificarDetallePerfilCobros 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=272";
		var urlUpdateCrearDetallePerfilCobros 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=273";
		
		var urlSaveValorCobro 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=286";
		var urlUpdateValorCobro 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=287";
		var urlModificarOrdenesAsignacionesYMovimientosPorDetalleCobro 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=700398";
		
		return $resource("", {},
		{
	  	  'savePerfilCobros'					: { method: "POST",	url: urlSavePerfilCobros},
	  	  'updatePerfilCobros'					: { method: "POST",	url: urlUpdateCabeceraPerfilCobros},
	  	  'updateDetallePerfilCobros'			: { method: "POST",	url: urlUpdateModificarDetallePerfilCobros},
		  'modificarOrdenesAsignacionesYMovimientosPorDetalleCobro': { method: "POST",	url: urlModificarOrdenesAsignacionesYMovimientosPorDetalleCobro},
	  	  'saveDetallePerfilCobros'				: { method: "POST",	url: urlUpdateCrearDetallePerfilCobros},
	  	  'saveValorCobro'						: { method: "POST",	url: urlSaveValorCobro},
	  	  'updateValorCobro'					: { method: "POST",	url: urlUpdateValorCobro}
	     });
	}]);
}());