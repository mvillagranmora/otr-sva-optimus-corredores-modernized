angular.module('ModuloCuentaInversion').controller('CuentaInversionClienteCtrl',
function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope,bcsServiciosCuentaInversion,
			bcsCuentaInversionGrid,bcsServicioPlataforma,bcsFormatoDocumentosCuentaInversion)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	
	var filtersSaldos = {};
	var gridSaldosCuentas;
	var gridMovimientosCuentaInversion;
	var gridLibroInventario;
	
	$scope.data = {};
	$scope.grid = {};
	
	//SETEO INICIAL	
	$scope.data.kngFechaSistema 			= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaEfectiva 			= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaMovimientoDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaMovimientoHasta 	= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.kngFechaConsulta			= util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.SaldoPorFechaMovimiento 	= false;
	$scope.data.SaldoPorFechaEfectiva 		= true;
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
		
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.data.oConfiguracionPortafolio = {
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	
	$scope.oConfiguracionCuentaInversion = {
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
	
	$scope.data.oConfiguracionTipoMovimiento = {
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
	
	$scope.data.oConfiguracionConcepto = {
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
	
	$scope.data.oConfiguracionEstado = {
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
	
	$scope.data.oConfiguracionCuentaInversionLIS 	= _.clone($scope.oConfiguracionCuentaInversion);
	$scope.data.oConfiguracionCuentaInversionCRTANU = _.clone($scope.oConfiguracionCuentaInversion);
		
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}
	};
	
	$scope.oProducto = {
		filtros: {
			IdProductoAgrupador : {"field": "IdProductoAgrupador","operator": "neq", "value"   :  0 }
		}
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		}
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
	
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	var dsSaldosCuentas = new bcs.newDataSource(bcsCuentaInversionGrid.dsDefSaldosCuentas);
	$scope.grid.confBasicaSaldosCuentas = {
		dataSource  : dsSaldosCuentas,
		columns 	: bcsCuentaInversionGrid.columnasSaldosCuentasCliente(),
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		height 		: 450,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		gridCreated : function(g) { 
			gridSaldosCuentas = g;
			g.hideColumn("Producto");         // Oculta columna ya que es para APV
			g.hideColumn("NombrePortafolio"); // Oculta columna ya que es para APV
		},

	};
	
	var dsLibroInventarioSaldos = new bcs.newDataSource(bcsCuentaInversionGrid.dsDefLibroInventarioSaldos);
	$scope.grid.confBasicaLibroInventarioSaldos = {
		dataSource  : dsLibroInventarioSaldos,
		columns 	: bcsCuentaInversionGrid.columnasLibroInventarioSaldosCliente(),
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		height 		: 450,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		
		gridCreated : function(g) { 
			gridLibroInventario = g;
			g.hideColumn("Producto");         // Oculta columna ya que es para APV
			g.hideColumn("NombrePortafolio"); // Oculta columna ya que es para APV
		},
		
		
		excel 		: { allPages: true },		
		rowColorBackground	: function(row) {
								 if (row.IdLibroInventarioDeSaldos === 0)
								 	return "red";
								 return "";
							  }
	};
	
	var dsMovimientosCuentaInversion = new bcs.newDataSource(bcsCuentaInversionGrid.dsDefMovimientosCuentaInversion);
	$scope.grid.confMovimientosCuentaInversion = {
		dataSource  : dsMovimientosCuentaInversion,
		columns 	: bcsCuentaInversionGrid.columnasMovimientosCuentaInversionCliente(),
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 430,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		
		gridCreated : function(g) { 
			gridMovimientosCuentaInversion = g;
			g.hideColumn("Producto");         // Oculta columna ya que es para APV
			g.hideColumn("NombrePortafolio"); // Oculta columna ya que es para APV
		},
		
		rowColorBackground	: function(row) {
								 if (row.IdEstado == 'ANU') //row.IdEstadoEvento == 'VIG' || 
								 	return "red";
								 if (row.IdEstadoEvento == 'AUT')
								 	return "green";
								 return "";
							  },
		notEditableColumns	: function(row) {
								if (row.IdEstadoEvento == 'VIG' || row.IdEstadoEvento == 'AUT' || row.IdEstado === 'ANU') {
									return ['GlosaAnulacion'];
								 };
	                          }
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.select = function(grid){
		$timeout(function(){			
			var t = $("."+grid).find(".k-grid");
			for (var i = 0; i < t.length; i++ ) {
				if ($(t[i]).data("kendo-grid")) $(t[i]).data("kendo-grid").refresh()
			}
		},10);
	};
	
	//BUSCAR INFO DE GRILLAS.
	$scope.buscarMovimientos = function()
	{
		var filter 					= [];
		var filterSaldos			= [];
		var filterLibroInventario 	= [];
		var filterMovimientos 		= [];
		var filterCertificacion 	= [];
		var filterAnulacion 		= [];
		
		var IdCliente				= null;
		var IdPortafolio			= null;
		var IdProductoAgrupador		= null;
		
		$scope.grid.seleccionSaldosCuentas = undefined;
		$scope.grid.seleccionLibroInventarioSaldos = undefined;
		$scope.grid.seleccionMovimientosCuentaInversion = undefined;
		
		if($scope.MostrarProducto)
		{	
			gridSaldosCuentas.showColumn("Producto");         				//Activa visualización columna para APV
			gridSaldosCuentas.showColumn("NombrePortafolio"); 				
			gridMovimientosCuentaInversion.showColumn("Producto");         	//Activa visualización columna para APV
			gridMovimientosCuentaInversion.showColumn("NombrePortafolio");
			gridLibroInventario.showColumn("Producto");         			//Activa visualización columna para APV
			gridLibroInventario.showColumn("NombrePortafolio");
		
			if($scope.oProducto.seleccionado.IdProductoAgrupador !== undefined){
				if(Number($scope.oProducto.seleccionado.IdProductoAgrupador) === 9999)
				{
					IdProductoAgrupador = 0;
					filter.push({ field: "IdProductoAgrupador", operator: "eq", value: Number(IdProductoAgrupador) });
				}
				else
				  {
					 IdProductoAgrupador = Number($scope.oProducto.seleccionado.IdProductoAgrupador);
					 filter.push({ field: "IdProductoAgrupador", operator: "eq", value: Number(IdProductoAgrupador) });	
				  }
			}
			
			if($scope.oSocioNegocio.seleccionado.IdCliente !== undefined)
			{
				IdCliente = Number($scope.oSocioNegocio.seleccionado.IdCliente);
				filter.push({ field: "IdCliente", operator: "eq", value: Number(IdCliente) });				
			}
			
			if($scope.oPortafolio.seleccionado.IdPortafolio !== undefined)
			{
				IdPortafolio = Number($scope.oPortafolio.seleccionado.IdPortafolio);
				filter.push({ field: "IdPortafolio", operator: "eq", value: Number(IdPortafolio) });
				
			}			
		}
		else
		{
			filter.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdCliente || 0) });
			filter.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio || 0)});
		}
		
		//SALDOS
		if (filter.length > 0 && ($scope.oSocioNegocio.seleccionado || $scope.data.oConfiguracionPortafolio.seleccionado))
		{
			filtersSaldos.p_IdEmpresa 					= $scope.SesionIdEmpresa;
			filtersSaldos.p_IdUsuario         			= $scope.SesionIdUsuario;
			filtersSaldos.p_IdModulo					= 'CTA';
			filtersSaldos.p_UsaReglasNegocio			= 'S';
			filtersSaldos.p_IncluyeOtrosSaldos			= 'S';
			filtersSaldos.p_IdPais						= $scope.SesionIdPaisSistema;
			filtersSaldos.p_FechaDesde					= util.dateSPdate($scope.data.kngFechaConsulta) || '0001-01-01';
			filtersSaldos.p_FechaHasta					= util.dateSPdate($scope.data.kngFechaConsulta) || '0001-01-01';
			
			if($scope.MostrarProducto)
			{
				filtersSaldos.p_IncluyeProductoApv	= 'S';
				filtersSaldos.p_IdCliente			= IdCliente;
				filtersSaldos.p_IdPortafolio		= IdPortafolio;
				filtersSaldos.p_IdProductoAgrupador	= IdProductoAgrupador;
				
				filtersSaldos.p_CuentaInversionAConsultar =
				[{
					IdEjecutivo			: null,
					IdPortafolio  	  	: IdPortafolio,
					IdMoneda      	  	: null
				}];
			}
			else
			{
				filtersSaldos.p_IdPortafolio = Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio);
				filtersSaldos.p_IdCliente =  Number($scope.data.oConfiguracionPortafolio.seleccionado.IdCliente);
				
				filtersSaldos.p_CuentaInversionAConsultar =
				[{
					IdEjecutivo			: null,
					IdPortafolio  	  	: Number($scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio),
					IdMoneda      	  	: null
				}];
			}
			
			bcsComunes.ventanaOverlay($scope.winID, true);
			bcsServiciosCuentaInversion.ConsultarSaldoCtaInv(filtersSaldos,
				function(respuesta){
					if (respuesta.P_OUT_ERRORESCONTADOR === 0){
						var Saldos = [];
						
						if ($scope.data.SaldoPorFechaMovimiento){
							var n = respuesta.P_OUT_SALDOPORFECHAMOVIMIENTO.length;
							var RespuestaSaldo = respuesta.P_OUT_SALDOPORFECHAMOVIMIENTO;
						}
						if ($scope.data.SaldoPorFechaEfectiva){
							var n = respuesta.P_OUT_SALDOPORFECHAEFECTIVA.length;
							var RespuestaSaldo = respuesta.P_OUT_SALDOPORFECHAEFECTIVA;
						}
						
						for(var i=0; i<n ; i++){
							Saldos.push(RespuestaSaldo[i]);
						}
						
						dsSaldosCuentas.data(Saldos);
					}
					bcsComunes.ventanaOverlay($scope.winID, false);
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					bcsComunes.ventanaOverlay($scope.winID, true);
				}
			);
		}

		//LIBRO INVENTARIO SALDOS
		if (filter.length > 0){
			filterLibroInventario = _.clone(filter);		
			filterLibroInventario.push({ field: "IdEstado", operator: "neq", value: 'ANU' });
			filterLibroInventario.push({ field: "FechaEfectivaMovimiento", operator: "gte", value: util.dateOdata($scope.data.kngFechaEfectiva) });

			if ($scope.data.oConfiguracionCuentaInversionLIS.seleccionado.IdCuentaInversion)
				filterLibroInventario.push({ field: "IdCuentaInversion", operator: "eq", value: Number($scope.data.oConfiguracionCuentaInversionLIS.seleccionado.IdCuentaInversion)});			
			
			dsLibroInventarioSaldos.filter(filterLibroInventario);
		}

		//CERTIFICAR / ANULAR
		if (filter.length > 0){
			filterMovimientos = _.clone(filter);
			//filterMovimientos.push({ field: "IdEstado", operator: "neq", value: 'ANU'});
			
			if ($scope.data.oConfiguracionCuentaInversionCRTANU.seleccionado.IdCuentaInversion !== undefined && Number($scope.data.oConfiguracionCuentaInversionCRTANU.idActual) !== 0)
				filterMovimientos.push({ field: "IdCuentaInversion", operator: "eq", value: Number($scope.data.oConfiguracionCuentaInversionCRTANU.seleccionado.IdCuentaInversion)});
			if ($scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== undefined && $scope.data.oConfiguracionTipoMovimiento.idActual !== 0 && $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== 'MOVTRN')
				filterMovimientos.push({ field: "IdTipoMovimiento", operator: "eq", value: $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento});
			if ($scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento !== undefined && $scope.data.oConfiguracionTipoMovimiento.idActual !== 0 && $scope.data.oConfiguracionTipoMovimiento.seleccionado.IdTipoMovimiento === 'MOVTRN')
				filterMovimientos.push({ field: "EsCargoAbonoPorTraspaso", operator: "eq", value: 'S'});			
			if ($scope.data.oConfiguracionConcepto.seleccionado.IdConcepto !== undefined && $scope.data.oConfiguracionConcepto.idActual !== 0)
				filterMovimientos.push({ field: "IdConcepto", operator: "eq", value: $scope.data.oConfiguracionConcepto.seleccionado.IdConcepto});
			if ($scope.data.oConfiguracionEstado.seleccionado.IdEstado !== undefined && Number($scope.data.oConfiguracionEstado.idActual) !== 0)
				filterMovimientos.push({ field: "IdEstado", operator: "eq", value: $scope.data.oConfiguracionEstado.seleccionado.IdEstado});
			if ($scope.data.kngFechaMovimientoDesde !== undefined && $scope.data.kngFechaMovimientoHasta !== undefined){
				filterMovimientos.push({ field: "FechaSistema", operator: "gte", value: util.dateOdata(util.dateInicioDia($scope.data.kngFechaMovimientoDesde)) });
				filterMovimientos.push({ field: "FechaSistema", operator: "lte", value: util.dateOdata(util.dateInicioDia($scope.data.kngFechaMovimientoHasta)) });
			}

			dsMovimientosCuentaInversion.filter(filterMovimientos);
		}
		
	};
	
	$scope.InformacionSocioDeNegocio = function()
	{
		var info = {};
		
		if($scope.MostrarProducto)
		{
			info.IdSocioDeNegocio 		= $scope.oSocioNegocio.seleccionado.IdSocioDeNegocio;
			info.NombreCliente			= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].NombreCliente : '';
			info.DocumentoIdentidad		= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].DocumentoIdentidad : '';
			info.IdCliente 				= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].IdCliente : 0;
			info.IdPortafolio 			= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].IdPortafolio : 0;
			info.NombrePortafolio		= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].NombrePortafolio : '';
		}
		else
		{
			info.IdSocioDeNegocio 		= $scope.data.oConfiguracionPortafolio.seleccionado.IdSocioDeNegocio;
			info.NombreCliente			= $scope.data.oConfiguracionPortafolio.seleccionado.NombreCompleto;
			info.DocumentoIdentidad		= $scope.data.oConfiguracionPortafolio.seleccionado.DocumentoIdentidad;
			info.IdCliente 				= $scope.data.oConfiguracionPortafolio.seleccionado.IdCliente;
			info.IdPortafolio 			= $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;
			info.NombrePortafolio		= $scope.data.oConfiguracionPortafolio.seleccionado.CodigoPropositoPortafolio;
		}
		
		info.IdMoneda				= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].IdMoneda : '';
		info.IdCuentaInversion  	= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].IdCuentaInversion : 0;
		info.NombreMoneda			= $scope.grid.seleccionSaldosCuentas ? $scope.grid.seleccionSaldosCuentas[0].NombreMoneda : '';
		info.IdOrigenGarantia		= 'CTA';
		info.autoBind				= true;								// info.IdMoneda ? true : false;
		info.ActivarCombo			= info.IdMoneda ? false : true;		
		info.FechaConsulta 			= $scope.data.kngFechaConsulta;	// Fecha Consulta Saldo Cuenta inversión
		
		return info;
	};

	//PROCESOS
	$scope.cartolaDeMovimiento = function(){		
		var objInfoCLiente = $scope.InformacionSocioDeNegocio ();
		if (objInfoCLiente.IdMoneda !== '')
			bcsComunes.ventanaAbrir("CartolaDeMovimientos", objInfoCLiente, false, false, $scope.winID);
		else
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_CUENTA_INVERSION"));
		
	};
	
	$scope.flujoCaja = function(){
		var objInfoCLiente = $scope.InformacionSocioDeNegocio ();
		
		if (objInfoCLiente.IdMoneda !== '')
			bcsComunes.ventanaAbrir("FlujoCaja", objInfoCLiente,false,false,$scope.winID);
		else
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_CUENTA_INVERSION"));
	};
	
	
	/********
	$scope.canal = function(){		
		var objInfoCLiente = $scope.InformacionSocioDeNegocio ();
		bcsComunes.ventanaAbrir("ConsultarCustodiaPortafolio", objInfoCLiente,false,false,$scope.winID);
	};	
	
	$scope.garantia = function(){
		var objInfoCLiente = $scope.InformacionSocioDeNegocio ();
		bcsComunes.ventanaAbrir("BlotterGarantia", objInfoCLiente,false,false,$scope.winID);
	};
	*********/
	
	$scope.crearCargoAbono = function(){
		var objInfoCLiente = $scope.InformacionSocioDeNegocio();	
		bcsComunes.ventanaAbrir("CrearCargoAbono", objInfoCLiente,false,false,$scope.winID);
	};
	
	$scope.certificarMovimientos = function(){
	
		var dataCols = [];	
		var indice = 0;	

		$.each($scope.grid.seleccionMovimientosCuentaInversion, function(index, value){
			if (value.EsCargoAbonoPorTraspaso === 'N' || (value.EsCargoAbonoPorTraspaso === 'S' && value.IdTipoMovimiento === 'MOVCAR'))
			{
				dataCols[indice] = _.pick(value,'IdCargoAbonoMovimiento');
				indice++;
		    }
		});

		if (dataCols.length === 0){
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_MOVMIENTOS_DISTINTO_TRANSFERENCIA"));
			return;
		}

        //VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa                       = $scope.SesionIdEmpresa;
		datos.p_IdUsuario                       = $scope.SesionIdUsuario;
		datos.p_IdModulo                        = 'CTA';
		datos.p_UsaReglasNegocio                = 'S';

		datos.p_FechaMovimiento                 = util.dateSPdate($scope.data.kngFechaSistema);
		datos.p_IdPais							= $scope.SesionIdPaisSistema;
		datos.p_CargosAbonosACertificar			= dataCols;

		bcsComunes.ventanaOverlay($scope.winID, true);
       	bcsServiciosCuentaInversion.CertificarCargoAbono(
       		datos,
       		function(respuesta){
       			bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				$scope.buscarMovimientos();
				
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_CERTIFICAR_MOVIMIENTO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);				
			}
       	); 
	};
	
	$scope.anularMovimientos = function(estado)
	{
		var dataCols = [];

		$.each($scope.grid.seleccionMovimientosCuentaInversion, function(index, value){
			if (value.EsCargoAbonoPorTraspaso === 'N' || (value.EsCargoAbonoPorTraspaso === 'S' && value.IdTipoMovimiento === 'MOVCAR'))
				dataCols[index] = _.pick(value,'IdCargoAbonoMovimiento','GlosaAnulacion');
		});		
		if (dataCols.length === 0){
			bcsComunes.msg("warning",$translate.instant("UI_WARNING"),$translate.instant("UI_DEBE_SELECCIONAR_MOVMIENTOS_DISTINTO_TRANSFERENCIA"));
			return;
		}
		
		//VARIABLES DEL SERVICIO
		var datos = {};
		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= 'CTA';
		datos.p_simularOperacion	= 'N';
		datos.p_UsaReglasNegocio	= 'S';
		datos.p_FechaMovimiento		= util.dateSPdate($scope.data.kngFechaSistema);
		datos.p_CargoAbonoAAnular	= dataCols
		datos.p_EsAnulacionManual	= 'S';

		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosCuentaInversion.AnularCargoAbono(
			datos,
       		function(respuesta){
       			bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);							
				$scope.buscarMovimientos();
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID, false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ANULAR_MOVIMIENTO"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);				
			}
		);
	};
	
	$scope.ImprimirComprobante = function()
	{
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
	
	$scope.limpiarFiltros = function()
	{			
		$scope.data.kngFechaSistema 			= util.dateInicioDia($scope.SesionFechaSistema);		
		//$scope.data.kngFechaEfectiva 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaMovimientoDesde 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaMovimientoHasta 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaConsulta			= util.dateInicioDia($scope.SesionFechaSistema);

		$scope.grid.seleccionSaldosCuentas = undefined;
		$scope.grid.seleccionMovimientosCuentaInversion = undefined;
		$scope.data.oConfiguracionPortafolio.idActual		= false;
		$scope.data.oConfiguracionTipoMovimiento.idActual 	= 0;
		$scope.data.oConfiguracionConcepto.idActual 		= 0;		
		$scope.data.oConfiguracionEstado.idActual 			= 0;
		$scope.data.ngEjecutivo								= '';
		$scope.data.ngSucursal								= '';
		
		$scope.oSocioNegocio.idActual						= false;
		$scope.oProducto.idActual							= 9999;
		$scope.oPortafolio.idActual							= false;

		dsSaldosCuentas.removeAll();
		dsLibroInventarioSaldos.removeAll();
		dsMovimientosCuentaInversion.removeAll();
	};
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	//BUSCAR EJECUTIVO / SUCURSAL
	$scope.$watch('data.oConfiguracionPortafolio.idActual',function(newval,oldval)
	{
		if (typeof $scope.data.oConfiguracionPortafolio.idActual !== 'undefined' && $scope.data.oConfiguracionPortafolio.idActual !== false)
		{
			$scope.data.IdCliente 	 = $scope.data.oConfiguracionPortafolio.seleccionado.IdCliente;
			$scope.data.IdPortafolio = $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio;

			var ejecutivoDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {read: {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1699",dataType: "json"}},
					schema : {
						model : {
							fields : {								
								NombreCompletoEjecutivo	: { type: "string"},
								NombreSucursal			: { type: "string"},
								IdSucursal				: { type: "number"}
							}
						}
					},		
					filter: [{ "field"  : "IdCliente", 		"operator": "eq", "value"  : $scope.data.oConfiguracionPortafolio.seleccionado.IdCliente },
							 { "field"  : "IdPortafolio",  	"operator": "eq", "value"  : $scope.data.oConfiguracionPortafolio.seleccionado.IdPortafolio },							 
							]
				}
			});
			ejecutivoDatasource.query().then(function(){
				$rootScope.$apply(function() {
					if (typeof ejecutivoDatasource.data()[0] !== 'undefined'){
						$scope.data.ngEjecutivo	= ejecutivoDatasource.data()[0].NombreCompletoEjecutivo;
						$scope.data.ngSucursal	= ejecutivoDatasource.data()[0].NombreSucursal;
					}
				});
			});
		}			
	},true);
	
	$scope.$watch('data.SaldoPorFechaMovimiento',function(newval,oldval)
	{
		switch (newval)
		{
			case true:  $scope.data.SaldoPorFechaEfectiva = false; break;					
			case false: $scope.data.SaldoPorFechaEfectiva = true;  break; 
		}
		dsSaldosCuentas.removeAll();
	});
	
	$scope.$watch('data.SaldoPorFechaEfectiva',function(newval,oldval)
	{
		switch (newval)
		{
			case true:  $scope.data.SaldoPorFechaMovimiento = false; break;					
			case false: $scope.data.SaldoPorFechaMovimiento = true;  break;  
		}
		dsSaldosCuentas.removeAll();
	});
	
	var firstHeight = $('#Win_CuentaInversionCliente');
        firstHeight.css('height', '742px');
/******************************************************/
/*** FINAL											***/
/******************************************************/
});