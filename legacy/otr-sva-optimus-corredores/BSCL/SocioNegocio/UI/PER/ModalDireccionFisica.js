/*************************************************************
 * Purpose	: Modal Dirección Física.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 * 
 * Purpose	: Se incluye campo CodigoCompuesto, req. proy Perú.
 *            Perú: UBIGEO / Chile: Codigo Unico Territorial
 * Author	: JGarces - BCS
 * Date		: 14/03/2017
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalDireccionFisicaCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes, bcsServicioPlataforma) {
		var mdfi = this;
		var idVentana = bcsComunes.ventanaWinId($scope); 
		var parametros = bcsComunes.ventanaParams(idVentana);		
			
		mdfi.filtroCiudad = {
			IdPais : {field: "IdPais", operator: "eq", value: -1}
		};	
		mdfi.filtroDivisionPais = {
			IdPais : {field: "IdPais", operator: "eq", value: -1}
		};	
		mdfi.IdPersona = parametros.IdPersona;
		mdfi.DireccionFisica = $.extend({},parametros.DireccionFisica);
		mdfi.CodigoFormulario = parametros.CodigoFormulario;
		mdfi.camposRequeridos = {};
		
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
		
		mdfi.cmbbxEstadoDireccionFisica = {
			filtros : {
				IdEstado : {field: "IdEstado", operator: "ne", value: 'PEN'}
			}
		};
		
		if($scope.SesionLocale=='PE') {
			$scope.PropositoRecadoExcluido = {field: "IdPropositoMedioContacto", operator: "neq", value: 'RECA'};
			$scope.PropositoFacturacionExcluido = {field: "IdPropositoMedioContacto", operator: "neq", value: 'FACT'};
			
			mdfi.configuracionProposito = {
				filtros : {
					complejo : [$scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido],
					logic: "and",
					filters: [$scope.PropositoRecadoExcluido,$scope.PropositoFacturacionExcluido]
				}
			};
		}
		
		mdfi.aceptar = function() {
			//modificacion
			if (mdfi.DireccionFisica.IdDireccionFisica){
				var campos = [
							{ nombre: "Calle" ,tipo : "texto"},
							{ nombre: "Numero" ,tipo : "texto"},
							{ nombre: "Departamento" ,tipo : "texto"},
							{ nombre: "Sector" ,tipo : "texto"},
							{ nombre: "CodigoPostal" ,tipo : "texto"},
							{ nombre: "IdCiudad" ,tipo : "combo"},
							{ nombre: "IdDivisionPais" ,tipo : "combo"},
							{ nombre: "CodigoProposito" ,tipo : "combo"},
							{ nombre: "Comentario" ,tipo : "texto"},
							{ nombre: "IdEstado" ,tipo : "combo"},
							{ nombre: "CodigoCompuesto" ,tipo : "texto"}];
					
				var registroCambio = [];
				
				mdfi.DireccionFisica.IdCiudad = (mdfi.paisResidenciaSeleccionado.UsaDivisionPais === 'S') ? mdfi.divisionPaisSeleccionado.IdCiudad : mdfi.DireccionFisica.IdCiudad;
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mdfi.DireccionFisica[value.nombre] !== mdfi.DireccionFisicaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdfi.DireccionFisica[value.nombre]?mdfi.DireccionFisica[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mdfi.DireccionFisica[value.nombre] === false && mdfi.DireccionFisicaOriginal[value.nombre] === null) && (mdfi.DireccionFisica[value.nombre] != mdfi.DireccionFisicaOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdfi.DireccionFisica[value.nombre]?mdfi.DireccionFisica[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mdfi.DireccionFisica[value.nombre] !== mdfi.DireccionFisicaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdfi.DireccionFisica[value.nombre]?util.dateSPdate(mdfi.DireccionFisica[value.nombre]) :"")});
					}
					if (value.tipo === 'bool') {
						if (mdfi.DireccionFisica[value.nombre] !== mdfi.DireccionFisicaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdfi.DireccionFisica[value.nombre]?"S":"N")});
					}			
				});
				
				if (registroCambio.length === 0)
				{
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdDireccionFisica: mdfi.DireccionFisica.IdDireccionFisica,
					p_RegistroCambio: registroCambio
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaDireccionFisica(datos).$promise.then(
					function(result) {
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_DIRECCION_FISICA_MODIFICADA"),
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(){
						bcsComunes.msg("error","error",$translate.instant("UI_ERROR_MODIFICAR_DIRECCION_FISICA"));
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
					});
			}
			//creacion
			else {
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_Calle :mdfi.DireccionFisica.Calle || '',
					p_Numero :mdfi.DireccionFisica.Numero || '',
					p_Departamento :mdfi.DireccionFisica.Departamento || '',
					p_Sector :mdfi.DireccionFisica.Sector || '',
					p_CodigoPostal :mdfi.DireccionFisica.CodigoPostal || '',
					p_IdCiudad : ((mdfi.paisResidenciaSeleccionado && mdfi.paisResidenciaSeleccionado.UsaDivisionPais !== 'S')?mdfi.DireccionFisica.IdCiudad:mdfi.divisionPaisSeleccionado.IdCiudad),
					p_IdDivisionPais : mdfi.DireccionFisica.IdDivisionPais || null,
					p_CodigoProposito :mdfi.DireccionFisica.CodigoProposito || '',
					p_Comentario :mdfi.DireccionFisica.Comentario || '',
					p_IdEstado :mdfi.DireccionFisica.IdEstado || '',
					p_IdPersona :mdfi.IdPersona,
					p_CodigoCompuesto:mdfi.DireccionFisica.CodigoCompuesto,
					p_UsaDivisionPais:mdfi.paisResidenciaSeleccionado.UsaDivisionPais || 'N'
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaDireccionFisica(datos).$promise.then(
					function(result) {
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_DIRECCION_FISICA_AGREGADA"),
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaOverlay(idVentana,false);
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						})
					},
					function(){
						bcsComunes.msg("error","error",$translate.instant("UI_ERROR_AGREGAR_DIRECCION_FISICA"));
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaData(idVentana,false);
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
		
		mdfi.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------