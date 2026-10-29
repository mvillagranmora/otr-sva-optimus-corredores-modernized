angular.module('ModuloSocioNegocio').controller('ReporteGestionClienteInstrumento', function($scope, $translate, $resource, bcsComunes, bcsServicioPlataforma, bcsAlerta) {
    	
        var idVentana = bcsComunes.ventanaWinId($scope);
        var fechaMinima = util.dateTerminoDia($scope.SesionFechaSistema);
        
        $scope.data = {};        
        $scope.seleccionReporteGestionCliente = {};
        $scope.data.IdEmpresa = $scope.SesionIdEmpresa;
        $scope.data.IdUsuario = $scope.SesionIdUsuario;        
        $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
        $scope.data.kngFechaOperacionConf = {
            max: fechaMinima //min: iODP.filtro.Fecha
        };
        
        
        
        // ---------------------------------------------------------
        // COMBOBOX
        // ---------------------------------------------------------
		$scope.data.oConfiguracionClasificacion={
				tipo : "combobox-clasificacion-tipo-instrumento",
				configuracion : {},
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				},
				idActual : false,
				seleccionado : false,
				idDefecto : 'RV'
		};
		
		$scope.data.oConfiguracionTipoInstrumento={
				tipo : "combobox-tipo-instrumento",
				configuracion : {},
				filtros : {
					IdEstado 						: { "field"   : "IdEstado","operator": "eq", "value"   : "VIG" },
					IdClasificacionTipoInstrumento 	: { "field"   : "IdClasificacionTipoInstrumento","operator": "eq", "value"   : "" },
					IdTipoInstrumentoExcluir 		: { "field"   : "IdTipoInstrumento","operator": "ne", "value"   : "ORF" }
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		

		
        // ---------------------------------------------------------
        // CONFIGURACION GRILLA
        // ---------------------------------------------------------
        modeloReporteGestionCliente = function() {
    		return {
    			model: {
    				fields : {
    					NombreCliente 			    : { type: "string"},
    					Nemotecnico 				: { type: "string"},
    					Pesos		 				: { type: "number"},
    					Nominales		            : { type: "number"},
    					Porcentaje			        : { type: "number"}
    				}
    			}
    		};
    	};
    	

    	
    	$scope.dataSourceReporteGestionCliente = new bcs.DataSource({
    		type 		: "call", //Los tipos posibles son: odata, call, array y object
    		definition 	: {    						
							serverFiltering : false, 	// Filtrado de datos en el servidor.
							serverPaging 	: false, 	// Paginado de los datos en el servidor.
							serverSorting 	: false,    // Ordenamiento de los datos en el servidor.							
    						pageSize		: 200,
    						callTransaction	: "2900",
    						callResultSet	: "P_OUT_TBL",
    			            schema 			: modeloReporteGestionCliente(),
    					    group			: 
				    						    [
				    								{field : "NombreCliente", 	
				    								 dir   : "asc",                
				    								 aggregates: [
		                 	                                       { field: "Pesos", aggregate: "sum" },
		                 	                                       { field: "Nominales", aggregate: "sum" },
		                 	                                       { field: "Porcentaje", aggregate: "sum" }
                 	                                             ]}    								
				    						    ]
    						
    		              }  
    	});
    	
    	columnasReporteGestionCliente = function() {
    		return[
    			{field : "NombreCliente", 	title:'UI_NOMBRE_CLIENTE', 	   width : "200px", align: "left", sortable : true}
    		   ,{field : "Nemotecnico", 	title:'UI_NEMO', 			   width : "110px", align: "left", sortable : true} 
    		   ,{field : "Pesos", 			title:'UI_PESOS', 			   width : "110px", align: "left",  sortable : false, template : "<label> #: (util.bcs_formato(Pesos,'GEN','Monto','CLP','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "} }
    		   ,{field : "Nominales", 		title:'UI_CANTIDAD_NOMINALES', width : "100px", align: "right", sortable : false, template : "<label> #: (util.bcs_formato(Nominales,'GEN','Cantidad','CLP','','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "} }
    		   ,{field : "Porcentaje", 		title:'UI_PORCENTAJE_SIMBOLO', width : "100px", align: "right", sortable : false, template : "<label> #: (util.bcs_formato(Porcentaje,'GEN','Cantidad','CLP','SIM','')).valor #</label>", aggregates: ["sum"], groupFooterTemplate: "#= util.formateaNumero(sum,0) #", footerAttributes:{style:"text-align: right; "}}
			
    		];
    	};
    	
        $scope.confBasicaReporteGestionCliente = {
            dataSource:		$scope.dataSourceReporteGestionCliente,
            columns: 		columnasReporteGestionCliente(),
            sortable: 		true,
            pageable: 		true,
            filterable: 	true,
            userSettings: 	true,
            excel: {
                allPages: 	true
            },
            height: 		450,
            selectable: 	"multiple row",
            navigatable: 	true,
            allowCopy: 		true,
            groupable: 		true
        };


        
        $scope.parametrosReporteGestionCliente = function(parametros) {
            var objParams = {};
            objParams.p_IdEmpresa 		= $scope.SesionIdEmpresa;
            objParams.p_IdUsuario 		= $scope.SesionIdUsuario;   
            objParams.p_FechaConsulta 	= util.dateSPdate($scope.data.kngFechaOperacion);            
            objParams.p_IdClasificacionTipoInstrumento = parametros.oConfiguracionClasificacion.idActual || "";
            objParams.p_IdTipoInstrumento = parametros.oConfiguracionTipoInstrumento.idActual || "";


            return objParams;
        }

        
        $scope.buscar = function() {
        	$scope.dataSourceReporteGestionCliente.setCallParameters($scope.parametrosReporteGestionCliente($scope.data));
        };

        $scope.limpiarFiltros = function() {
            $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);            
            $scope.seleccionReporteGestionCliente = {};
            $scope.data.oConfiguracionTipoInstrumento.idActual = null;
            $scope.data.oConfiguracionClasificacion.idActual = null;            
            $scope.data.oConfiguracionTipoInstrumento.seleccionado = null;
            $scope.data.oConfiguracionClasificacion.seleccionado = null;
            $scope.gridApiReporteGestionCliente.dataSource.removeAll();
        };
    });

