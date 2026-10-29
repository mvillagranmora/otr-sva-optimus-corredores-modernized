angular.module('ModuloPlataforma').directive('fileReader', function() {
  return {
    scope: {
      fileReader:"="
    },
    link: function(scope, element) {
      $(element).on('change', function(changeEvent) {
        var files = changeEvent.target.files;
        if (files.length) {
          var r = new FileReader();
          r.onload = function(e) {
              var contents = e.target.result;
              scope.$apply(function () {
                scope.fileReader = contents;
              });
          };
          
          r.readAsText(files[0],'iso-8859-1');
        }
      });
    }
  };
});

angular.module('ModuloPlataforma').controller('CargaMasivaDiccionarioCtrl',function($scope,bcsComunes,$resource,$translate){
	
	$scope.data = {};
	$scope.data.ignorarPrimeraLinea = true;
	$scope.idVentana = bcsComunes.ventanaWinId($scope);
	
	var csvToArray = function (csvString,skipFirstLine){
		// The array we're going to build
		var csvArray   = [];
		// Break it into rows to start
		var csvRows    = csvString.split(/\n/);
		// Take off the first line to get the headers, then split that into an array
		var csvHeaders = [ "CodDiccionario","CodLenguaje","Descripcion","Abreviado"  ];
		
		// Loop through remaining rows
		for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
			
			var rowArray  = csvRows[rowIndex].split(';');		
			
			if ((rowIndex === 0 && skipFirstLine === true) || (rowArray.length < 4)) {
				console.log("IGNORADO : " +  JSON.stringify(rowArray));
				continue;
			}
			
		    // Create a new row object to store our data.
		    var rowObject = {};
		    // Then iterate through the remaining properties and use the headers as keys
		    for(var propIndex = 0; propIndex < csvHeaders.length; ++propIndex){
		      	//Grab the value from the row array we're looping through...
		    	var propValue =   rowArray[propIndex].replace(/^"|"$/g,'') || '';
		      //...also grab the relevant header (the RegExp in both of these removes quotes)
		      	var propLabel = csvHeaders[propIndex].replace(/^"|"$/g,'');
				
		      	rowObject[propLabel] = propValue.trim();
		    }
		    csvArray.push(rowObject);
		  }
  		return csvArray;
	};
	
	$scope.data.defaultOptions = {
			columns : [
			           {field: "CodDiccionario",width: "150px"},
			           {field: "CodLenguaje",width: "50px"},
			           {field: "Descripcion",width: "200px"},
			           {field: "Abreviado",width: "70px"},
			           ],
			sortable: true,
	      	reorderable: true,
	      	filterable: true,
			groupable: true,
			selectable: "multiple, row",
			reorderable: true,
			allowCopy: true,
			resizable: true,
	      	autoBind: false,
	      	pageable: true,
	      	scrollable: true,
	      	height:400,
	      	userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
	      	filterable: true,
	      	dataSource : new bcs.DataSource({
	      		type: "odata",
	      		definition : {
	      			serverFiltering: true,
					serverSorting: true,
					serverPagina: true,
					pageSize: 1000,
					schema: {
						model : {
							fields : {
								CodDiccionario : { type: "string"},
				           		CodLenguaje  : { type: "string"},
				           		Descripcion  : { type: "string"},
				           		Abreviado  : { type: "string"},
				           	}
						}
					},
					transport: {
						read: {
							url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=1555",
							dataType: "json"
						}
					}
	      			
	      		}
	      	})
	};
	
	$scope.data.buscar = function() {
		$scope.data.defaultOptions.dataSource.query();
	};
	
	$scope.data.guardarDiccionario =  function() {
		
		var diccionario = csvToArray($scope.data.fileContent,true);

		var final = [];
		var repetidos = [];

		var indexDiccionario = {};
		for (var j = 0 ;j < diccionario.length; j++) {
			if (indexDiccionario[diccionario[j].CodDiccionario] === undefined) {
				final.push(diccionario[j]);
				indexDiccionario[diccionario[j].CodDiccionario] = diccionario[j];
			}
			else {
				repetidos.push(diccionario[j]);
			}
		}
		
		for (var z = 0; z < repetidos.length ; z++) {
			bcsComunes.msg("error","error","Valor Repetido: " + repetidos[z].CodDiccionario);
		}

		console.log("ENVIANDO: " + diccionario);
		//llamar servicio
		
		var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=67";
		var r = $resource("", {},
	    {
			'cargarDiccionario': { method: "POST", url: url}
		});
		var datos = {
			p_Diccionario : final,
			p_Lenguaje : $scope.SesionLocale
		}
		
		if (!final || final.length === 0) {
			bcsComunes.msg("info","info","No se encontraron registros validos en el archivos");
			return;
		}

		bcsComunes.ventanaOverlay($scope.idVentana,true);
		var post = r.cargarDiccionario(datos);
		post.$promise.then(
			function(result) {
				console.log(result);
				bcsComunes.msg("info","info","Diccionario cargado");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			},
			function(result) {
				console.log(result);
				bcsComunes.msg("error","info","Error al cargar el diccionario");
				bcsComunes.ventanaOverlay($scope.idVentana,false);
			}
		);
		
	};
	
	//MODIFICA DICCIONARIO
	//shechenleitner 17-05-2017	
	$scope.editarDiccionario = function(datos){		
		if (datos.length > 1)
			bcsComunes.msg("error","error",$translate.instant('UI_DEBE_SELECCIONAR_UNICO_REGISTRO'));
		var idVentanaEdicion = bcsComunes.ventanaAbrir("EditarDiccionario", {oDiccionario: datos[0]},false,false,$scope.idVentana)		
		var promesa = bcsComunes.ventanaPromesa(idVentanaEdicion);			

		promesa.then(function(){
			bcsComunes.ventanaOverlay(idVentanaEdicion,false);
			$scope.data.buscar();
		});
	};
	
	$scope.crearDiccionario = function(){		
		var idVentanaEdicion = bcsComunes.ventanaAbrir("EditarDiccionario", null ,false,false,$scope.idVentana)		
		var promesa = bcsComunes.ventanaPromesa(idVentanaEdicion);		
		
		promesa.then(function(){ 
			bcsComunes.ventanaOverlay(idVentanaEdicion,false);
			//$scope.data.buscar();
		});
	};
});