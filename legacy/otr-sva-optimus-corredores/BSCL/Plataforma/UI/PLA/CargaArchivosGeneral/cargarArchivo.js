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


angular.module('ModuloPlataforma').controller('CargarArchivosCtrl',function($scope,bcsComunes,bcsServicioPlataforma,$resource,$log,$translate,$rootScope){
	var folioBuscar;
	var codigoArchivo;
	var obj = {};
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.idVentanaCargaArchivo = idVentana;
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana) + ((parametros.titulo!=undefined)?" - "+parametros.titulo:" "));
	codigoArchivo=parametros.codigoArchivo;
	$scope.config = {
		conDetalle : false,
		conPie : false,
		archivo : {
			conHeader : false,
			columnas : [],
			codigoArchivo : parametros.codigoArchivo,
			formatoArchivoDefecto : parametros.formatoArchivoDefecto,
			funcionesDependencia : parametros.funcionesDependencia
		},
		detalle : {
			
		},
		pie : {
			
		}
	};
	
	$scope.errors = {
		erroresFatales : [],
		count : 0
	};
	
	var cleanLastBlankArray = function(row, config){
		if(!!row && config.headers.length !==row.length && row[row.length-1].trim().length==0){
			row.pop(row.length-1);
			return true;
		}else{
			return false;
		}
			
	}
	var parseField = function(fieldConfig, value){
		if(fieldConfig){
			if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="STRING"){
					value = ((fieldConfig.ForzarMayuscula && fieldConfig.ForzarMayuscula=='S')? String(value).toUpperCase() : String(value)).replace("\r","");
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="NUMBER"){
				try{
					if(fieldConfig.IncluyeDecimales==="S"){
						if (codigoArchivo === 'CARGA_MASIVA_GARANTIA') {
							value = value.replace(',','.');
							if (value.includes('.')) {
								let numero = value.split('.');
								value = parseFloat(numero[0] + '.' + numero[1].padEnd(fieldConfig.CantidadDecimales, '0'));
							} else {
								value = parseFloat(value);
							}
						}else if(codigoArchivo === 'ARCHIVO_OTROS_SALDOS'){
					        value = Math.round(parseFloat(parseFloat(value.replace(/,/g, '.')).toFixed(fieldConfig.CantidadDecimales + 1).slice(0, -1)) * Math.pow(10, fieldConfig.CantidadDecimales)) / Math.pow(10, fieldConfig.CantidadDecimales)
					    }else{
							value = util.incluirDecimales(value.replace(/,|\.|%/g,''), fieldConfig.CantidadDecimales);
						}
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
	
	var validateField = function(fieldConfig, value){
		if(!fieldConfig) return false;
		if(fieldConfig.EsObligatorio==="S" && value.length<=0){
			return fieldConfig.CodigoCampo;
		}
		if(fieldConfig.Validacion && fieldConfig.Validacion.trim().length>0 && value.length>0 ){
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
			return "{0:n"+fieldConfig.CantidadDecimales+"}";
		}else{
			return "{0:s}";
		}
	}

	var validateDependencies = function(linea){
		for(var i=0; i < $scope.config.archivo.funcionesDependencia.length;i++){
			var resultado = $scope.config.archivo.funcionesDependencia[i](linea);
			if(resultado!=="OK"){
				return resultado;
			}else if(i==$scope.config.archivo.funcionesDependencia.length-1){
				return "OK";	
			}
		}
	}
		
	var csvToArray = function (csvString, config){
		// The array we're going to build
		var csvArray   = [];
		// Break it into rows to start
		var csvRows    = csvString.split(/\n/);
		// Take off the first line to get the headers
		var csvHeaders = [];
		
		var errors = {};

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
						data[csvHeaders[key]]= parseField(fieldConf, value);
						if ((csvHeaders[key] == "FECHAVALORCUOTA")){
							var operaciones = ["APOFUSION","RESFUSION","INGTIT","RETTIT"];
							var esOperacion = rowArray.some(value => operaciones.includes(value));
							fieldConf.EsObligatorio = (esOperacion) && "S";
						}
						var mensaje = validateField(fieldConf, data[csvHeaders[key]]);
						
						


						
						if((fieldConf.TipoDato.toUpperCase()==="DATE") && (mensaje!="OK")){
							validacion=$translate.instant("UI_CAMPO_DATE_XXX_ERROR_FORMATO").replace("XXX", mensaje);
						}else if(mensaje!="OK"){
							
							validacion = $translate.instant((value!=""?(fieldConf.TipoDato.toUpperCase()==="STRING"?"UI_CAMPO_XXX_ERROR_FORMATO_STRING":"UI_CAMPO_XXX_ERROR_FORMATO"):"UI_FALTA_CAMPO_XXX_ARCHIVO")).replace("XXX",$translate.instant(mensaje));
							//validacion=$translate.instant( ((fieldConf && fieldConf.TipoDato.toUpperCase()==="STRING")?"UI_FALTA_CAMPO_XXX_ARCHIVO": "UI_CAMPO_XXX_ERROR_FORMATO") ).replace("XXX", mensaje);	
						}else if(value.match(/[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/)){
							if(codigoArchivo === "CARGA_LIQUIDAR_TES"){
							validacion = $translate.instant("UI_CAMPO_XXX_ERROR_FORMATO_STRING").replace("XXX",csvHeaders[key]);
							}
						}
						if(codigoArchivo === "ARCHIVO_SORTEO_LETRAS" && fieldConf.MensajeValidacion !="" && mensaje!="OK"){
							validacion = $translate.instant(fieldConf.MensajeValidacion);
						}else if (codigoArchivo === "CARGA_REFACTURACION"){
							if(csvHeaders[key]=="Iva" || csvHeaders[key]=="Comisiones" || csvHeaders[key]=="Gastos"){
								if(value.match(/[-]/)){
								validacion = $translate.instant("UI_CAMPO_XXX_ERROR_FORMATO").replace("XXX",csvHeaders[key]);
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
				if (validacion !== 'OK')
					$scope.errors.count++;
				
			}else{
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
		serverFiltering:true,
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

            console.log($scope.file);
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
            if (codigoArchivo === "CARGA_REFACTURACION") {
            	file = ValidacionBCS(file.fileData,config,archivo,grid);
            	return file;
             
		  }else{
			if(!file.fileData.codigo){
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
				
				//construir columnas de la grilla
				if(!!grid){
					grid.dataSource.read();//limpiar la grilla
					grid.dataSource.data(file.fileData, false);
					grid.refresh();	
				}
								
				//se deja nombre archivo en objeto salida
				file.fileName = archivo.nombre;
			}else{
				bcsComunes.msg("error",'Error', file.fileData.mensaje);
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
			}
			
			//Inicio GCMBISEGCU-1
			if (config.codigoArchivo==='CARGA_MASIVA_CUSTODIA_EXT')  //agregar datos del instrumento solo para Custodia Externa
			{
				 var nemos = [];
				 for (datos of file.fileData) if (!nemos.includes(datos.NEMO)) nemos.push((datos.NEMO || '').toUpperCase());
				 buscarInstrumentos(nemos);
			}

			//Fin GCMBISEGCU-1			
			
			return file;
            }
		}else{
			errorArchivoNoExiste();
			return {};
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
					columnas.push({field: value.CodigoCampo, title:$translate.instant(value.CodigoCampo), width:(value.CodigoCampo.length)+"em", format: getColumnFormat(value), hidden:false});
					campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
				});
				campos["MENSAJEVALIDACION"] = {type:"string"};
				columnas.push({field:"MENSAJEVALIDACION", title:"VALIDACION", width:"200px" });

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
	
	
	$scope.cerrar = function () {
		
		if($scope.file){
				bcsComunes.ventanaData(idVentana,$scope.file);
			}else{
				bcsComunes.msg("warning", "Info", $translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
			}
			
			bcsComunes.ventanaCerrar(idVentana);		
	};

	$scope.cancelar = function(){
		bcsComunes.ventanaCerrar(idVentana);
	};

	$scope.verFormato = function(){
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
	
	
	//Inicio GCMBISEGCU-1 
	//FUNCION QUE BUSCA LO ESCRITO
	var buscarInstrumentos = function(nemo,idInstrumento) {
		
		var r = $resource("",{} , {
			'Consultar' : { method : "POST", url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=21025", async: false,}
		});
		
		var instrumentos = [];
		
		for (n of nemo) {
			instrumentos.push({ Correlativo : 0, IdInstrumento : idInstrumento, Nemotecnico : n});
		}
			
		var datos = {
				p_IdEmpresa : $rootScope.SesionIdEmpresa,
				p_IdUsuario: $rootScope.SesionIdUsuario,
				p_IdModulo: "PLA",
				p_CreaOpcionAutomatica : "N",
				p_Instrumentos: instrumentos
				
		}

		r.Consultar(datos).$promise.then(function(result){
			
			if (result.P_OUT_INSTRUMENTOS.length === 0) {
				//scope.nemotecnico = "";
				bcsComunes.msg("error","UI_ERROR","UI_INSTRUMENTO_NO_EXISTE");
				return;
			}
			
			for(var i=0; i < $scope.file.fileData.length; i++) {
				var datosInstrumento = result.P_OUT_INSTRUMENTOS.find(item => item.Nemotecnico == $scope.file.fileData[i].NEMO);
				if (datosInstrumento) {
					$scope.file.fileData[i]["MONEDA"] = datosInstrumento.IdMoneda;
					$scope.file.fileData[i]["EMISOR"] = datosInstrumento.IdEmisor;
					$scope.file.fileData[i]["CLASIFICACIONINSTRUMENTO"] = datosInstrumento.IdClasificacionTipoInstrumento;
					$scope.file.fileData[i]["CONDICIONLIQUIDACION"] = "T0";
				}
			}
			console.log($scope.file.fileData);
			
			
		},function(result){
			console.log(result);
		});
	}

	

	
	// Transaccion
	

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
					console.log(result);
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
           
                // Inicio GCMBISEGCU-1
                if (config.codigoArchivo === 'CARGA_MASIVA_CUSTODIA_EXT') // agregar
                // datos
                // del
                // instrumento
                // solo
                // para
                // Custodia
                // Externa
                {
                    var nemos = [];
                    for (datos of file.fileData)
                        if (!nemos.includes(datos.NEMO)) nemos.push((datos.NEMO || '').toUpperCase());
                    buscarInstrumentos(nemos);
                }

                // Fin GCMBISEGCU-1

                return file;
					
				},function(result){
					console.log(result);
					
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
           
                // Inicio GCMBISEGCU-1
                if (config.codigoArchivo === 'CARGA_MASIVA_CUSTODIA_EXT') // agregar
                // datos
                // del
                // instrumento
                // solo
                // para
                // Custodia
                // Externa
                {
                    var nemos = [];
                    for (datos of file.fileData)
                        if (!nemos.includes(datos.NEMO)) nemos.push((datos.NEMO || '').toUpperCase());
                    buscarInstrumentos(nemos);
                }

                // Fin GCMBISEGCU-1

                return file;
				});
       
    };
	


});