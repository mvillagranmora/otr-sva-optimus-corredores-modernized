angular.module('ModuloPlataforma').controller('ConsultaUsuariosCtrl',
	function ($scope, $translate, $modal, $log,
		bcsComunes, bcsServicioPlataforma) {

		$scope.data = {};
		var overlay = bcsComunes.overlay;

		//Seteo de variables de sistema
		$scope.data.IdEmpresa = $scope.SesionIdEmpresa;
		$scope.data.IdUsuario = $scope.SesionIdUsuario;
		$scope.data.IdModulo = 'PLA';
		$scope.data.btnActivos = true;   //Activa botones

		var idVentana = bcsComunes.ventanaWinId($scope);
		bcsComunes.ventanaTitulo(idVentana, bcsComunes.ventanaGetTitulo(idVentana) + " - " + $scope.SesionEmpresa);


		//GRILLA PRINCIPAL DE MOVIMIENTOS A FACTURAR
		$scope.confBasicaConsultaUsuarios = {
			dataSource: new bcs.DataSource({
				type: "odata", //Los tipos posibles son: odata, call, array y object
				definition: {
					serverFiltering: true, // Filtrado de datos en el servidor.
					serverPaging: true, // Paginado de los datos en el servidor.
					serverSorting: true, // Ordenamiento de los datos en el servidor.
					groupable: true,
					sort: { field: "IdUsuario", dir: "asc" }, // Criterios de Ordenamiento.
					pageSize: 25, // Número de registros por página.
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=384",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdUsuario: { type: "string" },

								CodigoUsuario: { type: "string" },
								NombreCompleto: { type: "string" },
								RazonSocial: { type: "string" },
								TipoDocumentoIdentidad: { type: "string" },
								DocumentoIdentidad: { type: "string" },
								TipoEntidad: { type: "string" },
								NombreTipoSocioDeNegocio: { type: "string" },
								Clave: { type: "string" },
								FechaIngreso: { type: "date" },
								FechaVigencia: { type: "date" },
								FechaUltimaConexion: { type: "date" },
								DiasVencimientoClave: { type: "number" },
								Conectado: { type: "string" },
								Hora: { type: "string" },
								Terminal: { type: "string" },
								IdEstado: { type: "string" },
								DescripcionEstado: { type: "string" },
								IntentosConexion: { type: "string" },
								FechaBloqueoTemporal: { type: "date" },
								TieneBloqueoTemporal: { type: "string" },
								EsAutorizado: { type: "string" },
								EsUsuarioBolsa: { type: "string" },
								NombreTipoBloqueo: { type: "string" },
								ComentarioBloqueo: { type: "string" },
							}
						}
					},
					aggregate: []
				}
			}),
			columns: [
				{ field: "IdUsuario", title: "UI_ID_USUARIO", format: "{0:n5}", width: "150px", hidden: true },

				{ field: "CodigoUsuario", title: "UI_NOMBRE_USUARIO", format: "{0:n5}", width: "150px" },

				{ field: "NombreCompleto", title: "UI_NOMBRE_COMPLETO", format: "{0:n5}", width: "150px" },
				{ field: "RazonSocial", title: "UI_RAZON_SOCIAL", format: "{0:n5}", width: "150px" },
				{ field: "TipoDocumentoIdentidad", title: "UI_TIPO_DOCUMENTO_IDENTIDAD", format: "{0:n5}", width: "150px" },
				{ field: "DocumentoIdentidad", title: "UI_DOCUMENTO_IDENTIDAD", format: "{0:n5}", width: "150px" },
				{ field: "TipoEntidad", title: "UI_TIPO_ENTIDAD", format: "{0:n5}", width: "150px", hidden: true },
				{ field: "NombreTipoSocioDeNegocio", title: "UI_TIPO_SOCIO_NEGOCIO", format: "{0:n5}", width: "150px" },

				{ field: "Clave", title: "UI_CLAVE", format: "{0:n5}", width: "150px", hidden: true },
				{ field: "FechaIngreso", title: "UI_FECHA_INGRESO", format: "{0:d}", width: "150px" },
				{ field: "FechaVigencia", title: "UI_FECHA_VIGENCIA", format: "{0:d}", width: "150px" },
				{ field: "FechaUltimaConexion", title: "UI_FECHA_ULTIMA_CONEXION", format: "{0:d}", width: "150px" },
				{ field: "DiasVencimientoClave", title: "UI_DIAS_VENCIMIENTO_CLAVE", width: "150px" },
				{ field: "Conectado", title: "UI_CONECTADO", format: "{0:n5}", width: "150px" },
				{ field: "Hora", title: "UI_HORA", width: "150px", template: kendo.template("#=formatHora(Hora)#") },
				{ field: "Terminal", title: "UI_CODIGO_TERMINAL", format: "{0:n5}", width: "150px" },
				{ field: "IdEstado", title: "UI_ID_ESTADO", format: "{0:n5}", width: "150px", hidden: true },
				{ field: "DescripcionEstado", title: "UI_ESTADO", format: "{0:n5}", width: "150px" },
				{ field: "IntentosConexion", title: "UI_INTENTOS_CONEXION", format: "{0:n5}", width: "150px" },
				{ field: "FechaBloqueoTemporal", title: "UI_FECHA_BLOQUEO_TEMPORAL", format: "{0:d}", template: kendo.template("#if (util.dateisdefault(FechaBloqueoTemporal) || FechaBloqueoTemporal < util.date(new Date())) {##} else {##=util.formatoSecondate(FechaBloqueoTemporal)##}#"), width: "150px" },
				{ field: "EsAutorizado", title: "UI_AUTORIZADO", format: "{0:n5}", width: "100px", template: kendo.template("#if (EsAutorizado==='S') {##='Autorizado' ##} else{ ##='Pendiente' ##}#") },
				{ field: "EsUsuarioBolsa", title: "UI_USUARIOBOLSA", format: "{0:n5}", width: "100px", template: kendo.template("#if (EsUsuarioBolsa==='S') {##='S' ##} else{ ##='N' ##}#") },
				{ field: "NombreTipoBloqueo", title: "UI_TIPO_BLOQUEO", format: "{0:n5}", width: "150px" },
				{ field: "ComentarioBloqueo", title: "UI_COMENTARIO_BLOQUEO", format: "{0:n5}", width: "150px" }
			],
			gridCreated: function (grid) {
				$scope.data.mainGrid = grid;
			},
			sortable: true,
			pageable: true,
			filterable: true,
			groupable: true,
			//autoBind			: true,
			height: 300,
			userSettings: true,
			navigatable: true,
			excel: {
				allPages: true,
			},
			selectable: "row"
		};

		//SUB-GRILLA DE PERFILES
		$scope.confBasicaRolesPermisosUsuario = function (dataItem) {
			var dsDef = {
				type: "odata",
				definition: {
					serverFiltering: true, // Filtrado de datos en el servidor.
					serverPaging: true, // Paginado de los datos en el servidor.
					serverSorting: true, // Ordenamiento de los datos en el servidor.
					sort: { field: "NombreRol", dir: "asc" }, // Criterios de Ordenamiento.
					pageSize: 5, // Número de registros por página.
					transport: {
						read: {
							url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1521",
							dataType: "json"
						}
					},
					schema: {
						model: {
							fields: {
								IdUsuario: { type: "STRING" },
								NombrePerfil: { type: "string" },
								NombreRol: { type: "string" },
								IdEstadoPerfil: { type: "string" },
								EstadoPerfil: { type: "string" }
							}
						}
					},
					filter: [
						// Configuración de filtros sobre los datos en el detalle.
						{ field: "IdUsuario", operator: "eq", value: Number(dataItem.IdUsuario) },
						{ field: "EsAutorizado", operator: "eq", value: 'S' },
						{ field: "IdEstadoRol", operator: "eq", value: 'ACT' }
					]
				}
			};
			return {
				dataSource: new bcs.DataSource(dsDef),
				sortable: true,
				pageable: true,
				height: 120,
				//width				: 40,
				selectable: "row",
				autoBind: true,
				columns: [
					{ field: "IdUsuario", title: "UI_IDUSUARIO", format: "{0:n5}", width: "0px", hidden: true },
					{ field: "NombreRol", title: "UI_ROL", format: "{0:n5}", width: "20px" },
					{ field: "NombrePerfil", title: "UI_PERFIL", format: "{0:n5}", width: "20px" },
					{ field: "IdEstadoPerfil", title: "UI_ESTADO", format: "{0:n5}", width: "20px", hidden: true },
					{ field: "EstadoPerfil", title: "UI_ESTADO", format: "{0:n5}", width: "20px" }

				],
				gridCreated: function (grid) {
					$scope.data.detalleGrid = grid;
				},
			};
		};

		//COMBOBOX Estados
		$scope.data.oConfiguracionEstadosUsuario = {
			tipo: "combobox-estado-usuario",
			configuracion: {},
			filtros: {
				read: false
			},
			idActual: false,
			seleccionado: false,
			idDefecto: 'S'
		};


		$scope.buscar = function () {
			var filters = new Array();

			filters.push({ field: "IdEmpresa", operator: "eq", value: $scope.data.IdEmpresa });

			if ($scope.data.NombreUsuario) {
				filters.push({ field: "tolower(CodigoUsuario)", operator: "contains", value: $scope.data.NombreUsuario.toLowerCase() });
			}

			if ($scope.data.oConfiguracionEstadosUsuario.idActual != 0) {
				filters.push({ field: "tolower(IdEstado)", operator: "contains", value: $scope.data.oConfiguracionEstadosUsuario.idActual.toLowerCase() });
			}


			var obj2filters = {};
			var niv2filters = new Array();
			obj2filters.logic = 'or';
			obj2filters.filters = niv2filters;

			if ($scope.data.NombreCompleto) {
				var obj2filters = {};
				var niv2filters = new Array();
				obj2filters.logic = 'or';
				obj2filters.filters = niv2filters;

				//Busqueda por nombre Completo
				niv2filters.push({ field: "tolower(NombreCompleto)", operator: "contains", value: $scope.data.NombreCompleto.toLowerCase() });
				//Busqueda de Razon social
				niv2filters.push({ field: "tolower(RazonSocial)", operator: "contains", value: $scope.data.NombreCompleto.toLowerCase() });

				filters.push(obj2filters);
			}
			$scope.data.mainGrid.dataSource.filter(filters);
			$scope.data.mainGrid.clearSelection();
		};



		$scope.limpiarFiltros = function () {
			$scope.data.NombreCompleto = '';
			$scope.data.NombreUsuario = '';
			$scope.data.EstadoUsuario = '';
			$scope.data.oConfiguracionEstadosUsuario.idActual = 0;
			$scope.data.mainGrid.dataSource.removeAll();
			$scope.data.mainGrid.clearSelection();
		};


		$scope.data.opcionNoHabilitada = function () {
			bcsComunes.msg("warning", "warning", "Está opción no se encuentra habilitada.");
		}

		$scope.crearUsuario = function () {
			var id1 = bcsComunes.ventanaAbrir("CrearUsuario", {}, false, false, idVentana);

			var promesa = bcsComunes.ventanaPromesa(id1);
			promesa.then(function (data) {
				//Refrescar grilla para visualizar cambios.
				$scope.data.mainGrid.dataSource.read();
			});
		};
		$scope.crearUsuarioBolsa = function () {
			var id1 = bcsComunes.ventanaAbrir("CrearUsuarioBolsa", {}, false, false, idVentana);

			var promesa = bcsComunes.ventanaPromesa(id1);
			promesa.then(function (data) {
				//Refrescar grilla para visualizar cambios.
				$scope.data.mainGrid.dataSource.read();
			});

		};
		$scope.modificarUsuario = function () {
			if ($scope.seleccionDetalleUsuarios) {
				var id1 = bcsComunes.ventanaAbrir("ModificarUsuario", $scope.seleccionDetalleUsuarios, false, false, idVentana);

				var promesa = bcsComunes.ventanaPromesa(id1);
				promesa.then(function (data) {
					//Refrescar grilla para visualizar cambios.
					$scope.data.mainGrid.dataSource.read();
					$scope.data.mainGrid.clearSelection();
				});
			} else {
				bcsComunes.msg("error", 'Error', $translate.instant("MOD_NO_USUARIO_SELECT"));
			}
		}

		$scope.modificarRolesUsuario = function () {
			if ($scope.seleccionDetalleUsuarios) {
				var id1 = bcsComunes.ventanaAbrir("ModificarRolesUsuario", $scope.seleccionDetalleUsuarios, false, false, idVentana);

				var promesa = bcsComunes.ventanaPromesa(id1);
				promesa.then(function (data) {
					//Refrescar grilla para visualizar cambios.
					$scope.data.mainGrid.dataSource.read();
					$scope.data.mainGrid.clearSelection();
				});
			} else {
				bcsComunes.msg("error", 'Error', $translate.instant("MOD_NO_USUARIO_SELECT"));
			}

		};
	});

function formatHora(hora) {
	hora = hora.replace("PT", "").replace("H", ":").replace("M", ":").replace("S", "");

	hora = hora.split(":");
	$.each(hora, function (key) {
		hora[key] = (parseInt(hora[key]) < 10) ? '0' + parseInt(hora[key]) : hora[key];
	});
	hora = hora.join(":");

	return hora;
}
