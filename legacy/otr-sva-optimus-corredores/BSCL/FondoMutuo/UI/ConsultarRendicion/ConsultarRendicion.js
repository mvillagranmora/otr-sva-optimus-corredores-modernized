angular.module('ModuloFFM').controller('ConsultarRendicionCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){

	var winID=bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams(winID);
	
	var conf = bcsConfiguracionFFM.getSolicitudesRendicion();
	var rendicion = {};
	var parametrosRendicionDetalle = [];
	var parametrosRendicionCabecera = [];
	$scope.defaultOptions = conf.options;
	$scope.defaultOptions.gridCreated = function(grid) {
		rendicion.grid = grid;
		rendicion.grid.dataSource.data(parametros.Solicitudes);
		rendicion.grid.refresh();
	}
	
	console.log("solicitudes: ", parametros.Solicitudes);
	
	parametros.Parametros.p_simular = 'N';
	$scope.isVisible = true;
	
	$scope.procesarArchivo = function(){
		var pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CARGARAN_LOS_DATOS_DESDE_EL_ARCHIVO') );
		
		
		pregunta.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				parametrosRendicionCabecera = parametros.Parametros.p_CargaMasiva;
				
				for(i = 0; i < parametros.Solicitudes.length; i++){
					if (parametros.Solicitudes[i].EstadoProcesoRendicion != '') continue;
					for(j=0; j < parametros.Parametros.p_CargaMasivaDetalle.length; j++){
						if (parametros.Parametros.p_CargaMasivaDetalle[j].TipoRegistro == 'D' && parametros.Solicitudes[i].IdentServicio.trim() == parametros.Parametros.p_CargaMasivaDetalle[j].IdentServicio.trim()){
							parametrosRendicionDetalle.push({  CodigoBanco: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoBanco.toString(),	
							    CodigoEmpresa: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoEmpresa.toString(),
							    CodigoConvenio: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoConvenio.toString(),		
							    TipoRegistro: parametros.Parametros.p_CargaMasivaDetalle[j].TipoRegistro.toString(),
							    IdentServicio: parametros.Parametros.p_CargaMasivaDetalle[j].IdentServicio.toString(), 
							    Filler1: "",  			
							    MontoCargo: parametros.Parametros.p_CargaMasivaDetalle[j].MontoCargo.toString(),
							    FechaVencimiento: parametros.Parametros.p_CargaMasivaDetalle[j].FechaVencimiento.toString(),
							    FechaCargo: parametros.Parametros.p_CargaMasivaDetalle[j].FechaCargo.toString(),
							    EstadoCargo: parametros.Parametros.p_CargaMasivaDetalle[j].EstadoCargo.toString(),
							    CodigoRechazo: parametros.Parametros.p_CargaMasivaDetalle[j].CodigoRechazo.toString(),
							    InfAdicional: parametros.Parametros.p_CargaMasivaDetalle[j].InfAdicional.toString(),
							    Filler:""})
						}
					}
				}
				parametros.Parametros.p_CargaMasiva = [];
				parametros.Parametros.p_CargaMasivaDetalle = [];
				parametros.Parametros.p_CargaMasiva = parametrosRendicionCabecera;
				parametros.Parametros.p_CargaMasivaDetalle = parametrosRendicionDetalle;
				
				
				bcsComunes.ventanaOverlay(winID, true);
				bcsComunes.log().info(JSON.stringify(parametros.Parametros));							
				var params = {				
						p_IdEmpresa  : $scope.SesionIdEmpresa,
						p_ValorNuevo  : "S",
				};
				
				
				bcsServicioFondosMutuos.ValidaEjecucionProcesoRecPAC(params).$promise.then(function(resp){
					bcsComunes.setConflictos(util.parseServerResponse(resp));
					
					if (resp.P_OUT_ERRORESCONTADOR === 0){
						bcsServicioFondosMutuos.CrearCargaMasivaPacRen(parametros.Parametros).$promise.then(function(respuesta){
						       if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						     		bcsComunes.ventanaOverlay(winID, false);
						     		$scope.isVisible = false;
						     		
						     		var id=bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADO'), campoSecuencia : "id" ,nombreSecuencia : 'UI_SOLICITUDES', datos : respuesta.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
									bcsComunes.ventanaOverlay(winID, true);
									bcsConfiguracionFFM.ingresarCerrarVentana(id, winID);  	
						       }else{
						     		  bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO"),conflictos:util.parseServerResponse(respuesta)},winID);
						     		  bcsComunes.ventanaOverlay(winID, false);
						     		  
						     		  var params2 = {				
								  			p_IdEmpresa  : $scope.SesionIdEmpresa,
											p_ValorNuevo  : "N",
									  };
									
									  bcsServiciosTesoreria.ValidaEjecucionProcesoRecPAC(params2);
						     	    }
					   		 },function(respuesta){
					   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO"),conflictos:util.parseServerResponse(respuesta)},winID);
					   			bcsComunes.ventanaOverlay(winID, false);
					   			
				   				var params2 = {				
						  			p_IdEmpresa  : $scope.SesionIdEmpresa,
									p_ValorNuevo  : "N",
								};
								
								bcsServiciosTesoreria.ValidaEjecucionProcesoRecPAC(params2);
					   		 });	
					}else{
						var ErrorDetalle = resp.P_OUT_MENSAJES || [];
						$.each(ErrorDetalle,function(key,value){	
							bcsComunes.msg("error","Error","Existen problemas");
						});
						bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(resp)},winID);
						bcsComunes.ventanaOverlay(winID,false);
				    }
				 });	
							
			}
		})
	}
	

	
});
