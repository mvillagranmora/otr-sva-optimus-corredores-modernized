(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaCrsCtrl',
			function($scope, $translate,bcsAlerta,servicioSociodeNegocio, bcsComunes, $resource, bcsConfiguracionSociodeNegocio, bcsServicioPlataforma){
		//var fechaSistema = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		var ppc = this;
		ppc.dataPersona = {};
        ppc.dataPersonaOriginal = {};
		ppc.configForm = {
		    TipoPersona : $scope.admSoc.dataSocioNegocio.CodigoTipoPersona //carga automatica de campos correspondientes al tipo persona
		};
        ppc.combo = {};
        ppc.idVentana = bcsComunes.ventanaWinId($scope);
        ppc.Habilitado = false;
		ppc.HabilitadoExtranjero = false;

        $scope.$watch("admSoc.actualizarTab.PERSONA.Crs",function(nval){
            //evalua si debe actualizar los datos de la pestana por cambios en otra pestana
            if(nval){
                cargarDatosMaestroPPC();
                $scope.admSoc.actualizarTab.PERSONA["Crs"] = false;
            }
        });

		ppc.modificarPaisResidenciaFiscal = function(accion) {
			var id1 = bcsComunes.ventanaAbrir("ModalPaisResidenciaFiscal",
				{Accion: accion
				,IdPersona: $scope.admSoc.dataSocioNegocio.IdPersona
				,dataGrilla: ppc.paisResidenciaFiscalSeleccionada
				}, false, false);
	        var promesa=bcsComunes.ventanaPromesa(id1);
			ppc.Habilitado = false;                               
	        promesa.then(function(respuesta){ 
	        	if (respuesta){
					ppc.paisResidenciaFiscalSeleccionada = undefined;
					ppc.paisResidenciaFiscalOptions.dataSource.read();
					
				}
	    	}); 
		};

		ppc.editarPPC = function editarPPC() {
			ppc.Habilitado = true;
		};

        ppc.cancelarPPC = function cancelarPPC() {
			ppc.Habilitado = false;
			ppc.dataPersona = $.extend({},ppc.dataPersonaOriginal);
			ppc.avisarCambiosOk()
		};

		let cargarDatosMaestroPPC = function cargarDatosMaestroPPC() {
            ppc.Habilitado = false;
			$scope.admSoc.socioNegocioPromise.then(function(){
				ppc.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;

				ppc.paisResidenciaFiscalOptions = {
					dataSource: new bcs.DataSource({
						type: "odata",
						definition: {
							serverFiltering: true,
							serverPaging 	: true,
							serverSorting 	: false,
							pageable:true,
							pageSize: 5,
							transport: {
							read: {
									url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=101010129",
									dataType: "json"
								}
							},
							filter: [
								{ field: "IdPersona", operator: "eq", value: Number(ppc.IdPersona) }
							],					
							sort: { field: "FechaCreacion", dir: "desc" },
							schema: {
								model: { fields: { 
									IdPaisResidenciaFiscal: { type: "number"},
									IdPersona: { type: "number"},
									IdPais: { type: "number"},
									FechaInicioTributacionPais: { type: "date"},
									NombrePais: { type: "string"},
									IdEstado: { type: "string"},
									FechaCreacion: { type: "date"},
									FechaModificacion: { type: "date"}
									}
								}
							},
						}
					}),
					columns	: [
					{
						field: "NombrePais",
						title: $translate.instant('UI_PAIS'),
						attributes: {style:"text-align:left;"},
						width: "60px"
					},{
						field: "FechaInicioTributacionPais",
						title: $translate.instant('UI_FECHA_INICIO_TRIBUTACION_PAIS_RESIDENCIA_FISCAL'),
						attributes: {style:"text-align:left;"},
						format: "{0:d}",
						width: "80px"
					},{
						field: "IdEstado",
						title: $translate.instant('UI_NOMBRE_ESTADO'),
						attributes: {style:"text-align:left;"},
						width: "100px"
					}],		
					sortable			: true,
					groupable			: true,
					pageable : {
						pageSize : 5
					},
					autoBind	: true,  //comentar
					height		: 140,
					selectable	: "single row",
					filterable 	: false
				};

				validarActualizacionDeDatos();
			});
		};
		let validarActualizacionDeDatos = function validarActualizacionDeDatos(){
			ppc.Habilitado = false;
			let personaCRSDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1315",
							dataType: "json"
						}
					},
					filter: { field: "IdPersona", operator: "eq", value: Number(ppc.IdPersona) },
					schema: {
						model: {
							fields : {
								DeclaracionCRS : { type: "string"},
							}
						}
					}
				}
			});
			personaCRSDatasource.query().then(function(){
				let dataPersonaCRS = $.extend({},personaCRSDatasource.data()[0]);
				dataPersonaCRS.DeclaracionCRS = dataPersonaCRS.DeclaracionCRS === 'S' ? true : false;
				
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
						filter: { field: "IdPersona", operator: "eq", value: Number(ppc.IdPersona) },
						schema: {
							model: {
								fields : {
									ResidenciaTributariaExtranjera : { type: "string"},
								}
							}
						}
					}
				});
				personaDatosGeneralesDatasource.query().then(function(){
					let dataPersonaDG = $.extend({},personaDatosGeneralesDatasource.data()[0]);
					dataPersonaDG.ResidenciaTributariaExtranjera = dataPersonaDG.ResidenciaTributariaExtranjera === 'N'|| dataPersonaDG.ResidenciaTributariaExtranjera === null ? false : true; //BLOQUEA PAIS RESIDENCIA
					ppc.dataPersona = $.extend({},dataPersonaCRS,dataPersonaDG);
					ppc.dataPersonaOriginal = $.extend({}, ppc.dataPersona);
					if(dataPersonaDG.ResidenciaTributariaExtranjera == true){
						ppc.HabilitadoExtranjero = true;
					}else{
						ppc.HabilitadoExtranjero = false;
					}
				});
			});
		}

		
        let modificarPersonaCrs = function modificarPersonaCrs(v_Dato) {
			Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=304";
		    let r = $resource ("", {}, { 'url': { method: "POST", url: Url} } );
		    let post = r.url(v_Dato);
		    return post.$promise;
	    };

        let modificarDGPersonaCrs = function modificarDGPersonaCrs(v_Dato) {
			Url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=68";
		    let r = $resource ("", {}, { 'url': { method: "POST", url: Url} } );
		    let post = r.url(v_Dato);
		    return post.$promise;
	    };

        let actualizarDatos = function actualizarDatos(registroCambio, parametros){
            let dataPersonaDG = registroCambio.filter(registroCambio => ["ResidenciaTributariaExtranjera"].includes(registroCambio.CampoModificado));
            let dataFatca = registroCambio.filter(registroCambio => ["DeclaracionCRS"].includes(registroCambio.CampoModificado));                    
        
            let promesas = [];

            if(dataPersonaDG.length > 0){
                let parametrosPersona = {
                    ...parametros,
                    p_UsaReglasNegocio: 'S',
                    p_IdTipoCanalIngresoCorredor: $scope.SesionTipoCanalIngreso,
                    p_RegistroCambio: dataPersonaDG
                };
                promesas.push(modificarDGPersonaCrs(parametrosPersona));
            }
            if(dataFatca.length > 0){
                let parametrosFatca = {
                    ...parametros,
                    p_UsaReglasNegocio: 'N',
                    p_RegistroCambio: dataFatca
                };
                promesas.push(modificarPersonaCrs(parametrosFatca));
            }
            return Promise.all(promesas);
        }

		ppc.guardarPPC = function guardarPPC() {
            bcsComunes.ventanaOverlay(ppc.idVentana, true);
			if (ppc.Habilitado && ppc.dataPersona.ResidenciaTributariaExtranjera === true){
				bcsComunes.msg("warning","info","Checkbox de residencia extranjera marcado. Verificar Pais de Residencia.");
            }
			let campos = [	
                            { nombre: "ResidenciaTributariaExtranjera",tipo: "bool"},
                            { nombre: "DeclaracionCRS",tipo: "bool"}
                        ];
	
			let registroCambio = [];
			$.each(campos,function(key,value) {
                if (value.tipo === 'bool') {
					if (ppc.dataPersona[value.nombre] !== ppc.dataPersonaOriginal[value.nombre] || ppc.dataPersona[value.nombre] !== ppc.dataPersonaOriginal[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (ppc.dataPersona[value.nombre]?"S":"N")});
				}	
			});

            parametros = {}
            parametros.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
            parametros.p_IdUsuario = $scope.$root.SesionIdUsuario;
            parametros.p_IdModulo = 'SOC';
            parametros.p_UsaReglasNegocio = 'N';
            parametros.p_IdPersona = ppc.IdPersona;
            parametros.p_RegistroCambio = registroCambio;
            
			if (parametros.p_RegistroCambio.length === 0){ 
				bcsComunes.msg("warning","info","No hay modificaciones para guardar.");
				bcsComunes.ventanaOverlay(ppc.idVentana, false);
				cargarDatosMaestroPPC();
				return;
			}

            actualizarDatos(registroCambio, parametros).then(
                function(result){
                    bcsComunes.ventanaOverlay(ppc.idVentana, false);
                    var titulo = (result)? "Cambios realizado con éxito." : "La operacion tiene errores";
                    var idMensaje = bcsComunes.ventanaMsg({ mensaje : titulo,
                    conflictos: util.parseServerResponse(result)}, ppc.idVentana);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						ppc.avisarCambiosOk();
						validarActualizacionDeDatos();
						$scope.admSoc.actualizarTab.PERSONA["Fatca"] = true;
                    });	
                },
                function (result) {
                    bcsComunes.ventanaOverlay(ppc.idVentana,false);
                    bcsComunes.msg("error","Error","La operacion tiene errores");
                }
            );
	    };

		ppc.eliminar = function(pais) {
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_PAIS_RESIDENCIA_FISCAL"), ppc.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
					var paisResidenciaFiscal = {};
					var listPaisesResidenciaFiscal = [];
					var params = {};

					paisResidenciaFiscal.IdPaisResidenciaFiscal 	= pais.IdPaisResidenciaFiscal;
					paisResidenciaFiscal.FechaInicioTributacionPais = pais.FechaInicioTributacionPais;
					paisResidenciaFiscal.IdPais 					= pais.IdPais;
					paisResidenciaFiscal.IdUsuarioModificador		= $scope.$root.SesionIdUsuario;
					paisResidenciaFiscal.IdEstado					= "INA";
					paisResidenciaFiscal.IdEmpresa					= $scope.$root.SesionIdEmpresa;
					listPaisesResidenciaFiscal.push(paisResidenciaFiscal);

					params.idEmpresa = $scope.$root.SesionIdEmpresa;
					params.paisResidenciaFiscal = listPaisesResidenciaFiscal;
					params.accion = "MODIFICAR";
					//llamar el servicio de ingreso o modificacion
					servicioSociodeNegocio.CrearModificarPaisResidenciaFiscal(params,function(respuesta){
						console.log(respuesta);
						var id = bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},ppc.idVentana);
						bcsComunes.ventanaPromesa(id).then(function(result){
							ppc.paisResidenciaFiscalOptions.dataSource.read();
							bcsComunes.ventanaOverlay(ppc.idVentana, false);
						});
					},function(value){
						console.log(respuesta);
						var id = bcsComunes.ventanaMsg({
							conflictos:util.parseServerResponse(respuesta)
						},ppc.idVentana);
						bcsComunes.ventanaPromesa(id).then(function(result){
							bcsComunes.ventanaOverlay(ppc.idVentana, false);
						});
					});
					
				} else {
					bcsComunes.ventanaOverlay(ppc.idVentana, false);
					return;
				}
			});	
		};

		cargarDatosMaestroPPC();

		ppc.obtenerDatosComparables = function(data){
			return {
				ResidenciaTributariaExtranjera: data.ResidenciaTributariaExtranjera ? data.ResidenciaTributariaExtranjera : '',
                DeclaracionCRS: data.DeclaracionCRS ? data.DeclaracionCRS : ''
			};
        }	

		ppc.avisarCambioPanel = function() {
			const dataOriginal = ppc.obtenerDatosComparables(ppc.dataPersonaOriginal);
			const data = ppc.obtenerDatosComparables(ppc.dataPersona);
			const sonIguales = _.isEqual(data, dataOriginal);
			
			if (sonIguales) {
				ppc.avisarCambiosOk();
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "Persona", nombreTab: 'PanelPersonaCrs' });
			}		
		}

		ppc.avisarCambiosOk = function() {
			$scope.$emit('DatosCambiadosGuardados', { ambito: "Persona", nombreTab: 'PanelPersonaCrs' });	
		}
		
		$scope.$watchCollection('ppc.dataPersona', function(newv) {
			if (ppc.Habilitado) {
				ppc.avisarCambioPanel();
			}
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------