angular.module('ModuloTesoreria').controller('ConsultarCuentasBancariasCtrl',
		function($scope,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,$translate,bcsServiciosTesoreria,bcsBlotterCuentaBancariaGrid)
{
	 
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.btnActivo = true;
	
	//GRILLA PRINCIPAL
	$scope.confBasicaCuentasBancarias = {
		dataSource			: bcsBlotterCuentaBancariaGrid.dsCuentasBancarias,
		columns				: bcsBlotterCuentaBancariaGrid.columnasCuentasBancarias(),		
		sortable			: true,
		pageable			: true,
		//autoBind			: true,
		filterable			: true,
		height				: 370,
		selectable 			: "row" // "multiple row"
	};
	
	
	//CONFIGURACION COMBOBOX	
	$scope.oConfiguracionBanco={
			tipo          : "combobox-banco",
			configuracion : {},
			filtros       : { 
				IdEmpresa : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
				IdEstado  : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''			
	};
 
	
	$scope.oConfiguracionMoneda={
			tipo          : "combobox-moneda",
			configuracion : {},
			filtros       : {				
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''			
	};
	
	$scope.oConfiguracionTipoCuenta={
			tipo          : "combobox-tipo-cuenta-banco",
			configuracion : {},
			filtros       : {
				IdPais    : { "field"   : "IdPais","operator": "eq", "value"   : $scope.SesionIdPaisSistema },				
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : '',
			idPostFiltro  :	false
	};
	
	$scope.oConfiguracionEstado={
			tipo          : "combobox-estado-tesoreria",
			configuracion : {},
			filtros       : {
				NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "TES.CuentaBancaria" },
			},
			idActual      : false,
			seleccionado  : false,
			idDefecto     : ''
	};	
 
	
	//FUNCIONES
	$scope.abrirModal = function(oCuentaBancaria)
	{	
		
		console.log('DATOS ENVIADOS:' + JSON.stringify(oCuentaBancaria));
		
		var idModal = bcsComunes.ventanaAbrir("editarCuentasBancarias",{ CuentaBancaria: oCuentaBancaria, Empresa:String($scope.Empresa)},false,false);
		
        var promesa = bcsComunes.ventanaPromesa(idModal);
        
        promesa.then(function(data){ 
        	if (data === true)
			{	
        		//Actualiza datasource de la grilla
        		bcsBlotterCuentaBancariaGrid.dsCuentasBancarias.read();
			}
    	}); 
	};
	
	$scope.crearCuentaBancaria = function(){	
		$scope.abrirModal(undefined);
	};

	$scope.modificarCuentaBancaria = function(oCuentaBancaria){
		$scope.abrirModal(oCuentaBancaria);
	};
	
	
	$scope.limpiarFiltros = function ()
	{
		$scope.oConfiguracionBanco.idActual = false;
		$scope.oConfiguracionMoneda.idActual = false;
		$scope.oConfiguracionTipoCuenta.idActual = false;
		$scope.oConfiguracionEstado.idActual = false;		
		$scope.oConfiguracionBanco.idActual = 0;
		$scope.oConfiguracionMoneda.idActual = 0;
		$scope.oConfiguracionTipoCuenta.idActual = 0;
		$scope.oConfiguracionEstado.idActual = 0;
		
		bcsBlotterCuentaBancariaGrid.dsCuentasBancarias.removeAll();
		
		$scope.seleccionCuentasBancarias = false; //Quita selección de grilla
	}
	
	 
	$scope.buscarCuentas = function()
	{	
		var filters = new Array();		
		
		if ($scope.oConfiguracionBanco.seleccionado.IdBanco)
			filters.push({ field: "IdBanco", operator: "eq", value: Number($scope.oConfiguracionBanco.seleccionado.IdBanco || 0) });
		if ($scope.oConfiguracionMoneda.seleccionado.IdMoneda)
			filters.push({ field: "IdMoneda", operator: "eq", value: $scope.oConfiguracionMoneda.seleccionado.IdMoneda });
		if ($scope.oConfiguracionTipoCuenta.seleccionado.IdTipoCuentaBancaria)
			filters.push({ field: "IdTipoCuentaBancaria", operator: "eq", value: $scope.oConfiguracionTipoCuenta.seleccionado.IdTipoCuentaBancaria });		
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: $scope.oConfiguracionEstado.seleccionado.IdEstado });
		
		bcsBlotterCuentaBancariaGrid.dsCuentasBancarias.filters(filters);
	}
	
	
	//LLAMADA A SERVICIO DE ELIMINACION
	var successEliminarCuentaBancaria = function(respuesta)
	{	
		if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0)
		{	
			bcsBlotterCuentaBancariaGrid.dsCuentasBancarias.read();
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_DATOS_ELIMINAR_EXITO"), conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		}
		else
		{
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"), conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		}
	};

	var errorEliminarCuentaBancaria = function(respuesta)
	{	
		bcsComunes.ventanaOverlay($scope.winID, false);
		bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"), conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	};
	

	$scope.eliminarCuentaBancaria = function(oCuentaBancaria){
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_CUENTA_CORRIENTE_SELECCIONADA') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)
			{
				var parametros = {};
				bcsComunes.ventanaOverlay($scope.winID, true);
				
				parametros.p_IdEmpresa               = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario               = $scope.SesionIdUsuario;
				parametros.p_IdModulo                = 'TES';                                                                     
				parametros.p_simularOperacion        = 'N';                  
				parametros.p_UsaReglasNegocio        = 'S';
				parametros.p_IdCuentaBanco			 = oCuentaBancaria.IdCuentaBanco;						   
				   
				console.log("PARAMETROS:" + JSON.stringify(parametros));
				bcsServiciosTesoreria.EliminarCuentaBancaria(parametros, successEliminarCuentaBancaria, errorEliminarCuentaBancaria);
			}
		});
		
	

	};

	
});


