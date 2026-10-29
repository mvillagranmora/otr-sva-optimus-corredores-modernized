/*************************************************************
 * Purpose	: Modal Telefono
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalTelefonoCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
		var mte = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mte.IdPersona = parametros.IdPersona;
		mte.Telefono = $.extend({},parametros.Telefono);
		mte.CodigoFormulario = parametros.CodigoFormulario;
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
		
		mte.cmbbxEstadoTelefono = {
			filtros : {
				IdEstado : {field: "IdEstado", operator: "ne", value: 'PEN'}
			}
		};

		if($scope.SesionLocale=='PE') {
			$scope.PropositoRecadoExcluido = {field: "IdPropositoMedioContacto", operator: "neq", value: 'RECA'};
			$scope.PropositoFacturacionExcluido = {field: "IdPropositoMedioContacto", operator: "neq", value: 'FACT'};
			
			mte.configuracionProposito = {
				filtros : {
					complejo : [$scope.PropositoRecadoExcluido, $scope.PropositoFacturacionExcluido],
					logic: "and",
					filters: [$scope.PropositoRecadoExcluido,$scope.PropositoFacturacionExcluido]
				}
			};
		}
		
		mte.aceptar = function() {
			if(!mte.validaFormatoTelefono()) return;
			
			//modificacion
			if (mte.Telefono.IdTelefono){
				var campos = [
							{ nombre: "IdPais" 			, tipo : "combo"},
							{ nombre: "IdTipoTelefono"	, tipo : "combo"},
							{ nombre: "Numero" 			, tipo : "texto"},
							{ nombre: "Anexo" 			, tipo : "texto"},
							{ nombre: "CodigoProposito" , tipo : "combo"},
							{ nombre: "Comentario" 		, tipo : "texto"},
							{ nombre: "IdEstado" 		, tipo : "combo"}];
					
				var registroCambio = [];
				
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mte.Telefono[value.nombre] !== mte.TelefonoOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mte.Telefono[value.nombre]?mte.Telefono[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mte.Telefono[value.nombre] === false && mte.TelefonoOriginal[value.nombre] === null) && (mte.Telefono[value.nombre] != mte.TelefonoOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mte.Telefono[value.nombre]?mte.Telefono[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mte.Telefono[value.nombre] !== mte.TelefonoOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mte.Telefono[value.nombre]?util.dateSPdate(mte.Telefono[value.nombre]) :"")});
					}
					if (value.tipo === 'bool') {
						if (mte.Telefono[value.nombre] !== mte.TelefonoOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mte.Telefono[value.nombre]?"S":"N")});
					}			
				});
				
				if (registroCambio.length === 0){
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo 			:'SOC',
					p_UsaReglasNegocio 	:'S',
					p_IdTelefono		: mte.Telefono.IdTelefono,
					p_RegistroCambio	: registroCambio
				};
				//bloqueo ventana en caso de servicio lento o metan 2 clics
			    bcsComunes.ventanaOverlay(idVentana,true);
			    //llamada de servicio
				servicioSociodeNegocio.modificarPersonaTelefono(datos).$promise.then(
					//Servicio responde 200 => RESPUESTA OK,NOK
					function(result) {
						//libero ventana servicio respondio
						bcsComunes.ventanaOverlay(idVentana,false);						
						var srv=util.parseServerResponse(result);						
						//Titulo del mensaje (si no se especifica se usa default)
						var subtitulo='';
						if (srv.erroresContador > 0) {
							 subtitulo='UI_OPERACION_CON_ERRORES';
						}else if (srv.erroresContador === 0  && srv.autorizacionesContador === 0) {
							 subtitulo='UI_OPERACION_EJECUTADA';
						}else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
							 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
						}
						//Ventana de aviso
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, idVentana);
						
						//Acciones posterior del cierre de la ventana de aviso
						bcsComunes.ventanaPromesa(idMensaje).then(
							function() {
								 //Si es exitosa automatizo cierre ventana modal 
								 if (srv.erroresContador === 0) {
									 bcsComunes.ventanaData(idVentana,true);
									 bcsComunes.ventanaCerrar(idVentana);
								 }																		
							}
						);							
					},
					//Servicio responde != 200 => ERROR FATAL
					function(result){		
						//libero ventana servicio respondio
						bcsComunes.ventanaOverlay(idVentana,false);						
						var srv=util.parseServerResponse(result);
						var p=bcsComunes.ventanaMsg({ 
							mensaje : '', 
							conflictos : srv
						},idVentana);						
					});
			}
			//creacion
			else {
				var datos = {
					p_IdEmpresa 		: $scope.SesionIdEmpresa,
					p_IdUsuario 		: $scope.SesionIdUsuario,
					p_IdModulo	 		:'SOC',
					p_UsaReglasNegocio 	:'S',
					p_IdPais 			: mte.Telefono.IdPais || 0,
					p_IdTipoTelefono 	: mte.Telefono.IdTipoTelefono || '',
					p_Numero			: mte.Telefono.Numero || '',
					p_Anexo 			: mte.Telefono.Anexo || '',
					p_CodigoProposito	: mte.Telefono.CodigoProposito || '',   
					p_Comentario		: mte.Telefono.Comentario || '',
					p_IdEstado			: mte.Telefono.IdEstado || '',
					p_IdPersona			: mte.IdPersona
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaTelefono(datos).$promise.then(
					function(result) {
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Telefono agregado",
							conflictos : util.parseServerResponse(result)}, idVentana);

						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
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
					
		mte.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};

		// Se obtienen placeholder
		mte.placeholder = {};
		mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
		
		$scope.$watch('mte.Telefono.IdTipoTelefono',function(newval,oldval){
			if (newval) {
				if (mte.Telefono.IdTipoTelefono === 'FIJO') {
					mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
				}
				else if (mte.Telefono.IdTipoTelefono === 'MOVIL') {
					mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO_MOVIL;
				}
				else {
					mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
				}
			}
			else {
				mte.placeholder.telefono = $scope.$root.SesionValoresDefectoSistema.PLACEHOLDER_TELEFONO;
			}
		});

		// Valida formato Telefono
		mte.validaFormatoTelefono = function(){
			if ($scope.SesionLocale=='PE')return true;
	    	if (mte.Telefono.Numero !== undefined && mte.Telefono.Numero !== '' && region.ValidaFormatoNumeroTelefono(mte.Telefono.Numero)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_NUMERO_TELEFONO_INCORRECTO").replace("XXPLACEHOLDERXX",mte.placeholder.telefono));
				$scope.formTelefono.$valid = undefined;
				return false;
			}
	    	return true;
		}
		
		mte.camposRequeridos = {}
	});
}());
//------------------------- FIN CONTROLLER --------------------------