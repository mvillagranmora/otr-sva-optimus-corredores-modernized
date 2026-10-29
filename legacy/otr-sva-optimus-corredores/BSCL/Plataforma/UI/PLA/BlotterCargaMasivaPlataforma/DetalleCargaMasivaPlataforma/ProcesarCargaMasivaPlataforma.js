angular.module('ModuloPlataforma').controller('ProcesarCargaMasivaPlataformaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate, bcsBlotterCargaMasivaPlataformaGrid,bcsServiciosCargaMasivaPlataforma)
{	
	$scope.oCargaMasiva = {};
	
	$scope.IdVentana  = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros = bcsComunes.ventanaParams($scope.IdVentana);
	$scope.data = {};
	$scope.data.switchCartera = false;
	$scope.oCargaMasiva = $scope.parametros.CargaMasiva;
	
	$scope.data.ProcesarCargaMasiva = function() {

		bcsComunes.ventanaOverlay($scope.IdVentana, true);

		var datos = {};
		datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
		datos.p_IdModulo                	= 'PLA';
		datos.p_UsaReglasNegocio			= 'S';
		datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		datos.p_CargaMasivaProcesar			= $scope.oCargaMasiva.IdCargaMasiva
		datos.p_UsaCarterasSistema			= $scope.data.switchCartera ? 'S' : 'N';


		if (datos.p_CargaMasivaProcesar == 5) {
				bcsServiciosCargaMasivaPlataforma.ProcesarCargaMasivaPlataforma(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);

						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaMsg({				
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						} else {
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
					}
				);			

		}else
		{
				bcsServiciosCargaMasivaPlataforma.ProcesarCargaMasivaPltaformaReporte(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);

						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							bcsComunes.ventanaMsg({				
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						} else {
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.IdVentana);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
					}
				);			
		}


	};

});

