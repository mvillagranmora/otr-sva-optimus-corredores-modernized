/*************************************************************
 * Purpose	: Panel Portafolio Requisitos
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	  angular.module('ModuloPortafolio').controller('PanelPortafolioRequisitosCtrl',
	  function($scope,$translate,$stateParams){
	    var pPortReq = this;
	    pPortReq.datos = $scope.admPort.datos;
	  });
}());
//------------------------- FIN CONTROLLER --------------------------