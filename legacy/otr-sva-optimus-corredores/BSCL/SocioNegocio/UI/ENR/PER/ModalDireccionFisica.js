(function () {
    angular.module('ModuloSocioNegocio').controller('ModalEnrolamientoDireccionFisicaEnrolCtrl',
        function ($scope, $translate, servicioSociodeNegocio, bcsComunes, bcsServicioPlataforma) {
            const mdfi = this;
            const idVentana = bcsComunes.ventanaWinId($scope);
            const parametros = bcsComunes.ventanaParams(idVentana);
            mdfi.CodigoFormulario = parametros.CodigoFormulario;
            mdfi.IdEnrolamiento = parametros.IdEnrolamiento;
            mdfi.DireccionFisica = $.extend({}, parametros.DireccionFisica);
            
            mdfi.filtroCiudad = {
                IdPais: { field: "IdPais", operator: "eq", value: -1 }
            };
            mdfi.filtroDivisionPais = {
                IdPais: { field: "IdPais", operator: "eq", value: -1 }
            };
            
            bcsServicioPlataforma.CamposRequeridosFormulario(mdfi.CodigoFormulario, function(respuesta) {
                mdfi.camposRequeridos = respuesta;
                mdfi.asignarValoresPorDefecto();
                mdfi.CargarDatos();
            });
			     
            mdfi.CargarDatos = function(){
		
				if (parametros.DireccionFisica) {
					mdfi.DireccionFisicaOriginal = $.extend({}, parametros.DireccionFisica);
	                bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_DIRECCION_FISICA'));
	            } else {
	            	mdfi.DireccionFisicaOriginal = $.extend({}, mdfi.DireccionFisica);
	                bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_DIRECCION_FISICA'));
	                mdfi.DireccionFisicaOriginal.IdPais = $scope.$root.SesionIdPaisSistema;
	            }
			}

            if ($scope.SesionLocale == 'PE') {
                $scope.PropositoRecadoExcluido = { field: "IdPropositoMedioContacto", operator: "neq", value: 'RECA' };
                $scope.PropositoFacturacionExcluido = {
                    field: "IdPropositoMedioContacto",
                    operator: "neq",
                    value: 'FACT'
                };

                mdfi.configuracionProposito = {
                    filtros: {
                        complejo: [ $scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido ],
                        logic   : "and",
                        filters : [ $scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido ]
                    }
                };
            }

            mdfi.aceptar = function aceptar() {
				const estadoPredeterminado = mdfi.DireccionFisica.Predeterminado === 'UI_SI' ? 'S' : 'N';
				let datos = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'SOC',
                    p_UsaReglasNegocio: 'S',
					p_IdTipoCanal: 'OPT',
					p_CodigoFormulario: mdfi.CodigoFormulario,
                    p_DireccionesFisicas: [
                        {
							IdInterno		  : 1,
	                        IdEnrolamiento  : mdfi.IdEnrolamiento,
							IdPais			: mdfi.DireccionFisica.IdPais || 0,
							IdDireccionFisica: mdfi.DireccionFisica.IdDireccionFisica || 0,
	                        Calle           : mdfi.DireccionFisica.Calle || '',
	                        Numero          : mdfi.DireccionFisica.Numero || '',
	                        Departamento    : mdfi.DireccionFisica.Departamento || '',
	                        Sector          : mdfi.DireccionFisica.Sector || '',
	                        CodigoPostal    : mdfi.DireccionFisica.CodigoPostal || '',
	                        IdCiudad        : ((mdfi.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')
	                                             ? mdfi.DireccionFisica.IdCiudad || 0
	                                             : mdfi.divisionPaisSeleccionado.IdCiudad || 0),
	                        IdDivisionPais  : mdfi.DireccionFisica.IdDivisionPais || 0,
	                        CodigoProposito : mdfi.DireccionFisica.CodigoProposito || '',
	                        Comentario      : mdfi.DireccionFisica.Comentario || '',
	                        CodigoCompuesto : mdfi.DireccionFisica.CodigoCompuesto || '',
							Predeterminado	: estadoPredeterminado || 'N',
							CodigoDivisionPais: mdfi.divisionPaisSeleccionado.CodigoDivisionPais || ''
                        }
                    ]
                };

                if (mdfi.DireccionFisica.IdDireccionFisica) {

                    mdfi.DireccionFisica.IdCiudad = (mdfi.paisResidenciaSeleccionado.UsaDivisionPais === 'S')
                                                    ? mdfi.divisionPaisSeleccionado.IdCiudad
                                                    : mdfi.DireccionFisica.IdCiudad;

                    bcsComunes.ventanaOverlay(idVentana, true);
                    servicioSociodeNegocio.modificarBorradorDireccionFisica(datos).$promise.then(
                        function (result) {
                            let idMensaje = bcsComunes.ventanaMsg({
                                mensaje   : $translate.instant("UI_DIRECCION_FISICA_MODIFICADA"),
                                conflictos: util.parseServerResponse(result)
                            }, idVentana);
                            bcsComunes.ventanaPromesa(idMensaje).then(function () {
                                bcsComunes.ventanaOverlay(idVentana, false);
                                bcsComunes.ventanaData(idVentana, true);
                                bcsComunes.ventanaCerrar(idVentana);
                            });
                        },
                        function () {
                            bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_MODIFICAR_DIRECCION_FISICA"));
                            bcsComunes.ventanaOverlay(idVentana, false);
                            bcsComunes.ventanaData(idVentana, true);
                            bcsComunes.ventanaCerrar(idVentana);
                        });
                }
                else {
                    
                    bcsComunes.ventanaOverlay(idVentana, true);
                    servicioSociodeNegocio.crearBorradorDireccionFisica(datos).$promise.then(
                        function (result) {
                            let idMensaje = bcsComunes.ventanaMsg({
                                mensaje   : $translate.instant("UI_DIRECCION_FISICA_AGREGADA"),
                                conflictos: util.parseServerResponse(result)
                            }, idVentana);
                            bcsComunes.ventanaPromesa(idMensaje).then(function () {
                                bcsComunes.ventanaOverlay(idVentana, false);
                                bcsComunes.ventanaData(idVentana, true);
                                bcsComunes.ventanaCerrar(idVentana);
                            })
                        },
                        function () {
                            bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_AGREGAR_DIRECCION_FISICA"));
                            bcsComunes.ventanaOverlay(idVentana, false);
                            bcsComunes.ventanaData(idVentana, false);
                            bcsComunes.ventanaCerrar(idVentana);
                        }
                    );
                }
            };
            
            mdfi.asignarValoresPorDefecto = function (){
				if(parametros.DireccionFisica) return; 
				
				$.each(mdfi.camposRequeridos ,function(key,value){
					let noEsRequerido = value.Requerido === 'N';
					let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
					let esCampoEnBlanco = !mdfi.DireccionFisica[value.CodigoValorFrontend];
					if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
						mdfi.DireccionFisica[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
					}
				});
			};

            mdfi.cancelar = function cancelar() {
                bcsComunes.ventanaData(idVentana, false);
                bcsComunes.ventanaCerrar(idVentana);
            };

        });
}());
//------------------------- FIN CONTROLLER --------------------------