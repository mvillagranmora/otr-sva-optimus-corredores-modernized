angular.module('ModuloNormativa').controller(
		'DetailController',
		[ '$scope', '$translate', 'bcsComunes','$state', 'Model', 'Utils','bcsServicioPlataforma',
				DetailController ]);

function DetailController($scope, $translate, bcsComunes,$state, Model, Utils,bcsServicioPlataforma) {
	var ppdg = this;
	var idVentana = bcsComunes.ventanaWinId($scope);
	var windowParams = Utils.getWindowParams($scope);
	var accion = windowParams.item == null ? $translate.instant('UI_CREAR')
			: $translate.instant('UI_MODIFICAR');

	if(windowParams.Model){
		Model = windowParams.Model
	}
	
	Utils.setWindowTitle($scope, accion + ' ' + Model.getEsquema() + '-'
			+ Model.getVersion());

	$scope.configSocioNegocio = {
		filtros : {
			IdEmpresa : {
				'field' : 'IdEmpresa',
				'operator' : 'eq',
				'value' : $scope.SesionIdEmpresa
			},
			EstadoCliente : {
				'field' : 'EstadoCliente',
				'operator' : 'eq',
				'value' : 'ACT'
			},
			EsCliente : {
				'field' : 'EsCliente',
				'operator' : 'eq',
				'value' : 'UI_SI'
			}
		},
	};

	$scope.configPortafolio = {
		filtros : {
			IdCliente : {
				'field' : 'IdCliente',
				'operator' : 'eq',
				'value' : 0
			},
			TienePropositoAPV : {
				'field' : 'TienePropositoAPV',
				'operator' : 'eq',
				'value' : windowParams.TienePropositoAPV
			}
		},
	};
	
	
	// Variables referenciadas por el selector de cliente.
	$scope.cliente = {};
	$scope.cliente.mostrarSelector = Model.existsField('RutCliente') || Model.existsField('RutContraparte');
	$scope.cliente.rutInicial = '';
	$scope.cliente.buscar = false;
	$scope.cliente.seleccionado ={};
	
	
	
	
	
	if ($scope.cliente.mostrarSelector) {
		if (windowParams.item != null) {
			$scope.cliente.rutInicial = windowParams.item['RutCliente'] ?? windowParams.item['RutContraparte'];
			//$scope.cliente.rutInicial = util.formatRut(windowParams.item['RutCliente']);
			$scope.cliente.buscar = true;
		}

		$scope
				.$watch(
						'cliente.seleccionado',
						function(newValue, oldValue) {
							
							//if (newValue) {	
								//typeof
								console.log(newValue, $('#RutCliente')[0])
								if ( newValue.IdSocioDeNegocio !== undefined && newValue.IdCliente  !== undefined) {
									if ($('#RutCliente')[0]) {
										$('#RutCliente')[0].value = newValue.DocumentoIdentidadDefecto;
									}
									if ($('#RutContraparte')[0]) {
										$('#RutContraparte')[0].value = newValue.DocumentoIdentidadDefecto;
									}
									if (Model.existsField('IdCliente')) {
										
										if ($('#IdCliente')[0]){
											$('#IdCliente')[0].value = newValue.IdCliente;
										}
										
										//$scope.cliente.buscar = false;
										
									}
								} else {
									/*
									$('#RutCliente')[0].value = null;
									if (Model.existsField('IdCliente')) {
										$('#IdCliente')[0].value = null;
									}
									*/
								}
								
								
								
							//}
							
						});
		
					
	}
	

	// Variables referenciadas por el selector de portafolio.
	$scope.portafolio = {};
	$scope.portafolio.mostrarSelector = Model.existsField('IdPortafolio');
	if (windowParams.item != null) {
		
		$scope.portafolio.id = windowParams.item['IdPortafolio'];
	};
	
		if ($scope.portafolio.mostrarSelector) {
			
				$scope.$watch('portafolio.seleccionado', function(newValue, oldValue) {
					if (newValue != null) {
						if (typeof newValue.IdPortafolio !== 'undefined') {
							if($('#IdPortafolio')[0]){
								$('#IdPortafolio')[0].value = newValue.IdPortafolio;
							}
						} else {
							//$('#IdPortafolio')[0].value = null;
						}
					}
				});
		}
		
		
	
/*
	$scope.portafolio = {};
	$scope.portafolio.seleccionado  = {};
	$scope.portafolio.id = null;
	$scope.portafolio.mostrarSelector = Model.existsField('IdPortafolio');
	if ($scope.portafolio.mostrarSelector) {
		
		if (windowParams.item != null) {
			$scope.portafolio.id = windowParams.item['IdPortafolio'];
			$scope.portafolio.seleccionado.IdPortafolio=$scope.portafolio.id;
			//$('#IdPortafolio')[0].value = $scope.portafolio.id;
			 
		}else{
			
			$scope.$watch('portafolio.seleccionado', function(newValue, oldValue) {
				if (newValue != null) {
					if (typeof newValue.IdPortafolio !== 'undefined') {
						$('#IdPortafolio')[0].value = newValue.IdPortafolio;
					} else {
						//$('#IdPortafolio')[0].value = null;
					}
				}
			});
			
		}
		
		
		
		
	

		
	}
	*/
	
	// Variables referenciadas por el selector de nemotecnico.
	
	$scope.instrumento = {}
	$scope.instrumento.seleccionado  ={};
	$scope.instrumento.mostrarSelector = Model.existsField('Nemotecnico');
	$scope.instrumento.nemoInicial='';
	if ($scope.instrumento.mostrarSelector) {
		if (windowParams.item != null) {
			$scope.instrumento.nemoInicial = windowParams.item['Nemotecnico'];
		}
		$scope.$watch('instrumento.seleccionado', function(newValue, oldValue) {
			if (newValue != null) {
				//if (typeof newValue.Nemotecnico !== 'undefined') {
				if (newValue.Nemotecnico !== 'undefined') {
					$('#Nemotecnico')[0].value = newValue.Nemotecnico;
					if (Model.existsField('RutPagador') && typeof newValue.RutEmisor !== 'undefined') {
						$('#RutPagador')[0].value = newValue.RutEmisor;
					}
					if (Model.existsField('NombreEmisor') && typeof newValue.RazonSocialEmisor !== 'undefined') {
						$('#NombreEmisor')[0].value = newValue.RazonSocialEmisor;
					}
				} else {
					$('#Nemotecnico')[0].value = null;
					if (Model.existsField('RutPagador')) {
						$('#RutPagador')[0].value = null;
					}
					if (Model.existsField('NombreEmisor') && typeof newValue.RazonSocialEmisor !== 'undefined') {
						$('#NombreEmisor')[0].value = newValue.RazonSocialEmisor;
					}
				}
			}
		});
	}

	$scope.hide1 = false;
	$scope.hide2 = true;
	$scope.hide3 = true;
	$scope.hide4 = true;
	$scope.hide5 = true;
	$scope.hide6 = true;
	$scope.hide7 = true;
	$scope.hide8 = true;
	$scope.hide9 = true;

	function hidePage(n) {
		$scope['hide' + n] = true;
	}

	function showPage(n) {
		$scope['hide' + n] = false;
	}

	$scope.botonAnteriorHabilitado = false;
	$scope.botonSiguienteHabilitado = false;

	var campos = Model.getCampos();

	if (windowParams.item == null && !(Model.getEsquema().indexOf('DJ999') === 0 || Model.getEsquema().indexOf('DJ1') === 0)) {
		campos.forEach(function(campo) {
		campo.isEditable = function() { return true; };
		});
	}

	var template = kendo.template($('#fieldsTemplate').html(), {
		useWithBlock : true
	});

	$scope.titulos = [];
	$scope.paginaActual = 1;
	$scope.totalPages = 0;

	var currentGroup = '';
	var pageFields = [];
	function renderPage() {
		$scope.templateParams = {
			fields : pageFields,
			masivo : windowParams.masivo
		}
		$scope.totalPages++;
		$scope.titulos[$scope.totalPages] = currentGroup;
		$('#formContainer' + $scope.totalPages).html(
				template($scope.templateParams));
		pageFields = [];
	}

	for (var i = 0; i < campos.length; i++) {
		var campo = campos[i];
		if (!campo.isHiddenInForm()) {
			if (currentGroup.length > 0 && campo.Grupo != currentGroup) {
				if (pageFields.length > 0) {
					renderPage();
				}
			}
			currentGroup = campo.Grupo;
			pageFields.push(campo);
		}
	}

	if (pageFields.length > 0) {
		renderPage();
	}

	$scope.botonGrabarHabilitado = false;
	$scope.onChange = function() {
		$scope.botonGrabarHabilitado = isFormValido();
	}

	for (var i = 0; i < campos.length; i++) {
		var campo = campos[i];
		if (!campo.isHiddenInForm()) {

			bindControl(campo);

			if (windowParams.item != null) {
				var value = windowParams.item[campo.Nombre];
				if (value != null && campo.isDate()) {
					value = value.toISOString().substring(0, 10);
				}

				initControl(campo, value);
			}
		}

		$scope.onChange();
	}

	function initControl(campo, value) {
		if (campo.isList()) {
			$('#' + campo.Nombre).data('kendoDropDownList').value(value);
		} else if (campo.isNumber()) {
			$('#' + campo.Nombre).data('kendoNumericTextBox').value(value);
		} else {
			$('#' + campo.Nombre)[0].value = value;
		}
	}

	function bindControl(campo) {
		if (campo.isDate()) {
			bindDateControl(campo);
		} else if (campo.isList()) {
			bindListControl(campo);
		} else if (campo.isDecimal()) {
			bindDecimalControl(campo);
		} else if (campo.isNumber()) {
			bindNumberControl(campo);
		} else {
			bindTextControl(campo);
		}
	}

	function bindDateControl(campo) {
		$('#' + campo.Nombre).kendoDatePicker({
			change : $scope.onChange
		});
	}

	function bindListControl(campo) {
		$('#' + campo.Nombre).kendoDropDownList({
			dataTextField : 'Descripcion',
			dataValueField : 'Codigo',
			dataSource : Model.getDiccionario(campo.TipoDiccionario, false),
			autobind : true,
			index : 0,
			change : $scope.onChange
		});
	}

	function bindDecimalControl(campo) {
		var dec = campo.CantidadDecimales;
		var fmt = 'n' + dec;
		$('#' + campo.Nombre).kendoNumericTextBox({
			decimals : dec,
			format : fmt,
			change : $scope.onChange,
			spin : $scope.onChange
		});
	}

	function bindNumberControl(campo) {
		$('#' + campo.Nombre).kendoNumericTextBox({
			decimals : 0,
			format : 'n0',
			change : $scope.onChange,
			spin : $scope.onChange
		});
	}

	function bindTextControl(campo) {
		// $('#' + campo.Nombre).kendoAutoComplete({
		// 	change : $scope.onChange
		// });
		var esquema = campo.Esquema;
		// console.log(esquema);
		if(esquema === 'REPO2P1' || esquema === 'REPO3P1'){
			$('#' + campo.Nombre).on('change', $scope.onChange);
		}else{
		$('#' + campo.Nombre).kendoAutoComplete({
			change : $scope.onChange
		});
		}
	}

	habilitarBotones();
	var campoIdDocumento = Model.getCampo('IdDocumento');
	if (!campoIdDocumento.isHiddenInForm()) {
		$('#IdDocumento')[0].value = windowParams.idDocumento;
	}
	
	$scope.grabarDetalle = function() {
		if (windowParams.masivo) {
			$scope.grabarDetalleMasivo();
		} else {
			$scope.grabarDetalleSimple();
		}
	}

	$scope.grabarDetalleSimple = function() {
		var params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_UsrUltMod : $scope.SesionUsuario,
			p_IdModulo : Model.getModulo()
		};

		for (var i = 0; i < campos.length; i++) {
			var campo = campos[i];
			if (campo.isHiddenInForm()) {
				if (windowParams.item != null) {
					params['p_' + campo.Nombre] = windowParams.item[campo.Nombre];
				}
			} else {
				var val = $('#' + campo.Nombre)[0].value;
				if (campo.isDate() && val != null && typeof val === 'string') {
					if (campo.isDate() && val == '')
						{
							val = Utils.getNormalDate('0001-01-01');
						}else{
							
							val = Utils.getNormalDate(val);
						};
					
				} else if (campo.isDecimal() && val != null
						&& typeof val === 'string') {
					val = val.replace(',', '.');
				}
				if ((campo.isNumber() && val == '') || (campo.isDecimal() && val == ''))
				{
					val='0';
				};
				

				params['p_' + campo.Nombre] = val;
			}
		}

		params['p_IdDocumento'] = windowParams.idDocumento;

		if (Utils.isEmptyValue(params['p_ID'])) {
			params['p_ID'] = 0;
		}

		if (Utils.isEmptyValue(params['p_IdBaseDocumento'])) {
			params['p_IdBaseDocumento'] = 0;
		}

		if (Utils.isEmptyValue(params['p_IdPortafolio'])) {
			params['p_IdPortafolio'] = 0;
			if ($scope.portafolio.seleccionado != null) {
				params['p_IdPortafolio'] = $scope.portafolio.seleccionado.IdPortafolio;
			}
		}

		if (Model.getEsquema()=='DJ1'){
			Model.callServiceById(4290709, params, function(reply) {
				windowParams.gridScope.buscarDatos();
				bcsComunes.msg('info', 'Grabar Detalle', 'OK');
				bcsComunes.ventanaCerrar(idVentana);
			}, function(error) {
				var msg = "Error";
				if (typeof error.message !== 'undefined') {
					msg = error.message;
				} else if (typeof error.statusText !== 'undefined') {
					msg = error.statusText;
				}
				bcsComunes.msg('error', 'Grabar Detalle', msg);
			});
		} else {
			Model.callService('GrabarDetalle', params, function(reply) {
				windowParams.gridScope.seteaFiltroGrilla=false;				
				windowParams.gridScope.buscarDatos();
				var msj = ""; 
				if (reply.P_OUT_ADVERTENCIASCONTADOR > 0) {
					msj = reply.P_OUT_ADVERTENCIASDETALLE[0].Descripcion;
					if (msj.startsWith("VALIDACION->")) {
						var msjVAL = msj.replace("VALIDACION->", "");
       					bcsComunes.msg('warning', 'Validación', msjVAL);
					} else {
						bcsComunes.msg('warning','Grabar Detalle', msj);
					}

				} else if (reply.P_OUT_ERRORESCONTADOR > 0) {
					msj = reply.P_OUT_ERRORESDETALLE[0].Descripcion;
					bcsComunes.msg('error','Grabar Detalle', msj);

				} else {
					msj = "OK";
					bcsComunes.msg('info', 'Grabar Detalle', 'OK');
				}

				if (!msj.startsWith("VALIDACION->")) {
					bcsComunes.ventanaCerrar(idVentana);
				}
				
			}, function(error) {
				var msg = "Error";
				if (typeof error.message !== 'undefined') {
					msg = error.message;
				} else if (typeof error.statusText !== 'undefined') {
					msg = error.statusText;
				}
				bcsComunes.msg('error', 'Grabar Detalle', msg);
			});
		}
	}

	$scope.grabarDetalleMasivo = function() {
		var params = windowParams.gridScope.getFiltros();
		params.p_IdEmpresa = $scope.SesionIdEmpresa;
		params.p_IdUsuario = $scope.SesionIdUsuario;
		params.p_IdModulo = Model.getModulo();
		params.p_UsrUltMod = $scope.SesionUsuario;

		for (var i = 0; i < campos.length; i++) {
			var campo = campos[i];
			if (campo.ModoMasivo === 'S') {
				var val = $('#' + campo.Nombre)[0].value;
				if (campo.isDate() && val != null && typeof val === 'string') {
					
					if (campo.isDate() && val == '')
					{
						val = Utils.getNormalDate('0001-01-01');
					}else{
						val = Utils.getNormalDate(val);
					};
					
				} else if (campo.isDecimal() && val != null
						&& typeof val === 'string') {
					val = val.replace(',', '.');
				}

				params['x_' + campo.Nombre] = val;
			}
		}

		params['p_IdDocumento'] = windowParams.idDocumento;

		Model.callService('GrabarMasivo', params, function(reply) {
			windowParams.gridScope.establecerEsquema();
			if(reply.P_OUT_ADVERTENCIASCONTADOR > 0){
				var msj = reply.P_OUT_ADVERTENCIASDETALLE[0].Descripcion;
				bcsComunes.msg('warning','Grabar Detalle',msj);
			}else if(reply.P_OUT_ERRORESCONTADOR > 0){
				var msj = reply.P_OUT_ERRORESDETALLE[0].Descripcion;
				bcsComunes.msg('error','Grabar Detalle',msj);
			}else{
				bcsComunes.msg('info', 'Grabar Masivo', 'OK');
			}
		}, function(error) {
			var msg = "Error";
			if (typeof error.message !== 'undefined') {
				msg = error.message;
			} else if (typeof error.statusText !== 'undefined') {
				msg = error.statusText;
			}
			bcsComunes.msg('error', 'Grabar Masivo', msg);
		});
	}

	$scope.paginaAnterior = function() {
		if ($scope.paginaActual > 1) {
			hidePage($scope.paginaActual);
			$scope.paginaActual--;
			showPage($scope.paginaActual);
			habilitarBotones();
		}
	}

	$scope.paginaSiguiente = function() {
		if ($scope.paginaActual < $scope.totalPages) {
			hidePage($scope.paginaActual);
			$scope.paginaActual++;
			showPage($scope.paginaActual);
			habilitarBotones();
		}
	}

	function habilitarBotones() {
		$scope.botonAnteriorHabilitado = $scope.paginaActual > 1;
		$scope.botonSiguienteHabilitado = $scope.paginaActual < $scope.totalPages;
	}

	function isFormValido() {
		var ret = true;
		for (var i = 0; i < campos.length; i++) {
			var campo = campos[i];
			if ((!windowParams.masivo && !campo.isHiddenInForm() && campo.isEditable()) || (windowParams.masivo && campo.ModoMasivo === 'S')) {
				var val = $('#' + campo.Nombre)[0].value;
				if (campo.isRequired() && Utils.isEmptyValue(val)) {
					$('#' + campo.Nombre).removeClass('valid');
					$('#' + campo.Nombre).addClass('invalid');
					ret = false;
				} else {
					$('#' + campo.Nombre).removeClass('invalid');
					$('#' + campo.Nombre).addClass('valid');
				}
			}
		}

		return ret;
	}

	$('input').blur(function($event) {
		$scope.onChange();
	});
}
