angular.module('ModuloOrdenes').controller('DetalleSimultaneaCtrl',function ($scope,bcsComunes,$translate,$timeout) {
	var parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
	$scope.simultanea = parametros.simultanea;
	$scope.configuracionOrdenes = {
			allowCopy: true,
			autoBind: true,
			//height:parametros.simultanea.ordenes.length * 50,
			dataSource: new bcs.DataSource ({
	  			type : "array",
	  			definition : {
					data : parametros.simultanea.ordenes,
					schema : {
						model : {
							fields : {
								Instrumento			: { type: "string"},
								IdTipoInstrumento	: { type: "string"},
								Cantidad			: { type: "number"},
								Precio				: { type: "number"},
								Monto				: { type: "number"},
								IdMoneda			: { type: "string"},
							}
						}
					}
				},
	  		}),
	  		columns : [
				{field: "Instrumento",			title: $translate.instant('UI_INSTRUMENTO'),	width: "150px"},
				{field: "IdTipoInstrumento",	title: $translate.instant('UI_TIPO_INSTRUMENTO'),	width: "100px"},
				{field: "IdMoneda",				title: $translate.instant('UI_MONEDA'),			width: "100px"},
				{field: "Cantidad",				title: $translate.instant('UI_CANTIDAD'),	width: "100px", 
					template :" #:  (util.bcs_formato(Cantidad,'ORD','Cantidad',IdMoneda, IdTipoInstrumento,'')).valor #"},
				{field: "Precio",				title: $translate.instant('UI_PRECIO'),	width: "100px",
						template :" #:  (util.bcs_formato(Precio,'ORD','Precio',IdMoneda, IdTipoInstrumento,'')).valor #"},
				{field: "Monto",				title: $translate.instant('UI_MONTO'),
							template :" #:  (util.bcs_formato(Monto,'ORD','Monto',IdMoneda, IdTipoInstrumento,'')).valor #"}
				]
	}

	$scope.configuracionGarantias = {
			allowCopy: true,
			autoBind: true,
			//height:parametros.simultanea.ordenes.length * 50,
			dataSource: new bcs.DataSource ({
	  			type : "array",
	  			definition : {
					data : parametros.simultanea.garantias,
					schema : {
						model : {
							fields : {
								Instrumento			: { type: "string"},
								IdTipoInstrumento	: { type: "string"},
								Cantidad			: { type: "number"},
								Precio				: { type: "number"},
								Monto				: { type: "number"},
								IdMoneda			: { type: "string"},
								Origen				: { type: "string"},
							}
						}
					}
				},
	  		}),
	  		columns : [
				{field: "Origen",				title: $translate.instant('UI_ORIGEN'),	width: "100px"},
				{field: "Instrumento",			title: $translate.instant('UI_INSTRUMENTO'),	width: "100px"},
				{field: "IdMoneda",				title: $translate.instant('UI_MONEDA'),	width: "75px"},
				{field: "IdTipoInstrumento",	title: $translate.instant('UI_TIPO_INSTRUMENTO'),	width: "100px"},
				{field: "Cantidad",				title: $translate.instant('UI_CANTIDAD'),	width: "75px", 
					template :" #:  (util.bcs_formato(Cantidad,'ORD','Cantidad',IdMoneda, IdTipoInstrumento,'')).valor #"},
				{field: "Precio",				title: $translate.instant('UI_PRECIO'),	width: "100px",
						template :" #:  (util.bcs_formato(Precio,'ORD','Precio',IdMoneda, IdTipoInstrumento,'')).valor #"},
				{field: "Monto",				title: $translate.instant('UI_MONTO'),
							template :" #:  (util.bcs_formato(Monto,'ORD','Monto',IdMoneda, IdTipoInstrumento,'')).valor #"}
				]
	}
	

	$scope.configuracionCobros = {
			allowCopy: true,
			autoBind: true,
			selectable: "multiple, row",
			dataSource: new bcs.DataSource ({
	  			type : "array",
	  			definition : {
					data : parametros.simultanea.cobros,
					schema : {
						model : {
							fields : {
								IdCobro						: { type: "string"},
								NombreCobro					: { type: "string"},
								FormaCobro					: { type: "string"},
								NombreFormaCobro            : { type: "string"},
								ValorCobro			        : { type: "number"},
								IdMoneda					: { type: "string"},								
								MontoCobroMonedaOrden		: { type: "number"},
								MontoCobroMonedaLiquidacion	: { type: "number"},
								IdMonedaCobro 				: { type: "string"},
								IdMonedaLiquidacionOrden 	: { type: "string"}
							}
						}
					}
				},
	  		}),
	  		columns : [	  			
	  			{field: "IdCobro",						title: $translate.instant('UI_CODIGO'),	width: "80px"},
	  			{field: "NombreCobro",					title: $translate.instant('UI_NOMBRE'),	width: "180px"},
	  			{field: "NombreFormaCobro",				title: $translate.instant('UI_FORMA'),	width: "50px"},
	  			{field: "ValorCobro",			        title: $translate.instant('UI_VALOR_COBRO'),	width: "80px", template: kendo.template("#if ( FormaCobro == 'F')  {##= (util.bcs_formato(ValorCobro,'GEN','Monto',IdMoneda,'','')).valor ##} else { ##=ValorCobro + '%' ## } #") },
	  			{field: "MontoCobroMonedaOrden",		title: $translate.instant('UI_MONTO'),	width: "80px",             template :" #:  (util.bcs_formato(MontoCobroMonedaOrden,'ORD','Monto',IdMonedaCobro, '','')).valor #"},
	  			{field: "MontoCobroMonedaLiquidacion",	title: $translate.instant('UI_MONTO_LIQUIDACION'),	width: "100px", template :" #:  (util.bcs_formato(MontoCobroMonedaLiquidacion,'ORD','Monto',IdMonedaLiquidacionOrden, '','')).valor #"},
	  			{field: "IdMoneda",						title: $translate.instant('UI_MONEDA'),	width: "80px"},				
				]
	}

	
	$timeout(function(){bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));},10);
});
