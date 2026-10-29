angular.module("ModuloFacturacion").controller("AnularMovimientosPorAsignacionMasivaModalCtrl", function ($scope, $translate, bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, bcsServiciosFacturacion) {

    let idVentana = bcsComunes.ventanaWinId($scope);

    $scope.data = {};
    $scope.data.IdEmpresa = $scope.SesionIdEmpresa;
    $scope.data.IdUsuario = $scope.SesionIdUsuario;
    $scope.data.IdModulo = "FAC";

    $scope.data.filtraPorAsignacionMasiva = false;
    $scope.data.horaDesde = new Date($scope.SesionFechaSistema + " 00:00:00");
    $scope.data.horaHasta = new Date($scope.SesionFechaSistema + " " + $scope.SesionHoraSistema);
    $scope.data.fechaConsulta = util.dateInicioDia($scope.SesionFechaSistema);
    $scope.inputsHabilitados = true;
    $scope.data.oClientePortafolio={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};

/******************************************************/
/*** FUNCIONES										***/
/******************************************************/

    $scope.generarPayload = function() {

        let payload = {}
        payload.p_IdEmpresa = $scope.SesionIdEmpresa;
        payload.p_IdUsuario = $scope.SesionIdUsuario;
        payload.p_IdModulo = "FAC";
        payload.p_SimularOperacion = "N";
        payload.p_UsaReglasNegocio = "S";
        payload.p_EsIngresoAutomatico = "N";
        payload.p_Glosa = "";
        payload.p_IdAsignacionMasiva = [];
        payload.p_FiltraPorAsignacionMasiva = $scope.data.filtraPorAsignacionMasiva ? "S" : "N";
        
        if($scope.data.filtraPorAsignacionMasiva) {
            let asignacionMasiva = { "p_IdAsignacionMasiva": Number($scope.data.IdAsignacionMasiva) }
            payload.p_IdAsignacionMasiva.push(asignacionMasiva)
        } else {
            payload.p_Fecha = $scope.data.fechaConsulta ? util.dateSPdate($scope.data.fechaConsulta): "";
            payload.p_IdClasificacionTipoInstrumento = $scope.data.IdClasificacionTipoInstrumento || undefined
            payload.p_Operacion= $scope.data.AccionOrden || undefined
            payload.p_IdLibroOrdenes= $scope.data.IdLibroOrdenes || undefined
            payload.p_SecuenciaOrden= Number($scope.data.SecuenciaOrden) || undefined
            payload.p_HoraDesde= util.formatoTime($scope.data.horaDesde)|| undefined
            payload.p_HoraHasta= util.formatoTime($scope.data.horaHasta)|| undefined
            payload.p_IdTipoInstrumento= $scope.data.IdTipoInstrumento || undefined
            payload.p_IdCondicionDeLiquidacion= $scope.data.IdCondicionDeLiquidacion || undefined
            payload.p_Bolsa= $scope.data.IdBolsa || undefined
            payload.p_FolioTransaccion= $scope.data.FolioTransaccion || undefined
            payload.p_IdInstrumento= $scope.data.Instrumento || undefined
            payload.p_IdTipoCanalIngreso= $scope.data.IdTipoCanalIngreso || undefined
            payload.p_IdPortafolio= $scope.data.IdPortafolio || undefined
            payload.p_MarcaAsignacion= $scope.data.MarcaAsignacion || undefined
        }

        return payload
    }

    $scope.anularMovimientosPorAsignacionMasiva = function () {
        let datos = $scope.generarPayload();

        if ($scope.data.filtraPorAsignacionMasiva && (datos.p_IdAsignacionMasiva.length === 0 || datos.p_IdAsignacionMasiva[0].p_IdAsignacionMasiva == 0)) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_ID_ASIGNACION_MASIVA_MAYOR_0");
    		return;
		}       

        if(!$scope.data.filtraPorAsignacionMasiva && !datos.p_Fecha) {
            bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_FECHA_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR");
    		return;
        }        
        
        if(!$scope.data.filtraPorAsignacionMasiva && !datos.p_HoraDesde) {
            bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_HORA_DESDE_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR");
    		return;
        }

        if(!$scope.data.filtraPorAsignacionMasiva && !datos.p_HoraHasta) {
            bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_HORA_HASTA_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR");
    		return;
        }

        let t = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_ESTA_SEGURO_DE_ANULAR_MOVIMIENTOS_A_FACTURAR"), OPCIONES_BCSALERTA.SI_NO);
        t.result.then(function (result) {
            if (result === OPCIONES_BCSALERTA.SI) {
                bcsComunes.ventanaOverlay(idVentana, true);
                bcsServiciosFacturacion.AnularMovimientosPorAsignacionMasiva(datos).$promise.then(function (respuesta) {
                    if(respuesta.P_OUT_MENSAJES[0].codigo === "UI_NO_HAY_MOVIMIENTOS_A_ANULAR") {
                        bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_NO_HAY_MOVIMIENTOS_A_ANULAR");
                        bcsComunes.ventanaOverlay(idVentana, false);
                        return;
                    }

                    if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
                        let mensajeTotal = new Array();
                        $.each(respuesta.P_OUT_MENSAJES, function (key, value) {
                            let objMensaje = {
                                Id: value.id,
                                tipo: value.tipo === "OK" ? "INFORMACION" : value.tipo,
                                codigo: value.codigo,
                                descripcion: $translate.instant(value.descripcion)
                            };
                            mensajeTotal.push(objMensaje);
                        });
                        respuesta.P_OUT_MENSAJES = mensajeTotal;
                        bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_PROCESO_COMPLETADO"), conflictos: util.parseServerResponse(respuesta) }, idVentana);
                        bcsComunes.ventanaOverlay(idVentana, false);

                        if (respuesta.P_OUT_TRANSACCIONES_DESASIGNADAS && respuesta.P_OUT_TRANSACCIONES_DESASIGNADAS.length > 0) {
                            bcsComunes.ventanaAbrir("AsignacionMasivaPrecioPromedio", {transaccionesDesasignadas: respuesta.P_OUT_TRANSACCIONES_DESASIGNADAS}, false, false, idVentana )
                        }                 
                    }
                    else {
                        bcsComunes.ventanaMsg({ mensaje: $translate.instant("UI_ERROR"), conflictos: util.parseServerResponse(respuesta) }, idVentana);
                        bcsComunes.ventanaOverlay(idVentana, false);
                    }
                }, function (respuesta) {
                    bcsComunes.ventanaOverlay(idVentana, false);
                    bcsComunes.msg("error", "Error", "UI_ERROR_MODIFICACION");
                    bcsComunes.setConflictos(util.parseServerResponse(respuesta));
                    bcsComunes.ventanaCerrar(idVentana);
                });
            }
        })
    };

    $scope.buscarInformacionAnulaciones = function() {

        let datos = $scope.generarPayload();

        if ($scope.data.filtraPorAsignacionMasiva && (datos.p_IdAsignacionMasiva.length === 0 || datos.p_IdAsignacionMasiva[0].p_IdAsignacionMasiva == 0)) {
			bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_ID_ASIGNACION_MASIVA_MAYOR_0");
    		return;
		}       

        if(!$scope.data.filtraPorAsignacionMasiva && !datos.p_Fecha) {
            bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_FECHA_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR");
    		return;
        }        
        
        if(!$scope.data.filtraPorAsignacionMasiva && !datos.p_HoraDesde) {
            bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_HORA_DESDE_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR");
    		return;
        }

        if(!$scope.data.filtraPorAsignacionMasiva && !datos.p_HoraHasta) {
            bcsComunes.msg("warning", "UI_ADVERTENCIA", "UI_HORA_HASTA_INVALIDA_ANULACION_MOVIMIENTOS_FACTURAR");
    		return;
        }

        bcsServiciosFacturacion.ConsultarDetalleAnularMovimientos(datos).$promise.then(function (response) {
            if (response && response.P_OUT_ERRORESCONTADOR === 0) {
                bcsComunes.ventanaOverlay(idVentana,false);
                bcsComunes.ventanaAbrir('DetalleAnularMovimientos',{detalleMovimientos : response.P_OUT_DETALLE_MOVIMIENTOS },false, false, idVentana)
                if (response.P_OUT_DETALLE_MOVIMIENTOS.length === 0){
                    bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_MOVIMIENTOS_FACTURAR");
                }
            } else {
                bcsComunes.ventanaOverlay(idVentana, false);
                bcsComunes.ventanaMsg({conflictos: util.parseServerResponse(response)});
                bcsComunes.setConflictos(util.parseServerResponse(response));
            }
        }, function (response) {
            bcsComunes.ventanaOverlay(idVentana, false);
            bcsComunes.ventanaMsg({conflictos: util.parseServerResponse(response)});
            bcsComunes.setConflictos(util.parseServerResponse(response));
        })
    }

    $scope.cancelar = function () {
        bcsComunes.ventanaCerrar(idVentana);
    };

/******************************************************/
/*** WATCH											***/
/******************************************************/

    $scope.$watch('data.IdAsignacionMasiva',function(newval,oldval)
    {	
        if(newval === oldval) {
            return;
        }
        
        if($scope.data.IdAsignacionMasiva) {
            $scope.data.filtraPorAsignacionMasiva = true
        } else {
            $scope.data.filtraPorAsignacionMasiva = false
        }
    });

    $scope.$watch('data.MarcaAsignacion', function (newVal) {
        if (newVal) {
            var limpio = newVal.replace(/[^a-zA-Z0-9_]/g, '');
            if (limpio !== newVal) $scope.data.MarcaAsignacion = limpio;
        }
    });

});