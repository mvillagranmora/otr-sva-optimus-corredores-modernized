(function() {
	angular.module('ModuloPlataforma').controller('CriterioSeleccionTasasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas,bcsAlerta,OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var cSTas 		= this;
		cSTas.idVentana	= bcsComunes.ventanaWinId($scope);
		cSTas.win		= {};
		cSTas.grilla	= {};
		cSTas.cmbbx		= {};
		cSTas.filtro	= {};
		cSTas.fn		= {};
		
		cSTas.filtro.ValorDef	= 'TODOS';
		cSTas.filtro.IdTipoCriterioTasaDef	= 'TODOS';
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		cSTas.win.agregar = function() {
			cSTas.fn.abrirModal(undefined, 'Agregar');
		};
		
		cSTas.win.modificar = function(criterioSel) {
			cSTas.fn.abrirModal(criterioSel, 'Modificar');
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		cSTas.cmbbx.TipoCartera={
			filtros: {
				IdEmpresa		: { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa },
				NombreEntidad	: { field: "NombreEntidad",	operator: "eq", value: "TIPOCARTERATASAS" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_TIPO_CARTERA'),
			},
		};
		
		cSTas.cmbbx.UsoCriterio={
			filtros: {
				IdEmpresa	: { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstado",	operator: "eq", value: "ACT" }
			}
		};
		
		// De Custodia se traen los Instrumentos con clasificacion RF e IF 
		var clasifTipoInstr	= [ { field: "IdClasificacionTipoInstrumento", operator: "eq", value: "RF" },
		                        { field: "IdClasificacionTipoInstrumento", operator: "eq", value: "IF" }];
		var estClasTipoInstr=	{ field: "IdEstado", operator: "eq", value: "ACT" };
		/* A futuro se traeran los demas valores del combo, asi que no se filtra por IdClasificacionTipoInstrumento, solo por Estado
		cSTas.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				complejo: [clasifTipoInstr, estClasTipoInstr],
				logic	: "and",
				filters	: [
				    { logic: "or", filters: clasifTipoInstr },
				    estClasTipoInstr
				]
			}
		};
		*/
		// El combo solo se filtra por el Estado
		cSTas.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				IdEstado: estClasTipoInstr
			}
		};
		
		cSTas.cmbbx.TipoCriterioTasa={
			filtros : {
				IdEmpresa	: { field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: { field: "IdEstado",	operator: "eq", value: "ACT" }
			}
		};
		
		cSTas.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MANTENEDORCRITERIOS" } 
			}
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		cSTas.grilla.CriterioSeleccionTasas = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition: {
					serverFiltering : true, // Filtrado de datos en el servidor.
					serverPaging 	: true, // Paginado de los datos en el servidor.
					serverSorting 	: true, // Ordenamiento de los datos en el servidor.
					sortable 		: true,
					pageSize 		: 100,
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=613",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdCriterioSeleccionTasas: { type: "number"},
								Prioridad				: { type: "number"},
								DscTipoCartera			: { type: "string"},
								Clasificacion			: { type: "string"},
								TipoInstrumento			: { type: "string"},
								//TipoOperacion			: { type: "string"},
								EmisorUI				: { type: "string"},
								MonedaUI				: { type: "string"},
								NemotecnicoUI			: { type: "string"},
								DiasAntiguedad			: { type: "number"},
								TipoCriterioTasa		: { type: "string"},
								CodDescFuente			: { type: "string"},
								TipoTasa				: { type: "string"},
								TipoPrecio				: { type: "string"},
								InterpolarUI			: { type: "string"},
								//Estado					: { type: "string"}
								Uso						: { type: "string"},
								IdContraparte			: { type: "number"},
								NombreContraparte		: { type: "string"}
							}
						}
					}
				}
			}),
			columns: [
			    {field: "Prioridad",		title: $translate.instant('UI_PRIORIDAD'),			width: "50px"	},
			    {field: "DscTipoCartera",	title: $translate.instant('UI_TIPO_CARTERA'),		width: "90px"	},
			    {field: "Uso",				title: $translate.instant('UI_USO_CRITERIO'),		width: "70px"	},
			    {field: "NombreContraparte",title: $translate.instant('UI_ENTIDAD'),			width: "130px"	},
			    {field: "Clasificacion",	title: $translate.instant('UI_CLASIFICACION'),		width: "110px"	},
			    {field: "TipoInstrumento",	title: $translate.instant('UI_TIPO_INSTRUMENTO'),	width:"110px"	},
			    {field: "TipoCriterioTasa",	title: $translate.instant('UI_TIPO_CRITERIO_TASA'),	width: "70px"	},
			    {field: "CodDescFuente",	title: $translate.instant('UI_FUENTE'),				width: "120px"	},
			    {field: "TipoTasa",			title: $translate.instant('UI_TIPO_TASA'),			width: "70px"	},
			    {field: "TipoPrecio",		title: $translate.instant('UI_TIPO_PRECIO'),		width: "70px", hidden: true	},
			    {field: "EmisorUI",			title: $translate.instant('UI_EMISOR'),				width: "60px",	template : "<span translate>#: EmisorUI # </span>"	},
			    {field: "MonedaUI",			title: $translate.instant('UI_MONEDA'),				width: "60px",	template : "<span translate>#: MonedaUI # </span>"	},
			    {field: "NemotecnicoUI",	title: $translate.instant('UI_NEMO'),				width: "60px",	template : "<span translate>#: NemotecnicoUI # </span>"	},
			    {field: "DiasAntiguedad",	title: $translate.instant('UI_DIAS_ANTIGUEDAD'),	width: "60px"	},
			    {field: "InterpolarUI",		title: $translate.instant('UI_INTERPOLAR'),			width: "60px",	template : "<span translate>#: InterpolarUI # </span>"	},
			],
			pageable	: true,
			pageSize	: 100,
			autoBind	: false,
			height		: 500,
		    selectable 	: "row",
		    excel : {
				allPages: true,
			},
		    filterable	: true,
		    gridCreated : function(grid) {
		       if ($scope.SesionLocale === 'CL'){
			         grid.showColumn("TipoPrecio");
			     }
			}
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		cSTas.fn.buscar = function(){
			var objfilters = { };
			var niv1filters = new Array();
			objfilters.logic = 'and';
			objfilters.filters = niv1filters;
	    	
			niv1filters.push({ field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa });
			niv1filters.push({ field: "Borrado",	operator: "eq", value: 'N' });
			
			if(cSTas.filtro.Valor){
				niv1filters.push({ field: "TipoCartera", operator: "eq", value: cSTas.filtro.Valor });
	    	}
			if(cSTas.filtro.IdUso){
				niv1filters.push({ field: "IdUso", operator: "eq", value: cSTas.filtro.IdUso });
	    	}
			if(cSTas.filtro.IdClasificacionTipoInstrumento){				
				niv1filters.push({ field: "IdClasificacionTipoInstrumento", operator: "eq", value: cSTas.filtro.IdClasificacionTipoInstrumento });
	    	}
			if(cSTas.filtro.IdTipoCriterioTasa){
				niv1filters.push({ field: "IdTipoCriterioTasa", operator: "eq", value: cSTas.filtro.IdTipoCriterioTasa });
	    	}
			/*
			if(cSTas.filtro.IdEstado){
	    		niv1filters.push({ field: "IdEstado", operator: "eq", value: cSTas.filtro.IdEstado });
	    	}
	    	*/
			
			if (niv1filters.length>0) cSTas.grilla.CriterioSeleccionTasas.dataSource.filters(objfilters);
	    	else cSTas.grilla.CriterioSeleccionTasas.dataSource.filters(new Array());
	    	
	    	cSTas.grilla.criterioSeleccionSel = undefined;
		};
		
		
		cSTas.fn.limpiar = function(){
			cSTas.filtro.Valor		= 0;
			cSTas.filtro.IdUso		= 0;
			cSTas.filtro.IdClasificacionTipoInstrumento = 0;
			cSTas.filtro.IdTipoCriterioTasa	= 0;
			cSTas.filtro.IdEstado	= 0;
			cSTas.grilla.CriterioSeleccionTasas.dataSource.removeAll();
		};
		
		
		cSTas.fn.eliminar = function(criterioSel){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdModulo 		= "PLA";
			parametrosSP.p_UsaReglasNegocio	= "N";
			
	        var t = bcsAlerta.crear("PREGUNTA_SI_NO",$translate.instant("UI_MENSAJE_ELIMINAR_CRITERIO"));
			t.result.then(function(result){
				if(result === OPCIONES_BCSALERTA.SI){
					bcsComunes.ventanaOverlay(cSTas.idVentana,true);
					
					parametrosSP.p_IdCriterioSeleccionTasas	= criterioSel.IdCriterioSeleccionTasas;
					parametrosSP.p_Borrado = 'S';
					
					parametrosSP.p_DetalleLog = [];
					parametrosSP.p_DetalleLog.push({SubRegistro:'CRITERIOSELECCIONTASAS', CampoRegistro:'EliminacionRegistro', ValorAntiguo:criterioSel.IdCriterioSeleccionTasas, ValorNuevo:criterioSel.IdCriterioSeleccionTasas});

					bcsServiciosTasas.modificarCriterioSeleccionTasas(parametrosSP,function (result){
						cSTas.fn.evaluaRespuestaSP(result, 'UI_CRITERIO_SELECCION_TASAS_ELIMINADO');
					}, function(resError){
						bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",$translate.instant("UI_REVISE_LOG"));
						bcsComunes.ventanaOverlay(cSTas.idVentana,false);
					});
				}
				else{
					cSTas.grilla.CriterioSeleccionTasas.dataSource.read();
					cSTas.grilla.criterioSeleccionSel = undefined;
					bcsComunes.ventanaOverlay(cSTas.idVentana,false);
				}
			});
		};
		
		
		cSTas.fn.abrirModal = function(criterioSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalCriterioSeleccionTasas",{ CriterioSel: criterioSel, AccionBoton : accion },false,true);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		cSTas.grilla.criterioSeleccionSel = undefined;
	        		cSTas.fn.buscar();
				}
	    	});
		};
		
		
		cSTas.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_CRITERIO_SELECCION_TASAS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_CRITERIO_SELECCION_TASAS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_CRITERIO_SELECCION_TASAS",$translate.instant("UI_REVISE_LOG"));
			
			//console.log(result);
			cSTas.grilla.criterioSeleccionSel = undefined;
			cSTas.fn.buscar();
			bcsComunes.ventanaOverlay(cSTas.idVentana,false);
		};
		
	});
}());