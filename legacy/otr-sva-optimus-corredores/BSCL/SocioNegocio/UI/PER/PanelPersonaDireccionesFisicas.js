/*************************************************************
 * Purpose	: Panel Persona Direcciones Fisicaas
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 * 
 * Purpose	: Se incluye campo CodigoCompuesto, req. proy Perú.
 *            Perú: UBIGEO / Chile: Codigo Unico Territorial
 * Author	: JGarces - BCS
 * Date		: 14/03/2017
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('PanelPersonaDireccionesFisicasCtrl',
	function($scope,$translate,bcsComunes,$uibModal, $resource,bcsAlerta,servicioSociodeNegocio){
		/******************************************************
		 * INICIO DE VARIABLES
		 * ***************************************************/
		var ppdf = this;
		ppdf.idVentana = bcsComunes.ventanaWinId($scope);
		ppdf.abrirModal = function(xIdPersona,xDireccionFisica)
		{
			var id1 = bcsComunes.ventanaAbrir("agregarModificarDireccionFisica", {
				DireccionFisica: xDireccionFisica,
				IdPersona: xIdPersona,
				CodigoFormulario: ppdf.CodigoFormulario
			}, false, false, ppdf.idVentana);
	        var promesa=bcsComunes.ventanaPromesa(id1);                               
	        promesa.then(function(data){ 
	        	if (data === true)
				{
					ppdf.direccionFisicaSeleccionada = undefined;
					ppdf.direccionFisicaOptions.dataSource.read();
				}
	    	}); 
		};
		
		ppdf.agregar = function() {
			ppdf.abrirModal(ppdf.IdPersona,undefined);
		};
		
		ppdf.eliminar = function(dfi) {
			
			var confirmacion = bcsAlerta.crear("PREGUNTA_SI_NO", $translate.instant("UI_MENSAJE_ELIMINAR_DIRECCION"), ppdf.idVentana);
			confirmacion.then(function(result){//1:CONFIRMADO : 2 RECHAZADO
				if(result==1){
			
					if(dfi.IdDireccionFisica){
						var registroCambio = [];
						registroCambio.push({
							CampoModificado: "Borrado",
							ValorNuevo: "S",
							IdEntidad: ppdf.direccionFisicaSeleccionada.IdDireccionFisica
						});
						
						var parametros = {
							p_IdEmpresa 		: $scope.$root.SesionIdEmpresa,
			        		p_IdUsuario 		:  $scope.$root.SesionIdUsuario,
							p_IdPersona			: ppdf.IdPersona,
			        		p_IdModulo 			: 'SOC',
			        		p_UsaReglasNegocio	: "N",
			        		p_IdTipoCanal 		: "OPT",
							p_RegistroCambio	: registroCambio
						};
						
						bcsComunes.ventanaOverlay(ppdf.idVentana,true);
					
						servicioSociodeNegocio.eliminarPersonaDireccionFisicaMasivo(parametros).$promise.then(
							function(result){
								bcsComunes.ventanaOverlay(ppdf.idVentana,false);
								var srv=util.parseServerResponse(result);
								
								var subtitulo='';
								if (srv.erroresContador > 0){
									subtitulo='UI_OPERACION_CON_ERRORES';
									bcsComunes.msg("error", "error", $translate.instant("UI_ERROR"));
								} else if(srv.erroresContador === 0  && srv.autorizacionesContador === 0){
									subtitulo='UI_DIRECCION_FISICA_BORRADA';
								} else if (srv.erroresContador === 0  && srv.autorizacionesContador > 0) {
									 subtitulo='UI_OPERACION_PENDIENTE_AUTORIZAR';							
								}
								
								bcsComunes.ventanaMsg({ mensaje : subtitulo, conflictos : srv}, ppdf.idVentana);
								ppdf.direccionFisicaOptions.dataSource.read();
							},
							function(result){		
								bcsComunes.ventanaOverlay(ppdf.idVentana,false);						
								var srv=util.parseServerResponse(result);
								var p=bcsComunes.ventanaMsg({ 
									mensaje : '', 
									conflictos : srv
								},ppdf.idVentana);
							}
						);
					}
				} else {
					bcsComunes.ventanaOverlay(ppdf.ventanaId, false);
					return;
				}
				
			});		
				
				
		};
			/*var eliminarDireccionFisicaSP = function(v_Dato)
		    {
			       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1692";
			       var r = $resource
			         ("", {}, {
				                 'urlConsulta': { method: "POST", url: urlConsulta}
			                  }
			         );
			       var post = r.urlConsulta(v_Dato);
			       return post.$promise;
		    };
		    
		    var parametrosSP = {};
	        parametrosSP.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.$root.SesionIdUsuario;
	        parametrosSP.p_IdModulo 		= 'SOC';
	        parametrosSP.p_UsaReglasNegocio = "N";
	        parametrosSP.p_IdDireccionFisica = ppdf.direccionFisicaSeleccionada.IdDireccionFisica;
	  
	        bcsComunes.ventanaOverlay(ppdf.idVentana, true);
	        eliminarDireccionFisicaSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Direccion Fisica Borrada",
						conflictos : util.parseServerResponse(result)}, ppdf.idVentana);
					ppdf.direccionFisicaOptions.dataSource.read();
					bcsComunes.ventanaOverlay(ppdf.idVentana, false);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppdf.idVentana, false);
				});*/
	
		ppdf.predeterminar = function(dfi) {
			
			var predeterminarDireccionFisicaSP = function(v_Dato)
		    {
			       var urlServicio = "/BSCL/Plataforma/GWJAVA/PLA/call?t=13025";
			       var r = $resource
			         ("", {}, {
				                 'urlServicio': { method: "POST", url: urlServicio}
			                  }
			         );
			       var post = r.urlServicio(v_Dato);
			       return post.$promise;
		    };
		    
		    var parametrosSP = {};
	        parametrosSP.p_IdEmpresa 		=  $scope.SesionIdEmpresa;
	        parametrosSP.p_IdUsuario 		=  $scope.SesionIdUsuario;
	        parametrosSP.p_IdModulo 		=  'SOC';
	        parametrosSP.p_UsaReglasNegocio =  "S";
	        parametrosSP.p_IdDireccionFisica = ppdf.direccionFisicaSeleccionada.IdDireccionFisica;
	  
	        bcsComunes.ventanaOverlay(ppdf.idVentana, true);    	    	
	        predeterminarDireccionFisicaSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : $translate.instant("UI_DIRECCION_FISICA_PREDETERMINADO"),
						conflictos : util.parseServerResponse(result)}, ppdf.idVentana);
					ppdf.direccionFisicaOptions.dataSource.read();
					bcsComunes.ventanaOverlay(ppdf.idVentana, false);
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
					bcsComunes.ventanaOverlay(ppdf.idVentana, false);
				});
		};
		ppdf.modificar = function(dfi) {
			ppdf.abrirModal(ppdf.IdPersona,dfi);
		};
		
		$scope.admSoc.socioNegocioPromise.then(function(){
			ppdf.CodigoFormulario = $scope.admSoc.dataSocioNegocio.Matriz && $scope.admSoc.dataSocioNegocio.Matriz['DIRECCION_FISICA'];
			ppdf.IdPersona = $scope.admSoc.dataSocioNegocio.IdPersona;
			ppdf.direccionFisicaOptions = {
				dataSource: new  bcs.DataSource({
					type: "odata",
					definition : {
						serverFiltering:true,
						transport: {
							read: {
								url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1313",
								dataType: "json"
							}
						},
						filter: { field: "IdPersona", operator: "eq", value: Number(ppdf.IdPersona) },
						schema: {
							model: {
								fields: {
									IdDireccionFisica : { type: "number"},
									Calle : { type: "string"},
									IdPersona : { type: "number"},
									Numero : { type: "string"},
									Departamento : { type: "string"},
									Comentario: { type: "string"},
									Sector : { type: "string"},
									CodigoPostal : { type: "string"},
									CodigoProposito : { type: "string"},
									DescripcionProposito : { type: "string"},
									IdCiudad : { type: "number"},
									IdDivisionPais : { type: "number"},
									NombreCiudad : { type: "string"},
									NombreDivisionPais : { type: "string"},
									NombrePais : { type: "string"},
									IdEstado : { type: "string"},
									NombreEstado : { type: "string"},
									FechaIngreso : { type: "date"},
									FechaCreacion : { type: "date"},
									FechaModificacion : { type: "date"},
									CodigoCompuesto : {type:"string"},
									Predeterminado : {type: "string"},
									CodigoDivisionPais : { type: "string"},
									PaisNombreDivisionPais: {type: "string"}
								}
							}
						}
					}
				}),
				reorderable: true,
				filterable: false,
				groupable: false,
				pageable: false,
				selectable: "row",
				columnMenu: false,
				resizable: true,
				autoBind: true,
				scrollable: true,
				height:200,
				columns: [{
					field: "Calle",
					title: $translate.instant('UI_CALLE'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},{
					field: "Numero",
					title: $translate.instant('UI_NUMERO'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},{
					field: "Departamento",
					title: ($scope.SesionLocale=='PE')?$translate.instant('UI_REFERENCIA'):$translate.instant('UI_COMPLEMENTO'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},{
					field: "Comentario",
					title: $translate.instant('UI_COMENTARIO'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},{
					field: "Sector",
					title: $translate.instant('UI_SECTOR'),
					attributes: {style:"text-align:left;"},
					width: "150px"
				},{
					field: "DescripcionProposito",
					title: $translate.instant('UI_PROPOSITO'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},{
					field: "CodigoPostal",
					title: $translate.instant('UI_CODIGO_POSTAL'),
					attributes: {style:"text-align:left;"},
					width: "100px"
				},{
					field: "NombreCiudad",
					title: $translate.instant('UI_CIUDAD'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},{
					field: "PaisNombreDivisionPais",
					title: $translate.instant('UI_TIPO_DIVISION'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "NombreDivisionPais",
					title: $translate.instant('UI_DIVISION_PAIS'),
					attributes: {style:"text-align:left;"},
					width: "200px"
				},{
					field: "CodigoDivisionPais",
					title: ($scope.SesionLocale=='PE')?$translate.instant('UI_UBIGEO'):$translate.instant('UI_CODIGO_UNICO_TERRITORIAL'),
					attributes: {style:"text-align:left;"},
					width: "150px"
				},{
					field: "NombrePais",
					title: $translate.instant('UI_PAIS'),
					attributes: {style:"text-align:left;"},
					width: "150px"
				},{
					field: "NombreEstado",
					title: $translate.instant('UI_ESTADO'),
					attributes: {style:"text-align:left;"},
					width: "80px"
				},{
					field: "Predeterminado",
					title: $translate.instant("UI_PREDETERMINADO"),
					template: "<span translate> #: Predeterminado # </span>",
					attributes: {style:"text-align:left;"},
					width: "80px"
				}]	
			};
		});	
	});
}());
//------------------------- FIN CONTROLLER --------------------------