angular.module('ModuloNormativa').controller(
		'ConsultaCartolasConsolidadaController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils', 'bcsCartolas','bcsServiciosCartola', 'bcsServiciosNormativa',
				'Model', ConsultaCartolasConsolidadaController ]);

function ConsultaCartolasConsolidadaController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, bcsCartolas, bcsServiciosCartola, bcsServiciosNormativa, Model) {

	$scope.FechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
	
	
	var dataSourceDescargaCartolas = bcsCartolas
			.getDataSourceDescargaCartolas($scope.SesionIdEmpresa);

	$scope.configSocioNegocio = {
		filtros : {
			IdEmpresa : {
				'field' : 'IdEmpresa',
				'operator' : 'eq',
				'value' : $scope.SesionIdEmpresa
			},
			
			EsCliente : {
				'field' : 'EsCliente',
				'operator' : 'eq',
				'value' : 'UI_SI'
			}
		},
	};

	$scope.configPortafolio = {
		filtros : {
			IdCliente : {
				'field' : 'IdCliente',
				'operator' : 'eq',
				'value' : undefined
			}
		},
	};

	$scope.configGrupoPortafolio = {
		filtros : {
			IdCliente : {
				'field' : 'IdCliente',
				'operator' : 'eq',
				'value' : undefined
			}
		},
	};

   	$scope.filtroMoneda1 = { "field"   : "IdMoneda","operator": "eq", "value" : "CLP" };
	$scope.filtroMoneda2 = { "field"   : "IdMoneda","operator": "eq", "value" : "USD" };
	$scope.filtroMoneda3 = { "field"   : "IdMoneda","operator": "eq", "value" : "UF" };
    
	$scope.MonedaCartola = {};

	$scope.MonedaCartola.filtroMoneda = {
			complejo : [$scope.filMonCLP],
			logic : "or",
			filters : [
				$scope.filtroMoneda1, $scope.filtroMoneda2, $scope.filtroMoneda3
			]
	};
    
	$scope.configuracionGrillaDescargaCartolas = {
		dataSource : dataSourceDescargaCartolas,
		columns : [ 
			{field : 'Periodo',title : 'Periodo',width : '100px'}, 
			{field : 'IdEmpresa',title : 'IdEmpresa',width : '100px', hidden:true}, 
			{field : 'IdCartola',title : 'DESCARGAR',width : '100px',template : kendo.template("#if (IdCartola === '0' || IdCartola === '') {#Sin Errores#}else{#<a class='downloadOffline'>Descargar</a> #}#") },
			{field : 'IdNomina',title : 'IdNomina',width : '100px', hidden:true}, 
			{field : 'IdCliente',title : 'IdCliente',width : '100px', hidden:true}, 
			{field : 'IdPortafolio',title : 'IdPortafolio',width : '100px', hidden:true}, 
			{field : 'CodigoPortafolio',title : 'CodigoPortafolio',width : '100px', hidden:true}, 
			{field : 'Reprocesar',title : 'Reprocesar',width : '100px', hidden:true}, 
			{field : 'Notificar',title : 'Notificar',width : '100px', hidden:true}, 
			{field : 'EstadoProceso',title : 'EstadoProceso',width : '100px', hidden:true}, 
			{field : 'MensajeProceso',title : 'MensajeProceso',width : '100px', hidden:true}, 
			{field : 'DocumentoIdentidad',title : 'DocumentoIdentidad',width : '100px', hidden:true}, 
			{field : 'NombreCliente',title : 'NombreCliente',width : '100px', hidden:true}, 
			{field : 'DireccionElectronica',title : 'DireccionElectronica',width : '100px', hidden:true}
			],
		allowCopy : false,
		autoBind : false,
		userSettings : true,
		excel : {
			allPages : true
		},
		filterable : false,
		height : 260,
		pageable : true,
		reorderable : true,
		resizable : true,
		scrollable : true,
		sortable : true
	};

	$scope.limpiarFiltros = function() {
		$scope.IdCliente = null;
		$scope.IdPortafolio = null;
		$scope.AnoDesde = null;
		$scope.AnoHasta = null;
		$scope.IdGrupo = null;
	}

	$scope.buscarCartolas = function() {
		var errors = [];

		if ($scope.IdPortafolio === null
				|| typeof $scope.IdPortafolio !== 'number') {
			errors.push('Debe seleccionar un portafolio');
		}

		if ($scope.AnoDesde !== null && typeof $scope.AnoDesde === 'string'
				&& $scope.AnoHasta !== null
				&& typeof $scope.AnoHasta === 'string') {
			if ($scope.AnoDesde > $scope.AnoHasta)
				errors.push('Rango de años inválido');
		} else if ($scope.AnoDesde === false ||  $scope.AnoHasta === false) {
			errors.push('Rango de años inválido');
		}

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
			return;
		}

		var filters = new Array();

		filters.push({
			field : 'IdEmpresa',
			operator : 'eq',
			value : $scope.SesionIdEmpresa
		});

		if ($scope.IdCliente !== null && typeof $scope.IdCliente === 'number') {
			filters.push({
				field : 'IdCliente',
				operator : 'eq',
				value : $scope.ClienteSeleccionado.IdCliente
			});
		}

		if ($scope.IdPortafolio !== null
				&& typeof $scope.IdPortafolio === 'number' && $scope.IdPortafolio > 0) {
			filters.push({
				field : 'IdPortafolio',
				operator : 'eq',
				value : $scope.IdPortafolio
			});
		}
		
		if ($scope.AnoDesde !== null && typeof $scope.AnoDesde === 'string') {
			filters.push({
				field : 'Periodo',
				operator : 'gte',
				value : $scope.AnoDesde + '-01'
			});
		}

		if ($scope.AnoHasta !== null && typeof $scope.AnoHasta === 'string') {
			filters.push({
				field : 'Periodo',
				operator : 'lte',
				value : $scope.AnoHasta + '-12'
			});
		}
		
		filters.push({
			field : 'TipoProceso',
			operator : 'neq',
			value : 'ONLINE'
		});

		dataSourceDescargaCartolas.query({ filter: filters}).then(function(){
			$("a.downloadOffline").on("click",function(e){
				var data = $("#gridDescargaCartolas").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent());
				$scope.obtenerOffline(data);
			});	

		});;
	}

	$scope.obtenerOffline = function(dataItem) {
		var params = {};
		bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),true);
		params.p_IdEmpresa = $scope.SesionIdEmpresa;
		params.p_IdUsuario = $scope.SesionIdUsuario;
		params.p_IdCartola = dataItem.IdCartola;
		params.p_AccionNomina	= 'OFFLINE';
		bcsServiciosCartola.GenerarCartola(params, function(response) {
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
			if (response.P_OUT_CARTOLAS != null && response.P_OUT_CARTOLAS[0]){
				var leBlob = util.b64toBlob(response.P_OUT_CARTOLAS[0].CartolaEncoded,'application/pdf',512);
				saveAs(leBlob, response.P_OUT_CARTOLAS[0].NombreArchivo);
			}
		}, function(response) {
			bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
			Utils.showError(response, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
		});
	}

	$scope.verCartolaHabilitado = true;
	
	$scope.obtenerOnline = function(dataItem) {
		var errors = [];

		if ($scope.IdPortafolio === null
				|| typeof $scope.IdPortafolio !== 'number') {
			errors.push('Debe seleccionar un portafolio');
		}

		if ($scope.FechaHasta === null || typeof $scope.FechaHasta !== 'object') {
			errors.push('Debe seleccionar la fecha de termino');
		}

		var fh = Utils.formatDate($scope.FechaHasta);
		var fd = fh.substring(0, 8) + '01';

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
			return;
		}

		$scope.verCartolaHabilitado = false;
		bcsComunes.ventanaOverlay(bcsComunes
				.ventanaWinId($scope),true);
		
		var aux = [{
			"IdCliente"		: $scope.ClienteSeleccionado.IdCliente,
			"IdPortafolio" 	: $scope.IdPortafolio
		}];
		
		var params = {};
		params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		params.p_IdModulo 		= 'NOR';
		params.p_IdUsuario 		= $scope.SesionIdUsuario;
		params.p_FechaDesde 	= fd;
		params.p_FechaHasta 	= fh;
		params.p_IdCliente		= $scope.ClienteSeleccionado.IdCliente;
		params.p_IdPortafolio	= $scope.IdPortafolio;
		
		bcsServiciosCartola.InformacionCliente(params,
			function(response){
				if(response && response.clienteTieneInformacion){
					var params = {};
					params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
					params.p_IdModulo 		= 'NOR';
					params.p_IdUsuario 		= $scope.SesionIdUsuario;
					params.p_AccionNomina	= 'ONLINE';
					params.p_TipoProceso	= 'ONLINE';
					params.p_FechaDesde 	= fd;
					params.p_FechaHasta 	= fh;
					params.p_Clientes		= aux;

					bcsServiciosCartola.GenerarCartola(params,
						function(response){		
							bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
							if (response.P_OUT_CARTOLAS != null && response.P_OUT_CARTOLAS[0]){
								var leBlob = util.b64toBlob(response.P_OUT_CARTOLAS[0].CartolaEncoded,'application/pdf',512);
								saveAs(leBlob, response.P_OUT_CARTOLAS[0].NombreArchivo);
							}
							$scope.verCartolaHabilitado = true;
						},
						function(response){
							bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
							bcsComunes.ventanaMsg({
								conflictos : util.parseServerResponse(response)
							},bcsComunes.ventanaWinId($scope));
							$scope.verCartolaHabilitado = true;
						}
					);
				} else {
					bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("CLIENTE_SIN_INFORMACION")
					},bcsComunes.ventanaWinId($scope));
					$scope.verCartolaHabilitado = true;
					$scope.FechaHasta = util.dateInicioDia($scope.SesionFechaSistema);
				}
			},
			function(response){
				bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
				bcsComunes.ventanaMsg({
					conflictos : util.parseServerResponse(response)
				},bcsComunes.ventanaWinId($scope));
				$scope.verCartolaHabilitado = true;
			}
		);
	}
	
	//Abrir ventana de cartola Online
	$scope.solicitarCartolaOnline = function() {
		//validaciones de ventana
		var errors = [];

		if ($scope.IdPortafolio === null
				|| typeof $scope.IdPortafolio !== 'number') {
			errors.push('Debe seleccionar un portafolio');
		}

		if ($scope.FechaHasta === null || typeof $scope.FechaHasta !== 'object') {
			errors.push('Debe seleccionar la fecha de termino');
		}

		
		if (errors.length > 0) {
			Utils.showErrors(errors, 'Consulta de Cartolas', bcsComunes
					.ventanaWinId($scope));
			return;
		}
		
		// Validate the parameter IdGrupo if it is not selected 
		if ($scope.IdGrupo == false) {
			$scope.IdGrupo = 0;
		}

		//Preparar data para mostrar cartola (HTML)
		
		$scope.verCartolaHabilitado = false;
		bcsComunes.ventanaOverlay(bcsComunes
				.ventanaWinId($scope),true);
		
		var infoCliente = [{
			"IdCliente"		: $scope.ClienteSeleccionado.IdCliente,
			"IdPortafolio" 	: $scope.IdPortafolio
		}];
		
		var fh = Utils.formatDate($scope.FechaHasta);
		var fd = fh.substring(0, 8) + '01';
		
		var params = {};
		params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		params.p_IdModulo 		= 'NOR';
		params.p_IdUsuario 		= $scope.SesionIdUsuario;
		params.p_FechaDesde 	= fd;
		params.p_FechaHasta 	= fh;
		params.p_Datos			= infoCliente;
		params.p_Moneda			= $scope.MonedaCartola.idActual;
		
		var obtenerDataCartola = function() {
			bcsServiciosCartola.ObtieneDataCartola(params,
					function(response){		
						bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
						if (response.P_OUT_SALIDA != null){
							
							var datos = {}; //Datos para encabezado cartola
							for(var i =0; i < response.P_OUT_SALIDA.datosSocioNegocio.length; i++) {
								if ($scope.ClienteSeleccionado.IdCliente == response.P_OUT_SALIDA.datosSocioNegocio[i].IdCliente) {
									
									datos.NombreSocioNegocio = response.P_OUT_SALIDA.datosSocioNegocio[i].NombreSocioNegocio;
									datos.DocumentoIdentidad =response.P_OUT_SALIDA.datosSocioNegocio[i].DocumentosIdentidad;
									datos.NombreEjecutivo = response.P_OUT_SALIDA.datosSocioNegocio[i].NombreEjecutivo;
									datos.Telefono = response.P_OUT_SALIDA.datosSocioNegocio[i].Telefono;
									if($scope.IdPortafolio && !$scope.IdGrupo){
										datos.CodigoPortafolio = response.P_OUT_SALIDA.datosSocioNegocio[i].CodigoPortafolio;	
									}else{
										datos.CodigoPortafolio = 'TODOS';
									}
									
									break;								
								} 
							}
							if($scope.IdGrupo){
								datos.IdGrupo = $scope.GrupoPortafolioSeleccionado.IdGrupo;
								datos.NombreGrupoCliente = $scope.GrupoPortafolioSeleccionado.NombreGrupoPortafolio;
							}
							datos.MonedaCartola = $scope.MonedaCartola.idActual;
							datos.AliasPortafolio = $scope.PortafolioSeleccionado.KdataTextTemplate;
							datos.fh = fh;
							datos.fd = fd;
							datos.p_Datos = params.p_Datos;
							datos.MovimientosTitulos = response.P_OUT_SALIDA.movimientosTitulos;
							datos.MovimientosCaja = response.P_OUT_SALIDA.movimientosCaja;
							datos.CarteraInversiones=response.P_OUT_SALIDA.carteraInversiones;
							datos.ComposicionCartera = response.P_OUT_SALIDA.composicionCartera;
							datos.FlujoPatrimonial = response.P_OUT_SALIDA.flujoPatrimonial;
							datos.FechaInicioFlujoPatrimonial = response.P_OUT_SALIDA.flujoPatrimonial[2].fechaInicioCuenta;
							datos.DistribucionPorClaseActivo = response.P_OUT_SALIDA.distribucionPorClaseActivo;
							datos.ExposicionPorMoneda = response.P_OUT_SALIDA.exposicionPorMoneda; 
							datos.FechaActual = fh;
							datos.LogoCorredora = response.P_OUT_SALIDA.datosConfCorredora[0].logoCorredora;
							
							
							//Abrimos ventana para consultar cartola online
							var idVentana = bcsComunes.ventanaWinId($scope);
							ventanaSolicitarCartolaOnline = bcsComunes.ventanaAbrir("SolicitarCartolaOnline",datos, false, false,idVentana);
							var promesa=bcsComunes.ventanaPromesa(ventanaSolicitarCartolaOnline);
					        promesa.then(function(data){
					        	$scope.verCartolaHabilitado = true;
							});
						}
					},
					function(response){
						bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
						bcsComunes.ventanaMsg({
							conflictos : util.parseServerResponse(response)
						},bcsComunes.ventanaWinId($scope));
						$scope.verCartolaHabilitado = true;
					}
			);			
		}
		
		/* CELZORRO-464 - Get the IdClientes and IdPortafolios by IdPortafolio or IdGrupo depending of the condition */
		if ($scope.IdPortafolio == 0 || $scope.IdGrupo > 0) { 
			var datosCliente = {};
			datosCliente.p_IdEmpresa	= 	$scope.SesionIdEmpresa;
			datosCliente.p_IdCliente	= 	$scope.ClienteSeleccionado.IdCliente;
			datosCliente.p_IdPortafolio	=	$scope.IdPortafolio;
			datosCliente.p_IdGrupo		=  	$scope.IdGrupo;
			
			bcsServiciosNormativa.ConsultarGruposPortafolioCartola(datosCliente).$promise.then(function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){ 
					params.p_Datos = respuesta.P_OUT_PORTAFOLIOS;	// Set the IdClientes and IdPortafolios
					obtenerDataCartola(params);						// Run the service with the new parameters in params.p_Datos
				}
			})
		} else {
			obtenerDataCartola(params);		// Run the service
		}
		
	};
	

}