/*************************************************************
 * Purpose	: Modal Grupo Direccion Electronica Detalle
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 30/06/2016 [Norm.]
 *************************************************************/
(function() {
	angular.module('ModuloSocioNegocio').controller('ModalGrupoDireccionElectronicaDetalleCtrl',
	function($scope,$resource,$translate,servicioSociodeNegocio,bcsComunes, bcsServicioPlataforma) {
		var mgdied 				= this;	
		var idVentana 			= bcsComunes.ventanaWinId($scope);
		var parametros 			= bcsComunes.ventanaParams(idVentana);
		mgdied.data 			= parametros.Grupo
		mgdied.data.Activo 		= mgdied.data.IdEstado === 'ACT' ? false:true; 
		mgdied.IdPersona 		= parametros.IdPersona;
		mgdied.comboDireccion 	= {};
		mgdied.comboDireccion.filtros = {
			IdPersona : {field: "IdPersona", operator: "eq", value: Number(parametros.IdPersona)}
		};

		mgdied.CodigoFormularioDetalle = parametros.CodigoFormularioDetalle;
		mgdied.camposRequeridos = {};
		bcsServicioPlataforma.CamposRequeridosFormulario(mgdied.CodigoFormularioDetalle, function (respuesta) {
	            mgdied.camposRequeridos = respuesta;
	            mgdied.asignarValoresPorDefecto();
	            mgdied.dataOriginal = $.extend({},  mgdied.data);
	     });
		
		mgdied.direccionElectronicaOptions = {
			dataSource: new bcs.DataSource({
				type: "odata",
				definition : {
					serverFiltering:true,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1312",
							dataType: "json"
						}
					},
					filter:  [{field: "IdGrupoDireccion", operator: "eq", value: Number(parametros.Grupo.IdGrupoDireccion)},
					           {field: "IdEstado", operator: "eq", value: 'ACT'}],
					schema: {
						model: {
							fields: {
								IdGrupoDireccionDetalle : { type : "number"},
								IdDireccionElectronica : { type: "number"},
								IdTipoDestinatario : { type : "string"},
								IdGrupoDireccion : { type: "string"},
								IdPersona : { type: "number"},
								IdEstado : { type: "string"},
								FechaIngreso : { type: "date"},
								FechaCreacion : { type: "date"},
								FechaModificacion : { type: "date"},
								IdEstadoAutorizacion : { type : "number" },
								Direccion : { type: "string"},
								NombreTipoDestinatario : { type : "string"}
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
			columns: [
				{field: "Direccion",title: $translate.instant('UI_DIRECCION'),attributes: {style:"text-align:left;"},width: "200px"},
				{field: "NombreTipoDestinatario", title: $translate.instant('UI_TIPO_DESTINATARIO'), attributes: {style:"text-align:left;"},width: "100px"}
			]	
		};

		var agregarDireccionSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=241";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    }; 
	    
	    var modificarGrupoSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=242";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    };
	    
	    var eliminarDireccionSP = function(v_Dato)
	    {
		       var urlConsulta = "/BSCL/Plataforma/GWJAVA/PLA/call?t=243";
		       var r = $resource
	         ("", {}, {
		                 'urlConsulta': { method: "POST", url: urlConsulta}
	                  }
	         );
		       var post = r.urlConsulta(v_Dato);
		       return post.$promise;
	    }; 
		
	    mgdied.agregar = function() {
			var parametrosSP = {
				p_IdEmpresa 				: $scope.SesionIdEmpresa,
				p_IdUsuario 				: $scope.SesionIdUsuario,
				p_IdModulo 					:'SOC',
				p_UsaReglasNegocio 			:'N',
				p_IdDireccionElectronica 	: mgdied.data.direccionElectronica || 0,
				p_IdTipoDestinatario		: mgdied.data.IdTipoDestinatario || '',   
				p_IdGrupoDireccion			: parametros.Grupo.IdGrupoDireccion || 0,
				p_IdEstado					: 'ACT',
			};
			agregarDireccionSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaMsg({ mensaje : "Grupo de Direcciones creado",
						conflictos : util.parseServerResponse(result)}, idVentana);
					mgdied.direccionElectronicaOptions.dataSource.read();
					
				},
				function(result){
					bcsComunes.msg("error","Error","La operacion tiene errores");
				}
			);
		};
		
		mgdied.aceptar = function() {
			bcsComunes.ventanaOverlay(idVentana,true);
			var campos = [
						{ nombre: "Grupo",		tipo : "texto"},
						{ nombre: "Comentario",	tipo : "texto"},
						{ nombre: "IdEstado",	tipo : "bool"}
			];
			var registroCambio = [];
		
			$.each(campos,function(key,value) {
				if (value.tipo === 'texto') {
					if (mgdied.dataOriginal[value.nombre] !== mgdied.data[value.nombre])
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mgdied.data[value.nombre]?mgdied.data[value.nombre]:"")});
				}	
				if (value.tipo === 'bool') {
					if (mgdied.dataOriginal[value.nombre] !== (mgdied.data["Activo"] ? "INA" : "ACT"))
						registroCambio.push({CampoModificado : value.nombre, ValorNuevo : (mgdied.data["Activo"] ? "INA" : "ACT") });
				}	
			});
							
			var parametrosSP = {
				p_IdEmpresa 		: $scope.SesionIdEmpresa,
				p_IdUsuario 		: $scope.SesionIdUsuario,
				p_IdModulo 			:'SOC',
				p_UsaReglasNegocio 	:'S',
				p_IdGrupoDireccion	: parametros.Grupo.IdGrupoDireccion,
				p_RegistroCambio 	: registroCambio
			};
		
			if (registroCambio.length > 0){
			modificarGrupoSP(parametrosSP).then(
				function(result) {
					
					var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Grupo de Direcciones modificado",
						conflictos : util.parseServerResponse(result)}, idVentana);
					bcsComunes.ventanaPromesa(idMensaje).then(function(){
						bcsComunes.ventanaOverlay(idVentana,false);
						bcsComunes.ventanaCerrar(idVentana);	
					});
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
			}
			else
			{
				bcsComunes.ventanaOverlay(idVentana,false);
				bcsComunes.ventanaCerrar(idVentana);
			}
		};
	
		mgdied.eliminar = function() {
			var registroCambio = [];
			registroCambio.push({CampoModificado : "IdDireccionElectronica", ValorNuevo : mgdied.direccionElectronicaSeleccionada.IdDireccionElectronica });
			registroCambio.push({CampoModificado : "IdEstado", ValorNuevo : 'INA' });	
			var parametrosSP = {
				p_IdEmpresa 				: $scope.SesionIdEmpresa,
				p_IdUsuario 				: $scope.SesionIdUsuario,
				p_IdModulo 					:'SOC',
				p_UsaReglasNegocio 			:'N', 
				p_IdGrupoDireccionDetalle	: mgdied.direccionElectronicaSeleccionada.IdGrupoDireccionDetalle,
				p_RegistroCambio			: registroCambio,
			};
			bcsComunes.ventanaOverlay(idVentana,true);
			eliminarDireccionSP(parametrosSP).then(
				function(result) {
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.ventanaMsg({ mensaje : "Dirección Eliminada",
						conflictos : util.parseServerResponse(result)}, idVentana);
					mgdied.direccionElectronicaOptions.dataSource.read();
					
				},
				function(result){
					bcsComunes.ventanaOverlay(idVentana,false);
					bcsComunes.msg("error","Error","La operacion tiene errores");
				});
		};
		
		mgdied.asignarValoresPorDefecto = function (){
			$.each(mgdied.camposRequeridos ,function(key,value){
				let noEsRequerido = value.Requerido === 'N';
				let esValorDefectoValido = value.ValorDefectoCampoFormulario !== '';
				let esCampoEnBlanco = !mgdied.data[value.CodigoValorFrontend];
				if(noEsRequerido && esValorDefectoValido && esCampoEnBlanco){
					mgdied.data[value.CodigoValorFrontend] = value.ValorDefectoCampoFormulario;
				}
			});
		}

		mgdied.cancelar = function() {
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------