angular.module('ModuloOrdenes').controller('CargaArchivosAsignacionCtrl',function($scope,$sanitize,$translate,bcsComunes,bcsServicioOrden,bcsConfiguracionOrdenesV2,bcsConsultasOrdenes)
{
	
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.nombreTipoCarga = $translate.instant("UI_CARGA_ASIGNACIONES_CLIENTE");
	$scope.formatoCarga = '<FolioTran><FechaTran><Nemo><C/V><TipoDocIdentidad><DocIdentidad><CodPortafolio><Cantidad>';
	$scope.separador = "Separador <TAB>";
	$scope.ejemploCarga = '500	2017-06-06	CCU	C	R	123456-0	1	1';
	$scope.cargarArchivo = function() {
		var datosArchivo = {codigoArchivo: 'ASIGNACION_CLIENTE', titulo: 'Carga de Asignaciones'};
		var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,false,false,idVentana);
		bcsComunes.ventanaPromesa(id1).then(function(result){
			console.log(result);
			if (!result || !result.fileData)
				return;
			var asignaciones = [];
			var i = 0;
			$.each(result.fileData,function(key,value){
				value.IdInterno = i++;
				value.Cantidad = Number(value.Cantidad);
				asignaciones.push(value);
			})
			var datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario : $scope.SesionIdUsuario,
					p_IdModulo	: "ORD",
					p_Asignaciones : result.fileData			
			}
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServicioOrden.CargaAsignacionesPostreras(datos).$promise.then(function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				if (result.P_OUT_ERRORESCONTADOR > 0) {
					bcsComunes.ventanaAbrir('VentanaResultado',{ usaNuevosMensajes : true ,titulo: 'UI_RESULTADO_CREAR_ASIGNACIONES', 
						campoSecuencia : "id" ,
						nombreSecuencia : 'UI_LINEA',
						datos : result.P_OUT_MENSAJES } ,false,false,idVentana);
				}
				else {
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(result)
					},idVentana);
				}
				
				
			},function(result){
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_AL_CREAR_ASIGNACIONES"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			})
			
			
		},function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			console.log(result);
		})
	}
	
});


