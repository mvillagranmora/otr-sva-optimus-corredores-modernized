/*************************************************************
 * Purpose	: Panel Portafolio linea sobregiro.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
  angular.module('ModuloPortafolio').controller('PanelPortafolioLineaSobregiroCtrl',
  function($scope,$translate,$stateParams){
    var ppLS = this;
    ppLS.datos = $scope.admPort.datos;
  });
}());
//------------------------- FIN CONTROLLER --------------------------