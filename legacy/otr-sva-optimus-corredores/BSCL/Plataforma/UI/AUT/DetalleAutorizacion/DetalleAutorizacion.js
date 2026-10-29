angular.module('ModuloPlataforma').controller('DetalleAutoriacionCtrl',
		function($scope,$uibModal,$translate,bcsComunes,$rootScope, bcsAlerta, OPCIONES_BCSALERTA){
	
	var bModifMvtoMod = this;	
	//Recupera parametros desde la ventana recibida
    var idVentana = bcsComunes.ventanaWinId ($scope); 
    var clasificacion = 1;
    var clasificacion2 = 2;
    $scope.parametros 	= bcsComunes.ventanaParams(idVentana);

	console.log("id_evento "+$scope.parametros.IdEvento);

    $scope.MvtoComprobanteDatasource = new bcs.DataSource({
        type: "odata",
        definition 	: {
			                  serverFiltering 	: true, // Filtrado de datos en el servidor.
			                  serverPaging 		: true, // Paginado de los datos en el servidor.
			                  serverSorting 	: true, // Ordenamiento de los datos en el servidor.
			                  sort 				: {}, // Criterios de Ordenamiento.
			                                  
			                  transport 		: { read: {
										                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700000",
										                dataType: "json"
									 	                  }
								                  },
								                  pageSize 			: 50,
							   schema : { 
                                                  model : {
                                                             fields : {
                                                            	 IdEventoDetalle		: { type: "number"},
     								      						NombreCampo			: { type: "string"},
     								      						TipoDato		    : { type: "string"},
     								      						Valor	    	    : { type: "string"},
                                                             }
                                                  }
                               			},	
                              filter: [	{ field  : "IdEvento",      operator: "eq", value  : $scope.parametros.IdEvento}
                              		  ]
		                  }
    }); 
    
    $scope.MvtoComprobanteDatasource2 = new bcs.DataSource({
        type: "odata",
        definition 	: {
			                  serverFiltering 	: true, // Filtrado de datos en el servidor.
			                  serverPaging 		: true, // Paginado de los datos en el servidor.
			                  serverSorting 	: true, // Ordenamiento de los datos en el servidor.
			                  sort 				: {}, // Criterios de Ordenamiento.
			                                  
			                  transport 		: { read: {
										                url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=700000",
										                dataType: "json"
									 	                  }
								                  },
								                  pageSize 			: 50,
							   schema : { 
                                                  model : {
                                                             fields : {
                                                            	 IdEventoDetalle          		: { type: "number"},
     								      						NombreCampo			: { type: "string"},
     								      						TipoDato		    : { type: "string"},
     								      						Valor	    	    : { type: "string"},
                                                             }
                                                  }
                               			},	
                              filter: [	
                            	  			{ field  : "IdEvento",      operator: "eq", value  : $scope.parametros.IdEvento},
                            	  			{ field  : "Clasificacion",      operator: "eq", value  : clasificacion2} 
                              		  ]
		                  }
    });  
    
    
    var filters = [];
    filters.push({field: "IdEvento", operator: "eq", value: $scope.parametros.IdEvento});
    filters.push({ field  : "Clasificacion",      operator: "eq", value  : clasificacion});

    $scope.MvtoComprobanteDatasource.query({filter:filters}).then(function(){
		$scope.$apply(function() {
		bModifMvtoMod.oMovimientoComprobante 	= $scope.MvtoComprobanteDatasource.data();
		});
	});
    

    //------------------------------------------------------ gri
    $scope.DetalleMensaje =  {
			dataSource			: $scope.MvtoComprobanteDatasource2,
			userSettings		: true,
			columns				: [	{field: "IdEventoDetalle",title: "UI_ID",width: "100px", hidden:true},
									{field: "NombreCampo",title: "UI_SECUENCIA",width: "100px", hidden:false},
									{field: "TipoDato",title: "UI_RESULTADO",width: "100px", hidden:false},
									{field: "Valor",title: "UI_INFORMACION",width: "100px", hidden:false}],	
			sortable			: true,
			pageable			: true,
			autoBind			: true,  //carga la data automaticamente
			height				: 75,
		    selectable 			: "multiple row",
		    filterable			: false,
		    userSettings		: true,
			navigatable			: true,
			allowCopy 			: true,
			excel : {
				allPages: true,
			},
		};

});
