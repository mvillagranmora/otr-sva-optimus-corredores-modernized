angular.module('ModuloMesa').controller('ConsultarSolicitudPuntasCtrl', 
function($scope,$timeout,$modal,$state,bcsComunes, bcsBlotterSolicitudPuntasGrid, bcsServiciosMesa,
		bcsServicioReportes,$translate, bcsAlerta, OPCIONES_BCSALERTA,$http)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
	$scope.filtro = {};
	$scope.data ={};	
	$scope.seleccionSolicitudPuntas = {};
	var idVentana = bcsComunes.ventanaWinId($scope);
	
	$scope.filtro.fecInicio 		 =  util.dateInicioDia($scope.SesionFechaSistema);
	$scope.filtro.fecTermino		 =  util.dateInicioDia($scope.SesionFechaSistema);
		
	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa;
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; 
	datos.p_IdModulo = 'CUS';
	datos.p_UsaReglasNegocio = 'S';
	$scope.usuarioPermiteDistribucion = 'X';
	$scope.usuarioPermiteInterbancario = 'X';
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/

	$scope.data.oConfiguracionEstadoSolicitud={
		tipo : "combobox-estado-solicitudes-puntas",
		configuracion : {},
		filtros : {		
		},
		idActual : false,
		seleccionado : false,
		idDefecto : 'PEN'
	};
	
	var dsGrillaSolicitudes = new bcs.DataSource({
		type		: "odata",
		definition	: {
			serverFiltering	: true, // Filtrado de datos en el servidor.
			serverPaging	: true, // Paginado de los datos en el servidor.
			serverSorting	: true, // Ordenamiento de los datos en el servidor.
			sort			: { field: "IdSolicitudPuntas", dir: "desc" },
			//filter			: { field: "IdEstado", operator: "contains", value: ['PEN','PDA'] },
			pageSize		: 100,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=44290884",
					dataType : "json"
				}
			},
			schema			: modeloSolicitudes()
		}
	});
	
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.data.confBasicaSolicitud = {
		dataSource			: dsGrillaSolicitudes,
		columns				: bcsBlotterSolicitudPuntasGrid.columnasSolicitudes(),		
		sortable			: true,
		filterable 			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: true,
		height				: 470,
		selectable 			: "multiple row",
	    excel : {
	        allPages: true,
	      },
	    gridCreated : function(grid) {	    	
	    	
	    	grillaSolicitudes = grid;
	    	
	    	$scope.filtro.estadoObj = {};
	    	$scope.filtro.estadoObj.IdEstado = 'PEN';
	    	
	    	$scope.buscarSolicitudes(false);
	    	
	    }
	};
	
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
	
	$scope.buscarSolicitudes = function(sinFiltrosGrilla){
		
		var filters = new Array();
		
		
		if(!sinFiltrosGrilla){	

			if ($scope.filtro.estadoObj.IdEstado !== undefined  && $scope.filtro.estadoObj.IdEstado != "0")
			filters.push({ field: "IdEstadoOperacion", operator: "eq", value: $scope.filtro.estadoObj.IdEstado });			
	    	
	    	var auxDate1;
	    	var auxDate2;    	
	
	    	if ($scope.filtro.fecInicio !== undefined && $scope.filtro.fecInicio !== "" && $scope.filtro.fecInicio !== null)
	    	{
	    		auxDate1 =  util.dateOdata(util.date($scope.filtro.fecInicio).hours(0).minutes(0).seconds(0).toDate());
				filters.push({ field: "FechaRegistro", operator: "gte", value: auxDate1 });
	    	}
	    	
	    	if ($scope.filtro.fecTermino !== undefined && $scope.filtro.fecTermino !== "" && $scope.filtro.fecTermino !== null)
	    	{
	    		auxDate2 = util.dateOdata(util.date($scope.filtro.fecTermino).add(23,'hours').minutes(59).seconds(59).toDate());
	    	 	if (auxDate2 < auxDate1)
	    	 	{
	    	 		bcsComunes.msg('error',$translate.instant("UI_ERROR"), $translate.instant("FECHA_TERMINO_RANGO_CONSULTA_INVALIDA"));
	    	 		return;
	    	 	}
	    		filters.push({ field: "FechaRegistro", operator: "lte", value: util.dateTerminoDia(auxDate2) });
	    	}
		}
			
		dsGrillaSolicitudes.filter(filters);
	};
	
	
	$scope.limpiarFiltros = function() {
		
		$scope.filtro.fecInicio	= util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.fecTermino = util.dateInicioDia($scope.SesionFechaSistema);
		$scope.filtro.estadoObj.IdEstado = '';
	};
	
	$scope.aprobarSolicitud = function(datos) {
		
		if (datos.length > 1)
		{
			bcsComunes.ventanaMsgErr($translate.instant("UI_DEBE_SELECCIONAR_UNICO_REGISTRO"),idVentana);
			return;
		}
		
		var ventanaModalId = bcsComunes.ventanaAbrir("IngresarEnvioPuntas",datos[0], false,false,idVentana);	
		
		var promesa=bcsComunes.ventanaPromesa(ventanaModalId);	        
        promesa.then(function(){ 
        	$scope.buscarSolicitudes();     	
    	});

	};
	
	$scope.rechazarSolicitud = function()	
	{
		if ($scope.seleccionSolicitudPuntas.length > 1)
		{
			bcsComunes.ventanaMsgErr($translate.instant("UI_DEBE_SELECCIONAR_UNICO_REGISTRO"),idVentana);
			return;
		}
		
		var datos = {};	
		bcsComunes.ventanaOverlay(idVentana,true);
		
		datos.p_IdEmpresa 				= $scope.SesionIdEmpresa; 
		datos.p_IdUsuarioAutorizador	= $scope.SesionIdUsuario;
		datos.p_IdSolicitudPuntas		= $scope.seleccionSolicitudPuntas[0].IdSolicitudPuntas;						
		datos.p_IdEstado 				= 'RZD';
		datos.p_TipoCambioCostoAprobado	= 0;	
		datos.p_ObservacionAutorizador 	= '';		

		 bcsServiciosMesa.AprobarRechazarSolicitudPuntas(datos,function (result){				
		 	bcsComunes.ventanaMsg({
		 		conflictos:util.parseServerResponse(result)
		    	},idVentana);	
		 	$scope.buscarSolicitudes();
		 }, function(resError){
		 	bcsComunes.msg("error","UI_ERROR_SOLICITUD_PUNTAS",$translate.instant("UI_REVISE_LOG"));						
		 });
	}
	
	var error = function(respuesta){
		bcsComunes.ventanaMsg({
			conflictos:util.parseServerResponse(respuesta)
		},idVentana);
		bcsComunes.ventanaOverlay(idVentana, false);
		bcsComunes.ventanaCerrar(idVentana);
	};
	
	cargaConfiguracionUsuario = function(){
			//Se carga la configuración del usuario	
			var tablaDatosUsuario = [];	
			var ObjUsuario = { "IdUsuario" : $scope.SesionIdUsuario };
			tablaDatosUsuario.push(ObjUsuario);

			var parametros = {};
			parametros.p_IdEmpresa		= $scope.SesionIdEmpresa;
			parametros.p_in_Usuarios	= tablaDatosUsuario;

	       	bcsServiciosMesa.ConsultarConfiguracionUsuario(parametros,function(respuesta){
	       		$.each(respuesta.P_OUT_CONFIGURACION,function(key,value){
					if (value.Codigo === "PERMITEDISTRI") $scope.usuarioPermiteDistribucion = value.Valor;
					if (value.Codigo === "PERMITEINTERB") $scope.usuarioPermiteInterbancario = value.Valor;					
				});		       	
	       	}
   			,error);
	    };
	   
	  cargaConfiguracionUsuario();
	
/******************************************************/
/*** FINAL											***/
/******************************************************/
});