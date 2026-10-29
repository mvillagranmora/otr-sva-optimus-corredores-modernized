angular.module('ModuloAPV').directive('fileReader', function(bcsComunes,$translate) {
	return {
		//restrict: 'A',
		scope: {
			ngAccept:"=",
			idVentana:"=",
			callback:"=?"
		},
		link: function(scope, element) {
			$(element).on('change', function(changeEvent) {
				if(scope.idVentana) bcsComunes.ventanaOverlay(scope.idVentana,true);
				var files = changeEvent.target.files;
				if (files.length) {
					var r = new FileReader();
					r.onload = function(e) {
						var contents = e.target.result;
						var archivo = {
								contenido : contents,
								nombre : files[0].name
						}
						if(scope.idVentana) bcsComunes.ventanaOverlay(scope.idVentana,false);
						scope.callback(archivo);
					};
					r.readAsText(files[0],'iso-8859-1');
				}
				
			});
			
			if(scope.ngAccept) $(element).attr('accept',scope.ngAccept);
			
		}
	};
});

	
angular.module('ModuloAPV').controller('CargaRegistroHistoricoTraspaso',	function($scope,bcsComunes,bcsServiciosAPV,$resource,$log,$translate){
	$scope.data = {};
	$scope.data.conHeader = false;
	$scope.erroresValidacion = 0;
	$scope.config= {};
	$scope.configArchivo = {};
	$scope.configArchivoTotal = {};
	$scope.configColumnas = [];
	$scope.config.overlay = bcsComunes.overlay;
	$scope.errors = {};
	$scope.errors.erroresFatales = [];

	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.idVentanaCargaArchivo = idVentana;

	var parametros = bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana) + ((parametros.titulo!=undefined)?"  "+parametros.titulo:" "));
	
	$scope.configArchivo.codigoArchivo = parametros.codigoArchivo;
	$scope.configArchivo.formatoArchivoDefecto = parametros.formatoArchivoDefecto;
	$scope.configArchivo.funcionesDependencia = parametros.funcionesDependencia;
	

	var parseField = function(fieldConfig, value){
		if(fieldConfig){
			if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="STRING"){
				value = ((fieldConfig.ForzarMayuscula && fieldConfig.ForzarMayuscula=='S')? String(value).toUpperCase() : String(value)).replace("\r","");
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="NUMBER"){
				try{
					if(fieldConfig.IncluyeDecimales==="S"){
						value = util.incluirDecimales(value.replace(/,|\./g,''), fieldConfig.CantidadDecimales);
					}else{
						value = (value && value!="")?parseInt(value) : 0;
					}
				}catch(e){
					$log.debug(e);
				}
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="DOUBLE"){
				try{
					value = parseFloat(value);				
				}catch(e){
					$log.debug(e);
				}
			}
		}
		
		return value;
	}
	
	var validateField = function(fieldConfig, value){
		if(!fieldConfig) return false;
		if(fieldConfig.EsObligatorio==="S" && value.length<=0){
			return fieldConfig.CodigoCampo;
		}
		if(fieldConfig.Validacion && fieldConfig.Validacion.trim().length>0){
			var re = new RegExp(fieldConfig.Validacion);
			if(!re.test(value)){
				return fieldConfig.CodigoCampo;
			}
		}
		if(fieldConfig.TipoDato==="NUMBER" || fieldConfig.TipoDato==="DOUBLE"){
			if(!util.isNumeric(value) && fieldConfig.EsObligatorio==="S"){
				return fieldConfig.CodigoCampo;
			}
		}
		return "OK";
	}

	var allCamposObligatorios = function(config){
		for(var key in config.campos){
			if(config.campos[key].EsObligatorio=='S' && !config.campos[key].Existe){
				return false;
			}
		}
		return true;
	}
	
	var getColumnFormat = function(fieldConfig){
		if(fieldConfig.TipoDato.toUpperCase()=="NUMBER"){
			if(fieldConfig.IncluyeDecimales.toUpperCase()=="S"){
				return "{0:n"+fieldConfig.CantidadDecimales+"}"
			}else{
				return "{0:n0}";
			}
		}else if(fieldConfig.TipoDato.toUpperCase()=="DOUBLE"){
			return "{0:n3}";
		}else{
			return "{0:s}";
		}
	}

	var validateDependencies = function(linea){
		for(var i=0; i < $scope.configArchivo.funcionesDependencia.length;i++){
			var resultado = $scope.configArchivo.funcionesDependencia[i](linea);
			if(resultado!=="OK"){
				return resultado;
			}else if(i==$scope.configArchivo.funcionesDependencia.length-1){
				return "OK";	
			}
		}
	}
		
	var csvToArray = function (csvString, config, total){
		// The array we're going to build
		var csvArray   = [];
		// Break it into rows to start
		var csvRows    =csvString.split(/\r\n|\r|\n/g);
		// Take off the first line to get the headers
		//var csvHeaders = [];
		var errors = {};
    	//LOGICA PAC
		var textoFinal = "";
		var textoPrincipio = "";
		var cont =1;
		
		var filaTotal = csvRows.splice(csvRows.length);
				
		var datosArchivo = total == "SI" ? filaTotal : csvRows;
		
		for(var rowIndex = 0; rowIndex < datosArchivo.length; ++rowIndex){
			var fileRow = datosArchivo[rowIndex].replace(/\r?\n|\r/g, "");//eliminar el retorno de carro
			var rowArray = [];

			if(rowIndex > datosArchivo.length+999 ){
				//Si es la ultima fila y esta vacia terminar (fix para archivos csv).
				break;
			}
			
			if(rowIndex==0){
				var firstRow = [];
				if(config.tieneSeperador==='S'){
					//esta en la primera linea y tiene separador por lo tanto, validar.
					firstRow = fileRow.split(new RegExp(config.SeparadorColumnas));
					
					if($scope.data.conHeader==true){
						cleanLastBlankArray(firstRow);

						for(var key in firstRow){
							var campoKey = (firstRow[key].indexOf(" ") < 0 )? firstRow[key] : firstRow[key].replaceAll(" ", "");
							//console.log(config.campos[campoKey]);
							if(config.campos[campoKey]){
								config.campos[campoKey].Existe = true;
							}
							csvHeaders.push(campoKey);
						}
	 					
						for(var key  in config.campos){ 
							var formato = config.campos[key];
							if(formato.EsObligatorio==='S'){
								if(!formato.Existe || fileRow.indexOf(formato.CodigoCampo) < 0){
									errors.codigo = "ERROR";
									errors.mensaje =  $translate.instant("UI_CAMPO_XXX_OBLIGATORIO").replace("XXX", formato.CodigoCampo);
									//"El campo: "+formato.CodigoCampo+" es obligatorio en la cabezera del archivo."
								}
							}
						}
						continue;	
					}else{
						//Tiene separador pero no tiene cabecera.
						//console.log(firstRow);
						var cantidadCamposObligatorios =0;
						for(var key  in config.campos){
							if(config.campos[key].EsObligatorio==="S"){
								cantidadCamposObligatorios++;
							}
							csvHeaders.push(config.campos[key].CodigoCampo);
						}
						if(firstRow.length>=cantidadCamposObligatorios){
							for(var key  in config.campos){
								if(config.campos[key].EsObligatorio==="S"){
									config.campos[key].Existe="S";
								}
							}
						}

					}
				}
				else
				{
					//si es la primera fila, y es sin separador, validar el largo de la columna en la tabla de configuracion.
					var largoRow = total == "SI" ? 53    : 1;
					for(var key in config.headers)
					{
						largoRow += parseInt(config.campos[config.headers[key]].LargoCampo);
					}
					
					if (config.tieneCabecera==='S')  //Cuando tenga encabezado no debe procesar la primera fila
					{
						continue;
					}
					
				}
			}

			//ahora construir el arreglo con los datos del archivo.
			var data = {};
			
			if(config.tieneSeperador==='S'){
				//parse por key
				rowArray = fileRow.split(new RegExp(config.SeparadorColumnas));
				
				cleanLastBlankArray(rowArray);
				
				var validacion = "OK";
				for(var key in rowArray){
					if (key < csvHeaders.length){
						var fieldConf = config.campos[csvHeaders[key]];
						var value = rowArray[key];
						data[csvHeaders[key]]= parseField(fieldConf, value)
						var mensaje = validateField(fieldConf, data[csvHeaders[key]]);
						if(mensaje!="OK")validacion=$translate.instant( ((fieldConf && fieldConf.TipoDato.toUpperCase()==="STRING")?"UI_FALTA_CAMPO_XXX_ARCHIVO": "UI_CAMPO_XXX_ERROR_FORMATO") ).replace("XXX", mensaje);	
					}
				}
				
				if(rowArray.length < cantidadCamposObligatorios){
					validacion = $translate.instant("UI_FALTA_CAMPO");
				}
				
				if($scope.configArchivo.funcionesDependencia && $scope.configArchivo.funcionesDependencia.length>0){
					validacion = validateDependencies(data);
				}
				data["NroRegistro"] = cont;
				cont=cont+1;
				data["MENSAJEVALIDACION"] = validacion;
				csvArray.push(data);
				if (validacion !== 'OK')
					$scope.erroresValidacion++;
				
			}else{
				if(config.tieneSeperador==='N'){
					//parse por largo
					var posicionInicial = 0;
					var posicionFinal = 0;



					var validacion = "OK";
					
					for(var key in config.headers){
						posicionFinal = parseInt(config.campos[config.headers[key]].LargoCampo) + posicionInicial;

						posicionFinal = 20000;
						
						data[config.headers[key]] = parseField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );

						var mensaje = validateField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );
						if(mensaje!="OK")
							validacion=mensaje;

						
					}
					
					var largo = total == "SI" ? (fileRow.substring(0,60).length + 1) : fileRow.trim().length;
					
					if(largoRow != largo)
					{
						validacion = $translate.instant("UI_FILA_XXX_CON_ERROR").replace("XXX", String(rowIndex+1));
						//"La fila "+(rowIndex+1)+" del archivo, no es del mismo tamaño que la definida en la configuracion."
					}
					
					data["NroRegistro"] = cont;
					cont=cont+1;
					data["MENSAJEVALIDACION"] = validacion;
					csvArray.push(data);
					
					if (validacion !== 'OK')
						$scope.erroresValidacion++;
				}
			}
			
			
			
		}
		if(errors.codigo){
			return errors;//existen errores en la cabecera, desplegarlos en la pantalla.
		}else{
			return csvArray;
		}
  		
	};
	
	var ArchivosFormatoDatasource = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
	//	serverSorting   : true, // Ordenamiento de los datos en el servidor.
	    sort : [{ field: "IdFormato", dir: "ASC"}],
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1358",
				dataType: "json"
		    }
		},
		schema: {
        	model:
    		{
        		fields : {
        			numero: {type:"string"},
        		

				}
    		}
    	},
	});
	

	
	$scope.data.guardarDiccionario = function(archivo) {
		$scope.erroresValidacion = 0;
		$scope.archivo = archivo;
		if($scope.configColumnas.length > 0){
			bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, true);
			$scope.gridApiCargaArchivo.dataSource.read();//limpiar la grilla
			

			$scope.configArchivo.tieneSeperador = $scope.configColumnas[0].TieneSeparador;	
			$scope.configArchivo.tieneCabecera = $scope.configColumnas[0].TieneCabecera;
			
			if($scope.configArchivo.tieneSeperador==='S'){
				//archivo con separador de columnas	
				$scope.configArchivo.SeparadorColumnas = ($scope.configColumnas[0].SeparadorColumnas.indexOf("\\asd")>=0) ? $scope.configColumnas[0].SeparadorColumnas : "\\asd"+$scope.configColumnas[0].SeparadorColumnas;
			}
			$scope.configArchivo.headers = new Array();
			$scope.configArchivo.campos = {};
			$.each($scope.configColumnas, function(key, value){
				value.Existe = false;
				$scope.configArchivo.headers.push(value.CodigoCampo);
				$scope.configArchivo.campos[value.CodigoCampo] = value;
				$scope.configArchivo.rowSize =+ value.LargoCampo;

			});
			$scope.file = {};
			$scope.file.fileData = csvToArray(archivo.contenido,$scope.configArchivo, "NO");
			if(!$scope.file.fileData.codigo){
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
				//construir columnas de la grilla
				$scope.gridApiCargaArchivo.dataSource.data($scope.file.fileData, false);
				$scope.gridApiCargaArchivo.refresh();
				
				//se deja nombre archivo en objeto salida
				$scope.file.fileName = archivo.nombre;
				$scope.file.fileData = archivo.contenido;
			}else{
				bcsComunes.msg("error",'Error', $scope.file.fileData.mensaje);
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
			}
		}else{
			errorArchivoNoExiste();
		}
		if($scope.configColumnas.length > 0){
			bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, true);			

			//$scope.configArchivoTotal.tieneSeperador = $scope.configColumnasTotal[0].TieneSeparador;	
			//$scope.configArchivoTotal.tieneCabecera = $scope.configColumnasTotal[0].TieneCabecera;
			

			$scope.configArchivoTotal.headers = new Array();
			$scope.configArchivoTotal.campos = {};
			$.each($scope.configColumnas, function(key, value){
				value.Existe = false;
				$scope.configArchivoTotal.headers.push(value.CodigoCampo);
				$scope.configArchivoTotal.campos[value.CodigoCampo] = value;
				$scope.configArchivoTotal.rowSize =+ value.LargoCampo;

			});
			
		/*	totalArchivoArray = csvToArray($scope.archivo.contenido,$scope.configArchivoTotal, "SI");
			if(!totalArchivoArray.codigo){
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
	
				//se deja nombre archivo en objeto salida
				$scope.file.fileName = archivo.nombre;
			}else{
				bcsComunes.msg("error",'Error', totalArchivoArray.mensaje);
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
			}*/
			bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
		}
	};
	
	$scope.confBasicaCargaArchivo = {
			dataSource : new bcs.DataSource({
				definition : {},
				type : "array"
			}),
			columns : [],
			gridCreated: function(grid) {
				$scope.gridApiCargaArchivo = grid;
				$scope.buildGrid();
			},
			rowColorBackground : function(row) {
				if (row["MENSAJEVALIDACION"] !== "OK") {
					return "red";
				}
			},
			sortable: true,
			height: 200,
			selectable : "multiple row"
	};
	

	$scope.buildGrid = function(){

			var filterFile = new Array();
			filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
			filterFile.push({ field: "CodigoArchivo", operator: "eq", value: $scope.configArchivo.codigoArchivo });
		
		var columnas = [];
		var campos = {};
		
		ArchivosFormatoDatasource.query({sort: {field: "IdFormato", dir: "asc"}, filter:filterFile}).then(function() {
			$scope.configColumnas = ArchivosFormatoDatasource.view();
			

				campos["numero"] = {type:"string"};  columnas.push({field:"numero", title:"XML", width:"300px" });

								
				var def = {
						dataSource : new bcs.DataSource({
							definition : {
								schema : {
									model: {
										fields : campos
									}
								}
							},
							type : "array"
						}),
						columns : columnas
					};
					var options = $scope.gridApiCargaArchivo.dataSource.getOptions();
					options.definition.schema.model.fields = campos;
					
					$scope.gridApiCargaArchivo.dataSource.setOptions(options);
					$scope.gridApiCargaArchivo.setOptions({columns: columnas});


		});
	};
	
	$scope.cancelar = function(){
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.CargaRegistroHistoricoTraspaso = function () {

			//parametros
		
			var p_parametros = {
					p_IdEmpresa					:	$scope.SesionIdEmpresa,
					p_IdModulo					:	"APV",
					p_IdUsuario					: 	$scope.SesionIdUsuario,
					p_NombreCarga  				: 	$scope.file.fileName,
					p_Origen 					: 	"Carga Registro Historico APV",
					p_FechaCarga				:	util.dateInicioDia($scope.SesionFechaSistema),
					p_Archivo					:	$scope.file.fileData,				
			};
			bcsServiciosAPV.CargaRegistroHistoricoTraspaso(p_parametros).$promise.then(function(respuesta){
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Carga Exitosa" ),conflictos:util.parseServerResponse(respuesta)},idVentana);	
	   			bcsComunes.ventanaCerrar(idVentana);
	   			
	   			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		for(var key in respuesta.P_OUT_MENSAJES){
		   			if(respuesta.P_OUT_MENSAJES[key].codigo ===  "ERROR")
		   				bcsComunes.msg("warning","warning", respuesta.P_OUT_MENSAJES[key].descripcion);

					bcsComunes.ventanaOverlay(idVentana,false);
				}			     	

				if(respuesta.P_OUT_MENSAJES.length > 0){
					var mensajeCarga = new Array();
					$.each(respuesta.P_OUT_MENSAJES,function(key,value){
						 var objCarga = {
								IdInterno : value.id,
								tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
								codigo : value.codigo,
								descripcion : $translate.instant(value.descripcion)
						 };
						 mensajeCarga.push(objCarga);
					});
					respuesta.P_OUT_RESULTADO = mensajeCarga;
					bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);							

					bcsComunes.ventanaOverlay(idVentana,false);
				}
	   		},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_MENSAJES || [];
	   			$.each(ErrorDetalle,function(key,value){
	   				if(value.tipo === 'ERROR')
	   					bcsComunes.msg("error","Error","Existen problemas: " + value.descripcion);
				});
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Fallo la carga en java" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   			bcsComunes.ventanaOverlay(idVentana,false);
	   		});

	}

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