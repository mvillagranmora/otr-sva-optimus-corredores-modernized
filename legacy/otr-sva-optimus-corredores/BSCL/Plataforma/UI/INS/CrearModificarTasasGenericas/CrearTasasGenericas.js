angular.module('ModuloPlataforma').controller('CrearTasasGenericasCtrl',function($scope,bcsComunes,bcsServicioPlataforma){
	
	var MTG = this;
	$scope.filtros = {};
	$scope.formData = {};
	
	idVentana = bcsComunes.ventanaWinId ($scope);   
    $scope.Parametros = bcsComunes.ventanaParams(idVentana);
    $scope.formData.setInstrumento = "PAGARE NR";    
    $scope.formData.EsIngreso = true;

	$scope.IdEstado       = { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" };
	$scope.Clasificacion  = [ { field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'RF' },
	                           { field: "IdClasificacionTipoInstrumento", operator: "eq", value: 'IF' }]
    
    
	//CONFIGURACION DE COMBOBOX
	$scope.formData.oConfiguracionClasificacion={
		configuracion   : {},
		filtros : {
        complejo : [$scope.IdEstado,$scope.Clasificacion],
        	logic: "and",
        	filters: [
        		{
        			logic: "or",
        			filters: $scope.Clasificacion
        		},
        		$scope.IdEstado        
        		]}, 
        idActual     : false,
	  	seleccionado   : false,
	  	idDefecto     : '' 	            
	 };
    

	$scope.formData.oConfiguracionTipoInstrumento = {
			filtros : { 	
				IdClasificacionTipoInstrumento : {
			        "field"   : "IdClasificacionTipoInstrumento",
			        "operator": "eq",
			        "value"   : "" 
				}				
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
	 
	 
	 
	 $scope.formData.oConfiguracionInstrumentoTipoReajuste={
/*		filtros : { 	
			IdInstrumento : {
		        "field"   : "IdInstrumento",
		        "operator": "eq",
		        "value"   : "" 
			}				
		},*/
		configuracion : {
			template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
		},
		idDefecto : '',
		filtros : {
			IdInstrumento		: { "field"   : "IdInstrumento", "operator": "eq", "value"   : "" },
			IdTipoInstrumento	: { "field"   : "IdTipoInstrumento", "operator": "eq", "value"   : "" },
		} 
	};	 
	 

	var successCrearTasa = function(respuesta)
	{	
		if (respuesta.P_OUT_ERRORESCONTADOR == 0)
		{
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg("info","info",'Tasa creada con éxito');
			
		}
		else
		{	
			var id = bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaPromesa(id).then(function(result){
			});
			
		}		
		bcsComunes.ventanaOverlay(idVentana, false);
		bcsComunes.ventanaData(idVentana, respuesta);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
	var errorCrearTasa = function(respuesta)
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.msg("error","error",'INGRESO ERROR');
		
		bcsComunes.ventanaOverlay(idVentana, false);
	};
    
	$scope.formData.CrearTasa = function () {

		if( $scope.formData.PlazoHasta < $scope.formData.PlazoDesde ) {
			bcsComunes.msg("error","error", "UI_PLAZO_INCORRECTO");
			return;
		}
		
    	var parametros = {};		
    	
		parametros.p_IdEmpresa          = $scope.SesionIdEmpresa;
		parametros.p_IdUsuario          = $scope.SesionIdUsuario;
		
		parametros.p_IdAccion          	= "INS";		
		parametros.IdPrecioTasaTipoInstrumento = 0;
		parametros.p_IdTipoinstrumento  = $scope.formData.IdTipoInstrumento;
		parametros.p_IdTipoReajuste     = $scope.formData.oConfiguracionInstrumentoTipoReajuste.idActual;
		
		parametros.p_PlazoDesde    		= $scope.formData.PlazoDesde;
		parametros.p_PlazoHasta    		= $scope.formData.PlazoHasta;		
		
		parametros.p_Tasa				= $scope.formData.Tasa;
		parametros.p_Precio				= $scope.formData.Precio;
		
		bcsServicioPlataforma.Servicios().CrearTasaGenerica(parametros,successCrearTasa,errorCrearTasa);
    };

      
	$scope.$watch('formData.oConfiguracionClasificacion.idActual', function(newval,oldval ){
		if (newval === 'IF') {
			$scope.formData.setInstrumento = "PAGARE NR";
		} else {
			$scope.formData.setInstrumento = "-1";
		}
	});
	
	

	
	if ( $scope.Parametros.IdPrecioTasaTipoInstrumento != undefined ) {

		$scope.formData.oConfiguracionClasificacion.idActual = $scope.Parametros.IdClasificacionTipoInstrumento;
		$scope.formData.oConfiguracionClasificacion.idDefecto = $scope.Parametros.IdClasificacionTipoInstrumento;
		$scope.formData.oConfiguracionTipoInstrumento.idActual = $scope.Parametros.IdTipoInstrumento;
		$scope.formData.oConfiguracionTipoInstrumento.idDefecto = $scope.Parametros.IdTipoInstrumento;			
		$scope.formData.oConfiguracionInstrumentoTipoReajuste.idActual = $scope.Parametros.IdTipoReajuste;
		$scope.formData.oConfiguracionInstrumentoTipoReajuste.idDefecto = $scope.Parametros.IdTipoReajuste;			
		
		if ( $scope.Parametros.IdPrecioTasaTipoInstrumento > 0 ) {
			// Registro Existe
			$scope.formData.EsIngreso = false;
		} else {
			// Registro Nuevo
			$scope.formData.EsIngreso = true;
		}
	} 
	
	
	
});
