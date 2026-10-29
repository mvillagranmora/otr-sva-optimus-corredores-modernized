angular.module('ModuloOrdenes').controller('ModalLibroPorDefectoCtrl',function($scope,bcsConfiguracionOrdenesV2,$translate,bcsComunes,bcsConsultasOrdenes,$resource) {
	
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
	
	var modlpd = this;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	modlpd.LibroPorDefecto			= {};
	modlpd.LibroPorDefectoOriginal	= {};
	modlpd.filtros = {
			IdEmpresa : { field : "IdEmpresa", operator : "eq" , value : $scope.SesionIdEmpresa }
	};
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/
	
	modlpd.aceptar = function(){
		var datos = {
			p_IdEmpresa			: $scope.$root.SesionIdEmpresa,
			p_IdUsuario			: $scope.$root.SesionIdUsuario,
			p_IdModulo			: 'ORD',
			p_UsaReglasNegocio	: 'N',
			
			p_IdLibroOrdenes	: modlpd.LibroPorDefecto.IdLibroOrdenes
		};
		
		bcsComunes.ventanaOverlay($scope.idVentana,true);
		guardarLibroPorDefecto(datos).then(
			function(result) {
				bcsComunes.ventanaOverlay($scope.idVentana,false);
				var mensaje = "";
				if (result.P_OUT_ADVERTENCIASCONTADOR){
					mensaje = "Advertencia","La operacion tiene advertencias";
				}
				else if (result.P_OUT_ERRORESCONTADOR){
					mensaje = "Error al guardar Libro Por Defecto :: " + result.P_OUT_ERRORESDETALLE[0].Codigo;
				}
				else{
					bcsComunes.msg("info","info","Libro Por Defecto guardado");
				}
				if (mensaje) {
					var p = bcsComunes.ventanaMsg({
						mensaje: mensaje,
						conflictos:util.parseServerResponse(result)
					},idVentana);

					bcsComunes.ventanaPromesa(p).then(function(){
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
					});
				} else {
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
				}
		
			},
			function(result){
				bcsComunes.ventanaData(idVentana,true);
				bcsComunes.ventanaMsg({
					mensaje: mensaje,
					conflictos:util.parseServerResponse(result)
				},idVentana);
			});
	};
	
	modlpd.cancelar = function() {
		bcsComunes.ventanaData(idVentana,false);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
/*********************************************
 * DATASOURCES
 *********************************************/
	modlpd.libroPorDefectoDatasource = new bcs.DataSource(bcsConfiguracionOrdenesV2.getDataSourceLibroPorDefecto());
	
	// Cargar Libro Por Defecto
	var filtroLibDef = [{ field: "IdEmpresa",	operator: "eq", value: $scope.SesionIdEmpresa }];
	bcsComunes.ventanaOverlay($scope.idVentana,true);
	modlpd.libroPorDefectoDatasource.query({ filter: filtroLibDef }).then(function(result){
		bcsComunes.ventanaOverlay($scope.idVentana,false);
		if(modlpd.libroPorDefectoDatasource.view().length > 0){
			modlpd.LibroPorDefecto.modoCrear = false;
			modlpd.LibroPorDefecto.IdLibroOrdenes = modlpd.libroPorDefectoDatasource.view()[0].IdLibroOrdenes;		
		}
		else{
			bcsComunes.msg("info","LIBRO_POR_DEFECTO","El Libro Por Defecto no se ha configurado.");
		}
	});
	
	var guardarLibroPorDefecto = function(p_datos){
		var urlGuardarLibroPorDefecto = "/BSCL/Plataforma/GWJAVA/PLA/call?t=223";
		var r = $resource("", {}, {'urlGuardarLibroPorDefecto': {method:"POST", url:urlGuardarLibroPorDefecto}});
		var post = r.urlGuardarLibroPorDefecto(p_datos);
		
		return post.$promise;
    };
	
//---------------------------------- FIN CONTROLLER PRINCIPAL ----------------------------
});