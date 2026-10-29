angular.module('ModuloCustodia').controller('ConsultarFusionCarterasCtrl',
function($scope, $timeout, $modal, $state, bcsComunes, $rootScope, $translate, bcsServiciosValorizacion, bcsServiciosCustodia)
{	


/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
    var idVentana = bcsComunes.ventanaWinId($scope);
    var gridFusionCartera = null;

	$scope.esVentana = false;
	$scope.MostrarProducto = false;
	$scope.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.parametros = bcsComunes.ventanaParams($scope.idVentana);
	$scope.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
	$scope.esVentana = ($scope.parametros.esVentana == 'S');
	$scope.IdClienteVentana = 0;
	$scope.IdPortafolioVentana = 0;


	$scope.verFusion 	= false;
	$scope.verGrillaAll = true;
	
	//$scope.kngFechaConsulta		   = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaConsultaDesde   = util.dateInicioDia($scope.SesionFechaSistema);
	$scope.kngFechaConsultaHasta   = util.dateTerminoDia($scope.SesionFechaSistema);
	
	$scope.chkFechaAdquisicion 	 = true;  //b�squeda por defecto por fecha de liquidaci�n 
	
	

	$scope.cargaFiltroParametros = function(){
		console.log("cargaFiltroParametros");
		try{
			if ($scope.esVentana){
				$scope.filtros.clasificacionTipoInstrumento = $scope.parametros.IdClasificacionTipoInstrumento;
				$timeout(function(){
					$scope.filtros.tipoInstrumento = $scope.parametros.IdTipoInstrumento;	
				},1000);
			
				$scope.IdClienteVentana = $scope.parametros.IdCliente;				
				$scope.IdPortafolioVentana = $scope.parametros.IdPortafolio;
				$scope.oConfiguracionCliente.seleccionado.IdCliente = $scope.parametros.IdCliente;
				$scope.oConfiguracionPortafolio.seleccionado.IdPortafolio = $scope.parametros.IdPortafolio;
			
				$timeout(function(){
					$scope.buscarCarteras();
				},1000);
			}
		}catch(e){$scope.ErrorManager(e);}
	}

	$timeout(function(){
		$scope.cargaFiltroParametros();
	},1000);
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	
	
	$scope.data = {};

	
/******************************************************/
/*** FILTROS										***/
/******************************************************/
	
	$scope.oConfiguracionCliente = {

		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},			
		idActual : undefined,
		seleccionado : -1,
		idDefecto : -1
	};
	
	$scope.oConfiguracionPortafolio = {

		tipo : "combobox-portafolio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -1 }
		},
	};
	
	$scope.oConfiguracionClasificacionTipoInstrumento = {
		tipo : "combobox-clasificacion-tipo-instrumento",
		configuracion : {},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false,
		filtros : { 	
				IdClasificacionRV : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "RV"},
				IdClasificacionDV : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "DV"},
				IdClasificacionFM : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "FM"},
				IdClasificacionSP : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "SP"},
				IdClasificacionRE : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "RE"},
				IdClasificacionFW : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "FW"},
				IdClasificacionIR : { "field"   : "IdClasificacionTipoInstrumento","operator": "neq", "value"   : "IR"}
			}
	};
	
   	$scope.oConfiguracionTipoInstrumento = {
		tipo : "combobox-tipo-instrumento",
		configuracion : {},
		filtros : {
			IdClasificacionTipoInstrumento 	: { "field" : "IdClasificacionTipoInstrumento", "operator": "eq", "value" : "" },
			IdEstado 						: { "field" : "IdEstado",                       "operator": "eq", "value" : "VIG" },
			NoPactos 						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PAC" },
			NoPrestamos						: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "PRT" },
			NoEmprestitos					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "EMP" },
			NoSimultaneas					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SIM" },
			NoReportesRV					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRV" },
			NoReportesRF					: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "RRF" },
			NoSpot 							: { "field" : "IdTipoInstrumento",              "operator": "neq", "value" : "SPT" },
			NoDerivados						: { "field" : "CodigoTipoInstrumento",          "operator": "neq", "value" : "DER" }
		},
		idActual : false,
		seleccionado : false,
		idDefecto : '',
		idPostFiltro : false
	};
   	
   	$scope.oConfiguracionInstrumento = {
		buscar : "-1"    
	};
   	
	$scope.oSocioNegocio = {
		filtros: {
			IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		}
	};
	


	$scope.oProducto = {
		idActual : false,
		seleccionado : false
	};							// quitado revisar otras referencias
	
	$scope.oPortafolio = {
		filtros: {
			IdCliente : { "field": "IdCliente","operator": "eq", "value"   :  -1 },
			IdProductoAgrupador : { "field": "IdProductoAgrupador","operator": "eq", "value"   :  undefined }
		},
	};
	
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
	
	

	// Modelo para la sub-grilla DETALLES 
	modeloDetalleFusionCarteras = function() {
		return {
			model: {
				fields: {
					FolioCartera        : { type: "string"},
					Cantidad            : { type: "number"},
					ValorRescate 		: { type : "number"},
					TipoComprobante     : { type: "string"},
					FechaCompra         : { type: "sysdate"},
					FechaLiquidacion    : { type: "sysdate" },
					CategoriaIFRS       : { type: "string" },
					SecuenciaFusion 	: { type: "number" },
					MontoCompra         : { type: "number" },
					TasaCompra          : { type: "number" },
					ValorPresente       : { type: "number" }
				}
			}
		};
	};

    var datosCarterasFusionadas =
    {
        type : "odata",
        definition : 
        {
            serverFiltering : true,
            serverPaging    : true,
            serverSorting   : true,
            sort            : { field : "IdFusion", dir: "DESC"},
            pageSize        : 25,
            transport       :
            {
                read:
                {
                    url         :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290695",				
				    dataType    : "json"
                }
            },
            schema  : {
                model: {
                    fields: {
                        IdCliente								: { type : "number"},
                        INGFechaValorizacionCartera				: { type : "sysdatetime"},
                        FechaLiquidacion						: { type : "sysdate"},
                        CodigoUsuario							: { type : "string"},
                        DocumentoIdentidad						: { type : "string"},
                        IdPortafolio							: { type : "number"},
                        CodigoPortafolio						: { type : "string"},
                        IdClasificacionTipoInstrumento 			: { type : "string"},
                        FolioCartera							: { type : "number"},
                        Nemotecnico								: { type : "string"},
                        NemoSVS									: { type : "string"},
                        IdEmisor								: { type : "string"},
                        IdTipoReajuste							: { type : "string"},
						IdMonedaInstrReajuste 					: { type : "string"},
                        IdBoveda						  		: { type : "string"},
                        FechaComprobante						: { type : "sysdate"},
                        FechaVencimientoInstrumento				: { type : "sysdate"},
                        CarterasFusionadas						: { type : "number"},
                        INGTasaCompra							: { type : "number"},
                        INGCantidad								: { type : "number"},
                        ValorRescate 							: { type : "number"},
                        MontoCompraMonedaInstrumento			: { type : "number"},
                        ValorCalculadoFus						: { type : "number"},
                        DiferenciaInversion						: { type : "number"},
                        ClasificacionContable					: { type : "string"},
                        IdEmpresa 								: { type : "number"},
                        IdTipoInstrumento 						: { type : "string"},
                        IdEstado 								: { type : "string"},
                        IdFusion 								: { type : "number"}
                    }
                }
            }
        }
    };



	// dataSource Subgrilla
	dataSourceDetalleFusionCarteras = function(filtrosDetalleFusionFacturas) 
	{
	return new bcs.DataSource(
		{
			type 		: "odata",
			definition 	: definicionODetalleFusionCarteras(filtrosDetalleFusionFacturas)
		});

	};

	
	var transporteDetalleFusionCarteras = function() {
		return {
			read : {
				url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290684",				
				dataType : "json"
			}
		};
	};
	
	var definicionODetalleFusionCarteras = function(inFiltros) 
	{
		return { 
			serverFiltering : true,
			serverSorting   : true, 
			sort 			  : { field: "FolioCartera", dir: "asc" }, // Criterios de Ordenamiento.
			pageSize 		  : 10, // Número de registros por página.
			transport 	  : transporteDetalleFusionCarteras(),
			schema 		  : modeloDetalleFusionCarteras(),              
			filter		  : inFiltros
		};	


	};







	/******************************************************/
	/*** FUNCIONES DE CARGA DE TABLAS					***/
	/******************************************************/
	// columnas Grilla Principal
	columnaDetalleFusionar = function (){
		return [
			{field: "INGFechaValorizacionCartera", 		    	title: 'UI_FECHA_HORA',						width: "80px" }
			,{field: "FechaLiquidacion", 						title: 'UI_FECHA_SISTEMA', 	        		width: "80px" }        	
			,{field: "CodigoUsuario", 							title: 'UI_USUARIO', 						width: "80px", 	groupable: true }
		   	,{field: "DocumentoIdentidad", 						title: 'UI_CLIENTE', 						width: "85px", 	groupable: true } 
		   	,{field: "CodigoPortafolio", 						title: 'UI_PORTAFOLIO_ABR', 				width: "50px",  groupable: true }
		   	,{field: "IdClasificacionTipoInstrumento", 			title: 'UI_MERCADO', width: "50px", 	groupable: true }	    		
		   	,{field: "FolioCartera", 							title: 'UI_FOLIO_CARTERA', 	     			width: "50px" }
		   	,{field: "Nemotecnico", 							title: 'UI_INSTRUMENTO', 					width: "100px", groupable: true }	    				
			,{field: "NemoSVS", 								title: 'UI_NEMO_SVS', 						width: "80px", 	groupable: true }	    			
		    ,{field: "IdEmisor", 								title: 'UI_EMISOR', 						width: "80px", 	groupable: true }
		    ,{field: "IdTipoReajuste", 							title: 'UI_TIPO_REAJUSTE', 					width: "55px", 	groupable: true }
		    ,{field: "IdBoveda", 								title: 'UI_BOVEDA', 						width: "55px", 	groupable: true }
		    ,{field: "FechaComprobante", 						title: 'UI_FECHA_COMPRA', 	     			width: "80px" }	    			    		
		    ,{field: "FechaVencimientoInstrumento", 	   		title: 'UI_VENCIMIENTO', 					width: "80px" }
		    ,{field: "CarterasFusionadas", 						title: 'UI_CARTERA_FUSIONADA', 				width: "50px" }	    			    	    			    		
			,{field: "INGTasaCompra", 							title: 'UI_TASA'          , 				width: "50px", template : " <label> #:  (util.bcs_formato(INGTasaCompra,'GEN','Tasa','" + $rootScope.SesionIdMonedaSistema + "','IdTipoInstrumento','')).valor #</label>"  }
			,{field: "INGCantidad", 							title: 'UI_CANTIDAD_TOTAL', 				width: "80px", template : " <label> #:  (util.bcs_formato(INGCantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }	    		
			,{field: "INGValorRescate", 						title: 'UI_VALOR_RESCATE', 					width: "80px", template : " <label> #:  (util.bcs_formato(INGValorRescate,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }	    		
		   	,{field: "MontoCompraMonedaInstrumento", 			title: 'UI_MONTO_COMPRA',					width: "105px", template : " <label> #:  (util.bcs_formato(MontoCompraMonedaInstrumento,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#=util.formateaNumero(sum,2)#"  }
		   	,{field: "ValorCalculadoFus", 						title: 'UI_VALOR_PRESENTE', 				width: "105px", template : " <label> #:  (util.bcs_formato(ValorCalculadoFus,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }   		
		   	,{field: "DiferenciaInversion", 					title: 'UI_DIFERENCIA', 	        		width: "105px", template : " <label> #:  (util.bcs_formato(DiferenciaInversion,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" }	    		
		   	,{field: "ClasificacionContable", 					title: 'UI_CLASIFICACION_CONTABLE',   		width: "80px" }	           
			
	   
	   
			]
	};

	// COLUMNAS SUBGRILLA
	columnasDetalleFusionCarteras = function() {

		var FolioCartera        = { field: "FolioCartera",      title: "UI_FOLIO_CARTERA_ABR",          width: "70px", 	 };
		var Cantidad            = { field: "Cantidad",          title: "UI_CANTIDAD",                   witdh: "40px", template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>" 	 };
		var ValorRescate		= { field: "ValorRescate", 		title: 'UI_VALOR_RESCATE', 				width: "80px",	template : " <label> #:  (util.bcs_formato(ValorRescate,'GEN','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"	};
		var TipoComprobante     = { field: "TipoComprobante",   title: "UI_TIPO_COMPROBANTE",           witdh: "110px", 	 };
		var FechaCompra         = { field: "FechaCompra",       title: "UI_FECHA_COMPROBANTE",          width: "80px", 	 };
		var FechaLiquidacion    = { field: "FechaLiquidacion",  title: "UI_FECHA_LIQUIDACION_ABR",     	width: "80px", 	 };
		var CategoriaIFRS       = { field: "CategoriaIFRS",     title: "UI_CATEGORIA_IFRS_ABR",         width: "70px", 	 };
		var SecuenciaFusion     = { field: "SecuenciaFusion",   title: "UI_SECUENCIA_FUSION",           width: "70px", 	 };
		var MontoCompra         = { field: "MontoCompra",       title: "UI_MONTO_COMPRA",               width: "75px", template : " <label> #:  (util.bcs_formato(MontoCompra,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','IdTipoInstrumento','')).valor #</label>" };
		var TasaCompra          = { field: "TasaCompra",        title: "UI_TASA_COMPRA",                width: "70px", template : " <label> #:  (util.bcs_formato(TasaCompra,'GEN','Tasa','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"	 };
		var ValorPresente       = {field: "ValorPresente",      title: "UI_VALOR_PRESENTE",             width: "100px", template : " <label> #:  (util.bcs_formato(ValorPresente,'GEN','Monto','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}; 
	

		var base = [
			FolioCartera,
			Cantidad,
			ValorRescate,
			TipoComprobante,
			FechaCompra,
			FechaLiquidacion,
			CategoriaIFRS,
			SecuenciaFusion,
			MontoCompra,
			TasaCompra,
			ValorPresente
		];

		return base; 
	
	};


    var dsCarterasFusionadas = new bcs.DataSource(datosCarterasFusionadas);

	$scope.confBasicaCarterasFCAll = {
		dataSource			: dsCarterasFusionadas,
		columns				: columnaDetalleFusionar(),
			sortable		: true,
	    	pageable		: true,
	    	filterable		: true,
	    	navigatable		: true,
	    	height			: 460,
	    	userSettings	: true,
			excel 			: {allPages: true,},
			selectable 		: "row",
        gridCreated : function(grid){
            gridFusionCartera = grid;
        }

		
		
	};

	//SUBGRILLA

	$scope.confBasicaDetalleFusionCarteras = function(dataItem)
	{
	
		var filtrosDetalleFusionFacturas = [{field: "SecuenciaFusion", operator: "eq", value: dataItem.IdFusion}];
		return {
				dataSource		: dataSourceDetalleFusionCarteras(filtrosDetalleFusionFacturas),
				columns			: columnasDetalleFusionCarteras(),
				sortable			: true,
				pageable			: true,		
				height				: 150,
				width				: 300,
				selectable 			: "multiple row",
				autoBind         	: true,
				navigatable       	: true,
				allowCopy 			: true
				/*gridCreated 		: function(g) {
					$scope.ApiMovimientoCobros = g;
					},*/
			};
	}; 

/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	// TODO :


	$scope.FusionarCarteras = function(){
		
		var datosFC = {
			p_IdEmpresa 		: $scope.SesionIdEmpresa, 
			p_IdUsuario 		: $scope.SesionIdUsuario,
			p_IdPais			: $scope.SesionIdPaisSistema,
			p_IdModulo 			: "CUS",		
		};

		bcsServiciosCustodia.FusionarCarteras(datosFC).$promise.then(
			function(result) {
				bcsComunes.ventanaOverlay(idVentana,false);
				if(result ){
					var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
						conflictos : util.parseServerResponse(result)}, idVentana);
					if(result.P_OUT_ERRORESCONTADOR === 0) {
						if(id){
							var promesa=bcsComunes.ventanaPromesa(id);                               
							$.when(promesa).then(function(data){
								 $timeout(function() {
									bcsComunes.ventanaOverlay(idVentana,false);
									dsCarterasFusionadas.read();
								 },20);
							 }); 
						}else{
							bcsComunes.ventanaOverlay(idVentana,false);
		
						 }
					}
				  $scope.verFusion 	= true;
				  $scope.buscarCarteras();	
				}
			},
			function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
			}
		);

		bcsComunes.msg('info',$translate.instant("UI_INFO"), "Funcion fusionar carteras ejecutado");
	}

	$scope.buscarCarteras = function(){
		console.log("buscarCarteras");
		try{
			var _filter = Array();
			if ($scope.verFusion){
			  $scope.verFusion 	= false;
			 _filter.push({ field: "FechaComprobante", operator: "ge", value: util.dateInicioDia($scope.SesionFechaSistema) });
			 _filter.push({ field: "FechaComprobante", operator: "le", value: util.dateTerminoDia($scope.SesionFechaSistema) });		 
			}else{
			  if ($scope.kngFechaConsultaDesde  != undefined) {
			  	$scope.kngFechaConsultaDesde   = util.dateInicioDia($scope.kngFechaConsultaDesde);
			    $scope.kngFechaConsultaHasta   = util.dateTerminoDia($scope.kngFechaConsultaHasta);
			  	var resp = util.validarRangoFecha($scope.kngFechaConsultaDesde , $scope.kngFechaConsultaHasta );
			  	if(resp.estado){
			  	  bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
			  	  return;
			  	}
			  	_filter.push({ field: "FechaComprobante", operator: "ge", value: $scope.kngFechaConsultaDesde });
			  	
			  }
			  if ($scope.kngFechaConsultaHasta  != undefined) {
			  	_filter.push({ field: "FechaComprobante", operator: "le", value: $scope.kngFechaConsultaHasta  });
			  }
			  if($scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento !== undefined)
                _filter.push({ field: "IdTipoInstrumento"       , operator : "eq", value: $scope.oConfiguracionTipoInstrumento.seleccionado.IdTipoInstrumento });
			}
			/*
			if ($scope.oConfiguracionCliente.seleccionado.IdCliente == undefined || $scope.oConfiguracionPortafolio.seleccionado.IdPortafolio == undefined){
				{
					bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Debe ingresar un cliente/portafolio o un folio de cartera para buscar.");
					return;
				}
			}*/
			
			if($scope.oConfiguracionCliente.seleccionado.IdCliente !== undefined)
                _filter.push({ field: "IdCliente"       , operator : "eq", value: $scope.oConfiguracionCliente.seleccionado.IdCliente });

            if($scope.oConfiguracionPortafolio.seleccionado.IdPortafolio !== undefined)
                _filter.push({ field: "IdPortafolio"       , operator : "eq", value: $scope.oConfiguracionPortafolio.seleccionado.IdPortafolio });

            if($scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento !== undefined){
                _filter.push({ field: "IdClasificacionTipoInstrumento"       , operator : "eq", value: $scope.oConfiguracionClasificacionTipoInstrumento.seleccionado.IdClasificacionTipoInstrumento });
			}else{
				//bcsComunes.msg('error',$translate.instant("UI_ERROR"), "Debe ingresar si busca Renta Fija o Intermediación Financiera.");
				//return;
			}

            
			
			//_filter.push({ field: "CarterasFusionadas"       , operator : "neq", value: '0' });

            dsCarterasFusionadas.filter(_filter);
            dsCarterasFusionadas.read().then(function(){
                gridFusionCartera.refresh;
            });

		}catch(e){$scope.ErrorManager(e);}
		
    	
	};
	
	
	
	
	
	$scope.limpiarFiltros = function() {
		try{
			$scope.oConfiguracionClasificacionTipoInstrumento.idActual		= "";
			$scope.oConfiguracionClasificacionTipoInstrumento.idDefecto		= "";
			$scope.oConfiguracionTipoInstrumento.idActual					= "";
			$scope.oConfiguracionTipoInstrumento.idDefecto					= "";
			$scope.socioGral.idActual										= "";

			$scope.oConfiguracionCliente.idActual			= false;
			$scope.oConfiguracionPortafolio.filtros			= { IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  -547548 } };
			$scope.oConfiguracionPortafolio.idActual		= false;
			$scope.oConfiguracionPortafolio.seleccionado	= false;
			//$scope.kngFechaConsultaDesde					= "";
			$scope.oSocioNegocio.idActual					= false;
			$scope.oPortafolio.idActual						= false;
			dsCarterasFusionadas.removeAll();    
			

		}catch(e){
			$scope.ErrorManager(e);
		}
	}
	
	$scope.ErrorManager = function (errorcatched){
		bcsComunes.msg('error',$translate.instant("UI_ERROR"), errorcatched);
	}

	$scope.detalleCarterasFC = function(datos){
		bcsComunes.ventanaAbrir("DetalleCarteraFC", {FolioCartera: datos[0].FolioCartera}, false, false, idVentana );		
	};


	
/******************
		Watch
********************/
	$scope.$watch('oConfiguracionCliente.seleccionado',function(newval,oldval){
		if (!$scope.oConfiguracionCliente.seleccionado) return;
		try{
			$scope.socioGral.idActual = $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
			$scope.socioGral.idDefecto = $scope.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
		}catch(e){
		}
	});

	$scope.$watch('oConfiguracionClasificacionTipoInstrumento.seleccionado',function(newval,oldval){
		$scope.verGrillaAll = true;
		if(!!$scope.grillaAll && !$scope.grillaAll.refreshed)
			$timeout(function() {
				$scope.grillaAll.refresh();
				$scope.grillaAll.refreshed = true;
			}, 100);
	});	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});