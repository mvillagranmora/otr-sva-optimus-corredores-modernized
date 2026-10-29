(function() {
	angular.module('ModuloPlataforma').controller('ModalLimitesCuentaPropiaCtrl',
		function ($scope,bcsComunes,servicioLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mLCP 		= this;
		mLCP.idVentana	= bcsComunes.ventanaWinId($scope);
		mLCP.parametros	= bcsComunes.ventanaParams(mLCP.idVentana);
		mLCP.domVentana	= $("[ventanaid='"+mLCP.idVentana+"']");
		
		var lineaDetalle = _.clone(mLCP.parametros.LimiteSeleccionado);
		
		mLCP.dataDirectiva = {};
		mLCP.dataDirectiva = _.clone(mLCP.parametros.DataDirectiva);
		
		mLCP.win		= {};
		mLCP.cmbbx		= {};
		mLCP.respuesta	= {};
		mLCP.respuesta.IdEstadoDef = $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
		
		mLCP.win.MontoDecimal = (util.bcs_formato(0,'ORD','MONTO',$scope.$root.SesionIdMonedaSistema,'','')).cantidadDecimales;
		
		mLCP.IdBotonAceptar = 'ModalLimitesCuentaPropia'+ '_' + mLCP.dataDirectiva.TipoLimite + '_aceptar' + mLCP.parametros.AccionBoton;
		
		// Si hay mas TipoLimite para copiar, agregarlos acá
		mLCP.win.modoCopiar = mLCP.parametros.AccionBoton === 'Copiar' && (mLCP.dataDirectiva.TipoLimite === 'LIMCTPEXCLUIREMISOR' || mLCP.dataDirectiva.TipoLimite === 'LIMCTPMARGENTASAS') ? true : false;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
	
		(function cargarTipoValidacionDefecto() {
			const dsParametrosSistema = new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering	: true, 
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
							dataType: "json"
						}		
					},
					schema: {
				     	model: {}
					},
					filter: [
						{ field: "CodigoParametro"	, operator: "eq", value: 'LIM.TipoValidacionPorDefecto' },
						{ field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa },
						{ field: "Modulo"           , operator: "eq", value: "LIMITE" },
					]
				}
			});
			
			dsParametrosSistema.fetch(function () {
				if (!lineaDetalle) {
					const idComportamiento = dsParametrosSistema.data()[0].Valor || '';
					mLCP.respuesta.IdTipoComportamiento = idComportamiento;
					mLCP.respuesta.IdTipoComportamientoDef = idComportamiento;
				}
			});
		})();
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(mLCP.idVentana, $translate.instant('UI_MODIFICAR_LIMITE_' + mLCP.dataDirectiva.TituloModal));
			mLCP.respuesta.IdTipoInstrumento		= lineaDetalle.IdTipoInstrumento || 0;
			mLCP.respuesta.IdTipoInstrumentoDef		= lineaDetalle.IdTipoInstrumento || 0;
			mLCP.respuesta.IdTipoOperacion			= lineaDetalle.IdTipoOperacion || 0;
			mLCP.respuesta.IdTipoOperacionDef		= lineaDetalle.IdTipoOperacion || 0;
			mLCP.respuesta.IdCanalIngreso			= lineaDetalle.IdCanalIngreso || 0;
			mLCP.respuesta.IdCanalIngresoDef		= lineaDetalle.IdCanalIngreso || 0;
			mLCP.respuesta.IdEstado					= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
			mLCP.respuesta.IdEstadoDef				= lineaDetalle.IdEstado;
			mLCP.respuesta.IdTipoComportamiento 	= lineaDetalle.IdTipoComportamiento || '';
			mLCP.respuesta.IdTipoComportamientoDef 	= lineaDetalle.IdTipoComportamiento || '';
			
			if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO'){
				mLCP.respuesta.Plazo		= lineaDetalle.Plazo || 0;
				mLCP.respuesta.IdMoneda		= lineaDetalle.IdMoneda || $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteCuentaPropia;
				mLCP.respuesta.IdMonedaDef	= lineaDetalle.IdMoneda;
			}
			else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPEXCLUIREMISOR'){
				mLCP.win.modoEditarEmisor = true;
			}
			else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPCLASIFRIESGO'){
				mLCP.respuesta.IdClasificacionRiesgo	= lineaDetalle.IdClasificacionRiesgo || 0;
				mLCP.respuesta.IdClasificacionRiesgoDef	= lineaDetalle.IdClasificacionRiesgo || 0;
				mLCP.respuesta.IdMonedaCalculo			= lineaDetalle.IdMonedaCalculo || $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteCuentaPropia;
				mLCP.respuesta.IdMonedaCalculoDef		= lineaDetalle.IdMonedaCalculo;
				mLCP.respuesta.MontoTotal				= lineaDetalle.MontoTotal || 0;
			}
			else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPMARGENTASAS'){
				mLCP.respuesta.IdMoneda				= lineaDetalle.IdMoneda || $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteCuentaPropia;
				mLCP.respuesta.IdMonedaDef			= lineaDetalle.IdMoneda;
				mLCP.win.modoEditarMargenTasas	= true;
			}
			mLCP.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mLCP.idVentana, $translate.instant('UI_AGREGAR_LIMITE_' + mLCP.dataDirectiva.TituloModal));
			mLCP.respuesta.MontoTotal = 0;
			mLCP.win.modoEditar = false;
		}
		
		
		mLCP.agregarEmisor = function() {
			mLCP.abrirModal(undefined, 'Agregar', "ModalLimitesCuentaPropiaEmisor");
		};
		mLCP.modificarEmisor = function(emisorSel) {
			mLCP.abrirModal(emisorSel, 'Modificar', "ModalLimitesCuentaPropiaEmisor");
		};
		
		mLCP.agregarRangos = function() {
			mLCP.abrirModal(undefined, 'Agregar', "ModalLimitesCuentaPropiaRangos");
		};
		mLCP.modificarRangos = function(rangosSel) {
			mLCP.abrirModal(rangosSel, 'Modificar', "ModalLimitesCuentaPropiaRangos");
		};
		
		
		mLCP.cancelar = function () {
			bcsComunes.ventanaData(mLCP.idVentana, mLCP.respuesta);
			bcsComunes.ventanaCerrar(mLCP.idVentana);
		};
		
		
		mLCP.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(lineaDetalle && lineaDetalle.IdLimite) { // EDITAR
				if(!mLCP.win.modoCopiar){
					datosGuardar.p_IdLimite	= lineaDetalle.IdLimite;
					
					datosGuardar.p_RegistroCambio = [];
					if(mLCP.respuesta.IdTipoInstrumento !== lineaDetalle.IdTipoInstrumento)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoInstrumento',ValorNuevo:mLCP.respuesta.IdTipoInstrumento});
					if(mLCP.respuesta.IdTipoOperacion !== lineaDetalle.IdTipoOperacion)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoOperacion',ValorNuevo:mLCP.respuesta.IdTipoOperacion});
					if(String(mLCP.respuesta.IdCanalIngreso) !== lineaDetalle.IdCanalIngreso)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdCanalIngreso',ValorNuevo:mLCP.respuesta.IdCanalIngreso});
					if(mLCP.respuesta.IdEstado !== lineaDetalle.IdEstado)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mLCP.respuesta.IdEstado});
					if(mLCP.respuesta.IdTipoComportamiento !== lineaDetalle.IdTipoComportamiento)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoComportamiento',ValorNuevo:mLCP.respuesta.IdTipoComportamiento});
	
					if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO'){
						if(String(mLCP.respuesta.Plazo) !== lineaDetalle.Plazo)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'Plazo',ValorNuevo:mLCP.respuesta.Plazo});
						if(mLCP.respuesta.IdMoneda !== lineaDetalle.IdMoneda)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMoneda',ValorNuevo:mLCP.respuesta.IdMoneda});
					}
					else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPCLASIFRIESGO'){
						if(String(mLCP.respuesta.IdClasificacionRiesgo) !== lineaDetalle.IdClasificacionRiesgo)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdClasificacionRiesgo',ValorNuevo:mLCP.respuesta.IdClasificacionRiesgo});
						if(mLCP.respuesta.MontoTotal !== lineaDetalle.MontoTotal)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'MontoTotal',ValorNuevo:mLCP.respuesta.MontoTotal});
						if(mLCP.respuesta.IdMonedaCalculo !== lineaDetalle.IdMonedaCalculo)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMonedaCalculo',ValorNuevo:mLCP.respuesta.IdMonedaCalculo});
					}
					else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPMARGENTASAS'){
						if(mLCP.respuesta.IdMoneda !== lineaDetalle.IdMoneda)
							datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMoneda',ValorNuevo:mLCP.respuesta.IdMoneda});
					}
						
					if(datosGuardar.p_RegistroCambio.length === 0){
						mLCP.respuesta.aceptar = true;
						bcsComunes.ventanaData(mLCP.idVentana, mLCP.respuesta);
						bcsComunes.ventanaCerrar(mLCP.idVentana);
						return;
					}
					
					bcsComunes.ventanaOverlay(mLCP.idVentana,true);
					servicioLimites.modificarLimiteCtaPropia(datosGuardar,function (result){
						mLCP.evaluaRespuestaSP(result, 'UI_LIMITE_' + mLCP.dataDirectiva.TituloModal + '_MODIFICADO');
					}, function(resError){
						bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
						console.log(result);
						bcsComunes.ventanaOverlay(mLCP.idVentana,false);
					});
				}
				else{
					datosGuardar.p_IdLimiteOrigen	= lineaDetalle.IdLimite;
					datosGuardar.p_IdTipoOperacion	= mLCP.respuesta.IdTipoOperacion;
					datosGuardar.p_IdTipoComportamiento	= mLCP.respuesta.IdTipoComportamiento;
					
					bcsComunes.ventanaOverlay(mLCP.idVentana,true);
					servicioLimites.copiarLimiteCtaPropia(datosGuardar,function (result){
						mLCP.evaluaRespuestaSP(result, 'UI_LIMITE_' + mLCP.dataDirectiva.TituloModal + '_COPIADO');
					}, function(resError){
						bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
						console.log(result);
						bcsComunes.ventanaOverlay(mLCP.idVentana,false);
					});
				}
			}
			else { // CREAR
				datosGuardar.p_IdTipoLimite		= mLCP.dataDirectiva.TipoLimite;
				datosGuardar.p_IdTipoInstrumento= mLCP.respuesta.IdTipoInstrumento;
				datosGuardar.p_IdTipoOperacion	= mLCP.respuesta.IdTipoOperacion;
				datosGuardar.p_IdCanalIngreso	= mLCP.respuesta.IdCanalIngreso;
				datosGuardar.p_IdEstado			= mLCP.respuesta.IdEstado;
				datosGuardar.p_IdClasificacionTipoInstrumento = mLCP.dataDirectiva.Clasificacion;
				datosGuardar.p_IdTipoComportamiento = mLCP.respuesta.IdTipoComportamiento;
				
				if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO'){
					datosGuardar.p_Plazo	= Number(mLCP.respuesta.Plazo);
					datosGuardar.p_IdMoneda	= mLCP.respuesta.IdMoneda;
				}
				else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPCLASIFRIESGO'){
					datosGuardar.p_IdClasificacionRiesgo= mLCP.respuesta.IdClasificacionRiesgo;
					datosGuardar.p_MontoTotal			= mLCP.respuesta.MontoTotal;
					datosGuardar.p_IdMonedaCalculo		= mLCP.respuesta.IdMonedaCalculo;
				}
				else if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPMARGENTASAS'){
					datosGuardar.p_IdMoneda			= mLCP.respuesta.IdMoneda;
				}
				
				bcsComunes.ventanaOverlay(mLCP.idVentana,true);
				servicioLimites.crearLimiteCtaPropia(datosGuardar,function (result){
					mLCP.evaluaRespuestaSP(result, 'UI_LIMITE_' + mLCP.dataDirectiva.TituloModal + '_CREADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(mLCP.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		if(mLCP.win.modoEditar && mLCP.dataDirectiva.TipoLimite === 'LIMCTPEXCLUIREMISOR'){
			mLCP.domVentana.data("kendoWindow").setOptions({width: 600});
			mLCP.domVentana.data("kendoWindow").setOptions({height: 400});
			
			mLCP.grillaEmisores = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=359",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdLimite", operator: "eq", value: (lineaDetalle && lineaDetalle.IdLimite) || -1  }
						],
						schema: {
							model: {
								fields: {
									IdLimiteEmisor	: { type: "number"},
									IdEmisor		: { type: "string"},
									IdLimite		: { type: "number"},
									RazonSocial		: { type: "string"},
									NombreEstadoEmisor:{type: "string"}
								}
							}
						}
					}
				}),
				columns: [
					{field: "IdEmisor",		title: $translate.instant("UI_EMISOR_EXCLUIDO"),width: "100px"},
					{field: "RazonSocial",	title: $translate.instant("UI_RAZON_SOCIAL"),	width: "200px"},
					{field: "NombreEstadoEmisor",title: $translate.instant("UI_ESTADO"),	width: "60px"}
				],
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 180,
			    selectable 	: "row"
			};
		}
		else if(mLCP.win.modoEditar && mLCP.dataDirectiva.TipoLimite === 'LIMCTPMARGENTASAS'){
			mLCP.grillaRangos = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=360",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdLimite", operator: "eq", value: (lineaDetalle && lineaDetalle.IdLimite) || -1  }
						],
						schema: {
							model: {
								fields: {
									IdRangosMontoPlazo	: { type: "number"},
									IdLimite			: { type: "number"},
									MontoDesde			: { type: "number"},
									MontoHasta			: { type: "number"},
									PlazoDesde			: { type: "number"},
									PlazoHasta			: { type: "number"},
									Tasa				: { type: "number"},
									PuntosTasaMenos		: { type: "number"},
									PuntosTasaMas		: { type: "number"},
									TasaFinalMenos		: { type: "number"},
									TasaFinalMas		: { type: "number"},
									PuntosTasaMenosAgente:{ type: "number"},
									PuntosTasaMasAgente	: { type: "number"},
									TasaFinalMenosAgente: { type: "number"},
									TasaFinalMasAgente	: { type: "number"},
									NombreEstadoRango	: { type: "string"}
								}
							}
						}
					}
				}),
				columns: [
					{field: "MontoDesde",			title: $translate.instant("UI_MONTO_DESDE"),		width: "60px",	template: "<label> #: (util.bcs_formato(MontoDesde,				'ORD','MONTO',		IdMoneda,'','')).valor #</label>"},
					{field: "MontoHasta",			title: $translate.instant("UI_MONTO_HASTA"),		width: "60px",	template: "<label> #: (util.bcs_formato(MontoHasta,				'ORD','MONTO',		IdMoneda,'','')).valor #</label>"},
					{field: "PlazoDesde",			title: $translate.instant("UI_PLAZO_DESDE"),		width: "55px",	template: "<label> #: (util.bcs_formato(PlazoDesde,				'ORD','CANTIDAD',	IdMoneda,'','')).valor #</label>"},
					{field: "PlazoHasta",			title: $translate.instant("UI_PLAZO_HASTA"),		width: "55px",	template: "<label> #: (util.bcs_formato(PlazoHasta,				'ORD','CANTIDAD',	IdMoneda,'','')).valor #</label>"},
					{field: "Tasa",					title: $translate.instant("UI_TASA"),				width: "40px",	template: "<label> #: (util.bcs_formato(Tasa,					'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "PuntosTasaMenos",		title: $translate.instant("UI_PIB_MENOS"),			width: "40px",	template: "<label> #: (util.bcs_formato(PuntosTasaMenos,		'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "PuntosTasaMas",		title: $translate.instant("UI_PIB_MAS"),			width: "40px",	template: "<label> #: (util.bcs_formato(PuntosTasaMas,			'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "TasaFinalMenos",		title: $translate.instant("UI_TASA_FINAL_MENOS"),	width: "50px",	template: "<label> #: (util.bcs_formato(TasaFinalMenos,			'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "TasaFinalMas",			title: $translate.instant("UI_TASA_FINAL_MAS"),		width: "50px",	template: "<label> #: (util.bcs_formato(TasaFinalMas,			'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "PuntosTasaMenosAgente",title: $translate.instant("UI_PIB_AGENTE_MENOS"),	width: "55px",	template: "<label> #: (util.bcs_formato(PuntosTasaMenosAgente,	'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "PuntosTasaMasAgente",	title: $translate.instant("UI_PIB_AGENTE_MAS"),		width: "55px",	template: "<label> #: (util.bcs_formato(PuntosTasaMasAgente,	'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "TasaFinalMenosAgente",	title: $translate.instant("UI_TASA_FINAL_AGENTE_MENOS"),width:"80px",template:"<label> #: (util.bcs_formato(TasaFinalMenosAgente,	'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "TasaFinalMasAgente",	title: $translate.instant("UI_TASA_FINAL_AGENTE_MAS"),	width:"80px",template:"<label> #: (util.bcs_formato(TasaFinalMasAgente,		'ORD','TASA',		IdMoneda,'','')).valor #</label>"},
					{field: "NombreEstadoRango",	title: $translate.instant("UI_ESTADO"),				width: "50px"}
				],
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 180,
			    selectable 	: "row"
			};
		}
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mLCP.cmbbx.TipoInstrumento={
					
				filtros : {
					IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
					IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.$root.SesionBolsaDefectoSistema },
					IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: mLCP.dataDirectiva.Clasificacion }
				}
			
			
			};
		/*
		if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO' && mLCP.dataDirectiva.Clasificacion=='RV'){
			  var clasifica='';
			  switch(mLCP.dataDirectiva.Clasificacion) {
			    case 'RV':
			       clasifica='SIM';
			        break;
			    default:
			       clasifica='';
			}
			  				  
				  
				mLCP.cmbbx.TipoInstrumento={
						
					filtros : {
						IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
						IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.$root.SesionBolsaDefectoSistema },
						IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: "eq", value: mLCP.dataDirectiva.Clasificacion },
						IdTipoInstrumento : { field: "IdTipoInstrumento", operator: "eq", value: clasifica}
					}
				
				};
				mLCP.respuesta.IdTipoInstrumentoDef=clasifica;
		};
		*/
		if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO' && mLCP.dataDirectiva.Clasificacion=='RV'){
			mLCP.cmbbx.TipoInstrumento.filtros.IdTipoInstrumento = { field: "IdTipoInstrumento", operator: "eq", value: "SIM" };
			mLCP.respuesta.IdTipoInstrumentoDef='SIM';
		};
		
		if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO' && mLCP.dataDirectiva.Clasificacion=='DV'){
			mLCP.cmbbx.TipoInstrumento.filtros.IdTipoInstrumento = { field: "IdTipoInstrumento", operator: "eq", value: "FWD" };
			mLCP.respuesta.IdTipoInstrumentoDef='FWD';
		};
		
		
		mLCP.cmbbx.TipoOperacion={
			filtros : {
				IdTipoInstrumento : { field : "IdTipoInstrumento", operator: "eq", value : '-1' }
			}
		};
		if((mLCP.dataDirectiva.Clasificacion=='RF' || mLCP.dataDirectiva.Clasificacion=='IF') && mLCP.dataDirectiva.TipoLimite === 'LIMCTPPLAZO'){
			var filters = {};
			var filtroIdTipoInstrumento = { field : "IdTipoInstrumento", operator: "eq", value : '-1' };
			filters = {
				complejo : [filtroIdTipoInstrumento, []],
				logic: "and",
				filters: [
					{
					logic: "or",
					filters: [
						{ field: "IdTipoOperacion", operator: "eq", value: "COMPAC" },
						{ field: "IdTipoOperacion", operator: "eq", value: "VENPAC" }
					]},
				]
			};
			mLCP.cmbbx.TipoOperacion.filtros = filters;
		}
		
		
		mLCP.cmbbx.TipoCanalIngreso={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			}
		};
		
		mLCP.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			}
		};
		
		if(mLCP.dataDirectiva.TipoLimite === 'LIMCTPCLASIFRIESGO'){
			mLCP.cmbbx.ClasificacionRiesgo={
				filtros : {
					IdEstadoClasificacionRiesgo		: { field: "IdEstadoClasificacionRiesgo",		operator: "eq", value: "ACT" },
					IdEstadoTipoClasificacionRiesgo	: { field: "IdEstadoTipoClasificacionRiesgo",	operator: "eq", value: "ACT" },
					//IdTipoInstrumento				: { field: "IdTipoInstrumento",					operator: "eq", value: '-1' },
					IdClasificacionTipoInstrumento	: { field: "IdClasificacionTipoInstrumento",	operator: "eq", value: mLCP.dataDirectiva.Clasificacion },
					//IdEntidadClasificadoraRiesgo	: { field: "IdEntidadClasificadoraRiesgo",		operator: "eq", value: mLCP.parametros.EntidadClasRiesgo }
				}
			}
		}
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mLCP.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","LIMITES CUENTA PROPIA",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","LIMITES CUENTA PROPIA",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","LIMITES CUENTA PROPIA",result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
			}
			else bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			mLCP.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(mLCP.idVentana,false);
			bcsComunes.ventanaData(mLCP.idVentana, mLCP.respuesta);
			bcsComunes.ventanaCerrar(mLCP.idVentana);
		};
		
		
		mLCP.abrirModal = function(filaSel, p_accion, modal){
	        if(p_accion === 'Agregar' || (filaSel && filaSel.IdEstado !== 'PEN')){
				var id1 = bcsComunes.ventanaAbrir(modal,{LimiteSeleccionado: lineaDetalle, FilaSel: filaSel, AccionBoton: p_accion},false,true);
				
				var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(respuesta){
		        	console.log(respuesta);
		        	if(respuesta.aceptar === true){
		        		if(modal === 'ModalLimitesCuentaPropiaEmisor'){
		        			mLCP.grillaEmisores.dataSource.read();
			        		mLCP.emisorSel = undefined;
		        		}
		        		else if(modal === 'ModalLimitesCuentaPropiaRangos'){
		        			mLCP.grillaRangos.dataSource.read();
		        			mLCP.rangosSel = undefined;
		        		}
					}
		        	else console.log("cierra modal");
		    	});
			}
		};
	});
}());