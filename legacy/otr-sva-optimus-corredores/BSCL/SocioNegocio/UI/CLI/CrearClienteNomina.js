angular.module('ModuloOrdenes').controller('CrearClienteNominaCtrl', 
		function($scope,bcsComunes,$translate,$resource){
	
	var ventanaId = bcsComunes.ventanaWinId($scope);
	$scope.configData = {};
	$scope.data = {};
	$scope.recordData = {};
	$scope.configData.overlay = bcsComunes.overlay;
	$scope.ventanaId = bcsComunes.ventanaWinId($scope);
	$scope.configData.TipoDocumentoIdentidadBloqueado = false;
	$scope.data.filtroPerfilComercial = {
			IdCategoriaInversionista : {field: "IdCategoriaInversionista", operator: "eq", value: ""}
		};
	$scope.data.filtroEjecutivo = {
			IdEmpresa : {field: "IdEmpresa", operator: "eq", value: $scope.$root.SesionIdEmpresa},
			IdEstado : {field: "IdEstado", operator: "eq", value: 'ACT'}
		};
	$scope.data.filtroTipoCliente = {
		filtros : {
			IdEmpresa	: {field : "IdEmpresa", operator : "eq", value : $scope.$root.SesionIdEmpresa},
			IdEstado : {field : "IdEstado", operator : "eq", value : "ACT"}
		}
	};
	
	$scope.data.ClienteBloqueado = $scope.SesionValoresDefectoSistema.IdCrearClienteBloqueado;
	$scope.data.ParametrosDefecto = $scope.SesionValoresDefectoSistema.IdParametrosDefectoNominaCliente;
	$scope.data.ParametrosDefectoRequeridos = true;
	
	$scope.$watch('data.ParametrosDefecto', function(nval, oval){
		if(nval==="S"){
			$scope.data.IdEmpleadoEjecutivo = $scope.$root.SesionIdEmpleado;
			$scope.data.ParametrosDefectoRequeridos = true;
		}else{
			$scope.data.ParametrosDefectoRequeridos = false;
			$scope.data.IdEmpleadoEjecutivo = '';
			$scope.data.idCategoriaInversionista = -1;
			$scope.data.idPerfilComercialActual = -1;
			$scope.data.idTipoCliente = -1;
		}
	});
	
	$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
	$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
	$scope.configData.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
	
	var dSResultados = function(){
		return new bcs.DataSource({
			type: "array",
			definition:{
					data:[],
					serverFiltering 	: false, // Filtrado de datos en el servidor.
					serverPaging 		: false, // Paginado de los datos en el servidor.
					serverSorting 		: false, // Ordenamiento de los datos en el servidor.
					sortable 			: true,
					pageSize 			: 50,
					schema:{
						model:{
							fields:{
								TIPODOCUMENTO : {type: "string"},
								NOMBRES : {type: "string"},
								APELLIDOPATERNO : {type: "string"},
								APELLIDOMATERNO : {type: "string"},
								RAZONSOCIAL : {type: "string"},
								MENSAJE : {type: "string"},
							}
						}
					}
				}
		})
	};
	
	$scope.configResultados = {
		dataSource: dSResultados(),
		columns: [
			{field: 'TIPODOCUMENTO', title:$translate.instant('UI_TIPO_DOCUMENTO'), width: "100px" },
			{field: 'NOMBRES', title:$translate.instant('UI_NOMBRES'), width: "150px" },
			{field: 'APELLIDOPATERNO', title:$translate.instant('UI_APELLIDO_PATERNO'), width: "150px" },
			{field: 'APELLIDOMATERNO', title:$translate.instant('UI_APELLIDO_MATERNO'), width: "150px" },
			{field: 'RAZONSOCIAL', title:$translate.instant('UI_RAZON_SOCIAL'), width: "200px" },
			{field: 'MENSAJE', title:$translate.instant('UI_ESTADO')}

		],
		sortable: true,
		pageable: true,
		height: 200,
		//autoBind : true
	};
	
	$scope.cancelar = function() {
		$scope.limpiarPantalla();
		$scope.configData.bloqueoBuscador = false;	//BLOQUEA ZONA BUSCADOR
		$scope.configData.bloqueoPersona = true;	//BLOQUEA ZONA PERSONA
		$scope.configData.bloqueoCliente = true;	//BLOQUEA ZONA PERSONA
	};
	
	var crearClienteNominaSP = function(v_Dato){
       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=232";
       var r = $resource("", {}, {'urlConsulta': { method: "POST", url: urlConsulta}});
       var post = r.urlConsulta(v_Dato);
       return post.$promise;
    };
    
    var crearClienteNominaSP_PERU = function(v_Dato){
       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=3592";
       var r = $resource("", {}, {'urlConsulta': { method: "POST", url: urlConsulta}});
       var post = r.urlConsulta(v_Dato);
       return post.$promise;
    };
	
	$scope.cargarArchivo = function(){
		bcsComunes.ventanaOverlay($scope.ventanaId, true);
		//funciones para validar la dependencia entre campos
		 var dependenciaTipoPersonaNombre = function(linea){
		 	if(linea.TIPOPERSONA && linea.TIPOPERSONA==="N"){
		 		return (linea.NOMBRES!="" && linea.APELLIDOPATERNO!=="" && linea.APELLIDOMATERNO!=="")?"OK":"EL NOMBRE ES OBLIGATORIO PARA TIPOS DE PERSONA NATURAL";
		 	}else if(linea.TIPOPERSONA && linea.TIPOPERSONA==="J"){
		 		return (linea.RAZONSOCIAL!=="")?"OK":"LA RAZON SOCIAL ES OBLIGATORIA PARA TIPOS DE PERSONA JURIDICA";
		 	}
		 };
		 
		 var datosArchivo = {codigoArchivo: 'ARCHIVO_CLIENTE', funcionesDependencia:[dependenciaTipoPersonaNombre]};
		 if($scope.SesionLocale=='PE')
			 datosArchivo = {codigoArchivo: 'ARCHIVO_CLIENTE_PE', funcionesDependencia:[dependenciaTipoPersonaNombre]};
		 var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false);
		 var promesa=bcsComunes.ventanaPromesa(id1);                               
		 promesa.then(function(data){
			 bcsComunes.ventanaOverlay($scope.ventanaId, false);
			 if(!data || !data.fileData){
				 return;
			 }
			 var clientesValidos = [];
			 $scope.clientesNoValidos = [];
			 for(var i = 0; i < data.fileData.length; i++){
			 	if($scope.data.ParametrosDefecto==="S"){
			 		data.fileData[i].EJECUTIVO = (data.fileData[i].EJECUTIVO)?data.fileData[i].EJECUTIVO : $scope.recordData.EjecutivoObj.CodigoEmpleado;
			 		data.fileData[i].CATINV = (data.fileData[i].CATINV)?data.fileData[i].CATINV : $scope.data.idCategoriaInversionista;
			 		data.fileData[i].PERCOMER = (data.fileData[i].PERCOMER)?data.fileData[i].PERCOMER : $scope.data.idPerfilComercialActual;
			 		data.fileData[i].TIPOCLIENTE = (data.fileData[i].TIPOCLIENTE)?data.fileData[i].TIPOCLIENTE : $scope.data.idTipoCliente;
			 		if($scope.SesionLocale=='PE'){
			 			if($scope.recordData.BrokerObj){
			 				data.fileData[i].BROKER = (data.fileData[i].BROKER)?data.fileData[i].BROKER:$scope.recordData.BrokerObj.Codigo;
			 			}
			 		}
			 		clientesValidos.push(data.fileData[i]);
			 	}else if($scope.data.ParametrosDefecto==="N" 
			 		&& ( data.fileData[i].CATINV && data.fileData[i].CATINV.length>0
			 			 && data.fileData[i].PERCOMER && data.fileData[i].PERCOMER.length>0 
			 			 && data.fileData[i].TIPOCLIENTE && data.fileData[i].TIPOCLIENTE.length>0 
			 			 || (data.fileData[i].BROKER && data.fileData[i].BROKER.length>0 && $scope.SesionLocale=='PE') 
			 			)
			 	){
			 		clientesValidos.push(data.fileData[i]);	
			 	}else{
			 		data.fileData[i].MENSAJE = "Falta información para ingresar el cliente.";
			 		$scope.clientesNoValidos.push(data.fileData[i]);	
			 	}
			 }
			 
			 var parametrosSP = {};
		        parametrosSP.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
		        parametrosSP.p_IdUsuario =  $scope.$root.SesionIdUsuario;
		        parametrosSP.p_IdModulo = 'CLI';
		        parametrosSP.p_ClienteBloqueado = $scope.data.ClienteBloqueado;
		        parametrosSP.p_IdCategoriaInversionistaDefecto = $scope.data.idCategoriaInversionista || '';
		        parametrosSP.p_IdPerfilComercialDefecto = $scope.data.idPerfilComercialActual  || '';
		        parametrosSP.p_IdTipoClienteDefecto = $scope.data.idTipoCliente  || '';
		        parametrosSP.p_IdEmpleadoEjecutivoDefecto = $scope.recordData.EjecutivoObj.IdEmpleadoEjecutivo || "0";
		        parametrosSP.p_PermiteParametrosDefault = $scope.data.ParametrosDefecto;
		        parametrosSP.p_Archivo = clientesValidos;
		        //return;//debug
		        if(clientesValidos.length){ 
	        		bcsComunes.ventanaOverlay($scope.ventanaId,true);
	        		var sp;
	        		if($scope.SesionLocale=='PE'){
	        			sp = crearClienteNominaSP_PERU(parametrosSP);
	        		}else{
	        			sp = crearClienteNominaSP(parametrosSP);
	        		}
	        		
	        		sp.then(function(result) {
						bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_PROCESO_REALIZADO'), conflictos : util.parseServerResponse(result)}, ventanaId);
						$scope.gridApiResultados.dataSource.data(result.P_OUT_MENSAJE);
						for(var key in $scope.clientesNoValidos){
							$scope.gridApiResultados.dataSource.add($scope.clientesNoValidos[key]);
						}
						bcsComunes.ventanaOverlay($scope.ventanaId,false);	
					},function(result){
						console.log(JSON.stringify(result));
						bcsComunes.ventanaOverlay($scope.ventanaId,false);
						var p=bcsComunes.ventanaMsgErr($translate.instant('UI_OPERACION_TIENE_ERRORES'),$scope.ventanaId);
						bcsComunes.ventanaPromesa(p).then(function(){
							//bcsComunes.ventanaCerrar($scope.ventanaId);
						});
						
					});
					
	        	}else{
	        		for(var key in $scope.clientesNoValidos){
						$scope.gridApiResultados.dataSource.add($scope.clientesNoValidos[key]);
					}
	        	}
   			}); 
	};
	
	
		
});
