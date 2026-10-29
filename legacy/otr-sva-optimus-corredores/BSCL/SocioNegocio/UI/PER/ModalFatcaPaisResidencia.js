(function() {
	angular.module('ModuloSocioNegocio').controller('ModalFatcaPaisResidenciaCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
		let mfpr = this;
		let idVentana = bcsComunes.ventanaWinId($scope);
		let parametros = bcsComunes.ventanaParams(idVentana);
		mfpr.dataFatcaPaisResidencia = parametros.ObjFatcaPaisResidencia === undefined ? null : $.extend({},parametros.ObjFatcaPaisResidencia);
		mfpr.dataFatcaPaisResidenciaOriginal = parametros.ObjFatcaPaisResidencia === undefined ? null : $.extend({},parametros.ObjFatcaPaisResidencia);
		
		mfpr.TipoProceso = "";
		mfpr.CodigoFormulario = parametros.CodigoFormulario;
		mfpr.camposRequeridos = {};
			bcsServicioPlataforma.CamposRequeridosFormulario(mfpr.CodigoFormulario, function(respuesta) {
				mfpr.camposRequeridos = respuesta;
			});
		mfpr.oConfiguracionPaisResidencia = {
			filtros: {
				IdPais	: {field : "IdPais", operator : "neq", value : $scope.SesionIdPaisSistema},
			}
		};
		
		if (mfpr.dataFatcaPaisResidencia){
			//modificacion
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_PAIS_RESIDENCIA'));
			mfpr.TipoProceso = "M";
		} else{
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_PAIS_RESIDENCIA'));
			mfpr.TipoProceso = "C";
		}
		
		mfpr.aceptar = function aceptar() {
			let lstPaisResidencia = [];
			
			lstPaisResidencia.push({ 
					IdPersona 			: parametros.IdPersona,
					IdPaisResidencia	: mfpr.dataFatcaPaisResidencia.IdPaisResidencia || 0,
					TIN					: mfpr.dataFatcaPaisResidencia.TIN || '',
					IdCausaNoTin		: mfpr.dataFatcaPaisResidencia.IdCausaNoTin || '',   
					Observacion			: mfpr.dataFatcaPaisResidencia.IdCausaNoTin === 'OTRA' ? mfpr.dataFatcaPaisResidencia.CausaNoTin || '' :'',
					PaisResidenciaPrincipal	: mfpr.dataFatcaPaisResidencia.PaisResidenciaPrincipal || 'N'
 			});
					
			let datos = {
				p_IdEmpresa 			: $scope.SesionIdEmpresa,
				p_IdUsuario 			: $scope.SesionIdUsuario,
				p_FatcaPaisResidencia 	: lstPaisResidencia
			};
			
			//modificacion
			if (mfpr.TipoProceso === "M"){
				let campos = [
					{ nombre: "TIN" ,tipo : "texto"},
					{ nombre: "IdCausaNoTin" ,tipo : "combo"},
					{ nombre: "CausaNoTin" ,tipo : "texto"}
				];
				
				let contadorModificados = 0;
						
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mfpr.dataFatcaPaisResidencia[value.nombre] !== mfpr.dataFatcaPaisResidenciaOriginal[value.nombre])
							contadorModificados++;
					}
					if (value.tipo === 'combo') {
						if (!(mfpr.dataFatcaPaisResidencia[value.nombre] === "" && mfpr.dataFatcaPaisResidenciaOriginal[value.nombre] === undefined) && (mfpr.dataFatcaPaisResidencia[value.nombre] != mfpr.dataFatcaPaisResidenciaOriginal[value.nombre]))
							contadorModificados++;
					}			
				});
				
				if (contadorModificados === 0){
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.ModificarFatcaPaisResidencia(datos).$promise.then(
				function (result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					if(result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.ventanaMsg({ 
							mensaje : $translate.instant("UI_ERROR_MODIFICAR_PAIS_RESIDENCIA"),
							conflictos : util.parseServerResponse(result)}, idVentana);	
						return;
					}
						
                    let idMensaje = bcsComunes.ventanaMsg({
                    	mensaje   : $translate.instant("UI_OK_MODIFICAR_PAIS_RESIDENCIA"),
                    	conflictos: util.parseServerResponse(result)}, idVentana);
                        
					bcsComunes.ventanaPromesa(idMensaje).then(function () {
                        bcsComunes.ventanaOverlay(idVentana, false);
						bcsComunes.ventanaData(idVentana, true);
                       	bcsComunes.ventanaCerrar(idVentana);
                      });
				},
                function (result) {
                	bcsComunes.msg("error", "error", $translate.instant("UI_ERROR_MODIFICAR_PAIS_RESIDENCIA"));
                	bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaOverlay(idVentana,false);
                });
			//creacion
			} else {
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.CrearFatcaPaisResidencia(datos).$promise.then(
				function(result) {
					if(result.P_OUT_ERRORESCONTADOR > 0){
						bcsComunes.ventanaMsg({ 
							mensaje : $translate.instant("UI_ERROR_AGREGAR_PAIS_RESIDENCIA"),
							conflictos : util.parseServerResponse(result)}, idVentana);
						return;
					}
							
					let idMensaje = bcsComunes.ventanaMsg({ 
						mensaje : $translate.instant("UI_OK_PAIS_RESIDENCIA_AGREGADO"),
						conflictos : util.parseServerResponse(result)}, idVentana);
						
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);	
					});
				},
				function(result){
					console.log(result);
					bcsComunes.msg("error","error", $translate.instant("UI_ERROR_AGREGAR_PAIS_RESIDENCIA"));
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaOverlay(idVentana,false);
				});		
			}
		};
		
		mfpr.cancelar = function cancelar() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
		$scope.$watch('mfpr.dataFatcaPaisResidencia.TIN',function(newval){ 	
			if (newval !== null){
				if(newval === ""){
					mfpr.IdCausaNoTinRequerido = true;
					mfpr.IdCausaNoTinActivado = true;
				}else if(mfpr.dataFatcaPaisResidencia !== null){
					mfpr.dataFatcaPaisResidencia.IdCausaNoTin = "";
					mfpr.dataFatcaPaisResidenciaOriginal.IdCausaNoTin = "";
					mfpr.IdCausaNoTinRequerido = false;
					mfpr.IdCausaNoTinActivado = false;
					mfpr.CausaNoTinRequerido = false;
					mfpr.dataFatcaPaisResidencia.CausaNoTin = "";
				}
			}
		});
		
		$scope.$watch('mfpr.dataFatcaPaisResidencia.IdCausaNoTin',function(newval){ 
			if(newval != false){	
				if(newval === "OTRA"){
					mfpr.CausaNoTinRequerido = true;
				}else{
					mfpr.CausaNoTinRequerido = false;
					if(mfpr.TipoProceso === "M"){
						mfpr.dataFatcaPaisResidencia.CausaNoTin = "";
					}
				}
			}
		});
		
	});
}());
