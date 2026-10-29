angular.module('ModuloMesa').controller('DetalleMRFCtrl',
		function($scope,bcsComunes,$translate,bcsServiciosMesa)
{	

	$scope.data = {};
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.data = bcsComunes.ventanaParams($scope.winID);
	$scope.data.evento = $scope.data.item;
	$scope.Fecha = $scope.data.Fecha;
	$scope.IdMetodo = $scope.data.IdMetodo;
	$scope.NombreMetodo = $scope.data.NombreMetodo;
	$scope.TipoPosicion = $scope.data.TipoPosicion;


	// --------------------------------------------------
	// CONFIGURACION DE GRILLA
	// --------------------------------------------------

	var DataSourceMRF = new bcs.DataSource({
		type : "array",
		definition : {
			data : [],
			schema : {
				model : {
					fields : {
								FolioOperacion : {type : "number"},
								TipoOperacion : {type : "string"},
								FechaOperacion : {type : "date"},
								FechaLiquidacion : {type : "date"},
								MontoOperacion: {type : "number"},
								DocumentoIdentidad: {type : "string"},
								SumaResta: {type : "string"}
					}
				}
			},
			aggregate: [
                    { field: "MontoOperacion", aggregate: "sum" }
                ],
			sort: {
					field: "FolioOperacion", 
					dir: "desc" 
			}
		}
	});	 

	$scope.data.gridConfiguracionMRF = {	
		dataSource			: DataSourceMRF,
		columns				: 
			[

				{field: "FolioOperacion", 	title: 'Folio Operación', 			width: "60px" }
				,{field : "TipoOperacion", title: 'Tipo Operación', width: "120px"} 
				,{field : "FechaOperacion", title: 'Fecha Operación', width: "90px",template: function(dataItem) {
                var fecha = kendo.toString(dataItem.FechaOperacion, "yyyy-MM-dd");
                return fecha === "0001-01-01" ? "-" : fecha;
            }}
				,{field : "FechaLiquidacion", title: 'Fecha Liquidación', width: "90px",template: function(dataItem) {
                var fecha = kendo.toString(dataItem.FechaLiquidacion, "yyyy-MM-dd");
                return fecha === "0001-01-01" ? "-" : fecha;
            }}				
				,{field : "MontoOperacion", title: 'Monto Operación', width: "90px",template : " <label> #: (util.bcs_formato(MontoOperacion,'GEN','Monto','CLP','','')).valor #</label>", footerTemplate: "#= kendo.toString(sum, 'n0') #" }				
				,{field : "DocumentoIdentidad", title: 'Documento Identidad', width: "90px"}				
			],
		
		userSettings 		: false,
		pageable	        : true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		groupable 			: false,
		filterable 			: true,
		height				: 400,
		selectable 			: false,
		autoBind            : true,
		 dataBound: function(e) {
                var gridElement = e.sender.element[0];
                var rows = gridElement.querySelectorAll('tbody tr');
                rows.forEach(function(row) {
                    var dataItem = e.sender.dataItem(row);
					if (dataItem && dataItem.SumaResta && $scope.TipoPosicion === "RSM") {
	                    if (dataItem.SumaResta === "R") {
	                        row.style.color = "red";
	                    } else {
	                        row.style.color = "green";
	                    }
                    }
                });
            }
	};

	$scope.show = true;	
	
	$scope.buscarDetalle = function() {	
		bcsComunes.ventanaOverlay($scope.winID, true);
		var datosItem = {
			p_IdEmpresa: $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'MRF',
			p_IdPais: $scope.SesionIdPaisSistema,
			p_FechaPosicion: $scope.Fecha,
			p_NombreItem: $scope.data.evento.NombreAgrupacion,			
			p_NombreEvento: $scope.data.evento.NombreEvento,
			p_IdMetodo: $scope.IdMetodo,
			p_NombreMetodo: $scope.NombreMetodo,			
			p_TipoPosicion: $scope.TipoPosicion,			
		};
		
		var promesaDetalleItem = bcsServiciosMesa.ConsultarDetallePosicionMRF(datosItem).$promise;		
		promesaDetalleItem.then(function(result) {			
			var filasGrilla = [];
			result.P_OUT_OPERACIONES.forEach(operacion => {
				var fila = {};
				fila.FolioOperacion = operacion.FolioComprobante;
				fila.TipoOperacion = operacion.TipoOperacion;
				fila.FechaOperacion = operacion.FechaComprobante;
				fila.FechaLiquidacion = operacion.FechaLiquidacion;
				fila.MontoOperacion = operacion.MontoLiquidacionFinal;
				fila.DocumentoIdentidad = operacion.DocumentoIdentidad;						
				fila.SumaResta = operacion.SumaResta;						
				filasGrilla.push(fila);    			
			});			
	
			$scope.data.gridConfiguracionMRF.dataSource.data(filasGrilla);
	
			bcsComunes.ventanaOverlay($scope.winID, false);
		}).catch(function(error) {
			console.error("Error al consultar el detalle de posición MRF:", error);
			bcsComunes.msg("error", "info", "Error al consultar el detalle de posición MRF");
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
	}
	
	$scope.buscarDetalle();


	// funcion WebSocket
	$scope.msgWebsocket = function(message) {    	
		if(message.Evento==='ACTMRF' || message.Evento==='ANUMRF' || message.Evento==='MODMRF' || message.Evento==='SIMFACT' || message.Evento==='ACTPOSMRF'){
			console.log('hola mundo');		// TODO: reemplazar por funcion que llama el detalle
		}
	};
});
