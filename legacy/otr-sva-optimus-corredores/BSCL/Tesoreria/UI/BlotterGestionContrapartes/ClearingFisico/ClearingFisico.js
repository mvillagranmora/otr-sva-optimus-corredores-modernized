angular.module('ModuloTesoreria').controller('ClearingFisicoCtrl',
		function($rootScope, $scope,bcsComunes,$translate,bcsServiciosTesoreria,bcsBlotterGestionContrapartesGrid,bcsAlerta,OPCIONES_BCSALERTA)
{
	 
	$scope.winID = bcsComunes.ventanaWinId($scope);	
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	$scope.data = {};
	
	
	//SETEO INICIAL
	$scope.btnActivo = true;
	$scope.kngFechaLiquidacion = util.dateInicioDia($scope.SesionFechaSistema);	
	
	//---------------------------------------------------
	// OBTIENE DATOS DE LA EMPRESA
	//---------------------------------------------------	
	if ($scope.SesionIdEmpresa)
	{
		
			var dsEmpresa = new bcs.DataSource({
				type: "odata",
				
				definition 	: {			
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					transport 			: {
											read : {
												 url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1234",
												 dataType : "json",
											  }
										  },
					
					filter: { field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) },
										  
					schema 				: {
							model: {
								fields : {
									IdEmpresa          : {type: "number"},
									DocumentoIdentidad : {type: "string"}
								}
							}
						}
										  
				},
			});
			
	        dsEmpresa.fetch().then(function(){
				 if (dsEmpresa.data().length <= 0 ) 
				 {
					 $scope.Empresa = $scope.SesionEmpresa;
					 console.log("Empresa no existe!");
					 return false;
				 }
				 else 
				 {
					 $scope.$apply(function() {						
						$scope.Empresa = dsEmpresa.data()[0].DocumentoIdentidad +' - '+ $scope.SesionEmpresa;
						return true;
					 });
				 }
		    });
	};
	
	
	//CONFIGURACION DE GRILLA PINCIPAL
	$scope.confBasicaClearingFisico = {
		dataSource			: bcsBlotterGestionContrapartesGrid.dsClearingFisico,
		columns				: bcsBlotterGestionContrapartesGrid.columnasClearingFisico(),		
		sortable			: true,
		pageable			: true,
		autoBind			: false,
		filterable			: true,
		height				: 460,
		selectable 			: "row",
		userSettings		: true,
		allowCopy			: { delimeter: "\t" },
		excel 				: { allPages: true },
		rowColorBackground	: function(row) { 
			var color = "";
			 
			if (row.IdComprobanteTesoreria === 0)
				color = "yellow";
			else
				color = "green";
			
			return color;
		},		
	};
	

	
	//CONFIGURACION DE SUB-GRILLA DETALLE
	
	$scope.confBasicaClearingFisicoDetalle = function(item) {		
		var parametros = {};
		parametros = $scope.parametrosClearing();		

		var dsDetalleConf = bcsBlotterGestionContrapartesGrid.dsClearingFisicoDetalleConf();
		dsDetalleConf.definition.callParameters = parametros;

		dsDetalleConf.definition.filter = 
			[
			 	{ field: "IdBolsa", 				 		operator: "eq", value: item.IdBolsa },
			 	{ field: "IdClasificacionTipoInstrumento", 	operator: "eq", value: item.IdClasificacionTipoInstrumento },
			 	{ field: "IdTipoInstrumento", 				operator: "eq", value: item.IdTipoInstrumento },
			 	{ field: "IdInstrumento", 			 		operator: "eq", value: item.IdInstrumento }
			];
		
		return {
			dataSource			: new bcs.DataSource(dsDetalleConf),
			columns				: bcsBlotterGestionContrapartesGrid.columnasClearingFisicoDetalle(),
			sortable			: false,
			pageable			: false,
			filterable			: false,
			autoBind			: true,
			height				: 150,
			selectable 			: "multiple row"
		}
	};	
	
	
	//CONFIGURACION DE COMBOBOX
	$scope.oConfiguracionGrupoCamara={
			tipo 			: "combobox-grupo-camara",
			configuracion 	: {},
			filtros 		: {
								 IdCamaraDeCompensacion     : { "field"   : "IdCamaraDeCompensacion",    "operator": "eq", "value"   : ($scope.SesionLocale == 'CL' ? "CCLV" : "CAVALI") },								
								 IdPaisCamaraDeCompensacion : { "field"   : "IdPaisCamaraDeCompensacion","operator": "eq", "value"   : $scope.SesionIdPaisSistema},
								 IdEstadoGrupoCamara        : { "field"   : "IdEstadoGrupoCamara",       "operator": "eq", "value"   : "ACT"},								
								 SecuenciaAgrupacion        : { "field"   : "SecuenciaAgrupacion",       "operator": "eq", "value"   : "GRUPO1" },
							  },
			idActual 		: false,
			seleccionado 	: false,
			idDefecto 		: ''			
	};	
	
	
	
	$scope.limpiarFiltros = function ()
	{
		$scope.kngFechaLiquidacion 	= util.dateInicioDia($scope.SesionFechaSistema);
		bcsBlotterGestionContrapartesGrid.dsClearingFisico.removeAll();
	}
	
	
	 
	//Setea parámetros de consulta del clearing
	$scope.parametrosClearing = function()
	{	
		var parametros = {};
		var agrupacionCamaras = new Array();
		

		if ($scope.SesionLocale === 'CL')
		{
			// NombreGrupoCamara: "PH + SM" /// falta borrar
			// oConfiguracionGrupoCamara.seleccionado
			//$scope.oConfiguracionGrupoCamara.seleccionado.NombreGrupoCamara
			
			switch ($scope.oConfiguracionGrupoCamara.seleccionado.NombreGrupoCamara)
			{	
				case 'RV':				
					agrupacionCamaras.push({ Camara: "RV" });
					break;
				case 'PM':				
					agrupacionCamaras.push({ Camara: "PM" });
					break;				
				case 'PH + SM':
					agrupacionCamaras.push({ Camara: "PH" });				
					agrupacionCamaras.push({ Camara: "SM" });				
					break;			
				case 'RV + PM':
					agrupacionCamaras.push({ Camara: "RV" });
					agrupacionCamaras.push({ Camara: "PM" });				
					break;
				default:  //Todos
					agrupacionCamaras.push({ Camara: "RV" });
					agrupacionCamaras.push({ Camara: "PM" });
					agrupacionCamaras.push({ Camara: "PH" });
					agrupacionCamaras.push({ Camara: "SM" });			
			}
		}
		
		
		if ($scope.SesionLocale === 'PE')
		{
			switch ($scope.oConfiguracionGrupoCamara.seleccionado.NombreGrupoCamara)
			{	
				case 'RV':				
					agrupacionCamaras.push({ Camara: "RV" });
					break;
				case 'RF':				
					agrupacionCamaras.push({ Camara: "RF" });
					break;				
				case 'RP': // Reportes
					agrupacionCamaras.push({ Camara: "RP" });
					break;			
				case 'RM': 
					agrupacionCamaras.push({ Camara: "RM" });									
					break;
				default:  //Todos
					agrupacionCamaras.push({ Camara: "RV" });
					agrupacionCamaras.push({ Camara: "RF" });
					agrupacionCamaras.push({ Camara: "RP" });
					agrupacionCamaras.push({ Camara: "RM" });			
			}
		}
		
		parametros.p_IdEmpresa 			= $scope.SesionIdEmpresa;
		parametros.p_IdUsuario         	= $scope.SesionIdUsuario;
		parametros.p_IdModulo			= 'TES';
		parametros.p_UsaReglasNegocio	= 'S';
		parametros.p_FechaLiquidacion	= util.dateSPdate($scope.kngFechaLiquidacion) || '0001-01-01';	
		parametros.p_Camaras            = agrupacionCamaras;
		
		
		return parametros;
	}
	
 
	
	
	$scope.buscar = function()
	{
		var parametros = {};
		parametros = $scope.parametrosClearing();
		console.log("PARAMETROS:" + JSON.stringify(parametros));
		bcsBlotterGestionContrapartesGrid.dsClearingFisico.setCallParameters(parametros);	
	}

	

    //Abre ventana(del módulo "Orden") con el detalle de la transacción
	$scope.data.abrirDetalleTransaccion = function (row) {
		if (!row) return;
		bcsComunes.ventanaAbrir("DetalleTransaccion", {IdTransaccion : row[0].IdTransaccion} ,false,false);
	};

	
});


