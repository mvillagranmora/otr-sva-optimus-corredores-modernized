angular.module('ModuloPlataforma',["ngSanitize"]).controller('BlotterModificarPlantillaCtrl',function($rootScope,$scope,bcsServicioPlataforma,bcsComunes,$timeout,$translate){
	
	$scope.select = function(grid)
	{
		$timeout(function() {
			if ($("#" + grid).find("[kendo-grid]").data("kendo-grid"))
				$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
		},50);
	};
	$scope.data = {texto : null};
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
	
	var entityMap = {
		    "<": "&lt;",
		    ">": "&gt;"
		  };

	function htmlEncode(string) {
		    return String(string).replace(/[<>]/g, function (s) {
		      return entityMap[s];
		    });
		  }
	
	
	
	 
});

angular.module('ModuloPlataforma').controller('ModificarPlantillaCtrl',function($rootScope,$scope,$http,bcsServicioPlataforma,bcsComunes,$timeout,$translate){
	
	$scope.conf = {};
	$scope.filtros = {};
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.datosPlantilla = {Plantilla : parametros.Plantilla};
	$scope.conf = {Plantilla : parametros.Plantilla};
	$scope.data.descripcion = $scope.datosPlantilla.Plantilla.Nombre; 
	$scope.data.Asunto = $scope.datosPlantilla.Plantilla.Asunto; 
	$scope.data.texto = $scope.datosPlantilla.Plantilla.Template.replace(/!!}/g,'!}');
	//$scope.plantilla = $scope.data.texto; 
  	var keyValue = [];
	var obj = {};
	var view = {};
	$scope.tags =[];
	$scope.aux ;
	var objTagDefault = {};
	var objFather = {};   
	
	$scope.readTemplate = function(){  
		$rootScope.$emit("CallParentTemplateModify", {});
	};
	// tags predeterminados combo
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
	
	 
	
	var tagsDefaultDataSource_ = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: { 
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t="+1393,
				dataType: "json"
		    }
		},
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

	function replaceFreeMarke(input){
		
		var text = input.replace(/&lt;#list(.*)&gt;/g,'<!--<#list $1>-->');
  		text = text.replace(/\${(.*)}/g,'<!--${$1}-->');
 		return text;
		
		
	}
	function replaceFromEditor(input){
		
		var text = input.replace(/&lt;!--&lt;#list(.*)&gt;--&gt;/g,'<#list $1>');
  		text = text.replace(/&lt;!--\${(.*)}--&gt;/g,'${$1}');
 		return text;
		
		
	}

	function htmlDecode(input){
		  var e = document.createElement('div');
		  e.innerHTML = input;
		  return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;
		};
	$scope.EditarPlantilla = function() {   
		    
		 
        var datosPlantilla = {};
	        datosPlantilla.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	        datosPlantilla.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	        datosPlantilla.p_IdModulo = 'ORD'; //sacar de variables de sistema
	        datosPlantilla.p_IdPlantilla =$scope.datosPlantilla.Plantilla.IdPlantilla;
	        datosPlantilla.p_Template = $scope.html.replace(/<!--@(.*?)-->/g, '</@$1>');

	        //datosPlantilla.p_Template = datosPlantilla.p_Template.replace(/}/g,'!}');
	        datosPlantilla.P_Nombre =$scope.data.descripcion;  
	        datosPlantilla.p_Asunto= $scope.data.Asunto;

	        bcsServicioPlataforma.ActualizarPlantilla(datosPlantilla,function(result)
		    {
 		        	
		        	 
		        	 if (result.P_OUT_ERRORESCONTADOR == "0"){
	 
							$scope.readTemplate();
							bcsComunes.msg('info', 'Actualizar', result.P_SALIDA);
							bcsComunes.ventanaCerrar(idVentana);

							
				        }else{
				        	bcsComunes.msg('error', 'Actualizar', result.P_SALIDA);
				        }
		        	 
		        	 
		        },
				function(result){
		        	datosPlantilla.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
				});
	
	};
	          
	
	
	 
	$scope.$watch("filtros.codigoTipoEvento", function(newValue,oldValue){
	    		 
 	    		if (newValue===oldValue) {
	    		      return;
	    		    }
	    		if($scope.filtros.codigoTipoEvento) {
	    			
	    			$scope.tags = [];
	    			$scope.tags = view;

	    		}
	    		
	});  
	    
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

   		if (newValue===oldValue && $scope.plantilla !== undefined) {
		      return;
		    }

		$scope.plantilla = htmlEncode($scope.data.texto);
		
 		var editor = $("#editor").data("kendoEditor");
 	 	editor.value(htmlDecode($scope.plantilla));
 		return;   
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
		    	data.replace(/\"/g,'');
		    	var OpenWindow = window.open('/BSCL/Plataforma/UI/EVT/MantenedorPlantilla/MantenedorPlantillaEjemplo.html', 'popup', 'width=900,height=1200');
 		    	$timeout(function(){OpenWindow.init(data.replace(/\"/g,'')) ;},600);  
  		    	bcsComunes.msg('info', 'Procesar', 'OK');
   		    	
 		    	
		    }).error(function (err){
	 	    	bcsComunes.msg('error', 'Procesar', 'Error al procesar template, asegurese que no contenga errores');
 		    });
	
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
 
						 
						
 });



angular.module('ModuloPlataforma').controller('EditorVisualCtrl',function($rootScope,$scope,$http,bcsServicioPlataforma,bcsComunes,$timeout,$translate){
	
	
	$scope.conf = {};
	$scope.filtros = {};
	$scope.foco = 0;
	var keyValue = [];  
	var obj = {};
	var idVentana = bcsComunes.ventanaWinId($scope); 
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.datosPlantilla = {Plantilla : parametros.Plantilla};
	$scope.conf = {Plantilla : parametros.Plantilla};
	$scope.data.descripcion = $scope.datosPlantilla.Plantilla.Nombre; 
	$scope.data.Asunto = $scope.datosPlantilla.Plantilla.Asunto; 
 	
	$scope.readTemplate = function(){        
		$rootScope.$emit("CallParentTemplateModify", {});
	};
 

	var view = [];
	
	var tagsDataSource = new  kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1383",
				dataType: "json"
		    }
		},
  	});
	
	tagsDataSource.fetch(function(){
		 
		view = tagsDataSource.data();
  		
 	});

    $scope.EditarPlantilla = function() {  
 		 
        var datosPlantilla = {};
	        datosPlantilla.p_IdEmpresa = $scope.SesionIdEmpresa; //saca de variables de entorno
	        datosPlantilla.p_IdUsuario = $scope.SesionIdUsuario; //saca de variables de entorno
	        datosPlantilla.p_IdModulo = 'ORD'; //sacar de variables de sistema
	        datosPlantilla.p_IdPlantilla =$scope.datosPlantilla.Plantilla.IdPlantilla;
	        datosPlantilla.P_Nombre =$scope.data.descripcion;
	        datosPlantilla.p_Asunto= $scope.data.Asunto;
	        datosPlantilla.p_Template = $scope.html.replace(/<!--@(.*?)-->/g, '</@$1>');    
 	        
 

	        bcsServicioPlataforma.ActualizarPlantilla(datosPlantilla,function(result)
		    {
 		        	
	        	 	if (result.P_OUT_ERRORESCONTADOR == "0"){
	        	 		bcsComunes.msg('info', 'Modificar', result.P_SALIDA);
						$scope.readTemplate();
						bcsComunes.ventanaCerrar(idVentana);

						
			        } else{
				        
						bcsComunes.msg("error","error",$translate.instant(result.P_SALIDA));
	 		        	bcsComunes.ventanaCerrar(idVentana);
			        }
		        	 
		        },
				function(result){
		        	datosPlantilla.message = 'UI_ACTUALIZACION_INCORRECTA_LIBRO';
				});
	};
	        
	        
	$scope.$watch("seleccion",function(newValue,oldValue){
		
		$scope.filtros.codigoTipoEvento = $scope.seleccion;
		
	});
	
	$scope.$watch("filtros.codigoTipoEvento", function(newValue,oldValue){
	    		 
	    		if (newValue===oldValue) {
	    		      return;
	    		    }
	    		if($scope.filtros.codigoTipoEvento) {
	    			
	    			 $scope.tags = [];
	    			 $scope.tags = view;
	     		 
	    		}
	    		
	    	});
	    	
	 
	
	 

	$scope.evaluar = function(){
		editor = $("#editor").data("kendoEditor");
		
		var contexto = camposAsociados;

		var json = JSON.parse(contexto);
		var templ = $scope.html.replace(/<!--@(.*?)-->/g, '</@$1>');
		
	    var url = "/BSCL/Plataforma/GWJAVA/PLA/template?";
	    var datos = {
	      template:  (json.data.TagGenerales.Confirmar).replace('::URL::','#')+templ, 
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
		    	 $timeout(function(){OpenWindow.init(data);},600);
  		    	 bcsComunes.msg('info', 'Procesar', 'OK');
  		    	 $scope.conf.guardarContador = 1;

		    	
		    }).error(function (err){
	 	    	bcsComunes.msg('error', 'Procesar', err);
	 	    	$scope.conf.guardarContador = 0;
		    });
	    
	};
	
	$scope.$watch("data.texto", function(newValue,oldValue){
		 
 		if (htmlEncode(newValue)===oldValue &&  htmlDecode($scope.data.texto) === $scope.html ) {
 			 
		      return;
		    }
		//console.log(htmlDecode($scope.data.texto));
		$scope.html = htmlDecode(htmlEncode($scope.data.texto));
 

		return;

	});

	function replaceFreeMarke(input){
		
		var text = input.replace(/&lt;#list.*&gt;/g,' ');
 		text = text.replace(/&lt;\/#list.*&gt;/g,' ')
 		text = text.replace(/\${.*}/g,' ');
 		return text;
		
		
	}
 
	
	$scope.lostfocus = function(){
		if(!(typeof $scope.data.texto === 'undefined'))
		$scope.data.texto = $scope.html;
	};
	/*
	$scope.$watch("html", function(newValue,oldValue){
		 
		
		if (htmlEncode(newValue)===htmlEncode(oldValue)) {
		      return;
		    }
   		$scope.data.texto = $scope.html;
		return;

	});*/
	
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