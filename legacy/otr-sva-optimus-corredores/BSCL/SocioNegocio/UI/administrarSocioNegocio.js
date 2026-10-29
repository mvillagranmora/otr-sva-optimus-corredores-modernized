/*************************************************************
 * Purpose	: Administrar Socio Negocio. Contenedor principal
 * 			  de los paneles internos.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 * 
 * Purpose	: Se incluyen definiciones para proyecto Perú
 * Author	: JGarces - BCS
 * Date		: 14/03/2017
 *************************************************************/
(function() { 
	angular.module('ModuloSocioNegocio').controller('AdministrarSocioNegocioCtrl',function($compile,$scope,$translate,
			servicioSociodeNegocio,bcsComunes,$timeout,$state,bcsAlerta,OPCIONES_BCSALERTA, $q){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		const admSoc = this;		
		$scope.showPanel = false;
		admSoc.actualizarTab = {PERSONA:{}, CLIENTE:{}, EMPLEADO:{}, CONTRAPARTE:{},PROVEEDOR:{}};// variable para almacenar pestañas a actualizar
		admSoc.formConfig = {};
		admSoc.fnLoadFormConfig = function(SesionLocale){
		var idVentana = bcsComunes.ventanaWinId($scope);
			if(SesionLocale == 'PE'){
				admSoc.formConfig.showNacionalidadSecundaria = true;
				admSoc.formConfig.showPaisResidenciaSecundaria = true;
				admSoc.formConfig.showGradoInstruccion = true;
				admSoc.formConfig.showAnosServicio = true;
				admSoc.formConfig.showGiroCIIU = true;
				admSoc.formConfig.showIngresoPromedioMensual = true;
				admSoc.formConfig.defaultSimboloMonedaSistema='S/.';
			}else{
				admSoc.formConfig.showNacionalidadSecundaria = false;
				admSoc.formConfig.showPaisResidenciaSecundaria = false;
				admSoc.formConfig.showGradoInstruccion = false;
				admSoc.formConfig.showAnosServicio = false;
				admSoc.formConfig.showGiroCIIU = false;
				admSoc.formConfig.showIngresoPromedioMensual = false;
				admSoc.formConfig.defaultSimboloMonedaSistema='$';
			}
			bcsComunes.ventanaFrente(bcsComunes.ventanaWinId($scope));
		}; 
		admSoc.fnLoadFormConfig($scope.SesionLocale);
		
		admSoc.datosEntrada = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
		admSoc.dataSocioNegocio = {};		
		admSoc.idVentana = bcsComunes.ventanaWinId($scope);
		admSoc.domVentana = $("[ventanaid='"+admSoc.idVentana+"']");
		admSoc.dataSocioNegocio.Matriz = {};
		
		var socioNegocioDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering: true,
	        	serverPaging: true,
	        	serverSorting: true,
	        	transport: {
	            	read: {
	            		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1355",
	            		dataType: "json"
	            	}
	        	},
	        	schema: {
	        		model: {
		        		fields: {
		        			IdSocioDeNegocio : { type: "number"}
		        			,FechaIngreso : { type: "date"}
		        			,FechaCreacion : { type: "date"}
		        			,FechaModificacion : { type: "date"}
		        			,IdTipoDocumentoIdentidadDefecto : { type: "string"}
		        			,NombreTipoDocumentoIdentidadDefecto : { type: "string"}
		        			,NombreCompleto : { type: "string"}
		        			,DocumentoIdentidadDefecto : { type: "string"}
		        			,TipoPersona : { type: "string"}
		        			,RazonSocial : { type: "string"}
		        			,NombreCompleto : { type: "string"}
		        			,Nombres : { type: "string"}
		        			,ApellidoPaterno : { type: "string"}
		        			,ApellidoMaterno : { type: "string"}
		        			,Alias : { type: "string"}
		        			,FechaNacimiento : { type: "date"}
		        			,IdPaisResidencia : { type: "string"}
		        			,CodigoPaisResidencia : { type: "string"}
		        			,NombrePaisResidencia : { type: "string"}
		        			,FechaBloqueo: { type: "date"}
		        			,EsCliente : { type: "string"}
		        			,EsEmpleado : { type: "string"}
		        			,EsProveedor : { type: "string"}
		        			,EsContraparte : { type: "string"}
	        			}
	        		}
	        	}
			}
		});
		
		var identificarEntidad = function(data){
			if(data){
				if(data.IdSocioNegocio && data.Entidad==="SOC")
					return {field:"IdSocioDeNegocio", operator:"eq", value: data.IdSocioNegocio}
				else (data.IdCliente && data.Entidad==="CLI")
					return {field:"IdCliente", operator:"eq", value: data.IdCliente}
			}
			bcsComunes.ventanaCerrar(admSoc.idVentana);
			bcsComunes.msg("error","SOCIO NEGOCIO",'OPERACION INVALIDA');
			return null;
			
		};

		// Promise chaining (dealing with different types of promises, non of these are ES2015 Promise) see https://docs.angularjs.org/api/ng/service/$q#when
		admSoc.socioNegocioPromise = $q
			.when(socioNegocioDatasource.query({ filter: identificarEntidad(admSoc.datosEntrada) }))
			.then(function () {
				admSoc.dataSocioNegocio = socioNegocioDatasource.view()[0];

				admSoc.tituloVentana = admSoc.dataSocioNegocio.NombreCompleto;
				admSoc.entidadasSocio = [];
				if(admSoc.dataSocioNegocio.EsCliente === 'UI_SI')
					admSoc.entidadasSocio.push($translate.instant('UI_CLIENTE'));
				if(admSoc.dataSocioNegocio.EsProveedor === 'UI_SI')
					admSoc.entidadasSocio.push($translate.instant('UI_PROVEEDOR'));
				if(admSoc.dataSocioNegocio.EsEmpleado === 'UI_SI')
					admSoc.entidadasSocio.push($translate.instant('UI_EMPLEADO'));
				if(admSoc.dataSocioNegocio.EsContraparte === 'UI_SI')
					admSoc.entidadasSocio.push($translate.instant('UI_CONTRAPARTE'));
				if(admSoc.entidadasSocio.length===0) {
					admSoc.entidadasSocio.push($translate.instant('UI_PERSONA'));
					admSoc.tituloVentana += ' - ' + admSoc.entidadasSocio[0];
				}
				else {
					admSoc.tituloVentana += ' - ' + admSoc.entidadasSocio[0];
					if(admSoc.entidadasSocio.length>1)
						for(var x=1;x<admSoc.entidadasSocio.length;x++)
							admSoc.tituloVentana += '|' + admSoc.entidadasSocio[x];
				}
				bcsComunes.ventanaTitulo(admSoc.idVentana, admSoc.tituloVentana);

				//DocumentoIdentidadFormateado:
				admSoc.dataSocioNegocio.DocumentoIdentidadDefectoFormateado = admSoc.dataSocioNegocio.DocumentoIdentidadDefecto;

				const requestBody = {
					p_TipoEnrolamiento: admSoc.dataSocioNegocio.TipoEnrolamiento || '',
					p_TipoPersona: admSoc.dataSocioNegocio.CodigoTipoPersona,
				};

				return servicioSociodeNegocio.consultarFormularioSocioNegocio(requestBody).$promise;
			})
			.then(function (result) {
				if (result.p_FormulariosSocioNegocio && Array.isArray(result.p_FormulariosSocioNegocio)) {
					$scope.admSoc.dataSocioNegocio.Matriz = result.p_FormulariosSocioNegocio.reduce(function (acum, current) {
						acum[current.Formulario] = current.CodigoFormulario;

						return acum;
					}, {});
				}

				$scope.showPanel = true;

				return true;
			});
		
		
		
		/******************************************************
		 * Funciones
		 * ***************************************************/
		admSoc.generarDoc = function(){
			var datos = {
				p_IdEmpresa	: $scope.SesionIdEmpresa,
				p_IdModulo	: "SOC",
				p_UsaReglasNegocio : "N",
				p_IdUsuario	: $scope.SesionIdUsuario,
				p_IdCliente	: admSoc.dataSocioNegocio.IdCliente,
				p_IdTipoCanalIngresoCorredor : "OPT"
								
			};

			bcsComunes.ventanaOverlay(admSoc.idVentana,true);
				servicioSociodeNegocio.regenerarContratosClientes(datos).$promise.then(
					function(result) {	
						if(result.P_OUT_MENSAJES.length==0){
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Se han regenerado los contratos del cliente correctamente",
							conflictos : util.parseServerResponse(result)}, admSoc.idVentana);
						}
						else{
							var idMensaje = bcsComunes.ventanaMsg({ mensaje : "No se ha realizado la Regeneración de Contratos",
							conflictos : util.parseServerResponse(result)}, admSoc.idVentana);
						}

						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(admSoc.idVentana,true);
							bcsComunes.ventanaOverlay(admSoc.idVentana,false);	
						});						
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al Regenerar los contratos");
						bcsComunes.ventanaData(admSoc.idVentana,true);
						bcsComunes.ventanaOverlay(admSoc.idVentana,false);
					}
				);
		
		};
		admSoc.fnCambioDeTab=function (ambito,nombreTab) {
			admSoc.tabs[ambito][nombreTab].active=true; 
			$timeout(function(e) {
				$.each(admSoc.tabs[ambito],function(key,value) { value.active=false; });
				admSoc.tabs[ambito][nombreTab].active=true; 
			}
			,0);
		};
		admSoc.fnPreguntaCambioDeTab=function (ambito,nombreTab) {
			var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_PREGUNTA_GUARDAR'));
			t.result.then(function(result){
				if (result === OPCIONES_BCSALERTA.NO) {
					admSoc.fnCambioDeTab(ambito, nombreTab);
				} else {
					$scope.$emit('DatosCambiadosGuardados', { ambito: ambito, nombreTab: nombreTab });
				}
			});	
		};
		
		admSoc.refrescar = function(){
			$.each(admSoc.tabs,function(ambito,value) { 
				$.each(admSoc.tabs[ambito],function(key,value) { 
					var tabSeleccionada = admSoc.tabs[ambito][key]
					if(tabSeleccionada.cargado){
						if(tabSeleccionada.active && tabSeleccionada.datosCambiados){
							var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_PREGUNTA_DATOS_SIN_GUARDAR'));
							t.result.then(function(result){
								if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
									
									admSoc.domVentana.find('#'+tabSeleccionada.id).empty().append($compile(tabSeleccionada.url)($scope));
																	
								}
							});
						}else{									
							
								admSoc.domVentana.find('#'+tabSeleccionada.id).empty().append($compile(tabSeleccionada.url)($scope));
										
						}
					}
				});
			});
		};
			
		/******************************************************
		 * Eventos
		 * ***************************************************/	
		admSoc.tabs = { 
				Persona : {
					PanelPersonaDatosGenerales					: { id : 'PanelPersonaDatosGenerales',					url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaDatosGenerales.html\'" ></div>',		  active : true,  cargado: false, datosCambiados: false },
					PanelPersonaDocumentosIdentidad				: { id : 'PanelPersonaDocumentosIdentidad',				url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaDocumentosIdentidad.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaCuentasBancarias				: { id : 'PanelPersonaCuentasBancarias',				url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaCuentasBancarias.html\'" ></div>',		  active : false, cargado: false, datosCambiados: false },
					PanelPersonaTarjetasBancarias				: { id : 'PanelPersonaTarjetasBancarias',				url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaTarjetasBancarias.html\'" ></div>',		  active : false, cargado: false, datosCambiados: false },
					PanelPersonaTelefonos						: { id : 'PanelPersonaTelefonos',						url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaTelefonos.html\'" ></div>',				  active : false, cargado: false, datosCambiados: false },
					PanelPersonaDireccionesFisicas				: { id : 'PanelPersonaDireccionesFisicas',				url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaDireccionesFisicas.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaDireccionesElectronicas			: { id : 'PanelPersonaDireccionesElectronicas',			url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaDireccionesElectronicas.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelPersonaGrupoDireccionesElectronicas	: { id : 'PanelPersonaGrupoDireccionesElectronicas',	url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaGrupoDireccionesElectronicas.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelPersonaAtributos						: { id : 'PanelPersonaAtributos',						url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaAtributos.html\'" ></div>',				  active : false, cargado: false, datosCambiados: false },
					PanelPersonaRelaciones						: { id : 'PanelPersonaRelaciones',						url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaRelaciones.html\'" ></div>',			  active : false, cargado: false, datosCambiados: false },
					PanelPersonaNombresFantasia					: { id : 'PanelPersonaNombresFantasia',					url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaNombresFantasia.html\'" ></div>',		  active : false, cargado: false, datosCambiados: false },
					PanelPersonaFatca							: { id : 'PanelPersonaFatca',							url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaFatca.html\'" ></div>',		  			  active : false, cargado: false, datosCambiados: false },
					PanelPersonaCrs								: { id : 'PanelPersonaCrs',								url : '<div ng-include="\'../../SocioNegocio/UI/PER/PanelPersonaCrs.html\'" ></div>',		  			  active : false, cargado: false, datosCambiados: false },
				},
				Cliente : {   
					PanelClienteDatosGenerales			: { id : 'PanelClienteDatosGenerales',	url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteDatosGenerales.html\'" ></div>',active : false, cargado: false, datosCambiados: false },
					PanelClientePortafolios				: { id : 'PanelClientePortafolios',		url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClientePortafolios.html\'" ></div>',	active : false, cargado: false, datosCambiados: false },
					PanelClientePortafoliosAPV			: { id : 'PanelClientePortafoliosAPV',	url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClientePortafoliosAPV.html\'" ></div>',active : false, cargado: false, datosCambiados: false },
					PanelClientePortafoliosCotitulares	: { id : 'PanelClientePortafoliosCotitulares',		url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClientePortafoliosCotitulares.html\'" ></div>',	active : false, cargado: false, datosCambiados: false },
					PanelClienteAtributos				: { id : 'PanelClienteAtributos',		url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteAtributos.html\'" ></div>',		active : false, cargado: false, datosCambiados: false },
					PanelClienteRequisitos				: { id : 'PanelClienteRequisitos',		url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteRequisitos.html\'" ></div>',	active : false, cargado: false, datosCambiados: false },
					PanelClienteDocumentos				: { id : 'PanelClienteDocumentos',		url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteDocumentos.html\'" ></div>',	active : false, cargado: false, datosCambiados: false },
					PanelClienteDOF						: { id : 'PanelClienteDOF',				url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteDOF.html\'" ></div>',			active : false, cargado: false, datosCambiados: false },
					PanelClienteAnotaciones				: { id : 'PanelClienteAnotaciones',		url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteAnotaciones.html\'" ></div>',	active : false, cargado: false, datosCambiados: false },
					PanelClienteNotificaciones			: { id : 'PanelClienteNotificaciones',	url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteNotificaciones.html\'" ></div>',active : false, cargado: false, datosCambiados: false },
					PanelClientePreferenciasNegocios	: { id : 'PanelClientePreferenciasNegocios',	url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClientePreferenciasNegocios.html\'" ></div>',active : false, cargado: false, datosCambiados: false },
					PanelClienteHistorialCambios		: { id : 'PanelClienteHistorialCambios',url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteHistorialCambios.html\'" ></div>',active : false, cargado: false, datosCambiados: false },
					PanelClienteEncuesta				: { id : 'PanelClienteEncuesta',	url : '<div ng-include="\'../../SocioNegocio/UI/CLI/PanelClienteEncuesta.html\'" ></div>',active : false, cargado: false, datosCambiados: false },
				},
				Empleado : {
					PanelEmpleadoDatosGenerales : { id : 'PanelEmpleadoDatosGenerales',url : '<div ng-include="\'../../SocioNegocio/UI/EMP/PanelEmpleadoDatosGenerales.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelEmpleadoAtributos	: { id : 'PanelEmpleadoAtributos',url : '<div ng-include="\'../../SocioNegocio/UI/EMP/PanelEmpleadoAtributos.html\'" ></div>', active : false, cargado: false, datosCambiados: false }
				},
				Contraparte : {
					PanelContraparteDatosGenerales : { id : 'PanelContraparteDatosGenerales',url : '<div ng-include="\'../../SocioNegocio/UI/CNT/PanelContraparteDatosGenerales.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
				},
				Proveedor : {
					PanelProveedorDatosGenerales : { id : 'PanelProveedorDatosGenerales',url : '<div ng-include="\'../../SocioNegocio/UI/PRO/PanelProveedorDatosGenerales.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelProveedorAtributos	: { id : 'PanelProveedorAtributos',url : '<div ng-include="\'../../SocioNegocio/UI/PRO/PanelProveedorAtributos.html\'" ></div>', active : false, cargado: false, datosCambiados: false }
				},			
		};

		$scope.$on('HayDatosSinGuardar', function (evt, data) {
			admSoc.tabs[data.ambito][data.nombreTab].datosCambiados = true;			
		});

		$scope.$on('DatosCambiadosGuardados', function (evt, data) {
			admSoc.tabs[data.ambito][data.nombreTab].datosCambiados = false;
		});
		
		$scope.handleTabClick = function() {
            addClassToLastLi();
          };

		admSoc.fnTabSelect = function (ambito, nombreTab) {
			if (admSoc.tabs[ambito][nombreTab].cargado !== true) {
				admSoc.tabs[ambito][nombreTab].cargado = true;
				admSoc.domVentana.find('#'+admSoc.tabs[ambito][nombreTab].id).append($compile(admSoc.tabs[ambito][nombreTab].url)($scope));
			}
		};
		
		admSoc.fnTabDeselect = function(ambito,nombreTab) {
			if (admSoc.tabs[ambito][nombreTab].datosCambiados === true) admSoc.fnPreguntaCambioDeTab(ambito,nombreTab);
		};	

		admSoc.MaintabSelect = function(ambito) {
			admSoc.EncuestaActiva = true;
			switch(ambito) {
			case 'Cliente':
				admSoc.tabs['Cliente']['PanelClienteDatosGenerales'].active = true;
				admSoc.fnTabSelect('Cliente','PanelClienteDatosGenerales');
				
				if (admSoc.dataSocioNegocio.TipoEnrolamiento == 'FULL'){
					admSoc.EncuestaActiva = true;
					}
				else{
					admSoc.EncuestaActiva = false;
				}
				
				break;
			case 'Empleado':
				admSoc.tabs['Empleado']['PanelEmpleadoDatosGenerales'].active = true;
				admSoc.fnTabSelect('Empleado','PanelEmpleadoDatosGenerales');
				break;
			case 'Contraparte':
				admSoc.tabs['Contraparte']['PanelContraparteDatosGenerales'].active = true;
				admSoc.fnTabSelect('Contraparte','PanelContraparteDatosGenerales');
				break;
			case 'Proveedor':
				admSoc.tabs['Proveedor']['PanelProveedorDatosGenerales'].active = true;
				admSoc.fnTabSelect('Proveedor','PanelProveedorDatosGenerales');
				break;			
			}
		};

		
	});

	function addClassToLastLi() {
		let ulElement = document.querySelector('form#FormSocioNegocio > .row > .ng-isolate-scope > .tab-content > .tab-pane.ng-scope.active>.row.ng-scope>.ng-isolate-scope>.nav.nav-tabs');
		let listaItems = ulElement.querySelectorAll("li");
		let primeraFilaLi = [];

		for (let i = 0; i < listaItems.length; i++) {
		  if (listaItems[i].offsetTop === listaItems[0].offsetTop) {
			primeraFilaLi.push(listaItems[i]);
		  } else {
			break;
		  }
		}
		
		let ultimoLi = primeraFilaLi[primeraFilaLi.length - 1];

		for (var i = 0; i < listaItems.length; i++) {
		  listaItems[i].classList.remove("lastLi");
		}

		ultimoLi.classList.add("lastLi");
	  }
}());
//------------------------- FIN CONTROLLER --------------------------