/*************************************************************
 * Purpose	: Modal Relacion
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 * 
 * Purpose	: Se incorporan definiciones Optimus Perú
 * Author	: JGarces - BCS
 * Date		: 14/03/2017
 *************************************************************/
 (function() { 
	angular.module('ModuloSocioNegocio').controller('ModalRelacionCtrl',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes, bcsServicioPlataforma) {
	/*********************************************
	 * VARIABLES Y CONTEXTOS
	 ********************************************/
		var mr 					= this;
		mr.crearNuevaPersona 	= true;
		var idVentana 			= bcsComunes.ventanaWinId($scope);
		var parametros 			= bcsComunes.ventanaParams(idVentana);
		mr.IdPersona			= parametros.IdPersona;
		mr.Relacion				= $.extend({},parametros.RelSel);
		var relacion 			= '';
		var filtropersona;
		mr.emailValido 			= true;
		mr.CodigoFormulario 	= parametros.CodigoFormulario;
		mr.camposRequeridos = {};
		mr.configData={};
		mr.ciudadSeleccionado={};
		mr.primeraCargaRelacion = true;
		
		bcsServicioPlataforma.CamposRequeridosFormulario(mr.CodigoFormulario, function(respuesta) {
			mr.camposRequeridos = respuesta;
			mr.asignarValoresPorDefecto();
			mr.cargarDatos();
		});
		
		/** Carga Direcciones Electronicas y fisicas **/
		var DireccionesFisicas = new bcs.DataSource({
										type: "odata",
										definition : {
											serverFiltering:true,
											transport: {
												read: {
													url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1313",
													dataType: "json"
												}
											},
											filter: { field: "IdPersona", operator: "eq", value: Number(mr.IdPersona) },
											schema: {
												model: {
													fields: {
														IdDireccionFisica : { type: "number"},
														Calle : { type: "string"},
														IdPersona : { type: "number"},
														Numero : { type: "string"},
														Departamento : { type: "string"},
														Comentario: { type: "string"},
														Sector : { type: "string"},
														CodigoPostal : { type: "string"},
														CodigoProposito : { type: "string"},
														DescripcionProposito : { type: "string"},
														IdCiudad : { type: "number"},
														IdDivisionPais : { type: "number"},
														NombreCiudad : { type: "string"},
														NombreDivisionPais : { type: "string"},
														NombrePais : { type: "string"},
														IdEstado : { type: "string"},
														NombreEstado : { type: "string"},
														FechaIngreso : { type: "date"},
														FechaCreacion : { type: "date"},
														FechaModificacion : { type: "date"},
														CodigoCompuesto : {type:"string"},
														Predeterminado : {type: "string"},
														CodigoDivisionPais : { type: "string"},
														PaisNombreDivisionPais: {type: "string"}
													}
												}
											}
										}
									});
		
		
		
		var DireccionesElectronicas = new bcs.DataSource({
											type : "odata",
											definition : {
												serverFiltering:true,
												transport: {
													read: {
														url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1311",
														dataType: "json"
													}
												},
												filter: { field: "IdPersona", operator: "eq", value: Number(mr.IdPersona) },
												schema: {
													model: {
														fields: {
															IdDireccionElectronica : { type: "number"},
															Direccion : { type: "string"},
															IdPersona : { type: "number"},
															IdEstado : { type: "string"},
															CodigoProposito : { type: "string"},
															DescripcionProposito : { type: "string"},
															Comentario: { type: "string"},
															NombreEstado : { type: "string"},
															FechaIngreso : { type: "date"},
															FechaCreacion : { type: "date"},
															FechaModificacion : { type: "date"},
															Predeterminado: {type: "string"}
														}
													}
												}
											}
										});
	
		
		

		var Telefonos = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering:true,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1319",
										dataType: "json"
									}
								},
								filter: { field: "IdPersona", operator: "eq", value: Number(mr.IdPersona) },
								schema: {
									model: {
										fields: {
											
											IdTelefono : { type: "number"},
											IdPersona : { type: "number"},
											IdPais : { type: "number"},
											NombrePais : { type: "string"},
											NombreTipoTelefono : { type: "string"},
											Comentario : { type: "string"},
											CodigoProposito : { type: "string"},
											DescripcionProposito : { type: "string"},
											Numero : { type: "string"},
											Anexo : { type: "string"},
											IdEstado : { type: "string"},
											NombreEstado : { type: "string"},
											FechaIngreso : { type: "date"},
											FechaCreacion : { type: "date"},
											FechaModificacion : { type: "date"},
											Predeterminado : { type: "string"}
										}
									}
								}
							}
						});
		

	/*********************************************
	 * ACCIONES DE PANTALLA
	 ********************************************/
	mr.cargarDatos = function cargarDatos() {
		if (parametros.RelSel) {
			mr.cargandoModificar = true;
			//modificacion
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_RELACION'));		
			mr.Relacion = mr.Relacion || {};			
				
			DireccionesFisicas.query().then(function (result) {
				if (DireccionesFisicas.data() !== null) {
					if (DireccionesFisicas.data().length > 0) {
							mr.configData.filtroCiudad = {	IdPais : {field: "IdPais", operator: "eq", value: DireccionesFisicas.data()[0].IdPais}};
						
					}
				}
			});	
			
			mr.Relacion.IdRelacionPersona = parametros.RelSel.IdRelacionPersona;
			mr.Relacion.TipoRelacion = parametros.RelSel.NombreTipoRelacion;
			mr.Relacion.DocIdeRelacionado = util.formatRut(parametros.RelSel.DocumentoIdentidadDestino);
			mr.Relacion.NomComRelacionado = parametros.RelSel.NombreCompletoDestino;
			mr.Relacion.EsIndefinida = parametros.RelSel.EsIndefinida;
			mr.Relacion.FechaInicioRelacion = util.date(parametros.RelSel.FechaInicioRelacion).toDate();
			mr.Relacion.FechaFinRelacion = util.date(parametros.RelSel.FechaFinRelacion).toDate();
			mr.Relacion.IdTipoRelacion = parametros.RelSel.IdTipoRelacion;
			mr.Relacion.IdRelacionado = parametros.RelSel.IdRelacionado || parametros.RelSel.IdPersonaDestino;

			mr.RelacionadoObj = {
				IdPersona: parametros.RelSel.IdRelacionado || parametros.RelSel.IdPersonaDestino
			};

			mr.Relacion.PorcentajeParticipacion = Number(parametros.RelSel.PorcentajeParticipacion).toFixed(2).replace('.', ',');
			mr.Relacion.RetiraDocumentos = parametros.RelSel.RetiraDocumentos === 'N' ? false : true;
			mr.Relacion.FinalidadRelacion = parametros.RelSel.FinalidadRelacion;
			mr.Relacion.CondicionesRelacion = parametros.RelSel.CondicionesRelacion;
			mr.Relacion.IdGradoVinculacionParental = parametros.RelSel.IdGradoVinculacionParental;

			mr.Relacion.IdCiudad = parametros.RelSel.IdCiudad
			mr.Relacion.Ciudad = parametros.RelSel.Ciudad;
			mr.Relacion.Calle = parametros.RelSel.Calle;
			mr.Relacion.Numero = parametros.RelSel.Numero;
			mr.Relacion.Telefono = parametros.RelSel.Telefono;
			mr.Relacion.Email = parametros.RelSel.Email;
			mr.Relacion.Complemento = parametros.RelSel.Complemento;

			mr.RelacionOriginal = $.extend({}, mr.Relacion);
			mr.Relacion.modoCrear = false;

			setTimeout(function() {
				mr.cargandoModificar = false;
			}, 0);
		} else {
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_RELACION'));
			mr.Relacion = {};
			mr.Relacion.IdEstado = 'ACT';
			mr.Relacion.FechaInicioRelacion = util.date($scope.$root.SesionFechaSistema).toDate();
			mr.Relacion.FechaFinRelacion = util.date(new Date($scope.$root.SesionFechaSistema).getTime() + (365 * 24 * 60 * 60 * 1000)).toDate(); // un año.
			mr.Relacion.PorcentajeParticipacion = 0;
			mr.Relacion.modoCrear = true;
		}
	};
		
		mr.aceptar = function(){
		
			const gradoVinculacionParental = getGradoVinculacionParental();
			const condicionesRelacion = getCondicionesRelacion();
			const finalidadRelacion = getFinalidadRelacion();
			
			if (mr.Relacion.IdRelacionPersona){ //modificacion
				mr.Relacion.FechaInicioRelacion		= mr.Relacion.EsIndefinida === 'N' ? mr.Relacion.FechaInicioRelacion : '0001-01-01 00:00:00';
				mr.Relacion.FechaFinRelacion		= mr.Relacion.EsIndefinida === 'N' ? mr.Relacion.FechaFinRelacion : '0001-01-01 00:00:00';
				mr.Relacion.PorcentajeParticipacion	= mr.Relacion.PorcentajeParticipacion || 0;
				mr.Relacion.IdGradoVinculacionParental = gradoVinculacionParental || 0;
				mr.Relacion.CondicionesRelacion = condicionesRelacion || '';
				mr.Relacion.FinalidadRelacion = finalidadRelacion || '';
				
				var campos = [
					            { nombre: "PorcentajeParticipacion"		, tipo : "texto"},
								{ nombre: "FechaInicioRelacion"			, tipo : "fecha"},
								{ nombre: "FechaFinRelacion"			, tipo : "fecha"},
								{ nombre: "RetiraDocumentos"			, tipo : "bool"},
								{ nombre: "FinalidadRelacion"			, tipo : "texto"},
								{ nombre: "CondicionesRelacion"			, tipo : "texto"},
								{ nombre: "Ciudad"						, tipo : "texto"},
								{ nombre: "Calle"						, tipo : "texto"},
								{ nombre: "Numero"						, tipo : "texto"},
								{ nombre: "Email"						, tipo : "texto"},
								{ nombre: "Telefono"					, tipo : "texto"},
								{ nombre: "Complemento"					, tipo : "texto"},
								{ nombre: "IdCiudad"					, tipo : "combo"}
							];
				var registroCambio = [];
				$.each(campos,function(key,value) {
					if (value.tipo === 'texto') {
						if (mr.Relacion[value.nombre] !== mr.RelacionOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?mr.Relacion[value.nombre]:"")});
					}
					if (value.tipo === 'combo') {
						if (!(mr.Relacion[value.nombre] === false && mr.RelacionOriginal[value.nombre] === null) && (mr.Relacion[value.nombre] != mr.RelacionOriginal[value.nombre]))
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?mr.Relacion[value.nombre]:"")});
					}
					if (value.tipo === 'fecha') {
						if (mr.Relacion[value.nombre] !== mr.RelacionOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?util.dateSPseconddate(util.dateInicioDia(mr.Relacion[value.nombre])):"")});
					}
					if (value.tipo === 'bool') {
						if (mr.Relacion[value.nombre] !== mr.RelacionOriginal[value.nombre])
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mr.Relacion[value.nombre]?"S":"N")});
					}			
				});
				if (registroCambio.length === 0){
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					return;
				}
				var datos = {
					p_IdEmpresa			: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'SOC',
					p_UsaReglasNegocio	: 'S',
					p_IdRelacionPersona	: mr.Relacion.IdRelacionPersona,
					p_RegistroCambio	: registroCambio
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaRelacion(datos).$promise.then(
					function(result) {
						console.log(result);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_RELACION_MODIFICADA'),
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
					},
					function(result){
						console.log(result);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_MODIFICAR_RELACION'),
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
					});
			}
			else{ //creacion
				if (!mr.TipoRelacionObj) {
					mr.TipoRelacionObj = {};
				}
	
				var datos = {
					p_IdEmpresa			: $scope.SesionIdEmpresa,
					p_IdUsuario			: $scope.SesionIdUsuario,
					p_IdModulo			: 'SOC',
					p_UsaReglasNegocio	: 'S',
					p_IdTipoRelacion			: mr.TipoRelacionObj.IdTipoRelacion || '',
					p_IdPersonaOrigen			: Number(mr.IdPersona),
					p_IdPersonaDestino			: mr.RelacionadoObj.IdPersona,
					p_IdEstado					: mr.Relacion.IdEstado || '',
					p_FechaInicioRelacion		: mr.TipoRelacionObj.EsIndefinida === 'N' ? util.dateSPseconddate(util.dateInicioDia(mr.Relacion.FechaInicioRelacion))	: '0001-01-01 00:00:00',
					p_FechaFinRelacion			: mr.TipoRelacionObj.EsIndefinida === 'N' ? util.dateSPseconddate(util.dateInicioDia(mr.Relacion.FechaFinRelacion))		: '0001-01-01 00:00:00',
					p_PorcentajeParticipacion 	: mr.Relacion.PorcentajeParticipacion || 0,
					p_RetiraDocumentos			: mr.Relacion.RetiraDocumentos? 'S':'N',
					p_FinalidadRelacion			: finalidadRelacion || '',
					p_CondicionesRelacion		: condicionesRelacion || '',
					p_IdGradoVinculacionParental: gradoVinculacionParental || 0,
					p_Ciudad					: mr.ciudadSeleccionado.NombreCiudad||'',
					p_Calle						: mr.Relacion.Calle ||'',
					p_Numero					: mr.Relacion.Numero ||'',
					p_Telefono					: mr.Relacion.Telefono ||'',
					p_Email						: mr.Relacion.Email ||'',
					p_Complemento				: mr.Relacion.Complemento ||'',
					p_TipoRelacionUsaPorcentajeParticipacion	: mr.TipoRelacionObj.UsaPorcentajeParticipacion || '',
					p_TipoRelacionEsIndefinida					: mr.TipoRelacionObj.EsIndefinida || mr.Relacion.EsIndefinida || '',
					p_TipoRelacionUsaFinalidadRelacion			: mr.TipoRelacionObj.UsaFinalidadRelacion || '',
					p_TipoRelacionUsaCondicionesRelacion		: mr.TipoRelacionObj.UsaCondicionesRelacion || '',
					p_TipoRelacionUsaGradoVinculacionParental	: mr.TipoRelacionObj.UsaGradoVinculacionParental || ''
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.crearPersonaRelacion(datos).$promise.then(
					function(result) {
						console.log(result);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_RELACION_AGREGADA'),
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							bcsComunes.ventanaCerrar(idVentana);
							bcsComunes.ventanaOverlay(idVentana,false);	
						});
					},
					function(result){
						console.log(result);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_ERROR_CREAR_RELACION'),
							conflictos : util.parseServerResponse(result)}, idVentana);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,false);
							bcsComunes.ventanaCerrar(idVentana);
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
	
		//configuracion combo ciudad.
		mr.configData.filtroCiudad = {
			IdPais : {field: "IdPais", operator: "eq", value: -1}
		};	
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

			mr.cambioTipoRelacion = true;	
			//Para Beneficiarios Finales: Porcentaje de participación debe quedar restringido a ser igual o mayor a 10%.
			if(mr.TipoRelacionObj.IdTipoRelacion === 'BENFIN'){
				if(mr.Relacion.PorcentajeParticipacion < 10) mr.Relacion.PorcentajeParticipacion = 10;
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
		function limpiarDatosRelacion() {
			if (!mr.Relacion.modoCrear) {
				return;
			}

			console.warn('SE EJECUTA limpiarDatosRelacion');
			mr.Relacion.Ciudad = '';
			mr.Relacion.Calle = '';
			mr.Relacion.Numero = '';
			mr.Relacion.IdCiudad = null;
			mr.Relacion.Email = '';
			mr.Relacion.Telefono = '';
			mr.Relacion.Complemento = '';
		
			mr.configData.filtroCiudad = {
				IdPais: {
					field: 'IdPais',
					operator: 'eq',
					value: -1
				}
			};
		}
		
		$scope.$watch('mr.Relacion.IdRelacionado', function(nval) {
			if (parametros.RelSel && mr.cargandoModificar) {
				return;
			}
			
			if (mr.primeraCargaRelacion && parametros.RelSel) {
				mr.primeraCargaRelacion = false;
				return;
			}
		
			if (!nval || !mr.RelacionadoObj || !mr.RelacionadoObj.IdPersona) {
				if (mr.cambioTipoRelacion) {
					mr.cambioTipoRelacion = false;
					return;
				}

				if (parametros.RelSel) {
					return;
				}
		
				limpiarDatosRelacion();
				return;
			}

			if (parametros.RelSel && !mr.Relacion.modoCrear) {
				return;
			}
		
			const idPersona = Number(mr.RelacionadoObj.IdPersona);
		
			if (!idPersona) {
				return;
			}
		
			if (!parametros.RelSel) {
				limpiarDatosRelacion();
			}

			DireccionesFisicas.filter({
				field: 'IdPersona',
				operator: 'eq',
				value: idPersona
			});
		
			DireccionesFisicas.query().then(function() {
				const data = DireccionesFisicas.data();
		
				if (!data || !data.length) return;
		
				const direccion = data[0];
		
				mr.Relacion.Ciudad = direccion.NombreCiudad || '';
				mr.Relacion.Calle = direccion.Calle || '';
				mr.Relacion.Numero = direccion.Numero || '';
				mr.Relacion.IdCiudad = direccion.IdCiudad || null;
		
				mr.configData.filtroCiudad = {
					IdPais: {
						field: 'IdPais',
						operator: 'eq',
						value: direccion.IdPais
					}
				};
			});
		
			DireccionesElectronicas.filter({
				field: 'IdPersona',
				operator: 'eq',
				value: idPersona
			});
		
			DireccionesElectronicas.query().then(function() {
				const dataEmail = DireccionesElectronicas.data();
		
				if (!dataEmail || !dataEmail.length) return;
		
				mr.Relacion.Email = dataEmail[0].Direccion || '';
			});
		
			Telefonos.filter({
				field: 'IdPersona',
				operator: 'eq',
				value: idPersona
			});
		
			Telefonos.query().then(function() {
				const dataTelefono = Telefonos.data();
		
				if (!dataTelefono || !dataTelefono.length) return;
		
				mr.Relacion.Telefono = dataTelefono[0].Numero || '';
			});
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------