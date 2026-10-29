angular.module('ModuloPlataforma').directive('bcsWebsocket', function($timeout, $rootScope) {
	return {
		restrict: 'E',
		transclude: false,
		replace: false,
		scope: {
		    bcsMsg  : '&?'  				/* Funciona para dejar mensajes fn(ele)  */
		},
		template: '<!-- WEBSOCKET -->',
		link: function(scope, elem, attrs) {
			
			scope.close=false;
			
			if (scope.bcsMsg === undefined) {
				console.warn("Websocket: funcion bcs-msg no definida");
			}
			
			scope.safeExec = function (fn) {
				if($rootScope.$$phase){
					fn();
				} else {
					$rootScope.$applyAsync(fn);
				}
			}
			
			scope.start = function() {
				var url = window.location.href.split('/')[2];
				var httpType = window.location.href.split('/')[0];
				var ws;
				if (httpType === 'http:') {
					ws = "ws://"+url+"/BSCL/Plataforma/GWWS/PLA/CallWebsocket";
				}
				if (httpType === 'https:') {
					 ws = "wss://"+url+"/BSCL/Plataforma/GWWS/PLA/CallWebsocket";
				}
				if (!ws) {
					//no partio el websocket
					return null;
				}
				var webSocket = new WebSocket(ws);
				
				//Al abrir enviar KEY
				webSocket.onopen = function(message){ 
					console.log("Websocket: Comunicacion iniciada");
					webSocket.send("X-SESSION-TOKEN:"+sessionStorage.Token);
				};
				
				//Mensaje Recibido
				webSocket.onmessage = function(message){
					var data={};
					try { data=JSON.parse(message.data); }
					catch(err) {}
					if (data!== undefined && data.Evento !== undefined && data.DATA !== undefined) {
						scope.safeExec(function() {
							scope.bcsMsg({ message: data });
						});
					}
				};
				
				//Conexion cerrada
				webSocket.onclose = function(message){ 
					if (scope.close==false) {
						console.warn("Websocket: Comunicacion perdida. Reintento en 5 segs.");
						$timeout(function() {
							scope.ws=scope.start();
						},5000);
					}
				};					
				return webSocket;
			};
			
			//Inicia Websocket
			scope.ws=scope.start();
			
			if (scope.ws) {
				//Close WebSocoket on destroy
				scope.$on('$destroy', function() {
					console.log("Websocket: destruido por usuario");
					scope.close=true;
					scope.ws.close();      
			    });	
			}
			
	}
  };
});
