angular.module('ModuloPlataforma').controller('MantenedorAutorizacionCtrl',function($scope, bcsServiciosAUT, $translate, $resource, bcsComunes){
	
	const idVentana = bcsComunes.ventanaWinId($scope); 
	$scope.optEvento = {};
	
	$scope.optEvento.selectable = "row";
	$scope.optEvento.autoBind = false;
	
	$scope.optEtapa = {};
	$scope.optEtapa.selectable = "row";
	$scope.optEtapa.autoBind = false;
	
	$scope.optAutorizado = {};
	$scope.optAutorizado.selectable = "row";
	$scope.optAutorizado.autoBind = false;
	
	$scope.tipoEvento = "";
	
	$scope.read = function(){
		let filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
		$scope.kOptions.dataSource.filter(filters);
	};
	    
    $scope.reloadGrid = function(id){
    	bcsComunes.ventanaPromesa(id).then(function(){
    		$scope.gridEtapa.dataSource.removeAll();
			$scope.gridAutorizador.dataSource.removeAll();
			$scope.seleccionEvento = undefined;
			$scope.gridTipoEvento.dataSource.read();
		});
    }
    
	$scope.etapaModal = function() {
		let id = bcsComunes.ventanaAbrir("EtapaAutorizacion",{IdTipoEvento : $scope.seleccionEvento.IdTipoEvento, Nombre: $scope.seleccionEvento.Nombre},false,false,idVentana);
		bcsComunes.ventanaPromesa(id).then(function(){
    		$scope.seleccionEtapa = undefined;
    		$scope.gridAutorizador.dataSource.removeAll();
    		$scope.gridEtapa.dataSource.read();		
		});
	};
	
	$scope.autorizadorModal = function() {
		let id = bcsComunes.ventanaAbrir("AutorizadorAutorizacion",{IdEtapa : $scope.seleccionEtapa.IdEtapa, Nombre : $scope.seleccionEtapa.Nombre },false,false,idVentana);
		bcsComunes.ventanaPromesa(id).then(function(){
    		$scope.seleccionAutorizador = undefined;
    		$scope.gridAutorizador.dataSource.read();	
		});
	};
	
	$scope.modificarEtapa = function() {
		let id = bcsComunes.ventanaAbrir("EtapaAutorizacion",{IdTipoEvento : $scope.seleccionEvento.IdTipoEvento,
			Nombre: $scope.seleccionEvento.Nombre,
			Etapa : $scope.seleccionEtapa}
		,false,false,idVentana);
		bcsComunes.ventanaPromesa(id).then(function(){
    		$scope.seleccionEtapa = undefined;
    		$scope.gridAutorizador.dataSource.removeAll();
    		$scope.gridEtapa.dataSource.read();	
		});
	};
	
	$scope.modificarAutorizador = function() {
		let id = bcsComunes.ventanaAbrir("AutorizadorAutorizacion",{
			IdEtapa : $scope.seleccionEtapa.IdEtapa,
			Nombre : $scope.seleccionEtapa.Nombre,
			Autorizador : $scope.seleccionAutorizador,
			EsAutorizacionPorRol:  $scope.seleccionAutorizador.EsAutorizacionPorRol
		},false,false,idVentana);

    	bcsComunes.ventanaPromesa(id).then(function(){
			$scope.seleccionAutorizador = undefined;
    		$scope.gridAutorizador.dataSource.read();	
		});
	};
		
	////////////////////////////MIGRACION GRILLA
	
	const transporteMantenedorAutorizaciones = function(Grid) {
		if (Grid === 'TipoEvento')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1399";
		if (Grid === 'Etapa')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1400";
		if (Grid === 'Autorizador')
			url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1401";		
		return {
			read : {
				url : url,
				dataType : "json"
			}
		}
	};  	
	
	$scope.TipoEvento = {
			dataSource		: new bcs.DataSource ({
				type 		: "odata", //Los tipos posibles son: odata, call, array y object
				definition 	: {
								serverFiltering 	: true, // Filtrado de datos en el servidor.
								serverPaging 		: true, // Paginado de los datos en el servidor.
								serverSorting 		: true, // Ordenamiento de los datos en el servidor.
								sortable 			: true,
								pageSize 			: 50,
								transport 			: transporteMantenedorAutorizaciones('TipoEvento'),
								schema 				: {
									model: {
										fields : {
											IdTipoEvento : { type: "string"},
							        		Nombre : { type: "string"},
							        		HorasVigencia : { type: "number"},
							        		EtapasSecuenciales : { type : "string" },
							        		IdUsuarioSuperAutorizador : { type : "string" },
							        		IdRolSuperAutorizador : { type : "string" },
							        		IdTipoProducto : { type : "string" },
							        		IdTipoOperacion : { type: "string"},
							        		IdEstado : { type: "string"},
							        		Codigo : { type: "string"},
							        		IdEmpresa : { type: "number"},
							        		IdModulo : { type: "string"},
										}
									}
								},
							  	filter: [
									{ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa }
							  	],
								filtersFn			: function (row) {
	                				let filterDetalle = new Array();
	                					filterDetalle.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
										return filterDetalle;
			  						}
								
							  }
			}),
			gridCreated: function(grid) {
				$scope.gridTipoEvento = grid;
			},
			columns				: [ 
		        {field: "IdTipoEvento", title: $translate.instant('UI_TIPO_EVENTO_AUTORIZACION'), width: "100px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "Nombre", title: $translate.instant('UI_NOMBRE'), width: "100px", attributes:{style:"text-align:center;"} }
    	   	    ,{field: "HorasVigencia",title: $translate.instant('UI_HORAS_VIGENCIA'), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "EtapasSecuenciales", title: $translate.instant("UI_ETAPAS_SECUENCIALES"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdUsuarioSuperAutorizador", title: $translate.instant("UI_SUPER_AUTORIZADOR"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdRolSuperAutorizador", title: $translate.instant("UI_ROL_SUPER_AUTORIZADOR"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdTipoProducto", title: $translate.instant("UI_TIPO_PRODUCTO"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdTipoOperacion", title: $translate.instant("UI_TIPO_OPERACION"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdEstado", title: $translate.instant("UI_ESTADO"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "Codigo", title: $translate.instant("UI_CODIGO"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdEmpresa", title: $translate.instant("UI_EMPRESA"), width: "70px", attributes:{style:"text-align:center;"}}
	    	   	,{field: "IdModulo", title: $translate.instant("UI_MODULO"), width: "70px", attributes:{style:"text-align:center;"}}
			],		
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //comentar
			height				: 437,
		    selectable 			: "row",
		    filterable			: true
	};
	

	$scope.Etapa = {
			dataSource		: new bcs.DataSource({
				type 		: "odata", //Los tipos posibles son: odata, call, array y object
				definition 	: {
								serverFiltering 	: true, // Filtrado de datos en el servidor.
								serverPaging 		: true, // Paginado de los datos en el servidor.
								serverSorting 		: true, // Ordenamiento de los datos en el servidor.
								sort                : { field: "IdEtapa", dir: "asc" },
								pageSize 			: 50,
								transport 			: transporteMantenedorAutorizaciones('Etapa'),
								schema 				: {
										model: {
						    	        	fields:{
						    	        		IdEtapa				: { type: "number"},
												IdTipoEvento		: { type: "string"},
												Codigo				: { type: "string"},
												Nombre				: { type: "string"},
												Orden				: { type: "number"},
												TipoAutorizacion	: { type: "string"},
												MinimoAutorizan		: { type: "number"},
												IdEstado			: { type: "string"},
												IdEmpresa			: { type: "number"},
												Estado				: { type: "string"}
						    	        	} 
									}
								},
		    					filtersFn : function (row) {
												let filterDetalle = new Array();
												filterDetalle.push({ field: "IdTipoEvento", operator: "eq", value: $scope.seleccionEtapa });
												filterDetalle.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
												return filterDetalle;
											}
								}
			}),
			gridCreated: function(grid) {
				$scope.gridEtapa = grid;
			},
			columns				:  [
				{field: "Orden", title: "UI_ORDEN", width: "50px", attributes:{style:"text-align:center;"}},
				{field: "Nombre", title: "UI_ETAPA", width: "100px", attributes:{style:"text-align:center;"}},
				{field: "IdEstado", title: "UI_ETAPA", width: "70px", attributes:{style:"text-align:center;"}, hidden:true}, 
				{field: "Codigo", title: "UI_CODIGO", width: "50px", attributes:{style:"text-align:center;"}},
				{field: "Estado", title: "UI_ESTADO", width: "70px", attributes:{style:"text-align:center;"}},
				{field: "MinimoAutorizan", title: "UI_MINIMO_AUTORIZAN", width: "70px", attributes:{style:"text-align:center;"}}
			],		
			sortable			: true,
			pageable			: true,
			autoBind			: false,  //comentar
			height				: 200,
		    selectable 			: "row"
	};
	
	$scope.Autorizador = {
			dataSource		: new bcs.DataSource({
				type 		: "odata", //Los tipos posibles son: odata, call, array y object
				definition 	: {
								serverFiltering 	: true, // Filtrado de datos en el servidor.
								serverPaging 		: true, // Paginado de los datos en el servidor.
								serverSorting 		: false, // Ordenamiento de los datos en el servidor.
								sort                : { field: "IdAutorizadoEtapa", dir: "desc" },
								pageSize 			: 50,
								transport 			: transporteMantenedorAutorizaciones('Autorizador'),
								schema 				: {
									model: {
										fields:{
											IdAutorizadoEtapa       :{type:"number"},
											IdEtapa   				:{type:"number"},
											IdUsuario 				:{type:"number"},
											CodigoUsuario 			:{type:"string"},
											IdRol 					:{type:"number"},
											NombreRol 				:{type:"string"},
											IdUsuarioReemplaza		:{type:"number"},
											CodigoUsuarioReemplaza	:{type:"string"},
											IdEstado 				:{type:"string"},
											ReemplazaDesde			:{type:"date"},
											ReemplazaHasta			:{type:"date"},
											FechaIngreso			:{type:"date"},
											Estado 					:{type:"string"},
											EsAutorizacionPorRol	:{type:"string"}
										} 
									}
								},
		    					filtersFn : function (row) {
												let filterDetalle = new Array();
												filterDetalle.push({ field: "IdEtapa", operator: "eq", value: Number(row.IdEtapa) });
												return filterDetalle;
		    					  			}							
							  }
			}),
			gridCreated: function(grid) {
				$scope.gridAutorizador = grid;
			},
			columns: [
				{field: "CodigoUsuario", title: "UI_AUTORIZADOR", width: "100px", attributes:{style:"text-align:center;"}},
				{field: "NombreRol", title: "UI_ROL", width: "70px", attributes:{style:"text-align:center;"}},
				{field: "FechaIngreso", title: "UI_FECHA_INGRESO", width: "85px", attributes:{style:"text-align:center;"}},
				{field: "IdEstado", title: "UI_ESTADO", width: "70px", attributes:{style:"text-align:center;"}, hidden:true},
				{field: "Estado", title: "UI_ESTADO", width: "70px", attributes:{style:"text-align:center;"}},
				{field: "CodigoUsuarioReemplaza", title: "UI_USUARIO_REEMPLAZA", width: "110px", attributes:{style:"text-align:center;"}},
				{field: "ReemplazaDesde", title: "UI_REEMPLAZA_DESDE", width: "100px", attributes:{style:"text-align:center;"}},
				{field: "ReemplazaDesde", title: "UI_REEMPLAZA_HASTA", width: "100px", attributes:{style:"text-align:center;"}}
			],		
			sortable			: false,
			pageable			: true,
			autoBind			: false,  //comentar
			height				: 211,
		    selectable 			: "row"
	};

	$scope.$watch('seleccionEvento', function(newValue, oldValue) {
		if (newValue===undefined || angular.equals(newValue,oldValue)) return;
		$scope.seleccionAutorizador = undefined;
		$scope.seleccionEtapa = undefined;
		if (newValue!==false) {
			if($scope.gridEtapa.dataSource !== 'undefined'){
				let filters = new Array();
				filters.push( { field: "IdTipoEvento", operator: "eq", value: $scope.seleccionEvento.IdTipoEvento });
				filters.push( { field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa  });
				$scope.gridEtapa.dataSource.filter(filters);
				
				let filtersAutorizador = new Array();
				filtersAutorizador.push({ field: "IdEtapa", operator: "eq", value: -1 })
				$scope.gridAutorizador.dataSource.filter(filtersAutorizador);
			}
		}
	},true);
	
	$scope.$watch('seleccionEtapa', function(newValue, oldValue) {
		if (newValue===undefined || angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			if($scope.gridAutorizador.dataSource !== 'undefined'){
				let filters = new Array();
				filters.push( { field: "IdEtapa", operator: "eq", value: $scope.seleccionEtapa.IdEtapa });
				$scope.gridAutorizador.dataSource.filter(filters);
			}
		}
	},true);
	
	$scope.bloquearAutorizador = function(){
		const datosBloqueo = {
			p_IdEmpresa				: $scope.SesionIdEmpresa, 
			p_IdUsuario				: $scope.SesionIdUsuario,
			p_IdModulo				: 'PLA',
			p_AutorizadoEtapa		: [{
				IdAutorizadoEtapa 		: $scope.seleccionAutorizador.IdAutorizadoEtapa, 
				IdEtapa					: $scope.seleccionEtapa.IdEtapa,
				IdUsuario				: $scope.seleccionAutorizador.IdUsuario,
				IdRol					: $scope.seleccionAutorizador.IdRol,
				IdUsuarioReemplaza		: $scope.seleccionAutorizador.IdUsuarioReemplaza,
				EsAutorizacionPorRol 	: $scope.seleccionAutorizador.EsAutorizacionPorRol,
				SoloBloquear 			: 'S',
				ReemplazaDesde 			: $scope.seleccionAutorizador.ReemplazaDesde ? util.dateSPdate($scope.seleccionAutorizador.ReemplazaDesde) : '0001-01-01',
				ReemplazaHasta 			: $scope.seleccionAutorizador.ReemplazaHasta ? util.dateSPdate($scope.seleccionAutorizador.ReemplazaHasta) : '0001-01-01'			
			}]
		};
	        
		bcsServiciosAUT.ModificarAutorizadorEtapa(datosBloqueo).$promise.then(
			function(result) {
				let mensajeId = bcsComunes.ventanaMsg({ conflictos : util.parseServerResponse(result)}, idVentana);
				bcsComunes.ventanaPromesa(mensajeId).then(function(){
					if(result.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.ventanaCerrar(mensajeId);
						$scope.seleccionAutorizador = undefined;
    					$scope.gridAutorizador.dataSource.read();
					}
				});
			},
			function(){
				bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
		});	    
	};
	
	$scope.modificarEstadoEvento = function() {
		let datos = {
			p_IdEmpresa 	: $scope.SesionIdEmpresa,
        	p_IdUsuario 	: $scope.SesionIdUsuario,
        	p_IdTipoEvento  : $scope.seleccionEvento.IdTipoEvento,
        	p_IdEstado 		: $scope.seleccionEvento.IdEstado
		};

       bcsServiciosAUT.CambiarEstadoEventoMantenedor(datos).$promise.then(
			function(result) {
				bcsComunes.ventanaMsg({ mensaje : "Estado modificado", conflictos : util.parseServerResponse(result)}, idVentana);
				$scope.gridTipoEvento.dataSource.read();
			},
			function(result){
				bcsComunes.msg("error","error","Operación con errores");
		});
	};
});





