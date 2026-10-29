/*************************************************************
 * Purpose	: Plantilla de productos, utiliza directiva.
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('PlantillaProductosCtrl',
	function($scope,$translate,bcsComunes,$state,$filter,$resource,$uibModal){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pProd = this;
		pProd.cmbbx 	= {};
		pProd.fn		= {}; 
		pProd.datos	= {};
		pProd.datos.modoLectura = true;

/*********************************************
 * COMBOBOX
 *********************************************/
		pProd.cmbbx.plantillaProductos = {
			configuracion 	: {},
			filtros 		: {
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			}
		};
			
/*********************************************
 * FUNCIONES
 *********************************************/	
		pProd.fn.buscar = function(id){
			pProd.datos.IdPlantillaDirectiva = id;
		};
		
		$scope.$watch('pProd.datos.modoLectura',function(newVal){
			if(!newVal) { pProd.datos.IdPerfilProductos = '';
				//pProd.datos.IdPlantillaDirectiva = '0';
			}
		});
		
		$scope.$watch('pProd.datos.guardarOk',function(newVal){
			if(newVal) pProd.cmbbx.plantillaProductos.filtros.read = true;
		});

		
	});
}());
//------------------------- FIN CONTROLLER --------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioPerfilProductos',['$resource',function($resource) {
		var urlSavePerfilProductos 						= "/BSCL/Plataforma/GWJAVA/PLA/call?t=281";
		var urlUpdateCabeceraPerfilProductos 			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=282";
		var urlUpdateCrearDetallePerfilProductos 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=283";
		var urlUpdateModificarDetallePerfilProductos 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=284";
		return $resource("", {},
		{
	  	  'savePerfilProductos'						: { method: "POST",	url: urlSavePerfilProductos},
	  	  'updatePerfilProductos'					: { method: "POST",	url: urlUpdateCabeceraPerfilProductos},
	  	  'updateDetallePerfilProductos'			: { method: "POST",	url: urlUpdateModificarDetallePerfilProductos},
	  	  'saveDetallePerfilProductos'				: { method: "POST",	url: urlUpdateCrearDetallePerfilProductos}
	     });
	}]);
}());