angular.module('ModuloPlataforma').controller('ModificarArchivosContratosCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	$scope.data = {};
	$scope.parametros = {};

	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams(idVentana);
	
	$scope.data.CodigoArchivo =  $scope.parametros.CodigoArchivo;
	$scope.data.Descripcion =  $scope.parametros.Descripcion;
	$scope.data.EstadoDefault = $scope.parametros.IdEstado;
	
	$scope.data.idTipoPersona = $scope.parametros.IdTipoPersona;
	$scope.data.idTipoCliente = $scope.parametros.IdTipoCliente;
	$scope.data.idTipoClienteCMF = $scope.parametros.IdTipoClienteCMF;
	
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.estatico = ($scope.parametros.EsEstatico === "S") ? true : false;
	$scope.data.estaticoDefault =$scope.data.estatico;
	
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
				$scope.recordData.tipoClienteCMF = "TODOS";
			}			
		}
		
		
	})
	
	
	$scope.modificarArchivo = function(){
		
		var file = document.getElementById("data").files[0];//TODO validar que este seleccionado el archivo.
		bcsComunes.ventanaOverlay(idVentana,true);
		var formData = new FormData(document.getElementById('formModificarArchivo'));
		var tipoClienteCMF = ($scope.recordData.tipoClienteCMF != false) ? $scope.recordData.tipoClienteCMF : 0; 

		if(file){//si se selecciono un archivo
			var extension = file.name.substr(file.name.lastIndexOf(".")+1);
			if(extension.toUpperCase()!=$scope.parametros.TipoBinario.toUpperCase()){
				bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_INVALIDO"));
				return;
			}

			if(extension.toUpperCase()!="PDF"){
				$scope.data.estatico='N';
			}else{
				$scope.data.estatico=$scope.data.estaticoDefault === true ? 'S':'N';
			}
			
			formData.append("name", file.name);
			formData.append("type", file.type);
			
			if($scope.data.EsEstatico){
				formData.append("ext", "pdf");
			}else{
				formData.append("ext", file.name.substr(file.name.lastIndexOf(".")+1));
			};
		}
		else{
			$scope.data.estatico='N';
		}
		
		formData.append("OP", "PUT");
		formData.append("idUsuario", $scope.data.IdUsuario);
		formData.append("idEmpresa", $scope.data.IdEmpresa);
		formData.append("modulo", $scope.data.IdModulo);
		formData.append("initCode", $scope.parametros.CodigoArchivo);
		formData.append("template", $scope.parametros.IdTemplate);
		
				
		formData.append("code", $scope.data.CodigoArchivo);
		formData.append("desc", $scope.data.Descripcion);
		formData.append("static", $scope.data.estatico);
		formData.append("estado", $scope.data.Estado);

		formData.append("tipoPersona", $scope.recordData.tipoPersona);
		formData.append("tipoCliente", $scope.recordData.tipoCliente);
		formData.append("IdTipoClienteCmf", tipoClienteCMF);
		
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
        	bcsComunes.ventanaCerrar(idVentana);
    	}).error(function (jqXHR) {
    		console.log(jqXHR)
    		bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_SUBIDO_INCORRECTAMENTE"));
    		bcsComunes.ventanaOverlay(idVentana,false);
    		bcsComunes.ventanaCerrar(idVentana);
        });
        
	}
});