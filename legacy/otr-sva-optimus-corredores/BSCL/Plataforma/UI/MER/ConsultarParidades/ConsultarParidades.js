angular.module('ModuloPlataforma').controller('ConsultarParidadesCtrl',function($scope,bcsComunes,bcsServicioPlataforma,$translate){
	
	$scope.filtros = {};
	$scope.formData = {};
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	var datosNuevos = {};

	$scope.filtros.FechaHasta = util.dateDS($scope.SesionFechaSistema,"FechaSistema","CALL");
	$scope.filtros.FechaDesde =  new Date($scope.filtros.FechaHasta.getTime() - 30 * 24 * 60 * 60 * 1000);
	
	
	$scope.formData.buscar = function(filtros) {
		var filters = new Array();
		datosNuevos = {};
		if ($scope.filtros.FechaDesde){
			var resp = util.validarRangoFecha($scope.filtros.FechaDesde, $scope.filtros.FechaHasta,365);
			if(resp.estado){
				bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
				return;
			}
			filters.push({ field: "Fecha", operator: "gte", value: util.dateInicioDia($scope.filtros.FechaDesde)});
		}
		if ($scope.filtros.FechaHasta)
			filters.push({ field: "Fecha", operator: "lte", value: util.dateTerminoDia($scope.filtros.FechaHasta)});
		if ($scope.filtros.IdMoneda)
			filters.push({ field: "IdMoneda", operator: "eq", value: $scope.filtros.IdMoneda});
		if ($scope.filtros.IdTipoRegistro)
			filters.push({ field: "IdTipoRegistro", operator: "eq", value: $scope.filtros.IdTipoRegistro});

		$scope.formData.apiGrid.dataSource.filter(filters);
	};
	
	$scope.formData.ingresar = function() {
		
		var id = bcsComunes.ventanaAbrir("CrearParidadTipoCambio",{});
		var p = bcsComunes.ventanaPromesa(id);
		p.then(function(data){
			if (data && data.guardar) {
				$scope.formData.buscar($scope.filtros);
			}
		});
		
	};
	
	$scope.formData.guardar = function(iii) {
		
		var datos = {
			p_IdEmpresa 		: $scope.SesionIdEmpresa,
    		p_IdUsuario 		: $scope.SesionIdUsuario,
    		p_IdModulo 			: 'PLA',
    		p_simularOperacion 	: 'N',
			p_UsaReglasNegocio 	: 'S',
			p_Registros : []
		};
	
		for (var i = 0;i< Object.keys(datosNuevos).length;i++) {
			var key = Object.keys(datosNuevos)[i];
			datos.p_Registros.push({
				
				Fecha					: datosNuevos[key]["Fecha"],
				IdTipoRegistro			: datosNuevos[key]["IdTipoRegistro"],
				IdMoneda 				: datosNuevos[key]["IdMoneda"],
				ParidadMonedaCambio 	: datosNuevos[key]["ParidadMonedaCambio"] || null
			})
		}

		if (datos.p_Registros.length === 0) {
			bcsComunes.msg("","UI_INFORMACION","UI_NO_HAY_REGISTROS_MODIFICADOS");
			return;
		}

		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var p = bcsServicioPlataforma.Servicios().ModificarParidadYTipoCambio(datos).$promise;
		p.then(function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			var idP = bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_REGISTROS_MODIFICADOS"),
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
			var p = bcsComunes.ventanaPromesa(idP);
			p.then(function(result){
				$scope.formData.buscar($scope.filtros);
			})
		},
		function(result){
			bcsComunes.ventanaOverlay($scope.idVentana,false);
			bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_GUARDAR_REGISTROS"),
					conflictos:util.parseServerResponse(result)
				},$scope.idVentana);
		}
		);
	};
	
	

	$scope.formData.defaultOptions = {
			allowCopy: true,
			autoBind: false,
			userSettings : true,
			navigatable : true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	height:350,
	      	pageable : true,
	      	reorderable: true,
			resizable: true,
			scrollable: true,
	      	selectable: "multiple, row",
			sortable: true,
			save :function(e) {
				var fecha = util.dateSPdate(e.model.Fecha);
				if (!datosNuevos[fecha +"-"+e.model.IdTipoRegistro+"-"+e.model.IdMoneda]) datosNuevos[fecha +"-"+e.model.IdTipoRegistro+"-"+e.model.IdMoneda] = {};
				datosNuevos[fecha +"-"+e.model.IdTipoRegistro+"-"+e.model.IdMoneda] = {
					Fecha : fecha,
					IdMoneda : e.model.IdMoneda,
					IdTipoRegistro : e.model.IdTipoRegistro,
					ParidadMonedaCambio : e.values["ParidadMonedaCambio"],
				}

			},
			columns : [
			           {field: "Fecha",								title : "UI_FECHA",				width: "75px",groupable: true},
			           {field: "IdMoneda",							title : "UI_MONEDA",			width: "75px",groupable: true},
			           {field: "NombreMoneda",						title : "UI_NOMBRE_MONEDA",		width: "125px",groupable: true},
			           {field: "IdTipoRegistro",					title : "UI_TIPO_REGISTRO",		width: "75px",groupable: true},
			           {field: "ParidadMonedaCambio",				title : "UI_PARIDAD",			width: "75px",editable: true},
			           {field: "CodigoUsuarioUltimaModificacion",	title : "UI_ULTIMO_USUARIO_MODIFICADOR",width: "100px",groupable: true},
			           {field: "FechaUltimaModificacion",			title : "UI_FECHA_ULTIMA_MODIFICACION",width: "100px",groupable: true}
				           
			           ],
			dataSource : new bcs.DataSource({
	      		type : 'odata',
	      		definition : {
					serverFiltering: true,
					serverPaging: true,
					serverSorting: true,
					sort: [
					       { field: "Fecha", dir: "desc" }
					],
					pageSize:250,
					schema: {
						model : {
							fields : {
								Fecha  							: { type: "sysdate"},
								IdMoneda  						: { type: "string"},
								NombreMoneda  					: { type: "string"},
								IdTipoRegistro  				: { type: "string"},
								ParidadMonedaCambio  			: { type: "number"},
								CodigoUsuarioUltimaModificacion	: { type: "string"},
								FechaUltimaModificacion  		: { type: "sysdatetime"}
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1512",
							dataType: "json"
						}
					}
				}
	      	}),

	};	
});
