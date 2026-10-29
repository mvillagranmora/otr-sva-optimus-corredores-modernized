/*************************************************************
 * Purpose	: Panel Proveedor Atributos
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
  angular.module('ModuloSocioNegocio').controller('PanelProveedorAtributosCtrl',
  function($scope,$translate,servicioSociodeNegocio,$stateParams){
    var me = this;
    me.personaDatosGenerales = $scope.admSoc.dataSocioNegocio;
  });
}());
//------------------------- FIN CONTROLLER --------------------------