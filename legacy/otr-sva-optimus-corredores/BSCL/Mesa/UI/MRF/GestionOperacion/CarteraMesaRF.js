angular.module('ModuloMesa').controller('ConsultaCarteraRFCtrl',
			function($scope,$translate,$timeout,$rootScope
				,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosMesa,
				bcsServiciosGrillasMesaRF,bcsServicioPlataforma)
{

	$scope.data = {};
	$scope.Cartera = {};
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var idVentana = bcsComunes.ventanaWinId($scope);
	var confGridCarteraMRF = bcsServiciosGrillasMesaRF.getConfiguracionIngresoOperacionMesaRF();
	var confGridCarteraMRFJava = bcsServiciosGrillasMesaRF.getConfiguracionIngresoOperacionMesaRFJava()
	var dataSourceCarteraOperacion = bcsServiciosGrillasMesaRF.getDataSourceCarteraOperacion();
	var dataSourceCarteraOperacionMesaRF = bcsServiciosGrillasMesaRF.getDataSourceCarteraOperacionJava();
	var carterasOperacion = [];
	var existePapel = false;
	var obtenerSort = function(){ // Ordenamiento para presentar cartera en ingreso de operacion RF (sobreescribe el de ModuloMesaRfConfGrids)
		var sort = [];
		if($scope.data.kngMontoOperacion == 0){
			//sort.push({ field: "IdTipoInstrumento", dir: "asc" });
			sort.push({ field: "INGMontoATasaCompraActual", dir: "desc" });
			if($scope.data.oConfiguracionTipoReajusteOperacion.IdActual != "CLP"){
				sort.push({ field: "IdMonedaInstrumento", dir: "desc" });
			}else{
				sort.push({ field: "IdMonedaInstrumento", dir: "asc" });
			}
			
			
		}else{
			sort.push({ field: "INGMontoATasaCompraActual", dir: "desc" });
			if($scope.data.oConfiguracionTipoReajusteOperacion.IdActual != "CLP"){
				sort.push({ field: "IdMonedaInstrumento", dir: "desc" });
			}else{
				sort.push({ field: "IdMonedaInstrumento", dir: "asc" });
			}
			
			
		}
		return sort;
	}

	// --------------------------------------------------
	// PARAMETROS PARA SABER QUE CARTERA CARGAR
	// --------------------------------------------------
	$scope.winID = bcsComunes.ventanaWinId($scope);
	$scope.data = bcsComunes.ventanaParams($scope.winID);
	$scope.data.seleccionCarteraPapeles = false;
	var esVenta = $scope.data.IdTipoOperacionCartera == "VENPAC" || $scope.data.IdTipoOperacionCartera == "VENTER";
	var nombreTituloWin = esVenta ? `${$scope.data.NombreCuentaPropia} (${$scope.data.portafolioCuentaPropiaCodigo})` : `${$scope.data.NombreCompleto} (${$scope.data.codigoPortafolioCliente})`;
	bcsComunes.ventanaTitulo(idVentana, `Cartera: ${nombreTituloWin}`)
	

	confGridCarteraMRFJava.options.gridCreated = function(grid){
		$scope.data.grid = grid;
		$scope.data.buscarCartera();
	}

	// --------------------------------------------------
	//CONFIGURACION COMBOBOX E INPUTS
	// --------------------------------------------------
	$scope.configuracionMercado = {
		filtros : {
			logic : "or",
				Intermediacion : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "IF"},
				RentaFija : {"field": "IdClasificacionTipoInstrumento","operator": "eq","value": "RF"},
		}
	};		

	$scope.oConfiguracionTipoReajusteOperacion = {
			tipo: "combobox-tipo-reajuste",
			configuracion: {
				template: '<span >#: IdTipoReajuste # - #: Nombre #</span>'
			},
			idActual: false,
			seleccionado: false,
			filtros: {
				IdEstado: { "field": "IdEstado", "operator": "eq", "value": "ACT" }
			},
			idDefecto: ''
		};
		
	$scope.data.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
			NoDerivados						: { "field" : "CodigoTipoInstrumento",          "operator": "neq", "value" : "DER" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
	
	$scope.data.oConfiguracionInstrumento = {
		buscar : "-1"
	};

	$scope.data.oConfiguracionEmisor={
		tipo : "combobox-emisor",
		configuracion : {},
		filtros : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
			IdEmiteIIF : { "field"   : "EmiteIIF","operator": "eq", "value"   : "S" },
			IdEmiteIRF : { "field"   : "EmiteIRF","operator": "eq", "value"   : "S" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
	};

	$scope.configuracionTipoCarteraMesa = {
		idActual: '',
		seleccionado: null
	};


	
	// --------------------------------------------------
	// CONFIGURACION DE GRILLAS
	// --------------------------------------------------

	var sort = obtenerSort();
	


	$scope.data.confBasicaCarteraPapeles = confGridCarteraMRFJava.options;
	$scope.data.confBasicaCarteraPapeles.dataSource = dataSourceCarteraOperacionMesaRF;
	
	$scope.data.confBasicaCarteraPapeles.rowColorBackground = function(row) {
		if (!row || !row.CarteraPacto) {
			return;
		}

		switch (String(row.CarteraPacto).trim().toUpperCase()) {
			case 'N':
				return 'black';

			case 'S':
				return 'blue';
		}
	};

	$scope.data.confBasicaCarteraPapeles.boldRow = function(row) {
    	return true;
	};

	var dataSourceDetalleOperacion = bcsServiciosGrillasMesaRF.getDataSourceDetalleOperacionMesaRF();	

	//se vuelve a buscar con el nuevo valor
	$scope.data.buscarCartera = function(){
	
		$scope.data.parametrosCarteraJava();
		
		$scope.data.buscarCarteraJava();
	
	};

	$scope.data.buscarCarteraJava = function() {
		dataSourceCarteraOperacionMesaRF.setCallParameters($scope.data.params).then(function (){
			$scope.datos =  dataSourceCarteraOperacionMesaRF.data(); 
		});
	} 
	
	$scope.data.parametrosCarteraJava = function(){
		var params = {};
		var	idClasificacionTipoInstrumento = $scope.configuracionMercado.seleccionado && $scope.configuracionMercado.seleccionado.IdClasificacionTipoInstrumento ?$scope.configuracionMercado.seleccionado.IdClasificacionTipoInstrumento :'';
		var EsCompra = $scope.data.IdTipoOperacionCartera == 'COMTER' || $scope.data.IdTipoOperacionCartera == "COMPAC"
		var EsPacto = $scope.data.IdTipoOperacionCartera == 'VENPAC' || $scope.data.IdTipoOperacionCartera == "COMPAC"
	
		// params.p_IdCliente = $scope.data.oConfiguracionTipoOperacion.seleccionado.IdTipoOperacion == 'COMPAC' ||'COMTER' ?$scope.data.IdClienteFiltro:$scope.data.oConfiguracionMesas.seleccionado.IdCliente;
			
		params.p_IdEmpresa	= $scope.data.SesionIdEmpresa; 
		params.p_FechaLiquidacionMayor	=  EsPacto ? $scope.data.fechaVencimientoOperacion.toISOString().split('T')[0] : '0001-01-01';
		params.p_FechaLiquidacionMenor	= $scope.data.fechaLiquidacionOperacion.toISOString().split('T')[0];
		params.p_FechaVencimiento =$scope.data.fechaVencimientoOperacion.toISOString().split('T')[0];
		// params.p_IdCliente = EsCompra ? $scope.data.IdClienteFiltro : $scope.data.oConfiguracionMesas.seleccionado.IdCliente; 
		params.p_IdCliente = $scope.data.IdClienteFiltro; 
		// params.p_IdMesa = EsCompra ? 0 : $scope.data.oConfiguracionMesas.seleccionado.IdMesa;
        params.p_IdMesa = EsCompra ? 0 : $scope.data.IdMesa;
		params.p_IdPortafolio = EsCompra ? $scope.data.IdPortafolioFiltro:$scope.data.IdPortafolioCorredora;
		params.p_IdEmisor =$scope.data.oConfiguracionEmisor.seleccionado == false? '':$scope.data.oConfiguracionEmisor.seleccionado.IdEmisor ;
		params.p_IdClasificacionTipoInstrumento=idClasificacionTipoInstrumento;
		params.p_Nemotecnico = !$scope.data.instrumentoDatos? '': $scope.data.instrumentoDatos.Nemotecnico;
		params.p_IdTipoReajuste =$scope.oConfiguracionTipoReajusteOperacion.seleccionado == false ? '':$scope.oConfiguracionTipoReajusteOperacion.seleccionado.IdTipoReajuste;
		$scope.data.params = params;
	}
	
	$scope.data.cargarNominal = function() {
		var id1=bcsComunes.ventanaAbrir(
				"IngresoNominalMesaRF",
				{"FechaLiquidacionOperacion":$scope.data.fechaLiquidacionOperacion},
				false,false,$scope.winID);
		var promesa=bcsComunes.ventanaPromesa(id1);					

		
   		$.when(promesa).then(function(data){
   			if (data.boton === 'ACEPTAR'){
   				data.boton = 'ACEPTAR_NUEVO';
   				bcsComunes.ventanaData($scope.winID, data);
				$scope.data.WinIngOperacion.data.PuedeGrabarFormasPago = true;
				$scope.data.WinIngOperacion.dataNew.agregarCartera('new', data);
				$timeout(function () {
					$scope.data.buscarCartera();
				}, 3000);
   			}
      	});
	}; 
	

	$scope.data.cargarPapel = function() {
		var Papel = $scope.data.seleccionCarteraPapeles;
		if($scope.data.gridApiCarteraInstrumento.dataSource.data().length > 0 ){
			carterasOperacion = $scope.data.gridApiCarteraInstrumento.dataSource.data();
			carterasOperacion.find( carOp => existePapel = carOp.FolioCartera === Papel.FolioCartera ? true : false);
		}else{
			existePapel = false;
		}
		
		if(existePapel){
			bcsComunes.msg("warning",$translate.instant('UI_ADVERTENCIA'),$translate.instant("UI_PAPEL_YA_INGRESADO_EN_OPERACION"));
			return;
		}
		if (!Papel) {
			bcsComunes.msg("warning",$translate.instant('UI_ADVERTENCIA'),$translate.instant("UI_DEBE_SELECCIONAR_UN_PAPEL"));
		}
		else {
			// Es necesario??
			$scope.Cartera.FolioCartera = Papel.FolioCartera;
			$scope.Cartera.IdCartera = Papel.IdCartera;
			$scope.Cartera.IdInstrumento = Papel.IdInstrumento;		
			$scope.Cartera.IdTipoInstrumento = Papel.IdTipoInstrumento;
			$scope.Cartera.TipoValorizacion = Papel.TipoValorizacion;
			$scope.Cartera.Nemotecnico = Papel.Nemotecnico;
			$scope.Cartera.IdClasificacionTipoInstrumento = Papel.IdClasificacionTipoInstrumento;
			$scope.Cartera.IdEmisor = Papel.IdEmisor;
			$scope.Cartera.IdBoveda = Papel.IdBoveda;
			$scope.Cartera.EsFungible = Papel.EsFungible;
			$scope.Cartera.IdTipoReajuste = Papel.IdTipoReajuste;
			$scope.Cartera.INGPrecioMercadoActual = Papel.INGPrecioMercadoActual;
			$scope.Cartera.INGTasaCompraActual = Papel.INGTasaCompraActual;
			$scope.Cartera.INGCantidad = Papel.INGCantidad;				
			$scope.Cartera.INGMontoATasaCompraActual = Papel.INGMontoATasaCompraActual;
			$scope.Cartera.INGMontoATasaMercadoProximo = Papel.INGMontoATasaMercadoProximo;
			$scope.Cartera.INGValorRescate = Papel.INGValorRescate;
			$scope.Cartera.FechaVencimientoInstrumento = Papel.FechaVencimientoInstrumento;
			$scope.Cartera.CorteMinimo = Papel.CorteMinimo;
			$scope.Cartera.CodigoIFRS = Papel.CodigoIFRS;
			$scope.Cartera.ValorClasificacionRiesgo = Papel.ValorClasificacionRiesgo;
			$scope.Cartera.INGTasaMercadoActual = Papel.INGTasaMercadoActual;
			$scope.Cartera.INGPrecioMercadoActual = Papel.INGPrecioMercadoActual;
			$scope.Cartera.INGMontoCompraActualMonedaInstrumento = Papel.INGMontoCompraActualMonedaInstrumento;

			$scope.Cartera.CarteraPacto = Papel.CarteraPacto;		
			$scope.Cartera.IdMonedaSistema = Papel.IdMonedaSistema;
			$scope.Cartera.IdMesa = Papel.IdMesa;
			$scope.Cartera.IdTipoProducto = Papel.IdTipoProducto;
			$scope.Cartera.IdTipoOperacion = Papel.IdTipoOperacion;
			$scope.Cartera.IdBolsa = Papel.IdBolsa;
			$scope.Cartera.Moneda = Papel.IdMonedaInstrumento;
			$scope.Cartera.TipoCarteraCorredor = Papel.TipoCarteraCorredor;

			if($scope.data.SesionIdUsuario === Papel.IdUsuarioReserva){
				Papel.INGCantidad = Papel.Cantidad;
			}
					
			var id1=bcsComunes.ventanaAbrir(
					"IngresoCarteraMesaRF",
					{	"FechaLiquidacionOperacion":$scope.data.fechaLiquidacionOperacion,
						"FolioCartera":Papel.FolioCartera,
						"IdCartera":Papel.IdCartera,
						"IdClasificacionTipoInstrumento":Papel.IdClasificacionTipoInstrumento,
						"ClasificacionTipoInstrumento":Papel.ClasificacionTipoInstrumento,
						"IdInstrumento":Papel.IdInstrumento,
						"IdTipoInstrumento":Papel.IdTipoInstrumento,
						"Nemotecnico":Papel.Nemotecnico,
						"IdMonedaInstrumento" : Papel.IdMonedaInstrumento,
						"IdEmisor":Papel.IdEmisor,
						"IdBoveda":Papel.IdBoveda,
						"NombreBoveda":Papel.NombreBoveda,
						"IdBolsa":Papel.IdBolsa,
						"CodigoIFRS":Papel.CodigoIFRS,
						"CodigoDescIFRS":Papel.CodigoDescIFRS,
						"IdTipoReajuste":Papel.IdTipoReajuste,
						"EsFungible":Papel.EsFungible,
						"CorteMinimo":Papel.CorteMinimo,
						"FechaEmisionInstrumento":Papel.FechaEmisionInstrumento,
						"FechaVencimientoInstrumento":Papel.FechaVencimientoInstrumento,
						"IdMonedaLiquidacion": $scope.data.oConfiguracionMonedaLiquidacionOperacion.seleccionado.IdMonedaLiquidacion,
						"Cantidad":Papel.INGCantidad,						
						"ValorRescate":Papel.INGValorRescate,
						"INGPrecioMercadoActual":Papel.INGPrecioMercadoActual,
						"INGTasaMercadoActual":Papel.INGTasaMercadoActual,
						"INGMontoATasaMercadoActual":Papel.INGMontoATasaMercadoActual,
						"INGPrecioCompra":Papel.INGPrecioCompraActual,
						"INGTasaCompra":Papel.INGTasaCompra,
						"INGMontoATasaCompraActual":Papel.INGMontoATasaCompraActual,
						"TipoValorizacion":Papel.TipoValorizacion,
						"MontoOperacion":$scope.data.WinIngOperacion.data.kngMontoOperacionMoneda,
						"MontoAsignado":$scope.data.WinIngOperacion.data.kngMontoAsignado,
						"MontoFinalCLP":$scope.data.MontoCLP_Org,
						"IdCondicionDeLiquidacion": $scope.data.CondicionDeLiquidacion ? $scope.data.CondicionDeLiquidacion.IdCondicionDeLiquidacion : $scope.data.IdCondicionDeLiquidacion,
						"MonedaEmisionInstrumento": Papel.Moneda,
						"MonedaLiquidacionInstrumento": Papel.IdMonedaInstrumento,
						"MonedaLiquidacionOperacion" : $scope.data.IdMonedaLiquidacionOperacionDetalle,
						"INGMontoCompraActualMonedaInstrumento" : Papel.INGMontoCompraActualMonedaInstrumento,
						"MesaCodigoIFRS" : $scope.data.oConfiguracionMesas.seleccionado.CodigoIFRS,
						"MontoOperacionLiquidacion": Papel.MontoLiquidacionOperacion,
						"ValorizacionManual":'N',
						"IdTipoOperacionCartera": $scope.data.IdTipoOperacionCartera,
						"TipoCarteraCorredor": Papel.TipoCarteraCorredor,
						"ValorClasificacionRiesgo": Papel.ValorClasificacionRiesgo
					}, 
					false,false,$scope.winID);

			var promesa=bcsComunes.ventanaPromesa(id1);					
			$.when(promesa).then(function(dataCartera){
				if (dataCartera.boton === 'ACEPTAR') {
					$scope.Cartera.boton = 'ACEPTAR';

					$scope.Cartera.kngCantidad = dataCartera.data.kngCantidad;
					$scope.Cartera.kngValorRescate = dataCartera.data.kngValorRescate;
					$scope.Cartera.kngPrecio = dataCartera.data.kngPrecio;
					$scope.Cartera.kngTasa = dataCartera.data.kngTasa;
					$scope.Cartera.kngMonto = dataCartera.data.kngMonto;
					$scope.Cartera.kngMontoLiquidacion = dataCartera.data.kngMontoLiquidacion;
					$scope.Cartera.kngMontoLiquidacionOperacion = dataCartera.data.kngMontoLiquidacionOperacion;
					$scope.Cartera.ClasificacionTipoInstrumento = dataCartera.data.GlsMercado;
					$scope.Cartera.CodigoDescIFRS = dataCartera.data.GlsIFRS;
					$scope.Cartera.CodigoIFRS  = dataCartera.data.oConfiguracionClasificacionIfrs.idActual;
					$scope.Cartera.NombreBoveda	=dataCartera.data.GlsBoveda;
					$scope.Cartera.MonedaLiquidacionOperacion = $scope.data.IdMonedaLiquidacionOperacionDetalle;
					$scope.Cartera.ValorizacionManual = dataCartera.data.valorizacionManual?'S':'N';
					bcsComunes.ventanaData($scope.winID, $scope.Cartera);
					$scope.data.WinIngOperacion.data.PuedeGrabarFormasPago = true;
					$scope.data.WinIngOperacion.dataNew.agregarCartera('update', $scope.Cartera);
					$timeout(function () {
						$scope.data.buscarCartera();
					}, 3000);
				}
			});
		};
	};
	
	$scope.data.WinIngOperacion.WinCartera = $scope;

	$scope.$watch('configuracionTipoCarteraMesa.idActual', function(newVal) {
		var dataSource = $scope.data.gridApiCarteraPapeles &&
						$scope.data.gridApiCarteraPapeles.dataSource;

		if (!dataSource) {
			return;
		}

		var filtrosActuales = dataSource.filter() || {
			logic: "and",
			filters: []
		};

		var filtros = filtrosActuales.filters.filter(function(f) {
			return f.field !== 'CarteraPacto';
		});

		if (newVal) {
			filtros.push({
				field: 'CarteraPacto',
				operator: 'eq',
				value: newVal
			});
		}

		dataSource.filter({
			logic: filtrosActuales.logic,
			filters: filtros
		});
	});

	$scope.$watch('configuracionMercado.seleccionado',function(newval,oldval){
		if (!$scope.configuracionMercado.seleccionado) return;
		
		//$scope.data.oConfiguracionEmisor.filtros
		if($scope.configuracionMercado.seleccionado.IdClasificacionTipoInstrumento == 'IF'){
			$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value ='S';
			$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value ='N';

		}else if($scope.configuracionMercado.seleccionado.IdClasificacionTipoInstrumento=='RF'){
			$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value ='N';
			$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value ='S';
		}else{
			$scope.data.oConfiguracionEmisor.filtros.IdEmiteIIF.value ='S';
			$scope.data.oConfiguracionEmisor.filtros.IdEmiteIRF.value ='S';
		}


		
		
	});
	
	var glosaColores = [
		{ Estado: 'Cartera Propia', Color: 'black' },
		{ Estado: 'Cartera Retro Compra', Color: 'blue' }
	];
	bcsComunes.tootipGlosa('toolConsultarCarteraMesa', glosaColores)
	
	//al cerrar ventana debe dejar en frente ventana de ingreso de operacion y hacer focus
	$("[ventanaid='" + idVentana + "']").data("kendoWindow").bind("close", 
		function w(e) {
			if ($scope.data.WinIngOperacion != null && $scope.data.WinIngOperacion != undefined) bcsComunes.ventanaFrente(bcsComunes.ventanaWinId($scope.data.WinIngOperacion));
			if ($scope.data.WinIngOperacion != null && $scope.data.WinIngOperacion != undefined) $("[ventanaid='" + bcsComunes.ventanaWinId($scope.data.WinIngOperacion) + "']").focus();
			
		}
	);

});
