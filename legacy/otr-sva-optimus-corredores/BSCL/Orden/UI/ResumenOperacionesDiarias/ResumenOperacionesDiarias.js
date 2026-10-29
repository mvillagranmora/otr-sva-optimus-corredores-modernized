angular.module('ModuloOrdenes').controller('ResumenOperacionesDiariasCtrl',function($scope, bcsComunes, bcsServicioOrden,bcsServicioPlataforma) {

					// *** configuracion de objetos ***//
					$scope.filtros = {};
					$scope.data = {};

					var idVentana = bcsComunes.ventanaWinId($scope); 

					$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);

					$scope.data.oConfiguracionCliente = {
						tipo : "combobox-socio-negocio",
						configuracion : {},
						filtros : {IdEmpresa : {"field" : "IdEmpresa","operator" : "eq","value" : $scope.SesionIdEmpresa},
								   EsCliente : {"field" : "EsCliente", "operator" : "eq","value" : 'UI_SI'}
						},
						idActual : undefined,
						seleccionado : -1,
						idDefecto : -1
					};
						
					$scope.confGrillaResumenOperaciones = {
							autoBind: false,
				      		allowCopy: true,
				      		filterable: true,
							groupable: false,
							height:450,
				      		reorderable: true,
							resizable: true,
				      		selectable: "multiple, row",
				      		scrollable: true,
				      		sortable: true,
				      		userSettings:true,
							navigatable: true,
							gridCreated : function(grid) {
								//$scope.apiGridResumenOperaciones = grid;
								//$scope.buscar();
							}, 		 
							dataSource : new bcs.DataSource({
								type : "array",
								
								definition : {
									serverSorting : false,
									data : [],
									schema : {
										model : {
											fields : {	
												FechaOperacion : {type : "date"},
												Rut : {type : "string"},
												Cliente : {type : "number"},
												NombreCliente: {type: "string"},
												Portafolio : {type : "string"},
												Mercado : {type : "string"},
												TipoInstrumento : {type : "string"},
												Monto : {type : "number"},
												Cantidad : {type : "number"},
												IdMoneda : {type : "string"},
												Precio : {type : "number"},
												Tasa : {type : "number"},
												Paridad : {type : "number"},
												PrecioPromedio : {type : "number"},
												ParidadPromedio : {type : "number"},
												TasaPromedio : {type : "number"},
												FechaVencimiento : {type : "date"},
												FechaLiquidacion : {type : "date"}
											}
										}
									},
									aggregate:[
						        { field:"Monto", aggregate:"sum" },
						        { field:"Cantidad", aggregate:"sum" },
						        { field:"Precio", aggregate:"sum" }
						    ]
								}
								
							}),
				  			columns: [
								{field : "FechaOperacion",title : 'UI_FECHA_OPERACION',width : "90px",groupable : true}, 
								{field : "Rut",title : 'UI_RUT',width : "90px",groupable : true, align: 'center'}, 
								{field : "Cliente",title : 'UI_ID_CLIENTE',width : "60px",groupable : true}, 
								{field : "NombreCliente",title : 'UI_NOMBRE_CLIENTE',width : "200px",groupable : true}, 
								{field : "Portafolio",title : 'UI_PORTAFOLIO',width : "70px",groupable : true}, 
								{field : "Mercado",title : 'UI_MERCADO',width : "60px",groupable : true},
								{field : "TipoInstrumento",title : 'UI_TIPO_INSTRUMENTO',width : "90px",groupable : true},
								{field : "Monto",title : 'UI_MONTO',width : "100px",groupable : true, template : " <label> #:  (util.bcs_formato(Monto,'GEN','Monto',IdMoneda,'','')).valor #</label>", footerTemplate: "Monto: #=kendo.toString(sum, 'n0')#"},
								{field : "Cantidad",title : 'UI_CANTIDAD',width : "100px",groupable : true, template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Cantidad',IdMoneda,'','')).valor #</label>", footerTemplate: "Cantidad: #=kendo.toString(sum, 'n0')#"}, 
				{field : "Precio",title : 'UI_PRECIO',width : "100px",groupable : true, template : " <label> #:  (util.bcs_formato(Precio,'GEN','Precio',IdMoneda,TipoInstrumento,'')).valor #</label>", footerTemplate: "Precio: #=kendo.toString(sum, 'n0')#"}, 
				{field : "Tasa",title : 'UI_TASA',width : "50px",groupable : true, template : " <label> #:  (util.bcs_formato(Tasa,'GEN','Tasa',IdMoneda,TipoInstrumento,'')).valor #</label>"}, 
								{field : "Paridad",title : 'UI_PARIDAD',width : "55px",groupable : true},
								{field : "PrecioPromedio",title : 'UI_PRECIO_PROMEDIO',width : "90px"}, 
								{field : "ParidadPromedio",title : 'UI_PARIDAD_PROMEDIO',width : "90px",groupable : true}, 
								{field : "TasaPromedio",title : 'UI_TASA_PROMEDIO',width : "90px",groupable : true}, 
								{field : "FechaVencimiento",title : 'UI_FECHA_VENCIMIENTO',width : "90px",groupable : true}, 
								{field : "FechaLiquidacion",title : 'UI_FECHA_LIQUIDACION',width : "90px",groupable : true} 
							],
						};


					// ********* fin configuracion de objetos *********//
					$scope.buscar = function() {
						$scope.apiGridResumenOperaciones.dataSource.data();
						
						if($scope.data.kngFecha == null)
							return;
						if($scope.data.oConfiguracionCliente.seleccionado.IdCliente == undefined || $scope.data.oConfiguracionCliente.seleccionado.IdCliente == false)
							return;
						
						var datos = {
							p_IdCliente : $scope.data.oConfiguracionCliente.seleccionado.IdCliente,
							p_FechaOperacion : util.dateSPdate($scope.data.kngFecha)
						}

						bcsComunes.ventanaOverlay($scope.idVentana,true);
						var p = bcsServicioOrden.ResumenOperacionesDiarias(datos).$promise;
						p.then(function(result) { 
							if(result.ListaOperaciones.length == 0){
								bcsComunes.msg("warning", "Informacion"," La consulta no obtuvo resultados");
								bcsComunes.ventanaOverlay($scope.idVentana,false);
							}
							$scope.apiGridResumenOperaciones.dataSource.data(result.ListaOperaciones);
							bcsComunes.ventanaOverlay($scope.idVentana,false);
										},
						function(result) {
								bcsComunes.msg("error", "Error","Existen problemas al ingresar la consulta");
								bcsComunes.ventanaOverlay($scope.idVentana,false);
						});
					}
					
					$scope.$watch('data.oConfiguracionCliente.seleccionado',function(newval,oldval){
						if (!$scope.data.oConfiguracionCliente.seleccionado) return;
						try{
							$scope.socioGral.idActual = $scope.data.oConfiguracionCliente.seleccionado.IdSocioDeNegocio;
						}catch(e){
						}
					});	
			
		});