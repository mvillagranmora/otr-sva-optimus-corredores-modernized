// Parseo del archivo Datatec y calculo de puntas/margen.
//
// Es la misma logica de negocio que vive dentro del controlador DatatecCtl de la ventana
// Carga Online Datatec (Mesa/UI/SPT/Datatec/Datatec.js). Se extrae aqui para que la ventana
// Datatec Monitor Automatico no dependa de ese controlador. La ventana antigua conserva su copia
// y no se modifica: cualquier cambio de reglas de punta, de margen o de formato de archivo
// debe aplicarse en AMBOS lugares mientras las dos ventanas coexistan.
angular.module('ModuloMesa').factory('bcsDatatecProceso', function($translate, $log) {

	function ltrim(s) { return s.replace(/^\s+/, ""); }
	function rtrim(s) { return s.replace(/\s+$/, ""); }
	function trim(s)  { return rtrim(ltrim(s)); }

	function cleanLastBlankArray(row) {
		if (row[row.length-1].trim().length == 0)
			row.pop(row.length-1);
	}

	function parseField(fieldConfig, value) {
		if (fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase() === "STRING") {
			value = String(value);
		} else if (fieldConfig.TipoDato && fieldConfig.TipoDato.toUpperCase() === "NUMBER") {
			//El controlador antiguo envuelve esto en un try/catch que llama a $log sin
			//haberlo inyectado: al fallar el parseo revienta el catch y se cae el archivo
			//completo. Aqui se respeta la intencion original (registrar y continuar).
			try {
				if (fieldConfig.IncluyeDecimales === "S") {
					value = util.incluirDecimales(value.replace(/,|\./g,''), fieldConfig.CantidadDecimales);
				} else {
					value = parseInt(value);
				}
			} catch(e) {
				$log.debug(e);
			}
		}
		return value;
	}

	function validateField(fieldConfig, value) {
		if (fieldConfig.EsObligatorio === "S" && value.length <= 0) {
			return fieldConfig.CodigoCampo;
		}
		if (fieldConfig.Validacion && fieldConfig.Validacion.trim().length > 0) {
			var re = new RegExp(fieldConfig.Validacion);
			if (!re.test(value)) {
				return fieldConfig.CodigoCampo;
			}
		}
		return "OK";
	}

	function allCamposObligatorios(config) {
		for (var key in config.campos) {
			if (config.campos[key].EsObligatorio == 'S' && !config.campos[key].Existe) {
				return false;
			}
		}
		return true;
	}

	var svc = {};

	// Arma la configuracion de parseo a partir de las columnas de PLA.ArchivoFormato.
	svc.construirConfiguracion = function(configColumnas, codigoArchivo, conHeader) {
		var config = {
			codigoArchivo : codigoArchivo,
			conHeader     : conHeader === true,
			headers       : [],
			campos        : {},
			tieneSeperador: configColumnas[0].TieneSeparador
		};

		if (config.tieneSeperador === 'S') {
			config.SeparadorColumnas = configColumnas[0].SeparadorColumnas;
		}

		angular.forEach(configColumnas, function(value) {
			value.Existe = false;
			config.headers.push(value.CodigoCampo);
			config.campos[value.CodigoCampo] = value;
		});

		return config;
	};

	// Parsea el contenido del archivo. A diferencia del controlador antiguo no acumula sobre
	// arreglos externos: devuelve el resultado completo de esta llamada, de modo que dos
	// ejecuciones seguidas no puedan mezclar filas.
	//
	// Devuelve { errorCabecera, filas, validas, errores }
	//   errorCabecera : {codigo, mensaje} si la cabecera no cumple; en ese caso el resto va vacio
	//   filas         : todas las filas parseadas, con MensajeValidacion
	//   validas       : subconjunto de filas con MensajeValidacion === "OK" (lo que se envia)
	//   errores       : filas rechazadas, con la forma de la grilla de log
	svc.parsearArchivo = function(contenido, config) {
		var filas   = [];
		var validas = [];
		var errores = [];
		var errorCabecera = null;

		var csvRows    = String(contenido).split(/\n/);
		var csvHeaders = [];
		var largoRow   = 0;

		for (var i = 0; i < csvRows.length; ++i) {
			csvRows[i] = csvRows[i].replace(/\0/g, " ");
		}

		for (var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex) {
			var fileRow = csvRows[rowIndex].replace(/[\r\n]+/g, "");
			var rowArray = [];

			//Ultima fila vacia: fin del archivo
			if (rowIndex == csvRows.length-1 && fileRow.trim().length == 0) {
				break;
			}

			if (rowIndex == 0) {
				var firstRow = [];
				if (config.tieneSeperador === 'S') {
					firstRow = csvRows[rowIndex].split(new RegExp(config.SeparadorColumnas));

					if (config.conHeader === true) {
						cleanLastBlankArray(firstRow);

						for (var k in firstRow) {
							var campoKey = firstRow[k].trim();
							if (config.campos[campoKey]) {
								config.campos[campoKey].Existe = true;
							}
							csvHeaders.push(campoKey);
						}

						for (var c in config.campos) {
							var formato = config.campos[c];
							if (formato.EsObligatorio === 'S') {
								if (!formato.Existe || fileRow.indexOf(formato.CodigoCampo) < 0) {
									errorCabecera = {
										codigo : "ERROR",
										mensaje: $translate.instant("UI_CAMPO_XXX_OBLIGATORIO").replace("XXX", formato.CodigoCampo)
									};
								}
							}
						}
						continue;
					} else {
						//Tiene separador pero no tiene cabecera
						var cantidadCamposObligatorios = 0;
						for (var c2 in config.campos) {
							if (config.campos[c2].EsObligatorio === "S") {
								cantidadCamposObligatorios++;
							}
							csvHeaders.push(config.campos[c2].CodigoCampo);
						}
						if (firstRow.length >= cantidadCamposObligatorios) {
							for (var c3 in config.campos) {
								if (config.campos[c3].EsObligatorio === "S") {
									config.campos[c3].Existe = "S";
								}
							}
						}
					}
				} else {
					//Sin separador: el largo de la fila lo define la configuracion
					for (var h in config.headers) {
						largoRow += parseInt(config.campos[config.headers[h]].LargoCampo);
					}
				}
			}

			var data = {};

			if (config.tieneSeperador === 'S' && allCamposObligatorios(config)) {
				//Parse por separador
				rowArray = fileRow.split(new RegExp(config.SeparadorColumnas));

				var validacionSep = "OK";
				for (var r in rowArray) {
					data[csvHeaders[r]] = parseField(config.campos[csvHeaders[r]], rowArray[r]);
					var mensajeSep = validateField(config.campos[csvHeaders[r]], rowArray[r]);
					if (mensajeSep != "OK")
						validacionSep = $translate.instant("UI_FALTA_CAMPO_XXX_ARCHIVO").replace("XXX", mensajeSep);
				}
				if (rowArray.length != csvHeaders.length) {
					validacionSep = $translate.instant("UI_FALTA_CAMPO");
				}
				data["MensajeValidacion"] = validacionSep;
				filas.push(data);
			}
			else if (config.tieneSeperador === 'N') {
				//Parse por largo fijo
				var posicionInicial = 0;
				var posicionFinal   = 0;
				var valor = undefined;
				var validacion = "OK";

				for (var p in config.headers) {
					posicionFinal = parseInt(config.campos[config.headers[p]].LargoCampo) + posicionInicial;

					valor = parseField(config.campos[config.headers[p]], fileRow.substring(posicionInicial, posicionFinal));
					if (typeof valor === 'string') {
						valor = valor.trim();
					}
					data[config.headers[p]] = valor;

					var mensaje = validateField(config.campos[config.headers[p]], fileRow.substring(posicionInicial, posicionFinal));
					if (mensaje != "OK")
						validacion = mensaje;

					posicionInicial = posicionFinal;
				}

				if (largoRow != fileRow.length) {
					validacion = $translate.instant("UI_FILA_XXX_CON_ERROR").replace("XXX", String(rowIndex+1)) + "("+largoRow+"|"+fileRow.length+")";
				}

				data["MensajeValidacion"] = validacion;
				filas.push(data);

				if (data.MensajeValidacion === "OK") {
					validas.push(data);
				} else {
					errores.push({
						NumeroRegistro  : Number(trim(data.CONTADOR)),
						GlosaProceso    : data.MensajeValidacion.toUpperCase(),
						IdEstadoRegistro: 'ERR'
					});
				}
			}
		}

		if (errorCabecera) {
			return { errorCabecera: errorCabecera, filas: [], validas: [], errores: [] };
		}
		return { errorCabecera: null, filas: filas, validas: validas, errores: errores };
	};

	// Busca las puntas del tramo que corresponde al monto.
	//
	// Replica el comportamiento del controlador antiguo, incluido el recorrido sin corte: al
	// no interrumpir el ciclo, se queda con el ULTIMO tramo que cumple monto >= MontoPorTramo,
	// no con el primero. Se preserva a proposito para que ambas ventanas calculen el mismo
	// margen sobre el mismo archivo; corregirlo aqui las haria divergir.
	svc.obtenerCostoPorTramo = function(monto, idOperacion, datosTramos) {
		var ultimo = datosTramos[datosTramos.length-1];
		var puntas = null;

		if (monto > ultimo.MontoPorTramo) {
			puntas = {
				"VALORPUNTACOMPRA": ultimo.PuntaCompraDefault || '',
				"VALORPUNTAVENTA" : ultimo.PuntaVentaDefault  || '',
				"MONTOPUNTA"      : (idOperacion === 'COMSPT' ? ultimo.PuntaCompra : ultimo.PuntaVenta) || ''
			};
		}
		else if (monto < datosTramos[0].MontoPorTramo) {
			puntas = {
				"VALORPUNTACOMPRA": ultimo.PuntaCompraDefault || '',
				"VALORPUNTAVENTA" : ultimo.PuntaVentaDefault  || '',
				"MONTOPUNTA"      : (idOperacion === 'COMSPT' ? datosTramos[0].PuntaCompra : datosTramos[0].PuntaVenta) || ''
			};
		}
		else {
			angular.forEach(datosTramos, function(value) {
				if (monto >= value.MontoPorTramo) {
					puntas = {
						"VALORPUNTACOMPRA": ultimo.PuntaCompraDefault || '',
						"VALORPUNTAVENTA" : ultimo.PuntaVentaDefault  || '',
						"MONTOPUNTA"      : (idOperacion === 'COMSPT' ? value.PuntaCompra : value.PuntaVenta) || ''
					};
				}
			});
		}

		return puntas;
	};

	// Determina compra/venta segun de que lado esta la corredora, calcula el margen y aplica
	// las puntas configuradas. Muta las filas recibidas, igual que el controlador antiguo.
	svc.aplicarPuntas = function(filas, datosPuntas, nombreCorredora) {
		var cabecera = datosPuntas[0];

		angular.forEach(filas, function(value) {
			if (trim(value.INSTITUCIONPOSTURA) === nombreCorredora) {
				value.IDOPERACION = value.OD === 'D' ? 'COMSPT' : 'VENSPT';
			} else {
				value.IDOPERACION = value.OD === 'D' ? 'VENSPT' : 'COMSPT';
			}

			if (value.IDOPERACION === 'COMSPT') {
				value.MARGEN = ((value.MONTOPUNTA||0) - (value.PRECIO||0)) * (value.NUMEROTRANSADO||0);
			} else if (value.IDOPERACION === 'VENSPT') {
				value.MARGEN = ((value.PRECIO||0) - (value.MONTOPUNTA||0)) * (value.NUMEROTRANSADO||0);
			}

			if (cabecera.CheckPrecioPorTramo === 'NO') {
				value.VALORPUNTACOMPRA = cabecera.PuntaCompraDefault;
				value.VALORPUNTAVENTA  = cabecera.PuntaVentaDefault;
				if (value.IDOPERACION === 'COMSPT') {
					value.MONTOPUNTA = cabecera.PuntaCompra || '';
				} else if (value.IDOPERACION === 'VENSPT') {
					value.MONTOPUNTA = cabecera.PuntaVenta || '';
				}
			}
			else if (cabecera.CheckPrecioPorTramo === 'SI') {
				var x = svc.obtenerCostoPorTramo(trim(value.NUMEROTRANSADO), value.IDOPERACION, datosPuntas);
				if (x) {
					value.VALORPUNTACOMPRA = x.VALORPUNTACOMPRA;
					value.VALORPUNTAVENTA  = x.VALORPUNTAVENTA;
					value.MONTOPUNTA       = x.MONTOPUNTA;
				}
			}
		});
	};

	return svc;
});
