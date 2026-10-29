/*************************************************************
 * Purpose	: Panel Cliente Portafolios
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelClientePortafoliosCotitularesCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			bcsConfiguracionSociodeNegocio,
			servicioPortafolio){
	
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var pcport = this;
		pcport.ventanaId = bcsComunes.ventanaWinId($scope);
	    bcsComunes.ventanaAutoOverlay(pcport.ventanaId,'gridPortafolios input',5000);
		pcport.nuevoPortafolio = {}, pcport.nuevoPortafolio.IdPortafolio = 0;
		
		pcport.administrarPortafolio = function(f){
			var id1 = bcsComunes.ventanaAbrir("administrarPortafolio", {
				IdCliente: f.IdCliente, IdPortafolio: f.IdPortafolio, socioNegocioPromise: $scope.admSoc.socioNegocioPromise,
				dataSocioNegocio: $scope.admSoc.dataSocioNegocio
			}, false, false, pcport.ventanaId);
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
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
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=3555",
							dataType: "json"
						}
					},
					filter: { field: "IdClienteOrigen", operator: "eq", value: Number(pcport.IdCliente) },
					schema: {
						model: {
							fields: {
								IdCliente 			: { type: "number"},
								IdPortafolio 		: { type: "number"},
								CodigoPortafolio 	: { type: "string"},
								Proposito 			: { type: "string"},
								Estado 				: { type: "string"},
								NombreCompleto 		: { type: "string"},
								DocumentoIdentidad 	: { type: "string"},
								FechaIngreso 		: { type: "date"},
								
								
							
								
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
	
			columns: [
				{ field: "NombreCompleto",		title: 'UI_NOMBRE',					width: "150px"},
				{ field: "DocumentoIdentidad",	title: 'UI_DOCUMENTO_IDENTIDAD',	width: "150px"},
				{ field: "IdPortafolio",		title: 'UI_ID_PORTAFOLIO',			width: "150px"},
				{ field: "CodigoPortafolio",	title: 'UI_CODIGO',					width: "150px"},
				{ field: "Proposito",			title: 'UI_PROPOSITO',			width: "150px"},
				{ field: "Estado",				title: 'UI_ESTADO',				width: "150px"},
				{ field: "FechaIngreso",		title: 'UI_FECHA_INGRESO'}
			],
		};
		});
		
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------
