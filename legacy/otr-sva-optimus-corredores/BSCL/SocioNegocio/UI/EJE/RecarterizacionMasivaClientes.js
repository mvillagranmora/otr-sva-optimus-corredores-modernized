/******************************************
 * Purpose	: Recarterización masiva clientes
 * Author	: JGarces
 * Date		: 29/03/2017
 *****************************************/
(function() {
	angular.module('ModuloPlataforma').controller('RecarterizacionMasivaClientesCtrl',
	function($scope,bcsComunes,$translate,bcsAlerta,servicioSociodeNegocio){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
		var idVentana 	= bcsComunes.ventanaWinId($scope); 
		var parametros 	= bcsComunes.ventanaParams(idVentana);
		var recmascli 		= this;
		recmascli.conf 	= {};
		recmascli.filtros	= {};
		recmascli.datos	= [];
		recmascli.datosrespuesta	= [];
		recmascli.cmbbx 	= {};
		recmascli.win 		= {}; 
		recmascli.grid 	= {};
		recmascli.habilitaCargar=true;
		
		if($scope.SesionLocale=='PE')recmascli.IdTipoDocumentoIdentidadDef='IUP';
		else recmascli.IdTipoDocumentoIdentidadDef='R';
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		recmascli.filtroTipoDocumentoIdentidad={
			TipoPersona : {field: "IdPais", operator: "eq", value: $scope.$root.SesionIdPaisSistema}
		}

		/*********************************************
		 * GRILLAS
		 *********************************************/
		recmascli.dataProcesamiento = [];
		recmascli.grid.procesamiento =  {
			dataSource		: new bcs.DataSource({
				type 		: "array", //Los tipos posibles son: odata, call, array y object
				definition 	: {
					//data			: recmascli.dataProcesamiento,
					pageSize 		: 50,
					schema 			: {
						model	: {
		                    fields	: {
		                    	LINEA				: {type: "number"},
		                    	TIPOPERSONA			: {type: "string"},
		                    	DOCUMENTOIDENTIDAD 	: {type: "string"},
		                    	SUCURSALCLIENTE 	: {type: "string"},
		                    	EJECUTIVOCLIENTE	: {type: "string"},
		                    	CODIGOPORTAFOLIO	: {type: "string"},
		                    	EJECUTIVOPORTAFOLIO	: {type: "string"},
		                    	//salida
		                    	MENSAJE 			: {type: "string"},
		                    	MENSAJEERROR		: {type: "string"},
							}
                   		}
					}
				}
			}),
			columns			: [
			    {field: "LINEA",width: "80px",title: $translate.instant("UI_LINEA_ARCHIVO"),attributes : {style : "text-align:left;"}},
				{field: "TIPOPERSONA",width: "120px",title: $translate.instant("UI_TIPO_PERSONA"),attributes : {style : "text-align:left;"}},
				{field: "DOCUMENTOIDENTIDAD",width: "120px",title: $translate.instant("UI_DOCUMENTO_IDENTIDAD"),attributes : {style : "text-align:left;"}},
				{field: "SUCURSALCLIENTE",width: "120px",title: $translate.instant("UI_SUCURSAL_CLIENTE"),attributes: { style : "text-align:left;" }},
				{field: "EJECUTIVOCLIENTE",width: "120px",title: $translate.instant("UI_EJECUTIVO_CLIENTE"),attributes : {style : "text-align:left;"}},
				{field: "CODIGOPORTAFOLIO",width: "120px",title: $translate.instant("UI_CODIGO_PORTAFOLIO"),attributes: { style : "text-align:left;" }},
				{field: "EJECUTIVOPORTAFOLIO",width: "120px",title: $translate.instant("UI_EJECUTIVO_PORTAFOLIO"),attributes : {style : "text-align:left;"}},
				{field: "MENSAJE",width: "200px",title: $translate.instant("UI_MENSAJE_VALIDACION"),attributes: { style : "text-align:left;" }},
				{field: "MENSAJEERROR",width: "250px",title: $translate.instant("UI_MENSAJE_ERROR"),attributes : {style : "text-align:left;"}},
			],		
			sortable			: false,
			pageable			: true,
			autoBind			: true,  //comentar
			height				: 350,
			userSettings		: true,
			navigatable			: true,
			excel : {
				allPages: true,
			},
		    selectable 			: "row"
		 };
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		recmascli.win.seleccionarArchivo = function(){
			bcsComunes.ventanaOverlay(idVentana,true);
			var datosArchivo = {codigoArchivo: 'RECARTERIZACION_CLIENTES'};
			var id1 = bcsComunes.ventanaAbrir("CargaArchivosGeneral",datosArchivo,true,true);
			var promesa=bcsComunes.ventanaPromesa(id1);                               
			 promesa.then(function(data){
				 bcsComunes.ventanaOverlay(idVentana,false);
				 console.log(data);
				 if(!data || !data.fileData){
					 console.log("Error al obtener los datos");
					 return;
				 }
				 for(var vm=0;vm<data.fileData.length;vm++){
					 recmascli.datos.push(data.fileData[vm]); 
					 recmascli.datos[vm].LINEA=vm+1;
				 }
				 recmascli.grid.procesamiento.dataSource.data(recmascli.datos);
			 });
		}

		recmascli.win.limpiar = function(){
			recmascli.grid.procesamiento.dataSource.data([]);
			recmascli.datos=[];
			recmascli.datosrespuesta=[];
			recmascli.habilitaCargar=true;
		};
		
		recmascli.win.procesar = function(){
			if(recmascli.datos.length==0)return;
			bcsComunes.ventanaOverlay(idVentana,true);
			console.log(recmascli.datos);
			var datos = {};
			datos.p_IdEmpresa=$scope.$root.SesionIdEmpresa;
			datos.p_IdUsuario=$scope.$root.SesionIdUsuario;
			datos.p_SimularOperacion="N";
			datos.p_TipoDocumentoIdentidad=recmascli.IdTipoDocumentoIdentidad;
			datos.p_Registros=[];
			for(var vm=0;vm<recmascli.datos.length;vm++){
				var item={};
				item.LineaArchivo=recmascli.datos[vm].LINEA;
				item.TipoPersona=recmascli.datos[vm].TIPOPERSONA;
				item.DocumentoIdentidad=recmascli.datos[vm].DOCUMENTOIDENTIDAD;
				item.SucursalCliente=recmascli.datos[vm].SUCURSALCLIENTE;//"S002";//SC01
				item.EjecutivoCliente=recmascli.datos[vm].EJECUTIVOCLIENTE//"JUANGUIT";//0011
				item.CodigoPortafolio=recmascli.datos[vm].CODIGOPORTAFOLIO;
				item.EjecutivoPortafolio=recmascli.datos[vm].EJECUTIVOPORTAFOLIO;
				datos.p_Registros.push(item);
			}
			servicioSociodeNegocio.recarterizacionClientes(datos).$promise.then(
				function(resultado){
					console.log(resultado);
					for(var vm=0;vm<resultado.p_Registros.length;vm++){
						var item={};
						item.LINEA				= resultado.p_Registros[vm].LineaArchivo;
						item.TIPOPERSONA		= resultado.p_Registros[vm].TipoPersona;
						item.DOCUMENTOIDENTIDAD = resultado.p_Registros[vm].DocumentoIdentidad;
						item.SUCURSALCLIENTE 	= resultado.p_Registros[vm].SucursalCliente;
						item.EJECUTIVOCLIENTE	= resultado.p_Registros[vm].EjecutivoCliente;
						item.CODIGOPORTAFOLIO	= resultado.p_Registros[vm].CodigoPortafolio;
						item.EJECUTIVOPORTAFOLIO= resultado.p_Registros[vm].EjecutivoPortafolio;
                    				item.MENSAJE 			= resultado.p_Registros[vm].MensajeValidacion;
                    				item.MENSAJEERROR		= resultado.p_Registros[vm].MensajeError;
                    				recmascli.datosrespuesta.push(item);
					}
					if(recmascli.datosrespuesta.length>0) recmascli.grid.procesamiento.dataSource.data(recmascli.datosrespuesta);
					else recmascli.mensajeProceso="Procesamiento sin resultado"
					recmascli.habilitaCargar=false;
					bcsComunes.ventanaOverlay(idVentana,false);
				},function(resultado){
					console.log(resultado);
					bcsComunes.msg('error','Error',$translate.instant('UI_ERROR_RECARTERIZACION_CLIENTES'));
					recmascli.mensajeProceso=$translate.instant('UI_ERROR_RECARTERIZACION_CLIENTES');
					bcsComunes.ventanaOverlay(idVentana,false);
				}
			);
		};
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
	 	
		/*********************************************
		 * GO!!!
		 *********************************************/
	});
}());
//------------------------- FIN CONTROLLER --------------------------
