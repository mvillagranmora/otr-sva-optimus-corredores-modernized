(function() {
	angular.module('ModuloNormativa').controller('IngresarCir1720Ctrl',
	function($scope,$timeout,$compile,$modal,$translate,FileUpload,Model,Utils,bcsComunes,bcsAlerta,OPCIONES_BCSALERTA,bcsServiciosNormativa){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var IngresarCir1720 = this;
		IngresarCir1720.idVentana = bcsComunes.ventanaWinId($scope); 
		IngresarCir1720.params = bcsComunes.ventanaParams(IngresarCir1720.idVentana);
		IngresarCir1720.domVentana = $("[ventanaid='"+IngresarCir1720.idVentana+"']");
		bcsComunes.ventanaAutoOverlay(IngresarCir1720.idVentana,false,3000);
		IngresarCir1720.fechaInicioSol= util.dateInicioDia($scope.SesionFechaSistema);
		IngresarCir1720.FormValido = false;
		IngresarCir1720.FecTermino 	 = $scope.IngresarCir1720.params.FechaConsulta;
		
		IngresarCir1720.modulo = 'NOR';

		IngresarCir1720.DatosIngreso = {};
		IngresarCir1720.DatosIngreso.CantidadNominales			=0;
		IngresarCir1720.DatosIngreso.Valorizacion				=0;
		IngresarCir1720.DatosIngreso.CarteraPropia				=0;
		IngresarCir1720.DatosIngreso.CustodiaTerceros			=0;
		IngresarCir1720.DatosIngreso.CustodiaPropiaDCV			=0;
		IngresarCir1720.DatosIngreso.CustodiaTercerosDCV		=0;
		IngresarCir1720.DatosIngreso.CustodiaSociedadEmisora	=0;
		IngresarCir1720.DatosIngreso.CustodiaIntermediario		=0;
		IngresarCir1720.DatosIngreso.CustodiaBolsas				=0;
		IngresarCir1720.DatosIngreso.CustodiaOtros				=0;
		
		IngresarCir1720.Config = {};
		IngresarCir1720.Config.ConDecimales 	=4;
		IngresarCir1720.Config.SinDecimales		=0; 

			
		if(IngresarCir1720.params.PrevVentana !==undefined)
		{
			IngresarCir1720.PrevVentana 	 = IngresarCir1720.params.PrevVentana;
		};
		
	
		bcsComunes.ventanaTitulo(IngresarCir1720.idVentana, $translate.instant('UI_CIR1720_CUADRATURA_TRIMESTRAL_DE_CUSTODIA'));
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
	
/*********************************************
 * COMBOBOX
 *********************************************/
 	     	
/*********************************************
 * FUNCIONES
 ********************************************/

	IngresarCir1720.cancelar = function() {
		bcsComunes.ventanaCerrar(IngresarCir1720.idVentana);
		};
	
	
	

	$scope.IngresarCir1720.ingresarSol = function(){
	
		parametrosServicio= {}; 
		
		var date = '';
		date= util.dateSPdate(IngresarCir1720.FecTermino);						
			
	    //Comunes
	    parametrosServicio.p_IdEmpresa 				= $scope.SesionIdEmpresa;
	    parametrosServicio.p_IdUsuario              = $scope.SesionIdUsuario;
	    parametrosServicio.p_FechaCuadratura		= date;
	    
	    //Especificos   
	    parametrosServicio.p_Nemotecnico				= IngresarCir1720.DatosIngreso.Nemotecnico;
	    parametrosServicio.p_NombreEmisor				= IngresarCir1720.DatosIngreso.Emisor;
	    parametrosServicio.p_TipoInstrumento			= IngresarCir1720.DatosIngreso.TipoInstrumento;
	    parametrosServicio.p_CantidadUnidadesNominal	= IngresarCir1720.DatosIngreso.CantidadNominales;
		parametrosServicio.p_TipoUnidades				= IngresarCir1720.DatosIngreso.TipoUnidades;
		parametrosServicio.p_Valorizacion				= IngresarCir1720.DatosIngreso.Valorizacion;
		parametrosServicio.p_CarteraPropia				= IngresarCir1720.DatosIngreso.CarteraPropia;
		parametrosServicio.p_CustodiaTerceros			= IngresarCir1720.DatosIngreso.CustodiaTerceros;
		parametrosServicio.p_CustodiaPropiaDCV			= IngresarCir1720.DatosIngreso.CustodiaPropiaDCV;
		parametrosServicio.p_CustodiaTercerosDCV		= IngresarCir1720.DatosIngreso.CustodiaTercerosDCV;
		parametrosServicio.p_CustodiaSociedadEmisora	= IngresarCir1720.DatosIngreso.CustodiaSociedadEmisora;
		parametrosServicio.p_CustodiaIntermediario		= IngresarCir1720.DatosIngreso.CustodiaIntermediario;
		parametrosServicio.p_CustodiaBolsas				= IngresarCir1720.DatosIngreso.CustodiaBolsas;
		parametrosServicio.p_CustodiaOtros				= IngresarCir1720.DatosIngreso.CustodiaOtros;
		
		callService(parametrosServicio);
	};
	

	 	$scope.$watchGroup(['FormIngresoCir1720.$valid'],function(newval,oldval){
			if($scope.FormIngresoCir1720.$valid ) IngresarCir1720.FormValido = true;
			else IngresarCir1720.FormValido = false;
	});
	
	var callService = function(v_Dato){

		bcsServiciosNormativa.IngresarInstrumentoCircular1720(v_Dato).$promise.then(
			
			function(result) {
				
				var titulo = "";

				if (result.P_OUT_ERRORESCONTADOR === 0){

					titulo = "Instrumento Agregado";
				}
				else{

					titulo = "Error al Ingresar Instrumento";
				}

				var idMensajeModal = bcsComunes.ventanaMsg({ mensaje : titulo,
					conflictos : util.parseServerResponse(result)}, IngresarCir1720.idVentana);

				bcsComunes.ventanaPromesa(idMensajeModal).then(function(){

					bcsComunes.ventanaCerrar(IngresarCir1720.idVentana);
				})
			}
		);
		
	}
	
 	});
 	

	

	
	
}());