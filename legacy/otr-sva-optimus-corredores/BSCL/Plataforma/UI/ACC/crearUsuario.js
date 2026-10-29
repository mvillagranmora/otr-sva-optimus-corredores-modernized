angular.module('ModuloPlataforma').controller('CrearUsuarioCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.searchData = {};
	$scope.recordData = {};
	$scope.configData.TipoDocumentoIdentidadBloqueado = false;
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.configData.bloqueoUsuario = true;	//BLOQUEA ZONA PERSONA
	$scope.configData.bloqueoCrearUs = false;  //BLOQUEA BOTON CREAR USUARIO
	$scope.configData.clavesIguales  = true;
	$scope.configData.bloqueoUsuarioCli = true;		
	
	//watch que permite asignar dinamicamente un filtro al combobox-socio-negocio
	$scope.$watch('data.oTipoSocioNegocio.idActual',function(nval,oval) {
		//Eliminar los filtros anteriores
		delete $scope.data.oSocioNegocio.filtros.IdEmpleado;
		delete $scope.data.oSocioNegocio.filtros.IdPersona;
		delete $scope.data.oSocioNegocio.filtros.IdCliente;
		delete $scope.data.oSocioNegocio.filtros.EstadoCliente;
		delete $scope.data.oSocioNegocio.filtros.EstadoEmpleado;
		delete $scope.data.oSocioNegocio.filtros.EstadoPersona;
		
		if (nval=='CLI') {
			$scope.data.oSocioNegocio.filtros.IdCliente      = {"field": "IdCliente","operator": "gte", "value"   : 1 };
			$scope.data.oSocioNegocio.filtros.EstadoCliente = {"field": "EstadoCliente", "operator": "eq", "value": 'ACT' };
		}
		else if (nval=='EMP') {
			$scope.data.oSocioNegocio.filtros.IdEmpleado     = {"field": "IdEmpleado","operator": "gte", "value"   : 1 };
			$scope.data.oSocioNegocio.filtros.EstadoEmpleado = {"field": "EstadoEmpleado", "operator": "eq", "value": 'ACT' };
		}
		else if (nval=='PER') {
			$scope.data.oSocioNegocio.filtros.IdPersona    = {"field": "IdPersona","operator": "gte", "value"   : 1 };
			$scope.data.oSocioNegocio.filtros.EstadoPersona = {"field": "EstadoPersona", "operator": "eq", "value": 'ACT' };
/*		}else {
			$scope.data.oSocioNegocio.filtros.IdPersona     = {"field": "IdPersona","operator": "eq", "value"   : -1 };*/
			
		}
		$scope.configData.bloqueoUsuario    = true;//si cambia el socio de negocio se bloquea el formulario de creacion.
		$scope.configData.bloqueoUsuarioCli = true;//si cambia el socio de negocio se bloquea el formulario de creacion.
	});
	
	$scope.$watch('data.oSocioNegocio.idActual',function(nval,oval) {
		$scope.configData.bloqueoUsuario 	= true;	//si cambia el socio de negocio se bloquea el formulario de creacion.
		$scope.configData.bloqueoUsuarioCli = true;//si cambia el socio de negocio se bloquea el formulario de creacion.
	});
	
	$scope.data.oSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		idActual : false,
		seleccionado : false,
		filtros: {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa }/*,
			EstadoEmpleado : {field: "EstadoEmpleado", operator: "eq", value: 'ACT' }*/
		},
	};
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
	
	$scope.$watch('recordData.confirmacionClave', function(nval,oval){
		if(nval!=$scope.recordData.clave){
			$scope.configData.clavesIguales = false;
		}else{
			$scope.configData.clavesIguales = true;
		}
	});
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
	};
	
	$scope.limpiarPantalla = function() {
		document.formCrearUsuario.reset();
		$scope.configData.bloqueoUsuario    = false;
		$scope.configData.bloqueoCrearUs    = false;
		$scope.configData.bloqueoUsuarioCli = false;
		
	};
	
	
	var UsuarioDataSource = new kendo.data.DataSource({
		type: "odata",
		serverFiltering:true,
		transport: {
			read: {
				url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=384",
				dataType: "json"
		    }
		},
		schema: {
        	model:
    		{
        		fields : {
	            	IdUsuario: {type: "string"},
	            	IdEmpresa: {type: "string"},
	            	CodigoUsuario: {type: "string"},
	            	NombreCompleto: {type: "string"},
	            	RazonSocial: {type: "string"},
	            	TipoDocumentoIdentidad: {type: "string"},
	            	DocumentoIdentidad: {type: "string"},
	            	TipoEntidad: {type: "string"},
	            	NombreTipoSocioDeNegocio: {type: "string"},
	            	FechaIngreso: {type: "date"},
	            	FechaVigencia: {type: "date"},
	            	FechaUltimaConexion: {type: "date"},
	            	DiasVencimientoClave: {type: "string"},
	            	Conectado: {type: "string"},
	            	Hora: {type: "date"},
	            	Terminal: {type: "string"},
	            	IdEstado: {type: "string"},
	            	DescripcionEstado: {type: "string"},
	            	IntentosConexion: {type: "string"},
	            	FechaBloqueoTemporal: {type: "date"}
				}
    		}
    	}
	});
	
	$scope.configData.buscarUsuario = function() {

		var filterCli = new Array();
		filterCli.push({ field: "IdEmpresa", operator: "eq", value: Number($scope.SesionIdEmpresa) });
		filterCli.push({ field: "IdEntidad", operator: "eq", value: Number(getIdEntidad($scope.data.oTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio)) });
		filterCli.push({ field: "TipoEntidad", operator: "eq", value: $scope.data.oTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio });

		UsuarioDataSource.query({filter:filterCli}).then(function() {
			if(UsuarioDataSource.data().length > 0){
				
				bcsComunes.msg("error",'Error',$translate.instant("UI_SOCIO_ASOCIADO_A_USUARIO"));
				$scope.configData.bloqueoUsuario 	= true;
				$scope.configData.bloqueoUsuarioCli = true; 
				return false;
			}else {

				switch ($scope.data.oTipoSocioNegocio.idActual) 
		        {
		            case 'CLI': $scope.configData.bloqueoUsuarioCli = false;
								$scope.configData.bloqueoUsuario 	= true;
								$scope.recordData.usuarioClientes   = true;
		                     break;
		            case 'PER': $scope.configData.bloqueoUsuarioCli = false;
								$scope.configData.bloqueoUsuario 	= true;
								$scope.recordData.usuarioClientes   = true;
								break;
		            default: $scope.configData.bloqueoUsuarioCli 	= true;
							 $scope.configData.bloqueoUsuario 		= false;
		                     break;
		        }             
				
				$scope.recordData.idEmpresa		=$scope.SesionIdEmpresa;
				$scope.recordData.idEntidad 	= getIdEntidad($scope.data.oTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio);
				$scope.recordData.TipoEntidad 	= $scope.data.oTipoSocioNegocio.seleccionado.IdTipoSocioDeNegocio;
				bcsComunes.msg("info",'Info',$translate.instant("UI_SOCIO_OK_INGERESAR"));
			}
			
		});
	};
	
	function getIdEntidad(nval){
		if (nval=='CLI') {
			return $scope.data.oSocioNegocio.seleccionado.IdCliente;
		}else if (nval=='EMP') {
			return $scope.data.oSocioNegocio.seleccionado.IdEmpleado;			
		}else {
			return $scope.data.oSocioNegocio.seleccionado.IdPersona;					
		}
	}
	
	
	$scope.crearUsuario = function(){
		bcsComunes.ventanaOverlay(idVentana, true);
		var urlCrearUsuario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=112";
		var r = $resource("", {},
	    {
			'ConsultarEntidadEnrolamiento': { method: "POST", url: urlCrearUsuario}
		});
		
		var datosCreacion = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario: $scope.SesionIdUsuario,
			p_IdModulo: 'PLA',
			p_CodigoUsuario: $scope.recordData.nombreUsuario,
			p_Clave: CryptoJS.SHA256($scope.recordData.clave).toString(),
			//p_FechaIngreso: mxment().format("YYYY-MM-DD"),
			p_FechaIngreso:$scope.SesionFechaSistema, //Check
			//p_FechaVigencia: mxment($scope.recordData.fechaVigencia).format("YYYY-MM-DD"),
			p_FechaVigencia: (!$scope.recordData.indefinido) ? util.dateSPdate($scope.recordData.fechaVigencia) : "0001-01-01",
			//p_FechaUltimaConexion: mxment().format("YYYY-MM-DD"),
			p_FechaUltimaConexion: $scope.SesionFechaSistema,
			p_Conectado:'N',
			//p_Hora:mxment().format("HH:mm:ss"),
			p_Hora: util.date(new Date()).format("HH:mm:ss"),
			p_Terminal:'Terminal Nro',
			p_IdEstado:$scope.recordData.estado,
			p_IntentosConexion:0,
			//p_FechaBloqueoTempotal:mxment(), // ???
			p_FechaBLoqueoTempotal : util.dateSPseconddate(new Date()),
			p_TipoEntidad:$scope.recordData.TipoEntidad,
			p_IdEntidad:$scope.recordData.idEntidad,
			p_HeredarRolesUsuario: $scope.recordData.heredarRolesUsuario || -1,

			p_UsuarioCorredores: ($scope.recordData.usuarioCorredores) ? "S" : "N",
			p_UsuarioNegocios: ($scope.recordData.usuarioNegocios) ? "S" : "N",
			p_UsuarioClientes: ($scope.recordData.usuarioClientes) ? "S" : "N",
			p_UsuarioServicios: ($scope.recordData.usuarioServicios) ? "S" : "N"
		};
		
		var post = r.ConsultarEntidadEnrolamiento(datosCreacion);
		
		post.$promise.then(function(result){
			
			var idModalMensaje = bcsComunes.ventanaMsg({
					mensaje: (result.P_OUT_NEW_USER_ID>0)?$translate.instant("UI_USUARIO_CREADO_OK") : $translate.instant("UI_ERROR"),
					conflictos:util.parseServerResponse(result)
				},idVentana);
			bcsComunes.ventanaPromesa(idModalMensaje).then(function(){
				$scope.configData.bloqueoCrearUs = false;
				$scope.limpiarPantalla();
				bcsComunes.ventanaOverlay(idVentana, false);
				bcsComunes.ventanaCerrar(idVentana);
			});
		},function(error){
			$scope.configData.bloqueoCrearUs = false;
			bcsComunes.msg("error",'ERROR',$translate.instant("UI_INTERNAL_ERROR"));	
			bcsComunes.ventanaOverlay(idVentana, false);
		});
	};
	
});
