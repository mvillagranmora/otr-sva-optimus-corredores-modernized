angular.module('ModuloPlataforma').controller('MantenedorParamtroFinancieroCtrl',function($scope,bcsComunes,bcsServicioPlataforma,$translate){
	
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
		if ($scope.filtros.IdParametroFinanciero)
			filters.push({ field: "IdParametroFinanciero", operator: "eq", value: $scope.filtros.IdParametroFinanciero});

		$scope.formData.apiGrid.dataSource.filter(filters);
	};
	
	$scope.formData.ingresar = function() {
		
		var id = bcsComunes.ventanaAbrir("CrearParametroFinanciero",{});
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
				IdParametroFinanciero	: datosNuevos[key]["IdParametroFinanciero"],
				Valor 					: datosNuevos[key]["Valor"] || null
			})
		}

		if (datos.p_Registros.length === 0) {
			bcsComunes.msg("","UI_INFORMACION","UI_NO_HAY_REGISTROS_MODIFICADOS");
			return;
		}

		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var p = bcsServicioPlataforma.Servicios().ModificaParametroFinanciero(datos).$promise;
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
				if (!datosNuevos[fecha +"-"+e.model.IdParametroFinanciero]) datosNuevos[fecha +"-"+e.model.IdParametroFinanciero] = {};
				datosNuevos[fecha +"-"+e.model.IdParametroFinanciero] = {
					Fecha : fecha,
					IdParametroFinanciero : e.model.IdParametroFinanciero,
					Valor : e.values["Valor"],
				}

			},
			columns : [
			           {field: "Fecha",								title : "UI_FECHA",				width: "75px",groupable: true},
			           {field: "IdParametroFinanciero",				title : "UI_PARAMETRO",			width: "75px",groupable: true},
			           {field: "AgnoMes",							title : "UI_EJERCICIO",			width: "125px",groupable: true},
			           {field: "Agno",								title : "UI_ANIO",				width: "75px",groupable: true},
			           {field: "Valor",								title : "UI_VALOR",				width: "75px",editable: true},
			           {field: "FechaActualizacion",				title : "UI_FECHA_ULTIMA_MODIFICACION",width: "100px",groupable: true}
				           
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
								IdParametroFinanciero  			: { type: "string"},
								AgnoMes  						: { type: "string"},
								Agno  							: { type: "string"},
								Valor							: { type: "number"},
								FechaActualizacion  			: { type: "sysdatetime"}
							}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4257",
							dataType: "json"
						}
					}
				}
	      	}),

	};	
});
