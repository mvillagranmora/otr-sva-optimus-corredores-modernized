angular.module('ModuloCuentaInversion').controller(
		'ConsultaAtributtionInformeController',
		[ '$scope', '$http', '$translate', '$timeout', 'bcsComunes',
				'bcsAlerta', 'OPCIONES_BCSALERTA', 'Utils', 'bcsCartolas','bcsServiciosCuentaInversion', 'bcsServiciosNormativa',
				'Model', ConsultaAtributtionInformeController ]);

function ConsultaAtributtionInformeController($scope, $http, $translate, $timeout,
		bcsComunes, bcsAlerta, OPCIONES_BCSALERTA, Utils, bcsCartolas, bcsServiciosCuentaInversion, bcsServiciosNormativa, Model) {

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
				'value' : 0
			},
			IdPortafolio : {
				'field' : 'IdPortafolio',
				'operator' : 'eq',
				'value' : 0
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
    
	$scope.MonedaInforme = {};

	$scope.MonedaInforme.filtroMoneda = {
			complejo : [$scope.filMonCLP],
			logic : "or",
			filters : [
				$scope.filtroMoneda1, $scope.filtroMoneda2, $scope.filtroMoneda3
			]
	};
    
	$scope.configuracionGrillaDescargaInformeAtributtion = {
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
		allowCopy 		: false,
		autoBind 		: false,
		userSettings 	: true,
		excel : {
			allPages 	: true
		},
		filterable 		: false,
		height 			: 260,
		pageable 		: true,
		reorderable 	: true,
		resizable		: true,
		scrollable 		: true,
		sortable 		: true
	};

	$scope.limpiarFiltros	= function() {
		$scope.IdCliente 	= null;
		$scope.IdPortafolio = null;
		$scope.AnoDesde 	= null;
		$scope.AnoHasta 	= null;
		$scope.IdGrupo 		= null;
	}

	$scope.buscarInformeAtributtion = function() {
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
				&& typeof $scope.IdPortafolio === 'number') {
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
				var data = $("#gridInformeAtributtion").find("[kendo-grid]").data("kendoGrid").dataItem($(this).parent().parent());
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
		bcsServiciosCuentaInversion.GenerarCartola(params, function(response) {
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
	
		$scope.verInformeHabilitado = true;
		
		$scope.solicitarInforme = function() {
		
		var errors = [];

		if ($scope.IdPortafolio === null
				|| typeof $scope.IdPortafolio !== 'number') {
			errors.push('Debe seleccionar un portafolio');
		}

		if ($scope.FechaHasta === null || typeof $scope.FechaHasta !== 'object') {
			errors.push('Debe seleccionar la fecha de termino');
		}

		if (errors.length > 0) {
			Utils.showErrors(errors, 'Atributtion Analysis', bcsComunes
					.ventanaWinId($scope));
			return;
		}
		
		// Validate the parameter IdGrupo if it is not selected 
		if ($scope.IdGrupo == false) {
			$scope.IdGrupo = 0;
		}
		
		$scope.verInformeHabilitado = false;
		bcsComunes.ventanaOverlay(bcsComunes
				.ventanaWinId($scope),true);
		
		var infoCliente = [{
			"IdCliente"		: $scope.ClienteSeleccionado.IdCliente,
			"IdPortafolio" 	: $scope.IdPortafolio
		}];
		
		
		if($scope.AnoHasta==null || $scope.AnoDesde==null)
		{
			errors.push('Debe Seleccionar un rango de fechas');
			Utils.showErrors(errors, 'Atributtion Analysis', bcsComunes
					.ventanaWinId($scope));
					return;
		}

		var params = {};
		params.p_IdEmpresa 		= $scope.SesionIdEmpresa;
		params.p_IdModulo 		= 'NOR';
		params.p_IdUsuario 		= $scope.SesionIdUsuario;
		params.p_FechaDesde 	= Utils.formatDate($scope.AnoDesde);
		params.p_FechaHasta 	= Utils.formatDate($scope.AnoHasta);
		params.p_Datos			= infoCliente;
	    params.p_Moneda         = $scope.MonedaInforme.idActual;
		
	    var obtenerServiciosCuentaInversion = function() {
			bcsServiciosCuentaInversion.AtributtionAnalysisInforme(params,
				function(response){		
					bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
					   if (response.P_OUT_SALIDA != null){
						   
						var datos = {}; //Datos para encabezado cartola
						datos.Atributtion =response.P_OUT_SALIDA.Atributtion;
						datos.RentabilidadClasificacion =response.P_OUT_SALIDA.RentabilidadClasificacion;
						// Muestra el nombre del Grupo seleccionado
						if ($scope.IdGrupo > 0) {
							datos.NombreSocioNegocio = $scope.GrupoPortafoliosSeleccionado.NombreGrupoPortafolio;
							datos.DocumentoIdentidad = "0-0";
						} else {
							// Muestra la informacion del cliente
							for(var i =0; i < response.P_OUT_SALIDA.datosSocioNegocio.length; i++) {
								if ($scope.ClienteSeleccionado.IdCliente == response.P_OUT_SALIDA.datosSocioNegocio[i].IdCliente) {
									datos.NombreSocioNegocio = response.P_OUT_SALIDA.datosSocioNegocio[i].NombreSocioNegocio;
									datos.DocumentoIdentidad =response.P_OUT_SALIDA.datosSocioNegocio[i].DocumentosIdentidad;
									break;								
								} 
							}							
						}
						datos.rentabilidadindice =response.P_OUT_SALIDA.RentabilidadIndices;
						datos.FechaDesde         =Utils.formatDate($scope.AnoDesde);
						datos.FechaHasta         =Utils.formatDate($scope.AnoHasta);
		
						//Abrimos ventana para consultar cartola online
						var idVentana = bcsComunes.ventanaWinId($scope);
						ventanaSolicitarAtributtionAnalysis = bcsComunes.ventanaAbrir("AtributtionInforme",datos, false, false,idVentana);
						var promesa=bcsComunes.ventanaPromesa(ventanaSolicitarAtributtionAnalysis);
				        promesa.then(function(data){
				        	$scope.verInformeHabilitado = true;
						});
					}
				},
				function(response){
					bcsComunes.ventanaOverlay(bcsComunes.ventanaWinId($scope),false);
					bcsComunes.ventanaMsg({
						conflictos : util.parseServerResponse(response)
					},bcsComunes.ventanaWinId($scope));
					$scope.verInformeHabilitado = true;
				}
			);
	    }
	    
		/* CELZORRO-467 - Get the IdClientes and IdPortafolios by IdPortafolio or IdGrupo depending of the parameters */
		if ($scope.IdPortafolio == 0 || $scope.IdGrupo > 0) {
			var datosCliente = {};
			datosCliente.p_IdEmpresa	= 	$scope.SesionIdEmpresa;
			datosCliente.p_IdCliente	= 	$scope.ClienteSeleccionado.IdCliente;
			datosCliente.p_IdPortafolio	=	$scope.IdPortafolio;
			datosCliente.p_IdGrupo		=  	$scope.IdGrupo;
			
			bcsServiciosNormativa.ConsultarGruposPortafolioCartola(datosCliente).$promise.then(function(respuesta){
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					params.p_Datos = respuesta.P_OUT_PORTAFOLIOS;	// Set the IdClientes and IdPortafolios
					obtenerServiciosCuentaInversion(params);		// Run the service with the new parameters in 'params.p_Datos'
				}
			})
		} else {
			obtenerServiciosCuentaInversion(params);				// Run the service
		}
	};
	let firstHeight = $('#Win_AtributionAnalisysConsultarInforme');
	firstHeight.css('height', '475px');

}