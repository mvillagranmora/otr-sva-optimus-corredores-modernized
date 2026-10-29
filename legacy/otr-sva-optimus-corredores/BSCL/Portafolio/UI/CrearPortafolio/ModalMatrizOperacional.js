/********************************************************************
 * Purpose	: Controlador para visualizar matriz operacional 
 * de un perfil comercial.
 * Author	: JGarces - BCS
 * Date		: 29/02/2016
 *******************************************************************/

(function() {
	angular.module('ModuloPortafolio').controller('ModalMatrizOperacionalCtrl',
	function($q,$state,$scope,$translate,bcsComunes,bcsPerfilComercial,bcsConfiguracionMatrizPerfilComercial,bcsComunes){
	
		/*********************************************
		 * VARIABLES Y CONTEXTOS
		 ********************************************/		
		var modMatriz 			= this;
		modMatriz.idVentana 	= bcsComunes.ventanaWinId($scope);
		modMatriz.parametros 	= bcsComunes.ventanaParams(modMatriz.idVentana);
		modMatriz.domVentana 	= $("[ventanaid='"+modMatriz.idVentana+"']");	
		modMatriz.win 			= {};
		modMatriz.datos 		= {};
		modMatriz.grid			= {};
		modMatriz.cmbbx 		= {};
		modMatriz.ds 			= {};
		modMatriz.fn 			= {};		
		
		/*********************************************
		 * EVALUACION INICIAL
		 ********************************************/
		if(!modMatriz.parametros.PerfilComercialObj) {
			bcsComunes.msg("error","Abrir Ventana",'No se recibe identificador del cliente');
			bcsComunes.ventanaCerrar(modMatriz.idVentana);
		}

		/*********************************************
		 * ACCIONES DE PANTALLA
		 ********************************************/
		modMatriz.win.cerrarVentana = function(){
			bcsComunes.ventanaCerrar(modMatriz.idVentana);
		};

		/*********************************************
		 * DATASOURCES
		 *********************************************/
		modMatriz.ds.PerfilCobros = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1327",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdInternal 					: { type: "number"},
							Prioridad 					: { type: "number"},
							IdPerfilCobros 				: { type: "string"},
							DescripcionPerfilCobros 	: { type: "string"},
							IdEmpresa 					: { type: "number"},
							EstadoPerfilCobros 			: { type: "string"},
							IdGrupoCobrosNormal 		: { type: "string"},
							IdBolsa 					: { type: "string"},
							IdTipoProducto 				: { type: "string"},
							IdTipoOperacion 			: { type: "string"},
							IdCanalIngreso 				: { type: "string"},
							DiasPlazo 					: { type: "number"},
							EstadoDetalle 				: { type: "string"},
							IdCobro 					: { type: "string"},
							ValorCobro 					: { type: "number"},
							ValorTopeMinimoOrden 		: { type: "number"},
							IdMoneda 					: { type: "string"},
							EstadoDetalleGrupoCobroNormal : { type: "string"},
							NombreCobro 				: { type: "string"},
							IdCobroClasificacion 		: { type: "number"},
							FormaCobro 					: { type: "string"},
							PermiteCambiarValor 		: { type: "string"},
							CodigoCobroClasificacion 	: { type: "string"},
							CobroCalculado 				: { type: "number"}
						}
					}
				}
			}
		});

		modMatriz.ds.PerfilProductos = new bcs.DataSource({
			type: "odata",
			definition : {
				serverFiltering:true,
				transport: {
					read: {
						url :"/BSCL/Plataforma/GWJAVA/PLA/call?t=1328",
						dataType: "json"
					}
				},
				schema: {
					model: {
						fields: {
							IdPerfilProductos 			: { type: "string"},
							DescripcionPerfilProductos 	: { type: "string"},
							IdCategoriaInversionista 	: { type: "string"},
							FechaCreacion 				: { type: "date"},
							FechaModificacion 			: { type: "date"},
							EstadoPerfilProductos 		: { type: "string"},
							PorDefecto 					: { type: "string"},
							IdEmpresa 					: { type: "int"},
							IdTipoProducto 				: { type: "string"},
							IdTipoOperacion 			: { type: "string"},
							IdCanalIngreso 				: { type: "string"},
							IdMoneda 					: { type: "string"},
							EstadoProducto 				: { type: "string"}
						}
					}
				}
			}
		});
		
		/*********************************************
		 * GRILLAS
		 *********************************************/
		modMatriz.grid.matrizOperacional = bcsConfiguracionMatrizPerfilComercial.getConfiguracionGridMatrizOperacional();

		/*********************************************
		 * COMBOBOX
		 *********************************************/

		/*********************************************
		 * FUNCIONES
		 *********************************************/
		modMatriz.fn.iniciar = function()
		{
			var prom1 = modMatriz.ds.PerfilCobros.query({ filter: { field: "IdPerfilCobros", operator: "eq", value: modMatriz.parametros.PerfilComercialObj.IdPerfilCobros }});
			var prom2 = modMatriz.ds.PerfilProductos.query({ filter: { field: "IdPerfilProductos", operator: "eq", value: modMatriz.parametros.PerfilComercialObj.IdPerfilProductos }});
			$q.all([prom1,prom2]).then(function(){
				var dataMatrizOperacional = bcsPerfilComercial.generaMatrizOperacional(modMatriz.ds.PerfilProductos.data(),modMatriz.ds.PerfilCobros.data());
				modMatriz.grid.matrizOperacional.dataSource.data(dataMatrizOperacional);
			});
		};

		/*********************************************
		 * GOOOO
		 *********************************************/
		modMatriz.fn.iniciar();
	});
}());