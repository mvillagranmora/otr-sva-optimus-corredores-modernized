angular.module('ModuloPlataforma').controller('ModalClienteDocumentoSubirCtrl', 
		function($scope,bcsComunes,$translate,$resource,bcsServicioPlataforma){
	$scope.recordData = {};
	$scope.recordData.estatico = true;
	var ventanaId = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams (ventanaId);

	$scope.tipoDocumento = parametros.TipoDocumento;
	
	$scope.recordData.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.recordData.IdUsuario	= $scope.SesionIdUsuario;

	$scope.CodigoFormulario = parametros.CodigoFormulario;
	$scope.camposRequeridos = {};
	bcsServicioPlataforma.CamposRequeridosFormulario($scope.CodigoFormulario, function(respuesta) {
		$scope.camposRequeridos = respuesta;
	});

	
	$scope.subirArchivo = function(){
		bcsComunes.ventanaOverlay(ventanaId,true);
		var file = document.getElementById("data").files[0];//TODO validar que este seleccionado el archivo.
		if(file===undefined){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
			return;
		}
		var filename = file.name;
		var extension = filename.substr(file.name.lastIndexOf(".")+1);
		var extensions = ["PDF", "PNG", "BMP", "JPG"];
		if(extensions.indexOf(extension.toUpperCase()) < 0){
			bcsComunes.ventanaOverlay(ventanaId,false);
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_INVALIDO"));
			return;
		}

		var formData = new FormData(document.getElementById('formModalClienteDocumentoSubir'));
		formData.append("IdDocumento", parametros.IdDocumento || 0 );
		formData.append("IdAnexo", parametros.IdAnexo || 0);
		formData.append("IdCliente", parametros.IdCliente);
		formData.append("IdTipoDocumento", parametros.IdTipoDocumento);
		formData.append("NombreArchivoAdjunto", filename);
		formData.append("FormatoArchivoAdjunto", extension.toUpperCase());

		formData.append("NumeroFirmantesEmpresa", 0);
		formData.append("NumeroFirmantesCliente", 0);
		formData.append("IdTipoCanalIngreso", "OPT");
		
		$.ajax({
			method: "POST",
        	url: "/BSCL/Plataforma/GWJAVA/PLA/DocumentosClienteBin",
        	data: formData,
        	contentType : false,
        	processData: false,
        	cache: false
        }).done(function( msg ) {
        	var result = JSON.parse(msg);

        	if($scope.SesionLocale!=="PE"){
        		result["P_ERRORESCONTADOR"] = (result.respuesta==="OK")?0:1;
				$scope.respuestaServicio(result, $translate.instant((result.respuesta==="OK")?"UI_ARCHIVO_SUBIDO_CORRECTAMENTE":"UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"), ventanaId);
        	}else{
        		//firmar automaticamente el documento
        		$scope.firmarAnexoPeru(result.IdDocumento, result.IdAnexo);
        	}
    	}).error(function (jqXHR) {
    		console.log(jqXHR)
    		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
    		bcsComunes.ventanaOverlay(ventanaId,false);
    		document.getElementById('formModalClienteDocumentoSubir').reset();
        });
	}

	$scope.respuestaServicio = function(result, titulo, ventanaId){
		var resultId = bcsComunes.ventanaMsg({ mensaje : titulo,
		conflictos : util.parseServerResponse(result)}, ventanaId);

		bcsComunes.ventanaPromesa(resultId).then(function(){
			bcsComunes.ventanaOverlay(ventanaId,false);
			bcsComunes.ventanaData(ventanaId,true);
			bcsComunes.ventanaCerrar(ventanaId);	
		});

		bcsComunes.ventanaOverlay(ventanaId,false);
		document.getElementById('formModalClienteDocumentoSubir').reset();	
	};

	$scope.firmarAnexoPeru = function(idDocumento, idAnexo){
	 	var datosCrear = {};
		datosCrear.p_IdEmpresa			= $scope.$root.SesionIdEmpresa; 
		datosCrear.p_IdUsuario			= $scope.$root.SesionIdUsuario;
		datosCrear.p_IdModulo			= 'CLI';
		datosCrear.p_UsaReglasNegocio	= 'S';

		datosCrear.p_IdAnexo				= idAnexo;
		datosCrear.p_IdDocumento			= idDocumento;
		datosCrear.p_IdCliente = parametros.IdCliente;
		datosCrear.p_IdTipoDocumento = parametros.IdTipoDocumento;

		bcsServicioPlataforma.FirmarAnexo(datosCrear,function(result){
			$scope.respuestaServicio(result,$translate.instant((!!!result.P_OUT_ERRORESCONTADOR && result.P_OUT_ERRORESCONTADOR===0)?"UI_ARCHIVO_SUBIDO_CORRECTAMENTE":"UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"), ventanaId);
		},function(error){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
    		bcsComunes.ventanaOverlay(ventanaId,false);
    		document.getElementById('formModalClienteDocumentoSubir').reset();
		});
	};
	
	
});