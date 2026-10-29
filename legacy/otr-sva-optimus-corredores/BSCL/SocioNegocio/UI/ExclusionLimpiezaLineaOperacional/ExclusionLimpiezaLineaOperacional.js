angular.module('ModuloSocioNegocio').controller('ExclusionLimpiezaLineaOperacionalCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,$translate,bcsAlerta,OPCIONES_BCSALERTA,
					servicioSociodeNegocio,bcsExclusionLimpiezaLineaOperacionalGrid,bcsServicioPlataforma)
{
	
	$scope.data = {};
	
	$scope.IdVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	
	$scope.data.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
	};
	
	//------------------------------------------------
	// CONFIGURACION DE GRILLA
	//------------------------------------------------	
	var dsExclusionLimpiezaLineaOperacional = bcsExclusionLimpiezaLineaOperacionalGrid.dsExclusionLimpiezaLineaOperacional;
	$scope.confGridExclusionLimpiezaLineaOperacional = {
			dataSource			: dsExclusionLimpiezaLineaOperacional,		
			columns				: bcsExclusionLimpiezaLineaOperacionalGrid.columnasExclusionLimpiezaLineaOperacional(),		
			sortable			: true,
			pageable			: true,
			height				: 320,
			selectable			: "multiple row",
			userSettings        : true,
			autoBind: true,
  			scrollable: true,
	};
	
	$scope.buscarExclusionLimpiezaLineaOperacional = function(){
		$scope.apiGridExclusionLimpiezaLineaOperacional.dataSource.read();
	};	
	
	$scope.ingresarExclusionLimpiezaLineaOperacional = function(){
		if (BCS_ESTADO_MODULO['SOC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.IdVentana, true);
		
		let portafolio = {
			IdPortafolio : $scope.data.oClientePortafolio.seleccionado.IdPortafolio
		};
		
		let data = {};
		data.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
		data.p_IdUsuario               	= $scope.SesionIdUsuario; 
		data.p_IdModulo                	= 'SOC';
		data.p_UsaReglasNegocio			= 'S';
		data.p_Portafolios    		= [ portafolio ];
			
		bcsComunes.log().info(JSON.stringify(data));
						
		servicioSociodeNegocio.CrearExclusionLimpiezaLineaOperacional(data,function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.IdVentana);
				$scope.buscarExclusionLimpiezaLineaOperacional();									
			} else {	
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.IdVentana);
			}
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
		}, function(respuesta){
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},$scope.IdVentana);
			bcsComunes.ventanaOverlay($scope.IdVentana, false);
		});
		
	};
	
	$scope.eliminarExclusionLimpieza = function(seleccion) {
		if (BCS_ESTADO_MODULO['SOC'] === "CER") {
			bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
			return;
		}
		if (seleccion.length === 0){
			bcsComunes.ventanaMsg({mensaje: $translate.instant('UI_SELECCIONAR_REGISTROS_VALIDOS')}, $scope.IdVentana);		
			return;
		}
		
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARAN_LAS_EXCLUSIONES_DE_LOS_PORTAFOLIOS') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.IdVentana, true);
				
				let portafolios = [];
				
				$.each(seleccion,function(key,item){
  					portafolios.push({ IdPortafolio : item.IdPortafolio });
  				});
				
				let data = {};
				data.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				data.p_IdUsuario               	= $scope.SesionIdUsuario; 
				data.p_IdModulo                	= 'SOC';
				data.p_UsaReglasNegocio			= 'S';
				data.p_Portafolios    		= portafolios;
				
				bcsComunes.log().info(JSON.stringify(data));
				  
				servicioSociodeNegocio.EliminarExclusionLimpiezaLineaOperacional(data, function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaMsg({				
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);
						$scope.buscarExclusionLimpiezaLineaOperacional();
					} else {	
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.IdVentana);
					}
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
					$scope.buscarExclusionLimpiezaLineaOperacional();
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.IdVentana);
					bcsComunes.ventanaOverlay($scope.IdVentana, false);
				});
			}
		});
		return;					
	};	

	
});
