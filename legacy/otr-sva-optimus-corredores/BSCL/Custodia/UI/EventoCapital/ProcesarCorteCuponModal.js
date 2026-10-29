/***************************************************************************
* Purpose	: Controlador Procesar Corte Cupon.
* Author	: CGuzman - BCS
* Date	:05/06/2017 .
**************************************************************************/

angular.module('ModuloCustodia').controller('ProcesarCorteCuponModalCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,  
				  bcsServiciosEventoCapital) {

$scope.data = {};
$scope.formData = {};
var overlay = bcsComunes.overlay;
var idVentana = bcsComunes.ventanaWinId($scope);
var parametrosVentana = bcsComunes.ventanaParams(idVentana);
var objEventoCapital = $.extend({},parametrosVentana.EventoCapital);



	
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

$scope.data.kngFechaCorteCuponDesde = util.dateInicioDia($scope.SesionFechaSistema);
$scope.data.kngFechaCorteCuponHasta = util.dateInicioDia($scope.SesionFechaSistema);

$scope.data.btnCerrar = function(refrescar) {
	bcsComunes.ventanaData(idVentana,refrescar);
	bcsComunes.ventanaCerrar(idVentana);
};
	

/*************************************************************************
* PROCESAR CORTE CUPON POR FECHA
**************************************************************************/

$scope.data.ProcesarCorteCupon= function()
    {
		var datos = {};	
		bcsComunes.ventanaOverlay(idVentana,true);
		
		datos.p_IdEmpresa 			= $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario 			= $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo 			= 'CUS';
		datos.p_UsaReglasNegocio	= 'S';		
		datos.p_FechaCorteCuponDesde = util.dateSPdate($scope.data.kngFechaCorteCuponDesde) || '1900-01-01';
		datos.p_FechaCorteCuponHasta = util.dateSPdate($scope.data.kngFechaCorteCuponHasta) || '1900-01-01';
		datos.p_Confirmado = 'N';
		
		bcsServiciosEventoCapital.ProcesarEventoCapitalCorteCupon
			(
			datos,
			function(respuesta){
				  		
				$scope.resumen = respuesta.P_OUT_RESUMEN_CALCULO || [];

            var requiereConfirmacion = $scope.resumen.some(d => d.RequiereConfirmacion);
				if(!requiereConfirmacion){
					$scope.data.realizarCorteCupon();   
					return;
				}	
				$scope.data.mostrarResumen();
				return;
			}
			);

			
		
   	};
	   
   	
/*************************************************************************
* RESUMEN  CORTE CUPON
**************************************************************************/	
$scope.data.mostrarResumen = function(){

    var resumenMensaje = '';

    angular.forEach($scope.resumen,function(dato){

        resumenMensaje +=
        "Moneda: " + dato.IdMonedaOrigen + "; " +
        "Tipo Cambio: " + util.bcs_formato(dato.TipoCambio,'GEN','Monto',dato.IdMonedaOrigen,'','').valor+ "; " +
        "Fecha Valor " + dato.IdMonedaOrigen + ": " + dato.FechaValor + " ";
    });

    var t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_RESUMEN_CORTE_CUPON") + resumenMensaje);
	t.result.then(function(result){
		if (result===OPCIONES_BCSALERTA.SI) {
			$scope.data.realizarCorteCupon();
		}else{
			bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			$scope.data.btnCerrar(true);
		}
	})
	    
   
}

$scope.data.realizarCorteCupon = function(){

	var datos = {};	
		bcsComunes.ventanaOverlay(idVentana,true);
		
		datos.p_IdEmpresa 			= $scope.SesionIdEmpresa; //saca de variables de entorno
		datos.p_IdUsuario 			= $scope.SesionIdUsuario; //saca de variables de entorno
		datos.p_IdModulo 			= 'CUS';
		datos.p_UsaReglasNegocio	= 'S';		
		datos.p_FechaCorteCuponDesde = util.dateSPdate($scope.data.kngFechaCorteCuponDesde) || '1900-01-01';
		datos.p_FechaCorteCuponHasta = util.dateSPdate($scope.data.kngFechaCorteCuponHasta) || '1900-01-01';
		datos.p_Confirmado           = 'S';

		bcsServiciosEventoCapital.ProcesarEventoCapitalCorteCupon
			(
			datos,
			function(respuesta){	  		
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				$scope.data.btnCerrar(true);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
				$scope.data.btnCerrar(true);
			}
			);
		
   	};




});
