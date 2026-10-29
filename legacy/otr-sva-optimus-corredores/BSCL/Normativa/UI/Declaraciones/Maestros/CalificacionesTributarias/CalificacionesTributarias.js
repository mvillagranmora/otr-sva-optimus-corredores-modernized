angular.module('ModuloNormativa').controller('CalificacionesTributariasCtrl', function ($scope, $translate, bcsComunes, bcsConfiguracionMaestroDeclaraciones, bcsAlerta, bcsServiciosNormativa, OPCIONES_BCSALERTA, $timeout, $http, $resource) {
    var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.parametros = bcsComunes.ventanaParams($scope.winID);
    var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalificacionTributaria();
	$scope.defaultOptions = conf.options;
	$scope.buscarCalificacionesTributarias = conf.buscar;

    $scope.calificacion = {};
	$scope.formData = {};
	$scope.dataTemplate = {};
	$scope.filtros = {};
	$scope.formData = {};
	$scope.datos = {};
	$scope.esBolsa = '';
	
	$scope.filtros.activado = true;
	$scope.filtros.itemPostFiltro = 1;
	$scope.filtros.esCalificacionPendiente = false;
    $scope.filtros.validarBotonIngresar = '';
	$scope.filtros.validarBotonModificar = '';
	$scope.filtros.validarBotonEliminar = '';
	$scope.filtros.validarBotonCopiar = '';
	$scope.filtros.validarBotonCargar = '';

	$scope.defaultOptions.gridCreated = function (grid) {
		$scope.ajustaVentana();
	};

	$scope.filtrarCombo = function () {
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=55669999";
		var r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta, } });
		var post = r.urlConsulta();
		return post.$promise;
	};

	$scope.filtrarCombo().then(function (result) {
		$scope.esBolsa = result.d.results[0].CodigoCorredora === 11 ? 'S' : 'N';

		if ($scope.esBolsa === 'S') {
			$scope.filtros.activado = false;
			$scope.filtros.itemPostFiltro = 2;
		} else {
			$scope.filtros.itemPostFiltro = 3;
		}
	});

	$scope.ajustaVentana = function () {
		bcsComunes.ventanaTamanoAutomatico($scope.winID, 40);
	};

    var dateFormatYYYYMMDD = dateDDMMYYYY => {
        return moment(dateDDMMYYYY, 'DD-MM-YYYY').format('YYYY-MM-DD');
    };

	$scope.validarOrigen = function (calificacion) {
        $scope.calificacion = calificacion[0];
        const { TipoCalificacion, IdOrigen } = $scope.calificacion;

        $scope.filtros.validarBotonIngresar = 'true';
        $scope.filtros.validarBotonCargar = 'true';

		if ($scope.esBolsa === 'N') { 

			if ((TipoCalificacion === 'INGRESADA' || TipoCalificacion === 'PENDIENTE') && IdOrigen === 'BCS' || IdOrigen === '') {
				$scope.filtros.validarBotonModificar = '';
				$scope.filtros.validarBotonEliminar = '';
				$scope.filtros.validarBotonCopiar = 'true';
			}  
            
            if (IdOrigen === 'COR') {
                if (TipoCalificacion === 'INGRESADA') {
                    $scope.filtros.validarBotonModificar = 'true'; 
				    $scope.filtros.validarBotonEliminar = 'true';
				    $scope.filtros.validarBotonCopiar = '';
                }

                if (TipoCalificacion === 'PENDIENTE') {
                    $scope.filtros.validarBotonModificar = ''; 
				    $scope.filtros.validarBotonEliminar = '';
				    $scope.filtros.validarBotonCopiar = 'true';
                }
            }            
        }

        if ($scope.esBolsa === 'S') { 

			if (IdOrigen === 'BCS') {
				$scope.filtros.validarBotonModificar = 'true';
				$scope.filtros.validarBotonEliminar = 'true';
				$scope.filtros.validarBotonCopiar = '';
			}

            if(IdOrigen === 'COR') {
                $scope.filtros.validarBotonModificar = '';
				$scope.filtros.validarBotonEliminar = '';
				$scope.filtros.validarBotonCopiar = '';
            }
		}
	};

	$scope.modificarCalificacionTributaria = function (calificacion) {
		$scope.filtros.validarBotonModificar = '';
		if (!calificacion || calificacion.length == 0) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_HAY_CALIFICACION_SELECCIONADA'));
			return;
		}
		if (calificacion[0].IdCalificacionTributaria === 0) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_EXISTE_CALIFICACION_EDITAR'));
			return;
		}
		var id = bcsComunes.ventanaAbrir("ModificarCalificacion", { calificacion: calificacion[0] });
		var promesa = bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function (data) {
			conf.options.dataSource.read();
			$scope.datos.kNgModel = undefined;
		});
	};

	$scope.anexo = function (calificacion) {
		bcsComunes.ventanaAbrir("AnexoCalificacionesTributarias", {});
	};

	$scope.agregarCalificacionTributaria = function (calificacion) {

		if ($scope.filtros.PeriodoComercial === false) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_HAY_PERIODO_COMERCIAL_SELECCIONADO'));
			return;
		}

		if (!calificacion || calificacion.length == 0) {
			bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_HAY_CALIFICACION_SELECCIONADA'));
			return;
		}

		var id = bcsComunes.ventanaAbrir("IngresarCalificacion", { Calificacion: calificacion[0], Accion: 'copiar', IdOrigen: $scope.esBolsa === "S" ? 'BCS' : 'COR' });
		var promesa = bcsComunes.ventanaPromesa(id);
		$.when(promesa).then(function (data) {
			conf.options.dataSource.read();
			$scope.datos.kNgModel = undefined;
		});
	};

    const obtenerSecuencia = () => {
        try{
            return new Promise((resolve, reject) => {
                var datos = {};
                datos.p_IdEmpresa = $scope.SesionIdEmpresa;
                datos.p_IdUsuario = $scope.SesionIdUsuario;
                datos.p_IdModulo = 'NOR';        
                datos.p_IdTipoEventoCapital = '';
                datos.p_NumeroDividendo = 0;
        
                bcsServiciosNormativa.ConsultaSecuenciaEvento(datos, function(respuesta) {
                    if (respuesta.P_OUT_ERRORESCONTADOR == "0" && respuesta.P_OUT_ADVERTENCIASCONTADOR == "0") {
                        resolve(respuesta.P_OUT_SECUENCIAEVENTOCAPITAL);
                    } else {
                        reject('Secuencia sin valor');
                    }
                });
            });
        } catch (error) {
            throw new Error("No se pudo obtener secuencia incremental: ", error);
        }
    };
    
    $scope.obtenerAsignarSecuencia = function() {
        obtenerSecuencia()
        .then((secuencia) => {
            $scope.secuenciaObtenida=secuencia;
        })
        .catch((error) => {
            console.log(error);
        });
    };

    async function asignarSecuencia() {
        try {
            let secuencia = await obtenerSecuencia();
            return secuencia;
        } catch (error) {
            throw new Error("No se pudo asignar secuencia incremental:", error);
        }
    };

    $scope.ingresarCalificacionTributaria = async function (calificacion) {
        try {
            let secuenciaEvento = await asignarSecuencia();
            const dataSourceIpc = await getDataSourceIpc();
            let filterValue = $scope.filtros.PeriodoComercial;
            const nemos = await getNemos(filterValue);

            if ($scope.filtros.PeriodoComercial === false) {
                bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_HAY_PERIODO_COMERCIAL_SELECCIONADO'));
                return;
            }

            if ($scope.filtros.IdClasificacionTipoInstrumento === 0) {
                bcsComunes.msg('error', $translate.instant("UI_ERROR"), 'Debe seleccionar un tipo de Mercado.');
                return;
            }

            var id = bcsComunes.ventanaAbrir("IngresarCalificacion", 
            {   
                Accion: 'ingresar',
                SecuenciaEvento: secuenciaEvento, 
                PeriodoComercial: $scope.filtros.PeriodoComercial,
                Mercado: $scope.filtros.IdClasificacionTipoInstrumento,
                IdOrigen: $scope.esBolsa === "S" ? 'BCS' : 'COR',
                Nemos: nemos,
                DataSourceIpc: dataSourceIpc
            });

            var promesa = bcsComunes.ventanaPromesa(id);

            $.when(promesa).then(function (data) {
                conf.options.dataSource.read();
                $scope.datos.kNgModel = undefined;
            });
            
        } catch (error) {
            alertError('Ingresar calificacion', error);
        }
	};

	$scope.eliminarCalificacionTributaria = function (calificacion) {
        try {
            if ($scope.filtros.PeriodoComercial === false) {
                bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_HAY_PERIODO_COMERCIAL_SELECCIONADO'));
                return;
            }
    
            if (!calificacion || calificacion.length == 0) {
                bcsComunes.msg('error', $translate.instant("UI_ERROR"), $translate.instant('NO_HAY_CALIFICACION_SELECCIONADA'));
                return;
            }
    
            calificacion = {
                EJERCICIO: calificacion[0].Ejercicio,
                MERCADO: calificacion[0].Mercado,
                NEMO: calificacion[0].Nemo,
                FEC_PAGO: dateFormatYYYYMMDD(util.formatoDate(calificacion[0].FechaPago)),
                SEC_EVE: calificacion[0].SecuenciaEvento,
                IDORIGEN: calificacion[0].Origen = 'COR',    
                DESCRIPCION: calificacion[0].Descripcion.trim(),
                NUM_DIVDO: calificacion[0].NumeroDividendo,    
                FechaUltimaMod: '', // Valor manejado en DB
                UsuarioUltimaMod: $scope.SesionUsuario || 'Undefined',
            };
    
            var datos = {
                Accion: 'DELETE',
                Calificacion_in: [calificacion],
                p_IdEmpresa: $scope.SesionIdEmpresa,
            	p_IdUsuario: $scope.SesionIdUsuario
            };
    
            var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_ELIMINAR_CALIFICACION_TRIBUTARIA'), idVentana);
            t.then(function(result) {		
                if (result === OPCIONES_BCSALERTA.SI){
                    bcsComunes.ventanaOverlay(idVentana, true);
                    bcsServiciosNormativa.IngresarCalificacion(datos, function(respuesta) {
                        var mensajeId = bcsComunes.ventanaMsg(
                            {
                                mensaje: `Calificacion eliminada.`,
                                conflictos: util.parseServerResponse(respuesta)                     
                        }, idVentana);
                        conf.options.dataSource.read();
                        $scope.datos.kNgModel = undefined;
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.ventanaData(idVentana, true);              
                        
                    }, function(respuesta) {
                        bcsComunes.msg("error", "error", `Problemas para eliminar Calificacion`);
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.ventanaData(idVentana, true);
                        bcsComunes.ventanaCerrar(idVentana);
                    });
                } else {
                    bcsComunes.ventanaData(idVentana, false);
                }
            });

        } catch (error) {
            bcsComunes.msg("error", "error", 'error al eliminar calificacion tributaria', error);
        }
		
    };

    const obtenerNemos = (data) => {

        return new Promise( (resolve, reject) => {
            bcsServiciosNormativa.IngresarCalificacion(data)
            .$promise.then(response => {
                if(response.P_OUT_NEMOS.length > 0) {
                    nemos = response.P_OUT_NEMOS;
                    resolve(response.P_OUT_NEMOS);
                } else {
                    reject(new Error('No se pudo obtener Nemos'));
                }
            })
            .catch( error => {
                reject(new Error('Error al consultar Nemos', error));
            });
        });
    };

    const getNemos = async (filterValue) => {
        try {
            let nemos = await obtenerNemos({ EJERCICIO: parseInt(filterValue, 10) });
            return nemos;
        } catch (err) {            
            throw 'Error al consultar Nemos:', err;
        }
    }

    // Cosulta a TBSICAVC (Validacion registros existentes)
    const getDataSource = async (filterField, filterValue) => {
        try {
            const dataSource = new kendo.data.DataSource({
                type: "odata",
                transport: {
                    read: {
                        url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=69069069",
                        dataType: "json",
                        data: {
                            $filter: filterField + " eq '" + filterValue + "'"
                        }
                    }
                },
                schema: {
                    model: {
                        fields: {
                            ejercicio: { type: "number" },
                            mercado: { type: "string" },
                            nemo: { type: "string" },
                            fechaPago: { type: "date" },
                            secuencia: { type: "string" },
                            idOrigen: { type: "string" }
                        }
                    }
                }
            });
        
            await new Promise((resolve, reject) => {
                dataSource.fetch(() => {
                    const data = dataSource.data();
                    $scope.$apply(() => {
                        $scope.dataItems = data;
                    });
                    resolve(data);
                });
            });
        
            return dataSource.data();
        } catch (error) {
            throw new Error('Error getDataSource', error);
        }
    };
    
    // Consulta TB ParametroFinanciero -> Calcular FactorAct
    const getDataSourceIpc = async () => {
        try {
            const dataSource = new kendo.data.DataSource({
                type: "odata",
                transport: {
                    read: {
                        url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=198237",
                        dataType: "json",
                    }
                },
                schema: {
                    model: {
                        fields: {
                            valor: { type: "number" },
                            agnoMes: { type: "string" },
                            idParametroFinanciero: { type: "string" },
                        }
                    }
                }
            });
        
            await new Promise((resolve, reject) => {
                dataSource.fetch(() => {
                    const data = dataSource.data();
                    $scope.$apply(() => {
                        $scope.dataItems = data;
                    });
                    resolve(data);
                });
            });
        
            return dataSource.data();
        } catch (error) {
            throw new Error('Error getDataSource', error);
        }
    };

    $scope.cargarCalificacionTributaria = async function (tipo) {
        try {

            let filterField = 'EJERCICIO';
            let filterValue = $scope.filtros.PeriodoComercial;

            const nemos = await getNemos(filterValue);

            const dataSource = await getDataSource(filterField, filterValue);

            var codigoTipoArchivo = 'ARCHIVO_CALIFICACION_TRIBUTARIA'; //codigo para carga externa por factores
            if(tipo == 2){
                codigoTipoArchivo = 'ARCHIVO_CALIFICACION_TRIBUTARIA_MONTO'; //codigo para carga externa por montos
            }
            
            var datosArchivo = { 
                codigoArchivo: codigoTipoArchivo, 
                formatoArchivoDefecto: '.csv', 
                periodoComercial: $scope.filtros.PeriodoComercial,
                idOrigen: $scope.esBolsa === "S" ? 'BCS' : 'COR',                
                dataSicavc: dataSource,
                tipoCarga: tipo
            };
            var id = bcsComunes.ventanaAbrir("CargaArchivoCalificacion", datosArchivo, true, true);        
            var promesa = bcsComunes.ventanaPromesa(id);
            const data = await promesa;
    
            if (!data || !data.fileData) {
                bcsComunes.ventanaOverlay($scope.winID, false);
                return;
            }    
            $scope.datosCarga = data.fileData;
            
            grabarCargaExterna($scope.datosCarga, dataSource);
        } catch (error) {
            alertError('Cargar calificacion tributaria', error);
            return;
        };
    };
    
    var successGrabarCalificacion = function (respuesta) {
        var mensajeId = bcsComunes.ventanaMsg({
            mensaje: `Calificaciones grabadas.`,
            conflictos: util.parseServerResponse(respuesta)
        }, idVentana);

        bcsComunes.ventanaPromesa(mensajeId).then(function () {
            conf.options.dataSource.read();
            $scope.datos.kNgModel = undefined;
            bcsComunes.ventanaOverlay(idVentana, false);
            bcsComunes.ventanaData(idVentana, true);
        });
    };

    var errorGrabarCalificacion = function (respuesta) {        
        bcsComunes.msg("error", "error", respuesta.statusText);    
        var mensajeId = bcsComunes.ventanaMsg({
            mensaje: `Error grabar calificaciones`,
            conflictos: util.parseServerResponse(respuesta)
        }, idVentana);

        bcsComunes.ventanaPromesa(mensajeId).then(function () {
            bcsComunes.ventanaOverlay(idVentana, false);
            bcsComunes.ventanaData(idVentana, true);
        });
    };

    const grabarCargaExterna = async (data, dataSource) => {

        const insertCal = [];
        const updateCal = [];
        let corExist = false;
        let bcsExist = false;

        /** 
         * Control de flujo segun esBolsa
         * ! COR
         * * Si es COR debe -> validar data entrante por Llave ->
         * * si hay match con llaveBCS y llaveCOR then Update COR ->
         * * si hay match SOLO con llaveBCS then Create ->
         * ? sino hay match con llaveBCS ni llaveCOR then Create COR.
         *  
         * ! BCS
         * * Si es BCS debe -> validar data entrante por llave
         * * si hay match con llaveBCS y llaveCOR then Update BCS ->
         * * si hay match SOLO con llaveBCS then Create BCS ->
         * ? sino hay match con llaves then Create BCS
         * 
         * ! Llave
         * * Nemo: EJERCICIO + MERCADO + NEMO + FEC_PAGO + IDORIGEN
         * * TBSICAVC: EJERCICIO + MERCADO + NEMO + FEC_PAGO + SEC_EVE + IDORIGEN
        */

        try {

            const calificaciones = data.map(function(cal) {
                const { MENSAJEVALIDACION, ...resto } = cal;
    
                return {
                    ...resto,
                    FACTOR_06: 0,
                    FACTOR_07: 0,    
                    NUM_DIVDO: 0,
                    FACTORACT: 0,
                    NUMCERTSOC: 0,
                    TIPO_SOC: '',
                    IDORIGEN: $scope.esBolsa === "S" ? 'BCS' : 'COR',
                    FechaUltimaMod: '',
                    UsuarioUltimaMod: $scope.SesionUsuario || 'Undefined',
                    FEC_PAGO: dateFormatYYYYMMDD(cal.FEC_PAGO)
                };
            });
    
            const validatePkTbsicavc = keyIn => {
                const existe = dataSource.some( item => {
                    let tableKey = (item.EJERCICIO+item.MERCADO+(item.NEMO).trim()+item.SEC_EVE+item.IDORIGEN).trim();
                    return tableKey === keyIn;
                });
    
                return existe;
            };

            const validateSecuencia = secuencia => {
                const existe = dataSource.some( item => {
                    let dSecuencia = item.SEC_EVE;
                    return dSecuencia === secuencia;
                });
    
                return existe;
            };
    
            calificaciones.forEach(cal => {
                const keyCal = (cal.EJERCICIO+cal.MERCADO+cal.NEMO+cal.SEC_EVE+cal.IDORIGEN).trim();
            
                if ($scope.esBolsa === 'N') {
                    corExist = validatePkTbsicavc(keyCal);
                    bcsExist = validatePkTbsicavc(keyCal.replace('COR', 'BCS'));
    
                    if((bcsExist && corExist) || (bcsExist === false && corExist)) {
                        updateCal.push(cal);
                    }

                    if((bcsExist && corExist === false) || (bcsExist === false && corExist === false)) {
                        insertCal.push(cal);
                    }
                } else {
                    bcsExist = validatePkTbsicavc(keyCal);                        
                }    
            });

            if(updateCal.length > 0){
                var updateData = {
                    Accion: 'UPDATE',
                    Calificacion_in: updateCal,
                    p_IdEmpresa: $scope.SesionIdEmpresa,
            		p_IdUsuario: $scope.SesionIdUsuario
                };
                bcsComunes.ventanaOverlay(idVentana, true);
                bcsServiciosNormativa.IngresarCalificacion(updateData, successGrabarCalificacion, errorGrabarCalificacion);
            }
    
            if(insertCal.length > 0){
                var insertData = {
                    Accion: 'CREAR',
                    Calificacion_in: insertCal,
                    p_IdEmpresa: $scope.SesionIdEmpresa,
            		p_IdUsuario: $scope.SesionIdUsuario
                };
                bcsComunes.ventanaOverlay(idVentana, true);
                bcsServiciosNormativa.IngresarCalificacion(insertData, successGrabarCalificacion, errorGrabarCalificacion);
            }

        } catch (error) {
            throw new Error('Error grabar carga externa > logica', error);
        };        

    };

    const alertError = (msg, error) => {
        bcsComunes.msg("error", "error", msg);
        bcsComunes.ventanaMsgErr(`${msg} ${error}`);
    };

	$scope.limpiarFiltros = function () {
		$scope.filtros.IdClasificacionTipoInstrumento = false;
		$scope.filtros.buscarInstrumento = "-1";
		$scope.filtros.PeriodoComercial = "-1";
		$scope.filtros.IdOrigen = $scope.esBolsa === "S" ? 'BCS' : 'COR';
	};
    

});