// Acceso local al archivo Datatec que el operador selecciona directamente en el picker.
//
// Usa File System Access API, con tres restricciones que condicionan el diseno:
//   - Solo Chromium (Chrome/Edge) y solo en secure context (HTTPS).
//   - El archivo lo concede el operador con un gesto explicito (showOpenFilePicker): no
//     existe forma de preconcederlo por politica ni de resolver una ruta absoluta desde
//     codigo. El nombre configurado (NOMBREARCHIVODATATEC) es solo informativo: el
//     operador puede elegir cualquier archivo, no se valida contra ese nombre.
//   - El handle se persiste en IndexedDB y sobrevive reinicios, pero el permiso puede
//     volver a estado 'prompt' cuando se cierran todas las pestanas del sitio; en ese
//     caso se requiere un clic para reautorizar.
angular.module('ModuloMesa').factory('bcsDatatecFs', function($q) {

	var DB_NAME = 'bcsDatatec';
	var STORE   = 'handles';
	var KEY     = 'archivoDatatec';

	function abrirDb() {
		return new Promise(function(resolve, reject) {
			var req = indexedDB.open(DB_NAME, 1);
			req.onupgradeneeded = function() {
				req.result.createObjectStore(STORE);
			};
			req.onsuccess = function() { resolve(req.result); };
			req.onerror   = function() { reject(req.error); };
		});
	}

	function txDb(modo, fn) {
		return abrirDb().then(function(db) {
			return new Promise(function(resolve, reject) {
				var tx  = db.transaction(STORE, modo);
				var req = fn(tx.objectStore(STORE));
				req.onsuccess = function() { resolve(req.result); };
				req.onerror   = function() { reject(req.error); };
			});
		});
	}

	function guardarHandle(handle) {
		return txDb('readwrite', function(store) { return store.put(handle, KEY); });
	}

	function leerHandle() {
		return txDb('readonly', function(store) { return store.get(KEY); });
	}

	function borrarHandle() {
		return txDb('readwrite', function(store) { return store.delete(KEY); });
	}

	var svc = {};

	svc.soportado = function() {
		return typeof window.showOpenFilePicker === 'function' && window.isSecureContext === true;
	};

	// Abre el selector de archivo. Requiere gesto de usuario (ng-click) y persiste el
	// handle para que las siguientes sesiones no tengan que volver a elegirlo. Sin filtro
	// de tipo: el archivo Datatec puede tener cualquier extension.
	svc.conectar = function() {
		return $q.when(
			window.showOpenFilePicker({ id: 'datatec', multiple: false })
				.then(function(handles) {
					var handle = handles[0];
					return guardarHandle(handle).then(function() { return handle; });
				})
		);
	};

	// Recupera el handle persistido. permitirPrompt solo puede ser true bajo gesto de
	// usuario. Devuelve el handle si el permiso quedo concedido, null en cualquier otro caso.
	svc.reconectar = function(permitirPrompt) {
		return $q.when(
			leerHandle().then(function(handle) {
				if (!handle) return null;
				return handle.queryPermission({ mode: 'read' }).then(function(estado) {
					if (estado === 'granted') return handle;
					if (estado === 'prompt' && permitirPrompt) {
						return handle.requestPermission({ mode: 'read' }).then(function(nuevo) {
							return nuevo === 'granted' ? handle : null;
						});
					}
					return null;
				});
			})
		);
	};

	// Olvida el archivo conectado: borra el handle persistido para que la proxima llamada
	// a reconectar() no encuentre nada y conectarArchivo() tenga que abrir el picker de
	// nuevo, permitiendo elegir un archivo distinto.
	svc.olvidar = function() {
		return $q.when(borrarHandle());
	};

	// Lee el archivo solo si cambio respecto de la ultima carga y si quedo estable.
	//   ultimaVista.candidata : firma observada en el tick anterior (control de estabilidad)
	//   ultimaVista.procesado : firma del ultimo contenido efectivamente procesado
	// estado: LEIDO | SIN_CAMBIO | INESTABLE | NO_ENCONTRADO | BLOQUEADO | SIN_PERMISO
	svc.leerSiCambio = function(handle, ultimaVista) {
		return $q.when(
			handle.getFile()
				.then(function(file) {
					var firma = { size: file.size, lastModified: file.lastModified };

					if (ultimaVista.procesado &&
						ultimaVista.procesado.size === firma.size &&
						ultimaVista.procesado.lastModified === firma.lastModified) {
						return { estado: 'SIN_CAMBIO', firma: firma };
					}

					// El archivo puede estar a medio escribir: se exige la misma firma en
					// dos ticks consecutivos antes de darlo por completo.
					if (!ultimaVista.candidata ||
						ultimaVista.candidata.size !== firma.size ||
						ultimaVista.candidata.lastModified !== firma.lastModified) {
						return { estado: 'INESTABLE', firma: firma };
					}

					// La aplicacion Datatec escribe en iso-8859-1: file.text() decodificaria
					// como UTF-8 y corromperia los nombres de institucion, que son justamente
					// los que se comparan contra la razon social de la corredora.
					return file.arrayBuffer().then(function(buf) {
						return {
							estado: 'LEIDO',
							firma : firma,
							texto : new TextDecoder('iso-8859-1').decode(buf)
						};
					});
				})
				.catch(function(e) {
					var nombre = e && e.name;
					if (nombre === 'NotFoundError')    return { estado: 'NO_ENCONTRADO' };
					if (nombre === 'NotReadableError') return { estado: 'BLOQUEADO' };
					if (nombre === 'NotAllowedError')  return { estado: 'SIN_PERMISO' };
					if (nombre === 'SecurityError')    return { estado: 'SIN_PERMISO' };
					throw e;
				})
		);
	};

	return svc;
});
