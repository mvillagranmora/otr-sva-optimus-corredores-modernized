(function() {
	angular.module('ModuloPlataforma').controller('ModalAgregarValorCobroDetalleCtrl',
		function ($scope,bcsComunes,servicioPerfilCobros,bcsPerfilComercial,$timeout,bcsAlerta,OPCIONES_BCSALERTA,$translate) {
			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/
			var mavcd 			= this;
			mavcd.idVentana 	= bcsComunes.ventanaWinId($scope);
			mavcd.parametros 	= bcsComunes.ventanaParams(mavcd.idVentana);
			mavcd.domVentana	= $("[ventanaid='"+mavcd.idVentana+"']");
			var lineaDetalle	= _.clone(mavcd.parametros.lineaDetalle);
			var IdPerfilCobros	= mavcd.parametros.IdPerfilCobros;
			mavcd.cmbbx			= {};
			mavcd.respuesta 	= {};
			mavcd.etiquetaValorCobro = '';
			mavcd.dataModal 	= {};
			console.log(mavcd.parametros);
			mavcd.parametros.esPersonalizado=mavcd.parametros.esPersonalizado||false;
			/*********************************************
			 * FUNCIONES
			 *********************************************/
			mavcd.cargaDatosPantalla = function(){
				lineaDetalle = lineaDetalle || {};
				mavcd.respuesta.IdTipoProducto 				= lineaDetalle.IdTipoProducto || '0';
				mavcd.respuesta.IdTipoProductoDef			= lineaDetalle.IdTipoProducto || '0';
				mavcd.respuesta.IdCobroClasificacion 		= lineaDetalle.IdCobroClasificacion || 1;
				mavcd.respuesta.IdCobroClasificacionDef		= lineaDetalle.IdCobroClasificacion || 1;
				mavcd.respuesta.EsLineaNueva				= (lineaDetalle.esEdicionLinea === true) ? 'N': 'S';
				mavcd.respuesta.IdBolsaDef 					= lineaDetalle.IdBolsa || 'BCS';
				mavcd.respuesta.IdTipoOperacionDef 			= lineaDetalle.IdTipoOperacion || 0;
				mavcd.respuesta.IdCanalIngreso				= lineaDetalle.IdCanalIngreso || 0;
				mavcd.respuesta.IdCanalIngresoDef 			= lineaDetalle.IdCanalIngreso || 0;
				mavcd.respuesta.DiasPlazo 					= lineaDetalle.DiasPlazo || 0;
				mavcd.respuesta.EstadoDetalleDef 			= lineaDetalle.EstadoDetalle || 'ACT';
				mavcd.respuesta.Prioridad					= lineaDetalle.Prioridad || 0;
				mavcd.respuesta.IdMoneda					= lineaDetalle.IdMoneda || 0;
				mavcd.respuesta.IdMonedaDef					= lineaDetalle.IdMoneda || 0;
				if(mavcd.parametros.esPersonalizado==true){
					mavcd.respuesta.IdCobro					= lineaDetalle.IdCobro || 0;
					mavcd.respuesta.IdCobroDef				= lineaDetalle.IdCobro || 0;
					mavcd.respuesta.IdMoneda				= lineaDetalle.IdMoneda || 0;
					mavcd.respuesta.IdMonedaDef				= lineaDetalle.IdMoneda || 0;
					mavcd.respuesta.ValorCobro				= lineaDetalle.ValorCobro || 0;
					mavcd.respuesta.ValorTopeMinimoOrden	= lineaDetalle.ValorTopeMinimoOrden || 0;
				}else{
					mavcd.respuesta.IdValorCobro 			= lineaDetalle.IdValorCobro || 0;
					mavcd.respuesta.IdValorCobroDef			= lineaDetalle.IdValorCobro || 0;
				}
				$timeout(function(){
					bcsComunes.ventanaAutoHeight(mavcd.idVentana);
				}, 100);
			};		
	
			mavcd.cancelar = function () {
				mavcd.respuesta.aceptar = false;
				bcsComunes.ventanaData(mavcd.idVentana, mavcd.respuesta);
				bcsComunes.ventanaCerrar(mavcd.idVentana);
			};

			mavcd.aceptar = function () {
				var t = bcsAlerta.crear("PREGUNTA_SI_NO", 'Se modificara la comision de las ordenes, asignaciones y movimientos por facturar del cliente-portafolio este cambio podria sobregirar al cliente. �Desea Continuar?'
				, mavcd.idVentana);
		       t.then(function(result) {
				if (result === OPCIONES_BCSALERTA.SI) {

				if(lineaDetalle.IdDetallePerfilCobros) { // EDITAR
					var datosGuardar = mavcd.preparaDatosModificar();

					if(datosGuardar) {
						bcsComunes.ventanaOverlay(mavcd.idVentana,true);
						servicioPerfilCobros.updateDetallePerfilCobros(datosGuardar,function (result){
							var titulo = "";
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
								titulo = "Perfil de Cobros Actualizado";
								mavcd.respuesta.aceptar = true;
								bcsComunes.ventanaData(mavcd.idVentana, mavcd.respuesta);
								let idCobro = mavcd.respuesta.IdCobro;
								let valorCobro = datosGuardar.p_RegistroCambio.filter(function(elemento){ return elemento.CampoModificado =="ValorCobro"});
								if(valorCobro.length > 0 && (idCobro == "COVA" || idCobro == "COFI" )){
									let parametros ={
										p_IdEmpresa 		: $scope.$root.SesionIdEmpresa, 
										p_IdUsuario 		: $scope.$root.SesionIdUsuario,
										p_IdModulo 			: "PLA",
										p_UsaReglasNegocio 	: "N",
										p_IdDetallePerfilCobros : [datosGuardar.p_IdDetallePerfilCobros]
									};
									servicioPerfilCobros.modificarOrdenesAsignacionesYMovimientosPorDetalleCobro(parametros).$promise.then(function(resultado){
										if(resultado.P_OUT_ERRORESCONTADOR > 0){
											
										}
									}, function(error){
										bcsComunes.ventanaOverlay(mavcd.idVentana,false);
										bcsComunes.msg("error","PERFIL DE COBROS",'Error modificando ordenes, asignaciones o movimientos por facturar.');
										console.log(error);
									});
								}
							}else {
								titulo = "PERFIL DE COBROS"+result.CODIGO + ' : ' + result.PILA;
								console.log(result);
							}
							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},mavcd.idVentana);

							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(mavcd.idVentana,false);
								bcsComunes.ventanaCerrar(mavcd.idVentana);	
							});
							
						}, function(resError){
							bcsComunes.ventanaOverlay(mavcd.idVentana,false);
							bcsComunes.msg("error","PERFIL DE COBROS",'Revise Log');
							console.log(resError);
						});
					}
					else{
						bcsComunes.ventanaOverlay(mavcd.idVentana,false);
						bcsComunes.msg("warning","PERFIL DE COBROS",'No hay datos para guardar');
					}
				}
				else { // CREAR
					var datosGuardar = mavcd.preparaDatosCrear();
					var detalleExiste = mavcd.detalleExiste();
					if(!datosGuardar) { bcsComunes.msg("warning","PERFIL DE COBROS",'No hay datos para guardar'); return; }
					if(detalleExiste) { bcsComunes.msg("warning","PERFIL DE COBROS",'Detalle existe'); return; }	
					bcsComunes.ventanaOverlay(mavcd.idVentana,true);
					servicioPerfilCobros.saveDetallePerfilCobros(datosGuardar,function (result){
						var titulo = "";
							if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID  > 0 && result.P_OUT_ERRORESCONTADOR===0){
								titulo = "Detalle creado";
								mavcd.respuesta.aceptar = true;
								bcsComunes.ventanaData(mavcd.idVentana, mavcd.respuesta);
								let idCobro = mavcd.respuesta.IdCobro;
								if(idCobro == "COVA" || idCobro == "COFI" ){
									let parametros ={
										p_IdEmpresa 		: $scope.$root.SesionIdEmpresa, 
										p_IdUsuario 		: $scope.$root.SesionIdUsuario,
										p_IdModulo 			: "PLA",
										p_UsaReglasNegocio 	: "N",
										p_IdDetallePerfilCobros : [result.P_OUT_REFID]
									};
									servicioPerfilCobros.modificarOrdenesAsignacionesYMovimientosPorDetalleCobro(parametros).$promise.then(function(resultado){
										if(resultado.P_OUT_ERRORESCONTADOR > 0){
											
										}
									}, function(error){
										bcsComunes.ventanaOverlay(mavcd.idVentana,false);
										bcsComunes.msg("error","PERFIL DE COBROS",'Error modificando ordenes, asignaciones o movimientos por facturar.');
										console.log(error);
									});
								}	
							}else {
								titulo = "PERFIL DE COBROS"+result.CODIGO + ' : ' + result.PILA;
								console.log(result);
							}
							var idModal = bcsComunes.ventanaMsg({
								mensaje: titulo,
								conflictos:util.parseServerResponse(result)
							},mavcd.idVentana);

							bcsComunes.ventanaPromesa(idModal).then(function(){
								bcsComunes.ventanaOverlay(mavcd.idVentana,false);
								bcsComunes.ventanaCerrar(mavcd.idVentana);	
							});
							
					}, function(resError){
						bcsComunes.ventanaOverlay(mavcd.idVentana,false);
						bcsComunes.msg("error","PERFIL DE COBROS",'Revise Log');
						console.log(resError);
					});
				}
			  }
			 })
			};
			
			mavcd.detalleExiste = function(){
				var existe = false;
				if(mavcd.parametros && mavcd.parametros.detallePerfil) {
					$.each(mavcd.parametros.detallePerfil,function(key,value){
						if(	   value.IdBolsa == mavcd.respuesta.IdBolsa 
							&& value.IdTipoProducto == mavcd.respuesta.IdTipoProducto
							&& value.IdTipoOperacion == mavcd.respuesta.IdTipoOperacion
							&& value.IdCanalIngreso == mavcd.respuesta.IdCanalIngreso
							&& value.IdCobro == mavcd.dataModal.ValorCobroObj.IdCobro
						) {
							existe = true;
							return false;
						}
					});
				}
				return existe;
			};

			mavcd.preparaDatosCrear = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_UsaReglasNegocio 		= "N";
				datosGuardar.p_IdPerfilCobros 			= IdPerfilCobros;
				datosGuardar.p_IdValorCobro				= mavcd.respuesta.IdValorCobro;
				datosGuardar.p_IdBolsa					= mavcd.respuesta.IdBolsa;
				datosGuardar.p_IdTipoProducto			= mavcd.respuesta.IdTipoProducto; 
				datosGuardar.p_IdTipoOperacion			= mavcd.respuesta.IdTipoOperacion;
				datosGuardar.p_IdCanalIngreso			= mavcd.respuesta.IdCanalIngreso;
				datosGuardar.p_IdMoneda					= mavcd.parametros.esPersonalizado ? mavcd.respuesta.IdMoneda : mavcd.dataModal.ValorCobroObj.IdMoneda;
				datosGuardar.p_DiasPlazo				= Number(mavcd.respuesta.DiasPlazo);
				datosGuardar.p_Prioridad				= Number(mavcd.respuesta.Prioridad);
				datosGuardar.p_IdEstado					= mavcd.respuesta.EstadoDetalle;
				if(mavcd.parametros.esPersonalizado==true){
					datosGuardar.p_EsPersonalizado		= 'S';
					datosGuardar.p_PrefijoIdValorCobro	= mavcd.respuesta.IdCobro+'_';
					datosGuardar.p_IdCobro				= mavcd.respuesta.IdCobro;
					datosGuardar.p_ValorCobro			= mavcd.respuesta.ValorCobro||0;
					datosGuardar.p_ValorTopeMinimoOrden	= mavcd.respuesta.ValorTopeMinimoOrden||0;
				}
				return datosGuardar;
			};
			
			mavcd.preparaDatosModificar = function(){
				var datosGuardar = {};
				datosGuardar.p_IdEmpresa 				= $scope.$root.SesionIdEmpresa; 
				datosGuardar.p_IdUsuario 				= $scope.$root.SesionIdUsuario;
				datosGuardar.p_IdModulo 				= "PLA";
				datosGuardar.p_UsaReglasNegocio 		= "N";
				datosGuardar.p_IdDetallePerfilCobros 	= lineaDetalle.IdDetallePerfilCobros;
				datosGuardar.p_RegistroCambio	= [];	
				if(mavcd.respuesta.IdBolsa !== lineaDetalle.IdBolsa)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdBolsa',ValorNuevo:mavcd.respuesta.IdBolsa});
				if(mavcd.respuesta.IdTipoOperacion !== lineaDetalle.IdTipoOperacion)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdTipoOperacion',ValorNuevo:mavcd.respuesta.IdTipoOperacion});
				if(mavcd.respuesta.IdCanalIngreso != lineaDetalle.IdCanalIngreso)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdCanalIngreso',ValorNuevo:mavcd.respuesta.IdCanalIngreso});
				if(mavcd.respuesta.DiasPlazo !== lineaDetalle.DiasPlazo)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'DiasPlazo',ValorNuevo:mavcd.respuesta.DiasPlazo});
				if(mavcd.respuesta.Prioridad !== lineaDetalle.Prioridad)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'Prioridad',ValorNuevo:mavcd.respuesta.Prioridad});
				if(mavcd.respuesta.EstadoDetalle !== lineaDetalle.EstadoDetalle)
					datosGuardar.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo:mavcd.respuesta.EstadoDetalle});
				if(mavcd.respuesta.IdMoneda !== lineaDetalle.IdMoneda)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdMoneda',ValorNuevo:mavcd.respuesta.IdMoneda});
				if(mavcd.parametros.esPersonalizado==true){
					if(mavcd.respuesta.IdCobro !== lineaDetalle.IdCobro)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'IdCobro',ValorNuevo:mavcd.respuesta.IdCobro});
					if(mavcd.respuesta.ValorCobro !== lineaDetalle.ValorCobro)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'ValorCobro',ValorNuevo:mavcd.respuesta.ValorCobro});
					if(mavcd.respuesta.ValorTopeMinimoOrden !== lineaDetalle.ValorTopeMinimoOrden)
						datosGuardar.p_RegistroCambio.push({CampoModificado:'ValorTopeMinimoOrden',ValorNuevo:mavcd.respuesta.ValorTopeMinimoOrden});
					datosGuardar.p_RegistroCambio.push({CampoModificado:'EsPersonalizado',ValorNuevo:'S'});
				}
				if(datosGuardar.p_RegistroCambio.length>0) return datosGuardar;
				else return undefined;
			};

			/*********************************************
			 * COMBOBOX
			 *********************************************/
			mavcd.cmbbx.Bolsa={
				filtros : {},
			};
			
			mavcd.cmbbx.TipoProducto={
					filtros : {
						IdTipoProducto : { "field" : "IdTipoProducto", "operator": "ne","value" : 'INFM' }
					},
			};

			mavcd.cmbbx.TipoOperacion={
				filtros : {
					IdTipoProducto : { "field" : "IdTipoProducto", "operator": "eq","value" : '-1' },
					PermiteCobros : { "field" : "PermiteCobros", "operator": "eq","value" : 'S' }
				},
			};
			
			mavcd.cmbbx.ValorCobro={
				filtros : {
					IdTipoProducto : { "field" : "IdTipoProducto", "operator": "eq","value" : '-1' },
					IdCobroClasificacion : { "field" : "IdCobroClasificacion", "operator": "eq","value" : -1 }
				},
			};
			
			mavcd.cmbbx.Cobro={
				filtros : {
					PermiteAsignarPortafolio : { "field" : "PermiteAsignarPortafolio", "operator": "eq","value" : "S" },
					IdEmpresa : { "field" : "IdEmpresa", "operator": "eq","value" : $scope.$root.SesionIdEmpresa},
					IdCobroClasificacion : { "field" : "IdCobroClasificacion", "operator": "eq","value" : -1 },
			},
			};

			mavcd.cmbbx.EstadoPerfilComercial={
				filtros : {},
			};

			mavcd.cmbbx.TipoCanalIngreso={
				filtros : {
					IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" }
				},
			};
			
			/*********************************************
			 * FUNCIONES WATCH
			 *********************************************/
			$scope.$watch('mavcd.dataModal.ValorCobroObj',function(newVal){
				if(newVal && newVal.IdValorCobro){
					mavcd.etiquetaValorCobro = bcsPerfilComercial.generaInfoCobro(newVal);
				}
				else mavcd.etiquetaValorCobro = '';
			});

			$scope.$watch('mavcd.dataModal.CobroSeleccionado.PermiteCambiarValor',function(newVal){
				if(newVal!=='N'&&!newVal==='S'){
					mavcd.respuesta.ValorCobro = undefined;
					mavcd.respuesta.ValorTopeMinimoOrden = undefined;
					return;
				}
				if(lineaDetalle.IdDetallePerfilCobros){
					mavcd.respuesta.ValorCobro = lineaDetalle.ValorCobro;
					mavcd.respuesta.ValorTopeMinimoOrden = lineaDetalle.ValorTopeMinimoOrden;
				}else if(mavcd.dataModal.CobroSeleccionado&&mavcd.dataModal.CobroSeleccionado.ValorCobro){
					mavcd.respuesta.ValorCobro = mavcd.dataModal.CobroSeleccionado.ValorCobro;
					mavcd.respuesta.ValorTopeMinimoOrden = mavcd.dataModal.CobroSeleccionado.ValorTopeMinimoOrden;
				}
			});

			$scope.$watch('mavcd.respuesta.IdMoneda',function(newVal){
			
				if(lineaDetalle.IdDetallePerfilCobros){
					mavcd.respuesta.ValorCobro =0;
					
				}
			});
			
		
			
			/*********************************************
			 * GO!!!
			 *********************************************/
			mavcd.cargaDatosPantalla();
			
	});
}());