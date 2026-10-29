(function() {
	angular.module('ModuloPlataforma').controller('PlantillaCobrosPeruCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosPlataformaComercialPeru){

		var pCob = this;
		pCob.idVentana = bcsComunes.ventanaWinId($scope);
		pCob.cmbbx 	= {};
		
		pCob.cmbbx.plantillaCobros = {
			configuracion 	: {},
			filtros 		: {
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			}
		};
		
		pCob.refrescarPerfiles = function() {
			 pCob.cmbbx.plantillaCobros.filtros.read = true;
		}
		
	});
}());
//------------------------- FIN CONTROLLER --------------------------