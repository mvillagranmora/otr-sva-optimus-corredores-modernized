angular.module('ModuloNormativa').controller('AgregarModificarEntidadesAuditorasCtrl',function($scope,bcsComunes,$translate,$resource,bcsServiciosNormativa){
				
    mta = this;
    var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
    var parametros = bcsComunes.ventanaParams(idVentana);
    
    $scope.IdEntidadAuditora = parametros.IdEntidadAuditora;
    $scope.AccionBoton = parametros.AccionBoton;
    $scope.data = {};
    $scope.data = parametros;
    $scope.data.Estado = 'ACT';
    $scope.filtrosEstado = {
		Entidad : {"field" : "NombreEntidad", "operator" : "eq", "value" : 'AUDITOR'}
	}

    $scope.oConfiguracionTipoCasilla = {
        tipo : "combobox-tipo-casilla",
			configuracion :{
					placeholder: $translate.instant("UI_TIPO_CASILLA")
				},
				idActual : $scope.AccionBoton == 'MODIFICAR' ? $scope.data.TipoCasilla :'ELECTRONICA',
				seleccionado : false,
				idDefecto : $scope.AccionBoton == 'MODIFICAR' ? $scope.data.TipoCasilla :'ELECTRONICA',
				idPostFiltro : $scope.AccionBoton == 'MODIFICAR' ? $scope.data.TipoCasilla :'ELECTRONICA',
				itemPostFiltro : false
    }

    $scope.oConfiguracionEstado = {
        tipo : "combobox-estado-pla",
			configuracion :{
					placeholder: $translate.instant("UI_ESTADO")
				},
                idActual : $scope.AccionBoton == 'MODIFICAR' ? $scope.data.Estado :'ACT',
				seleccionado : false,
				idDefecto : $scope.AccionBoton == 'MODIFICAR' ? $scope.data.Estado :'ACT',
				idPostFiltro : $scope.AccionBoton == 'MODIFICAR' ? $scope.data.Estado :'ACT',
				itemPostFiltro : false
    }
    
   
    if (parametros.IdEntidadAuditora) //modificacion
    {
        console.log(parametros)
        bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_ENTIDAD_AUDITORA'));
    }
    else //AGREGAR
    {
        bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_ENTIDAD_AUDITORA'));
    }


    var successCrearSolicitud = function(respuesta)
    {	
        var mensajeId = bcsComunes.ventanaMsg({ mensaje : "Accion realizada exitosamente.",
                        conflictos : util.parseServerResponse(respuesta)}, idVentana);
                        bcsComunes.ventanaPromesa(mensajeId).then(function(){
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.ventanaData(idVentana,true);
                        bcsComunes.ventanaCerrar(idVentana);
        });
    };

    var errorCrearSolicitud = function(respuesta)
    {	
        console.log(respuesta);
                    bcsComunes.msg("error","error","Error");
                    bcsComunes.ventanaOverlay(idVentana, false);
                    bcsComunes.ventanaData(idVentana,true);
                    bcsComunes.ventanaCerrar(idVentana);
    };
            
    $scope.Aceptar = function() {
        //modificacion
        if ($scope.AccionBoton == 'MODIFICAR'){
    
            
            var entidadAuditora = [{
                IdEntidadAuditora : $scope.data.IdEntidadAuditora,
                EmpresaAuditora : $scope.data.EmpresaAuditora,
                AuditorEncargado : $scope.data.AuditorEncargado,
                TipoCasilla : $scope.oConfiguracionTipoCasilla.seleccionado.TipoCasilla,
                Casilla : $scope.data.Casilla,
                Estado : $scope.oConfiguracionEstado.seleccionado.IdEstado
            }];
            var datos = {
                Accion : $scope.AccionBoton,
                EntidadAuditora_in : entidadAuditora

            };

            bcsComunes.ventanaOverlay(idVentana, true);
            bcsServiciosNormativa.AgregarModificarEntidadAuditora(datos,successCrearSolicitud,errorCrearSolicitud);
            
            
            
            
        }
        //creacion
        else {
            
            
            var entidadAuditora = [{
                IdEntidadAuditora : 0,
                EmpresaAuditora : $scope.data.EmpresaAuditora,
                AuditorEncargado : $scope.data.AuditorEncargado,
                TipoCasilla : $scope.oConfiguracionTipoCasilla.seleccionado.TipoCasilla,
                Casilla : $scope.data.Casilla,
                Estado : $scope.oConfiguracionEstado.seleccionado.IdEstado
            }];

            var datos = {
                Accion : $scope.AccionBoton,
                EntidadAuditora_in : entidadAuditora

            };
            bcsComunes.ventanaOverlay(idVentana, true);
            bcsServiciosNormativa.AgregarModificarEntidadAuditora(datos,successCrearSolicitud,errorCrearSolicitud);
            
        }
        
        
    };
    
    $scope.cancelar = function() {
        bcsComunes.ventanaData(idVentana,false);
        bcsComunes.ventanaCerrar(idVentana);
    };
    

    $scope.$watch('EstadoSeleccionado',function(newval,oldval){   
        if (newval === oldval) return;
        
        console.log($scope.EstadoSeleccionado);
    },function(result){
        console.log(result);
    });
    


});