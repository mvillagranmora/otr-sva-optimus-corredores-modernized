angular.module('ModuloOrdenes').controller('CargaArchivosOrdenesCtrl',function($scope,$sanitize,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.nombreTipoCarga = $translate.instant("UI_CARGA_ORDENES_CLIENTE");
	$scope.formatoCarga = '<Rut><Portafolio><TipoOperacion><C/V><MecanismoCaptacion><Cantidad><Nemotecnico><Precio><IdCondicionDeLiquidacion>'
		+ '<TipoVigencia><D/P><FechaVigencia><IdFormaCustodia><C/N><IdBolsa><IdBoveda>'
	$scope.separador = "Separador <TAB>";
	$scope.ejemploCarga = '4444-K;0;C;OE;100;ENTEL;' 
			+		'6601.12;'
			+		'PH;'
			+		'D;'
			+		'2020-06-01;'
			+		'C;'
			+		'BCS;'
			+		'DCV'
	
	$scope.cargarArchivo = function() {
		var datosArchivo = {codigoArchivo: 'CARGA_INGRESO_ORDEN_MASIVO', titulo: 'Carga de Ordenes'};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
		var promesa = bcsComunes.ventanaPromesa(id1);
		var datos = [];
		var error = 0;
		promesa.then(function(data) {
			if (!data || !data.fileData || !data.fileData[0]){
				return;
			}

			for(value of data.fileData){
				if(value.MENSAJEVALIDACION !== "OK"){
					error =  1;
	
					
				}else{

					
					datos.push({
						Rut      					:value.RUT,
						Portafolio					:value.PORTAFOLIO,
						TipoOperacion				:value.OPERACION,   
						MecanismoCaptacion			:value.MECANISMO_CAPTACION,
						Cantidad					:value.CANTIDAD,
						Nemotecnico					:value.NEMOTECNICO,
						Precio						:value.PRECIO,
						IdCondicionDeLiquidacion	:value.CONDICIONLIQUIDACION,
						TipoVigencia				:value.TIPO_VIGENCIA,
						FechaVigencia				:value.FECHA_VIGENCIA,
						IdFormaCustodia				:value.OPERA_CUSTODIA,
						IdBolsa						:value.BOLSA,
						IdBoveda					:value.BOVEDA

					});
				}
			};
			
			var parametros = {}
			parametros.p_IdEmpresa = $scope.SesionIdEmpresa,
			parametros.p_IdUsuario = $scope.SesionIdUsuario,
			parametros.p_IdModulo	= "ORD",
			parametros.p_simularOperacion = "N",
			parametros.p_UsaReglasNegocio = "S",
			parametros.p_ArchivoEntrada		  = datos,
			
			console.log('DATOS: ' + JSON.stringify(parametros));
		
			if (error !== 0){
					bcsComunes.ventanaMsg({
				            mensaje:$translate.instant("Debe cargar nuevamente el archivo, Con el Formato correcto"),
					            conflictos:util.parseServerResponse()
			                },$scope.idVentana);
			}else{
				bcsComunes.ventanaOverlay($scope.idVentana,true);
				
				promise = bcsServicioOrden.CrearOrdenesMasiva(parametros).$promise; 
				
				promise.then(function(result) {
					console.log('RESULT: ' + JSON.stringify(result));
					bcsComunes.ventanaOverlay($scope.idVentana,false);
						if (result.P_OUT_ERRORESCONTADOR > 0) {
						bcsComunes.ventanaMsg({
					            mensaje:$translate.instant("Error: Debe cargar nuevamente el archivo, error en el procesamiento del archivo"),
					            conflictos:util.parseServerResponse(result)
				                },$scope.idVentana);
							bcsComunes.ventanaAbrir('VentanaResultado',{ 
								usaNuevosMensajes : true ,
								titulo: $translate.instant('Procesamiento Archivo'), 
								campoSecuencia : "id" ,
								nombreSecuencia : $translate.instant('UI_ORDENES'),
								datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
			     			bcsComunes.ventanaOverlay($scope.idVentana,false);
							console.log(result);
					}else{							
	     			bcsComunes.ventanaAbrir('VentanaResultado',{ 
								usaNuevosMensajes : true ,
								titulo: $translate.instant('Procesamiento Archivo'), 
								campoSecuencia : "id" ,
								nombreSecuencia : $translate.instant('UI_ORDENES'),
								datos : result.P_OUT_MENSAJES } ,false,false,$scope.idVentana);
			     			bcsComunes.ventanaOverlay($scope.idVentana,false);					
			console.log(result);
					
					}
				},function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_ERROR_INGRESAR_ORDEN"),
						conflictos:util.parseServerResponse(result)
						},idVentana);
					});
			}
		});

	};
});


