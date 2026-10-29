angular.module('ModuloOrdenes').controller('ComplementoCargaMasivaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioOrden)
{
	$scope.data = {};
	$scope.IdVentana    = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	

	
	$scope.oConfiguracionCargaMasiva = {};
	$scope.data.UsasPlantillaCobros  = true;
	
	$scope.data.Cantada 			 = true;
	$scope.data.PorCantar 			 = false;
	$scope.data.PorCantarDisabled    = false;
	$scope.data.CantadaDisabled      = false;

	
	$scope.data.HoraSistema 		 = true;
	$scope.data.HoraFija 			 = false;
	$scope.data.HoraFijaDisabled     = false;
	$scope.data.HoraSistemaDisabled  = false;


	
	//-----------------------------------------------------
	// CONFIGURACION DE COMBOBOX
	//-----------------------------------------------------
	
	$scope.data.oConfiguracionCanalDistribucion={
			tipo 			: "combobox-canal-distribucion",
			configuracion 	: {},
			filtros : {				
				IdTipoInstrumento 	: { field : "IdTipoInstrumento", 	operator : "eq", value : ""},
				IdBolsa 			: { field : "IdBolsa", 				operator : "eq", value : "BCS"},
				IdTipoCanalIngreso 	: { field : "IdTipoCanalIngreso", 	operator : "eq", value : $scope.SesionTipoCanalIngreso},
				IdEmpresa 			: { field : "IdEmpresa", 			operator : "eq", value : $scope.SesionIdEmpresa},				
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro  	: false
	};

	


	
	$scope.$watch('data.oConfiguracionCanalDistribucion.seleccionado', function(newval, oldval) {
		if (newval) {
			// Además en el caso del Ruteo, debe quedar la opcion Cantada
			if ($scope.data.oConfiguracionCanalDistribucion.seleccionado.IdTipoCanalDistribucion != "MAN")
			{
				$scope.data.Cantada = true;
				$scope.data.PorCantarDisabled = true;
				$scope.data.CantadaDisabled = true;				
			}
			else
			{
				$scope.data.PorCantada = true;
				$scope.data.PorCantarDisabled = false;
				$scope.data.CantadaDisabled = false;	
			}
		}
	});
	
	
	$scope.$watch('data.Cantada',function(newval,oldval)
	{
		switch (newval)
		{
			case true:  $scope.data.PorCantar = false; break; 				
			case false: $scope.data.PorCantar = true;  break; 
		}
	});
	
	$scope.$watch('data.PorCantar',function(newval,oldval)
	{
		switch (newval)
		{
			case true:  $scope.data.Cantada = false; break;					
			case false: $scope.data.Cantada = true;  break;  
		}
	});
	
	
	$scope.$watch('data.HoraSistema',function(newval,oldval)
	{
		switch (newval)
		{
			case true:  $scope.data.HoraFija = false; break; 				
			case false: $scope.data.HoraFija = true;  break; 
		}
	});
	
	$scope.$watch('data.HoraFija',function(newval,oldval)
	{
		switch (newval)
		{
			case true:  $scope.data.HoraSistema = false; break;					
			case false: $scope.data.HoraSistema = true;  break;  
		}
	});
	


	
	
	//------------------------------------------------------------------------------------
	// PROCESA LA CARGA MASIVA (genera movimientos tesorería y mueve cta. inversion)
	//------------------------------------------------------------------------------------
	
	$scope.procesarCargaMasiva = function() {

		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARA_LA_CARGA_MASIVA') );
		
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				
				bcsComunes.ventanaOverlay($scope.IdVentana, true);
				
				var cargaMasivaProcesar = [];
				

				
				
				cargaMasivaProcesar.push({
										 IdCargaMasiva          : $scope.parametros.IdCargaMasiva,
										 CodigoTipoCargaMasiva  : $scope.parametros.CodigoTipoCargaMasiva,	
										 CanalDistribucion		: $scope.data.oConfiguracionCanalDistribucion.seleccionado.IdTipoCanalDistribucion || null,
										 Negociacion			: ($scope.data.Cantada)? 'OFE' :'ING',										 
										
										// Solo si es hora fija (09:00) se envia FechaOperacion con la hora fija
										FechaOperacion          : ($scope.data.HoraFija) ? $scope.SesionFechaSistema + ' 09:00:00' : '0001-01-01',
										
											 
										 UsarPlantillaCobros	: ($scope.data.UsasPlantillaCobros)? 'S' :'N'
									  });
				

				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'ORD';				
				datos.p_SimularOperacion            = "N";				
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaProcesar			= cargaMasivaProcesar;
				datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			
				console.log("DATOS >> " + JSON.stringify(datos));
				     

				bcsServicioOrden.ProcesarCargaMasiva(datos,function(respuesta){
						bcsComunes.ventanaOverlay($scope.IdVentana, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							
							if (respuesta.P_OUT_ADVERTENCIASCONTADOR === 0)
							{
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);								
							}
							else
							{   
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.IdVentana);
							}
							bcsComunes.ventanaData($scope.IdVentana,1);
							bcsComunes.ventanaCerrar($scope.IdVentana);
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
		});

		return;					
	};


	$scope.limpiarComplemento = function()
	{	
		$scope.data.oConfiguracionCanalDistribucion.idActual   = 0;
		$scope.data.UsasPlantillaCobros = false;
	};
	

	
	
	
});
