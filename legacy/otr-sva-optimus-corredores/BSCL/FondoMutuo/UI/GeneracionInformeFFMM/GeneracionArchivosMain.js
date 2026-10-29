angular.module('ModuloFFM').controller('GeneracionArchivosMainCtrl', function ($scope, $translate, bcsComunes, bcsConfiguracionFFM, bcsServicioFondosMutuos) {

    $scope.filtros = {};
    $scope.forms = {};
    $scope.formData = {};
    $scope.datos = {};
    $scope.winID = bcsComunes.ventanaWinId($scope);

    function obtenerFechaSistema(fechaTexto) {
        var partes = fechaTexto.split('-');

        return new Date(
            parseInt(partes[0], 10),
            parseInt(partes[1], 10) - 1,
            parseInt(partes[2], 10)
        );
    }

    var fechaSistema = obtenerFechaSistema($scope.SesionFechaSistema);
    fechaSistema.setHours(0, 0, 0, 0);

    $scope.filtros.FechaCurse = new Date(fechaSistema);

    var fechaMax = new Date(fechaSistema);
    var fechaMin = new Date(fechaSistema);
    fechaMin.setDate(fechaMax.getDate() - 180);

    $scope.configuracionFechaCurse = {
        min: fechaMin,
        max: fechaMax
    };

    $scope.ingresarInstrumentoFMM = function () {
        $scope.formData.abrirVentanaFFMM({});
    };

    /******************************************************/
    /*** FILTROS										***/
    /******************************************************/
    $scope.oConfiguracionTipoArchivoFFMM = {
        tipo: "combobox-tipo-archivo-ffmm",
        configuracion: {},
        idActual: false,
        seleccionado: false,
        filtros: {
            logic: "or",
            InformeMovFFMM: { field: "IdArchivoFondoMutuo", operator: "eq", value: "INFORME_MOV_FFMM" },
            InformePartFFMM: { field: "IdArchivoFondoMutuo", operator: "eq", value: "INFORME_PARTICIPE_FFMM" }
        }
    };

    $scope.oConfiguracionTipoSolicitud = {
        tipo: "combobox-operacion-ffmm",
        configuracion: {},
        idDefecto: false,
        filtros: {
            TipoSolicitud: { "field": "TipoSolicitud", "operator": "eq", "value": "" }
        }
    };

    $scope.oConfiguracionEstadoFFMM = {
        tipo: "combobox-estado-ffmm",
        configuracion: {},
        idDefecto: false,
        filtros: {
            logic: "or",
            EstadoIgresado: { "field": "IdEstado", "operator": "eq", "value": "ING" },
            EstadoCertificado: { "field": "IdEstado", "operator": "eq", "value": "CRT" },
            EstadoPendiente: { "field": "IdEstado", "operator": "eq", "value": "PEN" }
        }
    };

    $scope.oConfiguracionMonedaFFMM = {
        tipo: "combobox-moneda",
        configuracion: {},
        idActual: false,
        seleccionado: false,
        idDefecto: false,
        filtros: {
            logic: "or",
            MonedaCLP: { "field": "IdMoneda", "operator": "eq", "value": "CLP" },
            MonedaUSD: { "field": "IdMoneda", "operator": "eq", "value": "USD" },
            MonedaEUR: { "field": "IdMoneda", "operator": "eq", "value": "EUR" }
        }
    };

    $scope.formData.filtrosEmisor = {
        IndicadorAGF: { "field": "IndicadorAGF", "operator": "eq", "value": "S" }
    };

    $scope.limpiar = function () {
        $scope.filtros.IdArchivoFondoMutuo = null;
        $scope.filtros.FechaCurse = new Date(fechaSistema);
        $scope.filtros.IdMoneda = null;
        $scope.filtros.IdEmisor = null;
        $scope.filtros.IdEstado = 'ING';
        $scope.filtros.TipoSolicitud = null;
        $scope.filtros.FondoMutuo = null;
    };


    $scope.generarInforme = function () {
        bcsComunes.ventanaOverlay($scope.winID, true);
    
        var tipoArchivo = $scope.filtros.IdArchivoFondoMutuo;
    
        var idTipoPrograma;
        if (tipoArchivo === 'INFORME_MOV_FFMM') {
            idTipoPrograma = 'GENERAR_ARCHIVO_FFMM_MOVIMIENTOS';
        } else if (tipoArchivo === 'INFORME_PARTICIPE_FFMM') {
            idTipoPrograma = 'GENERAR_ARCHIVO_FFMM_PARTICIPES';
        }

        var parametros = [{
            IdInterno: 0,
            NombreParametro: 'IdEmpresa',
            Valor: $scope.SesionIdEmpresa
        }, {
            IdInterno: 0,
            NombreParametro: 'IdUsuario',
            Valor: $scope.SesionIdUsuario
        }, {
            IdInterno: 0,
            NombreParametro: 'FechaCurse',
            Valor: util.dateSPseconddate($scope.filtros.FechaCurse)
        }, {
            IdInterno: 0,
            NombreParametro: 'IdArchivoFondoMutuo',
            Valor: tipoArchivo
        }, {
            IdInterno: 0,
            NombreParametro: 'TipoSolicitud',
            Valor: $scope.filtros.TipoSolicitud
        }, {
            IdInterno: 0,
            NombreParametro: 'IdEmisor',
            Valor: $scope.filtros.IdEmisor ? $scope.filtros.IdEmisor : null
        }, {
            IdInterno: 0,
            NombreParametro: 'FondoMutuo',
            Valor: $scope.filtros.FondoMutuo ? $scope.filtros.FondoMutuo.IdSerieFondo : null
        }, {
            IdInterno: 0,
            NombreParametro: 'EstadoMovimiento',
            Valor: $scope.filtros.IdEstado ? $scope.filtros.IdEstado : null
        }];

        if (tipoArchivo === 'INFORME_MOV_FFMM') {
            parametros.push({
                IdInterno: 0,
                NombreParametro: 'IdMoneda',
                Valor: $scope.filtros.IdMoneda ? $scope.filtros.IdMoneda : null
            });
        }
    
        var datos = {
            p_IdEmpresa: $scope.SesionIdEmpresa,
            p_IdUsuario: $scope.SesionIdUsuario,
            p_IdModulo: 'FMM',
            p_TipoPrograma: [{
                IdInterno: 0,
                IdTipoPrograma: idTipoPrograma,
                SegundosDesfase: 0
            }],
            p_Parametro: parametros
        };

        bcsServicioFondosMutuos.InscribirProcesoBatch(datos).$promise.then(
            function (result) {
                bcsComunes.ventanaOverlay($scope.winID.ventanaId, false);
                var fechaLocal = util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion, "FechaHora", "CALL");
                bcsComunes.ventanaMsg({
                    mensaje: $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO')
                        .replace("::IdPrograma", result.P_OUT_PROGRAMA[0].IdPrograma)
                        .replace("::FechaEjecucion", util.formatoSecondate(fechaLocal)),
                    conflictos: util.parseServerResponse(result)
                }, $scope.winID);
            },
            function (result) {
                bcsComunes.ventanaOverlay($scope.winID, false);
                bcsComunes.msg("error", "Error", $translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
            }
        );
    };
});