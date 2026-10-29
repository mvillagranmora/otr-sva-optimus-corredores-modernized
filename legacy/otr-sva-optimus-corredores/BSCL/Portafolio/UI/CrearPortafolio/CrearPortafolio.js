/********************************************************************
 * Purpose	: Controlador para crear un Portafolio
 * Author	: JGarces - BCS
 * Date		: 29/02/2016
 *******************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('CrearPortafolioCtrl',
	function($state,$q,$scope,$translate,bcsComunes,servicioPortafolio,bcsComunes,bcsServicioPlataforma){
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var cPort 			= this;
		cPort.idVentana 	= bcsComunes.ventanaWinId($scope);
		cPort.parametros 	= bcsComunes.ventanaParams(cPort.idVentana);
		cPort.domVentana 	= $("[ventanaid='"+cPort.idVentana+"']");	
		cPort.win 			= {};
		cPort.datos 		= {};
		cPort.cmbbx 		= {};
		cPort.ds 			= {};
		cPort.fn 			= {};
		cPort.datosCliente 	= {};
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/
		
		if(!cPort.parametros.IdCliente) {
			bcsComunes.msg("error","Abrir Ventana",'No se recibe identificador del cliente');
			bcsComunes.ventanaCerrar(cPort.idVentana);
		}
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/

		cPort.win.guardar = async function(){
			bcsComunes.ventanaOverlay(cPort.idVentana,true);
			var dataPortafolio = cPort.fn.datosGuardar();
			if (cPort.parametros.TipoPortafolio !== 'APV') {				
				try {
					const parametroMaximoCodigoPortafolio = await bcsServicioPlataforma.ObtenerParametroConfiguracionSistemaPromise(
						'POR.MaximoCodigoPortafolio',
						$scope.SesionIdEmpresa,
						'SOCIONEGOCIO'
					);
					let validacionMaximoCodigoPortafolio = true;
					if (parametroMaximoCodigoPortafolio.Valor) {
						const maximoCodigoRegex = new RegExp(parametroMaximoCodigoPortafolio.Valor);
						validacionMaximoCodigoPortafolio = maximoCodigoRegex.test(dataPortafolio.p_CodigoPortafolio);
					}
					if (!validacionMaximoCodigoPortafolio) {
						bcsComunes.msg("error", "CREAR PORTAFOLIO", $translate.instant('UI_ERROR_CODIGO_PORTAFOLIO'));
						bcsComunes.ventanaOverlay(cPort.idVentana, false);
						return;
					}
				} catch(error) {
					console.log("CREAR PORTAFOLIO", error)
				}				
			}
			servicioPortafolio.savePortafolio(dataPortafolio,function(result){
				console.log(JSON.stringify(result));
				if (result.P_OUT_ADVERTENCIASCONTADOR > 0){
					bcsComunes.ventanaMsg({ mensaje : $translate.instant('UI_OPERACION_REQUIERE_AUTORIZACION'),
						conflictos : util.parseServerResponse(result)}, cPort.idVentana);
					bcsComunes.ventanaCerrar(cPort.idVentana);
				}
				else if (result.p_out_RefId !== undefined && result.p_out_RefId > 0){
					
					bcsComunes.ventanaMsg({ mensaje : 'Portafolio creado',
						conflictos : util.parseServerResponse(result)}, cPort.idVentana);
					bcsComunes.ventanaCerrar(cPort.idVentana);
				}
				else {
					bcsComunes.ventanaMsg({ mensaje : 'Error al guardar',
						conflictos : util.parseServerResponse(result)}, cPort.idVentana);
				}
				bcsComunes.ventanaOverlay(cPort.idVentana,false);
			},function(error){
				bcsComunes.msg("error","CREAR PORTAFOLIO","Error al guardar");
				console.log("CREAR PORTAFOLIO",error);
				bcsComunes.ventanaOverlay(cPort.idVentana,false);
			});
		};

		cPort.win.cancelar = function(){
			bcsComunes.ventanaCerrar(cPort.idVentana);
		};
		
		cPort.win.verMatriz = function(f){
			if(cPort.datos.IdPerfilComercialBaseOrigen) {
				var id1 = bcsComunes.ventanaAbrir("modalMatrizOperacional",{ PerfilComercialObj : cPort.PerfilComercialObj},false, false,cPort.idVentana ); 
	            var promesa=bcsComunes.ventanaPromesa(id1);                               
	            $.when(promesa).then(function(data){ }); 
			}
			else {
				bcsComunes.msg("advertencia","CREAR PORTAFOLIO","Seleccione un Perfil");
			}
		};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		cPort.ds.DatosCliente = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1275",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdCliente 				 : { type: "number"},
							IdCategoriaInversionista : { type: "string"}
						}
					}
				}
			}
		});	


		/*********************************************
		 * COMBOBOX
		 *********************************************/

		cPort.cmbbx.oConfPerfilComercial={
			tipo 	: 'combobox-perfil-comercial', 
			filtros : { 
				IdCategoriaInversionista : {
						"field"   : "IdCategoriaInversionista",
						"operator": "eq",
						"value"   : "" }},
		};
		
		cPort.cmbbx.oConfPropositoPortafolio={
			tipo : "combobox-proposito-portafolio",
			filtros : { },
		};
				
		cPort.FiltroProductoAgrupador =  {
				filtros: {
					IdPlanTipoInversion : {"field" : "IdPlanTipoInversion", "operator" : "eq", "value" : 0}
				}	
		};
		
		cPort.FiltroRegimenAgrupador =  {
				filtros: {
					IdProductoAgrupador : {"field" : "IdProductoAgrupador", "operator" : "eq", "value" : 0}
				}	
		};		
		
		if(cPort.parametros.TipoPortafolio !== 'APV'){
			cPort.datos.FiltroIdPropositoPortafolio = {
					filtros: {
						IdPropositoPortafolio  : { "field" : "IdPropositoPortafolio" , "operator": "Ne" , "value"  : "PROPOSITOAPV" },
						IdEstado : {field: "IdEstado", operator: "eq", value: "ACT"}
					}
			}	
		}else{
			cPort.datos.FiltroIdPropositoPortafolio = {
					filtros: {
							IdPropositoPortafolio  : { "field" : "IdPropositoPortafolio" , "operator": "eq" , "value"  : "PROPOSITOAPV" }
					}
			}
		}

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		cPort.fn.datosGuardar = function(){
			return {
				p_IdEmpresa				: $scope.$root.SesionIdEmpresa,
				p_IdUsuario				: $scope.$root.SesionIdUsuario,
				p_IdModulo 				: 'SOC',
				p_UsaReglasNegocio 		: 'S',
				p_CodigoPortafolio 		: cPort.parametros.TipoPortafolio !== 'APV' ? cPort.datos.CodigoPortafolio : cPort.RegimenAgrupadorSeleccionado.Codigo,
				p_DescripcionPortafolio	: cPort.datos.Descripcion,
				p_PropositoPortafolio 	: cPort.datos.idProposito,
				p_DesdeEnrolamiento		: 'N',
				
				p_CodigoRegimen			: cPort.parametros.TipoPortafolio !== 'APV' ? '' : cPort.RegimenAgrupadorSeleccionado.Codigo,
				p_NombrePlan			: cPort.parametros.TipoPortafolio !== 'APV' ? '' : cPort.NombrePlanAhorroSeleccionado.NombrePlanTipoInversion,
				p_CodigoAgrupador		: cPort.parametros.TipoPortafolio !== 'APV' ? '' : cPort.RegimenAgrupadorSeleccionado.CodigoAgrupador,
				
				p_IdCliente				: cPort.parametros.IdCliente,
				p_IdPerfilComercialBase : cPort.datos.IdPerfilComercialBaseOrigen
			};
		};
		
		cPort.fn.getPortafolios = function(){
			return new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: { read: { url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1265",dataType: "json"}},
					filter: { field: "IdCliente", operator: "eq", value: Number(cPort.parametros.IdCliente)},
					schema: {model: { fields : { IdPortafolio: { type: "number"}}}},
				}
			});
		};
		
		cPort.fn.preparaDatosGuardar = function(){
			var dataPortafolio = {};
			dataPortafolio.p_IdEmpresa 			= $scope.$root.SesionIdEmpresa;
			dataPortafolio.p_IdUsuario 			= $scope.$root.SesionIdUsuario;
			dataPortafolio.p_IdCliente			= cPort.parametros.IdCliente;
			dataPortafolio.p_IdModulo 			= 'SOC';
			dataPortafolio.p_UsaReglasNegocio 	= 'N',
			dataPortafolio.p_IdPortafolio 		= cPort.parametros.IdPortafolio;
			dataPortafolio.p_RegistroCambio 	=
			[
				
			    {CampoModificado:'Proposito'						,ValorNuevo: cPort.PropositoPortafolioObjNuevo.Descripcion || '0'},

			];

			
			return dataPortafolio;
		};		
		
		cPort.fn.iniciar = function() {
			var filtro = { field: "IdCliente", operator: "eq", value: Number(cPort.parametros.IdCliente)};
			cPort.ds.DatosCliente.query({ filter : filtro}).then(function(result){
				if (cPort.ds.DatosCliente.data().length >0) {
					cPort.datosCliente = _.clone(cPort.ds.DatosCliente.data()[0]);
					cPort.cmbbx.oConfPerfilComercial.filtros.IdCategoriaInversionista.value = cPort.datosCliente.IdCategoriaInversionista;
					if (cPort.parametros.TipoPortafolio === 'APV') {
						cPort.cmbbx.oConfPerfilComercial.filtros.IdPerfilCobros = {
							"field"   : "IdPerfilCobros",
							"operator": "eq",
							"value"   : "APV_EXENTO"
						};
					}
					cPort.datos.idProposito  = 'PROPGENERALES';
					cPort.datos.idPropositoDef  = 'PROPGENERALES';
					if($scope.SesionValoresDefectoSistema.UsaCodigoPortafolioDefault=='S'){
						var p = cPort.fn.getPortafolios();
						p.query().then(function(result){
							var cant = p.data().length + 1;
							cPort.datos.CodigoPortafolio = String(cant);
						});
					}
				}else {
					bcsComunes.msg("error","Abrir Ventana",'Cliente no existe');
					bcsComunes.ventanaCerrar(cPort.idVentana);
				}
			});
		}

		/*********************************************
		 * GO!!!
		 *********************************************/
		cPort.fn.iniciar();
		
	});
}());