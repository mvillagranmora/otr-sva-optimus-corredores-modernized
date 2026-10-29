angular.module('ModuloNormativa').controller('DetailControllerRMO',function ($scope, $translate, bcsComunes, Model, ModelService, Utils,bcsServiciosNormativa) {
	const idVentana = bcsComunes.ventanaWinId($scope);
	const windowParams = Utils.getWindowParams($scope);
	if(windowParams.Model){
		Model = windowParams.Model
	}
	const IDP = 'RMO_';
	function $F(nombre) { return $('#' + IDP + nombre); }
	const accion = windowParams.item == null ? $translate.instant('UI_CREAR_OPERACION')
			: $translate.instant('UI_MODIFICAR_OPERACION');

	$scope.hideRMO1 = false;
	$scope.hideRMO2 = true;
	$scope.hideRMO3 = true;
	$scope.hideRMO4 = true;
	$scope.hideRMO5 = true;
	$scope.hideRMO6 = true;
	$scope.hideRMO7 = true;
	$scope.hideRMO8 = true;
	$scope.hideRMO9 = true;

	function hidePage(n) {
		$scope['hideRMO' + n] = true;
	}

	function showPage(n) {
		$scope['hideRMO' + n] = false;
	}

	$scope.botonAnteriorHabilitado = false;
	$scope.botonSiguienteHabilitado = false;

	let camposRMO = Model.getCampos();

	let template = kendo.template($('#fieldsTemplateRMO').html(), {
		useWithBlock : true
	});

	$scope.titulosRMO = [];
	$scope.paginaActual = 1;
	$scope.totalPages = 0;

	let currentGroup = '';
	let pageFields = [];
	function renderPage() {
		$scope.templateParams = {
			fields : pageFields,
			masivo : windowParams.masivo
		}
		$scope.totalPages++;
		$scope.titulosRMO[$scope.totalPages] = currentGroup;
		$('#formContainerRMO' + $scope.totalPages).html(
				template($scope.templateParams));
		pageFields = [];
	}

	for (let i = 0; i < camposRMO.length; i++) {
		let campo = camposRMO[i];
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

	for (let i = 0; i < camposRMO.length; i++) {
		let campo = camposRMO[i];
		if (!campo.isHiddenInForm()) {

			bindControlRMO(campo);

			if (windowParams.item != null) {
				let value = windowParams.item[campo.Nombre];
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
			$F(campo.Nombre).data('kendoDropDownList').value(value);
		} else if (campo.isNumber()) {
			$F(campo.Nombre).data('kendoNumericTextBox').value(value);
		} else {
			$F(campo.Nombre)[0].value = value;
		}
	}

	function bindControlRMO(campo) {
		if (campo.isDate()) {
			bindDateControlRMO(campo);
		} else if (campo.isList()) {
			bindListControlRMO(campo);
		} else if (campo.isDecimal()) {
			bindDecimalControlRMO(campo);
		} else if (campo.isNumber()) {
			bindNumberControlRMO(campo);
		} else {
			bindTextControlRMO(campo);
		}
	}

	function bindDateControlRMO(campo) {
		$F(campo.Nombre).kendoDatePicker({
			change : $scope.onChange
		});
	}

	function bindListControlRMO(campo) {
		$F(campo.Nombre).kendoDropDownList({
			dataTextField : 'Descripcion',
			dataValueField : 'Codigo',
			dataSource : Model.getDiccionario(campo.TipoDiccionario, false),
			autobind : true,
			index : 0,
			change : $scope.onChange
		});
	}

	function bindDecimalControlRMO(campo) {
		let dec = campo.CantidadDecimales;
		let fmt = 'n' + dec;
		$F(campo.Nombre).kendoNumericTextBox({
			decimals : dec,
			format : fmt,
			change : $scope.onChange,
			spin : $scope.onChange
		});
	}

	function bindNumberControlRMO(campo) {
		$F(campo.Nombre).kendoNumericTextBox({
			decimals : 0,
			format : 'n0',
			change : $scope.onChange,
			spin : $scope.onChange
		});
	}

	function bindTextControlRMO(campo) {
		$F(campo.Nombre).kendoAutoComplete({
			change : $scope.onChange
		});
	}

	habilitarBotones();
	let campoIdDocumento = Model.getCampo('IdDocumento');
	if (!campoIdDocumento.isHiddenInForm()) {
		$('#IdDocumento')[0].value = windowParams.idDocumento;
	}
	
	$scope.grabarDetalleRMO = function() {
		if (windowParams.masivo) {
			$scope.grabarDetalleMasivo();
		} else {
			$scope.grabarDetalleSimple();
		}
	}

	$scope.grabarDetalleSimple = function() {
		let params = {
			p_IdEmpresa : $scope.SesionIdEmpresa,
			p_IdUsuario : $scope.SesionIdUsuario,
			p_UsrUltMod : $scope.SesionUsuario,
			p_IdModulo : Model.getModulo()
		};

		for (let i = 0; i < camposRMO.length; i++) {
			let campo = camposRMO[i];
			if (campo.isHiddenInForm()) {
				if (windowParams.item != null) {
					params['p_' + campo.Nombre] = windowParams.item[campo.Nombre];
				}
			} else {
				let val = $F(campo.Nombre)[0].value;
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

		bcsServiciosNormativa.GrabarDetalleRMO(params).$promise.then(function(respuesta) {
			if (respuesta.P_OUT_ERRORESCONTADOR === 0) {
				windowParams.gridScope.buscarDatos();
				bcsComunes.msg('info', 'Grabar Detalle', 'OK');
				bcsComunes.ventanaCerrar(idVentana);
			} else {
				bcsComunes.msg('error', 'Grabar Detalle', respuesta.P_OUT_ERRORESDETALLE[0].Codigo);
			}

		}, function(error) {
			let msg = "Error";
			if (typeof error.message !== 'undefined') {
				msg = error.message;
			} else if (typeof error.statusText !== 'undefined') {
				msg = error.statusText;
			}
			bcsComunes.msg('error', 'Grabar Detalle', msg);
		});
	}

	$scope.paginaAnteriorRMO = function() {
		if ($scope.paginaActual > 1) {
			hidePage($scope.paginaActual);
			$scope.paginaActual--;
			showPage($scope.paginaActual);
			habilitarBotones();
		}
	}

	$scope.paginaSiguienteRMO = function() {
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
		let ret = true;
		for (let i = 0; i < camposRMO.length; i++) {
			let campo = camposRMO[i];
			if ((!windowParams.masivo && !campo.isHiddenInForm() && campo.isEditable()) || (windowParams.masivo && campo.ModoMasivo === 'S')) {
				let val = $F(campo.Nombre)[0].value;
				if (campo.isRequired() && Utils.isEmptyValue(val)) {
					$F(campo.Nombre).removeClass('valid');
					$F(campo.Nombre).addClass('invalid');
					ret = false;
				} else {
					$F(campo.Nombre).removeClass('invalid');
					$F(campo.Nombre).addClass('valid');
				}
			}
		}

		return ret;
	}

	$('input').blur(function($event) {
		$scope.onChange();
	});
});
