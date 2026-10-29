angular.module('ModuloCustodia').controller('BusquedaOperacionEspecificaCtrl',
				function($scope,$state,$resource,bcsComunes,bcsServicioPlataforma)
{		
	
	$scope.winID = bcsComunes.ventanaWinId($scope); // bcsComunes.ventanaOverlay($scope.ventanaId,true);
    $scope.winID = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams($scope.winID);
	
	$scope.grid = {};

	$scope.grid.confListaDeOperacionesVigentes = {
		dataSource  : new bcs.DataSource({
						type : "odata",
						definition : {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: false, // Ordenamiento de los datos en el servidor.
							pageSize 			: 25,
							sort 				: [],
							transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1795",dataType : "json"}},
							filter				: { logic: "and",
				                        			filters: [
																{ field: "IdEmpresa", operator: "eq", value: Number(parametros.IdEmpresa)},
																{ field: "IdCliente", operator: "eq", value: parametros.IdCliente},
																{ field: "IdPortafolio", operator: "eq", value: parametros.IdPortafolio},
																{logic: "or",
																 filters : 	[
																				{ field: "IdTipoOperacion", operator: "eq", value: parametros.IdTipoOperacion},
																				{ field: "IdTipoPrestamo", operator: "eq", value: parametros.IdTipoPrestamo},
																			]
																}
												  			]
												  },
							schema : {
								model : {
									fields : {																				
										IdCartera						: {type : "number"},
										OrigenOperacion					: {type : "string"},
										IdOperacion						: {type : "number"},
										FolioCartera					: {type : "number"},
										IdCliente						: {type : "number"},
										NombreCompleto					: {type : "string"},
										DocumentoIdentidad				: {type : "string"},
										IdPortafolio					: {type : "number"},
										CodigoPortafolio				: {type : "string"},
										DescripcionPortafolio			: {type : "string"},
										Proposito						: {type : "string"},
										IdConcepto						: {type : "string"},
										NombreConcepto					: {type : "string"},
										IdTipoInstrumento				: {type : "string"},
										IdInstrumento					: {type : "number"},
										IdTipoOperacion					: {type : "string"},
										NombreTipoOperacion				: {type : "string"},
										INGFechaOperacion				: {type : "sysdate"},
										INGFechaLiquidacion				: {type : "sysdate"},
										IdMonedaSistema					: {type : "string"},
										IdMoneda						: {type : "string"},
										INGCantidad						: {type : "number"},
										INGValorRescate					: {type : "number"},
										INGMontoATasaCompraActual		: {type : "number"},
										INGMontoATasaMercadoActual		: {type : "number"},
										IdBolsa							: {type : "string"},
									}
								}
							}
						}
					  }),
		columns 	: [
			{field : "OrigenOperacion", 			title:"UI_ORIGEN", 					width: "80px"}
			,{field : "IdOperacion", 				title:"UI_SECUENCIA_OPERACION", 			width: "80px"}
			,{field : "FolioCartera", 				title:"UI_FOLIO", 					width: "80px"}
			,{field : "NombreTipoOperacion", 		title:"UI_TIPO_OPERACION",			width: "200px"} 
			,{field : "IdMoneda", 					title:"UI_MONEDA", 					width: "50px"}
			,{field : "INGMontoATasaCompraActual", 	title:"UI_MONTO", 					width: "80px",	template : " <label> #:  (util.bcs_formato(INGMontoATasaCompraActual,'GEN','Monto',IdMoneda,'','')).valor #</label>"}
			,{field : "INGFechaLiquidacion", 		title:"UI_FECHA_LIQUIDACION",		width: "90px"}
			,{field : "NombreCompleto", 			title:"UI_CLIENTE",					width: "120px"}
			,{field : "DocumentoIdentidad", 		title:"UI_DOCUMENTO_IDENTIDAD",		width: "90px"}
			,{field : "CodigoPortafolio", 			title:"UI_PORTAFOLIO",				width: "80px"}
					  ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 270,
		selectable 	: "row",
		autoBind 	: true,
		userSettings: true
	};


	$scope.seleccionarOperacion = function(registro){
		var result = [];
		if (typeof registro === undefined ||  !registro.FolioCartera){
			return;
		}

		bcsComunes.ventanaOverlay($scope.winID,true);
		$scope.SubyacenteDataSource = new bcs.DataSource({
		type: "odata",
		definition 	: {
							serverFiltering 	: true, // Filtrado de datos en el servidor.
							serverPaging 		: true, // Paginado de los datos en el servidor.
							serverSorting 		: true, // Ordenamiento de los datos en el servidor.
							sort 				: { field: "FolioCartera", dir: "desc" }, // Criterios de Ordenamiento.
							filter				: { field: "FolioCartera", operator: "eq", value: Number(registro.FolioCartera)},
							transport 			: { read: {
														     url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1007",
														     dataType: "json"
													 	  }
												  },
							schema				: {
													model: {
														fields : {
															IdBolsa						: { type: "string"},
															suby_IdTipoInstrumento		: { type: "string"},
															suby_IdInstrumento			: { type: "number"},
														}
													}
												}
						}
		});
		$scope.SubyacenteDataSource.query().then(function(){
			var ExigibilidadSimulacion = [];
			for (i=0;i<$scope.SubyacenteDataSource.data().length;i++){
				var aux = {};
				aux.IdInterno			= i;
				aux.IdTipoInstrumento	= registro.IdTipoInstrumento;
				aux.IdInstrumento		= $scope.SubyacenteDataSource.data()[i].suby_IdInstrumento;
				aux.Cantidad			= $scope.SubyacenteDataSource.data()[i].suby_INGCantidad;
				aux.Precio				= $scope.SubyacenteDataSource.data()[i].suby_INGPrecioSubyacente;
				aux.Monto				= $scope.SubyacenteDataSource.data()[i].suby_INGMontoATasaSubyacente;
				aux.IdBolsa				= $scope.SubyacenteDataSource.data()[i].IdBolsa;

				ExigibilidadSimulacion.push(aux);
			};

			var datosExig = {
				p_IdEmpresa: $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'CUS',
				p_simularOperacion: 'N',
				p_UsaReglasNegocio: 'S',
				p_TableExigibilidadSimulacion:ExigibilidadSimulacion,
				p_IdTipoInstrumento: registro.IdTipoInstrumento,
				p_IdInstrumento: registro.IdInstrumento,
				p_IdPortafolio: registro.IdPortafolio,
				p_IdBolsa: registro.IdBolsa,
			};


			bcsServicioPlataforma.Servicios().CalcularExigibilidad(datosExig).$promise.then(
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID,false);
				result.Subyacente = respuesta.P_OUT_TABLEEXIGIBILIDADSIMULACION;
				result.Cartera = registro;
				bcsComunes.ventanaData($scope.winID,result);
				bcsComunes.ventanaCerrar($scope.winID);
			},
			function(respuesta){
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
		},function(){
			bcsComunes.ventanaData($scope.winID,[]);
			bcsComunes.ventanaCerrar($scope.winID);
		});
	};

});

