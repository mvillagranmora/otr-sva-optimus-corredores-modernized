angular.module('ModuloFacturacion').controller('ComprobanteInformesDTEModalCtrl', function($scope,$translate,$modal,
				        bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma) 
{
    
    $scope.data = {};
    
    //Recupera parametros desde la ventana recibida
    var idVentana = bcsComunes.ventanaWinId($scope);    
    $scope.idDte =  bcsComunes.ventanaParams(idVentana);
   
   
    //Seteo de variables de sistema
    $scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
    
    
    
    //Llama Informes DTE
    $scope.data.informeDte = function(tipoInforme) 
      {
    	bcsComunes.ventanaOverlay(idVentana, true);
		$scope.data.btnActivos  = false;
		
		
		
		//Recupera id del dte de comprobantes seleccionados		
		var ComprobanteDTE = [];
	    $.each($scope.idDte[1],function(key,value)
	      {
	    	ComprobanteDTE.push(
	    			              { 
	    			                IdDocumento : $scope.idDte[1][key].IdRespuestaDTE 
	    			              });
	      }
	    );
		
		//Parámetros de entrada al servicio de consulta dte		
		var datosConsultar = {};		
		
		datosConsultar.p_IdEmpresa   = $scope.data.IdEmpresa; 
		datosConsultar.p_IdUsuario	= $scope.data.IdUsuario;
		datosConsultar.p_Tipo   		= tipoInforme;
		datosConsultar.p_IdDocumentos = ComprobanteDTE; 
	    
	    
	    //Llamada a servicio para crear comprobante de facturación()
	    bcsServiciosFacturacion.InformeDTE(datosConsultar,function(result)
	      {
	    	bcsComunes.ventanaOverlay(idVentana, false);
	    	var consResultado = [];
	    	var respuestaDTE = [];

	    	bcsComunes.ventanaOverlay(idVentana, true);

	    	if (datosConsultar.p_Tipo === "PDF") {
	    		for(var i = 0; i < result.P_FILES.length; i++) {
	    			var leBlob = util.b64toBlob(result.P_FILES[i].encodedString,'application/pdf',512);
		      		saveAs(leBlob, "ComprobanteFacturacion_" + result.P_FILES[i].dteNumber + ".pdf");

		      		var idRespuestaDTE = parseInt(result.P_FILES[i].dteNumber);
		      		idRespuestaDTE = isNaN(idRespuestaDTE) ? 0 : idRespuestaDTE,
		      		respuestaDTE[idRespuestaDTE] = result.P_FILES[i].mensaje;
		      	 }
	    	}
	    	else {
	    		for(var i = 0; i < result.P_FILES.length; i++) {
	    			var leBlob = util.b64toBlob(result.P_FILES[i].encodedString,'application/xml',512);
		      		saveAs(leBlob, "ComprobanteFacturacion_" + result.P_FILES[i].dteNumber + ".xml");

		      		var idRespuestaDTE = parseInt(result.P_FILES[i].dteNumber);
		      		idRespuestaDTE = isNaN(idRespuestaDTE) ? 0 : idRespuestaDTE,
		      		respuestaDTE[idRespuestaDTE] = result.P_FILES[i].mensaje;
		      	 }
	    	}

	    	for(var i = 0; i < $scope.idDte[1].length; i++) {
				var msg = respuestaDTE[$scope.idDte[1][i].IdRespuestaDTE];
				msg = msg === "" ? $scope.idDte[1][i].Mensaje : 'Comprobante DTE no encontrado';
				consResultado[i] = {
					IdComprobante: $scope.idDte[1][i].IdComprobante,
					IdRespuestaDTE: $scope.idDte[1][i].IdRespuestaDTE,
					Mensaje: msg
				};
			}
			
		    bcsComunes.ventanaOverlay(idVentana, false);

		    mensajeSalida(consResultado);

	      },function(resultado)
	         {
	    	    bcsComunes.ventanaOverlay(idVentana, false);
	    	 });
	  };
	
	  
    
	var mensajeSalida = function(consResultado){
		var mensajesDte = _.union($scope.idDte[0], consResultado);

		var idModal = bcsComunes.ventanaAbrir("ComprobanteInformesDTEMensajesModal", mensajesDte, false, false, idVentana);
		var promesa = bcsComunes.ventanaPromesa(idModal);

		promesa.then(function(data) {
			bcsComunes.ventanaOverlay(idVentana, false);
		});

   }
    
    //-----------------------------------------------------
    //Cierra ventana 
    //-----------------------------------------------------	     
    $scope.data.cancelar = function()     
      {
		bcsComunes.ventanaCerrar(idVentana);
	  };
});
