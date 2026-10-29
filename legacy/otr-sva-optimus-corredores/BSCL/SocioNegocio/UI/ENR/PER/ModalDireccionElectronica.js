(function() {
	angular.module('ModuloSocioNegocio').controller('ModalDireccionElectronicaEnrolCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
		var mdie = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mdie.IdEnrolamiento = parametros.IdEnrolamiento;
		mdie.DireccionElectronica = $.extend({},parametros.DireccionElectronica);
		mdie.camposRequeridos = {};
		mdie.CodigoFormulario = parametros.CodigoFormulario;

		bcsServicioPlataforma.CamposRequeridosFormulario(mdie.CodigoFormulario, function(respuesta) {
			mdie.camposRequeridos = respuesta;
			mdie.asignarValoresPorDefecto();
			mdie.cargarDatos();
		});	
		
		mdie.cargarDatos = function(){
			
			if (parametros.DireccionElectronica){
				mdie.DireccionElectronicaOriginal = $.extend({},parametros.DireccionElectronica);
				mdie.DireccionElectronica.CodigoProposito = mdie.DireccionElectronicaOriginal.CodigoProposito;
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_DIRECCION_ELECTRONICA'));
			}else{
				mdie.DireccionElectronicaOriginal = $.extend({},mdie.DireccionElectronica);
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_DIRECCION_ELECTRONICA'));
				mdie.DireccionElectronicaOriginal.IdEstado = 'ACT';
			}
		}
		
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
			const predeterminado = mdie.DireccionElectronica.Predeterminado === 'UI_SI' ? 'S' : 'N';
			const datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoFormulario		: mdie.CodigoFormulario,
				p_IdTipoCanal			: 'OPT',
				p_IdPersona: mdie.IdPersona,
				p_DireccionesElectronicas:[{
					IdInterno : 1,
					IdDireccionElectronica:  mdie.DireccionElectronica.IdDireccionElectronica || 0,
					IdEnrolamiento: mdie.IdEnrolamiento,
					Direccion: mdie.DireccionElectronica.Direccion || '',
					CodigoProposito: mdie.DireccionElectronica.CodigoProposito || '',
					Comentario: mdie.DireccionElectronica.Comentario || '',
					Predeterminado: predeterminado || 'N'
				 }
			  ]
			}

			bcsComunes.ventanaOverlay(idVentana,true);
			if (mdie.DireccionElectronica.IdDireccionElectronica){
				servicioSociodeNegocio.modificarBorradorDireccionElectronica(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.msg("error","error","Error al modificar dirección electrónica");
							return;
						}
						
						bcsComunes.msg("info","info","Dirección Electrónica modificada");
						
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Dirección Electrónica modificada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
	
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(){
						bcsComunes.msg("error","error","Error al modificar dirección electrónica");
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
			}else {				
				servicioSociodeNegocio.crearBorradorDireccionElectronica(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR > 0){
							bcsComunes.msg("error","error","Error al agregar dirección electrónica");
							return;
						}
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Dirección Electrónica agregada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(){
						bcsComunes.msg("error","error","Error al agregar dirección electrónica");
						bcsComunes.ventanaData(idVentana,false);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
			}
		};
		
		mdie.asignarValoresPorDefecto = function (){
			if(parametros.DireccionElectronica) return;
			$.each(mdie.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mdie.DireccionElectronica[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mdie.DireccionElectronica[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});	
		}
	
		mdie.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
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