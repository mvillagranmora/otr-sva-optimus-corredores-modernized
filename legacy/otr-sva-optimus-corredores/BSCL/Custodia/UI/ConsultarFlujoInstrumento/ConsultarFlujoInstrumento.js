angular.module('ModuloCustodia').controller('ConsultarFlujoInstrumentoCtrl',
		function($rootScope, $scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosCustodia,bcsServicioPlataforma)
{
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	//*** configuracion de objetos ***//
	$scope.data ={};
	$scope.data.oDesplegar = {};
	$scope.data.oDesplegar.showEmisor = false;
	$scope.data.oDesplegar.showTipoReajuste = false;
	$scope.data.oDesplegar.showFechaVencimiento = false;
	
	var vienenParametros = false;
	
	$scope.data.kngFechaConsulta = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	$scope.data.oConfiguracionClasificacionTipoInstrumento={
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		idActual : 'RV',
		seleccionado : undefined,
		idDefecto : 'RV',
		idPostFiltro : 'RV',
		filtros: {
			IdClasificacionTipoInstrumento : {"field": "IdClasificacionTipoInstrumento","operator": "neq", "value"   :  "RE" },
		},
	};
	
	$scope.data.oSocioNegocio2={
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
   	$scope.data.oConfiguracionTipoInstrumento={
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
			NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
			IdTipoInstrumento				: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
			NoDerivados					    : { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "DER" }
		}
	};
   	
   	$scope.data.oConfiguracionInstrumento={
		buscar : "-1"    
	};
   	
	$scope.data.oConfiguracionEmisor={
		tipo : "combobox-emisor",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdEmisor : { "field"   : "IdEmisor","operator": "neq", "value"   : "" },
			IdEmiteIIF : { "field"   : "EmiteIIF","operator": "contains", "value"   : "" },
			IdEmiteIRF : { "field"   : "EmiteIRF","operator": "contains", "value"   : "" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.data.oConfiguracionTipoReajuste={
		tipo : "combobox-instrumento-tipo-reajuste",
		configuracion : {
			template: '<span >#: IdTipoReajuste # - #: NombreTipoReajuste #</span>'
		},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdInstrumento : { "field"   : "IdInstrumento","operator": "eq", "value"   : "" }			
		},
		idActual : false,
		seleccionado : false,
		idDefecto: false,
		idPostFiltro : false
	};
	
	$scope.data.oConfiguracionBoveda={
		filtros : {
			IdEstado : { field: "IdEstado", operator: "eq", value: "VIG" }
		},
		idActual : '0',
		idDefecto : '0'
	};
	
	$scope.data.oConfiguracionTipoFlujo={
			filtros : {
				//IdEstado : { field: "IdEstado", operator: "eq", value: "VIG" }
			},
			idActual : '0',
			idDefecto : 'FLJINS'
		};
	
	if (parametros.IdCliente > 0 && parametros.IdPortafolio > 0 && parametros.IdClasificacionTipoInstrumento && parametros.IdTipoInstrumento){
		vienenParametros = true;
		$timeout(function() {
			$scope.data.oSocioNegocio2.idDefecto = parametros.IdPortafolio;
			$scope.data.oSocioNegocio2.idActual = parametros.IdPortafolio;
			$scope.data.kngFechaConsulta = parametros.FechaConsulta;
			$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual	= parametros.IdClasificacionTipoInstrumento;
			$scope.data.oConfiguracionClasificacionTipoInstrumento.idDefecto = parametros.IdClasificacionTipoInstrumento;
			$scope.data.oConfiguracionTipoInstrumento.idActual = parametros.IdTipoInstrumento;
			$scope.data.oConfiguracionTipoInstrumento.idDefecto	= parametros.IdTipoInstrumento;
			
			$scope.instrumentoDatos = {};
			$scope.instrumentoDatos.IdClasificacionTipoInstrumento = parametros.IdClasificacionTipoInstrumento;
			$scope.instrumentoDatos.IdTipoInstrumento = parametros.IdTipoInstrumento;
			$scope.instrumentoDatos.IdInstrumento = parametros.IdInstrumento;
			$scope.data.oConfiguracionInstrumento.buscar = parametros.Nemotecnico;
			
			if (parametros.IdClasificacionTipoInstrumento === 'RF' || parametros.IdClasificacionTipoInstrumento === 'IF') {
				$scope.data.oConfiguracionEmisor.idActual	= parametros.IdEmisor;
				$scope.data.oConfiguracionEmisor.idDefecto	= parametros.IdEmisor;
				$scope.data.oConfiguracionTipoReajuste.idActual	= parametros.TipoReajuste;
				$scope.data.oConfiguracionTipoReajuste.idDefecto= parametros.TipoReajuste;
				$scope.data.kngFechaVencimiento = parametros.FechaVencimiento;
			}
			
			$scope.data.oConfiguracionBoveda.idActual = parametros.IdBoveda;
			$scope.data.oConfiguracionBoveda.idDefecto = parametros.IdBoveda;
		},40);
	}

	
	$scope.preparaParametros = function(filtros){
		var tblUso = [];
		var tblInstr = [];
		var tblAgrupaPor = [];
		var parametrosConsulta = {};

		tblUso.push({
			UsoTipoOperacion :$scope.data.oConfiguracionTipoFlujo.seleccionado? $scope.data.oConfiguracionTipoFlujo.seleccionado.UsoTipoOperacion : 'FLJINS',
			IdCliente 		: $scope.data.oSocioNegocio2.seleccionado.IdCliente,
			IdPortafolio 	: $scope.data.oSocioNegocio2.seleccionado.IdPortafolio,
			BusquedaEspecifica 			:$scope.data.oConfiguracionTipoFlujo.seleccionado?$scope.data.oConfiguracionTipoFlujo.seleccionado.BusquedaEspecifica :'FLUJO_INSTRUMENTO',
			IdTipoInstrumentoEspecifico : $scope.instrumentoDatos.IdTipoInstrumento
		});
			
/*	    	if ($scope.instrumentoDatos !== undefined){    		
		    	if ($scope.instrumentoDatos.IdInstrumento != "" && $scope.instrumentoDatos.IdInstrumento !== undefined)	    		
		    		filters.push({ field: "IdInstrumento", operator: "eq", value: Number($scope.instrumentoDatos.IdInstrumento) });
	    	}
*/			

		tblInstr.push({
			IdClasificacionTipoInstrumento 	: $scope.instrumentoDatos.IdClasificacionTipoInstrumento,
  			IdTipoInstrumento 				: $scope.instrumentoDatos.IdTipoInstrumento,
			IdInstrumento 					: $scope.instrumentoDatos.IdInstrumento,
			IdEmisor						: ($scope.data.oDesplegar.showEmisor ? $scope.data.oConfiguracionEmisor.seleccionado.IdEmisor : null) ,
			IdTipoReajuste					: ($scope.data.oDesplegar.showTipoREajuste && $scope.data.oConfiguracionTipoReajuste.seleccionado ? $scope.data.oConfiguracionTipoReajuste.seleccionado.IdTipoReajuste : null),
			FechaVencimiento				: ($scope.data.oDesplegar.showFechaVencimiento ? typeof $scope.data.kngFechaVencimiento !== 'string' ? util.dateSPdate($scope.data.kngFechaVencimiento) : $scope.data.kngFechaVencimiento : null),
			EsFungible						: null,
			IdBoveda						: $scope.data.oConfiguracionBoveda.idActual == '0' ? null : $scope.data.oConfiguracionBoveda.seleccionado.IdBoveda
  		 });


		tblAgrupaPor.push({
			ClasificacionTipoInstrumento	: 'S',
			TipoInstrumento					: 'S',
			Instrumento						: 'S',
			Emisor							: ($scope.data.oDesplegar.showEmisor ? 'S' : null),
			Nemotecnico						: null,
			NemoSVS							: null,
			FechaEmisionInstrumento			: null,
			FechaVencimientoInstrumento		: ($scope.data.oDesplegar.showFechaVencimiento ? 'S' : null),
			FechaLiquidacionInstrumento		: null,
			TipoReajuste					: ($scope.data.oDesplegar.showTipoREajuste ? 'S' : null),
			EsFungible						: null,
			CodigoIFRS						: null,
			Boveda							: $scope.data.oConfiguracionBoveda.idActual == '0' ? null : 'S',
			Bolsa							: null,
			TipoOperacion					: null,
			Mesa							: null,
			FolioRegistro					: null,
			ItemOrigen						: 'S',
			MonedaInstrumento				: 'N'
		});	
		
		parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa; // falta sacar de variables de sistema
		parametrosConsulta.p_IdUsuario = $scope.SesionIdUsuario; // falta sacar de variables de sistema
		parametrosConsulta.p_IdModulo = 'CUS';

		parametrosConsulta.p_IdPais = -1;
		parametrosConsulta.p_FechaSaldo = typeof $scope.data.kngFechaConsulta !== 'string' ? util.dateSPdate($scope.data.kngFechaConsulta) : $scope.data.kngFechaConsulta;
		parametrosConsulta.p_DiasProyeccion = 5;
		
		parametrosConsulta.p_Uso = tblUso;
		parametrosConsulta.p_FiltrosBusqueda = tblInstr;
		parametrosConsulta.p_EsAgrupadoPor = tblAgrupaPor;

		//parametrosConsulta.p_IdBoveda = '';		
		return parametrosConsulta;
	};
	

	var dsSaldoFlujoInstrumentos = new bcs.DataSource({
		type : "array",
		definition : {
			data		: [],
			pageSize 	: 100,
			serverSorting	: false, // Ordenamiento de los datos en el servidor.
			sort 		: [ { field: "ItemOrigen", dir: "asc" } ],
			schema  	: {
				model : {
					fields : {
						ItemOrigen		: { type: "string"},
						IdMoneda		: { type: "string"},						
						T0 				: { type: "number"},
						T1 				: { type: "number"},
						T2 				: { type: "number"},
						T3 				: { type: "number"},
						T4 				: { type: "number"},
						T5 				: { type: "number"}
					}
				}
			}
		}
	});
	$scope.confBasicaFlujoInstrumento = {
		dataSource  : dsSaldoFlujoInstrumentos,
		columns 	: [
						 {field: "ItemOrigen"	,title: 'UI_CONCEPTO'	,width: "100px"}
						,{field: "T0"			,title: 'PH'			,width: "100px"	,template : " <label> #:  (util.bcs_formato(T0,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
						,{field: "T1"			,title: 'PM'			,width: "100px"	,template : " <label> #:  (util.bcs_formato(T1,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
						,{field: "T2"			,title: 'CN'			,width: "100px"	,template : " <label> #:  (util.bcs_formato(T2,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
						,{field: "T3"			,title: 'T3'			,width: "100px"	,template : " <label> #:  (util.bcs_formato(T3,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
						,{field: "T4"			,title: 'T4'			,width: "100px"	,template : " <label> #:  (util.bcs_formato(T4,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}							
						,{field: "T5"			,title: 'T5'			,width: "100px"	,template : " <label> #:  (util.bcs_formato(T5,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}              							
					  ],
		contextMenu	: true,
	   	filterable  : true,
	   	sortable    : true,
	   	userSettings: true,
	   	allowCopy	: { delimeter: "\t" },
	   	pageable    : false,
	  	height      : 440,
	   	selectable  : "row",
	   	autoBind    : false,
	   	reorderable	: false,
	   	groupable	: true,
	   	scrollable	: true
	};


	$scope.btnBuscar = function (){
		if (!$scope.data.oSocioNegocio2.seleccionado){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_INGRESAR_PORTAFOLIO_A_CONSULTAR"));
			return;
		}
		if (!$scope.data.kngFechaConsulta){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_DEBE_INGRESAR_FECHA"));
			return;
		}
		if (!$scope.instrumentoDatos){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_INGRESAR_INSTRUMENTO"));
			return;
		}
		if ($scope.data.oDesplegar.showEmisor && !$scope.data.oConfiguracionEmisor.seleccionado){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("DEBE_INGRESAR_EMISOR_INSTRUMENTO"));
			return;
		}
		if ($scope.data.oDesplegar.showFechaVencimiento && !$scope.data.kngFechaVencimiento){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_FECHA_VENCIMIENTO_INVALIDA"));
			return;
		}
		if (!$scope.data.oConfiguracionBoveda.seleccionado){
			bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("UI_BOVEDA_INVALIDA"));
			return;
		}
		
		bcsComunes.ventanaOverlay(idVentana, true);

		bcsServiciosCustodia.ConsultarFlujoInstrumento($scope.preparaParametros($scope.data),
			function(respuesta){							
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					var Saldos = [];
					for(i=0; i<Object.keys(respuesta.P_OUT_SALIDA).length; i++){
						Saldos.push({
							ItemOrigen		: $translate.instant(respuesta.P_OUT_SALIDA[i].ItemOrigen),
							IdMoneda		: ($scope.data.oDesplegar.showTipoREajuste ? $scope.instrumentoDatos.IdTipoREajuste : $scope.instrumentoDatos.IdMoneda),							
							T0 				: respuesta.P_OUT_SALIDA[i].T0,
							T1 				: respuesta.P_OUT_SALIDA[i].T1,
							T2 				: respuesta.P_OUT_SALIDA[i].T2,
							T3 				: respuesta.P_OUT_SALIDA[i].T3,
							T4 				: respuesta.P_OUT_SALIDA[i].T4,
							T5 				: respuesta.P_OUT_SALIDA[i].T5
						});
					}
					dsSaldoFlujoInstrumentos.data(Saldos);						
				}
				bcsComunes.ventanaOverlay(idVentana, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay(idVentana, false);		
			}
		);
	};

	
	$scope.btnLimpiar = function() {
		$scope.data.oSocioNegocio2.idActual = '0';
		$scope.data.kngFechaConsulta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.oConfiguracionClasificacionTipoInstrumento.idActual = 'RV';
		$scope.data.oConfiguracionInstrumento.buscar = '-1';
		$scope.data.oConfiguracionEmisor.idActual = '';
		$scope.data.oConfiguracionTipoReajuste.idActual = '';
		$scope.data.kngFechaVencimiento = '';
		$scope.data.oConfiguracionBoveda.idActual = '0';
		dsSaldoFlujoInstrumentos.removeAll();
	}
	
	$scope.$watch('data.oConfiguracionClasificacionTipoInstrumento.seleccionado', function(newValue, oldValue) {
		if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado){
			if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento==='RV'){
				$scope.data.oDesplegar.showEmisor = false;
				$scope.data.oDesplegar.showTipoReajuste = false;
				$scope.data.oDesplegar.showFechaVencimiento = false;
				$scope.data.enabledEmisor = false; 
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value = 'Z';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.operator = 'neq';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value = 'Z';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.operator = 'neq';
			}
			if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento==='RF'){
				$scope.data.oDesplegar.showEmisor = true;
				$scope.data.oDesplegar.showTipoReajuste = true;
				$scope.data.oDesplegar.showFechaVencimiento = true;
				$scope.data.enabledEmisor = false; 
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value = 'S';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.operator = 'eq';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value = 'Z';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.operator = 'neq';
			}
			if ($scope.data.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento==='IF'){
				$scope.data.oDesplegar.showEmisor = true;
				$scope.data.oDesplegar.showTipoReajuste = true;
				$scope.data.oDesplegar.showFechaVencimiento = true;
				$scope.data.enabledEmisor = false;
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value = 'Z';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.operator = 'neq';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value = 'S';
				$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.operator = 'eq';
			}
		}
	});
	
	$scope.$watch('instrumentoDatos', function(newValue, oldValue) {
		if (!$scope.instrumentoDatos) return;
		
		if ($scope.instrumentoDatos.Reajustable !== undefined && $scope.data.oDesplegar.showTipoReajuste === true) {
			$scope.data.oConfiguracionTipoReajuste.filtros.IdInstrumento.value = Number($scope.instrumentoDatos.IdInstrumento) || 0;
			$scope.data.oConfiguracionTipoReajuste.idPostFiltro = $scope.instrumentoDatos.IdTipoReajuste;
			//$scope.data.oConfiguracionTipoReajuste.idActual = $scope.data.instrumento.IdTipoReajuste;
		}
	});
	
	//Funcion $watch para controlar busqueda inmediata cuando ventana es abierta desde ConsultaCustodiaPortafolio.js
	$scope.$watch('FormFlujoInstrumento.$valid', function(newValue, oldValue) {
		console.log("FormFlujoInstrumento.$valid : " + $scope.FormFlujoInstrumento.$valid);
		if (!$scope.FormFlujoInstrumento.$valid || newValue == oldValue) return;
		
		if (vienenParametros) {
			var filtrosValid = $scope.data.oSocioNegocio2.seleccionado && $scope.data.kngFechaConsulta && $scope.instrumentoDatos && $scope.data.oConfiguracionBoveda.seleccionado ? true : false;
			if (filtrosValid && $scope.data.oDesplegar.showEmisor) {
				filtrosValid = $scope.data.oConfiguracionEmisor.seleccionado && filtrosValid ? true : false;
			}
			if (filtrosValid && $scope.data.oDesplegar.showFechaVencimiento) {
				filtrosValid = $scope.data.kngFechaVencimiento && filtrosValid ? true : false;
			}
			
			// Si el formulario y todos los filtros son validos, realiza busqueda inmediata
			if (filtrosValid){
				vienenParametros = false;
				$scope.btnBuscar();
			}
		}
	});
});