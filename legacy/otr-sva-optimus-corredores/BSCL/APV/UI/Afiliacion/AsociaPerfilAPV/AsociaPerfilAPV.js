(function() {
	angular.module('ModuloAPV').controller('AsociaPerfilAPVCtrl',
	function($scope,$translate,$resource,bcsComunes,bcsServiciosAPV) {
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var apapv = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		apapv.oConfiguracionCategoriaInversionista = {
			filtros : { 
				IdEmpresa : { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa }
			}
		};
		
		apapv.oConfPerfilComercial = {
			filtros : { 
				IdEmpresa : { field: "IdEmpresa",  operator: "eq", value: $scope.SesionIdEmpresa },
				IdCategoriaInversionista : { field : "IdCategoriaInversionista", operator: "eq", value : "" },
			}
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		var dsAsociaPerfil = {
			type : "odata",
			definition : {
				serverFiltering : true,
                serverPaging    : true,
                serverSorting   : true,
                pageSize        : 25,
                transport: {
            		read: {
            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=6109",
						dataType:"json"
            		}
            	},
				schema : {
					model : {
						fields : {
							IdPerfilComercialAPV	: {type: "number"},
							IdEmpresa				: {type: "number"},
			    			IdCategoriaInversionista: {type: "string"},
			    			IdPerfilComercialAbierto: {type: "string"},
			    			IdPerfilComercialActivo	: {type: "string"},
			    			IdEstado				: {type: "string"}
			    		}
					}
				}
			},
		};
		
		/*********************************************
		 * GRILLAS
		 ********************************************/
		var dsGrillaAsociaPerfil = new bcs.DataSource(dsAsociaPerfil);
		apapv.configuracionGrillaAsociaPerfil = {
			dataSource: dsGrillaAsociaPerfil,
			columns: [
			     {field: "IdCategoriaInversionista",title: "UI_CATEGORIA_INVERSIONISTA",	width: "100px"}
				,{field: "IdPerfilComercialAbierto",title: "UI_PERFIL_COMERCIAL_ABIERTO",	width: "100px"}
				,{field: "IdPerfilComercialActivo",	title: "UI_PERFIL_COMERCIAL_ACTIVO",	width: "100px"}
			],
			sortable			: true,
			pageable			: true,
			filterable			: true,
			navigatable 		: true,
			userSettings 		: true,
			height				: 310,
			selectable 			: "row",
			autoBind 			: true
		};
		
		/*********************************************
		 * FUNCIONES
		 ********************************************/
		apapv.Buscar = function() {
			var filter = new Array();
			filter.push({ field: "IdEmpresa", operator: "eq", value: $scope.SesionIdEmpresa});
			filter.push({ field: "IdEstado", operator: "eq", value: 'VIG'});
			
			dsGrillaAsociaPerfil.filter(filter);
		};
		
		apapv.AsociarPerfilesAPV = function() {
			if (!apapv.IdCategoriaInversionista || apapv.IdCategoriaInversionista == '') {
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_CATEGORIA_INVERSIONISTA'));
				return;
			}
			if (!apapv.IdPerfilComercialBaseOrigenAbierto || apapv.IdPerfilComercialBaseOrigenAbierto == '') {
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_PERFIL_COMERCIAL_ABIERTO'));
				return;
			}
			if (!apapv.IdPerfilComercialBaseOrigenActivo || apapv.IdPerfilComercialBaseOrigenActivo == '') {
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_PERFIL_COMERCIAL_ACTIVO'));
				return;
			}
			if (apapv.IdPerfilComercialBaseOrigenAbierto == apapv.IdPerfilComercialBaseOrigenActivo) {
				bcsComunes.msg("error",$translate.instant("UI_ERROR"),$translate.instant('UI_DEBE_SELECCIONAR_PERFIL_COMERCIAL_DISTINTO'));
				return;
			}
			
			var parametros = {
				p_IdEmpresa	: $scope.SesionIdEmpresa,
				p_IdUsuario	: $scope.SesionIdUsuario,
				p_IdModulo	: "APV",
				p_AsociarPerfilesAPVIn	: [{
					"IdCategoriaInversionista": apapv.IdCategoriaInversionista,
					"IdPerfilComercialAbierto": apapv.IdPerfilComercialBaseOrigenAbierto,
					"IdPerfilComercialActivo": apapv.IdPerfilComercialBaseOrigenActivo
				}]
			};
			
			bcsComunes.ventanaOverlay(idVentana,true);
			bcsServiciosAPV.AsociarPerfilesAPV(parametros).$promise.then(function(respuesta){
		   		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		   		
				if (respuesta.P_OUT_ERRORESCONTADOR === 0){
					bcsComunes.msg("info","Informacion","Perfiles Comerciales asociados correctamente.");
				}
				else{
					bcsComunes.ventanaMsg({
						mensaje:$translate.instant("Existen errores al asociar Perfiles Comerciales."),
						conflictos:util.parseServerResponse(respuesta)
					},idVentana);
			    }
				
				bcsComunes.ventanaOverlay(idVentana,false);
				apapv.Buscar();
				
	   		},function(respuesta){
	   			bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				bcsComunes.msg("error","Error","Existen errores al asociar Perfiles Comerciales.");
	   		});
		};
		
	});
}());