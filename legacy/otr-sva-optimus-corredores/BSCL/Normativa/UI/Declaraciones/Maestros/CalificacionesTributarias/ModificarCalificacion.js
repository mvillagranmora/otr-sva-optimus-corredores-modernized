angular.module('ModuloNormativa').controller('IngresarModificarCalificacionCtrl', function ($scope, $translate, bcsComunes, bcsConfiguracionMaestroDeclaraciones, bcsAlerta, bcsServiciosNormativa, OPCIONES_BCSALERTA, $timeout, $http) {

	$scope.formData = {};
	$scope.dataTemplate = {};
	$scope.filtros = {};
	$scope.formData = {};
	$scope.datos = {};
	$scope.datosCalificacion = {};
	$scope.datosCalificacion.datosDespues = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.configuraciones = {};
	$scope.configuraciones.date = {
		start: "decade",
		depth: "decade",
		format: "yyyy"
	}
    $scope.configuraciones.factorFormat = { step: 0, format : 'n8', min: 0, max: 1, spinners: false, round: false };

    var dateFormatYYYYMMDD = dateDDMMYYYY => {
        return moment(dateDDMMYYYY, 'DD-MM-YYYY').format('YYYY-MM-DD');
    }

	$scope.parametros = bcsComunes.ventanaParams($scope.winID);

	$scope.datosCalificacion.datosDespues.factor06 = Number($scope.parametros.calificacion.Factor06);
	$scope.datosCalificacion.datosDespues.factor07 = Number($scope.parametros.calificacion.Factor07);
	$scope.datosCalificacion.datosDespues.factor08 = Number($scope.parametros.calificacion.Factor08);
	$scope.datosCalificacion.datosDespues.factor09 = Number($scope.parametros.calificacion.Factor09);
	$scope.datosCalificacion.datosDespues.factor10 = Number($scope.parametros.calificacion.Factor10);
	$scope.datosCalificacion.datosDespues.factor11 = Number($scope.parametros.calificacion.Factor11);
	$scope.datosCalificacion.datosDespues.factor12 = Number($scope.parametros.calificacion.Factor12);
	$scope.datosCalificacion.datosDespues.factor13 = Number($scope.parametros.calificacion.Factor13);
	$scope.datosCalificacion.datosDespues.factor14 = Number($scope.parametros.calificacion.Factor14);
	$scope.datosCalificacion.datosDespues.factor15 = Number($scope.parametros.calificacion.Factor15);
	$scope.datosCalificacion.datosDespues.factor16 = Number($scope.parametros.calificacion.Factor16);
	$scope.datosCalificacion.datosDespues.factor17 = Number($scope.parametros.calificacion.Factor17);
	$scope.datosCalificacion.datosDespues.factor18 = Number($scope.parametros.calificacion.Factor18);
	$scope.datosCalificacion.datosDespues.factor19 = Number($scope.parametros.calificacion.Factor19);
	$scope.datosCalificacion.datosDespues.factor20 = Number($scope.parametros.calificacion.Factor20);
	$scope.datosCalificacion.datosDespues.factor21 = Number($scope.parametros.calificacion.Factor21);
	$scope.datosCalificacion.datosDespues.factor22 = Number($scope.parametros.calificacion.Factor22);
	$scope.datosCalificacion.datosDespues.factor23 = Number($scope.parametros.calificacion.Factor23);
	$scope.datosCalificacion.datosDespues.factor24 = Number($scope.parametros.calificacion.Factor24);
	$scope.datosCalificacion.datosDespues.factor25 = Number($scope.parametros.calificacion.Factor25);
	$scope.datosCalificacion.datosDespues.factor26 = Number($scope.parametros.calificacion.Factor26);
	$scope.datosCalificacion.datosDespues.factor27 = Number($scope.parametros.calificacion.Factor27);
	$scope.datosCalificacion.datosDespues.factor28 = Number($scope.parametros.calificacion.Factor28);
	$scope.datosCalificacion.datosDespues.factor29 = Number($scope.parametros.calificacion.Factor29);
	$scope.datosCalificacion.datosDespues.factor30 = Number($scope.parametros.calificacion.Factor30);
	$scope.datosCalificacion.datosDespues.factor31 = Number($scope.parametros.calificacion.Factor31);
	$scope.datosCalificacion.datosDespues.factor32 = Number($scope.parametros.calificacion.Factor32);
	$scope.datosCalificacion.datosDespues.factor33 = Number($scope.parametros.calificacion.Factor33);
	$scope.datosCalificacion.datosDespues.factor34 = Number($scope.parametros.calificacion.Factor34);
	$scope.datosCalificacion.datosDespues.factor35 = Number($scope.parametros.calificacion.Factor35);
	$scope.datosCalificacion.datosDespues.factor36 = Number($scope.parametros.calificacion.Factor36);
	$scope.datosCalificacion.datosDespues.factor37 = Number($scope.parametros.calificacion.Factor37);
	$scope.datosCalificacion.datosDespues.factor38 = Number($scope.parametros.calificacion.Factor38);

	$scope.datosCalificacion.ejercicio = $scope.parametros.calificacion.Ejercicio;
	$scope.datosCalificacion.secuenciaEvento = $scope.parametros.calificacion.SecuenciaEvento;
	$scope.datosCalificacion.nemotecnico = $scope.parametros.calificacion.Nemo;
	$scope.datosCalificacion.nombreMercado = $scope.parametros.calificacion.Mercado;
    $scope.datosCalificacion.usuarioUltimaMod = $scope.SesionUsuario || 'Undefined',
	$scope.datosCalificacion.descripcion = ($scope.parametros.calificacion.Descripcion).toString().trim();
	$scope.datosCalificacion.fechaPago = dateFormatYYYYMMDD(util.formatoDate($scope.parametros.calificacion.FechaPago));
    $scope.datosCalificacion.origen = $scope.parametros.calificacion.IdOrigen;
	$scope.datosCalificacion.valorHistorico = $scope.parametros.calificacion.ValorHistorico || '0';
	$scope.datosCalificacion.eventoCapital = $scope.parametros.calificacion.IdEventoCapital;

	$scope.ajustaVentana = function () {
		bcsComunes.ventanaTamanoAutomatico($scope.winID, 40);
	}

    var successModificarCalificacion = function (respuesta) {
        var mensajeId = bcsComunes.ventanaMsg({
            mensaje: `Calificacion modificada.`,
            conflictos: util.parseServerResponse(respuesta)
        }, $scope.winID);

        bcsComunes.ventanaPromesa(mensajeId).then(function () {
            bcsComunes.ventanaOverlay($scope.winID, false);
            bcsComunes.ventanaData($scope.winID, true);
        });
    };

    var errorModificarCalificacion = function (respuesta) {
        
        bcsComunes.msg("error", "error", respuesta.statusText);
    
        var mensajeId = bcsComunes.ventanaMsg({
            mensaje: `Error modificar calificacion.`,
            conflictos: util.parseServerResponse(respuesta)
        }, $scope.winID);

        bcsComunes.ventanaPromesa(mensajeId).then(function () {
            bcsComunes.ventanaOverlay($scope.winID, false);
            bcsComunes.ventanaData($scope.winID, true);
        });
    };

	$scope.modificarCalificacionTributaria = function () {
        calificacion = {
            EJERCICIO: Number($scope.datosCalificacion.ejercicio),
            MERCADO: $scope.datosCalificacion.nombreMercado,
            NEMO: $scope.datosCalificacion.nemotecnico,
            FEC_PAGO: $scope.datosCalificacion.fechaPago,
            SEC_EVE: Number($scope.datosCalificacion.secuenciaEvento),
            DESCRIPCION: $scope.datosCalificacion.descripcion,
            NUM_DIVDO: $scope.parametros.calificacion.NumeroDividendo,
            TIPO_SOC: $scope.parametros.calificacion.TipoSociedad,
            FACTORACT: $scope.parametros.calificacion.FactorAct,
            NUMCERTSOC: $scope.parametros.calificacion.NumCertSoc || 0,
            FechaUltimaMod: '',
            UsuarioUltimaMod: $scope.SesionUsuario || 'Undefined',
            AcogidoISFUT: $scope.parametros.calificacion.AcogidoISFUT || 'N',
            IDORIGEN: $scope.datosCalificacion.origen, 
            FACTOR_06: $scope.datosCalificacion.datosDespues.factor06,
            FACTOR_07: $scope.datosCalificacion.datosDespues.factor07,
            FACTOR_08: $scope.datosCalificacion.datosDespues.factor08,
            FACTOR_09: $scope.datosCalificacion.datosDespues.factor09,
            FACTOR_10: $scope.datosCalificacion.datosDespues.factor10,
            FACTOR_11: $scope.datosCalificacion.datosDespues.factor11,
            FACTOR_12: $scope.datosCalificacion.datosDespues.factor12,
            FACTOR_13: $scope.datosCalificacion.datosDespues.factor13,
            FACTOR_14: $scope.datosCalificacion.datosDespues.factor14,
            FACTOR_15: $scope.datosCalificacion.datosDespues.factor15,
            FACTOR_16: $scope.datosCalificacion.datosDespues.factor16,
            FACTOR_17: $scope.datosCalificacion.datosDespues.factor17,
            FACTOR_18: $scope.datosCalificacion.datosDespues.factor18,
            FACTOR_19: $scope.datosCalificacion.datosDespues.factor19,
            FACTOR_20: $scope.datosCalificacion.datosDespues.factor20,
            FACTOR_21: $scope.datosCalificacion.datosDespues.factor21,
            FACTOR_22: $scope.datosCalificacion.datosDespues.factor22,
            FACTOR_23: $scope.datosCalificacion.datosDespues.factor23,
            FACTOR_24: $scope.datosCalificacion.datosDespues.factor24,
            FACTOR_25: $scope.datosCalificacion.datosDespues.factor25,
            FACTOR_26: $scope.datosCalificacion.datosDespues.factor26,
            FACTOR_27: $scope.datosCalificacion.datosDespues.factor27,
            FACTOR_28: $scope.datosCalificacion.datosDespues.factor28,
            FACTOR_29: $scope.datosCalificacion.datosDespues.factor29,
            FACTOR_30: $scope.datosCalificacion.datosDespues.factor30,
            FACTOR_31: $scope.datosCalificacion.datosDespues.factor31,
            FACTOR_32: $scope.datosCalificacion.datosDespues.factor32,
            FACTOR_33: $scope.datosCalificacion.datosDespues.factor33,
            FACTOR_34: $scope.datosCalificacion.datosDespues.factor34,
            FACTOR_35: $scope.datosCalificacion.datosDespues.factor35,
            FACTOR_36: $scope.datosCalificacion.datosDespues.factor36,
            FACTOR_37: $scope.datosCalificacion.datosDespues.factor37,
            FACTOR_38: $scope.datosCalificacion.datosDespues.factor38,
        };

        let factorSum = 0;
        factorSum = (
            calificacion.FACTOR_08 + calificacion.FACTOR_09 
            + calificacion.FACTOR_10 + calificacion.FACTOR_11 
            + calificacion.FACTOR_12 + calificacion.FACTOR_13 
            + calificacion.FACTOR_14 + calificacion.FACTOR_15 
            + calificacion.FACTOR_16 + calificacion.FACTOR_17 
            + calificacion.FACTOR_18 + calificacion.FACTOR_19 );

        if (factorSum > 1) {
            bcsComunes.msg("error", "error", 'La suma de los factores 08 al 19 no puede ser superior a 1.');
            return;
        }

        var datos = {
            Accion: 'UPDATE',
            Calificacion_in: [calificacion],
            p_IdEmpresa: $scope.SesionIdEmpresa,
            p_IdUsuario: $scope.SesionIdUsuario
        };

        console.log('datos', datos);

		bcsComunes.ventanaOverlay($scope.winID, true);
        // bcsServiciosNormativa.IngresarCalificacion(datos, successModificarCalificacion, errorModificarCalificacion);
        bcsServiciosNormativa.IngresarCalificacion(datos).$promise.then(function (respuesta) {

			if (respuesta.mensaje === "OK") {
				var id = bcsComunes.ventanaMsg({ mensaje: 'UI_OK_MODIFICAR_CALIFICACION_TRIBUTARIA', conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
				bcsConfiguracionMaestroDeclaraciones.ingresarCerrarVentana(id, $scope.winID);
			} else {
				bcsComunes.ventanaMsg({ mensaje: 'Error al modificar calificacion tributaria', conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
			}
		}, function (respuesta) {
			bcsComunes.ventanaMsg({ mensaje: 'Error al modificar calificacion tributaria', conflictos: util.parseServerResponse(respuesta) }, $scope.winID);
		});
       
	};

	bcsComunes.ventanaTamanoAutomatico($scope.winID, 10);
    
    $scope.descripcion = function(id,traduccion){
        var array = [{Estado: $translate.instant(traduccion), Color:'red'}];
        bcsComunes.tootipGlosa(id,array);
    };
       

});