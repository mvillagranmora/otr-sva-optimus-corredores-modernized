/*************************************************************
 * Purpose	: Panel Persona FATCA
 * Author	: TEAM SOCIO NEGOCIO - BCS
 * Date		: 27/07/2022
 *************************************************************/
 (function() {
	angular.module('ModuloSocioNegocio').controller('ModalPersonaFatcaCuentasBancariasExtranjerasCtrl',
	function($scope,$translate,bcsComunes,$resource,servicioSociodeNegocio,bcsAlerta,bcsServicioPlataforma){
		
		var mpfcbe = this;
		var idVentana = bcsComunes.ventanaWinId($scope);
		var parametros = bcsComunes.ventanaParams(idVentana);
		mpfcbe.IdPersona = parametros.IdPersona;
			
		mpfcbe.GrillaCuentasBancariasExt = {
			dataSource		: new bcs.DataSource({
				type 		: "odata",
				definition 	: {
					serverFiltering : true, 
					serverPaging 	: true, 
					serverSorting 	: true,
					sortable 		: true,
					pageSize 		: 5,
					transport: {
						read: {
							url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1310",
							dataType: "json"
						}
					},
					filter: [
						{ field: 'IdPaisOperacion'  , operator: "ne", value: $scope.$root.SesionIdPaisSistema}, //Extranjeras.
						{ field: 'IdPersona'		, operator: "eq", value: Number(mpfcbe.IdPersona)},
						{ field: 'IdEstado' 		, operator: "eq", value: 'ACT'}
					],
					schema 	 : {
						model	: { fields	: {		 	
								IdCuentaBancaria			: { type: "number"},
								IdBanco						: { type: "number"},
								IdPersona					: { type: "number"},
								NombreBanco					: { type: "string"},
								IdPaisOperacion				: { type: "number"},
								EsFatca						: { type: "string"},
								NombrePais					: { type: "string"},
								NombreTipoCuentaBancaria	: { type: "string"},
								NumeroCuentaBancaria		: { type: "string"},
								IdEstado					: { type: "string"},
								NombreEstado				: { type: "string"},
								NombreMoneda				: { type: "string"},
								// TODO: ORDENES PERMANENTES PENDIENTE.
								OrdenesPendientes    		: { type: "string"},
							} 
						}
					}
				}
			}),
			columns			: [
			    {field: "NombreTipoCuentaBancaria"	,width: "120px",title: $translate.instant("UI_TIPO_CUENTA")							,attributes : {style : "text-align:left;"}},
				{field: "NumeroCuentaBancaria"		,width: "100px",title: $translate.instant("UI_NUMERO_CUENTA")						,attributes : {style : "text-align:left;"}},		
				{field: "NombreBanco"				,width: "150px",title: $translate.instant("UI_BANCO")								,attributes : {style : "text-align:left;"}},
				{field: "NombrePais"				,width: "100px",title: $translate.instant("UI_PAIS")								,attributes : {style : "text-align:left;"}},
				{field: "OrdenesPendientes"			,width: "150px",title: $translate.instant("UI_ORDENES_PERMANENTES")					,attributes : {style : "text-align:left;"}},
				{field: "EsFatca"					,width: "100px",title: $translate.instant("UI_ESFATCA")								,attributes : {style : "text-align:left;"}},
			],		
			sortable			: true,
			filterable			: false,
			groupable			: false,
			pageable : {
		    	refresh: true,
		       	buttonCount: 5
		    },
			autoBind			: true,
			height				: 200,
		    selectable 			: "row"
		 };


        mpfcbe.agregarEsFatca = function agregarEsFatca(){

				var registroCambio = [];
				registroCambio.push({CampoModificado :"EsFatca", ValorNuevo : "S"});
				
				var datos = {
					p_IdEmpresa :$scope.SesionIdEmpresa,
					p_IdUsuario :$scope.SesionIdUsuario,
					p_IdPersona :mpfcbe.IdPersona,
					p_IdModulo  :'SOC',
					p_UsaReglasNegocio :'S',
					p_IdCuentaBancaria: mpfcbe.GrillaCuentasBancariasExtSeleccionado.IdCuentaBancaria,
					p_RegistroCambio: registroCambio,
				};
				bcsComunes.ventanaOverlay(idVentana,true);
				servicioSociodeNegocio.modificarPersonaCuentaBancaria(datos).$promise.then(
					function(result) {	
						var idMensaje = bcsComunes.ventanaMsg({ mensaje : "Cuenta bancaria modificada",
							conflictos : util.parseServerResponse(result)}, idVentana);
						
						bcsComunes.ventanaPromesa(idMensaje).then(function(){
							bcsComunes.ventanaData(idVentana,true);
							// bcsComunes.ventanaCerrar(mpfcbe.idVentana);
							bcsComunes.ventanaOverlay(mpfcbe.idVentana,false);	
						});	
					},
					function(result){
						console.log(result);
						bcsComunes.msg("error","error","Error al modificar cuenta bancaria");
						bcsComunes.ventanaData(idVentana,true);
						// bcsComunes.ventanaCerrar(mpfcbe.idVentana);
						bcsComunes.ventanaOverlay(idVentana,false);
					}
				);
		};
		mpfcbe.Cancelar = function Cancelar (){
			bcsComunes.ventanaData(idVentana,false);
			bcsComunes.ventanaCerrar(idVentana);
		};
	});
}());
//------------------------- FIN CONTROLLER --------------------------