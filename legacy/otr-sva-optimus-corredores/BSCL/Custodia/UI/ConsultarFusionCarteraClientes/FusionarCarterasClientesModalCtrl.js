angular.module('ModuloCustodia').controller('FusionarCarterasClientesModalCtrl',
function($scope, $timeout, $state, bcsComunes, $rootScope, $translate, bcsServiciosCustodia)
{
	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(idVentana);
	var idVentanaPadre = parametros.idVentanaPadre;
	
/******************************************************/
/*** CONFIGURACIÓN DE FILTROS *************************/
/******************************************************/

		$scope.oConfiguracionCliente = {

			tipo : "combobox-socio-negocio",
			configuracion :{},
			filtros: {
					IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
					EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},			
			idActual : undefined,
			seleccionado : -1,
			idDefecto : -1
		};
		
		$scope.oConfiguracionPortafolio = {

			tipo : "combobox-portafolio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
			},
		};
		
		$scope.oConfiguracionClasificacionTipoInstrumento = {
			tipo : "combobox-clasificacion-tipo-instrumento",
			configuracion : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false,
			filtros : { 	
					IdClasificacionDV : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "DV"},
					IdClasificacionFM : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "FM"},
					IdClasificacionSP : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "SP"},
					IdClasificacionRE : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "RE"},
					IdClasificacionFW : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "FW"},
					IdClasificacionIR : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "IR"}
			}
		};
		
		$scope.oConfiguracionTipoInstrumento = {
			tipo : "combobox-tipo-instrumento",
			configuracion : {},
			filtros : {
				IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
				IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
				NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
				NoPrestamos						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PRT" },
				NoEmprestitos					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "EMP" },
				NoSimultaneas					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
				NoReportesRV					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRV" },
				NoReportesRF					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRF" },
				NoSpot 							: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SPT" },
				NoDerivados						: { "field" : "CodigoTipoInstrumento",          "operator": "neq", "value" : "DER" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			idPostFiltro : false
		};
		
		$scope.oConfiguracionInstrumento = {

    		buscar : "-1",

    		filtros : {

        		IdTipoInstrumento : 
        		{
            	field : "IdTipoInstrumento",
            	operator : "eq",
            	value : ""
        		}
    		}

		};	
		
/******************************************************/
/*** FUNCIONES ***************************************/
/******************************************************/		
		
		$scope.validarFiltrosFusion = function () {

    		if (!$scope.oConfiguracionCliente.seleccionado || !$scope.oConfiguracionCliente.seleccionado.IdCliente) 
    		{
        		bcsComunes.msg("error",$translate.instant("UI_ERROR"), "Debe seleccionar un Cliente.");
        		return false;
    		}
    		
    		if (!$scope.oConfiguracionPortafolio.seleccionado || !$scope.oConfiguracionPortafolio.seleccionado.IdPortafolio) 
    		{
        		bcsComunes.msg("error",$translate.instant("UI_ERROR"),"Debe seleccionar un Portafolio.");
        		return false;
    		}

    		if (!$scope.oConfiguracionClasificacionTipoInstrumento.seleccionado || !$scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento) 
    		{
        		bcsComunes.msg("error",$translate.instant("UI_ERROR"),"Debe seleccionar un Mercado.");
        		return false;
    		}
    	return true;
		};
		
		$scope.previsualizarFusion = function () {
    		if (!$scope.validarFiltrosFusion()) {
        		return;
    		}

    		var datos = {
        		idVentanaPadre : idVentanaPadre,
        		filtros : {
            		p_IdCliente : $scope.oConfiguracionCliente.seleccionado.IdCliente,
            		p_IdPortafolio : $scope.oConfiguracionPortafolio.seleccionado.IdPortafolio,
            		p_IdClasificacionTipoInstrumento : $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento,
            		p_IdTipoInstrumento : $scope.oConfiguracionTipoInstrumento.seleccionado ? $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento : null,
            		p_Nemotecnico : $scope.instrumentoDatos ? $scope.instrumentoDatos.Nemotecnico : null
        		}
   	 		};
   	 		
   	 		var idVentanaPreview = bcsComunes.ventanaAbrir(
        		"FusionarCarterasClientesPreviewModal",
        		datos,
        		false,
        		false
    		);

    		var promesa = bcsComunes.ventanaPromesa(idVentanaPreview);
    		promesa.then(function (fusionEjecutada) {
        		if (fusionEjecutada) {
					bcsComunes.ventanaData(idVentana, true);
            		bcsComunes.ventanaCerrar(idVentana);
        		}
    		});
		};
		
		$scope.cerrar = function () {
    		bcsComunes.ventanaCerrar(idVentana);
		};
		
/******************************************************/
/*** WATCHERS ****************************************/
/******************************************************/		
    	$scope.$watch('oConfiguracionClasificacionTipoInstrumento.seleccionado',function (nuevo) 
    	{
        	if (!nuevo) 
        	{
            	return;
        	}
        	$scope.oConfiguracionTipoInstrumento.filtros.IdClasificacionTipoInstrumento.value = nuevo.IdClasificacionTipoInstrumento;
        	$scope.oConfiguracionTipoInstrumento.idActual = "";
        	$scope.oConfiguracionTipoInstrumento.seleccionado = false;       	
        	$scope.oConfiguracionInstrumento.buscar = "-1";
			$scope.oConfiguracionInstrumento.filtros.IdTipoInstrumento.value = "";
			$scope.instrumentoDatos = null;
    	});	
    	
    	$scope.$watch('oConfiguracionTipoInstrumento.seleccionado', function (nuevo) 
    	{
    		$scope.instrumentoDatos = null;
    		$scope.oConfiguracionInstrumento.filtros.IdTipoInstrumento.value = (nuevo && nuevo.IdTipoInstrumento)? nuevo.IdTipoInstrumento : "";

		});	
		
		$scope.$watch('oConfiguracionCliente.seleccionado', function (nuevo) 
		{
    		if (!nuevo || !nuevo.IdCliente) 
    		{
        		return;
    		}

    		$scope.oConfiguracionPortafolio.filtros.IdCliente.value = nuevo.IdCliente;
    		$scope.oConfiguracionPortafolio.idActual = false;
    		$scope.oConfiguracionPortafolio.seleccionado = false;

    		$scope.oConfiguracionClasificacionTipoInstrumento.idActual = "";
    		$scope.oConfiguracionClasificacionTipoInstrumento.seleccionado = false;

    		$scope.oConfiguracionTipoInstrumento.idActual = "";
    		$scope.oConfiguracionTipoInstrumento.seleccionado = false;

    		$scope.oConfiguracionInstrumento.buscar = "-1";
    		$scope.instrumentoDatos = null;
		});
});


