angular.module('ModuloMesa').controller('CalculadoraSpotCtrl',
	function ($scope, bcsComunes, bcsServiciosMesa) {

		var pCalc = this;
		pCalc.idVentana = bcsComunes.ventanaWinId($scope);
		pCalc.win = {};
		pCalc.grilla = {};
		pCalc.cmbbx = {};
		pCalc.filtro = {};
		pCalc.fn = {};
		pCalc.Fecha = util.dateInicioDia($scope.SesionFechaSistema);

		var p_IdEmpresa = $scope.SesionIdEmpresa;
		var p_IdUsuario = $scope.SesionIdUsuario;
		var p_Tipoinstrumento;
		var p_Mesas;
		var parametros;
		var p_grillaAll;
		
		modeloCalc = {
			model: {
				fields: {
					Precio: { type: "number" },
					Fecha: { type: "sysdate" },
					Mesa: { type: "string" },
					TipoInstrumento: { type: "string" },
					PrecioCierre: { type: "number" },
					PrecioPromedioCompra: { type: "number" },
					PrecioPromedioVenta: { type: "number" },
					Posicion: { type: "number" },
					Breakeven: { type: "number" },
					UtilidadPerdida: { type: "number" },
					ComprasCLP: { type: "number" },
					VentasCLP: { type: "number" },
					CajaCLP: { type: "number" },
					ComprasUSD: { type: "number" },
					VentasUSD: { type: "number" },
					CajaUSD: { type: "number" },
					PromPrecioCierre: { type: "number" }, 
					PromPrecioCompra: { type: "number" },
					PromPrecioVenta: { type: "number" },
					TotalPosicion: { type: "number" },
					TotalUtilidadPerdida: { type: "number" },
					TotalCajaCLP: { type: "number" },
					TotalCajaUSD: { type: "number" }
				}
			}
		};

		dsCalculadoraSpot = new bcs.DataSource({
			type: "call",
			definition: {
				callTransaction: "4290702",
				serverFiltering: false,
				serverPaging: false,
				serverSorting: false,
				callParameters: {},
				callResultSet: "P_OUT_RESULTADO",
				schema: modeloCalc,
				pageSize: 7,
				aggregate: [
							{ field:"PromPrecioCierre", aggregate: "max" } ,
							{ field:"PromPrecioCompra", aggregate: "max" } ,
							{ field:"PromPrecioVenta", aggregate: "max" } ,
							{ field:"TotalPosicion", aggregate: "max" } ,
							{ field:"UtilidadPerdida", aggregate: "sum" } ,
			            	{ field: "CajaCLP", aggregate: "sum" } ,
			             	{ field: "CajaUSD", aggregate: "sum" } ,         
			              ],                 
			},
		});

		pCalc.grilla.confCalculadoraSpot = {
			dataSource: dsCalculadoraSpot,
			columns:	
				[
					  { field: "Precio", title: 'UI_PRECIO', width: "100px", format: "{0:n3}" }
					, { field: "Fecha", title: 'UI_FECHA', width: "100px" }
					, { field: "Mesa", title: 'UI_MESA', width: "100px" }
					, { field: "TipoInstrumento", title: 'UI_TIPO_INSTRUMENTO', width: "100px" }
					, { field: "PrecioCierre", title: 'UI_PRECIO_CIERRE', width: "100px", format: "{0:n3}" ,hidden: true, footerTemplate: "#=kendo.toString(PromPrecioCierre.max,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "PrecioCierre", title: 'UI_PRECIO_CIERRE', width: "100px", format: "{0:n3}" }
					, { field: "PrecioPromedioCompra", title: 'UI_PRECIO_PROMEDIO_COMPRA', width: "100px",  format: "{0:n3}" ,hidden: true, footerTemplate: "#=kendo.toString(PromPrecioCompra.max,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "PrecioPromedioCompra", title: 'UI_PRECIO_PROMEDIO_COMPRA', width: "100px", format: "{0:n3}" }
					, { field: "PrecioPromedioVenta", title: 'UI_PRECIO_PROMEDIO_VENTA', width: "100px",  format: "{0:n3}" ,hidden: true, footerTemplate: "#=kendo.toString(PromPrecioVenta.max,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "PrecioPromedioVenta", title: 'UI_PRECIO_PROMEDIO_VENTA', width: "100px", format: "{0:n3}" }
					, { field: "Posicion", title: 'UI_POSICION', width: "100px",  format: "{0:n3}" ,hidden: true, footerTemplate: "#=kendo.toString(TotalPosicion.max,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "Posicion", title: 'UI_POSICION', width: "100px", format: "{0:n3}" }
					, { field: "Breakeven", title: 'UI_BREAKEVEN', width: "100px", format: "{0:n3}" }
					, { field: "UtilidadPerdida", title: 'UI_UTILIDAD/PERDIDA', width: "100px",  format: "{0:n3}" ,hidden: true, footerTemplate: "#=kendo.toString(sum,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "UtilidadPerdida", title: 'UI_UTILIDAD/PERDIDA', width: "100px", format: "{0:n3}" }
					, { field: "ComprasCLP", title: 'UI_COMPRAS_CLP', width: "100px", format: "{0:n3}" }
					, { field: "VentasCLP", title: 'UI_VENTAS_CLP', width: "100px", format: "{0:n3}" }
					, { field: "CajaCLP", title: 'UI_CAJA_CLP', width: "100px", format: "{0:n3}" ,  hidden: true, footerTemplate: "#=kendo.toString(sum,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "CajaCLP", title: 'UI_CAJA_CLP', width: "100px", format: "{0:n3}" }
					, { field: "ComprasUSD", title: 'UI_COMPRAS_USD', width: "100px", format: "{0:n3}" }
					, { field: "VentasUSD", title: 'UI_VENTAS_USD', width: "100px", format: "{0:n3}" }
					, { field: "CajaUSD", title: 'UI_CAJA_USD', width: "100px", format: "{0:n3}" , hidden: true, footerTemplate: "#=kendo.toString(sum,'n3')#",footerAttributes: {style:'text-align:right; font-size:12px; font-weight:bold; padding-top:10px; padding-bottom:10px;'}}
					, { field: "CajaUSD", title: 'UI_CAJA_USD', width: "100px", format: "{0:n3}" ,  }  ,
				],
			userSettings: false,
			pageable: true,
			allowCopy: { delimeter: "\t" },
			sortable: true,
			filterable: false,
			height: 170,	
			excel: { allPages: true, },
			selectable: "row",
			autoBind: false,
			gridCreated: function (grid) {
				$scope.grillaAll = grid;
			},
		};
		
		pCalc.fn.aceptar = function () {
			if(pCalc.cmbbx.tipo_instrumento.seleccionado.Instrumento == ' TODOS'){	
				$scope.grillaAll.showColumn(4);
				$scope.grillaAll.showColumn(6);
				$scope.grillaAll.showColumn(8);
				$scope.grillaAll.showColumn(10);
				$scope.grillaAll.showColumn(13);
				$scope.grillaAll.showColumn(17);
				$scope.grillaAll.showColumn(21);
				$scope.grillaAll.hideColumn(5);
				$scope.grillaAll.hideColumn(7);
				$scope.grillaAll.hideColumn(9);
				$scope.grillaAll.hideColumn(11);
				$scope.grillaAll.hideColumn(14);
				$scope.grillaAll.hideColumn(18);
				$scope.grillaAll.hideColumn(22);	
			}else{
				$scope.grillaAll.showColumn(5);
				$scope.grillaAll.showColumn(7);
				$scope.grillaAll.showColumn(9);
				$scope.grillaAll.showColumn(11);
				$scope.grillaAll.showColumn(14);
				$scope.grillaAll.showColumn(18);
				$scope.grillaAll.showColumn(22);
				$scope.grillaAll.hideColumn(4);
				$scope.grillaAll.hideColumn(6);
				$scope.grillaAll.hideColumn(8);
				$scope.grillaAll.hideColumn(10);
				$scope.grillaAll.hideColumn(13);
				$scope.grillaAll.hideColumn(17);
				$scope.grillaAll.hideColumn(21);	
				
			}
		
			p_Mesas = {
				p_IdMesa: pCalc.cmbbx.mesa.seleccionado.IdMesa,
				p_DocumentoIdentidad: pCalc.cmbbx.mesa.seleccionado.DocumentoIdentidad,
				p_Nombre: (pCalc.cmbbx.mesa.seleccionado.IdMesa == 0) ? ' TODOS' : pCalc.cmbbx.mesa.seleccionado.Nombre,
			};

			p_Tipoinstrumento = {
				p_IdInstrumento: (pCalc.cmbbx.tipo_instrumento.seleccionado.Instrumento == ' TODOS') ? 0 : pCalc.cmbbx.tipo_instrumento.seleccionado.IdInstrumento,
				p_Instrumento: pCalc.cmbbx.tipo_instrumento.seleccionado.Instrumento,
			};

			p_valorFecha = {
				p_valorUSD: pCalc.win.precio,
				p_Fecha: pCalc.Fecha,
			};
			parametros = { p_IdEmpresa, p_IdUsuario, p_Mesas, p_Tipoinstrumento, p_valorFecha, p_grillaAll }
			dsCalculadoraSpot.removeAll();
			dsCalculadoraSpot.setCallParameters(parametros);	
		};

		pCalc.cmbbx.mesa = {
			filtros: {
				IdTipoProducto: { "field": "IdTipoProducto", "operator": "eq", "value": "MEME" },
			}
		};

		pCalc.cmbbx.tipo_instrumento = {
			filtros: {
				NOARBITR: { "field": "Nemotecnico", "operator": "neq", "value": "ARBITR" },
				NOARBDIS: { "field": "Nemotecnico", "operator": "neq", "value": "ARBDIS" },
				NOARBINT: { "field": "Nemotecnico", "operator": "neq", "value": "ARBINT" },
				NOARBDIS: { "field": "Nemotecnico", "operator": "neq", "value": "ARBDIS" },
				NOSECANO: { "field": "Nemotecnico", "operator": "neq", "value": "SECANO" },
				NOSECARE: { "field": "Nemotecnico", "operator": "neq", "value": "SECARE" },
				NOSEINFL: { "field": "Nemotecnico", "operator": "neq", "value": "SEINFL" },
			}
		};

		pCalc.limpiar = function () {
			pCalc.Fecha = util.dateInicioDia($scope.SesionFechaSistema);
			pCalc.cmbbx.mesa.seleccionado = "TODOS";
			pCalc.cmbbx.tipo_instrumento.seleccionado = "TODOS";
			dsCalculadoraSpot.removeAll();
			p_grillaAll = new Array();
			pCalc.cmbbx.mesa.Actual = "";
			pCalc.cmbbx.tipo_instrumento.Actual = " TODOS";
			pCalc.buscaDolar();
		}

		var datos = {};
		(pCalc.buscaDolar = function () {
			datos.p_IdEmpresa = p_IdEmpresa;
			datos.p_IdMoneda = 'USD';
			datos.p_Fecha = util.dateSPdate(pCalc.Fecha);
			bcsServiciosMesa.ConsultarPosicionInicial(datos).$promise.then(function (respuesta) {
				bcsComunes.setConflictos(util.parseServerResponse(respuesta));
				if (respuesta.P_OUT_POSICION.length === 0) {
					pCalc.win.precioM = 1;
				}
				else {
					pCalc.win.precioM = respuesta.P_OUT_POSICION[0].CierreIngresado;
				}
			})		
		}
		)();
	
	});