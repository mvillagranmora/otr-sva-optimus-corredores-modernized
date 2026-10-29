angular.module('ModuloSocioNegocio').controller('ReporteGestionClienteFlujoMensualAportesRescates', 
			function($scope, $translate, $resource, bcsComunes, bcsServicioPlataforma, bcsAlerta) {	
		
        var idVentana = bcsComunes.ventanaWinId($scope);
        var fechaMinima = util.dateTerminoDia($scope.SesionFechaSistema);
        
        $scope.data = {};
        $scope.data.IdEmpresa = $scope.SesionIdEmpresa;
        $scope.data.IdUsuario = $scope.SesionIdUsuario;        
        $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
        $scope.seleccionReporteGestionCliente = {};
        
        $scope.data.kngFechaOperacionConf = {
            max: fechaMinima, 	// min: iODP.filtro.Fecha
			start: "decade",
			depth: "decade",
			format: "yyyy"
        };
        
        // Lista de Agentes
		$scope.data.oConfiguracionAgente={
				tipo : "combobox-ejecutivo",
				configuracion : {},
				filtros : { 	
					IdCliente : { "field"   : "IdCliente","operator": "eq", "value"   :  0 },
				},
				idActual : false,
				seleccionado : false,
				idDefecto : ''
		};
		
		modeloReporteGestionCliente = function() {
			return {
				model: {
					fields : {
						NombreAgente 	    : { type: "string"},
						NombreCliente	    : { type: "string"},
						Monto_01	 		: { type: "number"},
						Monto_02	        : { type: "number"},
						Monto_03	     	: { type: "number"},
						Monto_04			: { type: "number"},
						Monto_05			: { type: "number"},
						Monto_06		    : { type: "number"},
						Monto_07			: { type: "number"},
						Monto_08			: { type: "number"},
						Monto_09	  	    : { type: "number"},
						Monto_10	 		: { type: "number"},
						Monto_11 			: { type: "number"},
						Monto_12		    : { type: "number"},
						TotalPeriodo 		: { type: "number"},
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
							callTransaction	: "700021",	// Numero de servicio
							callResultSet	: "P_OUT_TBL",
				            schema 			: modeloReporteGestionCliente()
			              }  
		});
		
		// Formato de columnas para los totales por mes
		modeloFormatNumerico = function(mes, tituloMes) {
			let columna = "Monto_" + mes;
			let templateNumerico = "<label> #: (util.bcs_formato(" + columna +",'GEN','Cantidad','CLP','','')).valor #</label>";
			let objFormato = {	field : columna,	
								title: tituloMes, 
								width : "100px", 
								sortable : false, 
								template : templateNumerico, 
								headerAttributes: {"class": "HeaderCenter"}, 
								attributes: {"class": "columnaNumero"}};
			return objFormato;
		}
		
		columnasReporteGestionCliente = function(agno) {
			let d = new Date();					
			let diaActual 	= d.getDate();		// dia actual
			let mesActual 	= d.getMonth() + 1;	// mes actual getMonth() -> 0=Enero, 1=Febrero...
			let agnoActual 	= d.getFullYear();	// agno acutal
			
			// Columnas fijas
			let objColumnas = [
				 {field : "NombreAgente", 	title:	$translate.instant("UI_NOMBRE_EJECUTIVO"),  width : "200px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }			 			 
				,{field : "NombreCliente", 	title:	$translate.instant("UI_NOMBRE_CLIENTE"), 	width : "200px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }

			];
			
			let ultimoDiaMes, tituloTotalMes, m = 0;
			const listaMeses = ['01', '02', '03', '04', '05', '06', '07', '08', '09', '10', '11', '12']
			// Columnas variables por mes (Muestra todos los meses excepto para el agno actual en el caso que el mes se anterior a Diciembre)
			for (let i = 0; i < listaMeses.length; i++) {
				m = i + 1;
				ultimoDiaMes = new Date(agno, m, 0).getDate();						// Busca el ultimo dia del mes
				
				tituloTotalMes = agno + '-' + listaMeses[i] + '-' + ultimoDiaMes;	// Titulo de la columna
				objColumnas.push(modeloFormatNumerico(listaMeses[i], tituloTotalMes));
			}
			
		
			// Mas columnas fijas
			let objColumnasFin = {field : "TotalPeriodo", 	title:	$translate.instant("UI_TOTAL"),  width : "100px", sortable : true, editable: false ,  template : " <label> #:  (util.bcs_formato(TotalPeriodo,'GEN','Cantidad','CLP','','')).valor #</label>"};
			objColumnas.push(objColumnasFin);
		
			return objColumnas;
		};
		
		

		
		
		// Propiedades iniciales del grid
		var confBasicaReporteGestionCliente_2 = function () {
        	return {
			gridCreated : function(grid) {
        		$scope.gridApiReporteGestionCliente = grid;
			},		
			dataSource: $scope.dataSourceReporteGestionCliente,
            columns: columnasReporteGestionCliente($scope.data.kngFechaOperacion.getFullYear()),			
            sortable: true,
            pageable: true,
            filterable: true,
            userSettings: true,
            excel: {
                allPages: true
            },
            height: 450,
            selectable: "multiple row",
            navigatable: true,
            allowCopy: true,
            
            autoBind 	: false,
            groupable	: true
        	};
        };
        $scope.confBasicaReporteGestionCliente = confBasicaReporteGestionCliente_2();
        
        $scope.parametrosReporteGestionCliente = function() {
            var objParams = {};
            
            objParams.p_IdEmpresa = $scope.SesionIdEmpresa;
            objParams.p_IdUsuario = $scope.SesionIdUsuario;
            objParams.p_IdAgente = $scope.data.oConfiguracionAgente.idActual;
            objParams.p_agno = $scope.data.kngFechaOperacion.getFullYear();

            return objParams;
        }
       
        $scope.buscar = function() {

        	// Restablece las columnas para el agno seleccionado
			var agno = $scope.data.kngFechaOperacion.getFullYear();

			$scope.gridApiReporteGestionCliente.setOptions({			
				
				columns : columnasReporteGestionCliente(agno)
			}); 
			
			// Establece los parametros
			$scope.dataSourceReporteGestionCliente.setCallParameters($scope.parametrosReporteGestionCliente());
			
        };

        $scope.limpiarFiltros = function() {
            $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
            $scope.data.oConfiguracionAgente.idActual= false;
            $scope.seleccionReporteGestionCliente = {};
            $scope.gridApiReporteGestionCliente.dataSource.removeAll();
        };
    });
