angular.module('ModuloSocioNegocio').controller('ModalClienteDocumentosCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};
	
	var ventanaId = bcsComunes.ventanaWinId($scope);
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	
	
	$scope.Cerrar = function () {
		bcsComunes.ventanaCerrar(ventanaId); 
	};
	
	
	$scope.$watch('data.seleccionadoTipoDoc',function(newval,oldval){
		console.log("data FirmaEmpresa1 " +oldval+ " " +newval);
		
		$scope.data.mostrarFecha = ($scope.data.seleccionadoTipoDoc.ValidaFechaVigencia === 'S' ? true:false);
		$scope.data.mostrarEmpresa1 = false;
		$scope.data.mostrarEmpresa2 = false;
		$scope.data.mostrarEmpresa3 = false;
		$scope.data.mostrarCliente1 = false;
		$scope.data.mostrarCliente2 = false;
		$scope.data.mostrarCliente3 = false;
		
		if($scope.data.seleccionadoTipoDoc.NumeroFirmantesEmpresa == 1)
		{
			$scope.data.mostrarEmpresa1 = true;
		}
		else if($scope.data.seleccionadoTipoDoc.NumeroFirmantesEmpresa == 2)
		{
			$scope.data.mostrarEmpresa1 = true;
			$scope.data.mostrarEmpresa2 = true;
		}
		else if($scope.data.seleccionadoTipoDoc.NumeroFirmantesEmpresa == 3)
		{
			$scope.data.mostrarEmpresa1 = true;
			$scope.data.mostrarEmpresa2 = true;
			$scope.data.mostrarEmpresa3 = true;
		}
		
		if($scope.data.seleccionadoTipoDoc.NumeroFirmantesCliente == 1)
		{
			$scope.data.mostrarCliente1 = true;
		}
		else if($scope.data.seleccionadoTipoDoc.NumeroFirmantesCliente == 2)
		{
			$scope.data.mostrarCliente1 = true;
			$scope.data.mostrarCliente2 = true;
		}
		else if($scope.data.seleccionadoTipoDoc.NumeroFirmantesCliente == 3)
		{
			$scope.data.mostrarCliente1 = true;
			$scope.data.mostrarCliente2 = true;
			$scope.data.mostrarCliente3 = true;
		}
	});
	
	
	var agregarDocumentoSP = function(v_Dato)
    {
	       var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=210";
	       var r = $resource
         ("", {}, {
	                 'url': { method: "POST", url: url}
                  }
         );
	       var post = r.url(v_Dato);
	       return post.$promise;
    };
	
	$scope.Agregar = function () {
		var parametrosSP = {};
        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
        parametrosSP.p_IdModulo = 'CLI';
        parametrosSP.p_UsaReglasNegocio = "N";
        parametrosSP.p_IdCliente = parametros.IdCliente;
        parametrosSP.p_IdTipoDocumento = $scope.data.seleccionadoTipoDoc.IdTipoDocumento;
        parametrosSP.p_NumeroFirmantesEmpresa = $scope.data.seleccionadoTipoDoc.NumeroFirmantesEmpresa;
        parametrosSP.p_NumeroFirmantesCliente = $scope.data.seleccionadoTipoDoc.NumeroFirmantesCliente;
              
        parametrosSP.p_FechaVigencia = $scope.data.FechaVigencia || '';
        
        if($scope.data.seleccionadoEmpresa1 !== undefined)
        	parametrosSP.p_NombreFirmanteEmpresa1 =	$scope.data.seleccionadoEmpresa1;
        if($scope.data.seleccionadoEmpresa2 !== undefined)
        	parametrosSP.p_NombreFirmanteEmpresa2 =	$scope.data.seleccionadoEmpresa2;
        if($scope.data.seleccionadoEmpresa3 !== undefined)
        	parametrosSP.p_NombreFirmanteEmpresa3 =	$scope.data.seleccionadoEmpresa3;
        
        if($scope.data.seleccionadoCliente1 !== undefined)
        	parametrosSP.p_NombreFirmanteCliente1 =	$scope.data.seleccionadoCliente1;
        if($scope.data.seleccionadoCliente2 !== undefined)
        	parametrosSP.p_NombreFirmanteCliente2 =	$scope.data.seleccionadoCliente2;
        if($scope.data.seleccionadoCliente3 !== undefined)
        	parametrosSP.p_NombreFirmanteCliente3 =	$scope.data.seleccionadoCliente3;
        
        
    	    	
        agregarDocumentoSP(parametrosSP).then(
				function(result) {
					if (result.P_OUT_ADVERTENCIASCONTADOR){
						bcsComunes.msg("warning","Advertencia","La operacion tiene advertencias");
					}else if (result.P_OUT_ERRORESCONTADOR){
						bcsComunes.msg("error","Error","La operacion tiene errores");
					}else
						{
							bcsComunes.msg("info","info","Tipo Documento Agregado");
							bcsComunes.ventanaCerrar(ventanaId);
						}
				},
				function(result){
					
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
	};
	
});