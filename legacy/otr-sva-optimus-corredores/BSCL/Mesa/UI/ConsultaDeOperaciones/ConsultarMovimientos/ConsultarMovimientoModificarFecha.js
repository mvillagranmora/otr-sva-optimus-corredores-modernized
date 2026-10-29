angular.module('ModuloFacturacion').controller('ConsultarMovimientoModificarFechaCtrl',
		function ($scope,$translate,bcsComunes,bcsServicioPlataforma,bcsServiciosMesa) {
	
	$scope.data = {};
	idVentana = bcsComunes.ventanaWinId($scope);
	var params = bcsComunes.ventanaParams(idVentana);
			
	//Por Default
	$scope.data.oFechaOperacion = params.FechaOperacion;
	
	
	$scope.data.aceptar = function() {	
				
		//Recopila Id de comprobante(s) seleccionado(s)	
		var OperacionModificarFecha = [];
		OperacionModificarFecha.push(
                                      {   
                                    	  IdOperacion : params.IdOperacion,
                                      });
	
	    //Llamada a servicio
	    var datos = {
		  	           p_IdEmpresa : $scope.SesionIdEmpresa,
			           p_IdUsuario : $scope.SesionIdUsuario,
			           p_IdModulo  : "MES",
			           p_simularOperacion    : "N",
			           p_UsaReglasNegocio    : "S",
			           p_FechaOperacionNueva : util.dateSPdate($scope.data.oFechaOperacionNueva),
			           P_MOVIMIENTO : OperacionModificarFecha
		            };

		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServiciosMesa.ModificarFechaMovimiento(datos,
  	       function(respuesta){
  	    	     if (respuesta.P_OUT_ERRORESCONTADOR == "0"){	
      	        		 var idVentanaMsg = bcsComunes.ventanaMsg({
    			    	                         conflictos:util.parseServerResponse(respuesta)
			                                   }, idVentana);

      	        		 bcsComunes.ventanaOverlay(idVentana, false);
			             var promesa = bcsComunes.ventanaPromesa(idVentanaMsg);
			             
			             promesa.then(function(){ 
			             	bcsComunes.ventanaCerrar(idVentana);
			             });
    		     }else{	
				    if (respuesta.P_OUT_ERRORESCONTADOR > 0){
				         bcsComunes.ventanaMsg({
					                             mensaje:$translate.instant(""),
					                             conflictos:util.parseServerResponse(respuesta)
					                           }, idVentana);
                    }
    	          }
      		 },
  		   
      		 function(respuesta){
      	        bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			    bcsComunes.msg("error","error",$translate.instant('UI_ERROR_INGRESO'));
			    bcsComunes.ventanaOverlay(idVentana, false);
      		 }
      	);			
	};
	
	$scope.data.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}
	
});
