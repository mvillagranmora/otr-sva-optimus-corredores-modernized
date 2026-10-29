/*************************************************************
 * Purpose	: Controlador modal para consulta de notificaciones activadas de cada cliente
 * Author	: CHERNANDEZ - BCS
 * Date		: 10/04/2017
 * 
 * Modificaciones:
 * 
 * Author	: FPerales
 * Date		: 19/06/2018
 * Jira		: MBI - Parametrización de Perfiles Agentes / Supervisor
 *************************************************************/
angular.module('ModuloSocioNegocio').controller('ConsultarNotificacionClienteCtrl',	
		function($scope,$translate,$modal,bcsComunes,servicioSociodeNegocio)
{
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.data	= {};
	$scope.busqueda = {};
	
	$scope.data.oClientePortafolio ={
		tipo : "combobox-cliente-portafolio",
		configuracion :{},
		idActual : 0,
		seleccionado : false,
		filtros: {
			IdEstado : {"field": "IdEmpresa","operator": "eq", "value"   :  $scope.SesionIdEmpresa },
			EsCliente : {"field": "EsCliente","operator": "eq", "value"   :  'UI_SI'}
		},
	};
	
	$scope.confGrillaNotificacionesCliente = {
		autoBind: false,
  		allowCopy: true,
  		excel : {
			allPages: true,
		},
  		filterable: true,
  		groupable: true,
		height:420,
  		reorderable: true,
  		selectable: "row",
  		scrollable: true,
  		sortable: true,
  		userSettings:true,
		pageable: true,
		
		dataSource : new bcs.DataSource({
			type : "array",
			definition : {
				serverSorting : false,
				pageSize: 50,
				data : [],
				schema : {
					model : {
						fields : {	
							IdEmpresa 			: {type : "number"},
							DocumentoIdentidad 	: {type : "string"},
							Nombre 				: {type : "string"},
							Entidad 			: {type : "string"},
							DescripcionEntidad 	: {type : "string"},
							Notificacion 		: {type : "string"},
							EstadoNotificacion 	: {type : "string"},
							FechaActivacion 	: {type : "sysdate"}
						}
					}
				}
			}
		}),
		columns: [
			{field : "DocumentoIdentidad"	, title : "UI_DOCUMENTO_IDENTIDAD"	, width: "90px"},
			{field : "Nombre"				, title : "UI_NOMBRE"				, width: "200px"},
			{field : "Entidad"				, title : "UI_ENTIDAD"				, width: "50px"},
			{field : "DescripcionEntidad"	, title : "UI_DESCRIPCION"			, width: "100px"},
			{field : "Notificacion"			, title : "UI_NOTIFICACION"			, width: "100px"},
			{field : "EstadoNotificacion"	, title	: "UI_ESTADO"				, template: "<span translate> #:EstadoNotificacion #</span>",width: "50px"},
			{field : "FechaActivacion"		, title : "UI_FECHA_ACTIVACION"		, width: "50px"},
		],
		gridCreated : function(grid) {
			$scope.data.apiGridListadoClientes = grid;
		}
	};
	
	$scope.buscar = function(){
		$scope.data.apiGridListadoClientes.dataSource.data();
		
		if($scope.data.oClientePortafolio.seleccionado.IdCliente == undefined || $scope.data.oClientePortafolio.seleccionado.IdCliente == false){
			bcsComunes.msg("error", "Error","Debe seleccionar un cliente para consultar");
			return;
		}
		
		var datos = {
			p_IdEmpresa 			: $scope.SesionIdEmpresa,
			p_IdUsuario	 			: $scope.SesionIdUsuario,
			p_IdModulo 				: 'PLA',
			p_IdEmpleadoConsulta	: $scope.SesionIdEmpleado,
			p_IdCliente				: Number($scope.data.oClientePortafolio.seleccionado.IdCliente || 0),
			p_IdPortafolio			: Number($scope.data.oClientePortafolio.seleccionado.IdPortafolio || 0),
			p_estadoNotificacion	: $scope.busqueda.estado.toString(),
		}
		
		bcsComunes.ventanaOverlay(idVentana,true);
		
		servicioSociodeNegocio.ConsultarNotificacionesCliente(datos,function (result){
			if(!result.P_OUT_LISTACONFNOTIFICACIONES || result.P_OUT_LISTACONFNOTIFICACIONES.length == 0){
				bcsComunes.msg("warning", "Informacion"," La consulta no obtuvo resultados");
			}
			else {
				$scope.data.apiGridListadoClientes.dataSource.data(result.P_OUT_LISTACONFNOTIFICACIONES);
			}
			bcsComunes.ventanaOverlay(idVentana,false);
		},function(result){
			bcsComunes.msg("error", "Error","Existen problemas al ingresar la consulta");
			bcsComunes.ventanaOverlay(idVentana,false);	
		});
	};
		
	$scope.limpiarFiltros = function() {	
		$scope.data.apiGridListadoClientes.dataSource.removeAll();	
		$scope.data.oClientePortafolio.idActual = 0;
	};
	
});	