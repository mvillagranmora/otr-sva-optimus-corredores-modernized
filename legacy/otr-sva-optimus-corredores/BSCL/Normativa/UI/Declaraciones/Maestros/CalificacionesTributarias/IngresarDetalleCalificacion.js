angular.module('ModuloNormativa').controller('IngresarDetalleCalificacionCtrl', function ($scope, $translate, bcsComunes, bcsConfiguracionMaestroDeclaraciones, bcsAlerta, bcsServiciosNormativa, OPCIONES_BCSALERTA, $timeout, $http) {

    var idVentana = bcsComunes.ventanaWinId($scope);
    var parametros = bcsComunes.ventanaParams(idVentana);
    parametros = parametros.cabeceraCalificacion;
    var secuenciaEvento = parametros.SEC_EVE;
    var calificacion = {};
    let sumaMontos = 0;
    $scope.options = {};

    var dateFormatYYYYMMDD = dateDDMMYYYY => {
        return moment(dateDDMMYYYY, 'DD-MM-YYYY').format('YYYY-MM-DD');
    }

    $scope.filtros = {
        esIngresoMonto: false,
        FactoresCalculados: false,
        grabarCalificacion: true,
        titulo: '',
        showItem: {
            section1: true,
            section2: true,
            section3: false,
            section4: false
        }
    };

    $scope.options = {};
    $scope.options.factorFormat = { step: 0, format : 'n8', min: 0, max: 1, spinners: false, round: false };

    const truncate = (nuevoValor, option) => {
        if (option) {
            if (nuevoValor != null) {
                nuevoValor = parseFloat(nuevoValor);
                $scope.data.factor08 = (Math.trunc(nuevoValor * 100000000) / 100000000); 
            }
        }
    }

    $scope.FactoresCalculados= [];

    $scope.data = {
        factores: {},
        montos: {},
        antes: {},
    };
    $scope.parametros = parametros;
    $scope.accionBoton = parametros.AccionBoton;
    $scope.mostrarIngreso = {
        mostrarIngresoMonto: true
    };
    var auxMercado = "";
    auxMercado = parametros.MERCADO;
    if(auxMercado === 'FFM'){
        $scope.mostrarIngreso.mostrarIngresoMonto = false;
    };

    var successCrearModificarCalificacion = function (respuesta) {
        var mensajeId = bcsComunes.ventanaMsg({
            mensaje: `Calificacion ${secuenciaEvento} grabada.`,
            conflictos: util.parseServerResponse(respuesta)
        }, idVentana);

        bcsComunes.ventanaPromesa(mensajeId).then(function () {
            bcsComunes.ventanaOverlay(idVentana, false);
            bcsComunes.ventanaData(idVentana, true);
            bcsComunes.ventanaCerrar(idVentana);
        });
    };

    var errorCrearModificarCalificacion = function (respuesta) {
        bcsComunes.msg("error", "error", `Problemas para grabar Calificacion ${secuenciaEvento}`);
        bcsComunes.ventanaOverlay(idVentana, false);
        bcsComunes.ventanaData(idVentana, true);
        bcsComunes.ventanaCerrar(idVentana);
    };

 
    $scope.CalcularFactor = function () {
        try {
            var montos = { // *: Capturados por formulario
                MONTO_08: $scope.data.montos.monto08 || 0,
                MONTO_09: $scope.data.montos.monto09 || 0,
                MONTO_10: $scope.data.montos.monto10 || 0,
                MONTO_11: $scope.data.montos.monto11 || 0,
                MONTO_12: $scope.data.montos.monto12 || 0,
                MONTO_13: $scope.data.montos.monto13 || 0,
                MONTO_14: $scope.data.montos.monto14 || 0,
                MONTO_15: $scope.data.montos.monto15 || 0,
                MONTO_16: $scope.data.montos.monto16 || 0,
                MONTO_17: $scope.data.montos.monto17 || 0,
                MONTO_18: $scope.data.montos.monto18 || 0,
                MONTO_19: $scope.data.montos.monto19 || 0,
                MONTO_20: $scope.data.montos.monto20 || 0,
                MONTO_21: $scope.data.montos.monto21 || 0,
                MONTO_22: $scope.data.montos.monto22 || 0,
                MONTO_23: $scope.data.montos.monto23 || 0,
                MONTO_24: $scope.data.montos.monto24 || 0,
                MONTO_25: $scope.data.montos.monto25 || 0,
                MONTO_26: $scope.data.montos.monto26 || 0,
                MONTO_27: $scope.data.montos.monto27 || 0,
                MONTO_28: $scope.data.montos.monto28 || 0,
                MONTO_29: $scope.data.montos.monto29 || 0,
                MONTO_30: $scope.data.montos.monto30 || 0,
                MONTO_31: $scope.data.montos.monto31 || 0,
                MONTO_32: $scope.data.montos.monto32 || 0,
                MONTO_33: $scope.data.montos.monto33 || 0,
                MONTO_34: $scope.data.montos.monto34 || 0,
                MONTO_35: $scope.data.montos.monto35 || 0,
                MONTO_36: $scope.data.montos.monto36 || 0,
                MONTO_37: $scope.data.montos.monto37 || 0,
                MONTO_38: $scope.data.montos.monto38 || 0,
            }
    
            // * Suma de Monto 8 al 19 para calculo de Factores
            for (var i = 8; i <= 19; i++) {
                sumaMontos += montos['MONTO_' + (i < 10 ? '0' + i : i)];
            }

            $scope.factoresCalculados = { // *: Capturados por formulario
                MONTO_08: $scope.data.montos.monto08 / sumaMontos,
                MONTO_09: $scope.data.montos.monto09 / sumaMontos,
                MONTO_10: $scope.data.montos.monto10 / sumaMontos,
                MONTO_11: $scope.data.montos.monto11 / sumaMontos,
                MONTO_12: $scope.data.montos.monto12 / sumaMontos,
                MONTO_13: $scope.data.montos.monto13 / sumaMontos,
                MONTO_14: $scope.data.montos.monto14 / sumaMontos,
                MONTO_15: $scope.data.montos.monto15 / sumaMontos,
                MONTO_16: $scope.data.montos.monto16 / sumaMontos,
                MONTO_17: $scope.data.montos.monto17 / sumaMontos,
                MONTO_18: $scope.data.montos.monto18 / sumaMontos,
                MONTO_19: $scope.data.montos.monto19 / sumaMontos,
                MONTO_20: $scope.data.montos.monto20 / (sumaMontos + $scope.data.montos.monto20),
                MONTO_21: $scope.data.montos.monto21 / (sumaMontos + $scope.data.montos.monto21),
                MONTO_22: $scope.data.montos.monto22 / (sumaMontos + $scope.data.montos.monto22),
                MONTO_23: $scope.data.montos.monto23 / (sumaMontos + $scope.data.montos.monto23),
                MONTO_24: $scope.data.montos.monto24 / (sumaMontos + $scope.data.montos.monto24),
                MONTO_25: $scope.data.montos.monto25 / (sumaMontos + $scope.data.montos.monto25),
                MONTO_26: $scope.data.montos.monto26 / (sumaMontos + $scope.data.montos.monto26),
                MONTO_27: $scope.data.montos.monto27 / (sumaMontos + $scope.data.montos.monto27),
                MONTO_28: $scope.data.montos.monto28 / (sumaMontos + $scope.data.montos.monto28),
                MONTO_29: $scope.data.montos.monto29 / (sumaMontos + $scope.data.montos.monto29),
                MONTO_30: $scope.data.montos.monto30 / (sumaMontos + $scope.data.montos.monto30),
                MONTO_31: $scope.data.montos.monto31 / (sumaMontos + $scope.data.montos.monto31),
                MONTO_32: $scope.data.montos.monto32 / (sumaMontos + $scope.data.montos.monto32),
                MONTO_33: $scope.data.montos.monto33 / (sumaMontos + $scope.data.montos.monto33),
                MONTO_34: $scope.data.montos.monto34 / (sumaMontos + $scope.data.montos.monto34),
                MONTO_35: $scope.data.montos.monto35,
                MONTO_36: $scope.data.montos.monto36,
                MONTO_37: $scope.data.montos.monto37 / (sumaMontos + $scope.data.montos.monto37),
                MONTO_38: $scope.data.montos.monto38 / (sumaMontos + $scope.data.montos.monto38),
            }
            
            $scope.filtros.FactoresCalculados=true
            $scope.filtros.esIngresoMonto=false
            
        } catch (error) {
            alertError('Calcular factor', error);
        }
    }

    $scope.grabarCalificacion = function () {
        try {

            if ($scope.filtros.FactoresCalculados) {

                calificacion = {
                    EJERCICIO: parametros.EJERCICIO,
                    MERCADO: parametros.MERCADO,
                    NEMO: parametros.NEMO,
                    FEC_PAGO: parametros.FEC_PAGO,
                    SEC_EVE: secuenciaEvento,
                    DESCRIPCION: parametros.DESCRIPCION.trim(),
                    NUM_DIVDO: $scope.data.NumDivdo || parametros.NUM_DIVDO,
                    TIPO_SOC: '',
                    FACTORACT: parametros.FACTORACT,
                    NUMCERTSOC: 0,
                    FechaUltimaMod: '',
                    UsuarioUltimaMod: $scope.SesionUsuario || 'Undefined',
                    AcogidoISFUT: parametros.AcogidoISFUT || 'N',
                    IDORIGEN: parametros.IDORIGEN,
                    
                    FACTOR_06: $scope.data.factor06 || parametros.Factor06 || 0,
                    FACTOR_07: $scope.data.factor07 || parametros.Factor07 || 0,
                    FACTOR_08: Number($scope.factoresCalculados.MONTO_08.toFixed(8)) || 0,
                    FACTOR_09: Number($scope.factoresCalculados.MONTO_09.toFixed(8)) || 0,
                    FACTOR_10: Number($scope.factoresCalculados.MONTO_10.toFixed(8)) || 0,
                    FACTOR_11: Number($scope.factoresCalculados.MONTO_11.toFixed(8)) || 0,
                    FACTOR_12: Number($scope.factoresCalculados.MONTO_12.toFixed(8)) || 0,
                    FACTOR_13: Number($scope.factoresCalculados.MONTO_13.toFixed(8)) || 0,
                    FACTOR_14: Number($scope.factoresCalculados.MONTO_14.toFixed(8)) || 0,
                    FACTOR_15: Number($scope.factoresCalculados.MONTO_15.toFixed(8)) || 0,
                    FACTOR_16: Number($scope.factoresCalculados.MONTO_16.toFixed(8)) || 0,
                    FACTOR_17: Number($scope.factoresCalculados.MONTO_17.toFixed(8)) || 0,
                    FACTOR_18: Number($scope.factoresCalculados.MONTO_18.toFixed(8)) || 0,
                    FACTOR_19: Number($scope.factoresCalculados.MONTO_19.toFixed(8)) || 0,
                    FACTOR_20: Number($scope.factoresCalculados.MONTO_20.toFixed(8)) || 0,
                    FACTOR_21: Number($scope.factoresCalculados.MONTO_21.toFixed(8)) || 0,
                    FACTOR_22: Number($scope.factoresCalculados.MONTO_22.toFixed(8)) || 0,
                    FACTOR_23: Number($scope.factoresCalculados.MONTO_23.toFixed(8)) || 0,
                    FACTOR_24: Number($scope.factoresCalculados.MONTO_24.toFixed(8)) || 0,
                    FACTOR_25: Number($scope.factoresCalculados.MONTO_25.toFixed(8)) || 0,
                    FACTOR_26: Number($scope.factoresCalculados.MONTO_26.toFixed(8)) || 0,
                    FACTOR_27: Number($scope.factoresCalculados.MONTO_27.toFixed(8)) || 0,
                    FACTOR_28: Number($scope.factoresCalculados.MONTO_28.toFixed(8)) || 0,
                    FACTOR_29: Number($scope.factoresCalculados.MONTO_29.toFixed(8)) || 0,
                    FACTOR_30: Number($scope.factoresCalculados.MONTO_30.toFixed(8)) || 0,
                    FACTOR_31: Number($scope.factoresCalculados.MONTO_31.toFixed(8)) || 0,
                    FACTOR_32: Number($scope.factoresCalculados.MONTO_32.toFixed(8)) || 0,
                    FACTOR_33: Number($scope.factoresCalculados.MONTO_33.toFixed(8)) || 0,
                    FACTOR_34: Number($scope.factoresCalculados.MONTO_34.toFixed(8)) || 0,                    
                    FACTOR_35: $scope.factoresCalculados.MONTO_35 ? Number($scope.factoresCalculados.MONTO_35.toFixed(8)) : 0,
                    FACTOR_36: $scope.factoresCalculados.MONTO_36 ? Number($scope.factoresCalculados.MONTO_36.toFixed(8)) : 0,                    
                    FACTOR_37: Number($scope.factoresCalculados.MONTO_37.toFixed(8)) || 0,
                    FACTOR_38: Number($scope.factoresCalculados.MONTO_38.toFixed(8)) || 0,
                };
            } else {

                calificacion = {
                    EJERCICIO: parametros.EJERCICIO,
                    MERCADO: parametros.MERCADO,
                    NEMO: parametros.NEMO,
                    FEC_PAGO: parametros.FEC_PAGO,
                    SEC_EVE: secuenciaEvento,
                    DESCRIPCION: parametros.DESCRIPCION.trim(),
                    NUM_DIVDO: 0 || parametros.NUM_DIVDO,
                    TIPO_SOC: '',
                    FACTORACT: parametros.FACTORACT,
                    NUMCERTSOC: 0,
                    FechaUltimaMod: '',
                    UsuarioUltimaMod: $scope.SesionUsuario || 'Undefined',
                    AcogidoISFUT: parametros.AcogidoISFUT || 'N',
                    IDORIGEN: parametros.IDORIGEN,
                    
                    // ! Factores = Ingresada por form || Registrada por DB
                    FACTOR_06: $scope.data.factor06 || parametros.Factor06 || 0,
                    FACTOR_07: $scope.data.factor07 || parametros.Factor07 || 0,
                    FACTOR_08: $scope.data.factor08 || parametros.Factor08 || 0,
                    FACTOR_09: $scope.data.factor09 || parametros.Factor09 || 0,
                    FACTOR_10: $scope.data.factor10 || parametros.Factor10 || 0,
                    FACTOR_11: $scope.data.factor11 || parametros.Factor11 || 0,
                    FACTOR_12: $scope.data.factor12 || parametros.Factor12 || 0,
                    FACTOR_13: $scope.data.factor13 || parametros.Factor13 || 0,
                    FACTOR_14: $scope.data.factor14 || parametros.Factor14 || 0,
                    FACTOR_15: $scope.data.factor15 || parametros.Factor15 || 0,
                    FACTOR_16: $scope.data.factor16 || parametros.Factor16 || 0,
                    FACTOR_17: $scope.data.factor17 || parametros.Factor17 || 0,
                    FACTOR_18: $scope.data.factor18 || parametros.Factor18 || 0,
                    FACTOR_19: $scope.data.factor19 || parametros.Factor19 || 0,
                    FACTOR_20: $scope.data.factor20 || parametros.Factor20 || 0,
                    FACTOR_21: $scope.data.factor21 || parametros.Factor21 || 0,
                    FACTOR_22: $scope.data.factor22 || parametros.Factor22 || 0,
                    FACTOR_23: $scope.data.factor23 || parametros.Factor23 || 0,
                    FACTOR_24: $scope.data.factor24 || parametros.Factor24 || 0,
                    FACTOR_25: $scope.data.factor25 || parametros.Factor25 || 0,
                    FACTOR_26: $scope.data.factor26 || parametros.Factor26 || 0,
                    FACTOR_27: $scope.data.factor27 || parametros.Factor27 || 0,
                    FACTOR_28: $scope.data.factor28 || parametros.Factor28 || 0,
                    FACTOR_29: $scope.data.factor29 || parametros.Factor29 || 0,
                    FACTOR_30: $scope.data.factor30 || parametros.Factor30 || 0,
                    FACTOR_31: $scope.data.factor31 || parametros.Factor31 || 0,
                    FACTOR_32: $scope.data.factor32 || parametros.Factor32 || 0,
                    FACTOR_33: $scope.data.factor33 || parametros.Factor33 || 0,
                    FACTOR_34: $scope.data.factor34 || parametros.Factor34 || 0,
                    FACTOR_35: $scope.data.factor35 || parametros.Factor35 || 0,
                    FACTOR_36: $scope.data.factor36 || parametros.Factor36 || 0,
                    FACTOR_37: $scope.data.factor37 || parametros.Factor37 || 0,
                    FACTOR_38: $scope.data.factor38 || parametros.Factor38 || 0,
                };

            }

            let factorSum = 0;
                factorSum = (
                    calificacion.FACTOR_08 + calificacion.FACTOR_09 
                    + calificacion.FACTOR_10 + calificacion.FACTOR_11 
                    + calificacion.FACTOR_12 + calificacion.FACTOR_13 
                    + calificacion.FACTOR_14 + calificacion.FACTOR_15 
                    + calificacion.FACTOR_16 + calificacion.FACTOR_17 
                    + calificacion.FACTOR_18 + calificacion.FACTOR_19
                );   
            if (factorSum.toFixed(8) > 1) {
                bcsComunes.msg("error", "error", 'La suma de los factores 08 al 19 no puede ser superior a 1.');
                return;
            }

            if ($scope.filtros.esIngresoMonto && sumaMontos === 0) {
                bcsComunes.msg("error", "error", 'Falta calcular montos.');
                return;
            }

            for (var i = 8; i <= 38; i++) {
                var factorKey = 'FACTOR_' + (i < 10 ? '0' + i : i);
                if(Number(calificacion[factorKey]).toFixed(8) > 1) {
                    bcsComunes.msg("error", "error", factorKey +' no pueden ser mayor a 1.');
                    return;
                };
            }

            var datos = {
                Accion: 'CREAR',
                Calificacion_in: [calificacion],
                p_IdEmpresa: $scope.SesionIdEmpresa,
            	p_IdUsuario: $scope.SesionIdUsuario
            };

            bcsComunes.ventanaOverlay(idVentana, true);
            bcsServiciosNormativa.IngresarCalificacion(datos, successCrearModificarCalificacion, errorCrearModificarCalificacion);                        

        } catch (error) {
            alertError('Grabar calificacion tributaria', error);
            return;
        }
    }

    const alertError = (msg, error) => {
        bcsComunes.msg("error", "error", msg);
        bcsComunes.ventanaMsgErr(`${msg} ${error}`);
    };

    $scope.cancelarIngreso = function () {
        try {
            bcsComunes.ventanaData(idVentana, false);
            bcsComunes.ventanaCerrar(idVentana);
        } catch (error) {
            alertError('Cancelar ingreso', error);
        }
    };


});