/*************************************************************
 * Purpose	: Panel Portafolio Contrato
 * Author	: TEAM PRONET IT SPA - BCS
 * Date		: 15/01/2018
 **************************************************************/
(function() {
	angular.module('ModuloAPV').controller('PanelEmpleadoContratoCtrl',
	function($scope,
			$translate,
			bcsComunes,
			$timeout,
			$state,
			$modal,
			$q,
			bcsConsultasAPV,
			bcsServiciosAPV	
			){
		
/******************************************************/
/*** INICIO DE VARIABLES Y PRECARGA					***/
/******************************************************/
		
		//Obtiene datos desde la ventana principal
		var idVentana = bcsComunes.ventanaWinId($scope);
		var overlay = bcsComunes.overlay;
		var parametrosVentana = bcsComunes.ventanaParams(idVentana);
		var pecs = this;

		pecs.datosForm ={};
		$scope.csaapv.pecs = pecs;
		
		var date = util.dateInicioDia($scope.SesionFechaSistema);
		var ultimoDia = new Date(date.getFullYear(),date.getMonth(),0);
		
		pecs.limpiarEmpleadorHabilitado 		= true;
		pecs.entidadRecaudadoraDeshabilitado	= false;
		
		pecs.DocumentoIdentidadEmpleadorRequerido	= true;
		pecs.RazonSocialRequerido					= true;
		
		pecs.IdentidadRecaudadoraRequerido 			= false;
		pecs.ComunaEmpleadorRequerido				= false;
		//pecs.DireccionComercialEmpleadorRequerido	= false;
		//pecs.NumeroDireccionEmpleadorRequerido		= false;
		
/******************************************************/
/*** FILTROS										***/
/******************************************************/
		
		pecs.ConfiguracionEntidadRecaudadora={
			filtros 		: {
				NombreTipoEntidadRecaudadora : {field: "NombreTipoEntidadRecaudadora", operator: "eq", value: ''}
			}
		};	
		
/******************************************************/
/*** DATASOURCES									***/
/******************************************************/
		
		var datosCiudad = 
		{
			type : "odata",
			definition : 
			{
                serverFiltering : true, // Filtrado de datos en el servidor.
                serverPaging    : true, // Paginado de los datos en el servidor.
                serverSorting   : true, // Ordenamiento de los datos en el servidor.
                pageSize        : 10,
                transport		: {
					read: {
						url 		: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1627",
						dataType	: "json"
					}
				},
		        schema: {
		         	model: {
		           		fields: {
							IdCiudad			: {type: "number"},
							NombreCiudad		: {type: "string"},
							NombreCiudadPais	: {type: "string"},
							IdPais				: {type: "number"},
							NombrePais			: {type: "string"},
							CodigoPais			: {type: "string"}
						}
					}
	            }
			}
		};
		
/******************************************************/
/*** FUNCIONES										***/
/******************************************************/
		
		pecs.limpiarEmpleador = function()
		{
			//Identificación Empleador
			pecs.datosForm.SocioNegocioidActualEmp	= false;
			pecs.DocumentoIdentidadEmpleador		= undefined;
		    pecs.RazonSocial						= undefined;
		    pecs.ComunaEmpleadorIdActual			= false
		    pecs.CiudadEmpleador					= undefined;
		    pecs.DireccionComercialEmpleador		= undefined;
		    pecs.kngNumeroDireccionEmpleador		= undefined;
		    pecs.TelefonoEmpleador					= undefined;
		    
		    //Encargado RRHH
		    pecs.NombreEncargado					= undefined;
		    pecs.EmailEncargado						= undefined;
		    pecs.TelefonoEncargado					= undefined;
		    
		    //Identificación Empleador
		    pecs.datosForm.DoctoIdentidadEmpDeshabilitado	= false;
		    pecs.datosForm.RazonSocialDeshabilitado			= false;
		    pecs.datosForm.ComunaEmpDeshabilitado			= true;
			pecs.datosForm.DirComercialEmpDeshabilitado		= false;
			pecs.datosForm.NumeroEmpDeshabilitado			= false;
			pecs.datosForm.telEmpDeshabilitado				= false;
			
			//Encargado RRHH
			pecs.datosForm.nomEncDeshabilitado				= false;
			pecs.datosForm.emailEncDeshabilitado			= false;
			pecs.datosForm.telEncEncDeshabilitado			= false;
			
			pecs.DocumentoIdentidadEmpleadorRequerido	= true;
			pecs.RazonSocialRequerido					= true;
			
			if(pecs.FormaPagoDirectaIdActual === 2)
			{
				pecs.ComunaEmpleadorRequerido				= true;
				//pecs.DireccionComercialEmpleadorRequerido	= true;
				//pecs.NumeroDireccionEmpleadorRequerido		= true;
			}
			else
			{
				pecs.ComunaEmpleadorRequerido				= false;
				//pecs.DireccionComercialEmpleadorRequerido	= false;
				//pecs.NumeroDireccionEmpleadorRequerido		= false;
			}
		}
		
		pecs.validaFormatoEmail = function(email)
		{
			if(email === 'ENC')
			{
				if (pecs.EmailEncargado !== undefined && pecs.EmailEncargado !== '' && region.ValidaFormatoEmail(pecs.EmailEncargado)==false)
				{
					pecs.EmailEncargado = '';
		    		bcsComunes.msg("warning","warning",$translate.instant("UI_FORMATO_EMAIL_INCORRECTO"));
					$scope.formContratoSAAPV.$valid = undefined;
					return false;
				}	
			}
	    	
	    	return true;
	    };
	    
	    /*
	    pecs.ValidoDiaDescuento = function(num)
	    {
			if(num > ultimoDia.getDate(num) || num === 0)
			{
				bcsComunes.msg("warning","warning","DIA_DE_DESCUENTO_TIENE_QUE_SER_MAYOR_A_0_Y_MENOR_O_IGUAL_A_" + ultimoDia.getDate());
				pecs.kngDiaDescuento = ultimoDia.getDate(); 
			}
		};
		*/
	    
/******************************************************/
/*** WATCH											***/
/******************************************************/
		
		$scope.$watch('pecs.datosForm.SocioNegocioSeleccionadoEmp',function(newval,oldval)
		{ 	
			if ((newval=== oldval) && (newval === undefined))
			{
				return
			}
			else
			{
				if(pecs.datosForm.SocioNegocioSeleccionadoEmp ===  false )
				{
					/*
					pecs.datosForm.RazonSocialDeshabilitado						= false;
					pecs.datosForm.DoctoIdentidadEmpDeshabilitado 				= false;
					pecs.datosForm.DirComercialEmpDeshabilitado					= false;
					pecs.datosForm.NumeroEmpDeshabilitado						= false;
					pecs.datosForm.ComunaEmpDeshabilitado						= true;
					pecs.bloqueoPersona											= false;
					pecs.datosForm.telEmpDeshabilitado							= false;
					pecs.datosForm.nomEncDeshabilitado							= false;
					pecs.datosForm.emailEncDeshabilitado						= false;
					pecs.datosForm.telEncEncDeshabilitado						= false;
					*/
					
					pecs.datosForm.CiudadEmpDeshabilitado						= true;
					
					pecs.datosForm.FormaDePagoIndirectaDesabilitado				= true;
					pecs.datosForm.EntidadRecaudadoraInstitucionDesabilitado	= true;
				}
				else
				{								
					$scope.csaapv.fnCargarValoresPersona('J');
					
					pecs.DocumentoIdentidadEmpleadorRequerido	= false;
					pecs.RazonSocialRequerido					= false;
					pecs.ComunaEmpleadorRequerido				= false;
					//pecs.DireccionComercialEmpleadorRequerido	= false;
					//pecs.NumeroDireccionEmpleadorRequerido		= false;
				}
			}
		});

		pecs.Directa = true;
		$scope.$watch('pecs.Directa',function(newval,oldval)
		{
			if(pecs.Directa)
			{
				pecs.Indirecta = false;
				pecs.FormaPagoDirectaEnabled = false;
				
			    pecs.EntidadRecaudadoraInstitucion		= undefined;
			    pecs.FormaDePagoIndirecta				= undefined;
			    pecs.kngComisionTransferencia			= undefined;
			}
			else
			{
				pecs.FormaPagoDirectaEnabled 	= true;
				pecs.FormaPagoDirectaIdActual	= undefined;
				pecs.DiaDescuentoIdActual 		= undefined;
				
				pecs.Indirecta = true;
			}
		});
		
		$scope.$watch('pecs.Indirecta',function(newval,oldval)
		{
			if(pecs.Indirecta)
			{
				pecs.Directa 						= false;
				pecs.FormaPagoIndirectaEnabled 		= false;
				pecs.ComisionTransferenciaRequerido = true;
				pecs.DiaDescuentoIdActual 			= undefined;
				
				if(pecs.MedioRecaudacionTrabajadorIdActual !== undefined)
					pecs.FormaDePagoIndirecta = pecs.IdMedioRecaudacionTrabajador.Nombre;
				
				if(pecs.IdentidadRecaudadoraIdActual !== undefined)
					pecs.EntidadRecaudadoraInstitucion = pecs.entidadRecaudadora.Nombre;
				
				pecs.ComunaEmpleadorRequerido				= false;
				//pecs.DireccionComercialEmpleadorRequerido	= false;
				//pecs.NumeroDireccionEmpleadorRequerido		= false;
			}
			else
			{
				pecs.FormaPagoIndirectaEnabled					= true;
				pecs.MedioRecaudacionFormaPagoIndirectaIdActual	= undefined;
				pecs.EntidadRecaudadoraInstitucion 				= undefined;
				pecs.kngComisionTransferencia 					= undefined;
				pecs.ComisionTransferenciaRequerido				= false;
				
				pecs.Directa = true;
			}
		});
		
		$scope.$watch('pecs.FormaPagoDirecta',function(newval,oldval)
		{
			if(pecs.FormaPagoDirecta !== undefined)
			{
				if(pecs.FormaPagoDirecta.Nombre === "PAC")
				{
					pecs.DiaDescuentoRequerido 	= true
					pecs.DiaDescuentoEnabled 	= false;
				}
				else
				{
					pecs.DiaDescuentoRequerido 	= false;
					pecs.DiaDescuentoEnabled 	= true;
					pecs.DiaDescuentoIdActual	= undefined;
				}
			}
		});

		$scope.$watch('pecs.MedioRecaudacionTrabajadorIdActual',function(newval,oldval)
		{
			if(pecs.MedioRecaudacionTrabajadorIdActual !== undefined && pecs.MedioRecaudacionTrabajadorIdActual !== false)
			{
				if(pecs.IdMedioRecaudacionTrabajador.Nombre === 'IPS-EX INP')
				{
					pecs.entidadRecaudadoraDeshabilitado = false;
					pecs.IdentidadRecaudadoraIdActual = undefined;
					
					pecs.IdentidadRecaudadoraRequerido = false;
				}
				else
				{
					pecs.entidadRecaudadoraDeshabilitado = true;

					pecs.IdentidadRecaudadoraRequerido = true;
				}
			}
		});

		$scope.$watch('pecs.FormaPagoDirectaIdActual',function(newval,oldval)
		{
			if(pecs.TrabajadorIdActual !== undefined && pecs.TrabajadorIdActual !== false  && newval !== oldval)
			{
				if(pecs.TrabajadorIdActual === 'IND' && pecs.FormaPagoDirectaIdActual ===2)
				{
					bcsComunes.msg("warning","warning",$translate.instant("FORMA_DE_PAGO_NO_APLICA_A_TIPO_TRABAJADOR")); 
					pecs.FormaPagoDirectaIdActual = '';
				}
			}
			
			if(pecs.FormaPagoDirectaIdActual !== undefined && pecs.FormaPagoDirectaIdActual !== false)
			{
				if(pecs.FormaPagoDirectaIdActual === 2 && parametrosVentana.modoVentana === 'I')
				{
					pecs.ComunaEmpleadorRequerido				= true;
					//pecs.DireccionComercialEmpleadorRequerido	= true;
					//pecs.NumeroDireccionEmpleadorRequerido		= true;
				}
				else
				{
					pecs.ComunaEmpleadorRequerido				= false;
					//pecs.DireccionComercialEmpleadorRequerido	= false;
					//pecs.NumeroDireccionEmpleadorRequerido		= false;
				}
			}
		});

		$scope.$watch('pecs.TrabajadorIdActual',function(newval,oldval)
		{
			if(pecs.TrabajadorIdActual !== undefined && pecs.TrabajadorIdActual !== false  && newval !== oldval && newval !== '')
			{
				pecs.FormaPagoDirectaIdActual = '';		
			}
		});
		
		$scope.$watch('pecs.MedioRecaudacionTrabajadorIdActual',function(newval,oldval)
		{
			if(pecs.Indirecta)
			{
				if(pecs.MedioRecaudacionTrabajadorIdActual !== undefined)
					pecs.FormaDePagoIndirecta = pecs.IdMedioRecaudacionTrabajador.Nombre;
			}
		});
		
		$scope.$watch('pecs.IdentidadRecaudadoraIdActual',function(newval,oldval)
		{
			if(pecs.Indirecta)
			{
				if(pecs.IdentidadRecaudadoraIdActual !== undefined)
					pecs.EntidadRecaudadoraInstitucion = pecs.entidadRecaudadora.Nombre;
			}
		});

		$scope.$watch('pecs.ComunaEmpleadorIdActual',function(newval,oldval)
		{
			if(parametrosVentana.modoVentana === 'I')
			{
				if(pecs.ComunaEmpleadorIdActual !== undefined && pecs.ComunaEmpleadorIdActual !== false)
				{
					var ConsultaCiudad = new bcs.DataSource(datosCiudad);
					var filtro = { field: "IdCiudad", operator:"eq", value: Number(pecs.comunaEmpleadorSeleccionada.IdCiudad) };
					
					ConsultaCiudad.query({ filter: filtro }).then(function()
					{
						pecs.CiudadEmpleador = ConsultaCiudad.view()[0].NombreCiudad;
					});
				}
				else
				{
					pecs.CiudadEmpleador = undefined;
				}
			}
		});

		if(parametrosVentana.modoVentana === 'M')
		{
			if ($scope.csaapv.datosEmpleador.length  > 0 )
			{
				//Identificación Empleador
			    pecs.RazonSocial											= ($scope.csaapv.datosEmpleador[0].RazonSocial 					== undefined ? "" : $scope.csaapv.datosEmpleador[0].RazonSocial);
			    pecs.DocumentoIdentidadEmpleador							= ($scope.csaapv.datosEmpleador[0].DocumentoIdentidadEmpleador	== undefined ? "" : $scope.csaapv.datosEmpleador[0].DocumentoIdentidadEmpleador);
			    pecs.idComunaEmpleadorPostFiltro							= ($scope.csaapv.datosEmpleador[0].ComunaEmpleador	  			== undefined ? "" : $scope.csaapv.datosEmpleador[0].ComunaEmpleador);
			    pecs.CiudadEmpleador										= ($scope.csaapv.datosEmpleador[0].CiudadEmpleador	  			== undefined ? "" : $scope.csaapv.datosEmpleador[0].CiudadEmpleador);
			    pecs.DireccionComercialEmpleador							= ($scope.csaapv.datosEmpleador[0].DireccionComercialEmpleador	== undefined ? "" : $scope.csaapv.datosEmpleador[0].DireccionComercialEmpleador);
			    pecs.kngNumeroDireccionEmpleador							= ($scope.csaapv.datosEmpleador[0].NumeroDireccionEmpleador		== undefined ? "" : $scope.csaapv.datosEmpleador[0].NumeroDireccionEmpleador);
			    pecs.TelefonoEmpleador										= ($scope.csaapv.datosEmpleador[0].TelefonoEmpleador		 	== undefined ? "" : $scope.csaapv.datosEmpleador[0].TelefonoEmpleador);
			    //Encargado RRHH
			    pecs.NombreEncargado										= ($scope.csaapv.datosEmpleador[0].NombreEncargado				== undefined ? "" : $scope.csaapv.datosEmpleador[0].NombreEncargado);
			    pecs.EmailEncargado											= ($scope.csaapv.datosEmpleador[0].EmailEncargado				== undefined ? "" : $scope.csaapv.datosEmpleador[0].EmailEncargado);
			    pecs.TelefonoEncargado										= ($scope.csaapv.datosEmpleador[0].TelefonoEncargado			== undefined ? "" : $scope.csaapv.datosEmpleador[0].TelefonoEncargado);
			    //Tipo Trabajador
			    pecs.MedioRecaudacionTrabajadorIdActualPostFiltro			= ($scope.csaapv.datosEmpleador[0].MedioRecaudacionTrabajador	== undefined ? "" : $scope.csaapv.datosEmpleador[0].MedioRecaudacionTrabajador);
			    pecs.IdentidadRecaudadoraIdActualPostFiltro					= ($scope.csaapv.datosEmpleador[0].IdentidadRecaudadora			== undefined ? "" : $scope.csaapv.datosEmpleador[0].IdentidadRecaudadora);
			    pecs.PensionadoIdActualPostFiltro							= ($scope.csaapv.datosEmpleador[0].Pensionado					== undefined ? "" : $scope.csaapv.datosEmpleador[0].Pensionado);
			    pecs.TrabajadorIdActualPostFiltro							= ($scope.csaapv.datosEmpleador[0].Trabajador					== undefined ? "" : $scope.csaapv.datosEmpleador[0].Trabajador);
			    //Forma Pago
			    pecs.Directa												= ($scope.csaapv.datosEmpleador[0].Directa						== undefined ? "" : $scope.csaapv.datosEmpleador[0].Directa);
			    pecs.FormaPagoDirectaIdActualPostFiltro						= ($scope.csaapv.datosEmpleador[0].FormaPagoDirecta				== undefined ? "" : $scope.csaapv.datosEmpleador[0].FormaPagoDirecta);
			    pecs.DiaDescuentoIdActualPost								= ($scope.csaapv.datosEmpleador[0].DiaDescuento					== undefined ? "" : $scope.csaapv.datosEmpleador[0].DiaDescuento);
			    pecs.Indirecta												= ($scope.csaapv.datosEmpleador[0].Indirecta					== undefined ? "" : $scope.csaapv.datosEmpleador[0].Indirecta);
			    pecs.FormaDePagoIndirecta									= ($scope.csaapv.datosEmpleador[0].MedioRecaudacionIndirecta	== undefined ? "" : $scope.csaapv.datosEmpleador[0].MedioRecaudacionIndirecta);
			    pecs.EntidadRecaudadoraInstitucion							= ($scope.csaapv.datosEmpleador[0].Institucion					== undefined ? "" : $scope.csaapv.datosEmpleador[0].Institucion);
			    pecs.kngComisionTransferencia								= ($scope.csaapv.datosEmpleador[0].ComisionTransferencia		== undefined ? "" : $scope.csaapv.datosEmpleador[0].ComisionTransferencia);
			    
			    if(pecs.DocumentoIdentidadEmpleador !== "" && pecs.DocumentoIdentidadEmpleador !== undefined)
				{
				    //Identificación Empleador
					pecs.datosForm.RazonSocialDeshabilitado			= ($scope.csaapv.datosEmpleador[0].RazonSocialDesabilitado 					== undefined ? $scope.csaapv.datosEmpleador[0].RazonSocialDesabilitado : true);
					pecs.datosForm.DoctoIdentidadEmpDeshabilitado 	= ($scope.csaapv.datosEmpleador[0].DocumentoIdentidadEmpleadorDesabilitado	== undefined ? $scope.csaapv.datosEmpleador[0].DocumentoIdentidadEmpleadorDesabilitado : true);
					pecs.datosForm.ComunaEmpDeshabilitado			= ($scope.csaapv.datosEmpleador[0].ComunaEmpleadorDesabilitado				== undefined ? $scope.csaapv.datosEmpleador[0].ComunaEmpleadorDesabilitado : false);
					pecs.datosForm.DirComercialEmpDeshabilitado		= ($scope.csaapv.datosEmpleador[0].DireccionComercialEmpleadorDesabilitado	== undefined ? $scope.csaapv.datosEmpleador[0].DireccionComercialEmpleadorDesabilitado : true);
					pecs.datosForm.NumeroEmpDeshabilitado			= ($scope.csaapv.datosEmpleador[0].NumeroDireccionEmpleadorDesabilitado		== undefined ? $scope.csaapv.datosEmpleador[0].NumeroDireccionEmpleadorDesabilitado : true);
					pecs.datosForm.telEmpDeshabilitado				= ($scope.csaapv.datosEmpleador[0].TelefonoEmpleadorDesabilitado			== undefined ? $scope.csaapv.datosEmpleador[0].TelefonoEmpleadorDesabilitado : true);
					//Encargado RRHH
					pecs.datosForm.nomEncDeshabilitado				= ($scope.csaapv.datosEmpleador[0].NombreEncargadoDesabilitado				== undefined ? $scope.csaapv.datosEmpleador[0].NombreEncargadoDesabilitado : true);
					pecs.datosForm.emailEncDeshabilitado			= ($scope.csaapv.datosEmpleador[0].EmailEncargadoDesabilitado				== undefined ? $scope.csaapv.datosEmpleador[0].EmailEncargadoDesabilitado : true);
					pecs.datosForm.telEncEncDeshabilitado			= ($scope.csaapv.datosEmpleador[0].TelefonoEncargadoDesabilitado			== undefined ? $scope.csaapv.datosEmpleador[0].TelefonoEncargadoDesabilitado : true);
					
					pecs.limpiarEmpleadorHabilitado = false;					
					
					pecs.datosForm.SocioNegocioEmpDesabilitado	= false;
					
					pecs.DocumentoIdentidadEmpleadorRequerido	= false;
					pecs.RazonSocialRequerido					= false;
					pecs.ComunaEmpleadorRequerido				= false;
					//pecs.DireccionComercialEmpleadorRequerido	= false;
					//pecs.NumeroDireccionEmpleadorRequerido		= false;
				}
			}
		}
		else
		{
			pecs.datosForm.SocioNegocioEmpDesabilitado	= true;
		}
		
/******************************************************/
/*** FINAL											***/
/******************************************************/
		
	});
}());