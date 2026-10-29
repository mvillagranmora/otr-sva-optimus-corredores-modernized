angular.module('ModuloPlataforma').controller('ModificarTasasGenericasCtrl',function($scope,bcsComunes,bcsServicioPlataforma){
	
	var MTG = this;
	$scope.formData = {};
	
	idVentana 	= bcsComunes.ventanaWinId ($scope);   
    $scope.parametros 	= bcsComunes.ventanaParams(idVentana);

    $scope.formData.Clasificacion   =  $scope.parametros[0].NombreClasificacion;
    $scope.formData.TipoInstrumento =  $scope.parametros[0].NombreInstrumento;
    $scope.formData.TipoReajuste 	= $scope.parametros[0].IdTipoReajuste;

    $scope.formData.PlazoDesde	   	=  $scope.parametros[0].PlazoDesde;
    $scope.formData.PlazoHasta	   	=  $scope.parametros[0].PlazoHasta;
    $scope.formData.Precio		   	=  $scope.parametros[0].Precio;
    $scope.formData.Tasa		   	=  $scope.parametros[0].Tasa;

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
		bcsComunes.msg("error","error",'ERROR MODIFICACION');
		bcsComunes.ventanaOverlay(idVentana, false);
	};
    
	$scope.formData.ModificarTasasGenericas = function () {


		if( $scope.formData.PlazoHasta < $scope.formData.PlazoDesde ) {
			bcsComunes.msg("error","error", "UI_PLAZO_INCORRECTO");
			return;
		}
		
    	var parametros = {};		
    	
		parametros.p_IdEmpresa          = $scope.SesionIdEmpresa;
		parametros.p_IdUsuario          = $scope.SesionIdUsuario;
		
		parametros.p_IdAccion          	= "UPD";
		parametros.p_IdPrecioTasaTipoInstrumento = $scope.parametros[0].IdPrecioTasaTipoInstrumento;
		parametros.p_IdTipoinstrumento  = $scope.parametros[0].IdTipoInstrumento;
		parametros.p_IdTipoReajuste     = $scope.parametros[0].IdTipoReajuste;
		
		parametros.p_PlazoDesde    		= $scope.formData.PlazoDesde;
		parametros.p_PlazoHasta    		= $scope.formData.PlazoHasta;
		
		parametros.p_Tasa				= $scope.formData.Tasa;
		parametros.p_Precio				= $scope.formData.Precio;

		bcsServicioPlataforma.Servicios().CrearTasaGenerica(parametros,successCrearTasa,errorCrearTasa);
		
		
    };
    
    

});
