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
				else if(scope.idVentana) bcsComunes.ventanaOverlay(scope.idVentana,false);
			});
			
			if(scope.ngAccept) $(element).attr('accept',scope.ngAccept);
			
		}
	};
});

	
angular.module('ModuloAPV').controller('CargaSAAPVPAC',	function($scope,bcsComunes,bcsServiciosAPV,$resource,$log,$translate){
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	
	var parametros = bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana));
	
	$scope.config= {};
	$scope.config.overlay = bcsComunes.overlay;
	
	$scope.data = {};
	$scope.data.conHeader = false;
	
	$scope.configArchivoSAAPV = {};
	$scope.configArchivoPAC = {};
	$scope.configArchivoContratosPAC = {};
	$scope.configColumnasSAAPV = [];
	$scope.configColumnasPAC = [];
	$scope.configColumnasContratosPAC = [];
	
	$scope.erroresValidacion = 0;
	var totalArchivoArray = [];
	
	$scope.configArchivoSAAPV.codigoArchivo = 'CARGA_CONTRATOS_SAAPV';
	$scope.configArchivoSAAPV.formatoArchivoDefecto = parametros.formatoArchivoDefecto;
	$scope.configArchivoSAAPV.funcionesDependencia = parametros.funcionesDependencia;
	
	$scope.configArchivoPAC.codigoArchivo = 'CARGA_PAC';
	$scope.configArchivoPAC.formatoArchivoDefecto = parametros.formatoArchivoDefecto;
	$scope.configArchivoPAC.funcionesDependencia = parametros.funcionesDependencia;
	
	$scope.configArchivoContratosPAC.codigoArchivo = 'CARGA_CONTRATOS_PAC';
	$scope.configArchivoContratosPAC.formatoArchivoDefecto = parametros.formatoArchivoDefecto;
	$scope.configArchivoContratosPAC.funcionesDependencia = parametros.funcionesDependencia;
	
	
	/*********************************************
	 * Funciones para parsear archivo
	 ********************************************/
	//Funcion Trim
	function ltrim(s) {
	   return s.replace(/^\s+/, "");
	};

	function rtrim(s) {
	   return s.replace(/\s+$/, "");
	};

	function trim(s) {
	   return rtrim(ltrim(s));
	};
	
	var cleanLastBlankArray = function(row){
		if(!!row && $scope.configArchivoPAC.headers.length !==row.length && row[row.length-1].trim().length==0)
			row.pop(row.length-1);
	}

	var parseField = function(fieldConfig, value){
		if(fieldConfig){
			if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="STRING"){
				value = ((fieldConfig.ForzarMayuscula && fieldConfig.ForzarMayuscula=='S')? String(value).toUpperCase() : String(value)).replace("\r","");
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="NUMBER"){
				try{
					if(fieldConfig.IncluyeDecimales==="S"){
						value = util.incluirDecimales(value.replace(/,|\.|%/g,''), fieldConfig.CantidadDecimales);
					}else{
						value = (value && value!="")?parseInt(value) : 0;
					}
					if (isNaN(value)) {
						value = 0;
					}
				}catch(e){
					$log.debug(e);
				}
			}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="DOUBLE"){
				try{
					value = parseFloat(value);
					if (isNaN(value)) {
						value = 0;
					}
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
			return "{0:n3}";
		}else{
			return "{0:s}";
		}
	}

	var validateDependencies = function(linea){
		for(var i=0; i < $scope.configArchivoPAC.funcionesDependencia.length;i++){
			var resultado = $scope.configArchivoPAC.funcionesDependencia[i](linea);
			if(resultado!=="OK"){
				return resultado;
			}else if(i==$scope.configArchivoPAC.funcionesDependencia.length-1){
				return "OK";	
			}
		}
	}
		
	var csvToArray = function (csvString, config, total){
		// The array we're going to build
		var csvArray   = [];
		// Break it into rows to start
		var csvRows    = csvString.split(/\n/);
		// Take off the first line to get the headers
		var csvHeaders = [];
		var errors = {};
		
    	//LOGICA PAC
		var cont =1;
		
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
				else{
					//si es la primera fila, y es sin separador, validar el largo de la columna en la tabla de configuracion.
					var largoRow = total == "SI" ? 53 : 1;
					for(var key in config.headers){
						largoRow += parseInt(config.campos[config.headers[key]].LargoCampo);
					}
					
					if (config.tieneCabecera==='S'){  //Cuando tenga encabezado no debe procesar la primera fila
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
						
						if((fieldConf.TipoDato.toUpperCase()==="DATE") && (mensaje!="OK")){
							validacion=$translate.instant("UI_CAMPO_DATE_XXX_ERROR_FORMATO").replace("XXX", mensaje);
						}else if(mensaje!="OK")validacion=$translate.instant( ((fieldConf && fieldConf.TipoDato.toUpperCase()==="STRING")?"UI_FALTA_CAMPO_XXX_ARCHIVO": "UI_CAMPO_XXX_ERROR_FORMATO") ).replace("XXX", mensaje);
					}
				}
				
				if(rowArray.length < cantidadCamposObligatorios){
					validacion = $translate.instant("UI_FALTA_CAMPO");
				}
				
				if($scope.configArchivoPAC.funcionesDependencia && $scope.configArchivoPAC.funcionesDependencia.length>0){
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

						data[config.headers[key]] = parseField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );

						var mensaje = validateField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );
						if(mensaje!="OK")
							validacion=mensaje;

						if(total == "SI"){
							posicionInicial = posicionFinal == 10 ? 43 : posicionFinal;
						}else{
							posicionInicial = posicionFinal == 32 ? 33 : posicionFinal;
						}
					}
					
					var largo = total == "SI" ? (fileRow.substring(0,60).length + 20) : fileRow.trim().length;
					
					if(largoRow != largo){
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
	
	
	/*********************************************
	 * DATASOURCES
	 ********************************************/
	var saapvArchivosFormatoDatasource = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		serverSorting   : true, // Ordenamiento de los datos en el servidor.
	    sort : [{ field: "IdFormato", dir: "asc"}],
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
				}
    		}
    	},
	});
	
	var pacArchivosFormatoDatasource = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		serverSorting   : true, // Ordenamiento de los datos en el servidor.
	    sort : [{ field: "IdFormato", dir: "asc"}],
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
				}
    		}
    	},
	});
	
	
	/*********************************************
	 * GRILLAS
	 ********************************************/
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
		height: 400,
		selectable : "row",
		navigatable	: true,
	};
	
	
	/*********************************************
	 * FUNCIONES
	 ********************************************/
	$scope.buildGrid = function(){
		//construir grilla de resultados
		
		//Registros SAAPV
		var filterFile = new Array();
		filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
		filterFile.push({ field: "CodigoArchivo", operator: "eq", value: $scope.configArchivoSAAPV.codigoArchivo });
		
		var columnas = [];
		var campos = {};
		
		saapvArchivosFormatoDatasource.query({sort: {field: "IdFormato", dir: "asc"}, filter:filterFile}).then(function() {
			$scope.configColumnasSAAPV = saapvArchivosFormatoDatasource.view();
			
			if($scope.configColumnasSAAPV.length > 0){
				$scope.data.conHeader = $scope.configColumnasSAAPV[0].TieneCabecera === "S";
				
				$.each($scope.configColumnasSAAPV, function(key, value){
					//eliminar caracteres peligrosos en la cabecera del archivo
					value.CodigoCampo = value.CodigoCampo.replaceAll(" ", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("#", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("\\(%\\)", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("\\.", "");
					
					//construir columnas
					columnas.push({field: value.CodigoCampo, title:$translate.instant(value.CodigoCampo), width:(value.CodigoCampo.length)+"em", format: getColumnFormat(value), hidden:false});
					campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
				});
				//campos["MENSAJEVALIDACION"] = {type:"string"};
				//columnas.push({field:"MENSAJEVALIDACION", title:"Validacion", width:"200px" });
				
			}else{
				errorArchivoNoExiste();
			}
			
			//Registros PAC
			filterFile = new Array();
			filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
			filterFile.push({ field: "CodigoArchivo", operator: "eq", value: $scope.configArchivoContratosPAC.codigoArchivo });
			
			pacArchivosFormatoDatasource.query({sort: {field: "IdFormato", dir: "asc"}, filter:filterFile}).then(function() {
				$scope.configColumnasContratosPAC = pacArchivosFormatoDatasource.view();
				
				if($scope.configColumnasContratosPAC.length > 0){
					if($scope.configColumnasContratosPAC[0].TieneCabecera==="S"){
						$scope.data.conHeader = true;
					}
					$.each($scope.configColumnasContratosPAC, function(key, value){
						//eliminar caracteres peligrosos en la cabecera del archivo
						value.CodigoCampo = value.CodigoCampo.replaceAll(" ", "");
						value.CodigoCampo = value.CodigoCampo.replaceAll("#", "");
						value.CodigoCampo = value.CodigoCampo.replaceAll("\\(%\\)", "");
						value.CodigoCampo = value.CodigoCampo.replaceAll("\\.", "");
						
						//construir columnas
						//columnas.push({field: value.CodigoCampo, title:$translate.instant(value.CodigoCampo), width:(value.CodigoCampo.length)+"em", format: getColumnFormat(value), hidden:false});
						campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
					});
					
					campos["NumeroCuentaBancaria"] = {type:"string"};
					columnas.push({field:"NumeroCuentaBancaria", title:"UI_NUMERO_CUENTA_BANCARIA", width:"100px" });
					campos["CodigoBanco"] = {type:"string"};
					columnas.push({field:"CodigoBanco", title:"UI_CODIGO_BANCO", width:"90px" });
					campos["FolioPAC"] = {type:"string"};
					columnas.push({field:"FolioPAC", title:"UI_FOLIO_PAC", width:"90px" });
					campos["Receptor"] = {type:"string"};
					columnas.push({field:"Receptor", title:"UI_RECEPTOR", width:"90px" });
					campos["NumeroMandato"] = {type:"string"};
					columnas.push({field:"NumeroMandato", title:"UI_NUMERO_MANDATO", width:"90px" });
					campos["Firma"] = {type:"string"};
					columnas.push({field:"Firma", title:"UI_FIRMA", width:"50px" });
					campos["IdentServicio"] = {type:"string"};
					columnas.push({field:"IdentServicio", title:"UI_ID_SERVICIO", width:"200px" });
									
					campos["MENSAJEVALIDACION"] = {type:"string"};
					columnas.push({field:"MENSAJEVALIDACION", title:"Validacion", width:"200px" });
					
					//merge arreglos
					Array.prototype.push.apply($scope.configColumnasSAAPV, $scope.configColumnasContratosPAC);
					
					//setea grilla
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

				}else{
					errorArchivoNoExiste();
				}
			});
		});
	};
	
	
	$scope.guardarArchivo = function(archivo) {
		$scope.erroresValidacion = 0;
		$scope.archivo = archivo;
		
		if($scope.configColumnasSAAPV.length > 0){
			bcsComunes.ventanaOverlay(idVentana, true);
			$scope.gridApiCargaArchivo.dataSource.read();//limpiar la grilla

			$scope.configArchivoPAC.tieneSeperador = $scope.configColumnasSAAPV[0].TieneSeparador;	
			$scope.configArchivoPAC.tieneCabecera = $scope.configColumnasSAAPV[0].TieneCabecera;
			
			if($scope.configArchivoPAC.tieneSeperador==='S'){
				//archivo con separador de columnas	
				$scope.configArchivoPAC.SeparadorColumnas = ($scope.configColumnasSAAPV[0].SeparadorColumnas.indexOf("\\")>=0) ? $scope.configColumnasSAAPV[0].SeparadorColumnas : "\\"+$scope.configColumnasSAAPV[0].SeparadorColumnas;
			}
			$scope.configArchivoPAC.headers = new Array();
			$scope.configArchivoPAC.campos = {};
			$.each($scope.configColumnasSAAPV, function(key, value){
				value.Existe = false;
				$scope.configArchivoPAC.headers.push(value.CodigoCampo);
				$scope.configArchivoPAC.campos[value.CodigoCampo] = value;
				$scope.configArchivoPAC.rowSize =+ value.LargoCampo;

			});
			$scope.file = {};
			$scope.file.fileData = csvToArray(archivo.contenido,$scope.configArchivoPAC, "NO");
			
			if(!$scope.file.fileData.codigo){
				bcsComunes.ventanaOverlay(idVentana, false);
				
				//construir columnas de la grilla
				$scope.gridApiCargaArchivo.dataSource.read();
				$scope.gridApiCargaArchivo.dataSource.data($scope.file.fileData, false);
				$scope.gridApiCargaArchivo.refresh();
				
				//se deja nombre archivo en objeto salida
				$scope.file.fileName = archivo.nombre;
				
			}else{
				bcsComunes.msg("error",'Error', $scope.file.fileData.mensaje);
				bcsComunes.ventanaOverlay(idVentana, false);
			}
		}else{
			errorArchivoNoExiste();
		}
	};
	
	
	var obtieneDigitoVerificador = function dv(T) {
		var M=0,S=1;for(;T;T=Math.floor(T/10))
			S=(S+T%10*(9-M++%6))%11;return S?S-1:'K';
	};
	
	function generadorMandatoAgrupador(numeroCuentaBancaria, codigoBanco){
		numeroCuentaBancaria = numeroCuentaBancaria + (numeroCuentaBancaria.length < 13 ? codigoBanco.replace(/\D/g, "0") : '');
		numeroCuentaBancaria = numeroCuentaBancaria + (numeroCuentaBancaria.length < 17 ? obtieneDigitoVerificador(numeroCuentaBancaria) : '');
		
		var diferencia = 22 - numeroCuentaBancaria.length;
		var acumulador= '';
		
		for (var x = 0; x < diferencia; x++){
			var acumulador = acumulador +'0';
		}
		return acumulador + numeroCuentaBancaria;
	}
	
	
	$scope.cargarArchivoSAAPVPAC = function () {
		if($scope.file && $scope.file.fileData){
			bcsComunes.ventanaOverlay(idVentana,true);
			
			var i = 0;
			var contratosSAAPV = [];
			var distribucion = [];
			var regimenes = [];
			var detalleOrigenTraspasos = [];
			var contratosPAC = [];
			
			$.each($scope.file.fileData,function(key,value){
				//ParametrosContratoSAAPV
				var contenidoContrato = {
					IdContratoSAAPV				: 0,
					IdEmpresa					: $scope.SesionIdEmpresa,
					FolioContratoSAAPV 			: value.FolioContratoSAAPV,
					IdPlan						: value.NombrePlan.toUpperCase() === "A. ABIERTO" ? 1 : 2,
					NombrePlan					: value.NombrePlan.toUpperCase(),
					FechaSuscripcion			: value.FechaSuscripcion.substring(6)+'-'+value.FechaSuscripcion.substring(3,5)+'-'+value.FechaSuscripcion.substring(0,2),
					FechaCreacion				: value.FechaCreacion.substring(6)+'-'+value.FechaCreacion.substring(3,5)+'-'+value.FechaCreacion.substring(0,2),
					FechaAprobacion				: value.FechaAprobacion.substring(6)+'-'+value.FechaAprobacion.substring(3,5)+'-'+value.FechaAprobacion.substring(0,2),
					FechaNotificacion			: value.FechaNotificacion.substring(6)+'-'+value.FechaNotificacion.substring(3,5)+'-'+value.FechaNotificacion.substring(0,2),
					FechaRecepcion				: value.FechaRecepcion.substring(6)+'-'+value.FechaRecepcion.substring(3,5)+'-'+value.FechaRecepcion.substring(0,2),						
					Folio						: value.Folio,
					IdPersona					: null,
					IdEmpleador					: null,
					IdCliente					: null,
					IdCategoriaInversionista	: value.IdCategoriaInversionista.toUpperCase(),
					IdPerfilComercial			: value.IdPerfilComercial.toUpperCase(),
					IdEnrolamiento				: 0,
					IdPortafolio				: 0,
					NombreCliente				: value.NombreCliente.toUpperCase(),
					ApellidoPaternoCliente		: value.ApellidoPaternoCliente.toUpperCase(),
					ApellidoMaternoCliente		: value.ApellidoMaternoCliente.toUpperCase(), 
					DocumentoIdentidadCliente	: value.DocumentoIdentidadCliente,
					FechaNacimientoCliente		: value.FechaNacimientoCliente.substring(6)+'-'+value.FechaNacimientoCliente.substring(3,5)+'-'+value.FechaNacimientoCliente.substring(0,2),
					EstadoCivil					: value.EstadoCivil.toUpperCase(),
					IdTipoGenero				: value.IdTipoGenero.toUpperCase(),
					IdPaisResidenciaCliente		: value.IdPaisResidenciaCliente,
					IdCiudadCliente				: value.IdCiudadCliente,
					IdComunaCliente				: value.IdCiudadCliente,
					DireccionCliente			: value.DireccionCliente.toUpperCase(),
					NumeroDireccionCliente		: value.NumeroDireccionCliente,
					IdTipoCargoPersona			: value.IdTipoCargoPersona === undefined ? 1 :value.IdTipoCargoPersona,
					TelefonoFijo				: value.TelefonoFijo,
					TelefonoMovil				: value.TelefonoMovil,
					TelefonoLaboral				: value.TelefonoLaboral,
					EmailCliente				: value.EmailCliente.toUpperCase(),
					RazonSocialEmpleador		: value.RazonSocialEmpleador.toUpperCase(),	    
					DocumentoIdentidadEmpleador	: value.DocumentoIdentidadEmpleador,
					IdPaisEmpleador				: 44,
					DireccionComercialEmpleador	: value.DireccionComercialEmpleador.toUpperCase(),
					NumeroDireccionEmpleador	: value.NumeroDireccionEmpleador,
					IdCiudadEmpleador			: value.IdCiudadEmpleador, 
					IdComunaEmpleador			: value.IdComunaEmpleador, 
					TelefonoEmpleador			: value.TelefonoEmpleador, 
					NombreEncargado				: value.NombreEncargado.toUpperCase(),
					EmailEncargado				: value.EmailEncargado.toUpperCase(),
					TelefonoEncargado			: value.TelefonoEncargado,
					IdMedioRecaudacionTrabajador: value.IdMedioRecaudacionTrabajador,
					IdEntidadRecaudadora		: value.IdEntidadRecaudadora,
					CodigoPrevision				: value.CodigoPrevision,
					Pensionado					: value.Pensionado.toUpperCase(),
					Trabajador					: value.Trabajador.toUpperCase(),
					OrigenAhorro				: "", 
					MontoPesos 				 	: 0,
					MontoUF  					: 0,
					PorcentajeRegimenImponible  : 0,
					Pago						: "",
					PeriodoInicio				: "",
					PeriodoTermino				: "",
					RegimenTributario			: "",
					FormaDePago					: value.FormaDePago.toUpperCase(),
					IdMedioRecaudacionFormaPago	: value.IdMedioRecaudacionFormaPago,
					DiaDescuento				: value.DiaDescuento,
					Institucion					: value.Institucion === '' || value.Institucion === undefined ? '0' : value.Institucion,		
					CodigoTipoSolicitud			: value.CodigoTipoSolicitud,
					TipoMovimiento				: value.TipoMovimiento,
					RazonRetiro					: value.RazonRetiro,						
					ComisionTransferencia		: Number(value.ComisionTransferencia),
					NombreEntidadOrigen			: value.NombreEntidadOrigen.toUpperCase(),
					RutEntidadOrigen			: value.RutEntidadOrigen,						
					IdPersonaOrigen				: value.IdPersonaOrigen,							
					TipoDeAhorro				: value.TipoDeAhorro.toUpperCase(),
					FondoDeOrigen				: value.FondoDeOrigen.toUpperCase(),							
					TipoTraspaso				: value.TipoTraspaso,
					MontoTraspaso				: value.MontoTraspaso,
					UFTraspaso					: value.UFTraspaso,
					PorcentajeTraspaso			: value.PorcentajeTraspaso,					
					Descripcion					: value.Descripcion,							
					IdEstadoEnvioNotificacion	: "NEV",
					IdEstadoSolicitud			: "",
					IdEstado					: "PDI",
					IdCargaMasiva				: null,
					/*Usuario*/
					IdEmpleado					: value.IdEmpleado === undefined ? 0 : value.IdEmpleado,
					IdPropositoPortafolio       : "",
					IdMatrizAgrupadorAPV        : 0,
					CodigoAgrupador             : "",
					IdInterno					: i
				};
				contratosSAAPV.push(contenidoContrato);
				
				//DistribucionInversion
				if(value.D1_Instrumento && value.D1_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D1_Instrumento,
						RazonSocial  				: value.D1_Serie,
						Nemotecnico 				: value.D1_Serie,
						MontoPesos 					: value.D1_MontoPesos === undefined ? 0 : Number(value.D1_MontoPesos),
						Porcentaje 					: value.D1_Porcentaje === undefined ? 0 : Number(value.D1_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D2_Instrumento && value.D2_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D2_Instrumento,
						RazonSocial  				: value.D2_Serie,
						Nemotecnico 				: value.D2_Serie,
						MontoPesos 					: value.D2_MontoPesos === undefined ? 0 : Number(value.D2_MontoPesos),
						Porcentaje 					: value.D2_Porcentaje === undefined ? 0 : Number(value.D2_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D3_Instrumento && value.D3_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D3_Instrumento,
						RazonSocial  				: value.D3_Serie,
						Nemotecnico 				: value.D3_Serie,
						MontoPesos 					: value.D3_MontoPesos === undefined ? 0 : Number(value.D3_MontoPesos),
						Porcentaje 					: value.D3_Porcentaje === undefined ? 0 : Number(value.D3_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D4_Instrumento !== undefined && value.D4_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D4_Instrumento,
						RazonSocial  				: value.D4_Serie,
						Nemotecnico 				: value.D4_Serie,
						MontoPesos 					: value.D4_MontoPesos === undefined ? 0 : Number(value.D4_MontoPesos),
						Porcentaje 					: value.D4_Porcentaje === undefined ? 0 : Number(value.D4_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D5_Instrumento !== undefined && value.D5_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D5_Instrumento,
						RazonSocial  				: value.D5_Serie,
						Nemotecnico 				: value.D5_Serie,
						MontoPesos 					: value.D5_MontoPesos === undefined ? 0 : Number(value.D5_MontoPesos),
						Porcentaje 					: value.D5_Porcentaje === undefined ? 0 : Number(value.D5_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D6_Instrumento !== undefined && value.D6_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D6_Instrumento,
						RazonSocial  				: value.D6_Serie,
						Nemotecnico 				: value.D6_Serie,
						MontoPesos 					: value.D6_MontoPesos === undefined ? 0 : Number(value.D6_MontoPesos),
						Porcentaje 					: value.D6_Porcentaje === undefined ? 0 : Number(value.D6_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D7_Instrumento !== undefined && value.D7_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D7_Instrumento,
						RazonSocial  				: value.D7_Serie,
						Nemotecnico 				: value.D7_Serie,
						MontoPesos 					: value.D7_MontoPesos === undefined ? 0 : Number(value.D7_MontoPesos),
						Porcentaje 					: value.D7_Porcentaje === undefined ? 0 : Number(value.D7_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D8_Instrumento !== undefined && value.D8_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D8_Instrumento,
						RazonSocial  				: value.D8_Serie,
						Nemotecnico 				: value.D8_Serie,
						MontoPesos 					: value.D8_MontoPesos === undefined ? 0 : Number(value.D8_MontoPesos),
						Porcentaje 					: value.D8_Porcentaje === undefined ? 0 : Number(value.D8_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D9_Instrumento !== undefined && value.D9_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D9_Instrumento,
						RazonSocial  				: value.D9_Serie,
						Nemotecnico 				: value.D9_Serie,
						MontoPesos 					: value.D9_MontoPesos === undefined ? 0 : Number(value.D9_MontoPesos),
						Porcentaje 					: value.D9_Porcentaje === undefined ? 0 : Number(value.D9_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};	
					distribucion.push(tablaDistribucion);
				}
				if(value.D10_Instrumento !== undefined && value.D10_Instrumento !== ''){
					var tablaDistribucion = {
						FolioContratoSAAPV 			: value.FolioContratoSAAPV,
						IdInstrumento 				: value.D10_Instrumento,
						RazonSocial  				: value.D10_Serie,
						Nemotecnico 				: value.D10_Serie,
						MontoPesos 					: value.D10_MontoPesos === undefined ? 0 : Number(value.D10_MontoPesos),
						Porcentaje 					: value.D10_Porcentaje === undefined ? 0 : Number(value.D10_Porcentaje),
						IdEstado 					: 'VIG',
						IdInterno					: i
					};
					distribucion.push(tablaDistribucion);
				}
				
				//PortafoliosModificados
				var tablaRegimenes = {
					CodigoAgrupador             : value.CodigoAgrupador === undefined ? "" : value.CodigoAgrupador.toUpperCase(),
					IdMatrizAgrupadorAPV        : value.IdMatrizAgrupador === undefined ? 0 : value.IdMatrizAgrupador,
					OrigenAhorro 				: value.OrigenAhorro.toUpperCase(),
				    IdPortafolio				: value.IdPortafolio,
				    IdPropositoPortafolio       : value.IdPropositoPortafolio.toUpperCase(),
				    MesInicio					: value.MesInicio,
				    MesTermino					: value.MesTermino,
				    MontoPesos					: value.MontoPesos,
					MontoUF 					: value.MontoUF,
					Pago						: value.Pago.toUpperCase(),
					PagoNombre					: value.Pago.toUpperCase() === 'UNI' ? 'UNICO' : (value.Pago.toUpperCase() === 'IND' ? 'INDEFINIDO' : 'TEMPORAL'),
					PorcentajeRegimenImponible	: value.PorcentajeRegimenImponible,
					Producto					: value.Producto === undefined ? "" : value.Producto,
				    Regimen						: value.Regimen.toUpperCase(),
				    IdInterno					: i
				};
				regimenes.push(tablaRegimenes);
				
				//DetalleOrigenTraspaso
				if(value.D1_NombreEntidadTraspaso !== undefined && value.D1_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D1_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D1_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D1_NombreEntidadTraspaso,
						DocumentoEntidad : value.D1_DocumentoEntidadTraspaso,
						Fondo : value.D1_Fondo,
						MontoTraspaso : value.D1_MontoPesosTraspaso,
						UFTraspaso : value.D1_MontoUFtraspaso,
						PorcentajeTraspaso : value.D1_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				if(value.D2_NombreEntidadTraspaso !== undefined && value.D2_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D2_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D2_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D2_NombreEntidadTraspaso,
						DocumentoEntidad : value.D2_DocumentoEntidadTraspaso,
						Fondo : value.D2_Fondo,
						MontoTraspaso : value.D2_MontoPesosTraspaso,
						UFTraspaso : value.D2_MontoUFtraspaso,
						PorcentajeTraspaso : value.D2_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				if(value.D3_NombreEntidadTraspaso !== undefined && value.D3_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D3_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D3_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D3_NombreEntidadTraspaso,
						DocumentoEntidad : value.D3_DocumentoEntidadTraspaso,
						Fondo : value.D3_Fondo,
						MontoTraspaso : value.D3_MontoPesosTraspaso,
						UFTraspaso : value.D3_MontoUFtraspaso,
						PorcentajeTraspaso : value.D3_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				if(value.D4_NombreEntidadTraspaso !== undefined && value.D4_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D4_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D4_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D4_NombreEntidadTraspaso,
						DocumentoEntidad : value.D4_DocumentoEntidadTraspaso,
						Fondo : value.D4_Fondo,
						MontoTraspaso : value.D4_MontoPesosTraspaso,
						UFTraspaso : value.D4_MontoUFtraspaso,
						PorcentajeTraspaso : value.D4_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				if(value.D5_NombreEntidadTraspaso !== undefined && value.D5_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D5_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D5_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D5_NombreEntidadTraspaso,
						DocumentoEntidad : value.D5_DocumentoEntidadTraspaso,
						Fondo : value.D5_Fondo,
						MontoTraspaso : value.D5_MontoPesosTraspaso,
						UFTraspaso : value.D5_MontoUFtraspaso,
						PorcentajeTraspaso : value.D5_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				if(value.D6_NombreEntidadTraspaso !== undefined && value.D6_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D6_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D6_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D6_NombreEntidadTraspaso,
						DocumentoEntidad : value.D6_DocumentoEntidadTraspaso,
						Fondo : value.D6_Fondo,
						MontoTraspaso : value.D6_MontoPesosTraspaso,
						UFTraspaso : value.D6_MontoUFtraspaso,
						PorcentajeTraspaso : value.D6_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				if(value.D7_NombreEntidadTraspaso !== undefined && value.D7_NombreEntidadTraspaso !== ''){
					var tablaDetalleOrigenTraspaso = {
						IdTraspaso : 0,
						IdDetalleTraspaso : 0,
						CodigoTipoAhorro : value.D7_CodigoTipoAhorro,
						IdEntidadRecaudadora : value.D7_IdEntidadRecaudadoraTraspaso,
						NombreEntidad : value.D7_NombreEntidadTraspaso,
						DocumentoEntidad : value.D7_DocumentoEntidadTraspaso,
						Fondo : value.D7_Fondo,
						MontoTraspaso : value.D7_MontoPesosTraspaso,
						UFTraspaso : value.D7_MontoUFtraspaso,
						PorcentajeTraspaso : value.D7_PorcentajeTraspaso,
						IdEstado : "VIG",
						IdInterno : i
					};
					detalleOrigenTraspasos.push(tablaDetalleOrigenTraspaso);
				}
				
				//InContratoPAC
				var identServicioGenerado = generadorMandatoAgrupador(value.NumeroCuentaBancaria, value.CodigoBanco);
				var tablaContratosPAC = {
					NumeroCuentaBancaria: value.NumeroCuentaBancaria,
					CodigoBanco		: value.CodigoBanco,
					FolioSAAPV		: value.FolioContratoSAAPV,
					FolioPAC		: value.FolioPAC,
					Receptor		: value.Receptor,
					NumeroMandato	: value.NumeroMandato,
					Firma			: value.Firma,
					IdentServicio	: value.IdentServicio === identServicioGenerado ? value.IdentServicio : identServicioGenerado,
				    IdInterno		: i
				};
				contratosPAC.push(tablaContratosPAC);
				
				i++;
			});
			
			var parametros = {				
				p_IdEmpresa		: $scope.SesionIdEmpresa,
				p_IdUsuario		: $scope.SesionIdUsuario,
				p_IdModulo  	: "APV",
				p_NombreCarga  	: $scope.file.fileName,
				p_Origen 		: "PAC",
				p_IdEmpleado	: $scope.$root.SesionIdEmpleado,
				p_Archivo		: [{
					IdEmpresa 			: 	$scope.SesionIdEmpresa,
					IdCargaMasiva 		: 	0,
					NombreCargaMasiva	:	$scope.file.fileName,
					CantidadRegistros	:	$scope.file.fileData.length,
					IdUsuario			: 	$scope.SesionIdUsuario,
					IdTipoCarga			:	1,
					Descripcion			:	"Carga SAAPV+PAC",
					FechaCarga			:	$scope.SesionFechaSistema,
					IdEstado			:	"VIG",
				}],
				p_ParametrosContratos	: contratosSAAPV,
				p_DistribucionInversion : distribucion,
				p_PortafoliosModificados: regimenes,
				p_DetalleOrigenTraspaso	: detalleOrigenTraspasos,
				p_InContratoPAC			: contratosPAC
			};
			
			bcsServiciosAPV.CargaMasivaSAAPVPAC(parametros).$promise.then(function(respuesta){
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				
				var mensajeCarga = new Array();
				$.each(respuesta.P_OUT_MENSAJES,function(key,value){
					 var objCarga = {
						IdInterno : value.id,
						tipo : value.tipo === 'OK' ? 'INFORMACION' : value.tipo,
						codigo : value.codigo,
						descripcion : value.descripcion
					 };
					 mensajeCarga.push(objCarga);
				});
				
				if(mensajeCarga.length > 0){
					bcsComunes.ventanaAbrir('VentanaResultado',{usaNuevosMensajes: true, titulo: 'UI_RESULTADOS', campoSecuencia: "IdInterno", nombreSecuencia: 'UI_SECUENCIA', datos: mensajeCarga}, false, false, idVentana);							
				}
				
	   		},function(respuesta){
	   			$.each(respuesta.P_OUT_ERRORESDETALLE,function(key,value){	
					bcsComunes.msg("error","Error","Existen problemas:" + value );
				});
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
			});
		}
		else{
			bcsComunes.msg("warning", "Info", $translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
	};
	
	
	$scope.cancelar = function(){
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	
	var errorArchivoNoExiste = function(msj){
		var mensaje = (msj) ? msj : "ARCHIVO_NO_CONFIGURADO";
		var result ={"P_OUT_ERRORESDETALLE": [{"Codigo":"ARCHIVO_NO_CONFIGURADO","Descripcion":$translate.instant(mensaje)}] };
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_ERROR"),
			conflictos:util.parseServerResponse(result)
		},idVentana);
		bcsComunes.ventanaCerrar(idVentana);
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