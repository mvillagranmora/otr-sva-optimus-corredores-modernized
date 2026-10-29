(function() { 
	angular.module('ModuloSocioNegocio').controller('ModalRelacionEnrolCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,bcsServicioPlataforma) {
	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/
		var mr 					= this;
		mr.crearNuevaPersona 	= true;
		var idVentana 			= bcsComunes.ventanaWinId($scope);
		var parametros 			= bcsComunes.ventanaParams(idVentana);
		mr.IdEnrolamiento		= parametros.IdEnrolamiento;
		mr.CodigoFormulario		= parametros.CodigoFormulario;
		mr.Relacion				= $.extend({},parametros.RelSel);
		mr.RelacionOriginal		= $.extend({},parametros.RelSel);
		var relacion 			= '';
		mr.emailValido 			= true;
		mr.camposRequeridos 	= {};
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mr.CodigoFormulario, function(respuesta) {
			mr.camposRequeridos = respuesta;
			mr.asignarValoresPorDefecto();
			mr.cargarDatos();
		});
		
		/** Carga Direcciones Electronicas y fisicas **/
		var DatosEnrolamiento = new bcs.DataSource({
										type: "odata",
										definition : {
											serverFiltering:true,
											transport: {
												read: {
													url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700228",
													dataType: "json"
												}
											},
											filter: { field: "IdEnrolamiento", operator: "eq", value: Number(mr.IdEnrolamiento) },
											schema: {
												model: {
													fields: {
														DireccionCalle : {type: "string"},
														DireccionNumero: {type: "string"},
														NombreCiudad: {type: "string"},
														DireccionElectronica: {type: "string"},
														NumeroTelefono: {type: "string"} 
													}
												}
											}
										}
									});
									
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		 mr.cargarDatos = function() {
		 
		 	if(parametros.RelSel){
				//modificacion
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_RELACION'));
				mr.Relacion = {};
				mr.Relacion.IdPersonaDestino 			= parametros.RelSel.IdPersonaDestino;
				mr.Relacion.IdRelacionPersona			= parametros.RelSel.IdRelacionPersona;
				mr.Relacion.TipoRelacion				= parametros.RelSel.NombreTipoRelacion;
				mr.Relacion.DocIdeRelacionado			= util.formatRut(parametros.RelSel.DocumentoIdentidadDestino);
				mr.Relacion.NomComRelacionado			= parametros.RelSel.NombreCompletoDestino;
				mr.Relacion.EsIndefinida				= parametros.RelSel.EsIndefinida;
				mr.Relacion.FechaInicioRelacion			= util.date(parametros.RelSel.FechaInicioRelacion).toDate(); 
				mr.Relacion.FechaFinRelacion			= util.date(parametros.RelSel.FechaFinRelacion).toDate(); 
				mr.Relacion.IdTipoRelacion				= parametros.RelSel.IdTipoRelacion;
				mr.Relacion.PorcentajeParticipacion		= Number(parametros.RelSel.PorcentajeParticipacion);
				mr.Relacion.RetiraDocumentos			= parametros.RelSel.RetiraDocumentos === 'N' ? false:true;
				mr.Relacion.FinalidadRelacion			= parametros.RelSel.FinalidadRelacion;
				mr.Relacion.CondicionesRelacion			= parametros.RelSel.CondicionesRelacion;
				mr.Relacion.IdGradoVinculacionParental	= parametros.RelSel.IdGradoVinculacionParental;
				
				mr.Relacion.Ciudad		= parametros.RelSel.Ciudad;
				mr.Relacion.Calle		= parametros.RelSel.Calle;
				mr.Relacion.Numero		= parametros.RelSel.Numero;
				mr.Relacion.Telefono	= parametros.RelSel.Telefono;
				mr.Relacion.Email		= parametros.RelSel.Email;
				mr.Relacion.Complemento	= parametros.RelSel.Complemento;
				mr.Relacion.IdCiudad    = parametros.RelSel.IdCiudad;
				
				mr.RelacionOriginal	= $.extend({},mr.Relacion);
				mr.Relacion.modoCrear = false;
			}
			else{
				bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_RELACION'));
				mr.Relacion.IdEstado				= 'ACT';
				mr.Relacion.FechaInicioRelacion		= util.date($scope.$root.SesionFechaSistema).toDate();
				mr.Relacion.FechaFinRelacion		= util.date(new Date($scope.$root.SesionFechaSistema).getTime()+(365*24*60*60*1000)).toDate(); // un año.
				mr.Relacion.PorcentajeParticipacion = 0;
				mr.Relacion.modoCrear = true;
				
				DatosEnrolamiento.query().then(function(){
					if(DatosEnrolamiento.data() !== null ){
						if(DatosEnrolamiento.data().length > 0){
						
							mr.Relacion.IdCiudad = DatosEnrolamiento.data()[0].IdCiudad;
							mr.Relacion.Ciudad	= DatosEnrolamiento.data()[0].NombreCiudad;
							mr.Relacion.Calle	= DatosEnrolamiento.data()[0].DireccionCalle;
							mr.Relacion.Numero	= DatosEnrolamiento.data()[0].DireccionNumero;
							mr.Relacion.Email	= DatosEnrolamiento.data()[0].DireccionElectronica;
							mr.Relacion.Telefono= DatosEnrolamiento.data()[0].NumeroTelefono;
						}	
					}
				});
			}
		 
		 }
		
		
		mr.aceptar = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			var fechaInicioRelacion = mr.Relacion.FechaInicioRelacion || '0001-01-01 00:00:00';
			var fechaFinRelacion = mr.Relacion.FechaFinRelacion || '0001-01-01 00:00:00';
			var datos = {
					p_IdEmpresa			: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'SOC',
					p_UsaReglasNegocio	: 'S',
					p_CodigoFormulario  : mr.CodigoFormulario,
					p_IdTipoCanal		: 'OPT',
					p_RelacionesPersona	: []
					
						
			};
			
			const gradoVinculacionParental = getGradoVinculacionParental();
			const condicionesRelacion = getCondicionesRelacion();
			const finalidadRelacion = getFinalidadRelacion();
			
			if (mr.Relacion.IdRelacionPersona){ //modificacion
				datos.p_RelacionesPersona = [{
					IdInterno					: 1,
					IdRelacionPersona			: mr.Relacion.IdRelacionPersona || 0,
					IdEnrolamientoOrigen		: mr.IdEnrolamiento,
					IdTipoRelacion				: mr.RelacionOriginal.IdTipoRelacion || "",
					IdPersonaDestino			: mr.RelacionOriginal.IdPersonaDestino || 0,
					FechaInicioRelacion			: mr.RelacionOriginal.EsIndefinida === 'N' ? util.dateSPseconddate(util.dateInicioDia(fechaInicioRelacion))	: '0001-01-01 00:00:00',
					FechaFinRelacion			: mr.RelacionOriginal.EsIndefinida === 'N' ? util.dateSPseconddate(util.dateInicioDia(fechaFinRelacion)) : '0001-01-01 00:00:00',
					PorcentajeParticipacion 	: mr.Relacion.PorcentajeParticipacion || 0,
					RetiraDocumentos			: mr.Relacion.RetiraDocumentos? 'S':'N',
					FinalidadRelacion			: finalidadRelacion || '',
					CondicionesRelacion			: condicionesRelacion || '',
					IdGradoVinculacionParental	: gradoVinculacionParental || 0,
					Ciudad						: mr.Relacion.Ciudad ||'',
					IdCiudad					: mr.Relacion.IdCiudad ||'',
					Calle						: mr.Relacion.Calle ||'',
					Numero						: mr.Relacion.Numero ||'',
					Telefono					: mr.Relacion.Telefono ||'',
					Email						: mr.Relacion.Email ||'',
					Complemento					: mr.Relacion.Complemento ||''
				}]
				servicioSociodeNegocio.modificarBorradorRelacionPersona(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if(result.P_OUT_ERRORESCONTADOR > 0){
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_MODIFICAR_RELACION'),conflictos : util.parseServerResponse(result)}, idVentana);
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
							});
							return;
						}
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_RELACION_MODIFICADA'),conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(result){
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_MODIFICAR_RELACION'),conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
					});
			}
			else{ //creacion
				datos.p_RelacionesPersona = [{
					IdInterno					: 1,
					IdRelacionPersona			: mr.Relacion.IdRelacionPersona || 0,
					IdEnrolamientoOrigen		: mr.IdEnrolamiento,
					IdTipoRelacion				: mr.TipoRelacionObj.IdTipoRelacion || "",
					IdPersonaDestino			: mr.RelacionadoObj.IdPersona || 0,
					FechaInicioRelacion			: mr.TipoRelacionObj.EsIndefinida === 'N' ? util.dateSPseconddate(util.dateInicioDia(fechaInicioRelacion))	: '0001-01-01 00:00:00',
					FechaFinRelacion			: mr.TipoRelacionObj.EsIndefinida === 'N' ? util.dateSPseconddate(util.dateInicioDia(fechaFinRelacion))		: '0001-01-01 00:00:00',
					PorcentajeParticipacion 	: mr.Relacion.PorcentajeParticipacion || 0,
					RetiraDocumentos			: mr.Relacion.RetiraDocumentos? 'S':'N',
					FinalidadRelacion			: finalidadRelacion || '',
					CondicionesRelacion			: condicionesRelacion || '',
					IdGradoVinculacionParental	: gradoVinculacionParental || 0,
					Ciudad						: mr.Relacion.Ciudad ||'',
					IdCiudad					: mr.Relacion.IdCiudad ||'',
					Calle						: mr.Relacion.Calle ||'',
					Numero						: mr.Relacion.Numero ||'',
					Telefono					: mr.Relacion.Telefono ||'',
					Email						: mr.Relacion.Email ||'',
					Complemento					: mr.Relacion.Complemento ||''
				}]
				servicioSociodeNegocio.crearBorradorRelacionPersona(datos).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(idVentana,false);
						if(result.P_OUT_ERRORESCONTADOR > 0){
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_CREAR_RELACION'),conflictos : util.parseServerResponse(result)}, idVentana);
							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(idVentana,true);
							});
							return;
						}
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_RELACION_AGREGADA'),conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
						});
					},
					function(result){
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_CREAR_RELACION'),conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,false);
							bcsComunes.ventanaOverlay(idVentana,false);
						});
					}
				);
			}
		};
		
		const getGradoVinculacionParental = function() {
			if(!usaGradoVinculacionParental()) return 0;
			return mr.Relacion.IdGradoVinculacionParental;
		}
		
		const usaGradoVinculacionParental = function() {
			return mr.TipoRelacionObj.UsaGradoVinculacionParental === 'S';
		}
		
		const getCondicionesRelacion = function() {
			if(!usaCondicionesRelacion()) return '';
			return mr.Relacion.CondicionesRelacion;
		}
		
		const usaCondicionesRelacion = function() {
			return mr.TipoRelacionObj.UsaCondicionesRelacion === 'S';
		}
		
		const getFinalidadRelacion = function() {
			if(!usaFinalidadRelacion()) return '';
			return mr.Relacion.FinalidadRelacion;
		}
		
		const usaFinalidadRelacion = function() {
			return mr.TipoRelacionObj.UsaFinalidadRelacion === 'S';
		}
		
		mr.asignarValoresPorDefecto = function (){
			if(parametros.RelSel) return;
			$.each(mr.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mr.Relacion[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mr.Relacion[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}
		
		
		mr.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mr.conf = {};
		mr.conf.oConfiguracionTipoRelacion={
			tipo : "combobox-tipo-relacion",
			configuracion : {},
			filtros : {
				Administracion 	: {"field" : "Administracion", "operator" : "eq", "value" : 'RELACIONADOS'},
				IdEstado 		: {"field" : "IdEstado", "operator" : "eq", "value" : 'ACT'}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
	
		mr.conf.oConfiguracionRelacionado={
			tipo : "combobox-persona-tipo-relacion",
			configuracion : {},
			filtros : {
				IdTipoRelacion : {field: "IdTipoRelacion", operator: "eq", value: ""}
			},
			seleccionado : false,
			idActual  : false,
			idDefecto : ''
		};
		
		
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		mr.crearPersona = function(){
			var id1 = bcsComunes.ventanaAbrir("CrearPersona",{ },false,false,idVentana); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true){
	        		mr.oConfiguracionRelacionado.filtros.read = true;
				}
	    	}); 
		};
	
		
		mr.validarFormulario = function(){
			if(mr.TipoRelacionObj && (mr.Relacion.EsIndefinida === 'N' || mr.TipoRelacionObj.EsIndefinida === 'N')){
				if(mr.Relacion.FechaInicioRelacion && mr.Relacion.FechaFinRelacion && mr.Relacion.FechaInicioRelacion.getTime() < mr.Relacion.FechaFinRelacion.getTime()){
					if(mr.formRelacion.$valid || !mr.Relacion.modoCrear){
						return true;
					}
				}
				return false;
			}else if(mr.formRelacion.$valid || !mr.Relacion.modoCrear){
				return true;
			}
		}


		
		
		//IMPCREDOPT-2320
		$scope.$watch("mr.TipoRelacionObj",function(newval,oldval){ 
			
			if (newval == oldval || newval === '' || newval === undefined || !newval) return;
			relacion=mr.TipoRelacionObj.IdTipoRelacion;
			
			if(relacion === 'REPAD' || relacion === 'REPLEG' || relacion ==='RELEEN' ){
				mr.conf.oConfiguracionSocioNegocio={
						filtros: {
									IdEmpresa : {"field" : "TipoPersona", "operator" : "neq", "value" : "PERSONA JURIDICA"}
								}
						};
				}else{
					mr.conf.oConfiguracionSocioNegocio={
							filtros: {
										IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" :$scope.SesionIdEmpresa}
									}
							};
					}

			mr.Relacion.IdRelacionado=false;
			//Para Beneficiarios Finales: Porcentaje de participación debe quedar restringido a ser igual o mayor a 10%.
			if(mr.TipoRelacionObj.IdTipoRelacion === 'BENFIN'){
				if(mr.Relacion.PorcentajeParticipacion < 10) mr.Relacion.PorcentajeParticipacion = 10;
				mr.Relacion.PorcentajeParticipacion = 10;
				mr.conf.RelacionOpt = {
					min: 10,
					max: 999999999999999
				};
			}
			//Para Control Efectivo: Porcentaje de participación debe quedar restringido a ser menor a 10%.
			else if(mr.TipoRelacionObj.IdTipoRelacion === 'CONEFE'){
				if(mr.Relacion.PorcentajeParticipacion >= 10) mr.Relacion.PorcentajeParticipacion = 0;
				mr.conf.RelacionOpt = {
					min: 0,
					max: 9.99
				};
			}
			//El campo Porcentaje de participación queda bloqueado para la relación Alta Gerencia y la relacion Persona Declarante
			else{
				if(mr.TipoRelacionObj.IdTipoRelacion === 'AGYRLE' || mr.TipoRelacionObj.IdTipoRelacion === 'PERDEC') mr.Relacion.PorcentajeParticipacion = 0;
				mr.conf.RelacionOpt = {
					min: 0,
					max: 999999999999999
				};
			}
		});
		
		mr.validaEmail = function() { 
			
			if (mr.Relacion.Email !== undefined && mr.Relacion.Email !== '' && region.ValidaFormatoEmail(mr.Relacion.Email)==false){
	    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
	    		mr.Relacion.Email = undefined;
				return false;
			}
			else 
				mr.emailValido = true;
		}
		//$scope.recordData.email= mr.Relacion.Email;
		//$scope.validaFormatoEmail();
		
		
		
	});
}());
//------------------------- FIN CONTROLLER --------------------------