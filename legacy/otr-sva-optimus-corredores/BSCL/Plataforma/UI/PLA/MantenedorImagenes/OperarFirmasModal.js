angular.module('ModuloPlataforma').controller('OperarFirmasCtrl',function($scope,bcsServicioPlataforma,bcsComunes,$resource,$log,$translate,bcsAlerta,OPCIONES_BCSALERTA){
	
	$scope.data = {};
	$scope.config= {};
	
	document.getElementById("b64").style.display = "none";
	var idVentana 	= bcsComunes.ventanaWinId($scope); 
	var parametros 	= bcsComunes.ventanaParams(idVentana);
	$scope.config.operacion="";
	$scope.data = parametros;
	$scope.ImagenFirma;
	
	function start() {
		$scope.ImagenFirma ="GGWP";
	}

	function readFile() {
		  
		  if (this.files && this.files[0]) {
		    
		    var FR= new FileReader();
		    
		    FR.addEventListener("load", function(e) {
		      document.getElementById("img").src       	= e.target.result;
		      document.getElementById("b64").value 		= e.target.result;
		    }); 
		    
		    FR.readAsDataURL( this.files[0] );
		  }
		  
		}
	document.getElementById("inp").addEventListener("change", readFile);
	
	$scope.upload = function(){

		var parametros = {};
		var archivos = document.getElementById("inp").files;
		var file = archivos[0];
		var data;
		
		
		if(archivos.length>0 || $scope.config.operacion=="DELETE"){
			var name;
			var file;
			var type;
			var ext;
			var data;
			var extension;
			
			if($scope.config.operacion=="DELETE"){
				name="eliminar";
				fileimg="N";
				type="image";
				ext	="png";
				data="asdczxc23";
				extension="png";
			}else{
				name=file.name;
				fileimg=(file)? file.type : $scope.data.Tipo;
				type=(file)? file.type : $scope.data.Tipo;
				ext	= file.name.substr(file.name.lastIndexOf(".")+1);
				data=document.getElementById("b64").value;
				extension= (file.name.substr(file.name.lastIndexOf(".")+1));
			}
			
			bcsComunes.ventanaOverlay(idVentana,true);
			
			parametros.idUsuario   	= $scope.SesionIdUsuario;
			parametros.idEmpresa   	= $scope.SesionIdEmpresa;
			parametros.idDocumento 	= $scope.data.IdDocumentoIdentidad;
			parametros.idPersona  	= $scope.data.IdPersona;
			parametros.name			= name;
			parametros.version		= 1;
			parametros.file			= fileimg;
			parametros.type			= type;
			parametros.ext			= ext;
			parametros.data 		= data;
			parametros.operacion	= $scope.config.operacion;
			
			
			var extensionvalida= false;
			switch (extension) {
			  case 'jpg':
				  extensionvalida= true;
			    break;
			  case 'png':
				  extensionvalida= true;
			    break;
			  case 'jpeg':
				  extensionvalida= true;
			    break;
			}
			if(extensionvalida){
			
				bcsServicioPlataforma.Servicios().CargaImagenesFirmas(parametros).$promise.then(function(respuesta){
			     	 console.log(respuesta);
			     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		 
			     		 
			     		 if(respuesta.P_OUT_MENSAJES == "UI_FIRMACREADA"){
			     			 
			     		bcsComunes.msg("info",'Info',$translate.instant("UI_ARCHIVO_SUBIDO_CORRECTAMENTE"));
			     		bcsComunes.ventanaOverlay(idVentana,false)
			     		
			     		 }else if (respuesta.P_OUT_MENSAJES == "UI_FIRMAMODIFICADA"){
			     			bcsComunes.msg("info",'Info',$translate.instant("UI_MODFIRMA"));
				     		bcsComunes.ventanaOverlay(idVentana,false)
			     		 }else if (respuesta.P_OUT_MENSAJES == "UI_FIRMAELIMINADA"){
			     			document.getElementById("img").src= "";
			     			bcsComunes.msg("info",'Info',$translate.instant("UI_DELFIRMA"));
				     		bcsComunes.ventanaOverlay(idVentana,false)
			     		 }
			     	 }else if (respuesta.P_OUT_ERRORESCONTADOR === 1) {
			     		 
			     		 if(respuesta.P_OUT_MENSAJES == "UI_FIRMANOEXISTE"){
				     		bcsComunes.msg("error",'Error',$translate.instant("UI_NOFIRMA"));
							bcsComunes.ventanaOverlay(idVentana,false);
			     			 
			     		 }else if (respuesta.P_OUT_MENSAJES == "UI_FIRMAEXISTE"){
			     			bcsComunes.msg("error",'Error',$translate.instant("UI_FIRMAEXIS"));
							bcsComunes.ventanaOverlay(idVentana,false);
			     		 }
			     	 }else{
			     		bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_PROCESAR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
			     		bcsComunes.ventanaOverlay(idVentana,false);
			     	 }
		   		 },function(respuesta){
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_PROCESAR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
		   			bcsComunes.ventanaOverlay(idVentana,false);
		   		 });
				
			}else{
				bcsComunes.msg("error",'Error',$translate.instant("UI_EXTENSIONINVALIDA"));
				bcsComunes.ventanaOverlay(idVentana,false);
			}
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
	}
	$scope.updatefirma = function(){$scope.config.operacion="UPDATE";$scope.upload();}
	$scope.deletefirma = function(){$scope.config.operacion="DELETE";$scope.upload();}
	$scope.uploadfirma = function(){$scope.config.operacion="CREATE";$scope.upload();}
	
	$scope.cerrar = function () {
		bcsComunes.ventanaData(idVentana,$scope.file);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.$watch('ImagenFirma',function(newval,oldval) {
		var parametrosSelect = {};
		parametrosSelect.idUsuario   	= $scope.SesionIdUsuario;
		parametrosSelect.idEmpresa   	= $scope.SesionIdEmpresa;
		parametrosSelect.idDocumento 	= $scope.data.IdDocumentoIdentidad;
		parametrosSelect.idPersona  	= $scope.data.IdPersona;
		parametrosSelect.name			= "";
		parametrosSelect.version		= 0;
		parametrosSelect.file			= "";
		parametrosSelect.type			= "";
		parametrosSelect.ext			= "";
		parametrosSelect.data 			= "";
		parametrosSelect.operacion		= "SELECT";
			
		bcsServicioPlataforma.Servicios().CargaImagenesFirmas(parametrosSelect).$promise.then(function(respuesta){
	     	 if (respuesta.P_OUT_ERRORESCONTADOR === 0 && respuesta.P_OUT_IMAGEN!=="" && respuesta.P_OUT_IMAGEN!== null){ 		 
	     		document.getElementById("img").src =respuesta.P_OUT_IMAGEN;
	     		 
	     		 }
  		 },function(respuesta){});
	
	});
	
	start();
	
});