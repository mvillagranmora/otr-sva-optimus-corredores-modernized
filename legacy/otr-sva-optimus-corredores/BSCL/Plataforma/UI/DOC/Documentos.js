angular.module('ModuloPlataforma').controller('DocumentosCtrl',function($scope,$uibModal,$translate, bcsComunes,bcsServicioPlataforma){
		

			$scope.datos = {};
			$scope.confBasica = {};
			$scope.filtros = {};
			$scope.gridApi = {};
			$scope.seleccion = {};
			$scope.pestanas = [];
			var tabSeleccionado = "";
		
			
			
			
			getClasificacion = function () {
				//Lista la clasificacion para pintar tabs
				var listaAtributosFnAgregarDataSource = new kendo.data.DataSource
			       (
				     {		
				       type:"odata",		
		               serverFiltering: true,
			           serverSorting: true,  //ordena al lado del servidor
			           sort: { field: "Clasificacion", dir: "asc" }, //campo y criterio de ordenamiento
		               transport: 
		                 {
		                   read: 
		                    {	
		                      url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1545",
		                      dataType: "json" 
		                    },
		                 },
		             }
			       );
				
				listaAtributosFnAgregarDataSource.query(
					{
					  filter:
						[	        	  
		        		  { field: "IdCliente", operator: "eq", value: 10000 }, 
			      		]
					}
				).then(function()
				  {
					var viewAtriFnAgregar = listaAtributosFnAgregarDataSource.view();
					
					
					for (var i = 0; i < viewAtriFnAgregar.length;i++)
					  {
						$scope.pestanas.push({Clasificacion : viewAtriFnAgregar[i].Clasificacion});
					  }
				  });		
			};
			
			getClasificacion();
			
			transporte = function(Grid) {
				if (Grid === 'Anexo')
					url = "/BSCL/Plataforma/GWJAVA/PLA/call?t=1546";		
				return {
					read : {
						url : url,
						dataType : "json"
					}
				};
			};  
			
			ModeloAnexos= function() {
				return {
					model: {
						fields:{
	    	        		IdCliente 				: { type : "number" },
	    	        		IdAnexo 				: { type : "number" },
	    	        		IdDocumento				: { type : "number" },
	    		            IdTipoDocumento 		: { type : "string" },
	    		            FirmaEmpresa1   		: { type : "string" },
	    		            IdUsuarioEmpresa1 		: { type : "number" },
	    		            NombreFirmanteEmpresa1  : { type : "string" },
	    		       
	    		            FirmaEmpresa2  			: { type : "string" },
	    		            IdUsuarioEmpresa2 		: { type : "number" },
	    		            NombreFirmanteEmpresa2 	: { type : "string" },
	    		            
	    		            FirmaEmpresa3   		: { type : "string" },
	    		            IdUsuarioEmpresa3 		: { type : "number" },
	    		            NombreFirmanteEmpresa3  : { type : "string" },
	    		            
	    		            NombreFirmanteCliente1  : { type : "string" },
	    		            FirmaCliente1   		: { type : "string" },
	    		            IdUsuarioCliente1 		: { type : "number" },
	    		            
	    		            NombreFirmanteCliente2  : { type : "string" },
	    		            FirmaCliente2   		: { type : "string" },
	    		            IdUsuarioCliente2 		: { type : "number" },
	    		            
	    		            NombreFirmanteCliente3  : { type : "string" },
	    		            FirmaCliente3   		: { type : "string" },
	    		            IdUsuarioCliente3 		: { type : "number" },
	    		            
	    		            FechaVigencia			: { type : "date"}
	    	        	} 
					}
				};
			}; 
		   
			$scope.onTabSelect=function(tabName){
				tabSeleccionado = tabName;
				var filters = new Array()
				filters.push({field: "Clasificacion", operator: "eq", value: tabName });
				filters.push({field: "IdCliente", operator: "eq", value: 10000 });
				if($scope.gridApi[tabName] !== undefined)
					$scope.gridApi[tabName].dataSource.filters(filters);
			};
			
			dataSourceAnexos = function(){
				return {
					type 		: "odata", //Los tipos posibles son: odata, call, array y object
					definition 	: {
									serverFiltering 	: true, // Filtrado de datos en el servidor.
									serverPaging 		: true, // Paginado de los datos en el servidor.
									serverSorting 		: true, // Ordenamiento de los datos en el servidor.
									sortable 			: true,
									pageSize 			: 50,
									transport 			: transporte('Anexo'),
									schema 				: ModeloAnexos(),
									filtersFn 			: function (row) {
		                				var filterDetalle = new Array();
		                					filterDetalle.push({ field: "IdCliente", operator: "eq", value: 10000 });
		                					filterDetalle.push({field: "Clasificacion", operator: "eq", value: tabSeleccionado });
											return filterDetalle;
				  						}
									
								  }
				};
			};
			
		
						
			
			columnasAnexos = function() {
				return  [{
					field: "IdTipoDocumento",
					title: "Tipo Documento",
					width: "120px",
					attributes:{style:"text-align:left;"}
				},
				{
					field: "FechaVigencia",
					title: "Fecha Vigencia",
					width: "120px",
					attributes:{style:"text-align:center;"},
					format: "{0:dd/MM/yyyy}"
				},
				{
					field: "IdAnexo",
					title: "Anexo",
					width: "120px",
					attributes:{style:"text-align:left;"}
				},
				{
					field: "IdDocumento",
					title: "Documento",
					width: "120px",
					attributes:{style:"text-align:left;"}
				}];
			};
			
			
			
			$scope.Anexos = {
					dataSource			: dataSourceAnexos(),
					columns				: columnasAnexos(),		
					sortable			: true,
					pageable			: true,
					autoBind			: true,  //comentar
					height				: 437,
				    selectable 			: "row",
				  		};
			
			
			
			
			$scope.abrirDetalle=function(tabName){
				console.log($scope.seleccion[tabName]);
				
				
				var ventanaId = bcsComunes.ventanaAbrir("FirmaAnexo",$scope.seleccion[tabName],true,true);
				var promesa = bcsComunes.ventanaPromesa(ventanaId);   
				
				$.when(promesa).then(function(){
					$scope.gridApi[tabName].dataSource.read();
	             }); 
			};
			
		    
		});