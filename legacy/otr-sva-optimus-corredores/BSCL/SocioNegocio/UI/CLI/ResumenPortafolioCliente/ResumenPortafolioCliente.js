angular.module('ModuloSocioNegocio').controller('ResumenInversionClienteCtrl',
function($scope,bcsComunes,$translate,$resource, bcsServicioPlataforma)
{
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.data = {};	
	$scope.config = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoHeight(idVentana);
	$scope.config.params = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
	$scope.config.callCount = 0;
	$scope.data.IdMonedaSistema = $scope.SesionIdMonedaSistema;
	$scope.seleccionCustodia = null;
	$scope.data.dataCarteras = null;
	var usosUI = [];
	$scope.Ejecutivo = {
		nombre: "",
		telefono: "",
		email: "",
		fechaConsulta: ""
	}
	$scope.data.kngFechaFlujoCaja 	= util.dateInicioDia($scope.SesionFechaSistema);
 
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	/******************************************************/
	/*** FILTROS										***/
	/******************************************************/

	$scope.oClientePortafolio ={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
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


	/******************************************************/
	/*** FUNCIONES									***/
	/******************************************************/
	function mostrarChartComposicion(data) {
				
		if (data && data.length > 0) {
			var total = data.reduce(function(acc, item) {
				return acc + (item.value || 0);
			}, 0);
			mostrarDescripcionChart(1, null);	
			$scope.tieneDataChart1 = total > 0;
			$("#composicionActivosTitulo").text("Composición Activos")
	
		} else {
			$scope.tieneDataChart1 = true;
			$("#composicionActivosTitulo").text("Composición Activos sin datos")
		}
		$scope.$applyAsync();
	}
	function mostrarDescripcionChart(chartNum, e) {
		if(e){
			if(chartNum !== null && chartNum === 1 && e !== null){
				$("#desc-" + chartNum).text(e.category || e.dataItem.descripcion);
				$("#pct-" + chartNum).text(e.value.toFixed(2) + "%");
				$("#nemo").text("-");
				$("#monto").text("-");
				$("#cantidad").text("-");
				$("#precio").text("-");
				$("#moneda").text("-");
				$("#rentabilidad").text("-");
			}
			else if(chartNum !== null && chartNum === 2 && e !== null){
				$("#nemo").text(e.category);
				$("#monto").text(e.dataItem.montoFormateado);
				$("#cantidad").text(e.dataItem.cantidad);
				$("#precio").text("$"+e.dataItem.precio);
				$("#moneda").text(e.dataItem.moneda);
				$("#rentabilidad").text(e.dataItem.rentabilidad+"%");
			}
		}
		if(chartNum === 1 || chartNum === 2){
			$("#detalle-chart-" + chartNum).show();
		}
		
	}
	function abrirChartCustodia(dataItemChart1, custodiaPortafolio) {
		if (dataItemChart1){
			var clasificacionGrupo = dataItemChart1.clasificacionGrupo;
			var nombreGrupo = dataItemChart1.category;
		}

		var detalleFiltrado = custodiaPortafolio
			.filter(function(item) {
				return item.IdClasificacionTipoInstrumento === clasificacionGrupo;
			})
			.map(function(item) {
				var monto = parseFloat(item.Monto) || 0;
				var precio = parseFloat(item.Precio) || 0;
				var moneda = item.Moneda;
	
				var valorNumerico = (moneda === "CLP" || moneda === "UF" || item.IdClasificacionTipoInstrumento == 'DV')
					? Math.round(monto)
					: parseFloat(monto.toFixed(2));
	
				return {
					category: item.Nemotecnico,
					value: valorNumerico,
					montoFormateado: (moneda === "CLP" || moneda === "UF" || item.IdClasificacionTipoInstrumento == 'DV')
						? kendo.toString(valorNumerico, "n0")
						: kendo.toString(valorNumerico, "n2"),
					cantidad: (item.IdClasificacionTipoInstrumento === "RV")
						? kendo.toString(item.Cantidad, "n0")
						: kendo.toString(item.Cantidad, "n4"),
					precio: kendo.toString(precio, "n4"),
					moneda: moneda,
					rentabilidad: kendo.toString(item.Rentabilidad, "n2")
				};
			});
	
		//$("#detalle-chart-2").hide();
		cargarChartCustodia(nombreGrupo, detalleFiltrado);
	}
	function cargarChartCustodia(nombreGrupo, dataDetalle) {
		$("#chart2").kendoChart({
			chartArea: {
				background:"",
				margin: {
					top: 0,
					bottom: 0
				},
				padding: {
					top: 0,
					bottom: 0
				}
			},
			legend: {
				position: "right",
				offsetY: 10
			},
			seriesDefaults: {
				type: "donut",
				startAngle: 150,
				
			},
			series: [{
				name: nombreGrupo,
				data: dataDetalle,
				labels: {
					visible: false
				},
				size: 35,
				holeSize: 30
			}],
			tooltip: {
				visible: true,
				template: "#= category # : $#= value #"
			},
			seriesClick: function(e) {
				mostrarDescripcionChart(2, e);
			},
			legendItemClick: function(e) {
				e.preventDefault();
		
				var textoLeyenda = e.text;

				var dataItem = dataDetalle.find(function(item) {
					return item.category === textoLeyenda;
				});
		
				if (!dataItem) {
					return;
				}
		
				var total = dataDetalle.reduce(function(acum, item) {
					return acum + (parseFloat(item.value) || 0);
				}, 0);
		
				var fakeEvent = {
					category: dataItem.category,
					dataItem: dataItem,
					value: parseFloat(dataItem.value) || 0,
					percentage: total > 0 ? (parseFloat(dataItem.value) || 0) / total : 0,
					series: e.series,
					sender: e.sender
				};
		
				mostrarDescripcionChart(2, fakeEvent);
			}
			
		});
		if (dataDetalle && dataDetalle.length > 0) {
			var total = dataDetalle.reduce(function(acc, item) {
				return acc + (item.value || 0);
			}, 0);

			mostrarDescripcionChart(2, null);
			$scope.tieneDataChart2 = true;
			$("#custodiaPortafolioTitulo").text(nombreGrupo)
			
	
		} else {
			mostrarDescripcionChart(2, null);
			$scope.tieneDataChart2 = true;
			$("#custodiaPortafolioTitulo").text("Custodia sin datos")

		}
		$scope.$applyAsync();

	}
	function getColor(nombreGrupo) {
		var colores = {
			"CAJA": "#0b6e8a",
			"DERIVADOS": "#18c21f",
			"FONDOS MUTUOS": "#22cfd0",
			"INTERMEDIACION FINANCIERA": "#c9cf2f",
			"OPERACIONES POR LIQUIDAR": "#c95a57",
			"RENTA FIJA": "#0a7f63",
			"RENTA VARIABLE": "#d89a2b"
		};
	
		return colores[(nombreGrupo || "").toUpperCase().trim()] || "#999999";
	}
	function formatearMonto(monto, moneda) {
		monto = Number(monto) || 0;
	
		if (moneda === "CLP") {
			return Math.round(monto);
		} else {
			return Number(monto.toFixed(2));
		}
	}
	var FlujoCaja = function(filters){
		$scope.config.callCount =0;
		bcsComunes.ventanaOverlay(idVentana,true);
		bcsServicioPlataforma.Servicios().ConsultaFlujoCaja(filters).$promise.then(
			function(respuesta){			
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					if (respuesta.P_OUT_FLUJOCAJA && respuesta.P_OUT_FLUJOCAJA.length > 0) {
						var flujoCajaFiltrado = respuesta.P_OUT_FLUJOCAJA.filter(function(f) {
							return usosUI.some(function(u) {
								return u.Nombre === f.NombreUso;
							});
						});
						actualizarTablaFlujoCaja(flujoCajaFiltrado);
					}
				} else {				
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_CONSULTAR_FLUJO_CAJA"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_CONSULTAR_FLUJO_CAJA"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);	
			}	
		).finally(function() {
			$scope.config.callCount++;
		});
	};	
	function construirDataFlujoCaja(lista) {
		return (lista || []).map(function(item, index) {	
			return {
				id: index + 1,
				parentId: null,
				descripcion: $translate.instant(item.NombreUso),
				monto: formatearMonto(item.Saldo, item.IdMoneda),
				moneda: item.IdMoneda
			};
		});
	}
	function actualizarTablaFlujoCaja(result) {
		var dataFlujoCaja = construirDataFlujoCaja(result);
		var treeList = $("#treeMovimientos").data("kendoTreeList");
	
		if (treeList) {
			treeList.dataSource.data(dataFlujoCaja);
		}
	}
	function parseFechaUtcALocal(fechaStr) {
		if (!fechaStr) return null;
		return new Date(fechaStr.replace(" ", "T") + "Z");
	}
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
		/******************************************************/
		/*** GRID-ORDENES									***/
		/******************************************************/
		var colFecha = {
			field: "FechaOperacionOrden",
			title: 'UI_FECHA_ORDEN',
			width: "93px",
			align: "left",
			template: "#= FechaOperacionOrden ? kendo.toString(FechaOperacionOrden, 'dd-MM-yyyy HH:mm') : '' #"
		};
		
		var columnasOrdenes = [
			{ field: "IdOrden",                          title: 'UI_FOLIO',        width: "49px", align: "left" },
			colFecha,
			{ field: "IdClasificacionTipoInstrumento",   title: 'UI_CLASIFICACION',          width: "57px", align: "center" },
			{ field: "AccionTipoOperacion",              title: 'UI_OPERACION',              width: "64px", template: "<span translate>#: AccionTipoOperacion #</span>", align: "center" },
			{ field: "InstrumentoUI",                    title: 'UI_NEMOTECNICO',            width: "124px" },
			{ field: "IdMonedaOrden",                    title: 'UI_MONEDA',           width: "54px", align: "center" },
			{ field: "EstadoOrden",                      title: 'UI_ESTADO',                 width: "78px", template: "#: (EstadoOrden || '').toUpperCase() #", align: "center" }
		];
		
		var schemaOrden = {
			model: {
				fields: {
					IdOrden:                          { type: "number" },
					FechaOperacionOrden:              { type: "datetime" },
					IdClasificacionTipoInstrumento:   { type: "string" },
					AccionTipoOperacion:              { type: "string" },
					InstrumentoUI:                    { type: "string" },
					IdMonedaOrden:                    { type: "string" },
					EstadoOrden:                      { type: "string" }
				}
			}
		};
		var dsOrdenes = new bcs.DataSource({
			definition: {
				data: [
				],
				pageSize: 10,
				sort: {
					field: "FechaOperacionOrden",
					dir: "desc"
				},
				schema: schemaOrden,
			}
		});
		var conf = {
			userSettings:false,
			sortable: false,
			reorderable: false,
			filterable: false,
			groupable: false,
			pageable: true,
			selectable: false,
			allowCopy: false,
			resizable: false,
			autoBind: true,
			scrollable: true,
			navigatable: true,
			height:320,
			dataSource: dsOrdenes,
			columns: columnasOrdenes,
			excel : {
				allPages: true,
			},
			boldRow: function() {
				return "bold";
			}
		};
		$scope.defaultOptionsNew = conf;
		/******************************************************/
		/*** GRID-ORDENES									***/
		/******************************************************/

		/******************************************************/
		/*** GRID-CUSTODIA  								***/
		/******************************************************/

		$scope.columnasCustPortafolio = [
				 {field: "DocumentoIdentidad",				title: 'UI_CLIENTE',								width: "60px",	hidden: true	}
				,{field: "Producto",						title: 'UI_PRODUCTO',			                    width: "100px",	hidden: true}
				,{field: "CodigoPortafolio",				title: 'UI_PORTAFOLIO',								width: "90px",	hidden: true }
				,{field: "DocumentoIdentidadRutCavali",		title: 'UI_RUT_CAVALI', 							width: "100px", hidden: true }
				,{field: "NombreCliente",					title: 'UI_NOMBRE_CLIENTE',							width: "200px",	hidden: true}
				,{field: "NombreEjecutivo",					title: 'UI_NOMBRE_EJECUTIVO',						width: "200px",	hidden: true}
				,{field: "Relacion",						title: 'UI_RELACION',								width: "50px",	hidden: true}
				,{field: "IdClasificacionTipoInstrumento",	title: 'UI_MERCADO',								width: "60px"}
				,{field: "IdTipoInstrumento",				title: 'UI_TIPO_INSTRUMENTO_ABR',					width: "60px"}
				,{field: "Nemotecnico",						title: 'UI_INSTRUMENTO',							width: "100px"}
				,{field: "ISIN", 							title: 'UI_ISIN', 									width: "100px",	hidden: true }
				,{field: "IdEmisor",						title: 'UI_EMISOR',									width: "80px",	hidden: true}
				,{field: "ClasificacionRiesgoInstrumento",	title: 'UI_LISTA',									width: "50px"}
				,{field: "EsFungible",						title: 'UI_FUNGIBLE',								width: "50px"}
				,{field: "IdMonedaInstrReajuste",			title: 'UI_TIPO_REAJUSTE',							width: "60px"}
				,{field: "EsPagaderoEn",					title: 'UI_MONEDA_PAGO',							width: "60px",	hidden: true}
				,{field: "IdBoveda",						title: 'UI_BOVEDA',									width: "60px"}
				,{field: "NumeroCuentaBoveda", 				title: 'UI_NUMERO_CUENTA', 							width: "55px",	hidden: true}
				,{field: "TotalPosicion",					title: 'UI_TOTAL_CUSTODIA',							width: "100px",		template : " <label> #: util.bcs_formato(TotalPosicion,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "TotalPosicionPresunta",			title: 'UI_TOTAL_PRESUNTO',							width: "100px",	hidden: true, 	template : " <label> #: util.bcs_formato(TotalPosicionPresunta,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PPPCustodia",						title: 'UI_PPP_CUSTODIA',							width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PPPCustodia,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}			
				,{field: "ValorPPPCustodia",				title: 'UI_VALOR_PPP_CUSTODIA',						width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(ValorPPPCustodia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "MontoPPPCustodia",				title: 'UI_MONTO_PPP_CUSTODIA',						width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(MontoPPPCustodia,'GEN','Monto','"+$scope.data.IdMonedaSistema+"','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}	    		
				,{field: "Rentabilidad",					title: 'UI_RENTABILIDAD',							width: "100px",   	template : " <label> #: util.formateaNumero(Rentabilidad,2) #</label>"}
				
				,{field: "CantidadCustodia",				title: 'UI_CUSTODIA_DISPONIBLE',					width: "100px", 	template : " <label> #: util.bcs_formato(CantidadCustodia,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoCustodia",			title: 'UI_PRECIO_MERCADO_CUSTODIA',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoCustodia,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "TasaMercadoCustodia",				title: 'UI_TASA_MERCADO',							width: "70px",  hidden: true,	template : " <label> #: util.bcs_formato(TasaMercadoCustodia,'GEN','Tasa',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>"}
				,{field: "ValorMercadoCustodia",			title: 'UI_VALOR_MERCADO_CUSTODIA',					width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoCustodia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "MontoMercadoCustodia",			title: 'UI_MONTO_MERCADO_CUSTODIA',					width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(MontoMercadoCustodia,'GEN','Monto','"+$scope.data.IdMonedaSistema+"','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoCustodia",		title: 'UI_DIFERENCIA_MERCADO_CUSTODIA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoCustodia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				
				,{field: "DividendoEnDinero",				title: 'UI_DIVIDENDO_EN_DINERO',					width: "100px",	hidden: true,	template : " <label> #: util.bcs_formato(DividendoEnDinero,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				
				,{field: "CantidadGarantia",				title: 'UI_GARANTIAS',								width: "100px", 	template : " <label> #: util.bcs_formato(CantidadGarantia,'GEN','Cantidad',IdMonedaInstrReajuste,IdTipoInstrumento,'').valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoGarantia",			title: 'UI_PRECIO_MERCADO_GARANTIAS',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoGarantia,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoGarantia",			title: 'UI_MONTO_MERCADO_GARANTIAS',				width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(ValorMercadoGarantia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoGarantia",		title: 'UI_DIFERENCIA_MERCADO_GARANTIAS',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoGarantia,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				
				,{field: "CantidadSimulCom",				title: 'UI_COMPRA_A_PLAZO',						width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(CantidadSimulCom,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoSimulCom",			title: 'UI_PRECIO_MERCADO_COMPRA_A_PLAZO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoSimulCom,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"],groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoSimulCom",			title: 'UI_MONTO_MERCADO_COMPRA_A_PLAZO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoSimulCom,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoSimulCom",		title: 'UI_DIFERENCIA_MERCADO_COMPRA_A_PLAZO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoSimulCom,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
		
				,{field: "CantidadSimulVen",				title: 'UI_VENTA_A_PLAZO',						width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(CantidadSimulVen,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoSimulVen",			title: 'UI_PRECIO_MERCADO_VENTA_A_PLAZO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoSimulVen,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoSimulVen",			title: 'UI_MONTO_MERCADO_VENTA_A_PLAZO',		width: "100px", hidden: true,  	template : " <label> #: util.bcs_formato(ValorMercadoSimulVen,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoSimulVen",		title: 'UI_DIFERENCIA_MERCADO_VENTA_A_PLAZO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoSimulVen,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
		
				,{field: "CantidadPrestamo",				title: 'UI_PRESTATARIO',							width: "100px", hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPrestamo,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoPrestamo",			title: 'UI_PRECIO_MERCADO_PRESTATARIO',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPrestamo,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoPrestamo",			title: 'UI_MONTO_MERCADO_PRESTATARIO',				width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(ValorMercadoPrestamo,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoPrestamo",		title: 'UI_DIFERENCIA_MERCADO_PRESTATARIO',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPrestamo,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
		
				,{field: "CantidadEmpresti",				title: 'UI_EMPRESTITO',								width: "100px", hidden: true,  	template : " <label> #:  util.bcs_formato(CantidadEmpresti,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoEmpresti",			title: 'UI_PRECIO_MERCADO_EMPRESTITO',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoEmpresti,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoEmpresti",			title: 'UI_MONTO_MERCADO_EMPRESTITO',				width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoEmpresti,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoEmpresti",		title: 'UI_DIFERENCIA_MERCADO_EMPRESTITO',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoEmpresti,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				
				,{field: "CantidadPactosV",					title: 'UI_VENTAS_CON_PACTO',						width: "100px", 	template : " <label> #:  util.bcs_formato(CantidadPactosV,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoPactosV",			title: 'UI_PRECIO_MERCADO_VENTAS_CON_PACTO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPactosV,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoPactosV",				title: 'UI_MONTO_MERCADO_VENTAS_CON_PACTO',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoPactosV,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoPactosV",		title: 'UI_DIFERENCIA_MERCADO_VENTAS_CON_PACTO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPactosV,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				
				,{field: "CantidadPactosC",					title: 'UI_COMPRAS_CON_PACTO',						width: "100px", 	template : " <label> #:  util.bcs_formato(CantidadPactosC,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoPactosC",			title: 'UI_PRECIO_MERCADO_COMPRAS_CON_PACTO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPactosC,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoPactosC",				title: 'UI_MONTO_MERCADO_COMPRAS_CON_PACTO',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoPactosC,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoPactosC",		title: 'UI_DIFERENCIA_MERCADO_COMPRAS_CON_PACTO',	width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPactosC,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
		
				,{field: "CantidadReporteCompra",			title: 'UI_REPORTE_COMPRA',							width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(CantidadReporteCompra,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoReporteCompra",		title: 'UI_PRECIO_MERCADO_REPORTE_COMPRA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoReporteCompra,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoReporteCompra",		title: 'UI_MONTO_MERCADO_REPORTE_COMPRA',			width: "100px", hidden: true,  	template : " <label> #: util.bcs_formato(ValorMercadoReporteCompra,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoReporteCompra",	title: 'UI_DIFERENCIA_MERCADO_REPORTE_COMPRA',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoReporteCompra,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}	    		
				
				,{field: "CantidadReporteVenta",			title: 'UI_REPORTE_VENTA',							width: "100px", hidden: true,	template : " <label> #: util.bcs_formato(CantidadReporteVenta,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "PrecioMercadoReporteVenta",		title: 'UI_PRECIO_MERCADO_REPORTE_VENTA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoReporteVenta,'GEN','Precio',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP',IdTipoInstrumento,'').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "ValorMercadoReporteVenta",		title: 'UI_MONTO_MERCADO_REPORTE_VENTA',			width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoReporteVenta,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "DiferenciaMercadoReporteVenta",	title: 'UI_DIFERENCIA_MERCADO_REPORTE_VENTA',		width: "100px", hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoReporteVenta,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
	
				,{field: "CantidadTranomEgr",				title: 'UI_COMPRA_NOMINAL',							width: "100px", hidden: true, template : " <label> #: util.bcs_formato(CantidadTranomEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "CantidadTranomIng",				title: 'UI_VENTA_NOMINAL',							width: "100px", hidden: true, template : " <label> #: util.bcs_formato(CantidadTranomIng,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
		
				,{field: "CantidadTransIng",				title: 'UI_INGRESO_TRANSITO',						width: "100px", template : " <label> #: util.bcs_formato(CantidadTransIng,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "CantidadTransEgr",				title: 'UI_EGRESOS_TRANSITO',						width: "100px",template : " <label> #: util.bcs_formato(CantidadTransEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "CantidadTransVenDesEgr",			title: 'UI_VENTA_DESCUBIERTA',						width: "100px", hidden: true, template : " <label> #: util.bcs_formato(CantidadTransVenDesEgr,'GEN','Cantidad',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
				,{field: "RegimenTributario",				title: 'UI_REGIMEN_TRIBUTARIO',						width: "100px", hidden: true}
				,{field: "CodigoPortafolioWS",				title: 'UI_PORTAFOLIOWS',							width: "60px", hidden: true }
				,{field: "ValorMercadoCustodiaReajuste",	title: 'UI_VALOR_MERCADO_CUSTODIA_REAJUSTE',		width: "60px", hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoCustodiaReajuste,'GEN','Monto',IdMonedaInstrReajuste!=''?IdMonedaInstrReajuste:'CLP','','').valor #</label>",aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
			];

		var schemaCustPortafolio = {
				model : {
					fields : {
	        			IdClasificacionTipoInstrumento 		: { type: "string"},
	        			NombreClasificacionTipoInstrumento 	: { type: "string"},
	        			IdTipoInstrumento					: { type: "string"},
	        			NombreTipoInstrumento				: { type: "string"},
	        			ISIN								: { type: "string"},
	        			IdInstrumento						: { type: "number"},
	        			ClasificacionRiesgoInstrumento		: { type: "string"},
	        			Nemotecnico							: { type: "string"},
	        			IdEmisor							: { type: "string"},
	        			IdCliente							: { type: "number"},
	        			DocumentoIdentidad					: { type: "string"},
	        			DocumentoIdentidadRutCavali         : { type: "string"},
	        			NombreCliente						: { type: "string"},
						NombreEjecutivo						: { type: "string"},
	        			CodigoPortafolio					: { type: "string"},
						CodigoPortafolioWS					: { type:"string"},
	        			IdPortafolio						: { type: "number"},
	        			Relacion							: { type: "string"},
	        			IdBoveda							: { type: "string"},
	        			NumeroCuentaBoveda					: { type: "string"},
	        			EsFungible							: { type: "string"},
	        			CantidadCustodia					: { type: "number"},
	        			PPPCustodia							: { type: "number"},
	        			MontoPPPCustodia					: { type: "number"},
	        			ValorPPPCustodia					: { type: "number"},
	        			PrecioMercadoCustodia				: { type: "number"},
	        			TasaMercado 						: { type: "number"},
						TasaMercadoCustodia 				: { type: "number"},
	        			PrecioCompraCustodia				: { type: "number"},
	        			Rentabilidad						: { type: "number"},
	        			ValorMercadoCustodia				: { type: "number"},
	        			MontoMercadoCustodia				: { type: "number"},
	        			DiferenciaMercadoCustodia			: { type: "number"},
	        			IdMonedaInstrReajuste				: { type: "string"},
						EsPagaderoEn						: { type: "string"},
	        			CantidadSimulCom					: { type: "number"},
	        			PPPSimulCom							: { type: "number"},
	        			ValorPPPSimulCom					: { type: "number"},
	        			PrecioMercadoSimulCom				: { type: "number"},
	        			ValorMercadoSimulCom				: { type: "number"},
	        			DiferenciaMercadoSimulCom			: { type: "number"},
	        			CantidadSimulVen					: { type: "number"},
	        			PPPSimulVen							: { type: "number"},
	        			ValorPPPSimulVen					: { type: "number"},
	        			PrecioMercadoSimulVen				: { type: "number"},
	        			ValorMercadoSimulVen				: { type: "number"},
	        			DiferenciaMercadoSimulVen			: { type: "number"},
	        			CantidadPrestamo					: { type: "number"},
	        			PPPPrestamo							: { type: "number"},
	        			ValorPPPPrestamo					: { type: "number"},
	        			PrecioMercadoPrestamo				: { type: "number"},
	        			ValorMercadoPrestamo				: { type: "number"},
	        			DiferenciaMercadoPrestamo			: { type: "number"},
	        			CantidadEmpresti					: { type: "number"},
	        			PPPEmpresti							: { type: "number"},
	        			ValorPPPEmpresti					: { type: "number"},
	        			PrecioMercadoEmpresti				: { type: "number"},
	        			ValorMercadoEmpresti				: { type: "number"},
	        			DiferenciaMercadoEmpresti			: { type: "number"},
	        			CantidadGarantia					: { type: "number"},
	        			PPPGarantia							: { type: "number"},
	        			ValorPPPGarantia					: { type: "number"},
	        			PrecioMercadoGarantia				: { type: "number"},
	        			ValorMercadoGarantia				: { type: "number"},
	        			DiferenciaMercadoGarantia			: { type: "number"},
	        			CantidadPactosV						: { type: "number"},
	        			PPPPactosV							: { type: "number"},
	        			ValorPPPPactosV						: { type: "number"},
	        			PrecioMercadoPactosV				: { type: "number"},
	        			ValorMercadoPactosV					: { type: "number"},
	        			DiferenciaMercadoPactosV			: { type: "number"},
	        			CantidadPactosC						: { type: "number"},
	        			PPPPactosC							: { type: "number"},
	        			ValorPPPPactosC						: { type: "number"},
	        			PrecioMercadoPactosC				: { type: "number"},
	        			ValorMercadoPactosC					: { type: "number"},
	        			DiferenciaMercadoPactosC			: { type: "number"},													        																        		
	        			CantidadReporteVenta				: { type: "number"},
	        			PPPReporteVenta						: { type: "number"},
	        			ValorPPPReporteVenta				: { type: "number"},
	        			PrecioMercadoReporteVenta			: { type: "number"},
	        			ValorMercadoReporteVenta			: { type: "number"},
	        			DiferenciaMercadoReporteVenta		: { type: "number"},													        			
	        			CantidadReporteCompra				: { type: "number"},
	        			PPPReporteCompra					: { type: "number"},
	        			ValorPPPReporteCompra				: { type: "number"},
	        			PrecioMercadoReporteCompra			: { type: "number"},
	        			ValorMercadoReporteCompra			: { type: "number"},
	        			DiferenciaMercadoReporteCompra		: { type: "number"},													        																        			
						CantidadTranomEgr					: { type: "number"},
						CantidadTranomIng					: { type: "number"},
						DividendoEnDinero					: { type: "number"},
	        			CantidadTransIng					: { type: "number"},
	        			CantidadTransEgr					: { type: "number"},
	        			CantidadTransVenDesEgr				: { type: "number"},  // Venta Descubierta
	        			TotalPosicion   					: { type: "number"},
	        			TotalPosicionPresunta 				: { type: "number"},
	        			Producto							: { type: "string"},
	        			RegimenTributario					: { type: "string"},
	        			ValorMercadoCustodiaReajuste		: { type: "number"} 
					}
				}	
		};

		var dsCustPortafolio = new bcs.DataSource({
			definition: {
				data: [],
				pageSize: 5,
				schema: schemaCustPortafolio
			}
		});
		//GRILLA PRINCIPAL
		$scope.confGrillaCustPortafolio = {
			dataSource  : dsCustPortafolio,
			columns     : $scope.columnasCustPortafolio,
			sortable    : true,
			pageable    : true,
			filterable  : false,
			selectable  : "row",
			reorderable : false,
			groupable   : false,
			height      : 150,
			allowCopy   : { delimeter: "\t" },
			resizable   : false,
			autoBind    : false,
			scrollable  : true,
			userSettings : true,
			excel: {
				allPages: true
			},
			rowColorBackground: function(row) {
				return row.EsNegativa === 'S' ? "red" : "black";
			}
		};
		/******************************************************/
		/*** GRID-CUSTODIA  								***/
		/******************************************************/
		/******************************************************/
		/*** GRID-CARTERA    								***/
		/******************************************************/
		modeloCarteras = function() {
			return {
				model: {
					fields: {
					Cantidad								: { type: "string"},
						Cantidad								: { type: "number"},
						ClasificacionTipoInstrumento 			: { type: "string"},
						CodigoIFRS								: { type: "string"},
						CodigoPortafolio						: { type: "string"},
						CorteMinimo								: { type: "number"},
						DescripcionPortafolio 					: { type: "string"},
						DiferenciaInversion						: { type: "number"},
						DocumentoIdentidad						: { type: "string"},
						EGRCantidad 							: { type: "number"},
						EGRFechaComprobante 					: { type: "sysdate"},
						EGRFechaLiquidacion 					: { type: "sysdate"},
						EGRFechaOperacion 	 					: { type: "sysdate"},
						EGRFolioComprobante 					: { type: "number"},
						EGRIdConcepto 							: { type: "string"},
						EGRIdTipoComprobante 					: { type: "string"},
						EGRMonto 								: { type: "number"},
						EGRMontoMonedaInstrumento 				: { type: "number"},
						EGRNombreConcepto 						: { type: "string"},
						EGRPrecio 								: { type: "number"},
						EGRTasa 								: { type: "number"},
						EGRValorRescate 						: { type: "number"},
						EsFungible								: { type: "string"},
						EsPagaderoEn							: { type: "string"},
						FechaComprobante						: { type: "sysdate"},
						FechaEmisionInstrumento					: { type: "sysdate"},
						FechaLiquidacion						: { type: "sysdate"},
						FechaOperacion							: { type: "sysdate"},
						FechaVencimientoInstrumento				: { type: "sysdate"},
						FolioCartera							: { type: "string"},
						FolioComprobante						: { type: "number"},
						IdAgente								: { type: "number"},
						IdBolsa									: { type: "string"},
						IdBoveda								: { type: "string"},
						IdCartera								: { type: "number"},
						IdClasificacionTipoInstrumento			: { type: "string"},
						IdCliente								: { type: "number"},
						IdConcepto								: { type: "string"},
						IdCondicionLiquidacion 					: { type: "string"},
						IdEmisor								: { type: "string"},
						IdEmpresa 								: { type: "number"},
						IdEstado								: { type: "string"},
						IdInstrumento							: { type: "number"},
						IdMesa									: { type: "number"},
						IdMonedaInstrReajuste 					: { type: "string"},
						IdMonedaInstrumento						: { type: "string"},
						IdMonedaEmision							: { type: "string"}, // TODO Aqui pase
						IdMonedaSistema							: { type: "string"},
						IdPortafolio							: { type: "number"},
						IdSucursal								: { type: "number"},
						IdTipoComprobante						: { type: "string"},
						IdTipoCustodia							: { type: "string"},
						IdTipoInstrumento						: { type: "string"},
						IdTipoOperacion 						: { type: "string"},
						IdTipoReajuste							: { type: "string"},
						INGCantidad								: { type: "number"},
						INGFechaLiquidacion						: { type: "sysdate"},
						INGFechaOperacion						: { type: "sysdate"},
						INGFechaPagoCorteCupon					: { type: "sysdate"},
						INGFuenteTasaPrecioMercadoActual		: { type: "string"},
						INGFechaValorizacionCartera 			: { type: "sysdate"},
						INGMontoATasaCompraActual				: { type: "number"},
						INGMontoATasaMercadoActual				: { type: "number"},
						INGMontoCapitalCorteCupon				: { type: "number"},
						INGMontoCompraActualMonedaInstrumento	: { type: "number"},
						INGMontoCorteCupon						: { type: "number"},
						INGMontoInteresCompraActual				: { type: "number"},
						InteresCorridoMonedaInstrumento  		: { type: "number"},    				
						INGMontoInteresCorteCupon				: { type: "number"},
						INGInteresesPagoCorteCupon				: { type: "number"},	
						INGMontoMercadoActualMonedaInstrumento	: { type: "number"},
						INGMontoPrecioPromedioCompra			: { type: "number"},
						INGMontoReajusteCompraActual			: { type: "number"},
						ReajusteCorridoMonedaInstrumento		: { type: "number"},
						INGPrecioCompra							: { type: "number"},
						INGPrecioMercadoActual					: { type: "number"},
						INGPrecioPromedioCompra					: { type: "number"},
						INGTasaCompra							: { type: "number"},
						INGTasaMercadoActual					: { type: "number"},
						INGValorRescate							: { type: "number"},
						ISIN									: { type: "string"},    				
						MonedaInstrumento						: { type: "string"},
						MonedaSistema 							: { type: "string"},
						MontoCompra								: { type: "number"},
						MontoCompraInicial						: { type: "number"},
						ValorCompraInicial						: { type: "number"},
						NemoSVS									: { type: "string"},
						Nemotecnico								: { type: "string"},
						NombreBolsa								: { type: "string"},
						NombreBoveda							: { type: "string"},
						NombreConcepto							: { type: "string"},
						NombreCondicionLiquidacion				: { type: "string"},
						NombreEmpresa							: { type: "string"},
						NombreEstado							: { type: "string"},
						NombreMonedaInstrReajuste 				: { type: "string"},
						NombreTipoCustodia						: { type: "string"},
						NombreTipoReajuste						: { type: "string"},
						NumeroCuentaBoveda						: { type: "string"},
						PrecioCompra							: { type: "number"},
						RazonSocial 							: { type: "string"},
						RazonSocialInstrumento					: { type: "string"},
						Rentabilidad							: { type: "number"},
						RentabilidadReal						: { type: "number"},
						CostoUF									: { type: "number"},
						TasaCompra								: { type: "number"},
						TipoInstrumento							: { type: "string"},
						TipoOpcion								: { type: "string"},
						ValorRescate 							: { type: "number"},
						NombreProductoAgrupador		            : { type: "string"},
						RegimenTributario			            : { type: "string"},
						FechaOrigenCompra						: { type: "sysdate"}
					} 
				}
			};
		};
		var dsCartPortafolio = new bcs.DataSource({
			definition: {
				data: [],
				pageSize: 5,
				schema: modeloCarteras()
			}
		});
		$scope.confGrillaCartPortafolio = {
			dataSource			: dsCartPortafolio,
			columns				: 
				[
					 {field: "FolioCartera", 							title: 'UI_FOLIO_CARTERA_ABR', 			width: "70px" }
					,{field: "FechaOperacion", 							title: 'UI_FECHA_INGRESO_ABR', 			width: "80px", template: "# var fecha = kendo.parseDate(FechaOperacion, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!FechaOperacion || FechaOperacion.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}
					,{field: "FechaLiquidacion", 						title: 'UI_FECHA_LIQUIDACION_ABR', 		width: "80px", template: "# var fecha = kendo.parseDate(FechaLiquidacion, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!FechaLiquidacion || FechaLiquidacion.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}
					,{field: "DocumentoIdentidad", 						title: 'UI_CLIENTE', 					width: "85px", 	groupable: false,  	template: "<span class='flags' title='#=RazonSocial#'>#=DocumentoIdentidad#</span>"   } 
					,{field: "CodigoPortafolio", 						title: 'UI_PORTAFOLIO_ABR', 			width: "50px",  hidden: true, groupable: false }
					,{field: "NombreTipoCustodia", 						title: 'UI_TIPO_CUSTODIA', 				width: "50px",  hidden: true, groupable: false}
					,{field: "IdClasificacionTipoInstrumento", 			title: 'UI_MERCADO', 					width: "50px", 	groupable: false, 	template: "<span class='flags' title='#=ClasificacionTipoInstrumento#'>#=IdClasificacionTipoInstrumento#</span>" }
					,{field: "TipoInstrumento", 						title: 'UI_TIPO_INSTRUMENTO',	 		width: "100px", groupable: false,	attributes:{style:"white-space:nowrap;"}}
					,{field: "Nemotecnico", 							title: 'UI_INSTRUMENTO', 				width: "100px", groupable: false,	attributes:{style:"white-space:nowrap;"}}	    		
					,{field: "ISIN", 									title: 'UI_ISIN', 						width: "100px", hidden: true, groupable: false,		attributes:{style:"white-space:nowrap;"}}
					,{field: "IdEmisor", 								title: 'UI_EMISOR', 					width: "80px", 	groupable: false,	attributes:{style:"white-space:nowrap;"}}
					,{field: "FechaVencimientoInstrumento", 			title: 'UI_VENCIMIENTO', 				width: "80px",  template: "# var fecha = kendo.parseDate(FechaVencimientoInstrumento, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!FechaVencimientoInstrumento || FechaVencimientoInstrumento.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}
					,{field: "IdBoveda", 							    title: 'UI_BOVEDA', 					width: "55px", 	groupable: false }
					,{field: "NumeroCuentaBoveda", 						title: 'UI_NUMERO_CUENTA', 				width: "55px", 	hidden: true, groupable: false,		attributes:{style:"white-space:nowrap;"}}
					,{field: "INGCantidad", 							title: 'UI_CANTIDAD', 					width: "80px", 	template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad',IdMonedaInstrumento!=''?IdMonedaInstrumento:IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }	    		
					,{field: "INGValorRescate", 						title: 'UI_VALOR_RESCATE', 				width: "80px",  hidden: true, template : " <label> #:  (util.bcs_formato(INGValorRescate,'GEN','Cantidad',IdMonedaInstrReajuste,'','')).valor #</label>",	aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,4)#" }
					,{field: "IdMonedaInstrumento", 					title: 'UI_MONEDA_PAGO', 				width: "55px", 	hidden: true, groupable: false }//Modificado
					,{field: "IdTipoReajuste", 							title: 'UI_TIPO_REAJUSTE', 				width: "55px", 	groupable: false}
					,{field: "INGPrecioCompra", 						title: 'UI_PRECIO_COMPRA_ABR', 			width: "90px", 	hidden: true, template : " <label> #:  (util.bcs_formato(INGPrecioCompra,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>"}
					,{field: "INGTasaCompra", 							title: 'UI_TASA_COMPRA', 				width: "60px", 	template : " <label> #:  (util.bcs_formato(INGTasaCompra,'GEN','Tasa',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }				
					,{field: "INGMontoCompraActualMonedaInstrumento", 	title: 'UI_VALOR_COMPRA',				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoCompraActualMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }				
					,{field: "INGMontoATasaCompraActual", 				title: 'UI_MONTO_COMPRA', 				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    			    		
					,{field: "MontoCompraInicial",						title: 'UI_MONTO_COMPRA_INICIAL',		width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(MontoCompraInicial,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
					,{field: "ValorCompraInicial", 						title: 'UI_VALOR_COMPRA_INICIAL',		width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(ValorCompraInicial,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"}
					,{field: "INGMontoInteresCompraActual",				title: 'UI_INTERES_DIARIO', 			width: "105px",	hidden: true, template : " <label> #:  (util.bcs_formato(INGMontoInteresCompraActual,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>"}
					,{field: "INGMontoReajusteCompraActual", 			title: 'UI_REAJUSTE_DIARIO', 			width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(INGMontoReajusteCompraActual,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }								
					,{field: "INGPrecioMercadoActual", 					title: 'UI_PRECIO_MERCADO', 			width: "90px", 	hidden: true, template : " <label> #:  (util.bcs_formato(INGPrecioMercadoActual,'GEN','Precio',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
					,{field: "INGTasaMercadoActual", 					title: 'UI_TASA_MERCADO', 				width: "60px", 	hidden: true, template : " <label> #: (util.bcs_formato(INGTasaMercadoActual,'GEN','Tasa',IdMonedaInstrReajuste,IdTipoInstrumento,'')).valor #</label>" }
					,{field: "INGMontoMercadoActualMonedaInstrumento", 	title: 'UI_VALOR_MERCADO',				width: "105px", template : " <label> #:  (util.bcs_formato(INGMontoMercadoActualMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
					,{field: "Rentabilidad", 							title: 'UI_RENTABILIDAD', 				width: "60px",  format:"{0:n2}"}
					,{field: "RentabilidadReal",						title: 'UI_RENTABILIDAD_REAL',			width: "60px",  hidden: true, format:"{0:n2}"}
					,{field: "CostoUF",									title: 'UI_COSTO_UF',					width: "60px",  hidden: true, format:"{0:n2}"}
					,{field: "INGMontoATasaMercadoActual", 				title: 'UI_MONTO_MERCADO', 				width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(INGMontoATasaMercadoActual,'GEN','Monto',IdMonedaSistema,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
					,{field: "INGFuenteTasaPrecioMercadoActual", 		title: 'UI_FUENTE_PRECIO_MERCADO', 		width: "150px", hidden: true, groupable: false,	attributes:{style:"white-space:nowrap;"}}
					,{field: "INGFechaValorizacionCartera", 			title: 'UI_FECHA_VALORIZACION', 		width: "80px",  hidden: true, template: "# var fecha = kendo.parseDate(INGFechaValorizacionCartera, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!INGFechaValorizacionCartera || INGFechaValorizacionCartera.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}
					,{field: "DiferenciaInversion", 					title: 'UI_DIFERENCIA_MERCADO', 		width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(DiferenciaInversion,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }	    		
					,{field: "IdMonedaInstrumento", 					title: 'UI_MONEDA_INSTRUMENTO', 		width: "55px", 	groupable: false }	    		
					,{field: "EsFungible", 								title: 'UI_FUNGIBLE', 					width: "50px",  hidden: true}
					,{field: "IdConcepto", 								title: 'UI_CONCEPTO_ABR', 				width: "55px", 	hidden: true, groupable: false, 	template: "<span class='flags' title='#=NombreConcepto#'>#=IdConcepto#</span>"  }
					,{field: "NombreConcepto", 							title: 'UI_CONCEPTO', 					width: "100px", hidden: true, groupable: false,	attributes:{style:"white-space:nowrap;"}}
					,{field: "CodigoIFRS", 								title: 'UI_CATEGORIA_IFRS_ABR', 		width: "80px",  hidden: true}
					,{field: "NemoSVS", 								title: 'UI_NEMO_SVS', 					width: "80px", 	hidden: true, groupable: false,	attributes:{style:"white-space:nowrap;"}}	    		
					,{field: "IdTipoComprobante", 						title: 'UI_TIPO_COMPROBANTE', 			width: "60px",  hidden: true}
					,{field: "TipoOpcion",		 						title: 'UI_TIPO_OPCION',	 			width: "60px",  hidden: true}
					,{field: "FolioComprobante", 						title: 'UI_FOLIO_COMPROBANTE', 			width: "50px", 	hidden: true, format: "{0:n0}" }
					,{field: "FechaComprobante", 						title: 'UI_FECHA_COMPROBANTE', 			width: "80px",  hidden: true, template: "# var fecha = kendo.parseDate(FechaComprobante, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!FechaComprobante || FechaComprobante.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}	    		
					,{field: "INGFechaPagoCorteCupon", 					title: 'UI_FECHA_CORTE_CUPON', 			width: "80px",  hidden: true, template: "# var fecha = kendo.parseDate(INGFechaPagoCorteCupon, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!INGFechaPagoCorteCupon || INGFechaPagoCorteCupon.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}
					,{field: "INGMontoCorteCupon", 						title: 'UI_MONTO_CORTE_CUPON', 			width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(INGMontoCorteCupon,'GEN','Monto','" + $scope.data.IdMonedaSistema + "','','')).valor #</label>" }
					,{field: "INGMontoCapitalCorteCupon", 				title: 'UI_CAPITAL_CORTE_CUPON', 		width: "105px", hidden: true, template : " <label> #:  (util.bcs_formato(INGMontoCapitalCorteCupon,'GEN','Monto','" + $scope.data.IdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
					,{field: "INGInteresesPagoCorteCupon", 				title: 'UI_INTERES_CORTE_CUPON', 		width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(INGInteresesPagoCorteCupon,'GEN','Monto','" + $scope.data.IdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
					,{field: "InteresCorridoMonedaInstrumento",			title: 'UI_INTERESES_CORRIDOS', 		width: "105px",	hidden: true,		template : " <label> #:  (util.bcs_formato(InteresCorridoMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }
					,{field: "ReajusteCorridoMonedaInstrumento", 		title: 'UI_REAJUSTES_VAC_CORRIDO', 		width: "105px", hidden: true,		template : " <label> #:  (util.bcs_formato(ReajusteCorridoMonedaInstrumento,'GEN','Monto',IdMonedaInstrReajuste,'','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#" }																
					,{field: "NombreProductoAgrupador", 				title: 'UI_PRODUCTO',			        width: "80px", 	hidden: true}
					,{field: "RegimenTributario", 						title: 'UI_REGIMEN_TRIBUTARIO',	        width: "100px", hidden: true}
					,{field: "FechaOrigenCompra", 						title: 'UI_FECHA_ORIGEN_COMPRA',	    width: "100px", hidden: true, template: "# var fecha = kendo.parseDate(FechaOrigenCompra, 'yyyy-MM-dd HH:mm:ss.fff'); # #= (!FechaOrigenCompra || FechaOrigenCompra.indexOf('0001-01-01') === 0 || !fecha) ? '' : kendo.toString(fecha, 'dd-MM-yyyy') #"}
					,{field: "NombreEstado", 							title: 'UI_ESTADO', 					width: "80px",  hidden: true, groupable: false }
				],
			sortable    : true,
			pageable    : true,
			filterable  : false,
			selectable  : "row",
			reorderable : false,
			groupable   : false,
			height      : 150,
			allowCopy   : { delimeter: "\t" },
			resizable   : false,
			autoBind    : false,
			scrollable  : true,
			userSettings : true,	
			excel: {
				allPages: true
			}
		};
		/******************************************************/
		/*** GRID-CARTERA	 								***/
		/******************************************************/
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/

	$scope.buscar = function()
	{
		$scope.config.callCount =0;
		bcsComunes.ventanaOverlay(idVentana,true);
		$scope.limpiar(false);

		var dataResumenPortafolioCliente =
		{
			p_IdUsuario				: $scope.SesionIdUsuario,
			p_IdCliente             : $scope.oClientePortafolio.seleccionado.IdCliente,
			p_IdEmpresa				: $scope.SesionIdEmpresa,
			p_IdPortafolio          : $scope.oClientePortafolio.seleccionado.IdPortafolio,
			p_IdMonedaFlujoCaja     : $scope.oConfiguracionCuentaInversion.seleccionado.IdMoneda,
			p_FechaFlujoCaja        : ($scope.data.kngFechaFlujoCaja).toISOString().split("T")[0],
			p_FechaDesde 			: util.dateInicioDia($scope.SesionFechaSistema),
			p_FechaHasta 			: util.dateTerminoDia($scope.SesionFechaSistema)
		};
		
		var urlObtenerResumenPortafolioCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=8798791";
		
		/*******************************/
		/*** Resumen Portafolio Cliente ***/
		/*******************************/
		var r = $resource("", {},
		{
			'ResumenPortafolioCliente': { method: "POST", url: urlObtenerResumenPortafolioCliente}
		});
		
		var dataResumenPortafolioCliente = r.ResumenPortafolioCliente(dataResumenPortafolioCliente);
		dataResumenPortafolioCliente.$promise.then(function(result)
		{
			// Data Ejecutivo + fecha consulta
				if(result.P_OUT_INFORMACION_EJECUTIVO[0]){
					$scope.Ejecutivo.nombre = result.P_OUT_INFORMACION_EJECUTIVO[0].NombreEjecutivo || "";
					$scope.Ejecutivo.telefono = result.P_OUT_INFORMACION_EJECUTIVO[0].TelefonoEjecutivo || "";
					$scope.Ejecutivo.email = result.P_OUT_INFORMACION_EJECUTIVO[0].EmailEjecutivo || "";
				}
				if(result.P_OUT_FECHACONSULTA){
					$scope.Ejecutivo.fechaConsulta = result.P_OUT_FECHACONSULTA || "";
				}
				$("#ejecutivo-data").show();

			// ComposicionActivos y CustodiaPortafolio
				var composicion = result.P_OUT_COMPOSICION_ACTIVOS || [];
				var custodiaPortafolio = result.P_OUT_CUSTODIACHART || [];

				var dataComposicionActivos = composicion.map(function(item, index) {
					return {
						category: item.nombreGrupo,
						value: parseFloat(item.porcentaje),
						color: getColor(item.nombreGrupo),
						clasificacionGrupo: item.clasificacionGrupo
					};
				});

				var randomItem = (dataComposicionActivos && dataComposicionActivos.length)
				? dataComposicionActivos[Math.floor(Math.random() * dataComposicionActivos.length)]
				: null;

				$("#chart").kendoChart({
						chartArea: {
							background:"",
							margin: {
								top: 0,
								bottom: 0
							},
							padding: {
								top: 0,
								bottom: 0
							}
						},
						legend: {
							position: "right",
							offsetY: 0
						},
						seriesDefaults: {
							type: "donut",
							startAngle: 150,
							
						},
						series: [{
							name: "Inversión",
							data: dataComposicionActivos,
							labels: {
								visible: false
							},
							size: 35,
    						holeSize: 30
						}],
						tooltip: {
							visible: true,
							template: "#= category # (#= series.name #): #= value #%"
						},
						seriesClick: function(e) {
							mostrarDescripcionChart(1, e);
							abrirChartCustodia(e.dataItem,custodiaPortafolio);
						},
						legendItemClick: function(e) {
							e.preventDefault();
					
							var textoLeyenda = e.text;

							var dataItem = dataComposicionActivos.find(function(item) {
								return item.category === textoLeyenda;
							});
					
							if (!dataItem) {
								return;
							}
					
							var total = dataComposicionActivos.reduce(function(acum, item) {
								return acum + (parseFloat(item.value) || 0);
							}, 0);
					
							var fakeEvent = {
								category: dataItem.category,
								dataItem: dataItem,
								value: parseFloat(dataItem.value) || 0,
								percentage: total > 0 ? (parseFloat(dataItem.value) || 0) / total : 0,
								series: e.series,
								sender: e.sender
							};
					
							mostrarDescripcionChart(1, fakeEvent);
							abrirChartCustodia(fakeEvent.dataItem, custodiaPortafolio);
						}
				});

				// Muestra primer chart
				mostrarChartComposicion(dataComposicionActivos);
				// Carga segundo chart al azar
				abrirChartCustodia(randomItem,custodiaPortafolio);
				mostrarDescripcionChart(1, randomItem);

			// Flujo Caja
				usosUI = result.P_OUT_USOS;
				var dataFlujoCaja = construirDataFlujoCaja(result.P_OUT_FLUJOCAJA);
				$("#treeMovimientos").kendoTreeList({
					height: 240,
					scrollable: true,
				
					dataSource: {
						data: dataFlujoCaja,
						schema: {
							model: {
								id: "id",
								parentId: "parentId",
								fields: {
									descripcion: { type: "string" },
									monto: { type: "number" }
								}
							}
						}
					},
				
					columns: [
						{
							field: "descripcion",
							title: "Descripción",
							expandable: true,
							attributes: { style: "text-align:left;" },
							width: 100
						},
						{
							field: "monto",
							title: "Monto",
							template: "#= (moneda && moneda.toUpperCase().trim() === 'CLP') ? kendo.toString(monto, 'n0') : kendo.toString(monto, 'n2') #",
							attributes: { style: "text-align:right;" },
							headerAttributes: { style: "text-align:right;" },
							width: 80
						}
					],
				
					dataBound: function(e) {
						var rows = e.sender.tbody.find("tr");
					
						rows.each(function(index) {
							var item = e.sender.dataItem(this);
							if (!item) return;
					
							// alternar color: par/impar
							if (index % 2 === 0) {
								$(this).addClass("fila-naranja");
							}
						});
					}
				});
				if(dataFlujoCaja.length>0){
					$("#grafico-box").show();
				}

			// Ordenes
				var ordenes = result.P_OUT_ORDENES;
				ordenes.forEach(function(item) {
					if (item.FechaOperacionOrden) {
						item.FechaOperacionOrden = parseFechaUtcALocal(item.FechaOperacionOrden);
					}
				});
				
				$scope.gridApiOrdenes.dataSource.data(ordenes);
				$("#grid-orden").show();
			// Custodia - Cartera
				 var custPortafolio = (result.P_OUT_GRIDCUSTODIA != null && result.P_OUT_GRIDCUSTODIA.length > 0)
				? result.P_OUT_GRIDCUSTODIA
				: [];
				$scope.gridApiCustPortafolio.dataSource.data(custPortafolio);
				$("#grid-custodia").show(); 
				$scope.data.dataCarteras = (result.P_OUT_GRIDCARTERA != null && result.P_OUT_GRIDCARTERA.length > 0)
				? result.P_OUT_GRIDCARTERA
				: [];

		}).finally(function(){
			$scope.config.callCount++;
		});
		
	}; 

	$scope.limpiar = function (LimpiaClientePortafolio) {
		if($scope.filtros.IdPortafolio && LimpiaClientePortafolio){
			$scope.filtros.IdPortafolio = null;
		} 
	
		if ($scope.oClientePortafolio.seleccionado && LimpiaClientePortafolio) {
			$scope.oClientePortafolio.seleccionado = false;
			$scope.oClientePortafolio.idActual = false;
		}	
		$scope.ejecutivo = {
			nombre: "",
			telefono: "",
			email: "",
			fechaConsulta: ""
		};
	
		$("#ejecutivo-data").hide();
	
		$("#desc-1").text("-");
		$("#pct-1").text("-");
		$("#detalle-chart-1").hide();
	
		$("#nemo").text("-");
		$("#monto").text("-");
		$("#cantidad").text("-");
		$("#precio").text("-");
		$("#moneda").text("-");
		$("#rentabilidad").text("-");
		$("#detalle-chart-2").hide();
	
		var chart1 = $("#chart").data("kendoChart");
		if (chart1) {
			chart1.destroy();
			$("#chart").empty();
			$scope.tieneDataChart1 = false;
		}
	
		var chart2 = $("#chart2").data("kendoChart");
		if (chart2) {
			chart2.destroy();
			$("#chart2").empty();
			$scope.tieneDataChart2 = false;
		}
		var tree = $("#treeMovimientos").data("kendoTreeList");
		if (tree) {
			tree.destroy(); 
			$("#treeMovimientos").empty(); 
		}
		if ($scope.data.kngFechaFlujoCaja) {
			$scope.data.kngFechaFlujoCaja = util.dateInicioDia($scope.SesionFechaSistema);
		}
		$("#grafico-box").hide();
	
		var combo = $("#UI_SOCIO_NEGOCIO").data("kendoComboBox");
		if (combo) {
			combo.value("");
			combo.text("");
			combo.select(-1);
			combo.trigger("change");
		}
			
		$scope.gridApiOrdenes.dataSource.removeAll();
		$("#grid-orden").hide();

 		$scope.gridApiCustPortafolio.dataSource.removeAll();
		 $scope.gridApiCartPortafolio.dataSource.removeAll();
		$("#grid-custodia").hide(); 
		
	};
	
/******************************************************/
/*** WATCH											***/
/******************************************************/
	
	$scope.$watch('config.callCount', function(newValue, oldValue) {
		if(newValue==1){
			bcsComunes.ventanaOverlay(idVentana,false);
		}
	});
	$scope.$watch("oClientePortafolio", function(nuevo, oldValue) {
		if(nuevo.seleccionado == false && oldValue.seleccionado){
			$scope.limpiar(true);
		}
		if(nuevo.seleccionado && oldValue.seleccionado && oldValue.seleccionado != nuevo.seleccionado){
			$scope.limpiar(false);
		}
	}, true);
	$scope.$watch("oConfiguracionCuentaInversion", function(nuevo, oldValue) {
		if(nuevo.idActual != false && $scope.oClientePortafolio.seleccionado && oldValue.seleccionado != false && nuevo.idActual != oldValue.idActual ){
			var filters = {};
			filters.p_IdEmpresa				= $scope.SesionIdEmpresa;
			filters.p_IdUsuario				= $scope.SesionIdUsuario;
			filters.p_IdModulo				= 'SOC';
			filters.p_UsaReglasNegocio		= 'S';
			filters.p_FechaDesde			= util.dateSPdate($scope.data.kngFechaFlujoCaja);
			filters.p_FechaHasta			= util.dateSPdate($scope.data.kngFechaFlujoCaja);
			filters.p_Uso					= null;
			filters.p_DesplegarEnUI			= 'S';
			filters.p_AgruparPorOrigen		= 'N';
			filters.p_PortafolioAConsultar	= [{IdPortafolio:$scope.oClientePortafolio.seleccionado.IdPortafolio,IdMoneda: $scope.oConfiguracionCuentaInversion.seleccionado.IdMoneda}];
			FlujoCaja(filters);
		}
		else if(nuevo.idActual == false && $scope.oClientePortafolio.seleccionado && nuevo.filtros.IdPortafolio.value != oldValue.filtros.IdPortafolio.value){
			$scope.oConfiguracionCuentaInversion.idActual = oldValue.idActual;
			$scope.oConfiguracionCuentaInversion.seleccionado = oldValue.seleccionado;
		}
	}, true);
	$scope.$watch("data.kngFechaFlujoCaja", function(nuevo, oldValue) {
		if(nuevo==null && $scope.oClientePortafolio.seleccionado){
			$scope.$evalAsync(function () {
				$scope.data.kngFechaFlujoCaja = oldValue;
				var widget = $("#fechaFlujoCaja").find("input").data("kendoDatePicker");
				if (widget) {
					widget.value(oldValue);
					widget.trigger("change");
				}
			})
		}
		else if(nuevo!=null && $scope.oClientePortafolio.seleccionado && oldValue!=null && nuevo.getTime()!==oldValue.getTime()){
			var filters = {};
			filters.p_IdEmpresa				= $scope.SesionIdEmpresa;
			filters.p_IdUsuario				= $scope.SesionIdUsuario;
			filters.p_IdModulo				= 'SOC';
			filters.p_UsaReglasNegocio		= 'S';
			filters.p_FechaDesde			= util.dateSPdate($scope.data.kngFechaFlujoCaja);
			filters.p_FechaHasta			= util.dateSPdate($scope.data.kngFechaFlujoCaja);
			filters.p_Uso					= null;
			filters.p_DesplegarEnUI			= 'S';
			filters.p_AgruparPorOrigen		= 'N';
			filters.p_PortafolioAConsultar	= [{IdPortafolio:$scope.oClientePortafolio.seleccionado.IdPortafolio,IdMoneda: $scope.oConfiguracionCuentaInversion.seleccionado.IdMoneda}];
			FlujoCaja(filters);
		}
	});
	$scope.$watch('seleccionCustodia', function(newValue, oldValue) {
		if(newValue!==null){
			var listaFiltrada = $scope.data.dataCarteras.filter(function(c) {
				return c.IdInstrumento === newValue.IdInstrumento;
			});
			
			$scope.gridApiCartPortafolio.dataSource.data(listaFiltrada);
		}
	});
	
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});
