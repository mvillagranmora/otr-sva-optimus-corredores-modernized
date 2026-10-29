(function() {
	angular.module('ModuloPlataforma').controller('AgregarModificarConfiguracionPerfilComercialAdicionalCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes) {
		
		let amcpca = this;
		let idVentana = bcsComunes.ventanaWinId($scope);
		let parametros = bcsComunes.ventanaParams(idVentana);
		amcpca.ConfiguracionEnrolamiento = $.extend({},parametros.ConfiguracionEnrolamiento);
		amcpca.ConfiguracionEnrolamientoOriginal = $.extend({},parametros.ConfiguracionEnrolamiento);
		amcpca.EsPortafolioAPV = false;
		$scope.HabilitarCodigoPortafolio = "S";
		if (parametros.ConfiguracionEnrolamiento)
		{
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_CONFIGURACION_ENROLAMIENTO'));
			amcpca.ConfiguracionEnrolamientoOriginal.IdFormaCobro = amcpca.ConfiguracionEnrolamientoOriginal.IdFormaCobro;
		}
		else
		{
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_CONFIGURACION_ENROLAMIENTO'));
			amcpca.ConfiguracionEnrolamiento = {};
		}

		amcpca.oConfiguracionFormaCobro = { 
			filtros : { 
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				CodigoTipoIndicador : { field : "CodigoTipoIndicador", operator : "eq", value :'COBROCUST'}
			}
		};

		amcpca.oConfiguracionPerfilCobros = { 
			filtros : { 
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				IdEstado : {field: "EstadoPerfilCobros", operator: "eq", value: 'ACT'},
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: 'N'}
			}
		};

		amcpca.oConfiguracionPerfilProductos = { 
			filtros : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				IdEstado : {field: "EstadoPerfilProductos", operator: "eq", value: 'ACT'},
				EsPerfilPersonalizado : {field: "EsPerfilPersonalizado", operator: "eq", value: 'N'},
				IdCategoriaInversionista : {field : "IdCategoriaInversionista", operator: "eq", value : "" }
			}
		};

		amcpca.oConfiguracionPerfilComercialBase = { 
			filtros : { 
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				IdEstado : {field: "IdEstado", operator: "eq", value: 'ACT'},
				IdCategoriaInversionista : { field : "IdCategoriaInversionista", operator: "eq", value : "" }
			}
		};

		$scope.$watch('amcpca.ConfiguracionEnrolamiento.IdCategoriaInversionista',function(newVal, oldVal){
			if( newVal && newVal != oldVal ){
				amcpca.oConfiguracionPerfilProductos.filtros.IdCategoriaInversionista.value = amcpca.ConfiguracionEnrolamiento.IdCategoriaInversionista;
				amcpca.oConfiguracionPerfilComercialBase.filtros.IdCategoriaInversionista.value = amcpca.ConfiguracionEnrolamiento.IdCategoriaInversionista;
			}
		});

		$scope.$watch('amcpca.EsPortafolioAPV',function(newVal, oldVal){
			if( amcpca.ConfiguracionEnrolamiento.CodigoPortafolio === "APV" ) amcpca.EsPortafolioAPV = true;
		});
		
		amcpca.aceptar = function() {
		
			if(amcpca.ConfiguracionEnrolamiento.IdPerfilComercialAdicional){

				amcpca.ConfiguracionEnrolamiento.CodigoPortafolio = amcpca.EsPortafolioAPV ? 'APV' : amcpca.ConfiguracionEnrolamiento.CodigoPortafolio;
				
				var campos = [
					{ nombre: "IdCategoriaInversionista",tipo : "combo"},
					{ nombre: "IdFormaCobro",tipo : "combo"},
					{ nombre: "CodigoPortafolio" ,tipo : "combo", valorBlanco: null},
					{ nombre: "IdPerfilComercialBase",tipo : "combo", valorBlanco: null},
					{ nombre: "IdPerfilCobros" ,tipo : "combo"},
					{ nombre: "IdPerfilProductos",tipo : "combo"}
				];

				const obtenerValorCampo = function (campo) {
					switch (campo.tipo) {
						case 'texto': {
							return amcpca.ConfiguracionEnrolamiento[campo.nombre] || (campo.hasOwnProperty('valorBlanco') ? campo.valorBlanco : '');
						}
						case 'combo': {
							return amcpca.ConfiguracionEnrolamiento[campo.nombre] || (campo.hasOwnProperty('valorBlanco') ? campo.valorBlanco : '');
						}
						case 'fecha': {
							return amcpca.ConfiguracionEnrolamiento[campo.nombre] || '0001-01-01';
						}
						case 'bool': {
							return amcpca.ConfiguracionEnrolamiento[campo.nombre] ? 'S' : 'N';
						}
						default: return '';
					}
				};

				var registroCambio = [];
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (amcpca.ConfiguracionEnrolamiento[value.nombre] !== amcpca.ConfiguracionEnrolamientoOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
					}
					if (value.tipo === 'combo') {
						if (!((amcpca.ConfiguracionEnrolamiento[value.nombre] === false || amcpca.ConfiguracionEnrolamiento[value.nombre] === undefined) && (amcpca.ConfiguracionEnrolamientoOriginal[value.nombre] === null || amcpca.ConfiguracionEnrolamientoOriginal[value.nombre] == 0)) && (amcpca.ConfiguracionEnrolamiento[value.nombre] != amcpca.ConfiguracionEnrolamientoOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
					}
					if (value.tipo === 'fecha') {
						if (amcpca.ConfiguracionEnrolamiento[value.nombre] !== amcpca.ConfiguracionEnrolamientoOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
					}
					if (value.tipo === 'bool') {
						if (amcpca.ConfiguracionEnrolamiento[value.nombre] !== amcpca.ConfiguracionEnrolamientoOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
					}			
				});
			
				if (registroCambio.length === 0){
					bcsComunes.msg("warning","info",$translate.instant('UI_SIN_MODIFICACIONES_POR_GUARDAR'));
					return;
				}

				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo : 'SOC',
					p_UsaReglasNegocio : 'S',
					p_IdPerfilComercialAdicional: amcpca.ConfiguracionEnrolamiento.IdPerfilComercialAdicional,
					p_RegistroCambio : registroCambio,
				};

				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarConfiguracionPerfilComercialAdicional(datos).$promise.then(
					function(result) {
						let resp = util.parseServerResponse(result);
						let idMensaje = bcsComunes.ventanaMsg({ 
							mensaje : resp.errores.length == 0 ? "Perfil Comercial Adicional modificado" : "Error al modificar Perfil Comercial Adicional",
							conflictos : resp}, idVentana);

						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							if (resp.errores.length == 0){
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
						});
					},
					function(){
						bcsComunes.msg("error","error","Error al modificar Perfil Comercial Adicional");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});

			} else {

				let datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo : 'SOC',
					p_UsaReglasNegocio :'S',
					p_ConfiguracionesPerfilComercialAdicional : [{
						IdCategoriaInversionista	: amcpca.ConfiguracionEnrolamiento.IdCategoriaInversionista,
						IdFormaCobro 				: amcpca.ConfiguracionEnrolamiento.IdFormaCobro,
						CodigoPortafolio 			: amcpca.EsPortafolioAPV ? 'APV' : amcpca.ConfiguracionEnrolamiento.CodigoPortafolio,
						IdPerfilComercialBase		: amcpca.ConfiguracionEnrolamiento.IdPerfilComercialBase,
						IdPerfilCobros				: amcpca.ConfiguracionEnrolamiento.IdPerfilCobros,
						IdPerfilProductos			: amcpca.ConfiguracionEnrolamiento.IdPerfilProductos
					}]
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearConfiguracionPerfilComercialAdicional(datos).$promise.then(
					function(result) {
						let resp = util.parseServerResponse(result);
						let idMensaje = bcsComunes.ventanaMsg({ 
							mensaje : resp.errores.length == 0 ? "Perfil Comercial Adicional agregado" : "Error al agregar Perfil Comercial Adicional",
							conflictos : resp}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							if (resp.errores.length == 0){
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);
							}
						});
						
					},
					function(){
						bcsComunes.msg("error","error","Error al agregar Perfil Comercial Adicional");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}	
		};

		amcpca.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		bcsComunes.ventanaAutoHeight(idVentana);
		
	});	
}());