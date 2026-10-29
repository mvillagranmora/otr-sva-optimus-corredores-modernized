angular.module('ModuloSocioNegocio').
	controller('ConsultarEjecutivoCtrl',
			function($scope,$translate, bcsComunes, $resource){
	
	$scope.data = {};
	var ventanaId = bcsComunes.ventanaWinId($scope);
	var seleccionEjecutivosGrilla = new Array();
	parametros = bcsComunes.ventanaParams (ventanaId);
	
	if(parametros)
	{
		$scope.data.IdEmpleadoSupervisorDefecto = parametros.IdEmpleadoSupervisor;
	}
	
	$scope.gridEjecutivo = {
			columns :
			[
			 	{field: "NombreCompleto",title: $translate.instant('UI_NOMBRE'),align: "left",width: "200px"}
				,{field: "CodigoEmpleado",title: $translate.instant('UI_CODIGO_EMPLEADO'),align: "left",width: "80px"}
				,{field: "NombreSupervisor",title: $translate.instant('UI_NOMBRE_SUPERVISOR'),align: "left",width: "200px"}
				,{field: "NombreEstado",title: $translate.instant('UI_ESTADO'),align: "left",width: "80px"}	
				,{field: "NombreCompletoReemplaza",title: $translate.instant('UI_NOMBRE_REEMPLAZA'),align: "left",width: "200px"}	
				,{field: "FechaFinReemplazo",title: $translate.instant('UI_FECHA_FIN_REEMPLAZO'),align: "left",width: "80px"}	
			],
			selectable : "multiple row",	
	  		dataSource : new bcs.DataSource({
	  			type: 'odata',
	  			definition : {
	  				serverFiltering: true,
	            	serverPaging: true,
	            	serverSorting: true,
	           	 	pageSize:20,
	        		sort: [//{ field: "NombreTipoDocumentoIdentidadDefecto", dir: "asc" },
	              	  	//{ field: "DocumentoIdentidadDefecto", dir: "asc" },
	                   	//{ field: "NombreCompleto", dir: "asc" }
	                ],
	            	transport: {
	                	read: {
	                		url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1470",
	                		dataType: "json"
	                	}
	            	},
	            	schema: {
		        		model: {
	    	        		fields: {
	    	        			CodigoEmpleado:{type:"string"},
								CodigoSucursal:{type:"string"},
								FechaFinReemplazo:{type:"date"},
								FechaInicioReemplazo:{type:"date"},
								IdEmpleadoEjecutivo:{type:"number"},
								IdEmpleadoEjecutivoReemplaza:{type:"number"},
								IdEmpleadoSupervisor:{type:"number"},
								IdEmpresa:{type:"number"},
								IdEstado:{type:"string"},
								IdPersonaEjecutivo:{type:"number"},
								IdSucursal:{type:"number"},
								NombreCompleto:{type:"string"},
								NombreCompletoReemplaza:{type:"string"},
								NombreEstado:{type:"string"},
								NombreSucursal:{type:"string"},
								NombreSupervisor:{type:"string"}
		        			}
		        		}
		        	}
	  			},
	  		}),
	  		gridCreated: function(grid) {

				$scope.data.mainGrid = grid;
			},
			pageable: true,
			pageSize: 20,
			autoBind:false,
			filterable : true,
			sortable: true,
			groupable: true,
			height:300,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			}
		};
	
	var modificarEstadoEjecutivoSP = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1678";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };

	const eliminarReemplazoEjecutivo = function(v_Dato)
    {
		var urlIngresar = '';
		urlIngresar = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700701";
		var r = $resource
        ("", {}, {'urlIngresar': { method: "POST", url: urlIngresar}}
        );
		var post = r.urlIngresar(v_Dato);
		return post.$promise;
    };

	var listadoDeSeleccionGrilla = function(seleccionGrillaEjecutivos){

		var ejecutivosSeleccionados = [];
		$.each(seleccionGrillaEjecutivos,function(key,value){    
			ejecutivosSeleccionados.push(
				{      
					IdEmpleadoEjecutivo : seleccionGrillaEjecutivos[key].IdEmpleadoEjecutivo,
					IdEmpleadoSupervisor : seleccionGrillaEjecutivos[key].IdEmpleadoSupervisor,
					NombreCompleto : seleccionGrillaEjecutivos[key].NombreCompleto,
					IdEstado : seleccionGrillaEjecutivos[key].IdEstado
				}
			);
		});

		return ejecutivosSeleccionados;
	};

	var apruebaValidacionPreviaAccion = function(seleccionGrillaEjecutivos){

		if(seleccionGrillaEjecutivos.length == 0){

			bcsComunes.ventanaOverlay(ventanaId,false);
			bcsComunes.msg("warning","Advertencia"," Seleccione un ejecutivo");
			return false; 
		}
		else if(seleccionGrillaEjecutivos.length > 1){

			bcsComunes.ventanaOverlay(ventanaId,false);
			bcsComunes.msg("error","Error "," Solo se puede seleccionar un Ejecutivo para esta accion");
			return false;
		// se asume que tiene datos
		}else{

			return true;
		}
	};

	var resetearSeleccionGrilla = function(){

		$scope.seleccionGrilla.length = 0;
		seleccionEjecutivosGrilla = [];
	}

	$scope.buscar = function(){
		
		var filters = new Array();
		
		filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa });
		
		if($scope.data.IdEmpleadoSupervisor){
			filters.push({ field: "IdEmpleadoSupervisor", operator: "eq", value: $scope.data.IdEmpleadoSupervisor });
		}
				
		if ($scope.data.NombreEjecutivoFiltro){
    		filters.push({ field: "NombreCompleto", operator: "contains", value: $scope.data.NombreEjecutivoFiltro });
    	}
		
    	if ($scope.data.CodigoEmpleadoEjecutivoFiltro){
    		filters.push({ field: "CodigoEmpleado", operator: "contains", value: $scope.data.CodigoEmpleadoEjecutivoFiltro });
    	}
		
		$scope.data.mainGrid.dataSource.filter(filters);

		if(typeof($scope.seleccionGrilla ) === 'undefined'){

			seleccionEjecutivosGrilla = [];
		}
		else{

			resetearSeleccionGrilla();
		}
	};

	$scope.limpiarFiltros = function(){

		$scope.data.IdEmpleadoSupervisor = undefined;
		$scope.data.NombreEjecutivoFiltro = "";
		$scope.data.CodigoEmpleadoEjecutivoFiltro = "";

	};

	$scope.Ingresar = function() {

		var id1 = bcsComunes.ventanaAbrir("modalEjecutivo",{},false,false,ventanaId ); 
	    var promesa=bcsComunes.ventanaPromesa(id1);                               
	    $.when(promesa).then(function(respuesta){ 
	    	$scope.buscar();
	    });
	};
	
	$scope.Clientes = function() {

		seleccionEjecutivosGrilla = listadoDeSeleccionGrilla($scope.seleccionGrilla);

		if( apruebaValidacionPreviaAccion($scope.seleccionGrilla) === true){

			const datosParaVentana = {
			
				IdEmpleadoSupervisor: seleccionEjecutivosGrilla[0].IdEmpleadoSupervisor,
				IdEmpleadoEjecutivo : seleccionEjecutivosGrilla[0].IdEmpleadoEjecutivo
			}

			var id1 = bcsComunes.
			ventanaAbrir("ConsultarClienteEjecutivo",datosParaVentana,false,false, ventanaId); 
			var promesa=bcsComunes.ventanaPromesa(id1);  

		}
	};
	
	$scope.Reemplazo = function() {

		seleccionEjecutivosGrilla = listadoDeSeleccionGrilla($scope.seleccionGrilla);

		if( apruebaValidacionPreviaAccion($scope.seleccionGrilla) === true){

			const datosParaVentana = {

				IdEmpleadoEjecutivoAnterior : seleccionEjecutivosGrilla[0].IdEmpleadoEjecutivo,
				NombreEjecutivo :  seleccionEjecutivosGrilla[0].NombreCompleto
			}

			var id1 = bcsComunes.ventanaAbrir("modalReemplazoEjecutivo",datosParaVentana,false,false, ventanaId); 
		    var promesa=bcsComunes.ventanaPromesa(id1);

		}                        
	};

	var validarEstadosBloqueadoODesbloqueado = function(accionEjecutada ,estadoEjecutivoSelecionado){

		if(accionEjecutada === "BLO"){

			if(estadoEjecutivoSelecionado === "ACT"){

				return true;
			}
			else{
				bcsComunes.ventanaOverlay(ventanaId,false);
				bcsComunes.msg("warning","Advertencia"," Ejecutivo ya esta Bloqueado");
				return false;
			}
		}
		else if(accionEjecutada === "ACT"){

			if(estadoEjecutivoSelecionado === "BLO"){

				return true;
			}
			else{

				bcsComunes.ventanaOverlay(ventanaId,false);
				bcsComunes.msg("warning","Advertencia"," Ejecutivo ya esta Activo");
				return false;
			}
		}
	};
	
	$scope.Bloquear = function() {
		
		seleccionEjecutivosGrilla = listadoDeSeleccionGrilla($scope.seleccionGrilla);

		if( apruebaValidacionPreviaAccion($scope.seleccionGrilla) === true){

			if(validarEstadosBloqueadoODesbloqueado("BLO", seleccionEjecutivosGrilla[0].IdEstado) === true){

				bcsComunes.ventanaOverlay(ventanaId,true);
				var parametrosSP = {
					p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
					p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
					p_IdModulo			   	: 'SOC',
					p_UsaReglasNegocio	   	: 'N',
					p_IdEmpleadoEjecutivo	: seleccionEjecutivosGrilla[0].IdEmpleadoEjecutivo,
					p_IdEstado				: 'BLO',
				};

				modificarEstadoEjecutivoSP(parametrosSP).then(

					function(result) {

						bcsComunes.ventanaOverlay(ventanaId,false);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Ejecutivo Bloqueado",
							conflictos : util.parseServerResponse(result)}, ventanaId);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
 
							$scope.buscar();	
						});
					},
					function(result){

						bcsComunes.ventanaOverlay(ventanaId,false);
						bcsComunes.msg("error","Error","La operacion tiene errores");
				});
			}
		}
	};
	
	$scope.EliminarReemplazo = function() {
		seleccionEjecutivosGrilla = listadoDeSeleccionGrilla($scope.seleccionGrilla);
		bcsComunes.ventanaOverlay(ventanaId,true);
		var parametrosSP = {
			p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
			p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
			p_IdModulo			   	: 'SOC',
			p_UsaReglasNegocio	   	: 'N',
			p_IdEmpleadoEjecutivo	: seleccionEjecutivosGrilla[0].IdEmpleadoEjecutivo,
		};
		eliminarReemplazoEjecutivo(parametrosSP)
			.then(function(result) {
				bcsComunes.ventanaOverlay(ventanaId,false);
				var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Reemplazo Eliminado",
				conflictos : util.parseServerResponse(result)}, ventanaId);
				bcsComunes.ventanaPromesa(idMensaje)
					.then(
						function(){
							$scope.buscar();
						}
					);
			},function(result){
				bcsComunes.ventanaOverlay(ventanaId,false);
				bcsComunes.msg("error","Error","La operacion tiene errores");
			});
	};

	$scope.Desbloquear = function() {

		seleccionEjecutivosGrilla = listadoDeSeleccionGrilla($scope.seleccionGrilla);

		if( apruebaValidacionPreviaAccion($scope.seleccionGrilla) === true){

			if(validarEstadosBloqueadoODesbloqueado("ACT", seleccionEjecutivosGrilla[0].IdEstado) === true){

				bcsComunes.ventanaOverlay(ventanaId,true);
				var parametrosSP = {

					p_IdEmpresa			   	: $scope.$root.SesionIdEmpresa, 
					p_IdUsuario			   	: $scope.$root.SesionIdUsuario,
					p_IdModulo			   	: 'SOC',
					p_UsaReglasNegocio	   	: 'N',
					p_IdEmpleadoEjecutivo	: seleccionEjecutivosGrilla[0].IdEmpleadoEjecutivo,
					p_IdEstado				: 'ACT',
    			};

				modificarEstadoEjecutivoSP(parametrosSP).then(

					function(result) {
						bcsComunes.ventanaOverlay(ventanaId,false);
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Ejecutivo Desbloqueado",
							conflictos : util.parseServerResponse(result)}, ventanaId);
						bcsComunes.ventanaPromesa(idMensaje).then(function(){

							$scope.buscar();	
						});
					},
					function(result){
						bcsComunes.ventanaOverlay(ventanaId,false);
						bcsComunes.msg("error","Error","La operacion tiene errores");
					}
				);
			}
		}
	};
	
	$scope.TransferirClientes = function() {

		seleccionEjecutivosGrilla = listadoDeSeleccionGrilla($scope.seleccionGrilla);

		if( apruebaValidacionPreviaAccion($scope.seleccionGrilla) === true){

			const datosParaVentana = {

				IdEmpleadoEjecutivoAnterior : seleccionEjecutivosGrilla[0].IdEmpleadoEjecutivo
			}
			var id1 = bcsComunes.ventanaAbrir("modalTransferirClientes",datosParaVentana, false, false, ventanaId); 
	    	var promesa=bcsComunes.ventanaPromesa(id1);                               
	    	$.when(promesa).then(function(respuesta){ 

	    		$scope.buscar();
	    	});
		}

	};
	
	$scope.recarterizacionMasivaClientes = function(){
		
		var parametros = {};
		var id1 = bcsComunes.ventanaAbrir("RecarterizacionMasivaClientes",parametros, false, false, ventanaId); 
	    var promesa=bcsComunes.ventanaPromesa(id1);                               
	    $.when(promesa).then(function(respuesta){ 
	    	$scope.buscar();
	    });
	};
	
	$scope.ventanaModificarSupervisor = function(){

		var listadoDeSeleccion = listadoDeSeleccionGrilla($scope.seleccionGrilla);
		if(listadoDeSeleccion.length === 0){

			bcsComunes.ventanaOverlay(ventanaId,false);
			bcsComunes.msg("warning","Advertencia"," Seleccione un ejecutivo");
		}
		else{

			const parametros = {

				Ejecutivos : listadoDeSeleccion
			}
			
			var id1 = bcsComunes.ventanaAbrir("modalModificarSupervisorAEjecutivo", parametros, false, false, ventanaId);	
			var promesa=bcsComunes.ventanaPromesa(id1);  
			$.when(promesa).then(function(respuesta){

				$scope.buscar();
			});
		}
	};
	
});