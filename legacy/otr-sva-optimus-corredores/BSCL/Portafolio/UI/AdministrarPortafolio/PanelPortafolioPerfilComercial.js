/*************************************************************
 * Purpose	: Panel Portafolio Perfil Comercial
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('PanelPortafolioPerfilComercialCtrl',
	function($scope, $translate, bcsComunes, $translate, $resource, servicioPortafolio, bcsServicioPlataforma){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		var pPortCom = this;
		pPortCom.conf = {};
		pPortCom.win = {};
		pPortCom.cmbbx = {};
		pPortCom.ds = {};
		pPortCom.fn = {};
		pPortCom.grilla = {};
		pPortCom.datosAntes = {};
		pPortCom.datosDespues = {};
		pPortCom.idVentana = bcsComunes.ventanaWinId($scope);
		pPortCom.params = bcsComunes.ventanaParams(pPortCom.idVentana);
		pPortCom.win.modoLectura = true;
		pPortCom.CodigoFormulario = $scope.admPort.dataSocioNegocio.Matriz['PORTAFOLIO_CLIENTE_PERFIL_COMERCIAL_MATRIZ_OPERACIONAL'];
		pPortCom.camposRequeridos = {};

		// Configuración de campos requeridos para formularios
		bcsServicioPlataforma.CamposRequeridosFormulario(pPortCom.CodigoFormulario, function (respuesta) {
			pPortCom.camposRequeridos = respuesta;
		});

/*********************************************
 * MODALIDADES DE PANTALLA
 ********************************************/
		pPortCom.win.ModoEdicion = function(){
			pPortCom.win.modoLectura = false;
			pPortCom.grilla.optionsGrillaMatrizOperacional.dataSource.removeAll();
		};

		pPortCom.win.ModoLectura = function(){
			pPortCom.win.modoLectura = true;
			pPortCom.grilla.optionsGrillaMatrizOperacional.dataSource.removeAll();
		};

/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
		pPortCom.win.editar = function(){
			pPortCom.win.ModoEdicion();
		};

		pPortCom.win.guardar = function () {
			var dataPortafolio = pPortCom.fn.preparaDatosGuardar();
			bcsComunes.ventanaOverlay(pPortCom.idVentana, true);
			servicioPortafolio.updatePortafolio(dataPortafolio, function (result) {
				var titulo = "";
				if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
					titulo = "Portafolio modificado";
					pPortCom.win.ModoLectura();
					pPortCom.datosAntes = _.clone(pPortCom.datosDespues);
					pPortCom.filtroMatriz.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
					pPortCom.filtroMatriz.p_IdTipoEntidad = 'PORTAFOLIO',
						pPortCom.filtroMatriz.p_IdEntidad = String(pPortCom.datosDespues.IdPortafolio),
						pPortCom.filtroMatriz.p_IdPlantillaCobros = '';
					pPortCom.filtroMatriz.p_IdPlantillaProductos = '';
					pPortCom.grilla.optionsGrillaMatrizOperacional.dataSource.read();

					$scope.admPort.datos = _.clone(pPortCom.datosAntes);
					$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilComercial' });
				} else if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
					titulo = "La operacion tiene advertencias";
					pPortCom.win.cancelar();
				} else {
					titulo = "Error al guardar";
				}
				bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos: util.parseServerResponse(result)
				}, pPortCom.idVentana);
				bcsComunes.ventanaOverlay(pPortCom.idVentana, false);
			});
		};

		pPortCom.win.cancelar = function(){
			pPortCom.datosDespues = _.clone(pPortCom.datosAntes);
			pPortCom.win.ModoLectura();
			$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilComercial' });

		};


/*********************************************
 * 				COMBOBOX
 ********************************************/
		pPortCom.cmbbx.oConfiguracionPerfilComercial={
			tipo : "combobox-perfil-comercial",
			configuracion : {},
			filtros : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value : $scope.SesionIdEmpresa},
				IdCategoriaInversionista : {
						"field"   : "IdCategoriaInversionista",
						"operator": "eq",
						"value"   : "",
				}			
			},
			seleccionado : false,
		};

		
/*********************************************
 * 				GRILLAS
 ********************************************/
		pPortCom.filtroMatriz = { 
			p_IdEmpresa 			: $scope.SesionIdEmpresa, 
			p_IdTipoEntidad			:'PORTAFOLIO', 
			p_IdEntidad 			: pPortCom.params.IdPortafolio,
			p_IdPlantillaCobros		: '',
			p_IdPlantillaProductos	: ''
		};	
		
		pPortCom.grilla.optionsGrillaMatrizOperacional = {
			dataSource: new bcs.DataSource({
				type: "call",
				definition 	: {
					serverFiltering : true,
					serverPaging 	: false,
					serverSorting 	: true,
					callParameters	: pPortCom.filtroMatriz,					
					callTransaction	: "20052",
					callResultSet	: "P_OUT_MATRIZOPERACIONAL",
					schema: {
						model: {
							fields: {
								IdTipoProducto			: { type: "string"},
								NombreTipoProducto		: { type: "string"},
								IdTipoOperacion			: { type: "string"},
								NombreTipoOperacion		: { type: "string"},
								CobroIdTipoOperacion	: { type: "string"},
								IdTipoCanalIngreso		: { type: "string"},
								NombreTipoCanalIngreso	: { type: "string"},
								CobroIdCanalIngreso		: { type: "string"},
								IdCobro					: { type: "string"},
								ValorCobro				: { type: "number"},
								FormaCobro				: { type: "string"},
								ValorTopeMinimoOrden	: { type: "number"},
								IdMoneda				: { type: "string"},
								NombreCobro				: { type: "string"},
								IdCobroClasificacion	: { type: "number"},
								CodigoCobroClasificacion: { type: "string"},
								IdBolsa					: { type: "string"},
								NombreBolsa				: { type: "string"},
								IdEmpresa				: { type: "number"},
								
								//PERU
								CodigoDefinicionCobro 	: {type : "string"},
								NombreClaseCobro 		: {type : "string"},
								NombreTipoCobro 		: {type : "string"},
								ValorCobroCombinado 	: {type : "number"},
								IdMonedaCobroUI		 	: {type : "string"},
								ValorCobroMonedaPorcentaje 	: {type : "string"},
								UsoMonedaFormato 		: {type : "string"}
							}
						}
					},
					group: [
						{field: "NombreTipoProducto"},
				        {field: "NombreTipoOperacion"},
				        {field: "NombreBolsa"},
				        {field: "NombreTipoCanalIngreso"}
				    ]
				}
			}),
			columns: [
								
			],
			gridCreated: function(grid) {
				pPortCom.mainGrid = grid;
				util.refreshGridSize("grid-por-matriz-operacional", 500);
			},
			sortable	: false,
			groupable	: false,
			pageable	: false,
			autoBind	: true,
			height		: 240,
		    selectable	: "row",
			bcsName: 'ctm'
		};
		
		if ($scope.SesionLocale === 'CL') {
			pPortCom.grilla.optionsGrillaMatrizOperacional.columns = [
				{field: "NombreBolsa",				width: "120px",title: "UI_BOLSA",hidden :    true},
				{field: "NombreTipoProducto",		width: "120px",title: "UI_TIPO_PRODUCTO",hidden :    true},
				{field: "NombreTipoOperacion",		width: "120px",title: "UI_TIPO_OPERACION",hidden :    true},
				{field: "NombreTipoCanalIngreso",	width: "120px",title: "UI_CANAL_INGRESO",hidden :    true},
				
				{field: "CodigoCobroClasificacion",	width: "120px",title: "UI_CLASIFICACION"},
				{field: "IdCobro",					width: "120px",title: "UI_COBRO"},
				{field: "ValorCobro",				width: "120px",title: "UI_VALOR",template: kendo.template("#if (FormaCobro==='V') {##=ValorCobro# %#} else {##=ValorCobro##}#")},
				{field: "ValorTopeMinimoOrden",		width: "120px",title: "UI_MINIMO",template: kendo.template("#if (IdMoneda==='UF') {#UF #=ValorTopeMinimoOrden##} else {##=ValorTopeMinimoOrden##}#")},
				{field: "IdMoneda",					width: "100px",title: "UI_MONEDA"}

			]
		}
		if ($scope.SesionLocale === 'PE') {
			pPortCom.grilla.optionsGrillaMatrizOperacional.columns = [
				{field: "NombreBolsa",				width: "120px",title: "UI_BOLSA",hidden :    true},
				{field: "NombreTipoProducto",		width: "120px",title: "UI_TIPO_PRODUCTO",hidden :    true},
				{field: "NombreTipoOperacion",		width: "120px",title: "UI_TIPO_OPERACION",hidden :    true},
				{field: "NombreTipoCanalIngreso",	width: "120px",title: "UI_CANAL_INGRESO",hidden :    true},

				{field: "CodigoDefinicionCobro",		width: "100px",title: $translate.instant("UI_COBRO")},
				{field: "NombreClaseCobro",				width: "100px",title: $translate.instant("UI_CLASE_COBRO")},
				{field: "NombreTipoCobro",				width: "100px",title: $translate.instant("UI_TIPO_COBRO")},
				{field: "ValorCobroCombinado",			width: "100px",title: $translate.instant("UI_VALOR"),template : " <label> #:  (util.bcs_formato(ValorCobroCombinado,'GEN',UsoMonedaFormato,IdMonedaCobroUI, '','')).valor #</label>"},
				{field: "ValorCobroMonedaPorcentaje",	width: "100px",title: $translate.instant("UI_MONEDA_PORCENTAJE")},
			]
		}
		
/*********************************************
 * 				DATASOURCES
 ********************************************/
		pPortCom.ds.PerfilCobrosDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1327",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdInternal : { type: "int"}
							,Prioridad : { type: "int"}
							,IdPerfilCobros : { type: "string"}
							,DescripcionPerfilCobros : { type: "string"}
							,IdEmpresa : { type: "int"}
							,EstadoPerfilCobros : { type: "string"}
							,IdGrupoCobrosNormal : { type: "string"}
							,IdBolsa : { type: "string"}
							,IdTipoProducto : { type: "string"}
							,IdTipoOperacion : { type: "string"}
							,IdCanalIngreso : { type: "string"}
							,DiasPlazo : { type: "int"}
							,EstadoDetalle : { type: "string"}
							,IdCobro : { type: "string"}
							,ValorCobro : { type: "number"}
							,ValorTopeMinimoOrden : { type: "number"}
							,IdMoneda : { type: "string"}
							,EstadoDetalleGrupoCobroNormal : { type: "string"}
							,NombreCobro : { type: "string"}
							,IdCobroClasificacion : { type: "int"}
							,FormaCobro : { type: "string"}
							,PermiteCambiarValor : { type: "string"}
							,CodigoCobroClasificacion : { type: "string"}
							,CobroCalculado : { type: "int"}
						}
					}
				}
			}
		});

		pPortCom.ds.PerfilProductosDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1328",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPerfilProductos : { type: "string"}
							,DescripcionPerfilProductos : { type: "string"}
							,IdCategoriaInversionista : { type: "string"}
							,FechaCreacion : { type: "date"}
							,FechaModificacion : { type: "date"}
							,EstadoPerfilProductos : { type: "string"}
							,PorDefecto : { type: "string"}
							,IdEmpresa : { type: "int"}
							,IdTipoProducto : { type: "string"}
							,IdTipoOperacion : { type: "string"}
							,IdCanalIngreso : { type: "string"}
							,IdMoneda : { type: "string"}
							,EstadoProducto : { type: "string"}
						}
					}
				}
			}
		});

/*********************************************
 * 				FUNCIONES
 ********************************************/
		pPortCom.fn.preparaDatosGuardar = function(){
			var dataPortafolio = {};
			dataPortafolio.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa;
			dataPortafolio.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
			dataPortafolio.p_IdModulo 			= 'SOC';
			dataPortafolio.p_UsaReglasNegocio 	= 'N';
			dataPortafolio.p_IdPortafolio 		= pPortCom.params.IdPortafolio;
			dataPortafolio.p_RegistroCambio 	= [
  			     {CampoModificado:'IdPerfilComercialBaseOrigen'	,ValorNuevo: pPortCom.datosDespues.IdPerfilComercialBaseOrigen || '0'},
  			];
			return dataPortafolio;
		};
		
		pPortCom.fn.previsualizarMatrizOperacional = function()
		{			
			pPortCom.filtroMatriz.p_IdEmpresa = $scope.SesionIdEmpresa;
			pPortCom.filtroMatriz.p_IdTipoEntidad = 'PORTAFOLIO';
			pPortCom.filtroMatriz.p_IdEntidad = pPortCom.datosDespues.IdPortafolio;;
			pPortCom.filtroMatriz.p_IdPlantillaCobros	= "";
			pPortCom.filtroMatriz.p_IdPlantillaProductos = "";
			pPortCom.grilla.optionsGrillaMatrizOperacional.dataSource.setCallParameters(pPortCom.filtroMatriz);
			
			
		};

/*********************************************
 * 				FUNCIONES WATCH
 ********************************************/			
		$scope.$watch('pPortCom.PerfilComercialObj',function(newval,oldval){

			if(newval !== undefined){
				if(newval.IdPerfilComercialBase === pPortCom.datosAntes.IdPerfilComercialBaseOrigen && pPortCom.win.modoLectura === true){
					pPortCom.datosDespues.IdPerfilProductos 	= pPortCom.datosAntes.IdPerfilProductos;
					pPortCom.datosDespues.IdPerfilFacturacion	= pPortCom.datosAntes.IdPerfilFacturacion;
					pPortCom.win.muestraGuardar = false;
				}
				else {
					pPortCom.datosDespues.IdPerfilProductos 	= newval.IdPerfilProductos;
					pPortCom.datosDespues.IdPerfilFacturacion	= newval.IdPerfilFacturacion;
					pPortCom.win.muestraGuardar = true;
				}						
			}
		});
		
		pPortCom.cargarDatos = function(){
			pPortCom.cmbbx.oConfiguracionPerfilComercial.filtros.IdCategoriaInversionista.value = $scope.admPort.datos.IdCategoriaInversionistaDef;
			pPortCom.datosAntes = _.clone($scope.admPort.datos);
			pPortCom.datosDespues = _.clone($scope.admPort.datos);
			pPortCom.datosDespues.IdCategoriaInversionista = $scope.admPort.datos.IdCategoriaInversionistaDef;
			pPortCom.datosDespues.IdPerfilComercialBaseOrigenDef = $scope.admPort.datos.IdPerfilComercialBaseOrigen;
		};
		
		$scope.admPort.portafolioPromise.then(function(asd){
			if($scope.admPort && $scope.admPort.datos) {
				pPortCom.cargarDatos();
			}
			
			$scope.$watch('admPort.datos',function(newVal){
				if(newVal && newVal.IdPortafolio) {
					pPortCom.cargarDatos();
				}
			});

			$scope.$watch('pPortCom.AllModelosComercialesOrigen',function(newVal){
				if(newVal==true){
					$scope.pPortCom.cmbbx.oConfiguracionPerfilComercial.filtros.IdCategoriaInversionista.value = "";
				}else{
					$scope.pPortCom.cmbbx.oConfiguracionPerfilComercial.filtros.IdCategoriaInversionista.value = $scope.pPortCom.datosAntes.IdCategoriaInversionistaDef;
				}
			});
		});
		pPortCom.avisarCambioPanel = function () {
			const sonIguales = _.isEqual(pPortCom.datosAntes.IdPerfilComercialBaseOrigen, pPortCom.datosDespues.IdPerfilComercialBaseOrigen);
			if (sonIguales) {
				$scope.$emit('DatosCambiadosGuardados', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilComercial' });
			} else {
				$scope.$emit('HayDatosSinGuardar', { ambito: "PerfilComercial", nombreTab: 'PanelPortafolioPerfilComercial' });
			}
		};

		$scope.$watchCollection('pPortCom.datosDespues', function () {
			const modoEdicion = !pPortCom.win.modoLectura
			if (modoEdicion) {
				pPortCom.avisarCambioPanel();
			}
		});
	});
}());
//------------------------- FIN CONTROLLER --------------------------
