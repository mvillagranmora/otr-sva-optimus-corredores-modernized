angular.module('ModuloPlataforma').controller('MantenedorMatrizPerfilComercialAdicionalCtrl',
		function ($scope, $translate, bcsComunes, bcsAlerta, servicioSociodeNegocio) {

	var mmpca		= this;
	mmpca.idVentana	= bcsComunes.ventanaWinId($scope);
	mmpca.win		= {};
	mmpca.grilla		= {};
	mmpca.grilla.PerfilSeleccionado = undefined;
	
	mmpca.getSchemaMatrizPerfilComercialAdicional = function () {
		return {
			model	: {
                fields	: {
					IdCategoriaInversionista	: { type: "string"	},
                	IdFormaCobro				: { type: "string"	},
                	CodigoPortafolio	 		: { type: "string"	},
                	IdPerfilComercialBase 		: { type: "string"	},
                	IdPerfilCobros 				: { type: "string"	},
                	IdPerfilProductos			: { type: "string"	},
                	IdEstado					: { type: "string"	},
                	PorDefecto					: { type: "string"	},
                	IdEmpresa					: { type: "number"	}
				}
       		}
		};
	};
	
   	mmpca.grilla.oConfiguracionGrilla = {
		dataSource		: new bcs.DataSource({
			type 		: "odata", // Los tipos posibles son: odata, call, array y object
			definition 	: {
				serverFiltering : true,
				serverPaging 	: true, 
				serverSorting 	: true, 
				sortable 		: true,
				pageSize 		: 100,
				transport: {
					read: {
						url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=700414",
						dataType: "json"
					}
				},
				filter: { field: "IdEmpresa", operator: "eq", value: 1 },
				schema 	: mmpca.getSchemaMatrizPerfilComercialAdicional()
			}
		}),
		columns			: [
			{field: "IdCategoriaInversionista",width: "30%",title: $translate.instant("UI_CATEGORIA_INVERSIONISTA"),attributes : {style : "text-align:left;"}},
			{field: "IdFormaCobro",width: "30%",title: $translate.instant("UI_FORMA_COBRO"),attributes : {style : "text-align:left;"}},
			{field: "CodigoPortafolio",width: "30%",title: $translate.instant("UI_PORTAFOLIO"), attributes: { style : "text-align:left;" }},
			{field: "IdPerfilComercialBase",width: "60%",title: $translate.instant("UI_MODELO_COMERCIAL"), attributes: { style : "text-align:left;" }},
			{field: "IdPerfilCobros",width: "30%",title: $translate.instant("UI_PERFIL_COBROS"), attributes: { style : "text-align:left;" }},	
			{field: "IdPerfilProductos",width: "30%",title: $translate.instant("UI_PERFIL_PRODUCTOS"), attributes: { style : "text-align:left;" }}	
		],		
		sortable			: true,
		pageable			: true,
		filterable			: true,
		autoBind			: true,  // comentar
		height				: 350,
		userSettings		: true,
		navigatable			: true,
		excel : {
			allPages: true,
		},
		selectable 			: "row"
	};
   	
	
	mmpca.fnAgregarPerfilComercialAdicional = function() {
		mmpca.abrirModal(undefined, 'Agregar');
	};
	
	mmpca.fnModificarPerfilComercialAdicional = function(perfilSeleccionado) {
		mmpca.abrirModal(perfilSeleccionado, 'Modificar');
	};
	
	mmpca.fnEliminarPerfilComercialAdicional = function(perfilSeleccionado) {
		mmpca.abrirModal(perfilSeleccionado, 'Eliminar');
	};

	mmpca.abrirModal = function(perfilSeleccionado, accion){
		if( accion === "Agregar" || accion === "Modificar" ) {
			var id1 = bcsComunes.ventanaAbrir( "AgregarModificarConfiguracionPerfilComercialAdicional", { AccionBoton : accion , ConfiguracionEnrolamiento: perfilSeleccionado}, false, false, mmpca.idVentana);

			var promesa = bcsComunes.ventanaPromesa(id1);                               
			$.when(promesa).then(function(data){
				bcsComunes.ventanaOverlay(mmpca.idVentana,false);
				mmpca.grilla.oConfiguracionGrilla.dataSource.read();
			});
			
		} else if (accion === "Eliminar") {
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_PERFIL_COMERCIAL_ADICIONAL"), mmpca.idVentana);
			confirmacion.then(function(result){
				if( result==1 ) {

					var registroCambio = [];
					registroCambio.push({CampoModificado : "IdEstado", ValorNuevo : "INA"});

					var datos = {
						p_IdEmpresa : $scope.SesionIdEmpresa,
						p_IdUsuario : $scope.SesionIdUsuario,
						p_IdModulo : 'SOC',
						p_UsaReglasNegocio : 'S',
						p_IdPerfilComercialAdicional: perfilSeleccionado.IdPerfilComercialAdicional,
						p_RegistroCambio : registroCambio,
					};

					bcsComunes.ventanaOverlay(mmpca.idVentana,true);
					servicioSociodeNegocio.eliminarConfiguracionPerfilComercialAdicional(datos).$promise.then(
						function(result) {
							let resp = util.parseServerResponse(result);
							let idMensaje = bcsComunes.ventanaMsg({ 
								mensaje : resp.errores.length == 0 ? "Perfil Comercial Adicional eliminado" : "Error al elimninar Perfil Comercial Adicional",
								conflictos : resp}, mmpca.idVentana);

							bcsComunes.ventanaPromesa(idMensaje).then(function(){
								bcsComunes.ventanaData(mmpca.idVentana,true);
								if (resp.errores.length == 0){
									bcsComunes.ventanaOverlay(mmpca.idVentana,false);
									mmpca.grilla.oConfiguracionGrilla.dataSource.read();
								}
							});
						},
						function(){
							bcsComunes.msg("error","error","Error al eliminar Perfil Comercial Adicional");
							bcsComunes.ventanaData(mmpca.idVentana,true);
							bcsComunes.ventanaOverlay(mmpca.idVentana,false);
							mmpca.grilla.oConfiguracionGrilla.dataSource.read();
						});
				}
			});
		}
	};

	bcsComunes.ventanaAutoHeight(mmpca.idVentana);
	
})