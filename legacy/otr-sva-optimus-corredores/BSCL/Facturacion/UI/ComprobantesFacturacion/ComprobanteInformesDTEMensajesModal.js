angular.module('ModuloFacturacion').controller('ComprobanteInformesDTEMensajesModalCtrl', function($scope,$translate,$modal,
				        bcsComunes,bcsServiciosFacturacion,bcsServicioPlataforma, bcsComprobantesDteMensajesGrid) 
{
    
    $scope.data = {};
    
    //Recupera parametros desde la ventana recibida
    var idVentana = bcsComunes.ventanaWinId($scope);    
    $scope.datosEntrada = bcsComunes.ventanaParams(idVentana);
    if(!_.isArray($scope.datosEntrada) && _.isObject($scope.datosEntrada)) {
    	$scope.datosEntrada = _.toArray($scope.datosEntrada);
    }
    else {
    	$scope.datosEntrada = [];
    }
	
    //Seteo de variables de sistema
    $scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	
	//Grilla Mensajes
	var defGridCmbteDteMens = {
		type : "array",
		definition : {
			data : $scope.datosEntrada,
			schema : bcsComprobantesDteMensajesGrid.modeloCmbteDteMens(),
		}
	};	

    var dsGridCmbteDteMens = new bcs.DataSource(defGridCmbteDteMens);
    $scope.confBasicaCmbteDte = { 
          dataSource        : dsGridCmbteDteMens,
          columns	        : bcsComprobantesDteMensajesGrid.columnasCmbteDteMens(),
          autoBind			: true,
          sortable			: true,
          height			: 180,
          selectable 		: "multiple row"

     }; 
    
    
    
    //-----------------------------------------------------
    //Cierra ventana 
    //-----------------------------------------------------	     
    $scope.data.cancelar = function()     
      {
		bcsComunes.ventanaCerrar(idVentana);
	  };
});
