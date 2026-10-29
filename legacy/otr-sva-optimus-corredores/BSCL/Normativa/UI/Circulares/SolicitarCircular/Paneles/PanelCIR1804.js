(function() {
	angular.module('ModuloNormativa').controller('PanelCIR1804Ctrl',function($scope,$timeout,Model,Utils,$translate,bcsComunes,$compile,bcsServiciosNormativa, bcsVisorCircularConfig, bcsServicioPlataforma){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var CIR1804 = this;
		CIR1804.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
		CIR1804.domVentana = $("[ventanaid='"+$scope.idVentana+"']");
		
		CIR1804.TipoArchivo = 'pdf';
		CIR1804.IdVersionEsquema = '';
	
		CIR1804.IdTipoDocumento = 'CIR1804';
		CIR1804.FecInicioOptions = {format: 'MMMM yyyy', start: 'year',depth: 'year'};
		$scope.filtros = {};
		$scope.dataTemplate = {};
		$scope.oFondo = {};
		
		
		CIR1804.p_IdEmpresa 				=  $scope.SesionIdEmpresa;
		CIR1804.p_IdUsuario              	=  $scope.SesionIdUsuario;
		CIR1804.p_IdModulo 					=  $scope.solCir.modulo;

		$scope.oFondo.filtros = {
			IdFondo : { field: "IdFondo", operator: "eq", value:undefined },
		};
		
		$scope.filtros.FechaRegistros = util.dateDS($scope.SesionFechaSistema, "FechaSistema", "CALL");
		
		
		$scope.data = {};
		// --------------------------------------------------
		// CONFIGURACION DE COMBOS
		// --------------------------------------------------
		$scope.data.oConfiguracionApoderadoCliente={
				filtros: {
					IdEmpresa : {"field" : "IdEmpresa", "operator" : "eq", "value" : $scope.SesionIdEmpresa},
					IdRelacionPersona : {"field"   : "IdRelacionPersona","operator": "eq", "value" :'0'},
					IdEstado : {"field" : "IdEstado", "operator": "eq", "value": "ACT"}
				},
			};

		/*********************************************
		 * FUNCIONES
		 ********************************************/
		
		$scope.CIR1804.clear = function(){
		    $scope.filtros.FechaRegistros = util.dateInicioDia($scope.SesionFechaSistema);
		    $scope.filtros.IdFondo = '';
		    $scope.filtros.IdCliente = false;
		};


		$scope.imprimirCircular = function(){
			var filterTemplate = [ { field: "IdTipoDocumento",operator: "eq", value: 'CIR1804' },
			                       { field: "TipoArchivo",  operator: "eq", value: 'pdf' },
			                       { field: "Version",  	 operator: "eq", value: '1' },
			                       { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
			                       { field: "IdEstado",		operator: "eq", value: 'ACT' } ];
				
				var p_IdPeriodo			= (util.dateSPdate(CIR1804.FecInicio) != undefined ? util.dateSPdate(CIR1804.FecInicio) : '');
				var p_IdPersonaAGF		= (CIR1804.IdPersonaAGF != undefined ? CIR1804.IdPersonaAGF.IdPersonaDestino : '');
				var p_IdPersonaFondo	= (CIR1804.IdPersonaAGF != undefined ? CIR1804.IdPersonaAGF.IdPersonaDestino : '');
				
			
			$scope.solCir.ConsultaArchivoNormativa.query({ filter: filterTemplate }).then(function(){
				if($scope.solCir.ConsultaArchivoNormativa.view().length > 0) {
					$scope.dataTemplate.TipoReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].TipoArchivo;
					$scope.dataTemplate.IdReporte		= $scope.solCir.ConsultaArchivoNormativa.view()[0].IdArchivo;
					$scope.dataTemplate.NombreReporte	= $scope.solCir.ConsultaArchivoNormativa.view()[0].NombreArchivo;

					//PARAMETROS
					var parametros = {	"p_IdEmpresa" 				: 	$scope.$root.SesionIdEmpresa,
										"p_IdUsuario"              	: 	$scope.SesionIdUsuario,
										"p_IdModulo" 				:  	$scope.solCir.modulo,
										"p_Periodo"					:	p_IdPeriodo,
										"p_IdPersonaAGF"			:	p_IdPersonaAGF,
										"p_IdPersonaFondo"			:	p_IdPersonaFondo	};
					
					//DATA 
					$scope.dataTemplate.data= {  "serviceId"   :	$scope.solCir.ConsultaArchivoNormativa.view()[0].ServicioArchivo
											   , "serviceData" :	parametros
											  };
					
				
					var subrep = new Array();
					$scope.dataTemplate.subReports=subrep;		
					$scope.solCir.getReport($scope.dataTemplate);
					
				}
			});
		};

	});
}());