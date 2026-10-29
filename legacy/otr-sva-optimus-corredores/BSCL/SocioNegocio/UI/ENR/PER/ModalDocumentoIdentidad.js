(function () {
    angular.module('ModuloSocioNegocio').controller(
        'ModalEnrolamientoDocumentoIdentidadEnrolCtrl',
        function ($scope, $translate, servicioSociodeNegocio, bcsComunes, bcsServicioPlataforma) {
            var mdi = this;
            mdi.placeholder = {};
            var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
            var parametros = bcsComunes.ventanaParams(idVentana);

            mdi.CodigoFormulario = parametros.CodigoFormulario;
            mdi.IdEnrolamiento = parametros.IdEnrolamiento;
            mdi.tipoPersona = parametros.CodigoTipoPersona;
            mdi.DocumentoIdentidad = $.extend({}, parametros.DocumentoIdentidad);
            mdi.bloqueoDocumentoIdentidad = (!!parametros.DocumentoIdentidad === false || parametros.DocumentoIdentidad.IdTipoDocumentoIdentidad === "RUT" || !parametros.DocumentoIdentidad.DocumentoIdentidad) ? false : true;
            mdi.bloqueoTipoDocumentoIdentidad = !!parametros.DocumentoIdentidad && parametros.DocumentoIdentidad.IdTipoDocumentoIdentidad !== '';
            mdi.forzarTipoDocumento = (parametros.DocumentoIdentidad && parametros.DocumentoIdentidad.DocumentoIdentidad && parametros.DocumentoIdentidad.IdTipoDocumentoIdentidad) ? "S" : "N";
            console.log('dfjgk', mdi.forzarTipoDocumento)
            mdi.formConfig = {};
            mdi.formConfig.largoMaximoNumero = 0;

            mdi.oConfiguracionDocumentoIdentidad = {
                filtros: {
                    IdPais: { "field": "IdPais", "operator": "eq", "value": $scope.$root.SesionIdPaisSistema }
                }
            }

 
            bcsServicioPlataforma.CamposRequeridosFormulario(mdi.CodigoFormulario, function(respuesta) {
                mdi.camposRequeridos = respuesta;
                mdi.asignarValoresPorDefecto();
                mdi.cargarDatos();
            });
            
            mdi.cargarDatos = function() {
				if (parametros.DocumentoIdentidad){
					mdi.DocumentoIdentidadOriginal = $.extend({},parametros.DocumentoIdentidad);
					bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_DOCUMENTO_IDENTIDAD'));
				}else{
					mdi.DocumentoIdentidadOriginal = $.extend({},mdi.DocumentoIdentidad);
					bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_DOCUMENTO_IDENTIDAD'));
					mdi.DocumentoIdentidad.FechaEmision = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
					mdi.DocumentoIdentidadOriginal.IdEstado = 'ACT'; 
					mdi.DocumentoIdentidadOriginal.IdTipoDocumentoIdentidad = 'R';
				}
			}
            
            mdi.asignarValoresPorDefecto = function (){
				$.each(mdi.camposRequeridos ,function(key,value){
					let noEsRequerido = value.Requerido === 'N';
					let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
					let esCampoEnBlanco = !mdi.DocumentoIdentidad[value.CodigoValorFrontend];
					if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
						mdi.DocumentoIdentidad[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
					}
				});
			}
            
			mdi.convertirFechas = function() {
			
				if(mdi.DocumentoIdentidad.FechaVencimiento){
					mdi.DocumentoIdentidad.FechaVencimiento = util.date(mdi.DocumentoIdentidad.FechaVencimiento).toDate();
				}
				
				if(mdi.DocumentoIdentidad.FechaEmision) {
					mdi.DocumentoIdentidad.FechaEmision = util.date(mdi.DocumentoIdentidad.FechaEmision).toDate();
				}
			}
			
            mdi.aceptar = function aceptar() {
            	mdi.convertirFechas();
                if (mdi.DocumentoIdentidad.IdDocumentoIdentidad) {
                	
                    const requestBody = {
                        p_IdEmpresa: $scope.SesionIdEmpresa,
                        p_IdUsuario: $scope.SesionIdUsuario,
                        p_IdModulo: 'SOC',
                        p_UsaReglasNegocio: 'S',
                        p_CodigoFormulario: mdi.CodigoFormulario,
                        p_IdTipoCanal: 'OPT',
                        p_DocumentosIdentidades: [
                            {
                                IdDocumentoIdentidad: mdi.DocumentoIdentidad.IdDocumentoIdentidad,
                                IdEnrolamiento: mdi.IdEnrolamiento,
                                IdTipoDocumentoIdentidad: mdi.DocumentoIdentidad.IdTipoDocumentoIdentidadAsociado,
                                DocumentoIdentidad: mdi.DocumentoIdentidad.DocumentoIdentidadAsociado,
                                NumeroSerie: mdi.DocumentoIdentidad.NumeroSerie,
                                FechaEmision: util.dateSPdate(mdi.DocumentoIdentidad.FechaEmision),
                                FechaVencimiento: mdi.DocumentoIdentidad.FechaVencimiento ? util.dateSPdate(mdi.DocumentoIdentidad.FechaVencimiento) : '0001-01-01'
                            }
                        ]
                    };

                    bcsComunes.ventanaOverlay(idVentana, true);

                    servicioSociodeNegocio.modificarBorradorDocumentoIdentidad(requestBody).$promise.then(
                        function (result) {
                            const titulo = result.P_OUT_ERRORESCONTADOR > 0 ? 'UI_ERROR' : 'UI_DOCUMENTO_IDENTIDAD_MODIFICADO';

                            const idMensaje = bcsComunes.ventanaMsg({
                                mensaje: titulo,
                                conflictos: util.parseServerResponse(result)
                            }, idVentana);

                            bcsComunes.ventanaPromesa(idMensaje).then(function () {
                                bcsComunes.ventanaData(idVentana, true);
                                bcsComunes.ventanaCerrar(idVentana);
                                bcsComunes.ventanaOverlay(idVentana, false);
                            });
                        },
                        function () {
                            bcsComunes.msg("error", "error", "Error al modificar documento de identidad");
                            bcsComunes.ventanaData(idVentana, true);
                            bcsComunes.ventanaCerrar(idVentana);
                            bcsComunes.ventanaOverlay(idVentana, false);
                        }
                    );

                } else {
                    // Creación
                    const requestBody = {
                        p_IdEmpresa: $scope.SesionIdEmpresa,
                        p_IdUsuario: $scope.SesionIdUsuario,
                        p_IdModulo: 'SOC',
                        p_UsaReglasNegocio: 'S',
                        p_CodigoFormulario: mdi.CodigoFormulario,
                        p_IdTipoCanal: 'OPT',
                        p_DocumentosIdentidades: [
                            {
                                IdEnrolamiento: mdi.IdEnrolamiento,
                                IdTipoDocumentoIdentidad: mdi.DocumentoIdentidad.IdTipoDocumentoIdentidadAsociado,
                                DocumentoIdentidad: mdi.DocumentoIdentidad.DocumentoIdentidadAsociado,
                                NumeroSerie: mdi.DocumentoIdentidad.NumeroSerie ? mdi.DocumentoIdentidad.NumeroSerie: '',
                                FechaEmision: mdi.DocumentoIdentidad.FechaEmision ? util.dateSPdate(mdi.DocumentoIdentidad.FechaEmision) : '0001-01-01' ,
                                FechaVencimiento: mdi.DocumentoIdentidad.FechaVencimiento ? util.dateSPdate(mdi.DocumentoIdentidad.FechaVencimiento) : '0001-01-01'
                            }
                        ]
                    };

                    bcsComunes.ventanaOverlay(idVentana, true);

                    servicioSociodeNegocio.crearBorradorDocumentoIdentidad(requestBody).$promise.then(
                        function (result) {
                            const titulo = result.P_OUT_ERRORESCONTADOR > 0 ? 'UI_ERROR' : 'UI_DOCUMENTO_IDENTIDAD_AGREGADO'

                            const idMensaje = bcsComunes.ventanaMsg({
                                mensaje: titulo,
                                conflictos: util.parseServerResponse(result)
                            }, idVentana);

                            bcsComunes.ventanaPromesa(idMensaje).then(function () {
                                bcsComunes.ventanaData(idVentana, true);
                                bcsComunes.ventanaCerrar(idVentana);
                                bcsComunes.ventanaOverlay(idVentana, false);
                            });
                        },
                        function () {
                            bcsComunes.msg("error", "error", "Error al agregar documento de identidad");
                            bcsComunes.ventanaData(idVentana, false);
                            bcsComunes.ventanaCerrar(idVentana);
                            bcsComunes.ventanaOverlay(idVentana, false);
                        }
                    );
                }
            };

            mdi.cancelar = function cancelar() {
                bcsComunes.ventanaData(idVentana, false);
                bcsComunes.ventanaCerrar(idVentana);
            };
            
            

            // Se obtienen placeholder
            $scope.$watch('mdi.DocumentoIdentidad.IdTipoDocumentoIdentidadAsociado', function (tipo) {
                if (!tipo) {
                    return;
                }

                if (tipo === 'R') {
                    mdi.placeholder.documentoIdentidad = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_RUT;

                } else if (tipo === 'P') {
                    mdi.placeholder.documentoIdentidad = '';

                } else if (tipo === 'RUT') {
                    mdi.formConfig.largoMaximoNumero = 10;

                } else {
                    mdi.placeholder.documentoIdentidad = '';
                    mdi.formConfig.largoMaximoNumero = 0;
                }

                mdi.blurDocumentoIdentidad(tipo);
            });
            

            mdi.blurDocumentoIdentidad = function blurDocumentoIdentidad(tipo) {
                if (!tipo) return;
                var d = mdi.DocumentoIdentidad.DocumentoIdentidadAsociado || "";
                if (d.length == 0) return;
                $scope.errorValidarDocumento = false;
                var validacion = true;


                if (tipo == 'R') {

                    d = mdi.DocumentoIdentidad.DocumentoIdentidad;
                    var obtieneDigitoVerificador = function dv(T) {
                        var M = 0, S = 1;
                        for (; T; T = Math.floor(T / 10))
                            S = (S + T % 10 * (9 - M++ % 6)) % 11;
                        return S ? S - 1 : 'K';
                    };

                    if (!(d.indexOf('-') > 0 && d.length > (d.indexOf('-') + 1) && d[d.indexOf('-') + 1] && d.length >= 3 &&
                        obtieneDigitoVerificador(d.substr(0, d.length - 2)) == d[d.indexOf('-') + 1])) {
                        bcsComunes.msg("error", 'Error', 'UI_RUT_INVALIDO');
                        mdi.DocumentoIdentidad.DocumentoIdentidad = "";
                        d = "";
                        $scope.errorValidarDocumento = true;
                        return;
                    }

                }
                if (tipo == 'IUP') {
                    if (d.length < 14) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
                        $scope.errorValidarDocumento = true;
                    }
                    if (d.length > 14) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
                        d = "";
                        $scope.errorValidarDocumento = true;
                    } else {
                        var validacion = util.validarIUP(d);
                        if (!validacion.escorrecta) {
                            bcsComunes.msg("warning", "warning", $translate.instant(validacion.mensaje));
                            $scope.errorValidarDocumento = true;
                        }
                    }
                }

                if (tipo == 'DNI') {
                    if (d.length < 8) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
                        $scope.errorValidarDocumento = true;
                    }
                    if (d.length > 8) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
                        d = "";
                        $scope.errorValidarDocumento = true;
                    }
                }

                if (tipo == 'RUC') {
                    if (d.length < 11) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
                        $scope.errorValidarDocumento = true;
                    }
                    if (d.length > 11) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_SUPERA_LARGO_PERMITIDO"));
                        d = "";
                        $scope.errorValidarDocumento = true;
                    }
                }
                if (tipo == 'PAS') {
                    if (d.length > 11) {
                        bcsComunes.msg("warning", "warning", $translate.instant("UI_NO_CUMPLE_LARGO_PERMITIDO"));
                        $scope.errorValidarDocumento = true;
                    }
                }
                if (((tipo != 'PAS') && (tipo != 'RUC') && (tipo != 'DNI') && (tipo != 'IUP') && (tipo != 'R'))) {
                    bcsComunes.msg("error", 'Error', $translate.instant('UI_TIPO_INVALIDO'));//agregar msj al diccionario
                    return;
                }

                mdi.DocumentoIdentidad.DocumentoIdentidad = d;
            };

        }
    );
}());
//------------------------- FIN CONTROLLER --------------------------