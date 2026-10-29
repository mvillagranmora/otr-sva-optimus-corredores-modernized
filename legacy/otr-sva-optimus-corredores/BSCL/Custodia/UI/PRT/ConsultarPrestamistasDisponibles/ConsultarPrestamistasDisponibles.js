angular.module('ModuloCustodia').controller('ConsultarPrestamistasDisponiblesModalCtrl',
		function ($rootScope,$scope,$translate,bcsComunes,$timeout) {

	$scope.data = {};
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);

	parametrosConsulta = {};
	parametrosConsulta.p_IdTipoPrestamo = parametros.IdTipoPrestamo;
	parametrosConsulta.p_IdEmpresa = $scope.SesionIdEmpresa;
	parametrosConsulta.p_IdUsuario = $scope.SesionIdUsuario;
	parametrosConsulta.p_IdModulo = 'CUS';
	parametrosConsulta.p_IdPais = $scope.SesionIdPaisSistema;
	parametrosConsulta.p_IdClasificacionTipoInstrumento = parametros.IdClasificacionTipoInstrumento;
	parametrosConsulta.p_IdTipoInstrumento = parametros.IdTipoInstrumento;
	parametrosConsulta.p_IdInstrumento = parametros.IdInstrumento;
	parametrosConsulta.p_CantidadMinima = 1; //parametros.CantidadMinima;
	parametrosConsulta.p_FechaConsulta = $scope.SesionFechaSistema;
	parametrosConsulta.p_IdClienteExcluido = parametros.IdCliente;

	
	//filtros iniciales
	var dsGrillaPrestamistas = new bcs.DataSource({
		type : "call",
		definition : {
						pageSize		: 100,
	                    callTransaction : "100009",
	                    callResultSet 	: "P_OUT_DISPONIBLES",
						schema : {
							model : {
								fields : {
									IdCliente			: {type : "number"},
									IdPortafolio		: {type : "number"},
									NombreCliente 		: {type : "string"},
									Proposito			: {type : "string"},
									Cantidad			: {type : "number"},
									DocumentoIdentidad 	: {type : "string"},
									CodigoPortafolio	: {type : "string"}
								}
							}
						}
		}
	});
	columnas = function() {
  		return [
  			 {field: "DocumentoIdentidad",	title: 'UI_DOCUMENTO_IDENTIDAD', 	width: "100px"}
			,{field: "NombreCliente", 		title: 'UI_NOMBRE_CLIENTE', 		width: "150px"}
			,{field: "CodigoPortafolio", 	title: 'UI_CODIGO_PORTAFOLIO',		width: "100px"}
			,{field: "Proposito", 			title: 'UI_PROPOSITO',				width: "100px"}
			,{field: "Cantidad", 			title: 'UI_CANTIDAD_DISPONIBLE', 	width: "100px", template : " <label> #:  (util.bcs_formato(Cantidad,'CUS','Cantidad','" + $rootScope.SesionIdMonedaSistema + "','','')).valor #</label>"}
		];
    };
		

	$scope.data.confBasicaPrestamistas= {
		dataSource			: dsGrillaPrestamistas,
		columns				: columnas(),
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		sortable			: true,
		filterable			: true,
		height				: 250,
		selectable 			: "single row",
		autoBind			: false
	};
	
	$scope.data.buscar=function(){
		dsGrillaPrestamistas.setCallParameters(parametrosConsulta);
	};


	$scope.data.fnDobleClick = function(datos) {
		bcsComunes.ventanaData(idVentana,datos);
		bcsComunes.ventanaCerrar(idVentana);
	};

	
	
	$timeout(function(){
				dsGrillaPrestamistas.setCallParameters(parametrosConsulta);	
			},100);

});