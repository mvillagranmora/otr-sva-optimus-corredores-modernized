/***************************************************************************
* Purpose	: Controlador cerrar Corte Cupon.
* Author	: CGuzman - BCS
* Date	:13/10/2016 .
**************************************************************************/

angular.module('ModuloCustodia').controller('CerrarCorteCuponModalCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				  bcsServiciosEventoCapital) {

$scope.data = {};
$scope.formData = {};
var overlay = bcsComunes.overlay;
var idVentana = bcsComunes.ventanaWinId($scope);
var parametrosVentana = bcsComunes.ventanaParams(idVentana);
var objEventoCapital = $.extend({},parametrosVentana.EventoCapital);



	$scope.data.oEmisor={
		tipo : "combobox-emisor",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   	: "IdEstado","operator": "eq", "value"   : "ACT" },
		}
	};
	
	var admEveCap = this;
  	admEveCap.winId 		= bcsComunes.ventanaWinId($scope);

	$scope.formData.resetFoco =  function() {
			$scope.formData.Emisor = true;
	};
	
/*************************************************************************
* CARGA INICIAL
**************************************************************************/
$scope.data.accionDisabled = true;
$scope.data.Accion = parametrosVentana.Accion;
	
$scope.data.oEmisor.idDefecto= objEventoCapital.IdEmisor;
$scope.data.kngFechaPago = objEventoCapital.FechaPago;

$scope.data.btnCerrar = function(refrescar) {
	bcsComunes.ventanaData(idVentana,refrescar);
	bcsComunes.ventanaCerrar(idVentana);
};
	

/*************************************************************************
* CERRAR CORTE CUPON POR EMISOR
**************************************************************************/

$scope.data.CerrarCorteCupon= function()
    {
		var datos = {};	
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo = 'CUS';
		datos.p_UsaReglasNegocio = 'S';
		
		datos.p_IdTipoEventoCapital		= 'CC';
		datos.p_FechaPago				= util.dateSPdate($scope.data.kngFechaPago)		||'1900-01-01';
		datos.p_IdEmisor				= $scope.data.oEmisor.idDefecto;
		
		bcsServiciosEventoCapital.CerrarEventoCapitalCorteCupon
			(
			datos,
			function(respuesta){	  		
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				$scope.data.btnCerrar(true);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				$scope.data.btnCerrar(true);
			}
			);
		
   	};
   	

 	//Primer evento - filtro inicial
//	$timeout(function(){
//		$scope.data.oEmisor.filtros.read = true;
//		$scope.data.oConfiguracionMoneda.filtros.read = true; 
//	},1000);
	
});
