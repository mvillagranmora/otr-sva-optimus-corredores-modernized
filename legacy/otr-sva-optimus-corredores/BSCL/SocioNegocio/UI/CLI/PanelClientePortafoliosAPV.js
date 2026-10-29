/*************************************************************
 * Purpose	: Panel Cliente Portafolios APV
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 30/10/2017 
 *************************************************************/
(function() {
	
	angular.module('ModuloSocioNegocio').controller('PanelClientePortafoliosAPVCtrl',function($scope,
			$translate,
			servicioSociodeNegocio,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsConfiguracionSociodeNegocio,
			servicioPortafolio)
	{
	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		var pcportapv = this;
		pcportapv.ventanaId = bcsComunes.ventanaWinId($scope);
	    bcsComunes.ventanaAutoOverlay(pcportapv.ventanaId,'gridPortafolios input',5000);
		pcportapv.nuevoPortafolio = {}, pcportapv.nuevoPortafolio.IdPortafolio = 0;
		var idVentana = bcsComunes.ventanaWinId($scope)
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		pcportapv.crearPortafolio = function(f){
			var id1 = bcsComunes.ventanaAbrir("crearPortafolio",{ IdCliente: Number(pcportapv.IdCliente), TipoPortafolio: 'APV'},false, false, pcportapv.ventanaId); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
                   pcportapv.portafoliosOptions.dataSource.read(); 
                   pcportapv.portafolioSeleccionado = undefined;
                   bcsComunes.ventanaCerrar(id1);
             }); 
		};
		
		pcportapv.administrarPortafolio = function(f){
			var id1 = bcsComunes.ventanaAbrir("administrarPortafolio", {
				IdCliente: pcportapv.IdCliente,
				IdPortafolio: Number(f.IdPortafolio),
				TipoPortafolio: 'APV',
				Producto: pcportapv.portafolioSeleccionado.Producto,
				socioNegocioPromise: $scope.admSoc.socioNegocioPromise,
				dataSocioNegocio: $scope.admSoc.dataSocioNegocio
			}, false, false, pcportapv.ventanaId);
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(data){ 
                   pcportapv.portafoliosOptions.dataSource.read(); 
                   pcportapv.portafolioSeleccionado = undefined;
             }); 
		};
		
		pcportapv.activar = function(datosPortafolio){
			var winModificarEstado = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio", {
				IdEntidad : datosPortafolio.IdPortafolio, IdEstado : 'ACT', Entidad: 'PORTAFOLIO'}, false,false, pcportapv.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(winModificarEstado);                               
	        promesa.then(function(res){
	        	pcportapv.portafoliosOptions.dataSource.data([ ]);
				pcportapv.portafoliosOptions.dataSource.read();
				pcportapv.portafolioSeleccionado = undefined;
	        });
		};
		
		pcportapv.bloquear = function(datosPortafolio){
			var winModificarEstado = bcsComunes.ventanaAbrir("modificarEstadoSocioNegocio", {
				IdEntidad : datosPortafolio.IdPortafolio, IdEstado : 'BLO', Entidad: 'PORTAFOLIO'}, false,false, pcportapv.ventanaId);
			var promesa = bcsComunes.ventanaPromesa(winModificarEstado);                               
	        promesa.then(function(res){
	        	pcportapv.portafoliosOptions.dataSource.data([ ]);
				pcportapv.portafoliosOptions.dataSource.read();
				pcportapv.portafolioSeleccionado = undefined;
	        });
		};
		
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
		
		$scope.admSoc.socioNegocioPromise.then(function()
		{
			pcportapv.IdCliente = $scope.admSoc.dataSocioNegocio.IdCliente;
			pcportapv.dsPortafolio = function () {
				return new bcs.DataSource({
				type: "odata",
				definition :
				{
					serverFiltering:true,
					transport:
					{
						read:
						{
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=6019",
							dataType: "json"
						}
					},
					filter:
					{ 
						logic	: "and",
						filters	:
						[
							{field: "IdCliente", 			operator: "eq", value: Number(pcportapv.IdCliente)},
							{field: "PropositoPortafolio", 	operator: "contains", value: 'APV'}
						]
					},
					schema:
					{
						model:
						{
							fields:
							{
								IdSocioDeNegocio		: { type: "string"},
								IdPersona				: { type: "string"},
								IdCliente				: { type: "string"},
								IdPortafolio			: { type: "string"},
								DocumentoIdentidad		: { type: "string"},
								NombreAgrupador			: { type: "string"},
								CodigoAgrupador			: { type: "string"},
								NombrePlanAhorro		: { type: "string"},
								Producto				: { type: "string"},
								IdEstadoAgrupador		: { type: "string"},
								CodigoPortafolio		: { type: "string"},
								DescripcionPortafolio	: { type: "string"},
								PropositoPortafolio		: { type: "string"},
								IdEstado				: { type: "string"},
								EstadoPortafolio		: { type: "string"},
								FechaIngreso			: { type: "sysdate"},
								FechaCreacion			: { type: "sysdate"},
								FechaModificacion		: { type: "sysdate"},
								IdCarga					: { type: "string"},
								IdEstadoPortafolioAutorizacion	: { type: "string"},
								EstadoPortafolioAutorizacion	: { type: "string"}
							}
						}
					}
				}
			})};
			
			
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
			
			pcportapv.portafoliosOptions = 
			{
				dataSource	: pcportapv.dsPortafolio(),
				sortable	: false,
				reorderable	: false,
				filterable	: false,
				groupable	: false,
				pageable	: false,
				selectable	: "row",	
				resizable	: false,
				autoBind	: true,
				scrollable	: true,
				height		: 250,
				columns		: [
				{
					field: "Producto",
					title: $translate.instant('UI_GRUPO_PORTAFOLIO_ABR'),
					format: "{0:n5}",
					width: "110px"
				},{
					field: "NombrePlanAhorro",
					title: $translate.instant('UI_NOMBRE_PLAN_AHORRO'),
					format: "{0:n5}",
					width: "110px"
				},{
					field: "PropositoPortafolio",
					title: $translate.instant('UI_PROPOSITO_PORTAFOLIO_ABR'),
					format: "{0:n5}",
					width: "100px"
				},{
					field: "EstadoPortafolioAutorizacion",
					title: $translate.instant('UI_ESTADO_PORTAFOLIO_ABR'),
					format: "{0:n5}",
					width: "80px"
				},{
					field: "FechaIngreso",
					title: $translate.instant('UI_FECHA_INGRESO_ABR'),
					format: "{0:d}",
					width: "80px",
					align: "center"
				}],
			};
			
			$scope.pcportapv.portafoliosOptionsDetalle = function(dataItem)
			{
				var dsDef = new bcs.DataSource({
					type: "odata",
					definition :
					{
						serverFiltering:true,
						transport:
						{
							read:
							{
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=6059",
								dataType: "json"
							}
						},
						filter:
						{ 
							logic	: "and",
							filters	:
							[
								{field: "IdCliente",	operator: "eq", value: Number(dataItem.IdCliente)},
								{field: "Producto", 	operator: "eq", value: dataItem.Producto},
								{field: "IdPortafolio", operator: "eq", value: dataItem.IdPortafolio}

							]
						},
						schema:
						{
							model:
							{
								fields:
								{
									IdSocioDeNegocio		: { type: "string"},
									IdPersona				: { type: "string"},
									IdCliente				: { type: "string"},
									IdPortafolio            : { type: "string"},
									IdMatrizAgrupadorAPV    : { type: "string"},
									Producto                : { type: "string"},
									RegimenTributario       : { type: "string"},
									CodigoPortafolio        : { type: "string"},
									EstadoPortafolio        : { type: "string"},
									FechaIngreso            : { type: "string"},
									IdEstadoPortafolioAutorizacion	: { type: "string"},
									EstadoPortafolioAutorizacion	: { type: "string"}
								}
							}
						}
					}
				});
				
				return {
					dataSource: dsDef,
					columns: [
					{
						field: "IdPortafolio",
						title: $translate.instant('UI_ID_PORTAFOLIO_ABR'),
						format: "{0:n5}",
						width: "110px"
					},{
						field: "Producto",
						title: $translate.instant('UI_PRODUCTO'),
						format: "{0:n5}",
						width: "110px"
					},{
						field: "RegimenTributario",
						title: $translate.instant('UI_REGIMEN_TRIBUTARIO'),
						format: "{0:n5}",
						width: "110px"
					},{
						field: "EstadoPortafolioAutorizacion",
						title: $translate.instant('UI_ESTADO_PORTAFOLIO_ABR'),
						format: "{0:n5}",
						width: "80px"
					},{
						field: "FechaIngreso",
						title: $translate.instant('UI_FECHA_INGRESO_ABR'),
						format: "{0:d}",
						width: "80px",
						align: "center"
					}],
					sortable	: false,
					reorderable	: false,
					filterable	: false,
					groupable	: false,
					pageable	: false,
					selectable	: "row",	
					resizable	: false,
					autoBind	: true,
					scrollable	: true
				};
			};
		});
		bcsComunes.ventanaAutoHeight(idVentana);
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
	});
}());