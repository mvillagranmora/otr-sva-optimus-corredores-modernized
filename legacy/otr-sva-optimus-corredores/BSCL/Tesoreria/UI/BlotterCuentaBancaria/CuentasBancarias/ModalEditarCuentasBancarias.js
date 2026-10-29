angular.module('ModuloTesoreria').controller('ModalEditarCuentasBancariasCtrl',function($scope,$translate,$timeout,
	bcsServiciosTesoreria,bcsComunes) {


//Obtiene datos desde la ventana principal
var idVentana = bcsComunes.ventanaWinId($scope);
var parametrosVentana = bcsComunes.ventanaParams(idVentana);
var objCuentaBancaria = $.extend({},parametrosVentana.CuentaBancaria);

bcsComunes.ventanaAutoOverlay(idVentana,false,3000);


var campos = [   
	{ nombre: "IdBanco"},
	{ nombre: "NombreCuentaBancaria"},
	{ nombre: "IdTipoCuentaBancaria"},
	{ nombre: "IdMoneda"},
	{ nombre: "NumeroCuentaBancaria"},
	{ nombre: "Sucursal"},
	{ nombre: "Direccion"},
	{ nombre: "CodigoCuentaInterbancario"},		
	{ nombre: "CodigoAba"},
	{ nombre: "OtroCodigoBancoExtranjero"},
	{ nombre: "CodigoIban"},
	{ nombre: "OtroCodigoCuentaExtranjero"},
	{ nombre: "Contacto"},
	{ nombre: "TelefonoContacto"},
	{ nombre: "EmailContacto"},   
	{ nombre: "SaldoInicial"},
	{ nombre: "AfectaPosicionInicial"},
	{ nombre: "GeneraNomina"},
	{ nombre: "GeneraTransferencia"},
	{ nombre: "IdEstado"},
	{ nombre: "RecaudacionExclusivaPAC"}];


//Asigna valores iniciales
$scope.Empresa           = parametrosVentana.Empresa;
$scope.disabledSaldoInicial = false;


//CONFIGURACION COMBOBOX	
$scope.oConfiguracionTipoBanco={
		tipo          : "combobox-tipo-banco",
		configuracion : {},
		filtros       : {
			IdEstadoTipoBanco    : { "field"   : "IdEstadoTipoBanco","operator": "eq", "value"   : 'ACT' },
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',		
		idPostFiltro  :	false
};


$scope.oConfiguracionBanco={
		tipo          : "combobox-banco",
		configuracion : {},
		filtros       : {
				IdEmpresa   : { "field"   : "IdEmpresa","operator": "eq", "value"   : $scope.SesionIdEmpresa },
				IdEstado    : { "field"   : "IdEstado", "operator": "eq", "value"   : "ACT" },
				IdTipoBanco : { "field"   : "IdTipoBanco","operator": "eq", "value"   : '' },				
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',		
		idPostFiltro  :	false
};


$scope.oConfiguracionPlaza={
		tipo          : "combobox-plaza",
		configuracion : {},
		filtros       : {								
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : 'ACT' },				
			IdPais   : { "field"   : "IdPais",  "operator": "eq", "value"   : 0 },
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',		
		idPostFiltro  :	false
};


$scope.oConfiguracionTipoCuenta={
		tipo          : "combobox-tipo-cuenta-banco",
		configuracion : {},
		filtros       : {},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
};



$scope.oConfiguracionMoneda={
		tipo          : "combobox-moneda",
		configuracion : {},
		filtros       : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
};	


$scope.oConfiguracionEmpleadoFirma1={
		tipo          : "combobox-empleado",
		configuracion : {},
		filtros       : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
};		

$scope.oConfiguracionEmpleadoFirma2={
		tipo          : "combobox-empleado",
		configuracion : {},
		filtros       : {
			IdEstado : { "field"   : "IdEstado","operator": "eq", "value"   : "ACT" },
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
};	


$scope.oConfiguracionEstado={
		tipo          : "combobox-estado-tesoreria",
		configuracion : {},
		filtros       : {
			NombreEntidad : { "field"   : "NombreEntidad","operator": "eq", "value"   : "TES.CuentaBancaria" },
		},
		idActual      : false,
		seleccionado  : false,
		idDefecto     : '',
		idPostFiltro  :	false
};



$scope.cargarDatosEdicion = function()
{	
	//Si es "Modificación": asigna los datos recibidos(desde ventana principal) a los controles del formulario.
	if (parametrosVentana.CuentaBancaria)
	{
		$scope.disabledSaldoInicial                  = true; // no editable(afectó saldo banco cuando se creó cta)
		$scope.NombreCuentaBancaria 				 = objCuentaBancaria.NombreCuentaBancaria;
		$scope.NumeroCuentaBancaria 				 = objCuentaBancaria.NumeroCuentaBancaria;
		$scope.Sucursal 						 	 = objCuentaBancaria.Sucursal;
		$scope.Direccion 							 = objCuentaBancaria.Direccion;
		$scope.CodigoCuentaInterbancario			 = objCuentaBancaria.CodigoCuentaInterbancario;
		$scope.CodigoAba							 = objCuentaBancaria.CodigoAba;
		$scope.OtroCodigoBancoExtranjero			 = objCuentaBancaria.OtroCodigoBancoExtranjero;
		$scope.CodigoIban						 	 = objCuentaBancaria.CodigoIban;
		$scope.OtroCodigoCuentaExtranjero			 = objCuentaBancaria.OtroCodigoCuentaExtranjero;
		$scope.oConfiguracionPlaza.idPostFiltro      = objCuentaBancaria.IdPlaza;			
		$scope.Contacto 							 = objCuentaBancaria.Contacto;
		$scope.TelefonoContacto 					 = objCuentaBancaria.TelefonoContacto;
		$scope.EmailContacto 						 = objCuentaBancaria.EmailContacto;	
		$scope.oConfiguracionTipoCuenta.idPostFiltro = objCuentaBancaria.IdTipoCuentaBancaria;		
		$scope.oConfiguracionMoneda.idPostFiltro     = objCuentaBancaria.IdMoneda;
		$scope.oConfiguracionEstado.idPostFiltro     = objCuentaBancaria.IdEstado;
		$scope.oConfiguracionTipoBanco.idPostFiltro  = objCuentaBancaria.IdTipoBanco;
		$scope.oConfiguracionBanco.idPostFiltro      = objCuentaBancaria.IdBanco;
		
		$scope.SaldoInicial                          = objCuentaBancaria.SaldoInicial;
		$scope.AfectaPosicionInicial                 = objCuentaBancaria.AfectaPosicionInicial == 'S' ? true : false;
		$scope.GeneraNomina                 		 = objCuentaBancaria.GeneraNomina == 'S' ? true : false;		
		$scope.GeneraTransferencia                 	 = objCuentaBancaria.GeneraTransferencia == 'S' ? true : false;
		$scope.RecaudacionExclusivaPAC               = objCuentaBancaria.RecaudacionExclusivaPAC == 'SI' ? true : false;
		
		//Para firma de carta de transferencias bancarias
		$scope.oConfiguracionEmpleadoFirma1.idPostFiltro    = objCuentaBancaria.IdEmpleadoFirma1;
		$scope.MontoSegundaFirma                 	 		= objCuentaBancaria.MontoSegundaFirma;
		$scope.oConfiguracionEmpleadoFirma2.idPostFiltro    = objCuentaBancaria.IdEmpleadoFirma2;			
	}
	
	
};



//FUNCIONES
$scope.cancelar = function(respuesta) {		
	bcsComunes.ventanaData(idVentana,respuesta);  //Asocia data a la ventana, la cual será enviada a la respuesta de la promesa		
	bcsComunes.ventanaCerrar(idVentana);
};





$scope.$watch('NumeroCuentaBancaria',function(newval,oldval){
	if ($scope.NumeroCuentaBancaria !== undefined)
	{	
		//Valida que el número de cuenta banco, sea numérico 
		if (!/^([1-9][0-9]*)$/.test($scope.NumeroCuentaBancaria))
		{   
			bcsComunes.msg("error","Error",$translate.instant('UI_CUENTA_BANCARIA_NO_ES_NUMERICA'));			
			return(false);
		}
	}
});	



$scope.$watch('oConfiguracionBanco.seleccionado',function(newval,oldval){
	if ($scope.oConfiguracionBanco.seleccionado.IdTipoBanco == 'EXT')
	{	
		$scope.CodigoCuentaInterbancario = '';
	}
	
});



//LLAMADA A SERVICIOS
var successGrabarCuentaBancaria = function(respuesta)
{
	//si vuelve ref id, se creó el registro
	if (respuesta.P_OUT_ERRORESCONTADOR !== undefined && respuesta.P_OUT_ERRORESCONTADOR == 0)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		if (respuesta.P_OUT_ADVERTENCIASCONTADOR == "0")
		{
			if (parametrosVentana.CuentaBancaria) // Modificar
			{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("DATOS_MODIFICACION_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);
			}
			else // Crear
			{
				bcsComunes.ventanaMsg({
					mensaje:$translate.instant("DATOS_INGRESO_EXITO"),
					conflictos:util.parseServerResponse(respuesta)
				},idVentana);					
			}
			
			bcsComunes.ventanaOverlay(idVentana, false);
			$scope.cancelar(true);   //Cerrar ventana modal
		}
		else
		{
			bcsComunes.ventanaOverlay(idVentana, false);
			bcsComunes.ventanaMsg({
				mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
				conflictos:util.parseServerResponse(respuesta)
			},idVentana);
		}
		
	}
	else
	{
		console.log(JSON.stringify(respuesta));
		respuesta.P_OUT_ERRORESDETALLE[0].Descripcion = respuesta.P_OUT_ERRORESDETALLE[0].Descripcion
			.replace('::Cuenta::', $scope.NumeroCuentaBancaria)
			.replace('::Banco::' ,$scope.oConfiguracionBanco.seleccionado.NombreBanco);
		//$scope.NumeroCuentaBancaria
		bcsComunes.ventanaOverlay(idVentana, false);
		bcsComunes.ventanaMsg({
			mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
			conflictos:util.parseServerResponse(respuesta)
		},idVentana);

	}
};

var errorGrabarCuentaBancaria = function(respuesta)
{	
	bcsComunes.ventanaOverlay(idVentana, false);
	bcsComunes.ventanaMsg({
		mensaje:$translate.instant("UI_PROCESO_REALIZADO_CON_ERROR"),
		conflictos:util.parseServerResponse(respuesta)
	},idVentana);
	
};


var validarCuentaBancaria = function()
{	
	//Valida que el número de cuenta bancaria sea numérico
	if (!/^([1-9][0-9]*)$/.test($scope.NumeroCuentaBancaria))
	{   
		bcsComunes.msg("error","Error",$translate.instant('UI_CUENTA_BANCARIA_NO_ES_NUMERICA'));			
		return(false);
	}	
	
	return(true);		
};


$scope.grabarCuentaBancaria = function()
{
	
	if (validarCuentaBancaria())
	{
		var parametros = {};
		var TablaCuentasBancarias = new Array();		
		bcsComunes.ventanaOverlay(idVentana, true);
		
		TablaCuentasBancarias.push
		({
			IdCuentaBancaria        	: parametrosVentana.CuentaBancaria ? parametrosVentana.CuentaBancaria.IdCuentaBanco : 1,
			IdBanco  				  	: Number($scope.oConfiguracionBanco.seleccionado.IdBanco || 0),
			NombreCuentaBancaria    	: $scope.NombreCuentaBancaria || '',
			IdTipoCuentaBancaria    	: $scope.oConfiguracionTipoCuenta.seleccionado.IdTipoCuentaBancaria || '',
			IdMoneda  			  		: $scope.oConfiguracionMoneda.seleccionado.IdMoneda || '',
			NumeroCuentaBancaria    	: $scope.NumeroCuentaBancaria || '',
			Sucursal				  	: $scope.Sucursal || '',
			Direccion				  	: $scope.Direccion || '',
			CodigoCuentaInterbancario	: $scope.CodigoCuentaInterbancario || '',
			CodigoAba					: $scope.CodigoAba || '',
			OtroCodigoBancoExtranjero	: $scope.OtroCodigoBancoExtranjero || '',
			CodigoIban					: $scope.CodigoIban || '',
			OtroCodigoCuentaExtranjero	: $scope.OtroCodigoCuentaExtranjero || '',
			IdPlaza                     : $scope.oConfiguracionPlaza.seleccionado.IdPlaza || 0,				
			Contacto				  	: $scope.Contacto || '',	
			TelefonoContacto		  	: $scope.TelefonoContacto || '',
			EmailContacto			  	: $scope.EmailContacto || '',		
			SaldoInicial            	: Number($scope.SaldoInicial || 0),   
			AfectaPosicionInicial   	: $scope.AfectaPosicionInicial ? 'S' : 'N',		
			GeneraNomina           	    : $scope.GeneraNomina ? 'S' : 'N',
			GeneraTransferencia     	: $scope.GeneraTransferencia ? 'S' : 'N',		
			IdEstado				  	: $scope.oConfiguracionEstado.seleccionado.IdEstado || '',
			IdEmpleadoFirma1    	    : $scope.oConfiguracionEmpleadoFirma1.seleccionado.IdEmpleado || 0,
			MontoSegundaFirma    		: Number($scope.MontoSegundaFirma || 0),				
			IdEmpleadoFirma2    	    : $scope.oConfiguracionEmpleadoFirma2.seleccionado.IdEmpleado || 0,
			RecaudacionExclusivaPAC		: $scope.RecaudacionExclusivaPAC ? 'S' : 'N',
		});
		
		parametros.p_IdEmpresa               = $scope.SesionIdEmpresa;
		parametros.p_IdUsuario               = $scope.SesionIdUsuario;
		parametros.p_IdModulo                = 'TES';                                                                     
		parametros.p_simularOperacion        = 'N';                  
		parametros.p_UsaReglasNegocio        = 'S';			
		parametros.p_TablaCuentasBancarias   = TablaCuentasBancarias;
			 
		console.log("PARAMETROS:" + JSON.stringify(parametros));				 
		
		if (parametrosVentana.CuentaBancaria)
		{ // Accion: Modificar			
			var camposModificados = [];
			//Genera arreglo con campos modificados
			$.each(campos,function(key,value) {	
				 if (objCuentaBancaria[value.nombre] !== parametros['p_'+value.nombre])
				 {   //Si hay cambio lo registra
					 camposModificados.push({Campo: value.nombre, ValorNuevo: parametros['p_'+value.nombre], ValorAntiguo: objCuentaBancaria[value.nombre]});
				 }
			});
			   
			bcsServiciosTesoreria.ModificarCuentaBancaria(parametros, successGrabarCuentaBancaria, errorGrabarCuentaBancaria);	
		}
		else
		{ // Accion: Crear				
			parametros.p_FechaSistema            = $scope.SesionFechaSistema;
			bcsServiciosTesoreria.CrearCuentaBancaria(parametros, successGrabarCuentaBancaria, errorGrabarCuentaBancaria);	
		}
	}		
};
// Fin servicio grabar cuenta bancaria



$scope.$watch('GeneraNomina',function(newval,oldval){
	if ($scope.SesionLocale === "PE")
	{	//Para PERU se puede escoger "Nomina"(Carta) o "Transferencia"(Archivo), pero no ambas.
		if (newval === true)
			$scope.GeneraTransferencia = false;
	}
});	

$scope.$watch('GeneraTransferencia',function(newval,oldval){
	if ($scope.SesionLocale === "PE")
	{	//Para PERU se puede escoger "Nomina"(Carta) o "Transferencia"(Archivo), pero no ambas.
		if (newval === true)
			$scope.GeneraNomina = false;
	}
});

var firstHeight = $('#Win_editarCuentasBancarias');
    firstHeight.css('height', '600px');

$timeout(function(){ 
	$scope.cargarDatosEdicion();
},1000);

});
