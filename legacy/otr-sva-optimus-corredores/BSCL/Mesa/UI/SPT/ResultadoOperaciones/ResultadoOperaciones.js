(function() {
	angular.module('ModuloMesa').controller('ResultadoOperacionesCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var row = '';

		$scope.data = {};
		
		var resultOp = this;
		
		resultOp.datosResultado = {
				resultadoPosicionInicial: 0,
				resultadoInterbancario : 0,
				resultadoDistribucion : 0,
				resultadoArbitrajeInterbancario : 0,
				resultadoArbitrajeDistribucion : 0,
				resultadoTotal : 0,
				idMoneda : $scope.SesionIdMonedaSistema,
		};
		resultOp.datosAntes = {};
		resultOp.datosDespues = {};
		resultOp.datosAntes = angular.copy(resultOp.datosDespues);
		
		//Configurar al principio
		
		$scope.fechaOperacion = {};
		$scope.fechaOperacion.max = new Date();
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.data.MargenPunta = $scope.data.kngMargenPunta || 0;
		$scope.data.MontoCompraInterbancario = 0;
		$scope.data.PrecioCompraInterbancario = 0;
		$scope.data.MontoVentaInterbancario = 0;
		$scope.data.PrecioVentaInterbancario = 0;
		$scope.data.MontoCompraConsolidado = 0;
		$scope.data.PrecioCompraConsolidado = 0;
		$scope.data.MontoVentaConsolidado = 0;
		$scope.data.PrecioVentaConsolidado = 0;
		$scope.data.PosicionInicialCompra = 0;
		$scope.data.TipoCambioCompra = 0;
		$scope.data.PosicionInicialVenta = 0;
		$scope.data.TipoCambioVenta = 0;
		resultOp.data = {};

		
		//Asigna valores iniciales
		var datos = {};

		$scope.data.oConfiguracionTipoInstrumento={
				filtros : {
					IdTipoProducto : { "field"   : "IdTipoProducto","operator": "eq", "value" : parametrosVentana.idTipoProducto }
				}
			};
		
	
		
		var varIdPunta;
		var estado;
		
		var successResultadoOperaciones = function(respuesta)
		{
			if (respuesta.P_OUT_ERRORESCONTADOR == 0){

				$scope.data.MontoCompraInterbancario = respuesta.P_OUT_MONTOSINTERBANCARIOS[0].MontoCompraInterbancario;
				$scope.data.PrecioCompraInterbancario = respuesta.P_OUT_PRECIOSPROMEDIOS[0].PrecioCompraInterbancario;
				$scope.data.MontoVentaInterbancario = respuesta.P_OUT_MONTOSINTERBANCARIOS[0].MontoVentaInterbancario;
				$scope.data.PrecioVentaInterbancario = respuesta.P_OUT_PRECIOSPROMEDIOS[0].PrecioVentaInterbancario;
				$scope.data.MontoCompraConsolidado = respuesta.P_OUT_MONTOSCONSOLIDADOS[0].MontoCompraConsolidado;
				$scope.data.PrecioCompraConsolidado = respuesta.P_OUT_PRECIOSPROMEDIOS[0].PrecioCompraConsolidado;
				$scope.data.MontoVentaConsolidado = respuesta.P_OUT_MONTOSCONSOLIDADOS[0].MontoVentaConsolidado;
				$scope.data.PrecioVentaConsolidado = respuesta.P_OUT_PRECIOSPROMEDIOS[0].PrecioVentaConsolidado;
				$scope.data.PosicionInicialCompra = respuesta.P_OUT_POSICIONINICIAL[0].PosicionCompra;
				$scope.data.TipoCambioCompra = respuesta.P_OUT_POSICIONINICIAL[0].TipoCambioCompra;
				$scope.data.PosicionInicialVenta = respuesta.P_OUT_POSICIONINICIAL[0].PosicionVenta;
				$scope.data.TipoCambioVenta = respuesta.P_OUT_POSICIONINICIAL[0].TipoCambioVenta;
				
				dsOperaciones.data(respuesta.P_OUT_RESULTADOOPERACIONESINTERBANCARIOS);
				
				var posicionInicial = 0;
				
				$.each(respuesta.P_OUT_RESULTADOSTOTALES,function(key,value){
					switch(value.Campo) {
				    case 'POSINI':
				    	resultOp.datosResultado.resultadoPosicionInicial = value.Valor;
				    	posicionInicial = value.Valor;
				        break;
				    case 'INTERB':
				    	resultOp.datosResultado.resultadoInterbancario = value.Valor;
				        break;
				    case 'DISTRI':
				    	resultOp.datosResultado.resultadoDistribucion = value.Valor;
				        break;
				    case 'ARBINT':
				    	resultOp.datosResultado.resultadoArbitrajeInterbancario = value.Valor;
				        break;
				    case 'ARBDIS':
				    	resultOp.datosResultado.resultadoArbitrajeDistribucion = value.Valor;
				        break;
				    case 'TOTAL':
				    	resultOp.datosResultado.resultadoTotal = value.Valor;
				        break;
				    default:
				        break;
					}
				});
				
				if(resultOp.datosResultado.resultadoTotal === 0)
					resultOp.datosResultado.resultadoTotal = posicionInicial
			}
			else
			{		
				bcsComunes.ventanaMsg({
				      mensaje:$translate.instant("UI_ERROR"),
				      conflictos:{
				       errores : [ {
				           Codigo : 'COD',
				           Descripcion : 'Un error inesperado ha ocurrido'
				          } ],
				      }
				     },idVentana);
			}
			bcsComunes.ventanaOverlay($scope.winID,false);;
          };	
		
		var error = function(respuesta)
		{
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID,false);;
		};

		$scope.data.filtrarOperaciones = function()
		{	
			bcsComunes.ventanaOverlay($scope.winID,true);
			
			if($scope.data.kngFechaOperacion === undefined)
			{
				bcsComunes.msg("error","error",$translate.instant('UI_FECHA_VACIA'));
				bcsComunes.ventanaOverlay($scope.winID,false);
			}
			else{
				
				resultOp.datosResultado.resultadoPosicionInicial = 0;
				resultOp.datosResultado.resultadoInterbancario = 0;
				resultOp.datosResultado.resultadoDistribucion = 0;
				resultOp.datosResultado.resultadoArbitrajeInterbancario = 0;
				resultOp.datosResultado.resultadoArbitrajeDistribucion = 0;
				resultOp.datosResultado.resultadoTotal = 0;
				
				var parametros = {};
				parametros.p_FechaOperacion    = $scope.data.kngFechaOperacion;
				parametros.p_IdEmpresa         = $scope.SesionIdEmpresa;
				parametros.p_IdUsuario         = $scope.SesionIdUsuario;
				parametros.p_IdMoneda          = parametrosVentana.idMoneda;

				var tablaResultados = [];
				
				var ObjResultados = {
						"Campo" : 'TODOS'
					};
				
				tablaResultados.push(ObjResultados);
				
				parametros.p_Resultados = tablaResultados;

				bcsServiciosMesa.ConsultarResultadoSpot(parametros, successResultadoOperaciones,error);
			}
		};

		var dsDefDetalle = 
		{
				type : "call",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "IdOperacion", dir: "ASC" },
                    pageSize        : 100,
//                    transport		: {
//		        	            		read: 
//		        	            		{
//		        	            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=439",
//		        							dataType:"json"
//		        	            		}
//		        	            	},
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													FolioOperacion		: {type: "number"},
													MontoCompra			: {type: "number"},
													PrecioCompra		: {type: "number"},
													MontoVenta			: {type: "number"},
													PrecioVenta       	: {type: "number"}
												}
											}
									  },
    			 } 
		};
       	
		//---------------------------------------------
		//GRILLA OPERACIONES
 
		var dsOperaciones = new bcs.DataSource(dsDefDetalle);
		$scope.confBasicaOperaciones = 
		{
    				dataSource		: dsOperaciones,
    					columns		: [ {field: "FolioOperacion", title: "UI_FOLIO_OPERACION", width: "70px", align: "right", sortable : false  }
				       				   ,{field: "MontoCompra", title: "UI_MONTO_COMPRA", width: "70px", align: "right", template : " <label> #if(MontoCompra === 0){# 0,0000 #}else { # #: (util.bcs_formato(MontoCompra,'SPT','Monto','USD','','')).valor # #} #</label>"}
				       				   ,{field: "PrecioCompra", title: "UI_PRECIO_COMPRA", width: "70px", align: "right", sortable : false, template : " <label> #if(PrecioCompra === 0){# 0,0000 #}else { # #: (util.bcs_formato(PrecioCompra,'SPT','Monto','USD','','')).valor # #} #</label>" }
				       				   ,{field: "MontoVenta", title: "UI_MONTO_VENTA", width: "70px", align: "right", template : " <label> #if(MontoVenta === 0){# 0,0000 #}else { # #: (util.bcs_formato(MontoVenta,'SPT','Monto','USD','','')).valor # #} #</label>"}
				       				   ,{field: "PrecioVenta", title: "UI_PRECIO_VENTA", width: "70px", align: "right", sortable : false, template : " <label> #if(PrecioVenta === 0){# 0,0000 #}else { # #: (util.bcs_formato(PrecioVenta,'SPT','Monto','USD','','')).valor # #} #</label>" }
				       				   ],		
    					sortable	: true,
    					pageable	: false,
    					filterable	: false,
    					height		: 250,
    					navigatable : true,
    				    autoBind 	: true,
    					userSettings 	: true,
//    					excel 			: { allPages: true },
    				    gridCreated : function(grid){
    				    	dsOperaciones.read().then(function(){
 
    				    	});
    				    }
    				    
    			
       	};
		
		$scope.data.filtrarOperaciones();
//------------ GUARDAR PUNTAS -----
		
		
		

		
//------------		
	});
}());