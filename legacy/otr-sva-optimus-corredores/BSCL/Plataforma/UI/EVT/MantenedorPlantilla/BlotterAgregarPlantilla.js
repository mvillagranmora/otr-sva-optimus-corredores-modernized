angular.module('ModuloPlataforma').controller('BlotterAgregarPlantillaCtrl',function($scope,bcsServicioPlataforma,bcsComunes,$timeout,$translate,$http){

	$scope.data = {texto : null};
	$scope.data = {descripcionaux : null};
	$scope.data = {Asunto : ''};
	$scope.data = {AsuntoAux : ''};

	$scope.seleccion = {};
	var camposAsociados;
	$scope.dataSourceEditorHtml  = [];
	$scope.$watch('data.texto', function(newValue,oldValue){
		 
 		if (newValue===oldValue) {  
		      return;
		    }    
 		$scope.html = htmlEncode($scope.data.texto);
		return;

	});
	$scope.$watch('data.descripcion', function(newValue,oldValue){
		 
 		if (newValue===oldValue) {
		      return;
		    }
 		$scope.data.descripcionaux = $scope.data.descripcion;
		return;

	});
	 
	
	$scope.dataSourceEditorHtml  = [];
	
	var entityMap = {
		    "<": "&lt;",
		    ">": "&gt;"
		  };

	function htmlEncode(string) {
		    return String(string).replace(/[<>]/g, function (s) {
		      return entityMap[s];
		    });
		  };
		  
	
});
   
angular.module('ModuloPlataforma' ).controller('AgregarPlantillaCtrl',function($rootScope,$scope,bcsServicioPlataforma,bcsComunes,$timeout,$translate,$timeout,$http,bcsAlerta){
	$scope.camposAsociados;
	var view = {};
	$scope.conf = {};
	$scope.filtros = {};
	var keyValue = [];
	var obj = {};
	var objTagDefault = {};
	var objFather = {};   
	var count = 0;
	var tagg = [];
	var tagpre = [];
	var idVentana = bcsComunes.ventanaWinId($scope); 

	$scope.readTemplate = function(){  
		$rootScope.$emit("CallParentTemplateModify", {});
	};
	
	var tagsDataSource = new  kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+1383,
				dataType: "json"
		    }
		},
  	});
	
	tagsDataSource.fetch(function(){
		 
		view = tagsDataSource.data();
		$scope.dataSourceEditorHtml = view;
 		
 	});
	
	// tags asociados a la conf
	var tagsDefaultDataSource_ = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: { 
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+1393,
				dataType: "json"
		    }
		},
		filter : {	field: "CodigoTipoEvento", operator: "neq", value:  'INGRESOORDEN'}
 	 	});
 
	
	
	var tagsDefaultDataSourceArray = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: { 
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+1393,
				dataType: "json"
		    }
		},
		filter : {	field: "CodigoTipoEvento", operator: "eq", value:  'INGRESOORDEN'}
 	 	});
 
	tagsDefaultDataSourceArray.fetch(function(){ 
		
		agregarTagyValorArray(tagsDefaultDataSourceArray.view());
		tagsDefaultDataSource_.fetch(function(){ 
			
	 		agregarTagyValor(tagsDefaultDataSource_.view());
			
		});
		
	});
	 
	
    function agregarTagyValor(ds){
  		obj = {};
 		var i = 0;
 		var total = ds.length;
 		var codigoevento = '';
 		var name = '';
 		
		$.each(ds, function(key,codigos){
 			   
			if(i===0){
				codigoevento = codigos.CodigoTipoEvento;
				name = codigos.property;
			}
			
				 
				
 				if(codigoevento===codigos.CodigoTipoEvento){ 
						
						obj[codigos.Llave] = codigos.ValorDefault;
				}else{
					  
					objTagDefault[name] = obj;
					obj = {};    
 					codigoevento = codigos.CodigoTipoEvento;
					name = codigos.property;
					obj[codigos.Llave] = codigos.ValorDefault;
				}
			 
					i++;
					if(i===total){
 						objTagDefault[name] = obj;
						obj = {};
   
					}
        });
		objFather["data"] = objTagDefault;
  		$scope.camposAsociados = JSON.stringify(objFather);
 		camposAsociados = $scope.camposAsociados;  
    }
		
		function agregarTagyValorArray(ds){
			objFather = {};
 			obj = {};
	 		var i = 0;
	 		var total = ds.length;
	 		var codigoevento = '';
	 		var name = '';
	 		
			$.each(ds, function(key,codigos){
	 			
				if(i===0){
					codigoevento = codigos.CodigoTipoEvento;
					name = codigos.property;
				}
	 				if(codigoevento===codigos.CodigoTipoEvento){ 
							console.log(codigos.Llave + "  "+  codigos.ValorDefault);
							obj[codigos.Llave] = codigos.ValorDefault;
					}else{
						  
						keyValue.push(obj);
						objTagDefault[name] = keyValue;
						obj = {};
	 					keyValue = [];
						codigoevento = codigos.CodigoTipoEvento;
						name = codigos.property;
						obj[codigos.Llave] = codigos.ValorDefault;
					}
				 
						i++;
						if(i===total){
							keyValue.push(obj);
							objTagDefault[name] = keyValue;
							obj = {};
	   
						}
	        });
		
		
		
		
    }	

  
	$scope.AgregarPlantilla = function() {  
		   
		 
        var datosPlantilla = {};
	        datosPlantilla.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	        datosPlantilla.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	        datosPlantilla.p_IdModulo = 'ORD'; //sacar de variables de sistema
	        datosPlantilla.P_Nombre =$scope.data.descripcion;
	        datosPlantilla.p_Asunto= $scope.data.Asunto;
			 if(typeof $scope.html ==='undefined'){
	      	  datosPlantilla.p_Template = '';

			 }else{
	      	  datosPlantilla.p_Template = $scope.html.replace(/<!--@(.*?)-->/g, '</@$1>');
			 }
			 
	        bcsServicioPlataforma.AgregarPlantilla(datosPlantilla,function(result)
		    {
		        	 console.log(JSON.stringify(result));
		        	
		        	 
		        	 if (result.P_OUT_ERRORESCONTADOR == "0")
				        {
							bcsComunes.msg("info","info",$translate.instant(result.P_SALIDA));
							//$scope.readTemplate();
							bcsComunes.ventanaCerrar(idVentana);

 				        }   
				        else
				        {
 				        	bcsComunes.setConflictos(util.parseServerResponse(result));
							bcsComunes.msg("error","error",$translate.instant(result.P_SALIDA));
 				        }
		        },
				function(result){
		        	datosPlantilla.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
				});
	        
	        };
	        $scope.$watch("filtros.codigoTipoEvento", function(newValue,oldValue){
	       	 
	    		if (newValue===oldValue || !newValue) {
	    		      return;
	    		    }
	    		if($scope.filtros.codigoTipoEvento) {
	    			
	    			$scope.tags = [];
	    			$scope.tags = view;
	    		}
	    		
	    	});
    
	$scope.evaluar = function(){
		
		
		if($scope.html===undefined){
			$scope.html = htmlEncode($scope.plantilla);
		}
 		var contexto = $scope.camposAsociados;
		var json = JSON.parse(contexto);
  
  	    var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
	    var datos = {
	  	  template:json.data.TagGenerales.Confirmar.replace('::URL::','#')+htmlDecode(($scope.html).replace(/<!--/g,'').replace(/-->/g,'').replace(/&lt;!--/g,'').replace(/--&gt;/g,'').replace(' ',' ')),
	      context: contexto
	    };
	    
	    var error = function(data, status, request) {
	      console.log("Imposible generar plantilla, error al conectar con servidor");
	      console.log('\tdata', data);
	      console.log('\tstatus', status);
	      console.log('\trequest', request);
	      alert("Imposible generar plantilla, error al conectar con servidor");
	    };
	    
		    $http({ 
		        method: 'POST',
		        url: url,
		        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		        transformRequest: function(obj) {
		            var str = [];
		            for(var p in obj)
		            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
		            return str.join("&");
		        },
		        data: datos
		    }).success(function (data) {
		    	var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
 		    	$timeout(function(){OpenWindow.init(data) ;},500); 
  		    	bcsComunes.msg('info', 'Procesar', 'OK');
  		    	$scope.conf.guardarContador = 1;

		    	
		    }).error(function (err){
		    	console.log(err);
	 	    	bcsComunes.msg('error', 'Procesar', err);
  		    	$scope.conf.guardarContador = 0;

		    });
	   
	};
	$scope.$watch("plantilla", function(newValue,oldValue){
		var editor = $("#editor").data("kendoEditor");
		if (newValue===oldValue) {
			
		      return;
		    }
  		$scope.data.texto = $scope.plantilla;
  	 	editor.value(htmlDecode($scope.plantilla));
  	 	 
 		return;
	});
	
	$scope.$watch("data.texto", function(newValue,oldValue){

  		if (newValue===oldValue) {
		      return;
		    }
 
		$scope.plantilla = htmlEncode($scope.data.texto);
		
 		var editor = $("#editor").data("kendoEditor");
 	 	editor.value(htmlDecode($scope.plantilla));
 		return;   
	});
	var entityMap = {
		    "<": "&lt;",
		    ">": "&gt;"
		  };

	function htmlEncode(string) {
		    return String(string).replace(/[<>]/g, function (s) {
		      return entityMap[s];
		    });
		  }
	function htmlDecode(input){
		  var e = document.createElement('div');
		  e.innerHTML = input;
		  return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
		};

});



angular.module('ModuloPlataforma').controller('AgregarPlantillaEditorHtmlCtrl',function($rootScope,$scope,bcsServicioPlataforma,bcsComunes,$timeout,$translate,$http){
	
	$scope.conf = {};
	$scope.filtros = {};
	var keyValue = [];
	var obj = {};
	var objTagDefault = {};
	$scope.html = '';
 	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	var view = {};
	$scope.tags =[];

	var tagsDataSource = new  kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+1383,
				dataType: "json"
		    }
		},
  	});
	
	tagsDataSource.fetch(function(){
		 
		view = tagsDataSource.data();
 	});
    $scope.mostrarEjemploPlantilla = function () {  
    	console.log('Data - texto ' + $scope.data.texto)
		console.log('Codigo Tipo Evento ' + $scope.filtros.codigoTipoEvento.CodigoTipoEvento);
    };
	$scope.AgregarPlantilla = function() {  
		   
		 
        var datosPlantilla = {};
	        datosPlantilla.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	        datosPlantilla.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	        datosPlantilla.p_IdModulo = 'ORD'; //sacar de variables de sistema
	        datosPlantilla.P_Nombre =$scope.data.descripcion;
	        datosPlantilla.p_Asunto= $scope.data.Asunto;

	       if(typeof $scope.html ==='undefined'){
	      	  datosPlantilla.p_Template = '';

			 }else{
	      	  datosPlantilla.p_Template = $scope.html.replace(/<!--@(.*?)-->/g, '</@$1>');
			 }
 
  	    
	        bcsServicioPlataforma.AgregarPlantilla(datosPlantilla,function(result)
		    {
		        	 console.log(JSON.stringify(result));
		        	
		        	 
		        	 if (result.P_OUT_ERRORESCONTADOR == "0")
				        {
							bcsComunes.msg("info","info",$translate.instant(result.P_SALIDA));
							//$scope.readTemplate();
							bcsComunes.ventanaCerrar(idVentana);
  
 				        }   
				        else
				        {
 				        	bcsComunes.setConflictos(util.parseServerResponse(result));
							bcsComunes.msg("error","error",$translate.instant(result.P_SALIDA));
 				        }
		        },
				function(result){
		        	datosPlantilla.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
				});
	         
	        };
	        $scope.$watch("filtros.codigoTipoEvento", function(newValue,oldValue){
	       	 
	    		if (newValue===oldValue || !newValue) {
	    		      return;
	    		    }
	    		if($scope.filtros.codigoTipoEvento) {
	    			
	    			$scope.tags = [];
	    			if (angular.isArray(view)) {
	    				$scope.tags = view;
	    			}
	    		}
	    		
	    	});
   
	        
	        
	$scope.$watch("seleccion",function(newValue,oldValue){
		
		$scope.filtros.codigoTipoEvento = $scope.seleccion;
		
	});
	
	$scope.$watch("filtros.codigoTipoEvento", function(newValue,oldValue){
	    		 
	    		if (newValue===oldValue) {
	    		      return;
	    		    }
	    		console.log($scope.filtros.codigoTipoEvento.CodigoTipoEvento);
	    		if($scope.filtros.codigoTipoEvento) {

	    			$scope.tags = [];
	    			if (angular.isArray(view)) {
	    				$scope.tags = view;
	    			}
	    		}
	    		
	    	});
	$scope.evaluar = function(){
		 
		
		 
		
		
		editor = $("#editor").data("kendoEditor");
		var contexto = camposAsociados;
		var json = JSON.parse(contexto);
		var tplt = htmlDecode($scope.html);
		tplt = tplt.replace(/<!--@(.*?)-->/g, '</@$1>');
		console.log((json.data.TagGenerales.Confirmar).replace('::URL::','#')+tplt);
  	    var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
	    var datos = {
	  	  template:(json.data.TagGenerales.Confirmar).replace('::URL::','#')+tplt,
	      context: contexto
	    };
	    
		    $http({ 
		        method: 'POST',
		        url: url,
		        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
		        transformRequest: function(obj) {
		            var str = [];
		            for(var p in obj)
		            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
		            return str.join("&");
		        },
		        data: datos
		    }).success(function (data) {
   		    	var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
		    	console.log(data);
		    	$timeout(function(){OpenWindow.init(data) ;},500); 
  		    	bcsComunes.msg('info', 'Procesar', 'OK');
  		    	$scope.conf.guardarContador = 1;

		    	
		    }).error(function (err){
	 	    	bcsComunes.msg('error', 'Procesar', err);
  		    	$scope.conf.guardarContador = 0;

		    });
	    
	};
	
	$scope.$watch("data.descripcionaux", function(newValue,oldValue){
		if (htmlEncode(newValue)===oldValue) {
		      return;
		    }
 		$scope.data.descripcion = $scope.data.descripcionaux;
		return;

	});
	
	$scope.$watch("data.texto", function(newValue,oldValue){
		 
		
		if (htmlEncode(newValue)===oldValue || $scope.data.texto===undefined) {
		      return;
		    }
		
		$scope.html = htmlDecode($scope.data.texto);
 

		return;

	});
	 
	$scope.lostfocus = function(){ 
		
		$scope.data.texto = $scope.html;
	};
	
	var entityMap = {
		    "<": "&lt;",
		    ">": "&gt;"
		  };

	function htmlEncode(string) {
		    return String(string).replace(/[<>]/g, function (s) {
		      return entityMap[s];
		    });
		  };
	
	function htmlDecode(input){
		  var e = document.createElement('div');
		  e.innerHTML = input;
		  return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
		};
	
	
	


	 
	
	
});