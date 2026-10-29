/*************************************************************
 * Purpose	: Blotter de Préstamos
 * Author	: TEAM CUSTODIA - BCS
 * Date		: 13/07/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloCustodia').controller('PrestamosVigentesCtrl',
	function(	$rootScope,$scope,$timeout,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsConfiguracionPRT,$modal,$state,bcsServicioPlataforma,$translate,bcsServiciosPrestamo,$resource,$http)
	{
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		$scope.winID=bcsComunes.ventanaWinId($scope);
		$scope.prestamos = {};
		$scope.prestamos.emprestitos = {};
		$scope.prestamos.anticipos = {};
		$scope.emprestitos = {};
		$scope.emprestitos.prestamo = {};
		$scope.kngFecha 		 =  util.dateInicioDia($scope.SesionFechaSistema);	
		$scope.chkFechaOperacion = true;
		$scope.showBtnAccionPrestamos = true;
		$scope.showBtnAccionEmprestitos = false;
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		//EstadoPrestamo
		$scope.oConfiguracionEstado={
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
		$scope.oConfiguracionClasificacionTipoInstrumento = {
				tipo : "combobox-clasificacion-tipo-instrumento",
				configuracion : {},
				filtros : {
					Rv : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : 'RV' },
					Rf : { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : 'RF' },    // Se habilitará con VC RF
					logic: "or"
				},
				idActual : false,
				seleccionado : false,
				idDefecto : '',
				idPostFiltro : false
			};
			
		//TipoPrestamo
		$scope.oConfiguracionTipoPrestamo={
			tipo : "combobox-tipo-prestamo",
			configuracion : {},
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};
		
		$scope.oClientePortafolio={
			tipo : "combobox-cliente-portafolio",
			configuracion :{},
			seleccionado : false,
			IdActual: 0,
			filtros: {
				IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		 var dsPrestamos = new bcs.DataSource({
			type : "call",
			definition : {
			 	callTransaction	: "44290898",
			   	serverFiltering : false,
			   	serverPaging 	: false,
				serverSorting 	: false,
			   	pageSize 		: 22,
			 	callResultSet 	: "P_OUT_PRESTAMOS_VIGENTES",
			   	schema 			: bcsConfiguracionPRT.modeloPrestamos()
		 	}
	 	});
		
		var dsEmprestitos = new bcs.DataSource({
			type : "call",
			definition : {
			 	callTransaction	: "44290897",
			   	serverFiltering : false,
			   	serverPaging 	: false,
				serverSorting 	: false,
			   	pageSize 		: 22,
			 	callResultSet 	: "P_OUT_EMPRESTITOS_VIGENTES",
			   	schema 			: bcsConfiguracionPRT.modeloEmprestitos()
		 	}
	 	});
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		//PRESTAMOS
		$scope.prestamos.confBasicaPrestamos = {
			dataSource			: dsPrestamos,
			columns				: bcsConfiguracionPRT.columnasPrestamos(),
			rowColorBackground 	: bcsConfiguracionPRT.fnColorBk,
			sortable			: true,
			pageable			: true,
			filterable 			: true,
			excel 				: { allPages : true },
			userSettings 		: true,
			allowCopy			: { delimeter: "\t" },
			height				: 470,
			selectable 			: "single row"
		};

		$scope.emprestitos_prestamo_confGridEmprestitoPrestamo = function (item) {
			var dsPrestamosDetalle = {
				type : "call",
	         	definition : {
		  			serverFiltering : false,
		  			serverPaging 	: false,
		  			pageSize 		: 10,
		  			filterable 		: false,
					callTransaction	: "44290898",
					callResultSet 	: "P_OUT_PRESTAMOS_VIGENTES",
		  			schema : bcsConfiguracionPRT.modeloPrestamos(),
	           },
			};
			var parametros = {};
			parametros.p_IdEmpresa 			= item.IdEmpresa;
			parametros.p_FolioCartera 		= item.FolioPrestamo;
			dsPrestamosDetalle.definition.callParameters = parametros;

			return {
				dataSource	: new bcs.DataSource(dsPrestamosDetalle),
				columns		: bcsConfiguracionPRT.columnasPrestamos(),		
				sortable	: false,
				userSettings: true,
				allowCopy	: { delimeter: "\t" },
				pageable	: false,
				autoBind	: true,
				height		: 100,
				selectable 	: "single row"
			}
		};


		//EMPRESTITOS
		$scope.emprestitos.confBasicaEmprestitos = {
			dataSource			: dsEmprestitos,
			columns				: bcsConfiguracionPRT.columnasEmprestitos(true),
			sortable			: true,
			pageable			: true,
			userSettings 		: true,
			filterable 			: true,
			allowCopy			: { delimeter: "\t" },
			height				: 470,
			selectable 			: "single row"
		};
//este es la grilla emprestito INICIO
		$scope.prestamos_emprestitos_confGridPrestamoEmprestitos = function (item){
			var dsEmprestitoDetalle = {
				type : "call",
	         	definition : {
		  			serverFiltering : false,
		  			serverPaging 	: false,
		  			pageSize 		: 10,
		  			filterable 		: false,
					callTransaction	: "44290897",
					callResultSet 	: "P_OUT_EMPRESTITOS_VIGENTES",
		  			schema : bcsConfiguracionPRT.modeloEmprestitos(),
	           },
			};
			var parametros = {};
			parametros.p_IdEmpresa 			= item.IdEmpresa;
			parametros.p_FolioCartera 		= item.FolioPrestamo;
			dsEmprestitoDetalle.definition.callParameters = parametros;

			return {
				dataSource			: new bcs.DataSource(dsEmprestitoDetalle),
				columns				: bcsConfiguracionPRT.columnasEmprestitos(false),		
				sortable			: true,
				pageable			: true,
				userSettings 		: true,
				allowCopy			: { delimeter: "\t" },
				autoBind			: true,
				height				: 180,
				selectable 			: "single row"
			}
		};
		
	//este es la grilla emprestito FIN
	
		$scope.prestamos_anticipos_confGridPrestamoAnticipos = function (item){
			var dsDef2 = {
					type : "odata",
		            definition : {
				  		serverFiltering : true, 
				  		serverPaging : true, 
				  		pageSize : 10, 
				  		filterable : true,
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
				columns				: bcsConfiguracionPRT.columnasEmprestitosAnticipos(false),		
				sortable			: true,
				pageable			: true,
				userSettings 		: true,
				allowCopy			: { delimeter: "\t" },
				autoBind			: true,
				height				: 180,
				selectable 			: "single row"
			}
		};
		
		//-----------------------------------------------------------
		// GENERAR REPORTE (Carta de transferencias bancarias) 
		//-----------------------------------------------------------		

		$scope.generarAnexo2 = function(cual)	{
			
			var v_prestamo = 0;
			var v_empresti = 0;

			
			if (cual === 'PRT'){
				if ($scope.prestamos.selPrestamos == undefined && $scope.prestamos.emprestitos.selPrestamoEmprestitos == undefined){
					bcsComunes.msg("error","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'));
					return;				
				}
				v_empresti = 0;
				v_prestamo = $scope.prestamos.selPrestamos.FolioPrestamo;
				
			} else {
				if ($scope.prestamos.emprestitos.selPrestamoEmprestitos == undefined){
					bcsComunes.msg("error","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'));
					return;				
				}
				v_empresti = $scope.prestamos.emprestitos.selPrestamoEmprestitos.FolioEmprestito;
				v_prestamo = $scope.prestamos.emprestitos.selPrestamoEmprestitos.FolioPrestamo;
			}


			if ($scope.ReporteAnexo2.IdReporte == '' || $scope.ReporteAnexo2.IdReporte == undefined || $scope.ReporteAnexo2.NombreReporte  == '' || $scope.ReporteAnexo2.NombreReporte  == undefined || $scope.ReporteAnexo2.IdServicio == '' || $scope.ReporteAnexo2.IdServicio == undefined || $scope.ReporteAnexo2.IdServicio == 0 || $scope.ReporteAnexo2.FormatoSalida == '' || $scope.ReporteAnexo2.FormatoSalida == undefined){						
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_REPORTE_SIN_CONFIGURACION") //cambiar mensaje				
				},$scope.winID);
				return false;
			}
				
			bcsComunes.ventanaOverlay($scope.winID, true);
			var fecha = new Date();
					
			//Nombre de carta para descarga local
			var nombreCartaDescarga = $scope.ReporteAnexo2.NombreReporte +"_"+ String(fecha.getFullYear()) + String(fecha.getMonth()+1) + String(fecha.getDate()) + "_" + String(fecha.getHours()) + String(fecha.getMinutes()) + String(fecha.getSeconds()) + ".pdf"
			  
			var parameters = {
							"reportId": $scope.ReporteAnexo2.IdReporte,						
							"parameters": [],
							"exportFormat": $scope.ReporteAnexo2.FormatoSalida,
							//"debug" : true,
							
							"configuration": {
								"mainReport": {								
									"reportName": $scope.ReporteAnexo2.NombreReporte,
									"parameters": [],
									 "data" : {
						                  "serviceId" : $scope.ReporteAnexo2.IdServicio,
						                  "serviceData" : {								                      
						          			                 "p_IdEmpresa"		        : $scope.SesionIdEmpresa,
						          			                 "p_FolioPrestamo" 			: v_prestamo,
						                                     "p_FolioEmprestito"		: v_empresti,
															 "p_ConsultaHistorica"		: 0,
															 "$schema"					: [{ "field" : "LogoCorredora", "type" : "blob" }]
						                                  }
						              },
									"parameters": {},
								},
								"subReports": []
							},
						};

			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			  .success(function (response) {
				   var file = new Blob([response], {type: 'application/pdf'});
				   saveAs(file, nombreCartaDescarga);
				   bcsComunes.ventanaOverlay($scope.winID,false);
			}).error(function (response) {
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AL_GENERAR")//cambiar mensaje
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID,false);
			});

			return;

		};
		
		
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		$scope.prestamos.emprestitos.btnIngresarDetalle = function(){
			if ($scope.prestamos.selPrestamos == undefined){
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'));
				return;
			}
			
			if (!(util.dateInicioDia($scope.SesionFechaSistema) >= $scope.prestamos.selPrestamos.FechaPrestamo 
					&& util.dateInicioDia($scope.SesionFechaSistema) <= $scope.prestamos.selPrestamos.FechaVencimientoPrestamo)
				||
				$scope.prestamos.selPrestamos.IdEstado != 'VIG'){
				
				bcsComunes.msg("error","error",$translate.instant('UI_PRESTAMO_NO_VIGENTE'));
				return;
			}
			
			if ($scope.prestamos.selPrestamos.CantidadPorAsignar > 0){
				var id1 = bcsComunes.ventanaAbrir("CrearEmprestitoModal",$scope.prestamos.selPrestamos,false,false,$scope.winID); 
		        var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(data){ 
		        	if (data === true) {
		        		buscar();
		        		$scope.prestamos.selPrestamos = undefined;
					}
		    	}); 
			} else {
				bcsComunes.msg("error","error",$translate.instant('UI_PRESTAMO_YA_CUBIERTO'));
				return;
			}
			

		};
		
		$scope.prestamos.emprestitos.btnAnticiparDetalle = function(){
			
			if (!$scope.prestamos.emprestitos.selPrestamoEmprestitos) return;
			
			var id1 = bcsComunes.ventanaAbrir("AnticiparEmprestitoModal",$scope.prestamos.emprestitos.selPrestamoEmprestitos,false,false,$scope.winID); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) {
	        		buscar();
				}
	        	$scope.prestamos.emprestitos.selPrestamoEmprestitos = undefined;
	    	}); 
		};
		
		$scope.prestamos.emprestitos.anexo2 = function(){
			$scope.generarAnexo2('EMP');			
		};

		// ANULACION EMPRESTITO
		$scope.prestamos.emprestitos.btnAnularDetalle = function(){
			
			if ($scope.prestamos.emprestitos.selPrestamoEmprestitos){
				var id1 = bcsComunes.ventanaAbrir("AnularEmprestitoModal",$scope.prestamos.emprestitos.selPrestamoEmprestitos,false,false,$scope.winID); 
		        var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(data){ 
		        	if (data === true) {
		        		buscar();
						$scope.prestamos.selPrestamos = undefined;
					}
					$scope.prestamos.emprestitos.selPrestamoEmprestitos = undefined;
		    	}); 
			}
		};
	
		
		$scope.btnBuscar = function() {
			buscar();
		};

		buscar = function(){
			var filtros 	= {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario
			};

			filtros.p_ConsultaPor = $scope.chkFechaOperacion ? 'OPERACION' : 'VENCIMIENTO';
			filtros.p_FechaConsulta = util.dateSPdate($scope.kngFecha);

			try { // se mete dentro de un try ya q cuando no se ingresa un valor al objeto, este no se lee
				if ($scope.oConfiguracionTipoPrestamo.idActual !== 0){
					filtros.p_IdTipoPrestamo = $scope.oConfiguracionTipoPrestamo.seleccionado.IdTipoPrestamo;
				}
				if ($scope.oClientePortafolio.seleccionado.IdPersona){
					filtros.p_IdCliente = Number($scope.oClientePortafolio.seleccionado.IdCliente || 0);
				}
				if ($scope.oClientePortafolio.seleccionado.IdPortafolio){
					filtros.p_IdPortafolio = Number($scope.oClientePortafolio.seleccionado.IdPortafolio || 0);
				}
				if ($scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.Descripcion){
					filtros.p_IdClasificacionTipoInstrumento = $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento;
				}

				if ($scope.folioPrestamo !== undefined && $scope.folioPrestamo !=='' && $scope.folioPrestamo !==0){
					if (Number($scope.folioPrestamo)){
						var auxNum = Number($scope.folioPrestamo);
						filtros.p_FolioCartera = auxNum;
					} else {
						filtros.p_FolioCartera = Number($scope.folioPrestamo.replace(/\./g, ""));
					}
	        	}
	    	} catch(e) {
				//mensaje:$translate.instant("UI_ERROR_AL_BUSCAR")
	    	}

			dsPrestamos.setCallParameters(filtros);
			dsEmprestitos.setCallParameters(filtros);
		}
		
		$scope.btnLimpiar = function() {
			$scope.oConfiguracionEstado.idDefecto 		= 0;
			$scope.oConfiguracionEstado.idActual 		= 0;
			$scope.oConfiguracionTipoPrestamo.idDefecto = 0;
			$scope.oConfiguracionTipoPrestamo.idActual 	= 0;
			$scope.filtros.IdPortafolio = false;
			$scope.oClientePortafolio.seleccionado = false;	
			$scope.kngFecha 		   =  util.dateInicioDia($scope.SesionFechaSistema);	
			$scope.chkFechaOperacion   = true;
			$scope.chkFechaVencimiento = false;
			
			dsPrestamos.removeAll();
			dsEmprestitos.removeAll();
		};
		
	
		$scope.$watch('chkFechaOperacion',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.chkFechaVencimiento = false;  //desclikea check fecha vencimiento
					break;
				case false: 
					$scope.chkFechaVencimiento = true;				
					break;
			}				
		});
		
		$scope.$watch('chkFechaVencimiento',function(newval,oldval){
			switch (newval){
				case true:  
					$scope.chkFechaOperacion = false; //desclikea check fecha operacion				
					break;
					
				case false: 
					$scope.chkFechaOperacion = true;
					break;  
			}
		});
		
		$scope.btnIngresar = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			var id1 = bcsComunes.ventanaAbrir("CrearPrestamoModal",{Accion: 'CREAR'},false,false,$scope.winID); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) buscar();
	    	}); 
		};
		
		$scope.btnModificarPrestatario = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			
			var emprestitos = $scope.emprestitos.confBasicaEmprestitos.dataSource.ds._data;
			var prestamo = $scope.prestamos.selPrestamos;
			const existeEmprestito = emprestitos.some(emp => emp.FolioPrestamo === prestamo.FolioPrestamo)
			if (existeEmprestito) {
				bcsComunes.ventanaMsgErr($translate.instant("PRESTAMO_ASIGNADO_NO_SE_PUEDE_MODIFICAR"));
				return;
			}

			var id1 = bcsComunes.ventanaAbrir("ModificarPrestamoModal",prestamo,false,false,$scope.winID); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) buscar();
	    	}); 
		};
		
		
		//Modificar Emprestito (Prestamista)		  
		$scope.prestamos.emprestitos.btnModificarEmprestito = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			
			/***
			if (util.dateSPdate($scope.prestamos.selPrestamos.INGFechaOperacion) !== util.dateSPdate($scope.kngFecha)){
				bcsComunes.msg("error","info",$translate.instant('UI_SOLAMENTE_SE_PUEDEN_MODIFICAR_PRESTAMOS_DEL_DIA'));
				return;
			}
			***/
			
			if (!$scope.prestamos.emprestitos.selPrestamoEmprestitos) 
			{
				bcsComunes.msg("error","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMISTA'));
				return;
			}
 
			var id1 = bcsComunes.ventanaAbrir("ModificarEmprestitoModal",$scope.prestamos.emprestitos.selPrestamoEmprestitos,false,false,$scope.winID);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) buscar();
	    	}); 
		};		
		
/*
		$scope.prestamos.emprestitos.btnIngresarDetalle = function(){
			if ($scope.prestamos.selPrestamos == undefined){
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'));
				return;
			}
			
			if (!(util.dateInicioDia($scope.SesionFechaSistema) >= $scope.prestamos.selPrestamos.FechaPrestamo 
					&& util.dateInicioDia($scope.SesionFechaSistema) <= $scope.prestamos.selPrestamos.FechaVencimientoPrestamo)
				||
				$scope.prestamos.selPrestamos.IdEstado != 'VIG'){
				
				bcsComunes.msg("error","error",$translate.instant('UI_PRESTAMO_NO_VIGENTE'));
				return;
			}
			
			if ($scope.prestamos.selPrestamos.CantidadPorAsignar > 0){
				var id1 = bcsComunes.ventanaAbrir("CrearEmprestitoModal",$scope.prestamos.selPrestamos,false,false,$scope.winID); 
		        var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(data){ 
		        	if (data === true) {
		        		buscar();
		        		$scope.prestamos.selPrestamos = undefined;
					}
		    	}); 
			} else {
				bcsComunes.msg("error","error",$translate.instant('UI_PRESTAMO_YA_CUBIERTO'));
				return;
			}
			

		}; 
 * */		
	
		$scope.btnAnticipar = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			if ($scope.prestamos.selPrestamos == undefined){
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_PRESTAMO'));
				return;
			}
			if (util.datediff(util.dateInicioDia($scope.prestamos.selPrestamos.FechaPrestamo), util.dateInicioDia($scope.SesionFechaSistema)).asDays() === 0 ||
				util.datediff(util.dateInicioDia($scope.SesionFechaSistema), util.dateInicioDia($scope.prestamos.selPrestamos.FechaLiquidacion)).asDays() < 0 ) {
				bcsComunes.msg("error","error",$translate.instant('UI_ANTICIPO_PRESTAMO_ANTICIPO_PRIMER_DIA'));
				return;
			}
			
			if (!(util.dateInicioDia($scope.SesionFechaSistema) >= $scope.prestamos.selPrestamos.FechaPrestamo 
					&& util.dateInicioDia($scope.SesionFechaSistema) <= $scope.prestamos.selPrestamos.FechaVencimientoPrestamo)
				||
				$scope.prestamos.selPrestamos.IdEstado != 'VIG'){
				
				bcsComunes.msg("error","error",$translate.instant('UI_PRESTAMO_NO_VIGENTE'));
				return;
			}
			
			//solo permite anticipar un prestamo si este esta totalmente asignado
			if ($scope.prestamos.selPrestamos.CantidadPorAsignar > 0){
				
				if(util.datediff(util.dateInicioDia($scope.SesionFechaSistema), util.dateInicioDia($scope.prestamos.selPrestamos.FechaVencimientoPrestamo)).asDays() < 0){
					var msg = bcsAlerta.crear("PREGUNTA", $translate.instant('UI_CERRAR_PRESTAMO'));
					msg.result.then(function (result) {
						if (result === OPCIONES_BCSALERTA.ACEPTAR) {
							bcsComunes.ventanaOverlay($scope.winID, true);
							
							var id1 = bcsComunes.ventanaAbrir("AnticiparPrestamoModal",$scope.prestamos.selPrestamos,false,false,$scope.winID); 
					        var promesa=bcsComunes.ventanaPromesa(id1);                               
					        promesa.then(function(data){ 
					        	if (data === true) {
					        		buscar();
								}
					        	$scope.prestamos.selPrestamos = undefined;
					    	}); 							
							return;
							
							
							bcsComunes.msg("warning","info",$translate.instant('Me fui a anticipar '));
							
						}else{
							bcsComunes.msg("warning","info",$translate.instant('UI_PRESTAMO_DEBE_ESTAR_COMPLETAMENTE_ASIGNADO'));							
							return;
						}
	
					});
					
				}
				
				bcsComunes.ventanaOverlay($scope.winID, false);				
			}else{
				var id1 = bcsComunes.ventanaAbrir("AnticiparPrestamoModal",$scope.prestamos.selPrestamos,false,false,$scope.winID); 
		        var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(data){ 
		        	if (data === true) {
		        		buscar();
					}
		        	$scope.prestamos.selPrestamos = undefined;
		    	}); 							
				return;
			}
			
		};
		
		$scope.btnAnticiparEmpr = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			if (!$scope.emprestitos.selEmprestitos) return;
			var id1 = bcsComunes.ventanaAbrir("AnticiparEmprestitoModal",$scope.emprestitos.selEmprestitos,false,false,$scope.winID); 
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true) {
	        		buscar();
				}
	    	});

		};

		$scope.btnReversar = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") {
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			if(!$scope.prestamos.selPrestamos) return;
			
			if($scope.prestamos.selPrestamos.IdEstado != "ANT"){
				bcsComunes.msg("warning","info",'UI_SOLAMENTE_SE_PUEDEN_REVERSAR_PRESTAMOS_ANTICIPADOS');
				return
			};
			
			var mensajeAnticipoPrestamo = "UI_MENSAJE_REVERSA_ANTICIPO_OPERACION";
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant(mensajeAnticipoPrestamo));
			t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR) 
				fnReversar();			
			else 
				bcsComunes.msg("info","info",$translate.instant("UI_MENSAJE_CANCELAR") + ' : ' + $translate.instant("UI_CANCELAR"));
			});
			return;
		};
		
		var fnReversar = function(){
			var datos = {};	
			bcsComunes.ventanaOverlay($scope.winID,true); 
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'CUS';
			datos.p_UsaReglasNegocio = 'S';
			datos.p_IdFolio = Number($scope.prestamos.selPrestamos.FolioPrestamo) || 0;
			bcsServiciosPrestamo.ReversarAnticipoPrestamo
							(datos,
							function(respuesta){	  		
								var miVen = bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaOverlay($scope.winID,true);
								buscar();
							},
							function(respuesta){
								var miVen = bcsComunes.ventanaMsg({
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								bcsComunes.ventanaOverlay($scope.winID,true);
							});			
		};		
		
		

		$scope.btnAnular = function(){
			if (BCS_ESTADO_MODULO['CUS'] === "CER") 
			{
				bcsComunes.ventanaMsgErr($translate.instant("MODULO_CERRADO"));
				return;
			}
			
			if (util.dateSPdate($scope.prestamos.selPrestamos.FechaPrestamo) !== util.dateSPdate($scope.kngFecha))
			{
				
				
				bcsComunes.ventanaMsg({mensaje:$translate.instant("ANULACION_PERMITIDA_SOLO_DIA_INGRESO")},$scope.winID);
				return;
			}
			else
			{
				var t = bcsAlerta.crear("PREGUNTA",$translate.instant("UI_CONFIRMAR_ANULACION_PRESTAMO"));
				t.result.then(function(result){
					if (result===OPCIONES_BCSALERTA.ACEPTAR) 
					{
						fnAnular(); 
					}
				});
				return;				
			}
			
		};

		
		

		var fnAnular = function(){
			var datos = {};	
			bcsComunes.ventanaOverlay($scope.winID,true); 
			
			datos.p_IdEmpresa = $scope.SesionIdEmpresa;
			datos.p_IdUsuario = $scope.SesionIdUsuario;
			datos.p_IdModulo = 'CUS';
			datos.p_UsaReglasNegocio = 'S';
			datos.p_IdPais = $scope.SesionIdPaisSistema;
			datos.p_FechaAnulacion  = $scope.SesionFechaSistema;
			datos.p_FolioPrestamo = Number($scope.prestamos.selPrestamos.FolioPrestamo);
			
			
	   		bcsServiciosPrestamo.AnularPrestamo
								(datos,
								function(respuesta){	  		
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.winID);
									bcsComunes.ventanaOverlay($scope.winID,false);
									buscar();
								},
								function(respuesta){
									bcsComunes.ventanaMsg({
										conflictos:util.parseServerResponse(respuesta)
									},$scope.winID);
									bcsComunes.ventanaOverlay($scope.winID,false);
								});
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
		
		
		$scope.gridSelecionada = "";
		
		$scope.select = function(grid){
			$scope.gridSelecionada = grid;
			util.refreshGridSize(grid, 50);
			
			$timeout(function(){
				if ($("#" + grid).find("[kendo-grid]").data("kendo-grid")){
					$("#" + grid).find("[kendo-grid]").data("kendo-grid").refresh()
				}	
			},100);
			if (grid === 'grid-prestamos'){
				$scope.botonDisabled = true;
				$scope.showBtnAccionPrestamos = true;
				$scope.showBtnAccionEmprestitos = false;
			}
			if (grid === 'grid-emprestitos'){
				$scope.botonDisabled = false;
				$scope.showBtnAccionPrestamos = false;
				$scope.showBtnAccionEmprestitos = true;
			}
			
		};

		$scope.exportarExcel = function(){
			console.log($scope.gridSelecionada);
			if($scope.gridSelecionada==="grid-prestamos"){
				$scope.prestamos.exportarExcel();			           
			}else if($scope.gridSelecionada==="grid-emprestitos"){
				$scope.emprestitos.exportarExcel();			
			}
		};

	});
}());
//------------------------- FIN CONTROLLER --------------------------