angular.module('ModuloPlataforma').controller('MantenedorExigibilidadCtrl',function($scope,$translate,$timeout,bcsComunes,bcsServiciosGta,bcsConfiguracionGTA,$state,bcsServicioPlataforma){
	
	//Configuracion de Ventana Inicial
	bcsComunes.configurarVentana(1200,780,true,true,'UI_MANTENEDOR_EXIGIBILIDAD');
	$scope.data = {};
    var idVentana = bcsComunes.ventanaWinId($scope);
    

	//Prueba
	$scope.data.oConfiguracionInstrumentoExigeGarantia={
		tipo 			: "combobox-instrumento-bolsa",
		configuracion 	: {},
		filtros 		: {
			IdBolsa 				: { "field"   : "IdBolsa",					"operator": "eq", "value"   : "" },
			IdEstado 		  		: { "field"   : "IdEstado",         		"operator": "eq", "value"   : "ACT" },
			ExigeGarantia 	  		: { "field"   : "ExigeGarantia",    		"operator": "eq", "value"   : "S" },
			IdTipoInstrumento 		: { "field"   : "IdTipoInstrumento",		"operator": "eq", "value"   : "" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};

	$scope.data.oConfiguracionEntidadDestinoGarantia={
		tipo 			: "combobox-entidad-destino-garantia",
		configuracion 	: {},
		filtros 		: {IdEstadoEntidadDestinoGarantia : { "field"   : "IdEstadoEntidadDestinoGarantia","operator": "eq", "value"   : "ACT" }},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};


	$scope.data.oConfiguracionTipoInstrumentoExigeGarantia={
		tipo 			: "combobox-tipo-instrumento",
		configuracion 	: {},
		filtros 		: {
				IdEstado 				: { "field"   : "IdEstado",					"operator": "eq", "value"   : "VIG" },
				ExigeGarantia 			: { "field"   : "ExigeGarantia",			"operator": "eq", "value"   : "S" },
				IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },			
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};

	$scope.limpiarFiltros = function() {
		$scope.data.oConfiguracionEntidadDestinoGarantia.idActual = 0;		
		$scope.data.oConfiguracionTipoInstrumentoExigeGarantia.idActual = 0;
		bcsConfiguracionGTA.dataSourceExigibilidad.removeAll();
	};
		   
	//GRILLA PRINCIPAL
	$scope.confExigibilidad = {
		dataSource			: bcsConfiguracionGTA.dataSourceExigibilidad,
		columns				: bcsConfiguracionGTA.columnasExigibilidad($scope.SesionLocale),		
		sortable			: true,
		filterable			: true,
		pageable			: true,
		//autoBind			: true,  //comentar
		height				: 470,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
	    selectable 			: "row"
	};
  
	$scope.buscarExigibilidad = function(){
		var filters = new Array();	
		if ($scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidad)
			filters.push({ field: "IdEntidadClasificadoraRiesgo", operator: "eq", value: $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidad});
		if ($scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado){
			
			
			if($scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.CodigoTipoInstrumento){//CGO
			
				if($scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.CodigoTipoInstrumento=='DER' && $scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento != 'DER'){
					filters.push({ field: "IdTipoInstrumentoSubyacente", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento });		
				}else{
					filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento });		
				}
			
			}
			
			
		}
		if ($scope.data.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento)
			filters.push({ field: "IdInstrumento", operator: "eq", value: $scope.data.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento});					
		bcsConfiguracionGTA.dataSourceExigibilidad.filters(filters);		
	};  
	
	$scope.GrabarExigibilidad = function() {
      	     var RegistrosExigibilidadaux = bcsConfiguracionGTA.dataSourceExigibilidad.dataColumns(["IdTipoInstrumento", "IdBolsa", "IdTipoInstrumentoSubyacente", "IdTipoClasificacionRiesgo", "Nemotecnico", "ExigibilidadEntidad","ExigibilidadCorredora", "IdExigibilidadGarantia", "IdInstrumento","IdEntidadClasificadoraRiesgo","ClasificacionRiesgo"],
                                                                               bcsConfiguracionGTA.dataSourceExigibilidad.itemsEdited());
	      	    if (RegistrosExigibilidadaux) {
                  var datosExigibilidad = {};
                     
          			datosExigibilidad.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
			        datosExigibilidad.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
			        datosExigibilidad.p_IdModulo = 'PLA'; //sacar de variables de sistema
			        datosExigibilidad.p_simularOperacion ='N'
			        datosExigibilidad.p_UsaReglasNegocio = 'N'; 
                    datosExigibilidad.p_TableExigibilidad =	RegistrosExigibilidadaux;
                    
                    bcsServiciosGta.CrearExigibilidad(datosExigibilidad,function(result)
				    {
				        	 console.log(JSON.stringify(result));
				        	 console.log("Error contador." + result.P_OUT_ERRORESCONTADOR);
				        	 var msg = "";
						        if (result.P_OUT_ERRORESCONTADOR == "0")
						        {
									msg = $translate.instant('UI_ACT_EXIGIBILIDAD_EXITO');								
									$scope.buscarExigibilidad();
						        }
						        else
						        {
						        	msg = $translate.instant('UI_ERR_EXIGIBILIDAD');
						        }
						        
						        bcsComunes.ventanaMsg({
						        	mensaje: msg,
						        	conflictos:util.parseServerResponse(result)
						        },idVentana);

						        
				        },
						function(result){
				        	bcsComunes.ventanaMsg({
					        	mensaje: $translate.instant('UI_ERR_EXIGIBILIDAD'),
					        	conflictos:util.parseServerResponse(result)
					        },idVentana);
						});
				       
				    }
	};	
	
		//Primer evento - filtro inicial
	$timeout(function(){
			
	},1000);
	
	//GO

});
