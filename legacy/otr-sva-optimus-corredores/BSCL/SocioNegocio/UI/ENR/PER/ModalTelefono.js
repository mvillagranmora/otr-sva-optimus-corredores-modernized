(function () {
	angular.module('ModuloSocioNegocio').controller('ModalTelefonoEnrolCtrl',
		function ($scope, $translate, servicioSociodeNegocio, bcsComunes, bcsServicioPlataforma) {
			const mte = this;
			const idVentana = bcsComunes.ventanaWinId($scope);
			const parametros = bcsComunes.ventanaParams(idVentana);
			mte.IdEnrolamiento = parametros.IdEnrolamiento;
			mte.CodigoFormulario = parametros.CodigoFormulario;
			mte.Telefono = $.extend({}, parametros.Telefono);
			mte.camposRequeridos = {};
			
			bcsServicioPlataforma.CamposRequeridosFormulario(mte.CodigoFormulario, function (respuesta) {
				mte.camposRequeridos = respuesta;
				mte.asignarValoresPorDefecto();
				mte.cargarDatos();
			});
			
			mte.cargarDatos = function(){
				if (parametros.Telefono) {
					mte.TelefonoOriginal = $.extend({}, parametros.Telefono);
					bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_TELEFONO'));
				} else {
					mte.TelefonoOriginal = $.extend({}, mte.Telefono);
					bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_TELEFONO'));
					mte.TelefonoOriginal.IdPais = $scope.$root.SesionIdPaisSistema;
					mte.TelefonoOriginal.IdTipoTelefono = 'FIJO';
				}
			}

			if ($scope.SesionLocale == 'PE') {
				$scope.PropositoRecadoExcluido = { field: "IdPropositoMedioContacto", operator: "neq", value: 'RECA' };
				$scope.PropositoFacturacionExcluido = { field: "IdPropositoMedioContacto", operator: "neq", value: 'FACT' };

				mte.configuracionProposito = {
					filtros: {
						complejo: [$scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido],
						logic: "and",
						filters: [$scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido]
					}
				};
			}

			mte.aceptar = function aceptar() {
				if (!mte.validaFormatoTelefono()){
					return;
				}

				const estadoPredeterminado = mte.Telefono.Predeterminado === 'UI_SI' ? 'S' : 'N';
				const datos = {
					p_Telefonos: [{
						IdInterno: 1,
						IdEnrolamiento: mte.IdEnrolamiento,
						IdTelefono: mte.Telefono.IdTelefono || 0,
						IdPais: mte.Telefono.IdPais || 0,
						IdTipoTelefono: mte.Telefono.IdTipoTelefono || '',
						Numero: mte.Telefono.Numero || '',
						Anexo: mte.Telefono.Anexo || '',
						CodigoProposito: mte.Telefono.CodigoProposito || '',
						Comentario: mte.Telefono.Comentario || '',
						Predeterminado: estadoPredeterminado || 'N'
					}],
					p_IdEmpresa: $scope.SesionIdEmpresa,
					p_IdUsuario: $scope.SesionIdUsuario,
					p_IdModulo: 'SOC',
					p_UsaReglasNegocio: 'S',
					p_CodigoFormulario: mte.CodigoFormulario,
					p_IdTipoCanal: 'OPT',
				}

				bcsComunes.ventanaOverlay(idVentana, true);
				if (mte.Telefono.IdTelefono) {
					servicioSociodeNegocio.modificarBorradorTelefono(datos).$promise.then(
						function (result) {
							bcsComunes.ventanaOverlay(idVentana, false);
							if (result.P_OUT_ERRORESCONTADOR > 0) {
								bcsComunes.msg("error", "error", "Error al modificar teléfono");
								return;
							}

							bcsComunes.msg("info", "info", "Teléfono modificado");

							let idMensaje = bcsComunes.ventanaMsg({
								mensaje: "Teléfono modificado",
								conflictos: util.parseServerResponse(result)
							}, idVentana);

							bcsComunes.ventanaPromesa(idMensaje).then(function () {
								bcsComunes.ventanaData(idVentana, true);
								bcsComunes.ventanaCerrar(idVentana);
							});
						},
						function (result) {
							bcsComunes.msg("error", "error", "Error al modificar teléfono");
							bcsComunes.ventanaData(idVentana, true);
							bcsComunes.ventanaOverlay(idVentana, false);
						}
					);
				} else {
					servicioSociodeNegocio.crearBorradorTelefono(datos).$promise.then(
						function (result) {
							bcsComunes.ventanaOverlay(idVentana, false);
							if (result.P_OUT_ERRORESCONTADOR > 0) {
								bcsComunes.msg("error", "error", "Error al agregar teléfono");
								return;
							}
							let idMensaje = bcsComunes.ventanaMsg({
								mensaje: "Teléfono agregado",
								conflictos: util.parseServerResponse(result)
							}, idVentana);

							bcsComunes.ventanaPromesa(idMensaje).then(function () {
								bcsComunes.ventanaData(idVentana, true);
								bcsComunes.ventanaCerrar(idVentana);
							});
						},
						function (result) {
							bcsComunes.msg("error", "error", "Error al agregar teléfono");
							bcsComunes.ventanaData(idVentana, false);
							bcsComunes.ventanaOverlay(idVentana, false);
						}
					);
				}
			};
			
			mte.asignarValoresPorDefecto = function (){
				if(parametros.Telefono) return; 
				
				$.each(mte.camposRequeridos ,function(key,value){
					let noEsRequerido = value.Requerido === 'N';
					let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
					let esCampoEnBlanco = !mte.Telefono[value.CodigoValorFrontend];
					if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
						mte.Telefono[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
					}
				});
			};

			mte.cancelar = function cancelar() {
				bcsComunes.ventanaData(idVentana, false);
				bcsComunes.ventanaCerrar(idVentana);
			};

			// Se obtienen placeholder
			mte.placeholder = {};
			mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;

			$scope.$watch('mte.Telefono.IdTipoTelefono', function (newval, oldval) {
				if (newval) {
					if (mte.Telefono.IdTipoTelefono === 'FIJO') {
						mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
					} else if (mte.Telefono.IdTipoTelefono === 'MOVIL') {
						mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO_MOVIL;
					} else {
						mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
					}
				} else {
					mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
				}
			});

			// Valida formato Telefono
			mte.validaFormatoTelefono = function () {
				if ($scope.SesionLocale == 'PE') return true;
				if (mte.Telefono.Numero !== undefined && mte.Telefono.Numero !== '' && region.ValidaFormatoNumeroTelefono(mte.Telefono.Numero) == false) {
					bcsComunes.msg("warning", "warning", $translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO").replace("XXPLACEHOLDERXX", mte.placeholder.telefono));
					$scope.formTelefono.$valid = undefined;
					return false;
				}
				return true;
			}
		});
}());