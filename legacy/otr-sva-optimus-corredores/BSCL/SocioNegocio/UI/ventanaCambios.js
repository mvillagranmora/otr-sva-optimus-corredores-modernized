/*************************************************************
 * Purpose	: Ventana Cambios Socio Negocio (Antes/Después)
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ventanaCambiosCtrl',
	function($scope,bcsComunes,$translate,$resource){
		$scope.camposAtr = [];
		var ventanaId = bcsComunes.ventanaWinId($scope);
		parametros = bcsComunes.ventanaParams (ventanaId);
		Data = parametros.dataSolicitudCambio;
		for (var i = 0; i < Data.length;i++)
		 {
			var campo = {};
			campo.CampoModificado =  Data[i].CampoModificado;
			campo.ValorAntiguo 	  =  Data[i].ValorAntiguo;
			campo.ValorNuevo 	  =  Data[i].ValorNuevo;
			$scope.camposAtr.push(campo);		
		 };									    
			
		$scope.cerrar = function () {
			bcsComunes.ventanaCerrar(ventanaId); 
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------	