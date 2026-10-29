angular.module('ModuloPlataforma').directive('fileReaderTasas', function() {
	return {
		//restrict: 'A',
		scope: {
			fileReaderTasas:"=",
			fileName:"=",
			fileType:"="
		},
		link: function(scope, element) {
			$(element).on('change', function(changeEvent) {
				var files = changeEvent.target.files;
				if (files.length) {
					var r = new FileReader();
					r.onload = function(e) {
						var contents = e.target.result;
						scope.$apply(function () {
							scope.fileReaderTasas = contents;
							scope.fileName = files[0].name;
							scope.fileType = files[0].type;
						});
					};
					r.readAsText(files[0],'iso-8859-1');
				}
			});
		}
	};
});


angular.module('ModuloPlataforma').controller('ModalCargarArchivoTasasCtrl',
	function($scope,bcsComunes,$resource,$log,$translate,bcsServiciosTasas){
	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mCAT 		= this;
		mCAT.idVentana	= bcsComunes.ventanaWinId($scope);
		mCAT.parametros	= bcsComunes.ventanaParams(mCAT.idVentana);
		
		mCAT.fn	= {};
		
		mCAT.dataCabecera	= {};
		mCAT.dataDetalle	= {};
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		mCAT.fn.getDataSourceParametro = function () {
			return new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering : true, 
					serverSorting : true, 
					sort: { field: "CodigoParametro", dir: "asc" }, //campo y criterio de ordenamiento
					transport: {
			    		read: {
			    			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
							dataType:"json"
			    		}
			    	},
			    	schema : {
						model : {
							fields : {
								Modulo : { type: "string"},
								CodigoParametro  : { type: "string"},
								Descripcion  : { type: "string"},
								Valor  : { type: "string"},
								Uso : { type: "string"},
								ValorDefecto  : { type: "string"},
								ValoresPosibles  : { type: "string"},
								ModificablePorUsuario  : { type: "string"},
								ModificablePostProduccion : { type: "string"},
								CreadorParametro  : { type: "string"},
								FechaCreacion  : { type: "datetime"},
								ModificadorParametro  : { type: "string"},
								FechaModificacion : { type: "datetime"},
								Observaciones  : { type: "string"},
				           	}
						}
					}		
				}
			})
		};
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mCAT.fn.seleccionarArchivo = function(){
			$("#file").click();
		};
		
		
		mCAT.fn.cargarArchivo = function(){
			console.log($scope.data);
			if($scope.data.fileContent && $scope.data.fileName){
				try{
					if ($scope.data.fileType === "text/xml"){
						var xmlDoc = new DOMParser().parseFromString($scope.data.fileContent,"text/xml");
						
						if(xmlDoc.hasChildNodes()){
							//CABECERA
							mCAT.dataCabecera.Servicio	= xmlDoc.getElementsByTagName("SERVICIO")[0].childNodes[0].nodeValue;
							mCAT.dataCabecera.TipoTasa	= xmlDoc.getElementsByTagName("TIPOTASA")[0] ? xmlDoc.getElementsByTagName("TIPOTASA")[0].childNodes[0].nodeValue : '';
							mCAT.dataCabecera.Nombre	= xmlDoc.getElementsByTagName("NOMBRE")[0].childNodes[0].nodeValue;
							mCAT.dataCabecera.Cuenta	= xmlDoc.getElementsByTagName("CUENTA")[0] && xmlDoc.getElementsByTagName("CUENTA")[0].childNodes[0] ? xmlDoc.getElementsByTagName("CUENTA")[0].childNodes[0].nodeValue : '0';
							mCAT.dataCabecera.Rut		= xmlDoc.getElementsByTagName("RUT")[0].childNodes[0].nodeValue;
							mCAT.dataCabecera.Fecha		= xmlDoc.getElementsByTagName("FECHA")[0].childNodes[0].nodeValue;
							mCAT.dataCabecera.Hora		= xmlDoc.getElementsByTagName("HORA")[0].childNodes[0].nodeValue.substring(0,8);
							mCAT.dataCabecera.Registros	= mCAT.dataCabecera.TipoTasa === 'CURVA' || mCAT.dataCabecera.TipoTasa === 'PUNTO' ? xmlDoc.getElementsByTagName("BUENOS")[0].childNodes[0].nodeValue : xmlDoc.getElementsByTagName("REGISTROS")[0].childNodes[0].nodeValue;
							mCAT.dataCabecera.TipoArchivo = 'XML';

							//DETALLE
							var detalle = [];
							for(var i=0; i<xmlDoc.getElementsByTagName("REGISTRO").length; i++){
								var aux = {};
								aux.Nemo		= xmlDoc.getElementsByTagName("NEMO")[i] && xmlDoc.getElementsByTagName("NEMO")[i].childNodes[0] ? xmlDoc.getElementsByTagName("NEMO")[i].childNodes[0].nodeValue : '';
								aux.IdMoneda	= xmlDoc.getElementsByTagName("MONEDA")[i] ? xmlDoc.getElementsByTagName("MONEDA")[i].childNodes[0].nodeValue : '';
								aux.Tasa		= xmlDoc.getElementsByTagName("TASA")[i]? xmlDoc.getElementsByTagName("TASA")[i].childNodes[0].nodeValue.replace(',','.') : xmlDoc.getElementsByTagName("TASA-ESTIMADA")[i].childNodes[0].nodeValue.replace(',','.');
								aux.TasaEstimada= xmlDoc.getElementsByTagName("TASA-ESTIMADA")[i].childNodes[0].nodeValue.replace(',','.');
								aux.Plazo		= xmlDoc.getElementsByTagName("PLAZO")[i] ? xmlDoc.getElementsByTagName("PLAZO")[i].childNodes[0].nodeValue : xmlDoc.getElementsByTagName("PERIODO")[i] ? xmlDoc.getElementsByTagName("PERIODO")[i].childNodes[0].nodeValue : 0;
								detalle.push(aux);
							}
							mCAT.dataDetalle = detalle;
							
							mCAT.fn.cargarArchivoTasas();
						}
					}
					else if ($scope.data.fileType === "application/vnd.ms-excel" || $scope.data.fileType === "text/plain"){
						var txtDoc = $scope.data.fileContent.split("\n");
						
						if(!mCAT.parametros.IdClasTipoInst || mCAT.parametros.IdClasTipoInst === 0){
				        	bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_DEBE_INGRESAR_CLASIFICACION_TIPO_INSTRUMENTO"))
				        	bcsComunes.ventanaData(mCAT.idVentana,false);
							bcsComunes.ventanaCerrar(mCAT.idVentana);
							return;
				        }
						if (!mCAT.parametros.Fuente || mCAT.parametros.Fuente.Codigo === 0){
							bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_DEBE_INGRESAR_FUENTE"))
				        	bcsComunes.ventanaData(mCAT.idVentana,false);
							bcsComunes.ventanaCerrar(mCAT.idVentana);
							return;
						}
						if (!mCAT.parametros.TipoRegistro || mCAT.parametros.TipoRegistro.Valor === 0){
							bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_DEBE_INGRESAR_TIPO_REGISTRO"))
				        	bcsComunes.ventanaData(mCAT.idVentana,false);
							bcsComunes.ventanaCerrar(mCAT.idVentana);
							return;
						}
						
						if (txtDoc.length > 0){
							var dataSourceParam = mCAT.fn.getDataSourceParametro();
							var filters = new Array();
							
							filters.push( {
								logic: "and",
							    filters: [
							      {	logic: "or",
								    filters: [
								      { field: "CodigoParametro", operator: "eq", value: "POSICIONARCHIVONEMO" },
								      { field: "CodigoParametro", operator: "eq", value: "POSICIONARCHIVOTASA" }
								    ]
								  }, 
							      {	logic: "and",
								    filters: [
								    	{ field: "IdEmpresa", operator: "eq", value: Number($scope.$root.SesionIdEmpresa)}
										,{ field: "Modulo", operator: "eq", value: "MESA"}
								    ]
							      } 
							    ]
							  }
							);
														
							dataSourceParam.query({filter : filters}).then(function(){
								var data = dataSourceParam.data();
								console.log(data);
								if (data.length === 2){
									//CABECERA
									var fecha = util.dateSPdate(mCAT.parametros.FechaCreSol).split("-");
									var posNemo = data[0].CodigoParametro === 'POSICIONARCHIVONEMO'?Number(data[0].Valor):data[1].CodigoParametro === 'POSICIONARCHIVONEMO'?Number(data[1].Valor):0;
									var posTasa = data[0].CodigoParametro === 'POSICIONARCHIVOTASA'?Number(data[0].Valor):data[1].CodigoParametro === 'POSICIONARCHIVOTASA'?Number(data[1].Valor):1;
									
									mCAT.dataCabecera.Servicio	= mCAT.parametros.Fuente.Descripcion;
									mCAT.dataCabecera.TipoTasa	= mCAT.parametros.TipoRegistro.Valor;
									mCAT.dataCabecera.Nombre	= $scope.SesionEmpresa;
									mCAT.dataCabecera.Cuenta	= mCAT.parametros.Fuente.Codigo;
									mCAT.dataCabecera.Rut		= '';
									mCAT.dataCabecera.Fecha		= fecha[2]+'-'+fecha[1]+'-'+fecha[0];
									mCAT.dataCabecera.Hora		= '';

									if(mCAT.dataCabecera.TipoTasa === 'TASA'){
										if(txtDoc[0] && txtDoc[0].split(";").length === 3){
											mCAT.dataCabecera.TipoTasa = 'CURVA';
										}
									}
									//DETALLE
									var detalle = [];
									var cantReg = 0;
									for(var i=0; i<txtDoc.length; i++){
										var txtDocLine = txtDoc[i].split(";");
										if (txtDocLine.length === 3 && txtDocLine[0].split("/").length > 1){
											var aux = {};
											aux.Nemo		= '';
											aux.IdMoneda	= txtDocLine[0].split("/")[0].trim();
											aux.Tasa		= Number(txtDocLine[2].replace(',','.'));
											aux.TasaEstimada= Number(txtDocLine[2].replace(',','.'));
											aux.Plazo		= Number(txtDocLine[1]);
											detalle.push(aux);
											cantReg++;
										}
										else if(txtDocLine.length === 2){
											var aux = {};
											aux.Nemo		= txtDocLine[posNemo].trim();
											aux.IdMoneda	= '';
											aux.Tasa		= Number(txtDocLine[posTasa].replace(',','.'));
											aux.TasaEstimada= Number(txtDocLine[posTasa].replace(',','.'));
											aux.Plazo		= Number(0);
											detalle.push(aux);
											cantReg++;
										}
									}
									mCAT.dataCabecera.Registros	= Number(cantReg);
									mCAT.dataCabecera.TipoArchivo = 'CSV';
									mCAT.dataDetalle = detalle;
									mCAT.fn.cargarArchivoTasas();
								}
								else {bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_ERROR_CONFIGURACION_PARAMETRO_ARCHIVO"));}
							});
						}
						else {bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_ESTRUCTURA_ARCHIVO_INCORRECTA"));}
					}
					else {
						bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_ARCHIVO_INVALIDO"));
					}
				}
				catch(e){
					$log.debug(e);
					console.log(e);
					bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_ESTRUCTURA_ARCHIVO_INCORRECTA"));
				}
			}
		};
		
		
		mCAT.fn.cargarArchivoTasas = function(){
			var parametrosSP = {};
			var IdClasificacionTipoInstrumento = ''
			parametrosSP.p_IdEmpresa		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario		= $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo			= 'PLA';
	        parametrosSP.p_UsaReglasNegocio	= 'N';
	        
	        if((mCAT.dataCabecera.TipoTasa === 'CURVA' || mCAT.dataCabecera.TipoTasa === 'PUNTO') && (!(mCAT.parametros.IdClasTipoInst) || mCAT.parametros.IdClasTipoInst === 0)){
	        	bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_DEBE_INGRESAR_CLASIFICACION_TIPO_INSTRUMENTO"))
	        	bcsComunes.ventanaData(mCAT.idVentana,false);
				bcsComunes.ventanaCerrar(mCAT.idVentana);
				return;
	        }
        	
	        var fechaCabecera = '';
	        if(mCAT.parametros.FechaCreSol && mCAT.dataCabecera.Fecha){
	        	var fechaFiltro	= util.dateSPdate(mCAT.parametros.FechaCreSol);
	        	fechaCabecera	= mCAT.dataCabecera.Fecha.substring(6)+'-'+mCAT.dataCabecera.Fecha.substring(3,5)+'-'+mCAT.dataCabecera.Fecha.substring(0,2);;
	        	if(fechaFiltro !== fechaCabecera){
	        		bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_FECHA_CARGA_INCORRECTA"))
		        	bcsComunes.ventanaData(mCAT.idVentana,false);
					bcsComunes.ventanaCerrar(mCAT.idVentana);
					return;
	        	}
	        }
	        else{
	        	bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_DEBE_INGRESAR_FECHA"))
	        	bcsComunes.ventanaData(mCAT.idVentana,false);
				bcsComunes.ventanaCerrar(mCAT.idVentana);
				return;
	        }
			if (!mCAT.dataCabecera.Servicio){
				bcsComunes.msg("error",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_DEBE_INGRESAR_FUENTE"))
	        	bcsComunes.ventanaData(mCAT.idVentana,false);
				bcsComunes.ventanaCerrar(mCAT.idVentana);
				return;
			}
			if(mCAT.dataDetalle.length){
				parametrosSP.p_Archivo		= $scope.data.fileName;
				parametrosSP.p_IdClasificacionTipoInstrumento = mCAT.parametros.IdClasTipoInst == 0 ? '': mCAT.parametros.IdClasTipoInst;
				parametrosSP.p_FechaCreSol	= fechaCabecera;
				parametrosSP.p_IdTipoTasa	= mCAT.dataCabecera.TipoTasa;
				//parametrosSP.p_Fuente		= mCAT.dataCabecera.TipoTasa === 'CURVA' || mCAT.dataCabecera.TipoTasa === 'PUNTO' ? mCAT.dataCabecera.Nombre : mCAT.dataCabecera.Cuenta;
				parametrosSP.p_Fuente		= mCAT.dataCabecera.Servicio;
				parametrosSP.p_Rut			= mCAT.dataCabecera.Rut;
				parametrosSP.p_Fecha		= mCAT.dataCabecera.Fecha;
				parametrosSP.p_Hora			= mCAT.dataCabecera.Hora;
				parametrosSP.p_TipoArchivo	= mCAT.dataCabecera.TipoArchivo;
				parametrosSP.p_DataTasas	= mCAT.dataDetalle;
				
				bcsComunes.ventanaOverlay(mCAT.idVentana,true);
				bcsComunes.ventanaData(mCAT.idVentana, false);
				
				bcsServiciosTasas.cargarArchivoTasas(parametrosSP,function(result){
					var errores = {
					    "ERROR_PARAMETRO_INCORRECTO": $translate.instant("ERROR_PARAMETRO_INCORRECTO"),
					    "ERROR_PARAMETRO_TASA": $translate.instant("ERROR_PARAMETRO_TASA"),
					    "ERROR_PARAMETRO_FUENTE": $translate.instant("ERROR_PARAMETRO_FUENTE"),
					    "ERROR_SELECCIONAR_FUENTE": $translate.instant("ERROR_SELECCIONAR_FUENTE"),
					    "ERROR_DATOS_CARGA_INCORRECTO": $translate.instant("ERROR_DATOS_CARGA_INCORRECTO"),    
					    "ERROR_DATOS_GENERALES_INCORRECTO": $translate.instant("ERROR_DATOS_GENERALES_INCORRECTO"),
					    "ERROR_YA_EXISTEN_TASAS_CARGADAS_PARA_LA_FUENTE": $translate.instant("ERROR_YA_EXISTEN_TASAS_CARGADAS_PARA_LA_FUENTE"),
					    "ERROR_TASAS_ARCHIVO": $translate.instant("ERROR_TASAS_ARCHIVO"),
					    "ERROR_INSTRUMENTOS_DV": $translate.instant("ERROR_INSTRUMENTOS_DV"),
					    "ERROR_DATOS_GENERALES_UPDATE_VACIO": $translate.instant("ERROR_DATOS_GENERALES_UPDATE_VACIO"),
					    "ERROR_TASAS_PUBLICADAS": $translate.instant("ERROR_TASAS_PUBLICADAS"),
					    "ERROR_TASAS_NO_SOLICITADAS": $translate.instant("ERROR_TASAS_NO_SOLICITADAS"),
					    
					};
					
					if (result.P_OUT_ERRORESCONTADOR !== 0) {
					    for (const mensaje of result.P_OUT_MENSAJES) {
					        const resultado = mensaje.codigo;
					        const mensajeError = errores[resultado];
					        if (mensajeError) {
					            bcsComunes.msg("error", $translate.instant("UI_CARGA_ARCHIVO_TASAS"), mensajeError);
					            bcsComunes.ventanaOverlay(mCAT.idVentana, false);
					            bcsComunes.ventanaCerrar(mCAT.idVentana);
					            return;
					        }
					    }
					}else{
						bcsComunes.msg("info",$translate.instant("UI_CARGA_ARCHIVO_TASAS"),$translate.instant("UI_CARGA_ARCHIVO_TASAS_REALIZADA"));
						bcsComunes.ventanaData(mCAT.idVentana, true);
					}
					
					bcsComunes.ventanaOverlay(mCAT.idVentana,false);
					bcsComunes.ventanaCerrar(mCAT.idVentana);
					
				}, function(resError){
					bcsComunes.msg("error","UI_CARGA_ARCHIVO_TASAS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(mCAT.idVentana,false);
				});
	        } else bcsComunes.ventanaOverlay(mCAT.idVentana,false);
		};
		
		
		mCAT.fn.cerrar = function(){
			bcsComunes.ventanaData(mCAT.idVentana,false);
			bcsComunes.ventanaCerrar(mCAT.idVentana);
		};
	
});