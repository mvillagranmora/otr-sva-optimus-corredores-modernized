(function() {
	angular.module('ModuloPlataforma').controller('ModalLimitesCuentaPropiaSpotFwCarteraCtrl',
		function ($scope,bcsComunes,servicioLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mlCPSFC 			= this;
		mlCPSFC.idVentana	= bcsComunes.ventanaWinId($scope);
		mlCPSFC.parametros	= bcsComunes.ventanaParams(mlCPSFC.idVentana);
		mlCPSFC.domVentana	= $("[ventanaid='"+mlCPSFC.idVentana+"']");
		
		var lineaDetalle	= _.clone(mlCPSFC.parametros.LimiteSeleccionado);
		
		mlCPSFC.dataDirectiva	= {}
		mlCPSFC.dataDirectiva	= _.clone(mlCPSFC.parametros.DataDirectiva);
		
		mlCPSFC.win			= {};
		mlCPSFC.cmbbx		= {};
		mlCPSFC.respuesta	= {};
		mlCPSFC.respuesta.IdEstadoDef = $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;		
		mlCPSFC.win.MontoDecimal = (util.bcs_formato(0,'ORD','MONTO',$scope.$root.SesionIdMonedaSistema,'','')).cantidadDecimales;
		
		mlCPSFC.IdBotonAceptar = 'ModalLimitesCuentaPropia'+ '_' + mlCPSFC.dataDirectiva.TipoLimite + '_aceptar' + mlCPSFC.parametros.AccionBoton;
		
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
					mlCPSFC.respuesta.IdTipoComportamiento = idComportamiento;
					mlCPSFC.respuesta.IdTipoComportamientoDef = idComportamiento;
				}
			});
		})();
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(mlCPSFC.idVentana, $translate.instant('UI_MODIFICAR_LIMITE_' + mlCPSFC.dataDirectiva.TituloModal));

			mlCPSFC.respuesta.IdTipoInstrumento		= lineaDetalle.IdTipoInstrumento || 0;
			mlCPSFC.respuesta.IdTipoInstrumentoDef	= lineaDetalle.IdTipoInstrumento || 0;
			mlCPSFC.respuesta.IdCanalIngreso		= lineaDetalle.IdCanalIngreso || 0;
			mlCPSFC.respuesta.IdCanalIngresoDef		= lineaDetalle.IdCanalIngreso || 0;
			mlCPSFC.respuesta.IdMonedaCalculo		= lineaDetalle.IdMonedaCalculo || $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteCuentaPropia;
			mlCPSFC.respuesta.IdMonedaCalculoDef	= lineaDetalle.IdMonedaCalculo;
			mlCPSFC.respuesta.IdMoneda				= lineaDetalle.IdMoneda || $scope.$root.SesionValoresDefectoSistema.IdMonedaLimiteCuentaPropia;
			mlCPSFC.respuesta.IdMonedaDef			= lineaDetalle.IdMoneda;
			mlCPSFC.respuesta.IdEstado				= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
			mlCPSFC.respuesta.IdEstadoDef			= lineaDetalle.IdEstado;
			mlCPSFC.respuesta.IdTipoComportamiento 	= lineaDetalle.IdTipoComportamiento;
			mlCPSFC.respuesta.IdTipoComportamientoDef 	= lineaDetalle.IdTipoComportamiento;
			
			if(mlCPSFC.dataDirectiva.Clasificacion !== 'SP'){
				mlCPSFC.respuesta.IdTipoOperacion		= lineaDetalle.IdTipoOperacion || 0;
				mlCPSFC.respuesta.IdTipoOperacionDef	= lineaDetalle.IdTipoOperacion || 0;
			}
			
			if(mlCPSFC.dataDirectiva.Clasificacion !== 'CARTERA' && mlCPSFC.dataDirectiva.TipoLimite !== 'LIMCTPAGENTE'){
				mlCPSFC.respuesta.IdInstrumento		= lineaDetalle.IdInstrumento || 0;
				mlCPSFC.respuesta.IdInstrumentoDef	= lineaDetalle.IdInstrumento || 0;
				mlCPSFC.respuesta.CodigoOperador	= lineaDetalle.CodigoOperador || 0;
				mlCPSFC.respuesta.CodigoOperadorDef	= lineaDetalle.CodigoOperador || 0;
				mlCPSFC.win.modoEditarTpoIns = false;
			}
			else if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPGRUPOEMISOR'){
				mlCPSFC.respuesta.IdGrupoEmisor		= lineaDetalle.IdGrupoEmisor || 0;
				mlCPSFC.respuesta.IdGrupoEmisorDef	= lineaDetalle.IdGrupoEmisor || 0;
				mlCPSFC.win.modoEditarEmisor = true;
			}
			else if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPEMISOR'){
				mlCPSFC.respuesta.IdEmisor		= lineaDetalle.IdEmisor || 0;
				mlCPSFC.respuesta.IdEmisorDef	= lineaDetalle.IdEmisor || 0;
			}
			
			if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPAGENTE'){
				mlCPSFC.respuesta.IdAgente		= lineaDetalle.IdAgente || 0;
				mlCPSFC.respuesta.IdAgenteDef	= lineaDetalle.IdAgente || 0;
				mlCPSFC.respuesta.MontoTotal	= lineaDetalle.MontoTotal || 0;
			}
			else if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPPOSICIONINTERBANCARIO'){
				mlCPSFC.respuesta.MontoIntraday	= lineaDetalle.MontoIntraday || 0;
				mlCPSFC.respuesta.MontoOvernight= lineaDetalle.MontoOvernight || 0;
			}
			else if (mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPSPOT') {
				const replaceWithIfEquals = (current, replacement, unwanted) => current === unwanted ? replacement : current;
				mlCPSFC.respuesta.IdInstrumento = replaceWithIfEquals(lineaDetalle.RazonSocial, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.IdInstrumentoDef = replaceWithIfEquals(lineaDetalle.RazonSocial, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.CodigoOperador = replaceWithIfEquals(lineaDetalle.CodigoOperador, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.CodigoOperadorDef = replaceWithIfEquals(lineaDetalle.CodigoOperador, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.IdMonedaPrincipal = lineaDetalle.IdMoneda;
				mlCPSFC.respuesta.IdMonedaPrincipalDef = lineaDetalle.IdMoneda;
				mlCPSFC.respuesta.MontoIntraday = lineaDetalle.MontoIntraday;
				mlCPSFC.respuesta.MontoOvernight = lineaDetalle.MontoOvernight;
			}
			else if (mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPFORWARD') {
				const replaceWithIfEquals = (current, replacement, unwanted) => current === unwanted ? replacement : current;
				mlCPSFC.respuesta.IdInstrumento = replaceWithIfEquals(lineaDetalle.RazonSocial, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.IdInstrumentoDef = replaceWithIfEquals(lineaDetalle.RazonSocial, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.CodigoOperador = replaceWithIfEquals(lineaDetalle.CodigoOperador, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.CodigoOperadorDef = replaceWithIfEquals(lineaDetalle.CodigoOperador, ' TODOS', 'TODOS');
				mlCPSFC.respuesta.IdMonedaPrincipal = lineaDetalle.IdMoneda;
				mlCPSFC.respuesta.IdMonedaPrincipalDef = lineaDetalle.IdMoneda;
				mlCPSFC.respuesta.MontoTotal = lineaDetalle.MontoTotal;
				mlCPSFC.respuesta.UsaPlazoMaximoDias = lineaDetalle.UsaPlazo === 'S';
				mlCPSFC.respuesta.PlazoMaximoDias = lineaDetalle.Plazo;
			}
			mlCPSFC.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(mlCPSFC.idVentana, $translate.instant('UI_AGREGAR_LIMITE_' + mlCPSFC.dataDirectiva.TituloModal));
			mlCPSFC.win.modoEditar = false;
			
			mlCPSFC.respuesta.IdInstrumento	= 0;
			mlCPSFC.respuesta.IdInstrumentoDef = 0;
			mlCPSFC.respuesta.IdTipoOperacion = 0;
			mlCPSFC.respuesta.IdTipoOperacionDef = 0;
			
			if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPSPOT'){
				mlCPSFC.respuesta.IdTipoInstrumento		= 'SPT';
				mlCPSFC.respuesta.IdTipoInstrumentoDef	= 'SPT';
				mlCPSFC.win.modoEditarTpoIns = false;
			}
			else if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPFORWARD'){
				mlCPSFC.respuesta.IdTipoInstrumento		= 'FWD';
				mlCPSFC.respuesta.IdTipoInstrumentoDef	= 'FWD';
				mlCPSFC.respuesta.UsaPlazoMaximoDias = false;
				mlCPSFC.win.modoEditarTpoIns = false;
			}
		}
		
		
		mlCPSFC.cancelar = function () {
			bcsComunes.ventanaData(mlCPSFC.idVentana, mlCPSFC.respuesta);
			bcsComunes.ventanaCerrar(mlCPSFC.idVentana);
		};
		
		
		mlCPSFC.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(lineaDetalle && lineaDetalle.IdLimite) { // EDITAR
				datosGuardar.p_IdLimite	= lineaDetalle.IdLimite;
				datosGuardar.p_RegistroCambio = [];
				if (mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPSPOT') {
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdInstrumento', ValorNuevo: String(mlCPSFC.dataModal.InstrumentoObj.IdInstrumento || 0) });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdCanalIngreso', ValorNuevo: (mlCPSFC.respuesta.IdCanalIngreso || '0').trim() });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'CodigoOperador', ValorNuevo: (mlCPSFC.respuesta.CodigoOperador || '').trim() });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdMoneda', ValorNuevo: (mlCPSFC.respuesta.IdMonedaPrincipal || '0').trim() });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'MontoIntraday', ValorNuevo: String(mlCPSFC.respuesta.MontoIntraday) || '0' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'MontoOvernight', ValorNuevo: String(mlCPSFC.respuesta.MontoOvernight) || '0' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdTipoComportamiento', ValorNuevo: mlCPSFC.respuesta.IdTipoComportamiento || '' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdEstado', ValorNuevo: mlCPSFC.respuesta.IdEstado || '' });
					
				} else if (mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPFORWARD') {
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdInstrumento', ValorNuevo: String(mlCPSFC.dataModal.InstrumentoObj.IdInstrumento || 0) });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdCanalIngreso', ValorNuevo: (mlCPSFC.respuesta.IdCanalIngreso || '0').trim() });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'CodigoOperador', ValorNuevo: (mlCPSFC.respuesta.CodigoOperador || '').trim() });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdMoneda', ValorNuevo: (mlCPSFC.respuesta.IdMonedaPrincipal || '0').trim() });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'MontoTotal', ValorNuevo: String(mlCPSFC.respuesta.MontoTotal) || '0' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'UsaPlazo', ValorNuevo: mlCPSFC.respuesta.UsaPlazoMaximoDias ? 'S' : 'N' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'UnidadPlazo', ValorNuevo: 'D' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'Plazo', ValorNuevo: String(mlCPSFC.respuesta.PlazoMaximoDias) || '0' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdTipoComportamiento', ValorNuevo: mlCPSFC.respuesta.IdTipoComportamiento || '' });
					datosGuardar.p_RegistroCambio.push({ CampoModificado: 'IdEstado', ValorNuevo: mlCPSFC.respuesta.IdEstado || '' });
				}

					
				if(datosGuardar.p_RegistroCambio.length === 0){
					mlCPSFC.respuesta.aceptar = true;
					bcsComunes.ventanaData(mlCPSFC.idVentana, mlCPSFC.respuesta);
					bcsComunes.ventanaCerrar(mlCPSFC.idVentana);
					return;
				}
				
				bcsComunes.ventanaOverlay(mlCPSFC.idVentana,true);
				servicioLimites.modificarLimiteCtaPropia(datosGuardar,function (result){
					mlCPSFC.evaluaRespuestaSP(result, 'UI_LIMITE_' + mlCPSFC.dataDirectiva.TituloModal + '_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(resError);
					bcsComunes.ventanaOverlay(mlCPSFC.idVentana,false);
				});
			}
			else { // CREAR
				//LIMCTPMESADISTRIBUCION y //LIMCTPPOSICIONINTERBANCARIO

				datosGuardar.p_IdTipoLimite		= mlCPSFC.dataDirectiva.TipoLimite;
				datosGuardar.p_IdTipoInstrumento= mlCPSFC.respuesta.IdTipoInstrumento;
				datosGuardar.p_IdCanalIngreso	= (mlCPSFC.respuesta.IdCanalIngreso || '0').trim();
				datosGuardar.p_IdMonedaCalculo	= (mlCPSFC.respuesta.IdMonedaCalculo || '0').trim();
				datosGuardar.p_IdMoneda			= (mlCPSFC.respuesta.IdMonedaPrincipal || '0').trim();
				datosGuardar.p_IdEstado			= mlCPSFC.respuesta.IdEstado;
				datosGuardar.p_IdClasificacionTipoInstrumento = mlCPSFC.dataDirectiva.Clasificacion;
				datosGuardar.p_IdTipoComportamiento = mlCPSFC.respuesta.IdTipoComportamiento;
				
				datosGuardar.p_IdInstrumento	= Number(mlCPSFC.dataModal.InstrumentoObj.IdInstrumento) || 0;
				datosGuardar.p_CodigoOperador	= (mlCPSFC.respuesta.CodigoOperador || '').trim();
				datosGuardar.p_MontoIntraday	= mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPSPOT' ? Number(mlCPSFC.respuesta.MontoIntraday) : 0;
				datosGuardar.p_MontoOvernight	= mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPSPOT' ? Number(mlCPSFC.respuesta.MontoOvernight) : 0;
				datosGuardar.p_MontoTotal 		= mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPFORWARD' ? Number(mlCPSFC.respuesta.MontoTotal) : 0;
				
				datosGuardar.p_UsaPlazo = mlCPSFC.respuesta.UsaPlazoMaximoDias ? 'S' : 'N';
				datosGuardar.p_UnidadPlazo = 'D';
				datosGuardar.p_Plazo = mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPFORWARD' ? Number(mlCPSFC.respuesta.PlazoMaximoDias) : 0;

				/*
				if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPGRUPOEMISOR'){
					datosGuardar.p_IdGrupoEmisor = mlCPSFC.respuesta.IdGrupoEmisor;
				}
				else if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPEMISOR'){
					datosGuardar.p_IdEmisor = mlCPSFC.respuesta.IdEmisor;
				}*/
								
				/*if(mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPAGENTE'){
					datosGuardar.p_IdClasificacionTipoInstrumento = mlCPSFC.dataDirectiva.Clasificacion;
					datosGuardar.p_IdAgente = mlCPSFC.respuesta.IdAgente;
				}*/
				
				bcsComunes.ventanaOverlay(mlCPSFC.idVentana,true);
				servicioLimites.crearLimiteCtaPropia(datosGuardar,function (result){
					mlCPSFC.evaluaRespuestaSP(result, 'UI_LIMITE_' + mlCPSFC.dataDirectiva.TituloModal + '_CREADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(resError);
					bcsComunes.ventanaOverlay(mlCPSFC.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		/*if(mlCPSFC.win.modoEditar && mlCPSFC.dataDirectiva.TipoLimite === 'LIMCTPGRUPOEMISOR'){
			mlCPSFC.domVentana.data("kendoWindow").setOptions({width: 600});
			mlCPSFC.domVentana.data("kendoWindow").setOptions({height: 500});
			
			mlCPSFC.grillaEmisores = {
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=414",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdGrupoEmisor", operator: "eq", value: (lineaDetalle && lineaDetalle.IdGrupoEmisor) || -1  }
						],
						schema: {
							model: {
								fields: {
									IdEmisor		: { type: "string"},
									RazonSocial		: { type: "string"},
									EstadoEmisor:{type: "string"}
								}
							}
						}
					}
				}),
				columns: [
					{field: "IdEmisor",		title: $translate.instant("UI_EMISOR"),			width: "100px"},
					{field: "RazonSocial",	title: $translate.instant("UI_RAZON_SOCIAL"),	width: "200px"},
					{field: "EstadoEmisor",	title: $translate.instant("UI_ESTADO"),			width: "60px"}
				],
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 180,
			    selectable 	: "row"
			};
		}*/
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		var operador	= 'eq';
		var valorFiltro	= mlCPSFC.dataDirectiva.Clasificacion;


		mlCPSFC.cmbbx.TipoInstrumento={
			filtros : {
				IdEstado: {field: "IdEstado",	operator: "eq" , value: "VIG" },
				IdBolsa	: {field: "IdBolsa",	operator: "eq" , value: $scope.$root.SesionBolsaDefectoSistema },
				IdClasificacionTipoInstrumento : { field: "IdClasificacionTipoInstrumento", operator: operador, value: valorFiltro }
			}
		};
		
		mlCPSFC.cmbbx.Instrumento={
			filtros : {
				IdEstado			: { field : "IdEstado",			operator: "eq", value : "ACT"},
				IdTipoInstrumento	: { field : "IdTipoInstrumento",operator: "eq", value : '-1' }
			}
		};

		mlCPSFC.cmbbx.TipoCanalIngreso={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			}
		};
		
		mlCPSFC.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			}
		};
		
		mlCPSFC.cmbbx.Moneda={
			filtros : {
				IdEstado			: { field : "IdEstado",			operator: "eq", value : "ACT"},
				IdTipoInstrumento	: { field : "IdTipoInstrumento",operator: "eq", value : '-1' }
			}
		};

		mlCPSFC.cmbbx.Operador = {
			filtros : {
				IdEstado: { field: "IdEstado", operator: "eq", value: "ACT" },
				IdEmpresa: { field: 'IdEmpresa', operator: 'eq', value: $scope.SesionIdEmpresa },
				EsEnrolado: { field: 'EsEnrolado', operator: 'eq', value: 'S' }
			}
		};

		/*mlCPSFC.cmbbx.Agente={
			filtros : {
				CodigoTipoCargoEmpleado : { field: "CodigoTipoCargoEmpleado", operator: "eq", value: "AGE" }
			}
		};*/

		/*mlCPSFC.cmbbx.GrupoEmisor={
			filtros : {
				IdEstado : { field: "IdEstado", operator: "eq", value: "ACT" }
			}
		};
		
		mlCPSFC.cmbbx.Emisor={
			filtros : {
				IdEstado : {field: "IdEstado", operator: "eq" , value: "ACT" }
			}
		};
		*/
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mlCPSFC.evaluaRespuestaSP = function(result, msj){
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
			
			mlCPSFC.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(mlCPSFC.idVentana,false);
			bcsComunes.ventanaData(mlCPSFC.idVentana, mlCPSFC.respuesta);
			bcsComunes.ventanaCerrar(mlCPSFC.idVentana);
		};
		
		
		$scope.$watch('mlCPSFC.respuesta.IdMoneda', function (newValue, oldValue){
			if(newValue !== oldValue)
				mlCPSFC.win.MontoDecimal = (util.bcs_formato(0,'ORD','MONTO',mlCPSFC.respuesta.IdMoneda,'','')).cantidadDecimales;
		});
		
		$scope.$watch('mlCPSFC.respuesta.UsaPlazoDias', function(newValue, oldValue) {
			console.log({newValue, oldValue});
		});
	});
}());