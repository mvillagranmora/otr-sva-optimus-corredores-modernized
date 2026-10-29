angular.module('ModuloPlataforma').controller('MantenedorMensajeCtrl',
		function($rootScope,$scope,bcsComunes,bcsServicioPlataforma,$translate,$timeout)
	{
	const mmsj = this;
	mmsj.data={};
		
	//Seteo de variables de sistema
	mmsj.IdEmpresa	= $scope.SesionIdEmpresa;
	mmsj.IdUsuario	= $scope.SesionIdUsuario;
	mmsj.IdModulo	= 'PLA';
	
	mmsj.idVentana = bcsComunes.ventanaWinId($scope);
	
	
	$scope.abrirConfigurarMensaje = function(xMensaje){
		bcsComunes.ventanaOverlay(mmsj.idVentana,true);
		var id1 = bcsComunes.ventanaAbrir("ConfigurarMensajes", {
			Mensaje: xMensaje
		},false,false, mmsj.idVentana);
		var promesa = bcsComunes.ventanaPromesa(id1);                               
		promesa.then(function(data){
			if(data === true){
				mmsj.mensajeSeleccionado = undefined;
				mmsj.mainGrid.dataSource.read();
				bcsComunes.ventanaOverlay(mmsj.idVentana,false);
			}
		});
		
	};
	
	//GRILLA PRINCIPAL DE MENSAJES
	$scope.configuracionMensajes = {
		dataSource			: new bcs.DataSource({
			type 		: "odata", //Los tipos posibles son: odata, call, array y object
			definition 	: {
				serverFiltering 	: true, // Filtrado de datos en el servidor.
				serverPaging 		: true, // Paginado de los datos en el servidor.
				serverSorting 		: true, // Ordenamiento de los datos en el servidor.
				groupable 			: true, 
				//sort 				: { field: "FechaModificacion", dir: "asc" }, // Criterios de Ordenamiento.
				pageSize 			: 25, // Número de registros por página.
				transport 			: {
										read : {
											url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700310",
											dataType: "json"
										}
									},
				schema 				: {
					model: {
						fields : {
							IdMensaje: {type: "number"},
							Titulo: {type: "string"},
							NombreTipoMensaje: {type: "string"},
							NombreTipoSocioDeNegocio: {type: "string"},
							Texto: {type: "string"},
							FechaVigenciaDesde: {type: "date"},
							FechaVigenciaHasta: {type: "date"},
							Habilitado: {type: "string"},
							MuestraMensajeCorto: {type: "string"}
						}
					}
				}
			}
		}),
		columns	: [
			{field: "Titulo",title: "UI_TITULO",width: "150px"},
			{field: "NombreTipoMensaje",title: "UI_TIPO_MENSAJE",width: "90px"},
			{field: "NombreTipoSocioDeNegocio",title: "UI_TIPO_DESTINATARIO",width: "90px"},
			{field: "FechaVigenciaDesde",title: "UI_VIGENCIA_DESDE",width: "100px"},
			{field: "FechaVigenciaHasta",title: "UI_VIGENCIA_HASTA",width: "100px"},
			{field: "Habilitado",title: "UI_HABILITADO", width: "70px"},
			{field: "MuestraMensajeCorto",title: "UI_MUESTRA_MENSAJE_CORTO", width: "100px"}
		],
		gridCreated: function(grid) {
			mmsj.mainGrid = grid;
		},
		sortable			: true,
		pageable			: false,
		filterable			: true,
		groupable           : true,
		autoBind			: true,
		height				: 300,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable 			: "row"
	};
	
 
	$scope.limpiarGrillas = function ()
	{
		dataSourceConfiguracion.removeAll();
		dataSourceAsociados.removeAll();
  	 
	};
	
	$scope.agregarConfMensaje = function actualizarConfMensaje(){
		$scope.abrirConfigurarMensaje(undefined);
	};
	
	$scope.actualizarConfMensaje = function actualizarConfMensaje(msj){
		$scope.abrirConfigurarMensaje(msj);
	};
	
 
});
