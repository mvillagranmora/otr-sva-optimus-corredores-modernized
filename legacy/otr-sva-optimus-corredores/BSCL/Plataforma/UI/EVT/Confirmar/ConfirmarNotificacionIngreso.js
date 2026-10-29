angular.module('confirmar', ['ui.bootstrap', 'ui.router','ngResource' ,'kendo.directives']);
		angular.module('confirmar').run(function($rootScope) {		
		    $rootScope.safeApply = function(fn) {
		        var phase = $rootScope.$$phase;
		        if (phase === '$apply' || phase === '$digest') {
		            if (fn && (typeof(fn) === 'function')) {
		                fn();
		            }
		        } else {
		            this.$apply(fn);
		        }
		    };		
		});

angular.module('confirmar', ['ui.bootstrap', 'ui.router','ngResource' ,'kendo.directives']).directive('template', function($compile){
	
	return{
        link: function(scope, element){
            var template = "<button ng-click='doSomething()'>{{label}}</button>";
            var linkFn = $compile(template);
            var content = linkFn(scope);
            element.append(content);
        }
    }
});
angular.module('confirmar').factory('bcsServicioConfirmar',['$resource',function($resource) {
			var urlValidarConfirmarNotificacion = "/BSCL/Plataforma/XSREST/ValidaConfirmarNotificacion.xsjs";
	 		return $resource("", {},
	 		{
	  			'ValidarTokenNotificacion': { method: "POST", url: urlValidarConfirmarNotificacion},      
	   		});
}]);


 

angular.module('confirmar').controller('ConfirmarCtrl',function($scope,$window,bcsServicioConfirmar,$http){			
			
	$scope.tokenResp;
	$scope.rutResp;
	$scope.alerts = [ ];
	$scope.data = {};
	var token ;
	token = get('token');
	var r ;
	
	$(document).keypress(function (e) {
	    if (e.which == 13) {
	    	 $scope.ValidarConfirmarNotificacion();
	    }
	});
			 
	$scope.CerrarAlerta = function(index) {
		    	$scope.alerts.splice(index, 1);
		  	};	  	
		  	
	function get(name){
		  	   if(name=(new RegExp('[?&]'+encodeURIComponent(name)+'=([^&]*)')).exec(location.search))
  	      return decodeURIComponent(name[1]);
  	};
	$scope.ValidarConfirmarNotificacion = function()
	{
		var p_rut;

		if($scope.p_Rut){
			p_rut =$scope.p_Rut; 
		}else{
			$scope.alerts.push({msg: 'Debe ingresar código'});
			return;
		}
		token = angular.element('#p_token').val();
		var url = "/BSCL/Plataforma/GWJAVA/PORTAL/validate?";
	    var data = {
	      rut: p_rut,
	      token: token
	    };   
		$http({ 
	        method: 'POST',
	        url: url,    
	        headers: {
	        	'Content-Type': 'application/x-www-form-urlencoded',
	        	'X-SESSION-TOKEN': 'MTplZXNwaW5vemE6NTc4ODJEM0VGRDFFMEYxQkUxMDAwMDAwN0YwMDAwMDI='
	        		},
	        params: data
	    }).success(function (data) {  
	    	
	    		var array = data.split("||");
	    		var template = array[0];
	    		var displayObs = array[1];   
	    		var displayLogRut = array[2];
	    		var iframe = document.createElement('iframe');
	    		
	    		var html = template;
	    		iframe.src = 'data:text/html;charset=utf-8,' + encodeURI(html);
	    		iframe.width="100%";
	    		iframe.height="100%"; 
	    		iframe.scrolling="no";
	    		
 	    		
	    		
	    		document.getElementById('template').appendChild(iframe);
	    		document.getElementById('rut').innerHTML = array[3];
	    		document.getElementById('token').innerHTML = array[4];
	    		
	    		
	    		console.log(array[3]);
	    		console.log(array[4]);
	    		if(displayObs==1){
	    			document.getElementById('BGObservacion').style.display = 'block';
	    		}
	    		if(displayLogRut==0){
	    			
	    			document.getElementById('BGConfirmar').style.display = 'none';
	    			
	    		}

	    }).error(function (err){
	    	$scope.alerta = {msg : 'Código incorrecto'};
 	    	$scope.alerts.push({msg: 'Código Incorrecto'});
 	    });	

	};
      

  	$scope.ValidarRechazaNotificacion = function()
	{
		
		if($scope.p_comentarios==undefined){
			$scope.alerts.push({msg: 'Observación no puede ser vacío'});
			console.log($scope.alerts.msg);
			return;
		}else{
		//http://localhost:8086/ValidateNotifications/rest/api/val/doPost
		///BSCL/Plataforma/GWJAVA/PORTAL/validate?
		var url = "/BSCL/Plataforma/GWJAVA/PORTAL/validate?";
	    var datos = {
	    		 rut:  document.getElementById("p_rut").value,
			  	  token:  document.getElementById("p_token").value,
			  	observacion : $scope.p_comentarios,
	  	      accion : 2
	  	      
	  	    };   
	  		$http({ 
	  	        method: 'POST',
	  	        url: url,
	  	        headers: {
	  	        	'Content-Type': 'application/x-www-form-urlencoded',
	  	        	'X-SESSION-TOKEN': 'MTplZXNwaW5vemE6NTc4ODJEM0VGRDFFMEYxQkUxMDAwMDAwN0YwMDAwMDI='
	  	        		},
	  	        params: datos
	  	    }).success(function (data) {        
	  	    	 document.getElementById('BGObservacion').style.display = 'none';
	  	    	 document.getElementById('BGRechazo').style.display = 'block';  
	  	    }).error(function (err){
	  	    	document.getElementById('BGObservacion').style.display = 'none';
	  	    	document.getElementById('BGNoDisponible').style.display = 'block';
	  	    });	
		}
		 
		
		
		 
	};		   
	
	$scope.ConfirmaNotificacion = function()
	{
		if($scope.p_comentarios==undefined){
			 
			$scope.alerts.push({msg: 'Observación no puede ser vacío'});
 			console.log($scope.alerts[0].msg);
			return;
		}else{
		
		var url = "/BSCL/Plataforma/GWJAVA/PORTAL/validate?";
	    var datos = {
	    	  rut:  document.getElementById("p_rut").value,
		  	  token:  document.getElementById("p_token").value,
		  	observacion : $scope.p_comentarios,  
	  	      accion : 1
	  	      
	  	    };   
	  		$http({ 
	  	        method: 'POST',
	  	        url: url,
	  	        headers: {
	  	        	'Content-Type': 'application/x-www-form-urlencoded',
	  	        	'X-SESSION-TOKEN': 'MTplZXNwaW5vemE6NTc4ODJEM0VGRDFFMEYxQkUxMDAwMDAwN0YwMDAwMDI='
	  	        		},
	  	        params: datos
	  	    }).success(function (data) {        
	  	    	document.getElementById('BGObservacion').style.display = 'none';
	  	    	document.getElementById('BGConfirma').style.display = 'block';
	  	    }).error(function (err){
	  	    	document.getElementById('BGObservacion').style.display = 'none';
	  	    	document.getElementById('BGNoDisponible').style.display = 'block';  
 	  	    });	
		}
	};			
	//Cierra el progreso de la ventana
});		