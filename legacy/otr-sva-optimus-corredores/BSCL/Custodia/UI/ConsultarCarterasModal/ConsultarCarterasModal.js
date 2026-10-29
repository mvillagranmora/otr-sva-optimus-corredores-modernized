angular.module('ModuloCustodia').controller('ConsultarCarterasModalCtrl',
		function ($rootScope,$scope,$translate,bcsComunes) {

	$scope.data = {};
	$scope.carteras = {};

	var filters = [];
	//var overlay = bcsComunes.overlay;
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.data.oDatosCartera = parametros;
	
	$scope.data.oDatosCartera.fechaBusqueda = util.dateInicioDia('1900-01-01');
	if ($scope.data.oDatosCartera.FechaCondicionLiquidacion !== '0001-01-01')
		$scope.data.oDatosCartera.fechaBusqueda = util.dateInicioDia($scope.data.oDatosCartera.FechaCondicionLiquidacion);
	
	$scope.oSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};

	$scope.oPortafolio={
		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 }
		},
	};
	
	$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.data.fechaOptions = {min:util.dateInicioDia($scope.SesionFechaSistema)};
	
	/*
	var tblInstr = [];
	var rowInstr = {
				"IdClasificacionTipoInstrumento"	:$scope.data.oDatosCartera.IdClasificacionTipoInstrumento,
				"IdTipoInstrumento"					:$scope.data.oDatosCartera.IdTipoInstrumento,
				"IdInstrumento"						:null,
				"IdEmisor"							:null,
				"FechaVencimiento"					:null,
				"IdTipoReajuste"					:null,
				"EsFungible"						:null,
				"IdBoveda"							:null
				};
	tblInstr.push(rowInstr);
		
	var tblCar = [];
	var rowCar = {
				"FolioCartera"						:null,
				"FolioGarantia"						:null,
				"FolioSubyacente"					:null,
				"IdCliente"							:Number($scope.data.oDatosCartera.IdCliente),
				"IdPortafolio"						:Number($scope.data.oDatosCartera.IdPortafolio)
				};
	tblCar.push(rowCar);*/
	
	$scope.esIF = ($scope.data.oDatosCartera.IdClasificacionTipoInstrumento=="IF"?true:false);
	$scope.esRF = ($scope.data.oDatosCartera.IdClasificacionTipoInstrumento=="RF"?true:false);

	
	//filtros iniciales
	filters.push({field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.data.oDatosCartera.IdClasificacionTipoInstrumento});
	if ($scope.data.oDatosCartera.IdTipoInstrumento !== ""){
		filters.push({field: "IdTipoInstrumento", operator: "eq", value: $scope.data.oDatosCartera.IdTipoInstrumento});
	}
	if ($scope.data.oDatosCartera.IdClasificacionTipoInstrumento === 'IF' || $scope.data.oDatosCartera.IdClasificacionTipoInstrumento === 'RF'){
		filters.push({field: "FechaVencimiento", operator: "gte", value: util.dateInicioDia($scope.data.kngFecha)});	
	}
	filters.push({field: "IdCliente", operator: "eq", value: Number($scope.data.oDatosCartera.IdCliente)});
	filters.push({field: "IdPortafolio", operator: "eq", value: Number($scope.data.oDatosCartera.IdPortafolio)});
	filters.push({field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa)});
	filters.push({field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.data.oDatosCartera.fechaBusqueda) });
	filters.push({field: "Origen", operator: "eq", value: 'CARTERA' });

	
	var dsGrillaCarteras = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: [ { field: "FolioRegistro", dir: "asc" } ],
			filter				: filters,
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1767",dataType : "json"}},
			schema : {
				model : {
					fields : {
						FechaLiquidacion					: {type : "sysdate"},
						IdCliente							: {type : "number"},
						IdPortafolio						: {type : "number"},
						IdClasificacionTipoInstrumento		: {type : "string"},
						NombreClasificacionTipoInstrumento	: {type : "string"},
						IdTipoInstrumento					: {type : "string"},
						NombreTipoInstrumento				: {type : "string"},
						IdEstado							: {type : "string"},
						DecimalesPrecio						: {type : "number"},
						IdInstrumento						: {type : "number"},
						Nemotecnico							: {type : "string"},
						ISIN								: {type : "string"},
						CorteMinimo							: {type : "number"},
						EstaInscrito						: {type : "string"},
						EsDivisible							: {type : "string"},
						RazonSocial							: {type : "string"},
						Lista								: {type : "string"},
						FechaEmision						: {type : "sysdate"},
						FechaVencimiento					: {type : "sysdate"},
						IdInstrumentoRelacionado			: {type : "number"},
						IdEmisor							: {type : "string"},
						RazonSocialEmisor					: {type : "string"},
						IdTipoReajuste						: {type : "string"},
						NombreTipoReajuste					: {type : "string"},
						Reajustable							: {type : "string"},
						IdMoneda							: {type : "string"},
						Tamano								: {type : "number"},
						EsFungible							: {type : "string"},
						CodigoIFRS							: {type : "string"},
						NemoSVS								: {type : "string"},
						IdBoveda							: {type : "string"},
						NombreBoveda						: {type : "string"},
						Cantidad							: {type : "number"},
						ValorRescate						: {type : "number"},
						Repeticiones						: {type : "number"},
						ValorNominal						: {type : "number"},
						Precio								: {type : "number"},
						Tasa								: {type : "number"},
						Monto								: {type : "number"},
						IdRegistro							: {type : "number"},
						FolioRegistro						: {type : "number"},
						FechaOperacion						: {type : "sysdate"},
						FechaVencimientoOperacion			: {type : "sysdate"},
						IdMonedaOperacion					: {type : "string"},
						IdTipoComprobante					: {type : "string"},
						FolioComprobante					: {type : "number"},
						FechaComprobante					: {type : "sysdate"},
						IdConcepto 							: {type : "string"},
						NombreConcepto 						: {type : "string"}
					}
				}
			}
		}
	});
	columnas = function() {
  		return [
  			 {field: "FolioRegistro"	,title: 'UI_FOLIO'	,width: "100px",	template: kendo.template("#if (( FolioRegistro == 0  || (FolioRegistro == null)))  {##} else {##=FolioRegistro##}#")}
			,{field: "IdTipoInstrumento"	,title: 'UI_TIPO_INSTRUMENTO'	,width: "70px"}
			,{field: "FechaLiquidacion"	,title: 'UI_FECHA_LIQUIDACION'	,width: "100px"}
			,{field: "Nemotecnico"	,title: 'UI_NEMOTECNICO'	,width: "100px"}
			,{field: "NemoSVS"	,title: 'UI_NEMO_SVS'	,width: "100px"}
			,{field: "Cantidad"	,title: 'UI_CANTIDAD'	,width: "100px",	hidden: true, template : " <label> #:  (util.bcs_formato(Cantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
			,{field: "ValorRescate"	,title: 'UI_VALOR_RESCATE'	,width: "100px", template : " <label> #:  (util.bcs_formato(ValorRescate,'CUS','Monto',Nemotecnico =='PAGARE R'?IdTipoReajuste:'" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
			,{field: "Repeticiones"	,title: 'UI_CANTIDAD'	,width: "100px",	hidden: true, template : " <label> #:  (util.bcs_formato(Repeticiones,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
			,{field: "ValorNominal"	,title: 'UI_VALOR_NOMINAL'	,width: "100px",	hidden: true, template : " <label> #:  (util.bcs_formato(ValorNominal,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
			,{field: "CorteMinimo"	,title: 'UI_CORTE_MINIMO'	,width: "100px",	hidden: true, template : " <label> #:  (util.bcs_formato(CorteMinimo,'CUS','Monto',IdTipoReajuste!=''?IdTipoReajuste:'CLP','','')).valor #</label>"}
			,{field: "EsFungible"	,title: 'UI_FUNGIBLE'	,width: "70px", hidden: true}
			,{field: "Monto"	,title: 'UI_MONTO'	,width: "100px",	template : " <label> #:  (util.bcs_formato(Monto,'CUS','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
			,{field: "Precio"	,title: 'UI_PRECIO'	,width: "100px",	template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>"}
			,{field: "Tasa"	,title: 'UI_TASA_COMPRA'	,width: "100px",	template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa','" + $rootScope.SesionIdMonedaSistema + "',IdTipoInstrumento,'')).valor #</label>"}
			,{field: "IdTipoReajuste"	,title: 'UI_TIPO_REAJUSTE'	,width: "100px"}
			,{field: "CodigoIFRS"	,title: 'UI_CODIGO_IFRS'	,width: "100px"}
			,{field: "FechaEmision"	,title: 'UI_FECHA_EMISION'	,width: "100px"}
			,{field: "FechaVencimiento"	,title: 'UI_FECHA_VENCIMIENTO'	,width: "100px"}
			,{field: "IdEmisor"	,title: 'UI_EMISOR'	,width: "100px"}
			,{field: "IdTipoInstrumento"	,title: 'UI_TIPO_INSTRUMENTO'	,width: "100px"}
			,{field: "IdClasificacionTipoInstrumento"	,title: 'UI_ID_CLASIFICACION_TIPO_INSTRUMENTO'	,width: "100px"}
			,{field: "IdBoveda"	,title: 'UI_BOVEDA'	,width: "100px"}
			,{field: "IdConcepto"	,title: 'UI_CODIGO_CONCEPTO'	,width: "100px"}
			,{field: "NombreConcepto"	,title: 'UI_CONCEPTO'	,width: "100px"}
			,{field: "FolioComprobante"	,title: 'UI_FOLIO_REFERENCIA'	,width: "100px", template: kendo.template("#if (( FolioComprobante == 0  || (FolioComprobante == null)))  {##} else {##=FolioComprobante##}#")}		
		];
    };
		

	$scope.carteras.confBasicaCarteras= {
		dataSource			: dsGrillaCarteras,
		columns				: columnas(),
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		filterable			: true,
		height				: 470,
		selectable 			: "single row",
		autoBind			: $scope.data.oDatosCartera.Filtrado || false,
		gridCreated 		: function(grid) {	    	
	    	if ($scope.SesionLocale === 'CL'){
	    		if (!$scope.esIF){
	    			grid.showColumn("Cantidad");
	    			grid.hideColumn("ValorRescate");
	    			grid.hideColumn("NemoSVS");
	    		} else {
	    			grid.showColumn("EsFungible");
	    		}
	    		
	    		if ($scope.esIF || $scope.esRF) grid.showColumn("CorteMinimo");
	    		
	    		if (!$scope.esIF && !$scope.esRF){
	    			grid.hideColumn("CorteMinimo");
	    			grid.hideColumn("EsFungible");
	    			grid.hideColumn("IdTipoReajuste");
	    			grid.hideColumn("CodigoIFRS");
	    			grid.hideColumn("FechaVencimiento");
	    			grid.hideColumn("Tasa");
	    		}
	    	}
	    	if ($scope.SesionLocale === 'PE'){
	    		grid.showColumn("Repeticiones");
	    		grid.showColumn("ValorNominal");
	    	}
	    }
	};


	$scope.data.btnBuscar = function(datos) {
		var filters = [];
		filters.push({field: "IdClasificacionTipoInstrumento", operator: "eq", value: $scope.data.oDatosCartera.IdClasificacionTipoInstrumento});
		if ($scope.data.oDatosCartera.IdTipoInstrumento !== ""){
			filters.push({field: "IdTipoInstrumento", operator: "eq", value: $scope.data.oDatosCartera.IdTipoInstrumento});
		}
		
		if ($scope.data.oDatosCartera.IdClasificacionTipoInstrumento === 'IF' || $scope.data.oDatosCartera.IdClasificacionTipoInstrumento === 'RF'){
			filters.push({field: "FechaVencimiento", operator: "gte", value: util.dateInicioDia($scope.data.kngFecha)});	
		}
		
		filters.push({field: "IdCliente", operator: "eq", value: Number($scope.oSocioNegocio.seleccionado.IdCliente)});
		filters.push({field: "IdPortafolio", operator: "eq", value: Number($scope.oPortafolio.seleccionado.IdPortafolio)});
		filters.push({field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa)});
		filters.push({field: "FechaLiquidacion", operator: "lte", value: util.dateTerminoDia($scope.data.kngFecha) });
		filters.push({field: "Origen", operator: "eq", value: 'CARTERA' });

		dsGrillaCarteras.filter(filters);
	};

	$scope.data.fnDobleClick = function(datos) {
		bcsComunes.ventanaData(idVentana,datos);
		bcsComunes.ventanaCerrar(idVentana);
	};


});