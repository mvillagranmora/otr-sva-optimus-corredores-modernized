/*************************************************************
 * Purpose	: Atributos del Empleado.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
  angular.module('ModuloSocioNegocio').controller('PanelEmpleadoAtributosCtrl',
  function($scope,$translate,servicioSociodeNegocio,$stateParams){
    var me = this;
    me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;
  });
}());
//------------------------- FIN CONTROLLER --------------------------