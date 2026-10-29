angular.module('ModuloPlataforma').controller('ConsultaGeneralRegistroSistemaCtrl',
		function($scope,$translate,$modal,$log,$compile,
				$resource,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};
	$scope.debug = true;
	$scope.datasourceConfig = undefined;

	var overlay = bcsComunes.overlay;
	var idVentana = bcsComunes.ventanaWinId($scope);

	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';	
		
	$scope.exportarExcel = function(){
		bcsComunes.ventanaOverlay(idVentana,true);
		//obtener el datasource para el tipo de registro seleccionado
		var codigo = $scope.data.registroSeleccionado.CodigoRegistro;
		
		if(codigo==="USUARIOS_CLIENTE"){
			$scope.datasourceConfig = $scope.confConsultaUsuariosCliente;	
		}else if(codigo==="ACCESOS_USUARIOS_CLIENTE"){
			$scope.datasourceConfig = $scope.confGridAccesosUsuarios;
		}else if(codigo==="PERFILES_USUARIOS_CLIENTE"){
			$scope.datasourceConfig = $scope.confBasicaPerfilesUsuario;
		}else if(codigo==="ESTADO_PORTAFOLIO_CLIENTE"){
			$scope.datasourceConfig = $scope.portafolioDataSource;
		}else if(codigo==="PARAMETROS"){
			$scope.generarInformeParametrosSistema(); //SE PROGRAMA EL JOB PARA GENERAR INFORME
			bcsComunes.ventanaOverlay(idVentana,false);
			return false;
		}
		else{
			return false;
		}
		
		$scope.datasourceConfig.dataSource.read().then(function(){
			var data = _.map($scope.datasourceConfig.columns,function(element, index){
				return { field: element.field, title: $translate.instant(element.title), format: element.format};
			});
			$scope.datasourceConfig.dataSource.exportToExcel(data, $scope.data.registroSeleccionado.CodigoRegistro, $scope.data.registroSeleccionado.NombreRegistro);
			bcsComunes.ventanaOverlay(idVentana,false);
		});
	};
	
	/**
	 * Datasources para los tipos de registros
	 */
	//Consulta de Usuarios Internet Cliente y  Consulta de Accesos Usuarios Internet Cliente
	$scope.confConsultaUsuariosCliente = {
			dataSource: new bcs.DataSource({
				type: "odata", //Los tipos posibles son: odata, call, array y object
				definition: {
					serverFiltering: true, // Filtrado de datos en el servidor.
					serverPaging: false, // Paginado de los datos en el servidor.
					serverSorting: true, // Ordenamiento de los datos en el servidor.
					groupable: false,
					sort: {field: "CodigoUsuario", dir: "DESC"}, // Criterios de Ordenamiento.
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=384",
							dataType: "json",
							data:{
								$select: "\"CodigoUsuario\",\"NombreCompleto\",\"RazonSocial\",\"TipoDocumentoIdentidad\",\"DocumentoIdentidad\",\"TipoEntidad\",\"FechaIngreso\",\"Hora\""
							}
						}
					},
					schema: {
						model: {
							fields: {
								CodigoUsuario: {type: "string"},
								NombreCompleto: {type: "string"},
								RazonSocial: {type: "string"},
								TipoDocumentoIdentidad: {type: "string"},
								DocumentoIdentidad: {type: "string"},
								TipoEntidad: {type: "string"},
								FechaIngreso: {type: "date"},
								Hora: {type: "string"},
							}
						}
					},
					filter : [ 
   			          	// Configuración de filtros sobre los datos en el detalle.
						{ field: "TipoEntidad", operator: "eq", value: "CLI"},
						{ field: "EsAutorizado", operator: "eq", value: 'S'}
					]
				}
			}),
			columns: [
				{field: "CodigoUsuario",title: "UI_NOMBRE_USUARIO",format: "{0:n5}",width: "150px"}, 
				{field: "NombreCompleto",title: "UI_NOMBRE_COMPLETO",format: "{0:n5}",width: "150px"},
				{field: "RazonSocial",title: "UI_RAZON_SOCIAL",format: "{0:n5}",width: "150px"}, 
				{field: "TipoDocumentoIdentidad",title: "UI_TIPO_DOCUMENTO_IDENTIDAD",format: "{0:n5}",width: "150px"}, 
				{field: "DocumentoIdentidad",title: "UI_DOCUMENTO_IDENTIDAD",format: "{0:n5}",width: "150px"}, 
				{field: "TipoEntidad",title: "UI_TIPO_ENTIDAD",format: "{0:n5}",width: "150px",hidden: true}, 
				{field: "FechaIngreso",title: "UI_FECHA_INGRESO", format: "d", width: "150px"},
				{field: "Hora",title: "UI_HORA",width: "150px", format: "{0:n5}"}
				],
			gridCreated: function(grid) {
				$scope.data.mainGrid = grid;
				
			},
			pageable: false,
			filterable: true,
			excel: {
				allPages: true,
			}
		};
	
	//Consulta de perfiles de acceso por Usuario (cuándo se le dio acceso cada opción, quién le dio acceso)
	$scope.confGridAccesosUsuarios= {
			dataSource : new bcs.DataSource({
				type 		: "odata", //Los tipos posibles son: odata, call, array y object
				definition 	: {
					serverFiltering 	: true, // Filtrado de datos en el servidor.
					serverPaging 		: false, // Paginado de los datos en el servidor.
					serverSorting 		: true, // Ordenamiento de los datos en el servidor.
					groupable 			: false, 
					sort 				: { field: "CodigoUsuario", dir: "DESC" }, // Criterios de Ordenamiento.
					filter :  // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
						[
						 	{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
						 	{ field: "Existe ", operator: "eq", value: "S"}
						 ]
					,
					transport : {
								read : {
									url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1697",
									dataType : "json",
									data:{
										$select : "\"CodigoUsuario\",\"NombreAcceso\",\"TipoAcceso\",\"Existe\",\"CodigoAsignoUsuario\",\"FechaAsignacion\""
									}
								}
							},
					schema 				: {
						model: {
							fields : {
								CodigoUsuario 	: {type : "string"},
								NombreAcceso	: {type : "string"},
								TipoAcceso		: {type : "string"},
								Existe			: {type : "string"},
								CodigoAsignoUsuario: {type: "string"},
								FechaAsignacion: {type: "sysdatetime"}
							}
						}
					}
				}
			}),
			columns : [
					{field : "CodigoUsuario",title: "UI_USUARIO",width: "200px"},
					{field : "NombreAcceso",title: "UI_ACCESO",width: "350px"},
					{field : "TipoAcceso",title: "UI_CODIGO",width: "200px"},
					{field : "Existe",title: "UI_EXISTE",width: "150px"},
					{field : "CodigoAsignoUsuario",title: "UI_ASIGNO_USUARIO",width: "150px"},
					{field : "FechaAsignacion",title: "UI_FECHA_ASIGNACION",width: "150px"},
					
			],
			excel : {
				allPages: true,
			},
			pageable			: false,
			filterable			: true,
		};
	
	//Listado de pefiles y roles de todos los Usuarios con acecso al Sistema
	$scope.confBasicaPerfilesUsuario = {
			dataSource: new bcs.DataSource({
				type : "odata", //Los tipos posibles son: odata, call, array y object
				definition : {
					filter : [ // Configuración de filtros sobre los datos. Es equivalente a una operación "AND".
								{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa},
								{ field: "EsAutorizado", operator: "eq", value: 'S'}
							],
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging : false, // Paginado de los datos en el servidor.
					serverSorting : true, // Ordenamiento de los datos en el servidor.
					sort : { field: "CodigoUsuario", dir: "DESC" }, // Criterios de Ordenamiento.
					transport : {
						read : {
							url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1521",
							dataType : "json",
							data : {
								$select : "\"CodigoUsuario\",\"NombreRol\",\"NombrePerfil\",\"EstadoPerfil\""
							}
						}
					},
					schema : {
						model: {
							fields : {
								CodigoUsuario : { type : "string" },
								NombreRol : { type : "string" },
								NombrePerfil : { type : "string" },
								EstadoPerfil: {type: "string"}
							}
						}
					}
				}
			}),
			columns: [
				{field : "CodigoUsuario",title: "UI_USUARIO",width: "300px"},
				{field : "NombreRol",title: "UI_ROL",width: "300px"},
				{field : "NombrePerfil",title : "UI_PERFIL",width : "300px"},
				{field : "EstadoPerfil",title : "UI_ESTADO",width : "300px"}
			],
			sortable: true,
			pageable: false,
		};
		
	$scope.portafolioDataSource = {
		dataSource: new bcs.DataSource({
			type: "odata",
			definition : {
				filter : [
					{ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa}
				],
				serverFiltering : true, // Filtrado de datos en el servidor.
				serverPaging : false, // Paginado de los datos en el servidor.
				serverSorting : true, // Ordenamiento de los datos en el servidor.				
			    group: { field: "DocumentoIdentidad" },
				transport: {
					read: {   		            				
						url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1543",
						dataType:"json"
					}
				},
			    schema : {
					model : {
						fields : {
							DocumentoIdentidad : {type : "string"},									
							NombreCompleto: {type: "string"},
							CodigoPortafolio : {type : "string"},
							Proposito : {type : "string"},
							DescripcionPortafolio: {type: "string"},
							Estado : {type : "string"}
						}
					},
				}
			}
		}),
		columns: [
			{field : "DocumentoIdentidad",title: "UI_DOCUMENTO_IDENTIDAD",width: "300px"},
			{field : "NombreCompleto",title: "UI_NOMBRE",width: "300px"},
			{field : "CodigoPortafolio",title : "UI_CODIGO_PORTAFOLIO",width : "300px"},
			{field : "Proposito",title : "UI_PROPOSITO",width : "300px"},
			{field : "DescripcionPortafolio",title : "UI_DESCRIPCION_PORTAFOLIO",width : "300px"},
			{field : "Estado",title : "UI_ESTADO",width : "300px"}
		],
		sortable: true,
		pageable: false,
	};
	//SE PROGRAMA JOB QUE GENERA LISTADO DE TODOS LOS PARAMETROS DEL SISTEMA
	$scope.generarInformeParametrosSistema = function(){
		var datos = {
				p_IdEmpresa : $scope.SesionIdEmpresa,
				p_IdUsuario : $scope.SesionIdUsuario,
				p_IdModulo  : 'PLA',
				p_TipoPrograma : [{
					IdInterno : 0,
					IdTipoPrograma : 'GenerarInformeParametrosSistema',
					SegundosDesfase : 0
				}],
				p_Parametro : [{
					IdInterno : 0,
					NombreParametro : 'IdEmpresa',
					Valor : $scope.SesionIdEmpresa
				},{
					IdInterno : 0,
					NombreParametro : 'IdUsuario',
					Valor : $scope.SesionIdUsuario
				},{
					IdInterno : 0,
					NombreParametro : 'IdModulo',
					Valor : 'PLA'
				}]
		}
		bcsServicioPlataforma.Servicios().InsertarProcesoBatch(datos).$promise.then(
		function(result) {
			var fechaLocal=util.dateDS(result.P_OUT_PROGRAMA[0].FechaEjecucion,"FechaHora","CALL");
			bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_GENERACION_INFORME_BATCH_EXITOSO').replace("::IdPrograma",result.P_OUT_PROGRAMA[0].IdPrograma).replace("::FechaEjecucion",util.formatoSecondate(fechaLocal)),
				conflictos : util.parseServerResponse(result)}, idVentana);

		},
		function(result){
			bcsComunes.ventanaOverlay(idVentana,false);
			bcsComunes.msg("error","Error",$translate.instant('UI_GENERACION_INFORME_BATCH_ERRONEO'));
		});
	}
});	



