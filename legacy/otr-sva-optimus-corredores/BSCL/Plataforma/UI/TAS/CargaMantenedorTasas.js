(function() {
	angular.module('ModuloPlataforma').controller('CargaTasasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosTasas,bcsAlerta,OPCIONES_BCSALERTA){

		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var cTas 		= this;
		cTas.idVentana	= bcsComunes.ventanaWinId($scope);
		cTas.win		= {};
		cTas.grilla		= {};
		cTas.cmbbx		= {};
		cTas.filtro		= {};
		cTas.fn			= {};
		
		cTas.filtro.Fecha = util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		
		var idEstadoAux = '';
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		cTas.win.agregar = function() {
			cTas.fn.abrirModal(undefined, 'Agregar');
		};
		
		cTas.win.modificar = function(precioTasaSel) {
			if(precioTasaSel.length > 1){
				bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_MODIFICAR_UNA_TASA"));
			}
			/*else if(precioTasaSel[0].IdEstado === 'OFI' || precioTasaSel[0].IdEstado === 'PUB'){
				bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_MODIFICAR_TASA_OFI_PUB"));
			}*/
			else{
				cTas.fn.abrirModal(precioTasaSel[0], 'Modificar');
			}
		};
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		var dsDefPrecioTasa = new bcs.DataSource({
			type : "array", //Los tipos posibles son: odata, call, array y object
			definition: {
				serverFiltering : false,
				serverPaging 	: false,
				serverSorting 	: false,
				schema: {
					model: {
						fields: {
							IdPrecioTasa	: { type: "number"},
							IdFuente		: { type: "number"},
							CodigoFuente	: { type: "string"},
							CodDescFuente	: { type: "string"},
							//Emisor			: { type: "string"},
							Clasificacion	: { type: "string"},
							Nemotecnico		: { type: "string"},
							IdMoneda		: { type: "string"},
							IdTipoReajuste	: { type: "string"},
							MonedaFormato	: { type: "string"},
							TipoRegistro	: { type: "string"},
							TipoTasa		: { type: "string"},
							Tasa			: { type: "number"},
							TasaEstimada	: { type: "number"},
							TipoPrecio		: { type: "string"},
							Precio			: { type: "number"},
							Plazo			: { type: "number"},
							Estado			: { type: "string"},
							IdClasificacionTipoInstrumento	: { type: "string"},							
							FechaSolicitud		: { type: "sysdatetime"},
							FechaOficializacion	: { type: "sysdatetime"},
							FechaPublicacion	: { type: "sysdatetime"},
							CodigoUsuarioCreador: { type: "string"},
							CodigoUsuarioOficializador	: { type: "string"}
						}
					}
				}
			},
		});
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		/*
		cTas.cmbbx.TipoTasa={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'}
			}
		};
		*/
		
		cTas.cmbbx.Fuente={
			filtros: {
				IdEmpresa	: {field: "IdEmpresa",	operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado	: {field: "IdEstado",	operator: "eq", value: 'ACT'},
				NombreEntidad:{field: "NombreEntidad",	operator: "eq", value: 'MANTENEDOR'}
			}
		};
		
		// De Custodia se traen los Instrumentos con clasificacion RF e IF 
		var clasifTipoInstr	= [ { field: "IdClasificacionTipoInstrumento", operator: "eq", value: "RF" },
		                        { field: "IdClasificacionTipoInstrumento", operator: "eq", value: "IF" }];
		var estClasTipoInstr=	{ field: "IdEstado", operator: "eq" , value: "ACT" };
		/* A futuro se traeran los demas valores del combo, asi que no se filtra por IdClasificacionTipoInstrumento, solo por Estado
		cTas.cmbbx.ClasificacionTipoInstrumento={
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
		cTas.cmbbx.ClasificacionTipoInstrumento={
			filtros : {
				IdEstado: estClasTipoInstr
			}
		};
		
		cTas.cmbbx.TipoRegistro={
			filtros: {
				IdEmpresa		: { field: "IdEmpresa",		operator: "eq", value: $scope.$root.SesionIdEmpresa },
				NombreEntidad	: { field: "NombreEntidad",	operator: "eq", value: "TIPOREGISTROPRECIOTASA" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_TIPO_REGISTRO'),
			},
		};
		
		cTas.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MANTENEDORTASAS" } 
			}
		};
		
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		
		var dsPrecioTasa = new bcs.DataSource(dsDefPrecioTasa);
		
		cTas.grilla.PreciosTasas = {
			dataSource: dsPrecioTasa,
			reorderable	: true,
			userSettings: true,
			scrollable	: true,
			pageable	: true,
			//pageSize	: 100,
			sortable	: true,
			autoBind	: false,
			height		: 480,
		    selectable 	: "multiple row",
			groupable	: true,
		    excel : {
				allPages: true,
			},
		    filterable	: true,
			navigatable	: true,
			columns: [
			    {field: "CodDescFuente",title: $translate.instant('UI_FUENTE'),			width: "140px"	,groupable: true	},
			    //{field: "Emisor",		title: $translate.instant('UI_EMISOR'),			width: "140px"	},
			    {field: "Clasificacion",title: $translate.instant('UI_CLASIFICACION'),	width: "140px"	,groupable: true	},
			    {field: "Nemotecnico",	title: $translate.instant("UI_NEMO"),			width: "120px"	,groupable: true	},
			    {field: "IdMoneda",		title: $translate.instant("UI_MONEDA"),			width: "50px"	,groupable: true	},
				{field: "IdTipoReajuste",title: $translate.instant("UI_TIPO_REAJUSTE"),	width: "50px"	,groupable: true	},
			    {field: "TipoRegistro",	title: $translate.instant("UI_TIPO_REGISTRO"),	width: "70px"	,groupable: true	},
			    {field: "TipoTasa",		title: $translate.instant("UI_TIPO_TASA"),		width: "60px"	,groupable: true	},
			    {field: "Tasa",			title: $translate.instant("UI_TASA"),			width: "100px"  ,template: "<label> #: TipoRegistro === 'TASA' || TipoRegistro === 'PUNTO' ? (util.bcs_formato(Tasa,'TAS','TASA',MonedaFormato,'','')).valor : '' #</label>"	,groupable: true	},
			    //{field: "TasaEstimada",	title: $translate.instant("UI_TASA_ESTIMADA"),	width: "100px",	template: "<label> #: (util.bcs_formato(TasaEstimada,'TAS','TASA',IdMoneda,'','')).valor #</label>"},
			    {field: "TipoPrecio",	title: $translate.instant("UI_TIPO_PRECIO"),	width: "60px"	,groupable: true	},
			    {field: "Precio",		title: $translate.instant("UI_PRECIO"),			width: "100px"  ,template: "<label> #: TipoRegistro === 'PRECIO' ? (util.bcs_formato(Precio,'TAS','PRECIO',MonedaFormato,'','')).valor : '' #</label>"	,groupable: true	},
			    {field: "Plazo",		title: $translate.instant("UI_PLAZO_TASA"),		width: "50px"	,groupable: true	},
			    {field: "Estado",		title: $translate.instant("UI_ESTADO"),			width: "120px"  ,groupable: true		},
			    {field: "FechaSolicitud",title:$translate.instant("UI_FECHA_CREACION_SOLICITUD"),	width: "110px"	,groupable: true	},
			    {field: "FechaOficializacion",title:$translate.instant("UI_FECHA_OFICIALIZACION"),	width: "110px"	,groupable: true	},
			    {field: "FechaPublicacion",title:$translate.instant("UI_FECHA_PUBLICACION"),width: "110px"	,groupable: true	},
			    {field: "IdClasificacionTipoInstrumento",	title: $translate.instant("UI_MERCADO"),			width: "120px"	,groupable: true	},
				{field: "CodigoUsuarioCreador",title: $translate.instant('UI_USUARIO_CREADOR'),			width: "140px"	,groupable: true	},
				{field: "CodigoUsuarioOficializador",title: $translate.instant('UI_USUARIO_OFICIALIZADOR'),	width: "140px"	,groupable: true	},
			],
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		$scope.$watch('cTas.filtro.UltimaCurvaPublicada',function(newval,oldval) {
			if(newval){
				idEstadoAux = cTas.filtro.IdEstado;
				cTas.filtro.IdEstado = 'PUB';
			}
			else{
				cTas.filtro.IdEstado = idEstadoAux;
			}
		});
		
		
		cTas.fn.buscar = function(){
			if(!cTas.filtro.UltimaCurvaPublicada && (!cTas.filtro.Fecha || cTas.filtro.Fecha === '')){
				bcsComunes.msg("error",$translate.instant("UI_MANTENEDOR_TASAS"),"Debe ingresar valor en filtro "+$translate.instant("UI_FECHA_CREACION_SOLICITUD")+'.');
				return;
			}
			
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa			= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario			= $scope.$root.SesionIdUsuario;
			parametrosSP.p_FechaSolicitud		= cTas.filtro.UltimaCurvaPublicada ? '0001-01-01' : util.dateSPdate(cTas.filtro.Fecha);
			parametrosSP.p_CodigoFuente			= cTas.filtro.Codigo ? cTas.filtro.Codigo : '';
			parametrosSP.p_IdClasificacionTipoInstrumento = cTas.filtro.IdClasificacionTipoInstrumento ? cTas.filtro.IdClasificacionTipoInstrumento : '';
			parametrosSP.p_IdMoneda				= cTas.filtro.IdMoneda ? cTas.filtro.IdMoneda : '';
			parametrosSP.p_TipoRegistro			= cTas.filtro.Valor ? cTas.filtro.Valor : '';
			parametrosSP.p_IdEstado				= cTas.filtro.IdEstado ? cTas.filtro.IdEstado : '';
			parametrosSP.p_UltimaCurvaPublicada	= cTas.filtro.UltimaCurvaPublicada ? 'S' : 'N';
			
			bcsComunes.ventanaOverlay(cTas.idVentana,true);
			
			bcsServiciosTasas.consultarPrecioTasa(parametrosSP,function(result){
				if(result.P_OUT_ERRORESCONTADOR === 0){
					dsPrecioTasa.data(result.P_OUT_PRECIOTASA);
				}
				bcsComunes.ventanaOverlay(cTas.idVentana,false);
				
			}, function(resError){
				bcsComunes.msg("error",$translate.instant("UI_MANTENEDOR_TASAS"),$translate.instant("UI_REVISE_LOG"));
				bcsComunes.ventanaOverlay(cTas.idVentana,false);
			});
	    	cTas.grilla.precioTasaSel = undefined;
		};
		
		
		cTas.fn.limpiar = function(){
			cTas.filtro.Fecha				= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
			//cTas.filtro.TipoTasa			= 0;
			cTas.filtro.Codigo				= 0;
			cTas.filtro.IdClasificacionTipoInstrumento = 0;
			cTas.filtro.IdMoneda			= 0;
			cTas.filtro.UltimaCurvaPublicada= false;
			cTas.filtro.Valor				= 0;
			cTas.filtro.IdEstado			= 0;
			cTas.grilla.PreciosTasas.dataSource.removeAll();
		};
		
		
		cTas.fn.leerArchivo = function(){
			var id1 = bcsComunes.ventanaAbrir("ModalCargarArchivoTasas",{ IdClasTipoInst: cTas.filtro.IdClasificacionTipoInstrumento, FechaCreSol: cTas.filtro.Fecha, Fuente: cTas.dataFiltros.FuenteObj, TipoRegistro: cTas.dataFiltros.TipoRegistroObj },false,false,cTas.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		cTas.grilla.precioTasaSel = undefined;
	        		cTas.fn.buscar();
				}
	    	});
		};
		
		
		cTas.fn.cambioEstado = function(estado){
			var parametrosSP = {};
			parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
			parametrosSP.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			parametrosSP.p_IdModulo 		= "PLA";
			parametrosSP.p_UsaReglasNegocio	= "N";
			parametrosSP.p_Estado			= estado;
			
			var msjAlerta = '';
	        if(cTas.grilla.precioTasaSel && cTas.grilla.precioTasaSel.length){	// Registros seleccionados
	        	msjAlerta = $translate.instant("UI_MENSAJE_"+estado+"_TASAS_SELECCIONADOS");
	        	parametrosSP.p_TablaTasa = cTas.grilla.precioTasaSel;
	        }
	        else{	// Todos los registros
	        	msjAlerta = $translate.instant("UI_MENSAJE_"+estado+"_TASAS_TODOS");
	        	parametrosSP.p_TablaTasa = cTas.grilla.PreciosTasas.dataSource.data();
	        }
	        
	        if(parametrosSP.p_TablaTasa && parametrosSP.p_TablaTasa.length){
	        	var t = bcsAlerta.crear("PREGUNTA_SI_NO",msjAlerta);
				t.result.then(function(result){
					if(result === OPCIONES_BCSALERTA.SI){
						bcsComunes.ventanaOverlay(cTas.idVentana,true);
						
						bcsServiciosTasas.modificarEstadoTasas(parametrosSP,function (result){
							cTas.fn.evaluaRespuestaSP(result, 'UI_TASAS_'+estado);
						}, function(resError){
							bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_REVISE_LOG"));
							bcsComunes.ventanaOverlay(cTas.idVentana,false);
						});
					}
					else{
						cTas.grilla.PreciosTasas.dataSource.read();
						cTas.grilla.precioTasaSel = undefined;
						bcsComunes.ventanaOverlay(cTas.idVentana,false);
					}
				});
	        }
	        else{
	        	bcsComunes.msg("error","UI_MANTENEDOR_TASAS","SIN_REGISTROS");
	        }
		};
		
		
		cTas.fn.abrirModal = function(precioTasaSel, accion){
			var id1 = bcsComunes.ventanaAbrir("ModalCargaTasas",{ PrecioTasaSel: precioTasaSel, AccionBoton : accion },false,false,cTas.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);
	        promesa.then(function(respuesta){
	        	if(respuesta){
	        		cTas.grilla.precioTasaSel = undefined;
	        		cTas.fn.buscar();
				}
	    	});
		};
		
		
		cTas.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_MANTENEDOR_TASAS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_MANTENEDOR_TASAS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_MANTENEDOR_TASAS",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_MANTENEDOR_TASAS",$translate.instant("UI_REVISE_LOG"));
			
			//console.log(result);
			cTas.grilla.precioTasaSel = undefined;
    		cTas.fn.buscar();
			bcsComunes.ventanaOverlay(cTas.idVentana,false);
		};
		
	});
}());