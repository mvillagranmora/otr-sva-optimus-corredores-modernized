(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaDatosGeneralesCtrl',
			function($scope, servicioSociodeNegocio, bcsComunes, $resource, bcsConfiguracionSociodeNegocio, bcsServicioPlataforma){

		let ppdg = this;
		ppdg.ventanaId = bcsComunes.ventanaWinId($scope);
		ppdg.dataPersona = {};
		ppdg.configForm = {
			TipoPersona : $scope.admSoc.dataSocioNegocio.CodigoTipoPersona //carga automatica de campos correspondientes al tipo persona
		};
		$scope.configData = {};
		ppdg.dataSocioDeNegocio = {};
		ppdg.dataSocioDeNegocioEmpleador = {};
		ppdg.dataSolicitudCambio= {};
		ppdg.kOptFechaNacimiento = {};
		ppdg.kOptFechaDefuncion = {};
		ppdg.kOptFechaInicioActividad = {};
		ppdg.kOptFechaNacimiento.max = new Date(); //$scope.$root.SesionFechaSistema;
		ppdg.kOptFechaDefuncion.max = new Date();
		ppdg.kOptFechaInicioActividad.max = new Date();
		ppdg.dataSolicitudCambio.CambiosPendientes=false;
		ppdg.dataSolicitudCambio.CambioEstadoPendiente=false;
		ppdg.datosCargados = false;
		ppdg.SesionLocale = $scope.SesionLocale
		bcsComunes.ventanaOverlay(ppdg.ventanaId,true);
		var idVentana = bcsComunes.ventanaWinId($scope)
		ppdg.camposRequeridos = {};
		$scope.admSoc.socioNegocioPromise.then(function () {
			ppdg.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz['DATOS_GENERALES_PERSONA'];

			bcsServicioPlataforma.CamposRequeridosFormulario(ppdg.CodigoFormulario, function(respuesta) {
				ppdg.camposRequeridos = respuesta;
			});
		});

		ppdg.transparenciaFiscal = [ 'CONNACIONAL' , 'US-PERSON-FACTA' , 'OCDE-PERSON-CRS' , 'OTRO' ];
		ppdg.botonGuardarActivado = false;
		
		$scope.$watch("ppdg.dataPersona.FechaNacimiento", function(nval, oval){
			ppdg.kOptFechaDefuncion.min = ppdg.dataPersona.FechaNacimiento;
			if(nval && ppdg.dataPersona.FechaDefuncion && (moment(ppdg.dataPersona.FechaDefuncion).isBefore(nval))){
				ppdg.dataPersona.FechaDefuncion	= util.dateInicioDia('0001-01-01');
			}
			bcsComunes.ventanaAutoHeight(idVentana);
		});
		$scope.configData.filtroEmpleador = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
		}
		$scope.configData.filtroConyuge = {
			CodigoTipoPersona : {field: "CodigoTipoPersona", operator: "eq", value: 'N'},
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa}
		}

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
		
		let cargarDatosMaestroPPDG = function() {
			$scope.admSoc.socioNegocioPromise.then(function(){
				ppdg.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
				let personaDatosGeneralesDatasource = new bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1309",
								dataType: "json"
						    }
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppdg.IdPersona) },
						schema: bcsConfiguracionSociodeNegocio.getSchemaPersona()
					}
				});
				
				
				personaDatosGeneralesDatasource.query().then(function(){
					if (personaDatosGeneralesDatasource.data().length != 1){
						bcsComunes.msg("error","Error", "Ocurrio un error al consultar Cliente, intentelo nuevamente.");
					}
					else{
						ppdg.dataPersona = $.extend({},personaDatosGeneralesDatasource.data()[0]);
						ppdg.dataPersona.FechaNacimiento = util.date(ppdg.dataPersona.FechaNacimiento).toDate() || util.datedefault();  //rev fecha
						ppdg.dataPersona.FechaDefuncion = util.date(ppdg.dataPersona.FechaDefuncion).toDate() || util.datedefault();  //rev fecha
						ppdg.dataPersona.FechaBloqueo = util.date(ppdg.dataPersona.FechaBloqueo).toDate() || util.datedefault();
						ppdg.dataPersona.ResidenciaTributariaExtranjera = ppdg.dataPersona.ResidenciaTributariaExtranjera === 'N'|| ppdg.dataPersona.ResidenciaTributariaExtranjera === null ? false : true; //BLOQUEA PAIS RESIDENCIA
						ppdg.IdPersonaRelacionado = ppdg.dataPersona.Tipo === "N" ? ppdg.dataPersona.IdPersonaConyuge : ppdg.dataPersona.IdPersonaRepresentante;
						ppdg.asignarValoresPorDefecto();
						ppdg.dataPersonaOriginal = $.extend({},ppdg.dataPersona);
						
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
									ppdg.dataPersona.IdSocNegPersonaConyuge=socioDeNegocioRelacionadoConyugeDatasource.data()[0].IdSocioDeNegocio;
									ppdg.dataPersonaOriginal.IdSocNegPersonaConyuge=socioDeNegocioRelacionadoConyugeDatasource.data()[0].IdSocioDeNegocio;
								} 
							});
						}
						
						let paisResidenciaDatasource = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering:true,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1615",
										dataType: "json"
								    }
								},
								schema: {
									model: {
										fields: {
												IdPais :  { type: "number"},
												NombrePais : { type: "string"},
												CodigoPais : { type: "string"},
												EsOcde : { type: "string"},
												EsUsPerson : { type: "string"}
											}
									}
								}
							}
						});	
						let nombreFantasiaDatasource = new bcs.DataSource({
							type: "odata",
							definition : {
								serverFiltering:true,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1657",
										dataType: "json"
								    }
								},
								filter: { field: "IdPersona", operator: "eq", value: Number(ppdg.IdPersona) },
								schema: {
			            		model: {
			            			     fields : {
			            				 IdPersona : { type:"number" } ,
			            				 IdNombreFantasia : { type:"number" } ,
			            			}
			            		}
			            	},
							}
						});
						
						nombreFantasiaDatasource.query().then(function (result) {
							if(nombreFantasiaDatasource.data().length > 0){
								if(ppdg.dataPersonaOriginal.IdNombreFantasiaPrincipal == 0){
									ppdg.dataPersona.IdNombreFantasiaPrincipal =  '';
								} else{
									ppdg.dataPersona.IdNombreFantasiaPrincipal =  ppdg.dataPersonaOriginal.IdNombreFantasiaPrincipal;
								}
							}
						});
						
						
							
						
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
						socioDeNegocioRelacionadoDatasource.query().then(function (result) {
							if (socioDeNegocioRelacionadoDatasource.data().length > 0) {
								ppdg.asignarValoresPorDefecto();
								ppdg.dataSocioDeNegocio = $.extend({}, socioDeNegocioRelacionadoDatasource.data()[0]);
								if (ppdg.dataPersona.Tipo === "N") {
									ppdg.IdSocNegPersonaConyuge = ppdg.dataSocioDeNegocio.IdPersona;
									ppdg.dataPersona.IdPersonaConyuge = ppdg.dataSocioDeNegocio.IdPersona;
								}
								else {
									ppdg.IdSocNegPersonaRepresentante = ppdg.dataSocioDeNegocio.IdPersona;
									ppdg.dataPersona.IdPersonaRepresentante = ppdg.dataSocioDeNegocio.IdPersona;
								}
							}
							
							// Se consultan solicitudes de cambios Pendientes
							let filterSol = new Array();
							filterSol.push({ field: "IdTipoEntidad", operator: "eq", value: 'PERSONA' },
								{ field: "IdEntidadOrigen", operator: "eq", value: ppdg.IdPersona },
								{ field: "IdEstadoAutorizacion", operator: "eq", value: 'PEN' });
							let SolicitudDeCambioDatasource = new bcs.DataSource({
								type: "odata",
								definition: {
									serverFiltering: true,
									transport: {
										read: {
											url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1360",
											dataType: "json"
										}
									},
									filter: filterSol,
									schema: bcsConfiguracionSociodeNegocio.getSchemaSolicitudDeCambio()
								}
							});
							
							SolicitudDeCambioDatasource.query().then(function (result) {
								if (SolicitudDeCambioDatasource.data().length > 0) {
									ppdg.dataSolicitudCambio = $.extend({}, SolicitudDeCambioDatasource.data());
									ppdg.dataSolicitudCambio.CambiosPendientes = true;
								}
								
							});
						});

						const fechaBloqueo = util.date(ppdg.dataPersona.FechaBloqueo);
							if (fechaBloqueo && fechaBloqueo.toDate().getFullYear() < 2) {
								ppdg.dataPersona.FechaBloqueo = ppdg.dataPersona.FechaModificacion;
							}
							ppdg.datosCargados = true;
						}
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
				},function(e){
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
					bcsComunes.msg("error","Error", "Ocurrio un error al consultar Cliente, intentelo nuevamente.");
				});
			},function(e){
				bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
				bcsComunes.msg("error","Error", "Ocurrio un error al consultar Cliente, intentelo nuevamente.");
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
				IdPersonaRepresentado	: {field : "IdPersonaRepresentado",	operator : "eq", value : $scope.admSoc.dataSocioNegocio.IdPersona},
				IdEstadoRelacion : {field : "IdEstadoRelacion",	operator : "eq", value : "ACT"},
			}
		};
		
		ppdg.oConfiguracionRelacionado={
			filtros : {
				IdPersona	: {field : "IdPersona",	operator : "eq", value : ppdg.dataPersona.IdPersonaConyuge },
			}
		};
		
		ppdg.oConfiguracionRegimenConyugal={filtros : []};
		$scope.$watch('ppdg.dataPersona.EstadoCivil',function (nval,oval) {
			if(ppdg.dataPersona.IdPaisResidencia){
				let filters = {};
				let IdPais = {field: "IdPais", operator: "eq", value: ppdg.dataPersona.IdPaisResidencia};
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
			bcsComunes.ventanaAutoHeight(idVentana);
		});
		
		ppdg.oConfiguracionNombreFantasia={
			filtros : {
				IdPersona	: {field : "IdPersona",	operator : "eq", value : Number($scope.admSoc.dataSocioNegocio.IdPersona) },
				IdEstado    : {field : "IdEstado",	operator : "eq", value : 'ACT' }
			}
		};
		
		ppdg.oConfiguracionRelacionadoRepLegal.filtros.read = true;
		ppdg.oConfiguracionNombreFantasia.filtros.read = true;
		ppdg.oConfiguracionGiroCIIU.filtros.read = true;
		ppdg.oConfiguracionGiro.filtros.read = true;
		
		ppdg.cmbbxPaisResidenciaExtranjera = {
			filtros : {
				CodigoPais   : { "field"   : "CodigoPais","operator": "neq", "value"   : 'CL' }
			}
			
		};
		
		
		$scope.$watch("ppdg.cmbbxPaisResidenciaObj", function(nval, oval){
			if(nval){
				if(nval.EsPaisUs ==='S'){
					ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[1];
					return;
				}
				if(nval.EsOcde ==='S' && nval.IdPais !== $scope.SesionIdPaisSistema){
					ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[2];
					return;
				}
				if(nval.IdPais === $scope.SesionIdPaisSistema){
					ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[0];
					return;
				}
				ppdg.dataPersona.IdTransparenciaFiscal = ppdg.transparenciaFiscal[3];
			}
			bcsComunes.ventanaAutoHeight(idVentana);
		});
		/*
		$scope.$watch("ppdg.dataPersona.ResidenciaTributariaExtranjera", function(nval, oval){
			if(nval === false ){
				ppdg.dataPersona.IdPaisResidencia = $scope.SesionIdPaisSistema;
			}
		});
		*/
		$scope.$watch("ppdg.dataPersona.IdPaisNacimiento", function(nval, oval){
			ppdg.dataPersona.EsExtranjero = nval === $scope.SesionIdPaisSistema ? 'N' : 'S';
		});
		
		$scope.$watch("ppdg.dataPersona.CodigoTipoCargoEmpleado", function(nval, oval){
			ppdg.dataPersona.DetalleCargo = nval !== "EMPLEA" ? "": ppdg.dataPersona.DetalleCargo;
		});
		
		$scope.$watch("ppdg.dataPersona.EstadoCivil", function(nval, oval){
			if(nval === "SO" || nval === "VI" || nval === "SE" || nval === "DI"){
				ppdg.dataPersona.IdPersonaConyuge = 0;
				ppdg.dataPersona.RegimenConyugal = "";
			}
		});
		
		ppdg.editarPPDG = function() {
			ppdg.pantalla.modoEdicion = true;
		};
		
		ppdg.crearPersona = function() {
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
		
		ppdg.crearConyuge = function() {
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
		
		ppdg.guardarPPDG = function() {
			// if (ppdg.pantalla.modoEdicion && ppdg.dataPersona.ResidenciaTributariaExtranjera === true){ 
			// 	bcsComunes.msg("warning","info","Checkbox de residencia extranjera marcado. Verificar Pais de Residencia.");
			// 	//ppdg.dataPersona.IdPaisResidencia = ppdg.dataPersonaOriginal.IdPaisResidencia;
			// 	//cargarDatosMaestroPPDG();
			// 	//return;
			// }
			if(ppdg.dataPersona.Tipo === "N"){
				if(ppdg.dataPersona.EstadoCivil !== "CA" && ppdg.dataPersona.EstadoCivil !== "CC"){
					ppdg.dataPersona.IdPersonaConyuge = 0;
				}else{
					ppdg.dataPersona.IdPersonaConyuge = ppdg.RelacionadoObj.IdPersona;
				}
				if(ppdg.dataPersonaOriginal.IdPersonaConyuge === Number(ppdg.RelacionadoObj.IdSocioDeNegocio)){
					ppdg.dataPersonaOriginal.IdPersonaConyuge = ppdg.RelacionadoObj.IdPersona;
				}
				ppdg.dataPersona.IdPersonaEmpleador = ppdg.RelacionadoEmpleadorObj.IdPersona;
				if(ppdg.dataPersonaOriginal.IdPersonaEmpleador === Number(ppdg.RelacionadoEmpleadorObj.IdSocioDeNegocio)){
					ppdg.dataPersonaOriginal.IdPersonaEmpleador = ppdg.RelacionadoEmpleadorObj.IdPersona;
				}
			}
			else{
				ppdg.RelacionadoObj.IdPersonaRepresentante = ppdg.dataPersona.IdPersonaRepresentante;
				if(ppdg.dataPersonaOriginal.IdPersonaRepresentante === Number(ppdg.RelacionadoObj.IdSocioDeNegocio)){
					ppdg.dataPersonaOriginal.IdPersonaRepresentante = ppdg.RelacionadoObj.IdPersonaRepresentante;
				}
			}	
			let camposPNatural = [	{ nombre: "Nombres" ,tipo : "texto"},
									{ nombre: "ApellidoPaterno",tipo : "texto"},
									{ nombre: "ApellidoMaterno",tipo : "texto"},
			                      	{ nombre: "IdPaisNacimiento",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdPaisResidencia",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdTransparenciaFiscal",tipo : "combo"},
									{ nombre: "ResidenciaTributariaExtranjera",tipo : "bool"},
									{ nombre: "FechaNacimiento",tipo : "fecha"},
									{ nombre: "FechaDefuncion",tipo : "fecha"},
									{ nombre: "Sexo",tipo : "combo"},
									{ nombre: "EsExtranjero",tipo : "bool"},
									{ nombre: "EstadoCivil",tipo : "combo"},
									{ nombre: "IdPersonaConyuge",tipo : "combo", valorBlanco: '0'},
									{ nombre: "RegimenConyugal",tipo : "combo"},
									{ nombre: "Cargo",tipo : "texto"},
									{ nombre: "Empleador",tipo : "texto"},
									{ nombre: "IdProfesion",tipo : "combo", valorBlanco: '0'},
			                      	{ nombre: "IdPaisNacionalidadPrincipal",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdPersonaEmpleador",tipo : "combo", valorBlanco: '0'},
									{ nombre: "CodigoTipoCargoEmpleado",tipo : "combo"},
			                      	{ nombre: "DetalleCargo",tipo : "texto"},
									{ nombre: "IdRangoPatrimonio",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IngresoPromedioMensual",tipo : "texto"},
									{ nombre: "IdGiroPrincipal",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdSituacionLaboral",tipo : "combo"},
									{ nombre: "CodigoRangoRenta", tipo: "combo", valorBlanco: '0'},
									{ nombre: "CodigoPropositoTransaccion", tipo: "combo", valorBlanco: ""}
								];
									
			if ($scope.SesionLocale == 'PE'){
				camposPNatural.push({ nombre: "IdGradoInstruccion",tipo : "combo"});
				camposPNatural.push({ nombre: "AgnosServicio",tipo : "texto"});
				camposPNatural.push({ nombre: "IngresoPromedioMensual",tipo : "texto"});
				camposPNatural.push({ nombre: "IdGiroCIIU",tipo : "combo"});	
				camposPNatural.push({ nombre: "IdPaisResidenciaSecundaria",tipo : "combo"});
				camposPNatural.push({ nombre: "IdPaisNacionalidadSecundaria",tipo : "combo"});

			} 			
			let camposPJuridica = [ { nombre: "RazonSocial",tipo : "texto"},
									{ nombre: "IdGiroPrincipal",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdPaisNacimiento",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdPaisResidencia",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IdTransparenciaFiscal",tipo : "combo"},
									{ nombre: "ResidenciaTributariaExtranjera",tipo : "bool"},
									{ nombre: "FechaNacimiento",tipo : "fecha"},
									{ nombre: "EsExtranjero",tipo : "bool"},
									{ nombre: "FechaInicioActividad",tipo : "fechahora"},
									{ nombre: "IdPersonaRepresentante",tipo : "combo", valorBlanco: '0'},
									{ nombre: "GlobalLegalEntity",tipo : "texto"},
									{ nombre: "IdNombreFantasiaPrincipal",tipo : "combo"},
									{ nombre: "IdRangoPatrimonio",tipo : "combo", valorBlanco: '0'},
									{ nombre: "IngresoPromedioMensual",tipo : "texto"},
									{ nombre: "CodigoRangoNivelVenta" , tipo: "combo", valorBlanco : '0'}
								  ];
			if ($scope.SesionLocale == 'PE'){
				camposPJuridica.push({ nombre: "IdGradoInstruccion",tipo : "combo"});
				camposPJuridica.push({ nombre: "AgnosServicio",tipo : "texto"});
				camposPJuridica.push({ nombre: "IngresoPromedioMensual",tipo : "texto"});
			}

			const obtenerValorCampo = function (campo) {
				switch (campo.tipo) {
					case 'texto': {
						return ppdg.dataPersona[campo.nombre] || (campo.hasOwnProperty('valorBlanco') ? campo.valorBlanco : '');
					}
					case 'combo': {
						return ppdg.dataPersona[campo.nombre] || (campo.hasOwnProperty('valorBlanco') ? campo.valorBlanco : '');
					}
					case 'fecha': {
						return ppdg.dataPersona[campo.nombre] ? util.dateSPdate(ppdg.dataPersona[campo.nombre]) : '0001-01-01';
					}
					case 'fechahora': {
						return ppdg.dataPersona[campo.nombre] ? util.dateSPseconddate(ppdg.dataPersona[campo.nombre]) : '0001-01-01 00:00:00';
					}
					case 'bool': {
						return ppdg.dataPersona[campo.nombre] ? 'S' : 'N';
					}
					default: return '';
				}
			};
			 
			let campos = ppdg.dataPersona.Tipo === "J"? camposPJuridica: camposPNatural;
			let registroCambio = [];
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (ppdg.dataPersona[value.nombre] !== ppdg.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}
				
				if (value.tipo === 'combo') {
					if (!((ppdg.dataPersona[value.nombre] === false || ppdg.dataPersona[value.nombre] === undefined) && (ppdg.dataPersonaOriginal[value.nombre] === null || ppdg.dataPersonaOriginal[value.nombre] == 0)) && (ppdg.dataPersona[value.nombre] != ppdg.dataPersonaOriginal[value.nombre])) {
						if (obtenerValorCampo(value) === '' && value.nombre === 'IdNombreFantasiaPrincipal') {
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : 0});
						}else {
							registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
						}
						
					}
				}
		
				if (value.tipo === 'fecha') {
					if (ppdg.dataPersona[value.nombre] !== ppdg.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}
				if (value.tipo === 'fechahora') {
					if (ppdg.dataPersona[value.nombre] !== ppdg.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}
				if (value.tipo === 'bool') {
					if (ppdg.dataPersona[value.nombre] !== ppdg.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : obtenerValorCampo(value)});
				}			
			});
			if (registroCambio.length === 0){ 
				bcsComunes.msg("warning","info","No hay modificaciones para guardar.");
				bcsComunes.ventanaOverlay(ppdg.idVentana, false);
				//$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaDatosGenerales' });
				cargarDatosMaestroPPDG();
				return;
			}
						
			let datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo : 'SOC',
				p_UsaReglasNegocio : 'S',
				p_IdPersona : ppdg.dataPersona.IdPersona,
				p_RegistroCambio : registroCambio,
				p_IdTipoCanalIngresoCorredor : $scope.SesionTipoCanalIngreso,
			};
			
			bcsComunes.ventanaOverlay(ppdg.ventanaId,true);
			servicioSociodeNegocio.modificarPersonaDatosGenerales(datos).$promise.then(
				function(result) {

					cargarDatosMaestroPPDG();
					ppdg.pantalla.modoEdicion = false;
					$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaDatosGenerales' });
					
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);

					bcsComunes.ventanaMsg({ mensaje : "Los datos de la persona fueron modificados, operacion: "+ppdg.IdPersona,
						conflictos : util.parseServerResponse(result)}, ppdg.ventanaId);

					for(let i=0; i < registroCambio.length; i++){
						//para actualizar otros tabs
						if(registroCambio[i].CampoModificado==="IdPaisNacimiento" || registroCambio[i].CampoModificado==="IdPaisResidencia" || registroCambio[i].CampoModificado==="IdPaisNacionalidadPrincipal" || registroCambio[i].CampoModificado==="IdPaisNacionalidadSecundaria"){
							$scope.admSoc.actualizarTab.PERSONA["Fatca"] = true;
						}
					}
					ppdg.avisarCambiosOk()
				},
				function (result) {
					
					cargarDatosMaestroPPDG();
					ppdg.pantalla.modoEdicion = false;
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppdg.ventanaId,false);
				});
		};
		
		ppdg.cancelarPPDG = function() {
			ppdg.pantalla.modoEdicion = false;
			ppdg.dataPersona = $.extend({},ppdg.dataPersonaOriginal);
			ppdg.dataPersona.IdSocNegPersonaConyuge = ppdg.dataPersonaOriginal.IdSocNegPersonaConyuge;
			ppdg.dataPersona.IdSocNegPersonaEmpleador = ppdg.dataPersonaOriginal.IdSocNegPersonaEmpleador;
			ppdg.dataPersona.IdSocNegPersonaRepresentante = ppdg.dataPersonaOriginal.IdPersonaRepresentante;
			ppdg.avisarCambiosOk()
		};
	
		ppdg.mostrarCambios = function() {
			let ventanaId = bcsComunes.ventanaAbrir("VerCambiosPendientes",{dataSolicitudCambio : ppdg.dataSolicitudCambio},false,false,ppdg.ventanaId);
			let promesa = bcsComunes.ventanaPromesa(ventanaId);   
		};
		
		let bloqueoPersonaSP = function(v_Dato){
			let urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=108";
			let r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
			let post = r.urlConsulta(v_Dato);
			
			return post.$promise;
	    };
	    
		ppdg.desbloquearPersona = function() {
			let ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{
				IdEntidad : ppdg.IdPersona, Entidad: 'PERSONA',
				IdEstado  : (ppdg.dataPersona.IdEstado==='ACT'?'BLO':'ACT')}
				,false,false,ppdg.ventanaId);
			let promesa = bcsComunes.ventanaPromesa(ventanaId);   
			$.when(promesa).then(function(){
				cargarDatosMaestroPPDG();
			});		
		};
		
		ppdg.bloquearPersona = function() {
			let ventanaId = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio",{IdEntidad : ppdg.IdPersona, Entidad: 'PERSONA',
				IdEstado:(ppdg.dataPersona.IdEstado === 'ACT' ? 'BLO' : 'ACT')},false,false,ppdg.ventanaId);
			let promesa = bcsComunes.ventanaPromesa(ventanaId);   
			$.when(promesa).then(function(){
				cargarDatosMaestroPPDG();
	         }); 
		};
		
		let crearEventoAutorizacionSP = function(v_Dato){
			let urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=130";
			let r = $resource("", {}, { 'urlConsulta': { method: "POST", url: urlConsulta} });
			let post = r.urlConsulta(v_Dato);
			return post.$promise;
		};
		/*
		ppdg.validarNoEsResidenciaExtranjera = function(){
			return (ppdg.dataPersona.IdPaisResidencia === $scope.SesionIdPaisSistema && ppdg.dataPersona.ResidenciaTributariaExtranjera === true) ? false : true;
		};
		*/
		ppdg.solicitarEventoAutorizacion = function() {
			let parametrosSP = {};
			let eventoFila = {};
			let evento='ENRPER';
			let modulo='SOC';
	        parametrosSP.P_IDEMPRESA 				= $scope.$root.SesionIdEmpresa;
	        parametrosSP.P_IDUSUARIO 				= $scope.$root.SesionIdUsuario;
	        parametrosSP.P_IDMODULO 				= modulo;
	        parametrosSP.P_SIMULAROPERACION		= "N";
	        parametrosSP.P_USAREGLASNEGOCIO 		= "N";
	        parametrosSP.P_TABLAEVENTOS 			= [];
	        parametrosSP.P_DETALLE 				= [];

	        eventoFila.IdTipoEvento 				= evento;
	        eventoFila.IdModuloSolicita 			= modulo;
	        eventoFila.IdUsuarioSolicita 		= $scope.$root.SesionIdUsuario;
	        eventoFila.Fecha 					= ppdg.kngFechaSistema;
	        eventoFila.IdConcepto 				= evento;
	        eventoFila.NumeroOperacionConcepto	= String(ppdg.IdPersona);
	        eventoFila.FechaOperacionConcepto	= ppdg.kngFechaSistema;
	        eventoFila.IdCliente 				= ppdg.IdPersona;
	        eventoFila.Glosa 					= ppdg.dataPersona.DocumentoIdentidad;

	        parametrosSP.P_TABLAEVENTOS.push(eventoFila);
					
	        crearEventoAutorizacionSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Solicitud de autorizacion enviada",
						conflictos : util.parseServerResponse(result)}, ppdg.ventanaId);
					
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}
			);
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
		
		ppdg.validaBotonGuardar = function() {
			// if (ppdg.pantalla.modoEdicion && ppdg.dataPersona.ResidenciaTributariaExtranjera === true){ 
			// 	bcsComunes.msg("warning","info","Checkbox de residencia extranjera marcado. Verificar Pais de Residencia.");
			// 	//ppdg.dataPersona.IdPaisResidencia = ppdg.dataPersonaOriginal.IdPaisResidencia;
			// }
			if(ppdg.configForm.TipoPersona === 'N') {
				if(ppdg.dataPersona.EstadoCivil === 'CA' || ppdg.dataPersona.EstadoCivil === 'CC') {
					if(ppdg.dataPersona.IdSocNegPersonaConyuge === false || ppdg.dataPersona.RegimenConyugal === false) {
						ppdg.botonGuardarActivado = false;
					}
				}
			}
			ppdg.botonGuardarActivado = ppdg.formPersonaDatosGenerales.$valid;
		};
		
		ppdg.obtenerDatosComparables = function(data){
			return {
				Nombres: data.Nombres,
				ApellidoPaterno: data.ApellidoPaterno,
				ApellidoMaterno: data.ApellidoMaterno,
				Sexo: data.Sexo,
				RazonSocial: data.RazonSocial,
				IdNombreFantasiaPrincipal: data.IdNombreFantasiaPrincipal,
				IdPaisNacimiento: data.IdPaisNacimiento,
				IdTransparenciaFiscal: data.IdTransparenciaFiscal,
				ResidenciaTributariaExtranjera: data.ResidenciaTributariaExtranjera,
				IdPaisResidencia: data.IdPaisResidencia,
				IdPaisNacionalidadPrincipal: data.IdPaisNacionalidadPrincipal,
				FechaNacimiento: data.FechaNacimiento,
				FechaDefuncion: data.FechaDefuncion,
				FechaInicioActividad: data.FechaInicioActividad,
				EstadoCivil: data.EstadoCivil,
				GlobalLegalEntity: data.GlobalLegalEntity,
				IdSocNegPersonaConyuge: data.IdSocNegPersonaConyuge,
				RegimenConyugal: data.RegimenConyugal,
				IdPersonaRepresentante: data.IdPersonaRepresentante,
				IdGiroPrincipal: data.IdGiroPrincipal,
				IdProfesion: data.IdProfesion,
				CodigoTipoCargoEmpleado: data.CodigoTipoCargoEmpleado,
				DetalleCargo: data.DetalleCargo,
				IdGiroCIIU: data.IdGiroCIIU,
				IdSocNegPersonaEmpleador: data.IdSocNegPersonaEmpleador,
				IdRangoPatrimonio: data.IdRangoPatrimonio,
				FechaBloqueo: data.FechaBloqueo,
				NombreTipoBloqueo: data.NombreTipoBloqueo,
				ComentarioBloqueo: data.ComentarioBloqueo,
				SituacionLaboral: data.IdSituacionLaboral,
				CodigoRangoRenta: data.CodigoRangoRenta,
				CodigoRangoNivelVenta: data.CodigoRangoNivelVenta
				
			}
		}
		
		ppdg.avisarCambioPanel = function() {
			const dataOriginal = ppdg.obtenerDatosComparables(ppdg.dataPersonaOriginal);
			const data = ppdg.obtenerDatosComparables(ppdg.dataPersona);
			const sonIguales = _.isEqual(data, dataOriginal);
			
			if (sonIguales) {
				ppdg.avisarCambiosOk();
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaDatosGenerales' });
			}		
		}

		ppdg.avisarCambiosOk = function() {
			$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaDatosGenerales' });	
		}
		
		$scope.$watchCollection('ppdg.dataPersona', function() {
			ppdg.validaBotonGuardar();
			if (ppdg.pantalla.modoEdicion) {
				ppdg.avisarCambioPanel();
			}
		});

		seteoFechaSistema();
		
		/******************************************************
		* WATCH ACTUALIZACION PESTAÑA
		******************************************************/
		$scope.$watch("admSoc.actualizarTab.PERSONA.DatosGenerales",function(nval,oval){
		//evalua si debe actualizar los datos de la pestana por cambios en otra pestana
			if(nval){
				ppdg.oConfiguracionNombreFantasia.filtros.read = false;
				ppdg.oConfiguracionGiroCIIU.filtros.read = false;
				ppdg.oConfiguracionGiro.filtros.read = false;
				ppdg.oConfiguracionRelacionado.filtros.read = false;
				ppdg.oConfiguracionRelacionadoRepLegal.filtros.read = false;
				$scope.admSoc.actualizarTab.PERSONA["DatosGenerales"] = false;
			}else{
				ppdg.oConfiguracionRelacionadoRepLegal.filtros.read = true;
				ppdg.oConfiguracionNombreFantasia.filtros.read = true;
				ppdg.oConfiguracionGiroCIIU.filtros.read = true;
				ppdg.oConfiguracionGiro.filtros.read = true;
				ppdg.oConfiguracionRelacionado.filtros.read = true;
			}
		});
			
		/******************************************************
		 * GO
		 * ***************************************************/
		$scope.$watch('$viewContentLoaded', function(){
			cargarDatosMaestroPPDG();
		});

		$scope.$watch("ppdg.dataPersona.IdEstado", function (newVal, oVal){
			if(ppdg.dataPersona.Tipo == 'N'){
				if (ppdg.dataPersona.IdEstado !== 'BLO'){	
					$("div#FORM_administrarSocioNegocio[ventanaid='"+ppdg.ventanaId+"']").parent().css("height", "540px");	
				}
				else{
					if(ppdg.dataPersona.EstadoCivil === 'CC' || ppdg.dataPersona.EstadoCivil === 'CA'){
						$("div#FORM_administrarSocioNegocio[ventanaid='"+ppdg.ventanaId+"']").parent().css("height", "615px");
					}
					else{
						$("div#FORM_administrarSocioNegocio[ventanaid='"+ppdg.ventanaId+"']").parent().css("height", "591px");
					}
								
				}

			}
			else{
				if (ppdg.dataPersona.IdEstado !== 'BLO'){	
					$("div#FORM_administrarSocioNegocio[ventanaid='"+ppdg.ventanaId+"']").parent().css("height", "470px");	
				}
				else{
					$("div#FORM_administrarSocioNegocio[ventanaid='"+ppdg.ventanaId+"']").parent().css("height", "534px");			
				}
			}
			
			bcsComunes.ventanaAutoHeight(idVentana);
		})
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------