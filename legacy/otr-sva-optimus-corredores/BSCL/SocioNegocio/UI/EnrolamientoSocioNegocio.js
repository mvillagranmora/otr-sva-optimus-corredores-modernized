(function() { 
	angular.module('ModuloSocioNegocio').controller('EnrolamientoSocioNegocioCtrl',function($compile,$scope,$translate,bcsComunes,$timeout,servicioSociodeNegocio,bcsAlerta,OPCIONES_BCSALERTA, $q){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var admSoc = this;
		var idVentana = bcsComunes.ventanaWinId ($scope);
		$scope.showPanel = true;
		admSoc.formConfig = {};
		admSoc.fnLoadFormConfig = function(SesionLocale){
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
		admSoc.dataSocioNegocio = {IdEnrolamiento : admSoc.datosEntrada.IdEnrolamiento, Matriz : admSoc.datosEntrada.Matriz};		
		admSoc.idVentana = bcsComunes.ventanaWinId($scope);
		admSoc.domVentana = $("[ventanaid='"+admSoc.idVentana+"']");
		$scope.ValidaSalidaESC = function(){
			var t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_ENROLAMIENTO_INCONCLUSO'));
			t.result.then(function(result){
					if (result === OPCIONES_BCSALERTA.NO) {
					bcsComunes.ventanaCerrar(admSoc.idVentana);
				}
			});	
		};
		var socioNegocioDatasource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering: true,
	        	serverPaging: true,
	        	serverSorting: true,
	        	transport: {
	            	read: {
	            		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=700228",
	            		dataType: "json"
	            	}
	        	},
	        	schema: {
	        		model: {
		        		fields: {
		        			IdEnrolamiento : { type: "number"}
		        			,FechaIngreso : { type: "date"}
		        			,FechaModificacion : { type: "date"}
		        			,IdTipoDocumentoIdentidadDefecto : { type: "string"}
		        			,NombreTipoDocumentoIdentidadDefecto : { type: "string"}
							,NombreAbrTipoDocumentoIdentidadDefecto : { type: "string"}
		        			,NombreCompleto : { type: "string"}
		        			,DocumentoIdentidadDefecto : { type: "string"}
		        			,TipoPersona : { type: "string"}
		        			,RazonSocial : { type: "string"}
		        			,Nombres : { type: "string"}
		        			,ApellidoPaterno : { type: "string"}
		        			,ApellidoMaterno : { type: "string"}
		        			,Alias : { type: "string"}
		        			,FechaNacimiento : { type: "date"}
		        			,IdPaisResidencia : { type: "string"}
		        			,CodigoPaisResidencia : { type: "string"}
		        			,NombrePaisResidencia : { type: "string"}
	        			}
	        		}
	        	}
			}
		});
				
		admSoc.socioNegocioPromise = socioNegocioDatasource.query({filter:{field:"IdEnrolamiento", operator:"eq", value: admSoc.datosEntrada.IdEnrolamiento}}).then(function(){
			admSoc.dataSocioNegocio = socioNegocioDatasource.view()[0];
			admSoc.dataSocioNegocio.Matriz = admSoc.datosEntrada.Matriz;
			$scope.showPanel = true;
			admSoc.tituloVentana = $translate.instant('UI_ENROLAMIENTO') + " - "  + admSoc.dataSocioNegocio.NombreCompleto;
			bcsComunes.ventanaTitulo(admSoc.idVentana, admSoc.tituloVentana);
			admSoc.dataSocioNegocio.DocumentoIdentidadDefectoFormateado = admSoc.dataSocioNegocio.DocumentoIdentidadDefecto;
			return true;
		});
		
		
		
		/******************************************************
		 * Funciones
		 * ***************************************************/
		admSoc.fnCambioDeTab=function (ambito,nombreTab) {
			admSoc.tabs[ambito][nombreTab].active=true; 
			$timeout(function(e) {
				$.each(admSoc.tabs[ambito],function(key,value) { value.active=false; });
				admSoc.tabs[ambito][nombreTab].active=true; 
			}
			,0);
		};

		admSoc.fnPreguntaCambioDeTab=function (ambito,nombreTab) {
			const t = bcsAlerta.crear('PREGUNTA_SI_NO',$translate.instant('UI_PREGUNTA_GUARDAR'));
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
					if(tabSeleccionada.cargado && tabSeleccionada.id === "PanelPersonaDatosGenerales"){
							admSoc.domVentana.find('#'+tabSeleccionada.id).empty().append($compile(tabSeleccionada.url)($scope));
					}
				});
			});
		};		
		
		/******************************************************
		 * Eventos
		 * ***************************************************/	
		admSoc.tabs = { 
				Persona : {
					PanelPersonaDatosGenerales					: { id : 'PanelPersonaDatosGenerales',					url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaDatosGenerales.html\'" ></div>',		  active : true,  cargado: false, datosCambiados: false },
					PanelPersonaDocumentosIdentidad				: { id : 'PanelPersonaDocumentosIdentidad',				url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaDocumentosIdentidad.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaCuentasBancarias				: { id : 'PanelPersonaCuentasBancarias',				url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaCuentasBancarias.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaDireccionesFisicas				: { id : 'PanelPersonaDireccionesFisicas',				url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaDireccionesFisicas.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaTarjetasBancarias				: { id : 'PanelPersonaTarjetasBancarias',				url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaTarjetasBancarias.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaTelefonos						: { id : 'PanelPersonaTelefonos',						url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaTelefonos.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaDireccionesElectronicas			: { id : 'PanelPersonaDireccionesElectronicas',			url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaDireccionesElectronicas.html\'" ></div>',	  active : false, cargado: false, datosCambiados: false },
					PanelPersonaGrupoDireccionesElectronicas	: { id : 'PanelPersonaGrupoDireccionesElectronicas',	url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaGrupoDireccionesElectronicas.html\'" ></div>', active : false, cargado: false, datosCambiados: false },
					PanelPersonaAtributos						: { id : 'PanelPersonaAtributos',						url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaAtributos.html\'" ></div>',				  active : false, cargado: false, datosCambiados: false },
					PanelPersonaRelaciones						: { id : 'PanelPersonaRelaciones',						url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaRelaciones.html\'" ></div>',				  active : false, cargado: false, datosCambiados: false },
					PanelPersonaFatca							: { id : 'PanelPersonaFatca',							url : '<div ng-include="\'../../SocioNegocio/UI/ENR/PER/PanelPersonaFatca.html\'" ></div>',				  active : false, cargado: false, datosCambiados: false },
				},
				Cliente : {
					PanelClienteDatosGenerales					: { id : 'PanelClienteDatosGenerales',					url : '<div ng-include="\'../../SocioNegocio/UI/ENR/CLI/PanelClienteDatosGenerales.html\'" ></div>',		  active : false, cargado: false, datosCambiados: false },
					PanelClienteAtributos						: { id : 'PanelClienteAtributos',						url : '<div ng-include="\'../../SocioNegocio/UI/ENR/CLI/PanelClienteAtributos.html\'" ></div>',		active : false, cargado: false, datosCambiados: false },
					PanelClienteRequisitos						: { id : 'PanelClienteRequisitos',						url : '<div ng-include="\'../../SocioNegocio/UI/ENR/CLI/PanelClienteRequisitos.html\'" ></div>',	active : false, cargado: false, datosCambiados: false },
					
				},
				Portafolio: {
					PanelPortafolioCuentaCustodio					: { id : 'PanelPortafolioCuentaCustodio',					url : '<div ng-include="\'../../SocioNegocio/UI/ENR/POR/PanelPortafolioCuentaCustodio.html\'" ></div>',			  			active : false, cargado: false, datosCambiados: false },
					PanelPortafolioPreferenciasPago					: { id : 'PanelPortafolioPreferenciasPago',					url : '<div ng-include="\'../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasPago.html\'" ></div>',		  			active : false, cargado: false, datosCambiados: false },
					PanelPortafolioPreferenciasPagoClienteRecibe	: { id : 'PanelPortafolioPreferenciasPagoClienteRecibe',	url : '<div ng-include="\'../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasPagoClienteRecibe.html\'" ></div>',		active : false, cargado: false, datosCambiados: false },
					PanelPortafolioPreferenciasPagoClientePaga		: { id : 'PanelPortafolioPreferenciasPagoClientePaga',		url : '<div ng-include="\'../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasPagoClientePaga.html\'" ></div>',		active : false, cargado: false, datosCambiados: false },
					PanelPortafolioPreferenciasNegocio				: { id : 'PanelPortafolioPreferenciasNegocio',				url : '<div ng-include="\'../../SocioNegocio/UI/ENR/POR/PanelPortafolioPreferenciasNegocio.html\'" ></div>',		  		active : false, cargado: false, datosCambiados: false },
				}
		};
		
		$scope.$on('HayDatosSinGuardar', function (evt, data) {
			admSoc.tabs[data.ambito][data.nombreTab].datosCambiados = true;
		});

		$scope.$on('DatosCambiadosGuardados', function (evt, data) {
			admSoc.tabs[data.ambito][data.nombreTab].datosCambiados = false;
		});

		admSoc.fnTabSelect = function (ambito, nombreTab) {
			if (admSoc.tabs[ambito][nombreTab].cargado !== true) {
				admSoc.tabs[ambito][nombreTab].cargado = true;
				admSoc.domVentana.find('#'+admSoc.tabs[ambito][nombreTab].id).append($compile(admSoc.tabs[ambito][nombreTab].url)($scope));
			}
		};
		
		admSoc.fnTabDeselect = function(ambito,nombreTab) {
			if (admSoc.tabs[ambito][nombreTab].datosCambiados === true) admSoc.fnPreguntaCambioDeTab(ambito,nombreTab);
		};	

		admSoc.MaintabSelect = function mainTabSelect(ambito) {
			switch (ambito) {
				case 'Cliente':
					admSoc.tabs['Cliente']['PanelClienteDatosGenerales'].active = true;
					admSoc.fnTabSelect('Cliente', 'PanelClienteDatosGenerales');
					break;

				case 'Portafolio':
					admSoc.tabs['Portafolio']['PanelPortafolioCuentaCustodio'].active = true;
					admSoc.fnTabSelect('Portafolio', 'PanelPortafolioCuentaCustodio');
					break;

				case 'PortafolioPreferenciasPago':
					admSoc.tabs['Portafolio']['PanelPortafolioPreferenciasPagoClienteRecibe'].active = true;
					admSoc.fnTabSelect('Portafolio', 'PanelPortafolioPreferenciasPagoClienteRecibe');
					break;
			}
		};
		
		admSoc.CrearCliente = function CrearCliente(){
			bcsComunes.ventanaOverlay(admSoc.idVentana, true);
			const requestBody = {
                p_IdEmpresa: $scope.SesionIdEmpresa,
                p_IdUsuario: $scope.SesionIdUsuario,
                p_IdModulo: 'SOC',
                p_UsaReglasNegocio: 'N',
                p_CodigoFormulario: '',
				p_IdTipoCanalIngreso: 'OPT',
                p_Enrolamientos: [
                    {
						IdInterno : 0,
                        IdEnrolamiento: admSoc.datosEntrada.IdEnrolamiento
                    }
                ]
            };

			servicioSociodeNegocio.crearClienteFull(requestBody).$promise.then(
                function (result) {
					if(result.P_OUT_ERRORESCONTADOR==0){
	                    
						const idMensaje = bcsComunes.ventanaMsg({
	                        mensaje: $translate.instant('UI_CLIENTE_CREADO'),
	                        conflictos: util.parseServerResponse(result)
	                    }, admSoc.idVentana);

						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaCerrar(admSoc.idVentana);
						});
						
					} else{
						bcsComunes.ventanaOverlay(admSoc.idVentana, false);
						bcsComunes.ventanaMsg({
	                        mensaje: $translate.instant('UI_ERROR_CREAR_CLIENTE'),
	                        conflictos: util.parseServerResponse(result)
	                    }, admSoc.idVentana);

						if (result.P_OUT_REQUISITOS_ENR_FULL.length > 0) {
							bcsComunes.ventanaAbrir('ventanaGrillaRequisitosEnrolamientoFull', { validaciones: result.P_OUT_REQUISITOS_ENR_FULL });
						}
					}
					
                },
                function (result) {
					bcsComunes.ventanaOverlay(admSoc.idVentana, false);
                    bcsComunes.ventanaMsg({
                        mensaje: $translate.instant('UI_ERROR_CREAR_CLIENTE'),
                        conflictos: util.parseServerResponse(result)
                    }, admSoc.idVentana);
                }
				
            );
		}
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER --------------------------