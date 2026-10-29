/*************************************************************
 * Purpose	: Panel Cliente Portafolios
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClientePortafoliosCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsConfiguracionSociodeNegocio,
			servicioPortafolio){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var pcport = this;
		pcport.ventanaId = bcsComunes.ventanaWinId($scope);
	    bcsComunes.ventanaAutoOverlay(pcport.ventanaId,'gridPortafolios input',5000);
		pcport.nuevoPortafolio = {}, pcport.nuevoPortafolio.IdPortafolio = 0;
		var idVentana = bcsComunes.ventanaWinId($scope)
		
		pcport.crearPortafolio = function(f){
			var id1 = bcsComunes.ventanaAbrir("crearPortafolio",{ IdCliente: Number(pcport.IdCliente)},false, false, pcport.ventanaId); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
                   pcport.portafoliosOptions.dataSource.read(); 
                   pcport.portafolioSeleccionado = undefined;
                   bcsComunes.ventanaCerrar(id1);
             }); 
		};
		
		pcport.administrarPortafolio = function(f){
			var id1 = bcsComunes.ventanaAbrir("administrarPortafolio", {
				IdCliente: pcport.IdCliente,
				IdPortafolio: Number(f.IdPortafolio),
				CodigoPortafolio: f.CodigoPortafolio,
				socioNegocioPromise: $scope.admSoc.socioNegocioPromise,
				dataSocioNegocio: $scope.admSoc.dataSocioNegocio
			}, false, false, pcport.ventanaId);
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
                   pcport.portafoliosOptions.dataSource.read(); 
                   pcport.portafolioSeleccionado = undefined;
             }); 
		};
		
		pcport.activar = function(datosPortafolio){
			var winModificarEstado = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio", {
				IdEntidad : datosPortafolio.IdPortafolio, IdEstado : 'ACT', Entidad: 'PORTAFOLIO'}, false,false, pcport.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(winModificarEstado);                               
	        promesa.then(function(res){
	        	pcport.portafoliosOptions.dataSource.data([ ]);
				pcport.portafoliosOptions.dataSource.read();
				pcport.portafolioSeleccionado = undefined;
	        });
		};
		
		pcport.bloquear = function(datosPortafolio){
			var winModificarEstado = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio", {
				IdEntidad : datosPortafolio.IdPortafolio, IdEstado : 'BLO', Entidad: 'PORTAFOLIO'}, false,false, pcport.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(winModificarEstado);                               
	        promesa.then(function(res){
	        	pcport.portafoliosOptions.dataSource.data([ ]);
				pcport.portafoliosOptions.dataSource.read();
				pcport.portafolioSeleccionado = undefined;
	        });
		};

		pcport.eliminar = function(datosPortafolio){
			var winEliminarPortafolio = bcsComunes.ventanaAbrir("eliminarPortafolioSocioNegocio", {
				IdCliente: Number(pcport.IdCliente), IdEntidad : datosPortafolio.IdPortafolio}, false,false, pcport.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(winEliminarPortafolio);                               
	        promesa.then(function(res){
	        	pcport.portafoliosOptions.dataSource.data([ ]);
				pcport.portafoliosOptions.dataSource.read();
				pcport.portafolioSeleccionado = undefined;
	        });
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			pcport.IdCliente = $scope.admSoc.dataSocioNegocio.IdCliente;
			
			pcport.dsPortafolio = function () {
				return new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1265",
							dataType: "json"
						}
					},
					filter: { 
						logic: "and",
						filters: [
							{field: "IdCliente", operator: "eq", value: Number(pcport.IdCliente)},
							{field: "Proposito", operator: "ne", value: 'PROPOSITOAPV'},
							{field: "Proposito", operator: "ne", value: 'APV'}
								]
					},
					schema: {
						model: {
							fields: {
								IdPortafolio 				 : { type: "string"},
								CodigoPortafolio 			 : { type: "string"},
								Proposito 					 : { type: "string"},
								IdEstado 					 : { type: "string"},
								Estado 						 : { type: "string"},
								FechaCreacion 				 : { type: "date"},
								FechaIngreso 				 : { type: "date"},
								Alias						 : { type: "string"},
								FechaBloqueo				 : { type: "date"},
								ComentarioBloqueo			 : { type: "string"},
								IdTipoBloqueo				 : { type: "string"},
								NombreTipoBloqueo			 : { type: "string"},
								EstadoPortafolioAutorizacion : { type: "string"},
								IdEstadoPortafolioAutorizacion : { type: "string"},
								AreaCliente 				 : {type : "string"},
								FechaBloqueoTemporal         : { type: "date"},
								FechaDesbloqueoTemporal      : { type: "date"},
								IdTipoDesbloqueo             : { type: "string"},
								ComentarioDesbloqueo         : { type: "string"}
							}
						}
					}
				}
			})};
			
			pcport.portafoliosOptions = {
			dataSource: pcport.dsPortafolio(),
			sortable: false,
			reorderable: false,
			filterable: false,
			groupable: false,
			pageable: false,
			selectable: "row",	
			resizable: false,
			autoBind: true,
			scrollable: true,
			height:250,

			columns: [{
				field: "IdPortafolio",
				title: $translate.instant('UI_ID_PORTAFOLIO_ABR'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "CodigoPortafolio",
				title: $translate.instant('UI_CODIGO_PORTAFOLIO_ABR'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "Proposito",
				title: $translate.instant('UI_PROPOSITO_PORTAFOLIO_ABR'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "AreaCliente",
				title: $translate.instant('UI_AREA_CLIENTE'),
				format: "{0:d}",
				width: "150px"
			},{
				field: "EstadoPortafolioAutorizacion",
				title: $translate.instant('UI_ESTADO_PORTAFOLIO_ABR'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "Alias",
				title: $translate.instant('UI_RAZON_SOCIAL_PORTAFOLIO'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "FechaIngreso",
				title: $translate.instant('UI_FECHA_INGRESO_ABR'),
				format: "{0:d}",
				width: "150px"
			},{
				field: "NombreTipoBloqueo",
				title: $translate.instant('UI_TIPO_BLOQUEO'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "ComentarioBloqueo",
				title: $translate.instant('UI_COMENTARIO_BLOQUEO'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "FechaBloqueo",
				title: $translate.instant('UI_FECHA_BLOQUEO'),
				format: "{0:d}",
				width: "150px"
			},{
				field: "FechaBloqueoTemporal",
				title: $translate.instant('UI_FECHA_BTEMPO'),
				format: "{0:d}",
				width: "150px"
			},{
				field: "FechaDesbloqueoTemporal",
				title: $translate.instant('UI_FECHA_DESBLOQUEO_TEMPORAL'),
				format: "{0:d}",
				width: "150px"
			},{
				field: "IdTipoDesbloqueo",
				title: $translate.instant('UI_TIPO_DESBLOQUEO'),
				format: "{0:n5}",
				width: "150px"
			},{
				field: "ComentarioDesbloqueo",
				title: $translate.instant('UI_COMENTARIO_DESBLOQUEO'),
				format: "{0:n5}",
				width: "150px"
			}
			],
		};
		});
		bcsComunes.ventanaAutoHeight(idVentana);
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
