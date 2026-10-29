angular.module('ModuloCuentaInversion').controller('CartolaDeMovimientosCtrl',
		function($scope,$timeout,$modal,$state,bcsComunes,$translate,$rootScope,
				bcsServiciosCuentaInversion, bcsServicioPlataforma, bcsServiciosCustodia){

  
	var filters = {};
	$scope.filtros = {};
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams(idVentana);

	
	//SETEO INICIAL
   	//$rootScope.SesionPromise.then(function(){
	$scope.filtros.kngFechaInicio =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtros.kngFechaTermino = util.dateInicioDia($scope.SesionFechaSistema);
	//});
	$scope.filtros.SaldoPorFechaMovimiento 	= true;
	$scope.filtros.SaldoPorFechaEfectiva 	= false;
	$scope.showSeleccionSocioDeNegocio 		= true;
	$scope.showSocioDeNegocio 				= false;
	$scope.activadoCuentaInversion  		= true;
	$scope.activadoEstado           		= true;
	$scope.disabledFechaConsulta  			= false;
	$scope.habilitarBtnLimpiar 				= true;

	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
 
	//COMBOBOX
	$scope.filtros.oConfiguracionCuentaInversion={
			tipo : "combobox-cuenta-inversion", 
			configuracion : {},
			filtros : {
				EstadoCuentaInversion : { "field"   : "IdEstadoCuentaInversion","operator": "eq", "value"   : "ACT" },
				IdCliente             : { "field"   : "IdCliente","operator": "eq", "value"   : 0 },
				IdPortafolio          : { "field"   : "IdPortafolio","operator": "eq", "value"   : 0 }				
				////IdMoneda			  : {"field"    : "IdMoneda" ,   "operator":"eq", "value": ""}
			},
			idActual 		: false,
			seleccionado 	: false,
			idDefecto       : $scope.parametros.IdCuentaInversion || ''
	};
	
   $scope.filtros.oConfiguracionEstadoCuentaInversion={
			tipo : "combobox-estado-cuenta-inversion",
			configuracion : {},
			filtros : {
				Nombre : {field   : "NombreEntidad", operator: "eq", value   : "CTA.CargoAbonoMovimiento"}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
	};
   


   $scope.filtros.oSocioNegocio = {
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
			}
	};
		
	$scope.filtros.oProducto = {
			filtros: {
				IdProductoAgrupador : {"field": "IdProductoAgrupador","operator": "neq", "value"   :  0 }
			}
	};
		
	$scope.filtros.oPortafolio = {
			filtros: {
				IdCliente 			: { "field": "IdCliente","operator": "eq", "value"   :  0 },
				IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
			}
	};

   
	//********************************************
    //Calculo de Saldo Inicial y Saldo Final
    //********************************************
    var successCalculaSaldo = function(result)
    {	       		
   		if (result.P_OUT_ERRORESCONTADOR == "0")
   		{   
   			var IdCuentaInversion = $scope.filtros.oConfiguracionCuentaInversion.seleccionado != false ? $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdCuentaInversion: $scope.parametros.IdCuentaInversion;	
   			$scope.filtros.Simbolo = $scope.parametros.IdMoneda ? $scope.parametros.IdMoneda : $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda;
			$scope.filtros.SaldoInicialDecimal = (util.bcs_formato(0,'GEN','Monto',$scope.filtros.Simbolo,'','')).cantidadDecimales;
   			$scope.filtros.SaldoFinalDecimal = (util.bcs_formato(0,'GEN','Monto',$scope.filtros.Simbolo,'','')).cantidadDecimales;
   			
	   		for (var i = 0; i<result.P_OUT_SALDOINICIALFINAL.length;i++) {
	   			if (result.P_OUT_SALDOINICIALFINAL[i].IdCuentaInversion ==IdCuentaInversion ){
					if ($scope.filtros.SaldoPorFechaMovimiento){
						var SaldoInicial = result.P_OUT_SALDOINICIALFINAL[i].SaldoMovimientoInicial;
						var SaldoFinal = result.P_OUT_SALDOINICIALFINAL[i].SaldoMovimientoFinal;
					}else{
						var SaldoInicial = result.P_OUT_SALDOINICIALFINAL[i].SaldoEfectivoInicial;
						var SaldoFinal = result.P_OUT_SALDOINICIALFINAL[i].SaldoEfectivoFinal;
					}
	   			}
	         }
			
	   		//corregir bug saldo Inicial y final no se actualizaban en todas las busquedas
   			$scope.filtros.SaldoInicialKng = (util.bcs_formato(0,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
   			$scope.filtros.SaldoFinalKng = (util.bcs_formato(0,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
	   		
	   		if ($scope.esHistorico === 0 || !$scope.esHistorico){
	   			//se muestran ambos
	   			$scope.filtros.SaldoInicialKng = (util.bcs_formato(SaldoInicial,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
	   			$scope.filtros.SaldoFinalKng   = (util.bcs_formato(SaldoFinal,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
	   		}else if ($scope.esHistorico === 2){
	   			//se muestra saldo fin
	   			$scope.filtros.SaldoFinalKng   = (util.bcs_formato(SaldoFinal,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
	   		}

   			bcsComunes.setConflictos(util.parseServerResponse(result));
   		} 
    };   	
   	var errorCalculaSaldo = function(result)
   	{	   		
		bcsComunes.msg("error",'Error: ' ,UI_ERROR_INFORMACION_SALDO);
		bcsComunes.setConflictos(util.parseServerResponse(result));
		bcsComunes.log().info(JSON.stringify(result));
   	};
   	var calcularSaldoInicialFinal = function(filtros)
   	{
   		var parametrosConsulta = {};
   		parametrosConsulta.p_IdEmpresa 						= $scope.SesionIdEmpresa;
   		parametrosConsulta.p_IdUsuario 						= $scope.SesionIdUsuario;
   		parametrosConsulta.p_IdModulo 						= 'CTA';
   		parametrosConsulta.p_UsaReglasNegocio 				= 'S';
   		parametrosConsulta.p_IncluyeOtrosSaldos				= 'S';
   		parametrosConsulta.p_IdPais							= $scope.SesionIdPaisSistema;
   		

		
   		parametrosConsulta.p_FechaDesde 					= util.dateSPdate($scope.filtros.kngFechaInicio);
   		parametrosConsulta.p_FechaHasta 					= util.dateSPdate($scope.filtros.kngFechaTermino);
   		parametrosConsulta.p_SoloSaldoNegativo				= 'N';
   		
		if($scope.MostrarProducto)
		{	parametrosConsulta.p_IncluyeProductoApv	 = 'S';
			parametrosConsulta.p_IdCliente			 = Number(filtros.IdCliente)    > 0? Number(filtros.IdCliente):null;
			parametrosConsulta.p_IdPortafolio		 = Number(filtros.IdPortafolio) > 0? Number(filtros.IdPortafolio):null;
			parametrosConsulta.p_IdProductoAgrupador = Number(filtros.IdProductoAgrupador)  > 0? Number(filtros.IdProductoAgrupador):null;
			
			parametrosConsulta.p_CuentaInversionAConsultar =
			[{
				IdEjecutivo			: Number(filtros.IdEjecutivo) > 0? Number(filtros.IdEjecutivo):null,
				IdPortafolio  	  	: Number(filtros.IdPortafolio) > 0? Number(filtros.IdPortafolio):null,
				IdMoneda      	  	: filtros.IdMoneda? filtros.IdMoneda:null
			}];
		}
		else
		{	
			parametrosConsulta.p_CuentaInversionAConsultar		= 
			[{
				IdEjecutivo			: Number(filtros.IdEjecutivo) > 0? Number(filtros.IdEjecutivo):null,																
				IdPortafolio		: Number(filtros.IdPortafolio) > 0? Number(filtros.IdPortafolio):null,
				IdMoneda			: filtros.IdMoneda? filtros.IdMoneda:null
			}];
		}

       	
       	//Llamada a servicio
        bcsServiciosCuentaInversion.ConsultarSaldoCtaInv(parametrosConsulta,successCalculaSaldo,errorCalculaSaldo);       	
   	};
   	 //********************************************
    //Fin Calculo de Saldo Inicial y Saldo Final
    //********************************************
    
   	//Recepción y configuración de datos provenientes desde otra ventana
	if (typeof $scope.parametros.IdPortafolio !== 'undefined'){			
		$scope.showSeleccionSocioDeNegocio = false;
		$scope.showSocioDeNegocio = true;
		$scope.habilitarBtnLimpiar = false;		

		$scope.filtros.socioDeNegocio  = $scope.parametros.NombreCliente;
		$scope.filtros.portafolioSocio = $scope.parametros.NombrePortafolio;

		$scope.filtros.oConfiguracionCuentaInversion.filtros.IdCliente.value = $scope.parametros.IdCliente;
		$scope.filtros.oConfiguracionCuentaInversion.filtros.IdPortafolio.value = $scope.parametros.IdPortafolio;			
		///$scope.filtros.oConfiguracionCuentaInversion.filtros.IdMoneda.value = $scope.parametros.IdMoneda;
		$scope.filtros.oConfiguracionCuentaInversion.filtros.read = true;
		
		$scope.filtros.kngFechaInicio 	= $scope.parametros.FechaConsulta; 
		$scope.filtros.kngFechaTermino 	= $scope.parametros.FechaConsulta;			
		$scope.activadoCuentaInversion  = false;
		$scope.activadoEstado           = false;
		$scope.disabledFechaConsulta  	= true;	
		
		//Datos grillas	
		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;		
		filters.p_IdUsuario         	= $scope.SesionIdUsuario;
		filters.p_IdModulo				= 'CTA';
		filters.p_UsaReglasNegocio		= 'S';
		filters.p_IdCuentaInversion		= Number($scope.parametros.IdCuentaInversion);			
		filters.p_IdCliente				= null;
		filters.p_IdPortafolio			= $scope.parametros.IdPortafolio;
		filters.p_IdMoneda				= $scope.parametros.IdMoneda;
		filters.p_IdEstado				= null;
		filters.p_FechaDesde			= util.dateSPdate($scope.filtros.kngFechaInicio) || '0001-01-01';
		filters.p_FechaHasta			= util.dateSPdate($scope.filtros.kngFechaTermino) || '0001-01-01';
		filters.p_SaldoPorFechaEfectiva	= 'N';
		
		var filtros = {};
		filtros.IdEjecutivo			= null;
		filtros.IdCuentaInversion	= Number($scope.parametros.IdCuentaInversion);
		filtros.IdPortafolio		= $scope.parametros.IdPortafolio;
		filtros.IdMoneda			= $scope.parametros.IdMoneda;

		filtros.IdProductoAgrupador = $scope.parametros.IdProductoAgrupador;
		calcularSaldoInicialFinal(filtros);
	}
	
	$scope.$watch('filtros.oConfiguracionCuentaInversion.seleccionado',function(newval,oldval){
		if (typeof $scope.parametros.IdPortafolio !== 'undefined'){	
			$scope.filtros.oConfiguracionCuentaInversion.idActual = $scope.parametros.IdCuentaInversion;
		}
	});

	

	var dsCartolaDeMovimiento = new bcs.DataSource({
		type : "call",
		definition : {
			serverFiltering 	: false, // Filtrado de datos en el servidor.
			serverPaging 		: false, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			//pageSize 			: 25,								
			callParameters      : filters,
			callTransaction 	: "700139",
			callResultSet 		: "P_OUT_CARTOLADEMOVIMIENTO",
			schema : {
				model : {
					fields : {										
						FechaSistema					: {type : "sysdate"},																		  
						FechaEfectivaMovimiento 		: {type : "sysdate"},
						FechaCertificacion				: {type : "sysdatetime"},
						IdConcepto 						: {type : "string"},
						NombreConcepto					: {type : "string"},
						Cargo 							: {type : "number"},
						Abono 							: {type : "number"},
						SaldoPorFechaEfectiva 			: {type : "number"},
						SaldoPorFechaMovimiento			: {type : "number"},
						SaldoSimulado 					: {type : "number"},
						IdEstado 						: {type : "string"},
						NombreEstado					: {type : "string"},
						NombreTipoComprobante			: {type : "string"},
						CodigoTipoComprobante 			: {type : "string"},
						NombreTipoComprobante 			: {type : "string"},
						FechaComprobante 				: {type : "sysdate"},
						IdTipoOperacion 				: {type : "string"},
						NombreTipoOperacion				: {type : "string"},
						IdOperacion 					: {type : "number"}, // Folio Operación
						FechaOperacion 					: {type : "sysdate"},
						IdTipoProducto 					: {type : "string"},
						NombreTipoProducto				: {type : "string"},
						IdCargoAbonoMovimiento 			: {type : "number"},
						MontoOperacion 					: {type : "number"},
						TipoCambioOperacion 			: {type : "number"},
						FechaVencimientoOperacion 		: {type : "sysdate"},														                  
						EsIngresoManual 				: {type : "string"},
						IdComprobante					: {type : "number"},
						FolioComprobante				: {type : "number"},
						OrigenConcepto					: {type : "string"},
						Descripcion						: {type : "string"},
						EsCargoAbonoPorTraspasoTraducido: {type : "string"},
						IdCargoAbonoRelacionadoTraspaso	: {type : "number"},
						NombreClienteRelacionado		: {type : "string"},
						CodigoPortafolioRelacionado		: {type : "string"},
						EsVentaCorta 					: {type : "string"},
						NemotecnicoOriginal				: {type : "string"},
						MayorMenorValor					: { type : "number"},
						MontoRetencionGananciaCap		: { type : "number"},
						Cantidad						: {type : "number"}
					}
				}
			}
		}
	});
	var gridCartolaMovimiento;
	$scope.confBasicaCartolaMovimiento = {
		dataSource  : dsCartolaDeMovimiento,
		columns 	: [	 
						 {field : "FechaSistema", 						title:"UI_FECHA_MOVIMIENTO",			width: "85px"}
						,{field : "FechaEfectivaMovimiento", 			title:"UI_FECHA_EFECTIVA", 				width: "85px"}
						,{field : "NombreConcepto", 					title:"UI_CONCEPTO", 					width: "250px"}
						,{field : "EsVentaCorta", 						title:"UI_VENTA_CORTA", 				width: "60px"}
						,{field : "NemotecnicoOriginal", 				title:"UI_INSTRUMENTO", 				width: "120px"}

						
						,{field : "Cargo", 								title:"UI_CARGO", 						width: "85px", template : " <label> #:  (util.bcs_formato(Cargo,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "Abono", 								title:"UI_ABONO", 						width: "85px", template : " <label> #:  (util.bcs_formato(Abono,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
						,{field : "SaldoPorFechaEfectiva", 				title:"UI_SALDO", 						width: "85px", hidden: true, template : " <label> #if (IdEstado == 'ANU')  {##=0##} else {##=(util.bcs_formato(SaldoPorFechaEfectiva,'GEN','Monto',IdMoneda,'','')).valor##}# </label>"}
						,{field : "SaldoPorFechaMovimiento",			title:"UI_SALDO", 						width: "85px", hidden: true, template : " <label> #if (IdEstado == 'ANU')  {##=0##} else {##=(util.bcs_formato(SaldoPorFechaMovimiento,'GEN','Monto',IdMoneda,'','')).valor##}#</label>"}		
						,{field : "SaldoSimulado", 						title:"UI_SALDO_SIMULADO", 				width: "85px", hidden: true, template : " <label> #:  (util.bcs_formato(SaldoSimulado,'GEN','Monto',IdMoneda,'','')).valor #</label>"}						

						
						,{field : "EsCargoAbonoPorTraspasoTraducido", 	title:"UI_ES_TRANSFERENCIA", 			width: "60px"}
						,{field : "NombreEstado", 						title:"UI_ESTADO", 						width: "80px"}
						//Datos del Comprobante
						,{field : "NombreTipoComprobante", 				title:"UI_TIPO_COMPROBANTE", 			width: "200px"}						
						,{field : "IdComprobante", 						title:"UI_SECUENCIA_COMPROBANTE", 		width: "85px", template: kendo.template("#if (( IdComprobante == 0  || (IdComprobante == null)))  {##} else {##=IdComprobante##}#")}
						
						,{field : "FolioComprobante", 					title:"UI_FOLIO_COMPROBANTE", 			width: "85px", template: kendo.template("#if ((  (FolioComprobante == null)))  {##} else {##=FolioComprobante##}#")}
						
						,{field : "FechaComprobante", 					title:"UI_FECHA_COMPROBANTE", 			width: "85px"}
						//Datos de la Operación asociada
						,{field : "NombreTipoProducto", 				title:"UI_TIPO_PRODUCTO", 				width: "150px"}
						,{field : "NombreTipoOperacion", 				title:"UI_TIPO_OPERACION", 				width: "200px"}
						,{field : "FechaOperacion", 					title:"UI_FECHA_OPERACION", 			width: "85px"}
						,{field : "FechaVencimientoOperacion", 			title:"UI_FECHA_VENCIMIENTO", 			width: "85px"}
						,{field : "IdOperacion", 						title:"UI_SECUENCIA_OPERACION", 		width: "80px", template: kendo.template("#if (( IdOperacion == 0  || (IdOperacion == null)))  {##} else {##=IdOperacion##}#")}
						,{field : "MontoOperacion", 					title:"UI_MONTO_OPERACION", 			width: "80px", template : kendo.template("#if (( (util.bcs_formato(MontoOperacion,'GEN','Monto',IdMoneda,'','')).valor == 0  || ((util.bcs_formato(MontoOperacion,'GEN','Monto',IdMoneda,'','')).valor == null)))  {##} else {##=(util.bcs_formato(MontoOperacion,'GEN','Monto',IdMoneda,'','')).valor##}#") }
						//Datos del movimiento de CTA							
						,{field : "IdCargoAbonoMovimiento", 			title:"UI_SECUENCIA_MOVIMIENTO", 		width: "80px"}
						,{field : "IdCargoAbonoRelacionadoTraspaso", 	title:"UI_SECUENCIA_TRANSFERENCIA",		width: "80px",	template: kendo.template("#if (( IdCargoAbonoRelacionadoTraspaso == 0  || (IdCargoAbonoRelacionadoTraspaso == null)))  {##} else {##=IdCargoAbonoRelacionadoTraspaso##}#")}			
						,{field : "NombreClienteRelacionado", 			title:"UI_NOMBRE_CLIENTE_RELACIONADO", 	width: "150px",	groupable: true}
						,{field : "CodigoPortafolioRelacionado", 		title:"UI_PORTAFOLIO_RELACIONADO", 		width: "70px", 	groupable: true}						
						,{field : "FechaCertificacion", 				title:"UI_FECHA_CERTIFICACION", 		width: "100px"}
						,{field : "Descripcion", 						title:"UI_DESCRIPCION", 				width: "250px"}
						,{field : "EsIngresoManual", 					title:"UI_INGRESO_MANUAL", 				width: "80px"}
						//Ganancia Capital
						,{field: "MayorMenorValor", title: "UI_MAYOR_MENOR_VALOR", width: "120px"}
						,{field: "MontoRetencionGananciaCap", title: "UI_MONTO_RETENCION_GANANCIA_CAP", width: "120px"}
						,{field: "Cantidad",							title: "UI_CANTIDAD", 					width: "85px", template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>"}
					  ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: false,
		allowCopy	: { delimeter: "\t" },
		height 		: 490,
		editable	: false,
		selectable 	: "multiple row",
		autoBind 	: $scope.parametros.autoBind || false,
		userSettings : true,
		gridCreated : function(g) { 
						gridCartolaMovimiento = g; 
						g.showColumn("SaldoPorFechaMovimiento");
						g.hideColumn("SaldoPorFechaEfectiva");
		},
		rowColorBackground	: function(row) {								
								if (row.IdEstado == 'ANU')
									return "red";
								return "";
							  },
		dataBound: function(e) {
			console.log(e);
		    console.log('first row: ',dsCartolaDeMovimiento.at(0));
		    let dsRow = dsCartolaDeMovimiento.at(0);
		    
		    //TODO cuando CODMOVTO=saldoini o saldofin -> obtener este saldo y luego remover fila del ds.
		    if (dsRow && dsRow.Descripcion === "Saldo Inicial"){
		    	$scope.saldoInicialHistorico = dsRow.SaldoSimulado;
		    	dsCartolaDeMovimiento.remove(dsRow);
		    	console.log('saldo inicial hist',$scope.saldoInicialHistorico);
		    	$scope.filtros.SaldoInicialKng = (util.bcs_formato($scope.saldoInicialHistorico,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
	   			
		    }
		    
		    dsRow = dsCartolaDeMovimiento.at(0);
		    if (dsRow && dsRow.Descripcion === "Saldo Final"){
		    	$scope.saldoFinalHistorico = dsRow.SaldoSimulado;
		    	dsCartolaDeMovimiento.remove(dsRow);
		    	console.log('saldo final hist',$scope.saldoFinalHistorico);
		    	$scope.filtros.SaldoFinalKng   = (util.bcs_formato($scope.saldoFinalHistorico,'GEN','Monto',$scope.filtros.Simbolo,'','')).valor;
		    }
		}
	};
	$scope.detalleComprobanteFacturacion = function(datos){
		if (datos[0].IdModulo == 'FAC')
			//bcsComunes.ventanaAbrir("DetalleComprobanteFacturacion", {RegistroSel: datos[0]}, false, false, idVentana );
		    bcsComunes.ventanaAbrir("DetalleComprobanteFacturacion", {IdComprobante: datos[0].IdComprobante, IdTipoComprobante: datos[0].IdTipoComprobante},  false, false, idVentana );
		if (datos[0].IdModulo == 'TES')
			bcsComunes.ventanaAbrir("DetalleComprobanteTesoreria", {IdComprobante: datos[0].IdComprobante}, false, false, idVentana );
		if (datos[0].IdModulo == 'FMM')
			bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate: datos[0].IdComprobante}, false, false, idVentana );			
// FALTA CUENTA DE INVERSIÓN.			
//		if (datos[0].IdModulo == 'CTA')
//			bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate: datos[0].IdComprobante}, false, false, idVentana );			
	};
	
	//////////////////// FIN GRILLA
	
	$scope.$watch('filtros.SaldoPorFechaMovimiento',function(newval,oldval){
		switch (newval){
			case true:  $scope.filtros.SaldoPorFechaEfectiva = false; break;					
			case false: $scope.filtros.SaldoPorFechaEfectiva = true;  break; 
		}
		dsCartolaDeMovimiento.removeAll();
	});	
	$scope.$watch('filtros.SaldoPorFechaEfectiva',function(newval,oldval){
		switch (newval){
			case true:  $scope.filtros.SaldoPorFechaMovimiento = false; break;					
			case false: $scope.filtros.SaldoPorFechaMovimiento = true;  break;  
		}
		dsCartolaDeMovimiento.removeAll();
	});	


	$scope.buscarMovimientos = function() {
		var params = {};
		params.p_IdEmpresa	= $scope.SesionIdEmpresa; 
		params.p_IdUsuario	= $scope.SesionIdUsuario;
		params.p_IdModulo	= "AGE";
		params.p_ListaParametros = [{moduloParametro: "PLATAFORMA", nombreParametro : "PLA.FechaGoLive" }];
		
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		
		bcsServiciosCustodia.ConsultarFechaCorteHistorica(params,
			function(response){		
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				if (response.P_OUT_ERRORESCONTADOR === 0){
					var fechaCorte = response.P_OUT_LISTA_PARAMETROS[0].valorParametro;
					//$scope.fechaGoLive = fechaCorte;
					//esta definida una fecha de corte, entonces va a SIGACRM
					if (fechaCorte != ""){
						if (util.dateSPdate($scope.filtros.kngFechaInicio) < fechaCorte && util.dateSPdate($scope.filtros.kngFechaTermino) < fechaCorte) $scope.esHistorico = 1;
						else if (util.dateSPdate($scope.filtros.kngFechaInicio) < fechaCorte && util.dateSPdate($scope.filtros.kngFechaTermino) >= fechaCorte) $scope.esHistorico = 2;
						else $scope.esHistorico = 0;
					}
					$scope.buscarActual();
				}
				else //si hay error al obtener parametro el motivo principal es que no esta definido por lo que asume realizar la busqueda normal
				{
					$scope.buscarActual();
				}	

			},
			function(response){
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(response)
				},bcsComunes.ventanaWinId($scope));
				$scope.buscarActual();
			}
		);
	};
	
	$scope.buscarActual = function (historico=false){
		
		var IdPortafolio			= null;
		var IdProductoAgrupador		= 0;
		
		
		if($scope.MostrarProducto)
		{
			if($scope.filtros.oPortafolio.seleccionado !== undefined)
			{
				IdPortafolio = Number($scope.filtros.oPortafolio.seleccionado.IdPortafolio);
			} 
			
			if($scope.filtros.oProducto.seleccionado !== undefined){
				if(Number($scope.filtros.oProducto.seleccionado.IdProductoAgrupador) === 9999)
				{
					IdProductoAgrupador = 0;
				}
				else
				{
					IdProductoAgrupador = Number($scope.filtros.oProducto.seleccionado.IdProductoAgrupador);	
				}
			}
			
		}
		else
		{
			IdPortafolio = Number($scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdPortafolio);
		}
		
		
		var filtros = {};
		filtros.IdEjecutivo			= null;
		filtros.IdPortafolio		= IdPortafolio;
		filtros.IdProductoAgrupador = IdProductoAgrupador;
		filtros.IdMoneda			= $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda; //IdMoneda;
		calcularSaldoInicialFinal(filtros);

		if ( _.isUndefined(gridCartolaMovimiento) ) return;
		
		//Campos a mostrar
		if ($scope.filtros.VerSaldoSimulado)
			gridCartolaMovimiento.showColumn("SaldoSimulado");
		else
			gridCartolaMovimiento.hideColumn("SaldoSimulado");
		
		if ($scope.filtros.SaldoPorFechaMovimiento){
			gridCartolaMovimiento.showColumn("SaldoPorFechaMovimiento");
			gridCartolaMovimiento.hideColumn("SaldoPorFechaEfectiva");
		}			
		else{
			gridCartolaMovimiento.hideColumn("SaldoPorFechaMovimiento");
			gridCartolaMovimiento.showColumn("SaldoPorFechaEfectiva");
		}
		
		//Datos grillas	
		filters.p_IdEmpresa 			= $scope.SesionIdEmpresa;		
		filters.p_IdUsuario         	= $scope.SesionIdUsuario;
		filters.p_IdModulo				= 'CTA';
		filters.p_UsaReglasNegocio		= 'S';		
		filters.p_IdCuentaInversion		= Number($scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdCuentaInversion);		
		
		filters.p_IdCliente				= null;
		filters.p_IdPortafolio			= Number($scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdPortafolio);
		filters.p_IdMoneda				= $scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdMoneda;
		filters.p_IdEstado				= $scope.filtros.oConfiguracionEstadoCuentaInversion.seleccionado.IdEstado ? $scope.filtros.oConfiguracionEstadoCuentaInversion.seleccionado.IdEstado : null;
		filters.p_FechaDesde			= util.dateSPdate($scope.filtros.kngFechaInicio) || '0001-01-01';
		filters.p_FechaHasta			= util.dateSPdate($scope.filtros.kngFechaTermino) || '0001-01-01';
		filters.p_SaldoPorFechaEfectiva	= $scope.filtros.SaldoPorFechaMovimiento ? 'N' : 'S';
		filters.p_IncluyeSaldo			= 'S';
		
		
		dsCartolaDeMovimiento.setCallParameters(filters);
		
		
	};
	
	$scope.buscarHistorico = function() {
		var params = {};
		params.p_idEmpresa 		= $scope.SesionIdEmpresa;
		params.p_opcionMenu		= "showHistorialMovimientos";
		params.p_canal			= "AGENTE";
		params.p_idPortafolio	= Number($scope.filtros.oConfiguracionCuentaInversion.seleccionado.IdPortafolio);
		
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		bcsServiciosCustodia.ConsultarSigaCrmHistorico(params,
			function(response){		
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				if (response.P_OUT_ERRORESCONTADOR == 0){
					$scope.urlSigaCRM = response.P_OUT_SALIDA[0];
					console.log("URL: " + $scope.urlSigaCRM);		
					window.open($scope.urlSigaCRM, '_blank');
				}
				else 
				{
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(response)
					},bcsComunes.ventanaWinId($scope));
					
				}
			},
			function(response){
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(response)
				},bcsComunes.ventanaWinId($scope));
			}
		);
	};
	
	$scope.limpiarFiltros = function()
	{	
		$scope.filtros.oConfiguracionCuentaInversion.idActual 	= 0;
		$scope.filtros.oConfiguracionEstadoCuentaInversion.idActual = 0;
		$scope.filtros.kngFechaInicio 	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.kngFechaTermino 	= util.dateInicioDia($scope.SesionFechaSistema);
		
		if($scope.MostrarProducto)
		{
			$scope.filtros.oSocioNegocio.idActual						= false;
			$scope.filtros.oProducto.idActual							= 9999;
			$scope.filtros.oPortafolio.idActual							= false;			
		}
		else
		{
			$scope.filtros.oConfiguracionPortafolio.idActual		= false;			
		}
		
		$scope.filtros.Simbolo = '';
		$scope.filtros.SaldoInicialKng = 0;
		$scope.filtros.SaldoFinalKng  = 0;		
		$scope.filtros.VerSaldoSimulado = false;

		dsCartolaDeMovimiento.removeAll();
	};

	

		
		
});
