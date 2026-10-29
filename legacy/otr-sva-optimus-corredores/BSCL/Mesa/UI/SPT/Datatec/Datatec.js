angular.module('ModuloMesa').directive('fileReaderDatatec', function() {
	return {
		//restrict: 'A',
		scope: {
			fileReaderDatatec:"=",
			fileNameDatatec:"="
		},
		link: function(scope, element) {
			$(element).on('change', function(changeEvent) {
				var files = changeEvent.target.files;
				if (files.length) {
					var r = new FileReader();
					r.onload = function(e) {
						var contents = e.target.result;
						scope.$apply(function () {
							scope.fileReaderDatatec = contents;
							scope.fileNameDatatec = files[0].name;
						});
					};
					r.readAsText(files[0],'iso-8859-1');
				}
			});
		}
	};
});


angular.module('ModuloMesa').controller('DatatecCtl',
function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa,$interval){
	
	//Obtiene datos desde la ventana principal
	var idVentana = bcsComunes.ventanaWinId($scope);
	var overlay = bcsComunes.overlay;
	var parametrosVentana = bcsComunes.ventanaParams(idVentana);
	
	var row = '';
	var ingSpot = this;
	var csvArray = [];
	var sptArray = [];
	var errArray = [];
	var puntas   = [];
	var datosTramos;
	var nombreCorredora = '';
	
	$scope.data = {};
  	$scope.data.conHeader = false;
  	$scope.config= {};
  	$scope.configArchivo = {};
  	$scope.configColumnas = [];
  	$scope.config.overlay = bcsComunes.overlay;
  	$scope.data.texto = false;
	
  	$scope.configArchivo.codigoArchivo = 'CARGA_ARCHIVO_DATATEC';
  	
  	
  	//DATASOURCES
  	var dsDefDatatec = {
		type : "array",
		definition: {
			sort 			: { field: "NumeroRegistro", dir: "asc" },
			//pageSize        : 13,
			schema: {
				model: {
					fields: {
						NumeroRegistro	: {type: "number"},
						IdEstadoRegistro: {type: "string"},
						GlosaProceso	: {type: "string"}
					}
				}
			}
		}
	};
  	
  	var archivosFormatoDatasource = new kendo.data.DataSource({
  		type: "odata",
  		serverFiltering:true,
  		transport: {
  			read: {
  				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1358",
  				dataType: "json"
  		    }
  		},
  		schema: {
          	model: {
          		fields: {
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
          			MensajeValidacion: {type:"string"},
          			IdArchivoFormato: {type:"number"}
  				}
      		}
      	},
      	sort : [{ field: "Posicion", dir: "ASC"}],
  	});
  	
	 var consultarEmpresaaDatasource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=432",
					dataType: "json"
			    }
			},
			filter: [{ field: "IdTipoProducto", operator: "eq", value: "MEME" },
					{ field: "CodigoParametro", operator: "eq", value: "RAZONSOCIALDATATEC" },
					{ field: "IdEmpresa", operator: "eq", value: 1 }],
			schema: {
	          	model: {
	          		fields: {
	          			IdTipoProducto: {type:"string"},
	          			CodigoParametro: {type:"string"},
	          			Valor: {type:"string"},
	          			Descripcion: {type:"string"},
						IdEmpresa: {type:"number"}
	  				}
	      		}
	      	},
		}
	});
	
	
	//GRILLA
	var dsLog = new bcs.DataSource(dsDefDatatec);
	$scope.confGrillaLog = {
		dataSource: dsLog,
		columns : [
		     {field: "NumeroRegistro",	title: "UI_NRO_REGISTRO",	width: "15px",	sortable : true},
		     {field: "GlosaProceso",	title: "UI_LOG",			width: "110px",	sortable : true}
		],
		sortable	: true,
		pageable	: true,
		filterable	: false,
		navigatable	: true,
		height		: 420,
		selectable	: "row",
	    autoBind	: false,
	    gridCreated	: function(grid){$scope.buildGrid();},
		rowColorBackground :
			function(row) {
				if(row.IdEstadoRegistro === 'ERR') return "red";
				if(row.IdEstadoRegistro === 'PEN') return "yellow";
				return "";
			}
   	};
	
	
	/*$scope.confBasicaCargaArchivo = {
		dataSource : new bcs.DataSource({
			definition : {},
			type : "array"
		}),
		columns : [],
		gridCreated: function(grid) {
			$scope.gridApiCargaArchivo = grid;
			$scope.buildGrid();
		},
		sortable: true,
		height: 200,
		selectable : "multiple row"
	};*/
	
	$scope.data.salir = function(){
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	$scope.buildGrid = function(){
		//construir grilla de resultados
		var filterFile = new Array();
		filterFile.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
		filterFile.push({ field: "CodigoArchivo", operator: "eq", value: $scope.configArchivo.codigoArchivo });
		
		var columnas = [];
		var campos = {};
		
		archivosFormatoDatasource.query({filter:filterFile}).then(function() {
			$scope.configColumnas = archivosFormatoDatasource.view();
			
			if(archivosFormatoDatasource.view().length > 0){
				if(archivosFormatoDatasource.view()[0].TieneCabecera === "S"){
					$scope.data.conHeader = true;
				}
				$.each(archivosFormatoDatasource.view(), function(key, value){
					columnas.push({field: value.CodigoCampo, title:value.CodigoCampo, width:(value.CodigoCampo.length*1.5)+"em", format: getColumnFormat(value), hidden:false});
					campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
				});
				columnas.push({field:"MensajeValidacion", title:"VALIDACION", width:"200px" });
			}
			else{
				bcsComunes.msg("error",'Error',$translate.instant("ARCHIVO_NO_CONFIGURADO"));
				finalizarCargaDatatec(); //SVARESGIN-7992
				return;
			}
		});
		
		//Obtiene nombre de la Corredora
		consultarEmpresaaDatasource.query().then(function(result){
			if(consultarEmpresaaDatasource.data().length === 1){
				nombreCorredora = consultarEmpresaaDatasource.data()[0].Valor;
			}
			else{
				bcsComunes.msg("error","Error", $translate.instant("NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA"));
			}
		},function(e){
			bcsComunes.msg("error","Error", $translate.instant("NO_SE_PUDO_OBTENER_CONFIGURACION_CORREDORA"));
		});
	};
	
	//Obtiene y valida parametros para el proceso
	var parametrosSP = {};
	parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
	parametrosSP.p_IdTipoProducto = 'MEME';
	parametrosSP.p_CodigoParametro = '';
	
	bcsServiciosMesa.ConsultarConfiguracionMesa(parametrosSP, function(respuesta){
		if (respuesta.P_OUT_DATOSMESA.length === 0){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_OBTENER_PARAMETROS_MESA'));
			bcsComunes.ventanaData(idVentana,$scope.file);
			bcsComunes.ventanaCerrar(idVentana);
        }
		else{
			$scope.data.RutaDatatec = "";
			$scope.data.NombreArchivo = "";
			$.each(respuesta.P_OUT_DATOSMESA, function(key,value) {
				if (value.CodigoParametro === "CODRUTADATATEC")
					$scope.data.RutaDatatec = value.Valor || "";
				else if (value.CodigoParametro === "NOMBREARCHIVODATATEC")
					$scope.data.NombreArchivo = value.Valor || "";
				else if (value.CodigoParametro === "HORAFINPROCESO")
					$scope.data.HoraFinProceso = value.Valor;
				else if (value.CodigoParametro === "INTENTOSDEREPROCESO")
					$scope.data.Intentos = value.Valor;
				else if (value.CodigoParametro === "TIEMPOESPERAREPROCESO")
					$scope.data.Intervalo = value.Valor * 1000;// En segundos
			});
			
			if ($scope.data.RutaDatatec === "" || $scope.data.NombreArchivo === "") {
				bcsComunes.msg("error","error",$translate.instant('UI_FALTAN_PARAMETROS_MESA'));
				bcsComunes.ventanaData(idVentana,$scope.file);
				bcsComunes.ventanaCerrar(idVentana);
			};

			$scope.data.RutaDatatec = $scope.data.RutaDatatec + $scope.data.NombreArchivo;
			BloqueaDATATEC();
        }
		
	}, function(respuesta){
		bcsComunes.msg("error","error",$translate.instant('UI_ERROR_OBTENER_PARAMETROS_MESA'));
		bcsComunes.ventanaData(idVentana,$scope.file);
		bcsComunes.ventanaCerrar(idVentana);
	});
	
	
	// Valida si UI esta en uso
	var BloqueaDATATEC = function() {
		parametrosSP = {};
		parametrosSP.p_IdEmpresa = $scope.SesionIdEmpresa;
		parametrosSP.p_CodigoUsuario = $scope.SesionUsuario;
		
		bcsServiciosMesa.BloquearDATATEC(parametrosSP, function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR > 0 ) {
				bcsComunes.ventanaMsg({
										mensaje:$translate.instant("Carga Online Datatec Bloqueada"),
										conflictos:util.parseServerResponse(respuesta)
										},idVentana); 
				bcsComunes.ventanaData(idVentana,$scope.file);
				bcsComunes.ventanaCerrar(idVentana);
	        }
			else{
				// Modulo bloqueado OK!
				// Programo cada X segundos proceso
				//$interval( ProcesarIntervalo, $scope.data.Intervalo);
				
				//data.ProcesarDATATEC();//TODO
	        }
			
		}, function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_ERROR_BLOQUEAR_INTERFAZ_DATATEC'));
			
			bcsComunes.ventanaData(idVentana,$scope.file);
			bcsComunes.ventanaCerrar(idVentana);
		});
	}
	
	
	// ----------------------------------------------------------------------------------
	var ProcesarIntervalo = function() {
		if ( $scope.data.RutaDatatec === undefined ) {
			// Se salio del control...
			//$interval.cancel(ProcesarIntervalo);
			
			bcsComunes.ventanaData(idVentana,$scope.file);
			bcsComunes.ventanaCerrar(idVentana);
		}
		else {
			// EJECUCIÓN DE PROCESO
			bcsComunes.msg("error","error",$translate.instant('AHORA!'));

			//if( file !== "OK"){
			//	bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_INVALIDO"));
			
			//	bcsComunes.ventanaData(idVentana,$scope.file);
			//  bcsComunes.ventanaCerrar(idVentana);
			//} else {	
				data.ProcesarDATATEC();
			//}		
		}
	};


	//element.on('$destroy', function() {
    //	$interval.cancel(stopTime);
    //	$scope.data.RutaDatatec = 'FALTAN PARAMETROS DATATEC';
    //});
	
	
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
	
	
	//lectura contenido del archivo
  	var cleanLastBlankArray = function(row){
  		if(row[row.length-1].trim().length==0)
  			row.pop(row.length-1);
  	}

  	var parseField = function(fieldConfig, value){
  		if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="STRING"){
  			value = String(value);
  		}else if(fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase()==="NUMBER"){
  			try{
  				if(fieldConfig.IncluyeDecimales==="S"){
  					value = util.incluirDecimales(value.replace(/,|\./g,''), fieldConfig.CantidadDecimales);
  				}else{
  					value = parseInt(value);
  				}
  			}catch(e){
  				$log.debug(e);
  			}
  		}
  		return value;
  	}
  	
  	var validateField = function(fieldConfig, value){
  		if(fieldConfig.EsObligatorio==="S" && value.length<=0){
  			return fieldConfig.CodigoCampo;
  		}
  		if(fieldConfig.Validacion && fieldConfig.Validacion.trim().length>0){
  			var re = new RegExp(fieldConfig.Validacion);
  			if(!re.test(value)){
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
  		}else{
  			return "{0:s}";
  		}
  		return "{0:n3}";
  	}
  	
	var csvToArray = function (csvString, config){
  		// The array we're going to build
  		// var csvArray   = [];
  		// Break it into rows to start
  		var csvRows    = csvString.split(/\n/);
  		// Take off the first line to get the headers
  		var csvHeaders = [];
      		
      	var errors = {};

		for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
			csvRows[rowIndex] = csvRows[rowIndex].replace(/\0/g, " ");
		}
		
		
  		for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
  			var fileRow = csvRows[rowIndex];
  			fileRow = fileRow.replace(/[\r\n]+/g,"");
  			var rowArray = [];

  			if(rowIndex == csvRows.length-1 && fileRow.trim().length==0){
  				//Si es la ultima fila y esta vacia terminar (fix para archivos csv).
  				break;
  			}
  			
  			if(rowIndex==0){
  				var firstRow = [];
  				if(config.tieneSeperador==='S'){
  					//esta en la primera linea y tiene separador por lo tanto, validar.
  					firstRow = csvRows[rowIndex].split(new RegExp(config.SeparadorColumnas));
  					
  					if($scope.data.conHeader==true){
  						cleanLastBlankArray(firstRow);

  						for(var key in firstRow){
  							var campoKey = firstRow[key].trim();
  							console.log(config.campos[campoKey]);
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
  						console.log(firstRow);
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
  				}else{
  					//si es la primera fila, y es sin separador, validar el largo de la columna en la tabla de configuracion.
  					var largoRow = 0; 
  					for(var key in config.headers){ 
  						largoRow += parseInt(config.campos[config.headers[key]].LargoCampo);
  					}
  				}
  			}

  			//ahora construir el arreglo con los datos del archivo.
  			var data = {};
  			var dataError = {};
  			
  			if(config.tieneSeperador==='S' && allCamposObligatorios(config)){
  				//parse por key
  				rowArray = fileRow.split(new RegExp(config.SeparadorColumnas));
  				
  				//(rowArray);
  				
  				var validacion = "OK";
  				for(var key in rowArray){
  					data[csvHeaders[key]]= parseField(config.campos[csvHeaders[key]], rowArray[key])
  					var mensaje = validateField(config.campos[csvHeaders[key]], rowArray[key]);
  					if(mensaje!="OK")validacion=$translate.instant("UI_FALTA_CAMPO_XXX_ARCHIVO").replace("XXX", mensaje);
  				}
  				if(rowArray.length != csvHeaders.length){
  					validacion = $translate.instant("UI_FALTA_CAMPO");
  				}
  				data["MensajeValidacion"] = validacion;
  				csvArray.push(data);
  			}
  			else if(config.tieneSeperador === 'N'){
				//parse por largo
				var posicionInicial = 0;
				var posicionFinal = 0;
				var valor = undefined;
				
				var validacion = "OK";
				for(var key in config.headers){
					posicionFinal = parseInt(config.campos[config.headers[key]].LargoCampo) + posicionInicial;
					
					valor = parseField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal));
					if(typeof valor === 'string'){
						valor = valor.trim();
					}
					data[config.headers[key]] = valor;

					var mensaje = validateField(config.campos[config.headers[key]], fileRow.substring(posicionInicial, posicionFinal) );
					if(mensaje!="OK")
						validacion=mensaje;

					posicionInicial = posicionFinal;
				}

				if(largoRow!=fileRow.length){
					validacion = $translate.instant("UI_FILA_XXX_CON_ERROR").replace("XXX", String(rowIndex+1)) + "("+largoRow+"|"+fileRow.length+")";
					//"La fila "+(rowIndex+1)+" del archivo, no es del mismo tamaño que la definida en la configuracion."
				}

				data["MensajeValidacion"] = validacion;
				csvArray.push(data);
				if(data.MensajeValidacion === "OK"){
					sptArray.push(data);
				}
				else{
					/*errArray.IdInterno = Number(trim(data.CONTADOR));
					errArray.LOGS = data.MensajeValidacion.toUpperCase();
					errArray.CODIGO	= 'ER';*/
					
					dataError["NumeroRegistro"] = Number(trim(data.CONTADOR));
					dataError["GlosaProceso"] = data.MensajeValidacion.toUpperCase();
					dataError["IdEstadoRegistro"] = 'ERR';
					errArray.push(dataError);
				}
  			}		
  		}
  		if(errors.codigo){
  			return errors;//existen errores en la cabecera, desplegarlos en la pantalla.
  		}else{
  			return csvArray;
  		}
  	};
  	
  	
  	
  	
  	$scope.data.ProcesarDATATEC = function(){
		//SVARESGIN-7992 - evita múltiples ejecuciones simultáneas
	    if ($scope.data.cargandoDatatec) {
	        console.log("Carga ignorada: ya existe un proceso en ejecución");
	        return;
	    }  
	    
	    //SVARESGIN-7992 
		var finalizarCargaDatatec = function() {
		    $scope.data.texto = false;
		    $scope.data.cargandoDatatec = false;
		    $scope.$evalAsync();
		};
  		if(!$scope.data.fileContent || !$scope.data.fileName){//TODO eliminar cuando se defina proceso automatico
  			bcsComunes.msg("error",'Error',$translate.instant("DEBE_SELECCIONAR_ARCHIVO_CON_REGISTROS"));
  			return;
  		}
  		
  		$scope.data.cargandoDatatec = true; //SVARESGIN-7992
  		dsLog.removeAll();
    	sptArray=[];
    	
    	$scope.data.texto = true;
		if($scope.configColumnas.length > 0){
			$scope.configArchivo.tieneSeperador = $scope.configColumnas[0].TieneSeparador;	
			if($scope.configArchivo.tieneSeperador === 'S'){
				//archivo con separador de columnas	
				$scope.configArchivo.SeparadorColumnas = $scope.configColumnas[0].SeparadorColumnas;
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
			$scope.file.fileData = csvToArray($scope.data.fileContent,$scope.configArchivo);
			if(!$scope.file.fileData.codigo){
				//se deja nombre archivo en objeto salida
				$scope.file.fileName = $scope.data.fileName;
			}else{
				bcsComunes.msg("error",'Error', $scope.file.fileData.mensaje);
			}

		}else{
			bcsComunes.msg("error",'Error',$translate.instant("ARCHIVO_NO_CONFIGURADO"));
		}

		//* --------------------------------------------------------------------------------------------
    	//* PROCESO 
    	//* --------------------------------------------------------------------------------------------
		var parametrosPuntas = {};
		parametrosPuntas.p_IdEmpresa       = $scope.SesionIdEmpresa; 
		parametrosPuntas.p_IdMoneda        = 'USD'; 
		parametrosPuntas.p_MontoPorTramo   = 0;
		
		bcsServiciosMesa.ConsultarPuntasPorTramo(parametrosPuntas, function(respuesta){	
			if(respuesta.P_OUT_ADVERTENCIASDETALLE.length !=0){
				if(respuesta.P_OUT_ADVERTENCIASDETALLE[0].Codigo === 'PUNTA'){
				   	bcsComunes.msg("error","error",$translate.instant('UI_FALTA_CONF_PUNTA'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
				
				if(respuesta.P_OUT_ADVERTENCIASDETALLE[0].Codigo === 'TRAMO'){
				   	bcsComunes.msg("error","error",$translate.instant('UI_FALTA_CONF_TRAMO'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			}
			
			if((respuesta.P_OUT_PUNTAS.length) != 0){
				datosTramos = respuesta.P_OUT_PUNTAS;
				$.each(csvArray, function(key,value){
					//if(trim(value.INSTITUCIONPOSTURA) === 'FIT RESEARCH S.A.'){
					if(trim(value.INSTITUCIONPOSTURA) === nombreCorredora){
						value.IDOPERACION = value.OD === 'D' ? 'COMSPT' : 'VENSPT';
					}
					else{
						value.IDOPERACION = value.OD === 'D' ? 'VENSPT' : 'COMSPT';
					}
					
					if (value.IDOPERACION === 'COMSPT'){
						value.MARGEN = ((value.MONTOPUNTA||0) - (value.PRECIO||0)) * (value.NUMEROTRANSADO||0);
					}
					else if (value.IDOPERACION === 'VENSPT'){
						value.MARGEN = ((value.PRECIO||0) - (value.MONTOPUNTA||0)) * (value.NUMEROTRANSADO||0);
					}
					
					if(respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo === 'NO'){
						value.VALORPUNTACOMPRA = respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault;
						value.VALORPUNTAVENTA = respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault;
						if (value.IDOPERACION === 'COMSPT'){
							value.MONTOPUNTA = respuesta.P_OUT_PUNTAS[0].PuntaCompra||'';
						}
						else if (value.IDOPERACION === 'VENSPT'){
							value.MONTOPUNTA = respuesta.P_OUT_PUNTAS[0].PuntaVenta||'';
						}
					}
					else if(respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo === 'SI'){
						var x = obtieneCostoPorTramo(trim(value.NUMEROTRANSADO),value.IDOPERACION);
						value.VALORPUNTACOMPRA	= x.VALORPUNTACOMPRA;
						value.VALORPUNTAVENTA 	= x.VALORPUNTAVENTA;
						value.MONTOPUNTA		= x.MONTOPUNTA;
					}
				});
				
				//LLAMAR SERVICIO INGRESO DATATEC
				var p_IngresoDatatec = {};
				p_IngresoDatatec.p_IdEmpresa		= $scope.SesionIdEmpresa; 
				p_IngresoDatatec.p_IdUsuario		= $scope.SesionIdUsuario;
				p_IngresoDatatec.p_IdModulo			= 'MSP';
				p_IngresoDatatec.p_UsaReglasNegocio	= 'S';
				p_IngresoDatatec.p_DatosDatatec		= sptArray;
				
				bcsServiciosMesa.IngresoDatatec(p_IngresoDatatec, function(respuesta){
					if(respuesta.P_OUT_ERRORESCONTADOR === 0){
						if(errArray.length != 0){
							respuesta.P_OUT_DATOSDATATEC = respuesta.P_OUT_DATOSDATATEC.concat(errArray);
						}
						
						$scope.confGrillaLog.dataSource.data(respuesta.P_OUT_DATOSDATATEC);
						if(respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR > 0){
							$.each(respuesta.P_OUT_ERRORCONFIGURACIONDETALLE, function(key,value){
								bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.codigo));
							});
						}
					}
					else{
						bcsComunes.ventanaMsg({mensaje:$translate.instant("UI_ERROR_PROCESAR"),conflictos:util.parseServerResponse(respuesta)},idVentana);
					}
					//$scope.data.texto = false;
					finalizarCargaDatatec(); //SVARESGIN-7992
					
				}, function(respuesta){
					//$scope.data.texto = false;
					finalizarCargaDatatec(); //SVARESGIN-7992
					
					bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
					bcsComunes.setConflictos(util.parseServerResponse(respuesta));
					bcsComunes.ventanaOverlay($scope.winID, false);
				});
			}
		else{
			finalizarCargaDatatec(); //SVARESGIN-7992
		}
		}, function(respuesta){
			//$scope.data.texto = false;
			finalizarCargaDatatec(); //SVARESGIN-7992
			
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID, false);
		});
	};
	
	
	//Funcion Busca las puntas dependiendo del tramo
	obtieneCostoPorTramo = function(monto,IDOPERACION){
		var montoTramoAnterior = 999999999999;
		var continuar = true;
		var MONTOPUNTA = '';
		
		if (monto > (datosTramos[datosTramos.length-1].MontoPorTramo)){
			if(IDOPERACION === 'COMSPT'){
				puntas = {"VALORPUNTACOMPRA": datosTramos[datosTramos.length-1].PuntaCompraDefault||''
			 	 		 ,"VALORPUNTAVENTA" : datosTramos[datosTramos.length-1].PuntaVentaDefault||''
			 	 		 ,"MONTOPUNTA"		: datosTramos[datosTramos.length-1].PuntaCompra||''};
			}
			else if(IDOPERACION === 'VENSPT'){
				puntas = {"VALORPUNTACOMPRA": datosTramos[datosTramos.length-1].PuntaCompraDefault||''
						,"VALORPUNTAVENTA"  : datosTramos[datosTramos.length-1].PuntaVentaDefault||''
						,"MONTOPUNTA"		: datosTramos[datosTramos.length-1].PuntaVenta||''
				};
			}
		}
		else if(monto < (datosTramos[0].MontoPorTramo)){
			if(IDOPERACION === 'COMSPT'){
				puntas = {"VALORPUNTACOMPRA": datosTramos[datosTramos.length-1].PuntaCompraDefault||''
				 	 	 ,"VALORPUNTAVENTA"	: datosTramos[datosTramos.length-1].PuntaVentaDefault||''
				 	 	 ,"MONTOPUNTA"		: datosTramos[0].PuntaCompra||''};
			}
			else if(IDOPERACION === 'VENSPT'){
				puntas = {"VALORPUNTACOMPRA": datosTramos[datosTramos.length-1].PuntaCompraDefault||''
					 	 ,"VALORPUNTAVENTA"	: datosTramos[datosTramos.length-1].PuntaVentaDefault||''
						 ,"MONTOPUNTA"		: datosTramos[0].PuntaVenta||''	};
			}
		}
		else{
			$.each(datosTramos, function(index,value){
				//(continuar === true)
        		if ((monto >= value.MontoPorTramo)){
					if (IDOPERACION === 'COMSPT'){
						puntas = {"VALORPUNTACOMPRA": datosTramos[datosTramos.length-1].PuntaCompraDefault||''
								 ,"VALORPUNTAVENTA"	: datosTramos[datosTramos.length-1].PuntaVentaDefault||''
								 ,"MONTOPUNTA"		: value.PuntaCompra||''};
					}
					else if (IDOPERACION === 'VENSPT'){
						puntas = {"VALORPUNTACOMPRA": datosTramos[datosTramos.length-1].PuntaCompraDefault||''
							 	 ,"VALORPUNTAVENTA"	: datosTramos[datosTramos.length-1].PuntaVentaDefault||''
								 ,"MONTOPUNTA"		: value.PuntaVenta||''	};
					}
					montoTramoAnterior = value.MontoPorTramo
					continuar = false
        		}
        		else{
        			montoTramoAnterior = value.MontoPorTramo;
        		}
        	});
		}
		//$scope.data.kngTipoCambioCosto = MONTOPUNTA||'';
		return puntas;
	}
	
	
	//Limpiar grillas
	$scope.data.limpiarDatos = function(){
		dsLog.removeAll();
		csvArray = [];
		$scope.data.fileContent = null;
		$scope.data.fileName = null;
	};
	
});