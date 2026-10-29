angular.module('ModuloNormativa').controller('FilialArchivoP40Ctrl',function($scope,$translate,bcsComunes,bcsServicioInterfaces,bcsAlerta,OPCIONES_BCSALERTA,bcsServicioReportes,$timeout){
	$scope.data = {};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.parametrosEntrada = bcsComunes.ventanaParams($scope.winID);
	$scope.HabilitoCampos = true;	

	var transaccion_OK = function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){		
				id = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			else{			
				id = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			bcsComunes.ventanaPromesa(id).then(function(result){
				bcsComunes.ventanaCerrar($scope.winID);
			});			
		};
		
	var transaccion_Err = function(respuesta){
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.BuscarOperaciones();
		};
	
	/******************************************************/
    /***             FILTROS							***/
    /******************************************************/
	
	$scope.oConfiguracionCliente = {
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : undefined,
		seleccionado : -1,
		idDefecto : $scope.parametrosEntrada.IdSocioDeNegocio
	};

	$scope.$watch('oConfiguracionCliente.seleccionado',function(newval,oldval){
		if (!$scope.oConfiguracionCliente.seleccionado) return;
		try{
			$scope.socioGral.idActual = $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
			$scope.socioGral.idDefecto = $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
		}catch(e){
		}
	});
	 
	
	/********************************************************************************************/
	/***************************************   INICIO DE VARIABLES   ****************************/
	/********************************************************************************************/
	
		if($scope.parametrosEntrada.RutFilial != undefined){
			$scope.data.Esnuevo = 'N';
			$scope.data.NumeroDocumentoIdentidad = $scope.parametrosEntrada.RutFilial;
			$scope.data.NombreFilial = $scope.parametrosEntrada.NombreFilial;
			$scope.data.CodigoTenedor = $scope.parametrosEntrada.CodigoTenedor;
			$scope.data.NombreZip = $scope.parametrosEntrada.NombreZip;
			$scope.data.NombreArchivo = $scope.parametrosEntrada.NombreArchivo;
			$scope.data.IdSocioDeNegocio =$scope.parametrosEntrada.IdSocioDeNegocio;
		    $scope.HabilitoCampos = false;
		}else{
			$scope.data.Esnuevo = 'S';
			$scope.HabilitoCampos = true;
		}
    

		/*********************************************
		 			GUARDAR FILIALES
		 ********************************************/

		$scope.data.GuardarFiliales = function(){
			var parametros = {};
			var filiales   = [];
            var pregunta;
     		if($scope.data.Esnuevo == 'S'){
     			if($scope.oConfiguracionCliente.seleccionado == undefined  || $scope.data.CodigoTenedor == undefined || $scope.data.NombreZip == undefined || $scope.data.NombreArchivo == undefined ){
     				bcsComunes.msg("error","error",$translate.instant('UI_DEBE_COMPLETAR_TODOS_LOS_CAMPOS'));
     			} else if($scope.data.CodigoTenedor.length != 3){
     				bcsComunes.msg("error","error",$translate.instant('UI_CODIGO_TENEDOR_DEBE_SER_LARGO_3'));
     			}else{
     				var numeros = /^([0-9])*$/;
                    var sonNumeros = numeros.exec($scope.data.CodigoTenedor);
                    if(sonNumeros == null){
                        bcsComunes.msg("error","error",$translate.instant('UI_CODIGO_TENEDOR_DEBE_SER_NUMERICO'));
                    }else{
     			        pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_DESEA_AGREGAR_NUEVA_FILIAL'));	
                        pregunta.result.then(function(result){
						    if (pregunta.result.$$state.value === OPCIONES_BCSALERTA.ACEPTAR){
                	            filiales.push({	 
                	                 "Rut"                 : $scope.oConfiguracionCliente.seleccionado.DocumentoIdentidadDefecto
				                    ,"IdEmpresa"           : $scope.SesionIdEmpresa
							        ,"NombreFilial"        : $scope.oConfiguracionCliente.seleccionado.NombreCompleto
							        ,"IdSocioDeNegocio"    : $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio
							        ,"CodigoTenedor"       : $scope.data.CodigoTenedor
							        ,"NombreZip"           : $scope.data.NombreZip
							        ,"NombreArchivo"       : $scope.data.NombreArchivo
						        });
			                    parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
			                    parametros.p_IdUsuario = $scope.SesionIdUsuario;
			                    parametros.p_IdModulo = 'NOR';	
			                    parametros.p_FilialesP40 = filiales;
			                    bcsServicioInterfaces.AgregarModificarFilialesP40(parametros,transaccion_OK,transaccion_Err);	
			                 }
     			      });
     			    }
     		     }    
     		}else{
     			if( $scope.data.CodigoTenedor == "" || $scope.data.NombreZip == "" || $scope.data.NombreArchivo == "" ){
     				bcsComunes.msg("error","error",$translate.instant('UI_DEBE_COMPLETAR_TODOS_LOS_CAMPOS'));
     			} else if($scope.data.CodigoTenedor.length != 3){
     				bcsComunes.msg("error","error",$translate.instant('UI_CODIGO_TENEDOR_DEBE_SER_LARGO_3'));
     			}else{
     				var numeros = /^([0-9])*$/;
                    var sonNumeros = numeros.exec($scope.data.CodigoTenedor);
                    if(sonNumeros == null){
                    	bcsComunes.msg("error","error",$translate.instant('UI_CODIGO_TENEDOR_DEBE_SER_NUMERICO'));
                    }else{
     			        pregunta = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_DESEA_MODIFICARA_FILIAL'));
     			        pregunta.result.then(function(result){
     			        if (pregunta.result.$$state.value === OPCIONES_BCSALERTA.ACEPTAR){
                	        filiales.push({	 
                	             "IdSocioDeNegocio" : $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio  
                	            ,"Rut" : $scope.oConfiguracionCliente.seleccionado.DocumentoIdentidadDefecto
				                ,"IdEmpresa" : $scope.SesionIdEmpresa
							    ,"NombreFilial" : $scope.oConfiguracionCliente.seleccionado.NombreCompleto
							    ,"CodigoTenedor" : $scope.data.CodigoTenedor
							    ,"NombreZip" : $scope.data.NombreZip
							    ,"NombreArchivo" : $scope.data.NombreArchivo
						    });
					        parametros.p_IdEmpresa = $scope.SesionIdEmpresa;
							parametros.p_IdUsuario = $scope.SesionIdUsuario;
			                parametros.p_IdModulo = 'NOR';	
							parametros.p_FilialesP40 = filiales;
							bcsServicioInterfaces.AgregarModificarFilialesP40(parametros,transaccion_OK,transaccion_Err);	
     			       }
     			       });
     		        }
     		    }
     		}
		};

		/*********************************************
			CANCELAR
		 ********************************************/
		
		$scope.data.Cancelar = function(){
			bcsComunes.ventanaCerrar($scope.winID);
		};
});