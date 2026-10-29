angular.module('ModuloAPV').controller('AfiliacionMasivaCtrl',
	function($scope,$translate,$resource,$timeout,bcsComunes)
	{	

		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var row = '';

		$scope.data = {};
		
		var cargaInterfaces = this;
		
		
		
		//Configurar al principio
		
		cargaInterfaces.data = {};

		
		//Asigna valores iniciales
		var datos = {};

		
		var varIdPunta;
		var estado;
		
		$scope.data.tabCargaMasivaActive = true;
		
		var datosCarga = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "col1", dir: "ASC" },
                    pageSize        : 10,
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													Col1	: {type: "number"},
													Col2	: {type: "number"},
													Col3	: {type: "number"},
													Col4	: {type: "number"},
													Col5	: {type: "number"}

												}
											}
									  }

    			 }
		};
		//------ GRILLA
		var dsDatosCarga = new bcs.DataSource(datosCarga);
		$scope.confBasicaDatosCarga = 
		{
    		dataSource		: dsDatosCarga,
    			columns		: [  {field: "Col1", title: "Columna 1", width: "110px", align: "left", sortable : true, editable: false }
			      				,{field: "Col2", title: "Columna 2", width: "110px", align: "left", sortable : true, editable: false }
			      				,{field: "Col1", title: "Columna 1", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col2", title: "Columna 2", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col1", title: "Columna 1", width: "110px", align: "left", sortable : true, editable: false }
							  ],		
    			sortable	: true,
    			pageable	: true,
    			filterable	: false,
    			navigatable	: true,
    			height		: 200,
    			selectable 	: "row",
    		    autoBind 	: false
		};	
		
		var detalleRecaudacion = 
		{
				type : "odata",
				definition : 
				{
                    serverFiltering : true, // Filtrado de datos en el servidor.
                    serverPaging    : true, // Paginado de los datos en el servidor.
                    serverSorting   : true, // Ordenamiento de los datos en el servidor.
                    sort 			: { field: "col1", dir: "ASC" },
                    pageSize        : 10,
                    schema 			: { 
                    				   model: 
                    						{
												fields : 
												{
													Col1	: {type: "number"},
													Col2	: {type: "number"},
													Col3	: {type: "number"},
													Col4	: {type: "number"},
													Col5	: {type: "number"},
													Col6	: {type: "number"},
													Col7	: {type: "number"},
													Col8	: {type: "number"},
													Col9	: {type: "number"},
													Col10	: {type: "number"}
													
												}
											}
									  }

    			 }
		};
		//------ GRILLA
		var dsDetalleRecaudacion = new bcs.DataSource(detalleRecaudacion);
		$scope.confBasicaDetalleRecaudacion = 
		{
    		dataSource		: dsDetalleRecaudacion,
    			columns		: [  {field: "Col1", title: "Fecha Caja", width: "110px", align: "left", sortable : true, editable: false }
			      				,{field: "Col2", title: "Estado"    , width: "110px", align: "left", sortable : true, editable: false }
			      				,{field: "Col3", title: "Empleador" , width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col4", title: "Trabajador", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col5", title: "Monto $ DC", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col6", title: "Monto $ VOL", width: "110px", align: "left", sortable : true, editable: false }
			      				,{field: "Col7", title: "Monto $ Total", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col8", title: "Periodo Pago", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col9", title: "Folio Caja", width: "110px", align: "left", sortable : true, editable: false }
				      			,{field: "Col10", title: "Folio Planilla", width: "110px", align: "left", sortable : true, editable: false }
							  ],		
    			sortable	: true,
    			pageable	: true,
    			filterable	: false,
    			navigatable	: true,
    			height		: 200,
    			selectable 	: "row",
    		    autoBind 	: false
		};	
		$scope.fileChanged = function(input) {
			$scope.nombreArchivo = '';
			if(input.files[0]){
				$scope.fileName = input.files[0].name;
				if($scope.fileName){
						$scope.nombreArchivo = $scope.fileName;
				}
			}
		};

	});