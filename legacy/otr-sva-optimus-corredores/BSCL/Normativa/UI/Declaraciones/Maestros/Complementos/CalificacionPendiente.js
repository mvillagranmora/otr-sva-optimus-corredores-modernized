angular.module('ModuloNormativa').controller('CalificacionPendienteCtrl',function($scope,$translate,bcsComunes,bcsConfiguracionMaestroDeclaraciones,bcsAlerta,bcsServiciosNormativa,OPCIONES_BCSALERTA,$timeout){
	$scope.formData = {};
	$scope.filtros = {};
	var esquema = $scope.complementosMain.confGlobal.esquema;
	// $scope.filtros.esquema = esquema;
	var version = $scope.complementosMain.confGlobal.version;
	$scope.filtros.fechaPago = version;

	$scope.datos = {};

	
	if ($scope.complementosMain.confGlobal.esquema == 'DJ1949' ) {
		var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalificacionesPendientesDJ1949();
	};
	
	if ($scope.complementosMain.confGlobal.esquema == 'DJ1922' ) {
		var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalificacionesPendientesDJ1922();
	};
	
    console.log('conf.buscar', conf);
	$scope.defaultOptions = conf.options;
	$scope.buscarCalificacionesPendientes = conf.buscar;
	$scope.winID=bcsComunes.ventanaWinId($scope);
	
  	$scope.defaultOptions.gridCreated = function(grid){
		$scope.ajustaVentana();
	}
  	
  	$scope.ajustaVentana = function()
    {
		 bcsComunes.ventanaTamanoAutomatico($scope.winID,40);
    }
	

	$scope.exportarExcel = function () {



		if ($scope.complementosMain.confGlobal.esquema == 'DJ1949' ) {
			var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalificacionesPendientesDJ1949();
		};
		
		if ($scope.complementosMain.confGlobal.esquema == 'DJ1922' ) {
			var conf = bcsConfiguracionMaestroDeclaraciones.getConsultaCalificacionesPendientesDJ1922();
		};
		
		var grid =conf.options;
		
		if (!grid) {
			console.error("No se encontro Grid");
			return;
		}


		// =========================
		// DATA REAL 
		// =========================

		var data = $scope.apiGridCalificacionesPendientes.dataSource.data();

		console.log("TOTAL DATA:", data.length);

		if (!data || !data.length) {
			console.error("No hay datos");
			return;
		}

		// =========================
		// OBTENER COLUMNAS
		// =========================

		var gridColumns = [];

		grid.columns.forEach(function (col) {

			if (col.columns && col.columns.length) {

				col.columns.forEach(function (subCol) {
					gridColumns.push(subCol);
				});

			} else {

				gridColumns.push(col);
			}
		});

		// =========================
		// EXCEL
		// =========================

		var rows = [];

		// titulos
		rows.push({
			cells: gridColumns.map(function (col) {

				return {
					value: col.title || col.field || "",
					bold: true
				};

			})
		});

		// DATA
		data.forEach(function (item, index) {

			var row = item.toJSON ? item.toJSON() : item;

			// DEBUG SOLO PRIMERA FILA
			if (index === 0) {

				gridColumns.forEach(function (col) {

					console.log("================================");
					console.log("TITLE:", col.title);
					console.log("FIELD:", col.field);
					console.log("VALUE:", row[col.field]);
				});
			}

			rows.push({
				cells: gridColumns.map(function (col) {

					return {
						value: row[col.field]
					};

				})
			});

		});

		console.log("ROWS GENERADAS:", rows);

		// =========================
		// Hoja de excel
		// =========================

		var workbook = new kendo.ooxml.Workbook({
			sheets: [{
				title: "CalificacionesPendientes",
				columns: gridColumns.map(function () {
					return { autoWidth: true };
				}),
				rows: rows
			}]
		});

		// =========================
		// DESCARGA
		// =========================
		const hoy = new Date();

		const dia = String(hoy.getDate()).padStart(2, '0');
		const mes = String(hoy.getMonth() + 1).padStart(2, '0');
		const anio = hoy.getFullYear();

		const fechaActual = `${dia}-${mes}-${anio}`;

		kendo.saveAs({
			dataURI: workbook.toDataURL(),
			fileName: `CalificacionesPendientes_${fechaActual}.xlsx`
		});
	};

	var exportFn = $scope.exportarExcel;
	
	
		$timeout(function () {

		$(".k-context-menu").each(function () {

			var menu = $(this).data("kendoContextMenu");

			if (menu) {

				menu.bind("select", function (e) {

					var text = $(e.item).text().trim().toUpperCase();

					console.log("CLICK MENU:", text); 

					if (text.includes('EXPORTAR')) {
						console.log("ENTRANDO A EXPORTAR");
						 
						 exportFn();
					}

				});

			}
		});

	}, 1500);


});