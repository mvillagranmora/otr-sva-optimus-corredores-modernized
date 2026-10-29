(function() {
	angular.module('ModuloPlataforma').controller('ModalGrupoEmisoresCtrl',
		function ($scope,bcsComunes,servicioLimites,$translate) {
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var modGE 		= this;
		modGE.idVentana	= bcsComunes.ventanaWinId($scope);
		modGE.parametros= bcsComunes.ventanaParams(modGE.idVentana);
		modGE.domVentana= $("[ventanaid='"+modGE.idVentana+"']");
		
		var lineaDetalle= _.clone(modGE.parametros.GrupoEmisorSel);
		
		modGE.win		= {};
		modGE.fn		= {};
		modGE.grilla	= {};
		modGE.cmbbx		= {};
		modGE.respuesta	= {};
		modGE.respuesta.IdEstadoDef = $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
		
		modGE.IdBotonAceptar = 'ModalGrupoEmisores_aceptar' + modGE.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(lineaDetalle){
			bcsComunes.ventanaTitulo(modGE.idVentana, $translate.instant('UI_MODIFICAR_GRUPO_EMISORES'));
			modGE.respuesta.IdGrupoEmisor	= lineaDetalle.IdGrupoEmisor;
			modGE.respuesta.Descripcion		= lineaDetalle.Descripcion;
			modGE.respuesta.IdEstado		= lineaDetalle.IdEstado || $scope.$root.SesionValoresDefectoSistema.IdEstadoLimiteCuentaPropia;
			modGE.respuesta.IdEstadoDef		= lineaDetalle.IdEstado;
			
			modGE.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(modGE.idVentana, $translate.instant('UI_AGREGAR_GRUPO_EMISORES'));
			modGE.win.modoEditar = false;
		}
		
		
		modGE.win.agregarEmisor = function() {
			modGE.fn.abrirModal(undefined, 'Agregar');
		};
		modGE.win.modificarEmisor = function(emisorSel) {
			modGE.fn.abrirModal(emisorSel, 'Modificar');
		};
		
		
		modGE.win.cancelar = function () {
			bcsComunes.ventanaData(modGE.idVentana, modGE.respuesta);
			bcsComunes.ventanaCerrar(modGE.idVentana);
		};
		
		
		modGE.win.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(lineaDetalle && lineaDetalle.IdGrupoEmisor) { // EDITAR
				datosGuardar.p_IdGrupoEmisor = lineaDetalle.IdGrupoEmisor;
				
				datosGuardar.p_RegistroCambio = [];
				if(modGE.respuesta.Descripcion !== lineaDetalle.Descripcion)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'Descripcion',ValorNuevo:modGE.respuesta.Descripcion});
				if(modGE.respuesta.IdEstado !== lineaDetalle.IdEstado)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:modGE.respuesta.IdEstado});
					
				if(datosGuardar.p_RegistroCambio.length === 0){
					modGE.respuesta.aceptar = true;
					bcsComunes.ventanaData(modGE.idVentana, modGE.respuesta);
					bcsComunes.ventanaCerrar(modGE.idVentana);
					return;
				}
				
				bcsComunes.ventanaOverlay(modGE.idVentana,true);
				servicioLimites.modificarGrupoEmisor(datosGuardar,function (result){
					modGE.fn.evaluaRespuestaSP(result, 'UI_GRUPO_EMISORES_MODIFICADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(modGE.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_IdGrupoEmisor= modGE.respuesta.IdGrupoEmisor;
				datosGuardar.p_Descripcion	= modGE.respuesta.Descripcion;
				datosGuardar.p_IdEstado		= modGE.respuesta.IdEstado;
				
				bcsComunes.ventanaOverlay(modGE.idVentana,true);
				servicioLimites.crearGrupoEmisor(datosGuardar,function (result){
					modGE.fn.evaluaRespuestaSP(result, 'UI_GRUPO_EMISORES_CREADO');
				}, function(resError){
					bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
					console.log(result);
					bcsComunes.ventanaOverlay(modGE.idVentana,false);
				});
			}
		};
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		if(modGE.win.modoEditar){
			modGE.domVentana.data("kendoWindow").setOptions({width: 600});
			modGE.domVentana.data("kendoWindow").setOptions({height: 360});
			
			modGE.grilla.Emisores = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering : true, // Filtrado de datos en el servidor.
						serverPaging 	: true, // Paginado de los datos en el servidor.
						serverSorting 	: true, // Ordenamiento de los datos en el servidor.
						sortable 		: true,
						pageSize 		: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=414",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdGrupoEmisor", operator: "eq", value: (lineaDetalle && lineaDetalle.IdGrupoEmisor) || -1  }
						],
						schema: {
							model: {
								fields: {
									IdGrupoEmisor	: { type: "number"},
									IdEmisor		: { type: "string"},
									RazonSocial		: { type: "string"},
									EstadoEmisor	: { type: "string"}
								}
							}
						}
					}
				}),
				columns: [
					{field: "IdEmisor",		title: $translate.instant("UI_EMISOR"),width: "100px"},
					{field: "RazonSocial",	title: $translate.instant("UI_RAZON_SOCIAL"),	width: "200px"},
					{field: "EstadoEmisor",title: $translate.instant("UI_ESTADO"),	width: "60px"}
				],
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 180,
			    selectable 	: "row"
			};
		}
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		modGE.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "LIMITES" }
			}
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		modGE.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","LIMITES CUENTA PROPIA",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","LIMITES CUENTA PROPIA",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo + ' : ' + result.P_OUT_ADVERTENCIASDETALLE[0].Descripcion);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","LIMITES CUENTA PROPIA",result.P_OUT_ERRORESDETALLE[0].Codigo + ' : ' + result.P_OUT_ERRORESDETALLE[0].Descripcion);
			}
			else bcsComunes.msg("error","LIMITES CUENTA PROPIA",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			modGE.respuesta.aceptar = true;
			bcsComunes.ventanaOverlay(modGE.idVentana,false);
			bcsComunes.ventanaData(modGE.idVentana, modGE.respuesta);
			bcsComunes.ventanaCerrar(modGE.idVentana);
		};
		
		
		modGE.fn.abrirModal = function(emisorSel, p_accion){
	        if(p_accion === 'Agregar' || (emisorSel && emisorSel.IdEstado !== 'PEN')){
				var id1 = bcsComunes.ventanaAbrir("ModalGrupoEmisorDetalle",{GrupoEmisorSel: lineaDetalle, EmisorSel: emisorSel, AccionBoton: p_accion},false,true);
				
				var promesa=bcsComunes.ventanaPromesa(id1);                               
		        promesa.then(function(respuesta){
		        	console.log(respuesta);
		        	if(respuesta.aceptar === true){
		        		modGE.grilla.Emisores.dataSource.read();
		        		modGE.grilla.emisorSel = undefined;
					}
		        	else console.log("cierra modal");
		    	});
			}
		};
	});
}());