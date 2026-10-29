angular.module('ModuloPlataforma').controller('SubirContratoArchivoCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	$scope.recordData = {};
	$scope.recordData.estatico = true;
	$scope.configData = {};

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.recordData.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.recordData.IdUsuario	= $scope.SesionIdUsuario;
	$scope.recordData.IdModulo	= 'PLA';
	$scope.tipoClienteCmfEnabled = false;

	//Funcion para saber que tipo de persona es y asi determinar que tipo de cliente cmf mostrar
	$scope.$watch('recordData.tipoPersona', function(newval, oldval){
		
		var CodigoTipoIndicador = (newval == "N")?"TCLICMF":"TCLICMFJ";
		
		if(newval == "N" || newval == "J"){
			
			$scope.configData.filtroTipoClienteCMF = {
					filtros : {IdTipoIndicador : {field : "CodigoTipoIndicador", operator : "eq", value : CodigoTipoIndicador}}
				};
			$scope.tipoClienteCmfEnabled = true;
			
		}else{
			$scope.tipoClienteCmfEnabled = false;
			
			if($scope.recordData != undefined){
				$scope.recordData.tipoClienteCmf = undefined;
			}		
		}
		
	});
	
	$scope.subirArchivo = function(){
		
		var file = document.getElementById("data").files[0];//TODO validar que este seleccionado el archivo.
		if(file===undefined){
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
			return;
		}
		
		var extension = file.name.substr(file.name.lastIndexOf(".")+1);
		var tipoClienteCmf = ($scope.recordData.tipoClienteCmf != false) ? $scope.recordData.tipoClienteCmf : 0; 
		
		if(extension.toUpperCase()!="PDF"){
			$scope.recordData.estatico='N';
		}else{
			$scope.recordData.estatico=$scope.recordData.estatico === true ? 'S':'N';
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		var formData = new FormData(document.getElementById('formSubirArchivo'));
		formData.append("OP", 'CONTRACT');
		formData.append("idUsuario", $scope.recordData.IdUsuario);
		formData.append("idEmpresa", $scope.recordData.IdEmpresa);
		formData.append("modulo", $scope.recordData.IdModulo);
		formData.append("code", $scope.recordData.codigoArchivo);
		formData.append("desc", $scope.recordData.descripcion);
		formData.append("name", file.name);
		extension==="docx" ? formData.append("type", file.type.substr(0,19)) : formData.append("type", file.type);
		formData.append("type", file.type);
		formData.append("static", $scope.recordData.estatico);
		formData.append("ext", extension);
		
		formData.append("tipoPersona", $scope.recordData.tipoPersona);
		formData.append("tipoCliente", $scope.recordData.tipoCliente);
		formData.append("IdTipoClienteCmf", tipoClienteCmf);
		
		formData.append("estado", "ACT");
		
		$.ajax({
			method: "POST",
        	url: "/BSCL/Plataforma/GWJAVA/PLA/DatosContratosBin",
        	data: formData,
        	contentType : false,
        	processData: false,
        	cache: false
        }).done(function( msg ) {
        	if(msg==="OK"){
        		bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
        	}else{
        		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
        	}
        	bcsComunes.ventanaOverlay(idVentana,false);
        	document.getElementById('formSubirArchivo').reset();
        	bcsComunes.ventanaCerrar(idVentana);
    	}).error(function (jqXHR) {
    		console.log(jqXHR)
    		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
    		bcsComunes.ventanaOverlay(idVentana,false);
    		document.getElementById('formSubirArchivo').reset();
    		bcsComunes.ventanaCerrar(idVentana);
        });
	}
});