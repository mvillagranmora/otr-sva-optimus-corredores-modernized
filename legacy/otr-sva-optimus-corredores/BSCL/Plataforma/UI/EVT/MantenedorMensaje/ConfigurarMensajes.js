(function() {
	angular.module('ModuloPlataforma').controller('ConfigurarMensajesCtrl',
	function($rootScope, $scope,bcsServicioPlataforma,servicioSociodeNegocio,bcsComunes,$translate){
		const msj= this;
		const idVentana = bcsComunes.ventanaWinId($scope);
		const parametros = bcsComunes.ventanaParams(idVentana);
		msj.maxlenght = 5;
		msj.kOptFechaVigenciaDesde = {};
		msj.kOptFechaVigenciaHasta = {};
		msj.kOptFechaVigenciaDesde.min = $scope.$root.SesionFechaSistema;
		msj.kOptFechaVigenciaHasta.min = $scope.$root.SesionFechaSistema;
		msj.Mensaje = $.extend({},parametros.Mensaje);
		msj.MensajeOriginal = $.extend({},parametros.Mensaje);
	
		if (parametros.Mensaje) {
			msj.kOptFechaVigenciaDesde.min = parametros.Mensaje.FechaVigenciaDesde;
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_MENSAJE'));
			msj.Mensaje.Habilitado = parametros.Mensaje.Habilitado === "S" ? true : false;
			msj.Mensaje.MuestraMensajeCorto = parametros.Mensaje.MuestraMensajeCorto === "S" ? true : false;
		} else {
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_MENSAJE'));
			msj.Mensaje = {};
			msj.Mensaje.Habilitado = '';
			msj.Mensaje.MuestraMensajeCorto = '';
		}
		
		msj.Aceptar = function(){
			
			var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario :$scope.SesionIdUsuario,
				p_IdModulo :'PLA',
				p_UsaReglasNegocio :'N',
				p_Mensajes: [{
					IdInterno : 1,
					IdMensaje: msj.Mensaje.IdMensaje || 0,
					Titulo : msj.Mensaje.Titulo || '',
					MensajeCorto : msj.Mensaje.MensajeCorto || '',
					MensajeLargo : msj.Mensaje.MensajeLargo || '',
					IdTipoEntidadDestinatario : msj.Mensaje.IdTipoEntidadDestinatario || '',
					IdTipoMensaje : msj.Mensaje.IdTipoMensaje || 'AVISO',
					FechaVigenciaDesde : msj.Mensaje.FechaVigenciaDesde ? util.dateSPdate(msj.Mensaje.FechaVigenciaDesde): '0001-01-01',
					FechaVigenciaHasta : msj.Mensaje.FechaVigenciaHasta ? util.dateSPdate(msj.Mensaje.FechaVigenciaHasta): '0001-01-01',
					Habilitado : msj.Mensaje.Habilitado ? 'S': 'N',
					MuestraMensajeCorto : msj.Mensaje.MuestraMensajeCorto ? 'S': 'N',
					UrlAviso : '',
					IdTipoCanal : 'CLI'
				}]
			};
			
			bcsComunes.ventanaOverlay(idVentana, true);
			if(msj.Mensaje.IdMensaje){
				bcsServicioPlataforma.ModificarConfiguracionMensaje(datos,
					function (result) {
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_MODIFICAR_MENSAJE"));
							return;
						}
		                let idMensaje = bcsComunes.ventanaMsg({
		                    mensaje   : $translate.instant("UI_MENSAJE_MODIFICADO"),
		                    conflictos: util.parseServerResponse(result)
		                }, idVentana);

	                    bcsComunes.ventanaOverlay(idVentana, false);

		                bcsComunes.ventanaPromesa(idMensaje).then(function () {
		                    bcsComunes.ventanaData(idVentana, true);
		                    bcsComunes.ventanaCerrar(idVentana);
		                });
		            },
		            function () {
		                bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_MODIFICAR_MENSAJE"));
		                bcsComunes.ventanaData(idVentana, false);
		                bcsComunes.ventanaCerrar(idVentana);
		            }
		        );
			}else{
		        bcsServicioPlataforma.AgregarConfiguracionMensaje(datos, 
					function (result) {
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_AGREGAR_MENSAJE"));
							return;
						}
		                let idMensaje = bcsComunes.ventanaMsg({
		                    mensaje   : $translate.instant("UI_MENSAJE_AGREGADO"),
		                    conflictos: util.parseServerResponse(result)
		                }, idVentana);

	                    bcsComunes.ventanaOverlay(idVentana, false);

		                bcsComunes.ventanaPromesa(idMensaje).then(function () {
		                    bcsComunes.ventanaData(idVentana, true);
		                    bcsComunes.ventanaCerrar(idVentana);
		                })
		            },
		            function () {
		                bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_AGREGAR_MENSAJE"));
		                bcsComunes.ventanaData(idVentana, false);
		                bcsComunes.ventanaCerrar(idVentana);
		            }
		        );
			}
		};
		
		msj.Cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		}; 
		
		$scope.$watch("msj.Mensaje.FechaVigenciaDesde", function(nval, oval){
			msj.kOptFechaVigenciaHasta.min = msj.Mensaje.FechaVigenciaDesde;
			if(nval && msj.Mensaje.FechaVigenciaHasta && (moment(msj.Mensaje.FechaVigenciaHasta).isBefore(nval))){
				msj.Mensaje.FechaVigenciaHasta	= undefined;
			}
		});
	});
}());