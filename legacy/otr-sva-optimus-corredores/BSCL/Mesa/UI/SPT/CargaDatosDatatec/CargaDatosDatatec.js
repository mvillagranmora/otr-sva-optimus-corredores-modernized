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

(function() {	
	angular.module('ModuloMesa').controller('DatatecSpt',
	function($scope,$translate,$resource,$timeout,bcsComunes,bcsServiciosMesa){
		
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
		
		$scope.data = {};
      	$scope.data.conHeader = false;
      	$scope.config= {};
      	$scope.configArchivo = {};
      	$scope.configColumnas = [];
      	$scope.config.overlay = bcsComunes.overlay;
      	$scope.data.texto = false;
    	
      	$scope.configArchivo.codigoArchivo = 'CARGA_ARCHIVO_DATATEC';
		
		ingSpot.datosAntes = {};
		ingSpot.datosDespues = {};
		ingSpot.datosAntes = angular.copy(ingSpot.datosDespues);
		
		//Configurar al principio
		$scope.data.kngFecha = util.dateInicioDia($scope.SesionFechaSistema);
		ingSpot.data = {};
		
		//Valores para el SP Consultar Ruta Datatec
		var ObtenerValores = function(datos, success_fn,error_fn){
  			var urlObtenerValores = "/BSCL/Plataforma/GWJAVA/PLA/call?t=511";
  			var r = $resource("", {},{
  				'ConsultarPosicionInicial': { method: "POST", url: urlObtenerValores}
  			});
  			var post = r.ConsultarPosicionInicial(datos);
  			post.$promise.then(success_fn).catch(error_fn);	
  		};
  		
  		var datos = {};
		
		var successIngreso = function(respuesta){
			if (respuesta.P_OUT_DATOSMESA[0].Valor === undefined){
				$scope.data.RutaDatatec = 'No Existe Ruta Datatec Configurada';
				bcsComunes.msg("error","error",$translate.instant('UI_RUTA_DATATEC_NO_EXISTE'));
            }
			else{
				$scope.data.RutaDatatec = respuesta.P_OUT_DATOSMESA[0].Valor;
            }
		};
		
		datos.p_IdEmpresa = $scope.SesionIdEmpresa;
		datos.p_IdTipoProducto = 'MEME';
		datos.p_CodigoParametro = 'CODRUTADATATEC';
		ObtenerValores(datos, successIngreso,error);
		
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
      			var fileRow = csvRows[rowIndex];
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
      					var largoRow = 1; 
      					for(var key in config.headers){ 
      						largoRow += parseInt(config.campos[config.headers[key]].LargoCampo);
      					}
      				}
      			}

      			//ahora construir el arreglo con los datos del archivo.
      			var data = {};
      			
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
  						validacion = $translate.instant("UI_FILA_XXX_CON_ERROR").replace("XXX", String(rowIndex+1));
  						//"La fila "+(rowIndex+1)+" del archivo, no es del mismo tamaño que la definida en la configuracion."
  					}

  					data["MensajeValidacion"] = validacion;
  					csvArray.push(data);
  					if(data.MensajeValidacion === "OK"){
  						sptArray.push(data);
  					}
  					else{
  						//errArray.push(data);      						
  						errArray.IdInterno = Number(trim(data.CONTADOR));
  						errArray.LOGS = data.MensajeValidacion.toUpperCase();
  						errArray.CODIGO	= 'ER';
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
      	
      	$scope.data.cargarArchivo = function(){
      		if(!$scope.data.fileContent || !$scope.data.fileName){
      			bcsComunes.msg("error",'Error',$translate.instant("DEBE_SELECCIONAR_ARCHIVO"));
      			return;
      		}
      		
	    	sptArray=[];
	    	
	    	//REPROCESO 
	 		var parametrosReproceso = {};
			parametrosReproceso.p_IdEmpresa 		= $scope.SesionIdEmpresa; 
			parametrosReproceso.p_IdUsuario 		= $scope.SesionIdUsuario;
			parametrosReproceso.p_IdModulo			= 'MSP';
			parametrosReproceso.p_UsaReglasNegocio  = 'S';
			parametrosReproceso.p_Fecha				= util.dateInicioDia($scope.SesionFechaSistema);
	    	bcsServiciosMesa.ReprocesoDatatec(parametrosReproceso,successReproceso,error);
    	
	    	$scope.data.texto = true;
			if($scope.configColumnas.length > 0){
				//$scope.gridApiCargaArchivo.dataSource.read();//limpiar la grilla
	
				$scope.configArchivo.tieneSeperador = $scope.configColumnas[0].TieneSeparador;	
				if($scope.configArchivo.tieneSeperador==='S'){
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
					//construir columnas de la grilla
					//$scope.gridApiCargaArchivo.dataSource.data($scope.file.fileData, false);
					//$scope.gridApiCargaArchivo.refresh();
					//se deja nombre archivo en objeto salida
					$scope.file.fileName = $scope.data.fileName;
				}else{
					bcsComunes.msg("error",'Error', $scope.file.fileData.mensaje);
				}
	
			}else{
				bcsComunes.msg("error",'Error',$translate.instant("ARCHIVO_NO_CONFIGURADO"));
			}

			var parametrosPuntas = {};
			parametrosPuntas.p_IdEmpresa       = $scope.SesionIdEmpresa; 
			parametrosPuntas.p_IdMoneda        = 'USD'; 
			parametrosPuntas.p_MontoPorTramo   = 0;
			
			bcsServiciosMesa.ConsultarPuntasPorTramo(parametrosPuntas,function(respuesta){	
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
				
				if((respuesta.P_OUT_PUNTAS.length)!=0){
					datosTramos = respuesta.P_OUT_PUNTAS;
					$.each(csvArray,function(key,value){
						if(respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo === 'NO'){
							value.IDOPERACION = TipoOperacion(value.OD,value.INSTITUCIONPOSTURA);
							value.VALORPUNTACOMPRA = respuesta.P_OUT_PUNTAS[0].PuntaCompraDefault;
							value.VALORPUNTAVENTA = respuesta.P_OUT_PUNTAS[0].PuntaVentaDefault;
							if (value.IDOPERACION = 'COMSPT'){
								value.MONTOPUNTA = respuesta.P_OUT_PUNTAS[0].PuntaCompra||'';
							}
							if (value.IDOPERACION = 'VENSPT'){
								value.MONTOPUNTA = respuesta.P_OUT_PUNTAS[0].PuntaVenta||'';
							}
							value.MARGEN = calculoMargen(IDOPERACION,'CLP',value.PRECIO,value.NUMEROTRANSADO,value.MONTOPUNTA);
						}
						if(respuesta.P_OUT_PUNTAS[0].CheckPrecioPorTramo === 'SI'){
							value.IDOPERACION = TipoOperacion(value.OD,value.INSTITUCIONPOSTURA);
							var x = obtieneCostoPorTramo(trim(value.NUMEROTRANSADO),value.IDOPERACION);
							value.VALORPUNTACOMPRA	= x.VALORPUNTACOMPRA;
							value.VALORPUNTAVENTA 	= x.VALORPUNTAVENTA;
							value.MONTOPUNTA		= x.MONTOPUNTA;
							value.MARGEN			= calculoMargen(value.IDOPERACION,'CLP',value.PRECIO,value.NUMEROTRANSADO,value.MONTOPUNTA);
						}
					});
					
					//LLAMAR SERVICIO INGRESO SPOT
					var p_IngresoDatatec = {};
					p_IngresoDatatec.p_IdEmpresa       = $scope.SesionIdEmpresa; 
					p_IngresoDatatec.p_IdUsuario		= $scope.SesionIdUsuario;
					p_IngresoDatatec.p_IdModulo			= 'MSP';
					p_IngresoDatatec.p_UsaReglasNegocio	= 'S';
					p_IngresoDatatec.p_DatosDatatec		= sptArray;
					bcsServiciosMesa.IngresoDatatec(p_IngresoDatatec,successDatatec,error);//TODO
				}
			}
			,error);
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
			
			ArchivosFormatoDatasource.query({filter:filterFile}).then(function() {
				$scope.configColumnas = ArchivosFormatoDatasource.view();
				
				if(ArchivosFormatoDatasource.view().length > 0){
					if(ArchivosFormatoDatasource.view()[0].TieneCabecera==="S"){
						$scope.data.conHeader = true;
					}
					$.each(ArchivosFormatoDatasource.view(), function(key, value){
						columnas.push({field: value.CodigoCampo, title:value.CodigoCampo, width:(value.CodigoCampo.length*1.5)+"em", format: getColumnFormat(value), hidden:false});
						campos[value.CodigoCampo] = { type : value.TipoDato.toLowerCase() };
					});
					columnas.push({field:"MensajeValidacion", title:"VALIDACION", width:"200px" });
									
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
						//$scope.gridApiCargaArchivo.setOptions({columns: columnas});

				}else{
					bcsComunes.msg("error",'Error',$translate.instant("ARCHIVO_NO_CONFIGURADO"));
				}
			});
		};
	
		
		$scope.cerrar = function () {
			//retorna undefined si el archivo no se cargo; en caso contrario, retorna un array de objetos y nombre del archivo.
			bcsComunes.ventanaData(idVentana,$scope.file);
			bcsComunes.ventanaCerrar(idVentana);
		}

		//------FIN CARGAARCHIVO.JS	
		//puntas
		/*IngresoDatatec = function(csvArray){
		}*/
		
		
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
				$.each (datosTramos,function(index,value){
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
		
		
		TipoOperacion = function(OD,INSTITUCIONPOSTURA){
			//var c = InstitucionPostura.toUpperCase();
			if(trim(INSTITUCIONPOSTURA) === 'FIT RESEARCH S.A.'){
				if(OD === 'D'){
					return IDOPERACION = 'COMSPT';
				}
				else{
					return IDOPERACION = 'VENSPT';
				}
			}
			else{
				if(OD === 'D'){
					return IDOPERACION = 'VENSPT';
				}
				else{
					return IDOPERACION = 'COMSPT';
				}
			}
		}
		
		
		//Funcion Calcula MARGEN
		calculoMargen = function(IDOPERACION,MONEDA,PRECIO,NUMEROTRANSADO,MONTOPUNTA){
			//COMPRA CLP
			if(IDOPERACION === 'COMSPT' && MONEDA === 'CLP')
				return MARGEN = (((MONTOPUNTA||0) - (PRECIO||0))*(NUMEROTRANSADO||0));
			//VENTA CLP
			if(IDOPERACION === 'VENSPT' && MONEDA === 'CLP')
				return MARGEN = (((PRECIO||0) - (MONTOPUNTA||0))*(NUMEROTRANSADO||0));
		};
		
		
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
		
		
		var error = function(respuesta){
			bcsComunes.msg("error","error",$translate.instant('UI_OPERACION_INGRESO_ERROR'));
			bcsComunes.setConflictos(util.parseServerResponse(respuesta));
			bcsComunes.ventanaOverlay($scope.winID, false);
		};
		
		
		var successDatatec = function(respuesta){
			if(respuesta.P_OUT_ERRORCONFIG === 0){
				if(errArray != 0){
					respuesta.P_OUT_DATOSDATATEC.push(errArray);
				}
				$.each(respuesta.P_OUT_DATOSDATATEC,function(key,value){
					dsLog.data().push(value);
				});
				$scope.data.texto = false;
			}
			if(respuesta.P_OUT_ERRORCONFIG != 0){
				$.each(respuesta.P_OUT_ERRORCONFIGDETALLE,function(key,value){
					bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
				});
			}
		};
		
		var successReproceso = function(respuesta){
			if(respuesta.P_OUT_ERRORCONFIG === 0 || respuesta.P_OUT_ERRORCONFIG === undefined){
				if(respuesta.P_OUT_ERRORCONTADOR === 0 || respuesta.P_OUT_ERRORCONTADOR === undefined || respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR === 0 || respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR === undefined){
					if(respuesta.P_OUT_DATOSDATATEC.length != 0){
						$.each(respuesta.P_OUT_DATOSDATATEC,function(key,value){
							dsLog.data().push(value);
						});
						$scope.data.texto = false;
					}
				}
				if(respuesta.P_OUT_ERRORCONTADOR != 0 || respuesta.P_OUT_ERRORCONTADOR != undefined || respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR != 0 || respuesta.P_OUT_ERRORCONFIGURACIONCONTADOR != undefined){					
					$.each(respuesta.P_OUT_ERRORDETALLE,function(key,value){
						bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant(value.Descripcion));
					});
				}
			}
		};
		
		
		//Limpiar grillas
		$scope.data.limpiarDatos = function(){
			dsLog.removeAll();
			csvArray = [];
			$scope.data.fileName = '';
		};
		
		
		var dsDefDatatec = {
			type : "odata",
			definition: {
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging    : true, // Paginado de los datos en el servidor.
				serverSorting   : true, // Ordenamiento de los datos en el servidor.
				sort 			: { field: "IdInterno", dir: "ASC" },
				pageSize        : 13,
				schema: {
					model: {
						fields: {
							IdInterno	: {type: "number"},
							LOGS		: {type: "string"},
							CODIGO		: {type: "string"}
						}
					}
				}
			}
		};
		
		
		//GRILLA
		var dsLog = new bcs.DataSource(dsDefDatatec);
		$scope.confGrillaLog = {
			dataSource: dsLog,
    		columns : [
    		     {field: "IdInterno",	title: "UI_NRO_REGISTRO",	width: "15px",	align: "left", sortable : true, editable: false  }
    		    ,{field: "LOGS",		title: "UI_LOG",			width: "110px",	align: "left", sortable : true, editable: false }
    		],
    		sortable	: true,
    		pageable	: true,
			filterable	: false,
			navigatable	: true,
			height		: 350,
			selectable	: "row",
		    autoBind	: false,
		    gridCreated	: function(grid){$scope.buildGrid();},
			rowColorBackground :
				function(row) {
					if(row.CODIGO === 'ER') return "red";
					if(row.CODIGO === 'PEN') return "yellow";
					return "";
				}
       	};
		
	});
	
}());