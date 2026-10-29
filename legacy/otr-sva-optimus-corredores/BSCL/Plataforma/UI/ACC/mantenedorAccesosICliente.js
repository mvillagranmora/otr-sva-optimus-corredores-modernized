angular.module('ModuloPlataforma').controller('MantenedorAccesosICLienteCtrl',
		function($scope,$translate,$modal,$log,$resource,
				bcsComunes, bcsServicioPlataforma)
{

	$scope.data	= {};	
	var overlay = bcsComunes.overlay;
	
	//Seteo de variables de sistema
	$scope.data.IdEmpresa	= $scope.SesionIdEmpresa;
	$scope.data.IdUsuario	= $scope.SesionIdUsuario;
	$scope.data.IdModulo	= 'PLA';
	$scope.data.btnActivos  = true;   //Activa botones
	
	var idVentana = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana)+ " - "+ $scope.SesionEmpresa );
	
	$scope.showBloqueo = true;
	$scope.$watch("data.oConfiguracionAutorizados.seleccionado",
        function handleAutorizado(newValue, oldValue) {
			if(newValue !== undefined){
				$scope.showBloqueo = newValue.IdEstado === 'ACT' ? true : false;
			}
        }
    );
	
	$scope.bloquearAutorizado = function(){
        var datos = {
			p_IdEmpresa			: $scope.SesionIdEmpresa,
			p_IdUsuario			: $scope.SesionIdUsuario,
			p_IdModulo			: 'PLA',
			p_UsaReglasNegocio	: 'S',
			
			p_IdAutorizado	: $scope.data.oConfiguracionAutorizados.seleccionado.IdAutorizado,
			p_IdEstado		: 'BLO',
			p_Comentario	: 'Bloqueo por interfaz'
		};
        
        bcsComunes.ventanaOverlay(idVentana,true);
        cambioEstadoAutorizadoSP(datos).then(
        	function(result){
        		var titulo = "";
	        	if(result.P_OUT_ADVERTENCIASCONTADOR){
	        		titulo = "La operacion tiene advertencias";
	        	}else if(result.P_OUT_ERRORESCONTADOR){
	        		titulo = "La operacion tiene errores";
	        	}else{
	        		titulo = "Autorizado :: Bloqueado ::";
	        		$scope.gridAutorizados.dataSource.read();
	        	}
	        	bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
	        	
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        },
	        function(result){
	        	bcsComunes.ventanaMsg({
					mensaje: "La operacion tiene errores",
					conflictos:util.parseServerResponse(result)
				},idVentana);
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        });
    };
    
    $scope.desbloquearAutorizado = function(){
    	var datos = {
			p_IdEmpresa			: $scope.SesionIdEmpresa,
			p_IdUsuario			: $scope.SesionIdUsuario,
			p_IdModulo			: 'PLA',
			p_UsaReglasNegocio	: 'S',
			
			p_IdAutorizado	: $scope.data.oConfiguracionAutorizados.seleccionado.IdAutorizado,
			p_IdEstado		: 'ACT',
			p_Comentario	: 'Desbloqueo por interfaz'
		};
    	
    	bcsComunes.ventanaOverlay(idVentana,true);
    	cambioEstadoAutorizadoSP(datos).then(
        	function(result){
        		var titulo = "";
	        	if(result.P_OUT_ADVERTENCIASCONTADOR){
	        		titulo = "La operacion tiene advertencias";
	        	}else if(result.P_OUT_ERRORESCONTADOR){
	        		titulo = "La operacion tiene errores";
	        	}else{
	        		titulo = "Autorizado :: Desbloqueado ::";
	        		$scope.gridAutorizados.dataSource.read();
	        	}
	        	bcsComunes.ventanaMsg({
					mensaje: titulo,
					conflictos:util.parseServerResponse(result)
				},idVentana);
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        },
	        function(result){
	        	bcsComunes.ventanaMsg({
					mensaje: "La operacion tiene errores",
					conflictos:util.parseServerResponse(result)
				},idVentana);
	        	bcsComunes.ventanaOverlay(idVentana,false);
	        });
	};
	
	var cambioEstadoAutorizadoSP = function(p_datos){
		var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=162";
		var r = $resource("", {}, {'urlConsulta': {method:"POST", url:urlConsulta}});
		var post = r.urlConsulta(p_datos);
		return post.$promise;
    };
	
	//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
	$scope.gridAutorizados = {
		excel : {
			allPages: true
		},
		dataSource: new bcs.DataSource({
			type: "odata",
			definition: {
				serverFiltering : true, 
				serverPaging 	: true,
				serverSorting 	: true,
				sort 			: [{ field: "FechaCreacion", dir: "desc" }, { field: "IdPortafolio", dir: "desc" }],
				pageSize 		: 20,				
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1262",
						dataType: "json"
					}
				},
				filter: {
                    logic: "and",
                    filters: [
                    	{ field: "IdTipoRelacion", operator: "eq", value: 'AUTICL' }
                    ]
                },
				schema: {
					model: {
						fields: {
							IdAutorizado		: { type: "number"},
							IdPortafolio		: { type: "number"},
							CodigoPortafolio	: { type: "string"},
							IdPersona			: { type: "number"},
							IdTipoAutorizado	: { type: "string"},
							NombreTipoAutorizado: { type: "string"},
							IdEstado			: { type: "string"},
							Estado				: { type: "string"},
							IdEstadoAutorizacion: { type: "string"},
							FechaIngreso		: { type: "date"},
							FechaCreacion		: { type: "date"},
							FechaModificacion	: { type: "date"},
							IdUsuarioCreador	: { type: "number"},
							IdDocumentoIdentidad: { type: "number"},
							Tipo				: { type: "string"},
							NombreCompleto		: { type: "string"},
							Nombres				: { type: "string"},
							ApellidoPaterno		: { type: "string"},
							ApellidoMaterno		: { type: "string"},
							RazonSocial			: { type: "string"},
							DocumentoIdentidad	: { type: "string"},
							IdEstadoAutorizacion: { type: "string"},
							
							NumeroRegistroPublico	: { type: "string"},
							Asiento					: { type: "string"},
							FichaPartida			: { type: "string"},
							TipoFirma				: { type: "string"},
							TipoAcceso				: { type: "string"},
							AutorizadoPor			: { type: "string"},
							FirmaOptimusCliente		: { type: "string"},
							DocumentoIdentidadCLI   : { type: "string"},
							CodigoPropositoPortafolio  : { type: "string"}
						}
					}
				}
			}
		}),
		columns				: [
								{field: "NombreTipoAutorizado", title: $translate.instant('UI_TIPO_AUTORIZACION'), width: "100px"},
						    	{field: "AutorizadoPor", title: $translate.instant('UI_AUTORIZADO_POR'), width: "150px"},						    	
						    	{field: "IdPortafolio", title: $translate.instant('ID_PORTAFOLIO'), width: "80px"},
						    	{field: "CodigoPortafolio", title: $translate.instant('CODIGO_PORTAFOLIO'), width: "100px"},								
								{field: "DocumentoIdentidad", template:"<label> #:  util.formatRut(DocumentoIdentidad) #</label>", title: $translate.instant('UI_DOCUMENTO_IDENTIDAD'), width: "110px"},
						    	{field: "NombreCompleto", title: $translate.instant('UI_USUARIO_AUTORIZADO'), width: "150px"},
						    	{field: "TipoAcceso", title: $translate.instant('UI_TIPO_ACCESO'), width: "100px"},
								{field: "FirmaOptimusCliente", title: $translate.instant('UI_FIRMANTE_OPTIMUS_CLIENTE'), width: "120px", align:"center"},
						    	{field: "FechaCreacion", title: $translate.instant('UI_FECHA_CREACION'), width: "110px"},
						    	{field: "Estado", title: $translate.instant('UI_ESTADO'), width: "50px"},
						    	{field: "IdEstadoAutorizacion", title: $translate.instant('UI_AUTORIZADO'), width: "90px"}
				    		],
		sortable			: true,
		pageable			: true,
		autoBind			: true,
		filterable			: true,
		height				: 300,
	    selectable 			: "row"
	};
		
	//COMBOBOX Tipo Perfil
	$scope.data.oConfiguracionTipoPerfil={
		tipo : "combobox-tipo-perfil-portafolio",
		configuracion : {},
		filtros : {
			read : false,
			IdTipoRelacion : { field : "IdTipoRelacion", operator : "eq", value : "AUTICL" }
		},
		seleccionado : false,
		idDefecto : 'S'
	};
	
	$scope.buscar = function(){
		var filters = new Array();
		filters.push({ field: "IdTipoRelacion", operator: "eq", value: 'AUTICL' }); 
		//filters.push({ field: "IdEstado", operator: "eq", value: 'ACT' }); 
		if ($scope.data.oConfiguracionTipoPerfil.idActual != 0)
			filters.push({ field: "IdTipoAutorizado", operator: "eq", value: $scope.data.oConfiguracionTipoPerfil.idActual });  	
		$scope.gridAutorizados.dataSource.filter(filters);
	};	   
	
	$scope.opcionNoHabilitada = function() {
		bcsComunes.msg("warning","warning","Está opción no se encuentra habilitada.");
	}
	
	$scope.crearUsuario = function(){
		var id1 = bcsComunes.ventanaAbrir("IngresarAccesoICliente", {} ,false,false,idVentana);
	
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			$scope.gridAutorizados.dataSource.read();
		});
	};
	
	$scope.crearUsuarioRelacionado = function(){
		var id2 = bcsComunes.ventanaAbrir("IngresarAccesoIClienteRelacionado", {} ,false,false,idVentana);
	
		var promesa = bcsComunes.ventanaPromesa(id2);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			$scope.gridAutorizados.dataSource.read();
		});
	};	
	$scope.abrirDetalleAcc = function(items) {
		var id1 = bcsComunes
				.ventanaAbrir(
						"ModificarAccesoICliente",
						{
							IdCliente : items.IdCliente,
							IdPortafolio : items.IdPortafolio,
							IdAutorizado : items.IdAutorizado,
							IdTipoAutorizado : items.IdTipoAutorizado,
							DocumentoIdentidadCLI : items.DocumentoIdentidadCLI,
							AutorizadoPor : items.AutorizadoPor,
							CodigoPropositoPortafolio : items.CodigoPropositoPortafolio,
							IdTipoAutorizado : items.IdTipoAutorizado,
							FirmaOptimusCliente : items.FirmaOptimusCliente,
							NombreCompleto : items.NombreCompleto,
							DocumentoIdentidad : items.DocumentoIdentidad
						}  
						,false,false,idVentana);
		
		var promesa = bcsComunes.ventanaPromesa(id1);
		promesa.then(function(data){ 
			//Refrescar grilla para visualizar cambios.
			$scope.gridAutorizados.dataSource.read();
		});
		
		
	};
	
	
	
});	