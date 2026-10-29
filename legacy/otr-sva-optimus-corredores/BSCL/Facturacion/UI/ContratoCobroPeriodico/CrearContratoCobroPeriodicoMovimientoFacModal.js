angular.module('ModuloFacturacion').controller('CrearContratoCobroMovimientoFacModal',
		function($scope,$translate,$modal,bcsComunes,bcsServiciosFacturacion,
				 bcsServicioPlataforma, $timeout)
{
	$scope.data	= {};
	var idVentana = bcsComunes.ventanaWinId ($scope);
	
    var datosEntrada = bcsComunes.ventanaParams(idVentana);
	 
	
	//Cancela Operación	
    $scope.data.cancelar = function() 
     {	     
		bcsComunes.ventanaCerrar(idVentana);	
	 };	
	 
   
    //Opción no habilitada
    $scope.data.opcionNoHabilitada = function() 
     {
		bcsComunes.msg("warning","warning","Está opción no se encuentra habilitada.");
	 };
		
});