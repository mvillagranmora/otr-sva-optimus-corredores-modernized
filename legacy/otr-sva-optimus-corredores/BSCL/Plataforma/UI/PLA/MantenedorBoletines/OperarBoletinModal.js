angular.module('ModuloPlataforma').controller('OperarBoletinCtrl',function($scope,bcsComunes,$resource,$log,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.data = {};
	$scope.config= {};
	
	var idVentana = bcsComunes.ventanaWinId($scope); 
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
		if(archivos.length>0){
			
			bcsComunes.ventanaOverlay(idVentana,true);
			var file = archivos[0]; 

			var extension = file.name.substr(file.name.lastIndexOf(".")+1);

			if(extension.toUpperCase()!="PDF"){
				bcsComunes.msg("error",'Error',$translate.instant("UI_FORMATO_ARCHIVO_NO_VALIDO"));
				bcsComunes.ventanaOverlay(idVentana,false);
				return
			}

			var formData = new FormData(document.getElementById("formBoletin"));
			formData.append("modulo", "PLA");
			
			
			formData.append("OP", 'CONTRACT');
			formData.append("IdEmpresa", $scope.SesionIdEmpresa);
			formData.append("IdUsuario", $scope.SesionIdUsuario);
			formData.append("desc", $scope.data.Descripcion);
			formData.append("estado", "ACT");
			formData.append("name", file.name);
			formData.append("ext", extension);
			formData.append("type", file.type);

			
			
			$.ajax({
				method: "POST",
	        	url: "/BSCL/Plataforma/GWJAVA/PLA/Boletines",
	        	data: formData,
	        	contentType : false,
	        	processData: false,
	        	cache: false
	        }).done(function( msg ) {
	        	if(msg==="OK"){
	        		bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
					bcsComunes.ventanaCerrar(idVentana);
	        	}else{
	        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
	        	}
	        	bcsComunes.ventanaOverlay(idVentana,false);
        	}).error(function(msg){
        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
				bcsComunes.ventanaOverlay(idVentana,false);
        	});
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
	};
	
		
	
	$scope.cerrar = function () {
		//retorna undefined si el archivo no se cargo; en caso contrario, retorna un array de objetos y nombre del archivo.
		bcsComunes.ventanaData(idVentana,$scope.file);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.start();
});