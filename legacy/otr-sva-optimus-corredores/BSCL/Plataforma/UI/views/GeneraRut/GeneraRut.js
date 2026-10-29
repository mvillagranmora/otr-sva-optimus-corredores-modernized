/******************************
 * Utilitario para generar rut
 * author:JG
 ******************************/
(function() {
	angular.module('ModuloPlataforma').controller('GeneraRutCtrl',
	function($scope,bcsComunes,$translate,bcsAlerta,$window){
		//VARIABLES
		$scope.lista = [];
		var body = angular.element($window.document.body);
        var textarea = angular.element('<textarea/>');
        textarea.css({position: 'fixed',opacity: '0'});
        
        //FUNCIONES
		var randomRut = function(inCant){
		  if(isNaN(inCant) || inCant<1)return undefined;
		  var getDV = function(numero) {
		    nuevo_numero = numero.toString().split("").reverse().join("");
		    for(i=0,j=2,suma=0; i < nuevo_numero.length; i++, ((j==7) ? j=2 : j++)) {
		      suma += (parseInt(nuevo_numero.charAt(i)) * j); 
		    }
		    n_dv = 11 - (suma % 11);
		    return ((n_dv == 11) ? 0 : ((n_dv == 10) ? "K" : n_dv));
		  };
		  var min = 1;
		  var max = 99999999;
		  var salida=[]; 
		  for(pew=0;pew<inCant;pew++){
		    var nAux = Math.floor(Math.random() * (max - min + 1)) + min;
		    var dvAux = getDV(nAux);
		    objAux = {nro:nAux,dv:dvAux,rut:nAux+'-'+dvAux};
		    salida.push(objAux);
		  };
		  return salida;
		};
		
		$scope.winGenerarRut = function(cantidad){
			if(isNaN(cantidad) || cantidad<1)cantidad = 10; //default
			if(cantidad>100) alert("máximo 100 rut");
			$scope.lista = randomRut(cantidad);
		};
        
		$scope.winCopy = function(dato){
            textarea.val(dato);
            body.append(textarea);
            textarea[0].select();
            try {
                var successful = document.execCommand('copy');
                if (!successful) throw successful;
                bcsComunes.msg('info','rut copiado: ', dato);
            } catch (err) {
            	bcsComunes.msg('warning','error: ', 'no se pudo copiar el rut '+dato);
            	console.log("error copiar");
            }
            textarea.remove();
		}
		
		//GO
		$scope.winGenerarRut(10);		
	});
}());
