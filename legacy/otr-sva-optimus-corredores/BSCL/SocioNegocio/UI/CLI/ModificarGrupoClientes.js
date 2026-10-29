angular.module('ModuloSocioNegocio').controller('ModificarGrupoClientesCtrl', 
		function($scope, $translate, servicioSociodeNegocio, $resource, bcsComunes)
{
	const TRANSACCION_INSERT = "C";
	const TRANSACCION_UPDATE = "U";
	const TRANSACCION_DELETE = "D";
	
	//Obtener datos desde la pantalla consulta usuarios
	var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
	var parametros = bcsComunes.ventanaParams(idVentana);
	$scope.datosGrupo	= {};	
	$scope.datosForm	= {};	
	
	if (parametros.Modo == "NUEVO") {
		bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_NUEVO_GRUPO"));
		$scope.datosGrupo.IdGrupo = undefined;
		$scope.datosGrupo.nombreGrupo = undefined;
		$scope.datosGrupo.codigoGrupo = undefined;
		$scope.datosGrupo.IdEstado = "ACT";
	}
	else {
		bcsComunes.ventanaTitulo(idVentana,$translate.instant("UI_EDITAR_GRUPO"));
		$scope.datosGrupo.IdGrupo = parametros.IdGrupo;
		$scope.datosForm.IdEstado = parametros.Estado;
		$scope.datosGrupo.nombreGrupo = parametros.Nombre;
		$scope.datosGrupo.codigoGrupo = parametros.Codigo;
	}
	
	$scope.datosForm.filtrosPortafolio = {
			IdEmpresa : { "field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa }
		};
	
	
	$scope.dsClientesGrupo = new bcs.DataSource({
		type : "odata", 
		definition : {
			serverFiltering : true,
			filter : { field: "IdGrupo", operator: "eq", value: $scope.datosGrupo.IdGrupo},
			serverSorting : true, 
			sort : { field: "DocumentoIdentidad", dir: "ASC" }, 
			transport : {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700004",
					dataType : "json"
				}
			},
			schema : {
				model: {
					fields : {
						DocumentoIdentidad 		: {type: "string"},
						NombreCompleto 			: {type: "string"},
						IdPortafolio 			: {type: "number"}, 
						EstadoPortafolio 		: {type: "string"}, 
						CodigoPortafolio 		: {type: "string"},
						IdMoneda		 		: {type: "string"},
						IdEmpleadoEjecutivo		: {type: "number"}
					}
				}
			}
		}
	});
	
	$scope.confBasicaClientesGrupo = {
		dataSource: new bcs.DataSource({
			type : "array",
			definition : {
				serverSorting : false,
				sort : { field: "DocumentoIdentidad", dir: "ASC" }, // Criterios de Ordenamiento.
				data: [],
				schema : {
					model: {
						fields : {
							DocumentoIdentidad 		: {type: "string"},
							NombreCompleto 			: {type: "string"},
							IdPortafolio 			: {type: "number"}, 
							EstadoPortafolio 		: {type: "string"}, 
							IdMoneda		 		: {type: "string"},
							CodigoPortafolio 		: {type: "string"},
							IdEmpleadoEjecutivo		: {type: "number"}
						}
					}
				}
			}
		}),
		gridCreated: function(grid) {
			$scope.gridApiClientesGrupo = grid;
			var clientes = [];
			var datos = [];
			if (parametros.Modo === "NUEVO" && parametros.clientes && parametros.clientes.length > 0) {
				datos = parametros.clientes;
				$.each(datos,function(key,val){
					clientes.push($.extend({},val));
				})
				$scope.gridApiClientesGrupo.dataSource.data(clientes);
			}
			else if (parametros.Modo === "NUEVO" && !parametros.clientes) {
			}
			else {
				$scope.dsClientesGrupo.fetch().then(function(){
					datos = $scope.dsClientesGrupo.data(false);
					$.each(datos,function(key,value){
						value.IdMoneda = $scope.SesionIdMonedaSistema;
					})
					$scope.gridApiClientesGrupo.dataSource.data(datos);
				});	
			}
		},
		columns: [
					{field: "DocumentoIdentidad",	title: "UI_DOCUMENTO_IDENTIDAD",	width: "100px"},
					{field: "NombreCompleto",		title: "UI_NOMBRE_COMPLETO",		width: "200px"},
					{field: "CodigoPortafolio",		title: "UI_CODIGO_PORTAFOLIO",		width: "100px"},
					{field: "EstadoPortafolio",		title: "UI_ESTADO_PORTAFOLIO",		width: "75px"}
				],
		sortable		: true,
		height			: 200,
		userSettings	: true,
		navigatable		: true,
		excel : {
			allPages: true,
		},
		selectable : "multiple row",
  		autoBind : false
	};
	
	$scope.agregarClienteGrupo = function(){
	
		if($scope.datosForm.IdPortafolioSeleccionado!=false){
			
			for(var i = 0; i < $scope.gridApiClientesGrupo.dataSource.data().length; i++){
				if($scope.gridApiClientesGrupo.dataSource.data()[i].IdPortafolio === $scope.datosGrupo.portafolioSeleccionado.IdPortafolio){
					bcsComunes.msg("error",'Error',$translate.instant("UI_PORTAFOLIO_EN_GRUPO"));
					return;
				}
				if($scope.gridApiClientesGrupo.dataSource.data()[i].IdEmpleadoEjecutivo !== $scope.datosGrupo.portafolioSeleccionado.IdEmpleadoEjecutivo){
					bcsComunes.msg("error",'Error',$translate.instant("UI_PORTAFOLIO_CON_DISTINTO_EJECUTIVO"));
					return;
				}
			}

			var data = {
				IdCliente 				: $scope.datosGrupo.portafolioSeleccionado.IdCliente,
				DocumentoIdentidad 		: $scope.datosGrupo.portafolioSeleccionado.DocumentoIdentidad,
				NombreCompleto 			: $scope.datosGrupo.portafolioSeleccionado.NombreCompleto,
				IdPortafolio			: $scope.datosGrupo.portafolioSeleccionado.IdPortafolio,
				EstadoPortafolio 		: $scope.datosGrupo.portafolioSeleccionado.Estado, 
				CodigoPortafolio 		: $scope.datosGrupo.portafolioSeleccionado.CodigoPortafolio,
				IdEmpleadoEjecutivo		: $scope.datosGrupo.portafolioSeleccionado.IdEmpleadoEjecutivo
			}	
			$scope.gridApiClientesGrupo.dataSource.add(data);

			//des-seleccionar el socio de negocio
			$scope.datosForm.IdPortafolioSeleccionado=false;
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("UI_PORTAFOLIO_NO_SELECCIONADO"));	
		}
	};
	
	$scope.eliminarClienteGrupo = function(){
		if($scope.datosForm.seleccionClientesGrupo && $scope.datosForm.seleccionClientesGrupo.length>0){

			$scope.gridApiClientesGrupo.dataSource.remove($scope.datosForm.seleccionClientesGrupo);
			
		}else{
			bcsComunes.msg("error",'Error',$translate.instant("UI_PORTAFOLIO_NO_SELECCIONADO"));
		}
	};
	
	$scope.cancelar = function() {
		bcsComunes.ventanaCerrar(idVentana);
	}
	
	$scope.actualizarGrupo = function(){
		
		//llamar procedimiento almacenado de actualizacion grupos.
		bcsComunes.ventanaOverlay(idVentana,true);
				
		var clientesGrupo = new Array();
		for(var i = 0; i < $scope.gridApiClientesGrupo.dataSource.data().length; i++){
			clientesGrupo.push( {p_IdEmpresa: $scope.SesionIdEmpresa, p_IdGrupo: $scope.datosGrupo.IdGrupo, p_IdPortafolio: $scope.gridApiClientesGrupo.dataSource.data()[i].IdPortafolio} );
		} 

		if (parametros.Modo === 'NUEVO') {
			var datosNuevos= {
			    	p_IdGrupo	 				: $scope.datosGrupo.IdGrupo,
			    	p_IdEmpresa					: $scope.SesionIdEmpresa,
					p_IdEstado					: $scope.datosGrupo.IdEstado,
					p_Nombre					: $scope.datosGrupo.nombreGrupo,
					p_Codigo					: $scope.datosGrupo.codigoGrupo,
					p_IdTransaccion				: TRANSACCION_INSERT,
					p_tablaClientesPortafolio   : clientesGrupo
			};
					
			var post = servicioSociodeNegocio.modificarGrupoClientes(datosNuevos);
			
			post.$promise.then(function(result){
				var p = bcsComunes.ventanaMsg({
					mensaje : '',
					conflictos : util.parseServerResponse(result)
				},idVentana);

				bcsComunes.ventanaPromesa(p).then(function(){
					bcsComunes.ventanaOverlay(idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						var res = { IdGrupo : result.P_OUT_IDGRUPO};
						bcsComunes.ventanaData(idVentana,res);
						bcsComunes.ventanaCerrar(idVentana);
					}
				})
				
			}
			,function(result){
				bcsComunes.ventanaMsg({
					mensaje : '',
					conflictos : util.parseServerResponse(result)
				},idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		}
		else {
			var datosUpdate= {
			    	p_IdGrupo	 				: $scope.datosGrupo.IdGrupo,
			    	p_IdEmpresa					: $scope.SesionIdEmpresa,
					p_IdEstado					: $scope.datosGrupo.IdEstado,
					p_Nombre					: $scope.datosGrupo.nombreGrupo,
					p_Codigo					: $scope.datosGrupo.codigoGrupo,
					p_IdTransaccion				: TRANSACCION_UPDATE,
					p_tablaClientesPortafolio   : clientesGrupo
			};
					
			var post = servicioSociodeNegocio.modificarGrupoClientes(datosUpdate);
			
			post.$promise.then(function(result){
				var p = bcsComunes.ventanaMsg({
					mensaje : '',
					conflictos : util.parseServerResponse(result)
				},false,false,idVentana);

				bcsComunes.ventanaPromesa(p).then(function(){
					bcsComunes.ventanaOverlay(idVentana,false);
					if (result.P_OUT_ERRORESCONTADOR === 0) {
						bcsComunes.ventanaCerrar(idVentana);
					}
				})
			}
			,function(result){
				bcsComunes.ventanaMsg({
					mensaje : $translate.instant("UI_ERROR_AL_ACTUALIZAR_GRUPO"),
					conflictos : util.parseServerResponse(result)
				},false,false,idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			});
		}
		
		}
	//}
});


