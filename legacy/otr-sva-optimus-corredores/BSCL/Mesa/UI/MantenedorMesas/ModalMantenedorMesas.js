(function() {
	angular.module('ModuloMesa').controller('ModalMantenedorMesasCtrl',
	function($scope,$translate,bcsComunes,bcsServiciosMesa,bcsConfiguracionSociodeNegocio){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var mMM 		= this;
		mMM.idVentana	= bcsComunes.ventanaWinId($scope);
		mMM.parametros	= bcsComunes.ventanaParams(mMM.idVentana);
		mMM.domVentana	= $("[ventanaid='"+mMM.idVentana+"']");
		
		var mesaSel = _.clone(mMM.parametros.MesaSel);
		
		mMM.win			= {};
		mMM.cmbbx		= {};
		mMM.fn			= {};
		mMM.grilla		= {};
		mMM.dataIFRS    = {};
		
		mMM.datosMesa	= {};
		mMM.datosMesa.IdEstadoDef = 'ACT';
		mMM.dataSocioDeNegocio = {};
		
		mMM.IdBotonAceptar = 'ModalMantenedorMesas_aceptar' + mMM.parametros.AccionBoton;
		

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(mesaSel){
			bcsComunes.ventanaTitulo(mMM.idVentana, $translate.instant('UI_MODIFICAR_MESA'));
			mMM.datosMesa.IdMesa			= mesaSel.IdMesa || 0;
			mMM.datosMesa.Nombre			= mesaSel.Nombre || '';
			mMM.datosMesa.Codigo			= mesaSel.Codigo || '';
			mMM.datosMesa.IdCliente			= mesaSel.IdCliente;
			mMM.datosMesa.IdTipoProductoMesaModulo		= mesaSel.IdTipoProductoMesaModulo || 0;
			mMM.datosMesa.IdTipoProductoMesaModuloDef	= mesaSel.IdTipoProductoMesaModulo || 0;
			mMM.datosMesa.ValorRazonable	= mesaSel.ValorRazonable || 0;
			mMM.datosMesa.ValorRazonableDef	= mesaSel.ValorRazonable || 0;
			mMM.datosMesa.IdEstado			= mesaSel.IdEstadoRegistro || 'ACT';
			mMM.datosMesa.IdEstadoDef		= mesaSel.IdEstadoRegistro || 'ACT';
			mMM.datosMesa.PorDefecto		= mesaSel.PorDefecto === 'S' ? true : false;
			mMM.datosMesa.CodigoCarteraIFRS = mesaSel.CodigoIFRS;
			mMM.datosMesa.AplicaFusion		= mesaSel.AplicaFusion === 'S' ? true : false;
						
			// Se consulta el Socio de Negocio del Cliente de Mesa
			// Se comenta ya que se utiliza por defecto el Cliente de la Cuenta Propia 
			/*
			var socioDeNegocioClienteDatasource = new bcs.DataSource({
				type : "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1051",
							dataType: "json"
					    }
					},
					filter: { field: "IdCliente", operator: "eq", value: mMM.datosMesa.IdCliente },
					schema: bcsConfiguracionSociodeNegocio.getSchemaSocioDeNegocioRelacionado()
				}
			});
			socioDeNegocioClienteDatasource.query().then(function(result){
				if(socioDeNegocioClienteDatasource.data().length > 0){
					mMM.dataSocioDeNegocio = $.extend({},socioDeNegocioClienteDatasource.data()[0]);
					mMM.datosMesa.IdSocioDeNegocio	= mMM.dataSocioDeNegocio.IdSocioDeNegocio;
					mMM.datosMesa.IdPortafolio		= mesaSel.IdPortafolio || 0;
					mMM.datosMesa.IdPortafolioDef	= mesaSel.IdPortafolio || 0;
				}
			});
			*/
			mMM.datosMesa.IdSocioDeNegocio		= $scope.$root.SesionCorredoraIdSocioDeNegocio;
			mMM.datosMesa.IdSocioDeNegocioDef	= $scope.$root.SesionCorredoraIdSocioDeNegocio;
			mMM.datosMesa.IdPortafolio			= mesaSel.IdPortafolio || 0;
			mMM.datosMesa.IdPortafolioDef		= mesaSel.IdPortafolio || 0;
			// hasta aca por el comentario anterior
			
			mMM.win.modoEditar = true;
		
			if (mMM.datosMesa.IdTipoProductoMesaModulo === "MERF")
				{
					mMM.win.AplicaFusionDisable =false
				}else{
					mMM.win.AplicaFusionDisable =true
				}
			}
		else{
			bcsComunes.ventanaTitulo(mMM.idVentana, $translate.instant('UI_AGREGAR_MESA'));
			mMM.datosMesa.IdSocioDeNegocio		= $scope.$root.SesionCorredoraIdSocioDeNegocio;
			mMM.datosMesa.IdSocioDeNegocioDef	= $scope.$root.SesionCorredoraIdSocioDeNegocio;
			mMM.win.modoEditar = false;
		
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		if(mMM.win.modoEditar){
			mMM.domVentana.data("kendoWindow").setOptions({width: 600});
			mMM.domVentana.data("kendoWindow").setOptions({height: 480});
			
			mMM.grilla.OperadoresMesa = {
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
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=458",
								dataType: "json"
							}
						},
						filter: [
						    { field: "IdMesa", operator: "eq", value: mesaSel.IdMesa || -1 }
						],
						schema: {
							model: {
								fields: {
									IdAgenteOperadorMesa: { type: "number"},
									NombreMesa			: { type: "string"},
									CodigoNombreAgOp	: { type: "string"},
									NombreCargoAgOp		: { type: "string"},
									EstadoRelacion		: { type: "string"}
								}
							}
						}
					}
				}),
				columns: [
					{field: "NombreMesa",		title: $translate.instant("UI_MESA"),	width: "80px"},
					{field: "CodigoNombreAgOp",	title: $translate.instant("UI_AGENTE_OPERADOR"),width: "140px"},
					{field: "NombreCargoAgOp",	title: $translate.instant("UI_CARGO"),	width: "90px"},
					{field: "EstadoRelacion",	title: $translate.instant("UI_ESTADO"),	width: "50px"}
				],
				sortable	: false,
				pageable	: true,
				autoBind	: true,
				height		: 180,
			    selectable 	: "row"
			};
		}
		else{
			mMM.domVentana.data("kendoWindow").setOptions({height: 300});
		}
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		mMM.cmbbx.Cliente={
			filtros: {
				IdEmpresa : { field: "IdEmpresa",operator: "eq", value: $scope.$root.SesionIdEmpresa },
				EsCliente : { field: "EsCliente",operator: "eq", value: 'UI_SI' },
				EsCuentaPropia	: { field: "EsCuentaPropia",	operator: "eq", value: 'UI_SI' },
				IdSocioDeNegocio: { field: "IdSocioDeNegocio",	operator: "eq", value : $scope.$root.SesionCorredoraIdSocioDeNegocio },
				EsNombreFantasia: { field: "EsNombreFantasia",	operator: "eq", value : 'N' },
			}
		};
		
		mMM.cmbbx.Portafolio={
			filtros: {
				IdCliente : { field: "IdCliente",operator: "eq", value: -1 }
			}
		};
		
		mMM.cmbbx.ValorRazonable={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro", operator: "eq", value: "ValorRazonable" }
			},
			configuracion : {
				placeholder: $translate.instant('UI_VALOR_RAZONABLE'),
			},
		};
		
		mMM.cmbbx.Estado={
			filtros : {
				NombreEntidad : { field: "toupper(NombreEntidad)", operator: "eq", value: "MESA" }
			}
		};
		
		mMM.dataIFRS.oConfiguracionClasificacionIfrs = {
			tipo : "combobox-codigo-ifrs",
			configuracion : {
				//template: '<span >#: Codigo # - #: Nombre #</span>'
			},
			filtros : {
				Codigo : { "field"   : "Codigo","operator": "eq", "value"   : "" },
			}
		};

		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		mMM.fn.cancelar = function () {
			
			bcsComunes.ventanaData(mMM.idVentana, false);
			bcsComunes.ventanaCerrar(mMM.idVentana);
		};
		
		
		mMM.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "MES";
			datosGuardar.p_UsaReglasNegocio	= "N";
			if(mesaSel && mesaSel.IdMesa) { // EDITAR
				if(mMM.datosMesa.IdMesa				=== mesaSel.IdMesa && 
				   mMM.datosMesa.Nombre				=== mesaSel.Nombre && 
				   //mMM.datosMesa.Codigo				=== mesaSel.Codigo && 
				   //mMM.datosMesa.ClienteObj.IdCliente		=== mesaSel.IdCliente && 
				   mMM.datosMesa.IdPortafolio				=== mesaSel.IdPortafolio && 
				   //mMM.datosMesa.IdTipoProductoMesaModulo	=== mesaSel.IdTipoProductoMesaModulo && 
				   mMM.datosMesa.ValorRazonable		=== mesaSel.ValorRazonable && 
				   mMM.datosMesa.IdEstado			=== mesaSel.IdEstadoRegistro && 
				   mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Codigo  === mesaSel.CodigoIFRS &&
				   (mMM.datosMesa.PorDefecto		=== (mesaSel.PorDefecto === 'S' ? true : false)) &&
				   (mMM.datosMesa.AplicaFusion		=== (mesaSel.AplicaFusion === 'S' ? true : false))
				   ){
					bcsComunes.msg("warning","UI_MANTENEDOR_MESAS",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(mMM.idVentana, false);
					bcsComunes.ventanaCerrar(mMM.idVentana);
					return
				}
									
				datosGuardar.p_IdMesa			= mesaSel.IdMesa;
				datosGuardar.p_Nombre			= mMM.datosMesa.Nombre;
				//datosGuardar.p_Codigo			= mMM.datosMesa.Codigo;
				//datosGuardar.p_IdCliente		= mMM.datosMesa.ClienteObj.IdCliente;
				datosGuardar.p_IdPortafolio		= mMM.datosMesa.IdPortafolio;
				//datosGuardar.p_IdTipoProducto	= mMM.datosMesa.TipoProductoMesaModuloObj.IdTipoProductoMesa;
				datosGuardar.p_ValorRazonable	= mMM.datosMesa.ValorRazonable;
				datosGuardar.p_IdEstadoRegistro	= mMM.datosMesa.IdEstado;
				datosGuardar.p_CodigoIFRS       = mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Codigo;
				datosGuardar.p_PorDefecto		= mMM.datosMesa.PorDefecto ? 'S' : 'N';
				datosGuardar.p_AplicaFusion		= mMM.datosMesa.AplicaFusion ? 'S' : 'N';
				
				datosGuardar.p_DetalleLog = [];
				if(mMM.datosMesa.Nombre !== mesaSel.Nombre)
					datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'Nombre', ValorAntiguo:mesaSel.Nombre, ValorNuevo:mMM.datosMesa.Nombre});
				//if(mMM.datosMesa.Codigo !== mesaSel.Codigo)
					//datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'Codigo', ValorAntiguo:mesaSel.Codigo, ValorNuevo:mMM.datosMesa.Codigo});
				//if(mMM.datosMesa.IdCliente !== mesaSel.IdCliente)
					//datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'IdCliente', ValorAntiguo:mesaSel.IdCliente, ValorNuevo:mMM.datosMesa.ClienteObj.IdCliente});
				if(mMM.datosMesa.IdPortafolio !== mesaSel.IdPortafolio)
					datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'IdPortafolio', ValorAntiguo:mesaSel.IdPortafolio, ValorNuevo:mMM.datosMesa.IdPortafolio});
				//if(mMM.datosMesa.IdTipoProducto !== mesaSel.IdTipoProducto)
					//datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'IdTipoProducto', ValorAntiguo:mesaSel.IdTipoProductoMesaModulo, ValorNuevo:mMM.datosMesa.TipoProductoMesaModuloObj.IdTipoProductoMesa});
				if(mMM.datosMesa.ValorRazonable !== mesaSel.ValorRazonable)
					datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'ValorRazonable', ValorAntiguo:mesaSel.ValorRazonable, ValorNuevo:mMM.datosMesa.ValorRazonable});
				if(mMM.datosMesa.IdEstado !== mesaSel.IdEstadoRegistro)
					datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'IdEstado', ValorAntiguo:mesaSel.IdEstadoRegistro, ValorNuevo:mMM.datosMesa.IdEstado});
				if(mMM.datosMesa.PorDefecto !== (mesaSel.PorDefecto === 'S' ? true : false))
					datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'PorDefecto', ValorAntiguo:mesaSel.PorDefecto, ValorNuevo:mMM.datosMesa.PorDefecto ? 'S' : 'N'});
				if(mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Codigo!== mesaSel.CodigoIFRS){
					datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'CodigoIFRS', ValorAntiguo:mesaSel.DescripcionIFRS, ValorNuevo:mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Descripcion.toUpperCase()});
				}

				if(mMM.datosMesa.AplicaFusion !== (mesaSel.AplicaFusion === 'S' ? true : false))
				datosGuardar.p_DetalleLog.push({SubRegistro:mesaSel.Codigo, CampoRegistro:'AplicaFusion', ValorAntiguo:mesaSel.AplicaFusion, ValorNuevo:mMM.datosMesa.AplicaFusion ? 'S' : 'N'});
				
				if(mMM.datosMesa.TipoProductoMesaModuloObj.IdTipoProductoModulo==='MRF' &&  mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Codigo=== undefined){
					bcsComunes.msg("warning","UI_MANTENEDOR_MESAS",$translate.instant("UI_SIN_IFRS"));
				}
				else{
					bcsComunes.ventanaOverlay(mMM.idVentana,true);
					bcsServiciosMesa.ModificarMesa(datosGuardar,function (result){
						mMM.fn.evaluaRespuestaSP(result, 'UI_MESA_MODIFICADA');
						bcsComunes.ventanaCerrar(mMM.idVentana);
					}, function(resError){
						bcsComunes.msg("error","UI_MANTENEDOR_MESAS",$translate.instant("UI_REVISE_LOG"));						
						bcsComunes.ventanaOverlay(mMM.idVentana,false);
					});					
				}

								
		
			}
			else { // CREAR
				datosGuardar.p_Nombre			= mMM.datosMesa.Nombre;
				datosGuardar.p_Codigo			= mMM.datosMesa.Codigo;
				datosGuardar.p_IdCliente		= mMM.datosMesa.ClienteObj.IdCliente;
				datosGuardar.p_IdPortafolio		= mMM.datosMesa.IdPortafolio;
				datosGuardar.p_IdTipoProducto	= mMM.datosMesa.TipoProductoMesaModuloObj.IdTipoProductoMesa;
				datosGuardar.p_ValorRazonable	= mMM.datosMesa.ValorRazonable;
				datosGuardar.p_IdEstadoRegistro	= mMM.datosMesa.IdEstado;
				datosGuardar.p_CodigoIFRS       = mMM.datosMesa.TipoProductoMesaModuloObj.IdTipoProductoModulo==='MRF'? mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Codigo :'';
				datosGuardar.p_PorDefecto		= mMM.datosMesa.PorDefecto ? 'S' : 'N';
				datosGuardar.p_AplicaFusion		= mMM.datosMesa.AplicaFusion ? 'S' : 'N';
							
				if(mMM.datosMesa.TipoProductoMesaModuloObj.IdTipoProductoModulo==='MRF' &&  mMM.dataIFRS.oConfiguracionClasificacionIfrs.seleccionado.Codigo=== undefined){
					bcsComunes.msg("warning","UI_MANTENEDOR_MESAS",$translate.instant("UI_SIN_IFRS"));
				}
				else{
					bcsComunes.ventanaOverlay(mMM.idVentana,true);
					bcsServiciosMesa.CrearMesa(datosGuardar,function (result){
						mMM.fn.evaluaRespuestaSP(result, 'UI_MESA_CREADA');
						}, function(resError){
						bcsComunes.msg("error","UI_MANTENEDOR_MESAS",$translate.instant("UI_REVISE_LOG"));						
						bcsComunes.ventanaOverlay(mMM.idVentana,false);
					});					
				}
			}
		};
		
		
		mMM.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_MANTENEDOR_MESAS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_MANTENEDOR_MESAS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_MANTENEDOR_MESAS",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_MANTENEDOR_MESAS",$translate.instant("UI_REVISE_LOG"));
			console.log(result);
			
			bcsComunes.ventanaOverlay(mMM.idVentana,false);
			bcsComunes.ventanaData(mMM.idVentana, true);
			//bcsComunes.ventanaCerrar(mMM.idVentana);
		};

      /******************
		Watch
      ********************/
     $scope.$watch('mMM.datosMesa.IdTipoProductoMesaModulo',function(newval,oldval){
		
		if ($scope.mMM.datosMesa.IdTipoProductoMesaModulo === "MERF"){
		   mMM.win.AplicaFusionDisable = false
	    //   mMM.datosMesa.AplicaFusion = true;	
		}else{
			if (mMM.win.modoEditar === false){
				mMM.datosMesa.AplicaFusion = false;
			}			
			mMM.win.AplicaFusionDisable = true
		}
	
		});
	
	});
}());