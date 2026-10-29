angular.module('ModuloPlataforma').controller('OperarImagenCtrl',function($scope,bcsComunes,$resource,$log,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.data = {};
	$scope.config= {};
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	
	$scope.start = function(){
		$scope.data = parametros;
		$scope.config.operacion = parametros.operacion;
		$scope.config.method = ($scope.config.operacion==="CREATE")?"POST" : "PUT";
		var OP = ($scope.config.operacion==="CREATE")?"Subir" : "Modificar";
		bcsComunes.ventanaTitulo(idVentana, $translate.instant(bcsComunes.ventanaGetTitulo(idVentana)).replace("XXX",OP));
		
	};
	
	$scope.upload = function(){
		var archivos = document.getElementById("data").files;
		if(archivos.length > 0) {
			if ($scope.data.RutaPublica && !$scope.urlEsValida($scope.data.RutaPublica)) {
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENSAJE_CONFIRMACION_IMAGEN_URL_INVALIDA"));
				t.result.then(function(result) {
					if (result === OPCIONES_BCSALERTA.ACEPTAR){
						$scope.uploadEjecutar(archivos);
					}
				});
			} else {
				$scope.uploadEjecutar(archivos);
			}			
		} else {
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
	};

	$scope.uploadEjecutar = function(archivos) {
		bcsComunes.ventanaOverlay(idVentana,true);
		var file = archivos[0]; //Files[0] = 1st file
		var formData = new FormData(document.getElementById("formImagen"));
		formData.append("idUsuario", $scope.SesionIdUsuario);
		formData.append("idEmpresa", $scope.SesionIdEmpresa);
		formData.append("modulo", "PLA");
		formData.append("name", $scope.data.NombreImagen);
		formData.append("version", $scope.data.Version);
		formData.append("type", file.type);
		formData.append("ext", file.name.substr(file.name.lastIndexOf(".")+1));
		formData.append("rutaPublica", $scope.data.RutaPublica ? $scope.data.RutaPublica : '');
			
			$.ajax({
				method: "POST",
	        	url: "/BSCL/Plataforma/GWJAVA/PLA/ImageManager",
	        	data: formData,
	        	contentType : false,
	        	processData: false,
	        	cache: false
	        }).done(function( msg ) {
	        	if(msg.result==="OK"){
	        		bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
	        	}else{
	        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
	        	}
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        	bcsComunes.ventanaCerrar(idVentana);
	    	}).error(function(msg){
	    		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
				bcsComunes.ventanaOverlay(idVentana,false);
	    	});
	};
	
	$scope.update = function(){
		var archivos = document.getElementById("data").files;
		if(archivos.length > 0) {
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENSAJE_CONFIRMACION_SUBIR_ARCHIVO"));
			t.result.then(function(result){
				if (result === OPCIONES_BCSALERTA.ACEPTAR){
					$scope.updateEjecutar(archivos);
				}
			});
		} else {
			if ($scope.data.RutaPublica && !$scope.urlEsValida($scope.data.RutaPublica)) {
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENSAJE_CONFIRMACION_IMAGEN_URL_INVALIDA"));
				t.result.then(function(result) {
					if (result === OPCIONES_BCSALERTA.ACEPTAR){
						$scope.updateEjecutar(archivos);
					}
				});
			} else {
				$scope.updateEjecutar(archivos);
			}
		}
	};

	$scope.updateEjecutar = function(archivos) {
		bcsComunes.ventanaOverlay(idVentana,true);
		var file = archivos[0]; //Files[0] = 1st file
		var formData = new FormData(document.getElementById("formImagen"));
		formData.append("idUsuario", $scope.SesionIdUsuario);
		formData.append("idEmpresa", $scope.SesionIdEmpresa);
		formData.append("modulo", "PLA");
		formData.append("id", $scope.data.IdImagen);
		formData.append("name", $scope.data.NombreImagen);
		formData.append("version", $scope.data.Version);
		formData.append("file", (file)?"S":"N");
		formData.append("type", (file)? file.type : $scope.data.Tipo);
		formData.append("ext", (file)?file.name.substr(file.name.lastIndexOf(".")+1) : $scope.data.Extension);
		formData.append("rutaPublica", $scope.data.RutaPublica ? $scope.data.RutaPublica : '');

		$.ajax({
			method: "POST",
			url: "/BSCL/Plataforma/GWJAVA/PLA/ImageManager",
			data: formData,
			contentType : false,
			processData: false,
			cache: false
		}).done(function( msg ) {
			if (msg.result==="OK") {
				bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
			} else {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
			}
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		}).error(function(msg) {
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	}

	$scope.urlEsValida = function(url) {
		try {
			const parsed = new URL(url);

			if (parsed.protocol !== "http:" && parsed.protocol !== "https:") {
			  return false;
			}
			const extensiones = [".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".svg"];
	
			return extensiones.some(ext => parsed.pathname.toLowerCase().endsWith(ext));
		} catch (_) {
		  return false;
		}
	  };	  
	
	$scope.cerrar = function () {
		//retorna undefined si el archivo no se cargo; en caso contrario, retorna un array de objetos y nombre del archivo.
		bcsComunes.ventanaData(idVentana,$scope.file);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.start();
});