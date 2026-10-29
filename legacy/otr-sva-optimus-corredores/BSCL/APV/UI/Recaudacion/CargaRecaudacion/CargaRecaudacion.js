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
angular.module('ModuloAPV').controller('CargaRecaudacion',	function($scope,bcsComunes,bcsServiciosAPV,$resource,$log,$translate){
	$scope.data = {};
	$scope.data.conHeader = false;
	$scope.erroresValidacion = 0;
	$scope.config= {};
	$scope.configArchivo = {};
	$scope.configColumnas = [];
	$scope.config.overlay = bcsComunes.overlay;
	$scope.errors = {};
	$scope.errors.erroresFatales = [];
	$scope.habilitarBoton = true;
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	$scope.idVentanaCargaArchivo = idVentana;
	var parametros = bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana) + ((parametros.titulo!=undefined)?" - "+parametros.titulo:" "));
	
	$scope.configArchivo.codigoArchivo = parametros.codigoArchivo;
	$scope.configArchivo.formatoArchivoDefecto = parametros.formatoArchivoDefecto;
	$scope.configArchivo.funcionesDependencia = parametros.funcionesDependencia;

	var cleanLastBlankArray = function(row){
		if(!!row && $scope.configArchivo.headers.length !==row.length && row[row.length-1].trim().length==0)
			row.pop(row.length-1);
	}

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
		
	var csvToArray = function (csvString, config){
		// The array we're going to build
		var csvArray   = [];
		// Break it into rows to start
		var csvRows    = csvString.split(/\n/);
		// Take off the first line to get the headers
		var csvHeaders = [];
		var errors = {};
    	//LOGICA PREVIRED
		var textoFinal = "";
		var textoPrincipio = "";
		var arrayFila = [];
		var arrayFinal = [];
		var cont =1;
		for (var i=1; i < csvRows.length; ++i){			
			if (csvRows[i].substring(0, 1) === "1"){
				textoPrincipio+=(csvRows[i].substring(0,97));
				
			}			
			if(csvRows[i].substring(0, 1) === "2" )
			{
				
				textoPrincipio+=(csvRows[i].substring(0,289));
				
			}			
			
			if (csvRows[i].substring(0, 1) === "3"){
				arrayFila.push((csvRows[i].substring(0,156)));
				
			}			
			if (csvRows[i].substring(0, 1) === "4"){
				textoFinal+=rtrim(csvRows[i].substring(0,50));
				for(j=0;j< arrayFila.length; ++j){
					
					arrayFinal.push(textoPrincipio+arrayFila[j]+textoFinal);
				}
				arrayFila = [];
				textoFinal = "";
				textoPrincipio = "";
			
			}
		
		}
      //	csvRows = arrayFinal;
      	//FIN LOGICA		
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
				//data["NroRegistro"] = cont;
				//cont=cont+1;
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

						posicionInicial = posicionFinal;
					}
					
					if(largoRow!=fileRow.trim().length)
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
        serverSorting   : true, // Ordenamiento de los datos en el servidor.
    	sort : [{ field: "Posicion", dir: "ASC"}],

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
				$scope.configArchivo.SeparadorColumnas = ($scope.configColumnas[0].SeparadorColumnas.indexOf("\\")>=0) ? $scope.configColumnas[0].SeparadorColumnas : "\\"+$scope.configColumnas[0].SeparadorColumnas;
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
			$scope.file.fileData = csvToArray(archivo.contenido,$scope.configArchivo);
			console.log(archivo.contenido);
			console.log($scope.configArchivo);
			if(!$scope.file.fileData.codigo){
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
				//construir columnas de la grilla
				$scope.gridApiCargaArchivo.dataSource.data($scope.file.fileData, false);
				$scope.gridApiCargaArchivo.refresh();
				
				//se deja nombre archivo en objeto salida
				$scope.file.fileName = archivo.nombre;
			}else{
				bcsComunes.msg("error",'Error', $scope.file.fileData.mensaje);
				bcsComunes.ventanaOverlay($scope.idVentanaCargaArchivo, false);
			}
		}else{
			errorArchivoNoExiste();
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
		//construir grilla de resultados
		var filterFile = new Array();
		filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
		filterFile.push({ field: "CodigoArchivo", operator: "eq", value: $scope.configArchivo.codigoArchivo });
		
		var columnas = [];
		var campos = {};
		
		ArchivosFormatoDatasource.query({sort: {field: "Posicion", dir: "asc"},filter:filterFile}).then(function() {
			$scope.configColumnas = ArchivosFormatoDatasource.view();
			
			if($scope.configColumnas.length > 0){
				if($scope.configColumnas[0].TieneCabecera==="S"){
					$scope.data.conHeader = true;
				}
				$.each($scope.configColumnas, function(key, value){
					//eliminar caracteres peligrosos en la cabecera del archivo
					value.CodigoCampo = value.CodigoCampo.replaceAll(" ", "");
					value.CodigoCampo = value.CodigoCampo.replaceAll("#", "");
					//construir columnas
					//columnas.push({field: value.CodigoCampo, title:$translate.instant(value.CodigoCampo), width:(value.CodigoCampo.length)+"em", format: getColumnFormat(value), hidden:false});
					campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
				});
				campos["Fecha"] = {type:"string"};
				columnas.push({field:"Fecha", title:"Fecha", width:"90px" });
				campos["TipoDeposito"] = {type:"string"};
				columnas.push({field:"TipoDeposito", title:"Tipo Deposito", width:"90px" });
				campos["FolioCartola"] = {type:"number"};
				columnas.push({field:"FolioCartola", title:"Folio Cartola", width:"50px" });
				campos["FolioLineaCartola"] = {type:"number"};
				columnas.push({field:"FolioLineaCartola", title:"Folio Linea Cartola", width:"200px" });
				campos["Banco"] = {type:"string"};
				columnas.push({field:"Banco", title:"Banco", width:"100px" });
				campos["Cuenta"] = {type:"string"};
				columnas.push({field:"Cuenta", title:"Cuenta", width:"100px" });
				campos["Monto"] = {type:"number"};
				columnas.push({field:"Monto", title:"Monto", width:"100px" });
				campos["PeriodoPago"] = {type:"string"};
				columnas.push({field:"PeriodoPago", title:"Periodo Pago", width:"80px" });
				
				campos["TipoPago"] = {type:"string"};
				columnas.push({field:"TipoPago", title:"Tipo Pago", width:"70px" });
				campos["RUTEmpleador"] = {type:"string"};
				columnas.push({field:"RUTEmpleador", title:"RUT Empleador", width:"70px" });
				campos["RUTCliente"] = {type:"string"};
				columnas.push({field:"RUTCliente", title:"RUT Cliente", width:"70px" });
				campos["TipodeAfiliado"] = {type:"string"};
				columnas.push({field:"TipodeAfiliado", title:"Tipo de Afiliado", width:"40px" });
				campos["Regimen"] = {type:"string"};
				columnas.push({field:"Regimen", title:"Regimen", width:"40px" });
				
				
				
				
				campos["MENSAJEVALIDACION"] = {type:"string"};
				columnas.push({field:"MENSAJEVALIDACION", title:"Validacion", width:"200px" });
				//RazonSocial_Empleador

								
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
	};
	
	
	$scope.cerrar = function () {
		//retorna undefined si el archivo no se cargo; en caso contrario, retorna un array de objetos y nombre del archivo.
		$scope.habilitarBoton = false;
		if($scope.file){
			//parametros
		
			var p_parametros = {
					p_IdEmpresa		: $scope.SesionIdEmpresa,
					p_IdUsuario		: $scope.SesionIdUsuario,
					p_IdModulo  	: "APV",
					p_NombreCarga  	: $scope.file.fileName,
					p_Origen 		: "RECAUDA",
					p_IdEmpleado	:  $scope.$root.SesionIdEmpleado,
					p_Archivo		: [{
						IdEmpresa 			: 	$scope.SesionIdEmpresa,
						IdCargaMasiva 		: 	0,
						NombreCargaMasiva	:	$scope.file.fileName,
						CantidadRegistros	:	$scope.file.fileData.length,
						IdUsuario			: 	$scope.SesionIdUsuario,
						IdTipoCarga			:	2,
						Descripcion			:	"Carga Recaudacion",
						FechaCarga			:	util.dateInicioDia($scope.SesionFechaSistema),
						IdEstado			:	"VIG",
					}],
					p_Parametros			:	$scope.file.fileData,
				};

			bcsComunes.ventanaOverlay($scope.ventanaId,true);

			bcsServiciosAPV.CargaMasivaRecaudacion(p_parametros).$promise.then(function(respuesta){
		   	bcsComunes.setConflictos(util.parseServerResponse(respuesta));
	
		   		for(var key in respuesta.P_OUT_MENSAJES){
		   			if(respuesta.P_OUT_MENSAJES[key].codigo ===  "ERROR")
		   				bcsComunes.msg("warning","warning", respuesta.P_OUT_MENSAJES[key].descripcion);
		   			else
		   				bcsComunes.msg("success","success", respuesta.P_OUT_MENSAJES[key].descripcion);
	
					bcsComunes.ventanaOverlay(idVentana,false);
					$scope.habilitarBoton = true;
				}			     	
	
				if(respuesta.P_OUT_MENSAJES.length > 0){
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
					respuesta.P_OUT_RESULTADO = mensajeCarga;
					bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADOS', campoSecuencia : "IdInterno" ,nombreSecuencia : 'UI_SECUENCIA',datos : respuesta.P_OUT_RESULTADO } ,false,false,$scope.winID);							
	
					bcsComunes.ventanaOverlay(idVentana,false);
					$scope.habilitarBoton = true;
				}
		   	/*
			   	if (respuesta.P_OUT_MENSAJES[0].codigo !== "ERROR"){
			   		//bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);
					bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_PROCESO_COMPLETADO")},idVentana);

					//bcsComunes.msg("success","Info",respuesta.P_OUT_MENSAJES[0].descripcion);
					bcsComunes.ventanaOverlay(idVentana,false);
					//depm.fnLimpiar();
				}
				else{
					//bcsComunes.ventanaMsg({mensaje: respuesta.P_OUT_MENSAJES[0].descripcion},idVentana);
					//bcsComunes.ventanaMsgErr($translate.instant("Cargue banco primero"),idVentana);
					bcsComunes.msg("warning","Info",respuesta.P_OUT_MENSAJES[0].descripcion);
					bcsComunes.ventanaOverlay(idVentana,false);
			    }		   	*/
			},function(respuesta){
				var ErrorDetalle = respuesta.P_OUT_ERRORESDETALLE || [];
	   			$.each(ErrorDetalle,function(key,value){	
					bcsComunes.msg("error","Error","Existen problemas:" + value );
				});
	   			bcsComunes.ventanaMsg({mensaje:$translate.instant("Existen problemas:" ),conflictos:util.parseServerResponse(respuesta)},idVentana);
	   		});		
			 
			//CargaMasivaPrevired();
			
			bcsComunes.ventanaData(idVentana,$scope.file);
		}else{
			bcsComunes.msg("warning", "Info", $translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
		}
		$scope.habilitarBoton = true;
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