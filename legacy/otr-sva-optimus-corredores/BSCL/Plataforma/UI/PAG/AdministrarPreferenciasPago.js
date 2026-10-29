/*************************************************************
 * Purpose	: Administrar Preferencias de Pago.
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('AdministrarPreferenciasPagoCtrl',
		function ($scope,bcsComunes,$translate) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var admPrefPag 			= this;
			admPrefPag.idVentana 	= bcsComunes.ventanaWinId($scope);
			admPrefPag.parametros 	= bcsComunes.ventanaParams(admPrefPag.idVentana);
			admPrefPag.domVentana	= $("[ventanaid='"+admPrefPag.idVentana+"']");
			
			admPrefPag.fnTabPrefPagoRecibe = function() {
				admPrefPag.clicPagoRecibe 	= true;
				admPrefPag.clicPagoPaga 	= false;
			};
			
			admPrefPag.fnTabPrefPagoPaga = function() {
				admPrefPag.clicPagoPaga 	= true;
				admPrefPag.clicPagoRecibe 	= false;
			};
		}
	);
}());
//------------------------- FIN CONTROLLER --------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioPreferenciasPago',['$resource',function($resource) {
		var urlSavePreferenciaPago 		= "/BSCL/Plataforma/GWJAVA/PLA/call?t=288";
		var urlUpdatePreferenciaPago 	= "/BSCL/Plataforma/GWJAVA/PLA/call?t=289";
		return $resource("", {},
		{ 
	  	  'savePreferenciaPago'		: { method: "POST",	url: urlSavePreferenciaPago},
	  	  'updatePreferenciaPago'	: { method: "POST",	url: urlUpdatePreferenciaPago},
	     }); 
	}]);
}());
//------------------------- FIN SERVICIO --------------------------