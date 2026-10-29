angular.module('ModuloPlataforma').controller('OperarTemplateCtrl',function($scope,bcsComunes,$resource,$log,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	
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
		if(archivos.length>0){
			
			bcsComunes.ventanaOverlay(idVentana,true);
			var file = archivos[0]; //Files[0] = 1st file

			var formData = new FormData(document.getElementById("formTemplate"));
			formData.append("modulo", "PLA");
			
			formData.append("reportName", $scope.data.ReportName);
			formData.append("reportVersion", $scope.data.ReportVersion);
			
			$.ajax({
				method: "POST",
	        	url: "/BSCL/Plataforma/GWJAVA/PLA/report?upload",
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
        	}).error(function(msg){
        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
				bcsComunes.ventanaOverlay(idVentana,false);
        	});
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
	};
	
	$scope.update = function(){
		var archivos = document.getElementById("data").files;
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENSAJE_CONFIRMACION_SUBIR_ARCHIVO"));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				bcsComunes.ventanaOverlay(idVentana,true);
				var file = archivos[0]; //Files[0] = 1st file

				var formData = new FormData(document.getElementById("formTemplate"));
				formData.append("modulo", "PLA");

				formData.append("reportId", $scope.data.Id);
				formData.append("reportName", $scope.data.ReportName);
				formData.append("reportVersion", $scope.data.ReportVersion);
				
				$.ajax({
					method: "POST",
					url: "/BSCL/Plataforma/GWJAVA/PLA/report?update",
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
				}).error(function(msg){
					bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
					bcsComunes.ventanaOverlay(idVentana,false);
				});

			}
		});
	};
		
	
	$scope.cerrar = function () {
		//retorna undefined si el archivo no se cargo; en caso contrario, retorna un array de objetos y nombre del archivo.
		bcsComunes.ventanaData(idVentana,$scope.file);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.start();
});