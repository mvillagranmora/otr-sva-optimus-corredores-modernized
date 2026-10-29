angular.module('ModuloPlataforma').controller('FormatoArchivoCtrl',function($scope,bcsComunes,bcsServicioPlataforma,$resource,$log,$translate,$rootScope){
	var codigoArchivo;
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.idVentanaFormatoArchivo = idVentana;
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana) + ((parametros.titulo!=undefined)?" - "+parametros.titulo:" "));
	codigoArchivo = parametros.codigoArchivo;
	origenFormato = parametros.origenFormato;
	$scope.data = {};
	$scope.data.FormatoArchivo = {};
	
	var filterFile = new Array();
	filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
	filterFile.push({ field: "CodigoArchivo", operator: "eq", value: codigoArchivo });
	
	const EjemploLinea = async function getEjemploLinea(){
		var dataEjemplo = await new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1358",
						dataType:"json"
					}
				},
				filter: filterFile,
				schema : ModeloFormatoArchivo()
		  	}
		});
		
		dataEjemplo.fetch().then(function(){
			var str = '';
			var count = 0;
			var data = dataEjemplo.data();
			data.forEach(e => {
				if (e.Ejemplo.length>0){
					count++;
				}
				switch (origenFormato) {
					case "CALIFICACIONES_TRIBUTARIAS":
						$scope.data.MostrarEjemploDescarga = true;
						str += e.CodigoCampoTra;
						break;				
					default:
						str += e.Ejemplo;
						break;
				}
				
				str += e.SeparadorColumnas;
			})
			
			if (count>0){
				$scope.data.MostrarEjemplo = true;
				$scope.data.EjemploLinea = str;	
			}
		});	
	}
	
	dataSourceFormatoArchivo = function() {
		return new bcs.DataSource({
				type : "odata", 
				definition : {
					serverFiltering : true, 
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1358",
							dataType : "json"
						}
					},
					filter: filterFile,
					schema : ModeloFormatoArchivo()
				}
			}
		);
	};

	columnasFormatoArchivo = function() {
		return [ 
    	   	     {field: "Posicion", title: $translate.instant('UI_POSICION'), attributes:{style:"text-align:center;"} }
    	   	    ,{field: "CodigoCampoTra", title: $translate.instant('UI_CAMPO'), width: "200px", attributes:{style:"cursor:pointer;text-align:left;",title:"{{dataItem.Abreviado}}"} }
    	   	    ,{field: "TipoDato", title: $translate.instant('UI_TIPO'), attributes:{style:"text-align:center;"}  }
    	   	    ,{field: "LargoCampo", title: $translate.instant('UI_LARGO_CAMPO'), attributes:{style:"text-align:right;"} }
    	   	    ,{field: "CantidadDecimales", title: $translate.instant('UI_DECIMALES'), attributes:{style:"text-align:right;"} }
    	   	    ,{field: "EsObligatorio", title: $translate.instant('UI_OBLIGATORIO'), attributes:{style:"text-align:center;"} }
				,{field: "Ejemplo", title: $translate.instant('UI_EJEMPLO'), attributes:{style:"text-align:right;"} }
    	   	    
			];
	};
	
	ModeloFormatoArchivo = function() {
		return {
			model: {
	        	fields:{
	        		Posicion 			: { type : "number"},
	        		CodigoCampoTra 		: { type : "string"},
	        		TipoDato 			: { type : "string"},
	        		LargoCampo			: { type : "number"},
	        		CantidadDecimales 	: { type : "number"},
	        		EsObligatorio		: { type : "string"},
	        		Ejemplo				: { type : "string"},
	        	}
			}
		};
	};
	
	$scope.data.FormatoArchivo = {
		dataSource			: dataSourceFormatoArchivo(),
		columns				: columnasFormatoArchivo(),		
		sortable			: true,
		autoBind			: true,  //comentar
		navigatable			: true
	};
	
	$scope.cerrar = function () {
		bcsComunes.ventanaCerrar(idVentana);	
	};
	
	EjemploLinea();

	$scope.downloadCSV = function(){
		var content = $scope.data.EjemploLinea;	  
		var blob = new Blob([content], { type: 'text/csv' });
		var link = document.createElement('a');
		link.href = window.URL.createObjectURL(blob);
		link.download = origenFormato+'.csv';
	  
		// Simular un clic en el enlace para iniciar la descarga
		link.click();
	  };
	
});