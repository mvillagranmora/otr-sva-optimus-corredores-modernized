angular.module('ModuloOrdenes').controller('ModificarGrupoCtrl',function($scope,$translate,bcsConsultasOrdenes,bcsServicioOrden,$resource,bcsComunes,bcsConfiguracionOrdenesV2)
{
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
					url : bcsConsultasOrdenes('ConsultarPortafoliosGrupo'),
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
						Porcentaje 				: {type: "number"},
						NombreLibroOrdenes		: {type: "string"},
						IdLibroOrdenes			: {type: "string"}
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
							IdMoneda		 	: {type: "string"},
							CodigoPortafolio 		: {type: "string"},
							NombreLibroOrdenes		: {type: "string"},
							IdLibroOrdenes			: {type: "string"},
							Porcentaje 			: {type: "number",defaultValue: 0,
                                validation: {
                                    required: true,
                                    validatePorc: function (input) {
                                        if (input.attr("data-bind") == "value:Porcentaje" && input.val() < 0) {
                                        	bcsComunes.msg("error","UI_ERROR","UI_PORCENTAJE_NEGATIVO");
                            				return false;
                                        }else if (input.attr("data-bind") == "value:Porcentaje" && input.val() > 100)
                                        {
                                        	bcsComunes.msg("error","UI_ERROR","UI_PORCENTAJE_SOBREPASA_100");
                            				return false;
                                        }
                                        return true;
                                    }
                                }
							}
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
					{field: "DocumentoIdentidad",	title: "UI_DOCUMENTO_IDENTIDAD",	width: "110px"},
					{field: "NombreCompleto",		title: "UI_NOMBRE_COMPLETO",		width: "200px"},
					{field: "CodigoPortafolio",		title: "UI_CODIGO_PORTAFOLIO",		width: "92px"},
					{field: "EstadoPortafolio",		title: "UI_ESTADO_PORTAFOLIO",		width: "90px"},
					{field: "NombreLibroOrdenes",	title: "UI_LIBRO_ORDENES",			width: "92px"},
					bcsConfiguracionOrdenesV2.getConfiguracionColumna("Porcentaje",{editable : true})
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
			
			var clienteEstaEnGrupo = false;
			for(var i = 0; i < $scope.gridApiClientesGrupo.dataSource.data().length; i++){
				if($scope.gridApiClientesGrupo.dataSource.data()[i].IdPortafolio === $scope.datosGrupo.portafolioSeleccionado.IdPortafolio){
					clienteEstaEnGrupo = true;
				}
			}

			if(clienteEstaEnGrupo==false){
				var data = {
					IdCliente 				: $scope.datosGrupo.portafolioSeleccionado.IdCliente,
					DocumentoIdentidad 		: $scope.datosGrupo.portafolioSeleccionado.DocumentoIdentidad,
					NombreCompleto 			: $scope.datosGrupo.portafolioSeleccionado.NombreCompleto,
					IdPortafolio			: $scope.datosGrupo.portafolioSeleccionado.IdPortafolio, 
					IdMoneda				: $scope.SesionIdMonedaSistema, 
					EstadoPortafolio 		: $scope.datosGrupo.portafolioSeleccionado.Estado, 
					CodigoPortafolio 		: $scope.datosGrupo.portafolioSeleccionado.CodigoPortafolio,
					NombreLibroOrdenes 		: $scope.datosGrupo.portafolioSeleccionado.IdTipoCliente,
					Porcentaje 				: 0
				}	
				$scope.gridApiClientesGrupo.dataSource.add(data);
			}else{
				bcsComunes.msg("error",'Error',$translate.instant("UI_PORTAFOLIO_EN_GRUPO"));	
			}

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
		var sumaporcentajes = 0;
		var LibroOrden;
		var LibroOrdenDistinto = false;
		for(var i = 0; i < $scope.gridApiClientesGrupo.dataSource.data().length; i++){
			sumaporcentajes += $scope.gridApiClientesGrupo.dataSource.data()[i].Porcentaje;
			clientesGrupo.push( {IdPortafolio: $scope.gridApiClientesGrupo.dataSource.data()[i].IdPortafolio, Porcentaje : $scope.gridApiClientesGrupo.dataSource.data()[i].Porcentaje} );
			if(LibroOrden == null){
				LibroOrden = $scope.gridApiClientesGrupo.dataSource.data()[i].NombreLibroOrdenes;
			}else if(LibroOrden != $scope.gridApiClientesGrupo.dataSource.data()[i].NombreLibroOrdenes){
				LibroOrdenDistinto = true
			}
		} 
		if(LibroOrdenDistinto){
			bcsComunes.ventanaMsg({
				conflictos : {
    				errores: [{
   						Codigo : "",
   						Descripcion : $translate.instant("UI_ERROR_LIBROS_ORDENES_DISTINTOS")
   					}]
				}
			},false,false,idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		}
		else
		{
			if(sumaporcentajes > 100){
				bcsComunes.ventanaMsg({
					conflictos : {
						errores: [{
							   Codigo : "",
							   Descripcion : $translate.instant("UI_ERROR_SUMA_PORCENTAJE")
						   }]
					}
				},false,false,idVentana);
				bcsComunes.ventanaOverlay(idVentana,false);
			}
			else
			{
				if (parametros.Modo === 'NUEVO') {
					var datosNuevos= {
							p_IdUsuario					: $scope.SesionIdUsuario,
							p_IdEmpresa 				: $scope.SesionIdEmpresa,
							p_IdModulo					: 'ORD',
							p_Nombre					: $scope.datosGrupo.nombreGrupo,
							p_Codigo					: $scope.datosGrupo.codigoGrupo,
							p_IdEstado					: $scope.datosGrupo.IdEstado,
							p_tablaClientesPortafolio	: clientesGrupo
					};
							
					var post = bcsServicioOrden.CrearGrupo(datosNuevos);
					
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
							p_IdUsuario					: $scope.SesionIdUsuario,
							p_IdEmpresa 				: $scope.SesionIdEmpresa,
							p_IdModulo					: 'ORD',
							p_IdGrupo					: $scope.datosGrupo.IdGrupo,
							p_Nombre					: $scope.datosGrupo.nombreGrupo,
							p_Codigo					: $scope.datosGrupo.codigoGrupo,
							p_IdEstado					: $scope.datosGrupo.IdEstado,
							p_tablaClientesPortafolio	: clientesGrupo
					};
							
					var post = bcsServicioOrden.ActualizarGrupoPortafolio(datosUpdate);
					
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
		}
		
	}
});


