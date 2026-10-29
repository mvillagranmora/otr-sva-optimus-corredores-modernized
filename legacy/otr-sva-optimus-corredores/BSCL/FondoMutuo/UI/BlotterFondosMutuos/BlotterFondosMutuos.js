angular.module('ModuloFFM').controller('BlotterFFMCtrl',
function($scope,$translate,bcsComunes,bcsConfiguracionFFM,bcsServicioFondosMutuos,bcsServicioPlataforma, bcsAlerta,OPCIONES_BCSALERTA)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.formData = {};
	$scope.filtros = {IdCliente: { "field": "IdCliente","operator": "eq", "value"   :  undefined}};
	$scope.winID=bcsComunes.ventanaWinId($scope);
	$scope.dataTemplate = {};
	
	$scope.MostrarProducto = BCS_CONFIGURACION.PLATAFORMA["APV.Habilitado"] === 'S' ? true : false;
	
	var dataSchema = bcsConfiguracionFFM.getSchemaSolicitudAporteRescate();
	dataSchema.model.fields.ISIN			= { type : "string"};
	dataSchema.model.fields.CodigoUsuario	= { type : "string"};
	dataSchema.model.fields.EntidadOrigen	= { type : "string"};
	dataSchema.model.fields.EntidadDestino	= { type : "string"};
	dataSchema.model.fields.PeriodoPago		= { type : "string"};
	dataSchema.model.fields.CalculoMontoCuotaUI		= { type : "string"};
	
	var setFooterNumeroCuotas = function(campo, campoDec, dsRef) {
		if(!dsRef) return '';
		var agg = dsRef.aggregates();
		if (!agg || !agg[campo]) return '';
		var decimales = agg[campoDec] ? agg[campoDec].max : 0;
		return util.formateaNumero(agg[campo].sum, decimales);
	};
	
	var setTemplateNumeroCuotas = function(dataItem, dsRef){
		if(!dsRef) return '';
		var agg = dsRef.aggregates();
		var decimalesMax = (agg && agg.DecimalesCuota) ? agg.DecimalesCuota.max : 0;
		var usaDecimalesMax = decimalesMax > 0;
		var decimales = usaDecimalesMax ? decimalesMax : dataItem.DecimalesCuota;
		return util.formateaNumero(dataItem.NumeroCuotas, decimales);
	};
	
	var ds = new bcs.DataSource({ 
		type : "call",
		definition : {
			callTransaction	: "700189",
			serverFiltering : false, 
			serverPaging : false, 
			serverSorting : false,
			callParameters  : {},
			callResultSet   : "P_OUT_RESULTADO",
			sort: { field: "IdSolicitudAporteRescate", dir: "desc" },
			pageSize : 200,
			schema : dataSchema,
			aggregate: [
	            { field: "NumeroCuotas", aggregate: "sum" },
	            { field: "DecimalesCuota", aggregate: "max" },
	            { field: "MontoMonedaSistema", aggregate: "sum"},
				{ field: "MontoMonedaSistemaInicial", aggregate: "sum"}
	        ]
		},			
	});

	$scope.defaultOptions = {
      		userSettings:true,
			sortable: true,
      		reorderable: true,
      		filterable: true,
			groupable: true,
			pageable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
      		autoBind: false,
      		scrollable: true,
      		height:450,
	  		dataSource: ds,
		
			columns: [
			           	bcsConfiguracionFFM.getConfiguracionColumna("IdTipoCanalIngreso",{ groupable: true})
			           	,bcsConfiguracionFFM.getConfiguracionColumna("Modificaciones",{ groupable: true})
					  	,bcsConfiguracionFFM.getConfiguracionColumna("FolioContrato",{ groupable: true})
					  	,bcsConfiguracionFFM.getConfiguracionColumna("FolioContratoOperacion",{ groupable: true})
			          	,bcsConfiguracionFFM.getConfiguracionColumna("TipoContratoOperacion",{ groupable: true})
			          	,bcsConfiguracionFFM.getConfiguracionColumna("IdSolicitudAporteRescate",{ groupable: true})
			          	,bcsConfiguracionFFM.getConfiguracionColumna("FechaIngresoRegistro",{ groupable: true})
			          	,bcsConfiguracionFFM.getConfiguracionColumna("FechaAceptacion",{ groupable: true})
			          	,bcsConfiguracionFFM.getConfiguracionColumna("FechaCertificacion",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("FechaLiquidacion",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("DescripcionTipoMovimiento",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("DescripcionTipoSolicitud",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreEstado",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("EstadoLiquidacion",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("FolioCartaLiquidacion",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("Emisor",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("IdFondo",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreFondoReal",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("IdSerieFondo",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreSerieFondo",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("DocumentoIdentidad",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreCompleto",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("DescripcionPortafolio",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("IdMonedaFondo", {groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("ValorCuota",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("FechaValorCuotaFondo", {groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("NumeroCuotas",{aggregates: ["sum"],
							template: function(dataItem) {	return "<label>" + setTemplateNumeroCuotas(dataItem, ds) + "</label>";},
							groupFooterTemplate: function(e) { return setFooterNumeroCuotas('NumeroCuotas', 'DecimalesCuota', ds);},
							footerTemplate: function(e) { return setFooterNumeroCuotas('NumeroCuotas', 'DecimalesCuota', ds);},
							footerAttributes: {style: "text-align: right; padding-right:0;" }})
						,bcsConfiguracionFFM.getConfiguracionColumna("Monto",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("MontoRetencion")
						,bcsConfiguracionFFM.getConfiguracionColumna("MontoComision")
						,bcsConfiguracionFFM.getConfiguracionColumna("IdMoneda",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("Paridad",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("CostoMonedaLocal")
						,bcsConfiguracionFFM.getConfiguracionColumna("CostoUF")
						,bcsConfiguracionFFM.getConfiguracionColumna("MontoMonedaSistema",{aggregates: ["sum"],groupFooterTemplate:" #=(util.bcs_formato(sum,'FMM','Monto','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #",  footerTemplate:" #:  (util.bcs_formato(sum,'FMM','Monto','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}})
						,bcsConfiguracionFFM.getConfiguracionColumna("MontoMonedaSistemaInicial",{aggregates: ["sum"],groupFooterTemplate:" #=(util.bcs_formato(sum,'FMM','Monto','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #",  footerTemplate:" #:  (util.bcs_formato(sum,'FMM','Monto','"+ $scope.SesionIdMonedaSistema +"', '','')).valor #", footerAttributes:{style:"text-align: right; padding-right:0;"}})
						,bcsConfiguracionFFM.getConfiguracionColumna("TextoPreferenciaPago",{ groupable: true}) 
						,bcsConfiguracionFFM.getConfiguracionColumna("Spread",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("RentabilidadReal")
						,bcsConfiguracionFFM.getConfiguracionColumna("IdSerieFondoDestino",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("IdEmisorDestino",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("Switch",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("Producto",{ groupable: true})
						,bcsConfiguracionFFM.getConfiguracionColumna("IdIngresoEgreso")
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreEstadoTesoreria")
						,bcsConfiguracionFFM.getConfiguracionColumna("FechaLiquidacionTesoreria")
						,bcsConfiguracionFFM.getConfiguracionColumna("IdOperacionSpot")
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreEstadoSpot")
						,bcsConfiguracionFFM.getConfiguracionColumna("NombreTipoOperacionSpot")
						,bcsConfiguracionFFM.getConfiguracionColumna("IdOmnibus")
						,bcsConfiguracionFFM.getConfiguracionColumna("IdDividendo")
						,bcsConfiguracionFFM.getConfiguracionColumna("IdAgrupador")
						,bcsConfiguracionFFM.getConfiguracionColumna("RegimenTributario")
						,bcsConfiguracionFFM.getConfiguracionColumna("FolioCarteraAsociadoComision")
						,bcsConfiguracionFFM.getConfiguracionColumna("CodigoUsuario")
						,bcsConfiguracionFFM.getConfiguracionColumna("ISIN")
						,EntidadOrigen	= {field: "EntidadOrigen",   			title: $translate.instant('UI_ENTIDAD_ORIGEN'),		 	    			width: "100px", align: "left"}
						,EntidadDestino = {field: "EntidadDestino",   			title: $translate.instant('UI_ENTIDAD_DESTINO'),		 	    		width: "100px", align: "left"}
						,PeriodoPago	= {field: "PeriodoPago",   				title: $translate.instant('UI_PERIODO_PAGO'),		 	    			width: "100px", align: "left"}
						,TipoRescate	= {field: "CalculoMontoCuotaUI",   			title: $translate.instant('UI_TIPO_RESCATE'),		 	    			width: "100px", align: "left"}
			]
	};
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.formData.filtrosFondosMutuos = {
		IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value"   :  "INFM"}
	};
	
	$scope.formData.filtrosEmisor = {
		IndicadorAGF : { "field"   : "IndicadorAGF","operator": "eq", "value"   :  "S"}
	};
	
	$scope.oConfiguracionTipoSolicitud = {
		tipo : "combobox-operacion-ffmm",
		configuracion : {},
		idDefecto : false,
		filtros: {
			TipoSolicitud : {"field": "TipoSolicitud", "operator": "eq", "value" :""}
		}
	};
	
	$scope.oConfiguracionOperacion = {
		tipo : "combobox-tipo-operacion-ffmm",
		configuracion : {},
		idDefecto : false,
		filtros : {
			TieneCertificacion : { "field": "TieneCertificacion","operator": "eq", "value"   :  undefined },
			IdTipoMovimientoFondoMutuo : { "field" : "IdTipoMovimientoFondoMutuo",  "operator": "eq",  "value" : "" },
			CodigoConcepto : { "field" : "IndicadorAporteRescate",  "operator": "eq",  "value" : "" }
		}
	};
	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.oProducto = {
		idActual : false,
		seleccionado : false,			
	};
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  0 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  0 }
		},
	};
	
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
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/

	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	/*
	var dataSourceConfiguracion = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1556",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'APV.Habilitado' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceConfiguracion.fetch(function(){
		if(dataSourceConfiguracion.data().length > 0){
			if(dataSourceConfiguracion.data()[0].Valor === 'S'){
				$scope.MostrarProducto = true;
			}
		}
 	});
 	*/
	/*PRONET IT - parametro para saber si tiene portafolio apv habilitado*/
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	$scope.buscarSolicitudes = function(){

		var resp = util.validarRangoFecha($scope.filtros.fechaDesde, $scope.filtros.fechaHasta,30);
		if(resp.estado){
			bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
			return;
		}
		
		var filtros = {
			p_IdEmpresa : $scope.SesionIdEmpresa,		
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : 'FMM'
		};
		
		filtros.p_FechaAceptacionDesde = $scope.filtros.fechaDesde ? util.dateSPdate($scope.filtros.fechaDesde) : $scope.SesionFechaSistema;
		filtros.p_FechaAceptacionHasta = $scope.filtros.fechaHasta ? util.dateSPdate($scope.filtros.fechaHasta) : $scope.SesionFechaSistema;
		filtros.p_IdTipoMovimientoFondoMutuo = $scope.filtros.IdTipoMovimientoFondoMutuo || '';
		filtros.p_Estado = $scope.filtros.IdEstado || '';
		filtros.p_IdEmisor = $scope.filtros.IdEmisor || '';
		filtros.p_IdSerieFondo = $scope.filtros.FondoMutuo ? $scope.filtros.FondoMutuo.IdSerieFondo : '';
		if($scope.MostrarProducto){
			filtros.p_IdCliente = $scope.oSocioNegocio.seleccionado ? $scope.oSocioNegocio.seleccionado.IdCliente : 0;
			filtros.p_IdProductoAgrupador = $scope.oProducto.idActual || 0;
			filtros.p_IdPortafolio = $scope.oPortafolio.idActual || 0;
		}else{
			filtros.p_IdCliente = $scope.oClientePortafolio.seleccionado ? $scope.oClientePortafolio.seleccionado.IdCliente : 0;
			filtros.p_IdPortafolio = $scope.oClientePortafolio.seleccionado ? $scope.oClientePortafolio.seleccionado.IdPortafolio : 0;
		}
		
		filtros.p_TipoSolicitud = $scope.filtros.TipoSolicitud || '';
		filtros.p_IdMoneda = $scope.filtros.IdMoneda || '';
		
		ds.setCallParameters(filtros);
	}
	
	$scope.ingresarFFM = function()
	{
		bcsComunes.ventanaAbrir("IngresarFFM", {} );
	};
	
	$scope.certificarMasivoFFM = function()
	{
		bcsComunes.ventanaAbrir("CertificarFFM", {TipoMovimiento: $scope.filtros.IdTipoMovimientoFondoMutuo} );
	};
	
	$scope.abrirDetalle = function(items)
	{
		bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate : items[0].IdSolicitudAporteRescate} );
  	};
	
	$scope.blotterCambioSerieFFM = function()
	{
		bcsComunes.ventanaAbrir("BlotterCambioSerieFFM", {} );	
	};
	
	$scope.blotterTraspasosFFM = function()
	{
		bcsComunes.ventanaAbrir("BlotterTraspasosFFM", {} );	
	};
	
	$scope.certificarFFM = function(solicitudesTemp)
	{
		if(!solicitudesTemp || solicitudesTemp.length == 0) return;
		bcsComunes.ventanaOverlay($scope.winID,true);
		var datos = {
			p_IdEmpresa   : $scope.SesionIdEmpresa,
			p_IdUsuario   : $scope.SesionIdUsuario,
			p_IdModulo 	  : 'FMM',
			p_SimularOperacion : 'S',
			p_UsaReglasNegocio : 'S',
			p_FechaCertificacion : $scope.SesionFechaSistema,
			p_Solicitudes : [],
			p_Filtros : []
		}
		
		$.each(solicitudesTemp,function(key,sol){
  			datos.p_Solicitudes.push({IdSolicitudAporteRescate : sol.IdSolicitudAporteRescate});
  		});
	
		bcsServicioFondosMutuos.CertificarSolicitudAporteRescate(datos).$promise.then(function(respuesta){
			var id = bcsComunes.ventanaAbrir("ConfirmarCertificarFFM", {datosCertificarAporteRescate: datos, respuesta: respuesta, masivo: false}, false, false, $scope.winID );
			buscarLuegoDeCerrarVentana(id);
			$scope.datos.kNgModel = undefined;
		},function(respuesta){
			var id = bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_CERTIFICACION_APORTE_RESCATE"),conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			buscarLuegoDeCerrarVentana(id);
  			$scope.datos.kNgModel = undefined;
		});
	};
	
	$scope.abrirDetalle = function (items) {
		bcsComunes.ventanaAbrir("DetalleSolicitudAporteRescate", {IdSolicitudAporteRescate : items[0].IdSolicitudAporteRescate} );
  	};
	
	$scope.anularFFM = function(solicitudesTemp,idEstado){
		if(!solicitudesTemp || solicitudesTemp.length == 0) return;
		
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_ANULACION_APORTE_RESCATE'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'FMM',
		  			p_Solicitudes : [],
	    		};
		    	$.each(solicitudesTemp,function(key,sol){
		    
		    		datos.p_Solicitudes.push({IdSolicitudAporteRescate: sol.IdSolicitudAporteRescate, FolioContrato: sol.FolioContrato, IdCliente: sol.IdCliente, Estado: idEstado});
			  		
		  		});
		    	bcsComunes.ventanaOverlay($scope.winID,true);
		    	bcsServicioFondosMutuos.AnularSolicitudAporteRescateMasiva(datos).$promise.then(function(result){
		    		if(result.P_OUT_ERRORESCONTADOR == 0){
		    			var operaciones = [];
						$.each(result.P_SOLICITUDES,function(key,value) {
							operaciones.push({id: value.IdSolicitudAporteRescate , resultado: value.Descripcion, mensaje: ""});
						});

			     		var id = bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
							tituloVentana : $translate.instant("UI_INFORMACION"), 
							nombreEntidad : $translate.instant("UI_SECUENCIA"), 
							operaciones : operaciones   
						});
						
			    		$scope.buscarSolicitudes();
			    		bcsComunes.ventanaOverlay($scope.winID,false);
			    		$scope.datos.kNgModel = undefined;
		    		}
		    		else{
		    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_OPERACION_ERROR"),conflictos:util.parseServerResponse(result)},$scope.winID);
		    		}
		    	},function(result){
		    		console.log(result);
		    		$scope.buscarSolicitudes();
		    		bcsComunes.ventanaOverlay($scope.winID,false);
		    		$scope.datos.kNgModel = undefined;
		    	});
			}
			
		});
			
	};
	
	$scope.blotterCambioSerieFFM = function(){
		bcsComunes.ventanaAbrir("BlotterCambioSerieFFM", {} );
	};
	
	

	//Expotacion massiva Batch que genera un acrhivo CSV con solicitudes de aporte rescate. Hora de ejecucion de este proceso esta progamado por parametro
	$scope.exportacionMasivaBatch = function(){
		
		if($scope.MostrarProducto)
		{
			var parametros = {
		  			p_FechaAceptacionDesde 		 : $scope.filtros.fechaDesde || '',
		  			p_FechaAceptacionHasta 		 : $scope.filtros.fechaHasta || '',
		  			p_IdTipoMovimientoFondoMutuo : $scope.filtros.IdTipoMovimientoFondoMutuo || '',
		  			p_Estado 					 : $scope.filtros.IdEstado || '',
		  			p_IdEmisor 					 : $scope.filtros.IdEmisor || '',
		  			p_IdSerieFondo 				 : $scope.filtros.FondoMutuo ? $scope.filtros.FondoMutuo.IdSerieFondo : '',		  					
	  				p_IdCliente 			 	 : $scope.oSocioNegocio.seleccionado ? $scope.oSocioNegocio.seleccionado.IdCliente : 0,
	  				p_IdProductoAgrupador 	 	 : $scope.oProducto.idActual || 0,
	  				p_IdPortafolio 			 	 : $scope.oPortafolio.idActual || 0,	  				
		  			p_TipoSolicitud 			 : $scope.filtros.TipoSolicitud || '',
		  			p_IdMoneda 					 : $scope.filtros.IdMoneda || ''
			};
		}
		else
		{
			var parametros = {
		  			p_FechaAceptacionDesde 		 : $scope.filtros.fechaDesde || '',
		  			p_FechaAceptacionHasta 		 : $scope.filtros.fechaHasta || '',
		  			p_IdTipoMovimientoFondoMutuo : $scope.filtros.IdTipoMovimientoFondoMutuo || '',
		  			p_Estado 					 : $scope.filtros.IdEstado || '',
		  			p_IdEmisor 					 : $scope.filtros.IdEmisor || '',
		  			p_IdSerieFondo 				 : $scope.filtros.FondoMutuo ? $scope.filtros.FondoMutuo.IdSerieFondo : '',		  					
	  				p_IdCliente 			 	 : $scope.oClientePortafolio.seleccionado ? $scope.oClientePortafolio.seleccionado.IdCliente : 0,
	  				p_IdPortafolio 			 	 : $scope.oClientePortafolio.seleccionado ? $scope.oClientePortafolio.seleccionado.IdPortafolio : 0,
	  				p_IdProductoAgrupador 	 	 : 0,
		  			p_TipoSolicitud 			 : $scope.filtros.TipoSolicitud || '',
		  			p_IdMoneda 					 : $scope.filtros.IdMoneda || ''
			};
		}

		
		bcsComunes.ventanaAbrir("ExportarBatchFFM", parametros );		
	};
	
	
	
	$scope.verComprobante = function(data) {
		var solicitud = data[0];
		var tipoComprobante = '';
		var tipoMovimiento = solicitud.IdTipoMovimientoFondoMutuo;
		if(tipoMovimiento == 'RESTRANSF' || tipoMovimiento == 'RESTRASP' || tipoMovimiento == 'APODIV' || tipoMovimiento == 'RESCAM' || tipoMovimiento == 'RESCOM' || tipoMovimiento == 'APORESPRO') return;
		if(tipoMovimiento == 'RESNORM') tipoComprobante = 'CompResc';
		if(tipoMovimiento == 'APONORM'||tipoMovimiento == 'APOTRASP') tipoComprobante = 'CompApor';
		if(tipoMovimiento == 'APOOVER') tipoComprobante = 'CompOver';
		if(tipoMovimiento == 'APOREINV') tipoComprobante = 'ComApRei';
		if(tipoMovimiento == 'RESREINV') tipoComprobante = 'ComReRei';
		var resultadoSolicitud = {
			    SecuenciaOperacion              : solicitud.IdSolicitudAporteRescate,
				IdOperacion						: solicitud.IdTipoMovimientoFondoMutuo,
				FondoMutuo            			: solicitud.IdSerieFondo,
				TipoFondo 						: solicitud.IdTipoMovimientoFondoMutuo,
				FechaAporte  			 		: util.formatoDate(solicitud.FechaAceptacion),
				MontoInicial	 				: solicitud.CostoMonedaLocal,
				NumeroCuotas			 		: solicitud.NumeroCuotas,
				CostoUF                         : solicitud.CostoUF,
				IdEmisor                        : solicitud.IdEmisorOrigen,
				MontoAporteOrigen               : solicitud.CostoMonedaLocal,
				esMLT                           : solicitud.IndicadorMLT === 'S' ? 'SI' : 'NO',
				FechaOrigenInversion            : util.formatoDate(solicitud.FechaOriginalInversion),
				DocumentoIdentidad              : solicitud.DocumentoIdentidad,
				CodigoPortafolio                : solicitud.CodigoPortafolio,
				NombreCliente                   : solicitud.NombreCompleto,
				FechaLiquidacion                : util.formatoDate(solicitud.FechaLiquidacion),
				FechaCertificacion              : util.formatoDate(solicitud.FechaCertificacion),
				MontoTransferencia              : solicitud.MontoMovimientoAsociado,
				NumeroCuotaTransferencia        : solicitud.NumeroCuotasMovimientoAsociado,
				IdEmisorOrigen                  : solicitud.IdEmisorOrigen,
				IdSerieFondoDestino             : solicitud.IdSerieFondoDestino,
				IdEmisorDestino                 : solicitud.IdEmisorDestino,
				IndicadorMLT                    : solicitud.IndicadorMLT,
 		};
		var filterTemplate = [ { field: "IdTipoDocumento",operator:"eq", value: tipoComprobante },
		                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
		                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
		bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.query({ filter: filterTemplate }).then(function(){
			if(bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view().length > 0) {
				$scope.dataTemplate.TipoReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].TipoArchivo;
				$scope.dataTemplate.IdReporte		= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].IdArchivo;
				$scope.dataTemplate.NombreReporte	= bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].NombreArchivo;

				var parametros =  { "p_IdEmpresa"				: $scope.$root.SesionIdEmpresa,
									"p_IdUsuario"				: $scope.SesionIdUsuario,
									"p_IdModulo"		: 'FMM',
									"p_IdSolicitudAporteRescate" : solicitud.IdSolicitudAporteRescate,
									
		                            "$schema" : [{ "field" : "LogoCorredora", "type" : "blob" }]
									  };
	
				$scope.dataTemplate.data= {  "serviceId"   :	bcsConfiguracionFFM.ConsultaArchivoFondoMutuo.view()[0].ServicioArchivo
										   , "serviceData" :	parametros
										  };
				
				$scope.dataTemplate.parameters= [{
													"type" : "pdf417",
													"name" : "pdf417",
													"data" :  bcsConfiguracionFFM.generarPdf417(resultadoSolicitud)
												}];
				var subrep = new Array();
				$scope.dataTemplate.subReports=subrep;
				
				bcsConfiguracionFFM.getReport($scope.dataTemplate);
			}
		});
	};
	
	$scope.crearOmnibus = function(solicitudes){
		if(!solicitudes || solicitudes.length == 0) return;
		var datos = {
			p_IdEmpresa   : $scope.SesionIdEmpresa,
  			p_IdUsuario   : $scope.SesionIdUsuario,
  			p_IdModulo 	  : 'FMM',
  			p_Solicitudes : [],	
		}
		
		$.each(solicitudes,function(key,sol){
			if(sol.IdSolicitudAporteRescateRelacionado > 0 && sol.IdTipoMovimientoFondoMutuo == "RESCNN" ){
				if( !solicitudes.some(solicitud => solicitud.IdSolicitudAporteRescate == sol.IdSolicitudAporteRescateRelacionado) ) { 
					datos.p_Solicitudes.push({IdSolicitudAporteRescate : sol.IdSolicitudAporteRescateRelacionado, FolioContrato: 0});
				}				
			} else {
				datos.p_Solicitudes.push({IdSolicitudAporteRescate : sol.IdSolicitudAporteRescate, FolioContrato: 0});
			}
  		});
		
    		bcsComunes.ventanaOverlay($scope.winID,true);
	    	bcsServicioFondosMutuos.GenerarDataReporteTRXI(datos).$promise.then(function(result){
	    		if(result.P_OUT_ERRORESCONTADOR === 0){
	    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREAR_OMNIBUS_OK"),conflictos:util.parseServerResponse(result)},$scope.winID);
	    		}else{
	    			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_CREAR_OMNIBUS_ERROR"),conflictos:util.parseServerResponse(result)},$scope.winID);
	    		}
	    		$scope.buscarSolicitudes();
	    		bcsComunes.ventanaOverlay($scope.winID,false);
	    		$scope.datos.kNgModel = undefined;
	    	},function(result){
	    		console.log(result);
	    		$scope.buscarSolicitudes();
	    		bcsComunes.ventanaOverlay($scope.winID,false);
	    		$scope.datos.kNgModel = undefined;
	    	});
	}
	
	$scope.limpiarFiltros = function()
	{
		$scope.filtros.fechaDesde = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtros.fechaHasta = util.dateTerminoDia($scope.SesionFechaSistema);
		$scope.filtros.IdTipoMovimientoFondoMutuo = 0;
		$scope.filtros.IdEstado = 0;
		$scope.filtros.IdEmisor = 0;
		$scope.filtros.IdCliente.value =  undefined;
		$scope.filtros.IdPortafolio = 0;
		$scope.formData.setFondo = "-1";
		$scope.filtros.IdProducto = undefined;
		$scope.oProducto.seleccionado = false;
		$scope.filtros.TipoSolicitud = 0;
		$scope.filtros.IdMoneda = 0;
		
		if($scope.MostrarProducto){
			$scope.oSocioNegocio.idActual = false;
			$scope.oProducto.idActual = false;
			$scope.oPortafolio.idActual = false;
		}else{
			$scope.oClientePortafolio.idActual = false;
		}		
	};
	
	$scope.limpiarFiltros();
	
	$scope.formData.pagarAGF = function()
	{
		bcsComunes.ventanaAbrir("PagarAGF", {} );
	};
	
	$scope.blotterFondosMutuosModificarFechas = function(seleccionados)
	{
		if(seleccionados != null && seleccionados !== undefined){
			bcsComunes.ventanaAbrir("BlotterFondosMutuosModificarFechas", {seleccionados} );
		}else{
			bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_SELECCIONAR_OPERACIONES"),conflictos:util.parseServerResponse(seleccionados)},$scope.winID);
		}
		
	};

	$scope.blotterFondosMutuosHistorialCambiosModificarFechas = function()
	{
		bcsComunes.ventanaAbrir("BlotterFondosMutuosHistorialCambiosModificarFechas", {} );
	};
	

	$scope.revalorizarMovimientosFFMM = function() {
		if (!bcsComunes.validarAcceso('RevalorizarAportes')) return;
		var t = bcsAlerta.crear('PREGUNTA',$translate.instant('UI_ADVERTENCIA_REVALORIZAR_APORTES'));
		t.result.then(function(result){
			if (result===OPCIONES_BCSALERTA.ACEPTAR)  {
				bcsComunes.ventanaOverlay($scope.winID,true);
				var datos = {
	    			p_IdEmpresa   : $scope.SesionIdEmpresa,
		  			p_IdUsuario   : $scope.SesionIdUsuario,
		  			p_IdModulo 	  : 'FMM',
		  			p_FechaSistema	: $scope.SesionFechaSistema
				}
				bcsServicioFondosMutuos.RevalorizarMovimientosFFMM(datos).$promise.then(function(result){
					if(result.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_REVALORIZAR_APORTES_OK"),conflictos:util.parseServerResponse(result)},$scope.winID);
					} else {
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_REVALORIZAR_APORTES_ERROR"),conflictos:util.parseServerResponse(result)},$scope.winID);
					}
					$scope.buscarSolicitudes();
					bcsComunes.ventanaOverlay($scope.winID,false);
				});
			}
		},function(result){
    		console.log(result);
    		$scope.buscarSolicitudes();
    		bcsComunes.ventanaOverlay($scope.winID,false);
    	});
	};
	
	var buscarLuegoDeCerrarVentana = function(id){
		 var promesa=bcsComunes.ventanaPromesa(id);                               
	     $.when(promesa).then(function(data){
	    	 $scope.buscarSolicitudes();
	     });
	}
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});