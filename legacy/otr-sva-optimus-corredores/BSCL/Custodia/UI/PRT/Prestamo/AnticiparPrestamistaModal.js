
(function() {
	angular.module('ModuloCustodia').controller('AnticiparPrestamistaModalCtrl',
    function ($scope,$translate,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
		bcsServicioPlataforma,bcsServicioCalcularMontoTipoInstrumento,bcsServiciosPrestamo,bcsServiciosCustodia) {
        /*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.data = {};
		$scope.data.plazoAnticipo = 0;
		$scope.data.CantidadMaxima = 0;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		$scope.data.oDatosEmprestito = parametros;
		var miFechaSis = $scope.SesionFechaSistema;
        
        $scope.data.cantidadOptions 				= {format: 'n'+(util.bcs_formato(0,'CUS','Cantidad',$scope.SesionIdMonedaSistema,'','')).cantidadDecimales,min:0,spinners: false};
        $scope.data.CantidadMaxima 					= $scope.data.oDatosEmprestito.INGCantidad;
		$scope.data.kngCantidad 					= $scope.data.oDatosEmprestito.INGCantidad;
        $scope.data.kngCantidadPorAnticipar 		= $scope.data.oDatosEmprestito.INGCantidad;










        /*********************************************
        * ACCIONES DE PANTALLA
        ********************************************/
        $scope.data.btnCerrar = function(refrescar) {
        bcsComunes.ventanaData(idVentana,refrescar);
        bcsComunes.ventanaCerrar(idVentana);
        };


        $scope.data.fnAnticiparPrestamista = function(){
            if (Number($scope.data.kngCantidad) > Number($scope.data.kngCantidadPorAnticipar)){
				bcsComunes.msg("error","info",$translate.instant('UI_ANTICIPAR_EMPRESTITO_CANTIDAD_SOBRE_MAXIMO'));
				return false;
			}
			
			if (Number($scope.data.kngCantidad) <= 0){
				bcsComunes.msg("error","info",$translate.instant('UI_ANTICIPAR_EMPRESTITO_CANTIDAD_MENOR_CERO'));
				return false;
            }
            
            bcsComunes.ventanaOverlay(idVentana,true);
            $scope.data.accionDisabled = undefined;
            


            var datos = {};
			datos.p_IdEmpresa 					= $scope.SesionIdEmpresa;
			datos.p_IdUsuario 					= $scope.SesionIdUsuario;
			datos.p_IdModulo 					= 'CUS';
			datos.p_UsaReglasNegocio 			= 'S';
			datos.p_IdPais 						= $scope.SesionIdPaisSistema;
			datos.p_DatosEmprestitos 			= registro;
			datos.p_RebajaPrestamo				= 'S';
			datos.p_FechaAnticipo 				= $scope.data.kngFechaAnticipo || '1900-01-01';


            bcsServiciosPrestamo.AnticiparEmprestito
							(datos,
							function(respuesta){
								bcsComunes.ventanaOverlay(idVentana,false);
								var aaa=bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaPromesa(aaa).then(function(){
									bcsComunes.ventanaData(idVentana, true);
									bcsComunes.ventanaCerrar(idVentana);	
								});
								$scope.data.accionDisabled = true;
							},
							function(respuesta){
								bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaOverlay(idVentana,false);
								var datos = {};
								$scope.data.accionDisabled = true;
							});	
        }
    });
}());