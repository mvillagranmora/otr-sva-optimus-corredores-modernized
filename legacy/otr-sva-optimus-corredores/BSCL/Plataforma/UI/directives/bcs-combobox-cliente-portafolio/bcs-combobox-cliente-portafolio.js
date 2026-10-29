angular.module('ModuloPlataforma').directive('bcsComboboxClientePortafolio', function($modal,$translate,$timeout,bcsComunes) {
	return {
		restrict: 'E',
		replace: false,
		scope: {
			datosPortafolio : '=?',
			kOptions: '=?',
			ngModel : '=?',
			kNgModel : '=?',
			kNgDisabled : '=?',
			kNgReadonly : '=?',
			muestra : '=?',
			todos : '=?',
			filtroIdCliente : '=?',
			filtroClienteHabilitado : '=?',  // S/N
			filtroEstadoPortafolio : '=?',	 // 'ACT'
			glosaWidget: '@?',
		},
		template    : '<span class="k-textbox k-space-right" style="width: 100%;" >'+
     				'     <input class="bcs-autocomplete" kendo-auto-complete k-ng-delay="kOptions" k-ng-disabled="kNgDisabled" k-options="kOptions" ng-model="ngModel" k-ng-model="kNgModel"   />'+                    
     				'     <a href="#" ng-click="fnBusquedaCliente()" class="k-select k-icon k-i-search">&nbsp;</a>'+
					'</span>'+
					'<div ng-show="muestra">'+ 
					'     <div ng-show="datosPortafolio.IdPortafolio !== undefined">Cliente: {{datosPortafolio.DocumentoIdentidad}} - Portafolio: {{datosPortafolio.CodigoPortafolio}}</div>'+
					'</div>',
			controller : function($scope){
			this.getSeleccionado = function(){
				return ($scope.datosPortafolio && $scope.datosPortafolio.IdPortafolio);
			}
		},
		link: function(scope, element, attrs, fn) {
		$timeout(function(){
			//Valores iniciales
			scope.datosPortafolio={};
			scope.busquedaMinima = 3;				
			scope.muestra=scope.muestra||false;
			var filtros = new Array();
			scope.enfoco = false;
			if (scope.kNgDisabled == undefined) scope.kNgDisabled=false;
			if (scope.kNgReadonly == undefined) scope.kNgReadonly=false;
									
			//Evento de creacion del widget
			scope.$on("kendoWidgetCreated", function (event, widget) {
				if (widget.$angular_scope === scope) {
					//widget
					scope.kendoWidget = widget;
					scope.kendoWidget.popupCerrar = false;		
					//memoria del control
					scope.datosPortafolio=sesion.getPreferencias("bcsComboboxClientePortafolio")||{};			
					scope.ngModel=scope.fnTextoInput();
					//perdida del foco
					scope.kendoWidget.element.on("blur", function() {
						scope.kendoWidget.popupCerrar = true;				
						scope.kendoWidget.close();
						scope.kendoWidget.popupCerrar = false;
						scope.kendoWidget.value(scope.fnTextoInput());
						scope.enfoco = false;
					});	
					scope.kendoWidget.element.on("focus", function() {
						scope.enfoco = true;
						scope.kendoWidget.value(scope.fnTextoRInput());
					});						
				}					
			});
			
			//Busqueda de cliente 
			scope.fnBusquedaCliente = function(e) {
				//TODO: siguiente sprint
				
			};
			
			scope.$watch("datosPortafolio",function(newval,oldval) {
				if (newval && newval.IdPortafolio) {
					var glosa = scope.glosaWidget?scope.glosaWidget : "Cliente";
					newval.glosa = glosa;
					bcsComunes.setClientePortafolio(newval.IdPortafolio,newval);
					if (scope.enfoco===false) scope.kendoWidget.value(scope.fnTextoInput());	
				}
				if (newval===false) { //clean datos
					scope.datosPortafolio = { };
					scope.ngModel = "";
					scope.kendoWidget.value(scope.fnTextoInput());
				}
			});
			
			scope.tPortafolio = $translate.instant('UI_PORTAFOLIO');
			
			scope.fnTextoInput= function() {
				if (typeof scope.datosPortafolio === 'undefined') return "";	
				if (typeof scope.datosPortafolio.DocumentoIdentidadPortafolio === 'undefined') return "";
				return scope.datosPortafolio.DocumentoIdentidadPortafolio.replace(":UI_PORTAFOLIO:", scope.tPortafolio);
			};
			
			scope.fnTextoRInput= function() {
				if (typeof scope.ngModel === 'undefined') return "";	
				var x=scope.ngModel.replace(' :'+scope.tPortafolio+': ', ' ');
				var x=x.replace(' '+scope.tPortafolio+' ', ' ');				
				return x;
			};
			
			//Parametros del control
			var defaultOptions =
			{
					placeholder: scope.tPortafolio,
					headerTemplate: '<div class="noDataMessage">No se encontrarón resultados.</div>',
					fixedGroupTemplate: "Rut: #:data#",
					groupTemplate: "Rut: #:data#",
		            dataTextField: "DocumentoIdentidadPortafolio",
		            dataValueField: "IdPortafolio",
		            filter: "contains",
		            autoBind: false,	
					height: 300,
					delay: 800,
					//template: $.proxy(kendo.template("#= util.fnFormatHighlight(RazonSocial+' - "+scope.tPortafolio+": '+CodigoPortafolio+' ('+Proposito+')', KInputVal ) #"), this.dataSource),
					dataBound: function() {
					  var noItems = this.list.find(".noDataMessage");
					  var khg = this.list.find(".k-group-header");
					  if (!this.dataSource.view()[0] && scope.enfoco) {
						noItems.show();
						this.popup.open();						
						khg.hide();
					  } else {
						noItems.hide();
						khg.show();
					  }
					},
					close: function(e) {
						var widget = e.sender;
						if (scope.ngModel.length>0 && !widget.popupCerrar && !this.dataSource.view()[0]) {
							e.preventDefault();
						}
					},	
					filtering: function(e) {
						if (typeof scope.ngModel === 'undefined' || scope.ngModel.length<scope.busquedaMinima) {							
							this.popup.close();	
							e.preventDefault();
							scope.datosPortafolio={};
						}else {
							//Inyecta una busqueda mas completa
							if (e.filter.field=='DocumentoIdentidadPortafolio') {
								e.preventDefault();								
							}							
						}
					},	
					select: function(e) {
						var dataItem = this.dataItem(e.item.index());
						scope.datosPortafolio=dataItem;
						sesion.setPreferencias("bcsComboboxClientePortafolio",scope.datosPortafolio);
					},					
		            dataSource: new kendo.data.DataSource({
		            	type: "odata",
		            	serverFiltering: true,						
                        group: { field: "DocumentoIdentidad" },
		            	transport: {
		            		read: {   		            				
		            			url: "/BSCL/Plataforma/GWJAVA/PLA/call?t=1543",
								dataType:"json"
		            		}
		            	},
                        schema : {
							model : {
								fields : {
									IdCliente : {type : "number"},									
									DocumentoIdentidadPortafolio : {type : "string"},
									TextoBusquedaPortafolio : {type : "string"},
									EstaHabilitado : {type : "string"},
									DocumentoIdentidad : {type : "string"},
									RazonSocial : {type : "string"},
									IdPortafolio : {type : "number"},
									CodigoPortafolio : {type : "string"},
									Proposito : {type : "string"},
									EstadoPortafolio : {type : "string"},
								}
							},
		            	},
		            	requestStart: function(e) {
							//filtros Iniciales antes de llamar
							if (typeof scope.ngModel === 'undefined' || scope.ngModel.length<scope.busquedaMinima) {
								e.preventDefault();
								scope.datosPortafolio={};
							}
						},
						requestEnd: function(e) {							 
							//borra los repetidos
							var result = [];
							for (i=0;i<e.response.d.results.length;i++)
							{
								e.response.d.results[i].KInputVal=scope.ngModel;
								if (i>0)
								{
									if (e.response.d.results[i].IdPortafolio !== e.response.d.results[i-1].IdPortafolio)
									{
										result.push(e.response.d.results[i]);
									}
								}
								else
									result.push(e.response.d.results[i]);
							}
							//procesa para auto sel
							var autosel = false;
							for (i=0;i<result.length;i++)
							{
								//Auto selecciona el primero
								var cportafolio=result[i].DocumentoIdentidad + " " + result[i].CodigoPortafolio;
								var txt=scope.fnTextoRInput();
								if (txt.toUpperCase()===result[i].DocumentoIdentidad.toUpperCase() || 
										txt.toUpperCase()===result[i].RazonSocial.toUpperCase()  || 
										txt.toUpperCase()===cportafolio.toUpperCase()) {
									scope.datosPortafolio=result[i];
									sesion.setPreferencias("bcsComboboxClientePortafolio",scope.datosPortafolio);
									autosel=true;
								}
							}
							if (!autosel) {
								scope.datosPortafolio={};
							}														
							e.response.d.results = result;
					    }
		            })
			};
			
			
			//hack filtro para busqueda por texto libre
			scope.$watchGroup(['ngModel'], function(newValue, oldValue) {					
				var txt=scope.fnTextoRInput();	
				if (txt) {
					var filtros = new Array();
					if (scope.filtroIdCliente)
						filtros.push({
					        "field"   : "IdCliente ",
					        "operator": "eq",
					        "value"   : scope.filtroIdCliente
						});						
					if (scope.filtroClienteHabilitado)
						filtros.push({
					        "field"   : "EstaHabilitado",
					        "operator": "eq",
					        "value"   : scope.filtroClienteHabilitado
						});										
					if (scope.filtroEstadoPortafolio)
						filtros.push({
					        "field"   : "EstadoPortafolio",
					        "operator": "eq",
					        "value"   : scope.filtroEstadoPortafolio
						});															
					filtros.push({
				        "field"   : "TextoBusquedaPortafolio",
				        "operator": "contains",
				        "value"   : txt.toUpperCase()
					});
					scope.kOptions.dataSource.filter(filtros);
				}		
			}, true);						
			//filtro automatico
			scope.$watchGroup(['filtroIdCliente','filtroClienteHabilitado','filtroEstadoPortafolio'], function(newValue, oldValue) {				
				var filtros = new Array();
				if (scope.filtroIdCliente)
					filtros.push({
				        "field"   : "IdCliente ",
				        "operator": "eq",
				        "value"   : scope.filtroIdCliente
					});					
				if (scope.filtroClienteHabilitado)
					filtros.push({
				        "field"   : "EstaHabilitado",
				        "operator": "eq",
				        "value"   : scope.filtroClienteHabilitado
					});								
				if (scope.filtroEstadoPortafolio)
					filtros.push({
				        "field"   : "EstadoPortafolio",
				        "operator": "eq",
				        "value"   : scope.filtroEstadoPortafolio
					});				
				scope.kOptions.dataSource.filter(filtros);									
			}, true);			
			//Opciones defaults			
			scope.kOptions = $.extend( {}, defaultOptions, scope.kOptions || {} );
		});
		}
	};
});

