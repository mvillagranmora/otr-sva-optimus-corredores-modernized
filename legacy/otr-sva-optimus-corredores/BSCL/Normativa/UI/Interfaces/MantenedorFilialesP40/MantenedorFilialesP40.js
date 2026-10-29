angular.module('ModuloNormativa').controller('MantenedorFilialesP40Ctrl', 
function($scope,$timeout,$modal,$state,bcsComunes,bcsServicioPlataforma,
		$translate, bcsAlerta,bcsServicioInterfaces,OPCIONES_BCSALERTA)
{	
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
	
    var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);		
		var OperacionesOk = false;

		$scope.data = {};
		$scope.data.seleccionFilialP40 = false;
		
		//Configurar al principio
		$scope.HabilitoBotones = false;

		// SELECCION DE UN REGISTRO
		$scope.$watchCollection('data.seleccionFilialP40',function(newval,oldval){
			if(newval === undefined) return;
			if(newval === false) return;
			$scope.HabilitoBotones = true;
		});
		
		var Transaccion_OK = function(respuesta){
			if (respuesta.P_OUT_ERRORESCONTADOR === 0){		
				id = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			else{			
				id = bcsComunes.ventanaMsg({conflictos:util.parseServerResponse(respuesta)},$scope.winID);
			}
			bcsComunes.ventanaPromesa(id).then(function(result){
				bcsComunes.ventanaOverlay($scope.winID, false);
				// Vuelve a cargar datos en la grilla...
				$scope.data.BuscarFiliales();
			});			
		};
		
		var Transaccion_Err = function(respuesta){
			bcsComunes.ventanaMsg({
				conflictos:util.parseServerResponse(respuesta)
			},$scope.winID);
			bcsComunes.ventanaOverlay($scope.winID, false);
			$scope.data.BuscarFiliales();
		};

	var datos = {};
	datos.p_IdEmpresa = $scope.$root.SesionIdEmpresa; 
	datos.p_IdUsuario = $scope.$root.SesionIdUsuario; 
	datos.p_IdModulo = 'NOR';
	
/******************************************************/
/*** FILTROS										***/
/******************************************************/

	var dsGrillaMovimientos = new bcs.DataSource({
		type		: "odata",
		definition	: {
			serverFiltering	: true, // Filtrado de datos en el servidor.
			serverPaging	: true, // Paginado de los datos en el servidor.
			serverSorting	: true, // Ordenamiento de los datos en el servidor.
			pageSize		: 10,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700166",
					dataType : "json"
				}
			},
			schema			:  {
    		model: {
    			fields: {
					        Rut		        					: {type: "string"},
                			NombreFilial						: {type: "string"},
                			CodigoTenedor						: {type: "number"},
                			NombreZip       					: {type: "string"},
                			NombreArchivo  		     			: {type: "string"},
        		} 
        	}
		}    
	}
	});
	
/******************************************************/
/*** FUNCIONES DE CARGA DE TABLAS					***/
/******************************************************/
	
	$scope.data.confMantenedorFilialesP40 = {
		dataSource			: dsGrillaMovimientos,
		columns				:  [
    		      {field: "Rut", title: "UI_RUT", width: "50px", sortable : false, groupable: true  }
				 ,{field: "NombreFilial", title: "UI_NOMBRE_FILIAL", width: "150px", sortable : false, groupable: true  }
				 ,{field: "CodigoTenedor", title: "UI_CODIGO_TENEDOR", width: "60px", sortable : false, groupable: true }	
				 ,{field: "NombreZip", title: "UI_NOMBRE_ZIP", width: "100px", sortable : true, groupable: true }
				 ,{field: "NombreArchivo", title: "UI_NOMBRE_ARCHIVO", width: "100px", sortable : true, groupable: true }
		],		
		sortable			: true,
		filterable 			: true,
		userSettings 		: true,
		allowCopy			: { delimeter: "\t" },
		pageable			: true,
		height				: 370,
		selectable 			: "row",
	    excel : {
	        allPages: true,
	      },
	    gridCreated : function(grid) {
	    	grillaMovimientos = grid;
            dsGrillaMovimientos.read();
	    }
	};
	
		/******************************************************/
		/*** BUSCAR FILIALES								***/
		/******************************************************/
	
		$scope.data.BuscarFiliales = function(){
			var filters = new Array();
			filters.push({ field: "IdEmpresa", operator: "eq", value:datos.p_IdEmpresa });
			dsGrillaMovimientos.filter(filters);
			$scope.data.seleccionFilialP40 = false;
		};

        /*********************************************
		 * AGREGAR FILIALES
		 ********************************************/
		 	
		$scope.data.AgregarFiliales = function(){			 
			var IdVentanamodal = bcsComunes.ventanaAbrir("AgregarModificarFilialesP40",{});
			var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
			promesa.then(function(data){
				$scope.data.BuscarFiliales();
		    });
		};
	
        /*********************************************
		 * ELIMINAR FILIALES
		 ********************************************/
       
		$scope.data.EliminarFiliales = function(){
			if ($scope.data.seleccionFilialP40 === undefined || $scope.data.seleccionFilialP40 === false){	
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			}
			else{
				if ($scope.data.seleccionFilialP40.length > 1 ){
					bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
				}	
				else{
					var t = bcsAlerta.crear("PREGUNTA",$translate.instant('UI_SE_ELIMINARA_LA_FILIAL'));
					t.result.then(function(result){
						bcsComunes.ventanaOverlay($scope.winID, true);
						if (result===OPCIONES_BCSALERTA.ACEPTAR){
                            
								var parametros = {
				                p_IdEmpresa 				: $scope.SesionIdEmpresa,
				                p_IdUsuario 				: $scope.SesionIdUsuario,
				                p_IdModulo 					: 'NOR',
				                p_IdSocioDeNegocio			: $scope.data.seleccionFilialP40.IdSocioDeNegocio
								}
				                
								bcsComunes.log().info(JSON.stringify(parametros));	
							    bcsServicioInterfaces.EliminarFilialesP40(parametros, Transaccion_OK, Transaccion_Err)
						}
							else {
								bcsComunes.ventanaOverlay($scope.winID, false);
								$scope.data.BuscarFiliales();
							}					
					});
					return;
				}
			}
		};	
	
	    /*********************************************
		 * EDITAR FILIALES
		 ********************************************/
		$scope.data.EditarFiliales = function(){
		if ($scope.data.seleccionFilialP40 === undefined || $scope.data.seleccionFilialP40 === false){
				bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_UN_REGISTRO'));
			}if ($scope.data.seleccionFilialP40.length > 1 ){
					bcsComunes.msg("warning","info",$translate.instant('UI_DEBE_SELECCIONAR_SOLO_UN_REGISTRO'));
				}else{
		        var form = {};
		        form.RutFilial		    = $scope.data.seleccionFilialP40.Rut;
				form.NombreFilial		= $scope.data.seleccionFilialP40.NombreFilial;
				form.CodigoTenedor		= $scope.data.seleccionFilialP40.CodigoTenedor;
				form.NombreZip		    = $scope.data.seleccionFilialP40.NombreZip;
				form.NombreArchivo		= $scope.data.seleccionFilialP40.NombreArchivo;
				form.IdSocioDeNegocio   = $scope.data.seleccionFilialP40.IdSocioDeNegocio;
				var IdVentanamodal = bcsComunes.ventanaAbrir("AgregarModificarFilialesP40", form, false, false, idVentana);
				var promesa = bcsComunes.ventanaPromesa(IdVentanamodal);
				promesa.then(function(data){
				$scope.data.BuscarFiliales();
		        });
			}  
		};
/******************************************************/
/*** FINAL											***/
/******************************************************/
});