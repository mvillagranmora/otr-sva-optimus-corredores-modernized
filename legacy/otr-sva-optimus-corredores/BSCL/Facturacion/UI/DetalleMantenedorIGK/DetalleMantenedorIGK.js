angular.module('ModuloFacturacion').controller('DetalleMantenedorIGKCtrl',
		function($scope,$uibModal,$translate,bcsComunes, bcsServiciosFacturacion, bcsComprobanteFacturacionGrid,$http, $rootScope){
	
	
	//Recupera parametros desde la ventana recibida
	$scope.data	= {};

    var idVentana = bcsComunes.ventanaWinId ($scope);   
    $scope.parametros 	= bcsComunes.ventanaParams(idVentana);
	//Registro seleccionado
    var bModifMvtoMod = this;
    bModifMvtoMod.IdComprobante = $scope.parametros.IdComprobante;
    bModifMvtoMod.IdTipoComprobante = $scope.parametros.IdTipoComprobante;
	    
    //Variables de sistema
    $scope.data.IdEmpresa	= $scope.SesionIdEmpresa;	
    $scope.data.IdUsuario	= $scope.SesionIdUsuario;   
    $scope.data.btnRegularizar = false;

	$scope.getSchema = function(){
		return {
			model: {
				fields : {
					NroLinea					: { type: "number"},
					FechaLiquidacion			: { type: "sysdate"},
					NombreTipoInstrumento		: { type: "string"},
					NemotecnicoInstrumento		: { type: "string"},
					Cantidad					: { type: "number"},
					Precio						: { type: "number"},
					Tasa						: { type: "number"},
					Monto                       : { type: "number"},
					NombreTipoProducto			: { type: "string"},
					NombreTipoOperacion			: { type: "string"},
					Descripcion					: { type: "string"},
					
					IdCobro						: { type: "string"},
					NombreCobro					: { type: "string"},
					FormaCobro					: { type: "string"},
					Porcentaje					: { type: "number"},
					IdMonedaLocalCobro			: { type: "string"},
					MontoLocalCobro				: { type: "number"},
					IdMonedaLiquidacionCobro    : { type: "string"},  
					MontoLiquidacionCobro		: { type: "number"},
					
					NombreMedioDePago			: { type: "string"},
					Monto                       : { type: "number"},
					NombreFormaCustodia         : { type: "string"},
					MontoLiquidacionFormaDePago : { type: "number"},
					NombreTipoCuentaBancaria	: { type: "string"},
					NombreCuentaBancaria		: { type: "string"},
					NumeroCuentaBancaria		: { type: "number"},
					MayorMenorValor				: { type: "number"},
					MontoRetencionGananciaCap	: { type: "number"},
					MayorMenorValorActual		: { type: "number"}
				}
			}
		};
	};
	

	//Buscar Información del Comprobante
	$scope.comprobanteDatasource = new bcs.DataSource({
		type: "odata",
		definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "IdComprobante", dir: "desc" }, // Criterios de Ordenamiento.
							filter				: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
							transport 			: { read: {
														     url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306003",
														     dataType: "json"
													 	  }
												  },
							schema				: bcsComprobanteFacturacionGrid.modeloComprobantes()
						}
	});
	
	
	$scope.comprobanteDatasource.query().then(function(){
		$scope.$apply(function() {
			bModifMvtoMod.oComprobante 	= $scope.comprobanteDatasource.data();
		});
	});

	//GRILLA DE detalle
	var gridDetalle = null;
	$scope.confBasicaComprobanteDetalle = {
		dataSource			: new bcs.DataSource({
								type : "odata",
								definition : {
									serverFiltering : true, // Filtrado de datos en el servidor.
									serverPaging 	: true, // Paginado de los datos en el servidor.
									serverSorting 	: false, // Ordenamiento de los datos en el servidor.						
									sort 			: [ { field: "NroLinea", dir: "asc" } ],
									filter			: { field: "IdComprobante", operator: "eq", value: Number(bModifMvtoMod.IdComprobante)},
									transport 		: {read : {url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4306002",dataType : "json"}},
									schema 			: $scope.getSchema(),
								}
							  }),
		columns				: [
								 {field : "NroLinea", 				title:"UI_NUMERO_LINEA",		width: "30px"}
								,{field : "FechaLiquidacion", 		title:"UI_FECHA_LIQUIDACION", 	width: "85px"}
								,{field : "NombreTipoInstrumento", 	title:"UI_TIPO_INSTRUMENTO", 	width: "80px"}
								,{field : "NemotecnicoInstrumento", title:"UI_INSTRUMENTO", 		width: "80px"}
								,{field : "Cantidad", 				title:"UI_CANTIDAD", 			width: "80px",	template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
								,{field : "Precio", 				title:"UI_PRECIO", 				width: "80px",	template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
								,{field : "Tasa",   				title:"UI_TASA", 				width: "80px",	template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMoneda,IdTipoInstrumento,'')).valor #</label>"}
								,{field : "Monto",                	title:'UI_MONTO_OPERADO',		width: "80px", 	template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
								,{field : "NombreFormaCustodia",    title:'UI_FORMA_CUSTODIA',		width: "80px"}
								,{field : "NombreTipoProducto",		title:'UI_TIPO_PRODUCTO',		width: "80px"}
								,{field:  "MayorMenorValor",  		title:'UI_MAYOR_MENOR_VALOR',	width: "80px", editable: true}
								,{field : "MontoRetencionGananciaCap",title:'UI_RETENCION',			width: "80px", template : " <label> #:  (util.bcs_formato(MontoRetencionGananciaCap,'GEN','Monto',IdMoneda,'','')).valor #</label>",editable: true}
								,{field : "MayorMenorValorActual",hidden : true}
								
							  ],
		save: function (e) {
			var seleccionado = e.model;
			seleccionado.HayDatosModificados = 'N';

			if (e.values.MayorMenorValor != undefined && e.values.MayorMenorValor !== seleccionado.MayorMenorValorActual) {
				e.preventDefault(); //Para redefinir el comportamiento del evento 'save'

				seleccionado.MayorMenorValor = e.values.MayorMenorValor;				
				//Calcular retencion en base al porcentaje de retencion (indicador)
				var MontoRetencion = seleccionado.MontoRetencionGananciaCap;
				if(bModifMvtoMod.oComprobante[0].Residente == "RESIDENTE"){					
					MontoRetencion = Math.round(seleccionado.MayorMenorValor * Number(bModifMvtoMod.oComprobante[0].PorcentajeRetencionIgk / 100));
				}								
				seleccionado.MontoRetencionGananciaCap = MontoRetencion;
				seleccionado.HayDatosModificados = 'S';
				gridDetalle.refresh();
			}			
			else if(e.values.MontoRetencionGananciaCap != undefined && e.values.MontoRetencionGananciaCap !== seleccionado.MontoRetencionGananciaCap){
				e.preventDefault(); 
				
				seleccionado.MontoRetencionGananciaCap = Math.round(e.values.MontoRetencionGananciaCap);
				seleccionado.HayDatosModificados = 'S';
				gridDetalle.refresh();

			}			
			else{
				seleccionado.HayDatosModificados = 'N'
				$scope.data.btnRegularizar = false;
				var MontoRetencion = seleccionado.MontoRetencionGananciaCap;
				if(bModifMvtoMod.oComprobante[0].Residente == "RESIDENTE"){					
					MontoRetencion = Math.round(seleccionado.MayorMenorValor * Number(bModifMvtoMod.oComprobante[0].PorcentajeRetencionIgk / 100));
				}
				seleccionado.MontoRetencionGananciaCap = MontoRetencion;
				gridDetalle.refresh();
			}
			
			if(seleccionado.HayDatosModificados = 'S'){
				$scope.data.btnRegularizar = true;
			}

		},
		sortable			: true,	
		pageable			: true,
		height				: 250,
		autoBind            : true,
		selectable 			: "row",
		navigatable         : true,
		allowCopy 			: true,
		gridCreated			: function(e){ 
			gridDetalle = e;
		}
		,notEditableColumns	:
			function(row) {				
				if(bModifMvtoMod.oComprobante[0].Residente === "RESIDENTE")
				{
					return ['MontoRetencionGananciaCap'];					
				}
			}
		};		
	
		
	$scope.data.cancelar = function() {
		bcsComunes.ventanaOverlay(idVentana, false);
		bcsComunes.ventanaCerrar(idVentana);		
	};

// Actualiza los registros
$scope.data.regularizarIgk = function() {
	// Acumula tabla de entrada de movimiento comprobante - general
	var DetallesRecarculo = [];
	var grillaDetalle = gridDetalle;
	$.each(grillaDetalle.dataSource.data(),function(key,value) {
			DetallesRecarculo.push({
				IdComprobante : value.IdComprobante,
				IdComprobanteDetalle : value.IdComprobanteDetalle,
				MayorMenorValor : value.MayorMenorValor,
				MayorMenorValorActual : value.MayorMenorValorActual, //Valor actual sin modificar para ingreso del Log
				MontoRetencionGananciaCap : value.MontoRetencionGananciaCap
				
			});
		
	});
	
	// Valida que se hayan seleccionado movimientos
	if (DetallesRecarculo === undefined || DetallesRecarculo.length === 0) {
		$scope.data.btnActivos = true; // Activa botones
		bcsComunes.msg("warning", "warning", $translate.instant('UI_SIN_CAMBIOS'));
		return;
	}

	// parametros de servicio
	var datos = {};
	datos.p_IdEmpresa = $scope.SesionIdEmpresa;
	datos.p_IdUsuario = $scope.SesionIdUsuario;
	datos.p_IdModulo = 'FAC';	
	datos.p_UsaReglasNegocio = 'S';
	datos.p_IdComprobante = bModifMvtoMod.IdComprobante;
	datos.P_COMPROBANTERECALCULAR = DetallesRecarculo;

	bcsComunes.ventanaOverlay(idVentana, true);
	bcsServiciosFacturacion.ModificarRetencionIgk(datos).$promise.then(function(result) {

		if (result.P_OUT_ERRORESCONTADOR == "0" && (result.P_OUT_ADVERTENCIASCONTADOR == "0" || result.P_OUT_ADVERTENCIASCONTADOR === undefined)) {
			
			var mensajeTotal = new Array();
			$.each(result.P_OUT_MENSAJES,function(key,value){
				 var objMensaje = {
						Id : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : $translate.instant(value.descripcion)
				 };
				 mensajeTotal.push(objMensaje);
			});
			result.P_OUT_MENSAJES = mensajeTotal;			
			bcsComunes.ventanaMsg({mensaje: $translate.instant("UI_PROCESO_COMPLETADO"),conflictos:util.parseServerResponse(result)},idVentana);
		}
		else if (result.P_OUT_ERRORESCONTADOR > 0) {
			bcsComunes.ventanaMsg({
				mensaje : $translate.instant(""),
				conflictos : util.parseServerResponse(respuesta)
			},idVentana);
		}
		else if (result.P_OUT_ADVERTENCIASCONTADOR > 0) {
			bcsComunes.ventanaMsg({
				mensaje : $translate.instant(""),
				conflictos : util.parseServerResponse(result)
			},idVentana);
		}
		bcsComunes.ventanaOverlay(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	},
	function(result) {
		bcsComunes.ventanaOverlay(idVentana, false);
		bcsComunes.msg("error", "Error", 'UI_ERROR_MODIFICACION');
		bcsComunes.setConflictos(util.parseServerResponse(result));
	});
	
};

	
});
