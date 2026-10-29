angular.module('ModuloSocioNegocio').controller('ReportePatrimonioController',
	function($scope,$translate,$resource, bcsComunes, bcsServicioPlataforma, servicioSociodeNegocio, bcsAlerta, OPCIONES_BCSALERTA)
	{		
		  var idVentana = bcsComunes.ventanaWinId($scope);
	        var fechaMinima = util.dateTerminoDia($scope.SesionFechaSistema);
	        
	        $scope.data = {};
	        $scope.data.IdEmpresa = $scope.SesionIdEmpresa;
	        $scope.data.IdUsuario = $scope.SesionIdUsuario;        
	        $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
	        $scope.seleccionReportePatrimonio = {};
	        
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
		// Tipo de datos para cada columna en el grid
		modeloReporteGestionCliente = function() {
			return {
				model: {
					fields : {
						"NombreAgente" 	    : { type: "string"},
						"NombreCliente"	    : { type: "string"},
						"NombreMercado"	    : { type: "string"},
						"Total"	    		: { type: "number"}						
					}
				}
			};
		};
		
		// Formato de columnas para los totales por mes
		$scope.dataSourceReportePatrimonio = new bcs.DataSource({
			type 		: "array", //Los tipos posibles son: odata, call, array y object
			definition 	: {
				schema	: modeloReporteGestionCliente()
			}  
		});
		
		// Columnas iniciales para el grid
		columnasReportePatrimonio = function() {
			let objColumnas = [
				 {field : "NombreAgente", 	title:	$translate.instant("UI_NOMBRE_AGENTE"),	width : "100px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }			 			 
				,{field : "NombreCliente", 	title:	$translate.instant("UI_NOMBRE_CLIENTE"),width : "100px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }
				,{field : "NombreMercado", 	title:	$translate.instant("UI_MERCADO"), 	 	width : "200px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }
				,{field : "Total", 			title:	$translate.instant("UI_TOTAL"), 	 	width : "100px", sortable : true, editable: false, attributes: {"class": "columnaNumero"} }
			];
			return objColumnas;
		};
		
		// Propiedades iniciales del grid
		$scope.gridPATR = {};
		var confBasicaReportePatrimonio = function () {
        	return {
			gridCreated : function(grid) {
				$scope.gridPATR = grid;
			},		
			dataSource: $scope.dataSourceReportePatrimonio,
            columns: columnasReportePatrimonio(),			
            contextMenu : true,
    		filterable	: true,
    		sortable 	: true,
    		pageable 	: true,
    		height 		: 450,
    		autoBind 	: true,		// Importante asignar esta propieda a true para mostrar valores en el grid 
    		selectable 	: "multiple row",
    		excel 		: { allPages: true },
        	};
        };
        $scope.confBasicaReportePatrimonio = confBasicaReportePatrimonio();

		// Formato numerico para las columnas de Mercados y Total General
		modeloFormatNumerico = function(columna, titulo) {
			let templateNumerico = "<label> #: (util.bcs_formato(" + columna +",'GEN','Cantidad','CLP','','')).valor #</label>";
			let objFormato = {	field : columna,
								title: titulo, 
								width : "100px", 
								sortable : false, 
								template : templateNumerico, 
								headerAttributes: {"class": "HeaderCenter"}, 
								attributes: {"class": "columnaNumero"}};
			return objFormato;
		}
		
        // Parametros para la consulta
        $scope.parametrosReportePatrimonio = function() {
            var objParams = {};
            objParams.p_IdEmpresa = $scope.SesionIdEmpresa;
            objParams.p_IdUsuario = $scope.SesionIdUsuario;
            objParams.p_IdAgente = $scope.data.oConfiguracionAgente.idActual;
            objParams.p_FechaConsulta = util.dateSPdate($scope.data.kngFechaOperacion);

            return objParams;
        }      
        
        $scope.buscar = function() {
        	let objColumnas = [];
        	let objMercados = [];
        	let objValores = [];
        	
        	bcsComunes.ventanaOverlay(idVentana,true);
        	objColumnas = [
			 	 {field : "Agente", 		title:	$translate.instant("UI_NOMBRE_EJECUTIVO"),  width : "200px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }			 			 
			 	,{field : "NombreCliente", 	title:	$translate.instant("UI_NOMBRE_CLIENTE"), 	width : "200px", sortable : true, editable: false, attributes: {"class": "columnaTexto"} }
			];
        	
			objMercados = {
					Agente 	    	: { type: "string"},
					NombreCliente	: { type: "string"}
				};
			
        	var p = servicioSociodeNegocio.ReportePatrimonio($scope.parametrosReportePatrimonio()).$promise;
        	p.then(function(result){
        		
    			if(result.P_OUT_MERCADOS){
    				// Loop para crear columnas y tipo de datos dinamicas de motos de mercados por cliente en el grid
    				$.each(result.P_OUT_MERCADOS,function(key,value){
    					
    					objColumnas.push(modeloFormatNumerico(value.NombreMercadoCartola,$translate.instant(value.NombreMercadoCartola)));
    					
    					objMercados[value.NombreMercadoCartola] = { type: "number"};
    					
    					
    				});
    				// Columna fija del total general
    				objColumnas.push(modeloFormatNumerico("Total", $translate.instant("UI_TOTAL")));
    				objMercados["Total"] = { type: "number"};
    				
    				let patrimonioValor = {};
    				let patrimonioDatos = {};

    				$.each(result.P_OUT_TBL,function(key,value){
					    					
    					$.each(result.P_OUT_MERCADOS,function(key2,value2){

    					patrimonioValor = patrimonioDatos[value2.IdMercadoCartola] || {Agente: value.Agente, NombreCliente : value.NombreCliente};
    					patrimonioValor[value2.NombreMercadoCartola] = value2.MontoTotalMercado;	// Asigna el valor al Mercado por Cliente

    						$.each(value.ListaMercadoCartola,function(key3,value3){
    						
    						patrimonioValor[value3.NombreMercadoCartola] = value3.MontoTotalMercado;	// Asigna el valor al Mercado por Cliente
    						
    						});
    					});


    					patrimonioValor["Total"] = value.TotalGeneral;	
						patrimonioDatos[value.IdMercadoCartola] = patrimonioValor;

						$.each(patrimonioDatos,function(key4,value4){
    					objValores.push(value4);
    					}); 

    				});
    				
    				// Arma el json con el registro de valores de mercados por clientes
    				   				
					
					var dataSource = {
							type : "array",
							definition : {
								data		: objValores,				// Valores de mercados por cliente
								schema  	: {
									model 	: { fields : objMercados }	// Tipo de campos con las columnas dinamicas de Mercado
								}
							}
						}
		        	
					// Actualiza el grid con las nuevas columnas y los nuevos parametros
					$scope.gridPATR.setOptions({
						dataSource : new bcs.DataSource(dataSource),
						columns : objColumnas 	// Columnas del grid
					});
    			}
    		},
    		function(result){
    			bcsComunes.ventanaOverlay(idVentana,false);
    			bcsComunes.ventanaMsg({
    					mensaje:$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"),
    					conflictos:util.parseServerResponse(result)
    				},idVentana);
    		}
    		);
			bcsComunes.ventanaOverlay(idVentana,false);
        };

        $scope.limpiarFiltros = function() {
            $scope.data.kngFechaOperacion = util.dateInicioDia($scope.SesionFechaSistema);
            $scope.data.oConfiguracionAgente.idActual= false;
            $scope.seleccionReportePatrimonio = {};
            $scope.confBasicaReportePatrimonio.dataSource.removeAll();
        };
	});