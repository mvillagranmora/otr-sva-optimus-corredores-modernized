(function () {
	angular.module('ModuloPlataforma').controller('ModalConfirmacionCtrl',
		function ($scope, $translate, bcsComunes, $resource, $http, bcsServicioPlataforma) {

			/*********************************************
			 * VARIABLES Y CONTEXTOS
			 ********************************************/

			var mCN = this;
			mCN.idVentana = bcsComunes.ventanaWinId($scope);
			mCN.parametros = bcsComunes.ventanaParams(mCN.idVentana);
			mCN.domVentana = $("[ventanaid='" + mCN.idVentana + "']");

			var valor = _.clone(mCN.parametros.mDoc);


			mCN.win = {};
			mCN.fn = {};
			mCN.grilla = {};


			/*********************************************
			 * DATASOURCES
			 *********************************************/
			mCN.domVentana.data("kendoWindow").setOptions({ width: 800 });
			mCN.domVentana.data("kendoWindow").setOptions({ height: 300 });

			Filtros = function () {
                var filtrosX = new Array();
				var filtrosCanal = new Array();

					if (valor.IdTipoPersona === 'A') {
						if (valor.IdTipoDocumento) {
							filtrosX.push({ field: "IdTipoDocumento", operator: "eq", value: valor.IdTipoDocumento })
						}else{
							filtrosX.push({ field: "IdTipoDocumento", operator: "eq", value: valor.CodigoDocumento })
						}
					
					} else {
						filtrosX.push({ field: "IdTipoPersona", operator: "eq", value: valor.IdTipoPersona })
						if (valor.IdTipoDocumento) {
							filtrosX.push({ field: "IdTipoDocumento", operator: "eq", value: valor.IdTipoDocumento })
						}else{
							filtrosX.push({ field: "IdTipoDocumento", operator: "eq", value: valor.CodigoDocumento })
						}
					}
					if (valor.RequiereFirmaCliente !== 'S') {
						filtrosX.push({ field: "EstadoAnexo"	, operator: "neq", value: "ACT"})
						filtrosX.push({ field: "EstadoAnexo"	, operator: "neq", value: "VCD"})
     				}
				if (valor.CanalesDeEnrolamiento.length > 0) {
					valor.CanalesDeEnrolamiento.forEach(function (value, key) {
						filtrosCanal.push({ field: "IdTipoCanalIngreso", operator: "eq", value: value.IdTipoCanal })
					});
				}
				
				filtrosX.push({
					logic: "or",
					filters: filtrosCanal
				})

				return filtrosX;
			};

			
			mCN.grilla.FirmaDoc = {
				dataSource: new bcs.DataSource({
					type: "odata",
					definition: {
						serverFiltering: true, // Filtrado de datos en el servidor.
						serverPaging: true, // Paginado de los datos en el servidor.
						serverSorting: true, // Ordenamiento de los datos en el servidor.
						sortable: true,
						pageSize: 50,
						transport: {
							read: {
								url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290683",
								dataType: "json"
							}
						},

						filter: Filtros(),

						schema: {
							model: {
								fields: {
									IdEmpresa: { type: "number" },
									IdCliente: { type: "number" },
									IdPersona: { type: "number" },
									IdEstado: { type: "string" },
									Nombres: { type: "string" },
									ApellidoPaterno: { type: "string" },
									IdTipoCliente: { type: "string" },
									TipoPersona: { type: "string" },
									IdTipoDocumento: { type: "string" },
									NombreDocumento: { type: "string" },
									CodPlantilla: { type: "string" },
									RequiereFirmaCliente: { type: "string" },
									IdAnexo: { type: "number" },
									EstadoAnexo: { type: "string" },
									IdTipoPersona: { type: "string" },
									IdDocumento: { type: "string" },
									DocumentoIdentidad: { type: "string" },
									BloquearAlVencer: { type: "string" },
								}
							}
						}
					}
				}),
				columns: [
					{ field: "DocumentoIdentidad", title: $translate.instant("ID_CLIENTE"), width: "50px" },
					{ field: "Nombres", title: $translate.instant("UI_BCS_NOMBRE"), width: "50px" },
					{ field: "ApellidoPaterno", title: $translate.instant("UI_APELLIDO_PATERNO"), width: "50px" },
					{ field: "TipoPersona", title: $translate.instant("UI_BCS_TIPO_PERSONA"), width: "80px" },
					{ field: "IdTipoCliente", title: $translate.instant("UI_TIPO_CLIENTE"), width: "50px" },
					{ field: "IdTipoDocumento", title: $translate.instant("UI_ID_DOCUMENTO"), width: "80px" },
					{ field: "NombreDocumento", title: $translate.instant("UI_NOMRE_DOCUMENTO"), width: "80px" },

				],
				sortable: false,
				pageable: true,
				autoBind: true,
				height: 200,
				selectable: "row"
			};
			


			/*********************************************
			 * FUNCIONES
			 *********************************************/

			mCN.fn.cancelar = function () {

				bcsComunes.ventanaData(mCN.idVentana, false);
				bcsComunes.ventanaCerrar(mCN.idVentana);
			};






			mCN.fn.aceptar = function () {


				
				if (valor.IdTipoDocumento) {
					modificarTipoDocumentoSP(valor).then(
						function (result) {
							bcsComunes.ventanaMsg({
								mensaje: "Tipo Documento Modificado",
								conflictos: util.parseServerResponse(result)
							}, mCN.idVentana);
							GuardarDatosPostIngreso();
						},
						function (error) { bcsComunes.msg("error", "Error", "Error al Modificar documento"); }
					);		
				}
				else {
					valor.IdTipoDocumento = valor.CodigoDocumento;
					agregarTipoDocumentoSP(valor).then(
						function (result) {

							bcsComunes.ventanaMsg({
								mensaje: "Tipo Documento Agregado",
								conflictos: util.parseServerResponse(result)
							}, mCN.idVentana);
							GuardarDatosPostIngreso();

						},
						function (error) { bcsComunes.msg("error", "Error", "Error al Ingresar Documento nuevo"); }
					);
				}

			};

			var GuardarDatosPostIngreso = function (){

               if (valor.actualizaPlantilla){
				guardarPlatillaContrato(valor).then(
					function (result) {
						bcsComunes.msg("info", "Info", "Plantillas Actualizados");
					},
					function (error) { bcsComunes.msg("error", "Error", "Error al actualizar plantilla"); }
				);

				}

				if (valor.actualizarCanales){
					GuardarCambioCanales(valor).then(
						function (result) {
							bcsComunes.msg("info", "Info", "Canales Actualizados");
						},
						function (error) { bcsComunes.msg("error", "Error", "Error al Actualizar Canales Ingreso"); }
					);		
				}
				if (valor.UpdateCanalesEnrolamiento){
					ActualizarCanalesEnrolamiento(valor).then(
						function (result) {
								bcsComunes.msg("info", "Info", "Canales enrolamiento actualizados");
							},
							function (error) { bcsComunes.msg("error", "Error", "Error al Actualizar Canales Enrolamiento"); }
						);
				}

				mCN.fn.imprimirArchivo();  // comentar para pruebas			
				bcsComunes.ventanaData(mCN.idVentana, true);
				bcsComunes.ventanaCerrar(mCN.idVentana);

			}

			var GuardarCambioCanales = function (valores){
				var datosCNL = {};

				datosCNL.p_IdEmpresa 		= 	valores.p_IdEmpresa;
				datosCNL.p_IdUsuario 		= 	valores.p_IdUsuario;
				datosCNL.p_IdModulo	 		= 	'DOC';
				datosCNL.p_UsaReglasNegocio = 	"N";
				datosCNL.p_IdTipoDocumento 	=	valores.IdTipoDocumento;
				datosCNL.p_CanalesDeIngreso =   valores.CanalesDeIngreso

					
				var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=80001";
				var r = $resource
					("", {}, {
						'url': { method: "POST", url: url }
					}
					);
				var post = r.url(datosCNL);
				return post.$promise;			

			}

			var ActualizarCanalesEnrolamiento = function (valores){
				var datosCNL = {};

				datosCNL.p_IdEmpresa 		= 	valores.p_IdEmpresa;
				datosCNL.p_IdUsuario 		= 	valores.p_IdUsuario;
				datosCNL.p_IdModulo	 		= 	'DOC';
				datosCNL.p_UsaReglasNegocio = 	"N";
				datosCNL.p_IdTipoDocumento 	=	valores.IdTipoDocumento;
				datosCNL.p_CanalesDeEnrolamiento =   valores.CanalesDeEnrolamiento;

					
				var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=80003";
				var r = $resource
					("", {}, {
						'url': { method: "POST", url: url }
					}
					);
				var post = r.url(datosCNL);
				return post.$promise;			

			}
			
			var agregarTipoDocumentoSP = function (v_Dato) {
				var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700323";
				var r = $resource
					("", {}, {
						'url': { method: "POST", url: url }
					}
					);
				var post = r.url(v_Dato);
				return post.$promise;
			};

			var modificarTipoDocumentoSP = function (v_Dato) { 			
		    	var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=700324";
				var r = $resource
					("", {}, {
						'url': { method: "POST", url: url }
					}
					);
				var post = r.url(v_Dato);
				return post.$promise;
			};


			mCN.fn.imprimirArchivo = function () {
				var datos2 = {};
				datos2.p_IdEmpresa = valor.p_IdEmpresa;
				datos2.p_IdUsuario = valor.p_IdUsuario
				datos2.p_IdModulo = "PLA"
				datos2.p_IdAgenda = 0;
				datos2.p_CreaParam = "S";
				datos2.p_IdTipoTarea = "ACTUALIZA_CONTRATOS_FIRMADOS";
				datos2.p_Segundos = 0;
				datos2.p_TablaParametros = [
					{ NombreParametro: 'p_IdEmpresa', Valor: valor.p_IdEmpresa },
					{ NombreParametro: 'p_TipoDocumento', Valor: valor.IdTipoDocumento },
					{ NombreParametro: 'p_IdTipoPersona', Valor: valor.IdTipoPersona },
					{ NombreParametro: 'p_RequiereFirmaCliente', Valor: valor.RequiereFirmaCliente },
					{ NombreParametro: 'p_CanalesDeEnrolamiento', Valor: valor.CanalesDeEnrolamiento.map((canal) => canal.IdTipoCanal).join('-') }]
				actualizaContratos(datos2).then(
					function (result) {

						bcsComunes.ventanaCerrar(mCN.idVentana);
					},
					function (result) { bcsComunes.msg("error", "Error", "Error en imprimirArchivo"); }
				);
			}

			var actualizaContratos = function (datosx) {

				{
					var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=183";
					var r = $resource
						("", {}, {
							'url': { method: "POST", url: url }
						}
						);
					var post = r.url(datosx);
					return post.$promise;
				};


			}


			var guardarPlatillaContrato = function (valores){
			var datosPCD = {};

			datosPCD.p_IdEmpresa = valores.p_IdEmpresa;
			datosPCD.p_IdUsuario = valores.p_IdUsuario;
			datosPCD.p_IdModulo = 'DOC';
			datosPCD.p_UsaReglasNegocio = "N";
			datosPCD.p_CodigoArchivo = valores.p_CodigoArchivo;
			datosPCD.p_IdTipoDocumento =valores.IdTipoDocumento;

				
						var url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=4290680";
						var r = $resource
							("", {}, {
								'url': { method: "POST", url: url }
							}
							);
						var post = r.url(datosPCD);
						return post.$promise;

			}

		});
}());