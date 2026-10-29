angular.module('ModuloPlataforma').directive('bcsLimitesCuentaPropia',
function($q,$translate,bcsComunes,$timeout,$state){
	return {
		restrict: 'E',
    	replace: false,
		scope : {
			clasificacion	: '=?',	// RV, RF, SP
			tipoLimite		: '=?',	// LIMCTPPLAZO, LIMCTPEXCLUIREMISOR , LIMCTPCLASIFRIESGO, LIMCTPMARGENTASAS, etc ...
			tituloModal		: '=?'	// PLAZO, EXCLUIR_EMISOR, CLASIFICACION_RIESGO, MARGEN_TASAS, etc ...
		},
    	templateUrl: '../../Plataforma/UI/LIM/CuentaPropia/bcs-limites-cuenta-propia.html', 
    	link: function (scope, element, attrs, fn) {

    		console.log("directiva limites cuenta propia ");
    		
    		/*********************************************
			 * CHECK INICIAL DE VARIABLES
			 ********************************************/
    		if(scope.clasificacion	=== undefined) scope.clasificacion='';
			if(scope.tipoLimite		=== undefined) scope.tipoLimite='';
			if(scope.tituloModal	=== undefined) scope.tituloModal='';
			
    		/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			scope.win 				= scope.win	|| {};
			scope.win.modoLectura 	= true;
			
			scope.cmbbx		= scope.cmbbx 	|| {};
			scope.grilla	= scope.grilla 	|| {};
			scope.fn		= scope.fn 		|| {};
			scope.ds		= scope.ds 		|| {};
			
			var dataDirectiva = {};
			dataDirectiva.Clasificacion	= scope.clasificacion;
			dataDirectiva.TipoLimite	= scope.tipoLimite;
			dataDirectiva.TituloModal	= scope.tituloModal;

			(function prepararContexto() {
				const contextoPorTipoLimite = {};
				contextoPorTipoLimite['LIMCTPPLAZO'] = {
					nombreModal: 'ModalLimiteCuentaPropiaRFIFPlazoVencimiento',
					columnasGrilla: [
						{ field: 'TipoInstrumento', title: $translate.instant('UI_TIPO_INSTRUMENTO'), width: '170px' },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px' },
						{ field: 'Moneda', title: $translate.instant('UI_MONEDA_INSTRUMENTO'), width: '70px' },
						{ field: 'Plazo', title: $translate.instant('UI_PLAZO_MINIMO_DIAS'), width: '70px' },
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				contextoPorTipoLimite['LIMCTPEXCLUIREMISOR'] = {
					nombreModal: 'ModalLimiteCuentaPropiaRFIFEmisor',
					columnasGrilla: [
						{ field: 'TipoInstrumento', title: $translate.instant('UI_TIPO_INSTRUMENTO'), width: '170px' },
						{ field: 'TipoOperacion', title: $translate.instant('UI_TIPO_OPERACION'), width: '70px' },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px' },
						{ field: 'NombreEmisor', title: $translate.instant('UI_EMISOR'), width: '100px' }, // TODO: Reemplazar por nombre
						{ field: 'Porcentaje', title: $translate.instant('UI_PORCENTAJE_CUSTODIA'), width: '70px' }, // TODO: Formatear como porcentaje?
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				contextoPorTipoLimite['LIMCTPCLASIFRIESGO'] = {
					nombreModal: 'ModalLimiteCuentaPropiaRFIFClasificacionRiesgo',
					columnasGrilla: [
						{ field: 'TipoInstrumento', title: $translate.instant('UI_TIPO_INSTRUMENTO'), width: '170px' },
						{ field: 'TipoOperacion', title: $translate.instant('UI_TIPO_OPERACION'), width: '70px' },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px' },
						{ field: 'ClasificacionRiesgo', title: $translate.instant('UI_CLASIFICACION_RIESGO'), width: '100px' },
						{ field: 'MontoTotal', title: $translate.instant('UI_LIMITE_TOTAL'), width: '70px' },
						{ field: 'Moneda', title: $translate.instant('UI_MONEDA_LIMITE'), width: '70px' },
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				contextoPorTipoLimite['LIMCTPMARGENTASAS'] = {
					nombreModal: 'ModalLimiteCuentaPropiaRFIFMargenTasas',
					columnasGrilla: [
						{ field: 'TipoInstrumento', title: $translate.instant('UI_TIPO_INSTRUMENTO'), width: '170px' },
						{ field: 'TipoOperacion', title: $translate.instant('UI_TIPO_OPERACION'), width: '70px' },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px' },
						{ field: 'MargenInferior', title: $translate.instant('UI_MARGEN_TASA_INFERIOR'), width: '80px' },
						{ field: 'MargenSuperior', title: $translate.instant('UI_MARGEN_TASA_SUPERIOR'), width: '80px' },
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				contextoPorTipoLimite['LIMCTPMONEDA'] = {
					nombreModal: 'ModalLimiteCuentaPropiaRFIFMoneda',
					columnasGrilla: [
						{ field: 'TipoOperacion', title: $translate.instant('UI_TIPO_OPERACION'), width: '70px' },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px' },
						{ field: 'Moneda', title: $translate.instant('UI_MONEDA_LIMITE'), width: '70px' },
						{ field: 'Porcentaje', title: $translate.instant('UI_PORCENTAJE_CUSTODIA'), width: '70px' }, // TODO: Formatear como porcentaje?
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				contextoPorTipoLimite['LIMCTPSPOT'] = {
					nombreModal: 'ModalLimiteCuentaPropiaSpot',
					columnasGrilla: [
						{ field: 'TipoInstrumento', title: $translate.instant('UI_TIPO_INSTRUMENTO'), width: '100px', template: "<label translate> #: TipoInstrumento #</label>" },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px', template: "<label translate> #: CanalIngreso #</label>" },
						{ field: 'CodigoOperador', title: $translate.instant('UI_OPERADOR'), width: '100px', template: "<label translate> #: CodigoOperador #</label>" },
						{ field: "Moneda", title: $translate.instant("UI_MONEDA_PRINCIPAL"), width: "100px" },
						{ field: 'MontoIntraday', title: $translate.instant('UI_MONTO_INTRADAY'), width: '90px', template: "<label> #: (util.bcs_formato(MontoIntraday, 'ORD', 'MONTO', IdMoneda, '', '')).valor #</label>" },
						{ field: 'MontoOvernight', title: $translate.instant('UI_MONTO_OVERNIGHT'), width: '90px', template: "<label> #: (util.bcs_formato(MontoOvernight, 'ORD', 'MONTO', IdMoneda, '', '')).valor #</label>" },
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				contextoPorTipoLimite['LIMCTPFORWARD'] = {
					nombreModal: 'ModalLimiteCuentaPropiaForward',
					columnasGrilla: [
						{ field: 'TipoInstrumento', title: $translate.instant('UI_TIPO_INSTRUMENTO'), width: '100px', template: "<label translate> #: TipoInstrumento #</label>" },
						{ field: 'CanalIngreso', title: $translate.instant('UI_CANAL'), width: '50px', template: "<label translate> #: CanalIngreso #</label>" },
						{ field: 'CodigoOperador', title: $translate.instant('UI_OPERADOR'), width: '100px', template: "<label translate> #: CodigoOperador #</label>" },
						{ field: "Moneda", title: $translate.instant("UI_MONEDA_PRINCIPAL"), width: "100px" },
						{ field: 'MontoTotal', title: $translate.instant('UI_MONTO'), width: '90px', template: "<label> #: (util.bcs_formato(MontoTotal, 'ORD', 'MONTO', IdMoneda, '', '')).valor #</label>" },
						{ field: 'UsaPlazo', title: $translate.instant('UI_USA_PLAZO'), width: '80px' },
						{ field: 'Plazo', title: $translate.instant('UI_PLAZO_MAXIMO_DIAS'), width: '80px' },
						{ field: 'TipoComportamiento', title: $translate.instant('UI_TIPO_VALIDACION'), width: '70px' },
						{ field: 'IdEstadoConfigurable', title: $translate.instant('UI_ESTADO'), width: '50px' },
						{ field: 'IdEstadoAutorizacion', title: $translate.instant('UI_ESTADO_AUTORIZACION'), width: '70px' },
					],
				};
				
				const contextoSolicitado = contextoPorTipoLimite[scope.tipoLimite];
				if (contextoSolicitado === null || contextoSolicitado === undefined) {
					// throw new Error('Tipo limite [' + scope.tipoLimite + '] no esta implementado');
				}
				
				scope.contexto = contextoSolicitado;
			})();
			
			/*********************************************
			 * ACCIONES DE PANTALLA
			 ********************************************/
			scope.win.agregar = function agregar() {
				scope.fn.abrirModal(undefined, 'Agregar', scope.contexto.nombreModal);
			};

			scope.win.modificar = function modificar(limiteSel) {
				if (limiteSel && limiteSel.IdEstado !== 'PEN') {
					scope.fn.abrirModal(limiteSel, 'Modificar', scope.contexto.nombreModal);
				}
			};

			/*********************************************
			 * DATASOURCES
			 *********************************************/
			scope.ds.Configuracion = new kendo.data.DataSource({
				type: "odata",
				serverFiltering : true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=408",
						dataType: "json"
					}
				},
				schema: {
					model: { 
						fields: {		 	
							IdEmpresa	: { type: "number"},
							Codigo		: { type: "string"},
							Descripcion	: { type: "string"},
							Valor		: { type: "string"}
						}
					}
				}
			});
			
		
			/*********************************************
			 * COMBOBOX
			 *********************************************/
		
				
			/*********************************************
			 * GRILLAS
			 *********************************************/
			
			scope.grilla.grillaLimites = {
				dataSource : new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=101010103",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdEmpresa",						operator: "eq", value: scope.$root.SesionIdEmpresa },
						    { field: "IdTipoLimite",					operator: "eq", value: scope.tipoLimite },
						    { field: "IdClasificacionTipoInstrumento",	operator: "eq", value: scope.clasificacion === 'CARTERA' ? '' : scope.clasificacion}
						],
						schema: {
							model: {
								fields: {
									IdPlantillaLimite	: { type: "string"},
									IdLimite			: { type: "number"},
									TipoInstrumento		: { type: "string"},
									Nemotecnico			: { type: "string"},
									TipoOperacion   	: { type: "string"},
									CanalIngreso     	: { type: "string"},
									UsaPlazo        	: { type: "string"},
									Plazo				: { type: "number"},
									CantidadEmisores	: { type: "number"},
									ClasificacionRiesgo	: { type: "string"},
									CodigoOperador		: { type: "string"},
									UICodigoOperador	: { type: "string"},
									CodigoYNombreEmpleado:{ type: "string"},
									Moneda				: { type: "string"},
									MontoIntraday		: { type: "number"},
									MontoOvernight		: { type: "number"},
									MontoTotal			: { type: "number"},
									MonedaCalculo		: { type: "string"},
									IdGrupoEmisor		: { type: "string"},
									IdEmisor			: { type: "string"},
									NombreEmisor		: { type: "string"},
									NombreEstadoLimite	: { type: "string"},
									TipoComportamiento  : { type: "string"},
									RazonSocial         : { type: "string"},
									IdEstadoConfigurable: { type: "string"},
									IdEstadoAutorizacion: { type: "string"},
									MargenInferior		: { type: "number"},
									MargenSuperior		: { type: "number"},
									Porcentaje			: { type: "number"},
								}
							}
						}
					}
				}),
				columns: scope.contexto.columnasGrilla,
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 400,
				userSettings		: true,
				navigatable			: true,
				excel : {
					allPages: true,
				},
			    selectable 	: "row"
			};
		
		
			/*********************************************
			 * FUNCIONES
			 ********************************************/
			//Se obtiene valor Configuracion: IdClasificacionTipoInstrumento o IdTipoInstrumento
			var filterConfiguracion = [ { field: "IdEmpresa",	operator: "eq", value: scope.$root.SesionIdEmpresa },
			                            { field: "Codigo",		operator: "eq", value: 'ENTIDADCLASIFICADORARIESGO'} ];
			scope.ds.Configuracion.query({ filter: filterConfiguracion }).then(function(){
				if(scope.ds.Configuracion.view().length > 0) {
					scope.win.entidadClasRiesgo = scope.ds.Configuracion.view()[0].Valor;
				}
			});
			
			
			scope.fn.abrirModal = function(limiteSel, p_accion, modal){
				var id1 = bcsComunes.ventanaAbrir(modal,{DataDirectiva: dataDirectiva, LimiteSeleccionado: limiteSel, AccionBoton: p_accion, EntidadClasRiesgo: scope.win.entidadClasRiesgo},false,false,bcsComunes.ventanaWinId(scope));
				
				var promesa=bcsComunes.ventanaPromesa(id1);
		        promesa.then(function(respuesta){
		        	console.log(respuesta);
		        	if(respuesta.aceptar === true){
		        		scope.grilla.grillaLimites.dataSource.read();
		        		scope.grilla.limiteSel = undefined;
					}
		        	else{
		        		if(scope.tipoLimite === 'LIMCTPEXCLUIREMISOR'){
		        			scope.grilla.grillaLimites.dataSource.read();
			        		scope.grilla.limiteSel = undefined;
		        		}
		        		console.log("cierra modal");
		        	}
		    	});
			};
			
			scope.abrirMantenedorGrupoEmisores = function(){
				bcsComunes.ventanaAbrir("MantenedorGrupoEmisores",{});
			};
    	}
	}	
});