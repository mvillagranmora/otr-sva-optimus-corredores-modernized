angular.module('ModuloFacturacion').controller('CargarArchivoOtrosConceptosCtrl',
		function($scope,$state,$translate,$resource,$timeout,bcsComunes,bcsServiciosFacturacion){
	$scope.conf = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	$scope.conf.archivoValido = false;
	$scope.conf.cantidadMaximaCargaOperaciones = $scope.SesionValoresDefectoSistema.MaximaCargaOtrosConceptos;
	
	$scope.configResultados = {
		dataSource: new bcs.DataSource({
			type: "array",
			definition:{
					data:[],
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					serverPaging 		: true, // Paginado de los datos en el servidor.
					serverSorting 		: true, // Ordenamiento de los datos en el servidor.
					sortable 			: true,
					pageSize 			: 50,
					schema:{
						model:{
							fields:{
								SECUENCIA_MOVIMIENTO:{type:"number"},
								CONCEPTO:{type:"string"},
								DOCUMENTO_IDENTIDAD:{type:"string"},
								NOMBRE:{type:"string"},
								CODIGO_PORTAFOLIO:{type:"string"},
								TIPO_COMPROBANTE:{type:"string"},
								CONDICION_LIQUIDACION:{type:"string"},
								MONEDA:{type:"string"},
								MONTO:{type:"number"},
						//		FECHA_MOVIMIENTO_COMPROBANTE:{type:"string"},
								GLOSA:{type:"string"},
								MEDIO_DE_PAGO:{type:"string"},
								MODALIDAD_DE_PAGO:{type:"string"},
								EXISTE:{type:"string"},
								ID_EMPRESA:{type:"number"},
								ID_EMPLEADO_EJECUTIVO:{type:"number"},
								ID_CLIENTE:{type:"number"},
								ID_PORTAFOLIO:{type:"number"},
								OPERA_CUENTA_INVERSION:{type:"string"},
								VALIDO:{type:"string"}
							}
						}
					}
				}
		}),
		columns: [
			{field: 'SECUENCIA_MOVIMIENTO', title:$translate.instant('UI_SECUENCIA_MOVIMIENTO'), width: "100px" },
			{field: 'CONCEPTO', title:$translate.instant('UI_CONCEPTO'), width: "100px" },
			{field: 'DOCUMENTO_IDENTIDAD', title:$translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "100px" },
			{field: 'NOMBRE', title:$translate.instant('UI_NOMBRE'), width: "100px" },
			{field: 'CODIGO_PORTAFOLIO', title:$translate.instant('UI_CODIGO_PORTAFOLIO'), width: "100px" },
			{field: 'TIPO_COMPROBANTE', title:$translate.instant('UI_TIPO_COMPROBANTE'), width: "100px" },
			{field: 'CONDICION_LIQUIDACION', title:$translate.instant('UI_CONDICION_LIQUIDACION'), width: "100px" },
			{field: 'MONEDA', title:$translate.instant('UI_MONEDA'), width: "100px" },
			{field: 'MONTO', title:$translate.instant('UI_MONTO'), width: "100px" },
		//	{field: 'FECHA_MOVIMIENTO_COMPROBANTE', title:$translate.instant('UI_FECHA_MOVIMIENTO'), width: "100px" },
			{field: 'GLOSA', title:$translate.instant('UI_GLOSA'), width: "100px" },
			{field: 'MEDIO_DE_PAGO', title:$translate.instant('UI_MEDIO_DE_PAGO'), width: "100px" },
			{field: 'MODALIDAD_DE_PAGO', title:$translate.instant('UI_MODALIDAD_PAGO'), width: "100px" },
			{field: 'EXISTE', title:$translate.instant('UI_REGISTRO_VALIDO'), width: "100px"},
			{field: 'ID_EMPRESA', title:$translate.instant('ID_EMPRESA'), width: "100px",hidden:true },
			{field: 'ID_EMPLEADO_EJECUTIVO', title:$translate.instant('ID_EMPLEADO_EJECUTIVO'), width: "100px",hidden:true},
			{field: 'ID_CLIENTE', title:$translate.instant('ID_CLIENTE'), width: "100px",hidden:true},
			{field: 'ID_PORTAFOLIO', title:$translate.instant('ID_PORTAFOLIO'), width: "100px",hidden:true},
			{field: 'OPERA_CUENTA_INVERSION', title:$translate.instant('OPERA_CUENTA_INVERSION'), width: "100px",hidden:true},
			{field: 'VALIDO', title:$translate.instant('VALIDO'), width: "100px",hidden:true}
		],
		gridCreated: function(grid) {
			$scope.resultadoGrid = grid;
		},
		sortable: true,
		pageable: true,
		height: 200
	};

	$scope.PortafolioDataSource = new bcs.DataSource({
		type: "odata",
		definition : {
			serverFiltering:true,
			transport: {
				read: {
					url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=13015",
					dataType: "json"
				}
			},
			schema: {
				model: {
					fields: {
						IdPortafolio 	: { type: "number"},
						IdCliente		: { type: "number"},
						DocumentoIdentidad: {type: "string"},
						ID				: { type: "number"}
					}
				}
			}
		}
	});
	
	$scope.cargarArchivo = function(){
		$scope.conf.archivoValido = false;
		bcsComunes.ventanaOverlay(idVentana,true);
		 var datosArchivo = {codigoArchivo: 'ARCHIVO_OTROS_CONCEPTOS'};
		 var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,false,idVentana,false);
			
		 var promesa=bcsComunes.ventanaPromesa(id1);                               
		 promesa.then(function(data){
		 	if(data && data.fileData){
				var movimientos = {};
				//obtener informacion de todos los clientes con sus portafolios
				var objFilters = {};
				objFilters.logic = "or";
				objFilters.filters = new Array();
				for(var i=0; i < data.fileData.length; i++){
					var movimiento = data.fileData[i];
					var obj2filters = { };
					var niv2filters = new Array();
					obj2filters.logic = 'and';
					obj2filters.filters = niv2filters;

					//Busqueda por documento identidad y codigo portafolio de cada registro
					niv2filters.push({field:"DocumentoIdentidad", value: movimiento.DOCUMENTO_IDENTIDAD, operator: "eq"});
					niv2filters.push({field:"CodigoPortafolio", value: movimiento.CODIGO_PORTAFOLIO, operator: "eq"});

					objFilters.filters.push(obj2filters);
					//por defecto dejar todos los registros como invalidos.
					movimiento.NOMBRE = "";
					movimiento.ID_CLIENTE= 0;
					movimiento.ID_PORTAFOLIO= 0;
					movimiento.ID_EMPLEADO_EJECUTIVO= 0;
					movimiento.MEDIO_DE_PAGO= ""; 
					movimiento.ID_EMPRESA= $scope.SesionIdEmpresa;
					movimiento.EXISTE = $translate.instant("UI_NO");
					movimiento.VALIDO = "N";
					movimiento.ID = i;
					movimiento.OPERA_CUENTA_INVERSION  = "";
					//movimientos[movimiento.DOCUMENTO_IDENTIDAD] = movimiento;
					movimientos[i] = movimiento;
				}



				$scope.PortafolioDataSource.query({filter:objFilters}).then(function(){
					console.log("se obtuvieron " + $scope.PortafolioDataSource.view().length+" clientes de "+data.fileData.length+" presentes en el archivo");

					//if($scope.conf.cantidadMaximaCargaOperaciones > $scope.PortafolioDataSource.data().length){
					if($scope.conf.cantidadMaximaCargaOperaciones > data.fileData.length){ //se debe comparar con numero de registros del archivo, no del numero de clientes
					
						//for(var i=0; i<$scope.PortafolioDataSource.data().length; i++){
						var z = 0;
							for(var i=0; i<data.fileData.length; i++){
							
								
							for(var j=0; j<$scope.PortafolioDataSource.data().length; j++){
								
								if(data.fileData[i].DOCUMENTO_IDENTIDAD===$scope.PortafolioDataSource.data()[j].DocumentoIdentidad)
								{
									
									var row = $scope.PortafolioDataSource.data()[j];
									row.ID = z;
									//completar la informacion necesaria y validar los registros que si existen (documento identidad +codigo portafolio)
									/*movimientos[row.DocumentoIdentidad].NOMBRE = row.NombreCompleto;
									movimientos[row.DocumentoIdentidad].ID_CLIENTE= row.IdCliente;
									movimientos[row.DocumentoIdentidad].ID_PORTAFOLIO= row.IdPortafolio;
									movimientos[row.DocumentoIdentidad].ID_EMPLEADO_EJECUTIVO= row.IdEmpleadoEjecutivo;
									movimientos[row.DocumentoIdentidad].ID_EMPRESA= row.IdEmpresa;
									movimientos[row.DocumentoIdentidad].OPERA_CUENTA_INVERSION= row.OperaCuentaInversion;
									movimientos[row.DocumentoIdentidad].EXISTE = $translate.instant("UI_SI");
									movimientos[row.DocumentoIdentidad].VALIDO = 'S';*/
									
									
									movimientos[row.ID].NOMBRE = row.NombreCompleto;
									movimientos[row.ID].ID_CLIENTE= row.IdCliente;
									movimientos[row.ID].ID_PORTAFOLIO= row.IdPortafolio;
									movimientos[row.ID].ID_EMPLEADO_EJECUTIVO= row.IdEmpleadoEjecutivo;
									movimientos[row.ID].ID_EMPRESA= row.IdEmpresa;
									movimientos[row.ID].OPERA_CUENTA_INVERSION= row.OperaCuentaInversion;
									movimientos[row.ID].EXISTE = $translate.instant("UI_SI");
									movimientos[row.ID].VALIDO = 'S'
										
									z++;
									
									//if(i===$scope.PortafolioDataSource.data().length-1){
									if(i===data.fileData.length-1){
										//desplegar el resultado de la busqueda de clientes en la grilla
										$scope.resultadoGrid.dataSource.data(_.values(movimientos));
										$scope.conf.archivoValido = true;//activar el boton para guardar la info
									}
									
							
								}	

							}	

						}
					}else{
						//TODO MOSTAR MENSAJE COMO CORRESPONDE
						console.log("limite de lineas configuradas sobrepasadas");
					}
					bcsComunes.ventanaOverlay(idVentana,false);

				});
		 	}else if($scope.PortafolioDataSource.data().length>0){
		 		$scope.conf.archivoValido = true;
		 	}
			
		 });
	};
	
	$scope.guardar = function(){
		bcsComunes.ventanaOverlay(idVentana,true);

		var movimientos = [];
		for (var i =0; i < $scope.resultadoGrid.dataSource.data().length ; i++){
			if($scope.resultadoGrid.dataSource.data()[i].VALIDO==="S"){
				if(!$scope.resultadoGrid.dataSource.data()[i].MEDIO_DE_PAGO){
					$scope.resultadoGrid.dataSource.data()[i].MEDIO_DE_PAGO = "";
				}
				if(!$scope.resultadoGrid.dataSource.data()[i].MODALIDAD_DE_PAGO){
					$scope.resultadoGrid.dataSource.data()[i].MODALIDAD_DE_PAGO = "";
				}
				
			}
			$scope.resultadoGrid.dataSource.data()[i].ID = i;
			movimientos.push($scope.resultadoGrid.dataSource.data()[i]);
		}


		var entradaSP = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_IdModulo : "FAC",
			p_Movimientos : movimientos
		};

		console.log(entradaSP);
		
		var urlCrearUsuario = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13016&c=1";
		var r = $resource("", {},
	    {
			'ConsultarEntidadEnrolamiento': { method: "POST", url: urlCrearUsuario}
		});
		
		var post = r.ConsultarEntidadEnrolamiento(entradaSP);
		
		post.$promise.then(function(result){
			console.log(result);

			if(result.P_OUT_MOVIMIENTOCOMPROBANTE && result.P_OUT_MOVIMIENTOCOMPROBANTE.length>0){
				for(var i in result.P_OUT_MOVIMIENTOCOMPROBANTE){
					for(var j =0; j < $scope.resultadoGrid.dataSource.data().length; j++){
						if($scope.resultadoGrid.dataSource.data()[j].ID === result.P_OUT_MOVIMIENTOCOMPROBANTE[i].NumeroMvtoCmbte){
							$scope.resultadoGrid.dataSource.data()[j].SECUENCIA_MOVIMIENTO = result.P_OUT_MOVIMIENTOCOMPROBANTE[i].IdMovimientoComprobante;
						}
					}	
				}
			}
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant((result.P_OUT_ERRORESCONTADOR>0) ? "UI_ERROR" : "UI_MOVIMIENTOS_FACTURAR"),
				conflictos:util.parseServerResponse(result)
			},idVentana);
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(){
			bcsComunes.msg("error",'Error',$translate.instant("UI_OPERACION_NO_VALIDA"));	
			bcsComunes.ventanaOverlay(idVentana,false);
		});

	};
	
});