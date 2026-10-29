/*************************************************************
 * Purpose	: Modal Nombres Fantasia
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalNombresFantasiaCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes, bcsServicioPlataforma) {
		var mnf = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mnf.IdPersona = parametros.IdPersona;
		mnf.NombreFantasia = $.extend({},parametros.NombreFantasia);
		mnf.CodigoFormulario = parametros.CodigoFormulario;
		mnf.camposRequeridos = {};
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mnf.CodigoFormulario, function (respuesta) {
			mnf.camposRequeridos = respuesta;
			mnf.asignarValoresPorDefecto();
			mnf.cargarDatos();
		});
		
		mnf.cargarDatos = function() {
			if (parametros.NombreFantasia){
				//modificacion
				mnf.NombreFantasiaOriginal = $.extend({},parametros.NombreFantasia);
				mnf.NombreFantasia.NombreFantasia = mnf.NombreFantasia.DescripcionNombreFantasia;
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_NOMBRE_FANTASIA'));
			}else{
				mnf.NombreFantasiaOriginal = $.extend({},mnf.NombreFantasia);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_NOMBRE_FANTASIA'));
				mnf.NombreFantasiaOriginal.IdEstado = 'ACT';
				
			}
		}
		
		mnf.cmbbxNombreFantasia = {
			filtros : {
				IdEstado : {field: "IdEstado", operator: "ne", value: 'PEN'}
			}
		};

		mnf.aceptar = function() {
			
			//modificacion
			if (mnf.NombreFantasia.IdNombreFantasia){
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdPersona :mnf.IdPersona,
					p_IdNombreFantasia :mnf.NombreFantasia.IdNombreFantasia,
					p_NombreFantasia :mnf.NombreFantasia.NombreFantasia || '',
					p_IdEstado :mnf.NombreFantasia.IdEstado || ''
						
				};

				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaNombreFantasia(datos).$promise.then(function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);				
					
					var p = bcsComunes.ventanaMsg({ mensaje : "Nombre Fantasia modificada",
					conflictos : util.parseServerResponse(result)}, idVentana);
					
					bcsComunes.ventanaPromesa(p).then(function(){
						if (result.P_OUT_ERRORESCONTADOR === 0) {
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						}
					})
				}, function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","error","Error al modificar nombre fantasia");
				});

			}
			//creacion
			else {
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdPersona :mnf.IdPersona,
					p_NombreFantasia :mnf.NombreFantasia.NombreFantasia || '',
					p_IdEstado :mnf.NombreFantasia.IdEstado || ''
						
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaNombreFantasia(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);				
						var p = bcsComunes.ventanaMsg({ mensaje : "Nombre Fantasia agregado",
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(p).then(function(){
							if (result.P_OUT_ERRORESCONTADOR === 0) {
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
							}
						})
						
						
					},
					function(result){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.msg("error","error","Error al agregar nombre fantasia");
					});
			}
		};
		
		mnf.asignarValoresPorDefecto = function (){
			if(parametros.NombreFantasia) return;
			$.each(mnf.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mnf.NombreFantasia[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mnf.NombreFantasia[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
		
		mnf.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------