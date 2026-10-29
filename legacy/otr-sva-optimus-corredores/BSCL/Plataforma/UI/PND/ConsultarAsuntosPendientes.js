angular.module('ModuloPlataforma').controller('ConsultarAsuntosPendientesCtrl',
		function($scope,$translate,$modal,$log,
				$resource,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.modificaciones = {};	
	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';	
	
	$scope.data.inicioDia = util.date($scope.SesionFechaSistema).hour(0).minute(0).second(0).toDate();
	$scope.data.fechaDesde = $scope.data.inicioDia;
	$scope.data.fechaHasta = util.date($scope.SesionFechaSistema).add(23,'hours').minute(59).second(59).toDate();
	$scope.data.estado = "SCN";
	
	//GRILLA PRINCIPAL DE ARCHIVOS
	$scope.confGridAsuntosPendientes = {
			dataSource			: new bcs.DataSource({
									type 		: "odata", //Los tipos posibles son: odata, call, array y object
									definition 	: {
										serverFiltering 	: true, // Filtrado de datos en el servidor.
										serverPaging 		: true, // Paginado de los datos en el servidor.
										serverSorting 		: true, // Ordenamiento de los datos en el servidor.
										groupable 			: true, 
										//sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
										pageSize 			: 25, // Número de registros por página.
										filter :  // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
											[
											 	{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
											 	{ field: "IdEmpleado", operator: "eq", value: $scope.SesionIdEmpleado},
											 	{ field: "FechaVisibleDesde", operator: "gte", value: util.dateOdata($scope.data.inicioDia)},
											 	{ field: "FechaVisibleDesde", operator: "lte", value: util.dateOdata($scope.data.fechaHasta)},
											 	{ field: "FechaVisibleHasta", operator: "gte", value: util.dateOdata($scope.data.inicioDia)},
											 ]
										,
										transport 			: {
																read : {
																	url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1465",
																	dataType : "json",
																}
															},
										schema 				: {
											model: {
												fields : {
													Codigo : {type: "string"},
													IdEmpresa : {type: "string"},
													Asunto : {type: "string"},
													CodigoNotificacion : {type: "string"},
													CumplimientoAutomatico : {type: "string"},
													IdEstado : {type: "string"},
													IdDetalleAsuntoPendiente : {type: "string"},
													FechaCreacion : {type: "date"},
													FechaVisibleDesde : {type: "date"},
													FechaVisibleHasta : {type: "date"},
													IdEmpleado : {type: "string"},
													Observaciones : {type: "string"},
													TipoEntidad : {type: "string"},
													NombreTipoSocioDeNegocio : {type: "string"},
													IdEntidad : {type: "string"},
													DocumentoIdentidad: {type: "string"},
													NombreCompleto: {type: "string"},
													TipoOperacion : {type: "string"},
													IdOperacion : {type: "string"},
													Glosa : {type: "string"},
													Monto : {type: "string"},
													CompletadoAutomatico : {type: "string"},
													IdModulo : {type: "string"},
													IdEstadoDetalle : {type: "string"},
													Estado: {type: "string"}
												}
											}
										}
									}
								}),
			columns				: [
									{field: "Codigo",title: "UI_CODIGO",width: "150px", hidden:true},
									{field: "IdEmpresa",title: "UI_EMPRESA",width: "150px", hidden:true},
									{field: "Asunto",title: "UI_ASUNTO",width: "150px"},
									{field: "CodigoNotificacion",title: "UI_CODIGONOTIFICACION",width: "150px", hidden:true},
									{field: "CumplimientoAutomatico",title: "UI_CUMPLIMIENTOAUTOMATICO",width: "150px", hidden:true},
									{field: "IdEstado",title: "UI_ESTADO",width: "150px", hidden:true},
									{field: "IdDetalleAsuntoPendiente",title: "UI_IDDETALLEASUNTOPENDIENTE",width: "150px", hidden:true},
									{field: "FechaCreacion",title: "UI_FECHACREACION",width: "150px", hidden:true},
									{field: "FechaVisibleDesde",title: "UI_FECHAVISIBLEDESDE",width: "150px", hidden:true},
									{field: "FechaVisibleHasta",title: "UI_FECHAVISIBLEHASTA",width: "150px", hidden:true},
									{field: "IdEmpleado",title: "UI_EMPLEADO",width: "150px", hidden:true},
									
									{field: "TipoEntidad",title: "UI_TIPOENTIDAD",width: "150px", hidden:true},
									{field: "NombreTipoSocioDeNegocio",title: "UI_TIPO_SOCIO_NEGOCIO",width: "108px"},
									{field: "IdEntidad",title: "UI_ENTIDAD",width: "150px", hidden:true},
									{field: "DocumentoIdentidad",title: "UI_DOCUMENTO_IDENTIDAD",width: "161px"},
									{field: "NombreCompleto",title: "UI_NOMBRE",width: "161px"},
									
									{field: "TipoOperacion",title: "UI_TIPOOPERACION",width: "150px", hidden:true},
									{field: "IdOperacion",title: "UI_OPERACION",width: "150px", hidden:true},
									{field: "Glosa",title: "UI_GLOSA",width: "360px"},
									{field: "Monto",title: "UI_MONTO",width: "150px", hidden:true},
									{field: "CompletadoAutomatico",title: "UI_COMPLETADOAUTOMATICO",width: "150px", hidden:true},
									{field: "IdModulo",title: "UI_MODULO",width: "150px", hidden:true},
									{field: "IdEstadoDetalle",title: "UI_ESTADODETALLE",width: "150px", hidden:true},

									{field: "Estado", title:"UI_ESTADO", width:"82px"},
									{field: "Observaciones",title: "UI_OBSERVACIONES",width: "290px", editable:true}
									
							],
			gridCreated: function(grid) {
				$scope.data.mainGrid = grid;
			},
			save				: function(e){
				$scope.modificaciones[e.model.IdDetalleAsuntoPendiente] = (e.values.Observaciones.length > 0 )?e.values.Observaciones : " ";
			},
			userSettings		: true ,
			navigatable			: true,
			excel : {
				allPages: true,
			},
			sortable			: true,
			pageable			: false,
			filterable			: true,
			groupable           : true,
			autoBind			: true,
			height				: 300,
			selectable 			: "multiple row"
		};
	
	$scope.buscar = function(){
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa });
		

		if($scope.data.nombre)
			filters.push({ field: "NombreCompleto", operator: "contains", value: $scope.data.nombre});	
		if($scope.data.documentoIdentidad && $scope.data.documentoIdentidad!="")
			filters.push({ field: "DocumentoIdentidad", operator: "contains", value: $scope.data.documentoIdentidad.split('.').join("")});	
		
		if($scope.data.estado!=0)
			filters.push({ field: "IdEstadoDetalle", operator: "eq", value: $scope.data.estado});	
		if($scope.data.asunto)
			filters.push({ field: "Codigo", operator: "eq", value: $scope.data.asunto});	
		if ($scope.data.fechaDesde){
			var resp = util.validarRangoFecha($scope.data.fechaDesde, $scope.data.fechaHasta);
			if(resp.estado){
				bcsComunes.msg("error", "Error", $translate.instant(resp.mensaje));
				return;
			}
			filters.push({ field: "FechaVisibleDesde", operator: "gte", value: util.dateOdata($scope.data.fechaDesde)});
			filters.push({ field: "FechaVisibleDesde", operator: "lte", value: util.dateOdata($scope.data.fechaHasta)});
		}

		$scope.data.mainGrid.dataSource.filters(filters);
	};
	
	$scope.limpiarFiltros = function(){
		$scope.data.codigo = "";
		$scope.data.estado = "0";
		$scope.data.fechaDesde = "";
		$scope.data.fechaHasta = "";
	};

	$scope.comentar = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		var payload = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',	
			p_IdEmpleado: $scope.SesionIdEmpleado,
			p_Comentarios : []
		};
		$.each($scope.modificaciones, function(key, value){
			payload.p_Comentarios.push({"IdDetalle":key, "Comentario": value})
		});
		
		var urlComentarAsuntosPendientes = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1486";
		var r = $resource("", {},
	    {
			'ComentarAsuntosPendientes': { method: "POST", url: urlComentarAsuntosPendientes}
		});
		var post = r.ComentarAsuntosPendientes(payload);
		post.$promise.then(function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			
			if(result.P_OUT_ADVERTENCIASCONTADOR>0){
				bcsComunes.ventanaMsg({
					mensaje: "Existen problemas al ingresar la operacion",
					conflictos:util.parseServerResponse(result)
				},idVentana);
			}else{
				bcsComunes.msg("info",'Info',$translate.instant("UI_ASUNTOS_PENDIENTES_COMENTADOS"));	
			}
		});
		
	};
	
	$scope.cambiarEstadoAsuntoPendiente = function(operacion){
		bcsComunes.ventanaOverlay(idVentana,true);
		var op = (operacion==="completar")?"COM" : "ANU";
		
		var payload = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA',	
				p_IdEmpleado: $scope.SesionIdEmpleado,
				p_Detalle : []
			};
			$.each($scope.seleccionAsuntoPendiente, function(key, value){
				payload.p_Detalle.push({"IdDetalle":value.IdDetalleAsuntoPendiente, "Estado": op})
			});
			console.log(payload);
			
			var urlCambiarEstadoAsuntosPendientes = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1487";
			var r = $resource("", {},
		    {
				'CambiarEstadoAsuntosPendientes': { method: "POST", url: urlCambiarEstadoAsuntosPendientes}
			});
			var post = r.CambiarEstadoAsuntosPendientes(payload);
			post.$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				
				if(result.P_OUT_ADVERTENCIASCONTADOR>0){
					bcsComunes.ventanaMsg({
						mensaje: "Existen problemas al ingresar la operacion",
						conflictos:util.parseServerResponse(result)
					},idVentana);	
				}else{
					$scope.seleccionAsuntoPendiente = undefined;
					$scope.data.mainGrid.dataSource.read();

					bcsComunes.msg("info",'Info',$translate.instant("UI_ASUNTOS_PENDIENTES_"+((op==="COM")?"COMPLETADOS":"ANULADOS")));	
				}

			});
			
	};
	
	var firstHeightCrear = $('#Win_ConsultarAsuntosPendientes');
        firstHeightCrear.css('width','965px');

});	
