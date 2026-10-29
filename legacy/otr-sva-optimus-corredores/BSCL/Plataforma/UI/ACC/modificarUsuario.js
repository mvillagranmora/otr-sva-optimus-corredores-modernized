angular.module('ModuloPlataforma').controller('ModificarUsuarioCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.recordData = {};
	$scope.configData.clavesIguales = true;
	$scope.configData.bloqueoUsuario = false;
	$scope.configData.bloqueoUsuarioClientes = true;
	$scope.winID = bcsComunes.ventanaWinId($scope);
	
	//Parametros del usuario.
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	bcsComunes.ventanaAutoHeight($scope.winID);
	
	//si la fechaBloqueoTemporal es igual a la default o TieneBloqueoTemporal = 'N', no tiene bloqueo; caso contrario, bloqueado.
	$scope.configData.usuarioBloqueadoTemporal = (util.dateisdefault(parametros.FechaBloqueoTemporal) || parametros.TieneBloqueoTemporal==='N')? false: true;
	
	$scope.recordData.nombreUsuario = parametros.CodigoUsuario;
	$scope.recordData.fechaVigencia = util.dateisdefault(parametros.FechaVigencia)? "" : parametros.FechaVigencia;
	$scope.recordData.indefinido = util.dateisdefault(parametros.FechaVigencia)? true : false;
	$scope.recordData.idEstado = (parametros.IdEstado==="ACT")?'1':'2';

	$scope.recordData.usuarioCorredores = (parametros.UsuarioCorredores==="S") ? true : false;
	$scope.recordData.usuarioNegocios = (parametros.UsuarioNegocios==="S") ? true : false;
	$scope.recordData.usuarioClientes = (parametros.UsuarioClientes==="S") ? true : false;
	$scope.recordData.usuarioServicios = (parametros.UsuarioServicios==="S") ? true : false;
	
	$scope.configData.TipoEntidad = parametros.TipoEntidad;
	
	$scope.configData.bloqueoUsuarioClientes=$scope.configData.TipoEntidad==='CLI'?true:!$scope.recordData.usuarioClientes;
	$scope.configData.bloqueoUsuario=$scope.configData.TipoEntidad==='CLI'?true:$scope.configData.bloqueoUsuario;
	
	$scope.recordData.idTipoBloqueo = parametros.IdTipoBloqueo || '';
	$scope.recordData.comentarioPorDefecto = parametros.ComentarioBloqueo || '';
	
	$scope.configData.bloqueoBtnModUs = false; //BLOQUEA BOTON MODIFICAR USUARIO
	
	$scope.configData.clavesIguales = true;
	$scope.$watch('recordData.confirmacionClave', function(nval,oval){
		if(nval!=$scope.recordData.clave){
			$scope.configData.clavesIguales = false;
		}else{
			$scope.configData.clavesIguales = true;
		}
	});
	$scope.$watch('recordData.clave', function(nval,oval){
		if($scope.recordData.confirmacionClave || nval!=$scope.recordData.confirmacionClave){
			$scope.configData.clavesIguales = false;
		}else{
			$scope.configData.clavesIguales = true;
		}
	});
	$scope.data.oHeredarRolesUsuario={
		tipo : "combobox-usuario",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa },
			EstadoEmpleado : {field: "IdEstado", operator: "eq", value: 'ACT' }
		},
	};

	$scope.data.BloqueoSocioDeNegocio={
		tipo : "combobox-tipo-bloqueo-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdTipoEntidad : {field: "IdTipoEntidad", operator: "eq", value: 'USUARIO'}
		},
	};
	var UsuarioEntidadDataSource = new bcs.DataSource(
		{
			type : "odata",
			definition : {
				serverFiltering : true,
				transport : {
					read : {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1355",
						dataType : "json"
					}
				},

				schema: {
		        	model:
		    		{
		        		fields : {
			            	EsPersona: {type: "string"},
			            	EsCliente: {type: "string"},
			            	EsProveedor: {type: "string"},
			            	EsEmpleado: {type: "string"}
						}
		    		}
		    	}
			}
		});
	
	var filterCli = [];
	filterCli.push({
		field : "DocumentoIdentidadDefecto",
		operator : "eq",
		value : parametros.CodigoUsuario
	});
	UsuarioEntidadDataSource
			.query({
				filter : filterCli
			})
			.then(function() {
						if (UsuarioEntidadDataSource.view().length > 0) {
							var datos = UsuarioEntidadDataSource
									.data();
							if (datos[0]) {
								$scope.configData.bloqueoUsuarioClientes = datos[0].EsCliente ==='UI_SI'?false:true;
								$scope.configData.bloqueoUsuario = datos[0].EsEmpleado ==='UI_SI'?false:true;
							}
						}
					});
	
	$scope.modificarUsuario = function(){
		if($scope.configData.bloqueoBtnModUs ==false){
			bcsComunes.ventanaOverlay(idVentana, true);

			$scope.configData.bloqueoBtnModUs=true;
			
			var urlModificarUsuario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=159";
			var r = $resource("", {},
		    {
				'CallModificarUsuario': { method: "POST", url: urlModificarUsuario}
			});
			
			var datosModificacion= {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario: $scope.SesionIdUsuario,
				p_IdModulo: 'PLA',
				//Datos Actualizacion
				p_CodigoUsuario: $scope.recordData.nombreUsuario,
				p_Clave: (($scope.recordData.clave && $scope.recordData.clave.trim().length >0)? CryptoJS.SHA256($scope.recordData.clave).toString() : ''),
				p_HeredarRolesUsuario: $scope.recordData.heredarRolesUsuario || -1,
				p_FechaVigencia: (!$scope.recordData.indefinido)?util.dateSPdate($scope.recordData.fechaVigencia) : "0001-01-01",
				p_IdEstado:$scope.recordData.estado,
				p_UsuarioCorredores: ($scope.recordData.usuarioCorredores) ? "S" : "N",
				p_UsuarioNegocios: ($scope.recordData.usuarioNegocios) ? "S" : "N",
				p_UsuarioClientes: ($scope.recordData.usuarioClientes) ? "S" : "N",
				p_UsuarioServicios: ($scope.recordData.usuarioServicios) ? "S" : "N",
				p_IdTipoBloqueo: ($scope.recordData.estado === "ACT") ? "" : $scope.recordData.tipoBloqueoSeleccionado.CodigoTipoBloqueo,
    			p_ComentarioBloqueo: ($scope.recordData.estado === "ACT") ? "" : $scope.recordData.comentarioPorDefecto,
				p_NombreTipoBloqueo: ($scope.recordData.estado === "ACT") ? "" : $scope.recordData.tipoBloqueoSeleccionado.NombreTipoBloqueo
			};
			
			var post = r.CallModificarUsuario(datosModificacion);
			
			post.$promise.then(function(result){
				var titulo = "UI_USUARIO_ACTUALIZADO_OK";
				if(result.P_OUT_ERRORESCONTADOR>0){
					titulo = "UI_OPERACION_CON_ERRORES";
				}
				var idModalMensaje = bcsComunes.ventanaMsg({
					mensaje: $translate.instant(titulo),
					conflictos:util.parseServerResponse(result)
					},idVentana);
				bcsComunes.ventanaPromesa(idModalMensaje).then(function(){
					$scope.recordData.clave = '';
					$scope.recordData.confirmacionClave = '';
					$scope.configData.bloqueoBtnModUs = false;
					$scope.configData.bloqueoBtnModUs = false;
					bcsComunes.ventanaOverlay(idVentana, false);
					bcsComunes.ventanaCerrar(idVentana);
				});
			},function(){
				bcsComunes.msg("error",'ERROR',$translate.instant("UI_INTERNAL_ERROR"));	
				bcsComunes.ventanaOverlay(idVentana, false);
			});
			
			
		}
	};
	
	$scope.eliminarBloqueoTemporal = function(){
		
		var urlDesbloqueoTemporalUsuario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=160";
		var r = $resource("", {},
	    {
			'CallDesbloqueoUsuarioTemporal': { method: "POST", url: urlDesbloqueoTemporalUsuario}
		});
		
		var datosDesbloqueoUsuarioTemporal= {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			//Datos Actualizacion
			p_CodigoUsuario: $scope.recordData.nombreUsuario
		};
		
		var post = r.CallDesbloqueoUsuarioTemporal(datosDesbloqueoUsuarioTemporal);
		
		post.$promise.then(function(result){
			if(result.P_OUT_DESBLOQUEO!=null && result.P_OUT_DESBLOQUEO ==="ok++"){
				bcsComunes.msg("info",'Info',$translate.instant("UI_USUARIO_DESBLOQUEADO_OK"));
				
				$scope.configData.usuarioBloqueadoTemporal = false;
			}else{
				for(var i in result.P_OUT_ERRORESDETALLE){
					bcsComunes.msg("error",'Error',$translate.instant(result.P_OUT_ERRORESDETALLE[i].Codigo));	
				}
			}
		});
	}
	
	$scope.$watch('recordData.tipoBloqueoSeleccionado', function(newValue, oldValue) {
		if (!newValue?.ComentarioPorDefecto) return;
    	const comentario = $scope.recordData.comentarioPorDefecto || '';
    	const actualizarComentario = !comentario.trim() || (oldValue && comentario.trim() === oldValue.ComentarioPorDefecto) || (oldValue && newValue.CodigoTipoBloqueo !== oldValue.CodigoTipoBloqueo);
    	if (actualizarComentario) $scope.recordData.comentarioPorDefecto = newValue.ComentarioPorDefecto;
	}
);});