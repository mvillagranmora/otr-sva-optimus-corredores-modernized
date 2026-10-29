angular.module('ModuloMesa').controller('BlotterReservaCtrl',
		function($scope
				,$state
				,$timeout
				,$modal
				,$translate
				,bcsComunes 
				,bcsServiciosMesa
				,bcsConsultasMesa
				,bcsAlerta 
				) {


	$scope.data = {};
	$scope.grid = {};
	$scope.filtros = {};	
	$scope.IdVentana = bcsComunes.ventanaWinId ($scope);
	$scope.filtersCartera = [];
	
 
	$scope.data.oConfiguracionSocioNegocio={
			tipo : "combobox-socio-negocio",
			configuracion :{},
			idActual : false,
			seleccionado : false,
			filtros: {
				IdEmpresa : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
				EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'},
				EsNombreFantasia: { field: "EsNombreFantasia",	operator: "eq", value : 'N' },
			},
			idPostFiltro:false,
			idDefecto:false,
	};
	
	$scope.data.oMesasRF={
			tipo : "combobox-mesas",
			configuracion : {},
			filtros : { IdEstado : { "field"   : "IdEstadoRegistro", "operator": "eq", "value" : "ACT" },
					    IdTipoProducto : { "field"   : "IdTipoProducto", "operator": "eq", "value" : "MERF" }
					},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true 
		};

	$scope.data.oUsuario={
			tipo : "combobox-usuario",
			configuracion : {},
			filtros : {},
			idActual : false,
			seleccionado : false,
			idDefecto : '',
			autoCargar: true 
		};
	

	var dsCarteraReservada = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize 			: 25,								
		    transport: 			{
								read: {
										url:bcsConsultasMesa('CarteraReservada'),
										dataType: "json"
									  }
								},
			schema : {
				model : {
					fields : {										
						FolioCartera 		: {type : "number"},																		
						Nemotecnico			: {type : "string"},
						IdEmisor			: {type : "string"},
						IdTipoReajuste		: {type : "string"},
						Cantidad			: {type : "number"},
						CantidadTotalReservada 	: {type : "number"},
						CantidadReservadaCus 	: {type : "number"},
						CantidadReservadaMes 	: {type : "number"},
						CodUsrReserva 		: {type : "string"},
						IdUsrReserva 		: {type : "number"},
						CorteMinimo 		: {type : "number"},
						FechaVencimientoInstrumento	: { type: "sysdate"},
						IdReservaCus 		: {type : "number"},
						IdPortafolio 		: {type : "number"},
						IdReserva           : {type: "number"}
					}
				}
			}
		}
	}); 
	$scope.grid.confBasicaCarteraReservada = {
		dataSource  : dsCarteraReservada,
		columns 	: [	 {field : "FolioCartera", 		title:"UI_FOLIO_CARTERA",		width: "55px"}
						,{field : "Nemotecnico", 		title:"UI_NEMO", 				width: "90px"}			 
						,{field : "IdEmisor", 			title:"UI_EMISOR",				width: "90px"}
						,{field : "IdTipoReajuste", 	title:"UI_TIPO_REAJUSTE", 		width: "50px"}
						,{field : "Cantidad", 			title:"UI_CANTIDAD_DISPONIBLE", 			width: "100px", template : " <label> #:  (util.bcs_formato(Cantidad,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>"}
						//,{field : "CantidadTotalReservada",	title:"UI_CANTIDAD_RESERVA",	width: "50px", template : " <label> #:  (util.bcs_formato(CantidadTotalReservada,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>"}
						,{field : "CantidadReservadaCus",	title:"UI_CANTIDAD_RESERVA_MANUAL",	width: "100px", template : " <label> #:  (util.bcs_formato(CantidadReservadaCus,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>"}
						,{field : "CantidadReservadaMes",	title:"UI_CANTIDAD_RESERVA_OPERACION",	width: "100px", template : " <label> #:  (util.bcs_formato(CantidadReservadaMes,'GEN','Monto',IdTipoReajuste,'','')).valor #</label>"}
						,{field : "CodUsrReserva", 		title:"UI_USUARIO", 			width: "100px"}
					  ],
		contextMenu : true,
		filterable	: true,		
		sortable 	: true,
		pageable 	: true,
		height 		: 527,
		selectable 	: "multiple row",
		autoBind 	: false
	};	
	


	$scope.buscarCarteras = function(filtros) {
		// Busca las carteras de la mesa seleccionada.
		$scope.filtersCartera 	= [];
		
		if (Number($scope.data.oMesasRF.idActual) > 0){
			$scope.filtersCartera.push({ field: "IdMesa", operator: "eq", value: Number($scope.data.oMesasRF.idActual) });
			$scope.filtersCartera.push({ field: "IdPortafolio", operator: "eq", value: Number($scope.data.oMesasRF.seleccionado.IdPortafolio) });
		}
		else {
			$scope.filtersCartera.push({ field: "IdCliente", operator: "eq", value: Number($scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente) });
		}
		if ($scope.data.oUsuario.seleccionado){
			$scope.filtersCartera.push({ field: "IdUsrReserva", operator: "eq", value: Number($scope.data.oUsuario.idActual) });	
		}
										
		dsCarteraReservada.filter($scope.filtersCartera);
		//dsCarteraReservada.read();
	};


	//Cierra la ventana dado el ID
	$scope.Cancelar = function()
	{
		bcsComunes.ventanaData($scope.IdVentana);
		bcsComunes.ventanaCerrar($scope.IdVentana);  
	};	
	
	$scope.Reservar = function() {


		if ($scope.seleccionCartera === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_CARTERA'));		
		}
		else
		{	
			//Abre Modal de edición
			var info = {};
			info.Registro = $scope.seleccionCartera;
			info.Tipo = 'RES';
			
			var ventanaReserva = bcsComunes.ventanaAbrir("ReservaCartera", info, false, false,$scope.IdVentana);
			var promesa=bcsComunes.ventanaPromesa(ventanaReserva);
	
	        promesa.then(function(data){
	    		dsCarteraReservada.filters($scope.filtersCartera);
	    		//dsCarteraReservada.read();
			});
		};
	};	

	
	$scope.Liberar = function() {


		if ($scope.seleccionCartera === undefined)
		{	
			bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_CARTERA'));		
		}
		else
		{	
			
			//Abre Modal de edición
			var info = {};
			info.Registro = $scope.seleccionCartera;
			info.Tipo = 'LIB';

			if (info.Registro[0].CantidadReservada == 0 ){
				bcsComunes.msg("warning","info",$translate.instant('UI_CARTERA_NO_TIENE_RESERVA'));
			}
			else
			{
				if ( $scope.SesionUsuario !== info.Registro[0].CodUsrReserva ) {
					bcsComunes.msg("warning","info",$translate.instant('USUARIO_INVALIDO'));
				}
				else
				{
					var ventanaReserva = bcsComunes.ventanaAbrir("ReservaCartera", info, false, false,$scope.IdVentana);		
			        var promesa=bcsComunes.ventanaPromesa(ventanaReserva);
			
			        promesa.then(function(data){
			    		dsCarteraReservada.filters($scope.filtersCartera);
			    		dsCarteraReservada.read();
			     });
			};
		};
	};	
	
	
	}
	$scope.data.oConfiguracionSocioNegocio.idDefecto = $scope.SesionCorredoraIdSocioDeNegocio;
	});

