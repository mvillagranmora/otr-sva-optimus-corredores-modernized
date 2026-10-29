angular.module('ModuloTesoreria').controller('ComplementoCargaMasivaCtrl',
			function($scope,$translate,$timeout,$rootScope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosTesoreria)
{
	$scope.data = {};
	$scope.IdVentana    = bcsComunes.ventanaWinId ($scope);	
	$scope.parametros 	= bcsComunes.ventanaParams($scope.IdVentana);
	
	bcsComunes.ventanaAutoOverlay($scope.IdVentana,false,3000);
	
	$scope.data.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.oConfiguracionCargaMasiva = {};



	//-----------------------------------------------------
	// CONFIGURACION DE COMBOBOX
	//-----------------------------------------------------
	
	$scope.data.oConfiguracionConcepto={
			tipo 			: "combobox-concepto-tesoreria",
			configuracion 	: {},
			filtros : {
				NombreEntidad		   		: { "field" : "NombreEntidad",        "operator": "eq", "value" : "TES.IngresoEgreso" },
				IdEstado			   		: { "field" : "IdEstado",             "operator": "eq", "value" : "VIG" },
				DesplegarEnComboBox   		: { "field" : "DesplegarEnComboBox",  "operator": "eq", "value" : "S" }
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idPostFiltro  	: false
	};


	$scope.data.oConfiguracionCuentaBancoCorredora={
		tipo 			: "combobox-cuentas-bancarias-corredora",
		configuracion 	: {},
		filtros 		: {
				IdEstado  : { "field"   : "IdEstado", "operator": "eq",  "value"   : "ACT" },			
				IdMoneda  : { "field"   : "IdMoneda", "operator": "eq",  "value"   : "" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: false,
		idPostFiltro  	: false
	};
	
	
	$scope.data.oConfiguracionMedioDePago={
			tipo 			: "combobox-medio-pago",
			configuracion 	: {},
			filtros 		: {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: false,
			idPostFiltro  	: false
	};
	
	$scope.data.oConfiguracionMoneda={
		tipo 			: "combobox-moneda-tesoreria",
		configuracion 	: {},
		filtros 		: {IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: false,
		idPostFiltro  	: false				
	};	
	


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
										 IdMoneda				: $scope.oConfiguracionCargaMasiva.EditableEnProceso === 'S' ? $scope.data.oConfiguracionMoneda.seleccionado.IdMoneda || null : null,
										 IdConcepto				: $scope.oConfiguracionCargaMasiva.EditableEnProceso === 'S' ? $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto || null : null,
										 IdMedioDePago 			: $scope.oConfiguracionCargaMasiva.EditableEnProceso === 'S' ? $scope.data.oConfiguracionMedioDePago.seleccionado.IdMedioDePago || null : null, 
										 IdCuentaBanco			: $scope.oConfiguracionCargaMasiva.EditableEnProceso === 'S' ? $scope.data.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco || null : null
									  });
				
 
				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'TES';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_CargaMasivaProcesar			= cargaMasivaProcesar;
				datos.p_FechaProceso	   		    = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			
				console.log(datos);
				
				bcsServiciosTesoreria.ProcesarCargaMasiva(datos,function(respuesta){
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
	    $scope.data.oConfiguracionMoneda.idActual   = 0;
		$scope.data.oConfiguracionConcepto.idActual = 0;
		$scope.data.oConfiguracionCuentaBancoCorredora.idActual = 0;
		$scope.data.oConfiguracionMedioDePago.idActual = 0;
	};
	
	//----------------------------------------------------------
	//OBTIENE PREFERENCIA DE PAGO PARA LA CARGA MASIVA
	//----------------------------------------------------------
	if ($scope.SesionIdEmpresa)
	{	
			var dsPreferenciaPagoCargaMasiva = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1023",
	    				dataType: "json"
			    	}
				},
				filter: [
							{ field: "NombreEntidad", 			operator: "eq", value: 'TES.CargaMasiva' },
				         	{ field: "CodigoTipoCargaMasiva", 	operator: "eq", value: $scope.parametros.CodigoTipoCargaMasiva },				         
					        { field: "IdEmpresa",       		operator: "eq", value: $scope.SesionIdEmpresa },
					        { field: "IdEstado", 				operator: "eq", value: 'ACT' }
						],
			});
			
			dsPreferenciaPagoCargaMasiva.fetch().then(function()
			{	
				 if (dsPreferenciaPagoCargaMasiva.data().length == 0 ) 
				 {   //No existe configuracion
					 $scope.oConfiguracionCargaMasiva.EditableEnProceso = 'S';  //Si no existe preferencia de pago, deja editable el complemento
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() 
					 {
						 $scope.oConfiguracionCargaMasiva = dsPreferenciaPagoCargaMasiva.data();
						 		
						 if (dsPreferenciaPagoCargaMasiva.data().length > 1 )
						 {   //Si tiene más de una preferencia de pago, no se puede editar el complemento
							 $scope.oConfiguracionCargaMasiva.EditableEnProceso = 'N'; 	 
						 }
						 else
						 {	//Si tiene una sola preferencia de pago, se puede editar el complemento(si es que la preferencia lo tiene configurado así en BD)
							 $scope.oConfiguracionCargaMasiva.EditableEnProceso = $scope.oConfiguracionCargaMasiva[0].EditableEnProceso;
						 }
						 
						 $scope.data.oConfiguracionMoneda.idPostFiltro      = $scope.oConfiguracionCargaMasiva[0].IdMoneda || '';
						 $scope.data.oConfiguracionConcepto.idPostFiltro    = $scope.oConfiguracionCargaMasiva[0].IdConcepto || '';
						 $scope.data.oConfiguracionMedioDePago.idPostFiltro = $scope.oConfiguracionCargaMasiva[0].IdMedioDePago || '';
						 $scope.data.oConfiguracionCuentaBancoCorredora.idPostFiltro = $scope.oConfiguracionCargaMasiva[0].IdCuentaBanco || 0;
						 
						 return true;
					 });
				 }
		    });		 
	};
	
	var firstHeightCrear = $('#Win_ComplementoCargaMasiva');
        firstHeightCrear.css('height', '320px');
		
});
