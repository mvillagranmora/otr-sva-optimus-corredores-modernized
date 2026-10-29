angular.module('ModuloFFM').controller('ModificarFondoAdministracionLiquidacionCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	var parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.datosAntes = parametros.ConfiguracionAdministracionLiquidez;
	$scope.datosDespues = angular.copy($scope.datosAntes);
	$scope.formData.decimalesMonto = util.bcs_formato(0,'FMM','Monto',$scope.datosDespues.IdMoneda,'','').cantidadDecimales;
	
	$scope.oSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdSocioDeNegocio : { "field": "IdSocioDeNegocio","operator": "eq", "value"   :  0 },
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
			},
		};

	$scope.oPortafolio={
		 tipo : "combobox-portafolio",
		 configuracion :{},
		 idActual : false,
		 seleccionado : false,
		 filtros: {
		 	 IdCliente    : { "field": "IdCliente","operator": "eq", "value" : 0 },
		 	 IdPortafolio : { "field": "IdPortafolio","operator": "eq", "value" : 0 }
		 },
	 };
	
	$scope.oFondos={
			filtros:{
				MoneyMarket : {"field": "MoneyMarket","operator":"eq","value":'S'},
				IdMoneda : {"field": "IdMoneda","operator":"eq","value":$scope.datosDespues.IdMoneda}
			}
	}
	
	$scope.guardar = function(){
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosConfigurarFondoAdministracionLiquidez = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_FondoAdministracionLiquidez : [{
					 	IdMoneda		     :	$scope.datosDespues.IdMoneda,
					    IdSerieFondo 	     :	$scope.formData.FondoMutuo.IdSerieFondo || '',
					    MontoMinimo 	     :	$scope.datosDespues.MontoMinimo || 0
				 }]
		 }
	     bcsServicioFondosMutuos.ConfigurarFondoAdministracionLiquidez(datosConfigurarFondoAdministracionLiquidez).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ajustaVentana();
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CONFIGURAR_FONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_FONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_FONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	}
	
	$scope.guardarPortafolio = function(){
		 bcsComunes.ventanaOverlay($scope.winID,true);
		 var datosConfigurarPortafolioFondoAdministracionLiquidez = {
				 p_IdEmpresa : $scope.SesionIdEmpresa,
				 p_IdUsuario : $scope.SesionIdUsuario,
				 p_IdModulo : 'FMM',
				 p_PortafolioFondoAdministracionLiquidez : [{
					 	IdMoneda		     :	$scope.datosDespues.IdMoneda,
					    IdSerieFondo 	     :	$scope.formData.datosFondoPortafolio.IdSerieFondo || '',
					    MontoMinimo 	     :	$scope.formData.MontoMinimoPortafolio || 0,
					    IdCliente			 :  $scope.oSocioNegocio.seleccionado.IdCliente,
					    IdPortafolio		 :	$scope.oPortafolio.seleccionado.IdPortafolio,
					    IdEstado			 :	'ACT'
				 }]
		 }
	     bcsServicioFondosMutuos.ConfigurarPortafolioAdministracionLiquidez(datosConfigurarPortafolioFondoAdministracionLiquidez).$promise.then(function(respuesta){
	     	 console.log(respuesta);
	     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_ERRORESCONTADOR === 0){
	     		$scope.ajustaVentana();
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CONFIGURAR_PORTAFOLIOFONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     		$scope.oSocioNegocio.idActual = false;
		   		$scope.oPortafolio.idActual = 0;
		   		$scope.formData.setFondoPortafolio = '-1';
		   		$scope.formData.MontoMinimoPortafolio = undefined;
	     		$scope.buscar();
	     	 }else{
	     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_PORTAFOLIOFONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	     	 }
  		 },function(respuesta){
  			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CONFIGURAR_PORTAFOLIOFONDO_ADMINISTRACION_LIQUIDEZ"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
  		 });
	}
	
	$scope.eliminarPortafolioAdministracionLiquidez = function(datos){
		if(!datos || datos.length == 0) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ELIMINAR_CONFIGURACION_PORTAFOLIO'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				var datosConfigurarPortafolioFondoAdministracionLiquidez = {
						 p_IdEmpresa : $scope.SesionIdEmpresa,
						 p_IdUsuario : $scope.SesionIdUsuario,
						 p_IdModulo : 'FMM',
						 p_PortafolioFondoAdministracionLiquidez : []
				 }
		    	$.each(datos,function(key,configuracion){
		    		datosConfigurarPortafolioFondoAdministracionLiquidez.p_PortafolioFondoAdministracionLiquidez.push({IdMoneda : $scope.datosDespues.IdMoneda,
		  																IdSerieFondo : configuracion.IdSerieFondo,
		  																MontoMinimo : configuracion.MontoMinimo,
		  																IdCliente : configuracion.IdCliente,
		  																IdPortafolio : configuracion.IdPortafolio,
		  																IdEstado : 'INA'});
		  		});
		    	overlay.encendido = true;
		    	bcsServicioFondosMutuos.ConfigurarPortafolioAdministracionLiquidez(datosConfigurarPortafolioFondoAdministracionLiquidez).$promise.then(function(result){
		    		var configuraciones = [];
					$.each(result.P_OUT_RESULTADO,function(key,value) {
						configuraciones.push({id: value.IdPortafolio , resultado: $translate.instant(value.Codigo), mensaje: ''});
					});

		     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
						tituloVentana : $translate.instant("UI_INFORMACION"), 
						nombreEntidad : $translate.instant("UI_SECUENCIA"), 
						operaciones : configuraciones   
					});
					
		     		$scope.buscar();
		    		overlay.encendido = false;
		    	},function(result){
		    		$scope.buscar();
		    		overlay.encendido = false;
		    	});
			}
		});
	}
	
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaAutoHeight($scope.winID);
    }
	
	$scope.abrirDetalle = function(datos)
	{
		var id = bcsComunes.ventanaAbrir("ModificarConfiguracionAdministracionLiquidezPortafolioFondoMutuo", {ConfiguracionAdministracionLiquidez: datos[0]} );
		var promesa=bcsComunes.ventanaPromesa(id);                               
        $.when(promesa).then(function(data){  
        	$scope.buscar();
        }); 
	};
	
	var ds = bcsConfiguracionFFM.getDataSourceFondosPortafolioAdministracionLiquidez();
	ds.definition.filter = $scope.filtros;
	
	var gridDefault = {
			columns: [
			          bcsConfiguracionFFM.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
			          ,bcsConfiguracionFFM.getConfiguracionColumna("NombreCompleto",{ groupable: true})
			          ,bcsConfiguracionFFM.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
			          ,bcsConfiguracionFFM.getConfiguracionColumna("IdMoneda",{ groupable: true})
			          ,bcsConfiguracionFFM.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
			          ,bcsConfiguracionFFM.getConfiguracionColumna("MontoMinimo",{ groupable: true})
			          ,bcsConfiguracionFFM.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
			],
			userSettings:true,
			sortable: true,
      		reorderable: true,
      		filterable: true,
			groupable: true,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: false,
      		scrollable: true,
      		height:400,
	
			dataSource: ds,
		};
	
	$scope.gridConfigPortafoliosLiquidez = gridDefault;

	$scope.buscar = function(){
		var filtros = [];
		filtros.push({ field: "IdMoneda", operator: "eq", value: $scope.datosDespues.IdMoneda});
		if($scope.oSocioNegocio.seleccionado)
			filtros.push({ field: "IdCliente", operator: "eq", value: $scope.oSocioNegocio.seleccionado.IdCliente});
		if($scope.oPortafolio.seleccionado)
			filtros.push({ field: "IdPortafolio", operator: "eq", value: $scope.oPortafolio.seleccionado.IdPortafolio});
		if ($scope.formData.datosFondoPortafolio)
			filtros.push({ field: "IdSerieFondo", operator: "eq", value: $scope.formData.datosFondoPortafolio.IdSerieFondo});
		if ($scope.formData.MontoMinimoPortafolio && $scope.formData.MontoMinimoPortafolio != '')
			filtros.push({ field: "MontoMinimo", operator: "eq", value: $scope.formData.MontoMinimoPortafolio});
		
		$scope.apiGridConsultaAdministracionLiquidez.dataSource.filter(filtros);
		$scope.apiGridConsultaAdministracionLiquidez.refresh();
	}
	
	
});