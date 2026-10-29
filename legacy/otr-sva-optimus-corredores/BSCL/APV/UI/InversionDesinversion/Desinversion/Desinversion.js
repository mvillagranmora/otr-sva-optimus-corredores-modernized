(function() {
	angular.module('ModuloAPV').controller('DesinversionAPVCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsConsultasAPV,bcsServiciosAPV,bcsServicioOrden,bcsServicioPlataforma,bcsConfiguracionAPV,bcsAlerta,OPCIONES_BCSALERTA)
	{
		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var montoPesos = 0;
		var desAPV = this;
		var OrdenesRentaVariable = Array();
		var OrdenesFondosMutuos = Array();
		var cobrosCustodias = [];
		var cobrosModificadosFinal = [];
		var varCantidad = 0;
		var inicio = 0;
		var IdSolicitudAporteRescate = 0;
		var mensajeSalida = Array();
		var OrdenesFM = Array();
		var mensajeResultadoOrdenes = new Array();
		
		desAPV.IdPortafolioCliente = 0;
		desAPV.datosForm = {};
		desAPV.fn = {};
		desAPV.datasource = {};
		desAPV.ConfiguracionCmbo = {};
		desAPV.aporteNormal = {};
		desAPV.consultas = {};
		desAPV.TipoOrdenHabilito = 'RV';
		desAPV.OrdenesRV = [];
		desAPV.datosForm.fechaInicial = $scope.SesionFechaSistema; //util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
		desAPV.datosForm.IdTipoOperacion = 'COMACN'
		desAPV.kngTotalPatrimonio = 0;
		desAPV.datosForm.kngTotalDesinvertir = parametrosVentana.IdSocioNegocio !== undefined ? parametrosVentana.MontoTotalPesos : 0;
		desAPV.existeSocio = false;
		desAPV.datosForm.kngPorcentaje = 0;
		desAPV.datosForm.kngCantidadDesinvertir = 0;
		desAPV.mostrar = false;
		desAPV.habilitaEntidad = false;
		desAPV.habilitaEntidadDestino = false;
		$scope.kOptFechaTransferencia = {};
		//$scope.kOptFechaTransferencia.min = $scope.$root.SesionFechaSistema;
		$scope.kOptFechaEgreso = {};
		//$scope.kOptFechaEgreso.min = $scope.$root.SesionFechaSistema;
		desAPV.MostrarBoveda = true;
		
		var entrada = false;
		var primeraVez = true;
		var IdTraspaso = 0;
		var calcularDesinversionPrimeraVez = parametrosVentana.IdTraspaso?true:false;
		
		desAPV.HabilitoControlesBusqueda	= true;
		desAPV.HabilitoPanelPrincipal		= true;
		desAPV.HabilitoAutorizadoDarOrdenes = false;
		
		//******************************************************************************************************************************/
		//************************************************* Definicion de DataSources **************************************************/
		//******************************************************************************************************************************/
			var defCustodia =
			{
				type : "array",
				definition :
				{
	                serverFiltering : false, // Filtrado de datos en el servidor.
	                serverPaging    : false, // Paginado de los datos en el servidor.
	                serverSorting   : false, // Ordenamiento de los datos en el servidor.
	                sort 			: { field: "FolioCartera", dir: "ASC" },
	                pageSize        : 4,
	                schema 			: {
	                	model			: {
	                		fields			: {
	                			NombreProductoAgrupador 			: { type: "string" },
	                			IdProductoAgrupador					: { type: "number" },
								Regimen								: { type: "string" },
								IdClasificacionTipoInstrumento		: { type: "string" },
								IdTipoInstrumento					: { type: "string" },
								Nemotecnico							: { type: "string" },
		    					CantidadCustodia					: { type: "number" },
								PPPCustodia							: { type: "number" },
								ValorPPPCustodia					: { type: "number" },
    							MontoPPPCustodia					: { type: "number" },
    							CantidadADesinvertir				: { type: "number" },
    							MontoADesinvertirCLP				: { type: "number" },
    							MontoADesinvertir					: { type: "number" },
    							IdPortafolio						: { type: "number" },
    							IdEmisor							: { type: "string" },
    							IdInstrumento						: { type: "number" },
    							IdMonedaInstrReajuste				: { type: "string" },
	                			
    		        			DocumentoIdentidad					: { type: "string" },
    		        			DocumentoIdentidadRutCavali         : { type: "string" },
    		        			NombreCliente						: { type: "string" },
    		        			Relacion							: { type: "string" },
    		        			ISIN								: { type: "string" },
    		        			ClasificacionRiesgoInstrumento		: { type: "string" },
    		        			EsFungible							: { type: "string" },
    		        			EsPagaderoEn						: { type: "string" },
    		        			IdBoveda							: { type: "string" },
    		        			NumeroCuentaBoveda					: { type: "string" },
    		        			TotalPosicion   					: { type: "number" },
    		        			TotalPosicionPresunta 				: { type: "number" },
    		        			Rentabilidad						: { type: "number" },
    		        			
    		        			PrecioMercadoCustodia				: { type: "number" },
    		        			ValorMercadoCustodia				: { type: "number" },
    		        			MontoMercadoCustodia				: { type: "number" },
    		        			DiferenciaMercadoCustodia			: { type: "number" },
    		        			
    		        			CantidadGarantia					: { type: "number" },
    		        			PrecioMercadoGarantia				: { type: "number" },
    		        			ValorMercadoGarantia				: { type: "number" },
    		        			DiferenciaMercadoGarantia			: { type: "number" },
    		        			
    		        			CantidadSimulCom					: { type: "number" },
    		        			PrecioMercadoSimulCom				: { type: "number" },
    		        			ValorMercadoSimulCom				: { type: "number" },
    		        			DiferenciaMercadoSimulCom			: { type: "number" },
    		        			
    		        			CantidadSimulVen					: { type: "number" },
    		        			PrecioMercadoSimulVen				: { type: "number" },
    		        			ValorMercadoSimulVen				: { type: "number" },
    		        			DiferenciaMercadoSimulVen			: { type: "number" },
    		        			
    		        			CantidadPrestamo					: { type: "number" },
    		        			PrecioMercadoPrestamo				: { type: "number" },
    		        			ValorMercadoPrestamo				: { type: "number" },
    		        			DiferenciaMercadoPrestamo			: { type: "number" },
    		        			
    		        			CantidadEmpresti					: { type: "number" },
    		        			PrecioMercadoEmpresti				: { type: "number" },
    		        			ValorMercadoEmpresti				: { type: "number" },
    		        			DiferenciaMercadoEmpresti			: { type: "number" },
    		        			
    		        			CantidadPactosV						: { type: "number" },
    		        			PrecioMercadoPactosV				: { type: "number" },
    		        			ValorMercadoPactosV					: { type: "number" },
    		        			DiferenciaMercadoPactosV			: { type: "number" },
    		        			
    		        			CantidadPactosC						: { type: "number" },
    		        			PrecioMercadoPactosC				: { type: "number" },
    		        			ValorMercadoPactosC					: { type: "number" },
    		        			DiferenciaMercadoPactosC			: { type: "number" },
    		        			
    		        			CantidadReporteCompra				: { type: "number" },
    		        			PrecioMercadoReporteCompra			: { type: "number" },
    		        			ValorMercadoReporteCompra			: { type: "number" },
    		        			DiferenciaMercadoReporteCompra		: { type: "number" },
    		        			
      		        			CantidadReporteVenta				: { type: "number" },
    		        			PrecioMercadoReporteVenta			: { type: "number" },
    		        			ValorMercadoReporteVenta			: { type: "number" },
    		        			DiferenciaMercadoReporteVenta		: { type: "number" },													        			
													        																        			
    		        			CantidadTransIng					: { type: "number" },
    		        			CantidadTransEgr					: { type: "number" },
    		        			SaldoDisponibleCLP					: { type: "number" },
    		        			SaldoDisponibleUSD					: { type: "number" },
    		        			SaldoDisponibleEUR					: { type: "number" }
	                		}
	                	}
	                }
	    		}
			};
		
		//******************************************************************************************************************************/
		//*************************************************** Definicion de grillas ****************************************************/
		//******************************************************************************************************************************/
		var dsCustodia = new bcs.DataSource(defCustodia);
		desAPV.Ordenes = {
				dataSource: dsCustodia,
				columns: [

					{field: "NombreProductoAgrupador"			,title: $translate.instant('UI_PRODUCTO')								,width: "150px", 	align: "left" , sortable : true, editable: false },
					{field: "IdProductoAgrupador"				,title: $translate.instant('UI_ID_PRODUCTO_AGRUPADOR')					,width: "80px", 	align: "rigth", sortable : true, editable: false, hidden: true },
					{field: "Regimen"							,title: $translate.instant('UI_PORTAFOLIO')								,width: "60px", 	align: "left" , sortable : true, editable: false },
					{field: "IdClasificacionTipoInstrumento"	,title: $translate.instant('UI_MERCADO')								,width: "50px", 	align: "left" , sortable : true, editable: false },
					{field: "IdTipoInstrumento" 				,title: $translate.instant('UI_TIPO_INSTRUMENTO')						,width: "90px", 	align: "rigth", sortable : true, editable: false },
					{field: "Nemotecnico" 						,title: $translate.instant('UI_INSTRUMENTO')							,width: "75px", 	align: "rigth", sortable : true, editable: false },
				    
					{field: "CantidadCustodia"					,title: $translate.instant('UI_CUSTODIA_DISPONIBLE')					,width: "80px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(CantidadCustodia ,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','')).valor #</label>" },
					{field: "PPPCustodia" 						,title: $translate.instant('UI_PPP_CUSTODIA')							,width: "80px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(PPPCustodia ,'GEN','Monto','USD','','')).valor #</label>" },
					{field: "ValorPPPCustodia" 					,title: $translate.instant('UI_VALOR_PPP_CUSTODIA')						,width: "80px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(ValorPPPCustodia ,'GEN','Monto','CLP','','')).valor #</label>" },
				    {field: "MontoPPPCustodia"					,title: $translate.instant('UI_MONTO_PPP_CUSTODIA')						,width: "80px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoPPPCustodia ,'GEN','Monto','CLP','','')).valor #</label>"  },
				    {field: "CantidadADesinvertir"				,title: $translate.instant('UI_CANTIDAD_A_DESINVERTIR')					,width: "110px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(CantidadADesinvertir ,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','')).valor #</label>" },
				    {field: "MontoADesinvertirCLP"				,title: $translate.instant('UI_MONTO_A_DESINVERTIR_MONEDA_SISTEMA')		,width: "100px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoADesinvertirCLP ,'GEN','Monto','CLP','','')).valor #</label>"  },
				    {field: "MontoADesinvertir"					,title: $translate.instant('UI_MONTO_A_DESINVERTIR')					,width: "100px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(MontoADesinvertir ,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','')).valor #</label>"  },
				    {field: "IdPortafolio"						,title: $translate.instant('UI_PORTAFOLIO')								,width: "80px", 	align: "rigth", sortable : true, editable: false, hidden: true },
				    {field: "IdEmisor"							,title: $translate.instant('UI_EMISOR')									,width: "80px", 	align: "rigth", sortable : true, editable: false, hidden: true },
				    {field: "IdMonedaInstrReajuste"				,title: $translate.instant('UI_TIPO_REAJUSTE')							,width: "80px", 	align: "rigth", sortable : true, editable: false, hidden: true },
					{field: "DocumentoIdentidad"				,title: $translate.instant('UI_CLIENTE')								,width: "60px", 	sortable : true, editable: false },
					{field: "DocumentoIdentidadRutCavali"		,title: $translate.instant('UI_RUT_CAVALI')								,width: "60px", 	sortable : true, editable: false,	hidden: true },
			        {field: "NombreCliente"						,title: $translate.instant('UI_NOMBRE')									,width: "200px",	sortable : true, editable: false },
			        {field: "Relacion"							,title: $translate.instant('UI_RELACION')								,width: "50px", 	sortable : true, editable: false },
					{field: "ISIN"								,title: $translate.instant('UI_ISIN')									,width: "100px",	sortable : true, editable: false,	hidden: true },
					{field: "ClasificacionRiesgoInstrumento"	,title: $translate.instant('UI_LISTA')									,width: "50px", 	sortable : true, editable: false },
					{field: "EsFungible"						,title: $translate.instant('UI_FUNGIBLE')								,width: "50px",		sortable : true, editable: false },
					{field: "EsPagaderoEn"						,title: $translate.instant('UI_MONEDA_PAGO')							,width: "60px", 	sortable : true, editable: false },
					{field: "IdBoveda"							,title: $translate.instant('UI_BOVEDA')									,width: "60px", 	sortable : true, editable: false },
					{field: "NumeroCuentaBoveda"				,title: $translate.instant('UI_NUMERO_CUENTA')							,width: "55px", 	sortable : true, editable: false,	hidden: true },
					{field: "TotalPosicion"						,title: $translate.instant('UI_TOTAL_CUSTODIA')							,width: "100px",	template : " <label> #: util.bcs_formato(TotalPosicion,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>" ,aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "TotalPosicionPresunta"				,title: $translate.instant('UI_TOTAL_PRESUNTO')							,width: "100px",	template : " <label> #: util.bcs_formato(TotalPosicionPresunta,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>" ,aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "Rentabilidad"						,title: $translate.instant('UI_RENTABILIDAD')							,width: "100px",	template : " <label> #: util.bcs_formato(Rentabilidad,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>"},
					
					{field: "PrecioMercadoCustodia"				,title: $translate.instant('UI_PRECIO_MERCADO_CUSTODIA')				,width: "100px",  	template : " <label> #: util.bcs_formato(PrecioMercadoCustodia,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoCustodia"				,title: $translate.instant('UI_VALOR_MERCADO_CUSTODIA')					,width: "100px",  	template : " <label> #: util.bcs_formato(ValorMercadoCustodia,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "MontoMercadoCustodia"				,title: $translate.instant('UI_MONTO_MERCADO_CUSTODIA')					,width: "100px",  	template : " <label> #: util.bcs_formato(MontoMercadoCustodia,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoCustodia"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_CUSTODIA')			,width: "100px",  	template : " <label> #: util.bcs_formato(DiferenciaMercadoCustodia,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadGarantia"					,title: $translate.instant('UI_GARANTIAS')								,width: "100px",  	template : " <label> #:  util.bcs_formato(CantidadGarantia,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoGarantia"				,title: $translate.instant('UI_PRECIO_MERCADO_GARANTIAS')				,width: "100px",  	template : " <label> #: util.bcs_formato(PrecioMercadoGarantia,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoGarantia"				,title: $translate.instant('UI_MONTO_MERCADO_GARANTIAS')				,width: "100px",  	template : " <label> #: util.bcs_formato(ValorMercadoGarantia,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoGarantia"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_GARANTIAS')			,width: "100px",  	template : " <label> #: util.bcs_formato(DiferenciaMercadoGarantia,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadSimulCom"					,title: $translate.instant('UI_SIMULTANEA_COMPRAS')						,width: "100px", 	hidden: true,	template : " <label> #: util.bcs_formato(CantidadSimulCom,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoSimulCom"				,title: $translate.instant('UI_PRECIO_MERCADO_SIMULTANEA_COMPRAS')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoSimulCom,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoSimulCom"				,title: $translate.instant('UI_MONTO_MERCADO_SIMULTANEA_COMPRAS')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoSimulCom,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoSimulCom"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_SIMULTANEA_COMPRAS')	,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoSimulCom,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadSimulVen"					,title: $translate.instant('UI_SIMULTANEA_VENTAS')						,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(CantidadSimulVen,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoSimulVen"				,title: $translate.instant('UI_PRECIO_MERCADO_SIMULTANEA_VENTAS')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoSimulVen,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoSimulVen"				,title: $translate.instant('UI_MONTO_MERCADO_SIMULTANEA_VENTAS')		,width: "100px", 	hidden: true,  	template : " <label> #: util.bcs_formato(ValorMercadoSimulVen,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoSimulVen"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_SIMULTANEA_VENTAS')	,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoSimulVen,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadPrestamo"					,title: $translate.instant('UI_PRESTATARIO')							,width: "100px", 	hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPrestamo,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoPrestamo"				,title: $translate.instant('UI_PRECIO_MERCADO_PRESTATARIO')				,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPrestamo,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoPrestamo"				,title: $translate.instant('UI_MONTO_MERCADO_PRESTATARIO')				,width: "100px", 	hidden: true,	template : " <label> #: util.bcs_formato(ValorMercadoPrestamo,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoPrestamo"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_PRESTATARIO')			,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPrestamo,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadEmpresti"					,title: $translate.instant('UI_EMPRESTITO')								,width: "100px", 	hidden: true,  	template : " <label> #:  util.bcs_formato(CantidadEmpresti,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoEmpresti"				,title: $translate.instant('UI_PRECIO_MERCADO_EMPRESTITO')				,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoEmpresti,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoEmpresti"				,title: $translate.instant('UI_MONTO_MERCADO_EMPRESTITO')				,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoEmpresti,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoEmpresti"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_EMPRESTITO')			,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoEmpresti,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadPactosV"					,title: $translate.instant('UI_VENTAS_CON_PACTO')						,width: "100px", 	hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPactosV,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoPactosV"				,title: $translate.instant('UI_PRECIO_MERCADO_VENTAS_CON_PACTO')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPactosV,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoPactosV"				,title: $translate.instant('UI_MONTO_MERCADO_VENTAS_CON_PACTO')			,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoPactosV,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoPactosV"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_VENTAS_CON_PACTO')	,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPactosV,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadPactosC"					,title: $translate.instant('UI_COMPRAS_CON_PACTO')						,width: "100px", 	hidden: true, 	template : " <label> #:  util.bcs_formato(CantidadPactosC,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoPactosC"				,title: $translate.instant('UI_PRECIO_MERCADO_COMPRAS_CON_PACTO')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoPactosC,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoPactosC"				,title: $translate.instant('UI_MONTO_MERCADO_COMPRAS_CON_PACTO')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoPactosC,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoPactosC"			,title: $translate.instant('UI_DIFERENCIA_MERCADO_COMPRAS_CON_PACTO')	,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoPactosC,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadReporteCompra"				,title: $translate.instant('UI_REPORTE_COMPRA')							,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(CantidadReporteCompra,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoReporteCompra"		,title: $translate.instant('UI_PRECIO_MERCADO_REPORTE_COMPRA')			,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoReporteCompra,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoReporteCompra"			,title: $translate.instant('UI_MONTO_MERCADO_REPORTE_COMPRA')			,width: "100px", 	hidden: true,  	template : " <label> #: util.bcs_formato(ValorMercadoReporteCompra,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoReporteCompra"	,title: $translate.instant('UI_DIFERENCIA_MERCADO_REPORTE_COMPRA')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoReporteCompra,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadReporteVenta"				,title: $translate.instant('UI_REPORTE_VENTA')							,width: "100px", 	hidden: true,	template : " <label> #: util.bcs_formato(CantidadReporteVenta,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Cantidad','USD','','')).valor#"},
					{field: "PrecioMercadoReporteVenta"			,title: $translate.instant('UI_PRECIO_MERCADO_REPORTE_VENTA')			,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(PrecioMercadoReporteVenta,'GEN','Precio',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Precio','USD','','')).valor#"},
					{field: "ValorMercadoReporteVenta"			,title: $translate.instant('UI_MONTO_MERCADO_REPORTE_VENTA')			,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(ValorMercadoReporteVenta,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					{field: "DiferenciaMercadoReporteVenta"		,title: $translate.instant('UI_DIFERENCIA_MERCADO_REPORTE_VENTA')		,width: "100px", 	hidden: true, 	template : " <label> #: util.bcs_formato(DiferenciaMercadoReporteVenta,'GEN','Monto',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum||0,'GEN','Monto','USD','','')).valor#"},
					
					{field: "CantidadTransIng"					,title: $translate.instant('UI_INGRESO_TRANSITO')						,width: "100px", 	attributes:{style:"text-align:right;white-space:nowrap; background-color:grey"}, headerAttributes:{style:"text-align: left; background-color:grey"},  template : " <label> #: util.bcs_formato(CantidadTransIng,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','USD','','')).valor#"},
					{field: "CantidadTransEgr"					,title: $translate.instant('UI_EGRESOS_TRANSITO')						,width: "100px", 	attributes:{style:"text-align:right;white-space:nowrap; background-color:grey"}, headerAttributes:{style:"text-align: left; background-color:grey"},  template : " <label> #: util.bcs_formato(CantidadTransEgr,'GEN','Cantidad',(IdMonedaInstrReajuste||'CLP'),'','').valor #</label>",aggregates: ["sum"], groupFooterTemplate:"#=(util.bcs_formato(sum,'GEN','Cantidad','USD','','')).valor#"},
					{field: "SaldoDisponibleCLP"				,title: $translate.instant('UI_SALDO_CLP')							    ,width: "100px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(SaldoDisponibleCLP ,'GEN','Monto','CLP','','')).valor #</label>"  },
					{field: "SaldoDisponibleUSD"				,title: $translate.instant('UI_SALDO_USD')								,width: "100px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(SaldoDisponibleUSD ,'GEN','Monto','CLP','','')).valor #</label>"  },
					{field: "SaldoDisponibleEUR"				,title: $translate.instant('UI_SALDO_EUR')								,width: "100px", 	align: "rigth", sortable : true, editable: false, template : " <label> #:  (util.bcs_formato(SaldoDisponibleEUR ,'GEN','Monto','CLP','','')).valor #</label>"  },
					
				],	
				sortable		: true,
				pageable		: true,
				autoBind		: true,
				height			: 250,
			    selectable 		: "row",
			    userSettings	: true,
				navigatable		: true,
		};

		//******************************************************************************************************************************/
		//********************************************** Configuracion de ComboBox *****************************************************/
		//******************************************************************************************************************************/
		
	   	desAPV.oConfiguracionTipoInstrumento={
	   		tipo : "combobox-tipo-instrumento",
	   		configuracion : {},
	   			filtros : {
	   				IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
	   				IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
	   				NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
	   				NoPrestamos						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PRT" },
	   				NoEmprestitos					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "EMP" },
	   				NoSimultaneas					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
	   				NoSpot 							: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SPT" }
	   			},
	   			idActual : false,
	   			seleccionado : false,
	   			idDefecto : '',
	   			idPostFiltro : false
	   	};
	   	   	
	   	desAPV.oConfiguracionInstrumento={
	   		buscar          : "-1"    
	   	};	
	   	
//	   	$scope.filtroRazonRetiro1 = { "field"   : "Codigo","operator": "eq", "value" : "REI" };
//		$scope.filtroRazonRetiro2 = { "field"   : "Codigo","operator": "eq", "value" : "RES" };
//
//	   	desAPV.ConfiguracionCmbo.RazonRetiro = {
//	   		filtros:{
//		   		filters: [{
//					logic : "or",
//					filters: [$scope.filtroRazonRetiro1, $scope.filtroRazonRetiro2]
//				},]
//	   		}
//		};
	   	
	   	desAPV.ConfiguracionCmbo.SocioNegocio = {
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			},
		};
		
		desAPV.ConfiguracionCmbo.Portafolio = {
				filtros: {
					IdCliente : {"field" : "IdCliente", "operator" : "eq", "value" : 0},
					Proposito : {"field" : "Proposito", "operator" : "eq", "value" : "PROPOSITOAPV"}
				}	
		};
		
		desAPV.ConfiguracionCmbo.ProductoAgrupador =  {
			filtros: {
				IdPlanTipoInversion : {"field" : "IdPlanTipoInversion", "operator" : "eq", "value" : 0}
			}	
		};

		desAPV.ConfiguracionCmbo.filtrosCuentaInversion = {
			filtros: {
				IdPortafolio	: {"field"    : "IdPortafolio"   ,"operator": "eq", "value" : -1}	
			}	
		};
		
		desAPV.ConfiguracionCmbo.TipoVigencia = {
			defecto : 'SH', //bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaSoloHoy,
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
			}
		};
		desAPV.ConfiguracionCmbo.MonedaLiquidacionOrden = { 
			filtros : {
				IdTipoInstrumento : { "field"   : "IdTipoInstrumento","operator": "eq", "value"   : "" },
				IdMonedaInstrumento : { "field"   : "IdMoneda","operator": "eq", "value"   : "" }
			}
		};

		desAPV.ConfiguracionCmbo.MecanismoCaptacion = {
			filtros: {
				IdPortafolio : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" }
			}
		};
		
		desAPV.ConfiguracionCmbo.Boveda = {
			filtros : {
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" }
			}
		};

		desAPV.ConfiguracionCmbo.CondicionLiquidacion = { 
			filtros : {
				IdTipoOperacion : { "field"   : "IdTipoOperacion","operator": "eq", "value"   : "COMACN" },
				EstadoTipoOperacionYCondicionDeLiquidacion : { "field"   : "EstadoTipoOperacionYCondicionDeLiquidacion","operator": "eq", "value"   : "ACT" }
			}
		};
		desAPV.configuracionAutorizadoDarOrdenes = {
			filtros: {
				IdPortafolio : { "field"   : "IdPortafolio","operator": "eq", "value"   :  "1450366" },
				IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   :  "ACT" }
			},
		},
		//******************************************************************************************************************************/
		//***************************************** Consulta Custodia *********************************************/
		//******************************************************************************************************************************/
		desAPV.consultas.buscarMovimientos = function() {					
			var filter = [];
			desAPV.HabilitoAutorizadoDarOrdenes = false;

			bcsComunes.ventanaOverlay(idVentana,true);
							 
			if(desAPV.datosForm.SocioNegocioSeleccionado === false || desAPV.datosForm.SocioNegocioSeleccionado === undefined){
    			bcsComunes.msg("warning","",$translate.instant("UI_DEBE_SELECCIONAR_CLIENTE"));
    			bcsComunes.ventanaOverlay(idVentana,false);
				return;
			}
	
			if (desAPV.datosForm.SocioNegocioSeleccionado.IdCliente){
				filter.push({ field: "IdCliente", operator: "eq", value: Number(desAPV.datosForm.SocioNegocioSeleccionado.IdCliente) });
			}
				
			desAPV.OrdenesSeleccion = undefined;
			
			//CUSTODIA
			
			if (filter.length > 0){
				
				var IdClasificacionTipoInstrumento	= '';
				var IdTipoInstrumento				= '';
				
				if(desAPV.datosForm.oConfiguracionTipoInstrumento.seleccionado && desAPV.datosForm.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento != undefined && desAPV.datosForm.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento != 0){
					IdClasificacionTipoInstrumento = desAPV.datosForm.oConfiguracionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento;
					IdTipoInstrumento = desAPV.datosForm.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento;
				}
				
				
				var IdCliente 						= desAPV.datosForm.SocioNegocioSeleccionado != false ? desAPV.datosForm.SocioNegocioSeleccionado.IdCliente : 0;
				var IdInstrumento					= desAPV.datosForm.instrumentoDatos != undefined ?  desAPV.datosForm.instrumentoDatos.IdInstrumento : 0;
				var IdRegimen						= desAPV.datosForm.RegimenAgrupadorSeleccionado != undefined ? desAPV.datosForm.RegimenAgrupadorSeleccionado.IdRegimenTributario : 0;
				var IdProducto						= desAPV.datosForm.ProductoAgrupadorSeleccionado != undefined ? desAPV.datosForm.ProductoAgrupadorSeleccionado.IdProductoAgrupador : 0; 
				var IdPlan							= desAPV.datosForm.NombrePlanAhorroSeleccionado != undefined ? desAPV.datosForm.NombrePlanAhorroSeleccionado.IdPlanTipoInversion : 0;
				
				var filtersCustodia = { 
					p_IdEmpresa 						: $scope.SesionIdEmpresa,				
					p_IdUsuario         				: $scope.SesionIdUsuario,
					p_IdModulo							: 'APV',
					p_IdCliente							: IdCliente,
					p_IdPortafolio						: 0,
					p_IdClasificacionTipoInstrumento	: IdClasificacionTipoInstrumento,
					p_IdTipoInstrumento					: IdTipoInstrumento,
					p_IdInstrumento						: IdInstrumento,
					p_IdBoveda							: '',
					p_AgrupaPorBoveda					: 'S',
					p_AgrupaPorRelacion					: 'N',
					p_FechaConsulta						: '0001-01-01',
					p_TipoFechaConsulta					: 'LIQ',
					p_IdRegimen							: IdRegimen, 
					p_IdProducto						: IdProducto,
					p_IdPlan							: IdPlan,
					p_IdTraspaso                        : parametrosVentana.IdTraspaso || 0
				};
				
					if(parametrosVentana.MontoTotalUF > 0){
				 		var data = Object();

					 	data.p_IdEmpresa =  $scope.SesionIdEmpresa;
						data.p_IdTrabajo =  9999;
						data.p_CalculosEntrada = [{
							"IdTrabajo"			: data.p_IdTrabajo
							,"IdInterno"		: 1
							,"Fecha"			: util.dateSPdate(desAPV.datosForm.fechaInicial)
							,"IdMonedaOrigen" 	: 'UF'
							,"IdMonedaDestino" 	: 'CLP'
							,"ValorCambioLocalModificado" : 0
							,"MontoOrigen"		: parametrosVentana.MontoTotalUF
							,"IdUso"			: 'ORDENES'
							//,"TipoCambio" 		: 0
							//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
							//,"OrigenTipoCambio" : 'OBSERVADO'
						}];
			
					bcsServiciosAPV.ObtenerParidadTipoCambio(data).$promise.then(function(value){
                            if (value.p_out_CalculosSalida.length > 0){
                                montoPesos = value.p_out_CalculosSalida[0].MontoLocal;                                        
                            }
                            else {
                                montoPesos = 0;
                            }
                        }, function(error){
                            	bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
									conflictos:util.parseServerResponse(error)
								},$scope.winID);		
								bcsComunes.ventanaOverlay(IdVentana, false);			
                        });
					 	
				 	}				
					
				bcsServiciosAPV.ConsultarCustodiaPortafolioAPV(filtersCustodia).$promise.then(function(respuesta){		
					if (respuesta.P_OUT_CUSTODIA.length > 0){						
						var Custodia = Array();							
						desAPV.mostrar = false;

						for(var i=0; i < respuesta.P_OUT_CUSTODIA.length ; i++){
							Custodia.push(respuesta.P_OUT_CUSTODIA[i]);	
							if(desAPV.mostrar === false){
								if(respuesta.P_OUT_CUSTODIA[i].IdClasificacionTipoInstrumento === 'RV'){
									desAPV.mostrar = true;
								}
							}
						}
					
						dsCustodia.data(Custodia);
						desAPV.kngTotalPatrimonio = respuesta.P_OUT_TOTALCUSTODIA;
						
				 		if(dsCustodia.data().length === 1)
						{
							desAPV.HabilitoAutorizadoDarOrdenes = true;
						}
				 		else if(dsCustodia.data().length > 1)
				 		{
				 			var count = 0;
				 			
							for(var x=1; x<dsCustodia.data().length; x++)
							{
								if(dsCustodia.view()[x].IdProductoAgrupador !== dsCustodia.view()[x-1].IdProductoAgrupador)
								{
									count = count + 1;
								}
							}
							
							if(count === 0)
							{
								desAPV.HabilitoAutorizadoDarOrdenes = true;
							}
				 		}
						
					}else {
						dsCustodia.removeAll();
						desAPV.kngTotalPatrimonio = 0;

					}
					
					if(desAPV.tipoEgreso === "TOT" && primeraVez === true){
				 		desAPV.datosForm.kngPorcentaje = 100;
				 	}else{
				 		desAPV.datosForm.kngPorcentaje = 0;
				 	}
					
					if(parametrosVentana.IdSocioNegocio !== undefined && primeraVez === true)
			    	{	
			    		desAPV.GlosaTraspasoRetiro				= parametrosVentana.Glosa;
					 	desAPV.datosForm.tipoEgresoIdActual		= parametrosVentana.CodigoTipoTraspaso.substring(0,3);
					 	desAPV.datosForm.RazonRetiroIdActual	= parametrosVentana.RazonRetiro;
					 	desAPV.tipoEgreso						= parametrosVentana.CodigoTipoTraspaso.substring(0,3);
					 	desAPV.razonRetiro						= parametrosVentana.RazonRetiro;
					 	desAPV.datosForm.fechaTransferencia		= parametrosVentana.FechaTraspaso;
					 	desAPV.datosForm.fechaEgreso			= parametrosVentana.FechaEgreso;
					 	desAPV.datosForm.EntidadDestinoIdActual = parametrosVentana.CodigoEntidadDestino;
					 	desAPV.datosForm.kngTotalDesinvertir 	= montoPesos != 0 ? montoPesos:parametrosVentana.MontoTotalPesos;
					 	desAPV.datosForm.kngPorcentaje = parametrosVentana.PorcentajeTotal;
					 		
					 	if(parametrosVentana.MontoTotalUF > 0){
					 		var data = Object();

						 	data.p_IdEmpresa =  $scope.SesionIdEmpresa;
							data.p_IdTrabajo =  9999;
							data.p_CalculosEntrada = [{
								"IdTrabajo"			: data.p_IdTrabajo
								,"IdInterno"		: 1
								,"Fecha"			: desAPV.datosForm.fechaInicial
								,"IdMonedaOrigen" 	: 'UF'
								,"IdMonedaDestino" 	: 'CLP'
								,"ValorCambioLocalModificado" : 0
								,"MontoOrigen"		: parametrosVentana.MontoTotalUF
								,"IdUso"			: 'ORDENES'
								//,"TipoCambio" 		: 0
								//,"MonedaCambio"		: $scope.data.IdMonedaMontoDisplay
								//,"OrigenTipoCambio" : 'OBSERVADO'
							}];

							bcsServiciosAPV.ObtenerParidadTipoCambio(data,function(value){
								if (value.p_out_CalculosSalida.length > 0){
								 	desAPV.datosForm.kngTotalDesinvertir = value.p_out_CalculosSalida[0].MontoLocal;										
								}
								else {
									desAPV.datosForm.kngTotalDesinvertir = 0;
								}
							});
						 	
					 	}
					 	
					 	desAPV.HabilitoPanelPrincipal = false;
			    	}
					
					primeraVez = false;
					desAPV.datosForm.kngTotalDesinvertir = montoPesos != 0 ? montoPesos:parametrosVentana.MontoTotalPesos;
				 	desAPV.datosForm.kngPorcentaje = parametrosVentana.PorcentajeTotal;		 				 	
          
					desAPV.PorcentajeFinal = 0;
					desAPV.datosForm.kngTotalRetiro = 0;
					dsCustodia.refresh;
		    		desAPV.operaciones = [];
					bcsComunes.ventanaOverlay(idVentana, false);
					desAPV.HabilitoPanelPrincipal = true;
					
					if (calcularDesinversionPrimeraVez) {
						desAPV.fn.calcularDesinversion();
						calcularDesinversionPrimeraVez = false;
						montoPesos = 0;
					}
				},
				function(respuesta){
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_PROBLEMAS_AL_RECUPERAR_SALDOS_CLIENTES"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);		
					bcsComunes.ventanaOverlay(idVentana, false);
				}
				);
			}
			bcsComunes.ventanaOverlay(idVentana, false);
		};
		
		     
	     //******************************************************************************************************************************/
		 //******************************************************** Funciones ***********************************************************/
		 //******************************************************************************************************************************/
	     desAPV.fn.seleccionTab = function(tipoTab){
	    	 if(tipoTab === 'RV'){
	    		 desAPV.TipoOrdenHabilito = 'RV';
	    	 }else{
	    		 desAPV.TipoOrdenHabilito = 'FM';
	    	 }
	     };	   
	     	    
	   desAPV.validaMontoPorcentaje = function(){
		   if(parseFloat(desAPV.datosForm.kngPorcentaje) > 100){
			   desAPV.datosForm.kngPorcentaje = 100;
	    		bcsComunes.msg("warning","",$translate.instant("UI_PORCENTAJE_MAXIMO_100"));
	    		return false;
		   }	
		   if(desAPV.datosForm.kngTotalDesinvertir > desAPV.kngTotalPatrimonio){
			   bcsComunes.msg("warning","",$translate.instant("UI_MONTO_INGRESADO_SUPERA_AL_SALDO"));
			   desAPV.datosForm.kngTotalDesinvertir = 0;
			   return false;
		   }
		   if(desAPV.kngTotalPatrimonio === 0){
	    		bcsComunes.msg("warning","",$translate.instant("UI_SALDO_INSUFICIENTE_DESINVERTIR"));
	    		return false;
	    	}
	    	else if(desAPV.datosForm.kngPorcentaje === 0 && desAPV.datosForm.kngTotalDesinvertir === 0){
	    		bcsComunes.msg("warning","",$translate.instant("UI_INGRESE_VALORES_A_CALCULAR"));
	    		return false;
	    	}else{
	    		return true;
	    	}
		 };
	     
	     desAPV.fn.calcularDesinversion = function(){	 	
	    	 var validaOk = desAPV.validaMontoPorcentaje();
	    	 if(validaOk === false)
	    		return;
	    	 	
	    	 var porcentaje = 0;
	    	 if(desAPV.datosForm.kngPorcentaje != 0)
	    	 	porcentaje = typeof(desAPV.datosForm.kngPorcentaje) === "string" ? desAPV.datosForm.kngPorcentaje.replace(",", ".") : desAPV.datosForm.kngPorcentaje;
	    	 	totalDesinvertir = Math.floor(desAPV.datosForm.kngTotalDesinvertir);
	    	 var tablaCalculoMontos = [];
	    	 var IdRegistro = 1;
	    	 $.each(dsCustodia.data(),function(key,value){
	    		 var Custodia = {
	    			IdRegistro						: IdRegistro,
	    			NombreProductoAgrupador 		: value.NombreProductoAgrupador,
					Regimen							: value.Regimen,
					IdClasificacionTipoInstrumento	: value.IdClasificacionTipoInstrumento,
					IdTipoInstrumento				: value.IdTipoInstrumento,
					Nemotecnico						: value.Nemotecnico,
					CantidadCustodia				: value.CantidadCustodia,
					PPPCustodia						: value.PPPCustodia,
					ValorPPPCustodia				: value.ValorPPPCustodia,
					MontoPPPCustodia				: value.MontoPPPCustodia,	
					CantidadADesinvertir			: value.CantidadADesinvertir,
					MontoADesinvertir				: value.MontoADesinvertir,
					IdPortafolio					: value.IdPortafolio,
					//PrecioMercadoCustodia			: value.PrecioMercadoCustodia,
					//MontoMercadoCustodia			: value.MontoMercadoCustodia,
					IdEmisor						: value.IdEmisor,
					IdInstrumento					: value.IdInstrumento,
					IdMonedaInstrReajuste			: value.IdMonedaInstrReajuste,
					
					/* --- NUEVAS COLUMNAS --- */
					DocumentoIdentidad				: value.DocumentoIdentidad,
					DocumentoIdentidadRutCavali		: value.DocumentoIdentidadRutCavali,
			        NombreCliente					: value.NombreCliente,
			        Relacion						: value.Relacion,
					ISIN							: value.ISIN,
					ClasificacionRiesgoInstrumento	: value.ClasificacionRiesgoInstrumento,
					EsFungible						: value.EsFungible,
					EsPagaderoEn					: value.EsPagaderoEn,
					IdBoveda						: value.IdBoveda,
					NumeroCuentaBoveda				: value.NumeroCuentaBoveda,
					TotalPosicion					: value.TotalPosicion,
					TotalPosicionPresunta			: value.TotalPosicionPresunta,
					Rentabilidad					: value.Rentabilidad,
					
					PrecioMercadoCustodia			: value.PrecioMercadoCustodia,
					ValorMercadoCustodia			: value.ValorMercadoCustodia,
					MontoMercadoCustodia			: value.MontoMercadoCustodia,
					DiferenciaMercadoCustodia		: value.DiferenciaMercadoCustodia,
					
					CantidadGarantia				: value.CantidadGarantia,
					PrecioMercadoGarantia			: value.PrecioMercadoGarantia,
					ValorMercadoGarantia			: value.ValorMercadoGarantia,
					DiferenciaMercadoGarantia		: value.DiferenciaMercadoGarantia,
					
					CantidadSimulCom				: value.CantidadSimulCom,
					PrecioMercadoSimulCom			: value.PrecioMercadoSimulCom,
					ValorMercadoSimulCom			: value.ValorMercadoSimulCom,
					DiferenciaMercadoSimulCom		: value.DiferenciaMercadoSimulCom,
					
					CantidadSimulVen				: value.CantidadSimulVen,
					PrecioMercadoSimulVen			: value.PrecioMercadoSimulVen,
					ValorMercadoSimulVen			: value.ValorMercadoSimulVen,
					DiferenciaMercadoSimulVen		: value.DiferenciaMercadoSimulVen,
					
					CantidadPrestamo				: value.CantidadPrestamo,
					PrecioMercadoPrestamo			: value.PrecioMercadoPrestamo,
					ValorMercadoPrestamo			: value.ValorMercadoPrestamo,
					DiferenciaMercadoPrestamo		: value.DiferenciaMercadoPrestamo,
					
					CantidadEmpresti				: value.CantidadEmpresti,
					PrecioMercadoEmpresti			: value.PrecioMercadoEmpresti,
					ValorMercadoEmpresti			: value.ValorMercadoEmpresti,
					DiferenciaMercadoEmpresti		: value.DiferenciaMercadoEmpresti,
					
					CantidadPactosV					: value.CantidadPactosV,
					PrecioMercadoPactosV			: value.PrecioMercadoPactosV,
					ValorMercadoPactosV				: value.ValorMercadoPactosV,
					DiferenciaMercadoPactosV		: value.DiferenciaMercadoPactosV,
					
					CantidadPactosC					: value.CantidadPactosC,
					PrecioMercadoPactosC			: value.PrecioMercadoPactosC,
					ValorMercadoPactosC				: value.ValorMercadoPactosC,
					DiferenciaMercadoPactosC		: value.DiferenciaMercadoPactosC,
					
					CantidadReporteCompra			: value.CantidadReporteCompra,
					PrecioMercadoReporteCompra		: value.PrecioMercadoReporteCompra,
					ValorMercadoReporteCompra		: value.ValorMercadoReporteCompra,
					DiferenciaMercadoReporteCompra	: value.DiferenciaMercadoReporteCompra,
					
					CantidadReporteVenta			: value.CantidadReporteVenta,
					PrecioMercadoReporteVenta		: value.PrecioMercadoReporteVenta,
					ValorMercadoReporteVenta		: value.ValorMercadoReporteVenta,
					DiferenciaMercadoReporteVenta	: value.DiferenciaMercadoReporteVenta,
					
					CantidadTransIng				: value.CantidadTransIng,
					CantidadTransEgr				: value.CantidadTransEgr,
					/*Datos necesario para Obtener Precio Mercado*/
					IdInstrumentoCustodia			: value.IdInstrumentoCustodia,
	    			IdMonedaInstrumento				: value.IdMonedaInstrReajuste,	
	    			FechaOperacion					: desAPV.datosForm.fechaInicial,
	    			SaldoDisponibleCLP				: value.SaldoDisponibleCLP,
	    			SaldoDisponibleUSD				: value.SaldoDisponibleUSD,
	    			SaldoDisponibleEUR				: value.SaldoDisponibleEUR,
	    		 };
	    		 IdRegistro = IdRegistro + 1;
	    		 tablaCalculoMontos.push(Custodia);
	    	 });
	    	 	
	    	 var parametros = {		
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : "APV",
				p_TotalADesinvertir: desAPV.datosForm.kngTotalDesinvertir === null ? 0 : totalDesinvertir,
				p_PorcentajeADesinvertir: porcentaje === null ? 0 : porcentaje,
				p_CantidadADesinvertir: desAPV.datosForm.kngCantidadDesinvertir,  ///Individual Manual o Por Servicio
				//p_Redondeo: 'HALF_UP', // ROUND = Sin Redondeo, UP = Arriba, DOWN = Abajo.
				p_IdTraspaso: (calcularDesinversionPrimeraVez && parametrosVentana.IdTraspaso) ? parametrosVentana.IdTraspaso : 0,
				p_Custodia : tablaCalculoMontos
			};
	    	 
	    	bcsServiciosAPV.CalculoMontosDesinversion(parametros).$promise.then(function(respuesta){
				console.log(respuesta);
	    		 
				if (respuesta.P_OUT_ERRORESCONTADOR == 0){
					 if(respuesta.P_OUT_MENSAJES[0].codigo == 'OK'){
						 if(respuesta.P_OUT_CUSTODIA.length > 0){
							 dsCustodia.data(respuesta.P_OUT_CUSTODIA);
							 desAPV.datosForm.kngTotalRetiro = respuesta.P_OUT_MONTO_TOTAL;
							 //desAPV.datosForm.kngTotalDesinvertir = 0;
							 //desAPV.datosForm.kngPorcentaje = 0;
							 desAPV.PorcentajeFinal = respuesta.P_OUT_PORCENTAJE;
						 }
					 }else{
						 bcsComunes.ventanaMsgErr(respuesta.P_OUT_MENSAJES[0].descripcion,idVentana);
					 }
				 }else{
					 bcsComunes.ventanaMsgErr(respuesta.P_OUT_MENSAJES[0].descripcion,idVentana);
				 }
	   		 },function(respuesta){
	   			 
	   		 });		 
	     };
	     
	     //LIMPIAR DATOS GENERALES Y RENTA VARIABLE.
	     desAPV.fn.limpiarFormulario = function(){
	    	 desAPV.datosForm.fechaTransferencia = undefined;
	    	 desAPV.datosForm.fechaEgreso = undefined;
    		 desAPV.datosForm.tipoEgresoSeleccionado  = undefined;
    		 desAPV.datosForm.tipoEgresoIdActual  = undefined;
    		 desAPV.datosForm.RazonRetiroSeleccionado  = undefined;
    		 desAPV.datosForm.RazonRetiroIdActual = undefined;
    		 desAPV.datosForm.AutorizadoSeleccionado = undefined;
    		 desAPV.datosForm.AutorizadoIdActual = undefined;
    		 desAPV.datosForm.EntidadDestinoSeleccionado = undefined;
    		 desAPV.datosForm.EntidadDestinoIdActual = undefined;
    		 desAPV.GlosaTraspasoRetiro = undefined;
    		 
    		 desAPV.datosForm.mecanismoDeCaptacionSeleccionado = undefined;
    		 desAPV.ConfiguracionCmbo.MecanismoCaptacion.idActual = undefined;
    		 desAPV.vigenciaActual = "SH";
    		 desAPV.datosForm.idBolsaSeleccionada = "BCS";
    		 desAPV.datosForm.idTipoCanalDistribucion = "MAN";
    		 desAPV.ConfiguracionCmbo.Boveda.idActual = "DCV";
    		 montoPesos=0;
    		 desAPV.HabilitoAutorizadoDarOrdenes = false;
	     };
	     
	     desAPV.fn.limpiarFiltros = function(socio)
	     {
	    	 if(socio)
	    	 {
	    		 desAPV.datosForm.SocioNegocioSeleccionado					= undefined;
		    	 desAPV.datosForm.SocioNegocioidActual						= false;
	    	 }
	    	 desAPV.datosForm.oConfiguracionTipoInstrumento.seleccionado	= undefined;
	    	 desAPV.datosForm.oConfiguracionTipoInstrumento.idActual		= undefined;
	    	 desAPV.datosForm.PlanAhorroIdActual							= undefined;
			 desAPV.datosForm.NombrePlanAhorroSeleccionado					= undefined;
			 desAPV.datosForm.ProductoAgrupadorSeleccionado					= undefined;
			 desAPV.datosForm.ProductoAgrupadorIdActual						= undefined;
			 desAPV.datosForm.RegimenAgrupadorSeleccionado					= undefined;
			 desAPV.datosForm.RegimenAgrupadorIdActual						= undefined;
			 desAPV.datosForm.instrumentoDatos								= {};
			 desAPV.oConfiguracionInstrumento.buscar						= "-1";
			 
			 desAPV.HabilitoPanelPrincipal = true;
			 
			 desAPV.fn.limpiarSegundoPanel();
	     };
	     
	     desAPV.fn.limpiarSegundoPanel = function()
	     {
			 desAPV.kngTotalPatrimonio				= 0;
			 desAPV.datosForm.kngTotalDesinvertir	= 0;
			 desAPV.datosForm.kngPorcentaje			= 0;
			 desAPV.datosForm.kngTotalRetiro		= 0;
			 desAPV.PorcentajeFinal					= 0;
			 
	    	 dsCustodia.removeAll();
	    	 dsCustodia.refresh;
	    	 
	    	 desAPV.mostrar = false;
			 desAPV.OrdenesSeleccion = undefined;
	     }
	     
		desAPV.fn.quitarOrden = function(orden)
		{
			if(orden != undefined)
			{
				dsCustodia.remove(orden);
				
				
		 		if(dsCustodia.data().length === 1)
				{
					desAPV.HabilitoAutorizadoDarOrdenes = true;
					
					if(desAPV.mostrar)
					{
						if(dsCustodia.view()[0].IdProductoAgrupador === 1)
						{
							desAPV.mostrar = false;
						}
					}
				}
		 		else if(dsCustodia.data().length > 1)
		 		{
		 			var count = 0;
		 			
					for(var x=1; x<dsCustodia.data().length; x++)
					{
						if(dsCustodia.view()[x].IdProductoAgrupador !== dsCustodia.view()[x-1].IdProductoAgrupador)
						{
							count = count + 1;
						}
					}
					
					if(count === 0)
					{
						desAPV.HabilitoAutorizadoDarOrdenes = true;
						
						if(desAPV.mostrar)
						{
							if(dsCustodia.view()[0].IdProductoAgrupador === 1)
							{
								desAPV.mostrar = false;
							}
						}
					}
		 		}
				
				desAPV.OrdenesSeleccion = undefined;
				bcsComunes.msg("success","",$translate.instant("UI_SE_QUITA_ORDEN"));
			}
			else
			{
				bcsComunes.msg("warning","",$translate.instant("UI_SELECCIONE_ORDEN"));
			}
		};

	     desAPV.fn.HabilitaEntidad = function(){
	    	 if(desAPV.datosForm.tipoEgresoSeleccionado.Codigo === 'JUB')
	    		 return true;
	    	 else
	    		 return false;
	     };
	     desAPV.fn.filtroRazonRetiro = function(socioExiste){
	    	 if(socioExiste === true){
	    		 desAPV.ConfiguracionCmbo.RazonRetiro = { 
	    				filtros : {
			    	    		Codigo1 : { "field"   : "Codigo","operator": "neq", "value"   : "RES" },
			    	    		Codigo2 : { "field"   : "Codigo","operator": "neq", "value"   : "REI" },
			    	    } 
		    	    };
		    	}else{
		    		desAPV.ConfiguracionCmbo.RazonRetiro = { 
		    			filtros : {
			    	    	Codigo1 : { "field"   : "Codigo","operator": "neq", "value"   : "JUB" },
			    	    	Codigo2 : { "field"   : "Codigo","operator": "neq", "value"   : "HER" },
			    	    	Codigo3 : { "field"   : "Codigo","operator": "neq", "value"   : "TRA" },
			    	    }
		    	    };
		    	}	
		    };
		 
		 desAPV.fn.restaurar = function(){
			calcularDesinversionPrimeraVez = true;
			desAPV.consultas.buscarMovimientos();
		 };
		 	
	     desAPV.fn.ActualizarCobros = function(fila, cobros, boton){
	    	 
	    	 if(fila != undefined){
	    		 
	    		 desAPV.Fila = fila;
	    		 desAPV.TipoInstrumento = fila.IdClasificacionTipoInstrumento;
	    			 desAPV.operaciones = [{
						IdInstrumento  				: fila.IdInstrumento,
						FechaOperacion 				: $scope.SesionFechaSistema,
						CantidadOrden   			: fila.CantidadCustodia,
						PrecioOrden   				: fila.PPPCustodia,
						MontoTotal					: fila.MontoPPPCustodia,
						IdMonedaLiquidacionOrden	: 'CLP',
						IdPortafolio 				: fila.IdPortafolio,
						IdTipoProducto 				: 'INAC',
						IdTipoOperacion 			: 'VENACN',
						IdCanalIngreso 				: $scope.SesionTipoCanalIngreso,
						IdBolsa 					: desAPV.datosForm.bolsaSeleccionada.IdBolsa,
						IdCorredorCorresponsal		: "",
						}];
	    					
	    			 var datos = {	
	    						p_IdEmpresa: $scope.SesionIdEmpresa,
	    						p_IdUsuario: $scope.SesionIdUsuario,
	    						p_IdModulo: 'ORD',
	    											
	    						p_Ordenes : [],
	    						p_CobrosModificados : []
	    					};
	    			 
	    			 var orden = {
	 						CantidadOrden   				: fila.CantidadCustodia,
	 						DiasPlazo 						: 0,
	 						EsFungible  					: 'N',
	 						IdBolsa							: desAPV.datosForm.bolsaSeleccionada.IdBolsa,
	 						IdCorredorCorresponsal			: null,
	 						IdInterno  						: 1,
	 						IdInstrumento  					: fila.IdInstrumento,
	 						IdMonedaLiquidacionOrden        : 'CLP',
	 						IdPortafolio 					: fila.IdPortafolio,
	 						IdTipoProducto  				: 'INAC',
	 						IdTipoOperacion					: 'VENACN',
	 						IdTipoCanalIngreso				: $scope.SesionTipoCanalIngreso,
	 						FechaOperacion					: $scope.SesionFechaSistema,
	 						MontoOrden						: fila.MontoPPPCustodia,	//para compra simultanea
	 						PrecioOrden 					: fila.PPPCustodia,
	 						TasaSimultanea  				: 0,
	 						TasaRFIF  						: 0,
	 						TasaVentaCorta  				: 0,
	 						TipoCambio 						: null,
	 						ValorRescateOrden				: 0,
	 						
	 						MontoTotal						: fila.MontoPPPCustodia,
	 						EsPorMonto 						: fila.MontoPPPCustodia > 0 && !fila.CantidadCustodia? 'S' : 'N',
	 						TasaReporte						: 0,
	 						TasaReportado					: 0,
	 						PlazoReporte					: 0,
	 						BaseTasa						: 0,
	 						TipoInteres						: null,
	 						
	 						PorcentajeAllIn					: 0,
	 						MontoAllIn						: 0
	 					};
	    			 
	    			 datos.p_Ordenes.push(orden);
	    			 
	    			 var p = bcsServicioOrden.CalcularMontoYCobrosOrden(datos);
	 				
	     			p.$promise.then(function(result) {
	     				setTimeout(function(){ 
	     				if(fila.IdClasificacionTipoInstrumento === "RV"){
	     				if(result.p_out_Cobros.length > 0){
	     					if(cobros !== undefined){
	     						if(typeof cobros !== "undefined"){
		     						var agregar = true;
		     						if(cobrosCustodias.length > 0){
		     							if(desAPV.TipoInstrumentoAnterior === "RV"){
		     								$.each(cobros, function(key, value){
		     									for (i = 0; i < cobrosCustodias.length; i++) { 
		   			    							if(cobrosCustodias[i].Producto === desAPV.FilaAnterior.NombreProductoAgrupador && cobrosCustodias[i].Regimen === desAPV.FilaAnterior.Regimen && cobrosCustodias[i].Instrumento === desAPV.FilaAnterior.Nemotecnico && key === cobrosCustodias[i].IdCobro){
		   			    								cobrosCustodias[i].ValorCobroAplicado = value;
		   			    								agregar = false;
		   			    							}
		     									}
	   			 							});
		     								
			   			    				if(agregar === true){
			   			    					$.each(desAPV.cobrosModificados, function(key, value){
			   			    						cobrosCustodias.push({
				   			 							Producto			: desAPV.FilaAnterior.NombreProductoAgrupador,
				   			 							Regimen 			: desAPV.FilaAnterior.Regimen,
				   			 							Instrumento 		: desAPV.FilaAnterior.Nemotecnico,
				   			 							IdCobro 			: key,
				   			 							ValorCobroAplicado 	: value,  
				   			 						}); 
				   			 					});
			   			    				}
		     							}
		     							
		     								
		     								var asignado = false;
			     							$.each(desAPV.cobrosModificados, function(key, value){
		     									for (i = 0; i < cobrosCustodias.length; i++) { 
		   			    							if(cobrosCustodias[i].Producto === desAPV.Fila.NombreProductoAgrupador && cobrosCustodias[i].Regimen === desAPV.Fila.Regimen && cobrosCustodias[i].Instrumento === desAPV.Fila.Nemotecnico && key === cobrosCustodias[i].IdCobro){
		   			    								desAPV.cobrosModificados[key] = cobrosCustodias[i].ValorCobroAplicado;
		   			    								asignado = true;
		   			    							}
		     									}
	   			 							});
		     								
			   			    				if(asignado === false){
			   			    					$.each(desAPV.cobrosModificados, function(key, value){
			     									$.each(result.p_out_Cobros, function(llave, valor ){
			     										if(key === valor.IdCobro ){
						   			    					desAPV.cobrosModificados[key] = valor.ValorCobroAplicado;
					   			    					}
			     									});
			     								});
			   			    				}
		     								
		     						}else{
		     							if(desAPV.TipoInstrumentoAnterior === "RV"){
				    						$.each(cobros, function(key, value){
				    							cobrosCustodias.push({
				    								Producto			: desAPV.FilaAnterior.NombreProductoAgrupador,
				    								Regimen 			: desAPV.FilaAnterior.Regimen,
				    								Instrumento 		: desAPV.FilaAnterior.Nemotecnico,
				    								IdCobro 			: key,
				    								ValorCobroAplicado 	: value,  
				    							}); 
				    						});
		     							}			     							
		     							if(desAPV.FilaAnterior !== undefined && desAPV.FilaAnterior !== desAPV.Fila){
		     								$.each(desAPV.cobrosModificados, function(key, value){
		     									$.each(result.p_out_Cobros, function(llave, valor ){
		     										if(key === valor.IdCobro ){
					   			    					desAPV.cobrosModificados[key] = valor.ValorCobroAplicado;
				   			    					}
		     									});
		     								});
		     							}
		     						}
	     						}else{
	     							var asignado = false;
	     							$.each(desAPV.cobrosModificados, function(key, value){
     									for (i = 0; i < cobrosCustodias.length; i++) { 
   			    							if(cobrosCustodias[i].Producto === desAPV.FilaAnterior.NombreProductoAgrupador && cobrosCustodias[i].Regimen === desAPV.FilaAnterior.Regimen && cobrosCustodias[i].Instrumento === desAPV.FilaAnterior.Nemotecnico && key === cobrosCustodias[i].IdCobro){
   			    								desAPV.cobrosModificados[key] = cobrosCustodias[i].ValorCobroAplicado;
   			    								asignado = true;
   			    							}
     									}
			 						});
     								
	   			    				if(asignado === false){
	   			    					$.each(desAPV.cobrosModificados, function(key, value){
	     									$.each(result.p_out_Cobros, function(llave, valor ){
	     										if(key === valor.IdCobro ){
				   			    					desAPV.cobrosModificados[key] = valor.ValorCobroAplicado;
			   			    					}
	     									});
	     								});
	   			    				}
		     					}	 
	     					}else{
	     						var asignado = false;
     							$.each(desAPV.cobrosModificados, function(key, value){
 									for (i = 0; i < cobrosCustodias.length; i++) { 
			    							if(cobrosCustodias[i].Producto === desAPV.FilaAnterior.NombreProductoAgrupador && cobrosCustodias[i].Regimen === desAPV.FilaAnterior.Regimen && cobrosCustodias[i].Instrumento === desAPV.FilaAnterior.Nemotecnico && key === cobrosCustodias[i].IdCobro){
			    								desAPV.cobrosModificados[key] = cobrosCustodias[i].ValorCobroAplicado;
			    								asignado = true;
			    							}
 									}
		 						});
 								
   			    				if(asignado === false){
   			    					$.each(desAPV.cobrosModificados, function(key, value){
     									$.each(result.p_out_Cobros, function(llave, valor ){
     										if(key === valor.IdCobro ){
			   			    					desAPV.cobrosModificados[key] = valor.ValorCobroAplicado;
		   			    					}
     									});
     								});
   			    				}
	     					}	 
	     				}
	     				desAPV.FilaAnterior = desAPV.Fila;
	     				desAPV.TipoInstrumentoAnterior = desAPV.TipoInstrumento;
	     				 //$("#cobrosGrilla").css('pointer-events', 'auto');
	     				if(boton === true){
	     					desAPV.fn.enviarDesinversion();
	     				}else{
	     					bcsComunes.ventanaOverlay(idVentana,false);
	     				}
	     			}else{	
	     				var agregar = true;
			    		if(cobrosCustodias.length > 0){
			    			if(desAPV.TipoInstrumentoAnterior === "RV"){
				    			$.each(cobros, function(key, value){
									for (i = 0; i < cobrosCustodias.length; i++) { 
			    						if(cobrosCustodias[i].Producto === desAPV.FilaAnterior.NombreProductoAgrupador && cobrosCustodias[i].Regimen === desAPV.FilaAnterior.Regimen && cobrosCustodias[i].Instrumento === desAPV.FilaAnterior.Nemotecnico && key === cobrosCustodias[i].IdCobro){
			    							cobrosCustodias[i].ValorCobroAplicado = value;
			    							agregar = false;
			    						}
									}
								});
								
				    			if(agregar === true){
				    				$.each(desAPV.cobrosModificados, function(key, value){
				    					cobrosCustodias.push({
					 						Producto			: desAPV.FilaAnterior.NombreProductoAgrupador,
					 						Regimen 			: desAPV.FilaAnterior.Regimen,
					 						Instrumento 		: desAPV.FilaAnterior.Nemotecnico,
					 						IdCobro 			: key,
					 						ValorCobroAplicado 	: value,  
					 					}); 
					 				});
				    			}
			    			}
			    			$.each(desAPV.cobrosModificados, function(key, value){
								$.each(result.p_out_Cobros, function(llave, valor ){
									if(key === valor.IdCobro ){
	   			    					desAPV.cobrosModificados[key] = valor.ValorCobroAplicado;
   			    					}
								});
							});
			    		}else{	
			    			if(desAPV.TipoInstrumentoAnterior === "RV"){
				    			if(desAPV.FilaAnterior === undefined){
				    				$.each(cobros, function(key, value){
										cobrosCustodias.push({
											Producto			: desAPV.Fila.NombreProductoAgrupador,
											Regimen 			: desAPV.Fila.Regimen,
											Instrumento 		: desAPV.Fila.Nemotecnico,
											IdCobro 			: key,
											ValorCobroAplicado 	: value,  
										}); 
									});
				    			}else{
				    				$.each(cobros, function(key, value){
										cobrosCustodias.push({
											Producto			: desAPV.FilaAnterior.NombreProductoAgrupador,
											Regimen 			: desAPV.FilaAnterior.Regimen,
											Instrumento 		: desAPV.FilaAnterior.Nemotecnico,
											IdCobro 			: key,
											ValorCobroAplicado 	: value,  
										}); 
									});
				    			} 
			    			}
			    			$.each(desAPV.cobrosModificados, function(key, value){
								$.each(result.p_out_Cobros, function(llave, valor ){
									if(key === valor.IdCobro ){
	   			    					desAPV.cobrosModificados[key] = valor.ValorCobroAplicado;
   			    					}
								});
							});
			    		}
			    		 desAPV.FilaAnterior = desAPV.Fila;
			    		 desAPV.TipoInstrumentoAnterior = desAPV.TipoInstrumento;
			    		 $("#cobrosGrilla").css('pointer-events', 'none');
			    		 
			    		 if(boton === true){
		     				desAPV.fn.enviarDesinversion();
			    		 }else{
			    			 bcsComunes.ventanaOverlay(idVentana,false);
			    		 }
			    	}
	     				}, 200);
	     			});	 
	    	}else{
	    		if(boton === true){
 					desAPV.fn.enviarDesinversion();
 				}else{
					if(dsCustodia.data().length === 0)
 					desAPV.consultas.buscarMovimientos();
 					bcsComunes.ventanaOverlay(idVentana,false);
 				}
	    	}
	    };

			//******************************************************************************************************************************/
			//***************************************************** Ingreso Orden FFMM *****************************************************/
			//******************************************************************************************************************************/		
			desAPV.aporteNormal.ingresarAporteRescate = function(SolicitudAporteRescate) {
				//SolicitudAporteRescate
	   			bcsComunes.ventanaOverlay(idVentana,true);
	   			var ventanaActual = idVentana;
				var datosIngresoAporteRescate = {
						 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
						 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo : 'APV',
						 p_SimularOperacion : 'N',
						 p_UsaReglasNegocio : 'S',
						 p_SolicitudesAporteRescate : SolicitudAporteRescate
				};
				bcsServiciosAPV.CrearSolicitudAporteRescateNormal(datosIngresoAporteRescate).$promise.then(function(respuesta){
			     	 
			     	 if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0){
			     		IdSolicitudAporteRescate = respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate;
			     		
			     		var datosCambiarEstadoTraspaso = { 
								p_IdEmpresa 	: $scope.SesionIdEmpresa,				
								p_IdUsuario     : $scope.SesionIdUsuario,
								p_IdModulo		: 'APV',
								p_Parametros	: [{
													IdEstado : 'PRO',
													IdEmpresa: $scope.SesionIdEmpresa,
													IdTraspaso : parametrosVentana.IdTraspaso || 0,
													IdContratoSAAPV : 0
													}]
							};	
			     		
			     		bcsServiciosAPV.CambiarEstadoTraspaso(datosCambiarEstadoTraspaso).$promise.then(function(respuesta){
			     			console.log('respuesta',respuesta)
							if (respuesta.P_OUT_ERRORESCONTADOR > 0){
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);		
							}
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);		
							bcsComunes.ventanaOverlay(IdVentana, false);
						});		
			     			
			     		/*
			     		var id = bcsComunes.ventanaMsg({mensaje:"Operacion ingresada: " + respuesta.P_OUT_RESULTADO[0].IdSolicitudAporteRescate,conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			     		if(id){
			     			var promesa=bcsComunes.ventanaPromesa(id); 
				     		$.when(promesa).then(function(data){  
				     			//$scope.solicitarDeclaracion(respuesta);
				            });
			     		}*/
//			     		var Orden = Array();
//			     		Orden = respuesta.P_OUT_RESULTADO; 
//			     		$.each(Orden,function(key,value){
//			     			var resultado = {
//				     				Id : value.IdSolicitudAporteRescate,
//									tipo : 'INFORMACION',
//									codigo : '',
//									descripcion : "Operacion FFMM ingresada: " + value.IdSolicitudAporteRescate
//				     		}; 
//				     		mensajeSalida.push(resultado);
//			     		});
			     		
			     		var Orden = Array();
			     		Orden = respuesta.P_OUT_RESULTADO; 
			     		
						//CAMBIO VENTANA RESULTADO

			     		$.each(Orden,function(key,value){
				     		var objCarga = {
									IdInterno : 2,
									tipo : 'INFORMACION',
									codigo : '',
									descripcion : "Operacion FFMM ingresada : " + value.IdSolicitudAporteRescate
							};
				     		mensajeResultadoOrdenes.push(objCarga);
			     		});

			     		if(parametrosVentana.IdTraspaso === undefined){
			     			if(dsCustodia.data().length > 0){
			     				var detalle = [];
			     				$.each(dsCustodia.data(),function(key,value){
			     					var regimen = value.Regimen.split("-");
			     					var CodTipoAhorro = regimen[0];
			     					
				     				detalle.push({
				     					IdDetalleOrigenTraspaso : 0,
				     					IdTraspaso : 0,
				     					//Terminar de Asignar.
				     					CodigoTipoAhorro : CodTipoAhorro,
				     					IdEntidadRecaudadora : desAPV.datosForm.EntidadDestinoSeleccionado.IdSocioDeNegocio,
				     					NombreEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.NombreCompleto,
				     					DocumentoEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.DocumentoIdentidadDefecto,
				     					Fondo: '',//Definir.
				     					MontoTraspaso: value.MontoADesinvertir,
				     					UFTraspaso: 0,
				     					PorcentajeTraspaso: desAPV.PorcentajeFinal,
				     					IdEstado: 'VIG'
									});
				     			});

				     			/*Inicio - Creacion Traspaso y Detalle Traspaso.*/
								var Traspaso = {
									p_IdEmpresa : $scope.SesionIdEmpresa,
									p_IdUsuario : $scope.SesionIdUsuario,
									p_IdModulo  : "APV",
									p_Traspaso : {
										IdTraspaso : 0,
										IdContratoSAAPV : 0,
										IdEmpresa : $scope.SesionIdEmpresa,
										TipoMovimiento : 'EGRESO',
										RazonRetiro: desAPV.datosForm.RazonRetiroIdActual,
										FechaIngreso: "0001-01-01",
										FechaEgreso: "0001-01-01",
										FechaModificacion: "0001-01-01",
										CodigoTipoTraspaso: desAPV.datosForm.tipoEgresoIdActual,
										Descripcion: '',
										IdCliente: desAPV.datosForm.SocioNegocioSeleccionado.IdCliente,
										IdPersona: desAPV.datosForm.EntidadDestinoSeleccionado.IdPersona,
										CodigoTipoAhorro: '',	
										NombreEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.NombreCompleto,
										DocumentoEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.DocumentoIdentidadDefecto,
										IdEstado: "PDI",
									},
									p_DetalleOrigenTraspaso : detalle,
								}
								
								bcsServiciosAPV.CrearTraspaso(Traspaso).$promise.then(function(respuesta){
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){
										
										IdTraspaso = respuesta.P_OUT_IDTRASPASO;
										var OrdenTraspaso = Array();
										$.each(Orden,function(key,value){
							     			var obj = {
					     						IdOrdenTraspaso : 0,
												IdTraspaso : IdTraspaso || 0,
												IdOrden : 0,
												IdSolicitudAporteRescate : value.IdSolicitudAporteRescate
								     		}; 
							     			OrdenTraspaso.push(obj);
							     		});
										
										var resultado = {
							     				IdInterno : 2,
												tipo : 'INFORMACION',
												codigo : '',
												descripcion : "Traspaso Egreso ingresado : " + IdTraspaso
							     		}; 
										mensajeResultadoOrdenes.push(resultado);
							     		
							     		
										/*Inicio - Guardo Relacion entre Orden y Traspaso*/
										var OrdenTraspaso = {
											p_IdEmpresa : $scope.SesionIdEmpresa,
											p_IdUsuario : $scope.SesionIdUsuario,
											p_IdModulo  : "APV",
											p_OrdenTraspaso : OrdenTraspaso
										};
										bcsServiciosAPV.CrearOrdenTraspaso(OrdenTraspaso).$promise.then(function(respuesta){
//											if (respuesta.P_OUT_ERRORESCONTADOR === 0){
//												respuesta.P_OUT_MENSAJES = mensajeSalida;
//												bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
//												bcsComunes.ventanaOverlay(idVentana,false);
//											}else{
//												bcsComunes.ventanaMsg({
//													mensaje:$translate.instant("UI_ERROR_INGRESO_ORDEN_TRASPASO"),
//													conflictos:util.parseServerResponse(respuesta)
//												},idVentana);
//												bcsComunes.ventanaOverlay(idVentana,false);
//											}
																						
											//CAMBIO VENTANA RESULTADO
											respuesta.P_OUT_RESULTADO = mensajeResultadoOrdenes;
											if(respuesta.P_OUT_RESULTADO.length > 0){
												var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false, $scope.winID);		
												var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
												promesa.then(function(data){
													bcsComunes.ventanaOverlay(idVentana,false);
													bcsComunes.ventanaCerrar($scope.IdVentana);
												});	
											}
											
											bcsComunes.ventanaOverlay($scope.winID,false);
										});
										/*Fin - Guardo Relacion entre Orden y Traspaso*/
						     			bcsComunes.ventanaOverlay($scope.winID,false);
						     			
									}else{
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("UI_ERROR_INGRESO_TRASPASO"),
											conflictos:util.parseServerResponse(respuesta)
										},idVentana);
										bcsComunes.ventanaOverlay(idVentana,false);
									}
								});
			     			};			     			
			     		}else{
			     			var OrdenTraspaso = Array();
							$.each(Orden,function(key,value){
				     			var obj = {
		     						IdOrdenTraspaso : 0,
									IdTraspaso : parametrosVentana.IdTraspaso || 0,
									IdOrden : 0,
									IdSolicitudAporteRescate : value.IdSolicitudAporteRescate
					     		}; 
				     			OrdenTraspaso.push(obj);
				     		});
							
							/*Inicio - Guardo Relacion entre Orden y Traspaso*/
							var OrdenTraspaso = {
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_IdUsuario : $scope.SesionIdUsuario,
								p_IdModulo  : "APV",
								p_OrdenTraspaso : OrdenTraspaso
							};

							bcsServiciosAPV.CrearOrdenTraspaso(OrdenTraspaso).$promise.then(function(respuesta){
//								if (respuesta.P_OUT_ERRORESCONTADOR === 0){
//									respuesta.P_OUT_MENSAJES = mensajeSalida;
//									bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
//									bcsComunes.ventanaOverlay(idVentana,false);
//								}else{
//									bcsComunes.ventanaMsg({
//										mensaje:$translate.instant("UI_ERROR_INGRESO_ORDEN_TRASPASO"),
//										conflictos:util.parseServerResponse(respuesta)
//									},idVentana);
//									bcsComunes.ventanaOverlay(idVentana,false);
//								}
								
								//CAMBIO VENTANA RESULTADO
								respuesta.P_OUT_RESULTADO = mensajeResultadoOrdenes;
								if(respuesta.P_OUT_RESULTADO.length > 0){
									var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false, $scope.winID);		
									var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
									promesa.then(function(data){
										bcsComunes.ventanaOverlay(ventanaActual,false);
										bcsComunes.ventanaOverlay(idVentana,false);
										bcsComunes.ventanaCerrar(idVentana);
										//bcsComunes.ventanaCerrar($scope.IdVentana);
									});	
								}
							});
							/*Fin - Guardo Relacion entre Orden y Traspaso*/
			     			bcsComunes.ventanaOverlay($scope.winID,false);
			     		}		
			       	 }else{
			     		bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
			     		bcsComunes.ventanaOverlay($scope.winID,false);
			     	 }
		   		 },function(respuesta){
		   			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_INGRESO_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
		   			bcsComunes.ventanaOverlay($scope.winID,false);
		   		});
		     };
		     
		   //******************************************************************************************************************************/
			//***************************************************** Ingreso Orden RV *****************************************************/
			//******************************************************************************************************************************/		
		     desAPV.fn.enviarOrdenesRV = function(Ordenes, cobros) {
		   		bcsComunes.ventanaOverlay(idVentana,true);
		    	 var datosIngresoOrden = {
		    			 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
		    			 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo : 'APV',
						 p_simularOperacion : 'N',
						 p_UsaReglasNegocio : 'S',
						 p_Ordenes : Ordenes,
						 p_CobrosModificados : cobros
		    	 };

				 bcsComunes.ventanaOverlay(idVentana,true);
				 var promise = bcsServicioOrden.CrearOrdenes(datosIngresoOrden).$promise;
				 promise.then(function(respuesta){				   		
					//se valido la orden
					if (respuesta.P_OUT_RESULTADO[0] !== undefined && respuesta.P_OUT_RESULTADO[0].IdOrden > 0 && respuesta.P_OUT_ERRORESCONTADOR === 0) 
					{
						var IdOrden = respuesta.P_OUT_RESULTADO[0].IdOrden;
						desAPV.datosForm.modo = 'RESUMEN';
				
						var datosCambiarEstadoTraspaso = { 
								p_IdEmpresa 	: $scope.SesionIdEmpresa,				
								p_IdUsuario     : $scope.SesionIdUsuario,
								p_IdModulo		: 'APV',
								p_Parametros	: [{
													IdEstado : 'PRO',
													IdEmpresa: $scope.SesionIdEmpresa,
													IdTraspaso : parametrosVentana.IdTraspaso || 0,
													IdContratoSAAPV : 0
													}]
							};	
						
						bcsServiciosAPV.CambiarEstadoTraspaso(datosCambiarEstadoTraspaso).$promise.then(function(respuesta){		
							if (respuesta.P_OUT_ERRORESCONTADOR > 0){
								bcsComunes.ventanaMsg({
									mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
									conflictos:util.parseServerResponse(respuesta)
								},$scope.winID);		
							}
						},
						function(respuesta){
							bcsComunes.ventanaMsg({
								mensaje:$translate.instant("UI_PROBLEMAS_AL_CAMBIAR_ESTADO_TRASPASO"),
								conflictos:util.parseServerResponse(respuesta)
							},$scope.winID);		
							bcsComunes.ventanaOverlay(IdVentana, false);
						});
					
						
						
						/*
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_ORDEN_INGRESADA")+": " + respuesta.P_OUT_RESULTADO[0].IdOrden,
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
						*/
						var Orden = Array();
			     		Orden = respuesta.P_OUT_RESULTADO; 
//			     		$.each(Orden,function(key,value){
//			     			var resultado = {
//				     				Id : 1,
//									tipo : 'INFORMACION',
//									codigo : '',
//									descripcion : "Operacion RV ingresada: " + value.IdOrden
//				     		}; 
//				     		mensajeSalida.push(resultado);
//			     		});
//			     		
						//CAMBIO VENTANA RESULTADO

			     		$.each(Orden,function(key,value){
				     		var objCarga = {
									IdInterno : 1,
									tipo : 'INFORMACION',
									codigo : '',
									descripcion : "Operacion RV ingresada : " + value.IdOrden
							};
				     		mensajeResultadoOrdenes.push(objCarga);
			     		});

			     		
			     		
			     		if(parametrosVentana.IdTraspaso === undefined){
			     			if(dsCustodia.data().length > 0){
			     				var detalle = [];
			     				$.each(dsCustodia.data(),function(key,value){
			     					var regimen = value.Regimen.split("-");
			     					var CodTipoAhorro = regimen[0];
			     					
				     				detalle.push({
				     					IdDetalleOrigenTraspaso : 0,
				     					IdTraspaso : 0,
				     					//Terminar de Asignar.
				     					CodigoTipoAhorro : CodTipoAhorro,
				     					IdEntidadRecaudadora : desAPV.datosForm.EntidadDestinoSeleccionado.IdSocioDeNegocio,
				     					NombreEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.NombreCompleto,
				     					DocumentoEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.DocumentoIdentidadDefecto,
				     					Fondo: '',//Definir.
				     					MontoTraspaso: value.MontoADesinvertir,
				     					UFTraspaso: 0,
				     					PorcentajeTraspaso: desAPV.PorcentajeFinal,
				     					IdEstado: 'VIG'
									});
				     			});

				     			/*Inicio - Creacion Traspaso y Detalle Traspaso.*/
								var Traspaso = {
									p_IdEmpresa : $scope.SesionIdEmpresa,
									p_IdUsuario : $scope.SesionIdUsuario,
									p_IdModulo  : "APV",
									p_Traspaso : {
										IdTraspaso : 0,
										IdContratoSAAPV : 0,
										IdEmpresa : $scope.SesionIdEmpresa,
										TipoMovimiento : 'EGRESO',
										RazonRetiro: desAPV.datosForm.RazonRetiroIdActual,
										FechaIngreso: "0001-01-01",
										FechaEgreso: "0001-01-01",
										FechaModificacion: "0001-01-01",
										CodigoTipoTraspaso: desAPV.datosForm.tipoEgresoIdActual,
										Descripcion: '',
										IdCliente: desAPV.datosForm.SocioNegocioSeleccionado.IdCliente,
										IdPersona: desAPV.datosForm.EntidadDestinoSeleccionado.IdPersona,
										CodigoTipoAhorro: '',	
										NombreEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.NombreCompleto,
										DocumentoEntidad: desAPV.datosForm.EntidadDestinoSeleccionado.DocumentoIdentidadDefecto,
										IdEstado: "PDI",
									},
									p_DetalleOrigenTraspaso : detalle,
								}
								
								bcsServiciosAPV.CrearTraspaso(Traspaso).$promise.then(function(respuesta){
									if (respuesta.P_OUT_ERRORESCONTADOR === 0){

										IdTraspaso = respuesta.P_OUT_IDTRASPASO;
//										var resultado = {
//							     				Id : 1,
//												tipo : 'INFORMACION',
//												codigo : '',
//												descripcion : "Traspaso Egreso ingresado : " + IdTraspaso
//							     		}; 
//							     		mensajeSalida.push(resultado);
							     		
										//CAMBIO VENTANA RESULTADO

							     		var objCarga = {
												IdInterno : 1,
												tipo : 'INFORMACION',
												codigo : '',
												descripcion : "Traspaso Egreso ingresado : " + IdTraspaso
										 };
							     		
										mensajeResultadoOrdenes.push(objCarga);

							     		
										var OrdenTraspaso = Array();
										$.each(Orden,function(key,value){
							     			var obj = {
					     						IdOrdenTraspaso : 0,
												IdTraspaso : IdTraspaso || 0,
												IdOrden : 0,
												IdSolicitudAporteRescate : value.IdOrden
								     		}; 
							     			OrdenTraspaso.push(obj);
							     		});
										
										/*Inicio - Guardo Relacion entre Orden y Traspaso*/
										var OrdenTraspaso = {
											p_IdEmpresa : $scope.SesionIdEmpresa,
											p_IdUsuario : $scope.SesionIdUsuario,
											p_IdModulo  : "APV",
											p_OrdenTraspaso : OrdenTraspaso
										};
										bcsServiciosAPV.CrearOrdenTraspaso(OrdenTraspaso).$promise.then(function(respuesta){
											if(OrdenesFM.length > 0)
												desAPV.aporteNormal.ingresarAporteRescate(OrdenesFM);
											else{
												//CAMBIO VENTANA RESULTADO
												respuesta.P_OUT_RESULTADO = mensajeResultadoOrdenes;
												if(respuesta.P_OUT_RESULTADO.length > 0){
													var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false, $scope.winID);		
													var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
													promesa.then(function(data){
														bcsComunes.ventanaOverlay(idVentana,false);
														bcsComunes.ventanaCerrar($scope.IdVentana);
													});	
												}
											}
											
//											if (respuesta.P_OUT_ERRORESCONTADOR === 0){
//												respuesta.P_OUT_MENSAJES = mensajeSalida;
//												bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
//												bcsComunes.ventanaOverlay(idVentana,false);
//											}else{
//												bcsComunes.ventanaMsg({
//													mensaje:$translate.instant("UI_ERROR_INGRESO_ORDEN_TRASPASO"),
//													conflictos:util.parseServerResponse(respuesta)
//												},idVentana);
//												bcsComunes.ventanaOverlay(idVentana,false);
//											}
										});
										/*Fin - Guardo Relacion entre Orden y Traspaso*/
						     			bcsComunes.ventanaOverlay($scope.winID,false);
						     			
									}else{
										bcsComunes.ventanaMsg({
											mensaje:$translate.instant("UI_ERROR_INGRESO_TRASPASO"),
											conflictos:util.parseServerResponse(respuesta)
										},idVentana);
										bcsComunes.ventanaOverlay(idVentana,false);
									}
								});
			     			};			     			
			     		}else{
			     			var OrdenTraspaso = Array();
							$.each(Orden,function(key,value){
				     			var obj = {
		     						IdOrdenTraspaso : 0,
									IdTraspaso : parametrosVentana.IdTraspaso || 0,
									IdOrden : 0,
									IdSolicitudAporteRescate : value.IdOrden
					     		}; 
				     			OrdenTraspaso.push(obj);
				     		});
							
							/*Inicio - Guardo Relacion entre Orden y Traspaso*/
							var OrdenTraspaso = {
								p_IdEmpresa : $scope.SesionIdEmpresa,
								p_IdUsuario : $scope.SesionIdUsuario,
								p_IdModulo  : "APV",
								p_OrdenTraspaso : OrdenTraspaso
							};
							
							bcsServiciosAPV.CrearOrdenTraspaso(OrdenTraspaso).$promise.then(function(respuesta){
								
								//CAMBIO VENTANA RESULTADO
								mensajeResultadoOrdenes = new Array();
								$.each(respuesta.P_OUT_MENSAJES,function(key,value){
									 var objCarga = {
											IdInterno : value.id,
											tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
											codigo : value.codigo,
											descripcion : value.descripcion
									 };
									 mensajeResultadoOrdenes.push(objCarga);
								});
								
								
//								if (respuesta.P_OUT_ERRORESCONTADOR === 0){
//									respuesta.P_OUT_MENSAJES = mensajeSalida;
//									bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(respuesta)},idVentana);
//									bcsComunes.ventanaOverlay(idVentana,false);
//								}else{
//									bcsComunes.ventanaMsg({
//										mensaje:$translate.instant("UI_ERROR_INGRESO_ORDEN_TRASPASO"),
//										conflictos:util.parseServerResponse(respuesta)
//									},idVentana);
//									bcsComunes.ventanaOverlay(idVentana,false);
//								}
								
								//CAMBIO VENTANA RESULTADO
								
								if(OrdenesFM.length > 0)
									desAPV.aporteNormal.ingresarAporteRescate(OrdenesFM);
								else{
									//CAMBIO VENTANA RESULTADO
									respuesta.P_OUT_RESULTADO = mensajeResultadoOrdenes;
									if(respuesta.P_OUT_RESULTADO.length > 0){
										var IdVentanamodal = bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: $translate.instant('UI_RESULTADOS'), campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : mensajeResultadoOrdenes } ,false,false, $scope.winID);		
										var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
										promesa.then(function(data){
											bcsComunes.ventanaOverlay(idVentana,false);
											bcsComunes.ventanaCerrar($scope.IdVentana);
										});	
									}
								}
							});
							/*Fin - Guardo Relacion entre Orden y Traspaso*/
			     		}		
					}
					else{
						bcsComunes.ventanaMsg({
							mensaje:$translate.instant("UI_NO_SE_PUDO_INGRESAR_ORDEN"),
							conflictos:util.parseServerResponse(respuesta)
						},idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}						
			   	},function(respuesta){
			   		bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_EXISTEN_PROBLEMAS_PARA_INGRESAR_ORDEN"),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
						
					bcsComunes.ventanaOverlay(idVentana,false);
				});
		     };
			   	
	     //******************************************************************************************************************************/
		 //********************************************** Envia Solicitud de Desinversión ****************************************************/
		 //******************************************************************************************************************************/
	     desAPV.fn.enviarDesinversion = function(){
	    	 
	    	 if(dsCustodia.data().length === 0){
	    		 bcsComunes.msg("warning","warning","No se encuentran Ordenes para Desinvertir.");
				 return; 
	    	 }
	    	 
	    	 var OrdenesRV = Array();
	    	 OrdenesFM = Array();
	    	 var IdInterno = 1;
	    	 var IdInternoRV = 1;
	    	 mensajeSalida = Array();
	    	 cobrosModificadosFinal = [];

	    	 if(dsCustodia.data().length > 0){
	    		 
	    		 var parametrosPreferencia = {
		    			 p_IdEmpresa : $scope.SesionIdEmpresa, //saca de variables de entorno
		    			 p_IdUsuario : $scope.SesionIdUsuario, //saca de variables de entorno
						 p_IdModulo : 'APV',
						 p_IdCliente : desAPV.datosForm.SocioNegocioSeleccionado !== undefined ? desAPV.datosForm.SocioNegocioSeleccionado.IdCliente : 0,
		    	 };

	    		 bcsServiciosAPV.ConsultarPreferenciaPagoPortafolio(parametrosPreferencia).$promise.then(function(respuesta){
	    			 // Ciclo de ordenes RV
			    	 $.each(dsCustodia.data(),function(key,value){
			    		 if(value.IdClasificacionTipoInstrumento === "RV"){
			    			 var preferenciaPagoPortafolio = null;
			    			 if(respuesta.P_OUT_PREFERENCIA_PAGO_PORTAFOLIO.length > 0){
			    				 $.each(respuesta.P_OUT_PREFERENCIA_PAGO_PORTAFOLIO,function(llave,valor){
				    				 if(value.IdPortafolio === valor.IdPortafolio){
				    					 preferenciaPagoPortafolio = valor.IdPreferenciaPagoPortafolio;
				    					 return false;
				    				 }
				    			 });
			    			 }
			    			
			    			 
			    			 var Orden = {
									IdInterno :IdInternoRV,
								    FechaRecepcion : '0001-01-01 00:00:00',
									FechaVigencia  : '0001-01-01',
									FechaEmision : '0001-01-01',
									FechaOperacion : '0001-01-01 00:00:00', //se calcula en el server
									IdCondicionDeLiquidacion : desAPV.datosForm.condicionLiquidacionSeleccionada.IdCondicionDeLiquidacion,//'T0',
									IdTipoOperacion : 'VENACN',
									IdTipoVigencia : desAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden,
									Glosa : '',
									Cantidad : value.CantidadADesinvertir,
									MontoNeto : 0,
									ValorRescate : 0,
									IdBoveda 		: value.IdBoveda,
									IdFormaCustodia : 'CUS',
									IdPorcentajeVisibilidad :1,
									PrecioLimite : value.PPPCustodia,
									PrecioMercado : value.PPPCustodia,
									EsFacturaInmediata : 'N',
									IdTipoPrecio : 'M',
									IdPortafolio : value.IdPortafolio,
									IdTipoCanalIngreso : desAPV.datosForm.canalSeleccionado.IdTipoCanalIngreso, //$scope.SesionTipoCanalIngreso,
									IdTipoReajuste : '',
									IdEmisor: value.IdEmisor,
									IdBolsa : desAPV.datosForm.bolsaSeleccionada.IdBolsa,
									EspecificaBolsa : desAPV.datosForm.bolsaSeleccionada.IdBolsa === 0? 'N':'S',
									IdMonedaLiquidacionOrden : 'CLP',//value[0].IdMonedaLiquidacionOrden, Falta definir
									IdMonedaLiquidacionCobros : 'CLP',//value[0].IdMonedaLiquidacionOrden, Falta definir
									TipoCambio : null, // desAPV.datosForm.tipoCambio || null,
									IdInstrumento : value.IdInstrumento,
									IdInstrumentoSubyacente : 0,
									IdCondicionDeLiquidacionPlazo : '',
									TasaVentaCorta : 0,
									PlazoVentaCorta : 0,
									EsVentaCorta : 'N',
									TasaSimultanea : 0,
									PlazoSimultanea : 0,
									PlazoIF : 0,
									TasaRFIF : 0,
									EsOD: 'N',
									EsFungible : 'N',
									MontoPlazo : 0,
									IdTipoLibroOrden : 'NORMAL',
									IdLibroOrdenes : null,
									IdCuentaCustodioPortafolio : null,
									EsPorMonto : 'N',
									IdCorredorCorresponsal : null, //desAPV.datosForm.idCorredorCorresponsal || null,
									IdPersonaAutorizadoDarOrdenes : null, //desAPV.datosForm.autorizadoDarOrdenes.IdPersona || null,
									IdTipoCanalDistribucion : desAPV.datosForm.canalSeleccionado.IdTipoCanalDistribucion, //	desAPV.datosForm.idTipoCanalDistribucion || null,
									EsOrdenEspecial : 'N', //desAPV.datosForm.esOrdenEspecial === true?"S":"N",
									PagoCobroCuentaInversion :'N', //  desAPV.datosForm.pagoCobroCuentaInversion === true?"S":"N",
									IdPreferenciaPagoPortafolio : preferenciaPagoPortafolio, //desAPV.datosForm.formaDePagoSeleccionada.IdPreferenciaPagoPortafolio,
									BaseTasa : 0,
									TipoInteres : "",
									IdMecanismoCaptacion : desAPV.datosForm.mecanismoDeCaptacionSeleccionado.IdDetalleIndicador
								};
			    			 		 		    	 
					    	 if(cobrosCustodias.length > 0){
					    		 $.each(cobrosCustodias,function(key,valor) {
			    					if(valor.Producto === value.NombreProductoAgrupador && valor.Regimen === value.Regimen && valor.Instrumento === value.Nemotecnico){
			    						cobrosModificadosFinal.push({ IdInterno: IdInternoRV ,IdCobro : valor.IdCobro, ValorCobroAplicado : valor.ValorCobroAplicado  });
					    			 }	    			
					    		 });
					    	 }
			    	 		OrdenesRV.push(Orden);
			    	 		IdInternoRV = IdInternoRV + 1;
						}
			    		if(value.IdClasificacionTipoInstrumento === "FM"){
			    			var Orden = { 
								   	IdInterno		      		: IdInterno,
									IdCliente 	          		: desAPV.datosForm.SocioNegocioSeleccionado.IdCliente,
									IdPortafolio          		: value.IdPortafolio,
									TipoSolicitud         		: 'RES',
									IdSerieFondo          		: value.Nemotecnico,
									CalculoPorMontoCuota  		: 'C',
									EsPorMontoLiquido			: 'N',
									Monto 				  		: value.MontoADesinvertirCLP,
									NumeroCuotas 	  	  		: value.CantidadADesinvertir,
									IdMoneda 			  		: value.IdMonedaInstrReajuste,
									IdMonedaPago				: 'CLP',
									IdMedioDePago				: parametrosVentana.IdTraspaso ? 'TRA' : 'CHE',//desAPV.kngpagoCobroCI === true ? 'CIN' : desAPV.datosForm.FormaPagoCobroIdActual,
									IdPreferenciaPagoPortafolio : 0,
									Visado						: 'N',
									SolicitudBatch				: 'N',
									OrigenOperacion				: 'M',
									FechaOperacion				: $scope.SesionFechaSistema,
									IdRegimenTributario			: value.Regimen,
									Glosa						: 'DESINVERSION APV',
									IndicadorMLT				: 'N',
									IdSerieFondoDestino			: '',
									IdEmisorDestino				: '',
									IdAutorizadoOrigen			: 0,
									Porcentaje					: desAPV.PorcentajeFinal,
									FolioContrato				: parametrosVentana.IdTraspaso || 0,
									EsTraspaso					: parametrosVentana.RazonRetiro || '',
									Canal						: parametrosVentana.Canal || 'OPT'
						   }
						   IdInterno = IdInterno + 1;
						   OrdenesFM.push(Orden);
			    		}
					}); 
			    	 
			    	mensajeResultadoOrdenes = new Array();
			    	 
			    	if(OrdenesRV.length > 0)
						desAPV.fn.enviarOrdenesRV(OrdenesRV,cobrosModificadosFinal);
			    	else if(OrdenesFM.length > 0)
						desAPV.aporteNormal.ingresarAporteRescate(OrdenesFM);
			    	else{
			    		return;
			    	}
	    		 });
	    	 }else{
	    		 bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_NO_HAY_MONTOS_DESINVERSION")},idVentana);
	    	 }
	     };	     
	     
	     desAPV.limpiarPorcentaje = function(){
	    	 if(desAPV.datosForm.kngTotalDesinvertir > 0){
    			 desAPV.datosForm.kngPorcentaje = 0;
	    	 }
	     };
	     
	     $scope.$watch('desAPV.datosForm.kngPorcentaje',function(newval,oldval){
	    	 if(desAPV.datosForm.kngPorcentaje > 0){
    			 desAPV.datosForm.kngTotalDesinvertir = 0;
    			 montoPesos = 0;
    		}
	    	 if(desAPV.datosForm.kngPorcentaje > 100)
	    		 desAPV.datosForm.kngPorcentaje = 100;
	    });
	    		     
	     
	     $scope.$watch('desAPV.datosForm.RazonRetiroSeleccionado',function(newval,oldval)
	    {
			 desAPV.fn.filtroRazonRetiro(parametrosVentana.SocioSeleccionado);
	    	 
			 if(desAPV.datosForm.RazonRetiroIdActual !== undefined && desAPV.datosForm.RazonRetiroIdActual !== false){
	    		 desAPV.habilitaEntidadDestino = true;
	    		 if(desAPV.datosForm.RazonRetiroIdActual === 'JUB' || desAPV.datosForm.RazonRetiroIdActual === 'TRA')
	 	    		desAPV.habilitaEntidad = false;
	    		 else
	 	    		desAPV.habilitaEntidad = true; 
	    	 }else{
	    		 desAPV.habilitaEntidadDestino = false;
	    		 desAPV.datosForm.EntidadDestinoSeleccionado = undefined;
	    		 desAPV.datosForm.EntidadDestinoIdActual = false;
	    	 }
	    });
	     
	     $scope.$watchGroup(['desAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden'], function(newval,oldval){
	 		if (desAPV.datosForm.tipoVigenciaSeleccionada && desAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden){
	 			if (desAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden ===  bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaSoloHoy){
	 				desAPV.datosForm.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	 			}
	 			else if (desAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaPermanente) {
	 				desAPV.datosForm.fechaVigencia = util.dateDS("0001-01-01","FechaSistema","CALL");
	 			}
	 			else if (desAPV.datosForm.tipoVigenciaSeleccionada.IdTipoVigenciaOrden === bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaFechaFija) {
	 				desAPV.datosForm.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	 			}
	 			else if (desAPV.datosForm.tipoVigenciaSeleccionada === bcsConfiguracionAPV.CONSTANTES.IdTipoVigenciaSemanal) {
	 				var datos = {
	 					p_IdEmpresa : $scope.SesionIdEmpresa,
	 					p_IdUsuario : $scope.SesionIdUsuario,
	 					p_IdModulo : 'ORD',
	 					p_UsaReglasNegocio : 'S',
	 					p_Fecha : $scope.SesionFechaSistema,
	 					p_NumeroDias : 5,
	 					p_EsHabil : 'S',
	 					p_IdPais : $scope.SesionIdPaisSistema
	 				};
	 				bcsServicioPlataforma.FechaSiguiente(datos,function(result) {
	 					desAPV.datosForm.fechaVigencia = util.dateDS(result.P_OUT_FECHASIGUIENTE,"FechaSistema","CALL");
	 					}
	 				,function(result){
	 					console.log(result);
	 					}
	 				);
	 			}
	 			else {
	 				desAPV.datosForm.fechaVigencia = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	 			}
	 		}
	 	});
	     
	    $scope.$watch('desAPV.datosForm.SocioNegocioSeleccionado',function(newval,oldval)
	    {
	    	if(newval === false && entrada === false)
	    	{
	    		if(parametrosVentana.IdSocioNegocio !== undefined)
	    		{
		    		desAPV.datosForm.SocioNegocioidActual = parametrosVentana.IdSocioNegocio;
		    		desAPV.IdSocioNegocio = parametrosVentana.IdSocioNegocio;
		    		desAPV.IdSocioNegocioDefecto = parametrosVentana.IdSocioNegocio;
		    		entrada = true;
	    		}
	    	}
	    	
	    	if(inicio > 1)
	    	{
	    		desAPV.fn.limpiarFiltros(false)
		    	desAPV.fn.limpiarFormulario();
	    	}
	    	else
	    	{
	    		inicio = inicio + 1;
	    	}
	    });
	    
	    $scope.$watch('$viewContentLoaded', function()
	    {
	    	setTimeout(function(){ 
				desAPV.consultas.buscarMovimientos();
			}, 6000);
	    });
	     
	     $("#cobrosGrilla").css('pointer-events', 'none');
	});
}());