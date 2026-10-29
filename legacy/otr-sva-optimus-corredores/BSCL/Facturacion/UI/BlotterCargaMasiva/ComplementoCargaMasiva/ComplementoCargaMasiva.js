angular.module('ModuloFacturacion').controller('ComplementoCargaMasivaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosFacturacion)
{
	$scope.data = {};
	var idVentana    = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams(idVentana);
	
	bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
	
	
	//-----------------------------------------------------
	// CONFIGURACION DE COMBOBOX
	//-----------------------------------------------------
	
  	$scope.data.oConfiguracionEjecutivo={
			tipo 			: "combobox-empleado",
			configuracion 	: {},
			filtros 		: {
					IdSucursal : {"field": "IdSucursal", "operator": "eq", "value": 0 },
					IdEstado   : {"field": "IdEstado",   "operator": "eq", "value": 'ACT' }
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};
	
  	
	$scope.data.oConfiguracionBolsa={
			tipo 			: "combobox-bolsa",
			configuracion 	: {},
			filtros 		: {
					IdEstado                   : { "field": "IdEstado",                   "operator": "eq", "value": "ACT" },
					EstadoBolsaTipoInstrumento : { "field": "EstadoBolsaTipoInstrumento", "operator": "eq", "value": "ACT" }
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
		};
	
	$scope.data.oConfiguracionBoveda={
			tipo 			: "combobox-boveda",
			configuracion 	: {},
			filtros 		: {
					IdEstado : { "field": "IdEstado", "operator": "eq", "value": "VIG" }
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
		};
	

	//------------------------------------------------------------------------------------
	// PROCESA LA CARGA MASIVA (genera comprobantes tesorería y mueve cta. inversion)
	//------------------------------------------------------------------------------------
	$scope.procesarCargaMasiva = function() {
		
		if ($rootScope.SesionLocale === 'PE' && $scope.parametros.IdTipoCargaMasiva === 2){
	        var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARA_LA_CARGA_MASIVA_COMPROBANTE_AUTOMATICO'), idVentana );
        }else{
        	var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_PROCESARA_LA_CARGA_MASIVA'), idVentana );	
        }
		
		t.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				
				bcsComunes.ventanaOverlay(idVentana, true);
				
				var cargaMasivaProcesar = [];
								
				cargaMasivaProcesar.push({
										 IdCargaMasiva          : $scope.parametros.IdCargaMasiva,
										 IdTipoCargaMasiva      : $scope.parametros.IdTipoCargaMasiva,
										 CodigoArchivo			: $scope.parametros.CodigoArchivoTipoCargaMasiva,
										 IdEmpleadoDigitador	: $scope.data.oConfiguracionEjecutivo.seleccionado.IdEmpleado || 0,
										 IdBolsa  				: $scope.data.oConfiguracionBolsa.seleccionado.IdBolsa || '',
										 IdBoveda 				: $scope.data.oConfiguracionBoveda.seleccionado.IdBoveda || ''			  							  
									  }); 
				
 
				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'FAC';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaProcesar			= cargaMasivaProcesar;
				datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema)); //Fecha de sistema en que se procesa la CM 
			
				console.log(JSON.stringify(datos));
				
				bcsServiciosFacturacion.ProcesarCargaMasiva(datos,function(respuesta){
						bcsComunes.ventanaOverlay(idVentana, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							
							if (respuesta.P_OUT_ADVERTENCIASCONTADOR === 0)
							{
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);								
							}
							else
							{   
								bcsComunes.ventanaMsg({				
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ADVERTENCIAS"),
									conflictos:util.parseServerResponse(respuesta)
								},idVentana);
							}
							bcsComunes.ventanaData(idVentana,1);
							bcsComunes.ventanaCerrar(idVentana);
						} else {
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
								conflictos:util.parseServerResponse(respuesta)
							},idVentana);
						}
					},
					
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
						bcsComunes.ventanaOverlay(idVentana, false);
					}
				);
				
			}
		});
		
		return;					
	};


	$scope.limpiarComplemento = function()
	{	
	  	$scope.data.oConfiguracionEjecutivo.idActual = 0;
	  	$scope.data.oConfiguracionBolsa.idActual     = 0;
	  	$scope.data.oConfiguracionBoveda.idActual    = 0;
	};
	
	
});
