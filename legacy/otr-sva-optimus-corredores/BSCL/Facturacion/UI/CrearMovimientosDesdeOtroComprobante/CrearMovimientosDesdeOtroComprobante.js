angular.module('ModuloFacturacion').controller('CrearMovimientosDesdeOtroComprobanteCtrl',
		function ($scope,$translate,bcsComunes,bcsServicioPlataforma,bcsServiciosFacturacion) {
	
	$scope.formData = {};
	idVentana = bcsComunes.ventanaWinId($scope);
	var params = bcsComunes.ventanaParams(idVentana);
	
	$scope.formData.oTipoComprobante={
			tipo : "combobox-tipo-comprobante",
			configuracion : {},
			filtros : {
			    IdEstadoFacturacion        : {field : "IdEstadoFacturacion", operator : "eq", value : "ACT"},
			    UsarEnFacturaAPartirDeOtra : {field : "UsarEnFacturaAPartirDeOtra", operator : "eq", value : "S"},
			},
			idActual : false,
			seleccionado : false,
			idDefecto : false,
			idPostFiltro : false,
			
	};


	//Se necesita en este formato para HTML.
	$scope.formData.idTipCmbte = params.IdTipoComprobante; 	
	$scope.formData.IdComprobante = params.IdComprobante;	
	$scope.formData.nombTipCmbte = params.NombreTipoComprobante;
	$scope.formData.FechaSistema = params.FechaSistema;
	$scope.formData.oTipoComprobante.idDefecto = params.IdTipoComprobante;
	
	
	$scope.formData.aceptar = function() {	
		//Recopila Id de comprobante(s) seleccionado(s)	
		var ComprobanteACrear = [];
        ComprobanteACrear.push(
                                 {   
	                               IdComprobante		: $scope.formData.IdComprobante
                                 });
	
	    //Llamada a servicio
	    var datos = {
		  	           p_IdEmpresa : $scope.SesionIdEmpresa,
			           p_IdUsuario : $scope.SesionIdUsuario,
			           p_IdModulo : "FAC",
			           p_simularOperacion 		: "N",
			           p_UsaReglasNegocio 		: "S",
			           p_EsIngresoAutomatico   : "N",
			           p_IdCliente 			: $scope.formData.portafolioSeleccionado.IdCliente,
			           p_IdPortafolio 			: $scope.formData.portafolioSeleccionado.IdPortafolio,			
			           P_COMPROBANTE    		: ComprobanteACrear
		            };

		bcsComunes.ventanaOverlay(idVentana,true);
  	    bcsServiciosFacturacion.CrearMovimientosDesdeOtroComprobante(datos,
  	       function(respuesta){
  	    	     if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0"){	
      	        		 var idVentanaMsg = bcsComunes.ventanaMsg({
    			    	                         conflictos:util.parseServerResponse(respuesta)
			                                   }, idVentana);
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
	
	$scope.formData.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	
	
});
