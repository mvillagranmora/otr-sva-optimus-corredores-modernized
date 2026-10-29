angular.module('ModuloTesoreria').controller('ConsultarDocumentosCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,
				 $translate,bcsServiciosTesoreria, bcsBlotterComprobantesGrid)
{	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.btnActivo = true;
	$scope.ExcluyeDocumentosLiquidados = true;
	$scope.Portafolio = "";
	

	//VARIABLES REQUERIDAS PARA LLAMADO DE LA VENTANA EN FORMA MODAL
	$scope.winID = bcsComunes.ventanaWinId($scope);
	var parametros = bcsComunes.ventanaParams($scope.winID);

	//SETEO INICIAL
	var IdCuentaBanco = 0;
	var VentanaModal = 'N';
	$scope.kngFechaLiquidacionDesde	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaLiquidacionHasta	= util.dateTerminoDia($scope.SesionFechaSistema);

	//LLAMADO DE LA VENTANA EN FORMA MODAL
	if(parametros.datos !== undefined){
		VentanaModal = 'S'
		IdCuentaBanco = Number(parametros.datos[0].IdCuentaBanco);
		$scope.kngFechaLiquidacionDesde	= util.dateInicioDia(parametros.datos[0].FechaDesde);
		$scope.kngFechaLiquidacionHasta	= util.dateTerminoDia(parametros.datos[0].FechaDesde);
		$scope.mostrarBoton = false;	
	}

	$scope.activadoPortafolio=true;
	
	//COMBOBOX
	$scope.oConfiguracionCuentaBancoCorredora={
		tipo : "combobox-cuentas-bancarias-corredora",
		configuracion : {},
		filtros : {
			IdEstado 		 : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },			
		},
		idActual : false,
		seleccionado : false,
		activado : VentanaModal === 'N' ? true : false,
		idDefecto : VentanaModal === 'N' ? '' : IdCuentaBanco,
		todos :VentanaModal === 'N' ? 'S' : 'N',
	};
	$scope.oConfiguracionMedioDePago={
		tipo : "combobox-medio-pago",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa }			
		},
	};
	$scope.oConfiguracionTipoSocioNegocio={
		tipo 			: "combobox-tipo-socio-negocio",
		configuracion 	: {},
		filtros 		: {},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro  	: false				
	};
	$scope.oConfiguracionPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	$scope.oConfiguracionCuentaBancoSocioNegocio={
		tipo : "combobox-persona-cuentas-bancarias", 
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },				
			IdPersona : { "field"   : "IdPersona","operator": "eq", "value"   : 0 }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	$scope.oConfiguracionEstadoDocumentoTesoreria={
		tipo : "combobox-estado-tesoreria",
		configuracion : {},
		filtros : {
			NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value": 'TES.ComprobanteFormaDePago'},
			IdEstado :      { "field": "IdEstado", "operator": "neq", "value": "ANU" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};
	$scope.oConfiguracionCaja={
		tipo : "combobox-caja",
		configuracion : {},
		filtros : {
			//IdEstadoCaja : { "field"   : "IdEstadoCaja","operator": "eq", "value"   : "ABT" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};	
	$scope.oConfiguracionCajaMoneda={
		tipo : "combobox-caja-moneda",
		configuracion : {},
		filtros : {
			IdCaja : { "field"   : "IdCaja","operator": "eq", "value"   : "" },
			IdMoneda : { "field"   : "IdMoneda","operator": "neq", "value"   : "null" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};
	
	$scope.oConfiguracionChequerasSeries={
			tipo : "combobox-chequeras-series",
			configuracion : {},
			filtros : {
				IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
				IdCuentaBanco : { "field"   : "IdCuentaBanco","operator": "eq", "value"   :"" }
			},
			idActual : false,
			seleccionado : false,
			idDefecto : false
	};
	
	
	$scope.$watch('oConfiguracionEstadoDocumentoTesoreria.seleccionado',function(newval,oldval){		
		if ($scope.oConfiguracionEstadoDocumentoTesoreria.seleccionado.IdEstado === 'LIQ')
			$scope.ExcluyeDocumentosLiquidados = false;
		else
			$scope.ExcluyeDocumentosLiquidados = true;
	});
	
	
	$scope.$watch('oConfiguracionTipoSocioNegocio.seleccionado',function(newval,oldval){
		if($scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio === 'CLI' || $scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio === undefined ){
			$scope.activadoPortafolio=true;
			if( $scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio === undefined){
				$scope.Portafolio = "";
			}
		}else{
			$scope.activadoPortafolio=false;
			$scope.Portafolio = "";
		}
	});
	
	

	//GRILLA PRINCIPAL
	var dsDocumentos = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefDocumentos);
	$scope.confBasicaDocumentos = {
			dataSource			: dsDocumentos,
			columns				: bcsBlotterComprobantesGrid.columnasDocumentos(),
			sortable			: true,
			pageable			: true,
			autoBind			: VentanaModal === 'N' ? false : true,
			filterable			: true,
			height				: 430,
			selectable 			: "multiple row",
			userSettings		: true,
			navigatable         : true,
			gridCreated 		: function(g) { 
									$scope.ApiComprobantes = g; 

									//Busqueda Inicial
									var filters = [];
									filters.push({ field: "IdEstado", operator: "eq", value: 'ING'});
									filters.push({ field: "FechaLiquidacion", operator: "gte", value: util.dateInicioDia($scope.kngFechaLiquidacionDesde) });
									filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.kngFechaLiquidacionHasta) });
									
									if(sesion.getPreferencias('recordarCliente')){
										if (sesion.getPreferencias('IdPersonaRecordado') !== null && sesion.getPreferencias('IdPersonaRecordado') !== undefined)
										filters.push({ field: "IdPersona", operator: "eq", value: Number(sesion.getPreferencias('IdPersonaRecordado')) });
									}
									if ( VentanaModal === 'S' ){										
										filters.push({ field: "IdComprobantePartida", operator: "eq", value: 0 });										
										filters.push({ field: "IdCuentaBancariaCorredora", operator: "eq", value: IdCuentaBanco });
									}
									dsDocumentos.filter(filters);
									
									if ($scope.SesionLocale === 'PE')
									{
										g.showColumn("IdComprobanteFormaDePago");
									}
								  }
	};		
		
	$scope.buscarDocumentos = function(){
		var filters = new Array();
		
		//No se deben mostrar los documentos que originaron una partida(ingreso/egreso)
		filters.push({ field: "IdComprobantePartida", operator: "eq", value: 0 });
		filters.push({ field: "FechaLiquidacion", operator: "gte", value: util.dateInicioDia($scope.kngFechaLiquidacionDesde) });
		filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.kngFechaLiquidacionHasta) });
		
		if ($scope.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco && IdCuentaBanco ===0) {
			filters.push({ field: "IdCuentaBancariaCorredora", operator: "eq", value: $scope.oConfiguracionCuentaBancoCorredora.seleccionado.IdCuentaBanco });
		}else if(IdCuentaBanco !== 0){
			filters.push({ field: "IdCuentaBancariaCorredora", operator: "eq", value: IdCuentaBanco });
		}

		filters.push({ field: "IdMedioDePago"	, operator: "eq"		, value: $scope.oConfiguracionMedioDePago.seleccionado.IdMedioDePago || -1 });
		filters.push({ field: "IdPersona"		, operator: "eq"		, value: Number($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona) || -1 });
		//PERSONA TITULAR PAGO???
		filters.push({ field: "IdPortafolioTitularPago", operator: "eq"	, value: Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio)  || -1 });
		filters.push({ field: "IdCuentaBancariaSocio", operator: "eq"	, value: $scope.oConfiguracionCuentaBancoSocioNegocio.seleccionado.IdCuentaBancaria || -1  });	
		filters.push({ field: "IdEstado"		, operator: "eq"		, value: $scope.oConfiguracionEstadoDocumentoTesoreria.seleccionado.IdEstado || -1  });		
		filters.push({ field: "IdCaja"			, operator: "eq"		, value: $scope.oConfiguracionCaja.seleccionado.IdCaja || -1  });		
		filters.push({ field: "IdMoneda"		, operator: "eq"		, value: $scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda || -1  });
		filters.push({ field: "IdTipoPersona"	, operator: "eq"		, value: $scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio || -1  });
		filters.push({ field: "IdChequera"		, operator: "eq"	, value: $scope.oConfiguracionChequerasSeries.seleccionado.IdChequera || -1  });
	
		
		let filtrosGrid = []
		if($scope.ApiComprobantes.dataSource.ds._filter != undefined  
		&& $scope.ApiComprobantes.dataSource.ds._filter.filters != undefined  ) {
			filtrosGrid = $scope.ApiComprobantes.dataSource.ds._filter.filters;
		}
	
		//array filters son los filtros de busqueda
		
		var hashmap = new Map();
		if(!$scope.oConfiguracionEstadoDocumentoTesoreria.seleccionado.IdEstado == 0){
			hashmap.clear();
			//pusheamos los valores blotter 
			filters.forEach(element =>{
				hashmap.set(element.field+"**"+element.operator, element)
			});
		
			filtrosGrid.forEach(element =>{
				//preguntar si ya existe valor de grilla, se respeta valor de boton buscar
				let aux = hashmap.get(element.field+"**"+element.operator);
				if(!aux){
					//pusheamos los valores grilla
					hashmap.set(element.field+"**"+element.operator, element)
				}
			});
			
			
		}else{
			hashmap.clear();
			//pusheamos los valores blotter 
			filters.forEach(element =>{
				hashmap.set(element.field+"**"+element.operator, element)
			});
		
			filtrosGrid.forEach(element =>{
				//preguntar si ya existe valor de grilla, se respeta valor de boton buscar
				let aux = hashmap.get(element.field+"**"+element.operator);
				if(!aux && element.field != "IdEstado"){
					//pusheamos los valores grilla
					hashmap.set(element.field+"**"+element.operator, element)
				}
			});
		}
		
		//limpiamos filters
		filters = new Array();
		
		//llenamos filters desde el hash
		hashmap.forEach( (value, key, map) => {
			if (value.value != -1){
				filters.push(value);
			}
		});
		
		
		dsDocumentos.filter(filters);	
		$scope.seleccionDocumentos = undefined; // limpia la selección que pudiese haber realizado anteriormente
	};
	
	$scope.limpiarFiltros = function() {
		$scope.oConfiguracionSocioNegocio.idActual = false;
		$scope.oConfiguracionMedioDePago.idActual = 0;
		$scope.oConfiguracionCaja.idActual = 0;
		$scope.oConfiguracionCajaMoneda.idActual = 0;
		$scope.oConfiguracionTipoSocioNegocio.idActual = 0;
		$scope.oConfiguracionEstadoDocumentoTesoreria.idActual = 0;
		$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.seleccionDocumentos = undefined;
		$scope.ApiComprobantes.dataSource.ds._filter.filters = undefined;
		if (VentanaModal == 'N'){
			$scope.oConfiguracionCuentaBancoCorredora.idActual = 0;
		};
		

		$scope.seleccionDocumentos = [];
	};


	//OPCIONES
	$scope.certificarDocumentos = function() {
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_CERTIFICARAN_LOS_DOCUMENTOS_SELECCIONADOS') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				var Columns = [ 'IdComprobante','IdComprobanteFormaDePago','IdTipoMovimiento','IdMedioDePago','IdModalidadDePago',
								'IdMoneda','Monto','IdCuentaBancariaCorredora','IdTarjetaBancariaCorredora','IdChequera',
								'NumeroDocumentoCorredora','FechaDocumentoPagoCorredora','IdCuentaBancariaSocio','IdBancoSocio',
								'NombreCuentaBancariaSocio','NumeroCuentaBancariaSocio','IdTipoCuentaBancariaSocio',
								'NombreTipoCuentaBancariaSocio','IdTarjetaBancariaSocio','IdTipoTarjetaBancariaSocio',
								'NombreTipoTarjetaBancariaSocio','NumeroTarjetaBancariaSocio','NumeroSerieSocio',
								'NumeroDocumentoSocio','FechaDocumentoPagoSocio','CertificadoAutomatico']		
				var arrDataFormaPago = dsDocumentos.dataColumns(Columns, $scope.seleccionDocumentos);
				if (arrDataFormaPago !== undefined){
					$.each(arrDataFormaPago, function(key,value){
						arrDataFormaPago[key].CertificadoAutomatico = null;
					});
				}


				var datos = {};
				datos.p_IdEmpresa              	= $scope.SesionIdEmpresa;
				datos.p_IdUsuario              	= $scope.SesionIdUsuario;
				datos.p_IdModulo               	= 'TES';
				datos.p_simularOperacion       	= 'N';
				datos.p_UsaReglasNegocio       	= 'S';			
				datos.p_FechaMovimiento        	= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
				datos.p_TipoEjecucion			= 'CRT';
				datos.p_FormasDePago 			= arrDataFormaPago || [];						 
				
				bcsComunes.ventanaOverlay($scope.winID, true);
				bcsServiciosTesoreria.GestionFormaDePago(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);

						if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0){	

							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);

							$scope.buscarDocumentos();
						} else {	
							bcsComunes.ventanaMsg({						
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
						}
					},
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);

						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_MODIFICAR_ESTADO_DOCUMENTO"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);				
					}
				);
			}
		});
		return;	
	};

	$scope.liquidarDocumentos = function() {
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_LIQUIDARAN_LOS_DOCUMENTOS_SELECCIONADOS') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){

				bcsComunes.ventanaOverlay($scope.winID, true);
				var dataComp = [];
				var dataCompFP = [];
				$.each($scope.seleccionDocumentos, function(index, value){
					//if (value.IdEstado === 'ING') --Incluir la logica de cuando puede liquidar
						dataComp.push({	IdComprobante: value.IdComprobante, 
										IdTipoMovimiento: value.IdComprobanteFormaDePago, 
										AfectaCuentaInversion: '' 
									  });
						dataCompFP[index] = _.pick(value,'IdComprobante','IdComprobanteFormaDePago');
				});

				var datos = {};
				datos.p_IdEmpresa               	= $scope.SesionIdEmpresa; 
				datos.p_IdUsuario               	= $scope.SesionIdUsuario; 
				datos.p_IdModulo                	= 'TES';
				datos.p_UsaReglasNegocio			= 'S';
				datos.p_FechaMovimiento				= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
				datos.p_ComprobanteLiquidar			= dataComp
				datos.p_FormaDePagoLiquidar			= dataCompFP;

				bcsComunes.log().info(JSON.stringify(datos));
				bcsServiciosTesoreria.LiquidarComprobantes(
					datos,
					function(respuesta){
						bcsComunes.ventanaOverlay($scope.winID, false);
						 		
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
							$scope.buscarDocumentos();
							
						} else {	
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
						}
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						bcsComunes.ventanaOverlay($scope.winID, false);
					}
				);
			}
		});
		return;	
	};

	
	$scope.edicionFormaDePago = function(selectRow) {
		if (selectRow.length > 1) 
		{
			bcsComunes.ventanaMsgErr($translate.instant('UI_DEBE_SELECCIONAR_UNICO_REGISTRO'),$scope.winID);
			return;
		}

		// No se puede editar anulados, bloqueados, girados, liquidados
		if (selectRow[0].IdEstado !== 'ING') {
			bcsComunes.ventanaMsgErr($translate.instant("UI_SELECCIONAR_COMPROBANTES_VALIDOS_PARA_EDICION"),$scope.winID);
			return;
		};

		var objFormaDePago = $scope.seleccionDocumentos;
		var ventanaFormaDePago = bcsComunes.ventanaAbrir("EditarComprobanteFormaDePago", objFormaDePago,false, false, $scope.winID);
		
		var promesa = bcsComunes.ventanaPromesa(ventanaFormaDePago);

		promesa.then(function(data) {
			$scope.buscarDocumentos();
		});
	};
	
	
	$scope.ingresarDepositos = function() {
		var ventanaDocumentosDepositar = bcsComunes.ventanaAbrir("IngresoDeposito", {}, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(ventanaDocumentosDepositar);
		promesa.then(function(data){
			$scope.buscarDocumentos();
		});
	};
	$scope.$on('$viewContentLoaded', function() {
		$timeout(function() {
		  var iconoConfiguracion = angular.element('bcs-grid#grid-documentos-tesoreria > span > div.bcsgridconf');
		  iconoConfiguracion.css('display', 'block');
		});
	  });
});