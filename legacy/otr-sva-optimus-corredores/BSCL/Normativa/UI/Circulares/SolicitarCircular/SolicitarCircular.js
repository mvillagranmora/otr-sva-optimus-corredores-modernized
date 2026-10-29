/********************************************************************
 * Purpose	: Controlador para solicitar Circulares
 * Author	: CFLORES - BCS
 * Date		: 10/09/2016
 *******************************************************************/

(function() {
	angular.module('ModuloNormativa').controller('SolicitarCircularCtrl',
	function($state,$q,$scope,$compile,bcsComunes,$translate,$http){
		
/*********************************************
 * VARIABLES Y CONTEXTOS
 ********************************************/
		
		var solCir = this;
		solCir.idVentana = bcsComunes.ventanaWinId($scope); 
		solCir.params = bcsComunes.ventanaParams(solCir.idVentana);
		solCir.domVentana = $("[ventanaid='"+solCir.idVentana+"']");
		bcsComunes.ventanaAutoOverlay(solCir.idVentana,false,3000);

		solCir.AnchoVentana=bcsComunes.getAncho(solCir.idVentana);
		
		solCir.checkCliente = false;	
		solCir.fechaInicioSol= util.dateInicioDia($scope.SesionFechaSistema);
		solCir.cmbbx		= {};
		
		solCir.separadorListas = ';';
		solCir.simboloDecimal = ',';
		solCir.simboloFecha = 'dd/MM/yyyy';
		solCir.fuenteDatos = 'INICIAL';
		solCir.reemplazar = 'false';
		solCir.grabar = 'false';
		solCir.aceptarFechasNull = 'true';
		solCir.modulo = 'NOR';
		solCir.IdEstadoDocumento='PEN';

		$scope.selectedOption = 'Oficial';
		solCir.CheckOfiDet = "Oficial";
		// $watch para observar cambios en selectedOption de NCG380Cli oficial o Detallado
		$scope.$watch('selectedOption', function(newVal, oldVal) {
		if (newVal !== oldVal && solCir.checkCliente == true) {

			// Aquí puedes realizar las acciones que deseas cuando se selecciona cli si es oficial o detallado.
			if(newVal == "Detallado"){
				solCir.CheckOfiDet = newVal;
			}
			if(newVal == "Oficial"){
				solCir.CheckOfiDet = newVal;
			}
		}
		});

		
		
/*********************************************
 * FUNCIONES DE CARGA DE TABS
 ********************************************/
     	
     	// ** ART 171
		// CARTA 1 BCS
     	solCir.fnTabCarta1BCS = function () {
            console.log('PanelCarta1BCS click');
            if (solCir.TabCarta1BCSCargado !== true) {
            	solCir.TabCarta1BCSCargado = true;
            	solCir.domVentana.find('#PanelCarta1BCS').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCarta1BCS.html\'" ></div>')($scope));
            	
            }
            
     	};
		// CARTA 2 SVS
     	solCir.fnTabCarta2SVS = function () {
            console.log('PanelCarta2SVS click');
            if (solCir.TabCarta2SVSCargado !== true) {
            	solCir.TabCarta2SVSCargado = true;
            	solCir.domVentana.find('#PanelCarta2SVS').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCarta2SVS.html\'" ></div>')($scope));
            }
     	};
     	// ** CIRCULAR 63
     	//NOMINA DE PERSONAS
     	solCir.fnTabCI63Nomina = function () {
            console.log('PanelCI63Nomina click');
            if (solCir.TabCI63NominaCargado !== true) {
            	solCir.TabCI63NominaCargado = true;
            	solCir.domVentana.find('#PanelCI63Nomina').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCI63Nomina.html\'" ></div>')($scope));
            }
     	};
     	//TRANSACCIONES DE RELACIONADOS
     	solCir.fnTabCI63Transa = function () {
            console.log('PanelCI63Transa click');
            if (solCir.TabCI63TransaCargado !== true) {
            	solCir.TabCI63TransaCargado = true;
            	solCir.domVentana.find('#PanelCI63Transa').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCI63Transa.html\'" ></div>')($scope));
            }
     	};
     	//CARTA DE RELACIONADOS CIR63
     	solCir.fnTabCI63Carta = function () {
            console.log('PanelCI63Carta click');
            if (solCir.TabCI63CartaCargado !== true) {
            	solCir.TabCI63CartaCargado = true;
            	solCir.domVentana.find('#PanelCI63Carta').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCI63Carta.html\'" ></div>')($scope));
            }
     	};     	
     	//NORMA DE CARACTER GENERA 380
       	solCir.fnTabNCG380 = function () {
            console.log('PanelNCG380 click');
            if (solCir.TabNCG380Cargado !== true) {
            	solCir.TabNCG380Cargado = true;
				solCir.domVentana.find('#PanelNCG380').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNCG380.html\'" ></div>')($scope));
            }
             
     	}; 
     	//INFORME MENSUAL INVERSIONISTAS RV
       	solCir.fnTabInfInversionistasRV = function () {
            console.log('PanelInformeMensualRV click');
            if (solCir.TabInformeMensualRV !== true) {
            	solCir.TabInformeMensualRV = true;
            	solCir.domVentana.find('#PanelInformeMensualRV').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelInformeMensualRV.html\'" ></div>')($scope));
            }
             
     	}; 
     	// ** CIRCULARES UAF
     	//CIRCULAR 48 PEP
     	
     	solCir.fnTabUAF48PEP = function () {
            console.log('PanelUAF48PEP click');
            if (solCir.TabUAF48PEPCargado !== true) {
            	solCir.TabUAF48PEPCargado = true;
            	solCir.domVentana.find('#PanelUAF48PEP').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAF48PEP.html\'" ></div>')($scope));
            }
     	};     	
     	
     	     	
     	solCir.fnTabUAFDOF = function () {
            console.log('PanelUAFDOF click');
            if (solCir.TabUAFDOFCargado !== true) {
            	solCir.TabUAFDOFCargado = true;
            	solCir.domVentana.find('#PanelUAFDOF').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAFDOF.html\'" ></div>')($scope));
            }
     	};     	

     	solCir.fnTabUAFROE = function () {
            console.log('PanelUAFROE click');
            if (solCir.TabUAFROECargado !== true) {
            	solCir.TabUAFROECargado = true;
            	solCir.domVentana.find('#PanelUAFROE').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAFROE.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabUAF57BFPEJ = function () {
            console.log('PanelUAF57BFPEJ click');
            if (solCir.TabUAF57BFPEJCargado !== true) {
            	solCir.TabUAF57BFPEJCargado = true;
            	solCir.domVentana.find('#PanelUAF57BFPEJ').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelUAF57BFPEJ.html\'" ></div>')($scope));
            }
     	};
     	     	
     
     	solCir.fnTabINFTRASEIL = function() {
     		console.log('PanelINFTRASEIL click');
     		if (solCir.TabINFTRASEILCargado !== true) {
     			solCir.TabINFTRASEILCargado = true;
     			solCir.domVentana.find('#PanelINFTRASEIL').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFTRASEIL.html\'" ></div>')($scope));
     		}
     	};
     	
       	solCir.fnTabINFTRABCS = function() {
     		console.log('PanelINFTRABCS click');
     		if (solCir.TabINFTRABCSCargado !== true) {
     			solCir.TabINFTRABCSCargado = true;
     			solCir.domVentana.find('#PanelINFTRABCS').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFTRABCS.html\'" ></div>')($scope));
     		}
     	};
     	

    	solCir.fnTabCIR1064 = function () {
            console.log('PanelCIR1064 click');
            if (solCir.TabCIR1064Cargado !== true) {
            	solCir.TabCIR1064Cargado = true;
            	solCir.domVentana.find('#PanelCIR1064').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1064.html\'" ></div>')($scope));
            }
     	};  
     	     	
     	
     	solCir.fnTabCIR1064R = function () {
            console.log('PanelCIR1064R click');
            if (solCir.TabCIR1064RCargado !== true) {
            	solCir.TabCIR1064RCargado = true;
            	solCir.domVentana.find('#PanelCIR1064R').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1064Rueda.html\'" ></div>')($scope));
            }
     	}; 
     	
     	solCir.fnTabCIR1064FR = function () {
            console.log('PanelCIR1064FR click');
            if (solCir.TabCIR1064FRCargado !== true) {
            	solCir.TabCIR1064FRCargado = true;
            	solCir.domVentana.find('#PanelCIR1064FR').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1064FueraRueda.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabFATCAR = function () {
            console.log('PanelFATCAR click');
            if (solCir.TabFATCARCargado !== true) {
            	solCir.TabFATCARCargado = true;
            	solCir.domVentana.find('#PanelFATCAR').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelFATCAR.html\'" ></div>')($scope));
            }
     	};      	
     	
     	solCir.fnTabFATCAD = function () {
            console.log('PanelFATCAD click');
            if (solCir.TabFATCADCargado !== true) {
            	solCir.TabFATCADCargado = true;
            	solCir.domVentana.find('#PanelFATCAD').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelFATCAD.html\'" ></div>')($scope));
            }
     	}; 
     	
     	solCir.fnTabINFDERECH = function () {
            console.log('INFDERECH click');
            if (solCir.TabINFDERECHCargado !== true) {
            	solCir.TabINFDERECHCargado = true;
            	solCir.domVentana.find('#PanelInformeDerecho').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelInformeDerechos.html\'" ></div>')($scope));
            }
     	}; 
     	
     	solCir.fnTabACPE = function () {
            console.log('PanelACPE click');
            if (solCir.TabACPECargado !== true) {
            	solCir.TabACPECargado = true;
            	solCir.domVentana.find('#PanelACPE').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelAnexoCuentaPropiaEspeciales.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabACPOB = function () {
            console.log('PanelACPOB click');
            if (solCir.TabACPOBCargado !== true) {
            	solCir.TabACPOBCargado = true;
            	solCir.domVentana.find('#PanelACPOB').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelAnexoCuentaPropiaOtrasBolsas.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabAMF = function () {
            console.log('PanelAMF click');
            if (solCir.TabAMFCargado !== true) {
            	solCir.TabAMFCargado = true;
            	solCir.domVentana.find('#PanelAMF').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelAnexoMontosFacturados.html\'" ></div>')($scope));
            }
     	};

       	solCir.fnTabCIR1518 = function () {
            console.log('PanelCIR1518 click');
            if (solCir.TabCIR1518Cargado !== true) {
            	solCir.TabCIR1518Cargado = true;
            	solCir.domVentana.find('#PanelCIR1518').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1518.html\'" ></div>')($scope));
            }
     	};
		 
		 solCir.fnTabCIRCMF = function () {
            console.log('PanelCIRCMF click');
            if (solCir.TabCIRCMFCargado !== true) {
            	solCir.TabCIRCMFCargado = true;
            	solCir.domVentana.find('#PanelCIRCMF').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIRCMF.html\'" ></div>')($scope));
            }
     	};

       	solCir.fnTabCIR1720 = function () {
            console.log('PanelCIR1720 click');
            if (solCir.TabCIR1720Cargado !== true) {
            	solCir.TabCIR1720Cargado = true;
            	solCir.domVentana.find('#PanelCIR1720').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1720.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabCIR1985 = function () {
            console.log('PanelCIR1985 click');
            if (solCir.TabCIR1985Cargado !== true) {
            	solCir.TabCIR1985Cargado = true;
            	solCir.domVentana.find('#PanelCIR1985').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1985.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabCIR1804 = function () {
            console.log('PanelCIR1804 click');
            if (solCir.TabCIR1804Cargado !== true) {
            	solCir.TabCIR1804Cargado = true;
            	solCir.domVentana.find('#PanelCIR1804').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR1804.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabNGC368PART = function () {
            console.log('PanelNGC368PART click');
            if (solCir.TabNGC368PARTCargado !== true) {
            	solCir.TabNGC368PARTCargado = true;
            	solCir.domVentana.find('#PanelNGC368PART').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNGC368PART.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabNGC368GRAV = function () {
            console.log('PanelNGC368GRAV click');
            if (solCir.TabNGC368GRAVCargado !== true) {
            	solCir.TabNGC368GRAVCargado = true;
            	solCir.domVentana.find('#PanelNGC368GRAV').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNGC368GRAV.html\'" ></div>')($scope));
            }
     	};

     	solCir.fnTabNCG365FFMM = function () {
            console.log('PanelNCG365FFMM click');
            if (solCir.TabNCG365FFMMCargado !== true) {
            	solCir.TabNCG365FFMMCargado = true;
            	solCir.domVentana.find('#PanelNCG365FFMM').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelNCG365FFMM.html\'" ></div>')($scope));
            }
     	};
 	    
     	
     	// CIRCULAR 20 - ANEXO 1
     	solCir.fnTabCIR20Anexo1= function () {
            console.log('PanelCIR20Anexo1 click');
            if (solCir.TabCIR20Anexo1Cargado !== true) {
            	solCir.TabCIR20Anexo1Cargado = true;
            	solCir.domVentana.find('#PanelCIR20Anexo1').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR20Anexo1.html\'" ></div>')($scope));
            }
     	};
     	
    	solCir.fnTabCIR20Anexo1Detalle= function () {
            console.log('PanelCIR20Anexo1Detalle click');
            if (solCir.TabCIR20Anexo1DetalleCargado !== true) {
            	solCir.TabCIR20Anexo1DetalleCargado = true;
            	solCir.domVentana.find('#PanelCIR20Anexo1Detalle').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelCIR20Anexo1Detalle.html\'" ></div>')($scope));
            }
     	};

		solCir.fnTabPacVigEBox= function () {
            console.log('PanelArchivoPactosVigentesEBox click');
            if (solCir.TabArchivoPactosVigentesEBoxCargado !== true) {
            	solCir.TabArchivoPactosVigentesEBoxCargado = true;
            	solCir.domVentana.find('#PanelPacVigEBox').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelArchivoPactosVigentesEBox.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabINFOFWDEBOXForwardsVigentes = function () {
            console.log('PanelFwdVigEbox click');
            if (solCir.TabINFOFWDEBOXForwardsVigentesCargado !== true) {
            	solCir.TabINFOFWDEBOXForwardsVigentesCargado = true;
            	solCir.domVentana.find('#PanelFwdVigEbox').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFOFWDEBOXForwardsVigentes.html\'" ></div>')($scope));
            }
     	};
     	
     	solCir.fnTabINFOFWDEBOXGarantiasForward = function () {
            console.log('PanelGarFwdEbox click');
            if (solCir.TabTabINFOFWDEBOXGarantiasForwardCargado !== true) {
            	solCir.TabTabINFOFWDEBOXGarantiasForwardCargado = true;
            	solCir.domVentana.find('#PanelGarFwdEbox').append($compile('<div ng-include="\'../../Normativa/UI/Circulares/SolicitarCircular/Paneles/PanelINFOFWDEBOXGarantiasForward.html\'" ></div>')($scope));
            }
     	};

   	     	
     	
/*********************************************
 * DATASOURCES
 *********************************************/
	// --------------------------------------------------
	// IMPRIMIR REPORTE
	// --------------------------------------------------
	solCir.ConsultaArchivoNormativa = new bcs.DataSource({
		type 		: "odata",
		definition 	: {
			serverFiltering	: true,
			transport		: {
				read : {
					url : "/BSCL/Plataforma/GWJAVA/PLA/call?t=1674",
					dataType : "json",
				}
			},
			schema 				: {
				model: {
					fields : {
					    IdArchivoNormativa		: {type: "string"},
						VersionArchivo			: {type: "string"},
						Esquema					: {type: "string"},
						Version 				: {type: "string"},
						IdTipoDocumento 		: {type: "string"},
						Descripcion				: {type: "string"},
						NombreArchivo			: {type: "string"},
						IdArchivo				: {type: "string"},
						TipoArchivo				: {type: "string"},
						ServicioArchivo			: {type: "string"},
						IdEmpresa				: {type: "number"},
						IdEstado				: {type: "string"}						
					}
				}
			}
		},
	});		
/*********************************************
 * COMBOBOX
 *********************************************/
	solCir.cmbbx.TipoDocumento={
			filtros : {
				IdNormativa : { field: "IdNormativa", operator: "eq", value: '-1' },
				UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'S' }
			}
		};		
	solCir.cmbbx.Normativa={
			filtros : {
				IdNormativa : { field: "IdEstado", operator: "eq", value: 'ACT' },
				UI_Emision  : { field: "PermiteSolicitar", operator: "eq", value: 'S' }
			}
		};	     	
	
/*********************************************
 * FUNCIONES
 ********************************************/
	
	solCir.fnPantallaPrev = function () {
		if (solCir.params.PrevNormativa==undefined ||solCir.params.PrevNormativa.idactual==0)
		{
			
		    solCir.cmbbx.TipoDocumento.filtros.value='-1';
			
		};
	};
		
	solCir.getReport = function(dataTemplate){
		var parameters = {
			"exportFormat"	: dataTemplate.TipoReporte,
			"reportId"		: dataTemplate.IdReporte,
			"parameters"	: {},
			"configuration"	: {
				"mainReport": {
					"reportName": dataTemplate.NombreReporte,
					"data"		: dataTemplate.data,
					"parameters": {}
				},
				"subReports": dataTemplate.subReports
			}
		};
		
		$http.post('/BSCL/Plataforma/GWJAVA/PLA/report?file', parameters, {"responseType": "arraybuffer"})
			.success(function (response) {
				var file = new Blob([response], {type: 'application/pdf'});
				//var fileURL = URL.createObjectURL(file);
				var nombreArchivo = dataTemplate.NombreReporte+'_'+$scope.SesionFechaSistema.replace(/-/g,'');
				saveAs(file, nombreArchivo+"."+dataTemplate.TipoReporte);
				bcsComunes.ventanaOverlay($scope.winID,false);
			})
			.error(function (response) {
				bcsComunes.msg("error",'Error',$translate.instant("UI_ERROR_AL_GENERAR_REPORTE"));
				bcsComunes.ventanaOverlay($scope.winID,false);
			});
	};
	
	solCir.fnAjustaPantalla = function () {
		if( solCir.cmbbx.IdNormativa =='NCG380')
		{
			bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,550);	
		}else{
			bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,250);
				switch (solCir.cmbbx.IdTipoDocumento){
					case "CI63Transa":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,280);
						break;
					case "UAFDOF":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,280);
						break;
					case "UAFROE":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,280);
						break;
					case "FATCAR":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,500);
						break;
					case "FATCAD":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,500);
						break;
					case "INFTRASEIL":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					case "INFTRABCS":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					case "NGC368GRAV":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					case "NGC368PART":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					default:
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,250);
						break;
					case "CIR1804":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					case "PACVIGEBOX":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					case "INFOFWDVIG":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					case "INFOGARFWD":
						bcsComunes.ventanaCambiarTamano(solCir.idVentana,solCir.AnchoVentana,400);
						break;
					
				};
		};
		
	
		
		
	};
	
	$scope.solCir.fnPantallaPrev();
	

	
	solCir.fnNormativa = function (newval, oldval) {
		if (solCir.cmbbx.IdNormativa)
		{
			if( solCir.cmbbx.IdNormativa =='NCG380')
			{
				solCir.cmbbx.TipoDocumento={
					filtros : {
						IdNormativa : { field: "IdNormativa", operator: "eq", value: '-1' }
					}
				};
				
			}else{
				solCir.cmbbx.TipoDocumento={
					filtros : {
						IdNormativa : { field: "IdNormativa", operator: "eq", value: solCir.cmbbx.IdNormativa },
						UI_Emision  : { field: "UI_Emision", operator: "eq", value: 'S' }
						
					}
				};
				
			};
		};	
		// solCir.fnAjustaPantalla();  //Se cambia la funcion fnAjustaPantalla, para autoajustar la ventana mas abajo
		solCir.domVentana = $("[ventanaid="+solCir.idVentana+"]").closest(".k-window");
		solCir.domVentana.css("height","auto");
		
		
	};
	
	
	
	//$scope.$watch('solCir.cmbbx.IdTipoDocumentoObj',solCir.fnTipoDocumento);
	$scope.$watch('solCir.cmbbx.IdTipoDocumentoObj',solCir.fnNormativa);


	
	
	
 	});

}());