angular.module('ModuloOrdenes').controller('DetalleOrdenCtrl',
		function ($scope,bcsComunes,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,$timeout,$translate) {

		$scope.datos = {};
		$scope.formData = {};
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope))
		if (!$scope.parametros.IdOrden)
			console.log("Orden no existe!");
		else {
			bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_INFORMACION_ORDEN") + ' ' + $scope.parametros.IdOrden);
			var ordenDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarOrden'),
							dataType: "json"
						}
					},
	    		    filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
	    		    schema: bcsConfiguracionOrdenesV2.getSchemaOrden()
	    		}});
		
			var asignacionDatasource = new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering:true,
		    		transport: {
		    			read: {
		    				url: bcsConsultasOrdenes('ConsultarAsignacionesUI'),
		    				dataType: "json"
		    		    }
		    		},
		    		filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
					schema: bcsConfiguracionOrdenesV2.getSchemaAsignacion()
				}
			});

			var cobrosOrdenDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
						transport: {
							read: {
								url: bcsConsultasOrdenes('ConsultarCobrosOrden'),
								dataType: "json"
								}
						},
						filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
						schema: bcsConfiguracionOrdenesV2.getSchemaCobrosOrden(),
					}
			});

			var movFacDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
						transport: {
							read: {
								url: bcsConsultasOrdenes('ConsultarMovimientosFacturar'),
								dataType: "json"
								}
						},
						filter: { field: "IdOrden", operator: "eq", value: Number($scope.parametros.IdOrden) },
						schema: bcsConfiguracionOrdenesV2.getSchemaMovFacturar(),
				}
			});
			
			$scope.configuracionPreferenciaPagoPortafolio = {
				tipo: "combobox-preferencia-pago-portafolio",
				configuracion: {},
				filtros: {
					IdPortafolio: { "field": "IdPortafolio", "operator": "eq", "value": $scope.parametros.IdPortafolio },
					IdTipoOperacion: { "field": "IdTipoOperacion", "operator": "eq", "value": $scope.parametros.IdTipoOperacion },
					IdMedioDePago: { "field": "IdMedioDePago", "operator": "neq", "value": $scope.parametros.IdMedioDePago }
				},
				idActual : false,
				seleccionado: false
			};
		
			asignacionDatasource.fetch().then(function(){
				$scope.datos.asignaciones = asignacionDatasource.data();
			});
			movFacDatasource.fetch().then(function(){
				$scope.datos.movimientos = movFacDatasource.data();
			});
			ordenDatasource.fetch().then(function(){
				//bcsComunes.ventanaOverlay(idVentana,false);
				if (ordenDatasource.data().length!=1) {
					console.log("Orden no existe!");
					return false;
				}
				else {
					//$scope.$apply(function() {
						$scope.orden = ordenDatasource.data()[0];
						$scope.esVentaReporte=$scope.orden.IdTipoOperacion=="VENRRV" || $scope.orden.IdTipoOperacion=="VENRRF"? true:false;
						$scope.orden.DecimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.DecimalesMonto = util.bcs_formato(0,'GEN','Monto',$scope.orden.IdMonedaLiquidacionOrden,'','').cantidadDecimales;
						$scope.orden.DecimalesMontoOrden = util.bcs_formato(0,'GEN','Monto',$scope.orden.IdMonedaOrden,'','').cantidadDecimales;
						$scope.orden.DecimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.DecimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.orden.IdMonedaLiquidacionOrden,$scope.orden.IdTipoInstrumento,'').cantidadDecimales;
						$scope.orden.EsOrdenEspecialTexto = ($scope.orden.EsOrdenEspecial=='S')?'SI':'NO';
						$scope.orden.GeneraSpot = ($scope.orden.GeneraSpot=='S')?true:false;
						$scope.orden.pagoCobroCuentaInversion = ($scope.orden.IdMedioDePago=='CIN')?true:false;
						$scope.orden.fechaRecepcion = $scope.orden.FechaRecepcion;
						$scope.orden.comentario= $scope.orden.GlosaOrden;
						$scope.orden.tipoCambio= $scope.orden.TipoCambio;
						$timeout(function(){bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));},20);
					//	});
					return true;
				}

			},function(result){
				console.log(result);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
			
			
		}
		
		$scope.verBitacora = function()
		{
			bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));
			bcsComunes.ventanaAbrir("BitacoraOrden", {IdOrden : $scope.parametros.IdOrden},false,false );
		};
		
		$scope.verMapaRelaciones = function()
		{
			bcsComunes.ventanaAbrir("MapaRelaciones", {TipoEntidad : "ORDEN",IdEntidad :$scope.parametros.IdOrden},false,false );
		};

		$scope.verComprobante = function(){
			bcsConfiguracionOrdenes.FUNCIONES.verComprobante($scope.SesionIdEmpresa,$scope.orden.DocumentoIdentidad,$scope.orden.FechaOperacionOrden,$scope.orden.IdOrden,$scope.orden.MontoLiquidacion,idVentana);			
		};
		
		$scope.configuracionMecanismoCaptacion = {
			filtros: {
				IdPortafolio : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" }
			},
		};

	var firstWidth = $('#Win_DetalleOrden');
		firstWidth.css("width", "855px");

		
		$scope.tabClick = function (){
			var ventana = $("[ventanaid=" + idVentana +"]").closest(".k-window");
			ventana.css('height', 'auto');
		}			
});
