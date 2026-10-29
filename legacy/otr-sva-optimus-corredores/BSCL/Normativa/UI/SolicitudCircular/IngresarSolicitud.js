angular.module('ModuloNormativa').directive('fileReader', function() {
  return {
    scope: {
      fileReader:"="
    },
    link: function(scope, element) {
      $(element).on('change', function(changeEvent) {
        var files = changeEvent.target.files;
        if (files.length) {
          var r = new FileReader();
          r.onload = function(e) {
              var contents = e.target.result;
              scope.$apply(function () {
                scope.fileReader = contents;
              });
          };
          
          r.readAsText(files[0],'iso-8859-1');
        }
      });
    }
  };
});



angular.module('ModuloNormativa').controller('IngresarSolicitudCircularCtrl', 
function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa, bcsBlotterEmisionCircular)
{	

//*** configuracion de objetos ***//
	
	
var idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
var parametros = bcsComunes.ventanaParams(idVentana);
bcsComunes.ventanaAutoOverlay(idVentana,false,3000);
$scope.SolicitudCircular = $.extend({}, parametros.SolicitudCircular);
$scope.ParametrosSolicitud = $.extend({}, parametros.ParametrosSolicitud);


$scope.data ={
		oCircularConfiguracion : {},
		Filtros: {}
};

$scope.ingSol ={
		oSeleccionRegistros : {}
};

//INICIO seteo incial de filtros habilitados
//$scope.seleccionCli=false;
$scope.activadoEmpl =false;
$scope.activadoCli =true;
$scope.activadoAut =false;
$scope.seleccionFecInicio=false;
$scope.seleccionFecFin=false;
$scope.seleccionNemo=true;
$scope.seleccionMcdo=false;
$scope.seleccionOficio=false;
$scope.seleccionGrabar = false;
$scope.data.separadorListas = ';';
$scope.data.simboloDecimal = ',';
$scope.data.simboloFecha = 'dd/MM/yyyy';

//$scope.seleccionRueda		= true;
//$scope.seleccionNoRueda		= true;


$scope.listRut = false;
$scope.listRutAut = true;
$scope.listNemo = true;
$scope.listRutEmpl = true;

$scope.seleccionTodosRut = false;
$scope.seleccionTodosNemo = true;
$scope.seleccionTodosEmpl = true;
$scope.seleccionTodosAut = true;


$scope.ingSol.oSeleccionRegistros.registroCliente = false; 
$scope.ingSol.oSeleccionRegistros.registroAutorizados = false; 
$scope.ingSol.oSeleccionRegistros.registroOrdenes = false; 
$scope.ingSol.oSeleccionRegistros.registroOperaciones = false; 
$scope.ingSol.oSeleccionRegistros.registroAsignacion = false; 
$scope.ingSol.oSeleccionRegistros.registroFacturacion = false; 
$scope.ingSol.oSeleccionRegistros.registroCustodia = false; 
$scope.ingSol.oSeleccionRegistros.registroTrabajador = false; 

//FIN seteo incial de filtros habilitados 



var Accion = '';
var obj = {};
var ingSol = this;
var RutaArchivo = '';
ingSol.idVentana = bcsComunes.ventanaWinId($scope); // ID del Formulario.
ingSol.domVentana = $("[ventanaid='"+ingSol.idVentana+"']");

$scope.modoVisualizacion = false;

$scope.data.oConfiguracionSocioNegocio={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
		EsCliente             : {"field"    : "EsCliente",         "operator": "eq", "value"   :  'UI_SI'}				
		},			 
		idActual : false,
		seleccionado : false,
		idDefecto : ''
};

$scope.data.oConfiguracionEmpleado={
		tipo : "combobox-socio-negocio",
		configuracion :{},
		filtros: {
		EsEmpleado             : {"field"    : "EsEmpleado",         "operator": "eq", "value"   :  'UI_SI'}				
		},			 
		idActual : false,
		seleccionado : false,
		idDefecto : ''
};

/**
 * GRILLAS PARA MODO VISUALIZACION.
 */

$scope.confBasicaListadoRut= {
		dataSource			: bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular(),
		columns				: bcsBlotterEmisionCircular.columnasParametrosRut(),		
		sortable			: true,
		pageable			: true,
		height				: 200, 
		selectable 			: "row",
		filterable			: true,
		autoBind			: false,
		gridCreated			: function(grid) {
									if (parametros.SolicitudCircular) 
										filtrosRut();
								}
	};

$scope.confBasicaListadoNemo= {
		dataSource			: bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular(),
		columns				: bcsBlotterEmisionCircular.columnasParametrosNemo(),		
		sortable			: true,
		pageable			: true,
		height				: 200, 
		selectable 			: "row",
		filterable			: true,
		autoBind			: false,
		gridCreated			: function(grid) {
									if (parametros.SolicitudCircular) 
											filtrosNemo();
								}
	};

//$scope.ajustaVentana = function()
//{
//	 bcsComunes.ventanaTamanoAutomatico($scope.winID,7);
//};

if (parametros.SolicitudCircular){
	$scope.modoVisualizacion =  true;
	var filterDatos = [];
	filterDatos.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
	filterDatos.push({field: "TipoParametro", operator: "neq", value: "RUT"});
	filterDatos.push({field: "TipoParametro", operator: "neq", value: "NEMO"});

	var DatosDataSource = bcsBlotterEmisionCircular.dataSourceParametrosSolicitudCircular();
	
	DatosDataSource.query({filter: filterDatos}).then(function(e){
		console.log(DatosDataSource.data());
		cargarDatosParametros(DatosDataSource.data(), parametros.SolicitudCircular);
		$scope.data.modoLectura = true;
	});
}

function filtrosRut() {
	var filters = [];
	filters.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
	filters.push({field: "TipoParametro", operator: "eq", value: "RUT"});
	$scope.confBasicaListadoRut.dataSource.filters(filters);
};

function filtrosNemo() {
	var filters = [];
	filters.push({field: "IdSolicitudCircular", operator: "eq", value: $scope.SolicitudCircular.IdSolicitudCircular});
	filters.push({field: "TipoParametro", operator: "eq", value: "NEMO"});
	$scope.confBasicaListadoNemo.dataSource.filters(filters);
};

function cargarDatosParametros(arrayDatosParametros, objSoliciturCircular) {
	
	$scope.data.lblUsuarioCreador = objSoliciturCircular.CodigoUsuario;
	$scope.data.lblIdSolicitud = objSoliciturCircular.IdSolicitudCircular;
	
	switch (objSoliciturCircular.IdTipoDocumento){
		case "NCG380CLI":
			$scope.ingSol.oSeleccionRegistros.registroCliente = true;
			break;
		case "NCG380AUT":
			$scope.ingSol.oSeleccionRegistros.registroAutorizados = true;
			break;
		case "NCG380ORD":
			$scope.ingSol.oSeleccionRegistros.registroOrdenes = true;
			break;
		case "NCG380OPE":
			$scope.ingSol.oSeleccionRegistros.registroOperaciones = true;
			break;
		case "NCG380ASIG":
			$scope.ingSol.oSeleccionRegistros.registroAsignacion = true;
			break;
		case "NCG380FAC":
			$scope.ingSol.oSeleccionRegistros.registroFacturacion = true;
			break;
		case "NCG380CUS":
			$scope.ingSol.oSeleccionRegistros.registroCustodia = true;
			break;
		case "NCG380TRA":
			$scope.ingSol.oSeleccionRegistros.registroTrabajador = true;
			break;
	}
	
	$scope.data.Observaciones = objSoliciturCircular.Observaciones;
	
	for (i = 0; i<arrayDatosParametros.length; i++) {
		switch (arrayDatosParametros[i].TipoParametro){
			case "FINI":
				$scope.data.Filtros.FechaInicio = arrayDatosParametros[i].Valor;
				break;
			case "FTER":
				$scope.data.Filtros.FechaFin = arrayDatosParametros[i].Valor;
				break;
			case "MCDO":
				break;
			case "TAUT":
				$scope.data.Filtros.TodosAut = (arrayDatosParametros[i].Valor == 1) ? true: false;
				break;
			case "TRUT":
				$scope.data.Filtros.TodosCli = (arrayDatosParametros[i].Valor == 1) ? true: false;
				break;
			case "TNEMO":
				$scope.data.Filtros.TodosNemo = (arrayDatosParametros[i].Valor == 1) ? true: false;
				break;
			case "TEMPL":
				$scope.data.Filtros.TodosEmpl = (arrayDatosParametros[i].Valor == 1) ? true: false;
				break;
		}
	}
};





//Limpia los campos del formulario completo
$scope.data.limpiarFormulario = function()
{
	//Limpia datos formulario completo
	$scope.ingSol.oSeleccionRegistros.registroCliente = false;
	$scope.ingSol.oSeleccionRegistros.registroAutorizados= false;
	$scope.ingSol.oSeleccionRegistros.registroOrdenes= false;
	$scope.ingSol.oSeleccionRegistros.registroAsignacion= false;
	$scope.ingSol.oSeleccionRegistros.registroOperaciones= false;
	$scope.ingSol.oSeleccionRegistros.registroFacturacion= false;
	$scope.ingSol.oSeleccionRegistros.registroCustodia= false;
	$scope.ingSol.oSeleccionRegistros.registroTrabajador= false;
	
	$scope.data.Filtros.Nemo = '';
	$scope.data.Filtros.Mercados = '';
	$scope.data.Filtros.RutTrabajador = '';
	$scope.data.Filtros.Oficio = '';
	$scope.data.oConfiguracionSocioNegocio.seleccionado =0;
	
};


//Valida el ingreso de los campos minimos necesarios para crear la solicitud
$scope.data.validaFormulario = function()
{
	//Limpia datos formulario completo
	$scope.ingSol.oSeleccionRegistros.registroCliente = false;
	$scope.ingSol.oSeleccionRegistros.registroAutorizados= false;
	$scope.ingSol.oSeleccionRegistros.registroOrdenes= false;
	$scope.ingSol.oSeleccionRegistros.registroAsignacion= false;
	$scope.ingSol.oSeleccionRegistros.registroOperaciones= false;
	$scope.ingSol.oSeleccionRegistros.registroFacturacion= false;
	$scope.ingSol.oSeleccionRegistros.registroCustodia= false;
	$scope.ingSol.oSeleccionRegistros.registroTrabajador= false;
	
	$scope.data.Filtros.Nemo = '';
	$scope.data.Filtros.Mercados = '';
	$scope.data.Filtros.RutTrabajador = '';
	$scope.data.Filtros.Oficio = '';
	$scope.data.oConfiguracionSocioNegocio.seleccionado =0;
	
};
	



	


var IdSolicitudCircular =0;

var successCrearSolicitud = function(respuesta)
{	
	if (respuesta.P_OUT_ERRORESCONTADOR == 0)
	{
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));
		bcsComunes.msg("info","info",'Solicitud creada con éxito');
		IdSolicitudCircular =respuesta.P_OUT_RESULTADO;
		//$scope.data.limpiarFormulario();
	}
	else
	{	
		bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
		bcsComunes.msg("error","error",$translate.instant('Solicitud no fue creada'));
					
		var i = 0;
		for(i; i<respuesta.P_OUT_ERRORESCONTADOR; i++)
		{
			bcsComunes.log().info("Error: " + respuesta.P_OUT_ERRORESDETALLE[i].Descripcion);       
		}            
	}		
	bcsComunes.ventanaOverlay($scope.winID, false);
};

var errorCrearSolicitud = function(respuesta)
{	
	bcsComunes.setConflictos(util.parseServerResponse(respuesta));			
	bcsComunes.msg("error","error",$translate.instant('INGRESO ERROR'));
	bcsComunes.ventanaOverlay($scope.winID, false);
};


//					//	
//LECTURA DE ARCHIVO//
//					//
	
var csvToArray = function (csvString,skipFirstLine){

	var csvArray   = [];

	var csvRows    = csvString.split(/\n/);

	for(var rowIndex = 0; rowIndex < csvRows.length; ++rowIndex){
		
		var rowArray  = csvRows[rowIndex].split(';');		

		for(var propIndex = 0; propIndex < rowArray.length; ++propIndex){
		
			var propValue =   rowArray[propIndex].replace(/^"|"$/g,'') || '';

			csvArray.push(propValue);
		}
		
	  }
	return csvArray;
};

 $scope.data.oCircularConfiguracion.fechaInicioSol = $scope.SesionFechaSistema;



$scope.fuenteDatos = 'INICIAL';
$scope.reemplazar = 'false';
$scope.grabar = 'false';
$scope.aceptarFechasNull = 'true';
$scope.separadorListas = ';';
$scope.simboloDecimal = ',';
$scope.fechaDesde = null;
$scope.fechaHasta = null;
	
 
	
$scope.data.cargar = function() {


try {
var condicion = 0;

	
	if($scope.ingSol.oSeleccionRegistros.registroCliente === true)
	{
		if(($scope.data.oConfiguracionSocioNegocio.seleccionado === false) && ($scope.data.Filtros.TodosCli === false || $scope.data.Filtros.TodosCli === undefined ) && ($scope.data.FiltrosRut=== false || $scope.data.FiltrosRut=== undefined ))
		{
		 condicion = 1;
		}
	}
	if($scope.ingSol.oSeleccionRegistros.registroAutorizados === true)
	{
		//validacion clientes
		if(($scope.data.oConfiguracionSocioNegocio.seleccionado === false) && ($scope.data.Filtros.TodosCli === false || $scope.data.Filtros.TodosCli === undefined ) && ($scope.data.FiltrosRut=== false || $scope.data.FiltrosRut=== undefined ))
		{	//validacion autorizados
			if(($scope.data.oConfiguracionAutorizado.seleccionado === false) && ($scope.data.Filtros.TodosAut === false || $scope.data.Filtros.TodosAut === undefined ) && ($scope.data.Filtros.ListRutAut=== false || $scope.data.FiltrosRutAut === undefined ))
			{
				condicion = 1;
			}
		}
	}
	
	
	if($scope.ingSol.oSeleccionRegistros.registroOrdenes === true || $scope.ingSol.oSeleccionRegistros.registroCustodia === true || $scope.ingSol.oSeleccionRegistros.registroOperaciones === true || $scope.ingSol.oSeleccionRegistros.registroAsignacion === true || $scope.ingSol.oSeleccionRegistros.registroFacturacion === true)
	{
		if(($scope.data.oConfiguracionSocioNegocio.seleccionado === false) && ($scope.data.Filtros.TodosCli === false || $scope.data.Filtros.TodosCli === undefined ) && ($scope.data.FiltrosRut=== false || $scope.data.FiltrosRut=== undefined ))
		{	//validacion nemos y oficio
			if( ($scope.data.Filtros.Nemo=== false || $scope.data.Filtros.Nemo=== undefined) && ($scope.data.Filtros.Oficio=== false || $scope.data.Filtros.Oficio=== undefined ) && ($scope.data.oConfiguracionAutorizado.seleccionado === false) && ($scope.data.Filtros.TodosNemo === false || $scope.data.Filtros.TodosNemo === undefined ) && ($scope.data.FiltrosNemo=== false || $scope.data.FiltrosNemo=== undefined ))
			{
				condicion = 1;
			}
		}
		
	}

	if($scope.ingSol.oSeleccionRegistros.registroTrabajador === true)
	{
		if(($scope.data.oConfiguracionSocioNegocio.seleccionado === false) && ($scope.data.Filtros.TodosCli === false || $scope.data.Filtros.TodosCli === undefined ) && ($scope.data.FiltrosRut=== false || $scope.data.FiltrosRut=== undefined ))
		{	//validacion nemos y oficio
			if( ($scope.data.oConfiguracionEmpleado.seleccionado === false) && ($scope.data.Filtros.TodosEmpl === false || $scope.data.Filtros.TodosEmpl === undefined ) && ($scope.data.FiltrosRutEmpl=== false || $scope.data.FiltrosRutEmpl=== undefined ))
			{
				condicion = 1;
			}
		}
	}

	if($scope.data.separadorListas === ',' && $scope.data.simboloDecimal === ',' )
	{
		 
				condicion = 2;
		
	}

	
	if(condicion == 0)
	{
		submitirCarga();
		bcsComunes.ventanaCerrar(idVentana);
	}else{
		
		
		if(condicion==1){
			//se declara como tipo ERROR ya que este contiene la imagen "!"
			var t = bcsAlerta.crear("ERROR",'Debe ingresar los datos necesarios para crear solicitud ');
			
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){

					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;
							
					var datos = {};
					
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			});
		}
		
		if(condicion==2){
			//se declara como tipo ERROR ya que este contiene la imagen "!"
			var t = bcsAlerta.crear("ERROR",'Por seguridad en la exportación de datos, no se permite combinacion separador:coma y decimales:coma ');
			
			t.result.then(function(result){
				if (result===OPCIONES_BCSALERTA.ACEPTAR){

					bcsComunes.ventanaOverlay($scope.winID, true);
					$scope.btnActivo = false;
							
					var datos = {};
					
					bcsComunes.ventanaOverlay($scope.winID, false);
				}
			});
		}
		
		
		
	}
	
		

		
	}catch (err) {
		bcsComunes.msg('error', 'Cargar Archivo', err);
	}

}

$scope.loadFile = null;


function submitirCarga() {
//parametros comunes entre los tipos de registro

//validacion de seleccion de registro

if(($scope.ingSol.oSeleccionRegistros.registroCliente === true 
		|| $scope.ingSol.oSeleccionRegistros.registroAutorizados === true 
		|| $scope.ingSol.oSeleccionRegistros.registroOrdenes === true 
		|| $scope.ingSol.oSeleccionRegistros.registroOperaciones === true 
		|| $scope.ingSol.oSeleccionRegistros.registroAsignacion === true 
		|| $scope.ingSol.oSeleccionRegistros.registroFacturacion === true
		|| $scope.ingSol.oSeleccionRegistros.registroCustodia === true
		|| $scope.ingSol.oSeleccionRegistros.registroTrabajador === true) &&
		 ($scope.data.Filtros.FechaInicio != undefined))


{
		
	

if($scope.data.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto!=undefined)
{

var posicion = $scope.data.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto.lastIndexOf('-');
var Dig = $scope.data.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto.substring(posicion+1);
var Rut = $scope.data.oConfiguracionSocioNegocio.seleccionado.DocumentoIdentidadDefecto.substring(0,posicion);

}

var parametrosCliente = {};
var parametrosOrdenes = {};
var parametrosAsignaciones = {};
var parametrosFacturacion = {};
var parametrosOperaciones = {};
var parametrosCustodia = {};
var parametrosTrabajador = {};
var parametrosAutorizados = {};

//OBTENCION DE LISTADO DE RUT, RUTAUT,NEMO
var ListadoRuts = [];
if($scope.data.FiltrosRut!=undefined)
{
	var RutsCli = csvToArray($scope.data.FiltrosRut.fileContent,true);
	     
	    //Recorremos las filas seleccionadas
 	var v_iter ;
 	 v_iter=1;
 	 
 	 if(RutsCli.length>0){
 		 $.each(RutsCli,function(key,value){

   	    	ListadoRuts.push(
 	  		 {
 	  			  Rut: RutsCli[key]
 	  				 		      				 
 	  		 });
   	    	 v_iter=v_iter+1;
   	   });
 	 }

}else{
	
	var RutsCli = [];
}

//Obtiene datos de archivo con listado de rut de personas autorizadas

var ListadoRutAuts = [];
if($scope.data.FiltrosRutAut!=undefined)
{
	var RutsAut = csvToArray($scope.data.FiltrosRutAut.fileContent,true);
	     
	    //Recorremos las filas seleccionadas
 	var v_iter ;
 	 v_iter=1;
 	 
 	 if(RutsAut.length>0){
 		 $.each(RutsAut,function(key,value){

 			ListadoRutAuts.push(
 	  		 {
 	  			  RutAut: RutsAut[key]
 	  				 		      				 
 	  		 });
   	    	 v_iter=v_iter+1;
   	   });
 	 }

}else{
	
	var RutsAut = [];
}

var ListadoRutEmpl = [];
if($scope.data.FiltrosRutEmpl!=undefined)
{
	var RutsEmpl = csvToArray($scope.data.FiltrosRutEmpl.fileContent,true);
	     
	    //Recorremos las filas seleccionadas
 	var v_iter ;
 	 v_iter=1;
 	 
 	 if(RutsEmpl.length>0){
 		 $.each(RutsEmpl,function(key,value){

 			ListadoRutEmpl.push(
 	  		 {
 	  			Rut: RutsEmpl[key]
 	  				 		      				 
 	  		 });
   	    	 v_iter=v_iter+1;
   	   });
 	 }

}else{
	
	var RutsEmpl = [];
}





//Obtiene datos de archivo con listado de Nemos

var ListadoNemos = [];
if($scope.data.FiltrosNemo!=undefined)
{
	var Nemos = csvToArray($scope.data.FiltrosNemo.fileContent,true);
	     
	    //Recorremos las filas seleccionadas
 	var v_iter ;
 	 v_iter=1;
 	 
 	 if(Nemos.length>0){
 		 $.each(Nemos,function(key,value){

 			ListadoNemos.push(
 	  		 {
 	  			  Nemo: Nemos[key]
 	  				 		      				 
 	  		 });
   	    	 v_iter=v_iter+1;
   	   }); 
 	 }

}else{
	
	var Nemos = [];
}



//ingresar aqui la creación del documento, y agregar el valor del doc a los parametros

if($scope.ingSol.oSeleccionRegistros.registroCliente === true)
{
//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosCliente.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosCliente.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosCliente.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosCliente.p_ListaRut 			    = ListadoRuts || [];
parametrosCliente.p_FechaIngresoSolicitud 	= util.dateInicioDia($scope.SesionFechaSistema);//$scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosCliente.p_IdTipoCircular         = 'NCG380';
parametrosCliente.p_Observaciones           = $scope.data.Observaciones;
parametrosCliente.p_IdUsuario               = $scope.SesionIdUsuario;
parametrosCliente.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosCliente.p_Mercado					= 'mcdo';//$scope.filtros.IdClasificacionTipoInstrumento;
parametrosCliente.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosCliente.p_ListaRut = ListadoRuts || []; 
parametrosCliente.p_ListaRutAut = ListadoRutAuts || [];
parametrosCliente.p_ListaNemo = ListadoNemos || [];
parametrosCliente.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosCliente.p_IdTipoDocumento = 'NCG380CLI';
parametrosCliente.p_Esquema = 'NCG380Cli';
parametrosCliente.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosCliente.p_Version = parametrosCliente.p_Version.getFullYear();
parametrosCliente.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosCliente.p_FechaLimite ='2016-12-31';
parametrosCliente.p_IdEstadoDocumento = 'PEN';
parametrosCliente.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosCliente.p_FechaPresentacion = $scope.SesionFechaSistema;


 

parametrosCliente.p_IdModulo = 'NOR';
parametrosCliente.p_IdAgenda = 0;
parametrosCliente.p_IdTipoTarea = 'ExportaNCG380';
parametrosCliente.p_Segundos = 0;
parametrosCliente.p_CreaParam = 'S';


if($scope.data.Filtros.TodosCli===true)
{
	parametrosCliente.p_TodosRut = 1;	
}else{
	parametrosCliente.p_TodosRut = 0;	
}
if($scope.data.Filtros.TodosNemo===true)
{
	parametrosCliente.p_TodosNemo = 1;
}else{
	parametrosCliente.p_TodosNemo = 0;
}
if($scope.data.Filtros.TodosAut===true)
{
	parametrosCliente.p_TodosAut = 1;
}else{
	parametrosCliente.p_TodosAut = 0;
}
if($scope.data.Filtros.TodosEmpl===true)
{
	parametrosCliente.p_TodosEmpl = 1;
}else{
	parametrosCliente.p_TodosEmpl = 0;
}

var p_TablaParametrosaux ; //parametros generales que puede ver el jobmanager
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
{
	NombreParametro : 'idUsuario',
	Valor : $scope.SesionIdUsuario
},{
	NombreParametro : 'schema',
	Valor :  Model.getSchema()
}, {
	NombreParametro : 'grabar',
	Valor : $scope.grabar
}, {
	NombreParametro : 'reemplazar',
	Valor : $scope.reemplazar
},{
	NombreParametro : 'separadorListas',
	Valor : $scope.data.separadorListas
},{
	NombreParametro : 'simboloDecimal',
	Valor : $scope.data.simboloDecimal
},{
	NombreParametro : 'simboloFecha',
	Valor : $scope.data.simboloFecha
},{
	NombreParametro : 'aceptarFechasNull',
	Valor : 'true'
},]};
var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
if (fd != null) {
		params.p_TablaParametrosaux.push({
		NombreParametro : 'fechaDesde',
		Valor : fd
	});
}

var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
if (fh != null) {
		params.p_TablaParametrosaux.push({
		NombreParametro : 'fechaHasta',
		Valor : fh
	});
}

parametrosCliente.p_TablaParametros = params.p_TablaParametrosaux;

bcsComunes.msg('info', 'Carga', 'En proceso...');
bcsServiciosNormativa.CrearSolicitudCircular(parametrosCliente,successCrearSolicitud,errorCrearSolicitud);

}
if($scope.ingSol.oSeleccionRegistros.registroOrdenes === true)
{
	
//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosOrdenes.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosOrdenes.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosOrdenes.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosOrdenes.p_ListaRut 			    = ListadoRuts || [];
parametrosOrdenes.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosOrdenes.p_IdTipoCircular         = 'NCG380';
parametrosOrdenes.p_Observaciones           = $scope.data.Observaciones;
parametrosOrdenes.p_IdUsuario               = $scope.SesionIdUsuario;
parametrosOrdenes.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosOrdenes.p_Mercado					= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;
parametrosOrdenes.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosOrdenes.p_ListaRut = ListadoRuts || []; 
parametrosOrdenes.p_ListaRutAut = ListadoRutAuts || [];
parametrosOrdenes.p_ListaNemo = ListadoNemos || [];
parametrosOrdenes.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosOrdenes.p_IdTipoDocumento = 'NCG380ORD';
parametrosOrdenes.p_Esquema = 'NCG380Ord';
parametrosOrdenes.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosOrdenes.p_Version = parametrosOrdenes.p_Version.getFullYear();
parametrosOrdenes.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosOrdenes.p_FechaLimite ='2016-12-31';
parametrosOrdenes.p_IdEstadoDocumento = 'PEN';
parametrosOrdenes.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosOrdenes.p_FechaPresentacion = $scope.SesionFechaSistema;


 

parametrosOrdenes.p_IdModulo = 'NOR';
parametrosOrdenes.p_IdAgenda = 0;
parametrosOrdenes.p_IdTipoTarea = 'ExportaNCG380';
parametrosOrdenes.p_Segundos = 0;
parametrosOrdenes.p_CreaParam = 'S';

if($scope.data.Filtros.TodosCli===true)
{
	parametrosOrdenes.p_TodosRut = 1;	
}else{
	parametrosOrdenes.p_TodosRut = 0;	
}
if($scope.data.Filtros.TodosNemo===true)
{
	parametrosOrdenes.p_TodosNemo = 1;
}else{
	parametrosCliente.p_TodosNemo = 0;
}
if($scope.data.Filtros.TodosAut===true)
{
	parametrosOrdenes.p_TodosAut = 1;
}else{
	parametrosOrdenes.p_TodosAut = 0;
}
	

var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull', 
		Valor : 'true'
	},
	{ 
		NombreParametro : 'FechaRegistro',
		Valor : $scope.data.Filtros.FechaInicio  
	}]};
var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
if (fd != null) {
		params.p_TablaParametrosaux.push({
		NombreParametro : 'fechaDesde',
		Valor : fd
	});
}

var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
if (fh != null) {
		params.p_TablaParametrosaux.push({
		NombreParametro : 'fechaHasta',
		Valor : fh
	});
}
	
	parametrosOrdenes.p_TablaParametros = params.p_TablaParametrosaux;
	

bcsComunes.msg('info', 'Carga', 'En proceso...');
bcsServiciosNormativa.CrearSolicitudCircular(parametrosOrdenes,successCrearSolicitud,errorCrearSolicitud);


	
}
if($scope.ingSol.oSeleccionRegistros.registroAsignacion === true)
{


//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosAsignaciones.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosAsignaciones.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosAsignaciones.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosAsignaciones.p_ListaRut 			    = ListadoRuts || [];
parametrosAsignaciones.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosAsignaciones.p_IdTipoCircular         = 'NCG380';
parametrosAsignaciones.p_Observaciones          = $scope.data.Observaciones;
parametrosAsignaciones.p_IdUsuario              = $scope.SesionIdUsuario;
parametrosAsignaciones.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosAsignaciones.p_Mercado				= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;//$scope.filtros.clasificacionTipoInstrumento;
parametrosAsignaciones.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosAsignaciones.p_ListaRut = ListadoRuts || []; 
parametrosAsignaciones.p_ListaRutAut = ListadoRutAuts || [];
parametrosAsignaciones.p_ListaNemo = ListadoNemos || [];
parametrosAsignaciones.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosAsignaciones.p_IdTipoDocumento = 'NCG380ASIG';
parametrosAsignaciones.p_Esquema = 'NCG380Asig';
parametrosAsignaciones.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosAsignaciones.p_Version = parametrosAsignaciones.p_Version.getFullYear();
parametrosAsignaciones.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosAsignaciones.p_FechaLimite ='2016-12-31';
parametrosAsignaciones.p_IdEstadoDocumento = 'PEN';
parametrosAsignaciones.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosAsignaciones.p_FechaPresentacion = $scope.SesionFechaSistema;


 

parametrosAsignaciones.p_IdModulo = 'NOR';
parametrosAsignaciones.p_IdAgenda = 0;
parametrosAsignaciones.p_IdTipoTarea = 'ExportaNCG380';
parametrosAsignaciones.p_Segundos = 0;
parametrosAsignaciones.p_CreaParam = 'S';
	
	if($scope.data.Filtros.TodosCli===true)
	{
		parametrosAsignaciones.p_TodosRut = 1;	
	}else{
		parametrosAsignaciones.p_TodosRut = 0;	
	}
	if($scope.data.Filtros.TodosNemo===true)
	{
		parametrosAsignaciones.p_TodosNemo = 1;
	}else{
		parametrosAsignaciones.p_TodosNemo = 0;
	}
	if($scope.data.Filtros.TodosAut===true)
	{
		parametrosAsignaciones.p_TodosAut = 1;
	}else{
		parametrosAsignaciones.p_TodosAut = 0;
	}
	
	
var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull',
		Valor : 'true'
	}]};
	var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
	if (fd != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaDesde',
			Valor : fd
		});
	}

	var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
	if (fh != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaHasta',
			Valor : fh
		});
	}
	
	parametrosAsignaciones.p_TablaParametros = params.p_TablaParametrosaux;

	bcsComunes.msg('info', 'Carga', 'En proceso...');
	bcsServiciosNormativa.CrearSolicitudCircular(parametrosAsignaciones,successCrearSolicitud,errorCrearSolicitud);
	
 }


if($scope.ingSol.oSeleccionRegistros.registroFacturacion === true)
{


//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosFacturacion.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosFacturacion.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosFacturacion.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosFacturacion.p_ListaRut 			    = ListadoRuts || [];
parametrosFacturacion.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosFacturacion.p_IdTipoCircular        	= 'NCG380';
parametrosFacturacion.p_Observaciones         	= $scope.data.Observaciones;
parametrosFacturacion.p_IdUsuario             	= $scope.SesionIdUsuario;
parametrosFacturacion.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosFacturacion.p_Mercado					= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;//$scope.filtros.clasificacionTipoInstrumento;
parametrosFacturacion.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosFacturacion.p_ListaRut = ListadoRuts || []; 
parametrosFacturacion.p_ListaRutAut = ListadoRutAuts || [];
parametrosFacturacion.p_ListaNemo = ListadoNemos || [];
parametrosFacturacion.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosFacturacion.p_IdTipoDocumento = 'NCG380FAC';
parametrosFacturacion.p_Esquema = 'NCG380Fac';
parametrosFacturacion.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosFacturacion.p_Version = parametrosFacturacion.p_Version.getFullYear();
parametrosFacturacion.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosFacturacion.p_FechaLimite ='2016-12-31';
parametrosFacturacion.p_IdEstadoDocumento = 'PEN';
parametrosFacturacion.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosFacturacion.p_FechaPresentacion = $scope.SesionFechaSistema;


  

parametrosFacturacion.p_IdModulo = 'NOR';
parametrosFacturacion.p_IdAgenda = 0;
parametrosFacturacion.p_IdTipoTarea = 'ExportaNCG380';
parametrosFacturacion.p_Segundos = 0;
parametrosFacturacion.p_CreaParam = 'S';
	
	if($scope.data.Filtros.TodosCli===true)
	{
		parametrosFacturacion.p_TodosRut = 1;	
	}else{
		parametrosFacturacion.p_TodosRut = 0;	
	}
	if($scope.data.Filtros.TodosNemo===true)
	{
		parametrosFacturacion.p_TodosNemo = 1;
	}else{
		parametrosFacturacion.p_TodosNemo = 0;
	}
	if($scope.data.Filtros.TodosAut===true)
	{
		parametrosFacturacion.p_TodosAut = 1;
	}else{
		parametrosFacturacion.p_TodosAut = 0;
	}
	
	
var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull',
		Valor : 'true'
	}]};
	var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
	if (fd != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaDesde',
			Valor : fd
		});
	}

	var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
	if (fh != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaHasta',
			Valor : fh
		});
	}
	
	parametrosFacturacion.p_TablaParametros = params.p_TablaParametrosaux;

	bcsComunes.msg('info', 'Carga', 'En proceso...');
	bcsServiciosNormativa.CrearSolicitudCircular(parametrosFacturacion,successCrearSolicitud,errorCrearSolicitud);
	
 }

if($scope.ingSol.oSeleccionRegistros.registroCustodia === true)
{


//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosCustodia.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosCustodia.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosCustodia.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosCustodia.p_ListaRut 			    = ListadoRuts || [];
parametrosCustodia.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosCustodia.p_IdTipoCircular        	= 'NCG380';
parametrosCustodia.p_Observaciones         	= $scope.data.Observaciones;
parametrosCustodia.p_IdUsuario             	= $scope.SesionIdUsuario;
parametrosCustodia.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosCustodia.p_Mercado					= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;//$scope.filtros.clasificacionTipoInstrumento;
parametrosCustodia.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosCustodia.p_ListaRut = ListadoRuts || []; 
parametrosCustodia.p_ListaRutAut = ListadoRutAuts || [];
parametrosCustodia.p_ListaNemo = ListadoNemos || [];
parametrosCustodia.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosCustodia.p_IdTipoDocumento = 'NCG380CUS';
parametrosCustodia.p_Esquema = 'NCG380Cus';
parametrosCustodia.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosCustodia.p_Version = parametrosCustodia.p_Version.getFullYear();
parametrosCustodia.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosCustodia.p_FechaLimite ='2016-12-31';
parametrosCustodia.p_IdEstadoDocumento = 'PEN';
parametrosCustodia.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosCustodia.p_FechaPresentacion = $scope.SesionFechaSistema;


  

parametrosCustodia.p_IdModulo = 'NOR';
parametrosCustodia.p_IdAgenda = 0;
parametrosCustodia.p_IdTipoTarea = 'ExportaNCG380';
parametrosCustodia.p_Segundos = 0;
parametrosCustodia.p_CreaParam = 'S';
	
	if($scope.data.Filtros.TodosCli===true)
	{
		parametrosCustodia.p_TodosRut = 1;	
	}else{
		parametrosCustodia.p_TodosRut = 0;	
	}
	if($scope.data.Filtros.TodosNemo===true)
	{
		parametrosCustodia.p_TodosNemo = 1;
	}else{
		parametrosCustodia.p_TodosNemo = 0;
	}
	if($scope.data.Filtros.TodosAut===true)
	{
		parametrosCustodia.p_TodosAut = 1;
	}else{
		parametrosCustodia.p_TodosAut = 0;
	}
	
	
var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull',
		Valor : 'true'
	}]};
	var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
	if (fd != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaDesde',
			Valor : fd
		});
	}

	var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
	if (fh != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaHasta',
			Valor : fh
		});
	}
	 
	parametrosCustodia.p_TablaParametros = params.p_TablaParametrosaux;

	bcsComunes.msg('info', 'Carga', 'En proceso...');
	bcsServiciosNormativa.CrearSolicitudCircular(parametrosCustodia,successCrearSolicitud,errorCrearSolicitud);
	
 }
if($scope.ingSol.oSeleccionRegistros.registroTrabajador === true)
{


//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosTrabajador.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosTrabajador.p_RutEmpleado 				= $scope.data.oConfiguracionEmpleado.seleccionado.DocumentoIdentidadDefecto;
parametrosTrabajador.p_FechaInicio 				= $scope.data.Filtros.FechaInicio;
parametrosTrabajador.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosTrabajador.p_ListaRut 			    = ListadoRuts || [];
parametrosTrabajador.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosTrabajador.p_IdTipoCircular        	= 'NCG380';
parametrosTrabajador.p_Observaciones         	= $scope.data.Observaciones;
parametrosTrabajador.p_IdUsuario             	= $scope.SesionIdUsuario;
parametrosTrabajador.p_Nemo						= $scope.data.Filtros.Nemo;
parametrosTrabajador.p_Mercado					= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;//$scope.filtros.clasificacionTipoInstrumento;
parametrosTrabajador.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosTrabajador.p_ListaRut = ListadoRuts || []; 
parametrosTrabajador.p_ListaRutAut = ListadoRutAuts || [];
parametrosTrabajador.p_ListaNemo = ListadoNemos || [];
parametrosTrabajador.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosTrabajador.p_IdTipoDocumento = 'NCG380TRA';
parametrosTrabajador.p_Esquema = 'NCG380Tra';
parametrosTrabajador.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosTrabajador.p_Version = parametrosTrabajador.p_Version.getFullYear();
parametrosTrabajador.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosTrabajador.p_FechaLimite ='2016-12-31';
parametrosTrabajador.p_IdEstadoDocumento = 'PEN';
parametrosTrabajador.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosTrabajador.p_FechaPresentacion = $scope.SesionFechaSistema;


  

parametrosTrabajador.p_IdModulo = 'NOR';
parametrosTrabajador.p_IdAgenda = 0;
parametrosTrabajador.p_IdTipoTarea = 'ExportaNCG380';
parametrosTrabajador.p_Segundos = 0;
parametrosTrabajador.p_CreaParam = 'S';
	
	if($scope.data.Filtros.TodosCli===true)
	{
		parametrosTrabajador.p_TodosRut = 1;	
	}else{
		parametrosTrabajador.p_TodosRut = 0;	
	}
	if($scope.data.Filtros.TodosNemo===true)
	{
		parametrosTrabajador.p_TodosNemo = 1;
	}else{
		parametrosTrabajador.p_TodosNemo = 0;
	}
	if($scope.data.Filtros.TodosAut===true)
	{
		parametrosTrabajador.p_TodosAut = 1;
	}else{
		parametrosTrabajador.p_TodosAut = 0;
	}
	if($scope.data.Filtros.TodosEmpl===true)
	{
		parametrosTrabajador.p_TodosEmpl = 1;
	}else{
		parametrosTrabajador.p_TodosEmpl = 0;
	}
	
	
var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull',
		Valor : 'true'
	}]};
	var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
	if (fd != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaDesde',
			Valor : fd
		});
	}

	var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
	if (fh != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaHasta',
			Valor : fh
		});
	}
	 
	parametrosTrabajador.p_TablaParametros = params.p_TablaParametrosaux;

	bcsComunes.msg('info', 'Carga', 'En proceso...');
	bcsServiciosNormativa.CrearSolicitudCircular(parametrosTrabajador,successCrearSolicitud,errorCrearSolicitud);
	
 }

if($scope.ingSol.oSeleccionRegistros.registroAutorizados === true)
{


//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosAutorizados.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosAutorizados.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosAutorizados.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosAutorizados.p_ListaRut 			    = ListadoRuts || [];
parametrosAutorizados.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosAutorizados.p_IdTipoCircular        	= 'NCG380';
parametrosAutorizados.p_Observaciones         	= $scope.data.Observaciones;
parametrosAutorizados.p_IdUsuario             	= $scope.SesionIdUsuario;
parametrosAutorizados.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosAutorizados.p_Mercado					= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;//$scope.filtros.clasificacionTipoInstrumento;
parametrosAutorizados.p_Oficio					= $scope.data.Filtros.Oficio;
parametrosAutorizados.p_RutAut 				    = $scope.data.oConfiguracionAutorizado.seleccionado.DocumentoIdentidadDefecto;

 
parametrosAutorizados.p_ListaRut = ListadoRuts || []; 
parametrosAutorizados.p_ListaRutAut = ListadoRutAuts || [];
parametrosAutorizados.p_ListaNemo = ListadoNemos || [];
parametrosAutorizados.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosAutorizados.p_IdTipoDocumento = 'NCG380AUT';
parametrosAutorizados.p_Esquema = 'NCG380Aut';
parametrosAutorizados.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosAutorizados.p_Version = parametrosAutorizados.p_Version.getFullYear();
parametrosAutorizados.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosAutorizados.p_FechaLimite ='2016-12-31';
parametrosAutorizados.p_IdEstadoDocumento = 'PEN';
parametrosAutorizados.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosAutorizados.p_FechaPresentacion = $scope.SesionFechaSistema;


  

parametrosAutorizados.p_IdModulo = 'NOR';
parametrosAutorizados.p_IdAgenda = 0;
parametrosAutorizados.p_IdTipoTarea = 'ExportaNCG380';
parametrosAutorizados.p_Segundos = 0;
parametrosAutorizados.p_CreaParam = 'S';
	
	if($scope.data.Filtros.TodosCli===true)
	{
		parametrosAutorizados.p_TodosRut = 1;	
	}else{
		parametrosAutorizados.p_TodosRut = 0;	
	}
	if($scope.data.Filtros.TodosNemo===true)
	{
		parametrosAutorizados.p_TodosNemo = 1;
	}else{
		parametrosAutorizados.p_TodosNemo = 0;
	}
	if($scope.data.Filtros.TodosAut===true)
	{
		parametrosAutorizados.p_TodosAut = 1;
	}else{
		parametrosAutorizados.p_TodosAut = 0;
	}
	
	
var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull',
		Valor : 'true'
	}]};
	var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
	if (fd != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaDesde',
			Valor : fd
		});
	}

	var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
	if (fh != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaHasta',
			Valor : fh
		});
	}
	 
	parametrosAutorizados.p_TablaParametros = params.p_TablaParametrosaux;

	bcsComunes.msg('info', 'Carga', 'En proceso...');
	bcsServiciosNormativa.CrearSolicitudCircular(parametrosAutorizados,successCrearSolicitud,errorCrearSolicitud);
	
 }



if($scope.ingSol.oSeleccionRegistros.registroOperaciones === true)
{


//CREAR SOLICITUD Y OBTENER IDSOLICITUD

parametrosOperaciones.p_IdCliente 				= $scope.data.oConfiguracionSocioNegocio.seleccionado.IdCliente;
parametrosOperaciones.p_FechaInicio 			= $scope.data.Filtros.FechaInicio;
parametrosOperaciones.p_FechaTermino 		    = $scope.data.Filtros.FechaFin;
parametrosOperaciones.p_ListaRut 			    = ListadoRuts || [];
parametrosOperaciones.p_FechaIngresoSolicitud 	= $scope.data.oCircularConfiguracion.fechaInicioSol; //util.dateSPdate($scope.data.kngFechaSistema);
parametrosOperaciones.p_IdTipoCircular        	= 'NCG380';
parametrosOperaciones.p_Observaciones         	= $scope.data.Observaciones;
parametrosOperaciones.p_IdUsuario             	= $scope.SesionIdUsuario;
parametrosOperaciones.p_Nemo					= $scope.data.Filtros.Nemo;
parametrosOperaciones.p_Mercado					= 'mcdo';//$scope.filtros.clasificacionTipoInstrumento.IdClasificacionTipoInstrumento;//$scope.filtros.clasificacionTipoInstrumento;
parametrosOperaciones.p_Oficio					= $scope.data.Filtros.Oficio;


parametrosOperaciones.p_ListaRut = ListadoRuts || []; 
parametrosOperaciones.p_ListaRutAut = ListadoRutAuts || [];
parametrosOperaciones.p_ListaNemo = ListadoNemos || [];
parametrosOperaciones.p_ListaRutEmpl = ListadoRutEmpl || [];



parametrosOperaciones.p_IdTipoDocumento = 'NCG380OPE';
parametrosOperaciones.p_Esquema = 'NCG380Ope';
parametrosOperaciones.p_Version = util.dateInicioDia($scope.SesionFechaSistema);
parametrosOperaciones.p_Version = parametrosOperaciones.p_Version.getFullYear();
parametrosOperaciones.p_IdEmpresa = $scope.SesionIdEmpresa;
parametrosOperaciones.p_FechaLimite ='2016-12-31';
parametrosOperaciones.p_IdEstadoDocumento = 'PEN';
parametrosOperaciones.p_FechaCreacion = $scope.SesionFechaSistema;
parametrosOperaciones.p_FechaPresentacion = $scope.SesionFechaSistema;


  

parametrosOperaciones.p_IdModulo = 'NOR';
parametrosOperaciones.p_IdAgenda = 0;
parametrosOperaciones.p_IdTipoTarea = 'ExportaNCG380';
parametrosOperaciones.p_Segundos = 0;
parametrosOperaciones.p_CreaParam = 'S';
	
	if($scope.data.Filtros.TodosCli===true)
	{
		parametrosOperaciones.p_TodosRut = 1;	
	}else{
		parametrosOperaciones.p_TodosRut = 0;	
	}
	if($scope.data.Filtros.TodosNemo===true)
	{
		parametrosOperaciones.p_TodosNemo = 1;
	}else{
		parametrosOperaciones.p_TodosNemo = 0;
	}
	if($scope.data.Filtros.TodosAut===true)
	{
		parametrosOperaciones.p_TodosAut = 1;
	}else{
		parametrosOperaciones.p_TodosAut = 0;
	}
	
	
var p_TablaParametrosaux ;
var params = {
p_TablaParametrosaux : [ //{NombreParametro : 'IdSolicitudCircular',Valor : IdSolicitudCircular}
	// ,{NombreParametro : 'idDocumento',Valor : IdDocumento},
	{
		NombreParametro : 'idUsuario',
		Valor : $scope.SesionIdUsuario
	},{
		NombreParametro : 'schema',
		Valor :  Model.getSchema()
	}, {
		NombreParametro : 'grabar',
		Valor : $scope.grabar
	}, {
		NombreParametro : 'reemplazar',
		Valor : $scope.reemplazar
	},{
		NombreParametro : 'separadorListas',
		Valor : $scope.data.separadorListas
	},{
		NombreParametro : 'simboloDecimal',
		Valor : $scope.data.simboloDecimal
	},{
		NombreParametro : 'simboloFecha',
		Valor : $scope.data.simboloFecha
	},{
		NombreParametro : 'aceptarFechasNull',
		Valor : 'true'
	}]};
	var fd = Utils.formatDate($scope.data.Filtros.FechaInicio);
	if (fd != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaDesde',
			Valor : fd
		});
	}

	var fh = Utils.formatDate($scope.data.Filtros.FechaFin);
	if (fh != null) {
			params.p_TablaParametrosaux.push({
			NombreParametro : 'fechaHasta',
			Valor : fh
		});
	}
	 
	parametrosOperaciones.p_TablaParametros = params.p_TablaParametrosaux;

	bcsComunes.msg('info', 'Carga', 'En proceso...');
	bcsServiciosNormativa.CrearSolicitudCircular(parametrosOperaciones,successCrearSolicitud,errorCrearSolicitud);
	
 }




}//fin if condicion seleccion registros
else
{
	
	bcsComunes.msg("info","info",'Debe seleccionar al menos 1 tipo registro');
}

}



$scope.$watch('data.Filtros.TodosCli',function(newval,oldval){
	switch (newval){
		case true:  
			$scope.activadoCli = false;  
			$scope.listRut = true;
			break;					
		case false: 
			$scope.activadoCli = true;
			$scope.listRut = false;
			break; 
	}			
});	

$scope.$watch('data.Filtros.TodosNemo',function(newval,oldval){
	switch (newval){
		case true:  
			$scope.seleccionNemo = true;  
			$scope.listNemo = true;
			break;					
		case false:  
			$scope.seleccionNemo = false;  
			$scope.listNemo = false;
			break; 
	}			
});	

$scope.$watch('data.Filtros.TodosAut',function(newval,oldval){
	switch (newval){
		case true:  
			$scope.activadoAut = false;  
			$scope.listRutAut = false;
			break;					
		case false: 
			$scope.activadoAut = true;  
			$scope.listRutAut = true;
			break; 
	}			
});	

$scope.$watch('data.Filtros.TodosEmpl',function(newval,oldval){
	switch (newval){
		case true:  
			$scope.listRutEmpl = true;  
			$scope.activadoEmpl = false;
			break;					
		case false: 
			$scope.listRutEmpl = false;  
			$scope.activadoEmpl = true;
			break; 
	}			
});	


$scope.$watchGroup(['data.Filtros.FechaInicio','data.Filtros.FechaFin',
                    'ingSol.oSeleccionRegistros.registroCliente',
                    'ingSol.oSeleccionRegistros.registroAutorizados',
                    'ingSol.oSeleccionRegistros.registroOrdenes',
                    'ingSol.oSeleccionRegistros.registroOperaciones',
                    'ingSol.oSeleccionRegistros.registroAsignacion',
                    'ingSol.oSeleccionRegistros.registroFacturacion',
                    'ingSol.oSeleccionRegistros.registroCustodia',
                    'ingSol.oSeleccionRegistros.registroTrabajador'],function(newval,oldval){

		
		if(( $scope.ingSol.oSeleccionRegistros.registroAutorizados == true || $scope.ingSol.oSeleccionRegistros.registroCliente==true|| $scope.ingSol.oSeleccionRegistros.registroOperaciones==true || $scope.ingSol.oSeleccionRegistros.registroFacturacion==true || $scope.ingSol.oSeleccionRegistros.registroTrabajador==true || $scope.ingSol.oSeleccionRegistros.registroOrdenes==true || $scope.ingSol.oSeleccionRegistros.registroAsignacion==true || $scope.ingSol.oSeleccionRegistros.registroCustodia==true )	&& ($scope.data.Filtros.FechaInicio && $scope.data.Filtros.FechaFin ))
		{
			$scope.seleccionGrabar = true;	
		}else{
			
			$scope.seleccionGrabar = false;
		}
});
    
$scope.$watchGroup(['ingSol.oSeleccionRegistros.registroCliente','ingSol.oSeleccionRegistros.registroAutorizados','ingSol.oSeleccionRegistros.registroOrdenes','ingSol.oSeleccionRegistros.registroOperaciones','ingSol.oSeleccionRegistros.registroAsignacion','ingSol.oSeleccionRegistros.registroFacturacion','ingSol.oSeleccionRegistros.registroCustodia','ingSol.oSeleccionRegistros.registroTrabajador'],function(newval,oldval){
		
//$scope.seleccionCli = true; filtro estatico
//$scope.seleccionFecInicio = true; filtro estatico
//$scope.seleccionFecFin= true; filtro estatico
$scope.seleccionNemo= true;
$scope.seleccionMcdo= false;
$scope.seleccionOficio= false;
//$scope.seleccionRutTra=true;
$scope.activadoEmpl=false;
$scope.activadoAut=false;
$scope.activadoCli=true; 

$scope.seleccionTodosRut=false;
$scope.seleccionTodosNemo=true;
$scope.seleccionTodosEmpl=true;
$scope.seleccionTodosAut=true;

$scope.listRut = false;				
$scope.listRutAut = true;
$scope.listNemo = true;
$scope.listRutEmpl = true;

	
	

//da igual ya que los filtros de cliente aplican a todos los registros restantes
/*	if($scope.ingSol.oSeleccionRegistros.registroCliente==true) 
	{
		$scope.seleccionCli 		= false;
		$scope.seleccionFecInicio 	= false;
		$scope.seleccionFecFin		= false;
		$scope.activado				= true;
		$scope.listRut 				= false;
		
		if($scope.data.Filtros.TodosCli ===true)
		{
			$scope.activado = false;
			$scope.data.oConfiguracionSocioNegocio.seleccionado = 0;
			
		}
		
    }*/ 
	
	

if($scope.ingSol.oSeleccionRegistros.registroTrabajador==true) 
{
	//$scope.seleccionRutTra=false;
	
	$scope.activadoEmpl = true;
	$scope.listRutEmpl = false;
	$scope.seleccionTodosEmpl=false;
	if($scope.ingSol.oSeleccionRegistros.registroCliente==false && $scope.ingSol.oSeleccionRegistros.registroOrdenes==false && $scope.ingSol.oSeleccionRegistros.registroAsignacion==false && $scope.ingSol.oSeleccionRegistros.registroCustodia==false && $scope.ingSol.oSeleccionRegistros.registroOperaciones==false && $scope.ingSol.oSeleccionRegistros.registroFacturacion==false && $scope.ingSol.oSeleccionRegistros.registroAutorizados==false) 
	{
		$scope.activadoCli =false;
		$scope.listRut = true;
	}
				
}
	

if($scope.ingSol.oSeleccionRegistros.registroOrdenes==true || $scope.ingSol.oSeleccionRegistros.registroAsignacion==true || $scope.ingSol.oSeleccionRegistros.registroCustodia==true ) 
{
	$scope.seleccionNemo		= false;
	$scope.seleccionMcdo		= false;
	$scope.seleccionTodosNemo	= false;
	
	$scope.listNemo 			= false;
}


if($scope.ingSol.oSeleccionRegistros.registroOperaciones==true || $scope.ingSol.oSeleccionRegistros.registroFacturacion==true) 
{
	$scope.seleccionNemo		= false;
	$scope.seleccionMcdo		= false;
	$scope.seleccionTodosNemo	= false;
	//$scope.seleccionRueda		= false;
	//$scope.seleccionNoRueda	= false;

	$scope.listNemo 			= false;
				
}
if($scope.ingSol.oSeleccionRegistros.registroAutorizados==true ) 
{
	$scope.activadoAut=true;
	$scope.listRutAut = false;
	$scope.seleccionTodosAut = false;
	
	if($scope.ingSol.oSeleccionRegistros.registroOrdenes==false && $scope.ingSol.oSeleccionRegistros.registroAsignacion==false && $scope.ingSol.oSeleccionRegistros.registroCustodia==false && $scope.ingSol.oSeleccionRegistros.registroOperaciones==false && $scope.ingSol.oSeleccionRegistros.registroFacturacion==false )
	{
		$scope.seleccionNemo		= true;
		$scope.seleccionTodosNemo	= true ;
		$scope.listNemo 			= true;
	}
	
	$scope.seleccionMcdo		= false;

	//$scope.seleccionRueda		= false;
	//$scope.seleccionNoRueda	= false;

				
}

	

});	
   

/*
$scope.datos.oConfiguracionClasificacionTipoInstrumento={
		filtros : { 	
			
		},
		idActual : false,
		seleccionado : false,
		idDefecto : ''
};	*/
/*	$scope.data.obtenerRutaArchivos = function()
    {	
	var parametrosRuta = {};	
	 RutaArchivo =  bcsServiciosNormativa.ObtenerRutaArchivo(parametrosRuta,successCrearSolicitudCircular,successCrearSolicitudCircular)
	return RutaArchivo;	
		
    };
	*/

    
    


});
