/*********************************************************
 * Purpose	: Directiva para administrar Preferencias 
 *            de Pago de un Portafolio
 * Author	: JGarces - BCS
 * Date		: 18/04/2016
 ********************************************************/
(function() {
	angular.module('ModuloPlataforma').directive('bcsPreferenciasPago',
	function($q,$translate,bcsComunes,$timeout,$state){
		return {
			restrict: 'E',
	    	replace: false,
			scope : {
				recibePagaPortafolio : '=?',
			},
	    	templateUrl: '../../Plataforma/UI/PAG/bcs-preferencias-pago/bcs-preferencias-pago.html', 
	    	link: function (scope, element, attrs, fn) {
	    		console.log("directiva bcsPreferenciasPago",scope.recibePagaPortafolio);
	    		/*********************************************
				 * VARIABLES Y CONTEXTOS
				 ********************************************/
				scope.cmbbx 	= scope.cmbbx 	|| {};
				scope.fn 		= scope.fn 		|| {};
				scope.win 		= scope.win 	|| {};	
				scope.grid		= scope.grid	|| {};
		
				/*********************************************
				 * ACCIONES DE PANTALLA
				 ********************************************/
				
				scope.win.abrirPopUp = function(preferenciaPago){
					var datosEnviarModal = {
						RecibePagaPortafolio :scope.recibePagaPortafolio,
						PreferenciaPagoObj	 :preferenciaPago};
					var winPreferencia = bcsComunes.ventanaAbrir("ModalPreferenciaPago", datosEnviarModal, false,false,bcsComunes.ventanaWinId(scope) );
					var promesa = bcsComunes.ventanaPromesa(winPreferencia);                               
		            promesa.then(function(res){
		            	scope.grid.gridPrefPago.dataSource.read();
		            	scope.grid.prefPagoSeleccionado = undefined;
		            });
				};
				scope.win.editar= function(preferenciaPago){
					if(preferenciaPago.IdTipoOperacion) scope.win.abrirPopUp(preferenciaPago);
				}
				
				/*********************************************
				 * GRILLAS
				 *********************************************/
				scope.setGridPrefPago = function(){
					return {
						dataSource: new bcs.DataSource({
							type: "odata",
							definition: {
								serverFiltering 	: true, 
								serverPaging 		: true, 
								serverSorting 		: true, 
								sortable 			: true,
								pageSize 			: 50,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1250",
										dataType: "json"
									}
								},
								filter: [
								    { field: "RecibePagaPortafolio", operator: "eq", value: scope.recibePagaPortafolio}
								],
								schema: {
									model: {
										fields: {
											IdPreferenciaPago		: { type: "string"},
											IdTipoOperacion			: { type: "string"},
											IdMedioDePago			: { type: "string"},
											IdMoneda				: { type: "string"},
											UsaPersonaRelacionada	: { type: "string"},
											FechaCreacion			: { type: "date"  },
											FechaModificacion		: { type: "date"  },
											IdUsuarioCreador		: { type: "number"},
											IdEstado				: { type: "string"},
											IdEmpresa 				: { type: "number"},
											UsaCuentaBancaria		: { type: "string"},
											UsaTarjetaBancaria		: { type: "string"},
											NombreTipoOperacion		: { type: "string"},
											IdTipoProducto			: { type: "string"},
											NombreTipoProducto		: { type: "string"},
											RecibePagaPortafolio 	: { type: "string"},
											NombreMedioDePago		: { type: "string"},
											NombreMoneda			: { type: "string"},	
											NombreEstado			: { type: "string"}
										}
									}
								}
							}
						}),
						columns: [
						    {field: "IdTipoOperacion", 			title: $translate.instant('UI_CODIGO_OPERACION'), 		attributes: {style:"text-align:center;"}, 	width: "80px"					},
						    {field: "NombreTipoOperacion", 		title: $translate.instant('UI_TIPO_OPERACION'), 		attributes: {style:"text-align:center;"}, 	width: "200px"					},
						    {field: "NombreMedioDePago", 		title: $translate.instant('UI_MEDIO_PAGO'), 			attributes: {style:"text-align:left;"}, 	width: "150px"					},
						    {field: "IdMoneda", 				title: $translate.instant('UI_MONEDA'), 				attributes: {style:"text-align:left;"},		width: "50px"					},
						    {field: "UsaPersonaRelacionada", 	title: $translate.instant('UI_USA_PERSONA_RELACIONADA'),attributes: {style:"text-align:left;"},		width: "100px"					,template: kendo.template("#if (UsaPersonaRelacionada==='S') {#SI#} else {#NO#}#")},
						    {field: "UsaCuentaBancaria", 		title: $translate.instant('UI_USA_CUENTA_BANCARIA'), 	attributes: {style:"text-align:left;"},		width: "100px"					,template: kendo.template("#if (UsaCuentaBancaria==='S') {#SI#} else {#NO#}#")},
						    {field: "UsaTarjetaBancaria", 		title: $translate.instant('UI_USA_TARJETA_BANCARIA'), 	attributes: {style:"text-align:left;"},		width: "100px"					,template: kendo.template("#if (UsaTarjetaBancaria==='S') {#SI#} else {#NO#}#")},
						    {field: "FechaCreacion", 			title: $translate.instant('UI_FECHA_CREACION'), 		attributes: {style:"text-align:left;"},		width: "100px",	format: "{0:d}"	},
						    {field: "FechaModificacion", 		title: $translate.instant('UI_FECHA_MODIFICACION'), 	attributes: {style:"text-align:left;"},		width: "100px",	format: "{0:d}"	},
						    {field: "NombreEstado", 			title: $translate.instant('UI_ESTADO'), 				attributes: {style:"text-align:left;"},		width: "60px"					},
						    {field: "IdPreferenciaPago", title: $translate.instant('UI_IdPreferenciaPago'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "IdMedioDePago", title: $translate.instant('UI_IdMedioDePago'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "IdUsuarioCreador", title: $translate.instant('UI_IdUsuarioCreador'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "IdEstado", title: $translate.instant('UI_IdEstado'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "IdEmpresa", title: $translate.instant('UI_IdEmpresa'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "IdTipoProducto", title: $translate.instant('UI_IdTipoProducto'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "NombreTipoProducto", title: $translate.instant('UI_NombreTipoProducto'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "RecibePagaPortafolio", title: $translate.instant('UI_RecibePagaPortafolio'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true},
							{field: "NombreMoneda", title: $translate.instant('UI_NombreMoneda'), attributes: {style:"text-align:left;"}, width: "60px", hidden:true}
						],
						filterable		: true,
						sortable		: true,
						pageable		: true,
						autoBind		: true,
						height			: 400,
						userSettings		: true,
						navigatable			: true,
						excel : {
							allPages: true,
						},
					    selectable 		: "row"
					};
				};	
				scope.grid.gridPrefPago = scope.setGridPrefPago();
				scope.grid.prefPagoSeleccionado = undefined;
	    	}
		}
	});
}());
//------------------------- FIN DIRECTIVE --------------------------