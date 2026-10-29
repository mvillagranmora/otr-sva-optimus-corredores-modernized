/*************************************************************
 * Purpose	: Administrar Valores de Cobro.
 * Author	: JG - BCS
 * Date		: 30/06/2016
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('AdministrarValorCobroCtrl',
	function ($scope,bcsComunes,$translate) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var mAdmVC 			= this;
		mAdmVC.idVentana 	= bcsComunes.ventanaWinId($scope);
		mAdmVC.parametros 	= bcsComunes.ventanaParams(mAdmVC.idVentana);
		mAdmVC.domVentana	= $("[ventanaid='"+mAdmVC.idVentana+"']");
		/*********************************************
		 * GRILLA
		 *********************************************/
		mAdmVC.GrillaValoresCobros = {
			dataSource		: new bcs.DataSource({
				type 		: "odata", 
				definition 	: {
					serverFiltering : true,
					serverPaging 	: true, 
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 50,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1337",
							dataType: "json"
						}
					},
					filter: [
				         { field: 'IdEmpresa', operator: "eq", value: $scope.$root.SesionIdEmpresa},
				         { field: 'EsPersonalizado', operator: "eq", value: 'N'},
			         ],
					schema 	 : {
						model	: { fields	: {		 	
								IdValorCobro			: { type: "string"},
								DescripcionValorCobro	: { type: "string"},
								IdTipoProducto			: { type: "string"},
								NombreTipoProducto		: { type: "string"},
								IdCobro					: { type: "string"},
								IdCobroClasificacion	: { type: "number"},
								CodigoCobroClasificacion: { type: "string"},
								NombreCobro				: { type: "string"},
								FormaCobro				: { type: "string"},
								ValorCobro				: { type: "number"},
								ValorTopeMinimoOrden	: { type: "number"},
								IdMoneda				: { type: "string"},
								IdEstado				: { type: "string"},
								Estado					: { type: "string"},
								IdEmpresa				: { type: "number"},
								EsPersonalizado			: { type: "string"},
							} 
						}
					}
				}
			}),
			columns			: [
				{field: "IdValorCobro"				,width: "100px",title: $translate.instant("UI_CODIGO")								,attributes : {style : "text-align:left;"}},		
				{field: "IdTipoProducto"			,width: "100px",title: $translate.instant("UI_ID_TIPO_PRODUCTO")					,attributes : {style : "text-align:left;"}},
				{field: "NombreTipoProducto"		,width: "200px",title: $translate.instant("UI_TIPO_PRODUCTO")						,attributes : {style : "text-align:left;"}},
				{field: "CodigoCobroClasificacion"	,width: "100px",title: $translate.instant("UI_CLASIFICACION_COBRO")					,attributes : {style : "text-align:left;"}},
				{field: "IdCobro"					,width: "100px",title: $translate.instant("UI_ID_COBRO")							,attributes : {style : "text-align:left;"}},
				{field: "NombreCobro"				,width: "200px",title: $translate.instant("UI_COBRO")								,attributes : {style : "text-align:left;"}},
				{field: "FormaCobro"				,width: "100px",title: $translate.instant("UI_FORMA_COBRO")							,attributes : {style : "text-align:left;"}},
				{field: "ValorCobro"				,width: "100px",title: $translate.instant("UI_VALOR_COBRO")			,format:"{0:n2}",attributes : {style : "text-align:left;"},template: kendo.template("#if (FormaCobro==='V') {##=ValorCobro# %#} else {##=ValorCobro##}#")},
				{field: "ValorTopeMinimoOrden"		,width: "100px",title: $translate.instant("UI_VALOR_TOPE_MINIMO")					,attributes : {style : "text-align:left;"},template: "#= util.bcs_formato(ValorTopeMinimoOrden, 'GEN', 'Monto', IdMoneda, '', '').valor #"},
				{field: "IdMoneda"					,width: "100px",title: $translate.instant("UI_MONEDA")								,attributes : {style : "text-align:left;"}},
				{field: "Estado"					,width: "100px",title: $translate.instant("UI_ESTADO")								,attributes : {style : "text-align:left;"}},
			],		
			sortable			: true,
			filterable			: true,
			groupable			: false,
			pageable			: true,
			autoBind			: true, 
			height				: 400,
		    selectable 			: "row"
		 };
		/*********************************************
		 * FUNCIONES
		 *********************************************/	

		mAdmVC.modalValor = function (valor) {
			var id1 = bcsComunes.ventanaAbrir("ModalAdministrarValorCobro",{valorCobro:valor},false,false,mAdmVC.idVentana); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(respuesta){ 
            	if(respuesta.aceptar === true){
            		mAdmVC.apiGrillaValoresCobros.dataSource.read();
				}
				else {console.log("usuario cancela en el PopUp!");}
            });
		};
	});
}());