/*************************************************************
 * Purpose	: Modal Dirección Electrónica.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalDireccionElectronicaCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
		var mdie = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mdie.IdPersona = parametros.IdPersona;
		mdie.DireccionElectronica = $.extend({},parametros.DireccionElectronica);
		mdie.DireccionElectronicaOriginal = $.extend({},parametros.DireccionElectronica);
		mdie.CodigoFormulario = parametros.CodigoFormulario;
		mdie.camposRequeridos = {};
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mdie.CodigoFormulario, function (respuesta) {
			mdie.camposRequeridos = respuesta;
		});

		if (parametros.DireccionElectronica)
		{
			//modificacion
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_DIRECCION_ELECTRONICA'));
		}
		else
		{
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_DIRECCION_ELECTRONICA'));
			mdie.DireccionElectronica = {};
			mdie.DireccionElectronicaOriginal.IdEstado = 'ACT';
		}
		
		mdie.cmbbxDireccionElectronica = {
			filtros : {
				IdEstado : {field: "IdEstado", operator: "ne", value: 'PEN'}
			}
		};
		
		if($scope.SesionLocale=='PE') {
			$scope.PropositoRecadoExcluido = {field: "IdPropositoMedioContacto", operator: "neq", value: 'RECA'};
			$scope.PropositoFacturacionExcluido = {field: "IdPropositoMedioContacto", operator: "neq", value: 'FACT'};
			
			mdie.configuracionProposito = {
				filtros : {
					complejo : [$scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido],
					logic: "and",
					filters: [$scope.PropositoRecadoExcluido,$scope.PropositoFacturacionExcluido]
				}
			};
		}
		 
		mdie.aceptar = function() {
			if(!mdie.validaFormatoEmail()) return;
			//modificacion
			if (mdie.DireccionElectronica.IdDireccionElectronica){
				
				var campos = [
							{ nombre: "Direccion" ,tipo : "texto"},
							{ nombre: "CodigoProposito" ,tipo : "combo"},
							{ nombre: "Comentario" ,tipo : "texto"},
							{ nombre: "IdEstado" ,tipo : "combo"}];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mdie.DireccionElectronica[value.nombre] !== mdie.DireccionElectronicaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdie.DireccionElectronica[value.nombre]?mdie.DireccionElectronica[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mdie.DireccionElectronica[value.nombre] === false && mdie.DireccionElectronicaOriginal[value.nombre] === null) && (mdie.DireccionElectronica[value.nombre] != mdie.DireccionElectronicaOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdie.DireccionElectronica[value.nombre]?mdie.DireccionElectronica[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mdie.DireccionElectronica[value.nombre] !== mdie.DireccionElectronicaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdie.DireccionElectronica[value.nombre]?util.dateSPdate(mdie.DireccionElectronica[value.nombre]) :"")});
					}
					if (value.tipo === 'bool') {
						if (mdie.DireccionElectronica[value.nombre] !== mdie.DireccionElectronicaOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mdie.DireccionElectronica[value.nombre]?"S":"N")});
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
					p_IdDireccionElectronica: mdie.DireccionElectronica.IdDireccionElectronica,
					p_RegistroCambio: registroCambio
				};
				bcsComunes.ventanaOverlay(idVentana,true);				
				servicioSociodeNegocio.modificarPersonaDireccionElectronica(datos).$promise.then(
					function(result) {
						if (result.P_OUT_ERRORESCONTADOR>0) {						
							bcsComunes.ventanaMsg({ mensaje : result.P_OUT_ERRORESDETALLE[0].Codigo,
								conflictos : util.parseServerResponse(result)}, idVentana);
							bcsComunes.msg("error","Error",$translate.instant('UI_ERROR_CORREO_PREDETERMINADO'));

							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);	
						});							
						} else {						
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Direccion electronica modificada",
								conflictos : util.parseServerResponse(result)}, idVentana);
							
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
								bcsComunes.ventanaCerrar(idVentana);
								bcsComunes.ventanaOverlay(idVentana,false);	
							});							
						}						
					},
					function(){
						bcsComunes.msg("error","error","Error al agregar direccion electronica");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
				
			}
			//creacion
			else {
				
				var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_UsaReglasNegocio :'S',
					p_Direccion : mdie.DireccionElectronica.Direccion || '',
					p_CodigoProposito: mdie.DireccionElectronica.CodigoProposito || '',   
					p_Comentario: mdie.DireccionElectronica.Comentario || '',
					p_IdEstado: mdie.DireccionElectronica.IdEstado || '',
					p_IdPersona: mdie.IdPersona,
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaDireccionElectronica(datos).$promise.then(
					function(result) {
						var resp = util.parseServerResponse(result);
						var idMensaje = bcsComunes.ventanaMsg({ 
							mensaje : resp.errores.length == 0 ? "Dirección electrónica agregada" : "Error al agregar Dirección electrónica",
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
						bcsComunes.msg("error","error","Error al agregar telefono");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					});
			}
		};
		
		mdie.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		// Valida formato email
		mdie.validaFormatoEmail = function(){
	    	if (mdie.DireccionElectronica.Direccion !== undefined && mdie.DireccionElectronica.Direccion !== '' && region.ValidaFormatoEmail(mdie.DireccionElectronica.Direccion)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
				$scope.formDireccionElectronica.$valid = undefined;
				return false;
			}
	    	return true;
	    }
	});
}());
//------------------------- FIN CONTROLLER --------------------------