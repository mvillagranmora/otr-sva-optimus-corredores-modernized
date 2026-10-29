/*************************************************************
 * Purpose	: Panel Persona Datos Generales.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]

 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelEnrolamientoPersonaDatosGeneralesEnrolCtrl',function($scope,$timeout,
			servicioSociodeNegocio,
			bcsComunes,
			bcsConfiguracionSociodeNegocio,
			bcsServicioPlataforma){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/	
		let ppdg = this;
		ppdg.ventanaId = bcsComunes.ventanaWinId($scope);
		ppdg.dataPersona = {};
		let parametros = bcsComunes.ventanaParams(ppdg.ventanaId);
		ppdg.configForm = {
			//TipoPersona : $scope.admSoc.dataSocioNegocio.CodigoTipoPersona //carga automatica de campos correspondientes al tipo persona
			TipoPersona : parametros.IdTipoPersona //carga automatica de campos correspondientes al tipo persona
		};
		ppdg.dataSocioDeNegocio = {};
		ppdg.dataSocioDeNegocioEmpleador = {};
		ppdg.dataSolicitudCambio= {};
		ppdg.kOptFechaNacimiento = {};
		ppdg.kOptFechaInicioActividad = {};
		ppdg.kOptFechaNacimiento.max = new Date(); //$scope.$root.SesionFechaSistema;
		ppdg.kOptFechaInicioActividad.max = new Date();
		ppdg.dataSolicitudCambio.CambiosPendientes=false;
		ppdg.dataSolicitudCambio.CambioEstadoPendiente=false;
		ppdg.datosCargados = false;
		ppdg.SesionLocale = $scope.SesionLocale
		bcsComunes.ventanaOverlay(ppdg.ventanaId,true);
		ppdg.transparenciaFiscal = [ 'CONNACIONAL' , 'US-PERSON-FACTA' , 'OCDE-PERSON-CRS' , 'OTRO' ];
		
		ppdg.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['DATOS_GENERALES_PERSONA'];
       	ppdg.camposRequeridos = {};
		bcsServicioPlataforma.CamposRequeridosFormulario(ppdg.CodigoFormulario, function(respuesta) {
	    	ppdg.camposRequeridos = respuesta;
			
	    	
		});
		ppdg.botonGuardarActivado = false;
		
		let cargarDatosMaestroPPDG = function() {
			$scope.admSoc.socioNegocioPromise.then(function(){
				ppdg.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
				let personaDatosGeneralesDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700228",
								dataType: "json"
						    }
						},
						filter: { field: "IdEnrolamiento", operator: "eq", value: Number(parametros.IdEnrolamiento) },
						schema: bcsConfiguracionSociodeNegocio.getSchemaPersona()
					}
				});
				
				personaDatosGeneralesDatasource.query().then(function(){
					if (personaDatosGeneralesDatasource.data().length != 1){
						//bcsComunes.msg("error","Error", "Ocurrio un error al consultar Cliente, intentelo nuevamente.");
					}else{
	
						ppdg.dataPersona = $.extend({},personaDatosGeneralesDatasource.data()[0]);
						ppdg.asignarValoresPorDefecto();
						ppdg.dataPersona.FechaNacimiento = util.date(ppdg.dataPersona.FechaNacimiento).toDate();  //rev fecha
						ppdg.dataPersona.ResidenciaTributariaExtranjera = ppdg.dataPersona.IdPaisResidencia === $scope.SesionIdPaisSistema ? false : true; //BLOQUEA PAIS RESIDENCIA
						ppdg.IdPersonaRelacionado = ppdg.dataPersona.CodigoTipoPersona === "N" ? ppdg.dataPersona.IdPersonaConyuge : ppdg.dataPersona.IdPersonaRepresentante;
						ppdg.dataPersonaOriginal = $.extend({},ppdg.dataPersona);
						// Se consulta el Socio de Negocio del Relacionado (Conyuge / Representante Legal

						if(ppdg.dataPersona.IdPersonaEmpleador){
							var filter = { field: "IdPersona", operator: "eq", value: Number(ppdg.dataPersona.IdPersonaEmpleador) };
							var socioDeNegocioRelacionadoEmpleadorDatasource=bcsConfiguracionSociodeNegocio.getDatasourceSocioNegocio(filter);
							socioDeNegocioRelacionadoEmpleadorDatasource.query().then(function(result){
								if(socioDeNegocioRelacionadoEmpleadorDatasource.data().length>0){
									ppdg.dataPersona.IdSocNegPersonaEmpleador=socioDeNegocioRelacionadoEmpleadorDatasource.data()[0].IdSocioDeNegocio;
									ppdg.dataPersonaOriginal.IdSocNegPersonaEmpleador=socioDeNegocioRelacionadoEmpleadorDatasource.data()[0].IdSocioDeNegocio;

								} 
							});
						}


						if(ppdg.dataPersona.IdPersonaConyuge>0){
							var filter = { field: "IdPersona", operator: "eq", value: Number(ppdg.dataPersona.IdPersonaConyuge) };
							var socioDeNegocioRelacionadoConyugeDatasource=bcsConfiguracionSociodeNegocio.getDatasourceSocioNegocio(filter);
							socioDeNegocioRelacionadoConyugeDatasource.query().then(function(result){
								if(socioDeNegocioRelacionadoConyugeDatasource.data().length>0){
									$timeout(function() {
										ppdg.dataPersona.IdSocNegPersonaConyuge=socioDeNegocioRelacionadoConyugeDatasource.data()[0].IdSocioDeNegocio;
										ppdg.dataPersonaOriginal.IdSocNegPersonaConyuge=socioDeNegocioRelacionadoConyugeDatasource.data()[0].IdSocioDeNegocio;
									},1000);
								} 
							});
						}

						let socioDeNegocioRelacionadoDatasource = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering:true,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1051",
										dataType: "json"
								    }
								},
								filter: { field: "IdPersona", operator: "eq", value: Number(ppdg.IdPersonaRelacionado) },
								schema: bcsConfiguracionSociodeNegocio.getSchemaSocioDeNegocioRelacionado()
							}
						});
		
						let nombreFantasiaDatasource = new bcs.DataSource({
									type: "odata",
									definition : {
										serverFiltering:true,
										transport: {
											read: {
												url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700722",
												dataType: "json"
										    }
										},
										filter: { field: "IdEnrolamiento", operator: "eq", value: Number(parametros.IdEnrolamiento) },
										schema: bcsConfiguracionSociodeNegocio.getSchemaPersona()
									}
						});
						nombreFantasiaDatasource.query().then(function (result) {
							if (nombreFantasiaDatasource.data().length > 0) {
								ppdg.dataPersona.NombreFantasia =  nombreFantasiaDatasource.data()[0].NombreFantasia;
							}

						});
						socioDeNegocioRelacionadoDatasource.query().then(function(){
							if(socioDeNegocioRelacionadoDatasource.data().length > 0){
								ppdg.asignarValoresPorDefecto();
								ppdg.dataSocioDeNegocio = $.extend({},socioDeNegocioRelacionadoDatasource.data()[0]);							
								if(ppdg.dataPersona.CodigoTipoPersona === "N"){
									$timeout(function() {
										ppdg.IdSocNegPersonaConyuge	= ppdg.dataSocioDeNegocio.IdSocioDeNegocio;
										ppdg.dataPersona.IdPersonaConyuge = ppdg.dataSocioDeNegocio.IdSocioDeNegocio;
									},1000);	
									
								}else{
									$timeout(function() {
										ppdg.IdSocNegPersonaRepresentante = ppdg.dataSocioDeNegocio.IdPersona;
										ppdg.dataPersona.IdPersonaRepresentante= ppdg.dataSocioDeNegocio.IdPersona;
									},1000);
								}
							}
							

							ppdg.dataPersonaOriginal = $.extend({},ppdg.dataPersona);
						});
						ppdg.datosCargados = true;
					}
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
					ppdg.pantalla.modoEdicion = true;
				},function(e){
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
					ppdg.pantalla.modoEdicion = true;
					//bcsComunes.msg("error","Error", "Ocurrio un error al consultar Cliente, intentelo nuevamente.");
				});
			},function(e){
				bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
				//bcsComunes.msg("error","Error", "Ocurrio un error al consultar Cliente, intentelo nuevamente.");
			});
		};
		 
		ppdg.pantalla = {};
		ppdg.pantalla.modoEdicion = false;
		ppdg.pantalla.deshabilitado = false;
		
		ppdg.oConfiguracionGiro = {
			filtros: {
				IdPais	: {field : "IdPais", operator : "eq", value : $scope.SesionIdPaisSistema},
			}
		};
		ppdg.oConfiguracionGiroCIIU = {
			filtros: {
				IdPais	: {field : "IdPais", operator : "eq", value : $scope.SesionIdPaisSistema},
			}
		};

		
		ppdg.oConfiguracionRelacionadoRepLegal={
			filtros : {
				IdPersonaRepresentado	: {field : "IdPersonaRepresentado",	operator : "eq", value : Number($scope.admSoc.dataSocioNegocio.IdEnrolamiento)},
				//IdEstadoRelacion : {field : "IdEstadoRelacion",	operator : "eq", value : "ACT"},
			}
		};
		
		ppdg.oConfiguracionRelacionado={
			filtros : {
				IdEmpresa	: {field : "IdEmpresa",	operator : "eq", value : $scope.SesionIdEmpresa},
			}
		};
		
		ppdg.oConfiguracionRegimenConyugal={filtros : []};
		
		$scope.$watch('ppdg.dataPersona.EstadoCivil',function (nval,oval) {
			if(ppdg.dataPersona.IdPaisResidencia){
				let filters = {};
				let IdPais = {field: "IdPais", operator: "eq", value: ppdg.dataPersona.IdPaisResidencia }; //ppdg.dataPersona.IdPaisResidencia};
				if(nval ==="CC"){
					filters = {
						complejo : [IdPais, []],
						logic: "and",
						filters: [
							{
							logic: "or",
							filters: [
								{ field: "IdTipoRegimenConyugal", operator: "eq", value: "PG" },
								{ field: "IdTipoRegimenConyugal", operator: "eq", value: "SB" }
							]},
							IdPais
						]
					};
										
				}else{
					filters = IdPais;
				}
				
				
				ppdg.oConfiguracionRegimenConyugal.filtros = filters;
			}
		});
		
		ppdg.oConfiguracionRelacionado.filtros.read = true;
		ppdg.oConfiguracionGiro.filtros.read = true;
		
		/******************************************************
		 * Funciones
		 * ***************************************************/		
		
		ppdg.editarPPDG = function editarPPDG() {
			ppdg.pantalla.modoEdicion = true;
		};
		
		ppdg.crearPersona = function crearPersona() {
			bcsComunes.ventanaOverlay(ppdg.ventanaId,true);
			let id1 = bcsComunes.ventanaAbrir("CrearPersona",{modoRapido:true, juridicos:true},false,false); 
	        let promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data.creado === true){
	        		ppdg.dataPersona.IdSocNegPersonaEmpleador = data.IdSocioNegocio;
					setTimeout(function(){
						ppdg.dataPersona.IdSocNegPersonaEmpleador = data.IdSocioNegocio;
						setTimeout(function(){
							ppdg.validaBotonGuardar();
						},500);
					},1000);
				}
				bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
	    	}); 
		};
		
		ppdg.crearConyuge = function crearConyuge() {
			bcsComunes.ventanaOverlay(ppdg.ventanaId,true);
			let id1 = bcsComunes.ventanaAbrir("CrearPersona",{modoRapido:true, juridicos:false, esVentanaConyugue: true},false,false); 
	        let promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data.creado === true){
	        		ppdg.dataPersona.IdSocNegPersonaConyuge = data.IdSocioNegocio;
					setTimeout(function(){
						ppdg.dataPersona.IdSocNegPersonaConyuge = data.IdSocioNegocio;
						setTimeout(function(){
							ppdg.validaBotonGuardar();
						},500);
					},1000);
				}
				bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
	    	}); 
		};
		
		ppdg.guardarPPDG = function guardarPPDG() {
		
			if (ppdg.dataPersona.IdPaisResidencia === $scope.SesionIdPaisSistema && ppdg.dataPersona.ResidenciaTributariaExtranjera === true){ 
				bcsComunes.msg("warning","info","Checkbox de residencia extranjera marcado. Verificar Pais de Residencia.");
				ppdg.dataPersona.IdPaisResidencia = ppdg.dataPersonaOriginal.IdPaisResidencia;
				return;
			}
			
			if(ppdg.dataPersona.CodigoTipoPersona === "N"){
				ppdg.dataPersona.IdPersonaConyuge = ppdg.RelacionadoObj.IdPersona;
				if(ppdg.dataPersonaOriginal.IdPersonaConyuge === Number(ppdg.RelacionadoObj.IdSocioDeNegocio)){
					ppdg.dataPersonaOriginal.IdPersonaConyuge = ppdg.RelacionadoObj.IdPersona;
				}
				ppdg.dataPersona.IdPersonaEmpleador = ppdg.RelacionadoEmpleadorObj.IdPersona;
				if(ppdg.dataPersonaOriginal.IdPersonaEmpleador === Number(ppdg.RelacionadoEmpleadorObj.IdSocioDeNegocio)){
					ppdg.dataPersonaOriginal.IdPersonaEmpleador = ppdg.RelacionadoEmpleadorObj.IdPersona;
				}
			}
			else{
				ppdg.dataPersona.IdPersonaRepresentante = ppdg.RelacionadoObj.IdPersonaRepresentante;
				if(ppdg.dataPersonaOriginal.IdPersonaRepresentante === Number(ppdg.RelacionadoObj.IdSocioDeNegocio)){
					ppdg.dataPersonaOriginal.IdPersonaRepresentante = ppdg.RelacionadoObj.IdPersonaRepresentante;
				}
			}	
			
			let objPersona = [];
			objPersona.push({
				IdInterno : 0,
				IdEnrolamiento : parametros.IdEnrolamiento,
				Nombres : ppdg.dataPersona.Nombres || '',
				ApellidoPaterno : ppdg.dataPersona.ApellidoPaterno || '',
				ApellidoMaterno : ppdg.dataPersona.ApellidoMaterno || '',
				IdPaisNacimiento : ppdg.dataPersona.IdPaisNacimiento || 0,
				IdPaisResidencia : ppdg.dataPersona.IdPaisResidencia || 0,
				FechaNacimiento : util.dateSPdate(ppdg.dataPersona.FechaNacimiento) || '1900-01-01',
				Sexo : ppdg.dataPersona.Sexo || '',
				EstadoCivil : ppdg.dataPersona.EstadoCivil || '',
				IdPersonaConyuge : ppdg.dataPersona.IdPersonaConyuge || 0,
				RegimenConyugal : ppdg.dataPersona.RegimenConyugal || '',
				Cargo : ppdg.dataPersona.Cargo || '',
				Empleador : ppdg.dataPersona.Empleador || '',
				CodigoTipoCargoEmpleado : ppdg.dataPersona.CodigoTipoCargoEmpleado || '',
				DetalleCargo : ppdg.dataPersona.DetalleCargo || '',
				IdRangoPatrimonio : ppdg.dataPersona.IdRangoPatrimonio || 0,
				IngresoPromedioMensual : ppdg.dataPersona.IngresoPromedioMensual || 0,
				IdGiroPrincipal : ppdg.dataPersona.IdGiroPrincipal || 0,
				IdGradoInstruccion : ppdg.dataPersona.IdGradoInstruccion || 0,
				IdGiroCIIU : ppdg.dataPersona.IdGiroCIIU || 0,
				IdPaisResidenciaSecundaria : ppdg.dataPersona.IdPaisResidenciaSecundaria || 0,
				IdPaisNacionalidadSecundaria : ppdg.dataPersona.IdPaisNacionalidadSecundaria || 0,
				RazonSocial : ppdg.dataPersona.RazonSocial || '',
				FechaInicioActividad : util.dateSPdate(ppdg.dataPersona.FechaInicioActividad) || '1900-01-01',
				IdPersonaRepresentante : ppdg.dataPersona.IdPersonaRepresentante || 0,
				IdPersonaEmpleador : ppdg.dataPersona.IdPersonaEmpleador || 0,
				GlobalLegalEntity : ppdg.dataPersona.GlobalLegalEntity || '',
				NombreFantasia : ppdg.dataPersona.NombreFantasia || '',
				IdNombreFantasiaPrincipal : ppdg.dataPersona.IdNombreFantasiaPrincipal || 0,
				AgnosServicio : ppdg.dataPersona.AgnosServicio || 0,
				Alias : '',
				EsExtranjero: ppdg.dataPersona.EsExtranjero || '',
				IdTransparenciaFiscal: ppdg.dataPersona.IdTransparenciaFiscal || 0,
				IdProfesion: ppdg.dataPersona.IdProfesion || 0,
				IdPaisNacionalidadPrincipal : ppdg.dataPersona.IdPaisNacionalidadPrincipal || 0,
				ResidenciaTributariaExtranjera: ppdg.dataPersona.ResidenciaTributariaExtranjera === true ? 'S' : 'N',
				IdSituacionLaboral: ppdg.SituacionLaboral.CodigoSituacionLaboral || '',
				IdTipoCanalIngresoCorredor: 'OPT',
				CodigoRangoRenta: ppdg.dataPersona.CodigoRangoRenta || '',
				CodigoRangoNivelVenta: ppdg.dataPersona.CodigoRangoNivelVenta || ''
			});
			
			let datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_CodigoFormulario : ppdg.CodigoFormulario,
				p_IdTipoCanal : 'OPT',
				p_PersonaDatosGenerales : objPersona
			};
			
			if(ppdg.dataPersona.CodigoTipoPersona === "J"){
				let CODIGOFORMULARIO = "NOMBRES_FANTASIA_PERSONA_FULL_JURIDICA";
				
				let  p_NombreFantasia = [{
						IdInterno : 1,
						IdEnrolamiento : parametros.IdEnrolamiento,
						NombreFantasia : datos.p_PersonaDatosGenerales[0].NombreFantasia,
				   	 }]
						
				let objNombreFantasia = {
					 p_IdEmpresa : $scope.SesionIdEmpresa,
					 p_IdUsuario : $scope.SesionIdUsuario,
					 p_IdModulo : 'SOC',
					 p_UsaReglasNegocio : 'S',
					 p_CodigoFormulario : CODIGOFORMULARIO,
					 p_IdTipoCanal : 'OPT',
					 p_NombresFantasia : p_NombreFantasia,
			        
						            
		       }
				
				bcsComunes.ventanaOverlay(ppdg.ventanaId,true);
				servicioSociodeNegocio.crearBorradorNombreFantasia(objNombreFantasia).$promise.then(
					function(result) {
						bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
						bcsComunes.ventanaMsg({ mensaje : "Borrador de nombre fantasía creado existosamente",
							conflictos : util.parseServerResponse(result)}, ppdg.ventanaId);
				
					},
					function(){
						bcsComunes.msg("error","Error","La operacion tiene errores");
						bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
				});
			}
			
				servicioSociodeNegocio.modificarBorradorPersonaDatosGenerales(datos).$promise.then(
				function(result) {
					
					cargarDatosMaestroPPDG();
					ppdg.pantalla.modoEdicion = false;
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);

					bcsComunes.ventanaMsg({ mensaje : "Los datos de la persona fueron modificados, operacion: "+parametros.IdEnrolamiento,
						conflictos : util.parseServerResponse(result)}, ppdg.ventanaId);
			
				},
				function(){
					cargarDatosMaestroPPDG();
					ppdg.pantalla.modoEdicion = false;
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
				});
		};
		  
		//FECHA SISTEMA
		let successFecha = function(result){
			if (result.P_OUT_ERRORESCONTADOR == "0"){
				ppdg.kngFechaSistema = util.dateSPdate(util.date(result.P_OUT_FECHAHORASISTEMA).toDate());
			}
			else{
				ppdg.kngFechaSistema = '0001-01-01';
			}
		};

		let seteoFechaSistema = function(){
			let datos = {};
			datos.p_IdEmpresa = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario = $scope.SesionIdUsuario; 
			datos.p_IdModulo = 'SOC';
			datos.p_UsaReglasNegocio = 'S';		
			bcsServicioPlataforma.FechaSistema(datos,successFecha);
		};

		ppdg.validaBotonGuardar = function validaBotonGuardar() {
			if (ppdg.dataPersona.IdPaisResidencia === $scope.SesionIdPaisSistema && ppdg.dataPersona.ResidenciaTributariaExtranjera === true){
				bcsComunes.msg("warning","info","Checkbox de residencia extranjera marcado. Verificar Pais de Residencia.");
				ppdg.dataPersona.IdPaisResidencia = ppdg.dataPersonaOriginal.IdPaisResidencia;
			}
			if(ppdg.configForm.TipoPersona === 'N') {
				if(ppdg.dataPersona.EstadoCivil === 'CA' || ppdg.dataPersona.EstadoCivil === 'CC') {
					if(ppdg.dataPersona.IdSocNegPersonaConyuge === false || ppdg.dataPersona.RegimenConyugal === false) {
						ppdg.botonGuardarActivado = false;
					}
				}
			}
			ppdg.botonGuardarActivado = ppdg.formCrearCliente.$valid;
		};
		
		seteoFechaSistema();

		
		
		$scope.$watch("ppdg.cmbbxPaisResidenciaObj", function(nval, oval){
			if(nval){
				if(nval.EsPaisUs ==='S'){
					ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[1];
					return;
				}
				if(nval.EsOcde ==='S' && nval.CodigoPais !== 'CL'){
					ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[2];
					return;
				}
				if(nval.CodigoPais === 'CL'){
					ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[0];
					return;
				}
				ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[3];
			}
		});
		
		$scope.$watch("ppdg.dataPersona.ResidenciaTributariaExtranjera", function(nval, oval){
			if(nval === false){
				ppdg.dataPersona.IdPaisResidencia = $scope.SesionIdPaisSistema;
			}
		});
		
		$scope.$watch("ppdg.dataPersona.IdPaisNacimiento", function(nval, oval){
			ppdg.dataPersona.EsExtranjero = nval === $scope.SesionIdPaisSistema ? 'N' : 'S';
		});
		
		$scope.$watch("ppdg.dataPersona.CodigoTipoCargoEmpleado", function(nval, oval){
			ppdg.dataPersona.DetalleCargo = nval !== "EMPLEA" ? "" : ppdg.dataPersona.DetalleCargo;
		});
		
		$scope.$watch("ppdg.dataPersona.EstadoCivil", function(nval, oval){
			if(nval === "SO"){
				ppdg.dataPersona.IdPersonaConyuge = "";				
			}
		});
		
		ppdg.asignarValoresPorDefecto = function (){
			
			$.each(ppdg.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !ppdg.dataPersona[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					ppdg.dataPersona[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}

		ppdg.obtenerDatosComparables = function (data) {
			return {
				Nombres: data.Nombres || '',
				ApellidoPaterno: data.ApellidoPaterno || '',
				ApellidoMaterno: data.ApellidoMaterno || '',
				IdPaisNacimiento: data.IdPaisNacimiento || 0,
				IdPaisResidencia: data.IdPaisResidencia || 0,
				FechaNacimiento: ppdg.dataPersona.FechaNacimiento || '1900-01-01',
				Sexo: data.Sexo || '',
				EstadoCivil: data.EstadoCivil || '',
				IdPersonaConyuge: data.IdPersonaConyuge || 0,
				RegimenConyugal: data.RegimenConyugal || '',
				Cargo: data.Cargo || '',
				Empleador: data.Empleador || '',
				CodigoTipoCargoEmpleado: data.CodigoTipoCargoEmpleado || '',
				DetalleCargo: data.DetalleCargo || '',
				IdRangoPatrimonio: data.IdRangoPatrimonio || 0,
				IngresoPromedioMensual: data.IngresoPromedioMensual || 0,
				IdGiroPrincipal: data.IdGiroPrincipal || 0,
				IdGradoInstruccion: data.IdGradoInstruccion || 0,
				IdGiroCIIU: data.IdGiroCIIU || 0,
				IdPaisResidenciaSecundaria: data.IdPaisResidenciaSecundaria || 0,
				IdPaisNacionalidadSecundaria: data.IdPaisNacionalidadSecundaria || 0,
				RazonSocial: data.RazonSocial || '',
				FechaInicioActividad: ppdg.dataPersona.FechaInicioActividad || '1900-01-01',
				IdPersonaRepresentante: data.IdPersonaRepresentante || 0,
				IdPersonaEmpleador: data.IdPersonaEmpleador || 0,
				GlobalLegalEntity: data.GlobalLegalEntity || '',
				NombreFantasia: data.NombreFantasia || '',
				AgnosServicio: data.AgnosServicio || 0,
				IdProfesion: data.IdProfesion || 0,
				IdPaisNacionalidadPrincipal: data.IdPaisNacionalidadPrincipal || 0,
				ResidenciaTributariaExtranjera: data.ResidenciaTributariaExtranjera === true ? 'S': 'N'
			}
		}

		ppdg.datosPorGuardar = function() {
			const dataOriginal = ppdg.obtenerDatosComparables(ppdg.dataPersonaOriginal);
			const data = ppdg.obtenerDatosComparables(ppdg.dataPersona);

			const sonIguales = _.isEqual(data, dataOriginal);
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaDatosGenerales' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaDatosGenerales' });
			}		
		}

		/******************************************************
		* WATCH ACTUALIZACION PESTAÑA
		******************************************************/
		$scope.$watch("admSoc.actualizarTab.PERSONA.DatosGenerales",function(nval,oval){
		//evalua si debe actualizar los datos de la pestana por cambios en otra pestana
			if(nval){
				//ppdg.oConfiguracionNombreFantasia.filtros.read = false;
				ppdg.oConfiguracionGiroCIIU.filtros.read = false;
				ppdg.oConfiguracionGiro.filtros.read = false;
				ppdg.oConfiguracionRelacionado.filtros.read = false;
				$scope.admSoc.actualizarTab.PERSONA["DatosGenerales"] = false;
			}else{
				
				//ppdg.oConfiguracionNombreFantasia.filtros.read = true;
				ppdg.oConfiguracionRelacionado.filtros.read = true;
				ppdg.oConfiguracionGiroCIIU.filtros.read = true;
				ppdg.oConfiguracionGiro.filtros.read = true;
			}
		});

		
		
		/******************************************************
		 * GO
		 * ***************************************************/

		cargarDatosMaestroPPDG();

		$scope.$watchCollection('ppdg.dataPersona', function() {
			ppdg.validaBotonGuardar();
			if(ppdg.datosCargados){
				ppdg.datosPorGuardar();
			}
		});
		
	});
		
}());
//------------------------- FIN CONTROLLER --------------------------