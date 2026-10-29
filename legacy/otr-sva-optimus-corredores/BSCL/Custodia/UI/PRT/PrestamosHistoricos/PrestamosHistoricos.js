/*************************************************************
 * Purpose	: Blotter de Préstamos
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 13/07/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloCustodia').controller('PrestamosHistoricosCtrl',
	function($rootScope,$scope,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA, bcsConfiguracionPRT,$modal,$state,bcsServicioPlataforma,$translate, bcsServiciosPrestamo, $http)
	{
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.winID=bcsComunes.ventanaWinId($scope);
		$scope.prestamos2 = {};
		$scope.prestamos2.emprestitos = {};
		$scope.prestamos2.anticipos = {};
		$scope.emprestitos2 = {};
		$scope.emprestitos2.prestamo = {};
	
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		//EstadoPrestamo
		$scope.oConfiguracionEstado2={
			tipo : "combobox-estado-custodia",
			configuracion : {},
			filtros : {
				NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : 'PRESTAMO' }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		//Mercado
		$scope.oConfiguracionClasificacionTipoInstrumento2 = {
				tipo : "combobox-clasificacion-tipo-instrumento",
				configuracion : {},
				filtros : {
					Rv : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : 'RV' },
					Rf : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : 'RF' },
					logic: "or"
				},
				idActual : false,
				seleccionado : false,
				idDefecto : '',
				idPostFiltro : false
		};
			
		//TipoPrestamo
		$scope.oConfiguracionTipoPrestamo2={
			tipo : "combobox-tipo-prestamo",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		$scope.oClientePortafolio2={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			seleccionado : false,
			IdActual: 0,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
	
		//cargar al inicio
		//$scope.kngFechaIngresoDesde2 = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaIngresoHasta2 = util.dateInicioDia($scope.SesionFechaSistema);
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsPrestamos = new bcs.DataSource({
	       	type : "call",
	       	definition : {
				callTransaction	: "44290889",
	  			serverFiltering : false,
	  			serverPaging 	: false,
				serverSorting 	: false,
	  			pageSize 		: 22,
				callResultSet 	: "P_OUT_PRESTAMOS_HISTORICO",
	  			schema 			: bcsConfiguracionPRT.modeloPrestamos()
	    	}
		});

		var dsEmprestitos = new bcs.DataSource({
			type : "call",
			definition : {
				callTransaction	: "44290896",
				serverFiltering : false,
				serverPaging 	: false,
				serverSorting 	: false,
				pageSize 		: 22,
				callResultSet 	: "P_OUT_EMPRESTITOS_HISTORICOS",
				schema 			: bcsConfiguracionPRT.modeloEmprestitos()
			}
		});
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		//PRESTAMOS
		$scope.prestamos2.confBasicaPrestamos = {
			dataSource			: dsPrestamos,
			columns				: bcsConfiguracionPRT.columnasPrestamosHistoricos(false),
			sortable			: true,
			pageable			: true,
			filterable 			: true,
			excel 				: { allPages : true },
			userSettings 		: true,
			allowCopy			: { delimeter: "\t" },
			height				: 470,
			selectable 			: "single row"
		};

		$scope.prestamos_emprestitos_confGridPrestamoEmprestitos2 = function (item){
			var dsEmprestitoDetalle = {
				type : "call",
	         	definition : {
		  			serverFiltering : false,
		  			serverPaging 	: false,
		  			pageSize 		: 10,
		  			filterable 		: false,
					callTransaction	: "44290896",
					callResultSet 	: "P_OUT_EMPRESTITOS_HISTORICOS",
		  			schema : bcsConfiguracionPRT.modeloEmprestitos(),
	           },
			};
			var parametros = {};
			parametros.p_IdEmpresa 			= item.IdEmpresa;
			parametros.p_FolioCartera 		= item.FolioPrestamo;
			dsEmprestitoDetalle.definition.callParameters = parametros;

			return {
				dataSource			: new bcs.DataSource(dsEmprestitoDetalle),
				columns				: bcsConfiguracionPRT.columnasEmprestitosHistoricos(false),		
				sortable			: true,
				pageable			: true,
				userSettings 		: true,
				allowCopy			: { delimeter: "\t" },
				autoBind			: true,
				selectable 			: "single row",
				groupable 			: false
			}
		};

		//EMPRESTITOS
		$scope.emprestitos2.confBasicaEmprestitos = {
			dataSource			: dsEmprestitos,
			columns				: bcsConfiguracionPRT.columnasEmprestitosHistoricos(false),
			sortable			: true,
			pageable			: true,
			userSettings 		: true,
			filterable 			: true,
			allowCopy			: { delimeter: "\t" },
			height				: 470,
			selectable 			: "single row"
		};

		$scope.emprestitos_prestamo_confGridEmprestitoPrestamo2 = function (item) {
			var dsPrestamosDetalle = {
				type : "call",
	         	definition : {
		  			serverFiltering : false,
		  			serverPaging 	: false,
		  			pageSize 		: 10,
		  			filterable 		: false,
					callTransaction	: "44290889",
					callResultSet 	: "P_OUT_PRESTAMOS_HISTORICO",
		  			schema : bcsConfiguracionPRT.modeloPrestamos(),
	           },
			};
			var parametros = {};
			parametros.p_IdEmpresa 			= item.IdEmpresa;
			parametros.p_FolioCartera 		= item.FolioPrestamo;
			dsPrestamosDetalle.definition.callParameters = parametros;

			return {
				dataSource	: new bcs.DataSource(dsPrestamosDetalle),
				columns		: bcsConfiguracionPRT.columnasPrestamosHistoricos(false),		
				sortable	: false,
				pageable	: false,
				userSettings: true,
				allowCopy	: { delimeter: "\t" },
				autoBind	: true,
				selectable 	: "single row",
				groupable 	: false	
			}
		};

		$scope.prestamos_anticipos_confGridPrestamoAnticipos2 = function (item){
			var dsDef2 = {
					type : "odata",
		            definition : {
				  		serverFiltering : true, 
				  		serverPaging : true, 
				  		pageSize : 10, 
				        transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1011",
								dataType: "json"
							}
						},
				  		schema : bcsConfiguracionPRT.modeloAnticipos(),
				  	}
			};
			dsDef2.definition.filter = [
		        { field: "IdEmpresa", operator: "eq", value: Number(item.IdEmpresa) },
		        { field: "FolioPrestamo", operator: "eq", value: Number(item.FolioPrestamo) }
		    ];
			return {
				dataSource			: new bcs.DataSource(dsDef2),
				columns				: bcsConfiguracionPRT.columnasEmprestitosAnticipos(),		
				sortable			: true,
				pageable			: true,
				userSettings 		: true,
				allowCopy			: { delimeter: "\t" },
				autoBind			: true,
				selectable 			: "single row",
				groupable 			: false
			}
		};

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.prestamos2.emprestitos.btnIngresarDetalle = function(){
			if ($scope.prestamos2.selPrestamos == undefined){
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'));
				return;
			}
			var id1 = bcsComunes.ventanaAbrir("CrearEmprestitoModal",$scope.prestamos2.selPrestamos,true,true); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) {
	        		dsPrestamos.read();
	        		dsEmprestitos.read();
				}
	    	}); 
		};
		
		$scope.prestamos2.emprestitos.btnAnticiparDetalle = function(){
			var id1 = bcsComunes.ventanaAbrir("AnticiparEmprestitoModal",$scope.prestamos2.emprestitos.selPrestamoEmprestitos,true,true); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) {
	        		dsEmprestitos.read();
				}
	    	}); 
		};
		
		$scope.prestamos2.emprestitos.btnAnularDetalle = function(){
			bcsComunes.msg("warning","info",'Pendiente de desarrollo'); return;
			var id1 = bcsComunes.ventanaAbrir("AnularEmprestitoModal",$scope.prestamos2.emprestitos.selPrestamoEmprestitos,true,true); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) {
	        		dsEmprestitos.read();
				}
	    	}); 
		};
	
		
		$scope.btnBuscarHis = function() {
			buscarHis();
		};
		
		buscarHis = function() {
			var filtros 	= {
				p_IdEmpresa: 1,
				p_IdUsuario: $scope.SesionIdUsuario
			};
			
			try { // se mete dentro de un try ya q cuando no se ingresa un valor al objeto, este no se lee
	    		
	        	if ($scope.folioPrestamo2 !== undefined && $scope.folioPrestamo2 !=='' && $scope.folioPrestamo2 !==0){
					if (Number($scope.folioPrestamo2)){
						var auxNum = Number($scope.folioPrestamo2);
						seleccOk = true;
						filtros.p_FolioCartera = auxNum;
					} else {
						filtros.p_FolioCartera = Number($scope.folioPrestamo2.replace(/\./g, ""));
					}
	        	}
	    	} catch(e) {
	    	}
			
			
			if ($scope.kngFechaIngresoDesde2 != null) {
				filtros.p_FechaConsultaDesde = util.dateSPdate($scope.kngFechaIngresoDesde2);
			}
			if ($scope.kngFechaIngresoHasta2 != null) {
				filtros.p_FechaConsultaHasta = util.dateSPdate($scope.kngFechaIngresoHasta2);
			}	
			if ($scope.oConfiguracionEstado2.idActual !== 0){
				if ($scope.oConfiguracionEstado2.seleccionado.IdEstado == 'NAS'){
					filtros.p_CantidadPorAsignar = 0;
				} else {
					filtros.p_IdEstado = $scope.oConfiguracionEstado2.seleccionado.IdEstado;
				}
			}
			if ($scope.oConfiguracionTipoPrestamo2.idActual !== 0){
				filtros.p_IdTipoPrestamo = $scope.oConfiguracionTipoPrestamo2.seleccionado.IdTipoPrestamo;
			}
			if ($scope.oClientePortafolio2.seleccionado.IdPersona){
				filtros.p_IdCliente = $scope.oClientePortafolio2.seleccionado.IdCliente;
			}
			if ($scope.oClientePortafolio2.seleccionado.IdPortafolio){
				filtros.p_IdPortafolio = $scope.oClientePortafolio2.seleccionado.IdPortafolio;
			}

			dsPrestamos.setCallParameters(filtros);
			dsEmprestitos.setCallParameters(filtros);
		};
		
		$scope.gridSelecionada = "";
		
		$scope.select2 = function(grid)
		{
			$scope.gridSelecionada = grid;
			util.refreshGridSize(grid, 50);
		};

		$scope.exportarExcel = function(){

			console.log($scope.gridSelecionada);
			if($scope.gridSelecionada==="grid-prestamos2")
			{
				$scope.prestamos2.exportarExcel();			           
			}
			else if($scope.gridSelecionada==="grid-emprestitos2")
			{
				$scope.emprestitos2.exportarExcel();			
			}
		};
		


		$scope.btnLimpiarHis = function() {
			$scope.oConfiguracionEstado2.idDefecto 		= 0;
			$scope.oConfiguracionEstado2.idActual 		= 0;
			$scope.oConfiguracionTipoPrestamo2.idDefecto = 0;
			$scope.oConfiguracionTipoPrestamo2.idActual 	= 0;
			$scope.filtros.IdPortafolio = false;
			$scope.oClientePortafolio2.seleccionado = false;
			$scope.kngFechaIngresoDesde2 	= util.dateInicioDia($scope.SesionFechaSistema);
			$scope.kngFechaIngresoHasta2 	= util.dateInicioDia($scope.SesionFechaSistema);
			dsPrestamos.removeAll();
			dsEmprestitos.removeAll();
		};
		
		$scope.select = function(grid)
		{
			$timeout(function(){
				if ($("#" + grid).find("[kendo-grid]").data("kendo-grid")){
					$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
				}	
			},100);
			if (grid === 'grid-prestamos2') $scope.botonDisabled = true;
			if (grid === 'grid-emprestitos2') $scope.botonDisabled = false;
		};
		
		$scope.btnIngresar2 = function(){
			var id1 = bcsComunes.ventanaAbrir("CrearPrestamoModal",{Accion: 'CREAR'},true,true); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) buscar();
	    	}); 
		};
		
		var successAnticiparPrestamo2 = function(respuesta) {
	   		bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_ANTICIPADA_EXITO"));
				buscar();
			}
			else bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANTICIPADA_ERROR"));
		};
		
		var errorAnticiparPrestamo2 = function(respuesta) {
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANTICIPADA_ERROR"));
		};
		
		$scope.btnAnticipar2 = function(){
			if(!$scope.prestamos2.selPrestamos) return;
			var mensajeAnticipoPrestamo = ($scope.prestamos2.selPrestamos.CobraPrimaAlInicio === 'S')?"UI_MENSAJE_ANTICIPO_PRESTAMO":"UI_MENSAJE_ANTICIPO_PRESTAMO_COBRA_PRIMA";
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant(mensajeAnticipoPrestamo));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR) fnAnticipar2();			
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			});
			return;
		};
	
		var fnAnticipar2 = function(){
			var datos = {};	
			bcsComunes.ventanaOverlay($scope.winID,true); 
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'CUS';
			datos.p_UsaReglasNegocio = 'S';
			datos.p_IdFolio = Number($scope.prestamos2.selPrestamos.FolioCartera) || 0;
			datos.p_Fecha = util.dateSPdate(util.date($scope.SesionFechaSistema).toDate()) || '1900-01-01';
			datos.p_IdPais = $scope.SesionIdPaisSistema;
			datos.p_IdCondicionLiquidacion = 'T0'; //TODO REVISAR DESPUES CUAL ES EL POR DEFECTO
			datos.p_IdMonedaLiquidacion = $scope.SesionIdMonedaSistema || '';
			datos.p_FechaCondicionLiquidacion = util.dateSPdate(util.date($scope.SesionFechaSistema).toDate()) || '1900-01-01';
			bcsServiciosPrestamo.AnticiparPrestamo(datos,successAnticiparPrestamo2,errorAnticiparPrestamo2);	
		};
		
		var successAnularPrestamo2 = function(respuesta)
		{
	   		bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				bcsComunes.msg('info',$translate.instant("UI_INFORMACION"), $translate.instant("UI_OPERACION_ANULADA_EXITO"));
				buscar();
			}
			else bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULADA_ERROR"));
		};
		
		var errorAnularPrestamo2 = function(respuesta) {
			bcsComunes.ventanaOverlay($scope.winID,false);
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_OPERACION_ANULADA_ERROR"));
		};
		
		$scope.btnAnular2 = function(){
			bcsComunes.msg("warning","info",'Pendiente de desarrollo');
			return;
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_MENSAJE_ANULAR_PRESTAMO"));
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR) fnAnular2();
				else bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			});
			return;
		};
		
		var fnAnular2 = function(){
			var datos = {};	
			bcsComunes.ventanaOverlay($scope.winID,true); 
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'CUS';
			datos.p_UsaReglasNegocio = 'S';
			datos.p_FolioCarteraPrestamo = Number($scope.prestamos2.selPrestamos.FolioCartera) || 0;
			datos.p_FechaAnulacion = util.dateSPdate(util.date($scope.SesionFechaSistema).toDate())||'1900-01-01';
			bcsServiciosPrestamo.AnularPrestamo(datos,successAnularPrestamo2,errorAnularPrestamo2);				
		};
		
		$scope.prestamos2.btnRenovar = function renovarPretamo() {
			console.log('prestamo seleccionado: ', $scope.prestamos2.selPrestamos);
			
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}

			if (Number($scope.prestamos2.selPrestamos.PlazoDisponibleRenovacion) < Number($scope.prestamos2.selPrestamos.MinimoDiasPrestamo)) {
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), util.translate($translate, 'UI_NO_DISPONE_PLAZO_SUFICIENTE_RENOVACION', { FolioCartera: $scope.prestamos2.selPrestamos.FolioPrestamo }));
			}
			
			if ($scope.prestamos2.selPrestamos.CantidadPorAsignar > 0) {
				bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_PRESTAMO_SIN_ASIGNACION_PRESTAMISTA"));
				return;
			}

			const idVentana = bcsComunes.ventanaAbrir('RenovarPrestamoModal', $scope.prestamos2.selPrestamos, false, false, $scope.winID);
			bcsComunes.ventanaPromesa(idVentana)
				.then(function (success) {
					if (success) {
						buscarHis();
					}
				});

		};
		
		$scope.prestamos2.esPrestamoVencido = function esPrestamoVencido(prestamo) {
			if (!$scope || !$scope.SesionFechaSistema || !prestamo.FechaVencimientoPrestamo) {
				return false;
			}
			return prestamo && (prestamo.IdEstado === 'VCD');
		};
		
		$scope.prestamos2.esPrestamoRenovable = function esPrestamoRenovable(prestamo) { 
			return prestamo
					&& prestamo.EsRenovable === 'S'
					&& $scope.prestamos2.esPrestamoVencido(prestamo)
		};

		//-----------------------------------------------------------
		// OBTENER CONFIGURACION DE REPORTE (Anexo 2) 
		//-----------------------------------------------------------
		var dsTemplateCustodia = new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {			
				serverFiltering 	: true,
				transport 			: {
										read : {
											url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=4171",   //BSCL.Tesoreria.BD.Vistas::ConsultarTemplateTesoreria 
											dataType : "json",
										}
									},
				filter				: [
						          		{ field: "IdTemplateCustodia", operator: "eq", value: 'ANEXO2' }, //Reporte Carta Transferencias 
						          		{ field: "IdEstado",  operator: "eq", value: 'ACT' },
						          		{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
						            ],		
				schema 				: {
					model: {
						fields : {
							IdReporte       : {type: "string"},
							IdServicio	    : {type: "number"},
							NombreReporte   : {type: "string"},
							FormatoSalida	: {type: "string"},
						}
					}
				}
									
			}, 
		});
	 	
		dsTemplateCustodia.fetch().then(function(){
			 if (dsTemplateCustodia.data().length > 0 ){
				 $scope.$apply(function() {				  
					 $.each(dsTemplateCustodia.data(), function(key, registro){
						 $scope.ReporteAnexo2 = {
								 IdReporte      : registro.IdReporte,
								 NombreReporte	: registro.NombreReporte,
								 IdServicio 	: registro.IdServicio,
								 FormatoSalida  : registro.FormatoSalida
						 };
						 
					 });				 
					return true;
				 });
				 
			 }
			 else {
				 return false;
			 }
		});	
		
		//-----------------------------------------------------------
		// GENERAR REPORTE (Carta de transferencias bancarias) 
		//-----------------------------------------------------------		
		$scope.prestamos2.emprestitos.anexo2 = function(){
			$scope.generarAnexo2('EMP');			
		};

		$scope.generarAnexo2 = function(cual){
			let mensajes = {
				selccionarPrestamo: $translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'),
				reporteSinConfiguracion: $translate.instant('UI_REPORTE_SIN_CONFIGURACION'),
				errorAlGenerar: $translate.instant('UI_ERROR_AL_GENERAR')
			};
			let v_prestamo = 0, v_empresti = 0;

			if (cual === 'PRT') {
				if (!$scope.prestamos2.selPrestamos && !$scope.prestamos2.emprestitos.selPrestamoEmprestitos) {
					bcsComunes.msg("error", "info", mensajes.selccionarPrestamo);
					return;
				}
				v_prestamo = $scope.prestamos2.selPrestamos.FolioPrestamo;
			} else {
				if (!$scope.prestamos2.emprestitos.selPrestamoEmprestitos) {
					bcsComunes.msg("error", "info", mensajes.selccionarPrestamo);
					return;
				}
				v_empresti = $scope.prestamos2.emprestitos.selPrestamoEmprestitos.FolioEmprestito;
				v_prestamo = $scope.prestamos2.emprestitos.selPrestamoEmprestitos.FolioPrestamo;
			}

			const reporte = $scope.ReporteAnexo2;
			if (!reporte.IdReporte || !reporte.NombreReporte || !reporte.IdServicio || !reporte.FormatoSalida) {
				bcsComunes.ventanaMsg({mensaje: mensajes.reporteSinConfiguracion}, $scope.winID);
				return;
			}
			
			bcsComunes.ventanaOverlay($scope.winID, true);
			const fecha = new Date();
			//Nombre de carta para descarga local
			const nombreCartaDescarga = `${reporte.NombreReporte}_${fecha.getFullYear()}${fecha.getMonth() + 1}${fecha.getDate()}_${fecha.getHours()}${fecha.getMinutes()}${fecha.getSeconds()}.pdf`;
			
			let parameters = {
				"reportId": $scope.ReporteAnexo2.IdReporte,						
				"parameters": [],
				"exportFormat": $scope.ReporteAnexo2.FormatoSalida,
				"configuration": {
					"mainReport": {								
						"reportName": $scope.ReporteAnexo2.NombreReporte,
						"parameters": [],
							"data" : {
								"serviceId" : $scope.ReporteAnexo2.IdServicio,
								"serviceData" : {								                      
													"p_IdEmpresa"		        : $scope.SesionIdEmpresa,
													"p_FolioPrestamo" 			: v_prestamo,
													"p_FolioEmprestito"			: v_empresti,
													"p_ConsultaHistorica"		: 1,  // 1 Para consultar las tablas historicas 0 para tablas vigentes
													"$schema"					: [{ "field" : "LogoCorredora", "type" : "blob" }]
												}
							},
						"parameters": {},
					},
					"subReports": []
				},
			};
			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, { "responseType": "arraybuffer" })
				.success(function (response) {
					const file = new Blob([response], { type: 'application/pdf' });
					saveAs(file, nombreCartaDescarga);
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			).error(function (response) {
				bcsComunes.ventanaMsg({mensaje: mensajes.errorAlGenerar}, $scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			});
			return;
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------