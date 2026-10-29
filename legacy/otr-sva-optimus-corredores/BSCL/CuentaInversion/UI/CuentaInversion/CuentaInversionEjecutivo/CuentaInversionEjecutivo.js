angular.module('ModuloCuentaInversion').controller('CuentaInversionEjecutivoCtrl',
function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope,bcsAlerta,OPCIONES_BCSALERTA,
			bcsServiciosCuentaInversion, bcsCuentaInversionGrid, bcsServicioPlataforma,bcsFormatoDocumentosCuentaInversion)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.grid = {};
	$scope.filtros = {};
	
	var ResultadoSaldos = [];
	var gridSaldosCuentas;
	var gridMovimientosCuentaInversion;
	$scope.msjExito = '';
	$scope.msjError = '';
	$scope.msjAdvertencia = '';
	
	//SETEO INICIAL	
	$scope.filtros.kngFechaEfectivaDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaEfectivaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaMovimientoDesde = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaMovimientoHasta = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.ngSoloClientesConSaldoDisponible	= false;
	$scope.filtros.ngMostrarSoloDAPVencidos = false;
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise('CUS.CertCargoAbonoVencimientosDAP',$scope.SesionIdEmpresa,'CUSTODIA')
    			.then(
					function(parametro){
        				$scope.filtros.ngMostrarSoloDAPVencidos = parametro && parametro.Valor === 'S';
        				
        				if (!$scope.filtros.ngMostrarSoloDAPVencidos) {
        					$scope.filtros.ngSoloDAPVencidos = false;
    					}
    				}
    			);
	
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.filtros.oConfiguracionEjecutivo = {
		tipo : "combobox-ejecutivo",
		configuracion : {},
		filtros : {},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.filtros.oConfiguracionCuentaInversion = {
		tipo : "combobox-moneda-tesoreria", //Muestras las monedas que estan configuradas en Tesoreria.
		configuracion : {},
		filtros : {},
		idActual : false,
		idDefecto : $scope.SesionIdMonedaSistema || '',
		seleccionado : false,					
	};
	
	$scope.filtros.oConfiguracionPortafolio = {
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,		
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	
	$scope.filtros.oConfiguracionCuentaInversionCliente = {
		tipo : "combobox-cuenta-inversion",
		configuracion : {},
		filtros : {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   : 0 },
			IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 },
			EstadoCuentaInversion : { "field"   : "IdEstadoCuentaInversion","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.filtros.oConfiguracionEstado = {
		tipo : "combobox-estado-cuenta-inversion",
		configuracion : {},
		filtros : {
			//IdEstado 	: {field   : "IdEstado", operator: "neq", value   : "ANU"},
			Nombre 		: {field   : "NombreEntidad", operator: "eq", value   : "CTA.CargoAbonoMovimiento"}
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.filtros.oConfiguracionTipoMovimiento = {
		tipo : "combobox-tipo-movimiento",
		configuracion : {},
		filtros : {
			IdModulo : { "field"   : "IdModulo","operator": "eq", "value"   : "CTA" },
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.filtros.oConfiguracionConcepto = {
		tipo : "combobox-concepto-cta-inv",
		configuracion : {},
		filtros : {
			DesplegarEnCombobox	: { "field"   : "DesplegarEnCombobox","operator": "eq", "value"   : "S" },
			TipoConcepto 		: { "field"   : "TipoConcepto","operator": "eq", "value"   : "" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};
	
	$scope.filtros.oConfiguracionSaldoPortafolio = _.clone($scope.filtros.oConfiguracionPortafolio);  // copia la estructura de oConfiguracionPortafolio
	$scope.filtros.oConfiguracionSaldoCuentaInversion = _.clone($scope.filtros.oConfiguracionCuentaInversion);
	$scope.filtros.oConfiguracionCuentaInversionCliente2 = _.clone($scope.filtros.oConfiguracionCuentaInversion);
	
	$scope.oSocioNegocioSaldo = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}
	};
	
	$scope.oProductoSaldo = {
		filtros: {
			IdProductoAgrupador : {"field": "IdProductoAgrupador","operator": "neq", "value"   :  0 }
		}		
	};
	
	$scope.oPortafolioSaldo = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		}
	};
	
	$scope.oSocioNegocioMovimiento = _.clone($scope.oSocioNegocioSaldo);
	$scope.oProductoMovimiento = _.clone($scope.oProductoSaldo);
	$scope.oPortafolioMovimiento = _.clone($scope.oPortafolioSaldo);
	
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	var dsSaldosCuentas = new bcs.DataSource(bcsCuentaInversionGrid.dsDefSaldosCuentas);
	$scope.grid.confSaldosCuentas = {
		dataSource  : dsSaldosCuentas,
		columns 	: bcsCuentaInversionGrid.columnasSaldosCuentasEjecutivo(),
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 470,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		excel 		: { allPages: true },
		
		gridCreated : function(g) { 
			gridSaldosCuentas = g;
			g.hideColumn("Producto");         // Oculta columna ya que es para APV
		},
		
	};
	
	var dsMovimientosCuentaInversion = new bcs.newDataSource(bcsCuentaInversionGrid.dsDefMovimientosCuentaInversion);
	$scope.grid.confMovimientosCuentaInversion = {
		dataSource  : dsMovimientosCuentaInversion,
		columns 	: bcsCuentaInversionGrid.columnasMovimientosCuentaInversionEjecutivo(),
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 450,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		excel 				: { allPages: true },
		
		gridCreated : function(g) { 
			gridMovimientosCuentaInversion = g;
			g.hideColumn("Producto");         // Oculta columna ya que es para APV
		},

		
		rowColorBackground	: function(row) { 
								 if (row.IdEstado == 'ANU') //row.IdEstadoEvento == 'VIG' ||
								 	return "red";								 
								 if (row.IdEstadoEvento == 'AUT')
									 return "green";								 
								 if (row.IdEstado == 'PEN')
								 	return "yellow";
								 return "";
							  },
		notEditableColumns	: function(row) {
								if (row.IdEstadoEvento == 'VIG' || row.IdEstadoEvento == 'AUT' || row.IdEstado === 'ANU') {
									return ['GlosaAnulacion'];
								 };
	                          },
		aggregate			: [
								{ field: "Cargo", aggregate: "sum" },
								{ field: "Abono", aggregate: "sum" }
							  ]
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	//SETEO GRILLA TAB
	$scope.select = function(grid){
		$timeout(function() {			
			var t = $("."+grid).find(".k-grid");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
			}
		},1);
	};
	
	//DobleClick
	$scope.grid.cuentaInversionCliente = function(selectRow){
		//Abre Modal
		var objInfoSaldoCLiente = [];

		objInfoSaldoCLiente.InfoCliente = [{FechaEfectivaMovimiento 	: $scope.filtros.kngFechaMovimientoDesde,
								  			NombreCliente 				: selectRow[0].NombreCliente || '',
								  			DocumentoIdentidad 			: selectRow[0].DocumentoIdentidad || '',
								  			NombreEjecutivo				: selectRow[0].NombreEjecutivo || '',								  			
								  			NombreSucursal 				: selectRow[0].NombreSucursal || '',
								  			NombrePortafolio			: selectRow[0].NombrePortafolio || '',
								  			IdEjecutivo					: selectRow[0].IdEjecutivo || 0,
								  			IdCliente					: selectRow[0].IdCliente || 0,
								  			IdPortafolio				: selectRow[0].IdPortafolio || 0,
								  			IdCuentaInversion			: selectRow[0].IdCuentaInversion,
								 			}];
		objInfoSaldoCLiente.InfoSaldos  = selectRow;

		var ventanaFormaDePago = bcsComunes.ventanaAbrir("CuentaInversionClienteModal",objInfoSaldoCLiente,false,false,$scope.winID);		
        var promesa=bcsComunes.ventanaPromesa(ventanaFormaDePago);
	};
	
	$scope.grid.detalleComprobante = function(datos){
		if (datos[0].IdModulo == 'FAC')
			bcsComunes.ventanaAbrir("DetalleComprobanteDeFacturacion", {IdComprobante: datos[0].IdComprobante}, false, false, $scope.winID );
		if (datos[0].IdModulo == 'TES')
			bcsComunes.ventanaAbrir("DetalleComprobanteTesoreria", {IdComprobante: datos[0].IdComprobante}, false, false, $scope.winID );
		if (datos[0].IdModulo == 'FMM')
			bcsComunes.ventanaAbrir("ResumenCartaLiquidacion", {FolioCartaLiquidacion: datos[0].SecuenciaComprobanteOrigen}, false, false, $scope.winID );
	};

	$scope.procesarSaldos = function(data){
		var Saldos = [];
		var n = data.length;
		bcsComunes.ventanaOverlay($scope.winID, true);
		$timeout(function(){
			for(var i=0; i < n; i++){
				var saldo = data[i];
				Saldos.push(saldo);
				/*if ($scope.filtros.ngSoloClientesConSaldoDisponible && saldo.SaldoDisponible > 0){
					Saldos.push(saldo);
				}
				if (!$scope.filtros.ngSoloClientesConSaldoDisponible){
					Saldos.push(saldo);
				}*/
			}	
			dsSaldosCuentas.data(Saldos);
			bcsComunes.ventanaOverlay($scope.winID, false);			
		},0);
	};

	
	//************************************************************************************************
	// Solicita(Inscripción en JobManager) generación del archivo "Anexo 7 - Saldo de Clientes" (PERU)  
	//************************************************************************************************
	$scope.generarArchivoAnexo7SaldosDeClientes = function() {
		
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		//Parámetros de entrada al job
		var dataCols = [];

		//Parámetros fijos de entrada para el job
		dataCols.push({NombreParametro : 'IdEmpresa',    			Valor : $scope.SesionIdEmpresa});
		dataCols.push({NombreParametro : 'IdUsuario',    			Valor : $scope.SesionIdUsuario});
		dataCols.push({NombreParametro : 'CodigoFormatoArchivo',    Valor : 'ANEXO7_SALDOS_DE_CLIENTES'});
		
		//Parámetros Variables de entrada: los usa el SP que consulta data para el archivo (son los mismos que los filtros de búsqueda)
		if ($scope.filtros.oConfiguracionEjecutivo.idActual !== undefined && Number($scope.filtros.oConfiguracionEjecutivo.idActual) !==0)
		{
			dataCols.push({NombreParametro : 'IdEjecutivo',    			Valor : Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo)});
		}
		if ($scope.filtros.oConfiguracionSaldoCuentaInversion.idActual !== undefined && $scope.filtros.oConfiguracionSaldoCuentaInversion.idActual !== 0)
		{	
			dataCols.push({NombreParametro : 'IdMoneda',    			Valor : $scope.filtros.oConfiguracionSaldoCuentaInversion.seleccionado.IdMoneda});
		}
		if ($scope.filtros.oConfiguracionSaldoPortafolio.seleccionado.IdPortafolio !== undefined)
		{
			dataCols.push({NombreParametro : 'IdPortafolio',    		Valor : Number($scope.filtros.oConfiguracionSaldoPortafolio.seleccionado.IdPortafolio || 0)});
		}

		dataCols.push({NombreParametro : 'SoloSaldoNegativo',    	Valor : $scope.filtros.ngSoloClientesSobregirados ? 'S' : 'N'});
		dataCols.push({NombreParametro : 'SoloSaldoDisponible',    	Valor : $scope.filtros.ngSoloClientesConSaldoDisponible ? 'S' : 'N'});
		dataCols.push({NombreParametro : 'SoloSaldoDisponibleNegativo',    	Valor : $scope.filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados ? 'S' : 'N'});
		dataCols.push({NombreParametro : 'IncluyeOtrosSaldos',    	Valor : 'S'});
		dataCols.push({NombreParametro : 'FechaDesde',    			Valor : util.dateSPdate($scope.filtros.kngFechaEfectivaDesde) || '0001-01-01'});
		dataCols.push({NombreParametro : 'FechaHasta',    			Valor : util.dateSPdate($scope.filtros.kngFechaEfectivaHasta) || '0001-01-01'});
		
		
		var prom = bcsComunes.ventanaAbrir("ConsultaAnexo7", { data: dataCols } ,false,false,$scope.winID);
		bcsComunes.ventanaPromesa(prom).then(function(data){
			if (data) {
				
			}
		});
	};
	

	$scope.buscarSaldos = function()
	{		
		var filtersSaldos 		= {};
		var IdEjecutivo			= null;
		var IdMoneda			= null;
		var IdPortafolio		= null;
		var IdCliente			= null;
		var IncluyeProductoApv  = "N";
		var IncluyeOtrosSaldos  = "S";
		var Encabezados         = [];
	
		if($scope.MostrarProducto)
		{	
			gridSaldosCuentas.showColumn("Producto");         		//Activa visualización columna para APV
			gridMovimientosCuentaInversion.showColumn("Producto");  //Activa visualización columna para APV
			IncluyeProductoApv = 'S';
			
			filtersSaldos.p_IncluyeProductoApv	= IncluyeProductoApv;
			filtersSaldos.p_IdEjecutivo			= null;
			filtersSaldos.p_IdCliente			= null;
			filtersSaldos.p_IdPortafolio		= null;
			filtersSaldos.p_IdProductoAgrupador	= null;
			filtersSaldos.p_IdMoneda			= null;
			
			if($scope.oSocioNegocioSaldo.seleccionado.IdCliente !== undefined)
			{
				filtersSaldos.p_IdCliente = Number($scope.oSocioNegocioSaldo.seleccionado.IdCliente);
				IdCliente = Number($scope.oSocioNegocioSaldo.seleccionado.IdCliente)
			}
			
			if($scope.oProductoSaldo.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolioSaldo.seleccionado.IdPortafolio !== undefined)
			{
				filtersSaldos.p_IdPortafolio = Number($scope.oPortafolioSaldo.seleccionado.IdPortafolio);
				filtersSaldos.p_IdProductoAgrupador = Number($scope.oProductoSaldo.seleccionado.IdProductoAgrupador);
			}
			else if($scope.oProductoSaldo.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolioSaldo.seleccionado.IdPortafolio !== undefined)
			{
				filtersSaldos.p_IdPortafolio = Number($scope.oPortafolioSaldo.seleccionado.IdPortafolio);
			}
			else if($scope.oProductoSaldo.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolioSaldo.seleccionado.IdPortafolio === undefined)
			{
				if(Number($scope.oProductoSaldo.seleccionado.IdProductoAgrupador) !== 0)
				{
					if(Number($scope.oProductoSaldo.seleccionado.IdProductoAgrupador) === 9999)
					{
						filtersSaldos.p_IdProductoAgrupador = 0;
					}
					else
					{
						filtersSaldos.p_IdProductoAgrupador = Number($scope.oProductoSaldo.seleccionado.IdProductoAgrupador);
					}
				}
			}
			
			if($scope.filtros.oConfiguracionEjecutivo.idActual !== undefined && Number($scope.filtros.oConfiguracionEjecutivo.idActual) !==0)
			{
				filtersSaldos.p_IdEjecutivo = Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo);
				IdEjecutivo = Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo);
			}

			
			if($scope.filtros.oConfiguracionSaldoCuentaInversion.idActual !== undefined && $scope.filtros.oConfiguracionSaldoCuentaInversion.idActual !== 0)
			{
				filtersSaldos.p_IdMoneda = $scope.filtros.oConfiguracionSaldoCuentaInversion.seleccionado.IdMoneda;
				IdMoneda = $scope.filtros.oConfiguracionSaldoCuentaInversion.seleccionado.IdMoneda;
			}
						
			filtersSaldos.p_CuentaInversionAConsultar =
			[{
				IdEjecutivo			: filtersSaldos.p_IdEjecutivo,
				IdPortafolio  	  	: filtersSaldos.p_IdPortafolio,
				IdMoneda      	  	: filtersSaldos.p_IdMoneda
			}];
		}
		else
		{
			IncluyeProductoApv = 'N';
			if ($scope.filtros.oConfiguracionSaldoPortafolio.seleccionado.IdPortafolio !== undefined){
				IdPortafolio = Number($scope.filtros.oConfiguracionSaldoPortafolio.seleccionado.IdPortafolio || 0);
				IdCliente = Number($scope.filtros.oConfiguracionSaldoPortafolio.seleccionado.IdCliente || 0);
			}
					
			if ($scope.filtros.oConfiguracionEjecutivo.idActual !== undefined && Number($scope.filtros.oConfiguracionEjecutivo.idActual) !==0)
				IdEjecutivo = Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo);
			
			if ($scope.filtros.oConfiguracionSaldoCuentaInversion.idActual !== undefined && $scope.filtros.oConfiguracionSaldoCuentaInversion.idActual !== 0)
				IdMoneda = $scope.filtros.oConfiguracionSaldoCuentaInversion.seleccionado.IdMoneda;
			
			filtersSaldos.p_IdPortafolio = IdPortafolio;
			filtersSaldos.p_IdCliente =  IdCliente;
			
			filtersSaldos.p_CuentaInversionAConsultar =
			[{
				IdEjecutivo			: IdEjecutivo,
				IdPortafolio  	  	: IdPortafolio,
				IdMoneda      	  	: IdMoneda
			}];
		}

		if (IdCliente != null)
		{	//Si se especifica cliente, se despliegan datos por grilla de la UI
			
			filtersSaldos.p_IdEmpresa 					= $scope.SesionIdEmpresa;
			filtersSaldos.p_IdUsuario         			= $scope.SesionIdUsuario;
			filtersSaldos.p_IdModulo					= 'CTA';
			filtersSaldos.p_UsaReglasNegocio			= 'S';
			filtersSaldos.p_IncluyeOtrosSaldos			= IncluyeOtrosSaldos;
			filtersSaldos.p_IdPais						= $scope.SesionIdPaisSistema;		
			filtersSaldos.p_SoloSaldoNegativo			= $scope.filtros.ngSoloClientesSobregirados ? 'S' : 'N';
			filtersSaldos.p_SoloSaldoDisponible			= $scope.filtros.ngSoloClientesConSaldoDisponible ? 'S' : 'N';
			filtersSaldos.p_SoloSaldoDisponibleNegativo = $scope.filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados ? 'S' : 'N';
			filtersSaldos.p_FechaDesde					= util.dateSPdate($scope.filtros.kngFechaEfectivaDesde) || '0001-01-01';
			filtersSaldos.p_FechaHasta					= util.dateSPdate($scope.filtros.kngFechaEfectivaHasta) || '0001-01-01';
			
			bcsComunes.ventanaOverlay($scope.winID, true);
			
			bcsServiciosCuentaInversion.ConsultarSaldoCtaInv(filtersSaldos,
					function(respuesta){
						if (respuesta.P_OUT_ERRORESCONTADOR === 0){
							ResultadoSaldos = respuesta.P_OUT_SALDOPORFECHAEFECTIVA;
							$scope.procesarSaldos(ResultadoSaldos);
						}
						bcsComunes.ventanaOverlay($scope.winID, false);
					},
					function(respuesta){
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
							conflictos:util.parseServerResponse(respuesta)
						},$scope.winID);
						bcsComunes.ventanaOverlay($scope.winID, false);
					}
				);			
		}
		else
		{	//Si NO se especifica cliente,  se genera un proceso BACH que genera un csv con los datos de salida
			
			var entradaPrograma 		= {};					
			entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
			entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
			entradaPrograma.p_IdModulo    		= 'CTA';
			entradaPrograma.p_IdAgenda    		= 0;    
			entradaPrograma.p_IdTipoTarea   	= 'GENERAARCHIVOSALDOSCTA';
			entradaPrograma.p_Segundos      	= 0;			
			entradaPrograma.p_CreaParam     	= 'S';
		    
			//No se envia ni cliente ni portafolio ya que batch es masivo(considera todos los portafolios)

			var ListaSinFiltrar;
			var t = $("."+"TabSaldoCuentas").find(".k-grid");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) 
			       ListaSinFiltrar =$(t[i]).data("kendo-grid").columns
				}
			
			for (var j = 0; j < ListaSinFiltrar.length; j++) {
				if (ListaSinFiltrar[j].hidden===true){
					var elemento = ListaSinFiltrar[j].title;
					Encabezados.push(elemento);
				}
			}
			var jsonEncabezados = JSON.stringify(Encabezados);

			entradaPrograma.p_TablaParametros =
			[
			   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
			   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
			   {NombreParametro	: 'IdModulo', 	  		Valor: 'CTA'},			   
			   {NombreParametro	: 'IdEjecutivo', 	  	Valor: (IdEjecutivo != null ? IdEjecutivo : 0)},
			   {NombreParametro	: 'IdMoneda',           Valor: (IdMoneda != null ? IdMoneda : '')},			   
			   {NombreParametro	: 'FechaDesde', 	  	Valor: util.dateSPdate($scope.filtros.kngFechaEfectivaDesde) || '0001-01-01'},
			   {NombreParametro	: 'FechaHasta', 	  	Valor: util.dateSPdate($scope.filtros.kngFechaEfectivaHasta) || '0001-01-01'},			   
			   {NombreParametro	: 'SoloClientesSobregirados', 		Valor: $scope.filtros.ngSoloClientesSobregirados ? 'S' : 'N'},
			   {NombreParametro	: 'SoloClientesConSaldoDisponible', Valor: $scope.filtros.ngSoloClientesConSaldoDisponible ? 'S' : 'N'},
			   {NombreParametro : 'SoloClientesConSaldoDisponibleClientesSobregirados', Valor: $scope.filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados ? 'S' : 'N' },
			   {NombreParametro	: 'IncluyeProductoApv', 	  		Valor: IncluyeProductoApv},			   
			   {NombreParametro	: 'IncluyeOtrosSaldos', 	  		Valor: IncluyeOtrosSaldos},
			   {NombreParametro	: 'Encabezados', 	  	Valor: jsonEncabezados}    
			];

			
			console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
						
			bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
				function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
						Programa = respuesta.P_OUT_IDPROGRAMA;					
						if (Programa.length > 0)
						    idPrograma = Programa[0].IdPrograma;								
								
						bcsComunes.ventanaMsg({
							mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en Consultar Programas"							
						},$scope.winID);						
					}
					else
					{						
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
						},$scope.winID);
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					
				}
			);			
		}	
	};

	
	
	
	$scope.buscarMovimientos = function()
	{			
		var filterMovimientos	= [];		
		var idCliente   = 0;
		var idEjecutivo = 0;		
		var idMoneda = '0';
		var idTipoMovimiento = '0';
		var idConcepto = '0';
		var idEstado = '0';
		var fechaDesde = '';
		var fechaHasta = '';	
		var idPrograma = 0;
		
		if($scope.MostrarProducto)
		{
			if($scope.oSocioNegocioMovimiento.seleccionado.IdCliente !== undefined)
			{
				filterMovimientos.push({ field: "IdCliente", operator: "eq", value: Number($scope.oSocioNegocioMovimiento.seleccionado.IdCliente) });
				idCliente = Number($scope.oSocioNegocioMovimiento.seleccionado.IdCliente || 0)
			}
			
			if($scope.oProductoMovimiento.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolioMovimiento.seleccionado.IdPortafolio !== undefined)
			{
				filterMovimientos.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oPortafolioMovimiento.seleccionado.IdPortafolio) });
			}
			else if($scope.oProductoMovimiento.seleccionado.IdProductoAgrupador === undefined && $scope.oPortafolioMovimiento.seleccionado.IdPortafolio !== undefined)
			{
				filterMovimientos.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.oPortafolioMovimiento.seleccionado.IdPortafolio) });
			}
			else if($scope.oProductoMovimiento.seleccionado.IdProductoAgrupador !== undefined && $scope.oPortafolioMovimiento.seleccionado.IdPortafolio === undefined)
			{
				if(Number($scope.oProductoMovimiento.seleccionado.IdProductoAgrupador) !== 0)
				{
					if(Number($scope.oProductoMovimiento.seleccionado.IdProductoAgrupador) === 9999)
					{
						filterMovimientos.push({ field: "IdProductoAgrupador", operator: "eq", value: 0 });
					}
					else
					{
						filterMovimientos.push({ field: "IdProductoAgrupador", operator: "eq", value: Number($scope.oProductoMovimiento.seleccionado.IdProductoAgrupador) });
					}
				}
			}
		}
		else
		{
			if ($scope.filtros.oConfiguracionPortafolio.seleccionado.IdCliente  !== undefined)
			{			
				filterMovimientos.push({ field: "IdCliente", operator: "eq", value: Number($scope.filtros.oConfiguracionPortafolio.seleccionado.IdCliente || 0) });
				idCliente = Number($scope.filtros.oConfiguracionPortafolio.seleccionado.IdCliente || 0);
			}
			
			if ($scope.filtros.oConfiguracionPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				filterMovimientos.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.filtros.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0)});
			}
		}
		
		
		if($scope.filtros.oConfiguracionEjecutivo.idActual !== undefined)
		{
			if (Number($scope.filtros.oConfiguracionEjecutivo.idActual) !==0)
			filterMovimientos.push({ field: "IdEjecutivo", operator: "eq", value: Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo) });
			idEjecutivo = Number($scope.filtros.oConfiguracionEjecutivo.seleccionado.IdEmpleadoEjecutivo);
		}
		if ($scope.filtros.oConfiguracionCuentaInversionCliente2.seleccionado.Codigo !== undefined ) //Moneda
		{	
			if ($scope.filtros.oConfiguracionCuentaInversionCliente2.idActual !== 0 )
			filterMovimientos.push({ field: "IdMoneda", operator: "eq", value: $scope.filtros.oConfiguracionCuentaInversionCliente2.seleccionado.Codigo});
			idMoneda = $scope.filtros.oConfiguracionCuentaInversionCliente2.seleccionado.Codigo;
		}
		
		if ($scope.filtros.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== undefined && $scope.filtros.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== 'MOVTRN')
		{
			if ($scope.filtros.oConfiguracionTipoMovimiento.idActual !== 0 )
			filterMovimientos.push({ field: "IdTipoMovimiento", operator: "eq", value: $scope.filtros.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento});
			idTipoMovimiento = $scope.filtros.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento;
		}
		
		if ($scope.filtros.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== undefined && $scope.filtros.oConfiguracionTipoMovimiento.idActual !== 0 && $scope.filtros.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVTRN')
			filterMovimientos.push({ field: "EsCargoAbonoPorTraspaso", operator: "eq", value: 'S'});
		
		if ($scope.filtros.oConfiguracionConcepto.seleccionado.IdConcepto !== undefined)
		{
			if(Number($scope.filtros.oConfiguracionConcepto.idActual) !== 0)
			filterMovimientos.push({ field: "IdConcepto", operator: "eq", value: $scope.filtros.oConfiguracionConcepto.seleccionado.IdConcepto});
			idConcepto = $scope.filtros.oConfiguracionConcepto.seleccionado.IdConcepto;
		}
		
		if ($scope.filtros.ngSoloDAPVencidos)
		{
    		filterMovimientos.push({field: "IdClasificacionTipoInstrumento", operator: "eq", value: "IF"});
    		filterMovimientos.push({
						logic: "or",filters: [
											{field: "NemotecnicoOriginal", operator: "eq", value: "PAGARE R"},
            								{field: "NemotecnicoOriginal", operator: "eq", value: "PAGARE NR"}
        									]
    								});
		}		
		
		if ($scope.filtros.oConfiguracionEstado.seleccionado.IdEstado !== undefined)
		{
			if (Number($scope.filtros.oConfiguracionEstado.idActual) !== 0)
			filterMovimientos.push({ field: "IdEstado", operator: "eq", value: $scope.filtros.oConfiguracionEstado.seleccionado.IdEstado});
			idEstado = $scope.filtros.oConfiguracionEstado.seleccionado.IdEstado;
		}
		
		if ($scope.filtros.kngFechaMovimientoDesde !== undefined && $scope.filtros.kngFechaMovimientoHasta !== undefined)
		{
			filterMovimientos.push({ field: "FechaSistema", operator: "gte", value: util.dateOdata(util.dateInicioDia($scope.filtros.kngFechaMovimientoDesde)) });
			filterMovimientos.push({ field: "FechaSistema", operator: "lte", value: util.dateOdata(util.dateInicioDia($scope.filtros.kngFechaMovimientoHasta)) });
			fechaDesde  = util.dateSPdate($scope.filtros.kngFechaMovimientoDesde);
			fechaHasta  = util.dateSPdate($scope.filtros.kngFechaMovimientoHasta);		
		}
		
		if (idCliente != 0 || $scope.filtros.oConfiguracionEstado.seleccionado.IdEstado != 0)
		{	//Si se especifica cliente, se despliegan datos por grilla de la UI
			dsMovimientosCuentaInversion.filter(filterMovimientos);		
		}
		else
		{	//Si NO se especifica cliente,  se genera un proceso BACH que genera un csv con los datos de salida
			
			var entradaPrograma 		= {};					
			entradaPrograma.p_IdEmpresa			= $scope.SesionIdEmpresa;
			entradaPrograma.p_IdUsuario 		= $scope.SesionIdUsuario;
			entradaPrograma.p_IdModulo    		= 'CTA';
			entradaPrograma.p_IdAgenda    		= 0;		    
			entradaPrograma.p_IdTipoTarea   	= 'GENERAARCHIVOCARGOABONOCTA';
			entradaPrograma.p_Segundos      	= 0;			
			entradaPrograma.p_CreaParam     	= 'S';
		    
			//No se envia ni cliente ni portafolio ya que batch es masivo(considera todos los portafolios)
			entradaPrograma.p_TablaParametros =
			[
			   {NombreParametro	: 'IdEmpresa', 	  		Valor: $scope.SesionIdEmpresa},
			   {NombreParametro	: 'IdUsuario', 	  		Valor: $scope.SesionIdUsuario},
			   {NombreParametro	: 'IdModulo', 	  		Valor: 'CTA'},			   			   
			   {NombreParametro	: 'IdEjecutivo', 	  	Valor: idEjecutivo},
			   {NombreParametro	: 'IdMoneda',           Valor: idMoneda},			   
			   {NombreParametro	: 'IdTipoMovimiento', 	Valor: idTipoMovimiento},
			   {NombreParametro	: 'IdConcepto', 	  	Valor: idConcepto},
			   {NombreParametro	: 'IdEstado', 	  		Valor: idEstado},
			   {NombreParametro	: 'FechaDesde', 	  	Valor: fechaDesde},
			   {NombreParametro	: 'FechaHasta', 	  	Valor: fechaHasta}	   
			];
			
			console.log("ENTRADA BATCH> " + JSON.stringify(entradaPrograma));
						
			bcsServicioPlataforma.AgregarPrograma(entradaPrograma,
				function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){						
						Programa = respuesta.P_OUT_IDPROGRAMA;					
						if (Programa.length > 0)
						    idPrograma = Programa[0].IdPrograma;								
								
						bcsComunes.ventanaMsg({
							mensaje: "Programa solicitado correctamente (#" + idPrograma +"). Vea la salida en consultar programas"							
						},$scope.winID);						
					}
					else
					{						
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO")							
						},$scope.winID);
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("ERROR_EN_GENERACION_ARCHIVO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					
				}
			);
			
		}

	}; 


	
	//PROCESOS
	$scope.LlamadaCertificarMovimientos = function(){
		var dataCols = [];
		var clienteAnterior = 0;
		var countCliente = 0;

		$.each($scope.grid.seleccionMovimientosCuentaInversion, function(index, value){
			if (clienteAnterior === 0) 
				clienteAnterior = value.IdCliente;
			if (value.IdCliente !== clienteAnterior){
				countCliente = countCliente + 1;
				clienteAnterior = 0;
			}
			if (value.EsCargoAbonoPorTraspaso === 'N' || (value.EsCargoAbonoPorTraspaso === 'S' && value.IdTipoMovimiento === 'MOVCAR'))
				dataCols[index] = _.pick(value,'IdCargoAbonoMovimiento');			
		});		

		if (dataCols.length === 0){
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_MOVMIENTOS_DISTINTO_TRANSFERENCIA"));
			return;
		};

		if (countCliente !== 0){
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_CERTIFICACION_MASIVA_DE_DISTINTOS_CLIENTES') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					$scope.certificarMovimientos(dataCols);
				}
			});
    	}else{
    		$scope.certificarMovimientos(dataCols);
    	}
	}
	
	$scope.certificarMovimientos = function(dataCols){
		bcsComunes.ventanaOverlay($scope.winID, true);
		
        //VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
		datos.p_IdUsuario                       = $scope.SesionIdUsuario;
		datos.p_IdModulo                        = 'CTA';
		datos.p_UsaReglasNegocio                = 'S';

		datos.p_FechaMovimiento                 = util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		datos.p_IdPais							= $scope.SesionIdPaisSistema;
		datos.p_CargosAbonosACertificar			= dataCols;

       	bcsServiciosCuentaInversion.CertificarCargoAbono(
       		datos,
       		function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					
				} else {	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_HUBIERON_PROBLEMAS_ANULAR_MOVIMIENTO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID, false);
				$scope.buscarMovimientos();
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ANULAR_MOVIMIENTO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
       	);
	};

	$scope.LlamadaAnularMovimientos = function(){
		var dataCols = [];
		var clienteAnterior = 0;
		var countCliente = 0;

		$.each($scope.grid.seleccionMovimientosCuentaInversion, function(index, value){
			if (clienteAnterior === 0) 
				clienteAnterior = value.IdCliente;
			if (value.IdCliente !== clienteAnterior){
				countCliente = countCliente + 1;
				clienteAnterior = 0;
			}
			if (value.EsCargoAbonoPorTraspaso === 'N' || (value.EsCargoAbonoPorTraspaso === 'S' && value.IdTipoMovimiento === 'MOVCAR'))
				dataCols[index] = _.pick(value,'IdCargoAbonoMovimiento');			
		});		

		if (dataCols.length === 0){
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_MOVMIENTOS_DISTINTO_TRANSFERENCIA"));
			return;
		};

		if (countCliente !== 0){
			var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_ANULACION_MASIVA_DE_DISTINTOS_CLIENTES') );
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){
					$scope.anularMovimientos(dataCols);
				}
			});
    	}else{
    		$scope.anularMovimientos(dataCols);
    	}
	}
	
	$scope.anularMovimientos = function(dataCols){
		bcsComunes.ventanaOverlay($scope.winID, true);
		
		//VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= 'CTA';
		datos.p_simularOperacion	= 'N';
		datos.p_UsaReglasNegocio	= 'S';
		datos.p_FechaMovimiento		= util.dateSPdate(util.dateInicioDia($scope.SesionFechaSistema));
		datos.p_CargoAbonoAAnular	= dataCols
		datos.p_EsAnulacionManual	= 'S';

		bcsServiciosCuentaInversion.AnularCargoAbono(
			datos,
       		function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);					
				} else {	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_HUBIERON_PROBLEMAS_ANULAR_MOVIMIENTO"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID, false);
				$scope.buscarMovimientos();
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ANULAR_MOVIMIENTO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	};

	$scope.ImprimirComprobante = function(){
		var dataCols = [];
		
		//TEMPLATE COMPROBANTE
		$scope.configuracionComprobante = bcsFormatoDocumentosCuentaInversion.infoComprobante;		
		if (typeof $scope.configuracionComprobante === 'undefined'){
			bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_NO_EXISTE_CONFIGURACION_COMPROBANTE"));
			return;
		}
		
		$.each($scope.grid.seleccionMovimientosCuentaInversion, function(index, value){
			if (value.IdEstado === 'CRT') {
				dataCols[index] = _.pick(value,'IdCargoAbonoMovimiento');
			}			
		});	
		if (dataCols.length > 0){
			for (i=0;i<dataCols.length;i++){				
				bcsFormatoDocumentosCuentaInversion.CrearComprobanteCuentaInversion(
						$scope.configuracionComprobante,
						dataCols[i].IdCargoAbonoMovimiento
				);
			};

		} else {
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_MOVMIENTOS"));
			return;
		};
	};
	
	$scope.limpiarMovimientos = function()
	{	
		$scope.filtros.kngFechaMovimientoDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaMovimientoHasta = util.dateInicioDia($scope.SesionFechaSistema);
		
		if(!$scope.MostrarProducto)
			$scope.filtros.oConfiguracionPortafolio.idActual = false;
					
		$scope.filtros.oConfiguracionCuentaInversion.idActual = 0;
		$scope.filtros.oConfiguracionTipoMovimiento.idActual = 0;
		$scope.filtros.oConfiguracionConcepto.idActual = 0;
		$scope.filtros.oConfiguracionEstado.idActual = 0;
		
		$scope.oProductoMovimiento.idActual = 0;
		$scope.oSocioNegocioMovimiento.idActual = false;
		$scope.oPortafolioMovimiento.idActual = false;

		dsMovimientosCuentaInversion.removeAll();
	};
	
	$scope.limpiarSaldos = function()
	{	
		$scope.filtros.kngFechaEfectivaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaEfectivaHasta = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.oConfiguracionSaldoPortafolio.idActual = false;
		$scope.filtros.oConfiguracionSaldoCuentaInversion.idActual = 0;
		
		if(!$scope.MostrarProducto)
			$scope.filtros.oConfiguracionSaldoPortafolio.idActual = false;
		
		$scope.oProductoSaldo.idActual = 9999;
		$scope.oSocioNegocioSaldo.idActual = false;
		$scope.oPortafolioSaldo.idActual = false;
		$scope.filtros.ngSoloClientesSobregirados = false;
		$scope.filtros.ngSoloClientesConSaldoDisponible = false;
		$scope.filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados = false;
		
		dsSaldosCuentas.removeAll();
	};

	$scope.exportaExcelPdf = function(){
		$scope.grid.exportarExcel();
	    $scope.grid.exportarPDF();
	}

	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	/*
	$scope.$watchGroup(['filtros.ngSoloClientesSobregirados','filtros.ngSoloClientesConSaldoDisponible'],function(newval,oldval){
		if (ResultadoSaldos.length > 0 ){
			$scope.procesarSaldos(ResultadoSaldos);
		}
	},true);
	*/

	$scope.$watch('filtros.ngSoloClientesSobregirados',function(newval,oldval){
		switch (newval){
			case true:  
				//Desactiva check "Solo con saldo disponible"
				$scope.filtros.ngSoloClientesConSaldoDisponible = false;
				$scope.filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados = false; 
				break;
		}				
	});	
	
	$scope.$watch('filtros.ngSoloClientesConSaldoDisponible',function(newval,oldval){
		switch (newval){
			case true:
				//Desactiva check "Solo clientes sobregirados"
				$scope.filtros.ngSoloClientesSobregirados = false;
				$scope.filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados = false; 				
				break;
		}
	});
	
	$scope.$watch('filtros.ngSoloClientesConSaldoDisponibleClientesSobregirados',function(newval,oldval){
		switch (newval){
			case true:
				//Desactiva check "Solo clientes sobregirados"
				$scope.filtros.ngSoloClientesSobregirados = false;
				$scope.filtros.ngSoloClientesConSaldoDisponible = false; 				
				break;
		}
	});
	
	
	$scope.$watch('filtros.oConfiguracionSaldoPortafolio.idActual',function(newval,oldval)
	{
		if (newval)
		{
			//Copia el cliente selecionado en "Saldo Cuentas" a "Movimientos Clientes" 
			$scope.filtros.oConfiguracionPortafolio.idActual = $scope.filtros.oConfiguracionSaldoPortafolio.idActual;
		}
	});
	
	
	$scope.$watch('filtros.oConfiguracionPortafolio.idActual',function(newval,oldval)
	{
		if (newval)
		{	 
			//Copia el cliente selecionado en "Movimientos Clientes" a "Saldo Cuentas"
			$scope.filtros.oConfiguracionSaldoPortafolio.idActual = $scope.filtros.oConfiguracionPortafolio.idActual; 
		}
	});
	
	$scope.$watch('filtros.ngSoloDAPVencidos', function(newVal, oldVal) 
	{
    	if (newVal) 
    	{
        	$scope.filtros.oConfiguracionConcepto.idActual = 'VCDTIT';
        	$scope.filtros.oConfiguracionEstado.idActual = 'PEN';
    	}
    	else if (oldVal === true) 
    	{
			
        	 if ($scope.filtros.oConfiguracionConcepto.idActual === 'VCDTIT')
        	{
            $scope.filtros.oConfiguracionConcepto.idActual = 0;
        	}

        	if ($scope.filtros.oConfiguracionEstado.idActual === 'PEN')
        	{
            $scope.filtros.oConfiguracionEstado.idActual = 0;
        	}
    	}
	});
	
	$scope.$watch('filtros.oConfiguracionEstado.seleccionado.IdEstado', function(newVal, oldVal)
	{
    	if ($scope.filtros.ngSoloDAPVencidos)
    	{
        	if (newVal !== undefined && newVal !== 'PEN')
        	{
            $scope.filtros.ngSoloDAPVencidos = false;
        	}
    	}
	});
	
	$scope.$watch('filtros.oConfiguracionConcepto.seleccionado.IdConcepto', function(newVal, oldVal) 
	{
        if ($scope.filtros.ngSoloDAPVencidos) 
        {
            if (newVal !== undefined && newVal !== 'VCDTIT') 
            {
                $scope.filtros.ngSoloDAPVencidos = false;
            }
        }
    });
	
	
	$timeout(function(){
		//SETEO INICIAL (precarga el cliente fijado en la configuración)
		if(sesion.getPreferencias('recordarCliente'))
		{	
			IdPortafolioRecordado = sesion.getPreferencias("portafolioRecordado");
			if ( Number(IdPortafolioRecordado) > 0 ) {
				$scope.filtros.oConfiguracionSaldoPortafolio.idActual = Number(IdPortafolioRecordado);
			}
		}
	},1000);
	
	
	let firstHeight = $('#Win_CuentaInversionEjecutivo');
		firstHeight.css('height', '705px');
/******************************************************/
/*** FINAL											***/
/******************************************************/
});
