(function () {
    angular.module('ModuloSocioNegocio').controller(
        'ModalEnrolamientoGrupoDireccionElectronicaEnrolCtrl',
        function ($scope, servicioSociodeNegocio, bcsComunes, bcsServicioPlataforma) {
            var mgdie = this;
            var idVentana = bcsComunes.ventanaWinId($scope);
            var parametros = bcsComunes.ventanaParams(idVentana);
            mgdie.IdEnrolamiento = parametros.IdEnrolamiento;
            mgdie.CodigoFormulario = parametros.CodigoFormulario;
			mgdie.data = {};
			
            bcsServicioPlataforma.CamposRequeridosFormulario(mgdie.CodigoFormulario, function (respuesta) {
                mgdie.camposRequeridos = respuesta;
            });

            mgdie.aceptar = function () {
                var jsonEntrada = {
                    p_IdEmpresa: $scope.SesionIdEmpresa,
                    p_IdUsuario: $scope.SesionIdUsuario,
                    p_IdModulo: 'SOC',
                    p_UsaReglasNegocio: 'S',
                    p_CodigoFormulario: parametros.CodigoFormulario,
                    p_IdTipoCanal: 'OPT',
                    p_GrupoDirecciones: [
                        {
                            IdEnrolamiento: mgdie.IdEnrolamiento,
                            IdGrupoDireccion: mgdie.IdGrupoDireccion,
                            Grupo: mgdie.data.Grupo || '',
                            Comentario: mgdie.data.Comentario || '',
                        }
                    ]
                };

                bcsComunes.ventanaOverlay(idVentana, true);

                servicioSociodeNegocio.crearBorradorGrupoDireccion(jsonEntrada).$promise.then(
                    function (result) {
                        var idMensaje = bcsComunes.ventanaMsg({
                            mensaje: "Grupo de Direcciones creado",
                            conflictos: util.parseServerResponse(result)
                        }, idVentana);

                        bcsComunes.ventanaPromesa(idMensaje).then(function () {
                            bcsComunes.ventanaCerrar(idVentana);
                            bcsComunes.ventanaOverlay(idVentana, false);
                        });

                    },
                    function () {
                        bcsComunes.ventanaOverlay(idVentana, false);
                        bcsComunes.msg("error", "Error", "La operacion tiene errores");
                    }
                );

            };
            
            mgdie.asignarValoresPorDefecto = function (){
				$.each(mgdie.camposRequeridos ,function(key,value){
					let noEsRequerido = value.Requerido === 'N';
					let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
					let esCampoEnBlanco = !mdie.data[value.CodigoValorFrontend];
					if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
						mdie.data[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
					}
				});
			}
		

            mgdie.cancelar = function () {
                bcsComunes.ventanaData(idVentana, false);
                bcsComunes.ventanaCerrar(idVentana);
            };

        }
    );
}());
//------------------------- FIN CONTROLLER --------------------------