angular.module('ModuloPlataforma').controller('AnexoCtrl',function($scope,$translate, bcsComunes, bcsServicioPlataforma){
	
	var ventanaId = bcsComunes.ventanaWinId($scope);
	dato = bcsComunes.ventanaParams (ventanaId);
	
	$scope.data = {}
	$scope.data = dato;
	
	
	//Carga datos iniciales 
	
	$scope.data.bcsHabilitado1 = $scope.data.IdUsuarioEmpresa1 === $scope.$root.SesionIdUsuario ? true : false;
	$scope.data.bcsHabilitado2 = $scope.data.IdUsuarioEmpresa2 === $scope.$root.SesionIdUsuario ? true : false;
	$scope.data.bcsHabilitado3 = $scope.data.IdUsuarioEmpresa3 === $scope.$root.SesionIdUsuario ? true : false;
	$scope.data.bcsHabilitado4 = $scope.data.IdUsuarioCliente1 === $scope.$root.SesionIdUsuario ? true : false;
	$scope.data.bcsHabilitado5 = $scope.data.IdUsuarioCliente2 === $scope.$root.SesionIdUsuario ? true : false;
	$scope.data.bcsHabilitado6 = $scope.data.IdUsuarioCliente3 === $scope.$root.SesionIdUsuario ? true : false;
	
	$scope.data.swFirmaEmpresa1 = $scope.data.FirmaEmpresa1 == 'S' ? true : false;
	$scope.data.swFirmaEmpresa2 = $scope.data.FirmaEmpresa2 == 'S' ? true : false;
	$scope.data.swFirmaEmpresa3 = $scope.data.FirmaEmpresa3 == 'S' ? true : false;
	$scope.data.swFirmaCliente1 = $scope.data.FirmaCliente1 == 'S' ? true : false;
	$scope.data.swFirmaCliente2 = $scope.data.FirmaCliente2 == 'S' ? true : false;
	$scope.data.swFirmaCliente3 = $scope.data.FirmaCliente3 == 'S' ? true : false;
	
	$scope.data.mostrarEmpresa1 = false;
	$scope.data.mostrarEmpresa2 = false;
	$scope.data.mostrarEmpresa3 = false;
	$scope.data.mostrarCliente1 = false;
	$scope.data.mostrarCliente2 = false;
	$scope.data.mostrarCliente3 = false;
	
	if($scope.data.NumeroFirmantesEmpresa == 1)
	{
		$scope.data.mostrarEmpresa1 = true;
	}
	else if($scope.data.NumeroFirmantesEmpresa == 2)
	{
		$scope.data.mostrarEmpresa1 = true;
		$scope.data.mostrarEmpresa2 = true;
	}
	else if($scope.data.NumeroFirmantesEmpresa == 3)
	{
		$scope.data.mostrarEmpresa1 = true;
		$scope.data.mostrarEmpresa2 = true;
		$scope.data.mostrarEmpresa3 = true;
	}
	
	if($scope.data.NumeroFirmantesCliente == 1)
	{
		$scope.data.mostrarCliente1 = true;
	}
	else if($scope.data.NumeroFirmantesCliente == 2)
	{
		$scope.data.mostrarCliente1 = true;
		$scope.data.mostrarCliente2 = true;
	}
	else if($scope.data.NumeroFirmantesCliente == 3)
	{
		$scope.data.mostrarCliente1 = true;
		$scope.data.mostrarCliente2 = true;
		$scope.data.mostrarCliente3 = true;
	}
	
	$scope.cerrar = function () {
		bcsComunes.ventanaCerrar(ventanaId); 
	};
	
	//////////////////////////////////////////////////////////////////
	
	var valorFirma;
	var campoFirma;
	
	$scope.$watch('data.swFirmaEmpresa1',function(newval,oldval){
		console.log("data FirmaEmpresa1 " +oldval+ " " +newval);
		valorFirma = newval;
		campoFirma = 'FirmaEmpresa1';
	});
	$scope.$watch('data.swFirmaEmpresa2',function(newval,oldval){
		console.log("data FirmaEmpresa2 " +oldval+ " " +newval);
		valorFirma = newval;
		campoFirma = 'FirmaEmpresa2';
	});
	$scope.$watch('data.swFirmaEmpresa3',function(newval,oldval){
		console.log("data FirmaEmpresa3 " +oldval+ " " +newval);
		valorFirma = newval;
		campoFirma = 'FirmaEmpresa3';
	});
	$scope.$watch('data.swFirmaCliente1',function(newval,oldval){
		console.log("data FirmaCliente1 " +oldval+ " " +newval);
		valorFirma = newval;
		campoFirma = 'FirmaCliente1';
	});
	$scope.$watch('data.swFirmaCliente2',function(newval,oldval){
		console.log("data FirmaCliente2 " +oldval+ " " +newval);
		valorFirma = newval;
		campoFirma = 'FirmaCliente2';
	});
	$scope.$watch('data.swFirmaCliente3',function(newval,oldval){
		console.log("data FirmaCliente3 " +oldval+ " " +newval);
		valorFirma = newval;
		campoFirma = 'FirmaCliente3';
	});
	
	
		
	
	$scope.Firmar = function( ) {
		var EventoModificar = [];

		valorFirma = valorFirma == true ? 'S' : 'N'; //llamar esto antes del servicio de firma
		
	   //Parámetros de entrada al servicio
   	    var datosCrear = {};
   	    
	   	datosCrear.p_IdEmpresa			= $scope.$root.SesionIdEmpresa; 
	   	datosCrear.p_IdUsuario			= $scope.$root.SesionIdUsuario;
	   	datosCrear.p_IdModulo			= 'CLI';
	   	datosCrear.p_UsaReglasNegocio	= 'N';
   	    
	   	datosCrear.p_IdAnexo		= $scope.data.IdAnexo;
	   	datosCrear.p_IdDocumento	= $scope.data.IdDocumento;
	   	datosCrear.p_Firmante		= campoFirma;
	   	datosCrear.p_valorFirma		= valorFirma;
	   	  	
      	console.log(datosCrear);

    	var successModificar = function(respuesta)
    	{	  		
    		if (respuesta.P_OUT_ERRORESCONTADOR == "0")
    		{
    			    		
    			$scope.data.reglas = parseServerResponse(respuesta);
				
				
				var i = 0;
		        for(i; i<respuesta.P_OUT_ADVERTENCIASCONTADOR; i++)
		        {
		        	bcsComunes.msg("warning","Advertencia",respuesta.P_OUT_ADVERTENCIASDETALLE[i].Descripcion);
		        }
		        
		        
		        bcsComunes.msg("info","","Documento Firmado");
		        
		        bcsComunes.ventanaCerrar(ventanaId);
    		}
    		else
    	    {	
    			//El servicio arrojó errores. 
				var datos = {};
			
				$scope.data.reglas = parseServerResponse(respuesta);
				
				
				var i = 0;
		        for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
		        {
		        	bcsComunes.msg("error","Error",respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);
		        }
		             //Activa botones
    	    }
    	};
    	
    	var errorModificar = function(respuesta)
    	{	
    		console.log("Error:" + JSON.stringify(respuesta));	    		
    		var datos = {};
			
			bcsComunes.msg("error","Error Interno",JSON.stringify(respuesta));
			//$scope.data.errorpopup(datos);
			$scope.data.btnActivo   = true;     //Activa botones
    	};
    	

    	//Llamada a servicio para crear comprobante de facturación()
    	bcsServicioPlataforma.FirmarAnexo(datosCrear,successModificar,errorModificar);
		
};

var parseServerResponse = function(response)
{
	var result = { errores :[],advertencias:[]};
	$.each(response.P_OUT_ADVERTENCIASDETALLE,function(index,value){;		
		result.adv.push(value);
	});
	$.each(response.P_OUT_ERRORESDETALLE,function(index,value){;
			result.errores.push(value);
	});
return result;
}  
	
	
	
	
	
	
});
		