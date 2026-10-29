angular.module('ModuloOrdenes').controller('DetalleInstruccionCtrl',
		function ($scope,bcsComunes,bcsConfiguracionOrdenes,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes,$timeout,$translate,bcsServicioOrden) {

		$scope.datos = {};
		$scope.formData={};
		$scope.confGridArchivos= {};
		$scope.encoded = "";
		$scope.datosGrillaDocumentos = [];
		$scope.enabledDescarga = false;
		$scope.binaryString = "";
		$scope.formData= {
			tirTasaOptions: {min:-999999999999999999999}
		};
		const idVentana = bcsComunes.ventanaWinId($scope);
		
		$scope.parametros = bcsComunes.ventanaParams(bcsComunes.ventanaWinId($scope));
		if (!$scope.parametros.IdInstruccion){
			bcsComunes.msg("error",'Error',$translate.instant("UI_INSTRUCCION_NO_EXISTE"));
		}
		else {
			bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_INFORMACION_INSTRUCCION") + ' ' + $scope.parametros.IdInstruccion);
			const instruccionDatasource = new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url: bcsConsultasOrdenes('ConsultarInstruccion'),
							dataType: "json"
						}
					},
	    		    filter: { field: "IdInstruccion", operator: "eq", value: Number($scope.parametros.IdInstruccion) },
	    		    schema: bcsConfiguracionOrdenesV2.getSchemaInstruccion()
	    		}});
		
			instruccionDatasource.fetch().then(function(){
				if (instruccionDatasource.data().length!=1) {
					bcsComunes.msg("error",'Error',$translate.instant("UI_INSTRUCCION_NO_EXISTE"));
					return false;
				}
				else {
					$scope.instruccion = instruccionDatasource.data()[0];
					$scope.instruccion.DecimalesCantidad = util.bcs_formato(0,'GEN','Cantidad',$scope.instruccion.IdMonedaLiquidacion,'','').cantidadDecimales;
					$scope.instruccion.DecimalesMonto = util.bcs_formato(0,'GEN','Monto',$scope.instruccion.IdMonedaLiquidacion,'','').cantidadDecimales;
					$scope.instruccion.DecimalesTasa = util.bcs_formato(0,'GEN','Tasa',$scope.instruccion.IdMonedaLiquidacion,'','').cantidadDecimales;
					$scope.instruccion.DecimalesPrecio = util.bcs_formato(0,'GEN','Precio',$scope.instruccion.IdMonedaLiquidacion,'','').cantidadDecimales;
					$timeout(function(){bcsComunes.ventanaTamanoAutomatico(bcsComunes.ventanaWinId($scope));},20);

					return true;
				}

			},function(result){
				console.log(result);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
			
		}
		
		$scope.consultarDataSourceDocumentos = function (){
			$scope.datosGrillaDocumentos = bcsConsultasOrdenes('ConsultarDocumentoInstruccion');
		};
		
		$scope.configuracionMecanismoCaptacion = {
			filtros: {
				IdPortafolio : { "field"   : "CodigoTipoIndicador","operator": "eq", "value"   :  "ORDMODALIDAD" }
			},
		};

		$scope.confGridArchivos = {
			dataSource : new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering 	: false,
					serverPaging 		: true,
					serverSorting 		: true,
					groupable 			: true, 
					sort 				: { field: "FechaActualizacion", dir: "asc" },
					pageSize 			: 25,
					transport : {
	                    read : {
	                        url: bcsConsultasOrdenes('ConsultarDocumentoInstruccion'),
	                        dataType: "json"
	                    }
	                },
					filter: [
					    { field: "IdInstruccion", operator: "eq", value: Number($scope.parametros.IdInstruccion) },
					    { field: "Borrado", operator: "eq", value: 'N' }
					],
					schema : bcsConfiguracionOrdenesV2.getSchemaDocumentoInstruccion()
				}
			}),
			columns : [
				{field: "NombreDocumentoAdjunto",title: "UI_NOMBRE_ARCHIVO",width: "150px"},
				{field: "Descripcion",title: "UI_DESCRIPCION",width: "150px"},
				{field: "FechaActualizacion",title: "UI_FECHA_ACTUALIZACION", width: "100px"}
				
			],
			
			sortable			: false,
			pageable			: false,
			filterable			: false,
			groupable           : false,
			autoBind			: true,
			height				: 120,
			width				: 820,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
			selectable 			: "row"
		};	
		
		$scope.subirArchivo = function (){
			const file = document.getElementById("file").files[0];
			
			if(file===undefined){
				bcsComunes.msg("error",'Error',$translate.instant("UI_ARCHIVO_NO_SELECCIONADO"));
				return;
			}
			
			var promise = encodeArchivoBase64(file);
			
			promise.then(function(result){
				let datosIngreso= {
					p_IdEmpresa : $scope.SesionIdEmpresa, 
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'ORD',
					p_UsaReglasNegocio : 'S',
					p_IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					p_DocumentoInstruccion : {
						IdInstruccion:Number($scope.parametros.IdInstruccion),
						NombreDocumentoAdjunto : file.name ,
						Descripcion : $scope.formData.DescripcionDocumento || "Documento Ingresado sin Descripcion",
						Adjunto: result
			    	}
				};
				if(file!=undefined){
					bcsComunes.ventanaOverlay(idVentana,true);
					upload(datosIngreso);
				}
			});
		};
		
		let encodeArchivoBase64 = function (file){
			return new Promise(function (resolve,reject){
				const reader = new FileReader();
				reader.onload = function() { resolve(btoa(reader.result)); };
				reader.onerror = reject;
				reader.readAsDataURL(file);																																																																																																																																																																																																																																																																																																																																																																																																								
			});
		};
		
		let upload = function(formData){
			bcsServicioOrden.AdjuntarDocumentoInstruccion(formData).$promise.then(function (result){
				if(result.P_OUT_ERRORESCONTADOR === 0 && result.P_OUT_REFID.size >0){
					bcsComunes.msg("info",'Info',$translate.instant("UI_DOCUMENTO_INSTRUCCION_SUBIDO_CORRECTAMENTE"));
				}
				bcsComunes.ventanaOverlay(idVentana,false);
				$scope.confGridArchivos.dataSource.read();
				document.getElementById('formSubirArchivo').reset();
			},function (result){
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_SUBIR_DOCUMENTO_INSTRUCCION"));
				document.getElementById('formSubirArchivo').reset();
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		};
		
		$scope.descargarArchivo = function (){
			if($scope.seleccionArchivos){
				let formData = {
					p_IdEmpresa : $scope.SesionIdEmpresa, 
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'ORD',
					p_IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					p_DocumentoInstruccion : {
						IdDocumentoInstruccion: Number($scope.seleccionArchivos.IdDocumentoInstruccion),
		    		}
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioOrden.ObtenerDocumentoInstruccion(formData).$promise.then(function (result){
					if(result.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.msg("info",'Info',$translate.instant("UI_DOCUMENTO_INSTRUCCION_DESCARGADO_CORRECTAMENTE"));
					}
					const encodedFile = result.encoded;
					const decodedFile = atob(encodedFile);
					let file = decodeFile(decodedFile,$scope.seleccionArchivos.NombreDocumentoAdjunto);

					download(file);
					$scope.binaryString="";
					bcsComunes.ventanaOverlay(idVentana,false);
				},function (result){
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_DESCARGAR_DOCUMENTO_INSTRUCCION"));
					bcsComunes.ventanaOverlay(idVentana,false);
				});
			}
			
		};
		
		let decodeFile = function (dataurl, filename) {
 
	        var arr = dataurl.split(','),
            	mime = arr[0].match(/:(.*?);/)[1],
            	bstr = atob(arr[1]), 
            	n = bstr.length, 
            	u8arr = new Uint8Array(n);
		$scope.binaryString = arr[1];
			
	        while(n--){
	            u8arr[n] = bstr.charCodeAt(n);
	        }
	        return new File([u8arr], filename, {type:mime});
		    
		};
		
		let download = function (file){
			var leBlob = util.b64toBlob($scope.binaryString,file.type);
			saveAs(leBlob, file.name);
		};
		
		$scope.borrarArchivo = function (){
			
			if($scope.seleccionArchivos){
				let formData = {
					p_IdEmpresa : $scope.SesionIdEmpresa, 
					p_IdUsuario : $scope.SesionIdUsuario, 
					p_IdModulo : 'ORD',
					p_IdTipoCanalIngreso : $scope.SesionTipoCanalIngreso,
					p_DocumentosInstruccion : [{
							IdDocumentoInstruccion: Number($scope.seleccionArchivos.IdDocumentoInstruccion),
			    		}
					]
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				bcsServicioOrden.BorrarDocumentoInstruccion(formData).$promise.then(function (result){
					if(result.P_OUT_ERRORESCONTADOR === 0){
						bcsComunes.msg("info",'Info',$translate.instant("UI_DOCUMENTO_INSTRUCCION_BORRADO_CORRECTAMENTE"));
					}
					$scope.confGridArchivos.dataSource.read();
					bcsComunes.ventanaOverlay(idVentana,false);
				},function (result){
					bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_BORRAR_DOCUMENTO_INSTRUCCION"));
					bcsComunes.ventanaOverlay(idVentana,false);
				});
			}
		};
		
		$scope.cerrarDetalle = function () {
            		bcsComunes.ventanaCerrar(idVentana);
		};

		bcsComunes.ventanaAutoHeight(idVentana);
		$scope.fileChanged = function(input) {
			$scope.nombreArchivo = '';
			if(input.files[0]){
				$scope.fileName = input.files[0].name;
				if($scope.fileName){
						$scope.nombreArchivo = $scope.fileName;
				}
			}
		};
		let firstHeight = $('#Win_DetalleInstruccion');
		firstHeight.css('height', 'auto');
});
