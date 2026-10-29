angular.module('ModuloPlataforma').controller('dashboardCtrl',function($scope,$translate,bcsAlerta,bcsComunes,$resource, $timeout,$rootScope,$translate){

	var overlay = bcsComunes.overlay;
	
	////////////// ORDENES INGRESADAS POR AÑO/TIPO INSTRUMENTO ///////////////
	/*
	$scope.dataOI = [
 	        	{"Anio": 2014,"TipoInstrumento": "Acciones Nacionales","Cantidad": 100,},
	        	{"Anio": 2014,"TipoInstrumento": "Acciones Mila","Cantidad": 0,},
	        	{"Anio": 2014,"TipoInstrumento": "Simultaneas","Cantidad": 10,},
	            {"Anio": 2015,"TipoInstrumento": "Acciones Nacionales","Cantidad": 130,},
	        	{"Anio": 2015,"TipoInstrumento": "Acciones Mila","Cantidad": 30,},
	        	{"Anio": 2015,"TipoInstrumento": "Simultaneas","Cantidad": 50,},
	        	{"Anio": 2016,"TipoInstrumento": "Acciones Nacionales","Cantidad": 100,},
	        	{"Anio": 2016,"TipoInstrumento": "Acciones Mila","Cantidad": 10,},
	        	{"Anio": 2016,"TipoInstrumento": "Simultaneas","Cantidad": 5,},
	        ];
	*/
	$scope.datasourceOI = new kendo.data.DataSource({
        //data: $scope.dataOI,
        group: {
            field: "TipoInstrumento"
        }, 
        sort: {
            field: "Anio",
            dir: "asc"
        },
        schema: {
            model: {
                fields: {
                	"Anio": {
                        type: "numeric"
                    }
                }
            }
        }        
    });	
	
	$scope.seriesOI=[ { type: 'column' , field: 'Cantidad',  name: '#= group.value #' } ];
	
	$scope.valueaxisOI= { majorGridLines:  { visible: true }, labels: { format: '#=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#' } };
	
	$scope.categoryaxisOI={ field: "Anio" , majorGridLines:  { visible: true }, labels: { template: "#=value#"  }  } ;
	
	$scope.tooltipOI = { visible: true,  format: "{0}", template: '#= series.name #: #=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#'  };
	
	////////////// COMISIONES AGENTE POR AÑO/AGENTE  ///////////////
	/*
	$scope.dataCA = [
	            {"Anio": 2014,"Agente": "Smith","Monto": 25000,},
		        {"Anio": 2014,"Agente": "Doe","Monto": 40000,},
	        	{"Anio": 2015,"Agente": "Smith","Monto": 30000,},
	        	{"Anio": 2015,"Agente": "Doe","Monto": 30000,},
	        	{"Anio": 2015,"Agente": "Ulloa","Monto": 50000,},
	        	{"Anio": 2016,"Agente": "Smith","Monto": 105000,},
	        	{"Anio": 2016,"Agente": "Doe","Monto": 12000,},
	        	{"Anio": 2016,"Agente": "Ulloa","Monto": 15000,},
	        ];
	*/
	$scope.datasourceCA = new kendo.data.DataSource({
      //  data: $scope.dataCA,
        group: {
            field: "Agente"
        }, 
        sort: {
            field: "Anio",
            dir: "asc"
        },
        schema: {
            model: {
                fields: {
                	"Anio": {
                        type: "numeric"
                    }
                }
            }
        }        
    });	
	
	$scope.seriesCA=[ { type: 'column' , field: 'Monto',  name: '#= group.value #' } ];
	
	$scope.valueaxisCA= { majorGridLines:  { visible: true }, labels: { format: '#=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#' } };
	
	$scope.categoryaxisCA={ field: "Anio" , majorGridLines:  { visible: true }, labels: { template: "#=value#"  }  } ;
	
	$scope.tooltipCA = { visible: true,  format: "{0}", template: '#= series.name #: #=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#'  };	
	
	////////////// MONTOS AGENTE POR AÑO/AGENTE ACTUAL/ANTERIRO ///////////////
	/*
	$scope.dataMA = [
	        	{"Anio": 2015,"Agente": "Smith","Monto": 130000,},
	        	{"Anio": 2015,"Agente": "Doe","Monto": 30000,},
	        	{"Anio": 2015,"Agente": "Ulloa","Monto": 50000,},
	        	{"Anio": 2016,"Agente": "Smith","Monto": 100000,},
	        	{"Anio": 2016,"Agente": "Doe","Monto": 10000,},
	        	{"Anio": 2016,"Agente": "Ulloa","Monto": 5000,},
	        ];
	*/
	$scope.datasourceMA = new kendo.data.DataSource({
        //data: $scope.dataMA,
        group: {
            field: "Anio"
        }, 
        sort: {
            field: "Anio",
            dir: "asc"
        },
        schema: {
            model: {
                fields: {
                	"Anio": {
                        type: "numeric"
                    }
                }
            }
        }        
    });	
	
	$scope.seriesMA=[ { type: 'column' , field: 'Monto',  name: '#=group.value #' } ];
	
	$scope.valueaxisMA= { majorGridLines:  { visible: true }, labels: { format: '#=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#' } };
	
	$scope.categoryaxisMA={ field: "Agente" , majorGridLines:  { visible: true }, labels: { template: "#=value#", rotation:315  }  } ;
	
	$scope.tooltipMA = { visible: true,  format: "{0}", template: '#= series.name #: #=(util.bcs_formato(value,"ORD","Monto","CLP","","")).valor#'  };		
	
	////////////// DISTRIBUCION INSTRUMENTOS FINANCIEROS ///////////////
	/*$scope.dataIF = [
		        	{"TipoInstrumento": "Acciones Nacionales","Cantidad": 25000,"Anio": 2015},
		        	{"TipoInstrumento": "Acciones Mila","Cantidad": 5200,"Anio": 2015},
		            ];
		        	*/
	$scope.datasourceIF = new kendo.data.DataSource({
        //data: $scope.dataIF,      
        sort: {
            field: "Anio",
            dir: "asc"
        },
        schema: {
            model: {
                fields: {
                	"Cantidad": {
                        type: "numeric"
                    }
                }
            }
        }        
    });	
	
	$scope.seriesIF=[ { type: 'pie' , field: 'Cantidad', categoryField: "TipoInstrumento",  }];
	
	$scope.seriesDefaultsIF={ labels: { template: "#= kendo.format('{0:P0}', percentage)#",position: "insideEnd",visible: true,background: "transparent" }};
		
	$scope.tooltipIF = { visible: true, template: "#= category # - #= kendo.format('{0:P}', percentage) # [#= kendo.format('{0:N0}', value) #]"  };
	$scope.dashboard;
	(function(){
		//LLAMADA ANONIMA
		overlay.encendido = true;
		
		var urlCargarDashboard = "/BSCL/Plataforma/GWJAVA/PLA/call?t=298";
		var r = $resource("", {},
	    {
			'CargarDashboard': { method: "POST", url: urlCargarDashboard}
		});
		
		var datosCarga= {
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdModulo: 'ORD',
		};
		
		var post = r.CargarDashboard(datosCarga);
		try{
			post.$promise.then(function(result){
				$scope.dashboard = result;
				$scope.datasourceOI.data(convertData($scope.dashboard.P_ORDENES_INGRESADAS_RESUMEN));
				$scope.datasourceCA.data(convertData($scope.dashboard.P_TOTAL_COMISIONES_AGENTE));
				$scope.datasourceMA.data(convertData($scope.dashboard.P_TOTAL_COMISIONES_AGENTE_ANTERIOR));
				$scope.datasourceIF.data(convertData($scope.dashboard.P_ORDENES_INGRESADAS_ACTUAL));
				
				overlay.encendido = false;
			},function(){
				overlay.encendido = false;
			});
		}catch(e){
			bcsComunes.msj("Error", $translate.instant("UI_ERROR_CODIGO").replace("XXX", e.message));
			overlay.encendido = false;
		}
		var convertData = function(array) {			
			return Object.keys(array).map(function(k) { return array[k] });
		}
		
	})();
	
});

