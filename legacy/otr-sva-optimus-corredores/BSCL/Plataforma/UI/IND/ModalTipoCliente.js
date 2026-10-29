/*************************************************************
 * Purpose	: Modal Dirección Electrónica.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('ModalTipoCliente',
	function($scope,$translate,servicioSociodeNegocio,bcsComunes,$resource) {
		var mtc = this;
		mtc.TipoCliente = {};
		mtc.TipoClienteOriginal = {};

		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		
		mtc.modeloDefault = parametros.modelo;
		if (!!mtc.modeloDefault)
		{
			//modificacion
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_MODIFICAR_MODELO_TIPO_CLIENTE'));
			mtc.TipoCliente.ModeloTipoCliente = mtc.modeloDefault.Nombre;
			mtc.TipoClienteOriginal.IdEstado =  mtc.modeloDefault.IdEstado;
			mtc.TipoClienteOriginal.TipoEnrolamiento =  mtc.modeloDefault.TipoEnrolamiento;
			mtc.TipoCliente.PorDefecto = mtc.modeloDefault.PorDefecto === 'S' ? true:false;
		}
		else
		{
			bcsComunes.ventanaTitulo(idVentana, $translate.instant('UI_AGREGAR_MODELO_TIPO_CLIENTE'));
		}
		
		mtc.cmbbxTipoCliente = {
			filtros : {
				
			}
		};
		 
		mtc.aceptar = function() {
			//modificacion
			if (!!mtc.modeloDefault){
				
				var urlCrearTipoCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100017";
				var r = $resource("", {},
			    {
					'CrearTipoCliente': { method: "POST", url: urlCrearTipoCliente}
				});

				const estadoPorDefecto =  mtc.TipoCliente.PorDefecto ? 'S' : 'N';
				const datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'SOC',
					p_IdTipoCliente : mtc.modeloDefault.IdTipoCliente,
					p_Nombre: mtc.TipoCliente.ModeloTipoCliente,   
					p_IdEstado: mtc.TipoCliente.IdEstado,
					p_TipoEnrolamiento: mtc.TipoCliente.TipoEnrolamiento,
					p_PorDefecto: estadoPorDefecto
				};

				bcsComunes.ventanaOverlay(idVentana,true);
				
				var post = r.CrearTipoCliente(datos);
				post.$promise.then(function(result){

					var mensaje = "Modelo Tipo Cliente Modificado Correctamente.";
					if(result.P_OUT_ERRORESCONTADOR>0){
						mensaje = "Error";
					}

					console.log(result);
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : mensaje,
						conflictos : util.parseServerResponse(result)}, idVentana);

					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);	
					});
						
				},
				function(result){
					console.log(result);
					bcsComunes.msg("error","error","Error al modificar Modelo Tipo Cliente");
					bcsComunes.ventanaData(idVentana,true);
					bcsComunes.ventanaCerrar(idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				});
				
			}
			//creacion
			else {
				
				var urlCrearTipoCliente = "/BSCL/Plataforma/GWJAVA/PLA/call?t=100016";
				var r = $resource("", {},
			    {
					'CrearTipoCliente': { method: "POST", url: urlCrearTipoCliente}
				});
				
				const estadoPorDefecto =  mtc.TipoCliente.PorDefecto ? 'S' : 'N';
				const datos = {
					p_IdEmpresa : $scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdModulo :'PLA',
					p_UsaReglasNegocio :'S',
					p_IdTipoCliente : mtc.TipoCliente.ModeloTipoCliente.replace(" ", "_"),
					p_Nombre: mtc.TipoCliente.ModeloTipoCliente,
					p_Nombre: mtc.TipoCliente.ModeloTipoCliente,   
					p_IdEstado: mtc.TipoCliente.IdEstado,
					p_TipoEnrolamiento: mtc.TipoCliente.TipoEnrolamiento,
					p_PorDefecto: estadoPorDefecto
				};
				
				bcsComunes.ventanaOverlay(idVentana,true);
				
				var post = r.CrearTipoCliente(datos);
				post.$promise.then(function(result){
					
					var mensaje = "Modelo Tipo Cliente Ingresado Correctamente.";
					if(result.P_OUT_ERRORESCONTADOR>0){
						mensaje = "Error";
					}
					
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : mensaje,
						conflictos : util.parseServerResponse(result)}, idVentana);
					
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaData(idVentana,true);
						bcsComunes.ventanaCerrar(idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);	
					});
				},
				function(result){
					console.log(result);
					bcsComunes.msg("error","error","Error al agregar modelo tipo cliente");
					bcsComunes.ventanaData(idVentana,false);
					bcsComunes.ventanaCerrar(idVentana);
					bcsComunes.ventanaOverlay(idVentana,false);
				});
			}
		};
		
		mtc.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
		
	});
}());
//------------------------- FIN CONTROLLER --------------------------