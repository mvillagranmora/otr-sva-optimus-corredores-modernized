angular.module('ModuloCuentaInversion').controller('MantenedorCuentaInversionCtrl',
		function($scope,$modal,$state,bcsComunes,$translate,$rootScope,
				bcsServiciosCuentaInversion,bcsServicioPlataforma)
{
	$scope.winID = bcsComunes.ventanaWinId($scope);
	bcsComunes.ventanaAutoOverlay($scope.winID,false,3000);
	var filtros = [];

	var dsSocioDeNegocio = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			pageSize 	        : 10,
			sort 				: [{ field: "NombreCliente", dir: "desc" },
			                       { field: "NombrePortafolio", dir: "desc" },
			                      ],
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1485",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						NombreCliente             : {type : "string"},
						IdEstadoCliente           : {type : "string"},
						NombreEstadoCliente       : {type : "string"},
						NombrePortafolio          : {type : "string"},
						IdEstadoPortafolio        : {type : "string"},
						NombreEstadoPortafolio    : {type : "string"},
						IdEstado           		  : {type : "string"},
						NombreEstado       		  : {type : "string"},
						IdCliente                 : {type : "number"},
						IdPortafolio              : {type : "number"},
						DocumentoIdentidad		  : {type : "string"},
					}
				}
			}
		}
	});	
	$scope.confSocioDeNegocio = {
		dataSource  : dsSocioDeNegocio,
		columns 	: [   {field : "NombreCliente",           title:"UI_CLIENTE",             width: "200px"}
				 ,{field : "DocumentoIdentidad",      title:"UI_DOCUMENTO_IDENTIDAD", width: "80px"}
		                 ,{field : "NombreEstadoCliente",     title:"UI_ESTADO_CLIENTE",      width: "60px"}
		                 ,{field : "NombrePortafolio", 	      title:"UI_PORTAFOLIO",          width: "130px"}
		                 ,{field : "NombreEstadoPortafolio",  title:"UI_ESTADO_PORTAFOLIO",   width: "60px"}
		                 //,{field : "NombreEstado", 	      	  title:"UI_ESTADO",          	  width: "150px"}
                      ],
		contextMenu : true,
		filterable	: true,
		sortable 	: true,
		pageable 	: true,
		height 		: 330,
		selectable 	: "multiple row",
		autoBind 	: true,
		userSettings: true
	};

    var dsMonedaClienteCuentaInversion = new bcs.DataSource({
        type : "odata",
		definition : {
			serverFiltering 	: true, // Filtrado de datos en el servidor.
			serverPaging 		: true, // Paginado de los datos en el servidor.
			serverSorting 		: false, // Ordenamiento de los datos en el servidor.
			sort 				: [{ field: "CuentaInversionPrincipal", dir: "desc" },
			                       { field: "NombreLargoMoneda", dir: "desc" },
			                      ],			
			transport 			: {read : {url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1489",dataType : "json"}},
			schema : {
				model : {
					fields : {								
						NombreLargoMoneda         : {type : "string"},
						CuentaInversionPrincipal  : {type : "string"},
						NombreEstado              : {type : "string"},
						IdCliente                 : {type : "number"},
						IdPortafolio              : {type : "number"},
					}
				}
			}
		}
	});	
	$scope.confMonedaClienteCuentaInversion = {
		dataSource  : dsMonedaClienteCuentaInversion,
		columns 	: [   {field : "NombreLargoMoneda",           title:"UI_MONEDA", 	        width: "150px"}		                 
		                 ,{field : "NombreEstado",                title:"UI_ESTADO",            width: "100px"}
		                 ,{field : "CuentaInversionPrincipal",    title:"UI_PRINCIPAL_CUENTA", 	width: "50px",	editable: true}
                      ],
		contextMenu : true,
		filterable	: false,
		sortable 	: true,
		pageable 	: false,
		height 		: 300,
		selectable 	: "multiple row",
		autoBind 	: false,
		userSettings: true,
		save		: function(e){
						if (e.values.CuentaInversionPrincipal.toUpperCase() !== "S" && e.values.CuentaInversionPrincipal.toUpperCase() !== "N"){
							bcsComunes.msg("error","error","UI_INDICADOR_INCORRECTO");
							e.values.CuentaInversionPrincipal = e.model.CuentaInversionPrincipal;
							e.preventDefault();							
							return;
						}
						e.model.CuentaInversionPrincipal = e.values.CuentaInversionPrincipal.toUpperCase();
						e.preventDefault();
					  },					 					  
		rowColorBackground	: function(row) { 
								 var color = "";

								 if (row.IdEstado === 'SAS')
								 	color = "red";
								 return color;
							  }
	};

	$scope.$watch('seleccionSocioDeNegocio', function(newValue, oldValue) {  
		if (angular.equals(newValue,oldValue)) return;
		if (newValue!==false) {
			dsMonedaClienteCuentaInversion.removeAll();
			$scope.seleccionMonedaClienteCuentaInversion = null;
			filtros = [];

			if (newValue[0].IdEstadoCliente !== 'ACT' || newValue[0].IdEstadoPortafolio !== 'ACT'){
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant("UI_SOCIO_O_PORTAFOLIO_NO_SE_ENCUENTRA_ACTIVO"));
				return;
			}

			filtros.push({ field: "IdCliente",      operator: "eq", value: newValue[0].IdCliente });
            filtros.push({ field: "IdPortafolio",   operator: "eq", value: newValue[0].IdPortafolio });
            dsMonedaClienteCuentaInversion.filter(filtros);
		}
	},true);


	arrayMonedaClienteCuentaInversion = function(accion){		
		var datos = {};
		var dataCuentaInversion = [];

		for(i=0; i< $scope.seleccionMonedaClienteCuentaInversion.length;i++){
			t = $scope.seleccionMonedaClienteCuentaInversion[i];

			dataCuentaInversion.push({IdCliente 				: t.IdCliente,
									  IdPortafolio				: t.IdPortafolio,
									  IdMoneda					: t.IdMoneda,
									  IdCuentaInversion			: t.IdCuentaInversion ? Number(t.IdCuentaInversion) : null,
									  Nombre					: '',
									  CuentaInversionPrincipal	: t.CuentaInversionPrincipal,
									  Accion					: accion
			});
		}

		datos.p_IdEmpresa			= $scope.SesionIdEmpresa;
		datos.p_IdUsuario			= $scope.SesionIdUsuario;
		datos.p_IdModulo			= 'CTA';		
		datos.p_UsaReglasNegocio	= 'S';
		datos.p_EsEjecucionManual	= 'S';
		datos.p_CuentaInversion		= dataCuentaInversion;

		return datos;
	};

	$scope.asignarCuentaInversion = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosCuentaInversion.GestionCuentaInversion(
			arrayMonedaClienteCuentaInversion('I'),
			function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					dsMonedaClienteCuentaInversion.filters(filtros);
				} else {	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_HUBIERON_PROBLEMAS_ASIGNAR_CUENTA_INVERSION"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ASIGNAR_CUENTA_INVERSION"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	}
	$scope.modificarCuentaInversion = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosCuentaInversion.GestionCuentaInversion(
			arrayMonedaClienteCuentaInversion('M'),
			function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					dsMonedaClienteCuentaInversion.filters(filtros);
				} else {	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_HUBIERON_PROBLEMAS_MODIFICAR_CUENTA_INVERSION"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID, false);
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_MODIFICAR_CUENTA_INVERSION"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	}
	$scope.activarCuentaInversion = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosCuentaInversion.GestionCuentaInversion(
			arrayMonedaClienteCuentaInversion('A'),
			function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					dsMonedaClienteCuentaInversion.filters(filtros);
				} else {	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_HUBIERON_PROBLEMAS_ACTIVAR_CUENTA_INVERSION"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID, false);	
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_ACTIVAR_CUENTA_INVERSION"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	}
	$scope.desactivarCuentaInversion = function(){
		bcsComunes.ventanaOverlay($scope.winID, true);
		bcsServiciosCuentaInversion.GestionCuentaInversion(
			arrayMonedaClienteCuentaInversion('D'),
			function(respuesta){	  		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){	
					bcsComunes.ventanaMsg({
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
					dsMonedaClienteCuentaInversion.filters(filtros);
				} else {	
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("UI_HUBIERON_PROBLEMAS_DESACTIVAR_CUENTA_INVERSION"),
						conflictos:util.parseServerResponse(respuesta)
					},$scope.winID);
				}
				bcsComunes.ventanaOverlay($scope.winID, false);	
			},
			function(respuesta){
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("UI_ERROR_DESACTIVAR_CUENTA_INVERSION"),
					conflictos:util.parseServerResponse(respuesta)
				},$scope.winID);
				bcsComunes.ventanaOverlay($scope.winID, false);
			}
		);
	}

	var firstWindow = $("#Win_MantenedorCuentaInversion");
		firstWindow.css("height", "444px");
		firstWindow.css("width", "940px"); 
});