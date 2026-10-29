angular.module('ModuloPortafolio').controller('MantenedorPortafolioCtrl',
		function ($rootScope,$scope,$translate,$timeout,bcsComunes,
				OPCIONES_BCSALERTA,$resource,servicioPortafolio) {

	var portm		= this;
	portm.idVentana	= bcsComunes.ventanaWinId($scope);
	portm.win		= {};
	portm.grilla		= {};
	portm.cmbbx		= {};
	portm.filtro		= {};
	portm.fn			= {};
	
	
   	portm.grilla.Portafolio = {
		userSettings:true,
		sortable: true,
  		reorderable: true,
  		filterable: true,
		groupable: true,
		pageable: true,
		selectable: " row",
		reorderable: true,
		allowCopy: true,
		resizable: true,
  		autoBind: true,
  		scrollable: true,
  		height:450,

		dataSource: new bcs.DataSource({ 
			type : "odata",
			definition : {
				serverFiltering : true, 
				serverPaging : true, 
				serverSorting : true, 
				sort: { field: "CodigoPortafolio", dir: "desc" },
				pageSize : 200,
				transport: {
					read: {
						url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=4290678", //cambiar servicio
						dataType: "json"
					}
				},
				schema : {
					model: {
						fields : {
							IdPortafolioConfiguracionDefecto		: {type: "number"} // cambiar fields
							,Proposito								: {type: "string"}
							,CodigoPortafolio						: {type: "string"}
							
						}
					} 
				},
			},
			
		}),
	
		columns: [
			 { field: "Proposito",title: "UI_PROPOSITO_PORTAFOLIO", width: "117px"},
			 { field: "CodigoPortafolio",title: "UI_CODIGO_PORTAFOLIO" , width: "117px"}
		       
		],
		
	};
	
	portm.win.agregar = function() {
		portm.fn.abrirModal(undefined, 'Agregar');
	};
	
	portm.win.eliminar = function(portSel) {
		portm.fn.abrirModal(portSel, 'Eliminar');
	};

	portm.fn.abrirModal = function(portSel, accion){
		
		if(accion === "Agregar"){
		
			var id1 = bcsComunes.ventanaAbrir("CrearPortafolioMantenedor",{ AccionBoton : accion },false,false,portm.idVentana);
			
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			$.when(promesa).then(function(data){
				bcsComunes.ventanaOverlay(portm.idVentana,false);
				portm.grilla.Portafolio.dataSource.read();
				portm.grilla.PortSel = undefined;
			});
		}else if (accion === "Eliminar"){
			// PROCESO ELIMINAR PORTAFOLIO
			var datosEliminar = {
				p_IdEmpresa 		: $scope.$root.SesionIdEmpresa, 
				p_IdUsuario 		: $scope.$root.SesionIdUsuario,
				p_IdModulo 			: "POR",
				p_UsaReglasNegocio	: "N",
				p_Opcion			: "DELETE",
				p_Portafolios		: [{
					IdPortafolioConfiguracionDefecto 	: portSel.IdPortafolioConfiguracionDefecto,
					CodigoPortafolio				 	: portSel.CodigoPortafolio,
					Proposito							: portSel.Proposito
				}],

			};

			servicioPortafolio.AdministrarPortafolioDefecto(datosEliminar).$promise.then(
				function(result) {
					//bcsComunes.ventanaOverlay(cportm.idVentana,false);
					if(result ){
						var id = bcsComunes.ventanaMsg({ mensaje : $translate.instant(result.P_OUT_MENSAJE),
							conflictos : util.parseServerResponse(result)}, portm.idVentana);
						if(result.P_OUT_ERRORESCONTADOR === 0) {
							if(id){
								var promesa=bcsComunes.ventanaPromesa(id);                               
							    $.when(promesa).then(function(data){
							    	 $timeout(function() {
										bcsComunes.ventanaOverlay(portm.idVentana,false);
										portm.grilla.Portafolio.dataSource.read();
										portm.grilla.PortSel = undefined;
							    	 },20);
							     }); 
							}else{
								bcsComunes.ventanaOverlay(portm.idVentana,false);
								portm.grilla.PortSel = undefined;
							 }
						}
					}
				},
				function(result){
					bcsComunes.ventanaOverlay(cportm.idVentana,false);
					bcsComunes.msg("error","Error",$translate.instant('UI_ERROR'));
				}
			);
		}
		
	};
		
})