angular.module('ModuloNormativa').controller('IngresarCalificacionCtrl', function ($scope, $translate, bcsComunes, bcsConfiguracionMaestroDeclaraciones, bcsAlerta, bcsServiciosNormativa, OPCIONES_BCSALERTA, $timeout, $http) {
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
    
    var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalificacionTributaria();
	$scope.defaultOptions = conf.options;
	$scope.buscarCalificacionesTributarias = conf.buscar;

	$scope.filtros = {};
    $scope.formData = {};
	$scope.dataTemplate = {};
	$scope.filtros.datosAntes = {};
	$scope.filtros.datosDespues = {};
    $scope.filtros.accion = $scope.parametros.Accion;
    var parametros = $scope.parametros.Calificacion;
	
	$scope.formData = {};
	$scope.datos = {};
	$scope.datosCalificacion = {};

	$scope.datosCalificacion.datosDespues = {};

	$scope.configuraciones = {};
	$scope.configuraciones.date = {
		start: "decade",
		depth: "decade",
		format: "yyyy"
	}

    var dateFormatYYYYMMDD = dateDDMMYYYY => {
        return moment(dateDDMMYYYY, 'DD-MM-YYYY').format('YYYY-MM-DD');
    }

    if ($scope.filtros.accion == 'ingresar') {
        $scope.datosCalificacion.NombreMercado = $scope.parametros.Mercado;
        $scope.datosCalificacion.Nemo = $scope.datosCalificacion.Nemo;
        $scope.datosCalificacion.Descripcion = $scope.datosCalificacion.Descripcion || '';
        $scope.datosCalificacion.Secuencia = $scope.parametros.SecuenciaEvento;
        $scope.datosCalificacion.ValorHistorico = $scope.datosCalificacion.ValorHistorico || 0;
        $scope.datosCalificacion.Anio = $scope.parametros.PeriodoComercial;
        $scope.datosCalificacion.Isfut = $scope.datosCalificacion.AcogidoISFUT || 'N';
        $scope.datosCalificacion.Dividendo = 0;
        $scope.datosCalificacion.FactorActualizado = 0;
        $scope.datosCalificacion.FechaPago = util.formatoDate($scope.datosCalificacion.FechaPago);

        $scope.$watch('datosCalificacion.FechaPago', function(newFechaPago, oldFechaPago) {
            if (newFechaPago !== oldFechaPago && newFechaPago) {
                $scope.datosCalificacion.FactorActualizado = calcularNuevoFactor(newFechaPago);
            }
        });

        const calcularNuevoFactor = fechaPago => {            
            let fechaPagoFormateada = moment(fechaPago, 'DD-MM-YYYY').format('YYYY-MM')

            let fechaAntigua = (moment(fechaPago, 'DD-MM-YYYY').subtract(12, 'months')).format('YYYY-MM');

            const ipcActual = $scope.parametros.DataSourceIpc.find(item => {
                return item.AgnoMes === fechaPagoFormateada;
            });

            const ipcAntiguo = $scope.parametros.DataSourceIpc.find(item => {
                return item.AgnoMes === fechaAntigua;
            });

            if(!ipcActual || !ipcAntiguo || ipcActual.length == 0 || ipcAntiguo.length == 0) {
                bcsComunes.msg("error", "error", `No se pudo calcular Factor Actualizado. Revisar Fecha de Pago`);
                return;
            };

            let factorAct = parseFloat((ipcActual.Valor / ipcAntiguo.Valor).toFixed(2));

            return factorAct; 
        }
    } 

    if ($scope.filtros.accion == 'copiar') {
        $scope.datosCalificacion.NombreMercado = parametros.Mercado;
        $scope.datosCalificacion.Nemo = parametros.Nemo;
        $scope.datosCalificacion.Descripcion = parametros.Descripcion;
        $scope.datosCalificacion.Secuencia = parametros.SecuenciaEvento;
        $scope.datosCalificacion.Dividendo = parametros.NumeroDividendo;
        $scope.datosCalificacion.ValorHistorico = parametros.ValorHistorico || '';
        $scope.datosCalificacion.Anio = parametros.Ejercicio || '';
        $scope.datosCalificacion.Isfut = parametros.AcogidoISFUT || 'N';
        $scope.datosCalificacion.FactorActualizado = parametros.FactorAct;
        $scope.datosCalificacion.FechaPagoCopiar = dateFormatYYYYMMDD(util.formatoDate($scope.parametros.Calificacion.FechaPago));
        $scope.checked = parametros.AcogidoISFUT === 'S' ? true : false; 
    }

    var secuenciaEvento = $scope.parametros.SecuenciaEvento || $scope.datosCalificacion.Secuencia;

    var successGrabarCalificacion = function (respuesta) {
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

    var errorGrabarCalificacion = function (respuesta) {
        bcsComunes.msg("error", "error", `Problemas para grabar Calificacion ${secuenciaEvento}`);
        bcsComunes.ventanaOverlay(idVentana, false);
        bcsComunes.ventanaData(idVentana, true);
    };

    var alertaCamposFaltantes = function (respuesta) {
        bcsComunes.msg("warning", "warning", `Faltan campos para ingresar la calificacion ${secuenciaEvento}`);
        bcsComunes.ventanaOverlay(idVentana, false);
        bcsComunes.ventanaData(idVentana, true);
    };

	$scope.ajustaVentana = function () {
		bcsComunes.ventanaTamanoAutomatico($scope.winID, 80);
	}

	$scope.ingresarCalificacionTributaria = function () {
        cabeceraCalificacion = {
            EJERCICIO: $scope.parametros.PeriodoComercial,
            MERCADO: $scope.parametros.Mercado,
            NEMO: $scope.datosCalificacion.Nemo,
            FEC_PAGO: dateFormatYYYYMMDD($scope.datosCalificacion.FechaPago),
            FACTORACT: $scope.datosCalificacion.FactorActualizado,
            SEC_EVE: $scope.parametros.SecuenciaEvento,
            IDORIGEN: $scope.parametros.IdOrigen,
            DESCRIPCION: $scope.datosCalificacion.Descripcion,
            NUM_DIVDO: Number($scope.datosCalificacion.Dividendo),
            AcogidoISFUT: $scope.checked ? 'S' : 'N',
        }
        
        if(Number(cabeceraCalificacion.FEC_PAGO.substring(0, 4)) !== Number($scope.parametros.PeriodoComercial)) {
            bcsComunes.msg("warning", "warning", `Periodo de Fecha de pago no puede ser diferente a Ejercicio.`);
            return;
        }        
              
        if(!Object.values($scope.datosCalificacion).some(val => val === undefined || val === null || val === "")){
            var ventanaModalId = bcsComunes.ventanaAbrir("IngresarDetalleCalificacion", { cabeceraCalificacion }, true, false);
            var promesa = bcsComunes.ventanaPromesa(ventanaModalId);

            $.when(promesa).then(function (data) {
                bcsComunes.ventanaCerrar(idVentana);
            });
        } else {
            alertaCamposFaltantes();
        }
    }

    $scope.copiarCalificacionTributaria = function () {

        calificacion = {
            EJERCICIO: parametros.Ejercicio,
            MERCADO: parametros.Mercado, // TODO: Revisar, hay un cambio respecto a la copia BCS
            NEMO: parametros.Nemo,
            FEC_PAGO: dateFormatYYYYMMDD(util.formatoDate(parametros.FechaPago)),
            SEC_EVE: $scope.datosCalificacion.Secuencia,
            DESCRIPCION: parametros.Descripcion.trim(),
            NUM_DIVDO: parametros.NumeroDividendo,
            TIPO_SOC:  parametros.TipoSociedad,
            FACTORACT: parametros.FactorAct,
            NUMCERTSOC: 0, // TODO: Se debe agregar a View y MaestroDeclaraciones
            FechaUltimaMod: '', // Manejado por DB
            UsuarioUltimaMod: $scope.SesionUsuario || 'Undefined',
            AcogidoISFUT: parametros.AcogidoISFUT || 'N',
            IDORIGEN: $scope.parametros.IdOrigen,

            FACTOR_06: parametros.Factor06 || 0,
            FACTOR_07: parametros.Factor07 || 0,
            FACTOR_08: parametros.Factor08 || 0,
            FACTOR_09: parametros.Factor09 || 0,
            FACTOR_10: parametros.Factor10 || 0,
            FACTOR_11: parametros.Factor11 || 0,
            FACTOR_12: parametros.Factor12 || 0,
            FACTOR_13: parametros.Factor13 || 0,
            FACTOR_14: parametros.Factor14 || 0,
            FACTOR_15: parametros.Factor15 || 0,
            FACTOR_16: parametros.Factor16 || 0,
            FACTOR_17: parametros.Factor17 || 0,
            FACTOR_18: parametros.Factor18 || 0,
            FACTOR_19: parametros.Factor19 || 0,
            FACTOR_20: parametros.Factor20 || 0,
            FACTOR_21: parametros.Factor21 || 0,
            FACTOR_22: parametros.Factor22 || 0,
            FACTOR_23: parametros.Factor23 || 0,
            FACTOR_24: parametros.Factor24 || 0,
            FACTOR_25: parametros.Factor25 || 0,
            FACTOR_26: parametros.Factor26 || 0,
            FACTOR_27: parametros.Factor27 || 0,
            FACTOR_28: parametros.Factor28 || 0,
            FACTOR_29: parametros.Factor29 || 0,
            FACTOR_30: parametros.Factor30 || 0,
            FACTOR_31: parametros.Factor31 || 0,
            FACTOR_32: parametros.Factor32 || 0,
            FACTOR_33: parametros.Factor33 || 0,
            FACTOR_34: parametros.Factor34 || 0,
            FACTOR_35: parametros.Factor35 || 0,
            FACTOR_36: parametros.Factor36 || 0,
            FACTOR_37: parametros.Factor37 || 0,
            FACTOR_38: parametros.Factor38 || 0,
        };
        
        var datos = {
            Accion: 'CREAR',
            Calificacion_in: [calificacion],
            p_IdEmpresa: $scope.SesionIdEmpresa,
            p_IdUsuario: $scope.SesionIdUsuario
        };

        console.log('datos', datos);

        var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_COPIAR_CALIFICACION_TRIBUTARIA'),idVentana);
        t.then(function(result) {		
            if (result === OPCIONES_BCSALERTA.SI){
                bcsComunes.ventanaOverlay(idVentana, true);
                bcsServiciosNormativa.IngresarCalificacion(datos, successGrabarCalificacion, errorGrabarCalificacion);
            } else {
                bcsComunes.ventanaData(idVentana, false);
		        bcsComunes.ventanaCerrar(idVentana);
            }
        });	

	};

	$scope.buscarDatosCalificacion = function () {
		if (Object.keys($scope.datosCalificacion).length === 0) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), 'Debe ingresar al menos uno de los campos: Mercado, Instrumento o Fecha de Pago');
			return;
		}
		var ventanaModalId = bcsComunes.ventanaAbrir("BuscarCalificacion", {});
	}

	$scope.Cancelar = function () {
		bcsComunes.ventanaData(idVentana, false);
		bcsComunes.ventanaCerrar(idVentana);
	};

	bcsComunes.ventanaTamanoAutomatico($scope.winID, 10);
});