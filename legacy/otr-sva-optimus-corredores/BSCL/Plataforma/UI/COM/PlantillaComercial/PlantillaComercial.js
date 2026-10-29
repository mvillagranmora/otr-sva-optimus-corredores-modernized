/*************************************************************
 * Purpose	: Plantilla Comercial.
 * Author	: JG - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('PlantillaComercialCtrl',
	function($scope,$q,$translate,bcsComunes,$timeout,$state,bcsConfiguracionMatrizPerfilComercial,$resource,bcsAlerta,servicioPerfilComercial,$compile,bcsPerfilComercial){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
			
		var pCom = this;
		pCom.ds 			= {};
		pCom.grilla 		= {};
		pCom.cmbbx 			= {};
		pCom.win 			= {};
		pCom.fn				= {};
		pCom.ds				= {};
		pCom.busq 			= {};
		pCom.datosAntes 	= {};
		pCom.datosDespues	= {};

		pCom.win.idVentana 	= bcsComunes.ventanaWinId($scope);
		pCom.win.parametros = bcsComunes.ventanaParams(pCom.win.idVentana);
		pCom.win.domVentana = $("[ventanaid='"+pCom.win.idVentana+"']");
		
		pCom.win.modoLectura 	= true;
		pCom.win.modoEditar 	= false; 
		pCom.win.datosCargados 	= false;
			
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/

		pCom.win.editar = function() {
			pCom.win.modoLectura = false;
			pCom.win.modoEditar = true;
			//pCom.win.datosCargados = false;
		};

		pCom.win.crear = function() {
			pCom.fn.limpiarPantalla();
			pCom.cmbbx.perfilProductos.filtros.read = true;
			pCom.cmbbx.perfilCobros.filtros.read = true;
			pCom.cmbbx.perfilCobrosPeru.filtros.read = true;
			pCom.cmbbx.perfilLimites.filtros.read = true;
			pCom.datosDespues.IdEstado = true;
			pCom.win.modoLectura = false;
			pCom.win.modoEditar = false;
			pCom.win.datosCargados = false;
		};

		pCom.win.guardar = function () {
			const perfilComercialBase = pCom.fn.datosGuardar();

			bcsComunes.ventanaOverlay(pCom.win.idVentana, true);

			// Actualizar
			if (pCom.win.modoEditar) {
				servicioPerfilComercial.updatePerfilComercial(perfilComercialBase,
					function (result) {
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
							pCom.fn.postGuardar();
						}

						bcsComunes.ventanaMsg({
							mensaje: "Operación exitosa",
							conflictos: util.parseServerResponse(result)
						}, pCom.win.idVentana);

						bcsComunes.ventanaOverlay(pCom.win.idVentana, false);
					}, function (result) {


						bcsComunes.ventanaMsg({
							mensaje: "Error en el Ingreso",
							conflictos: util.parseServerResponse(result)
						}, pCom.win.idVentana);

						console.log(JSON.stringify(result));

						bcsComunes.ventanaOverlay(pCom.win.idVentana, false);
					}
				);
			} else {
				// Crear
				servicioPerfilComercial.savePerfilComercial(perfilComercialBase,
					function (result) {
						if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0) {
							pCom.fn.postGuardar();
						}

						bcsComunes.ventanaMsg({
							mensaje: "Operación exitosa",
							conflictos: util.parseServerResponse(result)
						}, pCom.win.idVentana);

						bcsComunes.ventanaOverlay(pCom.win.idVentana, false);
					}, function (result) {
						bcsComunes.ventanaMsg({
							mensaje: 'Error en el Ingreso ' + result.data.CODIGO + ' : ' + result.data.PILA,
							conflictos: util.parseServerResponse(result)
						}, pCom.win.idVentana);

						console.log(JSON.stringify(result));

						bcsComunes.ventanaOverlay(pCom.win.idVentana, false);
					}
				);
			}		
		};

		pCom.win.cancelar = function() {
			pCom.win.modoLectura = true;
			pCom.win.modoEditar  = false;
			//pCom.datosDespues = _.clone(pCom.datosAntes);	
			pCom.fn.limpiarPantalla();
		};
		
		pCom.win.cargarDatos = function(perfilComercial) {
			if(perfilComercial && perfilComercial.IdPerfilComercialBase){
				pCom.datosAntes 			= _.clone(perfilComercial);
				pCom.datosAntes.PorDefecto 	= perfilComercial.PorDefecto === 'S';
				pCom.datosAntes.IdEstado 	= perfilComercial.IdEstado === 'ACT';
				pCom.datosAntes.TipoEnrolamiento = perfilComercial.TipoEnrolamiento || 'FULL';
				pCom.datosDespues 			= _.clone(pCom.datosAntes);
				if ($scope.SesionLocale === 'CL') {
					pCom.filtroMatriz.p_IdEntidad 				= perfilComercial.IdPerfilComercialBase;		
					pCom.filtroMatriz.p_IdTipoEntidad 			='MODELO';
					pCom.filtroMatriz.p_IdPlantillaCobros 		= '';
					pCom.filtroMatriz.p_IdPlantillaCobrosPeru	= '';
					pCom.filtroMatriz.p_IdPlantillaProductos 	= '';
					pCom.grilla.matrizOperacional.dataSource.setCallParameters(pCom.filtroMatriz);
					//pCom.grilla.matrizOperacional.dataSource.read();
				}
				pCom.win.datosCargados 	= true;
				
				pCom.win.editar();
			}
			else { bcsComunes.msg("warning","Cargar datos","No ha seleccionado un dato."); }
		};
		
		pCom.win.verMatrizOperacional = function() {
			if($scope.SesionLocale === 'CL' && pCom.datosDespues && pCom.datosDespues.IdPerfilProductos && pCom.datosDespues.IdPerfilCobros){
				pCom.filtroMatriz.p_IdEntidad 				= '';	
				pCom.filtroMatriz.p_IdTipoEntidad 			= 'PREVISUALIZAR';
				pCom.filtroMatriz.p_IdPlantillaCobros 		= pCom.datosDespues.IdPerfilCobros;
				pCom.filtroMatriz.p_IdPlantillaProductos	= pCom.datosDespues.IdPerfilProductos;
				pCom.grilla.matrizOperacional.dataSource.setCallParameters(pCom.filtroMatriz);
			}	
			else if($scope.SesionLocale === 'PE' && pCom.datosDespues && pCom.datosDespues.IdPerfilProductos && pCom.datosDespues.IdPerfilCobrosPeru){
				pCom.filtroMatriz.p_IdEntidad 				= '';	
				pCom.filtroMatriz.p_IdTipoEntidad 			= 'PREVISUALIZAR';
				pCom.filtroMatriz.p_IdPlantillaCobros 		= pCom.datosDespues.IdPerfilCobrosPeru;
				pCom.filtroMatriz.p_IdPlantillaProductos	= pCom.datosDespues.IdPerfilProductos;
				pCom.grilla.matrizOperacional.dataSource.setCallParameters(pCom.filtroMatriz);
			}	
			else
				bcsComunes.msg("warning","Matriz Operacional","Seleccione Plantilla de Productos y Cobros");
		};
		
/*********************************************
 * DATASOURCES
 *********************************************/	


/*********************************************
 * COMBOBOX
 *********************************************/

		pCom.cmbbx.perfilComercial={
			filtros : { 
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			},
		};
	
		pCom.cmbbx.categoriaInversionista={
			filtros : { 
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			},
		};
		
		pCom.cmbbx.perfilProductos={
			filtros : {
				EsPerfilPersonalizado 		: { field: "EsPerfilPersonalizado",  operator: "eq", value: "N" },
				IdCategoriaInversionista 	: { field : "IdCategoriaInversionista", operator: "eq", value : "" },
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa },
				read : false,
			}
		};

		pCom.cmbbx.perfilCobros={
			filtros : {
				EsPerfilPersonalizado 		: {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa },
				filtros 		: false
			} 
		};
		
		pCom.cmbbx.perfilCobrosPeru = {
			filtros : {
				EsPerfilPersonalizado 		: {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa },
				filtros 		: false
			} 
		};

		pCom.cmbbx.modeloFacturacion={
			filtros : {
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			},
		};

		pCom.cmbbx.plantillaCondicionesOperativas={
			filtros : {
				EsPerfilPersonalizado 		: {field: "EsPerfilPersonalizado", operator: "eq", value: "N"},
				IdCategoriaInversionista 	: { field : "IdCategoriaInversionista", operator: "eq", value: ""},
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			} 
		};
		
		pCom.cmbbx.perfilLimites={
			filtros : {
				EsPerfilPersonalizado 		: { field: "EsPerfilPersonalizado",  operator: "eq", value: "N" },
				IdCategoriaInversionista 	: {field : "IdCategoriaInversionista", operator: "eq", value : "" },
				IdEmpresa 		: { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa },
				filtros 		: false
			} 
		};
		
/*********************************************
 * GRILLAS
 *********************************************/
		pCom.filtroMatriz = { 
				p_IdEmpresa : $scope.$root.SesionIdEmpresa, 
				p_IdTipoEntidad			:'MODELO', 
				p_IdEntidad 			:'0',
				p_IdPlantillaCobros		: '',
				p_IdPlantillaProductos	: ''
		};		
		
		pCom.grilla.matrizOperacional = {
				dataSource: new bcs.DataSource({
					type: "call",
					definition 	: {
						serverFiltering : true, 
						serverPaging 	: false,
						serverSorting 	: true,
						pageSize		: 200,
						callParameters	: pCom.filtroMatriz, 					
						//callTransaction	: "305",
						callTransaction	: 20052,
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
									UsoMonedaFormato 	: {type : "string"}
									
									
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
				columns: [],	
				sortable	: false,
				groupable	: false,
				pageable	: false,
				autoBind	: false,
				height		: 240,
				userSettings		: true,
				navigatable			: true,
				excel : {
					allPages: false,
				},
			    selectable	: "row"
			};
		
		if ($scope.SesionLocale === 'CL') {
			pCom.grilla.matrizOperacional.columns = [
				{field: "NombreBolsa",				width: "120px",title: "UI_BOLSA",hidden :    true},
				{field: "NombreTipoProducto",		width: "120px",title: "UI_TIPO_PRODUCTO",hidden :    true},
				{field: "NombreTipoOperacion",		width: "120px",title: "UI_TIPO_OPERACION",hidden :    true},
				{field: "NombreTipoCanalIngreso",	width: "120px",title: "UI_CANAL_INGRESO",hidden :    true},
				{field: "CodigoCobroClasificacion",	width: "120px",title: "UI_CLASIFICACION"},
				{field: "IdCobro",					width: "120px",title: "UI_COBRO"},
				{field: "ValorCobro",				width: "120px",title: "UI_VALOR",template: kendo.template("#if (ValorCobro === null) { ##}  else  {if (FormaCobro==='V') {##=ValorCobro# %#} else {##=ValorCobro##}}#")},
				{field: "ValorTopeMinimoOrden",		width: "120px",title: "UI_MINIMO",template: kendo.template("#if (ValorCobro === null) { ##} else { if (IdMoneda==='UF') {#UF #=ValorTopeMinimoOrden##} else {##=ValorTopeMinimoOrden##}}#")},
				{field: "IdMoneda",					width: "100px",title: "UI_MONEDA"}
	
			];
		}
		
		
		
		if ($scope.SesionLocale === 'PE') {
			pCom.grilla.matrizOperacional.columns = [
				{field: "NombreBolsa",					width: "120px",title: $translate.instant("UI_BOLSA"), hidden : true},
				{field: "NombreTipoProducto",			width: "120px",title: $translate.instant("UI_TIPO_PRODUCTO"), hidden : true},
				{field: "NombreTipoOperacion",			width: "120px",title: $translate.instant("UI_TIPO_OPERACION"), hidden : true},
				{field: "NombreTipoCanalIngreso",		width: "120px",title: $translate.instant("UI_CANAL"), hidden : true},
				{field: "CodigoDefinicionCobro",		width: "100px",title: $translate.instant("UI_COBRO")},
				{field: "NombreClaseCobro",				width: "100px",title: $translate.instant("UI_CLASE_COBRO")},
				{field: "NombreTipoCobro",				width: "100px",title: $translate.instant("UI_TIPO_COBRO")},
				{field: "ValorCobroCombinado",			width: "100px",title: $translate.instant("UI_VALOR"),template : " <label> #:  (util.bcs_formato(ValorCobroCombinado,'GEN',UsoMonedaFormato,IdMonedaCobroUI, '','')).valor #</label>"},
				{field: "ValorCobroMonedaPorcentaje",	width: "100px",title: $translate.instant("UI_MONEDA_PORCENTAJE")},
	
			];
		}
		
		
		
/*********************************************
 * FUNCIONES
 ********************************************/
		
		pCom.fn.datosGuardar = function obtenerPerfilComercialBase(){
			return {
				p_IdEmpresa: $scope.$root.SesionIdEmpresa,
				p_IdUsuario: $scope.$root.SesionIdUsuario,
				p_IdModulo: "PLA",
				p_simularOperacion: "N",
				p_UsaReglasNegocio: "N",
				p_IdPerfilComercialBase: pCom.datosDespues.IdPerfilComercialBase,
				p_DescripcionPerfilComercialBase: pCom.datosDespues.DescripcionPerfilComercialBase,
				p_IdCategoriaInversionista: pCom.datosDespues.IdCategoriaInversionista,
				p_IdEstado: pCom.datosDespues.IdEstado ? 'ACT' : 'INA',
				p_PorDefecto: pCom.datosDespues.PorDefecto ? 'S' : 'N',
				p_IdPerfilProductos: pCom.datosDespues.IdPerfilProductos,
				p_IdPerfilCobros: pCom.datosDespues.IdPerfilCobros,
				p_IdPerfilCobrosPeru: pCom.datosDespues.IdPerfilCobrosPeru,
				p_IdPerfilLimites: pCom.datosDespues.IdPerfilLimites,
				p_IdModeloFacturacion: '0',
				p_IdPerfilCondicionesOperativas: '0',
				p_TipoEnrolamiento: pCom.datosDespues.TipoEnrolamiento
			}
		};
		
		pCom.fn.postGuardar = function(){
			pCom.win.modoLectura 	= true;
			pCom.win.modoEditar 	= false; 
			pCom.win.datosCargados 	= true;
			pCom.cmbbx.perfilComercial.filtros.read = true;
			pCom.busq.IdPerfilComercialDef 	= pCom.datosDespues.IdPerfilComercialBase;
			pCom.busq.IdPerfilComercial		= pCom.datosDespues.IdPerfilComercialBase;
		};

		pCom.fn.limpiarPantalla = function(){
			pCom.datosAntes 	= {};
			pCom.datosDespues 	= {};
			pCom.datosDespues.IdCategoriaInversionista = '';
			pCom.datosDespues.IdPerfilProductos = '';
			pCom.datosDespues.IdPerfilCobros = '';
			pCom.datosDespues.IdPerfilCobrosPeru = '';
			pCom.datosDespues.IdPerfilLimites = '';
			pCom.grilla.matrizOperacional.dataSource.removeAll();
		};
		
		pCom.fn.exportarPlantillaCorredora = function(){
			var exceldataSource =  new bcs.DataSource({
				type: "call",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: false,
					serverSorting 	: true,
					pageSize		: 200,
					callParameters	: {	p_IdEmpresa : $scope.$root.SesionIdEmpresa}, 					
					callTransaction	: "1706",
					callResultSet	: "P_OUT_MATRIZOPERACIONAL",
					schema: {
						model: {
							fields: {
								DescripcionPerfilComercialBase : {type: "string"},
								IdCategoriaInversionista : {type:"string"},
								IdTipoProducto			: { type: "string"},
								IdTipoOperacion			: { type: "string"},
								CobroIdTipoOperacion	: { type: "string"},
								IdCanalIngreso			: { type: "string"},
								CobroIdCanalIngreso		: { type: "string"},
								IdCobro					: { type: "string"},
								ValorCobro				: { type: "number"},
								FormaCobro				: { type: "string"},
								ValorTopeMinimoOrden	: { type: "number"},
								IdMoneda				: { type: "string"},
								Prioridad				: { type: "number"},
								DiasPlazo				: { type: "number"},
								NombreCobro				: { type: "string"},
								IdCobroClasificacion	: { type: "number"},
								CodigoCobroClasificacion: { type: "string"},
								IdEmpresa				: { type: "number"},
							}
						}
					},
				}
			});
			
			columns = [
						{field: "DescripcionPerfilComercialBase",title: $translate.instant("UI_MODELO")},
						{field: "IdCategoriaInversionista",title: $translate.instant("UI_CATEGORIA_INVERSIONISTA")},
						{field: "TipoProducto",width: "0px",title: $translate.instant("UI_PRODUCTO"),attributes : {style : "text-align:left;"}},
						{field: "TipoOperacion",width: "0px",title: $translate.instant("UI_TIPO_OPERACION"),attributes : {style : "text-align:left;"}},
						{field: "CanalIngreso",width: "0px",title: $translate.instant("UI_CANAL"), attributes: { style : "text-align:left;" }},
						{field: "ValorCobro",width: "120px",title: $translate.instant("UI_VALOR"),attributes : {style : "text-align:left;"},template: kendo.template("#if (FormaCobro==='V') {##=ValorCobro# %#} else {##=ValorCobro##}#")},
						{field: "CodigoCobroClasificacion",width: "120px",title: $translate.instant("UI_CLASIFICACION"),attributes : {style : "text-align:left;"}},
						{field: "IdCobro",width: "120px",title: $translate.instant("UI_COBRO"),attributes : {style : "text-align:left;"}},
						{field: "Prioridad",width: "100px",title: $translate.instant("UI_PRIORIDAD"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
						{field: "ValorTopeMinimoOrden",width: "120px",title: $translate.instant("UI_MINIMO"),format:"{0:n2}",attributes : {style : "text-align:left;"},template: kendo.template("#if (IdMoneda==='UF') {#UF #=ValorTopeMinimoOrden##} else {##=ValorTopeMinimoOrden##}#")},
						{field: "IdMoneda",width: "100px",title: $translate.instant("UI_MONEDA"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
						{field: "DiasPlazo",width: "100px",title: $translate.instant("UI_PLAZO"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
						//falta nombre planilla limite
					];
			
			data = _.map(columns,function(element, index){
				return { field: element.field, title: element.title};
			});
			
			
			var title = "PlantillasCorredora";
			
			exceldataSource.fetch().then(function(result) {
				if (exceldataSource.data().length === 0) {
					bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_DATOS");
					return;
				}
				var nombre = "PlantillasCorredora";
				exceldataSource.exportToExcel(data,title,nombre);
			},function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};
		
		pCom.fn.exportarPlantillaPortafolios = function(){
			var exceldataSource =  new bcs.DataSource({
				type: "call",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: false,
					serverSorting 	: true,
					pageSize		: 200,
					callParameters	: {	p_IdEmpresa : $scope.$root.SesionIdEmpresa}, 					
					callTransaction	: "1709",
					callResultSet	: "P_OUT_MATRIZOPERACIONAL",
					schema: {
						model: {
							fields: {
								IdPortafolio : {type: "string"},
								IdCategoriaInversionista : {type:"string"},
								IdTipoProducto			: { type: "string"},
								IdTipoOperacion			: { type: "string"},
								CobroIdTipoOperacion	: { type: "string"},
								IdCanalIngreso			: { type: "string"},
								CobroIdCanalIngreso		: { type: "string"},
								IdCobro					: { type: "string"},
								ValorCobro				: { type: "number"},
								FormaCobro				: { type: "string"},
								ValorTopeMinimoOrden	: { type: "number"},
								IdMoneda				: { type: "string"},
								Prioridad				: { type: "number"},
								DiasPlazo				: { type: "number"},
								NombreCobro				: { type: "string"},
								IdCobroClasificacion	: { type: "number"},
								CodigoCobroClasificacion: { type: "string"},
								IdEmpresa				: { type: "number"},
							}
						}
					},
				}
			});
			
			columns = [
						{field: "NombreCompleto",title: $translate.instant("UI_NOMBRE_CLIENTE")},
						{field: "DocumentoIdentidad",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD")},
						{field: "CodigoPortafolio",title: $translate.instant("UI_CODIGO_PORTAFOLIO")},
						{field: "IdCategoriaInversionista",title: $translate.instant("UI_CATEGORIA_INVERSIONISTA")},
						{field: "TipoProducto",width: "0px",title: $translate.instant("UI_PRODUCTO"),attributes : {style : "text-align:left;"}},
						{field: "EsExtraProductos",width: "120px",title: $translate.instant("UI_EXTRA_COBRO")},//es extra
						{field: "TipoOperacion",width: "0px",title: $translate.instant("UI_TIPO_OPERACION"),attributes : {style : "text-align:left;"}},
						{field: "CanalIngreso",width: "0px",title: $translate.instant("UI_CANAL"), attributes: { style : "text-align:left;" }},
						{field: "NombreCobro",width: "0px",title: $translate.instant("UI_COBRO"),},//
						{field: "ValorCobro",width: "120px",title: $translate.instant("UI_VALOR"),attributes : {style : "text-align:left;"},template: kendo.template("#if (FormaCobro==='V') {##=ValorCobro# %#} else {##=ValorCobro##}#")},
						{field: "EsExtraCobros",width: "120px",title: $translate.instant("UI_EXTRA_COBRO")},
						{field: "CodigoCobroClasificacion",width: "120px",title: $translate.instant("UI_CLASIFICACION"),attributes : {style : "text-align:left;"}},
						{field: "IdBolsa",width: "120px",title: $translate.instant("UI_BOLSA"),attributes : {style : "text-align:left;"}},
						{field: "IdCobro",width: "120px",title: $translate.instant("UI_COBRO"),attributes : {style : "text-align:left;"}},
						{field: "Prioridad",width: "100px",title: $translate.instant("UI_PRIORIDAD"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
						{field: "ValorTopeMinimoOrden",width: "120px",title: $translate.instant("UI_MINIMO"),format:"{0:n2}",attributes : {style : "text-align:left;"},template: kendo.template("#if (IdMoneda==='UF') {#UF #=ValorTopeMinimoOrden##} else {##=ValorTopeMinimoOrden##}#")},
						{field: "IdMoneda",width: "100px",title: $translate.instant("UI_MONEDA"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
						{field: "DiasPlazo",width: "100px",title: $translate.instant("UI_PLAZO"),format:"{0:n0}",attributes : {style : "text-align:left;"}},
						//limite
					];
			
			data = _.map(columns,function(element, index){
				return { field: element.field, title: element.title};
			});
			
			
			var title = "PlantillasClientes";
			
			exceldataSource.fetch().then(function(result) {
				if (exceldataSource.data().length === 0) {
					bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_DATOS");
					return;
				}
				var nombre = "PlantillasClientes";
				exceldataSource.exportToExcel(data,title,nombre);
			},function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};

	});
}());
//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------