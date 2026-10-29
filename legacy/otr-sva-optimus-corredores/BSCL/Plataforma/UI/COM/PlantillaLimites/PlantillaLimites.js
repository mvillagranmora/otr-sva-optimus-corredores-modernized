(function() {
	angular.module('ModuloPlataforma').controller('PlantillaLimitesCtrl',
	function($scope,$translate,bcsComunes,$resource){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		$scope.idVentana = bcsComunes.ventanaWinId($scope);
		var pLim = this;
		pLim.cmbbx 	= {};
		pLim.fn		= {}; 
		pLim.datos	= {};
		pLim.datos.modoLectura = true;

/*********************************************
 * COMBOBOX
 *********************************************/
		pLim.cmbbx.plantillaLimite = {
			filtros : {
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa: {field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa}
			}
		};
			
/*********************************************
 * FUNCIONES
 *********************************************/	
		pLim.fn.buscar = function(id){
			pLim.datos.IdPlantillaDirectiva = id;
		};
		
		$scope.$watch('pLim.datos.modoLectura',function(newVal){
			if(!newVal) { pLim.datos.IdPlantillaLimite = '0';
			}
		});
		
		$scope.$watch('pLim.datos.guardarOk',function(newVal){
			if(newVal) pLim.cmbbx.plantillaLimite.filtros.read = true;
		});
	});
}());


//-------------------------------------------------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioPlantillaLimites',['$resource',function($resource) {
		//var urlSavePlantillaLimites			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=292";
		var urlSavePlantillaLimites			= "/BSCL/Plataforma/GWJAVA/PLA/call?t=292";
		var urlUpdatePlantillaLimites		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=294";
		var urlSaveDetallePlantillaLimites	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=293";
		var urlUpdateDetallePlantillaLimites= "/BSCL/Plataforma/GWJAVA/PLA/call?t=295";
		
		var urlCrearLimite		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=39";
		var urlModificarLimite	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=306";
		
		return $resource("", {},{
			'savePlantillaLimites'			: { method: "POST",	url: urlSavePlantillaLimites},
			'updatePlantillaLimites'		: { method: "POST",	url: urlUpdatePlantillaLimites},
			'saveDetallePlantillaLimites'	: { method: "POST",	url: urlSaveDetallePlantillaLimites},
			'updateDetallePlantillaLimites'	: { method: "POST",	url: urlUpdateDetallePlantillaLimites},
			'crearLimite'					: { method: "POST",	url: urlCrearLimite},
			'modificarLimite'				: { method: "POST",	url: urlModificarLimite}
	     });
	}]);
}());