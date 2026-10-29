(function() {
	angular.module('ModuloMesa').controller('InfOperacionesPenDatatecCtrl',
		function($scope,$translate,bcsComunes,$http,bcsServiciosMesa){
			
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
		
			var InOpPeDatc 		= this;
			InOpPeDatc.idVentana	= bcsComunes.ventanaWinId($scope);
			InOpPeDatc.grilla		= {};
			InOpPeDatc.fn			= {};
			InOpPeDatc.filtro		= {};
			
			InOpPeDatc.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			
			/*********************************************
			 * DATASOURCES
			 *********************************************/
			
			var dsDefOperacionesPendientesDatatec = new bcs.DataSource({
				type : "call",
				definition : {
					pageSize : 100,
					callTransaction : "601189",
					callResultSet : "P_OUT_TABLA",
					schema : {
						model : {
							fields : {
								IdDatatec				: {type: "string"},
								NumeroRegistro			: {type: "number", excelFormat: "#,##0"},
								FechaOperacion			: {type: "sysdate"},
								TipoOperacion			: {type: "string"},
								GlosaProceso			: {type: "string"},
								Estado					: {type: "string"},
								NombreCliente			: {type: "string"},
								IdMonedaPrincipal		: {type: "string"},
								IdMonedaSecundaria		: {type: "string"},
								MontoMonedaPrincipal	: {type: "number", excelFormat: "#,##0.000"},
								TipoCambioCierre		: {type: "number", excelFormat: "#,##0.000"},
								MontoMonedaSecundaria	: {type: "number", excelFormat: "#,##0.000"}
							}
						}
					}
				}
			});
			
			/*********************************************
			 * GRILLAS
			 *********************************************/
			
			InOpPeDatc.grilla.OperacionesPendientesDatatec = {
				dataSource: dsDefOperacionesPendientesDatatec,
				columns: [
				    {field: "IdDatatec", title: "UI_ID_DATATEC", width: "120px"},
				    {field: "NumeroRegistro", title: "UI_NUMERO_REGISTRO", width: "90px"},
				    {field: "FechaOperacion", title: "UI_FECHA_OPERACION", template : " <label> #:  util.formatoDate(FechaOperacion) #</label>", width: "80px"},
				    {field: "TipoOperacion", title: "UI_TIPO_OPERACION", width: "80px"},
				    {field: "GlosaProceso", title: "UI_GLOSA_PROCESO", width: "200px"},
				    {field: "Estado", title: "UI_ESTADO_REGISTRO", width: "80px"},
				    {field: "NombreCliente", title: "UI_NOMBRE_CLIENTE", width: "200px"},
				    {field: "IdMonedaPrincipal", title: "UI_MONEDA_PRINCIPAL", width: "80px"},
				    {field: "IdMonedaSecundaria", title: "UI_MONEDA_PRINCIPAL", width: "80px"},
				    {field: "MontoMonedaPrincipal", title: "UI_MONTO_MONEDA_SECUNDARIA", format: "{0:n0}", width: "80px"},
				    {field: "TipoCambioCierre", title: "UI_TIPO_CAMBIO_CIERRE", width: "80px", template : " <label> #:  (util.bcs_formato(TipoCambioCierre,'SPT','Monto',IdMonedaPrincipal,'','')).valor #</label>"},
				    {field: "MontoMonedaSecundaria", title: "UI_MONTO_MONEDA_SECUNDARIA", format: "{0:n0}", width: "80px"}
				],
				sortable	: true,
				pageable	: { pageSize : 25 },
				autoBind	: true,
				height		: 420,
			    selectable 	: "row",
			    excel: {
					allPages: true,
				},
			    filterable	: false,
			    navigatable : true,
			    scrollable	: true,
			    userSettings: true,
			    gridCreated	: function(grid) {
			    	gridOperacionesPendientes = grid;
			    	dsDefOperacionesPendientesDatatec.read();
				}
			};
			
			
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			InOpPeDatc.fn.buscar = function(){
				dsDefOperacionesPendientesDatatec.setCallParameters($scope.preparaParametros());
			};
			
			$scope.preparaParametros = function(){
				parametrosConsulta = {};
				parametrosConsulta.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
				parametrosConsulta.p_Fecha = util.dateSPdate(InOpPeDatc.filtro.Fecha);
				
				return parametrosConsulta;
			};
			
		});
}());