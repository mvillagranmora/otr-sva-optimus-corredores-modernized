/********************************************************************
 * Purpose	: Controlador para crear/modificar Preferencia de Pago
 * 			  en el portafolio.
 * Author	: JGarces - BCS
 * Date		: 21/03/2016
 *******************************************************************/
(function() {
	angular.module('ModuloPortafolio').controller('ModalPortafolioPreferenciaPagoCtrl',
	function($state,$q,$scope,$translate,bcsComunes,servicioPortafolio,bcsComunes,bcsServicioPlataforma){
	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var mPortPP 			= this;
		mPortPP.idVentana 		= bcsComunes.ventanaWinId($scope);
		mPortPP.parametros 		= bcsComunes.ventanaParams(mPortPP.idVentana);
		mPortPP.domVentana 		= $("[ventanaid='"+mPortPP.idVentana+"']");	
		mPortPP.win 			= {};
		mPortPP.datos 			= {};
		mPortPP.cmbbx 			= {};
		mPortPP.ds 				= {};
		mPortPP.ktv 			= {};
		mPortPP.fn 				= {};
		mPortPP.datosAntes 		= {};
		mPortPP.datosDespues	= {};
				
		mPortPP.win.modoEdicion = false;
		mPortPP.isOperacionMesaPaga = false;
		mPortPP.isOperacionMesaRecibe = true;
		mPortPP.CodigoFormulario = mPortPP.parametros.CodigoFormulario;
		mPortPP.camposRequeridos = {};

		// CAMPOS OBLIGATORIOS
		bcsServicioPlataforma.CamposRequeridosFormulario(mPortPP.CodigoFormulario, function (respuesta) {
			mPortPP.camposRequeridos = respuesta;
		});
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/
		if(!mPortPP.parametros.IdPortafolio || !mPortPP.parametros.RecibePagaPortafolio) {
			bcsComunes.msg("error","Abrir Ventana",'No se reciben parámetros iniciales');
			bcsComunes.ventanaCerrar(mPortPP.idVentana);
		}

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/

		mPortPP.win.guardar = function(){
			if(mPortPP.win.modoEdicion) { // EDITAR
				const dataPreferenciaPago = mPortPP.fn.datosEditar();
				mPortPP.salidaGuardado = [];
				mPortPP.cantidadGuardar = dataPreferenciaPago.length;
				if (dataPreferenciaPago.length > 0) {
					bcsComunes.ventanaOverlay(mPortPP.idVentana, true);
					const value = dataPreferenciaPago[0];

					servicioPortafolio.updatePreferenciaPago(value,
						function (result) {
							const idModal = bcsComunes.ventanaMsg(
								{
									mensaje: "Modificar Preferencias Pago",
									conflictos: util.parseServerResponse(result)
								},
								mPortPP.idVentana);

							bcsComunes.ventanaPromesa(idModal).then(function () {
								mPortPP.win.cancelar();
							});

						}, function onError(response) {
							const res = util.parseServerResponse(response);

							bcsComunes.ventanaOverlay(mPortPP.idVentana, false);
							const idModal = bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, mPortPP.idVentana);
							bcsComunes.ventanaPromesa(idModal).then(function () {
								mPortPP.win.cancelar();
							});
						});
				} else {
					bcsComunes.msg("warning", 'Editar Preferencia(s) Pago', 'No hay diferencias para guardar');
				}
			}
			else 
			{ // CREAR
				const dataPreferenciaPago = mPortPP.fn.datosCrear();
				mPortPP.salidaGuardado = [];
				bcsComunes.ventanaOverlay(mPortPP.idVentana, true);
				servicioPortafolio.savePreferenciaPago(
					dataPreferenciaPago,
					function (result) {
						const idModal = bcsComunes.ventanaMsg(
							{
								mensaje: "Crear Preferencias Pago",
								conflictos: util.parseServerResponse(result)
							},
							mPortPP.idVentana
						);
						bcsComunes.ventanaPromesa(idModal).then(function () {
							mPortPP.win.cancelar();
						});
					},
					function onError(response) {
						const res = util.parseServerResponse(response);

						bcsComunes.ventanaOverlay(mPortPP.idVentana, false);
						const idModal = bcsComunes.ventanaMsg({ mensaje: $translate.instant('UI_OPERACION_ERROR'), conflictos: res }, mPortPP.idVentana);
						bcsComunes.ventanaPromesa(idModal).then(function () {
							mPortPP.win.cancelar();
						});
					});
			}
		};
		
		mPortPP.win.cancelar = function(){
			bcsComunes.ventanaCerrar(mPortPP.idVentana);
		};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		mPortPP.ds.listaOperaciones = new bcs.DataSource({
			type			: "odata",
			definition		: {
			  	serverFiltering	: true,
				serverSorting	: true,
				sort: [
					 { field : "IdTipoOperacion", 	dir: "asc" },
				],
			    transport	: {
					read	: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1250", //PreferenciaPago
				      	dataType	: "json"
				   	}
				},
				schema: {
					model: {
						fields: {
							IdPreferenciaPago		: { type: "string"},
							IdTipoOperacion			: { type: "string"},
							IdMedioDePago			: { type: "string"},
							IdMoneda				: { type: "string"},
							UsaPersonaRelacionada	: { type: "string"},
							FechaCreacion			: { type: "date"},
							FechaModificacion		: { type: "date"},
							IdUsuarioCreador		: { type: "number"},
							IdEstado				: { type: "string"},
							IdEmpresa				: { type: "number"},
							NombreTipoOperacion		: { type: "string"},
							IdTipoProducto			: { type: "string"},
							NombreTipoProducto		: { type: "string"},
							RecibePagaPortafolio	: { type: "string"},
							NombreMedioDePago		: { type: "string"},
							NombreMoneda			: { type: "string"},
							UsaCuentaBancaria		: { type: "string"},
							UsaTarjetaBancaria		: { type: "string"},
							NombreEstado			: { type: "string"},
						}
					}
				}
			}
		});
			
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		mPortPP.cmbbx.prefPagoTipoOperacion = {
			filtros : {  
				RecibePagaPortafolio : {
					"field"   : "RecibePagaPortafolio",
					"operator": "eq",
					"value"   : mPortPP.parametros.RecibePagaPortafolio
				},
				IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				}
			},
		};

		mPortPP.cmbbx.personaOperacionPortafolioCliente = {
			filtros : {

			},
		};

		
		mPortPP.cmbbx.prefPagoMedioPago = { 
			filtros : { 
				RecibePagaPortafolio : {
					"field"   : "RecibePagaPortafolio",
					"operator": "eq",
					"value"   : mPortPP.parametros.RecibePagaPortafolio 
				},
				IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				}
			},
			read : false,
		};
		
		mPortPP.cmbbx.prefPagoMoneda = {
			filtros : { 
				IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				},
				RecibePagaPortafolio : {
					"field"   : "RecibePagaPortafolio",
					"operator": "eq",
					"value"   : mPortPP.parametros.RecibePagaPortafolio
				},
				IdMedioDePago : {
					"field"   : "IdMedioDePago",
					"operator": "eq",
					"value"   : "-1" 
				}
			},
			read : false,
		};
		
		
		mPortPP.cmbbx.personaCuentasBancarias = {
			filtros : { 
				IdPersona : {
					"field"   : "IdPersona",
					"operator": "eq",
					"value"   : mPortPP.parametros.IdPersona
				},
				IdMoneda : {
					"field"   : "IdMoneda",
					"operator": "eq",
					"value"   : "-1" 
				},
				IdEstado : {
					"field"   : "IdEstado",
					"operator": "eq",
					"value"   : "ACT" 
				}
			},
		};
		
		mPortPP.cmbbx.personaCuentasBancariasCorredora = {
				filtros : { 
					IdPersona : {
						"field"   : "IdEmpresa",
						"operator": "eq",
						"value"   : $scope.$root.SesionIdEmpresa
					},
					IdMoneda : {
						"field"   : "IdMoneda",
						"operator": "eq",
						"value"   : "-1" 
					},
					IdEstado : {
						"field"   : "IdEstado",
						"operator": "eq",
						"value"   : "ACT" 
					}
				},
			};
	
		mPortPP.cmbbx.personaTarjetasBancarias = {
				filtros : { 
					IdPersona : {
						"field"   : "IdPersona",
						"operator": "eq",
						"value"   : mPortPP.parametros.IdPersona 
					},
					/*IdMoneda : {
						"field"   : "IdMoneda",
						"operator": "eq",
						"value"   : "-1" 
					},*/
					IdEstado : {
						"field"   : "IdEstado",
						"operator": "eq",
						"value"   : "ACT" 
					}
				},
			};
		
		mPortPP.cmbbx.estado = { 
				filtros : { 
					NombreEntidad : {
						"field"   : 'NombreEntidad',
						"operator": "eq",
						"value"   : 'PREFERENCIAPAGO' 
					}
				},
			};

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		mPortPP.fn.datosCrear = function(){
			var aux = {};
			aux.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa,
			aux.p_IdUsuario 		= $scope.$root.SesionIdUsuario,
			aux.p_IdModulo 			= 'SOC',
			aux.p_UsaReglasNegocio 	= 'S',
			aux.p_IdPortafolio 		= mPortPP.parametros.IdPortafolio;
			aux.p_IdPreferenciaPago = mPortPP.datosDespues.IdPreferenciaPago || '';
			aux.p_IdCuentaBancaria 	= mPortPP.parametros.RecibePagaPortafolio === 'P' ? mPortPP.datosDespues.IdCuentaBancaria || mPortPP.datosDespues.IdCuentaBancariaCuenta || 0 : mPortPP.datosDespues.IdCuentaBancaria || 0;
			aux.p_IdTarjetaBancaria = mPortPP.datosDespues.IdTarjetaBancaria || 0;
			aux.p_IdEstado 			= 'ACT';
			aux.p_PorDefecto		= (mPortPP.datosDespues.PorDefecto)?'S':'N';
			return aux;
		};
		
		mPortPP.fn.datosEditar = function(){
			var datosEditar = [];

			var aux = {};
			aux.p_IdEmpresa 					= $scope.$root.SesionIdEmpresa,
			aux.p_IdUsuario 					= $scope.$root.SesionIdUsuario,
			aux.p_IdModulo 						= 'SOC',
			aux.p_UsaReglasNegocio 				= 'S',
			aux.p_IdPreferenciaPagoPortafolio 	= mPortPP.datosDespues.IdPreferenciaPagoPortafolio;
			aux.p_RegistroCambio				= [];
			var valorIdCuenta 					= 0;
			
			
			
            if(mPortPP.datosAntes.IdPreferenciaPago !== mPortPP.datosDespues.IdPreferenciaPago){
				aux.p_RegistroCambio.push({CampoModificado:'IdPreferenciaPago',ValorNuevo:mPortPP.datosDespues.IdPreferenciaPago});
			}
			
			if ((mPortPP.datosAntes.IdCuentaBancaria !== mPortPP.datosDespues.IdCuentaBancaria) || (mPortPP.datosAntes.IdCuentaBancariaCuenta !== mPortPP.datosDespues.IdCuentaBancariaCuenta))
			{
				valorIdCuenta = mPortPP.parametros.RecibePagaPortafolio === 'P' ? mPortPP.datosDespues.IdCuentaBancaria || mPortPP.datosDespues.IdCuentaBancariaCuenta || 0 : mPortPP.datosDespues.IdCuentaBancaria || 0;
				aux.p_RegistroCambio.push({CampoModificado:'IdCuentaBancaria',ValorNuevo:valorIdCuenta});
			}
			
			if(mPortPP.datosAntes.IdEstado !== mPortPP.datosDespues.IdEstado) {
				aux.p_RegistroCambio.push({CampoModificado:'IdEstado',ValorNuevo: mPortPP.datosDespues.IdEstado});
			}
			let porDefectoAux = mPortPP.datosDespues.PorDefecto == false ? 'N' : 'S'
			if(mPortPP.datosAntes.PorDefecto !==porDefectoAux) {
				aux.p_RegistroCambio.push({CampoModificado:'PorDefecto',ValorNuevo:porDefectoAux});
			}		
			if(mPortPP.datosAntes.IdTarjetaBancaria !== mPortPP.datosDespues.IdTarjetaBancaria && mPortPP.PrefPagoMedioPagoObj.UsaTarjetaBancaria === 'S'){
				aux.p_RegistroCambio.push({CampoModificado:'IdTarjetaBancaria',ValorNuevo:mPortPP.datosDespues.IdTarjetaBancaria});
			}

			if(aux.p_RegistroCambio.length>0) {
				datosEditar.push(aux);										
			}
			
			return datosEditar;
		};
		
		
		mPortPP.fn.cargaListaOperaciones = function(){

			mPortPP.cmbbx.personaOperacionPortafolioCliente = {
				filtros : {
					IdEstado: { field: "IdEstado", operator: "eq", value: 'ACT' }, 
					RecibePagaPortafolio: { field: "RecibePagaPortafolio",operator: "eq", value: mPortPP.parametros.RecibePagaPortafolio },
					IdMedioDePago : { field: "IdMedioDePago", operator: "eq", value: mPortPP.datosDespues.IdMedioDePago },
					IdMoneda : { field: "IdMoneda", operator: "eq", value: mPortPP.datosDespues.IdMoneda },
				},
			};

			mPortPP.listaOperaciones = undefined;
			bcsComunes.ventanaOverlay(mPortPP.idVentana,false);
		};
				
		mPortPP.fn.iniciar = function() {
			//título ventana:
			//esta modelado desde el punto de vista del cliente
			//pero los titulos se muestran desde el punto de vista de la corredora
			if(!mPortPP.win.modoEdicion && mPortPP.parametros.RecibePagaPortafolio === 'P') 
				bcsComunes.ventanaTitulo(mPortPP.idVentana, $translate.instant('UI_CREAR_FORMA_DE_PAGO_CLIENTE_PAGA'));
			if(mPortPP.win.modoEdicion && mPortPP.parametros.RecibePagaPortafolio === 'P')
				bcsComunes.ventanaTitulo(mPortPP.idVentana, $translate.instant('UI_EDITAR_FORMA_DE_PAGO_CLIENTE_PAGA'));
			if(!mPortPP.win.modoEdicion && mPortPP.parametros.RecibePagaPortafolio === 'R')
				bcsComunes.ventanaTitulo(mPortPP.idVentana, $translate.instant('UI_CREAR_FORMA_DE_PAGO_CLIENTE_RECIBE'));
			if(mPortPP.win.modoEdicion && mPortPP.parametros.RecibePagaPortafolio === 'R')
				bcsComunes.ventanaTitulo(mPortPP.idVentana, $translate.instant('UI_EDITAR_FORMA_DE_PAGO_CLIENTE_RECIBE'));
			mPortPP.datosAntes.PorDefecto = false; mPortPP.datosDespues.PorDefecto = false;
			//Filtros generales
			
			//Carga datos modo Edicion
			if(mPortPP.parametros.PreferenciaPagoObj) {
				mPortPP.win.modoEdicion = true;
				mPortPP.datosAntes = _.clone(mPortPP.parametros.PreferenciaPagoObj);
				mPortPP.datosDespues = _.clone(mPortPP.parametros.PreferenciaPagoObj);
				mPortPP.datosDespues.PorDefecto = mPortPP.datosDespues.PorDefecto === "S" ? true : false;
				if(mPortPP.parametros.RecibePagaPortafolio === 'P'){
					mPortPP.datosAntes.IdCuentaBancariaCuenta = mPortPP.datosAntes.IdCuentaBancaria
					mPortPP.datosDespues.IdCuentaBancariaCuenta = mPortPP.datosDespues.IdCuentaBancaria
				}
			}
			mPortPP.datosDespues.operacion = {};
			mPortPP.datosAntes.operacion = {};
		}
		
		mPortPP.fn.evaluarGuardado = function(result) {
			var errores = 0;
			var creados = 0;
			$.each(mPortPP.salidaGuardado,function(key,value){
				if(value.tipo==='error')errores++;
				if(value.tipo==='info')creados++;
			});
			var response = {};
			response.P_OUT_INFORMACIONDETALLE = [];
			response.P_OUT_ERRORESDETALLE = [];

			var titulo = "";
			if(creados>0)
				response.P_OUT_INFORMACIONDETALLE.push({"Descripcion" : 'Crear Preferencia Pago ' + String(creados) + ' OK'});
			if(errores>0)
				response.P_OUT_ERRORESDETALLE.push({"Descripcion" : 'Crear Preferencia Pago ' + String(errores) + ' con Errores'});

			var idModal = bcsComunes.ventanaMsg({
						mensaje: "Crear Preferencias Pago",
						conflictos:util.parseServerResponse(response)
					},mPortPP.idVentana);

			bcsComunes.ventanaPromesa(idModal).then(function(){
				mPortPP.win.cancelar();
			});
		};
		
		/*********************************************
		 * FUNCIONES WATCH
		 *********************************************/
		$scope.$watch('mPortPP.datosDespues.IdMoneda',function(newval,oldval){
			if(newval) mPortPP.fn.cargaListaOperaciones();
			else mPortPP.listaOperaciones = undefined;
		});

		$scope.$watch("mPortPP.datosDespues.IdMedioDePago", function(nval, oval){
			if(mPortPP.parametros.RecibePagaPortafolio === 'R'){
				if(!!nval && (nval==="CHE" || nval==="VVI" || nval==="VVE" || nval==="POR")){
					mPortPP.isOperacionMesaRecibe = false;
				}else{
					mPortPP.isOperacionMesaRecibe = true;
				}	
			}else{
				if(!!nval && (nval==="POR")){
					mPortPP.isOperacionMesaRecibe = false;
				}else{
					mPortPP.isOperacionMesaRecibe = true;
				}	
			}
			
		});

		$scope.$watch('mPortPP.PersonaOperacionPortafolioClienteObj', function(newVal, oldVal){
			if(!!newVal && (newVal.IdTipoProducto==="MEME" || newVal.IdTipoProducto==="MERF" || newVal.IdTipoProducto==="MEFW")){
				mPortPP.isOperacionMesaPaga = true;	
			}else{
				mPortPP.isOperacionMesaPaga = false;	
			}
		});

		/*********************************************
		 * GO!!!
		 *********************************************/
		mPortPP.fn.iniciar();
	});
}());