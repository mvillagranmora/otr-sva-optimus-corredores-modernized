angular.module('ModuloTesoreria').controller('ConsultarIngresoEgresoCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,bcsServiciosTesoreria,bcsAlerta,OPCIONES_BCSALERTA,
				$translate,bcsServicioPlataforma,bcsBlotterComprobantesGrid,$http)
{

	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var IdEmpresa  = $scope.SesionIdEmpresa;

	//Seteo de rango de fechas según fecha de sistema
	$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
	$scope.btnActivo = false;
	$scope.bloquearDesbloquear = false;  //FALSE = Desbloqueado;  TRUE =  Bloqueado
	$scope.seleccionarDeseleccionar = false;

	
	//Consulta TipoEmpleado  //BACKOFFICE (empleados que no son ejecutivos ni supervisores) //Los ejecutivos no tienen una caja asociada
	var TipoEmpleadoDatasource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport	: {read:  {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1752",dataType: "json"}},
			schema 		: {model: {fields : {TipoEmpleado		: { type: "string"}}}},		
			filter		: [{ "field": "IdUsuario",	"operator": "eq", "value" : $scope.SesionIdUsuario}]
		}
	});
	
	TipoEmpleadoDatasource.query().then(function(){
		if (typeof TipoEmpleadoDatasource.data()[0] !== 'undefined')
		{
			$scope.TipoEmpleado = TipoEmpleadoDatasource.data()[0].TipoEmpleado;
		}
	}); 
	
	
	//Configuración combo Caja
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
			IdMoneda : { "field"   : "IdMoneda","operator": "neq", "value"   : "" },
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''			
	};	
	
	//Configuración combo Tipo Movimiento (Ingreso/Egreso)
	$scope.oConfiguracionTipoMovimiento={
		tipo : "combobox-tipo-movimiento",
		configuracion : {},
		filtros : {
			IdModulo      : { "field"   : "IdModulo","operator": "eq", "value"   : "TES" },				
			IdEstado      : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.oConfiguracionTipoSocioNegocio={
		tipo 			: "combobox-tipo-socio-negocio",
		configuracion 	: {},
		filtros 		: {},
		idActual 		: false,
		seleccionado 	: false,
		idDefecto 		: '',
		idPostFiltro  	: false,		
		activado        : true
	};
	//Filtros para combo Concepto Tesoreria
	$scope.FiltroNombreEntidad		   		= { "field" : "NombreEntidad",        "operator": "eq", "value" : "TES.IngresoEgreso" };
	$scope.FiltroIdEstado			   		= { "field" : "IdEstado",             "operator": "eq", "value" : "VIG" };
	$scope.FiltroDesplegarEnComboBox   		= { "field" : "DesplegarEnComboBox",  "operator": "eq", "value" : "S" };
	$scope.FiltroDisponibleIngresoManual   	= { "field" : "DisponibleIngresoManual",  "operator": "eq", "value" : "S" };
	$scope.FiltroTipoConceptoTesoreria 		= { "field" : "TipoConceptoTesoreria","operator": "contains", "value" : "IN,EG" };  //IN|EG
	$scope.FiltroTipoConceptoTesoreriaVacio = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "" };  //vacio es para IN y EG 
	$scope.FiltroTipoConceptoTesoreriaAmbos = { "field" : "TipoConceptoTesoreria","operator": "eq", "value" : "AM" }; //
	$scope.VisibleOptimus   	            = { "field" : "VisibleOptimus",  "operator": "eq", "value" : "S" };
	$scope.oConfiguracionConcepto={
			tipo 			: "combobox-concepto-tesoreria",
			configuracion 	: {},			
			filtros : {
				complejo : [$scope.FiltroNombreEntidad,$scope.FiltroIdEstado,$scope.FiltroDesplegarEnComboBox,
				            $scope.FiltroDisponibleIngresoManual, $scope.VisibleOptimus
				            ],				
			  	logic: "and",
			  	filters: [
				{
				    logic: "or",
				    filters: [$scope.FiltroTipoConceptoTesoreria, $scope.FiltroTipoConceptoTesoreriaVacio, $scope.FiltroTipoConceptoTesoreriaAmbos]
				},
				$scope.FiltroNombreEntidad,
				$scope.FiltroIdEstado,
				$scope.FiltroDesplegarEnComboBox,
				$scope.FiltroDisponibleIngresoManual,
				$scope.VisibleOptimus
			  ]},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''
	};	

	
	
	
	$scope.oConfiguracionEstadoTesoreria={
		tipo : "combobox-estado-tesoreria",
		configuracion : {},
		filtros : {
			NombreEntidad : {"field": "NombreEntidad","operator": "eq", "value"   :  'TES.MovimientoIngresoEgreso'}				
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'ING'		
	};
	$scope.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEstado : { "field"   : "IdEmpresa","operator": "eq", "value"   :  IdEmpresa }
		},
	};
	
	
	$scope.oConfiguracionPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 }
		},
	};	

	$scope.oConfiguracionClasificacionTipoInstrumento = {
		tipo: "combobox-clasificacion-tipo-instrumento",
		cascadaValor: "IdClasificacionTipoInstrumento",
		seleccionado: false,
		idActual: false,
		filtros: {
			IdClasificacionTipoInstrumento: { "field" : "IdClasificacionTipoInstrumento", "operator" : "neq", "value" : "IR" }
		}
	};
	
	$scope.oConfiguracionTipoInstrumento = {
		tipo: "combobox-tipo-instrumento",
		cascadaValor: "IdTipoInstrumento",
		seleccionado: false,
		idActual: false,
		filtros: {
			IdClasificacionTipoInstrumento: { "field" : "IdClasificacionTipoInstrumento", "operator" : "eq", "value" : "" }
		}
	};

	//GRILLA PRINCIPAL
	dsMovimientosIngresosEgresos = new bcs.DataSource(bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgreso);
	$scope.confBasicaIngresoEgreso = {
		dataSource			: dsMovimientosIngresosEgresos,
		columns				: bcsBlotterComprobantesGrid.columnasIngresosEgresos(),		
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 470,
		selectable 			: "multiple row",
		userSettings		: true,
		navigatable         : true,
		gridCreated 		: function(g) { 
								$scope.ApiMovimientosIngresosEgresos = g;
																
								//Busqueda Inicial
								$scope.oConfiguracionEstadoTesoreria.seleccionado = {};
								$scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado = 'ING';

								if(sesion.getPreferencias('recordarCliente')){
									$scope.oConfiguracionSocioNegocio.seleccionado = {};
									$scope.oConfiguracionSocioNegocio.seleccionado.IdPersona = sesion.getPreferencias('IdPersonaRecordado');
								}
								
								$scope.buscarMovimientos();
							  },
		rowColorBackground	: function(row) { 
								var color = "";

								if (row.IdEstadoAutorizacion == 'VIG')
									color = "yellow";
								if (row.IdEstadoAutorizacion == 'AUT' && row.IdEstado != 'EMI')
									color = "green";								
								if (row.IdEstado === 'ANU')
									color = "red";
								return color;
							  }
	};	
	$scope.confBasicaIngresoEgresoFormaPago = function(item) {
		var dsDef = bcsBlotterComprobantesGrid.dsDefMovimientoIngresoEgresoFormaDePago;
		dsDef.definition.filter = 
		[
		    { field: "IdIngresoEgreso", operator: "eq",  value: item.IdIngresoEgreso },
		    { field: "IdEstado",        operator: "neq", value: "MOD" }
		] 
		return {
			dataSource			: new bcs.DataSource(dsDef),
			columns				: bcsBlotterComprobantesGrid.columnasIngresoEgresoFormaDePago(),		
			sortable			: true,
			pageable			: true,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row",
			navigatable         : true,
			userSettings		: true
		};
	};
	
	$scope.buscarMovimientos = function(){
		var filters = new Array();
				
		if ($scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento && $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== 0){
			filters.push({ field: "IdTipoMovimiento", operator: "eq", value: $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento });		
		}else {filters.push({ field: "IdTipoMovimiento", operator: "eq", value: -1 });}

		filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento || -1 });	
		filters.push({ field: "IdTipoInstrumento", operator: "eq", value: $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento || -1 });	

		filters.push({ field: "IdPersona", operator: "eq", value: Number($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona || -1) });	
		filters.push({ field: "IdPortafolioTitularPago", operator: "eq", value: Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || -1)});
		
		filters.push({ field: "IdCaja", operator: "eq", value: $scope.oConfiguracionCaja.seleccionado.IdCaja || -1 });

		filters.push({ field: "IdMoneda", operator: "eq", value: $scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda || -1});

		if ($scope.oConfiguracionConcepto.seleccionado.IdConcepto && $scope.oConfiguracionConcepto.seleccionado.IdConcepto != 0){
			filters.push({ field: "IdConcepto", operator: "eq", value: $scope.oConfiguracionConcepto.seleccionado.IdConcepto });
		}else {filters.push({ field: "IdConcepto", operator: "eq", value: -1 });}

		
		filters.push({ field: "IdEstado", operator: "eq", value: $scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado|| -1 });				
		
		filters.push({ field: "FechaLiquidacion", operator: "gte", value: util.dateOdata($scope.kngFechaLiquidacionDesde)|| -1 });
		
		filters.push({ field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.kngFechaLiquidacionHasta)|| -1 });
		
		filters.push({ field: "IdTipoPersona", operator: "eq", value: $scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio || -1});	
		
		
		
		let filtrosGrid = []
		if($scope.ApiMovimientosIngresosEgresos.dataSource.ds._filter != undefined  
		&& $scope.ApiMovimientosIngresosEgresos.dataSource.ds._filter.filters != undefined  ) {
			filtrosGrid = $scope.ApiMovimientosIngresosEgresos.dataSource.ds._filter.filters;
		}
	
		//array filters son los filtros de busqueda
		
		var hashmap = new Map();
		if(!$scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado == 0){
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
		
		
		dsMovimientosIngresosEgresos.filter(filters);		
	};
	function onChange(arg) {
		var selected = $.map(this.select(), function(item) {
			return $(item).text();
		});

		console.log("Selected: " + selected.length + " item(s), [" + selected.join(", ") + "]");
	};
	
	
	
	
	
	
	
	$scope.generarExportarReporte = function(configReporte)
	{
		if (configReporte.IdReporte == '' || configReporte.NombreReporte == '' || configReporte.IdServicio == '' || configReporte.FormatoSalida == '') 
		{
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.msg("error", 'Error', $translate.instant("UI_REPORTE_SIN_CONFIGURACION"));
			return false;
		}
	 
		bcsComunes.ventanaOverlay($scope.winID, true);
			// Genera el nombre del reporte para descarga local
		    var fecha = new Date ();
			var nombreArchivoDescarga = configReporte.CodigoReporte + "_" + String(fecha.getFullYear()) + String(fecha.getMonth() + 1) + String(fecha.getDate())+ "_" + String(fecha.getHours()) + String(fecha.getMinutes()) + String(fecha.getSeconds())+"."+configReporte.FormatoSalida;

			var parameters = {

				"reportId" : configReporte.IdReporte,
				"exportFormat" : configReporte.FormatoSalida,
				"parameters" : "",
				"configuration" : {
					"mainReport" : {
						"data" : {
							"serviceId" : configReporte.IdServicio,
							"serviceData" : {
								"P_IDEMPRESA" 		: Number($scope.SesionIdEmpresa),
								"P_IDTIPOMOVIMIENTO": $scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento==0?"":$scope.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento ,
								"P_IDPERSONA" 		:Number($scope.oConfiguracionSocioNegocio.seleccionado.IdPersona || 0),
								"P_IDPORTAFOLIOTITULARPAGO":Number($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0),
								"P_IDCAJA" :$scope.oConfiguracionCaja.seleccionado.IdCaja,
								"P_IDMONEDA" :$scope.oConfiguracionCajaMoneda.seleccionado.IdMoneda||"",
								"P_IDCONCEPTO":$scope.oConfiguracionConcepto.seleccionado.IdConcepto=="0"?"": $scope.oConfiguracionConcepto.seleccionado.IdConcepto,
								"P_IDESTADO":$scope.oConfiguracionEstadoTesoreria.seleccionado.IdEstado || "" ,
								"P_FECHADESDE":util.dateSPdate($scope.kngFechaLiquidacionDesde),
								"P_FECHAHASTA":util.dateSPdate($scope.kngFechaLiquidacionHasta),
								"P_IDTIPOPERSONA":$scope.oConfiguracionTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio || "",	
								"$schema" : [ {
									"field" : "LogoCorredora",
									"type" : "blob"
								} ]
							},
						},
						"parameters" : {},
						"reportName" : configReporte.NombreReporte
					},
					"subReports" : []
				},
			};

			$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file',parameters,{"responseType" : "arraybuffer"
							}).success(function(response) {
								var file = new Blob([ response ],{type : 'application/pdf'});
								saveAs(file,nombreArchivoDescarga);
								bcsComunes.ventanaOverlay($scope.winID,false);
								bcsComunes.msg("info","info",$translate.instant("UI_PDF_COMPROBANTE_GENERADO"));
							}).error(
							function(response) {
								bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
								bcsComunes.ventanaOverlay($scope.winID,false);
							});

			
			return;
			
	}
	
	$scope.exportarReporte = function(){
		
		var consultarTemplateTesoreria = new bcs.DataSource( 
				{
					type : "odata", // Los tipos posibles son: odata, call, array y object
					definition : {
						serverFiltering : true, // Filtrado de datos en el servidor
						transport : {
							read : {
								url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1492",
								dataType : "json",
							}
						},

						filter : [ 	{field : "CodigoTemplateTesoreria",operator : "eq",value : 'MOVTES'}, 
									{field : "IdEstado",operator : "eq",value : 'ACT'}, 
									{field : "IdEmpresa",operator : "eq",value : $scope.SesionIdEmpresa},
								 ],
						
						schema : {
							model : {
								fields : {
									IdReporte : {type : "string"},
									IdServicio : {type : "number"},
									RutaServicio : {type : "string"}, 
									NombreReporte : {type : "string"},
									FormatoSalida : {type : "string"},
								}
							}
						}

					},
				});

		consultarTemplateTesoreria.fetch().then(function() {
							if (consultarTemplateTesoreria.data().length > 0) {
								$scope.$apply(function() {
											$.each(consultarTemplateTesoreria.data(),function(key,registro) {
																$scope.ConfiguracionReporteDetalleComprobante = {
																				IdReporte : registro.IdReporte,
																				CodigoReporte :registro.CodigoTemplateTesoreria,
																				NombreReporte : registro.NombreReporte,
																				IdServicio : registro.IdServicio,
																				FormatoSalida : registro.FormatoSalida,
																				RutaServicio : registro.RutaServicio															
																};

																$scope.generarExportarReporte($scope.ConfiguracionReporteDetalleComprobante);
																
															});
											return true;
										});
							} else {
								return false;
							}
						});
		
		

		return;
		
	};	

	
	
	
	
	$scope.limpiarFiltros = function()
	{	
		$scope.oConfiguracionTipoMovimiento.idActual 						= 0;
		$scope.oConfiguracionCaja.idActual 									= 0;
		$scope.oConfiguracionCajaMoneda.idActual 							= 0;			
		$scope.oConfiguracionEstadoTesoreria.idActual 						= 0;
		$scope.oConfiguracionSocioNegocio.idActual							= false;
		$scope.oConfiguracionConcepto.idActual 								= 0;
		$scope.oConfiguracionTipoSocioNegocio.idActual 						= 0;
		$scope.oConfiguracionClasificacionTipoInstrumento.idActual 			= 0;
		$scope.oConfiguracionTipoInstrumento.IdClasificacionTipoInstrumento = 0;
		$scope.kngFechaLiquidacionDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.kngFechaLiquidacionHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		dsMovimientosIngresosEgresos.removeAll();

		$scope.seleccionIngresoEgreso = [];
		$scope.seleccionIngresoEgresoFormaPago = [];
		$scope.ApiMovimientosIngresosEgresos.dataSource.ds._filter.filters = [];
	};
   	
	$scope.ParametrosServicios = function(SP){
		var dataCols = [];

		switch (SP){
			case "Generar":
				$.each($scope.seleccionIngresoEgreso, function(index, value){
					if (value.IdEstado === 'ING')
						dataCols[index] = {'NumeroReferencia':0, 'IdIngresoEgreso': value.IdIngresoEgreso, 'TipoComprobante': ''}; //_.pick(value,0,'IdIngresoEgreso');
				});
				break;
			case "Certificar":
				$.each($scope.seleccionIngresoEgreso, function(index, value){
					//if (value.IdEstado === 'ING')
						dataCols[index] = _.pick(value,'IdIngresoEgreso');
				});
				break;
				
			case "Anular":				
				$.each($scope.seleccionIngresoEgreso, function(index, value){
					dataCols.push({Id : value.IdIngresoEgreso, Entidad  : 'TES.IngresoEgreso'})
				});
				break;
				
			case "Bloquear":
				$.each($scope.seleccionIngresoEgreso, function(index, value){
					dataCols.push({Id: value.IdIngresoEgreso, Entidad: 'TES.IngresoEgreso', Accion: 'BLO' });
				});
				break;
			case "Desbloquear":
				$.each($scope.seleccionIngresoEgreso, function(index, value){
					dataCols.push({Id: value.IdIngresoEgreso, Entidad: 'TES.IngresoEgreso', Accion: 'DES' });
				});
				break;
		}
		
		if (dataCols.length > 0){				
			var datos = {};
			datos.p_IdEmpresa               = $scope.SesionIdEmpresa; 
			datos.p_IdUsuario               = $scope.SesionIdUsuario; 
			datos.p_IdModulo                = 'TES';
			datos.p_UsaReglasNegocio		= 'S';
			datos.p_FechaMovimiento			= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
			datos.p_RegistrosAProcesar		= dataCols;

			return datos;
		} else {
			bcsComunes.ventanaOverlay($scope.winID, false);
			bcsComunes.msg("warning","warning",$translate.instant('UI_SELECCIONAR_MOVIMIENTOS_VALIDOS'));
		}
	};


	//OPCIONES
	$scope.generarComprobantes = function() {
		$scope.btnActivo = false;
		var data = dsMovimientosIngresosEgresos.dataColumns(
						["NombrePersona","IdMoneda","IdTipoMovimiento","NombreTipoMovimiento","Monto"],
						$scope.seleccionIngresoEgreso
					);

		cerrarventanasPorNombre('VistaPreviaComprobante');

		var vistaPrevia = bcsComunes.ventanaAbrir("VistaPreviaComprobante", data, false, false);
		var promesa = bcsComunes.ventanaPromesa(vistaPrevia);
		promesa.then(function(data){
			if (data === "true"){
				bcsComunes.ventanaOverlay($scope.winID, true);
				var datos = $scope.ParametrosServicios('Generar');
				if (datos != undefined){

					var data = _.omit(datos, 'p_RegistrosAProcesar');
					data.p_GenerarComprobanteDesdeMovimientos 	= 'S';					
					data.p_IngresoEgresoGenerar					= datos.p_RegistrosAProcesar;
					data.p_ComprobanteGenerar					= [];
					data.p_FormasDePago							= [];
					bcsComunes.log().info(JSON.stringify(data));
					bcsServiciosTesoreria.CrearComprobante(
						data,
						function(respuesta){
							bcsComunes.ventanaOverlay($scope.winID, false);

							if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_EXITO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);
								$scope.buscarMovimientos();
							} else {	
								if ($scope && $scope.ApiMovimientosIngresosEgresos && typeof $scope.ApiMovimientosIngresosEgresos.clearSelection === 'function') {
    								$scope.ApiMovimientosIngresosEgresos.clearSelection();
								}
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
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
			}
		});
		$scope.btnActivo = true;
		return;
	};
	
		

	// Permite generar una partida de ingresos o egresos a partir de uno o varios movimientos	
	$scope.crearPartidaIngresoEgreso = function(tipoMovimiento) {
		var ingresoEgresoGenerar = [];
		var mensajeErrorValidacion = "";
		
		bcsComunes.ventanaOverlay($scope.winID, true);

		//Recorre row seleccionados
		$.each($scope.seleccionIngresoEgreso,function(key,value){
			ingresoEgresoGenerar.push({IdIngresoEgreso: $scope.seleccionIngresoEgreso[key].IdIngresoEgreso});
		});

		//Personaliza mensaje de error de validaciones según topo de partida (ingreso/egreso)
		if (tipoMovimiento === 'PARING')		
			mensajeErrorValidacion = 'UI_MOVIMIENTOS_NO_CUMPLEN_REQUISITOS_PARA_PARTIDA_INGRESOS';		
		else
			mensajeErrorValidacion = 'UI_MOVIMIENTOS_NO_CUMPLEN_REQUISITOS_PARA_PARTIDA_EGRESOS';

	
		var paramValidaciones = {};
		paramValidaciones.p_TipoMovimiento = tipoMovimiento;
		paramValidaciones.p_IngresoEgresoGenerar = ingresoEgresoGenerar;

		//Valida que los movimientos cumplan las condiciones para generar una partida de ingreso o egreso
		bcsServiciosTesoreria.CumpleRequisitosCrearPartidaDesdeMov(paramValidaciones, 
			function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR == "0")
				{	
					var paramVentanaPartida = [];

					paramVentanaPartida.push({
						ingresoEgresoGenerar: ingresoEgresoGenerar,
						Monto				: respuesta.P_OUT_MONTO,
						IdMoneda			: respuesta.P_OUT_IDMONEDA,
						NombreMoneda		: respuesta.P_OUT_NOMBREMONEDA,
						FechaLiquidacion	: util.dateInicioDia(respuesta.P_OUT_FECHALIQUIDACION),
						IdConcepto			: respuesta.P_OUT_IDCONCEPTO,
						NombreConcepto		: respuesta.P_OUT_NOMBRECONCEPTO,
						IdCaja				: respuesta.P_OUT_IDCAJA,												
						IdTipoMovimiento	: (tipoMovimiento === 'PARING') ? 'MOVING' : 'MOVEGR',   // MOVING | MOVEGR					
						TipoMovimiento		: tipoMovimiento     // PARING | PAREGR
						,TipoEmpleado       : $scope.TipoEmpleado
					});
					
					var ventanaCrearPartidaIngresoEgreso = bcsComunes.ventanaAbrir("PartidaIngresoEgreso",paramVentanaPartida,false,false);
					var promesa = bcsComunes.ventanaPromesa(ventanaCrearPartidaIngresoEgreso);
					promesa.then(function(data){
						$scope.buscarMovimientos();
					});
				}
				else 
				{	
					bcsComunes.ventanaMsg({
						mensaje: $translate.instant(mensajeErrorValidacion),
						conflictos: util.parseServerResponse(respuesta)
					},$scope.winID);
				}

				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje: $translate.instant(mensajeErrorValidacion),
					conflictos: util.parseServerResponse(respuesta)
				},$scope.winID);				

				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);	
	};	
	
	
	
	
	$scope.crearIngresoEgreso = function(){
		bcsComunes.ventanaAbrir("CrearIngresoEgreso", {oIngresoEgreso: null},false,false )
		
	};

	
	$scope.editarIngresoEgreso = function(datos){		
		if (datos.length > 1)
		{
			bcsComunes.ventanaMsgErr($translate.instant("UI_DEBE_SELECCIONAR_UNICO_REGISTRO"),$scope.winID);
			return;
		}

		//No se puede editar movimientos anulados, bloqueados, emitidos
		if (datos[0].IdEstado !== "ING")
		{
			bcsComunes.ventanaMsgErr($translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS_PARA_EDICION"),$scope.winID);
			return;			
		}
		
		var idVentanaEdicion = bcsComunes.ventanaAbrir("EditarIngresoEgreso", {oIngresoEgreso: datos[0]},false,false )		
		var promesa = bcsComunes.ventanaPromesa(idVentanaEdicion);			

		promesa.then(function(data){ 
			if (data === true)
			{	
				//Actualiza grilla con los cambios
				$scope.buscarMovimientos();
			}
		});
	};

	
	$scope.anularIngresoEgreso = function() {
		var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ANULARAN_LOS_MOVIMIENTOS_SELECCIONADOS') );
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR){
				bcsComunes.ventanaOverlay($scope.winID, true);
				
				var datos = $scope.ParametrosServicios('Anular');
				if (datos != undefined){
					bcsComunes.log().info(JSON.stringify(datos));
					bcsServiciosTesoreria.AnularRegistros(
						datos,
						function(respuesta){
							bcsComunes.ventanaMsg({
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);
							bcsComunes.ventanaOverlay($scope.winID, false);
							if (respuesta.P_OUT_ERRORESCONTADOR == "0"){
								$scope.buscarMovimientos();
							}						
						},
						error
					);
				}
			}
		});
		return;	
	};
	
	$scope.bloquearDesbloquearIngresoEgreso = function(){
		if (!bcsComunes.validarAcceso('ConsultarIngreso_BloquearDesbloquear')) return;
		if (!$scope.seleccionIngresoEgreso || $scope.seleccionIngresoEgreso.length === 0) {
	        bcsComunes.ventanaMsgErr($translate.instant("UI_SELECCIONAR_MOVIMIENTO"), $scope.winID);
	        return; 
	    }
		
		bcsComunes.ventanaOverlay($scope.winID, true);		

		var opcion = '';
		$scope.seleccionIngresoEgreso[0].IdEstado === 'BLO' ? (opcion = 'Desbloquear') : (opcion = 'Bloquear');


		var datos = $scope.ParametrosServicios( opcion );
		datos.p_Entidad			= 'TES.IngresoEgreso';
		datos.p_FormasDePago	= [];

		if (datos != undefined){

			bcsServiciosTesoreria.EdicionMultiple(
				datos,
				function(respuesta){
					bcsComunes.ventanaOverlay($scope.winID, false);
					$scope.seleccionIngresoEgreso = [];
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);

					$scope.buscarMovimientos();
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
	};

	$scope.seleccionarDeseleccionarTodosMovimientos = function(){
		//seleccionar
		$scope.ApiMovimientosIngresosEgresos.select( $scope.ApiMovimientosIngresosEgresos.tbody.find(">tr"));

		//deseleccionar
		if (!!$scope.seleccionIngresoEgreso && $scope.seleccionIngresoEgreso.length > 0 ){
			$scope.ApiMovimientosIngresosEgresos.clearSelection();
		}
	};


	
	$scope.$watch('oConfiguracionTipoSocioNegocio.seleccionado',function(newval,oldval)
	{
		if ( _.isUndefined(newval) || newval === false) return;
		
		if (newval.CodigoTipoSocioDeNegocio === 'CLI' || newval.CodigoTipoSocioDeNegocio === 0)
		{
			$scope.oConfiguracionPortafolio.activado = true;
		}
		else //Para: PRO(proveedor)  CNT(Contraparte)  EMP(Empleado)
		{
			$scope.oConfiguracionPortafolio.idActual = 0;
			$scope.oConfiguracionPortafolio.activado = false;
		}
	});

	
	$scope.$watch('oConfiguracionSocioNegocio.seleccionado',function(newval,oldval)
	{
		if ( _.isUndefined(newval) || newval === false) return;
		
		if ($scope.oConfiguracionTipoSocioNegocio.seleccionado.CodigoTipoSocioDeNegocio === 'CLI' || $scope.oConfiguracionTipoSocioNegocio.seleccionado.CodigoTipoSocioDeNegocio === 0)
		{
			$scope.oConfiguracionPortafolio.activado = true;
		}
		else //Para: PRO(proveedor)  CNT(Contraparte)  EMP(Empleado)
		{
			$scope.oConfiguracionPortafolio.idActual = 0;
			$scope.oConfiguracionPortafolio.activado = false;
		}
	});	
	
	
	
	$scope.$watch('seleccionIngresoEgreso',function(newval,oldval)
	{
		if ( _.isUndefined(newval) ) return;
		
		var countMovimientosInvalidos = 0;
		for (i=0;i<newval.length;i++)
		{	
			if (newval[i].IdEstado !== "ING") countMovimientosInvalidos++;
		}; 
				
		if (newval.length > 0)
			if (countMovimientosInvalidos == newval.length)			
				$scope.btnActivo = false;  //Si todos los movimientos seleccionado son inválidos(EMI/ANU/BLO), se desactivan los botones
			else
				$scope.btnActivo = true;   //Si uno de los movimiento es válido, se activan los botones
		else
			$scope.btnActivo = false;
	});	
	
	$scope.$watch('oConfiguracionTipoMovimiento.seleccionado',function(newval,oldval){
		if (typeof $scope.oConfiguracionTipoMovimiento.seleccionado !== 'undefined' && $scope.oConfiguracionTipoMovimiento.seleccionado.seleccionado !== false)
		{
			
			//Se actualizan filtros para conceptos
			$scope.FiltroTipoConceptoTesoreria.value = $scope.oConfiguracionTipoMovimiento.seleccionado.Tipo==null?"":$scope.oConfiguracionTipoMovimiento.seleccionado.Tipo;  // IN | EG 
					
		}
	});

	
	$scope.edicionMultiple = function() {
		var data = dsMovimientosIngresosEgresos.dataColumns(["IdIngresoEgreso"],$scope.seleccionIngresoEgreso);
		data = _.map(data,function(info) { return { Id : info.IdIngresoEgreso } });

		var j = 0;
		var IdTipoMovimiento = '';
		var IdMedioDePago	= '';
		var IdMoneda	= '';		
		var countEstadoInvalidoParaEdicion	= 0;

		for (i=0;i<data.length;i++)
		{
			if (IdTipoMovimiento !== $scope.seleccionIngresoEgreso[i].IdTipoMovimiento)
			{				
				if (IdTipoMovimiento !== "") j++;
				IdTipoMovimiento = $scope.seleccionIngresoEgreso[i].IdTipoMovimiento;
			}
			
			if (IdMoneda !== $scope.seleccionIngresoEgreso[i].IdMoneda)
			{				
				if (IdMoneda !== "") j++;
				IdMoneda = $scope.seleccionIngresoEgreso[i].IdMoneda;
			}
			
			if ($scope.seleccionIngresoEgreso[i].IdEstado !== "ING") countEstadoInvalidoParaEdicion++;				
			
		};
		
		//No se puede editar movimientos anulados, bloqueados, emitidos
		if (countEstadoInvalidoParaEdicion > 0)
		{	                         
			bcsComunes.ventanaMsgErr($translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS_PARA_EDICION"),$scope.winID);
			return;
		};
		
		if (j > 0)
		{
			bcsComunes.ventanaMsgErr($translate.instant("UI_SELECCIONAR_MOVIMIENTOS_VALIDOS"),$scope.winID);
			return;
		};

		var datos = [];		
		datos.push({"Entidad": 'TES.IngresoEgreso' });
		datos.push(data);

		var edicionMultiple = bcsComunes.ventanaAbrir("EdicionMultipleTesoreria", datos, false, false, $scope.winID);
		var promesa = bcsComunes.ventanaPromesa(edicionMultiple);
		promesa.then(function(data){
			if (data === 'true'){
				$scope.buscarMovimientos();	
			}			
		});
	}
	
	
	var error = function(respuesta)
	{	
		bcsComunes.ventanaMsg({
			mensaje: $translate.instant('UI_PROCESO_REALIZADO_CON_ERROR'), 
			conflictos: util.parseServerResponse(respuesta)}
		,$scope.winID);
		bcsComunes.ventanaOverlay($scope.winID, false);	
	};
	

	$scope.detalleIngresoEgreso = function(datos){		
		if (datos[0].IdModulo == 'FAC')
			bcsComunes.ventanaAbrir("DetalleComprobanteFacturacion", {IdComprobante: datos[0].IdComprobanteOrigen}, false, false, $scope.winID );		
		if (datos[0].IdModulo == 'TES')
			bcsComunes.ventanaAbrir("DetalleIngresoEgresoTesoreria", {IdIngresoEgreso: datos[0].IdIngresoEgreso}, false, false,$scope.winID );
		if (datos[0].IdModulo == 'FMM')
			bcsComunes.ventanaAbrir("ResumenCartaLiquidacion", {FolioCartaLiquidacion: datos[0].IdComprobanteOrigen}, false, false, $scope.winID);			
	};

	$scope.$on('$viewContentLoaded', function() {
		$timeout(function() {
		  var iconoConfiguracion = angular.element('bcs-grid#grid-ingreso-egreso > span > div.bcsgridconf');
		  iconoConfiguracion.css('display', 'block');
		});
	  });

	function cerrarventanasPorNombre (winKey){
		$("[winkey='" + winKey + "']").each(function() {
			bcsComunes.ventanaCerrar($(this).attr('ventanaid'));
		});
	};
});
