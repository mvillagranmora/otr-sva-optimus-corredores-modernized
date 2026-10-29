angular.module('ModuloPlataforma').directive('fileReader', function(bcsComunes) {
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
				else if(scope.idVentana) bcsComunes.ventanaOverlay(scope.idVentana,false);
			});
			
			if(scope.ngAccept) $(element).attr('accept',scope.ngAccept);
			
		}
	};
});

angular.module('ModuloPlataforma').controller('CargarCalificacionCtrl',function($scope,bcsComunes,bcsServiciosNormativa,$resource,$log,$translate,$rootScope){	
	var codigoArchivo;
    var periodoComercial;
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.idVentanaCargaArchivo = idVentana;	
	codigoArchivo=parametros.codigoArchivo;
    periodoComercial=parametros.periodoComercial;
    idOrigen=parametros.idOrigen;
    dataSicavc=parametros.dataSicavc;
	$scope.tipoCarga = parametros.tipoCarga;
	$scope.factoresCalculados = false;
	$scope.validacionDatos = false;
	var tituloVentana = parametros.tipoCarga == 1 ? $translate.instant('UI_CARGAR_XFACTOR') : $translate.instant('UI_CARGAR_XMONTO')
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana) + ((parametros.titulo!=undefined)?" - "+parametros.titulo:" " + tituloVentana));

	$scope.config = {
		conDetalle : false,
		conPie : false,
		archivo : {
			conHeader : false,
			columnas : [],
			codigoArchivo : parametros.codigoArchivo,
            periodoComercial: parametros.periodoComercial,
			formatoArchivoDefecto : parametros.formatoArchivoDefecto,
			funcionesDependencia : parametros.funcionesDependencia,
		},
		detalle : {			
		},
		pie : {			
		},
	};

	$scope.errors = {
		erroresFatales : [],
		count : 0
	};
	
	var cleanLastBlankArray = function(row, config) {
		if(!!row && config.headers.length !==row.length && row[row.length-1].trim().length==0){
			row.pop(row.length-1);
			return true;
		}else{
			return false;
		}
			
	}

	var parseField = function(fieldConfig, value) {
		if(fieldConfig){
			if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="STRING"){
					value = ((fieldConfig.ForzarMayuscula && fieldConfig.ForzarMayuscula=='S')? String(value).toUpperCase() : String(value)).replace("\r","");
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="NUMBER"){
				try{
					if(fieldConfig.IncluyeDecimales==="S"){
						value = util.incluirDecimales(value.replace(/,|\.|%/g,''), fieldConfig.CantidadDecimales);						
					}else{
						if (value.match(/[a-z]/i)){
							if(codigoArchivo === "CARGA_LIQUIDAR_TES"){
								value = (value && value!="")?(value) : 0;
							}
						}else{
							value = (value && value!="")?parseInt(value) : 0;
						}
					}
				}catch(e){
					$log.debug(e);
				}
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="DOUBLE"){
				try{
					value = parseFloat(value.replace(',', '.'));				
				}catch(e){
					$log.debug(e);
				}
			}
		}
		
		return value;
	}
	
	var validateField = function(fieldConfig, value) {

		if(!fieldConfig) return false;

		if(fieldConfig.EsObligatorio==="S" && value.length<=0){
			return fieldConfig.CodigoCampo;
		}
        
        if(fieldConfig.CodigoCampo === 'MERCADO') {
            const mercados = ['AC', 'CF','FFM'];
            if(!mercados.includes(value)) {
                return fieldConfig.CodigoCampo;
            }
        }

        if(fieldConfig.CodigoCampo === 'NEMO') {
            if(Number(value.NEMO) === 0) {
                return fieldConfig.CodigoCampo;
            }  
        }

        if(fieldConfig.CodigoCampo === 'EJERCICIO'){
            if(Number(value) !== periodoComercial) {
                return fieldConfig.CodigoCampo;
            }
        }
		if(fieldConfig.CodigoCampo === 'AcogidoISFUT') {
            const opciones = ['S', 'N'];
            if(!opciones.includes(value)) {
                return fieldConfig.CodigoCampo;
            }
        }

        // Considerar que value puede ser string o numero.
		if(fieldConfig.Validacion && fieldConfig.Validacion.trim().length>0 && value.toString().length > 0 ){
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
		if(fieldConfig.TipoDato==="STRING" && value==="S" || fieldConfig.TipoDato==="STRING" && value==="S"){
			if(util.isNumeric(value) && fieldConfig.EsObligatorio==="S"){
				return fieldConfig.CodigoCampo;
			}
		}
		if(fieldConfig.TipoDato==="DATE"){
			if(new Date(value)  == "Invalid Date")
				return fieldConfig.CodigoCampo;			
		}
		return "OK";
	}
	
	var getColumnFormat = function(fieldConfig) {
		if(fieldConfig.TipoDato.toUpperCase()=="NUMBER"){
			if(fieldConfig.IncluyeDecimales.toUpperCase()=="S"){
				return "{0:n"+fieldConfig.CantidadDecimales+"}"
			}else{
				return "{0:n0}";
			}
		}else if(fieldConfig.TipoDato.toUpperCase()=="DOUBLE"){
			return "{0:n"+fieldConfig.CantidadDecimales+"}";
		}else{
			return "{0:s}";
		}
	}

	var validateDependencies = function(linea) {
		for(var i=0; i < $scope.config.archivo.funcionesDependencia.length;i++){
			var resultado = $scope.config.archivo.funcionesDependencia[i](linea);
			if(resultado!=="OK"){
				return resultado;
			}else if(i==$scope.config.archivo.funcionesDependencia.length-1){
				return "OK";	
			}
		}
	}
		
    // Esta funcion tiene validaciones puntuales para un flujo X
	var csvToArray = function (csvString, config) {

		var csvArray   = [];
		var csvRows    = csvString.split(/\n/);
		var csvHeaders = [];		
		var errors = {};
        let keyTbsicavc = new Set();

        let hayrepetidos = csvRows.some( fila => {
            let getKey =  fila.split(";").slice(0, 5).join('');

            if(keyTbsicavc.has(getKey)) {
                return true;
            } else {
                keyTbsicavc.add(getKey);
                return false;
            }
        });

        if(hayrepetidos) {
            errors.codigo = "warning";
            errors.mensaje = 'Archivo contiene filas duplicadas.';
            return errors;
        }

		for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
			var fileRow = csvRows[rowIndex].replace(/\r?\n|\r/g, "");//eliminar el retorno de carro
			var rowArray = [];

			if(rowIndex == csvRows.length-1 && fileRow.trim().length==0){
				//Si es la ultima fila y esta vacia terminar (fix para archivos csv).
				break;
			}
			
			if(rowIndex==0){
				var firstRow = [];
				if(config.tieneSeperador==='S'){
					//esta en la primera linea y tiene separador por lo tanto, validar.
					firstRow = fileRow.split(new RegExp(config.SeparadorColumnas));
					
					if($scope.config.archivo.conHeader==true){
						cleanLastBlankArray(firstRow, config);

						for(var key in firstRow){
							var campoKey = (firstRow[key].indexOf(" ") < 0 )? firstRow[key] : firstRow[key].replaceAll(" ", "");
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
								}
							}
						}
						continue;	
					}else{
						//Tiene separador pero no tiene cabecera.
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
					var largoRow = 0;
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
				
				cleanLastBlankArray(rowArray, config);
				
				var validacion = "OK";
				for(var key in rowArray){
                    
					if (key < csvHeaders.length){
						var fieldConf = config.campos[csvHeaders[key]];
						var value = rowArray[key];
						if(fieldConf.CodigoCampo.includes("FACTOR_")){
							data[csvHeaders[key]]=value
						}else{
							data[csvHeaders[key]]= parseField(fieldConf, value);
						}						  
                        let valObj = data[csvHeaders[key]];   

                        const calificacion = {};
                        for (const key in csvHeaders) {
                            const valor = rowArray[key];
                            calificacion[csvHeaders[key]] = valor;
                        }

                        if ((csvHeaders[key] == "NEMO")) {
                            valObj = {
                                EJERCICIO: rowArray[0],
                                MERCADO: rowArray[1],
                                NEMO: valObj, // rowArray[2]
                                FEC_PAGO: rowArray[3],
                            }
                        }

					    var mensaje = validateField(fieldConf, valObj);

						if((fieldConf.TipoDato.toUpperCase()==="DATE") && (mensaje!="OK")){
							validacion=$translate.instant("UI_CAMPO_DATE_XXX_ERROR_FORMATO").replace("XXX", mensaje);
						} else if (mensaje!="OK") {
							validacion = $translate.instant((value!=""?(fieldConf.TipoDato.toUpperCase()==="STRING"?"UI_CAMPO_XXX_ERROR_FORMATO_STRING":"UI_CAMPO_XXX_ERROR_FORMATO"):"UI_FALTA_CAMPO_XXX_ARCHIVO")).replace("XXX",$translate.instant(mensaje))+" Ejemplo: "+fieldConf.Ejemplo;
							if(csvHeaders[key] == "MERCADO") {
                                validacion = $translate.instant(`Mercado ${valObj} no permitido.`);
                            }
                            if(csvHeaders[key] == "NEMO") {
                                validacion = $translate.instant(`Nemo ${valObj} no ingresado en el ejercicio ${rowArray[0]}.`);
                            }
						}
						
                        if(codigoArchivo === "ARCHIVO_CALIFICACION_TRIBUTARIA" || codigoArchivo === "ARCHIVO_CALIFICACION_TRIBUTARIA_MONTO") {
							// Validacion FechaPago
							let fechaString = calificacion.FEC_PAGO;
							let partes = fechaString.split("-");
							let dateFechaPago = new Date(partes[2], partes[1] - 1, partes[0]);
                            let yearFechaPago = dateFechaPago.getFullYear();
                            if(yearFechaPago.toString() !== periodoComercial.toString()) {
                                mensaje = '';
                                validacion = 'El año de la fecha de pago no puede ser diferente a periodo comercial seleccionado: '+periodoComercial.toString();
                            }

							if(codigoArchivo === "ARCHIVO_CALIFICACION_TRIBUTARIA"){
								// Validacion suma de factores
								factorSum = (
									Number(calificacion.FACTOR_08) + Number(calificacion.FACTOR_09) 
									+ Number(calificacion.FACTOR_10) + Number(calificacion.FACTOR_11) 
									+ Number(calificacion.FACTOR_12) + Number(calificacion.FACTOR_13) 
									+ Number(calificacion.FACTOR_14) + Number(calificacion.FACTOR_15) 
									+ Number(calificacion.FACTOR_16) + Number(calificacion.FACTOR_17) 
									+ Number(calificacion.FACTOR_18) + Number(calificacion.FACTOR_19) );
						
								if (factorSum > 1) {
									mensaje = '';
									validacion = 'Suma de factores del 8 al 19 no puede ser mayor a 1.';
								}
							}

                            // Logica y validacion Secuencia repetida
                            let calificacionSecuencia = calificacion.SEC_EVE;
                            let calificacionKey = (calificacion.EJERCICIO+calificacion.MERCADO+calificacion.NEMO+calificacion.SEC_EVE+idOrigen+calificacion.FEC_PAGO).trim()
                            
                            const validatePk = dataSicavc.some( item => {
                                let pkSicavc = (item.EJERCICIO+item.MERCADO+(item.NEMO).trim()+item.SEC_EVE+item.IDORIGEN.trim()+item.FEC_PAGO);
                                return pkSicavc === calificacionKey;
                            });

                            const validateSecuencia = dataSicavc.some( item => {
                                let tbSecuencia = item.SEC_EVE;
                                return Number(tbSecuencia) === Number(calificacionSecuencia);
                            });

                            if (!validatePk) {
                                if (validateSecuencia) {
                                    mensaje = '';
                                    validacion = 'Secuencia de evento ya registrada.';
                                }
                            }
                        }                        
					}
				}
				
				if(rowArray.length < cantidadCamposObligatorios){
					validacion = $translate.instant("UI_FALTA_CAMPO");
				}
				
				if($scope.config.archivo.funcionesDependencia && $scope.config.archivo.funcionesDependencia.length>0){
					validacion = validateDependencies(data);
				}
				data["MENSAJEVALIDACION"] = validacion;
				csvArray.push(data);
				if (validacion !== 'OK'){
					$scope.errors.count++;
				}else{
					$scope.validacionDatos = true;
					if(codigoArchivo === "ARCHIVO_CALIFICACION_TRIBUTARIA"){
						$scope.factoresCalculados = true;
					}					
				}
					
				
			} else {
				if(config.tieneSeperador==='N'){
					//parse por largo
					var posicionInicial = 0;
					var posicionFinal = 0;



					var validacion = "OK";
					for(var key in config.headers){
						posicionFinal = parseInt(config.campos[config.headers[key]].LargoCampo) + posicionInicial;

						data[config.headers[key]] = parseField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );

						var mensaje = validateField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );
						if(mensaje!="OK")
							validacion=mensaje;

						posicionInicial = posicionFinal;
					}

					//aqui habia un fileRow.trim(), se elimina
					if(largoRow!=fileRow.length)
					{
						validacion = $translate.instant("UI_FILA_XXX_CON_ERROR").replace("XXX", String(rowIndex+1));
						//"La fila "+(rowIndex+1)+" del archivo, no es del mismo tamaño que la definida en la configuracion."
					}

					data["MENSAJEVALIDACION"] = validacion;
					csvArray.push(data);
					
					if (validacion !== 'OK')
						$scope.errors.count++;
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
		serverFiltering: true,
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
        			   IdArchivo: {type:"number"},
        			   IdEmpresa: {type:"number"},
        			   CodigoArchivo: {type:"string"},
        			   TieneSeparador: {type:"string"},
        			   SeparadorColumnas: {type:"string"},
        			   TieneCabecera: {type:"string"},
        			   IdFormato: {type:"number"},
        			   CodigoCampo: {type:"string"},
        			   TipoDato: {type:"string"},
        			   Posicion: {type:"string"},
        			   LargoCampo: {type:"string"},
        			   EsObligatorio: {type:"string"},
        			   Validacion: {type:"string"},
        			   MENSAJEVALIDACION: {type:"string"},
        			   IdArchivoFormato: {type:"number"},
        			   ForzarMayuscula: {type:"string"},
        			   CodigoArchivoDetalle: {type:"string"},
        			   CodigoArchivoPie: {type:"string"},
				}
    		}
    	},
    	sort : [{ field: "Posicion", dir: "DESC"}],
	});
	
	$scope.file = {};
	$scope.nombreArchivo = '';
	
	$scope.guardarArchivo = function(archivo){

        // evaluar si el archivo tiene detalle y pie
        if (!$scope.config.conDetalle && !$scope.config.conPie) {
            // cargar archivo tradicional, sin pie ni detalle

            bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, true);
            $scope.file = $scope.formatearArchivo($scope.config.archivo, $scope.gridApiCargaArchivo, archivo);
            bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);

			$scope.nombreArchivo = $scope.file.fileName;
        } else {
            var contenido = archivo.contenido;
            var filas = contenido.split(/\n/);
            if (!!filas && filas.length > 2) {
                var archivoTemp = archivo;
				//cargar header, siempre es solo la primera fila
				var header = filas[0];
				archivoTemp.contenido = header;
				$scope.file = $scope.formatearArchivo($scope.config.archivo, $scope.gridApiCargaArchivo, archivoTemp);
				$scope.nombreArchivo = $scope.file.fileName;
				
				while(cleanLastBlankArray(filas, $scope.config.archivo)){
					/*eliminar columas finales en blanco*/
				};

				//cargar detalle desde el segundo elemento a n-1
				var detalle = filas.slice(1, filas.length-1);
				archivoTemp.contenido = detalle.join("\n");
				$scope.file.detalle = $scope.formatearArchivo($scope.config.detalle, $scope.gridApiDetalle, archivoTemp);
				
				//cargar pie, solo n-1
				var pie = filas[filas.length-1];
				archivoTemp.contenido = pie;
				$scope.file.pie = $scope.formatearArchivo($scope.config.pie, $scope.gridApiPie, archivoTemp);
			}else{
				console.error("ARCHIVO INVALIDO, DEBE TENER A LO MENOS LINEAS: CABECERA, DETALLE, PIE");
			}	
		}
	}

	$scope.formatearArchivo = function(config, grid, archivo) {

		$scope.errors.count = 0;
		
		if(config.columnas.length > 0){
			bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, true);
			
			config.tieneSeperador = config.columnas[0].TieneSeparador;	
			config.tieneCabecera = config.columnas[0].TieneCabecera;
			
			if(config.tieneSeperador==='S'){
				//archivo con separador de columnas	
				config.SeparadorColumnas = (config.columnas[0].SeparadorColumnas.indexOf("\\")>=0) ? config.columnas[0].SeparadorColumnas : "\\"+config.columnas[0].SeparadorColumnas;
			}
			config.headers = new Array();
			config.campos = {};
			$.each(config.columnas, function(key, value){
				value.Existe = false;
				config.headers.push(value.CodigoCampo);
				config.campos[value.CodigoCampo] = value;
				config.rowSize =+ value.LargoCampo;

            });
			var file = {};
			file.fileData = csvToArray(archivo.contenido,config);
           
			if(!file.fileData.codigo){
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
				
				//construir columnas de la grilla
				if(!!grid){		
					var textoValidacionMax = "";
					var largoMensajeMax = 0;
					var columnas = grid.getOptions().columns;
					for(var i = 0; i < file.fileData.length; i++){
						var largoMensaje = file.fileData[i].MENSAJEVALIDACION.length;
						if(largoMensaje > largoMensajeMax){
							largoMensajeMax = largoMensaje;
							textoValidacionMax = file.fileData[i].MENSAJEVALIDACION;
						}
					}
					columnas.forEach(valor => {							
						if(valor.field.includes("MENSAJEVALIDACION")){
							valor.width = calcularAncho(textoValidacionMax) + "px";
						}
					});
					
					grid.setOptions({columns: columnas});
					grid.dataSource.read();//limpiar datos de la grilla
					grid.dataSource.data(file.fileData, false);
					grid.refresh();	
				}
								
			//se deja nombre archivo en objeto salida
			file.fileName = archivo.nombre;
			}else{
				bcsComunes.msg("error",'Error', file.fileData.mensaje);
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
			}
			return file;
            
		}else{
			errorArchivoNoExiste();
			return {};
		}
	};

	function calcularAncho(texto, factor = 6, minimo = 50, maximo = 1000) {
		const anchoCalculado = texto.length * factor;
		return Math.min(Math.max(anchoCalculado, minimo), maximo);
	}
	
	$scope.confBasicaCargaArchivo = {
			dataSource : new bcs.DataSource({
				definition : {},
				type : "array"
			}),
			columns : [],
			gridCreated: function(grid) {
				$scope.gridApiCargaArchivo = grid;
				$scope.buildGrid($scope.config.archivo, $scope.gridApiCargaArchivo);
			},
			rowColorBackground : function(row) {
				if (row["MENSAJEVALIDACION"] !== "OK") {
					return "red";
				}
			},
			sortable: true,
			pageable: true,
			height: 200,
			selectable : "row"
	};

	$scope.confBasicaDetalle = {
			dataSource : new bcs.DataSource({
				definition : {},
				type : "array"
			}),
			columns : [],
			gridCreated: function(grid) {
				$scope.gridApiDetalle = grid;
			},
			rowColorBackground : function(row) {
				if (row["MENSAJEVALIDACION"] !== "OK") {
					return "red";
				}
			},
			sortable: true,
			height: 200,
			selectable : "row"
	};

	$scope.confBasicaPie = {
			dataSource : new bcs.DataSource({
				definition : {},
				type : "array"
			}),
			columns : [],
			gridCreated: function(grid) {
				$scope.gridApiPie = grid;
			},
			rowColorBackground : function(row) {
				if (row["MENSAJEVALIDACION"] !== "OK") {
					return "red";
				}
			},
			sortable: true,
			height: 200,
			selectable : "row"
	};

	$scope.buildGrid = function(config, grid){
		//construir grilla de resultados
		var filterFile = new Array();
		filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
		filterFile.push({ field: "CodigoArchivo", operator: "eq", value: config.codigoArchivo });
		
		var columnas = [];
		var campos = {};
		
		ArchivosFormatoDatasource.query({filter:filterFile}).then(function() {
			config.columnas = ArchivosFormatoDatasource.view();
			
			if(config.columnas.length > 0){
				config.conHeader = (config.columnas[0].TieneCabecera==="S");
				
				//identificar si el archivo tiene detalle o pie
				if(config.columnas[0].CodigoArchivoDetalle && config.columnas[0].CodigoArchivoDetalle!==""){
					$scope.config.conDetalle = 	(config.columnas[0].CodigoArchivoDetalle!=="");
					$scope.config.detalle.codigoArchivo = config.columnas[0].CodigoArchivoDetalle;
					$scope.buildGrid($scope.config.detalle, $scope.gridApiDetalle);
				}
				if(config.columnas[0].CodigoArchivoPie && config.columnas[0].CodigoArchivoPie!==""){
					$scope.config.conPie = 	(config.columnas[0].CodigoArchivoPie!=="");
					$scope.config.pie.codigoArchivo = config.columnas[0].CodigoArchivoPie;
					$scope.buildGrid($scope.config.pie, $scope.gridApiPie);
				}

				$.each(config.columnas, function(key, value){
					//eliminar caracteres peligrosos en la cabecera del archivo
					value.CodigoCampo = value.CodigoCampo.replaceAll(" ", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("#", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("\\(%\\)", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("\\.", "");
					//construir columnas
					columnas.push({field: value.CodigoCampo, title:$translate.instant(value.CodigoCampo), width:(value.CodigoCampo.length)+"em", format: getColumnFormat(value), hidden:false,attributes:{title:$translate.instant(('XXX').replace('XXX',value.CodigoCampo))}});
					campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
				});
				campos["MENSAJEVALIDACION"] = {type:"string"};
				columnas.push({field:"MENSAJEVALIDACION", title:"VALIDACION", width:"600px" });

				if(!!grid){
					var options = grid.dataSource.getOptions();
					options.definition.schema.model.fields = campos;

					grid.dataSource.setOptions(options);
					grid.setOptions({columns: columnas});	
				}
			}else{
				errorArchivoNoExiste();
			}
		});
	};

    var validarMensajeValidacion = fileData => {
        for (var i = 0; i < fileData.length; i++) {
            if (fileData[i].MENSAJEVALIDACION !== "OK") {
                return true; 
            }
        }
        return false; 
    }

	$scope.grabar = function () {
		if(!$scope.file) {
            bcsComunes.msg("warning", "Info", $translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
        }

        if(validarMensajeValidacion($scope.file.fileData)) {
            bcsComunes.msg("error", "Info", $translate.instant("No se puede cargar el archivo."));
        } else {
            bcsComunes.ventanaData(idVentana,$scope.file);
            bcsComunes.ventanaCerrar(idVentana);
        }
	};

	$scope.cancelar = function(){
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.verFormato = function(){
		parametros.origenFormato = "CALIFICACIONES_TRIBUTARIAS";
		bcsComunes.ventanaAbrir("VerFormatoArchivo",parametros,true,true);
	};

	var errorArchivoNoExiste = function(msj){
		var mensaje = (msj) ? msj : "ARCHIVO_NO_CONFIGURADO";
		var result ={"P_OUT_ERRORESDETALLE": [{"Codigo":"ARCHIVO_NO_CONFIGURADO","Descripcion":$translate.instant(mensaje)}] };
		var promesa = bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_ERROR"),
			conflictos:util.parseServerResponse(result)
		},idVentana);
		bcsComunes.ventanaPromesa(promesa).then(function(){
			bcsComunes.ventanaCerrar(idVentana);	
		});
	}
	
    const obtenerNemos = (data) => {
        return new Promise( (resolve, reject) => {
            bcsServiciosNormativa.IngresarCalificacion(data)
            .$promise.then(response => {
                if(response.P_OUT_NEMOS.length > 0) {
                    nemos = response.P_OUT_NEMOS;
                    resolve(response.P_OUT_NEMOS);
                } else {
                    reject(new Error('No se pudo obtener Nemos'));
                }
            })
            .catch( error => {
                reject(new Error('Error al obtener Nemos', error));
            });
        });
    };

    const getNemos = async () => {
        try {
            const ejericioEntero = parseInt(periodoComercial, 10);
            
            if (!isNaN(ejericioEntero)) {
                let nemos = await obtenerNemos({ EJERCICIO: ejericioEntero });
            } else {
                console.error('EJERCICIO no es un número entero válido.');
                throw 'EJERCICIO no es un número entero válido.';
            }
        } catch (err) {
            console.error('Error al obtener Nemos', err);
            throw 'Error al obtener nemos:', err;
        }
    }

    getNemos();

    ValidacionBCS = function(file,config,archivo,grid) {
        file.fileData = file;
        var envio = {};
        var datosArray = [];
        var folio = {};
           for (datos of file.fileData) {
        	   folio.Folio = datos.Folio;
                    datosArray.push({
                    	Folio : folio.Folio});
                   
                }
           envio.p_Folio = datosArray;
             var r = $resource("",{} , {
					'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290599"}
				});
				r.Consultar(envio).$promise.then(function(result){
					var resultado = result.P_OUT_RESULTADOPROCESO;
					for (folios of resultado){
						for (datosGrilla of file.fileData) {
							if(folios.Folio === datosGrilla.Folio.toString() && folios.Bolsa === "NO BCS"){
								datosGrilla.MENSAJEVALIDACION = $translate.instant("UI_VALIDACION_BOLSA_XXX").replace("XXX", "Folio");
							}
						}
						
					}
					 if (!file.fileData.codigo) {

                    bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);

                    // construir columnas de la grilla
                    if (!!grid) {
                        grid.dataSource.read(); // limpiar la grilla
                        grid.dataSource.data(file.fileData, false);
                        grid.refresh();
                    }

                    // se deja nombre archivo en objeto salida
                    file.fileName = archivo.nombre;
                } else {
                    bcsComunes.msg("error", 'Error', file.fileData.mensaje);
                    bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
                }

                return file;
					
				},function(result){
					
					bcsComunes.msg("error","Error",$translate.instant("UI_VALIDACION_BOLSA"));
					 if (!file.fileData.codigo) {

                    bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);

                    // construir columnas de la grilla
                    if (!!grid) {
                        grid.dataSource.read(); // limpiar la grilla
                        grid.dataSource.data(file.fileData, false);
                        grid.refresh();
                    }

                    // se deja nombre archivo en objeto salida
                    file.fileName = archivo.nombre;
                } else {
                    bcsComunes.msg("error", 'Error', file.fileData.mensaje);
                    bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
                }           

                return file;
				});
       
    };

	$scope.calcularFactores = function () {
        if(validarMensajeValidacion($scope.file.fileData)) {
            bcsComunes.msg("error", "Info", $translate.instant("No se pueden calcular los factores"));
        } else {
			$scope.file.fileData.forEach((fila, index) => {
				let sumaMontos = 0;
				let factores = {};
				Object.keys(fila).forEach(key => {
					if (key.startsWith("FACTOR_")) {
						let numeroFactor = Number(fila[key]) || 0;
						factores[key] = numeroFactor;
			
						// Suma factores del 8 al 19 para calculo de factores
						let numKey = parseInt(key.split("_")[1]);
						if (numKey >= 8 && numKey <= 19) {
							sumaMontos += numeroFactor;
						}
					}
				});
			
				let nuevaFila = { ...fila };
			
				Object.keys(factores).forEach(key => {
					let numKey = parseInt(key.split("_")[1]);
					let valor = factores[key];
			
					if (numKey >= 8 && numKey <= 19) {
						//Formula de calculo de factores entre factor 8 al 19
						nuevaFila[key] = sumaMontos !== 0 ? Number((valor / sumaMontos)) : 0;
					} else if (numKey >= 20 && numKey <= 38) {
						//Formula de calculo de factores entre factor 20 al 38
						let divisor = sumaMontos + valor;
						nuevaFila[key] = divisor !== 0 ? Number((valor / divisor)) : 0;
					}
				});
				//Exclusiones
				nuevaFila["FACTOR_35"] = Number(fila["FACTOR_35"]) || 0;
				nuevaFila["FACTOR_36"] = Number(fila["FACTOR_36"]) || 0;
				$scope.file.fileData[index] = nuevaFila;
			});		
			//Aplica formato de 8 decimales a la grilla ya que eso es lo permitido para factores 
			var columnas = $scope.gridApiCargaArchivo.getOptions().columns;
			columnas.forEach(valor => {				
				if(valor.field.includes("FACTOR_")){
					valor.format = "{0:n8}";
				}
			});
			
			$scope.gridApiCargaArchivo.setOptions({columns: columnas});
			
			//Reconstruir columnas de la grilla
			if(!!$scope.gridApiCargaArchivo){
				$scope.gridApiCargaArchivo.dataSource.read();//limpiar la grilla
				$scope.gridApiCargaArchivo.dataSource.data($scope.file.fileData, false);
				$scope.gridApiCargaArchivo.refresh();	
			}
			
			//Al terminar de calcular factores habilita el boton grabar
            $scope.factoresCalculados = true;
        }
	};

});