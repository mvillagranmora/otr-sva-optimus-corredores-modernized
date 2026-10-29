/*************************************************************
 * Purpose	: Plantilla de Ejecutivo.
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloPlataforma').controller('PlantillaEmpleadoCtrl',function($scope,$translate,bcsComunes,$resource,servicioPlantillaEmpleado){

/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		$scope.data										= {};
		$scope.formData 								= {};	//GUARDA DATOS DEL FORMULARIO
		$scope.formData.gridMatrizProductosSeleccionado = {};
		$scope.dataPlantillaEmpleados					= {};
		$scope.dataPlantillaEmpleados.muestraEditar 	= false;
		$scope.dataPlantillaEmpleados.muestraCrear 		= true;
		$scope.dataPlantillaEmpleados.muestraGuardar 	= false;
		$scope.dataPlantillaEmpleados.muestraCancelar 	= false;
		$scope.dataPlantillaEmpleados.muestraCargarDatos = true;
		//
		$scope.pantalla 								= {}; // MODALIDADES DE PANTALLA
		$scope.pantalla.modoEditar 						= false;
		$scope.pantalla.muestraEditar 					= false;
		$scope.pantalla.modoLectura						= true;
		$scope.pantalla.plantillaEmpleados 				= {};
		$scope.pantalla.plantillaEmpleados.modoLectura	= true;
		var idVentana = bcsComunes.ventanaWinId($scope);
		//$scope.pantalla.modoBuscar = true;

/*********************************************
 * VARIABLES - CASO CARGA DESDE PORTAFOLIO
 ********************************************/	
		//datos de portafolio en caso de que cobros sea cargado desde portafolio!.
		$scope.datosGeneralesPortafolio = {};
		if($scope.admPort !== undefined) {
			$scope.datosGeneralesPortafolio = $scope.admPort.datosGenerales;
		};
		
/*********************************************
 * ACCIONES DE PANTALLA
 ********************************************/	
		$scope.pantalla.editar = function() {
			$scope.pantalla.modoEditar = true;
			$scope.pantalla.modoLectura = false;
			$scope.guardarOk = false;
		};
		
		$scope.pantalla.cancelar = function() {
			$scope.idPlantilla = '0';
			$scope.pantalla.modoEditar = false;
			$scope.pantalla.modoLectura = true;
			$scope.guardarOk = false;
		};
		
		$scope.dataPlantillaEmpleados.cargarDatos = function() {
			$scope.pantalla.perfilCargado = true;
			$scope.perfilCargado 	= true;
			cargaDatosPantalla($scope.dataPlantillaEmpleados.PlantillaEmpleadosObj.IdPlantillaEmpleado);
		}
		
		//CABECERA
		$scope.pantalla.administrarCabecera = function(perfilProductos) {
			var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaEjecutivos",{perfil:perfilProductos},false,false,idVentana); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(respuesta){ 
            	console.log(respuesta);
            	if(respuesta.res == true){
            		$scope.conf.plantillaEmpleados.oConfiguracionPlantillaEmpleados.filtros.read = true;
            		$scope.conf.plantillaEmpleados.oConfiguracionPlantillaEmpleados.idDefecto = respuesta.IdPlantillaEmpleado;
            		console.log(respuesta.IdPlantillaEmpleado + " ");//borrar
            		//cargaDatosPantalla($scope.datosCabecera.IdPerfilEmpleado);
            		cargaDatosPantalla(respuesta.IdPlantillaEmpleado);
            		$scope.guardarOk = true;
				}
				else {console.log("usuario cancela en el PopUp!");}
            });
		};

		$scope.pantalla.administrarDetalle = function(perfilProductos) {
			var id1 = bcsComunes.ventanaAbrir("modalAdministrarPlantillaEjecutivosDetalle",{IdPlantillaEmpleado: $scope.conf.plantillaEmpleados.oConfiguracionPlantillaEmpleados.idActual ,perfil:perfilProductos},false,false,idVentana); 
            var promesa=bcsComunes.ventanaPromesa(id1);                               
            $.when(promesa).then(function(respuesta){ 
            	console.log(respuesta);
            	if(respuesta){
            		cargaDatosPantalla($scope.dataPlantillaEmpleados.PlantillaEmpleadosObj.IdPlantillaEmpleado);
            		$scope.guardarOk = true;
				}
				else {console.log("usuario cancela en el PopUp!");}
            });
		};
	
		$scope.dataPlantillaEmpleados.cancelar = function() {
			$scope.pantalla.modalidad = '';
			$scope.pantalla.plantillaEmpleados.modoLectura = true;
			$scope.dataPlantillaEmpleados.muestraEditar 	= false;
			$scope.dataPlantillaEmpleados.muestraCrear 	= true;
			$scope.dataPlantillaEmpleados.muestraGuardar 	= false;
			$scope.dataPlantillaEmpleados.muestraCancelar 	= false;
			limpiarPantallaPlantillaEmpleados();
		};

/*********************************************
 * DATASOURCES
 *********************************************/
		$scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1335",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPlantillaEmpleado 			: { type: "string"}
							,Descripcion 					: { type: "string"}
							,FechaCreacion 					: { type: "date"}
							,FechaModificacion 				: { type: "date"}
							,IdEstado 						: { type: "string"}
							,IdEmpresa 						: { type: "int"}
							,IdUsuarioCreador 				: { type: "int"}
						}
					}
				}
			}
		});
		
		$scope.dataPlantillaEmpleados.PlantillaEmpleadosDataSource = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1329",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPlantillaEmpleado 			: { type: "string"}
							,Descripcion 					: { type: "string"}
							,FechaCreacion 					: { type: "sysdate"}
							,FechaModificacion 				: { type: "sysdate"}
							,IdEstado 						: { type: "string"}
							,Estado 						: { type: "string"}
							,IdTipoCargoEmpleado 			: { type: "number"}
							,IdEmpresa 						: { type: "number"}
							,IdUsuarioCreador 				: { type: "number"}
							,IdDetallePlantillaEmpleado 	: { type: "number"}
							,IdTipoProducto 				: { type: "string"}
							,TipoProducto 					: { type: "string"}
							,IdTipoOperacion				: { type: "string"}
							,TipoOperacion 					: { type: "string"}
							,Moneda 						: { type: "string"}
							,PorcentajeDescuentoComision 	: { type: "number"}
							,PorcentajeMargenEmpleado 		: { type: "number"}
							,PermiteRuteo					: { type: "string"}
							,MontoMaximoRuteo 				: { type: "number"}
							,OrdenDirecta 					: { type: "string"}
							,FechaCreacionDetalle 			: { type: "sysdate"}
							,IdEstadoDetalle 				: { type: "string"}
							,EstadoDetalle 					: { type: "string"}
						}
					}
				}
			}
		});
		
/*********************************************
 * COMBOBOX
 *********************************************/	
		$scope.conf = {};
		$scope.conf.plantillaEmpleados = {};
		
		$scope.conf.plantillaEmpleados.oConfiguracionPlantillaEmpleados={
			tipo : "combobox-plantilla-empleado",
			configuracion : {},
			filtros : {
				IdEmpresa : { field : "IdEmpresa", operator : "eq", value  : $scope.SesionIdEmpresa}
			},
			idActual : false,
			seleccionado : false,
			idDefecto : ''
		};

/*********************************************
 * GRILLAS
 *********************************************/
		//GRILLA DE PRODUCTOS.
		$scope.formData.optionsGrillaMatrizProductos = {
				dataSource		: new bcs.DataSource({
					type 		: "odata", //Los tipos posibles son: odata, call, array y object
					definition 	: {
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1567",
								dataType: "json"
							}
						},
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						sort			: { field: "TipoProducto", dir: "desc" },
		            	schema 			: {
							model	: {
			                    fields	: {
			                    	IdDetallePlantillaEmpleado 	: { type: "number"}
									,IdTipoProducto 				: { type: "string"}
									,TipoProducto 					: { type: "string"}
									,IdTipoOperacion				: { type: "string"}
									,TipoOperacion 					: { type: "string"}
									,Moneda 						: { type: "string"}
									,PorcentajeDescuentoComision 	: { type: "number"}
									,PermiteRuteo 					: { type: "string"}
									,NombrePermiteRuteo 			: { type: "string"}
									,MontoMaximoRuteo 				: { type: "number"}
									,MontoMaximoOperacion			: { type: "number"}
									,OrdenDirecta 					: { type: "string"}
									,FechaCreacionDetalle 			: { type: "sysdate"}
									,IdEstadoDetalle 				: { type: "string"}
									,EstadoDetalle 					: { type: "string"}
		                        }
	                   		}
						},
					}
				}),
				columns			: [
				 	{field: "TipoProducto",					width: "220px",	title: "UI_PRODUCTO"},
				 	{field: "TipoOperacion",				width: "220px",	title: "UI_TIPO_OPERACION"},
				 	{field: "EstadoDetalle",				width: "60px",	title: "UI_ESTADO"},
				 	{field: "NombrePermiteRuteo",			width: "60px",	title: "UI_PERMITE_RUTEO"},
				 	{field: "MontoMaximoOperacion",			width: "60px",	title: "UI_MONTO_MAXIMO_OPERACION"},
				 	{field: "PorcentajeDescuentoComision",	width: "60px",	title: "UI_DESCUENTO_COMISION", template: kendo.template("#=PorcentajeDescuentoComision# %")}
				],		
				sortable			: false,
				pageable			: true,
				autoBind			: false,  //comentar
				height				: 300,
				userSettings		: true,
				navigatable			: true,
				excel : {
					allPages: true,
				},
			    selectable 			: "row"
			 };

/*********************************************
 * FUNCIONES
 *********************************************/
		
		var cargaDatosPantalla = function(IdPlantillaEmpleado) {
			
			if (IdPlantillaEmpleado != undefined)
			{
				var filterPerfilProductos = [ { field: "IdPlantillaEmpleado", operator: "eq", value: String(IdPlantillaEmpleado) } ];
				$scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource.query({ filter: filterPerfilProductos	}).then(function(){
					if($scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource.view().length > 0)
					{
						$scope.dataPlantillaEmpleados.PlantillaEmpleadosObj.IdPlantillaEmpleado = $scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource.view()[0].IdPlantillaEmpleado;//codigo combobox
						$scope.dataPlantillaEmpleados.IdPlantillaEmpleado = $scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource.view()[0].IdPlantillaEmpleado;
						$scope.dataPlantillaEmpleados.Descripcion 	= $scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource.view()[0].Descripcion;
						$scope.dataPlantillaEmpleados.Habilitado = ($scope.dataPlantillaEmpleados.PlantillaEmpleadosCabeceraDataSource.view()[0].IdEstado === 'ACT') ? true : false;				
						$scope.formData.optionsGrillaMatrizProductos.dataSource.filter({ field: "IdPlantillaEmpleado", operator: "eq", value: String(IdPlantillaEmpleado) });
					}
					else {
						console.log("No hay datos");
					}
				});
			}
			else {
				bcsComunes.msg("warning","CARGAR DATOS","No ha seleccionado un dato.");
			}
		};
		
		$scope.dataPlantillaEmpleados.preparaDatosPlantillaEmpleadosGuardar = function(modalidad){
			var detalleRecorrer = [];
			if($scope.formData.optionsGrillaMatrizProductos.dataSource.data().length>0) {
				detalleRecorrer = $scope.formData.optionsGrillaMatrizProductos.dataSource.data();
			}
			if (detalleRecorrer.length>0){
				if( $scope.dataPlantillaEmpleados.IdPlantillaEmpleado!== undefined &&
					$scope.dataPlantillaEmpleados.Descripcion !== undefined)
				{
					var plantillaEmpleados = {};
					plantillaEmpleados.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa;
					plantillaEmpleados.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
					plantillaEmpleados.p_IdModulo 				= "PLA";
					plantillaEmpleados.p_simularOperacion 		= "N";
					plantillaEmpleados.p_UsaReglasNegocio 		= "N";
					//												  
					plantillaEmpleados.p_IdPlantillaEmpleado 	= $scope.dataPlantillaEmpleados.IdPlantillaEmpleado;
					plantillaEmpleados.p_Descripcion 			= $scope.dataPlantillaEmpleados.Descripcion;
					plantillaEmpleados.p_IdEstado 				= ($scope.dataPlantillaEmpleados.Habilitado === true) ? 'ACT' : 'INA';
					plantillaEmpleados.p_IdTipoCargoEmpleado 	= 0;
					//
					var p_DetallePerfil = [];
					$.each(detalleRecorrer,function(key,value){
						var lineaDetalle = {};
						lineaDetalle.p_IdTipoProducto  				= (value.IdTipoProducto === 'TODOS'  || value.IdTipoProducto  === undefined) ? '0' : value.IdTipoProducto;
						lineaDetalle.p_IdTipoOperacion 				= (value.IdTipoOperacion === 'TODOS' || value.IdTipoOperacion === undefined) ? '0' : value.IdTipoOperacion;
						lineaDetalle.p_PorcentajeDescuentoComision  = value.PorcentajeDescuentoComision;
						lineaDetalle.p_PorcentajeMargenEmpleado 	= 0;
						lineaDetalle.p_PermiteRuteo 				= 'N';
						lineaDetalle.p_MontoMaximoRuteo 			= 0;
						lineaDetalle.p_OrdenDirecta 				= 'N';
						lineaDetalle.p_IdEstado 					= value.IdEstadoDetalle;
						p_DetallePerfil.push(lineaDetalle);
					});
					if(p_DetallePerfil.length>0) {
						plantillaEmpleados.p_DetallePlantilla=p_DetallePerfil;
						return plantillaEmpleados;
					}
					else return {};
				}
				else {
					bcsComunes.msg("warning","GUARDAR PERFIL DE PRODUCTOS","No se han ingresado todos los datos.");
				}
			}
			else {
				bcsComunes.msg("warning","GUARDAR PERFIL DE PRODUCTOS","No hay lineas de detalle asociadas al nuevo perfil de cobros");
			}
		};

		var limpiarPantallaPlantillaEmpleados = function(){
			$scope.dataPlantillaEmpleados.IdPlantillaEmpleado							= undefined;
			$scope.dataPlantillaEmpleados.Descripcion 									= undefined;
			$scope.dataPlantillaEmpleados.PorDefecto 									= undefined;
			$scope.dataPlantillaEmpleados.Habilitado 									= undefined;
			$scope.conf.plantillaEmpleados.oConfiguracionPlantillaEmpleados.idActual	= undefined;
			$scope.conf.plantillaEmpleados.oConfiguracionPlantillaEmpleados.idDefecto	= undefined;
			$scope.detalleProductoSeleccionado 											= '';
			$scope.formData.optionsGrillaMatrizProductos.dataSource.removeAll();
		};
		
		$scope.dataPlantillaEmpleados.producto = {};
		$scope.dataPlantillaEmpleados.itemSeleccionadoProducto = {};
		$scope.dataPlantillaEmpleados.itemSeleccionadoProducto.dataProducto = {};
	});
}());
//------------------------- FIN CONTROLLER PRINCIPAL -----------------------------

(function() {
	angular.module('ModuloPlataforma').factory('servicioPlantillaEmpleado',['$resource',function($resource) {
		var urlGetTipoProductos 			= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=1541";
		var urlGetTipoOperacion 			= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=1542";
		var urlSavePerfilEmpleado 			= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=143";
		var urlSavePerfilEmpleadoDetalle 	= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=301";
		var urlModifyPerfilEmpleado 		= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=302";
		var urlModifyPerfilEmpleadoDetalle 	= "/BSCL/Plataforma/GWJAVA/PLA/call?c=1&t=303";
		return $resource("", {},
		{
			'getPerfilProductos'			: {method: "GET"	,url:urlGetTipoProductos},
			'getTipoOperacion'				: {method: "GET"	,url:urlGetTipoOperacion},
			'savePerfilEmpleado'			: {method: "POST"	,url:urlSavePerfilEmpleado},
			'savePerfilEmpleadoDetalle'		: {method: "POST"	,url:urlSavePerfilEmpleadoDetalle},
			'modifyPerfilEmpleado'			: {method: "POST"	,url:urlModifyPerfilEmpleado},
			'modifyPerfilEmpleadoDetalle'	: {method: "POST"	,url:urlModifyPerfilEmpleadoDetalle}
		});
	}]);
}()); 
