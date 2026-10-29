angular.module('ModuloOrdenes').controller('PrevisualizarDesasignacionCtrl',function ($scope,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenesV2) {

	let idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.aceptar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	};
 
	$scope.confAsignaciones = {
			gridCreated : function(e) {
				$scope.apiAsignaciones = e;
			},
			autoBind: true,
			height : 300,
			pageable: true,
			reorderable: true,
			resizable: true,
			selectable : "multiple, row",
			excel: {
				allPages: true
			},
			dataSource : new bcs.DataSource({
				type : "array",
				definition : {
					data : [],
					schema : bcsConfiguracionOrdenesV2.getSchemaAsignacion()
				}
				
			}),
			columns : [
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdAsignacion"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdBolsa"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdClasificacionTipoInstrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdTipoInstrumento"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreTipoCanalIngreso"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("Nemotecnico"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreLibroOrdenes"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("IdAsignacionMasiva"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("NombreCompleto"),
				bcsConfiguracionOrdenesV2.getConfiguracionColumna("CodigoPortafolio")
			]
	};

	let filtros = $.extend({},bcsComunes.ventanaParams(idVentana));
	
	let idinstrumento = filtros.instrumento && filtros.instrumento.IdInstrumento > 0?filtros.instrumento.IdInstrumento : undefined;
	let idlibro = filtros.IdLibroOrdenes > 0?filtros.IdLibroOrdenes : undefined;
	let idbolsa = filtros.IdBolsa != "" ?filtros.IdBolsa : undefined;
	let idPortafolio = filtros.IdPortafolio != "" ?filtros.IdPortafolio : undefined;
	let idclasificacion = filtros.IdClasificacionTipoInstrumento != ""?filtros.IdClasificacionTipoInstrumento : undefined;
	let idtipoinstrumento = filtros.IdTipoInstrumento != "" ?filtros.IdTipoInstrumento : undefined;
	let idaccion= filtros.AccionOperacion != "" ?filtros.AccionOperacion : undefined;
	let idasignacionmasiva = filtros.IdAsignacionMasiva && filtros.IdAsignacionMasiva >= 0 ? filtros.IdAsignacionMasiva : undefined;
	let idmoneda = filtros.IdMonedaOperacion ? filtros.IdMonedaOperacion : undefined;
	let idcondicionliquidacion = filtros.IdCondicionDeLiquidacion ? filtros.IdCondicionDeLiquidacion : undefined;
	let marcaasignacion = filtros.MarcaAsignacion && filtros.MarcaAsignacion.trim() !== "" ? filtros.MarcaAsignacion : undefined;

	let datos = {
		p_IdEmpresa 			: $scope.SesionIdEmpresa,
		p_IdUsuario 			: $scope.SesionIdUsuario,
		p_IdModulo	 			: "ORD",
		p_FechaDesde 			: util.dateSPseconddate(filtros.fechaDesde),
		p_FechaHasta 			: util.dateSPseconddate(filtros.fechaHasta),
		p_IdLibroOrdenes		: idlibro,
		p_IdClasificacionTipoInstrumento : idclasificacion,
		p_IdTipoInstrumento		: idtipoinstrumento,
		p_AccionOrden			: idaccion,
		p_IdBolsa				: idbolsa,	
		p_IdInstrumento			: idinstrumento,
		p_IdTipoCanalIngreso	: filtros.IdTipoCanalIngreso,
		p_IdPortafolio			: idPortafolio,
		p_IdAsignacionMasiva	: idasignacionmasiva,
		p_IdMonedaOperacion		: idmoneda,
		p_IdCondicionDeLiquidacion : idcondicionliquidacion,
		p_MarcaAsignacion		: marcaasignacion
	};
	
	bcsComunes.ventanaOverlay(idVentana,true);
	let p = bcsServicioOrden.PrevisualizarDesasignacion(datos).$promise;
	p.then(function(result){
		bcsComunes.ventanaOverlay(idVentana,false);
		if (result.P_OUT_ERRORESCONTADOR > 0) {
			bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result.p_out_Mensajes)
				},idVentana);
		}
		else {
			if (result.p_out_Asignaciones.length === 0) {
				bcsComunes.msg("warning","UI_ADVERTENCIA","UI_NO_SE_ENCONTRARON_ASIGNACIONES");
			}
			$scope.apiAsignaciones.dataSource.data(result.p_out_Asignaciones);
		}
	},function(result){
		bcsComunes.ventanaOverlay(idVentana,false);
		bcsComunes.ventanaMsg({
					conflictos:util.parseServerResponse(result.p_out_Mensajes)
				},idVentana);
	});
});
