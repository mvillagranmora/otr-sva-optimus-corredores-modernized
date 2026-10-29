angular.module('ModuloPlataforma').controller('UIPlaygroundCtrl', function($scope,$compile,bcsComunes,$timeout,$http,$interpolate,$filter){
	
	$scope.holi = function(msj) {
		alert(msj);
	}
	
	$scope.clicTest = function() {
		bcsComunes.ventanaAbrir('administrarPortafolio',{IdCliente:395174,IdPortafolio:343710},false,false)
	}
	
	$scope.opciones = [
			{ titulo : "POSTRERA", funcion : function() {alert('1');}},
			{ titulo : "PRECIOPROMEDIO", funcion : function() {alert('21');}},
			{ titulo : "MANUAL", funcion : function() {alert('3');}},
	]
	
	$scope.valores = {
			ESCRITAS : true,
			FAX : true,
			APLICACIONWEB : false
	};
	
	$scope.etiquetas = {
			ESCRITAS : "Escritas",
			FAX : "Fax",
			APLICACIONWEB : "Aplicaciones web"
	};
		
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	$scope.ctrl = {
			
	};
	$scope.ctrl = {};
	$scope.ctrl.address = {};
	$scope.ctrl.refreshAddresses = function(address) {
		if (!address) return;
	    return $http.post(
	      '/BSCL/Plataforma/GWJAVA/PLA/call?t=315',
	      {p_IdEmpresa: $scope.SesionIdEmpresa , p_FiltroTipoInstrumento : 'ACN', p_FiltroInstrumento : address}
	    ).then(function(response) {
	    	$scope.ctrl.addresses = response.data.P_OUT_INSTRUMENTO;
	    });
	  };
	  
	  $scope.ctrl.refreshTipoInstrumento = function(address) {
			return $http.get(
		      '/BSCL/Plataforma/GWJAVA/PLA/call?t=1570',
		      {}
		    ).then(function(response) {
		    	$scope.ctrl.addresses = $filter('unique')(response.data.d.results,'IdTipoInstrumento');
		    });
		  };
	
	$scope.filtrosPortafolio = {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
	};
	var iii = $interpolate;
	$scope.data = {};
	$scope.data.nemo1 = 'nemooo';
	$scope.data.cliente1 = 'clienteeeeee';
	$scope.data.portafolio1 = 'portafolioooo';
	$scope.data.cantidad1 = 'cantidaddddd';

	var verEnOtraVentana = function(url,contexto,atributos) {
		$http({
			  method: 'GET',
			  url: url
			}).then(function successCallback(response) {
				
				var htmlInterpolado = $interpolate(response.data)(contexto);
				myWindow = window.open(undefined,undefined,'menubar=no,location=yes,resizable=no,scrollbars=yes,status=yes');
				$(myWindow.document).find("body").html(htmlInterpolado);

			});
	}

	$scope.test = function() {
		verEnOtraVentana('../../Orden/UI/DetalleOrden/DetalleOrden.html',$scope.data,'menubar=no,location=yes,resizable=no,scrollbars=yes,status=yes');
		
	}
    
});
