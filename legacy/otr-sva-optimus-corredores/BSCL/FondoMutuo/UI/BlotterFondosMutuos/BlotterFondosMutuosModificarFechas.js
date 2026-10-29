angular.module('ModuloFFM').controller('BlotterFondosMutuosModificarFechasCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,$timeout){
$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
	$scope.formData = {};
	$scope.formData.datosAntes = $scope.parametros;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var seleccionados = $scope.formData.datosAntes;
		
	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	$scope.GrabarFechas = function(seleccionados){
		if($scope.formData.datosDespues !== undefined){
			bcsComunes.ventanaOverlay($scope.winID,true);
			var listSolicitudAporteRescate = []
			$.each($scope.formData.datosAntes.seleccionados,function(key, items){
		  			listSolicitudAporteRescate.push({IdSolicitudAporteRescate : items.IdSolicitudAporteRescate});
		  	});
		
			const datosModificarFondoMutuo = {

				p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
				
				p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
				
				p_IdModulo : 'FMM',
				
				p_IdSolicitudAporteRescate : listSolicitudAporteRescate,
				
				p_FechaAceptacion : $scope.formData.datosDespues.FechaAceptacion ? util.dateSPdate($scope.formData.datosDespues.FechaAceptacion) : '0001-01-01',
				
				p_FechaCertificacion : $scope.formData.datosDespues.FechaCertificacion ? util.dateSPdate($scope.formData.datosDespues.FechaCertificacion) : '0001-01-01',
				
				p_FechaLiquidacion : $scope.formData.datosDespues.FechaLiquidacion ? util.dateSPdate($scope.formData.datosDespues.FechaLiquidacion) : '0001-01-01',
				
				p_FechaValorCuota : $scope.formData.datosDespues.FechaValorCuota ? util.dateSPdate($scope.formData.datosDespues.FechaValorCuota) : '0001-01-01',
					
				
			}
			console.log($scope.formData.datosAntes);
			bcsServicioFondosMutuos.ActualizarFechaGestionOperaciones(datosModificarFondoMutuo).$promise.then(function(respuesta){

		     			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
						respuesta.P_OUT_RESULTADO = new Array();
						
						if(respuesta.P_OUT_MENSAJES.length > 0){
							$.each(obtenerMensajes(respuesta.P_OUT_MENSAJES),function(key,value){
								respuesta.P_OUT_RESULTADO.push( value );
							});
						}
						
						if(respuesta.P_OUT_ADVERTENCIAS.length > 0){
							$.each(obtenerMensajes(respuesta.P_OUT_ADVERTENCIAS),function(key,value){
								respuesta.P_OUT_RESULTADO.push( value );
							});
						}
							bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);	
						
						bcsComunes.ventanaOverlay(idVentana,false);
						$scope.habilitarBoton = true;
				
	 		 },function(respuesta){
	 		   bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_MODIFICAR_FECHAS_FONDO_MUTUO"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
	 		 });
	 		 
		}else{
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INSERTAR_FECHAS"),conflictos:util.parseServerResponse(seleccionados)},$scope.winID);
				
		}	
			let obtenerMensajes = function (listadoMensajes){
			var mensajeCarga = new Array();
			$.each(listadoMensajes,function(key,value){
				 var objCarga = {
						IdInterno : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : value.descripcion
				 };
				 mensajeCarga.push(objCarga);
			});
			return mensajeCarga;
		};
	
	}
		
});