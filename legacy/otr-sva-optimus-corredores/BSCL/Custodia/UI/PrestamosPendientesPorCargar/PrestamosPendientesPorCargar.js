angular.module('ModuloCustodia').controller('PrestamosPendientesPorCargarCtrl',
		function($scope, $translate, $rootScope, $timeout, bcsComunes, bcsServicioPlataforma,bcsServiciosPrestamo) {
			/** *************************************************** */
			/** * INICIO DE VARIABLES Y PRECARGA ** */
			/** *************************************************** */
			$scope.data  = {};
			$scope.winID = bcsComunes.ventanaWinId($scope);

			var idVentana = bcsComunes.ventanaWinId ($scope);
			bcsComunes.ventanaAutoOverlay(idVentana,false,3000);

			var v_fechaSistema = util.dateInicioDia($scope.SesionFechaSistema);

			/** *************************************************** */
			/** * DATASOURCES ** */
			/** *************************************************** */
			$scope.dsPrestamosPendientesPorCargar = new bcs.DataSource({
				type : "odata",
				definition : {
					pageSize : 100,
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=601195",
							dataType : "json",
						}
					},
					callResultSet : "",// "P_OUT_GARANTIASRESUMEN",
					schema : {
						model : {
							fields : {
								IdPrestamosPendientes	: { type: "number"},
								FolioPendiente      	: { type: "number"},
								TipoRegistro			: { type: "string"},
								Nemotecnico				: { type: "string"},
								FechaIngreso            : { type: "date"},
								Mercado					: { type: "string"},
								DocumentoIdentidad		: { type: "string"},
								Tipo					: { type: "string"},
								Cantidad				: { type: "string"},
								Tasa					: { type: "string"},
								CondicionLiquidacion	: { type: "string"},
								FechaLiquidacion		: { type: "date"},
								CodigoCorredora			: { type: "string"},
								FolioSistemaExterno		: { type: "string"},
								Origen					: { type: "string"},
								Mensaje					: { type: "string"},
								Estado					: { type: "string"}
							}
						}
					}
				}
			});

			$scope.columnasPrestamosPendientesPorCargar = function() {
				return [ {
					field : "FolioSistemaExterno",
					title : 'UI_FOLIO_BLOCKCHAIN',
					width : "80px"
				},{
					field : "TipoRegistro",
					title : 'UI_TIPO_REGISTRO',
					width : "70px"
				},{
					field : "Nemotecnico",
					title : 'UI_NEMO',
					width : "70px"
				},{
					field : "FechaIngreso",
					title : 'UI_FECHA_INGRESO',
					width : "70px"
				},{
					field : "Mercado",
					title : 'UI_MERCADO',
					width : "63px"
				}, {
					field : "DocumentoIdentidad",
					title : 'UI_DOCUMENTO_IDENTIDAD',
					width : "70px"
				}, {
					field : "Tipo",
					title : 'UI_TIPO',
					width : "53px"
				}, {
					field : "Cantidad",
					title : 'UI_CANTIDAD',
					width : "70px"
				}, {
					field : "Tasa",
					title : 'UI_TASA',
					width : "65px"
				}, {
					field : "CondicionLiquidacion",
					title : 'UI_CONDICION_LIQUIDACION',
					width : "65px"
				}, {
					field : "FechaLiquidacion",
					title : 'UI_FECHA_LIQUIDACION',
					width : "50px"
				}, {
					field : "CodigoCorredora",
					title : 'UI_CODIGO_CORREDORA',
					width : "75px"
				}, {
					field : "Origen",
					title : 'UI_ORIGEN',
					width : "70px"
				}, {
					field : "Mensaje",
					title : 'UI_MENSAJE',
					width : "70px"
				}, {
					field : "Estado",
					title : 'UI_ESTADO',
					width : "70px"
				} ];
			};

			/** *************************************************** */
			/** * FUNCIONES DE CARGA DE TABLAS ** */
			/** *************************************************** */
			$scope.configuracionPrestamosPendientesPorCargar = {
				dataSource : $scope.dsPrestamosPendientesPorCargar,
				columns : $scope.columnasPrestamosPendientesPorCargar(),
				sortable : true,
				filterable : true,
				pageable : true,
				autoBind : false,
				height : 470,
				selectable : "multiple row",
				userSettings : true,
				gridCreated : function(grid) {
					$scope.grilla = grid;
				},
				excel : {
					allPages : true,
				}
			};

			/** *************************************************** */
			/** * FUNCIONES ** */
			/** *************************************************** */
			$scope.buscarPrestamosPendientesPorCargar = function() {
				buscar();
			};
			buscar = function() {
				var filters = new Array();
				filters.push({  field : "TipoDocIdentidad",  operator : "eq",  value : $rootScope.SesionIdEmpresa  });
				$scope.grilla.dataSource.filter([]);
			};
		
			$scope.limpiarFiltros = function() {
				$scope.data.TipoDocumentoIdentidad = 0;
				$scope.data.DocumentoIdentidad = "";
				$scope.data.Fecha = "";
				$scope.dsPrestamosPendientesPorCargar.removeAll(); 
			};

			$scope.ReprocesarPrestamosPendientesPorCargar = function(){
				var error = 0;
				var descError = [];
				var total = "";
				$.each($scope.seleccionPrestamosPendientesPorCargar,function(key,value)
				{      		     
					
					if (util.dateSPdate($scope.seleccionPrestamosPendientesPorCargar[key].FechaIngreso) !== util.dateSPdate(v_fechaSistema)){
						descError.push("Reproceso permitido solo el dia de ingreso.");
						error++;
					}
				});
				console.log(descError);

				if (error > 0){
				
				var mensajeTotal = new Array();
					$.each(descError,function(key,value)
					{  
					mensajeTotal.push({id: $scope.seleccionPrestamosPendientesPorCargar[key].FolioPendiente , resultado: "Error en reproceso", mensaje: value});
					})

				bcsComunes.ventanaAbrir("ResumenOperacionesMasivas", { 
					tituloVentana : $translate.instant("UI_ERROR_FECHA_REPROCESA_PRESTAMOS"), 
					nombreEntidad : $translate.instant("UI_FOLIO_PENDIENTE_REPROCESA_PRESTAMOS"), 
					operaciones : mensajeTotal   
				});
					return;
				} else{
					
					fnReprocesar(); 
				}
			};

			var fnReprocesar = function(){
				var datos2 = [];
				$scope.CurrentDate = new Date();
				$.each($scope.seleccionPrestamosPendientesPorCargar,function(key,value)
				{      		
						datos2.push(
						{   
						IdPrestamosPendientes: $scope.seleccionPrestamosPendientesPorCargar[key].IdPrestamosPendientes,
						});
				});

				var datos = {};	
				bcsComunes.ventanaOverlay($scope.winID,true); 
				
				datos.p_IdEmpresa = $scope.SesionIdEmpresa;
				datos.p_IdUsuario = $scope.SesionIdUsuario;
				datos.p_IdModulo = 'CUS';
				datos.p_Funcion = 'REPROCESAR'; 
				datos.IDPRESTAMOS = datos2;
		   		bcsServiciosPrestamo.ReprocesarPrestamos
									(datos,
									function(respuesta){	  		
										bcsComunes.ventanaMsg({
											conflictos:util.parseServerResponse(respuesta)
										},$scope.winID);
										bcsComunes.ventanaOverlay($scope.winID,false);
										buscar();
									},
									function(respuesta){
										bcsComunes.ventanaMsg({
											conflictos:util.parseServerResponse(respuesta)
										},$scope.winID);
										bcsComunes.ventanaOverlay($scope.winID,false);
										buscar();
									});
			};
		});