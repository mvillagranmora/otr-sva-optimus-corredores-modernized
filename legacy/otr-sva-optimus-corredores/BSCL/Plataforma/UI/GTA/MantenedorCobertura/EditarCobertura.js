
	angular.module('ModuloPlataforma').controller('EditarCoberturaCtrl',
			function ($scope,$translate, $timeout,bcsComunes,bcsServiciosGta) {
	
			
	$scope.data	= {};			 
	$scope.data.showPlazoTope = false;  //Debe aparecer sólo cuando la cobertura sea de RF o IF.
	$scope.activaConcepto = false;
		
	$scope.winID = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams($scope.winID);
	
	
	//---------------------------------------------------	
	// CONFIGURACION DE COMBOBOX 
	//---------------------------------------------------
	
	$scope.data.oConfiguracionEntidadDestinoGarantia={
			tipo 			: "combobox-entidad-destino-garantia",
			configuracion 	: {},
			filtros 		: {IdEstadoEntidadDestinoGarantia : { "field"   : "IdEstadoEntidadDestinoGarantia","operator": "eq", "value"   : "ACT" }},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idpf			: false				
	};	
	
	$scope.data.oConfiguracionConceptoGarantia={
			tipo : "combobox-conceptos-garantia",
			configuracion : {},
			filtros : {
				IdEstado			: { "field"  : "IdEstado","operator": "eq", "value"   : "VIG" },
				ConfiguradoEnModulo	: { "field"  : "ConfiguradoEnModulo","operator": "eq", "value"   : "S" },
				VisibleOptimus		: { "field"  : "VisibleOptimus","operator": "eq", "value" : "S" },
				IdClasificacionTipoInstrumento :{"field"  : "IdClasificacionTipoInstrumento","operator": "eq", "value" : "DV" },
				IdTipoOperacion     : {"field"  : "IdTipoOperacion","operator": "eq", "value" :"-" },
				ConCertificadoAutomatico     : {"field"  : "ConCertificadoAutomatico","operator": "eq", "value" :"S" }

			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoInstrumentoExigeGarantia={
		tipo 			: "combobox-tipo-instrumento",
		configuracion 	: {},
		filtros 		: {
				IdEstado 				: { "field"   : "IdEstado",					"operator": "eq", "value"   : "VIG" },
				ExigeGarantia 			: { "field"   : "ExigeGarantia",			"operator": "eq", "value"   : "S" },
				IdEntidadDestinoGarantia: { "field"   : "IdEntidadDestinoGarantia",	"operator": "eq", "value"   : 0 },			
				IdTipoInstrumento 					: { "field" : "IdTipoInstrumento",  "operator": "ne",  "value" : "IOA" },
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: ''
	};

	$scope.data.oConfiguracionInstrumentoExigeGarantia={
		tipo 			: "combobox-instrumento-bolsa",
		configuracion 	: {},
		filtros 		: {
				IdEstado          		: { "field": "IdEstado",         		"operator": "eq", "value": "ACT"},
				ExigeGarantia     		: { "field": "ExigeGarantia",    		"operator": "eq", "value": "S"},
				IdTipoInstrumento 		: { "field": "IdTipoInstrumento",		"operator": "eq", "value": "" },
				IdEntidadDestinoGarantia: { "field":"IdEntidadDestinoGarantia" , "operator":"eq" , "value": 0}
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idpf			: false
	};			
	
	
	
	$scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento={
		tipo 			: "combobox-clasificacion-riesgo-tipo-instrumento",
		configuracion 	: {},		
		filtros 		: {
			IdEstadoClasificacionRiesgo      : { "field"   : "IdEstadoClasificacionRiesgo"     , "operator": "eq", "value"   : "ACT" },
			IdEstadoTipoClasificacionRiesgo  : { "field"   : "IdEstadoTipoClasificacionRiesgo" , "operator": "eq", "value"   : "ACT" },
			///IdEstadoTipoInstrumentoClasificacionRiesgo : {"field" : "IdEstadoTipoInstrumentoClasificacionRiesgo", "operator" : "eq", "value" : "ACT"},			 
			IdClasificacionTipoInstrumento   : { "field"   : "IdClasificacionTipoInstrumento"  , "operator": "eq", "value"   : "" },
			IdTipoInstrumento 				 : {"field" : "IdTipoInstrumento",  "operator" : "eq", "value" : ""},
		},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idpf			: false	
	};
	

	//Configuración de Combo Tipo instrumento para garantizar	
	$scope.data.oConfiguracionTipoInstrumentoComoGarantia={
			tipo 			: "combobox-tipo-instrumento-como-garantia",		
			configuracion 	: {},			
			filtros 		: {
					IdEstadoAval 		: { "field"   : "IdEstadoAval","operator": "eq", "value"   : "ACT" },
					CodigoTipoInstrumentoGarantizado 	: {"field" : "CodigoTipoInstrumentoGarantizado",  "operator" : "eq", "value" : ""},				
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: '',
			idpf			: false	
	}; 
	 

	//Configuración de Combo Estado de Coberturas
	$scope.data.oConfiguracionEstadoGarantia={
					tipo 			: "combobox-estado-custodia",
					configuracion 	: {},
					filtros 		: {
							NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "GARANTIA.COBERTURA" }
					},
					idActual 		: false,
					seleccionado 	: false,
					idDefecto 		: ''
	};	
		

	//aparece sólo cuando la cobertura sea de RF o IF.
	$scope.data.unidadPlazoOptions = {
	     placeholder	: "Plazo",
	     dataTextField	: "text",
	     dataValueField	: "value",
	     dataSource		: [
			         { text: "Día", value: "D" },
			         { text: "Mes", value: "M" },
			         { text: "Año", value: "A" }
	     ],
	     filter			: "contains",
	     autoBind		: true,
	     readonly       : true,
	     minLength		: 2,
	     index			: 2
	};

	
	//---------------------------------------------------
	//FUNCIONES
	//---------------------------------------------------
	
	$scope.limpiarFormulario = function()
	{	
		$scope.data.oConfiguracionEntidadDestinoGarantia.idActual = false;
		$scope.data.oConfiguracionTipoInstrumentoExigeGarantia.idActual = false;
		$scope.data.oConfiguracionInstrumentoExigeGarantia.idActual = false;
		$scope.data.oConfiguracionTipoInstrumentoComoGarantia.idActual = false;
		$scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.idActual = false;		
		$scope.data.oConfiguracionEstadoGarantia.idActual = false;
		$scope.data.plazoTope = 0;
		$scope.data.unidadPlazo = '';
		$scope.data.kngCoberturaEntidad = 0;
		$scope.data.kngCoberturaCorredor = 0;
		$scope.data.showPlazoTope = false;  //Debe aparecer sólo cuando la cobertura sea de RF o IF.
	};	
	
	
	
	//Carga en el formulario los datos actuales para edición	
	$scope.cargarDatosFormulario = function () 
	{
		$scope.data.unidadPlazo = parametros.UnidadPlazo;
		$scope.data.plazoTope = parametros.PlazoTope;
		$scope.data.kngCoberturaEntidad  = parametros.CoberturaEntidad;
		$scope.data.kngCoberturaCorredor = parametros.CoberturaCorredora;
		$scope.data.oConfiguracionEntidadDestinoGarantia.idpf = parametros.IdEntidadDestinoGarantia;
		$scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.idpf = parametros.IdClasificacionRiesgo;
		$scope.data.oConfiguracionEstadoGarantia.idActual = String(parametros.IdEstado);
		$scope.data.oConfiguracionEstadoGarantia.idDefecto = String(parametros.IdEstado);
		$scope.data.oConfiguracionTipoInstrumentoExigeGarantia.idpf = String(parametros.IdTipoInstrumentoExigeGarantia);
		$scope.data.oConfiguracionInstrumentoExigeGarantia.idpf = parametros.IdInstrumentoExigeGarantia;
		$scope.data.oConfiguracionTipoInstrumentoComoGarantia.idpf = String(parametros.IdTipoInstrumentoComoGarantia);

	};
	

	//Al cambiar TipoInstrumentoComoGarantia, Activa o Desactiva "PlazoTope"
	$scope.$watch('data.oConfiguracionTipoInstrumentoComoGarantia.seleccionado',
		function(newValue, oldValue) {
			if (newValue) {
				// El PlazoTope debe aparecer sólo cuando la cobertura sea de RF o IF
				if (newValue.IdClasificacionTipoInstrumentoAval == 'RF' || newValue.IdClasificacionTipoInstrumentoAval == 'IF')
					$scope.data.showPlazoTope = true;
				else
					$scope.data.showPlazoTope = false;

			}
		});
	
	$scope.$watch('data.oConfiguracionTipoInstrumentoExigeGarantia.idActual',
			function(newValue, oldValue) {
				if (newValue!= oldValue && newValue) {
					if($scope.data.oConfiguracionTipoInstrumentoExigeGarantia.idActual=="FWD"){
						$scope.activaConcepto = true;
					}else{
						$scope.activaConcepto = false;
					}
					
				}
			});
		
	
	
	
	var successProceso = function(result)
	{	
		var mensajeExito= "";
		if (result.P_OUT_ERRORESCONTADOR == "0")
			if (parametros.Accion === 'CREAR')
				mensajeExito = $translate.instant('DATOS_INGRESO_EXITO');
			else if (parametros.Accion === 'MODIFICAR')
				mensajeExito = $translate.instant('DATOS_MODIFICACION_EXITO');	
		else	    
			mensajeExito = $translate.instant('UI_ERROR_INGRESO');
	    
		bcsComunes.ventanaMsg({
			mensaje		: mensajeExito,
			conflictos	: util.parseServerResponse(result)
		},$scope.winID);
		
		bcsComunes.ventanaOverlay($scope.winID,false);
		
		if (result.P_OUT_ERRORESCONTADOR == "0")
			bcsComunes.ventanaCerrar($scope.winID); //cierra sino hya errores	
		
	};
	
	
	var errorProceso = function(result)
	{	
		bcsComunes.ventanaMsg({
			mensaje: $translate.instant('UI_ERROR_INGRESO'),
			conflictos:util.parseServerResponse(result)
		},$scope.winID);		
		bcsComunes.ventanaOverlay($scope.winID,false);
	};
	
	
	//---------------------------------------------------
	// CREAR COBERTURA GARANTIA
	//---------------------------------------------------
	
	$scope.crearCoberturaGarantia = function() 
	{
		var datosIngreso = {};		
		bcsComunes.ventanaOverlay($scope.winID,true);

		datosIngreso.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		datosIngreso.p_IdUsuario 		= $scope.SesionIdUsuario;
		datosIngreso.p_IdModulo 		= 'PLA';
		datosIngreso.p_simularOperacion = 'N';
		datosIngreso.p_UsaReglasNegocio = 'S';
		datosIngreso.p_Cobertura = [{
			p_IdEntidadDestinoGarantia : $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia || 0,
			p_IdTipoInstrumentoExigeGarantia : $scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento || '',
			p_IdInstrumentoExigeGarantia : $scope.data.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento || '0',
			p_IdTipoInstrumentoComoGarantia : $scope.data.oConfiguracionTipoInstrumentoComoGarantia.seleccionado.IdTipoInstrumentoAval || '0',
			p_ClasificacionRiesgo : $scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.seleccionado.ClasificacionRiesgo || '',
			p_IdTipoClasificacionRiesgo : $scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.seleccionado.IdTipoClasificacionRiesgo || '',
			p_IdEntidadClasificadoraRiesgo : $scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.seleccionado.IdEntidadClasificadoraRiesgo || '',		
			p_PlazoTope : $scope.data.plazoTope || 0,
			p_UnidadPlazo : $scope.data.unidadPlazo || '',
			p_CoberturaEntidad : $scope.data.kngCoberturaEntidad || 0,
			p_CoberturaCorredora : $scope.data.kngCoberturaCorredor || 0,
			p_IdEstado : $scope.data.oConfiguracionEstadoGarantia.seleccionado.IdEstado || '',
			p_IdConcepto : $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto || ''
		}]
		console.log("DATOS INGRESO:" + JSON.stringify(datosIngreso));

		bcsServiciosGta.CrearCoberturaGarantia(datosIngreso, successProceso, errorProceso);

	}



	//---------------------------------------------------
	// MODIFICAR COBERTURA GARANTIA
	//---------------------------------------------------			
			
	$scope.modificarCoberturaGarantia = function()
	{	
		var datosIngreso = {};
		
		//Valida que %cobertura no sea muy alto(puede ser mayor a 100%) 
		if ($scope.data.kngCoberturaEntidad > 399)
		{	
			bcsComunes.ventanaMsg({
				mensaje: $translate.instant('UI_COBERTURA_FUERA_RANGO')
			},$scope.winID);
			return;
		}
		
		bcsComunes.ventanaOverlay($scope.winID,true);
		datosIngreso.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		datosIngreso.p_IdUsuario 		= $scope.SesionIdUsuario;
		datosIngreso.p_IdModulo 		= 'PLA';
		datosIngreso.p_simularOperacion = 'N';
		datosIngreso.p_UsaReglasNegocio = 'S';
		datosIngreso.p_Cobertura = [{
			p_IdCoberturaGarantia : parametros.IdCoberturaGarantia,
			p_IdEntidadDestinoGarantia : $scope.data.oConfiguracionEntidadDestinoGarantia.seleccionado.IdEntidadDestinoGarantia || 0,
			p_IdTipoInstrumentoExigeGarantia : $scope.data.oConfiguracionTipoInstrumentoExigeGarantia.seleccionado.IdTipoInstrumento || '',
			p_IdInstrumentoExigeGarantia : $scope.data.oConfiguracionInstrumentoExigeGarantia.seleccionado.IdInstrumento || '0',
			p_IdTipoInstrumentoComoGarantia : $scope.data.oConfiguracionTipoInstrumentoComoGarantia.seleccionado.IdTipoInstrumentoAval || '0',
			p_ClasificacionRiesgo : $scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.seleccionado.ClasificacionRiesgo || '',
			p_IdTipoClasificacionRiesgo : $scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.seleccionado.IdTipoClasificacionRiesgo || '',
			p_IdEntidadClasificadoraRiesgo : $scope.data.oConfiguracionClasificacionRiesgoTipoInstrumento.seleccionado.IdEntidadClasificadoraRiesgo || '',		
			p_PlazoTope : $scope.data.plazoTope || 0,
			p_UnidadPlazo : $scope.data.unidadPlazo || '',
			p_CoberturaEntidad : $scope.data.kngCoberturaEntidad || 0,
			p_CoberturaCorredora : $scope.data.kngCoberturaCorredor || 0,
			p_IdEstado : $scope.data.oConfiguracionEstadoGarantia.seleccionado.IdEstado || '',
			p_IdConcepto : $scope.data.oConfiguracionConceptoGarantia.seleccionado.IdConcepto || ''
		}]
		console.log("DATOS A MODIFICAR:" + JSON.stringify(datosIngreso));				
		
		bcsServiciosGta.ModificarCoberturaGarantia(datosIngreso, successProceso, errorProceso);				
	}


	$scope.grabarCoberturaGarantia = function()
	{
		if (parametros.Accion === 'CREAR')
		{
			$scope.crearCoberturaGarantia ();
		}	
		else if(parametros.Accion === 'MODIFICAR') 
		{
			$scope.modificarCoberturaGarantia();
		}
	}
	
	
	$timeout(function(){
		$scope.cargarDatosFormulario();
	},1000);
});
