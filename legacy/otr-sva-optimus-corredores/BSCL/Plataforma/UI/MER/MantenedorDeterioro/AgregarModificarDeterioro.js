angular.module('ModuloPlataforma').controller('AgregarModificarDeterioroCtrl', function ($scope, bcsComunes, $translate, bcsServicioPlataforma) {
    var idVentana = bcsComunes.ventanaWinId($scope);
    var parametros = bcsComunes.ventanaParams(idVentana);
    $scope.IdEmisor = parametros.IdDeterioroEmisor;
    $scope.AccionBoton = parametros.AccionBoton;
    $scope.data = {};
    $scope.data = parametros;
    $scope.accionBoton = ($scope.data.AccionBoton === 'MODIFICAR');

    ModeloEmisores = function () {
        return {
            model: {
                fields: {
                    IdDeterioroEmisor: { type: "number" },
                    Emisor: { type: "string" }
                }
            }
        };
    };

    var dataSourceEmisores = function () {
        return new bcs.DataSource({
            type: "odata",
            definition: {
                serverFiltering: true,
                serverPaging: true,
                serverSorting: true,
                pageSize: 50,
                transport: {
                    read: {
                        url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=55669900",
                        dataType: "json"
                    }
                },
                schema: ModeloEmisores(),
            }
        });
    };

    $scope.emisores = [];
    $scope.emisoresDataSource = dataSourceEmisores();
    $scope.emisoresDataSource.fetch(function () {
        var data = this.data();
        $scope.$apply(function () {
            $scope.emisores = data;
        });

    });

    if (parametros.IdDeterioroEmisor) {
        bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR'));
    } else {
        bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_INGRESAR'));
    }

    const successCrearModificarDeterioro = function (respuesta) {
     
        let mensaje = ''
       if(respuesta.P_OUT_ERRORESCONTADOR === 0){
            mensaje = "Acción realizada exitosamente"		
       }else{
            mensaje = $translate.instant("UI_ERROR");
            let mensajeTotal = new Array();
            $.each(respuesta.P_OUT_MENSAJES,function(key,value){
                let objMensaje = {
                        Id : value.id,
                        tipo : value.tipo,
                        codigo : value.codigo,
                        descripcion : $translate.instant(value.descripcion)
                };
                mensajeTotal.push(objMensaje);
            });
            respuesta.P_OUT_MENSAJES = mensajeTotal;
       }

        let idModal = bcsComunes.ventanaMsg({
            mensaje: mensaje,
            conflictos:util.parseServerResponse(respuesta)
        },idVentana);

        bcsComunes.ventanaPromesa(idModal).then(function(){
            bcsComunes.ventanaOverlay(idVentana,false);
           if (respuesta.P_OUT_ERRORESCONTADOR === 0) bcsComunes.ventanaCerrar(idVentana);	
         });

    };

    const errorCrearModificarDeterioro = function () {
        bcsComunes.ventanaOverlay(idVentana,false);
		bcsComunes.msg("error","error",$translate.instant("UI_ERROR"));
    };


    $scope.aceptar = function () {
        let Flag;
        const flagDeterioro = $scope.data.FlagDeterioro ? 'S' : 'N';
        if ($scope.AccionBoton == 'MODIFICAR') {
            Flag = [{
                IdDeterioroEmisor: $scope.data.IdDeterioroEmisor,
                Emisor: $scope.data.Emisor,
                FlagDeterioro: flagDeterioro,
                PorcentajeDeterioro: $scope.data.PorcentajeDeterioro,
                Fase: $scope.data.Fase,
                Accion: $scope.AccionBoton
            }];
        } else {
            Flag = [{
                IdDeterioroEmisor: 0,
                Emisor: $scope.data.Emisor.toUpperCase(),
                FlagDeterioro: flagDeterioro,
                PorcentajeDeterioro: $scope.data.PorcentajeDeterioro,
                Fase: $scope.data.Fase,
                Accion: $scope.AccionBoton
            }];
        }


        var datos = {
            p_DeteriorosEmisores: Flag,
            p_IdEmpresa: $scope.SesionIdEmpresa,
            p_IdUsuario: $scope.SesionIdUsuario,
            p_IdModulo: "PLA",
            p_UsaReglasNegocio: "N"
        };

        bcsComunes.ventanaOverlay(idVentana, true);
        if($scope.AccionBoton == 'MODIFICAR'){
            bcsServicioPlataforma.Servicios().ModificarDeterioroEmisor(datos, successCrearModificarDeterioro, errorCrearModificarDeterioro);
        } else {
            bcsServicioPlataforma.Servicios().CrearDeterioroEmisor(datos, successCrearModificarDeterioro, errorCrearModificarDeterioro);
        }
        
    };

    $scope.cancelar = function () {
        bcsComunes.ventanaData(idVentana, false);
        bcsComunes.ventanaCerrar(idVentana);
    };
});
