(function() {
	angular.module('ModuloPlataforma').controller('ModalDiasFeriadosCtrl',
	function($scope,$translate,bcsComunes,servicioFeriado){
		
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/
	
		var modDF 		= this;
		modDF.idVentana	= bcsComunes.ventanaWinId($scope);
		modDF.parametros= bcsComunes.ventanaParams(modDF.idVentana);
		modDF.domVentana= $("[ventanaid='"+modDF.idVentana+"']");
		
		var feriadoSel = _.clone(modDF.parametros.FeriadoSel);
		
		modDF.win	= {};
		modDF.cmbbx	= {};
		modDF.fn	= {};
		
		modDF.datosFeriado	= {};
		modDF.datosFeriado.Fecha		= util.dateDS($scope.$root.SesionFechaSistema,"FechaSistema","CALL");
		modDF.datosFeriado.FechaInhabil = '';
		modDF.IdBotonAceptar= 'ModalDiasFeriados_aceptar' + modDF.parametros.AccionBoton;
		
		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		
		if(feriadoSel){
			bcsComunes.ventanaTitulo(modDF.idVentana, $translate.instant('UI_MODIFICAR_DIA_FERIADO'));
			
			modDF.datosFeriado.IdTipoDiaFeriado		= feriadoSel.Tipo || 0;
			modDF.datosFeriado.IdTipoDiaFeriadoDef	= feriadoSel.Tipo || 0;
			modDF.datosFeriado.Fecha				= feriadoSel.Fecha;
			modDF.datosFeriado.ValorParametro		= feriadoSel.Dia || 0;
			modDF.datosFeriado.ValorParametroDef	= feriadoSel.Dia || 0;
			modDF.datosFeriado.DescripcionDia		= feriadoSel.DescripcionDia || '';
			modDF.datosFeriado.AgnoCarga			= feriadoSel.AgnoCarga || '';
			modDF.datosFeriado.IdPais				= feriadoSel.IdPais || 0;
			modDF.datosFeriado.IdPaisDef			= feriadoSel.IdPais || 0;
			modDF.datosFeriado.IdEstado				= feriadoSel.IdEstado === 'ACT' ? true : false;
			modDF.win.modoEditar = true;
		}
		else{
			bcsComunes.ventanaTitulo(modDF.idVentana, $translate.instant('UI_AGREGAR_DIA_FERIADO'));
			modDF.datosFeriado.IdPaisDef			= $scope.$root.SesionIdPaisSistema;
			modDF.datosFeriado.IdEstado 			= true;
			modDF.win.modoEditar = false;
		}
		
		
		/*********************************************
		 * DATASOURCES
		 *********************************************/
		
		
		/*********************************************
		 * COMBOBOX
		 *********************************************/
		
		modDF.cmbbx.TipoDiaFeriado={
			filtros: {
				//IdPais	: { field: "IdPais",	operator: "eq", value: $scope.$root.SesionIdPaisSistema },
				IdEstado: { field: "IdEstado",	operator: "eq", value: "ACT" }
			},
		};
		
		modDF.cmbbx.Dia={
			filtros : {
				NombreTipoParametro : { field: "NombreTipoParametro",	operator: "eq", value: "DIASEMANAESPANOL" },
				IdEmpresa			: { field: "IdEmpresa",				operator: "eq", value: $scope.$root.SesionIdEmpresa },
				IdEstado			: { field: "IdEstado",				operator: "eq", value: "VIG" },
			},
			configuracion : {
				placeholder: $translate.instant('UI_DIA')
			},
		};
		
		
		/*********************************************
		 * FUNCIONES
		 *********************************************/
		
		modDF.fn.cancelar = function () {
			bcsComunes.ventanaData(modDF.idVentana, false);
			bcsComunes.ventanaCerrar(modDF.idVentana);
		};
		
		
		modDF.fn.aceptar = function () {
			var datosGuardar = {};
			datosGuardar.p_IdEmpresa 		= $scope.$root.SesionIdEmpresa; 
			datosGuardar.p_IdUsuario 		= $scope.$root.SesionIdUsuario;
			datosGuardar.p_IdModulo 		= "PLA";
			datosGuardar.p_UsaReglasNegocio	= "N";
			
			if(feriadoSel && feriadoSel.IdFeriado) { // EDITAR
				if(modDF.datosFeriado.Fecha					=== feriadoSel.FechaUI && 
				   Number(modDF.datosFeriado.ValorParametro)=== feriadoSel.Dia &&
				   modDF.datosFeriado.DescripcionDia		=== feriadoSel.DescripcionDia && 
				   Number(modDF.datosFeriado.AgnoCarga)		=== feriadoSel.AgnoCarga && 
				   modDF.datosFeriado.IdPais				=== feriadoSel.IdPais &&
				   modDF.datosFeriado.IdEstado				=== feriadoSel.IdEstado
				){
					bcsComunes.msg("warning","UI_DIAS_FERIADOS",$translate.instant("UI_SIN_MODIFICACIONES"));
					bcsComunes.ventanaData(modDF.idVentana, false);
					bcsComunes.ventanaCerrar(modDF.idVentana);
					return;
				}
				
				datosGuardar.p_IdFeriado		= feriadoSel.IdFeriado;
				datosGuardar.p_Fecha			= modDF.datosFeriado.IdTipoDiaFeriado === 'FERIADO' ? util.dateSPdate(modDF.datosFeriado.Fecha) : '0001-01-01';
				datosGuardar.p_Dia				= modDF.datosFeriado.ValorParametro;
				datosGuardar.p_DescripcionDia	= modDF.datosFeriado.DescripcionDia;
				datosGuardar.p_AgnoCarga		= modDF.datosFeriado.AgnoCarga;
				datosGuardar.p_IdPais			= modDF.datosFeriado.IdPais;
				datosGuardar.p_IdEstado			= modDF.datosFeriado.IdEstado ? "ACT" : "INA";
				
				datosGuardar.p_DetalleLog = [];
				if(modDF.datosFeriado.Fecha !== feriadoSel.FechaUI)
					datosGuardar.p_DetalleLog.push({SubRegistro:'DIAFERIADO', CampoRegistro:'Fecha', ValorAntiguo:feriadoSel.FechaUI, ValorNuevo:modDF.datosFeriado.Fecha});
				if(Number(modDF.datosFeriado.ValorParametro) !== feriadoSel.Dia)
					datosGuardar.p_DetalleLog.push({SubRegistro:'DIAFERIADO', CampoRegistro:'Dia', ValorAntiguo:feriadoSel.Dia, ValorNuevo:modDF.datosFeriado.ValorParametro});
				if(modDF.datosFeriado.DescripcionDia !== feriadoSel.DescripcionDia)
					datosGuardar.p_DetalleLog.push({SubRegistro:'DIAFERIADO', CampoRegistro:'DescripcionDia', ValorAntiguo:feriadoSel.DescripcionDia, ValorNuevo:modDF.datosFeriado.DescripcionDia});
				if(Number(modDF.datosFeriado.AgnoCarga) !== feriadoSel.AgnoCarga)
					datosGuardar.p_DetalleLog.push({SubRegistro:'DIAFERIADO', CampoRegistro:'AgnoCarga', ValorAntiguo:feriadoSel.AgnoCarga, ValorNuevo:modDF.datosFeriado.AgnoCarga});
				if(modDF.datosFeriado.IdPais !== feriadoSel.IdPais)
					datosGuardar.p_DetalleLog.push({SubRegistro:'DIAFERIADO', CampoRegistro:'IdPais', ValorAntiguo:feriadoSel.IdPais, ValorNuevo:modDF.datosFeriado.IdPais});
				if(modDF.datosFeriado.IdEstado !== feriadoSel.IdEstado)
					datosGuardar.p_DetalleLog.push({SubRegistro:'DIAFERIADO', CampoRegistro:'IdEstado', ValorAntiguo:feriadoSel.IdEstado, ValorNuevo:modDF.datosFeriado.IdEstado});
				
				bcsComunes.ventanaOverlay(modDF.idVentana,true);
				servicioFeriado.modificarDiaFeriado(datosGuardar,function (result){
					modDF.fn.evaluaRespuestaSP(result, 'UI_DIA_FERIADO_MODIFICADO');
					cargarFeriados();
				}, function(resError){
					bcsComunes.msg("error","UI_DIAS_FERIADOS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(modDF.idVentana,false);
				});
			}
			else { // CREAR
				datosGuardar.p_Tipo				= modDF.datosFeriado.IdTipoDiaFeriado;
				datosGuardar.p_Fecha			= modDF.datosFeriado.IdTipoDiaFeriado === 'FERIADO' ? util.dateSPdate(modDF.datosFeriado.Fecha) : '0001-01-01';
				datosGuardar.p_Dia				= modDF.datosFeriado.ValorParametro;
				datosGuardar.p_DescripcionDia	= modDF.datosFeriado.DescripcionDia;
				datosGuardar.p_AgnoCarga		= modDF.datosFeriado.AgnoCarga;
				datosGuardar.p_IdPais			= modDF.datosFeriado.IdPais;
				
				bcsComunes.ventanaOverlay(modDF.idVentana,true);
				servicioFeriado.crearDiaFeriado(datosGuardar,function (result){
					modDF.fn.evaluaRespuestaSP(result, 'UI_DIA_FERIADO_CREADO');
					cargarFeriados();
				}, function(resError){
					bcsComunes.msg("error","UI_DIAS_FERIADOS",$translate.instant("UI_REVISE_LOG"));
					bcsComunes.ventanaOverlay(modDF.idVentana,false);
				});
			}
		};
		
		
		modDF.fn.evaluaRespuestaSP = function(result, msj){
			if (result.P_OUT_REFID !== undefined && result.P_OUT_REFID > 0 && result.P_OUT_ERRORESCONTADOR == 0 && result.P_OUT_ADVERTENCIASCONTADOR == 0){
				bcsComunes.msg("info","UI_DIAS_FERIADOS",$translate.instant(msj));
			}
			else if(result.P_OUT_ADVERTENCIASCONTADOR !== undefined && result.P_OUT_ADVERTENCIASCONTADOR > 0){
				bcsComunes.msg("warning","UI_DIAS_FERIADOS",result.P_OUT_ADVERTENCIASDETALLE[0].Codigo);
			}
			else if(result.P_OUT_ERRORESCONTADOR !== undefined && result.P_OUT_ERRORESCONTADOR > 0){
				bcsComunes.msg("error","UI_DIAS_FERIADOS",result.P_OUT_ERRORESDETALLE[0].Codigo);
			}
			else bcsComunes.msg("error","UI_DIAS_FERIADOS",$translate.instant("UI_REVISE_LOG"));
			
			bcsComunes.ventanaOverlay(modDF.idVentana,false);
			bcsComunes.ventanaData(modDF.idVentana, true);
			bcsComunes.ventanaCerrar(modDF.idVentana);
		};
		
		
		$scope.$watch('modDF.datosFeriado.IdTipoDiaFeriado', function(newValue, oldValue){
			if(newValue && newValue !== oldValue){
				if(newValue === 'INHABIL'){
					modDF.datosFeriado.FechaInhabil = '01-01-0001';
					modDF.datosFeriado.ValorParametro = '';
				}
				else{
					var diaSemana = new Date(modDF.datosFeriado.Fecha).getDay();
					modDF.datosFeriado.ValorParametro = diaSemana ? diaSemana-1 : 6;
				}
			}
		});
		
		$scope.$watch('modDF.datosFeriado.Fecha', function(newValue, oldValue){
			if(newValue && newValue !== oldValue){
				var diaSemana = new Date(newValue).getDay();
				modDF.datosFeriado.ValorParametro = diaSemana ? diaSemana-1 : 6;
			}
		});

		function cargarFeriados(){
			var conf = new bcs.DataSource({ 
					type : "odata",
					definition : {
						serverFiltering : true, 
						serverPaging : true, 
						serverSorting : true,
						transport: {
							read: {
								url:"/BSCL/Plataforma/GWJAVA/PLA/call?t=623",
								dataType: "json"
							}
						},
						schema : {model: {
							fields : {
								IdPais	: {type: "number"}
								,Fecha : {type: "date"}
								,Tipo : {type: "string"}
								,Dia : {type: "number"}
								,DescripcionDia : {type: "string"}
								,AgnoCarga : {type: "number"}
								,IdEstado: {type: "string"}
							}
						}
					}
					}
				});
	        var confIna = angular.copy(conf);
			conf.query({filter:{field: "IdEstado", operator: "eq", value: "ACT"}}).then(function(){
				if(conf.data().length > 0){
					var datos = conf.data();
					$.each(datos,function(key,value){
						var fecha = value.Fecha;
						var fechaFormat = fecha.toISOString().substring(0, 10);
						bcs_feriados[value.IdPais][fechaFormat] = {
								Tipo : value.Tipo,
								Dia : value.Dia,
								DescripcionDia : value.DescripcionDia,
								AgnoCarga : value.AgnoCarga,							
						};
					});
				}
			});
			
			confIna.query({filter:{field: "IdEstado", operator: "eq", value: "INA"}}).then(function(){
				if(confIna.data().length > 0){
					var datos = confIna.data();
					$.each(datos,function(key,value){
						var fecha = value.Fecha;
						var fechaFormat = fecha.toISOString().substring(0, 10);
						if(bcs_feriados[value.IdPais] != null)
						{
						bcs_feriados[value.IdPais][fechaFormat] = undefined;
						}
					});
				}
			});
		}
	});
}());