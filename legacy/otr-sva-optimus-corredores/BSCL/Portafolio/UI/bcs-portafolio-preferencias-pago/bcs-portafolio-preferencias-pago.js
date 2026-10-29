/*********************************************************
 * Purpose	: Directiva para administrar Preferencias 
 *            de Pago de un Portafolio
 * Author	: JGarces - BCS
 * Date		: 29/03/2016
 ********************************************************/
(function() {
	angular.module('ModuloPortafolio').directive('bcsPortafolioPreferenciasPago',
	function($q,$translate,bcsComunes,$timeout,$state){
		return {
			restrict: 'E',
	    	replace: false,
			scope : {
				recibePagaPortafolio : '=?',
				idPortafolio 		 : '=?',
				idPersona 		 	 : '=?',
				codigoFormulario	 : '=?',
			},
	    	templateUrl: '../../Portafolio/UI/bcs-portafolio-preferencias-pago/bcs-portafolio-preferencias-pago.html', 
	    	link: function (scope, element, attrs, fn) {
	    		/*********************************************
				 * VARIABLES Y CONTEXTOS
				 ********************************************/
				scope.cmbbx 	= scope.cmbbx 	|| {};
				scope.fn 		= scope.fn 		|| {};
				scope.win 		= scope.win 	|| {};	
				scope.grid		= scope.grid	|| {};
				scope.idVentana 		= bcsComunes.ventanaWinId(scope);
		
				/*********************************************
				 * ACCIONES DE PANTALLA
				 ********************************************/
				scope.win.abrirPopUp = function(preferenciaPago){
					var datosEnviarModal = {
						IdPortafolio		 :scope.idPortafolio,
						IdPersona			 :scope.idPersona,
						CodigoFormulario	 :scope.codigoFormulario,
						RecibePagaPortafolio :scope.recibePagaPortafolio,
						PreferenciaPagoObj	 :preferenciaPago,
						PreferenciaPagoTodosObj : scope.grid.gridPrefPago.dataSource.data()};
						bcsComunes.ventanaOverlay(scope.idVentana,true);
					var winPreferencia = bcsComunes.ventanaAbrir("modalPortafolioPreferenciaPago", datosEnviarModal, false,false);
					var promesa = bcsComunes.ventanaPromesa(winPreferencia);                               
		            promesa.then(function(res){
		            	scope.grid.gridPrefPago.dataSource.read();
		            	bcsComunes.ventanaOverlay(scope.idVentana,false);
		            });
				};
				
				/*********************************************
				 * GRILLAS
				 *********************************************/
				scope.setGridPrefPago = function(){
					return {
						dataSource: new bcs.DataSource({
							type: "odata",
							definition: {
								serverFiltering : true,
								serverPaging 	: true,
								serverSorting 	: true,
								sortable 		: true,
								pageSize 		: 50,
								transport: {
									read: {
										url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1266",
										dataType: "json"
									}
								},
								filter: [
								    { field: "IdPortafolio", operator: "eq", value: scope.idPortafolio},
								    { field: "RecibePagaPortafolio", operator: "eq", value: scope.recibePagaPortafolio}
								],
								schema: {
									model: {
										fields: {
											IdPreferenciaPagoPortafolio	: { type: "number"},
											IdPortafolio				: { type: "number"},
											IdTipoOperacion				: { type: "string"},
											TipoOperacion				: { type: "string"},
											IdMedioDePago				: { type: "string"},
											MedioDePago					: { type: "string"},
											IdMoneda					: { type: "string"},
											UsaCuentaBancaria			: { type: "string"},
											TextoUsaCuentaBancaria		: { type: "string"},
											UsaTarjetaBancaria			: { type: "string"},
											TextoUsaTarjetaBancaria		: { type: "string"},
											IdEstado					: { type: "string"},
											Estado						: { type: "string"},
											IdBanco 					: { type: "number"},
											NombreBancoCuentaBancaria	: { type: "string"},
											NumeroCuentaBancaria		: { type: "string"},
											NombreBancoTarjetaBancaria	: { type: "string"},
											TextoPorDefecto				: { type: "string"},
											NumeroTarjetaBancaria		: { type: "string"},
											FechaCreacion				: { type: "date"},
											FechaModificacion			: { type: "date"},
										}
									}
								}
							}
						}),
						columns: [
						    {field: "MedioDePago", title: $translate.instant('UI_MEDIO_PAGO'), attributes: {style:"text-align:left;"}, width: "150px"},
						    {field: "IdMoneda", title: $translate.instant('UI_MONEDA'), attributes: {style:"text-align:left;"},width: "50px"},
						    {field: "TipoOperacion", title: $translate.instant('UI_TIPO_OPERACION'), attributes: {style:"text-align:center;"}, width: "200px"},
						    {field: "TextoUsaCuentaBancaria", title: $translate.instant('UI_USA_CUENTA_BANCARIA'), attributes: {style:"text-align:left;"},width: "100px"},
						    {field: "NombreBancoCuentaBancaria", title: $translate.instant('UI_BANCO'), attributes: {style:"text-align:left;"},width: "150px"},
						    {field: "NumeroCuentaBancaria", title: $translate.instant('UI_NUMERO_CUENTA'), attributes: {style:"text-align:left;"},width: "100px"},
						    {field: "TextoUsaTarjetaBancaria", title: $translate.instant('UI_USA_TARJETA_BANCARIA'), attributes: {style:"text-align:left;"},width: "100px"},
						    {field: "NombreBancoTarjetaBancaria", title: $translate.instant('UI_BANCO'), attributes: {style:"text-align:left;"},width: "150px"},
						    {field: "NumeroTarjetaBancaria", title: $translate.instant('UI_NUMERO_TARJETA'), attributes: {style:"text-align:left;"},width: "100px"},
						    {field: "TextoPorDefecto", title: $translate.instant('UI_POR_DEFECTO'), attributes: {style:"text-align:left;"},width: "100px"},
						    {field: "Estado", title: $translate.instant('UI_ESTADO'), attributes: {style:"text-align:left;"},width: "60px"},
						],
						sortable			: true,
						pageable			: true,
						autoBind			: true,
						filterable			: true,
						height				: 240,
					    selectable 			: "row"
					};
				};	
				
				/*********************************************
				 * FUNCIONES WATCH
				 *********************************************/
				scope.$watch('idPortafolio',function(newval,oldval){
					if(newval){
						//scope.grillaCargada = false;
						//scope.gridPrefPago = scope.setGridPrefPago(scope.idPortafolio);
						scope.grid.gridPrefPago = scope.setGridPrefPago();
						scope.grillaCargada = true;
					}
				});
	    	}
		}
	});
}());
