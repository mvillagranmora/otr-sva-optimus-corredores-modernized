angular.module('ModuloTesoreria').controller('ConsultarChequerasCtrl',
		function($scope,bcsComunes,$translate,bcsServiciosTesoreria,bcsBlotterCuentaBancariaGrid, $resource){
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.btnActivo =  true;
	
	//Obtiene datos de la empresa
	if ($scope.SesionIdEmpresa)
	{
			var empresaDatasource = new kendo.data.DataSource({
				type: "odata",
				serverFiltering:true,
	    		transport: {
	    			read: {	    				
	    				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",
	    				dataType: "json"
			    	}
				},
				filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
			});
	
	        empresaDatasource.fetch().then(function(){
				 if (empresaDatasource.data().length != 1 ) 
				 {
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = empresaDatasource.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });		
	};
	
	
	//GRILLA PRINCIPAL
	$scope.confBasicaChequeras = {
		dataSource			: bcsBlotterCuentaBancariaGrid.dsChequeras,
		columns				: bcsBlotterCuentaBancariaGrid.columnasChequeras(),		
		sortable			: true,
		pageable			: true,
		//autoBind			: true,
		filterable			: true,
		height				: 360,
		selectable 			: "row" // "multiple row"
	};
	
	$scope.oConfiguracionBanco={
			tipo          : "combobox-banco",
			configuracion : {},
			filtros       : {
							IdEmpresa : { "field"   : "IdEmpresa", "operator": "eq", "value"   : $scope.SesionIdEmpresa },
							IdEstado  : { "field"   : "IdEstado",  "operator": "eq", "value"   : "ACT" },
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
	
	$scope.fecInicio =  null;
	$scope.fecTermino = null;
	
	
	
	$scope.abrirModal = function(oChequera)
	{	
		var idModal = bcsComunes.ventanaAbrir("editarChequeras",{ Chequera: oChequera},false,false);
		
        var promesa = bcsComunes.ventanaPromesa(idModal);
        
        promesa.then(function(data){ 
        	if (data === true)
			{	
        		//Actualiza datasource de la grilla
        		bcsBlotterCuentaBancariaGrid.dsChequeras.read();
			}
    	}); 
	};

	
	$scope.crearChequera = function(){		
		$scope.abrirModal(undefined);
	};

	$scope.modificarChequera = function(oCuentaBancaria){		
		$scope.abrirModal(oCuentaBancaria);
	};
	
	$scope.limpiarFiltros = function (){
		$scope.oConfiguracionBanco.idActual = false;
		$scope.oConfiguracionMoneda.idActual = false;
		$scope.oConfiguracionTipoCuenta.idActual = false;
		$scope.oConfiguracionEstado.idActual = false;		
		$scope.oConfiguracionBanco.idActual = 0;
		$scope.oConfiguracionMoneda.idActual = 0;
		$scope.oConfiguracionTipoCuenta.idActual = 0;
		$scope.oConfiguracionEstado.idActual = 0;
		
		$scope.fecInicio =  null;
		$scope.fecTermino = null;		
		bcsBlotterCuentaBancariaGrid.dsChequeras.removeAll();
		
		$scope.seleccionChequeras = false; //Quita selección de grilla
	};
	
	$scope.buscarChequeras = function(){
		var filters = new Array();
		
		if ($scope.oConfiguracionBanco.seleccionado.IdBanco)
			filters.push({ field: "IdBanco", operator: "eq", value: Number($scope.oConfiguracionBanco.seleccionado.IdBanco || 0) });
		if ($scope.oConfiguracionMoneda.seleccionado.IdMoneda)
			filters.push({ field: "IdMoneda", operator: "eq", value: $scope.oConfiguracionMoneda.seleccionado.IdMoneda });
		if ($scope.oConfiguracionTipoCuenta.seleccionado.IdTipoCuentaBancaria)
			filters.push({ field: "IdTipoCuentaBancaria", operator: "eq", value: $scope.oConfiguracionTipoCuenta.seleccionado.IdTipoCuentaBancaria });		
		if ($scope.oConfiguracionEstado.seleccionado.IdEstado)
			filters.push({ field: "IdEstado", operator: "eq", value: $scope.oConfiguracionEstado.seleccionado.IdEstado });
		if ($scope.fecInicio)
			filters.push({ field: "FechaRetiro", operator: "gte", value: $scope.fecInicio});
		if ($scope.fecTermino)
			filters.push({ field: "FechaRetiro", operator: "lte", value: $scope.fecTermino});
		console.log("FILTROS: " + JSON.stringify(filters));		
		bcsBlotterCuentaBancariaGrid.dsChequeras.filters(filters);
		
	};
	
	var BloquearDesbloquearChequera = function(v_Dato){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=177";
		var r = $resource
		("", {}, {
					'urlConsulta': { method: "POST", url: urlConsulta}
				 }
		);
		var post = r.urlConsulta(v_Dato);
		return post.$promise;
	}
	
	$scope.bloquearDesbloquear =  function (Estado) {
		
		bcsComunes.ventanaOverlay($scope.winID, true)
		
		var parametrosSP = {}
		
		parametrosSP.p_IdEmpresa 		 = $scope.SesionIdEmpresa;
		parametrosSP.p_IdUsuario 		 = $scope.SesionIdUsuario;
		parametrosSP.p_IdModulo 		 = 'TES';
		parametrosSP.p_SimularOperacion  = 'N';
		parametrosSP.p_UsaReglasNegocio	 = 'S';
		parametrosSP.p_IdChequera		 = $scope.seleccionChequeras.IdChequera;
		parametrosSP.p_FechaModificacion = util.dateSPdate(new Date());
		parametrosSP.p_IdEstado 		 = Estado;
		parametrosSP.p_Dato 			 = [{SubRegistro: 'CHEQUERA', CampoRegistro:'IdEstado', ValorAntiguo: $scope.seleccionChequeras.IdEstado, ValorNuevo: Estado}];
		bcsComunes.log().info(JSON.stringify(parametrosSP));
		
		BloquearDesbloquearChequera(parametrosSP).then(
				function(result) {
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						bcsComunes.msg("warning", "Advertencia", "La operacion tiene advertencias");
					}else if (result.P_OUT_ERRORESCONTADOR) {
						bcsComunes.msg("error", "Error", "La operacion tiene errores");
					}else {
						bcsComunes.msg("info", "info", "Operacion realizada con exito");						
						bcsBlotterCuentaBancariaGrid.dsChequeras.read();
						$scope.seleccionChequeras = false;
					}
					bcsComunes.ventanaOverlay($scope.winID, false)
				},
				function(result){
					bcsComunes.msg("error", "Error", "La operacion tiene errores");
					bcsComunes.ventanaOverlay($scope.winID, false)
				});
	};
});