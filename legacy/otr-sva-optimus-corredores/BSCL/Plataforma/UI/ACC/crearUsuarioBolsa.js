angular.module('ModuloPlataforma').controller('CrearUsuarioBolsaCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	$scope.data = {};	
	$scope.configData = {};
	$scope.searchData = {};
	$scope.recordData = {};
	$scope.recordData.indefinido = true;
	$scope.recordData.usuarioCorredores = true;
	$scope.recordData.usuarioNegocios = true;
	$scope.configData.TipoDocumentoIdentidadBloqueado = false;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var idUsuarioBolsa = 0;
	$scope.configData.bloqueoUsuario = false;	//BLOQUEA ZONA PERSONA
	$scope.configData.bloqueoCrearUs = false;  //BLOQUEA BOTON CREAR USUARIO
	$scope.configData.clavesIguales  = true;

	

	



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
		
	};
	
	var dataSourceUsuarioBolsa = new bcs.DataSource({
		type : "odata",
		definition : {
			serverFiltering	: true, 
			serverPaging 	: true, 
			serverSorting	: true,
			transport: {
				read: {
					url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=30000",
					dataType: "json"
				}		
			},
			schema: {
		     	model: {
		     		fields: { Valor : { type: "string"}	}
		     	}
			},
			filter: [{ field: "CodigoParametro"	, operator: "eq", value: 'ACC.IdUsuarioBolsa' },
			         { field: "IdEmpresa"		, operator: "eq", value: $scope.SesionIdEmpresa }]
		}
	});
	
	dataSourceUsuarioBolsa.fetch(function(){
		if(dataSourceUsuarioBolsa.data().length > 0){
			idUsuarioBolsa = parseInt(dataSourceUsuarioBolsa.data()[0].Valor);	
		}
 	});
	
	
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
			p_TipoEntidad:"EMP",
			p_IdEntidad: idUsuarioBolsa,
			p_HeredarRolesUsuario: $scope.recordData.heredarRolesUsuario || -1,
			p_UsuarioCorredores: ($scope.recordData.usuarioCorredores) ? "S" : "N",
		    p_UsuarioNegocios: ($scope.recordData.usuarioNegocios) ? "S" : "N",
			p_UsuarioClientes: "N",
			p_UsuarioServicios: "N",
			p_EsUsuarioBolsa: "S"
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
